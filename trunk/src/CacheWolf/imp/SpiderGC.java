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

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheImages;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.DateFormat;
import CacheWolf.Extractor;
import CacheWolf.Global;
import CacheWolf.ImageInfo;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.LogList;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.STRreplace;
import CacheWolf.SafeXML;
import CacheWolf.Travelbug;
import CacheWolf.UrlFetcher;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.Track;
import CacheWolf.navi.TrackPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.BetterUTF8Codec;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.IOException;
import ewe.net.URL;
import ewe.net.UnknownHostException;
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
public class SpiderGC {

    public static boolean loggedIn = false;

    // Return values for spider action
    /**
     * Ignoring a premium member cache when spidering from a non premium account
     */
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

    private static double minDistance = 0;
    private static double maxDistance = 0;
    private static String direction = "";
    private static String[] directions;

    private final CacheDB cacheDB;
    private final Vector cachesToLoad = new Vector();
    private InfoBox infB;
    private static SpiderProperties p = null;
    // following filled at doit
    private CWPoint origin;
    private boolean doNotgetFound;
    private String cacheTypeRestriction;
    private boolean spiderAllFinds;
    private String htmlListPage;
    private final static String wayPointUrl = "http://www.geocaching.com/seek/cache_details.aspx?wp=";
    private final static String loginPageUrl = "https://www.geocaching.com/login/default.aspx";
    private int maxUpdate;
    // private boolean maxNumberAbort;
    private byte restrictedCacheType = 0;
    private String fileName = "";

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

    private static Regex RexPropWaypoint;
    private static Regex RexPropType;
    private static Regex RexUserToken;
    private static String icon_smile;
    private static String icon_camera;
    private static String icon_attended;
    private static Regex RexCacheType;

    private int numFoundUpdates = 0;
    private int numArchivedUpdates = 0;
    private int numAvailableUpdates = 0;
    private int numLogUpdates = 0;
    private int numPrivate = 0;
    private int page_number = 1;

