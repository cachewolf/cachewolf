rem make the working dir the one this file is in
cd %~d0%~p0
rem call getres.bat

java -cp eve.jar Eve EveMaker.eve -c ../deploy/cw-eve-pc.enf
java -cp eve.jar Eve EveMaker.eve -c ../deploy/cw-eve-pda.enf
@REM Dont change the order above because the PC version has to overwrite the PDA version of the EWE-file
pause
