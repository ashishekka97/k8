plugins {
    id("org.jetbrains.compose") version "1.2.0"
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
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.compose.material:material:1.2.1")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "me.ashishekka.k8"
        minSdk = 24
        targetSdk = 33
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