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
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheImages;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.DateFormat;
import CacheWolf.Extractor;
import CacheWolf.Global;
import CacheWolf.HttpConnection;
import CacheWolf.ImageInfo;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.LogList;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.STRreplace;
import CacheWolf.SafeXML;
import CacheWolf.Travelbug;
import CacheWolf.navi.Area;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.Track;
import CacheWolf.navi.TrackPoint;
import CacheWolf.navi.TransformCoordinates;

import com.stevesoft.ewe_pat.Regex;

import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.fx.Image;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.net.URL;
import ewe.net.UnknownHostException;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.ByteArray;
import ewe.util.CharArray;
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

	/**
	 * The maximum number of logs that will be stored
	 */
	private static boolean loggedIn = false;

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

	private static int ERR_LOGIN = -10;
	private static Preferences pref;
	private Profile profile;

	private static String cookieID = "";
	private static String cookieSession = "";
	private static double minDistance = 0;
	private static double maxDistance = 0;
	private static String direction = "";
	private static String[] directions;

	private CacheDB cacheDB;
	private Vector cachesToLoad = new Vector();
	private InfoBox infB;
	private static SpiderProperties p = null;
	// following filled at doit
	private CWPoint origin;
	private boolean doNotgetFound;
	private String cacheTypeRestriction;
	private boolean spiderAllFinds;
	private String htmlListPage;
	private int maxUpdate;
	private boolean maxNumberAbort;
	private byte restrictedCacheType = 0;
	private String fileName = "";
	private String userToken = "";

	private static String propFirstPage;
	private static String propFirstPage2;
	private static String propFirstPageFinds;
	private static String gotoNextPage = "ctl00$ContentBody$pgrTop$ctl08";
	private static String gotoNextBlock = "ctl00$ContentBody$pgrTop$ctl06";  // change to the block (10pages) of the wanted page
	private static String gotoPage = "ctl00$ContentBody$pgrTop$lbGoToPage_"; // add pagenumber
	private static String propMaxDistance;
	private static String propShowOnlyFound;
	private static Regex RexPropListBlock;
	private static Regex RexPropLine;
	private static Regex RexNumFinds;
	private static Regex RexPropLogDate;
	private static String propAvailable;
	private static String propArchived;
	private static String propFound;
	private static String propPM;
	private static Regex RexPropDistance;
	private static Regex RexPropDistanceCode;
	private static String DistanceCodeKey;
	private static String DTSCodeKey;
	private static Regex RexPropWaypoint;
	private static Regex RexPropType;
	private static Regex RexPropDTS;
	private static Regex RexPropOwn;
	private static Regex RexLogBlock;
	private static Extractor exSingleLog;
	private static Extractor exIcon;
	private static Extractor exNameTemp;
	private static Extractor exName;
	private static Extractor exDate;
	private static Extractor exLog;
	private static Extractor exLogId;
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
	private int num_added = 0;
	
	private static HttpConnection conn;

	public SpiderGC(Preferences prf, Profile profile, boolean bypass) {
		this.profile = profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		if (p == null) {
			p = new SpiderProperties();
		}
		initialiseProperties();
	}

	/**
	 * Method to start the spider for a search around the centre coordinates
	 */
	public void doIt() {
		doIt(false);
	}

	public void doIt(boolean _spiderAllFinds) {
		cachesToLoad.clear();
		spiderAllFinds = _spiderAllFinds;
		origin = pref.getCurCentrePt(); // No need to copy curCentrePt as it is only read and not written
		if (!spiderAllFinds && !origin.isValid()) {
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5509,"Coordinates for centre must be set"), FormBase.OKB)).execute();
			return;
		}

		// Reset states for all caches when spidering (http://tinyurl.com/dzjh7p)
		for (int i = 0; i < cacheDB.size(); i++) {
			CacheHolder ch = cacheDB.get(i);
			if (ch.mainCache == null)
				ch.initStates(false);
		}

		if (doDownloadGui(0)) {

			Vm.showWait(true);
			infB = new InfoBox("Status", MyLocale.getMsg(5502, "Fetching first page..."));
			infB.exec();

			pref.log("ListPages Properties : " + Preferences.NEWLINE
					+ "minDistance: " + minDistance + Preferences.NEWLINE
					+ "maxDistance: " + maxDistance + Preferences.NEWLINE
					+ "directions: " + direction + Preferences.NEWLINE
					+ "maxNew: " + pref.maxSpiderNumber + Preferences.NEWLINE
					+ "maxUpdate: " + maxUpdate + Preferences.NEWLINE
					+ "with Founds       : " + (doNotgetFound ? "no" : "yes")
					+ Preferences.NEWLINE + "alias is premium m: "
					+ (!pref.isPremium ? "no" : "yes") + Preferences.NEWLINE
					+ "Update if new Logs: " + (!pref.checkLog ? "no" : "yes")
					+ Preferences.NEWLINE, null);

			Hashtable cachesToUpdate = new Hashtable(cacheDB.size());

			cachesToUpdate = fillDownloadLists(pref.maxSpiderNumber, maxUpdate,
					maxDistance, minDistance, directions, cachesToUpdate);
			if (cachesToUpdate == null) { cachesToUpdate = new Hashtable(); };
			if (!infB.isClosed) {
				infB.setInfo(MyLocale.getMsg(5511, "Found ")
						+ cachesToLoad.size()
						+ MyLocale.getMsg(5512, " caches"));
			}
			// continue to update index to changed cache.xml things (size,terrain,difficulty,...?) 

			// =======
			// Now ready to spider each cache in the lists
			// =======

			if (cachesToUpdate.size() > 0) {
				switch (pref.spiderUpdates) {
				case Preferences.NO:
					cachesToUpdate.clear();
					break;
				case Preferences.ASK:
					MessageBox mBox = new MessageBox(MyLocale.getMsg(5517,"Spider Updates?"), cachesToUpdate.size() + MyLocale.getMsg(5518," caches in database need an update. Update now?") , FormBase.IDYES |FormBase.IDNO);
					if (mBox.execute() != FormBase.IDOK) {
						cachesToUpdate.clear();
					}
					break;
				}
			}

			int spiderErrors = 0;
			int totalCachesToLoad = cachesToLoad.size() + cachesToUpdate.size();
			boolean loadAllLogs = (pref.maxLogsToSpider > 5) || spiderAllFinds;
			pref.log("Download properties : " + Preferences.NEWLINE
					+ "maxLogs: "
					+ (loadAllLogs ? "completepage " : "shortpage") + "nr.:"
					+ pref.maxLogsToSpider + Preferences.NEWLINE
					+ "with pictures     : "
					+ (!pref.downloadPics ? "no" : "yes") + Preferences.NEWLINE
					+ "with tb           : "
					+ (!pref.downloadTBs ? "no" : "yes") + Preferences.NEWLINE,
					null);

			if (Global.mainTab.statBar != null)
				Global.mainTab.statBar.updateDisplay("");

			if (!infB.isClosed) {
				spiderErrors = downloadCaches(cachesToLoad, spiderErrors,
						totalCachesToLoad, loadAllLogs);
				spiderErrors = updateCaches(cachesToUpdate, spiderErrors,
						totalCachesToLoad, loadAllLogs);
			}

			if (spiderErrors > 0) {
				new MessageBox(MyLocale.getMsg(5500, "Error"),
						spiderErrors + MyLocale.getMsg(5516," cache descriptions%0acould not be loaded."),
						FormBase.DEFOKB).execute();
			}
			if (maxNumberAbort) {
				new MessageBox(MyLocale.getMsg(5519, "Information"),
						MyLocale.getMsg(5520,"Only the given maximum of caches were loaded.%0aRepeat spidering later to load more caches.%0aNo already existing caches were updated."),
						FormBase.DEFOKB).execute();
			}
			Global.getProfile().restoreFilter();
			Global.getProfile().saveIndex(Global.getPref(), true);

			if (!infB.isClosed) {
				infB.close(0);
			}
			Vm.showWait(false);
		}
	} // End of DoIt

	public void doItAlongARoute() {
		Area sq;
		Vector points = null;
		Navigate nav=Global.mainTab.nav;
		MovingMap mm=Global.mainTab.mm;

		if (!doDownloadGui(1))
			return;

		CWPoint startPos = pref.getCurCentrePt();
		if (!fileName.equals("")) {
			RouteImporter ri = new RouteImporter(fileName);
			points = ri.doIt();			
			if (points.size() > 0) {
				if (nav!=null) {
					if (mm==null) {
						Global.mainTab.mm = new MovingMap(nav, profile.cacheDB);
						mm=Global.mainTab.mm;
						nav.setMovingMap(Global.mainTab.mm);
					}
					if (nav.curTrack == null) {
						nav.curTrack=new Track(nav.trackColor);
						mm.addTrack(nav.curTrack);
					}
					for (int i = 0; i < points.size(); i++) {
						try {
							nav.curTrack.add((TrackPoint) points.get(i));
						} catch (IndexOutOfBoundsException e) { // track full -> create a new one
							nav.curTrack=new Track(nav.trackColor);
							nav.curTrack.add((TrackPoint) points.get(i));
							if (mm != null) mm.addTrack(nav.curTrack);
						}

					}
				}
				TrackPoint tp = (TrackPoint) points.get(0);
				startPos = new CWPoint(tp.latDec, tp.lonDec);
			} else
				startPos = null;
		}

		int answer = new MessageBox(MyLocale.getMsg(651, "Question"),
				MyLocale.getMsg(652,"Update caches with all details?"),
				MessageBox.YESB | MessageBox.NOB | MessageBox.CANCELB).execute();
		boolean complete = answer == MessageBox.YESB;
		if ( answer == MessageBox.IDCANCEL) {
			if (startPos != null) pref.setCurCentrePt(startPos);
			return;
		}

		if (startPos != null && !startPos.isValid()) {
			(new MessageBox(MyLocale.getMsg(5500, "Error"),
					MyLocale.getMsg(5509, "Coordinates for centre must be set"), FormBase.OKB))
					.execute();
			return; //
		}

		Vm.showWait(true);
		infB = new InfoBox("Status", MyLocale.getMsg(5502,"Fetching pages..."));
		infB.exec();

		if (!loggedIn || pref.forceLogin) {
			if (login() != FormBase.IDOK)
				return;
		}

		// Reset states for all caches when spidering
		// (http://tinyurl.com/dzjh7p)
		for (int i = 0; i < cacheDB.size(); i++) {
			CacheHolder ch = cacheDB.get(i);
			if (ch.mainCache == null)
				ch.initStates(false);
		}

		double lateralDistance = maxDistance; // Seitenabstand in km
		if (pref.metricSystem == Metrics.IMPERIAL) {
			lateralDistance = Metrics.convertUnit(maxDistance, Metrics.MILES,
					Metrics.KILOMETER);
		}
		cachesToLoad.clear();

		origin = startPos;
		CWPoint nextPos = startPos;
		int pointsIndex = 1;
		while (nextPos != null) {
			if (fileName.equals("")) {
				nextPos = nextRoutePoint(startPos, lateralDistance);
			} else {
				double tmpDistance = 0;
				while (tmpDistance < lateralDistance
						&& pointsIndex < points.size()) {
					TrackPoint tp = (TrackPoint) points.get(pointsIndex);
					nextPos = new CWPoint(tp.latDec, tp.lonDec);
					tmpDistance = nextPos.getDistance(startPos);
					pointsIndex++;
				}
				if (pointsIndex == points.size())
					nextPos = null;
			}
			if (nextPos != null) {
				sq = getSquare(startPos, lateralDistance);
				getCaches(sq.topleft.latDec, sq.topleft.lonDec,
						sq.buttomright.latDec, sq.buttomright.lonDec, complete);
				// pref.log("next WP = " + startPos.toString(), null);

				double degrees = startPos.getBearing(nextPos);
				double distanceToNextCache = startPos.getDistance(nextPos);
				double anzCheckPoints = distanceToNextCache / lateralDistance;
				for (int i = 1; i < anzCheckPoints; i++) {
					CWPoint nextCheckPoint = startPos.project(degrees,
							lateralDistance);
					startPos = nextCheckPoint;
					origin = nextCheckPoint;
					sq = getSquare(origin, lateralDistance);
					getCaches(sq.topleft.latDec, sq.topleft.lonDec,
							sq.buttomright.latDec, sq.buttomright.lonDec, complete);
					// pref.log("next CP = " + origin.toString(), null);
					if (infB.isClosed) {
						break;
					}
				}
				startPos = nextPos;
			}
			if (infB.isClosed) {
				break;
			}
		}
		sq = getSquare(startPos, lateralDistance);
		getCaches(sq.topleft.latDec, sq.topleft.lonDec, sq.buttomright.latDec,
				sq.buttomright.lonDec, complete);
		// pref.log("last WP = " + startPos.toString(), null);
		if (infB.isClosed) {
			Vm.showWait(false);
			return;
		} // or ask for download of intermediate result
		
		int spiderErrors = 0;
		if (complete) {
			for (int i = 0; i < cachesToLoad.size(); i++) {
				String wpt = (String) cachesToLoad.get(i);
				boolean is_found = wpt.indexOf("found") != -1;
				if (is_found)
					wpt = wpt.substring(0, wpt.indexOf("found"));
				int j = cacheDB.getIndex(wpt);
				if (j != -1)
					cacheDB.removeElementAt(j);
			}
			spiderErrors = downloadCaches(cachesToLoad, spiderErrors, cachesToLoad.size(), true);

		}
		else {
		}

		infB.close(0);
		Vm.showWait(false);
		if (spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500, "Error"),
					spiderErrors + MyLocale.getMsg(5516," cache descriptions%0acould not be loaded."),
					FormBase.DEFOKB).execute();
		}
		if (maxNumberAbort) {
			new MessageBox(MyLocale.getMsg(5519,"Information"),
					MyLocale.getMsg(5520,"Only the given maximum of caches were loaded.\nRepeat spidering later to load more caches.\nNo already existing caches were updated."),
					FormBase.DEFOKB).execute();
		}
		Global.getProfile().restoreFilter();
		Global.getProfile().saveIndex(Global.getPref(), true);
	}

	private CWPoint nextRoutePoint(CWPoint startPos, double lateralDistance) {
		// get next Destination
		double nextDistance = 0;
		int index = -1;
		CacheHolder nextCache = null;
		CacheHolder ch = null;
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			if (ch.is_Checked && ch.pos.isValid()) {
				CWPoint tmpPos = ch.pos;
				double tmpDistance = tmpPos.getDistance(startPos);
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
			return cacheDB.get(index).pos;
		} else
			return null;
	}

	private Area getSquare(CWPoint centre, double halfSideLengthKm) {
		int north = 0;
		int east = 1;
		int south = 2;
		int west = 3;
		double halfSideLength = halfSideLengthKm * 1000.0; // in meters
		Area ret = new Area();
		ret.topleft.latDec = centre.latDec;
		ret.topleft.lonDec = centre.lonDec;
		ret.topleft.shift(halfSideLength, north);
		ret.topleft.shift(halfSideLength, west);

		ret.buttomright.latDec = centre.latDec;
		ret.buttomright.lonDec = centre.lonDec;
		ret.buttomright.shift(halfSideLength, south);
		ret.buttomright.shift(halfSideLength, east);

		return ret;
	}

	public void doItQuickFillFromMapList() {

		CWPoint origin = pref.getCurCentrePt();
		if (!origin.isValid()) {
			(new MessageBox(MyLocale.getMsg(5500, "Error"),
					MyLocale.getMsg(5509, "Coordinates for centre must be set"),
					FormBase.OKB)).execute();
			return; //
		}
		if (!doDownloadGui(2))
			return;

		Vm.showWait(true);
		infB = new InfoBox("Status", MyLocale.getMsg(1609, "getting the caches."));
		infB.exec();

		// Reset states for all caches when spidering
		// (http://tinyurl.com/dzjh7p)
		for (int i = 0; i < cacheDB.size(); i++) {
			CacheHolder ch = cacheDB.get(i);
			if (ch.mainCache == null)
				ch.initStates(false);
		}

		double halfSideLength = maxDistance; // halbe Seitenlänge eines Quadrats ums Zentrum in km
		if (pref.metricSystem == Metrics.IMPERIAL) {
			halfSideLength = Metrics.convertUnit(maxDistance, Metrics.MILES,
					Metrics.KILOMETER);
		}

		if (!loggedIn || pref.forceLogin) {
			if (login() != FormBase.IDOK)
				return;
		}

		page_number = 0;
		num_added = 0;

		Area sq = getSquare(origin, halfSideLength);
		getCaches(sq.topleft.latDec, sq.topleft.lonDec, sq.buttomright.latDec,
				sq.buttomright.lonDec, false);

		if (!infB.isClosed)
			infB.close(0);
		Vm.showWait(false);

		Global.getProfile().restoreFilter();
		Global.getProfile().saveIndex(Global.getPref(), true);

	}

	private void getCaches(double north, double west, double south,
			double east, boolean setCachesToLoad) {
		if (infB.isClosed)
			return;
		double lm = (north + south) / 2.0;
		CWPoint middle = new CWPoint(lm, (west + east) / 2.0);
		CWPoint rm = new CWPoint(lm, east);
		double len = middle.getDistance(rm) * 2.0;
		page_number++;
		String listPage = getMapListPage(middle, north, west, south, east);
		int i = listPage.indexOf("\"count\\\":"); // \"count\":
		pref.log("" + north + " " + west + " " + south + " " + east + " " + listPage.substring(i) + "\n len=" + len);
		if ((listPage.indexOf("\"count\\\":501") > -1)
		||  (listPage.indexOf("\"count\\\":0") > -1 && len > 30)) {
			double northsouthmiddle = (north + south) / 2.0;
			double westeastmiddle = (west + east) / 2.0;
			getCaches(north, west, northsouthmiddle, westeastmiddle, setCachesToLoad);
			getCaches(north, westeastmiddle, northsouthmiddle, east, setCachesToLoad);
			getCaches(northsouthmiddle, west, south, westeastmiddle, setCachesToLoad);
			getCaches(northsouthmiddle, westeastmiddle, south, east, setCachesToLoad);
		} else {
			addCaches(listPage, setCachesToLoad);
		}
	}

	private void addCaches(String listPage, boolean setCachesToLoad) {
		String[] caches = mString.split(listPage, '{');
		//int posId=0;        //id egal
		//int posName=1;      //nn
		
		//positions decreased by 2, because we cut away the name to prevent parsing errors
		int posWP=0;        //gc 
		int posLat=1;       //lat
		int posLon=2;       //lon
		int posType=3;      //ctid
		int posFound=4;     //f
		int posOwn=5;       //o
		int posAvailable=6; //ia
		// ignoring first 3 lines
		for (int i = 4; i < caches.length; i++) {
			if (infB.isClosed) return;
			
			//cut away name to prevent parsing errors			
			int WpIndex = caches[i].indexOf("\"gc\\\"");
			String elements[] = mString.split(caches[i].substring(WpIndex), ',');
			
			boolean found = (elements[posFound].indexOf("true") > -1 ? true : false);
			if (found && doNotgetFound)	continue;
			
			byte cacheType = CacheType.gcSpider2CwType(mString.split(elements[posType], ':')[1]);
			if (restrictedCacheType != CacheType.CW_TYPE_ERROR) {
				if (restrictedCacheType != cacheType) continue;
			}

			String wp = mString.split(elements[posWP], '\"')[3];
			wp=wp.substring(0, wp.length()-1);
			CacheHolder ch = cacheDB.get(wp);
			if (ch == null) {

				String lat = mString.split(elements[posLat], ':')[1];
				String lon = mString.split(elements[posLon], ':')[1];
				String own = mString.split(elements[posOwn], ':')[1];
				boolean available = (elements[posAvailable].indexOf("true") > -1 ? true : false);
				
				int NameIndex = caches[i].indexOf("\"nn\\\"");
				String cacheName = caches[i].substring (NameIndex + 8, WpIndex - 4 );
				cacheName = STRreplace.replace(cacheName, "\\\"", "\"" );

				ch = new CacheHolder();
				ch.setWayPoint(wp);
				ch.setLatLon(lat + " " + lon);
				ch.setType(cacheType);
				if (own.equals("true")) {
					ch.setOwned(true);
				} else {
					if (found) {
						ch.setFound(true);
						ch.setCacheStatus(ch.getFoundText());
					}
				}
				ch.setAvailable(available);
				ch.setCacheName(cacheName);
				num_added++;
				cacheDB.add(ch);
				if (setCachesToLoad) {
					cachesToLoad.add(wp + "found");
				} else {
					ch.getCacheDetails(false).URL="http://www.geocaching.com/seek/cache_details.aspx?wp="+wp;
					ch.save();
				}
				if (Global.mainTab.statBar != null)
					Global.mainTab.statBar.updateDisplay("GC pages: "
							+ page_number + " Caches added to CW: "
							+ num_added);
			} else {
			}
		}
	}

	private boolean doDownloadGui(int menu) {

		OCXMLImporterScreen options;
		direction = "";
		if (menu == 0 && spiderAllFinds) {
			options = new OCXMLImporterScreen(MyLocale.getMsg(217,
					"Spider all finds from geocaching.com"),
					OCXMLImporterScreen.ISGC | OCXMLImporterScreen.MAXNUMBER
							| OCXMLImporterScreen.MAXUPDATE
							| OCXMLImporterScreen.IMAGES
							| OCXMLImporterScreen.TRAVELBUGS
							| OCXMLImporterScreen.MAXLOGS);
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
			options = new OCXMLImporterScreen(MyLocale.getMsg(131,
					"Download from geocaching.com"), OCXMLImporterScreen.ISGC
					| OCXMLImporterScreen.MAXNUMBER
					| OCXMLImporterScreen.MAXUPDATE
					| OCXMLImporterScreen.MINDIST | OCXMLImporterScreen.DIST
					| OCXMLImporterScreen.DIRECTION
					| OCXMLImporterScreen.INCLUDEFOUND
					| OCXMLImporterScreen.IMAGES
					| OCXMLImporterScreen.TRAVELBUGS
					| OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.TYPE);
			// setting defaults for input
			if (pref.spiderUpdates == Preferences.NO) {
				options.maxNumberUpdates.setText("0");
			}
			// doing the input
			if (options.execute() == FormBase.IDCANCEL) {
				return false;
			}
			// setting default values for options not used (if necessary)

			String minDist = options.minDistanceInput.getText();
			minDistance = Common.parseDouble(minDist);
			profile.setMinDistGC(Double.toString(minDistance).replace(',', '.'));

			direction = options.directionInput.getText();
			directions = mString.split(direction, '-');

			doNotgetFound = options.foundCheckBox.getState();
			profile.setDirectionGC(direction);

		} else if (menu == 1) { // menu = 1 input values for get Caches along a
								// route
			options = new OCXMLImporterScreen(MyLocale.getMsg(137,
					"Download along a Route from geocaching.com"),
					OCXMLImporterScreen.ISGC | OCXMLImporterScreen.DIST
							| OCXMLImporterScreen.INCLUDEFOUND
							| OCXMLImporterScreen.TRAVELBUGS
							| OCXMLImporterScreen.IMAGES
							| OCXMLImporterScreen.MAXLOGS
							| OCXMLImporterScreen.FILENAME
							| OCXMLImporterScreen.TYPE);
			// setting defaults for input
			// doing the input
			if (options.execute() == FormBase.IDCANCEL) {
				return false;
			}
			// setting default values for options not used (if necessary)
			minDistance = 0.0;
			doNotgetFound = options.foundCheckBox.getState();
			maxUpdate = 0;
			fileName = options.fileName;
		} else { // if (menu == 2) {
			options = new OCXMLImporterScreen(MyLocale.getMsg(138,
					"Qick Import"), OCXMLImporterScreen.ISGC
					| OCXMLImporterScreen.DIST
					| OCXMLImporterScreen.INCLUDEFOUND
					| OCXMLImporterScreen.TYPE);
			// setting defaults for input doing the input
			if (options.execute() == FormBase.IDCANCEL) {
				return false;
			}
			doNotgetFound = options.foundCheckBox.getState();
		}

		if (menu == 0) {

			int maxNew = -1;
			String maxNumberString = options.maxNumberInput.getText();
			if (maxNumberString.length() != 0) {
				maxNew = Common.parseInt(maxNumberString);
			}
			// if (maxNew == 0) return false;
			if (maxNew == -1)
				maxNew = Integer.MAX_VALUE;
			if (maxNew != pref.maxSpiderNumber) {
				pref.maxSpiderNumber = maxNew;
				pref.savePreferences();
			}

			maxUpdate = -1;
			String maxUpdateString = options.maxNumberUpdates.getText();
			if (maxUpdateString.length() != 0) {
				maxUpdate = Common.parseInt(maxUpdateString);
			}
			if (maxUpdate == -1)
				maxUpdate = Integer.MAX_VALUE;
		}

		// options for all

		if (options.maxDistanceInput != null) {
			String maxDist = options.maxDistanceInput.getText();
			maxDistance = Common.parseDouble(maxDist);
			if (maxDistance == 0) return false;
			if (maxDistance < 0.5) maxDistance = 0.5; // zur Sicherheit bei "along the route" mindenstens 500 meter Umkreis
			profile.setDistGC(Double.toString(maxDistance));
		}

		// works even if TYPE not in options
		cacheTypeRestriction = options.getCacheTypeRestriction(p);
		restrictedCacheType = options.getRestrictedCacheType(p);

		options.close(0);

		return true;

	}

	private Hashtable fillDownloadLists(int maxNew, int maxUpdate,
			double toDistance, double fromDistance, String[] directions,
			Hashtable cExpectedForUpdate) {
		if (!loggedIn || pref.forceLogin) {
			if (login() != FormBase.IDOK)
				return null;
		}

		int numFinds;
		int startPage = 1;
		// get pagenumber of page with fromDistance , to skip reading of pages < fromDistance
		if (fromDistance > 0) {
			// distance in miles for URL
			int fromDistanceInMiles = (int) java.lang.Math.ceil(fromDistance);
			if (pref.metricSystem != Metrics.IMPERIAL) {
				fromDistanceInMiles = (int) java.lang.Math.ceil(Metrics
						.convertUnit(fromDistance, Metrics.KILOMETER,
								Metrics.MILES));
			}
			// - a mile to be save to get a page with fromDistance
			getFirstListPage(java.lang.Math.max(fromDistanceInMiles - 1, 1));
			numFinds = getNumFound(htmlListPage); // Number of caches from gc Listpage
			// calc the number of the startpage
			startPage = (int) java.lang.Math.ceil(numFinds / 20);
		}

		// max distance in miles for URL, so we can get more than 80km
		int toDistanceInMiles = (int) java.lang.Math.ceil(toDistance);
		if (pref.metricSystem != Metrics.IMPERIAL) {
			toDistanceInMiles = (int) java.lang.Math.ceil(Metrics.convertUnit(
					toDistance, Metrics.KILOMETER, Metrics.MILES));
		}
		// add a mile to be save from different distance calculations in CW and at GC
		toDistanceInMiles++;
		getFirstListPage(toDistanceInMiles);
		numFinds = getNumFound(htmlListPage); // Number of caches from gc first Listpage

		if (fromDistance > 0) {
			// skip (most of) the pages with distance < fromDistance
			for (int i = 0; i < (startPage / 10); i++) {
				getAListPage(toDistanceInMiles, gotoNextBlock);
			}
			if (startPage > 1)
				getAListPage(toDistanceInMiles, gotoPage + startPage);
		}

		int numFoundInDB = 0; // Number of GC-founds already in this profile
		if (spiderAllFinds) {
			numFoundInDB = getFoundInDB();
			pref.log((spiderAllFinds ? "all Finds (DB/GC)" + numFoundInDB + "/"
					+ numFinds : "new and update Caches")
					+ Preferences.NEWLINE, null);
			maxNew = java.lang.Math.min(numFinds - numFoundInDB, maxNew);
			if (maxUpdate == 0 && maxNew == 0) {
				Vm.showWait(false);
				infB.close(0);
				return null;
			}
		}

		if (maxUpdate > 0) {
			double distanceInKm = toDistance;
			if (pref.metricSystem == Metrics.IMPERIAL) {
				distanceInKm = Metrics.convertUnit(toDistance, Metrics.MILES,
						Metrics.KILOMETER);
			}
			// expecting all are changed (archived caches remain always)
			for (int i = 0; i < cacheDB.size(); i++) {
				CacheHolder ch = cacheDB.get(i);
				if (spiderAllFinds) {
					if ((ch.getWayPoint().substring(0, 2)
							.equalsIgnoreCase("GC"))
							&& !ch.is_black()) {
						cExpectedForUpdate.put(ch.getWayPoint(), ch);
					}
				} else {
					if ((!ch.is_archived())
							&& (ch.kilom <= distanceInKm)
							&& !(doNotgetFound && (ch.is_found() || ch
									.is_owned()))
							&& (ch.getWayPoint().substring(0, 2)
									.equalsIgnoreCase("GC"))
							&& ((restrictedCacheType == CacheType.CW_TYPE_ERROR) || (ch
									.getType() == restrictedCacheType))
							&& !ch.is_black()) {
						cExpectedForUpdate.put(ch.getWayPoint(), ch);
					}
				}
			}
		}
		int startSize = cExpectedForUpdate.size(); // for save reasons

		Hashtable cFoundForUpdate = new Hashtable(cacheDB.size()); // for don't loose the already done work
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
					pref.log("check listBlockRex in spider.def" + Preferences.NEWLINE + htmlListPage);
					tableOfHtmlListPage = "";
				}
				RexPropLine.search(tableOfHtmlListPage);
				while (toDistance > 0) {
					if (!RexPropLine.didMatch()) {
						if (page_number == 1 && found_on_page == 0)
							pref.log("check lineRex in spider.def");
						break;
					}
					found_on_page++;
					if (Global.mainTab.statBar != null)
						Global.mainTab.statBar.updateDisplay("working "
								+ page_number + " / " + found_on_page);
					String CacheDescriptionGC = RexPropLine.stringMatched(1);
					String DistanceAndDirection = getDistanceAndDirection(CacheDescriptionGC);
					double gotDistance = getDistGC(DistanceAndDirection);
					String chWaypoint = getWP(CacheDescriptionGC);
					if (gotDistance <= toDistance) {
						CacheHolder ch = cacheDB.get(chWaypoint);
						if (ch == null) { // not in DB
							if (gotDistance >= fromDistance
									&& directionOK(directions, getDirection(DistanceAndDirection))
									&& doPMCache(CacheDescriptionGC)
									&& cachesToLoad.size() < maxNew) {
								if (CacheDescriptionGC.indexOf(propFound) != -1)
									chWaypoint = chWaypoint + "found";
								if (!cachesToLoad.contains(chWaypoint)) {
									cachesToLoad.add(chWaypoint);
								}
							} else {
								// pref.log("no load of (Premium Cache/other direction/short Distance ?) " + chWaypoint);
								cExpectedForUpdate.remove(chWaypoint);
							}
						} else {
							if (maxUpdate > 0) { // regardless of fromDistance
								if (!ch.is_black()) {
									if (doPMCache(CacheDescriptionGC)
										&& updateExists(ch, CacheDescriptionGC)) {
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
								if (cExpectedForUpdate.size() <= cFoundForUpdate
										.size()) {
									toDistance = 0;
								}
							}
						}
					} else
						toDistance = 0; // finish listing
					// get next row of table (next Cache Description) of this htmlListPage
					RexPropLine.searchFrom(tableOfHtmlListPage, RexPropLine
							.matchedTo());
					if (infB.isClosed) {
						toDistance = 0;
						break;
					}
				} // next Cache
				infB.setInfo(MyLocale.getMsg(5511, "Found ")
						+ cachesToLoad.size() + " / " + cFoundForUpdate.size()
						+ MyLocale.getMsg(5512, " caches"));
				if (found_on_page < 20) {
					if (spiderAllFinds) {
						// check all pages ( seen a gc-account with found_on_page less 20 and not on end )
						if (((page_number - 1) * 20 + found_on_page) >= numFinds) {
							toDistance = 0;
						}
					} else
						toDistance = 0; // last page (has less than 20 entries!?) to check reached
				}
				if (toDistance > 0) {
					getAListPage(toDistanceInMiles, gotoNextPage);
					page_number++;
					found_on_page = 0;
				}
			} // loop pages
		} // try
		catch (Exception ex) {
			pref.log("Download error : ", ex, true);
			infB.close(0);
			Vm.showWait(false);
			cExpectedForUpdate.clear();
		}
		pref.log("Checked " + page_number + " pages"+Preferences.NEWLINE+
				"with " + ((page_number-1)*20+found_on_page) + " caches"+Preferences.NEWLINE+
				"Found " + cachesToLoad.size() + " new caches"+Preferences.NEWLINE+
				"Found " + cExpectedForUpdate.size() + "/" + cFoundForUpdate.size() + " caches for update"+Preferences.NEWLINE+
				"Found " + numAvailableUpdates + " caches with changed available status."+Preferences.NEWLINE+
				"Found " + numLogUpdates + " caches with new found in log."+Preferences.NEWLINE+
				"Found " + (cExpectedForUpdate.size()-numAvailableUpdates-numLogUpdates) + " caches possibly archived."+Preferences.NEWLINE+
				"Found " + cFoundForUpdate.size() + "?=" + (numFoundUpdates+numArchivedUpdates+numAvailableUpdates+numArchivedUpdates) + " caches to update."+Preferences.NEWLINE+
				"Found " + numPrivate + " Premium Caches (for non Premium Member.)",null);
		if(spiderAllFinds){
			pref.log("Found " + numFoundUpdates + " caches with no found in profile."+Preferences.NEWLINE+
			"Found " + numArchivedUpdates + " caches with changed archived status."+Preferences.NEWLINE,null);
		}

		if (cExpectedForUpdate.size() == startSize)
			cExpectedForUpdate.clear(); // there must be something wrong
		if (cExpectedForUpdate.size() == 0
				|| cExpectedForUpdate.size() > maxUpdate)
			cExpectedForUpdate = cFoundForUpdate;
		return cExpectedForUpdate;

	}

	private int downloadCaches(Vector cachesToLoad, int spiderErrors,
			int totalCachesToLoad, boolean loadAllLogs) {
		for (int i = 0; i < cachesToLoad.size(); i++) {
			if (infB.isClosed)
				break;
			String wpt = (String) cachesToLoad.get(i);
			boolean is_found = wpt.indexOf("found") != -1;
			if (is_found)
				wpt = wpt.substring(0, wpt.indexOf("found"));
			// Get only caches not already available in the DB
			if (cacheDB.getIndex(wpt) == -1) {
				infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + wpt + " ("
						+ (i + 1) + " / " + totalCachesToLoad + ")");
				CacheHolder holder = new CacheHolder();
				holder.setWayPoint(wpt);
				int test = getCacheByWaypointName(holder, false,
						pref.downloadPics, pref.downloadTBs, doNotgetFound,
						loadAllLogs || is_found);
				if (test == SPIDER_CANCEL) {
					infB.close(0);
					break;
				} else if (test == SPIDER_ERROR) {
					spiderErrors++;
				} else if (test == SPIDER_OK) {
					cacheDB.add(holder);
					holder.save();
				} // For test == SPIDER_IGNORE_PREMIUM and SPIDER_IGNORE there
					// is nothing to do
			}
		}
		return spiderErrors;
	}

	private int updateCaches(Hashtable cachesToUpdate, int spiderErrors,
			int totalCachesToLoad, boolean loadAllLogs) {
		int j = 1;
		for (Enumeration e = cachesToUpdate.elements(); e.hasMoreElements(); j++) {
			if (infB.isClosed)
				break;
			CacheHolder ch = (CacheHolder) e.nextElement();
			infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + ch.getWayPoint()
					+ " (" + (cachesToLoad.size() + j) + " / "
					+ totalCachesToLoad + ")");
			int test = spiderSingle(cacheDB.getIndex(ch), infB, false,
					loadAllLogs);
			if (test == SPIDER_CANCEL) {
				break;
			} else if (test == SPIDER_ERROR) {
				spiderErrors++;
				pref.log("[updateCaches] could not spider " + ch.getWayPoint(), null);
			} else {
				// profile.hasUnsavedChanges=true;
			}
		}
		return spiderErrors;
	}

	/**
	 * Method to spider a single cache. It assumes a login has already been
	 * performed!
	 * 
	 * @return 1 if spider was successful, -1 if spider was cancelled by closing
	 *         the infobox, 0 error, but continue with next cache
	 */
	public int spiderSingle(int number, InfoBox pInfB, boolean forceLogin,
			boolean loadAllLogs) {
		int ret = -1;
		this.infB = pInfB;
		CacheHolder ch = new CacheHolder(); // cacheDB.get(number);
		ch.setWayPoint(cacheDB.get(number).getWayPoint());
		if (ch.isAddiWpt())
			return -1; // No point re-spidering an addi waypoint, comes with
						// parent

		// check if we need to login
		if (!loggedIn || forceLogin) {
			if (this.login() != FormBase.IDOK)
				return -1;
			// loggedIn is already set by this.login()
		}
		try {
			// Read the cache data from GC.COM and compare to old data
			ret = getCacheByWaypointName(ch, true, pref.downloadPics,
					pref.downloadTBs, false, loadAllLogs);
			// Save the spidered data
			if (ret == SPIDER_OK) {
				CacheHolder cacheInDB = cacheDB.get(number);
				cacheInDB.initStates(false);
				if (cacheInDB.is_found() && !ch.is_found() && !loadAllLogs) {
					// If the number of logs to spider is 5 or less, then the
					// "not found" information
					// of the spidered cache is not credible. In this case it
					// should not overwrite
					// the "found" state of an existing cache.
					ch.setFound(true);
				}
				// preserve rating information
				ch.setNumRecommended(cacheInDB.getNumRecommended());
				if (pref.downloadPics) {
					// delete obsolete images when we have current set
					CacheImages.cleanupOldImages(cacheInDB
							.getCacheDetails(true).images, ch
							.getCacheDetails(false).images);
				} else {
					// preserve images if not downloaded
					ch.getCacheDetails(false).images = cacheInDB
							.getCacheDetails(true).images;
				}
				cacheInDB.update(ch);
				cacheInDB.save();
			}
		} catch (Exception ex) {
			pref.log("[spiderSingle] Error spidering " + ch.getWayPoint() + " in spiderSingle", ex);
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
		// Check whether spider definitions could be loaded, if not issue
		// appropriate message and terminate
		// Try to login. If login fails, issue appropriate message and terminate
		if (!loggedIn || pref.forceLogin) {
			if (login() != FormBase.IDOK) {
				return "";
			}
		}
		InfoBox localInfB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
		localInfB.exec();
		try {
			String doc = p.getProp("waypoint") + wayPoint;
			completeWebPage = fetchText(doc, false);
			pref.log("Fetched " + wayPoint);
		} catch (Exception ex) {
			localInfB.close(0);
			pref.log("[getCacheCoordinates] Could not fetch " + wayPoint, ex);
			return "";
		}
		localInfB.close(0);
		try {
			return getLatLon(completeWebPage);
		} catch (Exception ex) {
			return "????";
		}
	} // getCacheCoordinates

	/**
	 * Method to login the user to gc.com It will request a password and use the
	 * alias defined in preferences If the login page cannot be fetched, the
	 * password is cleared. If the login fails, an appropriate message is
	 * displayed.
	 */
	private int login() {
		String passwort = pref.password;
		loggedIn = false;
		String loginPage, loginPageUrl, loginSuccess, nextPage;
		try {
			loginPageUrl = p.getProp("loginPage");
			loginSuccess = p.getProp("loginSuccess");
			nextPage = p.getProp("nextPage");
		} catch (Exception ex) { // Tag not found in spider.def
			return ERR_LOGIN;
		}
		// Get password
		InfoBox localInfB = new InfoBox(MyLocale.getMsg(5506, "Password"),
				MyLocale.getMsg(5505, "Enter Password"), InfoBox.INPUT);
		localInfB.feedback.setText(passwort);
		localInfB.feedback.isPassword = true;
		int code = FormBase.IDOK;
		if (passwort.equals("")) {
			code = localInfB.execute();
			passwort = localInfB.getInput();
		}
		localInfB.close(0);
		if (code != FormBase.IDOK)
			return code;
		// Now start the login proper
		localInfB = new InfoBox(MyLocale.getMsg(5507, "Status"), MyLocale.getMsg(5508, "Logging in..."));
		localInfB.exec();
		try {
			// Access the page once to get a viewstate
			loginPage = fetchText(loginPageUrl, true); // http://www.geocaching.com/login/Default.aspx
			pref.log("[login]:Fetched login page " + loginPageUrl);
			if (loginPage.equals("")) {
				localInfB.close(0);
				(new MessageBox(MyLocale.getMsg(5500, "Error"),
						MyLocale.getMsg(5499,"Error loading login page.%0aPlease check your internet connection."),
						FormBase.OKB)).execute();
				pref.log("[login]:Could not fetch: gc.com login page " + loginPageUrl, null);
				return ERR_LOGIN;
			}
		} catch (Exception ex) {
			localInfB.close(0);
			(new MessageBox(MyLocale.getMsg(5500, "Error"),
					MyLocale.getMsg(5499, "Error loading login page.%0aPlease check your internet connection."),
					FormBase.OKB)).execute();
			pref.log("[login]:Could not fetch: gc.com login page", ex);
			return ERR_LOGIN;
		}
		if (!localInfB.isClosed) { // If user has not aborted, we continue
			Regex rexCookieID = new Regex("(?i)Set-Cookie: userid=(.*?);.*");
			Regex rexViewstate = new Regex(
					"id=\"__VIEWSTATE\" value=\"(.*?)\" />");
			// Regex rexViewstate1 = new
			// Regex("id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
			// Regex rexEventvalidation = new
			// Regex("id=\"__EVENTVALIDATION\" value=\"(.*?)\" />");
			Regex rexCookieSession = new Regex(
					"(?i)Set-Cookie: ASP.NET_SessionId=(.*?);.*");
			String viewstate = "";
			rexViewstate.search(loginPage);
			if (rexViewstate.didMatch()) {
				viewstate = rexViewstate.stringMatched(1);
			} else {
				pref.log("[login]:rexViewstate not found before login", null);
			}

			if (loginPage.indexOf(loginSuccess) > 0)
				pref.log("[login]:Already logged in" + pref.myAlias);
			else {
				/*
				 * rexEventvalidation.search(loginPage);
				 * if(rexEventvalidation.didMatch()){ // eventvalidation =
				 * rexEventvalidation.stringMatched(1); } else
				 * pref.log("[login]:rexEventvalidation not found before login"
				 * ,null); //Ok now login!
				 */
				try {
					StringBuffer sb = new StringBuffer(1000);
					sb.append(URL.encodeURL("__VIEWSTATE", false));
					sb.append("=");
					sb.append(URL.encodeURL(viewstate, false));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("myUsername", false));
					sb.append("=");
					sb.append(encodeUTF8URL(Utils
							.encodeJavaUtf8String(pref.myAlias)));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("myPassword", false));
					sb.append("=");
					sb.append(encodeUTF8URL(Utils
							.encodeJavaUtf8String(passwort)));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("cookie", false));
					sb.append("=");
					sb.append(URL.encodeURL("on", false));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("Button1", false));
					sb.append("=");
					sb.append(URL.encodeURL("Login", false));
					// sb.append("&");
					// sb.append(URL.encodeURL("__EVENTVALIDATION",false));
					// sb.append("=");
					// sb.append(URL.encodeURL(eventvalidation,false));
					loginPage = fetch_post(loginPageUrl, sb.toString(), true);
					if (loginPage.indexOf(loginSuccess) > 0)
						pref.log("Login successful: " + pref.myAlias);
					else {
						pref.log("Login failed. Wrong Account or Password? " + pref.myAlias, null);
						pref.log("[login.LoginUrl]:" + sb.toString(), null);
						pref.log("[login.Answer]:" + loginPage, null);
						localInfB.close(0);
						(new MessageBox(MyLocale.getMsg(5500, "Error"),
								MyLocale.getMsg(5501,"Login failed! Wrong account or password?"),
								FormBase.OKB)).execute();
						return ERR_LOGIN;
					}
				} catch (Exception ex) {
					pref.log("[login]:Login failed with exception.", ex);
					localInfB.close(0);
					(new MessageBox(MyLocale.getMsg(5500, "Error"),
							MyLocale.getMsg(5501,"Login failed. Error loading page after login."),
							FormBase.OKB)).execute();
					return ERR_LOGIN;
				}
			}

			rexViewstate.search(loginPage);
			if (!rexViewstate.didMatch()) {
				pref.log("[login]:check rexViewstate in SpiderGC.java --> not found after login"
						+ Preferences.NEWLINE + loginPage, null);
			}
			viewstate = rexViewstate.stringMatched(1);

			rexCookieID.search(loginPage);
			if (!rexCookieID.didMatch()) {
				pref.log("[login]:check rexCookieID in SpiderGC.java --> CookieID not found. Using old one."
						 + Preferences.NEWLINE + loginPage, null);
			} else
				cookieID = rexCookieID.stringMatched(1);
			rexCookieSession.search(loginPage);
			if (!rexCookieSession.didMatch()) {
				pref.log("[login]:check rexCookieSession in SpiderGC.java --> CookieSession not found. Using old one."
						+ Preferences.NEWLINE + loginPage);
			} else
				cookieSession = rexCookieSession.stringMatched(1);

			/*
			 * String viewstate1; rexViewstate1.search(loginPage);
			 * if(rexViewstate1.didMatch()){ viewstate1 =
			 * rexViewstate1.stringMatched(1); } else { viewstate1 = "";
			 * pref.log(
			 * "[login]:check rexViewstate1 in SpiderGC.java --> not found after login"
			 * +Preferences.NEWLINE+loginPage); }
			 */

			/*
			 * rexEventvalidation.search(htmlPage);
			 * if(rexEventvalidation.didMatch()){ eventvalidation =
			 * rexEventvalidation.stringMatched(1); } else { eventvalidation =
			 * ""; }
			 */

			String strEnglishPage = "ctl00$uxLocaleList$uxLocaleList$ctl01$uxLocaleItem";
			String postStr = URL.encodeURL("__EVENTTARGET", false) + "="
					+ URL.encodeURL(strEnglishPage, false) + "&"
					+ URL.encodeURL("__EVENTARGUMENT", false) + "="
					+ URL.encodeURL("", false)
					// + "&" + URL.encodeURL("__VIEWSTATEFIELDCOUNT",false)
					// +"=2"
					+ "&" + URL.encodeURL("__VIEWSTATE", false) + "="
					+ URL.encodeURL(viewstate, false);
			// + "&" + URL.encodeURL("__VIEWSTATE1",false) +"="+
			// URL.encodeURL(viewstate1,false);
			// + "&" + URL.encodeURL("__EVENTVALIDATION",false) +"="+
			// URL.encodeURL(eventvalidation,false);
			try {
				loginPage = fetch_post(loginPageUrl, postStr, true);
				pref.log("Switched to English");
			} catch (Exception ex) {
				pref.log("Error switching to English: check/n" + loginPageUrl
						+ "/n" + postStr + "/n" + nextPage + "/n", ex);
			}
		}
		boolean loginAborted = localInfB.isClosed;
		localInfB.close(0);
		if (loginAborted)
			return FormBase.IDCANCEL;
		else {
			loggedIn = true;
			return FormBase.IDOK;
		}
	}

	/*
	 *
	 */
	private void initialiseProperties() {
		try {
			propFirstPage = p.getProp("firstPage");
			propFirstPage2 = p.getProp("firstPage2");
			propFirstPageFinds = p.getProp("firstPageFinds");
			propMaxDistance = p.getProp("maxDistance");
			propShowOnlyFound = p.getProp("showOnlyFound");
			RexPropListBlock = new Regex(p.getProp("listBlockRex"));
			RexPropLine = new Regex(p.getProp("lineRex"));
			RexNumFinds = new Regex("Total Records: <b>(.*?)</b>");
			RexPropLogDate = new Regex(p.getProp("logDateRex"));
			propAvailable = p.getProp("availableRex");
			propArchived = p.getProp("archivedRex");
			propFound = p.getProp("found");
			propPM = p.getProp("PMRex");
			RexPropDistance = new Regex(p.getProp("distRex"));
			RexPropDistanceCode = new Regex(p.getProp("distCodeRex"));
			DistanceCodeKey = p.getProp("distCodeKey");
			DTSCodeKey = p.getProp("DTSCodeKey");
			RexPropWaypoint = new Regex(p.getProp("waypointRex"));
			RexPropType = new Regex(p.getProp("TypeRex"));
			RexPropDTS = new Regex(p.getProp("DTSRex"));
			RexPropOwn = new Regex(p.getProp("own"));
			RexLogBlock = new Regex(p.getProp("blockRex"));
			exSingleLog = new Extractor("", p.getProp("singleLogExStart"), p.getProp("singleLogExEnd"), 0, false);
			exIcon = new Extractor("", p.getProp("iconExStart"), p.getProp("iconExEnd"), 0, true);
			exNameTemp = new Extractor("", p.getProp("nameTempExStart"), p.getProp("nameTempExEnd"), 0, true);
			exName = new Extractor("", p.getProp("nameExStart"), p.getProp("nameExEnd"), 0, true);
			exDate = new Extractor("", p.getProp("dateExStart"), p.getProp("dateExEnd"), 0, true);
			exLog = new Extractor("", p.getProp("logExStart"), p.getProp("logExEnd"), 0, true);
			exLogId = new Extractor("", p.getProp("logIdExStart"), p.getProp("logIdExEnd"), 0, true);
			icon_smile = p.getProp("icon_smile");
			icon_camera = p.getProp("icon_camera");
			icon_attended = p.getProp("icon_attended");
			RexCacheType = new Regex(p.getProp("cacheTypeRex"));
		} catch (Exception ex) {
			pref.log("Error fetching Properties.", ex);
		}
	}

	/*
	 *
	 */
	private void getFirstListPage(int distance) {
		// Get first page

		String url;
		if (spiderAllFinds) {
			url = propFirstPageFinds + encodeUTF8URL(Utils.encodeJavaUtf8String(pref.myAlias));
		} else {
			url = propFirstPage + origin.getLatDeg(TransformCoordinates.DD)
				+ propFirstPage2
				+ origin.getLonDeg(TransformCoordinates.DD)
				+ propMaxDistance + Integer.toString(distance);
			if (doNotgetFound)
				url = url + propShowOnlyFound;
		}
		url = url + cacheTypeRestriction;
		
		try {
			htmlListPage = fetchText(url, false);
			pref.log("[getFirstListPage] Got first page " + url);
		} catch (Exception ex) {
			pref.log("[getFirstListPage] Error fetching first list page " + url, ex, true);
			Vm.showWait(false);
			infB.close(0);
			(new MessageBox(MyLocale.getMsg(5500, "Error"), 
					MyLocale.getMsg(5503, "Error fetching first list page."),
					FormBase.OKB)).execute();
			return;
		}
	}

	/**
	 * in: ... out: page_number,htmlPage
	 */
	private void getAListPage(int distance, String whatPage) {
		String url;
		if (spiderAllFinds) {
			url = propFirstPage;
		} else {
			url = propFirstPage + origin.getLatDeg(TransformCoordinates.DD)
				+ propFirstPage2
				+ origin.getLonDeg(TransformCoordinates.DD)
				+ propMaxDistance + Integer.toString(distance);
			if (doNotgetFound)
				url = url + propShowOnlyFound;
		}
		url = url + cacheTypeRestriction;

		Regex rexViewstate = new Regex("id=\"__VIEWSTATE\" value=\"(.*?)\" />");
		String viewstate;
		rexViewstate.search(htmlListPage);
		if (rexViewstate.didMatch()) {
			viewstate = rexViewstate.stringMatched(1);
		} else {
			viewstate = "";
			pref.log("[getAListPage] check rexViewstate in SpiderGC.java" + Preferences.NEWLINE + htmlListPage);
		}

		Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
		String viewstate1;
		rexViewstate1.search(htmlListPage);
		if (rexViewstate1.didMatch()) {
			viewstate1 = rexViewstate1.stringMatched(1);
		} else {
			viewstate1 = "";
			pref.log("[getAListPage] check rexViewstate1 in SpiderGC.java" + Preferences.NEWLINE + htmlListPage);
		}

		/*
		 * rexEventvalidation.search(htmlPage);
		 * if(rexEventvalidation.didMatch()){ 
		 * eventvalidation = rexEventvalidation.stringMatched(1); } 
		 * else { eventvalidation = ""; }
		 */

		String postData = "__EVENTTARGET=" + URL.encodeURL(whatPage, false) + "&" +
						  "__EVENTARGUMENT=" + "&" +
						  "__VIEWSTATEFIELDCOUNT=2" + "&" +
						  "__VIEWSTATE=" + URL.encodeURL(viewstate, false) + "&" +
						  "__VIEWSTATE1=" + URL.encodeURL(viewstate1, false); // + "&" +
						  //"__EVENTVALIDATION=" + URL.encodeURL(eventvalidation,false);
		try {
			htmlListPage = fetch_post(url, postData, true);
			pref.log("[getAListPage] Got list page: " + URL.encodeURL(whatPage, false));
		} catch (Exception ex) {
			pref.log("[getAListPage] Error getting a list page" + url, ex);
		}
	}

	/* */
	private String getMapListPage(CWPoint middle, double north, double west, double south, double east) {
		String ret;
		
		String referer = "http://www.geocaching.com/map/default.aspx" + 
		"?lat="	+ middle.getLatDeg(TransformCoordinates.DD) + 
		"&lng="	+ middle.getLonDeg(TransformCoordinates.DD);

		if (userToken.equals("")) {
			ret = fetchText(referer, false);
			int i = ret.indexOf("userToken = '");
			i=i+13;
			int j = ret.indexOf("'", i);
			userToken = ret.substring(i,j);
		}
		
		String url = "http://www.geocaching.com/map/default.aspx/MapAction";

		String strLeft = MyLocale.formatDouble(west, "#0.00000").replace(',','.');
		String strUp = MyLocale.formatDouble(north, "#0.00000").replace(',','.');
		String strRight = MyLocale.formatDouble(east, "#0.00000").replace(',','.');
		String strDown = MyLocale.formatDouble(south, "#0.00000").replace(',','.');
		String param1 = "{\"dto\":{\"data\":{\"c\":1,\"m\":\"\",\"d\":\"";
		String param2 = strUp + "|" + strDown + "|" + strRight + "|" + strLeft;
		String param3 = "\"},\"ut\":\"";
		String param4 = "\"}}";
		String postData = param1+param2+param3+userToken+param4;

		try {
		ret = post(url, postData, referer, false);
		} catch (Exception ex) {
			ret = "";
			pref.log("[SpiderGC:getMapListPage] Error getting map Cachepage" + url + postData, ex);
		}
		return ret;
 	}

	private static String post(String url, String postData, String referer, boolean withResponseHeaders) {
		HttpConnection conn;
		try {
			conn = new HttpConnection(url);
			JavaUtf8Codec codec = new ewe.io.JavaUtf8Codec();
			conn.documentIsEncoded = true;
			conn.setRequestorProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
			conn.setRequestorProperty("Accept","application/json, text/javascript, */*; q=0.01");
			conn.setRequestorProperty("Accept-Language","de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
			conn.setRequestorProperty("Accept-Encoding","gzip,deflate");
			conn.setRequestorProperty("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			conn.setRequestorProperty("Keep-Alive","115");
			conn.setRequestorProperty("Proxy-Connection","keep-alive");
			conn.setRequestorProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestorProperty("X-Requested-With","XMLHttpRequest");
			conn.setRequestorProperty("Referer",referer);
			conn.setRequestorProperty("Pragma","no-cache");
			conn.setRequestorProperty("Cache-Control","no-cache");
			// if (cookieSession.length() > 0) { conn.setRequestorProperty("Cookie", "ASP.NET_SessionId=" + cookieSession + "; userid=" + cookieID); }
			conn.setRequestorProperty("Connection", "close");
			//conn.setPostData(codec.encodeText(postData.toCharArray(), 0, postData.length(), true, null));
			conn.setPostData(postData);
			Socket sock = conn.connect();
			// CharArray c_data = conn.readText(sock, codec);
			ByteArray daten = conn.readData(sock);
			CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			
			sock.close();
			
			if (withResponseHeaders)
				return getResponseHeaders(conn) + c_data.toString();
			else
				return c_data.toString();
		} catch (Exception e) {
			pref.log("[fetch_post] Ignored Exception", e, true);
		}
		return "";
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
		CacheHolderDetail chd = ch.getCacheDetails(false);
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
		if (ch.is_found() && chd.OwnLogId.equals("")) {
			ret = true;
		} // missing ownLogID
		boolean is_available_GC = !is_archived_GC
				&& CacheDescription.indexOf(propAvailable) == -1;
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
		String dts[]=mString.split(getDTS(CacheDescription),'/');
		if (dts.length == 3) {
			if (difficultyChanged(ch,  CacheTerrDiff.v1Converter(dts[0]))) {
				save = true;
				ret = true;
				pref.log("difficultyChanged");
			}
			if (terrainChanged(ch, CacheTerrDiff.v1Converter(dts[1]))) {
				save = true;
				ret = true;
				pref.log("terrainChanged");
			}
			if (sizeChanged(ch, (byte) Common.parseInt(dts[2]))) {
				save = true;
				ret = true;
				pref.log("sizeChanged");
			}
		}
		else {
			pref.log("check DTS calculation", null);
		}
		if (newFoundExists(ch,  CacheDescription)) {
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
			pref.log("check RexNumFinds in SpiderGC.java / initialiseProperties" 
					+ Preferences.NEWLINE + doc);
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
	
	private String decodeXor(String input, String key) {
	  byte ctmp[] = input.getBytes();
		byte ckey[] = key.getBytes();
		int codeLength = input.length();
		int keyLength = key.length();
		for (int i=0; i<codeLength; i++) {
		  ctmp[i]^=ckey[i%keyLength];
    }
		return new String(ctmp);
  }
	private String getDistanceAndDirection(String doc) {
		RexPropDistanceCode.search(doc);
		if (!RexPropDistanceCode.didMatch()) {
			pref.log("check distRex" + Preferences.NEWLINE + doc);
			return "";
		}
		String stmp = ewe.net.URL.decodeURL(RexPropDistanceCode.stringMatched(1));
		String ret = decodeXor( stmp, DistanceCodeKey);
		if (ret.indexOf("|") == -1) {
			// Versuch den DistanceCodeKey automatisch zu bestimmen
			// da dieser von gc mal wieder geändert wurde.
			// todo Benötigt ev noch weitere Anpassungen: | am Anfang, and calc of keylength
			String thereitis="|0.34 km|102.698";
			String page = fetchText("http://www.geocaching.com/seek/nearest.aspx?lat=48.48973&lng=009.26313&dist=2&f=1",false);

			RexPropListBlock.search(page);
			String table="";
			if (RexPropListBlock.didMatch()) {
				table = RexPropListBlock.stringMatched(1);
			}
			
			RexPropLine.search(table);
			String row="";
			if (RexPropLine.didMatch()) {
				row = RexPropLine.stringMatched(1);
			}

			RexPropDistanceCode.search(row);
			if (!RexPropDistanceCode.didMatch()) {
				pref.log("Didn't get DistanceCodeKey automaticly." + Preferences.NEWLINE);
				return "";
			}
			String coded = ewe.net.URL.decodeURL(RexPropDistanceCode.stringMatched(1));
			String newkey=decodeXor(coded,thereitis);
			int keylength=13; // wenn nicht 13 dann newkey auf wiederholung prüfen
			DistanceCodeKey=newkey.substring(0, keylength);
			ret = decodeXor( stmp, DistanceCodeKey);
		}
		return ret;
	}
	/**
	 * Get the Distance to the centre
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Distance
	 */
	private double getDistGC(String doc) throws Exception {
		RexPropDistance.search(doc); // km oder mi
		if (RexPropDistance.didMatch()) {
			if (MyLocale.getDigSeparator().equals(","))
				return Convert.toDouble(RexPropDistance.stringMatched(1)
						.replace('.', ','));
			return Convert.toDouble(RexPropDistance.stringMatched(1));
		}
		else {
			pref.log("(gc Code change ?) check distCodeKey in spider.def" + Preferences.NEWLINE + doc);
			return 0;			
		}
	}

	/**
	 * Get the waypoint name
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Name of waypoint to add to list
	 */
	private String getWP(String doc) throws Exception {
		RexPropWaypoint.search(doc);
		if (!RexPropWaypoint.didMatch()) {
			pref.log("check waypointRex in spider.def" + Preferences.NEWLINE + doc);
			return "???";
		}
		return "GC" + RexPropWaypoint.stringMatched(1);
	}

	/**
	 * check for Premium Member Cache
	 */
	private boolean doPMCache(String toCheck) {
		if (pref.isPremium)
			return true;
		if (toCheck.indexOf(propPM) <= 0) {
			return true;
		} else {
			numPrivate = numPrivate + 1;
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
		pref.log("check TypeRex in spider.def" + Preferences.NEWLINE + toCheck);
		return false;
	}
	
	private String getDTS(String toCheck) {
		RexPropDTS.search(toCheck);
		if (RexPropDTS.didMatch()) {
			String code=RexPropDTS.stringMatched(1);
			/* */
			String url = "http://www.geocaching.com/ImgGen/seek/CacheInfo.ashx?v="+code;
			ByteArray doc=fetch(url);
			Image idoc = new Image(doc,0,null,0,0);
			/*
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(new File("temp.png"));
				fos.write(doc.toBytes());
				fos.close();
			} catch (IOException e) {
			}
			finally {
			}
			*/
			return getDTfromImage(idoc) + "/" + getSizeFromImage(idoc);
			//*/

			/*
			int decoded = 0;
			int pwr = 1;
			for (int i = code.length()-1 ; i >= 0; i--) {
				decoded = decoded + DTSCodeKey.indexOf(code.substring(i,i+1)) * pwr;
				pwr = pwr * 42;
			}
			decoded = (decoded - 131586) % 16777216;
			// size 0=not choosen 1=Micro 3=Regular 5=Large 7=Virtual 8=Unknown 12=Small
			int sizecode = decoded / 74088; // 42 ^ 3
			int sizeremove;
			byte size;
			switch (sizecode) {
			case 0: size=CacheSize.CW_SIZE_NOTCHOSEN; sizeremove=0; break;
			case 1: size=CacheSize.CW_SIZE_MICRO; sizeremove=131072; break;
			case 3: size=CacheSize.CW_SIZE_REGULAR; sizeremove=262144; break;
			case 5: size=CacheSize.CW_SIZE_LARGE; sizeremove=393217; break;
			case 7: size=CacheSize.CW_SIZE_VIRTUAL; sizeremove=524288; break;
			case 8: size=CacheSize.CW_SIZE_OTHER; sizeremove=655360; break;
			case 12: size=CacheSize.CW_SIZE_SMALL; sizeremove=917504; break;
			default: size=CacheSize.CW_SIZE_ERROR; sizeremove=0; break;
			}
			decoded = decoded - sizeremove;
			int terraincode = decoded / 252;
			// terrain 0=1 1=1.5 2=2 3=2.5 4=3 5=3.5 6=4 7=4.5 8=5
			String terrain = "" + (1 + terraincode / 2.0 ); 
			// difficulty 0=1 1=1.5 2=2 3=2.5 4=3 5=3.5 6=4 7=4.5 8=5
			String difficulty = "" + (1+((decoded % 42) - (terraincode * 4)) / 2.0);
			if (difficulty.equals("0.5")) {
				difficulty = "5";
			}
			return difficulty+"/"+terrain+"/"+size;
			*/
		}
		pref.log("check DTSRex in spider.def" + Preferences.NEWLINE + toCheck);
		return "";
	}

	static Hashtable validChars = new Hashtable();
	
	static {
		validChars.put(".", new int[][] {
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 1, 0 },
				{0, 1, 0 }
		});
		validChars.put("/", new int[][] {
				{0, 0, 0, 0, 1},
				{0, 0, 0, 1, 0},
				{0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 1, 0, 0, 0},
				{0, 1, 0, 0, 0},
				{1, 0, 0, 0, 0}
		});
		validChars.put("1", new int[][] {
				{0, 0, 1, 0, 0},
				{1, 1, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{1, 1, 1, 1, 1}
		});
		validChars.put("2", new int[][] {
				{0, 1, 1, 1, 0},
				{1, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0},
				{0, 1, 0, 0, 0},
				{1, 0, 0, 0, 0},
				{1, 1, 1, 1, 1}
		});
		validChars.put("3", new int[][] {
				{0, 1, 1, 1, 0},
				{1, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{0, 0, 1, 1, 0},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{1, 0, 0, 0, 1},
				{0, 1, 1, 1, 0}
		});
		validChars.put("4", new int[][] {
				{0, 0, 0, 0, 1, 0},
				{0, 0, 0, 1, 1, 0},
				{0, 0, 1, 0, 1, 0},
				{0, 1, 0, 0, 1, 0},
				{1, 0, 0, 0, 1, 0},
				{1, 1, 1, 1, 1, 1},
				{0, 0, 0, 0, 1, 0},
				{0, 0, 0, 0, 1, 0}
		});
		validChars.put("5", new int[][] {
				{1, 1, 1, 1, 1},
				{1, 0, 0, 0, 0},
				{1, 0, 0, 0, 0},
				{1, 1, 1, 1, 0},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{1, 0, 0, 0, 1},
				{0, 1, 1, 1, 0}
		});
	}

	private static byte getSizeFromImage(Image bild) {
		int[] argb = bild.getPixels(null, 0, 5, 23, 1, 1, 0);
		if (argb[0] == -7005927) return CacheSize.CW_SIZE_MICRO;
		argb = bild.getPixels(null, 0, 10, 23, 1, 1, 0);
		if (argb[0] == -7005927) return CacheSize.CW_SIZE_SMALL;
		argb = bild.getPixels(null, 0, 17, 23, 1, 1, 0);
		if (argb[0] == -7005927) return CacheSize.CW_SIZE_REGULAR;
		argb = bild.getPixels(null, 0, 26, 23, 1, 1, 0);
		if (argb[0] == -7005927) return CacheSize.CW_SIZE_LARGE;
		argb = bild.getPixels(null, 0, 40, 23, 1, 1, 0);
		if (argb[0] == -6735302) return CacheSize.CW_SIZE_NOTCHOSEN;
		argb = bild.getPixels(null, 0, 41, 24, 1, 1, 0);
		if (argb[0] == -7005927) return CacheSize.CW_SIZE_OTHER;
		return CacheSize.CW_SIZE_ERROR;
	}

	private static String getDTfromImage(Image bild) {
		StringBuffer sb = new StringBuffer();
		for (int startX = 0; startX < bild.getWidth(); startX++) {
			for (Enumeration e = validChars.keys(); e.hasMoreElements();) {
				String key=(String) e.nextElement();
				if (testValidChar(bild, startX, 4, (int[][]) validChars.get(key))) {
					sb.append(key);
				}
			}
		}
		return sb.toString();
	}

	private static boolean testValidChar(Image bild, int startX, int startY, int[][] validChar) {
		for (int y = 0; y < validChar.length; y++) {
			if (bild.getHeight() > startY+y) {
				for (int x = 0; x < validChar[0].length; x++) {
					if (bild.getWidth() > startX+x) {
						// int[] alpha = bild.getAlphaRaster().getPixel(startX+x, startY+y, new int[1]);
						int[] argb = bild.getPixels(null, 0, startX+x, startY+y, 1, 1, 0);
						if ((argb[0] == 0 && validChar[y][x] == 0) ||
								(argb[0] != 0 && validChar[y][x] > 0)) {
							// matches
						} else {
							return false;
						} 
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
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

	/**
	 * Get the direction
	 * 
	 * @param doc String : A previously fetched cachepage
	 * @return direction String (degree)
	 */
	private String getDirection(String doc) throws Exception {
		String ret;
		String r="";
		if (doc.indexOf('|') >- 1) {
			r=mString.split(doc, '|')[2];
		}
		if (r.indexOf('.') > -1) {
			ret=mString.split(r,'.')[0];
		}
		else {
			ret="";
		}
		return ret;		
	}

	/*
	 * if cache lies in the desired direction
	 */
	private boolean directionOK(String[] directions, String gotDirection) {
		if (directions.length == 0)
			return true; // nothing means all
		int lowerLimit = Common.parseInt(directions[0]);
		int upperLimit = Common.parseInt(directions[1]);
		int toCheck = Common.parseInt(gotDirection);
		if (lowerLimit <= upperLimit) {
			if ((toCheck>=lowerLimit) && (toCheck<=upperLimit))
			{return true;}
			else {return false;}
		}
		else {
			if ((toCheck>=lowerLimit) || (toCheck<=upperLimit))
			{return true;}
			else {return false;}
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
		if (!pref.checkLog)
			return false;
		Time lastLogCW = new Time();
		Log lastLog = ch.getCacheDetails(true).CacheLogs.getLog(0);
		if (lastLog == null)
			return true;
		String slastLogCW = lastLog.getDate();
		if (slastLogCW.equals(""))
			return true; // or check cacheDescGC also no log?
		lastLogCW.parse(slastLogCW, "yyyy-MM-dd");

		Time lastLogGC = new Time(); // is current time
		lastLogGC.hour = 0;
		lastLogGC.minute = 0;
		lastLogGC.second = 0;
		lastLogGC.millis = 0;
		String[] SDate;
		String stmp = "";
		RexPropLogDate.search(cacheDescription);
		if (RexPropLogDate.didMatch()) {
			stmp = RexPropLogDate.stringMatched(1);
		} else {
			pref.log("check logDateRex in spider.def" + Preferences.NEWLINE + cacheDescription);
			return false;
		}
		if (stmp.indexOf("day") > 0) {
			// simplyfied (update if not newer than last week)
			lastLogGC.setTime(lastLogGC.getTime() - 691200000L); 
		} else if (stmp.equals("")) {
			return false; // no log yet
		} else {
			final String monthNames[] = { "January", "February", "March",
					"April", "May", "June", "July", "August", "September",
					"October", "November", "December" };
			SDate = mString.split(stmp, ' ');
			lastLogGC.day = Common.parseInt(SDate[0]);
			for (int m = 0; m < 12; m++) {
				if (monthNames[m].startsWith(SDate[1])) {
					lastLogGC.month = m + 1;
					m = 12;
				}
			}
			lastLogGC.year = 2000 + Common.parseInt(SDate[2].substring(0, 2));
		}
		boolean ret = lastLogCW.compareTo(lastLogGC) < 0;
		return ret;
	}
	private boolean TBchanged(CacheHolder ch, String cacheDescription) {
		// simplified Version: only presence is checked 
		if (pref.downloadTBs && pref.checkTBs ) {
			boolean hasTB=cacheDescription.indexOf("data-tbcount") > -1;
			return ch.has_bugs() != (hasTB);
		}
		return false;
	}

	/**
	 * Read a complete cachepage from geocaching.com including all logs. This is
	 * used both when updating already existing caches (via spiderSingle) and
	 * when spidering around a centre. It is also used when reading a GPX file
	 * and fetching the images.
	 * 
	 * This is the workhorse function of the spider.
	 * 
	 * @param CacheHolderDetail
	 *            chD The element wayPoint must be set to the name of a waypoint
	 * @param boolean isUpdate True if an existing cache is being updated, false
	 *        if it is a new cache
	 * @param boolean fetchImages True if the pictures are to be fetched
	 * @param boolean fetchTBs True if the TBs are to be fetched
	 * @param boolean doNotGetFound True if the cache is not to be spidered if
	 *        it has already been found
	 * @param boolean fetchAllLogs True if all logs are to be fetched (by adding
	 *        option '&logs=y' to command line). This is normally false when
	 *        spidering from GPXImport as the logs are part of the GPX file, and
	 *        true otherwise
	 * @return -1 if the infoBox was closed (cancel spidering), 0 if there was
	 *         an error (continue with next cache), 1 if everything ok
	 */
	private int getCacheByWaypointName(CacheHolder ch, boolean isUpdate,
			boolean fetchImages, boolean fetchTBs, boolean doNotGetFound,
			boolean fetchAllLogs) {
		int ret = SPIDER_OK; // initialize value;
		while (true) { // retry even if failure
			pref.log(""); // new line for more overview
			String completeWebPage;
			int spiderTrys = 0;
			int MAX_SPIDER_TRYS = 3;
			while (spiderTrys++ < MAX_SPIDER_TRYS) {
				ret = SPIDER_OK; // initialize value;
				try {
					String doc = p.getProp("getPageByName")
							+ ch.getWayPoint()
							+ ((fetchAllLogs || ch.is_found()) ? p
									.getProp("fetchAllLogs") : "");
					completeWebPage = fetchText(doc, false);
					pref.log("Fetched: " + ch.getWayPoint());
					if (completeWebPage.equals("")) {
						pref.log("Could not fetch " + ch.getWayPoint(), null);
						if (!infB.isClosed) {
							continue;
						} else {
							ch.setIncomplete(true);
							return SPIDER_CANCEL;
						}
					}
				} catch (Exception ex) {
					pref.log("Could not fetch " + ch.getWayPoint(), ex);
					if (!infB.isClosed) {
						continue;
					} else {
						ch.setIncomplete(true);
						return SPIDER_CANCEL;
					}
				}
				// Only analyse the cache data and fetch pictures if user has
				// not closed the progress window
				if (!infB.isClosed) {
					try {
						ch.initStates(!isUpdate);

						// first check if coordinates are available to prevent
						// deleting existing coordinates
						String latLon = getLatLon(completeWebPage);
						if (latLon.equals("???")) {
							if (completeWebPage.indexOf(p
									.getProp("premiumCachepage")) > 0) {
								// Premium cache spidered by non premium member
								pref.log("Ignoring premium member cache: " + ch.getWayPoint());
								spiderTrys = MAX_SPIDER_TRYS;
								ret = SPIDER_IGNORE_PREMIUM;
								continue;
							} else {
								if (spiderTrys == MAX_SPIDER_TRYS)
									pref.log(">>>> Failed to spider Cache. Retry." + completeWebPage, null);
								ret = SPIDER_ERROR;
								continue; // Restart the spider
							}
						}

						ch.setHTML(true);
						ch.setIncomplete(true);
						// Save size of logs to be able to check whether any new
						// logs were added
						// int logsz = chD.CacheLogs.size();
						// chD.CacheLogs.clear();
						ch.addiWpts.clear();
						ch.getCacheDetails(false).images.clear();

						ch.setAvailable(!(completeWebPage.indexOf(p
								.getProp("cacheUnavailable")) >= 0));
						ch.setArchived(completeWebPage.indexOf(p
								.getProp("cacheArchived")) >= 0);
						// ==========
						// Logs first (for check early for break)
						// ==========
						getLogs(completeWebPage, ch.getCacheDetails(false));
						pref.log("Got logs");
						// If the switch is set to not store found caches and we
						// found the cache => return
						if (ch.is_found() && doNotGetFound) {
							if (infB.isClosed) {
								return SPIDER_CANCEL;
							} else {
								return SPIDER_IGNORE;
							}
						}
						// ==========
						// General Cache Data
						// ==========
						ch.setLatLon(latLon);
						pref.log("LatLon: " + ch.getLatLon());

						String longDesc = getLongDesc(completeWebPage);
						ch.getCacheDetails(false).setLongDescription(longDesc);
						pref.log("Got description");

						ch.setCacheName(SafeXML
								.cleanback(getName(completeWebPage)));
						pref.log("Name: " + ch.getCacheName());

						String location = getLocation(completeWebPage);
						if (location.length() != 0) {
							int countryStart = location.indexOf(",");
							if (countryStart > -1) {
								ch.getCacheDetails(false).Country = SafeXML
										.cleanback(location.substring(
												countryStart + 1).trim());
								ch.getCacheDetails(false).State = SafeXML
										.cleanback(location.substring(0,
												countryStart).trim());
							} else {
								ch.getCacheDetails(false).Country = location
										.trim();
								ch.getCacheDetails(false).State = "";
							}
							pref.log("Got location (country/state)");
						} else {
							ch.getCacheDetails(false).Country = "";
							ch.getCacheDetails(false).State = "";
							pref.log("No location (country/state) found");
						}

						ch.setCacheOwner(SafeXML.cleanback(
								getOwner(completeWebPage)).trim());
						if (ch.getCacheOwner().equals(pref.myAlias)
								|| (pref.myAlias2.length() > 0 && ch
										.getCacheOwner().equals(pref.myAlias2)))
							ch.setOwned(true);
						pref.log("Owner: " + ch.getCacheOwner()
								+ "; is_owned = " + ch.is_owned()
								+ ";  alias1,2 = [" + pref.myAlias + "|"
								+ pref.myAlias2 + "]");

						ch.setDateHidden(DateFormat
								.MDY2YMD(getDateHidden(completeWebPage)));
						pref.log("Hidden: " + ch.getDateHidden());

						ch.getCacheDetails(false).setHints(
								getHints(completeWebPage));
						pref.log("Hints: " + ch.getCacheDetails(false).Hints);

						ch.setCacheSize(CacheSize
								.gcSpiderString2Cw(getSize(completeWebPage)));
						pref.log("Size: " + ch.getCacheSize());

						ch.setHard(CacheTerrDiff.v1Converter(getDiff(completeWebPage)));
						pref.log("Hard: " + ch.getHard());

						ch.setTerrain(CacheTerrDiff
								.v1Converter(getTerr(completeWebPage)));
						pref.log("Terr: " + ch.getTerrain());

						ch.setType(getType(completeWebPage));
						pref.log("Type: " + ch.getType());
						// ==========
						// Bugs
						// ==========
						if (fetchTBs)
							getBugs(ch.getCacheDetails(false), completeWebPage);
						ch.setHas_bugs(ch.getCacheDetails(false).Travelbugs
								.size() > 0);
						pref.log("Got TBs");
						// ==========
						// Images
						// ==========
						if (fetchImages) {
							getImages(completeWebPage, ch.getCacheDetails(false),true);
							pref.log("Got images");
						}
						// ==========
						// Addi waypoints
						// ==========
						getAddWaypoints(completeWebPage, ch.getWayPoint(), ch
								.is_found());
						pref.log("Got additional waypoints");
						// ==========
						// Attributes
						// ==========
						getAttributes(completeWebPage, ch
								.getCacheDetails(false));
						pref.log("Got attributes");
						// ==========
						// Last sync date
						// ==========
						ch.setLastSync((new Time()).format("yyyyMMddHHmmss"));
						ch.setIncomplete(false);
						pref.log("ready " + ch.getWayPoint() + " : " + ch.getLastSync());
						break;
					} catch (Exception ex) {
						pref.log("[getCacheByWaypointName: ]Error reading cache: " + ch.getWayPoint(), ex);
					}
				} else {
					break;
				}
			} // spiderTrys
			if ((spiderTrys >= MAX_SPIDER_TRYS) && (ret == SPIDER_OK)) {
				pref.log(">>> Failed to spider cache. Number of retrys exhausted.", null);
				int decision = (new MessageBox(MyLocale.getMsg(5500, "Error"),
						MyLocale.getMsg(5515,"Failed to load cache.%0aPleas check your internet connection.%0aRetry?"),
						FormBase.DEFOKB | FormBase.NOB | FormBase.CANCELB))
						.execute();
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
		if (infB.isClosed) {// If the infoBox was closed before getting here, we
							// return -1
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
		Regex inRex = new Regex(p.getProp("latLonRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check latLonRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
		Regex shortDescRex = new Regex(p.getProp("shortDescRex"));
		Regex longDescRex = new Regex(p.getProp("longDescRex"));
		shortDescRex.search(doc);
		if (!shortDescRex.didMatch()) {
			if (shortDescRex_not_yet_found)
				pref.log("no shortDesc or check shortDescRex in spider.def");
				//		+ Preferences.NEWLINE + doc);
		} else {
			res = shortDescRex.stringMatched(1);
			shortDescRex_not_yet_found = false;
		}
		res += "<br>";
		longDescRex.search(doc);
		if (!longDescRex.didMatch()) {
			pref.log("check longDescRex in spider.def" 
					+ Preferences.NEWLINE + doc);
		} else {
			res += longDescRex.stringMatched(1);
		}
		int spanEnd = res.lastIndexOf("</span>");
		if (spanEnd >= 0) {
			res = res.substring(0, spanEnd);
		}
		return SafeXML.cleanback(res); // since internal viewer doesn't show
										// html-entities that are now in
										// cacheDescription
	}

	/**
	 * Get the cache location (country and state)
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the location (country and state) of the cache
	 */
	private String getLocation(String doc) throws Exception {
		Regex inRex = new Regex(p.getProp("cacheLocationRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check cacheLocationRex in spider.def"
					+ Preferences.NEWLINE + doc);
			return "";
		}
		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache name
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the name of the cache
	 */
	private String getName(String doc) throws Exception {
		Regex inRex = new Regex(p.getProp("cacheNameRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check cacheNameRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("cacheOwnerRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check cacheOwnerRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("dateHiddenRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check dateHiddenRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("hintsRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) {
			pref.log("check hintsRex in spider.def"
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("sizeRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else {
			pref.log("check sizeRex in spider.def"
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("difficultyRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else {
			pref.log("check difficultyRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
		Regex inRex = new Regex(p.getProp("terrainRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else {
			pref.log("check terrainRex in spider.def" 
					+ Preferences.NEWLINE + doc);
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
			pref.log("check cacheTypeRex in spider.def" 
					+ Preferences.NEWLINE + doc);
			return 0;
		}
	}

	/**
	 * Get the logs
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @param chD
	 *            Cache Details
	 * @return A HTML string containing the logs
	 */
	private void getLogs(String completeWebPage, CacheHolderDetail chD)
			throws Exception {
		String icon = "";
		String name = "";
		String logText = "";
		String logId = "";
		String singleLog = "";
		LogList reslts = chD.CacheLogs;
		RexLogBlock.search(completeWebPage);
		if (!RexLogBlock.didMatch()) {
			pref.log("check blockRex in spider.def" 
					+ Preferences.NEWLINE + completeWebPage);
		}
		String LogBlock = RexLogBlock.stringMatched(1);
		exSingleLog.setSource(LogBlock);
		singleLog = exSingleLog.findNext();
		exIcon.setSource(singleLog);
		exNameTemp.setSource(singleLog);
		exName.setSource(exNameTemp.findNext());
		exDate.setSource(singleLog);
		exLog.setSource(singleLog);
		exLogId.setSource(singleLog);
		int nLogs = 0;
		boolean foundown = false;
		while (!exSingleLog.endOfSearch()) {
			// pref.log(singleLog);
			nLogs++;
			icon = exIcon.findNext();
			name = exName.findNext();
			logText = exLog.findNext();
			logId = exLogId.findNext();
			String d = DateFormat.logdate2YMD(exDate.findNext());
			// pref.log(Integer.toString(nLogs)+":"+icon+"|logger:"+name+"|id:"+logId+"|"+d);
			// if this log says this Cache is found by me
			if ((icon.equals(icon_smile) || icon.equals(icon_camera) || icon
					.equals(icon_attended))
					&& (name.equalsIgnoreCase(SafeXML.clean(pref.myAlias)) || (pref.myAlias2
							.length() > 0 && name.equalsIgnoreCase(SafeXML
							.clean(pref.myAlias2))))) {
				chD.getParent().setFound(true);
				chD.getParent().setCacheStatus(d);
				chD.OwnLogId = logId;
				chD.OwnLog = new Log(icon, d, name, logText);
				foundown = true;
				// pref.log("own log detected!");
			}
			if (nLogs <= pref.maxLogsToSpider) {
				reslts.add(new Log(icon, d, name, logText));
			} else {
				if (foundown) {
					break;
				}
			}
			singleLog = exSingleLog.findNext();
			exIcon.setSource(singleLog);
			exNameTemp.setSource(singleLog);
			exName.setSource(exNameTemp.findNext());
			exDate.setSource(singleLog);
			exLog.setSource(singleLog);
			exLogId.setSource(singleLog);
		}
		if (nLogs > pref.maxLogsToSpider) {
			reslts.add(Log.maxLog());
			// pref.log("MAXLOGS reached ("+pref.maxLogsToSpider+")");
		}
		// pref.log(nLogs+" checked!");
		// return reslts;
	}

	/**
	 * Read the travelbug names from a previously fetched Cache page and then
	 * read the travelbug purpose for each travelbug
	 * 
	 * @param doc
	 *            The previously fetched cachepage
	 * @return A HTML formatted string with bug names and there purpose
	 */
	public void getBugs(CacheHolderDetail chD, String doc) throws Exception {
		Extractor exBlock = new Extractor(doc, 
				p.getProp("blockExStart"), 
				p.getProp("blockExEnd"), 
				0, Extractor.EXCLUDESTARTEND);
		String bugBlock = exBlock.findNext();
		Extractor exBug = new Extractor(bugBlock, 
				p.getProp("bugExStart"), 
				p.getProp("bugExEnd"), 
				0, Extractor.EXCLUDESTARTEND);
		String link, bug, linkPlusBug, bugDetails;
		String oldInfoBox = infB.getInfo();
		chD.Travelbugs.clear();
		while (!exBug.endOfSearch()) {
			if (infB.isClosed)
				break; // Allow user to cancel by closing progress form
			linkPlusBug = exBug.findNext();
			int idx = linkPlusBug.indexOf(p.getProp("bugLinkEnd"));
			if (idx < 0)
				break; // No link/bug pair found
			link = linkPlusBug.substring(0, idx);
			Extractor exBugName = new Extractor(linkPlusBug, 
					p.getProp("bugNameExStart"), 
					p.getProp("bugNameExEnd"), 
					0, Extractor.EXCLUDESTARTEND);
			bug = exBugName.findNext();
			if (bug.length() > 0) { // Found a bug, get its details
				Travelbug tb = new Travelbug(bug);
				try {
					infB.setInfo(oldInfoBox
							+ MyLocale.getMsg(5514, "\nGetting bug: ")
							+ SafeXML.cleanback(bug));
					bugDetails = fetchText(link, false);
					pref.log("[getBugs] Fetched TB details: " + bug);
					Extractor exDetails = new Extractor(bugDetails, p
							.getProp("bugDetailsStart"), p
							.getProp("bugDetailsEnd"),
							0, Extractor.EXCLUDESTARTEND);
					tb.setMission(exDetails.findNext());
					Extractor exGuid = new Extractor(bugDetails,
							"action=\"details.aspx?guid=",
							"\" id=\"aspnetForm",
							0, Extractor.EXCLUDESTARTEND);
					tb.setGuid(exGuid.findNext());
					chD.Travelbugs.add(tb);
				} catch (Exception ex) {
					pref.log("[getBugs] Could not fetch bug details", ex);
				}
			}
		}
		infB.setInfo(oldInfoBox);
	}

	/**
	 * Get the images for a previously fetched cache page. Images are extracted
	 * from two areas: The long description and the pictures section (including
	 * the spoiler)
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
		Vector spideredUrls = new Vector(15);
		ImageInfo imageInfo = null;
		Extractor exImgBlock, exImgComment;
		int idxUrl; // Index of already spidered Url in list of spideredUrls
		CacheImages lastImages = null;

		// First: Get current image object of waypoint before spidering images.
		CacheHolder oldCh = Global.getProfile().cacheDB.get(chD.getParent().getWayPoint());
		if (oldCh != null) {
			lastImages = oldCh.getCacheDetails(false).images;
		}

		// ========
		// In the long description
		// ========
		String longDesc = "";
		try {
			if (chD.getParent().getWayPoint().startsWith("TC") || !extractLongDesc)
				longDesc = doc;
			else
				longDesc = getLongDesc(doc);
			longDesc = STRreplace.replace(longDesc, "<img", "<IMG");
			longDesc = STRreplace.replace(longDesc, "src=", "SRC=");
			longDesc = STRreplace.replace(longDesc, "'", "\"");
			exImgBlock = new Extractor(longDesc, 
					p.getProp("imgBlockExStart"),
					p.getProp("imgBlockExEnd"), 
					0, false);
		} catch (Exception ex) {// Missing property in spider.def
			return;
		}
		String tst;
		tst = exImgBlock.findNext();
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while (exImgBlock.endOfSearch() == false) {
			imgUrl = exImgSrc.findNext();
			if (imgUrl.length() > 0) {
				// Optimize: img.groundspeak.com -> img.geocaching.com (for
				// better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf('.'))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with .
					if (imgType.startsWith(".png")
							|| imgType.startsWith(".jpg")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_"
								+ Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl < 0) { // New image
							fileName = chD.getParent().getWayPoint()
									.toLowerCase()
									+ "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl,
										fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("[getImages] Already exising image: " + imgUrl + " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint()
									.toLowerCase()
									+ "_" + Convert.toString(idxUrl);
							pref.log("[getImages] Already loaded image: " + imgUrl + " as " + fileName + imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName + imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(imgName);
						imageInfo.setComment(null);
						imgCounter++;
						chD.images.add(imageInfo);
					}
				} catch (IndexOutOfBoundsException e) {
					pref.log("[getImages] Problem loading image. imgURL:" + imgUrl, e);
				}
			}
			exImgSrc.setSource(exImgBlock.findNext());
		}
		// ========
		// In the image span
		// ========
		Extractor spanBlock, exImgName;
		try {
			spanBlock = new Extractor(doc, p.getProp("imgSpanExStart"), 
					p.getProp("imgSpanExEnd"), 0, true);
			tst = spanBlock.findNext();
			exImgName = new Extractor(tst, p.getProp("imgNameExStart"), 
					p.getProp("imgNameExEnd"), 0, true);
			exImgSrc = new Extractor(tst, p.getProp("imgSrcExStart"), 
					p.getProp("imgSrcExEnd"), 0, true);
			exImgComment = new Extractor(tst, p.getProp("imgCommentExStart"), 
					p.getProp("imgCommentExEnd"), 0, true);
		} catch (Exception ex) {
			return;
		}
		while (!exImgSrc.endOfSearch()) {
			imgUrl = exImgSrc.findNext();
			imgComment = exImgComment.findNext();
			if (imgUrl.length() > 0) {
				imgUrl = "http://" + imgUrl;
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf('.'))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with .
					if (imgType.startsWith(".png")
							|| imgType.startsWith(".jpg")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_"
								+ Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl < 0) { // New image
							fileName = chD.getParent().getWayPoint()
									.toLowerCase()
									+ "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl,
										fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("[getImages] Already exising image: " + imgUrl + " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(idxUrl);
							pref.log("[getImages] Already loaded image: " + imgUrl + " as " + fileName + imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName + imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(exImgName.findNext());
						while (imgComment.startsWith("<br />"))
							imgComment = imgComment.substring(6);
						while (imgComment.endsWith("<br />"))
							imgComment = imgComment.substring(0, imgComment.length() - 6);
						imageInfo.setComment(imgComment);
						chD.images.add(imageInfo);
					}
				} catch (IndexOutOfBoundsException e) {
					pref.log("[getImages] IndexOutOfBoundsException in image span. imgURL:" + imgUrl, e);
				}
			}
		}
		// ========
		// Final sweep to check for images in hrefs
		// ========
		Extractor exFinal = new Extractor(longDesc, "http://", "\"", 0, true);
		while (!exFinal.endOfSearch()) {
			imgUrl = exFinal.findNext();
			if (imgUrl.length() > 0) {
				// Optimize: img.groundspeak.com -> img.geocaching.com (for
				// better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf('.'))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with . Delete
					// characters in URL after the image extension
					imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.')
							+ imgType.length());
					if (imgType.startsWith(".jpg")
							|| imgType.startsWith(".bmp")
							|| imgType.startsWith(".png")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						if (idxUrl < 0) { // New image
							imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
							fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl, fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("[getImages] Loading image: " + imgUrl + " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("[getImages] Already exising image: " + imgUrl + " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
							imageInfo.setTitle(imgName);
							imgCounter++;
							chD.images.add(imageInfo);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					pref.log("[getImages] Problem loading image. imgURL:" + imgUrl, e);
				}
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
	private void spiderImage(String imgUrl, String target) {
		// TODO implement a fetch(URL, filename) in HttpConnection and use that one
		HttpConnection connImg;
		Socket sockImg;
		// InputStream is;
		FileOutputStream fos;
		// int bytes_read;
		// byte[] buffer = new byte[9000];
		ByteArray daten;
		String datei = "";
		datei = profile.dataDir + target;
		connImg = new HttpConnection(imgUrl);
		if (imgUrl.indexOf('%') >= 0)
			connImg.documentIsEncoded = true;
		connImg.setRequestorProperty("Connection", "close");
		// connImg.setRequestorProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12");
		// connImg.setRequestorProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		try {
			pref.log("[spiderImage] Trying to fetch image from: " + imgUrl);
			String redirect = null;
			do {
				sockImg = connImg.connect();
				redirect = connImg.getRedirectTo();
				if (redirect != null) {
					connImg = connImg.getRedirectedConnection(redirect);
					pref.log("[spiderImage] Redirect to " + redirect);
				}
			} while (redirect != null); 
			// TODO this can end up in an endless loop if trying to load from a malicous site
			daten = connImg.readData(sockImg);
			fos = new FileOutputStream(new File(datei));
			fos.write(daten.toBytes());
			fos.close();
			sockImg.close();
		} catch (UnknownHostException e) {
			pref.log("[spiderImage] Host not there...", e);
		} catch (IOException ioex) {
			pref.log("[spiderImage] File not found!", ioex);
		} catch (Exception ex) {
			pref.log("[spiderImage] Some other problem while fetching image", ex);
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
	 *            Found status of the cached (is inherited by the additional
	 *            waypoints)
	 */
	boolean koords_not_yet_found = true;

	private void getAddWaypoints(String doc, String wayPoint, boolean is_found)
			throws Exception {
		Extractor exWayBlock = new Extractor(doc, 
				p.getProp("wayBlockExStart"),
				p.getProp("wayBlockExEnd"), 
				0, false);
		String wayBlock = "";
		String rowBlock = "";
		wayBlock = exWayBlock.findNext();
		Regex nameRex = new Regex(p.getProp("nameRex"));
		Regex koordRex = new Regex(p.getProp("koordRex"));
		Regex descRex = new Regex(p.getProp("descRex"));
		Regex typeRex = new Regex(p.getProp("typeRex"));
		int counter = 0;
		if (!exWayBlock.endOfSearch()
				&& wayBlock.indexOf("No additional waypoints to display.") < 0) {
			Extractor exRowBlock = new Extractor(wayBlock, 
					p.getProp("rowBlockExStart"), 
					p.getProp("rowBlockExEnd"),
					0, false);
			rowBlock = exRowBlock.findNext();
			rowBlock = exRowBlock.findNext();
			while (!exRowBlock.endOfSearch()) {
				CacheHolder hd = null;

				/*
				 * String[] AddiBlock=mString.split(rowBlock,'\n'); int
				 * linePrefix=8; if(AddiBlock.length < linePrefix + 1) { (new
				 * MessageBox(MyLocale.getMsg(5500,"Error"),
				 * "GC changed table output \nCW must be changed too!",
				 * FormBase.OKB)).execute(); break; } String
				 * prefix=AddiBlock[linePrefix].trim();
				 */

				// Extractor exPrefix=new
				// Extractor(AddiBlock[linePrefix].trim(),p.getProp("prefixExStart"),p.getProp("prefixExEnd"),0,true);
				Extractor exPrefix = new Extractor(rowBlock, 
						p.getProp("prefixExStart"),
						p.getProp("prefixExEnd"), 
						0, true);
				String prefix = exPrefix.findNext();

				String adWayPoint;
				if (prefix.length() == 2)
					adWayPoint = prefix + wayPoint.substring(2);
				else
					adWayPoint = MyLocale.formatLong(counter, "00")
							+ wayPoint.substring(2);
				counter++;
				int idx = profile.getCacheIndex(adWayPoint);
				if (idx >= 0) {
					// Creating new CacheHolder, but accessing old cache.xml
					// file
					hd = new CacheHolder();
					hd.setWayPoint(adWayPoint);
					hd.getCacheDetails(true); // Accessing Details reads file if
												// not yet done
				} else {
					hd = new CacheHolder();
					hd.setWayPoint(adWayPoint);
				}
				hd.initStates(idx < 0);
				nameRex.search(rowBlock);
				if (nameRex.didMatch()) {
					hd.setCacheName(nameRex.stringMatched(1));
				} else {
					pref.log("check nameRex in spider.def"
							+ Preferences.NEWLINE + rowBlock);
				}
				koordRex.search(rowBlock);
				typeRex.search(rowBlock);
				if (koordRex.didMatch()) {
					hd.setLatLon(koordRex.stringMatched(1));
					koords_not_yet_found = false;
				} else {
					if (koords_not_yet_found)
						pref.log("check koordRex in spider.def"
								+ Preferences.NEWLINE + rowBlock);
				}
				if (typeRex.didMatch()) {
					hd.setType(CacheType.gpxType2CwType("Waypoint|"
							+ typeRex.stringMatched(1)));
				} else {
					pref.log("check typeRex in spider.def"
							+ Preferences.NEWLINE + rowBlock);
				}
				rowBlock = exRowBlock.findNext();
				descRex.search(rowBlock);
				if (descRex.didMatch()) {
					hd.getCacheDetails(false).setLongDescription(
							descRex.stringMatched(1).trim());
				} else {
					pref.log("check descRex in spider.def"
							+ Preferences.NEWLINE + rowBlock);
				}
				hd.setFound(is_found);
				hd.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				hd.setHard(CacheTerrDiff.CW_DT_UNSET);
				hd.setTerrain(CacheTerrDiff.CW_DT_UNSET);
				if (idx < 0) {
					cacheDB.add(hd);
					hd.save();
				} else {
					CacheHolder cx = cacheDB.get(idx);
					if (cx.is_Checked && // Only re-spider existing addi
											// waypoints that are ticked
							cx.isVisible()) { // and are visible (i.e. not
												// filtered)
						cx.initStates(false);
						cx.update(hd);
						cx.is_Checked = true;
						cx.save();
					}
				}
				rowBlock = exRowBlock.findNext();

			}
		}
	}

	public void getAttributes(String doc, CacheHolderDetail chD)
			throws Exception {
		Extractor attBlock = new Extractor(doc, p.getProp("attBlockExStart"), 
				p.getProp("attBlockExEnd"), 0, true);
		String atts = attBlock.findNext();
		Extractor attEx = new Extractor(atts, p.getProp("attExStart"), 
				p.getProp("attExEnd"), 0, true);
		String attribute = attEx.findNext();
		chD.attributes.clear();
		while (!attEx.endOfSearch()) {
			chD.attributes.add(attribute);
			attribute = attEx.findNext();
		}
		chD.getParent().setAttribsAsBits(chD.attributes.getAttribsAsBits());
	}

	public static String fetchText(String address, boolean withResponseHeaders) {
		ByteArray daten = fetch(address);
		if (daten == null) return "";
		try {
			JavaUtf8Codec codec = new JavaUtf8Codec();
			CharArray c_data;
			c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			if (withResponseHeaders)
				return getResponseHeaders(conn) + c_data.toString();
			else
				return c_data.toString();
		} catch (IOException e) {
			pref.log("IOException in fetch", e);
			return null;
		}
	}

	/**
	 * Performs an initial fetch to a given address. 
	 */
	private static ByteArray fetch(String address) {
		try {
			if (pref.myproxy.length() > 0 && pref.proxyActive) {
				pref.log("[fetch]:Using proxy: " + pref.myproxy + " / " + pref.myproxyport);
			}
			conn = new HttpConnection(address);
			conn.setRequestorProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			if (cookieSession.length() > 0) {conn.setRequestorProperty("Cookie", "ASP.NET_SessionId=" + cookieSession + "; userid=" + cookieID);};
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			Socket sock = conn.connect();
			pref.log("[fetch]:Connect ok! " + address);
			ByteArray daten = conn.readData(sock);
			sock.close();
			return daten;
		} catch (IOException ioex) {
			pref.log("IOException in fetch", ioex);
			return null;
		}
	}

	/**
	 * After a fetch to gc.com the next fetches have to use the post method.
	 * This method does exactly that. Actually this method is generic in the
	 * sense that it can be used to post to a URL using http post.
	 */
	private static String fetch_post(String address, String postData, boolean withResponseHeaders) {
		try {
			HttpConnection conn = new HttpConnection(address);
			JavaUtf8Codec codec = new JavaUtf8Codec();
			conn.documentIsEncoded = true;
			conn.setRequestorProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setPostData(codec.encodeText(postData.toCharArray(), 0, postData.length(), true, null));
			conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
			if (cookieSession.length() > 0) { conn.setRequestorProperty("Cookie", "ASP.NET_SessionId=" + cookieSession + "; userid=" + cookieID);};
			conn.setRequestorProperty("Connection", "close");
			Socket sock = conn.connect();
			pref.log("[fetch_post]:Connect ok! " + address);
			// ByteArray daten = conn.readData(sock);
			CharArray c_data = conn.readText(sock, codec);
			pref.log("[fetch_post]:Read data ok " + address);
			// CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			sock.close();
			if (withResponseHeaders)
				return getResponseHeaders(conn) + c_data.toString();
			else
				return c_data.toString();
		} catch (Exception e) {
			pref.log("[fetch_post] Ignored Exception", e, true);
		}
		return "";
	}

	private static String getResponseHeaders(HttpConnection conn) {
		PropertyList pl = conn.documentProperties;
		if (pl != null) {
			StringBuffer sb = new StringBuffer(1000);
			boolean gotany = false;

			for (int i = 0; i < pl.size(); i++) {
				Property currProp = (Property) pl.get(i);
				if (currProp.value != null) {
					sb.append(currProp.name).append(": ")
							.append(currProp.value).append("\r\n");
					gotany = true;
				}
			}
			if (gotany)
				return sb.toString() + "\r\n";
		}
		return "";
	}

	final static String hex = ewe.util.TextEncoder.hex;

	public String encodeUTF8URL(byte[] what) {
		int max = what.length;
		char[] dest = new char[6 * max]; // Assume each char is a UTF char and
											// encoded into 6 chars
		char d = 0;
		for (int i = 0; i < max; i++) {
			char c = (char) what[i];
			if (c <= ' ' || c == '+' || c == '&' || c == '%' || c == '='
					|| c == '|' || c == '{' || c == '}' || c > 0x7f) {
				dest[d++] = '%';
				dest[d++] = hex.charAt((c >> 4) & 0xf);
				dest[d++] = hex.charAt(c & 0xf);
			} else
				dest[d++] = c;
		}
		return new String(dest, 0, d);
	}

	/**
	 * Load the bug id for a given name. This method is not ideal, as there are
	 * sometimes several bugs with identical names but different IDs. Normally
	 * the bug GUID is used which can be obtained from the cache page.<br>
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
			bugList = fetchText(p.getProp("getBugByName") + STRreplace.replace(SafeXML.clean(name), " ", "+"), false);
			pref.log("[getBugId] Fetched bugId: " + name);
		} catch (Exception ex) {
			pref.log("[getBugId] Could not fetch bug list" + name, ex);
			bugList = "";
		}
		try {
			if (bugList.equals("")
					|| bugList.indexOf(p.getProp("bugNotFound")) >= 0) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"), 
						MyLocale.getMsg(6020, "Travelbug not found."),
						FormBase.OKB)).execute();
				return "";
			}
			if (bugList.indexOf(p.getProp("bugTotalRecords")) < 0) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"),
						MyLocale.getMsg(6021, "More than one travelbug found. Specify name more precisely."),
						FormBase.OKB)).execute();
				return "";
			}
			Extractor exGuid = new Extractor(bugList, 
					p.getProp("bugGuidExStart"), 
					p.getProp("bugGuidExEnd"), 
					0, Extractor.EXCLUDESTARTEND); 
			// TODO Replace with spider.def
			return exGuid.findNext();
		} catch (Exception ex) {
			pref.log("[getBugId] Error getting TB", ex);
			return "";
		}
	}

	/**
	 * Fetch a bug's mission for a given GUID or ID. If the guid String is
	 * longer than 10 characters it is assumed to be a GUID, otherwise it is an
	 * ID.
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
				bugDetails = fetchText(p.getProp("getBugByGuid") + guid, false);
			else
				bugDetails = fetchText(p.getProp("getBugById") + guid, false);
			pref.log("[getBugMissionByGuid] Fetched TB detailsById: " + guid);
		} catch (Exception ex) {
			pref.log("[getBugMissionByGuid] Could not fetch TB details " + guid, ex);
			bugDetails = "";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"),
						MyLocale.getMsg(6020, "Travelbug not found."),
						FormBase.OKB)).execute();
				return "";
			}
			Extractor exDetails = new Extractor(bugDetails, 
					p.getProp("bugDetailsStart"), 
					p.getProp("bugDetailsEnd"),
					0, Extractor.EXCLUDESTARTEND);
			return exDetails.findNext();
		} catch (Exception ex) {
			pref.log("[getBugMissionByGuid] Error getting TB " + guid, ex);
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
			bugDetails = fetchText(p.getProp("getBugByTrackNr") + trackNr, false);
			pref.log("[getBugMissionByTrackNr] Fetched bug detailsByTrackNr: " + trackNr);
		} catch (Exception ex) {
			pref.log("[getBugMissionByTrackNr] getBugByTrackNr " + trackNr, ex);
			bugDetails = "";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
				pref.log("[getBugMissionByTrackNr], bugNotFound " + trackNr, null);
				// (new MessageBox(MyLocale.getMsg(5500,"Error"),
				// MyLocale.getMsg(6020,"Travelbug not found."),
				// MessageBox.OKB)).execute();
				return "";
			}
			Extractor exDetails = new Extractor(bugDetails, 
					p.getProp("bugDetailsStart"), 
					p.getProp("bugDetailsEnd"),
					0, Extractor.EXCLUDESTARTEND);
			return exDetails.findNext();
		} catch (Exception ex) {
			pref.log("[getBugMissionByTrackNr] TB Details, bugNotFound " + trackNr, ex);
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
		String trackNr = TB.getTrackingNo();
		try {
			bugDetails = fetchText(p.getProp("getBugByTrackNr") + trackNr, false);
			pref.log("[getBugMissionAndNameByTrackNr] Fetched TB getBugByTrackNr: " + trackNr);
		} catch (Exception ex) {
			pref.log("[getBugMissionAndNameByTrackNr] Could not fetch bug details: " + trackNr, ex);
			bugDetails = "";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
				pref.log("[getBugMissionAndNameByTrackNr], bugNotFound: " + trackNr, null);
				// (new MessageBox(MyLocale.getMsg(5500,"Error"),
				// MyLocale.getMsg(6020,"Travelbug not found."),
				// MessageBox.OKB)).execute();
				return false;
			}
			Extractor exDetails = new Extractor(bugDetails, 
					p.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0,
					Extractor.EXCLUDESTARTEND);
			TB.setMission(exDetails.findNext());
			Extractor exName = new Extractor(bugDetails, 
					p.getProp("bugNameStart"), p.getProp("bugNameEnd"), 0,
					Extractor.EXCLUDESTARTEND);
			TB.setName(exName.findNext());
			return true;
		} catch (Exception ex) {
			pref.log("[getBugMissionAndNameByTrackNr] TB Details, bugNotFound: " + trackNr, ex);
			return false;
		}
	}

	public class SpiderProperties extends Properties {
		SpiderProperties() {
			super();
			try {
				load(new FileInputStream(FileBase.getProgramDirectory() + "/spider.def"));
			} catch (Exception ex) {
				pref.log("Failed to load spider.def from " + FileBase.getProgramDirectory(), ex);
				(new MessageBox(MyLocale.getMsg(5500, "Error"), 
						MyLocale.getMsg(5504, "Could not load 'spider.def'"),
						FormBase.OKB)).execute();
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
			String s = super.getProperty(key);
			if (s == null) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"),
						MyLocale.getMsg(5497, "Error missing tag in spider.def")+ ": " + key,
						FormBase.OKB)).execute();
				pref.log("Missing tag in spider.def: " + key);
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
			FileInputStream rFIS = null;
			InputStreamReader r = null;
			try {
				rFIS = new ewe.io.FileInputStream(_fileName);
				r = new ewe.io.InputStreamReader(rFIS);
				if (r.read() != 65279) {
					r.close();
					rFIS.close();
					rFIS = new ewe.io.FileInputStream(_fileName);
					r = new ewe.io.InputStreamReader(rFIS);
				}
				parse(r);
				r.close();
				rFIS.close();
			} catch (Exception e) {
			}
			return _routePoints;
		}

		public void startElement(String name, AttributeList atts) {
			if (name.equals("trkpt")|| name.equals("rtept")) {
				double lat = Common.parseDouble(atts.getValue("lat"));
				double lon = Common.parseDouble(atts.getValue("lon"));
				TrackPoint tp = new TrackPoint(lat, lon);
				if (tp.isValid())
					_routePoints.add(tp);
				return;
			}
		}

		public void endElement(String name) {

		}

	}
}
