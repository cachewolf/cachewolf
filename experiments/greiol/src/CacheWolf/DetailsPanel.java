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
 * Class to create the panel to show the cache details.<br>
 * Also allows for creation of a custom waypoint.<br>
 */
public class DetailsPanel extends CellPanel {

	// ===== GUI elements =====
	/** waypoint id */
	private static mInput inpWaypoint;
	/** waypoint name */
	private static mInput inpName;
	/** waypoint hidden date */
	private static mInput inpHidden;
	/** waypoint owner */
	private static mInput inpOwner;
	/** waypoint coordinates, open change coordinates dialog */
	private static mButton btnCoordinates;
	/** FIXME */
	private static mButton btnCenter;
	/** add time stamp to notes */
	private static mButton btnAddDateTime;
	/** FIXME */
	private static mButton btnNewWpt;
	/** show details for travelbus in waypoint */
	private static mButton btnShowBug;
	/** FIXME */
	private static mButton btnShowMap;
	/** FIXME */
	private static mButton btnGoto;
	/** FIXME */
	private static mButton btnAddPicture;
	/** toggle blacklist status */
	private static mButton btnBlack;
	/** add or edit notes for waypoint */
	private static mButton btnNotes;
	/** FIXME */
	private static mButton btnFoundDate;
	/** FIXME */
	private static mButton btnHiddenDate;
	/** FIXME */
	private static mButton btnTerr;
	/** FIXME */
	private static mButton btnDiff;
	/** drop down list with cache types */
	private static mChoice chcType;
	/** drop down list with container sizes */
	private static mChoice chcSize;
	/** FIXME */
	private static mComboBox chcStatus;
	/** FIXME */
	private static CellPanel pnlTools;
	/** FIXME */
	private static mTextPad cacheNotes;
	/** FIXME */
	private static mLabel lblAddiCount;
	/** FIXME move to image broker?*/
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
//	private boolean dirty_details;
	/** cache is blacklisted FIXME: make this obsolete*/
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
//		dirty_details = false;
		blackStatus = false;
		blackStatusChanged = false;
		needsTableUpdate = false;
		isBigScreen = pref.isBigScreen;
		useBigIcons = pref.useBigIcons;

		// ===== initialize GUI objects =====

