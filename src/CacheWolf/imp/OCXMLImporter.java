    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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

import ewe.io.BufferedReader;
import ewe.io.File;
import ewe.io.FileOutputStream;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.net.MalformedURLException;
import ewe.net.URL;
import ewe.sys.Convert;
import ewe.sys.Double;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

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
	int numCacheImported, numDescImported, numLogImported = 0;
	int numCacheUpdated, numDescUpdated, numLogUpdated = 0;

	boolean debugGPX = false;
	CacheDB cacheDB;
	InfoBox inf;
	//CacheHolder ch;
	CacheHolder holder;
	Preferences pref;
	Profile profile;
	Time dateOfthisSync;
	String strData = "";
	int picCnt;
	boolean incUpdate = true; // complete or incremental Update
	boolean incFinds = true;
	Hashtable DBindexID = new Hashtable();

	String picUrl = "";
	String picTitle =  "";
	String picID;
	String cacheID;

	String logData, logIcon, logDate, logFinder, logId;
	boolean loggerRecommended;
	int logtype;
	String user;

	/** Temporarly save the values from XML */
	double longitude;
	/** Temporarly save the values from XML: set to the language of the description which is currently parsed */
	String processingDescLang;
	boolean isHTML;
	boolean isSyncSingle; // to load archieved

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
				//DBindexID.put(ch.getOcCacheID(), (Integer)i);
				DBindexID.put(ch.getOcCacheID(), ch.getWayPoint());
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


		picCnt = 0;
		//Build url
		String url = "http://" + hostname + "/xml/ocxml11.php?"
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
		isSyncSingle=true;
		syncOC(url);
		inf.close(0);
		return true;
	}

	public void doIt(){
		boolean success=true;
		String finalMessage;

		String lastS =  profile.getLast_sync_opencaching();
		CWPoint centre = pref.getCurCentrePt(); // No need to clone curCentrePt as centre is only read
		if (!centre.isValid()) {
			(new MessageBox("Error", "Coordinates for centre must be set", FormBase.OKB)).execute();
			return;
		}
		OCXMLImporterScreen importOpt = new OCXMLImporterScreen(
				MyLocale.getMsg(130,"Download from opencaching"),
				OCXMLImporterScreen.ALL |
				OCXMLImporterScreen.DIST |
				OCXMLImporterScreen.IMAGES|
				OCXMLImporterScreen.INCLUDEFOUND|
				OCXMLImporterScreen.HOST);
		if (importOpt.execute() == FormBase.IDCANCEL) {	return; }
		Vm.showWait(true);
		String dist = importOpt.maxDistanceInput.getText();
		incFinds = !importOpt.foundCheckBox.getState();
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
		String url = "http://" + hostname + "/xml/ocxml11.php?"
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

		isSyncSingle=false;
		success = syncOC(url);
		profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
		Vm.showWait(false);
		if (success) {
			profile.setLast_sync_opencaching(dateOfthisSync.format("yyyyMMddHHmmss"));
			//pref.savePreferences();
			finalMessage = MyLocale.getMsg(1607,"Update from opencaching successful");
			inf.addWarning("\nNumber of"+
			"\n...caches new/updated: " + numCacheImported + " / " + numCacheUpdated +
			"\n...cache descriptions new/updated: " + numDescImported +
			"\n...logs new/updated: " + numLogImported);
			inf.setInfo(finalMessage);
		}
		inf.addOkButton();
	}

	private boolean syncOC(String url) {
		boolean success=true;
		File tmpFile = null;
		BufferedReader r;

		//inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);

		picCnt = 0;
		String finalMessage = "";
		try{
			holder = null;
			pref.log(url+"fetching");
			String file = fetch(url, "dummy");

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
			pref.log("Parse error: " + state + " " + holder.getWayPoint(),e,true);
		}catch (Exception e){ // here should be used the correct exception
			if (holder != null)	finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: "+ holder.getWayPoint();
			else finalMessage = MyLocale.getMsg(1615,"Error parsing update file, state:")+" "+state+", waypoint: <unkown>";
			success = false;
			pref.log("",e,true);
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
				pref.log(" Name: " + atts.getName(i)+ " Value: "+atts.getValue(i));
			}
		}
		strData ="";

		if (name.equals("oc11xml")){
			Time lastSync = new Time();
			try {
				lastSync.parse(atts.getValue("date"),"yyyy-MM-dd HH:mm:ss");
			}catch (IllegalArgumentException e){
				pref.log("",e,true);
			}
			// reduce time at 1 second to avoid sync problems
			lastSync.setTime(lastSync.getTime() - 1000);
			dateOfthisSync = lastSync;
			state = STAT_INIT;
		}

		// look for changes in the state
		if (name.equals("cache")) 		{ state = STAT_CACHE; }
		if (name.equals("cachedesc")) 	{ state = STAT_CACHE_DESC;}
		if (name.equals("cachelog")) 	{ state = STAT_CACHE_LOG; logtype = 0;}
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
		if (debugGPX) pref.log(strData,null);
	}

	private void startCache(String name, AttributeList atts){
		if(name.equals("id")){
			cacheID = atts.getValue("id");
		}
		if (holder==null) return;
		inf.setInfo(MyLocale.getMsg(1609,"Importing Cache:")+" " + numCacheImported + " / " + numCacheUpdated + "\n");
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
				if( (atts.getValue("id").equals("3")) || (atts.getValue("id").equals("6"))|| (atts.getValue("id").equals("7")) ) {
					if (!isSyncSingle) {
						holder=null; // holder.setArchived(true);
						numCacheImported--;
					}
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
			String CName = atts.getValue("nccom") + " " + atts.getValue("gccom");
			if (!CName.equals(" ")) {
				holder.setCacheOwner(holder.getCacheOwner() + " / " + CName.trim());
				holder.getCacheDetails(false).attributes.add(7); // wwwlink
				holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
			}
			else {
				holder.getCacheDetails(false).attributes.add(6); // oconly
				holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
			}
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
		if (name.equals("cacheid")){
			cacheID = atts.getValue("id");
			return;
		}

		if (name.equals("desc")){
			isHTML = atts.getValue("html").equals("1")?true:false;
			return;
		}

		if (name.equals("language")) {
			processingDescLang = atts.getValue("id");
			return;
		}
	}

	private void startPicture(String name, AttributeList atts){
	}

	private void startCacheLog(String name, AttributeList atts){
		if (name.equals("id")){
			logId = atts.getValue("id");
			return;
		}
		if (holder==null) return;
		inf.setInfo(MyLocale.getMsg(1612,"Importing Cachlog:")+" " + numLogImported);
		
		if (name.equals("logtype")){
			logtype = Convert.toInt(atts.getValue("id"));
			switch (logtype) {
			case 1:
				logIcon = Log.typeText2Image("Found");
				break;
			case 2:	
				logIcon = Log.typeText2Image("Not Found");
				holder.setNoFindLogs((byte)(holder.getNoFindLogs()+1));
				break;
			case 3: 
				logIcon = Log.typeText2Image("Note");
			}
			loggerRecommended = atts.getValue("recommended").equals("1");
			return;
		}
	}

	private void endCache(String name){
		if(name.equals("id")){ // </id>
			holder = getHolder(strData, true); // Allocate a new CacheHolder object
			holder.setOcCacheID(strData);
			holder.getCacheDetails(false).URL = "http://" + hostname + "/viewcache.php?cacheid=" + cacheID;
			return;
		}
		if (holder == null) return; // id should always be the first for a <cache>
		if (name.equals("cache")){
			holder.setLastSync(dateOfthisSync.format("yyyyMMddHHmmss"));
			int index;
			index = cacheDB.getIndex(holder.getWayPoint());
			if (index == -1){
				numCacheImported++;
				holder.setNew(true);
				cacheDB.add(holder);
				//DBindexID.put(holder.getOcCacheID(), (Integer)cacheDB.size()-1);
				DBindexID.put(holder.getOcCacheID(), holder.getWayPoint());
			}
			// update (overwrite) data
			else {
				numCacheUpdated++;
				holder.setNew(false);
				holder.setIncomplete(false);
				cacheDB.get(index).update(holder);
				// save ocCacheID, in case, the previous data is from GPX
				// DBindexID.put(holder.getOcCacheID(), (Integer)index);
				DBindexID.put(holder.getOcCacheID(), holder.getWayPoint());
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

			holder=null;
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
		if (name.equals("cacheid")){
			holder = getHolder(strData, false);
			return;
		}
		if (holder == null) return;
		if (name.equals("cachedesc")){
			 numDescImported++;
			 holder.setHTML(isHTML);
			if (pref.downloadPics && isHTML) {
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
							imgAltText = "No image title";
						else imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/')+1);
					}
					descIndex = imgRegexUrl.matchedTo();
					getPic(fetchUrl, imgAltText);
				}
			}
			holder.getCacheDetails(false).hasUnsavedChanges = true;
			return;
		}

		if (name.equals("shortdesc")){
			String linebraek;

			if (isHTML)	linebraek = "<br>\n";
			else 					linebraek = "\n";
			
			     // this is set by "hint" a few lines down: if a long description is already updated, then this one is likely to be in another language
			if (holder.is_updated())	holder.getCacheDetails(false).LongDescription += linebraek + processingDescLang + ":" +  linebraek + strData  +  linebraek;
			else 					 	holder.getCacheDetails(false).LongDescription =              processingDescLang + ":" +  linebraek + strData  +  linebraek;
			return;
		}

		if (name.equals("desc")){ // </desc>
			if (isHTML)	holder.getCacheDetails(false).LongDescription +=SafeXML.cleanback(strData);
			else holder.getCacheDetails(false).LongDescription +=strData;
			return;
		}
		if (name.equals("hint")){
			String linebreak;
			if (isHTML)	linebreak = "<br>\n";
			else 					linebreak = "\n";
			if (holder.is_updated())	holder.getCacheDetails(false).Hints += linebreak + "[" + processingDescLang + ":]" +  linebreak + Common.rot13(strData)  +  linebreak;
			else 					 	holder.getCacheDetails(false).Hints =              "[" + processingDescLang + ":]" +  linebreak + Common.rot13(strData)  +  linebreak;
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
			if (!fetchURL.startsWith("http://")) {
				fetchURL = new URL(new URL("http://" + hostname+"/"), fetchURL).toString();
			}
			String fileName = createPicFilename(fetchURL);
			ImageInfo imageInfo = new ImageInfo();
			imageInfo.setURL(fetchURL);
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
				else 												wp = "WP???";
				if (holder != null && holder.getCacheName() != null) n = holder.getCacheName();
				else 												 n = "name???";

				if (e == null) ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: "+fileName+" from URL:"+fetchURL;
				else {
					if (e.getMessage().equalsIgnoreCase("could not connect") ||
							e.getMessage().equalsIgnoreCase("unkown host")) {
						// is there a better way to find out what happened?
						ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ")+ n + " ("+wp+")"+MyLocale.getMsg(1619,": could not download image from URL: ")+fetchURL;
					} else
						ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ")+ n + " ("+wp+"): ignoring IOException: "+e.getMessage()+ " while downloading picture:"+fileName+" from URL:"+fetchURL;
				}
				inf.addWarning("\n"+ErrMessage);
				pref.log(ErrMessage,e,true);
			}
		} catch (MalformedURLException e) {
			String ErrMessage = MyLocale.getMsg(1618,"Ignoring error in cache: ") + holder.getWayPoint() + ": ignoring MalformedUrlException: " + e.getMessage()+ " while downloading from URL:" + fetchURL;
			inf.addWarning("\n"+ErrMessage);
			pref.log(ErrMessage,e);
		}

	}


	private void endPicture(String name){
		if(name.equals("object")){
			holder = getHolder(strData, false);
			return;
		}
		if (holder == null) return;

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
		if(name.equals("picture")){
			inf.setInfo(MyLocale.getMsg(1613,"Pictures:")+" " + ++picCnt);
			//String fileName = holder.wayPoint + "_" + picUrl.substring(picUrl.lastIndexOf("/")+1);
			getPic(picUrl,picTitle);
			holder.getCacheDetails(false).hasUnsavedChanges = true; //saveCacheDetails(profile.dataDir);
			return;
		}
	}

	private void endCacheLog(String name){
		if (name.equals("cacheid")){
			holder = getHolder(strData, false);
			return;
		}
		if (holder == null) return;
		if (name.equals("cachelog")){ // </cachelog>
			if (holder.getCacheDetails(false).CacheLogs.merge(new Log(logIcon, logDate, logFinder, logData, loggerRecommended))> -1) {
				numLogImported++;
				holder.getCacheDetails(false).hasUnsavedChanges = true; //chD.saveCacheDetails(profile.dataDir);
			}
			// 
			if((logFinder.toLowerCase().compareTo(user) == 0 || logFinder.equalsIgnoreCase(pref.myAlias2)) && logtype == 1) {
				if (incFinds || !holder.is_new()) {
					// aber vorhandene werden mit gefunden aktualisiert
					holder.setCacheStatus(logDate);
					holder.setFound(true);
					holder.getCacheDetails(false).OwnLogId = logId;
					holder.getCacheDetails(false).OwnLog = new Log(logIcon, logDate, logFinder, logData, loggerRecommended);
				}
				else {
					//if (holder.is_new())
					cacheDB.removeElementAt(cacheDB.getIndex(holder));
					DBindexID.remove(holder.GetCacheID());
					// und Dateien löschen?
					File tmpFile = new File(profile.dataDir + holder.getWayPoint()+".xml");
					tmpFile.delete();
					// todo: was ist mit den schon heruntergeladenen Bildern?
				}
			}
			return;
		}

		if (name.equals("date"))  {
			logDate = strData;
			return;
		}
		if (name.equals("userid")){
			logFinder = strData;
			return;
		}
		if (name.equals("text")){
			logData = strData;
			return;
		}

	}

	private String fetch(String addr, String fileName ) throws IOException
	{
		CharArray realurl = new CharArray();
		ByteArray daten = UrlFetcher.fetchByteArray(addr, realurl);
		FileOutputStream outp =  new FileOutputStream(profile.dataDir + fileName);
		outp.write(daten.toBytes());
		outp.close();
		return fileName;
	}

	private CacheHolder getHolder(String guid, boolean create){// See also LOCXMLImporter
		CacheHolder ch = null;
		//Integer INTR = (Integer)DBindexID.get(guid);
		String wp = (String)DBindexID.get(guid);
		//if(INTR != null){
		if(wp != null){
			//ch = cacheDB.get(INTR.intValue());
			ch = cacheDB.get(wp);
		} else {
			if (create) ch = new CacheHolder();
		}
		return ch;
	}

}
