import org.gradle.api.artifacts.Configuration

var ignoreDependencyNames: List<String> = listOf()
var gradleRepositoriesMode: String = "project"

fun Settings.initEnvironment() {
    ignoreDependencyNames =
        extra.getStringOrNull("community.project.ignore.dependencies.names")?.split(",")?.map { it.trim() } ?: listOf()
    gradleRepositoriesMode = extra.getStringOrNull("community.project.gradle.repositories.mode")?.also { value ->
        val allowedValues = listOf("project", "settings")
        if (allowedValues.none { it == value })
            throw IllegalArgumentException(
                "The 'community.project.gradle.repositories.mode' parameter can be set " +
                        "to one of the following values: $allowedValues"
            )
    } ?: "project"
}

settingsEvaluated {
    initEnvironment()
    val kotlinVersion = extra.getStringOrNull("community.project.kotlin.version")
    val kotlinRepo = extra.getStringOrNull("community.project.kotlin.repo")

    val pluginPath = if (extra.has("community.project.plugin.build.path")) {
        extra["community.project.plugin.build.path"].toString()
    } else {
        gradle.startParameter.initScripts
            .single { it.name == "community-project.init.gradle.kts" }.parentFile.absolutePath
    }

    if (gradleRepositoriesMode == "settings") {
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
            repositories {
                setupRepositories(kotlinRepo)
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
    }

    pluginManagement {
        repositories {
            setupRepositories(kotlinRepo)
            google()
            mavenCentral()
            gradlePluginPortal()
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
                dependencies.add("classpath", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            }

            dependencies.add("classpath", "org.jetbrains.kotlin:community-project-plugin")
        }
    }

    if (gradleRepositoriesMode == "project") {
        repositories {
            setupRepositories(kotlinRepo)
        }
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
        if (requested.group == "org.jetbrains.kotlin" && !isExcludedDependency()) {
            useVersion(version)
        }
    }
}

fun DependencyResolveDetails.isExcludedDependency(): Boolean {
    return ignoreDependencyNames.any { this.requested.name == it }
}

fun RepositoryHandler.setupRepositories(kotlinRepo: String?) {
    if (kotlinRepo != null) {
        maven(kotlinRepo)
    }
}

fun ExtraPropertiesExtension.getStringOrNull(propertyName: String): String? {
    return if (has(propertyName)) {
        get(propertyName) as String
    } else {
        null
    }
}