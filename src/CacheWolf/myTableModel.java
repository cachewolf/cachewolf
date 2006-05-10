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
*/
public class myTableModel extends TableModel{
	
	Vector cacheDB;
	String[] colName;
	Image cacheImages[] = new Image[138];
	Image noFindLogs[] = new Image[4];
	int[] breiten;
	Image red, blue, green, yellow;
	mImage bug;
	myTableControl tcControl;
	boolean sortAsc = false;
	FontMetrics fm;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	String nmCheck, nmQuest, nmD,nmT,nmWay,nmName,nmLoc,nmOwn,nmHid,nmStat,nmDist,nmBear = new String();
	
	public myTableModel(Vector DB, String[] colNs,int[] colWidth, myTableControl tc, FontMetrics fm){
		super();
		nmCheck = " ";
		nmQuest = "?";
		nmD = (String)lr.get(1000,"D");
		nmT = (String)lr.get(1001,"T");
		nmWay = (String)lr.get(1002,"Waypoint");
		nmName = (String)lr.get(1003,"Name");
		nmLoc = (String)lr.get(1004,"Location");
		nmOwn = (String)lr.get(1005,"Owner");
		nmHid = (String)lr.get(1006,"Hidden");
		nmStat = (String)lr.get(1007,"Status");
		nmDist = (String)lr.get(1008,"Dist");
		nmBear = (String)lr.get(1009,"Bear");
		fm = this.fm;
		tcControl = tc;
		cacheDB = DB;
		this.numRows = cacheDB.size();
		Dimension selrow = new Dimension(-1,1);
		this.cursorSize = selrow;
		colName = new String[colNs.length];
		colName = colNs;
		breiten = new int[colWidth.length];
		breiten = colWidth;
		this.numCols = colName.length;
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

		cacheImages[108] = new Image("108.png");
		cacheImages[109] = new Image("109.png");
		cacheImages[110] = new Image("110.png");
		cacheImages[137] = new Image("137.png");
		noFindLogs[0] = new Image("no_1_log.png");
		noFindLogs[1] = new Image("no_2_log.png");
		noFindLogs[2] = new Image("no_3_log.png");
		noFindLogs[3] = new Image("no_4_log.png");
		red = new Image("red.png");
		blue = new Image("blue.png");
		green = new Image("green.png");
		yellow = new Image("yellow.png");
		bug = new mImage("bug.png");
	}
	
	public void setVector(Vector DB){
		cacheDB = DB;
		this.numRows = cacheDB.size();
	}
	
