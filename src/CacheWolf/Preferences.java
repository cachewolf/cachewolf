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

import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CWPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.HttpConnection;
import CacheWolf.utils.Metrics;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.DrawnIcon;
import ewe.fx.Rect;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.io.SerialPort;
import ewe.io.SerialPortOptions;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.Window;
import ewe.ui.WindowConstants;
import ewe.ui.mApp;
import ewe.util.Comparer;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Map.MapEntry;
import ewe.util.StringTokenizer;
import ewe.util.Utils;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * A class to hold the preferences that were loaded upon start up of CacheWolf. This class is also capable of parsing the prefs.xml file as well as saving the current settings of preferences.
 */
public class Preferences extends MinML {

    private static Preferences preferences;
    /** Log file is in program directory and called log.txt */
    private final String LOGFILENAME = FileBase.getProgramDirectory() + "/log.txt";

    public static final int GPSD_DISABLED = 0; // do not use gpsd
    public static final int GPSD_FORMAT_OLD = 1; // use old protocol
    public static final int GPSD_FORMAT_NEW = 2; // use new protocol (JSON)
    public final int DEFAULT_GPSD_PORT = 2947;
    public final int DEFAULT_INITIAL_HINT_HEIGHT = 10;
    public static String NEWLINE = "\n";
    // ////////////////////////////////////////////////////////////////////////////////////
    // Public fields stored in pref.xml
    // SET BY PreferencesScreen
    // ////////////////////////////////////////////////////////////////////////////////////
    // General / Datadirectory
    /** The base directory contains one subdirectory for each profile */
    public String baseDir = "data/";
    /** If true, the last profile is reloaded automatically without a dialogue */
    public boolean autoReloadLastProfile = true;
    // General / Account
    /** This is the login alias for geocaching.com and opencaching.de */
    public String myAlias = "";
    /** Premium Member ? */
    public boolean isPremium = true;
    /** The Cookie from GC */
    public String userID = "";
    /** The own GC member ID (for gpx - export)*/
    public String gcMemberId = "";
    public final String[] gpxStyles = { //
    "STYLE_COMPCAT_OUTPUT_SINGLE", //
	    "STYLE_COMPCAT_OUTPUT_SEPARATE", //
	    "STYLE_COMPCAT_OUTPUT_POI", //
	    "STYLE_GPX_PQLIKE", //
	    "STYLE_GPX_MYFINDS" //
    };
    /** This is an alternative alias used to identify found caches */
    public String myAlias2 = "";
    /** Optional password, if empty you are asked. (OC equal GC) */
    public String password = "";
    // Maps/GPS
    /** Maps */
    String mapsBaseDir = "data/maps/";
    // GPS Serial Port Options
    /** Serial port Name and Bits, Parity, Stopbits, Baudrate */
    public SerialPortOptions mySPO = new SerialPortOptions();
    /** True if the GPS data should be forwarded to an IP address */
    public boolean forwardGPS = false;
    /** IP address for forwarding GPS data */
    public String forwardGpsHost = "192.168.1.15";
    /** Should GPS data be received from a GPSD on this or another host? */
    public int useGPSD = GPSD_DISABLED;
    /** IP address of GPSD host */
    public String gpsdHost = "127.0.0.1";
    /** Port for forwarding GPS data */
    public int gpsdPort = DEFAULT_GPSD_PORT;
    /** True if the GPS data should be logged to a file */
    public boolean logGPS = false;
    /** Timer for logging GPS data */
    public String logGPSTimer = "5";
    // Import from GC
    /** Check if lastFound in Listpage is newer than saved log */
    public boolean checkLog = false;
    /** Check if Difficulty, Terrain or Size changed in Listpage */
    public boolean checkDTS = true;
    /** Check if TBs changed in Listpage */
    public boolean checkTBs = true;
    /** maximum number of logs to store in cache details */
    public int maxLogsToKeep = Integer.MAX_VALUE;
    /** keep own logs even when exceeding <code>maxLogsToKeep</code> */
    public boolean alwaysKeepOwnLogs = true;
    /** GC (and OC) don't save the logTime. CW perhaps has the time.*/
    public boolean keepTimeOnUpdate = true;
    /** Maximum logs to spider */
    public int maxLogsToSpider = 99;
    /** overwrite stored Logs with the ones now fetched from GC*/
    public boolean overwriteLogs = true;
    /** */
    public boolean askForMaxNumbersOnImport = false;
    /** */
    public boolean addPremiumGC = false;
    public boolean useGCFavoriteValue = false; // use GCVote
    // Export
    /** Add short details to waypoint on export */
    public boolean addDetailsToWaypoint = false;
    /** Add short details to name on export */
    public boolean addDetailsToName = false;
    /** The type of connection which GPSBABEL uses: com1 OR usb. */
    public String garminConn = "com1";
    /** Additional options for GPSBabel, i.e. -s to synthethise short names */
    public String garminGPSBabelOptions = "";
    // Screen
    /** The locale code (DE, EN, ...) */
    public String language = "Auto";
    /** the name of an existing font to use*/
    public String fontName;
    /** The default font name and size will be determined automatically */
    public int fontSize;
    /** display text (on buttons ...) else display icons. */
    public boolean useText = true;
    /** display icons (on buttons ...) else display text. */
    public boolean useIcons = true;
    /** display big icons. default only true for VGA PDAs */
    public boolean useBigIcons = false;
    /** True if don't use tabs for program navigation (using Buttons with a homepage) */
    public boolean noTabs = true;
    /** True if the tabs are to be displayed at the top of the screen */
    public boolean tabsAtTop = true;
    /** True its on the same side as the tab selection */
    public boolean menuAtTab = true;
    /** True if the status bar is to be displayed (hidden if false) */
    public boolean showStatus = true;
    /** True if the application can be closed by clicking on the close button in the top line. This can be set to avoid accidental closing of the application */
    public boolean hasCloseButton = true;
    // Tabcards
    /** TablePanel: SortingGroupedByCache */
    public boolean SortingGroupedByCache = true;
    /** DescriptionPanel: True if the description panel should show images */
    public boolean descShowImg = true;
    /** ImagePanel: If this is true, deleted images are shown with a red ?-Image */
    public boolean showDeletedImages = true;
    /** HintLogPanel: Determines how many logs are shown per page */
    public final int DEFAULT_LOGS_PER_PAGE = 5;
    public int logsPerPage = DEFAULT_LOGS_PER_PAGE;
    // More
    /** The path to the browser */
    public String browser = "";
    /** Name of HTTP proxy for spidering */
    public String myproxy = "";
    /** HTTP proxy port when spidering */
    public String myproxyport = "";
    /** Flag whether proxy is to be used */
    public boolean proxyActive = false;
    /** The metric system to use */
    public int metricSystem = Metrics.METRIC;
    /** set Debugging. can also be set by program arguments */
    public boolean debug = false;
    // lookout of TablePanel Cachetable
    /** The list of visible columns in the list view */
    public String listColMap = "0,1,2,3,4,5,6,7,8,9,10,11,12";
    // Travelbugs
    /** The columns which are to be displayed in TravelbugsJourneyScreen. See also TravelbugJourney */
    public String travelbugColMap = "1,4,5,6,8,9,10,7";

