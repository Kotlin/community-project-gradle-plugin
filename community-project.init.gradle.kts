import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.gradle.api.artifacts.Configuration
import org.gradle.plugin.management.PluginRequest
import org.gradle.plugin.management.internal.DefaultPluginRequest

settingsEvaluated {
    val kotlinVersion = extra["community.project.kotlin.version"].toString()
    val kotlinRepo = extra["community.project.kotlin.repo"].toString()
    val pluginPath = if (extra.has("community.project.plugin.build.path")) {
        extra["community.project.plugin.build.path"].toString()
    } else {
        gradle.startParameter.initScripts
            .single { it.name == "community-project.init.gradle.kts" }.parentFile.absolutePath
    }

    fun rewriteOriginalRequestVersion(request: PluginRequest, version: String) {
        val defaultPluginRequest = request as DefaultPluginRequest
        val originalRequest = defaultPluginRequest.originalRequest as DefaultPluginRequest

        val versionField = DefaultPluginRequest::class.java.getDeclaredField("version")
        versionField.isAccessible = true

        versionField.set(originalRequest, version)
    }

    pluginManagement {
        repositories {
            setupRepositories(kotlinRepo)
        }

        resolutionStrategy {
            eachPlugin {
                if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                    rewriteOriginalRequestVersion(requested, kotlinVersion)
                    useVersion(kotlinVersion)
                }
            }
        }
    }

    includeBuild(pluginPath)
}

allprojects {
    val kotlinVersion = extra["community.project.kotlin.version"].toString()
    val kotlinRepo = extra["community.project.kotlin.repo"].toString()

    if (rootProject.name != "buildSrc" && rootProject.name != "community-project-plugin") {
        buildscript {
            repositories {
                setupRepositories(kotlinRepo)
            }

            configurations.all {
                useKotlinVersionResolutionStrategy(kotlinVersion)
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
        dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        configurations["implementation"].apply {
            useKotlinVersionResolutionStrategy(kotlinVersion)
        }
    }

    if (rootProject.name != "buildSrc" && rootProject.name != "community-project-plugin") {
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

fun RepositoryHandler.setupRepositories(kotlinRepo: String) {
    maven(kotlinRepo)
    mavenCentral()
    gradlePluginPortal()
}