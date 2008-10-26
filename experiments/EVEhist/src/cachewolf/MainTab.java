package cachewolf;

import eve.sys.*;
import eve.ui.*;
import eve.fx.*;
import java.util.*;

import cachewolf.navi.GotoPanel;
import cachewolf.navi.MapImage;
import cachewolf.navi.MovingMap;
import cachewolf.navi.Navigate;

import eve.ui.event.*;
import eve.ui.table.*;

/**
 *	This class creates the tabbed panel and sets the tabs to the respective
 *	other panels. Important is to have a look at the event handler!<br>
 *	@see MainForm
 *	@see MainMenu
 */
public class MainTab extends TabbedPanel {
	DescriptionPanel descP=new DescriptionPanel();
	HintLogPanel hintLP = new HintLogPanel();
	TablePanel tbP;
	Vector cacheDB;
	public DetailsPanel detP = new DetailsPanel();
	CalcPanel calcP;
	Preferences pref;
	Profile profile;
	GotoPanel gotoP; 
	RadarPanel radarP = new RadarPanel();
	ImagePanel imageP;
	SolverPanel solverP;
	String lastselected = "";
	public CacheHolder ch=null;
	CacheHolderDetail chD =null, chMain=null;
	MainMenu mnuMain;
	StatusBar statBar;
	public MovingMap mm;
	Navigate nav;
	public String mainCache="";
	int oldCard=0;
	boolean cacheDirty=false;
	// These flags are set if data is put into the relevant panels
	// They ensure that the panel is cleared, when a new cache is selected
	boolean imagePhasData=false;
	boolean descPhasData=false;
	boolean detPhasData=false;
	boolean hintLPhasData=false;
	boolean solverPhasData=false;
	boolean radarPhasData=false;
	
	public MainTab(MainMenu mainMenu,StatusBar statBar){
		Global.mainTab=this;
		mnuMain=mainMenu;
		pref = Global.getPref();
		profile=Global.getProfile();
		if (!pref.tabsAtTop) tabLocation=SOUTH;
		cacheDB = profile.cacheDB;
		this.statBar=statBar;
		//TODOMyLocale.setSIPButton();
		//Don't expand tabs if the screen is very narrow, i.e. HP IPAQ 65xx, 69xx
		int sw = MyLocale.getScreenWidth();
		if ( sw <= 240) this.dontExpandTabs=true;
		String imagesize="";
		if (Device.isMobile() && sw >= 400) imagesize="_vga";  
		calcP = new CalcPanel(); // Init here so that Global.MainT is already set
		tbP = new TablePanel(statBar);
		Card c = this.addCard(new TableForm(tbP), MyLocale.getMsg(1200,"List"), null);
		
		c = this.addCard(detP, MyLocale.getMsg(1201,"Details"), null);
		c.iconize(new Picture("details"+imagesize+".gif"),true);

		c = this.addCard(descP, MyLocale.getMsg(1202,"Description"), null);
		c.iconize(new Picture("descr"+imagesize+".gif"),true);

		c = this.addCard(new MyScrollBarPanel(imageP = new ImagePanel()), MyLocale.getMsg(1203,"Images"), null);
		c.iconize(new Picture("images"+imagesize+".gif"),true);

		c = this.addCard(hintLP, MyLocale.getMsg(1204,"Hints & Logs"), null);
		c.iconize(new Picture("more"+imagesize+".gif"),true);

		c = this.addCard(solverP = new SolverPanel(), MyLocale.getMsg(1205,"Solver"), null);
		c.iconize(new Picture("solver"+imagesize+".gif"),true);

		c = this.addCard(calcP, MyLocale.getMsg(1206,"Calc"), null);
		Picture imgCalc=new Picture("projecttab"+imagesize+".gif",new Color(0,255,0),0); 
		c.iconize(imgCalc,true);

		nav = new Navigate();
		c = this.addCard(gotoP = new GotoPanel(nav), "Goto", null);
		c.iconize(new Picture("goto"+imagesize+".gif"),true);
		nav.setGotoPanel(gotoP);

		c = this.addCard(radarP, "Radar", null);
		c.iconize(new Picture("radar"+imagesize+".gif"),true);
		mnuMain.allowProfileChange(true);
	}
	
