import java.util.Properties

plugins {
    id("com.android.application")
}

// CI passes -PappVersionName / -PappVersionCode derived from the pushed tag.
val appVersionName: String = (project.findProperty("appVersionName") as String?) ?: "0.1.0"
val appVersionCode: Int = (project.findProperty("appVersionCode") as String?)?.toIntOrNull() ?: 1

// Release signing from keystore/keystore.properties (gitignored); falls back to unsigned.
val signingPropsFile = rootProject.file("keystore/keystore.properties")
val signingProps = if (signingPropsFile.exists()) Properties().apply {
    signingPropsFile.inputStream().use { load(it) }
} else null

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

    signingConfigs {
        if (signingProps != null) {
            create("release") {
                storeFile = rootProject.file("keystore/${signingProps.getProperty("storeFile")}")
                storeType = signingProps.getProperty("storeType")
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingProps != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
