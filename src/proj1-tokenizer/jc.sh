#!/bin/sh

# compile source into bin directory
javac -d bin src/*.java

# run Program entrypoint using given test name
java -cp bin Program tests/$1.minc