	public TablePanel getTablePanel(){
		return tbP;
	}

	public void selectAndActive(int rownum){// Called from myInteractivePanel.imageClicked
		tbP.selectRow(rownum);
		this.selectAndExpand(0);
	}

	public void clearDetails() {
		if (imagePhasData) { imageP.clearImages(); imagePhasData=false; }// Remove all images
		if (descPhasData) { descP.clear(); descPhasData=false; }// write "loading ..."
		if (detPhasData) { detP.clear(); detPhasData=false; }// Clear only the attributes
		if (hintLPhasData) { hintLP.clear(); hintLPhasData=false; }// Remove the logs
		if (solverPhasData) { solverP.setInstructions("loading ..."); solverPhasData=false; }
		if (radarPhasData) { radarP.removeCircle(); radarPhasData=false; }
	}
	
	static int level=0;
	public void onEvent(Event ev) {
//eve.sys.Vm.debug("              ".substring(0,level++*2)+"["+ev.target.getClass().getName()+"] "+event2Name(ev.type));
		// This section clears old data when a new line is selected in the table
		if (ev instanceof TableEvent) {
			clearDetails();
		}
		if(ev instanceof MultiPanelEvent){
			// Check whether a profile change is allowed, if not disable the relevant options
			checkProfileChange();
			// Perform clean up actions for the panel we are leaving
			onLeavingPanel(oldCard);
			// Prepare actions for the panel we are about to enter
			onEnteringPanel(((MultiPanelEvent)ev).selectedIndex);
			oldCard=((MultiPanelEvent)ev).selectedIndex;
		}
		// If we are in Listview update status
		if (this.getSelectedItem()==0 && statBar!=null) statBar.updateDisplay();
//eve.sys.Vm.debug("              ".substring(0,--level*2)+"END "+"["+ev.target.getClass().getName()+"] "+event2Name(ev.type));
		super.onEvent(ev);
	}

	/**
	 * Code to execute when leaving a panel (oldCard is the panel number)
	 *
	 */
	private void onLeavingPanel(int panelNo) {//Vm.debug("Leaving "+panelNo);
		if (panelNo==0) { // Leaving the list view
			// Get the cache for the current line (ch)
			// Get the details for the current line (chD)
			// If it is Addi get details of main Wpt (chMain)
			chMain=null;
			cacheDirty=false;
			if (tbP.getSelectedCache()>=Global.mainTab.tbP.tModel.numRows || tbP.getSelectedCache()<0) {
				ch=null; chD=null; 
				lastselected="";
			} else {
				ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
				lastselected=ch.wayPoint;  // Used in Parser.Skeleton
				try {
					chD = ch.getCacheDetails(true);
					//chD=new CacheHolderDetail(ch);
					//chD.readCache(profile.dataDir);//Vm.debug("MainTab:readCache "+chD.wayPoint+"/S:"+chD.Solver);
				} catch(Exception e){
					//Vm.debug("Error loading: "+ch.wayPoint);
				}
			}
		}
		else if (panelNo==1) { // Leaving the Details Panel
			// Update chD with Details
			if(detP.isDirty()) {
				cacheDirty=true;
				boolean needTableUpdate = detP.needsTableUpdate();
				detP.saveDirtyWaypoint();
				if (needTableUpdate) {
					tbP.updateRows();// This sorts the waypoint (if it is new) into the right position
					tbP.selectRow(profile.getCacheIndex(detP.thisCache.wayPoint));
				}
				//was tbP.refreshTable();
				tbP.tControl.update(true); // Update and repaint
				if (statBar!=null) statBar.updateDisplay();
			}
		}
		else if (panelNo==5) { // Leaving the Solver Panel
			// Update chD or chMain with Solver
			// If chMain is set (i.e. if it is an addi Wpt) save it immediately
			if (chD!=null && solverP.isDirty()) {
				cacheDirty=true;
				if (chMain==null) {
					chD.solver=solverP.getInstructions();
				} else {
					chMain.solver=solverP.getInstructions();
					chMain.saveCacheDetails(Global.getProfile().dataDir);//Vm.debug("mainT:SaveCache "+chMain.wayPoint+"/S:"+chMain.Solver);
					chMain=null;
				}
			}
		}
	}

