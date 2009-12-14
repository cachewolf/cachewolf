package CacheWolf.imp;

import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.utils.CWWrapper;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;

/*
 * get rating for a cache from an external tool
 */
public class Rating {

	String rater;

	public Rating() {
		rater = Global.getPref().rater;
	}

	/**
	 * call the tool defined by Global.getPref().rater with a visible waypoint
	 * as parameter wait for tool to finish, catch exit code and write it to
	 * CacheHolder.numRecommended
	 */
	public void run() {
		if (null == rater)
			return;

		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		int totalWaypoints = Global.getProfile().cacheDB.countVisible();
		int countWaypoints = 0;

		pbf.showMainTask = false;
		pbf.setTask(h, "Rating ...");
		pbf.exec();

		for (int i = 0; i < Global.getProfile().cacheDB.size(); i++) {
			CacheHolder ch = Global.getProfile().cacheDB.get(i);
			if (ch.isVisible()) {
				if (ch.isCacheWpt()) {
					int rate;
					try {
						rate = CWWrapper.exec(rater, ch.getWayPoint(), true, true);
						ch.setNumRecommended(rate);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				countWaypoints++;
				h.progress = (float) countWaypoints / (float) totalWaypoints;
				h.changed();
			}
		}
		pbf.exit(0);
		Global.mainForm.repaintNow();
	}

}
