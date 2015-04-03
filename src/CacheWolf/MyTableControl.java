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

import CacheWolf.controls.DataMover;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheType;
import CacheWolf.exp.OCLogExport;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.CWWrapper;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.UrlFetcher;
import ewe.fx.IconAndText;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.io.IOException;
import ewe.sys.Locale;
import ewe.sys.Vm;
import ewe.ui.Control;
import ewe.ui.DragContext;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.KeyEvent;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.PenEvent;
import ewe.ui.ProgressBarForm;
import ewe.ui.TableControl;
import ewe.ui.TableEvent;
import ewe.ui.mList;

/**
 * Implements the user interaction of the list view. Works together with myTableModel and TablePanel
 */
public class MyTableControl extends TableControl {
    public CacheDB cacheDB;

    public int clickedColumn = 0;

    private MenuItem miSetDestination, miCenter, miUnhideAddis;
    private MenuItem miOpenOnline, miOpenOffline, miLogOnline, miEditLog, miOpenGmaps;
    private MenuItem miDelete, miUpdate, miChangeBlack;
    private MenuItem miTickAll, miUntickAll;
    private MenuItem miSeparator;

    private Menu theMenu;
    private MenuItem[] menuItems;

    MyTableControl() {
	cacheDB = MainForm.profile.cacheDB;
	allowDragSelection = false; // allow only one row to be selected at one time

	miSeparator = new MenuItem("-"); //

	miSetDestination = new MenuItem(MyLocale.getMsg(345, "Go to these coordinates"));
	miCenter = new MenuItem(MyLocale.getMsg(1019, "Center")); //
	miUnhideAddis = new MenuItem(MyLocale.getMsg(1042, "Unhide Addis")); //

	miOpenOnline = new MenuItem(MyLocale.getMsg(1020, "Open in $browser online")); //
	miOpenOffline = new MenuItem(MyLocale.getMsg(1018, "Open in browser offline")); //
	miLogOnline = new MenuItem(MyLocale.getMsg(1052, "Log online in Browser")); //
	miEditLog = new MenuItem(MyLocale.getMsg(1055, "Change log (online)")); //

	miOpenGmaps = new MenuItem(MyLocale.getMsg(1053, "Open in Google maps online")); //

	miDelete = new MenuItem(MyLocale.getMsg(1012, "Delete selected")); //
	miUpdate = new MenuItem(MyLocale.getMsg(1014, "Update")); //
	miChangeBlack = new MenuItem(MyLocale.getMsg(1054, "Change Blacklist")); //

	miTickAll = new MenuItem(MyLocale.getMsg(1015, "Select all")); //
	miUntickAll = new MenuItem(MyLocale.getMsg(1016, "De-select all")); //

    }

    Menu getTheMenu() {
	if (menuItems == null) {
	    if (Preferences.itself().hasTickColumn) {
		menuItems = new MenuItem[] { miSetDestination, miCenter, miUnhideAddis, miSeparator, //
			miOpenOnline, miOpenOffline, miLogOnline, miEditLog, miOpenGmaps, miSeparator,//
			miDelete, miUpdate, miChangeBlack, miSeparator, //
			miTickAll, miUntickAll };
	    } else {
		menuItems = new MenuItem[] { miSetDestination, miCenter, miUnhideAddis, miSeparator, miOpenOnline, miOpenOffline, miLogOnline, miOpenGmaps };
	    }
	    theMenu = new Menu(menuItems, MyLocale.getMsg(1013, "With selection"));
	    setMenu(theMenu);
	}
	return theMenu;
    }

    public void penRightReleased(Point p) {
	if (cacheDB.size() > 0) { // No context menu when DB is empty
	    adjustAddiHideUnhideMenu();
	    menuState.doShowMenu(p, true, null); // direct call (not through doMenu) is neccesary because it will exclude the whole table

	}
    }

    public void penHeld(Point p) {
	if (cacheDB.size() > 0) // No context menu when DB is empty
	    adjustAddiHideUnhideMenu();
	menuState.doShowMenu(p, true, null);
    }

