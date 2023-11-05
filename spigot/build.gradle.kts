import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar

plugins {
    id("skinsetter-build")
    id("skinsetter-publish")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
    alias(libs.plugins.shadow)
}


// MultiVersion
multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

configurations.create("shadow17").extendsFrom(configurations.shadow.get())
configurations.create("shadow8") {
    extendsFrom(configurations.shadow.get())
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
}

tasks.named<Jar>("java8Jar") {
    val id = project.properties["id"]
    archiveBaseName = "${id}-${project.name}"
}

tasks.shadowJar {
    archiveClassifier.set("1.17-1.20")
    configurations = listOf(project.configurations["shadow17"])
    minimize {
        exclude("org.wallentines.*")
    }
}

val java8ShadowJar = tasks.register<ShadowJar>("java8ShadowJar") {
    from(sourceSets["java8"].output)
    from(tasks.processResources.get().destinationDir)
    archiveClassifier.set("1.8-1.16")
    configurations = listOf(project.configurations["shadow8"])
    minimize {
        exclude("org.wallentines.*")
    }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
    dependsOn(java8ShadowJar)
}


repositories {
    maven("https://libraries.minecraft.net/")
    maven("https://maven.wallentines.org/")
    mavenLocal()
}


dependencies {

    // SkinSetter
    api(project(":api"))
    api(project(":common"))
    api(libs.midnight.core.spigot)

    shadow(project(":common").setTransitive(false))
    shadow(project(":api").setTransitive(false))

    compileOnly(libs.midnight.cfg)
    compileOnly(libs.midnight.cfg.json)
    compileOnly(libs.midnight.cfg.binary)

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(libs.jetbrains.annotations)

}


tasks.withType<ProcessResources>() {
    filesMatching("plugin.yml") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String))
        )
    }
}
