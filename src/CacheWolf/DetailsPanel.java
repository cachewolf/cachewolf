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

import CacheWolf.controls.DateTimeChooser;
import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.*;
import CacheWolf.exp.OCLogExport;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.CWWrapper;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import ewe.fx.*;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.graphics.MosaicPanel;
import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Vector;
import ewe.util.mString;

/**
 * Class to create the panel to show the way point details.<br>
 * Also allows for creation and editing of way points
 */
public class DetailsPanel extends CellPanel {

    private final int BUG = 0;
    private final int GOTO = 1;
    private final int NOTES = 2;
    private int thisWidth;
    private int thisHeight;
    private MyChoice waypoint;
    private mInput alias;
    private mInput owner;
    private MyChoice cacheType;
    private MyChoice difficulty;
    private MyChoice terrain;
    private MyChoice cacheSize;
    private MyChoice more;

    private mButton coordinates;
    private mButton btnCountryState;
    private mButton btnHint;

    private mLabel lblOwner;
    private mLabel lblSolved;
    private mLabel lblBlacklist;
    private mLabel lblStatus;

    private LastLogsPanel lastLogs;
    private mCheckBox cbIsSolved;
    private mCheckBox cbIsBlacklisted;
    private mButton btnFoundDate;
    private mButton hiddenDate;
    private mComboBox chcStatus;
    private mTextPad waypointNotes;
    private AttributesViewer attViewer;
    private CellPanel logPanel;
    private mTextPad ownLog;

    // ===== data handles =====
    private mButton btnLog;
    private mButton btnLogToOC;

    // ===== flags =====
    private mButton btnEditLog;
    /**
     * waypoint to be displayed.
     */
    private CacheHolder ch;
    private CacheHolder mainCache;
    /**
     * details have changed FIXME: make this obsolete
     */
    private boolean dirtyDetails = false;
    /**
     * cache is blacklisted FIXME: make this obsolete
     */
    private boolean blackStatus = false;
    /**
     * blacklist status was changed by user FIXME: make this obsolete
     */
    private boolean blackStatusChanged = false;
    /**
     * FIXME
     */
    private boolean needsTableUpdate = false;
    /**
     * String to display for invalid or not applicable terrain or difficulty values.
     */

    private String newWaypoint;
    private byte newDifficulty;
    private byte newTerrain;
    private byte newCacheType;
    private byte newCacheSize;
    private String newHiddenDate;
    private String hint;

    private boolean isBigScreen;

