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

import CacheWolf.controls.CWProgressBar;
import CacheWolf.controls.DataMover;
import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.InfoScreen;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.exp.ASCExporter;
import CacheWolf.exp.ExploristExporter;
import CacheWolf.exp.GarminPicExporter;
import CacheWolf.exp.GpxExportNg;
import CacheWolf.exp.HTMLExporter;
import CacheWolf.exp.KMLExporter;
import CacheWolf.exp.LocExporter;
import CacheWolf.exp.MSARCSVExporter;
import CacheWolf.exp.OCLogExport;
import CacheWolf.exp.OVLExporter;
import CacheWolf.exp.OziExporter;
import CacheWolf.exp.SpoilerPOIExporter;
import CacheWolf.exp.TPLExporter;
import CacheWolf.exp.TomTomExporter;
import CacheWolf.imp.CSVImporter;
import CacheWolf.imp.FieldnotesImporter;
import CacheWolf.imp.GCImporter;
import CacheWolf.imp.GCVoteImporter;
import CacheWolf.imp.GPXImporter;
import CacheWolf.imp.ImportGui;
import CacheWolf.imp.LOCXMLImporter;
import CacheWolf.imp.OCGPXfetch;
import CacheWolf.imp.OCLinkImporter;
import CacheWolf.imp.OCXMLImporter;
import CacheWolf.navi.MapImporter;
import CacheWolf.navi.MapLoaderGui;
import CacheWolf.navi.SelectMap;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.view.TravelbugJourneyScreenFactory;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Font;
import ewe.fx.IconAndText;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.InputBox;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.ProgressBarForm;
import ewe.ui.PullDownMenu;
import ewe.ui.UIConstants;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;
import ewe.util.Vector;

/**
 * This class creates the menu for cachewolf. It is also responsible for reacting to user inputs in the menu.<br>
 * This class id=100
 * 
 * @see MainForm
 * @see MainTab Last change: 20061123 salzkammergut Tidied up, added MyLocale, added additional internationalisation, combine save/filter for small screens, garminConn
 */
public class TablePanelMenu extends MenuBar {
    private MenuItem mnuSeparator = new MenuItem("-");
    private MenuItem appMenuProfile, appMenuPreferences, appMenuContext, appMenuImport, appMenuExport, appMenuMaps, appMenuExit;
    private MenuItem searchMenuContinue, searchMenuStart, searchMenuClr;
    private MenuItem loadcaches, loadOC, loadOCFinds;
    private MenuItem downloadmap, kalibmap, importmap, selectMapPath;
    private MenuItem spider, spiderRoute, spiderAllFinds, loadGCVotes, fetchOCLink, update, chkVersion;
    private MenuItem about, wolflang, sysinfo, legend;
    private MenuItem exportGpxNg, exporthtml, exporttop50, exportASC, exportTomTom, exportMSARCSV, exportSpoilerPOI;
    private MenuItem exportOZI, exportKML, exportTPL, exportExplorist, exportOCLog;
    private MenuItem exportGarminPic;
    private MenuItem filtApply, filtCreate, filtInvert, filtSelected, filtNonSelected, filtBlack;
    private MenuItem exportLOC, exportGPSBabel;
    private MenuItem orgNewWP, orgCopy, orgMove, orgDelete, orgCheckNotesAndSolver;
    public MenuItem cacheTour, orgTravelbugs, orgAbout;
    private MenuItem mnuNewProfile, mnuOpenProfile, mnuDeleteProfile, mnuRenameProfile, mnuEditCenter, orgRebuild, savenoxit;
    IconAndText filtApplyImage, filtClearImage;

    private TablePanel tablePanel;
    private FilterScreen scnFilter = new FilterScreen();
    private static boolean searchInDescriptionAndNotes = false;
    private static boolean searchInLogs = false;
    GCVoteImporter sGCV = null;

    public TablePanelMenu() {
	Preferences.itself().setgpsbabel();
	this.equalWidths = true;
	this.addNext(makeApplicationMenu());
	// Depending on screen width display either filter and search menus or the combined menu
	if (Preferences.itself().getScreenWidth() > 300) {
	    this.addNext(makeSearchMenu());
	}
	this.addNext(makeFilterMenu());
	this.addNext(makeOrganizeMenu());
    }

    private Menu makeImportSubMenu() {
	MenuItem[] mnuImport = { loadcaches = new MenuItem(MyLocale.getMsg(129, "Import GPX")), //
		mnuSeparator, //
		loadOC = new MenuItem(MyLocale.getMsg(130, "Download from opencaching")), //
		loadOCFinds = new MenuItem(MyLocale.getMsg(163, "Finds from opencaching")), //
		mnuSeparator, //
		spider = new MenuItem(MyLocale.getMsg(131, "Download from geocaching.com")), //
		spiderAllFinds = new MenuItem(MyLocale.getMsg(217, "Spider all finds from geocaching.com")), //
		spiderRoute = new MenuItem(MyLocale.getMsg(137, "Download along a Route from geocaching.com")), //
		mnuSeparator, loadGCVotes = new MenuItem(MyLocale.getMsg(1208, "Import ratings from GCVote")), //
		fetchOCLink = new MenuItem(MyLocale.getMsg(1209, "Fetch link to OC - Cache")), //
		mnuSeparator, //
		update = new MenuItem(MyLocale.getMsg(1014, "Update cache data")), //
	};
	updateGCVotesMenu();
	return new Menu(mnuImport, MyLocale.getMsg(175, "Import"));
    }

    public void updateGCVotesMenu() {
	if (Preferences.itself().useGCFavoriteValue) {
	    loadGCVotes.modifiers |= MenuItem.Disabled;
	} else {
	    loadGCVotes.modifiers &= ~MenuItem.Disabled;
	}
    }

    private Menu makeExportSubMenu() {
	MenuItem[] exitems = { exporthtml = new MenuItem(MyLocale.getMsg(100, "to HTML")), //
		exportGpxNg = new MenuItem(MyLocale.getMsg(101, "to GPX Test")), //
		exporttop50 = new MenuItem(MyLocale.getMsg(102, "to TOP50 ASCII")), //
		exportASC = new MenuItem(MyLocale.getMsg(104, "to CSV")), //
		exportTomTom = new MenuItem(MyLocale.getMsg(105, "to TomTom")), //
		exportMSARCSV = new MenuItem(MyLocale.getMsg(106, "to MS AutoRoute CSV")), //
		exportLOC = new MenuItem(MyLocale.getMsg(215, "to LOC")), //
		exportGPSBabel = new MenuItem(MyLocale.getMsg(122, "to GPS")), //
		exportOZI = new MenuItem(MyLocale.getMsg(124, "to OZI")), //
		exportKML = new MenuItem(MyLocale.getMsg(125, "to Google Earth")), //
		exportExplorist = new MenuItem(MyLocale.getMsg(132, "to Explorist")), //
		exportSpoilerPOI = new MenuItem(MyLocale.getMsg(135, "to SpoilerPOI")), //
		exportTPL = new MenuItem(MyLocale.getMsg(128, "via Template")), //
		exportOCLog = new MenuItem(MyLocale.getMsg(1210, "logs to OC")), //
		exportGarminPic = new MenuItem("Garmin pictures"), };
	if (Preferences.itself().gpsbabel == null) {
	    exportGPSBabel.modifiers = MenuItem.Disabled;
	    exportGPSBabel.setText(MyLocale.getMsg(136, "to GPS : gpsbabel missing."));
	}
	Menu exportMenu = new Menu(exitems, MyLocale.getMsg(107, "Export"));
	return exportMenu;
    }

