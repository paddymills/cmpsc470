#!/bin/bash

# compile source into bin directory
javac -d bin src/*.java

if [ $# -eq 0 ]; then
    for i in {1..8}; do
        # java -cp bin Program tests/test$i.minc
        echo "File: test$i"
        diff -ywqs --suppress-common-lines <(java -cp bin Program tests/test$i.minc) tests/testsolu$i.txt
    done;
else
    # run Program entrypoint using given test name
    # java -cp bin Program tests/test$1.minc
    diff --color=always -yw <(java -cp bin Program tests/test$1.minc) tests/testsolu$1.txt
fi


# diff
# for i in {0..8}; do
#     result = ${java -cp bin Program "test$i"}
#     solution = "tests/testsolu$i.txt"
#     diff --color=always --width=80 -yw --suppress-common-lines <($result) $solution
# done;