import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// CI passes -PappVersionName / -PappVersionCode derived from the pushed tag.
// Local defaults track the current release so debug installs don't downgrade
// against a CI-built release APK (versionCode 2002 = v0.2.2).
val appVersionName: String = (project.findProperty("appVersionName") as String?) ?: "0.2.2"
val appVersionCode: Int = (project.findProperty("appVersionCode") as String?)?.toIntOrNull() ?: 2002

// Release signing from keystore/keystore.properties (gitignored); falls back to unsigned.
val signingPropsFile = rootProject.file("keystore/keystore.properties")
val signingProps = if (signingPropsFile.exists()) Properties().apply {
    signingPropsFile.inputStream().use { load(it) }
} else null

android {
    namespace = "com.micro.gameportal"
    buildFeatures {
        compose = true
    }
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
            isMinifyEnabled = true
            isShrinkResources = true
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    testImplementation("junit:junit:4.13.2")
}
