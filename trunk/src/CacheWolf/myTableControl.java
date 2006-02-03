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
			p.waitFor();
		}catch(Exception ex){
			Vm.debug("Cannot start browser! " +ex.toString());
		}
	}
}
