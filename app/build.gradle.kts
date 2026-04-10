plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

import java.util.Properties
import org.gradle.api.GradleException

android {
    namespace = "com.cocido.nonna"
    compileSdk = 35
    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    val hasKeystoreProps = keystorePropsFile.exists().also { exists ->
        if (exists) keystorePropsFile.inputStream().use { keystoreProps.load(it) }
    }
    
    packaging {
        jniLibs {
            // false = .so sin comprimir, permite alineación 16 KB requerida por Google Play (nov 2025+)
            useLegacyPackaging = false
        }
    }

    defaultConfig {
        applicationId = "com.cocido.nonna.free"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "0.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasKeystoreProps) {
                val storeFilePath = keystoreProps.getProperty("storeFile")
                if (!storeFilePath.isNullOrBlank()) {
                    storeFile = file(storeFilePath)
                }
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    val isReleaseTaskRequested = gradle.startParameter.taskNames.any { task ->
        val normalized = task.lowercase()
        normalized.contains("bundlerelease") ||
            normalized.contains("assemblerelease") ||
            normalized.contains("publishrelease")
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        create("qa") {
            initWith(getByName("release"))
            // Variante release-like para validar R8/obfuscación sin keystore de producción.
            isDebuggable = true
            applicationIdSuffix = ".qa"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        release {
            if (!hasKeystoreProps && isReleaseTaskRequested) {
                throw GradleException("Falta keystore.properties para firmar la release.")
            }
            // MVP release: desactivamos minify/shrink para evitar errores de reflexión
            // (ej: Retrofit/Kotlin generic signatures) que no aparecen en debug.
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
        viewBinding = false
        dataBinding = false
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    
    // Material Design
    implementation(libs.material)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.compose.hilt.navigation)
    implementation(libs.coil.compose)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.6")
    debugImplementation(libs.compose.ui.tooling)
    
    // Layout
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.motionlayout)
    
    // Navigation (keep for existing fragments)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // WorkManager
    implementation(libs.androidx.work.runtime)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Media
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.ui)
    
    // Security
    implementation(libs.androidx.security.crypto)
    
    // Image Loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}