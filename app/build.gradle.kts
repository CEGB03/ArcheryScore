plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cegb03.archeryscore"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cegb03.archeryscore"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 🔐 Credenciales de Supabase desde local.properties (GITIGNORED)
        val supabaseUrl = project.findProperty("SUPABASE_URL") as String? ?: "https://CONFIGURAR_EN_LOCAL_PROPERTIES"
        val supabaseAnonKey = project.findProperty("SUPABASE_ANON_KEY") as String? ?: "CONFIGURAR_EN_LOCAL_PROPERTIES"
        
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Hilt Dependency Injection
    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.androidx.compose.foundation.layout)
    kapt(libs.com.google.dagger.hilt.compiler)
    
    // Retrofit & OkHttp
    implementation(libs.com.squareup.retrofit2)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.okhttp3)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    
    // jsoup for HTML parsing
    implementation(libs.org.jsoup)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Coil for Image Loading
    implementation(libs.io.coil.kt.coil.compose)
    
    // Biometric
    implementation(libs.androidx.biometric)

    // PDF search (native)
    implementation(libs.com.tom.roush.pdfbox.android)

    // Room (local storage)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Location (weather)
    implementation(libs.com.google.android.gms.play.services.location)
    
    // Supabase 2.3.1 - Modules (comentado temporalmente - investigar integración)
    // implementation(libs.io.github.jan.tennert.supabase.postgrest)
    // implementation(libs.io.github.jan.tennert.supabase.realtime)
    // implementation(libs.io.github.jan.tennert.supabase.gotrue)
    
    // Ktor (requerido por Supabase)
    implementation(libs.io.ktor.client.android)
    implementation(libs.io.ktor.client.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    
    // Kotlinx Serialization
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    
    // Firebase
    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.analytics)
    implementation(libs.com.google.firebase.auth)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    
    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}