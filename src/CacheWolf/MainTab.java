package CacheWolf;

import ewe.sys.Vm;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;

/**
 *	This class creates the tabbed panel and sets the tabs to the respective
 *	other panels. Important is to have a look at the event handler!<br>
 *	Class ID = 1200
 *	@see MainForm
 *	@see MainMenu
 */
public class MainTab extends mTabbedPanel {
	DescriptionPanel descP= new DescriptionPanel();
	HintLogPanel hintLP = new HintLogPanel();
	TablePanel tbP;
	Vector cacheDB;
	DetailsPanel detP = new DetailsPanel();
	CalcPanel calcP;
	Preferences pref;
	Profile profile;
	GotoPanel gotoP; 
	RadarPanel radarP = new RadarPanel();
	ImagePanel imageP;
	SolverPanel solverP;
	String lastselected = "";
	CacheHolder ch=null;
	CacheHolderDetail chD =null, chMain=null;
	MainMenu mnuMain;
	StatusBar statBar;
	MovingMap mm;
	Navigate nav;
	String mainCache="";
	int oldCard=0;
	boolean cacheDirty=false;
	
	public MainTab(MainMenu mainMenu,StatusBar statBar){
		Global.mainTab=this;
		mnuMain=mainMenu;
		pref = Global.getPref();
		profile=Global.getProfile();
		if (!pref.tabsAtTop) tabLocation=SOUTH;
		cacheDB = profile.cacheDB;
		this.statBar=statBar;
		MyLocale.setSIPButton();
		//Don't expand tabs if the screen is very narrow, i.e. HP IPAQ 65xx, 69xx
		if (MyLocale.getScreenWidth() <= 240) this.dontExpandTabs=true;
		calcP = new CalcPanel(); // Init here so that Global.MainT is already set
		tbP = new TablePanel(pref, profile, statBar);
		Card c = this.addCard(new TableForm(tbP), MyLocale.getMsg(1200,"List"), null);

		c = this.addCard(detP, MyLocale.getMsg(1201,"Details"), null);
		c.iconize(new Image("details.gif"),true);

		c = this.addCard(descP, MyLocale.getMsg(1202,"Description"), null);
		c.iconize(new Image("descr.gif"),true);

		c = this.addCard(new ScrollBarPanel(imageP = new ImagePanel()), MyLocale.getMsg(1203,"Images"), null);
		c.iconize(new Image("images.gif"),true);

		c = this.addCard(hintLP, MyLocale.getMsg(1204,"Hints & Logs"), null);
		c.iconize(new Image("more.gif"),true);

		c = this.addCard(solverP = new SolverPanel(pref, profile), MyLocale.getMsg(1205,"Solver"), null);
		c.iconize(new Image("solver.gif"),true);

		c = this.addCard(calcP, MyLocale.getMsg(1206,"Calc"), null);
		c.iconize(new Image("ewe/HandHeld.bmp"),true);

		nav = new Navigate();
		c = this.addCard(gotoP = new GotoPanel(nav), "Goto", null);
		c.iconize(new Image("goto.gif"),true);
		nav.setGotoPanel(gotoP);

		c = this.addCard(radarP, "Radar", null);
		radarP.setMainTab(this);
		c.iconize(new Image("radar.gif"),true);
	}

	public TablePanel getTablePanel(){
		return tbP;
	}

	public void selectAndActive(int rownum){// Called from myInteractivePanel.imageClicked
		tbP.selectRow(rownum);
		this.selectAndExpand(0);
	}


	public void onEvent(Event ev)
	{
		if(ev instanceof MultiPanelEvent){
			// Perform clean up actions for the panel we are leaving
			onLeavingPanel(oldCard);
			// Prepare actions for the panel we are about to enter
			onEnteringPanel(getSelectedItem());
			oldCard=getSelectedItem();
		}
		super.onEvent(ev); //Make sure you call this.
		// If we are in Listview update status
		if (this.getSelectedItem()==0) statBar.updateDisplay();
	}

