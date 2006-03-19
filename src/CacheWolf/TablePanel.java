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
	int selectedCache;
	Preferences myPreferences = new Preferences();
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	Vector cacheDB;
	
	public TablePanel(Vector DB, Preferences pref){
		cacheDB = DB;
		myPreferences = pref;
		String [] spName = {"?",(String)lr.get(1000,"D"),"T",(String)lr.get(1002,"Waypoint"),"Name",(String)lr.get(1004,"Location"),(String)lr.get(1005,"Owner"),(String)lr.get(1006,"Hidden"),(String)lr.get(1007,"Status"),(String)lr.get(1008,"Dist"),(String)lr.get(1009,"Bear")};
		String[] jester;
		int colWidth[];
		int colnum = 0;
		
		for(int i = 0; i<=10; i++){
			if(pref.tablePrefs[i] == 1) colnum++;
		}
		jester = new String[colnum];
		colWidth = new int[colnum];
		
		int ji = 0;
		for(int i = 0; i<=10;i++){
			if(pref.tablePrefs[i] == 1){
				jester[ji] = spName[i];
				colWidth[ji] = pref.tableWidth[i];
				ji++;
			}
		}
		
		addLast(new ScrollBarPanel(tc = new myTableControl()));
		tc.db = cacheDB;
		tc.pref = pref;
		myMod = new myTableModel(cacheDB, jester, colWidth, tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
	}
	
	public int getSelectedCache(){
		return selectedCache;
	}
	
	public void selectAndActive(int rownum){
		tc.scrollToVisible(rownum, 0);
		tc.clearSelectedCells(new Vector());
		selectedCache = rownum;
		for(int i= 0; i < 10; i++){
			tc.addToSelection(rownum,i); 
		}
	}
	
	public void resetModel(Vector cacheDB) {
		myMod.setVector(cacheDB);
		updateBearingDistance(cacheDB, myPreferences);
		////Vm.debug("IS mypref loaded? " + myPreferences.mylgDeg);
		tc.setTableModel(myMod);
		tc.update(true);
	}
	
	public void refreshTable(){
		String [] spName = {"?",(String)lr.get(1000,"D"),"T",(String)lr.get(1002,"Waypoint"),"Name",(String)lr.get(1004,"Location"),(String)lr.get(1005,"Owner"),(String)lr.get(1006,"Hidden"),(String)lr.get(1007,"Status"),(String)lr.get(1008,"Dist"),(String)lr.get(1009,"Bear")};
		String[] jester;
		int colWidth[];

		int colnum = 0;
		
		for(int i = 0; i<=10; i++){
			if(myPreferences.tablePrefs[i] == 1) colnum++;
		}
		jester = new String[colnum];
		colWidth = new int[colnum];
		int ji = 0;
		for(int i = 0; i<=10;i++){
			if(myPreferences.tablePrefs[i] == 1){
				jester[ji] = spName[i];
				colWidth[ji] = myPreferences.tableWidth[ji];
				ji++;
			}
		}
		myMod = new myTableModel(cacheDB, jester, colWidth, tc, getFontMetrics());
		myMod.hasRowHeaders = false;
		myMod.hasColumnHeaders  = true;
		tc.setTableModel(myMod);
		myMod.updateRows();
		tc.update(true);
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
		Locale l = Vm.getLocale();


		CWPoint fromPoint = new CWPoint(p.mylgNS, p.mylgDeg, p.mylgMin,"0",
										p.mybrWE, p.mybrDeg, p.mybrMin,"0", CWPoint.DMM);
		
		//Vm.debug(" New location: " + fromPoint);
		
		int anz = cacheDB.getCount();
		anz--;
		CacheHolder ch = new CacheHolder();
		CWPoint toPoint = new CWPoint();
		
		// Jetzt durch die CacheDaten schleifen
		while(anz >= 0){
			ch = new CacheHolder();
			ch = (CacheHolder)cacheDB.get(anz);
			toPoint.set(ch.LatLon, CWPoint.CW);
			ewe.sys.Double db = new ewe.sys.Double();
			ch.kilom = fromPoint.getDistance(toPoint);
			ch.degrees = fromPoint.getBearing(toPoint);
			ch.bearing = CWPoint.getDirection(ch.degrees);
			db.set(ch.kilom);
			ch.distance = l.format(ewe.sys.Locale.FORMAT_PARSE_NUMBER,db,"0.00");
			ch.distance = ch.distance + " km";
			cacheDB.del(anz);
			cacheDB.add(anz, ch);
			anz--;
		}
	} //updateBearingDistance
	
	
	public void onEvent(Event ev)
	{
		////Vm.debug(ev.toString());
		if(ev instanceof TableEvent){
			Point a = new Point();
			Point dest = new Point();
			a = tc.getSelectedCell(dest);
			try{
				selectedCache = a.y;
			}catch(NullPointerException npe){
			}
		}
	  
	  super.onEvent(ev); //Make sure you call this.
	}
}


