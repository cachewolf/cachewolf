#!/bin/sh
cd work
java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -cp ../programs/ewe.jar:../lib_foo:../bin:/herehome/andi/CacheWolf/bouncyewe/bin/ Ewe CacheWolf.CacheWolf
cd ..