    /**
     * public constructor for detail panels. should only be called from main tab.
     */
    public DetailsPanel() {
        super();

        alias = new mInput();
        waypoint = new MyChoice("blabla", new String[]{"blabla"});
        hiddenDate = GuiImageBroker.getButton(MyLocale.getMsg(305, "Hidden on:") + MyLocale.getMsg(31415, "Set hidden date"), "calendar");
        hiddenDate.setToolTip(MyLocale.getMsg(31415, "Set hidden date"));
        final String[] tdSelectionList = new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"};
        difficulty = new MyChoice(MyLocale.getMsg(1000, "D"), "star0", tdSelectionList);
        difficulty.getBtn().setToolTip(MyLocale.getMsg(31415, "Edit difficulty"));
        terrain = new MyChoice(MyLocale.getMsg(1001, "T"), "star0", tdSelectionList);
        terrain.getBtn().setToolTip(MyLocale.getMsg(31415, "Edit terrain"));
        cacheType = new MyChoice(CacheType.type2Gui(CacheType.CW_TYPE_REFERENCE), CacheType.typeImageNameForId(CacheType.CW_TYPE_TRADITIONAL), CacheType.guiTypeStrings());
        cacheSize = new MyChoice(CacheType.type2Gui(CacheType.CW_TYPE_REFERENCE), CacheType.typeImageNameForId(CacheType.CW_TYPE_TRADITIONAL), CacheSize.guiSizeStrings());
        lblOwner = new mLabel(MyLocale.getMsg(306, "Owner:"));
        owner = new mInput();
        coordinates = new mButton();
        coordinates.setToolTip(MyLocale.getMsg(31415, "Edit coordinates"));
        btnCountryState = new mButton("");
        btnHint = GuiImageBroker.getButton(MyLocale.getMsg(402, "Hint"), "decode");
        attViewer = new AttributesViewer();
        String[] texts = new String[]{MyLocale.getMsg(346, "Show travelbugs"), //
                MyLocale.getMsg(326, "Set as destination and show Compass View"), //
                MyLocale.getMsg(351, "Add/Edit notes") //
        };
        String[] icons = new String[]{"bug", "goto", "notes"};
        more = new MyChoice(MyLocale.getMsg(632, "More"), "more", texts, icons);
        lblSolved = new mLabel(MyLocale.getMsg(362, "solved") + ": ");
        lblBlacklist = new mLabel(MyLocale.getMsg(363, "Blacklist") + ": ");
        lblStatus = new mLabel(MyLocale.getMsg(307, "Status:"));
        cbIsSolved = new mCheckBox("");
        cbIsBlacklisted = new mCheckBox("");
        chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(), 0);
        btnFoundDate = GuiImageBroker.getButton("", "calendar");
        btnFoundDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));
        ownLog = new mTextPad();
        btnLog = GuiImageBroker.getButton(MyLocale.getMsg(1052, "Log online in Browser"), "");
        btnLogToOC = GuiImageBroker.getButton(MyLocale.getMsg(1210, "logs to OC"), "");
        btnEditLog = GuiImageBroker.getButton(MyLocale.getMsg(1055, "Change log (online)"), "");
        waypointNotes = new mTextPad();
        // waypointNotes.modify(ControlConstants.NotEditable, 0);

        makeLayout(Preferences.itself().lastSavedWidth, Preferences.itself().lastSavedHeight);

    }

    public void makeLayout(int windowWidth, int windowHeight) {

        thisWidth = windowWidth;
        thisHeight = windowHeight;
        double f = Preferences.itself().fontSize * 12.5;
        int anzPerLine = Math.max(1, (int) (((double) windowWidth) / f));
        isBigScreen = anzPerLine > 1;

        CellPanel ownerPanel = new CellPanel();
        ownerPanel.addNext(lblOwner, DONTSTRETCH, DONTFILL | LEFT);
        ownerPanel.addNext(owner, STRETCH, FILL);

        if (isBigScreen)
            lastLogs = new LastLogsPanel((int) (0.75 * windowWidth), (int) (0.5 * windowHeight));
        else
            lastLogs = new LastLogsPanel(windowWidth, windowHeight);

        CellPanel mainPanel = new CellPanel();
        mainPanel.equalWidths = true;

        Vector panelControls = new Vector();
        CellPanel panelTypeWaypoint = new CellPanel();
        panelTypeWaypoint.addNext(waypoint.getBtn());
        panelTypeWaypoint.addLast(cacheType.getBtn());
        panelControls.add(panelTypeWaypoint);
        panelControls.add(hiddenDate);
        panelControls.add(ownerPanel);
        panelControls.add(difficulty.getBtn());
        panelControls.add(terrain.getBtn());
        panelControls.add(cacheSize.getBtn());
        panelControls.add(coordinates);
        if (isBigScreen) {
            panelControls.add(btnCountryState);
        }
        panelControls.add(btnHint);
        panelControls.add(lastLogs);
        panelControls.add(more.getBtn());

        mainPanel.addLast(alias);
        //         ist | big icons | small icons | no icons | small icon | big icon
        //         257 | +text     |  +text      | only text| no text    | no text
        // 240  ->  1  |     1     |   1         |   1      |     1      |    1
        // 320  ->  1  |     1     |   1         |   1      |     2      |    1
        // 480  ->  1  |     2     |   2         |   2      |     3      |    2
        // 514  ->  2
        // 640  ->  2  |     3     |   3         |   3      |     4      |    3
        // 771  ->  3
        // 800  ->  3  |     3     |   4         |   4      |     6      |    4
        // 1028 ->  4
        // 1280 ->  4  |     4     |   6         |   7      |     9      |    7
        // 1285 ->  5
        // 1920 ->  7  |     9     |  10         |  11      |    11      |   11
        int i = 0;
        for (Iterator ite = panelControls.iterator(); ite.hasNext(); ) {
            Control ctrl = (Control) ite.next();
            i++;
            if ((i % anzPerLine) == 0) {
                mainPanel.addLast(ctrl);
            } else {
                mainPanel.addNext(ctrl);
            }
        }
        mainPanel.addLast(null);
        if (isBigScreen) {
            mainPanel.setText(MyLocale.getMsg(1201, "Details"));
            mainPanel.addLast(attViewer, HSTRETCH, HFILL);
        }

        addLast(mainPanel, HSTRETCH, HFILL);

        CellPanel solvedPanel = new CellPanel();
        solvedPanel.addNext(lblSolved, DONTSTRETCH, DONTFILL | LEFT);
        solvedPanel.addNext(cbIsSolved);
        solvedPanel.addNext(lblBlacklist, DONTSTRETCH, DONTFILL | LEFT);
        solvedPanel.addNext(cbIsBlacklisted);

        CellPanel foundPanel = new CellPanel();
        foundPanel.addNext(lblStatus, DONTSTRETCH, DONTFILL | LEFT);
        foundPanel.addNext(chcStatus, HSTRETCH, (HFILL | RIGHT));
        foundPanel.addLast(btnFoundDate, DONTSTRETCH, DONTFILL);


        CellPanel statusPanel = new CellPanel();
        statusPanel.addLast(solvedPanel, HSTRETCH, HFILL);
        statusPanel.addLast(foundPanel, HSTRETCH, HFILL);
        if (isBigScreen)
            statusPanel.setText(MyLocale.getMsg(307, "Status:"));
        addLast(statusPanel, HSTRETCH, HFILL);

        if (isBigScreen) {
            logPanel = new CellPanel();
            logPanel.setText(" ");
            logPanel.addNext(btnLog);
            logPanel.addNext(btnLogToOC);
            logPanel.addLast(btnEditLog);
            logPanel.addLast(new MyScrollBarPanel(ownLog), STRETCH, FILL);
            addLast(logPanel, STRETCH, FILL);
            MyScrollBarPanel sp = new MyScrollBarPanel(waypointNotes);
            sp.setText(MyLocale.getMsg(308, "Notes"));
            addLast(sp, STRETCH, FILL);
        } else {
            addLast(attViewer, HSTRETCH, HFILL);
        }

    }

    public void clear() {
        attViewer.clear();
    }

    public boolean getNeedsTableUpdate() {
        return needsTableUpdate;
    }

    public CacheHolder getCache() {
        return ch;
    }

    public boolean isDirty() {
        return dirtyDetails || needsTableUpdate;
    }

    public boolean hasBlackStatusChanged() {
        return blackStatusChanged;
    }

    public void setDetails(final CacheHolder _ch, boolean isNew) {
        ch = _ch;
        needsTableUpdate = isNew;
        mainCache = ch;
        if (ch.isAddiWpt() && (ch.mainCache != null)) {
            mainCache = ch.mainCache;
        }
        dirtyDetails = false;

        GuiImageBroker.setButtonText(waypoint.getBtn(), ch.getCode());
        int wptsSize = mainCache.addiWpts.size() + 2;
        String[] waypointList = new String[wptsSize];
        String[] iconList = new String[wptsSize];
        waypointList[0] = mainCache.getCode();
        iconList[0] = CacheType.typeImageNameForId(mainCache.getType());
        int tomark = 0;
        for (int i = 0; i < mainCache.addiWpts.size(); i++) {
            CacheHolder tmpCh = (CacheHolder) mainCache.addiWpts.get(i);
            if (ch == tmpCh)
                tomark = i + 1;
            waypointList[i + 1] = tmpCh.getCode() + " " + tmpCh.getName();
            iconList[i + 1] = CacheType.typeImageNameForId(tmpCh.getType());
        }
        waypointList[wptsSize - 1] = MyLocale.getMsg(311, "Create Waypoint");
        iconList[wptsSize - 1] = "newwpt";
        this.waypoint.setSelectionList(waypointList, iconList);
        this.waypoint.mark(tomark);

        alias.setText(ch.getName());
        coordinates.setText(ch.getWpt().toString());
        newHiddenDate = mainCache.getHidden();
        GuiImageBroker.setButtonText(hiddenDate, MyLocale.getMsg(305, "Hidden on:") + mainCache.getHidden());
        owner.setText(mainCache.getOwner());
        this.cbIsSolved.setState(ch.isSolved());
        chcStatus.setText(ch.getStatusText());
        newCacheType = ch.getType();
        GuiImageBroker.setButtonIconAndText(cacheType.getBtn(), CacheType.type2Gui(newCacheType), GuiImageBroker.makeImageForButton(cacheType.getBtn(), CacheType.type2Gui(newCacheType), CacheType.typeImageNameForId(newCacheType)));
        this.cbIsBlacklisted.setState(ch.isBlack());

        blackStatus = ch.isBlack();
        blackStatusChanged = false;

        this.newWaypoint = ch.getCode();
        if (this.newWaypoint.length() == 0) {
            createWptName();
        }

        if (mainCache.hasBugs()) {
            more.enable(BUG);
        } else {
            more.disable(BUG);
        }

        newCacheSize = mainCache.getSize();
        String text = CacheSize.cw2ExportString(newCacheSize);
        String icon = CacheSize.cacheSize2ImageName(newCacheSize);
        IImage btnSizeNewImage = GuiImageBroker.makeImageForButton(cacheSize.getBtn(), text, icon);
        GuiImageBroker.setButtonIconAndText(cacheSize.getBtn(), text, btnSizeNewImage);
        CacheHolderDetail mainCacheDetails = mainCache.getDetails();
        attViewer.showImages(mainCacheDetails.attributes);

        if (ch.isAddiWpt() || ch.isCustomWpt()) {
            disable(difficulty.getBtn());
            disable(terrain.getBtn());
            disable(cacheSize.getBtn());
            disable(owner);
            disable(this.hiddenDate);
            disable(this.cbIsBlacklisted);
        } else {
            enable(difficulty.getBtn());
            enable(terrain.getBtn());
            enable(cacheSize.getBtn());
            enable(owner);
            enable(this.hiddenDate);
            enable(this.cbIsBlacklisted);
        }

        newDifficulty = mainCache.getDifficulty();
        String longD = "-.-"; // Custom or invalid
        newTerrain = mainCache.getTerrain();
        String longT = "-.-"; // Custom or invalid
        if (!ch.isCustomWpt()) {
            if (CacheTerrDiff.isValidTD(newTerrain)) {
                longT = CacheTerrDiff.longDT(newTerrain);
            } else {
                mainCache.setIncomplete(true);
                Preferences.itself().log(mainCache.getCode() + " has wrong terrain " + newTerrain);
            }
            if (CacheTerrDiff.isValidTD(newDifficulty)) {
                longD = CacheTerrDiff.longDT(newDifficulty);
            } else {
                mainCache.setIncomplete(true);
                Preferences.itself().log(mainCache.getCode() + " has wrong difficulty " + newDifficulty);
            }
        }
        GuiImageBroker.setButtonIconAndText(difficulty.getBtn(), MyLocale.getMsg(1000, "D") + " " + longD, GuiImageBroker.makeImageForButton(difficulty.getBtn(), MyLocale.getMsg(1000, "D") + " " + longD, "star" + newDifficulty));
        GuiImageBroker.setButtonIconAndText(terrain.getBtn(), MyLocale.getMsg(1001, "T") + " " + longT, GuiImageBroker.makeImageForButton(terrain.getBtn(), MyLocale.getMsg(1001, "T") + " " + longT, "star" + newTerrain));

        hint = "";
        if (ch.isAddiWpt()) {
            CacheHolderDetail cacheDetails = ch.getDetails();
            hint = STRreplace.replace(cacheDetails.LongDescription, "<br>", "\n");
            if (hint.length() > 0) {
                hint = hint + "\n-\n";
            }
        }
        hint += STRreplace.replace(Common.rot13(mainCacheDetails.Hints), "<br>", "\n");
        if (hint.length() > 0) {
            enable(btnHint);
        } else {
            disable(btnHint);
        }

        this.btnCountryState.setText(mainCacheDetails.getCountry() + " / " + mainCacheDetails.getState());

        lastLogs.images.clear();
        Dimension lastLogsDimension = lastLogs.getMySize(null);
        int lastLogsWidth = lastLogsDimension.width;
        int lastLogsHeight = lastLogsDimension.height;
        int anz = Math.min(5, mainCacheDetails.CacheLogs.size());
        for (int i = 0; i < anz; i++) {
            Log log = mainCacheDetails.CacheLogs.getLog(i);
            if (!(log.getIcon().equals(Log.MAXLOGICON))) {
                AniImage ai = new AniImage(log.getIcon());
                ai.setLocation(i * Math.max(ai.getWidth(), (int) (lastLogsWidth / anz)), (int) ((lastLogsHeight - ai.getHeight()) / 2));
                lastLogs.addImage(ai, log);
            }
        }
        lastLogs.refresh();

        ownLog.setText("");
        if (isBigScreen) {
            disable(btnEditLog);
            disable(btnLogToOC);
            disable(btnLog);
            //if (ch.isCacheWpt()) {
            if (mainCache.getDetails().getOwnLog() != null) {
                logPanel.setText(MyLocale.getMsg(278, "Eigener Log: ") + mainCache.getDetails().getOwnLog().getLogID());
                if (mainCache.getDetails().getOwnLog().getLogID().length() == 0) {
                    enable(btnLog);
                } else {
                    enable(btnEditLog);
                    String ocWpName = mainCache.getIdOC();
                    if (ocWpName.length() > 0 && ocWpName.charAt(0) < 65) {
                        enable(btnLogToOC);
                    } else {
                        disable(btnLogToOC);
                    }
                }
                ownLog.setText(mainCache.getDetails().getOwnLog().getMessageWithoutHTML());
            } else {
                logPanel.setText(" ");
                enable(btnLog);
                disable(btnEditLog);
                disable(btnLogToOC);
            }
            waypointNotes.setText(mainCache.getDetails().getCacheNotes());
            //}
        }
    }

    /**
     * if is addi -> returns the respective AddiWpt if is main -> returns the respective MainWpt
     */
    public void createWptName() {
        final String wpt = this.newWaypoint.toUpperCase();
        if (CacheType.isAddiWpt(newCacheType) //
                && MainTab.itself.mainCache != null //
                && (MainTab.itself.mainCache.startsWith("GC") //
                || OC.isOC(MainTab.itself.mainCache) //
                || MainTab.itself.mainCache.startsWith("CW")) && wpt.startsWith("CW")) {
            // for creating the Addiname on creating a new Waypoint
            MainTab.itself.lastselected = MainTab.itself.mainCache;
            this.newWaypoint = MainForm.profile.getNewAddiWayPointName(MainTab.itself.mainCache);
        }
        if (!CacheType.isAddiWpt(newCacheType) && !(wpt.startsWith("GC") || OC.isOC(wpt) || wpt.startsWith("CW"))) {
            this.newWaypoint = MainForm.profile.getNewWayPointName("CW");
        }
    }

    /**
     * Method to react to a user input.
     */
    public void onEvent(final Event ev) {
        ev.consumed = true;
        if (ev instanceof DataChangeEvent) {
            if (ch == null)
                return;
            if (ev.target == this.alias) {
                if (ch.setName(alias.getText().trim())) {
                    dirtyDetails = true;
                }
            } else if (ev.target == this.owner) {
                dirtyDetails = true;
            } else if (ev.target == this.ownLog) {
                dirtyDetails = true;
            } else if (ev.target == this.waypointNotes) {
                dirtyDetails = true;
            } else {
                //Preferences.itself().log("DataChangeEvent at Details for " + ev.target.toString() + ". DirtyDetails not set.");
                dirtyDetails = true;
            }
            // FIXME: check if something was actually changed, since datacachnge events also occur if you just hop through the fields with the tab key (Why? don't know!)
        } else if (ev instanceof MenuEvent) {
            if (ev.type == MenuEvent.SELECTED) {
                Menu menu = ((MenuEvent) ev).menu;
                if (menu == this.cacheType.getMnu()) {
                    if (cacheType.mark(cacheType.getSelectedIndex())) {
                        newCacheType = CacheType.guiSelect2Cw(cacheType.getSelectedIndex());
                        GuiImageBroker.setButtonIconAndText(cacheType.getBtn(), CacheType.type2Gui(newCacheType), GuiImageBroker.makeImageForButton(cacheType.getBtn(), CacheType.type2Gui(newCacheType), CacheType.typeImageNameForId(newCacheType)));
                        createWptName();
                        if (CacheType.isCacheWpt(newCacheType)) {
                            enable(terrain.getBtn());
                            enable(difficulty.getBtn());
                            enable(cacheSize.getBtn());
                            enable(owner);
                            enable(this.hiddenDate);
                            more.enable(BUG);
                            enable(this.cbIsBlacklisted);
                        } else {
                            disable(terrain.getBtn());
                            disable(difficulty.getBtn());
                            disable(cacheSize.getBtn());
                            disable(owner);
                            disable(this.hiddenDate);
                            more.disable(BUG);
                            disable(this.cbIsBlacklisted);
                            newCacheSize = CacheSize.CW_SIZE_NOTCHOSEN;
                            GuiImageBroker.setButtonIconAndText(cacheSize.getBtn(), CacheSize.cw2ExportString(newCacheSize),
                                    GuiImageBroker.makeImageForButton(cacheSize.getBtn(), CacheSize.cw2ExportString(newCacheSize), CacheSize.cacheSize2ImageName(newCacheSize)));
                            this.newTerrain = CacheTerrDiff.CW_DT_UNSET;
                            this.newDifficulty = CacheTerrDiff.CW_DT_UNSET;
                            GuiImageBroker.setButtonIconAndText(difficulty.getBtn(), MyLocale.getMsg(1000, "D"), GuiImageBroker.makeImageForButton(difficulty.getBtn(), MyLocale.getMsg(1000, "D"), "star0"));
                            GuiImageBroker.setButtonIconAndText(terrain.getBtn(), MyLocale.getMsg(1001, "T"), GuiImageBroker.makeImageForButton(terrain.getBtn(), MyLocale.getMsg(1001, "T"), "star0"));
                        }
                        dirtyDetails = true;
                    }
                } else if (menu == this.waypoint.getMnu()) {
                    String selectedValue = (String) waypoint.getSelectedValue();
                    if (selectedValue.startsWith(this.newWaypoint)) {
                        // editieren
                        InfoBox inf = new InfoBox(MyLocale.getMsg(757, "Input"), MyLocale.getMsg(756, "Edit waypoint"), InfoBox.INPUT);
                        inf.setInput(this.newWaypoint);
                        if (inf.execute() == FormBase.IDOK) {
                            this.newWaypoint = inf.getInput().trim().toUpperCase();
                            if (!this.newWaypoint.equals(this.ch.getCode())) {
                                // perhaps check against internal DB
                                // filename is LowerCase
                                if ((new File(MainForm.profile.dataDir + this.newWaypoint.toLowerCase() + ".xml")).exists()) {
                                    new InfoBox("Warning :", this.newWaypoint + "\n" + MyLocale.getMsg(275, "Waypoint already exists!")).wait(FormBase.OKB);
                                    // revert waypointname
                                    this.newWaypoint = this.ch.getCode();
                                } else {
                                    this.waypoint.setText(this.waypoint.getSelectedIndex(), this.newWaypoint + " " + ch.getName());
                                    this.waypoint.getBtn().setText(this.newWaypoint);
                                    dirtyDetails = true;
                                    needsTableUpdate = true;
                                }
                            }
                        }
                    } else if (selectedValue.equals(MyLocale.getMsg(311, "Create Waypoint"))) {
                        // new waypoint
                        this.createWaypoint();
                    } else {
                        // change to
                        if (!mainCache.showAddis()) {
                            mainCache.setShowAddis(true);
                            MainTab.itself.tablePanel.refreshTable();
                        }
                        MainTab.itself.openPanel(MainTab.DETAILS_CARD, mString.split(selectedValue, ' ')[0], MainTab.DETAILS_CARD);
                    }
                } else if (menu == this.difficulty.getMnu()) {
                    if (difficulty.mark(difficulty.getSelectedIndex())) {
                        this.newDifficulty = (byte) (this.difficulty.getSelectedIndex() * 5 + 10);
                        String longD = CacheTerrDiff.longDT(this.newDifficulty);
                        GuiImageBroker.setButtonIconAndText(difficulty.getBtn(), MyLocale.getMsg(1000, "D") + " " + longD, GuiImageBroker.makeImageForButton(difficulty.getBtn(), MyLocale.getMsg(1000, "D") + " " + longD, "star" + this.newDifficulty));
                        dirtyDetails = true;
                    }
                } else if (menu == this.terrain.getMnu()) {
                    if (terrain.mark(terrain.getSelectedIndex())) {
                        this.newTerrain = (byte) (terrain.getSelectedIndex() * 5 + 10);
                        String longT = CacheTerrDiff.longDT(this.newTerrain);
                        GuiImageBroker.setButtonIconAndText(terrain.getBtn(), MyLocale.getMsg(1001, "T") + " " + longT, GuiImageBroker.makeImageForButton(terrain.getBtn(), MyLocale.getMsg(1001, "T") + " " + longT, "star" + this.newTerrain));
                        dirtyDetails = true;
                    }
                } else if (menu == this.cacheSize.getMnu()) {
                    if (cacheSize.mark(cacheSize.getSelectedIndex())) {
                        newCacheSize = CacheSize.guiSizeStrings2CwSize(cacheSize.getSelectedValue());
                        GuiImageBroker.setButtonIconAndText(cacheSize.getBtn(), CacheSize.cw2ExportString(newCacheSize),
                                GuiImageBroker.makeImageForButton(cacheSize.getBtn(), CacheSize.cw2ExportString(newCacheSize), CacheSize.cacheSize2ImageName(newCacheSize)));
                        dirtyDetails = true;
                    }
                } else if (menu == this.more.getMnu()) {
                    switch (more.getSelectedIndex()) {
                        case BUG:
                            final TravelbugInCacheScreen ts = new TravelbugInCacheScreen(ch.getDetails().Travelbugs.toHtml(), "Travelbugs");
                            ts.execute(this.getFrame(), Gui.CENTER_FRAME);
                            break;
                        case NOTES:
                            if (isBigScreen) {
                                ch.getDetails().setCacheNotes(waypointNotes.getText());
                            }
                            final NotesScreen nsc = new NotesScreen(ch);
                            nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
                            if (isBigScreen) {
                                waypointNotes.setText(ch.getDetails().getCacheNotes());
                            }
                            break;
                        case GOTO:
                            if (ch.getWpt().isValid()) {
                                Navigate.itself.setDestination(ch);
                                MainTab.itself.select(MainTab.GOTO_CARD);
                            }
                            break;
                    }
                }
            }
        } else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == difficulty.getBtn()) {
                int startIndex = this.newDifficulty;
                startIndex = (startIndex > 0) ? (startIndex - 10) / 5 : 0;
                difficulty.mark(startIndex);
                difficulty.showMenu();
            } else if (ev.target == terrain.getBtn()) {
                int startIndex = this.newTerrain;
                startIndex = (startIndex > 0) ? (startIndex - 10) / 5 : 0;
                terrain.mark(startIndex);
                terrain.showMenu();
            } else if (ev.target == this.cacheSize.getBtn()) {
                int startIndex = CacheSize.cwSizeId2GuiSizeId(newCacheSize);
                cacheSize.mark(startIndex);
                cacheSize.showMenu();
            } else if (ev.target == this.cacheType.getBtn()) {
                int startIndex = CacheType.cw2GuiSelect(newCacheType);
                cacheType.mark(startIndex);
                cacheType.showMenu();
            } else if (ev.target == this.more.getBtn()) {
                more.showMenu();
            } else if (ev.target == waypoint.getBtn()) {
                // mark selected cache
                waypoint.showMenu();
            } else if (ev.target == this.cbIsBlacklisted) {
                if (ch.isBlack()) {
                    ch.setBlack(false);
                } else {
                    ch.setBlack(true);
                }
                blackStatus = ch.isBlack();
                ch.setAttributesFromMainCacheToAddiWpts();
                dirtyDetails = true;
                blackStatusChanged = true;
            } else if (ev.target == coordinates) {
                CWPoint coords = new CWPoint(coordinates.getText(), TransformCoordinates.DMM);
                if (Vm.isMobile()) {
                    CoordsPDAInput InScr = new CoordsPDAInput(TransformCoordinates.DMM, true);
                    if (coords.isValid())
                        InScr.setCoords(coords);
                    else
                        InScr.setCoords(new CWPoint(0, 0));
                    if (InScr.execute(null, TOP) == FormBase.IDOK) {
                        dirtyDetails = true;
                        coords = InScr.getCoords();
                        ch.setWpt(coords);
                        coordinates.setText(coords.toString());
                        // If the current centre is valid, calculate the distance and bearing to it
                        final CWPoint centre = Preferences.itself().curCentrePt;
                        if (centre.isValid()) {
                            ch.calcDistance(centre); // todo perhaps sortTable
                        }
                    }
                } else {
                    final CoordsInput cs = new CoordsInput(true);
                    cs.setFields(coords, TransformCoordinates.DMM);
                    if (cs.execute() == FormBase.IDOK) {
                        dirtyDetails = true;
                        coords = cs.getCoords();
                        ch.setWpt(coords);
                        coordinates.setText(coords.toString());
                        // If the current centre is valid, calculate the distance and bearing to it
                        final CWPoint centre = Preferences.itself().curCentrePt;
                        if (centre.isValid()) {
                            ch.calcDistance(centre); // todo perhaps sortTable
                        }
                    }
                }
            } else if (ev.target == btnFoundDate) {
                int msgNr = CacheType.getLogMsgNr(ch.getType());
                // DateChooser.dayFirst=true;
                final DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
                dc.title = MyLocale.getMsg(328, "Date found");
                Preferences.itself().setSubWindowSize(dc);
                String foundDate = chcStatus.getText();
                if (foundDate.startsWith(MyLocale.getMsg(msgNr, "Found") + " ")) {
                    foundDate = foundDate.substring(MyLocale.getMsg(msgNr, "Found").length() + 1);
                } else if (foundDate.endsWith(MyLocale.getMsg(319, "not Found"))) {
                    foundDate = foundDate.substring(0, foundDate.length() - MyLocale.getMsg(319, "not Found").length());
                    dc.title = MyLocale.getMsg(330, "Date Not Found");
                }
                foundDate = foundDate.trim();
                if (foundDate.length() > 0 && foundDate.indexOf('-') > 0) { // Don't try and parse empty date
                    final Time t = new Time();
                    try {
                        t.parse(foundDate, "y-M-d H:m");
                    } catch (IllegalArgumentException e) {
                        try {
                            t.parse(foundDate, "y-M-d");
                        } catch (IllegalArgumentException e1) {
                            Preferences.itself().log("No parsable date given - should not appear (" + foundDate + ")", e1, true);
                        }
                    }

                    dc.reset(t);
                }
                // We can create a not found log with date in two ways:
                // 1) Exiting the date-time dialog by clicking the x if the status is empty (somewhat
                // non-standard but quick and dirty)
                // 2) Exiting the date-time dialog by clicking the tick. Then we check whether
                // the status field was preset with the not-found text. If yes it stays a not found
                // but the date is prepended
                // TODO: The functions for extracting the date and the found/not-found text should not be in the GUI
                int retCode = dc.execute();
                if (retCode == ewe.ui.FormBase.IDOK && !chcStatus.getText().endsWith(MyLocale.getMsg(319, "not Found"))) {
                    chcStatus.setText(MyLocale.getMsg(msgNr, "Found") + " " + Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00") + " " + dc.time);
                    dirtyDetails = true;
                } else if (chcStatus.getText().length() == 0 || (retCode == ewe.ui.FormBase.IDOK && chcStatus.getText().endsWith(MyLocale.getMsg(319, "not Found")))) {
                    chcStatus.setText(dc.year + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00") + " " + dc.time + " " + MyLocale.getMsg(319, "not Found"));
                    dirtyDetails = true;
                }
            } else if (ev.target == hiddenDate) {
                DateChooser.dayFirst = true;
                final DateChooser dc = new DateChooser(Vm.getLocale());
                dc.title = MyLocale.getMsg(329, "Hidden date");
                Preferences.itself().setSubWindowSize(dc);
                if (newHiddenDate.length() == 10)
                    try {
                        dc.setDate(new Time(Convert.parseInt(newHiddenDate.substring(8)), Convert.parseInt(newHiddenDate.substring(5, 7)), Convert.parseInt(newHiddenDate.substring(0, 4))));
                    } catch (NumberFormatException e) {
                        dc.reset(new Time());
                    }
                if (dc.execute() == ewe.ui.FormBase.IDOK) {
                    newHiddenDate = Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00");
                    dirtyDetails = true;
                    // profile.hasUnsavedChanges=true;
                }
            } else if (ev.target == btnLog) {
                String url = "";
                if (mainCache.isCacheWpt()) {
                    CacheHolderDetail chD = mainCache.getDetails();
                    String ownLogMessage = STRreplace.replace(ownLog.getText(), "\n", "<br />");
                    if (chD != null) {
                        if (chD.getOwnLog() != null) {
                            // Cache schon im CW gelogged
                            ownLogMessage = chD.getOwnLog().getMessage();
                        }
                    }
                    if (ownLogMessage.length() > 0) {
                        Vm.setClipboardText(mainCache.getStatus() + '\n' + "<br>" + ownLogMessage);
                    }
                    if (mainCache.isOC()) {
                        if (chD != null) {
                            url = chD.URL;
                            if (url.indexOf("viewcache") >= 0) {
                                url = STRreplace.replace(url, "viewcache", "log");
                            }
                        } else {
                            url = "http://" + OC.getOCHostName(mainCache.getCode()) + "/log.php?wp=" + mainCache.getCode();
                        }
                    } else { // bei GC loggen
                        url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.getCacheID();
                    }

                    if (url.length() > 0) {
                        callExternalProgram(Preferences.itself().browser, url);
                    }

                } else {
                    if (mainCache.isCustomWpt() && mainCache.getCode().startsWith("GC")) {
                        url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.getCacheID();
                        callExternalProgram(Preferences.itself().browser, url);
                    }
                }
            } else if (ev.target == btnLogToOC) {
                // GC und schon gelogged --> log bei OC eintragen
                OCLogExport.doOneLog(mainCache);
                MainTab.itself.tablePanel.refreshTable();
            } else if (ev.target == btnEditLog) {
                String url = "";
                if (mainCache.isCacheWpt()) {
                    CacheHolderDetail chD = mainCache.getDetails();
                    if (chD != null) {
                        if (chD.getOwnLog() != null) {
                            if (mainCache.isGC()) {
                                url = "http://www.geocaching.com/seek/log.aspx?LID=" + chD.getOwnLog().getLogID();
                                callExternalProgram(Preferences.itself().browser, url);
                            }
                        }
                    }
                }
            } else if (ev.target == this.btnHint) {
                new InfoBox(MyLocale.getMsg(402, "Hint"), hint).wait(FormBase.OKB);
            }
        }
    }

    private void createWaypoint() {
        final CacheHolder newCache = new CacheHolder();
        newCache.setWpt(ch.getWpt());
        newCache.setType(CacheType.CW_TYPE_STAGE);
        newCache.setDifficulty(CacheTerrDiff.CW_DT_UNSET);
        newCache.setTerrain(CacheTerrDiff.CW_DT_UNSET);
        newCache.setSize(CacheSize.CW_SIZE_NOTCHOSEN);
        MainTab.itself.newWaypoint(newCache);
    }

    private void callExternalProgram(String program, String parameter) {
        try {
            CWWrapper.exec(program, parameter);
        } catch (IOException ex) {
            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1034, "Cannot start " + program + "!") + "\n" + ex.toString() + "\n" + MyLocale.getMsg(1035, "Possible reason:") + "\n" + MyLocale.getMsg(1036, "A bug in ewe VM, please be")
                    + "\n" + MyLocale.getMsg(1037, "patient for an update")).wait(FormBase.OKB);
        }
    }

    private void enable(final Control ctrl) {
        if (ctrl.change(0, ControlConstants.Disabled))
            ctrl.repaint();
    }

    private void disable(final Control ctrl) {
        if (ctrl.change(ControlConstants.Disabled, 0))
            ctrl.repaint();
    }

    public void saveDirtyWaypoint() {
        // we have a mix of setting dirty flag (most values) and directly changing the cache values (notes,...).
        // that doesn't matter as long as we have no revert.
        // the dirty flag indicates the global change (which is not necessary for the directly changed values).
        // so each value will be set by the setter, that checks for a change and sets the flag for writing to disk.
        //
        // We have to update two objects:
        // thisCache (a CacheHolderDetail) which contains the full cache which will be written to the cache.xml file
        // AND
        // the CacheHolder object which sits in cacheDB

        int msgNr = CacheType.getLogMsgNr(ch.getType());

        // Strip the found message if the status contains a date
        if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) && chcStatus.getText().length() >= MyLocale.getMsg(msgNr, "Found").length() + 11) {
            ch.setStatus(chcStatus.getText().substring(MyLocale.getMsg(msgNr, "Found").length() + 1));
        } else {
            ch.setStatus(chcStatus.getText());
        }

        if (ch.isSolved() != this.cbIsSolved.getState())
            ch.setIsSolved(this.cbIsSolved.getState());

        if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) || (ch.getStatus().length() == 10 || ch.getStatus().length() == 16) && ch.getStatus().charAt(4) == '-') {
            // Use same heuristic condition as in setDetails(CacheHolder) to determine, if this cache has to considered as found.
            ch.setFound(true);
        } else
            ch.setFound(false);

        if (!ch.isAddiWpt()) {
            ch.setOwner(owner.getText().trim());
        }
        // special
        if (ch.getStatus().equals(MyLocale.getMsg(320, "Owner"))) {
            ch.setOwned(true);
        }
        ch.setBlack(blackStatus);

        final String oldWaypoint = ch.getCode();
        ch.setCode(this.newWaypoint);
        if (!ch.isAddiWpt()) {
            ch.setSize(newCacheSize);
        }
        // If the waypoint does not have a name, give it one
        if (ch.getCode().equals("")) {
            ch.setCode(MainForm.profile.getNewWayPointName("CW"));
        }
        // Don't allow single letter names=> Problems in updateBearingDistance
        // This is a hack but faster than slowing down the loop in
        // updateBearingDistance
        if (ch.getCode().length() < 2)
            ch.setCode(ch.getCode() + " ");

        ch.setName(alias.getText().trim());
        if (!ch.isAddiWpt()) {
            ch.setHidden(newHiddenDate.trim());
        }

        final byte oldType = ch.getType();
        ch.setType(newCacheType);

        String ownLogText = STRreplace.replace(ownLog.getText(), "\n", "<br />");
        Log oldLog = ch.getDetails().getOwnLog();
        if (oldLog == null) {
            if (ownLogText.length() > 0) {
                // todo must get date from status
                // todo must set icon to correct value
                ch.getDetails().setOwnLog(new Log("", Preferences.itself().gcMemberId, "2.png", "1900-01-01", Preferences.itself().myAlias, ownLogText));
            }
        } else {
            if (ownLogText.length() > 0)
                oldLog.setMessage(ownLogText);
            else {
                oldLog.setLogID("");
                oldLog.setMessage("");
            }
        }

        if (ch.getName().length() == 0) {
            ch.setName(ch.getCode());
        }
        ch.checkIncomplete();

	/*
     * The references have to be rebuilt if:
	 *  the cachetype changed from addi->normal or from normal->addi
	 *  or the old or new cachetype is 'addi' and the waypointname has changed
	 */
        if (CacheType.isAddiWpt(newCacheType) != CacheType.isAddiWpt(oldType) //
                || ((CacheType.isAddiWpt(ch.getType()) || CacheType.isAddiWpt(oldType)) && !ch.getCode().equals(oldWaypoint)) //
                ) {
            // If we changed the type to addi, check that a parent exists
            // FIXME: if cache was renamed we need to rebuild CacheDB.hashDB first
            MainForm.profile.buildReferences();
        } else {
            // set status also on addi wpts
            ch.setAttributesFromMainCacheToAddiWpts();
        }
        if (!ch.isAddiWpt()) {
            ch.setDifficulty(this.newDifficulty);
            ch.setTerrain(this.newTerrain);
        }

        if (isBigScreen) {
            ch.getDetails().setCacheNotes(waypointNotes.getText());
        }
        dirtyDetails = false;
        needsTableUpdate = false;
        ch.getDetails().hasUnsavedChanges = true;
        if (!oldWaypoint.equals(ch.getCode())) {
            // Delete old XML - File
            ch.getDetails().deleteFile(MainForm.profile.dataDir + oldWaypoint + ".xml");
        }
    }

    private class TravelbugInCacheScreen extends Form {

        private final DispPanel disp = new DispPanel();
        private final mButton btCancel;

        TravelbugInCacheScreen(String text, String title) {
            super();
            this.setTitle(title);
            Preferences.itself().setBigWindowSize(this);
            disp.setHtml(text);
            final ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
            this.addLast(sbp);
            this.addLast(btCancel = new mButton(MyLocale.getMsg(3000, "Close")), DONTSTRETCH, FILL);
        }

        public void onEvent(Event ev) {
            if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
                if (ev.target == btCancel) {
                    this.close(0);
                }
            }
        }

        // Subclassed HtmlDisplay with Pop-up menu
        private class DispPanel extends HtmlDisplay {
            private final MenuItem mnuPickupTB, mnuDropTB;
            private final MenuItem[] tbMenuItems = new MenuItem[2];
            private final Menu mnuPopup;

            DispPanel() {
                super();
                tbMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(6016, "Pick up Travelbug"));
                tbMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6017, "Drop Travelbug"));
                mnuPopup = new Menu(tbMenuItems, "");
            }

            public void penRightReleased(Point p) {
                setMenu(mnuPopup);
                doShowMenu(p); // direct call (not through doMenu) is neccesary because it will exclude the whole table
            }

            public void penHeld(Point p) {
                setMenu(mnuPopup);
                doShowMenu(p);
            }

            public void popupMenuEvent(Object selectedItem) {
                if (selectedItem.equals(mnuPickupTB)) {
                    final Travelbug tb = TravelbugPickup.pickupTravelbug(getCache().getDetails().Travelbugs);
                    TravelbugJourneyList tbjList;
                    if (tb != null) {
                        dirtyDetails = true;
                        // Get the list of my travelbugs
                        tbjList = new TravelbugJourneyList();
                        tbjList.readTravelbugsFile();
                        // Add the tb to this list
                        tbjList.addTbPickup(tb, MainForm.profile.name, getCache().getCode());
                        tbjList.saveTravelbugsFile();
                        setHtml(getCache().getDetails().Travelbugs.toHtml());
                        repaint();
                        getCache().hasBugs(getCache().getDetails().Travelbugs.size() > 0);
                    }
                } else if (selectedItem.equals(mnuDropTB)) {
                    TravelbugJourneyList tbjList;
                    tbjList = new TravelbugJourneyList();
                    tbjList.readTravelbugsFile();
                    TravelbugList tbl = tbjList.getMyTravelbugs();
                    TravelbugScreen tbs = new TravelbugScreen(tbl, MyLocale.getMsg(6017, "Drop a travelbug"), false);
                    tbs.execute();
                    if (tbs.selectedItem >= 0) {
                        Travelbug tb = tbl.getTB(tbs.selectedItem);
                        getCache().getDetails().Travelbugs.add(tb);
                        tbjList.addTbDrop(tb, MainForm.profile.name, getCache().getCode());
                    }
                    tbjList.saveTravelbugsFile();
                    getCache().hasBugs(getCache().getDetails().Travelbugs.size() > 0);
                    setHtml(getCache().getDetails().Travelbugs.toHtml());
                    repaint();
                    dirtyDetails = true;
                } else {
                    super.popupMenuEvent(selectedItem);
                }
            }
        }
    }
}

