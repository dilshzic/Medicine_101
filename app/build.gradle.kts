import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.algorithmx.medicine101"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.algorithmx.medicine101"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- AI API Keys from local.properties ---
        val geminiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val groqKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
        val deepseekKey = localProperties.getProperty("DEEPSEEK_API_KEY") ?: ""

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // --- Core Android & Kotlin ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.json)

    // --- UI & Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- Dependency Injection (Hilt) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Database & Persistence ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // --- Networking ---
    implementation(libs.okhttp)
    implementation("com.google.code.gson:gson:2.10.1") // Kept for legacy if needed

    // --- AI & Machine Learning (The Brain) ---
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation(libs.digital.ink.recognition)

    // --- Firebase Ecosystem ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.play.services.auth)

    // --- Media & PDF ---
    implementation(libs.core) // YouTube Player Core
    implementation(libs.androidx.pdf.viewer)
    implementation(libs.androidx.pdf.compose)
    implementation(libs.androidx.pdf.viewer.fragment)
    implementation("androidx.pdf:pdf-document-service:1.0.0-alpha13")
    implementation(libs.pdfbox.android)
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation("androidx.pdf:pdf-ink:1.0.0-alpha13")
    implementation(libs.android.pdf.viewer)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
