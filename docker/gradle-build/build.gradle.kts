plugins {
    idea
    id("com.palantir.docker-run")
}

fun bool(propertyName: String, defaultValue: String = "false") =
    project.findProperty(propertyName) ?: defaultValue == "true"

dockerRun {
    name = "gradle-builder"
    image = "gradle:7.0.2-jdk11-hotspot"
    daemonize = bool("demonize")
    clean = bool("clean", "true")
    volumes(mapOf(
        "$rootDir" to "$rootDir"
    ))
    env(
        mapOf("HELLO" to "WORLD")
            .plus(
                System
                    .getenv()
                    .filterNot {
                        it.key == "PATH"
                                || it.key.toLowerCase().contains("java")
                                || it.key.toLowerCase().contains("gradle")
                                || it.value.startsWith(System.getenv("HOME"))
                    }
            )
    )
    arguments( // docker run arguments
        "--workdir=$rootDir" // same dir as defined under volumes
    )
    // the command cannot have spaces, so split on space, and convert to an array of strings
    command("bash", "-c", "env && gradle clean build")
}

val `docker-build` by tasks.registering {
    dependsOn(tasks.named("dockerRun"))
    finalizedBy(
        tasks.named("dockerStop"),
        tasks.named("dockerRemoveContainer")
    )
}

defaultTasks(`docker-build`.name)
