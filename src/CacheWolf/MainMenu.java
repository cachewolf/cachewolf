package CacheWolf;

import CacheWolf.navi.MapImporter;
import CacheWolf.navi.MapLoaderGui;
import CacheWolf.navi.SelectMap;
import ewe.ui.*;
import ewe.util.Vector;
//import ewe.util.mString;
import ewe.fx.*;
import ewe.sys.*;
//import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;
import exp.*;

/**
 *	This class creates the menu for cachewolf. It is also responsible
 *	for reacting to user inputs in the menu.<br>
 *	This class id=100
 *	@see MainForm
 *	@see MainTab
 *   Last change:
 *     20061123 salzkammergut Tidied up, added MyLocale, added additional internationalisation, combine save/filter for small screens, garminConn
 */
public class MainMenu extends MenuBar {
	private MenuItem profiles, preferences, mnuContext,loadcaches,loadOC, /* savenexit, */ savenoxit,exit,search,searchAll,searchClr;
	private MenuItem downloadmap, kalibmap, importmap;
	private MenuItem spider, update, chkVersion;
	private MenuItem about, wolflang, sysinfo, legend;
	private MenuItem exportpcx5, exporthtml, exporttop50, exportGPX, exportASC, exportTomTom, exportMSARCSV;
	private MenuItem exportOZI, exportKML, exportTPL, exportExplorist;
	private MenuItem filtCreate, filtClear, filtInvert, filtSelected, filtNonSelected, filtBlack, filtApply;
	private MenuItem exportGPS, exportCacheMate,mnuSeparator;
	private MenuItem orgNewWP, orgCopy, orgMove, orgDelete,orgRebuild;
	public MenuItem cacheTour,orgTravelbugs, mnuForceLogin;
	private MenuItem mnuNewProfile, mnuOpenProfile, mnuEditCenter;
	private Form father;
	private TablePanel tbp;
	private FilterScreen scnFilter=new FilterScreen();

