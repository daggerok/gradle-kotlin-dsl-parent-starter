pluginManagement {
    buildscript {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            maven(uri(extra["springMilestoneRepositoryUrl"] as String))
        }
    }

    val kotlinVersion: String by extra
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.ajoberstar.grgit") version "4.1.0"
        id("com.palantir.docker-run") version "0.26.0"
        id("org.springframework.boot") version extra["springBootVersion"].toString()
        id("org.ajoberstar.reckon") version extra["reckonGradlePluginVersion"] as String
        id("com.github.ben-manes.versions") version extra["versionsGradlePluginVersion"] as String
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(uri(extra["springMilestoneRepositoryUrl"] as String))
    }
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(uri(extra["springMilestoneRepositoryUrl"] as String))
    }
}

rootProject.name = extra["rootProjectName"] as String

include(
    ":docker:consul",
    ":docker:gradle-build"
)
