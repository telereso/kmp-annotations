#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION" \
    -x :annotations-models:jsTestPackageJson -x :annotations-models:jsPackageJson \
    -x :annotations-client:jsPackageJson -x :annotations-client:jsTestPackageJson
#    ./gradlew closeAndReleaseSonatypeStagingRepository
elif [[ "$@" == *"--local" ]]; then
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION" \
    -x publishWatchosArm64PublicationToSonatypeRepository -x publishWatchosArm32PublicationToSonatypeRepository \
    -x publishJvmPublicationToSonatypeRepository -x publishWatchosArm32PublicationToSonatypeRepository \
    -x publishJvmPublicationToSonatypeRepository -x publishKotlinMultiplatformPublicationToSonatypeRepository \
    -x publishJvmPublicationToSonatypeRepository -x publishJvmPublicationToSonatypeRepository \
    -x publishIosArm64PublicationToSonatypeRepository -x publishIosSimulatorArm64PublicationToSonatypeRepository \
    -x publishIosX64PublicationToSonatypeRepository publishJsPublicationToSonatypeRepository \
    -x :annotations:publishJsPublicationToSonatypeRepository \
    -x :annotations-models:jsTestPackageJson -x :annotations-models:jsPackageJson \
    -x :annotations-client:jsPackageJson -x :annotations-client:jsTestPackageJson
else
  ./gradlew publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local" \
      -x publishWatchosArm64PublicationToSonatypeRepository -x publishWatchosArm32PublicationToSonatypeRepository \
      -x publishJvmPublicationToSonatypeRepository -x publishWatchosArm32PublicationToSonatypeRepository \
      -x publishJvmPublicationToSonatypeRepository -x publishKotlinMultiplatformPublicationToSonatypeRepository \
      -x publishJvmPublicationToSonatypeRepository -x publishJvmPublicationToSonatypeRepository \
      -x publishIosArm64PublicationToSonatypeRepository -x publishIosSimulatorArm64PublicationToSonatypeRepository \
      -x publishIosX64PublicationToSonatypeRepository publishJsPublicationToSonatypeRepository \
      -x :annotations:publishJsPublicationToSonatypeRepository \
      -x :annotations-models:jsTestPackageJson -x :annotations-models:jsPackageJson \
      -x :annotations-client:jsPackageJson -x :annotations-client:jsTestPackageJson
fi

