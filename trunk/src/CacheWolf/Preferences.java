package CacheWolf;

import CacheWolf.imp.SpiderGC;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
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
import ewe.ui.InputBox;
import ewe.ui.MessageBox;
import ewe.ui.Window;
import ewe.ui.WindowConstants;
import ewe.util.Comparer;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.StringTokenizer;
import ewe.util.Utils;
import ewe.util.Map.MapEntry;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 *	A class to hold the preferences that were loaded upon start up of CacheWolf.
 *	This class is also capable of parsing the prefs.xml file as well as
 *	saving the current settings of preferences.
 */
public class Preferences extends MinML{

	public final int DEFAULT_MAX_LOGS_TO_SPIDER=250;
	public final int DEFAULT_LOGS_PER_PAGE=5;
	public final int DEFAULT_INITIAL_HINT_HEIGHT=10;
	public final int DEFAULT_GPSD_PORT=2947;
	public static final int GPSD_DISABLED   = 0; // do not use gpsd
	public static final int GPSD_FORMAT_OLD = 1; // use old protocol
	public static final int GPSD_FORMAT_NEW = 2; // use new protocol (JSON)
	public static final int YES = 0;
	public static final int NO = 1;
	public static final int ASK = 2;
	private static String NEWLINE="\n";
	// Hashtable is saving filter data objects the user wants to save
	private Hashtable filterList = new Hashtable(15);
	/** screen is big enough to hold additional information like cache notes */
	public boolean isBigScreen;
	/** display big icons. default only true for VGA PDAs */
	// TODO: make this configurable via pref.xml
	public boolean useBigIcons;

	//////////////////////////////////////////////////////////////////////////////////////
    // Constructor
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Singleton pattern - return reference to Preferences
	 * @return Singleton Preferences object
	 */
	public static Preferences getPrefObject() {
		if (_reference == null)
			// it's ok, we can call this constructor
			_reference = new Preferences();
		return _reference;
	}

	private static Preferences _reference;

	private String pathToConfigFile;

	/**
	 * Call this method to set the path of the config file <br>
	 * If you call it with null it defaults to [program-dir]/pref.xml
	 * if p is a directory "pref.xml" will automatically appended
	 * @param p
	 */
	public void setPathToConfigFile(String p) {
		String p_;
		if (p == null) {
			/*
			String test;
			test = Vm.getenv("APPDATA", "/"); // returns in java-vm on win xp: c:\<dokumente und Einstellungen>\<username>\<application data>
			log("Vm.getenv(APPDATA: " + test); // this works also in win32.exe (ewe-vm on win xp)
			test = Vm.getenv("HOME", "/"); // This should return on *nix system the home dir
			log("Vm.getenv(HOME: " + test);
			test = System.getProperty("user.dir"); // return in java-vm on win xp: <working dir> or maybe <program dir>
			log("System.getProperty(user.dir: " + test); // in win32.exe -> null
			test = System.getProperty("user.home"); // in MS-java-VM env variable $HOME is ignored and always <windir>\java returned, see http://support.microsoft.com/kb/177181/en-us/
			log("System.getProperty(user.home: " + test); // in win32.exe -> null
			// "user.dir"              User's current working directory
			// "user.home"             User home directory (taken from http://scv.bu.edu/Doc/Java/tutorial/java/system/properties.html )
			 */
			p_ = FileBase.makePath(FileBase.getProgramDirectory(), "pref.xml");
		}
		else {
			if (new FileBugfix(p).isDirectory()) p_ = FileBase.makePath(p, "pref.xml");
			else p_ = p;
		}
		pathToConfigFile = STRreplace.replace(p_, "//", "/"); // this is necessary in case that the root dir is the dir where the pref.xml is stored
		pathToConfigFile = pathToConfigFile.replace('\\', '/');
		p=System.getProperty("os.name");
		if (p==null || p.indexOf("indows")!=-1) {
			NEWLINE = "\r\n";
		} 
	}

	/**
	 * Constructor is private for a singleton object
	 */
	private Preferences(){
		mySPO.bits = 8;
		mySPO.parity = SerialPort.NOPARITY;
		mySPO.stopBits = 1;
		mySPO.baudRate = 4800;
		if ( ((ewe.fx.Rect) (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new ewe.fx.Rect(),0))).height > 400) {
			if (Vm.getPlatform().equals("Unix"))
				fontSize = 12;
			else{
				// Default on VGA-PDAs: fontSize 21 + adjust ColWidth
				if (Vm.isMobile()){
					fontSize = 21;
					listColWidth="20,20,30,30,92,177,144,83,60,105,50,104,22,30,30";
				}
				else
					fontSize = 16;
			}
		} else
			fontSize = 11;
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // Public fields stored in pref.xml
	//////////////////////////////////////////////////////////////////////////////////////

