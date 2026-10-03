plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "dev.scarday"
            artifactId = "identica-limbo-api"
            version = project.version.toString()
        }
        create<MavenPublication>("mavenIdenticaLimbo") {
            from(components["java"])
            groupId = "dev.scarday.identicalimbo"
            artifactId = "api"
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "WctxRegistry"
            url = uri("https://registry.wctx.lol/maven/public-maven")
            credentials {
                username = providers.gradleProperty("registryUsername").orNull
                    ?: System.getenv("REGISTRY_USERNAME")
                    ?: "ScarDay"
                password = providers.gradleProperty("registryPassword").orNull
                    ?: System.getenv("REGISTRY_PASSWORD")
                    ?: "sPR6h7fcr5mU8MYwMUQ4"
            }
        }
    }
}
