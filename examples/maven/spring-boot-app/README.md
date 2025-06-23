# Introduction

This sample deploys a Spring Boot Application into the rock container. It uses the default configuration of `rockcraft-maven-plugin` and overrides `createService` property to disable the startup service.

# Building

Install the plugin if needed:

`(cd ../../../ && mvn install)`

Install docker if needed:

`snap install docker`

Build the rock. The rockcraft.yaml will be found in `target/rockcraft.yaml` and the rock file under `target/rock`:

`mvn install`

This will build the rock image and push it to the local docker daemon

# Running

Run the application:

`docker run spring-boot-app-sample exec /usr/bin/java -jar /jars/spring-boot-app-sample-0.0.1-SNAPSHOT.jar`

This will output:

`Hello World!{Hello=World}`

and start Tomcat on port 8080.
