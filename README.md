# Smart Pigs

Collaborators: [Daksh Jotwani](https://github.com/dakshj), [Nidhi Mundra](https://github.com/nidhimundra)

## Building the Project
**Windows**

`gradlew clean`

`gradlew build`

**Unix**

`./gradlew clean`

`./gradlew build`

## Generating a JAR
**Windows**

`gradlew clean`

`gradlew fatJar`

**Unix**

`./gradlew clean`

`./gradlew fatJar`

## Starting a Pig Server
`java -jar {jar-file-path} 1 {port-number}`

## Starting a Game Server
`java -jar {jar-file-path} 0 {config-json-file-path}`

## Process
1. Start N Pig Servers.<br/>(on N unique ports if running on the same machine)
2. Specify each Pig Server's Host (IP Address / domain name) and port in a configuration JSON file.<br/>(Sample configuration JSON file is [here](https://github.com/umass-cs677/spring17-lab1-nidhimundra/blob/master/smartpigs/txt/sample.json).)
3. Specify the network of peers in the configuration JSON file.
4. Start one Game Server.
5. Your game is now running.
