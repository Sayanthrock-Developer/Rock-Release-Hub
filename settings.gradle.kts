pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RockReleaseHub"

include(":app")
include(":core-model")
include(":core-common")
include(":core-network")
include(":core-database")
include(":core-designsystem")
include(":feature-auth")
include(":feature-home")
include(":feature-repositories")
include(":feature-workflows")
include(":feature-releases")
include(":feature-downloads")
include(":feature-apkinspector")
include(":feature-updates")
include(":feature-settings")