class MyChoice extends Menu {
    private mButton btn;
    private Menu mnu;
    private MenuItem miList[];
    private String[] selectionList;
    private String[] iconList;
    private int currentIndex;

    public MyChoice(String btnText, String[] _selectionList) {
        btn = new mButton(btnText);
        setSelectionList(_selectionList, null);
    }

    public MyChoice(String btnText, String icon, String[] _selectionList) {
        btn = GuiImageBroker.getButton(btnText, icon);
        setSelectionList(_selectionList, null);
    }

    public MyChoice(String btnText, String icon, String[] _selectionList, String[] _iconList) {
        btn = GuiImageBroker.getButton(btnText, icon);
        setSelectionList(_selectionList, _iconList);
    }

    public void setSelectionList(String[] _selectionList, String[] _iconList) {
        selectionList = _selectionList;
        iconList = _iconList;
        miList = new MenuItem[selectionList.length];
        this.keepFrame = false;
        this.items.clear();
        for (int i = 0; i < selectionList.length; i++) {
            if (iconList == null) {
                this.addItem(miList[i] = new MenuItem(selectionList[i]));
            } else {
                if (iconList[i].length() > 0)
                    this.addItem(miList[i] = GuiImageBroker.getMenuItem(selectionList[i], iconList[i]));
                else
                    this.addItem(miList[i] = new MenuItem(selectionList[i]));
            }
        }
        btn.setMenu(this);
        currentIndex = -1;
        btn.modifyAll(ControlConstants.WantHoldDown, 0);
        mnu = this;
    }

