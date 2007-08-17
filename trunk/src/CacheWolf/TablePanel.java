package CacheWolf;

import ewe.ui.*;
import ewe.util.*;
import ewe.sys.*;
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
	 * When the cacheDB is reorganized (by sort/filter/search), the selected cache
	 * may end up at a new index.
	 */
	int selectedIdx=0;
	CacheHolder selectedCh;
	
	public TablePanel(Preferences p, Profile profileXX, StatusBar statBar){
		pref = Global.getPref();
		Profile profile=Global.getProfile();
		this.statBar = statBar;
		cacheDB = profile.cacheDB;
		addLast(new ScrollBarPanel(tc = new myTableControl(this)));
		if (statBar!=null) addLast(statBar,CellConstants.DONTSTRETCH, CellConstants.FILL);
		myMod = new myTableModel(tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
	}
	
	public void setSelectedCache(int row){ 
	/*	selectedCh=null;
		selectedIdx=-1;
		if (row>=0 && row<cacheDB.size())  {
			selectedCh=(CacheHolder) cacheDB.get(row);
			selectedIdx=row;
		} 		
	*/
	}
	
	/** Mark the row as selected so that myTableModel can color it grey */
	public void selectRow(int row) {
		setSelectedCache(row);
//		tc.addToSelection(row,0); 
//		tc.addToSelection(row,myMod.MAXCOLUMNS-1);
		// Ensure that the highlighted row is visible (e.g. when coming from radar panel)
		tc.scrollToVisible(row,0);
		// Next line needed for key scrolling 
		tc.cursorTo(row, tc.cursor.x+tc.listMode, true);
// <<<<<<< .mine
		//tc.scrollToVisible(row,0);
		//tc.clearSelection(null);
		//tc.addToSelection(row,0); 
		//tc.addToSelection(row,myMod.MAXCOLUMNS-1); 
		//tc.scrollToVisible(row,0);
		/*tc.clearSelection(null);
		tc.addToSelection(row,0); 
		tc.addToSelection(row,myMod.MAXCOLUMNS-1);*/ 
		//tc.paintSelectedCells();
//=======
//>>>>>>> .r820
	}
	
	/** Highlight the first row in grey. It can be unhighlighted by clicking */
	public void selectFirstRow() {
		myMod.cursorSize=new Dimension(-1,1);
		if (cacheDB.size()>0) {
			tc.cursorTo(0, -1, true);
//			tc.addToSelection(0,0); 
//			tc.addToSelection(0,myMod.MAXCOLUMNS-1);
		}
	}
	
	/** Returns the index of the currently selected cache or -1 of the cache is no longer visible
	 * due to a sort/filter or search operation
	 * @return index of selected cache (-1 if not visible)
	 */
	public int getSelectedCache(){
		return tc.cursor.y;
		/*
		// If cacheDB is empty return -1, cannot select a cache
		if (cacheDB.size()==0) return -1;
		// If cacheDB has entries, but all are filtered, return -1
		if (((CacheHolder)cacheDB.get(0)).is_filtered) return -1;
		// Now we have at least one visible cache
		// Check whether selected cache was deleted or filtered 
		if (selectedIdx>=Global.mainTab.tbP.myMod.numRows) return Global.mainTab.tbP.myMod.numRows-1;
		// We had a previously selected cache, check whether it is now filtered
		if (selectedCh==null || selectedCh.is_filtered) return 0; // Return first visible cache
		// Check whether the order of the list has changed because of sort/filter/search operations
		if (cacheDB.get(selectedIdx)==selectedCh) return selectedIdx;
		// The position has changed, return the new position
		return cacheDB.find(selectedCh); */
	}
	
	public void saveColWidth(Preferences pref){
		int j=0;
		for (int i = 0; i<myMod.MAXCOLUMNS; i++){
			if(pref.tablePrefs[i] == 1){
				pref.tableWidth[i] = myMod.getColWidth(j++);
			}
		}
		pref.savePreferences();
	}
	
	/*
	public void selectAndActive(int rownum){
		//		tc.scrollToVisible(rownum, 0);
		selectRow(rownum);  // color it in grey
	}
	*/
	
	public void resetModel() {
		setSelectedCache(0);
		myMod.numRows = cacheDB.size();
		Global.getProfile().updateBearingDistance();
		Filter flt = new Filter();
		flt.setFilter();
		flt.doFilter();
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
	
	/** Move all filtered caches to the end of the table and redesplay table */
	//TODO Add a sort here to restore the sort after a filter
	public void refreshTable(){
		myMod.updateRows();
		// Check whether the currently selected cache is still visible
		selectRow(getSelectedCache());
		tc.update(true); // Update and repaint
		if (statBar!=null) statBar.updateDisplay();
	}
	
}
