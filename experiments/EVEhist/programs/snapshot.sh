#!/bin/sh

if [ ! -e snapshot.sh ]; then
	echo "This script must be run from inside the programs/ directory!"
	exit 1
fi

cd ..

echo Updating version number in code ...
v=$(svn info | sed -n '/Revision: /s///p')
# the '' below are so that svn doesn't ever fuck up _this_ file
printf '/VER_SVN =/s/\$.*\$/$LastChangedRe''vision: %s $/\nwq\n' $v | \
    ed -s src/cachewolf/Version.java

echo Removing old build ...
rm -rf bin

echo Compiling Java classes ...
mkdir -p bin/cachewolf
javac -source 1.3 -target 1.1 -encoding windows-1252 \
    -cp lib/CompileEve.zip:lib/JavaEve.zip:lib/lib-folder -d bin \
    -deprecation -nowarn src/eveWorkArounds/*.java src/cachewolf/*.java src/cachewolf/*/*.java

echo Linking executables ...
cd programs
java -cp evecl-gui.jar Eve EveMaker.eve -c ../deploy/cw-eve-pc.enf
java -cp evecl-gui.jar Eve EveMaker.eve -c ../deploy/cw-eve-pda.enf

cd ..
mkdir -p published/dat/attributes
mkdir -p published/dat/webmapservices
mkdir -p published/dat/languages
mv deploy/CacheWolf/* published/
chmod 755 published/*
cp lib/java_ewe.dll published/Jar/
chmod 644 published/*/*
chmod 755 published/dat/attributes
chmod 755 published/dat/webmapservices
chmod 755 published/dat/languages
printf '1,$g/ 12M/s///\nwq\n' | ed -s published/Jar/CacheWolf.bat
install -c -m 644 work/CacheWolf.ewe published/
install -c -m 644 res_noewe/* published/dat/
install -c -m 644 res_noewe/attributes-big/*.gif published/dat/attributes/
install -c -m 644 res_noewe/attributes/*-non.gif published/dat/attributes/
install -c -m 644 res_noewe/webmapservices/* published/dat/webmapservices/
install -c -m 644 res_noewe/languages/* published/dat/languages/
(
	cd published/dat
	find * -type f | sort >../flst
	$CPIO -oC512 -Hustar -Mdist <../flst | gzip -n9 >../datfiles.tgz
	zip -X ../datfiles.zip -@ <../flst
)
rm -rf published/dat published/flst
chmod 644 published/datfiles.tgz published/datfiles.zip
mkdir -p $HOME/public_html/CacheWolf-BE/r$v
mv published/* $HOME/public_html/CacheWolf-BE/r$v/
rm -rf bin published
