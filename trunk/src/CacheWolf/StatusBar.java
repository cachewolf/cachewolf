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
	mButton btnCacheTour;
	mImage imgCacheTour; 
	
	public StatusBar(Preferences p, Vector db){
		pref=p;
		addNext(btnCacheTour=new mButton(imgCacheTour=new mImage("cachetour.png")),CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		imgCacheTour.transparentColor=Color.White;
		btnCacheTour.setPreferredSize(20,13);btnCacheTour.borderWidth=0; 
		btnCacheTour.setToolTip(MyLocale.getMsg(197,"Show/Hide cachetour"));
		stats = new DBStats(db);
		addNext(disp = new mLabel(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		disp.setToolTip(MyLocale.getMsg(196,"Total # of caches (GC&OC)\nTotal # visible\nTotal # found"));
		addNext(btnFlt= new mButton(imgFlt=new mImage("filter.png")),CellConstants.DONTSTRETCH, CellConstants.DONTFILL); 
		btnFlt.backGround=new ewe.fx.Color(0,255,0); 
		btnFlt.setPreferredSize(20,13);
		btnFlt.borderWidth=0; imgFlt.transparentColor=Color.White;
		btnFlt.setToolTip("Filter status");
//		addNext(lblFlt= new mLabel("Flt"),CellConstants.DONTSTRETCH, CellConstants.DONTFILL); lblFlt.backGround=new ewe.fx.Color(0,255,0);
		addLast(lblCenter=new mLabel(""),CellConstants.STRETCH, WEST|CellConstants.FILL);
		lblCenter.setToolTip(MyLocale.getMsg(195,"Current centre"));
		updateDisplay();
	}
	
	public void updateDisplay(){
		String strStatus, strCenter="";
		strStatus = MyLocale.getMsg(4500,"Tot:") + " " + stats.total() + " " +
					MyLocale.getMsg(4501,"Dsp:") + " " + stats.visible() + " " +
					MyLocale.getMsg(4502,"Fnd:") + " " + stats.totalFound() + "  ";
		disp.setText(strStatus);
		// Indicate that a filter is active in the status line
		if (Filter.filterActive==Filter.FILTER_ACTIVE)
			btnFlt.backGround=new Color(0,255,0);
		else if (Filter.filterActive==Filter.FILTER_CACHELIST)
			btnFlt.backGround=new Color(0,0,255);
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
				if (Filter.filterActive==Filter.FILTER_INACTIVE) {
					flt.setFilter();
					flt.doFilter();
				} else {
					flt.clearFilter();
				}
				Global.mainTab.tbP.refreshTable();
			}
			if (ev.target == btnCacheTour){
				Global.mainForm.toggleCacheListVisible();			
			}
			Gui.takeFocus(Global.mainTab.tbP.tc, Control.ByKeyboard);
		}
		super.onEvent(ev);
	}
}
