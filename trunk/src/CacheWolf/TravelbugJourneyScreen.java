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

/**
 * A list to manage the travelbugs. Each row represents one @see TravelbugJourney.
 * The lower half of the screen which is separated from the top by a splitter bar,
 * contains four tabs: One for the travelbug, one for the source (where the travelbug 
 * was picked up), one for the destination (where the travelbug was dropped) and one
 * for the mission. These tabs are used for inputting data about the travelbug journey.
 * The travelbugs are read from file travelbugs.xml which is stored in the base directory.
 * When the screen is closed, all data is written back to the file.
 * @author salzkammergut
 */

import CacheWolf.controls.DateTimeChooser;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.controls.TableColumnChooser;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugJourney;
import CacheWolf.database.TravelbugJourneyList;
import CacheWolf.database.TravelbugList;
import CacheWolf.imp.GCImporter;
import CacheWolf.utils.CWWrapper;
import CacheWolf.utils.MyLocale;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.FontMetrics;
import ewe.fx.IImage;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormEvent;
import ewe.ui.Gui;
import ewe.ui.HtmlDisplay;
import ewe.ui.IKeys;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.MultiPanelEvent;
import ewe.ui.PanelSplitter;
import ewe.ui.PenEvent;
import ewe.ui.ScrollablePanel;
import ewe.ui.SplittablePanel;
import ewe.ui.TableCellAttributes;
import ewe.ui.TableControl;
import ewe.ui.TableModel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;

public class TravelbugJourneyScreen extends Form {

    /** The list control */
    private tbListControl tcTbJourneyList;
    /** The list model */
    private tbListTableModel modTbJourneyList;
    /** The actual journeys */
    private TravelbugJourneyList tblMyTravelbugJourneys;
    /** The panel for the lower half of the screen */
    private CellPanel lowerpane;
    private mInput inpName, inpTrackingNo, inpFromDate, inpFromProfile, inpFromWaypoint, inpToDate, inpToProfile, inpToWaypoint;
    private mLabel lblId;
    private mButton btnFromDate, btnToDate;
    private mCheckBox chkFromLogged, chkToLogged;
    private HtmlDisplay txtMission;
    private mTabbedPanel pnlTab;
    /** List of TBs in the current cache */
    private TravelbugList tblSrcCache;
    /** The currently selected row */
    private int selectedRow = -1;
    /** A label which holds the number of currently displayed travelbug journeys */
    private mLabel lblNumVisibleJourneys;
    private final Color RED = new Color(255, 0, 0);
    private int exitKeys[] = { 75009 };
    /**
     * A flag to track whether the current cache has to be saved because a travelbug
     * was added to or taken from it.
     */
    private boolean chDmodified = false;

    /** The current cache */
    private CacheHolderDetail chD;
    /** The base data of the current cache */
    private CacheHolder ch;
    /** The name of the current waypoint */
    private String waypoint = "";

    public TravelbugJourneyScreen() {
	CacheDB cacheDB = MainForm.profile.cacheDB;
	SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);
	CellPanel tablepane = split.getNextPanel();
	int curCacheNo = MainTab.itself.tablePanel.getSelectedCache();
	String cache = "";
	if (curCacheNo >= 0 && curCacheNo < cacheDB.size()) {
	    ch = cacheDB.get(curCacheNo);
	    cache = MyLocale.getMsg(6022, ": Current cache: ") + ch.getWayPoint() + " - " + ch.getCacheName();
	    waypoint = ch.getWayPoint();
	    chD = ch.getCacheDetails(true);
	    tblSrcCache = ch.getCacheDetails(true).Travelbugs;
	}
	title = "Travelbugs" + cache;
	tcTbJourneyList = new tbListControl();
	tcTbJourneyList.setTableModel(modTbJourneyList = new tbListTableModel());
	tablepane.addLast(new MyScrollBarPanel(tcTbJourneyList, ScrollablePanel.AlwaysShowVerticalScrollers), STRETCH, FILL);

