package org.jetbrains.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import javax.inject.Inject


class CommunityProjectPlugin @Inject constructor (
    private val providerFactory: ProviderFactory
) : Plugin<Project> {

    private val kotlinApiVersion: Provider<String> =
        providerFactory.gradleProperty(KOTLIN_API_VERSION_PROPERTY)

    private val kotlinLanguageVersion: Provider<String> =
        providerFactory.gradleProperty(KOTLIN_LANGUAGE_VERSION_PROPERTY)

    override fun apply(project: Project) {
        project.tasks.withType<KotlinCompilationTask<*>> {
            if (kotlinApiVersion.isPresent) {
                compilerOptions.apiVersion.set(kotlinApiVersion.map { KotlinVersion.fromVersion(it) })
            }

            if (kotlinLanguageVersion.isPresent) {
                compilerOptions.languageVersion.set(kotlinLanguageVersion.map { KotlinVersion.fromVersion(it) })
            }

            compilerOptions.freeCompilerArgs.add("-version")

            doFirst {
                val compilerOptions = (this as KotlinCompilationTask<*>).compilerOptions
                logger.info(
                    "compilerOptions" +
                            " languageVersion: ${compilerOptions.languageVersion.orNull?.version}" +
                            " apiVersion: ${compilerOptions.apiVersion.orNull?.version}"
                )
            }
        }
    }

    companion object {
        private const val KOTLIN_LANGUAGE_VERSION_PROPERTY = "community.project.kotlin.languageVersion"
        private const val KOTLIN_API_VERSION_PROPERTY = "community.project.kotlin.apiVersion"
    }
}