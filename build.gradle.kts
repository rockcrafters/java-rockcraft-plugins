allprojects {
    version = "1.1.0"
    group = "io.github.rockcrafters"
    apply(plugin = "maven-publish")
    if (project.findProperty("gpr.repo") != null) {
        plugins.withType<MavenPublishPlugin>().configureEach {
            extensions.configure<PublishingExtension>() {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri(project.findProperty("gpr.repo") ?: "")
                        credentials {
                            username = project.findProperty("gpr.user") as String? ?: System.getenv("GHP_USERNAME")
                            password = project.findProperty("gpr.key") as String? ?: System.getenv("GHP_TOKEN")
                        }
                    }
                }
            }
        }
    }
}
