#!/bin/sh

java -cp eve.jar Eve EveMaker.eve -c ../deploy/cw-eve-pc.enf
java -cp eve.jar Eve EveMaker.eve -c ../deploy/cw-eve-pda.enf

# Don't change the order above because the PC version has to
# overwrite the PDA version of the EWE-file
