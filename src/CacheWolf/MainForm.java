/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf;

import CacheWolf.controls.InfoBox;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.DrawnIcon;
import ewe.fx.Font;
import ewe.fx.Graphics;
import ewe.fx.IconAndText;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.io.BufferedWriter;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Device;
import ewe.sys.Vm;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DragContext;
import ewe.ui.Editor;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.KeyEvent;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.MultiPanelEvent;
import ewe.ui.PanelSplitter;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.SplittablePanel;
import ewe.ui.WindowConstants;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;
import ewe.ui.mList;
import ewe.util.Comparer;

/**
 * Mainform is responsible for building the user interface. Class ID = 5000
 */
public class MainForm extends Editor {
    public static MainForm itself;
    public static Profile profile;

    // The next three declares are for the cachelist
    public boolean cacheListVisible = false;
    public CacheList cacheList;
    SplittablePanel split;

    MainTab mainTab;
    MainMenu mainMenu;

    /**
     * Constructor for MainForm
     * <p>
     * Loads preferences and the cache index list. Then constructs a MainMenu and the tabbed Panel (MainTab). MainTab holds the different tab panels. MainMenu contains the menu entries.
     * 
     * @see MainMenu
     * @see MainTab
     */
    public MainForm(boolean dbg, String pathToPrefXml) {
	itself = this;
	profile = new Profile(); // sets MainForm.profile static access
	// Resize the Close und Ok-Buttons of all Forms. This is just a test for the PDA Versions:
	int fontSize = Preferences.itself().fontSize; // constructor default value
	FormBase.close = new DrawnIcon(DrawnIcon.CROSS, fontSize, fontSize, new Color(0, 0, 0));
	FormBase.tick = new DrawnIcon(DrawnIcon.TICK, fontSize, fontSize, new Color(0, 128, 0));
	FormBase.cross = new DrawnIcon(DrawnIcon.CROSS, fontSize, fontSize, new Color(128, 0, 0));

	Preferences.itself().debug = dbg;
	// in case pathtoprefxml == null the preferences will determine the path itself
	Preferences.itself().setPathToConfigFile(pathToPrefXml);
	doIt();
    }

    public void doIt() {
	this.exitSystemOnClose = true;
	this.resizable = true;
	this.moveable = true;
	this.windowFlagsToSet = WindowConstants.FLAG_MAXIMIZE_ON_PDA;
	// if (ewe.ui.Gui.screenSize.width <= 350 && ewe.ui.Gui.screenSize.height <= 350)
	//Rect screen = ((ewe.fx.Rect) (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new ewe.fx.Rect(),0)));
	//if ( screen.height >= 600 && screen.width >= 800) this.setPreferredSize(800, 600);
	this.resizeOnSIP = true;
	InfoBox infB = null;
	try {
	    Preferences.itself().readPrefFile();
	    Preferences.itself().logInit();
	    if (MyLocale.initErrors.length() != 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.initErrors).wait(FormBase.OKB);
	    }
	    if (Vm.isMobile()) {
		//this.windowFlagsToSet |=Window.FLAG_FULL_SCREEN;
		this.resizable = false;
		this.moveable = false;
	    } else {
		int h, w;
		h = Preferences.itself().myAppHeight;
		if (h > MyLocale.getScreenHeight())
		    h = MyLocale.getScreenHeight();
		w = Preferences.itself().myAppWidth;
		if (w > MyLocale.getScreenWidth())
		    w = MyLocale.getScreenWidth();
		this.setPreferredSize(w, h);
	    }
	    addGuiFont();
	    // Replace buildt-in symbols with customized images (if there are some)
	    GuiImageBroker.customizedSymbols();

	    if (!Preferences.itself().selectProfile(Preferences.PROFILE_SELECTOR_ONOROFF, true))
		ewe.sys.Vm.exit(0); // User MUST select or create a profile
	    Vm.showWait(true);

	    // Load CacheList
	    infB = new InfoBox("CacheWolf", MyLocale.getMsg(5000, "Loading Cache-List"));
	    infB.exec();
	    infB.waitUntilPainted(100);
	    MainForm.profile.readIndex(infB);
	    setTitle(MainForm.profile.name + " - CW " + Version.getRelease());
	} catch (Exception e) {
	    Preferences.itself().log("[MainForm:DoIt]", e);
	}

