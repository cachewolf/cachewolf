cd %~d0%~p0
cd ..
javac -source 1.3 -target 1.1 -encoding windows-1252 -cp lib/CompileEve.zip;lib/lib-folder ^
  -d bin -deprecation -nowarn src/cachewolf/*.java src/cachewolf/exp/*.java ^
  src/cachewolf/imp/*.java src/cachewolf/navi/*.java src/cachewolf/utils/*.java ^
  src/eve/ui/formatted/*.java src/eve/ui/formatted/data/*.java 
rmdir /s /q bin\com > NUL
rmdir /s /q bin\ewesoft > NUL
rmdir /s /q bin\HTML > NUL

pause
