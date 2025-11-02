# Introduction

The primary goal of this project is to migrate notes from Bear (MacOS) to [Memos](https://github.com/usememos/memos) (Any platform).
It's mainly for my own usage as I'm using Memos at work (Restrictions preventing me to use Bear).

As I'm preferring the layout of Bear and will keep Bear as my main bearNote taking application, I'm literally creating files based on tags in Bear.

# Building the code

```bash
./gradlew build
```

# Running the code

Building the code will create a self-running jar file inside your `build/libs` directory

```bash
java -jar build/libs/notes-exporter-1.0-SNAPSHOT.jar --bear=<path to your bear directory> --token=<value for the API token created in memos> --url=<URL for memos>
```
- path to your bear directory: usually `~/Library/Group Containers/9K33E3U3T4.net.shinyfrog.bear`
- token: any output directory where you want to export your notes
- url: the url for memos: usually http://localhost:5230