	if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
	    Vm.setSIP(Vm.SIP_LEAVE_BUTTON, mApp.mainApp);
	}
	if (Preferences.itself().fixSIP) {
	    if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
		//Vm.setSIP(Vm.SIP_LEAVE_BUTTON|Vm.SIP_ON);
		Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON, 1);
		Device.preventIdleState(true);
	    }
	} else
	    Vm.setSIP(0);

	mainMenu = new MainMenu(this); // ctor prior to mainTab
	mainTab = new MainTab();
	Preferences.itself().setCurCentrePt(MainForm.profile.centre); //uses mainTab.tablePanel

	mainMenu.allowProfileChange(true);

	split = new SplittablePanel(PanelSplitter.HORIZONTAL);
	split.theSplitter.thickness = 0;
	split.theSplitter.modify(Invisible, 0);
	// CacheList for CacheTour
	CellPanel pnlCacheList = split.getNextPanel();
	CellPanel pnlMainTab = split.getNextPanel();
	split.setSplitter(PanelSplitter.MIN_SIZE | PanelSplitter.BEFORE, PanelSplitter.HIDDEN | PanelSplitter.BEFORE, PanelSplitter.CLOSED);

	pnlCacheList.addLast(cacheList = new CacheList(), STRETCH, FILL);
	pnlMainTab.addLast(mainTab, STRETCH, FILL);
	this.addLast(split, STRETCH, FILL);

	mainMenu.setTablePanel(mainTab.getTablePanel());
	if (infB != null)
	    infB.close(0);
	mainTab.tablePanel.refreshTable();
	mainTab.tablePanel.selectFirstRow();
	//mainTab.tablePanel.tc.paintSelection();
	Vm.showWait(false);
	this.firstFocus = mainTab.tablePanel.myTableControl; // works if tablePanel is the first screen
    }

    protected void checkButtons() {
	if (Preferences.itself().hasCloseButton)
	    super.checkButtons();
    }

    protected boolean canExit(int exitCode) {
	mainTab.saveUnsavedChanges(true);
	return Preferences.itself().hasCloseButton;
    }

    private void addGuiFont() {
	Font defaultGuiFont = mApp.findFont("gui");
	Font newGuiFont = new Font(Preferences.itself().fontName, defaultGuiFont.getStyle(), Preferences.itself().fontSize);
	mApp.addFont(newGuiFont, "gui");
	mApp.fontsChanged();
	mApp.mainApp.font = newGuiFont;
    }

    public void doPaint(Graphics g, Rect r) {
	Preferences.itself().myAppHeight = this.height;
	Preferences.itself().myAppWidth = this.width;
	super.doPaint(g, r);
    }

    public void toggleCacheListVisible() {
	cacheListVisible = !cacheListVisible;
	if (cacheListVisible) {
	    // Make the splitterbar visible with a width of 6
	    split.theSplitter.modify(0, Invisible);
	    split.theSplitter.resizeTo(6, split.theSplitter.getRect().height);
	    MainForm.itself.mainMenu.cacheTour.modifiers |= MenuItem.Checked;
	} else {
	    // Hide the splitterbar and set width to 0
	    split.theSplitter.modify(Invisible, 0);
	    split.theSplitter.resizeTo(0, split.theSplitter.getRect().height);
	    MainForm.itself.mainMenu.cacheTour.modifiers &= ~MenuItem.Checked;
	}
	split.theSplitter.doOpenClose(cacheListVisible);
	MainForm.itself.mainMenu.repaint();
    }

    public boolean addCache(String wayPoint) {
	return cacheList.addCache(wayPoint);
    }

    public boolean contains(String wayPoint) {
	return cacheList.contains(wayPoint);
    }

    public void onEvent(Event ev) { // Preferences have been changed by PreferencesScreen
	if (Preferences.itself().dirty) {
	    mainTab.getTablePanel().myTableModel.setColumnNamesAndWidths();
	    mainTab.getTablePanel().refreshControl();
	    Preferences.itself().dirty = false;
	}
	super.onEvent(ev);
    }

}

