package CacheWolf;

import ewe.io.*;
import ewe.sys.*;
import ewe.ui.*;
import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.filechooser.*;

/**
 *	A class to hold the preferences that were loaded upon start up of CacheWolf.
 *	This class is also capable of parsing the prefs.xml file as well as
 *	saving the current settings of preferences.
 */
public class Preferences extends MinML{

	public int tablePrefs[] = {1,1,1,1,1,1,1,1,1,1,1,1};
	public int tableWidth[] = {20,20,20,20,65,135,135,100,60,50,50,50};

	static protected final int PROFILE_SELECTOR_FORCED_ON=0;
	static protected final int PROFILE_SELECTOR_FORCED_OFF=1;
	static protected final int PROFILE_SELECTOR_ONOROFF=2;

	/** The currently used centre point, can be different from the profile's centrepoint. This is used
	 *  for spidering */
	public CWPoint curCentrePt=new CWPoint();
	/** Name of last used profile */
	public String lastProfile=new String(); 
	/** If true, the last profile is reloaded automatically without a dialogue */
	public boolean autoReloadLastProfile=false; 
	/** The base directory contains one subdirectory for each profile*/
	public String baseDir = new String();  // TODO Set this initially to mydataDir ??

	public String myproxy = new String();    
	public String myproxyport = new String();
	/** This is the login alias for geocaching.com and opencaching.de */
	public String myAlias = new String();
	/** This is an alternative alias used to identify found caches (i.e. if using multiple IDs) 
	 *  It is currently not used yet */
	public String myAlias2 = new String();
	/** The path to the browser */
	public String browser = new String();
	public boolean showDeletedImages=true; /* Used in ImagePanel */
	public boolean solverIgnoreCase=false;

	public int myAppHeight = 0;
	public int myAppWidth = 0;
	//public int nLogs = 5;
	public boolean dirty = false;

	public int currProfile = 0;
	public String profiles[] = new String[4];
	public String profdirs[] = new String[4];
	public String lats[] = new String[4];
	public String longs[] = new String[4];
	public String lastSyncOC[] = new String[4];
	public String lastDistOC[] = new String[4];
	public String garminConn="com1";  // The type of connection which GPSBABEL uses: com1 OR usb.
	// These settings govern where the menu and the tabs are displayed and whether the statusbas is shown
	public boolean menuAtTop=true;
	public boolean tabsAtTop=true;
	public boolean showStatus=true;
	public boolean hasCloseButton=true;
	// This setting determines how many logs are shown per page of hintlogs (default 5)
	public final int DEFAULT_LOGS_PER_PAGE=5;
	public int logsPerPage=DEFAULT_LOGS_PER_PAGE;
	public boolean downloadPicsOC = true; //TODO Sollten die auch im Profil gespeichert werden mit Preferences als default Werte ?
	public boolean downloadMapsOC = true;
	public boolean downloadmissingOC = false;
	public boolean fixSIP = false;

	public String digSeparator = new String();
	public boolean debug = false;
	public SerialPortOptions mySPO = new SerialPortOptions();
	public boolean forwardGPS = false;
	public String forwardGpsHost = new String();
	public int fontSize = 12;

	public String mapsPath = new String("/maps/standard");
	// Helper variables for XML parser 
	private StringBuffer collectElement=null; 
	private String lastName; // The string to the last XML that was processed

	private final String LOGFILENAME="log.txt";
	// The following declarations may eventually be moved to a separate class
	/** The actual directory of a profile, for new profiles this is a direct child of baseDir */
	//TODO Find all references amd move to profile.dataDir
	//public String mydatadir = new String();  //Redundant ??
	/** The centre as read from the profile */

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

