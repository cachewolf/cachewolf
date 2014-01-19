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
import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.Attribute;
import CacheWolf.database.Attributes;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugJourneyList;
import CacheWolf.database.TravelbugList;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import ewe.fx.Dimension;
import ewe.fx.IImage;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.io.File;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.DateChooser;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.HtmlDisplay;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTextPad;

/**
 * Class to create the panel to show the way point details.<br>
 * Also allows for creation and editing of way points
 */
public class DetailsPanel extends CellPanel {

    // ===== GUI elements =====
    /** way point id. */
    private static mInput inpWaypoint;
    /** way point name. */
    private static mInput inpName;
    /** way point hidden date. */
    private static mInput inpHidden;
    /** way point owner. */
    private static mInput inpOwner;
    /** way point coordinates, open change coordinates dialog. */
    private static mButton btnCoordinates;

    /** create a new way point. */
    private static mButton btnNewWpt;
    /** set this waypoint as destination and change to compass view */
    private static mButton btnSetDestination;
    /** show details for travel bus in way point. */
    private static mButton btnTBs;
    private static IImage showBugImage;
    private static IImage showBugNoImage;
    /** toggle blacklist status. */
    private static mButton btnBlackListed;
    private static IImage btnImageBlackListed;
    private static IImage btnImageNotBlackListed;
    /** add or edit notes for way point. */
    private static mButton btnNotes;

    /** set found date. */
    private static mButton btnFoundDate;
    /** set hidden date. */
    private static mButton btnHiddenDate;
    /** set terrain value. */
    private static mButton btnTerr;
    /** set difficulty value. */
    private static mButton btnDiff;
    /** drop down list with cache types. */
    private static mChoice chcType;
    /** drop down list with container sizes. */
    private static mChoice chcSize;
    /** select way point status. */
    private static mComboBox chcStatus;
    /** notes for way point. */
    private static mTextPad waypointNotes;
    /** shows number of additional way points. */
    private static mLabel lblAddiCount;

    // ===== data handles =====
    /** waypoint to be displayed. */
    public CacheHolder cache;
    /** panel to display waypoint attributes */
    private static AttributesViewer attViewer;

    // ===== flags =====
    /** notes have changes */
    private boolean dirtyNotes;
    /** details have changed FIXME: make this obsolete */
    private boolean dirtyDetails;
    /** cache is blacklisted FIXME: make this obsolete */
    private boolean blackStatus;
    /** blacklist status was changed by user FIXME: make this obsolete */
    private boolean blackStatusChanged;
    /** FIXME */
    private boolean needsTableUpdate;
    /** String to display for invalid or not applicable terrain or difficulty values. */
    private final static String DTINVALID = ": -.-";
    public boolean evWaypointChanged = false;
    private String warnedForWaypoint = "";

    // TODO: move images to image broker
    // mImage imgBlack, imgBlackNo, imgShowBug, imgShowBugNo, imgNewWpt, imgGoto, imgShowMaps, imgAddImages, imgNotes;

    /**
     * public constructor for detail panels. should only be called from main tab.
     */
    public DetailsPanel() {
	super();
	// ===== local objects =====
	/** helper panels to organize layout */
	CellPanel helperPanel1, helperPanel2, helperPanel3, helperPanel4, helperPanel5, leftPanel, helperPanel6;

	// ===== initialize flags =====
	dirtyNotes = false;
	dirtyDetails = false;
	blackStatus = false;
	blackStatusChanged = false;
	needsTableUpdate = false;

	// ===== initialize GUI objects =====
	// ----- main body -----

	helperPanel1 = new CellPanel();
	helperPanel2 = new CellPanel();
	helperPanel3 = new CellPanel();
	helperPanel4 = new CellPanel();
	helperPanel5 = new CellPanel();
	leftPanel = new CellPanel();
	helperPanel6 = new CellPanel();
	attViewer = new AttributesViewer();

	chcType = new mChoice(CacheType.guiTypeStrings(), 0);
	chcType.alwaysDrop = true;
	chcSize = new mChoice(CacheSize.guiSizeStrings(), 0);
	chcSize.alwaysDrop = true;
	chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(), 0);
	inpWaypoint = new mInput();
	inpName = new mInput();

	btnCoordinates = new mButton();
	btnCoordinates.setToolTip(MyLocale.getMsg(31415, "Edit coordinates"));

