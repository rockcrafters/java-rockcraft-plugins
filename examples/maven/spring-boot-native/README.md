# Introduction

This sample deploys a Spring Boot Application compiled as a native executable into the rock container.

# Building

### Plugin installation

To use the latest rockcraft plugins, run an mvn install:

```
(cd ../../../ && mvn install)
```

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
mvn -Pnative native:compile install
```
This will build the rock image and push it to the local docker daemon. The rockcraft.yaml will be found in `target/rockcraft.yaml` and the rock file under `target/rock`.

# Running

Run the application:

```
docker run spring-boot-native-sample exec /spring-boot-native-sample
```

This will output:

`Hello World!{Hello=World}`

and start Tomcat on port 8080.
