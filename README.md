# CPS JAVA CONTRACTS

This repository contains the smart contracts for CPS in Java.

### Requirement
- JDK 11+ 

### Setting up Local Environment 

- Clone submodule 

```git submodule update --init```

- Run unit tests

```./gradlew clean build optimizedJar```

### Run integration tests

- Install [docker](https://docs.docker.com/engine/install/) and [docker compose](https://docs.docker.com/compose/install/)
- Start local blockchain

```docker compose up -d```


- execute following command

```./gradlew intTestClasses```

```./gradlew assemble```

```./gradlew integrationTest```