package CacheWolf.imp;


import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.ImageInfo;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.MyLocale;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.SafeXML;
import CacheWolf.UrlFetcher;

import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.FileBugfix;

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
 *	Class to import Data from opencaching.
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

	String hostname;

	int state = STAT_INIT;
	int numCacheImported, numDescImported, numLogImported= 0;

	boolean debugGPX = false;
	CacheDB cacheDB;
	InfoBox inf;
	//CacheHolder ch;
	CacheHolder holder;
	Preferences pref;
	Profile profile;
	Time dateOfthisSync;
	String strData = new String();
	int picCnt;
	boolean incUpdate = true; // complete or incremental Update
	Hashtable DBindexID = new Hashtable();

	String picUrl = new String();
	String picTitle =  new String();
	String picID = new String();
	String cacheID = new String();

	String logData, logIcon, logDate, logFinder, logId;
	boolean loggerRecommended;
	int logtype;
	String user;

	/** Temporarly save the values from XML */
	double longitude;
	/** Temporarly save the values from XML: set to the language of the description which is currently parsed */
	String processingDescLang;

	public OCXMLImporter(Preferences p,Profile prof)
	{
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
		if(profile.getLast_sync_opencaching() == null ||
				profile.getLast_sync_opencaching().length() < 12){
			profile.setLast_sync_opencaching("20050801000000");
			incUpdate = false;
		}
		user = p.myAlias.toLowerCase();
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = cacheDB.get(i);
			if (!ch.getOcCacheID().equals(""))
				DBindexID.put(ch.getOcCacheID(), new Integer(i));
		}//for

	}

	/**
	 *
	 * @param number
	 * @param infB
	 * @return true, if some change was made to the cacheDB
	 */
	public boolean syncSingle(int number, InfoBox infB) {
		CacheHolder ch;
		ch = cacheDB.get(number);
		hostname=OC.getOCHostName(ch.getWayPoint());
		holder= null;

		if (infB.isClosed) {
			// there could have been an update before
			return true;
		}

		inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from " + hostname), InfoBox.PROGRESS_WITH_WARNINGS, false);
		inf.setPreferredSize(220, 300);
		inf.relayout(false);
		inf.exec();

		String lastS;
		/** pref.downloadmissingOC = true, if not the last syncdate shall be used,
		 *  but the caches shall be reloaded
		 *  only used in syncSingle  */
		if (pref.downloadMissingOC)  lastS = "20050801000000";
		else {
			if (ch.getLastSync().length() < 14) lastS = "20050801000000";
			else lastS = ch.getLastSync();
		}
		dateOfthisSync = new Time();
		dateOfthisSync.parse(lastS, "yyyyMMddHHmmss");


		String url = new String();
		picCnt = 0;
		//Build url
		url = "http://" + hostname + "/xml/ocxml11.php?"
			+ "modifiedsince=" + lastS
			+ "&cache=1"
			+ "&cachedesc=1";

		if (pref.downloadPics) url += "&picture=1";
		else url += "&picture=0";
		url += "&cachelog=1"
			+ "&removedobject=0"
			+ "&wp=" + ch.getWayPoint()
			+ "&charset=utf-8"
			+ "&cdata=0"
			+ "&session=0";
		ch.setUpdated(false);
		syncOC(url);
		inf.close(0);
		return true;
	}

	public void doIt(){
		boolean success=true;
		String finalMessage;

		String url = new String();

		String lastS =  profile.getLast_sync_opencaching();
		CWPoint centre = pref.getCurCentrePt(); // No need to clone curCentrePt as centre is only read
		if (!centre.isValid()) {
			(new MessageBox("Error", "Coordinates for centre must be set", FormBase.OKB)).execute();
			return;
		}
		OCXMLImporterScreen importOpt = new OCXMLImporterScreen( MyLocale.getMsg(1600," Download") + hostname,
																 OCXMLImporterScreen.ALL | OCXMLImporterScreen.DIST | OCXMLImporterScreen.IMAGES
																 | OCXMLImporterScreen.HOST);
		if (importOpt.execute() == FormBase.IDCANCEL) {	return; }
		Vm.showWait(true);
		String dist = importOpt.maxDistanceInput.getText();

		if (importOpt.domains.getSelectedItem()!=null) {
			hostname = (String)importOpt.domains.getSelectedItem();
			pref.lastOCSite=hostname;
		}

		if (dist.length()== 0) return;

		Double distDouble = new Double();
		distDouble.value = Common.parseDouble(dist);
		dist = distDouble.toString(0, 1, 0).replace(',', '.');
		//check, if distance is greater than before
		if (Convert.toInt(dist) > Convert.toInt(profile.getDistOC()) ||
				pref.downloadMissingOC  ){
			// resysnc
			lastS = "20050801000000";
			incUpdate = false;
		}
		profile.setDistOC(dist);
		// Clear status of caches in db
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i>=0 ;i--){
			ch = cacheDB.get(i);
			ch.setUpdated(false);
			ch.setNew(false);
			ch.setLog_updated(false);
		}
		picCnt = 0;
		//Build url
		url = "http://" + hostname + "/xml/ocxml11.php?"
			+ "modifiedsince=" + lastS
			+ "&cache=1"
			+ "&cachedesc=1";
		if (pref.downloadPics) url += "&picture=1";
		else url += "&picture=0";
		url += "&cachelog=1"
			+ "&removedobject=0"
			+ "&lat=" + centre.getLatDeg(TransformCoordinates.DD)
			+ "&lon=" + centre.getLonDeg(TransformCoordinates.DD)
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
			profile.setLast_sync_opencaching(dateOfthisSync.format("yyyyMMddHHmmss"));
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
			holder = null;
			pref.log(url+"fetching");
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
					finalMessage = MyLocale.getMsg(1616,"Error: could not download update file from " + hostname);
				} else { finalMessage = "IOException: "+e.getMessage(); }
				success = false;
			}
		}catch (IllegalArgumentException e) {
			finalMessage = MyLocale.getMsg(1621,"Error parsing update file\n this is likely a bug in " + hostname + "\nplease try again later\n, state:")+" "+state+", waypoint: "+ holder.getWayPoint();
			success = false;
			Vm.debug("Parse error: " + state + " " + holder.getWayPoint());
			e.printStackTrace();
		}catch (Exception e){ // here should be used the correct exception
			if (holder != null)	finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: "+ holder.getWayPoint();
			else finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: <unkown>";
			success = false;
			//Vm.debug("Parse error: " + state + " Exception:" + e.toString()+"   "+holder.getOcCacheID());
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

	public void characters(char[] ch2,int start,int length){
		String chars = new String(ch2,start,length);
		strData += chars;
		if (debugGPX) Vm.debug(strData);
	}

	private void startCache(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1609,"Importing Cache:")+" " + numCacheImported + "\n");
		if(name.equals("id")){
			cacheID = atts.getValue("id");
		}
		if(name.equals("type")){
			holder.setType(CacheType.ocType2CwType(atts.getValue("id")));
			holder.getCacheDetails(false).attributes.clear();
			return;
		}
		if(name.equals("status")){
            // meaning of OC status :
			//  1=Kann gesucht werden ;
			//  2=Momentan nicht verfügbar ;
			//  3=Archiviert ;
			//  4= ;
			//  5= ;
			//  6=Gesperrt ;
			//  are there more ? ;
			if (atts.getValue("id").equals("1")) {
				holder.setAvailable(true);
			} else {
				holder.setAvailable(false);
				if(atts.getValue("id").equals("3")) {
					holder.setArchived(true);
				}
			}
			return;
		}
		if(name.equals("size")){
			holder.setCacheSize(CacheSize.ocXmlString2Cw(atts.getValue("id")));
			return;
		}

		if(name.equals("waypoints")){
			holder.setWayPoint(atts.getValue("oc"));
			if (holder.getWayPoint().length()==0) throw new IllegalArgumentException("empty waypointname"); // this should not happen - it is likey a bug in opencaching / it happens on 27-12-2006 on cache OC143E
			return;
		}
		
		if (name.equals("attribute")) {
			int id = Integer.parseInt(atts.getValue("id"));
			holder.getCacheDetails(false).attributes.add(id);
			holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
			return;
		}

	}


	private void startCacheDesc(String name, AttributeList atts){
		inf.setInfo(MyLocale.getMsg(1611,"Importing cache description:")+" " + numDescImported);
		if (name.equals("cachedesc")){
		}

		if (name.equals("desc")){
			holder.setHTML(atts.getValue("html").equals("1")?true:false);
		}

		if (name.equals("language")) {
			processingDescLang = atts.getValue("id");
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
			case 2:	
				logIcon = GPXImporter.typeText2Image("Not Found");
				holder.setNoFindLogs((byte)(holder.getNoFindLogs()+1));
				break;
			case 3: 
				logIcon = GPXImporter.typeText2Image("Note");
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
			holder.setLastSync(dateOfthisSync.format("yyyyMMddHHmmss"));
			int index;
			index = cacheDB.getIndex(holder.getWayPoint());
			if (index == -1){
				holder.setNew(true);
				cacheDB.add(holder);
				Integer indexInt = new Integer(cacheDB.size()-1);
				DBindexID.put(holder.getOcCacheID(), indexInt);
			}
			// update (overwrite) data
			else {
				holder.setNew(false);
				holder.setIncomplete(false);
				cacheDB.get(index).update(holder);
				// save ocCacheID, in case, the previous data is from GPX
				DBindexID.put(holder.getOcCacheID(), new Integer(index));
			}
			// clear data (picture, logs) if we do a complete Update
			if (incUpdate == false){
				holder.getCacheDetails(false).CacheLogs.clear();
				holder.getCacheDetails(false).images.clear();
			}

			// save all
			holder.getCacheDetails(false).hasUnsavedChanges = true; // this makes CachHolder save the details in case that they are unloaded from memory
			// chD.saveCacheDetails(profile.dataDir);
			// profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR); // this is done after .xml is completly processed
			return;
		}
		if(name.equals("id")){ // </id>
			holder = getHolder(strData); // Allocate a new CacheHolder object
			holder.setOcCacheID(strData);
			String ocSeekUrl = new String("http://" + hostname + "/viewcache.php?cacheid=");
			holder.getCacheDetails(false).URL = ocSeekUrl + cacheID;
			return;
		}

		if(name.equals("name")){
			holder.setCacheName(strData);
			return;
		}
		if(name.equals("userid")) {
			holder.setCacheOwner(strData);
			if(holder.getCacheOwner().equalsIgnoreCase(pref.myAlias) || (pref.myAlias2.length()>0 && holder.getCacheOwner().equalsIgnoreCase(pref.myAlias2))) holder.setOwned(true);
			return;
		}

		if(name.equals("longitude")){
			longitude = Common.parseDouble(strData);
			return;
		}
		if(name.equals("latitude")) {
			holder.pos.set(Common.parseDouble(strData),longitude);
			holder.setLatLon(holder.pos.toString());
			return;
		}
		if(name.equals("difficulty")) {
			holder.setHard(CacheTerrDiff.v1Converter(strData));
			return;
		}
		if(name.equals("terrain")) {
			holder.setTerrain(CacheTerrDiff.v1Converter(strData));
			return;
		}
		if(name.equals("datehidden")) {
			holder.setDateHidden(strData.substring(0,10)); //Date;
			return;
		}
		if (name.equals("country")){
			holder.getCacheDetails(false).Country = strData;
			return;
		}
	}

	private void endCacheDesc(String name){
		if (name.equals("cachedesc")){
			if (pref.downloadPics && holder.is_HTML()) {
				String fetchUrl, imgTag, imgAltText;
				Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); //  Ergebnis enthlt keine Anfhrungszeichen
				Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get alternative text for Pic
				imgRegexAlt.setIgnoreCase(true);
				imgRegexUrl.setIgnoreCase(true);
				int descIndex=0;
				int numDownloaded=1;
				while (imgRegexUrl.searchFrom(holder.getCacheDetails(false).LongDescription, descIndex)) { // "img" found
					imgTag=imgRegexUrl.stringMatched(1); // (1) enthlt das gesamte <img ...>-tag
					fetchUrl=imgRegexUrl.stringMatched(2); // URL in Anfhrungszeichen in (2) falls ohne in (3) Ergebnis ist auf jeden Fall ohne Anfhrungszeichen
					if (fetchUrl==null) { fetchUrl=imgRegexUrl.stringMatched(3); }
					if (fetchUrl==null) { // TODO Fehler ausgeben: nicht abgedeckt ist der Fall, dass in einem Cache Links auf Bilder mit unterschiedlichen URL, aber gleichem Dateinamen sind.
						inf.addWarning(MyLocale.getMsg(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache "+holder.getWayPoint()));
						continue;
					}
					inf.setInfo(MyLocale.getMsg(1611,"Importing cache description:")+" " + numDescImported + "\n"+MyLocale.getMsg(1620, "downloading embedded images: ") + numDownloaded++);
					if (imgRegexAlt.search(imgTag)) {
						imgAltText=imgRegexAlt.stringMatched(1);
						if (imgAltText==null)	imgAltText=imgRegexAlt.stringMatched(2);
						// no alternative text as image title -> use filename
					} else {
						if (fetchUrl.toLowerCase().indexOf("opencaching.") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0) //wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend
							imgAltText = new String("No image title");
						else imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/')+1);
					}
					descIndex = imgRegexUrl.matchedTo();
					getPic(fetchUrl, imgAltText);
				}
			}
			holder.getCacheDetails(false).hasUnsavedChanges = true;
			return;
		}


		if (name.equals("cacheid")){
			// load cachedata
			holder = getHolder(strData);
			return;
		}

		if (name.equals("shortdesc")){
			String linebraek;

			if (holder.is_HTML())	linebraek = "<br>\n";
			else 					linebraek = "\n";
			
			     // this is set by "hint" a few lines down: if a long description is already updated, then this one is likely to be in another language
			if (holder.is_updated())	holder.getCacheDetails(false).LongDescription += linebraek + processingDescLang + ":" +  linebraek + strData  +  linebraek;
			else 					 	holder.getCacheDetails(false).LongDescription =              processingDescLang + ":" +  linebraek + strData  +  linebraek;
			return;
		}

		if (name.equals("desc")){ // </desc>
			if (holder.is_HTML())	holder.getCacheDetails(false).LongDescription +=SafeXML.cleanback(strData);
			else holder.getCacheDetails(false).LongDescription +=strData;
			return;
		}
		if (name.equals("hint")){
			holder.getCacheDetails(false).Hints = Common.rot13(strData);
			holder.setUpdated(true); // remark: this is used in "shortdesc" to decide weather the description should be appended or replaced
			return;
		}
	}

	private String createPicFilename(String fetchURL) {
		String fileName = holder.getWayPoint() + "_" + fetchURL.substring(fetchURL.lastIndexOf('/')+1);
		return Common.ClearForFileName(fileName).toLowerCase();
	}

	private void getPic(String fetchURL, String picDesc) { // TODO handling of relativ URLs
		try {
			//TODO this is not quite correct: actually the "base" URL must be known...
			// but anyway a different baseURL should not happen very often  - it doesn't in my area
			if (!fetchURL.startsWith("http://")) fetchURL = new URL(
				new URL("http://" + hostname+"/"), fetchURL).toString();
			String fileName = createPicFilename(fetchURL);
			ImageInfo imageInfo = new ImageInfo();
			// add title
			imageInfo.setTitle(picDesc);
			holder.getCacheDetails(false).images.add(imageInfo);
			try {
				File ftest = new FileBugfix(profile.dataDir + fileName);
				if (ftest.exists()){
					imageInfo.setFilename(fileName);
				}
				else {
					if (pref.downloadPics) {
						imageInfo.setFilename(fetch(fetchURL, fileName));
					}
				}
			} catch (IOException e) {
				String ErrMessage;
				String wp, n;
				if (holder != null && holder.getWayPoint() != null) wp = holder.getWayPoint();
				else 												wp = new String("WP???");
				if (holder != null && holder.getCacheName() != null) n = holder.getCacheName();
				else 												 n = new String("name???");

				if (e == null) ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: "+fileName+" from URL:"+fetchURL;
				else {
					if (e.getMessage().equalsIgnoreCase("could not connect") ||
							e.getMessage().equalsIgnoreCase("unkown host")) {
						// is there a better way to find out what happened?
						ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ")+ n + " ("+wp+")"+MyLocale.getMsg(1619,": could not download image from URL: ")+fetchURL;
					} else
						ErrMessage = new String (MyLocale.getMsg(1618,"Ignoring error in cache: ")+ n + " ("+wp+"): ignoring IOException: "+e.getMessage()+ " while downloading picture:"+fileName+" from URL:"+fetchURL);
				}
				inf.addWarning("\n"+ErrMessage);
				//(new MessageBox(MyLocale.getMsg(144, "Warning"), ErrMessage, MessageBox.OKB)).exec();
				pref.log(ErrMessage);
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			String ErrMessage = new String (MyLocale.getMsg(1618,"Ignoring error in cache: ") + holder.getWayPoint() + ": ignoring MalformedUrlException: " + e.getMessage()+ " while downloading from URL:" + fetchURL);
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
			holder = getHolder(strData);
			return;
		}
		if(name.equals("picture")){
			//String fileName = holder.wayPoint + "_" + picUrl.substring(picUrl.lastIndexOf("/")+1);
			getPic(picUrl,picTitle);
			holder.getCacheDetails(false).hasUnsavedChanges = true; //saveCacheDetails(profile.dataDir);
			return;
		}
	}

	private void endCacheLog(String name){
		if (name.equals("cachelog")){ // </cachelog>
			holder.getCacheDetails(false).CacheLogs.merge(new Log(logIcon, logDate, logFinder, logData, loggerRecommended));
			if((logFinder.toLowerCase().compareTo(user) == 0 || logFinder.equalsIgnoreCase(pref.myAlias2)) && logtype == 1) {
						holder.setCacheStatus(logDate);
						holder.setFound(true);
						holder.getCacheDetails(false).OwnLogId = logId;
						holder.getCacheDetails(false).OwnLog = new Log(logIcon, logDate, logFinder, logData, loggerRecommended);
			}
			holder.getCacheDetails(false).hasUnsavedChanges = true; //chD.saveCacheDetails(profile.dataDir);
			return;
		}

		if (name.equals("cacheid")){ // </cacheid>
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
		// String address = realurl.toString();
		// if (holder != null) fileName = holder.getWayPoint() + "_" + Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));
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
	 * Method to iterate through cache database and look for cacheID.
	 * Returns value >= 0 if cacheID is found, else -1
	 */
	private int searchID(String cacxheID){
		Integer INTR = (Integer)DBindexID.get(cacxheID);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}


	private CacheHolder getHolder(String wpt){// See also LOCXMLImporter
		CacheHolder chx;
		int index;

		index = cacheDB.getIndex(wpt);
		if (index == -1) index = searchID(wpt);
		if (index == -1) {
			chx = new CacheHolder();
		} else {
			chx = cacheDB.get(index);
		}
		return chx;
	}

}
