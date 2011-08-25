#!/bin/sh

ENCODING=${ENCODING:-windows-1252}

do_compile() {
   javac \
      -source 1.3 \
      -target 1.1 \
      -encoding "${ENCODING}" \
      -cp ./lib/CompileEwe.zip:./lib/ \
      -deprecation \
      -nowarn \
      "$@"
}

do_compile \
   -d ./bin/ \
   ./src/org/bouncycastle/*/*.java \
   ./src/org/bouncycastle/*/*/*.java \
   ./src/org/bouncycastle/*/*/*/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
   ./src/CacheWolf/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
do_compile \
   lib/net/*/*.java \
   lib/org/*/*.java
