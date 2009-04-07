package CacheWolf;

import utils.CWWrapper;
import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.io.File;
import ewe.io.IOException;
import ewe.util.*;

/**
 *	Implements the user interaction of the list view. Works together with myTableModel and TablePanel
 */
public class myTableControl extends TableControl{

	public Preferences pref;
	public Profile profile;
	public CacheDB cacheDB;
	public TablePanel tbp;
	
	private MenuItem miOpen, miGoto, miCenter;
	private MenuItem miOpenOnline, miOpenOffline;
	private MenuItem miDelete, miUpdate;
	private MenuItem miTickAll, miUntickAll;
	private MenuItem miSeparator;

	private Menu mFull;
	private Menu mSmall;

	myTableControl(TablePanel tablePanel) {
		profile=Global.getProfile();
		cacheDB = profile.cacheDB;
		pref = Global.getPref();
		tbp =tablePanel;
		allowDragSelection = false; // allow only one row to be selected at one time
				
		MenuItem[] mnuFull = new MenuItem[12];
  	mnuFull[0] = miOpen = new MenuItem(MyLocale.getMsg(1021,"Open description"));
  	mnuFull[1] = miGoto = new MenuItem(MyLocale.getMsg(1010,"Goto"));
  	mnuFull[2] = miCenter = new MenuItem(MyLocale.getMsg(1019,"Center"));
  	mnuFull[3] = miSeparator = new MenuItem("-");
  	mnuFull[4] = miOpenOnline = new MenuItem(MyLocale.getMsg(1020,"Open in $browser online"));
  	mnuFull[5] = miOpenOffline = new MenuItem(MyLocale.getMsg(1018,"Open in browser offline"));
  	mnuFull[6] = miSeparator;
  	mnuFull[7] = miDelete = new MenuItem(MyLocale.getMsg(1012,"Delete selected"));
  	mnuFull[8] = miUpdate = new MenuItem(MyLocale.getMsg(1014,"Update"));
  	mnuFull[9] = miSeparator;
  	mnuFull[10] = miTickAll = new MenuItem(MyLocale.getMsg(1015,"Select all"));
  	mnuFull[11] = miUntickAll = new MenuItem(MyLocale.getMsg(1016,"De-select all"));	
  	mFull = new Menu(mnuFull, MyLocale.getMsg(1013,"With selection"));

  	MenuItem[] mnuSmall = new MenuItem[6];
  	mnuSmall[0] = miOpen;
  	mnuSmall[1] = miGoto;
  	mnuSmall[2] = miCenter;
  	mnuSmall[3] = miSeparator;
  	mnuSmall[4] = miOpenOnline;
  	mnuSmall[5] = miOpenOffline;
  	mSmall = new Menu(mnuSmall, MyLocale.getMsg(1013,"With selection"));	
	}

	/** Full menu when listview includes checkbox */
	public void setMenuFull() {
		setMenu(mFull);
//		if (!Vm.getPlatform().equals("Win32") && !Vm.getPlatform().equals("Java"))
//		   ((MenuItem)mFull.items.get(5)).modifiers|=MenuItem.Disabled;
	}
	
	public Menu getMenuFull() {
		return mFull;
	}

	/** Small menu when listview does not include checkbox */
	public void setMenuSmall() {
		setMenu(mSmall);
		//if (!Vm.getPlatform().equals("Win32") && !Vm.getPlatform().equals("Java"))
		//	   ((MenuItem)mSmall.items.get(5)).modifiers|=MenuItem.Disabled;
	}
	
	public void penRightReleased(Point p){
		if (cacheDB.size()>0) // No context menu when DB is empty
			menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}
	public void penHeld(Point p){
		if (cacheDB.size()>0) // No context menu when DB is empty
			menuState.doShowMenu(p,true,null); 
	}

