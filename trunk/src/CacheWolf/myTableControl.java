package CacheWolf;

import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;

/**
*	This class is not required?!
*/
public class myTableControl extends TableControl{

	public Vector db;
	public Preferences pref;
	public TablePanel tbp;
	
	public void penRightReleased(Point p){
		p.x =0;
		doShowMenu(p);
                //penReleased(p);
	}
	public void penHeld(Point p){
		doShowMenu(p);
	}
	public void popupMenuEvent(Object selectedItem){
		CacheHolder ch = new CacheHolder();
		if (selectedItem.toString().equals("Filter")){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if(ch.is_Checked != true) {
					ch.is_filtered = true;
					db.set(i, ch);
				}
			}
			tbp.refreshTable();
		}
		if (selectedItem.toString().equals("Delete")){
			for(int i = 0; i <	db.size(); i++){
				ch = (CacheHolder)db.get(i);
				if(ch.is_Checked == true) {
					db.remove(ch);
					i--;
				}
			}
			tbp.refreshTable();
		}
	}
	
	public void  penDoubleClicked(Point where) {
		Point a = new Point();
		Point dest = new Point();
		a = getSelectedCell(dest);
		CacheHolder ch = new CacheHolder();
		CacheReaderWriter crw = new CacheReaderWriter();

		ch = (CacheHolder)db.get(a.y);
		try{
			//String cmd = "\""+pref.browser+ "\"" + " \"http://www.geocaching.com/seek/cache_details.aspx?wp="+ch.wayPoint+"&Submit6=Find&log=y\"";
			crw.readCache(ch, pref.mydatadir);
			String cmd = "\""+pref.browser+ "\" " + ch.URL;
			//String cmd = "\""+pref.browser+ ".exe\"" + " www.aragorn.de";
			Vm.debug(cmd);
			ewe.sys.Process p = Vm.exec(cmd);
			//p.waitFor();
		}catch(Exception ex){
			Vm.debug("Cannot start browser! " +ex.toString());
		}
	}
}
