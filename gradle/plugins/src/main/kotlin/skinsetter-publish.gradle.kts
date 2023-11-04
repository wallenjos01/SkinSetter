plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

publishing {
    publications.create<MavenPublication>("maven") {
        if(rootProject == project) {
            artifactId = project.name
        } else {
            var name = project.name
            var currentParent = project.parent
            while(currentParent != rootProject) {
                name = currentParent!!.name + "-" + name
                currentParent = currentParent.parent
            }
            artifactId = rootProject.name + "-" + name
        }
        from(components["java"])
    }

    if (project.hasProperty("pubUrl")) {
        repositories.maven(project.properties["pubUrl"] as String) {
            name = "pub"
            credentials(PasswordCredentials::class.java)
        }
    }
}