	public void updateRows(){
		this.numRows = cacheDB.size();
		int counter = 0;
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_filtered == true) counter++;
		}
		//Vm.debug("Counter: " + Convert.toString(counter));
		if(counter > 0){
			this.numRows = this.numRows - counter;
			cacheDB.sort(new MyComparer("filter"), false);
		}
		 
	}
	
	/**
	* Method to set the row color of the table displaying the
	* cache list, depending on different flags set to the cache.
	*/
	public TableCellAttributes getCellAttributes(int row,int col,boolean  isSelected, TableCellAttributes ta){
		try{
			ta = super.getCellAttributes(row, col, isSelected, ta);
			ta.alignment = ta.LEFT;
			ta.anchor = ta.LEFT;
			if(row >= 0){ 
				CacheHolder ch = (CacheHolder)cacheDB.get(row);
				// Color code:
				// red := flagged
				if(ch.is_flaged == true) ta.fillColor = new Color(255,255,0);
				// green := found
				if(ch.is_found == true) ta.fillColor = new Color(152,251,152);
				// blue := owner
				if(ch.is_owned == true) ta.fillColor = new Color(135,206,235);
				if(ch.is_available == false) ta.fillColor = new Color(255,69,0);
				if(ch.is_archived == true) ta.fillColor = new Color(139,37,0);
				if(ch.is_available == false && ch.is_found == true){
					//Green background
					ta.fillColor = new Color(152,251,152);
					//Change font color to red
					ta.foreground = new Color(255,0,0);
				}
				// yellow := new
				// check DateHidden ? <7 days : new!
				// orange := updated (logs?)
				// grey := selected
				if(isSelected == true) ta.fillColor = new Color(198,198,198);
			}
		}catch(NumberFormatException nfe){}
		catch(ArrayIndexOutOfBoundsException abe){}
		return ta;
	}
	
	
	public int calculateColWidth(int col){
		//Vm.debug("myTableModel:: Calculating col width" + col);
		int retval = 50;
		if(col == -1) retval = 0;
		try{
			if(col >= 0) retval = breiten[col];
		}catch(Exception ex){}
		return retval;
	}
	
	public int calculateRowHeight(int row){
		return 18;
	}
	
	public Object getCellData(int row, int col){
		IconAndText wpVal = new IconAndText(); //(IImage)bug, "Test Me", fm);
		Object rettext = new Object();
		int collector = 0;
			if(row == -1) {
				rettext = (String)colName[col];
			}
			if(row >= 0 ){
				try{
					CacheHolder ch = (CacheHolder)cacheDB.get(row);
					//Vm.debug(String.valueOf(row));
					//Vm.debug(String.valueOf(cols[col]));
					if(ch.is_filtered == false){
						try{
							if(colName[col].equals(nmCheck)) {
								mCheckBox m = new mCheckBox();
								m.setTag(0, ch.wayPoint);
								if(ch.is_Checked == true) m.setState(true);
								else m.setState(false);
								rettext = m;
							}
							if(colName[col].equals(nmQuest)) rettext = (IImage)cacheImages[Convert.parseInt(ch.type)];				
							if(colName[col].equals(nmD)) rettext = (String)ch.hard;
							if(colName[col].equals(nmT)) rettext = (String)ch.terrain;
							if(colName[col].equals(nmWay)){
								rettext = (String)ch.wayPoint;
								if(ch.is_log_update == true) wpVal = new IconAndText((IImage)blue, ch.wayPoint, fm);
								if(ch.is_update == true) wpVal = new IconAndText((IImage)red, ch.wayPoint, fm);
								if(ch.is_new == true) wpVal = new IconAndText((IImage)yellow, ch.wayPoint, fm);
								if(ch.is_log_update == false &&
								   ch.is_update == false &&
								   ch.is_new == false) rettext = (String)ch.wayPoint;
								else rettext = wpVal;
							}
							if(colName[col].equals(nmName)) {
								rettext = (String)ch.CacheName;
								wpVal = new IconAndText();
								if(ch.has_bug == true){
									wpVal.addColumn((IImage)bug);
								}
								if(ch.noFindLogs > 0){
									wpVal.addColumn((IImage)noFindLogs[ch.noFindLogs-1]);
								}
								wpVal.addColumn(rettext);
								rettext = wpVal;
							}
							if(colName[col].equals(nmLoc)) rettext = (String)ch.LatLon;
							if(colName[col].equals(nmOwn)) rettext = (String)ch.CacheOwner;
							if(colName[col].equals(nmHid)) rettext = (String)ch.DateHidden;
							if(colName[col].equals(nmStat)) rettext = (String)ch.CacheStatus;
							if(colName[col].equals(nmDist)) rettext = (String)ch.distance;
							if(colName[col].equals(nmBear)) rettext = (String)ch.bearing;
						}catch(NumberFormatException nfe){}
					}
				}catch(ArrayIndexOutOfBoundsException abe){
					rettext = "deleted";
				}
			}
		return rettext;
	}
	
	public boolean penPressed(Point onTable,Point cell){
		boolean retval = false;
		// Table header hit
		try{
			if(cell.y == -1){
				if(sortAsc == false) sortAsc = true;
				else sortAsc = false;
				retval = true;
				if(colName[cell.x].equals(nmDist) == false){
					CacheHolder ch = new CacheHolder();
					Vm.showWait(true);
					Point a = new Point();
					a = tcControl.getSelectedCell(a);
					if(!(a == null)) ch = (CacheHolder)cacheDB.get(a.y);
					cacheDB.sort(new MyComparer(colName[cell.x]), sortAsc);
					updateRows();
					if(!(a == null)){
						int rownum = getCacheIndex(ch.wayPoint);
						if(rownum >= 0){
							tcControl.scrollToVisible(rownum, 0);
							tcControl.clearSelectedCells(new Vector());
							for(int i= 0; i < 11; i++){
								tcControl.addToSelection(rownum,i); 
							}
						}
					}
					Vm.showWait(false);
				}
				if(colName[cell.x].equals(nmDist)) {
					CacheHolder ch = new CacheHolder();
					Vm.showWait(true);
					Point a = new Point();
					Point dest = new Point();
					a = tcControl.getSelectedCell(dest);
					if(!(a == null)) ch = (CacheHolder)cacheDB.get(a.y);
					cacheDB.sort(new DistComparer(), sortAsc);
					updateRows();
					if(!(a == null)){
						int rownum = getCacheIndex(ch.wayPoint);
						if(rownum >= 0){
							tcControl.scrollToVisible(rownum, 0);
							tcControl.clearSelectedCells(new Vector());
							for(int i= 0; i < 11; i++){
								tcControl.addToSelection(rownum,i); 
							}
						}
					}
					Vm.showWait(false);
				}
				updateRows();
				tcControl.update(true);
				
			}
		}catch(NullPointerException npex){}
		return retval;
	}
	
	private int getCacheIndex(String wp){
		int retval = -1;
		CacheHolder ch = new CacheHolder();
		for(int i = 0; i<cacheDB.size();i++){
			ch = new CacheHolder();
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.wayPoint.equals(wp)){
				return i;
			}
		}
		return retval;
	}
}
