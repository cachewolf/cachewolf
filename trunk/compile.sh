#!/bin/sh
javac -cp ./lib/CompileEwe.zip:./lib/ewesoft.zip:./lib/EwesoftRegex.zip:./lib/HTML.zip:./lib/openmap.jar  -d ./bin/ -deprecation -nowarn  ./src/CacheWolf/*.java ./src/exp/*.java ./src/utils/*.java ./src/CacheWolf/navi/*.java 