	/**
	 * Code to execute when entering a panel (getSelectedItem() is the panel number)
	 *
	 */
	private void onEnteringPanel(int panelNo) {//Vm.debug("Entering "+panelNo);
		switch (panelNo) {// Switch by panel number
		case 0:
			// If Solver or Details has changed, save Cache
			updatePendingChanges();
			if (detP.hasBlackStatusChanged()) {
				// Restore the filter status (this automatically sets the status for blacklisted caches)
				Global.getProfile().restoreFilter(true);
				tbP.refreshTable();
			}
			break;
		case 1:  // DetailsPanel
			if (chD==null) { // Empty DB - show a dummy detail
				newWaypoint(ch=new CacheHolder()); 
			}
			//TODO MyLocale.setSIPButton();
			if (!detPhasData)  {
				detP.setDetails(ch);
				detPhasData=true;
			}
			break;
		case 2: // Description Panel
				//TODOMyLocale.setSIPOff();
				if (!descPhasData) {
					descP.setText(chD);
					descPhasData=true;
				}
			break;
		case 3: // Picture Panel
			if (chD!=null && !imagePhasData) {
				//TODOMyLocale.setSIPOff();
				if (chD.isAddiWpt()) { 
					imageP.setImages(chD.mainCache.getCacheDetails(true));
				} else {
					imageP.setImages(chD);
				}
				imagePhasData=true;
			}
			break;
		case 4:  // Log Hint Panel
			if (chD!=null && !hintLPhasData) {
				//TODO MyLocale.setSIPOff();
				if (chD.isAddiWpt()) { 
					hintLP.setText(chD.mainCache.getCacheDetails(true));
				} else {
					hintLP.setText(chD);
				}
				hintLPhasData=true;
			}
			break;
		case 5:  // Solver Panel
			//TODO MyLocale.setSIPButton();
			if (chD!=null && !solverPhasData) {
				if (chD.isAddiWpt()) { 
					chMain=chD.mainCache.getCacheDetails(true);//new CacheHolderDetail(chD.mainCache);
/*					try {
						chMain.readCache(profile.dataDir); //Vm.debug("mainT:readCache "+chD.wayPoint+"=>Main=>"+chMain.wayPoint+"/S:"+chMain.Solver);
					} catch(Exception e){pref.log("Error reading cache .xml",e);}
*/					solverP.setInstructions(chMain.solver);
				} else {
					//Vm.debug("mainT: Waypoint:"+chD.wayPoint);
					solverP.setInstructions(chD.solver);
				}
				solverPhasData=true;
			}
			break;
		case 6:  // CalcPanel
			if (chD!=null) {
				//TODO MyLocale.setSIPOff();
				calcP.setFields(chD);
			}
			break;
		case 7: // GotoPanel
			//TODO MyLocale.setSIPOff();
			break;
		case 8:  // Cache Radar Panel
			//TODO MyLocale.setSIPOff();
			if (!radarPhasData) {
				radarP.drawCachesAndCircle();
				radarPhasData=true;
			}
			break;
		}
	}
	
	/** Update the distances of all caches to the centre and display a message 
	 */
	public void updateBearDist(){// Called from DetailsPanel, GotoPanel and myTableControl
		MessageBox info = new MessageBox(MyLocale.getMsg(327,"Information"), MyLocale.getMsg(1024,"Entfernungen in der Listenansicht \n werden neu berechnet...").replace('~','\n'), 0);
		info.exec();
		profile.updateBearingDistance();
		//tbP.refreshTable();
		info.close(0);
		//tbP.tControl.repaint();
	}

	public void gotoPoint(CWPoint where) { // Called from CalcPanel, DetailsPanel
		gotoP.setDestinationAndSwitch(where); 
	}

