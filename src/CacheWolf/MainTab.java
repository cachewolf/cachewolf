/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
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

import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import CacheWolf.navi.CWPoint;
import CacheWolf.navi.GotoPanel;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import ewe.fx.Graphics;
import ewe.fx.IconAndText;
import ewe.sys.Vm;
import ewe.ui.Card;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.MultiPanelEvent;
import ewe.ui.TableEvent;
import ewe.ui.mButton;
import ewe.ui.mTabbedPanel;

/**
 * This class creates the tabbed panel and sets the tabs to the respective other panels. Important is to have a look at the event handler!<br>
 * Class ID = 1200
 * 
 * @see MainForm
 * @see MainMenu
 */
public class MainTab extends mTabbedPanel {

    // following numbers depend on tabNames order (MENU_CARD must be the last tabName)
    static int LIST_CARD = 0;
    static int DETAILS_CARD = 1;
    static int DESCRIPTION_CARD = 2;
    static int IMAGES_CARD = 3;
    static int HINTSANDLOGS_CARD = 4;
    static int SOLVER_CARD = 5;
    static int CALC_CARD = 6;
    static int GOTO_CARD = 7;
    static int RADAR_CARD = 8;
    static int MAP_CARD = 9;
    static int MENU_CARD = 10;

    public TablePanel tablePanel;
    public DetailsPanel detailsPanel;
    DescriptionPanel descriptionPanel;
    HintLogPanel hintLogPanel;
    CalcPanel calcPanel;
    public GotoPanel gotoPanel;
    ImagePanel imagePanel;
    SolverPanel solverPanel;
    CellPanel mapPanel;
    RadarPanel radarPanel;

    CellPanel homePanel;
    CellPanel selectPanels[];

    CacheDB cacheDB;

    String lastselected = "";
    public CacheHolder ch = null, chMain = null;
    CacheHolderDetail chD = null;

    public MovingMap movingMap;
    public Navigate navigate;

    public String mainCache = "";

    int oldCard;
    boolean cacheDirty = false;

    Control[] baseControls;

    public MainTab() {
	Global.mainTab = this;
	if (!Global.pref.tabsAtTop)
	    tabLocation = SOUTH;

	cacheDB = Global.profile.cacheDB;

	tablePanel = new TablePanel();
	detailsPanel = new DetailsPanel();
	descriptionPanel = new DescriptionPanel();
	imagePanel = new ImagePanel();
	hintLogPanel = new HintLogPanel();
	solverPanel = new SolverPanel();
	calcPanel = new CalcPanel();
	gotoPanel = new GotoPanel();
	mapPanel = new CellPanel();
	radarPanel = new RadarPanel();

	//this.dontAutoScroll = true;

	MyLocale.setSIPButton();

	// Don't expand tabs if the screen is very narrow, i.e. HP IPAQ
	// 65xx, 69xx
	// int sw = MyLocale.getScreenWidth();
	// if (sw <= 480)
	//	this.dontExpandTabs = true;
	// if true no Tab outside is shown (big icons)

	baseControls = new Control[] { tablePanel, //
		detailsPanel, //
		descriptionPanel, //
		imagePanel, //
		hintLogPanel, //
		solverPanel, //
		calcPanel, //
		gotoPanel, //
		radarPanel, //
		mapPanel, //
	};

	String[] tabNames;
	tabNames = new String[] { MyLocale.getMsg(1200, "List"), //
		MyLocale.getMsg(1201, "Details"), //
		MyLocale.getMsg(1202, "Description"), //
		MyLocale.getMsg(1203, "Images"), //
		MyLocale.getMsg(1204, "Hints & Logs"), //
		MyLocale.getMsg(1205, "Solver"), //
		MyLocale.getMsg(1206, "Calc"), //
		MyLocale.getMsg(1010, "Kompass"), //
		MyLocale.getMsg(205, "Radar"), //
		MyLocale.getMsg(347, "Show map"), //
	};

	String[] imageNames = { "list", //
		"details", //
		"description", //
		"images", //
		"hint", //
		"solver", //
		"calc", //
		"compass", //
		"radar", //
		"globe", //
	};

	for (int i = 0; i < tabNames.length; i++) {
	    CellPanel alles = new CellPanel();
	    CellPanel selection = new CellPanel();
	    if (!Global.pref.tabsAtTop)
		alles.addLast(baseControls[i]);
	    selection.equalWidths = true;
	    if (Global.pref.noTabs) {
		// navigation with buttons instead of tabs
		mButton btn = GuiImageBroker.getButton(MyLocale.getMsg(1211, "Home"), "home");
		btn.name = "select";
		btn.setTag(999, "" + tabNames.length);
		selection.addNext(btn);
		if (i > 0) {
		    btn = GuiImageBroker.getButton(tabNames[i - 1], imageNames[i - 1]);
		    btn.name = "select";
		    btn.setTag(999, "" + (i - 1));
		    selection.addNext(btn);
		}
		if (i < tabNames.length - 1) {
		    btn = GuiImageBroker.getButton(tabNames[i + 1], imageNames[i + 1]);
		    btn.name = "select";
		    btn.setTag(999, "" + (i + 1));
		    selection.addLast(btn);
		}
	    }
	    alles.addLast(selection, HSTRETCH, HFILL);
	    if (Global.pref.tabsAtTop)
		alles.addLast(baseControls[i]);
	    Card c = this.addCard(alles, tabNames[i], null);
	    c.iconize(GuiImageBroker.getImage(imageNames[i]), Global.pref.useIcons);
	    if (Global.pref.noTabs) {
		((IconAndText) c.image).textPosition = Graphics.Down;
	    }
	}

	movingMap = new MovingMap();
	navigate = new Navigate(); // attention gotoP and mm must be created before;
	gotoPanel.init(); // do the rest from ctor creation;

	/*
	if (Global.pref.isBigScreen || !Global.pref.useRadar) {
		// use map
	}

	if (Global.pref.isBigScreen || Global.pref.useRadar) {
		// use radar
	}
	*/

	if (Global.pref.noTabs) {
	    this.addCard(homePanel = new CellPanel(), MyLocale.getMsg(1211, "Home"), null);
	    initHomePanel();
	    this.select(MyLocale.getMsg(1211, "Home"));
	    top.modify(ShrinkToNothing, 0);
	}

	oldCard = LIST_CARD;
    }

