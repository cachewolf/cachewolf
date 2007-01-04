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
	
	public void penRightReleased(Point p){
		menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}
	public void penHeld(Point p){
		menuState.doShowMenu(p,true,null);
	}
	
	public void popupMenuEvent(Object selectedItem){
		CacheHolder ch;
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1015,"Select all"))){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				ch.is_Checked = true;
				//db.set(i, ch);
			}
			tbp.refreshTable();
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1016,"De-select all"))){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				ch.is_Checked = false;
				//db.set(i, ch);
			}
			tbp.refreshTable();
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1017,"All add. wpts"))){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if (CacheType.isAddiWpt(ch.type))ch.is_Checked = true;
				//db.set(i, ch);
			}
			tbp.refreshTable();
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1018,"Sel. add. wpts."))){
			CacheHolder ch_addi;
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if (ch.is_Checked && !CacheType.isAddiWpt(ch.type)){
					// search addi wpts to main cache
					for (int j = 0; j < db.size(); j++){
						ch_addi = (CacheHolder)db.get(j);
						if (ch_addi.belongsTo(ch))ch_addi.is_Checked = true;
					}
				}
			}
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
			//TODO prüfen, ob es sich um ein gc oder oc cache handelt. Aber wie?
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if(ch.is_Checked == true) {
					spider.spiderSingle(i);
				}
			}
			Vm.showWait(false);
			tbp.refreshTable();
		}
		if (selectedItem.toString().equals(MyLocale.getMsg(1019,"Center"))){
			CacheHolder thisCache = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());
			CWPoint cp=new CWPoint(thisCache.LatLon);
			if (!cp.isValid()){
				MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
				tmpMB.exec();
			} else {				
				pref.curCentrePt.set(cp);
				TablePanel.updateBearingDistance(db, pref);
				tbp.refreshTable();
			}
		}
		
		if (selectedItem.toString().equals(MyLocale.getMsg(1010,"Goto"))){
//			Point a = new Point();
	//		a = this.getSelectedCell(a);
		//	if(!(a == null)) ch = (CacheHolder)tbp.cacheDB.get(a.y);
			ch = (CacheHolder)tbp.cacheDB.get(tbp.getSelectedCache());
			
			tbp.myGotoPanel.setDestination((ch.LatLon));
//this.getSelectedCell(((Menu)selectedItem).curPoint)..LatLon
		}
	}
	
	public void  penDoubleClicked(Point where) {
		Point a = new Point();
		Point dest = new Point();
		a = getSelectedCell(dest);
		CacheHolder ch;

		ch = (CacheHolder)db.get(a.y);
		try{
			//String cmd = "\""+pref.browser+ "\"" + " \"http://www.geocaching.com/seek/cache_details.aspx?wp="+ch.wayPoint+"&Submit6=Find&log=y\"";
			ch.readCache(profile.dataDir);
			String cmd = "\""+pref.browser+ "\" " + ch.URL;
			//String cmd = "\""+pref.browser+ ".exe\"" + " www.aragorn.de";
			//Vm.debug(cmd);
			//ewe.sys.Process p = 
			Vm.exec(cmd);
			//p.waitFor();
		}catch(IOException ex){
			(new MessageBox("Error", "Cannot start browser!\n"+ex.toString()+"\nThe are two possible reasons:\n * path to internet browser in \npreferences not correct\n * An bug in ewe VM, please be \npatient for an update",MessageBox.OKB)).execute();
			Vm.debug("Cannot start browser! " +ex.toString());
		}
	}
}
