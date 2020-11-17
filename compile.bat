if not exist bin\CacheWolf mkdir bin\CacheWolf
dir /s /B src\*.java > sources.txt
javac -source 1.3 -target 1.1 -cp ./Libraries/CompiledEwe.jar;./lib/ -d ./bin/ -deprecation @sources.txt
pause
