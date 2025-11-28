plugins {
    id("com.gradleup.shadow") version "8.3.9"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.0.0"
    id ("org.gradlex.reproducible-builds") version "1.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
   archiveClassifier.set("")
}

dependencies {
    implementation(project(":rockcraft"))
    implementation(libs.osdetector)
    implementation(libs.commons.text)
    implementation(libs.maven.model)
    implementation(libs.maven.model.builder)

    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.snakeyaml)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

gradlePlugin {
    website = "https://github.com/rockcrafters/java-rockcraft-plugins"
    vcsUrl = "https://github.com/rockcrafters/java-rockcraft-plugins"

    plugins {
        create("rockcraftPlugin") {
            id = "io.github.rockcrafters.rockcraft"
            displayName = "Rockcraft plugin"
            description = "Plugin for rock image generation"
            implementationClass = "com.canonical.rockcraft.gradle.RockcraftPlugin"
            tags = listOf("rockcraft", "rock", "container", "docker", "oci")
        }
    }
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform() {
        val testTags: String? by project
        if (testTags != null) {
            includeTags.add(testTags)
        }
    }
}
tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("-Xlint:all")
}
