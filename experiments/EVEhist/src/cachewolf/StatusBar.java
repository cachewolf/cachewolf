package cachewolf;

import eve.ui.*;
import eve.sys.Event;
import java.util.*;
import eve.sys.Device;
import eve.fx.*;
import eve.ui.event.ControlEvent;

/**
 * @author Marc Schnitzler
 *
 */
public class StatusBar extends CellPanel{
	private DBStats stats=new DBStats();
	private StringBuffer sb=new StringBuffer(100);
	private Label disp,lblCenter;
	private Button btnFlt;
	private Button btnCacheTour;
	boolean mobileVGA;
	private Color BLUE=new Color(0,0,255);
	private Color GREEN=new Color(0,255,0);
	private Color TURQUOISE=new Color(0,255,255);

	public StatusBar(){
		int sw = MyLocale.getScreenWidth();
		mobileVGA = (Device.isMobile() && sw >= 400);
		String imagesize="";
		if(mobileVGA) imagesize="_vga";
		addNext(btnCacheTour=new Button(new Picture("cachetour"+imagesize+".png",Color.White,0)),CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		btnCacheTour.setPreferredSize(20,13); btnCacheTour.borderWidth=0;
		btnCacheTour.setToolTip(MyLocale.getMsg(197,"Show/Hide cachetour"));
		if(mobileVGA)
			btnCacheTour.setPreferredSize(28,20);
		else
			btnCacheTour.setPreferredSize(20,13);
		addNext(btnFlt= new Button(new Picture("filter"+imagesize+".png",Color.White,0)),CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		btnFlt.backGround=new eve.fx.Color(0,255,0);
		btnFlt.setPreferredSize(20,13);
		btnFlt.borderWidth=0;
		btnFlt.setToolTip("Filter status");
		if(mobileVGA)
			btnFlt.setPreferredSize(28,20);
		else
			btnFlt.setPreferredSize(20,13);
		addNext(disp = new Label(""),CellConstants.DONTSTRETCH, CellConstants.FILL);
		disp.setToolTip(MyLocale.getMsg(196,"Total # of caches (GC&OC)\nTotal # visible\nTotal # found"));
		addLast(lblCenter=new Label(""),CellConstants.STRETCH, WEST|CellConstants.FILL);
		lblCenter.setToolTip(MyLocale.getMsg(195,"Current centre"));
		updateDisplay();
	}

	public void updateDisplay(){
		String strCenter="";
		sb.delete(0,100);
		sb.append(MyLocale.getMsg(4500,"Tot:")).append(" ").append(stats.total()).append(" ").
					append(MyLocale.getMsg(4501,"Dsp:")).append(" ").append(stats.visible()).append(" ").
					append(MyLocale.getMsg(4502,"Fnd:")).append(" ").append(stats.totalFound()).append("  ");
		disp.setText(sb.toString());
		// Indicate that a filter is active in the status line
		if (Global.getProfile().filterActive==Filter.FILTER_ACTIVE)
			btnFlt.backGround=GREEN;
		else if (Global.getProfile().filterActive==Filter.FILTER_CACHELIST)
			btnFlt.backGround=BLUE;
		else if (Global.getProfile().filterActive==Filter.FILTER_MARKED_ONLY)
			btnFlt.backGround=TURQUOISE;
		else
			btnFlt.backGround=null;
		// Current centre can only be displayed if screen is big
		// Otherwise it forces a scrollbar
		// This can happen even on bigger screens with big fonts
		if ((MyLocale.getScreenWidth()>=320) && !(mobileVGA && (Global.getPref().fontSize > 20)))
			strCenter="  \u00a4 " + Global.getPref().curCentrePt.toString();

		lblCenter.setText(strCenter);
		relayout(true); // in case the numbers increased and need more space
	}

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnFlt){
				Filter flt = new Filter();
				if (Global.getProfile().filterActive==Filter.FILTER_INACTIVE) {
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
			Gui.takeFocus(Global.mainTab.tbP.tControl, Control.ByKeyboard);
		}
		super.onEvent(ev);
	}

//################################################################################
//  DBStats
//################################################################################

	/**
	 * @author Marc
	 * Use this class to obtain statistics or information on a cache database.
	 */
	private class DBStats {
		Vector cacheDB;

		public DBStats(){
			cacheDB = Global.getProfile().cacheDB;
		}

		/**
		 * Method to get the number of caches displayed in the list.
		 * It will count waypoints only that start with
		 * GC,or
		 * OC
		 * @return
		 */
		public int visible(){
			CacheHolder ch;
			int counter = 0;
			for(int i = cacheDB.size()-1;i>=0; i--){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_black == false && ch.is_filtered == false){
					if(ch.wayPoint.startsWith("GC") || ch.wayPoint.startsWith("OC")) counter++;
				}
			}
			return counter;
		}

		/**
		 * Method to get the number of caches available for display
		 * @return
		 */
		public int total(){
			CacheHolder ch;
			int counter = 0;
			for(int i = cacheDB.size()-1;i>=0; i--){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_black == false){
					if(ch.wayPoint.startsWith("GC") || ch.wayPoint.startsWith("OC")) counter++;
				}
			}
			return counter;
		}

		public int totalFound(){
			CacheHolder ch;
			int counter = 0;
			for(int i = cacheDB.size()-1;i>=0; i--){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_found == true) {
					if(ch.wayPoint.startsWith("GC") || ch.wayPoint.startsWith("OC")) counter++;
				}
			}
			return counter;
		}
	}


}
