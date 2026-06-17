plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jme3)
    implementation(libs.zay.es)
    implementation(libs.lemur)
    implementation(libs.guava)

    // Cross-platform LWJGL natives so the distribution runs on any OS, not just the build host.
    // jME's lwjgl3 backend resolves only the build host's natives via Gradle module metadata; we
    // add every platform's classifier artifacts explicitly so `distZip` is a portable bundle
    // (e.g. zip built on WSL/Linux still runs on native Windows — where real FPS mouse-look works).
    val lwjglVersion = "3.3.6"
    val lwjglNatives = listOf("natives-windows", "natives-linux", "natives-macos", "natives-macos-arm64")
    val lwjglNativeModules = listOf("lwjgl", "lwjgl-glfw", "lwjgl-jemalloc", "lwjgl-openal", "lwjgl-opengl")
    for (module in lwjglNativeModules) {
        for (classifier in lwjglNatives) {
            runtimeOnly("org.lwjgl:$module:$lwjglVersion:$classifier")
        }
    }

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.ourcraft.OurcraftGame"
    applicationName = "ourcraft"
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
