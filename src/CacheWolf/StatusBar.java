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
	
	public StatusBar(Vector db){
		stats = new DBStats(db);
		this.addLast(disp = new mLabel(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		updateDisplay();
	}
	
	public void updateDisplay(){
		String dspString = new String();
		dspString = MyLocale.getMsg(4500,"Tot:") + " " + stats.total() + " ";
		dspString += MyLocale.getMsg(4501,"Dsp:") + " " + stats.visible() + " ";
		dspString += MyLocale.getMsg(4502,"Fnd:") + " " + stats.totalFound();
		disp.setText(dspString);
	}
}
