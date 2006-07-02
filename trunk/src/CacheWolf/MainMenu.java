package CacheWolf;

import ewe.ui.*;
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
*/
public class MainMenu extends MenuBar {
	MenuItem profiles, preferences,loadcaches,loadOC,savenexit,savenoxit,exit,search,searchClr,export;
	MenuItem kalibmap,importmap;
	MenuItem spider, chkVersion;
	MenuItem about, wolflang, sysinfo, testgps, legend;
	MenuItem exportpcx5, exporthtml, exporttop50, exportGPX, exportASC, exportTomTomASC, exportMSARCSV;
	MenuItem exportOZI, exportKML, exportTomTomOVL, exportTPL;
	MenuItem filtCreate, filtApply, filtClear, filtInvert;
	MenuItem exportGPS, exportCacheMate;
	MenuItem orgCopy, orgMove, orgDelete;
	Form father;
	Preferences myPreferences = new Preferences();
	Vector cacheDB = new Vector();
	TablePanel tbp;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public MainMenu(Form f, Preferences pref, Vector DB){
		cacheDB = DB;
		myPreferences = pref;
		father = f;
		String dummy = new String();
		///////////////////////////////////////////////////////////////////////
		// Sub - Menu for export
		///////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = new MenuItem[13];
		exporthtml = new MenuItem((String)lr.get(100,"to HTML"));
		exitems[0] = exporthtml;
		exportpcx5 = new MenuItem((String)lr.get(101,"to PCX5 Mapsource"));
		exitems[1] = exportpcx5;
		exporttop50 = new MenuItem((String)lr.get(102,"to TOP50 ASCII"));
		exitems[2] = exporttop50;
		exportGPX = new MenuItem((String)lr.get(103,"to GPX"));
		exitems[3] = exportGPX;
		exportASC = new MenuItem((String)lr.get(104,"to ASC"));
		exitems[4] = exportASC;
		exportTomTomASC = new MenuItem((String)lr.get(105,"to TomTom ASC"));
		exitems[5] = exportTomTomASC;
		exportMSARCSV = new MenuItem((String)lr.get(106,"to MS AutoRoute CSV"));
		exitems[6] = exportMSARCSV;
		exportGPS = new MenuItem((String)lr.get(122,"to GPS"));
		exitems[7] = exportGPS;
		exportCacheMate = new MenuItem((String)lr.get(123,"to Cachemate"));
		exitems[8] = exportCacheMate;
		
		exportOZI = new MenuItem((String)lr.get(124,"to OZI"));
		exitems[9] = exportOZI;
		exportKML = new MenuItem((String)lr.get(125,"to Google Earth"));
		exitems[10] = exportKML;
		exportTomTomOVL = new MenuItem((String)lr.get(126,"to TomTom OV2"));
		exitems[11] = exportTomTomOVL;
		exportTPL = new MenuItem("via Template");
		exitems[12] = exportTPL;
		String cwd = new String();
		cwd = File.getProgramDirectory();
		File ftest = new File(cwd + "/cmconvert/cmconvert.exe");
		if(!ftest.exists()){
			 exitems[8].modifiers = MenuItem.Disabled;
		}
		
		Menu mn2 = new Menu(exitems, (String)lr.get(107,"Export"));
		///////////////////////////////////////////////////////////////////////
		// Sub - Menu for maps
		///////////////////////////////////////////////////////////////////////
		MenuItem[] mapitems = new MenuItem[2];
		importmap = new MenuItem((String)lr.get(150,"Import"));
		mapitems[0] = importmap;
		kalibmap = new MenuItem((String)lr.get(151,"Calibrate"));
		mapitems[1] = kalibmap;
		Menu mn3 = new Menu(mapitems, (String)lr.get(149,"Maps"));
		///////////////////////////////////////////////////////////////////////
		ftest = new File(cwd + "/gpsbabel.exe");
		if(!ftest.exists()){
			exitems[7].modifiers = MenuItem.Disabled;
		}
		Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
		String dum = new String();
		dum = (String)lr.get(120,"Application");
		if (Vm.isMobile() && s.height < 400) dum = dum.substring(0,3)+".";
		Menu mn = this.addMenu(dum).getMenu();
		dummy = (String)lr.get(121,"Profiles");
		dummy += "|";
		dummy += (String)lr.get(108,"Preferences");
		dummy = dummy + "|";
		dummy = dummy + "Import GPX";
		dummy = dummy + "|";
		dummy = dummy + "Sync OC";
		ftest = new File(cwd + "/geotoad.exe");
		//Hide spider menu if geotoad.exe is not found
		if(ftest.exists()){
			dummy = dummy + "|";
			dummy = dummy + "Spider";
		}
		MenuItem [] items = mn.addItems(mString.split(dummy));
		profiles = items[0]; preferences = items[1]; loadcaches = items[2];loadOC = items[3];
		//Hide spider menu if geotoad.exe is not found
		if(ftest.exists()){
			spider=items[4];
		}
		mn.addItem(mn3);
		mn.addItem("-");
		mn.addItem(mn2);
		dummy = "-|";
		dummy += (String)lr.get(127,"Save");
		dummy += "|";
		dummy += (String)lr.get(110,"Save & Exit");
		dummy += "|";
		dummy += (String)lr.get(111,"Exit");
		items = mn.addItems(mString.split(dummy));
		savenoxit = items[1]; savenexit = items[2]; exit = items[3];
		
		dum = (String)lr.get(112,"Search");
		if (Vm.isMobile() && s.height < 400) dum = dum.substring(0,3)+".";
		mn = this.addMenu(dum).getMenu();
		dummy = (String)lr.get(112,"Search");
		dummy = dummy + "|";
		dummy += (String)lr.get(113,"Clear search");
		items = mn.addItems(mString.split(dummy));
		search = items[0]; searchClr = items[1];
		
		dum = "Filter";
		if (Vm.isMobile() && s.height < 400) dum = dum.substring(0,3)+".";
		mn = this.addMenu(dum).getMenu();
		dummy = (String)lr.get(114,"Create");
		dummy = dummy + "|";
		dummy += (String)lr.get(115,"Invert");
		dummy = dummy + "|";
		dummy += (String)lr.get(116,"Clear");
		items = mn.addItems(mString.split(dummy));
		filtCreate = items[0]; filtInvert=items[1]; filtClear=items[2];
		
		dum = (String)lr.get(140,"Organise");
		if (Vm.isMobile() && s.height < 400) dum = dum.substring(0,3)+".";
		mn =this.addMenu(dum).getMenu();
		dummy = (String)lr.get(141,"Copy");
		dummy = dummy + "|";
		dummy += (String)lr.get(142,"Move");
		dummy = dummy + "|";
		dummy += (String)lr.get(143,"Delete");
		items = mn.addItems(mString.split(dummy));
		orgCopy = items[0]; orgMove=items[1]; orgDelete=items[2];
		
		mn = this.addMenu("?").getMenu();
		dummy = (String)lr.get(117,"About");
		dummy = dummy + "|";
		dummy += (String)lr.get(155,"Legend");
		dummy = dummy + "|";
		dummy += (String)lr.get(118,"WolfLanguage");
		dummy = dummy + "|";
		dummy += "System";
		dummy = dummy + "|";
		dummy += "Version Check";
		
		items = mn.addItems(mString.split(dummy));
		about = items[0]; legend = items[1]; wolflang = items[2]; sysinfo = items[3]; chkVersion=items[4];
		
	}
	