    public SpiderGC() {
	this.cacheDB = Global.profile.cacheDB;
	initialiseProperties();
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
	origin = Global.pref.getCurCentrePt();
	if (!spiderAllFinds && !origin.isValid()) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5509, "Coordinates for centre must be set")).wait(FormBase.OKB);
	    return;
	}

	// Reset states for all caches when spidering (http://tinyurl.com/dzjh7p)
	for (int i = 0; i < cacheDB.size(); i++) {
	    final CacheHolder ch = cacheDB.get(i);
	    if (ch.mainCache == null)
		ch.initStates(false);
	}

	if (doDownloadGui(0)) {

	    Vm.showWait(true);
	    infB = new InfoBox("Status", MyLocale.getMsg(5502, "Fetching first page..."));
	    infB.exec();

	    double lowerDistance;
	    double upperDistance = minDistance;
	    int completeSpiderErrors = 0;
	    boolean alreadyAnswered = false;
	    boolean lastAnswer = true;
	    int pageLimit = 20; // das sorgt für die Gruppierung
	    int maxPages = 0;

	    if (!login())
		upperDistance = maxDistance; // Abbruch
	    else {
		getFirstListPage(this.getDistanceInMiles(maxDistance));
		maxPages = (int) java.lang.Math.ceil(getNumFound(htmlListPage) / 20);
	    }

	    String s = "ListPages Properties : " + Preferences.NEWLINE;
	    s = s + "minDistance          : " + minDistance + Preferences.NEWLINE;
	    s = s + "maxDistance          : " + maxDistance + Preferences.NEWLINE;
	    s = s + "directions           : " + direction + Preferences.NEWLINE;
	    s = s + "maxNew               : " + Global.pref.maxSpiderNumber + Preferences.NEWLINE;
	    s = s + "maxUpdate            : " + maxUpdate + Preferences.NEWLINE;
	    s = s + "with Founds          : " + (doNotgetFound ? "no" : "yes") + Preferences.NEWLINE;
	    s = s + "alias is premium memb: " + (!Global.pref.isPremium ? "no" : "yes") + Preferences.NEWLINE;
	    s = s + "Update if new Log    : " + (Global.pref.checkLog ? "yes" : "no") + Preferences.NEWLINE;
	    s = s + "Update if TB changed : " + (Global.pref.checkTBs ? "yes" : "no") + Preferences.NEWLINE;
	    s = s + "Update if DTS changed: " + (Global.pref.checkDTS ? "yes" : "no") + Preferences.NEWLINE;
	    s = s + "maxPages for x Miles : " + maxPages + " for " + this.getDistanceInMiles(maxDistance) + Preferences.NEWLINE;
	    Global.pref.log(s, null);
	    lastPageVisited = -1; // for not to double check pages on next group run

	    while (upperDistance < maxDistance && !infB.isClosed()) {
		lowerDistance = upperDistance;
		if ((int) lowerDistance < ((int) maxDistance - 1)) {
		    upperDistance = this.getUpperDistance(lowerDistance, maxDistance, maxPages, pageLimit);
		} else {
		    upperDistance = maxDistance;
		}
		Hashtable cachesToUpdate = new Hashtable(cacheDB.size());
		cachesToUpdate = fillDownloadLists(Global.pref.maxSpiderNumber, maxUpdate, upperDistance, lowerDistance, directions, cachesToUpdate);
		if (cachesToUpdate == null) {
		    cachesToUpdate = new Hashtable();
		}
		if (!infB.isClosed()) {
		    infB.setInfo(MyLocale.getMsg(5511, "Found ") + cachesToLoad.size() + MyLocale.getMsg(5512, " caches"));
		}
		// continue to update index to changed cache.xml things
		// (size,terrain,difficulty,...?)

		// =======
		// Now ready to spider each cache in the lists
		// =======

		int spiderErrors = 0;
		final int totalCachesToLoad = cachesToLoad.size() + cachesToUpdate.size();
		Global.pref.log("Download properties : " + Preferences.NEWLINE + "maxLogs: " + Global.pref.maxLogsToSpider + Preferences.NEWLINE + "with pictures     : " + (!Global.pref.downloadPics ? "no" : "yes") + Preferences.NEWLINE
			+ "with tb           : " + (!Global.pref.downloadTBs ? "no" : "yes") + Preferences.NEWLINE, null);

		Global.mainTab.tablePanel.updateStatusBar();

		if (!infB.isClosed()) {
		    spiderErrors = downloadCaches(cachesToLoad, spiderErrors, totalCachesToLoad);

		    if (cachesToUpdate.size() > 0) {
			switch (Global.pref.spiderUpdates) {
			case Preferences.NO:
			    cachesToUpdate.clear();
			    break;
			case Preferences.ASK:
			    if (!alreadyAnswered) {
				lastAnswer = (new InfoBox(MyLocale.getMsg(5517, "Spider Updates?"), cachesToUpdate.size() + MyLocale.getMsg(5518, " caches in database need an update. Update now?")).wait(FormBase.IDYES | FormBase.IDNO) != FormBase.IDOK);
				alreadyAnswered = true;
			    }
			    if (lastAnswer) {
				cachesToUpdate.clear();
			    }
			    break;
			}
		    }

		    spiderErrors = updateCaches(cachesToUpdate, spiderErrors, totalCachesToLoad);
		}
		completeSpiderErrors = completeSpiderErrors + spiderErrors;
	    } // while

	    if (completeSpiderErrors > 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), completeSpiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded.")).wait(FormBase.OKB);
	    }

	    Global.profile.restoreFilter();
	    Global.profile.saveIndex(true);

	    if (!infB.isClosed()) {
		infB.close(0);
	    }
	    Vm.showWait(false);
	    loggedIn = false; // check again login on next spider
	}
    } // End of DoIt

    private int getDistanceInMiles(double value) {
	// max distance in miles for URL, so we can get more than 80km
	int toDistanceInMiles = (int) java.lang.Math.ceil(value);
	if (Global.pref.metricSystem != Metrics.IMPERIAL) {
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
	Navigate navigate = Global.mainTab.navigate;
	MovingMap movingMap = Global.mainTab.movingMap;
	// vorsichtshalber
	if (navigate == null)
	    return;
	if (movingMap == null)
	    return;
	if (!doDownloadGui(1))
	    return;

	// getting the route
	CWPoint startPos = Global.pref.getCurCentrePt();
	if (!fileName.equals("")) {
	    final RouteImporter ri = new RouteImporter(fileName);
	    points = ri.doIt();
	    if (points.size() > 0) {
		if (navigate.curTrack == null) {
		    navigate.curTrack = new Track(navigate.trackColor);
		    movingMap.addTrack(navigate.curTrack);
		}
		for (int i = 0; i < points.size(); i++) {
		    try {
			navigate.curTrack.add((TrackPoint) points.get(i));
		    } catch (final IndexOutOfBoundsException e) {
			// track full -> create a new one
			navigate.curTrack = new Track(navigate.trackColor);
			navigate.curTrack.add((TrackPoint) points.get(i));
			movingMap.addTrack(navigate.curTrack);
		    }

		}
		final TrackPoint tp = (TrackPoint) points.get(0);
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
	for (int i = 0; i < cacheDB.size(); i++) {
	    final CacheHolder ch = cacheDB.get(i);
	    if (ch.mainCache == null)
		ch.initStates(false);
	}

	double lateralDistance = maxDistance; // Seitenabstand in km
	if (Global.pref.metricSystem == Metrics.IMPERIAL) {
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
	    Global.pref.log("Start at " + origin + " to check " + points.size() + " points.");

	while (nextPos != null) {
	    if (fileName.equals("")) {
		nextPos = nextRoutePoint(startPos, lateralDistance);
	    } else {
		double tmpDistance = 0;
		while (tmpDistance < lateralDistance && pointsIndex < points.size()) {
		    final TrackPoint tp = (TrackPoint) points.get(pointsIndex);
		    nextPos = new CWPoint(tp.latDec, tp.lonDec);
		    tmpDistance = nextPos.getDistance(startPos);
		    pointsIndex++;
		}
		if (pointsIndex == points.size())
		    nextPos = null;
		else {
		    if (points != null)
			Global.mainTab.tablePanel.updateStatusBar("" + pointsIndex + "(" + points.size() + ")" + nextPos);
		}
	    }

	    if (nextPos != null) {
		origin = startPos;
		if (points != null)
		    Global.pref.log("m: do " + pointsIndex + " of (" + points.size() + ") at " + origin);
		getCaches(lateralDistance);

		final double degrees = startPos.getBearing(nextPos);
		final double distanceToNextCache = startPos.getDistance(nextPos);
		final double anzCheckPoints = distanceToNextCache / lateralDistance;
		for (int i = 1; i < anzCheckPoints; i++) {
		    final CWPoint nextCheckPoint = startPos.project(degrees, lateralDistance);
		    startPos = nextCheckPoint;
		    origin = nextCheckPoint;
		    if (points != null)
			Global.pref.log("s: do " + pointsIndex + " of (" + points.size() + ") at " + origin);
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
		String wpt = (String) cachesToLoad.get(i);
		final boolean is_found = wpt.indexOf("found") != -1;
		if (is_found)
		    wpt = wpt.substring(0, wpt.indexOf("found"));
		final int j = cacheDB.getIndex(wpt);
		if (j != -1)
		    cacheDB.removeElementAt(j);
	    }
	    // und frisch geladen
	    spiderErrors = downloadCaches(cachesToLoad, spiderErrors, cachesToLoad.size());
	} else {
	    // man könnte auch aus der Liste einen Quick - Import erstellen
	}

	infB.close(0);
	Vm.showWait(false);
	if (spiderErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), spiderErrors + MyLocale.getMsg(5516, " cache descriptions%0acould not be loaded.")).wait(FormBase.OKB);
	}

	Global.profile.restoreFilter();
	Global.profile.saveIndex(true);
	loggedIn = false; // check again login on next spider

    }

    private CWPoint nextRoutePoint(CWPoint startPos, double lateralDistance) {
	// get next Destination
	double nextDistance = 0;
	int index = -1;
	CacheHolder nextCache = null;
	CacheHolder ch = null;
	for (int i = 0; i < cacheDB.size(); i++) {
	    ch = cacheDB.get(i);
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
	    return cacheDB.get(index).getPos();
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
		double[] DistanceAndDirection = { (0.0), (0.0) };
		RexPropListBlock.search(htmlListPage);
		String tableOfHtmlListPage;
		if (RexPropListBlock.didMatch()) {
		    tableOfHtmlListPage = RexPropListBlock.stringMatched(1);
		} else {
		    Global.pref.log("[SpiderGC.java:fillDownloadLists]check listBlockRex!");
		    tableOfHtmlListPage = "";
		}
		RexPropLine.search(tableOfHtmlListPage);
		while (toDistance > 0) {
		    if (!RexPropLine.didMatch()) {
			if (page_number == 1 && found_on_page == 0)
			    Global.pref.log("[SpiderGC.java:fillDownloadLists]check lineRex!");
			break;
		    }
		    found_on_page++;
		    Global.mainTab.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
		    final String CacheDescriptionGC = RexPropLine.stringMatched(1);
		    DistanceAndDirection = getDistanceAndDirection(CacheDescriptionGC);
		    String chWaypoint = getWP(CacheDescriptionGC);
		    if (DistanceAndDirection[0] <= radiusKm) {
			final CacheHolder ch = cacheDB.get(chWaypoint);
			if (ch == null) { // not in DB
			    if (CacheDescriptionGC.indexOf(propFound) != -1)
				chWaypoint = chWaypoint + "found";
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
	    Global.pref.log("Nr Caches now: " + cachesToLoad.size());
	} // try
	catch (final Exception ex) {
	    Global.pref.log("Download error : ", ex, true);
	    infB.close(0);
	    Vm.showWait(false);
	}
    }

    private boolean doDownloadGui(int menu) {

	OCXMLImporterScreen options;
	direction = "";
	if (menu == 0 && spiderAllFinds) {
	    options = new OCXMLImporterScreen(MyLocale.getMsg(217, "Spider all finds from geocaching.com"), OCXMLImporterScreen.ISGC | OCXMLImporterScreen.MAXNUMBER | OCXMLImporterScreen.MAXUPDATE | OCXMLImporterScreen.IMAGES
		    | OCXMLImporterScreen.TRAVELBUGS | OCXMLImporterScreen.MAXLOGS);
	    // setting defaults for input
	    options.maxNumberUpdates.setText("0");
	    // doing the input
	    if (options.execute() == FormBase.IDCANCEL) {
		return false;
	    }
	    // setting default values for options not used (if necessary)
	    maxDistance = 1.0;
	    minDistance = 0.0;
	} else if (menu == 0) {
	    options = new OCXMLImporterScreen(MyLocale.getMsg(131, "Download from geocaching.com"), OCXMLImporterScreen.ISGC | OCXMLImporterScreen.MAXNUMBER | OCXMLImporterScreen.MAXUPDATE | OCXMLImporterScreen.MINDIST | OCXMLImporterScreen.DIST
		    | OCXMLImporterScreen.DIRECTION | OCXMLImporterScreen.INCLUDEFOUND | OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.TRAVELBUGS | OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.TYPE);
	    // setting defaults for input
	    if (Global.pref.spiderUpdates == Preferences.NO) {
		options.maxNumberUpdates.setText("0");
	    }
	    // doing the input
	    if (options.execute() == FormBase.IDCANCEL) {
		return false;
	    }
	    // setting default values for options not used (if necessary)

	    final String minDist = options.minDistanceInput.getText();
	    minDistance = Common.parseDouble(minDist);
	    Global.profile.setMinDistGC(Double.toString(minDistance).replace(',', '.'));

	    direction = options.directionInput.getText();
	    directions = mString.split(direction, '-');

	    doNotgetFound = options.foundCheckBox.getState();
	    Global.profile.setDirectionGC(direction);

	} else if (menu == 1) {
	    // menu = 1 input values for get Caches along a route
	    options = new OCXMLImporterScreen(MyLocale.getMsg(137, "Download along a Route from geocaching.com"), OCXMLImporterScreen.ISGC | OCXMLImporterScreen.DIST | OCXMLImporterScreen.INCLUDEFOUND | OCXMLImporterScreen.TRAVELBUGS
		    | OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.FILENAME | OCXMLImporterScreen.TYPE);
	    // setting defaults for input doing the input
	    if (options.execute() == FormBase.IDCANCEL) {
		return false;
	    }
	    // setting default values for options not used (if necessary)
	    minDistance = 0.0;
	    doNotgetFound = options.foundCheckBox.getState();
	    maxUpdate = 0;
	    fileName = options.fileName;
	} else { // if (menu == 2) {
	    options = new OCXMLImporterScreen(MyLocale.getMsg(138, "Qick Import"), OCXMLImporterScreen.ISGC | OCXMLImporterScreen.DIST | OCXMLImporterScreen.INCLUDEFOUND | OCXMLImporterScreen.TYPE);
	    // setting defaults for input doing the input
	    if (options.execute() == FormBase.IDCANCEL) {
		return false;
	    }
	    doNotgetFound = options.foundCheckBox.getState();
	}
	Global.pref.doNotGetFound = doNotgetFound;
	if (menu == 0) {

	    int maxNew = -1;
	    final String maxNumberString = options.maxNumberInput.getText();
	    if (maxNumberString.length() != 0) {
		maxNew = Common.parseInt(maxNumberString);
	    }
	    // if (maxNew == 0) return false;
	    if (maxNew == -1)
		maxNew = Integer.MAX_VALUE;
	    if (maxNew != Global.pref.maxSpiderNumber) {
		Global.pref.maxSpiderNumber = maxNew;
		Global.pref.savePreferences();
	    }

	    maxUpdate = -1;
	    final String maxUpdateString = options.maxNumberUpdates.getText();
	    if (maxUpdateString.length() != 0) {
		maxUpdate = Common.parseInt(maxUpdateString);
	    }
	    if (maxUpdate == -1)
		maxUpdate = Integer.MAX_VALUE;
	}

	// options for all

	if (options.maxDistanceInput != null) {
	    final String maxDist = options.maxDistanceInput.getText();
	    maxDistance = Common.parseDouble(maxDist);
	    if (maxDistance == 0)
		return false;
	    // zur Sicherheit bei "along the route"
	    // mindenstens 500 meter Umkreis
	    if (maxDistance < 0.5)
		maxDistance = 0.5;
	    Global.profile.setDistGC(Double.toString(maxDistance));
	}

	// works even if TYPE not in options
	cacheTypeRestriction = options.getCacheTypeRestriction(p);
	restrictedCacheType = options.getRestrictedCacheType(p);

	options.close(0);

	return true;

    }

    private Hashtable fillDownloadLists(int maxNew, int maxUpdate, double toDistance, double fromDistance, String[] directions, Hashtable cExpectedForUpdate) {
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
	Global.pref.log("[SpiderGC:fillDownloadLists] got Listpage: " + startPage, null);

	int endPage = (int) (numFinds / 20);
	int anzPages = 1 + endPage - startPage;
	Global.pref.log("List up to " + anzPages + " pages (" + startPage + ".." + endPage + "). From " + fromDistanceInMiles + " miles (" + fromDistance + " km/miles)" + " to " + toDistanceInMiles + " miles (" + toDistance + " km/miles)", null);
	int numFoundInDB = 0; // Number of GC-founds already in this profile
	if (spiderAllFinds) {
	    numFoundInDB = getFoundInDB();
	    Global.pref.log((spiderAllFinds ? "all Finds (DB/GC)" + numFoundInDB + "/" + numFinds : "new and update Caches") + Preferences.NEWLINE, null);
	    maxNew = java.lang.Math.min(numFinds - numFoundInDB, maxNew);
	    if (maxUpdate == 0 && maxNew == 0) {
		Vm.showWait(false);
		infB.close(0);
		return null;
	    }
	}

	if (maxUpdate > 0) {
	    double distanceInKm = toDistance;
	    if (Global.pref.metricSystem == Metrics.IMPERIAL) {
		distanceInKm = Metrics.convertUnit(toDistance, Metrics.MILES, Metrics.KILOMETER);
	    }

	    double fromDistanceInKm = fromDistance;
	    if (Global.pref.metricSystem == Metrics.IMPERIAL) {
		fromDistanceInKm = Metrics.convertUnit(fromDistance, Metrics.MILES, Metrics.KILOMETER);
	    }

	    // expecting all are changed (archived caches remain always)
	    for (int i = 0; i < cacheDB.size(); i++) {
		final CacheHolder ch = cacheDB.get(i);
		if (spiderAllFinds) {
		    if ((ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC")) && !ch.is_black()) {
			cExpectedForUpdate.put(ch.getWayPoint(), ch);
		    }
		} else {
		    if ((!ch.is_archived()) && (ch.kilom >= fromDistanceInKm) && (ch.kilom <= distanceInKm) && !(doNotgetFound && (ch.is_found() || ch.is_owned())) && (ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC"))
			    && ((restrictedCacheType == CacheType.CW_TYPE_ERROR) || (ch.getType() == restrictedCacheType)) && !ch.is_black()) {
			cExpectedForUpdate.put(ch.getWayPoint(), ch);
		    }
		}
	    }
	}
	// for save reasons
	final int startSize = cExpectedForUpdate.size();

	// for don't loose the already done work
	final Hashtable cFoundForUpdate = new Hashtable(cacheDB.size());
	page_number = 1;
	int found_on_page = 0;
	try {
	    // Loop pages till maximum distance has been found or no more caches are in the list
	    while (toDistance > 0) {
		double[] DistanceAndDirection = { (0.0), (0.0) };
		RexPropListBlock.search(htmlListPage);
		String tableOfHtmlListPage;
		if (RexPropListBlock.didMatch()) {
		    tableOfHtmlListPage = RexPropListBlock.stringMatched(1);
		} else {
		    Global.pref.log("[SpiderGC.java:fillDownloadLists]check listBlockRex!");
		    tableOfHtmlListPage = "";
		}
		RexPropLine.search(tableOfHtmlListPage);
		while (toDistance > 0) {
		    if (!RexPropLine.didMatch()) {
			if (page_number == 1 && found_on_page == 0)
			    Global.pref.log("[SpiderGC.java:fillDownloadLists]check lineRex!");
			break;
		    }
		    found_on_page++;
		    Global.mainTab.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
		    final String CacheDescriptionGC = RexPropLine.stringMatched(1);
		    DistanceAndDirection = getDistanceAndDirection(CacheDescriptionGC);
		    String chWaypoint = getWP(CacheDescriptionGC);
		    if (DistanceAndDirection[0] <= toDistance) {
			final CacheHolder ch = cacheDB.get(chWaypoint);
			if (ch == null) { // not in DB
			    if (DistanceAndDirection[0] >= fromDistance && directionOK(directions, DistanceAndDirection[1]) && doPMCache(chWaypoint, CacheDescriptionGC) && cachesToLoad.size() < maxNew) {
				if (CacheDescriptionGC.indexOf(propFound) != -1)
				    chWaypoint = chWaypoint + "found";
				if (!cachesToLoad.contains(chWaypoint)) {
				    cachesToLoad.add(chWaypoint);
				}
			    } else {
				// Global.pref.log("no load of (Premium Cache/other direction/short
				// Distance ?) " + chWaypoint);
				cExpectedForUpdate.remove(chWaypoint);
			    }
			} else {
			    if (maxUpdate > 0) {
				// regardless of fromDistance
				if (!ch.is_black()) {
				    if (doPMCache(chWaypoint, CacheDescriptionGC) && updateExists(ch, CacheDescriptionGC)) {
					if (cFoundForUpdate.size() < maxUpdate) {
					    cFoundForUpdate.put(chWaypoint, ch);
					} else
					    cExpectedForUpdate.remove(chWaypoint);
				    } else
					cExpectedForUpdate.remove(chWaypoint);
				} else
				    cExpectedForUpdate.remove(chWaypoint);
			    }
			}
			if (cachesToLoad.size() >= maxNew) {
			    if (cFoundForUpdate.size() >= maxUpdate) {
				toDistance = 0;
				cExpectedForUpdate.clear();
			    } else {
				if (cExpectedForUpdate.size() <= cFoundForUpdate.size()) {
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
			cExpectedForUpdate.clear();
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
			Global.pref.log("[SpiderGC:fillDownloadLists] got Listpage: " + lastPageVisited, null);
			page_number++;
			found_on_page = 0;
		    } else {
			// stop, but download new ones if possible
			cExpectedForUpdate.clear();
			Global.pref.log("[SpiderGC:fillDownloadLists] Stopped at page number: " + page_number + " this is distance: " + DistanceAndDirection[0], null);
			found_on_page = 0;
			toDistance = 0;
		    }
		}
	    } // loop pages
	} // try
	catch (final Exception ex) {
	    Global.pref.log("Download error : ", ex, true);
	    infB.close(0);
	    Vm.showWait(false);
	    cExpectedForUpdate.clear();
	}

	String s = "Checked " + page_number + " pages" + Preferences.NEWLINE;
	s = s + "with " + ((page_number - 1) * 20 + found_on_page) + " caches" + Preferences.NEWLINE;
	s = s + "Found " + cachesToLoad.size() + " new caches" + Preferences.NEWLINE;
	s = s + "Found " + cExpectedForUpdate.size() + "/" + cFoundForUpdate.size() + " caches for update" + Preferences.NEWLINE;
	s = s + "Found " + (cExpectedForUpdate.size() - cFoundForUpdate.size()) + " caches possibly archived." + Preferences.NEWLINE;
	s = s + "Found " + numPrivate + " Premium Caches (for non Premium Member.)" + Preferences.NEWLINE;
	s = s + "Found " + numAvailableUpdates + " caches with changed available status." + Preferences.NEWLINE;
	s = s + "Found " + numLogUpdates + " caches with new found in log." + Preferences.NEWLINE;
	s = s + "Found " + numFoundUpdates + " own Finds" + Preferences.NEWLINE;
	s = s + "Found " + numArchivedUpdates + " unarchived." + Preferences.NEWLINE;
	Global.pref.log(s, null);

	if (spiderAllFinds) {
	    Global.pref.log("Found " + numFoundUpdates + " caches with no found in Global.profile." + Preferences.NEWLINE + "Found " + numArchivedUpdates + " caches with changed archived status." + Preferences.NEWLINE, null);
	}

	if (cExpectedForUpdate.size() == startSize)
	    cExpectedForUpdate.clear(); // there must be something wrong
	if (cExpectedForUpdate.size() == 0 // prima, alle tauchen in der gc-Liste auf
		|| cExpectedForUpdate.size() > maxUpdate // Restmenge zu gross, wir nehmen nur die sicher geänderten.
	) {
	    cExpectedForUpdate = cFoundForUpdate;
	} else {
	    // checking if all is in List by adding the known changed ones
	    Global.pref.log("possibly " + cExpectedForUpdate.size() + " + known " + cFoundForUpdate.size(), null);
	    for (final Enumeration e = cFoundForUpdate.elements(); e.hasMoreElements();) {
		final CacheHolder ch = (CacheHolder) e.nextElement();
		cExpectedForUpdate.put(ch.getWayPoint(), ch);
	    }
	    Global.pref.log("now will update: " + cExpectedForUpdate.size(), null);
	}
	s = "These Caches will be updated :" + Preferences.NEWLINE;
	s = s + "Out of " + startSize + Preferences.NEWLINE;
	for (final Enumeration e = cExpectedForUpdate.elements(); e.hasMoreElements();) {
	    final CacheHolder ch = (CacheHolder) e.nextElement();
	    s = s + ch.getWayPoint() + "(" + ch.kilom + " km )";
	    if (cFoundForUpdate.containsKey(ch.getWayPoint())) {
		s = s + " sure";
	    }
	    s = s + Preferences.NEWLINE;
	}
	Global.pref.log(s, null);
	return cExpectedForUpdate;
    }

    private int downloadCaches(Vector cachesToLoad, int spiderErrors, int totalCachesToLoad) {
	for (int i = 0; i < cachesToLoad.size(); i++) {
	    if (infB.isClosed())
		break;
	    String wpt = (String) cachesToLoad.get(i);
	    final boolean is_found = wpt.indexOf("found") != -1;
	    if (is_found)
		wpt = wpt.substring(0, wpt.indexOf("found"));
	    // Get only caches not already available in the DB
	    if (cacheDB.getIndex(wpt) == -1) {
		infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + wpt + " (" + (i + 1) + " / " + totalCachesToLoad + ")");
		final CacheHolder holder = new CacheHolder();
		holder.setWayPoint(wpt);
		final int test = getCacheByWaypointName(holder, false, Global.pref.downloadPics, Global.pref.downloadTBs, doNotgetFound);
		if (test == SPIDER_CANCEL) {
		    infB.close(0);
		    break;
		} else if (test == SPIDER_ERROR) {
		    spiderErrors++;
		} else if (test == SPIDER_OK) {
		    cacheDB.add(holder);
		    holder.save();
		} // For test == SPIDER_IGNORE_PREMIUM and SPIDER_IGNORE there is nothing to do
	    }
	}
	return spiderErrors;
    }

    private int updateCaches(Hashtable cachesToUpdate, int spiderErrors, int totalCachesToLoad) {
	int jj = 0;
	for (final Enumeration e = cachesToUpdate.elements(); e.hasMoreElements();) {
	    if (infB.isClosed())
		break;
	    final CacheHolder ch = (CacheHolder) e.nextElement();
	    jj++;
	    infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + ch.getWayPoint() + " (" + (cachesToLoad.size() + jj) + " / " + totalCachesToLoad + ")");
	    final int test = spiderSingle(cacheDB.getIndex(ch), infB);
	    if (test == SPIDER_CANCEL) {
		break;
	    } else {
		if (test == SPIDER_ERROR) {
		    spiderErrors++;
		    Global.pref.log("[updateCaches] could not spider " + ch.getWayPoint(), null);
		} else {
		    // Global.profile.hasUnsavedChanges=true;
		    if (test == SPIDER_IGNORE_PREMIUM)
			spiderErrors++;
		}
	    }
	}
	return spiderErrors;
    }

    /**
     * Method to spider a single cache. It assumes a login has already been performed!
     * 
     * @return 1 if spider was successful, -1 if spider was cancelled by closing the infobox, 0 error, but continue with next cache
     */
    public int spiderSingle(int number, InfoBox pInfB) {
	int ret = -1;
	this.infB = pInfB;
	final CacheHolder ch = new CacheHolder();
	ch.setWayPoint(cacheDB.get(number).getWayPoint());
	if (ch.isAddiWpt())
	    return -1; // addi waypoint, comes with parent cache
	if (!login())
	    return -1;
	try {
	    // Read the cache data from GC.COM and compare to old data
	    ret = getCacheByWaypointName(ch, true, Global.pref.downloadPics, Global.pref.downloadTBs, false);
	    // Save the spidered data
	    if (ret == SPIDER_OK) {
		final CacheHolder cacheInDB = cacheDB.get(number);
		cacheInDB.initStates(false);
		// preserve rating information
		ch.setNumRecommended(cacheInDB.getNumRecommended());
		if (Global.pref.downloadPics) {
		    // delete obsolete images when we have current set
		    CacheImages.cleanupOldImages(cacheInDB.getCacheDetails(true).images, ch.getCacheDetails(false).images);
		} else {
		    // preserve images if not downloaded
		    ch.getCacheDetails(false).images = cacheInDB.getCacheDetails(true).images;
		}
		cacheInDB.update(ch);
		cacheInDB.save();
	    }
	} catch (final Exception ex) {
	    Global.pref.log("[spiderSingle] Error spidering " + ch.getWayPoint() + " in spiderSingle", ex);
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
    public String getCacheCoordinates(String wayPoint) {
	String completeWebPage;
	// Check whether spider definitions could be loaded,
	// if not issue appropriate message and terminate
	// Try to login. If login fails, issue appropriate message and terminate
	if (!login())
	    return "";
	final InfoBox localInfB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
	localInfB.exec();
	try {
	    final String url = wayPointUrl + wayPoint;
	    completeWebPage = UrlFetcher.fetch(url);
	    Global.pref.log("Fetched " + wayPoint);
	} catch (final Exception ex) {
	    completeWebPage = "";
	    Global.pref.log("[getCacheCoordinates] Could not fetch " + wayPoint, ex);
	}
	localInfB.close(0);
	loggedIn = false; // check again login on next spider
	try {
	    return getLatLon(completeWebPage);
	} catch (final Exception ex) {
	    return "????";
	}

    } // getCacheCoordinates

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
	    if (Global.pref.userID.length() == 0) {
		if (gcLogin()) {
		    UrlFetcher.rememberCookies();
		    Global.pref.userID = UrlFetcher.getCookie("userid;www.geocaching.com");
		    if (Global.pref.userID == null) {
			Global.pref.userID = "";
			new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5524, "Please correct your account in preferences\n\n see http://cachewolf.aldos.de/userid.html !")).wait(FormBase.OKB);
			return false;
		    }
		} else {
		    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5525, "Perhaps GC is not available. This should not happen!")).wait(FormBase.OKB);
		    return false;
		}
	    }

	    if (Global.pref.userID.length() > 0) {
		// we have a saved userID (perhaps invalid)
		switch (checkGCSettings()) {
		case 0:
		    loggedIn = true;
		    Global.pref.userID = UrlFetcher.getCookie("userid;www.geocaching.com");
		    Global.pref.savePreferences();
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
		    Global.pref.userID = "";
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
	UrlFetcher.setCookie("userid;www.geocaching.com", Global.pref.userID);
	try {
	    page = UrlFetcher.fetch(url); // getting the sessionid
	} catch (final Exception ex) {
	    Global.pref.log("[checkGCSettings]:Exception calling " + url + " with userID " + Global.pref.userID, ex);
	    return 2;
	}

	UrlFetcher.rememberCookies();
	String SessionId = UrlFetcher.getCookie("ASP.NET_SessionId;www.geocaching.com");
	if (SessionId == null) {
	    Global.pref.log("[checkGCSettings]:got no SessionID.");
	    return 5;
	}

	Extractor ext = new Extractor();
	Extractor extValue = new Extractor();

	//1.) http://www.geocaching.com/my/
	//<a href="http://www.geocaching.com/my/" class="CommonUsername" title="arbor95" target="_self">arbor95</a>	
	String userBlock = ext.set(page, "http://www.geocaching.com/my/", "</a>", 0, true).findNext();
	String loggedInAs = extValue.set(userBlock, "title=\"", "\"", 0, true).findNext();
	Global.pref.log("[checkGCSettings]:loggedInAs= " + loggedInAs, null);
	if (loggedInAs.length() == 0)
	    return 6;

	//2.) ctl00$ContentBody$uxLanguagePreference
	//<select name="ctl00$ContentBody$uxLanguagePreference" id="ctl00_ContentBody_uxLanguagePreference" class="Select">
	//<option selected="selected" value="en-US">English</option>
	//oder
	//<option selected=\"selected\" value=\"de-DE">Deutsch</option>	
	String languageBlock = ext.findNext("ctl00$ContentBody$uxLanguagePreference", "</select>");
	String oldLanguage = extValue.set(languageBlock, "<option selected=\"selected\" value=\"", "\">", 0, true).findNext();
	Global.pref.log("[checkGCSettings]:Language= " + oldLanguage, null);

	//3.) ctl00$ContentBody$uxTimeZone

	//4.) ctl00_ContentBody_uxDisplayUnits
	//<table id="ctl00_ContentBody_uxDisplayUnits" .. </table>
	//<input id="ctl00_ContentBody_uxDisplayUnits_1" type="radio" name="ctl00$ContentBody$uxDisplayUnits" value="1" checked="checked" /><label for="ctl00_ContentBody_uxDisplayUnits_1">Metric</label>
	String distanceUnitBlock = ext.findNext("ctl00_ContentBody_uxDisplayUnits", "</table>");
	String distanceUnit = extValue.set(extValue.set(distanceUnitBlock, "\"checked\"", "</td>", 0, true).findNext(), "\">", "</label>", 0, true).findNext();
	Global.pref.log("[checkGCSettings]:Units= " + distanceUnit, null);
	if (!distanceUnit.equalsIgnoreCase(Global.pref.metricSystem == Metrics.METRIC ? "Metric" : "Imperial")) {
	    return 3;
	}

	//5.) ctl00$ContentBody$uxDateTimeFormat
	//<select name="ctl00$ContentBody$uxDateTimeFormat" id="ctl00_ContentBody_uxDateTimeFormat" class="Select">
	//<option selected="selected" value="yyyy-MM-dd"> 2013-12-04</option>
	String GCDateFormatBlock = ext.findNext("ctl00$ContentBody$uxDateTimeFormat", "</select>");
	String GCDateFormat = extValue.set(GCDateFormatBlock, "selected\" value=\"", "\">", 0, true).findNext();
	Global.pref.log("[checkGCSettings]:GCDateFormat= " + GCDateFormat, null);
	DateFormat.GCDateFormat = GCDateFormat;

	//6.)ctl00$ContentBody$uxInstantMessengerProvider

	//7.) ctl00$ContentBody$ddlGPXVersion

	if (oldLanguage.equals("en-US")) {
	    Global.pref.changedGCLanguageToEnglish = false;
	    return 0;
	} else {
	    Global.pref.oldGCLanguage = oldLanguage;
	    if (setGCLanguage("en-US")) {
		Global.pref.changedGCLanguageToEnglish = true;
		return 0;
	    } else {
		Global.pref.changedGCLanguageToEnglish = false;
		return 1;
	    }
	}

	/*
	// other place to check/set selected language
	String languageBlock = ext.set(page, "\"selected-language\"", "</div>", 0, true).findNext();
	String oldLanguage = ext.set(languageBlock, "<a href=\"#\">", "&#9660;</a>", 0, true).findNext();
	if (oldLanguage.equals("English")) {
	    Global.pref.switchGCLanguageToEnglish = false;
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
	    Global.pref.log("[recentlyviewedcaches]:Exception", ex, true);
	    return false;
	}
	Extractor ext = new Extractor();
	String viewstate = ext.set(page, "id=\"__VIEWSTATE\" value=\"", "\" />", 0, true).findNext();
	String viewstate1 = ext.findNext("id=\"__VIEWSTATE1\" value=\"");
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
	    Global.pref.log("[setGCLanguage] Exception", ex);
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
        Global.pref.log(page, null);
    } catch (final Exception ex) {
        Global.pref.log("[checkGCSettings] Error at post checkGCSettings", ex);
        return 1;
    }	
    }
    */

    private boolean gcLogin() {

	// Get password 
	String passwort = Global.pref.password;
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
	    Global.pref.log("[gcLogin]:Exception gc.com login page", ex, true);
	    return false;
	}
	Extractor ext = new Extractor();
	String viewstate = ext.set(page, "id=\"__VIEWSTATE\" value=\"", "\" />", 0, true).findNext();
	final String postData = "__EVENTTARGET=" //
		+ "&" + "__EVENTARGUMENT="//
		+ "&" + "__VIEWSTATEFIELDCOUNT=1" //
		+ "&" + "__VIEWSTATE=" + UrlFetcher.encodeURL(viewstate, false) //
		+ "&" + "ctl00%24ContentBody%24tbUsername=" + encodeUTF8URL(Utils.encodeJavaUtf8String(Global.pref.myAlias)) //
		+ "&" + "ctl00%24ContentBody%24tbPassword=" + encodeUTF8URL(Utils.encodeJavaUtf8String(passwort)) //
		+ "&" + "ctl00%24ContentBody%24cbRememberMe=" + "true" //
		+ "&" + "ctl00%24ContentBody%24btnSignIn=" + "Login" //
	;
	try {
	    UrlFetcher.setpostData(postData);
	    page = UrlFetcher.fetch(loginPageUrl);
	} catch (final Exception ex) {
	    Global.pref.log("[gcLogin] Exception", ex);
	    return false;
	}
	return true;
    }

    private static boolean stillLoggedIn(String page) {
	if (!(page.indexOf("ctl00_hlSignOut") > -1)) {
	    if (!(page.indexOf("ctl00_ContentLogin_uxLoginStatus_uxLoginURL") > -1)) {
		Global.pref.log(page);
		return false;
	    }
	}
	return true;
    }

    /*
     * 
     */
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
	    RexCacheType = new Regex(p.getProp("cacheTypeRex"));
	} catch (final Exception ex) {
	    Global.pref.log("Error fetching Properties.", ex);
	}
    }

    /*
     * 
     */
    private void getFirstListPage(int distance) {
	String url = makeUrl(distance);

	try {
	    htmlListPage = UrlFetcher.fetch(url);
	    Global.pref.log("[getFirstListPage] Got first page " + url);
	} catch (final Exception ex) {
	    Global.pref.log("[getFirstListPage] Error fetching first list page " + url, ex, true);
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
	    Global.pref.log("[SpiderGC.java:getAListPage] check rexViewstate!", null);
	}

	final Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
	String viewstate1;
	rexViewstate1.search(htmlListPage);
	if (rexViewstate1.didMatch()) {
	    viewstate1 = rexViewstate1.stringMatched(1);
	} else {
	    viewstate1 = "";
	    Global.pref.log("[SpiderGC.java:getAListPage] check rexViewstate1!", null);
	}

	final String postData = "__EVENTTARGET=" + URL.encodeURL(whatPage, false) //
		+ "&" + "__EVENTARGUMENT=" //
		+ "&" + "__VIEWSTATEFIELDCOUNT=2" //
		+ "&" + "__VIEWSTATE=" + URL.encodeURL(viewstate, false) //
		+ "&" + "__VIEWSTATE1=" + URL.encodeURL(viewstate1, false);
	try {
	    UrlFetcher.setpostData(postData);
	    htmlListPage = UrlFetcher.fetch(url);
	    Global.pref.log("[getAListPage] " + whatPage);
	} catch (final Exception ex) {
	    Global.pref.log("[getAListPage] Error at " + whatPage, ex);
	    ret = false;
	}
	return ret;
    }

    private String makeUrl(int distance) {
	String url;
	if (spiderAllFinds) {
	    url = urlSeek //
		    + queryUserFinds + encodeUTF8URL(Utils.encodeJavaUtf8String(Global.pref.myAlias));
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
     *            A previously fetched cachepage
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
	if (Global.pref.checkDTS) {
	    final String dts[] = mString.split(getDTS(CacheDescription), '/');
	    if (dts.length == 3) {
		if (difficultyChanged(ch, CacheTerrDiff.v1Converter(dts[0]))) {
		    save = true;
		    ret = true;
		    Global.pref.log("difficultyChanged");
		}
		if (terrainChanged(ch, CacheTerrDiff.v1Converter(dts[1]))) {
		    save = true;
		    ret = true;
		    Global.pref.log("terrainChanged");
		}
		if (sizeChanged(ch, CacheSize.gcGpxString2Cw(dts[2]))) {
		    save = true;
		    ret = true;
		    Global.pref.log("sizeChanged");
		}
	    } else {
		try {
		    Global.pref.log("[SpiderGC.java:updateExists]check DTS calculation (DTSRex)! \n" + CacheDescription, null);
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
	    Global.pref.log("[SpiderGC.java:getNumFound]check RexNumFinds!", null);
	    return 0;
	}
    }

    private int getFoundInDB() {
	CacheHolder ch;
	int counter = 0;
	for (int i = 0; i < cacheDB.size(); i++) {
	    ch = cacheDB.get(i);
	    if (ch.is_found()) {
		if (ch.getWayPoint().startsWith("GC"))
		    counter++;
	    }
	}
	return counter;
    }

    private double[] getDistanceAndDirection(String doc) {
	// #<span class="small NoWrap"><img src="/images/icons/compass/SW.gif" alt="SW" title="SW" />SW<br />0.31km</span>
	// DistDirRex = compass/(.*?)\.gif(.*?)<br />(.*?)(?:km|mi|ft)
	final double[] distanceAndDirection = { (0.0), (0.0) };
	if (spiderAllFinds)
	    return distanceAndDirection;
	// <span class="small NoWrap"><br />Here</span>
	if (doc.indexOf(">Here<") > 0)
	    return distanceAndDirection;
	String stmp;
	DistDirRex.search(doc);
	if (!DistDirRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getDistanceAndDirection]check DistDirRex!", null);
	    distanceAndDirection[0] = -1.0; // Abbruch
	    return distanceAndDirection;
	}
	stmp = DistDirRex.stringMatched(3);
	distanceAndDirection[0] = Common.parseDouble(stmp);
	stmp = DistDirRex.stringMatched(1);
	if (stmp.equals("N"))
	    distanceAndDirection[1] = 0.0;
	else if (stmp.equals("NE"))
	    distanceAndDirection[1] = 45.0;
	else if (stmp.equals("E"))
	    distanceAndDirection[1] = 90.0;
	else if (stmp.equals("SE"))
	    distanceAndDirection[1] = 135.0;
	else if (stmp.equals("S"))
	    distanceAndDirection[1] = 180.0;
	else if (stmp.equals("SW"))
	    distanceAndDirection[1] = 225.0;
	else if (stmp.equals("W"))
	    distanceAndDirection[1] = 270.0;
	else if (stmp.equals("NW"))
	    distanceAndDirection[1] = 315.0;
	else
	    distanceAndDirection[1] = 0.0;

	return distanceAndDirection;
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
	    Global.pref.log("[SpiderGC.java:getWP]check waypointRex!", null);
	    return "???";
	}
	String stmp = RexPropWaypoint.stringMatched(1);
	return "GC" + stmp;
    }

    /**
     * check for Premium Member Cache
     */
    private boolean doPMCache(String waypoint, String toCheck) {
	if (Global.pref.isPremium)
	    return true;
	if (toCheck.indexOf(propPM) <= 0) {
	    return true;
	} else {
	    numPrivate = numPrivate + 1;
	    // if (spiderAllFinds) {
	    Global.pref.log(waypoint + " is private.", null);
	    // }
	    /*
	    CacheHolder ch = cacheDB.get(waypoint);
	    if (ch == null) {
	    ch = new CacheHolder(waypoint);
	    cacheDB.add(ch);
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
	Global.pref.log("[SpiderGC.java:typeChanged]check TypeRex!", null);
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
     * if cache lies in the desired direction
     */
    private boolean directionOK(String[] directions, double toCheck) {
	if (directions == null || directions.length == 0)
	    return true; // nothing means all
	final int lowerLimit = Common.parseInt(directions[0]);
	final int upperLimit = Common.parseInt(directions[1]);
	if (lowerLimit <= upperLimit) {
	    if ((toCheck >= lowerLimit) && (toCheck <= upperLimit)) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    if ((toCheck >= lowerLimit) || (toCheck <= upperLimit)) {
		return true;
	    } else {
		return false;
	    }
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
	if (!Global.pref.checkLog)
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
	    Global.pref.log("[SpiderGC.java:newFoundExists]check logDateRex!", null);
	    return false;
	}
	final Time lastLogGC = DateFormat.toDate(stmp);
	// String timecheck = DateFormat.toYYMMDD(lastLogGC);
	final boolean ret = lastUpdateCW.compareTo(lastLogGC) < 0;
	return ret;
    }

    private boolean TBchanged(CacheHolder ch, String cacheDescription) {
	// simplified Version: only presence is checked
	if (Global.pref.downloadTBs && Global.pref.checkTBs) {
	    final boolean hasTB = cacheDescription.indexOf("data-tbcount") > -1;
	    return ch.has_bugs() != (hasTB);
	}
	return false;
    }

    /**
     * Read a complete cachepage from geocaching.com including all logs. This is used both when updating already existing caches (via spiderSingle) and when spidering around a centre. It is also used when reading a GPX file and fetching the images.
     * 
     * This is the workhorse function of the spider.
     * 
     * @param CacheHolderDetail
     *            chD The element wayPoint must be set to the name of a waypoint
     * @param boolean isUpdate True if an existing cache is being updated, false if it is a new cache
     * @param boolean fetchImages True if the pictures are to be fetched
     * @param boolean fetchTBs True if the TBs are to be fetched
     * @param boolean doNotGetFound True if the cache is not to be spidered if it has already been found
     * @return -1 if the infoBox was closed (cancel spidering), 0 if there was an error (continue with next cache), 1 if everything ok
     */
    private int getCacheByWaypointName(CacheHolder ch, boolean isUpdate, boolean fetchImages, boolean fetchTBs, boolean doNotGetFound) {
	int ret = SPIDER_OK; // initialize value;

	while (true) { // retry even if failure
	    Global.pref.log(""); // new line for more overview
	    String completeWebPage;
	    int spiderTrys = 0;
	    final int MAX_SPIDER_TRYS = 3;
	    while (spiderTrys++ < MAX_SPIDER_TRYS) {
		ret = SPIDER_OK; // initialize value;
		try {
		    final String url = wayPointUrl + ch.getWayPoint();
		    completeWebPage = UrlFetcher.fetch(url);
		    Global.pref.log("Fetched: " + ch.getWayPoint());
		} catch (final Exception ex) {
		    Global.pref.log("Could not fetch " + ch.getWayPoint(), ex);
		    if (!infB.isClosed()) {
			continue;
		    } else {
			ch.setIncomplete(true);
			return SPIDER_CANCEL;
		    }
		}
		// Only analyse the cache data and fetch pictures if user has not closed the progress window
		if (!infB.isClosed()) {
		    try {
			ch.initStates(!isUpdate);

			// first check if coordinates are available to prevent deleting existing coordinates
			final String latLon = getLatLon(completeWebPage);
			if (latLon.equals("???")) {
			    if (completeWebPage.indexOf(p.getProp("premiumCachepage")) > 0) {
				// Premium cache spidered by non premium member
				Global.pref.log("Ignoring premium member cache: " + ch.getWayPoint(), null);
				spiderTrys = MAX_SPIDER_TRYS;
				ret = SPIDER_IGNORE_PREMIUM;
				continue;
			    } else {
				if (spiderTrys == MAX_SPIDER_TRYS)
				    Global.pref.log(">>>> Failed to spider Cache. Retry.", null);
				ret = SPIDER_ERROR;
				continue; // Restart the spider
			    }
			}

			ch.setHTML(true);
			ch.setIncomplete(true);
			// Save size of logs to be able to check whether any new logs were added
			// int logsz = chD.CacheLogs.size();
			// chD.CacheLogs.clear();
			ch.addiWpts.clear();
			ch.getCacheDetails(false).images.clear();

			ch.setAvailable(!(completeWebPage.indexOf(p.getProp("cacheUnavailable")) > -1));
			ch.setArchived(completeWebPage.indexOf(p.getProp("cacheArchived")) > -1);

			// Logs
			boolean foundByMe = false;
			if (completeWebPage.indexOf("ctl00_ContentBody_GeoNav_logText") > -1) {
			    foundByMe = true;
			    // If the switch is set to not store found caches and we found the cache => return
			    if (doNotGetFound)
				return SPIDER_IGNORE;
			}
			RexUserToken.search(completeWebPage);
			if (!RexUserToken.didMatch()) {
			    Global.pref.log("[SpiderGC.java:getLogs]check RexUserToken!", null);
			}
			final String userToken = RexUserToken.stringMatched(1);
			getLogs(userToken, ch, foundByMe);
			Global.pref.log("Got logs");

			// General Cache Data
			ch.setPos(new CWPoint(latLon));
			Global.pref.log("LatLon: " + ch.getPos().toString());

			final String longDesc = getLongDesc(completeWebPage);
			ch.getCacheDetails(false).setLongDescription(longDesc);
			Global.pref.log("Got description");

			ch.setCacheName(SafeXML.cleanback(getName(completeWebPage)));
			Global.pref.log("Name: " + ch.getCacheName());

			final String location = getLocation(completeWebPage);
			if (location.length() != 0) {
			    final int countryStart = location.indexOf(",");
			    if (countryStart > -1) {
				ch.getCacheDetails(false).Country = SafeXML.cleanback(location.substring(countryStart + 1).trim());
				ch.getCacheDetails(false).State = SafeXML.cleanback(location.substring(0, countryStart).trim());
			    } else {
				ch.getCacheDetails(false).Country = location.trim();
				ch.getCacheDetails(false).State = "";
			    }
			    Global.pref.log("Got (country/state)" + ch.getCacheDetails(false).Country + "/" + ch.getCacheDetails(false).State);
			} else {
			    ch.getCacheDetails(false).Country = "";
			    ch.getCacheDetails(false).State = "";
			    Global.pref.log("No location (country/state) found");
			}
			String owner = getOwner(completeWebPage);
			owner = SafeXML.cleanback(owner).trim();
			ch.setCacheOwner(owner);
			if (ch.getCacheOwner().equalsIgnoreCase(Global.pref.myAlias) || (ch.getCacheOwner().equalsIgnoreCase(Global.pref.myAlias2)))
			    ch.setOwned(true);
			Global.pref.log("Owner: " + ch.getCacheOwner() + "; is_owned = " + ch.is_owned() + ";  alias1,2 = [" + Global.pref.myAlias + "|" + Global.pref.myAlias2 + "]");

			ch.setDateHidden(DateFormat.toYYMMDD(getDateHidden(completeWebPage)));
			Global.pref.log("Hidden: " + ch.getDateHidden());

			ch.getCacheDetails(false).setHints(getHints(completeWebPage));
			Global.pref.log("Hints: " + ch.getCacheDetails(false).Hints);

			ch.setCacheSize(CacheSize.gcSpiderString2Cw(getSize(completeWebPage)));
			Global.pref.log("Size: " + ch.getCacheSize());

			ch.setHard(CacheTerrDiff.v1Converter(getDiff(completeWebPage)));
			Global.pref.log("Hard: " + ch.getHard());

			ch.setTerrain(CacheTerrDiff.v1Converter(getTerr(completeWebPage)));
			Global.pref.log("Terr: " + ch.getTerrain());

			ch.setType(getType(completeWebPage));
			Global.pref.log("Type: " + ch.getType());
			// ==========
			// Bugs
			// ==========
			if (fetchTBs)
			    getBugs(ch.getCacheDetails(false), completeWebPage);
			ch.setHas_bugs(ch.getCacheDetails(false).Travelbugs.size() > 0);
			Global.pref.log("Got TBs");
			// ==========
			// Images
			// ==========
			if (fetchImages) {
			    getImages(completeWebPage, ch.getCacheDetails(false), true);
			    Global.pref.log("Got images");
			}
			// ==========
			// Addi waypoints
			// ==========
			getAddWaypoints(completeWebPage, ch.getWayPoint(), ch.is_found());
			Global.pref.log("Got additional waypoints");
			// ==========
			// Attributes
			// ==========
			getAttributes(completeWebPage, ch.getCacheDetails(false));
			Global.pref.log("Got attributes");
			// ==========
			// Last sync date
			// ==========
			ch.setLastSync((new Time()).format("yyyyMMddHHmmss"));
			ch.setIncomplete(false);
			Global.pref.log("ready " + ch.getWayPoint() + " : " + ch.getLastSync());
			break;
		    } catch (final Exception ex) {
			Global.pref.log("[getCacheByWaypointName: ]Error reading cache: " + ch.getWayPoint(), ex);
		    }
		} else {
		    break;
		}
	    } // spiderTrys
	    if ((spiderTrys >= MAX_SPIDER_TRYS) && (ret == SPIDER_OK)) {
		Global.pref.log(">>> Failed to spider cache. Number of retrys exhausted.", null);
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
	}// while(true) // retry even if failure
	if (infB.isClosed()) {
	    // If the infoBox was closed before getting here, we return -1
	    return SPIDER_CANCEL;
	}
	return ret;
    } // getCacheByWaypointName

    /**
     * Get the coordinates of the cache
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Cache coordinates
     */
    private String getLatLon(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("latLonRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    if (doc.indexOf("Unpublished Geocache") < 0 && doc.indexOf("Premium Member Only Cache") < 0) {
		Global.pref.log("[SpiderGC.java:getLatLon]check latLonRex!" + doc, null);
	    }
	    return "???";
	}
	return inRex.stringMatched(1);
    }

    boolean shortDescRex_not_yet_found = true;

    /**
     * Get the long description
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return the long description
     */
    private String getLongDesc(String doc) throws Exception {
	String res = "";
	final Regex shortDescRex = new Regex(p.getProp("shortDescRex"));
	final Regex longDescRex = new Regex(p.getProp("longDescRex"));
	shortDescRex.search(doc);
	if (!shortDescRex.didMatch()) {
	    if (shortDescRex_not_yet_found)
		Global.pref.log("[SpiderGC.java:getLongDesc]no shortDesc or check shortDescRex!", null);
	    // + Preferences.NEWLINE + doc);
	} else {
	    res = shortDescRex.stringMatched(1);
	    shortDescRex_not_yet_found = false;
	}
	res += "<br>";
	longDescRex.search(doc);
	if (!longDescRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getLongDesc]check longDescRex!", null);
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

    /**
     * Get the cache location (country and state)
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return the location (country and state) of the cache
     */
    private String getLocation(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("cacheLocationRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getLocation]check cacheLocationRex!", null);
	    return "";
	}
	String res2 = inRex.stringMatched(2);
	return res2;
    }

    /**
     * Get the cache name
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return the name of the cache
     */
    private String getName(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("cacheNameRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getName]check cacheNameRex!", null);
	    return "???";
	}
	return inRex.stringMatched(1);
    }

    /**
     * Get the cache owner
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return the cache owner
     */
    private String getOwner(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("cacheOwnerRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getOwner]check cacheOwnerRex!", null);
	    return "???";
	}
	return inRex.stringMatched(1);
    }

    /**
     * Get the date when the cache was hidden
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Hidden date
     */
    private String getDateHidden(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("dateHiddenRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getDateHidden]check dateHiddenRex!", null);
	    return "???";
	}
	return inRex.stringMatched(1);
    }

    /**
     * Get the hints
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Cachehints
     */
    private String getHints(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("hintsRex"));
	inRex.search(doc);
	if (!inRex.didMatch()) {
	    Global.pref.log("[SpiderGC.java:getHints]check hintsRex!", null);
	    return "";
	}
	return inRex.stringMatched(1);
    }

    /**
     * Get the cache size
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Cache size
     */
    private String getSize(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("sizeRex"));
	inRex.search(doc);
	if (inRex.didMatch())
	    return inRex.stringMatched(1);
	else {
	    Global.pref.log("[SpiderGC.java:getSize]check sizeRex!", null);
	    return "None";
	}
    }

    /**
     * Get the Difficulty
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return The cache difficulty
     */
    private String getDiff(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("difficultyRex"));
	inRex.search(doc);
	if (inRex.didMatch())
	    return inRex.stringMatched(1);
	else {
	    Global.pref.log("[SpiderGC.java:getDiff]check difficultyRex!", null);
	    return "-1";
	}
    }

    /**
     * Get the terrain rating
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return Terrain rating
     */
    private String getTerr(String doc) throws Exception {
	final Regex inRex = new Regex(p.getProp("terrainRex"));
	inRex.search(doc);
	if (inRex.didMatch())
	    return inRex.stringMatched(1);
	else {
	    Global.pref.log("[SpiderGC.java:getTerr]check terrainRex!", null);
	    return "-1";
	}
    }

    /**
     * Get the waypoint type
     * 
     * @param doc
     *            A previously fetched cachepage
     * @return the waypoint type (Tradi, Multi, etc.)
     */
    private byte getType(String doc) {
	RexCacheType.search(doc);
	if (RexCacheType.didMatch())
	    return CacheType.gcSpider2CwType(RexCacheType.stringMatched(1));
	else {
	    Global.pref.log("[SpiderGC.java:getType]check cacheTypeRex!", null);
	    return 0;
	}
    }

    /**
     * Get the logs
     */
    private void getLogs(String userToken, CacheHolder ch, boolean foundByMe) throws Exception {
	final CacheHolderDetail chD = ch.getCacheDetails(false);
	final LogList reslts = chD.CacheLogs;
	reslts.clear();

	int idx = 0;
	int nLogs = 0;
	boolean foundown = false;
	boolean fertig = false;
	int num = 100;

	if (!foundByMe) {
	    if (Global.pref.maxLogsToSpider < 100)
		num = Global.pref.maxLogsToSpider + 1;
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
		Global.pref.log("Error getting Logs. \r\n" + fetchResult, e);
		return;
	    }
	    if (!resp.getString("status").equals("success")) {
		Global.pref.log("status is " + resp.getString("status"));
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
		if ((icon.equals(icon_smile) || icon.equals(icon_camera) || icon.equals(icon_attended)) && (name.equalsIgnoreCase(Global.pref.myAlias) || (name.equalsIgnoreCase(Global.pref.myAlias2)))) {
		    ch.setFound(true);
		    ch.setCacheStatus(d);
		    // final String logId = entry.getString("LogID");
		    chD.OwnLogId = entry.getString("LogID");
		    chD.OwnLog = new Log(icon, d, name, logText);
		    foundown = true;
		    reslts.add(new Log(icon, d, name, logText));
		}
		if (nLogs <= Global.pref.maxLogsToSpider) {
		    reslts.add(new Log(icon, d, name, logText));
		} else {
		    if (foundown || !foundByMe) {
			fertig = true;
			break;
		    }
		}
	    }
	} while (!fertig);

	if (nLogs > Global.pref.maxLogsToSpider) {
	    // there are more logs
	    reslts.add(Log.maxLog());
	}
	// Bei Update ev. doppelt berechnet
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
     *            The previously fetched cachepage
     * @return A HTML formatted string with bug names and there purpose
     */
    public void getBugs(CacheHolderDetail chD, String doc) throws Exception {
	chD.Travelbugs.clear();
	if (doc.indexOf("ctl00_ContentBody_uxTravelBugList_uxNoTrackableItemsLabel") >= 0) {
	    return; // there are no trackables
	}
	final Extractor exBlock = new Extractor(doc, p.getProp("blockExStart"), p.getProp("blockExEnd"), 0, Extractor.EXCLUDESTARTEND);
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
		    Global.pref.log("[SpiderGC.java:getBugs]check TBs bugLinkEnd!", null);
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
			Global.pref.log("[SpiderGC.java:getBugs] Could not fetch buginfo from " + link, ex);
		    }
		}
	    }
	    infB.setInfo(oldInfoBox);
	    if (exBugWrong) {
		if (blockLength > 200)
		    Global.pref.log("[SpiderGC.java:getBugs]check TBs bugExStart / bugExEnd! blockLength = " + blockLength + " for " + chD.URL, null);
	    }
	} else {
	    Global.pref.log("[SpiderGC.java:getBugs]check TBs blockExStart / blockExEnd! ", null);
	}
    }

    /**
     * Get the images for a previously fetched cache page. Images are extracted from two areas: The long description and the pictures section (including the spoiler)
     * 
     * @param doc
     *            The previously fetched cachepage
     * @param chD
     *            The Cachedetails
     */
    public void getImages(String doc, CacheHolderDetail chD, boolean extractLongDesc) {
	int imgCounter = 0;
	int spiderCounter = 0;
	String fileName, imgName, imgType, imgUrl, imgComment;
	final Vector spideredUrls = new Vector();
	ImageInfo imageInfo = null;
	Extractor exImgBlock;
	int idxUrl; // Index of already spidered Url in list of spideredUrls
	CacheImages lastImages = null;

	// First: Get current image object of waypoint before spidering images.
	final CacheHolder oldCh = Global.profile.cacheDB.get(chD.getParent().getWayPoint());
	if (oldCh != null) {
	    lastImages = oldCh.getCacheDetails(false).images;
	}
	// ========
	// In the long Description
	// ========
	String longDesc = doc;
	try {
	    if (chD.getParent().getWayPoint().startsWith("GC") || extractLongDesc)
		longDesc = getLongDesc(doc);
	    longDesc = STRreplace.replace(longDesc, "<img", "<IMG");
	    longDesc = STRreplace.replace(longDesc, "src=", "SRC=");
	    longDesc = STRreplace.replace(longDesc, "'", "\"");
	    exImgBlock = new Extractor(longDesc, p.getProp("imgBlockExStart"), p.getProp("imgBlockExEnd"), 0, false);
	} catch (final Exception ex) {// Missing property in spider.def
	    return;
	}
	String tst;
	Extractor exImgSrc = new Extractor("", "http://", "\"", 0, true);
	while ((tst = exImgBlock.findNext()).length() > 0) {
	    // Optimize: img.groundspeak.com -> img.geocaching.com (for better caching purposes)
	    imgUrl = exImgSrc.findFirst(tst);
	    imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
	    try {
		imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
		// imgType is now max 4 chars, starting with .
		if (imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")) {
		    // Check whether image was already spidered for this cache
		    idxUrl = spideredUrls.find(imgUrl);
		    imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
		    imageInfo = null;
		    if (idxUrl < 0) { // New image
			fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(spiderCounter);
			if (lastImages != null) {
			    imageInfo = lastImages.needsSpidering(imgUrl, fileName + imgType);
			}
			if (imageInfo == null) {
			    imageInfo = new ImageInfo();
			    Global.pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
			    spiderImage(imgUrl, fileName + imgType);
			    imageInfo.setFilename(fileName + imgType);
			    imageInfo.setURL(imgUrl);
			} else {
			    Global.pref.log("[getImages] Already existing image: " + imgUrl + " as " + imageInfo.getFilename());
			}
			spideredUrls.add(imgUrl);
			spiderCounter++;
		    } else { // Image already spidered as wayPoint_'idxUrl'
			fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(idxUrl);
			Global.pref.log("[getImages] Already loaded image: " + imgUrl + " as " + fileName + imgType);
			imageInfo = new ImageInfo();
			imageInfo.setFilename(fileName + imgType);
			imageInfo.setURL(imgUrl);
		    }
		    imageInfo.setTitle(imgName);
		    imageInfo.setComment(null);
		    imgCounter++;
		    chD.images.add(imageInfo);
		}
	    } catch (final IndexOutOfBoundsException e) {
		Global.pref.log("[getImages] Problem loading image. imgURL:" + imgUrl, e);
	    }
	}
	// ========
	// In the image span
	// ========
	Extractor spanBlock;
	String imgSrcExStart, imgSrcExEnd;
	String imgNameExStart, imgNameExEnd;
	String imgCommentExStart, imgCommentExEnd;
	try {
	    imgSrcExStart = p.getProp("imgSrcExStart");
	    imgSrcExEnd = p.getProp("imgSrcExEnd");
	    imgNameExStart = p.getProp("imgNameExStart");
	    imgNameExEnd = p.getProp("imgNameExEnd");
	    imgCommentExStart = p.getProp("imgCommentExStart");
	    imgCommentExEnd = p.getProp("imgCommentExEnd");
	    spanBlock = new Extractor(doc, p.getProp("imgSpanExStart"), p.getProp("imgSpanExEnd"), 0, false);
	    spanBlock.set(spanBlock.findNext(), p.getProp("imgSpanExStart2"), p.getProp("imgSpanExEnd"), 0, true);
	    spanBlock.set(spanBlock.findNext() + imgSrcExStart, imgSrcExStart, imgSrcExStart, 0, false);
	} catch (final Exception ex) {
	    return;
	}
	while ((tst = spanBlock.findNext()).length() > 0) {
	    exImgSrc.set(tst, imgSrcExStart, imgSrcExEnd, 0, true);
	    imgUrl = "http://" + exImgSrc.findNext();
	    try {
		// links to images in the description directs to one which has reduced in their size.
		// We like to load the images in their original size:
		if (imgUrl.startsWith("http://img.geocaching.com/cache/display")) {
		    imgUrl = "http://img.geocaching.com/cache" + imgUrl.substring("http://img.geocaching.com/cache/display".length());
		}
		// end

		imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
		// imgType is now max 4 chars, starting with .
		if (imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")) {
		    // Check whether image was already spidered for this cache
		    idxUrl = spideredUrls.find(imgUrl);
		    imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
		    imageInfo = null;
		    if (idxUrl < 0) { // New image
			fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(spiderCounter);
			if (lastImages != null) {
			    imageInfo = lastImages.needsSpidering(imgUrl, fileName + imgType);
			}
			if (imageInfo == null) {
			    imageInfo = new ImageInfo();
			    Global.pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
			    spiderImage(imgUrl, fileName + imgType);
			    imageInfo.setFilename(fileName + imgType);
			    imageInfo.setURL(imgUrl);
			} else {
			    Global.pref.log("[getImages] Already exising image: " + imgUrl + " as " + imageInfo.getFilename());
			}
			spideredUrls.add(imgUrl);
			spiderCounter++;
		    } else { // Image already spidered as wayPoint_'idxUrl'
			fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(idxUrl);
			Global.pref.log("[getImages] Already loaded image: " + imgUrl + " as " + fileName + imgType);
			imageInfo = new ImageInfo();
			imageInfo.setFilename(fileName + imgType);
			imageInfo.setURL(imgUrl);
		    }
		    imageInfo.setTitle(exImgSrc.findNext(imgNameExStart, imgNameExEnd));
		    imgComment = exImgSrc.findNext(imgCommentExStart, imgCommentExEnd);
		    while (imgComment.startsWith("<br />"))
			imgComment = imgComment.substring(6);
		    while (imgComment.endsWith("<br />"))
			imgComment = imgComment.substring(0, imgComment.length() - 6);
		    imageInfo.setComment(imgComment);
		    chD.images.add(imageInfo);
		}
	    } catch (final IndexOutOfBoundsException e) {
		Global.pref.log("[getImages] IndexOutOfBoundsException in image span. imgURL:" + imgUrl, e);
	    }

	}
	// ========
	// Final sweep to check for images in hrefs
	// ========
	final Extractor exFinal = new Extractor(longDesc, "http://", "\"", 0, true);
	while ((imgUrl = exFinal.findNext()).length() > 0) {
	    // Optimize: img.groundspeak.com -> img.geocaching.com (for better caching purposes)
	    imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
	    try {
		imgType = (imgUrl.substring(imgUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
		// imgType is now max 4 chars, starting with .
		// Delete characters in URL after the image extension
		imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
		if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
		    // Check whether image was already spidered for this cache
		    idxUrl = spideredUrls.find(imgUrl);
		    if (idxUrl < 0) { // New image
			imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
			fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(spiderCounter);
			if (lastImages != null) {
			    imageInfo = lastImages.needsSpidering(imgUrl, fileName + imgType);
			}
			if (imageInfo == null) {
			    imageInfo = new ImageInfo();
			    Global.pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
			    spiderImage(imgUrl, fileName + imgType);
			    imageInfo.setFilename(fileName + imgType);
			    imageInfo.setURL(imgUrl);
			} else {
			    Global.pref.log("[getImages] Already exising image: " + imgUrl + " as " + imageInfo.getFilename());
			}
			spideredUrls.add(imgUrl);
			spiderCounter++;
			imageInfo.setTitle(imgName);
			imgCounter++;
			chD.images.add(imageInfo);
		    }
		}
	    } catch (final IndexOutOfBoundsException e) {
		Global.pref.log("[getImages] Problem loading image. imgURL:" + imgUrl, e);
	    }

	}
    }

    /**
     * Read an image from the server
     * 
     * @param imgUrl
     *            The Url of the image
     * @param target
     *            The bytes of the image
     */
    private void spiderImage(String address, String fn) {
	try {
	    UrlFetcher.fetchDataFile(address, Global.profile.dataDir + fn);
	} catch (final UnknownHostException e) {
	    Global.pref.log("[spiderImage] Host not there...", e);
	} catch (final IOException ioex) {
	    Global.pref.log("[spiderImage] File not found!", ioex);
	} catch (final Exception ex) {
	    Global.pref.log("[spiderImage] Some other problem while fetching image", ex);
	} finally {
	    // Continue with the spider
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
		    final int idx = Global.profile.getCacheIndex(adWayPoint);
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
			Global.pref.log("check nameRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }

		    koordRex.search(rowBlock);
		    if (koordRex.didMatch()) {
			hd.setPos(new CWPoint(koordRex.stringMatched(1)));
			koords_not_yet_found = false;
		    } else {
			if (koords_not_yet_found) {
			    koords_not_yet_found = false;
			    Global.pref.log("check koordRex in spider.def" + Preferences.NEWLINE + rowBlock);
			}
		    }

		    typeRex.search(rowBlock);
		    if (typeRex.didMatch()) {
			hd.setType(CacheType.gpxType2CwType("Waypoint|" + typeRex.stringMatched(1)));
		    } else {
			Global.pref.log("check typeRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }

		    rowBlock = exRowBlock.findNext();
		    descRex.search(rowBlock);
		    if (descRex.didMatch()) {
			hd.getCacheDetails(false).setLongDescription(descRex.stringMatched(1).trim());
		    } else {
			Global.pref.log("check descRex in spider.def" + Preferences.NEWLINE + rowBlock);
		    }
		    hd.setFound(is_found);
		    hd.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
		    hd.setHard(CacheTerrDiff.CW_DT_UNSET);
		    hd.setTerrain(CacheTerrDiff.CW_DT_UNSET);

		    if (idx < 0) {
			cacheDB.add(hd);
			hd.save();
		    } else {
			final CacheHolder cx = cacheDB.get(idx);
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

    public void getAttributes(String doc, CacheHolderDetail chD) throws Exception {
	final Extractor attBlock = new Extractor(doc, p.getProp("attBlockExStart"), p.getProp("attBlockExEnd"), 0, true);
	final String atts = attBlock.findNext();
	final Extractor attEx = new Extractor(atts, p.getProp("attExStart"), p.getProp("attExEnd"), 0, true);
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
	    Global.pref.log("[getBugId] Fetched bugId: " + name);
	} catch (final Exception ex) {
	    Global.pref.log("[getBugId] Could not fetch bug list" + name, ex);
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
	    Global.pref.log("[getBugId] Error getting TB", ex);
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
	    Global.pref.log("[getBugMissionByGuid] Fetched TB detailsById: " + guid);
	} catch (final Exception ex) {
	    Global.pref.log("[getBugMissionByGuid] Could not fetch TB details " + guid, ex);
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
	    Global.pref.log("[getBugMissionByGuid] Error getting TB " + guid, ex);
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
	    Global.pref.log("[getBugMissionByTrackNr] Fetched bug detailsByTrackNr: " + trackNr);
	} catch (final Exception ex) {
	    Global.pref.log("[getBugMissionByTrackNr] getBugByTrackNr " + trackNr, ex);
	    bugDetails = "";
	}
	try {
	    if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
		Global.pref.log("[getBugMissionByTrackNr], bugNotFound " + trackNr, null);
		return "";
	    }
	    final Extractor exDetails = new Extractor(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND);
	    return exDetails.findNext();
	} catch (final Exception ex) {
	    Global.pref.log("[getBugMissionByTrackNr] TB Details, bugNotFound " + trackNr, ex);
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
	    Global.pref.log("[getBugMissionAndNameByTrackNr] Fetched TB getBugByTrackNr: " + trackNr);
	} catch (final Exception ex) {
	    Global.pref.log("[getBugMissionAndNameByTrackNr] Could not fetch bug details: " + trackNr, ex);
	    bugDetails = "";
	}
	try {
	    if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
		Global.pref.log("[getBugMissionAndNameByTrackNr], bugNotFound: " + trackNr, null);
		return false;
	    }
	    final Extractor exDetails = new Extractor(bugDetails, p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0, Extractor.EXCLUDESTARTEND);
	    TB.setMission(exDetails.findNext());
	    final Extractor exName = new Extractor(bugDetails, p.getProp("bugNameStart"), p.getProp("bugNameEnd"), 0, Extractor.EXCLUDESTARTEND);
	    TB.setName(exName.findNext());
	    return true;
	} catch (final Exception ex) {
	    Global.pref.log("[getBugMissionAndNameByTrackNr] TB Details, bugNotFound: " + trackNr, ex);
	    return false;
	}
    }

    public class SpiderProperties extends Properties {
	SpiderProperties() {
	    super();
	    try {
		load(new FileInputStream(FileBase.getProgramDirectory() + "/spider.def"));
	    } catch (final Exception ex) {
		Global.pref.log("Failed to load spider.def from " + FileBase.getProgramDirectory(), ex);
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
		Global.pref.log("Missing tag in spider.def: " + key);
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
		final TrackPoint tp = new TrackPoint(lat, lon);
		if (tp.isValid())
		    _routePoints.add(tp);
		return;
	    }
	}

	public void endElement(String name) {

	}
    }
}