    public mButton getBtn() {
        return btn;
    }

    public Menu getMnu() {
        return mnu;
    }

    public boolean mark(int newIndex) {
        if (newIndex == currentIndex) {
            return false;
        } else {
            if (currentIndex > -1)
                miList[currentIndex].modifiers &= ~MenuItem.Checked;
            miList[newIndex].modifiers |= MenuItem.Checked;
            currentIndex = newIndex;
            return true;
        }
    }

    public void enable(int index) {
        this.miList[index].modifiers &= ~MenuItem.Disabled;
    }

    public void disable(int index) {
        this.miList[index].modifiers |= MenuItem.Disabled;
    }

    public void setText(int index, String text) {
        selectionList[index] = text;
        this.items.clear();
        for (int i = 0; i < selectionList.length; i++) {
            if (i != index) {
                this.addItem(miList[i]);
            } else {
                if (iconList == null) {
                    this.addItem(miList[i] = new MenuItem(selectionList[i]));
                } else {
                    if (iconList[i].length() > 0)
                        this.addItem(miList[i] = GuiImageBroker.getMenuItem(selectionList[i], iconList[i]));
                    else
                        this.addItem(miList[i] = new MenuItem(selectionList[i]));
                }
            }
        }
        miList[index].modifiers |= MenuItem.Checked;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedValue() {
        return selectionList[selectedIndex];
    }

    public void showMenu() {
        Rect startPos = mnu.getRect();
        btn.startDropMenu(new Point(startPos.width, 0));
    }

}

class AttributesViewer extends CellPanel {
    protected final int TILESIZE = Attribute.getImageWidth() + 2;
    protected final int MAX_ICONS_PER_ROW = Preferences.itself().getScreenWidth() / (TILESIZE + 6);
    protected final int MAX_ROWS = 1 + Attributes.MAXATTRIBS / MAX_ICONS_PER_ROW;
    protected InteractivePanel iap;
    protected mLabel mInfo;

