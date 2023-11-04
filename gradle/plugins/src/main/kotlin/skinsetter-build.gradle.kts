plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.wallentines.org/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.withType<Jar>() {
    val id = project.properties["id"]
    archiveBaseName = "${id}-${project.name}"
}

tasks.withType<Test>() {
    useJUnitPlatform()
    workingDir("run/test")
}