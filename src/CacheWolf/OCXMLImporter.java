package CacheWolf;

import utils.FileBugfix;

import com.stevesoft.ewe_pat.Regex;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.util.zip.*;
import ewe.net.*;
import ewe.sys.Double;

/**
 *	Class to import Data from opencaching.de. 
 *	It uses the lastmodified parameter to identify new or changed caches.
 *	See here: http://www.opencaching.com/phpBB2/viewtopic.php?t=281 (out-dated)
 *   See here: http://www.opencaching.de/doc/xml/xml11.htm and http://develforum.opencaching.de/viewtopic.php?t=135&postdays=0&postorder=asc&start=0
 *	for more information.
 */
public class OCXMLImporter extends MinML {
	static protected final int STAT_INIT = 0;
	static protected final int STAT_CACHE = 1;
	static protected final int STAT_CACHE_DESC = 2;
	static protected final int STAT_CACHE_LOG = 3;
	static protected final int STAT_PICTURE = 4;

	final static String OPENCACHING_HOST = "www.opencaching.de";
	int state = STAT_INIT;
	int numCacheImported, numDescImported, numLogImported= 0;

	boolean debugGPX = false;
	Vector cacheDB;
	InfoBox inf;
	CacheHolder ch;
	CacheHolderDetail chD;
	Preferences pref;
	Profile profile;
	Time dateOfthisSync;
	String strData = new String();
	int picCnt;
	boolean incUpdate = true; // complete or incremental Update
	boolean ignoreDesc = false;
	boolean askForOptions = true;
	Hashtable DBindexWpt = new Hashtable();
	Hashtable DBindexID = new Hashtable();

	String picUrl = new String();
	String picTitle =  new String();
	String picID = new String();
	String ocSeekUrl = new String("http://"+OPENCACHING_HOST+"/viewcache.php?cacheid=");
	String cacheID = new String();

	String logData, logIcon, logDate, logFinder, logId;
	boolean loggerRecommended;
	int logtype;
	String user;
	double longitude;