	public void setTablePanel(TablePanel t){
		tbp = t;
	}
	
	public void onEvent(Event ev){
		if (ev instanceof MenuEvent){ //&& ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent)ev;
			if(mev.selectedItem == wolflang){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "wolflang.html", (String)lr.get(118,"WolfLanguage"), true, myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == about){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "info.html", (String)lr.get(117,"About"),true, myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if(mev.selectedItem == legend){
				InfoScreen is = new InfoScreen(File.getProgramDirectory() + "/" + "legende.html", (String)lr.get(155,"Legend"),true, myPreferences);
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
				fc.setTitle((String)lr.get(909,"Select file(s)"));
				if(fc.execute() != fc.IDCANCEL){
					String file = fc.getChosenFile().toString();
					if (file.endsWith("loc")){
						LOCXMLImporter loc = new LOCXMLImporter(cacheDB, file, myPreferences);
						loc.doIt();
					}
					else {
						GPXImporter gpx = new GPXImporter(cacheDB, file,myPreferences);
						gpx.doIt(GPXImporter.DOIT_ASK);
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
				pbf.display("Transfer", "Sending to GPS", null);
				String cwd = new String();
				cwd = File.getProgramDirectory() + "/temp.pcx";
				try{
					ewe.sys.Process p = Vm.exec("gpsbabel -s -i pcx -f "+ cwd +" -o garmin -F com1: ");
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
				pbf.display("CMCONVERT", "Converting...", null);
				String cwd = new String();
				cwd = File.getProgramDirectory() + "/temp.gpx";
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
				String srch = new InputBox((String)lr.get(119,"Search for:")).input("",10);
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.search(srch);
				tbp.refreshTable();
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
				fc.setTitle("Select Template file");
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
					Map map = new Map(myPreferences, sM.getSelectedMap(),sM.worldfileexists);
					map.execute(null, Gui.CENTER_FRAME);
				}
			}
			if(mev.selectedItem == importmap){

				Map map = new Map(myPreferences);
				boolean ok = map.importMap();
				if(ok == true){
					InfoBox inf = new InfoBox((String)lr.get(152,"File import"), (String)lr.get(153,"Map imported successfully"));
					inf.execute();
				} else {
					InfoBox inf = new InfoBox((String)lr.get(152,"File import"), (String)lr.get(154,"Error importing map"));
					inf.execute();
				}
				
			}
			if(mev.selectedItem == chkVersion){
				Version vers = new Version();
				if(vers.newVersionAvailable(myPreferences)){
					InfoBox inf = new InfoBox("New Version", "New version\navailable.");
					inf.execute();
				} else {
					InfoBox inf = new InfoBox("Version Check", "You are at\nthe current version.");
					inf.execute();
				}
			}
			if(mev.selectedItem == spider){
				//GeoToadUI gtUI = new GeoToadUI(myPreferences, File.getProgramDirectory(),cacheDB);
				SpiderGC spGC = new SpiderGC(myPreferences);
				spGC.doIt();
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
				sysstring += "Plattform: " + Vm.getPlatform() + "<br>";
				sysstring += "Locale lang is: " + l.getString(Locale.LANGUAGE_SHORT, l.LANGUAGE_SHORT, 0) + "<br>";
				sysstring += "Locale country is: " + l.getString(Locale.COUNTRY_SHORT, l.COUNTRY_SHORT, 0) + "<br>";
				sysstring += "Decimal seperator is: \"" + myPreferences.digSeparator + "\"<br>";
				sysstring += "Device is PDA: " + Vm.isMobile()+ "<br>";
				sysstring += "Screen: " + s.width + " x " + s.height + "<br>";
				sysstring += "Font size: " + f.getSize() + "<br>";
				sysstring += "Entries in DB: " +cacheDB.size() + "<br>";
				sysstring += "File seperator is: \"" + Vm.getProperty("file.separator","def")+ "\"<br>";
				sysstring += "Programm directory is " + File.getProgramDirectory()+"<br>";
				InfoScreen is = new InfoScreen(sysstring, "System", false,myPreferences);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
		}
	}
}
