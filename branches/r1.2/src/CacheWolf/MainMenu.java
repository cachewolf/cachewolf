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
import CacheWolf.imp.GCVoteImporter;
import CacheWolf.imp.GPXImporter;
import CacheWolf.imp.LOCXMLImporter;
import CacheWolf.imp.OCGPXfetch;
import CacheWolf.imp.OCLinkImporter;
import CacheWolf.imp.OCXMLImporter;
import CacheWolf.imp.OCXMLImporterScreen;
import CacheWolf.imp.SpiderGC;
import CacheWolf.navi.MapImporter;
import CacheWolf.navi.MapLoaderGui;
import CacheWolf.navi.SelectMap;
import CacheWolf.view.TravelbugJourneyScreenFactory;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Font;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.sys.Vm;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.ui.PullDownMenu;
import ewe.ui.mApp;
import ewe.util.Vector;

/**
 * This class creates the menu for cachewolf. It is also responsible for reacting to user inputs in the menu.<br>
 * This class id=100
 * 
 * @see MainForm
 * @see MainTab Last change: 20061123 salzkammergut Tidied up, added MyLocale, added additional internationalisation, combine save/filter for small screens, garminConn
 */
public class MainMenu extends MenuBar {
	private MenuItem preferences, mnuContext, loadcaches, loadOC, loadOCFinds, savenoxit, exit, search, searchAll, searchClr;
	private MenuItem downloadmap, kalibmap, importmap, selectMapPath;
	private MenuItem spider, spiderRoute, spiderQuick, spiderAllFinds, loadGCVotes, fetchOCLink, update, chkVersion;
	private MenuItem about, wolflang, sysinfo, legend;
	private MenuItem exportGpxNg, exporthtml, exporttop50, exportASC, exportTomTom, exportMSARCSV, exportSpoilerPOI;
	private MenuItem exportOZI, exportKML, exportTPL, exportExplorist, exportOCLog;
	private MenuItem exportGarminPic;
	private MenuItem filtCreate, filtClear, filtInvert, filtSelected, filtNonSelected, filtBlack, filtApply;
	private MenuItem exportLOC, exportGPS, mnuSeparator = new MenuItem("-");
	private MenuItem orgNewWP, orgCopy, orgMove, orgDelete, orgRebuild, orgCheckNotesAndSolver;
	public MenuItem cacheTour, orgTravelbugs;
	private MenuItem mnuNewProfile, mnuOpenProfile, mnuDeleteProfile, mnuRenameProfile, mnuEditCenter;
	private Form father;
	private TablePanel tbp;
	private FilterScreen scnFilter = new FilterScreen();
	private static boolean searchInDescriptionAndNotes = false;
	private static boolean searchInLogs = false;
	GCVoteImporter sGCV = null;

	public MainMenu(Form f) {

		Global.getPref().setgpsbabel();

		father = f;

		// /////////////////////////////////////////////////////////////////////
		// subMenu for profiles, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] mnuProfile = { mnuNewProfile = new MenuItem(MyLocale.getMsg(1107, "New")), //
				mnuOpenProfile = new MenuItem(MyLocale.getMsg(1109, "Open")), //
				mnuDeleteProfile = new MenuItem(MyLocale.getMsg(1125, "Delete")), //
				mnuRenameProfile = new MenuItem(MyLocale.getMsg(1126, "Rename")), //
		};
		Menu profileMenu = new Menu(mnuProfile, MyLocale.getMsg(121, "Profiles"));

		// /////////////////////////////////////////////////////////////////////
		// subMenu for import, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		if (Global.getPref().spiderRoute)
			spiderRoute = new MenuItem(MyLocale.getMsg(137, "Download along a Route from geocaching.com"));
		else
			spiderRoute = mnuSeparator;
		MenuItem[] mnuImport = { loadcaches = new MenuItem(MyLocale.getMsg(129, "Import GPX")), //
				mnuSeparator, //
				loadOC = new MenuItem(MyLocale.getMsg(130, "Download from opencaching")), //
				loadOCFinds = new MenuItem(MyLocale.getMsg(163, "Finds from opencaching")), //
				mnuSeparator, //
				spider = new MenuItem(MyLocale.getMsg(131, "Download from geocaching.com")), //
				spiderAllFinds = new MenuItem(MyLocale.getMsg(217, "Spider all finds from geocaching.com")), //
				spiderRoute, //
				mnuSeparator, // diese Zeile entfernen, wenn spiderQuick wieder aktiviert wird
				// spiderQuick = new MenuItem(MyLocale.getMsg(138, "from GC quick")), //
				loadGCVotes = new MenuItem(MyLocale.getMsg(1208, "Import ratings from GCVote")), //
				fetchOCLink = new MenuItem(MyLocale.getMsg(1209, "Fetch link to OC - Cache")), //
				mnuSeparator, //
				update = new MenuItem(MyLocale.getMsg(1014, "Update cache data")), //
		};
		Menu importMenu = new Menu(mnuImport, MyLocale.getMsg(175, "Import"));

