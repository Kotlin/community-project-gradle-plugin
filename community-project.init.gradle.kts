import org.gradle.api.artifacts.Configuration

settingsEvaluated {
    val kotlinVersion = extra.getOrNull("community.project.kotlin.version")?.toString()
    val kotlinRepo = extra.getOrNull("community.project.kotlin.repo")?.toString()

    val pluginPath = if (extra.has("community.project.plugin.build.path")) {
        extra["community.project.plugin.build.path"].toString()
    } else {
        gradle.startParameter.initScripts
            .single { it.name == "community-project.init.gradle.kts" }.parentFile.absolutePath
    }

    pluginManagement {
        repositories {
            setupRepositories(kotlinRepo)
        }

        if (kotlinVersion != null) {
            resolutionStrategy {
                eachPlugin {
                    if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                        useVersion(kotlinVersion)
                    }
                }
            }
        }
    }

    includeBuild(pluginPath)
}

allprojects {
    val kotlinVersion = extra.getOrNull("community.project.kotlin.version")?.toString()
    val kotlinRepo = extra.getOrNull("community.project.kotlin.repo")?.toString()

    if (rootProject.name != "buildSrc" && rootProject.name != "community-project-plugin") {
        buildscript {
            repositories {
                setupRepositories(kotlinRepo)
            }

            if (kotlinVersion != null) {
                configurations.all {
                    useKotlinVersionResolutionStrategy(kotlinVersion)
                }
            }

            dependencies.add("classpath", "org.jetbrains.kotlin:community-project-plugin")
            dependencies.add("classpath", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        }
    }

    repositories {
        setupRepositories(kotlinRepo)
    }

    if (name == "buildSrc") {
        dependencies.add("implementation", "org.jetbrains.kotlin:community-project-plugin")
        if (kotlinVersion != null) {
            dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        }

        if (kotlinVersion != null) {
            configurations["implementation"].apply {
                useKotlinVersionResolutionStrategy(kotlinVersion)
            }
        }
    }

    if (kotlinVersion != null && rootProject.name != "buildSrc" && rootProject.name != "community-project-plugin") {
        configurations.all {
            useKotlinVersionResolutionStrategy(kotlinVersion)
        }
    }
}

afterProject {
    if (rootProject.name != "buildSrc" && rootProject.name != "community-project-plugin") {
        apply(plugin = "org.jetbrains.kotlin.community-project")
    }
}

fun Configuration.useKotlinVersionResolutionStrategy(version: String) = resolutionStrategy {
    eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(version)
        }
    }
}

fun RepositoryHandler.setupRepositories(kotlinRepo: String?) {
    if (kotlinRepo != null) {
        maven(kotlinRepo)
    }

    mavenCentral()
    gradlePluginPortal()
}

fun ExtraPropertiesExtension.getOrNull(propertyName: String): Any? {
    return if (has(propertyName)) {
        get(propertyName)
    } else {
        null
    }
}