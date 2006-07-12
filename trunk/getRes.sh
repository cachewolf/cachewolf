#!/bin/sh
# clean up
rm ./work/*.png
rm ./work/*.gif
rm ./work/*.html
rm ./work/*.tpl
rm ./work/*.ico
# get ressources
cp resources/*.* work
mv work/cachewolf.Languages.cfg work/_config


