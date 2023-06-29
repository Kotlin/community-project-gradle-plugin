# Kotlin Community Project Gradle Plugin

[![JetBrains team project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Plugin and a Gradle init script for overriding all Kotlin dependencies in a project to a given Kotlin version.

Sample usage:
```bash
./gradlew build --info \
          --init-script ../community-project-plugin/community-project.init.gradle.kts \
          -Pcommunity.project.kotlin.repo=/path/to/maven/repo \
          -Pcommunity.project.kotlin.version=1.9.255-SNAPSHOT \
          -Pcommunity.project.kotlin.languageVersion=2.0 \
          -Pcommunity.project.kotlin.apiVersion=2.0
```

`community.project.plugin.build.path` optional property can be used to specify path to the directory containing plugin build. Defaults to the directory containing `community-project.init.gradle.kts`. 
