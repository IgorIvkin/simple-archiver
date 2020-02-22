# Simple Archiver
This small _Groovy_ class is intended to pack your folders ignoring some
files and sub-folders that you will add to exclusion list.

### How to use
Below you find a small example of script that will pack entire directory
`/home/test/` skipping all the possible `/.out` sub-folders and the file `/home/test/readme.txt`.

It will produce a zip-file named `2020_02_20_result.zip` where the date will be set
according to the current date.

```groovy
import java.text.SimpleDateFormat

SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd")
String targetZipFileName = dateFormat.format(new Date()) + "_result.zip"

SimpleArchiver archiver = new SimpleArchiver(
        "/home/test/",
        "/home/test/" + targetZipFileName)
archiver
        .setOutputFileName(true)
        .exclude("**/.out/**")
        .exclude("/home/test/readme.txt")
        .zip()
```