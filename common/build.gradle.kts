@file:Suppress("OPT_IN_USAGE")

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-18"
    id("org.jetbrains.compose") version "1.5.1"
    id("com.android.library")
}

group = "me.ashishekka"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    js(IR) {
        browser {
            testTask {
                testLogging.showStandardStreams = true
                useKarma {
                    useChromeHeadless()
                    useFirefox()
                }
            }
        }
        binaries.executable()
    }

    cocoapods {
        summary = "Core implementation of the Chip8 emulator and additional common APIs"
        homepage = "Link to the Common Module homepage"
        version = "1.0"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "common"
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsApi::class")
        }
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("com.russhwolf:multiplatform-settings:1.1.0")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.0")
                implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.5.1")
                api("androidx.core:core-ktx:1.9.0")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("com.russhwolf:multiplatform-settings-datastore:1.1.0")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "me.ashishekka.k8"
}

// Workaround for https://slack-chats.kotlinlang.org/t/13166318/i-m-playing-around-with-compose-multiplatform-using-the-temp
task("testClasses").doLast {
    println("This is a dummy testClasses task")
}