    public void onKeyEvent(KeyEvent ev) {
	if (ev.type == KeyEvent.KEY_PRESS && ev.target == this) {
	    if ((ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 1) {
		// <ctrl-a> gives 1, <ctrl-b> == 2
		// select all on <ctrl-a>
		setSelectForAll(true);
		ev.consumed = true;
	    } else {
		MainTab.itself.clearDetails();
		if (ev.key == IKeys.HOME)
		    // cursorTo(0,cursor.x+listMode,true);
		    MainTab.itself.tablePanel.selectRow(0);
		else if (ev.key == IKeys.END)
		    // cursorTo(model.numRows-1,cursor.x+listMode,true);
		    MainTab.itself.tablePanel.selectRow(model.numRows - 1);
		else if (ev.key == IKeys.PAGE_DOWN)
		    // cursorTo(java.lang.Math.min(cursor.y+ getOnScreen(null).height-1, model.numRows-1),cursor.x+listMode,true); 
		    // I don't know why this doesn't work: tablePanel.doScroll(IScroll.Vertical, IScroll.PageHigher, 1);
		    MainTab.itself.tablePanel.selectRow(java.lang.Math.min(cursor.y + getOnScreen(null).height - 1, model.numRows - 1));
		else if (ev.key == IKeys.PAGE_UP)
		    // cursorTo(java.lang.Math.max(cursor.y-getOnScreen(null).height+1, 0),cursor.x+listMode,true);
		    MainTab.itself.tablePanel.selectRow(java.lang.Math.max(cursor.y - getOnScreen(null).height + 1, 0));
		else if (ev.key == IKeys.ACTION || ev.key == IKeys.ENTER)
		    MainTab.itself.select(MainTab.DESCRIPTION_CARD);
		else if (ev.key == IKeys.DOWN)
		    MainTab.itself.tablePanel.selectRow(java.lang.Math.min(cursor.y + 1, model.numRows - 1));
		else if (ev.key == IKeys.UP)
		    MainTab.itself.tablePanel.selectRow(java.lang.Math.max(cursor.y - 1, 0));
		else if (ev.key == IKeys.LEFT && MainForm.itself.cacheTourVisible && cursor.y >= 0 && cursor.y < MainTab.itself.tablePanel.myTableModel.numRows)
		    MainForm.itself.getCacheTour().addCache(cacheDB.get(cursor.y).getWayPoint());
		else if (ev.key == IKeys.RIGHT) {
		    CacheHolder ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
		    if (ch.getPos().isValid()) {
			Navigate.itself.setDestination(ch);
			MainTab.itself.select(MainTab.GOTO_CARD);
		    }
		} else if (ev.key == 6) {
		    // (char)6 == ctrl + f
		    MainTab.itself.tablePanel.mainMenu.search();
		    MainTab.itself.tablePanel.refreshTable();
		} else
		    super.onKeyEvent(ev);
	    }
	} else
	    super.onKeyEvent(ev);
    }

    /** Set all caches either as selected or as deselected, depending on argument */
    private void setSelectForAll(boolean selectStatus) {
	MainForm.profile.setSelectForAll(selectStatus);
	MainTab.itself.tablePanel.refreshTable();
    }

    /** always select a whole row */
    public boolean isSelected(int pRow, int pCol) {
	return pRow == selection.y;
    }

    private void callExternalProgram(String program, String parameter) {
	try {
	    CWWrapper.exec(program, parameter);
	} catch (IOException ex) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1034, "Cannot start " + program + "!") + "\n" + ex.toString() + "\n" + MyLocale.getMsg(1035, "Possible reason:") + "\n" + MyLocale.getMsg(1036, "A bug in ewe VM, please be")
		    + "\n" + MyLocale.getMsg(1037, "patient for an update")).wait(FormBase.OKB);
	}
    }

    public void popupMenuEvent(Object selectedItem) {
	String url;
	CacheHolder mainCache;
	CacheHolderDetail chD;
	if (selectedItem == null)
	    return;
	CacheHolder ch;
	if (selectedItem == miTickAll) {
	    setSelectForAll(true);
	} else

	if (selectedItem == miUntickAll) {
	    setSelectForAll(false);
	} else

	if (selectedItem == miDelete) {
	    Vm.showWait(true);
	    // Count # of caches to delete
	    int allCount = 0;
	    int mainNonVisibleCount = 0;
	    int addiNonVisibleCount = 0;
	    int shouldDeleteCount = 0;
	    boolean deleteFiltered = true; // Bisheriges Verhalten
	    for (int i = cacheDB.size() - 1; i >= 0; i--) {
		CacheHolder currCache = cacheDB.get(i);
		if (currCache.isChecked) {
		    allCount++;
		    if (!currCache.isVisible()) {
			if (currCache.isAddiWpt()) {
			    addiNonVisibleCount++;
			} else {
			    mainNonVisibleCount++;
			}
		    }
		}
	    }
	    // Warn if there are ticked but invisible caches - and ask if they should be deleted too.
	    shouldDeleteCount = allCount;
	    if (addiNonVisibleCount + mainNonVisibleCount > 0) {
		if (new InfoBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(1029, "There are caches that are ticked but invisible.\n(Main caches: ") + mainNonVisibleCount + MyLocale.getMsg(1030, ", additional Waypoints: ") + addiNonVisibleCount
			+ ")\n" + MyLocale.getMsg(1031, "Delete them, too?")).wait(FormBase.YESB | FormBase.NOB) == FormBase.IDYES) {
		    deleteFiltered = true;
		} else {
		    deleteFiltered = false;
		    shouldDeleteCount = allCount - mainNonVisibleCount - addiNonVisibleCount;
		}
	    }
	    if (shouldDeleteCount > 0) {
		if (new InfoBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(1022, "Delete selected caches (") + shouldDeleteCount + MyLocale.getMsg(1028, ") ?")).wait(FormBase.YESB | FormBase.NOB) == FormBase.IDYES) {
		    DataMover dm = new DataMover();
		    dm.deleteCaches(false);
		    MainTab.itself.tablePanel.refreshTable();
		}
	    }
	    Vm.showWait(false);
	} else

	if (selectedItem == miUpdate) {
	    MainTab.itself.tablePanel.mainMenu.updateSelectedCaches();
	} else if (selectedItem == miChangeBlack) {
	    Vm.showWait(true);
	    try {
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
		    CacheHolder currCache = cacheDB.get(i);
		    if (currCache.isVisible() && currCache.isChecked) {
			if (currCache.isAddiWpt()) {
			    // currCache.setBlack(!currCache.is_black());
			} else {
			    currCache.setBlack(!currCache.isBlack());
			    currCache.save(); // to reflect it in xml and what takes time reading+writing
			}
		    }
		}
		// profile.saveIndex(pref,true);
		MainForm.profile.buildReferences();
		MainTab.itself.tablePanel.refreshTable();
	    } finally {
		Vm.showWait(false);
	    }
	    ;
	} else if (selectedItem == this.miSetDestination) {
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    if (ch.getPos().isValid()) {
		Navigate.itself.setDestination(ch);
		MainTab.itself.select(MainTab.GOTO_CARD);
	    }
	} else if (selectedItem == miCenter) {
	    if (MainTab.itself.tablePanel.getSelectedCache() < 0) {
		Preferences.itself().log("[myTableControl:popupMenuEvent] getSelectedCache() < 0");
		return;
	    }
	    CacheHolder thisCache = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    CWPoint cp = new CWPoint(thisCache.getPos());
	    if (!cp.isValid()) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4111, "Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM")).wait(FormBase.OKB);
	    } else {
		MainForm.itself.setCurCentrePt(cp);
	    }
	} else if (selectedItem == miUnhideAddis) {
	    // This toggles the "showAddis" Flag
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    ch.setShowAddis(!ch.showAddis());
	    if (ch.addiWpts.size() > 0) {
		MainTab.itself.tablePanel.refreshTable();
	    } else {
		// This should never occur, as we check prior to activating the menu if the
		// cache has addis. But just in case...
		new InfoBox(MyLocale.getMsg(4201, "Info"), MyLocale.getMsg(1043, "This cache has no additional waypoints.")).wait(FormBase.OKB);
	    }
	} else if (selectedItem == miOpenOnline) {
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    mainCache = ch;
	    if (ch.isAddiWpt() && (ch.mainCache != null)) {
		mainCache = ch.mainCache;
	    }
	    chD = mainCache.getCacheDetails(true);
	    url = chD.URL;
	    String wpName = mainCache.getOcCacheID();
	    if (clickedColumn == 14) {
		if (mainCache.isOC()) {
		    String s = OC.getGCWayPoint(ch.getCacheOwner());
		    if (s.length() > 0)
			url = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + s;
		} else {
		    if (wpName.length() > 0) {
			if (wpName.charAt(0) < 65)
			    wpName = mainCache.getOcCacheID().substring(1);
			url = "http://" + OC.getOCHostName(wpName) + "/viewcache.php?wp=" + wpName;
		    }
		}
	    }
	    if (url != null) {
		callExternalProgram(Preferences.itself().browser, url);
	    }
	} else if (selectedItem == miOpenGmaps) {
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    if (ch.getPos().isValid()) {
		String lat = "" + ch.getPos().getLatDeg(TransformCoordinates.DD);
		String lon = "" + ch.getPos().getLonDeg(TransformCoordinates.DD);
		String nameOfCache = UrlFetcher.encodeURL(ch.getCacheName(), false).replace('#', 'N').replace('@', '_');
		String language = Vm.getLocale().getString(Locale.LANGUAGE_SHORT, 0, 0);
		if (!Preferences.itself().language.equalsIgnoreCase("auto")) {
		    language = Preferences.itself().language;
		}
		url = "http://maps.google." + language + "/maps?q=" + nameOfCache + "@" + lat + "," + lon;
		callExternalProgram(Preferences.itself().browser, url);
		url = "http://www.geocaching.com/map/default.aspx?lat=" + lat + "&lng=" + lon;
		callExternalProgram(Preferences.itself().browser, url);
	    }
	} else if (selectedItem == miOpenOffline) {
	    ShowCacheInBrowser sc = new ShowCacheInBrowser();
	    sc.showCache(cacheDB.get(MainTab.itself.tablePanel.getSelectedCache()));
	} else if (selectedItem == miEditLog) {
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    mainCache = ch;
	    url = "";
	    if (ch.isAddiWpt() && (ch.mainCache != null)) {
		mainCache = ch.mainCache;
	    }
	    if (mainCache.isCacheWpt()) {
		chD = mainCache.getCacheDetails(false);
		if (chD != null) {
		    if (chD.OwnLog != null) {
			url = "http://www.geocaching.com/seek/log.aspx?LID=" + chD.OwnLogId;
			callExternalProgram(Preferences.itself().browser, url);
		    }
		}
	    }
	} else if (selectedItem == miLogOnline) {
	    ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	    mainCache = ch;
	    url = "";
	    if (ch.isAddiWpt() && (ch.mainCache != null)) {
		mainCache = ch.mainCache;
	    }
	    if (mainCache.isCacheWpt()) {
		chD = mainCache.getCacheDetails(false);
		String ownLogMessage = "";
		if (chD != null) {
		    if (chD.OwnLog != null) {
			// Cache schon im CW gelogged
			ownLogMessage = chD.OwnLog.getMessage();
			if (ownLogMessage.length() > 0) {
			    Vm.setClipboardText(mainCache.cacheStatus() + '\n' + "<br>" + ownLogMessage);
			}
		    }
		}
		if (mainCache.isOC()) {
		    if (chD != null) {
			url = chD.URL;
			if (url.indexOf("viewcache") >= 0) {
			    url = STRreplace.replace(url, "viewcache", "log");
			}
		    } else {
			// OC Url aus Cachenamen http://www.opencaching.de/log.php?wp=OC11871
			url = "http://" + OC.getOCHostName(mainCache.getWayPoint()) + "/log.php?wp=" + mainCache.getWayPoint();
		    }
		} else {
		    // GC und schon gelogged --> log bei OC eintragen, wenn auf OC-Index-Spalte geklickt
		    if (chD.OwnLogId.length() > 0) {
			String ocWpName = mainCache.getOcCacheID();
			if (ocWpName.length() > 0 && ocWpName.charAt(0) < 65) {
			    // OC log (already logged at GC but not at OC)
			    if (clickedColumn == 14) {
				OCLogExport.doOneLog(mainCache);
				MainTab.itself.tablePanel.refreshTable();
			    } else {
				// open OC logpage with Logtext in Clipboard
				Vm.setClipboardText(chD.OwnLog.getDate() + '\n' + "<br>" + ownLogMessage);
				if (ocWpName.length() > 1) {
				    if (ocWpName.charAt(0) < 65) {
					ocWpName = ocWpName.substring(1);
				    }
				    url = "http://" + OC.getOCHostName(ocWpName) + "/log.php?wp=" + ocWpName;
				}
			    }
			}
		    } else
			// bei GC loggen
			url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.GetCacheID();
		}

		if (url.length() > 0) {
		    callExternalProgram(Preferences.itself().browser, url);
		}

	    } else {
		if (mainCache.isCustomWpt() && mainCache.getWayPoint().startsWith("GC")) {
		    url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.GetCacheID();
		    callExternalProgram(Preferences.itself().browser, url);
		}
	    }
	}
    }

    // Overrides
    public void penDoubleClicked(Point where) {
	MainTab.itself.select(MainTab.DESCRIPTION_CARD);
    }

    public void onEvent(Event ev) {
	if (ev instanceof PenEvent && (ev.type == PenEvent.PEN_DOWN)) {
	    MainTab.itself.tablePanel.myTableModel.penEventModifiers = ((PenEvent) ev).modifiers;
	}

	super.onEvent(ev);
    }

    /**
     * Adjusting the menu item for hiding or unhiding additional waypoints. If the cache has no addis, then the menu is deactivated. If it has addis, then the menu text is adapted according to the current value of the property
     * <code>showAddis()</code>.
     * 
     */
    public void adjustAddiHideUnhideMenu() {
	if (MainTab.itself.tablePanel.getSelectedCache() < 0) {
	    return;
	}
	CacheHolder selCache = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
	if (selCache != null) {
	    // Depending if it has Addis and the ShowAddis-Flag the menu item to unhide
	    // addis is properly named and activated or disabled.
	    if (selCache.addiWpts.size() > 0) {
		miUnhideAddis.modifiers &= ~MenuItem.Disabled;
		if (!selCache.showAddis()) {
		    miUnhideAddis.setText(MyLocale.getMsg(1042, "Unhide Addis"));
		} else {
		    miUnhideAddis.setText(MyLocale.getMsg(1045, "Hide Addis"));
		}
	    } else {
		miUnhideAddis.setText(MyLocale.getMsg(1042, "Unhide Addis"));
		miUnhideAddis.modifiers |= MenuItem.Disabled;
	    }
	}
    }

    // /////////////////////////////////////////////////
    // Allow the caches to be dragged into a cachelist
    // /////////////////////////////////////////////////

    IconAndText imgDrag;
    String wayPoint;
    int row;

    public void startDragging(DragContext dc) {
	Point p = cellAtPoint(dc.start.x, dc.start.y, null);
	if (p == null) {
	    super.startDragging(dc);
	    return;
	}
	wayPoint = null;
	if (p.y >= 0) {
	    if (!MainForm.itself.cacheTourVisible) {
		dc.cancelled = true;
		return;
	    }
	    row = p.y;
	    CacheHolder ch = cacheDB.get(p.y);
	    wayPoint = ch.getWayPoint();
	    imgDrag = new IconAndText();
	    imgDrag.addColumn(CacheType.getTypeImage(ch.getType()));
	    imgDrag.addColumn(ch.getWayPoint());
	    dc.dragData = dc.startImageDrag(imgDrag, new Point(8, 8), this);
	} else
	    super.startDragging(dc);
    }

    public void stopDragging(DragContext dc) {
	if (wayPoint != null && !dc.cancelled) {
	    dc.stopImageDrag(true);
	    Point p = Gui.getPosInParent(this, getWindow());
	    p.x += dc.curPoint.x;
	    p.y += dc.curPoint.y;
	    Control c = getWindow().findChild(p.x, p.y);
	    if (c instanceof mList && c.text.equals("CacheList")) {
		if (MainForm.itself.getCacheTour().addCache(wayPoint)) {
		    c.repaintNow();
		    ((mList) c).makeItemVisible(((mList) c).itemsSize() - 1);
		}
	    }
	    MainTab.itself.tablePanel.selectRow(row);
	} else
	    super.stopDragging(dc);
    }

    public void dragged(DragContext dc) {
	if (wayPoint != null)
	    dc.imageDrag();
	else
	    super.dragged(dc);
    }

    public void cursorTo(int pRow, int pCol, boolean selectNew) {
	if (pRow != -2 && pCol != -2 && !canSelect(pRow, pCol))
	    return;
	cursor.set(pCol, pRow);
	if (selectNew) {
	    clearSelectedCells(oldExtendedSelection);
	    paintCells(null, oldExtendedSelection);
	    if (pRow != -2 && pCol != -2) {
		if (scrollToVisible(pRow, pCol))
		    repaintNow();
		addToSelection(Rect.buff.set(0, pRow, model.numCols, 1), true);
		// fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
		clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
		if (clickMode)
		    clicked(pRow, pCol);
		clickedFlags = 0;
	    }
	}
    }

    /**
     * this is only necessary to hinder the user to unselect
     */
    public void penReleased(Point p, boolean isDouble) {
	Point p2 = cellAtPoint(p.x, p.y, null);
	super.penReleased(p, isDouble);
	Rect sel = getSelection(null);
	if ((sel.height == 0 || sel.height == 0) && p2 != null)
	    cursorTo(p2.y, p2.x, true); // if the selection is gone -> reselect it

    }

    class myProgressBarForm extends ProgressBarForm {

	boolean isClosed = false;

	protected boolean canExit(int exitCode) {
	    isClosed = true;
	    return true;
	}

    }
}
