plugins {
    `java-library`
}

dependencies {
    implementation(project(":common"))
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    archiveFileName.set("IdenticaLimbo-BUNGEECORD-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val rootBuildDir = rootProject.layout.buildDirectory

val copyPluginToBuild = tasks.register<Copy>("copyPluginToBuild") {
    from(tasks.named<org.gradle.jvm.tasks.Jar>("jar"))
    into(rootBuildDir)
}

tasks.named("build") {
    dependsOn(copyPluginToBuild)
}
