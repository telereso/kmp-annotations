# KMP Annotations 

A kotlin multiplatform (kmp) annotations to auto generate code to support and speed up building KMP SDKs, 

The project also support a [gradle plugin](https://plugins.gradle.org/plugin/io.telereso.kmp) that has helpful tasks to can setup the integration faster 


## Requirements

* kmp project structure , you can start your [kmp project here](https://kmp-starter.telereso.io) , 
  the annotations and gradle plugin are compatible with this structure but you can try using them in your own structure

## Annotations

### [@Serializable](https://kmp.telereso.io/annotations/Serializable.html) 
This is the same [official annotation in kotlin](https://kotlinlang.org/docs/serialization.html)
However the gradle plugin [io.telereso.kmp](https://plugins.gradle.org/plugin/io.telereso.kmp) will add json conversion extension functions
Example
```kotlin
@Serializable
data class Data(val a: Int, val b: String)
```
After adding the plugin you will get `fromJson`, `fromJsonArray` , `toJson` , and to `toPrettyJson` functions

```kotlin
val data = Data.fromJson("{\"a\": 1,\"b\": \"test\"}")
val json = data.toJson() // minified json
val json = data.toPrettyJson() // human readable json

// Also support json array
val jsonArray = Data.fromJsonArray("[{\"a\":1,\"b\":\"test\"},{\"a\":1,\"b\":\"test\"}]")
```
### [@ReactNativeExport](https://kmp.telereso.io/annotations/ReactNativeExport.html) (alpha)
An "interop" from kotlin to react native plugins , basically it convert a kmp sdk manager class into a ReactNative plugin manger interface.

### [@Builder](https://kmp.telereso.io/annotations/Builder.html)

### [@SwiftOverloads](https://kmp.telereso.io/annotations/SwiftOverloads.html)

### [@ListWrapper](https://kmp.telereso.io/annotations/ListWrapper.html)

### [@FlutterExport](https://kmp.telereso.io/annotations/FlutterExport.html) (WIP)
An "interop" from kotlin to flutter plugins , basically it convert a kmp sdk manager class into a Flutter plugin manger interface.
