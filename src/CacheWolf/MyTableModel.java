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

import CacheWolf.controls.TableColumnChooser;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.LogList;
import CacheWolf.utils.DateFormat;
import CacheWolf.utils.MyLocale;
import ewe.fx.Color;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.mImage;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.IKeys;
import ewe.ui.TableCellAttributes;
import ewe.ui.TableModel;
import ewe.util.Comparer;
import ewe.util.Vector;

/**
 * Table model used to display the cache list.
 * Used by the table control in the first panel of CacheWolf.
 * 20061212 salzkammergut, patch to speed up scrolling,
 * Used MyLocale
 */
public class MyTableModel extends TableModel {
    public final String[] colHeaderNames = { " ", //
	    "?", //
	    MyLocale.getMsg(1000, "D"), //
	    MyLocale.getMsg(1001, "T"), //
	    MyLocale.getMsg(1002, "Waypoint"), //
	    "Name", //
	    MyLocale.getMsg(1004, "Location"), //
	    MyLocale.getMsg(1005, "Owner"), //
	    MyLocale.getMsg(1006, "Hidden"), //
	    MyLocale.getMsg(1007, "Status"), //
	    MyLocale.getMsg(1008, "Dist"), //
	    MyLocale.getMsg(1009, "Bear"), //
	    MyLocale.getMsg(1017, "S"), //
	    MyLocale.getMsg(1026, "#Rec"), //
	    MyLocale.getMsg(1027, "OC-Waypoint"), //
	    MyLocale.getMsg(1038, "S"), //
	    MyLocale.getMsg(1040, "N"), //
	    MyLocale.getMsg(1047, "A"), //
	    MyLocale.getMsg(1049, "DNF"), //
	    MyLocale.getMsg(1051, "Last synced"), //
	    MyLocale.getMsg(677, "PM"), //
	    MyLocale.getMsg(362, "solved"), //
    };

    // Colors for Cache status (BG unless otherwise stated)
    private static final Color COLOR_STATUS = new Color(206, 152, 255);
    private static final Color COLOR_FLAGED = new Color(255, 255, 0);
    private static final Color COLOR_FOUND = new Color(152, 251, 152);
    private static final Color COLOR_OWNED = new Color(135, 206, 235);
    private static final Color COLOR_AVAILABLE = new Color(255, 128, 0);
    private static final Color COLOR_ARCHIVED = new Color(200, 0, 0);
    private static final Color COLOR_SELECTED = new Color(141, 141, 141);
    private static final Color COLOR_DETAILS_LOADED = new Color(229, 206, 235);
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private Color lineColorBG = new Color(255, 255, 255);
    private Color lastColorBG = new Color(255, 255, 255);
    private Color lastColorFG = new Color(0, 0, 0);
    private int lastRow = -2;
    private CacheDB cacheDB;
    /** The max number of columns in the list view */
    public static final int N_COLUMNS = 22;
    /**
     * How the columns are mapped onto the list view.<br> 
     * If colMap[i]=j, it means that the element j (as per the list below) is visible in column i.<br> 
     * [0]TickBox, [1]Type, [2]Distance, [3]Terrain, [4]waypoint, [5]name, [6]coordinates, [7]owner,
     * [8]datehidden, [9]status, [10]distance, [11]bearing, [12] Size, [13] # of OC recommend. [14] OC index, 
     * [15] Solver exists, [16] Note exists, [17] # Additionals, [18] # DNF [19] Last Sync Date<br>
     * 
     **/
    private int[] colMap;
    /** The column widths corresponding to the list of columns above */
    private int[] colWidth;

