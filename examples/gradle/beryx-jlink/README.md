# Introduction 

This example builds a rock image for a modular Java application using an application image provided by [Beryx JLink](https://badass-jlink-plugin.beryx.org/releases/latest/) plugin.

# Building 

Install the plugin if needed:

`(cd ../../../ && ./gradlew publishToMavenLocal)`

Build the rock. The rockcraft.yaml will be found in `build/rockcraft.yaml` and the rock file under `build/rock`:

`./gradlew build-rock -i`

Push the rock to the local Docker daemon:

`./gradlew push-rock -i`

# Running

`docker run app exec /image/bin/app`

# Troubleshooting

See [../README.md](../README.md#Troubleshooting).
