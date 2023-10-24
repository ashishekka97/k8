import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.5.1"
}

group = "me.ashishekka"
version = "0.0.1"

kotlin {
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
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(project(":common"))
            }
        }
    }
}

// Workaround for https://slack-chats.kotlinlang.org/t/13166318/i-m-playing-around-with-compose-multiplatform-using-the-temp
task("testClasses").doLast {
    println("This is a dummy testClasses task")
}