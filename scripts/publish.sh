#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION" \
    -x :annotations-models:jsTestPackageJson -x :annotations-models:jsPackageJson \
    -x :annotations-client:jsPackageJson -x :annotations-client:jsTestPackageJson
#    ./gradlew closeAndReleaseSonatypeStagingRepository
else
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION" \
    -x :annotations-models:jsTestPackageJson -x :annotations-models:jsPackageJson \
    -x :annotations-client:jsPackageJson -x :annotations-client:jsTestPackageJson
fi

