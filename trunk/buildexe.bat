cd %~d0%~p0
call getres.bat
java -cp lib/ewe.jar Ewe programs/Jewel.ewe -c cw-pda.jnf
java -cp lib/ewe.jar Ewe programs/Jewel.ewe -c cw-pc.jnf
REM Dont change the order above because the PC version has to overwrite the PDA version of the EWE-file
pause
