package CacheWolf;

import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.fx.mImage;
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
import ewe.ui.MessageBox;
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
	/** way point id */
	private static mInput inpWaypoint;
	/** way point name */
	private static mInput inpName;
	/** way point hidden date */
	private static mInput inpHidden;
	/** way point owner */
	private static mInput inpOwner;
	/** way point coordinates, open change coordinates dialog */
	private static mButton btnCoordinates;
	/** set center to current way point */
	private static mButton btnCenter;
	/** add time stamp to notes */
	private static mButton btnAddDateTime;
	/** create a new way point */
	private static mButton btnNewWpt;
	/** show details for travel bus in way point */
	private static mButton btnShowBug;
	/** switch to moving map and center on way point coordinates */
	private static mButton btnShowMap;
	/** set way point as destination and switch to goto ppanel */
	private static mButton btnGoto;
	/** add a user picture to way point */
	private static mButton btnAddPicture;
	/** toggle blacklist status */
	private static mButton btnBlack;
	/** add or edit notes for way point */
	private static mButton btnNotes;
	/** set found date */
	private static mButton btnFoundDate;
	/** set hidden date */
	private static mButton btnHiddenDate;
	/** set terrain value */
	private static mButton btnTerr;
	/** set difficulty value */
	private static mButton btnDiff;
	/** drop down list with cache types */
	private static mChoice chcType;
	/** drop down list with container sizes */
	private static mChoice chcSize;
	/** select way point status */
	private static mComboBox chcStatus;
	/** toolbar panel */
	private static CellPanel pnlTools;
	/** notes for way point */
	private static mTextPad waypointNotes;
	/** shows number of additional way points */
	private static mLabel lblAddiCount;
	/** FIXME move to image broker? */
	private static mImage imgBlack, imgBlackNo, imgShowBug, imgShowBugNo, imgNewWpt, imgGoto, imgNotes;
	/** FIXME: what are they for? */
	private static mImage imgShowMaps, imgAddImages;

	// ===== data handles =====
	/** FIXME: never used? */
	private static CacheDB cacheDB;
	/** waypoint to be displayed */
	private static CacheHolder ch;
	/** FIXME: never used? */
	private static int dbIndex = -1;
	/** panel to display waypoint attributes */
	private static AttributesViewer attViewer;
	/** preferences object */
	private static Preferences pref;
	/** waypoint profile */
	private static Profile profile;

	// ===== flags =====
	/** notes have changes */
	private boolean dirty_notes;
	/** details have changed FIXME: make this obsolete */
	private boolean dirty_details;
	/** cache is blacklisted FIXME: make this obsolete */
	private boolean blackStatus;
	/** blacklist status was changed by user FIXME: make this obsolete */
	private boolean blackStatusChanged;
	/** FIXME */
	private boolean needsTableUpdate;
	/** screen is VGA or better */
	private boolean isBigScreen;
	/** use big icons */
	private boolean useBigIcons;

    // TODO: move images to image broker
    //mImage imgBlack, imgBlackNo, imgShowBug, imgShowBugNo, imgNewWpt, imgGoto, imgShowMaps, imgAddImages, imgNotes;

	//FIXME: remove
	CacheHolder thisCache;
	
	/**
	 * public constructor for detail panels. should only be called from main tab.
	 */
	public DetailsPanel() {
        // ===== local objects =====
		/** helper panels to organize layout */
		CellPanel helperPanel1, helperPanel2, helperPanel3, helperPanel4, helperPanel5;

		// ===== initialize data handles =====
		pref = Global.getPref();
		profile = Global.getProfile();
		cacheDB = profile.cacheDB;

		// ===== initialize flags =====
		dirty_notes = false;
		dirty_details = false;
		blackStatus = false;
		blackStatusChanged = false;
		needsTableUpdate = false;
		isBigScreen = pref.isBigScreen;
		useBigIcons = pref.useBigIcons;

		// ===== initialize GUI objects =====

		// ----- tools panel ------
		pnlTools = new CellPanel();
		btnNewWpt = new mButton(imgNewWpt = new mImage(useBigIcons ? "newwpt_vga.png" : "newwpt.png"));
		// FIXME: get an image with proper transparency
		imgNewWpt.transparentColor = new Color(255, 0, 0);
		btnNewWpt.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));

		btnGoto = new mButton(imgGoto = new mImage(useBigIcons ? "goto_vga.png"	: "goto.png"));
		// FIXME: get an image with proper transparency
		imgGoto.transparentColor = Color.White;
		btnGoto.setToolTip(MyLocale.getMsg(345, "Goto these coordinates"));

		btnShowBug = new mButton(new mImage(useBigIcons ? "bug_no_vga.gif" : "bug_no.gif"));
		btnShowBug.setToolTip(MyLocale.getMsg(346, "Show travelbugs"));

		btnShowMap = new mButton(new mImage(useBigIcons ? "globe_small_vga.gif" : "globe_small.gif"));
		btnShowMap.setToolTip(MyLocale.getMsg(347, "Show map"));

		btnAddPicture = new mButton(imgAddImages = new mImage(useBigIcons ? "images_vga.gif" : "images.gif"));
		btnAddPicture.setToolTip(MyLocale.getMsg(348, "Add user pictures"));

		btnBlack = new mButton(imgBlack = new mImage(useBigIcons ? "no_black_vga.png" : "no_black.png"));
		// FIXME: get an image with proper transparency
		imgBlack.transparentColor = Color.Black;
		btnBlack.setToolTip(MyLocale.getMsg(349, "Toggle Blacklist status"));

		btnNotes = new mButton(imgNotes = new mImage(useBigIcons ? "notes_vga.gif" : "notes.gif"));
		// FIXME: get an image with proper transparency
		imgNotes.transparentColor = Color.DarkBlue;
		btnNotes.setToolTip(MyLocale.getMsg(351, "Add/Edit notes"));

		btnAddDateTime = new mButton(new mImage(useBigIcons ? "date_time_vga.gif" : "date_time.gif"));
		btnAddDateTime.setToolTip(MyLocale.getMsg(350, "Add timestamp to notes"));
		
        // ----- main body -----

		helperPanel1 = new CellPanel();
		helperPanel2 = new CellPanel();
		helperPanel3 = new CellPanel();
		helperPanel4 = new CellPanel();
		helperPanel5 = new CellPanel();
		attViewer = new AttributesViewer();

		chcType = new mChoice(CacheType.guiTypeStrings(), 0);
		chcType.alwaysDrop = true;
		chcSize = new mChoice(CacheSize.guiSizeStrings(), 0);
		chcSize.alwaysDrop = true;
		chcStatus = new mComboBox(new String[] { "",
				MyLocale.getMsg(313, "Flag 1"), MyLocale.getMsg(314, "Flag 2"),
				MyLocale.getMsg(315, "Flag 3"), MyLocale.getMsg(316, "Flag 4"),
				MyLocale.getMsg(317, "Search"), MyLocale.getMsg(318, "Found"),
				MyLocale.getMsg(319, "Not Found"),
				MyLocale.getMsg(320, "Owner") }, 0);
		inpWaypoint = new mInput();
		inpName = new mInput();

		btnCoordinates = new mButton();
		btnCoordinates.setToolTip(MyLocale.getMsg(31415, "Edit coordinates"));

		inpOwner = new mInput();
		inpHidden = new mInput();
		inpHidden.modifyAll(DisplayOnly, 0);

		// ===== put the controls onto the GUI =====
		// ----- tools panel ------
		pnlTools.addNext(btnNewWpt);
		pnlTools.addNext(btnGoto);
		pnlTools.addNext(btnShowBug);
		pnlTools.addNext(btnShowMap);
		pnlTools.addNext(btnAddPicture);
		pnlTools.addNext(btnBlack);
		pnlTools.addNext(btnNotes);
		pnlTools.addLast(btnAddDateTime);

		pnlTools.stretchFirstRow = true;

        // ----- helper panels -----

		btnDiff = new mButton(MyLocale.getMsg(1000, "D") + ": 5.5");
		btnDiff.setPreferredSize(pref.fontSize * 3, chcSize.getPreferredSize(null).height);
		btnDiff.setToolTip(MyLocale.getMsg(31415, "Edit difficulty"));

		btnTerr = new mButton(MyLocale.getMsg(1001, "T") + ": 5.5");
		btnTerr.setPreferredSize(pref.fontSize * 3, chcSize.getPreferredSize(null).height);
		btnTerr.setToolTip(MyLocale.getMsg(31415, "Edit terrain"));

		lblAddiCount = new mLabel(MyLocale.getMsg(1044, "Addis") + ": 888");

		btnFoundDate = new mButton(new mImage(useBigIcons ? "calendar_vga.png" : "calendar.png"));
		btnFoundDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));

		btnHiddenDate = new mButton(new mImage(useBigIcons ? "calendar_vga.png"	: "calendar.png"));
		btnHiddenDate.setToolTip(MyLocale.getMsg(31415, "Set hidden date"));

		helperPanel1.addNext(chcType, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel1.addLast(btnDiff, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));

		helperPanel2.addNext(chcSize, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel2.addLast(btnTerr, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));

		helperPanel3.addNext(inpWaypoint, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel3.addLast(lblAddiCount, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));

		helperPanel4.addNext(chcStatus, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel4.addLast(btnFoundDate, DONTSTRETCH, DONTFILL);

		helperPanel5.addNext(inpHidden, CellConstants.HSTRETCH,	(CellConstants.HFILL | CellConstants.WEST));
		helperPanel5.addLast(btnHiddenDate, DONTSTRETCH, DONTFILL);

		// ----- main body -----
		addLast(pnlTools, CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN, new Dimension(3, 1));

		addNext(new mLabel(MyLocale.getMsg(300, "Type:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.NORTHWEST));
		addLast(helperPanel1, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(301, "Size:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.NORTHWEST));
		addLast(helperPanel2, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(302, "Waypoint:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel3, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(303, "Name:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(inpName.setTag(CellConstants.SPAN, new Dimension(2, 1)), CellConstants.DONTSTRETCH,	(CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(304, "Coordinates:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(btnCoordinates.setTag(CellConstants.SPAN, new Dimension(2, 1)),	CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(307, "Status:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel4, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(306, "Owner:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(inpOwner.setTag(CellConstants.SPAN, new Dimension(2, 1)), CellConstants.DONTSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(305, "Hidden on:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel5, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addLast(attViewer);

		//TODO: decide whether to still use the object without display on small screens to handle notes
		if (isBigScreen) {
			addLast(new mLabel(MyLocale.getMsg(308, "Notes:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			waypointNotes = new mTextPad();
			waypointNotes.modify(ControlConstants.NotEditable, 0);
			addLast(new MyScrollBarPanel(waypointNotes));
		}

		//FIXME: get rid of this
		imgBlack = new mImage(useBigIcons?"is_black_vga.png":"is_black.png");
		imgBlack.transparentColor = Color.White;
		imgBlackNo = new mImage(useBigIcons ? "no_black_vga.png" : "no_black.png");
		imgBlackNo.transparentColor = Color.Black;
		imgShowBug = new mImage(useBigIcons ? "bug_vga.gif":"bug.gif");
		imgShowBugNo = new mImage(useBigIcons ? "bug_no_vga.gif":"bug_no.gif");
	}

	public void clear() {
		attViewer.clear();
	}

	public void setNeedsTableUpdate(boolean tableUpdate) {
		needsTableUpdate = tableUpdate;
	}

	public boolean needsTableUpdate() {
		return needsTableUpdate;
	}

	public boolean isDirty() {
		return dirty_notes || dirty_details || needsTableUpdate;
	}

	public boolean hasBlackStatusChanged() {
		return blackStatusChanged;
	}

	public void setDetails(CacheHolder ch) {
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		inpWaypoint.setText(ch.getWayPoint());
		inpName.setText(ch.getCacheName());
		btnCoordinates.setText(ch.pos.toString());
		inpHidden.setText(ch.getDateHidden());
		inpOwner.setText(ch.getCacheOwner());
		if (ch.getCacheStatus().length() >= 10 && ch.getCacheStatus().charAt(4) == '-') {
			chcStatus.setText(MyLocale.getMsg(318, "Found") + " " + ch.getCacheStatus());
		} else {
			chcStatus.setText(ch.getCacheStatus());
			// If the cache status contains a date, do not overwrite it with
			// 'found' message
			if (ch.is_found() == true)
				chcStatus.setText(MyLocale.getMsg(318, "Found"));
		}
		chcType.setInt(CacheType.cw2GuiSelect(ch.getType()));
		if (ch.is_black()) {
			btnBlack.image = imgBlack;
		} else {
			btnBlack.image = imgBlackNo;
		}
		blackStatus = ch.is_black();
		blackStatusChanged = false;
		btnBlack.repaintNow();
		if (inpWaypoint.getText().length() == 0)
			createWptName();
		if (ch.has_bugs() == true) {
			// btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = imgShowBug;
		} else {
			// btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = imgShowBugNo;
		}
		btnShowBug.repaintNow();
		chcSize.setInt(ch.getCacheSize());

		attViewer.showImages(ch.getCacheDetails(true).attributes);
		if (ch.isAddiWpt() || ch.isCustomWpt()) {
			btnTerr.setText(MyLocale.getMsg(1001, "T")+": -.-");
			btnDiff.setText(MyLocale.getMsg(1000, "D")+": -.-");
			deactivateControl(btnTerr);
			deactivateControl(btnDiff);
			deactivateControl(chcSize);
			deactivateControl(inpOwner);
			deactivateControl(inpHidden);
			deactivateControl(btnShowBug);
			deactivateControl(btnBlack);
			chcSize.select(0);
		} else {
			activateControl(btnTerr);
			activateControl(btnDiff);
			activateControl(chcSize);
			activateControl(inpOwner);
			activateControl(inpHidden);
			activateControl(btnShowBug);
			activateControl(btnBlack);
			if (CacheTerrDiff.isValidTD(ch.getTerrain())) {
				btnTerr.setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(ch.getTerrain()));
			} else {
				btnTerr.setText("T: -.-");
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(ch.getWayPoint() + " has wrong terrain " + ch.getTerrain());
			}
			if (CacheTerrDiff.isValidTD(ch.getHard())) {
				btnDiff.setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(ch.getHard()));
			} else {
				btnDiff.setText("D: -.-");
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(ch.getWayPoint() + " has wrong difficulty " + ch.getHard());
			}
		}
		int addiCount = 0;
		if (ch.mainCache == null) {
			addiCount = ch.addiWpts.size();
		} else {
			addiCount = ch.mainCache.addiWpts.size();
		}
		lblAddiCount.setText(MyLocale.getMsg(1044, "Addis") + ": " + String.valueOf(addiCount));

		if (isBigScreen)
			waypointNotes.setText(ch.getExistingDetails().getCacheNotes());
	}

	/**
	 * if is addi -> returns the respective AddiWpt if is main -> returns the
	 * respective MainWpt
	 */
	public void createWptName() {
		String wpt = inpWaypoint.getText().toUpperCase();
		if (CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt())) 
				&& Global.mainTab.mainCache != null 
				&& (Global.mainTab.mainCache.startsWith("GC") 
						|| Global.mainTab.mainCache.startsWith("OC") 
						|| Global.mainTab.mainCache.startsWith("CW")) 
				&& wpt.startsWith("CW")) {
			// for what was this?:
			Global.mainTab.lastselected = Global.mainTab.mainCache; // I don't know exactly, but it's needed for creating a series of Addis

			inpWaypoint.setText(Global.getProfile().getNewAddiWayPointName(
					Global.mainTab.mainCache));
		}
		if (!CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt()))
				&& !(wpt.startsWith("GC") || wpt.startsWith("OC") || wpt
						.startsWith("CW"))) {
			inpWaypoint.setText(Global.getProfile().getNewWayPointName());
		}
	}

	/**
	 * Method to react to a user input.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof DataChangeEvent) {
			if (ev.target == inpWaypoint) {
				// If user used lower case -> convert directly to upper case
				inpWaypoint.setText(inpWaypoint.getText().toUpperCase());
				//FIXME: if name was changed, we should rename the waypoint.xml file. how? where?
			} else if (ev.target == chcType) {
				createWptName();
				if (CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.selectedIndex))) {
					activateControl(btnTerr);
					activateControl(btnDiff);
					activateControl(chcSize);
					activateControl(inpOwner);
					activateControl(inpHidden);
					activateControl(btnShowBug);
					activateControl(btnBlack);
				} else {
					deactivateControl(btnTerr);
					deactivateControl(btnDiff);
					deactivateControl(chcSize);
					deactivateControl(inpOwner);
					deactivateControl(inpHidden);
					deactivateControl(btnShowBug);
					deactivateControl(btnBlack);
					chcSize.select(0);
					btnTerr.setText(MyLocale.getMsg(1001, "T")+": -.-");
					btnDiff.setText(MyLocale.getMsg(1000, "D")+": -.-");
				}
			}
			//FIXME: check if something was actually changed, since datacachnge events also occur if you just hop through the fileds with the tab key (Why? don't know!)
			dirty_details = true;
			needsTableUpdate = true;
		}
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnNotes) {
				dirty_notes = true; // TODO I think this is redundant, because
									// the notes are saved separately by the notes screen itself
				NotesScreen nsc = new NotesScreen(thisCache
						.getCacheDetails(true));
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
				if (isBigScreen)
					waypointNotes.setText(thisCache.getCacheDetails(true).getCacheNotes());
			} else if (ev.target == btnShowMap) {
				Global.mainTab.SwitchToMovingMap(thisCache.pos, true);
				/*
				 * try { MapDetailForm mdf = new
				 * MapDetailForm(thisCache.wayPoint, pref, profile);
				 * mdf.execute(); } catch (IllegalArgumentException e) {
				 * MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Error"),
				 * MyLocale.getMsg(322,"Kann Bild/Karte nicht finden")+":
				 * "+e.getMessage(), MessageBox.OKB); tmp.exec(); }
				 */
			} else if (ev.target == btnShowBug) {
				// InfoScreen is = new InfoScreen(thisCache.Travelbugs.toHtml(),
				// "Travelbugs",
				// false, pref);
				// is.execute();
				TravelbugInCacheScreen ts = new TravelbugInCacheScreen(thisCache.getCacheDetails(true).Travelbugs.toHtml(),	"Travelbugs");
				ts.execute(this.getFrame(), Gui.CENTER_FRAME);
			} else if (ev.target == btnCenter) {
				CWPoint cp = new CWPoint(thisCache.LatLon);
				if (!cp.isValid()) {
					MessageBox tmpMB = new MessageBox(
							MyLocale.getMsg(312, "Error"),
							MyLocale.getMsg(4111, "Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"),
							FormBase.OKB);
					tmpMB.exec();
				} else {
					pref.curCentrePt.set(cp);
					Global.mainTab.updateBearDist();
				}
			} else if (ev.target == btnAddDateTime) {
				dirty_notes = true;
				String note = thisCache.getCacheDetails(true).getCacheNotes();
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if (note.length() > 0)
					note = note + "\n" + dtm.toString();
				else
					note = note + dtm.toString();
				note = note + "\n";
				thisCache.getCacheDetails(true).setCacheNotes(note);
				//FIXME: better use saveDirtyWaypoint()?
				thisCache.save();
			} else if (ev.target == btnAddPicture) {
				thisCache.getCacheDetails(true).addUserImage(profile);
			} else if (ev.target == btnBlack) {
				if (thisCache.is_black()) {
					thisCache.setBlack(false);
					btnBlack.image = imgBlackNo;
				} else {
					thisCache.setBlack(true);
					btnBlack.image = imgBlack;
				}
				blackStatus = thisCache.is_black();
				thisCache.setAttributesToAddiWpts();
				btnBlack.repaintNow();
				dirty_details = true;
				blackStatusChanged = true;
			} else if (ev.target == btnNewWpt) {
				CacheHolder ch = new CacheHolder();
				ch.LatLon = thisCache.LatLon;
				ch.pos = new CWPoint(thisCache.pos);
				ch.setType(CacheType.CW_TYPE_STAGE);
				ch.setHard(CacheTerrDiff.CW_DT_UNSET);
				ch.setTerrain(CacheTerrDiff.CW_DT_UNSET);
				ch.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				Global.mainTab.newWaypoint(ch);
			} else if (ev.target == btnGoto) {
				// FIXME: if something changed saveDirtyWaypoint();
				Global.mainTab.gotoP.setDestinationAndSwitch(thisCache);
			} else if (ev.target == btnCoordinates) {
				CWPoint coords = new CWPoint(btnCoordinates.getText(), CWPoint.CW);
				CoordsScreen cs = new CoordsScreen(true);
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute() == FormBase.IDOK) {
					dirty_details = true;
					coords = cs.getCoords();
					Global.getProfile().notifyUnsavedChanges(!thisCache.pos.toString().equals(coords.toString()));
					thisCache.pos.set(coords);
					btnCoordinates.setText(coords.toString());
					thisCache.LatLon = coords.toString();
					// If the current centre is valid, calculate the distance and bearing to it
					CWPoint centre = Global.getPref().curCentrePt;
					if (centre.isValid())
						thisCache.calcDistance(centre);
				}
			} else if (ev.target == btnFoundDate) {
				// DateChooser.dayFirst=true;
				DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
				dc.title = MyLocale.getMsg(328, "Date found");
				dc.setPreferredSize(240, 240);
				String foundDate = chcStatus.getText();
				if (foundDate.startsWith(MyLocale.getMsg(318, "Found") + " "))
					foundDate = foundDate.substring(MyLocale.getMsg(318, "Found").length() + 1);
				Time t = new Time();
				try {
					t.parse(foundDate, "y-M-d H:m");
				} catch (IllegalArgumentException e) {
					try {
						t.parse(foundDate, "y-M-d");
					} catch (IllegalArgumentException e1) {
						// No parsable date given - should not appear
					}
				}
				;
				dc.reset(t);
				if (dc.execute() == ewe.ui.FormBase.IDOK) {
					chcStatus.setText(MyLocale.getMsg(318, "Found") + " "
									+ Convert.toString(dc.year) + "-"
									+ MyLocale.formatLong(dc.month, "00") + "-"
									+ MyLocale.formatLong(dc.day, "00") + " "
									+ dc.time);
					dirty_details = true;
					// profile.hasUnsavedChanges=true;
				}
			} else if (ev.target == btnHiddenDate) {
				DateChooser.dayFirst = true;
				DateChooser dc = new DateChooser(Vm.getLocale());
				dc.title = MyLocale.getMsg(329, "Hidden date");
				dc.setPreferredSize(240, 240);
				if (inpHidden.getText().length() == 10)
					try {
						dc.setDate(new Time(Convert.parseInt(inpHidden
								.getText().substring(8)), Convert
								.parseInt(inpHidden.getText().substring(5, 7)),
								Convert.parseInt(inpHidden.getText().substring(
										0, 4))));
					} catch (NumberFormatException e) {
						dc.reset(new Time());
					}
				if (dc.execute() == ewe.ui.FormBase.IDOK) {
					inpHidden.setText(Convert.toString(dc.year) + "-"
							+ MyLocale.formatLong(dc.month, "00") + "-"
							+ MyLocale.formatLong(dc.day, "00"));
					dirty_details = true;
					// profile.hasUnsavedChanges=true;
				}
			} else if (ev.target == btnTerr) {
				int returnValue;
				TerrDiffForm tdf = new TerrDiffForm(true, 
						decodeTerrDiff(btnTerr, 
								MyLocale.getMsg(1001, "T"), 
								CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))
							)
						);
				returnValue = tdf.execute();
				if (returnValue == 1) {
					btnTerr.setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(tdf.getDT()));
					dirty_details = true;
				}
			} else if (ev.target == btnDiff) {
				int returnValue;
				TerrDiffForm tdf = new TerrDiffForm(false, 
						decodeTerrDiff(btnDiff, 
								MyLocale.getMsg(1001, "D"), 
								CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))
							)
						);
						
				returnValue = tdf.execute();
				if (returnValue == 1) {
					btnDiff.setText(MyLocale.getMsg(1000, "D") + ": "
							+ CacheTerrDiff.longDT(tdf.getDT()));
					dirty_details = true;
				}
			}
			ev.consumed = true;
		}
	}
	
	/** allow user input on control item */
	private void activateControl(Control ctrl) {
		if (ctrl.change(0, ControlConstants.Disabled))
			ctrl.repaint();
	}

	/** block user input on control item */
	private void deactivateControl(Control ctrl) {
		if (ctrl.change(ControlConstants.Disabled, 0))
			ctrl.repaint();
	}

	public void saveDirtyWaypoint() {
		//FIXME: here we should check if the data is now different from what it used to be when calling the details panel instead of relying on dirty flags
		//FIXME: take care of renaming waypoints
		//FIXME: add method to convert back text of difficulty & terrain buttons
		//FIXME: check if manual changes have converted a cache from incomplete to complete
		
		// We have to update two objects: thisCache (a CacheHolderDetail) which
		// contains
		// the full cache which will be written to the cache.xml file AND
		// the CacheHolder object which sits in cacheDB
		//FIXME: so how do we do this??
		
		// Strip the found message if the status contains a date
		if (chcStatus.getText().startsWith(MyLocale.getMsg(318, "Found"))
				&& chcStatus.getText().length() >= MyLocale
						.getMsg(318, "Found").length() + 11) {
			thisCache.setCacheStatus(chcStatus.getText().substring(
					MyLocale.getMsg(318, "Found").length() + 1));
		} else {
			thisCache.setCacheStatus(chcStatus.getText());
		}
		if (!thisCache.is_found() && thisCache.getCacheStatus().length() >= 10
				&& thisCache.getCacheStatus().charAt(4) == '-') {
			// Use same heuristic condition as in setDetails(CacheHolder) to
			// determine, if this
			// cache
			// has to considered as found.
			thisCache.setFound(true);
		} else {
			thisCache.setFound(chcStatus.getText().startsWith(
					MyLocale.getMsg(318, "Found")));
		}
		thisCache.setCacheOwner(inpOwner.getText().trim());
		thisCache.setOwned(thisCache.getCacheStatus().equals(
				MyLocale.getMsg(320, "Owner")));
		// Avoid setting is_owned if alias is empty and username is empty
		if (thisCache.is_owned() == false) {
			thisCache.setOwned((!pref.myAlias.equals("") && pref.myAlias
					.equals(thisCache.getCacheOwner()))
					|| (!pref.myAlias2.equals("") && pref.myAlias2
							.equals(thisCache.getCacheOwner())));
		}
		thisCache.setBlack(blackStatus);
		String oldWaypoint = thisCache.getWayPoint();
		thisCache.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
		thisCache.setCacheSize(CacheSize.guiSizeStrings2CwSize(chcSize
				.getText()));
		// If the waypoint does not have a name, give it one
		if (thisCache.getWayPoint().equals("")) {
			thisCache.setWayPoint(profile.getNewWayPointName());
		}
		// Don't allow single letter names=> Problems in updateBearingDistance
		// This is a hack but faster than slowing down the loop in
		// updateBearingDistance
		if (thisCache.getWayPoint().length() < 2)
			thisCache.setWayPoint(thisCache.getWayPoint() + " ");
		thisCache.setCacheName(inpName.getText().trim());
		thisCache.LatLon = thisCache.pos.toString();
		thisCache.setDateHidden(inpHidden.getText().trim());
		byte oldType = thisCache.getType();
		thisCache.setType(CacheType.guiSelect2Cw(chcType.getInt()));
		// thisCache.saveCacheDetails(profile.dataDir); // this is redundant,
		// because all changes
		// affecting the details are immediately saved
		// Now update the table
		
		thisCache.checkIncomplete();
		
		/*
		 * The references have to be rebuilt if: - the cachetype changed from
		 * addi->normal or normal->addi - the old cachetype or the new cachetype
		 * were 'addi' and the waypointname has changed
		 */
		if (CacheType.isAddiWpt(thisCache.getType()) != CacheType.isAddiWpt(oldType)
				|| ((CacheType.isAddiWpt(thisCache.getType()) || CacheType
						.isAddiWpt(oldType)) && !thisCache.getWayPoint()
						.equals(oldWaypoint))) {
			// If we changed the type to addi, check that a parent exists
			//FIXME: if cache was renamed we need to rebuild CacheDB.hashDB first
			if (CacheType.isAddiWpt(thisCache.getType())) {
				profile.setAddiRef(thisCache);
			} else {
				// rebuild links between caches
				profile.buildReferences();
			}
		} else {
			// set status also on addi wpts
			thisCache.setAttributesToAddiWpts();
		}
		thisCache.setHard(decodeTerrDiff(btnDiff,MyLocale.getMsg(1000, "D"),thisCache.isCacheWpt()));
		thisCache.setTerrain(decodeTerrDiff(btnTerr,MyLocale.getMsg(1001, "T"),thisCache.isCacheWpt()));
		dirty_notes = false;
		dirty_details = false;
		setNeedsTableUpdate(false);
		thisCache.getFreshDetails().hasUnsavedChanges = true;
	}
	
	/**
	 * convert the string displayed in the terrain in difficulty buttons to a byte for intrernal use<br>
	 * assumes that the relevant information will at positions 3 and 5 in a 0 indexed string
	 * @param button button control to get the text from 
	 * @param td localized string for abbreviation of terrain or difficulty
	 * @param isCache true if waypoint is a cache, false for addis and custom
	 * @return 0 for additional or custum waypoints, -1 for caches if td is not valid, parsed byte otherwise
	 */
	private byte decodeTerrDiff(mButton button, String td, boolean isCache) {
		StringBuffer tdv = new StringBuffer(2);
		
		// terrain and difficulty are always unset for non cache waypoints
		if (! isCache) return CacheTerrDiff.CW_DT_UNSET;
		
		// cut off beginning of string
		String buttonText = button.getText().substring(td.length()+2);
		// we now should have a string of length 3
		if (buttonText.length() != 3) return -1;

		buttonText=tdv.append(buttonText.charAt(0)).append(buttonText.charAt(2)).toString();

		// unset value is invalid
		if (buttonText.equals("--")) return CacheTerrDiff.CW_DT_ERROR;
		
		return Byte.parseByte(buttonText);
	}

	private class TravelbugInCacheScreen extends Form {

		private DispPanel disp = new DispPanel();
		private mButton btCancel;
		private TravelbugJourneyList tbjList;

		TravelbugInCacheScreen(String text, String title) {
			this.setTitle(title);
			this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
			disp.setHtml(text);
			ScrollBarPanel sbp = new MyScrollBarPanel(disp,
					ScrollablePanel.NeverShowHorizontalScrollers);
			this.addLast(sbp);
			this.addLast(
					btCancel = new mButton(MyLocale.getMsg(3000, "Close")),
					CellConstants.DONTSTRETCH, CellConstants.FILL);
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
			MenuItem mnuPickupTB, mnuDropTB;
			MenuItem[] TBMenuItems = new MenuItem[2];
			Menu mnuPopup;

			DispPanel() {
				TBMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(
						6016, "Pick up Travelbug"));
				TBMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6017,
						"Drop Travelbug"));
				mnuPopup = new Menu(TBMenuItems, "");
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
				if (selectedItem == mnuPickupTB) {
					Travelbug tb = TravelbugPickup.pickupTravelbug(thisCache.getCacheDetails(true).Travelbugs);
					if (tb != null) {
						dirty_details = true;
						// Get the list of my travelbugs
						tbjList = new TravelbugJourneyList();
						tbjList.readTravelbugsFile();
						// Add the tb to this list
						tbjList.addTbPickup(tb, Global.getProfile().name,
								thisCache.getWayPoint());
						tbjList.saveTravelbugsFile();
						tbjList = null;
						setHtml(thisCache.getCacheDetails(true).Travelbugs.toHtml());
						repaint();
						thisCache.setHas_bugs(thisCache.getCacheDetails(true).Travelbugs.size() > 0);
					}
				} else if (selectedItem == mnuDropTB) {
					tbjList = new TravelbugJourneyList();
					tbjList.readTravelbugsFile();
					TravelbugList tbl = tbjList.getMyTravelbugs();
					TravelbugScreen tbs = new TravelbugScreen(tbl, MyLocale
							.getMsg(6017, "Drop a travelbug"), false);
					tbs.execute();
					if (tbs.selectedItem >= 0) {
						Travelbug tb = tbl.getTB(tbs.selectedItem);
						thisCache.getCacheDetails(true).Travelbugs.add(tb);
						tbjList.addTbDrop(tb, Global.getProfile().name,
								thisCache.getWayPoint());
					}
					tbjList.saveTravelbugsFile();
					tbjList = null;
					thisCache.setHas_bugs(thisCache.getCacheDetails(true).Travelbugs.size() > 0);
					setHtml(thisCache.getCacheDetails(true).Travelbugs.toHtml());
					repaint();
					dirty_details = true;
				} else
					super.popupMenuEvent(selectedItem);
			}
		}
	}

	private class TerrDiffForm extends Form {
		private mChoice mcDT;
		private mButton btnOk, btnCancel;
		private String[] DT = new String[] { "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };

		public TerrDiffForm(boolean terrain, int startVal) {
			mcDT = new mChoice(DT, (startVal > 0) ? (startVal - 10) / 5 : 0);
			btnOk = new mButton(MyLocale.getMsg(1605, "OK"));
			btnCancel = new mButton(MyLocale.getMsg(1604, "Cancel"));

			resizable = false;
			setTitle(MyLocale.getMsg(31415, "D & T"));

			addNext(new mLabel(terrain ? MyLocale.getMsg(31415, "Terrain")
					: MyLocale.getMsg(31415, "Difficulty")));
			addLast(mcDT);
			addButton(btnOk);
			addButton(btnCancel);
		}

		public void onEvent(Event ev) {
			if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
				if (ev.target == btnCancel) {
					close(-1);
				} else if (ev.target == btnOk) {
					close(1);
				}
			}
		}

		public byte getDT() {
			return (byte) (mcDT.selectedIndex * 5 + 10);
		}
	}
}