	lowerpane = split.getNextPanel();

	pnlTab = new mTabbedPanel();
	pnlTab.extraControlsRight = lblNumVisibleJourneys = new mLabel("  0");
	// ------------------------------------------------
	// First Tab - Name & Tracking #
	// ------------------------------------------------
	CellPanel pnlName = new CellPanel();
	pnlName.addNext(new mLabel(MyLocale.getMsg(6025, "Name:")), DONTSTRETCH, DONTFILL);
	pnlName.addLast(inpName = new mInput(), HSTRETCH, HFILL);
	pnlName.addNext(new mLabel(MyLocale.getMsg(6026, "Tracking #:")), DONTSTRETCH, DONTFILL);
	pnlName.addLast(inpTrackingNo = new mInput(), HSTRETCH, HFILL);
	pnlName.addNext(new mLabel(MyLocale.getMsg(6027, "ID/GUID:")), DONTSTRETCH, DONTFILL);
	pnlName.addLast(lblId = new mLabel(""), HSTRETCH, HFILL);
	pnlTab.addCard(pnlName, MyLocale.getMsg(6028, "Name"), "Name");

	// ------------------------------------------------
	// Second Tab - Where was the TB picked up from
	// ------------------------------------------------
	CellPanel pnlFrom = new CellPanel();
	pnlFrom.addNext(new mLabel(MyLocale.getMsg(6029, "Profile/Cache:")), DONTSTRETCH, DONTFILL | WEST);
	pnlFrom.addNext(inpFromProfile = new mInput(), HSTRETCH, HFILL);
	pnlFrom.addLast(inpFromWaypoint = new mInput(), HSTRETCH, HFILL);

