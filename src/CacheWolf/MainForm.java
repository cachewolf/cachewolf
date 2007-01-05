package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	Mainform is responsible for building the user interface.
*	Class ID = 5000
*/
public class MainForm extends Form {
	
	StatusBar statBar;
	Preferences pref = Preferences.getPrefObject(); // Singleton pattern
	Profile profile = new Profile();
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
		if(Vm.isMobile() == true) 
			this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
		else 
			this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		// Load CacheList
		
		try{
			pref.readPrefFile();
			addGuiFont();
			if (!pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_ONOROFF, true)) 
				ewe.sys.Vm.exit(0); // User MUST select or create a profile
			long start = Vm.getTimeStampLong();
			Vm.showWait(true);
			InfoBox infB = new InfoBox("CacheWolf",MyLocale.getMsg(5000,"Loading Cache-List"));
			infB.exec();
			profile.readIndex();
			infB.close(0);
			Vm.showWait(false);
			pref.curCentrePt.set(profile.centre);
			long end = Vm.getTimeStampLong();
			Vm.debug("index.xml read: " + Convert.toString(end - start)+ " msec");
			TablePanel.updateBearingDistance(profile.cacheDB,pref);
		} catch (Exception e){
			if(pref.debug == true) Vm.debug("MainForm:: Exception:: " + e.toString());
		}
		
		if(pref.fixSIP == true){
			if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
				Vm.setSIP(Vm.SIP_LEAVE_BUTTON|Vm.SIP_ON);
			}
		} else Vm.setSIP(0);
		Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,1);
		statBar = new StatusBar(pref, profile.cacheDB);
		this.addLast(mMenu = new MainMenu(this, pref, profile),CellConstants.DONTSTRETCH, CellConstants.FILL);
		this.addLast(mTab = new MainTab(pref,profile,statBar),CellConstants.STRETCH, CellConstants.FILL);
		mMenu.setTablePanel(mTab.getTablePanel());
		
	}

	
	public MainForm(String what, String dist, String profileName){
		try{
			pref.readPrefFile();
			addGuiFont();
			pref.lastProfile=profileName;
			pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_FORCED_OFF,false);
			profile.readIndex();
			Spider mySpidy = new Spider(pref, profile,null, Spider.SPIDERNEAREST);
			mySpidy.SpiderNearest(dist);
			ewe.sys.Vm.exit(0);
		} catch (Exception e){
			//Vm.debug(e.toString());
		}
	}
	
	private void addGuiFont(){
		Font defaultGuiFont = mApp.findFont("gui");
		int sz = (pref.fontSize);
		Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
		mApp.addFont(newGuiFont, "gui"); 
		mApp.fontsChanged();
		mApp.mainApp.font = newGuiFont;
	}
	
	public void doPaint(Graphics g, Rect r){
		pref.myAppHeight = this.height;
		pref.myAppWidth = this.width;
		super.doPaint(g,r);
	}
	
	public void onEvent(Event ev){ // Preferences have been changed by PreferencesScreen
		if(pref.dirty == true){
		    mTab.getTablePanel().refreshTable();
			pref.dirty = false;
		}
		super.onEvent(ev);
	}

}
