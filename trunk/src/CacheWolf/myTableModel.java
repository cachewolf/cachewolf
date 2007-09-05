package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;
import ewe.util.*;
import java.lang.*;
import ewe.fx.*;
import ewe.reflect.*;
import ewe.graphics.*;

/**
*	Table model used to display the cache list.
* 	Used by the table control in the first panel of
*	CacheWolf.
* 20061212 salzkammergut, patch to speed up scrolling, Used MyLocale
*/
public class myTableModel extends TableModel{
	
	public static final int MAXCOLUMNS=12;
	// Colors for Cache status (BG unless otherwise stated)
	private static final Color COLOR_FLAGED		= new Color(255,255,0);
	private static final Color COLOR_FOUND		= new Color(152,251,152);
	private static final Color COLOR_OWNED		= new Color(135,206,235);
	private static final Color COLOR_AVAILABLE	= new Color(255,69,0);
	private static final Color COLOR_ARCHIVED	= new Color(139,37,0);
	private static final Color COLOR_SELECTED	= new Color(198,198,198);
	private static final Color COLOR_ARCHFND_FG	= new Color(255,0,0); // Archived && Found
	private static final Color COLOR_ARCHFND_BG	= new Color(152,251,152);	
	private Vector cacheDB;
	/** How the columns are mapped onto the list view. If colMap[i]=j, it means that
	 * the element j (as per the list below) is visible in column i. 
	 * [0]TickBox, [1]Type, [2]Distance, [3]Terrain, [4]waypoint, [5]name, [6]coordinates, 
	 * [7]owner, [8]datehidden, [9]status, [10]distance, [11]bearing
	 */
	private int[] colMap;
	/** The column widths corresponding to the list of columns above */
	private int[] colWidth;
	private String [] colName = {" ","?",MyLocale.getMsg(1000,"D"),MyLocale.getMsg(1001,"T"),MyLocale.getMsg(1002,"Waypoint"),"Name",MyLocale.getMsg(1004,"Location"),MyLocale.getMsg(1005,"Owner"),MyLocale.getMsg(1006,"Hidden"),MyLocale.getMsg(1007,"Status"),MyLocale.getMsg(1008,"Dist"),MyLocale.getMsg(1009,"Bear")};
	
	public static Image cacheImages[] = new Image[454]; // Images are used by TableControl
	private static Image noFindLogs[] = new Image[4];
	private Image red, blue, green, yellow, skull;
	private Image checkboxTicked,checkboxUnticked;
	private mImage bug;
	private boolean sortAsc = false;
	private int sortedBy = -1;
	private FontMetrics fm;
	/** This is the modifier (Shift & Control key status) for Pen Events
	 * it is set in myTableControl.onEvent */
	public int penEventModifiers; 
	/** The row of the last click where the shift key was pressed */
	private int lastRow=-1;
	private myTableControl tcControl;
	
	public myTableModel(myTableControl tc, FontMetrics fm){
		super();
		cacheDB = Global.getProfile().cacheDB;
		fm = this.fm;
		tcControl = tc;
		setColumnNamesAndWidths(); 
		this.numRows = cacheDB.size();
		Dimension selrow = new Dimension(-1,1);
		this.cursorSize = selrow;
		cacheImages[0] = new Image("0.png");
		//cacheImages[1] = new Image();
		cacheImages[2] = new Image("2.png");
		cacheImages[3] = new Image("3.png");
		cacheImages[4] = new Image("4.png");
		cacheImages[5] = new Image("5.png");
		cacheImages[6] = new Image("6.png");
		cacheImages[8] = new Image("8.png");
		//cacheImages[9] = new Image();
		//cacheImages[10] = new Image();
		cacheImages[11] = new Image("11.png");
		cacheImages[12] = new Image("12.png");
		cacheImages[13] = new Image("13.png");
		//additional waypoints, begin with 50
		cacheImages[50] = new Image("pkg.png");
		cacheImages[51] = new Image("stage.png");
		cacheImages[52] = new Image("puzzle.png");
		cacheImages[53] = new Image("flag.png");
		cacheImages[54] = new Image("trailhead.png");
		cacheImages[55] = new Image("waypoint.png");

		cacheImages[108] = new Image("108.png");
		cacheImages[109] = new Image("109.png");
		cacheImages[110] = new Image("110.png");
		cacheImages[137] = new Image("137.png");
		cacheImages[453] = new Image("453.png");
		noFindLogs[0] = new Image("no_1_log.png");
		noFindLogs[1] = new Image("no_2_log.png");
		noFindLogs[2] = new Image("no_3_log.png");
		noFindLogs[3] = new Image("no_4_log.png");
		red = new Image("red.png");
		blue = new Image("blue.png");
		green = new Image("green.png");
		yellow = new Image("yellow.png");
		skull = new Image("skull.png");
		bug = new mImage("bug.png");
		checkboxTicked = new Image("checkboxTicked.png");
		checkboxUnticked= new Image("checkboxUnticked.png");
		updateRows();
	}
	
