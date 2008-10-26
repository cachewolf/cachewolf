package cachewolf;

import eve.ui.filechooser.FileChooser;
import eve.fx.*;
import eve.sys.*;
import eve.ui.*;
import java.util.*;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import eve.ui.event.KeyEvent;
import eve.fx.gui.IKeys;
import eve.ui.event.MenuEvent;
import eve.ui.event.MultiPanelEvent;
import eve.ui.event.ControlEvent;
import java.io.BufferedReader;
import java.io.FileWriter;


/********************************************************
 * This class implements the core functionality of a flexible cachelist for collecting
 * "Cachetours". Caches can be dragged into the list from the main list view and
 * from the radar panel view. Caches can be removed from the list by dragging them
 * out or selecting them and pressing the "delete" key. Within the list the selected
 * cache can be moved up/down using two buttons. The finished list can be saved and
 * reloaded with the selected position being stored. The list can be applied as
 * a filter to the main list, thereby hiding all caches that are not in the list
 * and sorting the caches according to the list.
 * Created by skg, Februar 2007
 ********************************************************/
public class CacheList extends CellPanel {
    /** The extension for cachelists (CL) */
    private final static String EXTENSION="CL";
	private final static String TITLE=MyLocale.getMsg(188,"CACHETOUR: NEW");
	private static int applyCount=0; // Counts the number of times we apply the list
    CacheList() {
		this.setPreferredSize(100,-1);
		this.equalWidths=true;
		Picture imgDown=new Picture("eve/downarrowsmall.png"); //imgDown.transparentColor=Color.White;
		Picture imgUp=new Picture("eve/uparrowsmall.png"); //imgUp.transparentColor=Color.White;
		// Title
		lblTitle=new Label(TITLE);
		lblTitle.backGround=new Color(0,0,200); lblTitle.foreGround=Color.White;
		addLast(lblTitle,HSTRETCH,HFILL|HCENTER);
		// The actual list
		lstCaches=new myList(10,1,false); lstCaches.text="CacheList";
		lstCaches.addItem(MyLocale.getMsg(180,"Drag caches"));lstCaches.addItem(MyLocale.getMsg(181,"here"));
		ScrollablePanel scp=lstCaches.getScrollablePanel();
		addLast(scp,STRETCH,FILL); scp.setOptions(MyScrollBarPanel.NeverShowHorizontalScrollers);
		// The buttons to move the selected cache
		addNext(btnDown=new Button(imgDown),HSHRINK,HFILL); btnDown.modify(Disabled,0);
		addLast(btnUp=new Button(imgUp),HSHRINK,HFILL); btnUp.modify(Disabled,0);
		// Buttons to clear, load and save the list
		Panel cp=new Panel(); cp.equalWidths=true;
		cp.addNext(btnNew=new Button(new Picture("clnew.png",new Color(255,0,0),0)),HSTRETCH,HFILL);
		btnNew.setToolTip(MyLocale.getMsg(182,"New list"));
		cp.addNext(btnLoad=new Button(new Picture("clopen.png",new Color(255,0,0),0)),HSTRETCH,HFILL);
		btnLoad.setToolTip(MyLocale.getMsg(183,"Load list"));
		cp.addNext(btnSaveAs=new Button(new Picture("clsaveas.png",new Color(0,255,0),0)),HSTRETCH,HFILL);
		btnSaveAs.setToolTip(MyLocale.getMsg(184,"Save as"));
		cp.addLast(btnSave=new Button(new Picture("clsave.png",new Color(255,0,0),0)),HSTRETCH,HFILL);
		btnSave.setToolTip(MyLocale.getMsg(185,"Save (without confirmation)"));
		addLast(cp,HSTRETCH,HFILL);
		// Button to toggle whether additional waypoints are automatically dragged
		// with the parent waypoint
		addLast(chkAddAddis=new CheckBox(MyLocale.getMsg(193,"add Addis")),HSTRETCH,HFILL);
		chkAddAddis.setToolTip(MyLocale.getMsg(186,"Also drag Addi Wpts"));
		// Finally button to apply the list as a filter
		addLast(btnFilter=new Button(MyLocale.getMsg(189,"Apply List")),HSTRETCH,HFILL);btnFilter.modify(Disabled,0);
		btnFilter.setToolTip(MyLocale.getMsg(190,"Show only these waypoints"));
	}
    /** Flag to ensure the initial message "Caches hierher ziehen" is cleared
     * when the first cache is dragged into the list */
	private boolean needsInit=true;
	/** The actual list. This is mirrored by cacheList */
	private myList lstCaches;
	/** True if there are unsaved changes */
	private boolean dirty=false;
	// The UI elements
	private Label lblTitle;
	private CheckBox chkAddAddis;
	private Button btnDown, btnUp, btnLoad, btnNew, btnSave, btnSaveAs, btnFilter;
	/** This list mirrors the items in the list of selected caches for faster access. When the
     * list of selected caches is manipulated (btnUp, btnDown), this list is also kept up to date
     */
	private Vector cacheList=new Vector(20);
	/** The full filename of the current file */
	private String currFile=null;

