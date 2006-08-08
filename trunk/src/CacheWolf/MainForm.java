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
	
	Vector cacheDB = new Vector();
	double myLat, myLon;
	String myNS = new String();
	String myWE = new String();
	Preferences myPreferences = new Preferences();
	MainTab mTab;
	MainMenu mMenu;
	Locale l = Vm.getLocale();
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
		myPreferences.debug = dbg;
		doIt();
	}
	
	public void doIt(){
		this.title = "CacheWolf " + Version.getRelease();
		this.exitSystemOnClose = true;
		this.resizable = true;
		this.moveable = true;
		if(Vm.isMobile() == true) this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
		
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
		
		this.resizeOnSIP = true;
		// Load CacheList
		try{
			Vm.showWait(true);
			LoadPreferences(false);
			
			Font defaultGuiFont = mApp.findFont("gui");
			int sz = (myPreferences.fontSize);
			//Vm.debug("Font size:" + myPreferences.fontSize);
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
			
			LoadAXML();
			//updateBearingDistance();
			TablePanel.updateBearingDistance(cacheDB,myPreferences);
			Vm.showWait(false);
		} catch (Exception e){
			if(myPreferences.debug == true) Vm.debug("MainForm:: Exception:: " + e.toString());
		}
		//Dimension dim = new Dimension();
		//dim = this.getSize(new Dimension());
		
		CellPanel [] p = addToolbar();
		//p[0].defaultTags.set(INSETS,new Insets(0,1,0,1));
		p[0].addNext(mMenu = new MainMenu(this, myPreferences, cacheDB)).setCell(DONTSTRETCH);
		p[1].addLast(mTab = new MainTab(cacheDB, myPreferences));
		//this.addLast(mMenu = new MainMenu(this, myPreferences, cacheDB),this.DONTSTRETCH, this.FILL);
		//this.addLast(mTab = new MainTab(cacheDB, myPreferences),this.STRETCH, this.FILL); 
		mMenu.setTablePanel(mTab.getTablePanel());
	}

	
	public MainForm(String what, String dist){
		try{
			LoadPreferences(false);
			
			Font defaultGuiFont = mApp.findFont("gui");
			//int sz = (int)(defaultGuiFont.getSize()+4);
			int sz = (myPreferences.fontSize);
			//Vm.debug("Font size:" + myPreferences.fontSize);
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
			
			LoadAXML();
			Spider mySpidy = new Spider(cacheDB, myPreferences, null, Spider.SPIDERNEAREST);
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
		MyXMLBuilder myB = new MyXMLBuilder(cacheDB, myPreferences.mydatadir);
		myB.doIt();
		//Vm.debug(Convert.toString(cacheDB.size()));
	}
	
	public void doPaint(Graphics g, Rect r){
		myPreferences.myAppHeight = this.height;
		myPreferences.myAppWidth = this.width;
		super.doPaint(g,r);
	}
	
	/**
	*	Routine to load the prefernces file. Sets up the global
	*	"my" variables.
	*/
	private void LoadPreferences(boolean from_event) throws Exception {
		if(from_event == false) myPreferences.doIt(1);
		else myPreferences.doIt(0);

		String lg1NS = myPreferences.mylgNS;
		String lgDeg = myPreferences.mylgDeg;
		String lgMin = myPreferences.mylgMin;
		String br1WE = myPreferences.mybrWE;
		String brDeg = myPreferences.mybrDeg;
		String brMin = myPreferences.mybrMin;
		//Set my location
		myLon = Common.parseDouble(lgDeg) + Common.parseDouble(lgMin)/60;
		myLat = Common.parseDouble(brDeg) + Common.parseDouble(brMin)/60;
		myNS = lg1NS;
		myWE = br1WE;
	}
	
	
	public void onEvent(Event ev){
		if(myPreferences.dirty == true){
			cacheDB.clear();
			try{
				LoadPreferences(true);
				LoadAXML();
				//updateBearingDistance();
				TablePanel.updateBearingDistance(cacheDB,myPreferences);
				mTab.getTablePanel().refreshTable();
			} catch (Exception e){
				//Vm.debug(e.toString());
			}
			myPreferences.dirty = false;
		}
		super.onEvent(ev);
	}
	

}
