/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf;

import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import ewe.fx.Color;
import ewe.sys.Vm;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Gui;
import ewe.ui.mButton;
import ewe.ui.mLabel;

/**
 * Class ID = 4500
 * 
 * @author Marc Schnitzler
 * 
 */
public class TablePanelStatusBar extends CellPanel {
    private DBStats stats;
    private mLabel disp, lblCenter;
    private mButton btnFlt;
    private mButton btnCacheTour;
    private mButton btnNoSorting;
    private MyTableModel myTableModel;

    public TablePanelStatusBar(MyTableModel myTableModel) {
	this.myTableModel = myTableModel;

	addNext(btnCacheTour = GuiImageBroker.getButton("", "cachetour"), DONTSTRETCH, FILL);
	btnCacheTour.setToolTip(MyLocale.getMsg(197, "Show/Hide cachetour"));

	addNext(btnFlt = GuiImageBroker.getButton("", "filter"), DONTSTRETCH, FILL);
	btnFlt.setToolTip(MyLocale.getMsg(273, "filter on/off"));

	addNext(btnNoSorting = GuiImageBroker.getButton("", "nosort"), DONTSTRETCH, FILL);
	btnNoSorting.setToolTip(MyLocale.getMsg(274, "no autosort"));

	stats = new DBStats();
	addNext(disp = new mLabel(""), DONTSTRETCH, FILL);
	disp.setToolTip(MyLocale.getMsg(196, "Total # of caches (GC&OC)\nTotal # visible\nTotal # found"));

	lblCenter = new mLabel("");
	lblCenter.setToolTip(MyLocale.getMsg(195, "Current centre"));
	// vermeide horizontales scrollen
	// hängt auch von der Icongrösse / Schriftgrösse ab (ist so nicht korrekt) 
	if (Preferences.itself().myAppWidth >= 640) {
	    addLast(lblCenter, STRETCH, LEFT | FILL);
	}
    }

    String oldInfo = "";

    public void updateDisplay(String strInfo) {
	if (strInfo.equals("")) {
	    lblCenter.backGround = null;
	} else {
	    if (oldInfo.equals(strInfo)) {
		return;
	    }
	    lblCenter.backGround = new Color(0, 255, 0);
	    oldInfo = strInfo;
	}
	String strStatus = "";
	// boolean bigScreen=(MyLocale.getScreenWidth()>=480) && !(MobileVGA && (pref.fontSize > 20));
	boolean bigScreen = !Vm.isMobile();
	strStatus += MyLocale.getMsg(4500, "Tot:") + " " + stats.total(bigScreen) + " " + MyLocale.getMsg(4501, "Dsp:") + " " + stats.visible(bigScreen) + " " + MyLocale.getMsg(4502, "Fnd:") + " " + stats.totalFound() + "  ";
	disp.setToolTip("Cache/Addi +Blacklisted");
	disp.setText(strStatus);
	// Indicate that a filter is active in the status line
	if (MainForm.profile.getFilterActive() == Filter.FILTER_ACTIVE)
	    btnFlt.backGround = new Color(0, 255, 0);
	else if (MainForm.profile.getFilterActive() == Filter.FILTER_CACHELIST)
	    btnFlt.backGround = new Color(0, 0, 255);
	else if (MainForm.profile.getFilterActive() == Filter.FILTER_MARKED_ONLY)
	    btnFlt.backGround = new Color(0, 255, 255);
	else
	    btnFlt.backGround = null;
	if (bigScreen && lblCenter.backGround == null)
	    strInfo = "  \u00a4 " + Preferences.itself().curCentrePt.toString();
	if (Preferences.itself().sortAutomatic) {
	    this.btnNoSorting.backGround = new Color(0, 255, 255);
	} else {
	    this.btnNoSorting.backGround = null;
	}

	lblCenter.setText(strInfo);
	relayout(true); // in case the numbers increased and need more space
	this.repaintNow();
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == btnFlt) {
		Filter flt = new Filter();
		if (MainForm.profile.getFilterActive() == Filter.FILTER_INACTIVE) {
		    flt.setFilter();
		    flt.doFilter();
		} else {
		    flt.clearFilter();
		}
		MainTab.itself.tablePanel.refreshTable();
	    }
	    if (ev.target == btnCacheTour) {
		MainTab.itself.tablePanel.mainMenu.toggleCacheTourVisible();
	    }
	    if (ev.target == btnNoSorting) {
		Preferences.itself().sortAutomatic = !Preferences.itself().sortAutomatic;
		myTableModel.sortTable(-1, true);
	    }
	    Gui.takeFocus(MainTab.itself.tablePanel.myTableControl, ControlConstants.ByKeyboard);
	}
	super.onEvent(ev);
    }
}

/**
 * @author Marc
 *         Use this class to obtain statistics or information on a cache database.
 */
class DBStats {

    public DBStats() {
    }

    /**
     * Method to get the number of caches displayed in the list.
     * It will count waypoints only that start with
     * GC,or
     * OC
     * 
     * @return
     */
    public String visible(boolean big) {
	CacheHolder holder;
	int counter = 0;
	int whiteCaches = 0;
	int whiteWaypoints = 0;
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    holder = MainForm.profile.cacheDB.get(i);
	    if (holder.isVisible()) {
		counter++;
		if (CacheType.isAddiWpt(holder.getType())) {
		    whiteWaypoints++;
		} else {
		    whiteCaches++;
		}
	    }
	}
	if (big)
	    return counter + "(" + whiteCaches + "/" + whiteWaypoints + ")";
	else
	    return "" + whiteCaches;

    }

    /**
     * Method to get the number of caches available for display
     * 
     * @return
     */
    public String total(boolean big) {
	CacheHolder holder;
	int all = MainForm.profile.cacheDB.size();
	int whiteCaches = 0;
	int whiteWaypoints = 0;
	int blackCaches = 0;
	int blackWaypoints = 0;
	for (int i = 0; i < all; i++) {
	    holder = MainForm.profile.cacheDB.get(i);
	    if (holder.is_black()) {
		if (CacheType.isAddiWpt(holder.getType())) {
		    blackWaypoints++;
		} else {
		    blackCaches++;
		}
	    } else {
		if (CacheType.isAddiWpt(holder.getType())) {
		    whiteWaypoints++;
		} else {
		    whiteCaches++;
		}
	    }
	}
	if (big) {
	    if (blackCaches > 0 || blackWaypoints > 0) {
		return all + "(" + whiteCaches + "/" + whiteWaypoints + "+" + blackCaches + "/" + blackWaypoints + ")";
	    } else {
		return all + "(" + whiteCaches + "/" + whiteWaypoints + ")";
	    }
	} else
	    return "" + whiteCaches;
    }

    public int totalFound() {
	CacheHolder holder;
	int counter = 0;
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    holder = MainForm.profile.cacheDB.get(i);
	    if (holder.is_found() == true) {
		if (holder.isCacheWpt())
		    counter++;
	    }
	}
	return counter;
    }
}