#!/bin/bash

PUBLISH_VERSION=0.0.3

./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"