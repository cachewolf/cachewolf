#!/bin/sh
javac -cp ./lib/compile/CompileEwe.zip:./lib/additional/  -d ./bin/ -deprecation -nowarn ./src/CacheWolf/*.java ./src/CacheWolf/*/*.java ./src/exp/*.java ./src/utils/*.java
