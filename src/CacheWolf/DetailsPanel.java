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
import CacheWolf.database.Attribute;
import CacheWolf.database.Attributes;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.Log;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugJourneyList;
import CacheWolf.database.TravelbugList;
import CacheWolf.exp.OCLogExport;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.CWWrapper;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import ewe.fx.IImage;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
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
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTextPad;
import ewe.util.Iterator;
import ewe.util.Vector;

/**
 * Class to create the panel to show the way point details.<br>
 * Also allows for creation and editing of way points
 */
public class DetailsPanel extends CellPanel {

    private mInput inpWaypoint;
    private mInput inpName;
    private mInput inpOwner;
    private MyChoice btnType;
    private MyChoice btnDiff;
    private MyChoice btnTerr;
    private MyChoice btnSize;
    private MyChoice btnMore;
    private mButton btnCoordinates;
    private mCheckBox cbIsSolved;
    private mCheckBox cbIsBlacklisted;
    private mButton btnFoundDate;
    private mButton btnHiddenDate;
    private mComboBox chcStatus;
    private mTextPad waypointNotes;
    private AttributesViewer attViewer;
    private CellPanel logPanel;
    private mTextPad ownLog;
    private mButton btnLog;
    private mButton btnLogToOC;
    private mButton btnEditLog;

    // ===== data handles =====
    /** waypoint to be displayed. */
    private CacheHolder ch;

    // ===== flags =====
    /** notes have changes */
    private boolean dirtyNotes = false;
    /** details have changed FIXME: make this obsolete */
    private boolean dirtyDetails = false;
    /** cache is blacklisted FIXME: make this obsolete */
    private boolean blackStatus = false;
    /** blacklist status was changed by user FIXME: make this obsolete */
    private boolean blackStatusChanged = false;
    /** FIXME */
    private boolean needsTableUpdate = false;
    /** String to display for invalid or not applicable terrain or difficulty values. */

    private final String DTINVALID = ": -.-";
    public boolean reactOnWaypointChange = false;
    private String warnedForWaypoint = "";
    private byte newCacheType;
    private byte newCacheSize;
    private String newHiddenDate;
    private boolean isBigScreen;
    private final int BUG = 0;
    private final int GOTO = 1;
    private final int NOTES = 2;
    private final int NEWWPT = 3;

