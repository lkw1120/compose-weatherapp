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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "WeatherApp"
include(":app")
include(":core")
include(":core-domain")
include(":core-data")
include(":core-ui")
include(":feature-main")
include(":feature-search")
include(":feature-bookmarks")
include(":feature-settings")
include(":feature-weather-preview")
include(":feature-widget")
include(":navigation")
