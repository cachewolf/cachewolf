package CacheWolf;

import CacheWolf.navi.TransformCoordinates;
import CacheWolf.InputScreen;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.fx.mImage;
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
	/** set center to current way point. */
	// private static mButton btnCenter;
	/** add time stamp to notes. */
	private static mButton btnAddDateTime;
	/** create a new way point. */
	private static mButton btnNewWpt;
	/** show details for travel bus in way point. */
	private static mButton btnShowBug;
	/** switch to moving map and center on way point coordinates. */
	private static mButton btnShowMap;
	/** set way point as destination and switch to goto panel. */
	private static mButton btnGoto;
	/** add a user picture to way point. */
	private static mButton btnAddPicture;
	/** toggle blacklist status. */
	private static mButton btnBlack;
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
	/** FIXME move to image broker? */
	private static mImage imgBlack, imgBlackNo, imgShowBug, imgShowBugNo, imgNewWpt, imgGoto, imgNotes;

	// ===== data handles =====
	/** waypoint to be displayed. */
	public CacheHolder cache;
	/** panel to display waypoint attributes */
	private static AttributesViewer attViewer;
	/** preferences object. */
	private static Preferences pref;
	/** waypoint profile. */
	private static Profile profile;

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
	/** screen is VGA or better */
	private final boolean isBigScreen;
	/** use big icons */
	private final boolean useBigIcons;
	/** String to display for invalid or not applicable terrain or difficulty values.*/
	private final static String DTINVALID = ": -.-";
	public boolean evWaypointChanged=false;
	private String warnedForWaypoint="";

    // TODO: move images to image broker
    //mImage imgBlack, imgBlackNo, imgShowBug, imgShowBugNo, imgNewWpt, imgGoto, imgShowMaps, imgAddImages, imgNotes;

	/**
	 * public constructor for detail panels. should only be called from main tab.
	 */
	public DetailsPanel() {
		super();
        // ===== local objects =====
		/** helper panels to organize layout */
		CellPanel helperPanel1, helperPanel2, helperPanel3, helperPanel4, helperPanel5;

		// ===== initialize data handles =====
		pref = Global.getPref();
		profile = Global.getProfile();

		// ===== initialize flags =====
		dirtyNotes = false;
		dirtyDetails = false;
		blackStatus = false;
		blackStatusChanged = false;
		needsTableUpdate = false;
		isBigScreen = pref.isBigScreen;
		useBigIcons = pref.useBigIcons;

		// ===== initialize GUI objects =====
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
		chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(),0);
		inpWaypoint = new mInput();
		inpName = new mInput();

		btnCoordinates = new mButton();
		btnCoordinates.setToolTip(MyLocale.getMsg(31415, "Edit coordinates"));

		inpOwner = new mInput();
		inpHidden = new mInput();
		inpHidden.modifyAll(DisplayOnly, 0);

		// ===== put the controls onto the GUI =====

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
		addLast(createToolsPanel(), CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN, new Dimension(3, 1));

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

	private CellPanel createToolsPanel() {
		final CellPanel pnlTools = new CellPanel();

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

		btnAddPicture = new mButton(new mImage(useBigIcons ? "images_vga.gif" : "images.gif"));
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

		pnlTools.addNext(btnNewWpt);
		pnlTools.addNext(btnGoto);
		pnlTools.addNext(btnShowBug);
		pnlTools.addNext(btnShowMap);
		pnlTools.addNext(btnAddPicture);
		pnlTools.addNext(btnBlack);
		pnlTools.addNext(btnNotes);
		pnlTools.addLast(btnAddDateTime);

		pnlTools.stretchFirstRow = true;

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
		btnCoordinates.setText(ch.pos.toString());
		inpHidden.setText(mainCache.getDateHidden());
		inpOwner.setText(mainCache.getCacheOwner());

		if ((cache.getCacheStatus().length() == 10 || cache.getCacheStatus().length() == 16) &&
				cache.getCacheStatus().charAt(4) == '-') {
			chcStatus.setText(MyLocale.getMsg(318, "Found") + " " + ch.getCacheStatus());
		} else {
			chcStatus.setText(ch.getCacheStatus());
			// If the cache status contains a date, do not overwrite it with
			// 'found' message
			if (ch.is_found()) {
				chcStatus.setText(MyLocale.getMsg(318, "Found"));
			}
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
		if (inpWaypoint.getText().length() == 0) {
			createWptName();
		}
		if (mainCache.has_bugs()) {
			// btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = imgShowBug;
		} else {
			// btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = imgShowBugNo;
		}
		btnShowBug.repaintNow();
		chcSize.setInt(mainCache.getCacheSize());

		attViewer.showImages(ch.getCacheDetails(true).attributes);
		if (ch.isAddiWpt() || ch.isCustomWpt()) {
			deactivateControl(btnTerr);
			deactivateControl(btnDiff);
			deactivateControl(chcSize);
			deactivateControl(inpOwner);
			deactivateControl(inpHidden);
			deactivateControl(btnShowBug);
			deactivateControl(btnBlack);
		} else {
			activateControl(btnTerr);
			activateControl(btnDiff);
			activateControl(chcSize);
			activateControl(inpOwner);
			activateControl(inpHidden);
			activateControl(btnShowBug);
			activateControl(btnBlack);
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
				if (Global.getPref().debug) {
					Global.getPref().log(mainCache.getWayPoint() + " has wrong terrain " + mainCache.getTerrain());
				}
			}
			if (CacheTerrDiff.isValidTD(mainCache.getHard())) {
				btnDiff.setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(mainCache.getHard()));
			} else {
				btnDiff.setText("D: -.-");
				mainCache.setIncomplete(true);
				if (Global.getPref().debug) {
					Global.getPref().log(mainCache.getWayPoint() + " has wrong difficulty " + mainCache.getHard());
				}
			}
		}

		int addiCount = 0;
		if (ch.mainCache == null) {
			addiCount = ch.addiWpts.size();
		} else {
			addiCount = ch.mainCache.addiWpts.size();
		}
		lblAddiCount.setText(MyLocale.getMsg(1044, "Addis") + ": " + addiCount);

		if (isBigScreen) {
			waypointNotes.setText(ch.getCacheDetails(true).getCacheNotes());
		}
	}

	/**
	 * if is addi -> returns the respective AddiWpt if is main -> returns the
	 * respective MainWpt
	 */
	public void createWptName() {
		final String wpt = inpWaypoint.getText().toUpperCase();
		if (CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt()))
				&& Global.mainTab.mainCache != null
				&& (Global.mainTab.mainCache.startsWith("GC")
					|| OC.isOC(Global.mainTab.mainCache) || Global.mainTab.mainCache.startsWith("CW"))
				&& wpt.startsWith("CW")) {
			// for what was this?:
			Global.mainTab.lastselected = Global.mainTab.mainCache; // I don't know exactly, but it's needed for creating a series of Addis

			inpWaypoint.setText(Global.getProfile().getNewAddiWayPointName(
					Global.mainTab.mainCache));
		}
		if (!CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt()))
				&& !(wpt.startsWith("GC")
					|| OC.isOC(wpt) || wpt.startsWith("CW"))) {
			inpWaypoint.setText(Global.getProfile().getNewWayPointName());
		}
	}

    /**
     * Method to react to a user input.
     */
    public void onEvent(final Event ev) {
        if (ev instanceof DataChangeEvent) {
            if (ev.target == inpWaypoint) {
                if (evWaypointChanged) {
                    String iTmp=inpWaypoint.getText();
                    String uTmp=iTmp.toUpperCase();
                    if (!iTmp.equals(uTmp)){
                        inpWaypoint.setText(uTmp); // If user entered LowerCase -> convert directly to UpperCase
                        evWaypointChanged=false; //next DataChangeEvent fired by change to UpperCase will be ignored
                    }
                    // already warned(multi same DataChangeEvents) or same waypointname as before edit !!!
                    if(!warnedForWaypoint.equals(uTmp) && !uTmp.equals(this.cache.getWayPoint())){
                        if ((new File(profile.dataDir + iTmp.toLowerCase()+".xml")).exists()) {
                            warnedForWaypoint=uTmp; // before MessageBox cause Multithread DataChangeEvents
                            // filename is LowerCase
                            new MessageBox("Warning :",uTmp+"\n"+MyLocale.getMsg(275,"Waypoint already exists!"),MessageBox.OKB).execute();
                            // revert waypointname
                            inpWaypoint.setText(this.cache.getWayPoint());
                        }
                    }
                }
                else {
                    // first DataChangeEvent is fired by Klick into (after reload).
                    // that really didn't change anything
                    evWaypointChanged=true;
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
                    btnTerr.setText(MyLocale.getMsg(1001, "T") + DTINVALID);
                    btnDiff.setText(MyLocale.getMsg(1000, "D") + DTINVALID);
                }
            }
            //FIXME: check if something was actually changed, since datacachnge events also occur if you just hop through the fileds with the tab key (Why? don't know!)
            dirtyDetails = true;
            needsTableUpdate = true;
        }
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == btnNotes) {
                dirtyNotes = true; // TODO I think this is redundant, because
                                    // the notes are saved separately by the notes screen itself
                final NotesScreen nsc = new NotesScreen(cache
                        .getCacheDetails(true));
                nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
                if (isBigScreen) {
                    waypointNotes.setText(cache.getCacheDetails(true).getCacheNotes());
                }
            } else if (ev.target == btnShowMap) {
                Global.mainTab.SwitchToMovingMap(cache.pos, true);
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
                final TravelbugInCacheScreen ts = new TravelbugInCacheScreen(cache.getCacheDetails(true).Travelbugs.toHtml(),	"Travelbugs");
                ts.execute(this.getFrame(), Gui.CENTER_FRAME);
            /* not fully implemented
            } else if (ev.target == btnCenter) {
                final CWPoint cp = new CWPoint(cache.LatLon);
                if (cp.isValid()) {
                    pref.setCurCentrePt(cp);
                } else {
                    final MessageBox tmpMB = new MessageBox(
                            MyLocale.getMsg(312, "Error"),
                            MyLocale.getMsg(4111, "Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"),
                            FormBase.OKB);
                    tmpMB.exec();
                }
            */

            } else if (ev.target == btnAddDateTime) {
                dirtyNotes = true;

                final StringBuffer newNote = new StringBuffer();
                newNote.append(cache.getCacheDetails(true).getCacheNotes());

                final Time dtm = new Time();
                dtm.getTime();
                dtm.setFormat("E dd.MM.yyyy '/' HH:mm");

                if (newNote.length() > 0) {
                    newNote.append('\n');
                }
                newNote.append(dtm.toString()).append('\n');

                cache.getCacheDetails(true).setCacheNotes(newNote.toString());
                //FIXME: better use saveDirtyWaypoint()?
                cache.save();
            } else if (ev.target == btnAddPicture) {
                cache.getCacheDetails(true).addUserImage(profile);
            } else if (ev.target == btnBlack) {
                if (cache.is_black()) {
                    cache.setBlack(false);
                    btnBlack.image = imgBlackNo;
                } else {
                    cache.setBlack(true);
                    btnBlack.image = imgBlack;
                }
                blackStatus = cache.is_black();
                cache.setAttributesToAddiWpts();
                btnBlack.repaintNow();
                dirtyDetails = true;
                blackStatusChanged = true;
            } else if (ev.target == btnNewWpt) {
                final CacheHolder ch = new CacheHolder();
                ch.setLatLon(cache.getLatLon());
                ch.pos = new CWPoint(cache.pos);
                ch.setType(CacheType.CW_TYPE_STAGE);
                ch.setHard(CacheTerrDiff.CW_DT_UNSET);
                ch.setTerrain(CacheTerrDiff.CW_DT_UNSET);
                ch.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
                Global.mainTab.newWaypoint(ch);
            } else if (ev.target == btnGoto) {
                // FIXME: if something changed saveDirtyWaypoint();
                Global.mainTab.gotoP.setDestinationAndSwitch(cache);
            } else if (ev.target == btnCoordinates) {
                CWPoint coords = new CWPoint(btnCoordinates.getText(), TransformCoordinates.CW);
                if(Vm.isMobile()){
                    InputScreen InScr = new InputScreen(TransformCoordinates.CW, true);
                    if (coords.isValid())	InScr.setCcords(coords);
                        else InScr.setCcords(new CWPoint(0,0));
                    if (InScr.execute(null, CellConstants.TOP) == FormBase.IDOK)
                    {
                        dirtyDetails = true;
                        coords = InScr.getCoords();
                        Global.getProfile().notifyUnsavedChanges(!cache.pos.toString().equals(coords.toString()));
                        cache.pos.set(coords);
                        btnCoordinates.setText(coords.toString());
                        cache.setLatLon(coords.toString());
                        // If the current centre is valid, calculate the distance and bearing to it
                        final CWPoint centre = Global.getPref().getCurCentrePt();
                        if (centre.isValid()) {
                            cache.calcDistance(centre); // todo perhaps sortTable
                        }
                    }
                }else{
                    final CoordsScreen cs = new CoordsScreen(true);
                    cs.setFields(coords, TransformCoordinates.CW);
                    if (cs.execute() == FormBase.IDOK) {
                        dirtyDetails = true;
                        coords = cs.getCoords();
                        Global.getProfile().notifyUnsavedChanges(!cache.pos.toString().equals(coords.toString()));
                        cache.pos.set(coords);
                        btnCoordinates.setText(coords.toString());
                        cache.setLatLon(coords.toString());
                        // If the current centre is valid, calculate the distance and bearing to it
                        final CWPoint centre = Global.getPref().getCurCentrePt();
                        if (centre.isValid()) {
                            cache.calcDistance(centre); // todo perhaps sortTable
                        }
                    }
                }
            } else if (ev.target == btnFoundDate) {
                // DateChooser.dayFirst=true;
                final DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
                dc.title = MyLocale.getMsg(328, "Date found");
                dc.setPreferredSize(240, 240);
                String foundDate = chcStatus.getText();
                if (foundDate.startsWith(MyLocale.getMsg(318, "Found") + " ")) {
                    foundDate = foundDate.substring(MyLocale.getMsg(318, "Found").length() + 1);
                }
                else if (foundDate.endsWith(MyLocale.getMsg(319, "not Found"))) {
                    foundDate = foundDate.substring(0,foundDate.length()-MyLocale.getMsg(319, "not Found").length());
                    dc.title=MyLocale.getMsg(330,"Date Not Found");
                }
                foundDate=foundDate.trim();
                if (foundDate.length()>0 && foundDate.indexOf('-')>0) { //Don't try and parse empty date
                    final Time t = new Time();
                    try {
                        t.parse(foundDate, "y-M-d H:m");
                    } catch (IllegalArgumentException e) {
                        try {
                            t.parse(foundDate, "y-M-d");
                        } catch (IllegalArgumentException e1) {
                            Global.getPref().log("No parsable date given - should not appear ("+foundDate+")", e1, true);
                        }
                    }

                    dc.reset(t);
                }
                // We can create a not found log with date in two ways:
                //   1) Exiting the date-time dialog by clicking the x if the status is empty (somewhat
                //      non-standard but quick and dirty)
                //   2) Exiting the date-time dialog by clicking the tick. Then we check whether
                //      the status field was preset with the not-found text. If yes it stays a not found
                //      but the date is prepended
                //TODO: The functions for extracting the date and the found/not-found text should not be in the GUI
                int retCode=dc.execute();
                if (retCode == ewe.ui.FormBase.IDOK && !chcStatus.getText().endsWith(MyLocale.getMsg(319, "not Found"))) {
                    chcStatus.setText(MyLocale.getMsg(318, "Found") + " "
                                    + Convert.toString(dc.year) + "-"
                                    + MyLocale.formatLong(dc.month, "00") + "-"
                                    + MyLocale.formatLong(dc.day, "00") + " "
                                    + dc.time);
                    dirtyDetails = true;
                }
                else if (chcStatus.getText().length()==0 ||
                		(retCode==ewe.ui.FormBase.IDOK && chcStatus.getText().endsWith(MyLocale.getMsg(319, "not Found")) )) {
                    chcStatus.setText(Convert.toString(dc.year) + "-"
                            + MyLocale.formatLong(dc.month, "00") + "-"
                            + MyLocale.formatLong(dc.day, "00") + " "
                            + dc.time + " "
                            + MyLocale.getMsg(319, "not Found")
                            );
                    dirtyDetails = true;
                }
            } else if (ev.target == btnHiddenDate) {
                DateChooser.dayFirst = true;
                final DateChooser dc = new DateChooser(Vm.getLocale());
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
                    dirtyDetails = true;
                    // profile.hasUnsavedChanges=true;
                }
            } else if (ev.target == btnTerr) {
                int returnValue;
                final TerrDiffForm tdf = new TerrDiffForm(true,
                        decodeTerrDiff(btnTerr,
                                MyLocale.getMsg(1001, "T"),
                                CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))
                            )
                        );
                returnValue = tdf.execute();
                if (returnValue == 1) {
                    btnTerr.setText(MyLocale.getMsg(1001, "T") + ": " + CacheTerrDiff.longDT(tdf.getDT()));
                    dirtyDetails = true;
                }
            } else if (ev.target == btnDiff) {
                int returnValue;
                final TerrDiffForm tdf = new TerrDiffForm(false,
                        decodeTerrDiff(btnDiff,
                                MyLocale.getMsg(1001, "D"),
                                CacheType.isCacheWpt(CacheType.guiSelect2Cw(chcType.getInt()))
                            )
                        );

                returnValue = tdf.execute();
                if (returnValue == 1) {
                    btnDiff.setText(MyLocale.getMsg(1000, "D") + ": "
                            + CacheTerrDiff.longDT(tdf.getDT()));
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
		if (chcStatus.getText().startsWith(MyLocale.getMsg(318, "Found")) &&
			chcStatus.getText().length() >= MyLocale.getMsg(318, "Found").length() + 11) {
			cache.setCacheStatus(chcStatus.getText().substring(MyLocale.getMsg(318, "Found").length() + 1));
		} else {
			cache.setCacheStatus(chcStatus.getText());
		}

		if (chcStatus.getText().startsWith(MyLocale.getMsg(318, "Found")) ||
			chcStatus.getText().startsWith(MyLocale.getMsg(355, "Attended")) ||
			(cache.getCacheStatus().length() == 10 || cache.getCacheStatus().length() == 16) &&
			cache.getCacheStatus().charAt(4) == '-') {
			// Use same heuristic condition as in setDetails(CacheHolder) to
			// determine, if this
			// cache
			// has to considered as found.
			cache.setFound(true);
		}
		else cache.setFound(false);

		if (!cache.isAddiWpt()) {
			cache.setCacheOwner(inpOwner.getText().trim());
		}
		cache.setOwned(cache.getCacheStatus().equals(
				MyLocale.getMsg(320, "Owner")));
		// Avoid setting is_owned if alias is empty and username is empty
		if (!cache.is_owned()) {
			cache.setOwned((!pref.myAlias.equals("") && pref.myAlias
					.equals(cache.getCacheOwner()))
					|| (!pref.myAlias2.equals("") && pref.myAlias2
							.equals(cache.getCacheOwner())));
		}
		cache.setBlack(blackStatus);
		final String oldWaypoint = cache.getWayPoint();
		cache.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
		if (!cache.isAddiWpt()) {
			cache.setCacheSize(CacheSize.guiSizeStrings2CwSize(chcSize.getText()));
		}
		// If the waypoint does not have a name, give it one
		if (cache.getWayPoint().equals("")) {
			cache.setWayPoint(profile.getNewWayPointName());
		}
		// Don't allow single letter names=> Problems in updateBearingDistance
		// This is a hack but faster than slowing down the loop in
		// updateBearingDistance
		if (cache.getWayPoint().length() < 2)
			cache.setWayPoint(cache.getWayPoint() + " ");
		cache.setCacheName(inpName.getText().trim());
		cache.setLatLon(cache.pos.toString());
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
		 * The references have to be rebuilt if:
		 * - the cachetype changed from addi->normal or from normal->addi
		 * - the old cachetype or the new cachetype were 'addi' and the waypointname has changed
		 */
		if (CacheType.isAddiWpt(cache.getType()) != CacheType.isAddiWpt(oldType) ||
			((CacheType.isAddiWpt(cache.getType()) || CacheType.isAddiWpt(oldType)) && !cache.getWayPoint().equals(oldWaypoint))) {
			// If we changed the type to addi, check that a parent exists
			//FIXME: if cache was renamed we need to rebuild CacheDB.hashDB first
			profile.buildReferences();
		} else {
			// set status also on addi wpts
			cache.setAttributesToAddiWpts();
		}
		if (!cache.isAddiWpt()) {
			cache.setHard(decodeTerrDiff(btnDiff,MyLocale.getMsg(1000, "D"),cache.isCacheWpt()));
			cache.setTerrain(decodeTerrDiff(btnTerr,MyLocale.getMsg(1001, "T"),cache.isCacheWpt()));
		}
		dirtyNotes = false;
		dirtyDetails = false;
		needsTableUpdate = false;
		cache.getCacheDetails(false).hasUnsavedChanges = true;
		if (!oldWaypoint.equals(cache.getWayPoint())){
		 // Delete old XML - File
			cache.getCacheDetails(false).deleteFile(Global.getProfile().dataDir+oldWaypoint+".xml");
		}
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
		// terrain and difficulty are always unset for non cache waypoints
		if (! isCache) return CacheTerrDiff.CW_DT_UNSET;

		// cut off beginning of string
		String buttonText = button.getText().substring(td.length()+2);
		// we now should have a string of length 3
		if (buttonText.length() != 3) return -1;

		final StringBuffer tdv = new StringBuffer(2);
		buttonText=tdv.append(buttonText.charAt(0)).append(buttonText.charAt(2)).toString();

		// unset value is invalid
		if ("--".equals(buttonText)) return CacheTerrDiff.CW_DT_ERROR;

		return Byte.parseByte(buttonText);
	}

	private class TravelbugInCacheScreen extends Form {

		private final DispPanel disp = new DispPanel();
		private final mButton btCancel;


		TravelbugInCacheScreen(String text, String title) {
			super();
			this.setTitle(title);
			this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
			disp.setHtml(text);
			final ScrollBarPanel sbp = new MyScrollBarPanel(disp,
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
			private final MenuItem mnuPickupTB, mnuDropTB;
			private final MenuItem[] tbMenuItems = new MenuItem[2];
			private final Menu mnuPopup;

			DispPanel() {
				super();
				tbMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(
						6016, "Pick up Travelbug"));
				tbMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6017,
						"Drop Travelbug"));
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
						tbjList.addTbPickup(tb, Global.getProfile().name,
								cache.getWayPoint());
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
					TravelbugScreen tbs = new TravelbugScreen(tbl, MyLocale
							.getMsg(6017, "Drop a travelbug"), false);
					tbs.execute();
					if (tbs.selectedItem >= 0) {
						Travelbug tb = tbl.getTB(tbs.selectedItem);
						cache.getCacheDetails(true).Travelbugs.add(tb);
						tbjList.addTbDrop(tb, Global.getProfile().name,
								cache.getWayPoint());
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
		private final mButton btnOk, btnCancel;
		private final String[] DT = new String[] { "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };

		public TerrDiffForm(boolean terrain, int startVal) {
			super();
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
