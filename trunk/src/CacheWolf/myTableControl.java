package CacheWolf;

import CacheWolf.utils.CWWrapper;
import CacheWolf.utils.FileBugfix;
import ewe.sys.*;
import ewe.ui.*;
import ewe.fx.*;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.IOException;

/**
 *	Implements the user interaction of the list view. Works together with myTableModel and TablePanel
 */
public class myTableControl extends TableControl{

	public Preferences pref;
	public Profile profile;
	public CacheDB cacheDB;
	public TablePanel tbp;

	private MenuItem miOpen, miGoto, miCenter, miUnhideAddis;
	private MenuItem miOpenOnline, miOpenOffline, miLogOnline, miOpenGmaps;
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

		MenuItem[] mnuFull = new MenuItem[15];
  	mnuFull[0] = miOpen = new MenuItem(MyLocale.getMsg(1021,"Open description"));
  	mnuFull[1] = miGoto = new MenuItem(MyLocale.getMsg(1010,"Goto"));
  	mnuFull[2] = miCenter = new MenuItem(MyLocale.getMsg(1019,"Center"));
  	mnuFull[3] = miUnhideAddis = new MenuItem(MyLocale.getMsg(1042,"Unhide Addis"));
  	mnuFull[4] = miSeparator = new MenuItem("-");
  	mnuFull[5] = miOpenOnline = new MenuItem(MyLocale.getMsg(1020,"Open in $browser online"));
  	mnuFull[6] = miOpenOffline = new MenuItem(MyLocale.getMsg(1018,"Open in browser offline"));
  	mnuFull[7] = miLogOnline = new MenuItem(MyLocale.getMsg(1052,"Log online in Browser"));
  	mnuFull[8] = miOpenGmaps = new MenuItem(MyLocale.getMsg(1053,"Open in Google maps online"));
  	mnuFull[9] = miSeparator;
  	mnuFull[10] = miDelete = new MenuItem(MyLocale.getMsg(1012,"Delete selected"));
  	mnuFull[11] = miUpdate = new MenuItem(MyLocale.getMsg(1014,"Update"));
  	mnuFull[12] = miSeparator;
  	mnuFull[13] = miTickAll = new MenuItem(MyLocale.getMsg(1015,"Select all"));
  	mnuFull[14] = miUntickAll = new MenuItem(MyLocale.getMsg(1016,"De-select all"));
  	mFull = new Menu(mnuFull, MyLocale.getMsg(1013,"With selection"));

