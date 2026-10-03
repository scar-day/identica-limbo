import org.gradle.jvm.tasks.Jar


plugins {
    `java-library`
    id("eclipse")
    alias(libs.plugins.run.velocity)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":limbo-adapter-command"))
    implementation(project(":providers:picolimbo"))
    compileOnly(libs.velocity.api)
    compileOnly(libs.identica.api)
    implementation(libs.configura)
    implementation(libs.jackson.databind)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.velocity.api)
}

tasks {
    runVelocity {
        velocityVersion(libs.versions.velocity.api.get())
    }
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    inputs.property("version", project.version)
    from(templateSource)
    into(templateDest)
    expand("version" to project.version)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

val internalModules = listOf(
    project(":api"),
    project(":common"),
    project(":limbo-adapter-command"),
    project(":providers:picolimbo")
)


tasks.named<Jar>("jar") {
    archiveFileName.set("IdenticaLimbo-VELOCITY-${project.version}.jar")
    dependsOn(internalModules.map { it.tasks.named("jar") })
    from(configurations.runtimeClasspath.map { runtimeClasspath ->
        runtimeClasspath.filter { file -> file.name.endsWith(".jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val rootBuildDir = rootProject.layout.buildDirectory

val copyPluginToBuild = tasks.register<Copy>("copyPluginToBuild") {
    from(tasks.named<Jar>("jar"))
    into(rootBuildDir)
}

tasks.named("build") {
    dependsOn(copyPluginToBuild)
}
