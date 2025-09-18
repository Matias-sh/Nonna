pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Nonna"

// Core modules
include(":core:ui")
include(":core:common")
include(":core:domain")
include(":core:data")

// Feature modules
include(":feature:onboarding")
include(":feature:auth")
include(":feature:memories")
include(":feature:timeline")
include(":feature:conversation")
include(":feature:genealogy")
include(":feature:room3d")
include(":feature:sharing")
include(":feature:settings")

// App module
include(":app")
