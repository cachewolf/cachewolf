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
import CacheWolf.UrlFetcher;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;

public class OCLinkImporter {
	private CacheDB cacheDB;

	public OCLinkImporter() {
		this.cacheDB = Global.getProfile().cacheDB;
	}

	public void doIt() {
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		pbf.showMainTask = false;
		pbf.setTask(h, "Import OC names ...");
		pbf.exec();
		OCGPXfetch.login();
		for (int o = 0; o < cacheDB.size(); o += 1) {
			CacheHolder ch = cacheDB.get(o);
			String wp = ch.getWayPoint();
			if (wp.startsWith("GC")) {
				String url = "http://www.opencaching.de/map2.php?mode=wpsearch&wp=" + wp;
				h.progress = (float) o / (float) (cacheDB.size() - 1);
				h.changed();
				try {
					String result = UrlFetcher.fetch(url);
					boolean found = false;
					int start = result.indexOf("found=\"") + 7;
					if (result.substring(start).startsWith("1"))
						found = true;
					start = result.indexOf("wpoc=\"") + 6;
					if (start > 5) {
						int idend = result.indexOf("\"", start);
						String ocwp = result.substring(start, idend);
						if (!found)
							ocwp = "-" + ocwp;
						ch.setOcCacheID(ocwp);
					}
				} catch (Exception e) {
					// dann halt nicht
				}
			}
		}
		pbf.exit(0);
	}

}
