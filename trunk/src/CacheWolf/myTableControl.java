package CacheWolf;

import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.io.IOException;
import ewe.util.*;

/**
*	Implements the user interaction of the list view. Works together with myTableModel and TablePanel
*/
public class myTableControl extends TableControl{

	public Preferences pref;
	public Profile profile;
	public Vector cacheDB;
	public TablePanel tbp;
	
	myTableControl(TablePanel tablePanel) {
		Menu m = new Menu(new String[]{
				MyLocale.getMsg(1021,"Open description"),
				MyLocale.getMsg(1010,"Goto"),
				MyLocale.getMsg(1019,"enter"),
				MyLocale.getMsg(1020,"open in $browser online"),
				"-",
				MyLocale.getMsg(1011,"Filter"),
				MyLocale.getMsg(1012,"Delete"),
				MyLocale.getMsg(1014,"Update"),
				"-",
				MyLocale.getMsg(1015,"Select all"),
				MyLocale.getMsg(1016,"De-select all")},
				MyLocale.getMsg(1013,"With selection"));
		setMenu(m);
		profile=Global.getProfile();
		cacheDB = profile.cacheDB;
		pref = Global.getPref();
		tbp =tablePanel;
	}
	
	public void penRightReleased(Point p){
		if (cacheDB.size()>0) // No context menu when DB is empty
		   menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}
	public void penHeld(Point p){
		if (cacheDB.size()>0) // No context menu when DB is empty
		   menuState.doShowMenu(p,true,null); 
	}
	
	public void onKeyEvent(KeyEvent ev) {
		if (ev.type == KeyEvent.KEY_PRESS && ev.target == this){
			if ( (ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 1){ // <ctrl-a> gives 1, <ctrl-b> == 2
				// select all on <ctrl-a>
				setSelectForAll(true);
				ev.consumed = true;
			}
		}
		super.onKeyEvent(ev);
	}
		
	/** Set all caches either as selected or as deselected, depending on argument */
	private void setSelectForAll(boolean selectStatus) {
		Global.getProfile().setSelectForAll(selectStatus);
		tbp.refreshTable();
	}
	
	public void popupMenuEvent(Object selectedItem){
		CacheHolder ch;
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1015,"Select all"))){
			setSelectForAll(true);
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1016,"De-select all"))){
			setSelectForAll(false);
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1011,"Filter"))){
			for(int i = cacheDB.size()-1; i >=0; i--){
				ch = (CacheHolder)cacheDB.get(i);
				// incremental filter. Keeps status of all marked caches and
				// adds unmarked caches to filtered list
				ch.is_filtered = !ch.is_Checked || ch.is_filtered;
			}
			tbp.refreshTable();
		}
		if (selectedItem.toString().equals(MyLocale.getMsg(1012,"Delete"))){
			if ((new MessageBox(MyLocale.getMsg(144,"Warnung"),MyLocale.getMsg(1022, "Delete all caches that have a tick?"), MessageBox.YESB | MessageBox.NOB)).execute() != Form.IDYES) return;
				DataMover dm=new DataMover();
				for(int i = cacheDB.size()-1; i >=0; i--){
					ch = (CacheHolder)cacheDB.get(i);
					if(ch.is_Checked == true) {
						dm.deleteCacheFiles(ch.wayPoint,profile.dataDir);
						cacheDB.remove(ch);
					}
				}
			profile.saveIndex(pref,true);	
			tbp.refreshTable();
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1014,"Update"))){
            SpiderGC spider = new SpiderGC(pref, profile);
            Vm.showWait(true);
            spider.login();
            boolean alreadySaid = false;
            boolean alreadySaid2 = false;
            for(int i = 0; i <	cacheDB.size(); i++){
                    ch = (CacheHolder)cacheDB.get(i);
                    if(ch.is_Checked == true) {
                            if ( (ch.wayPoint.length() > 1 && ch.wayPoint.substring(0,2).equalsIgnoreCase("GC")))
//Notiz: Wenn es ein addi Wpt ist, sollte eigentlich der Maincache gespidert werden
//Alter code prüft aber nur ob ein Maincache von GC existiert und versucht dann den addi direkt zu spidern, was nicht funktioniert
//TODO: Diese Meldungen vor dem Einloggen darstellen						
		{
                                    spider.spiderSingle(i);
                            } else if (ch.isAddiWpt() && !ch.mainCache.is_Checked) { // Is the father ticked?
                            		if (!alreadySaid2) {
                                            alreadySaid2=true;
                                            (new MessageBox("Information","Hilfswegpunkte könnnen nicht direkt gespidert werden\nBitte zusätzlich den Vater anhaken", MessageBox.OKB)).exec();
                                    }
                            } else if (ch.mainCache != null &&	ch.mainCache.wayPoint.length() > 1 	&& !ch.mainCache.wayPoint.substring(0,2).equalsIgnoreCase("GC") && 
                                               !alreadySaid) {
                                    alreadySaid = true;
                                    (new MessageBox("Information",ch.wayPoint+">"+ch.mainCache.wayPoint+": Diese Funktion steht gegenwärtig nur für Geocaching.com zur Verfügung", MessageBox.OKB)).exec();
                            }
                    }
            }
			profile.hasUnsavedChanges=true;	
            tbp.refreshTable();
            Vm.showWait(false);
		}
		if (selectedItem.toString().equals(MyLocale.getMsg(1019,"Center"))){
			CacheHolder thisCache = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			CWPoint cp=new CWPoint(thisCache.LatLon);
			if (!cp.isValid()){
				MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
				tmpMB.exec();
			} else {				
				pref.curCentrePt.set(cp);
				Global.mainTab.updateBearDist(); // Update the distances with a warning message
				tbp.refreshTable();
			}
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1010,"Goto"))){
			ch = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			Global.mainTab.gotoPoint(ch.LatLon);
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1020,"Open online in Browser"))){
			ch = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			try{
				ch.readCache(profile.dataDir);
			}catch(IOException ex){	(new MessageBox(MyLocale.getMsg(321,"Error"), "Cannot read cache data\n"+ex.toString()+"\nCache: "+ch.wayPoint,MessageBox.OKB)).execute(); }
			try {
				String cmd = "\""+pref.browser+ "\" \"" + ch.URL+"\"";
				Vm.exec(cmd);
			} catch (IOException ex) {
				(new MessageBox("Error", "Cannot start browser!\n"+ex.toString()+"\nThe are two possible reasons:\n * path to internet browser in \npreferences not correct\n * An bug in ewe VM, please be \npatient for an update",MessageBox.OKB)).execute();
			}
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1021,"Open description"))){
			penDoubleClicked(null);
		}

	}
	
	public void penDoubleClicked(Point where) {
		Global.mainTab.select(Global.mainTab.descP);
	}

	
}
