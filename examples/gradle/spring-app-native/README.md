# Introduction 

This example builds a rock image with default settings for a spring boot application.

# Building

### Plugin installation

To use the latest rockcraft plugins, run this gradle command:
`(cd ../../../ && ./gradlew publishToMavenLocal)`


### Docker installation and configuration

Install docker if not already installed:

```
snap install docker
```

If you are running docker as a normal user, create and join the docker group:

```
sudo addgroup --system docker
sudo adduser $USER docker
newgrp docker
```

Disable and re-enable the docker snap if you added the group while Docker Engine was running:
```
sudo snap disable docker
sudo snap enable docker
```

Manually connect relevant plugs and slots:
```
sudo snap connect docker:privileged :docker-support
sudo snap connect docker:support :docker-support
sudo snap connect docker:firewall-control :firewall-control
sudo snap connect docker:network-control :network-control
sudo snap connect docker:docker-cli docker:docker-daemon
sudo snap connect docker:home

sudo snap disable docker
sudo snap enable docker
```

> **_NOTE:_** : Refer to the docker snap [README](https://github.com/canonical/docker-snap/blob/main/README.md) for more details.

### GraalVM installation

You may install the GraalVM Community Edition snap:

```
snap install graalvm-jdk --channel=v21
```

### Build the rock


```
export GRAALVM_HOME=/snap/graalvm-jdk/current/graalvm-ce
./gradlew nativeCompile push-rock -i
```
This will build the rock image and push it to the local docker daemon. The rockcraft.yaml will be found in `build/rockcraft.yaml` and the rock file under `build/rock`.

# Running

Run the application:

```
docker run spring-app-native:latest exec /spring-app-native
```

This will output `Hello, World!` among other log output and exit.

# Creating a build rock
You may also create an docker image to build the local application. Such images could be used in continuous integration pipelines to build and test the application.

### Building the build-rock

```
export GRAALVM_HOME=/snap/graalvm-jdk/current/graalvm-ce
./gradlew nativeCompile push-build-rock -i
```

This will build the build-rock image and push it to the local docker daemon. The rockcraft.yaml will be found in `build/build-rock/rockcraft.yaml` and the rock file under `build/build-rock/rock`.

### Build the local application sources using the build-rock

```
cd java-rockcraft-plugins/examples/gradle/spring-app-native
docker run -v `pwd`:`pwd` --user $(id -u):$(id -g) --env PEBBLE=/tmp build-spring-app-native exec build `pwd`
```

This should launch the build-container and produce the native image `build/native/nativeCompile/spring-app-native`.
