package CacheWolf;

import com.stevesoft.ewe_pat.Regex;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.util.zip.*;
import ewe.net.*;

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
	CacheHolder holder;
	Preferences pref;
	Profile profile;
	Time dateOfthisSync;
	String strData = new String();
	int picCnt;
	boolean incUpdate = true; // complete or incremental Update
	boolean ignoreDesc = false;
	Hashtable DBindexWpt = new Hashtable();
	Hashtable DBindexID = new Hashtable();

	String picUrl = new String();
	String picTitle =  new String();
	String picID = new String();
	String ocSeekUrl = new String("http://"+OPENCACHING_HOST+"/viewcache.php?cacheid=");
	String cacheID = new String();

	String logData, logIcon, logDate, logFinder;
	int logtype;
	String user;


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
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindexWpt.put((String)ch.wayPoint, new Integer(i));
			DBindexID.put((String)ch.ocCacheID, new Integer(i));
		}//for

	}

	public void doIt(){
		String finalMessage = new String();
		boolean success=true;
		try{
			BufferedReader r;
			String file = new String();
			String url = new String();

			String lastS =  profile.last_sync_opencaching;
			CWPoint center = pref.curCentrePt; // No need to clone curCentrePt as center is only read
			if (!center.isValid()) {
				(new MessageBox("Error", "Coordinates for center must be set", MessageBox.OKB)).execute();
				return;
			}
			OCXMLImporterScreen importOpt = new OCXMLImporterScreen( MyLocale.getMsg(1600, "Opencaching.de Download"),OCXMLImporterScreen.ALL);
			if (importOpt.execute() == OCXMLImporterScreen.IDCANCEL) {	return; }
			Vm.showWait(true);
			String dist = importOpt.distanceInput.getText();
			if (dist.length()== 0) return;
			//check, if distance is greater than before
			if (Convert.toInt(dist) > Convert.toInt(profile.distOC) ||
					pref.downloadmissingOC  ){
				// resysnc
				lastS = "20050801000000";
				incUpdate = false;
			}
			profile.distOC = dist;
			// Clear status of caches in db
			CacheHolder ch;
			for(int i = 0; i<cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				ch.is_update = false;
				ch.is_new = false;
				ch.is_log_update = false;
			}	
			picCnt = 0;
			//Build url
			url ="http://" + OPENCACHING_HOST + "/xml/ocxml11.php?";
			url += "modifiedsince=" + lastS;
			url +="&cache=1";
			url +="&cachedesc=1";
			url +="&picture=1";
			url +="&cachelog=1";
			url +="&removedobject=0";
			url +="&lat=" + center.getLatDeg(CWPoint.DD);
			url +="&lon=" + center.getLonDeg(CWPoint.DD);
			url +="&distance=" + dist;
			url +="&charset=utf-8";
			url +="&cdata=0";
			url +="&session=0";
			inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
			inf.setPreferredSize(220, 300);
			inf.relayout(false);
			inf.exec();
			//Vm.debug(url);
			//get file
			file = fetch(url, "dummy");
			//file = "628-0-1.zip";

			//parse
			File tmpFile = new File(profile.dataDir + file);
			if (tmpFile.getLength() == 0 ) throw new IOException("no updates available");

			ZipFile zif = new ZipFile (profile.dataDir + file);
			ZipEntry zipEnt;
			Enumeration zipEnum = zif.entries();
			// there could be more than one file in the archive
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
			finalMessage = MyLocale.getMsg(1621,"Error parsing update file\n this is likely a bug in opencaching.de\nplease try again later\n, state:")+" "+state+", waypoint: "+ holder.wayPoint;
			success = false;
			Vm.debug("Parse error: " + state + " " + holder.wayPoint);
			e.printStackTrace();
		}catch (Exception e){ // here schould be used the correct exepion
			if (holder != null)	finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: "+ holder.wayPoint;
			else finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: <unkown>";
			success = false;
			Vm.debug("Parse error: " + state + " " + holder.wayPoint);
			e.printStackTrace();
		}
		Vm.showWait(false);
		if (success) {
			profile.last_sync_opencaching = dateOfthisSync.format("yyyyMMddHHmmss");
			//pref.savePreferences();
			profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
			finalMessage = MyLocale.getMsg(1607,"Update from opencaching successful");
		}
		inf.setInfo(finalMessage);
		inf.addOkButton();
		//inf.close(0);
//		MessageBox mb = new MessageBox("Opencaching",finalMessage,MessageBox.OKB);
		//	mb.execute();
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
			holder.type = CacheType.transOCType(atts.getValue("id"));
			return;
		}
		if(name.equals("status")){
			if(atts.getValue("id").equals("1")) holder.is_available = true;
			if(atts.getValue("id").equals("2")) holder.is_available = false;
			if(atts.getValue("id").equals("3")) {
				holder.is_archived = true;
				holder.is_available = false;
			}
			if(atts.getValue("id").equals("4")) holder.is_available = false;
			return;
		}
		if(name.equals("size")){
			holder.CacheSize = transSize(atts.getValue("id"));
			return;
		}

		if(name.equals("waypoints")){
			holder.wayPoint = atts.getValue("oc");
			if (holder.wayPoint.length()==0) throw new IllegalArgumentException("empty waypointname"); // this should not happen - it is likey a bug in opencaching.de / it happens on 27-12-2006 on cache OC143E
			return;
		}

	}
	private void startCacheDesc(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1611,"Importing cache description:")+" " + numDescImported);
		if (name.equals("cachedesc")){
			ignoreDesc = false;
		}

		if (name.equals("desc")){
			holder.is_HTML = atts.getValue("html").equals("1")?true:false;
		}

		if (name.equals("language") && !atts.getValue("id").equals("DE")){
			if (holder.LongDescription.length()> 0) ignoreDesc = true; // TODO "DE" in preferences adjustable
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
				if (logFinder.equalsIgnoreCase(user)) { // see also endCacheLog
					holder.is_found = true;
					holder.CacheStatus = MyLocale.getMsg(318,"Found");
				}
				break;
			case 2:	logIcon = GPXImporter.typeText2Image("Not Found"); 
			holder.noFindLogs += 1;
			break;
			case 3: logIcon = GPXImporter.typeText2Image("Note");
			}
			return;
		}

	}
	private boolean fileExits(String filename) {
		File myfile = new File(filename);
		return myfile.exists();
	}

	private void endCache(String name){
		if (name.equals("cache")){
			int index;
			index = searchWpt(holder.wayPoint);
			if (index == -1){
				holder.is_new = true;
				cacheDB.add(holder);
				DBindexWpt.put((String)holder.wayPoint, new Integer(cacheDB.size()-1));
				DBindexID.put((String)holder.ocCacheID, new Integer(cacheDB.size()-1));
			}
			// update (overwrite) data
			else {
				holder.is_new = false;
				cacheDB.set(index, holder);
				// save ocCacheID, in case, the previous data is from GPX
				DBindexID.put((String)holder.ocCacheID, new Integer(index));
			}
			// clear data (picture, logs) if we do a complete Update
			if (incUpdate == false){
				holder.CacheLogs.clear();
				holder.Images.clear();
				holder.ImagesText.clear();
			}
			/*			if(holder.LatLon.length() > 1 && holder.is_archived == false &&
					pref.downloadMapsOC){

				ParseLatLon pll = new ParseLatLon(holder.LatLon,".");
				pll.parse();

 				MapLoader mpl = new MapLoader(pref.myproxy, pref.myproxyport);
				// MapLoader tests itself if the file already exists and doesnt download if so.
				String filename = Global.getPref().baseDir + "/maps/expedia/" + holder.wayPoint + "_map.gif";
				if (!(new File(filename).getParentFile().isDirectory())) { // dir exists? 
					if (new File(filename).getParentFile().mkdir() == false) // dir creation failed?
					{ pref.downloadMapsOC = false;
					(new MessageBox("Warning", "Ignoring error (stopping to download maps):\n cannot create maps directory: \n"+new File(filename).getParentFile(), MessageBox.OKB)).exec(); 
					}
				}
				if (!fileExits(filename)){
					inf.setInfo(MyLocale.getMsg(1609,"Importing Cache:")+" " + numCacheImported + "\n"+MyLocale.getMsg(1610,"Downloading missing map")+" 1");
					//mpl.loadTo(filename, "3"); 
					}
				//filename = profile.dataDir + "/" + holder.wayPoint + "_map_2.gif";
				filename = Global.getPref().baseDir + "/maps/expedia/" + holder.wayPoint + "_map_2.gif";
				if (!fileExits(filename)){
					inf.setInfo(MyLocale.getMsg(1609,"Importing Cache: ")+" " + numCacheImported + "\n"+MyLocale.getMsg(1610,"Downloading missing map")+" 2");
					//mpl.loadTo(filename, "10"); 
					}
			} */

			// save all
			holder.saveCacheDetails(profile.dataDir);
			profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
			return;
		}
		if(name.equals("id")){
			holder = getHolder(strData);
			holder.URL = ocSeekUrl + cacheID;
			return;
		}

		if(name.equals("name")){
			holder.CacheName = strData;
			return;
		}
		if(name.equals("userid")) {
			holder.CacheOwner = strData;
			if(holder.CacheOwner.equalsIgnoreCase(pref.myAlias) || (pref.myAlias2.length()>0 && holder.CacheOwner.equalsIgnoreCase(pref.myAlias2))) holder.is_owned = true;
			return;
		}

		if(name.equals("longitude")){
			holder.LatLon = GPXImporter.londeg2min(strData);
			return;
		}
		if(name.equals("latitude")) {
			holder.LatLon = GPXImporter.latdeg2min(strData) + " " + holder.LatLon;
			holder.pos.set(holder.LatLon);
			return;
		}
		if(name.equals("difficulty")) {
			holder.hard = strData;
			return;
		}
		if(name.equals("terrain")) {
			holder.terrain = strData;
			return;
		}
		if(name.equals("datehidden")) {
			String Date = new String();
			Date = strData.substring(5,7); // month
			Date += "/" + strData.substring(8,10); // day
			Date += "/" + strData.substring(0,4); // year
			holder.DateHidden = Date;
			return;
		}
	}

	private void endCacheDesc(String name){

		if (!ignoreDesc){
			if (name.equals("cachedesc")){
				if (pref.downloadPicsOC && holder.is_HTML) {
					String fetchUrl, imgTag, imgAltText;
					Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); //  Ergebnis enthält keine Anführungszeichen
					Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get alternative text for Pic
					imgRegexAlt.setIgnoreCase(true);
					imgRegexUrl.setIgnoreCase(true);
					int descIndex=0;
					int numDownloaded=1;
					while (imgRegexUrl.searchFrom(holder.LongDescription, descIndex)) { // "img" found
						imgTag=imgRegexUrl.stringMatched(1); // (1) enthält das gesamte <img ...>-tag
						fetchUrl=imgRegexUrl.stringMatched(2); // URL in Anführungszeichen in (2) falls ohne in (3) Ergebnis ist auf jeden Fall ohne Anführungszeichen 
						if (fetchUrl==null) { fetchUrl=imgRegexUrl.stringMatched(3); }
						if (fetchUrl==null) { // TODO Fehler ausgeben: nicht abgedeckt ist der Fall, dass in einem Cache Links auf Bilder mit unterschiedlichen URL, aber gleichem Dateinamen sind.
							(new MessageBox(MyLocale.getMsg(144, "Warning"),MyLocale.getMsg(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache "+holder.wayPoint), MessageBox.OKB)).exec();
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
				holder.saveCacheDetails(profile.dataDir);
				return;
			}


			if (name.equals("cacheid")){
				// load cachedata
				holder = getHolder(strData);
				holder.is_update = true;
				return;
			}

			if (name.equals("shortdesc")){
				holder.LongDescription = strData;
				return;
			}

			if (name.equals("desc")){
				if (holder.is_HTML)	holder.LongDescription +=SafeXML.cleanback(strData);
				else holder.LongDescription +=strData;
				return;
			}
			if (name.equals("hint")){
				holder.Hints = Common.rot13(strData);
				return;
			}
		}
	}

	private void getPic(String fetchURL, String picDesc){ // TODO handling of relativ URLs
		if (!fetchURL.startsWith("http://")) fetchURL = "http://" + OPENCACHING_HOST + "/"+fetchURL; // TODO this is not quite correct: actually the "base" URL must be known... but anyway a different baseURL should not happen very often  - it doesn't in my area
		String fileName = holder.wayPoint + "_" + fetchURL.substring(fetchURL.lastIndexOf("/")+1);
		fileName = Common.ClearForFileName(fileName);
		// add title
		holder.ImagesText.add(picDesc);
		try {
			File ftest = new File(profile.dataDir + fileName);
			if (ftest.exists()){
				holder.Images.add(fileName);
			}
			else {
				if (pref.downloadPicsOC) {
					holder.Images.add(fetch(fetchURL, fileName));
				}
			}
		} catch (IOException e) {
			String ErrMessage = new String (MyLocale.getMsg(1618,"Ignoring error in cache: ") + holder.wayPoint + ": ignoring IOException: "+e.getMessage()+ " while downloading picture:"+fileName+" from URL:"+fetchURL); 
			if (e.getMessage().toLowerCase().equalsIgnoreCase("could not connect") ||
					e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
				ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ")+holder.CacheName + " ("+holder.wayPoint+")"+MyLocale.getMsg(1619,": could not download image from URL: ")+fetchURL;
			} 
			inf.addWarning("\n"+ErrMessage);
			//(new MessageBox(MyLocale.getMsg(144, "Warning"), ErrMessage, MessageBox.OKB)).exec();
			Vm.debug("Could not load Image " + fetchURL);
			e.printStackTrace();
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
			holder = getHolder(strData);
			return;
		}
		if(name.equals("picture")){ 
			//String fileName = holder.wayPoint + "_" + picUrl.substring(picUrl.lastIndexOf("/")+1);
			getPic(picUrl,picTitle);
			holder.saveCacheDetails(profile.dataDir);
			return;
		}
	}

	private void endCacheLog(String name){
		if (name.equals("cachelog")){
			holder.addLog(logIcon + logDate + " by " + logFinder + "</strong><br>" + logData + "<br>");
			holder.saveCacheDetails(profile.dataDir);
			return;
		}

		if (name.equals("cacheid")){
			// load cachedata
			holder = getHolder(strData);
			return;
		}

		if (name.equals("date"))  {
			logDate = new String(strData);
			return;
		}
		if (name.equals("userid")){
			logFinder = new String(strData);
			if(logFinder.toLowerCase().compareTo(user) == 0 && logtype == 1){
				holder.is_found = true; // see startCacheLog - in the current .xml this is set by startCacheLog but we sequence in the xml from opencaching might change, so I leave this also here
				holder.CacheStatus = MyLocale.getMsg(318,"Found");
			}
			return;
		}
		if (name.equals("text")){ 
			logData = new String(strData);
			return;
		}

	}

	private String fetch(String addr, String fileName ) throws IOException
	{
		final int maxRedirections = 5;
		//Vm.debug(address);
		HttpConnection conn = null;
		Socket sock = null;
		int i=-1;
		String address = new String(addr);
		while (address != null && i <= maxRedirections ) { // allow max 5 redirections (http 302 location)
			i++;
			if(pref.myproxy.length() > 0){
				conn = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), address);
				Vm.debug("Proxy here: " + address);
			} else {
				conn = new HttpConnection(address);
			}
			conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			sock = conn.connect();
			address = conn.getRedirectTo();
			if (address != null){
				if (holder != null) fileName = holder.wayPoint + "_" + Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));
				else fileName = Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));
			}
		}
		if (i > maxRedirections) throw new IOException("too many http redirections while trying to fetch: "+addr + " only "+maxRedirections+" are allowed");
		//Vm.debug("Redirect: " + redirect);
		ByteArray daten = conn.readData(sock);

		//save file
		//Vm.debug("Save: " + myPref.mydatadir + fileName);
		//Vm.debug("Daten: " + daten.length);
		BufferedOutputStream outp =  new BufferedOutputStream(new FileOutputStream(profile.dataDir + fileName));
		outp.write(daten.toBytes());
		outp.close();
		sock.close();
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


	private CacheHolder getHolder(String CacheID){
		int index;
		CacheHolder ch;

		index = searchID(CacheID);
		if (index == -1){
			ch = new CacheHolder();
			ch.ocCacheID = CacheID;
			return ch;
		}
		ch = (CacheHolder) cacheDB.get(index);
		try {
			ch.readCache(profile.dataDir);
		} catch (Exception e) {Vm.debug("Could not open file: " + e.toString());};
		return ch;
	}


}