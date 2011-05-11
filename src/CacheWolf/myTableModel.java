/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

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

import ewe.fx.Color;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.mImage;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.IKeys;
import ewe.ui.TableCellAttributes;
import ewe.ui.TableModel;
import ewe.util.Vector;
import ewe.util.mString;

/**
 * Table model used to display the cache list. Used by the table control in the first panel of CacheWolf. 20061212 salzkammergut, patch to speed up scrolling, Used MyLocale
 */
public class myTableModel extends TableModel {

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
	private static final Time lastSyncWorker = new Time();
	private int lastRow = -2;
	private CacheDB cacheDB;
	/** The max number of columns in the list view */
	public static final int N_COLUMNS = 20;
	/**
	 * How the columns are mapped onto the list view. If colMap[i]=j, it means that the element j (as per the list below) is visible in column i. [0]TickBox, [1]Type, [2]Distance, [3]Terrain, [4]waypoint, [5]name, [6]coordinates, [7]owner, [8]datehidden,
	 * [9]status, [10]distance, [11]bearing, [12] Size, [13] # of OC recommend. [14] OC index, [15] Solver exists, [16] Note exists, [17] # Additionals, [18] # DNF [19] Last Sync Date
	 * 
	 * Attention: When adding columns here, also add a default width in Preferences.listColWidth
	 */
	private int[] colMap;
	/** The column widths corresponding to the list of columns above */
	private int[] colWidth;
	private String[] colName = { " ", "?", MyLocale.getMsg(1000, "D"), MyLocale.getMsg(1001, "T"), MyLocale.getMsg(1002, "Waypoint"), "Name", MyLocale.getMsg(1004, "Location"), MyLocale.getMsg(1005, "Owner"), MyLocale.getMsg(1006, "Hidden"),
			MyLocale.getMsg(1007, "Status"), MyLocale.getMsg(1008, "Dist"), MyLocale.getMsg(1009, "Bear"), MyLocale.getMsg(1017, "S"), MyLocale.getMsg(1026, "#Rec"), MyLocale.getMsg(1027, "OC-IDX"), MyLocale.getMsg(1038, "S"),
			MyLocale.getMsg(1040, "N"), MyLocale.getMsg(1047, "A"), MyLocale.getMsg(1049, "DNF"), MyLocale.getMsg(1051, "Last synced") };

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
	/**
	 * This is the modifier (Shift & Control key status) for Pen Events it is set in myTableControl.onEvent
	 */
	public int penEventModifiers;
	public myTableControl tcControl;
	public boolean showExtraWptInfo = true;

	public myTableModel(myTableControl tc, FontMetrics fm) {
		super();
		cacheDB = Global.getProfile().cacheDB;
		this.fm = fm;
		tcControl = tc;
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
		bug = new mImage("bug_table.png");
		bug.transparentColor = Color.DarkBlue;
		checkboxTicked = new Image("checkboxTicked.png");
		checkboxUnticked = new Image("checkboxUnticked.png");
		imgSortUp = new mImage("sortup.png");
		imgSortUp.transparentColor = Color.White;
		imgSortDown = new mImage("sortdown.png");
		imgSortDown.transparentColor = Color.White;

		// picSizeMicro=new mImage("sizeMicro.png");
		// picSizeMicro.transparentColor=Color.White;
		// picSizeSmall=new mImage("sizeSmall.png");
		// picSizeSmall.transparentColor=Color.White;
		// picSizeReg=new mImage("sizeReg.png");
		// picSizeReg.transparentColor=Color.White;
		// picSizeLarge=new mImage("sizeLarge.png");
		// picSizeLarge.transparentColor=Color.White;
		// picSizeVLarge=new mImage("sizeVLarge.png");
		// picSizeVLarge.transparentColor=Color.White;
		// picSizeNonPhysical=new mImage("sizeNonPhysical.png");
		// picSizeNonPhysical.transparentColor=Color.White;

		for (byte i = 0; i < CacheSize.CW_TOTAL_SIZE_IMAGES; i++) {
			sizePics[i] = new mImage(CacheSize.sizeImageForId(i));
			sizePics[i].transparentColor = Color.White;
		}

		picHasSolver = new mImage("solver_exists.png");
		picHasSolver.transparentColor = Color.White;
		picHasNotes = new mImage("notes_exist.png");
		picHasNotes.transparentColor = Color.White;
		// updateRows();
	}

