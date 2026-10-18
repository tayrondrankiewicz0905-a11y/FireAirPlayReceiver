plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "de.tayron.fireairplayreceiver"
    compileSdk = 35
    defaultConfig { applicationId = "de.tayron.fireairplayreceiver"; minSdk = 23; targetSdk = 28; versionCode = 1; versionName = "1.0.0" }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
