/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf;

import CacheWolf.navi.GotoPanel;
import CacheWolf.navi.MapImage;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import ewe.fx.Color;
import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.ui.Card;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.MultiPanelEvent;
import ewe.ui.TableEvent;
import ewe.ui.mTabbedPanel;

/**
 * This class creates the tabbed panel and sets the tabs to the respective other panels. Important is to have a look at
 * the event handler!<br>
 * Class ID = 1200
 * 
 * @see MainForm
 * @see MainMenu
 */
public class MainTab extends mTabbedPanel {
	static int LIST_CARD;
	static int DETAILS_CARD;
	static int DESCRIPTION_CARD;
	static int IMAGES_CARD;
	static int HINTSANSLOGS_CARD;
	static int SOLVER_CARD;
	static int CALC_CARD;
	static int GOTO_CARD;
	static int RADAR_CARD;
	static int MAP_CARD;

	TableForm listP;
	public DetailsPanel detP = new DetailsPanel();
	DescriptionPanel descP = new DescriptionPanel();
	HintLogPanel hintLP = new HintLogPanel();
	public TablePanel tbP;
	CalcPanel calcP;
	GotoPanel gotoP;
	RadarPanel radarP = new RadarPanel();
	ImagePanel imageP;
	SolverPanel solverP;
	ewe.ui.CellPanel mapP;

	Preferences pref;
	Profile profile;

	CacheDB cacheDB;
	String lastselected = "";
	public CacheHolder ch = null, chMain = null;
	CacheHolderDetail chD = null;
	MainMenu mnuMain;

	public StatusBar statBar;
	public MovingMap mm;
	public Navigate nav;
	public String mainCache = "";
	int oldCard;
	boolean cacheDirty = false;

	public MainTab(MainMenu mainMenu, StatusBar statBar) {
		// Init here so that Global.MainT is already set
		Global.mainTab = this;
		mnuMain = mainMenu;
		pref = Global.getPref();
		profile = Global.getProfile();
		if (!pref.tabsAtTop)
			tabLocation = SOUTH;
		cacheDB = profile.cacheDB;
		this.statBar = statBar;
		MyLocale.setSIPButton();
		// Don't expand tabs if the screen is very narrow, i.e. HP IPAQ
		// 65xx, 69xx
		int sw = MyLocale.getScreenWidth();
		if (sw <= 240)
			this.dontExpandTabs = true;
		String imagesize = "";
		if (pref.useBigIcons)
			imagesize = "_vga";

		tbP = new TablePanel(pref, profile, statBar);
		Card c = this.addCard(listP = new TableForm(tbP), MyLocale.getMsg(1200, "List"), null);
		LIST_CARD = this.cardPanel.cards.size() - 1;
		oldCard = LIST_CARD;

		c = this.addCard(detP, MyLocale.getMsg(1201, "Details"), null);
		DETAILS_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("details" + imagesize + ".gif"), true);