  	MenuItem[] mnuSmall = new MenuItem[8];
  	mnuSmall[0] = miOpen;
  	mnuSmall[1] = miGoto;
  	mnuSmall[2] = miCenter;
  	mnuSmall[3] = miUnhideAddis;
  	mnuSmall[4] = miSeparator;
  	mnuSmall[5] = miOpenOnline;
  	mnuSmall[6] = miOpenOffline;
  	mnuSmall[7] = miLogOnline;
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
		if (cacheDB.size()>0) { // No context menu when DB is empty
			adjustAddiHideUnhideMenu();
			menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table

		}
	}

    public void penHeld(Point p){
		if (cacheDB.size()>0) // No context menu when DB is empty
			adjustAddiHideUnhideMenu();
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
					Global.mainTab.gotoP.setDestinationAndSwitch(ch);
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
	public boolean isSelected(int pRow,int pCol) {
		return pRow==selection.y;
	}
	private void callExternalProgram(String program, String parameter) {
		// invalid path will be handled by try
		try {
			CWWrapper.exec(program, parameter); // maybe this works on some PDAs?
		} catch (IOException ex) {
			(new MessageBox(MyLocale.getMsg(321,"Error"),
					MyLocale.getMsg(1034,"Cannot start "+program+"!") + "\n" + ex.toString() + "\n" +
					MyLocale.getMsg(1035,"Possible reason:") + "\n" +
					MyLocale.getMsg(1036,"A bug in ewe VM, please be") + "\n" +
					MyLocale.getMsg(1037,"patient for an update"),FormBase.OKB)).execute();
		}
	}

	public void popupMenuEvent(Object selectedItem){
		if (selectedItem == null) return;
		CacheHolder ch;
		if (selectedItem == miTickAll){
			setSelectForAll(true);
		} else

		if (selectedItem == miUntickAll){
			setSelectForAll(false);
		} else

		if (selectedItem == miDelete){
			Vm.showWait(true);
			// Count # of caches to delete
			int allCount=0;
			int mainNonVisibleCount=0;
			int addiNonVisibleCount=0;
			int shouldDeleteCount=0;
			boolean deleteFiltered=true;  // Bisheriges Verhalten
			for(int i = cacheDB.size()-1; i >=0; i--){
				CacheHolder currCache = cacheDB.get(i);
				if ( currCache.is_Checked) {
					allCount++;
					if (! currCache.isVisible()) {
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
			if (addiNonVisibleCount + mainNonVisibleCount > 0){
				if ((new MessageBox(MyLocale.getMsg(144,"Warning"),
						            MyLocale.getMsg(1029, "There are caches that are ticked but invisible.\n(Main caches: ") +
						            	mainNonVisibleCount + MyLocale.getMsg(1030, ", additional Waypoints: ") +
						            	addiNonVisibleCount+")\n" + MyLocale.getMsg(1031, "Delete them, too?"),
						            	FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
					deleteFiltered = true;
				} else {
					deleteFiltered = false;
					shouldDeleteCount = allCount - mainNonVisibleCount - addiNonVisibleCount;
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
					String[] CacheFiles = new FileBugfix(profile.dataDir).list(null, FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
					for(int i = size-1; i >=0; i--){// Start Counting down, as the size decreases with each deleted cache
						ch = cacheDB.get(i);
						if(ch.is_Checked && (ch.isVisible() || deleteFiltered)) {
							nDeleted++;
							h.progress = ((float)nDeleted)/(float)allCount;
							h.changed();
							cacheDB.removeElementAt(i);
							dm.deleteCacheFiles(ch.getWayPoint(),profile.dataDir,CacheFiles);
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
		} else

		if (selectedItem == miUpdate){
			MainMenu.updateSelectedCaches(tbp);
		} else

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
				pref.setCurCentrePt(cp);
			}
		} else

		if (selectedItem == miUnhideAddis) {
			// This toggles the "showAddis" Flag
			ch = cacheDB.get(tbp.getSelectedCache());
			ch.setShowAddis(!ch.showAddis());
			if (ch.addiWpts.size()>0) {
				tbp.refreshTable();
			} else {
				// This should never occur, as we check prior to activating the menu if the
				// cache has addis. But just in case...
				new MessageBox(MyLocale.getMsg(4201, "Info"), MyLocale.getMsg(1043, "This cache has no additional waypoints."),FormBase.OKB).execute();
			}
		} else

		if (selectedItem == miGoto){
			ch = cacheDB.get(tbp.getSelectedCache());
			Global.mainTab.gotoP.setDestinationAndSwitch(ch);
		} else

		if (selectedItem == miOpenOnline){
				ch = cacheDB.get(tbp.getSelectedCache());
				CacheHolderDetail chD=ch.getCacheDetails(false, true);
				if (chD != null) { callExternalProgram(pref.browser, chD.URL); }
		} else

		if (selectedItem == miOpenGmaps) {
			ch = cacheDB.get(tbp.getSelectedCache());
			if (ch.pos.isValid()) {
				String latlon=""+ch.pos.getLatDeg(CWPoint.DD)+","+ch.pos.getLonDeg(CWPoint.DD);
				String nameOfCache=ewe.net.URL.encodeURL(ch.cacheName,false).replace('#','N').replace('@','_');
				String language=Vm.getLocale().getString(Locale.LANGUAGE_SHORT, 0, 0);
				if (!pref.language.equalsIgnoreCase("auto")) {language=pref.language;}
				String url="http://maps.google."+language+"/maps?q="+nameOfCache+"@"+latlon;
				callExternalProgram(pref.browser, url);
			}
		} else

		if (selectedItem == miOpenOffline) {
			ShowCacheInBrowser sc=new ShowCacheInBrowser();
			sc.showCache(cacheDB.get(tbp.getSelectedCache()));
		} else

		if (selectedItem == miLogOnline){
				ch = cacheDB.get(tbp.getSelectedCache());
				CacheHolder mainCache = ch;
				if (ch.isAddiWpt() && (ch.mainCache != null)) {
					mainCache = ch.mainCache;
				}
				if (mainCache.isCacheWpt()) {
					CacheHolderDetail chD=mainCache.getCacheDetails(false, true);
						if (chD != null) {
							String URL = "";
							if (ch.isOC()) {
								URL = chD.URL;
								if (URL.indexOf("viewcache") >= 0) {
									URL = STRreplace.replace(URL, "viewcache", "log");
								} else {
									URL = "";
								}
							} else {
								URL = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.GetCacheID();
							}

							if (URL.length() > 0) {
								String notes = chD.getCacheNotes();
								if (notes.length() > 0) {
									Vm.setClipboardText(notes);
								}
								callExternalProgram(pref.browser, URL);
							}
						}
				}
		} else

		if (selectedItem == miOpen){
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

	/**
	 * Adjusting the menu item for hiding or unhiding additional waypoints. If the cache has no
	 * addis, then the menu is deactivated. If it has addis, then the menu text is adapted according
	 * to the current value of the property <code>showAddis()</code>.
	 *
	 */
	public void adjustAddiHideUnhideMenu() {
		if (tbp.getSelectedCache() < 0) {
			return;
		}
		CacheHolder selCache = cacheDB.get(tbp.getSelectedCache());
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
    ///////////////////////////////////////////////////

	IconAndText imgDrag;
	String wayPoint;
	int row;

	public void startDragging(DragContext dc) {
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
			 imgDrag.addColumn(GuiImageBroker.getTypeImage(ch.getType()));
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

	 public void cursorTo(int pRow,int pCol,boolean selectNew) {
		if (pRow != -2 && pCol != -2 && !canSelect(pRow,pCol)) return;
		cursor.set(pCol,pRow);
		if (selectNew){
			clearSelectedCells(oldExtendedSelection);
			paintCells(null,oldExtendedSelection);
			if (pRow != -2 && pCol != -2){
				if (scrollToVisible(pRow,pCol)) repaintNow();
				addToSelection(Rect.buff.set(0,pRow,model.numCols,1),true);
				//fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
				clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
				if (clickMode) clicked(pRow,pCol);
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