	btnSetDestination = GuiImageBroker.getButton(MyLocale.getMsg(1500, "Destination"), "goto"); //345, "Go to these coordinates"
	btnSetDestination.setToolTip(MyLocale.getMsg(326, "Set as destination and show Compass View"));

	inpOwner = new mInput();
	inpHidden = new mInput();
	inpHidden.modifyAll(DisplayOnly, 0);

	// ===== put the controls onto the GUI =====

	// ----- helper panels -----

	btnDiff = new mButton(MyLocale.getMsg(1000, "D") + ": 5.5");
	btnDiff.setPreferredSize(Preferences.itself().fontSize * 3, chcSize.getPreferredSize(null).height);
	btnDiff.setToolTip(MyLocale.getMsg(31415, "Edit difficulty"));

	btnTerr = new mButton(MyLocale.getMsg(1001, "T") + ": 5.5");
	btnTerr.setPreferredSize(Preferences.itself().fontSize * 3, chcSize.getPreferredSize(null).height);
	btnTerr.setToolTip(MyLocale.getMsg(31415, "Edit terrain"));

	btnNewWpt = GuiImageBroker.getButton(MyLocale.getMsg(733, "Addi Wpt"), "newwpt");
	btnNewWpt.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));
	lblAddiCount = new mLabel(": 888"); //MyLocale.getMsg(1044, "Addis") + 

	btnFoundDate = GuiImageBroker.getButton("", "calendar");
	btnFoundDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));

	btnHiddenDate = GuiImageBroker.getButton("", "calendar");
	btnHiddenDate.setToolTip(MyLocale.getMsg(31415, "Set hidden date"));

	if (!Preferences.itself().tabsAtTop)
	    addLast(createToolsPanel(), HSTRETCH, HFILL);//.setTag(SPAN, new Dimension(3, 1)); //, DONTSTRETCH, FILL

	helperPanel1.addNext(chcType, HSTRETCH, (HFILL | RIGHT));
	helperPanel1.addLast(btnDiff, DONTSTRETCH, (DONTFILL | EAST));

	helperPanel2.addNext(chcSize, HSTRETCH, (HFILL | RIGHT));
	helperPanel2.addLast(btnTerr, DONTSTRETCH, (DONTFILL | EAST));

	helperPanel3.addNext(inpWaypoint, HSTRETCH, (HFILL | RIGHT));
	helperPanel3.addNext(btnNewWpt, DONTSTRETCH, DONTFILL);
	helperPanel3.addLast(lblAddiCount, DONTSTRETCH, RIGHT);

	helperPanel4.addNext(chcStatus, HSTRETCH, (HFILL | RIGHT));
	helperPanel4.addLast(btnFoundDate, DONTSTRETCH, DONTFILL);

	helperPanel5.addNext(inpHidden, HSTRETCH, (HFILL | RIGHT));
	helperPanel5.addLast(btnHiddenDate, DONTSTRETCH, DONTFILL);

	helperPanel6.addNext(btnCoordinates, HSTRETCH, HFILL);
	helperPanel6.addLast(btnSetDestination, DONTSTRETCH, RIGHT);

	// ----- main body -----

	leftPanel.addNext(new mLabel(MyLocale.getMsg(300, "Type:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel1, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(301, "Size:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel2, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(302, "Waypoint:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel3, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(303, "Name:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(inpName, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(304, "Coordinates:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel6, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(307, "Status:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel4, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(306, "Owner:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(inpOwner, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	leftPanel.addNext(new mLabel(MyLocale.getMsg(305, "Hidden on:")), DONTSTRETCH, (DONTFILL | LEFT));
	leftPanel.addLast(helperPanel5, HSTRETCH, HFILL).setTag(SPAN, new Dimension(2, 1));

	addLast(leftPanel, HSTRETCH, HFILL);
	// addLast(rightPanel, HSTRETCH, HFILL | LEFT);

	addLast(attViewer);

	if (Preferences.itself().isBigScreen) {
	    waypointNotes = new mTextPad();
	    waypointNotes.modify(ControlConstants.NotEditable, 0);
	    addLast(new MyScrollBarPanel(waypointNotes), STRETCH, FILL);
	}
	if (Preferences.itself().tabsAtTop)
	    addLast(createToolsPanel(), HSTRETCH, HFILL);// .setTag(SPAN, new Dimension(3, 1)); //, DONTSTRETCH, FILL

    }

    private CellPanel createToolsPanel() {

	final CellPanel pnlTools = new CellPanel();

	btnTBs = GuiImageBroker.getButton(MyLocale.getMsg(353, "TBs"), "bug");
	showBugImage = GuiImageBroker.makeImageForButton(btnTBs, MyLocale.getMsg(353, "TBs"), "bug");
	showBugNoImage = GuiImageBroker.makeImageForButton(btnTBs, MyLocale.getMsg(353, "TBs"), "bug_no");
	btnTBs.setToolTip(MyLocale.getMsg(346, "Show travelbugs"));

	btnBlackListed = GuiImageBroker.getButton(MyLocale.getMsg(363, "Blacklist"), "is_black");
	btnImageNotBlackListed = GuiImageBroker.makeImageForButton(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), "no_black");
	btnImageBlackListed = GuiImageBroker.makeImageForButton(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), "is_black");
	btnBlackListed.setToolTip(MyLocale.getMsg(349, "Toggle Blacklist status"));

	btnNotes = GuiImageBroker.getButton(MyLocale.getMsg(308, "Notes"), "notes");
	btnNotes.setToolTip(MyLocale.getMsg(351, "Add/Edit notes"));

	pnlTools.equalWidths = true;
	pnlTools.addNext(btnNotes);
	pnlTools.addNext(btnTBs);
	pnlTools.addLast(btnBlackListed);

	return pnlTools;
    }

    public void clear() {
	attViewer.clear();
    }

    public boolean getNeedsTableUpdate() {
	return needsTableUpdate;
    }

    public boolean isDirty() {
	return dirtyNotes || dirtyDetails || needsTableUpdate;
    }

    public boolean hasBlackStatusChanged() {
	return blackStatusChanged;
    }

    public void setDetails(final CacheHolder ch, boolean isNew) {
	needsTableUpdate = isNew;
	cache = ch;
	CacheHolder mainCache = ch;
	if (ch.isAddiWpt() && (ch.mainCache != null)) {
	    mainCache = ch.mainCache;
	}
	dirtyNotes = false;
	dirtyDetails = false;
	inpWaypoint.setText(ch.getWayPoint());
	inpName.setText(ch.getCacheName());
	btnCoordinates.setText(ch.getPos().toString());
	inpHidden.setText(mainCache.getDateHidden());
	inpOwner.setText(mainCache.getCacheOwner());
	chcStatus.setText(ch.getStatusText());
	chcType.setInt(CacheType.cw2GuiSelect(ch.getType()));

	if (ch.is_black()) {
	    GuiImageBroker.setButtonIconAndText(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), btnImageBlackListed);
	} else {
	    GuiImageBroker.setButtonIconAndText(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), btnImageNotBlackListed);
	}

	blackStatus = ch.is_black();
	blackStatusChanged = false;

	if (inpWaypoint.getText().length() == 0) {
	    createWptName();
	}

	if (mainCache.has_bugs()) {
	    // btnTBs.modify(Control.Disabled,1);
	    GuiImageBroker.setButtonIconAndText(btnTBs, MyLocale.getMsg(353, "TBs"), showBugImage);
	} else {
	    // btnTBs.modify(Control.Disabled,0);
	    GuiImageBroker.setButtonIconAndText(btnTBs, MyLocale.getMsg(353, "TBs"), showBugNoImage);
	}

	chcSize.setInt(mainCache.getCacheSize());

	attViewer.showImages(ch.getCacheDetails(true).attributes);
	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    deactivateControl(btnTerr);
	    deactivateControl(btnDiff);
	    deactivateControl(chcSize);
	    deactivateControl(inpOwner);
	    deactivateControl(inpHidden);
	    deactivateControl(btnTBs);
	    deactivateControl(btnBlackListed);
	} else {
	    activateControl(btnTerr);
	    activateControl(btnDiff);
	    activateControl(chcSize);
	    activateControl(inpOwner);
	    activateControl(inpHidden);
	    activateControl(btnTBs);
	    activateControl(btnBlackListed);
	}

	if (ch.isCustomWpt()) {
	    btnTerr.setText(MyLocale.getMsg(1001, "T") + DTINVALID);
	    btnDiff.setText(MyLocale.getMsg(1000, "D") + DTINVALID);
	    chcSize.select(0);
	} else {
	    if (CacheTerrDiff.isValidTD(mainCache.getTerrain())) {
		btnTerr.setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(mainCache.getTerrain()));
	    } else {
		btnTerr.setText("T: -.-");
		mainCache.setIncomplete(true);
		Preferences.itself().log(mainCache.getWayPoint() + " has wrong terrain " + mainCache.getTerrain());
	    }
	    if (CacheTerrDiff.isValidTD(mainCache.getHard())) {
		btnDiff.setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(mainCache.getHard()));
	    } else {
		btnDiff.setText("D: -.-");
		mainCache.setIncomplete(true);
		Preferences.itself().log(mainCache.getWayPoint() + " has wrong difficulty " + mainCache.getHard());
	    }
	}

	int addiCount = 0;
	if (ch.mainCache == null) {
	    addiCount = ch.addiWpts.size();
	} else {
	    addiCount = ch.mainCache.addiWpts.size();
	}
	lblAddiCount.setText(": " + addiCount); //MyLocale.getMsg(1044, "Addis") + 

	if (Preferences.itself().isBigScreen) {
	    waypointNotes.setText(ch.getCacheDetails(true).getCacheNotes());
	}
    }

    /**
     * if is addi -> returns the respective AddiWpt if is main -> returns the respective MainWpt
     */
    public void createWptName() {
	final String wpt = inpWaypoint.getText().toUpperCase();
	if (CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt())) //
		&& MainTab.itself.mainCache != null //
		&& (MainTab.itself.mainCache.startsWith("GC") //
			|| OC.isOC(MainTab.itself.mainCache) //
		|| MainTab.itself.mainCache.startsWith("CW")) && wpt.startsWith("CW")) {
	    // for creating the Addiname on creating a new Waypoint
	    MainTab.itself.lastselected = MainTab.itself.mainCache;

	    inpWaypoint.setText(MainForm.profile.getNewAddiWayPointName(MainTab.itself.mainCache));
	}
	if (!CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt())) && !(wpt.startsWith("GC") || OC.isOC(wpt) || wpt.startsWith("CW"))) {
	    inpWaypoint.setText(MainForm.profile.getNewWayPointName("CW"));
	}
    }

    /**
     * Method to react to a user input.
     */
    public void onEvent(final Event ev) {
	if (ev instanceof DataChangeEvent) {
	    if (ev.target == inpWaypoint) {
		if (evWaypointChanged) {
		    String iTmp = inpWaypoint.getText();
		    String uTmp = iTmp.toUpperCase();
		    if (!iTmp.equals(uTmp)) {
			inpWaypoint.setText(uTmp); // If user entered LowerCase -> convert directly to UpperCase
			evWaypointChanged = false; // next DataChangeEvent fired by change to UpperCase will be ignored
		    }
		    // already warned(multi same DataChangeEvents) or same waypointname as before edit !!!
		    if (!warnedForWaypoint.equals(uTmp) && !uTmp.equals(this.cache.getWayPoint())) {
			if ((new File(MainForm.profile.dataDir + iTmp.toLowerCase() + ".xml")).exists()) {
			    warnedForWaypoint = uTmp;
			    // filename is LowerCase
			    new InfoBox("Warning :", uTmp + "\n" + MyLocale.getMsg(275, "Waypoint already exists!")).wait(FormBase.OKB);
			    // revert waypointname
			    inpWaypoint.setText(this.cache.getWayPoint());
			}
		    }
		} else {
		    // first DataChangeEvent is fired by Klick into (after reload).
		    // that really didn't change anything
		    evWaypointChanged = true;
		}
		// FIXME: if name was changed, we should rename the waypoint.xml file. how? where?
	    } else if (ev.target == chcType) {
		createWptName();
		if (CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.selectedIndex))) {
		    activateControl(btnTerr);
		    activateControl(btnDiff);
		    activateControl(chcSize);
		    activateControl(inpOwner);
		    activateControl(inpHidden);
		    activateControl(btnTBs);
		    activateControl(btnBlackListed);
		} else {
		    deactivateControl(btnTerr);
		    deactivateControl(btnDiff);
		    deactivateControl(chcSize);
		    deactivateControl(inpOwner);
		    deactivateControl(inpHidden);
		    deactivateControl(btnTBs);
		    deactivateControl(btnBlackListed);
		    chcSize.select(0);
		    btnTerr.setText(MyLocale.getMsg(1001, "T") + DTINVALID);
		    btnDiff.setText(MyLocale.getMsg(1000, "D") + DTINVALID);
		}
	    }
	    // FIXME: check if something was actually changed, since datacachnge events also occur if you just hop through the fields with the tab key (Why? don't know!)
	    dirtyDetails = true;
	    needsTableUpdate = true;
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == btnNotes) {
		dirtyNotes = true; // TODO I think this is redundant, because
		// the notes are saved separately by the notes screen itself
		final NotesScreen nsc = new NotesScreen(cache.getCacheDetails(true));
		nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
		if (Preferences.itself().isBigScreen) {
		    waypointNotes.setText(cache.getCacheDetails(true).getCacheNotes());
		}
	    } else if (ev.target == btnTBs) {
		final TravelbugInCacheScreen ts = new TravelbugInCacheScreen(cache.getCacheDetails(true).Travelbugs.toHtml(), "Travelbugs");
		ts.execute(this.getFrame(), Gui.CENTER_FRAME);

	    } else if (ev.target == btnBlackListed) {
		if (cache.is_black()) {
		    cache.setBlack(false);
		    GuiImageBroker.setButtonIconAndText(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), btnImageNotBlackListed);
		} else {
		    cache.setBlack(true);
		    GuiImageBroker.setButtonIconAndText(btnBlackListed, MyLocale.getMsg(363, "Blacklist"), btnImageBlackListed);
		}
		blackStatus = cache.is_black();
		cache.setAttributesToAddiWpts();
		dirtyDetails = true;
		blackStatusChanged = true;
	    } else if (ev.target == btnNewWpt) {
		final CacheHolder ch = new CacheHolder();
		ch.setPos(cache.getPos());
		ch.setType(CacheType.CW_TYPE_STAGE);
		ch.setHard(CacheTerrDiff.CW_DT_UNSET);
		ch.setTerrain(CacheTerrDiff.CW_DT_UNSET);
		ch.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
		MainTab.itself.newWaypoint(ch);
	    }

	    else if (ev.target == btnSetDestination) {
		if (cache.getPos().isValid()) {
		    Navigate.itself.setDestination(cache);
		    MainTab.itself.select(MainTab.GOTO_CARD);
		}
	    }

	    else if (ev.target == btnCoordinates) {
		CWPoint coords = new CWPoint(btnCoordinates.getText(), TransformCoordinates.CW);
		if (Vm.isMobile()) {
		    CoordsPDAInput InScr = new CoordsPDAInput(TransformCoordinates.CW, true);
		    if (coords.isValid())
			InScr.setCoords(coords);
		    else
			InScr.setCoords(new CWPoint(0, 0));
		    if (InScr.execute(null, TOP) == FormBase.IDOK) {
			dirtyDetails = true;
			coords = InScr.getCoords();
			cache.setPos(coords);
			btnCoordinates.setText(coords.toString());
			// If the current centre is valid, calculate the distance and bearing to it
			final CWPoint centre = Preferences.itself().curCentrePt;
			if (centre.isValid()) {
			    cache.calcDistance(centre); // todo perhaps sortTable
			}
		    }
		} else {
		    final CoordsInput cs = new CoordsInput(true);
		    cs.setFields(coords, TransformCoordinates.CW);
		    if (cs.execute() == FormBase.IDOK) {
			dirtyDetails = true;
			coords = cs.getCoords();
			cache.setPos(coords);
			btnCoordinates.setText(coords.toString());
			// If the current centre is valid, calculate the distance and bearing to it
			final CWPoint centre = Preferences.itself().curCentrePt;
			if (centre.isValid()) {
			    cache.calcDistance(centre); // todo perhaps sortTable
			}
		    }
		}
	    } else if (ev.target == btnFoundDate) {
		int msgNr = cache.getLogMsgNr();
		// DateChooser.dayFirst=true;
		final DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
		dc.title = MyLocale.getMsg(328, "Date found");
		dc.setPreferredSize(240, 240);
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
		    chcStatus.setText(Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00") + " " + dc.time + " " + MyLocale.getMsg(319, "not Found"));
		    dirtyDetails = true;
		}
	    } else if (ev.target == btnHiddenDate) {
		DateChooser.dayFirst = true;
		final DateChooser dc = new DateChooser(Vm.getLocale());
		dc.title = MyLocale.getMsg(329, "Hidden date");
		dc.setPreferredSize(240, 240);
		if (inpHidden.getText().length() == 10)
		    try {
			dc.setDate(new Time(Convert.parseInt(inpHidden.getText().substring(8)), Convert.parseInt(inpHidden.getText().substring(5, 7)), Convert.parseInt(inpHidden.getText().substring(0, 4))));
		    } catch (NumberFormatException e) {
			dc.reset(new Time());
		    }
		if (dc.execute() == ewe.ui.FormBase.IDOK) {
		    inpHidden.setText(Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00"));
		    dirtyDetails = true;
		    // profile.hasUnsavedChanges=true;
		}
	    } else if (ev.target == btnTerr) {
		int returnValue;
		final TerrDiffForm tdf = new TerrDiffForm(true, decodeTerrDiff(btnTerr, MyLocale.getMsg(1001, "T"), CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))));
		returnValue = tdf.execute();
		if (returnValue == 1) {
		    btnTerr.setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(tdf.getDT()));
		    dirtyDetails = true;
		}
	    } else if (ev.target == btnDiff) {
		int returnValue;
		final TerrDiffForm tdf = new TerrDiffForm(false, decodeTerrDiff(btnDiff, MyLocale.getMsg(1001, "D"), CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))));

		returnValue = tdf.execute();
		if (returnValue == 1) {
		    btnDiff.setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(tdf.getDT()));
		    dirtyDetails = true;
		}
	    }
	    ev.consumed = true;
	}
    }

    /** allow user input on control item */
    private void activateControl(final Control ctrl) {
	if (ctrl.change(0, ControlConstants.Disabled))
	    ctrl.repaint();
    }

    /** block user input on control item */
    private void deactivateControl(final Control ctrl) {
	if (ctrl.change(ControlConstants.Disabled, 0))
	    ctrl.repaint();
    }

    public void saveDirtyWaypoint() {
	// FIXME: here we should check if the data is now different from what it used to be when calling the details panel instead of relying on dirty flags
	// FIXME: take care of renaming waypoints
	// FIXME: add method to convert back text of difficulty & terrain buttons
	// FIXME: check if manual changes have converted a cache from incomplete to complete

	// We have to update two objects: thisCache (a CacheHolderDetail) which contains
	// the full cache which will be written to the cache.xml file AND
	// the CacheHolder object which sits in cacheDB

	int msgNr = cache.getLogMsgNr();

	// Strip the found message if the status contains a date
	if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) && chcStatus.getText().length() >= MyLocale.getMsg(msgNr, "Found").length() + 11) {
	    cache.setCacheStatus(chcStatus.getText().substring(MyLocale.getMsg(msgNr, "Found").length() + 1));
	} else {
	    cache.setCacheStatus(chcStatus.getText());
	}

	if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) || (cache.getCacheStatus().length() == 10 || cache.getCacheStatus().length() == 16) && cache.getCacheStatus().charAt(4) == '-') {
	    // Use same heuristic condition as in setDetails(CacheHolder) to
	    // determine, if this
	    // cache
	    // has to considered as found.
	    cache.setFound(true);
	} else
	    cache.setFound(false);

	if (!cache.isAddiWpt()) {
	    cache.setCacheOwner(inpOwner.getText().trim());
	}
	cache.setOwned(cache.getCacheStatus().equals(MyLocale.getMsg(320, "Owner")));
	// Avoid setting is_owned if alias is empty and username is empty
	if (!cache.is_owned()) {
	    cache.setOwned((!Preferences.itself().myAlias.equals("") && Preferences.itself().myAlias.equalsIgnoreCase(cache.getCacheOwner())) || (Preferences.itself().myAlias2.equalsIgnoreCase(cache.getCacheOwner())));
	}
	cache.setBlack(blackStatus);
	final String oldWaypoint = cache.getWayPoint();
	cache.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
	if (!cache.isAddiWpt()) {
	    cache.setCacheSize(CacheSize.guiSizeStrings2CwSize(chcSize.getText()));
	}
	// If the waypoint does not have a name, give it one
	if (cache.getWayPoint().equals("")) {
	    cache.setWayPoint(MainForm.profile.getNewWayPointName("CW"));
	}
	// Don't allow single letter names=> Problems in updateBearingDistance
	// This is a hack but faster than slowing down the loop in
	// updateBearingDistance
	if (cache.getWayPoint().length() < 2)
	    cache.setWayPoint(cache.getWayPoint() + " ");
	cache.setCacheName(inpName.getText().trim());
	if (!cache.isAddiWpt()) {
	    cache.setDateHidden(inpHidden.getText().trim());
	}
	final byte oldType = cache.getType();
	cache.setType(CacheType.guiSelect2Cw(chcType.getInt()));
	// thisCache.saveCacheDetails(profile.dataDir); // this is redundant,
	// because all changes
	// affecting the details are immediately saved
	// Now update the table

	cache.checkIncomplete();

	/*
	 * The references have to be rebuilt if: - the cachetype changed from addi->normal or from normal->addi - the old cachetype or the new cachetype were 'addi' and the waypointname has changed
	 */
	if (CacheType.isAddiWpt(cache.getType()) != CacheType.isAddiWpt(oldType) || ((CacheType.isAddiWpt(cache.getType()) || CacheType.isAddiWpt(oldType)) && !cache.getWayPoint().equals(oldWaypoint))) {
	    // If we changed the type to addi, check that a parent exists
	    // FIXME: if cache was renamed we need to rebuild CacheDB.hashDB first
	    MainForm.profile.buildReferences();
	} else {
	    // set status also on addi wpts
	    cache.setAttributesToAddiWpts();
	}
	if (!cache.isAddiWpt()) {
	    cache.setHard(decodeTerrDiff(btnDiff, MyLocale.getMsg(1000, "D"), cache.isCacheWpt()));
	    cache.setTerrain(decodeTerrDiff(btnTerr, MyLocale.getMsg(1001, "T"), cache.isCacheWpt()));
	}
	dirtyNotes = false;
	dirtyDetails = false;
	needsTableUpdate = false;
	cache.getCacheDetails(false).hasUnsavedChanges = true;
	if (!oldWaypoint.equals(cache.getWayPoint())) {
	    // Delete old XML - File
	    cache.getCacheDetails(false).deleteFile(MainForm.profile.dataDir + oldWaypoint + ".xml");
	}
    }

    /**
     * convert the string displayed in the terrain in difficulty buttons to a byte for intrernal use<br>
     * assumes that the relevant information will at positions 3 and 5 in a 0 indexed string
     * 
     * @param button
     *            button control to get the text from
     * @param td
     *            localized string for abbreviation of terrain or difficulty
     * @param isCache
     *            true if waypoint is a cache, false for addis and custom
     * @return 0 for additional or custum waypoints, -1 for caches if td is not valid, parsed byte otherwise
     */
    private byte decodeTerrDiff(mButton button, String td, boolean isCache) {
	// terrain and difficulty are always unset for non cache waypoints
	if (!isCache)
	    return CacheTerrDiff.CW_DT_UNSET;

	// cut off beginning of string
	String buttonText = button.getText().substring(td.length() + 2);
	// we now should have a string of length 3
	if (buttonText.length() != 3)
	    return -1;

	final StringBuffer tdv = new StringBuffer(2);
	buttonText = tdv.append(buttonText.charAt(0)).append(buttonText.charAt(2)).toString();

	// unset value is invalid
	if ("--".equals(buttonText))
	    return CacheTerrDiff.CW_DT_ERROR;

	return Byte.parseByte(buttonText);
    }

    private class TravelbugInCacheScreen extends Form {

	private final DispPanel disp = new DispPanel();
	private final mButton btCancel;

	TravelbugInCacheScreen(String text, String title) {
	    super();
	    this.setTitle(title);
	    this.setPreferredSize(Preferences.itself().myAppWidth, Preferences.itself().myAppHeight);
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
		doShowMenu(p); // direct call (not through doMenu) is neccesary
		// because it will
		// exclude the whole table
	    }

	    public void penHeld(Point p) {
		setMenu(mnuPopup);
		doShowMenu(p);
	    }

	    public void popupMenuEvent(Object selectedItem) {
		if (selectedItem.equals(mnuPickupTB)) {
		    final Travelbug tb = TravelbugPickup.pickupTravelbug(cache.getCacheDetails(true).Travelbugs);
		    TravelbugJourneyList tbjList;
		    if (tb != null) {
			dirtyDetails = true;
			// Get the list of my travelbugs
			tbjList = new TravelbugJourneyList();
			tbjList.readTravelbugsFile();
			// Add the tb to this list
			tbjList.addTbPickup(tb, MainForm.profile.name, cache.getWayPoint());
			tbjList.saveTravelbugsFile();
			setHtml(cache.getCacheDetails(true).Travelbugs.toHtml());
			repaint();
			cache.setHas_bugs(cache.getCacheDetails(true).Travelbugs.size() > 0);
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
			cache.getCacheDetails(true).Travelbugs.add(tb);
			tbjList.addTbDrop(tb, MainForm.profile.name, cache.getWayPoint());
		    }
		    tbjList.saveTravelbugsFile();
		    cache.setHas_bugs(cache.getCacheDetails(true).Travelbugs.size() > 0);
		    setHtml(cache.getCacheDetails(true).Travelbugs.toHtml());
		    repaint();
		    dirtyDetails = true;
		} else {
		    super.popupMenuEvent(selectedItem);
		}
	    }
	}
    }

    private class TerrDiffForm extends Form {
	private final mChoice mcDT;
	private final ExecutePanel executePanel;
	private final String[] DT = new String[] { "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };

	public TerrDiffForm(boolean terrain, int startVal) {
	    super();
	    mcDT = new mChoice(DT, (startVal > 0) ? (startVal - 10) / 5 : 0);
	    resizable = false;
	    setTitle(MyLocale.getMsg(31415, "D & T"));

	    addNext(new mLabel(terrain ? MyLocale.getMsg(31415, "Terrain") : MyLocale.getMsg(31415, "Difficulty")));
	    addLast(mcDT);
	    executePanel = new ExecutePanel(this);
	}

	public void onEvent(Event ev) {
	    if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
		if (ev.target == executePanel.cancelButton) {
		    close(-1);
		} else if (ev.target == executePanel.applyButton) {
		    close(1);
		}
	    }
	}

	public byte getDT() {
	    return (byte) (mcDT.selectedIndex * 5 + 10);
	}
    }
}

