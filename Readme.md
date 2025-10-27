# Introduction

The primary goal of this project is to migrate notes from Bear (MacOS) to Obsidian (Any platform).
It's mainly for my own usage as I'm using Obsidian at work (Windows Platform).

As I'm preferring the layout of Bear and will keep Bear as my main bearNote taking application, I'm literally creating files based on tags in Bear.
A single bearNote is then duplicated based on its tag definition.

Exportation of images and PDF is working and we get access to the files directly inside Obsidian. Images are going into an `attachments` folder and pdf into `attachments/pdf`

I'm using a cloud sync environment to replicate changes I might be doing while on Windows and have a rudimentary checksum mechanism which lists the files I might have modified or added on Windows based on the checksum at the time of exportation.

In the case files have been changed, you will need to manually change or add these in Bear and remove the exported folders to proceed with another exportation (exportation checks first that nothing has been modified on the Obsidian side)

# Configuration
Note that the file `src/main/resources` lists the location to the SQLite DB for Bear. This should work with your user but change it otherwise.

# Building the code

```bash
./gradlew build
```

# Running the code

Building the code will create a self-running jar file inside your `build/libs` directory

```bash
java -jar build/libs/notes-exporter-1.0-SNAPSHOT.jar --bear=<path to your bear directory> --output=<path to your exportation directory>
```
- path to your bear directory: usually `~/Library/Group Containers/9K33E3U3T4.net.shinyfrog.bear`
- output: any output directory where you want to export your notes