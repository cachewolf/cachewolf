package CacheWolf;

import ewe.ui.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;

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
	private MenuItem profiles, preferences,loadcaches,loadOC,savenexit,savenoxit,exit,search,searchClr,export;
	private MenuItem kalibmap,importmap;
	private MenuItem spider, chkVersion;
	private MenuItem about, wolflang, sysinfo, testgps, legend;
	private MenuItem exportpcx5, exporthtml, exporttop50, exportGPX, exportASC, exportTomTomASC, exportMSARCSV;
	private MenuItem exportOZI, exportKML, exportTomTomOVL, exportTPL;
	private MenuItem filtCreate, filtApply, filtClear, filtInvert, filtSelected;
	private MenuItem exportGPS, exportCacheMate,mnuSeparator;
	private MenuItem orgCopy, orgMove, orgDelete;
	private Form father;
	private Preferences myPreferences;
	private Vector cacheDB;
	private TablePanel tbp;
	//Locale l = Vm.getLocale();
	//LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public MainMenu(Form f, Preferences pref, Vector DB){
		cacheDB = DB;
		myPreferences = pref;
		father = f;
		String cwd = File.getProgramDirectory();

		///////////////////////////////////////////////////////////////////////
		// subMenu for export, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = new MenuItem[13];
		//Vm.debug("Hi in MainMenu "+lr);
		exitems[0] = exporthtml = new MenuItem(MyLocale.getMsg(100,"to HTML"));
		exitems[1] = exportpcx5 = new MenuItem(MyLocale.getMsg(101,"to PCX5 Mapsource"));
		exitems[2] = exporttop50 = new MenuItem(MyLocale.getMsg(102,"to TOP50 ASCII"));
		exitems[3] = exportGPX = new MenuItem(MyLocale.getMsg(103,"to GPX"));
		exitems[4] = exportASC = new MenuItem(MyLocale.getMsg(104,"to ASC"));
		exitems[5] = exportTomTomASC = new MenuItem(MyLocale.getMsg(105,"to TomTom ASC"));
		exitems[6] = exportMSARCSV = new MenuItem(MyLocale.getMsg(106,"to MS AutoRoute CSV"));
		exitems[7] = exportGPS = new MenuItem(MyLocale.getMsg(122,"to GPS"));
		if(!(new File(cwd + "/gpsbabel.exe")).exists()) exitems[7].modifiers = MenuItem.Disabled;
		exitems[8] = exportCacheMate = new MenuItem(MyLocale.getMsg(123,"to Cachemate"));
		if(!(new File(cwd + "/cmconvert/cmconvert.exe")).exists()) exitems[8].modifiers = MenuItem.Disabled;
		exitems[9] = exportOZI = new MenuItem(MyLocale.getMsg(124,"to OZI"));
		exitems[10] = exportKML = new MenuItem(MyLocale.getMsg(125,"to Google Earth"));
		exitems[11] = exportTomTomOVL = new MenuItem(MyLocale.getMsg(126,"to TomTom OV2"));
		exitems[12] = exportTPL = new MenuItem(MyLocale.getMsg(128,"via Template"));
		Menu exportMenu = new Menu(exitems, MyLocale.getMsg(107,"Export"));

		///////////////////////////////////////////////////////////////////////
		// subMenu for maps, part of "Application" menu below
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mapMenuItems = new MenuItem[2];
		mapMenuItems[0] = importmap = new MenuItem(MyLocale.getMsg(150,"Import"));
		mapMenuItems[1] = kalibmap = new MenuItem(MyLocale.getMsg(151,"Calibrate"));
		Menu mapsMenu = new Menu(mapMenuItems, null);

		// Now we start with the horizontal menu bar "Application", "Search", "Filter", "Organize", "About"
		///////////////////////////////////////////////////////////////////////
		// Create the "Application" pulldown menu
		///////////////////////////////////////////////////////////////////////
		MenuItem [] appMenuItems=new MenuItem[12];
		appMenuItems[0] = profiles 	 = new MenuItem(MyLocale.getMsg(121,"Profiles")); 
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
		MenuItem[] filterMenuItems=new MenuItem[5];
		filterMenuItems[0] = filtCreate  = new MenuItem(MyLocale.getMsg(114,"Create")); 
		filterMenuItems[1] = filtInvert  = new MenuItem(MyLocale.getMsg(115,"Invert")); 
		filterMenuItems[2] = filtClear   = new MenuItem(MyLocale.getMsg(116,"Clear"));
		filterMenuItems[3] = new MenuItem("-");
		filterMenuItems[4] = filtSelected = new MenuItem(MyLocale.getMsg(160,"Selected"));
		
		///////////////////////////////////////////////////////////////////////
		// Create a combined "Filter and Search" pulldown menu for devices with small screens
		///////////////////////////////////////////////////////////////////////
		MenuItem[] filterAndSearchMenuItems=new MenuItem[6];
		filterAndSearchMenuItems[0]=filtCreate;
		filterAndSearchMenuItems[1]=filtInvert;
		filterAndSearchMenuItems[2]=filtClear;
		filterAndSearchMenuItems[3]=mnuSeparator;
		filterAndSearchMenuItems[4]=search;
		filterAndSearchMenuItems[5]=searchClr;
		
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
	
	public void onEvent(Event ev){
		if (ev instanceof MenuEvent){ //&& ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent)ev;
			if(mev.selectedItem == wolflang){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "wolflang.html", MyLocale.getMsg(118,"WolfLanguage"), true, myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == about){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "info.html", MyLocale.getMsg(117,"About"),true, myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == legend){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "legende.html", MyLocale.getMsg(155,"Legend"),true, myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == profiles){
				ProfilesScreen pfs = new ProfilesScreen(myPreferences);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == preferences){
				PreferencesScreen pfs = new PreferencesScreen(myPreferences);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
				myPreferences.doIt(0);
			}
			if(mev.selectedItem == loadcaches){
				//LoadScreen lsc = new LoadScreen(cacheDB, myPreferences);
				//lsc.execute(father.getFrame(), Gui.CENTER_FRAME);
				//Vm.debug("Sending repaint!");
				FileChooser fc = new FileChooser(FileChooser.OPEN|FileChooser.MULTI_SELECT, myPreferences.mydatadir);
				fc.addMask("*.gpx,*.zip,*.loc");
				fc.setTitle(MyLocale.getMsg(909,"Select file(s)"));
				if(fc.execute() != FileChooser.IDCANCEL){
					String dir = fc.getChosenDirectory().toString();
					String files[] = fc.getAllChosen();
					int how = GPXImporter.DOIT_ASK;
					if (files.length > 0){
							InfoBox iB = new InfoBox("Spider?", "Spider Images?", InfoBox.CHECKBOX);
							iB.execute();
							boolean doSpider = iB.mCB_state;
							if (doSpider) how = GPXImporter.DOIT_WITHSPOILER;
							else how = GPXImporter.DOIT_NOSPOILER;
					}
					for (int i = 0; i < files.length; i++){ 
						String file = dir + "/" + files[i];
						if (file.endsWith("loc")){
							LOCXMLImporter loc = new LOCXMLImporter(cacheDB, file, myPreferences);
							loc.doIt();
						}
						else {
							GPXImporter gpx = new GPXImporter(cacheDB, file,myPreferences);
							gpx.doIt(how);
						}
					}
				}
				tbp.resetModel(cacheDB);
			}
			if(mev.selectedItem == loadOC){
				OCXMLImporter oc = new OCXMLImporter(cacheDB,myPreferences);
				oc.doIt();
				tbp.resetModel(cacheDB);
			}
			if(mev.selectedItem == filtCreate){
				FilterScreen fsc = new FilterScreen(cacheDB, myPreferences.mydatadir);
				fsc.execute(father.getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
			}
			if(mev.selectedItem == filtInvert){
				Filter flt = new Filter();
				flt.invertFilter(cacheDB);
				tbp.refreshTable();
			}
			if(mev.selectedItem == exportGPS){
				Vm.showWait(true);
				PCX5Exporter pcx = new PCX5Exporter(cacheDB, myPreferences);
				pcx.doIt(PCX5Exporter.MODE_AUTO);
				ProgressBarForm pbf = new ProgressBarForm();
				pbf.display(MyLocale.getMsg(950,"Transfer"),MyLocale.getMsg(951,"Sending to GPS"), null);
				String cwd = new String();
				cwd = File.getProgramDirectory() + "/temp.pcx";
				try{
					ewe.sys.Process p = Vm.exec("gpsbabel -s -i pcx -f "+ cwd +" -o garmin -F " + myPreferences.garminConn +":");
					p.waitFor();
				}catch(IOException ioex){};
				pbf.clear();
				Vm.showWait(false);
			}
			if(mev.selectedItem == exportCacheMate){
				Vm.showWait(true);
				GPXExporter htm = new GPXExporter(cacheDB, myPreferences);
				htm.doIt(0);
				ProgressBarForm pbf = new ProgressBarForm();
				pbf.display("CMCONVERT", MyLocale.getMsg(952,"Converting..."), null);
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
				pbf.clear();
				Vm.showWait(false);
			}
			if(mev.selectedItem == filtClear){
				Filter flt = new Filter();
				flt.clearFilter(cacheDB);
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == filtSelected){
				CacheHolder ch = new CacheHolder();
				for(int i = 0; i <	cacheDB.size(); i++){
					ch = (CacheHolder)cacheDB.get(i);
					ch.is_filtered = true;
					if(ch.is_Checked == true) ch.is_filtered = false;
					cacheDB.set(i, ch);
				}
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == exportpcx5){
				PCX5Exporter pcx = new PCX5Exporter(cacheDB, myPreferences);
				pcx.doIt(PCX5Exporter.MODE_ASK);
			} 
			if(mev.selectedItem == exporttop50){
				OVLExporter ovl = new OVLExporter(cacheDB, myPreferences);
				ovl.doIt();
			}
			if(mev.selectedItem == exporthtml){
				HTMLExporter htm = new HTMLExporter(cacheDB, myPreferences);
				htm.doIt();
			}
			if(mev.selectedItem == exportGPX){
				GPXExporter htm = new GPXExporter(cacheDB, myPreferences);
				htm.doIt(1);
			}
			if(mev.selectedItem == exportASC){
				ASCExporter asc = new ASCExporter(cacheDB, myPreferences);
				asc.doIt();
			}
			if(mev.selectedItem == exportTomTomASC){
				TomTomASCExporter asc = new TomTomASCExporter(cacheDB, myPreferences);
				asc.doIt();
			}
			if(mev.selectedItem == exportMSARCSV){
				MSARCSVExporter msar = new MSARCSVExporter(cacheDB, myPreferences);
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
				OziExporter ozi = new OziExporter(cacheDB, myPreferences);
				ozi.doIt();
			}
			if(mev.selectedItem == exportTomTomOVL){
				TomTomOV2Exporter tomovl = new TomTomOV2Exporter(cacheDB, myPreferences);
				tomovl.doIt();
			}
			if(mev.selectedItem == exportKML){
				KMLExporter kml = new KMLExporter(cacheDB, myPreferences);
				kml.doIt();
			}

			if(mev.selectedItem == exportTPL){
				FileChooser fc = new FileChooser(FileChooser.OPEN, File.getProgramDirectory());
				fc.addMask("*.tpl");
				fc.setTitle(MyLocale.getMsg(910,"Select Template file"));
				if(fc.execute() != FileChooser.IDCANCEL){
					TPLExporter tpl = new TPLExporter(cacheDB, myPreferences,fc.getChosenFile().toString());
					tpl.doIt();
				}
			}

			if(mev.selectedItem == searchClr){
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();		
			}
			
			if(mev.selectedItem == orgCopy){
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveIndex(cacheDB, myPreferences.mydatadir);

				DataMover dm = new DataMover(cacheDB, myPreferences);
				dm.copyCaches();
				tbp.refreshTable();
			}

			if(mev.selectedItem == orgMove){
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveIndex(cacheDB, myPreferences.mydatadir);

				DataMover dm = new DataMover(cacheDB, myPreferences);
				dm.moveCaches();
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == orgDelete){
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveIndex(cacheDB, myPreferences.mydatadir);

				DataMover dm = new DataMover(cacheDB, myPreferences);
				dm.deleteCaches();
				tbp.refreshTable();
			}
			
			if(mev.selectedItem == savenoxit){
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveIndex(cacheDB, myPreferences.mydatadir);
				tbp.saveColWith(myPreferences);
			}
			
			if(mev.selectedItem == savenexit){
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveIndex(cacheDB, myPreferences.mydatadir);
				tbp.saveColWith(myPreferences);
				ewe.sys.Vm.exit(0);
			}
			if(mev.selectedItem == kalibmap){
				SelectMap sM = new SelectMap();
				sM.execute();
				if((sM.getSelectedMap()).length()>0){
					try {
						Map map = new Map(myPreferences, sM.getSelectedMap(),sM.worldfileexists);
						map.execute(null, Gui.CENTER_FRAME);
					} catch (java.lang.OutOfMemoryError e) {
						MessageBox tmpMB=new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(156,"Out of memory error, map to big"), MessageBox.OKB);
						tmpMB.exec();
					}
				}
			}
			if(mev.selectedItem == importmap){

				Map map = new Map(myPreferences);
				boolean ok = map.importMap();
				if(ok == true){
					InfoBox inf = new InfoBox(MyLocale.getMsg(152,"File import"), MyLocale.getMsg(153,"Map imported successfully"));
					inf.execute();
				} else {
					InfoBox inf = new InfoBox(MyLocale.getMsg(152,"File import"), MyLocale.getMsg(154,"Error importing map"));
					inf.execute();
				}
				
			}
			if(mev.selectedItem == chkVersion){
				Version vers = new Version();
				if(vers.newVersionAvailable(myPreferences)){
					InfoBox inf = new InfoBox("New Version", "New version\navailable.");// TODO Internationalisation when code has been written
					inf.execute();
				} else {
					InfoBox inf = new InfoBox("Version Check", "You are at\nthe current version.");// TODO Internationalisation when code has been written
					inf.execute();
				}
			}
			if(mev.selectedItem == spider){
				//GeoToadUI gtUI = new GeoToadUI(myPreferences, File.getProgramDirectory(),cacheDB);
				SpiderGC spGC = new SpiderGC(myPreferences, cacheDB);
				spGC.doIt();
				tbp.resetModel(cacheDB);
				/*
				gtUI.execute();
				File ftest = new File(File.getProgramDirectory() + "/temp.gpx");
				if(ftest.exists()){
					if(gtUI.chkImport.getState()){
						//if(chkSpoilers.getState()) inf = new InfoBox("GPX", "import + spoiler");
						//else inf = new InfoBox("GPX", "import");
						//inf.show();
						GPXImporter imp = new GPXImporter(cacheDB, File.getProgramDirectory() + "/temp.gpx", myPreferences);
						if(gtUI.chkSpoilers.getState()) {
							imp.doIt(GPXImporter.DOIT_WITHSPOILER);
						} else imp.doIt(GPXImporter.DOIT_NOSPOILER);
					} else {
						//Vm.debug("timer checking...");					
					}
					tbp.resetModel(cacheDB);
				}
				*/
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
				sysstring += "Decimal seperator is: \"" + myPreferences.digSeparator + "\"<br>";
				sysstring += "Device is PDA: " + Vm.isMobile()+ "<br>";
				sysstring += "Screen: " + MyLocale.getScreenWidth() + " x " + MyLocale.getScreenHeight() + "<br>";
				sysstring += "Font size: " + f.getSize() + "<br>";
				sysstring += "Entries in DB: " +cacheDB.size() + "<br>";
				sysstring += "File seperator is: \"" + Vm.getProperty("file.separator","def")+ "\"<br>";
				sysstring += "Programme directory is " + File.getProgramDirectory()+"<br>";
				InfoScreen is = new InfoScreen(sysstring, "System", false,myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
		}
	}
}
