#!/bin/bash
set -ev
if [ -z "${TRAVIS_TAG}" ]; then
	./gradlew build publish bintrayUpload
else
    ./gradlew build
fi