	public MainMenu(Form f){
		father = f;
		String cwd = FileBase.getProgramDirectory();

		///////////////////////////////////////////////////////////////////////
		// subMenu for profiles, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mnuProfile = new MenuItem[2];
		mnuProfile[0] = mnuNewProfile = new MenuItem(MyLocale.getMsg(1107,"New"));
		mnuProfile[1] = mnuOpenProfile = new MenuItem(MyLocale.getMsg(1109,"Open"));
		Menu profileMenu = new Menu(mnuProfile,MyLocale.getMsg(121,"Profiles"));

		///////////////////////////////////////////////////////////////////////
		// subMenu for import, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mnuImport = new MenuItem[6];
		mnuImport[0] = loadcaches  = new MenuItem(MyLocale.getMsg(129,"Import GPX")); //TODO internationalization
		mnuImport[1] = loadOC      = new MenuItem(MyLocale.getMsg(130,"Download von opencaching.de")); 
		mnuImport[2] = spider      = new MenuItem(MyLocale.getMsg(131,"Spider von geocaching.com")); 
		mnuImport[3] = update      = new MenuItem(MyLocale.getMsg(1014,"Update cache data"));
		mnuImport[4] = mnuSeparator = new MenuItem("-"); 
		mnuImport[5] = mnuForceLogin      = new MenuItem("Always login to GC"); 
		Menu importMenu = new Menu(mnuImport, MyLocale.getMsg(175,"Import"));
		if (Global.getPref().forceLogin) mnuForceLogin.modifiers^=MenuItem.Checked;

		///////////////////////////////////////////////////////////////////////
		// subMenu for export, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = new MenuItem[12];
		//Vm.debug("Hi in MainMenu "+lr);
		exitems[0] = exporthtml = new MenuItem(MyLocale.getMsg(100,"to HTML"));
		exitems[1] = exportpcx5 = new MenuItem(MyLocale.getMsg(101,"to PCX5 Mapsource"));
		exitems[2] = exporttop50 = new MenuItem(MyLocale.getMsg(102,"to TOP50 ASCII"));
		exitems[3] = exportGPX = new MenuItem(MyLocale.getMsg(103,"to GPX"));
		exitems[4] = exportASC = new MenuItem(MyLocale.getMsg(104,"to CSV"));
		exitems[5] = exportTomTom = new MenuItem(MyLocale.getMsg(105,"to TomTom"));
		exitems[6] = exportMSARCSV = new MenuItem(MyLocale.getMsg(106,"to MS AutoRoute CSV"));
		exitems[7] = exportGPS = new MenuItem(MyLocale.getMsg(122,"to GPS"));
		if(!(new File(cwd + "/gpsbabel.exe")).exists()) exitems[7].modifiers = MenuItem.Disabled;
		//exitems[8] = exportCacheMate = new MenuItem(MyLocale.getMsg(123,"to Cachemate"));
		//if(!(new File(cwd + "/cmconvert/cmconvert.exe")).exists()) exitems[8].modifiers = MenuItem.Disabled;
		exitems[8] = exportOZI = new MenuItem(MyLocale.getMsg(124,"to OZI"));
		exitems[9] = exportKML = new MenuItem(MyLocale.getMsg(125,"to Google Earth"));
		exitems[10] = exportExplorist = new MenuItem(MyLocale.getMsg(132,"to Explorist"));
		exitems[11] = exportTPL = new MenuItem(MyLocale.getMsg(128,"via Template"));

		Menu exportMenu = new Menu(exitems, MyLocale.getMsg(107,"Export"));

		///////////////////////////////////////////////////////////////////////
		// subMenu for maps, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mapMenuItems = new MenuItem[3];
		mapMenuItems[0] = downloadmap = new MenuItem(MyLocale.getMsg(162,"Download calibrated"));
		mapMenuItems[1] = importmap = new MenuItem(MyLocale.getMsg(150,"Import"));
		mapMenuItems[2] = kalibmap = new MenuItem(MyLocale.getMsg(151,"Calibrate"));
		Menu mapsMenu = new Menu(mapMenuItems, null);

		// Now we start with the horizontal menu bar "Application", "Search", "Filter", "Organise", "About"
		///////////////////////////////////////////////////////////////////////
		// Create the "Application" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem [] appMenuItems=new MenuItem[11];
		appMenuItems[0] = profiles 	 = new MenuItem(MyLocale.getMsg(121,"Profile"), 0, profileMenu); 
		appMenuItems[1] = preferences = new MenuItem(MyLocale.getMsg(108,"Preferences")); 
		appMenuItems[2] = mnuEditCenter = new MenuItem(MyLocale.getMsg(1110,"Centre"));
		appMenuItems[3] = mnuContext = new MenuItem(MyLocale.getMsg(134,"Current Cache"));
		appMenuItems[4] = mnuSeparator;
		appMenuItems[5] = new MenuItem(MyLocale.getMsg(175,"Import"),0,importMenu);
		appMenuItems[6] = new MenuItem(MyLocale.getMsg(107,"Export"),0,exportMenu);
		appMenuItems[7] = new MenuItem(MyLocale.getMsg(149,"Maps"),0,mapsMenu);
		appMenuItems[8] = mnuSeparator;
		appMenuItems[9] = savenoxit = new MenuItem(MyLocale.getMsg(127,"Save")); 
		//appMenuItems[10] = savenexit = new MenuItem(MyLocale.getMsg(110,"Save & Exit")); 
		appMenuItems[10] = exit = new MenuItem(MyLocale.getMsg(111,"Exit"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(120,"Application"),new Menu(appMenuItems,null)));

		///////////////////////////////////////////////////////////////////////
		// Create the "Search" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] searchMenuItems=new MenuItem[3];
		searchMenuItems[0] = search = new MenuItem(MyLocale.getMsg(112,"Search$"+(char)6)); // char 6 = ctrl +f 
		searchMenuItems[1] = searchAll = new MenuItem(MyLocale.getMsg(133,"Search All"));
		searchMenuItems[2] = searchClr = new MenuItem(MyLocale.getMsg(113,"Clear search"));

		///////////////////////////////////////////////////////////////////////
		// Create the "Filter" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] filterMenuItems=new MenuItem[9];
		filterMenuItems[0] = filtApply  = new MenuItem(MyLocale.getMsg(709,"Apply")); 
		filterMenuItems[1] = filtCreate  = new MenuItem(MyLocale.getMsg(114,"Create")); 
		filterMenuItems[2] = filtInvert  = new MenuItem(MyLocale.getMsg(115,"Invert")); 
		filterMenuItems[3] = filtClear   = new MenuItem(MyLocale.getMsg(116,"Clear"));
		filterMenuItems[4] = mnuSeparator;
		filterMenuItems[5] = filtSelected = new MenuItem(MyLocale.getMsg(160,"Filter selected"));
		filterMenuItems[6] = filtNonSelected = new MenuItem(MyLocale.getMsg(1011,"Filter out non selected"));
		filterMenuItems[7] = mnuSeparator;
		filterMenuItems[8] = filtBlack   = new MenuItem(MyLocale.getMsg(161,"Show Blacklist"));
		//filterMenuItems[9] = mnuSeparator;
		//filterMenuItems[10] = cacheTour;

		///////////////////////////////////////////////////////////////////////
		// Create a combined "Filter and Search" pulldown menu for devices with small screens
		///////////////////////////////////////////////////////////////////////
		MenuItem[] filterAndSearchMenuItems=new MenuItem[12];
		filterAndSearchMenuItems[0]=filtApply;
		filterAndSearchMenuItems[1]=filtCreate;
		filterAndSearchMenuItems[2]=filtInvert;
		filterAndSearchMenuItems[3]=filtClear;
		filterAndSearchMenuItems[4]=mnuSeparator;
		filterAndSearchMenuItems[5]=filtSelected;
		filterAndSearchMenuItems[6]=filtNonSelected;
		filterAndSearchMenuItems[7]=mnuSeparator;
		filterAndSearchMenuItems[8]=filtBlack;
		filterAndSearchMenuItems[9]=mnuSeparator;
		filterAndSearchMenuItems[10]=search;
		filterAndSearchMenuItems[11]=searchClr;
		//filterAndSearchMenuItems[12] = mnuSeparator;
		//filterAndSearchMenuItems[13] = cacheTour;

		// Depending on screen width display either filter and search menus or the combined menu 
		if (MyLocale.getScreenWidth()>300) {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(112,"Search"),new Menu(searchMenuItems,null)));
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159,"Filter"),new Menu(filterMenuItems,null)));
		} else {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159,"Filter"),new Menu(filterAndSearchMenuItems,null)));
		}

		///////////////////////////////////////////////////////////////////////
		// Create the "Organise" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] organiseMenuItems=new MenuItem[9];
		organiseMenuItems[0] = orgNewWP = new MenuItem(MyLocale.getMsg(214,"New Waypoint"));
		organiseMenuItems[1] = mnuSeparator;
		organiseMenuItems[2] = orgCopy  = new MenuItem(MyLocale.getMsg(141,"Copy")); 
		organiseMenuItems[3] = orgMove  = new MenuItem(MyLocale.getMsg(142,"Move")); 
		organiseMenuItems[4] = orgDelete   = new MenuItem(MyLocale.getMsg(143,"Delete"));
		organiseMenuItems[5] = orgRebuild   = new MenuItem(MyLocale.getMsg(208,"Rebuild Index"));
		organiseMenuItems[6] = mnuSeparator;
		organiseMenuItems[7] = orgTravelbugs = new MenuItem(MyLocale.getMsg(139,"Manage travelbugs"));
		cacheTour = new MenuItem(MyLocale.getMsg(198,"Cachetour"));
		organiseMenuItems[8] = cacheTour;
		this.addMenu(new PullDownMenu(MyLocale.getMsg(140,"Organise"),new Menu(organiseMenuItems,null)));

		///////////////////////////////////////////////////////////////////////
		// Create the "About" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] aboutMenuItems=new MenuItem[5];
		aboutMenuItems[0] = about = new MenuItem(MyLocale.getMsg(117,"About")); 
		aboutMenuItems[1] = legend = new MenuItem(MyLocale.getMsg(155,"Legend")); 
		aboutMenuItems[2] = wolflang = new MenuItem(MyLocale.getMsg(118,"WolfLanguage")); 
		aboutMenuItems[3] = sysinfo = new MenuItem(MyLocale.getMsg(157,"System")); 
		aboutMenuItems[4] = chkVersion = new MenuItem(MyLocale.getMsg(158,"Version Check"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(117,"About"),new Menu(aboutMenuItems,null)));
	}

	public void setTablePanel(TablePanel t){
		tbp = t;
		if (mnuContext.subMenu == null) {
			if ((","+Global.getPref().listColMap+",").indexOf(",0,")>=0)
				mnuContext.subMenu = tbp.tc.getMenuFull();
			else
				mnuContext.subMenu = tbp.tc.getMenuSmall();
		}		
	}

	public void allowProfileChange(boolean profileChangeAllowed) {
		if (profileChangeAllowed) {
			mnuNewProfile.modifiers&=~MenuItem.Disabled;
			mnuOpenProfile.modifiers&=~MenuItem.Disabled;
		} else {
			mnuNewProfile.modifiers|=MenuItem.Disabled;
			mnuOpenProfile.modifiers|=MenuItem.Disabled;
		}
	}

	public void setForceLogin() {
		mnuForceLogin.modifiers=Global.getPref().forceLogin ? Global.mainTab.mnuMain.modifiers|MenuItem.Checked : Global.mainTab.mnuMain.modifiers&~MenuItem.Checked;
	}

	public static void search() {
		String srch = new InputBox(MyLocale.getMsg(119,"Search for:")).input("",10);
		if (srch != null) {
			SearchCache ssc = new SearchCache(Global.getProfile().cacheDB);
			ssc.search(srch);
			Global.mainTab.tbP.refreshTable();
		}
	}
	public void onEvent(Event ev){
		Preferences pref=Global.getPref();
		Profile profile=Global.getProfile();
		Vector cacheDB=profile.cacheDB;
		Global.mainTab.updatePendingChanges();
		if (ev instanceof MenuEvent){ //&& ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent)ev;
			///////////////////////////////////////////////////////////////////////
			// subMenu for profiles, part of "Application" menu 
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == mnuNewProfile){
				if (NewProfileWizard.startNewProfileWizard(getFrame()) ) {
					pref.curCentrePt = new CWPoint(profile.centre);
					Filter.showBlacklisted=false;
					filtBlack.modifiers&=~MenuItem.Checked;
					tbp.refreshTable();
				}
			}
			if(mev.selectedItem == mnuOpenProfile){
				Global.mainTab.saveUnsavedChanges(true);
				if (pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_FORCED_ON,false)) {
					tbp.myMod.numRows=0;
					CacheHolder.removeAllDetails();
					profile.cacheDB.clear();
					InfoBox infB = new InfoBox("CacheWolf",MyLocale.getMsg(5000,"Loading Cache-List"));
					infB.exec();
					infB.waitUntilPainted(1000);
					Vm.showWait(infB, true);
					profile.readIndex();
					Vm.showWait(infB, false);
					pref.curCentrePt.set(profile.centre);
					filtBlack.modifiers&=~MenuItem.Checked;
					Filter.showBlacklisted=false;
					Global.mainForm.setTitle("Cachewolf "+Version.getRelease()+" - "+profile.name);
					infB.close(0);
					tbp.resetModel();
				}
			}
			if(mev.selectedItem == mnuEditCenter){
				ProfileDataForm f=new ProfileDataForm(pref,profile);
				f.execute(getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
				f.close(0);
			}
			///////////////////////////////////////////////////////////////////////
			// subMenu for import, part of "Application" menu 
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == spider){
				SpiderGC spGC = new SpiderGC(pref, profile, true);
				Global.mainTab.saveUnsavedChanges(false);
				spGC.doIt();
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
			}
			if(mev.selectedItem == loadcaches){
				FileChooser fc = new FileChooser(FileChooserBase.OPEN|FileChooserBase.MULTI_SELECT, pref.baseDir);
				fc.addMask("*.gpx,*.zip,*.loc");
				fc.setTitle(MyLocale.getMsg(909,"Select file(s)"));
				if(fc.execute() != FormBase.IDCANCEL){
					String dir = fc.getChosenDirectory().toString();
					String files[] = fc.getAllChosen();
					/*
					int how = GPXImporter.DOIT_ASK;
					if (files.length > 0){
							InfoBox iB = new InfoBox("Spider?", "Spider Images?", InfoBox.CHECKBOX);
							iB.execute();
							boolean doSpider = iB.mCB_state;
							if (doSpider) how = GPXImporter.DOIT_WITHSPOILER;
							else how = GPXImporter.DOIT_NOSPOILER;
					}
					 */
					for (int i = 0; i < files.length; i++){ 
						String file = dir + "/" + files[i];
						if (file.endsWith("loc")){
							LOCXMLImporter loc = new LOCXMLImporter(pref, profile, file);
							loc.doIt();
						}
						else {
							GPXImporter gpx = new GPXImporter(pref, profile, file);
							gpx.doIt(0);
						}
					}
				}
				Filter.showBlacklisted=false;
				filtBlack.modifiers&=~MenuItem.Checked;
				tbp.resetModel();
			}
			if(mev.selectedItem == loadOC){
				OCXMLImporter oc = new OCXMLImporter(pref,profile);
				oc.doIt();
				Filter.showBlacklisted=false;
				filtBlack.modifiers&=~MenuItem.Checked;
				tbp.resetModel();
			}
			if (mev.selectedItem == update) 
				updateSelectedCaches(tbp);
			if(mev.selectedItem == mnuForceLogin) {
				mnuForceLogin.modifiers^=MenuItem.Checked;
				Global.getPref().forceLogin=(mnuForceLogin.modifiers&MenuItem.Checked)!=0;
				Global.getPref().savePreferences();
			}
			///////////////////////////////////////////////////////////////////////
			// subMenu for export, part of "Application" menu 
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == exporthtml){
				HTMLExporter htm = new HTMLExporter(pref, profile);
				htm.doIt();
			}
			if(mev.selectedItem == exportpcx5){
				PCX5Exporter pcx = new PCX5Exporter( pref, profile);
				pcx.doIt(PCX5Exporter.MODE_ASK);
			} 
			if(mev.selectedItem == exporttop50){
				OVLExporter ovl = new OVLExporter(pref, profile);
				ovl.doIt();
			}
			if(mev.selectedItem == exportGPX){
				GPXExporter htm = new GPXExporter(pref, profile);
				htm.doIt(1);
			}
			if(mev.selectedItem == exportASC){
				ASCExporter asc = new ASCExporter(pref,profile);
				asc.doIt();
			}
			if(mev.selectedItem == exportTomTom){
				TomTomExporter tt = new TomTomExporter();
				tt.doIt();
			}
			if(mev.selectedItem == exportMSARCSV){
				MSARCSVExporter msar = new MSARCSVExporter(pref,profile);
				msar.doIt();
			}
			if(mev.selectedItem == exportGPS){
				Vm.showWait(true);
				LocExporter loc = new LocExporter();
				String tmpFileName = FileBase.getProgramDirectory() + "/temp.loc";
				loc.setTmpFileName(tmpFileName);
				loc.doIt(LocExporter.MODE_AUTO);
				ProgressBarForm.display(MyLocale.getMsg(950,"Transfer"),MyLocale.getMsg(951,"Sending to GPS"), null);
				try{
					pref.log("gpsbabel.exe "+pref.garminGPSBabelOptions+" -i geo -f \""+ tmpFileName +"\" -o garmin -F " + pref.garminConn +":");
					// this will *only* work with ewe.jar at the moment
					ewe.sys.Process p = Vm.exec("gpsbabel.exe "+pref.garminGPSBabelOptions+" -i geo -f \""+ tmpFileName +"\" -o garmin -F " + pref.garminConn +":");
					p.waitFor();
				}catch(IOException ioex){
					Vm.showWait(false);
					(new MessageBox("Error", "Garmin export unsuccessful", FormBase.OKB)).execute();
					pref.log("Error exporting to Garmin",ioex);
				};
				ProgressBarForm.clear();
				Vm.showWait(false);
			}
			if(mev.selectedItem == exportOZI){
				OziExporter ozi = new OziExporter( pref, profile);
				ozi.doIt();
			}
			if(mev.selectedItem == exportKML){
				KMLExporter kml = new KMLExporter( pref, profile);
				kml.doIt();
			}
			if(mev.selectedItem == exportTPL){
				FileChooser fc = new FileChooser(FileChooserBase.OPEN, FileBase.getProgramDirectory());
				fc.addMask("*.tpl");
				fc.setTitle(MyLocale.getMsg(910,"Select Template file"));
				if(fc.execute() != FormBase.IDCANCEL){
					TPLExporter tpl = new TPLExporter( pref,profile, fc.getChosenFile().toString());
					tpl.doIt();
				}
			}
			if(mev.selectedItem == exportExplorist) {
				ExploristExporter mag = new ExploristExporter( pref, profile);
				mag.doIt();
			}
			///////////////////////////////////////////////////////////////////////
			// subMenu for maps, part of "Application" menu 
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == downloadmap){
				MapLoaderGui mLG = new MapLoaderGui(cacheDB);
				mLG.exec(); // .execute doesn't work because the tcp-socket uses another thread which cannot be startet if here .execute() is used!
			}
			if(mev.selectedItem == importmap){

				MapImporter map = new MapImporter(pref);
				map.importMap();
			}
			if(mev.selectedItem == kalibmap){
				SelectMap sM = new SelectMap();
				sM.execute();
				if((sM.getSelectedMap()).length()>0){
					try {
						MapImporter map = new MapImporter(pref, sM.getSelectedMap(),sM.worldfileexists);
						map.execute(null, Gui.CENTER_FRAME);
					} catch (java.lang.OutOfMemoryError e) {
						MessageBox tmpMB=new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(156,"Out of memory error, map to big"), FormBase.OKB);
						tmpMB.exec();
					}
				}
			}
			///////////////////////////////////////////////////////////////////////
			// "Application" pulldown menu
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == preferences){
				tbp.saveColWidth(pref);
				PreferencesScreen pfs = new PreferencesScreen(pref);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
				pref.readPrefFile();
			}
			if(mev.selectedItem == savenoxit){
				profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
				tbp.saveColWidth(pref);
			}