    private void initHomePanel() {
	selectPanels = new CellPanel[this.cardPanel.cards.size()];
	homePanel.equalWidths = true;
	for (int i = 0; i < this.cardPanel.cards.size() - 1; i++) {
	    selectPanels[i] = new CellPanel();
	    mButton btn = new mButton();
	    btn.name = "select";
	    btn.setTag(999, "" + i);
	    btn.image = ((Card) this.cardPanel.cards.get(i)).image;
	    selectPanels[i].addLast(btn);
	    if (i % 2 == 1) {
		homePanel.addLast(selectPanels[i]);
	    } else {
		homePanel.addNext(selectPanels[i]);
	    }
	}
    }

    /* replaced by select(index of Card) where used 
    // Overrrides
    // direct select by baseControl no longer works see ctor: Card c = this.addCard(alles, tabNames[i], null);
    public void select(Control c) {
    	for (int i = 0; i < baseControls.length; i++) {
    		if (baseControls[i] == c) {
    			select(i);
    			break;
    		}
    	}
    }
    */

    public TablePanel getTablePanel() {
	return this.tablePanel;
    }

    public void selectAndActive(int rownum) {
	// Called from myInteractivePanel.imageClicked
	this.tablePanel.selectRow(rownum);
	this.selectAndExpand(0);
    }

    public void clearDetails() {
	imagePanel.clearImages(); // Remove all images
	descriptionPanel.clear(); // write "loading ..."
	detailsPanel.clear(); // Clear only the attributes
	hintLogPanel.clear(); // Remove the logs
	solverPanel.setInstructions("loading ...");
    }

    private void checkProfileChange() {
	MainMenu.itself.allowProfileChange(false);
	if (this.getSelectedItem() == LIST_CARD) {// List view selected
	    MainMenu.itself.allowProfileChange(true);
	    MyLocale.setSIPOff();
	}
    }