/********************************************************
 * This class implements the core functionality of a flexible cachelist for collecting "Cachetours". Caches can be dragged into the list from the main list view and from the radar panel view. Caches can be removed from the list by dragging them out
 * or selecting them and pressing the "delete" key. Within the list the selected cache can be moved up/down using two buttons. The finished list can be saved and reloaded with the selected position being stored. The list can be applied as a filter to
 * the main list, thereby hiding all caches that are not in the list and sorting the caches according to the list. Created by skg, Februar 2007
 ********************************************************/
class CacheList extends CellPanel {
    /** The extension for cachelists (CL) */
    private final String EXTENSION = "CL";
    private final String TITLE = MyLocale.getMsg(188, "CACHETOUR: NEW");
    private static int applyCount = 0; // Counts the number of times we apply the list

    CacheList() {
	this.setPreferredSize(100, -1);
	this.equalWidths = true;
	mImage imgDown = new mImage("ewe/downarrowsmall.bmp");
	imgDown.transparentColor = Color.White;
	mImage imgUp = new mImage("ewe/uparrowsmall.bmp");
	imgUp.transparentColor = Color.White;
	// Title
	lblTitle = new mLabel(TITLE);
	lblTitle.backGround = new Color(0, 0, 200);
	lblTitle.foreGround = Color.White;
	addLast(lblTitle, HSTRETCH, HFILL | HCENTER);
	// The actual list
	lstCaches = new myList(10, 1, false);
	lstCaches.text = "CacheList";
	lstCaches.addItem(MyLocale.getMsg(180, "Drag caches"));
	lstCaches.addItem(MyLocale.getMsg(181, "here"));
	ScrollablePanel scp = lstCaches.getScrollablePanel();
	addLast(scp, STRETCH, FILL);
	scp.setOptions(ScrollablePanel.NeverShowHorizontalScrollers);
	// The buttons to move the selected cache
	addNext(btnDown = new mButton(imgDown), HSHRINK, HFILL);
	btnDown.modify(Disabled, 0);
	addLast(btnUp = new mButton(imgUp), HSHRINK, HFILL);
	btnUp.modify(Disabled, 0);
	// Buttons to clear, load and save the list
	CellPanel cp = new CellPanel();
	cp.equalWidths = true;
	cp.addNext(btnNew = new mButton(imgNew = new mImage("clnew.png")), HSTRETCH, HFILL);
	imgNew.transparentColor = new Color(255, 0, 0);
	btnNew.setToolTip(MyLocale.getMsg(182, "New list"));
	cp.addNext(btnLoad = new mButton(imgOpen = new mImage("clopen.png")), HSTRETCH, HFILL);
	imgOpen.transparentColor = new Color(255, 0, 0);
	btnLoad.setToolTip(MyLocale.getMsg(183, "Load list"));
	cp.addNext(btnSaveAs = new mButton(imgSaveAs = new mImage("clsaveas.png")), HSTRETCH, HFILL);
	imgSaveAs.transparentColor = new Color(0, 255, 0);
	btnSaveAs.setToolTip(MyLocale.getMsg(184, "Save as"));
	cp.addLast(btnSave = new mButton(imgSave = new mImage("clsave.png")), HSTRETCH, HFILL);
	imgSave.transparentColor = new Color(255, 0, 0);
	btnSave.setToolTip(MyLocale.getMsg(185, "Save (without confirmation)"));
	addLast(cp, HSTRETCH, HFILL);
	// Button to toggle whether additional waypoints are automatically dragged
	// with the parent waypoint
	addLast(chkAddAddis = new mCheckBox(MyLocale.getMsg(193, "add Addis")), HSTRETCH, HFILL);
	chkAddAddis.setToolTip(MyLocale.getMsg(186, "Also drag Addi Wpts"));
	// Finally button to apply the list as a filter
	addLast(btnFilter = new mButton(MyLocale.getMsg(189, "Apply List")), HSTRETCH, HFILL);
	btnFilter.modify(Disabled, 0);
	btnFilter.setToolTip(MyLocale.getMsg(190, "Show only these waypoints"));
    }

