# Introduction

This example showcases using a devcontainer built with Rockcraft plugin in Visual Studio Code.

# Building

Install the plugin if needed:

`(cd ../../../ && ./gradlew publishToMavenLocal)`

Install Visual Studio Code and Devcontainers extension:

```
$ snap install code
$ code --install-extension ms-vscode-remote.remote-containers

```

Build and push the rock to the local Docker daemon:

`./gradlew push-build-rock -i`

The rockcraft.yaml will be found in `build/build-rock/rockcraft.yaml`.

# Running

Open the project in Visual Studio Code:

```
$ code .
```

Visual Studio Code will detect [devcontainer configuration](.devcontainer/devcontainer.json)
and prompt to open the project in the container.
