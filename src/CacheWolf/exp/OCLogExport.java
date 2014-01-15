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
package CacheWolf.exp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.DateFormat;
import CacheWolf.Global;
import CacheWolf.OC;
import CacheWolf.UrlFetcher;
import CacheWolf.database.CacheType;
import CacheWolf.imp.OCGPXfetch;
import CacheWolf.imp.OCLinkImporter;
import CacheWolf.utils.Extractor;
import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.ProgressBarForm;

public final class OCLogExport {
    private static boolean loggedIn = false;
    private static CacheDB cacheDB = null;

    public static void doit() {

	if (cacheDB == null)
	    cacheDB = Global.profile.cacheDB;
	int totalWaypoints = cacheDB.countVisible();
	int updated = 0;
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();

	pbf.showMainTask = false;
	pbf.setTask(h, "logging opencaching ...");
	pbf.exec();
	if (OCGPXfetch.login()) {
	    for (int o = 0; o < cacheDB.size(); o += 1) {
		if (pbf.exitValue == -1)
		    break;
		CacheHolder ch = cacheDB.get(o);
		if (ch.isVisible()) {
		    doOneLog(ch);
		    updated++;
		    h.progress = (float) updated / (float) totalWaypoints;
		    h.changed();
		}
	    }
	}
	pbf.exit(0);
    }

    public static void doOneLog(CacheHolder ch) {
	if (!ch.is_found())
	    return;
	// take GC log direct to OC, needs valid ch
	Vm.showWait(true);
	String wpName = ch.getOcCacheID();
	if (wpName.length() > 1) {
	    if (!loggedIn)
		loggedIn = OCGPXfetch.login();
	    if (loggedIn) {
		if (wpName.charAt(0) < 65) {
		    // noch nicht bei OC gelogged
		    wpName = ch.getOcCacheID().substring(1);
		    String url = "http://" + OC.getOCHostName(wpName) + "/log.php?wp=" + wpName;
		    String page = "";
		    try {
			CacheHolderDetail chD = ch.getCacheDetails(false);
			if (chD.OwnLog != null) {
			    page = UrlFetcher.fetch(url);
			    loggedIn = page.indexOf("Eingeloggt als") > -1; // next time perhaps
			    String ocCacheId = new Extractor(page, "viewcache.php?cacheid=", "\">", 0, true).findNext();
			    String postData = "cacheid=" + ocCacheId + "&version3=1&descMode=3";
			    if (ch.getType() == CacheType.CW_TYPE_EVENT || ch.getType() == CacheType.CW_TYPE_MEGA_EVENT || ch.getType() == CacheType.CW_TYPE_MAZE)
				postData = postData + "&logtype=7";
			    else
				postData = postData + "&logtype=1";
			    Time logDate = DateFormat.toDate(chD.OwnLog.getDate());
			    postData += "&logday=" + logDate.day;
			    postData += "&logmonth=" + logDate.month;
			    postData += "&logyear=" + logDate.year;
			    postData += "&logtext=" + UrlFetcher.toUtf8Url(chD.OwnLog.getMessage());
			    postData += "&submitform=Log+eintragen"; // todo for other opencaching sites
			    UrlFetcher.setpostData(postData);
			    page = UrlFetcher.fetch(url);
			    OCLinkImporter.updateOCLink(ch);
			    if (ch.getOcCacheID().startsWith("-")) {
				ch.setOcCacheID("!" + ch.getOcCacheID().substring(1));
				ch.save();
			    }
			}
		    } catch (IOException e) {
			// dann nicht
		    }
		}
	    }
	}
	Vm.showWait(false);
    }
}
