package CacheWolf;

import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.*;
import ewe.fx.*;

/**
 * Class ID = 4500
 * @author Marc Schnitzler
 *
 */
public class StatusBar extends CellPanel{
	DBStats stats;
	mLabel disp,lblFlt,lblCenter;
	Preferences pref;
	mButton btnFlt;
	mImage imgFlt;
	
	public StatusBar(Preferences p, Vector db){
		pref=p;
		stats = new DBStats(db);
		addNext(disp = new mLabel(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		disp.setToolTip("Total # of caches (GC&OC)\nTotal # visible\nTotal # found");
		addNext(btnFlt= new mButton(imgFlt=new mImage("filter.png")),CellConstants.DONTSTRETCH, CellConstants.DONTFILL); 
		btnFlt.backGround=new ewe.fx.Color(0,255,0); 
		btnFlt.setPreferredSize(20,13);
		btnFlt.borderWidth=0; imgFlt.transparentColor=Color.White;
		btnFlt.setToolTip("Filter status");
//		addNext(lblFlt= new mLabel("Flt"),CellConstants.DONTSTRETCH, CellConstants.DONTFILL); lblFlt.backGround=new ewe.fx.Color(0,255,0);
		addLast(lblCenter=new mLabel(""),CellConstants.STRETCH, WEST|CellConstants.FILL);
		lblCenter.setToolTip("Current center");
		updateDisplay();
	}
	
	public void updateDisplay(){
		String strStatus, strCenter="";
		strStatus = MyLocale.getMsg(4500,"Tot:") + " " + stats.total() + " " +
					MyLocale.getMsg(4501,"Dsp:") + " " + stats.visible() + " " +
					MyLocale.getMsg(4502,"Fnd:") + " " + stats.totalFound() + "  ";
		disp.setText(strStatus);
		// Indicate that a filter is active in the status line
		if (Filter.filterActive)
			btnFlt.backGround=new Color(0,255,0);
		else
			btnFlt.backGround=null;
		// Current centre can only be displayed if screen is big
		// Otherwise it forces a scrollbar
		if (MyLocale.getScreenWidth()>=320) 
			strCenter="  \u00a4 " + pref.curCentrePt.toString();
		
		lblCenter.setText(strCenter);
		relayout(true); // in case the numbers increased and need more space
	}
	
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnFlt){
				Filter flt = new Filter();
				if (Filter.filterActive) {
					flt.clearFilter();
				} else {
					flt.setFilter();
					flt.doFilter();
				}
				Global.mainTab.tbP.refreshTable();
			}
		}
		super.onEvent(ev);
	}
}