    private static Image noFindLogs[] = new Image[4];
    public static mImage red, blue, yellow; // skull, green
    private Image checkboxTicked, checkboxUnticked;
    private mImage bug;
    private static mImage imgSortUp, imgSortDown;
    public boolean sortAscending = false;
    public int sortedBy = -1; // -1 don't sort
    public boolean isSorted = false; // true if "sort order indicators" should
    // be displayed
    private FontMetrics fm;
    // private mImage
    // picSizeMicro,picSizeSmall,picSizeReg,picSizeLarge,picSizeVLarge,picSizeNonPhysical;
    private mImage picHasSolver, picHasNotes;
    private mImage[] sizePics = new mImage[CacheSize.CW_TOTAL_SIZE_IMAGES];
    private mImage picIsPM;
    private mImage picIsSolved;
    /**
     * This is the modifier (Shift & Control key status) for Pen Events it is set in myTableControl.onEvent
     */
    public int penEventModifiers;
    public MyTableControl myTableControl;
    public boolean showExtraWptInfo = true;

    public MyTableModel(MyTableControl myTableControl) {
	super();
	cacheDB = MainForm.profile.cacheDB;
	this.fm = myTableControl.getFontMetrics();
	this.myTableControl = myTableControl;
	setColumnNamesAndWidths();
	numRows = cacheDB.size();
	// Dimension selrow = new Dimension(-1,1);
	// this.cursorSize = selrow;
	noFindLogs[0] = new Image("no_1_log.png");
	noFindLogs[1] = new Image("no_2_log.png");
	noFindLogs[2] = new Image("no_3_log.png");
	noFindLogs[3] = new Image("no_4_log.png");
	red = new mImage("red.png");
	red.transparentColor = Color.White;
	blue = new mImage("blue.png");
	blue.transparentColor = Color.White;
	// green = new mImage("green.png");green.transparentColor=Color.White;
	yellow = new mImage("yellow.png");
	yellow.transparentColor = Color.White;
	// skull = new
	// mImage("skull.png");skull.transparentColor=Color.DarkBlue;
	bug = new mImage("bug.png");
	bug.transparentColor = Color.DarkBlue;
	checkboxTicked = new Image("checkboxTicked.png");
	checkboxUnticked = new Image("checkboxUnticked.png");
	imgSortUp = new mImage("sortup.png");
	imgSortUp.transparentColor = Color.White;
	imgSortDown = new mImage("sortdown.png");
	imgSortDown.transparentColor = Color.White;

	for (byte i = 0; i < CacheSize.CW_TOTAL_SIZE_IMAGES; i++) {
	    sizePics[i] = new mImage(CacheSize.sizeImageForId(i));
	    sizePics[i].transparentColor = Color.White;
	}

	picHasSolver = new mImage("solver_exists.png");
	picHasSolver.transparentColor = Color.White;
	picHasNotes = new mImage("notes_exist.png");
	picHasNotes.transparentColor = Color.White;

	picIsPM = new mImage("isPM.png");
	picIsSolved = new mImage("edit.png");
    }

    /**
     * Sets the column names and widths from preferences
     * 
     */
    public void setColumnNamesAndWidths() {
	colMap = TableColumnChooser.str2Array(Preferences.itself().listColMap, 0, N_COLUMNS - 1, 0, -1);
	colWidth = TableColumnChooser.str2Array(Preferences.itself().listColWidth, 10, 1024, 50, colMap.length);
	numCols = colMap.length;
	clearCellAdjustments();
    }

    /**
     * Return the column widths as a comma delimited string for storing in the preferences
     * 
     * @return
     */
    public String getColWidths() {
	// Update the list with the current widths
	for (int col = 0; col < numCols; col++) {
	    colWidth[colMap[col]] = getColWidth(col);
	}
	clearCellAdjustments();
	// Convert to string
	StringBuffer sb = new StringBuffer(100);
	for (int i = 0; i < N_COLUMNS; i++) {
	    if (sb.length() != 0)
		sb.append(',');
	    sb.append(colWidth[i]);
	}
	return sb.toString();
    }

