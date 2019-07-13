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

import CacheWolf.*;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.*;
import CacheWolf.navi.MovingMap;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.Track;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.*;
import com.stevesoft.ewe_pat.Regex;
import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.net.URL;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.*;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class to spider caches from gc.com
 */
public class GCImporter {
    // Return values for spider action
    /**
     * Ignoring a premium member cache when spidering from a non premium account
     */
    public final static int SPIDER_IGNORE_PREMIUM = -2;
    /**
     * Canceling spider process
     */
    public final static int SPIDER_CANCEL = -1;
    /**
     * Error occured while spidering
     */
    public final static int SPIDER_ERROR = 0;
    /**
     * Cache was spidered without problems
     */
    private final static int SPIDER_OK = 1;
    private final static String distanceUnit = (Preferences.itself().metricSystem == Metrics.IMPERIAL ? " " + Metrics.getUnit(Metrics.MILES) + " " : " " + Metrics.getUnit(Metrics.KILOMETER) + " ");
    private final static double MAXNROFCACHESPERLISTPAGE = 20.0;
    //# CachTypRestrictions
    private final static String onlyTraditional = "&tx=32bc9333-5e52-4957-b0f6-5a2c8fc7b257";
    private final static String onlyMulti = "&tx=a5f6d0ad-d2f2-4011-8c14-940a9ebf3c74";
    private final static String onlyVirtual = "&tx=294d4360-ac86-4c83-84dd-8113ef678d7e";
    private final static String onlyLetterboxHybrid = "&tx=4bdd8fb2-d7bc-453f-a9c5-968563b15d24";
    //#allEventTypes     == onlyEvent
    private final static String onlyEvent = "&tx=69eb8534-b718-4b35-ae3c-a856a55b0874";
    private final static String onlyMegaEvent = "&tx=69eb8535-b718-4b35-ae3c-a856a55b0874";
    private final static String onlyCito = "&tx=57150806-bc1a-42d6-9cf0-538d171a2d22";
    // private final static String onlyL_FEvent = "&tx=3ea6533d-bb52-42fe-b2d2-79a3424d4728";
    private final static String onlyUnknown = "&tx=40861821-1835-4e11-b666-8d41064d03fe";
    private final static String onlyWebcam = "&tx=31d2ae3c-c358-4b5f-8dcd-2185bf472d3d";
    private final static String onlyEarth = "&tx=c66f5cf3-9523-4549-b8dd-759cd2f18db8";
    private final static String onlyWherigo = "&tx=0544fa55-772d-4e5c-96a9-36a51ebcf5c9";
    //#onlyProjectAPE    = "&tx=2555690d-b2bc-4b55-b5ac-0cb704c0b768";
    //#onlyGPSAdventure  = "&tx=72e69af2-7986-4990-afd9-bc16cbbb4ce3";
    // navigate the listPages
    private final static String gotoPage = "ctl00$ContentBody$pgrTop$lbGoToPage_";// add pagenumber
    private final static String gotoNextPage = "ctl00$ContentBody$pgrTop$ctl08";
    // change to the block (10pages) of the wanted page
    // private final static String gotoPreviousBlock = "ctl00$ContentBody$pgrTop$ctl05";
    private final static String gotoNextBlock = "ctl00$ContentBody$pgrTop$ctl06";
    private final static int pageLimit = 20; // immer maximal 20 Listpages prüfen, dann download bzw aktualisierung (GC meckerte sonst schon mal)
    private static final String iconsRelativePath = "<img src=\"/images/icons/";
    /**
     * no probs, but exmpl found und not want this
     */
    public static int SPIDER_IGNORE = 2;
    private static Regex listBlockRex;
    private static Regex lineRex;
    private static Regex numFindsRex;
    private static String propPM;
    private static Regex logDateRex;
    private static String propAvailable;
    private static String propArchived;
    private static String propFound;
    private static Regex waypointRex;
    private static Regex DistDirRex;
    private static Regex DTSRex;
    private static Regex difficultyRex;
    private static Regex terrainRex;
    private static Regex backgroundImageUrlRex;
    private static Regex cacheTypeRex;
    private static Regex cacheNameRex;
    private static Regex cacheOwnerRex;
    private static Regex dateHiddenRex;
    private static Regex sizeRex;
    private static Regex favoriteValueRex;
    private static Regex latLonRex;
    private static Regex cacheLocationRex;
    private static Regex shortDescRex;
    private static Regex longDescRex;
    private static Regex hintsRex;
    private static Regex notesRex;
    private static String premiumGeocache = "Premium Member Only";
    private static String unpublishedGeocache = "Unpublished Geocache";
    private static String unavailableGeocache = "This cache is temporarily unavailable";
    private static String archivedGeocache = "This cache has been archived";
    private static String lockedGeocache = "This cache has been locked";
    private static String foundByMe = "ctl00_ContentBody_GeoNav_logText";
    private static String correctedCoordinate = "\"isUserDefined\":true";
    // private static String premiumCacheForPM = "Warning NoBottomSpacing" is a PM-Cache for PMs
    // TBs
    private static String blockExStart, blockExEnd;
    private static String bugExStart, bugExEnd;
    private static String bugLinkEnd;
    private static String bugNameExStart, bugNameExEnd;
    private static String bugDetailsStart, bugDetailsEnd;
    // Addis
    private static String wayBlockExStart, wayBlockExEnd;
    private static String rowBlockExStart, rowBlockExEnd;
    private static String prefixExStart, prefixExEnd;
    private static Regex nameRex;
    private static Regex koordRex;
    private static Regex descRex;
    private static Regex typeRex;
    private static Regex uuidRex;

    // images Spoiler
    private static String spoilerSectionStart, spoilerSectionEnd, spoilerSectionStart2;
    private static String imgCommentExStart, imgCommentExEnd;
    // attributes
    private static String attBlockExStart, attBlockExEnd;
    private static String attExStart, attExEnd;
    private static Regex listPageTypeRex;
    // Logs
    private static Regex RexUserToken;
    private static String icon_smile;
    private static String icon_camera;
    private static String icon_attended;
    private static String getBugByNameUrl = "http://www.geocaching.com/track/search.aspx?k=";
    private static String getBugByGuidUrl = "http://www.geocaching.com/track/details.aspx?guid=";
    private static String getBugByIdUrl = "http://www.geocaching.com/track/details.aspx?id=";
    private static String getBugByTrackNrUrl = "http://www.geocaching.com/track/details.aspx?tracker=";
    private static String bugGuidExStart, bugGuidExEnd;
    private static String bugNotFound;
    private static String bugTotalRecords;
    private static String bugNameStart, bugNameEnd;
    private static double minDistance = 0;
    private static double maxDistance = 0;
    private static String WebPage;
    private static Extractor extractor = new Extractor();
    private static Extractor extractValue = new Extractor();
    boolean shortDescRex_not_yet_found = true;
    Vector spideredUrls;
    CacheImages oldImages;
    int spiderCounter;
    boolean koords_not_yet_found = true;
    private InfoBox infB;
    private ImportGui importGui;
    private boolean downloadDescriptionImages;
    private boolean downloadSpoilerImages;
    private boolean downloadLogImages;
    private byte restrictedCacheType = 0;
    private String cacheTypeRestriction;
    private int maxNew, newTillNow, numPrivateNew;
    private int maxUpdate, updateTillNow;
    private int maxLogs;
    private boolean doNotgetFound;
    private boolean spiderAllFinds;
    private int lastPageVisited;
    private int numFoundUpdates = 0;
    private int numArchivedUpdates = 0;
    private int numAvailableUpdates = 0;
    private int numLogUpdates = 0;
    private int numPremium = 0;
    private Vector downloadList = new Vector();
    private Hashtable possibleUpdateList, sureUpdateList;
    private String wayPointPage;
    private int wayPointPageIndex = 0;
    private CWPoint origin;
    private boolean loggedIn = false;
    private int spiderErrors;
    private int spiderIgnorePremium;
    private char imageSource;
    private String backgroudImageName;
    private CacheHolder newCache;
    private CacheHolderDetail newCacheDetails;
    private String aCacheDescriptionOfListPage;
    private String listPagesUrl;

    public GCImporter() {
        initialiseProperties();
    }

    private boolean setGCLanguage(String toLanguage) {
        String url = "https://www.geocaching.com/account/chrome/setculture?culture=" + toLanguage;
        try {
            WebPage = UrlFetcher.fetch(url);
        } catch (final Exception ex) {
            Preferences.itself().log("[setculture]:Exception", ex, true);
            return false;
        }
        if (WebPage.indexOf("isLoggedIn: true") > -1) {
            return true;
        }
        return false;
    }

    /**
     * from WebPage
     *
     * @return
     */
    private String getViewState() {
        String Result = "";
        int searchPosition = 0;
        final Regex rexViewstateFieldCount = new Regex("id=\"__VIEWSTATEFIELDCOUNT\" value=\"(.*?)\" />");
        String sfieldcount;
        rexViewstateFieldCount.search(WebPage);
        if (rexViewstateFieldCount.didMatch()) {
            sfieldcount = rexViewstateFieldCount.stringMatched(1);
            searchPosition = rexViewstateFieldCount.matchedTo();
        } else {
            sfieldcount = "";
        }
        int fieldcount = 1;
        if (sfieldcount.length() > 0) {
            fieldcount = Common.parseInt(sfieldcount);
            Result = "&" + "__VIEWSTATEFIELDCOUNT=" + sfieldcount;
        }

        final Regex rexViewstate = new Regex("id=\"__VIEWSTATE[0-9]?\" value=\"(.*?)\" />");
        for (int i = 1; i <= fieldcount; i++) {
            rexViewstate.searchFrom(WebPage, searchPosition);
            String viewstate;
            if (rexViewstate.didMatch()) {
                viewstate = rexViewstate.stringMatched(1);
                searchPosition = rexViewstate.matchedTo();
            } else {
                viewstate = "";
                Preferences.itself().log("[GCImporter] Viewstate " + i + " not found." + WebPage, null);
            }
            if (i == 1)
                Result = Result + "&" + "__VIEWSTATE=" + URL.encodeURL(viewstate, false); //
            else
                Result = Result + "&" + "__VIEWSTATE" + (i - 1) + "=" + URL.encodeURL(viewstate, false); //

        }

        final Regex rexViewstateGenerator = new Regex("id=\"__VIEWSTATEGENERATOR\" value=\"(.*?)\" />");
        String sViewstateGeneratorValue;
        rexViewstateGenerator.searchFrom(WebPage, searchPosition);
        if (rexViewstateGenerator.didMatch()) {
            sViewstateGeneratorValue = rexViewstateGenerator.stringMatched(1);
            searchPosition = rexViewstateGenerator.matchedTo();
            Result = Result + "&" + "__VIEWSTATEGENERATOR=" + sViewstateGeneratorValue;
        }

        return Result;
    }

    private void initialiseProperties() {
        SpiderProperties p = new SpiderProperties();
        try {

            listBlockRex = new Regex(p.getProp("listBlockRex"));
            lineRex = new Regex(p.getProp("lineRex"));
            numFindsRex = new Regex("Total Records: <b>(.*?)</b>");
            propPM = p.getProp("PM");
            logDateRex = new Regex(p.getProp("logDateRex")); // newFoundExists
            propAvailable = p.getProp("Available");
            propArchived = p.getProp("Archived");
            propFound = p.getProp("found");
            DistDirRex = new Regex(p.getProp("DistDirRex"));
            DTSRex = new Regex(p.getProp("DTSRex"));
            waypointRex = new Regex(p.getProp("waypointRex"));
            listPageTypeRex = new Regex(p.getProp("TypeRex"));
            RexUserToken = new Regex(p.getProp("UserTokenRex"));
            icon_smile = p.getProp("icon_smile");
            icon_camera = p.getProp("icon_camera");
            icon_attended = p.getProp("icon_attended");

            difficultyRex = new Regex(p.getProp("difficultyRex"));
            terrainRex = new Regex(p.getProp("terrainRex"));
            // <body background="http://img.geocaching.com/cache/thumb/90bd7b72-98b3-40a1-824d-e9c3a8187ba4.jpg" class="CacheDetailsPage">
            backgroundImageUrlRex = new Regex("background=\"(.*?)\" class");
            cacheTypeRex = new Regex(p.getProp("cacheTypeRex"));
            cacheNameRex = new Regex(p.getProp("cacheNameRex"));
            cacheOwnerRex = new Regex(p.getProp("cacheOwnerRex"));
            dateHiddenRex = new Regex(p.getProp("dateHiddenRex"));
            sizeRex = new Regex(p.getProp("sizeRex"));
            favoriteValueRex = new Regex(p.getProp("favoriteValueRex"));
            latLonRex = new Regex(p.getProp("latLonRex"));
            cacheLocationRex = new Regex(p.getProp("cacheLocationRex"));
            shortDescRex = new Regex(p.getProp("shortDescRex"));
            longDescRex = new Regex(p.getProp("longDescRex"));
            hintsRex = new Regex(p.getProp("hintsRex"));
            notesRex = new Regex("viewCacheNote\" class=\"formatted\">((?s).*?)</span>");

            spoilerSectionStart = p.getProp("spoilerSectionStart");
            spoilerSectionEnd = p.getProp("spoilerSectionEnd");
            spoilerSectionStart2 = p.getProp("spoilerSectionStart2");
            imgCommentExStart = p.getProp("imgCommentExStart");
            imgCommentExEnd = p.getProp("imgCommentExEnd");

            attBlockExStart = p.getProp("attBlockExStart");
            attBlockExEnd = p.getProp("attBlockExEnd");
            attExStart = p.getProp("attExStart");
            attExEnd = p.getProp("attExEnd");

            blockExStart = p.getProp("blockExStart");
            blockExEnd = p.getProp("blockExEnd");
            bugExStart = p.getProp("bugExStart");
            bugExEnd = p.getProp("bugExEnd");
            bugLinkEnd = p.getProp("bugLinkEnd");
            bugNameExStart = p.getProp("bugNameExStart");
            bugNameExEnd = p.getProp("bugNameExEnd");
            bugDetailsStart = p.getProp("bugDetailsStart");
            bugDetailsEnd = p.getProp("bugDetailsEnd");

            rowBlockExStart = p.getProp("rowBlockExStart");
            rowBlockExEnd = p.getProp("rowBlockExEnd");
            nameRex = new Regex(p.getProp("nameRex"));
            koordRex = new Regex(p.getProp("koordRex"));
            descRex = new Regex(p.getProp("descRex"));
            typeRex = new Regex(p.getProp("typeRex"));

            wayBlockExStart = p.getProp("wayBlockExStart");
            wayBlockExEnd = p.getProp("wayBlockExEnd");
            prefixExStart = p.getProp("prefixExStart");
            prefixExEnd = p.getProp("prefixExEnd");

            bugGuidExStart = p.getProp("bugGuidExStart");
            bugGuidExEnd = p.getProp("bugGuidExEnd");
            bugNotFound = p.getProp("bugNotFound");
            bugTotalRecords = p.getProp("bugTotalRecords");
            bugNameStart = p.getProp("bugNameStart");
            bugNameEnd = p.getProp("bugNameEnd");
            uuidRex = new Regex(p.getProp("uuidRex"));
        } catch (final Exception ex) {
            Preferences.itself().log("Error fetching Properties.", ex);
        }
    }

