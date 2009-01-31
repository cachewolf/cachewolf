rem make the working dir the one this file is in
cd %~d0%~p0
java -cp eve.jar Eve EveMaker.eve -c ../deploy/cw-eve-pc.enf
copy ..\deploy\Exe\CacheWolf.exe ..\work
cd ..\work
cachewolf.exe
rem "\Program Files\eve\Eve.exe" cachewolf.eve