	public void onKeyEvent(KeyEvent ev) {
		if (ev.type == KeyEvent.KEY_PRESS && ev.target == this){
			if ( (ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 1){ // <ctrl-a> gives 1, <ctrl-b> == 2
				// select all on <ctrl-a>
				setSelectForAll(true);
				ev.consumed = true;
			}
			else  {
				Global.mainTab.clearDetails();
				if (ev.key == IKeys.HOME) Global.mainTab.tbP.selectRow(0); //  cursorTo(0,cursor.x+listMode,true);
				else if (ev.key == IKeys.END) Global.mainTab.tbP.selectRow(model.numRows-1); //cursorTo(model.numRows-1,cursor.x+listMode,true);
				else if (ev.key == IKeys.PAGE_DOWN) Global.mainTab.tbP.selectRow(java.lang.Math.min(cursor.y+ getOnScreen(null).height-1, model.numRows-1)); //cursorTo(java.lang.Math.min(cursor.y+ getOnScreen(null).height-1, model.numRows-1),cursor.x+listMode,true); // I don't know why this doesn't work: tbp.doScroll(IScroll.Vertical, IScroll.PageHigher, 1);
				else if (ev.key == IKeys.PAGE_UP) Global.mainTab.tbP.selectRow(java.lang.Math.max(cursor.y-getOnScreen(null).height+1, 0)); // cursorTo(java.lang.Math.max(cursor.y-getOnScreen(null).height+1, 0),cursor.x+listMode,true);
				else if (ev.key == IKeys.ACTION || ev.key == IKeys.ENTER) Global.mainTab.select(Global.mainTab.descP);
				else if (ev.key == IKeys.DOWN) Global.mainTab.tbP.selectRow(java.lang.Math.min(cursor.y+ 1, model.numRows-1)); 
				else if (ev.key == IKeys.UP) Global.mainTab.tbP.selectRow(java.lang.Math.max(cursor.y-1, 0));
				else if (ev.key == IKeys.LEFT && Global.mainForm.cacheListVisible && cursor.y>=0 && cursor.y<tbp.myMod.numRows) Global.mainForm.cacheList.addCache(cacheDB.get(cursor.y).getWayPoint()); 
				else if (ev.key == IKeys.RIGHT) {
					CacheHolder ch;
					ch = cacheDB.get(tbp.getSelectedCache());
					Global.mainTab.gotoPoint(ch.pos);
				}
				else if (ev.key == 6 ) MainMenu.search(); // (char)6 == ctrl + f 
				else super.onKeyEvent(ev);
			}
		}
		else super.onKeyEvent(ev);
	}

	/** Set all caches either as selected or as deselected, depending on argument */
	private void setSelectForAll(boolean selectStatus) {
		Global.getProfile().setSelectForAll(selectStatus);
		tbp.refreshTable();
	}
	
	 
	/** always select a whole row */
	public boolean isSelected(int row,int col) {
		return row==selection.y;
	}
	
	public void popupMenuEvent(Object selectedItem){
		if (selectedItem == null) return;
		CacheHolder ch;
		if (selectedItem == miTickAll){
			setSelectForAll(true);
		}

		if (selectedItem == miUntickAll){
			setSelectForAll(false);
		}

		if (selectedItem == miDelete){
			Vm.showWait(true);
			// Count # of caches to delete
			int allCount=0;
			int mainFilteredCount=0;
			int addiFilteredCount=0;
			int shouldDeleteCount=0;
			boolean deleteFiltered=true;  // Bisheriges Verhalten
			for(int i = cacheDB.size()-1; i >=0; i--){
				CacheHolder currCache = cacheDB.get(i);
				if ( currCache.is_Checked) {
					allCount++;
					if (currCache.is_filtered()) {
						if (currCache.isAddiWpt()) {
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
					myProgressBarForm pbf = new myProgressBarForm();
					Handle h = new Handle();
					pbf.setTask(h,MyLocale.getMsg(1012, "Delete selected"));
					pbf.exec();
					int nDeleted=0;
					int size=cacheDB.size();
					for(int i = size-1; i >=0; i--){// Start Counting down, as the size decreases with each deleted cache
						ch = cacheDB.get(i);
						if(ch.is_Checked && (!ch.is_filtered() || deleteFiltered)) {
							nDeleted++;
							h.progress = ((float)nDeleted)/(float)allCount;
							h.changed();
							dm.deleteCacheFiles(ch.getWayPoint(),profile.dataDir);
							cacheDB.removeElementAt(i);
							ch.releaseCacheDetails();
							ch=null;
							if (pbf.isClosed) break;
						}
					}
					pbf.exit(0);
					tbp.myMod.numRows-=nDeleted;
					profile.saveIndex(pref,true);	
					tbp.refreshTable();
				}
			}
			Vm.showWait(false);
		}
				
		if (selectedItem == miUpdate){
			MainMenu.updateSelectedCaches(tbp);
		}

		if (selectedItem == miCenter){
			if (tbp.getSelectedCache() < 0) {
				Global.getPref().log("popupMenuEvent: getSelectedCache() < 0");
				return;
			}
			CacheHolder thisCache = cacheDB.get(tbp.getSelectedCache());
			CWPoint cp=new CWPoint(thisCache.LatLon);
			if (!cp.isValid()){
				MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), FormBase.OKB);
				tmpMB.execute();
			} else {				
				pref.curCentrePt.set(cp);
				Global.mainTab.updateBearDist(); // Update the distances with a warning message
				//tbp.refreshTable();
			}
		}

		if (selectedItem == miGoto){
			ch = cacheDB.get(tbp.getSelectedCache());
			Global.mainTab.gotoPoint(ch.pos);
		}
		if (selectedItem == miOpenOnline){
			if(browserPathIsValid()){
				ch = cacheDB.get(tbp.getSelectedCache());
				CacheHolderDetail chD=ch.getCacheDetails(false, true);
				try {
					if (chD != null) {
						CWWrapper.exec(pref.browser, chD.URL); // maybe this works on some PDAs?
					}
				} catch (IOException ex) {
					(new MessageBox(MyLocale.getMsg(321,"Error"),
							MyLocale.getMsg(1034,"Cannot start browser!") + "\n" + ex.toString() + "\n" +
							MyLocale.getMsg(1035,"Possible reason:") + "\n" +
							MyLocale.getMsg(1036,"A bug in ewe VM, please be") + "\n" +
							MyLocale.getMsg(1037,"patient for an update"),FormBase.OKB)).execute();
				}
			}
		}
		if (selectedItem == miOpenOffline) {
			if(browserPathIsValid()){
				ShowCacheInBrowser sc=new ShowCacheInBrowser();
				sc.showCache(cacheDB.get(tbp.getSelectedCache()).getCacheDetails(false, true));
			}
		}
		if (selectedItem == miOpen){
			penDoubleClicked(null);
		}

	}
	
	public boolean browserPathIsValid() {
		if(!new File(pref.browser).exists()){
			(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(1032,"Path to browser:")+"\n"+pref.browser+"\n"+MyLocale.getMsg(1033,"is incorrect!"),FormBase.OKB)).execute();
			return false;
		}
		else
			return true;
	}
	