    public AttributesViewer() {
        iap = new attInteractivePanel();
        mInfo = new mLabel("");
        iap.setPreferredSize(10, TILESIZE * MAX_ROWS);
        addLast(iap, HSTRETCH, HFILL);
        addLast(mInfo, HSTRETCH, HFILL);
        setBorder(EDGE_BUMP, 5);
    }

    public void showImages(Attributes atts) {
        iap.images.clear();
        for (int i = 0; i < atts.count(); i++) {
            attAniImage img = new attAniImage(atts.getAttribute(i).getImage());
            img.info = atts.getAttribute(i).getMsg();
            img.location = new Rect((i % MAX_ICONS_PER_ROW) * TILESIZE, (i / MAX_ICONS_PER_ROW) * TILESIZE, TILESIZE, TILESIZE);
            iap.addImage(img);
        }
        iap.repaint();
    }

    public void clear() {
        iap.images.clear();
    }

    protected class attInteractivePanel extends InteractivePanel {
        //Overrides: imageMovedOn(...) in InteractivePanel
        public boolean imageMovedOn(AniImage which) {
            if (!((attAniImage) which).info.startsWith("*")) { // If text starts with * we have no explanation yet
                mInfo.setText(((attAniImage) which).info);
                mInfo.repaintNow();
            }
            return true;
        }

        //Overrides: imageMovedOff(...) in InteractivePanel
        public boolean imageMovedOff(AniImage which) {
            mInfo.setText("");
            mInfo.repaintNow();
            return true;
        }
    }

    protected class attAniImage extends AniImage {
        public String info;

        attAniImage(mImage img) {
            super(img);
        }
    }
}

class LastLogsPanel extends MosaicPanel {
    Hashtable reference = new Hashtable(5);
    int breite, hoehe;

    public LastLogsPanel(int _breite, int _hoehe) {
        breite = _breite;
        hoehe = _hoehe;
    }

    public void addImage(AniImage im, Log log) {
        reference.put(im, log);
        addImage(im);
    }

    //Overrides: imagePressed(...) in MosaicPanel
    public boolean imagePressed(AniImage which, Point pos) {
        if (which != null) {
            if (reference.containsKey(which)) {
                HtmlDisplay log = new HtmlDisplay();
                log.startHtml();
                log.addHtml(((Log) reference.get(which)).toHtml(), new ewe.sys.Handle());
                log.endHtml();
                log.scrollTo(0, false);
                new InfoBox(MyLocale.getMsg(403, "Log"), log, breite, hoehe).wait(FormBase.OKB);
            }
        }
        return true;
    }
}
