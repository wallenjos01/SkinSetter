pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        mavenLocal()
    }

    includeBuild("gradle/plugins")
}

rootProject.name = "skinsetter"


include("api")
include("common")

include("fabric")
include("spigot")