	/**
	 * Constructor is private for a singleton object
	 *
	 */
	private Preferences(){
		digSeparator=MyLocale.getDigSeparator();
		//Vm.debug("Separ: " + digSeparator);
		mySPO.bits = 8;
		mySPO.parity = SerialPort.NOPARITY;
		mySPO.stopBits = 1;
		mySPO.baudRate = 4800;
		// Ensure that logfile does not grow infinitely. Not really needed as every spider resets it
		File logFile = new File(LOGFILENAME);
		if (logFile.length()>60000) logInit();
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
	 * Later the maps-path shall be saved in the preferences
	 */
	public String getMapLoadPath() {
		// here could also a list of map-types displayed...
		// standard dir
		File t = new File(getMapManuallySavePath());
		String[] f = t.list("*.wfl", File.LIST_ALWAYS_INCLUDE_DIRECTORIES | File.LIST_FILES_ONLY);
		if (f != null && f.length > 0) return  baseDir + mapsPath;
		f = t.list("*.wfl", File.LIST_DIRECTORIES_ONLY | File.LIST_ALWAYS_INCLUDE_DIRECTORIES);
		if (f != null && f.length > 0) { // see if in a subdir of <baseDir>/maps/standard are .wfl files
			String[] f2;
			for (int i = 0; i< f.length; i++) {
				t.set(null, getMapManuallySavePath()+"/"+f[i]);
				f2 = t.list("*.wfl", File.LIST_FILES_ONLY);
				if (f2 != null && f2.length > 0) return  getMapManuallySavePath();
			}
		}
		// lagacy dir 
		t.set(null, File.getProgramDirectory() + "/maps");
		f = t.list("*.wfl", File.LIST_FILES_ONLY);
		if (f != null && f.length > 0) {
			MessageBox inf = new MessageBox("Information", "The directory for calibrated maps \nhas moved in this program version\n to '<profiles directory>/maps/standard'\n Do you want to move your calibrated maps there now?", MessageBox.YESB | MessageBox.NOB);
			if (inf.execute() == MessageBox.IDYES) {
				String sp = getMapManuallySavePath();
				File spF = new File(sp);
				if (!spF.exists()) spF.mkdirs();
				String image;
				String lagacypath = File.getProgramDirectory() + "/maps/";
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
			else return  File.getProgramDirectory() + "/maps";
		}
		// expedia dir
		return getMapExpediaLoadPath(); 
	}

	/**
	 * 
	 * @return the path where manually imported maps should be stored
	 * this should be adjustable in preferences...
	 */
	public String getMapManuallySavePath() {
		return baseDir + mapsPath;
	}

	/**
	 * to this path the automatically downloaded maps are saved
	 */
	public String getMapExpediaSavePath() {
		String subdir = Global.getProfile().dataDir.substring(Global.getPref().baseDir.length());
		String mapsDir = Global.getPref().baseDir + "/maps/expedia/" + subdir;
		if (!(new File(mapsDir).isDirectory())) { // dir exists? 
			if (new File(mapsDir).mkdirs() == false) // dir creation failed?
			{(new MessageBox("Error", "Error: cannot create maps directory: \n"+new File(mapsDir).getParentFile(), MessageBox.OKB)).exec();
			return null;
			}
		}
		return mapsDir;
	}

	public String getMapExpediaLoadPath() {
		return Global.getPref().baseDir + "/maps/expedia/";
	}

	/**
	 * Returns true if coordinates have been set.
	 * Does not validate! if coordinates are real.
	 */
	public boolean existCenter(){
		return curCentrePt.latDec!=0.0 && curCentrePt.lonDec!=0.0; // TODO: use cusCentrePt.isValid() 
	}

	/**
	 * Method to open and parse the pref.xml file. Results are stored in the
	 * public variables of this class.
	 */
	public void readPrefFile(){
		try{
			String datei = File.getProgramDirectory() + "/" + "pref.xml";
			datei = datei.replace('\\', '/');
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(datei));
			parse(r);
			r.close();
		}catch(Exception e){
			if (e instanceof NullPointerException)
				Vm.debug("NullPointerException in Element "+lastName +". Wrong attribute?");
			else 
				Vm.debug(e.toString());
		}
	}

	/**
	 * Open Profile selector screen 
	 * @param prof
	 * @param showProfileSelector
	 * @return True if a profile was selected
	 */

	public boolean selectProfile(Profile prof, int showProfileSelector, boolean hasNewButton) {
		// If datadir is empty, ask for one
		if (baseDir.length()==0) {
			FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT,null);
			fc.title = "Select base directory for cache data";
			// If no base directory given, terminate
			if (fc.execute() == FileChooser.IDCANCEL) ewe.sys.Vm.exit(0);
			baseDir = fc.getChosenFile().toString();
		}
		if (!baseDir.endsWith("/")) baseDir+="/";
		//Vm.showWait(false);
		if((showProfileSelector==PROFILE_SELECTOR_FORCED_ON) || 
				(showProfileSelector==PROFILE_SELECTOR_ONOROFF && !autoReloadLastProfile)){ // Ask for the profile
			ProfilesForm f = new ProfilesForm(baseDir,profiles,lastProfile,hasNewButton);
			int code = f.execute();
			// If no profile chosen (includes a new one), terminate
			if (code==-1) return false; // Cancel pressed
			prof.clearProfile();
			curCentrePt.set(0,0); // No centre yet
			lastProfile=f.newSelectedProfile;
		} 
		prof.name=lastProfile;
		currProfile=-1;
		if (lastProfile.equals(profiles[0])) openOldProfile(prof, 0);
		else if (lastProfile.equals(profiles[1])) openOldProfile(prof, 1);
		else if (lastProfile.equals(profiles[2])) openOldProfile(prof, 2);
		else if (lastProfile.equals(profiles[3])) openOldProfile(prof, 3);
		else { 
			prof.dataDir=baseDir+lastProfile+"/";
			//mydatadir=prof.dataDir;
		}
		savePreferences();
		return true;

	}

