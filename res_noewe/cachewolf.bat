@echo off
echo "Starting Cachewolf..."
rem change into the directory this file is located in
cd %~d0%~p0
rem if a java vm is installed and cachewolf.jar exists, use it
rem otherwise use cachewolf.exe (= cachewolf.ewe with integrated ewe-vm)
if exist CacheWolf.jar (
javaw -version 2>nul
if not errorlevel 1 (
start javaw -Xms64M -Xmx1024M -cp CacheWolf.jar ewe.applet.Applet CacheWolf.CacheWolf
) else (
start cachewolf.exe
) ) else (
start /b cachewolf.exe
)
exit