    private Menu makeMapsSubMenu() {
	MenuItem[] mapMenuItems = { downloadmap = new MenuItem(MyLocale.getMsg(162, "Download calibrated")), //
		importmap = new MenuItem(MyLocale.getMsg(150, "Import")), //
		kalibmap = new MenuItem(MyLocale.getMsg(151, "Calibrate")), //
		mnuSeparator, //
		selectMapPath = new MenuItem(MyLocale.getMsg(4236, "Change map directory$c")), //
	};
	Menu mapsMenu = new Menu(mapMenuItems, null);
	return mapsMenu;
    }

    private Menu makeProfileSubMenu() {
	MenuItem[] mnuProfile = { mnuNewProfile = new MenuItem(MyLocale.getMsg(1107, "New")), //
		mnuOpenProfile = new MenuItem(MyLocale.getMsg(1109, "Open")), //
		mnuDeleteProfile = new MenuItem(MyLocale.getMsg(1125, "Delete")), //
		mnuRenameProfile = new MenuItem(MyLocale.getMsg(1126, "Rename")), //
		mnuSeparator, //
		mnuEditCenter = new MenuItem(MyLocale.getMsg(1110, "Centre")), //
		mnuSeparator, //
		orgRebuild = new MenuItem(MyLocale.getMsg(208, "Rebuild Index")), //
		orgCheckNotesAndSolver = new MenuItem(MyLocale.getMsg(220, "Check Notes/Solver")), //
		savenoxit = new MenuItem(MyLocale.getMsg(127, "Save")), //
	};
	Menu profileMenu = new Menu(mnuProfile, null);

	return profileMenu;
    }

    private PullDownMenu makeApplicationMenu() {
	appMenuContext = GuiImageBroker.getMenuItem(MyLocale.getMsg(134, "Waypoint"), "waypoint");
	appMenuImport = GuiImageBroker.getMenuItem(MyLocale.getMsg(175, "Import"), "import");
	appMenuImport.subMenu = makeImportSubMenu();
	appMenuExport = GuiImageBroker.getMenuItem(MyLocale.getMsg(107, "Export"), "export");
	appMenuExport.subMenu = makeExportSubMenu();
	appMenuMaps = GuiImageBroker.getMenuItem(MyLocale.getMsg(149, "Maps"), "globe");
	appMenuMaps.subMenu = makeMapsSubMenu();
	appMenuProfile = GuiImageBroker.getMenuItem(MyLocale.getMsg(121, "Profile"), "profile");
	appMenuProfile.subMenu = makeProfileSubMenu();
	appMenuPreferences = GuiImageBroker.getMenuItem(MyLocale.getMsg(108, "Preferences"), "tools");
	appMenuExit = GuiImageBroker.getMenuItem(MyLocale.getMsg(111, "Exit"), "exit");
	MenuItem[] appMenuItems;
	appMenuItems = new MenuItem[] { appMenuContext, mnuSeparator, appMenuImport, appMenuExport, appMenuMaps, mnuSeparator, appMenuProfile, mnuSeparator, appMenuPreferences, mnuSeparator, appMenuExit };
	return GuiImageBroker.getPullDownMenu(MyLocale.getMsg(120, "CacheWolf"), "cw", appMenuItems);
    }

    private void makeSearchItems() {
	searchMenuStart = GuiImageBroker.getMenuItem(MyLocale.getMsg(133, "Search" + "$" + (char) 6), "search"); // char 6 = ctrl +f
	searchMenuContinue = GuiImageBroker.getMenuItem(MyLocale.getMsg(112, "Search results"), "searchmore");
	searchMenuClr = GuiImageBroker.getMenuItem(MyLocale.getMsg(113, "Clear search"), "searchoff");
    }

    private PullDownMenu makeSearchMenu() {
	makeSearchItems();
	MenuItem[] searchMenuItems = { searchMenuStart, searchMenuContinue, searchMenuClr };
	return GuiImageBroker.getPullDownMenu(MyLocale.getMsg(133, "Search"), "search", searchMenuItems);
    }

    private PullDownMenu makeFilterMenu() {
	filtApply = GuiImageBroker.getMenuItem(MyLocale.getMsg(709, "Apply"), "filter");
	filtApplyImage = (IconAndText) filtApply.image;
	filtClearImage = GuiImageBroker.getIconAndText(MyLocale.getMsg(116, "Clear"), "filterclear");
	filtCreate = GuiImageBroker.getMenuItem(MyLocale.getMsg(114, "Create"), "filtercreate");
	filtInvert = GuiImageBroker.getMenuItem(MyLocale.getMsg(115, "Invert"), "filterinvert");
	filtSelected = GuiImageBroker.getMenuItem(MyLocale.getMsg(160, "Filter selected"), "filterselected");
	filtNonSelected = GuiImageBroker.getMenuItem(MyLocale.getMsg(1011, "Filter out non selected"), "filternonselected");
	filtBlack = GuiImageBroker.getMenuItem(MyLocale.getMsg(161, "Show Blacklist"), "no_black");
	MenuItem[] filterMenuItems;
	if (Preferences.itself().getScreenWidth() > 300) {
	    if (Preferences.itself().hasTickColumn) {
		filterMenuItems = new MenuItem[] { filtApply, filtCreate, filtInvert, mnuSeparator, filtSelected, filtNonSelected, mnuSeparator, filtBlack };
	    } else {
		filterMenuItems = new MenuItem[] { filtApply, filtCreate, filtInvert, mnuSeparator, filtBlack };
	    }
	} else {
	    makeSearchItems();
	    if (Preferences.itself().hasTickColumn) {
		filterMenuItems = new MenuItem[] { filtApply, filtCreate, filtInvert, mnuSeparator, filtSelected, filtNonSelected, mnuSeparator, filtBlack, mnuSeparator, searchMenuContinue, searchMenuClr };
	    } else {
		filterMenuItems = new MenuItem[] { filtApply, filtCreate, filtInvert, mnuSeparator, mnuSeparator, filtBlack, mnuSeparator, searchMenuContinue, searchMenuClr };
	    }
	}
	// filtApply.modifiers = (MainForm.profile.getFilterActive() == Filter.FILTER_ACTIVE) ? filtApply.modifiers | MenuItem.Checked : filtApply.modifiers & ~MenuItem.Checked;
	// filtApply.image = (MainForm.profile.getFilterActive() == Filter.FILTER_ACTIVE) ? filtClearImage : filtApplyImage;
	setfiltApplyImage();
	filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
	return GuiImageBroker.getPullDownMenu(MyLocale.getMsg(159, "Filter"), "filter", filterMenuItems);
    }

