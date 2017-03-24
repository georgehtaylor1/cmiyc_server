#!/bin/bash
mkdir -p bin
find src ../cmiyc_shared/src -name '*.java' -exec javac -d bin -cp lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar {} +