    public void updateRows() {
	Vector sortDB = new Vector();
	Vector notVisibleDB = new Vector();
	CacheHolder ch, addiWpt;
	// sort cacheDB:
	// - addi wpts are listet behind the main cache
	// - filtered caches are moved to the end
	int size = cacheDB.size();
	for (int i = 0; i < size; i++) {
	    ch = cacheDB.get(i);
	    if (!ch.isVisible()) {
		notVisibleDB.add(ch);
	    } else { // point is not filtered
		if (Preferences.itself().SortingGroupedByCache) {
		    if (ch.isAddiWpt()) { // unfiltered Addi Wpt
			// check if main wpt is filtered
			if (ch.mainCache != null) { // parent exists
			    if (!ch.mainCache.isVisible())
				sortDB.add(ch); // Unfiltered Addi Wpt with
			    // filtered Main Wpt, show it on
			    // its own
			    // else
			    // Main cache is not filtered, Addi will be added
			    // below main cache further down
			    // This case doesn't seem to be a problem. It occurs
			    // regularly, when
			    // filtered addis are unfiltered, so there is not
			    // need to log this case.
			} else { // Addi without main Cache
			    sortDB.add(ch);
			}
		    } else { // Main Wpt, not filtered. Check for Addis
			sortDB.add(ch);
			if (ch.hasAddiWpt()) {
			    for (int j = 0; j < ch.addiWpts.getCount(); j++) {
				addiWpt = (CacheHolder) ch.addiWpts.get(j);
				if (addiWpt.isVisible())
				    sortDB.add(addiWpt);
			    }
			}// if hasAddiWpt
		    } // if AddiWpt
		} else {
		    sortDB.add(ch);
		}
	    } // if filtered
	}
	// rebuild database
	cacheDB.rebuild(sortDB, notVisibleDB);
	this.numRows = sortDB.getCount();
    }

    /**
     * Method to set the row color of the table displaying the cache list, depending on different flags set to the cache.
     */
    /*
     * (non-Javadoc)
     * 
     * @see ewe.ui.TableModel#getCellAttributes(int, int, boolean, ewe.ui.TableCellAttributes)
     */
    public TableCellAttributes getCellAttributes(int row, int col, boolean isSelected, TableCellAttributes ta) {
	ta = super.getCellAttributes(row, col, isSelected, ta);
	ta.alignment = CellConstants.LEFT;
	ta.anchor = CellConstants.LEFT;
	// The default color of a line is white
	lineColorBG.set(COLOR_WHITE);
	// Determination of colors is only done for first column.
	// Other columns take same color.
	if (row >= 0) {
	    if (row == 0 || row != lastRow) {
		try {
		    // Now find out if the line should be painted in an other color.
		    // Selected lines are not considered, so far
		    CacheHolder ch = cacheDB.get(row);
		    if (ch != null) {
			if (ch.isOwned())
			    lineColorBG.set(COLOR_OWNED);
			else if (ch.isFound())
			    lineColorBG.set(COLOR_FOUND);
			else if (ch.isFlagged)
			    lineColorBG.set(COLOR_FLAGED);
			else if (ch.getStatus().indexOf(MyLocale.getMsg(319, "not found")) > -1)
			    lineColorBG.set(COLOR_STATUS);
			else if (Preferences.itself().debug && ch.detailsLoaded()) {
			    lineColorBG.set(COLOR_DETAILS_LOADED);
			}

			if (ch.isArchived()) {
			    if (lineColorBG.equals(COLOR_WHITE)) {
				lineColorBG.set(COLOR_ARCHIVED);
				ta.foreground = COLOR_WHITE;
			    } else {
				ta.foreground = COLOR_ARCHIVED;
			    }
			} else if (!ch.isAvailable()) {
			    if (lineColorBG.equals(COLOR_WHITE)) {
				lineColorBG.set(COLOR_AVAILABLE);
			    } else {
				ta.foreground = COLOR_AVAILABLE;
			    }
			}

			// Now, if a line is selected, blend the determined color
			// with the selection color.
			if (isSelected) {
			    mergeColor(lineColorBG, lineColorBG, COLOR_SELECTED);
			}
			ta.fillColor = lineColorBG;
			lastColorBG.set(ta.fillColor);
			lastColorFG.set(ta.foreground);
			lastRow = row;
		    }
		} catch (Exception e) {
		    Preferences.itself().log("[myTableModel:getCellAttributes]Ignored row=" + row + " lastRow=" + lastRow, e, true);
		}
		;
	    } else {
		// Here: We already had this row.
		// Take color computed for last column
		ta.fillColor = lastColorBG;
		ta.foreground = lastColorFG;
	    }
	} else if (row == -1 && colMap[col] == 0 && MainForm.profile.showBlacklisted()) {
	    ta.fillColor = Color.Black;
	    lastColorBG.set(ta.fillColor);
	}
	return ta;
    }

