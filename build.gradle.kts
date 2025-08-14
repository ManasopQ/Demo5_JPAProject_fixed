plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0"
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("be.tarsos:dsp:2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
