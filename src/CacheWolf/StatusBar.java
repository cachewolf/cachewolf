package CacheWolf;

import ewe.ui.*;
import ewe.util.*;

/**
 * Class ID = 4500
 * @author Marc Schnitzler
 *
 */
public class StatusBar extends CellPanel{
	DBStats stats;
	mLabel disp;
	Preferences pref;
	
	public StatusBar(Preferences p, Vector db){
		pref=p;
		stats = new DBStats(db);
		this.addLast(disp = new mLabel(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		updateDisplay();
	}
	
	public void updateDisplay(){
		String dspString;
		dspString = MyLocale.getMsg(4500,"Tot:") + " " + stats.total() + " " +
					MyLocale.getMsg(4501,"Dsp:") + " " + stats.visible() + " " +
					MyLocale.getMsg(4502,"Fnd:") + " " + stats.totalFound() + "  " +
					"Centre: " + pref.curCentrePt.toString();
		
		disp.setText(dspString);
	}
}