    private PullDownMenu makeOrganizeMenu() {
	MenuItem[] organizeMenuItems = { //
	orgNewWP = GuiImageBroker.getMenuItem(MyLocale.getMsg(214, "New Waypoint"), "add"), //
		orgCopy = GuiImageBroker.getMenuItem(MyLocale.getMsg(141, "Copy Waypoints"), "copy"), //
		orgMove = GuiImageBroker.getMenuItem(MyLocale.getMsg(142, "Move Waypoints"), "move"), //
		orgDelete = GuiImageBroker.getMenuItem(MyLocale.getMsg(143, "Delete Waypoints"), "delete"), //
		mnuSeparator, //
		orgTravelbugs = GuiImageBroker.getMenuItem(MyLocale.getMsg(139, "Manage travelbugs"), "bug"), //
		cacheTour = GuiImageBroker.getMenuItem(MyLocale.getMsg(198, "Cachetour"), "cachetour"), //
		mnuSeparator, //
		orgAbout = GuiImageBroker.getMenuItem(MyLocale.getMsg(117, "About"), "about"), //
	};
	orgAbout.subMenu = this.makeAboutMenu();
	return GuiImageBroker.getPullDownMenu(MyLocale.getMsg(140, "Organise"), "admin", organizeMenuItems);
    }

    private Menu makeAboutMenu() {
	MenuItem[] aboutMenuItems = { //
	about = GuiImageBroker.getMenuItem(MyLocale.getMsg(117, "About"), "about"),//
		legend = GuiImageBroker.getMenuItem(MyLocale.getMsg(155, "Legend"), "legend"),//
		wolflang = GuiImageBroker.getMenuItem(MyLocale.getMsg(118, "WolfLanguage"), "wolflanguage"),//
		sysinfo = GuiImageBroker.getMenuItem(MyLocale.getMsg(157, "System"), "system"),//
		chkVersion = GuiImageBroker.getMenuItem(MyLocale.getMsg(158, "Version Check"), "version"),//
	};
	return new Menu(aboutMenuItems, MyLocale.getMsg(117, "About"));
    }

    public void setTablePanel(TablePanel tablePanel) {
	this.tablePanel = tablePanel;
	if (appMenuContext.subMenu == null) {
	    appMenuContext.subMenu = tablePanel.myTableControl.getTheMenu();
	}
    }

    void search() {
	SearchBox inp = new SearchBox(MyLocale.getMsg(119, "Search for:"));
	String srch = inp.input(null, "", searchInDescriptionAndNotes, searchInLogs, 10);
	MyLocale.setSIPOff();
	if (srch != null) {
	    searchInDescriptionAndNotes = inp.useNoteDesc();
	    searchInLogs = inp.useLogs();
	    SearchCache ssc = new SearchCache(MainForm.profile.cacheDB);
	    ssc.search(srch, searchInDescriptionAndNotes, searchInLogs);
	}
    }

    public void updateSelectedCaches() {
	CacheDB cacheDB = MainForm.profile.cacheDB;
	CacheHolder ch;
	ImportGui importGui = new ImportGui(MyLocale.getMsg(1014, "updateSelectedCaches"), ImportGui.IMAGES | ImportGui.TRAVELBUGS | ImportGui.ALL);
	if (importGui.execute() == FormBase.IDCANCEL) {
	    return;
	}

	GCImporter gcImporter = new GCImporter();
	gcImporter.setDownloadPics(importGui.downloadPics);
	gcImporter.setMaxLogsToSpider(Preferences.itself().maxLogsToSpider);

	OCXMLImporter ocSync = new OCXMLImporter();
	Vm.showWait(true);
	boolean alreadySaid = false;
	boolean alreadySaid2 = false;
	InfoBox infB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
	infB.exec();

	Vector cachesToUpdate = new Vector();
	for (int i = 0; i < cacheDB.size(); i++) {
	    ch = cacheDB.get(i);
	    if (ch.isChecked == true && ch.isVisible()) {
		// should work even if only the wayPoint is created
		if ((ch.getWayPoint().toUpperCase().startsWith("GC") || ch.isOC()))
		// Notiz: Wenn es ein addi Wpt ist, sollte eigentlich der Maincache gespidert werden
		// Alter code prüft aber nur ob ein Maincache von GC existiert und versucht dann den addi direkt zu spidern, was nicht funktioniert
		{
		    cachesToUpdate.add(new Integer(i));
		} else {
		    if (ch.isAddiWpt() && ch.mainCache != null && !ch.mainCache.isChecked && !alreadySaid2) {
			alreadySaid2 = true;
			new InfoBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(5001, "Can't spider additional waypoint directly. Please check main cache.")).wait(FormBase.OKB);
		    }
		    if (!ch.isAddiWpt() && !alreadySaid) {
			alreadySaid = true;
			new InfoBox(MyLocale.getMsg(327, "Information"), ch.getWayPoint() + MyLocale.getMsg(5002, ": At the moment this function is only applicable for geocaching.com and opencaching.")).wait(FormBase.OKB);
		    }
		}

	    }
	}

