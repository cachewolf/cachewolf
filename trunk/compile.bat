if not exist bin\CacheWolf mkdir bin\CacheWolf
if not exist bin\exp mkdir bin\exp
javac -classpath ./lib/CompileEwe.zip;./lib/ewesoft.zip;./lib/EwesoftRegex.zip;./lib/HTML.zip;./lib/openmap.jar  -d ./bin/ -deprecation ./src/CacheWolf/*.java ./src/CacheWolf/imp/*.java ./src/CacheWolf/navi/*.java ./src/exp/*.java ./src/utils/*.java
