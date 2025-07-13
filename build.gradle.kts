import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    jvm("desktop"){
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                // Весь общий UI и логика Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Мультиплатформенные библиотеки
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.serialization.json)

                // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ ---
                // 1. Применяем BOM здесь, чтобы он действовал на все Ktor-зависимости.
                implementation(project.dependencies.platform(libs.ktor.bom))

                // 2. Теперь эти KMP-совместимые зависимости Ktor получат версию из BOM.
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

        val androidMain by getting {
            // Зависимости только для Android
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                // Добавляем только специфичный для Android движок Ktor.
                // Версию он получит из BOM, примененной в commonMain.
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
            }
        }
    }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
