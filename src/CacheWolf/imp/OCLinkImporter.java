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
package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.OC;
import CacheWolf.UrlFetcher;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.ui.ProgressBarForm;

public class OCLinkImporter {
	private static CacheDB cacheDB = null;

	public static void doIt() {

		if (cacheDB == null)
			cacheDB = Global.getProfile().cacheDB;
		int totalWaypoints = cacheDB.countVisible();
		int updated = 0;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		pbf.showMainTask = false;
		pbf.setTask(h, "Import OC names ...");
		pbf.exec();
		if (OCGPXfetch.login()) {
			for (int o = 0; o < cacheDB.size(); o += 1) {
				h.progress = (float) updated / (float) totalWaypoints;
				h.changed();
				if (pbf.exitValue == -1)
					break;
				CacheHolder ch = cacheDB.get(o);
				if (ch.isVisible()) {
					updateOCLink(ch);
					updated++;
				}
			}
		}
		pbf.exit(0);
	}

	public static void updateOCLink(CacheHolder ch) {
		// todo other OC sites
		Vm.showWait(true);
		boolean save = false;
		String wp = ch.getWayPoint();
		if (wp.startsWith("GC")) {
			String wpName = ch.getOcCacheID();
			if (wpName.length() > 0) {
				if (wpName.charAt(0) < 65)
					wp = wpName.substring(1);
				else {
					if (wpName.startsWith("OC")) // other OC sites
						wp = wpName;
				}
				if (!wp.startsWith("OC")) {
					// other OC sites
					ch.setOcCacheID(""); // there may be a value from gpx - import
					save = true;
				}
			}
			// other OC sites
			String url = "http://" + OC.getOCHostName("OC") + "/map2.php?mode=wpsearch&wp=" + wp;
			try {
				String result = UrlFetcher.fetch(url);
				boolean found = false;
				if (result.substring(result.indexOf("found=\"") + 7).startsWith("1"))
					found = true;
				int start = result.indexOf("wpoc=\"") + 6;
				if (start > 5) {
					int idend = result.indexOf("\"", start);
					String ocwp = result.substring(start, idend);
					if (!found)
						ocwp = "-" + ocwp;
					if (!ocwp.equals(ch.getOcCacheID())) {
						ch.setOcCacheID(ocwp);
						save = true;
					}
				} else {
					// check over coordinates
					// still looking for the best method
				}
				if (save)
					ch.save();

			} catch (Exception e) {
				// dann halt nicht
			}
		}
		// return ch;
		Vm.showWait(false);
	}

}
