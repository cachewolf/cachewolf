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
package CacheWolf;

import CacheWolf.controls.CWProgressBar;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.utils.MyLocale;

/**
 * A class to perform a search on the cache database.
 * The searchstr is searched for in the waypoint
 * and the name of the cache.
 * A method is also provided to erase the search results.
 */
public class SearchCache {

    CacheDB cacheDB;

    public SearchCache(CacheDB DB) {
	cacheDB = DB;
    }

    /**
     * Method to iterate through the cache database.
     * Each cache, where the search string is found (in waypoint and / or cache name), is flagged as matching.
     * The search only acts on the filtered (=visible) set of caches
     */
    public void search(String searchStr, boolean searchInDescriptionAndNotes, boolean searchInLogs) {
	if (searchStr.length() > 0) {
	    MainForm.profile.selectionChanged = true;
	    searchStr = searchStr.toUpperCase();
	    CacheHolder ch;
	    int counter = 0;
	    if (searchInDescriptionAndNotes || searchInLogs) {
		counter = cacheDB.countVisible();
	    }
	    CWProgressBar cwp = new CWProgressBar(MyLocale.getMsg(219, "Searching..."), 0, counter, searchInDescriptionAndNotes);
	    cwp.exec();
	    cwp.allowExit(true);
	    //Search through complete database
	    //Mark finds by setting is_flaged
	    //TableModel will be responsible for displaying marked caches.
	    CacheHolderDetail chD;
	    for (int i = 0; i < cacheDB.size(); i++) {
		cwp.setPosition(i);
		ch = cacheDB.get(i);
		if (!ch.isVisible())
		    break; // Reached end of visible records
		if (searchInDescriptionAndNotes || searchInLogs) {
		    chD = ch.getDetails();
		} else {
		    chD = null;
		}
		if (ch.getCode().toUpperCase().indexOf(searchStr) < 0 //
			&& ch.getName().toUpperCase().indexOf(searchStr) < 0 //
			&& ch.getStatus().toUpperCase().indexOf(searchStr) < 0 //
			&& (!searchInDescriptionAndNotes //
			|| chD.LongDescription.toUpperCase().indexOf(searchStr) < 0 //
				&& chD.getCacheNotes().toUpperCase().indexOf(searchStr) < 0) //
			&& (!searchInLogs //
			|| chD.CacheLogs.allMessages().toUpperCase().indexOf(searchStr) < 0))//
		    ch.isFlagged = false;
		else
		    ch.isFlagged = true;
		if (cwp.isClosed())
		    break;
	    } // for
	    cwp.exit(0);
	    MainForm.profile.setShowSearchResult(true);
	    MainTab.itself.tablePanel.selectRow(0);
	} // if
    }

    /**
     * Method to remove the flag from all caches in the
     * cache database.
     */
    public void clearSearch() {
	MainForm.profile.selectionChanged = true;
	MainForm.profile.setShowSearchResult(false);
	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    cacheDB.get(i).isFlagged = false;
	}
    }
}
