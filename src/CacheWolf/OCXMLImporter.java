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
	
	int state = STAT_INIT;
	int numCacheImported, numDescImported, numLogImported= 0;
	
	boolean debugGPX = false;
	Vector cacheDB;
	InfoBox inf;
	CacheHolder holder;
	Preferences myPref = new Preferences();
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
	String ocSeekUrl = new String("http://www.opencaching.de/viewcache.php?cacheid=");
	String cacheID = new String();
	
	String logData, logIcon, logDate, logFinder;
	String user;
	

	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public OCXMLImporter(Vector DB, Preferences pf)
	{
		cacheDB = DB;
		myPref = pf;
		if(myPref.last_sync_opencaching == null ||
			myPref.last_sync_opencaching.length() < 12){
			myPref.last_sync_opencaching = "20050801000000";
			incUpdate = false;
		}
		user = pf.myAlias.toLowerCase();
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
			
			String lastS =  myPref.last_sync_opencaching;
			CWPoint center = new CWPoint(myPref.mylgNS, myPref.mylgDeg, myPref.mylgMin,"0",
					myPref.mybrWE, myPref.mybrDeg, myPref.mybrMin,"0", CWPoint.DMM);

			OCXMLImporterScreen importOpt = new OCXMLImporterScreen(myPref, MyLocale.getMsg(1600, "Opencaching.de Download"),OCXMLImporterScreen.ALL);
			if (importOpt.execute() == OCXMLImporterScreen.IDCANCEL) {	return; }
    		Vm.showWait(true);
			String dist = importOpt.distanceInput.getText();
			if (dist.length()== 0) return;
			//check, if distance is greater than before
			if (Convert.toInt(dist) > Convert.toInt(myPref.distOC) ||
			  myPref.downloadmissingOC  ){
				// resysnc
				lastS = "20050801000000";
				incUpdate = false;
			}
			myPref.distOC = dist;
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
			url ="http://www.opencaching.de/xml/ocxml11.php?";
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
			inf = new InfoBox("Opencaching download", (String)lr.get(1608,"downloading data\n from opencaching"));
			inf.setPreferredSize(210, 120);
			inf.exec();
			//Vm.debug(url);
			//get file
			file = fetch(url, "dummy");
			//file = "628-0-1.zip";
			
			//parse
			File tmpFile = new File(myPref.mydatadir + file);
			if (tmpFile.getLength() == 0 ) throw new IOException("no updates available");
			
			ZipFile zif = new ZipFile (myPref.mydatadir + file);
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
			finalMessage=(String)lr.get(1614,"Error while unzipping udpate file");
			success = false;
		}catch (IOException e){
			if (e.getMessage().equalsIgnoreCase("no updates available")) { finalMessage="No updates available"; success = false; }
			else {
			if (e.getMessage().equalsIgnoreCase("could not connect") ||
					e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
				finalMessage=(String)lr.get(1616,"Error: could not download udpate file from opencaching.de");
			} else { finalMessage = "IOException: "+e.getMessage(); }
			success = false;
			}
		}catch (Exception e){ // here schould be used the correct exepion
			finalMessage=(String)lr.get(1615,"Error parsing update file, state:")+" "+state+", waypoint: "+ holder.wayPoint;
			success = false;
			Vm.debug("Parse error: " + state + " " + holder.wayPoint);
			e.printStackTrace();
		}
		Vm.showWait(false);
		if (success) {
			// @TODO: this should be saved in the index.xml not with the profiles!
			myPref.last_sync_opencaching = dateOfthisSync.format("yyyyMMddHHmmss");
			myPref.savePreferences();
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveIndex(cacheDB, myPref.mydatadir);
			finalMessage=(String)lr.get(1607,"Update from opencaching successful");
		}
		inf.close(0);
		MessageBox mb = new MessageBox("Opencaching",finalMessage,MessageBox.OKB);
		mb.execute();
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
		if (name.equals("cachelog")) 	{ state = STAT_CACHE_LOG; numLogImported++;}
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
		inf.setInfo((String)lr.get(1609,"Importing Cache:")+" " + numCacheImported + "\n");
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
			return;
		}

	}
	private void startCacheDesc(String name, AttributeList atts){
		inf.setInfo((String)lr.get(1611,"Importing cache description:")+" " + numDescImported);
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
			inf.setInfo((String)lr.get(1613,"Pictures:")+" " + ++picCnt);
		}
	}

	private void startCacheLog(String name, AttributeList atts){
		inf.setInfo((String)lr.get(1612,"Importing Cachlog:")+" " + numLogImported);
		if (name.equals("logtype")){
			if(atts.getValue("id").equals("1")) logIcon = GPXImporter.typeText2Image("Found");
			if(atts.getValue("id").equals("2")) {
				logIcon = GPXImporter.typeText2Image("Not Found");
				holder.noFindLogs += 1;
			}
			if(atts.getValue("id").equals("3")) logIcon = GPXImporter.typeText2Image("Note");
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
			if(holder.LatLon.length() > 1 && holder.is_archived == false &&
					myPref.downloadMapsOC){
				
				ParseLatLon pll = new ParseLatLon(holder.LatLon,".");
				pll.parse();
				MapLoader mpl = new MapLoader(pll.getLatDeg(),pll.getLonDeg(), myPref.myproxy, myPref.myproxyport);
				// MapLoader tests itself if the file already exists and doesnt download if so.
				String filename = myPref.mydatadir + "/" + holder.wayPoint + "_map.gif";
				if (!fileExits(filename)){
					inf.setInfo((String)lr.get(1609,"Importing Cache:")+" " + numCacheImported + "\n"+(String)lr.get(1610,"Downloading missing map")+" 1");
					mpl.loadTo(filename, "3"); }
				filename = myPref.mydatadir + "/" + holder.wayPoint + "_map_2.gif";
				if (!fileExits(filename)){
					inf.setInfo((String)lr.get(1609,"Importing Cache: ")+" " + numCacheImported + "\n"+(String)lr.get(1610,"Downloading missing map")+" 2");
					mpl.loadTo(filename, "10"); }
			}
			// save all
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveCacheDetails(holder,myPref.mydatadir);
			crw.saveIndex(cacheDB,myPref.mydatadir);
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
			return;
		}

		if(name.equals("longitude")){
			holder.LatLon = GPXImporter.londeg2min(strData);
			return;
		}
		if(name.equals("latitude")) {
			holder.LatLon = GPXImporter.latdeg2min(strData) + " " + holder.LatLon;
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
				if (myPref.downloadPicsOC && holder.is_HTML) {
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
							(new MessageBox((String)lr.get(144, "Warning"),(String)lr.get(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache "+holder.wayPoint), MessageBox.OKB)).exec();
							continue;
						}
						inf.setInfo((String)lr.get(1611,"Importing cache description:")+" " + numDescImported + "\n"+(String)lr.get(1620, "downloading embedded images: ") + numDownloaded++);
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
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveCacheDetails(holder,myPref.mydatadir);
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
				holder.LongDescription += strData;
				return;
			}
			if (name.equals("hint")){
				holder.Hints = Common.rot13(strData);
				return;
			}
		}
	}
	
	private void getPic(String fetchURL, String picDesc){
		String fileName = holder.wayPoint + "_" + fetchURL.substring(fetchURL.lastIndexOf("/")+1);
		// add title
		holder.ImagesText.add(picDesc);
		try {
			File ftest = new File(myPref.mydatadir + fileName);
			if (ftest.exists()){
				holder.Images.add(fileName);
			}
			else {
				if (myPref.downloadPicsOC) {
					holder.Images.add(fetch(fetchURL, fileName));
				}
			}
		} catch (IOException e) {
			String ErrMessage = new String ("Ignoring IOException: "+e.getMessage())+ "\nwhile downloading picture:"+fileName+"\nfrom URL:"+fetchURL+"\nin cache: "+holder.wayPoint; 
			if (e.getMessage().toLowerCase().equalsIgnoreCase("could not connect") ||
					e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
				ErrMessage=(String)lr.get(1618,"Ignoring error in cache ")+holder.wayPoint+(String)lr.get(1619,": could not download image from URL: ")+fetchURL;
			} 
			(new MessageBox((String)lr.get(144, "Warning"), ErrMessage, MessageBox.OKB)).exec();
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
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveCacheDetails(holder,myPref.mydatadir);
			return;
		}
	}

	private void endCacheLog(String name){
		if (name.equals("cachelog")){
			holder.addLog(logIcon + logDate + " by " + logFinder + "</strong><br>" + logData + "<br>");
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveCacheDetails(holder,myPref.mydatadir);
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
			if(logFinder.toLowerCase().compareTo(user) == 0) holder.is_found = true;
			return;
		}
		if (name.equals("text")){ 
			logData = new String(strData);
			return;
		}
		
	}
	
	private String fetch(String address, String fileName ) throws IOException
	   	{
			//Vm.debug(address);
			String redirect;

			HttpConnection conn, fileConn;
			
			if(myPref.myproxy.length() > 0){
				conn = new HttpConnection(myPref.myproxy, Convert.parseInt(myPref.myproxyport), address);
				Vm.debug("Proxy here: " + address);
			} else {
				conn = new HttpConnection(address);
			}
			conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			Socket sock = conn.connect();
			redirect = conn.getRedirectTo();
			if (redirect != null){
				//Vm.debug("Redirect: " + redirect);
				fileConn = conn.getRedirectedConnection(redirect);
			}
			else {
				fileConn = conn;
			}
			sock = fileConn.connect();
			ByteArray daten = fileConn.readData(sock);
			
			//save file
			if (redirect != null){
				fileName = redirect.substring(redirect.lastIndexOf("/")+1);
			}
			//Vm.debug("Save: " + myPref.mydatadir + fileName);
			//Vm.debug("Daten: " + daten.length);
			BufferedOutputStream outp =  new BufferedOutputStream(new FileOutputStream(myPref.mydatadir + fileName));
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
		CacheReaderWriter crw = new CacheReaderWriter();
		ch = (CacheHolder) cacheDB.get(index);
		try {
			crw.readCache(ch, myPref.mydatadir);
		} catch (Exception e) {Vm.debug("Could not open file: " + e.toString());};
		return ch;
	}

	
}