if not exist bin\CacheWolf mkdir bin\CacheWolf
javac -cp ./lib/CompileEwe.zip;./lib/ewesoft.zip;./lib/EwesoftRegex.zip;./lib/HTML.zip;./lib/openmap.jar  -d ./bin/ -deprecation ./src/CacheWolf/*.java 