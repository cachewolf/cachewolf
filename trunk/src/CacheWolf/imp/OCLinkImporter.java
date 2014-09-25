/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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

import CacheWolf.MainForm;
import CacheWolf.OC;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.UrlFetcher;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.ui.ProgressBarForm;

public final class OCLinkImporter {
    private static CacheDB cacheDB = null;

    public static void doIt() {

	if (cacheDB == null)
	    cacheDB = MainForm.profile.cacheDB;
	int totalWaypoints = cacheDB.countVisible();
	int updated = 0;
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();

	pbf.showMainTask = false;
	pbf.setTask(h, "Import OC names ...");
	pbf.exec();
	if (OCGPXfetch.login()) {
	    for (int o = 0; o < cacheDB.size(); o += 1) {
		if (pbf.exitValue == -1)
		    break;
		CacheHolder ch = cacheDB.get(o);
		if (ch.isVisible()) {
		    updateOCLink(ch);
		    updated++;
		    h.progress = (float) updated / (float) totalWaypoints;
		    h.changed();
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
	    String baseurl = "http://" + OC.getOCHostName("OC") + "/map2.php?";
	    boolean hasOC = false;
	    try {
		String url = baseurl + "mode=wpsearch&wp=" + wp;
		String result = UrlFetcher.fetch(url);
		if (result.indexOf("wpoc=\"") > -1)
		    hasOC = true;
		else {
		    // check over coordinates
		    // getting a cache next to the coordinates
		    String nLat = ch.getPos().getLatDeg(TransformCoordinates.DD);
		    String nLon = ch.getPos().getLonDeg(TransformCoordinates.DD);
		    url = baseurl + "mode=locate&lat=" + nLat + "&lon=" + nLon;
		    result = SafeXML.html2iso8859s1(UrlFetcher.fetch(url));
		    String ocCacheName = new Extractor(result, "name=\"", "\"", 0, true).findNext();
		    if (ch.getCacheName().equals(ocCacheName)) {
			hasOC = true;
		    } else {
			int start = result.indexOf("coords=\"") + 8;
			int lonend = result.indexOf(",", start);
			int latend = result.indexOf("\"", lonend);
			double lon = Common.parseDouble(result.substring(start, lonend));
			double lat = Common.parseDouble(result.substring(lonend + 1, latend));
			boolean sameCoord = lon == ch.getPos().lonDec && lat == ch.getPos().latDec;
			if (sameCoord) {
			    start = result.indexOf("username=\"") + 10;
			    int end = result.indexOf("\"", start);
			    if (ch.getCacheOwner().toLowerCase().equals(result.substring(start, end).toLowerCase()))
				hasOC = true;
			}
		    }
		}
		if (hasOC) {
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
		    }
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
