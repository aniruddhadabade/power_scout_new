plugins {
    alias(libs.plugins.android.application) // Use alias for AGP
    id("com.google.gms.google-services") // Firebase plugin for Google services
}

android {
    namespace = "com.example.powerscout"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.powerscout"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX and UI libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Networking and data visualization
    implementation(libs.volley) // Use version catalog for Volley
    implementation(libs.mpandroidchart) // Use version catalog for MPAndroidChart

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase dependencies using BOM
    implementation(platform(libs.firebase.bom)) // Use version catalog reference for Firebase BOM
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth)
}
