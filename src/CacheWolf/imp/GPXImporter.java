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

import CacheWolf.Filter;
import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.Attribute;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheImage;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.database.Log;
import CacheWolf.database.Travelbug;
import CacheWolf.utils.BetterUTF8Codec;
import CacheWolf.utils.Common;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.UrlFetcher;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.TextReader;
import ewe.net.MalformedURLException;
import ewe.net.URL;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.Enumeration;
import ewe.util.Vector;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import Data from an GPX File. If cache data exists, the data from the GPX-File is ignored. Class ID = 4000
 */
public class GPXImporter extends MinML {
    CacheDB cacheDB;
    CacheHolder holder;
    String strData, saveDir, logData, logIcon, logDate, logFinder, logId, finderID;
    boolean inWpt, inCache, inLogs, inBug;
    public XMLElement document;
    private Vector files = new Vector();
    private boolean debugGPX = false;
    InfoBox infB;
    boolean spiderOK = true;
    boolean downloadPics = false;
    boolean fromOC = false;
    boolean fromTC = false;
    boolean nameFound = false;
    static final Time gpxDate = new Time();
    int zaehlerGel = 0;
    public static final int DOIT_ASK = 0;
    public static final int DOIT_NOSPOILER = 1;
    public static final int DOIT_WITHSPOILER = 2;
    GCImporter imgSpider;
    StringBuffer strBuf;
    private int doitHow;
    private String attID;
    private String attInc;

    public GPXImporter(String f) {
	cacheDB = MainForm.profile.cacheDB;
	files.add(f);
	saveDir = MainForm.profile.dataDir;
	inWpt = false;
	inCache = false;
	inLogs = false;
	inBug = false;
	doitHow = DOIT_ASK;
    }

