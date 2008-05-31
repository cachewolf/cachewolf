#!/bin/sh
# $Id$

set -x
v=$(svn info | sed -n '/Revision: /s///p')
printf '/VER_SVN =/s/\$.*\$/$LastChangedRevision: %s $/\nwq\n' $v | \
    ed -s src/CacheWolf/Version.java
rm -rf bin
mkdir -p bin/CacheWolf
javac -source 1.3 -target 1.1 -encoding windows-1252 \
    -cp lib/CompileEwe.zip:lib -d bin -deprecation -nowarn \
    src/CacheWolf/*.java src/CacheWolf/*/*.java src/exp/*.java src/utils/*.java
`which ewecl` programs/Jewel.ewe -c cw-pda.jnf
`which ewecl` programs/Jewel.ewe -c cw-pc.jnf
# Don’t change the order of the above Jewel commands because
# the PC version has to overwrite the PDA version of the EWE file
rm -rf published
if test '!' -e programs/CacheWolf/Jar/CacheWolf.bat; then
	rm -rf bin
	exit 1
fi
mkdir -p published/dat/attributes
mkdir -p published/dat/webmapservices
mkdir -p published/dat/languages
mv programs/CacheWolf/* published/
chmod 755 published/*
cp lib/java_ewe.dll published/Jar/
chmod 644 published/*/*
chmod 755 published/dat/attributes
chmod 755 published/dat/webmapservices
chmod 755 published/dat/languages
printf '1,$g/ 12M/s///\nwq\n' | ed -s published/Jar/CacheWolf.bat
install -c -m 644 work/CacheWolf.ewe published/
install -c -m 644 res_noewe/* published/dat/
install -c -m 644 resources/attributes-big/*.gif published/dat/attributes/
install -c -m 644 resources/attributes/*-non.gif published/dat/attributes/
install -c -m 644 res_noewe/webmapservices/* published/dat/webmapservices/
install -c -m 644 res_noewe/languages/* published/dat/languages/
(cd published/dat && find * -type f | sort | \
    `which cpio` -oC512 -Hustar -Mdist >../datfiles.tar)
rm -rf published/dat
chmod 644 published/datfiles.tar
mkdir -p $HOME/public_html/CacheWolf-BE/r$v
mv published/* $HOME/public_html/CacheWolf-BE/r$v/
rm -rf bin published