	/**
	 * Open an old Profile (stored in preferences)
	 * @param i 0-3 for profiles 1-4
	 */
	private void openOldProfile(Profile prof, int i) {
		currProfile=i+1;
		curCentrePt.set(lats[i]+" "+longs[i]);
		//mydatadir=profdirs[i];
		if(lastSyncOC[i] == null || lastSyncOC[i].endsWith("null")){
			prof.last_sync_opencaching = "20050801000000";
		}else {
			prof.last_sync_opencaching = lastSyncOC[i];
		}
		if(lastDistOC[i] == null || lastDistOC[i].endsWith("null")){
			prof.distOC = "0";
		} else {
			prof.distOC = lastDistOC[i];
		}
		prof.centre.set(lats[i]+" "+longs[i]);
		prof.dataDir=profdirs[i];
	}




	/**
	 * Method that gets called when a new element has been identified in pref.xml
	 */
	public void startElement(String name, AttributeList atts){
		//Vm.debug("name = "+name);
		lastName=name;
		String tmp;
		if(name.equals("browser")) browser = atts.getValue("name");
		if(name.equals("fixedsip")) {
			if(atts.getValue("state").equals("true")) {
				fixSIP = true;
			}
		}
		if(name.equals("font")) fontSize = Convert.toInt(atts.getValue("size"));
		if(name.equals("alias")) myAlias = atts.getValue("name");
		if(name.equals("alias2")) myAlias2 = atts.getValue("name");
		if(name.equals("location")){
			curCentrePt.set(atts.getValue("lat")+" "+atts.getValue("long"));
		}
		if(name.equals("port")){
			mySPO.portName = atts.getValue("portname");
			mySPO.baudRate = Convert.toInt(atts.getValue("baud"));
		}
		if(name.equals("portforward")) {
			forwardGPS = Convert.toBoolean(atts.getValue("active"));
			forwardGpsHost = atts.getValue("destinationHost");
		}
		//if(name.equals("logs")){
		//	nLogs = Convert.parseInt(atts.getValue("number"));
		//}
		if(name.equals("profile1")){
			profiles[0] = atts.getValue("name");
			profdirs[0] = atts.getValue("dir");
			lats[0] = atts.getValue("lat");
			longs[0] = atts.getValue("lon");
			lastSyncOC[0] = atts.getValue("lastsyncoc");
			lastDistOC[0] = atts.getValue("lastdistoc");
		}
		if(name.equals("profile2")){
			profiles[1] = atts.getValue("name");
			profdirs[1] = atts.getValue("dir");
			lats[1] = atts.getValue("lat");
			longs[1] = atts.getValue("lon");
			lastSyncOC[1] = atts.getValue("lastsyncoc");
			lastDistOC[1] = atts.getValue("lastdistoc");
		}
		if(name.equals("profile3")){
			profiles[2] = atts.getValue("name");
			profdirs[2] = atts.getValue("dir");
			lats[2] = atts.getValue("lat");
			longs[2] = atts.getValue("lon");
			lastSyncOC[2] = atts.getValue("lastsyncoc");
			lastDistOC[2] = atts.getValue("lastdistoc");
		}
		if(name.equals("profile4")){
			profiles[3] = atts.getValue("name");
			profdirs[3] = atts.getValue("dir");
			lats[3] = atts.getValue("lat");
			longs[3] = atts.getValue("lon");
			lastSyncOC[3] = atts.getValue("lastsyncoc");
			lastDistOC[3] = atts.getValue("lastdistoc");
		}
		if (name.equals("lastprofile")) {
			collectElement=new StringBuffer(50);
			if (atts.getValue("autoreload").equals("true")) autoReloadLastProfile=true;
		}

		//if(name.equals("datadir")) {
		//mydatadir = atts.getValue("dir");
		//profile.dataDir=mydatadir;
		//}
		if(name.equals("basedir")) {
			baseDir = atts.getValue("dir");
		}
		if (name.equals("opencaching")) {
			downloadPicsOC = Boolean.valueOf(atts.getValue("downloadPics")).booleanValue();
			downloadMapsOC = Boolean.valueOf(atts.getValue("downloadMaps")).booleanValue();
			downloadmissingOC = Boolean.valueOf(atts.getValue("downloadmissing")).booleanValue();

		}
		if(name.equals("proxy")) {
			myproxy = atts.getValue("prx");
			myproxyport = atts.getValue("prt");
		}
		if (name.equals("garmin")) {
			garminConn=atts.getValue("connection");
		}

		if(name.equals("tableType")){ 
			tablePrefs[1] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[1] = Convert.parseInt(tmp);
		}
		if(name.equals("tableD")){
			tablePrefs[2] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[2] = Convert.parseInt(tmp);
		}
		if(name.equals("tableT")){
			tablePrefs[3] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[3] = Convert.parseInt(tmp);
		}
		if(name.equals("tableWay")) {
			tablePrefs[4] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[4] = Convert.parseInt(tmp);
		}
		if(name.equals("tableName")){
			tablePrefs[5] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[5] = Convert.parseInt(tmp);
		}
		if(name.equals("tableLoc")){
			tablePrefs[6] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[6] = Convert.parseInt(tmp);
		}
		if(name.equals("tableOwn")){
			tablePrefs[7] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[7] = Convert.parseInt(tmp);
		}
		if(name.equals("tableHide")){
			tablePrefs[8] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[8] = Convert.parseInt(tmp);
		}
		if(name.equals("tableStat")){
			tablePrefs[9] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[9] = Convert.parseInt(tmp);
		}
		if(name.equals("tableDist")){
			tablePrefs[10] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[10] = Convert.parseInt(tmp);
		}
		if(name.equals("tableBear")){
			tablePrefs[11] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[11] = Convert.parseInt(tmp);
		}
		if (name.equals("imagepanel")) {
			showDeletedImages = Boolean.valueOf(atts.getValue("showdeletedimages")).booleanValue();
		}
		if (name.equals("screen")) {
			menuAtTop=Boolean.valueOf(atts.getValue("menuattop")).booleanValue();
			tabsAtTop=Boolean.valueOf(atts.getValue("tabsattop")).booleanValue();
			showStatus=Boolean.valueOf(atts.getValue("showstatus")).booleanValue();
			if (atts.getValue("hasclosebutton")!=null)
				hasCloseButton=Boolean.valueOf(atts.getValue("hasclosebutton")).booleanValue();
		}
		if (name.equals("hintlogpanel")) {
			logsPerPage = Convert.parseInt(atts.getValue("logsperpage"));
		}
		if (name.equals("solver")) {
			solverIgnoreCase=Boolean.valueOf(atts.getValue("ignorevariablecase")).booleanValue();
		}
	}

