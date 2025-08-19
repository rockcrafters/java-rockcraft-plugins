plugins {
    id("com.gradleup.shadow") version "9.0.2"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
    id ("org.gradlex.reproducible-builds") version "1.0"
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
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("-Xlint:all")
}
