#!/bin/sh

cd "$(dirname "$0")"
java -Xms64M -Xmx1024M -cp CacheWolf.jar ewe.applet.Applet CacheWolf.CacheWolf &
