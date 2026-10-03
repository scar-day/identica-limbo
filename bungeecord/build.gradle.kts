import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
}

dependencies {
    implementation(project(":common"))
    implementation(project(":limbo-adapter-command"))
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.identica.api)
    implementation(libs.configura)
    implementation(libs.jackson.databind)
    implementation(libs.adventure.api)
    implementation(libs.adventure.minimessage)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.bungeecord.api)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("bungee.yml") {
        expand(props)
    }
}

val internalModules = listOf(project(":api"), project(":common"), project(":limbo-adapter-command"))

tasks.named<Jar>("jar") {
    archiveFileName.set("IdenticaLimbo-BUNGEECORD-${project.version}.jar")
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