    /**
     * Determines the arithmetic mean value of two colors and stores the result in the third color.
     * 
     * @param colorMerged
     *            Resulting color
     * @param colorA
     *            First color to merge. May be same object as <code>colorMerged</code>.
     * @param colorB
     *            Second color to merge. May be same object as <code>colorMerged</code>.
     */
    private void mergeColor(Color colorMerged, Color colorA, Color colorB) {
	colorMerged.set((colorA.getRed() + colorB.getRed()) / 2, (colorA.getGreen() + colorB.getGreen()) / 2, (colorA.getBlue() + colorB.getBlue()) / 2);
    }

    public int calculateRowHeight(int row) {
	return java.lang.Math.max(18, charHeight + 4);
    }

    public int calculateColWidth(int col) {
	if (col == -1)
	    return 0;
	else if (col < numCols)
	    return colWidth[colMap[col]];
	else
	    return 0;
    }

    /**
     * Need to override this method with a null return to avoid getCellData being called twice on each access to a cell. For further reference see the Ewe source code.
     * 
     * @author skg
     */
    public Object getCellText(int row, int col) {
	return null;
    }

    public Object getCellData(int row, int col) {
	if (row == -1) {
	    if (colMap[col] == sortedBy && isSorted) {
		if (sortAscending) {
		    return new IconAndText(imgSortUp, colHeaderNames[colMap[col]], fm);
		    // return "^ "+colName[colMap[col]];
		} else {
		    return new IconAndText(imgSortDown, colHeaderNames[colMap[col]], fm);
		    // return "v "+colName[colMap[col]];
		}
	    } else {
		return colHeaderNames[colMap[col]];
	    }
	}
	try { // Access to row can fail if many caches are deleted
	    CacheHolder ch = cacheDB.get(row);
	    if (ch != null /* ch.isVisible() */) { // Check of visibility
		// needed here??
		switch (colMap[col]) { // Faster than using column names
		case 0: // Checkbox
		    if (ch.isChecked)
			return checkboxTicked;
		    else
			return checkboxUnticked;
		case 1: // Type
		    return CacheType.getTypeImage(ch.getType());
		case 2: // Difficulty;
		    if (!ch.isCacheWpt()) {
			return "";
		    } else {
			return CacheTerrDiff.longDT(ch.getDifficulty());
		    }
		case 3: // Terrain
		    if (!ch.isCacheWpt()) {
			return "";
		    } else {
			return CacheTerrDiff.longDT(ch.getTerrain());
		    }
		case 4: // Waypoint
		    if (showExtraWptInfo) {
			IconAndText iat = ch.getModificationIcon(fm);
			if (iat != null)
			    return iat;
		    }
		    return ch.getCode();
		case 5: // Cachename
			// Fast return for majority of case
		    if (!showExtraWptInfo || (ch.hasBugs() == false && ch.getNoFindLogs() == 0))
			return ch.getName();
		    // Now need more checks
		    IconAndText wpVal = new IconAndText();
		    if (ch.hasBugs() == true)
			wpVal.addColumn(bug);
		    if (ch.getNoFindLogs() > 0) {
			if (ch.getNoFindLogs() > noFindLogs.length)
			    wpVal.addColumn(noFindLogs[noFindLogs.length - 1]);
			else
			    wpVal.addColumn(noFindLogs[ch.getNoFindLogs() - 1]);
		    }
		    wpVal.addColumn(ch.getName());
		    return wpVal;
		case 6: // Location
		    return ch.getWpt().toString();
		case 7: // Owner
		    return ch.getOwner();
		case 8: // Date hidden
		    return ch.getHidden();
		case 9: // Status
		    return ch.getStatus();
		case 10: // Distance
		    return ch.getDistance();
		case 11: // Bearing
		    return ch.getBearing();
		case 12: // Size
		    if (ch.isAddiWpt()) {
			return "";
		    } else {
			return sizePics[CacheSize.cacheSize2ImageId(ch.getSize())];
		    }
		case 13: // OC / gcvote Bewertung
		    return ch.getRecommended();
		case 14: //
		    if (ch.isGC())
			return ch.getIdOC();
		    else {
			return OC.getGCWayPoint(ch.getOwner());
		    }
		case 15: // Is solver filled?
		    if (ch.hasSolver())
			return picHasSolver;
		    else
			return null;
		case 16: // Does note exist?
		    if (ch.hasNote())
			return picHasNotes;
		    else
			return null;
		case 17: // Number of Additional Waypoints;
		    if (ch.mainCache == null && ch.addiWpts.size() > 0) {
			return String.valueOf(ch.addiWpts.size());
		    } else {
			return "";
		    }
		case 18: // Number of DNF logs
		    if (ch.getNoFindLogs() > 0) {
			return String.valueOf(ch.getNoFindLogs());
		    } else {
			return "";
		    }
		case 19: // Last sync date
		    return DateFormat.formatLastSyncDate(ch.getLastSync(), "yyyy-MM-dd HH:mm");
		case 20: // PM
		    if (ch.isPremiumCache())
			return picIsPM;
		    else
			return null;
		case 21: // isSolved
		    if (ch.isSolved())
			return picIsSolved;
		    else
			return null;
		} // Switch
	    } // if
	} catch (Exception e) {
	    // Preferences.itself().log("[myTableModel:getCellData]Ignored", e,true);
	    return null;
	}
	return null;
    }

