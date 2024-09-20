pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("micrometer-core", "io.micrometer:micrometer-core:1.13.0")
        }
    }
}

rootProject.name = "chesslounge"
include("engine")
include("chesshouse")
include("client")