	/** The base directory contains one subdirectory for each profile*/
	private String baseDir = "";
	public String absoluteBaseDir = "";
	/** Name of last used profile */
	public String lastProfile="";
	/** If true, the last profile is reloaded automatically without a dialogue */
	public boolean autoReloadLastProfile=false;
	/** If true current cetre will be set from gps position	 */
	public boolean setCurrentCentreFromGPSPosition=true;
	/** This is the login alias for geocaching.com and opencaching.de */
	public String myAlias = "";
	/** Optional password */
	public String password="";
	/** This is an alternative alias used to identify found caches (i.e. if using multiple IDs) */
	public String myAlias2 = "";
	/** The path to the browser */
	public String browser = "";
	/** Name of HTTP proxy for spidering */
	public String myproxy = "";
	/** HTTP proxy port when spidering */
	public String myproxyport = "";
	/** Flag whether proxy is to be used */
	public boolean proxyActive=false;
	/** Serial port name and baudrate */
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
	/** The default font size */
	public int fontSize = 11;
	// These settings govern where the menu and the tabs are displayed and whether the statusbas is shown
	/** True if the menu is to be displayed at the top of the screen */
	public boolean menuAtTop=true;
	/** True if the tabs are to be displayed at the top of the screen */
	public boolean tabsAtTop=true;
	/** True if the status bar is to be displayed (hidden if false) */
	public boolean showStatus=true;
	//public boolean noTabs=false;
	/** True if the application can be closed by clicking on the close button in the top line.
	 * This can be set to avoid accidental closing of the application */
	public boolean hasCloseButton=true;
	/** True if the SIP is always visible */
	public boolean fixSIP = false;
	/** The list of visible columns in the list view */
	public String listColMap="0,1,2,3,4,5,6,7,8,9,10,11,12";
	/** The widths for each column in list view */
	public String listColWidth="15,20,20,25,92,177,144,83,60,105,50,104,22,30,30,30,30,30,30,30";
	/** The columns which are to be displayed in TravelbugsJourneyScreen. See also TravelbugJourney */
	public String travelbugColMap="1,4,5,6,8,9,10,7";
	/** The column widths for the travelbug journeys. */
	public String travelbugColWidth="212,136,62,90,50,56,90,38,50,50,94,50";
	/** If this flag is true, only non-logged travelbug journeys will be shown */
	public boolean travelbugShowOnlyNonLogged=false;
	/** If this is true, deleted images are shown with a ? in the imagepanel */
	public boolean showDeletedImages=true;
	/** This setting determines how many logs are shown per page of hintlogs (default 5) */
	public int logsPerPage=DEFAULT_LOGS_PER_PAGE;
	/** Initial height of hints field (set to 0 to hide them initially) */
	public int initialHintHeight=DEFAULT_INITIAL_HINT_HEIGHT;
	/** Maximum logs to spider */
	public int maxLogsToSpider = DEFAULT_MAX_LOGS_TO_SPIDER;
	/** True if the Solver should ignore the case of variables */
	public boolean solverIgnoreCase=true;
	/** True if the solver expects arguments for trigonometric functions in degrees */
	public boolean solverDegMode=true;
	/** True if the description panel should show images */
	public boolean descShowImg=true;
	/** The type of connection which GPSBABEL uses: com1 OR usb. */
	public String garminConn="com1";
	/** Additional options for GPSBabel, i.e. -s to synthethise short names */
	public String garminGPSBabelOptions="";
	/** Max. length for Garmin waypoint names (for etrex which can only accept 6 chars) */
	public int garminMaxLen=0;
	public boolean downloadMissingOC = false;
	public String lastOCSite=OC.OCSites[0][OC.OC_HOSTNAME];
	/** The currently used centre point, can be different from the profile's centrepoint. This is used
	 *  for spidering */
	private CWPoint curCentrePt=new CWPoint();
	/** True if a login screen is displayed on each spider operation */
	public boolean forceLogin=true;
	/** True if the goto panel is North centered */
	public boolean northCenteredGoto = true;
	/** If not null, a customs map path has been specified by the user */
	private String customMapsPath=null;
	/** Number of CacheHolder details that are kept in memory */
	public int maxDetails=50;
	/** Number of details to delete when maxDetails have been stored in cachesWithLoadedDetails */
	public int deleteDetails=5;
	/** The locale code (DE, EN, ...) */
	public String language="";
	/** The metric system to use */
	public int metricSystem = Metrics.METRIC;
	/** Load updated caches while spidering */
	public int spiderUpdates = ASK;
	/** Maximum number of new caches to spider */
	public int maxSpiderNumber = 200;
	/** Add short details to waypoint on export */
	public boolean addDetailsToWaypoint = false;
	/** Add short details to name on export */
	public boolean addDetailsToName = false;
	/** The own GC member ID */
	public String gcMemberId = "";
	/** Premium Member ? */
	public boolean isPremium=true;
	/** The maximum number of logs to export */
	public int numberOfLogsToExport = 5;
	/** Add Travelbugs when exporting */
	public boolean exportTravelbugs = false;
	/** Try to make a MyFinds GPX when exporting to GPX */
	public boolean exportGpxAsMyFinds = true;
	/** Check if lastFound is newer than saved log*/
	public boolean checkLog=false;
	/** Download images when loading cache data */
	public boolean downloadPics = true;
	/** Download TB information when loading cache data */
	public boolean downloadTBs = true;
	/** Last mode select in the DataMover for processing cache*/
	public int processorMode = 0;
	/** external Cacherating software */
	public String rater;
	/** maximum number of logs to store in cache details */
	public int maxLogsToKeep = DEFAULT_MAX_LOGS_TO_SPIDER;
	/** keep own logs even when excessing <code>maxLogsToKeep</code> */
	public boolean alwaysKeepOwnLogs = true;

	/** Determines whether to fill the white areas on the map */
	public boolean fillWhiteArea=false;

    /** Selected Size of map tiles */
    public int mapTileSize=1;
    /** How many should maptiles overlap */
    public int mapOverlapping=100;
    /** Width and height of free defined tile size */
    public int tilewidth;
    public int tileheight;
    
    /** ShowCachesOnMap */
    public boolean showCachesOnMap=true;

    /** SortingGroupedByCache */
    public boolean SortingGroupedByCache=true;

    /** useOwnSymbols */
    public boolean useOwnSymbols=true;

    /** TRUE if we want automatic sorting **/
    public boolean sortAutomatic = true;

	//////////////////////////////////////////////
	/** The debug switch (Can be used to activate dormant code) by adding
	 * the line: <pre><debug value="true" /></pre>
	 * to the pref.xml file.
	 */
	public boolean debug = false;
	//////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////
    // Public fields not stored in pref.xml
	//////////////////////////////////////////////////////////////////////////////////////

	/** The height of the application */
	public int myAppHeight = 600;
	/** The width of the application */
	public int myAppWidth = 800;
	/** True if the preferences were changed and need to be saved */
	public boolean dirty = false;

	private String pathToProfile;

    //////////////////////////////////////////////////////////////////////////////////////
    // Read pref.xml file
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to open and parse the config file (pref.xml). Results are stored in the
	 * public variables of this class.
	 * If you want to specify a non default config file call setPathToConfigFile() first.
	 */
	public void readPrefFile(){
		if (pathToConfigFile == null) setPathToConfigFile(null); // this sets the default value
		try{
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(pathToConfigFile));
			parse(r);
			r.close();
		}catch(IOException e){
			log("IOException reading config file: " + pathToConfigFile, e, true);
			browser = getDefaultBrowser();
			(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(176, "First start - using default preferences \n For experts only: \n Could not read preferences file:\n") + pathToConfigFile, FormBase.OKB)).execute();
		}catch(Exception e){
			if (e instanceof NullPointerException)
				log("Error reading pref.xml: NullPointerException in Element "+lastName +". Wrong attribute?",e,true);
			else
				log("Error reading pref.xml: ", e);
		}
		useBigIcons = Vm.isMobile() && MyLocale.getScreenWidth() >= 400;
		isBigScreen = (MyLocale.getScreenWidth() >= 400) && (MyLocale.getScreenHeight() >= 600);
	}

