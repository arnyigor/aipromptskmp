import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
                // Весь общий UI и логика Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(libs.compose.material.icons)
                implementation(libs.kotlinx.datetime)

                // --- DECOMPOSE ---
                implementation(libs.decompose)
                implementation(libs.decompose.extensions.compose)

                // --- КОРОУТИНЫ И СЕРИАЛИЗАЦИЯ ---
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.serialization.json) // kotlinx.serialization.core подтянется транзитивно

                // --- KTOR (Общие модули) ---
                // BOM для Ktor, чтобы управлять версиями движков
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.kermit)

                // --- KOIN ---
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.ktx)
                implementation(libs.sqlite.bundled)
                implementation(libs.okio)

                implementation(libs.multiplatform.settings.no.arg)
            }
        }

        val androidMain by getting {
            // Зависимости только для Android
            dependencies {
                // Зависимости только для Android
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.security.crypto)

                // Движок Ktor для Android
                implementation(libs.ktor.client.okhttp)
            }
        }

        val desktopMain by getting {
            // Зависимости только для Desktop, включая Ktor и Ollama
            dependencies {
                implementation(compose.desktop.currentOs)

                // Ktor и Ollama теперь живут здесь, так как используются только на Desktop
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.cio) // Движок для Desktop
                implementation(libs.nirmato.ollama.client)

                implementation(libs.kotlinx.coroutines.swing) // Предоставляет Dispatchers.Main для Desktop (AWT/Swing)

                // Добавляем библиотеку для работы с системными хранилищами
                implementation(libs.keytar.java)
            }
        }
    }

}

android {
    namespace = "com.arny.aipromptskmp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.arny.aipromptskmp.android"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
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