	private class myList extends eve.ui.List {
		myList(int rows, int columns, boolean multi) {
			super(rows,columns,multi);
			modify(WantDrag,0);
		}

		//  Allow the caches to be dragged out of the cachelist
		//String wayPoint;
		int idx;

		public void startDragging(DragContext dc) {
			 idx=getSelectedIndex(0);
			 if (idx>=0) {
				 CacheHolder ch=(CacheHolder)cacheList.get(idx);
				 //wayPoint=ch.wayPoint;
				 IconAndText imgDrag=new IconAndText();
				 imgDrag.addColumn( CacheType.cache2Img(ch.type));
				 imgDrag.addColumn(ch.wayPoint);
				 dc.dragData=dc.startImageDrag(imgDrag,new Point(8,8),this);
			 }
		}

		public void dragged(DragContext dc) {
			 	dc.imageDrag();
		}

		public void stopDragging(DragContext dc) {
			 dc.stopImageDrag(true);
			 Point p = Gui.getPosInParent(this,getWindow(),null);
			 p.x += dc.curPoint.x;
			 p.y += dc.curPoint.y;
			 Control c = getWindow().findChild(p.x,p.y);
		     if (!(c instanceof myList)) {
		    	 // target is not myList => Remove dragged cache from list
		    	 cacheList.removeElementAt(idx);
		    	 lstCaches.deleteItem(idx);
		    	 repaint();
		    	 changeUpDownButtonStatus();
		     }
		 }

		// Alternative method of deleting a cache from the list through
		// Keyboard interface
		public void onKeyEvent(KeyEvent ev) {
			/* This is a bit of a hack. By default eve sends key events to
			 * this panel. So if the list has not had anything dragged into it,
			 * we redirect the focus to the list view, assuming that that is where
			 * the key event needs to go.
			 */
			if (needsInit && ev.target==this) {
				Gui.takeFocus(Global.mainTab.tbP.tControl, Control.ByKeyboard);
				ev.target=Global.mainTab.tbP.tControl;
				postEvent(ev);
			}
			if (ev.type == KeyEvent.KEY_PRESS && ev.target == this){
				if (ev.key == IKeys.DELETE && cacheList.size()>0) {
			    	 idx=getSelectedIndex(0);
					 cacheList.removeElementAt(idx);
			    	 lstCaches.deleteItem(idx);
			    	 repaint();
			    	 changeUpDownButtonStatus();
				}
			}
			super.onKeyEvent(ev);
		}
		public ScrollablePanel getScrollablePanel() {
			dontAutoScroll = amScrolling = true;
			ScrollBarPanel sp = new MyScrollBarPanel(this);
			sp.modify(0,TakeControlEvents);
			return sp;
		}


	} //******************* myList

	/** Simple sort to ensure that the main list keeps the order of this list */
	private class mySort implements eve.util.Comparer{
		public int compare(Object o1, Object o2){
			CacheHolder oo1 = (CacheHolder)o1;
			CacheHolder oo2 = (CacheHolder)o2;
			return oo1.sort.compareTo(oo2.sort);
		}
	}

