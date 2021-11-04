#!/bin/sh

ENCODING=${ENCODING:-windows-1252}

if [ ! -d bin ]; then
  mkdir bin
fi

compile_cw() {
   /opt/jdk1.8.0_144/bin/javac \
      -source 1.3 \
      -target 1.1 \
      -g \
      -encoding "${ENCODING}" \
      -cp ./Libraries/CompiledEwe.jar:./lib/ \
      -deprecation \
      -nowarn \
      "$@"
}

compile_cw \
   -d ./bin/ \
   ./src/gro/cachewolf/*/*.java \
   ./src/gro/bouncycastle/*/*.java \
   ./src/gro/bouncycastle/*/*/*.java \
   ./src/gro/bouncycastle/*/*/*/*.java \
   ./src/gro/bouncycastle/*/*/*/*/*.java \
   ./src/CacheWolf/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
