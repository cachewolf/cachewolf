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

import org.json.JSONArray;
import org.json.JSONObject;

import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.Preferences;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheImage;
import CacheWolf.database.CacheImages;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.database.Log;
import CacheWolf.database.LogList;
import CacheWolf.database.Travelbug;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.Track;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.BetterUTF8Codec;
import CacheWolf.utils.Common;
import CacheWolf.utils.DateFormat;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.UrlFetcher;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.net.URL;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Properties;
import ewe.util.Utils;
import ewe.util.Vector;
import ewe.util.mString;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to spider caches from gc.com
 */
public class GCImporter {

    public static boolean loggedIn = false;

    // Return values for spider action
    /** Ignoring a premium member cache when spidering from a non premium account  */
    public static int SPIDER_IGNORE_PREMIUM = -2;
    /** Canceling spider process */
    public static int SPIDER_CANCEL = -1;
    /** Error occured while spidering */
    public static int SPIDER_ERROR = 0;
    /** Cache was spidered without problems */
    public static int SPIDER_OK = 1;
    /** no probs, but exmpl found und not want this */
    public static int SPIDER_IGNORE = 2;

    /**
     * This is the pattern for inlined smilies
     */
    private static final String iconsRelativePath = "<img src=\"/images/icons/";
    private int spiderErrors;
    private static double minDistance = 0;
    private static double maxDistance = 0;

    private final Vector cachesToLoad = new Vector();
    private Hashtable cachesToUpdate;
    private InfoBox infB;
    private static SpiderProperties p = null;
    // following filled at doit
    private CWPoint origin;
    private String htmlListPage;
    private final static String loginPageUrl = "https://www.geocaching.com/login/default.aspx";

    ImportGui importGui;
    private boolean spiderAllFinds;

    private String cacheTypeRestriction;
    private byte restrictedCacheType = 0;
    private boolean doNotgetFound;
    private int maxUpdate;

    static Extractor extractor = new Extractor();
    Extractor extractValue = new Extractor();

    private static String urlSeek;
    private static String queryLat;
    private static String queryLon;
    private static String queryUserFinds;
    private static String queryDistance;
    private final static String gotoNextPage = "ctl00$ContentBody$pgrTop$ctl08";
    // change to the block (10pages) of the wanted page
    private static String gotoPreviousBlock = "ctl00$ContentBody$pgrTop$ctl05";
    private static String gotoNextBlock = "ctl00$ContentBody$pgrTop$ctl06";
    // add pagenumber
    private static String gotoPage = "ctl00$ContentBody$pgrTop$lbGoToPage_";
    private static String queryDoNotGetFound;
    private static Regex RexPropListBlock;
    private static Regex RexPropLine;
    private static Regex RexNumFinds;
    private static Regex logDateRex;
    private static String propAvailable;
    private static String propArchived;
    private static String propFound;
    private static String propPM;

    private static Regex DistDirRex;
    private static Regex DTSRex;

    private final static String wayPointUrl = "http://www.geocaching.com/seek/cache_details.aspx?wp=";
    private String wayPointPage;
    private static Regex difficultyRex;
    private static Regex terrainRex;
    private static Regex cacheTypeRex;
    private static Regex cacheNameRex;
    private static Regex cacheOwnerRex;
    private static Regex dateHiddenRex;
    private static Regex sizeRex;
    private static Regex latLonRex;
    private static Regex cacheLocationRex;
    private static Regex shortDescRex;
    private static Regex longDescRex;
    private static Regex hintsRex;

    private static String premiumGeocache = "> Premium Member Only Cache";
    private static String unpublishedGeocache = "Unpublished Geocache";
    private static String unavailableGeocache = "This cache is temporarily unavailable";
    private static String archivedGeocache = "This cache has been archived";
    private static String foundByMe = "ctl00_ContentBody_GeoNav_logText";
    // private static String correctedCoordinate = "\"oldLatLngDisplay\":"; //#1200
    // private static String premiumCacheForPM = "Warning NoBottomSpacing" is a PM-Cache for PMs

    // images Spoiler
    private static String spoilerSectionStart, spoilerSectionEnd, spoilerSectionStart2;
    private static String imgCommentExStart, imgCommentExEnd;

    // attributes
    private static String attBlockExStart;
    private static String attBlockExEnd;
    private static String attExStart;
    private static String attExEnd;

    private static Regex RexPropWaypoint;
    private static Regex RexPropType;
    private static Regex RexUserToken;
    private static String icon_smile;
    private static String icon_camera;
    private static String icon_attended;

    private int numFoundUpdates = 0;
    private int numArchivedUpdates = 0;
    private int numAvailableUpdates = 0;
    private int numLogUpdates = 0;
    private int numPrivate = 0;
    private int page_number = 1;

    public GCImporter() {
	initialiseProperties();
    }

    private void initialiseProperties() {
	p = new SpiderProperties();
	try {
	    urlSeek = p.getProp("urlSeek"); // http://www.geocaching.com/seek/nearest.aspx
	    queryLat = p.getProp("queryLat"); // ?lat=
	    queryLon = p.getProp("queryLon"); // &lng=
	    queryUserFinds = p.getProp("queryUserFinds"); // ?ul=
	    queryDistance = p.getProp("queryDistance"); // &dist=
	    queryDoNotGetFound = p.getProp("queryDoNotGetFound"); // &f=1

	    RexPropListBlock = new Regex(p.getProp("listBlockRex"));
	    RexPropLine = new Regex(p.getProp("lineRex"));
	    RexNumFinds = new Regex("Total Records: <b>(.*?)</b>");
	    logDateRex = new Regex(p.getProp("logDateRex")); // newFoundExists

	    propAvailable = p.getProp("Available");
	    propArchived = p.getProp("Archived");
	    propPM = p.getProp("PM");
	    propFound = p.getProp("found");

	    DistDirRex = new Regex(p.getProp("DistDirRex"));
	    DTSRex = new Regex(p.getProp("DTSRex"));

	    RexPropWaypoint = new Regex(p.getProp("waypointRex"));
	    RexPropType = new Regex(p.getProp("TypeRex"));
	    RexUserToken = new Regex(p.getProp("UserTokenRex"));
	    icon_smile = p.getProp("icon_smile");
	    icon_camera = p.getProp("icon_camera");
	    icon_attended = p.getProp("icon_attended");

	    difficultyRex = new Regex(p.getProp("difficultyRex"));
	    terrainRex = new Regex(p.getProp("terrainRex"));
	    cacheTypeRex = new Regex(p.getProp("cacheTypeRex"));
	    cacheNameRex = new Regex(p.getProp("cacheNameRex"));
	    cacheOwnerRex = new Regex(p.getProp("cacheOwnerRex"));
	    dateHiddenRex = new Regex(p.getProp("dateHiddenRex"));
	    sizeRex = new Regex(p.getProp("sizeRex"));
	    latLonRex = new Regex(p.getProp("latLonRex"));
	    cacheLocationRex = new Regex(p.getProp("cacheLocationRex"));
	    shortDescRex = new Regex(p.getProp("shortDescRex"));
	    longDescRex = new Regex(p.getProp("longDescRex"));
	    hintsRex = new Regex(p.getProp("hintsRex"));

	    spoilerSectionStart = p.getProp("spoilerSectionStart");
	    spoilerSectionEnd = p.getProp("spoilerSectionEnd");
	    spoilerSectionStart2 = p.getProp("spoilerSectionStart2");
	    imgCommentExStart = p.getProp("imgCommentExStart");
	    imgCommentExEnd = p.getProp("imgCommentExEnd");

	    attBlockExStart = p.getProp("attBlockExStart");
	    attBlockExEnd = p.getProp("attBlockExEnd");
	    attExStart = p.getProp("attExStart");
	    attExEnd = p.getProp("attExEnd");

	} catch (final Exception ex) {
	    Preferences.itself().log("Error fetching Properties.", ex);
	}
    }

    /**
     * Method to start the spider for a search around the centre coordinates
     */
    public void doIt() {
	doIt(false);
    }

    int lastPageVisited;