class AttributesViewer extends CellPanel {
    protected static int TILESIZE = Attribute.getImageWidth() + 2;
    protected final static int ICONS_PER_ROW = MyLocale.getScreenWidth() / TILESIZE < Attributes.MAXATTRIBS ? MyLocale.getScreenWidth() / TILESIZE : Attributes.MAXATTRIBS;
    protected final static int ICONROWS = (Attributes.MAXATTRIBS + ICONS_PER_ROW - 1) / ICONS_PER_ROW;
    protected mLabel mInfo;

    protected class attInteractivePanel extends InteractivePanel {
	public boolean imageMovedOn(AniImage which) {
	    if (!((attAniImage) which).info.startsWith("*")) { // If text starts with * we have no explanation yet
		mInfo.setText(((attAniImage) which).info);
		mInfo.repaintNow();
	    }
	    return true;
	}

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

    public AttributesViewer() {
	Rect r = new Rect(0, 0, TILESIZE * ICONS_PER_ROW, TILESIZE * ICONROWS); // As on GC: 6 wide, 2 high
	iap.virtualSize = r;
	iap.setFixedSize(TILESIZE * ICONS_PER_ROW, TILESIZE * ICONROWS);
	addLast(iap, CellConstants.HSTRETCH, CellConstants.FILL);
	addLast(mInfo = new mLabel(""), HSTRETCH, HFILL);
    }

    protected InteractivePanel iap = new attInteractivePanel();

    public void showImages(Attributes atts) {
	iap.images.clear();
	for (int i = 0; i < atts.count(); i++) {
	    attAniImage img = new attAniImage(atts.getAttribute(i).getImage());
	    img.info = atts.getAttribute(i).getMsg();
	    img.location = new Rect((i % ICONS_PER_ROW) * TILESIZE, (i / ICONS_PER_ROW) * TILESIZE, TILESIZE, TILESIZE);
	    iap.addImage(img);
	}
	iap.repaintNow();
    }

    public void clear() {
	iap.images.clear();
    }
    /*	public void resizeTo(int width, int height) {
    		super.resizeTo(width,height);
    	}
    */
}
