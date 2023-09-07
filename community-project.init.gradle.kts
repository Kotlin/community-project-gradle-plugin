import org.gradle.api.artifacts.Configuration

settingsEvaluated {
    val kotlinVersion = extra.getStringOrNull("community.project.kotlin.version")
    val kotlinRepo = extra.getStringOrNull("community.project.kotlin.repo")

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

    val disableVerificationTasks =
        extra.getStringOrNull("community.project.disable.verification.tasks")?.toBoolean() ?: false

    if (disableVerificationTasks) {
        logger.info("Verification tasks are disabled because `community.project.disable.verification.tasks` is true")
        gradle.taskGraph.whenReady {
            allTasks.forEach {
                if (it is VerificationTask) {
                    logger.info("Task ${it.path} is disabled because `community.project.disable.verification.tasks` is true")
                    it.enabled = false
                }
            }
        }
    }
}

allprojects {
    val kotlinVersion = extra.getStringOrNull("community.project.kotlin.version")
    val kotlinRepo = extra.getStringOrNull("community.project.kotlin.repo")

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

fun ExtraPropertiesExtension.getStringOrNull(propertyName: String): String? {
    return if (has(propertyName)) {
        get(propertyName) as String
    } else {
        null
    }
}