# Introduction

This sample deploys a Spring Boot Application compiled as a native executable into the rock container.

# Building

Install the plugin if needed:

```
(cd ../../../ && mvn install)
```

Install docker if needed:

```
snap install docker
```

Install GraalVM, if needed. You may install the GraalVM Community Edition snap:

```
snap install graalvm-jdk --channel=v21
```

Build the rock. The rockcraft.yaml will be found in `target/rockcraft.yaml` and the rock file under `target/rock`:

```
export GRAALVM_HOME=/snap/graalvm-jdk/current/graalvm-ce
mvn -Pnative native:compile install
```

This will build the rock image and push it to the local docker daemon

# Running

Run the application:

```
docker run spring-boot-native-sample exec /spring-boot-native-sample
```

This will output:

`Hello World!{Hello=World}`

and start Tomcat on port 8080.