    /**
     * public constructor for detail panels. should only be called from main tab.
     */
    public DetailsPanel() {
	super();

	inpName = new mInput();

	inpWaypoint = new mInput();
	btnHiddenDate = GuiImageBroker.getButton(MyLocale.getMsg(305, "Hidden on:") + MyLocale.getMsg(31415, "Set hidden date"), "calendar");
	btnHiddenDate.setToolTip(MyLocale.getMsg(31415, "Set hidden date"));

	final String[] tdSelectionList = new String[] { "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };
	btnDiff = new MyChoice(MyLocale.getMsg(1000, "D") + ": 5.5", tdSelectionList);
	btnDiff.getBtn().setToolTip(MyLocale.getMsg(31415, "Edit difficulty"));

	btnTerr = new MyChoice(MyLocale.getMsg(1001, "T") + ": 5.5", tdSelectionList);
	btnTerr.getBtn().setToolTip(MyLocale.getMsg(31415, "Edit terrain"));

	btnType = new MyChoice(CacheType.type2Gui(CacheType.CW_TYPE_REFERENCE), CacheType.typeImageNameForId(CacheType.CW_TYPE_TRADITIONAL), CacheType.guiTypeStrings());

	btnSize = new MyChoice(CacheType.type2Gui(CacheType.CW_TYPE_REFERENCE), CacheType.typeImageNameForId(CacheType.CW_TYPE_TRADITIONAL), CacheSize.guiSizeStrings());

	CellPanel ownerPanel = new CellPanel();
	ownerPanel.addNext(new mLabel(MyLocale.getMsg(306, "Owner:")), DONTSTRETCH, DONTFILL | LEFT);
	inpOwner = new mInput();
	ownerPanel.addNext(inpOwner, STRETCH, FILL);

	btnCoordinates = new mButton();
	btnCoordinates.setToolTip(MyLocale.getMsg(31415, "Edit coordinates"));

	attViewer = new AttributesViewer();

	String[] texts = new String[] { MyLocale.getMsg(346, "Show travelbugs"), MyLocale.getMsg(326, "Set as destination and show Compass View"), MyLocale.getMsg(351, "Add/Edit notes"), MyLocale.getMsg(311, "Create Waypoint") };
	String[] icons = new String[] { "bug", "goto", "notes", "newwpt" };
	btnMore = new MyChoice(MyLocale.getMsg(632, "More"), "more", texts, icons);

	CellPanel mainPanel = new CellPanel();
	mainPanel.equalWidths = true;
	Vector panelControls = new Vector();
	panelControls.add(inpWaypoint);
	panelControls.add(btnType.getBtn());
	panelControls.add(btnHiddenDate);
	panelControls.add(ownerPanel);
	panelControls.add(btnDiff.getBtn());
	panelControls.add(btnTerr.getBtn());
	panelControls.add(btnSize.getBtn());
	panelControls.add(btnCoordinates);
	panelControls.add(btnMore.getBtn());

	mainPanel.addLast(inpName);
	int anzPerLine = Math.max(1, Preferences.itself().getScreenWidth() / 200);
	isBigScreen = anzPerLine > 1;
	int i = 0;
	for (Iterator ite = panelControls.iterator(); ite.hasNext();) {
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

	CellPanel statusPanel = new CellPanel();
	CellPanel solvedPanel = new CellPanel();
	solvedPanel.addNext(new mLabel(MyLocale.getMsg(362, "solved") + ": "), DONTSTRETCH, DONTFILL | LEFT);
	cbIsSolved = new mCheckBox("");
	solvedPanel.addNext(cbIsSolved);

	solvedPanel.addNext(new mLabel(MyLocale.getMsg(363, "Blacklist") + ": "), DONTSTRETCH, DONTFILL | LEFT);
	cbIsBlacklisted = new mCheckBox("");
	solvedPanel.addNext(cbIsBlacklisted);

	statusPanel.addLast(solvedPanel, HSTRETCH, HFILL);
	CellPanel foundPanel = new CellPanel();
	foundPanel.addNext(new mLabel(MyLocale.getMsg(307, "Status:")), DONTSTRETCH, DONTFILL | LEFT);
	chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(), 0);
	foundPanel.addNext(chcStatus, HSTRETCH, (HFILL | RIGHT));
	btnFoundDate = GuiImageBroker.getButton("", "calendar");
	btnFoundDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));
	foundPanel.addLast(btnFoundDate, DONTSTRETCH, DONTFILL);
	statusPanel.addLast(foundPanel, HSTRETCH, HFILL);
	if (isBigScreen)
	    statusPanel.setText(MyLocale.getMsg(307, "Status:"));
	addLast(statusPanel, HSTRETCH, HFILL);

	if (isBigScreen) {
	    logPanel = new CellPanel();
	    logPanel.setText(" ");
	    ownLog = new mTextPad();
	    btnLog = GuiImageBroker.getButton(MyLocale.getMsg(1052, "Log online in Browser"), "");
	    btnLogToOC = GuiImageBroker.getButton(MyLocale.getMsg(1210, "logs to OC"), "");
	    btnEditLog = GuiImageBroker.getButton(MyLocale.getMsg(1055, "Change log (online)"), "");
	    logPanel.addNext(btnLog);
	    logPanel.addNext(btnLogToOC);
	    logPanel.addLast(btnEditLog);
	    logPanel.addLast(new MyScrollBarPanel(ownLog), STRETCH, FILL);
	    addLast(logPanel, STRETCH, FILL);
	    waypointNotes = new mTextPad();
	    // waypointNotes.modify(ControlConstants.NotEditable, 0);
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
	return dirtyNotes || dirtyDetails || needsTableUpdate;
    }

    public boolean hasBlackStatusChanged() {
	return blackStatusChanged;
    }