    /**
     * Flag to ensure the initial message "Caches hierher ziehen" is cleared when the first cache is dragged into the list
     */
    private boolean needsInit = true;
    /** The actual list. This is mirrored by cacheList */
    private myList lstCaches;
    /** True if there are unsaved changes */
    private boolean dirty = false;
    // The UI elements
    private mLabel lblTitle;
    private mCheckBox chkAddAddis;
    private mButton btnDown, btnUp, btnLoad, btnNew, btnSave, btnSaveAs, btnFilter;
    private mImage imgOpen, imgNew, imgSave, imgSaveAs;
    /**
     * This list mirrors the items in the list of selected caches for faster access. When the list of selected caches is manipulated (btnUp, btnDown), this list is also kept up to date
     */
    private CacheDB cacheList = new CacheDB();
    /** The full filename of the current file */
    private String currFile = null;

    private class myList extends mList {
	myList(int rows, int columns, boolean multi) {
	    super(rows, columns, multi);
	}

	//  Allow the caches to be dragged out of the cachelist
	int idx;

	public void startDragging(DragContext dc) {
	    idx = getSelectedIndex(0);
	    if (idx >= 0) {
		CacheHolder ch = cacheList.get(idx);
		IconAndText imgDrag = new IconAndText();
		imgDrag.addColumn(CacheType.getTypeImage(ch.getType()));
		imgDrag.addColumn(ch.getWayPoint());
		dc.dragData = dc.startImageDrag(imgDrag, new Point(8, 8), this);
	    }
	}

	public void dragged(DragContext dc) {
	    dc.imageDrag();
	}

	public void stopDragging(DragContext dc) {
	    dc.stopImageDrag(true);
	    Point p = Gui.getPosInParent(this, getWindow());
	    p.x += dc.curPoint.x;
	    p.y += dc.curPoint.y;
	    Control c = getWindow().findChild(p.x, p.y);
	    if (!(c instanceof myList)) {
		// target is not myList => Remove dragged cache from list
		cacheList.removeElementAt(idx);
		lstCaches.deleteItem(idx);
		repaint();
		changeUpDownButtonStatus();
	    }
	}

	// Alternative method of deleting a cache from the list through
	// Keyboard interface
	public void onKeyEvent(KeyEvent ev) {
	    /* This is a bit of a hack. By default Ewe sends key events to
	     * this panel. So if the list has not had anything dragged into it,
	     * we redirect the focus to the list view, assuming that that is where
	     * the key event needs to go.
	     */
	    if (needsInit && ev.target == this) {
		Gui.takeFocus(MainTab.itself.tablePanel.myTableControl, ControlConstants.ByKeyboard);
		ev.target = MainTab.itself.tablePanel.myTableControl;
		postEvent(ev);
	    }
	    if (ev.type == KeyEvent.KEY_PRESS && ev.target == this) {
		if (ev.key == IKeys.DELETE && cacheList.size() > 0) {
		    idx = getSelectedIndex(0);
		    cacheList.removeElementAt(idx);
		    lstCaches.deleteItem(idx);
		    repaint();
		    changeUpDownButtonStatus();
		}
	    }
	    super.onKeyEvent(ev);
	}

	public ScrollablePanel getScrollablePanel() {
	    dontAutoScroll = amScrolling = true;
	    ScrollBarPanel sp = new MyScrollBarPanel(this);
	    sp.modify(0, TakeControlEvents);
	    return sp;
	}

    } //******************* myList

    /** Simple sort to ensure that the main list keeps the order of this list */
    private class mySort implements Comparer {
	public int compare(Object o1, Object o2) {
	    CacheHolder oo1 = (CacheHolder) o1;
	    CacheHolder oo2 = (CacheHolder) o2;
	    return oo1.sort.compareTo(oo2.sort);
	}
    }

