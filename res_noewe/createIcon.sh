#!/bin/bash

if [ $# != 1 ]; then
  echo "Usage convertsvgs <dir>"
  echo "Converts all SVG-file in <dir> and its subdir to PNG-files."
  echo "If <dir> is not given, then svg is used as default."
  exit 1
fi

function toPNG {
    echo $1
    pushd $1
    for j in *.svg; do
        export fn=`basename $j .svg`
        echo Converting $j to PNG
        #add -resize <w>x<h> here if you want to resize the image
        convert "$fn".svg "$fn".png     
    done
    popd
}

pushd $1
for i in `find -type d`; do
    toPNG $i
done
popd
