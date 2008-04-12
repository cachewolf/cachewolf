package CacheWolf;

import ewe.ui.*;
import ewe.util.*;
import ewe.fx.*;

/**
*	Class to display the cache database in a table.
*	Class ID = 1000
*/
public class TablePanel extends CellPanel{
	
	myTableControl tc;
	myTableModel myMod;
	Preferences pref;
	Vector cacheDB;
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
	
	public TablePanel(Preferences p, Profile profileXX, StatusBar statBar){
		pref = Global.getPref();
		Profile profile=Global.getProfile();
		this.statBar = statBar;
		cacheDB = profile.cacheDB;
		addLast(new MyScrollBarPanel(tc = new myTableControl(this)));
		if (statBar!=null) addLast(statBar,CellConstants.DONTSTRETCH, CellConstants.FILL);
		myMod = new myTableModel(tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
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
	
	/** Returns the index of the currently selected cache or -1 of the cache is no longer visible
	 * due to a sort/filter or search operation
	 * @return index of selected cache (-1 if not visible)
	 */
	public int getSelectedCache(){
		// If the selected Cache is no longer visible (e.g. after applying a filter)
		// select the last row
		if (tc.cursor.y>=myMod.numRows)
			return myMod.numRows-1;
		return tc.cursor.y;
	}
	
	public void saveColWidth(Preferences pref){
		String colWidths=myMod.getColWidths();
		if (!colWidths.equals(pref.listColWidth)) {
			pref.listColWidth=colWidths;
			pref.savePreferences();
		}
	}
	
	public void resetModel() {
		myMod.numRows = cacheDB.size();
		Global.getProfile().updateBearingDistance();
		Global.getProfile().restoreFilter(); // Restore the isActive & isInverted status of the filter
		tc.scrollToVisible(0,0);
		refreshTable();
	}
	
	/**
	 * Similar to refreshTable but not so "heavy".
	 * Is used when user changes settings in preferences.
	 */
	public void refreshControl(){
		tc.update(true);
	}
	
	/** Move all filtered caches to the end of the table and redisplay table */
	//TODO Add a sort here to restore the sort after a filter
	public void refreshTable(){
		String wayPoint;
		if (getSelectedCache() >= 0)
			wayPoint = ((CacheHolder)cacheDB.get(getSelectedCache())).wayPoint;
		else wayPoint = null;
		myMod.updateRows();

		// Check whether the currently selected cache is still visible
		int rownum = 0;
		if (wayPoint != null) {
			rownum = Global.getProfile().getCacheIndex(wayPoint);
			if ( (rownum < 0) || (rownum>=myMod.numRows) )
				rownum = 0;	
		}
		selectRow(rownum);

		tc.update(true); // Update and repaint
		if (statBar!=null) statBar.updateDisplay();
	}
	
}
