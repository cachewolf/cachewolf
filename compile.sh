#!/bin/sh

ENCODING=${ENCODING:-windows-1252}

if [ ! -d bin ]; then
  mkdir bin
fi

compile_cw() {
   javac \
      -source 1.3 \
      -target 1.1 \
      -encoding "${ENCODING}" \
      -cp ./Libraries/CompiledEwe.jar:./lib/ \
      -deprecation \
      -nowarn \
      "$@"
}

compile_cw \
   -d ./bin/ \
   ./src/CacheWolf/*.java \
   ./src/CacheWolf/*/*/*.java \
   ./src/CacheWolf/*/*.java
