plugins {
    base
    idea
    kotlin("jvm")
    kotlin("plugin.spring")
    //id("org.ajoberstar.grgit")
    id("org.ajoberstar.reckon")
    id("com.github.ben-manes.versions")
}

val javaVersion = JavaVersion.VERSION_1_8

idea {
    project {
        languageLevel = org.gradle.plugins.ide.idea.model.IdeaLanguageLevel(javaVersion)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        //sourceDirs.add(file("src/main/proto"))
        sourceDirs.add(file("src/generated/main/java"))
        generatedSourceDirs.add(file("src/generated/main/java"))
    }
}

allprojects {
    apply<JavaPlugin>()

    java.sourceCompatibility = javaVersion

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    repositories {
        mavenCentral()
        maven(uri(extra["springMilestoneRepositoryUrl"] as String))
    }

    dependencies {
        val kotlinVersion: String by project
        implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
        implementation(kotlin("reflect"))
        implementation(kotlin("stdlib-jdk8"))

        val springBootVersion: String by project
        implementation(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

        val springCloudVersion: String by project
        implementation(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))
        implementation("ch.qos.logback:logback-classic:${property("logbackVersion")}")
        implementation("org.apache.logging.log4j:log4j-core")
        implementation("org.apache.logging.log4j:log4j-api-kotlin:${property("log4jKotlinVersion")}")

        implementation(enforcedPlatform("org.projectlombok:lombok:${property("lombokVersion")}"))
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        implementation("io.github.hakky54:logcaptor:${property("logcaptorVersion")}")

        implementation(enforcedPlatform("org.awaitility:awaitility:${property("awaitilityVersion")}"))
        implementation("org.awaitility:awaitility")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "$javaVersion"
            }
        }
        withType<Test> {
            useJUnitPlatform()
            testLogging {
                showCauses = true
                showExceptions = true
                showStackTraces = true
                showStandardStreams = true
            }
        }
    }

    defaultTasks("clean", "build")
}

reckon {
    scopeFromProp()
    snapshotFromProp()
    //stageFromProp("release")
}

tasks {
    clean {
        doLast {
            File(rootDir.absolutePath)
                .walkTopDown()
                .filter { it.isDirectory }
                .filter { it.endsWith("out") }
                .filter { it.resolveSibling("./src").normalize().exists() }
                .onEach { println("Removing $it") }
                .forEach { it.deleteRecursively() }
        }
    }

    register("version") {
        println(project.version.toString())
    }

    //register("status") {
    //    val grgit = org.ajoberstar.grgit.Grgit.open(mapOf("currentDir" to project.rootDir))
    //    doLast {
    //        val status = grgit.status()
    //        status?.let { s ->
    //            println("workspace is clean: ${s.isClean}")
    //            if (!s.isClean) {
    //                if (s.unstaged.allChanges.isNotEmpty()) {
    //                    println("""all unstaged changes: ${s.unstaged.allChanges.joinToString(separator = "") { i -> "\n - $i" }}""")
    //                }
    //            }
    //        }
    //    }
    //}

    named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
        resolutionStrategy {
            componentSelection {
                all {
                    val rejected = listOf("alpha", "m", "SNAPSHOT")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                        .any { it.matches(candidate.version) }
                    if (rejected) reject("Release candidate")
                }
            }
        }
        outputFormatter = "plain" // "json"
    }

    named<Wrapper>("wrapper") {
        gradleVersion = "${property("gradleVersion")}"
        distributionType = Wrapper.DistributionType.BIN
    }
}

defaultTasks("clean", "test")