		c = this.addCard(descP, MyLocale.getMsg(1202, "Description"), null);
		DESCRIPTION_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("descr" + imagesize + ".gif"), true);

		c = this.addCard(new MyScrollBarPanel(imageP = new ImagePanel()), MyLocale.getMsg(1203, "Images"), null);
		IMAGES_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("images" + imagesize + ".gif"), true);

		c = this.addCard(hintLP, MyLocale.getMsg(1204, "Hints & Logs"), null);
		HINTSANSLOGS_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("more" + imagesize + ".gif"), true);

		c = this.addCard(solverP = new SolverPanel(pref, profile), MyLocale.getMsg(1205, "Solver"), null);
		SOLVER_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("solver" + imagesize + ".gif"), true);

		c = this.addCard(calcP = new CalcPanel(), MyLocale.getMsg(1206, "Calc"), null);
		CALC_CARD = this.cardPanel.cards.size() - 1;
		mImage imgCalc = new mImage("projecttab" + imagesize + ".gif");
		imgCalc.transparentColor = new Color(0, 255, 0);
		c.iconize(imgCalc, true);

		nav = new Navigate();
		c = this.addCard(gotoP = new GotoPanel(nav), MyLocale.getMsg(345, "Goto these coordinates"), null);
		GOTO_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("goto" + imagesize + ".gif"), true);
		nav.setGotoPanel(gotoP);

		c = this.addCard(mapP = new ewe.ui.CellPanel(), MyLocale.getMsg(347, "Show map"), null);
		MAP_CARD = this.cardPanel.cards.size() - 1;
		c.iconize(new Image("globe_small" + imagesize + ".gif"), true);

		if (pref.useBigIcons) {
			c = this.addCard(radarP, "Radar", null);
			RADAR_CARD = this.cardPanel.cards.size() - 1;
			radarP.setMainTab(this);
			c.iconize(new Image("radar" + imagesize + ".gif"), true);
		}

		mnuMain.allowProfileChange(true);
		// if (pref.noTabs) top.modify(ShrinkToNothing,0);//TODO
	}

	public TablePanel getTablePanel() {
		return tbP;
	}

	public void selectAndActive(int rownum) {// Called from
		// myInteractivePanel.imageClicked
		tbP.selectRow(rownum);
		this.selectAndExpand(0);
	}

	public void clearDetails() {
		imageP.clearImages(); // Remove all images
		descP.clear(); // write "loading ..."
		detP.clear(); // Clear only the attributes
		hintLP.clear(); // Remove the logs
		solverP.setInstructions("loading ...");
	}

	public void onEvent(Event ev) {
		// This section clears old data when a new line is selected in the
		// table
		if (ev instanceof TableEvent) {
			clearDetails();
		}
		if (ev instanceof MultiPanelEvent) {
			// Check whether a profile change is allowed, if not disable the
			// relevant options
			checkProfileChange();
			// Perform clean up actions for the panel we are leaving
			onLeavingPanel(oldCard);
			// Prepare actions for the panel we are about to enter
			onEnteringPanel(((MultiPanelEvent) ev).selectedIndex);
		}
		super.onEvent(ev); // Make sure you call this.
	}

	/**
	 * Code to execute when leaving a panel (oldCard is the panel number)
	 * 
	 */
	private void onLeavingPanel(int panelNo) {
		if (panelNo == MainTab.LIST_CARD) { // Leaving the list view
			// Get the cache for the current line (ch)
			// Get the details for the current line (chD)
			// If it is Addi get details of main Wpt (chMain)
			chMain = null;
			cacheDirty = false;
			if (tbP.getSelectedCache() >= Global.mainTab.tbP.myMod.numRows || tbP.getSelectedCache() < 0) {
				ch = null;
				chD = null;
				lastselected = "";
			} else {
				ch = cacheDB.get(tbP.getSelectedCache());
				lastselected = ch.getWayPoint(); // Used in
				// Parser.Skeleton
				chD = ch.getCacheDetails(true);
			}
		}
		if (panelNo == MainTab.DETAILS_CARD) { // Leaving the Details Panel
			detP.evWaypointChanged = false;
			// Update chD with Details
			if (detP.isDirty()) {
				cacheDirty = true;
				boolean needTableUpdate = detP.getNeedsTableUpdate();
				detP.saveDirtyWaypoint();
				if (needTableUpdate) {
					tbP.myMod.updateRows();// This sorts the waypoint (if
					// it is new) into the right
					// position
					tbP.selectRow(profile.getCacheIndex(detP.cache.getWayPoint()));
				}
				// was tbP.refreshTable();
				tbP.tc.update(true); // Update and repaint
				if (statBar != null)
					statBar.updateDisplay("");
			}
		}
		if (panelNo == MainTab.SOLVER_CARD) { // Leaving the Solver Panel
			// Update chD or chMain with Solver
			// If chMain is set (i.e. if it is an addi Wpt) save it
			// immediately
			if (chD != null && solverP.isDirty()) {
				if (chMain == null) {
					cacheDirty = true;
					boolean oldHasSolver = chD.getParent().hasSolver();
					chD.setSolver(solverP.getInstructions());
					if (oldHasSolver != chD.getParent().hasSolver())
						tbP.tc.update(true);
					// For safety reasons: Immediately save solver
					// instructions when
					// switching panels
					updatePendingChanges();
				} else {
					boolean oldHasSolver = chMain.hasSolver();
					chMain.getCacheDetails(true).setSolver(solverP.getInstructions());
					if (oldHasSolver != chMain.hasSolver())
						tbP.tc.update(true);
					chMain.save();
					chMain = null;
				}
			}
		}
	}

	/**
	 * Code to execute when entering a panel (getSelectedItem() is the panel number)
	 * 
	 */
	private void onEnteringPanel(int panelNo) {
		MyLocale.setSIPOff();
		if (panelNo == LIST_CARD) {
			// If Solver or Details has changed, save Cache
			updatePendingChanges();
			if (detP.hasBlackStatusChanged()) {
				tbP.refreshTable();
			}
			updateCurCentrePtFromGPS();
		} else if (panelNo == DETAILS_CARD) {
			boolean newCache = false;
			if (chD == null) { // Empty DB - show a dummy detail
				newWaypoint(ch = new CacheHolder());
				newCache = true;
			}
			detP.setDetails(ch, newCache);
		} else if (panelNo == DESCRIPTION_CARD) {
			descP.setText(ch);
		} else if (panelNo == IMAGES_CARD) {
			if (ch.isAddiWpt()) {
				imageP.setImages(ch.mainCache.getCacheDetails(true));
			} else {
				imageP.setImages(chD);
			}
		} else if (panelNo == HINTSANSLOGS_CARD) {
			if (ch.isAddiWpt()) {
				hintLP.setText(ch.mainCache.getCacheDetails(true));
			} else {
				hintLP.setText(chD);
			}
		} else if (panelNo == SOLVER_CARD) {
			if (ch.isAddiWpt()) {
				chMain = ch.mainCache;
				solverP.setInstructions(ch.mainCache);
			} else {
				solverP.setInstructions(ch);
			}
		} else if (panelNo == CALC_CARD) {
			if (ch != null)
				calcP.setFields(ch);
		} else if (panelNo == MAP_CARD) {
			gotoP.switchToMovingMap();
			if (oldCard == LIST_CARD) {
				select(listP);
			} else if (oldCard == DETAILS_CARD) {
				select(detP);
			} else if (oldCard == DESCRIPTION_CARD) {
				select(descP);
			} else if (oldCard == IMAGES_CARD) {
				select(imageP);
			} else if (oldCard == HINTSANSLOGS_CARD) {
				select(hintLP);
			} else if (oldCard == SOLVER_CARD) {
				select(solverP);
			} else if (oldCard == CALC_CARD) {
				select(calcP);
			} else if (oldCard == GOTO_CARD) {
				select(gotoP);
			} else if (oldCard == RADAR_CARD) {
				select(radarP);
			}
		} else if (panelNo == RADAR_CARD) {
			radarP.setParam(pref, cacheDB, ch);
			radarP.drawThePanel();
			updateCurCentrePtFromGPS();
		}
		oldCard = panelNo;
	}

	/**
	 * this is called from MovingMap Cache context menu
	 * 
	 * @param chi ,
	 *            the CacheHolder for the Cache to switch to
	 * @param panelNo
	 *            1=DetailsPanel 2=Description Panel
	 */
	public void openPanel(CacheHolder chi, int panelNo) {
		// To change cache we need to be in panel LIST_CARD
		onLeavingPanel(oldCard);
		onEnteringPanel(LIST_CARD);
		int row = profile.getCacheIndex(chi.getWayPoint());
		tbP.selectRow(row);
		if (panelNo == DETAILS_CARD) {
			ch = chi; // possibly wrong (old) detail if map is called
			// directly
			onEnteringPanel(DETAILS_CARD);
			select(detP);
		} else if (panelNo == DESCRIPTION_CARD) {
			onEnteringPanel(DESCRIPTION_CARD);
			select(descP);
		}

	}

	/**
	 * this is called from goto / MovingMap / CalcPanel / DetailsPanel and so on to offer the user the possibility of
	 * entering an new waypoint at a given position. pCh must already been preset with a valid CacheHolder object
	 * 
	 * @param pCh
	 */
	public void newWaypoint(CacheHolder pCh) {
		// When creating a new waypoint, simulate a change to the list view
		// if we are currently NOT in the list view
		if (oldCard != LIST_CARD) {
			onLeavingPanel(oldCard);
		}
		updatePendingChanges(); // was: onEnteringPanel(0); oldCard=0;

		mainCache = lastselected;
		int selectedIndex = profile.getCacheIndex(lastselected);
		if (selectedIndex >= 0) {
			CacheHolder selectedCache = profile.cacheDB.get(selectedIndex);
			if (selectedCache.isAddiWpt()) {
				if (selectedCache.mainCache != null) {
					mainCache = selectedCache.mainCache.getWayPoint();
				} else {
					mainCache = null;
				}
			}
		}
		if (CacheType.isAddiWpt(pCh.getType()) && mainCache != null && mainCache.length() > 2) {
			pCh.setWayPoint(profile.getNewAddiWayPointName(mainCache));
			profile.setAddiRef(pCh);
		} else {
			pCh.setWayPoint(profile.getNewWayPointName());
			lastselected = pCh.getWayPoint();
		}
		pCh.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
		chD = pCh.getCacheDetails(false);
		this.ch = pCh;
		cacheDB.add(pCh);
		Global.getProfile().notifyUnsavedChanges(true); // Just to be sure
		tbP.myMod.numRows++;
		detP.setDetails(pCh, true);
		oldCard = DETAILS_CARD;
		if (this.cardPanel.selectedItem != 1)
			select(detP);
		solverP.setInstructions(pCh);
		// tbP.refreshTable(); // moved this instruction to onLeavingPanel

	}

	/**
	 * sets posCircle Lat/Lon to centerTo
	 * 
	 * @param centerTo
	 *            true: centers centerTo on the screen and disconnects MovingMap from GPS if Gps-pos is not on the
	 *            loaded map
	 * @param forceCenter
	 */
	public void SwitchToMovingMap(CWPoint centerTo, boolean forceCenter) {
		try {
			if (!centerTo.isValid()) {
				(new MessageBox("Error", "No valid coordinates", FormBase.OKB)).execute();
				return;
			}
			if (mm == null) {
				mm = new MovingMap(nav, profile.cacheDB);
				nav.setMovingMap(mm);
			}

			mm.myExec(centerTo, forceCenter);

			if (forceCenter) {
				try {
					int i = 0;
					while (MapImage.screenDim.width == 0 && i < 10 * 60) {
						i++;
						ewe.sys.mThread.sleep(100);
					} // wait until the window size of the moving map is
					// known note: ewe.sys.sleep() will pause the whole
					// vm - no other thread will run
					if (i >= 10 * 60) {
						(new MessageBox("Error", "MovingMap cannot be displayed - this is most likely a bug - plaese report it on www.geoclub.de", FormBase.OKB)).execute();
						return;
					}
					// If white Areas are filled there is a problem with
					// painting the image. We force painting here.
					mm.repaint();
				} catch (InterruptedException e) {
					Global.getPref().log("Error starting mavoing map (1): ", e, true);
					(new MessageBox("Error", "This must not happen please report to pfeffer how to produce this error message", FormBase.OKB)).execute();
				}
			}
		} catch (Exception e) { // TODO swith waiting indication clock off
			Global.getPref().log("Error starting moving map (2): ", e, true);
			(new MessageBox("Error", "Error starting moving map: " + e.getMessage(), FormBase.OKB)).execute();
		}
	}

	void updatePendingChanges() {
		if (cacheDirty) {
			if (chD != null)
				chD.getParent().save();
			cacheDirty = false;
		}
	}

	/**
	 * Save the index file
	 * 
	 * @param askForConfirmation
	 *            is ignored, old: If true, the save can be cancelled by user
	 */
	public void saveUnsavedChanges(boolean askForConfirmation) {
		if (oldCard != LIST_CARD) {
			onLeavingPanel(oldCard);
			onEnteringPanel(LIST_CARD);
			oldCard = LIST_CARD;
		}
		updatePendingChanges();
		if (profile.hasUnsavedChanges())
			profile.saveIndex(Global.getPref(), true);
		this.tbP.saveColWidth(pref);
		Global.getPref().savePreferences();
	}

	private void checkProfileChange() {
		// A panel is selected. Could be the same panel twice
		mnuMain.allowProfileChange(false);
		if (this.getSelectedItem() == 0) {// List view selected
			mnuMain.allowProfileChange(true);
			MyLocale.setSIPOff();
		}
	}

	private void updateCurCentrePtFromGPS() {
		if (pref.setCurrentCentreFromGPSPosition) {
			if (nav.gpsRunning) {
				CWPoint whereAmI = nav.gpsPos;
				if (whereAmI.isValid()) {
					CWPoint curCentr = pref.getCurCentrePt();
					if (whereAmI.latDec != curCentr.latDec || whereAmI.lonDec != curCentr.lonDec) {
						pref.setCurCentrePt(whereAmI);
					}
				}
			}
		}
	}
}
//

