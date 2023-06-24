# Structure

## Annotations
This module will hold all annotations to be used ,eg : `@ReactNativeExport`, `@FlutterExport` ..etc

## Processor
A ksp processor inside each package will have the code generation logic

## Gradle Plugin
This module will package all needed dependencies and tasks for the kmp project structure to work

# Versioning
Handled by the ci/cd (using tags)

# Samples
Not provided yet (working on it), inject this project locally into another local project using absolute paths 

# Project Secrets
Handled by the pipeline , but project can run locally without any secrets 

# Debug 
Run the following command 

```shell
./gradlew :annotations-client:kspCommonMainKotlinMetadata --no-daemon -Dorg.gradle.debug=true -Pkotlin.compiler.execution.strategy=in-process
```

```shell
./gradlew :annotations-models:kspCommonMainKotlinMetadata --no-daemon -Dorg.gradle.debug=true -Pkotlin.compiler.execution.strategy=in-process
```

Then click debug `ksp`