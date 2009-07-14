#!/bin/sh

# backup of prefs.xml
if [ -f "work/pref.xml" ]; then
	echo "backup of pref.xml"
	mv work/pref.xml /tmp
	if [ $? -ne 0 ] ; then
		echo "echo could not save pref.xml to /tmp. aborting"
	fi
fi

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

# set sane permissions
find work -type f -exec chmod 644 "{}" \;
find work -type d -exec chmod 755 "{}" \;

# restore of pref.xml
if [ -f "/tmp/pref.xml" ] ; then
	echo "restore of pref.xml"
	mv /tmp/pref.xml work/pref.xml
fi
#