    //Overrides: penPressed(...) in TableModel
    public boolean penPressed(Point onTable, Point cell) {
	boolean retval = false;
	if (cell == null)
	    return false;
	try {
	    // Check whether the click is on the checkbox image
	    myTableControl.clickedColumn = colMap[cell.x];
	    if (cell.y >= 0 && colMap[cell.x] == 0) {
		MainForm.profile.selectionChanged = true;
		if ((penEventModifiers & IKeys.SHIFT) > 0) {
		    if (myTableControl.cursor.y >= 0) {
			// Second row being marked with shift key pressed
			if (myTableControl.cursor.y < cell.y)
			    toggleSelect(myTableControl.cursor.y + 1, cell.y, cell.x);
			else
			    toggleSelect(cell.y, myTableControl.cursor.y - 1, cell.x);
		    } else {
			// Remember this row as start of range, but don't toggle yet
		    }
		} else { // Single row marked
		    toggleSelect(cell.y, cell.y, cell.x);
		}
	    }
	    if (cell.y == -1) { // Hit a header => sort the table accordingly
		// cell.x is the physical column
		// but we have to sort by the column it is mapped into
		int mappedCol = colMap[cell.x];
		boolean sortvalue = true;
		if (mappedCol == 0)
		    sortvalue = !showExtraWptInfo;
		else if (mappedCol == sortedBy)
		    sortvalue = !sortAscending;
		sortTable(mappedCol, sortvalue);
		retval = true;
	    }
	} catch (NullPointerException npex) {
	    Preferences.itself().log("[myTableModel:Penpressed]", npex, true);
	    Vm.showWait(false);
	}
	return retval;
    }

