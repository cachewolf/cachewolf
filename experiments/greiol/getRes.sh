#!/bin/sh

filestosave="pref.xml garminmap.xml"

for savefile in $filestosave; do 
	if [ -f "work/$savefile" ]; then
		echo "backup of $savefile"
		mv "work/$savefile" /tmp/
		if [ $? -ne 0 ] ; then
			echo "could not backup $savefile to /tmp. aborting"
			exit 1
		fi
	fi
done

# clean up
rm -rf work

#make directorys if they don't exist
mkdir -p work/attributes
mkdir -p work/webmapservices
mkdir -p work/languages
mkdir -p work/templates
mkdir -p work/exporticons

# get ressources
cp -fa resources/*.* work
cp -fa res_noewe/*.* work
cp -fa resources/attributes/* work/attributes
cp -fa res_noewe/webmapservices/* work/webmapservices/
cp -fa res_noewe/languages/* work/languages/
cp -fa res_noewe/templates/* work/templates/
cp -fa res_noewe/exporticons/* work/exporticons/

# set sane permissions
find work -type f -exec chmod 644 "{}" \;
find work -type d -exec chmod 755 "{}" \;

# restore of pref.xml
for savefile in $filestosave; do
	if [ -f "/tmp/$savefile" ] ; then
		echo "restore of $savefile"
		mv "/tmp/$savefile" "work/"
	fi
done
#