    // TODO
    // ////////////////////////////////////////////////////////////////////////////////////
    // Public fields stored in pref.xml
    // NOT SET BY PreferencesScreen
    // ////////////////////////////////////////////////////////////////////////////////////

    public String absoluteBaseDir; // wird gespeichert
    public String absoluteMapsBaseDir; // wird gespeichert

    /** Maximum number of new caches to spider */
    public int maxSpiderNumber = 200;

    /** icons middle or left (on Buttons) - middle looks better : not used*/
    public boolean leftIcons = false;

    /** Name of last used profile */
    public String lastProfile = "home";
    /** If true current cetre will be set from gps position */
    public boolean setCurrentCentreFromGPSPosition = true;

    /** True if the SIP is always visible */
    public boolean fixSIP = false;

    /** The widths for each column in list view */
    public String listColWidth = "15,20,20,25,92,177,144,83,60,105,50,104,22,30,30,30,30,30,30,30";
    /** TRUE if we want automatic sorting (by distance) */
    public boolean sortAutomatic = true;
    public boolean hasTickColumn = true;
    // Hashtable is saving filter data objects the user wants to save
    private Hashtable filterList = new Hashtable(15);
    /** screen is big enough to hold additional information like cache notes */

    /** Initial height of hints field (set to 0 to hide them initially) */
    public int initialHintHeight = DEFAULT_INITIAL_HINT_HEIGHT;
    /** True if the Solver should ignore the case of variables */
    public boolean solverIgnoreCase = true;
    /**
     * True if the solver expects arguments for trigonometric functions in degrees
     */
    public boolean solverDegMode = true;
    /**
     * Max. length for Garmin waypoint names (for etrex which can only accept 6 chars)
     */
    public int garminMaxLen = 0;
    /** OC true = alle neu Laden false = wenn ?nderungsdatum neuer */
    public boolean downloadAllOC = false;
    public String lastOCSite = OC.OCSites[0][OC.OC_HOSTNAME];
    /**
     * The currently used centre point, can be different from the profile's centrepoint. This is used for spidering
     */
    public CWPoint curCentrePt = new CWPoint();
    public boolean changedGCLanguageToEnglish = false;
    /** True if the goto panel is North centered */
    public boolean northCenteredGoto = true;

    /** Number of CacheHolder details that are kept in memory */
    public int maxDetails = 50;
    /** Number of details to delete when maxDetails have been stored in cachesWithLoadedDetails */
    public int deleteDetails = 5;

    /** Download images when loading cache data */
    public boolean downloadPics = true;
    /** Download TB information when loading cache data */
    public boolean downloadTBs = true;

    /** Last mode select in the DataMover for processing cache */
    public int processorMode = 0;
    // maps
    /** Determines whether to fill the white areas on the map */
    public boolean fillWhiteArea = false;
    public boolean showCachesOnMap = true;
    public float lastScale = 1f;
    // maps download
    /** Width and height of free defined tile size */
    public int tilewidth;
    public int tileheight;
    /** How many pixels should maptiles overlap */
    public int mapOverlapping = 2;

    /** The column widths for the travelbug journeys. */
    public String travelbugColWidth = "212,136,62,90,50,56,90,38,50,50,94,50";
    /** If this flag is true, only non-logged travelbug journeys will be shown */
    public boolean travelbugShowOnlyNonLogged = false;
    //
    public String oldGCLanguage = "";
    public boolean doNotGetFound = true;
    /**
     * This switches the behaviour of GUI-Element factories.
     * If set to true, it will construct alternative Forms.
     * It can only be set in the Preference-File directly, not by user-interaction now.
     * Add <MobileGui value="true"/> to your pref.xml
     */
    public boolean mobileGUI = false;

    // ////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Singleton pattern - return reference to Preferences
     * 
     * @return Singleton Preferences object
     */
    public static Preferences itself() {
	if (preferences == null)
	    // it's ok, we can call this constructor
	    preferences = new Preferences();
	return preferences;
    }

    private String pathToConfigFile;

    /**
     * Call this method to set the path of the config file <br>
     * If you call it with null it defaults to [program-dir]/pref.xml if p is a directory "pref.xml" will automatically appended
     * 	// in case pathtoprefxml == null the preferences will determine the path itself
     * @param p
     */
    public void setPathToConfigFile(String pathToPrefXml) {
	String p_;
	if (pathToPrefXml == null) {
	    /*
	    // returns in java-vm on win xp: c:\<dokumente und Einstellungen>\<username>\<application data> 
	    String test;
	    test = Vm.getenv("APPDATA", "/");
	    log("Vm.getenv(APPDATA: " + test);

	    // this works also in win32.exe (ewe-vm on win xp) 
	    // in MS-java-VM  env variable $HOME is ignored and always <windir>\java  returned, see http:support.microsoft.com/kb/177181/en-us/
	    // This should return on *nix system the home dir
	    test = Vm.getenv("HOME", "/");
	    log("Vm.getenv(HOME: " + test);

	    // "user.dir" User's current working directory 
	    // return in java-vm on win  xp: <working dir> or maybe <program dir>
	    // in win32.exe ->  null 
	    test = System.getProperty("user.dir");
	    log("System.getProperty(user.dir: " + test);

	    // "user.home" User home directory (taken from http://scv.bu.edu/Doc/Java/tutorial/java/system/properties.html )
	    // in win32.exe -> null 
	    test = System.getProperty("user.home");
	    log("System.getProperty(user.home: " + test);
	    */
	    p_ = FileBase.makePath(FileBase.getProgramDirectory(), "pref.xml");
	} else {
	    if (new File(pathToPrefXml).isDirectory())
		p_ = FileBase.makePath(pathToPrefXml, "pref.xml");
	    else
		p_ = pathToPrefXml;
	}
	// this is necessary in case that the root dir is the dir where the pref.xml is stored
	pathToConfigFile = STRreplace.replace(p_, "//", "/");
	pathToConfigFile = pathToConfigFile.replace('\\', '/');
	pathToPrefXml = System.getProperty("os.name");
	if (pathToPrefXml == null || pathToPrefXml.indexOf("indows") != -1) {
	    NEWLINE = "\r\n";
	}
    }

    /**
     * Constructor is private for a singleton object
     */
    private Preferences() {
	mySPO.bits = 8;
	mySPO.parity = SerialPort.NOPARITY;
	mySPO.stopBits = 1;
	mySPO.baudRate = 4800;
	fontName = mApp.findFont("gui").getName();
	if (((ewe.fx.Rect) (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT, null, new ewe.fx.Rect(), 0))).height > 400) {
	    if (Vm.getPlatform().equals("Unix"))
		fontSize = 12;
	    else {
		// Default on VGA-PDAs: fontSize 21 + adjust ColWidth
		if (Vm.isMobile()) {
		    fontSize = 21;
		    listColWidth = "20,20,30,30,92,177,144,83,60,105,50,104,22,30,30";
		} else
		    fontSize = 16;
	    }
	} else
	    fontSize = 11;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // Public fields not stored in pref.xml
    // ////////////////////////////////////////////////////////////////////////////////////

