#!/bin/sh
# clean up
rm ./work/*.png
rm ./work/*.gif
rm ./work/*.html
rm ./work/*.tpl
rm ./work/*.ico
# get ressources
cp resources/*.* work
cp res_noewe/*.* work
cp resources/attributes/*.* work/attributes
cp res_noewe/webmapservices/*.* work/webmapservices/*.*
cp res_noewe/languages/*.* work/languages/*.*
