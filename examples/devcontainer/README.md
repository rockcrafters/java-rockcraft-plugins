# Introduction 

This example showcases using a devcontainer built with Rockcraft plugin in Visual Studio Code.

# Building 

Install the plugin if needed:

`(cd ../../../ && ./gradlew publishToMavenLocal)`

Install Visual Studio code and Devcontainers extension:

```
$ snap install code 
$ code --install-extension ms-vscode-remote.remote-containers

```

Build the rock. The rockcraft.yaml will be found in `build/build-rock/rockcraft.yaml`:

`./gradlew build-build-rock -i`

Push the rock to the local Docker daemon:

`./gradlew push-build-rock -i`

# Running

Open the project in Visual Studio Code:

```
$ code .
```

