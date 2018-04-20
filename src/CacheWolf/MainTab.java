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

import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.*;
import CacheWolf.navi.GotoPanel;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import CacheWolf.utils.MyLocale;
import ewe.fx.Graphics;
import ewe.fx.IconAndText;
import ewe.sys.Vm;
import ewe.ui.*;

/**
 * This class creates the tabbed panel and sets the tabs to the respective other panels. Important is to have a look at the event handler!<br>
 * Class ID = 1200
 *
 * @see MainForm
 */
public class MainTab extends mTabbedPanel {

    public static MainTab itself;
    // following numbers depend on tabNames order (MENU_CARD must be the last tabName)
    public static int DETAILS_CARD = 1;
    public static int DESCRIPTION_CARD = 2;
    public static int MAP_CARD = 9;
    static int LIST_CARD = 0;
    static int IMAGES_CARD = 3;
    static int HINTSANDLOGS_CARD = 4;
    static int SOLVER_CARD = 5;
    static int CALC_CARD = 6;
    static int GOTO_CARD = 7;
    static int RADAR_CARD = 8;
    static int MENU_CARD = 10;

    public TablePanel tablePanel;
    public DetailsPanel detailsPanel;
    public GotoPanel gotoPanel;
    public CacheHolder ch = null, chMain = null;
    public MovingMap movingMap;
    public Navigate navigate;
    public String mainCache = "";
    DescriptionPanel descriptionPanel;
    HintLogPanel hintLogPanel;
    CalcPanel calcPanel;
    ImagePanel imagePanel;
    SolverPanel solverPanel;
    CellPanel mapPanel;
    RadarPanel radarPanel;
    CellPanel homePanel;
    CellPanel selectPanels[];
    CacheDB cacheDB;
    String lastselected = "";
    CacheHolderDetail chD = null;
    int oldCard;
    boolean cacheDirty = false;

    Control[] baseControls;