	public void openDescriptionPanel(CacheHolder chi) {
		//TODO MyLocale.setSIPOff();
		// To change cache we need to be in panel 0
		onLeavingPanel(oldCard);
		onEnteringPanel(0); oldCard=0;
		int row = profile.getCacheIndex(chi.wayPoint);
		tbP.selectRow(row);
		//tbP.tc.scrollToVisible(row, 0);
		//tbP.selectRow(row);
		select(descP);
		//descP.setText(chi);
	}


	/**
	 * this is called from goto / MovingMap / CalcPanel / DetailsPanel and so on to 
	 * offer the user the possibility of entering an new waypoint
	 * at a given position. ch must already been preset with a valid
	 * CacheHolder object
	 * 
	 * @param ch
	 */
	public void newWaypoint(CacheHolder ch){
		//When creating a new waypoint, simulate a change to the list view
		//if we are currently NOT in the list view
		if (oldCard != 0) {
			onLeavingPanel(oldCard);
		}
		updatePendingChanges(); // was: onEnteringPanel(0); oldCard=0;
		mainCache=lastselected;
		int selectedIndex = profile.getCacheIndex( lastselected );
		if (selectedIndex >= 0) {
			CacheHolder selectedCache = (CacheHolder) profile.cacheDB.get( selectedIndex );
			if ( selectedCache.isAddiWpt() ) {
				mainCache = selectedCache.mainCache.wayPoint;
			}			
		}
		Global.getProfile().hasUnsavedChanges=true;
		detP.setNeedsTableUpdate(true);
		if (CacheType.isAddiWpt(ch.type) && mainCache!=null && mainCache.length()>2) {
			ch.wayPoint = profile.getNewAddiWayPointName(mainCache);
			profile.setAddiRef(ch);
		} else { 
			ch.wayPoint = profile.getNewWayPointName();
			ch.type=0;
			lastselected=ch.wayPoint;
		}
		ch.setCacheSize("None");
		chD = ch.getCacheDetails(true);
		this.ch = ch;
		cacheDB.add(ch);
		tbP.tModel.numRows++;
		detP.setDetails(ch);
		oldCard=1;
		if (this.cardPanel.selectedItem !=1) select(detP);
		solverP.setInstructions("");
		//tbP.refreshTable(); // moved this instruction to onLeavingPanel

	}

	
	/**
	 * sets posCircle Lat/Lon to centerTo
	 * 
	 * @param centerTo true: centers centerTo on the screen and disconnects MovingMap from GPS if Gps-pos is not on the loaded map
	 * @param forceCenter
	 */
	public void switchToMovingMap(CWPoint centerTo, boolean forceCenter) {
		try {
			if (!centerTo.isValid()) {
				(new MessageBox("Error", "No valid coordinates", MessageBox.OKB)).execute();
				return;
			}
			if (mm == null) {
				mm = new MovingMap(nav, profile.cacheDB);
				nav.setMovingMap(mm);
			} 
			if (forceCenter) mm.setGpsStatus(MovingMap.noGPS); // disconnect movingMap from GPS TODO only if GPS-pos is not on the screen
			mm.updatePosition(centerTo);
			mm.myExec();
			if (forceCenter) {
				try {
					int i = 0;
					while (MapImage.screenDim.width == 0 && i < 10*60) { i++; eve.sys.mThread.sleep(100);} // wait until the window size of the moving map is known note: eve.sys.sleep() will pause the whole vm - no other thread will run
					if (i >= 10*60) {(new MessageBox("Error", "MovingMap cannot be displayed - this is most likely a bug - plaese report it on www.geoclub.de", MessageBox.OKB)).execute(); return;}
					mm.setCenterOfScreen(centerTo, false); // this can only be executed if mm knows its window size that's why myExec must be executed before
					mm.updatePosition(centerTo);
					/*			if(!mm.posCircle.isOnScreen()) { // TODO this doesn't work because lat lon is set to the wished pos and not to gps anymore
				mm.setGpsStatus(MovingMap.noGPS); // disconnect movingMap from GPS if GPS-pos is not on the screen
				mm.setResModus(MovingMap.HIGHEST_RESOLUTION);
				mm.updatePosition(centerTo.latDec, centerTo.lonDec);
				mm.setCenterOfScreen(centerTo, true); 
			}
					 */			//TODO what to do, if there is a map at centerTo, but it is not loaded because of mapSwitchMode == dest & cuurpos und dafür gibt es keine Karte 
				}catch (InterruptedException e) {//TODO switch waiting indication off
					Global.getPref().log("Error starting mavoing map (1): " + e.getMessage(), e, true);
					(new MessageBox("Error", "This must not happen please report to pfeffer how to produce this error message", MessageBox.OKB)).execute(); } 
			}
		} catch (Exception e) { 
			Global.getPref().log("Error starting moving map (2): " + e.getMessage(), e, true);
			(new MessageBox("Error", "Error starting moving map: " + e.getMessage(), MessageBox.OKB)).execute(); }
	}

