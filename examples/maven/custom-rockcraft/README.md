# Introduction

This sample demonstrates deploying a rock with a custom `rockcraft.yaml` that defines dependency deployment.

`maven-dependency-plugin` is used to copy dependencies to `target/dependencies/jars`. `rockcraft.yaml` defines a part that copies those dependencies to target image `/jars` directory:

```yaml
parts:
  deploy-dependencies:
    plugin: dump
    source: dependencies
    source-type: local
```

The `maven/rockcraft/runtime` is overriden to ensure that runtime is created after dependencies are copied in the target image:

```yaml
  maven/rockcraft/runtime:
    after:
      - deploy-dependencies
      - maven/rockcraft/dump
      - maven/rockcraft/deps
```

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

`docker run custom-rockcraft-sample exec /usr/bin/java -jar jars/custom-rockcraft-sample-1.0-SNAPSHOT.jar`

This will output:

`Hello World!{Hello=World}`

# Troubleshooting

See [README.md](../../README.md#Troubleshooting).