    public void sortTable(int mappedCol, boolean howToDo) {
	sortedBy = mappedCol;
	if (mappedCol <= 0) {
	    // 0 is Tickbox header
	    // Hide/unhide the additional information about a waypoint such as
	    // travelbugs/number of notfound logs/yellow circle/red circle etc.
	    // This helps on small PDA screens
	    if (mappedCol == 0)
		showExtraWptInfo = howToDo;
	} else {
	    sortAscending = howToDo;
	    Vm.showWait(true);
	    // get selected Cache
	    Point a = myTableControl.getSelectedCell(null);
	    CacheHolder ch = null;
	    if ((a != null) && (a.y >= 0) && (a.y < cacheDB.size()))
		ch = cacheDB.get(a.y);
	    cacheDB.sort(new MyComparer(cacheDB, sortedBy, numRows), !sortAscending);
	    updateRows();
	    // = cacheDB.rebuild(sortedVector of ch,
	    // invisibleVector of ch)
	    // select previously selected Cache again
	    if (ch != null) {
		int rownum = MainForm.profile.getCacheIndex(ch.getCode());
		if (rownum >= 0)
		    myTableControl.cursorTo(rownum, 0, true);
	    }
	    this.isSorted = true;
	    Vm.showWait(false);
	}

	myTableControl.update(true);
	MainTab.itself.tablePanel.updateStatusBar();
    }

    /**
     * Toggle the select status for a group of caches. Addi waypoints are set to the same state as their main cache. (Exception: Their main cache is not in the range of toggled caches, then they are toggled independently.)
     * 
     * @param from
     *            index of first cache to toggle
     * @param to
     *            index of last cache to toggle
     * @param x
     *            is column of checkbox (does not have to be 0)
     */
    private void toggleSelect(int from, int to, int x) {
	CacheHolder ch;
	boolean singleRow = from == to;
	for (int j = from; j <= to; j++) {
	    boolean checkAddiWpts = false;
	    ch = cacheDB.get(j);
	    if (singleRow) {
		// If its a single row click, then toggle the cache. Remember to
		// toggle addis too, if there are.
		ch.isChecked = !ch.isChecked;
		checkAddiWpts = true;
	    } else {
		// If not a single row click...
		if (ch.isAddiWpt()) {
		    // Only toggle addis, if their main cache is not within the
		    // range that will be toggled
		    int mainIdx = cacheDB.getIndex(ch.mainCache);
		    if (mainIdx < from - 1 || mainIdx > to) {
			ch.isChecked = !ch.isChecked;
		    } else {
			// Otherwise the addis will be toggled along with their
			// main caches, so nothing is to do here.
		    }
		} else {
		    // If its a main cache, then toggle it and remember to
		    // toggle the addis, too.
		    ch.isChecked = !ch.isChecked;
		    checkAddiWpts = true;
		}
	    }
	    myTableControl.repaintCell(j, x);
	    // Now look for addi wpts.
	    if (checkAddiWpts) {
		CacheHolder addiWpt;
		int addiCount = ch.addiWpts.getCount();
		for (int i = 0; i < addiCount; i++) {
		    // This code will only run when the main cache
		    // has been toggled.
		    addiWpt = (CacheHolder) ch.addiWpts.get(i);
		    // Set all addi check states to the state of the
		    // main cache.
		    addiWpt.isChecked = ch.isChecked;
		    if (addiWpt.isVisible()) {
			myTableControl.repaintCell(cacheDB.getIndex(addiWpt), x);
		    }
		}
	    }

	}
    }

    // Overrides
    public void select(int row, int col, boolean selectOn) {
	myTableControl.cursorTo(row, col, true);
    }

}

/**
 * This class handles the sorting for most of the sorting tasks. If a cache is to be displayed in the table or not is handled in the table model
 * 
 * @see MyTableModel
 * @see DistComparer
 */
class MyComparer implements Comparer {
    Vector cacheDB;

