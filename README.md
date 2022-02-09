## Setup instructions
- Create a `botToken` file in the working directory where this will run.
- Include in the first line of the file a Discord bot token that has access to the target message to be monitored
____
This will create an empty file `shutdown.flag` when a 🛑 reaction is added to the target message.
____
## Compile to native binary (in Linux)
- Download GraalVM CE Java 17 archive (`graalvm-ce-java17-22.0.0.2` tested here)
- Extract GraalVM to a directory, note down that directory (let's call it `<GraalVM_HOME>`)
- Open a terminal where the built `<jar_name>.jar` is located
- Run:
```bash
pushd <GraalVM_HOME>
export PATH="$(pwd)/bin":$PATH
export JAVA_HOME="$(pwd)"
popd
native-image -jar <jar_name>.jar \
  --allow-incomplete-classpath \
  -H:+AddAllCharsets \
  -H:ReflectionConfigurationFiles=<repository_root>/src/main/java/reflect-config.json
```

This should create a new executable self-contained binary that can run anywhere.