	int spiderErrors = 0;
	for (int j = 0; j < cachesToUpdate.size(); j++) {
	    int i = ((Integer) cachesToUpdate.get(j)).intValue();
	    ch = cacheDB.get(i);
	    infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + ch.getWayPoint() + " (" + (j + 1) + " / " + cachesToUpdate.size() + ")");
	    infB.redisplay();
	    if (ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC")) {
		int test = gcImporter.spiderSingle(i, infB);
		if (test == GCImporter.SPIDER_CANCEL) {
		    infB.close(0);
		    break;
		} else if (test == GCImporter.SPIDER_ERROR || test == GCImporter.SPIDER_IGNORE_PREMIUM) {
		    spiderErrors++;
		} else {
		    // MainForm.profile.hasUnsavedChanges=true;
		}
	    } else {
		if (!ocSync.syncSingle(i, infB)) {
		    infB.close(0);
		    break;
		} else {
		    // MainForm.profile.hasUnsavedChanges=true;
		}
	    }
	}
	gcImporter.setOldGCLanguage();

	infB.close(0);
	MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
	MainForm.profile.restoreFilter();
	MainForm.profile.updateBearingDistance();
	tablePanel.refreshTable();
	Vm.showWait(false);
	if (spiderErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), spiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded.")).wait(FormBase.OKB);
	}
    }

    public void setfiltApplyImage() {
	if (MainForm.profile.getFilterActive() >= Filter.FILTER_ACTIVE) {
	    // filtApply.modifiers = filtApply.modifiers | MenuItem.Checked;
	    filtApply.image = filtClearImage;
	} else {
	    // filtApply.modifiers = filtApply.modifiers & ~MenuItem.Checked;
	    filtApply.image = filtApplyImage;
	}
    }

    /*
     * operation 2=delete 3=rename
     */
    public void editProfile(int operation, int ErrorMsgActive, int ErrorMsg) {
	Preferences.itself().checkAbsoluteBaseDir(); // perhaps not necessary
	// select profile
	ProfilesForm f = new ProfilesForm(Preferences.itself().absoluteBaseDir, "", operation);
	if (f.execute() == -1)
	    return; // no select
	// check selection
	if (Preferences.itself().lastProfile.equals(f.newSelectedProfile)) {
	    // aktives Profil kann nicht gelöscht / umbenannt werden;
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(ErrorMsgActive, "[Profile active...]")).wait(FormBase.OKB);
	} else {
	    boolean err = true;
	    File profilePath = new File(Preferences.itself().absoluteBaseDir + f.newSelectedProfile);
	    if (operation == 3) {
		String newName = new InputBox("Bitte neuen Verzeichnisnamen eingeben : ").input("", 50);
		if (!newName.equals(null)) {
		    err = !profilePath.renameTo(new File(Preferences.itself().absoluteBaseDir + newName));
		}
	    } else if (operation == 2) {
		// Delete
		Profile p = new Profile();
		p.readIndex(null, Preferences.itself().absoluteBaseDir + f.newSelectedProfile + "/");
		String mapsPath = p.getMapsDir();
		//Really check if the user wants to delete the profile
		String questionText = MyLocale.getMsg(276, "Do You really want to delete profile '") + f.newSelectedProfile + MyLocale.getMsg(277, "' ?");
		if (new InfoBox("", questionText).wait(FormBase.MBYESNO) != FormBase.IDOK)
		    return;
		if (new InfoBox("", MyLocale.getMsg(1125, "Delete") + " " + MyLocale.getMsg(654, "Maps directory") + "?\n\n" + mapsPath + "\n").wait(FormBase.MBYESNO) == FormBase.IDOK) {
		    deleteDirectory(new File(mapsPath));
		}
		err = !deleteDirectory(profilePath);
		// ? wait until deleted ?
	    }
	    if (err) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(ErrorMsg, "[Profile Error...]")).wait(FormBase.OKB);
	    }
	}
    }

    private boolean deleteDirectory(File path) {
	if (path.exists()) {
	    String[] files = path.list();
	    for (int i = 0; i < files.length; i++) {
		File f = new File(path.getFullPath() + "/" + files[i]);
		if (f.isDirectory()) {
		    deleteDirectory(f);
		} else {
		    f.delete();
		}
	    }
	}
	return (path.delete());
    }

    public void toggleCacheTourVisible() {
	cacheTour.modifiers ^= MenuItem.Checked;
	MainForm.itself.toggleCacheTourVisible();
    }

    public void onEvent(Event ev) {
	CacheDB cacheDB = MainForm.profile.cacheDB;
	MainTab.itself.updatePendingChanges();
	if (ev instanceof MenuEvent) { // && ev.type == MenuEvent.PRESSED
	    MenuEvent mev = (MenuEvent) ev;
	    // /////////////////////////////////////////////////////////////////////
	    // subMenu for MainForm.profiles, part of "Application" menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == mnuNewProfile) {
		if (NewProfileWizard.startNewProfileWizard(getFrame())) {
		    tablePanel.myTableModel.numRows = 0;
		    MainForm.itself.setCurCentrePt(MainForm.profile.center);
		    filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		    tablePanel.refreshTable();
		}
	    }
	    if (mev.selectedItem == mnuOpenProfile) {
		MainTab.itself.saveUnsavedChanges(true);
		if (MainForm.itself.selectProfile(MainForm.PROFILE_SELECTOR_FORCED_ON, false)) {

		    MainForm.profile.setFilterActive(Filter.FILTER_INACTIVE);

		    CWPoint savecenter = new CWPoint(MainForm.profile.center);
		    MainForm.profile.clearProfile();
		    MainForm.profile.setCenterCoords(savecenter);

		    tablePanel.myTableModel.sortedBy = -1;
		    tablePanel.myTableModel.numRows = 0;

		    InfoBox infB = new InfoBox("CacheWolf", MyLocale.getMsg(5000, "Loading Cache-List"));
		    infB.exec();
		    infB.waitUntilPainted(1000);
		    Vm.showWait(infB, true);
		    MainForm.profile.readIndex(infB, Preferences.itself().absoluteBaseDir + Preferences.itself().lastProfile);
		    Vm.showWait(infB, false);
		    infB.close(0);

		    MainForm.itself.setCurCentrePt(MainForm.profile.center);
		    filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		    MainForm.itself.setTitle(MainForm.profile.name + " - CW " + Version.getRelease());
		    tablePanel.resetModel();
		}
	    }
	    if (mev.selectedItem == mnuDeleteProfile) {
		editProfile(2, 227, 226);
	    }
	    if (mev.selectedItem == mnuRenameProfile) {
		editProfile(3, 228, 229);
	    }
	    if (mev.selectedItem == mnuEditCenter) {
		EditCenter f = new EditCenter();
		f.execute(getFrame(), Gui.CENTER_FRAME);
		tablePanel.refreshTable();
		f.close(0);
	    }
	    // /////////////////////////////////////////////////////////////////////
	    // subMenu for import, part of "Application" menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == spider) {
		GCImporter gcImporter = new GCImporter();
		MainTab.itself.saveUnsavedChanges(false);
		gcImporter.doIt();
		cacheDB.clear();
		MainForm.profile.readIndex(null, Preferences.itself().absoluteBaseDir + Preferences.itself().lastProfile);
		tablePanel.resetModel();
		gcImporter.setOldGCLanguage();
	    }
	    if (mev.selectedItem == spiderRoute) {
		GCImporter gcImporter = new GCImporter();
		MainTab.itself.saveUnsavedChanges(false);
		gcImporter.doItAlongARoute();
		cacheDB.clear();
		MainForm.profile.readIndex(null, Preferences.itself().absoluteBaseDir + Preferences.itself().lastProfile);
		tablePanel.resetModel();
		gcImporter.setOldGCLanguage();
	    }
	    if (mev.selectedItem == spiderAllFinds) {
		GCImporter gcImporter = new GCImporter();
		MainTab.itself.saveUnsavedChanges(false);
		gcImporter.doIt(true);
		cacheDB.clear();
		MainForm.profile.readIndex(null, Preferences.itself().absoluteBaseDir + Preferences.itself().lastProfile);
		tablePanel.resetModel();
		gcImporter.setOldGCLanguage();
	    }
	    if (mev.selectedItem == loadGCVotes) {
		if (!Preferences.itself().useGCFavoriteValue) {
		    if (sGCV == null)
			sGCV = new GCVoteImporter();
		    sGCV.doIt();
		    tablePanel.resetModel();
		}
	    }
	    if (mev.selectedItem == fetchOCLink) {
		OCLinkImporter.doIt();
		tablePanel.resetModel();
	    }
	    if (mev.selectedItem == loadcaches) {
		String dir = Preferences.itself().getImporterPath("LocGpxImporter");
		FileChooser fc = new FileChooser(FileChooserBase.OPEN | FileChooserBase.MULTI_SELECT, dir);
		fc.addMask("*.gpx,*.zip,*.loc,*.txt,*.log,*.csv");
		fc.setTitle(MyLocale.getMsg(909, "Select file(s)"));
		if (fc.execute() != FormBase.IDCANCEL) {
		    dir = fc.getChosenDirectory().toString();
		    Preferences.itself().setImporterPath("LocGpxImporter", dir);
		    String files[] = fc.getAllChosen();
		    int how = GPXImporter.DOIT_ASK;
		    for (int i = 0; i < files.length; i++) {
			String file = dir + "/" + files[i];
			if (file.endsWith("txt") || file.endsWith("log")) {
			    FieldnotesImporter fn = new FieldnotesImporter(file);
			    fn.doIt();
			} else if (file.endsWith("loc")) {
			    LOCXMLImporter loc = new LOCXMLImporter(file);
			    loc.doIt();
			} else if (file.endsWith("csv")) {
			    CSVImporter mn = new CSVImporter(file);
			    mn.doIt();
			} else { // gpx + zip
			    GPXImporter gpx = new GPXImporter(file);
			    gpx.doIt(how);
			    how = gpx.getHow();
			}
		    }
		}
		MainForm.profile.setShowBlacklisted(false);
		filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		tablePanel.resetModel();
	    }
	    if (mev.selectedItem == loadOC) {
		OCXMLImporter oc = new OCXMLImporter();
		oc.doIt();
		MainForm.profile.setShowBlacklisted(false);
		filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		tablePanel.resetModel();
	    }
	    if (mev.selectedItem == loadOCFinds) {
		OCGPXfetch.doIt();
		MainForm.profile.setShowBlacklisted(false);
		filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		tablePanel.resetModel();
	    }
	    if (mev.selectedItem == update) {
		updateSelectedCaches();
	    }
	    // /////////////////////////////////////////////////////////////////////
	    // subMenu for export, part of "Application" menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == exporthtml) {
		HTMLExporter htm = new HTMLExporter();
		htm.doIt();
	    }
	    if (mev.selectedItem == exportGpxNg) {
		GpxExportNg gpx = new GpxExportNg();
		gpx.doit();
	    }
	    if (mev.selectedItem == exporttop50) {
		OVLExporter ovl = new OVLExporter();
		ovl.doIt();
	    }
	    if (mev.selectedItem == exportASC) {
		ASCExporter asc = new ASCExporter();
		asc.doIt();
	    }
	    if (mev.selectedItem == exportTomTom) {
		TomTomExporter tt = new TomTomExporter();
		tt.doIt();
	    }
	    if (mev.selectedItem == exportMSARCSV) {
		MSARCSVExporter msar = new MSARCSVExporter();
		// NewCSVExporter msar = new NewCSVExporter();
		msar.doIt();
	    }
	    if (mev.selectedItem == exportLOC) {
		LocExporter loc = new LocExporter();
		loc.doIt();
	    }
	    if (mev.selectedItem == exportGPSBabel) {
		String gpsBabelCommand;
		Vm.showWait(true);
		LocExporter loc = new LocExporter();
		// String tmpFileName = FileBase.getProgramDirectory() + "/temp.loc";
		// Must not contain special characters, because we don't quote below, because quoting causes problems on some platforms.
		// Find another way, when CW can be started from outside the program directory.
		String tmpFileName = "temp.loc";
		loc.setTmpFileName(tmpFileName);
		loc.doIt(LocExporter.MODE_AUTO);
		ProgressBarForm.display(MyLocale.getMsg(950, "Transfer"), MyLocale.getMsg(951, "Sending to GPS"), null);
		gpsBabelCommand = Preferences.itself().gpsbabel + " " + Preferences.itself().garminGPSBabelOptions + " -i geo -f " + tmpFileName + " -o garmin -F " + Preferences.itself().garminConn + ":";
		Preferences.itself().log("[MainMenu:onEvent] " + gpsBabelCommand);
		try {
		    // this will *only* work with ewe.jar at the moment
		    ewe.sys.Process p = Vm.exec(gpsBabelCommand);
		    p.waitFor();
		} catch (IOException ioex) {
		    Vm.showWait(false);
		    new InfoBox(MyLocale.getMsg(5500, "Error"), "Garmin export unsuccessful").wait(FormBase.OKB);
		    Preferences.itself().log("Error exporting to Garmin", ioex, true);
		}
		;
		ProgressBarForm.clear();
		Vm.showWait(false);
	    }
	    if (mev.selectedItem == exportOZI) {
		OziExporter ozi = new OziExporter();
		ozi.doIt();
	    }
	    if (mev.selectedItem == exportKML) {
		KMLExporter kml = new KMLExporter();
		kml.doIt();
	    }
	    if (mev.selectedItem == exportTPL) {
		FileChooser fc = new FileChooser(FileChooserBase.OPEN, FileBase.getProgramDirectory() + FileBase.separator + "templates");
		fc.addMask("*.tpl");
		fc.setTitle(MyLocale.getMsg(910, "Select Template file"));
		if (fc.execute() != FormBase.IDCANCEL) {
		    TPLExporter tpl = new TPLExporter(fc.getChosenFile().toString());
		    tpl.doIt();
		}
	    }
	    //
	    if (mev.selectedItem == exportOCLog) {
		OCLogExport.doit();
		tablePanel.resetModel();
	    }
	    if (mev.selectedItem == exportExplorist) {
		ExploristExporter mag = new ExploristExporter();
		mag.doIt();
	    }
	    if (mev.selectedItem == exportSpoilerPOI) {
		SpoilerPOIExporter spoilerpoi = new SpoilerPOIExporter();
		spoilerpoi.doIt();
	    }
	    if (mev.selectedItem == exportGarminPic) {
		GarminPicExporter garminpic = new GarminPicExporter();
		garminpic.doIt();
	    }

	    // /////////////////////////////////////////////////////////////////////
	    // subMenu for maps, part of "Application" menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == downloadmap) {
		MapLoaderGui mLG = new MapLoaderGui();
		// .execute doesn't work because the tcp-socket uses another thread
		// which cannot be startet if here .execute() is used!
		if (mLG.isCreated)
		    mLG.exec(); // no wait for close window
	    }
	    if (mev.selectedItem == importmap) {

		MapImporter map = new MapImporter();
		map.importMap();
	    }
	    if (mev.selectedItem == kalibmap) {
		SelectMap sM = new SelectMap();
		sM.execute();
		if ((sM.getSelectedMap()).length() > 0) {
		    try {
			MapImporter map = new MapImporter(sM.getSelectedMap(), sM.worldfileexists);
			map.execute(null, Gui.CENTER_FRAME);
		    } catch (java.lang.OutOfMemoryError e) {
			new InfoBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(156, "Out of memory error, map to big")).wait(FormBase.OKB);
		    }
		}
	    }
	    if (mev.selectedItem == selectMapPath) {
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, MainForm.profile.getMapsDir());
		fc.addMask("*.wfl");
		fc.setTitle(MyLocale.getMsg(4200, "Select map directory:"));
		if (fc.execute() != FormBase.IDCANCEL) {
		    MainForm.profile.setRelativeMapsDir(MainForm.profile.getMapsSubDir(fc.getChosen().toString()));
		}

	    }
	    // /////////////////////////////////////////////////////////////////////
	    // "Application" pulldown menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == appMenuPreferences) {
		tablePanel.saveColWidth();
		PreferencesScreen preferencesScreen = new PreferencesScreen();
		preferencesScreen.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
		Preferences.itself().readPrefFile();
	    }
	    if (mev.selectedItem == savenoxit) {
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
		tablePanel.saveColWidth();
	    }
	    /*
	     * if(mev.selectedItem == savenexit){ MainForm.profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR); tablePanel.saveColWidth(pref); ewe.sys.Vm.exit(0); }
	     */
	    if (mev.selectedItem == appMenuExit) {
		Preferences.itself().log("End CacheWolf");
		MainTab.itself.saveUnsavedChanges(true);
		Preferences.itself().log("End CacheWolf: changes saved!");
		ewe.sys.Vm.exit(0);
	    }

	    // /////////////////////////////////////////////////////////////////////
	    // "Search" pulldown menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == searchMenuContinue) {
		search();
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == searchMenuStart) {
		SearchCache ssc = new SearchCache(cacheDB);
		ssc.clearSearch();
		search();
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == searchMenuClr) {
		SearchCache ssc = new SearchCache(cacheDB);
		ssc.clearSearch();
		tablePanel.refreshTable();
	    }
	    // /////////////////////////////////////////////////////////////////////
	    // "Filter" pulldown menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == filtApply) {
		Filter flt = new Filter();
		if (MainForm.profile.getFilterActive() >= Filter.FILTER_ACTIVE) {
		    flt.clearFilter();
		} else {
		    flt.setFilter();
		    flt.doFilter();
		}
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == filtCreate) {
		scnFilter.setData(MainForm.profile.getCurrentFilter());
		scnFilter.setPreferredSize(450, 480);
		if (Vm.isMobile())
		    scnFilter.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight()); // Fullscreen
		scnFilter.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == filtInvert) {
		Filter flt = new Filter();
		flt.invertFilter();
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == filtSelected) { // incremental filter
		MainForm.profile.selectionChanged = true;
		CacheHolder ch;
		boolean filterChanged = false;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
		    ch = cacheDB.get(i);
		    // This is an incremental filter, i.e. it keeps the existing filter
		    // status and only adds the marked caches to the filtered set
		    if (ch.isChecked && ch.isVisible()) {
			ch.setFiltered(true);
			filterChanged = true;
		    }
		}
		if (filterChanged && MainForm.profile.getFilterActive() == Filter.FILTER_INACTIVE) {
		    MainForm.profile.setFilterActive(Filter.FILTER_MARKED_ONLY);
		}
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == filtNonSelected) {
		MainForm.profile.selectionChanged = true;
		CacheHolder ch;
		boolean filterChanged = false;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
		    ch = cacheDB.get(i);
		    // incremental filter. Keeps status of all marked caches and
		    // adds unmarked caches to filtered list
		    if (!ch.isChecked && ch.isVisible()) {
			ch.setFiltered(true);
			filterChanged = true;
		    }
		}
		if (filterChanged && MainForm.profile.getFilterActive() == Filter.FILTER_INACTIVE) {
		    MainForm.profile.setFilterActive(Filter.FILTER_MARKED_ONLY);
		}
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == filtBlack) {
		// filtBlack.modifiers=filtBlack.modifiers|MenuItem.Checked;
		MainForm.profile.setShowBlacklisted(!MainForm.profile.showBlacklisted());
		filtBlack.modifiers = MainForm.profile.showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
		SearchCache ssc = new SearchCache(cacheDB);
		ssc.clearSearch();// Clear search & restore filter status
		tablePanel.refreshTable();
	    }
	    // /////////////////////////////////////////////////////////////////////
	    // "Organise" pulldown menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == orgNewWP) {
		if (MainTab.itself.tablePanel.getSelectedCache() >= 0)
		    MainTab.itself.lastselected = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache()).getWayPoint();
		else
		    MainTab.itself.lastselected = "";
		MainTab.itself.newWaypoint(new CacheHolder());
	    }

	    if (mev.selectedItem == orgCopy) {
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
		DataMover dm = new DataMover();
		dm.copyCaches(Preferences.itself().absoluteBaseDir + MainForm.itself.selectProfileDir(Preferences.itself().absoluteBaseDir, Preferences.itself().lastProfile, 0) + "/");
		tablePanel.refreshTable();
	    }

	    if (mev.selectedItem == orgMove) {
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
		DataMover dm = new DataMover();
		dm.moveCaches(Preferences.itself().absoluteBaseDir + MainForm.itself.selectProfileDir(Preferences.itself().absoluteBaseDir, Preferences.itself().lastProfile, 0) + "/");
		tablePanel.refreshTable();
	    }

	    if (mev.selectedItem == orgDelete) {
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
		DataMover dm = new DataMover();
		dm.deleteCaches();
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == orgRebuild) {
		Rebuild rb = new Rebuild();
		rb.rebuild();
		MainForm.profile.updateBearingDistance();
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == orgCheckNotesAndSolver) {
		// Checking every cache if notes or solver data exist
		CWProgressBar cwp = new CWProgressBar(MyLocale.getMsg(219, "Searching..."), 0, cacheDB.size(), true);
		cwp.exec();
		cwp.allowExit(true);
		for (int i = 0; i < cacheDB.size(); i++) {
		    cwp.setPosition(i);
		    CacheHolder ch = cacheDB.get(i);
		    if (ch.mainCache == null) {
			ch.setHasNote(!ch.getCacheDetails(false).getCacheNotes().equals(""));
			ch.setHasSolver(!ch.getCacheDetails(false).getSolver().equals(""));
		    }
		    if (cwp.isClosed())
			break;
		} // for
		cwp.exit(0);
		tablePanel.refreshTable();
	    }
	    if (mev.selectedItem == orgTravelbugs) {
		Form tbs = TravelbugJourneyScreenFactory.createTravelbugJourneyScreen();
		tbs.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
		tbs.execute();
		tbs.close(0);
	    }
	    if (mev.selectedItem == cacheTour) {
		toggleCacheTourVisible();
	    }

	    // /////////////////////////////////////////////////////////////////////
	    // "About" pulldown menu
	    // /////////////////////////////////////////////////////////////////////
	    if (mev.selectedItem == about) {
		InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("info.html"), MyLocale.getMsg(117, "About"), true);
		is.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
		is.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
	    }
	    if (mev.selectedItem == legend) {
		InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("legende.html"), MyLocale.getMsg(155, "Legend"), true);
		is.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
		is.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
	    }
	    if (mev.selectedItem == wolflang) {
		InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("wolflang.html"), MyLocale.getMsg(118, "WolfLanguage"), true);
		is.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
		is.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
	    }
	    if (mev.selectedItem == sysinfo) {
		StringBuffer sb = new StringBuffer(400);
		Font f = mApp.guiFont;
		sb.append(MyLocale.getMsg(121, "Profile"));
		sb.append(": ");
		sb.append(MainForm.profile.dataDir);
		sb.append("<br>");
		sb.append(MyLocale.getMsg(260, "Platform:"));
		sb.append(' ');
		sb.append(Vm.getPlatform());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(261, "Locale lang is:"));
		sb.append(' ');
		sb.append(MyLocale.getLocaleLanguage());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(262, "Locale country is:"));
		sb.append(' ');
		sb.append(MyLocale.getLocaleCountry());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(263, "Decimal separator is:"));
		sb.append(" \"");
		sb.append(Common.getDigSeparator());
		sb.append("\"<br>");
		sb.append(MyLocale.getMsg(264, "Device is PDA:"));
		sb.append(' ');
		sb.append(Vm.isMobile());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(265, "Screen:"));
		sb.append(' ');
		sb.append(Preferences.itself().getScreenWidth());
		sb.append(" x ");
		sb.append(Preferences.itself().getScreenHeight());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(266, "Font size:"));
		sb.append(' ');
		sb.append(f.getSize());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(267, "Entries in DB:"));
		sb.append(' ');
		sb.append(cacheDB.size());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(268, "File separator is:"));
		sb.append(" \"");
		sb.append(Vm.getProperty("file.separator", "def"));
		sb.append("\"<br>");
		sb.append(MyLocale.getMsg(269, "Programme directory is:"));
		sb.append(' ');
		sb.append(FileBase.getProgramDirectory());
		sb.append("<br>");
		sb.append(MyLocale.getMsg(270, "Number of details in RAM is"));
		sb.append(' ');
		sb.append(CacheHolder.cachesWithLoadedDetails.size());
		sb.append(' ');
		sb.append(MyLocale.getMsg(271, "Max.:"));
		sb.append(' ');
		sb.append(Preferences.itself().maxDetails);
		sb.append("<br>");
		sb.append(MyLocale.getMsg(272, "CacheWolf version:"));
		sb.append(' ');
		sb.append(Version.getReleaseDetailed());
		sb.append("<br>");
		InfoScreen is = new InfoScreen(sb.toString(), "System", false);
		is.setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
		is.execute(MainForm.itself.getFrame(), Gui.CENTER_FRAME);
		// Log for debug purposes 
		Preferences.itself().log(STRreplace.replace(sb.toString(), "<br>", Preferences.NEWLINE), null);
	    }
	    if (mev.selectedItem == chkVersion) {
		new InfoBox(MyLocale.getMsg(178, "Version Checking"), Version.getUpdateMessage()).wait(FormBase.OKB);
	    }
	    /*
	    			else if (mev.selectedItem == this.miDetails) {
	    				MainTab.itself.select(MainTab.itself.detailsPanel);
	    			}
	    			else if (mev.selectedItem == miOpen) {
	    				penDoubleClicked(null);
	    			}
	    			else if (mev.selectedItem == this.miSpoiler) {
	    				MainTab.itself.select(MainTab.itself.imagesPanel);
	    			}
	    			else if (mev.selectedItem == this.miHints) {
	    				MainTab.itself.select(MainTab.itself.hintLogPanel);
	    			}
	    			else if (mev.selectedItem == this.miSolver) {
	    				MainTab.itself.select(MainTab.itself.solverPanel);
	    			}
	    			else if (mev.selectedItem == this.miCalculator) {
	    				MainTab.itself.select(MainTab.itself.calcPanel);
	    			}
	    			else if (mev.selectedItem == miGoto) {
	    				MainTab.itself.gotoPanel.setDestinationAndSwitch(cacheDB.get(tablePanel.getSelectedCache()));
	    			}
	    			else if (mev.selectedItem == this.miMap) {
	    				MainTab.itself.SwitchToMovingMap();
	    			}
	    			else if (mev.selectedItem == this.miRadar) {
	    				MainTab.itself.select(MainTab.itself.radarPanel);
	    			}
	    */
	    // In case that the triggered event was due to one of the context menu items, process
	    // the event by the context menu handler
	    tablePanel.myTableControl.popupMenuEvent(mev.selectedItem);

	} else if (ev instanceof ControlEvent) {
	    if (ev.type == ControlEvent.MENU_SHOWN) {
		MainTab.itself.tablePanel.myTableControl.adjustAddiHideUnhideMenu();
	    }
	}
    }

}

