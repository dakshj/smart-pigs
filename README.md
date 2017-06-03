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

## License

    Copyright 2017 Daksh Jotwani, Nidhi Mundra

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