/**
 * Tries to find a executable browser
 * return "" if no browser found
 * @return
 */
	private String getDefaultBrowser() {
		String pf = Vm.getPlatform();
		String testlist[] = null;
		if (pf.equals("Java") || pf.equals("Win32")) {
			String progdir = Vm.getenv("ProgramFiles", null); // at least in java-Win XP this is set
			String homedir=Vm.getenv("HOMEPATH", "");
			if (progdir != null) {
				String test[] = {
						homedir+"/Lokale Einstellungen/Anwendungsdaten/Google/Chrome/Application/chrome.exe",
						progdir+"/Firefox/firefox.exe",
						progdir+"/Opera/opera.exe",
						progdir+"/Internet Explorer/iexplore.exe"};
				testlist = test;
			} else {
				String test[] = { // this part is not tested
						"/opt/firefox/firefox", // default path in ubuntu
						"/usr/bin/firefox"};
				testlist = test;
			}
		}
		if (pf.equals("WinCE")) {
			String test[] = {"/windows/iexplore.exe"};
			testlist = test;
		}
		if (testlist != null) {
			for (int i=0; i < testlist.length; i++)
				if ((new FileBugfix(testlist[i])).exists()) {
					return testlist[i];
				}
		}
		return "";
	}

	/** Helper variables for XML parser */
	private StringBuffer collectElement=null;
	private String lastName; // The string to the last XML that was processed
	private long getLongAttr(AttributeList atts, String name) {
		String stmp=atts.getValue(name);
		long ret = 0l;
		if (stmp != null) {
			ret = Convert.parseLong(stmp);
		}
		return ret;
	}

	/**
	 * Method that gets called when a new element has been identified in pref.xml
	 */
	public void startElement(String name, AttributeList atts){
		//Vm.debug("name = "+name);
		lastName=name;
		String tmp;
		if(name.equals("browser")) {
			browser = atts.getValue("name");
			if (browser == null || browser.length() == 0) browser = getDefaultBrowser();
		}
		else if(name.equals("fixedsip")) {
			if(atts.getValue("state").equals("true")) {
				fixSIP = true;
			}
		}
		else if(name.equals("font")) fontSize = Convert.toInt(atts.getValue("size"));
		else if(name.equals("alias")) {
			myAlias = SafeXML.cleanback(atts.getValue("name"));
			tmp = SafeXML.cleanback(atts.getValue("password"));
			if (tmp != null) password=tmp;
			SpiderGC.passwort=password;
		}
		else if(name.equals("alias2")) myAlias2 = SafeXML.cleanback(atts.getValue("name"));
		else if(name.equals("gcmemberid")) {
			gcMemberId = atts.getValue("name");
			tmp=atts.getValue("Premium");
			if (tmp != null) isPremium=Boolean.valueOf(tmp).booleanValue();
		}
		else if(name.equals("location")){
			curCentrePt.set(atts.getValue("lat")+" "+atts.getValue("long"));
		}
		else if(name.equals("port")){
			mySPO.portName = atts.getValue("portname");
			mySPO.baudRate = Convert.toInt(atts.getValue("baud"));
		}
		else if(name.equals("portforward")) {
			forwardGPS = Convert.toBoolean(atts.getValue("active"));
			forwardGpsHost = atts.getValue("destinationHost");
		}
		else if(name.equals("gpsd")) {
			useGPSD = Convert.toInt(atts.getValue("active"));
			gpsdHost = atts.getValue("host");
			gpsdPort = Convert.toInt(atts.getValue("port"));
		}
		else if(name.equals("portlog")) {
			logGPS = Convert.toBoolean(atts.getValue("active"));
			logGPSTimer = atts.getValue("logTimer");
		}
		else if (name.equals("lastprofile")) {
			collectElement=new StringBuffer(50);
			if (atts.getValue("autoreload").equals("true")) autoReloadLastProfile=true;
		}
		else if (name.equals("CurrentCentre")) {
			setCurrentCentreFromGPSPosition=Boolean.valueOf(atts.getValue("FromGPSPosition")).booleanValue();
		}
		else if(name.equals("basedir")) {
			setBaseDir(atts.getValue("dir"));
		}
		else if (name.equals("opencaching")) {
			tmp=atts.getValue("lastSite");
			if (!(tmp == null) && OC.getSiteIndex(tmp)>=0 ) lastOCSite=tmp;
			tmp=atts.getValue("downloadMissing");
			if (!(tmp == null)) downloadMissingOC = Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("listview")) {
			listColMap=atts.getValue("colmap");
			listColWidth=atts.getValue("colwidths");
			while ((new StringTokenizer(listColWidth,",")).countTokens()<myTableModel.N_COLUMNS) listColWidth+=",30"; // for older versions
		}
		else if(name.equals("proxy")) {
			myproxy = atts.getValue("prx");
			myproxyport = atts.getValue("prt");
			tmp = atts.getValue("active");
			if (tmp != null) proxyActive=Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("garmin")) {
			garminConn=atts.getValue("connection");
			tmp = atts.getValue("GPSBabelOptions");
			if (tmp != null) garminGPSBabelOptions=tmp;
			tmp = atts.getValue("MaxWaypointLength");
			if (tmp != null) garminMaxLen=Convert.toInt(tmp);
			tmp = atts.getValue("addDetailsToWaypoint");
			if (tmp != null) addDetailsToWaypoint = Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("addDetailsToName");
			if (tmp != null) addDetailsToName = Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("imagepanel")) {
			showDeletedImages = Boolean.valueOf(atts.getValue("showdeletedimages")).booleanValue();
		}
		else if (name.equals("descpanel")) {
			descShowImg = Boolean.valueOf(atts.getValue("showimages")).booleanValue();
		}
		else if (name.equals("screen")) {
			menuAtTop=Boolean.valueOf(atts.getValue("menuattop")).booleanValue();
			tabsAtTop=Boolean.valueOf(atts.getValue("tabsattop")).booleanValue();
			showStatus=Boolean.valueOf(atts.getValue("showstatus")).booleanValue();
			if (atts.getValue("hasclosebutton")!=null)
				hasCloseButton=Boolean.valueOf(atts.getValue("hasclosebutton")).booleanValue();
			if (atts.getValue("h")!=null) {
				myAppHeight=Convert.toInt(atts.getValue("h"));
				myAppWidth=Convert.toInt(atts.getValue("w"));
			}
		}
		else if (name.equals("hintlogpanel")) {
			logsPerPage = Convert.parseInt(atts.getValue("logsperpage"));
			String strInitialHintHeight=atts.getValue("initialhintheight");
			if (strInitialHintHeight!=null) initialHintHeight=Convert.parseInt(strInitialHintHeight);
			String strMaxLogsToSpider=atts.getValue("maxspiderlogs");
			if (strMaxLogsToSpider!=null) maxLogsToSpider=Convert.parseInt(strMaxLogsToSpider);
		}
		else if (name.equals("solver")) {
			solverIgnoreCase=Boolean.valueOf(atts.getValue("ignorevariablecase")).booleanValue();
			tmp = atts.getValue("degMode");
			if (tmp != null) solverDegMode=Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("mapspath")) {
			customMapsPath=SafeXML.cleanback(atts.getValue("dir")).replace('\\', '/');
		}
		else if (name.equals("debug")) debug=Boolean.valueOf(atts.getValue("value")).booleanValue();

		else if (name.equals("expPath")){
			exporterPaths.put(atts.getValue("key"),atts.getValue("value"));
		}
		else if (name.equals("impPath")) {
			importerPaths.put(atts.getValue("key"), atts.getValue("value"));
		}
		else if (name.equals("travelbugs")) {
			travelbugColMap=atts.getValue("colmap");
			travelbugColWidth=atts.getValue("colwidths");
			travelbugShowOnlyNonLogged=Boolean.valueOf(atts.getValue("shownonlogged")).booleanValue();
		}
		else if (name.equals("gotopanel")) {
			northCenteredGoto = Boolean.valueOf(atts.getValue("northcentered")).booleanValue();
		}
		else if (name.equals("spider")) {
			forceLogin = Boolean.valueOf(atts.getValue("forcelogin")).booleanValue();
			checkLog = Boolean.valueOf(atts.getValue("checkLog")).booleanValue();
			tmp = atts.getValue("spiderUpdates");
			if (tmp != null) spiderUpdates=Convert.parseInt(tmp);
			tmp = atts.getValue("maxSpiderNumber");
			if (tmp != null) maxSpiderNumber=Convert.parseInt(tmp);
			tmp = atts.getValue("downloadPics");
			if (tmp != null) downloadPics=Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("downloadTBs");
			if (tmp != null) downloadTBs=Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("details")) {
			maxDetails=Common.parseInt(atts.getValue("cacheSize"));
			deleteDetails=Common.parseInt(atts.getValue("delete"));
			if (maxDetails<2) maxDetails=2;
			if (deleteDetails<1) deleteDetails=1;
		}
		else if (name.equals("metric")) {
			metricSystem=Common.parseInt(atts.getValue("type"));
			if (metricSystem != Metrics.METRIC &&
					metricSystem != Metrics.IMPERIAL) {
				metricSystem = Metrics.METRIC;
			}
		}
		else if (name.equals("export")) {
			tmp = atts.getValue("numberOfLogsToExport");
			if (tmp != null) numberOfLogsToExport=Convert.parseInt(tmp);
			tmp = atts.getValue("exportTravelbugs");
			if (tmp != null) exportTravelbugs = Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("exportGpxAsMyFinds");
			if (tmp != null) exportGpxAsMyFinds = Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("locale")) {
			language = atts.getValue("language");
		}
		else if (name.equals("rater")) {
			rater = SafeXML.cleanback(atts.getValue("tool"));
		}
		else if (name.equals("FILTERDATA")) {
			// Creating a filter object and reading the saved data
			String id = SafeXML.cleanback(atts.getValue("id"));
			FilterData data = new FilterData();
			data.setFilterRose(atts.getValue("rose"));
			data.setFilterType(atts.getValue("type"));
			data.setFilterVar(atts.getValue("var"));
			data.setFilterDist(atts.getValue("dist"));
			data.setFilterDiff(atts.getValue("diff"));
			data.setFilterTerr(atts.getValue("terr"));
			data.setFilterSize(atts.getValue("size"));
			long[] filterAttr = { 0l,0l,0l,0l };
			filterAttr[0] = getLongAttr(atts, "attributesYes");
			filterAttr[1] = getLongAttr(atts, "attributesYes1");
			filterAttr[2] = getLongAttr(atts, "attributesNo");
			filterAttr[3] = getLongAttr(atts, "attributesNo1");
			data.setFilterAttr(filterAttr);
			data.setFilterAttrChoice(Convert.parseInt(atts.getValue("attributesChoice")));
			data.setFilterStatus(SafeXML.cleanback(atts.getValue("status")));
			data.setUseRegexp(Boolean.valueOf(atts.getValue("useRegexp")).booleanValue());
			tmp = atts.getValue("noCoord");
			if (tmp != null) {
			  data.setFilterNoCoord(Boolean.valueOf(tmp).booleanValue());
      } else {
			  data.setFilterNoCoord(true);
      }
			// Filter object is remembered under the given ID
			this.addFilter(id, data);
		}
		else if (name.equals ("datamover")){
			tmp = atts.getValue("processorMode");
			if (tmp != null){
				processorMode=Convert.parseInt(tmp);
			}
		}
		else if (name.equals("logkeeping")) {
			tmp = atts.getValue("maximum");
			if (tmp != null)
				maxLogsToKeep = Convert.parseInt(tmp);
			if (maxLogsToKeep < 0) maxLogsToKeep = 0;

			tmp = atts.getValue("keepown");
			if (tmp != null)
				alwaysKeepOwnLogs = Boolean.valueOf(tmp).booleanValue();
		}
		else if (name.equals("fillWhiteArea")){
			tmp = atts.getValue("on");
			fillWhiteArea = tmp != null && tmp.equalsIgnoreCase("true");
		}
		else if (name.equals("mapLoader")){
			     tmp = atts.getValue("tileSize");
			     if (tmp == null || tmp.length() == 0) tmp = "1";
			     mapTileSize = Convert.parseInt(tmp);
                 tmp = atts.getValue("overlapping");
			     if (tmp == null || tmp.length() == 0) tmp = "100";
			     mapOverlapping = Convert.parseInt(tmp);
			     tmp = atts.getValue("tilewidth");
			     tilewidth= (tmp != null && tmp.length() > 0)?Convert.parseInt(tmp):0;
			     tmp = atts.getValue("tileheight");
			     tileheight= (tmp != null && tmp.length() > 0)?Convert.parseInt(tmp):0;			    	 
		}
		else if (name.equals("showCachesOnMap")){
			tmp = atts.getValue("on");
			showCachesOnMap = tmp != null && tmp.equalsIgnoreCase("true");
		}
		else if (name.equals("SortingGroupedByCache")){
			tmp = atts.getValue("on");
			SortingGroupedByCache = tmp != null && tmp.equalsIgnoreCase("true");
		}
		else if (name.equals("Symbols")) {
			useOwnSymbols=Boolean.valueOf(atts.getValue("useOwnSymbols")).booleanValue();
		}
	}

	public void characters( char ch[], int start, int length ) {
		if (collectElement!=null) {
			collectElement.append(ch,start,length); // Collect the name of the last profile
		}
	}

	/**
	 * Method that gets called when the end of an element has been identified in pref.xml
	 */
	public void endElement(String tag){
		if (tag.equals("lastprofile")) {
			if (collectElement!=null) lastProfile=collectElement.toString();
		}
		collectElement=null;
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // Write pref.xml file
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to save current preferences in the pref.xml file
	 */
	public void savePreferences(){
		if (pathToConfigFile == null) setPathToConfigFile(null); // this sets the default value
		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(pathToConfigFile)));
			outp.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			outp.print("<preferences>\n");
			outp.print("    <locale language=\"" + SafeXML.clean(language) + "\"/>\n");
			outp.print("    <basedir dir = \"" + SafeXML.clean(getBaseDir()) + "\"/>\n");
			outp.print("    <lastprofile autoreload=\"" + SafeXML.strxmlencode(autoReloadLastProfile) + "\">" + SafeXML.clean(lastProfile) + "</lastprofile>\n"); //RB
			outp.print("    <CurrentCentre FromGPSPosition=\"" + SafeXML.clean(Convert.toString(setCurrentCentreFromGPSPosition)) + "\"/>\n");
			outp.print("    <alias name =\""+ SafeXML.clean(myAlias) +"\" password=\""+SafeXML.clean(password)+"\" />\n");
			outp.print("    <alias2 name =\""+ SafeXML.clean(myAlias2) +"\"/>\n");
			outp.print("    <gcmemberid name =\""+ SafeXML.clean(gcMemberId) + "\"" + " Premium =\""+ SafeXML.strxmlencode(isPremium) +"\"/>\n");
			outp.print("    <browser name = \"" + SafeXML.clean(browser) + "\"/>\n");
			outp.print("    <proxy prx = \"" + SafeXML.clean(myproxy) + "\" prt = \"" + SafeXML.clean(myproxyport) + "\" active = \"" + SafeXML.strxmlencode(proxyActive) + "\" />\n");
			outp.print("    <port portname = \"" + SafeXML.clean(mySPO.portName) + "\" baud = \"" + SafeXML.strxmlencode(mySPO.baudRate) + "\"/>\n");
			outp.print("    <portforward active= \"" + SafeXML.clean(Convert.toString(forwardGPS)) + "\" destinationHost = \"" + SafeXML.clean(forwardGpsHost) + "\"/>\n");
			outp.print("    <gpsd active= \"" + SafeXML.strxmlencode(useGPSD) + "\" host = \"" + SafeXML.clean(gpsdHost) + "\" port = \"" + SafeXML.strxmlencode(gpsdPort) + "\"/>\n");
			outp.print("    <portlog active= \"" + SafeXML.clean(Convert.toString(logGPS)) + "\" logTimer = \"" + SafeXML.clean(logGPSTimer) + "\"/>\n");
			outp.print("    <font size =\"" + SafeXML.strxmlencode(fontSize) + "\"/>\n");
			outp.print("    <screen menuattop=\""+menuAtTop+"\" tabsattop=\""+tabsAtTop+"\" showstatus=\""+showStatus+"\" hasclosebutton=\""+hasCloseButton+
	                "\" h=\""+myAppHeight+"\" w=\""+myAppWidth+"\" />\n");
			outp.print("    <fixedsip state = \"" + SafeXML.strxmlencode(fixSIP) + "\"/>\n");
			outp.print("    <listview colmap=\"" + SafeXML.clean(listColMap) + "\" colwidths=\"" + SafeXML.clean(listColWidth) + "\" />\n");
			outp.print("    <travelbugs colmap=\"" + SafeXML.clean(travelbugColMap) + "\" colwidths=\"" + SafeXML.clean(travelbugColWidth) + "\" shownonlogged=\"" + SafeXML.strxmlencode(travelbugShowOnlyNonLogged) + "\" />\n");
			outp.print("    <descpanel showimages=\"" + SafeXML.strxmlencode(descShowImg) + "\" />\n");
			outp.print("    <imagepanel showdeletedimages=\"" + SafeXML.strxmlencode(showDeletedImages) + "\"/>\n");
			outp.print("    <hintlogpanel logsperpage=\"" + SafeXML.strxmlencode(logsPerPage) + "\" initialhintheight=\"" + SafeXML.strxmlencode(initialHintHeight) + "\"  maxspiderlogs=\"" + SafeXML.strxmlencode(maxLogsToSpider) + "\" />\n");
			outp.print("    <solver ignorevariablecase=\"" + SafeXML.strxmlencode(solverIgnoreCase) + "\" degMode=\"" + SafeXML.strxmlencode(solverDegMode) + "\" />\n");
			outp.print("    <garmin connection = \"" + SafeXML.clean(garminConn) + "\" GPSBabelOptions = \"" + SafeXML.clean(garminGPSBabelOptions) + "\" MaxWaypointLength = \"" + SafeXML.strxmlencode(garminMaxLen) +
					        "\" addDetailsToWaypoint = \"" + SafeXML.strxmlencode(addDetailsToWaypoint) + "\" addDetailsToName = \"" + SafeXML.strxmlencode(addDetailsToName) + "\" />\n");
			outp.print("    <opencaching lastSite=\""+lastOCSite+"\" downloadMissing=\"" + SafeXML.strxmlencode(downloadMissingOC) + "\"/>\n");
			outp.print("    <location lat = \"" + SafeXML.clean(curCentrePt.getLatDeg(TransformCoordinates.DD)) + "\" long = \"" + SafeXML.clean(curCentrePt.getLonDeg(TransformCoordinates.DD)) + "\"/>\n");
			outp.print("    <spider forcelogin=\"" + SafeXML.strxmlencode(forceLogin) + "\" spiderUpdates=\"" + SafeXML.strxmlencode(spiderUpdates) + "\" checkLog=\"" + SafeXML.strxmlencode(checkLog) + "\" maxSpiderNumber=\"" + SafeXML.strxmlencode(maxSpiderNumber) + "\" downloadPics=\"" + SafeXML.strxmlencode(downloadPics) + "\" downloadTBs=\"" + SafeXML.strxmlencode(downloadTBs) +"\"/>\n");
			outp.print("    <gotopanel northcentered=\"" + SafeXML.strxmlencode(northCenteredGoto) + "\" />\n");
			outp.print("    <details cacheSize=\"" + SafeXML.strxmlencode(maxDetails) + "\" delete=\"" + SafeXML.strxmlencode(deleteDetails) + "\"/>\n");
			outp.print("    <metric type=\"" + SafeXML.strxmlencode(metricSystem) + "\"/>\n");
			outp.print("    <export numberOfLogsToExport=\"" + SafeXML.strxmlencode(numberOfLogsToExport) + "\" exportTravelbugs=\"" + SafeXML.strxmlencode(exportTravelbugs) + "\" exportGpxAsMyFinds=\"" + SafeXML.strxmlencode(exportGpxAsMyFinds) + "\"/>\n");
			outp.print("    <datamover processorMode=\"" + SafeXML.strxmlencode(processorMode) + "\" />\n");
			if (customMapsPath!=null) outp.print("    <mapspath dir = \"" + SafeXML.clean(customMapsPath.replace('\\','/')) + "\"/>\n");
			// Saving filters
			String[] filterIDs = this.getFilterIDs();
			for (int i=0; i<filterIDs.length; i++){
				outp.print(this.getFilter(filterIDs[i]).toXML(filterIDs[i]));
			}
			if (debug) outp.print("    <debug value=\"true\" />\n"); // Keep the debug switch if it is set
			// save last path of different exporters
			Iterator itPath = exporterPaths.entries();
			MapEntry entry;
			while(itPath.hasNext()){
				entry = (MapEntry) itPath.next();
				outp.print("    <expPath key = \"" + SafeXML.clean(entry.getKey().toString()) + "\" value = \"" + SafeXML.clean(entry.getValue().toString().replace('\\', '/')) + "\"/>\n");
			}
			itPath = importerPaths.entries();
			while(itPath.hasNext()){
				entry = (MapEntry) itPath.next();
				outp.print("    <impPath key = \"" + SafeXML.clean(entry.getKey().toString()) + "\" value = \"" + SafeXML.clean(entry.getValue().toString().replace('\\', '/')) + "\"/>\n");
			}
			if (rater != null) outp.print("    <rater tool=\"".concat(SafeXML.clean(rater)).concat("\"/>\n"));
			outp.print("    <logkeeping maximum=\""+SafeXML.strxmlencode(maxLogsToKeep)+"\" keepown=\""+SafeXML.strxmlencode(alwaysKeepOwnLogs)+"\" />\n");
			outp.print("    <fillWhiteArea on=\""+SafeXML.strxmlencode(fillWhiteArea)+"\"/>\n");
			outp.print("    <mapLoader tileSize=\""+SafeXML.strxmlencode(mapTileSize)+"\" overlapping=\""+SafeXML.strxmlencode(mapOverlapping)+"\" tilewidth=\""+SafeXML.strxmlencode(tilewidth)+"\" tileheight=\""+SafeXML.strxmlencode(tileheight)+"\"/>\n");
			outp.print("    <showCachesOnMap on=\""+SafeXML.strxmlencode(showCachesOnMap)+"\"/>\n");
			outp.print("    <SortingGroupedByCache on=\""+SafeXML.strxmlencode(SortingGroupedByCache)+"\"/>\n");
			outp.print("    <Symbols useOwnSymbols=\"" + SafeXML.strxmlencode(useOwnSymbols) + "\"/>\n");
			outp.print("</preferences>");
			outp.close();
		} catch (Exception e) {
			log("Problem saving: " +pathToConfigFile,e,true);
		}
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // Maps
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String mapsPath = "maps/standard";

	/**
	 * custom = set by the user
	 * @return custom Maps Path, null if not set
	 */
	public String getCustomMapsPath() {
	   String rCMP = Global.getProfile().getRelativeCustomMapsPath();
	   if (rCMP.equals("")) {
		   return customMapsPath;
		} else {
			return absoluteBaseDir+"maps"+rCMP;
		}
	}

	public void saveCustomMapsPath(String mapspath_) {
		if (customMapsPath == null || !customMapsPath.equals(mapspath_)) {
			customMapsPath=new String(mapspath_).replace('\\', '/');
			savePreferences();
			String s = absoluteBaseDir+"maps";
			if (customMapsPath.indexOf(s)==0) {
				String t=customMapsPath.substring(s.length(), customMapsPath.length());
				Global.getProfile().setRelativeCustomMapsPath(t);
			} else {
				Global.getProfile().setRelativeCustomMapsPath("");
			}
		}
	}

	private myTableModel tableMod;
	public void setMyTableModel(myTableModel myTableModel) {tableMod=myTableModel;}
	public CWPoint getCurCentrePt() {return curCentrePt;}
	public void setCurCentrePt(CWPoint newCentre) {
		Vm.showWait(true);
		curCentrePt.set(newCentre);
		Global.getProfile().updateBearingDistance();
		if (tableMod != null) {
			// corresponding column for "distance" is column 10
			if (tableMod.sortedBy == 10) tableMod.isSorted = false;
			if (tableMod.sortedBy == 10 && this.sortAutomatic) {
				tableMod.sortTable(tableMod.sortedBy, tableMod.sortAscending);
				// or ??? TablePanel.refreshTable();
			}
			else tableMod.tcControl.repaint();
		}
		Vm.showWait(false);
	}

	/**
	 * gets the path to the calibrated maps
	 * it first tries if there are manually imported maps
	 * in <baseDir>/maps/standard then it tries
	 * the legacy dir: <program-dir>/maps
	 * In case in both locations are no .wfl-files
	 * it returns  <baseDir>/maps/expedia - the place where
	 * the automatically downloaded maps are placed.
	 *
	 *
	 */
	public String getMapLoadPath() {
		saveCustomMapsPath(getMapLoadPathInternal());
		return getCustomMapsPath();
	}

	private String getMapLoadPathInternal() {
		// here could also a list of map-types displayed...
		// standard dir
		String ret = getCustomMapsPath();
		if (ret != null) return ret;
		ret = getMapManuallySavePath(false);
		File t = new FileBugfix(ret);
		String[] f = t.list("*.wfl", FileBase.LIST_FILES_ONLY);
		if (f != null && f.length > 0) return  absoluteBaseDir + mapsPath;
		f = t.list("*.wfl", FileBase.LIST_DIRECTORIES_ONLY | FileBase.LIST_ALWAYS_INCLUDE_DIRECTORIES);
		if (f != null && f.length > 0) { // see if in a subdir of <baseDir>/maps/standard are .wfl files
			String[] f2;
			for (int i = 0; i< f.length; i++) {
				t.set(null, ret+"/"+f[i]);
				f2 = t.list("*.wfl", FileBase.LIST_FILES_ONLY);
				if (f2 != null && f2.length > 0) return  ret;
			}
		}
		// lagacy dir
		ret = FileBase.getProgramDirectory() + "/maps";
		t.set(null, ret);
		f = t.list("*.wfl", FileBase.LIST_FILES_ONLY);
		if (f != null && f.length > 0) {
			MessageBox inf = new MessageBox("Information", "The directory for calibrated maps \nhas moved in this program version\n to '<profiles directory>/maps/standard'\n Do you want to move your calibrated maps there now?", FormBase.YESB | FormBase.NOB);
			if (inf.execute() == FormBase.IDYES) {
				String sp = getMapManuallySavePath(false);
				FileBugfix spF = new FileBugfix(sp);
				if (!spF.exists()) spF.mkdirs();
				String image;
				String lagacypath = ret;
				for (int i=0; i<f.length; i++) {
					t.set(null, lagacypath+f[i]);
					spF.set(null, sp+"/"+f[i]);
					t.move(spF);
					image = Common.getImageName(lagacypath+f[i].substring(0, f[i].lastIndexOf(".")));
					t.set(null, image);
					spF.set(null, sp+"/"+t.getFileExt());
					t.move(spF);
				}
				t.set(null, lagacypath);
				t.delete();
				return sp;
			}
			else return  ret;
		}
		// expedia dir
		// return getMapExpediaLoadPath();

		//whole maps directory
		return Global.getPref().absoluteBaseDir.replace('\\', '/') + "maps";
	}

	/**
	 * @param create if true the directory if it doesn't exist will be created
	 * @return the path where manually imported maps should be stored
	 * this should be adjustable in preferences...
	 */
	public String getMapManuallySavePath(boolean create) {
		String mapsDir = absoluteBaseDir + mapsPath;
		if (create && !(new FileBugfix(mapsDir).isDirectory())) { // dir exists?
			if (new FileBugfix(mapsDir).mkdirs() == false) {// dir creation failed?
				(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(172,"Error: cannot create maps directory: \n")+mapsDir, FormBase.OKB)).exec();
				return null;
			}
		}
		return mapsDir;
	}

	/**
	 * to this path the automatically downloaded maps are saved
	 */
	public String getMapDownloadSavePath(String mapkind) {
		String subdir = Global.getProfile().dataDir.substring(Global.getPref().absoluteBaseDir.length()).replace('\\', '/');
		String mapsDir = Global.getPref().absoluteBaseDir + "maps/" + Common.ClearForFileName(mapkind)+ "/" + subdir;
		if (!(new FileBugfix(mapsDir).isDirectory())) { // dir exists?
			if (new FileBugfix(mapsDir).mkdirs() == false) // dir creation failed?
			{(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(172,"Error: cannot create maps directory: \n")+new FileBugfix(mapsDir).getParentFile(), FormBase.OKB)).exec();
			return null;
			}
		}
		return mapsDir;
	}

	public String getMapExpediaLoadPath() {
		return Global.getPref().absoluteBaseDir.replace('\\', '/') + "maps/expedia"; // baseDir has trailing /
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // Profile Selector
	//////////////////////////////////////////////////////////////////////////////////////

	static protected final int PROFILE_SELECTOR_FORCED_ON=0;
	static protected final int PROFILE_SELECTOR_FORCED_OFF=1;
	static protected final int PROFILE_SELECTOR_ONOROFF=2;

	/**
	 * tries to get the home data dir of the user
	 * e.g. "c:\documents and...\<user>\my documents" or "/home/<user>" in linux
	 * if none could be identified, "/" is returned.
	 * @return
	 */
	public String getHomeDir() {
		String test;
		test = Vm.getenv("HOMEDRIVE", ""); // returns in java-vm on win xp: c:\<dokumente und Einstellungen>\<username>\<application data>
		log("Vm.getenv(HOMEDRIVE: " + test); // this works also in win32.exe (ewe-vm on win xp)
		test += Vm.getenv("HOMEPATH", ""); // returns in java-vm on win xp: c:\<dokumente und Einstellungen>\<username>\<application data>
		log("Vm.getenv(HOMEPATH: " + test); // this works also in win32.exe (ewe-vm on win xp)
		if (test.length() == 0)	test = Vm.getenv("HOME", ""); // This should return on *nix system the home dir
		if (test.length() == 0)	test = "/";
		return test;
	}

	private void checkAbsoluteBaseDir() {
		// If datadir is empty, ask for one
		if (absoluteBaseDir.length()==0 || !(new FileBugfix(absoluteBaseDir)).exists()) {
			do {
				FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, getHomeDir());
				fc.title = MyLocale.getMsg(170,"Select base directory for cache data");
				// If no base directory given, terminate
				if (fc.execute() == FormBase.IDCANCEL) ewe.sys.Vm.exit(0);
				setBaseDir(fc.getChosenFile().toString());
			}while (!(new FileBugfix(absoluteBaseDir)).exists());
		}
	}

	/**
	 * Open Profile selector screen
	 * @param prof
	 * @param showProfileSelector
	 * @return True if a profile was selected
	 */
	public boolean selectProfile(Profile prof, int showProfileSelector, boolean hasNewButton) {
		checkAbsoluteBaseDir();
		boolean profileExists=true;  // Assume that the profile exists
		do {
			if(!profileExists || (showProfileSelector==PROFILE_SELECTOR_FORCED_ON) ||
					(showProfileSelector==PROFILE_SELECTOR_ONOROFF && !autoReloadLastProfile)){ // Ask for the profile
				ProfilesForm f = new ProfilesForm(absoluteBaseDir,lastProfile,!profileExists || hasNewButton ? 0 : 1);
				int code = f.execute();
				// If no profile chosen (includes a new one), terminate
				if (code==-1) return false; // Cancel pressed
				CWPoint savecenter = new CWPoint(prof.centre);
				prof.clearProfile();
				prof.setCenterCoords(savecenter);
				//prof.hasUnsavedChanges = true;
				//curCentrePt.set(0,0); // No centre yet
				lastProfile=f.newSelectedProfile;
			}
			profileExists=(new FileBugfix(absoluteBaseDir+lastProfile)).exists();
			if (!profileExists) (new MessageBox(MyLocale.getMsg(144,"Warning"),
					           MyLocale.getMsg(171,"Profile does not exist: ")+lastProfile,FormBase.MBOK)).execute();
		} while (profileExists==false);
		// Now we are sure that baseDir exists and basDir+profile exists
		prof.name=lastProfile;
		prof.dataDir=absoluteBaseDir+lastProfile;
		prof.dataDir=prof.dataDir.replace('\\','/');
		if (!prof.dataDir.endsWith("/")) prof.dataDir+='/';
		pathToProfile=prof.dataDir;
		savePreferences();
		return true;
	}

	static public boolean deleteDirectory(FileBugfix path) {
	    if( path.exists() ) {
	    	String[] files = path.list();
	    	for(int i=0; i<files.length; i++) {
	    		FileBugfix f = new FileBugfix(path.getFullPath() + "/" + files[i]);
		        if(f.isDirectory()) {
		        	deleteDirectory(f);
		        }
		        else {
		        	f.delete();
		        }
		     }
		    }
	    return( path.delete() );
	}

	static public boolean renameDirectory(FileBugfix OldPath, FileBugfix NewPath) {
	    return OldPath.renameTo(NewPath);
	}

	/*
	 * operation 2=delete 3=rename
	 */
	public void editProfile(int operation, int ErrorMsgActive, int ErrorMsg) {
		checkAbsoluteBaseDir(); // perhaps not necessary
		// select profile
		ProfilesForm f = new ProfilesForm(absoluteBaseDir,"",operation);
		if (f.execute()==-1) return ; // no select
		// check selection
		if (lastProfile.equals(f.newSelectedProfile)) {
			// aktives Profil kann nicht gel�scht / umbenannt werden;
			new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(ErrorMsgActive,"[Profile active...]"),FormBase.MBOK).execute();
		}
		else {
			boolean err=true;
			if (operation==3) {
				String newName = new InputBox("Bitte neuen Verzeichnisnamen eingeben : ").input("",50);
				if (!newName.equals(null)) {
					err=!renameDirectory(new FileBugfix(absoluteBaseDir+f.newSelectedProfile),new FileBugfix(absoluteBaseDir+newName));
				}
			}
			else
			if (operation==2){
				Profile p = new Profile();
				p.dataDir=absoluteBaseDir+f.newSelectedProfile+"/";
				p.readIndex();
				String mapsPath=absoluteBaseDir+"maps"+p.getRelativeCustomMapsPath();
				int answer=new MessageBox("",mapsPath+" "+MyLocale.getMsg(143,"l�schen ?"),FormBase.MBYESNO).execute();
				if (answer==1) {
					deleteDirectory(new FileBugfix(mapsPath));
				}
				err=!deleteDirectory(new FileBugfix(absoluteBaseDir+f.newSelectedProfile));
				// ? wait until deleted ?
			}
			if (err) {new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(ErrorMsg,"[Profile Error...]"),FormBase.MBOK).execute();}
		}
	}
    //////////////////////////////////////////////////////////////////////////////////////
    // Log functions
	//////////////////////////////////////////////////////////////////////////////////////

	// FIXME: should use path to config file instead of program directory
	/** Log file is in program directory and called log.txt */
	private final String LOGFILENAME=FileBase.getProgramDirectory()+"/log.txt";

	/**
	 * Method to delete an existing log file. Called on every SpiderGC.
	 * The log file is also cleared when Preferences is created and the filesize > 60KB
	 */
	public void logInit(){
		File logFile = new FileBugfix(LOGFILENAME);
		logFile.delete();
		log("CW Version "+Version.getReleaseDetailed());
	}

	/**
	 * Method to log messages to a file called log.txt
	 * It will always append to an existing file.
	 * To show the message on the console, the global variable debug must be set.
	 * This can be done by adding
	 * <pre>&lt;debug value="true"&gt;</pre>
	 * to the pref.xml file
	 * @param text to log
	 */
	public void log(String text){
		if (debug) {
			Vm.debug(text);
			Time dtm = new Time();
			dtm.getTime();
			dtm.setFormat("dd.MM.yyyy'/'HH:mm:ss.SSS");
			text = dtm.toString()+ ": "+ text;
			FileWriter logFile = null;
			try{
				logFile = new FileWriter(LOGFILENAME, true);
				//Stream strout = null;
				//strout = logFile.toWritableStream(true);
				logFile.print(text+NEWLINE);
				//Vm.debug(text); Not needed - put <debug value="true"> into pref.xml
			}catch(Exception ex){
				Vm.debug("Error writing to log file!");
			}finally{
				if (logFile != null) try {logFile.close(); } catch (IOException ioe) {
					log("Ignored Exception", ioe, true);
				}
			}
		}
	}

	/** Log an exception to the log file with or without a stack trace
	 *
	 * @param text Optional message (Can be empty string)
	 * @param e The exception
	 * @param withStackTrace If true and the debug switch is true, the stack trace is appended to the log
	 * The debug switch can be set by including the line <i>&lt;debug value="true"&gt;&lt;/debug&gt;</i> in the pref.xml file
	 * or by manually setting it (i.e. in BE versions or RC versions) by including the line
	 * <pre>Global.getPref().debug=true;</pre>
	 * in Version.getRelease()
	 */
	public void log(String text,Throwable e, boolean withStackTrace) {
		String msg;
		if (text.equals("")) msg=text; else msg=text+"\n";
		if (e!=null) {
			if (withStackTrace && debug)
				msg+=ewe.sys.Vm.getAStackTrace(e);
			else
				msg+=e.toString();
		}
		log(msg);
	}

	/** Log an exception to the log file without a stack trace, i.e.
	 * where a stack trace is not needed because the location/cause of the error is clear
	 *
	 * @param message Optional message (Can be empty string)
	 * @param e The exception
	 */
	public void log(String message,Exception e) {
		log (message,e,false);
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // Exporter path functions
	//////////////////////////////////////////////////////////////////////////////////////

	/** Hashtable for storing the last export path */
	private Hashtable exporterPaths = new Hashtable();

	public void setExportPath(String exporter,String path){
		exporterPaths.put(exporter, path);
		savePreferences();
	}

	public void setExportPathFromFileName(String exporter,String filename){
		File tmpfile = new FileBugfix (filename);
		exporterPaths.put(exporter, tmpfile.getPath());
		savePreferences();
	}

	public String getExportPath(String exporter){
		String dir = (String) exporterPaths.get(exporter);
		if (dir == null){
			dir = Global.getProfile().dataDir;
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
		if (null == dir) dir = Global.getProfile().dataDir;
		return dir;
	}

	/**
	 * <code>True</code> or <code>false</code>, depending if a filter with the given ID is
	 * saved in the preferences.
	 * @param filterID ID of the filter to check
	 * @return True or false
	 */
	public boolean hasFilter(String filterID) {
		return this.filterList.containsKey(filterID);
	}

	/**
	 * Returns the FilterData object saved with the given ID. The ID is not saved in the object,
	 * so it may be resaved under another ID.
	 * @param filterID ID of the FilterData object
	 * @return FilterData object
	 */
	public FilterData getFilter(String filterID) {
		return (FilterData)this.filterList.get(filterID);
	}

	/**
	 * Adds a FilterData object to the list. If a FilterData object is already saved unter the
	 * given ID, the old object is removed and the new one is set at its place.
	 * @param filterID ID to associate with the filter object
	 * @param filter FilterData object
	 */
	public void addFilter(String filterID, FilterData filter) {
		this.filterList.put(filterID, filter);
	}

	/**
	 * Removed the FilterData object which is saved with the given ID. If no such FilterData object
	 * exists, nothing happens.
	 * @param filterID ID of FilterData object to remove
	 */
	public void removeFilter(String filterID) {
		this.filterList.remove(filterID);
	}

	/**
	 * Returns a alphabetically sorted array of ID of saved FilterData objects.
	 * @return Array of IDs
	 */
	public String[] getFilterIDs() {
		String[] result;
		result = new String[this.filterList.size()];
		Enumeration en = this.filterList.keys();
		int i=0;
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
		try{
			ewe.sys.Process p = Vm.exec("gpsbabel -V");
			p.waitFor();
			gpsbabel="gpsbabel";
		}catch(IOException ioex){
			// Most of the time there will be an exception, so don't complain
		}
		if ( gpsbabel == null ) {
			try{
				ewe.sys.Process p = Vm.exec("gpsbabel.exe -V");
				p.waitFor();
				gpsbabel = "gpsbabel.exe";
			}catch(IOException ioex){
				// Most of the time there will be an exception, so don't complain
			}
		}
	}

	/** get directory where pref.xml is stored<br>
	 *  use this if you need a path where the user has sufficient rights to create a file */
	public String getPathToConfigFile() {
		return pathToConfigFile;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String dir) {
		baseDir = dir;
		baseDir=baseDir.replace('\\','/');
		if (!baseDir.endsWith("/")) baseDir+="/";
		absoluteBaseDir = new FileBugfix(baseDir).getAbsolutePath();
		absoluteBaseDir=absoluteBaseDir.replace('\\','/');
		if (!absoluteBaseDir.endsWith("/")) absoluteBaseDir+="/";
	}

}
