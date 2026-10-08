plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android { namespace = "com.horizonteselvagem3d.game"; compileSdk = 35
    defaultConfig { applicationId = "com.horizonteselvagem3d.game"; minSdk = 26; targetSdk = 35; versionCode = 6; versionName = "0.6.0" }
}

kotlin { jvmToolchain(17) }


dependencies {
    // Renderer PBR moderno e carregamento de modelos glTF/GLB.
        }
