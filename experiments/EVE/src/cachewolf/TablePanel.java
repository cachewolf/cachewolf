package cachewolf;

import eve.sys.Convert;
import eve.sys.Event;
import eve.sys.Handle;
import eve.ui.*;
import eve.ui.event.KeyEvent;
import eve.ui.event.PenEvent;
import eve.ui.table.TableCellAttributes;
import eve.ui.table.TableControl;
import eve.ui.table.TableModel;

import java.io.IOException;
import java.util.*;

import eve.fx.*;
import eve.fx.gui.IKeys;
import eve.io.File;

/**
*	Class to display the cache database in a table.
*/
public class TablePanel extends CellPanel{
	private static final Color COLOR_SEARCH		= new Color(255,255,0);
	private static final Color COLOR_FOUND		= new Color(152,251,152);
	private static final Color COLOR_OWNED		= new Color(135,206,235);
	private static final Color COLOR_AVAILABLE	= new Color(255,69,0);
	private static final Color COLOR_ARCHIVED	= new Color(139,37,0);
	private static final Color COLOR_SELECTED	= new Color(198,198,198);
	private static final Color COLOR_ARCHFND_FG	= new Color(255,0,0); // Archived && Found
	private static final Color COLOR_ARCHFND_BG	= new Color(152,251,152);

	ListTableControl tControl;
	ListTableModel tModel;

	Vector cacheDB;
	//MainTab myMaintab;
	StatusBar statBar;
	/** We keep track of the currently selected cache in two variables(for speed)
	 * selectedIdx is the index in cacheDB, selectedch is the actual cache
	 * selectedIdx=-1 if no caches are visible (i.e. database empty or filtered). In
	 * this case selectedch is "null".
	 * Otherwise selectedIdx points to a visible cache.
	 * When the cacheDB is reorganised (by sort/filter/search), the selected cache
	 * may end up at a new index.
	 */
	//int selectedIdx=0;
	//CacheHolder selectedCh;

	public TablePanel(StatusBar statBar){
		Profile profile=Global.getProfile();
		this.statBar = statBar;
		cacheDB = profile.cacheDB;
		addLast(new MyScrollBarPanel(tControl = new ListTableControl(this)));
		if (statBar!=null) addLast(statBar,CellConstants.DONTSTRETCH, CellConstants.FILL);
		tModel = new ListTableModel(tControl, getFontMetrics());
		tModel.hasRowHeaders = false;
		tModel.hasColumnHeaders  = true;
		tControl.setTableModel(tModel);
	}

	/** Mark the row as selected so that myTableModel can color it grey */
	public void selectRow(int row) {
		// Ensure that the highlighted row is visible (e.g. when coming from radar panel)
		// Next line needed for key scrolling
		tControl.cursorTo(row, 0, true); //tc.cursor.x+tc.listMode
	}

	/** Highlight the first row in grey. It can be unhighlighted by clicking */
	public void selectFirstRow() {
		tModel.cursorSize=new Dimension(-1,1);
		if (cacheDB.size()>0) {
			tControl.cursorTo(0, 0, true);
		}
	}

	/** Returns the index of the currently selected cache or -1 of the cache is no longer visible
	 * due to a sort/filter or search operation
	 * @return index of selected cache (-1 if not visible)
	 */
	public int getSelectedCache(){
		// If the selected Cache is no longer visible (e.g. after applying a filter)
		// select the last row
		if (tControl.cursor.y>=tModel.numRows)
			return tModel.numRows-1;
		return tControl.cursor.y;
	}

	public void saveColWidth(Preferences pref){
		String colWidths=tModel.getColWidths();
		if (!colWidths.equals(pref.listColWidth)) {
			pref.listColWidth=colWidths;
			pref.savePreferences();
		}
	}

	public void resetModel() {
		tModel.numRows = cacheDB.size();
		Global.getProfile().updateBearingDistance();
		Global.getProfile().restoreFilter(true); // Restore the isActive & isInverted status of the filter
		tControl.scrollToVisible(0,0);
		refreshTable();
	}

