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
	public boolean cacheListVisible=false;
    public CacheList cacheList;
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
		this(false, null);
	}

	public MainForm(boolean dbg, String pathtoprefxml){
		//Resize the Close und Ok-Buttons of all Forms. This is just a test for the PDA Versions:
		int fontSize = pref.fontSize;
		FormBase.close = new DrawnIcon(DrawnIcon.CROSS,fontSize,fontSize,new Color(0,0,0));
		FormBase.tick = new DrawnIcon(DrawnIcon.TICK,fontSize,fontSize,new Color(0,128,0));
		FormBase.cross = new DrawnIcon(DrawnIcon.CROSS,fontSize,fontSize,new Color(128,0,0));

		pref.debug = dbg;
		pref.setPathToConfigFile(pathtoprefxml); // in case pathtoprefxml == null the preferences will determine the path itself
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
		this.windowFlagsToSet = WindowConstants.FLAG_MAXIMIZE_ON_PDA;
		//Rect screen = ((ewe.fx.Rect) (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new ewe.fx.Rect(),0)));
		//if ( screen.height >= 600 && screen.width >= 800) this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		InfoBox infB = null;
		try{
			pref.readPrefFile();
			pref.logInit();
			if (MyLocale.initErrors.length() != 0) {
				new MessageBox("Error", MyLocale.initErrors, FormBase.OKB).execute();
			}
			if(Vm.isMobile()) {
				//this.windowFlagsToSet |=Window.FLAG_FULL_SCREEN;
				this.resizable = false;
				this.moveable = false;
			} else {
				int h,w;
				h=pref.myAppHeight;
				if (h>MyLocale.getScreenHeight()) h=MyLocale.getScreenHeight();
				w=pref.myAppWidth;
				if (w>MyLocale.getScreenWidth()) w=MyLocale.getScreenWidth();
				this.setPreferredSize(w,h);
			}
			addGuiFont();
			if (!pref.selectProfile(profile,Preferences.PROFILE_SELECTOR_ONOROFF, true))
				ewe.sys.Vm.exit(0); // User MUST select or create a profile
			Vm.showWait(true);


			// Replace buildt-in symbols with customized images
			if (pref.useOwnSymbols) GuiImageBroker.customizedSymbols();

			// Load CacheList
			infB = new InfoBox("CacheWolf",MyLocale.getMsg(5000,"Loading Cache-List"));
			infB.exec();
			infB.waitUntilPainted(100);
			profile.readIndex(infB);
			pref.setCurCentrePt(profile.centre);
			setTitle(profile.name + " - CW "+Version.getRelease());
		} catch (Exception e){
			if(pref.debug) Vm.debug("MainForm:: Exception:: " + e.toString());
		}


		if(pref.fixSIP){
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

		this.addLast(split,STRETCH,FILL);
		/*
		if (pref.menuAtTop) {
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(split,STRETCH,FILL);
		} else {
			this.addLast(split,STRETCH,FILL);
			this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		*/
		mMenu.setTablePanel(mTab.getTablePanel());
		if (infB != null) infB.close(0);
		mTab.tbP.refreshTable();
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
		if(pref.dirty){
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
			Global.mainForm.mMenu.cacheTour.modifiers|=MenuItem.Checked;
		} else {
			// Hide the splitterbar and set width to 0
			split.theSplitter.modify(Invisible,0);
			split.theSplitter.resizeTo(0,split.theSplitter.getRect().height);
			Global.mainForm.mMenu.cacheTour.modifiers&=~MenuItem.Checked;
		}
		split.theSplitter.doOpenClose(cacheListVisible);
		Global.mainForm.mMenu.repaint();

	}

}