/**
 * It allows the copying of the current centre to the profile centre and versa
 */
class EditCenter extends Form {

    private mButton btnOK, btnCurrentCentre, btnProfileCentre, btnCur2Prof, btnProf2Cur;
    mCheckBox chkSetCurrentCentreFromGPSPosition;
    private CellPanel content = new CellPanel();

    public EditCenter() {
	super();

	resizable = false;
	content.setText(MyLocale.getMsg(1115, "Centre"));
	content.borderStyle = UIConstants.BDR_RAISEDOUTER | UIConstants.BDR_SUNKENINNER | UIConstants.BF_RECT;
	//defaultTags.set(this.INSETS,new Insets(2,2,2,2));
	title = MyLocale.getMsg(1118, "Profile") + ": " + MainForm.profile.name;
	content.addNext(new mLabel(MyLocale.getMsg(108, "Preferences")));
	content.addLast(chkSetCurrentCentreFromGPSPosition = new mCheckBox(MyLocale.getMsg(646, "centre from GPS")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
	// content.addLast(btnGPS2Cur=new mButton("   v   "),DONTSTRETCH,DONTFILL|LEFT);
	if (Preferences.itself().setCurrentCentreFromGPSPosition)
	    chkSetCurrentCentreFromGPSPosition.setState(true);
	content.addNext(new mLabel(MyLocale.getMsg(1116, "Current")));
	content.addLast(btnCurrentCentre = new mButton(Preferences.itself().curCentrePt.toString()), HSTRETCH, HFILL | LEFT);
	content.addNext(new mLabel("      "), HSTRETCH, HFILL);
	content.addNext(btnCur2Prof = new mButton("   v   "), DONTSTRETCH, DONTFILL | LEFT);
	content.addNext(new mLabel(MyLocale.getMsg(1117, "copy")));
	content.addLast(btnProf2Cur = new mButton("   ^   "), DONTSTRETCH, DONTFILL | RIGHT);
	content.addNext(new mLabel(MyLocale.getMsg(1118, "Profile")));
	content.addLast(btnProfileCentre = new mButton(MainForm.profile.center.toString()), HSTRETCH, HFILL | LEFT);
	addLast(content, HSTRETCH, HFILL);
	//addLast(new mLabel(""),VSTRETCH,FILL);
	//addNext(btnCancel = new mButton(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
	addLast(btnOK = new mButton("OK"), DONTSTRETCH, HFILL | RIGHT);
    }

    /**
     * The event handler to react to a users selection.
     * A return value is created and passed back to the calling form
     * while it closes itself.
     */
    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    /*if (ev.target == btnCancel){
	    	close(-1);
	    }*/
	    if (ev.target == btnOK) {
		Preferences.itself().setCurrentCentreFromGPSPosition = chkSetCurrentCentreFromGPSPosition.getState();
		close(1);
	    }
	    if (ev.target == btnCurrentCentre) {
		CoordsInput cs = new CoordsInput();
		cs.setFields(Preferences.itself().curCentrePt, TransformCoordinates.DMM);
		if (cs.execute() == FormBase.IDOK) {
		    MainForm.itself.setCurCentrePt(cs.getCoords());
		    btnCurrentCentre.setText(Preferences.itself().curCentrePt.toString());
		}
	    }
	    if (ev.target == btnProfileCentre) {
		CoordsInput cs = new CoordsInput();
		cs.setFields(MainForm.profile.center, TransformCoordinates.DMM);
		if (cs.execute() == FormBase.IDOK) {
		    MainForm.profile.notifyUnsavedChanges(cs.getCoords().equals(MainForm.profile.center));
		    MainForm.profile.center.set(cs.getCoords());
		    btnProfileCentre.setText(MainForm.profile.center.toString());
		}
	    }
	    if (ev.target == btnCur2Prof) {
		MainForm.profile.notifyUnsavedChanges(Preferences.itself().curCentrePt.equals(MainForm.profile.center));
		MainForm.profile.center.set(Preferences.itself().curCentrePt);
		btnProfileCentre.setText(MainForm.profile.center.toString());
	    }
	    if (ev.target == btnProf2Cur) {
		MainForm.itself.setCurCentrePt(MainForm.profile.center);
		btnCurrentCentre.setText(Preferences.itself().curCentrePt.toString());
	    }
	}
	super.onEvent(ev);
    }

}

