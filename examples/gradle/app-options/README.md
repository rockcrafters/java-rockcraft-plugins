# Introduction

This example demonstrates the use of plugin options to customize the image.

# Building

Install the plugin if needed:

`(cd ../../../ && ./gradlew publishToMavenLocal)`

Build the rock. The rockcraft.yaml will be found in `build/rockcraft.yaml` and the rock file under `build/rock`:

`./gradlew build-rock -i`

Push the rock to the local Docker daemon:

`./gradlew push-rock -i`

# Running

Run the application:

`docker run app-options exec /usr/bin/java -jar jars/app-options-0.01`

Start the default service:

`docker run app-options -v`

# Troubleshooting

See [../README.md](../README.md#Troubleshooting).
