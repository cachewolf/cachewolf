#!/bin/sh
javac	-source 1.3 \
	-target 1.1 \
	-encoding iso-8859-1 \
	-cp ./lib/CompileEwe.zip:./lib/  \
	-d ./bin/ \
	-deprecation \
	-nowarn \
	./src/CacheWolf/*.java \
	./src/CacheWolf/*/*.java \
	./src/exp/*.java \
	./src/utils/*.java
