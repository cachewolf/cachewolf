package CacheWolf;

import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.io.IOException;
import ewe.util.*;

/**
*	This class is not required?!
*/
public class myTableControl extends TableControl{

	public Profile profile;
	public Vector db;
	public Preferences pref;
	public TablePanel tbp;
	public MainTab mainTabs;
	
	public void penRightReleased(Point p){
		menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}
	public void penHeld(Point p){
		menuState.doShowMenu(p,true,null); 
	}
	
	public void onKeyEvent(KeyEvent ev) {
		if (ev.type == KeyEvent.KEY_PRESS && ev.target == this){
			if ( (ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 'a'-'a'+1){ // <ctrl-a> gives 1, <ctrl-b> == 2
				// select all on <ctrl-a>
				selectAll();
				ev.consumed = true;
			}
		}
		super.onKeyEvent(ev);
	}
		
		public void selectAll() {
			CacheHolder ch;
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if (ch.is_filtered == false) ch.is_Checked = true;
				//db.set(i, ch);
			}
			tbp.myMod.cacheSelectionChanged = true;
			tbp.refreshTable();
		}
	
	
	public void popupMenuEvent(Object selectedItem){
		CacheHolder ch;
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1015,"Select all"))){
			selectAll();
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1016,"De-select all"))){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if (ch.is_filtered == false) ch.is_Checked = false;
				//db.set(i, ch);
			}
			tbp.myMod.cacheSelectionChanged = true;
			tbp.refreshTable();
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1011,"Filter"))){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				ch.is_filtered = true;
				if(ch.is_Checked == true) ch.is_filtered = false;
				//db.set(i, ch);
			}
			tbp.refreshTable();
		}
		if (selectedItem.toString().equals(MyLocale.getMsg(1012,"Delete"))){
			if ((new MessageBox("Warnung", "Alle mit Häckchen markierten Caches löschen?", MessageBox.YESB | MessageBox.NOB)).execute() != Form.IDYES) return;
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if(ch.is_Checked == true) {
					db.remove(ch);
					i--;
				}
			}
			tbp.refreshTable();
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1014,"Update"))){
			SpiderGC spider = new SpiderGC(pref, profile);
			Vm.showWait(true);
			spider.login();
			boolean alreadySaid = false;
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if(ch.is_Checked == true) {
					if ( (ch.wayPoint.length() > 1 && ch.wayPoint.substring(0,2).equalsIgnoreCase("GC"))
							|| (ch.mainCache != null &&	ch.mainCache.wayPoint.length() > 1 	&& ch.mainCache.wayPoint.substring(0,2).equalsIgnoreCase("GC")) ) 
					{
						spider.spiderSingle(i);
					} else if (!alreadySaid) {
						alreadySaid = true;
						(new MessageBox("Information","Diese Funktion steht gegenwärtig nur für Geocaching.com zur Verfügung", MessageBox.OKB)).exec();
					}
				}
			}
			tbp.refreshTable();
			Vm.showWait(false);
		}
		if (selectedItem.toString().equals(MyLocale.getMsg(1019,"Center"))){
			CacheHolder thisCache = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());
			CWPoint cp=new CWPoint(thisCache.LatLon);
			if (!cp.isValid()){
				MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
				tmpMB.exec();
			} else {				
				pref.curCentrePt.set(cp);
				profile.updateBearingDistance();
				tbp.refreshTable();
				(new MessageBox("Info", "Entfernungen in der Listenansicht \nvom aktuellen Standpunkt aus \nneu berechnet", MessageBox.OKB)).execute();
			}
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1010,"Goto"))){
			ch = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());
			tbp.myGotoPanel.setDestinationAndSwitch((ch.LatLon));
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1020,"Open online in Browser"))){
			ch = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());
			try{
				ch.readCache(profile.dataDir);
			}catch(IOException ex){	(new MessageBox("Error", "Cannot read cache data\n"+ex.toString()+"\n in cache: "+ch.wayPoint,MessageBox.OKB)).execute(); }
			try {
				String cmd = "\""+pref.browser+ "\" " + ch.URL;
				Vm.exec(cmd);
			} catch (IOException ex) {
				(new MessageBox("Error", "Cannot start browser!\n"+ex.toString()+"\nThe are two possible reasons:\n * path to internet browser in \npreferences not correct\n * An bug in ewe VM, please be \npatient for an update",MessageBox.OKB)).execute();
			}
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1021,"Open description"))){
			openCacheDesc();
		}

	}
	
	void openCacheDesc() {
//		Point a = new Point();
		//	Point dest = new Point();
			//a = getSelectedCell(dest);
			CacheHolder ch;
			ch = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());

		//	ch = (CacheHolder)db.get(a.y);
			try{
				ch.readCache(profile.dataDir);
			}catch(IOException ex){	
				(new MessageBox("Error", "Cannot read cache data\n"+ex.toString()+"\n in cache: "+ch.wayPoint,MessageBox.OKB)).execute(); 
			}
			tbp.myMaintab.select(tbp.myMaintab.descP);
		
	}
	
	public void  penDoubleClicked(Point where) {
		openCacheDesc();
	}
}
