plugins {
    id("org.jetbrains.compose") version "1.5.1"
    id("com.android.application")
    kotlin("android")
}

group = "me.ashishekka"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.russhwolf:multiplatform-settings:1.1.0")
    implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.0")
    implementation("androidx.startup:startup-runtime:1.1.1")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "me.ashishekka.k8"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}