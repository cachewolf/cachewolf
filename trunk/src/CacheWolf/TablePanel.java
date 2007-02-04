package CacheWolf;

import ewe.ui.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	Class to display the cache database in a table.
*	Class ID = 1000
*   Changes:
*     20061124 salzkammergut: Bugfix 9529, Conversion to Mylocale
*     20061212 salzkammergut: Commented out line 186ff (eventually to be removed)
*/
public class TablePanel extends CellPanel{
	
	myTableControl tc;
	myTableModel myMod;
	int selectedCache=0;
	Preferences pref;
	Vector cacheDB;
	MainTab myMaintab;
	StatusBar statBar;
	
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
	
	public void gotoFirstLine() {
		tc.scrollToVisible(0,0);
	}
	
	/** @deprecated */
	public void setPanels(GotoPanel gp, MainTab mt) {
		myMaintab = mt;
	}

	public void setSelectedCache(int row){
		selectedCache=row;
	}
	
	
	public int getSelectedCache(){
		return selectedCache;
	}
	
	public void saveColWith(Preferences pref){
		int j=0;
		for (int i = 0; i<=11; i++){
			if(pref.tablePrefs[i] == 1){
				pref.tableWidth[i] = myMod.getColWidth(j++);
			}
		}
		pref.savePreferences();
	}
	
	public void selectAndActive(int rownum){
		tc.scrollToVisible(rownum, 0);
		tc.clearSelectedCells(new Vector());
		selectedCache = rownum;
		for(int i= 0; i < 11; i++){
			tc.addToSelection(rownum,i); 
		}
	}
	
	public void resetModel() {
		myMod.numRows = cacheDB.size();
		Global.getProfile().updateBearingDistance();
		Filter flt = new Filter();
		flt.setFilter();
		flt.doFilter();
		refreshTable();
		selectedCache=0;
	}
	
	/** Move all filtered caches to the end of the table and redesplay table */
	//TODO Add a sort here to restore the sort after a filter
	public void refreshTable(){
		myMod.updateRows();
		tc.update(true);
		if (statBar!=null) statBar.updateDisplay();
	}
	
	public void onEvent(Event ev)
	{
		if(ev instanceof TableEvent){
			Point a = new Point();
			Point dest = new Point();
			a = tc.getSelectedCell(dest);
			try {
				selectedCache = a.y;
			} catch(NullPointerException npe){}
		}
	  super.onEvent(ev); //Make sure you call this.
	}
}
