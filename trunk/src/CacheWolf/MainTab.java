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
	String lastselected = new String();
	CacheHolder ch=null,chNew=null;
	CacheHolderDetail chD =null, chMain=null;
	MainMenu mnuMain;
	StatusBar statBar;
	MovingMap mm;
	Navigate nav;
	boolean cacheChanged;

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
		Card c = this.addCard(tbP = new TablePanel(pref, profile, statBar), MyLocale.getMsg(1200,"List"), null);

		c = this.addCard(detP, MyLocale.getMsg(1201,"Details"), null);
		c.iconize(new Image("details.gif"),true);

		c = this.addCard(descP, MyLocale.getMsg(1202,"Description"), null);
		c.iconize(new Image("descr.gif"),true);

		c = this.addCard(new ScrollBarPanel(imageP = new ImagePanel()), MyLocale.getMsg(1203,"Images"), null);
		c.iconize(new Image("images.gif"),true);

		c = this.addCard(hintLP, MyLocale.getMsg(1204,"Hints & Logs"), null);
		c.iconize(new Image("more.gif"),true);

		c = this.addCard(calcP, MyLocale.getMsg(1206,"Calc"), null);
		c.iconize(new Image("ewe/HandHeld.bmp"),true);

		nav = new Navigate();
		c = this.addCard(gotoP = new GotoPanel(nav), "Goto", null);
		c.iconize(new Image("goto.gif"),true);
		nav.setGotoPanel(gotoP);

		c = this.addCard(solverP = new SolverPanel(pref, profile), MyLocale.getMsg(1205,"Solver"), null);
		c.iconize(new Image("solver.gif"),true);

		c = this.addCard(radarP, "Radar", null);
		radarP.setMainTab(this);
		c.iconize(new Image("radar.gif"),true);
		mnuMain.allowProfileChange(true);
	}

	public TablePanel getTablePanel(){
		return tbP;
	}

	public void selectAndActive(int rownum){// Called from myInteractivePanel.imageClicked
		tbP.selectRow(rownum);
		this.selectAndExpand(0);
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
	 * this is called from goto / MovingMap / CalcPanel and so on to 
	 * offer the user the possibility of entering an new waypoint
	 * at a given position. ch must already been preset with a valid
	 * CacheHolder object
	 * 
	 * @param ch
	 */
	public void newWaypoint(CacheHolder ch){
		if (detP.isDirty()) detP.saveDirtyWaypoint();
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


	public void onEvent(Event ev)
	{
		if(ev instanceof MultiPanelEvent){ 
			// A panel is selected.
			mnuMain.allowProfileChange(false);	  
			if(this.getSelectedItem() == 0){// List view selected
				mnuMain.allowProfileChange(true);	  
//				Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,0);
//				Vm.setSIP(0);
				MyLocale.setSIPOff();
			}
			// Get current cacheHolder
			if (tbP.getSelectedCache()>=cacheDB.size() || tbP.getSelectedCache()<0) {
				chNew=null; chD=null;
			}
			else
				chNew = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
			// Is it the same as the last one?
			cacheChanged=chNew!=ch;
			if (cacheChanged) { // new object not same reference as old
				updatePendingChanges(); // Save dirty data
	            ch=chNew;		
	            chD=null;
			}
			// Only load the details if we leave the list view and the details
			// have not already been loaded
			if(this.getSelectedItem() != 0 && chD==null){// any panel other than list view without detail
				try {
					chD=new CacheHolderDetail(ch);
					chD.readCache(profile.dataDir);//Vm.debug("MainTab:readCache "+chD.wayPoint+"/S:"+chD.Solver);
					//lastselected = ch.wayPoint;
				} catch(Exception e){
					//Vm.debug("Error loading: "+ch.wayPoint);
				}
			}
			// We are in list view, update the status display
			else statBar.updateDisplay();
			switch (this.getSelectedItem()) {// Switch by panel number
			case 0:
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
			case 5:  // CalcPanel
				if (chD!=null) {
					MyLocale.setSIPButton();
					calcP.setFields(chD);
				}
				break;

			case 6: // GotoPanel
				MyLocale.setSIPButton();
				break;
			case 7:  // Solver Panel
				MyLocale.setSIPButton();
				if (chD!=null) {
					if (chD.isAddiWpt()) { 
						chMain=new CacheHolderDetail(chD.mainCache);
						try {
							chMain.readCache(profile.dataDir);//Vm.debug("mainT:readCache "+chD.wayPoint+"=>Main=>"+chMain.wayPoint+"/S:"+chMain.Solver);
						} catch(Exception e){pref.log("Error reading cache",e);}
						solverP.setInstructions(chMain.Solver);
					} else {
						//Vm.debug("mainT: Waypoint:"+chD.wayPoint);
						solverP.setInstructions(chD.Solver);
					}
				}
				break;
			case 8:  // Cache Radar Panel
				MyLocale.setSIPOff();
				radarP.setParam(pref, cacheDB, chD==null?"":chD.wayPoint);
				radarP.drawThePanel();
				break;
			}
		}
		super.onEvent(ev); //Make sure you call this.
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
					if (i >= 10*60) {(new MessageBox("Error", "MovingMap cannot be displaed - this is most likely a bug - plaese report it on www.geoclub.de", MessageBox.OKB)).execute(); return;}
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
			Global.getPref().log("Error starting mavoing map (2): " + e.getMessage(), e, true);
			(new MessageBox("Error", "Error starting mavoing map: " + e.getMessage(), MessageBox.OKB)).execute(); }
	}

	/** Save any changes from DetailsPanel before operating on the database */
	public void updatePendingChanges() {
		if(detP.isDirty()) {
			detP.saveDirtyWaypoint();
		}
		if (chD!=null && solverP.isDirty()) {
			if (chMain==null) {
				chD.Solver=solverP.getInstructions();
				chD.saveCacheDetails(Global.getProfile().dataDir);//Vm.debug("mainT:SaveCache "+chD.wayPoint+"/S:"+chD.Solver);
				solverP.setInstructions("");
			} else {
				chMain.Solver=solverP.getInstructions();
				chMain.saveCacheDetails(Global.getProfile().dataDir);//Vm.debug("mainT:SaveCache "+chMain.wayPoint+"/S:"+chMain.Solver);
				solverP.setInstructions("");
				chMain=null;
			}
		}
	}

	/** Save the index file and any pending change in DetailsPanel
	 * 
	 * @param askForConfirmation If true, the save can be cancelled by user
	 */
	public void saveUnsavedChanges(boolean askForConfirmation) {
		boolean saveIndex=!askForConfirmation; // Definitely save it if no confirmation needed
		updatePendingChanges(); // Updated the cacheDB with pending changes from DetailsPanel
		if (askForConfirmation) { // Don't know whether to save, have to ask
			if (profile.hasUnsavedChanges &&     // Only ask if there were changes 
					(new MessageBox(MyLocale.getMsg(144,"Warnung"),MyLocale.getMsg(1207,"Your profile has unsaved changes. Do you want to save?"),MessageBox.DEFOKB|MessageBox.NOB)).execute()==MessageBox.IDOK) {
				saveIndex=true; 
			}
		}
		if (saveIndex) profile.saveIndex(Global.getPref(),false);
	}
}