		// /////////////////////////////////////////////////////////////////////
		// subMenu for export, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = { exporthtml = new MenuItem(MyLocale.getMsg(100, "to HTML")), //
				exportGpxNg = new MenuItem(MyLocale.getMsg(101, "to GPX Test")), //
				exporttop50 = new MenuItem(MyLocale.getMsg(102, "to TOP50 ASCII")), //
				exportASC = new MenuItem(MyLocale.getMsg(104, "to CSV")), //
				exportTomTom = new MenuItem(MyLocale.getMsg(105, "to TomTom")), //
				exportMSARCSV = new MenuItem(MyLocale.getMsg(106, "to MS AutoRoute CSV")), //
				exportLOC = new MenuItem(MyLocale.getMsg(215, "to LOC")), //
				exportGPS = new MenuItem(MyLocale.getMsg(122, "to GPS")), //
				exportOZI = new MenuItem(MyLocale.getMsg(124, "to OZI")), //
				exportKML = new MenuItem(MyLocale.getMsg(125, "to Google Earth")), //
				exportExplorist = new MenuItem(MyLocale.getMsg(132, "to Explorist")), //
				exportSpoilerPOI = new MenuItem(MyLocale.getMsg(135, "to SpoilerPOI")), //
				exportTPL = new MenuItem(MyLocale.getMsg(128, "via Template")), //
				exportOCLog = new MenuItem(MyLocale.getMsg(1210, "logs to OC")), //
				exportGarminPic = new MenuItem("Garmin pictures"), };
		if (Global.getPref().gpsbabel == null) {
			exportGPS.modifiers = MenuItem.Disabled;
			exportGPS.setText(MyLocale.getMsg(136, "to GPS : gpsbabel missing."));
		}
		Menu exportMenu = new Menu(exitems, MyLocale.getMsg(107, "Export"));

		// /////////////////////////////////////////////////////////////////////
		// subMenu for maps, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] mapMenuItems = { downloadmap = new MenuItem(MyLocale.getMsg(162, "Download calibrated")), //
				importmap = new MenuItem(MyLocale.getMsg(150, "Import")), //
				kalibmap = new MenuItem(MyLocale.getMsg(151, "Calibrate")), //
				selectMapPath = new MenuItem(MyLocale.getMsg(4236, "Change map directory$c")), //
		};
		Menu mapsMenu = new Menu(mapMenuItems, null);

		// Now we start with the horizontal menu bar "Application", "Search", "Filter", "Organise", "About"
		// /////////////////////////////////////////////////////////////////////
		// Create the "Application" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] appMenuItems = { new MenuItem(MyLocale.getMsg(121, "Profile"), 0, profileMenu), //
				preferences = new MenuItem(MyLocale.getMsg(108, "Preferences")), //
				mnuEditCenter = new MenuItem(MyLocale.getMsg(1110, "Centre")), //
				mnuContext = new MenuItem(MyLocale.getMsg(134, "Current Cache")), //
				mnuSeparator, new MenuItem(MyLocale.getMsg(175, "Import"), 0, importMenu), //
				new MenuItem(MyLocale.getMsg(107, "Export"), 0, exportMenu), //
				new MenuItem(MyLocale.getMsg(149, "Maps"), 0, mapsMenu), //
				mnuSeparator, //
				savenoxit = new MenuItem(MyLocale.getMsg(127, "Save")), //
				exit = new MenuItem(MyLocale.getMsg(111, "Exit")), //
		};
		this.addMenu(new PullDownMenu(MyLocale.getMsg(120, "Application"), new Menu(appMenuItems, null)));

		// /////////////////////////////////////////////////////////////////////
		// Create the "Search" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] searchMenuItems = { search = new MenuItem(MyLocale.getMsg(112, "Search$" + (char) 6)), // char 6 = ctrl +f
				searchAll = new MenuItem(MyLocale.getMsg(133, "Search All")), //
				searchClr = new MenuItem(MyLocale.getMsg(113, "Clear search")), //
		};

		// /////////////////////////////////////////////////////////////////////
		// Create the "Filter" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] filterMenuItems = { filtApply = new MenuItem(MyLocale.getMsg(709, "Apply")), filtCreate = new MenuItem(MyLocale.getMsg(114, "Create")), filtInvert = new MenuItem(MyLocale.getMsg(115, "Invert")),
				filtClear = new MenuItem(MyLocale.getMsg(116, "Clear")), //
				mnuSeparator, //
				filtSelected = new MenuItem(MyLocale.getMsg(160, "Filter selected")), //
				filtNonSelected = new MenuItem(MyLocale.getMsg(1011, "Filter out non selected")), //
				mnuSeparator, filtBlack = new MenuItem(MyLocale.getMsg(161, "Show Blacklist")), //
		};
		filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;

		// /////////////////////////////////////////////////////////////////////
		// Create a combined "Filter and Search" pulldown menu for devices with small screens
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] filterAndSearchMenuItems = { filtApply, //
				filtCreate, //
				filtInvert, //
				filtClear, //
				mnuSeparator, //
				filtSelected, //
				filtNonSelected, //
				mnuSeparator, //
				filtBlack, //
				mnuSeparator, //
				search, //
				searchClr, //
		};

		// Depending on screen width display either filter and search menus or the combined menu
		if (MyLocale.getScreenWidth() > 300) {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(112, "Search"), new Menu(searchMenuItems, null)));
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159, "Filter"), new Menu(filterMenuItems, null)));
		} else {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159, "Filter"), new Menu(filterAndSearchMenuItems, null)));
		}

		// /////////////////////////////////////////////////////////////////////
		// Create the "Organise" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] organiseMenuItems = { orgNewWP = new MenuItem(MyLocale.getMsg(214, "New Waypoint")), //
				mnuSeparator, //
				orgCopy = new MenuItem(MyLocale.getMsg(141, "Copy")), //
				orgMove = new MenuItem(MyLocale.getMsg(142, "Move")), //
				orgDelete = new MenuItem(MyLocale.getMsg(143, "Delete")), //
				orgRebuild = new MenuItem(MyLocale.getMsg(208, "Rebuild Index")), //
				orgCheckNotesAndSolver = new MenuItem(MyLocale.getMsg(220, "Check Notes/Solver")), //
				mnuSeparator, //
				orgTravelbugs = new MenuItem(MyLocale.getMsg(139, "Manage travelbugs")), //
				cacheTour = new MenuItem(MyLocale.getMsg(198, "Cachetour")), //
		};
		this.addMenu(new PullDownMenu(MyLocale.getMsg(140, "Organise"), new Menu(organiseMenuItems, null)));

		// /////////////////////////////////////////////////////////////////////
		// Create the "About" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] aboutMenuItems = { about = new MenuItem(MyLocale.getMsg(117, "About")), legend = new MenuItem(MyLocale.getMsg(155, "Legend")), wolflang = new MenuItem(MyLocale.getMsg(118, "WolfLanguage")),
				sysinfo = new MenuItem(MyLocale.getMsg(157, "System")), chkVersion = new MenuItem(MyLocale.getMsg(158, "Version Check")), };
		this.addMenu(new PullDownMenu(MyLocale.getMsg(117, "About"), new Menu(aboutMenuItems, null)));
	}

	public void setTablePanel(TablePanel t) {
		tbp = t;
		if (mnuContext.subMenu == null) {
			if (("," + Global.getPref().listColMap + ",").indexOf(",0,") >= 0)
				mnuContext.subMenu = tbp.tc.getMenuFull();
			else
				mnuContext.subMenu = tbp.tc.getMenuSmall();
		}
	}

	public void allowProfileChange(boolean profileChangeAllowed) {
		if (profileChangeAllowed) {
			mnuNewProfile.modifiers &= ~MenuItem.Disabled;
			mnuOpenProfile.modifiers &= ~MenuItem.Disabled;
		} else {
			mnuNewProfile.modifiers |= MenuItem.Disabled;
			mnuOpenProfile.modifiers |= MenuItem.Disabled;
		}
	}

	public static void search() {
		SearchBox inp = new SearchBox(MyLocale.getMsg(119, "Search for:"));
		String srch = inp.input(null, "", searchInDescriptionAndNotes, searchInLogs, 10);
		MyLocale.setSIPOff();
		if (srch != null) {
			searchInDescriptionAndNotes = inp.useNoteDesc();
			searchInLogs = inp.useLogs();
			SearchCache ssc = new SearchCache(Global.getProfile().cacheDB);
			ssc.search(srch, searchInDescriptionAndNotes, searchInLogs);
			Global.mainTab.tbP.refreshTable();
		}
	}

	public void onEvent(Event ev) {
		Preferences pref = Global.getPref();
		Profile profile = Global.getProfile();
		CacheDB cacheDB = profile.cacheDB;
		Global.mainTab.updatePendingChanges();
		if (ev instanceof MenuEvent) { // && ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent) ev;
			// /////////////////////////////////////////////////////////////////////
			// subMenu for profiles, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == mnuNewProfile) {
				if (NewProfileWizard.startNewProfileWizard(getFrame())) {
					tbp.myMod.numRows = 0;
					pref.setCurCentrePt(profile.centre);
					filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
					tbp.refreshTable();
				}
			}
			if (mev.selectedItem == mnuOpenProfile) {
				Global.mainTab.saveUnsavedChanges(true);
				if (pref.selectProfile(profile, Preferences.PROFILE_SELECTOR_FORCED_ON, false)) {
					tbp.myMod.sortedBy = -1;
					tbp.myMod.numRows = 0;
					CacheHolder.removeAllDetails();
					profile.cacheDB.clear();
					InfoBox infB = new InfoBox("CacheWolf", MyLocale.getMsg(5000, "Loading Cache-List"));
					infB.exec();
					infB.waitUntilPainted(1000);
					Vm.showWait(infB, true);
					profile.readIndex(infB);
					Vm.showWait(infB, false);
					pref.setCurCentrePt(profile.centre);
					filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
					Global.mainForm.setTitle(profile.name + " - CW " + Version.getRelease());
					infB.close(0);
					tbp.resetModel();
				}
			}
			if (mev.selectedItem == mnuDeleteProfile) {
				pref.editProfile(2, 227, 226);
			}
			if (mev.selectedItem == mnuRenameProfile) {
				pref.editProfile(3, 228, 229);
			}
			if (mev.selectedItem == mnuEditCenter) {
				ProfileDataForm f = new ProfileDataForm(pref, profile);
				f.execute(getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
				f.close(0);
			}
			// /////////////////////////////////////////////////////////////////////
			// subMenu for import, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == spider) {
				SpiderGC spGC = new SpiderGC(pref, profile);
				Global.mainTab.saveUnsavedChanges(false);
				spGC.doIt();
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
				pref.setOldGCLanguage();
			}
			if (mev.selectedItem == spiderRoute) {
				SpiderGC spGC = new SpiderGC(pref, profile);
				Global.mainTab.saveUnsavedChanges(false);
				spGC.doItAlongARoute();
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
				pref.setOldGCLanguage();
			}
			if (mev.selectedItem == spiderQuick) {
				SpiderGC spGC = new SpiderGC(pref, profile);
				Global.mainTab.saveUnsavedChanges(false);
				spGC.doItQuickFillFromMapList();
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
				pref.setOldGCLanguage();
			}
			if (mev.selectedItem == spiderAllFinds) {
				SpiderGC spGC = new SpiderGC(pref, profile);
				Global.mainTab.saveUnsavedChanges(false);
				spGC.doIt(true);
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
				pref.setOldGCLanguage();
			}
			if (mev.selectedItem == loadGCVotes) {
				if (sGCV == null) sGCV = new GCVoteImporter(pref, profile);
				sGCV.doIt();
				tbp.resetModel();
			}
			if (mev.selectedItem == fetchOCLink) {
				OCLinkImporter.doIt();
				tbp.resetModel();
			}
			if (mev.selectedItem == loadcaches) {
				String dir = pref.getImporterPath("LocGpxImporter");
				FileChooser fc = new FileChooser(FileChooserBase.OPEN | FileChooserBase.MULTI_SELECT, dir);
				fc.addMask("*.gpx,*.zip,*.loc,*.txt,*.log,*.csv");
				fc.setTitle(MyLocale.getMsg(909, "Select file(s)"));
				if (fc.execute() != FormBase.IDCANCEL) {
					dir = fc.getChosenDirectory().toString();
					pref.setImporterPath("LocGpxImporter", dir);
					String files[] = fc.getAllChosen();
					int how = GPXImporter.DOIT_ASK;
					for (int i = 0; i < files.length; i++) {
						String file = dir + "/" + files[i];
						if (file.endsWith("txt") || file.endsWith("log")) {
							FieldnotesImporter fn = new FieldnotesImporter(pref, profile, file);
							fn.doIt();
						} else if (file.endsWith("loc")) {
							LOCXMLImporter loc = new LOCXMLImporter(pref, profile, file);
							loc.doIt();
						} else if (file.endsWith("csv")) {
							CSVImporter mn = new CSVImporter(pref, profile, file);
							mn.doIt();
						} else { // gpx + zip
							GPXImporter gpx = new GPXImporter(pref, profile, file);
							gpx.doIt(how);
							how = gpx.getHow();
						}
					}
				}
				Global.getProfile().setShowBlacklisted(false);
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
				tbp.resetModel();
				pref.setOldGCLanguage();
			}
			if (mev.selectedItem == loadOC) {
				OCXMLImporter oc = new OCXMLImporter(pref, profile);
				oc.doIt();
				Global.getProfile().setShowBlacklisted(false);
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
				tbp.resetModel();
			}
			if (mev.selectedItem == loadOCFinds) {
				OCGPXfetch.doIt();
				Global.getProfile().setShowBlacklisted(false);
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
				tbp.resetModel();
			}
			if (mev.selectedItem == update) {
				updateSelectedCaches(tbp);
				pref.setOldGCLanguage();
			}
			// /////////////////////////////////////////////////////////////////////
			// subMenu for export, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == exporthtml) {
				HTMLExporter htm = new HTMLExporter(pref, profile);
				htm.doIt();
			}
			if (mev.selectedItem == exportGpxNg) {
				GpxExportNg gpx = new GpxExportNg();
				gpx.doit();
			}
			if (mev.selectedItem == exporttop50) {
				OVLExporter ovl = new OVLExporter(pref, profile);
				ovl.doIt();
			}
			if (mev.selectedItem == exportASC) {
				ASCExporter asc = new ASCExporter(pref, profile);
				asc.doIt();
			}
			if (mev.selectedItem == exportTomTom) {
				TomTomExporter tt = new TomTomExporter();
				tt.doIt();
			}
			if (mev.selectedItem == exportMSARCSV) {
				MSARCSVExporter msar = new MSARCSVExporter(pref, profile);
				msar.doIt();
			}
			if (mev.selectedItem == exportLOC) {
				LocExporter loc = new LocExporter();
				loc.doIt();
			}
			if (mev.selectedItem == exportGPS) {
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
				gpsBabelCommand = pref.gpsbabel + " " + pref.garminGPSBabelOptions + " -i geo -f " + tmpFileName + " -o garmin -F " + pref.garminConn + ":";
				pref.log("[MainMenu:onEvent] " + gpsBabelCommand);
				try {
					// this will *only* work with ewe.jar at the moment
					ewe.sys.Process p = Vm.exec(gpsBabelCommand);
					p.waitFor();
				} catch (IOException ioex) {
					Vm.showWait(false);
					(new MessageBox("Error", "Garmin export unsuccessful", FormBase.OKB)).execute();
					pref.log("Error exporting to Garmin", ioex, true);
				}
				;
				ProgressBarForm.clear();
				Vm.showWait(false);
			}
			if (mev.selectedItem == exportOZI) {
				OziExporter ozi = new OziExporter(pref, profile);
				ozi.doIt();
			}
			if (mev.selectedItem == exportKML) {
				KMLExporter kml = new KMLExporter(pref, profile);
				kml.doIt();
			}
			if (mev.selectedItem == exportTPL) {
				FileChooser fc = new FileChooser(FileChooserBase.OPEN, FileBase.getProgramDirectory() + FileBase.separator + "templates");
				fc.addMask("*.tpl");
				fc.setTitle(MyLocale.getMsg(910, "Select Template file"));
				if (fc.execute() != FormBase.IDCANCEL) {
					TPLExporter tpl = new TPLExporter(pref, profile, fc.getChosenFile().toString());
					tpl.doIt();
				}
			}
			//
			if (mev.selectedItem == exportOCLog) {
				OCLogExport.doit();
				tbp.resetModel();
			}
			if (mev.selectedItem == exportExplorist) {
				ExploristExporter mag = new ExploristExporter(pref, profile);
				mag.doIt();
			}
			if (mev.selectedItem == exportSpoilerPOI) {
				SpoilerPOIExporter spoilerpoi = new SpoilerPOIExporter(pref, profile);
				spoilerpoi.doIt();
			}
			if (mev.selectedItem == exportGarminPic) {
				GarminPicExporter garminpic = new GarminPicExporter(pref, profile);
				garminpic.doIt();
			}

			// /////////////////////////////////////////////////////////////////////
			// subMenu for maps, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == downloadmap) {
				MapLoaderGui mLG = new MapLoaderGui(cacheDB);
				mLG.exec(); // .execute doesn't work because the tcp-socket uses another thread which cannot be startet if here .execute() is used!
			}
			if (mev.selectedItem == importmap) {

				MapImporter map = new MapImporter(pref);
				map.importMap();
			}
			if (mev.selectedItem == kalibmap) {
				SelectMap sM = new SelectMap();
				sM.execute();
				if ((sM.getSelectedMap()).length() > 0) {
					try {
						MapImporter map = new MapImporter(pref, sM.getSelectedMap(), sM.worldfileexists);
						map.execute(null, Gui.CENTER_FRAME);
					} catch (java.lang.OutOfMemoryError e) {
						MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(156, "Out of memory error, map to big"), FormBase.OKB);
						tmpMB.exec();
					}
				}
			}
			if (mev.selectedItem == selectMapPath) {
				FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.getPref().getCustomMapsPath());
				fc.addMask("*.wfl");
				fc.setTitle(MyLocale.getMsg(4200, "Select map directory:"));
				if (fc.execute() != FormBase.IDCANCEL) {
					Global.getPref().saveCustomMapsPath(fc.getChosen().toString());
					// mm.loadMaps(Global.getPref().getCustomMapsPath(), mm.posCircle.where.latDec);
					// mm.forceMapLoad();
				}

			}
			// /////////////////////////////////////////////////////////////////////
			// "Application" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == preferences) {
				tbp.saveColWidth(pref);
				PreferencesScreen pfs = new PreferencesScreen(pref);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
				pref.readPrefFile();
			}
			if (mev.selectedItem == savenoxit) {
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				tbp.saveColWidth(pref);
			}
			/*
			 * if(mev.selectedItem == savenexit){ profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR); tbp.saveColWidth(pref); ewe.sys.Vm.exit(0); }
			 */
			if (mev.selectedItem == exit) {
				Global.mainTab.saveUnsavedChanges(true);
				ewe.sys.Vm.exit(0);
			}

			// /////////////////////////////////////////////////////////////////////
			// "Search" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == search) {
				search();
			}
			if (mev.selectedItem == searchAll) {
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();
				search();
			}
			if (mev.selectedItem == searchClr) {
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();
			}
			// /////////////////////////////////////////////////////////////////////
			// "Filter" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == filtApply) {
				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtCreate) {
				scnFilter.setData(profile.getCurrentFilter());
				scnFilter.setPreferredSize(450, 480);
				if (Vm.isMobile()) scnFilter.setPreferredSize(MyLocale.getScreenWidth(), MyLocale.getScreenHeight()); // Fullscreen
				scnFilter.execute(father.getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtInvert) {
				Filter flt = new Filter();
				flt.invertFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtClear) {
				Filter flt = new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtSelected) { // incremental filter
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				boolean filterChanged = false;
				for (int i = cacheDB.size() - 1; i >= 0; i--) {
					ch = cacheDB.get(i);
					// This is an incremental filter, i.e. it keeps the existing filter
					// status and only adds the marked caches to the filtered set
					if (ch.is_Checked && ch.isVisible()) {
						ch.setFiltered(true);
						filterChanged = true;
					}
				}
				if (filterChanged && Global.getProfile().getFilterActive() == Filter.FILTER_INACTIVE) {
					Global.getProfile().setFilterActive(Filter.FILTER_MARKED_ONLY);
				}
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtNonSelected) {
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				boolean filterChanged = false;
				for (int i = cacheDB.size() - 1; i >= 0; i--) {
					ch = cacheDB.get(i);
					// incremental filter. Keeps status of all marked caches and
					// adds unmarked caches to filtered list
					if (!ch.is_Checked && ch.isVisible()) {
						ch.setFiltered(true);
						filterChanged = true;
					}
				}
				if (filterChanged && Global.getProfile().getFilterActive() == Filter.FILTER_INACTIVE) {
					Global.getProfile().setFilterActive(Filter.FILTER_MARKED_ONLY);
				}
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtBlack) {
				// filtBlack.modifiers=filtBlack.modifiers|MenuItem.Checked;
				Global.getProfile().setShowBlacklisted(!Global.getProfile().showBlacklisted());
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers | MenuItem.Checked : filtBlack.modifiers & ~MenuItem.Checked;
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();// Clear search & restore filter status
				tbp.refreshTable();
			}
			// /////////////////////////////////////////////////////////////////////
			// "Organise" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == orgNewWP) {
				if (Global.mainTab.tbP.getSelectedCache() >= 0)
					Global.mainTab.lastselected = cacheDB.get(Global.mainTab.tbP.getSelectedCache()).getWayPoint();
				else
					Global.mainTab.lastselected = "";
				Global.mainTab.newWaypoint(new CacheHolder());
			}

			if (mev.selectedItem == orgCopy) {
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.copyCaches();
				tbp.refreshTable();
			}

			if (mev.selectedItem == orgMove) {
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.moveCaches();
				tbp.refreshTable();
			}

			if (mev.selectedItem == orgDelete) {
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.deleteCaches();
				tbp.refreshTable();
			}
			if (mev.selectedItem == orgRebuild) {
				Rebuild rb = new Rebuild();
				rb.rebuild();
				profile.updateBearingDistance();
				tbp.refreshTable();
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
					if (cwp.isClosed()) break;
				} // for
				cwp.exit(0);
				tbp.refreshTable();
			}
			if (mev.selectedItem == orgTravelbugs) {
				Form tbs = TravelbugJourneyScreenFactory.createTravelbugJourneyScreen();
				tbs.setPreferredSize(MyLocale.getScreenWidth(), MyLocale.getScreenHeight());
				tbs.execute();
				tbs.close(0);
			}
			if (mev.selectedItem == cacheTour) {
				cacheTour.modifiers ^= MenuItem.Checked;
				Global.mainForm.toggleCacheListVisible();
			}

			// /////////////////////////////////////////////////////////////////////
			// "About" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == about) {
				InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("info.html"), MyLocale.getMsg(117, "About"), true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == legend) {
				InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("legende.html"), MyLocale.getMsg(155, "Legend"), true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == wolflang) {
				InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("wolflang.html"), MyLocale.getMsg(118, "WolfLanguage"), true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == sysinfo) {
				StringBuffer sb = new StringBuffer(400);
				Font f = mApp.guiFont;
				sb.append(MyLocale.getMsg(121, "Profile"));
				sb.append(": ");
				sb.append(profile.dataDir);
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
				sb.append(MyLocale.getDigSeparator());
				sb.append("\"<br>");
				sb.append(MyLocale.getMsg(264, "Device is PDA:"));
				sb.append(' ');
				sb.append(Vm.isMobile());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(265, "Screen:"));
				sb.append(' ');
				sb.append(MyLocale.getScreenWidth());
				sb.append(" x ");
				sb.append(MyLocale.getScreenHeight());
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
				sb.append(Global.getPref().maxDetails);
				sb.append("<br>");
				sb.append(MyLocale.getMsg(272, "CacheWolf version:"));
				sb.append(' ');
				sb.append(Version.getReleaseDetailed());
				sb.append("<br>");
				InfoScreen is = new InfoScreen(sb.toString(), "System", false, pref);
				pref.log(STRreplace.replace(sb.toString(), "<br>", Preferences.NEWLINE), null);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == chkVersion) {
				(new MessageBox(MyLocale.getMsg(178, "Version Checking"), Version.getUpdateMessage(), FormBase.OKB)).execute();
			}

			// In case that the triggered event was due to one of the context menu items, process
			// the event by the context menu handler
			tbp.tc.popupMenuEvent(mev.selectedItem);

		} else if (ev instanceof ControlEvent) {
			if (ev.type == ControlEvent.MENU_SHOWN) {
				Global.mainTab.tbP.tc.adjustAddiHideUnhideMenu();
			}
		}
	}

	public static void updateSelectedCaches(TablePanel tablePanel) {
		Preferences pref = Global.getPref();
		Profile profile = Global.getProfile();
		CacheDB cacheDB = profile.cacheDB;
		CacheHolder ch;

		OCXMLImporterScreen options = new OCXMLImporterScreen(MyLocale.getMsg(1014, "updateSelectedCaches"), OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.TRAVELBUGS | OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.ALL);
		if (options.execute() == FormBase.IDCANCEL) {
			return;
		}

		SpiderGC spider = new SpiderGC(pref, profile);
		OCXMLImporter ocSync = new OCXMLImporter(pref, profile);
		Vm.showWait(true);
		boolean alreadySaid = false;
		boolean alreadySaid2 = false;
		InfoBox infB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
		infB.exec();

		boolean loadAllLogs = (pref.maxLogsToSpider > 5);

		Vector cachesToUpdate = new Vector();
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			if (ch.is_Checked == true && ch.isVisible()) {
				// should work even if only the wayPoint is created
				if ((ch.getWayPoint().toUpperCase().startsWith("GC") || ch.isOC()))
				// Notiz: Wenn es ein addi Wpt ist, sollte eigentlich der Maincache gespidert werden
				// Alter code prft aber nur ob ein Maincache von GC existiert und versucht dann den addi direkt zu spidern, was nicht funktioniert
				{
					cachesToUpdate.add(new Integer(i));
				} else {
					if (ch.isAddiWpt() && ch.mainCache != null && !ch.mainCache.is_Checked && !alreadySaid2) { // Is the father ticked?
						alreadySaid2 = true;
						(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(5001, "Can't spider additional waypoint directly. Please check main cache."), FormBase.OKB)).execute();
					}
					if (!ch.isAddiWpt() && !alreadySaid) {
						alreadySaid = true;
						(new MessageBox(MyLocale.getMsg(327, "Information"), ch.getWayPoint() + MyLocale.getMsg(5002, ": At the moment this function is only applicable for geocaching.com and opencaching.de/.cz/.org.uk ."), FormBase.OKB))
								.execute();
					}
				}

			}
		}

		int spiderErrors = 0;
		for (int j = 0; j < cachesToUpdate.size(); j++) {
			int i = ((Integer) cachesToUpdate.get(j)).intValue();
			ch = cacheDB.get(i);
			// infB.setInfo("Loading: " + ch.wayPoint);
			infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + ch.getWayPoint() + " (" + (j + 1) + " / " + cachesToUpdate.size() + ")");
			infB.redisplay();
			if (ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC")) {
				int test = spider.spiderSingle(i, infB, loadAllLogs || ch.is_found());
				if (test == SpiderGC.SPIDER_CANCEL) {
					infB.close(0);
					break;
				} else if (test == SpiderGC.SPIDER_ERROR || test == SpiderGC.SPIDER_IGNORE_PREMIUM) {
					spiderErrors++;
				} else {
					// profile.hasUnsavedChanges=true;
				}
			} else {
				if (!ocSync.syncSingle(i, infB)) {
					infB.close(0);
					break;
				} else {
					// profile.hasUnsavedChanges=true;
				}
			}

			// cacheDB.clear();
			// profile.readIndex();
		}
		infB.close(0);
		profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
		profile.restoreFilter();
		profile.updateBearingDistance();
		tablePanel.refreshTable();
		Vm.showWait(false);
		if (spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500, "Error"), spiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded."), FormBase.DEFOKB).execute();
		}
	}

}