    public void doIt(int how) {
	doitHow = how;
	Filter flt = new Filter();
	boolean wasFiltered = (MainForm.profile.getFilterActive() == Filter.FILTER_ACTIVE);
	flt.clearFilter();
	try {
	    String file;
	    if (how == DOIT_ASK) {
		ImportGui importGui = new ImportGui(MyLocale.getMsg(5510, "Spider Options"), ImportGui.IMAGES | ImportGui.ISGC);
		if (importGui.execute() == FormBase.IDCANCEL) {
		    return;
		}
		downloadPics = importGui.downloadPics;
		importGui.close(0);
	    } else if (how == DOIT_NOSPOILER) {
		downloadPics = false;
	    } else {
		downloadPics = true;
	    }
	    if (downloadPics) {
		imgSpider = new GCImporter();
		imgSpider.setDownloadPics(downloadPics);
		doitHow = DOIT_WITHSPOILER;
	    } else {
		doitHow = DOIT_NOSPOILER;
	    }

	    Vm.showWait(true);
	    infB = new InfoBox("Info", MyLocale.getMsg(4000, "Loaded caches: "));
	    infB.show();
	    for (int i = 0; i < files.size(); i++) {
		// Test for zip.file
		file = (String) files.get(i);
		if (file.indexOf(".zip") > 0) {
		    ZipFile zif = new ZipFile(file);
		    ZipEntry zipEnt;
		    Enumeration zipEnum = zif.entries();
		    // there could be more than one file in the archive
		    while (zipEnum.hasMoreElements()) {
			zipEnt = (ZipEntry) zipEnum.nextElement();
			// skip over PRC-files
			if (zipEnt.getName().endsWith("gpx")) {
			    // InputStreamReader r = new InputStreamReader(zif.getInputStream(zipEnt));		
			    TextReader tr = new TextReader(zif.getInputStream(zipEnt));
			    tr.codec = new AsciiCodec();
			    // infB.setTitle(zipEnt.toString());
			    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
			    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);

			    String readLine = tr.readLine().toLowerCase();
			    if (readLine.startsWith("ï»¿") || readLine.indexOf("encoding=\"utf-8\"") > 0) {
				tr.close();
				// InputStreamReader r = new InputStreamReader(zif.getInputStream(zipEnt));								
				tr = new TextReader(zif.getInputStream(zipEnt));
				tr.codec = new BetterUTF8Codec();
			    }
			    parse(tr);
			    tr.close();
			}
		    }
		    zif.close();
		} else {
		    TextReader tr = new TextReader(file);
		    tr.codec = new AsciiCodec();
		    // infB.setTitle("Info");
		    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
		    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);

		    String readLine = tr.readLine().toLowerCase();
		    if (readLine.startsWith("ï»¿") || readLine.indexOf("encoding=\"utf-8\"") > 0) {
			tr.close();
			tr = new TextReader(file);
			tr.codec = new BetterUTF8Codec();
		    }
		    parse(tr);
		    tr.close();
		}
		// save Index
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
	    }
	    Vm.showWait(false);
	    infB.close(0);
	} catch (Exception e) {

	    if (holder == null) {
		Preferences.itself().log("[GPXImporter:DoIt] no holder LogID=" + logId, e, true);
	    } else if (holder.getWayPoint() == null) {
		Preferences.itself().log("[GPXImporter:DoIt] no waypoint LogID=" + logId, e, true);
	    } else if (holder.getWayPoint().length() > 0) {
		Preferences.itself().log("[GPXImporter:DoIt] " + holder.getWayPoint() + " LogID=" + logId, e, true);
	    } else {
		Preferences.itself().log("[GPXImporter:DoIt] " + holder.getPos().toString() + " LogID=" + logId, e, true);
	    }
	    infB.close(0);
	    Vm.showWait(false);
	}
	if (wasFiltered) {
	    flt.setFilter();
	    flt.doFilter();
	}
    }

    public void startElement(String name, AttributeList atts) {

	strBuf = new StringBuffer(300);
	if (infB.isClosed())
	    return;
	if (name.equals("gpx")) {
	    // check for opencaching

	    String strCreator = atts.getValue("creator");
	    if (strCreator == null) {

		fromOC = false;
		fromTC = false;
	    } else {

		if (strCreator.indexOf("opencaching") > 0)
		    fromOC = true;
		else
		    fromOC = false;

		if (strCreator.startsWith("TerraCaching"))
		    fromTC = true;
		else
		    fromTC = false;
	    }
	    zaehlerGel = 0;
	}
	if (name.equals("wpt")) {
	    holder = new CacheHolder();
	    holder.setPos(new CoordinatePoint(Common.parseDouble(atts.getValue("lat")), Common.parseDouble(atts.getValue("lon"))));
	    inWpt = true;
	    inLogs = false;
	    inBug = false;
	    nameFound = false;
	    zaehlerGel++;
	    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
	    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
	    logId = "";
	    return;
	}

	if (name.equals("link") && inWpt) {
	    holder.getCacheDetails(false).URL = atts.getValue("href");
	    return;
	}

	if (name.equals("groundspeak:cache")) {
	    inCache = true;
	    String strAvailable = atts.getValue("available");
	    String strArchived = atts.getValue("archived");
	    holder.setAvailable(strAvailable != null && strAvailable.equalsIgnoreCase("True"));
	    holder.setArchived(strArchived != null && strArchived.equalsIgnoreCase("True"));
	    // OC now has GC - Format, get CacheID -- missing p.ex. on GcTour gpx
	    /* */
	    for (int i = 0; i < atts.getLength(); i++) {
		if (atts.getName(i).equals("id")) {
		    holder.setOcCacheID(atts.getValue("id"));
		    break;
		}
	    }
	    /* */
	    return;
	}
	// OC
	if (name.equals("geocache") || name.equals("cache")) {
	    boolean available = false;
	    boolean archived = false;
	    inCache = true;
	    // get CacheID -- missing p.ex. on GcTour gpx
	    for (int i = 0; i < atts.getLength(); i++) {
		if (atts.getName(i).equals("id")) {
		    holder.setOcCacheID(atts.getValue("id"));
		    break;
		}
	    }
	    // get status
	    String status = atts.getValue("status");
	    if ("Available".equalsIgnoreCase(status))
		available = true;
	    else if ("Unavailable".equalsIgnoreCase(status))
		available = false;
	    else if ("Draft".equalsIgnoreCase(status))
		available = false;
	    else if ("Archived".equalsIgnoreCase(status))
		archived = true;
	    holder.setArchived(archived);
	    holder.setAvailable(available);
	    return;
	}

	if (name.equals("terra:terracache")) {
	    inCache = true;
	}

	if (name.indexOf("long_description") > -1) {
	    holder.setHTML(atts.getValue("html").toLowerCase().equals("true"));
	}
	if (name.equals("description") || name.equals("terra:description")) {
	    // set HTML always to true if from oc.de or TC
	    holder.setHTML(true);
	}

	if (name.equals("groundspeak:logs") || name.equals("logs") || name.equals("terra:logs")) {
	    inLogs = true;
	    return;
	}
	if (name.equals("groundspeak:log") || name.equals("log") || name.equals("terra:log")) {
	    inLogs = true;
	    logId = atts.getValue("id");
	    return;
	}
	if (name.equals("groundspeak:finder") || name.equals("geocacher") || name.equals("finder") || name.equals("terra:user")) {
	    finderID = atts.getValue("id");
	    return;
	}
	if (name.equals("groundspeak:travelbugs")) {
	    inBug = true;
	    holder.getCacheDetails(false).Travelbugs.clear();
	    return;
	}
	if (name.equals("groundspeak:attribute")) {
	    attID = atts.getValue("id");
	    attInc = atts.getValue("inc");
	    return;
	}
	if (debugGPX) {
	    for (int i = 0; i < atts.getLength(); i++) {
		Preferences.itself().log("[GPXExporter:startElement]Type: " + atts.getType(i) + " Name: " + atts.getName(i) + " Value: " + atts.getValue(i), null);
	    }
	}
    }

    public void endElement(String name) {
	strData = strBuf.toString();
	if (infB.isClosed())
	    return;
	// logs
	if (inLogs) {
	    if (name.equals("groundspeak:date") || name.equals("time") || name.equals("date") || name.equals("terra:date")) {
		logDate = new String(strData.substring(0, 10));
		return;
	    }
	    if (name.equals("groundspeak:type") || name.equals("type") || name.equals("terra:type")) {
		logIcon = new String(Log.typeText2Image(strData));
		return;
	    }
	    if (name.equals("groundspeak:finder") || name.equals("geocacher") || name.equals("finder") || name.equals("terra:user")) {
		logFinder = new String(strData);
		return;
	    }
	    if (name.equals("groundspeak:text") || name.equals("text") || name.equals("terra:entry")) {
		logData = new String(strData);
		return;
	    }
	    if (name.equals("groundspeak:log") || name.equals("log") || name.equals("terra:log")) {
		holder.getCacheDetails(false).CacheLogs.add(new Log(logId, finderID, logIcon, logDate, logFinder, logData));
		if ((logIcon.equals("icon_smile.gif") || logIcon.equals("icon_camera.gif") || logIcon.equals("icon_attended.gif"))
			&& (SafeXML.html2iso8859s1(logFinder).equalsIgnoreCase(Preferences.itself().myAlias) || (SafeXML.html2iso8859s1(logFinder).equalsIgnoreCase(Preferences.itself().myAlias2)))) {
		    holder.setCacheStatus(logDate);
		    holder.setFound(true);
		    holder.getCacheDetails(false).OwnLogId = logId;
		    holder.getCacheDetails(false).OwnLog = new Log(logId, finderID, logIcon, logDate, logFinder, logData);
		}
		return;
	    }
	}

	if (name.equals("wpt")) {

	    int index = cacheDB.getIndex(holder.getWayPoint());
	    if (index == -1) {// Add cache Data only, if waypoint not already in database
		holder.setNoFindLogs(holder.getCacheDetails(false).CacheLogs.countNotFoundLogs());
		holder.setNew(true);
		cacheDB.add(holder);
		// don't spider additional waypoints, so check
		// if waypoint starts with "GC"
		if (downloadPics) {
		    if (spiderOK && !holder.isArchived()) {
			// spiderImages();
			getImages();
			// Rename image sources
			String text;
			String orig;
			String imgName;
			orig = holder.getCacheDetails(false).LongDescription;

			Extractor ex = new Extractor(orig, "<img src=\"", ">", 0, false);
			int num = 0;
			while ((text = ex.findNext()).length() > 0 && spiderOK) {
			    if (num >= holder.getCacheDetails(false).images.size())
				break;
			    imgName = holder.getCacheDetails(false).images.get(num).getTitle();
			    holder.getCacheDetails(false).LongDescription = STRreplace.replace(holder.getCacheDetails(false).LongDescription, text, "[[Image: " + imgName + "]]");
			    num++;
			}
		    }
		}
		holder.save();
		// crw.saveIndex(cacheDB,saveDir);
	    }
	    // Update cache data
	    else {
		CacheHolder oldCh = cacheDB.get(index);
		// Preserve images: Copy images from old cache version because here we didn't add
		// any image information to the holder object.
		if (downloadPics && holder.isOC()) {
		    getImages();
		} else {
		    holder.getCacheDetails(false).images = oldCh.getCacheDetails(true).images;
		}
		oldCh.initStates(false);
		if (!oldCh.isOC()) {
		    if (Preferences.itself().useGCFavoriteValue) {
			// todo get GC Favs from gpx if they exist there
			holder.setNumRecommended(oldCh.getNumRecommended());
		    } else {
			holder.setNumRecommended(oldCh.getNumRecommended()); // gcvote Bewertung bleibt erhalten			
		    }
		}
		oldCh.update(holder);
		oldCh.save();
	    }

	    inWpt = false;
	    return;
	}
	if (name.equals("sym") && strData.endsWith("Found")) {
	    holder.setFound(true);
	    holder.setCacheStatus(holder.getFoundText());
	    return;
	}
	if (name.equals("groundspeak:travelbugs")) {
	    inBug = false;
	    return;
	}

	if (name.equals("groundspeak:name") && inBug) {
	    Travelbug tb = new Travelbug(strData);
	    holder.getCacheDetails(false).Travelbugs.add(tb);
	    holder.setHas_bugs(true);
	    return;
	}

	if (name.equals("time") && !inWpt) {
	    try {
		gpxDate.parse(strData.substring(0, 19), "yyyy-MM-dd'T'HH:mm:ss");
	    } catch (IllegalArgumentException e) {
		gpxDate.setTime(0);
		Preferences.itself().log("[GPXImporter:endElement]Error parsing Element time: '" + strData + "'. Ignoring.");
	    }
	    return;
	}

	if (name.equals("time") && inWpt) {
	    holder.setDateHidden(strData.substring(0, 10)); // Date;
	    return;
	}
	// cache information
	if (name.equals("groundspeak:cache") || name.equals("geocache") || name.equals("cache") || name.equals("terra:terracache")) {
	    inCache = false;
	}

	if (name.equals("name") && inWpt && !inCache) {
	    holder.setWayPoint(strData);
	    if (gpxDate.getTime() != 0) {
		holder.setLastSync(gpxDate.format("yyyyMMddHHmmss"));
	    } else {
		holder.setLastSync("");
	    }
	    // msgA.setText("import " + strData);
	    return;
	}

	// fill name with contents of <desc>, in case of gc.com the name is
	// later replaced by the contents of <groundspeak:name> which is shorter
	if (name.equals("desc") && inWpt) {
	    holder.setCacheName(strData);
	    // msgA.setText("import " + strData);
	    return;
	}
	if (name.equals("url") && inWpt) {
	    holder.getCacheDetails(false).URL = strData;
	    return;
	}

	// Text for additional waypoints, no HTML
	if (name.equals("cmt") && inWpt) {
	    holder.getCacheDetails(false).LongDescription = strData;
	    holder.setHTML(false);
	    return;
	}

	// aditional wapypoint
	if (name.equals("type") && inWpt && !inCache && strData.startsWith("Waypoint")) {
	    holder.setType(CacheType.gpxType2CwType(strData));
	    holder.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
	    holder.setHard(CacheTerrDiff.CW_DT_UNSET);
	    holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
	    holder.setLastSync("");
	}

	if (name.indexOf("name") > -1 && inCache) {
	    holder.setCacheName(strData);
	    return;
	}
	if (name.equals("groundspeak:owner") || name.equals("owner") || name.equals("terra:owner")) {
	    holder.setCacheOwner(strData);
	    if (Preferences.itself().myAlias.equals(SafeXML.html2iso8859s1(strData)) || (SafeXML.html2iso8859s1(strData).equalsIgnoreCase(Preferences.itself().myAlias2)))
		holder.setOwned(true);
	    return;
	}
	if (name.equals("groundspeak:placed_by")) {
	    if (holder.getCacheOwner().equals("")) {
		holder.setCacheOwner(strData);
		if (Preferences.itself().myAlias.equals(SafeXML.html2iso8859s1(strData)) || (SafeXML.html2iso8859s1(strData).equalsIgnoreCase(Preferences.itself().myAlias2)))
		    holder.setOwned(true);
	    }
	    return;
	}
	if (name.equals("groundspeak:difficulty") || name.equals("difficulty") || name.equals("terra:mental_challenge")) {
	    try {
		holder.setHard(CacheTerrDiff.v1Converter(strData));
	    } catch (IllegalArgumentException e) {

		Preferences.itself().log(holder.getCacheName() + ": illegal difficulty value: " + strData);
	    }
	    return;
	}
	if (name.equals("groundspeak:terrain") || name.equals("terrain") || name.equals("terra:physical_challenge")) {
	    try {
		holder.setTerrain(CacheTerrDiff.v1Converter(strData));
	    } catch (IllegalArgumentException e) {

		Preferences.itself().log(holder.getCacheName() + ": illegal terrain value: " + strData);
	    }
	    return;
	}
	if ((name.equals("groundspeak:type") || name.equals("type") || name.equals("terra:style")) && inCache) {
	    holder.setType(CacheType.gpxType2CwType(strData));
	    if (holder.isCustomWpt()) {
		holder.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
		holder.setHard(CacheTerrDiff.CW_DT_UNSET);
		holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
	    }
	    return;
	}
	if (name.equals("groundspeak:container") || name.equals("container")) {
	    holder.setCacheSize(CacheSize.gcGpxString2Cw(strData));
	    return;
	}
	if (name.equals("groundspeak:country") || name.equals("country")) {
	    holder.getCacheDetails(false).Country = strData;
	    return;
	}
	if (name.equals("groundspeak:state") || name.equals("state")) {
	    holder.getCacheDetails(false).State = strData;
	    return;
	}
	if (name.equals("terra:size")) {
	    holder.setCacheSize(CacheSize.tcGpxString2Cw(strData));
	}

	if (name.indexOf("short_description") > -1 || name.equals("summary")) {
	    if (holder.isHTML())
		holder.getCacheDetails(false).LongDescription = SafeXML.html2iso8859s1(strData) + "<br>"; // <br> needed because we also use a <br> in SpiderGC. Without it the comparison in ch.update fails
	    else
		holder.getCacheDetails(false).LongDescription = strData + "\n";
	    return;
	}

	if (name.indexOf("long_description") > -1 || name.equals("description") || name.equals("terra:description")) {
	    if (holder.isHTML())
		holder.getCacheDetails(false).LongDescription += SafeXML.html2iso8859s1(strData);
	    else
		holder.getCacheDetails(false).LongDescription += strData;
	    return;
	}
	if (name.indexOf("encoded_hints") > -1 || name.equals("hints")) {
	    holder.getCacheDetails(false).Hints = STRreplace.replace(STRreplace.replace(Common.rot13(strData), "\n", "<br>"), "\t", "");
	    return;
	}

	if (name.equals("terra:hint")) {
	    // remove "&lt;br&gt;<br>" from the end
	    int indexTrash = strData.indexOf("&lt;br&gt;<br>");
	    if (indexTrash > 0)
		holder.getCacheDetails(false).Hints = STRreplace.replace(STRreplace.replace(Common.rot13(strData.substring(0, indexTrash)), "\n", "<br>"), "\t", "");
	    return;
	}

	if (name.equals("groundspeak:attribute")) {
	    if (attID.equals("")) {
		attID = Attribute.getIdFromGCText(strData);
	    }
	    if (attInc == null)
		attInc = "1";
	    int id = Integer.parseInt(attID);
	    holder.getCacheDetails(false).attributes.add(id, attInc);
	    holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
	    return;
	}

    }

    public void characters(char[] ch, int start, int length) {
	strBuf.append(ch, start, length);
	if (debugGPX)
	    Preferences.itself().log("Char: " + strBuf.toString(), null);
    }

    public static String TCSizetoText(String size) {
	if (size.equals("1"))
	    return "Micro";
	if (size.equals("2"))
	    return "Medium";
	if (size.equals("3"))
	    return "Regular";
	if (size.equals("4"))
	    return "Large";
	if (size.equals("5"))
	    return "Very Large";

	return "None";
    }

    private void getImages() {
	String addresse;
	String cacheText;
	CacheHolderDetail chD = holder.getCacheDetails(false);

	try {
	    if (fromTC) {
		// special ;
		Preferences.itself().log("[gpx Import]Spider images from TerraCaching not implemented!", null);
	    } else {
		if (fromOC) {
		    chD.images.clear();
		    addresse = chD.URL;
		    cacheText = UrlFetcher.fetch(addresse);
		    Extractor exBeschreibung = new Extractor(cacheText, "<!-- Beschreibung -->", "<!-- End Beschreibung -->", 0, false);
		    String beschreibung = exBeschreibung.findNext();
		    getOCPictures(beschreibung);
		    Extractor exBilder = new Extractor(cacheText, "<!-- Bilder -->", "<!-- End Bilder -->", 0, false);
		    String bilder = exBilder.findNext();
		    getOCPictures(bilder);
		} else {
		    String wayPoint = holder.getWayPoint();
		    if (wayPoint.startsWith("GC")) {
			imgSpider.fetchWayPointPage(wayPoint);
			chD.setLongDescription(imgSpider.getDescription(0));
			imgSpider.getImages(chD);
			// todo if Attributes are in the gpx (Version 1.1.0) : don't spider them
			imgSpider.getAttributes(chD);
		    }
		}
	    }
	} catch (Exception e1) {
	    // e1.printStackTrace();
	}
    }

    private void getOCPictures(String html) {
	Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)");
	imgRegexUrl.setIgnoreCase(true);
	int descIndex = 0;
	while (imgRegexUrl.searchFrom(html, descIndex)) {
	    descIndex = imgRegexUrl.matchedTo();
	    String fetchUrl = imgRegexUrl.stringMatched(2); // URL in Anführungszeichen in (2)
	    if (fetchUrl == null) {
		fetchUrl = imgRegexUrl.stringMatched(3);
	    } // falls ohne in (3)
	    if (fetchUrl == null) {
		continue;
	    } // schlechtes html
	      // fetchUrl ist auf jeden Fall ohne Anführungszeichen
	    if (fetchUrl.startsWith("resource"))
		continue; //
	    if (fetchUrl.startsWith("images")) // z.B. Flaggen
		if (!fetchUrl.startsWith("images/uploads"))
		    continue;
	    if (fetchUrl.startsWith("thumbs"))
		continue; // z.B. Flaggen
	    try {
		// TODO this is not quite correct: actually the "base" URL must be known...
		// but anyway a different baseURL should not happen very often - it doesn't in my area
		String hostname = OC.getOCHostName(holder.getWayPoint());
		if (!fetchUrl.startsWith("http://")) {
		    fetchUrl = new URL(new URL("http://" + hostname + "/"), fetchUrl).toString();
		}
	    } catch (MalformedURLException e) {
		// auch egal
		continue;
	    }
	    CacheImage imageInfo = new CacheImage();
	    imageInfo.setURL(fetchUrl);
	    imageInfo.setTitle(makeTitle(imgRegexUrl.stringMatched(1), fetchUrl));
	    getOCPicture(imageInfo);
	}

	Extractor exHref = new Extractor(html, "<a href=", "</a>", 0, true);
	String href = "";
	Extractor exHttp = new Extractor(href, "http://", "\"", 0, true);
	while ((href = exHref.findNext()).length() > 0) {
	    exHttp.set(href, "http://", "\"", 0, true);
	    String fetchUrl = exHttp.findNext();
	    if (fetchUrl.length() > 0) {
		try {
		    String imgType = (fetchUrl.substring(fetchUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
		    fetchUrl = "http://" + fetchUrl.substring(0, fetchUrl.lastIndexOf('.') + imgType.length());
		    if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
			CacheImage imageInfo = new CacheImage();
			imageInfo.setURL(fetchUrl);
			imageInfo.setTitle(makeTitle(href, fetchUrl));
			getOCPicture(imageInfo);
		    }
		} catch (IndexOutOfBoundsException e) {
		}
	    }
	}
    }

    private String makeTitle(String imgTag, String fetchUrl) {
	Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))");
	imgRegexAlt.setIgnoreCase(true);
	String imgAltText;
	if (imgRegexAlt.search(imgTag)) {
	    imgAltText = imgRegexAlt.stringMatched(1);
	    if (imgAltText == null)
		imgAltText = imgRegexAlt.stringMatched(2);
	} else { // no alternative text as image title -> use --- or filename
		 // wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend
	    if (fetchUrl.toLowerCase().indexOf("opencaching.") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0)
		imgAltText = "---"; // no image title
	    else
		imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/') + 1);
	}
	return imgAltText;
    }

    private void getOCPicture(CacheImage imageInfo) {
	String fileName = holder.getWayPoint() + "_" + imageInfo.getURL().substring(imageInfo.getURL().lastIndexOf('/') + 1);
	fileName = Common.ClearForFileName(fileName).toLowerCase();
	String target = MainForm.profile.dataDir + fileName;
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
	} catch (IOException e) {
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
	    // ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: " + fileName + " from URL:" + imageInfo.getURL();
	    //else {
	    if (e.getMessage().equalsIgnoreCase("could not connect") || e.getMessage().equalsIgnoreCase("unkown host")) {
		// is there a better way to find out what happened?
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + ")" + MyLocale.getMsg(1619, ": could not download image from URL: ") + imageInfo.getURL();
	    } else
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + "): ignoring IOException: " + e.getMessage() + " while downloading picture:" + fileName + " from URL:" + imageInfo.getURL();
	    //}
	    Preferences.itself().log(ErrMessage, e, true);
	}

    }

    public int getHow() {
	return doitHow;
    }
}
