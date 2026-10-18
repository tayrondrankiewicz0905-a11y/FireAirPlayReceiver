# Fire AirPlay Receiver — Eigenentwicklung

Android-TV-Projekt (Kotlin/Gradle) mit TV-Oberfläche, experimenteller DNS-SD-Ankündigung für `_airplay._tcp` und `_raop._tcp`, minimalem RTSP-Grundserver und GitHub-Actions-Build.

## Ehrlicher Funktionsstand
Dies ist ein **kompilierbares Entwicklungsgerüst, keine fertige AirPlay-Mirroring-App**. Apple-AirPlay erfordert weitere Protokollschritte (Pairing/Authentifizierung, vollständige RTSP-Sitzung, RTP-Streams und Audio-/Videodecodierung). Diese Teile sind noch nicht implementiert. Deshalb kann ich nicht behaupten, dass iPhone-Bild und Ton bereits übertragen werden.

## APK über GitHub Actions bauen
1. Neues GitHub-Repository erstellen, z. B. `FireAirPlayReceiver`.
2. Dateien aus diesem ZIP hochladen und committen.
3. Tab **Actions** öffnen und **Build Fire TV APK** ausführen.
4. Im fertigen Workflow das Artefakt `FireAirPlayReceiver-debug-apk` herunterladen und entpacken. Darin liegt `app-debug.apk`.

## Installation
Auf Fire TV die Installation aus unbekannten Apps für das verwendete Übertragungsprogramm erlauben und die APK installieren. Fire TV und iPhone müssen im selben WLAN sein; Client-Isolation im Router muss aus sein.

## Nächste Schritte für echte Übertragung
- AirPlay-kompatible Service-Ankündigung und Session-State-Machine
- RTSP SETUP/RECORD/TEARDOWN und Session-Verhandlung
- UDP/RTP-Paketempfang
- unterstützte Video-/Audioformate decodieren und auf dem TV ausgeben
- Kompatibilitätstests mit mehreren iOS-Versionen

Kein offizielles Apple-Produkt und keine Apple-Zugehörigkeit.