	public void updatePendingChanges() {
		if (cacheDirty) {
			if (chD!=null)
				chD.saveCacheDetails(Global.getProfile().dataDir);
			cacheDirty=false;
		}
	}
	
	/** Save the index file
	 * 
	 * @param askForConfirmation If true, the save can be cancelled by user
	 */
	public void saveUnsavedChanges(boolean askForConfirmation) {
		if (oldCard!=0) {
			onLeavingPanel(oldCard);
			onEnteringPanel(0);
			oldCard=0;
		}
		updatePendingChanges();
		if (profile.hasUnsavedChanges) profile.saveIndex(true);
		this.tbP.saveColWidth(pref);
		Global.getPref().savePreferences();
	}
	
	private void checkProfileChange() {
		// A panel is selected. Could be the same panel twice
		mnuMain.allowProfileChange(false);	  
		if(this.getSelectedItem() == 0){// List view selected
			mnuMain.allowProfileChange(true);	  
			//TODO MyLocale.setSIPOff();
		}
	}
	private String event2Name(int type) {//TODO Comment out
		switch(type) {
			case ControlEvent.PRESSED: return "ControlEvent.PRESSED"; 
			case ControlEvent.FOCUS_IN: return "ControlEvent.FOCUS_IN";
			case ControlEvent.FOCUS_OUT: return "ControlEvent.FOCUS_OUT";
			case ControlEvent.TIMER: return "ControlEvent.TIMER"; 
			case ControlEvent.CANCELLED: return "ControlEvent.CANCELLED"; 
			case ControlEvent.EXITED: return "ControlEvent.EXITED"; 
			case ControlEvent.MENU_SHOWN: return "ControlEvent.MENU_SHOWN";
			case eve.ui.formatted.TextDisplay.LINES_SPLIT: return "ControlEvent.LINES_SPLIT"; 
			case ControlEvent.STRUCTURE_CHANGED: return "ControlEvent.STRUCTURE_CHANGED";
			case ControlEvent.OPERATION_CANCELLED: return "ControlEvent.OPERATION_CANCELLED";
			case ControlEvent.POPUP_CLOSED: return "ControlEvent.POPUP_CLOSED";
			case DataChangeEvent.DATA_CHANGED: return "DataChangeEvent.DATA_CHANGED"; 
			case MultiPanelEvent.SELECTED: return "MultiPanelEvent.SELECTED"; 
			case TableEvent.CELL_CLICKED: return "TableEvent.CELL_CLICKED"; 
			case TableEvent.SELECTION_CHANGED: return "TableEvent.SELECTION_CHANGED";
			case TableEvent.CELL_DOUBLE_CLICKED:  return "TableEvent.CELL_DOUBLE_CLICKED";
			case eve.ui.data.EditorEvent.CLOSED: return "EditorEvent.CLOSED"; 
			case eve.ui.data.EditorEvent.FROM_CONTROLS: return "EditorEvent.FROM_CONTROLS";
			case eve.ui.data.EditorEvent.OBJECT_SET: return "EditorEvent.OBJECT_SET"; 
			case eve.ui.data.EditorEvent.SHOWN: return "EditorEvent.SHOWN"; 
			case eve.ui.data.EditorEvent.TO_CONTROLS: return "EditorEvent.TO_CONTROLS";
		}
		return Integer.toString(type);
	}

}
// 

