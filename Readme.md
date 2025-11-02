# Introduction

The primary goal of this project is to migrate notes from Bear (MacOS) to [Memos](https://github.com/usememos/memos) (Any platform).
It's mainly for my own usage as I'm using Memos at work (Restrictions preventing me to use Bear).

As I'm preferring the layout of Bear and will keep Bear as my main bearNote taking application, I'm literally creating files based on tags in Bear.

Code will also produce a list of notes being modified, created or deleted in Memos as a subfolder of the memos folder. This will help making the modifications manually in Bear in the future

# Building the code

```bash
./gradlew build
```

# Running the code

Building the code will create a self-running jar file inside your `build/libs` directory

```bash
java -jar build/libs/notes-exporter-1.0-SNAPSHOT.jar --spring.config.additional-location=~/memos/config/
```
Which allows to override the values contained in the `application.yml` bundled with the jar. The values of interest will probably be:
```yaml
memos:
  folders:
    root: "/example/of/root/folder"
  token: "example-of-token created in Memos"
```

Note that the root folder for Memos need to correspond to the one you will use for the docker volume

# Start Memos using docker
The following command will start a docker instance of Memos
```bash
docker run -d \
  --name memos \
  --publish 5230:5230 \
  --volume "/example/of/root/folder/memos":/var/opt/memos \
  neosmemo/memos:stable
```

Note that the volume folder is a subfolder `memos` of the root folder defined in the Spring profile above.