    public void setDetails(final CacheHolder _ch, boolean isNew) {
	ch = _ch;
	needsTableUpdate = isNew;
	CacheHolder mainCache = ch;
	if (ch.isAddiWpt() && (ch.mainCache != null)) {
	    mainCache = ch.mainCache;
	}
	dirtyNotes = false;
	dirtyDetails = false;
	inpWaypoint.setText(ch.getWayPoint());
	inpName.setText(ch.getCacheName());
	btnCoordinates.setText(ch.getPos().toString());
	newHiddenDate = mainCache.getDateHidden();
	GuiImageBroker.setButtonText(btnHiddenDate, MyLocale.getMsg(305, "Hidden on:") + mainCache.getDateHidden());
	inpOwner.setText(mainCache.getCacheOwner());
	this.cbIsSolved.setState(ch.isSolved());
	chcStatus.setText(ch.getStatusText());
	newCacheType = ch.getType();
	GuiImageBroker.setButtonIconAndText(btnType.getBtn(), CacheType.type2Gui(newCacheType), GuiImageBroker.makeImageForButton(btnType.getBtn(), CacheType.type2Gui(newCacheType), CacheType.typeImageNameForId(newCacheType)));
	this.cbIsBlacklisted.setState(ch.isBlack());

	blackStatus = ch.isBlack();
	blackStatusChanged = false;

	if (inpWaypoint.getText().length() == 0) {
	    createWptName();
	}

	if (mainCache.hasBugs()) {
	    btnMore.enable(BUG);
	} else {
	    btnMore.disable(BUG);
	}

	newCacheSize = mainCache.getCacheSize();
	String text = CacheSize.cw2ExportString(newCacheSize);
	String icon = CacheSize.cacheSize2ImageName(newCacheSize);
	IImage btnSizeNewImage = GuiImageBroker.makeImageForButton(btnSize.getBtn(), text, icon);
	GuiImageBroker.setButtonIconAndText(btnSize.getBtn(), CacheSize.cw2ExportString(newCacheSize), btnSizeNewImage);

	attViewer.showImages(ch.getCacheDetails(true).attributes);

	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    deactivateControl(btnTerr.getBtn());
	    deactivateControl(btnDiff.getBtn());
	    deactivateControl(btnSize.getBtn());
	    deactivateControl(inpOwner);
	    deactivateControl(this.btnHiddenDate);
	    deactivateControl(this.cbIsBlacklisted);
	} else {
	    activateControl(btnTerr.getBtn());
	    activateControl(btnDiff.getBtn());
	    activateControl(btnSize.getBtn());
	    activateControl(inpOwner);
	    activateControl(this.btnHiddenDate);
	    activateControl(this.cbIsBlacklisted);
	}

