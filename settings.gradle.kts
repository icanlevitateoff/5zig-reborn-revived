pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    // Lets Gradle download the JDKs it needs (Java 25 for the daemon/Loom, per-version toolchains).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // One jar per entry; versions/<v>/gradle.properties has its deps and the supported Minecraft range.
        versions(
            "1.20.1", "1.20.4",
            "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10",
            "26.3",
        )
        vcsVersion = "1.21.8"
    }
}

rootProject.name = "the-5zig-mod"