	/**
	 * Code to execute when leaving a panel (oldCard is the panel number)
	 *
	 */
	private void onLeavingPanel(int panelNo) {
		if (panelNo==0) { // Leaving the list view
			// Get the cache for the current line (ch)
			// Get the details for the current line (chD)
			// If it is Addi get details of main Wpt (chMain)
			chMain=null;
			cacheDirty=false;
			if (tbP.getSelectedCache()>=cacheDB.size() || tbP.getSelectedCache()<0) {
				ch=null; chD=null; 
				lastselected="";
			} else {
				ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
				lastselected=ch.wayPoint;  // Used in Parser.Skeleton
				try {
					chD=new CacheHolderDetail(ch);
					chD.readCache(profile.dataDir);//Vm.debug("MainTab:readCache "+chD.wayPoint+"/S:"+chD.Solver);
				} catch(Exception e){
					//Vm.debug("Error loading: "+ch.wayPoint);
				}
			}
		}
		if (panelNo==1) { // Leaving the Details Panel
			// Update chD with Details
			if(detP.isDirty()) {
				cacheDirty=true;
				detP.saveDirtyWaypoint();
			}
		}
		if (panelNo==5) { // Leaving the Solver Panel
			// Update chD or chMain with Solver
			// If chMain is set (i.e. if it is an addi Wpt) save it immediately
			if (chD!=null && solverP.isDirty()) {
				cacheDirty=true;
				if (chMain==null) {
					chD.Solver=solverP.getInstructions();
				} else {
					chMain.Solver=solverP.getInstructions();
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
	private void onEnteringPanel(int panelNo) {
		switch (panelNo) {// Switch by panel number
		case 0:
			// If Solver or Details has changed, save Cache
			updatePendingChanges();
			if (detP.hasBlackStatusChanged()) {
				// Restore the filter status (this automatically sets the status for blacklisted caches)
				//TODO This is not very elegant (see also SearchCache)
				Filter flt=new Filter();
				if (Filter.filterActive) {
					flt.setFilter();
					flt.doFilter();
				} else {
					flt.clearFilter();
				}
				if (Filter.filterInverted) 
					flt.invertFilter();
				tbP.refreshTable();
			}
			break;
		case 1:  // DetailsPanel
			if (chD==null) { // Empty DB - show a dummy detail
				newWaypoint(chD=new CacheHolderDetail()); 
			}
			MyLocale.setSIPButton();
			detP.setDetails(chD);
			break;
		case 2: // Description Panel
			if (chD!=null) {
				MyLocale.setSIPOff();
				descP.setText(chD);
			}
			break;
		case 3: // Picture Panel
			if (chD!=null) {
				MyLocale.setSIPOff();
				imageP.setImages(chD);
			}
			break;
		case 4:  // Log Hint Panel
			if (chD!=null) {
				MyLocale.setSIPOff();
				hintLP.setText(chD);
			}
			break;
		case 5:  // Solver Panel
			MyLocale.setSIPButton();
			if (chD!=null) {
				if (chD.isAddiWpt()) { 
					chMain=new CacheHolderDetail(chD.mainCache);
					try {
						chMain.readCache(profile.dataDir); //Vm.debug("mainT:readCache "+chD.wayPoint+"=>Main=>"+chMain.wayPoint+"/S:"+chMain.Solver);
					} catch(Exception e){pref.log("Error reading cache",e);}
					solverP.setInstructions(chMain.Solver);
				} else {
					//Vm.debug("mainT: Waypoint:"+chD.wayPoint);
					solverP.setInstructions(chD.Solver);
				}
			}
			break;
		case 6:  // CalcPanel
			if (chD!=null) {
				MyLocale.setSIPButton();
				calcP.setFields(chD);
			}
			break;
		case 7: // GotoPanel
			MyLocale.setSIPButton();
			break;
		case 8:  // Cache Radar Panel
			MyLocale.setSIPOff();
			radarP.setParam(pref, cacheDB, chD==null?"":chD.wayPoint);
			radarP.drawThePanel();
			break;
		}
	}
	
	/** Update the distances of all caches to the center and display a message 
	 */
	public void updateBearDist(){// Called from DetailsPanel, GotoPanel and myTableControl
		tbP.pref = pref;
		profile.updateBearingDistance();
		tbP.refreshTable();
		(new MessageBox(MyLocale.getMsg(327,"Information"), MyLocale.getMsg(1024,"Entfernungen in der Listenansicht \nvom aktuellen Standpunkt aus \nneu berechnet").replace('~','\n'), MessageBox.OKB)).execute();
	}

	public void gotoPoint(String LatLon) { // TODO übergabe nicht als String
		gotoP.setDestinationAndSwitch(LatLon); 
	}

	public void openDesciptionPanel(CacheHolder chi) {
		MyLocale.setSIPOff();
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
		onLeavingPanel(oldCard);
		onEnteringPanel(0); oldCard=0;
		mainCache=lastselected;
		int selectedIndex = profile.getCacheIndex( lastselected );
		if (selectedIndex >= 0) {
			CacheHolder selectedCache = (CacheHolder) profile.cacheDB.get( selectedIndex );
			if ( selectedCache.isAddiWpt() ) {
				mainCache = selectedCache.mainCache.wayPoint;
			}			
		}
		//if (detP.isDirty()) detP.saveDirtyWaypoint();
		Global.getProfile().hasUnsavedChanges=true;
		String waypoint= ch.wayPoint = profile.getNewWayPointName();
		ch.type = "0";
		ch.CacheSize = "None";
		cacheDB.add(ch);
		tbP.myMod.updateRows();
		tbP.selectRow(profile.getCacheIndex(waypoint));
		//Global.mainTab.tbP.refreshTable();
		if (this.cardPanel.selectedItem==1) { // Detailpanel already selected
			postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,detP,0));
		} else	
			select(detP);
		solverP.setInstructions("");
		tbP.refreshTable();

	}

	
	/**
	 * sets posCircle Lat/Lon to centerTo
	 * 
	 * @param centerTo true: centers centerTo on the screen and disconnects MovingMap from GPS if Gps-pos is not on the loaded map
	 * @param forceCenter
	 */
	public void SwitchToMovingMap(CWPoint centerTo, boolean forceCenter) {
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
			mm.updatePosition(centerTo.latDec, centerTo.lonDec);
			mm.myExec();
			if (forceCenter) {
				try {
					int i = 0;
					while (MapImage.screenDim.width == 0 && i < 10*60) { i++; ewe.sys.mThread.sleep(100);} // wait until the window size of the moving map is known note: ewe.sys.sleep() will pause the whole vm - no other thread will run
					if (i >= 10*60) {(new MessageBox("Error", "MovingMap cannot be displayed - this is most likely a bug - plaese report it on www.geoclub.de", MessageBox.OKB)).execute(); return;}
					mm.setCenterOfScreen(centerTo, false); // this can only be executed if mm knows its window size that's why myExec must be executed before
					mm.updatePosition(centerTo.latDec, centerTo.lonDec);
					/*			if(!mm.posCircle.isOnScreen()) { // TODO this doesn't work because lat lon is set to the wished pos and not to gps anymore
				mm.setGpsStatus(MovingMap.noGPS); // disconnect movingMap from GPS if GPS-pos is not on the screen
				mm.setResModus(MovingMap.HIGHEST_RESOLUTION);
				mm.updatePosition(centerTo.latDec, centerTo.lonDec);
				mm.setCenterOfScreen(centerTo, true); 
			}
					 */			//TODO what to do, if there is a map at centerTo, but it is not loaded because of mapSwitchMode == dest & cuurpos und dafür gibt es keine Karte 
				}catch (InterruptedException e) {
					Global.getPref().log("Error starting mavoing map (1): " + e.getMessage(), e, true);
					(new MessageBox("Error", "This must not happen please report to pfeffer how to produce this error message", MessageBox.OKB)).execute(); } 
			}
		} catch (Exception e) { 
			Global.getPref().log("Error starting moving map (2): " + e.getMessage(), e, true);
			(new MessageBox("Error", "Error starting moving map: " + e.getMessage(), MessageBox.OKB)).execute(); }
	}

	void updatePendingChanges() {
		if (cacheDirty) {
			chD.saveCacheDetails(Global.getProfile().dataDir);
			//Vm.debug("mainT: Saveing "+chD.wayPoint);
		}
	}
	
	/** Save the index file
	 * 
	 * @param askForConfirmation If true, the save can be cancelled by user
	 */
	public void saveUnsavedChanges(boolean askForConfirmation) {
		boolean saveIndex=!askForConfirmation; // Definitely save it if no confirmation needed
		updatePendingChanges();
		if (askForConfirmation) { // Don't know whether to save, have to ask
			if (profile.hasUnsavedChanges &&     // Only ask if there were changes 
					(new MessageBox(MyLocale.getMsg(144,"Warnung"),MyLocale.getMsg(1207,"Your profile has unsaved changes. Do you want to save?"),MessageBox.DEFOKB|MessageBox.NOB)).execute()==MessageBox.IDOK) {
				saveIndex=true; 
			}
		}
		if (saveIndex) profile.saveIndex(Global.getPref(),false);
	}
	
}
// 