/**
 * A SearchBox is a customized input box optimized for searching in CacheWolf. The actual
 * implementation is able to display a CheckBox which with label "in Notes/Description" and logs.

 * @author Engywuck
 */
class SearchBox extends InputBox {

    protected mCheckBox useNoteDesc;
    protected mCheckBox useLogs;
    protected boolean buildingForm = false;

    /**
     * Creates the search box with given title.
     * @param title The titel of the box
     */
    public SearchBox(String title) {
	super(title);
    }

    /**
     * Displays the search Box and returns the String value entered, if OK is pressed, otherwise
     * the value is null.
     * @param initialValue Initial value to display in the search box
     * @param checkUseNoteDesc Initial value for check box
     * @param checkUseLogs Initial value for check box
     * @param pWidth ?
     * @return String to search for if ok is pressed and a string is entered, <code>null</code> otherwise.
     */
    public String input(String initialValue, boolean checkUseNoteDesc, boolean checkUseLogs, int pWidth) {
	return this.input(null, initialValue, checkUseNoteDesc, checkUseLogs, pWidth);
    }

    protected String input(Frame pParent, String initialValue, boolean checkUseNoteDesc, boolean checkUseLogs, int pWidth) {
	String result;
	buildingForm = true;
	useNoteDesc = new mCheckBox(MyLocale.getMsg(218, "Also in description/notes"));//,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	useNoteDesc.setState(checkUseNoteDesc);
	useLogs = new mCheckBox(MyLocale.getMsg(225, "Also in logs"));//,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	useLogs.setState(checkUseLogs);
	result = super.input(pParent, initialValue, pWidth);
	return result;
    }

