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
	CacheHolder ch =null;
	MainMenu mnuMain;
	StatusBar statBar;
	MovingMap mm;
	Navigate nav;

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

	public void selectAndActive(int rownum){
		tbP.selectAndActive(rownum);
		this.selectAndExpand(0);
	}

	/** Update the distances of all caches to the center and display a message 
	 */
	public void updateBearDist(){
		tbP.pref = pref;
		profile.updateBearingDistance();
		tbP.refreshTable();
		(new MessageBox(MyLocale.getMsg(327,"Information"), MyLocale.getMsg(1024,"Entfernungen in der Listenansicht \nvom aktuellen Standpunkt aus \nneu berechnet").replace('~','\n'), MessageBox.OKB)).execute();
	}

	public void gotoPoint(String LatLon) {
		gotoP.setDestinationAndSwitch(LatLon);
	}

	public void openDesciptionPanel(CacheHolder chi) {
		MyLocale.setSIPOff();
		descP.setText(chi);
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
		Global.mainTab.tbP.myMod.updateRows();
		Global.mainTab.tbP.selectRow(profile.getCacheIndex(waypoint));
		//Global.mainTab.tbP.refreshTable();
		if (this.cardPanel.selectedItem==1) { // Detailpanel already selected
			postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,detP,0));
		} else	
			select(detP);
		Global.mainTab.tbP.refreshTable();

	}


	public void onEvent(Event ev)
	{
		////Vm.debug(ev.toString());
		if(ev instanceof MultiPanelEvent){
			mnuMain.allowProfileChange(false);	  
			if(this.getSelectedItem() == 0){
				mnuMain.allowProfileChange(true);	  
//				Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,0);
//				Vm.setSIP(0);
				MyLocale.setSIPOff();
			}
			updatePendingChanges();
			if(this.getSelectedItem() != 0){
				if (tbP.getSelectedCache()>=cacheDB.size() || tbP.getSelectedCache()<0)
					ch=null;
				else {
					ch = (CacheHolder)cacheDB.get(tbP.getSelectedCache());
					try {
						if(ch.wayPoint.equals(lastselected) == false){
							ch.readCache(profile.dataDir);
							lastselected = ch.wayPoint;
						}
					} catch(Exception e){
						//Vm.debug("Error loading: "+ch.wayPoint);
					}
				}
			} else statBar.updateDisplay();

			// If no cache is selected, create a new one
			switch (this.getSelectedItem()) {
			case 1:  // DetailsPanel
				if (ch==null) newWaypoint(ch=new CacheHolder());
				MyLocale.setSIPButton();
				detP.setDetails(ch);
				break;
			case 2: // Description Panel
				if (ch!=null) {
					MyLocale.setSIPOff();
					descP.setText(ch);
				}
				break;
			case 3: // Picture Panel
				if (ch!=null) {
					MyLocale.setSIPOff();
					imageP.setImages(ch);
				}
				break;
			case 4:  // Log Hint Panel
				if (ch!=null) {
					MyLocale.setSIPOff();
					hintLP.setText(ch);
				}
				break;
			case 5:  // CalcPanel
				if (ch!=null) {
					MyLocale.setSIPButton();
					calcP.setFields(ch);
				}
				break;

			case 6: // GotoPanel
				MyLocale.setSIPButton();
				break;
			case 7:  // Solver Panel
				MyLocale.setSIPButton();
				solverP.setCh(ch);
				break;
			case 8:  // Cache Radar Panel
				MyLocale.setSIPOff();
				radarP.setParam(pref, cacheDB, ch==null?"":ch.wayPoint);
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
		if (!centerTo.isValid()) {
			(new MessageBox("Error", "No valid coordinates", MessageBox.OKB)).execute();
			return;
		}
		if (mm == null) {
			mm = new MovingMap(nav, profile.cacheDB);
			nav.setMovingMap(mm);
			if (nav.gpsRunning) mm.gpsStarted();
		} 
		if (forceCenter) mm.setGpsStatus(MovingMap.noGPS); // disconnect movingMap from GPS TODO only if GPS-pos is not on the screen
		mm.updatePosition(centerTo.latDec, centerTo.lonDec);
		mm.myExec();
		if (forceCenter) {
			try {
				while (MapImage.screenDim.width == 0) { ewe.sys.mThread.sleep(100);} // wait until the window size of the moving map is known note: ewe.sys.sleep() will pause the whole vm - no other thread will run 
				mm.setCenterOfScreen(centerTo, false); // this can only be executed if mm knows its window size that's why myExec must be executed before
				mm.updatePosition(centerTo.latDec, centerTo.lonDec);
				/*			if(!mm.posCircle.isOnScreen()) { // TODO this doesn't work because lat lon is set to the wished pos and not to gps anymore
				mm.setGpsStatus(MovingMap.noGPS); // disconnect movingMap from GPS if GPS-pos is not on the screen
				mm.setResModus(MovingMap.HIGHEST_RESOLUTION);
				mm.updatePosition(centerTo.latDec, centerTo.lonDec);
				mm.setCenterOfScreen(centerTo, true); 
			}
				 */			//TODO what to do, if there is a map at centerTo, but it is not loaded because of mapSwitchMode == dest & cuurpos und dafür gibt es keine Karte 
			}catch (InterruptedException e) { (new MessageBox("Error", "This must not happen please report to pfeffer how to produce this error message", MessageBox.OKB)).execute(); } 
		}
	}

	/** Save any changes from DetailsPanel before operating on the database */
	public void updatePendingChanges() {
		if(detP.isDirty()) {
			detP.saveDirtyWaypoint();
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