	/**
	 * Sets the column names and widths from preferences
	 *
	 */
	public void setColumnNamesAndWidths() {
		colMap=TableColumnChooser.str2Array(Global.getPref().listColMap,0,11,0);
		colWidth=TableColumnChooser.str2Array(Global.getPref().listColWidth,10,1024,50);
		numCols=colMap.length;
		clearCellAdjustments();
		// If the displayed columns include the checkbox, we use the full menu
		if ((","+Global.getPref().listColMap+",").indexOf(",0,")>=0)
			tcControl.setMenuFull();
		else
			tcControl.setMenuSmall();
	}
	
	/**
	 * Return the column widths as a comma delimited string for storing in the preferences
	 * @return
	 */
	public String getColWidths() {
		// Update the list with the current widths
		for (int col=0; col<numCols; col++) {
			colWidth[colMap[col]]=getColWidth(col);
		}
		clearCellAdjustments();
		// Convert to string
		StringBuffer sb=new StringBuffer(40);
		for (int i=0; i<colWidth.length; i++) {
			if (sb.length()!=0) sb.append(',');
			sb.append(colWidth[i]);
		}
		return sb.toString();
	}
	public void updateRows(){
		Vector sortDB = new Vector();
		Vector filteredDB = new Vector();
		CacheHolder ch, addiWpt;
		// sort cacheDB:
		// - addi wpts are listet behind the main cache
		// - filtered caches are moved to the end
		int size=cacheDB.size();
		for (int i=0; i<size; i++){
			ch = (CacheHolder) cacheDB.get(i);
			if (ch.is_filtered) {
				filteredDB.add(ch);
			} else { // point is not filtered
				if (ch.isAddiWpt()){ // unfiltered Addi Wpt
					// check if main wpt is filtered
					if(ch.mainCache != null) { // parent exists
						if (ch.mainCache.is_filtered) 
							sortDB.add(ch); // Unfiltered Addi Wpt with filtered Main Wpt, show it on its own
						// else Main cache is not filtered, Addi will be added below main cache further down
					} else { //Addi without main Cache
						sortDB.add(ch);
					}
				} else { // Main Wpt, not filtered. Check for Addis
					sortDB.add(ch);
					if (ch.hasAddiWpt()){
						for (int j=0; j<ch.addiWpts.getCount();j++){
							addiWpt = (CacheHolder)ch.addiWpts.get(j);
							if (!addiWpt.is_filtered) sortDB.add(addiWpt);
						}
					}// if hasAddiWpt
				} // if AddiWpt
			} // if filtered
		}
		// rebuild database
		cacheDB.clear();
		cacheDB.addAll(sortDB);
		cacheDB.addAll(filteredDB);
		this.numRows = sortDB.getCount();
	}
	
	/**
	* Method to set the row color of the table displaying the
	* cache list, depending on different flags set to the cache.
	*/
	public TableCellAttributes getCellAttributes(int row,int col,boolean  isSelected, TableCellAttributes ta){
		ta = super.getCellAttributes(row, col, isSelected, ta);
		ta.alignment = CellConstants.LEFT;
		ta.anchor = CellConstants.LEFT;
		if(row >= 0){ 
			try {
			   CacheHolder ch = (CacheHolder)cacheDB.get(row);
				if(isSelected == true) ta.fillColor = COLOR_SELECTED;
				else if(ch.is_available == false && ch.is_found == true){
					ta.fillColor = COLOR_ARCHFND_BG;   // Green BG
					ta.foreground = COLOR_ARCHFND_FG;  // Red FG
				}
				else if(ch.is_archived == true) ta.fillColor = COLOR_ARCHIVED;
				else if(ch.is_available == false) ta.fillColor = COLOR_AVAILABLE;
				else if(ch.is_owned == true) ta.fillColor = COLOR_OWNED;
				else if(ch.is_found == true) ta.fillColor = COLOR_FOUND;
				else if(ch.is_flaged == true) ta.fillColor = COLOR_FLAGED;
			} catch (Exception e) {};
		}
		return ta;
	}

	public int calculateRowHeight(int row){
		return java.lang.Math.max(18, charHeight+4);
	}

	public int calculateColWidth(int col){
		if(col == -1) 
        	return 0;
        else if (col<numCols)
        	return colWidth[colMap[col]];
        else return 0;
	}
	
	/**
	 * Need to override this method with a null return to avoid
	 * getCellData being called twice on each access to a cell.
	 * For further reference see the Ewe source code.
	 * @author skg
	 */
	public Object getCellText(int row, int col) {
		return null;
	}