    /** Enable the up/down buttons only if at least 2 caches are in the list */
    private void changeUpDownButtonStatus() {
	btnUp.modify(0, Disabled);
	if (needsInit || lstCaches.itemsSize() < 2 || lstCaches.getSelectedIndex(0) == 0)
	    btnUp.modify(Disabled, 0);
	btnDown.modify(0, Disabled);
	if (needsInit || lstCaches.itemsSize() < 2 || lstCaches.getSelectedIndex(0) == lstCaches.itemsSize() - 1)
	    btnDown.modify(Disabled, 0);
	btnUp.repaintNow();
	btnDown.repaintNow();
	// Need at least 2 caches in list to enable it
	btnFilter.modify(0, Disabled);
	if (needsInit || lstCaches.itemsSize() < 2)
	    btnFilter.modify(Disabled, 0);
	btnFilter.repaintNow();
    }

    public void onEvent(Event ev) {
	if (ev instanceof MenuEvent && ev.type == MenuEvent.SELECTED) {
	    if (lstCaches.itemsSize() > 0 && !needsInit) {
		int lstCacheIdx = lstCaches.getSelectedIndex(0);
		CacheHolder ch = cacheList.get(lstCacheIdx);
		int idx = MainForm.profile.cacheDB.getIndex(ch);
		// Ensure that the main view is updated with the selected cache, i.e.
		// DetailsPanel, HintLog, Pictures etc.
		int activeTab = MainTab.itself.cardPanel.selectedItem;
		if (activeTab == 0) {
		    // Select the cache also in the main list view
		    MainTab.itself.tablePanel.selectRow(idx);
		    MainTab.itself.tablePanel.myTableControl.repaint();
		} else {
		    // We need to change to the list view first to load a new cache
		    MainTab.itself.onEvent(new MultiPanelEvent(0, MainTab.itself, 0));
		    MainTab.itself.tablePanel.selectRow(idx);
		    MainTab.itself.onEvent(new MultiPanelEvent(0, MainTab.itself, activeTab));
		}
	    }
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == btnNew) {
		newCacheList();
	    } else if (ev.target == btnLoad) {
		FileChooser fc = new FileChooser(FileChooserBase.OPEN, MainForm.profile.dataDir);
		//fc.addMask(currCh.wayPoint + ".wl");
		fc.addMask("*." + EXTENSION);
		fc.addMask("*.*");
		fc.setTitle(MyLocale.getMsg(191, "Select File"));
		if (fc.execute() != FormBase.IDCANCEL) {
		    currFile = fc.getChosen();
		    readFromFile(currFile);
		}
	    } else if ((ev.target == btnSave) && (currFile != null)) {
		saveToFile(currFile);
	    } else if ((ev.target == btnSaveAs) || ((ev.target == btnSave) && (currFile == null))) {
		FileChooser fc = new FileChooser(FileChooserBase.SAVE, MainForm.profile.dataDir);
		//fc.addMask(currCh.wayPoint + ".wl");
		fc.addMask("*." + EXTENSION);
		fc.setTitle(MyLocale.getMsg(191, "Select File"));
		if (fc.execute() != FormBase.IDCANCEL) {
		    currFile = fc.getChosen();
		    if (currFile.indexOf('.') == 0 || !currFile.toUpperCase().endsWith("." + EXTENSION))
			currFile += "." + EXTENSION;
		    saveToFile(currFile);
		}
	    } else if (ev.target == btnUp) {
		int sel = lstCaches.getSelectedIndex(0);
		if (sel > 0) {
		    dirty = true;
		    // Swap items in hidden list
		    Object swap = cacheList.get(sel - 1);
		    cacheList.set(sel - 1, cacheList.get(sel));
		    cacheList.set(sel, (CacheHolder) swap);
		    // Swap items in visible cachelist and repaint
		    swap = lstCaches.items.get(sel - 1);
		    lstCaches.items.set(sel - 1, lstCaches.items.get(sel));
		    lstCaches.items.set(sel, swap);
		    lstCaches.repaintDataNow();
		    lstCaches.select(sel - 1);
		}
	    } else if (ev.target == btnDown) {
		int sel = lstCaches.getSelectedIndex(0);
		if (sel < lstCaches.itemsSize() - 1) {
		    dirty = true;
		    // Swap items in hidden list
		    Object swap = cacheList.get(sel + 1);
		    cacheList.set(sel + 1, cacheList.get(sel));
		    cacheList.set(sel, (CacheHolder) swap);
		    // Swap items in visible cachelist and repaint
		    swap = lstCaches.items.get(sel + 1);
		    lstCaches.items.set(sel + 1, lstCaches.items.get(sel));
		    lstCaches.items.set(sel, swap);
		    lstCaches.repaintDataNow();
		    lstCaches.select(sel + 1);
		}
	    } else if (ev.target == btnFilter) {
		applyCacheList();
	    }
	}
	changeUpDownButtonStatus();
    }

    /** Apply the cache list */
    public void applyCacheList() {
	MainForm.profile.selectionChanged = true;
	CacheDB cacheDB = MainForm.profile.cacheDB;
	CacheHolder ch;
	int wrongVisStatus = 0;
	String apply = "\uFFFF" + Convert.toString(applyCount++);
	MainForm.profile.setFilterActive(Filter.FILTER_CACHELIST);
	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    cacheDB.get(i).sort = apply;
	}
	for (int i = cacheList.size() - 1; i >= 0; i--) {
	    ch = cacheDB.get((cacheList.get(i)).getWayPoint());
	    if (ch == null)
		continue; // Cache was deleted
	    if (!ch.isVisible())
		wrongVisStatus++;
	    String s = MyLocale.formatLong(i, "00000");
	    ch.sort = s;
	}
	// The sort command places all filtered caches at the end
	cacheDB.sort(new mySort(), false);
	updateScreen(cacheList.size() - wrongVisStatus);
	if (wrongVisStatus > 0)
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4600, "Some cache(s) cannot be shown because of wrong blacklist status")).wait(FormBase.OKB);

    }

    /**
     * Add a cache (and its addis) to the list
     * 
     * @return true if the cache is not already in lstCaches
     */
    public boolean addCache(String wayPoint) {
	// Check whether this is the first cache being added
	if (needsInit) {
	    lstCaches.deleteItem(0);
	    lstCaches.deleteItem(0);
	    needsInit = false;
	    lstCaches.repaint();
	}
	CacheHolder ch = MainForm.profile.cacheDB.get(wayPoint);
	if (ch == null)
	    return false;
	boolean cachesAdded = false;
	// Add main cache
	cachesAdded |= addCache(ch);
	// Add addis if user wants it
	if (chkAddAddis.state && ch.hasAddiWpt()) {
	    CacheHolder addiWpt;
	    for (int j = 0; j < ch.addiWpts.getCount(); j++) {
		addiWpt = (CacheHolder) ch.addiWpts.get(j);
		if (addiWpt.isVisible())
		    cachesAdded |= addCache(addiWpt);
	    }
	}
	// Update screen if any cache was added
	if (cachesAdded) {
	    lstCaches.select(lstCaches.itemsSize() - 1);
	    changeUpDownButtonStatus();
	}
	return cachesAdded;
    }

    /** Add a cache to the visible and invisible list */
    private boolean addCache(CacheHolder ch) {
	if (cacheList.getIndex(ch.getWayPoint()) < 0) {
	    // Add cache reference to hidden list
	    cacheList.add(ch);
	    // Add Cache and cache icon to visible list
	    lstCaches.addItem((new MenuItem()).iconize(ch.getWayPoint() + "   " + ch.getCacheName(), CacheType.getTypeImage(ch.getType()), true));
	    dirty = true;
	    return true;
	} else
	    return false;
    }

    void updateScreen(int numRows) {
	//		MainTab.itself.tablePanel.myMod.numRows=numRows;
	MainTab.itself.tablePanel.refreshTable(); // Update and repaint
	int selPanel;
	if ((selPanel = MainTab.itself.cardPanel.selectedItem) > -1) {
	    if (selPanel == 1) {
		//postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,MainTab.itself,1));
		MainTab.itself.detailsPanel.repaint();
	    }
	}
    }

    /** Check if there are any unsaved changes and ask user if he wants to save */
    public void saveIfDirty() {
	if (dirty) {
	    if (new InfoBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(192, "Save changes")).wait(FormBase.MBYESNO) == FormBase.IDYES) {
		if (currFile != null)
		    saveToFile(currFile);
		else {
		    FileChooser fc = new FileChooser(FileChooserBase.SAVE, MainForm.profile.dataDir);
		    fc.addMask("*." + EXTENSION);
		    fc.setTitle(MyLocale.getMsg(191, "Select File"));
		    if (fc.execute() != FormBase.IDCANCEL) {
			currFile = fc.getChosen();
			saveToFile(currFile);
		    }
		}
	    }
	}
	dirty = false;
    }

    /** Clear the cachelist (save unsaved changes if needed) */
    private void newCacheList() {
	saveIfDirty();
	lstCaches.items.clear();
	cacheList.clear();
	lstCaches.repaint();
	lblTitle.setText(TITLE);
	currFile = null;
    }

    /** Read a list of caches */
    private void readFromFile(String fileName) {
	if (needsInit) {
	    lstCaches.deleteItem(0);
	    lstCaches.deleteItem(0);
	    needsInit = false;
	}
	int select = -1;
	boolean selected = false;
	try {
	    FileReader in = new FileReader(fileName);
	    String wayPoint;
	    int lineNr = 0;
	    while ((wayPoint = in.readLine()) != null) {
		wayPoint = wayPoint.trim();
		// Select the cache starting with >
		if (wayPoint.startsWith(">")) {
		    wayPoint = wayPoint.substring(1);
		    selected = true;
		}
		// Only add the cache if it is in this MainForm.profile
		CacheHolder ch = MainForm.profile.cacheDB.get(wayPoint);
		if (ch != null) {
		    addCache(ch);
		    if (selected)
			select = lineNr + lineNr++;
		}
		selected = false;
	    }
	    in.close();
	} catch (Exception e) {
	    Preferences.itself().log("Problem reading: " + fileName, e, true);
	}
	if (select > -1)
	    lstCaches.select(select);
	else
	    lstCaches.select(lstCaches.itemsSize() - 1);
	lstCaches.repaint();
	this.postEvent(new MenuEvent(MenuEvent.SELECTED, this, null));
	changeUpDownButtonStatus();
	setTitle(fileName);
	dirty = false;
    }

    /** Save the cachelist */
    private void saveToFile(String fileName) {
	int selectedIndex = lstCaches.getSelectedIndex(0);
	try {
	    PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
	    for (int i = 0; i < cacheList.size(); i++) {
		// Put a > in front of the selected cache
		outp.print((i == selectedIndex ? ">" : "") + (cacheList.get(i)).getWayPoint() + "\n");
	    }
	    outp.close();
	} catch (Exception e) {
	    Preferences.itself().log("Problem saving: " + fileName, e, true);
	}
	setTitle(fileName);
	dirty = false;
    }

    /** Set the title */
    private void setTitle(String fileName) {
	String localFileName = fileName.replace('\\', '/');
	// Delete the path preceding the filename
	if (localFileName.lastIndexOf('/') > 0)
	    localFileName = localFileName.substring(localFileName.lastIndexOf('/') + 1);
	// Drop the extension
	if (localFileName.indexOf('.') > 0)
	    lblTitle.setText(localFileName.substring(0, localFileName.indexOf('.')));
	else
	    lblTitle.setText(localFileName);
	lblTitle.repaint();
    }

    /**
     * Determines if the cache tour contains a cache with a certain waypoint
     * 
     * @param waypoint
     *            Waypoint to check
     * @return <code>True</code>: Contains waypoint, otherwise not.
     */
    public boolean contains(String waypoint) {
	return cacheList.getIndex(waypoint) >= 0;
    }
}
