package CacheWolf.model;

import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.TravelbugJourney;
import CacheWolf.TravelbugJourneyList;
import ewe.util.Vector;

public class TravelBugJourneyScreenModel {

	public CacheHolder actualCache;
	public TravelbugJourneyList allTravelbugJourneys;
	public Vector shownTravelbugJourneys = new Vector ();
	public boolean onlyLogged;
	public int sortCriteria;

	public void toggleOnlyLogged() {
		onlyLogged = !onlyLogged;
		createShowSet();
		Global.getPref().travelbugShowOnlyNonLogged = onlyLogged;
		Global.getPref().savePreferences();
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
