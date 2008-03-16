package CacheWolf;

import utils.CWWrapper;
import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.io.IOException;
import ewe.util.*;

/**
 *	Implements the user interaction of the list view. Works together with myTableModel and TablePanel
 */
public class myTableControl extends TableControl{

	public Preferences pref;
	public Profile profile;
	public Vector cacheDB;
	public TablePanel tbp;
	private Menu mFull = new Menu(new String[]{
			MyLocale.getMsg(1021,"Open description"),
			MyLocale.getMsg(1010,"Goto"),
			MyLocale.getMsg(1019,"Enter"),
			"-",
			MyLocale.getMsg(1020,"Open in $browser online"),
			MyLocale.getMsg(1018,"Open in browser offline"),
			"-",
			MyLocale.getMsg(1012,"Delete selected"),
			"-",
			MyLocale.getMsg(1015,"Select all"),
			MyLocale.getMsg(1016,"De-select all")},
			MyLocale.getMsg(1013,"With selection"));
	private Menu mSmall = new Menu(new String[]{
			MyLocale.getMsg(1021,"Open description"),
			MyLocale.getMsg(1010,"Goto"),
			MyLocale.getMsg(1019,"Enter"),
			"-",
			MyLocale.getMsg(1020,"Open in $browser online"),
			MyLocale.getMsg(1018,"Open in browser offline")},
			MyLocale.getMsg(1013,"With selection"));

	myTableControl(TablePanel tablePanel) {
		profile=Global.getProfile();
		cacheDB = profile.cacheDB;
		pref = Global.getPref();
		tbp =tablePanel;
		allowDragSelection = false; // allow only one row to be selected at one time
	}

	/** Full menu when listview includes checkbox */
	public void setMenuFull() {
		setMenu(mFull);
//		if (!Vm.getPlatform().equals("Win32") && !Vm.getPlatform().equals("Java"))
//		   ((MenuItem)mFull.items.get(5)).modifiers|=MenuItem.Disabled;
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
				else if (ev.key == IKeys.LEFT && Global.mainForm.cacheListVisible && cursor.y>=0 && cursor.y<tbp.myMod.numRows) Global.mainForm.cacheList.addCache(((CacheHolder)cacheDB.elementAt(cursor.y)).wayPoint); 
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
		CacheHolder ch;
		if (selectedItem.toString().equals(MyLocale.getMsg(1015,"Select all"))){
			setSelectForAll(true);
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1016,"De-select all"))){
			setSelectForAll(false);
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1012,"Delete"))){
			Vm.showWait(true);
			// Count # of caches to delete
			int count=0;
			for(int i = cacheDB.size()-1; i >=0; i--){
				if ( ((CacheHolder)cacheDB.get(i)).is_Checked) count++;
			}
			if (count>0) {
				if ((new MessageBox(MyLocale.getMsg(144,"Warnung"),MyLocale.getMsg(1022, "Delete all caches that have a tick?"), MessageBox.YESB | MessageBox.NOB)).execute() != Form.IDYES) return;
				DataMover dm=new DataMover();
				myProgressBarForm pbf = new myProgressBarForm();
				Handle h = new Handle();
				pbf.setTask(h,MyLocale.getMsg(1012, "Delete selected"));
				pbf.exec();
				int nDeleted=0;
				int size=cacheDB.size();
				for(int i = size-1; i >=0; i--){// Start Counting down, as the size decreases with each deleted cache
					ch = (CacheHolder)cacheDB.get(i);
					if(ch.is_Checked == true) {
						nDeleted++;
						h.progress = ((float)nDeleted)/(float)count;
						h.changed();
						dm.deleteCacheFiles(ch.wayPoint,profile.dataDir);
						cacheDB.remove(ch);
						ch.releaseCacheDetails();
						ch=null;
						if (pbf.isClosed) break;
					}
				}
				pbf.exit(0);
				profile.saveIndex(pref,true);	
				tbp.refreshTable();
			}
			Vm.showWait(false);
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1019,"Centre"))){
			CacheHolder thisCache = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			CWPoint cp=new CWPoint(thisCache.LatLon);
			if (!cp.isValid()){
				MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
				tmpMB.execute();
			} else {				
				pref.curCentrePt.set(cp);
				Global.mainTab.updateBearDist(); // Update the distances with a warning message
				//tbp.refreshTable();
			}
		}

		if (selectedItem.toString().equals(MyLocale.getMsg(1010,"Goto"))){
			ch = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			Global.mainTab.gotoPoint(ch.pos);
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1020,"Open online in Browser"))){
			ch = (CacheHolder)cacheDB.get(tbp.getSelectedCache());
			CacheHolderDetail chD=ch.getCacheDetails(false, true);
			try {
				if (chD != null) {
					//String cmd = "\""+pref.browser+ "\" \"" + chD.URL+"\"";
					CWWrapper.exec(pref.browser, chD.URL); // maybe this works on some PDAs?
				}
			} catch (IOException ex) {
				(new MessageBox("Error", "Cannot start browser!\n"+ex.toString()+"\nThe are two possible reasons:\n * path to internet browser in \npreferences not correct\n * An bug in ewe VM, please be \npatient for an update",MessageBox.OKB)).execute();
			}
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1018,"Open in browser offline"))) {
			ShowCacheInBrowser sc=new ShowCacheInBrowser();
			sc.showCache(((CacheHolder)cacheDB.get(tbp.getSelectedCache())).getCacheDetails(false, true));
		}
		if (selectedItem.toString().equalsIgnoreCase(MyLocale.getMsg(1021,"Open description"))){
			penDoubleClicked(null);
		}

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
			 imgDrag.addColumn((IImage) CacheType.cache2Img(ch.type));
			 imgDrag.addColumn(ch.wayPoint);
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
	 
	 class myProgressBarForm extends ProgressBarForm {

		 boolean isClosed=false;
		 
		 protected boolean canExit(int exitCode) {
			isClosed=true;
			return true;
		 }
		 
	 }
}
