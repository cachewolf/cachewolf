package CacheWolf;

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
	private MenuItem profiles, preferences,loadcaches,loadOC,savenexit,savenoxit,exit,search,searchClr;
	private MenuItem downloadmap, kalibmap, importmap;
	private MenuItem spider, chkVersion;
	private MenuItem about, wolflang, sysinfo, legend;
	private MenuItem exportpcx5, exporthtml, exporttop50, exportGPX, exportASC, exportTomTom, exportMSARCSV;
	private MenuItem exportOZI, exportKML, exportTPL;
	private MenuItem filtCreate, filtClear, filtInvert, filtSelected, filtBlack, filtApply;
	private MenuItem exportGPS, exportCacheMate,mnuSeparator;
	private MenuItem orgCopy, orgMove, orgDelete;
	private MenuItem mnuNewProfile, mnuOpenProfile, mnuEditProfile;
	private Form father;
	private TablePanel tbp;
	private FilterScreen scnFilter=new FilterScreen();
	
	public MainMenu(Form f){
		father = f;
		String cwd = File.getProgramDirectory();

		///////////////////////////////////////////////////////////////////////
		// subMenu for profiles, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mnuProfile = new MenuItem[3];
		mnuProfile[0] = mnuNewProfile = new MenuItem(MyLocale.getMsg(1107,"New"));
		mnuProfile[1] = mnuOpenProfile = new MenuItem(MyLocale.getMsg(1109,"Open"));
		mnuProfile[2] = mnuEditProfile = new MenuItem(MyLocale.getMsg(1110,"Edit"));
		Menu profileMenu = new Menu(mnuProfile,MyLocale.getMsg(121,"Profiles"));
		
		///////////////////////////////////////////////////////////////////////
		// subMenu for export, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = new MenuItem[12];
		//Vm.debug("Hi in MainMenu "+lr);
		exitems[0] = exporthtml = new MenuItem(MyLocale.getMsg(100,"to HTML"));
		exitems[1] = exportpcx5 = new MenuItem(MyLocale.getMsg(101,"to PCX5 Mapsource"));
		exitems[2] = exporttop50 = new MenuItem(MyLocale.getMsg(102,"to TOP50 ASCII"));
		exitems[3] = exportGPX = new MenuItem(MyLocale.getMsg(103,"to GPX"));
		exitems[4] = exportASC = new MenuItem(MyLocale.getMsg(104,"to ASC"));
		exitems[5] = exportTomTom = new MenuItem(MyLocale.getMsg(105,"to TomTom"));
		exitems[6] = exportMSARCSV = new MenuItem(MyLocale.getMsg(106,"to MS AutoRoute CSV"));
		exitems[7] = exportGPS = new MenuItem(MyLocale.getMsg(122,"to GPS"));
		if(!(new File(cwd + "/gpsbabel.exe")).exists()) exitems[7].modifiers = MenuItem.Disabled;
		exitems[8] = exportCacheMate = new MenuItem(MyLocale.getMsg(123,"to Cachemate"));
		if(!(new File(cwd + "/cmconvert/cmconvert.exe")).exists()) exitems[8].modifiers = MenuItem.Disabled;
		exitems[9] = exportOZI = new MenuItem(MyLocale.getMsg(124,"to OZI"));
		exitems[10] = exportKML = new MenuItem(MyLocale.getMsg(125,"to Google Earth"));
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

		// Now we start with the horizontal menu bar "Application", "Search", "Filter", "Organize", "About"
		///////////////////////////////////////////////////////////////////////
		// Create the "Application" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem [] appMenuItems=new MenuItem[12];
		appMenuItems[0] = profiles 	 = new MenuItem(MyLocale.getMsg(121,"Profiles"), 0, profileMenu); 
		appMenuItems[1] = preferences = new MenuItem(MyLocale.getMsg(108,"Preferences")); 
		appMenuItems[2] = loadcaches  = new MenuItem(MyLocale.getMsg(129,"Import GPX")); //TODO internationalization
		appMenuItems[3] = loadOC      = new MenuItem(MyLocale.getMsg(130,"Download von opencaching.de")); 
		appMenuItems[4] = spider      = new MenuItem(MyLocale.getMsg(131,"Spider von geocaching.com")); 
		appMenuItems[5] = new MenuItem(MyLocale.getMsg(149,"Maps"),0,mapsMenu);
		appMenuItems[6] = mnuSeparator = new MenuItem("-");
		appMenuItems[7] = new MenuItem(MyLocale.getMsg(107,"Export"),0,exportMenu);
		appMenuItems[8] = mnuSeparator;
		appMenuItems[9] = savenoxit = new MenuItem(MyLocale.getMsg(127,"Save")); 
		appMenuItems[10] = savenexit = new MenuItem(MyLocale.getMsg(110,"Save & Exit")); 
		appMenuItems[11] = exit = new MenuItem(MyLocale.getMsg(111,"Exit"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(120,"Application"),new Menu(appMenuItems,null)));
		
		///////////////////////////////////////////////////////////////////////
		// Create the "Search" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] searchMenuItems=new MenuItem[2];
		searchMenuItems[0] = search = new MenuItem(MyLocale.getMsg(112,"Search")); 
		searchMenuItems[1] = searchClr = new MenuItem(MyLocale.getMsg(113,"Clear search"));
		
		///////////////////////////////////////////////////////////////////////
		// Create the "Filter" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] filterMenuItems=new MenuItem[8];
		filterMenuItems[0] = filtApply  = new MenuItem(MyLocale.getMsg(709,"Apply")); 
		filterMenuItems[1] = filtCreate  = new MenuItem(MyLocale.getMsg(114,"Create")); 
		filterMenuItems[2] = filtInvert  = new MenuItem(MyLocale.getMsg(115,"Invert")); 
		filterMenuItems[3] = filtClear   = new MenuItem(MyLocale.getMsg(116,"Clear"));
		filterMenuItems[4] = mnuSeparator;
		filterMenuItems[5] = filtBlack   = new MenuItem(MyLocale.getMsg(161,"Show Blacklist"));
		filterMenuItems[6] = mnuSeparator;
		filterMenuItems[7] = filtSelected = new MenuItem(MyLocale.getMsg(160,"Selected"));
		
		///////////////////////////////////////////////////////////////////////
		// Create a combined "Filter and Search" pulldown menu for devices with small screens
		///////////////////////////////////////////////////////////////////////
		MenuItem[] filterAndSearchMenuItems=new MenuItem[10];
		filterAndSearchMenuItems[0]=filtApply;
		filterAndSearchMenuItems[1]=filtCreate;
		filterAndSearchMenuItems[2]=filtInvert;
		filterAndSearchMenuItems[3]=filtClear;
		filterAndSearchMenuItems[4]=mnuSeparator;
		filterAndSearchMenuItems[5]=filtBlack;
		filterAndSearchMenuItems[6]=filtSelected;
		filterAndSearchMenuItems[7]=mnuSeparator;
		filterAndSearchMenuItems[8]=search;
		filterAndSearchMenuItems[9]=searchClr;
		
		// Depending on screen width display either filter and searach menus or the combined menu 
		if (MyLocale.getScreenWidth()>300) {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(112,"Search"),new Menu(searchMenuItems,null)));
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159,"Filter"),new Menu(filterMenuItems,null)));
		} else {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159,"Filter"),new Menu(filterAndSearchMenuItems,null)));
		}
		
		///////////////////////////////////////////////////////////////////////
		// Create the "Organize" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem[] organizeMenuItems=new MenuItem[3];
		organizeMenuItems[0] = orgCopy  = new MenuItem(MyLocale.getMsg(141,"Copy")); 
		organizeMenuItems[1] = orgMove  = new MenuItem(MyLocale.getMsg(142,"Move")); 
		organizeMenuItems[2] = orgDelete   = new MenuItem(MyLocale.getMsg(143,"Delete"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(140,"Organize"),new Menu(organizeMenuItems,null)));

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
	
	public void onEvent(Event ev){
		Preferences pref=Global.getPref();
		Profile profile=Global.getProfile();
		Vector cacheDB=profile.cacheDB;
		if (ev instanceof MenuEvent){ //&& ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent)ev;
			if(mev.selectedItem == wolflang){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "wolflang.html", MyLocale.getMsg(118,"WolfLanguage"), true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == about){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "info.html", MyLocale.getMsg(117,"About"),true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == legend){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "legende.html", MyLocale.getMsg(155,"Legend"),true, pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == mnuNewProfile){
				NewProfileForm f=new NewProfileForm(pref.baseDir);
			    int code=f.execute(getFrame(), Gui.CENTER_FRAME);
				if (code==0) { 
					profile.clearProfile(); 
					pref.lastProfile=profile.name=f.profileDir;
					pref.savePreferences(); // Remember that this was the last profile used
					profile.dataDir=pref.baseDir+f.profileDir+"/";
					pref.showBlacklisted=false;
					filtBlack.modifiers&=~MenuItem.Checked;
					tbp.refreshTable();
				}
				f.close(0);
			}
			if(mev.selectedItem == mnuOpenProfile){
				if (pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_FORCED_ON,false)) {
					profile.cacheDB.clear();
					tbp.setSelectedCache(-1);
					profile.readIndex();
					pref.curCentrePt.set(profile.centre);
					filtBlack.modifiers&=~MenuItem.Checked;
					pref.showBlacklisted=false;
					tbp.resetModel();
					Global.mainTab.tbP.gotoFirstLine();
				}
			}
			if(mev.selectedItem == mnuEditProfile){
				ProfileDataForm f=new ProfileDataForm(pref,profile);
			    int code=f.execute(getFrame(), Gui.CENTER_FRAME);
			    tbp.refreshTable();
				f.close(0);
			}
			if(mev.selectedItem == preferences){
				tbp.saveColWith(pref);
				PreferencesScreen pfs = new PreferencesScreen(pref);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
				pref.readPrefFile();
			}
			if(mev.selectedItem == loadcaches){
				//LoadScreen lsc = new LoadScreen(cacheDB, myPreferences);
				//lsc.execute(father.getFrame(), Gui.CENTER_FRAME);
				//Vm.debug("Sending repaint!");
				FileChooser fc = new FileChooser(FileChooser.OPEN|FileChooser.MULTI_SELECT, pref.baseDir);
				fc.addMask("*.gpx,*.zip,*.loc");
				fc.setTitle(MyLocale.getMsg(909,"Select file(s)"));
				if(fc.execute() != FileChooser.IDCANCEL){
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
				pref.showBlacklisted=false;
				filtBlack.modifiers&=~MenuItem.Checked;
				tbp.resetModel();
			}
			if(mev.selectedItem == loadOC){
				OCXMLImporter oc = new OCXMLImporter(pref,profile);
				oc.doIt();
				pref.showBlacklisted=false;
				filtBlack.modifiers&=~MenuItem.Checked;
				tbp.resetModel();
			}
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
			if(mev.selectedItem == filtBlack){
				filtBlack.modifiers^=MenuItem.Checked;
				pref.showBlacklisted=!pref.showBlacklisted;
				Filter flt = new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			if(mev.selectedItem == exportGPS){
				Vm.showWait(true);
				LocExporter loc = new LocExporter();
				String tmpFileName = File.getProgramDirectory() + "/temp.loc";
				loc.setTmpFileName(tmpFileName);
				loc.doIt(LocExporter.MODE_AUTO);
				ProgressBarForm.display(MyLocale.getMsg(950,"Transfer"),MyLocale.getMsg(951,"Sending to GPS"), null);
				try{
					ewe.sys.Process p = Vm.exec("gpsbabel -i geo -f \""+ tmpFileName +"\" -o garmin -F " + pref.garminConn +":");
					Vm.debug("gpsbabel -i geo -f  \""+ tmpFileName +"\" -o garmin -F " + pref.garminConn +":");
					p.waitFor();
				}catch(IOException ioex){};
				ProgressBarForm.clear();
				Vm.showWait(false);
			}
			if(mev.selectedItem == exportCacheMate){
				Vm.showWait(true);
				GPXExporter htm = new GPXExporter( pref, profile);
				htm.doIt(0);
				ProgressBarForm.display("CMCONVERT", MyLocale.getMsg(952,"Converting..."), null);
				String cwd = new String();
				cwd = File.getProgramDirectory() + "/temp.gpx";
				// add surrounding "
				cwd = "\"" + cwd + "\"";
				try{
					//Vm.debug(File.getProgramDirectory() + "/cmconvert/cmconvert " + cwd);
					ewe.sys.Process p = Vm.exec(File.getProgramDirectory() + "/cmconvert/cmconvert " + cwd);
					p.waitFor();
				}catch(IOException ioex){
					//Vm.debug("Scheint ein Problem zu geben");
				};
				ProgressBarForm.clear();
				Vm.showWait(false);
			}
			if(mev.selectedItem == filtClear){
				Filter flt = new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == filtSelected){
				CacheHolder ch;
				for(int i = cacheDB.size()-1; i>=0; i--){
					ch = (CacheHolder)cacheDB.get(i);
					ch.is_filtered = ch.is_Checked;
					//cacheDB.set(i, ch);
				}
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == exportpcx5){
				PCX5Exporter pcx = new PCX5Exporter( pref, profile);
				pcx.doIt(PCX5Exporter.MODE_ASK);
			} 
			if(mev.selectedItem == exporttop50){
				OVLExporter ovl = new OVLExporter(pref, profile);
				ovl.doIt();
			}
			if(mev.selectedItem == exporthtml){
				HTMLExporter htm = new HTMLExporter(pref, profile);
				htm.doIt();
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
			if(mev.selectedItem == search){
				String srch = new InputBox(MyLocale.getMsg(119,"Search for:")).input("",10);
				if (srch != null) {
					SearchCache ssc = new SearchCache(cacheDB);
					ssc.search(srch);
					tbp.refreshTable();
				}
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
				FileChooser fc = new FileChooser(FileChooser.OPEN, File.getProgramDirectory());
				fc.addMask("*.tpl");
				fc.setTitle(MyLocale.getMsg(910,"Select Template file"));
				if(fc.execute() != FileChooser.IDCANCEL){
					TPLExporter tpl = new TPLExporter( pref,profile, fc.getChosenFile().toString());
					tpl.doIt();
				}
			}

			if(mev.selectedItem == searchClr){
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();		
			}
			
			if(mev.selectedItem == orgCopy){
				profile.saveIndex(pref);

				DataMover dm = new DataMover();
				dm.copyCaches();
				tbp.refreshTable();
			}

			if(mev.selectedItem == orgMove){
				profile.saveIndex(pref);

				DataMover dm = new DataMover();
				dm.moveCaches();
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == orgDelete){
				profile.saveIndex(pref);

				DataMover dm = new DataMover();
				dm.deleteCaches();
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == savenoxit){
				profile.saveIndex(pref);
				tbp.saveColWith(pref);
			}
			
			if(mev.selectedItem == savenexit){
				profile.saveIndex(pref);
				tbp.saveColWith(pref);
				ewe.sys.Vm.exit(0);
			}
			if(mev.selectedItem == downloadmap){
				MapLoaderGui mLG = new MapLoaderGui(cacheDB);
				mLG.exec(); // .execute doesn't work because the tcp-socket uses another thread which cannot be startet if here .execute() is used!
			}
			if(mev.selectedItem == kalibmap){
				SelectMap sM = new SelectMap();
				sM.execute();
				if((sM.getSelectedMap()).length()>0){
					try {
						Map map = new Map(pref, sM.getSelectedMap(),sM.worldfileexists);
						map.execute(null, Gui.CENTER_FRAME);
					} catch (java.lang.OutOfMemoryError e) {
						MessageBox tmpMB=new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(156,"Out of memory error, map to big"), MessageBox.OKB);
						tmpMB.exec();
					}
				}
			}
			if(mev.selectedItem == importmap){

				Map map = new Map(pref);
				map.importMap();
			}
			if(mev.selectedItem == chkVersion){
				Version vers = new Version();
				if(vers.newVersionAvailable(pref)){
					InfoBox inf = new InfoBox("New Version", "New version\navailable.");// TODO Internationalisation when code has been written
					inf.execute();
				} else {
					InfoBox inf = new InfoBox("Version Check", "You are at\nthe current version.");// TODO Internationalisation when code has been written
					inf.execute();
				}
			}
			if(mev.selectedItem == spider){
				SpiderGC spGC = new SpiderGC(pref, profile);
				spGC.doIt();
				tbp.resetModel();
			}
			if(mev.selectedItem == exit){
				ewe.sys.Vm.exit(0);
			}
			if(mev.selectedItem == sysinfo){
				//Vm.debug("Checking system...");
				String sysstring = new String();
				Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
				Font f = mApp.guiFont;
				sysstring += "Platform: " + Vm.getPlatform() + "<br>";
				sysstring += "Locale lang is: " + MyLocale.getLocaleLanguage() + "<br>";
				sysstring += "Locale country is: " + MyLocale.getLocaleCountry() + "<br>";
				sysstring += "Decimal seperator is: \"" + pref.digSeparator + "\"<br>";
				sysstring += "Device is PDA: " + Vm.isMobile()+ "<br>";
				sysstring += "Screen: " + MyLocale.getScreenWidth() + " x " + MyLocale.getScreenHeight() + "<br>";
				sysstring += "Font size: " + f.getSize() + "<br>";
				sysstring += "Entries in DB: " +cacheDB.size() + "<br>";
				sysstring += "File seperator is: \"" + Vm.getProperty("file.separator","def")+ "\"<br>";
				sysstring += "Programme directory is " + File.getProgramDirectory()+"<br>";
				InfoScreen is = new InfoScreen(sysstring, "System", false,pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
		}
	}
}