		// ----- tools panel ------
		pnlTools = new CellPanel();
		btnNewWpt = new mButton(imgNewWpt = new mImage(useBigIcons?"newwpt_vga.png":"newwpt.png"));
		//FIXME: get an image with proper transparency
		imgNewWpt.transparentColor = new Color(255, 0, 0);
		btnNewWpt.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));

		btnGoto = new mButton(imgGoto = new mImage(useBigIcons?"goto_vga.png":"goto.png"));
		//FIXME: get an image with proper transparency
		imgGoto.transparentColor = Color.White;
		btnGoto.setToolTip(MyLocale.getMsg(345, "Goto these coordinates"));

		btnShowBug = new mButton(new mImage(useBigIcons?"bug_no_vga.gif":"bug_no.gif"));
		btnShowBug.setToolTip(MyLocale.getMsg(346, "Show travelbugs"));

		btnShowMap = new mButton(new mImage(useBigIcons?"globe_small_vga.gif":"globe_small.gif"));
		btnShowMap.setToolTip(MyLocale.getMsg(347, "Show map"));

		btnAddPicture = new mButton(imgAddImages = new mImage(useBigIcons?"images_vga.gif":"images.gif"));
		btnAddPicture.setToolTip(MyLocale.getMsg(348, "Add user pictures"));

		btnBlack = new mButton(imgBlack = new mImage(useBigIcons?"no_black_vga.png":"no_black.png"));
		//FIXME: get an image with proper transparency
		imgBlack.transparentColor = Color.Black;
		btnBlack.setToolTip(MyLocale.getMsg(349, "Toggle Blacklist status"));

		btnNotes = new mButton(imgNotes = new mImage(useBigIcons?"notes_vga.gif":"notes.gif"));
		//FIXME: get an image with proper transparency
		imgNotes.transparentColor = Color.DarkBlue;
		btnNotes.setToolTip(MyLocale.getMsg(351, "Add/Edit notes"));

		btnAddDateTime = new mButton(new mImage(useBigIcons?"date_time_vga.gif":"date_time.gif"));
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
				MyLocale.getMsg(319, "Not Found"), MyLocale.getMsg(320, "Owner") },
				0);
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

		btnDiff = new mButton(MyLocale.getMsg(1000, "D")	+ ": 5.5");
		btnDiff.setPreferredSize(pref.fontSize * 3, chcSize.getPreferredSize(null).height);
		btnDiff.setToolTip(MyLocale.getMsg(31415, "Edit difficulty"));

		btnTerr = new mButton(MyLocale.getMsg(1001, "T")	+ ": 5.5");
		btnTerr.setPreferredSize(pref.fontSize * 3, chcSize.getPreferredSize(null).height);
		btnTerr.setToolTip(MyLocale.getMsg(31415, "Edit terrain"));

		lblAddiCount = new mLabel(MyLocale.getMsg(1044, "Addis") + ": 888");

		btnFoundDate = new mButton(new mImage(useBigIcons?"calendar_vga.png":"calendar.png"));
		btnFoundDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));

		btnHiddenDate = new mButton(new mImage(useBigIcons?"calendar_vga.png":"calendar.png"));
		btnHiddenDate.setToolTip(MyLocale.getMsg(31415, "Set hidden date"));

		helperPanel1.addNext(chcType, CellConstants.HSTRETCH,	(CellConstants.HFILL | CellConstants.WEST));
		helperPanel1.addLast(btnDiff, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));		

		helperPanel2.addNext(chcSize, CellConstants.HSTRETCH,	(CellConstants.HFILL | CellConstants.WEST));
		helperPanel2.addLast(btnTerr, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));

		helperPanel3.addNext(inpWaypoint, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel3.addLast(lblAddiCount, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.EAST));

		helperPanel4.addNext(chcStatus, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel4.addLast(btnFoundDate, DONTSTRETCH, DONTFILL);

		helperPanel5.addNext(inpHidden, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		helperPanel5.addLast(btnHiddenDate, DONTSTRETCH, DONTFILL);

		// ----- main body -----
		addLast(pnlTools, CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN, new Dimension(3, 1));

		addNext(new mLabel(MyLocale.getMsg(300, "Type:")),	CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.NORTHWEST));
		addLast(helperPanel1, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,	new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(301, "Size:")),	CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.NORTHWEST));
		addLast(helperPanel2, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,	new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(302, "Waypoint:")),	CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel3, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,	new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(303, "Name:")),	CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(inpName.setTag(CellConstants.SPAN, new Dimension(2, 1)), CellConstants.DONTSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(304, "Coordinates:")),	CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(btnCoordinates.setTag(CellConstants.SPAN, new Dimension(2, 1)), CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(307, "Status:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel4, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,	new Dimension(2, 1));

		addNext(new mLabel(MyLocale.getMsg(306, "Owner:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(inpOwner.setTag(CellConstants.SPAN, new Dimension(2, 1)), CellConstants.DONTSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		addNext(new mLabel(MyLocale.getMsg(305, "Hidden on:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		addLast(helperPanel5, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN, new Dimension(2, 1));

		addLast(attViewer);

		if (isBigScreen) {
			this.addLast(new mLabel(MyLocale.getMsg(308, "Notes:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			cacheNotes = new mTextPad();
			cacheNotes.modify(ControlConstants.NotEditable, 0);
			addLast(new MyScrollBarPanel(cacheNotes));
		}
	}

	// ------------- present data

	public void setDetails(CacheHolder nch) {
		ch = nch;

		int addiCount;

		dirty_notes = false;

		inpWaypoint.setText(ch.getWayPoint());
		inpName.setText(ch.getCacheName());
		inpHidden.setText(ch.getDateHidden());
		inpOwner.setText(ch.getCacheOwner());
		
		btnCoordinates.setText(ch.pos.toString());
		// If the cache status contains a date, do not overwrite it with 'found' message		
		if (ch.getCacheStatus().length() >= 10 && ch.getCacheStatus().charAt(4) == '-') {
			chcStatus.setText(MyLocale.getMsg(318, "Found") + " " + ch.getCacheStatus());
		} else {
			chcStatus.setText(ch.getCacheStatus());

			if (ch.is_found() == true)
				chcStatus.setText(MyLocale.getMsg(318, "Found"));
		}
		
		chcType.setInt(CacheType.cw2GuiSelect(ch.getType()));
		
		blackStatus = !ch.is_black();
		toggleBlackStatus();
		blackStatusChanged = false;
		
		if (inpWaypoint.getText().length() == 0)
			createWptName();
		
		//TODO: update after TB dialog?
		if (ch.has_bugs()) {
			btnShowBug.image = new mImage(useBigIcons?"bug_vga.gif":"bug.gif");	
		} else {
			btnShowBug.image = new mImage(useBigIcons?"bug_no_vga.gif":"bug_no.gif");
		}
		btnShowBug.repaint();
		
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
				//FIXME: put incomplete to save method?
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(ch.getWayPoint() + " has wrong terrain " + ch.getTerrain());
			}
			if (CacheTerrDiff.isValidTD(ch.getHard())) {
				btnDiff.setText(MyLocale.getMsg(1000, "D") + ": " + CacheTerrDiff.longDT(ch.getHard()));
			} else {
				btnDiff.setText("D: -.-");
				//FIXME: put incomplete to save method?
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(nch.getWayPoint() + " has wrong difficulty " + ch.getHard());
			}
		}

		if (ch.mainCache == null) {
			addiCount = nch.addiWpts.size();
		} else {
			addiCount = nch.mainCache.addiWpts.size();
		}
		
		lblAddiCount.setText(MyLocale.getMsg(1044, "Addis") + ": " + String.valueOf(addiCount));

		if (isBigScreen)
			cacheNotes.setText(nch.getExistingDetails().getCacheNotes());
	}

	// ------------- user interaction
	// ------------- auxiliary methods

	/*****************************************************\

	                      CLEAN UP BELOW

	\*****************************************************/

	// ------------- save data
	/**
	 * save waypoint if there have been any changes
	 * @return true if changes were saved, false if there were no changes to save
	 */
	protected boolean saveIfNeeded() {
		// waypoint
		// userimages ???
		// coordinates
		// status
		// terrain
		// difficulty
		boolean needsSaving = false;
		int newDifficulty, newTerrain;
		
		if (!inpHidden.getText().equals(ch.getDateHidden())) {
			ch.setDateHidden(inpHidden.getText());
			needsSaving = true;
		}
		
		if (!inpOwner.getText().equals(ch.getCacheOwner())) {
			ch.setCacheOwner(inpOwner.getText());
			needsSaving = true;
		}
		
		if (!inpName.getText().equals(ch.getCacheName())) {
			ch.setCacheName(inpName.getText());
			needsSaving = true;
		}
		
		if (CacheSize.guiSizeStrings2CwSize(chcSize.getText()) != ch.getCacheSize()) {
			ch.setCacheSize(CacheSize.guiSizeStrings2CwSize(chcSize.getText()));
			needsSaving = true;
		}
		
		if (ch.getType() != CacheType.guiSelect2Cw(chcType.getInt())) {
			ch.setType(CacheType.guiSelect2Cw(chcType.getInt()));
			needsSaving = true;
		}
		
		if (blackStatus != ch.is_black()) {
			ch.setBlack(blackStatus);
			needsSaving = true;
			//TODO: propagate to addis. how?
		}
		
		if (!ch.pos.toString().equals(btnCoordinates.getText())) {
			ch.LatLon = btnCoordinates.getText();
			/// PUH!
		}
		
		if (isBigScreen) {
			if (!cacheNotes.getText().equals(ch.getFreshDetails().getCacheNotes())) {
				ch.getFreshDetails().setCacheNotes(cacheNotes.getText());
				needsSaving = true;
			}
		}
		
		// ch.setAttributesToAddiWpts();
		//		boolean saveWpt = false;
		//		boolean renameWpt = false;
		//		int newdiff;
		//		int newTerr;
		//	
		//		Objects to check:


		//		if (!this.inpWaypoint.equals(thisCache.getWayPoint())) {
		//			saveWpt = true;
		//			renameWpt = true;
		//		}
		//		if (!this.chcSize)
		//		if (!this.chcStatus)
		//		if (!this.chcType)
		//		if (newdiff)
		//		if (newterr)
		//		if (btnBlack.getImage()
		needsTableUpdate = needsTableUpdate || needsSaving;
		return true;
	}


	public void toggleBugImage() {

	}

	/** toggle blackStatus and update image accordingly */
	public void toggleBlackStatus() {
		blackStatus = ! blackStatus;
		if (blackStatus) {
			btnBlack.image = (imgBlack = new mImage(useBigIcons?"is_black_vga.png":"is_black.png"));
			//FIXME: get image with proper transparency
			imgBlack.transparentColor = Color.White;
		} else {
			btnBlack.image = (imgBlack = new mImage(useBigIcons?"no_black_vga.png":"no_black.png"));
			//FIXME: get image with proper transparency
			imgBlack.transparentColor = Color.Black;
		}
		btnBlack.repaint();
	}

	/** clear attributes panel */
	public void clearAttributes() {
		attViewer.clear();
	}

	public void setNeedsTableUpdate(boolean tableUpdate) {
		needsTableUpdate = tableUpdate;
	}

	public boolean needsTableUpdate() {
		return needsTableUpdate;
	}

	public boolean isDirty() {
		return dirty_notes || needsTableUpdate;
		//return dirty_notes || dirty_details || needsTableUpdate;
	}

	public boolean hasBlackStatusChanged() {
		return blackStatusChanged;
	}

	/**
	 * @param chD
	 *            details of the cache to display
	 * @param dbindex
	 *            index in cacheDB, in which changes will be saved
	 */


	/**
	 * if is addi -> returns the respective AddiWpt if is main -> returns the
	 * respective MainWpt
	 */
	public void createWptName() {
		String wpt = inpWaypoint.getText().toUpperCase();
		if (CacheType.isAddiWpt(CacheType.guiSelect2Cw(chcType.getInt())) && 
				(Global.mainTab.mainCache.startsWith("GC") || 
						Global.mainTab.mainCache.startsWith("OC") || 
						Global.mainTab.mainCache.startsWith("CW")) 
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
				} else {
					deactivateControl(btnTerr);
					deactivateControl(btnDiff);
					deactivateControl(chcSize);
					chcSize.select(0);
					btnTerr.setText(MyLocale.getMsg(1001, "T")+": -.-");
					btnDiff.setText(MyLocale.getMsg(1000, "D")+": -.-");
				}
			}
			//FIXME: check if something was actually changed, since datachange events also occur if you just hop through the fileds with the tab key (Why? don't know!)
//			dirty_details = true;
			needsTableUpdate = true;
		}
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnNotes) {
				dirty_notes = true; // TODO I think this is redundant, because
				// the notes are saved separately by the notes screen itself
				NotesScreen nsc = new NotesScreen(ch
						.getCacheDetails(true));
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
				if (isBigScreen)
					cacheNotes.setText(ch.getCacheDetails(true).getCacheNotes());
				
			} else if (ev.target == btnShowMap) {
				Global.mainTab.SwitchToMovingMap(ch.pos, true);
				
			} else if (ev.target == btnShowBug) {
				TravelbugInCacheScreen ts = new TravelbugInCacheScreen(ch.getCacheDetails(true).Travelbugs.toHtml(),	"Travelbugs");
				ts.execute(this.getFrame(), Gui.CENTER_FRAME);
				
			} else if (ev.target == btnCenter) {
				CWPoint cp = new CWPoint(ch.LatLon);
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
				String note = ch.getCacheDetails(true).getCacheNotes();
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if (note.length() > 0)
					note = note + "\n" + dtm.toString();
				else
					note = note + dtm.toString();
				note = note + "\n";
				ch.getCacheDetails(true).setCacheNotes(note);
				//FIXME: better use saveDirtyWaypoint()?
				ch.save();
				
			} else if (ev.target == btnAddPicture) {
				ch.getCacheDetails(true).addUserImage(profile);
				
			} else if (ev.target == btnBlack) {
				toggleBlackStatus();
//				if (ch.is_black()) {
//					//FIXME: only change thisCache in save routine
//					ch.setBlack(false);
//					btnBlack.image = imgBlackNo;
//				} else {
//					//FIXME: only change thisCache in save routine
//					ch.setBlack(true);
//					btnBlack.image = imgBlack;
//				}
//				//FIXME: during display of details only the GUI should be relevant
//				// and we collect results at the end
//				blackStatus = ch.is_black();
//				
//				btnBlack.repaintNow();
////				dirty_details = true;
//				blackStatusChanged = true;
				
			} else if (ev.target == btnNewWpt) {
				CacheHolder newch = new CacheHolder();
				newch.LatLon = ch.LatLon;
				newch.pos = new CWPoint(ch.pos);
				newch.setType(CacheType.CW_TYPE_STAGE);
				newch.setHard(CacheTerrDiff.CW_DT_UNSET);
				newch.setTerrain(CacheTerrDiff.CW_DT_UNSET);
				newch.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				Global.mainTab.newWaypoint(newch);
				
			} else if (ev.target == btnGoto) {
				// FIXME: if something changed saveDirtyWaypoint();
				Global.mainTab.gotoP.setDestinationAndSwitch(ch);
				
			} else if (ev.target == btnCoordinates) {
				CWPoint coords = new CWPoint(btnCoordinates.getText(), CWPoint.CW);
				CoordsScreen cs = new CoordsScreen(true);
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute() == FormBase.IDOK) {
//					dirty_details = true;
					//FIXME: don't do this, use save funktion instead!!
					coords = cs.getCoords();
					Global.getProfile().notifyUnsavedChanges(!ch.pos.toString().equals(coords.toString()));
					ch.pos.set(coords);
					btnCoordinates.setText(coords.toString());
					ch.LatLon = coords.toString();
					// If the current centre is valid, calculate the distance and bearing to it
					CWPoint centre = Global.getPref().curCentrePt;
					if (centre.isValid())
						ch.calcDistance(centre);
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
//					dirty_details = true;
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
//						dirty_details = true;
						// profile.hasUnsavedChanges=true;
					}
			} else if (ev.target == btnTerr) {
				int returnValue;
				TerrDiffForm tdf = new TerrDiffForm(true, ch
						.getTerrain());
				returnValue = tdf.execute();
				if (returnValue == 1 && tdf.getDT() != ch.getTerrain()) {
					//FIXME: do this when waypoint is checked for saving
					ch.setTerrain(tdf.getDT());
					btnTerr.setText(MyLocale.getMsg(1001, "T") + ": "
							+ CacheTerrDiff.longDT(ch.getTerrain()));
//					dirty_details = true;
				}
			} else if (ev.target == btnDiff) {
				int returnValue;
				TerrDiffForm tdf = new TerrDiffForm(false, ch.getHard());
				returnValue = tdf.execute();
				if (returnValue == 1 && tdf.getDT() != ch.getHard()) {
					//FIXME: do this when waypoint is checked for saving
					ch.setHard(tdf.getDT());
					btnDiff.setText(MyLocale.getMsg(1000, "D") + ": "
							+ CacheTerrDiff.longDT(ch.getHard()));
//					dirty_details = true;
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
		// Strip the found message if the status contains a date
		if (chcStatus.getText().startsWith(MyLocale.getMsg(318, "Found"))
				&& chcStatus.getText().length() >= MyLocale
				.getMsg(318, "Found").length() + 11) {
			ch.setCacheStatus(chcStatus.getText().substring(
					MyLocale.getMsg(318, "Found").length() + 1));
		} else {
			ch.setCacheStatus(chcStatus.getText());
		}
		
		if (!ch.is_found() && ch.getCacheStatus().length() >= 10
				&& ch.getCacheStatus().charAt(4) == '-') {
			// Use same heuristic condition as in setDetails(CacheHolder) to
			// determine, if this
			// cache
			// has to considered as found.
			ch.setFound(true);
		} else {
			ch.setFound(chcStatus.getText().startsWith(
					MyLocale.getMsg(318, "Found")));
		}
		
		ch.setCacheOwner(inpOwner.getText().trim());
		
		ch.setOwned(ch.getCacheStatus().equals(
				MyLocale.getMsg(320, "Owner")));
		// Avoid setting is_owned if alias is empty and username is empty
		if (ch.is_owned() == false) {
			ch.setOwned((!pref.myAlias.equals("") && pref.myAlias
					.equals(ch.getCacheOwner()))
					|| (!pref.myAlias2.equals("") && pref.myAlias2
							.equals(ch.getCacheOwner())));
		}
		
		ch.setBlack(blackStatus);
		
		String oldWaypoint = ch.getWayPoint();
		ch.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
		
		ch.setCacheSize(CacheSize.guiSizeStrings2CwSize(chcSize
				.getText()));
		
		// If the waypoint does not have a name, give it one
		if (ch.getWayPoint().equals("")) {
			ch.setWayPoint(profile.getNewWayPointName());
		}
		
		// Don't allow single letter names=> Problems in updateBearingDistance
		// This is a hack but faster than slowing down the loop in
		// updateBearingDistance
		if (ch.getWayPoint().length() < 2)
			ch.setWayPoint(ch.getWayPoint() + " ");
		
		ch.setCacheName(inpName.getText().trim());
		
		ch.LatLon = ch.pos.toString();
		
		ch.setDateHidden(inpHidden.getText().trim());
		
		byte oldType = ch.getType();
		ch.setType(CacheType.guiSelect2Cw(chcType.getInt()));
		
		// thisCache.saveCacheDetails(profile.dataDir); // this is redundant,
		// because all changes
		// affecting the details are immediately saved
		// Now update the table
		

		/*
		 * The references have to be rebuilt if: - the cachetype changed from
		 * addi->normal or normal->addi - the old cachetype or the new cachetype
		 * were 'addi' and the waypointname has changed
		 */
		if (CacheType.isAddiWpt(ch.getType()) != CacheType.isAddiWpt(oldType)
				|| ((CacheType.isAddiWpt(ch.getType()) || CacheType
						.isAddiWpt(oldType)) && !ch.getWayPoint()
						.equals(oldWaypoint))) {
			// If we changed the type to addi, check that a parent exists
			if (CacheType.isAddiWpt(ch.getType())) {
				int idx;
				if (ch.getWayPoint().length() < 5)
					idx = -1;
				else {
					idx = profile.getCacheIndex("GC" + ch.getWayPoint().substring(ch.getWayPoint().length() == 5 ? 1 : 2));
					if (idx < 0)
						idx = profile.getCacheIndex("OC" + ch.getWayPoint().substring(ch.getWayPoint().length() == 5 ? 1 : 2));
					if (idx < 0)
						idx = profile.getCacheIndex("CW"+ ch.getWayPoint().substring(ch.getWayPoint().length() == 5 ? 1 : 2));
					if (idx < 0)
						(new MessageBox(
								MyLocale.getMsg(144, "Warning"),
								MyLocale.getMsg(734,"No main cache found for addi waypoint ")
								+ " "+ ch.getWayPoint()+ "\n"
								+ MyLocale.getMsg(735,"Addi Waypoints must have the format xxYYYY, where xx are any 2 chars and YYYY are the main cache's chars after the GC"),
								FormBase.OKB)).execute();
				}
				profile.buildReferences(); // TODO this takes quite long -> use
				// profile.setAddiRef
				// instead
			} else {
				profile.buildReferences(); // we have to do this to release the
				// link between the
				// two caches
			}
		}
		// set status also on addi wpts
		ch.setAttributesToAddiWpts();
		ch.checkIncomplete();
		dirty_notes = false;
//		dirty_details = false;
		setNeedsTableUpdate(false);
		ch.getFreshDetails().hasUnsavedChanges = true;
	}

	public String getDisplayedWaypoint() {
		return ch.getWayPoint();
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
					Travelbug tb = TravelbugPickup.pickupTravelbug(ch.getCacheDetails(true).Travelbugs);
					if (tb != null) {
//						dirty_details = true;
						// Get the list of my travelbugs
						tbjList = new TravelbugJourneyList();
						tbjList.readTravelbugsFile();
						// Add the tb to this list
						tbjList.addTbPickup(tb, Global.getProfile().name,
								ch.getWayPoint());
						tbjList.saveTravelbugsFile();
						tbjList = null;
						setHtml(ch.getCacheDetails(true).Travelbugs.toHtml());
						repaint();
						ch.setHas_bugs(ch.getCacheDetails(true).Travelbugs.size() > 0);
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
						ch.getCacheDetails(true).Travelbugs.add(tb);
						tbjList.addTbDrop(tb, Global.getProfile().name,
								ch.getWayPoint());
					}
					tbjList.saveTravelbugsFile();
					tbjList = null;
					ch.setHas_bugs(ch.getCacheDetails(true).Travelbugs.size() > 0);
					setHtml(ch.getCacheDetails(true).Travelbugs.toHtml());
					repaint();
//					dirty_details = true;
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
