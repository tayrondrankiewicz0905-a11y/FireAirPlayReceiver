package de.tayron.fireairplayreceiver

import android.content.Context
import android.net.wifi.WifiManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.io.BufferedInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Experimental discovery and RTSP skeleton; not a complete Apple AirPlay receiver. */
class AirPlayServer(private val context: Context, private val onStatus: (String) -> Unit) {
    private val running = AtomicBoolean(false)
    private val pool = Executors.newCachedThreadPool()
    private var sockets = mutableListOf<ServerSocket>()
    private var lock: WifiManager.MulticastLock? = null
    private val registrations = mutableListOf<NsdManager.RegistrationListener>()
    private var nsd: NsdManager? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            lock = wifi.createMulticastLock("fire-airplay-mdns").apply { setReferenceCounted(false); acquire() }
            nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            register("_airplay._tcp.", 7000, true)
            register("_raop._tcp.", 7001, false)
            pool.execute { listen(7000) }; pool.execute { listen(7001) }
            onStatus("Netzwerk-Grundserver aktiv")
        } catch (e: Exception) { onStatus("Startfehler: ${e.message}") }
    }
    private fun register(type: String, port: Int, airplay: Boolean) {
        val info = NsdServiceInfo().apply {
            serviceName = if (airplay) "Fire AirPlay Receiver" else "FireAudioReceiver"
            serviceType = type; setPort(port)
            if (airplay) { setAttribute("deviceid", "02:00:00:00:00:01"); setAttribute("features", "0x5A7FFFF7,0x1E"); setAttribute("model", "AppleTV3,2"); setAttribute("srcvers", "220.68") }
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) { onStatus("Im WLAN sichtbar: ${serviceInfo.serviceName}") }
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) { onStatus("WLAN-Erkennung fehlgeschlagen: $errorCode") }
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {}
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        }
        registrations.add(listener); nsd?.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
    }
    private fun listen(port: Int) {
        try {
            val ss = ServerSocket(port, 20, InetAddress.getByName("0.0.0.0")); synchronized(sockets) { sockets.add(ss) }
            while (running.get()) pool.execute { handle(ss.accept()) }
        } catch (e: Exception) { if (running.get()) onStatus("Port $port: ${e.message}") }
    }
    private fun handle(socket: Socket) {
        socket.use { client ->
            try {
                client.soTimeout = 5000
                val input = BufferedInputStream(client.getInputStream()); val out = client.getOutputStream()
                while (running.get()) {
                    val header = readHeader(input) ?: break
                    val cseq = Regex("(?im)^CSeq:\\s*(\\d+)").find(header)?.groupValues?.get(1) ?: "1"
                    val method = header.substringBefore(' ')
                    val response = if (method.equals("OPTIONS", true))
                        "RTSP/1.0 200 OK\r\nCSeq: $cseq\r\nPublic: OPTIONS, GET, POST, SETUP, RECORD, PAUSE, TEARDOWN, SET_PARAMETER\r\nServer: FireAirPlay/1.0\r\nContent-Length: 0\r\n\r\n"
                    else "RTSP/1.0 501 Not Implemented\r\nCSeq: $cseq\r\nServer: FireAirPlay/1.0\r\nContent-Length: 0\r\n\r\n"
                    out.write(response.toByteArray(Charsets.US_ASCII)); out.flush()
                    if (method.equals("TEARDOWN", true)) break
                }
            } catch (_: Exception) {}
        }
    }
    private fun readHeader(input: BufferedInputStream): String? {
        val b = StringBuilder(); var state = 0
        while (b.length < 16384) {
            val c = input.read(); if (c < 0) return if (b.isEmpty()) null else b.toString(); b.append(c.toChar())
            state = when { state == 0 && c == 13 -> 1; state == 1 && c == 10 -> 2; state == 2 && c == 13 -> 3; state == 3 && c == 10 -> return b.toString(); else -> if (c == 13) 1 else 0 }
        }
        return b.toString()
    }
    fun stop() {
        running.set(false); synchronized(sockets) { sockets.forEach { try { it.close() } catch (_: Exception) {} }; sockets.clear() }
        registrations.forEach { try { nsd?.unregisterService(it) } catch (_: Exception) {} }
        try { lock?.release() } catch (_: Exception) {}; pool.shutdownNow()
    }
}
