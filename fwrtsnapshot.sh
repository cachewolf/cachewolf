#!/bin/sh
# $Id$

v=$(svn info | sed -n '/Revision: /s///p')
echo '/VER_SVN =/s/\$.*\$/$LastChangedRevision: '$v' $/\nwq' | \
    ed -s src/CacheWolf/Version.java
javac -cp ./lib/CompileEwe.zip:./lib/  -d ./bin/ -deprecation -nowarn ./src/CacheWolf/*.java ./src/CacheWolf/*/*.java ./src/exp/*.java ./src/utils/*.java
java -cp lib/ewe.jar Ewe programs/Jewel.ewe -c cwberlios.jnf
-rm -rf published
mkdir -p published/attributes
cd programs/CacheWolf && mv * ../../published/
chmod 755 published/*
chmod 644 published/*/*
install -c -m 644 work/CacheWolf.ewe published/
install -c -m 644 resources/*.{def,html,tpl,zip} published/
install -c -m 644 resources/attributes-big/*.gif published/attributes/
mkdir -p ~/public_html/CacheWolf-BE/r$v
cd published && \
    find *.{def,html,tpl,zip} attributes/*.gif -type f | sort | \
    cpio -oC512 -Hustar -Mdist | gzip -n9 >datfiles.tgz && \
    rm -rf *.{def,html,tpl,zip} attributes && \
    chmod 644 datfiles.tgz && \
    mv * ~/public_html/CacheWolf-BE/r$v/
rm -rf published
