plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jme3)
    implementation(libs.zay.es)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.ourcraft.OurcraftGame"
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
