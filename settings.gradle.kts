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

rootProject.name = "k-chat"

include(":app")
include(":core:design")
include(":core:model")
include(":core:ui")
include(":core:navigation")
include(":data:repository")
include(":data:fake")
include(":data:local")
include(":data:network")
