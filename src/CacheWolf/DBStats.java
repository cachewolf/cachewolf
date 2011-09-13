/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

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

/**
 * @author Marc
 *         Use this class to obtain statistics or information on a cache database.
 */
public class DBStats {
	CacheDB cacheDB = null;

	public DBStats(CacheDB db) {
		cacheDB = db;
	}

	/**
	 * Method to get the number of caches displayed in the list.
	 * It will count waypoints only that start with
	 * GC,or
	 * OC
	 * 
	 * @return
	 */
	public String visible(boolean big) {
		CacheHolder holder;
		int counter = 0;
		int whiteCaches = 0;
		int whiteWaypoints = 0;
		for (int i = 0; i < cacheDB.size(); i++) {
			holder = cacheDB.get(i);
			if (holder.isVisible()) {
				counter++;
				if (CacheType.isAddiWpt(holder.getType())) {
					whiteWaypoints++;
				} else {
					whiteCaches++;
				}
			}
		}
		if (big)
			return counter + "(" + whiteCaches + "/" + whiteWaypoints + ")";
		else
			return "" + whiteCaches;

	}

	/**
	 * Method to get the number of caches available for display
	 * 
	 * @return
	 */
	public String total(boolean big) {
		CacheHolder holder;
		int all = cacheDB.size();
		int whiteCaches = 0;
		int whiteWaypoints = 0;
		int blackCaches = 0;
		int blackWaypoints = 0;
		for (int i = 0; i < all; i++) {
			holder = cacheDB.get(i);
			if (holder.is_black()) {
				if (CacheType.isAddiWpt(holder.getType())) {
					blackWaypoints++;
				} else {
					blackCaches++;
				}
			} else {
				if (CacheType.isAddiWpt(holder.getType())) {
					whiteWaypoints++;
				} else {
					whiteCaches++;
				}
			}
		}
		if (big) {
			if (blackCaches > 0 || blackWaypoints > 0) {
				return all + "(" + whiteCaches + "/" + whiteWaypoints + "+" + blackCaches + "/" + blackWaypoints + ")";
			} else {
				return all + "(" + whiteCaches + "/" + whiteWaypoints + ")";
			}
		} else
			return "" + whiteCaches;
	}

	public int totalFound() {
		CacheHolder holder;
		int counter = 0;
		for (int i = 0; i < cacheDB.size(); i++) {
			holder = cacheDB.get(i);
			if (holder.is_found() == true) {
				if (holder.isCacheWpt())
					counter++;
			}
		}
		return counter;
	}
}