	/** Enable the up/down buttons only if at least 2 caches are in the list */
	private void changeUpDownButtonStatus() {
		btnUp.modify(0,Disabled);
		if (needsInit || lstCaches.itemsSize()<2 || lstCaches.getSelectedIndex(0)==0) btnUp.modify(Disabled,0);
		btnDown.modify(0,Disabled);
		if (needsInit || lstCaches.itemsSize()<2 || lstCaches.getSelectedIndex(0)==lstCaches.itemsSize()-1) btnDown.modify(Disabled,0);
		btnUp.repaintNow();
		btnDown.repaintNow();
		// Need at least 2 caches in list to enable it
		btnFilter.modify(0,Disabled);
		if (needsInit || lstCaches.itemsSize()<2) btnFilter.modify(Disabled,0);
		btnFilter.repaintNow();
	}

	public void onEvent(Event ev) {
		if (ev instanceof MenuEvent && ev.type==MenuEvent.SELECTED) {
			if (lstCaches.itemsSize()>0 && !needsInit) {
				int lstCacheIdx=lstCaches.getSelectedIndex(0);
				CacheHolder ch=(CacheHolder)cacheList.get(lstCacheIdx);
				int idx=Global.getProfile().cacheDB.indexOf(ch);
				// Ensure that the main view is updated with the selected cache, i.e.
				// DetailsPanel, HintLog, Pictures etc.
				int activeTab=Global.mainTab.cardPanel.selectedItem;
				if (activeTab==0) {
					// Select the cache also in the main list view
					Global.mainTab.tbP.selectRow(idx);
					Global.mainTab.tbP.tControl.repaint();
				} else {
					// We need to change to the list view first to load a new cache
					Global.mainTab.onEvent(new MultiPanelEvent(0,Global.mainTab,0));
					Global.mainTab.tbP.selectRow(idx);
					Global.mainTab.onEvent(new MultiPanelEvent(0,Global.mainTab,activeTab));
				}
			}
		}
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target==btnNew) {
				newCacheList();
			} else if(ev.target == btnLoad){
				FileChooser fc = new FileChooser(FileChooser.OPEN, Global.getProfile().dataDir);
				//fc.addMask(currCh.wayPoint + ".wl");
				fc.addMask("*."+EXTENSION);
				fc.addMask("*.*");
				fc.title=MyLocale.getMsg(191,"Select File");
				if(fc.execute() != FileChooser.IDCANCEL){
					currFile = fc.getChosen();
					readFromFile(currFile);
				}
			} else if((ev.target == btnSave) && (currFile != null)){
				saveToFile(currFile);
			} else if((ev.target == btnSaveAs)||((ev.target == btnSave) && (currFile == null))){
				FileChooser fc = new FileChooser(FileChooser.SAVE, Global.getProfile().dataDir);
				//fc.addMask(currCh.wayPoint + ".wl");
				fc.addMask("*."+EXTENSION);
				fc.title=MyLocale.getMsg(191,"Select File");
				if(fc.execute() != FileChooser.IDCANCEL){
					currFile = fc.getChosen();
					if (currFile.indexOf('.')==0 || !currFile.toUpperCase().endsWith("."+EXTENSION)) currFile+="."+EXTENSION;
					saveToFile(currFile);
				}
			} else if (ev.target==btnUp) {
				int sel=lstCaches.getSelectedIndex(0);
				if (sel>0) {
					dirty=true;
					// Swap items in hidden list
					Object swap=cacheList.get(sel-1);
					cacheList.setElementAt(cacheList.get(sel),sel-1);
					cacheList.setElementAt(swap,sel);
					// Swap items in visible cachelist and repaint
					swap=lstCaches.items.get(sel-1);
					lstCaches.items.setElementAt(lstCaches.items.get(sel),sel-1);
					lstCaches.items.setElementAt(swap,sel);
					lstCaches.repaintDataNow();
					lstCaches.select(sel-1);
				}
			} else if (ev.target==btnDown) {
				int sel=lstCaches.getSelectedIndex(0);
				if (sel<lstCaches.itemsSize()-1) {
					dirty=true;
					// Swap items in hidden list
					Object swap=cacheList.get(sel+1);
					cacheList.setElementAt(cacheList.get(sel),sel+1);
					cacheList.setElementAt(swap,sel);
					// Swap items in visible cachelist and repaint
					swap=lstCaches.items.get(sel+1);
					lstCaches.items.setElementAt(lstCaches.items.get(sel),sel+1);
					lstCaches.items.setElementAt(swap,sel);
					lstCaches.repaintDataNow();
					lstCaches.select(sel+1);
				}
			} else if (ev.target==btnFilter) {
				applyCacheList();			}
		}
		changeUpDownButtonStatus();
	}

	/** Apply the cache list */
	public void applyCacheList() {
		Global.getProfile().selectionChanged = true;
		Vector cacheDB=Global.getProfile().cacheDB;
		CacheHolder ch;
		int wrongBlackStatus=0;
		String apply="\uFFFF"+Convert.toString(applyCount++);
		// Start by setting all caches to filtered
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered=true ; // ignore blacklist attribute
			ch.sort=apply;
		}
		// Now "unfilter" the caches in our list
		for (int i = cacheList.size()-1; i>=0; i--) {
			ch = (CacheHolder)cacheList.get(i);
			/* If the cache was reloaded from a GPX file since we dragged it into the list,
			   the pointer ch points to a CacheHolder object that is no longer part of cacheDB.
			   In this case we need to search the cacheDB for an object with the name of ch.wayPoint
			   and use that object. To speed up this process and avoid having to search the whole
			   cacheDB for each entry in cacheList, we simply compare the sort field of ch to apply.
			*/
			if (!ch.sort.equals(apply)) {
				int idx=Global.getProfile().getCacheIndex(ch.wayPoint);
				if (idx==-1) continue;
				ch=null;
				ch=(CacheHolder) cacheDB.get(idx);
			}
			if (ch.is_black!=Global.getProfile().showBlacklisted)
				wrongBlackStatus++;
			else {
				ch.is_filtered=false;
				ch.sort=MyLocale.formatLong(i,"00000");
			}
		}
		// The sort command places all filtered caches at the end
		eve.util.Utils.sort(new Handle(),cacheDB, new mySort(),false);
		Global.getProfile().filterActive=Filter.FILTER_CACHELIST;
		Global.getProfile().filterInverted=false;
		Global.getProfile().hasUnsavedChanges=true;
		updateScreen(cacheList.size()-wrongBlackStatus);
		if (wrongBlackStatus>0)
			(new MessageBox(MyLocale.getMsg(5500,"Error"),MyLocale.getMsg(4600,"Some cache(s) cannot be shown because of wrong blacklist status"), MessageBox.OKB)).execute();

	}

	/** Add a cache (and its addis) to the list
	 * @return true if the cache is not already in lstCaches */
	public boolean addCache(String wayPoint) {
		// Check whether this is the first cache being added
		if (needsInit)  {lstCaches.deleteItem(0);lstCaches.deleteItem(0);  needsInit=false; lstCaches.repaint(); }
		int idx=Global.getProfile().getCacheIndex(wayPoint);
		if (idx==-1) return false;
		CacheHolder ch=(CacheHolder) Global.getProfile().cacheDB.get(idx);
		boolean cachesAdded=false;
		// Add main cache
		cachesAdded|=addCache(ch);
		// Add addis if user wants it
		if (chkAddAddis.state && ch.hasAddiWpt()) {
			CacheHolder addiWpt;
			ch.allocAddiMem();
			for (int j=0; j<ch.addiWpts.size();j++){
				addiWpt = (CacheHolder)ch.addiWpts.get(j);
				if (!addiWpt.is_filtered) cachesAdded|=addCache(addiWpt);
			}
		}
		// Update screen if any cache was added
		if (cachesAdded) {
			lstCaches.select(lstCaches.itemsSize()-1);
			changeUpDownButtonStatus();
		}
		return cachesAdded;
	}

	/** Add a cache to the visible and invisible list */
	private boolean addCache(CacheHolder ch) {
		if (cacheList.indexOf(ch)<0) {
			// Add cache reference to hidden list
			cacheList.add(ch);
			// Add Cache and cache icon to visible list
			lstCaches.addItem((new MenuItem()).iconize(ch.wayPoint+"   "+ch.cacheName,CacheType.cache2Img(ch.type),true));
		    dirty=true;
			return true;
		}
		return false;
	}

	void updateScreen(int numRows) {
		Global.mainTab.tbP.tModel.numRows=numRows;
		// Check whether the currently selected cache is still visible
		//selectRow(getSelectedCache());
		Global.mainTab.tbP.tControl.update(true); // Update and repaint
		if (Global.mainTab.tbP.statBar!=null) Global.mainTab.tbP.statBar.updateDisplay();
		int selPanel;
		if ((selPanel=Global.mainTab.cardPanel.selectedItem)>-1) {
			if (selPanel==1) {
				//postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,Global.mainTab,1));
				Global.mainTab.detP.repaint();
			}
		}
	}

	/** Check if there are any unsaved changes and ask user if he wants to save */
	public void saveIfDirty() {
		if (dirty) {
			if ((new MessageBox(MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(192,"Save changes"),MessageBox.MBYESNO)).execute()==MessageBox.IDYES) {
				if (currFile!=null)
					saveToFile(currFile);
				else {
					FileChooser fc = new FileChooser(FileChooser.SAVE, Global.getProfile().dataDir);
					fc.addMask("*."+EXTENSION);
					fc.title=MyLocale.getMsg(191,"Select File");
					if(fc.execute() != FileChooser.IDCANCEL){
						currFile = fc.getChosen();
						saveToFile(currFile);
					}
				}
			}
		}
		dirty=false;
	}

	/** Clear the cachelist (save unsaved changes if needed) */
	private void newCacheList() {
		saveIfDirty();
		lstCaches.items.clear();
		cacheList.clear();
		lstCaches.repaint();
		lblTitle.setText(TITLE);
		currFile=null;
	}

	/** Read a list of caches */
	private void readFromFile(String fileName) {
		if (needsInit)  {lstCaches.deleteItem(0);lstCaches.deleteItem(0);  needsInit=false; }
		int select=-1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String wayPoint;
			int idx;
			Vector cacheDB=Global.getProfile().cacheDB;
			int lineNr=0;
			while ((wayPoint = in.readLine()) != null){
				wayPoint=wayPoint.trim();
				// Select the cache starting with >
				if (wayPoint.startsWith(">")) {
					wayPoint=wayPoint.substring(1);
					select=lineNr;
				}
				// Only add the cache if it is in this profile
				idx=Global.getProfile().getCacheIndex(wayPoint);
				if (idx>=0) {
					addCache((CacheHolder) cacheDB.get(idx));
				}
				lineNr++;
			}
			in.close();
		} catch(Exception e) {
			Global.getPref().log("Problem reading: " +fileName,e,true);
		}
		if (select>-1)
			lstCaches.select(select);
		else
		    lstCaches.select(lstCaches.itemsSize()-1);
		lstCaches.repaint();
		this.postEvent(new MenuEvent(MenuEvent.SELECTED,this,null));
		changeUpDownButtonStatus();
		setTitle(fileName);
		dirty=false;
	}

	/** Save the cachelist */
	private void saveToFile(String fileName) {
		int selectedIndex=lstCaches.getSelectedIndex(0);
		try {
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			for (int i=0; i<cacheList.size(); i++) {
				// Put a > in front of the selected cache
				outp.print((i==selectedIndex?">":"")+((CacheHolder)cacheList.get(i)).wayPoint+"\n");
			}
			outp.close();
		} catch(Exception e) {
			Global.getPref().log("Problem saving: " +fileName,e,true);
		}
		setTitle(fileName);
		dirty=false;
	}

	/** Set the title */
	private void setTitle(String fileName) {
		String localFileName=fileName.replace('\\','/');
		// Delete the path preceding the filename
		if (localFileName.lastIndexOf('/')>0)
			localFileName=localFileName.substring(localFileName.lastIndexOf('/')+1);
		// Drop the extension
		if (localFileName.indexOf('.')>0)
			lblTitle.setText(localFileName.substring(0,localFileName.indexOf('.')));
		else
			lblTitle.setText(localFileName);
		lblTitle.repaint();
	}
}
