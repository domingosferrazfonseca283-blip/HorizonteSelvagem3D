plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android { namespace = "com.horizonteselvagem3d.game"; compileSdk = 35
    defaultConfig { applicationId = "com.horizonteselvagem3d.game"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0" }
}

kotlin { jvmToolchain(17) }