    public void doIt(boolean _spiderAllFinds) {
	cachesToLoad.clear();
	spiderAllFinds = _spiderAllFinds;
	// No need to copy curCentrePt as it is only read and not written
	origin = Preferences.itself().curCentrePt;
	if (!spiderAllFinds && !origin.isValid()) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5509, "Coordinates for centre must be set")).wait(FormBase.OKB);
	    return;
	}

	// Reset states for all caches when spidering (http://tinyurl.com/dzjh7p)
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    final CacheHolder ch = MainForm.profile.cacheDB.get(i);
	    if (ch.mainCache == null)
		ch.initStates(false);
	}

	if (doDownloadGui(0)) {

	    Vm.showWait(true);
	    infB = new InfoBox("Status", MyLocale.getMsg(5502, "Fetching first page..."));
	    infB.exec();

	    double lowerDistance;
	    double upperDistance = minDistance;
	    spiderErrors = 0;
	    int pageLimit = 20; // das sorgt für die Gruppierung
	    int maxPages = 0;
	    if (login()) {
		getFirstListPage(this.getDistanceInMiles(maxDistance));
		maxPages = (int) java.lang.Math.ceil(getNumFound(htmlListPage) / 20);

		String s = "ListPages Properties : " + Preferences.NEWLINE;
		s = s + "minDistance          : " + minDistance + Preferences.NEWLINE;
		s = s + "maxDistance          : " + maxDistance + Preferences.NEWLINE;
		s = s + "maxNew               : " + Preferences.itself().maxSpiderNumber + Preferences.NEWLINE;
		s = s + "maxUpdate            : " + maxUpdate + Preferences.NEWLINE;
		s = s + "with Founds          : " + (doNotgetFound ? "no" : "yes") + Preferences.NEWLINE;
		s = s + "alias is premium memb: " + (!Preferences.itself().isPremium ? "no" : "yes") + Preferences.NEWLINE;
		s = s + "Update if new Log    : " + (Preferences.itself().checkLog ? "yes" : "no") + Preferences.NEWLINE;
		s = s + "Update if TB changed : " + (Preferences.itself().checkTBs ? "yes" : "no") + Preferences.NEWLINE;
		s = s + "Update if DTS changed: " + (Preferences.itself().checkDTS ? "yes" : "no") + Preferences.NEWLINE;
		s = s + "maxPages for x Miles : " + maxPages + " for " + this.getDistanceInMiles(maxDistance) + Preferences.NEWLINE;
		Preferences.itself().log(s, null);

		lastPageVisited = -1; // for not to double check pages on next group run

		Preferences.itself().log("Download properties : " + Preferences.NEWLINE //
			+ "maxLogs: " + Preferences.itself().maxLogsToSpider + Preferences.NEWLINE //
			+ "with pictures     : " + (!Preferences.itself().downloadPics ? "no" : "yes") + Preferences.NEWLINE //
			+ "with tb           : " + (!Preferences.itself().downloadTBs ? "no" : "yes") + Preferences.NEWLINE, null);

	    } else {
		upperDistance = maxDistance; // Abbruch
	    }
	    while (upperDistance < maxDistance && !infB.isClosed()) {
		lowerDistance = upperDistance;
		if ((int) lowerDistance < ((int) maxDistance - 1)) {
		    upperDistance = this.getUpperDistance(lowerDistance, maxDistance, maxPages, pageLimit);
		} else {
		    upperDistance = maxDistance;
		}

		fillDownloadLists(Preferences.itself().maxSpiderNumber, maxUpdate, upperDistance, lowerDistance);

		if (!infB.isClosed()) {
		    infB.setInfo(MyLocale.getMsg(5511, "Found ") + cachesToLoad.size() + MyLocale.getMsg(5512, " caches"));
		}

		MainTab.itself.tablePanel.updateStatusBar();

		downloadCaches();
		updateCaches();

	    } // while

	    if (spiderErrors > 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), spiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded.")).wait(FormBase.OKB);
	    }

	    MainForm.profile.restoreFilter();
	    MainForm.profile.saveIndex(true);

	    if (!infB.isClosed()) {
		infB.close(0);
	    }
	    Vm.showWait(false);
	    loggedIn = false; // check again login on next spider
	}
    } // End of DoIt

    public void setOldGCLanguage() {
	if (Preferences.itself().changedGCLanguageToEnglish) {
	    GCImporter.setGCLanguage(Preferences.itself().oldGCLanguage);
	    Preferences.itself().changedGCLanguageToEnglish = false;
	}
    }

    private int getDistanceInMiles(double value) {
	// max distance in miles for URL, so we can get more than 80km
	int toDistanceInMiles = (int) java.lang.Math.ceil(value);
	if (Preferences.itself().metricSystem != Metrics.IMPERIAL) {
	    toDistanceInMiles = (int) java.lang.Math.ceil(Metrics.convertUnit(value, Metrics.KILOMETER, Metrics.MILES));
	}
	return toDistanceInMiles;
    }

    private double getUpperDistance(double fromDistance, double toDistance, int maxPages, int pageLimit) {

	int startPage;
	if (fromDistance > 0) {
	    getFirstListPage(this.getDistanceInMiles(fromDistance));
	    startPage = (int) java.lang.Math.ceil(getNumFound(htmlListPage) / 20);
	} else {
	    startPage = 1;
	}

	int endPage = maxPages;

	while ((1 + endPage - startPage) > pageLimit) {
	    toDistance = fromDistance + (toDistance - fromDistance) / 2;
	    if ((int) toDistance <= ((int) fromDistance + 1))
		return toDistance;
	    getFirstListPage(this.getDistanceInMiles(toDistance));
	    endPage = (int) java.lang.Math.ceil(getNumFound(htmlListPage) / 20);
	}
	return toDistance;

    }

    public void doItAlongARoute() {
	Vector points = null;
	Navigate navigate = MainTab.itself.navigate;
	MovingMap movingMap = MainTab.itself.movingMap;
	// vorsichtshalber
	if (navigate == null)
	    return;
	if (movingMap == null)
	    return;
	if (!doDownloadGui(1))
	    return;

	// getting the route
	CWPoint startPos = Preferences.itself().curCentrePt;
	if (!importGui.fileName.equals("")) {
	    final RouteImporter ri = new RouteImporter(importGui.fileName);
	    points = ri.doIt();
	    if (points.size() > 0) {
		if (navigate.curTrack == null) {
		    navigate.curTrack = new Track(navigate.trackColor);
		    movingMap.addTrack(navigate.curTrack);
		}
		for (int i = 0; i < points.size(); i++) {
		    try {
			navigate.curTrack.add((CoordinatePoint) points.get(i));
		    } catch (final IndexOutOfBoundsException e) {
			// track full -> create a new one
			navigate.curTrack = new Track(navigate.trackColor);
			navigate.curTrack.add((CoordinatePoint) points.get(i));
			movingMap.addTrack(navigate.curTrack);
		    }

		}
		final CoordinatePoint tp = (CoordinatePoint) points.get(0);
		startPos = new CWPoint(tp.latDec, tp.lonDec);
	    } else
		startPos = null;
	}

	final boolean complete = true;

	if ((startPos == null) || (startPos != null && !startPos.isValid())) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5509, "Coordinates for centre must be set")).wait(FormBase.OKB);
	    return; //
	}

	Vm.showWait(true);
	infB = new InfoBox("Status", MyLocale.getMsg(5502, "Fetching pages..."));
	infB.exec();

	if (!login())
	    return;

	// Reset states for all caches when spidering
	// (http://tinyurl.com/dzjh7p)
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    final CacheHolder ch = MainForm.profile.cacheDB.get(i);
	    if (ch.mainCache == null)
		ch.initStates(false);
	}

	double lateralDistance = maxDistance; // Seitenabstand in km
	if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
	    lateralDistance = Metrics.convertUnit(maxDistance, Metrics.MILES, Metrics.KILOMETER);
	}
	// Load über die Kreise
	// daher Faktor 1.2
	lateralDistance = 1.2 * lateralDistance;
	cachesToLoad.clear();

	origin = startPos;
	CWPoint nextPos = startPos;
	int pointsIndex = 1;

	if (points != null)
	    Preferences.itself().log("Start at " + origin + " to check " + points.size() + " points.");

	while (nextPos != null) {
	    if (importGui.fileName.length() == 0) {
		nextPos = nextRoutePoint(startPos, lateralDistance);
	    } else {
		double tmpDistance = 0;
		while (tmpDistance < lateralDistance && pointsIndex < points.size()) {
		    final CoordinatePoint tp = (CoordinatePoint) points.get(pointsIndex);
		    nextPos = new CWPoint(tp.latDec, tp.lonDec);
		    tmpDistance = nextPos.getDistance(startPos);
		    pointsIndex++;
		}
		if (pointsIndex == points.size())
		    nextPos = null;
		else {
		    if (points != null)
			MainTab.itself.tablePanel.updateStatusBar("" + pointsIndex + "(" + points.size() + ")" + nextPos);
		}
	    }

	    if (nextPos != null) {
		origin = startPos;
		if (points != null)
		    Preferences.itself().log("m: do " + pointsIndex + " of (" + points.size() + ") at " + origin);
		getCaches(lateralDistance);

		final double degrees = startPos.getBearing(nextPos);
		final double distanceToNextCache = startPos.getDistance(nextPos);
		final double anzCheckPoints = distanceToNextCache / lateralDistance;
		for (int i = 1; i < anzCheckPoints; i++) {
		    final CWPoint nextCheckPoint = startPos.project(degrees, lateralDistance);
		    startPos = nextCheckPoint;
		    origin = nextCheckPoint;
		    if (points != null)
			Preferences.itself().log("s: do " + pointsIndex + " of (" + points.size() + ") at " + origin);
		    getCaches(lateralDistance);
		    if (infB.isClosed())
			break;
		}
		startPos = nextPos;
	    }
	    if (infB.isClosed())
		break;
	}
	if (infB.isClosed()) {
	    Vm.showWait(false);
	    return;
	} // or ask for download of intermediate result

	int spiderErrors = 0;
	if (complete) {
	    // vorhandene Cache werden aus der DB gelöscht
	    for (int i = 0; i < cachesToLoad.size(); i++) {
		final int j = MainForm.profile.cacheDB.getIndex((String) cachesToLoad.get(i));
		if (j != -1)
		    MainForm.profile.cacheDB.removeElementAt(j);
	    }
	    // und frisch geladen
	    downloadCaches();
	} else {
	    // man könnte auch aus der Liste einen Quick - Import erstellen
	}

	infB.close(0);
	Vm.showWait(false);
	if (spiderErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), spiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded.")).wait(FormBase.OKB);
	}

	MainForm.profile.restoreFilter();
	MainForm.profile.saveIndex(true);
	loggedIn = false; // check again login on next spider

    }

    private CWPoint nextRoutePoint(CWPoint startPos, double lateralDistance) {
	// get next Destination
	double nextDistance = 0;
	int index = -1;
	CacheHolder nextCache = null;
	CacheHolder ch = null;
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    ch = MainForm.profile.cacheDB.get(i);
	    if (ch.is_Checked && ch.getPos().isValid()) {
		final CWPoint tmpPos = ch.getPos();
		final double tmpDistance = tmpPos.getDistance(startPos);
		if (nextDistance == 0) {
		    // Startwert
		    index = i;
		    nextDistance = tmpDistance;
		    nextCache = ch;
		    nextCache.is_Checked = false;
		} else {
		    if (tmpDistance > lateralDistance) {
			if (tmpDistance < nextDistance) {
			    index = i;
			    nextDistance = tmpDistance;
			    nextCache = ch;
			    nextCache.is_Checked = false;
			}
		    } else {
			ch.is_Checked = false;
		    }
		}
	    }
	}
	if (index > -1) {
	    return MainForm.profile.cacheDB.get(index).getPos();
	} else
	    return null;
    }

    private void getCaches(double radiusKm) {
	// von origin aus
	// kein update vorhandener Cache, da
	int toDistance = (int) java.lang.Math.ceil(radiusKm);
	getFirstListPage(toDistance);
	// Number of caches from gcfirst Listpage
	int numFinds = getNumFound(htmlListPage);
	if (numFinds == 0)
	    return;
	page_number = 1;
	int found_on_page = 0;
	try {
	    // Loop pages till maximum distance has been found or no more caches are in the list
	    while (toDistance > 0) {
		double distance = 0.0;
		RexPropListBlock.search(htmlListPage);
		String tableOfHtmlListPage;
		if (RexPropListBlock.didMatch()) {
		    tableOfHtmlListPage = RexPropListBlock.stringMatched(1);
		} else {
		    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check listBlockRex!");
		    tableOfHtmlListPage = "";
		}
		RexPropLine.search(tableOfHtmlListPage);
		while (toDistance > 0) {
		    if (!RexPropLine.didMatch()) {
			if (page_number == 1 && found_on_page == 0)
			    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check lineRex!");
			break;
		    }
		    found_on_page++;
		    MainTab.itself.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
		    final String CacheDescriptionGC = RexPropLine.stringMatched(1);
		    distance = getDistance(CacheDescriptionGC);
		    String chWaypoint = getWP(CacheDescriptionGC);
		    if (distance <= radiusKm) {
			final CacheHolder ch = MainForm.profile.cacheDB.get(chWaypoint);
			if (ch == null) { // not in DB
			    if (!cachesToLoad.contains(chWaypoint)) {
				cachesToLoad.add(chWaypoint);
				infB.setInfo(MyLocale.getMsg(5511, "Found ") + cachesToLoad.size());
			    }
			}
		    } else
			// finish this htmlListPage
			toDistance = 0;
		    RexPropLine.searchFrom(tableOfHtmlListPage, RexPropLine.matchedTo());
		    if (infB.isClosed()) {
			toDistance = 0;
			break;
		    }
		} // next Cache
		if (found_on_page < 20)
		    toDistance = 0;
		if (toDistance > 0) {
		    if (page_number % 100 == 45) {
			getAListPage(toDistance, gotoPreviousBlock);
			getAListPage(toDistance, gotoNextBlock);
		    }
		    if (getAListPage(toDistance, gotoNextPage)) {
			page_number++;
			found_on_page = 0;
		    }
		}
	    } // loop pages
	    Preferences.itself().log("Nr Caches now: " + cachesToLoad.size());
	} // try
	catch (final Exception ex) {
	    Preferences.itself().log("Download error : ", ex, true);
	    infB.close(0);
	    Vm.showWait(false);
	}
    }

    private boolean doDownloadGui(int menu) {
	int options = ImportGui.ISGC | ImportGui.IMAGES | ImportGui.MAXLOGS | ImportGui.TRAVELBUGS;
	if (spiderAllFinds) {
	    options = options | ImportGui.MAXUPDATE;
	    if (Preferences.itself().askForMaxNumbersOnImport) {
		options = options | ImportGui.MAXNUMBER;
	    }
	    importGui = new ImportGui(MyLocale.getMsg(217, "Spider all finds from geocaching.com"), options);
	    // setting defaults for input
	    importGui.maxNumberUpdates.setText("0");
	} else if (menu == 0) {
	    options = options | ImportGui.TYPE | ImportGui.DIST | ImportGui.INCLUDEFOUND;
	    if (Preferences.itself().askForMaxNumbersOnImport) {
		options = options | ImportGui.MAXNUMBER | ImportGui.MAXUPDATE;
	    }
	    importGui = new ImportGui(MyLocale.getMsg(131, "Download from geocaching.com"), options);
	} else if (menu == 1) {
	    options = options | ImportGui.TYPE | ImportGui.DIST | ImportGui.INCLUDEFOUND | ImportGui.FILENAME;
	    importGui = new ImportGui(MyLocale.getMsg(137, "Download along a Route from geocaching.com"), options);
	} else {
	    return false;
	}

	// doing the input
	if (importGui.execute() == FormBase.IDCANCEL) {
	    return false;
	}

	//
	cacheTypeRestriction = importGui.getCacheTypeRestriction(p);
	restrictedCacheType = importGui.getRestrictedCacheType(p);
	//
	minDistance = 0.0; // no longer really used
	// MainForm.profile.setMinDistGC(Convert.toString(0).replace(',', '.'));
	//
	if ((options & ImportGui.DIST) > 0) {
	    maxDistance = importGui.getDoubleFromInput(importGui.maxDistanceInput, 0);
	    if (maxDistance == 0)
		return false;
	    if (menu == 1) {
		// "along the route" mindenstens 500 meter Umkreis
		if (maxDistance < 0.5)
		    maxDistance = 0.5;
	    }
	    MainForm.profile.setDistGC(Convert.toString(maxDistance));
	} else {
	    maxDistance = 1.0; // only to be > minDistance
	}
	//
	Preferences.itself().maxSpiderNumber = importGui.getIntFromInput(importGui.maxNumberInput, Integer.MAX_VALUE);
	//
	maxUpdate = importGui.getIntFromInput(importGui.maxNumberUpdates, Integer.MAX_VALUE);
	if (menu == 1) {
	    maxUpdate = 0;
	}
	//
	if ((options & ImportGui.INCLUDEFOUND) > 0) {
	    doNotgetFound = importGui.foundCheckBox.getState();
	    Preferences.itself().doNotGetFound = doNotgetFound;
	}

	importGui.close(0);
	return true;
    }

    private void fillDownloadLists(int maxNew, int maxUpdate, double toDistance, double fromDistance) {
	if (cachesToUpdate == null) {
	    cachesToUpdate = new Hashtable(MainForm.profile.cacheDB.size());
	} else {
	    cachesToUpdate.clear();
	}
	int numFinds;
	int startPage = 1;
	// max distance in miles for URL, so we can get more than 80km
	// get pagenumber of page with fromDistance , to skip reading of pages < fromDistance
	int fromDistanceInMiles = 0;
	if (fromDistance > 0) {
	    fromDistanceInMiles = this.getDistanceInMiles(fromDistance) - 1;
	    getFirstListPage(java.lang.Math.max(fromDistanceInMiles, 1));
	    if (lastPageVisited > -1) {
		startPage = lastPageVisited;
	    } else {
		// Number of caches from gc Listpage calc the number of the startpage
		startPage = (int) java.lang.Math.ceil(getNumFound(htmlListPage) / 20);
	    }
	}
	lastPageVisited = startPage;
	// add a mile to be save from different distance calculations in CW and at GC
	int toDistanceInMiles = this.getDistanceInMiles(toDistance) + 1;
	getFirstListPage(toDistanceInMiles);
	// Number of caches from gcfirst Listpage
	numFinds = getNumFound(htmlListPage);
	if (fromDistance > 0) {
	    // skip (most of) the pages with distance < fromDistance
	    for (int i = 0; i < (startPage / 10); i++) {
		getAListPage(toDistanceInMiles, gotoNextBlock);
	    }
	    if (startPage > 1) {
		if (startPage % 10 == 1)
		    getAListPage(toDistanceInMiles, gotoNextPage);
		else
		    getAListPage(toDistanceInMiles, gotoPage + startPage);
	    }
	}
	Preferences.itself().log("[SpiderGC:fillDownloadLists] got Listpage: " + startPage, null);

	int endPage = (int) (numFinds / 20);
	int anzPages = 1 + endPage - startPage;
	Preferences.itself().log("List up to " + anzPages + " pages (" + startPage + ".." + endPage + "). From " + fromDistanceInMiles + " miles (" + fromDistance + " km/miles)" + " to " + toDistanceInMiles + " miles (" + toDistance + " km/miles)",
		null);
	int numFoundInDB = 0; // Number of GC-founds already in this profile
	if (spiderAllFinds) {
	    numFoundInDB = getFoundInDB();
	    Preferences.itself().log((spiderAllFinds ? "all Finds (DB/GC)" + numFoundInDB + "/" + numFinds : "new and update Caches") + Preferences.NEWLINE, null);
	    maxNew = java.lang.Math.min(numFinds - numFoundInDB, maxNew);
	    if (maxUpdate == 0 && maxNew == 0) {
		Vm.showWait(false);
		infB.close(0);
		cachesToUpdate.clear();
		return;
	    }
	}

	if (maxUpdate > 0) {
	    double distanceInKm = toDistance;
	    if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
		distanceInKm = Metrics.convertUnit(toDistance, Metrics.MILES, Metrics.KILOMETER);
	    }

	    double fromDistanceInKm = fromDistance;
	    if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
		fromDistanceInKm = Metrics.convertUnit(fromDistance, Metrics.MILES, Metrics.KILOMETER);
	    }

	    // expecting all are changed (archived caches remain always)
	    for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
		final CacheHolder ch = MainForm.profile.cacheDB.get(i);
		if (spiderAllFinds) {
		    if ((ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC")) && !ch.is_black()) {
			cachesToUpdate.put(ch.getWayPoint(), ch);
		    }
		} else {
		    if ((!ch.is_archived()) && (ch.kilom >= fromDistanceInKm) && (ch.kilom <= distanceInKm) && !(doNotgetFound && (ch.is_found() || ch.is_owned())) && (ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC"))
			    && ((restrictedCacheType == CacheType.CW_TYPE_ERROR) || (ch.getType() == restrictedCacheType)) && !ch.is_black()) {
			cachesToUpdate.put(ch.getWayPoint(), ch);
		    }
		}
	    }
	}
	// for save reasons
	final int startSize = cachesToUpdate.size();

	// for don't loose the already done work
	final Hashtable cFoundForUpdate = new Hashtable(MainForm.profile.cacheDB.size());
	page_number = 1;
	int found_on_page = 0;
	try {
	    // Loop pages till maximum distance has been found or no more caches are in the list
	    while (toDistance > 0) {
		RexPropListBlock.search(htmlListPage);
		String tableOfHtmlListPage;
		if (RexPropListBlock.didMatch()) {
		    tableOfHtmlListPage = RexPropListBlock.stringMatched(1);
		} else {
		    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check listBlockRex!");
		    tableOfHtmlListPage = "";
		}
		RexPropLine.search(tableOfHtmlListPage);
		double distance = 0.0;
		while (toDistance > 0) {
		    if (!RexPropLine.didMatch()) {
			if (page_number == 1 && found_on_page == 0)
			    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check lineRex!");
			break;
		    }
		    found_on_page++;
		    MainTab.itself.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
		    final String CacheDescriptionGC = RexPropLine.stringMatched(1);
		    distance = getDistance(CacheDescriptionGC);
		    String chWaypoint = getWP(CacheDescriptionGC);
		    if (distance <= toDistance) {
			final CacheHolder ch = MainForm.profile.cacheDB.get(chWaypoint);
			if (ch == null) { // not in DB
			    if (distance >= fromDistance && doPMCache(chWaypoint, CacheDescriptionGC) && cachesToLoad.size() < maxNew) {
				if (!cachesToLoad.contains(chWaypoint)) {
				    cachesToLoad.add(chWaypoint);
				}
			    } else {
				// Preferences.itself().log("no load of (Premium Cache/other direction/short
				// Distance ?) " + chWaypoint);
				cachesToUpdate.remove(chWaypoint);
			    }
			} else {
			    if (maxUpdate > 0) {
				// regardless of fromDistance
				if (!ch.is_black()) {
				    if (doPMCache(chWaypoint, CacheDescriptionGC) && updateExists(ch, CacheDescriptionGC)) {
					if (cFoundForUpdate.size() < maxUpdate) {
					    cFoundForUpdate.put(chWaypoint, ch);
					} else
					    cachesToUpdate.remove(chWaypoint);
				    } else
					cachesToUpdate.remove(chWaypoint);
				} else
				    cachesToUpdate.remove(chWaypoint);
			    }
			}
			if (cachesToLoad.size() >= maxNew - numPrivate) {
			    if (cFoundForUpdate.size() >= maxUpdate) {
				toDistance = 0;
				cachesToUpdate.clear();
			    } else {
				if (cachesToUpdate.size() <= cFoundForUpdate.size()) {
				    toDistance = 0;
				}
			    }
			}
		    } else
			// finish listing get next row of table (next Cache Description) of this htmlListPage
			toDistance = 0;
		    RexPropLine.searchFrom(tableOfHtmlListPage, RexPropLine.matchedTo());
		    if (infB.isClosed()) {
			toDistance = 0;
			cachesToUpdate.clear();
			break;
		    }
		} // next Cache

		infB.setInfo(MyLocale.getMsg(5511, "Found ") + cachesToLoad.size() + " / " + cFoundForUpdate.size() + MyLocale.getMsg(5512, " caches"));
		if (found_on_page < 20) {
		    if (spiderAllFinds) {
			// check all pages ( seen a gc-account with found_on_page less 20 and not on end )
			if (((page_number - 1) * 20 + found_on_page) >= numFinds) {
			    toDistance = 0;
			}
		    } else {
			// toDistance = 0; // last page (has less than 20 entries!?) to check reached
			// ??? http://www.geoclub.de/viewtopic.php?f=40&t=61614
			if (((page_number - 1) * 20 + found_on_page) >= numFinds) {
			    toDistance = 0;
			}
		    }
		}

		if (toDistance > 0) {
		    if (getAListPage(toDistanceInMiles, gotoNextPage)) {
			lastPageVisited++;
			Preferences.itself().log("[SpiderGC:fillDownloadLists] got Listpage: " + lastPageVisited, null);
			page_number++;
			found_on_page = 0;
		    } else {
			// stop, but download new ones if possible
			cachesToUpdate.clear();
			Preferences.itself().log("[SpiderGC:fillDownloadLists] Stopped at page number: " + page_number + " this is distance: " + distance, null);
			found_on_page = 0;
			toDistance = 0;
		    }
		}
	    } // loop pages
	} // try
	catch (final Exception ex) {
	    Preferences.itself().log("Download error : ", ex, true);
	    infB.close(0);
	    Vm.showWait(false);
	    cachesToUpdate.clear();
	}

	String s = "Checked " + page_number + " pages" + Preferences.NEWLINE;
	s = s + "with " + ((page_number - 1) * 20 + found_on_page) + " caches" + Preferences.NEWLINE;
	s = s + "Found " + cachesToLoad.size() + " new caches" + Preferences.NEWLINE;
	s = s + "Found " + cachesToUpdate.size() + "/" + cFoundForUpdate.size() + " caches for update" + Preferences.NEWLINE;
	s = s + "Found " + (cachesToUpdate.size() - cFoundForUpdate.size()) + " caches possibly archived." + Preferences.NEWLINE;
	s = s + "Found " + numPrivate + " Premium Caches (for non Premium Member.)" + Preferences.NEWLINE;
	s = s + "Found " + numAvailableUpdates + " caches with changed available status." + Preferences.NEWLINE;
	s = s + "Found " + numLogUpdates + " caches with new found in log." + Preferences.NEWLINE;
	s = s + "Found " + numFoundUpdates + " own Finds" + Preferences.NEWLINE;
	s = s + "Found " + numArchivedUpdates + " unarchived." + Preferences.NEWLINE;
	Preferences.itself().log(s, null);

	if (spiderAllFinds) {
	    Preferences.itself().log("Found " + numFoundUpdates + " caches with no found in MainForm.profile." + Preferences.NEWLINE + "Found " + numArchivedUpdates + " caches with changed archived status." + Preferences.NEWLINE, null);
	}

	if (cachesToUpdate.size() == startSize)
	    cachesToUpdate.clear(); // there must be something wrong
	if (cachesToUpdate.size() == 0 // prima, alle tauchen in der gc-Liste auf
		|| cachesToUpdate.size() > maxUpdate // Restmenge zu gross, wir nehmen nur die sicher geänderten.
	) {
	    cachesToUpdate = cFoundForUpdate;
	} else {
	    // checking if all is in List by adding the known changed ones
	    Preferences.itself().log("possibly " + cachesToUpdate.size() + " + known " + cFoundForUpdate.size(), null);
	    for (final Enumeration e = cFoundForUpdate.elements(); e.hasMoreElements();) {
		final CacheHolder ch = (CacheHolder) e.nextElement();
		cachesToUpdate.put(ch.getWayPoint(), ch);
	    }
	    Preferences.itself().log("now will update: " + cachesToUpdate.size(), null);
	}
	s = "These Caches will be updated :" + Preferences.NEWLINE;
	s = s + "Out of " + startSize + Preferences.NEWLINE;
	for (final Enumeration e = cachesToUpdate.elements(); e.hasMoreElements();) {
	    final CacheHolder ch = (CacheHolder) e.nextElement();
	    s = s + ch.getWayPoint() + "(" + ch.kilom + " km )";
	    if (cFoundForUpdate.containsKey(ch.getWayPoint())) {
		s = s + " sure";
	    }
	    s = s + Preferences.NEWLINE;
	}
	Preferences.itself().log(s, null);
    }

    private void downloadCaches() {
	for (int i = 0; i < cachesToLoad.size(); i++) {
	    if (infB.isClosed())
		break;
	    String wpt = (String) cachesToLoad.get(i);
	    // Get only caches not already available in the DB
	    if (MainForm.profile.cacheDB.getIndex(wpt) == -1) {
		infB.setInfo(MyLocale.getMsg(5531, "New: ") + wpt + " (" + (i + 1) + " / " + cachesToLoad.size() + ")");
		final CacheHolder ch = new CacheHolder(wpt);
		ch.initStates(true);
		final int test = getCacheByWaypointName(ch, Preferences.itself().downloadPics, Preferences.itself().downloadTBs);
		if (test == SPIDER_CANCEL) {
		    infB.close(0);
		    break;
		} else if (test == SPIDER_ERROR) {
		    spiderErrors++;
		} else if (test == SPIDER_OK) {
		    MainForm.profile.cacheDB.add(ch);
		    ch.save();
		} // For test == SPIDER_IGNORE_PREMIUM and SPIDER_IGNORE there is nothing to do
	    }
	}
    }

    private void updateCaches() {
	int jj = 0;
	for (final Enumeration e = cachesToUpdate.elements(); e.hasMoreElements();) {
	    if (infB.isClosed())
		break;
	    final CacheHolder ch = (CacheHolder) e.nextElement();
	    jj++;
	    infB.setInfo(MyLocale.getMsg(5530, "Update: ") + ch.getWayPoint() + " (" + (jj) + " / " + cachesToUpdate.size() + ")");
	    final int test = spiderSingle(MainForm.profile.cacheDB.getIndex(ch), infB);
	    if (test == SPIDER_CANCEL) {
		break;
	    } else {
		if (test == SPIDER_ERROR) {
		    spiderErrors++;
		    Preferences.itself().log("[updateCaches] could not spider " + ch.getWayPoint(), null);
		} else {
		    // MainForm.profile.hasUnsavedChanges=true;
		    if (test == SPIDER_IGNORE_PREMIUM)
			spiderErrors++;
		}
	    }
	}
    }

    /**
     * Method to spider a single cache.
     * 
     * @return 1 if spider was successful, -1 if spider was cancelled (by closing the infobox etc...), 0 error, but continue with next cache
     */
    public int spiderSingle(int number, InfoBox pInfB) {
	int ret = SPIDER_CANCEL;
	if (login()) {
	    this.infB = pInfB;
	    final CacheHolder cacheInDB = MainForm.profile.cacheDB.get(number);
	    if (cacheInDB.isAddiWpt())
		return SPIDER_ERROR; // addi waypoint, comes with parent cache
	    try {
		final CacheHolder ch = new CacheHolder(cacheInDB.getWayPoint());
		ch.initStates(false);
		ret = getCacheByWaypointName(ch, Preferences.itself().downloadPics, Preferences.itself().downloadTBs);
		// Save the spidered data
		if (ret == SPIDER_OK) {
		    cacheInDB.initStates(false);
		    // preserve rating information
		    ch.setNumRecommended(cacheInDB.getNumRecommended());
		    if (!Preferences.itself().downloadPics) {
			// use existing images, if not downloaded
			ch.getCacheDetails(false).images = cacheInDB.getCacheDetails(true).images;
		    }
		    cacheInDB.update(ch);
		    cacheInDB.save();
		}
	    } catch (final Exception ex) {
		Preferences.itself().log("[spiderSingle] Error spidering " + cacheInDB.getWayPoint(), ex);
	    }
	}
	return ret;
    } // spiderSingle

    /**
     * Fetch the coordinates of a waypoint from GC
     * 
     * @param wayPoint
     *            the name of the waypoint
     * @return the cache coordinates
     */
    public String fetchCacheCoordinates(String wayPoint) {
	if (!login())
	    return "";
	final InfoBox localInfB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
	localInfB.exec();
	try {
	    final String url = wayPointUrl + wayPoint;
	    wayPointPage = UrlFetcher.fetch(url);
	    Preferences.itself().log("Fetched " + wayPoint);
	} catch (final Exception ex) {
	    wayPointPage = "";
	    Preferences.itself().log("[getCacheCoordinates] Could not fetch " + wayPoint, ex);
	}
	localInfB.close(0);
	loggedIn = false; // check again login on next spider
	try {
	    return getLatLon();
	} catch (final Exception ex) {
	    return "???";
	}
    }

    /**
     * login to geocaching.com
     */
    private boolean login() {

	if (loggedIn) {
	    return true;
	}

	loggedIn = false;
	int retrycount = -1;
	int maxretries = 1;
	boolean retry = false;

	do {
	    retry = false;
	    retrycount = retrycount + 1;
	    // we try to get a userId by logging in with username and password
	    if (Preferences.itself().userID.length() == 0) {
		if (gcLogin()) {
		    UrlFetcher.rememberCookies();
		    Preferences.itself().userID = UrlFetcher.getCookie("userid;www.geocaching.com");
		    if (Preferences.itself().userID == null) {
			Preferences.itself().userID = "";
			new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5524, "Please correct your account in preferences\n\n see http://cachewolf.aldos.de/userid.html !")).wait(FormBase.OKB);
			return false;
		    }
		} else {
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5525, "Perhaps GC is not available. This should not happen!")).wait(FormBase.OKB);
		    return false;
		}
	    }

	    if (Preferences.itself().userID.length() > 0) {
		// we have a saved userID (perhaps invalid)
		switch (checkGCSettings()) {
		case 0:
		    loggedIn = true;
		    Preferences.itself().userID = UrlFetcher.getCookie("userid;www.geocaching.com");
		    Preferences.itself().savePreferences();
		    break;
		case 1:
		    // language not set to "en-US" , we couldn't change
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5526, "Couldn't change language to EN.")).wait(FormBase.OKB);
		    break;
		case 2:
		    // exception on http://www.geocaching.com/account/ManagePreferences.aspx
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5527, "Exception on ManagePreferences.aspx")).wait(FormBase.OKB);
		    break;
		case 3:
		    // "Metric" : "Imperial" don't correspond
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5528, "Change Metric/Imperial (km / mi) manually. (GC or CW)")).wait(FormBase.OKB);
		    break;
		case 4:
		    break;
		case 5:
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5529, "got no SessionID")).wait(FormBase.OKB);
		    break;
		case 6:
		    // no correct login
		    Preferences.itself().userID = "";
		    if (retrycount < maxretries)
			retry = true;
		    else {
			retry = false;
			new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5524, "Please correct your account in preferences\n\n see http://cachewolf.aldos.de/userid.html !")).wait(FormBase.OKB);
		    }
		    break;
		default:
		}
	    }

	} while (retry);
	return loggedIn;
    }

    private int checkGCSettings() {
	String page = "";
	String url = "http://www.geocaching.com/account/ManagePreferences.aspx";
	UrlFetcher.clearCookies();
	UrlFetcher.setCookie("userid;www.geocaching.com", Preferences.itself().userID);
	try {
	    page = UrlFetcher.fetch(url); // getting the sessionid
	} catch (final Exception ex) {
	    Preferences.itself().log("[checkGCSettings]:Exception calling " + url + " with userID " + Preferences.itself().userID, ex);
	    return 2;
	}

	UrlFetcher.rememberCookies();
	String SessionId = UrlFetcher.getCookie("ASP.NET_SessionId;www.geocaching.com");
	if (SessionId == null) {
	    Preferences.itself().log("[checkGCSettings]:got no SessionID.");
	    return 5;
	}

	//1.) http://www.geocaching.com/my/
	//<a href="http://www.geocaching.com/my/" class="CommonUsername" title="arbor95" target="_self">arbor95</a>	
	String userBlock = extractor.set(page, "http://www.geocaching.com/my/", "</a>", 0, true).findNext();
	String loggedInAs = extractValue.set(userBlock, "title=\"", "\"", 0, true).findNext();
	Preferences.itself().log("[checkGCSettings]:loggedInAs= " + loggedInAs, null);
	if (loggedInAs.length() == 0)
	    return 6;

	//2.) ctl00$ContentBody$uxLanguagePreference
	//<select name="ctl00$ContentBody$uxLanguagePreference" id="ctl00_ContentBody_uxLanguagePreference" class="Select">
	//<option selected="selected" value="en-US">English</option>
	//oder
	//<option selected=\"selected\" value=\"de-DE">Deutsch</option>	
	String languageBlock = extractor.findNext("ctl00$ContentBody$uxLanguagePreference", "</select>");
	String oldLanguage = extractValue.set(languageBlock, "<option selected=\"selected\" value=\"", "\">", 0, true).findNext();
	Preferences.itself().log("[checkGCSettings]:Language= " + oldLanguage, null);

	//3.) ctl00$ContentBody$uxTimeZone

	//4.) ctl00_ContentBody_uxDisplayUnits
	//<table id="ctl00_ContentBody_uxDisplayUnits" .. </table>
	//<input id="ctl00_ContentBody_uxDisplayUnits_1" type="radio" name="ctl00$ContentBody$uxDisplayUnits" value="1" checked="checked" /><label for="ctl00_ContentBody_uxDisplayUnits_1">Metric</label>
	String distanceUnitBlock = extractor.findNext("ctl00_ContentBody_uxDisplayUnits", "</table>");
	String distanceUnit = extractValue.set(extractValue.set(distanceUnitBlock, "\"checked\"", "</td>", 0, true).findNext(), "\">", "</label>", 0, true).findNext();
	Preferences.itself().log("[checkGCSettings]:Units= " + distanceUnit, null);
	if (!distanceUnit.equalsIgnoreCase(Preferences.itself().metricSystem == Metrics.METRIC ? "Metric" : "Imperial")) {
	    return 3;
	}

	//5.) ctl00$ContentBody$uxDateTimeFormat
	//<select name="ctl00$ContentBody$uxDateTimeFormat" id="ctl00_ContentBody_uxDateTimeFormat" class="Select">
	//<option selected="selected" value="yyyy-MM-dd"> 2013-12-04</option>
	String GCDateFormatBlock = extractor.findNext("ctl00$ContentBody$uxDateTimeFormat", "</select>");
	String GCDateFormat = extractValue.set(GCDateFormatBlock, "selected\" value=\"", "\">", 0, true).findNext();
	Preferences.itself().log("[checkGCSettings]:GCDateFormat= " + GCDateFormat, null);
	DateFormat.GCDateFormat = GCDateFormat;

	//6.)ctl00$ContentBody$uxInstantMessengerProvider

	//7.) ctl00$ContentBody$ddlGPXVersion

	if (oldLanguage.equals("en-US")) {
	    Preferences.itself().changedGCLanguageToEnglish = false;
	    return 0;
	} else {
	    Preferences.itself().oldGCLanguage = oldLanguage;
	    if (setGCLanguage("en-US")) {
		Preferences.itself().changedGCLanguageToEnglish = true;
		return 0;
	    } else {
		Preferences.itself().changedGCLanguageToEnglish = false;
		return 1;
	    }
	}

	/*
	// other place to check/set selected language
	String languageBlock = ext.set(page, "\"selected-language\"", "</div>", 0, true).findNext();
	String oldLanguage = ext.set(languageBlock, "<a href=\"#\">", "&#9660;</a>", 0, true).findNext();
	if (oldLanguage.equals("English")) {
	    Preferences.itself().switchGCLanguageToEnglish = false;
	    return 0;
	}
	*/

    }

    public static boolean setGCLanguage(String toLanguage) {
	// language now goes into gc account Display Preferences
	// (is permanent, must be reset)
	// must do post (get no longer works)

	String languages[] = { "en-US", "de-DE", "fr-FR", "pt-PT", "cs-CZ", //		
		"da-DK", "sv-SE", "es-ES", "et-EE", "it-IT", //
		"el-GR", "lv-LV", "nl-NL", "ca-ES", "pl-PL", //
		"et-EE", "nb-NO", "ko-KR", "hu-HU", "ro-RO", //
		"ja-JP", //
	};
	String languageCode = "00"; // defaults to "en-US"
	for (int i = 0; i < languages.length; i++) {
	    if (toLanguage.equals(languages[i])) {
		languageCode = MyLocale.formatLong(i, "00");
		break;
	    }
	}
	String page;
	String url = "http://www.geocaching.com/my/recentlyviewedcaches.aspx";
	try {
	    page = UrlFetcher.fetch(url);
	} catch (final Exception ex) {
	    Preferences.itself().log("[recentlyviewedcaches]:Exception", ex, true);
	    return false;
	}
	String viewstate = extractor.set(page, "id=\"__VIEWSTATE\" value=\"", "\" />", 0, true).findNext();
	String viewstate1 = extractor.findNext("id=\"__VIEWSTATE1\" value=\"");
	final String postData = "__EVENTTARGET=ctl00%24uxLocaleListTop%24uxLocaleList%24ctl" + languageCode + "%24uxLocaleItem" //
		+ "&" + "__EVENTARGUMENT="//
		+ "&" + "__VIEWSTATEFIELDCOUNT=2" //
		+ "&" + "__VIEWSTATE=" + UrlFetcher.encodeURL(viewstate, false) //
		+ "&" + "__VIEWSTATE1=" + UrlFetcher.encodeURL(viewstate1, false) //
		+ "&" + "ctl00%24ContentBody%24wp=" //
	;
	try {
	    UrlFetcher.setpostData(postData);
	    page = UrlFetcher.fetch(url);
	} catch (final Exception ex) {
	    Preferences.itself().log("[setGCLanguage] Exception", ex);
	    return false;
	}
	if (stillLoggedIn(page)) {
	    // check success
	    return true;
	} else {
	    return false;
	}
    }

    /* 
    private boolean setGCLanguage(String toLanguage) {
    // switch to English with
    // url = "http://www.geocaching.com/account/ManagePreferences.aspx";
    // todo to work successfull with this perhaps set all values (did not test).
    String viewstate = ext.set(page, "id=\"__VIEWSTATE\" value=\"", "\" />", 0, true).findNext();
    String viewstate1 = ext.findNext("id=\"__VIEWSTATE1\" value=\"");
    String setLanguageEN = "ctl00$ContentBody$uxLanguagePreference=en-US";
    String commit = "ctl00$ContentBody$uxSave=Save Changes";

    final String postData = "__EVENTTARGET=" //
    	+ "&" + "__EVENTARGUMENT="//
    	+ "&" + "__VIEWSTATEFIELDCOUNT=2" //
    	+ "&" + "__VIEWSTATE=" + UrlFetcher.encodeURL(viewstate, false) //
    	+ "&" + "__VIEWSTATE1=" + UrlFetcher.encodeURL(viewstate1, false) //
    	+ "&" + UrlFetcher.encodeURL(setLanguageEN, false) //
    	+ "&" + UrlFetcher.encodeURL(commit, true) //
    ;
    try {
        UrlFetcher.setpostData(postData);
        page = UrlFetcher.fetch(url);
        Preferences.itself().log(page, null);
    } catch (final Exception ex) {
        Preferences.itself().log("[checkGCSettings] Error at post checkGCSettings", ex);
        return 1;
    }	
    }
    */

    private boolean gcLogin() {

	// Get password 
	String passwort = Preferences.itself().password;
	InfoBox localInfB = new InfoBox(MyLocale.getMsg(5506, "Password"), MyLocale.getMsg(5505, "Enter Password"), InfoBox.INPUT);
	localInfB.setInputPassword(passwort);
	int code = FormBase.IDOK;
	if (passwort.equals("")) {
	    code = localInfB.execute();
	    passwort = localInfB.getInput();
	}
	localInfB.close(0);
	if (code != FormBase.IDOK)
	    return false;

	String page;
	UrlFetcher.clearCookies();
	try {
	    page = UrlFetcher.fetch(loginPageUrl); // 
	} catch (final Exception ex) {
	    Preferences.itself().log("[gcLogin]:Exception gc.com login page", ex, true);
	    return false;
	}
	String viewstate = extractor.set(page, "id=\"__VIEWSTATE\" value=\"", "\" />", 0, true).findNext();
	final String postData = "__EVENTTARGET=" //
		+ "&" + "__EVENTARGUMENT="//
		+ "&" + "__VIEWSTATEFIELDCOUNT=1" //
		+ "&" + "__VIEWSTATE=" + UrlFetcher.encodeURL(viewstate, false) //
		+ "&" + "ctl00%24ContentBody%24tbUsername=" + encodeUTF8URL(Utils.encodeJavaUtf8String(Preferences.itself().myAlias)) //
		+ "&" + "ctl00%24ContentBody%24tbPassword=" + encodeUTF8URL(Utils.encodeJavaUtf8String(passwort)) //
		+ "&" + "ctl00%24ContentBody%24cbRememberMe=" + "true" //
		+ "&" + "ctl00%24ContentBody%24btnSignIn=" + "Login" //
	;
	try {
	    UrlFetcher.setpostData(postData);
	    page = UrlFetcher.fetch(loginPageUrl);
	} catch (final Exception ex) {
	    Preferences.itself().log("[gcLogin] Exception", ex);
	    return false;
	}
	return true;
    }

    private static boolean stillLoggedIn(String page) {
	if (!(page.indexOf("ctl00_hlSignOut") > -1)) {
	    if (!(page.indexOf("ctl00_ContentLogin_uxLoginStatus_uxLoginURL") > -1)) {
		Preferences.itself().log(page);
		return false;
	    }
	}
	return true;
    }

    private void getFirstListPage(int distance) {
	String url = makeUrl(distance);

	try {
	    htmlListPage = UrlFetcher.fetch(url);
	    Preferences.itself().log("[getFirstListPage] Got first page " + url);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getFirstListPage] Error fetching first list page " + url, ex, true);
	    Vm.showWait(false);
	    infB.close(0);
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5503, "Error fetching first list page.")).wait(FormBase.OKB);
	    return;
	}
    }

    /**
     * in: distance whatPage out: htmlListPage
     */
    private boolean getAListPage(int distance, String whatPage) {
	boolean ret = true;
	String url = makeUrl(distance);

	final Regex rexViewstate = new Regex("id=\"__VIEWSTATE\" value=\"(.*?)\" />");
	String viewstate;
	rexViewstate.search(htmlListPage);
	if (rexViewstate.didMatch()) {
	    viewstate = rexViewstate.stringMatched(1);
	} else {
	    viewstate = "";
	    Preferences.itself().log("[SpiderGC.java:getAListPage] check rexViewstate!", null);
	}

	final Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
	String viewstate1;
	rexViewstate1.search(htmlListPage);
	if (rexViewstate1.didMatch()) {
	    viewstate1 = rexViewstate1.stringMatched(1);
	} else {
	    viewstate1 = "";
	    Preferences.itself().log("[SpiderGC.java:getAListPage] check rexViewstate1!", null);
	}

	final String postData = "__EVENTTARGET=" + URL.encodeURL(whatPage, false) //
		+ "&" + "__EVENTARGUMENT=" //
		+ "&" + "__VIEWSTATEFIELDCOUNT=2" //
		+ "&" + "__VIEWSTATE=" + URL.encodeURL(viewstate, false) //
		+ "&" + "__VIEWSTATE1=" + URL.encodeURL(viewstate1, false);
	try {
	    UrlFetcher.setpostData(postData);
	    htmlListPage = UrlFetcher.fetch(url);
	    Preferences.itself().log("[getAListPage] " + whatPage);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getAListPage] Error at " + whatPage, ex);
	    ret = false;
	}
	return ret;
    }

    private String makeUrl(int distance) {
	String url;
	if (spiderAllFinds) {
	    url = urlSeek //
		    + queryUserFinds + encodeUTF8URL(Utils.encodeJavaUtf8String(Preferences.itself().myAlias));
	} else {
	    url = urlSeek //
		    + queryLat + origin.getLatDeg(TransformCoordinates.DD) //
		    + queryLon + origin.getLonDeg(TransformCoordinates.DD) //
		    + queryDistance + Integer.toString(distance); //
	    if (doNotgetFound)
		url = url + queryDoNotGetFound;
	}
	url = url + cacheTypeRestriction;
	return url;
    }

    /**
     * check if new Update exists
     * 
     * @param ch
     *            CacheHolder
     * @param CacheDescription
     *            A previously fetched Page
     * @return true if new Update exists else false
     */
    private boolean updateExists(CacheHolder ch, String CacheDescription) {
	boolean ret = false;
	boolean save = false;
	boolean is_archived_GC = false;
	boolean is_found_GC = false;
	if (spiderAllFinds) {
	    if (!ch.is_found()) {
		ch.setFound(true);
		save = true;
		numFoundUpdates += 1;
		ret = true;
	    }
	    is_archived_GC = CacheDescription.indexOf(propArchived) != -1;
	    if (is_archived_GC != ch.is_archived()) {
		ch.setArchived(is_archived_GC);
		save = true;
		numArchivedUpdates += 1;
		ret = true;
	    }
	} else if (!doNotgetFound) { // there could be a found or own ...
	    is_found_GC = CacheDescription.indexOf(propFound) != -1;
	    if (is_found_GC != ch.is_found()) {
		ch.setFound(is_found_GC);
		save = true;
		ret = true;
	    }
	}

	if (ch.is_found()) {
	    // check for missing ownLogID (and logtext)
	    if (ch.getCacheDetails(false).OwnLogId.equals(""))
		ret = true;
	}

	final boolean is_available_GC = !is_archived_GC && CacheDescription.indexOf(propAvailable) == -1;
	if (is_available_GC != ch.is_available()) {
	    ch.setAvailable(is_available_GC);
	    save = true;
	    numAvailableUpdates += 1;
	    ret = true;
	}
	if (typeChanged(ch, CacheDescription)) {
	    save = true;
	    ret = true;
	}
	if (Preferences.itself().checkDTS) {
	    final String dts[] = mString.split(getDTS(CacheDescription), '/');
	    if (dts.length == 3) {
		if (difficultyChanged(ch, CacheTerrDiff.v1Converter(dts[0]))) {
		    save = true;
		    ret = true;
		    Preferences.itself().log("difficultyChanged");
		}
		if (terrainChanged(ch, CacheTerrDiff.v1Converter(dts[1]))) {
		    save = true;
		    ret = true;
		    Preferences.itself().log("terrainChanged");
		}
		if (sizeChanged(ch, CacheSize.gcGpxString2Cw(dts[2]))) {
		    save = true;
		    ret = true;
		    Preferences.itself().log("sizeChanged");
		}
	    } else {
		try {
		    Preferences.itself().log("[SpiderGC.java:updateExists]check DTS calculation (DTSRex)! \n" + CacheDescription, null);
		} catch (Exception e) {
		}
	    }
	}
	if (newFoundExists(ch, CacheDescription)) {
	    numLogUpdates++;
	    ret = true;
	}
	if (!ret) {
	    ret = TBchanged(ch, CacheDescription);
	}
	if (save)
	    ch.save();
	return ret;
    }

    /**
     * Get num found
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return numFound
     */
    private int getNumFound(String doc) {
	RexNumFinds.search(doc);
	if (RexNumFinds.didMatch()) {
	    return Common.parseInt(RexNumFinds.stringMatched(1));
	} else {
	    Preferences.itself().log("[SpiderGC.java:getNumFound]check RexNumFinds!", null);
	    return 0;
	}
    }

    private int getFoundInDB() {
	CacheHolder ch;
	int counter = 0;
	for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
	    ch = MainForm.profile.cacheDB.get(i);
	    if (ch.is_found()) {
		if (ch.getWayPoint().startsWith("GC"))
		    counter++;
	    }
	}
	return counter;
    }

    private double getDistance(String doc) {
	// #<span class="small NoWrap"><img src="/images/icons/compass/SW.gif" alt="SW" title="SW" />SW<br />0.31km</span>
	// DistDirRex = compass/(.*?)\.gif(.*?)<br />(.*?)(?:km|mi|ft)
	if (spiderAllFinds)
	    return 0.0;
	// <span class="small NoWrap"><br />Here</span>
	if (doc.indexOf(">Here<") > 0)
	    return 0.0;
	String stmp;
	DistDirRex.search(doc);
	if (!DistDirRex.didMatch()) {
	    Preferences.itself().log("[SpiderGC.java:getDistance]check DistDirRex!", null);
	    // Abbruch
	    return -1.0;
	}
	stmp = DistDirRex.stringMatched(3);
	return Common.parseDouble(stmp);
	// stmp = DistDirRex.stringMatched(1);
	// = "N"(0),"NE"(45),"E"(90),"SE"(135),"S"(180),"SW"(225),"W"(270),"NW"(315)
    }

    /**
     * Get the waypoint name
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Name of waypoint to add to list
     */
    private String getWP(String doc) throws Exception {
	//#<span class="small">
	//#                            by OlSiTiNi
	//#                            |
	//#                            GC34CQJ
	//#                            |
	//#                            Hessen, Germany</span>
	//#
	//waypointRex        = \\|\\s+GC(.*?)\\s+\\|
	RexPropWaypoint.search(doc);
	if (!RexPropWaypoint.didMatch()) {
	    Preferences.itself().log("[SpiderGC.java:getWP]check waypointRex!", null);
	    return "???";
	}
	String stmp = RexPropWaypoint.stringMatched(1);
	return "GC" + stmp;
    }

    /**
     * check for Premium Member Cache
     */
    private boolean doPMCache(String waypoint, String toCheck) {
	if (Preferences.itself().isPremium)
	    return true;
	if (toCheck.indexOf(propPM) <= 0) {
	    return true;
	} else {
	    numPrivate = numPrivate + 1;
	    // if (spiderAllFinds) {
	    Preferences.itself().log(waypoint + " is private.", null);
	    // }
	    /*
	    CacheHolder ch = MainForm.profile.cacheDB.get(waypoint);
	    if (ch == null) {
	    ch = new CacheHolder(waypoint);
	    MainForm.profile.cacheDB.add(ch);
	    ch.save();
	    }
	    */
	    return false;
	}
    }

    /*
     * check for changed Cachetype
     */
    private boolean typeChanged(CacheHolder ch, String toCheck) {
	RexPropType.search(toCheck);
	if (RexPropType.didMatch()) {
	    String stmp = RexPropType.stringMatched(1);
	    if (Common.parseInt(stmp) == 0) {
		if (stmp.equalsIgnoreCase("EarthCache"))
		    stmp = "137";
	    }
	    if (ch.getType() == CacheType.gcSpider2CwType(stmp))
		return false;
	    else {
		ch.setType(CacheType.gcSpider2CwType(stmp));
		return true;
	    }
	}
	Preferences.itself().log("[SpiderGC.java:typeChanged]check TypeRex!", null);
	return false;
    }

    private String getDTS(String toCheck) {
	// result 3 values separated by /
	String res = "";
	DTSRex.search(toCheck);
	if (DTSRex.didMatch()) {
	    res = DTSRex.stringMatched(1) + "/" + DTSRex.stringMatched(2) + "/" + DTSRex.stringMatched(5);
	}
	return res;
    }

    /*
     * check for changed Difficulty
     */
    private boolean difficultyChanged(CacheHolder ch, byte toCheck) {
	if (ch.getHard() == toCheck)
	    return false;
	else {
	    ch.setHard(toCheck);
	    return true;
	}
    }

    /*
     * check for changed Terrain
     */
    private boolean terrainChanged(CacheHolder ch, byte toCheck) {
	if (ch.getTerrain() == toCheck)
	    return false;
	else {
	    ch.setTerrain(toCheck);
	    return true;
	}
    }

    /*
     * check for changed CacheSize
     */
    private boolean sizeChanged(CacheHolder ch, byte toCheck) {
	if (ch.getCacheSize() == toCheck)
	    return false;
	else {
	    ch.setCacheSize(toCheck);
	    return true;
	}
    }

    /*
     * @param CacheHolder ch
     * 
     * @param String cacheDescGC
     * 
     * @return boolean newLogExists
     */
    private boolean newFoundExists(CacheHolder ch, String cacheDescription) {
	if (!Preferences.itself().checkLog)
	    return false;
	final Time lastUpdateCW = new Time();
	String stmp = ch.getLastSync();
	if (stmp.length() > 0) {
	    try {
		lastUpdateCW.parse(stmp, "yyyyMMddHHmmss");
	    } catch (Exception e) {
		// lastUpdateCW = now
	    }
	}

	/*
	 * don't use real log --> don't read CacheDetails final Time lastLogCW = new Time(); final Log lastLog = ch.getCacheDetails(true).CacheLogs.getLog(0); if (lastLog == null) return true; final String slastLogCW = lastLog.getDate(); if
	 * (slastLogCW.equals("") || slastLogCW.equals("1900-00-00")) return true; // or check cacheDescGC also no log? lastLogCW.parse(slastLogCW, "yyyy-MM-dd");
	 */

	logDateRex.search(cacheDescription);
	if (logDateRex.didMatch()) {
	    stmp = logDateRex.stringMatched(1);
	} else {
	    Preferences.itself().log("[SpiderGC.java:newFoundExists]check logDateRex!", null);
	    return false;
	}
	final Time lastLogGC = DateFormat.toDate(stmp);
	// String timecheck = DateFormat.toYYMMDD(lastLogGC);
	final boolean ret = lastUpdateCW.compareTo(lastLogGC) < 0;
	return ret;
    }

    private boolean TBchanged(CacheHolder ch, String cacheDescription) {
	// simplified Version: only presence is checked
	if (Preferences.itself().downloadTBs && Preferences.itself().checkTBs) {
	    final boolean hasTB = cacheDescription.indexOf("data-tbcount") > -1;
	    return ch.has_bugs() != (hasTB);
	}
	return false;
    }

    public int getWayPointPage(String wayPoint) {
	int ret = SPIDER_OK; // initialize value;
	try {
	    wayPointPage = UrlFetcher.fetch(wayPointUrl + wayPoint);
	    Preferences.itself().log("Fetched: " + wayPoint);
	} catch (final Exception ex) {
	    Preferences.itself().log("Could not fetch " + wayPoint, ex);
	    ret = SPIDER_ERROR;
	}
	return ret;
    }

    /**
     * Read a complete wayPointPage from geocaching.com.<br>
     * This is used both when updating already existing caches (via spiderSingle) and when spidering around a centre.<br>
     * It is also used when reading a GPX file and fetching the images.<br>
     * 
     * This is the workhorse function of the spider.
     * 
     * @param boolean fetchImages True if the pictures are to be fetched
     * @param boolean fetchTBs True if the TBs are to be fetched
     * @return -1=SPIDER_CANCEL if the infoBox was closed (cancel spidering), 0=SPIDER_ERROR if there was an error (continue with next cache), 1=SPIDER_OK if everything ok
     */
    private int getCacheByWaypointName(CacheHolder ch, boolean fetchImages, boolean fetchTBs) {
	int ret = SPIDER_OK;
	ch.setIncomplete(true);
	CacheHolderDetail chD = ch.getCacheDetails(false);
	while (true) { // retry even if failure
	    // Preferences.itself().log(""); // new line for more overview
	    int spiderTrys = 0;
	    final int MAX_SPIDER_TRYS = 3;
	    while (spiderTrys++ < MAX_SPIDER_TRYS) {
		ret = getWayPointPage(ch.getWayPoint());
		if (ret == SPIDER_OK) {
		    if (infB.isClosed())
			ret = SPIDER_CANCEL;
		    else if (wayPointPage.indexOf(premiumGeocache) > -1) {
			// old ch.setCacheStatus("PM");
			// Premium cache spidered by non premium member
			Preferences.itself().log("Ignoring premium member cache: " + ch.getWayPoint(), null);
			spiderTrys = MAX_SPIDER_TRYS;
			ret = SPIDER_IGNORE_PREMIUM;
		    } else if (wayPointPage.indexOf(unpublishedGeocache) > -1) {
			// old ch.setCacheStatus("unpublished Geocache");
			Preferences.itself().log("unpublished Geocache: " + ch.getWayPoint(), null);
			spiderTrys = MAX_SPIDER_TRYS;
			ret = SPIDER_IGNORE;
		    }
		}
		if (ret == SPIDER_OK) {
		    try {
			ch.setHTML(true);
			ch.addiWpts.clear();
			chD.images.clear();

			ch.setAvailable(!(wayPointPage.indexOf(unavailableGeocache) > -1));
			ch.setArchived(wayPointPage.indexOf(archivedGeocache) > -1);

			// Logs
			getLogs(ch, wayPointPage.indexOf(foundByMe) > -1); // or get finds

			// order of occurrence in wayPointPage (perhaps replace RegEx with Extractor)
			ch.setHard(getDiff());
			ch.setTerrain(getTerr());
			ch.setType(getType());
			ch.setCacheName(getName());
			String owner = getOwner();
			ch.setCacheOwner(owner);
			if (owner.equalsIgnoreCase(Preferences.itself().myAlias) || owner.equalsIgnoreCase(Preferences.itself().myAlias2))
			    ch.setOwned(true);
			ch.setDateHidden(getDateHidden());
			ch.setCacheSize(getSize());
			final String latLon = getLatLon();
			if (latLon.equals("???")) {
			    Preferences.itself().log("[SpiderGC.java:getLatLon]check latLonRex!", null);
			    if (spiderTrys == MAX_SPIDER_TRYS)
				Preferences.itself().log(">>>> Failed to spider Cache. Retry.", null);
			    ret = SPIDER_ERROR;
			    continue;
			}
			ch.setPos(new CWPoint(latLon));
			final String location = getLocation();
			if (location.length() != 0) {
			    final int countryStart = location.indexOf(",");
			    if (countryStart > -1) {
				chD.Country = SafeXML.cleanback(location.substring(countryStart + 1).trim());
				chD.State = SafeXML.cleanback(location.substring(0, countryStart).trim());
			    } else {
				chD.Country = location.trim();
				chD.State = "";
			    }
			} else {
			    chD.Country = "";
			    chD.State = "";
			}
			chD.setLongDescription(getDescription());
			chD.setHints(getHints());
			if (fetchTBs)
			    getBugs(chD);
			ch.setHas_bugs(chD.Travelbugs.size() > 0);
			if (fetchImages) {
			    this.getImages(chD);
			}
			getAddWaypoints(wayPointPage, ch.getWayPoint(), ch.is_found());
			getAttributes(chD);
			ch.setLastSync((new Time()).format("yyyyMMddHHmmss"));
			ch.setIncomplete(false);
			Preferences.itself().log("ready " + ch.getWayPoint() + " : " + ch.getLastSync());
			break;
		    } catch (final Exception ex) {
			Preferences.itself().log("[getCacheByWaypointName: ]Error reading cache: " + ch.getWayPoint(), ex);
		    }
		}
	    } // spiderTrys

	    if ((spiderTrys >= MAX_SPIDER_TRYS) && (ret == SPIDER_OK)) {
		Preferences.itself().log(">>> Failed to spider cache. Number of retrys exhausted.", null);
		int decision = new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5515, "Failed to load cache.%0aPleas check your internet connection.%0aRetry?")).wait(FormBase.DEFOKB | FormBase.NOB | FormBase.CANCELB);
		if (decision == FormBase.IDOK) {
		    continue; // retry even if failure
		} else if (decision == FormBase.IDNO) {
		    ret = SPIDER_ERROR;
		} else {
		    ret = SPIDER_CANCEL;
		}
	    }

	    break;

	}// while(true)

	return ret;
    } // getCacheByWaypointName

    // get from wayPointPage
    private byte getDiff() {
	difficultyRex.search(wayPointPage);
	if (difficultyRex.didMatch())
	    return CacheTerrDiff.v1Converter(difficultyRex.stringMatched(1));
	else {
	    Preferences.itself().log("[SpiderGC.java:getDiff]check difficultyRex!", null);
	    return CacheTerrDiff.v1Converter("-1");
	}
    }

    private byte getTerr() {
	terrainRex.search(wayPointPage);
	if (terrainRex.didMatch())
	    return CacheTerrDiff.v1Converter(terrainRex.stringMatched(1));
	else {
	    Preferences.itself().log("[SpiderGC.java:getTerr]check terrainRex!", null);
	    return CacheTerrDiff.v1Converter("-1");
	}
    }

    private byte getType() {
	cacheTypeRex.search(wayPointPage);
	if (cacheTypeRex.didMatch())
	    return CacheType.gcSpider2CwType(cacheTypeRex.stringMatched(1));
	else {
	    Preferences.itself().log("[SpiderGC.java:getType]check cacheTypeRex!", null);
	    return 0;
	}
    }

    private String getName() {
	cacheNameRex.search(wayPointPage);
	if (cacheNameRex.didMatch()) {
	    return SafeXML.cleanback(cacheNameRex.stringMatched(1));
	} else {
	    Preferences.itself().log("[SpiderGC.java:getName]check cacheNameRex!", null);
	    return "???";
	}
    }

    private String getOwner() {
	cacheOwnerRex.search(wayPointPage);
	if (cacheOwnerRex.didMatch()) {
	    return SafeXML.cleanback(cacheOwnerRex.stringMatched(1)).trim();
	} else {
	    Preferences.itself().log("[SpiderGC.java:getOwner]check cacheOwnerRex!", null);
	    return "???";
	}
    }

    private String getDateHidden() {
	dateHiddenRex.search(wayPointPage);
	if (dateHiddenRex.didMatch()) {
	    return DateFormat.toYYMMDD(dateHiddenRex.stringMatched(1));
	} else {
	    Preferences.itself().log("[SpiderGC.java:getDateHidden]check dateHiddenRex!", null);
	    return "???";
	}
    }

    private byte getSize() {
	sizeRex.search(wayPointPage);
	if (sizeRex.didMatch())
	    return CacheSize.gcSpiderString2Cw(sizeRex.stringMatched(1));
	else {
	    Preferences.itself().log("[SpiderGC.java:getSize]check sizeRex!", null);
	    return CacheSize.gcSpiderString2Cw("None");
	}
    }

    private String getLatLon() {
	latLonRex.search(wayPointPage);
	if (latLonRex.didMatch()) {
	    return latLonRex.stringMatched(1);
	} else {
	    return "???";
	}
    }

    private String getLocation() {
	cacheLocationRex.search(wayPointPage);
	if (cacheLocationRex.didMatch()) {
	    return cacheLocationRex.stringMatched(2);
	} else {
	    Preferences.itself().log("[SpiderGC.java:getLocation]check cacheLocationRex!", null);
	    return "";
	}
    }

    boolean shortDescRex_not_yet_found = true;

    public String getDescription() {
	String res = "";
	shortDescRex.search(wayPointPage);
	if (!shortDescRex.didMatch()) {
	    if (shortDescRex_not_yet_found)
		Preferences.itself().log("[SpiderGC.java:getLongDesc]no shortDesc or check shortDescRex!", null);
	    // + Preferences.NEWLINE + doc);
	} else {
	    res = shortDescRex.stringMatched(1);
	    shortDescRex_not_yet_found = false;
	}
	res += "<br>";
	longDescRex.search(wayPointPage);
	if (!longDescRex.didMatch()) {
	    Preferences.itself().log("[SpiderGC.java:getLongDesc]check longDescRex!", null);
	} else {
	    res += longDescRex.stringMatched(1);
	}
	final int spanEnd = res.lastIndexOf("</span>");
	if (spanEnd >= 0) {
	    res = res.substring(0, spanEnd);
	}
	// since internal viewer doesn't show html-entities that are now in cacheDescription
	return SafeXML.cleanback(res);
    }

    private String getHints() {
	hintsRex.search(wayPointPage);
	if (hintsRex.didMatch()) {
	    return hintsRex.stringMatched(1);
	} else {
	    Preferences.itself().log("[SpiderGC.java:getHints]check hintsRex!", null);
	    return "";
	}
    }

    /**
     * Get the logs
     */
    private void getLogs(CacheHolder ch, boolean fetchAllLogs) throws Exception {
	final CacheHolderDetail chD = ch.getCacheDetails(false);
	final LogList reslts = chD.CacheLogs;
	reslts.clear();

	RexUserToken.search(wayPointPage);
	if (!RexUserToken.didMatch()) {
	    Preferences.itself().log("[SpiderGC.java:getLogs]check RexUserToken!", null);
	    return;
	}
	final String userToken = RexUserToken.stringMatched(1);

	int idx = 0;
	int nLogs = 0;
	boolean foundown = false;
	boolean fertig = false;
	int num = 100;

	if (!fetchAllLogs) {
	    if (Preferences.itself().maxLogsToSpider < 100)
		num = Preferences.itself().maxLogsToSpider + 1;
	}

	do {
	    idx++;
	    String url = "http://www.geocaching.com/seek/geocache.logbook?tkn=" + userToken + "&idx=" + idx + "&num=" + num + "&decrypt=false";
	    UrlFetcher.setRequestorProperty("Content-Type", "application/json; charset=UTF-8");
	    JSONObject resp = null;
	    String fetchResult = "";
	    try {
		fetchResult = UrlFetcher.fetch(url);
		char[] fr = fetchResult.toCharArray();
		for (int i = 0; i < fr.length; i++) {
		    if (fr[i] == 0) {
			fr[i] = ' ';
		    }
		}
		fetchResult = String.valueOf(fr);
		resp = new JSONObject(fetchResult);
	    } catch (Exception e) {
		if (fetchResult == null)
		    fetchResult = "";
		Preferences.itself().log("Error getting Logs. \r\n" + fetchResult, e);
		return;
	    }
	    if (!resp.getString("status").equals("success")) {
		Preferences.itself().log("status is " + resp.getString("status"));
	    }
	    final JSONArray data = resp.getJSONArray("data");
	    fertig = data.length() < num;
	    for (int index = 0; index < data.length(); index++) {
		nLogs++;
		final JSONObject entry = data.getJSONObject(index);

		final String icon = entry.getString("LogTypeImage");
		final String name = entry.getString("UserName");
		String logText = SafeXML.cleanback(entry.getString("LogText"));
		logText = STRreplace.replace(logText, "\u000b", " ");
		logText = STRreplace.replace(logText, "<br/>", "<br>");
		logText = correctSmilies(logText);
		final String d = DateFormat.toYYMMDD(entry.getString("Visited"));

		// if this log says this Cache is found by me
		if ((icon.equals(icon_smile) || icon.equals(icon_camera) || icon.equals(icon_attended)) && (name.equalsIgnoreCase(Preferences.itself().myAlias) || (name.equalsIgnoreCase(Preferences.itself().myAlias2)))) {
		    ch.setFound(true);
		    ch.setCacheStatus(d);
		    // final String logId = entry.getString("LogID");
		    chD.OwnLogId = entry.getString("LogID");
		    chD.OwnLog = new Log(icon, d, name, logText);
		    foundown = true;
		    reslts.add(new Log(icon, d, name, logText));
		}
		if (nLogs <= Preferences.itself().maxLogsToSpider) {
		    reslts.add(new Log(icon, d, name, logText));
		} else {
		    if (foundown || !fetchAllLogs) {
			fertig = true;
			break;
		    }
		}
	    }
	} while (!fertig);

	if (nLogs > Preferences.itself().maxLogsToSpider) {
	    // there are more logs
	    reslts.add(Log.maxLog());
	}
	// Bei Update wird es doppelt berechnet
	ch.setNoFindLogs(reslts.countNotFoundLogs());
    }

    /**
     * This methods cleans up the path for inlined smilies in logtexts.
     * 
     * @param logText
     * @return
     */
    private String correctSmilies(String logText) {
	int indexOf = logText.indexOf(iconsRelativePath);
	while (indexOf >= 0) {
	    final String prefix = logText.substring(0, indexOf);
	    final String postFix = logText.substring(indexOf + iconsRelativePath.length());
	    logText = prefix + "<img src=\"" + postFix;
	    indexOf = logText.indexOf(iconsRelativePath);
	}
	return logText;
    }

    /**
     * Read the travelbug names from a previously fetched Cache page and then read the travelbug purpose for each travelbug
     * 
     * @param doc
     *            The previously fetched wayPointPage
     * @return A HTML formatted string with bug names and there purpose
     */
    public void getBugs(CacheHolderDetail chD) throws Exception {
	chD.Travelbugs.clear();
	if (wayPointPage.indexOf("ctl00_ContentBody_uxTravelBugList_uxNoTrackableItemsLabel") >= 0) {
	    return; // there are no trackables
	}
	final Extractor exBlock = new Extractor(wayPointPage, p.getProp("blockExStart"), p.getProp("blockExEnd"), 0, Extractor.EXCLUDESTARTEND);
	final Extractor exBug = new Extractor("", p.getProp("bugExStart"), p.getProp("bugExEnd"), 0, Extractor.EXCLUDESTARTEND);
	final Extractor exBugName = new Extractor("", "", "", 0, Extractor.EXCLUDESTARTEND);
	final String bugBlock;
	bugBlock = exBlock.findNext();
	int blockLength = bugBlock.length();
	if (blockLength > 0) {
	    String link, bug, linkPlusBug, bugDetails;
	    final String oldInfoBox = infB.getInfo();
	    boolean exBugWrong = true;
	    exBug.set(bugBlock);
	    while ((linkPlusBug = exBug.findNext()).length() > 0) {
		exBugWrong = false;
		if (infB.isClosed())
		    break;
		final int idx = linkPlusBug.indexOf(p.getProp("bugLinkEnd"));
		if (idx < 0) {
		    Preferences.itself().log("[SpiderGC.java:getBugs]check TBs bugLinkEnd!", null);
		    break; // No link/bug pair found
		}
		link = linkPlusBug.substring(0, idx);
		exBugName.set(linkPlusBug, p.getProp("bugNameExStart"), p.getProp("bugNameExEnd"), idx, Extractor.EXCLUDESTARTEND);
		if ((bug = exBugName.findNext()).length() > 0) {
		    // Found a bug, get its mission
		    try {
			infB.setInfo(oldInfoBox + MyLocale.getMsg(5514, "\nGetting bug: ") + SafeXML.cleanback(bug));
			bugDetails = UrlFetcher.fetch(link);
			exBugName.set(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND); // reusing
			// exBugName
			chD.Travelbugs.add(new Travelbug(link.substring(1 + link.indexOf("=")), bug, exBugName.findNext()));
		    } catch (final Exception ex) {
			Preferences.itself().log("[SpiderGC.java:getBugs] Could not fetch buginfo from " + link, ex);
		    }
		}
	    }
	    infB.setInfo(oldInfoBox);
	    if (exBugWrong) {
		if (blockLength > 200)
		    Preferences.itself().log("[SpiderGC.java:getBugs]check TBs bugExStart / bugExEnd! blockLength = " + blockLength + " for " + chD.URL, null);
	    }
	} else {
	    Preferences.itself().log("[SpiderGC.java:getBugs]check TBs blockExStart / blockExEnd! ", null);
	}
    }

    /**
     * prerequisites:
     * chD.LongDescription must be filled
     * this.wayPointPage must be filled to extract spoilerSection
     * 
     * @param chD
     */
    public void getImages(CacheHolderDetail chD) {
	this.getDescriptionImages(chD);
	this.getSpoilerImages(chD);
	this.cleanupOldImages(chD);
    }

    /**
     * In the long Description
     * the img/src - tags and the a/href - tags
     * 
     * @param chD
     */
    private void getDescriptionImages(CacheHolderDetail chD) {
	// ==================================
	// checking img - tags of description
	// ==================================
	String longDesc = STRreplace.replace(chD.LongDescription, "<IMG", "<img");
	longDesc = STRreplace.replace(longDesc, "SRC=", "src=");
	longDesc = STRreplace.replace(longDesc, "HREF=", "href=");
	longDesc = STRreplace.replace(longDesc, "'", "\"");
	extractor.set(longDesc, "<img", ">", 0, true);
	String tag;
	extractValue.set("", "src=\"", "\"", 0, true);
	while ((tag = extractor.findNext()).length() > 0) {
	    String imgUrl = extractValue.findFirst(tag);
	    int intQmark = imgUrl.indexOf("?");
	    if (intQmark > -1) {
		imgUrl = imgUrl.substring(0, intQmark);
	    }
	    int typeStart = imgUrl.lastIndexOf('.');
	    if (typeStart > -1) {
		String imgType = (imgUrl.substring(typeStart).toLowerCase() + "    ").substring(0, 4).trim();
		// Delete possible characters in URL after the image extension
		imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
		if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
		    try {
			CacheImage imageInfo = spiderImage(chD, imgUrl, imgType);
			// title from title or alt
			chD.images.add(imageInfo);
		    } catch (Exception e) {
			Preferences.itself().log("Error loading image: " + imgUrl, e);
		    }
		}
	    }
	}
	// ===================================
	// checking a - tags of description
	// ===================================
	extractor.set(longDesc, "<a", "/a>", 0, true);
	extractValue.set("", "href=\"", "\"", 0, true);
	while ((tag = extractor.findNext()).length() > 0) {
	    String imgUrl = extractValue.findFirst(tag);
	    int intQmark = imgUrl.indexOf("?");
	    if (intQmark > -1) {
		imgUrl = imgUrl.substring(0, intQmark);
	    }
	    int typeStart = imgUrl.lastIndexOf('.');
	    if (typeStart > 0) {
		String imgType = (imgUrl.substring(typeStart).toLowerCase() + "    ").substring(0, 4).trim();
		// Delete possible characters in URL after the image extension
		imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
		if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
		    try {
			CacheImage imageInfo = spiderImage(chD, imgUrl, imgType);
			imageInfo.setTitle(extractValue.findNext(">", "<"));
			chD.images.add(imageInfo);
		    } catch (Exception e) {
			Preferences.itself().log("Error loading image: " + imgUrl, e);
		    }
		}
	    }
	}
    }

    /**
     * In the spoilerSection
     * 
     * @param chD
     */
    private void getSpoilerImages(CacheHolderDetail chD) {
	// ===================================
	// checking li / a - tags of spoilerSection
	// ===================================
	String tag;
	extractor.set(extractor.set(extractor.set(this.wayPointPage, spoilerSectionStart, spoilerSectionEnd, 0, false).findNext(), spoilerSectionStart2, spoilerSectionEnd, 0, true).findNext());
	while ((tag = extractor.findNext("<li>", "</li>")).length() > 0) {
	    String imgUrl = extractValue.set(tag, "href=\"", "\"", 0, true).findNext();
	    int intQmark = imgUrl.indexOf("?");
	    if (intQmark > -1) {
		imgUrl = imgUrl.substring(0, intQmark);
	    }
	    int typeStart = imgUrl.lastIndexOf('.');
	    if (typeStart > 0) {
		String imgType = (imgUrl.substring(typeStart).toLowerCase() + "    ").substring(0, 4).trim();
		if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
		    // Delete possible characters in URL after the image extension
		    imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
		    try {
			CacheImage imageInfo = spiderImage(chD, imgUrl, imgType);
			imageInfo.setTitle(extractValue.findNext(">", "<"));
			String imgComment = extractValue.findNext(imgCommentExStart, imgCommentExEnd);
			while (imgComment.startsWith("<br />"))
			    imgComment = imgComment.substring(6);
			while (imgComment.endsWith("<br />"))
			    imgComment = imgComment.substring(0, imgComment.length() - 6);
			imageInfo.setComment(imgComment);
			chD.images.add(imageInfo);
		    } catch (Exception e) {
			Preferences.itself().log("Error loading image: " + imgUrl, e);
		    }
		}
	    }
	}
    }

    Vector spideredUrls;
    int spiderCounter = Integer.MAX_VALUE; // number of files (images)

    private CacheImage spiderImage(CacheHolderDetail chD, String originalUrl, String imgType) {
	if (chD.images.size() == 0) {
	    spideredUrls = new Vector();
	    spiderCounter = 0; // the first file
	}
	String wayPoint = chD.getParent().getWayPoint();
	CacheImages lastImages = null;
	// First: Get current image object of waypoint before spidering images.
	final CacheHolder oldCh = MainForm.profile.cacheDB.get(wayPoint);
	if (oldCh != null) {
	    lastImages = oldCh.getCacheDetails(false).images;
	}
	wayPoint = wayPoint.toLowerCase();

	String downloadUrl = originalUrl;
	String spideredName = downloadUrl;
	if (!downloadUrl.startsWith("http"))
	    // only clear if starts with / not ..
	    downloadUrl = "http://www.geocaching.com" + downloadUrl;
	else {
	    downloadUrl = STRreplace.replace(downloadUrl, "groundspeak", "geocaching");
	    // links to images in the description directs to one which has reduced in their size.
	    // We like to load the images in their original size:
	    // if (imgUrl.startsWith("http://img.geocaching.com/cache/display")) imgUrl = "http://img.geocaching.com/cache" + imgUrl.substring("http://img.geocaching.com/cache/display".length());
	    //
	    // http://imgcdn.geocaching.com/cache/large/3f8dfccc-958a-4cb8-bfd3-be3ab7db276b.jpg
	    // is same as http://img.geocaching.com/cache/3f8dfccc-958a-4cb8-bfd3-be3ab7db276b.jpg
	    if (downloadUrl.indexOf("geocaching.com") > -1) {
		spideredName = downloadUrl.substring(downloadUrl.lastIndexOf("/"), downloadUrl.lastIndexOf("."));
		if (downloadUrl.indexOf("www.geocaching.com") == -1) {
		    downloadUrl = "http://img.geocaching.com/cache" + spideredName + imgType;
		}
		// else gc smileys from www.geocaching.com
	    }
	}

	CacheImage imageInfo = null;
	// Index of already spidered Url in list of spideredUrls
	int idxUrl = spideredUrls.find(spideredName);
	if (idxUrl < 0) {
	    // Not yet spidered 
	    String fileName = wayPoint + "_" + spiderCounter + imgType;
	    if (lastImages != null) {
		if (downloadUrl.indexOf("geocaching.com") > -1) {
		    // former download from gc
		    imageInfo = needsSpidering(lastImages, originalUrl, fileName);
		}
	    }
	    if (imageInfo == null) {
		//needsSpidering
		imageInfo = new CacheImage();
		try {
		    UrlFetcher.fetchDataFile(downloadUrl, MainForm.profile.dataDir + fileName);
		} catch (Exception ex) {
		    Preferences.itself().log("[spiderImage] Problem while fetching image", ex);
		}
		imageInfo.setFilename(fileName);
		imageInfo.setURL(originalUrl);
		Preferences.itself().log("[getImages] Loaded image: " + originalUrl + " as " + fileName);
	    }
	    spideredUrls.add(spideredName); // index spiderCounter
	    spiderCounter++;
	} else {
	    // Image already spidered as wayPoint_'idxUrl'
	    String fileName = wayPoint + "_" + idxUrl + imgType;
	    imageInfo = new CacheImage();
	    imageInfo.setFilename(fileName);
	    imageInfo.setURL(originalUrl);
	    Preferences.itself().log("[getImages] Already loaded image: " + originalUrl + " as " + fileName);
	}

	imageInfo.setTitle(wayPoint + "_" + chD.images.size());
	imageInfo.setComment(null);
	return imageInfo;
    }

    // images of gc don't change, they get a new url
    // the numbering of files (newFilename) must be the same (alternative of renaming not implemented)
    // and they must exist in the filesystem
    private CacheImage needsSpidering(CacheImages lastImages, String toCheckUrl, String newFilename) {
	CacheImage result = null;
	for (int i = 0; i < lastImages.size(); i++) {
	    CacheImage img = lastImages.get(i);
	    if (img.getURL().equals(toCheckUrl) && img.getFilename().equals(newFilename)) {
		String location = MainForm.profile.dataDir + newFilename;
		if ((new File(location)).exists()) {
		    result = img;
		    Preferences.itself().log("[getImages] Already existing image: " + toCheckUrl + " as " + newFilename);
		    break;
		}
	    }
	}
	return result;
    }

    /**
     * Deletes images that are no longer needed.
     * we have a counter in the imagename starting with 0
     * files of oldImages are already overwritten (by download)
     * or reused
     * 
     * @param CacheHolderDetail chD
     *            to get reference to oldimages
     */
    private void cleanupOldImages(CacheHolderDetail chD) {
	String wayPoint = chD.getParent().getWayPoint();
	CacheImages oldImages = null;
	// First: Get current image object of waypoint before spidering images.
	final CacheHolder oldCh = MainForm.profile.cacheDB.get(wayPoint);
	if (oldCh != null) {
	    oldImages = oldCh.getCacheDetails(false).images;
	}
	if (oldImages != null) {
	    for (int i = 0; i < oldImages.size(); i++) {
		String obsoleteFilename = oldImages.get(i).getFilename();
		String[] parts = mString.split(obsoleteFilename, '_');
		String[] counter = mString.split(parts[1], '.');
		if (Convert.toInt(counter[0]) >= spiderCounter) {
		    File tmpFile = new File(MainForm.profile.dataDir + obsoleteFilename);
		    if (tmpFile.exists() && tmpFile.canWrite()) {
			Preferences.itself().log("Image no longer needed. Deleting: " + obsoleteFilename);
			tmpFile.delete();
		    }
		}
	    }
	}
    }

    /**
     * Read all additional waypoints from a previously fetched cachepage.
     * 
     * @param doc
     *            The previously fetched cachepage
     * @param wayPoint
     *            The name of the cache
     * @param is_found
     *            Found status of the cached (is inherited by the additional waypoints)
     */
    boolean koords_not_yet_found = true;

    private void getAddWaypoints(String doc, String wayPoint, boolean is_found) throws Exception {
	final Extractor exWayBlock = new Extractor(doc, p.getProp("wayBlockExStart"), p.getProp("wayBlockExEnd"), 0, false);
	String wayBlock;
	if ((wayBlock = exWayBlock.findNext()).length() > 0) {
	    if (wayBlock.indexOf("No additional waypoints to display.") < 0) {
		final Regex nameRex = new Regex(p.getProp("nameRex"));
		final Regex koordRex = new Regex(p.getProp("koordRex"));
		final Regex descRex = new Regex(p.getProp("descRex"));
		final Regex typeRex = new Regex(p.getProp("typeRex"));
		int counter = 0;
		final Extractor exRowBlock = new Extractor(wayBlock, p.getProp("rowBlockExStart"), p.getProp("rowBlockExEnd"), 0, false);
		String rowBlock;
		rowBlock = exRowBlock.findNext();
		while ((rowBlock = exRowBlock.findNext()).length() > 0) {
		    CacheHolder hd = null;

		    final Extractor exPrefix = new Extractor(rowBlock, p.getProp("prefixExStart"), p.getProp("prefixExEnd"), 0, true);
		    final String prefix = exPrefix.findNext();
		    String adWayPoint;
		    if (prefix.length() == 2)
			adWayPoint = prefix + wayPoint.substring(2);
		    else
			adWayPoint = MyLocale.formatLong(counter, "00") + wayPoint.substring(2);
		    counter++;
		    final int idx = MainForm.profile.getCacheIndex(adWayPoint);
		    if (idx >= 0) {
			// Creating new CacheHolder, but accessing old cache.xml file
			hd = new CacheHolder();
			hd.setWayPoint(adWayPoint);
			// Accessing Details reads file if not yet done
			hd.getCacheDetails(true);
		    } else {
			hd = new CacheHolder();
			hd.setWayPoint(adWayPoint);
		    }
		    hd.initStates(idx < 0);

		    nameRex.search(rowBlock);
		    if (nameRex.didMatch()) {
			hd.setCacheName(nameRex.stringMatched(1));
		    } else {
			Preferences.itself().log("check nameRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }

		    koordRex.search(rowBlock);
		    if (koordRex.didMatch()) {
			hd.setPos(new CWPoint(koordRex.stringMatched(1)));
			koords_not_yet_found = false;
		    } else {
			if (koords_not_yet_found) {
			    koords_not_yet_found = false;
			    Preferences.itself().log("check koordRex in spider.def" + Preferences.NEWLINE + rowBlock);
			}
		    }

		    typeRex.search(rowBlock);
		    if (typeRex.didMatch()) {
			hd.setType(CacheType.gpxType2CwType("Waypoint|" + typeRex.stringMatched(1)));
		    } else {
			Preferences.itself().log("check typeRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }

		    rowBlock = exRowBlock.findNext();
		    descRex.search(rowBlock);
		    if (descRex.didMatch()) {
			hd.getCacheDetails(false).setLongDescription(descRex.stringMatched(1).trim());
		    } else {
			Preferences.itself().log("check descRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }
		    hd.setFound(is_found);
		    hd.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
		    hd.setHard(CacheTerrDiff.CW_DT_UNSET);
		    hd.setTerrain(CacheTerrDiff.CW_DT_UNSET);

		    if (idx < 0) {
			MainForm.profile.cacheDB.add(hd);
			hd.save();
		    } else {
			final CacheHolder cx = MainForm.profile.cacheDB.get(idx);
			final boolean checked = cx.is_Checked;
			cx.initStates(false);
			cx.update(hd);
			cx.is_Checked = checked;
			cx.save();
		    }
		}
	    }
	}
    }

    public void getAttributes(CacheHolderDetail chD) {
	final Extractor attBlock = new Extractor(wayPointPage, attBlockExStart, attBlockExEnd, 0, true);
	final String atts = attBlock.findNext();
	final Extractor attEx = new Extractor(atts, attExStart, attExEnd, 0, true);
	String attribute;
	chD.attributes.clear();
	while ((attribute = attEx.findNext()).length() > 0) {
	    chD.attributes.add(attribute);
	}
	chD.getParent().setAttribsAsBits(chD.attributes.getAttribsAsBits());
    }

    final static String hex = ewe.util.TextEncoder.hex;

    public String encodeUTF8URL(byte[] what) {
	final int max = what.length;
	// Assume each char is a UTF char and encoded into 6 chars
	final char[] dest = new char[6 * max];
	char d = 0;
	for (int i = 0; i < max; i++) {
	    final char c = (char) what[i];
	    if (c <= ' ' || c == '+' || c == '&' || c == '%' || c == '=' || c == '|' || c == '{' || c == '}' || c > 0x7f) {
		dest[d++] = '%';
		dest[d++] = hex.charAt((c >> 4) & 0xf);
		dest[d++] = hex.charAt(c & 0xf);
	    } else
		dest[d++] = c;
	}
	return new String(dest, 0, d);
    }

    /**
     * Load the bug id for a given name. This method is not ideal, as there are sometimes several bugs with identical names but different IDs. Normally the bug GUID is used which can be obtained from the cache page.<br>
     * Note that each bug has both an ID and a GUID.
     * 
     * @param name
     *            The name (or partial name) of a travelbug
     * @return the id of the bug
     */
    public String getBugId(String name) {
	String bugList;
	try {
	    // infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
	    bugList = UrlFetcher.fetch(p.getProp("getBugByName") + STRreplace.replace(SafeXML.clean(name), " ", "+"));
	    Preferences.itself().log("[getBugId] Fetched bugId: " + name);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugId] Could not fetch bug list" + name, ex);
	    bugList = "";
	}
	try {
	    if (bugList.equals("") || bugList.indexOf(p.getProp("bugNotFound")) >= 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6020, "Travelbug not found.")).wait(FormBase.OKB);
		return "";
	    }
	    if (bugList.indexOf(p.getProp("bugTotalRecords")) < 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6021, "More than one travelbug found. Specify name more precisely.")).wait(FormBase.OKB);
		return "";
	    }
	    final Extractor exGuid = new Extractor(bugList, p.getProp("bugGuidExStart"), p.getProp("bugGuidExEnd"), 0, Extractor.EXCLUDESTARTEND);
	    // TODO Replace with spider.def
	    return exGuid.findNext();
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugId] Error getting TB", ex);
	    return "";
	}
    }

    /**
     * Fetch a bug's mission for a given GUID or ID. If the guid String is longer than 10 characters it is assumed to be a GUID, otherwise it is an ID.
     * 
     * @param guid
     *            the guid or id of the travelbug
     * @return The mission
     */
    public String getBugMissionByGuid(String guid) {
	String bugDetails;
	try {
	    // infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
	    if (guid.length() > 10)
		bugDetails = UrlFetcher.fetch(p.getProp("getBugByGuid") + guid);
	    else
		bugDetails = UrlFetcher.fetch(p.getProp("getBugById") + guid);
	    Preferences.itself().log("[getBugMissionByGuid] Fetched TB detailsById: " + guid);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionByGuid] Could not fetch TB details " + guid, ex);
	    bugDetails = "";
	}
	try {
	    if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6020, "Travelbug not found.")).wait(FormBase.OKB);
		return "";
	    }
	    final Extractor exDetails = new Extractor(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND);
	    return exDetails.findNext();
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionByGuid] Error getting TB " + guid, ex);
	    return "";
	}
    }

    /**
     * Fetch a bug's mission for a given tracking number
     * 
     * @param trackNr
     *            the tracking number of the travelbug
     * @return The mission
     */
    public String getBugMissionByTrackNr(String trackNr) {
	String bugDetails;
	try {
	    bugDetails = UrlFetcher.fetch(p.getProp("getBugByTrackNr") + trackNr);
	    Preferences.itself().log("[getBugMissionByTrackNr] Fetched bug detailsByTrackNr: " + trackNr);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionByTrackNr] getBugByTrackNr " + trackNr, ex);
	    bugDetails = "";
	}
	try {
	    if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
		Preferences.itself().log("[getBugMissionByTrackNr], bugNotFound " + trackNr, null);
		return "";
	    }
	    final Extractor exDetails = new Extractor(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND);
	    return exDetails.findNext();
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionByTrackNr] TB Details, bugNotFound " + trackNr, ex);
	    return "";
	}
    }

    /**
     * Fetch a bug's mission and namefor a given tracking number
     * 
     * @param TB
     *            the travelbug
     * @return true if suceeded
     */
    public boolean getBugMissionAndNameByTrackNr(Travelbug TB) {
	String bugDetails;
	final String trackNr = TB.getTrackingNo();
	try {
	    bugDetails = UrlFetcher.fetch(p.getProp("getBugByTrackNr") + trackNr);
	    Preferences.itself().log("[getBugMissionAndNameByTrackNr] Fetched TB getBugByTrackNr: " + trackNr);
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionAndNameByTrackNr] Could not fetch bug details: " + trackNr, ex);
	    bugDetails = "";
	}
	try {
	    if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
		Preferences.itself().log("[getBugMissionAndNameByTrackNr], bugNotFound: " + trackNr, null);
		return false;
	    }
	    final Extractor exDetails = new Extractor(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND);
	    TB.setMission(exDetails.findNext());
	    final Extractor exName = new Extractor(bugDetails, p.getProp("bugNameStart"), p.getProp("bugNameEnd"), 0, Extractor.EXCLUDESTARTEND);
	    TB.setName(exName.findNext());
	    return true;
	} catch (final Exception ex) {
	    Preferences.itself().log("[getBugMissionAndNameByTrackNr] TB Details, bugNotFound: " + trackNr, ex);
	    return false;
	}
    }

    class SpiderProperties extends Properties {
	SpiderProperties() {
	    super();
	    try {
		load(new FileInputStream(FileBase.getProgramDirectory() + "/spider.def"));
	    } catch (final Exception ex) {
		Preferences.itself().log("Failed to load spider.def from " + FileBase.getProgramDirectory(), ex);
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5504, "Could not load 'spider.def'")).wait(FormBase.OKB);
	    }
	}

	/**
	 * Gets an entry in spider.def by its key (tag)
	 * 
	 * @param key
	 *            The key which is attributed to a specific entry
	 * @return The value for the key
	 * @throws Exception
	 *             When a key is requested which doesn't exist
	 */
	public String getProp(String key) throws Exception {
	    final String s = super.getProperty(key);
	    if (s == null) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5497, "Error missing tag in spider.def") + ": " + key).wait(FormBase.OKB);
		Preferences.itself().log("Missing tag in spider.def: " + key);
		throw new Exception("Missing tag in spider.def: " + key);
	    }
	    return s;
	}

    }

    class RouteImporter extends MinML {

	String _fileName;
	Vector _routePoints;

	RouteImporter(String fileName) {
	    _fileName = fileName;
	    _routePoints = new Vector();
	}

	Vector doIt() {
	    ewe.io.TextReader r = null;
	    try {
		r = new ewe.io.TextReader(_fileName);
		r.codec = new AsciiCodec();
		String s;
		s = r.readLine();
		if (s.startsWith("ï»¿")) {
		    r.close();
		    r = new ewe.io.TextReader(_fileName);
		    r.codec = new BetterUTF8Codec();
		} else {
		    r.close();
		    r = new ewe.io.TextReader(_fileName);
		    r.codec = new AsciiCodec();
		}
		parse(r);
		r.close();
	    } catch (final Exception e) {
	    }
	    return _routePoints;
	}

	public void startElement(String name, AttributeList atts) {
	    if (name.equals("wpt") || name.equals("trkpt") || name.equals("rtept") || name.equals("gpxx:rpt")) {
		final double lat = Common.parseDouble(atts.getValue("lat"));
		final double lon = Common.parseDouble(atts.getValue("lon"));
		final CoordinatePoint tp = new CoordinatePoint(lat, lon);
		if (tp.isValid())
		    _routePoints.add(tp);
		return;
	    }
	}

	public void endElement(String name) {

	}
    }
}
