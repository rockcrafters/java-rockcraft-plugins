# Introduction

This sample deploys a shaded jar into the rock container. It uses the default configuration of `rockcraft-maven-plugin` and overrides `createService` property to disable the startup service.

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

`docker run shaded-jar-sample exec /usr/bin/java -jar jars/shaded-jar-sample-1.0-SNAPSHOT.jar`

This will output:

`Hello World!{Hello=World}`

# Troubleshooting

See [README.md](../../README.md#Troubleshooting).
