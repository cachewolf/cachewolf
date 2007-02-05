package CacheWolf;

import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.*;

/**
 * Class ID = 4500
 * @author Marc Schnitzler
 *
 */
public class StatusBar extends CellPanel{
	DBStats stats;
	mLabel disp,lblFlt,lblCenter;
	Preferences pref;
	
	public StatusBar(Preferences p, Vector db){
		pref=p;
		stats = new DBStats(db);
		addNext(disp = new mLabel(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		addNext(lblFlt= new mLabel("Flt"),CellConstants.DONTSTRETCH, CellConstants.DONTFILL); lblFlt.backGround=new ewe.fx.Color(0,255,0);
		addLast(lblCenter=new mLabel(""),CellConstants.STRETCH, WEST|CellConstants.FILL);
		updateDisplay();
	}
	
	public void updateDisplay(){
		String strStatus, strCenter="";
		strStatus = MyLocale.getMsg(4500,"Tot:") + " " + stats.total() + " " +
					MyLocale.getMsg(4501,"Dsp:") + " " + stats.visible() + " " +
					MyLocale.getMsg(4502,"Fnd:") + " " + stats.totalFound() + "  ";
		disp.setText(strStatus);
		// Indicate that a filter is active in the status line
		Profile prof=Global.getProfile();
		if (Global.getPref().filterActive)
			lblFlt.modify(0,Invisible); // Set the filter to "invisible"
		else
			lblFlt.modify(Invisible,0); // Make filter visible
		// Current centre can only be displayed if screen is big
		// Otherwise it forces a scrollbar
		if (MyLocale.getScreenWidth()>=320) 
			strCenter="  \u00a4 " + pref.curCentrePt.toString();
		
		lblCenter.setText(strCenter);
		repaint();
	}
}
