if not exist bin\CacheWolf mkdir bin\CacheWolf
javac -source 1.3 -target 1.1 -classpath ./lib/CompileEwe.zip;./lib/  -d ./bin/ -deprecation ./src/CacheWolf/*.java ./src/CacheWolf/imp/*.java ./src/CacheWolf/navi/*.java ./src/CacheWolf/exp/*.java ./src/CacheWolf/utils/*.java
pause