/*
			if(mev.selectedItem == savenexit){
				profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
				tbp.saveColWidth(pref);
				ewe.sys.Vm.exit(0);
			}
*/
			if(mev.selectedItem == exit){
				Global.mainTab.saveUnsavedChanges(true);
				ewe.sys.Vm.exit(0);
			}

			///////////////////////////////////////////////////////////////////////
			// "Search" pulldown menu
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == search){
				search();
			}
			if(mev.selectedItem == searchAll){
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();		
                search();
			}
			if(mev.selectedItem == searchClr){
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();		
			}
			///////////////////////////////////////////////////////////////////////
			// "Filter" pulldown menu
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == filtApply){
				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtCreate){
				scnFilter.setData();
				scnFilter.execute(father.getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtInvert){
				Filter flt = new Filter();
				flt.invertFilter();
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtClear){
				Filter flt = new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtSelected){ // incremental filter
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				for(int i = cacheDB.size()-1; i>=0; i--){
					ch = (CacheHolder)cacheDB.get(i);
					// This is an incremental filter, i.e. it keeps the existing filter
					// status and only adds the marked caches to the filtered set
					ch.is_filtered = ch.is_Checked || ch.is_filtered;
				}
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtNonSelected){
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				for(int i = cacheDB.size()-1; i >=0; i--){
					ch = (CacheHolder)cacheDB.get(i);
					// incremental filter. Keeps status of all marked caches and
					// adds unmarked caches to filtered list
					ch.is_filtered = !ch.is_Checked || ch.is_filtered;
				}
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtBlack){
				filtBlack.modifiers^=MenuItem.Checked;
				Filter.showBlacklisted=!Filter.showBlacklisted;
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();// Clear search & restore filter status
				Filter flt=new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			///////////////////////////////////////////////////////////////////////
			// "Organise" pulldown menu
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == orgNewWP){
				if (Global.mainTab.tbP.getSelectedCache() >= 0) Global.mainTab.lastselected = ((CacheHolder)cacheDB.get(Global.mainTab.tbP.getSelectedCache())).wayPoint;
				Global.mainTab.newWaypoint(new CacheHolder());
			}

			if(mev.selectedItem == orgCopy){
				profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.copyCaches();
				tbp.refreshTable();
			}

			if(mev.selectedItem == orgMove){
				profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.moveCaches();
				tbp.refreshTable();
			}

			if(mev.selectedItem == orgDelete){
				profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.deleteCaches();
				tbp.refreshTable();
			}
			if(mev.selectedItem == orgRebuild){
				Rebuild rb=new Rebuild();
				rb.rebuild();
				tbp.refreshTable();
			}
			if(mev.selectedItem == orgTravelbugs){
				TravelbugJourneyScreen tbs=new TravelbugJourneyScreen();
				tbs.setPreferredSize(800,600);
				tbs.execute(); //getFrame(), Gui.CENTER_FRAME);
				tbs.close(0);
			}
			if(mev.selectedItem == cacheTour){
				cacheTour.modifiers^=MenuItem.Checked;
				Global.mainForm.toggleCacheListVisible();			
			}
			
			///////////////////////////////////////////////////////////////////////
			// "About" pulldown menu
			///////////////////////////////////////////////////////////////////////
			if(mev.selectedItem == about){
				InfoScreen is = new InfoScreen(FileBase.getProgramDirectory() + "/" + "info.html", MyLocale.getMsg(117,"About"),true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == legend){
				InfoScreen is = new InfoScreen(FileBase.getProgramDirectory() + "/" + "legende.html", MyLocale.getMsg(155,"Legend"),true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == wolflang){
				InfoScreen is = new InfoScreen(FileBase.getProgramDirectory() + "/" + "wolflang.html", MyLocale.getMsg(118,"WolfLanguage"), true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == sysinfo){
				//Vm.debug("Checking system...");
				StringBuffer sb=new StringBuffer(400);
				Font f = mApp.guiFont;
				sb.append("Profile: "); 				sb.append(profile.dataDir);
				sb.append("<br>Platform: "); 			sb.append(Vm.getPlatform());
				sb.append("<br>Locale lang is: ");		sb.append(MyLocale.getLocaleLanguage()); 
				sb.append("<br>Locale country is: "); 	sb.append(MyLocale.getLocaleCountry()); 
				sb.append("<br>Decimal separator is: \""); sb.append(pref.digSeparator);
				sb.append("\"<br>Device is PDA: ");		sb.append(Vm.isMobile());
				sb.append("<br>Screen: ");				sb.append(MyLocale.getScreenWidth()); 
				sb.append(" x ");	 sb.append(MyLocale.getScreenHeight()); 
				sb.append("<br>Font size: ");			sb.append(f.getSize());
				sb.append("<br>Entries in DB: ");		sb.append(cacheDB.size());
				sb.append("<br>File separator is: \""); sb.append(Vm.getProperty("file.separator","def")); 
				sb.append("\"<br>Programme directory is "); sb.append(FileBase.getProgramDirectory());
				sb.append("<br>Number of details in RAM is "); sb.append(CacheHolder.cachesWithLoadedDetails.size());
				sb.append(" Max.: ");					sb.append(Global.getPref().maxDetails);
				sb.append("<br>CacheWolf version: ");		sb.append(Version.getReleaseDetailed());
				sb.append("<br>");
				InfoScreen is = new InfoScreen(sb.toString(), "System", false,pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == chkVersion){
				Vm.showWait(true);
				(new MessageBox(MyLocale.getMsg(178, "Version Checking"), Version.getUpdateMessage(), FormBase.OKB)).execute();
				Vm.showWait(false);
			}
			
			// In case that the triggered event was due to one of the context menu items, process
			// the event by the context menu handler
			tbp.tc.popupMenuEvent(mev.selectedItem);
		}
	}

	public static void updateSelectedCaches(TablePanel tablePanel) {
		Preferences pref=Global.getPref();
		Profile profile=Global.getProfile();
		Vector cacheDB=profile.cacheDB;
		CacheHolder ch;
		
		SpiderGC spider = new SpiderGC(pref, profile, false);
		OCXMLImporter ocSync = new OCXMLImporter(pref, profile);
		//Vm.debug("ByPass? " + profile.byPassIndexActive);
		Vm.showWait(true);
		boolean alreadySaid = false;
		boolean alreadySaid2 = false;
		InfoBox infB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
		infB.exec();
		
		Vector cachesToUpdate = new Vector();
		for(int i = 0; i <	cacheDB.size(); i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_Checked == true && !ch.is_filtered) {
				if ( ch.wayPoint.length()>1 && (ch.wayPoint.substring(0,2).equalsIgnoreCase("GC") 
						|| ch.wayPoint.substring(0,2).equalsIgnoreCase("OC")))
//					if ( (ch.wayPoint.length() > 1 && ch.wayPoint.substring(0,2).equalsIgnoreCase("GC")))
//					Notiz: Wenn es ein addi Wpt ist, sollte eigentlich der Maincache gespidert werden
//					Alter code prüft aber nur ob ein Maincache von GC existiert und versucht dann den addi direkt zu spidern, was nicht funktioniert
				{
					cachesToUpdate.add(new Integer(i));
				} else {
					if (ch.isAddiWpt() && ch.mainCache!=null && !ch.mainCache.is_Checked && !alreadySaid2) { // Is the father ticked?
						alreadySaid2=true;
						(new MessageBox(MyLocale.getMsg(327,"Information"),
								        MyLocale.getMsg(5001,"Can't spider additional waypoint directly. Please check main cache."), FormBase.OKB)).execute();
					}
					if (!ch.isAddiWpt() && !alreadySaid) {
						alreadySaid = true;
						(new MessageBox(MyLocale.getMsg(327,"Information"),
								        ch.wayPoint+ 
								        MyLocale.getMsg(5002,": At the moment this function is only applicable for geocaching.com and opencaching.de."), FormBase.OKB)).execute();
					}
				}

			}
		}

		int spiderErrors = 0;
		for(int j = 0; j <	cachesToUpdate.size(); j++){
			int i = ((Integer)cachesToUpdate.get(j)).intValue();
			ch = (CacheHolder)cacheDB.get(i);
//			infB.setInfo("Loading: " + ch.wayPoint);
			infB.setInfo(MyLocale.getMsg(5513,"Loading: ") + ch.wayPoint +" (" + (j+1) + " / " + cachesToUpdate.size() + ")");
			infB.redisplay();
			if (ch.wayPoint.substring(0,2).equalsIgnoreCase("GC")) {
				int test = spider.spiderSingle(i, infB);
				if (test == -1) {
					infB.close(0);
					break;
				} else if (test == 0) {
					spiderErrors++;
				} else {
					profile.hasUnsavedChanges=true;	
				}
			}
			else {
				if (!ocSync.syncSingle(i, infB)) {
					infB.close(0);
					break;
				} else 
					profile.hasUnsavedChanges=true;	
			}

//			cacheDB.clear();
//			profile.readIndex();
		}
		infB.close(0);
//		profile.hasUnsavedChanges=true;	
		profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
		profile.restoreFilter();
		profile.updateBearingDistance();
		tablePanel.refreshTable();
		Vm.showWait(false);
		if ( spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500,"Error"),spiderErrors + MyLocale.getMsg(5516," cache descriptions%0acould not be loaded."),FormBase.DEFOKB).execute();
		}		
	}
	
}