	if (ch.isCustomWpt()) {
	    btnTerr.getBtn().setText(MyLocale.getMsg(1001, "T") + DTINVALID);
	    btnDiff.getBtn().setText(MyLocale.getMsg(1000, "D") + DTINVALID);
	} else {
	    if (CacheTerrDiff.isValidTD(mainCache.getTerrain())) {
		btnTerr.getBtn().setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(mainCache.getTerrain()));
	    } else {
		btnTerr.getBtn().setText("T: -.-");
		mainCache.setIncomplete(true);
		Preferences.itself().log(mainCache.getWayPoint() + " has wrong terrain " + mainCache.getTerrain());
	    }
	    if (CacheTerrDiff.isValidTD(mainCache.getHard())) {
		btnDiff.getBtn().setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(mainCache.getHard()));
	    } else {
		btnDiff.getBtn().setText("D: -.-");
		mainCache.setIncomplete(true);
		Preferences.itself().log(mainCache.getWayPoint() + " has wrong difficulty " + mainCache.getHard());
	    }
	}

	/*
	int addiCount = 0;
	if (ch.mainCache == null) {
	    addiCount = ch.addiWpts.size();
	} else {
	    addiCount = ch.mainCache.addiWpts.size();
	}
	lblAddiCount.setText(": " + addiCount); //MyLocale.getMsg(1044, "Addis") + 
	*/

	if (isBigScreen) {
	    deactivateControl(btnEditLog);
	    deactivateControl(btnLogToOC);
	    deactivateControl(btnLog);
	    if (ch.isCacheWpt()) {
		logPanel.setText(MyLocale.getMsg(278, "Eigener Log: ") + ch.getCacheDetails(false).OwnLogId);
		if (ch.getCacheDetails(false).OwnLog != null) {
		    if (ch.getCacheDetails(false).OwnLogId.length() == 0) {
			activateControl(btnLog);
		    } else {
			activateControl(btnEditLog);
			String ocWpName = ch.getOcCacheID();
			if (ocWpName.length() > 0 && ocWpName.charAt(0) < 65) {
			    activateControl(btnLogToOC);
			} else {
			    deactivateControl(btnLogToOC);
			}
		    }
		    ownLog.setText(ch.getCacheDetails(false).OwnLog.getMessageWithoutHTML());
		} else {
		    ownLog.setText("");
		    activateControl(btnLog);
		    deactivateControl(btnEditLog);
		    deactivateControl(btnLogToOC);
		}
		waypointNotes.setText(ch.getCacheDetails(false).getCacheNotes());
	    }
	}
    }

    /**
     * if is addi -> returns the respective AddiWpt if is main -> returns the respective MainWpt
     */
    public void createWptName() {
	final String wpt = inpWaypoint.getText().toUpperCase();
	if (CacheType.isAddiWpt(newCacheType) //
		&& MainTab.itself.mainCache != null //
		&& (MainTab.itself.mainCache.startsWith("GC") //
			|| OC.isOC(MainTab.itself.mainCache) //
		|| MainTab.itself.mainCache.startsWith("CW")) && wpt.startsWith("CW")) {
	    // for creating the Addiname on creating a new Waypoint
	    MainTab.itself.lastselected = MainTab.itself.mainCache;
	    inpWaypoint.setText(MainForm.profile.getNewAddiWayPointName(MainTab.itself.mainCache));
	}
	if (!CacheType.isAddiWpt(newCacheType) && !(wpt.startsWith("GC") || OC.isOC(wpt) || wpt.startsWith("CW"))) {
	    inpWaypoint.setText(MainForm.profile.getNewWayPointName("CW"));
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
	    if (ev.target == inpWaypoint) {
		if (reactOnWaypointChange) {
		    String iTmp = inpWaypoint.getText();
		    String uTmp = iTmp.toUpperCase();
		    if (!iTmp.equals(uTmp)) {
			inpWaypoint.setText(uTmp); // If user entered LowerCase -> convert directly to UpperCase
			reactOnWaypointChange = false; // next DataChangeEvent fired by change to UpperCase will be ignored
		    }
		    // already warned(multi same DataChangeEvents) or same waypointname as before edit !!!
		    if (!warnedForWaypoint.equals(uTmp) && !uTmp.equals(this.ch.getWayPoint())) {
			if ((new File(MainForm.profile.dataDir + iTmp.toLowerCase() + ".xml")).exists()) {
			    warnedForWaypoint = uTmp;
			    // filename is LowerCase
			    new InfoBox("Warning :", uTmp + "\n" + MyLocale.getMsg(275, "Waypoint already exists!")).wait(FormBase.OKB);
			    // revert waypointname
			    inpWaypoint.setText(this.ch.getWayPoint());
			}
		    }
		    dirtyDetails = true;
		    needsTableUpdate = true;
		} else {
		    // first DataChangeEvent is fired by Klick into (after reload).
		    // that really didn't change anything
		    reactOnWaypointChange = true;
		}
		// FIXME: if name was changed, we should rename the waypoint.xml file. how? where?
	    } else if (ev.target == this.inpName) {
		if (ch.setCacheName(inpName.getText().trim())) {
		    dirtyDetails = true;
		}
	    } else if (ev.target == this.inpOwner) {
		dirtyDetails = true;
	    } else if (ev.target == this.ownLog) {
		dirtyDetails = true;
	    } else if (ev.target == this.waypointNotes) {
		ch.getCacheDetails(false).setCacheNotes(waypointNotes.getText());
	    } else {
		//Preferences.itself().log("DataChangeEvent at Details for " + ev.target.toString() + ". DirtyDetails not set.");
		dirtyDetails = true;
	    }
	    // FIXME: check if something was actually changed, since datacachnge events also occur if you just hop through the fields with the tab key (Why? don't know!)
	} else if (ev instanceof MenuEvent) {
	    if (ev.type == MenuEvent.SELECTED) {
		Menu menu = ((MenuEvent) ev).menu;
		if (menu == this.btnType.getMnu()) {
		    if (btnType.mark(btnType.getSelectedIndex())) {
			newCacheType = CacheType.guiSelect2Cw(btnType.getSelectedIndex());
			GuiImageBroker.setButtonIconAndText(btnType.getBtn(), CacheType.type2Gui(newCacheType), GuiImageBroker.makeImageForButton(btnType.getBtn(), CacheType.type2Gui(newCacheType), CacheType.typeImageNameForId(newCacheType)));
			createWptName();
			if (CacheType.isCacheWpt(newCacheType)) {
			    activateControl(btnTerr.getBtn());
			    activateControl(btnDiff.getBtn());
			    activateControl(btnSize.getBtn());
			    activateControl(inpOwner);
			    activateControl(this.btnHiddenDate);
			    btnMore.enable(BUG);
			    activateControl(this.cbIsBlacklisted);
			} else {
			    deactivateControl(btnTerr.getBtn());
			    deactivateControl(btnDiff.getBtn());
			    deactivateControl(btnSize.getBtn());
			    deactivateControl(inpOwner);
			    deactivateControl(this.btnHiddenDate);
			    btnMore.disable(BUG);
			    deactivateControl(this.cbIsBlacklisted);
			    newCacheSize = CacheSize.CW_SIZE_NOTCHOSEN;
			    GuiImageBroker.setButtonIconAndText(btnSize.getBtn(), CacheSize.cw2ExportString(newCacheSize),
				    GuiImageBroker.makeImageForButton(btnSize.getBtn(), CacheSize.cw2ExportString(newCacheSize), CacheSize.cacheSize2ImageName(newCacheSize)));
			    btnTerr.getBtn().setText(MyLocale.getMsg(1001, "T") + DTINVALID);
			    btnDiff.getBtn().setText(MyLocale.getMsg(1000, "D") + DTINVALID);
			}
			dirtyDetails = true;
			menu.close();
		    }
		} else if (menu == this.btnDiff.getMnu()) {
		    if (btnDiff.mark(btnDiff.getSelectedIndex())) {
			btnDiff.getBtn().setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT((byte) (btnDiff.getSelectedIndex() * 5 + 10)));
			dirtyDetails = true;
			menu.close();
		    }
		} else if (menu == this.btnSize.getMnu()) {
		    if (btnSize.mark(btnSize.getSelectedIndex())) {
			newCacheSize = CacheSize.guiSizeStrings2CwSize(btnSize.getSelectedValue());
			GuiImageBroker.setButtonIconAndText(btnSize.getBtn(), CacheSize.cw2ExportString(newCacheSize),
				GuiImageBroker.makeImageForButton(btnSize.getBtn(), CacheSize.cw2ExportString(newCacheSize), CacheSize.cacheSize2ImageName(newCacheSize)));
			dirtyDetails = true;
			menu.close();
		    }
		} else if (menu == this.btnTerr.getMnu()) {
		    if (btnTerr.mark(btnTerr.getSelectedIndex())) {
			btnTerr.getBtn().setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT((byte) (btnTerr.getSelectedIndex() * 5 + 10)));
			dirtyDetails = true;
			menu.close();
		    }
		} else if (menu == this.btnMore.getMnu()) {
		    switch (btnMore.getSelectedIndex()) {
		    case BUG:
			final TravelbugInCacheScreen ts = new TravelbugInCacheScreen(ch.getCacheDetails(false).Travelbugs.toHtml(), "Travelbugs");
			ts.execute(this.getFrame(), Gui.CENTER_FRAME);
			break;
		    case NEWWPT:
			final CacheHolder newCache = new CacheHolder();
			newCache.setPos(ch.getPos());
			newCache.setType(CacheType.CW_TYPE_STAGE);
			newCache.setHard(CacheTerrDiff.CW_DT_UNSET);
			newCache.setTerrain(CacheTerrDiff.CW_DT_UNSET);
			newCache.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
			MainTab.itself.newWaypoint(newCache);
			break;
		    case NOTES:
			final NotesScreen nsc = new NotesScreen(ch);
			nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
			if (isBigScreen) {
			    waypointNotes.setText(ch.getCacheDetails(false).getCacheNotes());
			}
			break;
		    case GOTO:
			if (ch.getPos().isValid()) {
			    Navigate.itself.setDestination(ch);
			    MainTab.itself.select(MainTab.GOTO_CARD);
			}
			break;
		    }
		}
	    }
	} else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == btnDiff.getBtn()) {
		int startIndex = decodeTerrDiff(btnDiff.getBtn(), MyLocale.getMsg(1001, "D"), CacheType.isCacheWpt(newCacheType));
		startIndex = (startIndex > 0) ? (startIndex - 10) / 5 : 0;
		btnDiff.mark(startIndex);
		btnDiff.showMenu();
	    } else if (ev.target == btnTerr.getBtn()) {
		int startIndex = decodeTerrDiff(btnTerr.getBtn(), MyLocale.getMsg(1001, "T"), CacheType.isCacheWpt(newCacheType));
		startIndex = (startIndex > 0) ? (startIndex - 10) / 5 : 0;
		btnTerr.mark(startIndex);
		btnTerr.showMenu();
	    } else if (ev.target == this.btnSize.getBtn()) {
		int startIndex = CacheSize.cwSizeId2GuiSizeId(newCacheSize);
		btnSize.mark(startIndex);
		btnSize.showMenu();
	    } else if (ev.target == this.btnType.getBtn()) {
		int startIndex = CacheType.cw2GuiSelect(newCacheType);
		btnType.mark(startIndex);
		btnType.showMenu();
	    } else if (ev.target == this.btnMore.getBtn()) {
		btnMore.showMenu();
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
	    } else if (ev.target == btnCoordinates) {
		CWPoint coords = new CWPoint(btnCoordinates.getText(), TransformCoordinates.DMM);
		if (Vm.isMobile()) {
		    CoordsPDAInput InScr = new CoordsPDAInput(TransformCoordinates.DMM, true);
		    if (coords.isValid())
			InScr.setCoords(coords);
		    else
			InScr.setCoords(new CWPoint(0, 0));
		    if (InScr.execute(null, TOP) == FormBase.IDOK) {
			dirtyDetails = true;
			coords = InScr.getCoords();
			ch.setPos(coords);
			btnCoordinates.setText(coords.toString());
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
			ch.setPos(coords);
			btnCoordinates.setText(coords.toString());
			// If the current centre is valid, calculate the distance and bearing to it
			final CWPoint centre = Preferences.itself().curCentrePt;
			if (centre.isValid()) {
			    ch.calcDistance(centre); // todo perhaps sortTable
			}
		    }
		}
	    } else if (ev.target == btnFoundDate) {
		int msgNr = ch.getLogMsgNr();
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
		    chcStatus.setText(dc.year + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00") + " " + dc.time + " " + MyLocale.getMsg(319, "not Found"));
		    dirtyDetails = true;
		}
	    } else if (ev.target == btnHiddenDate) {
		DateChooser.dayFirst = true;
		final DateChooser dc = new DateChooser(Vm.getLocale());
		dc.title = MyLocale.getMsg(329, "Hidden date");
		dc.setPreferredSize(240, 240);
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
		CacheHolder mainCache = ch;
		url = "";
		if (ch.isAddiWpt() && (ch.mainCache != null)) {
		    mainCache = ch.mainCache;
		}
		if (mainCache.isCacheWpt()) {
		    CacheHolderDetail chD = mainCache.getCacheDetails(false);
		    String ownLogMessage = "";
		    if (chD != null) {
			if (chD.OwnLog != null) {
			    // Cache schon im CW gelogged
			    ownLogMessage = chD.OwnLog.getMessage();
			    if (ownLogMessage.length() > 0) {
				Vm.setClipboardText(mainCache.cacheStatus() + '\n' + "<br>" + ownLogMessage);
			    }
			}
		    }
		    if (mainCache.isOC()) {
			if (chD != null) {
			    url = chD.URL;
			    if (url.indexOf("viewcache") >= 0) {
				url = STRreplace.replace(url, "viewcache", "log");
			    }
			} else {
			    url = "http://" + OC.getOCHostName(mainCache.getWayPoint()) + "/log.php?wp=" + mainCache.getWayPoint();
			}
		    } else { // bei GC loggen
			url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.GetCacheID();
		    }

		    if (url.length() > 0) {
			callExternalProgram(Preferences.itself().browser, url);
		    }

		} else {
		    if (mainCache.isCustomWpt() && mainCache.getWayPoint().startsWith("GC")) {
			url = "http://www.geocaching.com/seek/log.aspx?ID=" + mainCache.GetCacheID();
			callExternalProgram(Preferences.itself().browser, url);
		    }
		}
	    } else if (ev.target == btnLogToOC) {
		// GC und schon gelogged --> log bei OC eintragen
		OCLogExport.doOneLog(ch);
		MainTab.itself.tablePanel.refreshTable();
	    } else if (ev.target == btnEditLog) {
		String url = "";
		if (ch.isCacheWpt()) {
		    CacheHolderDetail chD = ch.getCacheDetails(false);
		    if (chD != null) {
			if (chD.OwnLog != null) {
			    if (ch.isGC()) {
				url = "http://www.geocaching.com/seek/log.aspx?LID=" + chD.OwnLogId;
				callExternalProgram(Preferences.itself().browser, url);
			    }
			}
		    }
		}
	    }
	}
    }

    private void callExternalProgram(String program, String parameter) {
	try {
	    CWWrapper.exec(program, parameter);
	} catch (IOException ex) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1034, "Cannot start " + program + "!") + "\n" + ex.toString() + "\n" + MyLocale.getMsg(1035, "Possible reason:") + "\n" + MyLocale.getMsg(1036, "A bug in ewe VM, please be")
		    + "\n" + MyLocale.getMsg(1037, "patient for an update")).wait(FormBase.OKB);
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
	// we have a mix of setting dirty flag (most values) and directly changing the cache values (notes,...).
	// that doesn't matter as log as we have no revert.
	// the dirty flag indicates the global change (which is not necessary for the directly changed values).
	// so each value will be set by the setter, that checks for a change and sets the flag for writing to disk.
	// 
	// We have to update two objects: 
	// thisCache (a CacheHolderDetail) which contains the full cache which will be written to the cache.xml file
	// AND
	// the CacheHolder object which sits in cacheDB

	int msgNr = ch.getLogMsgNr();

	// Strip the found message if the status contains a date
	if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) && chcStatus.getText().length() >= MyLocale.getMsg(msgNr, "Found").length() + 11) {
	    ch.setCacheStatus(chcStatus.getText().substring(MyLocale.getMsg(msgNr, "Found").length() + 1));
	} else {
	    ch.setCacheStatus(chcStatus.getText());
	}

	if (ch.isSolved() != this.cbIsSolved.getState())
	    ch.setIsSolved(this.cbIsSolved.getState());

	if (chcStatus.getText().startsWith(MyLocale.getMsg(msgNr, "Found")) || (ch.cacheStatus().length() == 10 || ch.cacheStatus().length() == 16) && ch.cacheStatus().charAt(4) == '-') {
	    // Use same heuristic condition as in setDetails(CacheHolder) to determine, if this cache has to considered as found.
	    ch.setFound(true);
	} else
	    ch.setFound(false);

	if (!ch.isAddiWpt()) {
	    ch.setCacheOwner(inpOwner.getText().trim());
	}
	ch.setOwned(ch.cacheStatus().equals(MyLocale.getMsg(320, "Owner")));
	// Avoid setting is_owned if alias is empty and username is empty
	if (!ch.isOwned()) {
	    ch.setOwned((!Preferences.itself().myAlias.equals("") && Preferences.itself().myAlias.equalsIgnoreCase(ch.getCacheOwner())) || (Preferences.itself().myAlias2.equalsIgnoreCase(ch.getCacheOwner())));
	}
	ch.setBlack(blackStatus);

	final String oldWaypoint = ch.getWayPoint();
	ch.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
	if (!ch.isAddiWpt()) {
	    ch.setCacheSize(newCacheSize);
	}
	// If the waypoint does not have a name, give it one
	if (ch.getWayPoint().equals("")) {
	    ch.setWayPoint(MainForm.profile.getNewWayPointName("CW"));
	}
	// Don't allow single letter names=> Problems in updateBearingDistance
	// This is a hack but faster than slowing down the loop in
	// updateBearingDistance
	if (ch.getWayPoint().length() < 2)
	    ch.setWayPoint(ch.getWayPoint() + " ");

	ch.setCacheName(inpName.getText().trim());
	if (!ch.isAddiWpt()) {
	    ch.setDateHidden(newHiddenDate.trim());
	}

	final byte oldType = ch.getType();
	ch.setType(newCacheType);

	String ownLogText = STRreplace.replace(ownLog.getText(), "\n", "<br />");
	Log oldLog = ch.getCacheDetails(false).OwnLog;
	if (oldLog == null) {
	    if (ownLogText.length() > 0) {
		String OwnLogId = ch.getCacheDetails(false).OwnLogId;
		// todo must get date from status
		// todo must set icon to correct value
		ch.getCacheDetails(false).OwnLog = new Log(OwnLogId, Preferences.itself().gcMemberId, "2.png", "1900-01-01", Preferences.itself().myAlias, ownLogText);
	    }
	} else {
	    if (ownLogText.length() > 0)
		oldLog.setMessage(ownLogText);
	    else {
		oldLog.setLogID("");
		oldLog.setMessage("");
	    }
	}

	ch.checkIncomplete();

	/*
	 * The references have to be rebuilt if:
	 *  the cachetype changed from addi->normal or from normal->addi
	 *  or the old or new cachetype is 'addi' and the waypointname has changed
	 */
	if (CacheType.isAddiWpt(newCacheType) != CacheType.isAddiWpt(oldType) //
		|| ((CacheType.isAddiWpt(ch.getType()) || CacheType.isAddiWpt(oldType)) && !ch.getWayPoint().equals(oldWaypoint)) //
	) {
	    // If we changed the type to addi, check that a parent exists
	    // FIXME: if cache was renamed we need to rebuild CacheDB.hashDB first
	    MainForm.profile.buildReferences();
	} else {
	    // set status also on addi wpts
	    ch.setAttributesFromMainCacheToAddiWpts();
	}
	if (!ch.isAddiWpt()) {
	    ch.setHard(decodeTerrDiff(btnDiff.getBtn(), MyLocale.getMsg(1000, "D"), ch.isCacheWpt()));
	    ch.setTerrain(decodeTerrDiff(btnTerr.getBtn(), MyLocale.getMsg(1001, "T"), ch.isCacheWpt()));
	}
	dirtyNotes = false;
	dirtyDetails = false;
	needsTableUpdate = false;
	ch.getCacheDetails(false).hasUnsavedChanges = true;
	if (!oldWaypoint.equals(ch.getWayPoint())) {
	    // Delete old XML - File
	    ch.getCacheDetails(false).deleteFile(MainForm.profile.dataDir + oldWaypoint + ".xml");
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
	    this.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
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
		    final Travelbug tb = TravelbugPickup.pickupTravelbug(getCache().getCacheDetails(true).Travelbugs);
		    TravelbugJourneyList tbjList;
		    if (tb != null) {
			dirtyDetails = true;
			// Get the list of my travelbugs
			tbjList = new TravelbugJourneyList();
			tbjList.readTravelbugsFile();
			// Add the tb to this list
			tbjList.addTbPickup(tb, MainForm.profile.name, getCache().getWayPoint());
			tbjList.saveTravelbugsFile();
			setHtml(getCache().getCacheDetails(true).Travelbugs.toHtml());
			repaint();
			getCache().hasBugs(getCache().getCacheDetails(true).Travelbugs.size() > 0);
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
			getCache().getCacheDetails(true).Travelbugs.add(tb);
			tbjList.addTbDrop(tb, MainForm.profile.name, getCache().getWayPoint());
		    }
		    tbjList.saveTravelbugsFile();
		    getCache().hasBugs(getCache().getCacheDetails(true).Travelbugs.size() > 0);
		    setHtml(getCache().getCacheDetails(true).Travelbugs.toHtml());
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
	selectionList = _selectionList;
	iconList = null;
	init();
    }

    public MyChoice(String btnText, String icon, String[] _selectionList) {
	btn = GuiImageBroker.getButton(btnText, icon);
	selectionList = _selectionList;
	iconList = null;
	init();
    }

    public MyChoice(String btnText, String icon, String[] _selectionList, String[] _iconList) {
	btn = GuiImageBroker.getButton(btnText, icon);
	selectionList = _selectionList;
	iconList = _iconList;
	init();
    }

    private void init() {
	mnu = this;
	miList = new MenuItem[selectionList.length];
	for (int i = 0; i < selectionList.length; i++) {
	    if (iconList == null) {
		mnu.addItem(miList[i] = new MenuItem(selectionList[i]));
	    } else {
		if (iconList[i].length() > 0)
		    mnu.addItem(miList[i] = GuiImageBroker.getMenuItem(selectionList[i], iconList[i]));
		else
		    mnu.addItem(miList[i] = new MenuItem(selectionList[i]));
	    }
	}
	btn.setMenu(mnu);
	btn.modifyAll(ControlConstants.WantHoldDown, 0);
	currentIndex = -1;
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

    public int getSelectedIndex() {
	return selectedIndex;
    }

    public String getSelectedValue() {
	return selectionList[currentIndex];
    }

    public void showMenu() {
	Rect startPos = mnu.getRect();
	btn.startDropMenu(new Point(startPos.width, 0));
    }

}

class AttributesViewer extends CellPanel {
    protected int TILESIZE = Attribute.getImageWidth() + 2;
    protected final int MAX_ICONS_PER_ROW = Preferences.itself().getScreenWidth() / (TILESIZE + 6);
    protected final int MAX_ROWS = 1 + Attributes.MAXATTRIBS / MAX_ICONS_PER_ROW;
    protected InteractivePanel iap;
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

}
