plugins {
    id("skinsetter-build")
    id("skinsetter-publish")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
}

multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/")
    mavenLocal()
}

dependencies {

    api(project(":api"))

    api(libs.midnight.cfg)
    api(libs.midnight.cfg.json)
    api(libs.midnight.cfg.binary)
    api(libs.midnight.lib)
    api(libs.midnight.core)
    api(libs.midnight.core.server)

    api(libs.slf4j.api)

}