	pnlFrom.addNext(new mLabel(MyLocale.getMsg(6030, "Date found:")), DONTSTRETCH, DONTFILL | WEST);
	pnlFrom.addNext(inpFromDate = new mInput(), CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
	pnlFrom.addLast(btnFromDate = new mButton(new mImage("calendar.png")), DONTSTRETCH, DONTFILL | WEST);
	btnFromDate.modify(0, ControlConstants.TakesKeyFocus);

	pnlFrom.addNext(new mLabel(MyLocale.getMsg(6031, "Logged:")), DONTSTRETCH, DONTFILL | WEST);
	pnlFrom.addLast(chkFromLogged = new mCheckBox(""), DONTSTRETCH, DONTFILL | WEST);
	chkFromLogged.exitKeys = exitKeys;
	pnlFrom.addLast(new mLabel(""));

	pnlTab.addCard(pnlFrom, MyLocale.getMsg(6032, "From"), "From");

	// ------------------------------------------------
	// Third Tab - Where was the TB dropped
	// ------------------------------------------------
	CellPanel pnlTo = new CellPanel();
	pnlTo.addNext(new mLabel(MyLocale.getMsg(6029, "Profile/Cache:")), DONTSTRETCH, DONTFILL | WEST);
	pnlTo.addNext(inpToProfile = new mInput(), HSTRETCH, HFILL);
	pnlTo.addLast(inpToWaypoint = new mInput(), HSTRETCH, HFILL);

	pnlTo.addNext(new mLabel(MyLocale.getMsg(6033, "Date dropped:")), DONTSTRETCH, DONTFILL | WEST);
	pnlTo.addNext(inpToDate = new mInput(), CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
	// inpToDate.modifyAll(DisplayOnly,0);
	pnlTo.addLast(btnToDate = new mButton(new mImage("calendar.png")), DONTSTRETCH, DONTFILL | WEST);
	btnToDate.modify(0, ControlConstants.TakesKeyFocus);
	// pnlTo.addLast(new mLabel(""));

	pnlTo.addNext(new mLabel(MyLocale.getMsg(6031, "Logged:")), DONTSTRETCH, DONTFILL | WEST);
	pnlTo.addLast(chkToLogged = new mCheckBox(""), DONTSTRETCH, DONTFILL | WEST);
	chkToLogged.exitKeys = exitKeys;
	pnlTo.addLast(new mLabel(""));

	pnlTab.addCard(pnlTo, MyLocale.getMsg(6034, "To"), "To");

	// ------------------------------------------------
	// Last Panel - TB Mission
	// ------------------------------------------------
	CellPanel pnlDest = new CellPanel();
	pnlDest.addLast(new mLabel(MyLocale.getMsg(6035, "Mission:")));
	pnlDest.addLast(txtMission = new HtmlDisplay(), STRETCH, FILL);
	txtMission.rows = 3;
	pnlTab.addCard(pnlDest, MyLocale.getMsg(6036, "Mission"), "Mission");

	lowerpane.addLast(pnlTab, STRETCH, FILL);

	split.setSplitter(PanelSplitter.AFTER | PanelSplitter.HIDDEN, PanelSplitter.BEFORE | PanelSplitter.HIDDEN, 0);
	addLast(split, STRETCH, FILL);
	// setPreferredSize(MyLocale.getScreenWidth()<=240?240:MyLocale.getScreenWidth()*2/3,240);

	tblMyTravelbugJourneys = new TravelbugJourneyList();
	tblMyTravelbugJourneys.readTravelbugsFile();
	modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
	// Get the columns to display and their widths from preferences
	modTbJourneyList.columnMap = TableColumnChooser.str2Array(Preferences.itself().travelbugColMap, 0, 11, 0, -1);
	modTbJourneyList.colWidth = TableColumnChooser.str2Array(Preferences.itself().travelbugColWidth, 10, 1024, 50, -1);
	modTbJourneyList.numCols = modTbJourneyList.columnMap.length;

	modTbJourneyList.select(0, 12, true);
	/* Restore the saved setting about showing only non-logged bugs */
	if (Preferences.itself().travelbugShowOnlyNonLogged) {
	    tcTbJourneyList.toggleNonLogged();
	}
	updateNumBugs();
    }

    /** Indicate the number of journeys currently displayed */
    private void updateNumBugs() {
	lblNumVisibleJourneys.setText("" + modTbJourneyList.numRows);
	lblNumVisibleJourneys.repaint();
    }

    /** The control which had the last focus */
    private Control currentControl;

    public void onEvent(Event ev) {
	// Update the table from the input form
	if ((ev instanceof MultiPanelEvent || ev instanceof ControlEvent || ev instanceof DataChangeEvent) && selectedRow != -1 && selectedRow < tblMyTravelbugJourneys.size()) {
	    TravelbugJourney tbj = tblMyTravelbugJourneys.getTBJourney(selectedRow);
	    if (currentControl == inpName)
		tbj.getTb().setName(inpName.getText());
	    else if (currentControl == inpTrackingNo)
		tbj.getTb().setTrackingNo(inpTrackingNo.getText());
	    else if (currentControl == inpFromProfile)
		tbj.setFromProfile(inpFromProfile.getText());
	    else if (currentControl == inpFromWaypoint)
		tbj.setFromWaypoint(inpFromWaypoint.getText());
	    else if (currentControl == inpFromDate)
		tbj.setFromDate(inpFromDate.getText());
	    else if (currentControl == chkFromLogged)
		tbj.setFromLogged(chkFromLogged.state);
	    else if (currentControl == inpToProfile)
		tbj.setToProfile(inpToProfile.getText());
	    else if (currentControl == inpToWaypoint)
		tbj.setToWaypoint(inpToWaypoint.getText());
	    else if (currentControl == inpToDate)
		tbj.setToDate(inpToDate.getText());
	    else if (currentControl == chkToLogged)
		tbj.setToLogged(chkToLogged.state);
	    // else if (ev.target==txtMission) tb.setMission(txtMission.getText());
	    tcTbJourneyList.repaint();
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED && selectedRow != -1) {
	    if (ev.target == inpTrackingNo) {
		pnlTab.selectNextTab(true, true);
		Gui.takeFocus(inpFromProfile, ControlConstants.ByKeyboard);
		pnlTab.repaint();
	    }
	    if (ev.target == inpFromDate)
		Gui.takeFocus(chkFromLogged, ControlConstants.ByKeyboard);
	    if (ev.target == inpToDate)
		Gui.takeFocus(chkToLogged, ControlConstants.ByKeyboard);
	    if (ev.target == btnFromDate || ev.target == btnToDate) {
		mInput inpDate = ev.target == btnFromDate ? inpFromDate : inpToDate;
		DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
		dc.title = MyLocale.getMsg(328, "Date found");
		dc.setPreferredSize(240, 240);
		String foundDate = inpDate.getText();
		Time t = new Time();
		try {
		    t.parse(foundDate, "y-M-d H:m");
		} catch (IllegalArgumentException e) {
		    try {
			t.parse(foundDate, "y-M-d");
		    } catch (IllegalArgumentException e1) {
			// Can't parse date - should not happen
		    }
		}
		;
		dc.reset(t);
		if (dc.execute() == ewe.ui.FormBase.IDOK) {
		    inpDate.setText(Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00") + " " + dc.time);
		    if (ev.target == btnFromDate) {
			tblMyTravelbugJourneys.getTBJourney(selectedRow).setFromDate(inpDate.getText());
			Gui.takeFocus(chkFromLogged, ControlConstants.ByKeyboard);
		    } else {
			tblMyTravelbugJourneys.getTBJourney(selectedRow).setToDate(inpDate.getText());
			Gui.takeFocus(chkToLogged, ControlConstants.ByKeyboard);
		    }
		    tcTbJourneyList.repaint();
		}
	    }
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.EXITED) {
	    pnlTab.selectNextTab(true, true);
	    if (ev.target == chkFromLogged)
		Gui.takeFocus(inpToProfile, ControlConstants.ByKeyboard);
	    if (ev.target == chkToLogged)
		Gui.takeFocus(txtMission, ControlConstants.ByKeyboard);
	}
	// The user closed the travelbugs screen
	if (ev instanceof FormEvent && ev.type == FormEvent.CLOSED && chD != null) {
	    tblMyTravelbugJourneys.saveTravelbugsFile();
	    tblMyTravelbugJourneys.clear();
	    // Save the flag about showing non-logged journeys only
	    boolean old = Preferences.itself().travelbugShowOnlyNonLogged;
	    Preferences.itself().travelbugShowOnlyNonLogged = (tcTbJourneyList.mnuToggleList.modifiers & MenuItem.Checked) == MenuItem.Checked;
	    String travelbugColWidth = modTbJourneyList.getColWidths();
	    // If the preferences changed, save the pref.xml file
	    Vm.showWait(true);
	    if (!Preferences.itself().travelbugColWidth.equals(travelbugColWidth) || old != Preferences.itself().travelbugShowOnlyNonLogged) {
		Preferences.itself().travelbugColWidth = travelbugColWidth;
		Preferences.itself().savePreferences();
	    }
	    // If the list of travelbugs in the cache was modified, we need to save the cache too
	    if (chDmodified) {
		ch.setHas_bugs(chD.Travelbugs.size() > 0);
		ch.save();
	    }
	    Vm.showWait(false);
	    chD = null;
	}
	updateNumBugs();
	currentControl = Gui.focusedControl();
    }

    // ==============================================================
    // tbListTableModel
    // ==============================================================
    class tbListTableModel extends TableModel {
	private FontMetrics fm;
	private Image imgRed;

	tbListTableModel() {

	    fillToEqualHeights = true;
	    allRowsSameSize = true;
	    hasRowHeaders = false;
	    // shadeAlternateRows=true;
	    cursorSize = new Dimension(12, 1);
	    clipData = true;
	    fm = getFontMetrics();
	    // A red dot indicates that the journey has not been completely logged
	    imgRed = new Image("red.png");
	}

	private int colWidth[];
	private int columnMap[];

	public Object getCellText(int row, int col) {
	    return null;
	}

	public Object getCellData(int row, int col) {
	    if (row == -1) {
		return TravelbugJourney.getElementNameByNumber(columnMap[col]);
	    } else {
		int map = columnMap[col];
		// If we have not yet logged the from or the to, a red dot is placed in front of the first item
		if (col == 0 && (!tblMyTravelbugJourneys.getTBJourney(row).getFromLogged() || !tblMyTravelbugJourneys.getTBJourney(row).getToLogged())) {
		    // Is it a column with a checkbox?
		    if (map != 7 && map != 11)
			return new IconAndText((IImage) imgRed, (String) tblMyTravelbugJourneys.getTBJourney(row).getElementByNumber(map), fm);
		    else { // Checkbox - special treatment
			IconAndText iat = new IconAndText(imgRed, "", fm);
			iat.addColumn(tblMyTravelbugJourneys.getTBJourney(row).getElementByNumber(map));
			return iat;
		    }
		} else
		    return tblMyTravelbugJourneys.getTBJourney(row).getElementByNumber(map);
	    }
	}

	public int calculateRowHeight(int row) {
	    return charHeight + 2;
	}

	public int calculateColWidth(int col) {
	    if (col == -1)
		return 0;
	    else if (col < numCols)
		return colWidth[columnMap[col]];
	    else
		return 0;
	}

	public TableCellAttributes getCellAttributes(int row, int col, boolean isSelected, TableCellAttributes ta) {
	    ta = super.getCellAttributes(row, col, isSelected, ta);
	    ta.alignment = CellConstants.LEFT;
	    ta.anchor = CellConstants.LEFT;
	    // Color the elements red, if we have not yet logged
	    if (row >= 0)
		switch (columnMap[col]) {
		case 6: // fromDate
		    if (!tblMyTravelbugJourneys.getTBJourney(row).getFromLogged())
			ta.foreground = RED;
		    break;
		case 10: // toDate
		    if (!tblMyTravelbugJourneys.getTBJourney(row).getToLogged())
			ta.foreground = RED;
		    break;
		}
	    return ta;
	}

	private void showFields(TravelbugJourney tbj) {
	    inpName.setText(tbj.getTb().getName());
	    inpTrackingNo.setText(tbj.getTb().getTrackingNo());
	    lblId.setText(tbj.getTb().getGuid());
	    inpFromProfile.setText(tbj.getFromProfile());
	    inpFromWaypoint.setText(tbj.getFromWaypoint());
	    inpFromDate.setText(tbj.getFromDate());
	    chkFromLogged.setState(tbj.getFromLogged());
	    inpToProfile.setText(tbj.getToProfile());
	    inpToWaypoint.setText(tbj.getToWaypoint());
	    inpToDate.setText(tbj.getToDate());
	    chkToLogged.setState(tbj.getToLogged());
	    txtMission.setHtml(tbj.getTb().getMission());
	}

	private boolean sortAsc = false;
	private int sortedBy = -1;
	private int lastRow = -1;
	public int penEventModifiers;

	public boolean penPressed(Point onTable, Point cell) {
	    boolean retval = false;
	    if (cell != null && cell.y == -1) { // Hit a header => sort the table accordingly
		Vm.showWait(true);
		if (cell.x == sortedBy)
		    sortAsc = !sortAsc;
		else
		    sortAsc = false;
		sortedBy = cell.x;
		// Check whether the list only shows non-logged journeys. If so, a subset
		// of the table must be sorted
		if ((tcTbJourneyList.mnuToggleList.modifiers & MenuItem.Checked) == MenuItem.Checked) {
		    tblMyTravelbugJourneys.sortFirstHalf(columnMap[cell.x], sortAsc, modTbJourneyList.numRows);
		} else { // Showing all journeys - sort the full table
		    tblMyTravelbugJourneys.sort(columnMap[cell.x], sortAsc);
		}
		tcTbJourneyList.repaint();
		Vm.showWait(false);
		retval = true;
	    } else if (cell != null && cell.y >= 0 && (penEventModifiers & IKeys.SHIFT) > 0) {
		// A range of rows can be marked by shift-click on the first and last row
		if (lastRow != -1) { // Second row being marked with shift key pressed
		    if (lastRow < cell.y)
			toggleSelect(lastRow, cell.y);
		    else
			toggleSelect(cell.y, lastRow);
		    lastRow = -1;
		    retval = true;
		} else { // Remember this row as start of range, but don't toggle yet
		    lastRow = cell.y;
		}
	    } else { // Single row marked
		lastRow = -1;
	    }
	    return retval;
	}

	/** Select a range of rows */
	private void toggleSelect(int fromRow, int toRow) {
	    tcTbJourneyList.clearSelection(null);
	    tcTbJourneyList.addToSelection(new Rect(0, fromRow, numCols, toRow - fromRow + 1), false, true);
	}

	/**
	 * Return the column widths as a comma delimited string for storing in the preferences
	 * 
	 * @return
	 */
	private String getColWidths() {
	    // Update the list with the current widths
	    for (int col = 0; col < numCols; col++) {
		colWidth[columnMap[col]] = getColWidth(col);
	    }
	    // Convert to string
	    StringBuffer sb = new StringBuffer(40);
	    for (int i = 0; i < colWidth.length; i++) {
		if (sb.length() != 0)
		    sb.append(',');
		sb.append(colWidth[i]);
	    }
	    return sb.toString();
	}
    }

    // ==============================================================
    // tbListControl
    // ==============================================================
    class tbListControl extends TableControl {
	private MenuItem mnuNewTB, mnuDeleteTB, mnuGetMission, mnuOpenOnline, mnuDropTB, mnuPickupTB, mnuDeleteTBs;
	public MenuItem mnuToggleList;
	private Menu mnuFullMenu, mnuDeleteMenu;

	tbListControl() {
	    MenuItem[] TBMenuItems = new MenuItem[10];
	    TBMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(6040, "Pick up TB from current cache"));
	    TBMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6041, "Drop TB in cache"));
	    TBMenuItems[2] = new MenuItem("-");
	    TBMenuItems[3] = mnuNewTB = new MenuItem(MyLocale.getMsg(6042, "New Travelbug"));
	    TBMenuItems[4] = mnuDeleteTB = new MenuItem(MyLocale.getMsg(6043, "Delete Travelbug"));
	    TBMenuItems[5] = new MenuItem("-");
	    TBMenuItems[6] = mnuGetMission = new MenuItem(MyLocale.getMsg(6044, "Get Mission"));
	    TBMenuItems[7] = mnuOpenOnline = new MenuItem(MyLocale.getMsg(6045, "Open on-line"));
	    TBMenuItems[8] = new MenuItem("-");
	    TBMenuItems[9] = mnuToggleList = new MenuItem(MyLocale.getMsg(6046, "Show only not logged"));
	    mnuFullMenu = new Menu(TBMenuItems, "");
	    // A second pop-up menu with only one entry, if a range of rows is selected
	    MenuItem[] TBMenuItemsDel = new MenuItem[1];
	    TBMenuItemsDel[0] = mnuDeleteTBs = new MenuItem(MyLocale.getMsg(6047, "Delete selected Travelbugs"));
	    mnuDeleteMenu = new Menu(TBMenuItemsDel, "");
	    mnuDropTB.modifiers |= MenuItem.Disabled;
	    mnuDeleteTB.modifiers |= MenuItem.Disabled;
	    mnuGetMission.modifiers |= MenuItem.Disabled;
	    mnuOpenOnline.modifiers |= MenuItem.Disabled;
	}

