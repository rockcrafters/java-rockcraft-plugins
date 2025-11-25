# Introduction 

This example builds a rock image with default settings for a spring boot application.

# Building 

Install the plugin if needed:

`(cd ../../../ && ./gradlew publishToMavenLocal)`

Build the rock. The rockcraft.yaml will be found in `build/rockcraft.yaml` and the rock file under `build/rock`:

`./gradlew build-rock -i`

Push the rock to the local Docker daemon:

`./gradlew push-rock -i`

# Running

`docker run spring-app exec  /usr/bin/java -jar /jars/spring-app-0.0.1-SNAPSHOT.jar`

# Troubleshooting

See [README.md](../../README.md#Troubleshooting).
