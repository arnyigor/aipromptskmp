import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    // Добавляем плагин для сериализации, он необходим для работы с JSON
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

group = "com.arny.aipromptskmp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// Определяем версию Ktor в одном месте
val ktorVersion = "3.1.3"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    // Клиентская библиотека для Ollama. Ваша версия 0.2.0 - отличный выбор.
    implementation("org.nirmato.ollama:nirmato-ollama-client-ktor:0.2.0")


    // --- Правильные и согласованные зависимости Ktor ---
    // 1. Используем Bill of Materials (BOM) для Ktor 3.1.3
    implementation(platform("io.ktor:ktor-bom:$ktorVersion"))

    // 2. Добавляем нужные модули Ktor БЕЗ УКАЗАНИЯ ВЕРСИИ
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe)
            packageName = "AiPrompsKMP"
            packageVersion = "1.0.0"
        }
    }
}
