plugins {
    id("skinsetter-build")
    id("skinsetter-publish")
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
}


loom {
    runs {
        getByName("client") {
            runDir = "run/client"
            ideConfigGenerated(false)
            client()
        }
        getByName("server") {
            runDir = "run/server"
            ideConfigGenerated(false)
            server()
        }
    }
}


tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(project.configurations.shadow.get())
        minimize {
            exclude("org.wallentines.*")
        }
    }
    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)

        val id = project.properties["id"]
        archiveBaseName.set("${id}-${project.name}")
    }
}


repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.wallentines.org/")
    mavenLocal()
}


dependencies {

    // SkinSetter
    api(project(":api"))
    api(project(":common"))

    shadow(project(":api").setTransitive(false))
    shadow(project(":common").setTransitive(false))

    // Minecraft
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.15.0")

    // Fabric API
    modApi(fabricApi.module("fabric-command-api-v2", "0.91.1+1.20.4"))

    // MidnightCore
    modApi(libs.midnight.core.fabric)
}


tasks.withType<ProcessResources>() {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String))
        )
    }
}
