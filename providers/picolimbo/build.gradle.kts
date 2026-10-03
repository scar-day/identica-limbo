plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    api(project(":common"))
    implementation(libs.jackson.databind)
    compileOnly(libs.configura)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