	public void penDoubleClicked(Point where) {
		Global.mainTab.select(Global.mainTab.descP);
	}
	
	public void onEvent(Event ev) {
		if (ev instanceof PenEvent && (ev.type == PenEvent.PEN_DOWN) ){
			Global.mainTab.tbP.myMod.penEventModifiers=((PenEvent)ev).modifiers;
	    } 

		super.onEvent(ev);
	}
    ///////////////////////////////////////////////////
	//  Allow the caches to be dragged into a cachelist
    ///////////////////////////////////////////////////
	
	IconAndText imgDrag;
	String wayPoint;
	int row;
	
	public void startDragging(DragContext dc) {
		CacheDB cacheDB=Global.getProfile().cacheDB;
		 Point p=cellAtPoint(dc.start.x,dc.start.y,null);
		 wayPoint=null;
		 if (p.y>=0) { 
			if (!Global.mainForm.cacheListVisible) {
				dc.cancelled=true;
				return;
			}
			 row=p.y;
			 CacheHolder ch=cacheDB.get(p.y);
			 wayPoint=ch.getWayPoint();
			 //Vm.debug("Waypoint : "+ch.wayPoint);
			 imgDrag=new IconAndText();
			 imgDrag.addColumn(CacheType.cache2Img(ch.getType()));
			 imgDrag.addColumn(ch.getWayPoint());
			 dc.dragData=dc.startImageDrag(imgDrag,new Point(8,8),this);
		 } else super.startDragging(dc);
	 }

	 public void stopDragging(DragContext dc) {
		 if (wayPoint!=null && !dc.cancelled) {
			 //Vm.debug("Stop  Dragging"+dc.curPoint.x+"/"+dc.curPoint.y);
			 dc.stopImageDrag(true);
			 Point p = Gui.getPosInParent(this,getWindow());
			 p.x += dc.curPoint.x;
			 p.y += dc.curPoint.y;
			 Control c = getWindow().findChild(p.x,p.y);
		     if (c instanceof mList && c.text.equals("CacheList")) {
		    	 if (Global.mainForm.cacheList.addCache(wayPoint)) {
		    		 c.repaintNow();
		    		 ((mList) c).makeItemVisible(((mList)c).itemsSize()-1);
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
		if (row != -2 && col != -2 && !canSelect(row,col)) return;
		cursor.set(col,row);
		if (selectNew){
			clearSelectedCells(oldExtendedSelection);
			paintCells(null,oldExtendedSelection);
			if (row != -2 && col != -2){
				if (scrollToVisible(row,col)) repaintNow();
				addToSelection(Rect.buff.set(0,row,model.numCols,1),true);
				//fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
				clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
				if (clickMode) clicked(row,col);
				clickedFlags = 0;
			}
		}
	 }

	 /**
	  * this is only necessary to hinder the user to unselect
	  */
	 public void penReleased(Point p,boolean isDouble)
	 {
		 Point p2 = cellAtPoint(p.x,p.y,null);
		 super.penReleased(p, isDouble);
		 Rect sel = getSelection(null); 
		 if ((sel.height == 0 || sel.height == 0) && p2 != null) cursorTo(p2.y,p2.x, true); // if the selection is gone -> reselect it 
			 
	 }

	 class myProgressBarForm extends ProgressBarForm {

		 boolean isClosed=false;
		 
		 protected boolean canExit(int exitCode) {
			isClosed=true;
			return true;
		 }
		 
	 }
	 
	 

	public Menu getMenuSmall() {
		return mSmall;
	}
}