	public Object getCellData(int row, int col){
		if(row == -1) {
			return (String)colName[colMap[col]];
		} else {
			try { // Access to row can fail if many caches are deleted
				CacheHolder ch = (CacheHolder)cacheDB.get(row);
				if(ch.is_filtered == false){
					switch(colMap[col]) { // Faster than using column names
						case 0: // Checkbox
							if (ch.is_Checked) 
								return checkboxTicked; 
							else 
								return checkboxUnticked;
						case 1: // Type
							try {
								return (IImage) cacheImages[Convert.parseInt(ch.type)]; // TODO save in cacheholder as int
							} catch (NumberFormatException e) { return "?";}
						case 2: // Difficulty;
							return (String)ch.hard;
						case 3: // Terrain
							return (String)ch.terrain;
						case 4: // Waypoint
							if(ch.is_incomplete) return new IconAndText((IImage)skull, ch.wayPoint, fm);
							if(ch.is_update    ) return new IconAndText((IImage)red, ch.wayPoint, fm); // TODO this is for sure quite inefficient, better store it, don't create always new when the table is refreshed or only scrolled
							if(ch.is_new       ) return new IconAndText((IImage)yellow, ch.wayPoint, fm);
							if(ch.is_log_update) return new IconAndText((IImage)blue, ch.wayPoint, fm);
							return (String)ch.wayPoint;
						case 5: // Cachename
							// Fast return for majority of case
							if (ch.has_bug == false && ch.noFindLogs==0) return (String)ch.CacheName; 
							// Now need more checks
							IconAndText wpVal = new IconAndText();
							if(ch.has_bug == true) wpVal.addColumn((IImage)bug);
							if(ch.noFindLogs > 0){
								if (ch.noFindLogs > noFindLogs.length) 
									wpVal.addColumn((IImage)noFindLogs[noFindLogs.length-1]);
								else 
									wpVal.addColumn((IImage)noFindLogs[ch.noFindLogs-1]);
							}
							wpVal.addColumn((String)ch.CacheName);
							return wpVal;
						case 6: // Location
							return (String)ch.LatLon;
						case 7: // Owner
							return (String)ch.CacheOwner;
						case 8: // Date hidden
							return (String)ch.DateHidden;
						case 9: // Status
							return (String)ch.CacheStatus;
						case 10: // Distance
							return (String)ch.distance;
						case 11: // Bearing
							return (String)ch.bearing;
					} // Switch
				} // if
			} catch (Exception e) { return null; }
		}
		return null;
	}
	
	public boolean penPressed(Point onTable,Point cell){
		boolean retval = false;
		try{
			if (cell.y>=0) Global.mainTab.tbP.setSelectedCache(cell.y);
			// Check whether the click is on the checkbox image
			if (cell.y>=0 && cell.x==0) {
				Global.getProfile().selectionChanged = true;
				if ((penEventModifiers & IKeys.SHIFT)>0) {
					if (lastRow!=-1) { // Second row being marked with shift key pressed
						if (lastRow<cell.y)
							toggleSelect(lastRow,cell.y);
						else
							toggleSelect(cell.y,lastRow);
						lastRow=-1;
					} else { // Remember this row as start of range, but don't toggle yet
						lastRow=cell.y;
					}
				} else { // Single row marked
					toggleSelect(cell.y,cell.y);
					lastRow=-1;
				}
			}
			if(cell.y == -1){ // Hit a header => sort the table accordingly
				CacheHolder ch=null;
				// cell.x is the physical column but we have to sort by the
				// column it is mapped into
				int mappedCol=colMap[cell.x];
				Vm.showWait(true);
				Point a = tcControl.getSelectedCell(null);
				if(a != null) ch = (CacheHolder)cacheDB.get(a.y);
				if (mappedCol == sortedBy) sortAsc=!sortAsc;
				else sortAsc = false;
				sortedBy = mappedCol;
				cacheDB.sort(new MyComparer(cacheDB, colName[mappedCol],numRows), sortAsc);
				updateRows();
				if(a != null){
					int rownum = Global.getProfile().getCacheIndex(ch.wayPoint);
					if(rownum >= 0){
						tcControl.cursorTo(rownum, 0, true);
	/*					tcControl.scrollToVisible(rownum, 0);
						tcControl.clearSelectedCells(new Vector());
						for(int i= 0; i < MAXCOLUMNS; i++){
							tcControl.addToSelection(rownum,i); 
						}
		*/			}
				}
				Vm.showWait(false);
				tcControl.update(true);
				retval = true;
			}
		}catch(NullPointerException npex){}
		return retval;
	}
	
	/** Toggle the select status for a group of caches
	 * If from==to, the addi Waypoints are also toggled if the cache is a main waypoint
	 * If from!=to, each cache is toggled irrespective of its type (main or addi)
	 * @param from index of first cache to toggle
	 * @param to index of last cache to toggle
	 */
	void toggleSelect(int from, int to) {
		CacheHolder ch;
		boolean singleRow= from == to;
		for (int j=from; j<=to; j++) {
			ch=(CacheHolder) cacheDB.get(j);
			ch.is_Checked= !ch.is_Checked; 
			tcControl.repaintCell(j, 0);
			// set the ceckbox also for addi wpts
			if (ch.hasAddiWpt() && singleRow){
				CacheHolder addiWpt;
				int addiCount=ch.addiWpts.getCount();
				for (int i=0;i<addiCount;i++){
					addiWpt = (CacheHolder)ch.addiWpts.get(i);
					addiWpt.is_Checked = ch.is_Checked;
					if (!addiWpt.is_filtered){
						tcControl.repaintCell(cacheDB.find(addiWpt), 0);
					}
				}
				
			}
		}		
	}
	
}