	/** Move all filtered caches to the end of the table and redisplay table */
	//TODO Add a sort here to restore the sort after a filter
	public void refreshTable(){
		String wayPoint;
		if (getSelectedCache() >= 0)
			wayPoint = ((CacheHolder)cacheDB.get(getSelectedCache())).wayPoint;
		else wayPoint = null;
		tControl.clearSelection(null); // otherwise problems when deleting last cache in list
		Global.mainTab.tbP.updateRows();
		// Check whether the currently selected cache is still visible
		int rownum = 0;
		if (wayPoint != null) {
			rownum = Global.getProfile().getCacheIndex(wayPoint);
			if ( (rownum < 0) || (rownum>=tModel.numRows) )
				rownum = 0;
		}

		// Check whether the currently selected cache is still visible
		selectRow(rownum);
		tControl.update(true); // Update and repaint
		Global.mainTab.radarP.clearRadarPanel();
		Global.mainTab.clearDetails();
		if (statBar!=null) statBar.updateDisplay();
	}

	/**
	 * Similar to refreshTable but not so "heavy".
	 * Is used when user changes settings in preferences.
	 */
	public void refreshControl(){
		tControl.update(true);
	}

	/**
	 * Moves the addi waypoints just behind their main cache and hides filteres caches
	 */
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
						for (int j=0; j<ch.addiWpts.size();j++){
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
		tModel.numRows = sortDB.size();
	}

