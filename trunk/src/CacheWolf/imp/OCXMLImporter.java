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
package CacheWolf.imp;

import CacheWolf.MainForm;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheImage;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.database.Log;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.UrlFetcher;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.BufferedReader;
import ewe.io.File;
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
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import Data from opencaching. It uses the lastmodified parameter to identify new or changed caches. See here: http://www.opencaching.com/phpBB2/viewtopic.php?t=281 (out-dated) See here: http://www.opencaching.de/doc/xml/xml11.htm and
 * http://develforum.opencaching.de/viewtopic.php?t=135&postdays=0&postorder=asc&start=0 for more information.
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
    // CacheHolder ch;
    CacheHolder holder;
    Time dateOfthisSync;
    String strData = "";
    int picCnt;
    boolean incUpdate = true; // complete or incremental Update
    boolean incFinds = true;
    Hashtable DBindexID = new Hashtable();

    String picUrl = "";
    String picTitle = "";
    String picID;
    String cacheID;

    String logData, logIcon, logDate, logFinder, logId, finderID;
    boolean loggerRecommended;
    int logtype;
    String user;

    /** Temporarly save the values from XML */
    double longitude;
    /** Temporarly save the values from XML: set to the language of the description which is currently parsed */
    String processingDescLang;
    boolean isHTML;
    boolean isSyncSingle; // to load archieved
    boolean downloadPics = true;

    public OCXMLImporter() {
	cacheDB = MainForm.profile.cacheDB;
	incUpdate = true;
	if (MainForm.profile.getLast_sync_opencaching() == null || MainForm.profile.getLast_sync_opencaching().length() < 12) {
	    MainForm.profile.setLast_sync_opencaching("20050801000000");
	    incUpdate = false;
	}
	user = Preferences.itself().myAlias.toLowerCase();
	CacheHolder ch;
	for (int i = 0; i < cacheDB.size(); i++) {
	    ch = cacheDB.get(i);
	    if (!ch.getOcCacheID().equals(""))
		DBindexID.put(ch.getOcCacheID(), ch.getWayPoint());
	}// for

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
	hostname = OC.getOCHostName(ch.getWayPoint());
	holder = null;

	if (infB.isClosed()) {
	    // there could have been an update before
	    return true;
	}

	inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608, "downloading data\n from " + hostname), InfoBox.PROGRESS_WITH_WARNINGS, false);
	inf.setPreferredSize(220, 300);
	inf.relayout(false);
	inf.exec();

	String lastS;
	/**
	 * Preferences.itself().downloadmissingOC = true, if not the last syncdate shall be used, but the caches shall be reloaded only used in syncSingle
	 */
	incUpdate = false;
	if (Preferences.itself().downloadAllOC)
	    lastS = "20050801000000";
	else {
	    if (ch.getLastSync().length() < 14)
		lastS = "20050801000000";
	    else {
		lastS = ch.getLastSync();
		incUpdate = true;
	    }
	}
	dateOfthisSync = new Time();
	dateOfthisSync.parse(lastS, "yyyyMMddHHmmss");

	picCnt = 0;
	// Build url
	String url = "http://" + hostname + "/xml/ocxml11.php?" + "modifiedsince=" + lastS + "&cache=1" + "&cachedesc=1";

	if (downloadPics)
	    url += "&picture=1";
	else
	    url += "&picture=0";
	url += "&cachelog=1" + "&removedobject=0" + "&wp=" + ch.getWayPoint() + "&charset=utf-8" + "&cdata=0" + "&session=0";
	ch.setUpdated(false);
	isSyncSingle = true;
	syncOC(url);
	inf.close(0);
	return true;
    }

    public void doIt() {
	boolean success = true;
	String finalMessage;

	String lastS = MainForm.profile.getLast_sync_opencaching();
	final CWPoint centre = Preferences.itself().curCentrePt; // No need to clone curCentrePt as centre is only read
	if (!centre.isValid()) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "Coordinates for centre must be set").wait(FormBase.OKB);
	    return;
	}
	final ImportGui importGui = new ImportGui(MyLocale.getMsg(130, "Download from opencaching"), ImportGui.ALL | ImportGui.DIST | ImportGui.IMAGES | ImportGui.INCLUDEFOUND | ImportGui.HOST);
	if (importGui.execute() == FormBase.IDCANCEL) {
	    return;
	}
	downloadPics = importGui.downloadPics;
	Vm.showWait(true);
	String dist = importGui.maxDistanceInput.getText();
	incFinds = !importGui.foundCheckBox.getState();
	if (importGui.domains.getSelectedItem() != null) {
	    hostname = (String) importGui.domains.getSelectedItem();
	    Preferences.itself().lastOCSite = hostname;
	}

	if (dist.length() == 0)
	    return;

	final Double distDouble = new Double();
	distDouble.value = Common.parseDouble(dist);
	dist = distDouble.toString(0, 1, 0).replace(',', '.');
	// check, if distance is greater than before
	incUpdate = true;
	if (Convert.toInt(dist) > Convert.toInt(MainForm.profile.getDistOC()) || Preferences.itself().downloadAllOC) {
	    // resysnc
	    lastS = "20050801000000";
	    incUpdate = false;
	}
	MainForm.profile.setDistOC(dist);
	// Clear status of caches in db
	CacheHolder ch;
	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    ch = cacheDB.get(i);
	    ch.setUpdated(false);
	    ch.setNew(false);
	    ch.setLog_updated(false);
	}
	picCnt = 0;
	// Build url
	String url = "http://" + hostname + "/xml/ocxml11.php?" + "modifiedsince=" + lastS + "&cache=1" + "&cachedesc=1";
	if (downloadPics)
	    url += "&picture=1";
	else
	    url += "&picture=0";
	url += "&cachelog=1" + "&removedobject=0" + "&lat=" + centre.getLatDeg(TransformCoordinates.DD) + "&lon=" + centre.getLonDeg(TransformCoordinates.DD) + "&distance=" + dist + "&charset=utf-8" + "&cdata=0" + "&session=0";
	inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608, "downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
	inf.setPreferredSize(220, 300);
	inf.relayout(false);
	inf.exec();

	isSyncSingle = false;
	success = syncOC(url);
	MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
	Vm.showWait(false);
	if (success) {
	    MainForm.profile.setLast_sync_opencaching(dateOfthisSync.format("yyyyMMddHHmmss"));
	    // Preferences.itself().savePreferences();
	    finalMessage = MyLocale.getMsg(1607, "Update from opencaching successful");
	    inf.addWarning("Number of" + "\n...caches new/updated: " + numCacheImported + " / " + numCacheUpdated + "\n...cache descriptions new/updated: " + numDescImported + "\n...logs new/updated: " + numLogImported);
	    inf.setInfo(finalMessage);
	}
	inf.showButton(FormBase.YESB);
    }

    private boolean syncOC(String address) {
	boolean success = true;
	File tmpFile = null;
	BufferedReader r;

	// inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,"downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);

	picCnt = 0;
	String finalMessage = "";
	try {
	    holder = null;
	    final String target = MainForm.profile.dataDir + "dummy.zip";
	    UrlFetcher.fetchDataFile(address, target);

	    // parse
	    tmpFile = new File(target);
	    if (tmpFile.getLength() == 0) {
		throw new IOException("no updates available");
	    }

	    final ZipFile zif = new ZipFile(target);
	    ZipEntry zipEnt;
	    final Enumeration zipEnum = zif.entries();
	    inf.setInfo("...unzipping update file");
	    while (zipEnum.hasMoreElements()) {
		zipEnt = (ZipEntry) zipEnum.nextElement();
		// skip over PRC-files and empty files
		if (zipEnt.getSize() > 0 && zipEnt.getName().endsWith("xml")) {
		    r = new BufferedReader(new InputStreamReader(zif.getInputStream(zipEnt), IO.JAVA_UTF8_CODEC));
		    parse(r);
		    r.close();
		}
	    }
	    zif.close();
	} catch (final ZipException e) {
	    finalMessage = MyLocale.getMsg(1614, "Error while unzipping udpate file");
	    success = false;
	} catch (final IOException e) {
	    if (e.getMessage().equalsIgnoreCase("no updates available")) {
		finalMessage = "No updates available";
		success = false;
	    } else {
		if (e.getMessage().equalsIgnoreCase("could not connect") || e.getMessage().equalsIgnoreCase("unkown host")) { // is there a better way to find out what happened?
		    finalMessage = MyLocale.getMsg(1616, "Error: could not download update file from " + hostname);
		} else {
		    finalMessage = "IOException: " + e.getMessage();
		}
		success = false;
	    }
	} catch (final IllegalArgumentException e) {
	    finalMessage = MyLocale.getMsg(1621, "Error parsing update file\n this is likely a bug in " + hostname + "\nplease try again later\n, state:") + " " + state + ", waypoint: " + holder.getWayPoint();
	    success = false;
	    Preferences.itself().log("Parse error: " + state + " " + holder.getWayPoint(), e, true);
	} catch (final Exception e) { // here should be used the correct exception
	    if (holder != null)
		finalMessage = MyLocale.getMsg(1615, "Error parsing update file, state:") + " " + state + ", waypoint: " + holder.getWayPoint();
	    else
		finalMessage = MyLocale.getMsg(1615, "Error parsing update file, state:") + " " + state + ", waypoint: <unkown>";
	    success = false;
	    Preferences.itself().log("", e, true);
	} finally {
	    if (tmpFile != null)
		tmpFile.delete();
	}
	/*
	 * for (int i=cacheDB.size()-1; i >=0; i--) { ch = (CacheHolder)cacheDB.get(i); if (ch.wayPoint.toUpperCase().startsWith("OC")) { //TODO only handle changed caches ch.calcRecommendationScore(); } }
	 */
	inf.setInfo(finalMessage);

	return success;
    }

    public void startElement(String name, AttributeList atts) {
	if (debugGPX) {
	    for (int i = 0; i < atts.getLength(); i++) {
		Preferences.itself().log(" Name: " + atts.getName(i) + " Value: " + atts.getValue(i));
	    }
	}
	strData = "";

	if (name.equals("oc11xml")) {
	    final Time lastSync = new Time();
	    try {
		lastSync.parse(atts.getValue("date"), "yyyy-MM-dd HH:mm:ss");
	    } catch (final IllegalArgumentException e) {
		Preferences.itself().log("", e, true);
	    }
	    // reduce time at 1 second to avoid sync problems
	    lastSync.setTime(lastSync.getTime() - 1000);
	    dateOfthisSync = lastSync;
	    state = STAT_INIT;
	}

	// look for changes in the state
	if (name.equals("cache")) {
	    state = STAT_CACHE;
	}
	if (name.equals("cachedesc")) {
	    state = STAT_CACHE_DESC;
	}
	if (name.equals("cachelog")) {
	    state = STAT_CACHE_LOG;
	    logtype = 0;
	}
	if (name.equals("picture")) {
	    state = STAT_PICTURE;
	}

	// examine data
	switch (state) {
	case STAT_CACHE:
	    startCache(name, atts);
	    break;
	case STAT_CACHE_DESC:
	    startCacheDesc(name, atts);
	    break;
	case STAT_CACHE_LOG:
	    startCacheLog(name, atts);
	    break;
	case STAT_PICTURE:
	    startPicture(name, atts);
	    break;
	}

    }

    public void endElement(String name) {
	// examine data
	switch (state) {
	case STAT_CACHE:
	    endCache(name);
	    break;
	case STAT_CACHE_DESC:
	    endCacheDesc(name);
	    break;
	case STAT_CACHE_LOG:
	    endCacheLog(name);
	    break;
	case STAT_PICTURE:
	    endPicture(name);
	    break;
	}

	// look for changes in the state
	if (name.equals("cache"))
	    state = STAT_INIT;
	if (name.equals("cachedesc"))
	    state = STAT_INIT;
	if (name.equals("cachelog"))
	    state = STAT_INIT;
	if (name.equals("picture"))
	    state = STAT_INIT;

    }

    public void characters(char[] ch2, int start, int length) {
	final String chars = new String(ch2, start, length);
	strData += chars;
	if (debugGPX)
	    Preferences.itself().log(strData, null);
    }

    private void startCache(String name, AttributeList atts) {
	if (name.equals("id")) {
	    cacheID = atts.getValue("id");
	    return;
	}
	if (holder == null)
	    return;
	inf.setInfo(MyLocale.getMsg(1609, "Importing Cache:") + " " + numCacheImported + " / " + numCacheUpdated + "\n");
	if (name.equals("type")) {
	    holder.setType(CacheType.ocType2CwType(atts.getValue("id")));
	    holder.getCacheDetails(false).attributes.clear();
	    return;
	}
	if (name.equals("status")) {
	    // meaning of OC status :
	    // 1=Kann gesucht werden ;
	    // 2=Momentan nicht verfügbar ;
	    // 3=Archiviert ;
	    // 4= ;
	    // 5= ;
	    // 6=Gesperrt ;
	    // are there more ? ;
	    if (atts.getValue("id").equals("1")) {
		holder.setAvailable(true);
		holder.setArchived(false);
	    } else {
		holder.setAvailable(false);
		if ((atts.getValue("id").equals("3")) || (atts.getValue("id").equals("6")) || (atts.getValue("id").equals("7"))) {
		    if (!isSyncSingle) {
			holder = null;
			numCacheImported--;
		    } else {
			// Umsetzung wie in gpx für Status 6
			if (atts.getValue("id").equals("6")) {
			    holder.setArchived(false);
			} else {
			    holder.setArchived(true);
			}
		    }
		}
	    }
	    return;
	}
	if (name.equals("size")) {
	    holder.setCacheSize(CacheSize.ocXmlString2Cw(atts.getValue("id")));
	    return;
	}

	if (name.equals("waypoints")) {
	    holder.setWayPoint(atts.getValue("oc"));
	    final String CName = atts.getValue("nccom") + " " + atts.getValue("gccom");
	    if (!CName.equals(" ")) {
		holder.setCacheOwner(holder.getCacheOwner() + " / " + CName.trim());
		holder.getCacheDetails(false).attributes.add(7); // wwwlink
		holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
	    } else {
		holder.getCacheDetails(false).attributes.add(6); // oconly
		holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
	    }
	    if (holder.getWayPoint().length() == 0)
		throw new IllegalArgumentException("empty waypointname"); // this should not happen - it is likey a bug in opencaching / it happens on 27-12-2006 on cache OC143E
	    return;
	}

	if (name.equals("attribute")) {
	    final int id = Integer.parseInt(atts.getValue("id"));
	    holder.getCacheDetails(false).attributes.add(id);
	    holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
	    return;
	}

    }

    private void startCacheDesc(String name, AttributeList atts) {
	inf.setInfo(MyLocale.getMsg(1611, "Importing cache description:") + " " + numDescImported);

	if (name.equals("cacheid")) {
	    cacheID = atts.getValue("id");
	    holder = getHolder(cacheID, false);
	    return;
	}

	if (name.equals("desc")) {
	    isHTML = atts.getValue("html").equals("1") ? true : false;
	    return;
	}

	if (name.equals("language")) {
	    processingDescLang = atts.getValue("id");
	    return;
	}
    }

    private void startCacheLog(String name, AttributeList atts) {
	if (name.equals("id")) {
	    logId = atts.getValue("id");
	    return;
	}

	if (name.equals("cacheid")) {
	    holder = getHolder(atts.getValue("id"), false);
	    return;
	}
	if (holder == null)
	    return;

	inf.setInfo(MyLocale.getMsg(1612, "Importing Cachlog:") + " " + numLogImported);

	if (name.equals("logtype")) {
	    logtype = Convert.toInt(atts.getValue("id"));
	    switch (logtype) {
	    case 1:
		logIcon = Log.typeText2Image("Found");
		break;
	    case 2:
		logIcon = Log.typeText2Image("Not Found");
		holder.setNoFindLogs((byte) (holder.getNoFindLogs() + 1));
		break;
	    case 3:
		logIcon = Log.typeText2Image("Note");
	    }
	    loggerRecommended = atts.getValue("recommended").equals("1");
	    return;
	}
    }

    private void startPicture(String name, AttributeList atts) {
	if (name.equals("object")) {
	    cacheID = atts.getValue("id"); // are there picture without cacheID?
	    holder = getHolder(cacheID, false);
	    return;
	}
    }

    private void endCache(String name) {
	if (name.equals("id")) { // </id>
	    // the guid (=strData) is not part of gpx , so we use id of cacheID
	    holder = getHolder(cacheID, true); // Allocate a new CacheHolder object
	    holder.setOcCacheID(cacheID);
	    holder.getCacheDetails(false).URL = "http://" + hostname + "/viewcache.php?cacheid=" + cacheID;
	    return;
	}
	if (holder == null)
	    return; // id should always be the first for a <cache>
	if (name.equals("cache")) {
	    holder.setLastSync(dateOfthisSync.format("yyyyMMddHHmmss"));
	    int index;
	    index = cacheDB.getIndex(holder.getWayPoint());
	    if (index == -1) {
		numCacheImported++;
		holder.setNew(true);
		cacheDB.add(holder);
		DBindexID.put(holder.getOcCacheID(), holder.getWayPoint());
	    }
	    // update (overwrite) data
	    else {
		numCacheUpdated++;
		holder.setNew(false);
		holder.setIncomplete(false);
		cacheDB.get(index).update(holder);
		DBindexID.put(holder.getOcCacheID(), holder.getWayPoint());
	    }
	    // clear data (picture, logs) if we do a complete Update
	    if (!incUpdate) {
		holder.getCacheDetails(false).CacheLogs.clear();
		holder.getCacheDetails(false).images.clear();
	    }

	    // save all
	    holder.getCacheDetails(false).hasUnsavedChanges = true; // this makes CachHolder save the details in case that they are unloaded from memory
	    // chD.saveCacheDetails(MainForm.profile.dataDir);
	    // MainForm.profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR); // this is done after .xml is completly processed

	    holder = null;
	    return;
	}

	if (name.equals("name")) {
	    holder.setCacheName(strData);
	    return;
	}
	if (name.equals("userid")) {
	    holder.setCacheOwner(strData);
	    if (holder.getCacheOwner().equalsIgnoreCase(Preferences.itself().myAlias) || (holder.getCacheOwner().equalsIgnoreCase(Preferences.itself().myAlias2)))
		holder.setOwned(true);
	    return;
	}

	if (name.equals("longitude")) {
	    longitude = Common.parseDouble(strData);
	    return;
	}
	if (name.equals("latitude")) {
	    holder.setPos(new CoordinatePoint(Common.parseDouble(strData), longitude));
	    holder.setUpdated(false); // todo : correct definition of usage for this
	    return;
	}
	if (name.equals("difficulty")) {
	    holder.setHard(CacheTerrDiff.v1Converter(strData));
	    return;
	}
	if (name.equals("terrain")) {
	    holder.setTerrain(CacheTerrDiff.v1Converter(strData));
	    return;
	}
	if (name.equals("datehidden")) {
	    holder.setDateHidden(strData.substring(0, 10)); // Date;
	    return;
	}
	if (name.equals("country")) {
	    holder.getCacheDetails(false).Country = strData;
	    return;
	}
    }

    private void endCacheDesc(String name) {
	if (holder == null)
	    return;
	if (name.equals("cachedesc")) {
	    numDescImported++;
	    holder.isHTML(isHTML);
	    if (downloadPics && isHTML) {
		getImageNamesFromDescription();
	    }
	    holder.getCacheDetails(false).hasUnsavedChanges = true;
	    return;
	}

	if (name.equals("shortdesc")) {
	    String linebraek;

	    if (isHTML)
		linebraek = "<br>\n";
	    else
		linebraek = "\n";
	    // if a long description has been entered in this run (==holder.cache_updated is true),
	    // then this one is added (for another language)
	    // otherwise all previous descriptions will be overwritten ( or there are none yet)
	    if (holder.isUpdated())
		holder.getCacheDetails(false).LongDescription += linebraek + processingDescLang + ":" + linebraek + strData + linebraek;
	    else
		holder.getCacheDetails(false).LongDescription = processingDescLang + ":" + linebraek + strData + linebraek;
	    return;
	}

	if (name.equals("desc")) { // </desc>
	    if (isHTML)
		holder.getCacheDetails(false).LongDescription += SafeXML.html2iso8859s1(strData);
	    else
		holder.getCacheDetails(false).LongDescription += strData;
	    return;
	}
	if (name.equals("hint")) {
	    String linebreak;
	    if (isHTML)
		linebreak = "<br>\n";
	    else
		linebreak = "\n";
	    if (holder.isUpdated())
		holder.getCacheDetails(false).Hints += linebreak + "[" + processingDescLang + ":]" + linebreak + Common.rot13(strData) + linebreak;
	    else
		holder.getCacheDetails(false).Hints = "[" + processingDescLang + ":]" + linebreak + Common.rot13(strData) + linebreak;
	    // remark:
	    // holder.cache_updated will be set to true
	    // after the subtag-infos of tag <cachedesc> have been entered
	    // (ending with the subtag </hint>)
	    // to possibly add the <cachedesc> for an additional language
	    holder.setUpdated(true);
	    return;
	}
    }

    private void endCacheLog(String name) {
	if (holder == null)
	    return;
	if (name.equals("cachelog")) { // </cachelog>
	    if (holder.getCacheDetails(false).CacheLogs.merge(new Log(logId, finderID, logIcon, logDate, logFinder, logData, loggerRecommended)) > -1) {
		numLogImported++;
		holder.getCacheDetails(false).hasUnsavedChanges = true; // chD.saveCacheDetails(MainForm.profile.dataDir);
	    }
	    //
	    if ((logFinder.toLowerCase().compareTo(user) == 0 || logFinder.equalsIgnoreCase(Preferences.itself().myAlias2)) && logtype == 1) {
		if (incFinds || !holder.isNew()) {
		    // aber vorhandene werden mit gefunden aktualisiert
		    holder.setCacheStatus(logDate);
		    holder.setFound(true);
		    holder.getCacheDetails(false).OwnLogId = logId;
		    holder.getCacheDetails(false).OwnLog = new Log(logId, finderID, logIcon, logDate, logFinder, logData, loggerRecommended);
		} else {
		    // if (holder.is_new())
		    cacheDB.removeElementAt(cacheDB.getIndex(holder));
		    DBindexID.remove(holder.GetCacheID());
		    // und Dateien löschen?
		    final File tmpFile = new File(MainForm.profile.dataDir + holder.getWayPoint() + ".xml");
		    tmpFile.delete();
		    // todo: was ist mit den schon heruntergeladenen Bildern?
		}
	    }
	    return;
	}

	if (name.equals("date")) {
	    logDate = strData;
	    return;
	}
	if (name.equals("userid")) {
	    logFinder = strData;
	    return;
	}
	if (name.equals("text")) {
	    logData = strData;
	    return;
	}

    }

    private void endPicture(String name) {
	if (holder == null)
	    return;

	if (name.equals("id")) {
	    picID = strData;
	    return;
	}

	if (name.equals("url")) {
	    picUrl = strData;
	    return;
	}
	if (name.equals("title")) {
	    picTitle = strData;
	    return;
	}
	if (name.equals("picture")) {
	    inf.setInfo(MyLocale.getMsg(1613, "Pictures:") + " " + ++picCnt);
	    // String fileName = holder.wayPoint + "_" + picUrl.substring(picUrl.lastIndexOf("/")+1);
	    final CacheImage ii = new CacheImage();
	    ii.setTitle(picTitle);
	    ii.setURL(picUrl);
	    getPic(ii);
	    holder.getCacheDetails(false).hasUnsavedChanges = true; // saveCacheDetails(MainForm.profile.dataDir);
	    return;
	}
    }

    private CacheHolder getHolder(String guid, boolean create) {// See also LOCXMLImporter
	CacheHolder ch = null;
	// Integer INTR = (Integer)DBindexID.get(guid);
	final String wp = (String) DBindexID.get(guid);
	// if(INTR != null){
	if (wp != null) {
	    // ch = cacheDB.get(INTR.intValue());
	    ch = cacheDB.get(wp);
	} else {
	    if (create)
		ch = new CacheHolder();
	}
	return ch;
    }

    private void getImageNamesFromDescription() {
	String fetchUrl;
	String imgTag;
	String imgAltText;
	final Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); // Ergebnis enthlt keine Anfhrungszeichen
	final Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get alternative text for Pic
	imgRegexAlt.setIgnoreCase(true);
	imgRegexUrl.setIgnoreCase(true);
	int descIndex = 0;
	int numDownloaded = 1;
	while (imgRegexUrl.searchFrom(holder.getCacheDetails(false).LongDescription, descIndex)) { // "img" found
	    imgTag = imgRegexUrl.stringMatched(1); // (1) enthlt das gesamte <img ...>-tag
	    fetchUrl = imgRegexUrl.stringMatched(2); // URL in Anfhrungszeichen in (2) falls ohne in (3) Ergebnis ist auf jeden Fall ohne Anfhrungszeichen
	    if (fetchUrl == null) {
		fetchUrl = imgRegexUrl.stringMatched(3);
	    }
	    if (fetchUrl == null) { // TODO Fehler ausgeben: nicht abgedeckt ist der Fall, dass in einem Cache Links auf Bilder mit unterschiedlichen URL, aber gleichem Dateinamen sind.
		inf.addWarning(MyLocale.getMsg(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache " + holder.getWayPoint()));
		continue;
	    }
	    inf.setInfo(MyLocale.getMsg(1611, "Importing cache description:") + " " + numDescImported + "\n" + MyLocale.getMsg(1620, "downloading embedded images: ") + numDownloaded++);
	    if (imgRegexAlt.search(imgTag)) {
		imgAltText = imgRegexAlt.stringMatched(1);
		if (imgAltText == null)
		    imgAltText = imgRegexAlt.stringMatched(2);
		// no alternative text as image title -> use filename
	    } else {
		if (fetchUrl.toLowerCase().indexOf("opencaching.") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0) // wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend
		    imgAltText = "No image title";
		else
		    imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/') + 1);
	    }
	    descIndex = imgRegexUrl.matchedTo();
	    try {
		// TODO this is not quite correct: actually the "base" URL must be known...
		// but anyway a different baseURL should not happen very often - it doesn't in my area
		if (!fetchUrl.startsWith("http://")) {
		    fetchUrl = new URL(new URL("http://" + hostname + "/"), fetchUrl).toString();
		}
	    } catch (final MalformedURLException e) {
		final String ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + holder.getWayPoint() + ": ignoring MalformedUrlException: " + e.getMessage() + " while downloading from URL:" + fetchUrl;
		inf.addWarning("\n" + ErrMessage);
		Preferences.itself().log(ErrMessage, e);
	    }
	    final CacheImage imageInfo = new CacheImage();
	    imageInfo.setURL(fetchUrl);
	    imageInfo.setTitle(imgAltText);
	    getPic(imageInfo);
	}
    }

    private void getPic(CacheImage imageInfo) { // TODO handling of relativ URLs
	String fileName = holder.getWayPoint() + "_" + imageInfo.getURL().substring(imageInfo.getURL().lastIndexOf('/') + 1);
	fileName = Common.ClearForFileName(fileName).toLowerCase();
	final String target = MainForm.profile.dataDir + fileName;
	imageInfo.setFilename(fileName);
	try {
	    File ftest = new File(target);
	    if (ftest.exists()) {
		if (ftest.length() == 0) {
		    ftest.delete();
		} else {
		    holder.getCacheDetails(false).images.add(imageInfo);
		}
	    } else {
		if (downloadPics) {
		    UrlFetcher.fetchDataFile(imageInfo.getURL(), target);
		    ftest = new File(target);
		    if (ftest.exists()) {
			if (ftest.length() > 0) {
			    holder.getCacheDetails(false).images.add(imageInfo);
			} else {
			    ftest.delete();
			}
		    }
		}
	    }
	} catch (final IOException e) {
	    String ErrMessage;
	    String wp, n;
	    if (holder != null && holder.getWayPoint() != null)
		wp = holder.getWayPoint();
	    else
		wp = "WP???";
	    if (holder != null && holder.getCacheName() != null)
		n = holder.getCacheName();
	    else
		n = "name???";

	    //if (e == null)
	    //	ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: " + fileName + " from URL:" + imageInfo.getURL();
	    //else {
	    if (e.getMessage().equalsIgnoreCase("could not connect") || e.getMessage().equalsIgnoreCase("unkown host")) {
		// is there a better way to find out what happened?
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + ")" + MyLocale.getMsg(1619, ": could not download image from URL: ") + imageInfo.getURL();
	    } else
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + "): ignoring IOException: " + e.getMessage() + " while downloading picture:" + fileName + " from URL:" + imageInfo.getURL();
	    //}
	    inf.addWarning(ErrMessage);
	    Preferences.itself().log(ErrMessage, e, true);
	}

    }

}
