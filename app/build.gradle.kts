plugins {
    id("com.android.application")
}

// CI passes -PappVersionName / -PappVersionCode derived from the pushed tag.
val appVersionName: String = (project.findProperty("appVersionName") as String?) ?: "0.1.0"
val appVersionCode: Int = (project.findProperty("appVersionCode") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "com.micro.gameportal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.micro.gameportal"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