    public MainTab() {
        itself = this;
        if (!Preferences.itself().tabsAtTop)
            tabLocation = SOUTH;

        cacheDB = MainForm.profile.cacheDB;

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

        // Don't expand tabs if the screen is very narrow,
        // i.e. HP IPAQ 65xx, 69xx
        // int sw = Preferences.itself().getScreenWidth();
        // if (sw <= 480)
        //	this.dontExpandTabs = true;
        // if true no Tab outside is shown (big icons)

        baseControls = new Control[]{tablePanel, //
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

        String[] tabNames = {MyLocale.getMsg(1200, "List"), //
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

        String[] imageNames = {"list", //
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

        int sw = Preferences.itself().getScreenWidth();
        int buttonsPerLine = Math.min(9, sw / 115);
        for (int i = 0; i < tabNames.length; i++) {
            CellPanel alles = new CellPanel();
            CellPanel selection = new CellPanel();
            if (!Preferences.itself().tabsAtTop)
                alles.addLast(baseControls[i]);
            //selection.equalWidths = true;
            if (Preferences.itself().noTabs) {
                // navigation with buttons instead of tabs
                mButton btn;

                btn = GuiImageBroker.getButton("", "home");//MyLocale.getMsg(1211, "Home")
                btn.name = "select";
                btn.setTag(999, "" + tabNames.length);
                selection.addNext(btn, DONTSTRETCH | DONTFILL, LEFT);

                int start = Math.max(0, i - (buttonsPerLine / 2));
                if (start + buttonsPerLine >= tabNames.length) {
                    start = tabNames.length - buttonsPerLine - 1;
                }
                for (int j = start; j <= start + buttonsPerLine; j++) {
                    if (j != i) {
                        btn = GuiImageBroker.getButton(tabNames[j], imageNames[j]);
                        btn.name = "select";
                        btn.setTag(999, "" + (j));
                        selection.addNext(btn);
                    } else {
                    }
                }
            }
            selection.addLast(null);
            alles.addLast(selection, HSTRETCH, HFILL);

            if (Preferences.itself().tabsAtTop)
                alles.addLast(baseControls[i]);
            Card c = this.addCard(alles, tabNames[i], null);
            c.iconize(GuiImageBroker.getImage(imageNames[i]), Preferences.itself().useIcons);
            if (Preferences.itself().noTabs) {
                ((IconAndText) c.image).textPosition = Graphics.Down;
            }
        }

        movingMap = new MovingMap();
        navigate = new Navigate(); // attention movingMap must be created before;

        if (Preferences.itself().noTabs) {
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

    /**
     * Code to execute when leaving a panel (oldCard is the panel number)
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
                lastselected = ch.getCode(); // Used in
                // Parser.Skeleton
                chD = ch.getDetails();
            }
        }
        if (panelNo == MainTab.DETAILS_CARD) {
            // Update chD with Details
            if (detailsPanel.isDirty()) {
                cacheDirty = true;
                boolean needTableUpdate = detailsPanel.getNeedsTableUpdate();
                detailsPanel.saveDirtyWaypoint();
                if (needTableUpdate) {
                    // This sorts the waypoint (if it is new) into the right position
                    this.tablePanel.myTableModel.updateRows();
                    this.tablePanel.selectRow(MainForm.profile.getCacheIndex(detailsPanel.getCache().getCode()));
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
                    // For safety reasons: Immediately save solver instructions when switching panels updatePendingChanges();
                } else {
                    boolean oldHasSolver = chMain.hasSolver();
                    chMain.getDetails().setSolver(solverPanel.getInstructions());
                    if (oldHasSolver != chMain.hasSolver())
                        this.tablePanel.myTableControl.update(true);
                    chMain.saveCacheDetails();
                    chMain = null;
                }
            }
        }
    }

    /**
     * Code to execute when entering a panel (getSelectedItem() is the panel number)
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
                imagePanel.setImages(ch.mainCache.getDetails());
            } else {
                imagePanel.setImages(chD);
            }
        } else if (panelNo == HINTSANDLOGS_CARD) {
            if (ch.isAddiWpt()) {
                hintLogPanel.setText(ch.mainCache.getDetails());
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
     * @param _wayPoint the wayPoint for the Cache to switch to
     * @param toPanel   1=DetailsPanel 2=Description Panel ...
     */
    public void openPanel(int fromPanel, String _wayPoint, int toPanel) {
        onLeavingPanel(fromPanel);
        // to switch to cache we do action as if leaving LIST_CARD
        this.tablePanel.selectRow(MainForm.profile.getCacheIndex(_wayPoint));
        onLeavingPanel(LIST_CARD);
        onEnteringPanel(toPanel);
        select(toPanel);
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
        int selectedIndex = MainForm.profile.getCacheIndex(lastselected);
        if (selectedIndex >= 0) {
            // why not using the target ???
            CacheHolder selectedCache = MainForm.profile.cacheDB.get(selectedIndex);
            // try to start new waypoint with real coords
            if (!pCh.getWpt().isValid()) {
                pCh.setWpt(selectedCache.getWpt());
            }
            if (selectedCache.isAddiWpt()) {
                if (selectedCache.mainCache != null) {
                    mainCache = selectedCache.mainCache.getCode();
                    // try to start new waypoint with real coords
                    if (!pCh.getWpt().isValid()) {
                        pCh.setWpt(selectedCache.mainCache.getWpt());
                    }
                } else {
                    mainCache = null;
                }
            }
        }
        if (CacheType.isAddiWpt(pCh.getType()) && mainCache != null && mainCache.length() > 2) {
            pCh.setCode(MainForm.profile.getNewAddiWayPointName(mainCache));
            MainForm.profile.setAddiRef(pCh);
        } else {
            pCh.setCode(MainForm.profile.getNewWayPointName("CW"));
            lastselected = pCh.getCode();
        }
        pCh.setSize(CacheSize.CW_SIZE_NOTCHOSEN);
        chD = pCh.getDetails();
        this.ch = pCh;
        cacheDB.add(pCh);
        MainForm.profile.notifyUnsavedChanges(true); // Just to be sure
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
        if (centerTo != null && centerTo.isValid()) {
            movingMap.display(centerTo);
        } else
            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1513, "Cannot start moving map without valid coordinates. Please enter coordinates as destination, as center, in selected cache or start GPS")).wait(FormBase.OKB);
    }

    private CWPoint getPositionAndSetDestination() {
        CWPoint position = null;
        if (navigate.isGpsPosValid())
            position = new CWPoint(Navigate.gpsPos);
        else {
            //if (Navigate.destination.isValid())
            //position = new CWPoint(Navigate.destination);
            //else {
            if (ch != null && ch.getWpt().isValid()) {
                position = new CWPoint(ch.getWpt());
                navigate.setDestination(ch);
            } else {
                if (Preferences.itself().curCentrePt.isValid()) {
                    position = new CWPoint(Preferences.itself().curCentrePt);
                    navigate.setDestination(position);
                }
            }
            //}
        }
        return position;
    }

    void updatePendingChanges() {
        if (cacheDirty) {
            if (chD != null)
                chD.getParent().saveCacheDetails();
            cacheDirty = false;
        }
    }

    /**
     * Save the index file
     *
     * @param askForConfirmation is ignored, old: If true, the save can be cancelled by user
     */
    public void saveUnsavedChanges(boolean askForConfirmation) {
        if (oldCard != LIST_CARD) {
            onLeavingPanel(oldCard);
            onEnteringPanel(LIST_CARD);
            oldCard = LIST_CARD;
        }
        updatePendingChanges();
        MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR, Profile.NOFORCESAVE);
        this.tablePanel.saveColWidth();
        Preferences.itself().savePreferences();
    }

    private void updateCurCentrePtFromGPS() {
        if (Preferences.itself().setCurrentCentreFromGPSPosition) {
            if (navigate.gpsRunning) {
                CWPoint whereAmI = Navigate.gpsPos;
                if (whereAmI.isValid()) {
                    CWPoint curCentr = Preferences.itself().curCentrePt;
                    if (!whereAmI.equals(curCentr)) {
                        MainForm.itself.setCurCentrePt(whereAmI);
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
