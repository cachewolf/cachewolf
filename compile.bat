if not exist bin\CacheWolf mkdir bin\CacheWolf
javac -source 1.3 -target 1.1 -cp ./lib/CompileEwe.zip;./lib/  -d ./bin/ -deprecation ./src/CacheWolf/*.java ./src/CacheWolf/imp/*.java ./src/CacheWolf/navi/*.java ./src/CacheWolf/navi/touchControls/*.java ./src/CacheWolf/exp/*.java ./src/CacheWolf/utils/*.java ./src/CacheWolf/model/*.java ./src/CacheWolf/view/*.java  ./src/CacheWolf/view/ewe/*.java  ./src/CacheWolf/view/pda/*.java
javac -source 1.3 -target 1.1 -cp ./lib/CompileEwe.zip;./lib/  -d ./lib/ -deprecation ./lib/net/ax86/*.java ./lib/org/json/*.java
pause
