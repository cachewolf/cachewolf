#!/bin/sh

CP1252=${ENCODING:-windows-1252}
UTF8=${ENCODING:-UTF-8}

compile_cw() {
   javac \
      -source 1.3 \
      -target 1.1 \
      -encoding "${UTF-8}" \
      -cp ./lib/CompileEwe.zip:./lib/ \
      -deprecation \
      -nowarn \
      "$@"
}

compile_json() {
   javac \
      -source 1.3 \
      -target 1.1 \
      -encoding "${cp1252}" \
      -cp ./lib/CompileEwe.zip:./lib/ \
      -deprecation \
      -nowarn \
      "$@"
}

compile_cw \
   -d ./bin/ \
   ./src/org/bouncycastle/*/*.java \
   ./src/org/bouncycastle/*/*/*.java \
   ./src/org/bouncycastle/*/*/*/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
   ./src/CacheWolf/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
compile_json \
   lib/net/*/*.java \
   lib/org/*/*.java
