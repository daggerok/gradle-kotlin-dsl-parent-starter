plugins {
    idea
    id("com.palantir.docker-run")
}

dockerRun {
    name = "consul"
    image = "consul:1.9.5"
    clean = project.findProperty("clean") ?: "true" == "true"
    daemonize = project.findProperty("demonize") ?: "false" == "true"
    env(
        mapOf(
            "CONSUL_BIND_INTERFACE" to "eth0"
        )
    )
    ports(
        "8500:8500",
        "8600:8600"
    )
    command(
        "agent", "-server", "-ui", "-node=server1", "-bootstrap-expect=1", "-client=0.0.0.0"
    )
}

val up by tasks.registering {
    dependsOn(tasks.named("dockerRun"))
}

val down by tasks.registering {
    dependsOn("dockerStop")
    finalizedBy("dockerRemoveContainer")
}