	/**
	 * Sets the column names and widths from preferences
	 * 
	 */
	public void setColumnNamesAndWidths() {
		colMap = TableColumnChooser.str2Array(Global.getPref().listColMap, 0, N_COLUMNS - 1, 0, -1);
		colWidth = TableColumnChooser.str2Array(Global.getPref().listColWidth, 10, 1024, 50, colMap.length);
		numCols = colMap.length;
		clearCellAdjustments();
		// If the displayed columns include the checkbox, we use the full menu
		if (("," + Global.getPref().listColMap + ",").indexOf(",0,") >= 0)
			tcControl.setMenuFull();
		else
			tcControl.setMenuSmall();
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
				if (Global.getPref().SortingGroupedByCache) {
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
					if (ch.is_owned())
						lineColorBG.set(COLOR_OWNED);
					else if (ch.is_found())
						lineColorBG.set(COLOR_FOUND);
					else if (ch.is_flaged)
						lineColorBG.set(COLOR_FLAGED);
					else if (ch.getCacheStatus().indexOf(MyLocale.getMsg(319, "not found")) > -1)
						lineColorBG.set(COLOR_STATUS);
					else if (Global.getPref().debug && ch.detailsLoaded())
						lineColorBG.set(COLOR_DETAILS_LOADED);

					if (ch.is_archived()) {
						if (lineColorBG.equals(COLOR_WHITE)) {
							lineColorBG.set(COLOR_ARCHIVED);
							ta.foreground = COLOR_WHITE;
						} else {
							ta.foreground = COLOR_ARCHIVED;
						}
					} else if (!ch.is_available()) {
						if (lineColorBG.equals(COLOR_WHITE)) {
							lineColorBG.set(COLOR_AVAILABLE);
						} else {
							ta.foreground = COLOR_AVAILABLE;
						}
					}

					// Now, if a line is selected, blend the determined color
					// with the selection
					// color.
					if (isSelected)
						mergeColor(lineColorBG, lineColorBG, COLOR_SELECTED);
					ta.fillColor = lineColorBG;
					lastColorBG.set(ta.fillColor);
					lastColorFG.set(ta.foreground);
					lastRow = row;
				} catch (Exception e) {
					Global.getPref().log("[myTableModel:getCellAttributes]Ignored row=" + row + " lastRow=" + lastRow, e, true);
				}
				;
			} else {
				// Here: We already had this row.
				// Take color computed for last column
				ta.fillColor = lastColorBG;
				ta.foreground = lastColorFG;
			}
		} else if (row == -1 && colMap[col] == 0 && Global.getProfile().showBlacklisted()) {
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
					return new IconAndText(imgSortUp, colName[colMap[col]], fm);
					// return "^ "+colName[colMap[col]];
				} else {
					return new IconAndText(imgSortDown, colName[colMap[col]], fm);
					// return "v "+colName[colMap[col]];
				}
			} else {
				return colName[colMap[col]];
			}
		}
		try { // Access to row can fail if many caches are deleted
			CacheHolder ch = cacheDB.get(row);
			if (ch != null /* ch.isVisible() */) { // Check of visibility
													// needed here??
				switch (colMap[col]) { // Faster than using column names
				case 0: // Checkbox
					if (ch.is_Checked)
						return checkboxTicked;
					else
						return checkboxUnticked;
				case 1: // Type
					return CacheType.getTypeImage(ch.getType());
				case 2: // Difficulty;
					if (!ch.isCacheWpt()) {
						return "";
					} else {
						return CacheTerrDiff.longDT(ch.getHard());
					}
				case 3: // Terrain
					if (!ch.isCacheWpt()) {
						return "";
					} else {
						return CacheTerrDiff.longDT(ch.getTerrain());
					}
				case 4: // Waypoint
					if (showExtraWptInfo) {
						if (ch.is_incomplete())
							return ch.getIconAndTextWP(4, fm);
						if (ch.is_new())
							return ch.getIconAndTextWP(3, fm);
						if (ch.is_updated())
							return ch.getIconAndTextWP(2, fm);
						if (ch.is_log_updated())
							return ch.getIconAndTextWP(1, fm);
					}
					return ch.getWayPoint();
				case 5: // Cachename
					// Fast return for majority of case
					if (!showExtraWptInfo || (ch.has_bugs() == false && ch.getNoFindLogs() == 0))
						return ch.getCacheName();
					// Now need more checks
					IconAndText wpVal = new IconAndText();
					if (ch.has_bugs() == true)
						wpVal.addColumn(bug);
					if (ch.getNoFindLogs() > 0) {
						if (ch.getNoFindLogs() > noFindLogs.length)
							wpVal.addColumn(noFindLogs[noFindLogs.length - 1]);
						else
							wpVal.addColumn(noFindLogs[ch.getNoFindLogs() - 1]);
					}
					wpVal.addColumn(ch.getCacheName());
					return wpVal;
				case 6: // Location
					return ch.getLatLon();
				case 7: // Owner
					return ch.getCacheOwner();
				case 8: // Date hidden
					return ch.getDateHidden();
				case 9: // Status
					return ch.getCacheStatus();
				case 10: // Distance
					return ch.getDistance();
				case 11: // Bearing
					return ch.getBearing();
				case 12: // Size
					if (ch.isAddiWpt()) {
						return "";
					} else {
						return sizePics[CacheSize.guiSizeImageId(ch.getCacheSize())];
					}
				case 13: // OC / gcvote Bewertung
					return ch.getRecommended();
				case 14: //
					if (ch.getWayPoint().startsWith("GC"))
						return ch.getOcCacheID();
					else {
						String[] stmp = mString.split(ch.getCacheOwner(), '/');
						int l = stmp.length - 1;
						if (l > 0) {
							String s = stmp[l].trim();
							if (s.startsWith("GC"))
								return s;
							else
								return "";
						} else
							return "";
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
					if (!ch.getLastSync().equals("")) {
						try {
							lastSyncWorker.parse(ch.getLastSync(), "yyyyMMddHHmmss");
						} catch (IllegalArgumentException e) {
							Global.getPref().log("Could not parse 'lastSyncDate': " + ch.getLastSync() + ". Reset to empty.", e);
							ch.setLastSync("");
						}
						return lastSyncWorker.format("yyyy-MM-dd HH:mm");
					} else {
						return "";
					}
				} // Switch
			} // if
		} catch (Exception e) {
			// Global.getPref().log("[myTableModel:getCellData]Ignored", e,true);
			return null;
		}
		return null;
	}

	public boolean penPressed(Point onTable, Point cell) {
		boolean retval = false;
		if (cell == null)
			return false;
		try {
			// Check whether the click is on the checkbox image
			if (cell.y >= 0 && colMap[cell.x] == 0) {
				Global.getProfile().selectionChanged = true;
				if ((penEventModifiers & IKeys.SHIFT) > 0) {
					if (tcControl.cursor.y >= 0) { // Second row being marked
													// with shift key pressed
						if (tcControl.cursor.y < cell.y)
							toggleSelect(tcControl.cursor.y + 1, cell.y, cell.x);
						else
							toggleSelect(cell.y, tcControl.cursor.y - 1, cell.x);
					} else { // Remember this row as start of range, but don't
								// toggle yet
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
			Global.getPref().log("[myTableModel:Penpressed]", npex, true);
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
			Point a = tcControl.getSelectedCell(null);
			CacheHolder ch = null;
			if ((a != null) && (a.y >= 0) && (a.y < cacheDB.size()))
				ch = cacheDB.get(a.y);
			cacheDB.sort(new MyComparer(cacheDB, sortedBy, numRows), !sortAscending);
			updateRows();
			// = cacheDB.rebuild(sortedVector of ch,
			// invisibleVector of ch)
			// select previously selected Cache again
			if (ch != null) {
				int rownum = Global.getProfile().getCacheIndex(ch.getWayPoint());
				if (rownum >= 0)
					tcControl.cursorTo(rownum, 0, true);
			}
			this.isSorted = true;
			Vm.showWait(false);
		}
		tcControl.tbp.refreshControl(); // repaint with update Statusbar
	}

	/**
	 * Toggle the select status for a group of caches If from==to, the addi Waypoints are also toggled if the cache is a main waypoint If from!=to, each cache is toggled irrespective of its type (main or addi)
	 * 
	 * @param from
	 *            index of first cache to toggle
	 * @param to
	 *            index of last cache to toggle
	 * @param x
	 *            is column of checkbox (does not have to be 0)
	 */
	void toggleSelect(int from, int to, int x) {
		CacheHolder ch;
		boolean singleRow = from == to;
		for (int j = from; j <= to; j++) {
			ch = cacheDB.get(j);
			ch.is_Checked = !ch.is_Checked;
			tcControl.repaintCell(j, x);
			// set the ceckbox also for addi wpts
			if (ch.hasAddiWpt() && singleRow) {
				CacheHolder addiWpt;
				int addiCount = ch.addiWpts.getCount();
				for (int i = 0; i < addiCount; i++) {
					addiWpt = (CacheHolder) ch.addiWpts.get(i);
					addiWpt.is_Checked = ch.is_Checked;
					if (addiWpt.isVisible()) {
						tcControl.repaintCell(cacheDB.getIndex(addiWpt), x);
					}
				}

			}
		}
	}

	public void select(int row, int col, boolean selectOn) {
		// super.select(row, col, selectOn);
		tcControl.cursorTo(row, col, true);
	}

}
