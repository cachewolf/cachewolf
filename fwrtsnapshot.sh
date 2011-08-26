#!/bin/sh
# $Id$

PATH=$PATH:/usr/local/bin:/usr/bin:$HOME/bin:./programs
LC_ALL=C
unset LANG LANGUAGE
export PATH LC_ALL

# natureshadow insists on using “which”… so be it
# allow the caller to override the paths to the tools
test -n "$EWE" || EWE=$(which ewecl)
test -n "$CPIO" || CPIO=$(which cpio)

set -x
v=$(svn info | sed -n '/Last Changed Rev: /s///p')
printf '/VER_SVN =/s/\$.*\$/$LastChanged''Revision: %s $/\nw\nq\n' $v | \
    ed -s src/CacheWolf/Version.java
rm -rf bin
mkdir -p bin/CacheWolf
javac -source 1.3 -target 1.1 -encoding windows-1252 \
     -cp lib/CompileEwe.zip:lib -d bin -deprecation -nowarn \
     src/CacheWolf/*.java src/CacheWolf/*/*.java src/CacheWolf/navi/touchControls/*.java src/CacheWolf/view/*/*.java \
	 src/org/bouncycastle/*/*.java src/org/bouncycastle/*/*/*.java src/org/bouncycastle/*/*/*/*.java
javac -source 1.3 -target 1.1 -encoding windows-1252 \
    -cp ./lib/CompileEwe.zip:lib -d lib -deprecation -nowarn \
	lib/net/ax86/*.java ./lib/org/json/*.java
$EWE programs/Jewel.ewe -c cw-pda.jnf
$EWE programs/Jewel.ewe -c cw-ppc2003.jnf
$EWE programs/Jewel.ewe -c cw-pc.jnf
$EWE programs/Jewel.ewe -c cw-jar.jnf
# Don’t change the order of the above Jewel commands because
# the PC version has to overwrite the PDA version of the EWE file
rm -rf published
if test '!' -e programs/CacheWolf/Jar/CacheWolf.bat; then
	rm -rf bin
	exit 1
fi
mkdir -p published/dat/attributes
mv programs/CacheWolf/* published/
cp lib/java_ewe.dll published/Jar/

chmod -R u+w published

printf '1,$g/ 12M/s///\nw\nq\n' | ed -s published/Jar/CacheWolf.bat
#preparing the files for datfiles zip/tgz in ../published/dat
## CacheWolf.ewe from work and all files from res_noewe
cp work/CacheWolf.ewe published/
(cd res_noewe && cp -R * ../published/dat/)
## vga-sized attributes
cp res_noewe/attributes-big/*.gif published/dat/attributes/
## ewe libs
cp platform-dep/PocketPC2003/ewe.dll published/dat/
mkdir -p published/dat/PNA-WinCE42
cp -R platform-dep/PNA-WinCE42/ewe.dll published/dat/PNA-WinCE42/
# make datfiles zip and tgz
chmod -R 0755 published
find published -type f -print0 | xargs -0 chmod 0644
(
	cd published/dat
	find * -type f | fgrep -v /.svn/ | sort >../flst
	$CPIO -oC512 -Hustar -Mdist <../flst | gzip -n9 >../datfiles.tgz
	zip -X ../datfiles.zip -@ <../flst
)
rm -rf published/dat published/flst
chmod 644 published/datfiles.tgz published/datfiles.zip
mkdir -p $HOME/public_html/CacheWolf-BE/r$v
mv published/* $HOME/public_html/CacheWolf-BE/r$v/
rm -rf bin published
