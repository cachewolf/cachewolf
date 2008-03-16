package CacheWolf;

import ewe.ui.InputBox;

public class TravelbugPickup {

	/**
	 * Choose a travelbug from those listed in the travelbug list and delete it, if
	 * the operation was not cancelled.
	 * @param tbl List of travelbugs from where a bug is picked up
	 */
	public static Travelbug pickupTravelbug(TravelbugList tbl) {
		Travelbug tb=null;
		TravelbugScreen tbs=new TravelbugScreen(tbl,MyLocale.getMsg(6016,"Pick up travelbug"),true);
		tbs.execute(); // Select TB to pick up
		if (tbs.selectedItem>=0) { // Was a TB selected ?
			// If the returned item is bigger than number of bugs in cache
			// we have found a new unlisted bug. 
			if (tbs.selectedItem==tbl.size()) {
				InputBox ibox=new InputBox(MyLocale.getMsg(6018,"Travelbug name"));
				String name=ibox.input("",240);
				if (name==null) return null; // No name given
				tb=new Travelbug(name);
			} else { // A bug in the list was chosen
				tb=tbl.getTB(tbs.selectedItem);
				// Remove the tb from the list
				tbl.remove(tbs.selectedItem);
			}
			InputBox ibox=new InputBox(MyLocale.getMsg(6019,"Tracking number"));
			String trackingNo=ibox.input("",240);
			if (trackingNo==null) trackingNo="";
			tb.setTrackingNo(trackingNo);
		}
		return tb;
	}
}
