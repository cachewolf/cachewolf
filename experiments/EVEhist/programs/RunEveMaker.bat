rem make the working dir the one this file is in
cd %~d0%~p0
java -cp eve.jar Eve EveMaker.eve
