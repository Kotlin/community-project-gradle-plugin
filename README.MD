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