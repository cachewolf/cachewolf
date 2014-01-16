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
package CacheWolf.model;

import CacheWolf.Global;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.TravelbugJourney;
import CacheWolf.database.TravelbugJourneyList;
import ewe.util.Vector;

public class TravelBugJourneyScreenModel {

    public CacheHolder actualCache;
    public TravelbugJourneyList allTravelbugJourneys;
    public Vector shownTravelbugJourneys = new Vector();
    public boolean onlyLogged;
    public int sortCriteria;

    public void toggleOnlyLogged() {
	onlyLogged = !onlyLogged;
	createShowSet();
	Global.pref.travelbugShowOnlyNonLogged = onlyLogged;
	Global.pref.savePreferences();
    }

    public void createShowSet() {
	shownTravelbugJourneys.clear();
	for (int i = 0; i < allTravelbugJourneys.size(); i++) {
	    TravelbugJourney tbJourney = allTravelbugJourneys.getTBJourney(i);
	    if (!onlyLogged || (onlyLogged && (!tbJourney.getFromLogged() || !tbJourney.getToLogged()))) {
		shownTravelbugJourneys.add(tbJourney);
	    }
	}
    }
}
