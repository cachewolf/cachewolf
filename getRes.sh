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
mkdir -p work/exporticons/exporticons
mkdir -p work/symbols
mkdir -p work/mmc
mkdir -p work/mmc/Desktop
mkdir -p work/mmc/pda
mkdir -p work/mmc/pda_vga

# get ressources
cp -fa resources/*.* work
cp -fa res_noewe/*.* work
cp -fa res_noewe/attributes/* work/attributes
cp -fa res_noewe/mmc/Desktop/* work/mmc/Desktop/
cp -fa res_noewe/mmc/pda/* work/pda/
cp -fa res_noewe/mmc/pda_vga/* work/mmc/pda_vga/
cp -fa res_noewe/webmapservices/* work/webmapservices/
cp -fa res_noewe/languages/* work/languages/
cp -fa res_noewe/templates/* work/templates/
cp -fa res_noewe/exporticons/exporticons/* work/exporticons/exporticons/
cp -fa res_noewe/symbols/* work/symbols/

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
