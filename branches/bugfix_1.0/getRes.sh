#!/bin/sh

# clean up
rm ./work/*.png
rm ./work/*.gif
rm ./work/*.html
rm ./work/*.tpl
rm ./work/*.ico
rm ./work/languages/*
rm ./work/attributes/*
rm ./work/webmapservices/*

#make directorys if they don't exist
mkdir work/attributes
mkdir work/webmapservices
mkdir work/languages

# get ressources
cp resources/*.* work
cp res_noewe/*.* work
cp resources/attributes/* work/attributes
cp res_noewe/webmapservices/* work/webmapservices/
cp res_noewe/languages/* work/languages/
