package de.tayron.fireairplayreceiver

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var server: AirPlayServer
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(Color.rgb(12, 14, 20))
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(32, 24, 32, 24) }
        fun label(text: String, size: Float, color: Int) = TextView(this).apply { this.text = text; textSize = size; setTextColor(color); gravity = Gravity.CENTER }
        layout.addView(label("Fire AirPlay Receiver", 30f, Color.WHITE))
        layout.addView(label("iPhone-Bildschirm auf Fire TV spiegeln", 18f, Color.LTGRAY))
        status = label("Starte Netzwerkdienste …", 16f, Color.rgb(120, 210, 255))
        layout.addView(status)
        layout.addView(label("iPhone und Fire TV müssen im selben WLAN sein. Öffne auf dem iPhone das Kontrollzentrum → Bildschirmsynchronisierung.\n\nWICHTIG: Dies ist eine Eigenentwicklung im frühen Protokollstadium. Die vollständige AirPlay-Video-/Audioübertragung ist noch nicht implementiert.", 16f, Color.WHITE))
        setContentView(layout)
        server = AirPlayServer(this) { msg -> runOnUiThread { status.text = msg } }
        server.start()
    }
    override fun onDestroy() { if (::server.isInitialized) server.stop(); super.onDestroy() }
}
