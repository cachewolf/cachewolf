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
	int selectedCache;
	Preferences pref;
	Vector cacheDB;
	GotoPanel myGotoPanel;
	MainTab myMaintab;
	StatusBar statBar;
	
	public TablePanel(Preferences p, Profile profile, StatusBar statBar){
		this.statBar = statBar;
		cacheDB = profile.cacheDB;
		pref = p;
/*
		String [] spName = {" ","?",MyLocale.getMsg(1000,"D"),"T",MyLocale.getMsg(1002,"Waypoint"),"Name",MyLocale.getMsg(1004,"Location"),MyLocale.getMsg(1005,"Owner"),MyLocale.getMsg(1006,"Hidden"),MyLocale.getMsg(1007,"Status"),MyLocale.getMsg(1008,"Dist"),MyLocale.getMsg(1009,"Bear")};
		String[] jester;
		int colWidth[];
		int colnum = 0;
		
		for(int i = 0; i<=11; i++){
			if(pref.tablePrefs[i] == 1) colnum++;
		}
		jester = new String[colnum];
		colWidth = new int[colnum];
		
		int ji = 0;
		for(int i = 0; i<=11;i++){
			if(pref.tablePrefs[i] == 1){
				jester[ji] = spName[i];
				colWidth[ji] = pref.tableWidth[i];
				ji++;
			}
		}
*/		
		addLast(new ScrollBarPanel(tc = new myTableControl()));
		if (statBar!=null) addLast(statBar,CellConstants.DONTSTRETCH, CellConstants.FILL);
		Menu m = new Menu(new String[]{MyLocale.getMsg(1010,"Goto"),MyLocale.getMsg(1019,"Center"),"-",MyLocale.getMsg(1011,"Filter"),MyLocale.getMsg(1012,"Delete"),MyLocale.getMsg(1014,"Update"),"-",MyLocale.getMsg(1015,"Select all"),MyLocale.getMsg(1016,"De-select all")},MyLocale.getMsg(1013,"With selection"));
		tc.setMenu(m);
		tc.profile=profile;
		tc.db = cacheDB;
		tc.pref = p;
		tc.tbp = this;
		myMod = new myTableModel(tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
	}
	
	public void setPanels(GotoPanel gp, MainTab mt) {
		myGotoPanel = gp;
		myMaintab = mt;
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
		//RB  myMod.setVector(cacheDB);
		myMod.numRows = cacheDB.size();
		updateBearingDistance(cacheDB, pref);
		////Vm.debug("IS mypref loaded? " + myPreferences.mylgDeg);
		// tc.setTableModel(myMod); Not needed ?
		myMod.updateRows();
		tc.update(true);
		if (statBar!=null) statBar.updateDisplay();
	}
	
	public void refreshTable(){
		myMod.updateRows();
		tc.update(true);
		if (statBar!=null) statBar.updateDisplay();
	}
	
	/**
	*	Method to calculate bearing and distance of a cache in the index
	*	list.
	*	@see	CacheHolder
	*	@see	Extractor
	*	@see	Navi
	*/
	public static void updateBearingDistance(Vector cacheDB, Preferences p){
		//myPreferences = p;

		CWPoint fromPoint = new CWPoint(p.curCentrePt); // Clone current centre to be sure
		
		//Vm.debug(" New location: " + fromPoint);
		
		int anz = cacheDB.getCount();
		CacheHolder ch;
		CWPoint toPoint = new CWPoint();
		// Jetzt durch die CacheDaten schleifen
		while(--anz >= 0){
			ch = (CacheHolder)cacheDB.get(anz); // This returns a pointer to the CacheHolder object
			if(ch.LatLon.length()>4){
				toPoint.set(ch.LatLon, CWPoint.CW); // Fast parse with traditional parse algorithm
				ch.kilom = fromPoint.getDistance(toPoint);
				ch.degrees = fromPoint.getBearing(toPoint);
				ch.bearing = CWPoint.getDirection(ch.degrees);
				ch.distance = MyLocale.formatDouble(ch.kilom,"0.00");
				ch.distance = ch.distance + " km";
// skg20061223: These two lines are superfluous as we have already updated the correct CacheHolder object
//				cacheDB.del(anz);
//				cacheDB.add(anz, ch);
			}
		}
	} //updateBearingDistance
	
	public void onEvent(Event ev)
	{
		////Vm.debug(ev.toString());
		if(ev instanceof PenEvent){
			if(ev.type == PenEvent.RIGHT_BUTTON){
				Vm.debug("Right mouse button pressed");
			}
		}
		if(ev instanceof TableEvent){
			Point a = new Point();
			Point dest = new Point();
			a = tc.getSelectedCell(dest);
			try{
				selectedCache = a.y;
					}catch(NullPointerException npe){
			}
		}
		/* Not needed because myTableModel contains code to handle click on checkBox image
		if(ev instanceof ControlEvent && ev.target instanceof mCheckBox){
			mCheckBox m = new mCheckBox();
			m = (mCheckBox)ev.target;
			CacheHolder ch = new CacheHolder();
			String tag = new String();
			tag = (String)m.getTag(0, "nix");
			for(int i = 0; i<cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.wayPoint.equals(tag)){
					ch.is_Checked = m.getState();
					cacheDB.set(i, ch);
				}
			}
		} */
	  super.onEvent(ev); //Make sure you call this.
	}
}


