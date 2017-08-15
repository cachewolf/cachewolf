cd %~d0%~p0
rem call getres.bat
rem java -cp lib\ewe.jar Ewe programs\Jewel.ewe -c cw-pda.jnf
rem java -cp lib\ewe.jar Ewe programs\Jewel.ewe -c cw-ppc2003.jnf
rem java -cp lib\ewe.jar Ewe programs\Jewel.ewe -c cw-pc.jnf
rem works wrong packing of some png 's( can replace them manually) : java -cp lib\ewe.jar Ewe programs\Jewel.ewe -c cw-jar.jnf
.\programs\ewe.exe programs\Jewel.ewe -c cw-pda.jnf
.\programs\ewe.exe programs\Jewel.ewe -c cw-ppc2003.jnf
.\programs\ewe.exe programs\Jewel.ewe -c cw-pc.jnf
.\programs\ewe.exe programs\Jewel.ewe -c cw-jar.jnf
REM Dont change the order above because the PC version has to overwrite the PDA version of the EWE-file

