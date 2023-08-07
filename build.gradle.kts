plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "org.jetbrains.kotlin"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    gradleApi()
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

gradlePlugin {
    plugins {
        create("community-project") {
            id = "org.jetbrains.kotlin.community-project"
            implementationClass = "org.jetbrains.kotlin.CommunityProjectPlugin"
        }
    }
}