    private Rect screenSize = (Rect) Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT, null, new Rect(), 0);
    /** The height of the application */
    private int screenHeight = screenSize.height;
    /** The width of the application */
    private int screenWidth = screenSize.width;
    /** True if the preferences were changed and need to be saved */
    public boolean dirty = false;

    // ////////////////////////////////////////////////////////////////////////////////////
    // Read pref.xml file
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method to open and parse the config file (pref.xml). Results are stored in the public variables of this class. If you want to specify a non default config file call setPathToConfigFile() first.
     */
    public void readPrefFile() {
	MyLocale.language = language;
	if (pathToConfigFile == null)
	    setPathToConfigFile(null); // this sets the default value
	try {
	    ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(pathToConfigFile));
	    parse(r);
	    r.close();
	} catch (IOException e) {
	    log("IOException reading config file: " + pathToConfigFile, e, true);
	    browser = getDefaultBrowser();

	    File f = new File(FileBase.makePath(FileBase.makePath(FileBase.getProgramDirectory(), baseDir), lastProfile));
	    if (f.mkdirs()) {
		setBaseDir(baseDir);
	    }

	    f = new File(FileBase.makePath(FileBase.getProgramDirectory(), mapsBaseDir));
	    if (f.mkdirs()) {
		setMapsBaseDir(mapsBaseDir);
	    }
	    savePreferences();

	    GuiImageBroker.init(useText, useIcons, useBigIcons, leftIcons);
	    InfoBox.init(fontSize, useBigIcons);
	    new InfoBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(176, "First start - using default preferences \n For experts only: \n Could not read preferences file:\n") + pathToConfigFile).wait(FormBase.OKB);
	} catch (Exception e) {
	    if (e instanceof NullPointerException)
		log("Error reading pref.xml: NullPointerException in Element " + lastName + ". Wrong attribute?", e, true);
	    else
		log("Error reading pref.xml: " + lastName, e);
	}

	// Resize the Close und Ok-Buttons of all Forms. This is just a test for the PDA Versions:
	FormBase.close = new DrawnIcon(DrawnIcon.CROSS, fontSize, fontSize, new Color(0, 0, 0));
	FormBase.tick = new DrawnIcon(DrawnIcon.TICK, fontSize, fontSize, new Color(0, 128, 0));
	FormBase.cross = new DrawnIcon(DrawnIcon.CROSS, fontSize, fontSize, new Color(128, 0, 0));
	GuiImageBroker.init(useText, useIcons, useBigIcons, leftIcons);
	InfoBox.init(fontSize, useBigIcons);
	HttpConnection.setProxy(this.myproxy, Common.parseInt(this.myproxyport), this.proxyActive);
	MyLocale.language = language;
    }

    /**
     * Tries to find a executable browser return "" if no browser found
     * 
     * @return
     */
    private String getDefaultBrowser() {
	String pf = Vm.getPlatform();
	String testlist[] = null;
	if (pf.equals("Java") || pf.equals("Win32")) {
	    // at least in java-Win XP this is set
	    String progdir = Vm.getenv("ProgramFiles", null);
	    String homedir = Vm.getenv("HOMEPATH", "").replace('\\', '/');
	    if (progdir != null) {
		String test[] = { homedir + "/Lokale Einstellungen/Anwendungsdaten/Google/Chrome/Application/chrome.exe", progdir + "/Firefox/firefox.exe", progdir + "/Opera/opera.exe", progdir + "/Internet Explorer/iexplore.exe" };
		testlist = test;
	    } else {
		String test[] = { // this part is not tested
		"/opt/firefox/firefox", // default path in ubuntu
			"/usr/bin/firefox" };
		testlist = test;
	    }
	}
	if (pf.equals("WinCE")) {
	    String test[] = { "/windows/iexplore.exe" };
	    testlist = test;
	}
	if (testlist != null) {
	    for (int i = 0; i < testlist.length; i++)
		if ((new File(testlist[i])).exists()) {
		    return testlist[i];
		}
	}
	return "";
    }

    public int getScreenWidth() {
	return screenWidth;
    }

    public void setScreenWidth(int myAppWidth) {
	this.screenWidth = myAppWidth;
    }

    public int getScreenHeight() {
	return screenHeight;
    }

    public void setScreenHeight(int myAppHeight) {
	screenHeight = myAppHeight;
    }

    /** Helper variables for XML parser */
    private StringBuffer collectElement = null;
    // The string to the last XML that was processed
    private String lastName;

    private long getLongAttr(AttributeList atts, String name) {
	String stmp = atts.getValue(name);
	long ret = 0l;
	if (stmp != null) {
	    ret = Convert.parseLong(stmp);
	}
	return ret;
    }

    /**
     * Method that gets called when a new element has been identified in pref.xml
     */
    public void startElement(String name, AttributeList atts) {
	lastName = name;
	String tmp;
	if (name.equals("browser")) {
	    browser = atts.getValue("name");
	    if (browser == null || browser.length() == 0)
		browser = getDefaultBrowser();
	} else if (name.equals("fixedsip")) {
	    if (atts.getValue("state").equals("true")) {
		fixSIP = true;
	    }
	} else if (name.equals("font")) {
	    fontSize = Convert.toInt(atts.getValue("size"));
	    fontName = atts.getValue("name");
	    if (fontName == null)
		fontName = mApp.findFont("gui").getName();
	} else if (name.equals("alias")) {
	    myAlias = SafeXML.html2iso8859s1(atts.getValue("name"));
	    tmp = SafeXML.html2iso8859s1(atts.getValue("password"));
	    if (tmp != null)
		password = tmp;
	} else if (name.equals("alias2"))
	    myAlias2 = SafeXML.html2iso8859s1(atts.getValue("name"));
	else if (name.equals("gcmemberid")) {
	    gcMemberId = atts.getValue("name");
	    tmp = atts.getValue("Premium");
	    if (tmp != null)
		isPremium = Boolean.valueOf(tmp).booleanValue();
	} else if (name.equals("location")) {
	    curCentrePt.set(atts.getValue("lat") + " " + atts.getValue("long"));
	} else if (name.equals("port")) {
	    mySPO.portName = atts.getValue("portname");
	    mySPO.baudRate = Convert.toInt(atts.getValue("baud"));
	} else if (name.equals("portforward")) {
	    forwardGPS = Convert.toBoolean(atts.getValue("active"));
	    forwardGpsHost = atts.getValue("destinationHost");
	} else if (name.equals("gpsd")) {
	    useGPSD = Convert.toInt(atts.getValue("active"));
	    gpsdHost = atts.getValue("host");
	    gpsdPort = Convert.toInt(atts.getValue("port"));
	} else if (name.equals("portlog")) {
	    logGPS = Convert.toBoolean(atts.getValue("active"));
	    logGPSTimer = atts.getValue("logTimer");
	} else if (name.equals("lastprofile")) {
	    collectElement = new StringBuffer(50);
	    if (!atts.getValue("autoreload").equalsIgnoreCase("true"))
		autoReloadLastProfile = false;
	} else if (name.equals("CurrentCentre")) {
	    setCurrentCentreFromGPSPosition = Boolean.valueOf(atts.getValue("FromGPSPosition")).booleanValue();
	} else if (name.equals("basedir")) {
	    setBaseDir(atts.getValue("dir"));
	} else if (name.equals("opencaching")) {
	    tmp = atts.getValue("lastSite");
	    if (!(tmp == null) && OC.getSiteIndex(tmp) >= 0)
		lastOCSite = tmp;
	    tmp = atts.getValue("downloadMissing");
	    if (!(tmp == null))
		downloadAllOC = Boolean.valueOf(tmp).booleanValue();
	} else if (name.equals("listview")) {
	    listColMap = atts.getValue("colmap");
	    if (("," + listColMap + ",").indexOf(",0,") >= 0)
		this.hasTickColumn = true;
	    else
		this.hasTickColumn = false;
	    listColWidth = atts.getValue("colwidths");
	    while ((new StringTokenizer(listColWidth, ",")).countTokens() < MyTableModel.N_COLUMNS)
		listColWidth += ",30"; // for older versions
	} else if (name.equals("proxy")) {
	    myproxy = atts.getValue("prx");
	    myproxyport = atts.getValue("prt");
	    tmp = atts.getValue("active");
	    if (tmp != null)
		proxyActive = Boolean.valueOf(tmp).booleanValue();
	} else if (name.equals("garmin")) {
	    garminConn = atts.getValue("connection");
	    tmp = atts.getValue("GPSBabelOptions");
	    if (tmp != null)
		garminGPSBabelOptions = tmp;
	    tmp = atts.getValue("MaxWaypointLength");
	    if (tmp != null)
		garminMaxLen = Convert.toInt(tmp);
	    tmp = atts.getValue("addDetailsToWaypoint");
	    if (tmp != null)
		addDetailsToWaypoint = Boolean.valueOf(tmp).booleanValue();
	    tmp = atts.getValue("addDetailsToName");
	    if (tmp != null)
		addDetailsToName = Boolean.valueOf(tmp).booleanValue();
	} else if (name.equals("imagepanel")) {
	    showDeletedImages = Boolean.valueOf(atts.getValue("showdeletedimages")).booleanValue();
	} else if (name.equals("descpanel")) {
	    descShowImg = Boolean.valueOf(atts.getValue("showimages")).booleanValue();
	} else if (name.equals("screen")) {
	    noTabs = Boolean.valueOf(atts.getValue("noTabs")).booleanValue();
	    tabsAtTop = Boolean.valueOf(atts.getValue("tabsAtTop")).booleanValue();
	    menuAtTab = Boolean.valueOf(atts.getValue("menuAtTab")).booleanValue();
	    showStatus = Boolean.valueOf(atts.getValue("showstatus")).booleanValue();
	    if (atts.getValue("hasclosebutton") != null)
		hasCloseButton = Boolean.valueOf(atts.getValue("hasclosebutton")).booleanValue();
	    if (atts.getValue("h") != null) {
		screenHeight = Convert.toInt(atts.getValue("h"));
		screenWidth = Convert.toInt(atts.getValue("w"));
	    }
	    if (atts.getValue("useText") != null)
		useText = Boolean.valueOf(atts.getValue("useText")).booleanValue();
	    else
		useText = true;
	    if (atts.getValue("useIcons") != null)
		useIcons = Boolean.valueOf(atts.getValue("useIcons")).booleanValue();
	    else
		useIcons = true;
	    if (atts.getValue("useBigIcons") != null)
		useBigIcons = Boolean.valueOf(atts.getValue("useBigIcons")).booleanValue();
	    else {
		useBigIcons = screenWidth >= 400; // && Vm.isMobile()
	    }
	} else if (name.equals("hintlogpanel")) {
	    logsPerPage = Convert.parseInt(atts.getValue("logsperpage"));
	    String strInitialHintHeight = atts.getValue("initialhintheight");
	    if (strInitialHintHeight != null)
		initialHintHeight = Convert.parseInt(strInitialHintHeight);
	    String strMaxLogsToSpider = atts.getValue("maxspiderlogs");
	    if (strMaxLogsToSpider != null)
		maxLogsToSpider = Convert.parseInt(strMaxLogsToSpider);
	} else if (name.equals("solver")) {
	    solverIgnoreCase = Boolean.valueOf(atts.getValue("ignorevariablecase")).booleanValue();
	    tmp = atts.getValue("degMode");
	    if (tmp != null)
		solverDegMode = Boolean.valueOf(tmp).booleanValue();
	} else if (name.equals("mapspath")) {
	    setMapsBaseDir(atts.getValue("dir"));
	} else if (name.equals("debug"))
	    debug = Boolean.valueOf(atts.getValue("value")).booleanValue();
	else if (isGpxStyleName(name)) {
	    Hashtable h = getGpxExportPreferences(name);
	    for (int i = 0; i < atts.getLength(); i++) {
		h.put(atts.getName(i), atts.getValue(i));
	    }
	} else if (name.equals("expPath")) {
	    for (int i = 0; i < atts.getLength(); i++) {
		exporterPaths.put(atts.getName(i), atts.getValue(i));
	    }
	} else if (name.equals("impPath")) {
	    for (int i = 0; i < atts.getLength(); i++) {
		importerPaths.put(atts.getName(i), atts.getValue(i));
	    }
	} else if (name.equals("travelbugs")) {
	    travelbugColMap = atts.getValue("colmap");
	    travelbugColWidth = atts.getValue("colwidths");
	    travelbugShowOnlyNonLogged = Boolean.valueOf(atts.getValue("shownonlogged")).booleanValue();
	} else if (name.equals("gotopanel")) {
	    northCenteredGoto = Boolean.valueOf(atts.getValue("northcentered")).booleanValue();
	} else if (name.equals("spider")) {
	    doNotGetFound = !Boolean.valueOf(atts.getValue("getFinds")).booleanValue();
	    checkLog = Boolean.valueOf(atts.getValue("checkLog")).booleanValue();
	    overwriteLogs = Boolean.valueOf(atts.getValue("overwriteLogs")).booleanValue();
	    askForMaxNumbersOnImport = Boolean.valueOf(atts.getValue("askForMaxNumbersOnImport")).booleanValue();
	    addPremiumGC = Boolean.valueOf(atts.getValue("addPremiumGC")).booleanValue();
	    useGCFavoriteValue = Boolean.valueOf(atts.getValue("useGCFavoriteValue")).booleanValue();
	    tmp = atts.getValue("checkTBs");
	    if (tmp != null)
		checkTBs = Boolean.valueOf(atts.getValue("checkTBs")).booleanValue();
	    tmp = atts.getValue("checkDTS");
	    if (tmp != null)
		checkDTS = Boolean.valueOf(atts.getValue("checkDTS")).booleanValue();
	    /*
	    tmp = atts.getValue("spiderUpdates");
	    if (tmp != null)
	    spiderUpdates = Convert.parseInt(tmp);
	    */
	    tmp = atts.getValue("maxSpiderNumber");
	    if (tmp != null)
		maxSpiderNumber = Convert.parseInt(tmp);
	    tmp = atts.getValue("downloadPics");
	    if (tmp != null)
		downloadPics = Boolean.valueOf(tmp).booleanValue();
	    tmp = atts.getValue("downloadTBs");
	    if (tmp != null)
		downloadTBs = Boolean.valueOf(tmp).booleanValue();
	    userID = atts.getValue("UserID");
	    if (userID == null)
		userID = "";
	} else if (name.equals("details")) {
	    maxDetails = Common.parseInt(atts.getValue("cacheSize"));
	    deleteDetails = Common.parseInt(atts.getValue("delete"));
	    if (maxDetails < 2)
		maxDetails = 2;
	    if (deleteDetails < 1)
		deleteDetails = 1;
	} else if (name.equals("metric")) {
	    metricSystem = Common.parseInt(atts.getValue("type"));
	    if (metricSystem != Metrics.METRIC && metricSystem != Metrics.IMPERIAL) {
		metricSystem = Metrics.METRIC;
	    }
	} else if (name.equals("locale")) {
	    language = atts.getValue("language");
	} else if (name.equals("FILTERDATA")) {
	    // Creating a filter object and reading the saved data
	    String id = SafeXML.html2iso8859s1(atts.getValue("id"));
	    FilterData data = new FilterData();
	    data.setFilterRose(atts.getValue("rose"));
	    data.setFilterType(atts.getValue("type"));
	    data.setFilterVar(atts.getValue("var"));
	    data.setFilterDist(atts.getValue("dist"));
	    data.setFilterDiff(atts.getValue("diff"));
	    data.setFilterTerr(atts.getValue("terr"));
	    data.setFilterSize(atts.getValue("size"));
	    long[] filterAttr = { 0l, 0l, 0l, 0l };
	    filterAttr[0] = getLongAttr(atts, "attributesYes");
	    filterAttr[1] = getLongAttr(atts, "attributesYes1");
	    filterAttr[2] = getLongAttr(atts, "attributesNo");
	    filterAttr[3] = getLongAttr(atts, "attributesNo1");
	    data.setFilterAttr(filterAttr);
	    data.setFilterAttrChoice(Convert.parseInt(atts.getValue("attributesChoice")));
	    data.setFilterStatus(SafeXML.html2iso8859s1(atts.getValue("status")));
	    data.setUseRegexp(Boolean.valueOf(atts.getValue("useRegexp")).booleanValue());
	    tmp = atts.getValue("noCoord");
	    if (tmp != null) {
		data.setFilterNoCoord(Boolean.valueOf(tmp).booleanValue());
	    } else {
		data.setFilterNoCoord(true);
	    }
	    // Order within the search items must not be changed
	    String searchFilter = SafeXML.html2iso8859s1(atts.getValue("search"));
	    String[] searchFilterList = ewe.util.mString.split(searchFilter, '|');
	    for (int i = 0; i < searchFilterList.length; i++) {
		if (i == 0)
		    data.setSyncDate(searchFilterList[i]);
		if (i == 1)
		    data.setNamePattern(searchFilterList[i]);
		if (i == 2) {
		    if (searchFilterList[i].length() >= 2) {
			data.setNameCompare(Integer.parseInt(searchFilterList[i].substring(0, 1)));
			data.setNameCaseSensitive(searchFilterList[i].substring(1, 2).equals("0") ? false : true);
		    }
		}
	    }
	    // Filter object is remembered under the given ID
	    this.addFilter(id, data);
	} else if (name.equals("datamover")) {
	    tmp = atts.getValue("processorMode");
	    if (tmp != null) {
		processorMode = Convert.parseInt(tmp);
	    }
	} else if (name.equals("logkeeping")) {
	    tmp = atts.getValue("maximum");
	    if (tmp != null)
		maxLogsToKeep = Convert.parseInt(tmp);
	    if (maxLogsToKeep < 0)
		maxLogsToKeep = 0;

	    tmp = atts.getValue("keepown");
	    if (tmp != null)
		alwaysKeepOwnLogs = Boolean.valueOf(tmp).booleanValue();

	    tmp = atts.getValue("keepTimeOnUpdate");
	    if (tmp != null)
		keepTimeOnUpdate = Boolean.valueOf(tmp).booleanValue();

	} else if (name.equals("fillWhiteArea")) {
	    tmp = atts.getValue("on");
	    fillWhiteArea = tmp != null && tmp.equalsIgnoreCase("true");
	} else if (name.equals("mapLoader")) {
	    tmp = atts.getValue("overlapping");
	    if (tmp == null || tmp.length() == 0)
		tmp = "0";
	    mapOverlapping = Convert.parseInt(tmp);
	    tmp = atts.getValue("tilewidth");
	    tilewidth = (tmp != null && tmp.length() > 0) ? Convert.parseInt(tmp) : 0;
	    tmp = atts.getValue("tileheight");
	    tileheight = (tmp != null && tmp.length() > 0) ? Convert.parseInt(tmp) : 0;
	} else if (name.equals("Map")) {
	    tmp = atts.getValue("showCachesOnMap");
	    if (tmp != null)
		showCachesOnMap = Boolean.valueOf(atts.getValue("showCachesOnMap")).booleanValue();
	    tmp = atts.getValue("lastScale");
	    if (tmp != null)
		lastScale = ((int) (Common.parseDouble(tmp) * 100)) / 100f;
	} else if (name.equals("SortingGroupedByCache")) {
	    tmp = atts.getValue("on");
	    SortingGroupedByCache = tmp != null && tmp.equalsIgnoreCase("true");
	} else if (name.equals("MobileGui"))
	    mobileGUI = Boolean.valueOf(atts.getValue("value")).booleanValue();
    }

    private boolean isGpxStyleName(String name) {
	for (int i = 0; i < this.gpxStyles.length; i++) {
	    if (name.equals(gpxStyles[i]))
		return true;
	}
	return false;
    }

    public void characters(char ch[], int start, int length) {
	if (collectElement != null) {
	    // Collect the name of the last profile
	    collectElement.append(ch, start, length);
	}
    }

    /**
     * Method that gets called when the end of an element has been identified in pref.xml
     */
    public void endElement(String tag) {
	if (tag.equals("lastprofile")) {
	    if (collectElement != null)
		lastProfile = collectElement.toString();
	}
	collectElement = null;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // Write pref.xml file
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Method to save current preferences in the pref.xml file
     */
    public void savePreferences() {
	if (pathToConfigFile == null)
	    setPathToConfigFile(null); // this sets the default value
	try {
	    File f = new File(pathToConfigFile);
	    if (f.exists())
		f.delete();

	    PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(pathToConfigFile)));
	    outp.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
		    + "<preferences>\n" //
		    + "    <locale language=\"" + SafeXML.string2Html(language) + "\" />\n" //

		    + "    <basedir dir=\"" + SafeXML.string2Html(baseDir) + "\" />\n"//

		    + "    <lastprofile" //
		    + " autoreload=\"" + SafeXML.strxmlencode(autoReloadLastProfile) + "\"" //
		    + ">" //
		    + SafeXML.string2Html(lastProfile) + "</lastprofile>\n" //

		    + "    <CurrentCentre" //
		    + " FromGPSPosition=\"" + SafeXML.strxmlencode(setCurrentCentreFromGPSPosition) + "\"" //
		    + " />\n" //

		    + "    <alias" //
		    + " name=\"" + SafeXML.string2Html(myAlias) + "\"" //
		    + " password=\"" + SafeXML.string2Html(password) + "\"" //
		    + " />\n" //

		    + "    <alias2 name=\"" + SafeXML.string2Html(myAlias2) + "\" />\n" //

		    + "    <gcmemberid" //
		    + " name=\"" + SafeXML.string2Html(gcMemberId) + "\"" //
		    + " Premium=\"" + SafeXML.strxmlencode(isPremium) + "\"" //
		    + " />\n" //

		    + "    <browser name=\"" + SafeXML.string2Html(browser) + "\" />\n" //

		    + "    <proxy" //
		    + " prx=\"" + SafeXML.string2Html(myproxy) + "\"" //
		    + " prt=\"" + SafeXML.string2Html(myproxyport) + "\"" //
		    + " active=\"" + SafeXML.strxmlencode(proxyActive) + "\"" //
		    + " />\n" //

		    + "    <port" //
		    + " portname=\"" + SafeXML.string2Html(mySPO.portName) + "\"" //
		    + " baud=\"" + SafeXML.strxmlencode(mySPO.baudRate) + "\"" //
		    + " />\n" //

		    + "    <portforward" //
		    + " active=\"" + SafeXML.strxmlencode(forwardGPS) + "\"" //
		    + " destinationHost=\"" + SafeXML.string2Html(forwardGpsHost) + "\"" //
		    + " />\n" //

		    + "    <gpsd" //
		    + " active=\"" + SafeXML.strxmlencode(useGPSD) + "\"" //
		    + " host=\"" + SafeXML.string2Html(gpsdHost) + "\"" //
		    + " port=\"" + SafeXML.strxmlencode(gpsdPort) + "\"" //
		    + " />\n" //

		    + "    <portlog" //
		    + " active=\"" + SafeXML.strxmlencode(logGPS) + "\"" //
		    + " logTimer=\"" + SafeXML.string2Html(logGPSTimer) + "\"" //
		    + " />\n" //

		    + "    <font" //
		    + " name=\"" + fontName + "\"" //
		    + " size=\"" + SafeXML.strxmlencode(fontSize) + "\"" //
		    + " />\n" //

		    + "    <screen" //
		    + " noTabs=\"" + noTabs + "\"" //
		    + " tabsAtTop=\"" + tabsAtTop + "\"" //
		    + " menuAtTab=\"" + menuAtTab + "\"" //
		    + " showstatus=\"" + showStatus + "\"" //
		    + " hasclosebutton=\"" + hasCloseButton + "\"" //
		    + " h=\"" + screenHeight + "\"" //
		    + " w=\"" + screenWidth + "\"" //
		    + " useText=\"" + useText + "\"" //
		    + " useIcons=\"" + useIcons + "\"" //
		    + " useBigIcons=\"" + useBigIcons + "\"" //
		    + " />\n" //

		    + "    <fixedsip state=\"" + SafeXML.strxmlencode(fixSIP) + "\" />\n" //

		    + "    <listview" //
		    + " colmap=\"" + SafeXML.string2Html(listColMap) + "\"" //
		    + " colwidths=\"" + SafeXML.string2Html(listColWidth) + "\"" //
		    + " />\n" //

		    + "    <travelbugs" //
		    + " colmap=\"" + SafeXML.string2Html(travelbugColMap) + "\"" //
		    + " colwidths=\"" + SafeXML.string2Html(travelbugColWidth) + "\"" //
		    + " shownonlogged=\"" + SafeXML.strxmlencode(travelbugShowOnlyNonLogged) + "\"" //
		    + " />\n");
	    outp.print("    <descpanel showimages=\"" + SafeXML.strxmlencode(descShowImg) + "\" />\n");
	    outp.print("    <imagepanel showdeletedimages=\"" + SafeXML.strxmlencode(showDeletedImages) + "\" />\n");
	    outp.print("    <hintlogpanel"//
		    + " logsperpage=\"" + SafeXML.strxmlencode(logsPerPage) + "\"" //
		    + " initialhintheight=\"" + SafeXML.strxmlencode(initialHintHeight) + "\"" //
		    + " maxspiderlogs=\"" + SafeXML.strxmlencode(maxLogsToSpider) + "\"" //
		    + " />\n");
	    outp.print("    <solver ignorevariablecase=\"" + SafeXML.strxmlencode(solverIgnoreCase) + "\" degMode=\"" + SafeXML.strxmlencode(solverDegMode) + "\" />\n");
	    outp.print("    <garmin" //
		    + " connection=\"" + SafeXML.string2Html(garminConn) + "\"" //
		    + " GPSBabelOptions=\"" + SafeXML.string2Html(garminGPSBabelOptions) + "\"" //
		    + " MaxWaypointLength=\"" + SafeXML.strxmlencode(garminMaxLen) + "\"" //
		    + " addDetailsToWaypoint=\"" + SafeXML.strxmlencode(addDetailsToWaypoint) + "\"" //
		    + " addDetailsToName=\"" + SafeXML.strxmlencode(addDetailsToName) + "\"" //
		    + " />\n");
	    outp.print("    <opencaching lastSite=\"" + lastOCSite + "\" downloadMissing=\"" + SafeXML.strxmlencode(downloadAllOC) + "\" />\n");
	    outp.print("    <location lat=\"" + SafeXML.string2Html(curCentrePt.getLatDeg(TransformCoordinates.DD)) + "\" long=\"" + SafeXML.string2Html(curCentrePt.getLonDeg(TransformCoordinates.DD)) + "\" />\n");
	    outp.print("    <spider" //
		    //+ " spiderUpdates=\"" + SafeXML.strxmlencode(spiderUpdates) + "\"" //
		    + " checkLog=\"" + SafeXML.strxmlencode(checkLog) + "\"" //
		    + " overwriteLogs=\"" + SafeXML.strxmlencode(overwriteLogs) + "\"" //
		    + " askForMaxNumbersOnImport=\"" + SafeXML.strxmlencode(askForMaxNumbersOnImport) + "\"" //
		    + " addPremiumGC=\"" + SafeXML.strxmlencode(addPremiumGC) + "\"" //
		    + " useGCFavoriteValue=\"" + SafeXML.strxmlencode(useGCFavoriteValue) + "\"" //		    
		    + " checkTBs=\"" + SafeXML.strxmlencode(checkTBs) + "\"" //
		    + " checkDTS=\"" + SafeXML.strxmlencode(checkDTS) + "\"" //
		    + " maxSpiderNumber=\"" + SafeXML.strxmlencode(maxSpiderNumber) + "\"" //
		    + " downloadPics=\"" + SafeXML.strxmlencode(downloadPics) + "\"" //
		    + " downloadTBs=\"" + SafeXML.strxmlencode(downloadTBs) + "\"" //
		    + " UserID=\"" + SafeXML.string2Html(userID) + "\"" //
		    + " getFinds=\"" + SafeXML.strxmlencode(!doNotGetFound) + "\"" //
		    + " />\n");
	    outp.print("    <gotopanel northcentered=\"" + SafeXML.strxmlencode(northCenteredGoto) + "\" />\n");
	    outp.print("    <details cacheSize=\"" + SafeXML.strxmlencode(maxDetails) + "\" delete=\"" + SafeXML.strxmlencode(deleteDetails) + "\" />\n");
	    outp.print("    <metric type=\"" + SafeXML.strxmlencode(metricSystem) + "\" />\n");
	    outp.print("    <datamover processorMode=\"" + SafeXML.strxmlencode(processorMode) + "\" />\n");
	    if (mapsBaseDir != null)
		outp.print("    <mapspath dir=\"" + SafeXML.string2Html(mapsBaseDir) + "\" />\n");
	    // Saving filters
	    String[] filterIDs = this.getFilterIDs();
	    for (int i = 0; i < filterIDs.length; i++) {
		outp.print(this.getFilter(filterIDs[i]).toXML(filterIDs[i]));
	    }
	    outp.print("    <debug value=\"" + SafeXML.strxmlencode(debug) + "\" />\n");
	    // save the exportValues of the gpxStyles
	    Iterator itGpxExportPreferences = gpxExportPreferences.entries();
	    while (itGpxExportPreferences.hasNext()) {
		MapEntry gpxExportPreference = (MapEntry) itGpxExportPreferences.next();
		String gpxStyle = (String) gpxExportPreference.getKey();
		Hashtable exportValues = (Hashtable) gpxExportPreference.getValue();
		Iterator itExportValues = exportValues.entries();
		outp.print("    <" + gpxStyle + " ");
		while (itExportValues.hasNext()) {
		    MapEntry entry = (MapEntry) itExportValues.next();
		    outp.print(entry.getKey().toString() + "=\"" + (String) entry.getValue() + "\" ");
		}
		outp.print("/>\n");
	    }
	    // save last path of im and exporters
	    exporterPaths.remove("key"); // relikt bereinigen
	    exporterPaths.remove("value"); // relikt bereinigen
	    Iterator itPath = exporterPaths.entries();
	    if (itPath.hasNext()) {
		outp.print("    <expPath ");
		while (itPath.hasNext()) {
		    MapEntry entry = (MapEntry) itPath.next();
		    outp.print(entry.getKey().toString() + "=\"" + (String) entry.getValue() + "\" ");
		}
		outp.print("/>\n");
	    }
	    importerPaths.remove("key"); // relikt bereinigen
	    importerPaths.remove("value"); // relikt bereinigen
	    itPath = importerPaths.entries();
	    if (itPath.hasNext()) {
		outp.print("    <impPath ");
		while (itPath.hasNext()) {
		    MapEntry entry = (MapEntry) itPath.next();
		    outp.print(entry.getKey().toString() + "=\"" + (String) entry.getValue() + "\" ");
		}
		outp.print("/>\n");
	    }
	    outp.print("    <logkeeping" //
		    + " maximum=\"" + SafeXML.strxmlencode(maxLogsToKeep) + "\"" //
		    + " keepown=\"" + SafeXML.strxmlencode(alwaysKeepOwnLogs) + "\"" //
		    + " keepTimeOnUpdate=\"" + SafeXML.strxmlencode(keepTimeOnUpdate) + "\"" //
		    + " />\n");
	    outp.print("    <fillWhiteArea on=\"" + SafeXML.strxmlencode(fillWhiteArea) + "\" />\n");
	    outp.print("    <mapLoader" //
		    + " overlapping=\"" + SafeXML.strxmlencode(mapOverlapping) + "\"" //
		    + " tilewidth=\"" + SafeXML.strxmlencode(tilewidth) + "\"" //
		    + " tileheight=\"" + SafeXML.strxmlencode(tileheight) + "\"" //
		    + " />\n");
	    String tmp = Common.DoubleToString(Double.parseDouble(Float.toString(lastScale)), 0, 2);
	    outp.print("    <Map" //
		    + " showCachesOnMap=\"" + SafeXML.strxmlencode(showCachesOnMap) + "\"" //
		    + " lastScale=\"" + tmp + "\"" //
		    + " />\n");
	    outp.print("    <SortingGroupedByCache on=\"" + SafeXML.strxmlencode(SortingGroupedByCache) + "\" />\n");
	    if (mobileGUI)
		// Keep the vmgui switch if it is set
		outp.print("    <MobileGui value=\"true\" />\n");
	    outp.print("</preferences>");
	    outp.close();
	    dirty = false;
	} catch (Exception e) {
	    log("Problem saving: " + pathToConfigFile, e, true);
	    dirty = true;
	}
    }

    void checkAbsoluteBaseDir() {
	// If datadir is empty, ask for one
	if (absoluteBaseDir.length() == 0 || !(new File(absoluteBaseDir)).exists()) {
	    do {
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, getHomeDir());
		fc.title = MyLocale.getMsg(170, "Select base directory for cache data");
		// If no base directory given, terminate
		if (fc.execute() == FormBase.IDCANCEL)
		    ewe.sys.Vm.exit(0);
		setBaseDir(fc.getChosenFile().toString());
	    } while (!(new File(absoluteBaseDir)).exists());
	}
    }

    /**
     * tries to get the home data dir of the user e.g. "c:\documents and...\<user>\my documents" or "/home/<user>" in linux if none could be identified, "/" is returned.
     * 
     * @return
     */
    private String getHomeDir() {
	String test;
	// returns in java-vm on win xp:
	// c:\<dokumente und Einstellungen>\<username>\<application data>
	test = Vm.getenv("HOMEDRIVE", "");
	log("[Preferences:getHomeDir]" + test);
	// this works also in win32.exe (ewe-vm on win xp)
	test += Vm.getenv("HOMEPATH", "");
	// returns in java-vm on win xp:
	// c:\<dokumente und Einstellungen>\<username>\<application data>
	log("[Preferences:getHomeDir]" + test);
	// this works also in win32.exe (ewe-vm on win xp)
	if (test.length() == 0)
	    // This should return on *nix system the home dir
	    test = Vm.getenv("HOME", "");
	if (test.length() == 0)
	    test = "/";
	return test;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // Log functions
    // ////////////////////////////////////////////////////////////////////////////////////

    // FIXME: should use path to config file instead of program directory

    /**
     * Method to delete an existing log file. Called on every SpiderGC. The log file is also cleared when Preferences is created and the filesize > 60KB
     */
    public void emptyLog() {
	File logFile = new File(LOGFILENAME);
	logFile.delete();
    }

    public void writeLogHeader() {
	log("CW Version " + Version.getReleaseDetailed(), null, true);
	if (System.getProperty("os.name") != null)
	    log("Operating system: " + System.getProperty("os.name") + "/" + System.getProperty("os.arch"), null, true);
	if (System.getProperty("java.vendor") != null)
	    log("Java: " + System.getProperty("java.vendor") + "/" + System.getProperty("java.version"), null, true);
    }

    boolean forceLog = false;

    /**
     * Method to log messages to a file called log.txt It will always append to an existing file. To show the message on the console, the global variable debug must be set. This can be done by adding
     * 
     * <pre>
     * &lt;debug value=&quot;true&quot;&gt;
     * </pre>
     * 
     * to the pref.xml file
     * 
     * @param text
     *            to log
     */
    public void log(String text) {
	if (debug || forceLog) {
	    if (debug)
		Vm.debug(text);
	    Time dtm = new Time();
	    dtm.getTime();
	    dtm.setFormat("dd.MM.yyyy'/'HH:mm:ss.SSS");
	    text = dtm.toString() + ": " + text;
	    FileWriter logFile = null;
	    try {
		logFile = new FileWriter(LOGFILENAME, true);
		// Stream strout = null;
		// strout = logFile.toWritableStream(true);
		logFile.print(text + NEWLINE);
	    } catch (Exception ex) {
		Vm.debug("Error writing to log file!");
	    } finally {
		if (logFile != null)
		    try {
			logFile.close();
		    } catch (IOException ioe) {
			// log("Ignored Exception", ioe, true);
		    }
	    }
	}
    }

    /**
     * Log an exception to the log file with or without a stack trace
     * 
     * @param text
     *            Optional message (Can be empty string)
     * @param e
     *            The exception
     * @param withStackTrace
     *            If true and the debug switch is true, the stack trace is appended to the log The debug switch can be set by including the line <i>&lt;debug value="true"&gt;&lt;/debug&gt;</i> in the pref.xml file or by manually setting it (i.e. in
     *            BE versions or RC versions) by including the line
     * 
     *            <pre>
     * Preferences.itself().debug = true;
     * </pre>
     * 
     *            in Version.getRelease()
     */
    public void log(String text, Throwable e, boolean withStackTrace) {
	if (e != null) {
	    text += Preferences.NEWLINE;
	    if (withStackTrace)
		text += ewe.sys.Vm.getAStackTrace(e);
	    else
		text += e.toString();
	}
	forceLog = true;
	log(text);
	forceLog = false;
    }

    /**
     * Log an exception to the log file without a stack trace, i.e. where a stack trace is not needed because the location/cause of the error is clear
     * 
     * @param message
     *            Optional message (Can be empty string)
     * @param e
     *            The exception
     */
    public void log(String message, Exception e) {
	log(message, e, false);
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // Exporter path functions
    // ////////////////////////////////////////////////////////////////////////////////////

    /** Hashtable for storing the last export path */
    private Hashtable exporterPaths = new Hashtable();

    public void setExportPath(String exporter, String path) {
	exporterPaths.put(exporter, path);
	savePreferences();
    }

    public void setExportPathFromFileName(String exporter, String filename) {
	File tmpfile = new File(filename);
	exporterPaths.put(exporter, tmpfile.getPath());
	savePreferences();
    }

    public String getExportPath(String exporter) {
	String dir = (String) exporterPaths.get(exporter);
	if (dir == null) {
	    dir = Preferences.itself().absoluteBaseDir;
	}
	return dir;
    }

    private Hashtable importerPaths = new Hashtable();

    public void setImporterPath(String importer, String directory) {
	importerPaths.put(importer, directory);
	savePreferences();
    }

    public String getImporterPath(String importer) {
	String dir = (String) importerPaths.get(importer);
	if (null == dir)
	    dir = Preferences.itself().absoluteBaseDir;
	return dir;
    }

    private Hashtable gpxExportPreferences = new Hashtable(4);

    public Hashtable getGpxExportPreferences(String gpxStyle) {
	Hashtable ret = (Hashtable) gpxExportPreferences.get(gpxStyle);
	if (ret == null) {
	    gpxExportPreferences.put(gpxStyle, new Hashtable());
	    ret = (Hashtable) gpxExportPreferences.get(gpxStyle);
	}
	return ret;
    }

    /**
     * <code>True</code> or <code>false</code>, depending if a filter with the given ID is saved in the preferences.
     * 
     * @param filterID
     *            ID of the filter to check
     * @return True or false
     */
    public boolean hasFilter(String filterID) {
	return this.filterList.containsKey(filterID);
    }

    /**
     * Returns the FilterData object saved with the given ID. The ID is not saved in the object, so it may be resaved under another ID.
     * 
     * @param filterID
     *            ID of the FilterData object
     * @return FilterData object
     */
    public FilterData getFilter(String filterID) {
	return (FilterData) this.filterList.get(filterID);
    }

    /**
     * Adds a FilterData object to the list. If a FilterData object is already saved unter the given ID, the old object is removed and the new one is set at its place.
     * 
     * @param filterID
     *            ID to associate with the filter object
     * @param filter
     *            FilterData object
     */
    public void addFilter(String filterID, FilterData filter) {
	this.filterList.put(filterID, filter);
    }

    /**
     * Removed the FilterData object which is saved with the given ID. If no such FilterData object exists, nothing happens.
     * 
     * @param filterID
     *            ID of FilterData object to remove
     */
    public void removeFilter(String filterID) {
	this.filterList.remove(filterID);
    }

    /**
     * Returns a alphabetically sorted array of ID of saved FilterData objects.
     * 
     * @return Array of IDs
     */
    public String[] getFilterIDs() {
	String[] result;
	result = new String[this.filterList.size()];
	Enumeration en = this.filterList.keys();
	int i = 0;
	while (en.hasMoreElements()) {
	    result[i++] = (String) en.nextElement();
	}
	// Now sorting the array of filter IDs
	Comparer comp = new ewe.util.Comparer() {
	    public int compare(Object o1, Object o2) {
		return ((String) o1).compareTo((String) o2);
	    }
	};
	Utils.sort(result, comp, false);
	return result;
    }

    public String gpsbabel = null;

    public void setgpsbabel() {
	try {
	    ewe.sys.Process p = Vm.exec("gpsbabel -V");
	    p.waitFor();
	    gpsbabel = "gpsbabel";
	} catch (IOException ioex) {
	    try {
		ewe.sys.Process p = Vm.exec("gpsbabel.exe -V");
		p.waitFor();
		gpsbabel = "gpsbabel.exe";
	    } catch (IOException io_ex) {
	    }
	}
    }

    /**
     * get directory where pref.xml is stored<br>
     * use this if you need a path where the user has sufficient rights to create a file
     */
    public String getPathToConfigFile() {
	return pathToConfigFile;
    }

    public void setBaseDir(String dir) {
	baseDir = dir;
	baseDir = baseDir.replace('\\', '/');
	if (!baseDir.endsWith("/"))
	    baseDir += "/";
	absoluteBaseDir = new File(baseDir).getAbsolutePath();
	absoluteBaseDir = absoluteBaseDir.replace('\\', '/');
	if (!absoluteBaseDir.endsWith("/"))
	    absoluteBaseDir += "/";
    }

    public void setMapsBaseDir(String dir) {
	mapsBaseDir = dir;
	mapsBaseDir = mapsBaseDir.replace('\\', '/');
	if (!mapsBaseDir.endsWith("/"))
	    mapsBaseDir += "/";
	absoluteMapsBaseDir = new File(mapsBaseDir).getAbsolutePath();
	absoluteMapsBaseDir = absoluteMapsBaseDir.replace('\\', '/');
	if (!absoluteMapsBaseDir.endsWith("/"))
	    absoluteMapsBaseDir += "/";
    }
}