	public OCXMLImporter(Preferences p,Profile prof)
	{
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
		if(profile.last_sync_opencaching == null ||
				profile.last_sync_opencaching.length() < 12){
			profile.last_sync_opencaching = "20050801000000";
			incUpdate = false;
		}
		user = p.myAlias.toLowerCase();
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindexWpt.put(ch.wayPoint, new Integer(i));
			if (!ch.ocCacheID.equals(""))
				DBindexID.put(ch.ocCacheID, new Integer(i));
		}//for

	}

	/** true, if not the last syncdate shall be used, but the caches shall be reloaded
	 * only used in syncSingle */
	boolean reload;
	/**
	 * 
	 * @param number
	 * @param infB
	 * @return true, if some change was made to the cacheDB
	 */
	public boolean syncSingle(int number, InfoBox infB) {
		ch = (CacheHolder)cacheDB.get(number);
		chD= null; //new CacheHolderDetail(ch); //TODO is this still correct? use getDetails ?

		if (infB.isClosed) {
			if (askForOptions) return false; 
			else return true;
		}
		if (askForOptions) {
			OCXMLImporterScreen importOpt = new OCXMLImporterScreen( MyLocale.getMsg(1600, "Opencaching.de Download"),OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.ALL);
			if (importOpt.execute() == FormBase.IDCANCEL) {	return false; }
			askForOptions = false;
			reload = importOpt.missingCheckBox.getState();
		}

		// this is only a dummy-InfoBox for capturing the output
		inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
//		inf.setPreferredSize(220, 300);
//		inf.relayout(false);
//		inf.exec();

		String lastS; 
		if (reload)  lastS = "20050801000000";
		else {
			if (ch.lastSyncOC.length() < 14) lastS = "20050801000000";
			else lastS = ch.lastSyncOC;
		}
		dateOfthisSync = new Time();
		dateOfthisSync.parse(lastS, "yyyyMMddHHmmss");
	

		String url = new String();
		picCnt = 0;
		//Build url
		url = "http://" + OPENCACHING_HOST + "/xml/ocxml11.php?"
			+ "modifiedsince=" + lastS
			+ "&cache=1"
			+ "&cachedesc=1";
		if (pref.downloadPicsOC) url += "&picture=1";
		else url += "&picture=0";
		url += "&cachelog=1"
			+ "&removedobject=0"
			+ "&wp=" + ch.wayPoint
			+ "&charset=utf-8"
			+ "&cdata=0"
			+ "&session=0";
		syncOC(url);
		inf.close(0);
		return true;
	}

	public void doIt(){
		boolean success=true;
		String finalMessage;

		
		String url = new String();

		String lastS =  profile.last_sync_opencaching;
		CWPoint centre = pref.curCentrePt; // No need to clone curCentrePt as centre is only read
		if (!centre.isValid()) {
			(new MessageBox("Error", "Coordinates for centre must be set", FormBase.OKB)).execute();
			return;
		}
		OCXMLImporterScreen importOpt = new OCXMLImporterScreen( MyLocale.getMsg(1600, "Opencaching.de Download"),
																 OCXMLImporterScreen.ALL | OCXMLImporterScreen.DIST | OCXMLImporterScreen.IMAGES);
		if (importOpt.execute() == FormBase.IDCANCEL) {	return; }
		Vm.showWait(true);
		String dist = importOpt.distanceInput.getText();
		if (dist.length()== 0) return;
		
		Double distDouble = new Double();
		distDouble.value = Common.parseDouble(dist);
		dist = distDouble.toString(0, 1, 0).replace(',', '.');
		//check, if distance is greater than before
		if (Convert.toInt(dist) > Convert.toInt(profile.distOC) ||
				pref.downloadmissingOC  ){
			// resysnc
			lastS = "20050801000000";
			incUpdate = false;
		}
		profile.distOC = dist;
		// Clear status of caches in db
		for(int i = cacheDB.size()-1; i>=0 ;i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_update = false;
			ch.is_new = false;
			ch.is_log_update = false;
		}	
		picCnt = 0;
		//Build url
		url = "http://" + OPENCACHING_HOST + "/xml/ocxml11.php?"
			+ "modifiedsince=" + lastS
			+ "&cache=1"
			+ "&cachedesc=1";
		if (pref.downloadPicsOC) url += "&picture=1";
		else url += "&picture=0";
		url += "&cachelog=1"
			+ "&removedobject=0"
			+ "&lat=" + centre.getLatDeg(CWPoint.DD)
			+ "&lon=" + centre.getLonDeg(CWPoint.DD)
			+ "&distance=" + dist
			+ "&charset=utf-8"
			+ "&cdata=0"
			+ "&session=0";
		inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
		inf.setPreferredSize(220, 300);
		inf.relayout(false);
		inf.exec();

		success = syncOC(url);
		profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
		Vm.showWait(false);
		if (success) {
			profile.last_sync_opencaching = dateOfthisSync.format("yyyyMMddHHmmss");
			//pref.savePreferences();
			finalMessage = MyLocale.getMsg(1607,"Update from opencaching successful"); 
			inf.addWarning("\nNumber of"+
			"\n...caches new/updated: " + numCacheImported +
			"\n...cache descriptions new/updated: " + numDescImported +
			"\n...logs new/updated: " + numLogImported);
			inf.setInfo(finalMessage);
		}
		inf.addOkButton();
	}
	
	private boolean syncOC(String url) {
		String finalMessage = new String();
		boolean success=true;
		File tmpFile = null;
		BufferedReader r;
		String file = new String();

		//inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
		
		picCnt = 0;
		try{
			chD = null;
			file = fetch(url, "dummy");

			//parse
			tmpFile = new FileBugfix(profile.dataDir + file);
			if (tmpFile.getLength() == 0 ) {
				throw new IOException("no updates available");
			}

			ZipFile zif = new ZipFile (profile.dataDir + file);
			ZipEntry zipEnt;
			Enumeration zipEnum = zif.entries();
			inf.setInfo("...unzipping update file"); 
			while (zipEnum.hasMoreElements())
			{
				zipEnt = (ZipEntry) zipEnum.nextElement();
				// skip over PRC-files and empty files
				if (zipEnt.getSize()> 0 && zipEnt.getName().endsWith("xml")){
					r = new BufferedReader (new InputStreamReader(zif.getInputStream(zipEnt), IO.JAVA_UTF8_CODEC));
					parse(r);
					r.close();
				}
			}
			zif.close();
		}catch (ZipException e){
			finalMessage = MyLocale.getMsg(1614,"Error while unzipping udpate file");
			success = false;
		}catch (IOException e){
			if (e.getMessage().equalsIgnoreCase("no updates available")) { finalMessage = "No updates available"; success = false; }
			else {
				if (e.getMessage().equalsIgnoreCase("could not connect") ||
						e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
					finalMessage = MyLocale.getMsg(1616,"Error: could not download udpate file from opencaching.de");
				} else { finalMessage = "IOException: "+e.getMessage(); }
				success = false;
			}
		}catch (IllegalArgumentException e) {
			finalMessage = MyLocale.getMsg(1621,"Error parsing update file\n this is likely a bug in opencaching.de\nplease try again later\n, state:")+" "+state+", waypoint: "+ chD.wayPoint;
			success = false;
			Vm.debug("Parse error: " + state + " " + chD.wayPoint);
			e.printStackTrace();
		}catch (Exception e){ // here schould be used the correct exepion
			if (chD != null)	finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: "+ chD.wayPoint;
			else finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: <unkown>";
			success = false;
			Vm.debug("Parse error: " + state + " Exception:" + e.toString()+"   "+chD.ocCacheID);
			e.printStackTrace();
		} finally {
			if (tmpFile != null) tmpFile.delete();
		}
		/*
		for (int i=cacheDB.size()-1; i >=0; i--) {
			ch = (CacheHolder)cacheDB.get(i);
			if (ch.wayPoint.toUpperCase().startsWith("OC")) { //TODO only handle changed caches
				ch.calcRecommendationScore();
			}
		} */
		inf.setInfo(finalMessage);

		return success;
	}

	public void startElement(String name, AttributeList atts){
		if (debugGPX){
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug(" Name: " + atts.getName(i)+ " Value: "+atts.getValue(i));
			}
		}
		strData ="";

		if (name.equals("oc11xml")){
			Time lastSync = new Time();
			try {
				lastSync.parse(atts.getValue("date"),"yyyy-MM-dd HH:mm:ss");
			}catch (IllegalArgumentException e){ // TODO Fehler werfen
				Vm.debug(e.toString());
			}
			// reduce time at 1 second to avoid sync problems
			lastSync.setTime(lastSync.getTime() - 1000);
			dateOfthisSync = lastSync;
			state = STAT_INIT;
		}

		// look for changes in the state
		if (name.equals("cache")) 		{ state = STAT_CACHE; numCacheImported++;}
		if (name.equals("cachedesc")) 	{ state = STAT_CACHE_DESC; numDescImported++;}
		if (name.equals("cachelog")) 	{ state = STAT_CACHE_LOG; numLogImported++; logtype = 0;}
		if (name.equals("picture")) 	{ state = STAT_PICTURE; }

		//examine data
		switch (state) {
		case STAT_CACHE: startCache(name, atts); break;
		case STAT_CACHE_DESC: startCacheDesc(name, atts); break; 
		case STAT_CACHE_LOG: startCacheLog(name, atts); break;
		case STAT_PICTURE: startPicture(name,atts); break;
		}

	}

	public void endElement(String name){
		//examine data
		switch (state) {
		case STAT_CACHE: endCache(name); break;
		case STAT_CACHE_DESC: endCacheDesc(name);break;
		case STAT_CACHE_LOG: endCacheLog(name); break;
		case STAT_PICTURE: endPicture(name); break;
		}

		// look for changes in the state
		if (name.equals("cache")) 		state = STAT_INIT;
		if (name.equals("cachedesc")) 	state = STAT_INIT;
		if (name.equals("cachelog")) 	state = STAT_INIT;
		if (name.equals("picture")) 	state = STAT_INIT;

	}

	public void characters(char[] ch,int start,int length){
		String chars = new String(ch,start,length);
		strData += chars;
		if (debugGPX) Vm.debug(strData);
	}

	private void startCache(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1609,"Importing Cache:")+" " + numCacheImported + "\n");
		if(name.equals("id")){
			cacheID = atts.getValue("id");
		}
		if(name.equals("type")){
			chD.type = CacheType.transOCType(atts.getValue("id"));
			return;
		}
		if(name.equals("status")){
			if(atts.getValue("id").equals("1")) chD.is_available = true;
			if(atts.getValue("id").equals("2")) chD.is_available = false;
			if(atts.getValue("id").equals("3")) {
				chD.is_archived = true;
				chD.is_available = false;
			}
			if(atts.getValue("id").equals("4")) chD.is_available = false;
			return;
		}
		if(name.equals("size")){
			chD.CacheSize = transSize(atts.getValue("id"));
			return;
		}

		if(name.equals("waypoints")){
			chD.wayPoint = atts.getValue("oc");
			if (chD.wayPoint.length()==0) throw new IllegalArgumentException("empty waypointname"); // this should not happen - it is likey a bug in opencaching.de / it happens on 27-12-2006 on cache OC143E
			return;
		}

	}
	private void startCacheDesc(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1611,"Importing cache description:")+" " + numDescImported);
		if (name.equals("cachedesc")){
			ignoreDesc = false;
		}

		if (name.equals("desc")){
			chD.is_HTML = atts.getValue("html").equals("1")?true:false;
		}

		if (name.equals("language") && !atts.getValue("id").equals("DE")){
			if (chD.LongDescription.length()> 0) ignoreDesc = true; // TODO "DE" in preferences adjustable
			else ignoreDesc = false;
		}
	}

	private void startPicture(String name, AttributeList atts){
		if(name.equals("picture")){
			inf.setInfo(MyLocale.getMsg(1613,"Pictures:")+" " + ++picCnt);
		}
	}

	private void startCacheLog(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1612,"Importing Cachlog:")+" " + numLogImported);
		if (name.equals("logtype")){
			logtype = Convert.toInt(atts.getValue("id"));
			switch (logtype) {
			case 1: 
				logIcon = GPXImporter.typeText2Image("Found");
				break;
			case 2:	logIcon = GPXImporter.typeText2Image("Not Found"); 
			chD.noFindLogs += 1;
			break;
			case 3: logIcon = GPXImporter.typeText2Image("Note");
			}
			loggerRecommended = atts.getValue("recommended").equals("1");
			return;
		}
		
		if (name.equals("id")){
			logId = atts.getValue("id");
		}
	}

	private void endCache(String name){
		if (name.equals("cache")){
			chD.lastSyncOC = dateOfthisSync.format("yyyyMMddHHmmss");
			int index;
			index = searchWpt(chD.wayPoint);
			if (index == -1){
				chD.is_new = true;
				CacheHolder ch = new CacheHolder(chD);
				ch.details = chD;
				cacheDB.add(ch);
				ch.detailsAdded();
				Integer indexInt = new Integer(cacheDB.size()-1);
				DBindexWpt.put(chD.wayPoint, indexInt);
				DBindexID.put(chD.ocCacheID, indexInt);
			}
			// update (overwrite) data
			else {
				chD.is_new = false;
				cacheDB.set(index, new CacheHolder(chD));
				// save ocCacheID, in case, the previous data is from GPX
				DBindexID.put(chD.ocCacheID, new Integer(index));
			}
			// clear data (picture, logs) if we do a complete Update
			if (incUpdate == false){
				chD.CacheLogs.clear();
				chD.Images.clear();
				chD.ImagesText.clear();
				chD.ImagesInfo.clear();
			}

			// save all
			chD.hasUnsavedChanges = true; // this makes CachHolder save the details in case that they are unloaded from memory
			// chD.saveCacheDetails(profile.dataDir); 
			// profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR); // this is done after .xml is completly processed
			return;
		}
		if(name.equals("id")){ // </id>
			chD = getHolder(strData); // Allocate a new CacheHolder object
			chD.ocCacheID=strData;
			chD.URL = ocSeekUrl + cacheID;
			return;
		}

		if(name.equals("name")){
			chD.CacheName = strData;
			return;
		}
		if(name.equals("userid")) {
			chD.CacheOwner = strData;
			if(chD.CacheOwner.equalsIgnoreCase(pref.myAlias) || (pref.myAlias2.length()>0 && chD.CacheOwner.equalsIgnoreCase(pref.myAlias2))) chD.is_owned = true;
			return;
		}

		if(name.equals("longitude")){
			longitude = Common.parseDouble(strData);
			return;
		}
		if(name.equals("latitude")) {
			chD.pos.set(Common.parseDouble(strData),longitude);
			chD.LatLon = chD.pos.toString();
			return;
		}
		if(name.equals("difficulty")) {
			chD.hard = strData;
			return;
		}
		if(name.equals("terrain")) {
			chD.terrain = strData;
			return;
		}
		if(name.equals("datehidden")) {
			chD.DateHidden = strData.substring(0,10); //Date;
			return;
		}
		if (name.equals("country")){
			chD.Country = strData;
			return;
		}
	}

	private void endCacheDesc(String name){

		if (!ignoreDesc){
			if (name.equals("cachedesc")){
				if (pref.downloadPicsOC && chD.is_HTML) {
					String fetchUrl, imgTag, imgAltText;
					Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); //  Ergebnis enthält keine Anführungszeichen
					Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get alternative text for Pic
					imgRegexAlt.setIgnoreCase(true);
					imgRegexUrl.setIgnoreCase(true);
					int descIndex=0;
					int numDownloaded=1;
					while (imgRegexUrl.searchFrom(chD.LongDescription, descIndex)) { // "img" found
						imgTag=imgRegexUrl.stringMatched(1); // (1) enthält das gesamte <img ...>-tag
						fetchUrl=imgRegexUrl.stringMatched(2); // URL in Anführungszeichen in (2) falls ohne in (3) Ergebnis ist auf jeden Fall ohne Anführungszeichen 
						if (fetchUrl==null) { fetchUrl=imgRegexUrl.stringMatched(3); }
						if (fetchUrl==null) { // TODO Fehler ausgeben: nicht abgedeckt ist der Fall, dass in einem Cache Links auf Bilder mit unterschiedlichen URL, aber gleichem Dateinamen sind.
							inf.addWarning(MyLocale.getMsg(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache "+chD.wayPoint));
							continue;
						}
						inf.setInfo(MyLocale.getMsg(1611,"Importing cache description:")+" " + numDescImported + "\n"+MyLocale.getMsg(1620, "downloading embedded images: ") + numDownloaded++);
						if (imgRegexAlt.search(imgTag)) {
							imgAltText=imgRegexAlt.stringMatched(1);
							if (imgAltText==null)	imgAltText=imgRegexAlt.stringMatched(2);
							// kein alternativer Text als Bildüberschrift -> Dateiname
						} else { 
							if (fetchUrl.toLowerCase().indexOf("opencaching.de") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0) //wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend 
								imgAltText = new String("No image title");
							else imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf("/")+1);
						}
						descIndex = imgRegexUrl.matchedTo();
						getPic(fetchUrl, imgAltText);
					}
				}
				chD.hasUnsavedChanges = true; //saveCacheDetails(profile.dataDir);
				return;
			}


			if (name.equals("cacheid")){
				// load cachedata
				chD = getHolder(strData);
				chD.is_update = true;
				return;
			}

			if (name.equals("shortdesc")){
				chD.LongDescription = strData;
				return;
			}

			if (name.equals("desc")){ // </desc>
				if (chD.is_HTML)	chD.LongDescription +=SafeXML.cleanback(strData);
				else chD.LongDescription +=strData;
				return;
			}
			if (name.equals("hint")){
				chD.Hints = Common.rot13(strData);
				return;
			}
		}
	}

	private String createPicFilename(String fetchURL) {
		String fileName = chD.wayPoint + "_" + fetchURL.substring(fetchURL.lastIndexOf("/")+1);
		return Common.ClearForFileName(fileName);
	}
	
	private void getPic(String fetchURL, String picDesc) { // TODO handling of relativ URLs
		try {
			if (!fetchURL.startsWith("http://")) fetchURL = new URL(new URL("http://" + OPENCACHING_HOST+"/"), fetchURL).toString(); // TODO this is not quite correct: actually the "base" URL must be known... but anyway a different baseURL should not happen very often  - it doesn't in my area
			String fileName = createPicFilename(fetchURL);
			// add title
			chD.ImagesText.add(picDesc);
			chD.ImagesInfo.add(null); // need to stay in sync with ImagesText
			try {
				File ftest = new File(profile.dataDir + fileName);
				if (ftest.exists()){
					chD.Images.add(fileName);
				}
				else {
					if (pref.downloadPicsOC) {
						chD.Images.add(fetch(fetchURL, fileName));
					}
				}
			} catch (IOException e) {
				String ErrMessage = new String (MyLocale.getMsg(1618,"Ignoring error in cache: ") + chD.wayPoint + ": ignoring IOException: "+e.getMessage()+ " while downloading picture:"+fileName+" from URL:"+fetchURL); 
				if (e.getMessage().toLowerCase().equalsIgnoreCase("could not connect") ||
						e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
					ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ")+chD.CacheName + " ("+chD.wayPoint+")"+MyLocale.getMsg(1619,": could not download image from URL: ")+fetchURL;
				} 
				inf.addWarning("\n"+ErrMessage);
				//(new MessageBox(MyLocale.getMsg(144, "Warning"), ErrMessage, MessageBox.OKB)).exec();
				pref.log(ErrMessage);
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			String ErrMessage = new String (MyLocale.getMsg(1618,"Ignoring error in cache: ") + chD.wayPoint + ": ignoring MalformedUrlException: " + e.getMessage()+ " while downloading from URL:" + fetchURL); 
			inf.addWarning("\n"+ErrMessage);
			pref.log(ErrMessage);
		}

	}


	private void endPicture(String name){

		if(name.equals("id")){
			picID = strData;
			return;
		}

		if (name.equals("url")){
			picUrl = strData;
			return;
		}
		if (name.equals("title")){
			picTitle = strData;
			return;
		}
		if(name.equals("object")){
			// get cachedata
			chD = getHolder(strData);
			return;
		}
		if(name.equals("picture")){ 
			//String fileName = holder.wayPoint + "_" + picUrl.substring(picUrl.lastIndexOf("/")+1);
			getPic(picUrl,picTitle);
			chD.hasUnsavedChanges = true; //saveCacheDetails(profile.dataDir);
			return;
		}
	}

	private void endCacheLog(String name){
		if (name.equals("cachelog")){ // </cachelog>
			chD.CacheLogs.merge(new Log(logIcon, logDate, logFinder, logData, loggerRecommended));
			if((logFinder.toLowerCase().compareTo(user) == 0 || logFinder.equalsIgnoreCase(pref.myAlias2)) && logtype == 1) {
						chD.CacheStatus=logDate;
						chD.is_found=true;
						chD.OwnLogId = logId;
						chD.OwnLogText = logData;
			}
			chD.hasUnsavedChanges = true; //chD.saveCacheDetails(profile.dataDir);
			return;
		}

		if (name.equals("cacheid")){ // </cacheid>
			// load cachedata
			chD = getHolder(strData);
			return;
		}

		if (name.equals("date"))  {
			logDate = new String(strData);
			return;
		}
		if (name.equals("userid")){
			logFinder = new String(strData);
			return;
		}
		if (name.equals("text")){ 
			logData = new String(strData);
			return;
		}

	}

	private String fetch(String addr, String fileName ) throws IOException
	{
		//Vm.debug("Redirect: " + redirect);
		CharArray realurl = new CharArray();
		ByteArray daten = UrlFetcher.fetchByteArray(addr, realurl);
		String address = realurl.toString();
		if (chD != null) fileName = chD.wayPoint + "_" + Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));
		// else fileName = Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));

		//save file
		//Vm.debug("Save: " + myPref.mydatadir + fileName);
		//Vm.debug("Daten: " + daten.length);
		FileOutputStream outp =  new FileOutputStream(profile.dataDir + fileName);
		outp.write(daten.toBytes());
		outp.close();
		return fileName;
	}


	/**
	 *	Method to translate opencaching size types to geocaching types.
	 *	Required to be "backwards" compatible :-(
	 *	OC	GC	Comment	Rule
	 *	1	5	other size	1->5
	 *	2 	1	micro		2->1
	 *	3 	2	small		3->2
	 *	4	3	normal		4->3
	 *	5 	4	large		5->4
	 *	6	6	very large	6->6
	 *	7	7	no container	7->7
	 */
	private String transSize(String type){
		if(type.equals("1")) return "Other";
		if(type.equals("2")) return "Micro";
		if(type.equals("3")) return "Small";
		if(type.equals("4")) return "Regular";
		if(type.equals("5")) return "Large";
		if(type.equals("6")) return "Very Large";
		if(type.equals("7")) return "None";
		return "0";
	}

	/**
	 * Method to iterate through cache database and look for waypoint.
	 * Returns value >= 0 if waypoint is found, else -1
	 */
	private int searchWpt(String wpt){
		Integer INTR = (Integer)DBindexWpt.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}

	/**
	 * Method to iterate through cache database and look for cacheID.
	 * Returns value >= 0 if cacheID is found, else -1
	 */
	private int searchID(String cacheID){
		Integer INTR = (Integer)DBindexID.get(cacheID);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}


	private CacheHolderDetail getHolder(String wpt){// See also LOCXMLImporter
		int index;
		
		index = searchWpt(wpt);
		if (index ==-1) index = searchID(wpt);
		if (index == -1) {
			chD = new CacheHolderDetail();
			return chD;
		}
		chD = ((CacheHolder) cacheDB.get(index)).getCacheDetails(true);
/*		try {
			chD.readCache(profile.dataDir);
		} catch (Exception e) {Vm.debug("Could not open file: " + e.toString());};
	*/	return chD;
	}


}