	public void characters( char ch[], int start, int length )
	{
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


	/**
	 * Method to save current preferences in the pref.xml file
	 */
	public void savePreferences(){
		String datei = File.getProgramDirectory() + "/" + "pref.xml";
		datei = datei.replace('\\', '/');
		//last_sync_opencaching = last_sync_opencaching==null?"20050801000000":last_sync_opencaching;
		//distOC = distOC==null?"0":distOC;
		if (currProfile > 0) {
			lastSyncOC[currProfile -1] = Global.getProfile().last_sync_opencaching;
			lastDistOC[currProfile - 1] = Global.getProfile().distOC;
		}

		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(datei)));
			outp.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			outp.print("<preferences>\n");
			outp.print("	<alias name =\""+ SafeXML.clean(myAlias) +"\"/>\n");
			outp.print("	<alias2 name =\""+ SafeXML.clean(myAlias2) +"\"/>\n");
			outp.print("	<basedir dir = \""+ baseDir +"\"/>\n");
			outp.print("	<proxy prx = \""+ myproxy+"\" prt = \""+ myproxyport + "\"/>\n");
			outp.print("	<port portname = \""+ mySPO.portName +"\" baud = \""+ mySPO.baudRate+"\"/>\n");
			outp.print("	<portforward active= \""+ Convert.toString(forwardGPS)+"\" destinationHost = \""+ forwardGpsHost+"\"/>\n");
			outp.print("	<tableType active = \"1\" width = \""+Convert.toString(tableWidth[1])+"\"/>\n");
			outp.print("	<tableD active = \""+Convert.toString(tablePrefs[2])+ "\"" +
					" width = \""+Convert.toString(tableWidth[2])+"\"/>\n");
			outp.print("	<tableT active = \""+Convert.toString(tablePrefs[3])+ "\"" +
					" width = \""+Convert.toString(tableWidth[3])+"\"/>\n");
			outp.print("	<tableWay active = \"1\" width = \""+Convert.toString(tableWidth[4])+"\"/>\n");
			outp.print("	<tableName active = \"1\" width = \""+Convert.toString(tableWidth[5])+"\"/>\n");
			outp.print("	<tableLoc active = \""+Convert.toString(tablePrefs[6])+ "\"" +
					" width = \""+Convert.toString(tableWidth[6])+"\"/>\n");
			outp.print("	<tableOwn active = \""+Convert.toString(tablePrefs[7])+ "\"" +
					" width = \""+Convert.toString(tableWidth[7])+"\"/>\n");
			outp.print("	<tableHide active = \""+Convert.toString(tablePrefs[8])+ "\"" +
					" width = \""+Convert.toString(tableWidth[8])+"\"/>\n");
			outp.print("	<tableStat active = \""+Convert.toString(tablePrefs[9])+ "\"" +
					" width = \""+Convert.toString(tableWidth[9])+"\"/>\n");
			outp.print("	<tableDist active = \""+Convert.toString(tablePrefs[10])+ "\"" +
					" width = \""+Convert.toString(tableWidth[10])+"\"/>\n");
			outp.print("	<tableBear active = \""+Convert.toString(tablePrefs[11])+ "\"" +
					" width = \""+Convert.toString(tableWidth[11])+"\"/>\n");
			outp.print("    <font size =\""+fontSize+"\"/>\n");
			outp.print("	<browser name = \""+browser+"\"/>\n");
			outp.print("    <fixedsip state = \""+fixSIP+"\"/>\n");
			outp.print("    <garmin connection = \""+garminConn+"\"/>\n");
			outp.print("    <lastprofile autoreload=\""+autoReloadLastProfile+"\">"+lastProfile+"</lastprofile>\n"); //RB
			outp.print("    <screen menuattop=\""+menuAtTop+"\" tabsattop=\""+tabsAtTop+"\" showstatus=\""+showStatus+"\" hasclosebutton=\""+hasCloseButton+"\"/>\n");
			outp.print("    <imagepanel showdeletedimages=\""+showDeletedImages+"\"/>\n");
			outp.print("    <hintlogpanel logsperpage=\""+logsPerPage+"\"/>\n");
			outp.print("    <solver ignorevariablecase=\""+solverIgnoreCase+"\"/>\n");
			outp.print("    <opencaching downloadPicsOC=\""+downloadPicsOC+"\" downloadMaps=\""+downloadMapsOC+"\" downloadMissing=\""+downloadmissingOC+"\"/>\n");
			// Obsolete data kept for backward compatibility
			//outp.print("	<syncOC date = \"" + last_sync_opencaching + "\" dist = \"" + distOC +  "\"/>\n");
			outp.print("	<location lat = \""+curCentrePt.getLatDeg(CWPoint.DD)+"\" long = \""+curCentrePt.getLonDeg(CWPoint.DD)+"\"/>\n");
			//outp.print("	<datadir dir = \""+ mydatadir +"\"/>\n");
			outp.print("	<profile1 name = \""+profiles[0]+"\" lat = \""+ lats[0] +"\" lon = \""+ longs[0] +"\" dir = \""+ profdirs[0] +"\" lastsyncoc= \"" + lastSyncOC[0] + "\" lastdistoc= \"" + lastDistOC[0] + "\" />\n");
			outp.print("	<profile2 name = \""+profiles[1]+"\" lat = \""+ lats[1] +"\" lon = \""+ longs[1] +"\" dir = \""+ profdirs[1] +"\" lastsyncoc= \"" + lastSyncOC[1] + "\" lastdistoc= \"" + lastDistOC[1] + "\" />\n");
			outp.print("	<profile3 name = \""+profiles[2]+"\" lat = \""+ lats[2] +"\" lon = \""+ longs[2] +"\" dir = \""+ profdirs[2] +"\" lastsyncoc= \"" + lastSyncOC[2] + "\" lastdistoc= \"" + lastDistOC[2] + "\" />\n");
			outp.print("	<profile4 name = \""+profiles[3]+"\" lat = \""+ lats[3] +"\" lon = \""+ longs[3] +"\" dir = \""+ profdirs[3] +"\" lastsyncoc= \"" + lastSyncOC[3] + "\" lastdistoc= \"" + lastDistOC[3] + "\" />\n");
			outp.print("</preferences>");
			outp.close();
		} catch (Exception e) {
			Vm.debug("Problem saving: " +datei);
			Vm.debug("Error: " +e.toString());
		}
	}

	/**
	 * Method to log messages to a file called log.txt
	 * It will always append to an existing file.
	 * @param text
	 */
	public void log(String text){
		Time dtm = new Time();
		dtm.getTime();
		dtm.setFormat("dd.MM.yyyy'/'HH:mm");
		text = dtm.toString()+ ": "+ text + "\n";
		File logFile = new File(LOGFILENAME);
		Stream strout = null;
		try{
			strout = logFile.toWritableStream(true);
			strout.write(text.getBytes());
		}catch(Exception ex){
			Vm.debug("Error writing to log file!");
		}finally{
			strout.close();
		}
	}

	/**
	 * Method to delete an existing log file. Something like a "reset".
	 * Should be used "from time to time" to make sure the log file does not grow
	 * to a huge size! Called on every SpiderGC
	 */
	public void logInit(){
		File logFile = new File(LOGFILENAME);
		logFile.delete();
	}
}
