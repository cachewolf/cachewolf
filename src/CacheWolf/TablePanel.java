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

import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import ewe.fx.Dimension;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.util.Vector;

/**
 * Class to display the cache database in a table.
 * Class ID = 1000
 */
public class TablePanel extends CellPanel {

    MyTableControl myTableControl;
    public MyTableModel myTableModel;
    CacheDB cacheDB;
    TablePanelStatusBar statusBar;
    TablePanelMenu mainMenu;
    /**
     * We keep track of the currently selected cache in two variables(for speed)
     * selectedIdx is the index in cacheDB, selectedCh is the actual cache
     * selectedIdx=-1 if no caches are visible (i.e. database empty or filtered). In
     * this case selectedCh is "null".
     * Otherwise selectedIdx points to a visible cache.
     * When the cacheDB is reorganised (by sort/filter/search), the selected cache
     * may end up at a new index.
     */
    int selectedIdx = 0;
    CacheHolder selectedCh;

    public TablePanel() {

	cacheDB = MainForm.profile.cacheDB;
	MyScrollBarPanel theTableControl = new MyScrollBarPanel(myTableControl = new MyTableControl());

	myTableModel = new MyTableModel(myTableControl);
	myTableModel.hasRowHeaders = false;
	myTableModel.hasColumnHeaders = true;

	myTableControl.setTableModel(myTableModel);

	mainMenu = new TablePanelMenu();
	mainMenu.setTablePanel(this);

	if (Preferences.itself().showStatus) {
	    statusBar = new TablePanelStatusBar(myTableModel);
	} else
	    statusBar = null;

	if (Preferences.itself().tabsAtTop) {
	    if (Preferences.itself().menuAtTab) {
		addLast(mainMenu, CellConstants.DONTSTRETCH, CellConstants.FILL);
		addLast(theTableControl, STRETCH, FILL);
		if (Preferences.itself().showStatus)
		    addLast(statusBar, CellConstants.DONTSTRETCH, CellConstants.FILL);
	    } else {
		addLast(theTableControl, STRETCH, FILL);
		if (Preferences.itself().showStatus)
		    addLast(statusBar, CellConstants.DONTSTRETCH, CellConstants.FILL);
		addLast(mainMenu, CellConstants.DONTSTRETCH, CellConstants.FILL);
	    }
	} else {
	    if (Preferences.itself().menuAtTab) {
		addLast(theTableControl, STRETCH, FILL);
		if (Preferences.itself().showStatus)
		    addLast(statusBar, CellConstants.DONTSTRETCH, CellConstants.FILL);
		addLast(mainMenu, CellConstants.DONTSTRETCH, CellConstants.FILL);
	    } else {
		addLast(mainMenu, CellConstants.DONTSTRETCH, CellConstants.FILL);
		addLast(theTableControl, STRETCH, FILL);
		if (Preferences.itself().showStatus)
		    addLast(statusBar, CellConstants.DONTSTRETCH, CellConstants.FILL);
	    }
	}

    }

    /** Mark the row as selected so that myTableModel can color it grey */
    public void selectRow(int row) {
	// Ensure that the highlighted row is visible (e.g. when coming from radar panel)
	// Next line needed for key scrolling 
	myTableControl.cursorTo(row, 0, true); //tc.cursor.x+tc.listMode
    }

    /** Highlight the first row in grey. It can be unhighlighted by clicking */
    public void selectFirstRow() {
	myTableModel.cursorSize = new Dimension(-1, 1);
	if (cacheDB.size() > 0) {
	    myTableControl.cursorTo(0, 0, true);
	}
    }

    /**
     * Returns the index of the currently selected cache or 0 if the cache is no longer visible
     * due to a sort/filter or search operation
     * -1 if no cache is visible
     * 
     * @return index of selected cache (0 if not visible, -1 if no cache is visible)
     */
    public int getSelectedCache() {
	if (myTableModel.numRows < 1)
	    return -1;
	// If the selected Cache is no longer visible (e.g. after applying a filter)
	// select the first row
	if (myTableControl.cursor.y >= myTableModel.numRows)
	    return 0;
	return myTableControl.cursor.y;
    }

    public void saveColWidth() {
	String colWidths = myTableModel.getColWidths();
	if (!colWidths.equals(Preferences.itself().listColWidth)) {
	    Preferences.itself().listColWidth = colWidths;
	    Preferences.itself().savePreferences();
	}
    }

    public void resetModel() {
	myTableModel.numRows = cacheDB.size();
	MainForm.profile.updateBearingDistance();
	myTableControl.scrollToVisible(0, 0);
	refreshTable();
    }

    /**
     * Similar to refreshTable but not so "heavy".
     * Is used when user changes settings in preferences.
     */
    public void refreshControl() {
	myTableControl.update(true);
	updateStatusBar();
    }

    /** Move all filtered caches to the end of the table and redisplay table */
    //TODO Add a sort here to restore the sort after a filter
    public void refreshTable() {

	// First: Remember currently selected waypoint
	String wayPoint;
	Vector oldVisibleCaches = null;
	int sel = getSelectedCache();
	if ((sel >= 0) && (sel < cacheDB.size())) // sel > cacheDB.size() can happen if you load a new profile, which is smaller than the old profile and you selected one cache that exceeds the number of caches in the new profile  
	    wayPoint = cacheDB.get(sel).getWayPoint();
	else
	    wayPoint = null;
	// Then: remember all caches that are visible before the refresh
	if (wayPoint != null) {
	    oldVisibleCaches = new Vector(sel);
	    for (int i = 0; i < sel; i++) {
		oldVisibleCaches.add(cacheDB.get(i));
	    }
	}
	myTableModel.updateRows();

	// Check whether the currently selected cache is still visible
	int rownum = 0;
	if (wayPoint != null) {
	    rownum = MainForm.profile.cacheDB.getIndex(wayPoint); //profile.cacheDB.getIndex(wayPoint);
	    // If it is not visible: Go backward in the list of the 
	    // previously visible caches and look if you find
	    // any cache that is now still visible.
	    if ((rownum < 0) || (rownum >= myTableModel.numRows)) {
		if (oldVisibleCaches != null) {
		    int i;
		    for (i = sel - 1; i >= 0; i--) {
			CacheHolder checkCache = (CacheHolder) oldVisibleCaches.get(i);
			rownum = MainForm.profile.cacheDB.getIndex(checkCache.getWayPoint()); //profile.cacheDB.getIndex(checkCache.getWayPoint());
			if ((rownum >= 0) && (rownum < myTableModel.numRows))
			    break;
			rownum = 0;
		    }
		}
	    }
	}
	selectRow(rownum);

	myTableControl.update(true); // Update and repaint
	mainMenu.setfiltApplyImage();
	updateStatusBar();
    }

    public void updateStatusBar() {
	updateStatusBar("");
    }

    public void updateStatusBar(String status) {
	if (statusBar != null)
	    statusBar.updateDisplay(status);
    }

    public void autoSort() {
	if (myTableModel != null) {
	    // corresponding column for "distance" is column 10
	    if (myTableModel.sortedBy == 10 && Preferences.itself().sortAutomatic) {
		myTableModel.isSorted = false;
		myTableModel.sortTable(myTableModel.sortedBy, myTableModel.sortAscending);
	    } else
		myTableControl.repaint();
	}
    }
}