    public MyComparer(CacheDB cacheDB, int colToCompare, int visibleSize) {
	// visibleSize=MainTab.itself.tbP.myMod.numRows;
	if (visibleSize < 2)
	    return;
	for (int i = visibleSize; i < cacheDB.size(); i++) {
	    CacheHolder ch = cacheDB.get(i);
	    ch.sort = "\uFFFF";
	}
	if (colToCompare == 1) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = String.valueOf(ch.getType());
	    }
	} else if (colToCompare == 2) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = String.valueOf(ch.getDifficulty());
	    }
	} else if (colToCompare == 3) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = String.valueOf(ch.getTerrain());
	    }
	} else if (colToCompare == 4) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getCode().toUpperCase();
	    }
	} else if (colToCompare == 5) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getName().trim().toLowerCase();
	    }
	} else if (colToCompare == 6) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getWpt().toString();
	    }
	} else if (colToCompare == 7) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getOwner().toLowerCase();
	    }
	} else if (colToCompare == 8) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getHidden();
	    }
	} else if (colToCompare == 9) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getStatus();
	    }
	} else if (colToCompare == 10) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		// CHECK Is the formatting correctly done?
		if (ch.kilom == -1.0) {
		    ch.sort = "\uFFFF";
		} else {
		    ch.sort = MyLocale.formatDouble(ch.kilom * 1000, "000000000000");
		}
	    }
	} else if (colToCompare == 11) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.getBearing().equals("?")) {
		    ch.sort = "\uFFFF";
		} else {
		    ch.sort = ch.getBearing();
		}
	    }

	} else if (colToCompare == 12) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = Integer.toString(ch.getSize());
	    }
	} else if (colToCompare == 13) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isOC()) {
		    ch.sort = MyLocale.formatLong(LogList.getScore(ch.getNumRecommended(), ch.getNumFoundsSinceRecommendation()), "000") + MyLocale.formatLong(ch.getNumRecommended(), "00000");
		} else {
		    if (Preferences.itself().useGCFavoriteValue) {
			ch.sort = MyLocale.formatLong(ch.getNumRecommended(), "000000") + "00000000";
		    } else {
			int gcVote = ch.getNumRecommended();
			if (gcVote < 100) {
			    ch.sort = MyLocale.formatLong(gcVote, "000") + "00000000";
			} else {
			    int votes = gcVote / 100;
			    gcVote = gcVote - 100 * votes;
			    ch.sort = MyLocale.formatLong(gcVote, "000") + MyLocale.formatLong(votes, "00000000");
			}
		    }
		}
	    }
	} else if (colToCompare == 14) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isGC())
		    if (ch.getIdOC().length() == 0)
			ch.sort = "\uFFFF";
		    else
			ch.sort = ch.getIdOC();
		else {
		    ch.sort = OC.getGCWayPoint(ch.getOwner());
		    if (ch.sort.length() == 0)
			ch.sort = "\uFFFF"; // ans Ende
		}
	    }
	} else if (colToCompare == 15) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.hasSolver()) {
		    ch.sort = "1";
		} else {
		    ch.sort = "2";
		}
	    }
	} else if (colToCompare == 16) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.hasNote()) {
		    ch.sort = "1";
		} else {
		    ch.sort = "2";
		}
	    }
	} else if (colToCompare == 17) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = MyLocale.formatLong(ch.addiWpts.size(), "000");
	    }
	} else if (colToCompare == 18) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = MyLocale.formatLong(ch.getNoFindLogs(), "000");
	    }
	} else if (colToCompare == 19) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		ch.sort = ch.getLastSync();
	    }
	} else if (colToCompare == 20) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isPremiumCache()) {
		    ch.sort = "1";
		} else {
		    ch.sort = "2";
		}
	    }
	} else if (colToCompare == 21) {
	    for (int i = 0; i < visibleSize; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isSolved()) {
		    ch.sort = "1";
		} else {
		    ch.sort = "2";
		}
	    }
	}
    }

    public int compare(Object o1, Object o2) {
	CacheHolder oo1 = (CacheHolder) o1;
	CacheHolder oo2 = (CacheHolder) o2;
	return oo1.sort.compareTo(oo2.sort);
    }
}