	public void onEvent(Event ev) {
	    Rect sel = getSelection(null);
	    if (sel.y < tblMyTravelbugJourneys.size()) {
		mnuDeleteTB.modifiers &= ~MenuItem.Disabled;
		mnuGetMission.modifiers &= ~MenuItem.Disabled;
		mnuOpenOnline.modifiers &= ~MenuItem.Disabled;
		if (tblMyTravelbugJourneys.getTBJourney(sel.y).inMyPosession())
		    mnuDropTB.modifiers &= ~MenuItem.Disabled;
		else
		    mnuDropTB.modifiers |= MenuItem.Disabled;
	    } else {
		mnuDeleteTB.modifiers |= MenuItem.Disabled;
		mnuGetMission.modifiers |= MenuItem.Disabled;
		mnuOpenOnline.modifiers |= MenuItem.Disabled;
	    }
	    // If more than one row is selected, show the limited pop-up menu
	    if (sel.height > 1)
		setMenu(mnuDeleteMenu);
	    else
		setMenu(mnuFullMenu);
	    if (ev instanceof PenEvent)
		modTbJourneyList.penEventModifiers = ((PenEvent) ev).modifiers;
	    super.onEvent(ev);
	}

	public void penRightReleased(Point p) {
	    menuState.doShowMenu(p, true, null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}

	public void penHeld(Point p) {
	    menuState.doShowMenu(p, true, null);
	}

	public void popupMenuEvent(Object selectedItem) {
	    if (selectedItem == mnuPickupTB) {
		Travelbug tb = TravelbugPickup.pickupTravelbug(tblSrcCache);
		if (tb != null) {
		    chDmodified = true;
		    tblMyTravelbugJourneys.addTbPickup(tb, MainForm.profile.name, waypoint);
		    modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
		    tcTbJourneyList.repaint();
		}
	    }
	    if (selectedItem == mnuDropTB) {
		if (selectedRow >= 0 && selectedRow < modTbJourneyList.numRows) {
		    Travelbug tb = tblMyTravelbugJourneys.getTBJourney(selectedRow).getTb();
		    chD.Travelbugs.add(tb);
		    tblMyTravelbugJourneys.addTbDrop(tb, MainForm.profile.name, waypoint);
		    chDmodified = true;
		    ch.setHas_bugs(true);
		}
		repaint();
	    }
	    if (selectedItem == mnuNewTB) {
		TravelbugJourney tbj = new TravelbugJourney("New");
		tbj.setFromProfile(MainForm.profile.name);
		tbj.setFromWaypoint(waypoint);
		tblMyTravelbugJourneys.add(tbj);
		modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
		cursorTo(tblMyTravelbugJourneys.size() - 1, 1, true);
		tcTbJourneyList.repaint();
	    }
	    if (selectedItem == mnuDeleteTB && selectedRow >= 0) {
		tblMyTravelbugJourneys.remove(selectedRow);
		modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
		if (selectedRow > 0)
		    cursorTo(selectedRow - 1, 0, true);
		else
		    modTbJourneyList.showFields(new TravelbugJourney(""));
		tcTbJourneyList.repaint();
	    }
	    /* Delete a group of travelbugs which have been marked with Shift-Click */
	    if (selectedItem == mnuDeleteTBs) {
		Rect sel = getSelection(null);
		for (int i = 0; i < sel.height; i++)
		    tblMyTravelbugJourneys.remove(sel.y);
		modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
		if (sel.y < modTbJourneyList.numRows)
		    cursorTo(sel.y, 0, true);
		else
		    modTbJourneyList.showFields(new TravelbugJourney(""));
		tcTbJourneyList.repaint();
	    }
	    if (selectedItem == mnuGetMission && selectedRow > -1) {
		TravelbugJourney tbj = tblMyTravelbugJourneys.getTBJourney(selectedRow);
		GCImporter spider = new GCImporter();
		Vm.showWait(true);

		// if we have an ID, get mission by ID
		if (tbj.getTb().getGuid().length() != 0) {
		    tbj.getTb().setMission(spider.getBugMissionByGuid(tbj.getTb().getGuid()));
		} else {
		    // try to get mission and name by tracking number
		    boolean suceeded = false;
		    if (tbj.getTb().getTrackingNo().length() != 0) {
			suceeded = spider.getBugMissionAndNameByTrackNr(tbj.getTb());
		    }
		    // if this has't worked, try to get ID by name
		    if (!suceeded) {
			tbj.getTb().setGuid(spider.getBugId(tbj.getTb().getName().trim()));
			// if we have an ID now, get mission by ID
			if (tbj.getTb().getGuid().length() != 0) {
			    tbj.getTb().setMission(spider.getBugMissionByGuid(tbj.getTb().getGuid()));
			}
		    }
		}

		spider.setOldGCLanguage();

		Vm.showWait(false);
		tcTbJourneyList.repaint();
		txtMission.setHtml(tbj.getTb().getMission());
		inpName.setText(tbj.getTb().getName());
		lblId.setText(tbj.getTb().getGuid());
		lowerpane.repaint();
	    }
	    if (selectedItem == mnuOpenOnline && selectedRow >= 0) {
		TravelbugJourney tbj = tblMyTravelbugJourneys.getTBJourney(selectedRow);
		GCImporter spider = new GCImporter();
		Vm.showWait(true);
		// First check whether ID is set, if not get it
		if (tbj.getTb().getGuid().length() == 0)
		    tbj.getTb().setGuid(spider.getBugId(tbj.getTb().getName()));
		if (tbj.getTb().getGuid().length() != 0) {
		    Vm.showWait(false);
		    try {
			String s;
			if (tbj.getTb().getGuid().length() > 10)
			    s = "http://www.geocaching.com/track/details.aspx?guid=" + tbj.getTb().getGuid();
			else
			    s = "http://www.geocaching.com/track/details.aspx?id=" + tbj.getTb().getGuid();

			CWWrapper.exec(Preferences.itself().browser, s);
			Preferences.itself().log("Executed: \"" + Preferences.itself().browser + "\" \"" + s + "\"");
		    } catch (Exception ioex) {
			// Preferences.itself().log("Ignored Exception", ioex, true);
		    }
		}
		spider.setOldGCLanguage();
	    }
	    if (selectedItem == mnuToggleList) {
		toggleNonLogged();
	    }
	    updateNumBugs();
	}

	/** Toggle between displaying all journeys or just those which still need to be logged */
	public void toggleNonLogged() {
	    mnuToggleList.modifiers ^= MenuItem.Checked;
	    if ((mnuToggleList.modifiers & MenuItem.Checked) == MenuItem.Checked) {
		// First sort the non-logged items to the top
		tblMyTravelbugJourneys.sort(TravelbugJourney.BOTHLOGGED, false);
		// modListTable.numRows=tblMyTravelbugJourneys.size();
		modTbJourneyList.numRows = tblMyTravelbugJourneys.countNonLogged();
	    } else {
		modTbJourneyList.numRows = tblMyTravelbugJourneys.size();
	    }
	    tcTbJourneyList.repaint();
	}

	public void cursorTo(int row, int col, boolean selectNew) {
	    super.cursorTo(row, col, selectNew);
	    selectedRow = row;
	    if (row >= 0) {
		modTbJourneyList.showFields(tblMyTravelbugJourneys.getTBJourney(row));
	    } else {
		modTbJourneyList.showFields(new TravelbugJourney(""));
	    }
	}
    }

}
