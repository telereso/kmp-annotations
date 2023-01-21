#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--release" ]]; then
    ./gradlew gradle-plugin:publishPlugins "-PpublishVersion=$PUBLISH_VERSION"
else
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" gradle-plugin:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
fi

