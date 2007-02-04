package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	Mainform is responsible for building the user interface.
*	Class ID = 5000
*/
public class MainForm extends Form {
	
	StatusBar statBar=null;
	Preferences pref = Global.getPref(); // Singleton pattern
	Profile profile = Global.getProfile();
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
	protected void checkButtons() {
		if (pref.hasCloseButton) super.checkButtons();
	} 

	public void doIt(){
		this.title = "CacheWolf " + Version.getRelease();
		this.exitSystemOnClose = true;
		this.resizable = true;
		this.moveable = true;
		if(Vm.isMobile() == true) {
			this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
			this.resizable = false;
			this.moveable = false;
			this.windowFlagsToClear=WindowConstants.FLAG_HAS_TITLE;
		} else 
			this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		// Load CacheList
		InfoBox infB = new InfoBox("CacheWolf",MyLocale.getMsg(5000,"Loading Cache-List"));
		infB.exec();
		infB.waitUntilPainted(1000);
		try{
			pref.readPrefFile();
			addGuiFont();
			if (!pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_ONOROFF, true)) 
				ewe.sys.Vm.exit(0); // User MUST select or create a profile
			Vm.showWait(true);
			profile.readIndex();
			pref.curCentrePt.set(profile.centre);
			profile.updateBearingDistance();
			Filter flt=new Filter();
			flt.setFilter();
			flt.doFilter();
		} catch (Exception e){
			if(pref.debug == true) Vm.debug("MainForm:: Exception:: " + e.toString());
		}
		
		if(pref.fixSIP == true){
			if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
				//Vm.setSIP(Vm.SIP_LEAVE_BUTTON|Vm.SIP_ON);
				Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,1);
				Device.preventIdleState(true);
			}
		} else Vm.setSIP(0);
		//Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,1);
		if (pref.showStatus) statBar = new StatusBar(pref, profile.cacheDB);
		mMenu = new MainMenu(this);
		mTab = new MainTab(mMenu,statBar);
		if (pref.menuAtTop) {
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(mTab,CellConstants.STRETCH, CellConstants.FILL);
		} else {
			this.addLast(mTab,CellConstants.STRETCH, CellConstants.FILL);
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		mMenu.setTablePanel(mTab.getTablePanel());
		infB.close(0);
		Vm.showWait(false);
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
			mTab.getTablePanel().myMod.setColumnNamesAndWidths();	
		    mTab.getTablePanel().refreshTable();
			pref.dirty = false;
		}
		super.onEvent(ev);
	}

}