    /**
     * Queries the check box to search in Notes and Description if it is checked or not.
     * @return <code>True</code> if check box is checked, <code>false</code> if not.
     */
    public boolean useNoteDesc() {
	boolean result = false;
	if (useNoteDesc != null) {
	    result = useNoteDesc.getState();
	}
	return result;
    }

    /**
     * Queries the check box to search in Logs if it is checked or not.
     * @return <code>True</code> if check box is checked, <code>false</code> if not.
     */
    public boolean useLogs() {
	boolean result = false;
	if (useLogs != null) {
	    result = useLogs.getState();
	}
	return result;
    }

    public Control addLast(Control c) {
	// This method is a dirty hack, because in InputBox every thing, from creation of the 
	// controls to displaying it and returning the return value is done at once.
	// To be able to add other controls, I have to enhance the addLast(Control) method - not
	// nice but it works.
	Control result;
	if (!buildingForm) {
	    result = super.addLast(c);
	} else {
	    buildingForm = false;
	    this.addControlsBeforeInput();
	    result = super.addLast(c);
	    this.addControlsAfterInput();
	}
	return result;
    }

    /**
     * Called before creating the input box. Additional controls may be added here.
     */
    private void addControlsBeforeInput() {
	// For future use 
    }

    /**
     * Called after creating the input box. Additional controls may be added here.
     */
    private void addControlsAfterInput() {
	if (useNoteDesc != null) {
	    this.addLast(useNoteDesc, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
	}
	if (useLogs != null) {
	    this.addLast(useLogs, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
	}
    }

}
