#!/bin/bash
set -ev
if [ -z "${TRAVIS_TAG}" ]; then
	./gradlew build
else
    ./gradlew build publish bintrayUpload
fi
