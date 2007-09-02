package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	Mainform is responsible for building the user interface.
*	Class ID = 5000
*/
public class MainForm extends Editor {
	// The next three declares are for the cachelist
	boolean cacheListVisible=false;
    CacheList cacheList;
    SplittablePanel split;
	
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

	protected boolean canExit(int exitCode) {
		mTab.saveUnsavedChanges(true);
		return pref.hasCloseButton;
	}
	
	public void doIt(){
		//CellPanel [] p = addToolbar();
		Global.mainForm=this;
		//this.title = "CacheWolf " + Version.getRelease();
		this.exitSystemOnClose = true;
		this.resizable = true;
		this.moveable = true;
		this.windowFlagsToSet = Window.FLAG_MAXIMIZE_ON_PDA;
		if(Vm.isMobile() == true) {
			//this.windowFlagsToSet |=Window.FLAG_FULL_SCREEN;
			this.resizable = false;
			this.moveable = false;
		}
		Rect screen = ((ewe.fx.Rect) (Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new ewe.fx.Rect(),0)));
		if ( screen.height >= 600 && screen.width >= 800) this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		// Load CacheList
		InfoBox infB = new InfoBox("CacheWolf",MyLocale.getMsg(5000,"Loading Cache-List"));
		infB.exec();
		infB.waitUntilPainted(100);
		try{
			pref.readPrefFile();
			addGuiFont();
			if (!pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_ONOROFF, true)) 
				ewe.sys.Vm.exit(0); // User MUST select or create a profile
			Vm.showWait(true);
			profile.readIndex();
			pref.curCentrePt.set(profile.centre);
			profile.updateBearingDistance();
			profile.restoreFilter();
			setTitle("Cachewolf "+Version.getRelease()+" - "+profile.name);
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
		
        if (pref.showStatus) statBar = new StatusBar(pref, profile.cacheDB);
		mMenu = new MainMenu(this);
		mTab = new MainTab(mMenu,statBar);
		split=new SplittablePanel(PanelSplitter.HORIZONTAL);
		split.theSplitter.thickness=0; split.theSplitter.modify(Invisible,0);
		CellPanel pnlCacheList = split.getNextPanel();
		CellPanel pnlMainTab = split.getNextPanel(); 
		split.setSplitter(PanelSplitter.MIN_SIZE|PanelSplitter.BEFORE,PanelSplitter.HIDDEN|PanelSplitter.BEFORE,PanelSplitter.CLOSED);
		pnlCacheList.addLast(cacheList=new CacheList(),STRETCH,FILL);
		pnlMainTab.addLast(mTab,STRETCH,FILL);
		
		mTab.dontAutoScroll=true;
		
		if (pref.menuAtTop) {
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(split,STRETCH,FILL);
		} else {
			this.addLast(split,STRETCH,FILL);
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		mMenu.setTablePanel(mTab.getTablePanel());
		infB.close(0);
		mTab.tbP.selectFirstRow();
		//mTab.tbP.tc.paintSelection();
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
			mTab.getTablePanel().refreshControl();
		    //mTab.getTablePanel().refreshTable();
			pref.dirty = false;
		}
		super.onEvent(ev);
	}

	public void toggleCacheListVisible() {
		cacheListVisible=!cacheListVisible;
		if (cacheListVisible) {
			// Make the splitterbar visible with a width of 6 
			split.theSplitter.modify(0,Invisible);
			split.theSplitter.resizeTo(6,split.theSplitter.getRect().height);
			Global.mainForm.mMenu.filtCacheTour.modifiers|=MenuItem.Checked;
		} else {
			// Hide the splitterbar and set width to 0
			split.theSplitter.modify(Invisible,0);
			split.theSplitter.resizeTo(0,split.theSplitter.getRect().height);
			Global.mainForm.mMenu.filtCacheTour.modifiers&=~MenuItem.Checked;
		}
		split.theSplitter.doOpenClose(cacheListVisible);
		Global.mainForm.mMenu.repaint();
		
	}
	
}
