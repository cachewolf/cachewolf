/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf;

public final class OC {

	/** thou shallst not instantiate this object */
	private OC() {
		// Nothing to do
	}

	public final static int OC_HOSTNAME = 0;
	public final static int OC_PREFIX = 1;
	public final static String[][] OCSites = {//
	{ "www.opencaching.de", "OC" },// 1
			{ "www.opencaching.pl", "OP" },// 2
			{ "www.opencaching.cz", "OZ" },// 3
			{ "www.opencaching.org.uk", "OK" },// 4
			{ "www.opencaching.se", "OS" },// 5
			{ "www.opencaching.no", "ON" },// 6
			{ "www.opencaching.us", "OU" },// 7
			{ "www.opencachingspain.es", "OC" },// 8 !!!
			{ "www.opencaching.it", "OC" },// 9 !!!
			{ "www.opencaching.jp", "OJ" },// 10
			{ "www.opencaching.nl", "OB" },// 11
	};

	public final static String[] OCHostNames() {
		String[] ret = new String[OCSites.length];
		for (int i = 0; i < OCSites.length; i++) {
			ret[i] = OCSites[i][OC_HOSTNAME];
		}
		return ret;
	}

	public final static String getOCHostName(String wpName) {
		for (int i = 0; i < OCSites.length; i++) {
			if (wpName.startsWith(OCSites[i][OC_PREFIX])) {
				return OCSites[i][OC_HOSTNAME];
			}
		}
		return null;
	}

	public final static boolean isOC(String wpName) {
		return (getOCHostName(wpName.toUpperCase()) != null);
	}

	public final static int getSiteIndex(String site) {
		for (int i = 0; i < OCSites.length; i++) {
			if (site.equalsIgnoreCase(OCSites[i][OC_HOSTNAME])) {
				return i;
			}
		}
		return 0; // don't get a fault
	}

	public final static String getGCWayPoint(String owner) {
		owner = owner + " ";
		int l = owner.lastIndexOf('/');
		if (l > 0) {
			int i = owner.indexOf("GC", l);
			if (i > -1) {
				int j = owner.indexOf(" ", i);
				return owner.substring(i, j);
			}
		}
		return "";
	}
}
