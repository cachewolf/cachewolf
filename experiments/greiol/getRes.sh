#!/bin/sh

# clean up
rm -rf work

#make directorys if they don't exist
mkdir -p work/attributes
mkdir -p work/webmapservices
mkdir -p work/languages

# get ressources
cp -fa resources/*.* work
cp -fa res_noewe/*.* work
cp -fa resources/attributes/* work/attributes
cp -fa res_noewe/webmapservices/* work/webmapservices/
cp -fa res_noewe/languages/* work/languages/

find work -type f | xargs chmod 644
find work -type d | xargs chmod 755
#