    /**
     * Code to execute when leaving a panel (oldCard is the panel number)
     * 
     */
    private void onLeavingPanel(int panelNo) {
	if (panelNo == MainTab.LIST_CARD) {
	    // Leaving the list view
	    // Get the cache for the current line (ch)
	    // Get the details for the current line (chD)
	    // If it is Addi get details of main Wpt (chMain)
	    chMain = null;
	    cacheDirty = false;
	    if (this.tablePanel.getSelectedCache() >= this.tablePanel.myTableModel.numRows || this.tablePanel.getSelectedCache() < 0) {
		ch = null;
		chD = null;
		lastselected = "";
	    } else {
		ch = cacheDB.get(this.tablePanel.getSelectedCache());
		lastselected = ch.getWayPoint(); // Used in
		// Parser.Skeleton
		chD = ch.getCacheDetails(true);
	    }
	}
	if (panelNo == MainTab.DETAILS_CARD) {
	    detailsPanel.evWaypointChanged = false;
	    // Update chD with Details
	    if (detailsPanel.isDirty()) {
		cacheDirty = true;
		boolean needTableUpdate = detailsPanel.getNeedsTableUpdate();
		detailsPanel.saveDirtyWaypoint();
		if (needTableUpdate) {
		    // This sorts the waypoint (if it is new) into the right position
		    this.tablePanel.myTableModel.updateRows();
		    this.tablePanel.selectRow(Global.profile.getCacheIndex(detailsPanel.cache.getWayPoint()));
		}
		// was this.tablePanel.refreshTable();
		this.tablePanel.myTableControl.update(true); // Update and repaint
		this.tablePanel.updateStatusBar();
	    }
	}
	if (panelNo == MainTab.SOLVER_CARD) { // Leaving the Solver Panel
	    // Update chD or chMain with Solver
	    // If chMain is set (i.e. if it is an addi Wpt) save it
	    // immediately
	    if (chD != null && solverPanel.isDirty()) {
		if (chMain == null) {
		    cacheDirty = true;
		    boolean oldHasSolver = chD.getParent().hasSolver();
		    chD.setSolver(solverPanel.getInstructions());
		    if (oldHasSolver != chD.getParent().hasSolver())
			this.tablePanel.myTableControl.update(true);
		    // For safety reasons: Immediately save solver
		    // instructions when
		    // switching panels
		    updatePendingChanges();
		} else {
		    boolean oldHasSolver = chMain.hasSolver();
		    chMain.getCacheDetails(true).setSolver(solverPanel.getInstructions());
		    if (oldHasSolver != chMain.hasSolver())
			this.tablePanel.myTableControl.update(true);
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
	    if (detailsPanel.hasBlackStatusChanged()) {
		this.tablePanel.refreshTable();
	    }
	    updateCurCentrePtFromGPS();
	} else if (panelNo == DETAILS_CARD) {
	    boolean newCache = false;
	    if (chD == null) { // Empty DB - show a dummy detail
		newWaypoint(ch = new CacheHolder());
		newCache = true;
	    }
	    detailsPanel.setDetails(ch, newCache);
	} else if (panelNo == DESCRIPTION_CARD) {
	    descriptionPanel.setText(ch);
	} else if (panelNo == IMAGES_CARD) {
	    if (ch.isAddiWpt()) {
		imagePanel.setImages(ch.mainCache.getCacheDetails(true));
	    } else {
		imagePanel.setImages(chD);
	    }
	} else if (panelNo == HINTSANDLOGS_CARD) {
	    if (ch.isAddiWpt()) {
		hintLogPanel.setText(ch.mainCache.getCacheDetails(true));
	    } else {
		hintLogPanel.setText(chD);
	    }
	} else if (panelNo == SOLVER_CARD) {
	    if (ch.isAddiWpt()) {
		chMain = ch.mainCache;
		solverPanel.setInstructions(ch.mainCache);
	    } else {
		solverPanel.setInstructions(ch);
	    }
	} else if (panelNo == CALC_CARD) {
	    if (ch != null)
		calcPanel.setFields(ch);
	} else if (panelNo == GOTO_CARD) {
	    getPositionAndSetDestination();
	} else if (panelNo == MAP_CARD) {
	    switchToMovingMap();
	    if (oldCard == MENU_CARD)
		oldCard = LIST_CARD;
	    select(oldCard);
	} else if (panelNo == RADAR_CARD) {
	    radarPanel.setParam(cacheDB, ch);
	    radarPanel.drawThePanel();
	    updateCurCentrePtFromGPS();
	}
	oldCard = panelNo;
	Vm.showWait(false); // else stays on subpanels, even if set false there, don't know why
    }

    /**
     * this is called from MovingMap Cache context menu
     * 
     * @param chi
     *            , the CacheHolder for the Cache to switch to
     * @param panelNo
     *            1=DetailsPanel 2=Description Panel
     */
    public void openPanel(CacheHolder chi, int panelNo) {
	// oldCard could have been DETAILS_CARD or GOTO_CARD
	// on DETAILS_CARD changes already should have been applied before entering map
	// on GOTO_CARD there is no action
	// onLeavingPanel(oldCard);

	// do we really need this actions ???
	// onEnteringPanel(LIST_CARD);

	// to switch to cache selected on map we do action as if leaving LIST_CARD
	this.tablePanel.selectRow(Global.profile.getCacheIndex(chi.getWayPoint()));
	onLeavingPanel(LIST_CARD);

	if (panelNo == DETAILS_CARD) {
	    onEnteringPanel(DETAILS_CARD);
	    select(DETAILS_CARD);
	} else if (panelNo == DESCRIPTION_CARD) {
	    onEnteringPanel(DESCRIPTION_CARD);
	    select(DESCRIPTION_CARD);
	}

    }

    /**
     * this is called from goto / MovingMap / CalcPanel / DetailsPanel and so on to offer the user the possibility of entering an new waypoint at a given position. pCh must already been preset with a valid CacheHolder object
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
	int selectedIndex = Global.profile.getCacheIndex(lastselected);
	if (selectedIndex >= 0) {
	    // why not using the target ???
	    CacheHolder selectedCache = Global.profile.cacheDB.get(selectedIndex);
	    // try to start new waypoint with real coords
	    if (!pCh.getPos().isValid()) {
		pCh.setPos(selectedCache.getPos());
	    }
	    if (selectedCache.isAddiWpt()) {
		if (selectedCache.mainCache != null) {
		    mainCache = selectedCache.mainCache.getWayPoint();
		    // try to start new waypoint with real coords
		    if (!pCh.getPos().isValid()) {
			pCh.setPos(selectedCache.mainCache.getPos());
		    }
		} else {
		    mainCache = null;
		}
	    }
	}
	if (CacheType.isAddiWpt(pCh.getType()) && mainCache != null && mainCache.length() > 2) {
	    pCh.setWayPoint(Global.profile.getNewAddiWayPointName(mainCache));
	    Global.profile.setAddiRef(pCh);
	} else {
	    pCh.setWayPoint(Global.profile.getNewWayPointName("CW"));
	    lastselected = pCh.getWayPoint();
	}
	pCh.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
	chD = pCh.getCacheDetails(false);
	this.ch = pCh;
	cacheDB.add(pCh);
	Global.profile.notifyUnsavedChanges(true); // Just to be sure
	this.tablePanel.myTableModel.numRows++;
	detailsPanel.setDetails(pCh, true);
	oldCard = DETAILS_CARD;
	if (this.cardPanel.selectedItem != 1)
	    select(DETAILS_CARD);
	solverPanel.setInstructions(pCh);
	// this.tablePanel.refreshTable(); // moved this instruction to onLeavingPanel

    }

    private void switchToMovingMap() {
	CWPoint centerTo = getPositionAndSetDestination();
	// lade entsprechende Karte
	if (centerTo != null && centerTo.isValid())
	    movingMap.display(centerTo);
	else
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1513, "Cannot start moving map without valid coordinates. Please enter coordinates as destination, as center, in selected cache or start GPS")).wait(FormBase.OKB);
    }

    private CWPoint getPositionAndSetDestination() {
	CWPoint position = null;
	if (navigate.isGpsPosValid())
	    position = new CWPoint(navigate.gpsPos);
	else {
	    if (navigate.destination.isValid())
		position = new CWPoint(navigate.destination);
	    else {
		if (ch != null && ch.getPos().isValid()) {
		    position = new CWPoint(ch.getPos());
		    navigate.setDestination(ch);
		} else {
		    if (Global.pref.getCurCentrePt().isValid()) {
			position = new CWPoint(Global.pref.getCurCentrePt());
			navigate.setDestination(position);
		    }
		}
	    }
	}
	return position;
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
	if (Global.profile.hasUnsavedChanges())
	    Global.profile.saveIndex(true);
	this.tablePanel.saveColWidth();
	Global.pref.savePreferences();
    }

    private void updateCurCentrePtFromGPS() {
	if (Global.pref.setCurrentCentreFromGPSPosition) {
	    if (navigate.gpsRunning) {
		CWPoint whereAmI = navigate.gpsPos;
		if (whereAmI.isValid()) {
		    CWPoint curCentr = Global.pref.getCurCentrePt();
		    if (whereAmI.latDec != curCentr.latDec || whereAmI.lonDec != curCentr.lonDec) {
			Global.pref.setCurCentrePt(whereAmI);
		    }
		}
	    }
	}
    }

    public void onEvent(Event ev) {
	// This section clears old data when a new line is selected in the table
	if (ev instanceof TableEvent) {
	    clearDetails();
	} else if (ev instanceof MultiPanelEvent) {
	    // Check whether a profile change is allowed, if not disable the relevant options
	    checkProfileChange();
	    // Perform clean up actions for the panel we are leaving
	    onLeavingPanel(oldCard);
	    // Prepare actions for the panel we are about to enter
	    onEnteringPanel(((MultiPanelEvent) ev).selectedIndex);
	    // Card cp = (Card) ((CardPanel) ev.target).cards.get(((MultiPanelEvent) ev).selectedIndex);
	} else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target instanceof mButton) {
		mButton btn = (mButton) ev.target;
		if (btn.name.equals("select")) {
		    this.select(Integer.parseInt(btn.getTag(999, "0").toString()));
		}
	    }
	}
	super.onEvent(ev); // Make sure you call this.
    }

}
//

