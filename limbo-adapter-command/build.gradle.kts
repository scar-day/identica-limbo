plugins {
    `java-library`
}

dependencies {
    api(project(":common"))
    compileOnly(libs.identica.api)
    compileOnly(libs.keystone)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.annotations)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