    public void setImportGui(ImportGui importGui) {
        this.importGui = importGui;
        downloadDescriptionImages = importGui.downloadDescriptionImages;
        downloadSpoilerImages = importGui.downloadSpoilerImages;
        downloadLogImages = importGui.downloadLogImages;
        // Einstellung bei Funden nicht speichern (sollte man extra speichern)
        Preferences.itself().downloadDescriptionImages = downloadDescriptionImages;
        Preferences.itself().downloadSpoilerImages = downloadSpoilerImages;
        Preferences.itself().downloadLogImages = downloadLogImages;
    }

    public void setMaxLogsToSpider(int maxLogs) {
        this.maxLogs = maxLogs;
    }

    /**
     * Method to start the spider for a search around the centre coordinates
     */
    public void doIt() {
        doIt(false);
    }

    public void doIt(boolean _spiderAllFinds) {
        downloadList.clear();
        spiderAllFinds = _spiderAllFinds;

        origin = Preferences.itself().curCentrePt;
        if (!spiderAllFinds && !origin.isValid()) {
            CoordsInput cs = new CoordsInput();
            cs.setFields(Preferences.itself().curCentrePt, TransformCoordinates.DMM);
            if (cs.execute() == FormBase.IDOK) {
                MainForm.itself.setCurCentrePt(cs.getCoords());
                origin = Preferences.itself().curCentrePt;
            }
            if (!origin.isValid()) {
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5509, "Coordinates for centre must be set")).wait(FormBase.OKB);
                return;
            }
        }

        // Reset states for all caches when spidering (http://tinyurl.com/dzjh7p)
        for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
            final CacheHolder ch = MainForm.profile.cacheDB.get(i);
            if (ch.mainCache == null)
                ch.initStates(false);
        }

        if (doDownloadGui(0)) {
            Time startZeit = new Time();
            UrlFetcher.usedTime = 0;
            Vm.showWait(true);
            infB = new InfoBox(MyLocale.getMsg(611, "Status"), MyLocale.getMsg(5502, "Fetching first page..."), InfoBox.PROGRESS_WITH_WARNINGS);
            infB.exec();
            if (login()) {

                spiderErrors = 0;
                spiderIgnorePremium = 0;

                fetchFirstListPage(this.getDistanceInMiles(maxDistance));
                int numFinds = getNumFound(WebPage);
                int maxPages = (int) java.lang.Math.ceil(numFinds / MAXNROFCACHESPERLISTPAGE);
                correctMaxNewForFinds(numFinds);

                String s = "ListPages Properties : " + Preferences.NEWLINE;
                s = s + "minDistance          : " + minDistance + Preferences.NEWLINE;
                s = s + "maxDistance          : " + maxDistance + Preferences.NEWLINE;
                s = s + "maxNew               : " + maxNew + Preferences.NEWLINE;
                s = s + "maxUpdate            : " + maxUpdate + Preferences.NEWLINE;
                s = s + "with Founds          : " + (doNotgetFound ? "no" : "yes") + Preferences.NEWLINE;
                s = s + "alias is premium memb: " + (!Preferences.itself().havePremiumMemberRights ? "no" : "yes") + Preferences.NEWLINE;
                s = s + "Update if new Log    : " + (Preferences.itself().checkLog ? "yes" : "no") + Preferences.NEWLINE;
                s = s + "Update if TB changed : " + (Preferences.itself().checkTBs ? "yes" : "no") + Preferences.NEWLINE;
                s = s + "Update if DTS changed: " + (Preferences.itself().checkDTS ? "yes" : "no") + Preferences.NEWLINE;
                s = s + "maxPages for x Miles : " + maxPages + " for " + this.getDistanceInMiles(maxDistance) + Preferences.NEWLINE;
                Preferences.itself().log(s);

                Preferences.itself()
                        .log("Download properties : " + Preferences.NEWLINE //
                                + "maxLogs: " + maxLogs + Preferences.NEWLINE //
                                + "with Descr. images: " + (!downloadDescriptionImages ? "no" : "yes") + Preferences.NEWLINE //
                                + "with Spoil. images: " + (!downloadSpoilerImages ? "no" : "yes") + Preferences.NEWLINE //
                                + "with Log    images: " + (!downloadLogImages ? "no" : "yes") + Preferences.NEWLINE //
                                + "with tb           : " + (!Preferences.itself().downloadTBs ? "no" : "yes") + Preferences.NEWLINE //
                        );

                newTillNow = 0;
                updateTillNow = 0;
                numPrivateNew = 0;
                lastPageVisited = -1; // for not to double check pages on next group run
                boolean withinMaxLimits = true;
                double lowerDistance;
                double upperDistance = minDistance;
                while (upperDistance < maxDistance && withinMaxLimits && !infB.isClosed()) {
                    lowerDistance = upperDistance;
                    upperDistance = getUpperDistance(lowerDistance, maxPages);
                    withinMaxLimits = fillDownloadAndUpdateList(lowerDistance, upperDistance);
                    downloadCaches();
                    updateCaches();
                } // while

                if (spiderErrors > 0 || spiderIgnorePremium > 0) {
                    String infoString = "";
                    if (spiderErrors > 0) {
                        infoString = spiderErrors + MyLocale.getMsg(5516, " cache descriptions\ncould not be loaded.") + "\n";
                    }
                    if (spiderIgnorePremium > 0) {
                        infoString = infoString + spiderIgnorePremium + " Premium " + MyLocale.getMsg(5516, " cache descriptions\ncould not be loaded.");
                    }
                    new InfoBox(MyLocale.getMsg(5500, "Error"), infoString).wait(FormBase.OKB);
                }
                MainForm.profile.restoreFilter();
                MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR, Profile.FORCESAVE);
                MainTab.itself.tablePanel.updateStatusBar();

            }

            Vm.showWait(false);
            loggedIn = false; // check again login on next spider
            long benoetigteZeit = (new Time().getTime() - startZeit.getTime()) / 1000; // sec
            if (!infB.isClosed()) {
                infB.setInfo(MyLocale.getMsg(5535, "Caches added:   ") + newTillNow + "\n" + //
                        MyLocale.getMsg(5536, "Caches updated: ") + updateTillNow + "\n" + //
                        MyLocale.getMsg(5534, "Time required: ") + (benoetigteZeit / 60) + " min " + (benoetigteZeit % 60) + " sec " + //
                        "(" + (UrlFetcher.usedTime / 60) + " min " + (UrlFetcher.usedTime % 60) + " sec " + ")"//
                );
                infB.setButtonText(MyLocale.getMsg(4107, "Done"), FormBase.CANCELB);
            }
        }
    } // End of DoIt

    public void setOldGCLanguage() {
        if (Preferences.itself().changedGCLanguageToEnglish) {
            setGCLanguage(Preferences.itself().oldGCLanguage);
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

    private double getUpperDistance(double fromDistance, int maxPages) {
        int fromPage = 1;
        int toPage = maxPages;
        double toDistance = maxDistance;
        if ((int) fromDistance < ((int) toDistance - 1)) {

            if (fromDistance > 0) {
                fetchFirstListPage(this.getDistanceInMiles(fromDistance));
                fromPage = (int) java.lang.Math.ceil(getNumFound(WebPage) / MAXNROFCACHESPERLISTPAGE);
            }

            while ((1 + toPage - fromPage) > pageLimit) {
                toDistance = fromDistance + (toDistance - fromDistance) / 2;
                if ((int) toDistance <= ((int) fromDistance + 1))
                    break;
                fetchFirstListPage(this.getDistanceInMiles(toDistance));
                toPage = (int) java.lang.Math.ceil(getNumFound(WebPage) / MAXNROFCACHESPERLISTPAGE);
            }
        }
        String pageString = MyLocale.getMsg(5532, "Page");
        String fromString = " " + Common.DoubleToString(fromDistance, 0, 2) + distanceUnit + " (" + pageString + " " + fromPage + ") ";
        String toString = " " + Common.DoubleToString(toDistance, 0, 2) + distanceUnit + " (" + pageString + " " + toPage + ") ";
        infB.addWarning(MyLocale.getMsg(2001, "from") + fromString + MyLocale.getMsg(1831, "to") + toString);
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
        if (!importGui.getFileName().equals("")) {
            final RouteImporter ri = new RouteImporter(importGui.getFileName());
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

        if ((startPos == null) || (startPos != null && !startPos.isValid())) {
            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(5533, "No start point found! (Check track / route / center)")).wait(FormBase.OKB);
            return; //
        }

        Vm.showWait(true);
        infB = new InfoBox("Status", MyLocale.getMsg(5502, "Fetching pages..."), InfoBox.PROGRESS_WITH_WARNINGS);
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

        if (possibleUpdateList == null) {
            possibleUpdateList = new Hashtable(MainForm.profile.cacheDB.size());
            sureUpdateList = new Hashtable(MainForm.profile.cacheDB.size());

        } else {
            possibleUpdateList.clear();
            sureUpdateList.clear();
        }

        double lateralDistance = maxDistance; // Seitenabstand in km
        if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
            lateralDistance = Metrics.convertUnit(maxDistance, Metrics.MILES, Metrics.KILOMETER);
        }
        // Load über die Kreise, daher Faktor 1.2
        lateralDistance = 1.2 * lateralDistance;
        downloadList.clear();

        origin = startPos;
        CWPoint nextPos = startPos;
        int pointsIndex = 1;

        if (points != null)
            Preferences.itself().log("Start at " + origin + " to check " + points.size() + " points.");

        Time startZeit = new Time();
        while (nextPos != null) {
            if (importGui.getFileName().length() == 0) {
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
                downloadCaches();
                updateCaches();
                startPos = nextPos;
            }
            if (infB.isClosed())
                break;
        }
        if (infB.isClosed()) {
            Vm.showWait(false);
            return;
        } // or ask for download of intermediate result

        if (spiderErrors > 0 || spiderIgnorePremium > 0) {
            String infoString = "";
            if (spiderErrors > 0) {
                infoString = spiderErrors + MyLocale.getMsg(5516, " cache descriptions\ncould not be loaded.") + "\n";
            }
            if (spiderIgnorePremium > 0) {
                infoString = infoString + spiderIgnorePremium + " Premium " + MyLocale.getMsg(5516, " cache descriptions\ncould not be loaded.");
            }
            new InfoBox(MyLocale.getMsg(5500, "Error"), infoString).wait(FormBase.OKB);
        }
        MainForm.profile.restoreFilter();
        MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR, Profile.FORCESAVE);
        Vm.showWait(false);

        loggedIn = false; // check again login on next spider

        Time endZeit = new Time();
        long benoetigteZeit = (endZeit.getTime() - startZeit.getTime()) / 1000; // sec
        if (!infB.isClosed()) {
            infB.setInfo(MyLocale.getMsg(5535, "Caches added:   ") + newTillNow + "\n" + //
                    MyLocale.getMsg(5536, "Caches updated: ") + updateTillNow + "\n" + //
                    MyLocale.getMsg(5534, "Time required: ") + (benoetigteZeit / 60) + " min " + (benoetigteZeit % 60) + " sec "//
            );
            infB.setButtonText(MyLocale.getMsg(4107, "Done"), FormBase.CANCELB);
        }
    }

    private CWPoint nextRoutePoint(CWPoint startPos, double lateralDistance) {
        // get next Destination
        double nextDistance = 0;
        int index = -1;
        CacheHolder nextCache = null;
        CacheHolder ch = null;
        for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
            ch = MainForm.profile.cacheDB.get(i);
            if (ch.isChecked && ch.getWpt().isValid()) {
                final CWPoint tmpPos = ch.getWpt();
                final double tmpDistance = tmpPos.getDistance(startPos);
                if (nextDistance == 0) {
                    // Startwert
                    index = i;
                    nextDistance = tmpDistance;
                    nextCache = ch;
                    nextCache.isChecked = false;
                } else {
                    if (tmpDistance > lateralDistance) {
                        if (tmpDistance < nextDistance) {
                            index = i;
                            nextDistance = tmpDistance;
                            nextCache = ch;
                            nextCache.isChecked = false;
                        }
                    } else {
                        ch.isChecked = false;
                    }
                }
            }
        }
        if (index > -1) {
            return MainForm.profile.cacheDB.get(index).getWpt();
        } else
            return null;
    }

    private void getCaches(double radiusKm) {
        // von origin aus
        // kein update vorhandener Cache, da
        int toDistance = (int) java.lang.Math.ceil(radiusKm);
        fetchFirstListPage(toDistance);
        // Number of caches from gcfirst Listpage
        int numFinds = getNumFound(WebPage);
        if (numFinds == 0)
            return;
        int page_number = 1;
        int found_on_page = 0;
        try {
            // Loop pages till maximum distance has been found or no more caches are in the list
            while (toDistance > 0) {
                double distance = 0.0;
                listBlockRex.search(WebPage);
                String tableOfWebPage;
                if (listBlockRex.didMatch()) {
                    tableOfWebPage = listBlockRex.stringMatched(1);
                } else {
                    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check listBlockRex!" + WebPage);
                    tableOfWebPage = "";
                }
                lineRex.search(tableOfWebPage);
                while (toDistance > 0) {
                    if (!lineRex.didMatch()) {
                        if (page_number == 1 && found_on_page == 0)
                            Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check lineRex!");
                        break;
                    }
                    found_on_page++;
                    MainTab.itself.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
                    aCacheDescriptionOfListPage = lineRex.stringMatched(1);
                    distance = getDistance();
                    String chWaypoint = getWP();
                    if (distance <= radiusKm) {
                        final CacheHolder ch = MainForm.profile.cacheDB.get(chWaypoint);
                        if (ch == null) { // not in DB
                            if (!downloadList.contains(chWaypoint)) {
                                downloadList.add(chWaypoint);
                                infB.setInfo(MyLocale.getMsg(5511, "Found ") + downloadList.size());
                            }
                        } else {
                            if (updateExists(ch)) {
                                possibleUpdateList.put(ch.getCode(), ch);
                            }
                        }
                    } else
                        // finish this WebPage
                        toDistance = 0;
                    lineRex.searchFrom(tableOfWebPage, lineRex.matchedTo());
                    if (infB.isClosed()) {
                        toDistance = 0;
                        break;
                    }
                } // next Cache
                if (found_on_page < MAXNROFCACHESPERLISTPAGE)
                    toDistance = 0;
                if (toDistance > 0) {
                    if (fetchAListPage(gotoNextPage)) {
                        page_number++;
                        found_on_page = 0;
                    }
                }
            } // loop pages
            Preferences.itself().log("Nr Caches now: " + newTillNow + downloadList.size());
        } // try
        catch (final Exception ex) {
            Preferences.itself().log("Download error : ", ex, true);
            infB.close(0);
            Vm.showWait(false);
        }
    }

    private boolean doDownloadGui(int menu) {
        int options = ImportGui.ISGC | ImportGui.TRAVELBUGS;
        if (spiderAllFinds) {
            options = options | ImportGui.MAXUPDATE | ImportGui.MAXLOGS;
            if (Preferences.itself().askForMaxNumbersOnImport) {
                options = options | ImportGui.MAXNUMBER;
            }
            importGui = new ImportGui(MyLocale.getMsg(217, "Spider all finds from geocaching.com"), options, ImportGui.DESCRIPTIONIMAGE | ImportGui.SPOILERIMAGE | ImportGui.LOGIMAGE);
            // setting defaults for input
            importGui.maxNumberUpdates.setText("0");
        } else if (menu == 0) {
            options = options | ImportGui.TYPE | ImportGui.DIST | ImportGui.INCLUDEFOUND;
            if (Preferences.itself().askForMaxNumbersOnImport) {
                options = options | ImportGui.MAXNUMBER | ImportGui.MAXUPDATE | ImportGui.MAXLOGS;
            }
            importGui = new ImportGui(MyLocale.getMsg(131, "Download from geocaching.com"), options, ImportGui.DESCRIPTIONIMAGE | ImportGui.SPOILERIMAGE | ImportGui.LOGIMAGE);
        } else if (menu == 1) {
            options = options | ImportGui.TYPE | ImportGui.DIST | ImportGui.INCLUDEFOUND | ImportGui.FILENAME;
            importGui = new ImportGui(MyLocale.getMsg(137, "Download along a Route from geocaching.com"), options, ImportGui.DESCRIPTIONIMAGE | ImportGui.SPOILERIMAGE | ImportGui.LOGIMAGE);
            importGui.maxDistanceInput.setText("0.5");

        } else {
            return false;
        }

        // doing the input
        if (importGui.execute() == FormBase.IDCANCEL) {
            return false;
        }

        downloadDescriptionImages = importGui.downloadDescriptionImages;
        downloadSpoilerImages = importGui.downloadSpoilerImages;
        downloadLogImages = importGui.downloadLogImages;
        if (!spiderAllFinds) {
            // Einstellung bei Funden nicht speichern (sollte man extra speichern)
            Preferences.itself().downloadDescriptionImages = downloadDescriptionImages;
            Preferences.itself().downloadSpoilerImages = downloadSpoilerImages;
            Preferences.itself().downloadLogImages = downloadLogImages;
        }

        //
        restrictedCacheType = importGui.getRestrictedCacheType();
        cacheTypeRestriction = getCacheTypeRestriction(restrictedCacheType);
        //
        minDistance = 0.0; // no longer really used
        // MainForm.profile.setMinDistGC(Convert.toString(0).replace(',', '.'));
        //
        if ((options & ImportGui.DIST) > 0) {
            maxDistance = importGui.getDoubleFromInput(importGui.maxDistanceInput, 0);
            if (maxDistance == 0) {
                importGui.close(0);
                return false;
            }
            if (menu == 1) {
                // "along the route" mindenstens 500 meter Umkreis
                if (maxDistance < 0.5)
                    maxDistance = 0.5;
            } else {
                MainForm.profile.setDistGC(Common.DoubleToString(maxDistance, 0, 2));
            }
        } else {
            maxDistance = 1.0; // only to be > minDistance
        }
        //
        maxNew = Preferences.itself().maxSpiderNumber = importGui.getIntFromInput(importGui.maxNumberInput, Integer.MAX_VALUE);
        //
        maxUpdate = importGui.getIntFromInput(importGui.maxNumberUpdates, Integer.MAX_VALUE);
        if (menu == 1) {
            maxUpdate = 0;
        }
        //
        if ((options & ImportGui.MAXLOGS) > 0) {
            maxLogs = importGui.getIntFromInput(importGui.maxLogsInput, -1);
        } else {
            maxLogs = Preferences.itself().maxLogsToSpider;
        }
        //
        if ((options & ImportGui.INCLUDEFOUND) > 0) {
            doNotgetFound = importGui.foundCheckBox.getState();
            Preferences.itself().doNotGetFound = doNotgetFound;
        }

        importGui.close(0);
        return true;
    }

    private String getCacheTypeRestriction(byte restrictedCacheType) {
        if (restrictedCacheType == CacheType.CW_TYPE_ERROR)
            return "";
        else if (restrictedCacheType == CacheType.CW_TYPE_TRADITIONAL)
            return onlyTraditional;
        else if (restrictedCacheType == CacheType.CW_TYPE_MULTI)
            return onlyMulti;
        else if (restrictedCacheType == CacheType.CW_TYPE_VIRTUAL)
            return onlyVirtual;
        else if (restrictedCacheType == CacheType.CW_TYPE_LETTERBOX)
            return onlyLetterboxHybrid;
        else if (restrictedCacheType == CacheType.CW_TYPE_EVENT)
            return onlyEvent;
        else if (restrictedCacheType == CacheType.CW_TYPE_MEGA_EVENT)
            return onlyMegaEvent;
        else if (restrictedCacheType == CacheType.CW_TYPE_WEBCAM)
            return onlyWebcam;
        else if (restrictedCacheType == CacheType.CW_TYPE_MYSTERY)
            return onlyUnknown;
        else if (restrictedCacheType == CacheType.CW_TYPE_EARTH)
            return onlyEarth;
        else if (restrictedCacheType == CacheType.CW_TYPE_WHEREIGO)
            return onlyWherigo;
        else if (restrictedCacheType == CacheType.CW_TYPE_CITO)
            return onlyCito;
        else
            return "";
    }

    private boolean fillDownloadAndUpdateList(double fromDistance, double toDistance) {
        boolean withinMaxLimits = true;
        int numFinds = getFirstListPage(fromDistance, toDistance);
        fillPossibleUpdateAndClearSureUpdateList(fromDistance, toDistance);
        // remember for later checks
        final int startSize = possibleUpdateList.size();
        int page_number = 1;
        int found_on_page = 0;
        try {
            // Loop pages till maximum distance has been found or no more caches are in the list
            while (toDistance > 0) {

                if (infB.isClosed()) {
                    toDistance = 0;
                    possibleUpdateList.clear();
                    break;
                }

                listBlockRex.search(WebPage);
                String allCachesOfListPage;
                if (listBlockRex.didMatch()) {
                    allCachesOfListPage = listBlockRex.stringMatched(1);
                } else {
                    Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check listBlockRex!" + WebPage);
                    allCachesOfListPage = "";
                }

                lineRex.search(allCachesOfListPage);
                if (!lineRex.didMatch()) {
                    if (page_number == 1 && found_on_page == 0)
                        Preferences.itself().log("[SpiderGC.java:fillDownloadLists]check lineRex!");
                    break;
                }

                // Loop caches on a ListPage (examine the rows of the SearchResultsTable up to MAXNROFCACHESPERLISTPAGE)
                boolean isThereOneMoreCacheOnTheListpage;
                do {
                    found_on_page++;
                    MainTab.itself.tablePanel.updateStatusBar("working " + page_number + " / " + found_on_page);
                    aCacheDescriptionOfListPage = lineRex.stringMatched(1);
                    toDistance = examineCacheDescriptionOfListPage(fromDistance, toDistance);
                    if (toDistance >= 0) {
                        if (newTillNow + downloadList.size() >= maxNew - numPrivateNew) {
                            if (updateTillNow + sureUpdateList.size() >= maxUpdate) {
                                withinMaxLimits = false;
                            } else {
                                if (maxNew == 0) {
                                    // wir möchten noch updates
                                } else {
                                    withinMaxLimits = false;
                                }
                            }
                        } else {
                            if (updateTillNow + sureUpdateList.size() >= maxUpdate) {
                                if (maxUpdate == 0) {
                                    // wir möchten noch Neue
                                } else {
                                    withinMaxLimits = false;
                                }
                            } else {
                                // noch keine Grenze erreicht
                            }
                        }
                    }
                    isThereOneMoreCacheOnTheListpage = lineRex.searchFrom(allCachesOfListPage, lineRex.matchedTo());
                } // Loop caches on a ListPage (examine the rows of the SearchResultsTable up to MAXNROFCACHESPERLISTPAGE)
                while (toDistance >= 0 && withinMaxLimits && isThereOneMoreCacheOnTheListpage);
                infB.setInfo(MyLocale.getMsg(5511, "Found ") + (newTillNow + downloadList.size()) + " / " + (updateTillNow + sureUpdateList.size()) + MyLocale.getMsg(5512, " caches"));

                if (withinMaxLimits) {
                    if (found_on_page < MAXNROFCACHESPERLISTPAGE) {
                        // possibly on end but:
                        // see http://www.geoclub.de/viewtopic.php?f=40&t=61614
                        //  ( there are gc-accounts with found_on_page less MAXNROFCACHESPERLISTPAGE and not on end )
                        // so: checking on numFinds
                        if (((page_number - 1) * MAXNROFCACHESPERLISTPAGE + found_on_page) >= numFinds) {
                            // toDistance = 0; // last page (has less than MAXNROFCACHESPERLISTPAGE entries!?) to check reached
                            toDistance = -1;
                        }
                    }
                } else {
                    toDistance = -1;
                    possibleUpdateList.clear();
                }

                if (toDistance >= 0) {
                    // for not to do consecutive gotoNextPage for finds
                    if (spiderAllFinds) {
                        if (page_number % pageLimit == 0) {
                            fetchFirstListPage(0);
                            skipToListpage(page_number);
                        }
                    }
                    if (fetchAListPage(gotoNextPage)) {
                        lastPageVisited++;
                        Preferences.itself().log("[SpiderGC:fillDownloadLists] got Listpage: " + lastPageVisited);
                        page_number++;
                        found_on_page = 0;
                    } else {
                        // stop, but download new ones if possible
                        possibleUpdateList.clear();
                        found_on_page = 0;
                        toDistance = -1;
                    }
                }

            } // loop pages
        } // try
        catch (final Exception ex) {
            Preferences.itself().log("Download error : ", ex, true);
            infB.close(0);
            Vm.showWait(false);
            possibleUpdateList.clear();
        }

        String s = "Checked " + page_number + " pages" + Preferences.NEWLINE;
        s = s + "with " + ((page_number - 1) * MAXNROFCACHESPERLISTPAGE + found_on_page) + " caches" + Preferences.NEWLINE;
        s = s + "Found " + downloadList.size() + " new caches" + Preferences.NEWLINE;
        s = s + "Found " + possibleUpdateList.size() + "/" + sureUpdateList.size() + " caches for update" + Preferences.NEWLINE;
        s = s + "Found " + (possibleUpdateList.size() - sureUpdateList.size()) + " caches possibly archived." + Preferences.NEWLINE;
        s = s + "Found " + numPremium + " Premium Caches (for non Premium Member.)" + Preferences.NEWLINE;
        s = s + "Found " + numAvailableUpdates + " caches with changed available status." + Preferences.NEWLINE;
        s = s + "Found " + numLogUpdates + " caches with new found in log." + Preferences.NEWLINE;
        s = s + "Found " + numFoundUpdates + " own Finds" + Preferences.NEWLINE; //caches with no found in DB
        s = s + "Found " + numArchivedUpdates + " unarchived." + Preferences.NEWLINE; // caches with changed archived status.
        Preferences.itself().log(s);

        // if (possibleUpdateList.size() == 0 // prima, alle tauchen in der sureUpdateList (Liste bei GC) auf
        if (possibleUpdateList.size() == startSize //
                || possibleUpdateList.size() > maxUpdate // Restmenge zu gross, wir nehmen nur die sicher geänderten.
                ) {
            possibleUpdateList.clear();
        }
        Preferences.itself().log("possibly " + possibleUpdateList.size() + " + known " + sureUpdateList.size());

        // checking if all is in possibleUpdateList by adding the known changed ones (sureUpdateList)
        for (final Enumeration e = sureUpdateList.elements(); e.hasMoreElements(); ) {
            final CacheHolder ch = (CacheHolder) e.nextElement();
            possibleUpdateList.put(ch.getCode(), ch);
        }

        Preferences.itself().log("now will update: " + possibleUpdateList.size());

        s = "These Caches will be updated :" + Preferences.NEWLINE;
        s = s + "Out of " + startSize + Preferences.NEWLINE;
        for (final Enumeration e = possibleUpdateList.elements(); e.hasMoreElements(); ) {
            final CacheHolder ch = (CacheHolder) e.nextElement();
            s = s + ch.getCode() + "(" + ch.kilom + " km )";
            if (sureUpdateList.containsKey(ch.getCode())) {
                s = s + " sure";
            }
            s = s + Preferences.NEWLINE;
        }
        Preferences.itself().log(s);

        return withinMaxLimits;
    }

    // using either the page last visited or calc it
    private int getFirstListPage(double fromDistance, double toDistance) {
        int numFinds;
        int startPage = 1;
        // max distance in miles for URL, so we can get more than 80km
        // get pagenumber of page with fromDistance , to skip reading of pages < fromDistance
        int fromDistanceInMiles = 0;

        if (fromDistance > 0) {
            fromDistanceInMiles = this.getDistanceInMiles(fromDistance) - 1;
            fetchFirstListPage(java.lang.Math.max(fromDistanceInMiles, 1));
            if (lastPageVisited > -1) {
                startPage = lastPageVisited;
            } else {
                // Number of caches from gc Listpage calc the number of the startpage
                startPage = (int) java.lang.Math.ceil(getNumFound(WebPage) / MAXNROFCACHESPERLISTPAGE);
            }
        }
        lastPageVisited = startPage;

        // add a mile to be save from different distance calculations in CW and at GC
        int toDistanceInMiles = this.getDistanceInMiles(toDistance) + 1;
        //
        fetchFirstListPage(toDistanceInMiles);
        // Number of caches from gcfirst Listpage
        numFinds = getNumFound(WebPage);

        // skip (most of) the pages with distance < fromDistance
        if (fromDistance > 0) {
            skipToListpage(startPage);
        }

        int endPage = (int) (numFinds / MAXNROFCACHESPERLISTPAGE);
        int anzPages = 1 + endPage - startPage;
        Preferences.itself().log("List up to " + anzPages + " pages (" + startPage + ".." + endPage + "). From " + fromDistanceInMiles + " miles (" + fromDistance + " km/miles)" + " to " + toDistanceInMiles + " miles (" + toDistance + " km/miles)");

        return numFinds;
    }

    private void skipToListpage(int pageNr) {
        // 1..10, 11..20, ...
        for (int i = 0; i < ((pageNr - 1) / 10); i++) {
            fetchAListPage(gotoNextBlock);
        }
        if (pageNr % 10 == 1) {
            // 1, 11 , 21
            // fetchAListPage(toDistanceInMiles, gotoNextPage);
        } else {
            // 2..10, 12..20, 22-30
            fetchAListPage(gotoPage + pageNr);
        }
    }

    private void correctMaxNewForFinds(int numFinds) {

        if (spiderAllFinds) {
            int numFindsInDB = getFoundInDB();
            Preferences.itself().log((spiderAllFinds ? "all Finds (DB/GC)" + numFindsInDB + "/" + numFinds : "new and update Caches") + Preferences.NEWLINE);
            maxNew = java.lang.Math.min(numFinds - numFindsInDB, maxNew);
            if (maxUpdate == 0 && maxNew == 0) {
                Vm.showWait(false);
                infB.close(0);
                possibleUpdateList = new Hashtable();
            }
        }

    }

    private int getFoundInDB() {
        int counter = 0;
        for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
            CacheHolder ch = MainForm.profile.cacheDB.get(i);
            if (ch.isFound()) {
                if (ch.isGC())
                    counter++;
            }
        }
        return counter;
    }

    private void fillPossibleUpdateAndClearSureUpdateList(double fromDistance, double toDistance) {

        if (possibleUpdateList == null) {
            possibleUpdateList = new Hashtable(MainForm.profile.cacheDB.size());
            sureUpdateList = new Hashtable(MainForm.profile.cacheDB.size());

        } else {
            possibleUpdateList.clear();
            sureUpdateList.clear();
        }

        if (maxUpdate > 0) {

            double toDistanceInKm = toDistance;
            if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
                toDistanceInKm = Metrics.convertUnit(toDistance, Metrics.MILES, Metrics.KILOMETER);
            }

            double fromDistanceInKm = fromDistance;
            if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
                fromDistanceInKm = Metrics.convertUnit(fromDistance, Metrics.MILES, Metrics.KILOMETER);
            }

            // all of DB (=possibleUpdateList) - listed by GC = possibly archived (to check separately)
            for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
                final CacheHolder ch = MainForm.profile.cacheDB.get(i);
                if (!ch.isBlack()) {
                    if (ch.isGC()) {
                        if (Preferences.itself().havePremiumMemberRights || !ch.isPremiumCache()) {
                            if (spiderAllFinds //
                                    || ( //
                                    (!ch.isArchived()) //
                                            && ch.kilom >= fromDistanceInKm //
                                            && ch.kilom <= toDistanceInKm //
                                            && (!(doNotgetFound && (ch.isFound() || ch.isOwned()))) //
                                            && (restrictedCacheType == CacheType.CW_TYPE_ERROR || restrictedCacheType == ch.getType()) // all typs or chTyp=selected typ
                            ) //
                                    ) //
                            {
                                possibleUpdateList.put(ch.getCode(), ch);
                            }
                        }
                    }
                }
            }

        }
    }

    /* 
    private boolean setGCLanguage(String toLanguage) {
    // switch to English with
    // url = "http://www.geocaching.com/account/ManagePreferences.aspx";
    // todo to work successfull with this perhaps set all values (did not test).
    String setLanguageEN = "ctl00$ContentBody$uxLanguagePreference=en-US";
    String commit = "ctl00$ContentBody$uxSave=Save Changes";
    
    final String postData = "__EVENTTARGET=" //
    	+ "&" + "__EVENTARGUMENT="//
    
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

    private double examineCacheDescriptionOfListPage(double fromDistance, double toDistance) {
        double distance;
        Preferences.itself().log("[AP:] start examineCacheDescriptionOfListPage");

        try {
            distance = getDistance();
            Preferences.itself().log("[AP:] current distance: [" + distance + "] + toDistance: [" + toDistance + "]");
        } catch (Exception e) {
            return -1;
        }
        if (distance >= fromDistance) { // finds have distance 0
            if (distance <= toDistance) {
                final String chWaypoint = getWP();
                Preferences.itself().log("[AP:] examining waypoint: [" + chWaypoint + "]");
                CacheHolder ch = MainForm.profile.cacheDB.get(chWaypoint);
                boolean isPremiumOnly = isPremiumOnly();
                if (Preferences.itself().havePremiumMemberRights || !isPremiumOnly) {
                    if (ch == null) { // not in DB
                        downloadList.add(chWaypoint);
                    } else {
                        Preferences.itself().log("[AP:] Waypoint is in Database: [" + chWaypoint + "]");
                        if (ch.isPremiumCache() != isPremiumOnly) {
                            ch.setIsPremiumCache(isPremiumOnly);
                            ch.saveCacheDetails();
                        }
                        possibleUpdateList.remove(chWaypoint);
                        if (updateExists(ch)) {
                            sureUpdateList.put(chWaypoint, ch);
                        }
                    }
                } else {
                    Preferences.itself().log("[AP:] premium cache found: [" + chWaypoint + "]");
                    // ein PremiumCache für ein BasicMember
                    if (ch == null) {
                        if (Preferences.itself().addPremiumGC) {
                            numPrivateNew = numPrivateNew + 1;
                            ch = new CacheHolder(chWaypoint);
                            ch.setIsPremiumCache(true);
                            // next 2 for to avoid warning triangle
                            ch.setType(CacheType.CW_TYPE_CUSTOM);
                            ch.setWpt(Preferences.itself().curCentrePt); // or MainForm.profile.centre
                            ch.getDetails().setLongDescription(aCacheDescriptionOfListPage); // for Info
                            ch.saveCacheDetails();
                            MainForm.profile.cacheDB.add(ch);
                        }
                    } else {
                        boolean save = false;
                        possibleUpdateList.remove(chWaypoint);
                        if (updateExists(ch)) {
                            ch.setLastSync(""); //
                            save = true;
                        }
                        if (!ch.isPremiumCache()) {
                            ch.setIsPremiumCache(true);
                            save = true;
                        }
                        if (save)
                            ch.saveCacheDetails();
                    }
                }

            } else {
                // more than toDistance away: we can stop
                return -1;
            }
        } else {
            // less than fromDistance away: ignore the cache, but don't abort
        }

        return toDistance;
    }

    private void downloadCaches() {

        infB.addWarning(MyLocale.getMsg(5531, "New: ") + downloadList.size());
        int limit = Math.min(downloadList.size(), maxNew - newTillNow);
        newTillNow = newTillNow + limit;
        for (int i = 0; i < limit; i++) {
            if (infB.isClosed())
                break;
            String wpt = (String) downloadList.get(i);
            // Get only caches not already available in the DB
            if (MainForm.profile.cacheDB.getIndex(wpt) == -1) {
                infB.setInfo(MyLocale.getMsg(5531, "New: ") + wpt + " (" + (i + 1) + " / " + downloadList.size() + ")");
                newCache = new CacheHolder(wpt);
                newCacheDetails = newCache.getDetails();
                newCache.initStates(true);
                final int test = getCacheByWaypointName(Preferences.itself().downloadTBs);
                if (newCache.details == null)
                    newCache.details = newCacheDetails;
                if (test == SPIDER_CANCEL) {
                    infB.close(0);
                    break;
                } else if (test == SPIDER_ERROR) {
                    spiderErrors++;
                } else if (test == SPIDER_OK) {
                    MainForm.profile.cacheDB.add(newCache);
                    newCache.saveCacheDetails();
                } // For test == SPIDER_IGNORE_PREMIUM and SPIDER_IGNORE there is nothing to do
            }
        }
        downloadList.clear();
    }

    private void updateCaches() {

        infB.addWarning(MyLocale.getMsg(5530, "Update: ") + possibleUpdateList.size());
        int limit = Math.min(possibleUpdateList.size(), maxUpdate - updateTillNow);
        updateTillNow = updateTillNow + limit;
        int jj = 0;
        for (final Enumeration e = possibleUpdateList.elements(); e.hasMoreElements(); ) {
            if (jj == limit || infB.isClosed())
                break;
            jj++;
            final CacheHolder ch = (CacheHolder) e.nextElement();
            infB.setInfo(MyLocale.getMsg(5530, "Update: ") + ch.getCode() + " (" + (jj) + " / " + possibleUpdateList.size() + ")");
            final int test = spiderSingle(ch, infB);
            if (test == SPIDER_CANCEL) {
                break;
            } else {
                if (test == SPIDER_ERROR) {
                    spiderErrors++;
                    Preferences.itself().log("[updateCaches] could not spider " + ch.getCode(), null);
                } else {
                    // MainForm.profile.hasUnsavedChanges=true;
                    if (test == SPIDER_IGNORE_PREMIUM) {
                        spiderIgnorePremium++;
                    }
                }
            }
        }
    }

    /**
     * Method to spider a single cache.
     *
     * @return 1 if spider was successful, -1 if spider was cancelled (by closing the infobox etc...), 0 error, but continue with next cache
     */
    public int spiderSingle(final CacheHolder cacheInDB, InfoBox pInfB) {
        int ret = SPIDER_CANCEL;
        if (login()) {
            this.infB = pInfB;
            if (cacheInDB.isAddiWpt())
                return SPIDER_ERROR;
            try {
                newCache = new CacheHolder(cacheInDB.getCode());
                newCacheDetails = newCache.getDetails();
                newCache.initStates(false);
                ret = getCacheByWaypointName(Preferences.itself().downloadTBs);
                if (newCache.details == null)
                    newCache.details = newCacheDetails;
                // Save the spidered data
                if (ret == SPIDER_OK) {
                    cacheInDB.initStates(false);
                    if (!Preferences.itself().useGCFavoriteValue)
                        newCache.setNumRecommended(cacheInDB.getNumRecommended()); // gcvote Bewertung bleibt erhalten
                    cacheInDB.update(newCache);
                    cacheInDB.saveCacheDetails();
                }
            } catch (final Exception ex) {
                Preferences.itself().log("[spiderSingle] Error spidering " + cacheInDB.getCode(), ex);
            }
        }
        return ret;
    } // spiderSingle

    /**
     * Fetch the coordinates of a waypoint from GC
     *
     * @param wayPoint the name of the waypoint
     * @return the cache coordinates
     */
    public String fetchCacheCoordinates(String wayPoint) {
        if (!login())
            return "";
        final InfoBox localInfB = new InfoBox("Info", "Loading " + wayPoint, InfoBox.PROGRESS_WITH_WARNINGS);
        localInfB.exec();
        fetchWayPointPage(wayPoint);
        localInfB.close(0);
        loggedIn = false; // check again login on next spider
        wayPointPageIndex = 0;
        return wayPointPageGetLatLon();
    }

    public int uploadCoordsToGC(CacheHolder mainCache, InfoBox infB) {
        try {
            if (login()) {
                fetchWayPointPage(mainCache.getCode());
                String userToken = getUserToken();

                JSONObject data = new JSONObject();
                data.put("lng", mainCache.getWpt().lonDec);
                data.put("lat", mainCache.getWpt().latDec);
                JSONObject dto = new JSONObject();
                dto.put("ut", userToken);
                dto.put("data", data);
                JSONObject result = new JSONObject();
                result.put("dto", dto);
                UrlFetcher.setpostData(result.toString());
                String uploadResponse = UrlFetcher.fetch("https://www.geocaching.com/seek/cache_details.aspx/SetUserCoordinate");
                JSONObject response = new JSONObject(uploadResponse);
                response = new JSONObject(response.getString("d"));
                if (!response.getString("status").equals("success")) {
                    Preferences.itself().log("uploadCoordsToGC status is " + response.getString("status"), null);
                    infB.setText(MyLocale.getMsg(7013, "error") + ": " + response.getString("status"));
                    return SPIDER_ERROR;
                } else {
                    return SPIDER_OK;
                }
            } else {
                return SPIDER_CANCEL; // could not login
            }
        } catch (Exception e) {
            Preferences.itself().log("uploadCoordsToGC: ", e);
            infB.setInfo(MyLocale.getMsg(7013, "error: " + e.getLocalizedMessage()));
            return SPIDER_ERROR;
        }
    }

    public int uploadLogToGC(CacheHolder mainCache, InfoBox infB) {
        try {
            if (login()) {
                String url = "https://www.geocaching.com/play/geocache/" + mainCache.getCode() + "/log";
                String logPage = UrlFetcher.fetch(url);
                String postData = getItemString(extractor.set(logPage, "action=\"/play/geocache/", "<div class", 0, Extractor.EXCLUDESTARTEND).findNext(), "__RequestVerificationToken");

                url = "https://www.geocaching.com/api/proxy/web/v1/Geocache/" + mainCache.getCode() + "/GeocacheLog";
                // String uploadResponse = UrlFetcher.fetch(url);
                // JSONObject response = new JSONObject(uploadResponse);
                return SPIDER_OK;
            } else {
                return SPIDER_CANCEL; // could not login
            }
        } catch (Exception e) {
            Preferences.itself().log("uploadLogToGC: ", e);
            infB.setInfo(MyLocale.getMsg(7013, "error: ") + e.getLocalizedMessage());
            return SPIDER_ERROR;
        }
    }

    public boolean isFoundByMe(CacheHolder ch) {
        String url;
        if (!login())
            return false;
        //final InfoBox localInfB = new InfoBox("Info", "Loading " + ch.getCode(), InfoBox.PROGRESS_WITH_WARNINGS);
        //localInfB.exec();

        try {
            url = "https://www.geocaching.com/seek/log.aspx?LID=" + ch.getDetails().getOwnLog().getLogID();
            wayPointPage = UrlFetcher.fetch(url);
        } catch (final Exception ex) {
            Preferences.itself().log("Could not fetch " + ch.getCode(), ex);
            return false;
        }

        //localInfB.close(0);
        //loggedIn = false; // check again login on next spider
        wayPointPageIndex = 0;
        boolean ret = this.wayPointPage.indexOf("images/logtypes/2.png") > -1;
        if (!ret) {
            // event
            ret = this.wayPointPage.indexOf("images/logtypes/10.png") > -1;
            if (!ret) {
                // webcam
                ret = this.wayPointPage.indexOf("images/logtypes/11.png") > -1;
            }
        }
        return ret;
    }

    /**
     * login to geocaching.com
     */
    public boolean login() {

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
            boolean isExpired = true;
            if (Preferences.itself().hasGCLogin()) {
                Hashtable ht = Preferences.itself().getGCLogin(Preferences.itself().gcLogin);
                String expires = (String) ht.get("expires");
                String cookie = (String) ht.get("auth");
                boolean withoutExpiration = true;
                if (cookie != null) {
                    if (cookie.length() > 0) {
                        if (expires != null) {
                            if (expires.length() > 0) {
                                withoutExpiration = false;
                                // check expires
                                String[] sExpires = mString.split(expires, ' ');
                                Time tExpires = DateFormat.toDate(sExpires[1]);
                                Time now = new Time();
                                if (tExpires.after(now)) {
                                    UrlFetcher.setCookie("gspkauth;www.geocaching.com", "gspkauth=" + cookie + ";");
                                    isExpired = false;
                                }
                            }
                        }
                        if (withoutExpiration) {
                            UrlFetcher.setCookie("gspkauth;www.geocaching.com", "gspkauth=" + cookie + ";");
                            isExpired = false;
                        }
                    }
                }
            }

            if (isExpired) {
                byte ret = gcLogin(); // todo ? get the cookie for gcLogin, not for myAlias
                if (ret > 0) {
                    if (ret < 5)
                        new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5525, "Perhaps GC is not available. This should not happen!")).wait(FormBase.OKB);
                    return false;
                }
            }

            switch (checkGCSettings()) {
                case 0:
                    loggedIn = true;
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
                    break;
                case 6:
                    // no correct login (Passwort falsch eingegeben oder ähnliches)
                    if (retrycount < maxretries)
                        retry = true;
                    else {
                        new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5524, "Please correct your account in preferences!\n\n")).wait(FormBase.OKB);
                    }
                    break;
                default:
            }

        } while (retry);
        return loggedIn;
    }

    private byte checkGCSettings() {
        String page = "";
        String gcSettingsUrl = "https://www.geocaching.com/account/settings/preferences";
        try {
            page = UrlFetcher.fetch(gcSettingsUrl); // getting cookies
        } catch (final Exception ex) {
            Preferences.itself().log("[checkGCSettings]:Exception calling " + gcSettingsUrl + " with userID ", ex);
            return 2;
        }
        UrlFetcher.rememberCookies();
        // 1.) loggedInAs
        String loggedInAs;
        extractor.set(page, "<span class=\"user-name\">", "</span>", 0, true);
        loggedInAs = extractor.findNext();
        Preferences.itself().log("[checkGCSettings]:loggedInAs= " + loggedInAs, null);
        if (loggedInAs.length() == 0) {
            return 6;
        }

        //4.) distanceUnit
        //<label><input checked="checked" id="DistanceUnits" name="DistanceUnits" type="radio" value="Metric" /> Metric</label>
        String distanceUnitBlock = extractor.findNext("checked\" id=\"DistanceUnits", "/label");
        String distanceUnit = extractValue.set(distanceUnitBlock, "value=\"", "\"", 0, true).findNext();
        Preferences.itself().log("[checkGCSettings]:Units= " + distanceUnit, null);
        String compareTo = Preferences.itself().metricSystem == Metrics.METRIC ? "Metric" : "Imperial";
        if (!distanceUnit.equalsIgnoreCase(compareTo)) {
            Preferences.itself().log(page, null);
            return 3;
        }

        //5.) GCDateFormat
        String GCDateFormatBlock = extractor.findNext("<label for=\"SelectedDateFormat", "<label for=\"SelectedGPXVersion");
        String GCDateFormat = extractValue.set(GCDateFormatBlock, "selected\" value=\"", "\">", 0, true).findNext();
        Preferences.itself().log("[checkGCSettings]:GCDateFormat= " + GCDateFormat, null);
        DateFormat.setGCDateFormat(GCDateFormat);

        // 2.) oldLanguage
        String languageBlock = extractor.findNext("selected\"><a href=\"/account", "</li>");
        String oldLanguage = extractValue.set(languageBlock, "culture=", "\"", 0, true).findNext();
        Preferences.itself().log("[checkGCSettings]:Language= " + oldLanguage, null);

        //6.)ctl00$ContentBody$uxInstantMessengerProvider

        //7.) ctl00$ContentBody$ddlGPXVersion

        byte retCode = 0;
        if (oldLanguage.equals("en-US")) {
            Preferences.itself().changedGCLanguageToEnglish = false;
        } else {
            Preferences.itself().oldGCLanguage = oldLanguage;
            if (setGCLanguage("en-US")) {
                Preferences.itself().changedGCLanguageToEnglish = true;
            } else {
                Preferences.itself().changedGCLanguageToEnglish = false;
                retCode = 1;
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

        //8.)
        if (retCode == 0) {
            page = "";
            gcSettingsUrl = "https://www.geocaching.com/account/settings/membership";
            try {
                page = UrlFetcher.fetch(gcSettingsUrl);
                //save login Cookie
                UrlFetcher.rememberCookies();
                String theCookie[] = mString.split(UrlFetcher.getCookie("gspkauth;www.geocaching.com"), ';');
                if (theCookie.length <= 1) {
                    new InfoBox(MyLocale.getMsg(5523, "Login error!"), MyLocale.getMsg(5524, "Bitte korrigieren Sie Ihr Benutzerkonto in den Einstellungen!\n\n")).wait(FormBase.OKB);
                    return 5;
                } else {
                    String username = Preferences.itself().gcLogin;
                    // remember for next time, so you don't have to login
                    String gspkauth = "";
                    String expires = "";
                    for (int i = 0; i < theCookie.length; i++) {
                        String[] rp = mString.split(theCookie[i], '=');
                        if (rp.length == 2) {
                            if (rp[0].equalsIgnoreCase("gspkauth")) {
                                gspkauth = rp[1];
                            } else if (rp[0].trim().equalsIgnoreCase("expires")) {
                                expires = rp[1];
                                break;
                            }
                        }
                    }
                    if (gspkauth.length() > 0) {
                        Preferences.itself().setGCLogin(username, gspkauth, expires);
                    }
                    Preferences.itself().savePreferences();
                }
                //9.)
                String membershipBlock = extractor.set(page, "membership-details\">", "</dl>", 0, true).findNext();
                String memberId = extractValue.set(membershipBlock, "<dd>", "</dd>", 0, true).findNext();
                Preferences.itself().gcMemberId = memberId;
                String membership = extractValue.findNext();
                membership = membership.trim();
                Preferences.itself().havePremiumMemberRights = membership.indexOf("Basic") == -1;
            } catch (final Exception ex) {
                Preferences.itself().log("[checkGCSettings] " + gcSettingsUrl + page, ex);
            }
        }

        return retCode;

    }

    private byte gcLogin() {
        Preferences.itself().log("[gcLogin]");
        // get username
        String username = Preferences.itself().myAlias;
        if (username.equals("")) {
            InfoBox usernameInput = new InfoBox(MyLocale.getMsg(601, "Your alias:"), MyLocale.getMsg(601, "Your alias:"), InfoBox.INPUT);
            usernameInput.setInput(username);
            int code = FormBase.IDOK;
            code = usernameInput.execute();
            username = usernameInput.getInput();
            usernameInput.close(0);
            if (code != FormBase.IDOK)
                return 1;
            Preferences.itself().myAlias = username;
        }
        // get password
        String passwort = Preferences.itself().password;
        if (passwort.equals("")) {
            InfoBox passwortInput = new InfoBox(MyLocale.getMsg(5506, "Password"), MyLocale.getMsg(5505, "Enter Password"), InfoBox.INPUT);
            passwortInput.setInputPassword(passwort);
            int code = FormBase.IDOK;
            code = passwortInput.execute();
            passwort = passwortInput.getInput();
            passwortInput.close(0);
            if (code != FormBase.IDOK)
                return 2;
        }

        UrlFetcher.clearCookies();
        String loginPageUrl = "https://www.geocaching.com/account/login?ReturnUrl=/play/search";
        // String loginPageUrl = "https://www.geocaching.com/account/oauth/token";
        try {
            WebPage = UrlFetcher.fetch(loginPageUrl); //
        } catch (final Exception ex) {
            Preferences.itself().log("[gcLogin]:Exception gc.com login page", ex, true);
            return 3;
        }
        UrlFetcher.rememberCookies();
        final String postData = getItemString(WebPage, "__RequestVerificationToken") //
                + "&" + "UsernameOrEmail=" + encodeUTF8URL(Utils.encodeJavaUtf8String(username)) //
                + "&" + "Password=" + encodeUTF8URL(Utils.encodeJavaUtf8String(passwort)) //
                ;
        try {
            UrlFetcher.setpostData(postData);
            WebPage = UrlFetcher.fetch(loginPageUrl);
        } catch (final Exception ex) {
            Preferences.itself().log("[gcLogin] Exception", ex);
            return 4;
        }
        UrlFetcher.rememberCookies();

        Preferences.itself().gcLogin = username;
        return 0;

    }

    private void fetchFirstListPage(int distance) {
        makelistPagesUrl(distance);
        int retrycount = 0;
        while (true) {
            try {
                WebPage = UrlFetcher.fetch(listPagesUrl);
                Preferences.itself().log("[fetchFirstListPage] Got first page " + listPagesUrl);
                return;
            } catch (final Exception ex) {
                Preferences.itself().log("[fetchFirstListPage] Error fetching first list page " + listPagesUrl, ex, true);
                if (retrycount == 3) {
                    Vm.showWait(false);
                    infB.close(0);
                    new InfoBox(MyLocale.getMsg(5500, "Error") + " (" + retrycount + " x)", MyLocale.getMsg(5503, "Error fetching first list page.")).wait(FormBase.OKB);
                    return;
                }
                retrycount++;
            }
        }

    }

    /**
     * in: distance whatPage out: WebPage
     */
    private boolean fetchAListPage(String whatPage) {
        boolean ret = true;
        String postData = "__EVENTTARGET=" + URL.encodeURL(whatPage, false) //
                + "&" + "__EVENTARGUMENT=" + getViewState(); //
        try {
            UrlFetcher.setpostData(postData);
            WebPage = UrlFetcher.fetch(listPagesUrl);
            Preferences.itself().log("[fetchAListPage] " + whatPage);
        } catch (final Exception ex) {
            Preferences.itself().log("[fetchAListPage] Error at " + whatPage, ex);
            ret = false;
        }
        return ret;
    }

    /**
     * from WebPage
     *
     * @return
     */
    private String getItemString(String _ExtractFrom, String _Item) {
        String Result = extractor.set(_ExtractFrom, _Item, ">", 0, Extractor.EXCLUDESTARTEND).findNext();
        String sItem = extractor.set(Result, "value=\"", "\"", 0, Extractor.EXCLUDESTARTEND).findNext();
        if (sItem.length() > 0) {
            Result = _Item + "=" + sItem;
        }
        return Result;
    }

    private void makelistPagesUrl(int distance) {
        listPagesUrl = "https://www.geocaching.com/seek/nearest.aspx";
        if (spiderAllFinds) {
            listPagesUrl = listPagesUrl + "?ul=" + encodeUTF8URL(Utils.encodeJavaUtf8String(Preferences.itself().myAlias));
        } else {
            listPagesUrl = listPagesUrl //
                    + "?lat=" + origin.getLatDeg(TransformCoordinates.DD) //
                    + "&lng=" + origin.getLonDeg(TransformCoordinates.DD) //
                    + "&dist=" + distance; //
            if (doNotgetFound)
                listPagesUrl = listPagesUrl + "&f=1";
        }
        listPagesUrl = listPagesUrl + cacheTypeRestriction;
    }

    private int getNumFound(String doc) {
        numFindsRex.search(doc);
        if (numFindsRex.didMatch()) {
            return Common.parseInt(numFindsRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getNumFound]check numFindsRex!", null);
            return 0;
        }
    }

    private double getDistance() {
        // #<span class="small NoWrap"><img src="/images/icons/compass/SW.gif" alt="SW" title="SW" />SW<br />0.31km</span>
        // DistDirRex = compass/(.*?)\.gif(.*?)<br />(.*?)(?:km|mi|ft)
        if (spiderAllFinds)
            return 0.0;
        // <span class="small NoWrap"><br />Here</span>
        if (aCacheDescriptionOfListPage.indexOf(">Here<") > 0)
            return 0.0;
        String stmp;
        DistDirRex.search(aCacheDescriptionOfListPage);
        if (!DistDirRex.didMatch()) {
            Preferences.itself().log("[SpiderGC.java:getDistance]check DistDirRex!", null);
            // Abbruch
            return -1.0;
        }
        stmp = DistDirRex.stringMatched(4);
        double d = Common.parseDouble(DistDirRex.stringMatched(3));
        if (stmp.equals("ft")) {
            d = Metrics.convertUnit(d, Metrics.FEET, Metrics.MILES);
        }
        return d;
        // stmp = DistDirRex.stringMatched(1);
        // = "N"(0),"NE"(45),"E"(90),"SE"(135),"S"(180),"SW"(225),"W"(270),"NW"(315)
    }

    private String getWP() {
        //#<span class="small">
        //#                            by OlSiTiNi
        //#                            |
        //#                            GC34CQJ
        //#                            |
        //#                            Hessen, Germany</span>
        //#
        //waypointRex        = \\|\\s+GC(.*?)\\s+\\|
        waypointRex.search(aCacheDescriptionOfListPage);
        if (!waypointRex.didMatch()) {
            Preferences.itself().log("[SpiderGC.java:getWP]check waypointRex!", null);
            return "???";
        }
        String stmp = waypointRex.stringMatched(1);
        return "GC" + stmp;
    }

    private boolean isPremiumOnly() {
        if (aCacheDescriptionOfListPage.indexOf(propPM) <= 0) {
            return false;
        } else {
            numPremium = numPremium + 1;
            return true;
        }
    }

    private boolean updateExists(CacheHolder ch) {
        boolean ret = false;
        boolean save = false;
        boolean is_archived_GC = false;
        boolean is_found_GC = false;

        if (ch.isBlack())
            return false;

        if (spiderAllFinds) {
            if (!ch.isFound()) {
                ch.setFound(true);
                save = true;
                numFoundUpdates += 1;
                ret = true;
            }
            is_archived_GC = aCacheDescriptionOfListPage.indexOf(propArchived) != -1;
            if (is_archived_GC != ch.isArchived()) {
                ch.setArchived(is_archived_GC);
                save = true;
                numArchivedUpdates += 1;
                ret = true;
            }
        } else if (!doNotgetFound) { // there could be a found or own ...
            is_found_GC = aCacheDescriptionOfListPage.indexOf(propFound) != -1;
            if (is_found_GC != ch.isFound()) {
                ch.setFound(is_found_GC);
                save = true;
                ret = true;
            }
        }

        if (ch.isFound()) {
            // check for missing ownLogID (and logtext)
            if (ch.getDetails().getOwnLog() == null) {
                ret = true;
            } else {
                if (ch.getDetails().getOwnLog().getLogID().length() == 0)
                    ret = true;
            }
        }

        final boolean is_available_GC = !is_archived_GC && aCacheDescriptionOfListPage.indexOf(propAvailable) == -1;
        if (is_available_GC != ch.isAvailable()) {
            ch.setAvailable(is_available_GC);
            save = true;
            numAvailableUpdates += 1;
            ret = true;
        }
        if (typeChanged(ch)) {
            save = true;
            ret = true;
        }
        if (Preferences.itself().checkDTS) {
            final String dts[] = mString.split(getDTS(), '/');
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
                    Preferences.itself().log("[SpiderGC.java:updateExists]check DTS calculation (DTSRex)! \n" + aCacheDescriptionOfListPage, null);
                } catch (Exception e) {
                }
            }
        }
        if (newFoundExists(ch)) {
            numLogUpdates++;
            ret = true;
        }
        if (!ret) {
            ret = TBchanged(ch);
        }
        if (save)
            ch.saveCacheDetails();
        return ret;
    }

    private boolean typeChanged(CacheHolder ch) {
        listPageTypeRex.search(aCacheDescriptionOfListPage);
        if (listPageTypeRex.didMatch()) {
            String stmp = listPageTypeRex.stringMatched(1);
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

    private String getDTS() {
        // result 3 values separated by /
        String res = "";
        DTSRex.search(aCacheDescriptionOfListPage);
        if (DTSRex.didMatch()) {
            res = DTSRex.stringMatched(1) + "/" + DTSRex.stringMatched(2) + "/" + DTSRex.stringMatched(5);
        }
        return res;
    }

    private boolean difficultyChanged(CacheHolder ch, byte toCheck) {
        if (ch.getDifficulty() == toCheck)
            return false;
        else {
            ch.setDifficulty(toCheck);
            return true;
        }
    }

    private boolean terrainChanged(CacheHolder ch, byte toCheck) {
        if (ch.getTerrain() == toCheck)
            return false;
        else {
            ch.setTerrain(toCheck);
            return true;
        }
    }

    private boolean sizeChanged(CacheHolder ch, byte toCheck) {
        if (ch.getSize() == toCheck)
            return false;
        else {
            ch.setSize(toCheck);
            return true;
        }
    }

    private boolean newFoundExists(CacheHolder ch) {
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

        logDateRex.search(aCacheDescriptionOfListPage);
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

    private boolean TBchanged(CacheHolder ch) {
        // simplified Version: only presence is checked
        if (Preferences.itself().downloadTBs && Preferences.itself().checkTBs) {
            final boolean hasTB = aCacheDescriptionOfListPage.indexOf("data-tbcount") > -1;
            return ch.hasBugs() != (hasTB);
        }
        return false;
    }

    public int fetchWayPointPage(String wayPoint) {
        int ret = SPIDER_OK; // initialize value;
        try {
            wayPointPage = UrlFetcher.fetch("https://www.geocaching.com/seek/cache_details.aspx?wp=" + wayPoint);
            Preferences.itself().log("Fetched: " + wayPoint);
            // Preferences.itself().log("Fetched: " + wayPoint + "\r\n" + wayPointPage);
        } catch (final Exception ex) {
            Preferences.itself().log("Could not fetch " + wayPoint, ex);
            ret = SPIDER_ERROR;
        }
        return ret;
    }

    private int getCacheByWaypointName(boolean fetchTBs) {
        int ret = SPIDER_OK;
        CacheHolder chOld = MainForm.profile.cacheDB.get(newCache.getCode());
        boolean isInDB = chOld != null;
        newCache.setIncomplete(true);
        // if ch is a new object, the <cache>.xml is read again as a new object chD
        while (true) { // retry even if failure
            // Preferences.itself().log(""); // new line for more overview
            int spiderTrys = 0;
            final int MAX_SPIDER_TRYS = 3;
            while (spiderTrys++ < MAX_SPIDER_TRYS) {
                ret = fetchWayPointPage(newCache.getCode());
                if (ret == SPIDER_OK) {
                    newCache.setIsPremiumCache(wayPointPage.indexOf(premiumGeocache) > -1);
                    if (infB.isClosed())
                        ret = SPIDER_CANCEL;
                    else if (newCache.isPremiumCache()) {
                        if (!Preferences.itself().havePremiumMemberRights) {
                            // Premium cache spidered by non premium member
                            // alternative Abfrage
                            // wayPointPage.indexOf("PersonalCacheNote") == -1 // (BasicMember hat keine PersonalCacheNote)
                            // Preferences.itself().log("Ignoring premium member cache: " + ch.getCode(), null);
                            spiderTrys = MAX_SPIDER_TRYS; // retry zwecklos da BasicMember
                            if (isInDB) {
                                chOld.setIsPremiumCache(true);
                                chOld.setLastSync(""); //
                                chOld.saveCacheDetails();
                            } else {
                                if (Preferences.itself().addPremiumGC) {
                                    MainForm.profile.cacheDB.add(newCache);
                                    newCache.saveCacheDetails();
                                }
                            }
                            ret = SPIDER_IGNORE_PREMIUM;
                        }
                    } else if (wayPointPage.indexOf(unpublishedGeocache) > -1) {
                        Preferences.itself().log("unpublished Geocache: " + newCache.getCode(), null);
                        spiderTrys = MAX_SPIDER_TRYS;
                        ret = SPIDER_IGNORE;
                    }
                }
                if (ret == SPIDER_OK) {
                    try {
                        newCache.isHTML(true);
                        newCache.addiWpts.clear();

                        newCache.setAvailable(!(wayPointPage.indexOf(unavailableGeocache) > -1));
                        newCache.setArchived(wayPointPage.indexOf(archivedGeocache) > -1);
                        newCache.setArchived(wayPointPage.indexOf(lockedGeocache) > -1);
                        extractor.set(wayPointPage, correctedCoordinate, ";", 0, Extractor.EXCLUDESTARTEND);
                        String extracted = extractor.findNext();
                        if (extracted.length() > 0) {
                            String newLatLng = extractValue.set(extracted, "newLatLng\":[", "]", 0, Extractor.EXCLUDESTARTEND).findNext();
                            String oldLatLng = extractValue.findNext("oldLatLng\":[");
                            newCache.setIsSolved(true);
                            if (newLatLng.equals(oldLatLng)) {
                                Preferences.itself().log(newCache.getCode() + " coords equal original.", null);
                            }
                        }
                        /*
                        JSONObject initialLogs = new JSONObject(extractor.findNext("initalLogs = ", "};") + "}");
                        JSONObject pageInfo = initialLogs.getJSONObject("pageInfo");
                        int got = pageInfo.getInt("size"); //<=25
                        int max = pageInfo.getInt("totalRows");
                        if (max<=got) {

                        }
                        else {
                        what else
                        }
                        */
                        // Logs: getAllLogs = isFoundByMe = (logged at gc (by spidering account) || marked as found in this profile)
                        getLogs((wayPointPage.indexOf(foundByMe) > -1) || (isInDB && chOld.isFound()));

                        // order of occurrence in wayPointPage
                        wayPointPageIndex = 0;
                        // from <head
                        newCache.setType(wayPointPageGetType());
                        newCache.setDifficulty(wayPointPageGetDiff());
                        newCache.setTerrain(wayPointPageGetTerr());
                        // from <body
                        this.backgroudImageName = wayPointPageGetBackgroundImageUrl();
                        newCache.setName(wayPointPageGetName());
                        String owner = wayPointPageGetOwner();
                        newCache.setOwner(owner);
                        newCache.setHidden(wayPointPageGetDateHidden());
                        newCache.setSize(wayPointPageGetSize());
                        if (Preferences.itself().useGCFavoriteValue)
                            newCache.setNumRecommended(Common.parseInt(wayPointPageGetFavoriteValue()));
                        final String latLon = wayPointPageGetLatLon();
                        if (latLon.equals("???")) {
                            Preferences.itself().log("[SpiderGC.java:getLatLon]check latLonRex!", null);
                            if (spiderTrys == MAX_SPIDER_TRYS)
                                Preferences.itself().log(">>>> Failed to spider Cache. Retry.", null);
                            ret = SPIDER_ERROR;
                            continue;
                        }
                        newCache.setWpt(new CWPoint(latLon));
                        final String location = wayPointPageGetLocation();
                        if (location.length() != 0) {
                            final int countryStart = location.indexOf(",");
                            if (countryStart > -1) {
                                newCacheDetails.setCountry(SafeXML.html2iso8859s1(location.substring(countryStart + 1).trim()));
                                newCacheDetails.setState(SafeXML.html2iso8859s1(location.substring(0, countryStart).trim()));
                            } else {
                                newCacheDetails.setCountry(location.trim());
                                newCacheDetails.setState("");
                            }
                        } else {
                            newCacheDetails.setCountry("");
                            newCacheDetails.setState("");
                        }
                        newCacheDetails.setLongDescription(wayPointPageGetDescription());
                        newCacheDetails.setHints(wayPointPageGetHints());

                        if (fetchTBs)
                            getBugs(newCacheDetails);
                        newCache.hasBugs(newCacheDetails.Travelbugs.size() > 0);
                        this.getImages(newCacheDetails);

                        newCacheDetails.setGCNotes(getNotes());
                        getAddWaypoints(wayPointPage, newCache.getCode(), newCache.isFound());
                        getAttributes(newCacheDetails);
                        newCache.setLastSync((new Time()).format("yyyyMMddHHmmss"));
                        newCache.setIdOC (wayPointPageGetUuid());
                        newCache.setIncomplete(false);
                        Preferences.itself().log("ready " + newCache.getCode() + " : " + newCache.getLastSync());
                        break;
                    } catch (final Exception ex) {
                        Preferences.itself().log("[getCacheByWaypointName: ]Error reading cache: " + newCache.getCode(), ex, true);
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

        } // while(true)

        return ret;
    } // getCacheByWaypointName

    // get from wayPointPage
    private byte wayPointPageGetDiff() {
        difficultyRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (difficultyRex.didMatch()) {
            wayPointPageIndex = difficultyRex.matchedTo();
            return CacheTerrDiff.v1Converter(difficultyRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getDiff]check difficultyRex!", null);
            return CacheTerrDiff.v1Converter("-1");
        }
    }

    private byte wayPointPageGetTerr() {
        terrainRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (terrainRex.didMatch()) {
            wayPointPageIndex = terrainRex.matchedTo();
            return CacheTerrDiff.v1Converter(terrainRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getTerr]check terrainRex!", null);
            return CacheTerrDiff.v1Converter("-1");
        }
    }

    private String wayPointPageGetBackgroundImageUrl() {
        backgroundImageUrlRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (backgroundImageUrlRex.didMatch()) {
            wayPointPageIndex = backgroundImageUrlRex.matchedTo();
            String ret = backgroundImageUrlRex.stringMatched(1);
            int fnStart = ret.lastIndexOf('/');
            if (fnStart > 0)
                ret = ret.substring(fnStart).toLowerCase();
            fnStart = ret.lastIndexOf('?');
            if (fnStart > 0)
                ret = ret.substring(0, fnStart);
            if (ret.length() > 36)
                ret = ret.substring(0, 37);
            return ret;
        }
        return "";
    }

    private byte wayPointPageGetType() {
        cacheTypeRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (cacheTypeRex.didMatch()) {
            wayPointPageIndex = cacheTypeRex.matchedTo();
            String cacheType = cacheTypeRex.stringMatched(1);
            return CacheType.gcSpider2CwType(cacheType);
        } else {
            Preferences.itself().log("[SpiderGC.java:getType]check cacheTypeRex!", null);
            return 0;
        }
    }

    private String wayPointPageGetName() {
        cacheNameRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (cacheNameRex.didMatch()) {
            wayPointPageIndex = cacheNameRex.matchedTo();
            return SafeXML.html2iso8859s1(cacheNameRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getName]check cacheNameRex!", null);
            return "???";
        }
    }

    private String wayPointPageGetOwner() {
        cacheOwnerRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (cacheOwnerRex.didMatch()) {
            wayPointPageIndex = cacheOwnerRex.matchedTo();
            return SafeXML.html2iso8859s1(cacheOwnerRex.stringMatched(1)).trim();
        } else {
            Preferences.itself().log("[SpiderGC.java:getOwner]check cacheOwnerRex!", null);
            return "???";
        }
    }

    private String wayPointPageGetDateHidden() {
        dateHiddenRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (dateHiddenRex.didMatch()) {
            wayPointPageIndex = dateHiddenRex.matchedTo();
            return DateFormat.toYYMMDD(dateHiddenRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getDateHidden]check dateHiddenRex!", null);
            return "???";
        }
    }

    private byte wayPointPageGetSize() {
        sizeRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (sizeRex.didMatch()) {
            wayPointPageIndex = sizeRex.matchedTo();
            return CacheSize.gcSpiderString2Cw(sizeRex.stringMatched(1));
        } else {
            Preferences.itself().log("[SpiderGC.java:getSize]check sizeRex!", null);
            return CacheSize.gcSpiderString2Cw("None");
        }
    }

    private String wayPointPageGetFavoriteValue() {
        favoriteValueRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (favoriteValueRex.didMatch()) {
            wayPointPageIndex = favoriteValueRex.matchedTo();
            return favoriteValueRex.stringMatched(1);
        } else {
            return "";
        }
    }

    private String wayPointPageGetLatLon() {
        latLonRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (latLonRex.didMatch()) {
            wayPointPageIndex = latLonRex.matchedTo();
            return latLonRex.stringMatched(1);
        } else {
            return "???";
        }
    }

    private String wayPointPageGetLocation() {
        cacheLocationRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (cacheLocationRex.didMatch()) {
            wayPointPageIndex = cacheLocationRex.matchedTo();
            return cacheLocationRex.stringMatched(2);
        } else {
            Preferences.itself().log("[SpiderGC.java:getLocation]check cacheLocationRex!", null);
            return "";
        }
    }

    public String getDescription() {
        wayPointPageIndex = 0;
        return wayPointPageGetDescription();
    }

    private String wayPointPageGetDescription() {
        String res = "";
        shortDescRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (!shortDescRex.didMatch()) {
            if (shortDescRex_not_yet_found)
                Preferences.itself().log("[SpiderGC.java:getLongDesc]no shortDesc or check shortDescRex!", null);
            // + Preferences.NEWLINE + doc);
        } else {
            res = shortDescRex.stringMatched(1);
            shortDescRex_not_yet_found = false;
        }
        res += "<br>";
        longDescRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (!longDescRex.didMatch()) {
            Preferences.itself().log("[SpiderGC.java:getLongDesc]check longDescRex!", null);
        } else {
            res += longDescRex.stringMatched(1);
            wayPointPageIndex = longDescRex.matchedTo();
        }
        final int spanEnd = res.lastIndexOf("</span>");
        if (spanEnd >= 0) {
            res = res.substring(0, spanEnd);
        }
        // since internal viewer doesn't show html-entities that are now in cacheDescription
        return SafeXML.html2iso8859s1(res);
    }

    private String wayPointPageGetHints() {
        hintsRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (hintsRex.didMatch()) {
            wayPointPageIndex = hintsRex.matchedTo();
            return hintsRex.stringMatched(1);
        } else {
            Preferences.itself().log("[SpiderGC.java:getHints]check hintsRex!", null);
            return "";
        }
    }

    private String getNotes() {
        if (Preferences.itself().havePremiumMemberRights) {
            notesRex.search(wayPointPage);
            if (notesRex.didMatch()) {
                String tmp = notesRex.stringMatched(1);
                if (tmp.length() > 2)
                    return "<GC>" + tmp + "</GC>";
                else
                    return "";
            } else {
                Preferences.itself().log("[getNotes]check notesRex!", null);
            }
        }
        return "";
    }

    public String getUserToken() {

        RexUserToken.search(wayPointPage);
        if (!RexUserToken.didMatch()) {
            Preferences.itself().log("[SpiderGC.java:getLogs]check RexUserToken!", null);
            return "";
        }
        return RexUserToken.stringMatched(1);
    }

    /**
     * Get the logs
     */
    private void getLogs(boolean isFoundByMe) throws Exception {
        boolean fetchAllLogs = isFoundByMe;
        final LogList reslts = newCacheDetails.CacheLogs;
        reslts.clear();
        final String userToken = getUserToken();
        if (userToken.length() == 0)
            return;
        int idx = 0;
        int nLogs = 0;
        boolean foundown = false;
        boolean fertig = false;
        int num = 100;

        if (maxLogs == -1) {
            fetchAllLogs = true;
        }

        if (!fetchAllLogs) {
            if (maxLogs < 100)
                num = maxLogs + 1;
        }
        int nrOfOwnFinds = 0;
        do {
            idx++;
            String url = "https://www.geocaching.com/seek/geocache.logbook?tkn=" + userToken + "&idx=" + idx + "&num=" + num + "&decrypt=false";
            UrlFetcher.setRequestorProperty("Content-Type", "application/json; charset=UTF-8");
            JSONObject response = null;
            String fetchResult = "";
            try {
                fetchResult = UrlFetcher.fetch(url);
                Preferences.itself().log("" + nLogs); // in 100er Schritten
                response = new JSONObject(fetchResult);
            } catch (Exception e) {
                if (fetchResult == null)
                    fetchResult = "";
                Preferences.itself().log("Error getting Logs. \r\n" + fetchResult, e);
                return;
            }
            if (!response.getString("status").equals("success")) {
                Preferences.itself().log("status is " + response.getString("status"), null);
            }
            final JSONArray data = response.getJSONArray("data");
            fertig = data.length() < num;
            for (int index = 0; index < data.length(); index++) {
                nLogs++;
                final JSONObject entry = data.getJSONObject(index);

                final String icon = entry.getString("LogTypeImage");
                final String name = entry.getString("UserName");
                String logText = SafeXML.html2iso8859s1(entry.getString("LogText"));
                logText = STRreplace.replace(logText, "\u000b", " ");
                logText = STRreplace.replace(logText, "<br/>", "<br>");
                logText = correctSmilies(logText);
                final String visitedDate = DateFormat.toYYMMDD(entry.getString("Visited"));
                final String logID = entry.getString("LogID");
                final String finderID = entry.getString("AccountID");

                // if this log says this Cache is found by me or other logtype
                if ((name.equalsIgnoreCase(Preferences.itself().myAlias)) || (name.equalsIgnoreCase(Preferences.itself().myAlias2))) {
                    if ((icon.equals(icon_smile) || icon.equals(icon_camera) || icon.equals(icon_attended))) {
                        newCache.setFound(true);
                        newCache.setStatus(visitedDate);
                        // final String logId = entry.getString("LogID");
                        newCacheDetails.setOwnLog(new Log(logID, finderID, icon, visitedDate, name, logText));
                        foundown = true;
                        nrOfOwnFinds = nrOfOwnFinds + 1;
                        fertig = true;
                    } else {
                        // make it possible to edit a "write note"
                        if (!newCache.isFound()) {
                            // do not overwrite a find log
                            newCacheDetails.setOwnLog(new Log(logID, finderID, icon, visitedDate, name, logText));
                            newCache.setStatus(newCacheDetails.getOwnLog().icon2Message());
                        }
                    }
                }

                if (nLogs <= maxLogs || fetchAllLogs) {
                    Log l = new Log(logID, finderID, icon, visitedDate, name, logText);
                    reslts.add(l);
                } else {
                    // don't add more logs, but still searching own log
                    if (foundown || !fetchAllLogs) {
                        // ownLog or the last one (perhaps maxLogs + 1, the ownLog is possibly not found)
                        reslts.add(new Log(logID, finderID, icon, visitedDate, name, logText));
                        fertig = true;
                        break;
                    }
                }
            }
        } while (!fertig);

        if (nrOfOwnFinds > 1) {
            Preferences.itself().log("doppelter Fund bei " + newCache.getCode(), null);
        }

        if (nLogs > maxLogs) {
            if (!fetchAllLogs) {
                // there are more logs
                reslts.add(Log.maxLog());
            }
        }
        // Bei Update wird es doppelt berechnet
        newCache.setNoFindLogs(reslts.countNotFoundLogs());
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
     * @param chD The previously fetched wayPointPage
     * @return A HTML formatted string with bug names and there purpose
     */
    public void getBugs(CacheHolderDetail chD) throws Exception {
        chD.Travelbugs.clear();
        if (wayPointPage.indexOf("ctl00_ContentBody_uxTravelBugList_uxNoTrackableItemsLabel") >= 0) {
            return; // there are no trackables
        }
        final Extractor exBlock = new Extractor(wayPointPage, blockExStart, blockExEnd, 0, Extractor.EXCLUDESTARTEND);
        final Extractor exBug = new Extractor("", bugExStart, bugExEnd, 0, Extractor.EXCLUDESTARTEND);
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
                final int idx = linkPlusBug.indexOf(bugLinkEnd);
                if (idx < 0) {
                    Preferences.itself().log("[SpiderGC.java:getBugs]check TBs bugLinkEnd!", null);
                    break; // No link/bug pair found
                }
                link = linkPlusBug.substring(0, idx);
                exBugName.set(linkPlusBug, bugNameExStart, bugNameExEnd, idx, Extractor.EXCLUDESTARTEND);
                if ((bug = exBugName.findNext()).length() > 0) {
                    // Found a bug, get its mission
                    try {
                        infB.setInfo(oldInfoBox + MyLocale.getMsg(5514, "\nGetting bug: ") + SafeXML.html2iso8859s1(bug));
                        bugDetails = UrlFetcher.fetch(link);
                        exBugName.set(bugDetails, bugDetailsStart, bugDetailsEnd, 0, Extractor.EXCLUDESTARTEND); // reusing
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
        // an existing <cache>.xml has been read again (on getting chD)
        // (and is an other object than that in the DB)
        // So the already downloaded images are listed in chD.images
        // But we will build this list from clean
        chD.images.clear();
        spideredUrls = new Vector();
        spiderCounter = 0; // the first file

        // First: Get current image object of waypoint before spidering images.
        String wayPoint = chD.getParent().getCode();
        final CacheHolder oldCh = MainForm.profile.cacheDB.get(wayPoint);
        if (oldCh != null)
            oldImages = oldCh.getDetails().images;
        else
            oldImages = new CacheImages();
        this.getDescriptionImages(chD);
        this.getSpoilerImages(chD);
        this.getLogImages(chD);
        this.cleanupOldImages(chD);
    }

    /**
     * In the long Description
     * the img/src - tags and the a/href - tags
     *
     * @param chD
     */
    private void getDescriptionImages(CacheHolderDetail chD) {
        imageSource = CacheImage.FROMDESCRIPTION;
        String wayPoint = chD.getParent().getCode().toLowerCase();
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
                String imgType = (imgUrl.substring(typeStart).toLowerCase() + "     ").substring(0, 5).trim();
                // Delete possible characters in URL after the image extension
                imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
                if (imgType.startsWith(".jpg") || imgType.startsWith(".jpeg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
                    try {
                        CacheImage imageInfo = spiderImage(wayPoint, imgUrl, imgType);
                        imageInfo.setComment(""); // correct dirty hack
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
                        CacheImage imageInfo = spiderImage(wayPoint, imgUrl, imgType);
                        imageInfo.setComment(null); // correct dirty hack
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
        imageSource = CacheImage.FROMSPOILER;
        String wayPoint = chD.getParent().getCode().toLowerCase();
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
                String imgType = (imgUrl.substring(typeStart).toLowerCase() + "     ").substring(0, 5).trim();
                if (imgType.startsWith(".jpg") || imgType.startsWith(".jpeg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
                    // Delete possible characters in URL after the image extension
                    imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('.') + imgType.length());
                    if (this.backgroudImageName.length() == 0 || imgUrl.indexOf(this.backgroudImageName) < 0) {
                        try {
                            CacheImage cacheImage = spiderImage(wayPoint, imgUrl, imgType);
                            if (cacheImage.getComment().length() == 0) { // dirty hack (using comment):
                                // noch nicht in Description runtergeladen (comment = "") --> also ist es ein spoiler
                                cacheImage.setTitle(extractValue.findNext(">", "<"));
                                String imgComment = extractValue.findNext(imgCommentExStart, imgCommentExEnd);
                                if (imgComment.length() > 0) {
                                    while (imgComment.startsWith("<br />"))
                                        imgComment = imgComment.substring(6);
                                    while (imgComment.endsWith("<br />"))
                                        imgComment = imgComment.substring(0, imgComment.length() - 6);
                                }
                                cacheImage.setComment(imgComment);
                                chD.images.add(cacheImage);
                            }
                        } catch (Exception e) {
                            Preferences.itself().log("Error loading image: " + imgUrl, e);
                        }
                    }
                }
            }
        }
    }

    private void getLogImages(CacheHolderDetail chD) {
        // TODO

    }

    private CacheImage spiderImage(String wayPoint, String originalUrl, String imgType) {
        String downloadUrl = originalUrl;
        String spideredName = downloadUrl;
        if (!downloadUrl.startsWith("http"))
            // only clear if starts with / not ..
            downloadUrl = "https://www.geocaching.com" + downloadUrl;
        else {
            downloadUrl = STRreplace.replace(downloadUrl, "groundspeak", "geocaching");
            // links to images in the description directs to one which has reduced in their size.
            // We like to load the images in their original size:
            // if (imgUrl.startsWith("http://img.geocaching.com/cache/display")) imgUrl = "http://img.geocaching.com/cache" + imgUrl.substring("http://img.geocaching.com/cache/display".length());
            //
            // http://imgcdn.geocaching.com/cache/large/3f8dfccc-958a-4cb8-bfd3-be3ab7db276b.jpg
            // is same as http://img.geocaching.com/cache/3f8dfccc-958a-4cb8-bfd3-be3ab7db276b.jpg
            //
            // nowerdays
            // http://img.geocaching.com/cache/6bcc126a-aa28-4f30-b501-f7ec60409218.jpg
            // is same as
            // https://s3.amazonaws.com/gs-geo-images/6bcc126a-aa28-4f30-b501-f7ec60409218.jpg
            if (downloadUrl.indexOf("geocaching.com") > -1) {
                spideredName = downloadUrl.substring(downloadUrl.lastIndexOf('/'), downloadUrl.lastIndexOf('.'));
                if (downloadUrl.indexOf("www.geocaching.com") == -1) {
                    downloadUrl = "https://img.geocaching.com/cache" + spideredName + imgType;
                }
                // else gc smileys from www.geocaching.com
            } else if ((downloadUrl.indexOf("cloudfront.net") > -1) || (downloadUrl.indexOf("amazonaws.com") > -1)) {
                spideredName = downloadUrl.substring(downloadUrl.lastIndexOf('/'), downloadUrl.lastIndexOf('.'));
                if (spideredName.endsWith("_l")) {
                    spideredName = spideredName.substring(0, spideredName.length() - 2);
                }
                downloadUrl = "https://img.geocaching.com/cache" + spideredName + imgType;
            }
        }

        CacheImage cacheImage = null;
        // Index of already spidered Url in list of spideredUrls
        int idxUrl = spideredUrls.find(spideredName);
        if (idxUrl < 0) {
            // Not yet spidered
            String fileName = wayPoint + "_" + spiderCounter + imgType;
            cacheImage = needsSpidering(originalUrl, fileName);
            if (cacheImage == null) {
                //needsSpidering
                cacheImage = new CacheImage(imageSource);
                try {
                    if (imageSource == CacheImage.FROMDESCRIPTION && downloadDescriptionImages || imageSource == CacheImage.FROMSPOILER && downloadSpoilerImages) {
                        UrlFetcher.fetchDataFile(downloadUrl, MainForm.profile.dataDir + fileName);
                        Preferences.itself().log("[getImages] Loaded image: " + originalUrl + " as " + fileName);
                    }
                } catch (Exception ex) {
                    Preferences.itself().log("[spiderImage] Problem while fetching image", ex);
                }
                cacheImage.setFilename(fileName);
                cacheImage.setURL(originalUrl);
            }
            spideredUrls.add(spideredName); // index spiderCounter
            spiderCounter++;
            cacheImage.setComment(""); // eigentlich überflüssig wegen constructor
        } else {
            // Image already spidered as wayPoint_'idxUrl'
            String fileName = wayPoint + "_" + idxUrl + imgType;
            cacheImage = new CacheImage(imageSource);
            cacheImage.setFilename(fileName);
            cacheImage.setURL(originalUrl);
            // Preferences.itself().log("[getImages] Already loaded image: " + originalUrl + " as " + fileName);
            cacheImage.setComment("noSpoiler");
        }

        cacheImage.setTitle(""); // wayPoint + "_" + chD.images.size()
        return cacheImage;
    }

    private CacheImage needsSpidering(String toCheckUrl, String newFilename) {
        // images of gc don't change, they get a new url
        // the numbering of files (newFilename) must be the same (alternative of renaming not implemented)
        // and they must exist in the filesystem

        CacheImage result = null;
        if (oldImages.size() > spiderCounter) {
            if (toCheckUrl.indexOf("geocaching.com") > -1 || toCheckUrl.indexOf("amazonaws.com") > -1) {
                CacheImage img = oldImages.get(spiderCounter);
                if (img.getURL().equals(toCheckUrl) && img.getFilename().equals(newFilename)) {

                    String location = MainForm.profile.dataDir + newFilename;
                    if ((new File(location)).exists()) {
                        result = img;
                        // Preferences.itself().log("[getImages] Already existing image: " + toCheckUrl + " as " + newFilename);
                    } else {
                        if (imageSource == CacheImage.FROMDESCRIPTION) {
                            if (this.downloadDescriptionImages) {
                                result = null;
                            } else {
                                result = img;
                            }
                        }
                    }

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
     * @param chD to get reference to oldimages
     */
    private void cleanupOldImages(CacheHolderDetail chD) {
        if (oldImages != null) {
            for (int i = spiderCounter; i < oldImages.size(); i++) {
                File tmpFile = new File(MainForm.profile.dataDir + oldImages.get(i).getFilename());
                if (tmpFile.exists() && tmpFile.canWrite()) {
                    // Preferences.itself().log("Image no longer needed. Deleting: " + oldImages.get(i).getFilename());
                    tmpFile.delete();
                }
            }
        }
    }

    /**
     * Read all additional waypoints from a previously fetched cachepage.
     *
     * @param doc      The previously fetched cachepage
     * @param wayPoint The name of the cache
     * @param is_found Found status of the cached (is inherited by the additional waypoints)
     * @throws Exception
     */
    private void getAddWaypoints(String doc, String wayPoint, boolean is_found) throws Exception {
        final Extractor exWayBlock = new Extractor(doc, wayBlockExStart, wayBlockExEnd, 0, false);
        String wayBlock;
        if ((wayBlock = exWayBlock.findNext()).length() > 0) {
            if (wayBlock.indexOf("No additional waypoints to display.") < 0) {
                int counter = 0;
                final Extractor exRowBlock = new Extractor(wayBlock, rowBlockExStart, rowBlockExEnd, 0, false);
                String rowBlock = exRowBlock.findNext();
                while ((rowBlock = exRowBlock.findNext()).length() > 0) {
                    CacheHolder hd = null;

                    final Extractor exPrefix = new Extractor(rowBlock, prefixExStart, prefixExEnd, 0, true);
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
                        hd.setCode(adWayPoint);
                        // Accessing Details reads file if not yet done
                        hd.getDetails();
                    } else {
                        hd = new CacheHolder();
                        hd.setCode(adWayPoint);
                    }
                    hd.initStates(idx < 0);

                    nameRex.search(rowBlock);
                    if (nameRex.didMatch()) {
                        hd.setName(nameRex.stringMatched(1));
                    } else {
                        Preferences.itself().log("check nameRex in spider.def" + Preferences.NEWLINE + rowBlock, null);
                    }

                    koordRex.search(rowBlock);
                    if (koordRex.didMatch()) {
                        hd.setWpt(new CWPoint(koordRex.stringMatched(1)));
                        koords_not_yet_found = false;
                    } else {
                        if (koords_not_yet_found) {
                            koords_not_yet_found = false;
                            if (rowBlock.indexOf("icon_nocoords.jpg") < 0)
                                Preferences.itself().log("check koordRex in spider.def" + Preferences.NEWLINE + rowBlock, null);
                        }
                    }

                    typeRex.search(rowBlock);
                    if (typeRex.didMatch()) {
                        hd.setType(CacheType.gcSpider2CwType(typeRex.stringMatched(1)));
                        //hd.setType(CacheType.gpxType2CwType("Waypoint|" + typeRex.stringMatched(1)));
                    } else {
                        Preferences.itself().log("check typeRex in spider.def" + Preferences.NEWLINE + rowBlock, null);
                    }

                    rowBlock = exRowBlock.findNext();
                    descRex.search(rowBlock);
                    if (descRex.didMatch()) {
                        hd.getDetails().setLongDescription(descRex.stringMatched(1).trim());
                    } else {
                        Preferences.itself().log("check descRex in spider.def" + Preferences.NEWLINE + rowBlock, null);
                    }
                    hd.setFound(is_found);
                    hd.setSize(CacheSize.CW_SIZE_NOTCHOSEN);
                    hd.setDifficulty(CacheTerrDiff.CW_DT_UNSET);
                    hd.setTerrain(CacheTerrDiff.CW_DT_UNSET);

                    if (idx < 0) {
                        MainForm.profile.cacheDB.add(hd);
                        hd.saveCacheDetails();
                    } else {
                        final CacheHolder cx = MainForm.profile.cacheDB.get(idx);
                        final boolean checked = cx.isChecked;
                        cx.initStates(false);
                        cx.update(hd);
                        cx.isChecked = checked;
                        cx.saveCacheDetails();
                    }
                }
            }
        }
    }

    private String wayPointPageGetUuid (){
        uuidRex.searchFrom(wayPointPage, wayPointPageIndex);
        if (uuidRex.didMatch()) {
            wayPointPageIndex = uuidRex.matchedTo();
	    return uuidRex.stringMatched(1);
        } else {
            return "";
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

    public String encodeUTF8URL(byte[] what) {
        final int max = what.length;
        // Assume each char is a UTF char and encoded into 6 chars
        final char[] dest = new char[6 * max];
        char d = 0;
        for (int i = 0; i < max; i++) {
            final char c = (char) what[i];
            if (c <= ' ' || c == '+' || c == '&' || c == '%' || c == '=' || c == '|' || c == '{' || c == '}' || c > 0x7f) {
                dest[d++] = '%';
                dest[d++] = ewe.util.TextEncoder.hex.charAt((c >> 4) & 0xf);
                dest[d++] = ewe.util.TextEncoder.hex.charAt(c & 0xf);
            } else
                dest[d++] = c;
        }
        return new String(dest, 0, d);
    }

    /**
     * Load the bug id for a given name. This method is not ideal, as there are sometimes several bugs with identical names but different IDs. Normally the bug GUID is used which can be obtained from the cache page.<br>
     * Note that each bug has both an ID and a GUID.
     *
     * @param name The name (or partial name) of a travelbug
     * @return the id of the bug
     */
    public String getBugId(String name) {
        String bugList;
        try {
            // infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
            bugList = UrlFetcher.fetch(getBugByNameUrl + STRreplace.replace(SafeXML.string2Html(name), " ", "+"));
            Preferences.itself().log("[getBugId] Fetched bugId: " + name);
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugId] Could not fetch bug list" + name, ex);
            bugList = "";
        }
        try {
            if (bugList.equals("") || bugList.indexOf(bugNotFound) >= 0) {
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6020, "Travelbug not found.")).wait(FormBase.OKB);
                return "";
            }
            if (bugList.indexOf(bugTotalRecords) < 0) {
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6021, "More than one travelbug found. Specify name more precisely.")).wait(FormBase.OKB);
                return "";
            }
            final Extractor exGuid = new Extractor(bugList, bugGuidExStart, bugGuidExEnd, 0, Extractor.EXCLUDESTARTEND);
            return exGuid.findNext();
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugId] Error getting TB", ex);
            return "";
        }
    }

    /**
     * Fetch a bug's mission for a given GUID or ID. If the guid String is longer than 10 characters it is assumed to be a GUID, otherwise it is an ID.
     *
     * @param guid the guid or id of the travelbug
     * @return The mission
     */
    public String getBugMissionByGuid(String guid) {
        String bugDetails;
        try {
            // infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
            if (guid.length() > 10)
                bugDetails = UrlFetcher.fetch(getBugByGuidUrl + guid);
            else
                bugDetails = UrlFetcher.fetch(getBugByIdUrl + guid);
            Preferences.itself().log("[getBugMissionByGuid] Fetched TB detailsById: " + guid);
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionByGuid] Could not fetch TB details " + guid, ex);
            bugDetails = "";
        }
        try {
            if (bugDetails.indexOf(bugNotFound) >= 0) {
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(6020, "Travelbug not found.")).wait(FormBase.OKB);
                return "";
            }
            final Extractor exDetails = new Extractor(bugDetails, bugDetailsStart, bugDetailsEnd, 0, Extractor.EXCLUDESTARTEND);
            return exDetails.findNext();
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionByGuid] Error getting TB " + guid, ex);
            return "";
        }
    }

    /**
     * Fetch a bug's mission for a given tracking number
     *
     * @param trackNr the tracking number of the travelbug
     * @return The mission
     */
    public String getBugMissionByTrackNr(String trackNr) {
        String bugDetails;
        try {
            bugDetails = UrlFetcher.fetch(getBugByTrackNrUrl + trackNr);
            Preferences.itself().log("[getBugMissionByTrackNr] Fetched bug detailsByTrackNr: " + trackNr);
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionByTrackNr] getBugByTrackNr " + trackNr, ex);
            bugDetails = "";
        }
        try {
            if (bugDetails.indexOf(bugNotFound) >= 0) {
                Preferences.itself().log("[getBugMissionByTrackNr], bugNotFound " + trackNr, null);
                return "";
            }
            final Extractor exDetails = new Extractor(bugDetails, bugDetailsStart, bugDetailsEnd, 0, Extractor.EXCLUDESTARTEND);
            return exDetails.findNext();
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionByTrackNr] TB Details, bugNotFound " + trackNr, ex);
            return "";
        }
    }

    /**
     * Fetch a bug's mission and namefor a given tracking number
     *
     * @param TB the travelbug
     * @return true if suceeded
     */
    public boolean getBugMissionAndNameByTrackNr(Travelbug TB) {
        String bugDetails;
        final String trackNr = TB.getTrackingNo();
        try {
            bugDetails = UrlFetcher.fetch(getBugByTrackNrUrl + trackNr);
            Preferences.itself().log("[getBugMissionAndNameByTrackNr] Fetched TB getBugByTrackNr: " + trackNr);
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionAndNameByTrackNr] Could not fetch bug details: " + trackNr, ex);
            bugDetails = "";
        }
        try {
            if (bugDetails.indexOf(bugNotFound) >= 0) {
                Preferences.itself().log("[getBugMissionAndNameByTrackNr], bugNotFound: " + trackNr, null);
                return false;
            }
            final Extractor exDetails = new Extractor(bugDetails, bugDetailsStart, bugDetailsEnd, 0, Extractor.EXCLUDESTARTEND);
            TB.setMission(exDetails.findNext());
            final Extractor exName = new Extractor(bugDetails, bugNameStart, bugNameEnd, 0, Extractor.EXCLUDESTARTEND);
            TB.setName(exName.findNext());
            return true;
        } catch (final Exception ex) {
            Preferences.itself().log("[getBugMissionAndNameByTrackNr] TB Details, bugNotFound: " + trackNr, ex);
            return false;
        }
    }

    class SpiderProperties extends Properties {

        public SpiderProperties() {
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
         * @param key The key which is attributed to a specific entry
         * @return The value for the key
         * @throws Exception When a key is requested which doesn't exist
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
                    s = r.readString(1); // reading the bom will result in error
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
