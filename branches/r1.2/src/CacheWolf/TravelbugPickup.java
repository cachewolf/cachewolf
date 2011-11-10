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

import CacheWolf.view.ITravelbugScreen;
import CacheWolf.view.TravelBugScreenFactory;
import ewe.ui.InputBox;

public class TravelbugPickup {

	/**
	 * Choose a travelbug from those listed in the travelbug list and delete it,
	 * if the operation was not cancelled.
	 * 
	 * @param tbl
	 *            List of travelbugs from where a bug is picked up
	 */
	public static Travelbug pickupTravelbug(TravelbugList tbl) {
		Travelbug tb = null;
		// TravelbugScreen tbs=new TravelbugScreen(tbl,MyLocale.getMsg(6016,"Pick up travelbug"),true);
		ITravelbugScreen tbs = TravelBugScreenFactory.createTravelbugScreen(tbl, MyLocale.getMsg(6016, "Pick up travelbug"), new Boolean(true));
		tbs.execute(); // Select TB to pick up
		if (tbs.getSelectedItem() >= 0) { // Was a TB selected ?
			// If the returned item is bigger than number of bugs in cache
			// we have found a new unlisted bug.
			if (tbs.getSelectedItem() == tbl.size()) {
				InputBox ibox = new InputBox(MyLocale.getMsg(6018, "Travelbug name"));
				String name = ibox.input("", 240);
				if (name == null)
					return null; // No name given
				tb = new Travelbug(name);
			} else { // A bug in the list was chosen
				tb = tbl.getTB(tbs.getSelectedItem());
				// Remove the tb from the list
				tbl.remove(tbs.getSelectedItem());
			}
			InputBox ibox = new InputBox(MyLocale.getMsg(6019, "Tracking number"));
			String trackingNo = ibox.input("", 240);
			if (trackingNo == null)
				trackingNo = "";
			tb.setTrackingNo(trackingNo);
		}
		return tb;
	}
}
