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

import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.utils.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugJourney;
import CacheWolf.imp.GCImporter;
import CacheWolf.utils.CWWrapper;
import ewe.sys.Vm;

public class PDATravelbugDetailMenu extends PDAMenu {

    private static final String BROWSER = "browser";

    private static final String SPIDER = "spider";

    private static final String DELETE = "delete";

    private static final String EXIT = "exit";

    private static final String DROP = "drop";

    private PDATravelbugDetailPanel view;

    private PDATravelbugJourneyScreen journeyScreen;

    public PDATravelbugDetailMenu(PDATravelbugDetailPanel view, PDATravelbugJourneyScreen journeyScreen) {
	this.view = view;
	this.journeyScreen = journeyScreen;

	setTitle("TravelBugDetail - Menu");

	addMenuItem(MyLocale.getMsg(6041, "Drop TB in cache"), DROP);
	addMenuItem(MyLocale.getMsg(6044, "Get mission (and name)"), SPIDER);
	addMenuItem(MyLocale.getMsg(6045, "Open on-line"), BROWSER);
	addMenuItem(MyLocale.getMsg(6043, "Delete Travelbug"), DELETE);
	addMenuItem(MyLocale.getMsg(6061, "Close"), EXIT);

	buildMenu();

    }

    public void actionPerformed(String action) {
	if (action.equals(DROP)) {
	    Travelbug tb = view.getTravelbug().getTb();
	    int curCacheNo = MainTab.itself.tablePanel.getSelectedCache();
	    CacheDB cacheDB = MainForm.profile.cacheDB;
	    if (curCacheNo >= 0 && curCacheNo < cacheDB.size()) {
		CacheHolder ch = cacheDB.get(curCacheNo);
		CacheHolderDetail cacheDetails = ch.getCacheDetails(true);
		cacheDetails.Travelbugs.add(tb);
		journeyScreen.model.allTravelbugJourneys.addTbDrop(tb, MainForm.profile.name, ch.getWayPoint());
		ch.setHas_bugs(true);
		ch.save();
		// Set Input fields to the new Values:
		view.getInpToWaypoint().setText(view.getTravelbug().getToWaypoint());
		view.getInpToProfile().setText(view.getTravelbug().getToProfile());
		view.getInpToDate().setText(view.getTravelbug().getToDate());
		// Save now. The action won't recognize the changes:
		journeyScreen.model.allTravelbugJourneys.saveTravelbugsFile();
	    }
	    journeyScreen.setupTBButtons();
	    exit(0);
	} else if (action.equals(BROWSER)) {
	    GCImporter spider = new GCImporter();
	    Vm.showWait(true);
	    // First check whether ID is set, if not get it
	    Travelbug tb = view.getTravelbug().getTb();
	    if (tb.getGuid().length() == 0) {
		tb.setGuid(spider.getBugId(tb.getName()));
	    }
	    if (tb.getGuid().length() != 0) {
		Vm.showWait(false);
		try {
		    String s;
		    if (tb.getGuid().length() > 10)
			s = "http://www.geocaching.com/track/details.aspx?guid=" + tb.getGuid();
		    else
			s = "http://www.geocaching.com/track/details.aspx?id=" + tb.getGuid();

		    CWWrapper.exec(Preferences.itself().browser, s);
		    Preferences.itself().log("Executed: \"" + Preferences.itself().browser + "\" \"" + s + "\"");
		} catch (Exception ioex) {
		    Preferences.itself().log("Ignored Exception", ioex, true);
		}
	    }
	    Preferences.itself().setOldGCLanguage();
	    exit(0);
	} else if (action.equals(SPIDER)) {
	    Travelbug tb = view.getTravelbug().getTb();
	    GCImporter spider = new GCImporter();
	    Vm.showWait(true);

	    // if we have an ID, get mission by ID
	    if (tb.getGuid().length() != 0) {
		tb.setMission(spider.getBugMissionByGuid(tb.getGuid()));
	    } else {
		// try to get mission and name by tracking number
		boolean suceeded = false;
		if (tb.getTrackingNo().length() != 0) {
		    suceeded = spider.getBugMissionAndNameByTrackNr(tb);
		}
		// if this has't worked, try to get ID by name
		if (!suceeded) {
		    tb.setGuid(spider.getBugId(tb.getName().trim()));
		    // if we have an ID now, get mission by ID
		    if (tb.getGuid().length() != 0) {
			tb.setMission(spider.getBugMissionByGuid(tb.getGuid()));
		    }
		}
	    }
	    journeyScreen.model.allTravelbugJourneys.saveTravelbugsFile();
	    Vm.showWait(false);
	    Preferences.itself().setOldGCLanguage();
	    exit(0);
	} else if (action.equals(DELETE)) {
	    // LOESCHEN DES TB's aus der Datenbank ist Boese!!!
	    // Erst mal eine Sicherheitesabfrage bauen:
	    int r = PDAOptionPane.showConfirmDialog(this.getFrame(), "Sind Sie Sicher???", "Wollen Sie wirklich den TB löschen??");
	    if (r == PDAOptionPane.OK) {
		for (int i = 0; i < journeyScreen.model.allTravelbugJourneys.size(); i++) {
		    TravelbugJourney tbJourney = journeyScreen.model.allTravelbugJourneys.getTBJourney(i);
		    if (tbJourney.getTb().getTrackingNo().equals(view.getTravelbug().getTb().getTrackingNo())) {
			journeyScreen.model.allTravelbugJourneys.remove(i);
			journeyScreen.model.allTravelbugJourneys.saveTravelbugsFile();
			journeyScreen.setupTBButtons();
			break;
		    }
		}
	    }
	} else if (action.equals(EXIT)) {
	    boolean changed = false;
	    if (!view.getInpName().text.equals(view.getTravelbug().getTb().getName())) {
		view.getTravelbug().getTb().setName(view.getInpName().text);
		changed = true;
	    }
	    if (!view.getInpTrackingNo().text.equals(view.getTravelbug().getTb().getTrackingNo())) {
		view.getTravelbug().getTb().setTrackingNo(view.getInpTrackingNo().text);
		changed = true;
	    }

	    if (!view.getInpFromProfile().text.equals(view.getTravelbug().getFromProfile())) {
		view.getTravelbug().setFromProfile(view.getInpFromProfile().text);
		changed = true;
	    }
	    if (!view.getInpFromWaypoint().text.equals(view.getTravelbug().getFromWaypoint())) {
		view.getTravelbug().setFromWaypoint(view.getInpFromWaypoint().text);
		changed = true;
	    }
	    if (!view.getInpFromDate().text.equals(view.getTravelbug().getFromDate())) {
		view.getTravelbug().setFromDate(view.getInpFromDate().text);
		changed = true;
	    }
	    if (view.getTravelbug().getFromLogged() != view.getChkFromLogged().state) {
		view.getTravelbug().setFromLogged(view.getChkFromLogged().state);
		changed = true;
	    }

	    if (!view.getInpToProfile().text.equals(view.getTravelbug().getToProfile())) {
		view.getTravelbug().setToProfile(view.getInpToProfile().text);
		changed = true;
	    }
	    if (!view.getInpToWaypoint().text.equals(view.getTravelbug().getToWaypoint())) {
		view.getTravelbug().setToWaypoint(view.getInpToWaypoint().text);
		changed = true;
	    }
	    if (!view.getInpToDate().text.equals(view.getTravelbug().getToDate())) {
		view.getTravelbug().setToDate(view.getInpToDate().text);
		changed = true;
	    }
	    if (view.getTravelbug().getToLogged() != view.getChkToLogged().state) {
		view.getTravelbug().setToLogged(view.getChkToLogged().state);
		changed = true;
	    }

	    if (changed) {
		journeyScreen.model.allTravelbugJourneys.saveTravelbugsFile();
		journeyScreen.createShowSet();
	    }

	    exit(1);
	}
    }

}