	/**
	 * Delete all caches which have been ticked
	 * @param showProgress Show the progress
	 */
	public void deleteSelectedCaches(boolean showProgress) {
		CacheHolder ch;
		Profile profile=Global.getProfile();
		Handle h=null;
		myProgressBarForm pbf=null;
		if (showProgress) Form.showWait();

		// Count # of caches to delete
		int allCount=0;
		int mainFilteredCount=0;
		int addiFilteredCount=0;
		int shouldDeleteCount=0;
		boolean deleteFiltered=true;  // Bisheriges Verhalten
		for(int i = cacheDB.size()-1; i >=0; i--){
			ch = (CacheHolder)cacheDB.get(i);
			if ( ch.is_Checked) {
				allCount++;
				if (ch.is_filtered) {
					if (ch.isAddiWpt()) {
						addiFilteredCount++;
					} else {
						mainFilteredCount++;
					}
				}
			}
		}
		// Warn if there are ticked but invisible caches - and ask if they should be deleted,
		// too.
		shouldDeleteCount = allCount;
		if (addiFilteredCount + mainFilteredCount > 0){
			if ((new MessageBox(MyLocale.getMsg(144,"Warning"),
					            MyLocale.getMsg(1029, "There are caches that are ticked but invisible.\n(Main caches: ") +
					            	mainFilteredCount + MyLocale.getMsg(1030, ", additional Waypoints: ") +
					            	addiFilteredCount+")\n" + MyLocale.getMsg(1031, "Delete them, too?"),
					            	FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
				deleteFiltered = true;
			} else {
				deleteFiltered = false;
				shouldDeleteCount = allCount - mainFilteredCount - addiFilteredCount;
			}
		}
		if (shouldDeleteCount>0) {
			if ((new MessageBox(MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(1022, "Delete selected caches (") + shouldDeleteCount + MyLocale.getMsg(1028, ") ?"), FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
				DataMover dm=new DataMover();
				if (showProgress) {
					pbf = new myProgressBarForm();
					h = new Handle();
					pbf.setTask(h,MyLocale.getMsg(1012, "Delete selected"));
					pbf.exec();
				}
				int nDeleted=0;
				int size=cacheDB.size();
				for(int i = size-1; i >=0; i--){// Start Counting down, as the size decreases with each deleted cache
					ch = (CacheHolder)cacheDB.get(i);
					if(ch.is_Checked && (!ch.is_filtered || deleteFiltered)) {
						nDeleted++;
						if (showProgress) {
							h.progress = ((float)nDeleted)/(float)shouldDeleteCount;
							h.changed();
						}
						dm.deleteCacheFiles(ch.wayPoint,profile.dataDir);
						cacheDB.remove(ch);
						ch.releaseCacheDetails();
						ch=null;
						if (showProgress && pbf.isClosed) break;
					}
				}
				if (showProgress) pbf.exit(0);
				tModel.numRows-=nDeleted;
				profile.saveIndex(true);
				Global.getProfile().buildReferences();
				refreshTable();
			}
		}
		if (showProgress) Form.cancelWait();
	}

	/** Toggle the select status for a group of caches
	 * If from==to, the addi Waypoints are also toggled if the cache is a main waypoint
	 * If from!=to, each cache is toggled irrespective of its type (main or addi)
	 * @param from index of first cache to toggle
	 * @param to index of last cache to toggle
	 * @param x is column of checkbox (does not have to be 0)
	 */
	void toggleSelect(int from, int to, int x) {
		CacheHolder ch;
		boolean singleRow= from == to;
		for (int j=from; j<=to; j++) {
			ch=(CacheHolder) cacheDB.get(j);
			ch.is_Checked= !ch.is_Checked;
			tControl.repaintCell(j, x);
			// set the ceckbox also for addi wpts
			if (ch.hasAddiWpt() && singleRow){
				CacheHolder addiWpt;
				int addiCount=ch.addiWpts.size();
				for (int i=0;i<addiCount;i++){
					addiWpt = (CacheHolder)ch.addiWpts.get(i);
					addiWpt.is_Checked = ch.is_Checked;
					if (!addiWpt.is_filtered){
						tControl.repaintCell(cacheDB.indexOf(addiWpt), x);
					}
				}
			}
		}
	}

	 private static class myProgressBarForm extends ProgressBarForm {
		 boolean isClosed=false;
		 protected boolean canExit(int exitCode) {
			isClosed=true;
			return true;
		 }
	 }


//####################################################################################
//   myTableControl
//####################################################################################

	/**
	 *	Implements the user interaction of the list view. Works together with myTableModel and TablePanel
	 */
	public static class ListTableControl extends TableControl{
		public Preferences pref;
		public Profile profile;
		public Vector cacheDB;
		public TablePanel tbP;
		private MenuItem miOpen,miGoto, miCenter, miOpenOnline, miOpenOffline, miDelete,miUpdate,
		        miTickAll, miUntickAll,miSeparator;
		private MenuItem[] mnuFull;
		private MenuItem[] mnuSmall;
		private Menu mFull,mSmall;
		private Rect oldCursor=new Rect();
		private Rect rbuff=new Rect();

		ListTableControl(TablePanel tablePanel) {
			mnuFull=new MenuItem[12];
			mnuFull[0]= miOpen		= new MenuItem(MyLocale.getMsg(1021,"Open description"));
			mnuFull[1]= miGoto			= new MenuItem(MyLocale.getMsg(1010,"Goto"));
			mnuFull[2]= miCenter		= new MenuItem(MyLocale.getMsg(1019,"Set this as center"));
			mnuFull[3]= miSeparator	= new MenuItem("-");
			mnuFull[4]= miOpenOnline 	= new MenuItem(MyLocale.getMsg(1020,"Open in $browser online"));
			mnuFull[5]= miOpenOffline	= new MenuItem(MyLocale.getMsg(1018,"Open in browser offline"));
			mnuFull[6]= miSeparator;
			mnuFull[7]= miDelete= new MenuItem(MyLocale.getMsg(1012,"Delete selected"));
			mnuFull[8] = miUpdate = new MenuItem(MyLocale.getMsg(1014,"Update"));
			mnuFull[9]= miSeparator;
			mnuFull[10]= miTickAll	= new MenuItem(MyLocale.getMsg(1015,"Select all"));
			mnuFull[11]=miUntickAll	= new MenuItem(MyLocale.getMsg(1016,"De-select all"));
			mFull = new Menu(mnuFull,"");

			mnuSmall=new MenuItem[6];
			mnuSmall[0]= miOpen;
			mnuSmall[1]= miGoto;
			mnuSmall[2]= miCenter;
			mnuSmall[3]= miSeparator;
			mnuSmall[4]= miOpenOnline;
			mnuSmall[5]= miOpenOffline;
			mSmall = new Menu(mnuSmall,"");
			profile=Global.getProfile();
			cacheDB = profile.cacheDB;
			pref = Global.getPref();
			tbP =tablePanel;
			allowDragSelection = false; // allow only one row to be selected at one time
			modify(WantDrag,0);
		}

		/** Full menu when tablePanel includes checkbox */
		public void setMenuFull() {
			setMenu(mFull);
		}

		public Menu getMenuFull() {
			return mFull;
		}

		/** Small menu when tablePanel does not include checkbox */
		public void setMenuSmall() {
			setMenu(mSmall);
		}
		public Menu getMenuSmall() {
			return mSmall;
		}

		/** Set all caches either as selected or as deselected, depending on argument */
		private void setTickForAll(boolean selectStatus) {
			Global.getProfile().setSelectForAll(selectStatus);
			this.repaint();
		}

		/** always select a whole row */
		public boolean isSelected(int row,int col) {
			return row==selection.y;
		}

		//============================= Event Handlers =============================
		public void onEvent(Event ev) {
			// If we changed the cache (e.g. via radar panel, we have to clear the details */
			//if (ev instanceof TableEvent) Global.mainTab.clearDetails();
			if (ev instanceof PenEvent && (ev.type == PenEvent.PEN_DOWN) ){
				Global.mainTab.tbP.tModel.penEventModifiers=((PenEvent)ev).modifiers;
		    }
			super.onEvent(ev);
		}
		// penPressed is in Model
		public void penRightReleased(Point p){
			if (cacheDB.size()>0) // No context menu when DB is empty
				menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
		}
		public void penHeld(Point p){
			if (cacheDB.size()>0) // No context menu when DB is empty
				menuState.doShowMenu(p,true,null);
		}
		public void penDoubleClicked(Point where) {
			Global.mainTab.select(Global.mainTab.descP);
		}

	 /**
	  * this is only necessary to hinder the user to unselect - not clear why this is needed
	  */
/*		 public void penReleased(Point p,boolean isDouble) {
			 Point p2 = cellAtPoint(p.x,p.y,null);
			 super.penReleased(p, isDouble);
			 Rect sel = getSelection(null);
			 if ((sel.height == 0 || sel.height == 0) && p2 != null) cursorTo(p2.y,p2.x, true); // if the selection is gone -> reselect it

		 }
*/		public void onKeyEvent(KeyEvent ev) {
			if (ev.type == KeyEvent.KEY_PRESS && ev.target == this){
				if ( (ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 1){ // <ctrl-a> gives 1, <ctrl-b> == 2
					// select all on <ctrl-a>
					setTickForAll(true);
					ev.consumed = true;
				}else {
					Global.mainTab.clearDetails();
					if (ev.key == IKeys.HOME) tbP.selectRow(0); //  cursorTo(0,cursor.x+listMode,true);
					else if (ev.key == IKeys.END) tbP.selectRow(model.numRows-1); //cursorTo(model.numRows-1,cursor.x+listMode,true);
					else if (ev.key == IKeys.PAGE_DOWN) tbP.selectRow(java.lang.Math.min(cursor.y+ getOnScreen(null).height-1, model.numRows-1)); //cursorTo(java.lang.Math.min(cursor.y+ getOnScreen(null).height-1, model.numRows-1),cursor.x+listMode,true); // I don't know why this doesn't work: tbp.doScroll(IScroll.Vertical, IScroll.PageHigher, 1);
					else if (ev.key == IKeys.PAGE_UP) tbP.selectRow(java.lang.Math.max(cursor.y-getOnScreen(null).height+1, 0)); // cursorTo(java.lang.Math.max(cursor.y-getOnScreen(null).height+1, 0),cursor.x+listMode,true);
					else if (ev.key == IKeys.ACTION || ev.key == IKeys.ENTER) Global.mainTab.select(Global.mainTab.descP);
					else if (ev.key == IKeys.DOWN) tbP.selectRow(java.lang.Math.min(cursor.y+ 1, model.numRows-1));
					else if (ev.key == IKeys.UP) tbP.selectRow(java.lang.Math.max(cursor.y-1, 0));
					else if (ev.key == IKeys.LEFT && Global.mainForm.cacheListVisible && cursor.y>=0 && cursor.y<tbP.tModel.numRows) Global.mainForm.cacheList.addCache(((CacheHolder)cacheDB.elementAt(cursor.y)).wayPoint);
					else if (ev.key == IKeys.RIGHT) {
						CacheHolder ch;
						ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
						Global.mainTab.gotoPoint(ch.pos);
					}
					else if (ev.key == 6 ) MainMenu.search(); // (char)6 == ctrl + f
					else super.onKeyEvent(ev);
				}
			}
			else super.onKeyEvent(ev);
		}

		public void popupMenuEvent(Object selectedItem){
			if (selectedItem == null) return;
			CacheHolder ch;
			if (selectedItem==miOpen){
				penDoubleClicked(null);
			} else if (selectedItem==miGoto){
				ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
				Global.mainTab.gotoPoint(ch.pos);
			} else if (selectedItem==miCenter){
				if (Global.mainTab.tbP.getSelectedCache() < 0) {
					Global.getPref().log("popupMenuEvent: getSelectedCache() < 0");
					return;
				}
				CacheHolder thisCache = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
				CWPoint cp=new CWPoint(thisCache.latLon);
				if (!cp.isValid()){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
					tmpMB.execute();
				} else {
					pref.curCentrePt.set(cp);
					Global.getProfile().updateBearingDistance();
					this.repaint();
				}
			} else if (selectedItem==miOpenOnline){
				if(browserPathIsValid()){
					ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
					CacheHolderDetail chD=ch.getCacheDetails(false, true);
					try {
						if (chD != null) {
							String cmd = "\""+pref.browser+ "\" \"" + chD.URL+"\"";
							eve.sys.Vm.execCommandLine(cmd);
						}
					} catch (IOException ex) {
						(new MessageBox("Error", "Cannot start browser!\n"+ex.toString()+"\nThe are two possible reasons:\n * path to internet browser in \npreferences not correct\n * An bug in eve VM, please be \npatient for an update",MessageBox.OKB)).execute();
					}
				}
			} else if (selectedItem==miOpenOffline) {
				if(browserPathIsValid()){
					ShowCacheInBrowser sc= new ShowCacheInBrowser();
					sc.showCache(((CacheHolder)cacheDB.get(tbP.getSelectedCache())).getCacheDetails(false, true));
				}
			} else if (selectedItem==miDelete){
				tbP.deleteSelectedCaches(true);
			} else if (selectedItem==miTickAll){
				setTickForAll(true);
			} else if (selectedItem==miUntickAll){
				setTickForAll(false);
			} else if (selectedItem == miUpdate){
				MainMenu.updateSelectedCaches(tbP);
			}
		}

		public boolean browserPathIsValid() {
			if(!new eve.io.File(pref.browser).exists()){
				(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(1032,"Path to browser:")+"\n"+pref.browser+"\n"+MyLocale.getMsg(1033,"is incorrect!"),FormBase.OKB)).execute();
				return false;
			} else
				return true;
		}




	    ///////////////////////////////////////////////////
		//  Allow the caches to be dragged into a cachelist
	    ///////////////////////////////////////////////////

		IconAndText imgDrag;
		String wayPoint;
		int row;

		public void startDragging(DragContext dc) {
			Vector cacheDB=Global.getProfile().cacheDB;
			 Point p=cellAtPoint(dc.start.x,dc.start.y,null);
			 wayPoint=null;
			 if (p.y>=0) {
				if (!Global.mainForm.cacheListVisible) {
					dc.cancelled=true;
					return;
				}
				 row=p.y;
				 CacheHolder ch=(CacheHolder)cacheDB.get(p.y);
				 wayPoint=ch.wayPoint;
				 //Vm.debug("Waypoint : "+ch.wayPoint);
				 imgDrag=new IconAndText();
				 imgDrag.addColumn( CacheType.cache2Img(ch.type));
				 imgDrag.addColumn(ch.wayPoint);
				 dc.dragData=dc.startImageDrag(imgDrag,new Point(8,8),this);
			 } else super.startDragging(dc);
		 }

		 public void stopDragging(DragContext dc) {
			 if (wayPoint!=null && !dc.cancelled) {
				 //Vm.debug("Stop  Dragging"+dc.curPoint.x+"/"+dc.curPoint.y);
				 dc.stopImageDrag(true);
				 Point p = Gui.getPosInParent(this,getWindow(),null);
				 p.x += dc.curPoint.x;
				 p.y += dc.curPoint.y;
				 Control c = getWindow().findChild(p.x,p.y);
			     if (c instanceof eve.ui.List && c.text.equals("CacheList")) {
			    	 if (Global.mainForm.cacheList.addCache(wayPoint)) {
			    		 c.repaintNow();
			    		 ((eve.ui.List) c).makeItemVisible(((eve.ui.List)c).itemsSize()-1);
			    	 }
			     }
			     Global.mainTab.tbP.selectRow(row);
				 //Vm.debug("Control "+c.toString()+"/"+c.text);
			 }else super.stopDragging(dc);
		 }

		 public void dragged(DragContext dc) {
		 	if (wayPoint!=null)
			   dc.imageDrag();
		 	else
		 		super.dragged(dc);
		 }

		 public void cursorTo(int row,int col,boolean selectNew) {
			col=0;
			if (row != -2 && col != -2 && !canSelect(row,col)) return;
			oldCursor.set(cursor.x,cursor.y,tbP.tModel.numCols,1);
			cursor.set(col,row);
			if (selectNew){
/*				Vector v = (Vector)Cache.get(Vector.class);
				clearSelectedCells(v);
				paintCells(v);
				v.removeAllElements();
				Cache.put(v);
*/				if (row != -2 && col != -2){
					if (scrollToVisible(row,col)) repaintNow();
					addToSelection(rbuff.set(0,row,tbP.tModel.numCols,1),false,true);
					//fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
					//clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
					//if (clickMode) clicked(row,col);
					//clickedFlags = 0;
				}
				paintCells(oldCursor);
			}
		 }
	}

//####################################################################################
//  MyTableModel
//####################################################################################
	/**
	*	Table model used to display the cache list.
	* 	Used by the table control in the first panel of
	*	CacheWolf.
	*/
	public static class ListTableModel extends TableModel{

		public static final int MAXCOLUMNS=14;
		// Colors for Cache status (BG unless otherwise stated)
		private Vector cacheDB;
		/** How the columns are mapped onto the list view. If colMap[i]=j, it means that
		 * the element j (as per the list below) is visible in column i.
		 * [0]TickBox, [1]Type, [2]Distance, [3]Terrain, [4]waypoint, [5]name, [6]coordinates,
		 * [7]owner, [8]datehidden, [9]status, [10]distance, [11]bearing, [12] Size, [13] # of OC recommend.
		 * [14] OC index
		 */
		private int[] colMap;
		/** The column widths corresponding to the list of columns above */
		private int[] colWidth;
		private String [] colName = {" ","?",MyLocale.getMsg(1000,"D"),MyLocale.getMsg(1001,"T"),
				MyLocale.getMsg(1002,"Waypoint"),"Name",MyLocale.getMsg(1004,"Location"),
				MyLocale.getMsg(1005,"Owner"),MyLocale.getMsg(1006,"Hidden"),MyLocale.getMsg(1007,"Status"),
				MyLocale.getMsg(1008,"Dist"),MyLocale.getMsg(1009,"Bear"),MyLocale.getMsg(1017,"S"),
				MyLocale.getMsg(1026,"#Rec"),MyLocale.getMsg(1027,"OC-IDX")};

		private Picture noFindLogs[] = new Picture[4];
		private Picture red, blue, yellow, skull, checkboxTicked,checkboxUnticked,bug;
		private boolean sortAsc = false;
		private int sortedBy = -1;
		private FontMetrics fm;
		private Picture picSizeMicro,picSizeSmall,picSizeReg,picSizeLarge,picSizeVLarge;
		/** This is the modifier (Shift & Control key status) for Pen Events
		 * it is set in myTableControl.onEvent */
		public int penEventModifiers;
		private ListTableControl tcControl;
		public boolean showExtraWptInfo=true;
		private IconAndText iAndT = new IconAndText();
		private IconAndText wpVal = new IconAndText();

		public ListTableModel(ListTableControl tc, FontMetrics fm){
			super();
			cacheDB = Global.getProfile().cacheDB;
			this.fm = fm;
			tcControl = tc;
			setColumnNamesAndWidths();
			//this.numRows = cacheDB.size();
			//Dimension selrow = new Dimension(-1,1);
			//this.cursorSize = selrow;
			noFindLogs[0] = new Picture("no_1_log.png");
			noFindLogs[1] = new Picture("no_2_log.png");
			noFindLogs[2] = new Picture("no_3_log.png");
			noFindLogs[3] = new Picture("no_4_log.png");
			red = new Picture("red.png",Color.White,0);
			blue = new Picture("blue.png",Color.White,0);
			//green = new Picture("green.png",Color.White,0);
			yellow = new Picture("yellow.png",Color.White,0);
			skull = new Picture("skull.png",Color.DarkBlue,0);
			bug = new Picture("bug_table.png",Color.DarkBlue,0);
			checkboxTicked = new Picture("checkboxTicked.png");
			checkboxUnticked= new Picture("checkboxUnticked.png");
			picSizeMicro=new Picture("sizeMicro.png",Color.White,0);
			picSizeSmall=new Picture("sizeSmall.png",Color.White,0);
			picSizeReg=new Picture("sizeReg.png",Color.White,0);
			picSizeLarge=new Picture("sizeLarge.png",Color.White,0);
			picSizeVLarge=new Picture("sizeVLarge.png",Color.White,0);
			iAndT.fontMetrics=fm;
			wpVal.fontMetrics=fm;
			//updateRows();
		}

		/**
		 * Sets the column names and widths from preferences
		 *
		 */
		public void setColumnNamesAndWidths() {
			colMap=TableColumnChooser.str2Array(Global.getPref().listColMap,0,14,0, -1);
			colWidth=TableColumnChooser.str2Array(Global.getPref().listColWidth,10,1024,50, colMap.length);
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
					else if(ch.is_found == true)
						ta.fillColor = COLOR_FOUND;
					else if(ch.is_flagged == true) ta.fillColor = COLOR_SEARCH;
				} catch (Exception e) {};
			} else if (row==-1 && colMap[col]==0 && Global.getProfile().showBlacklisted) ta.fillColor=Color.Black;
			return ta;
		}

		/**
		 * Return the data in a cell
		 */
		public Object getCellData(int row, int col){
			if(row == -1) return colName[colMap[col]];
			try { // Access to row can fail if many caches are deleted
				CacheHolder ch = (CacheHolder)cacheDB.get(row);
				if(ch.is_filtered == false){
					switch(colMap[col]) { // Faster than using column names
						case 0: // Checkbox
							if (ch.is_Checked)
								return checkboxTicked;
							return checkboxUnticked;
						case 1: // Type
							return  CacheType.cache2Img(ch.type);
						case 2: // Difficulty;
							return ch.hard;
						case 3: // Terrain
							return ch.terrain;
						case 4: // Waypoint
							if (showExtraWptInfo) {
/*								if(ch.is_incomplete) return new IconAndText(skull, ch.wayPoint, fm);
								if(ch.is_new       ) return new IconAndText(yellow, ch.wayPoint, fm);
								if(ch.is_update    ) return new IconAndText(red, ch.wayPoint, fm); // TODO this is for sure quite inefficient, better store it, don't create always new when the table is refreshed or only scrolled
								if(ch.is_log_update) return new IconAndText(blue, ch.wayPoint, fm);*/
								if(ch.is_incomplete) {
									iAndT.set(skull,ch.wayPoint);
									return iAndT;
								}
								if(ch.is_new       )  {
									iAndT.set(yellow,ch.wayPoint);
									return iAndT;
								}
								if(ch.is_update    ) {
									iAndT.set(red,ch.wayPoint);
									return iAndT;
								}
								if(ch.is_log_update) {
									iAndT.set(blue,ch.wayPoint);
									return iAndT;
								}
							}
							return ch.wayPoint;
						case 5: // Cachename
							// Fast return for majority of case
							if (!showExtraWptInfo || (ch.has_bug == false && ch.noFindLogs==0)) return ch.cacheName;
							wpVal.clearColumns();
							// Now need more checks
							if(ch.has_bug == true) wpVal.addColumn(bug);
							if(ch.noFindLogs > 0){
								if (ch.noFindLogs > noFindLogs.length)
									wpVal.addColumn(noFindLogs[noFindLogs.length-1]);
								else
									wpVal.addColumn(noFindLogs[ch.noFindLogs-1]);
							}
							wpVal.addColumn(ch.cacheName);
							return wpVal;
						case 6: // Location
							return ch.latLon;
						case 7: // Owner
							return ch.cacheOwner;
						case 8: // Date hidden
							return ch.dateHidden;
						case 9: // Status
							return ch.cacheStatus;
						case 10: // Distance
							return ch.distance;
						case 11: // Bearing
							return ch.bearing;
						case 12: // Size
							if (ch.cacheSize.length()==0) return "?";
							switch (ch.cacheSize.charAt(0)) {
								case 'M': return picSizeMicro;
								case 'S': return picSizeSmall;
								case 'R': return picSizeReg;
								case 'L': return picSizeLarge;
								case 'V': return picSizeVLarge;
								default: return "?";
							}
						case 13: // OC number of recommendations
							if (ch.wayPoint.startsWith("OC"))
								return Convert.formatInt(ch.numRecommended);
							return null;
						case 14: // OC rating
							if (ch.wayPoint.startsWith("OC"))
								return Convert.formatInt(ch.recommendationScore);
							return null;
					} // Switch
				} // if
			} catch (Exception e) { return null; }
			return null;
		}

		public boolean penPressed(Point onTable,Point cell){
			boolean retval = false;
			if (cell==null) return false;
			try{
				// Check whether the click is on the checkbox image
				if (cell.y>=0 && colMap[cell.x]==0) {
					Global.getProfile().selectionChanged = true;
					if ((penEventModifiers & IKeys.SHIFT)>0) {
						if (tcControl.cursor.y >= 0) { // Second row being marked with shift key pressed
							if (tcControl.cursor.y<cell.y)
								tcControl.tbP.toggleSelect(tcControl.cursor.y+1,cell.y,cell.x);
							else
								tcControl.tbP.toggleSelect(cell.y,tcControl.cursor.y-1,cell.x);
						} else { // Remember this row as start of range, but don't toggle yet
						}
					} else { // Single row marked
						tcControl.tbP.toggleSelect(cell.y,cell.y,cell.x);
					}
				}
				if(cell.y == -1){ // Hit a header => sort the table accordingly
					CacheHolder ch=null;
					// cell.x is the physical column but we have to sort by the
					// column it is mapped into
					int mappedCol=colMap[cell.x];
					if (mappedCol==0) { // Click on Tickbox header
						// Hide/unhide the additional information about a waypoint such as
						// travelbugs/number of notfound logs/yellow circle/red circle etc.
						// This helps on small PDA screens
						showExtraWptInfo=!showExtraWptInfo;
						this.table.repaint();
						return true;
					}
					Form.showWait();
					Point a = tcControl.getSelectedCell(null);
					if((a != null) && (a.y >= 0) && (a.y < cacheDB.size())) ch = (CacheHolder)cacheDB.get(a.y);
					if (mappedCol == sortedBy) sortAsc=!sortAsc;
					else sortAsc = false;
					sortedBy = mappedCol;
					eve.util.Utils.sort(new Handle(), cacheDB,new MyComparer(cacheDB, mappedCol,numRows), sortAsc );
					Global.mainTab.tbP.updateRows();
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
					Form.cancelWait();
					tcControl.update(true);
					retval = true;
				}
			} catch(NullPointerException npex){
				Form.cancelWait();
				Global.getPref().log("NPE in MyTableModel.Penpressed");}
			return retval;
		}

		public void select(int row,int col,boolean selectOn) {
			tcControl.cursorTo(row, col, true);
		}



//####################################################################################
// MyComparer
//####################################################################################

		/**
		*	This class handles the sorting for most of the sorting tasks. If a cache is
		*	to be displayed in the table or not is handled in the table model
		*	@see ListTableModel
		*	@see DistComparer
		*/
		public static class MyComparer implements eve.util.Comparer{

			public MyComparer(Vector cacheDB, int colToCompare, int visibleSize){
				if (visibleSize<2) return;
				for (int i=visibleSize; i<cacheDB.size(); i++) {
					CacheHolder ch=(CacheHolder) cacheDB.get(i);
					ch.sort="\uFFFF";
				}
				if (colToCompare==1) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=("000"+String.valueOf(ch.type)).substring(0,4);
					}
				} else if (colToCompare==2) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.hard;
					}
				} else if (colToCompare==3) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.terrain;
					}
				} else if (colToCompare==4) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.wayPoint.toUpperCase();
					}
				} else if (colToCompare==5) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.cacheName.toLowerCase();
					}
				} else if (colToCompare==6) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.latLon;
					}
				} else if (colToCompare==7) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.cacheOwner.toLowerCase();
					}
				} else if (colToCompare==8) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.dateHidden;
					}
				} else if (colToCompare==9) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.cacheStatus;
					}
				} else if (colToCompare==10) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						int p=ch.distance.indexOf(",");
						if (p<0) p=ch.distance.indexOf(".");
						if (p>=0 && p<=5)
							ch.sort="00000".substring(0,5-p)+ch.distance;
						else
							ch.sort=ch.distance;
					}
				} else if (colToCompare==11) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						ch.sort=ch.bearing;
					}

				} else if (colToCompare==12) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						if (ch.cacheSize.length()==0) ch.sort="?";
						else switch (ch.cacheSize.charAt(0)) {
							case 'M': ch.sort="1"; break;
							case 'S': ch.sort="2"; break;
							case 'R': ch.sort="3"; break;
							case 'L': ch.sort="4"; break;
							case 'V': ch.sort="5"; break;
							default: ch.sort="?";
						}
					}
				} else if (colToCompare==13) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						if (ch.wayPoint.startsWith("OC"))
							ch.sort=MyLocale.formatLong(ch.numRecommended,"00000");
						else
							ch.sort="\uFFFF";
					}
				} else if (colToCompare==14) {
					for (int i=0; i<visibleSize; i++) {
						CacheHolder ch=(CacheHolder) cacheDB.get(i);
						if (ch.wayPoint.startsWith("OC"))
							ch.sort=MyLocale.formatLong(ch.recommendationScore,"00000");
						else
							ch.sort="\uFFFF";
					}
				}
			}

			public int compare(Object o1, Object o2){
				CacheHolder oo1 = (CacheHolder)o1;
				CacheHolder oo2 = (CacheHolder)o2;
				return oo1.sort.compareTo(oo2.sort);
			}
		}
	} // MyTableModel
} // TablePanel


