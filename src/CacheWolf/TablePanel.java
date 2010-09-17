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

import ewe.fx.Dimension;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.util.Vector;

/**
*	Class to display the cache database in a table.
*	Class ID = 1000
*/
public class TablePanel extends CellPanel{
	
	myTableControl tc;
	myTableModel myMod;
	Preferences pref;
	Profile profile;
	CacheDB cacheDB;
	MainTab myMaintab;
	StatusBar statBar;
	/** We keep track of the currently selected cache in two variables(for speed)
	 * selectedIdx is the index in cacheDB, selectedch is the actual cache
	 * selectedIdx=-1 if no caches are visible (i.e. database empty or filtered). In 
	 * this case selectedch is "null".
	 * Otherwise selectedIdx points to a visible cache.
	 * When the cacheDB is reorganised (by sort/filter/search), the selected cache
	 * may end up at a new index.
	 */
	int selectedIdx=0;
	CacheHolder selectedCh;
	
	public TablePanel(Preferences p, Profile prof, StatusBar statBar){
		pref = p; //Global.getPref();
		profile=prof; //Global.getProfile();
		this.statBar = statBar;
		cacheDB = profile.cacheDB;
		addLast(new MyScrollBarPanel(tc = new myTableControl(this)));
		if (statBar!=null) addLast(statBar,CellConstants.DONTSTRETCH, CellConstants.FILL);
		myMod = new myTableModel(tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
		if (statBar!=null) statBar.setMyTableModel(myMod);
		pref.setMyTableModel(myMod);
	}
	
	/** Mark the row as selected so that myTableModel can color it grey */
	public void selectRow(int row) {
		// Ensure that the highlighted row is visible (e.g. when coming from radar panel)
		// Next line needed for key scrolling 
		tc.cursorTo(row, 0, true); //tc.cursor.x+tc.listMode
	}
	
	/** Highlight the first row in grey. It can be unhighlighted by clicking */
	public void selectFirstRow() {
		myMod.cursorSize=new Dimension(-1,1);
		if (cacheDB.size()>0) {
			tc.cursorTo(0, 0, true);
		}
	}
	
	/** Returns the index of the currently selected cache or 0 if the cache is no longer visible
	 * due to a sort/filter or search operation
	 * -1 if no cache is visible
	 * @return index of selected cache (0 if not visible, -1 if no cache is visible)
	 */
	public int getSelectedCache(){
		if ( myMod.numRows < 1 )
			return -1;
		// If the selected Cache is no longer visible (e.g. after applying a filter)
		// select the first row
		if (tc.cursor.y>=myMod.numRows)
			return 0;
		return tc.cursor.y;
	}
	
	public void saveColWidth(Preferences pPref){
		String colWidths=myMod.getColWidths();
		if (!colWidths.equals(pPref.listColWidth)) {
			pPref.listColWidth=colWidths;
			pPref.savePreferences();
		}
	}
	
	public void resetModel() {
		myMod.numRows = cacheDB.size();
		profile.updateBearingDistance(); //Global.getProfile().updateBearingDistance();
		tc.scrollToVisible(0,0);
		refreshTable();
	}
	
	/**
	 * Similar to refreshTable but not so "heavy".
	 * Is used when user changes settings in preferences.
	 */
	public void refreshControl(){
		tc.update(true);
		if (statBar!=null) statBar.updateDisplay("");
	}
	
	/** Move all filtered caches to the end of the table and redisplay table */
	//TODO Add a sort here to restore the sort after a filter
	public void refreshTable(){
		
		// First: Remember currently selected waypoint
		String wayPoint;
		Vector oldVisibleCaches = null;
		int sel = getSelectedCache();
		if ((sel >= 0) && (sel < cacheDB.size()) ) // sel > cacheDB.size() can happen if you load a new profile, which is smaller than the old profile and you selected one cache that exceeds the number of caches in the new profile  
			wayPoint = cacheDB.get(sel).getWayPoint();
		else wayPoint = null;
		// Then: remember all caches that are visible before the refresh
		if (wayPoint != null) {
	        oldVisibleCaches = new Vector(sel);
	        for (int i = 0; i < sel; i++) {
		        oldVisibleCaches.add(cacheDB.get(i));
	        }
        }
		myMod.updateRows();

		// Check whether the currently selected cache is still visible
		int rownum = 0;
		if (wayPoint != null) {
			rownum = profile.cacheDB.getIndex(wayPoint); //Global.getProfile().cacheDB.getIndex(wayPoint);
			// If it is not visible: Go backward in the list of the 
			// previously visible caches and look if you find
			// any cache that is now still visible.
			if ( (rownum < 0) || (rownum>=myMod.numRows) ) {
				if (oldVisibleCaches!=null) {
	                int i;
	                for (i = sel - 1; i >= 0; i--) {
		                CacheHolder checkCache = (CacheHolder) oldVisibleCaches.get(i);
		                rownum = profile.cacheDB.getIndex(checkCache.getWayPoint()); //Global.getProfile().cacheDB.getIndex(checkCache.getWayPoint());
		                if ((rownum >= 0) && (rownum < myMod.numRows))
			                break;
		                rownum = 0;
	                }
                }
			}	
		}
		selectRow(rownum);

		tc.update(true); // Update and repaint
		if (statBar!=null) statBar.updateDisplay("");
	}
	
}
