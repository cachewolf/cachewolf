package CacheWolf;

import ewe.ui.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	Mainform is responsible for building the user interface.
*	Class ID = not required
*/
public class MainForm extends Form {
	
	StatusBar statBar;
	Vector cacheDB = new Vector();
	Preferences pref = new Preferences();
	MainTab mTab;
	MainMenu mMenu;

	/**
	*	Constructor for MainForm<p>
	*	Loads preferences and the cache index list. Then constructs a
	*	MainMenu and the tabbed Panel (MainTab). MainTab holds the different
	*	tab panels. MainMenu contains the menu entries.
	*	@see	MainMenu
	*	@see	MainTab
	*/
	
	//Build3:
	// "," und "." in den exportern (ok)
	// Schriftgröße bei VGA PPCs (ok: mal schauen)
	public MainForm(){
		doIt();
	}
	
	public MainForm(boolean dbg){
		pref.debug = dbg;
		doIt();
	}
	
	public void doIt(){
		this.title = "CacheWolf " + Version.getRelease();
		this.exitSystemOnClose = true;
		this.resizable = true;
		this.moveable = true;
		if(Vm.isMobile() == true) this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
		else this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		// Load CacheList
		try{
			Vm.showWait(true);
			LoadPreferences(false);
			
			Font defaultGuiFont = mApp.findFont("gui");
			int sz = (pref.fontSize);
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
			long start, end;
			start = Vm.getTimeStampLong();
			LoadAXML();
			end = Vm.getTimeStampLong();
			Vm.debug("index.xml read: " + Convert.toString(end - start)+ " msec");
			//updateBearingDistance();
			TablePanel.updateBearingDistance(cacheDB,pref);
		} catch (Exception e){
			if(pref.debug == true) Vm.debug("MainForm:: Exception:: " + e.toString());
		}
		
		
		if(pref.fixSIP == true){
			if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
				Vm.setSIP(Vm.SIP_LEAVE_BUTTON|Vm.SIP_ON);
			}
		} else Vm.setSIP(0);
		Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,1);
		
		//Dimension dim = new Dimension();
		//dim = this.getSize(new Dimension());
		
		//CellPanel [] p = addToolbar();
		//p[0].defaultTags.set(INSETS,new Insets(0,1,0,1));
		//p[1].addLast(mMenu = new MainMenu(this, myPreferences, cacheDB)).setCell(DONTSTRETCH);
		//p[1].addLast(mTab = new MainTab(cacheDB, myPreferences));
		statBar = new StatusBar(cacheDB);
		this.addLast(mMenu = new MainMenu(this, pref, cacheDB),CellConstants.DONTSTRETCH, CellConstants.FILL);
		this.addLast(mTab = new MainTab(cacheDB, pref,statBar),CellConstants.STRETCH, CellConstants.FILL);
		mMenu.setTablePanel(mTab.getTablePanel());
		Vm.showWait(false);
	}

	
	public MainForm(String what, String dist){
		try{
			LoadPreferences(false);
			
			Font defaultGuiFont = mApp.findFont("gui");
			//int sz = (int)(defaultGuiFont.getSize()+4);
			int sz = (pref.fontSize);
			//Vm.debug("Font size:" + myPreferences.fontSize);
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
			
			LoadAXML();
			Spider mySpidy = new Spider(cacheDB, pref, null, Spider.SPIDERNEAREST);
			mySpidy.SpiderNearest(dist);
			ewe.sys.Vm.exit(0);
		} catch (Exception e){
			//Vm.debug(e.toString());
		}
	}
	
	/**
	*	Routine that sets up the XML reader for the cache index
	*	list.
	*	@see	MyXMLBuilder
	*/
	public void LoadAXML() throws Exception{
		CacheReaderWriter.readIndex(cacheDB, pref.mydatadir);
	}
	
	public void doPaint(Graphics g, Rect r){
		pref.myAppHeight = this.height;
		pref.myAppWidth = this.width;
		super.doPaint(g,r);
	}
	
	/**
	*	Routine to load the prefernces file. Sets up the global
	*	"my" variables.
	*/
	private void LoadPreferences(boolean from_event) throws Exception {
		if(from_event == false) pref.doIt(1);
		else pref.doIt(0);
	}
	
	
	public void onEvent(Event ev){
		if(pref.dirty == true){
			cacheDB.clear();
			try{
				LoadPreferences(true);
				LoadAXML();
				//updateBearingDistance();
				TablePanel.updateBearingDistance(cacheDB,pref);
				mTab.getTablePanel().refreshTable();
			} catch (Exception e){
				//Vm.debug(e.toString());
			}
			pref.dirty = false;
		}
		super.onEvent(ev);
	}
	

}
