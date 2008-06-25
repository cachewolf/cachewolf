#!/bin/sh
javac -cp ./lib/CompileEwe.zip:./lib/  -d ./bin/ -deprecation -nowarn ./src/CacheWolf/*.java ./src/CacheWolf/*/*.java ./src/exp/*.java ./src/utils/*.java
