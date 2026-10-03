plugins {
    `java-library`
}

dependencies {
    implementation(libs.configura)
    implementation(libs.jackson.databind)
    compileOnly(libs.identica.api)
    compileOnly(libs.adventure.api)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    api(project(":api"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
