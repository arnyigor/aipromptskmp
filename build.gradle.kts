import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    jvm("desktop"){
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose (версии от плагина)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(libs.compose.material.icons.extended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Kotlinx
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlin.coroutines.core) // ТОЛЬКО -core
                implementation(libs.kotlin.serialization.json)

                // Decompose, Ktor, Room, Koin-core и т.д.
                implementation("com.arkivanov.decompose:decompose:${libs.versions.decompose.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }
                implementation("com.arkivanov.decompose:extensions-compose:${libs.versions.decompose.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }
                implementation("com.arkivanov.essenty:lifecycle-coroutines:${libs.versions.lifecycle.coroutines.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation("androidx.room:room-runtime:${libs.versions.room.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }
                implementation("androidx.room:room-ktx:${libs.versions.room.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }

                implementation(libs.sqlite.bundled)
                implementation(libs.kermit)
                implementation(libs.okio)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.koin.core)
                // Объявляем зависимость явно, чтобы можно было применить exclude
                implementation("io.insert-koin:koin-compose:${libs.versions.koin.get()}") {
                    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
                }
            }
        }

        val androidMain by getting {
            // Зависимости только для Android
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.security.crypto)

                // Платформенные реализации
                implementation(libs.kotlinx.coroutines.android) // ПРАВИЛЬНОЕ МЕСТО
                implementation(libs.ktor.client.okhttp)
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose) // Для viewModel()
            }
        }

        val desktopMain by getting {
            // Зависимости только для Desktop, включая Ktor и Ollama
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.keytar.java)
                implementation(libs.nirmato.ollama.client)

                // Платформенные реализации
                implementation(libs.kotlinx.coroutines.swing) // ПРАВИЛЬНОЕ МЕСТО
                implementation(libs.ktor.client.cio)
            }
        }
    }

}

// Загружаем свойства из local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.arny.aipromptskmp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arny.aipromptskmp.android"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "AiPrompsKMP"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    // Указываем, что room-compiler - это KSP процессор для каждой цели
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}


