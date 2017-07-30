if not exist bin\CacheWolf mkdir bin\CacheWolf
javac -source 1.3 -target 1.1 -cp ./Libraries/CompiledEwe.jar;./lib/  -d ./bin/ -deprecation ./src/CacheWolf/*.java ./src/CacheWolf/imp/*.java ./src/CacheWolf/controls/*.java ./src/CacheWolf/database/*.java ./src/CacheWolf/navi/*.java ./src/CacheWolf/navi/touchControls/*.java ./src/CacheWolf/exp/*.java ./src/CacheWolf/utils/*.java ./src/CacheWolf/model/*.java ./src/CacheWolf/view/*.java  ./src/CacheWolf/view/ewe/*.java  ./src/CacheWolf/view/pda/*.java
pause
