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
package CacheWolf.view.pda;

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.TravelbugPickup;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugJourney;
import CacheWolf.database.TravelbugList;
import CacheWolf.view.ewe.TravelbugJourneyScreen;
import ewe.fx.Dimension;

public class PDATravelbugMenuPanel extends PDAMenu {

    private static final String RETRIEVE = "retrieve";
    private static final String TOGGLE_LOG = "toggle_log";
    private static final String NEW_TB = "new_tb";
    private static final String EXPERT = "expert_view";
    private static final String SORT = "sort";
    private static final String EXIT = "exit";
    private PDATravelbugJourneyScreen view;

    public PDATravelbugMenuPanel(PDATravelbugJourneyScreen view) {
	this.view = view;
	setTitle(MyLocale.getMsg(6053, "Travelbug - Menu"));

	addMenuItem(view.model.onlyLogged ? MyLocale.getMsg(6054, "Show all") : MyLocale.getMsg(6046, "Show only not logged"), TOGGLE_LOG);
	addMenuItem(MyLocale.getMsg(6055, "Sort ..."), SORT);
	addMenuItem(MyLocale.getMsg(6042, "New Travelbug"), NEW_TB);
	addMenuItem(MyLocale.getMsg(6040, "Pick up TB from current cache"), RETRIEVE);
	addMenuItem(MyLocale.getMsg(6056, "Expertview"), EXPERT);
	addMenuItem(MyLocale.getMsg(6061, "Close"), EXIT);
	buildMenu();
    }

    public void actionPerformed(String actionCommand) {
	if (actionCommand.equals(RETRIEVE)) {
	    int curCacheNo = Global.mainTab.tablePanel.getSelectedCache();
	    CacheDB cacheDB = Global.profile.cacheDB;
	    if (curCacheNo >= 0 && curCacheNo < cacheDB.size()) {
		CacheHolder ch = cacheDB.get(curCacheNo);
		String waypoint = ch.getWayPoint();
		TravelbugList tblSrcCache = ch.getCacheDetails(true).Travelbugs;

		Travelbug tb = TravelbugPickup.pickupTravelbug(tblSrcCache);
		if (tb != null) {
		    view.model.allTravelbugJourneys.addTbPickup(tb, Global.profile.name, waypoint);
		    CacheHolderDetail cacheDetails = ch.getCacheDetails(true);
		    ch.setHas_bugs(cacheDetails.Travelbugs.size() > 0);
		    ch.save();
		    view.model.allTravelbugJourneys.saveTravelbugsFile();
		}
	    }
	    view.createShowSet();
	    view.setupTBButtons();
	    exit(0);
	} else if (actionCommand.equals(TOGGLE_LOG)) {
	    view.toggleOnlyLogged();
	    exit(0);
	} else if (actionCommand.equals(EXPERT)) {
	    TravelbugJourneyScreen travelbugJourneyScreen = new TravelbugJourneyScreen(view.model);
	    Dimension arg0 = new Dimension();
	    getSize(arg0);
	    travelbugJourneyScreen.setPreferredSize(arg0.width, arg0.height);
	    travelbugJourneyScreen.execute();
	    exit(0);
	    view.exit(0);
	} else if (actionCommand.equals(NEW_TB)) {
	    int curCacheNo = Global.mainTab.tablePanel.getSelectedCache();
	    CacheDB cacheDB = Global.profile.cacheDB;
	    CacheHolder ch = cacheDB.get(curCacheNo);
	    TravelbugJourney tbj = new TravelbugJourney("New");
	    tbj.setFromProfile(Global.profile.name);
	    tbj.setFromWaypoint("");
	    tbj.setFromLogged(true);
	    view.model.allTravelbugJourneys.add(tbj);
	    CacheHolderDetail cacheDetails = ch.getCacheDetails(true);
	    ch.setHas_bugs(cacheDetails.Travelbugs.size() > 0);
	    ch.save();
	    view.model.allTravelbugJourneys.saveTravelbugsFile();
	    view.createShowSet();
	    view.setupTBButtons();
	    exit(0);
	} else if (actionCommand.equals(SORT)) {
	    PDATravelbugSortMenu sortMenu = new PDATravelbugSortMenu();
	    sortMenu.execute();
	    if (sortMenu.sortColumn > 0) {
		view.model.allTravelbugJourneys.sort(sortMenu.sortColumn, sortMenu.ascending);
		view.createShowSet();
		view.setupTBButtons();
	    }
	    exit(0);
	} else if (actionCommand.equals(EXIT)) {
	    exit(1);
	}
    }

}
