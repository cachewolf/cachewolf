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

/// Cache-Details:
// curl 'https://www.opencaching.de/okapi/services/caches/geocache?consumer_key=EgcYTe8ZZsWd4PqGXNu6&cache_code=OC15EC9&langpref=de&fields=code|name|location|type|status|owner|founds|size2|difficulty|terrain|short_descriptions|descriptions|hint2|images|attrnames|latest_logs|trackables|alt_wpts|date_hidden|internal_id'
// Cache-Suche: https://www.opencaching.de/okapi/services/caches/search/nearest?center=50.4|7.432&consumer_key=EgcYTe8ZZsWd4PqGXNu6&radius=20

//Mit found-flag: https://www.opencaching.de/okapi/services/caches/geocache?consumer_key=EgcYTe8ZZsWd4PqGXNu6&cache_code=OC137FD&langpref=de&fields=name|location|type|status|owner|gc_code|size2|difficulty|terrain|short_description|description|hints2|images|trackables|alt_wpts|attr_acodes|date_hidden|internal_id|code|recommendations|latest_logs|is_found&user_uuid=a57adda6-25d8-102b-9493-00163e103232&lpc=all
//User-Infos: https://www.opencaching.de/okapi/services/users/by_username?consumer_key=EgcYTe8ZZsWd4PqGXNu6&username=colleisarco&fields=uuid
package CacheWolf.imp;

import CacheWolf.MainForm;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.*;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.*;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.BufferedReader;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.TextReader;
import ewe.net.MalformedURLException;
import ewe.net.URL;
import ewe.sys.Convert;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to import Data from opencaching. It uses the lastmodified parameter to identify new or changed caches. See here: http://www.opencaching.com/phpBB2/viewtopic.php?t=281 (out-dated) See here: http://www.opencaching.de/doc/xml/xml11.htm and
 * http://develforum.opencaching.de/viewtopic.php?t=135&postdays=0&postorder=asc&start=0 for more information.
 */
public class OCXMLImporter {
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
    private Hashtable DBindexID = new Hashtable();

    String picUrl = "";
    String picTitle = "";
    String picID;
    String cacheID;

    String logData, logIcon, logDate, logFinder, logId, finderID;
    boolean loggerRecommended;
    int logtype;
    String user;

    /**
     * Temporarly save the values from XML
     */
    double longitude;
    /**
     * Temporarly save the values from XML: set to the language of the description which is currently parsed
     */
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
            if (!ch.getIdOC().equals(""))
                DBindexID.put(ch.getIdOC(), ch.getCode());
        }

    }

    /**
     * @param number
     * @param infB
     * @return true, if some change was made to the cacheDB
     */
    public boolean syncSingle(int number, InfoBox infB) {

        CacheHolder ch;
        ch = cacheDB.get(number);
        hostname = OC.getOCHostName(ch.getCode());
        holder = null;

        if (infB.isClosed()) {
            // there could have been an update before
            return true;
        }

        inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608, "downloading data\n from " + hostname), InfoBox.PROGRESS_WITH_WARNINGS, false);
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
        ch.setUpdated(false);
        isSyncSingle = true;
        try{
            updateOkapi(ch.getCode());
        }
        catch (Exception e){
            Preferences.itself().log ("Error while updating OC-Cache: " + e);
            return false;
        }
        finally{
            inf.close(0);
        }

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
        final ImportGui importGui = new ImportGui(MyLocale.getMsg(130, "Download from opencaching"), ImportGui.ALL | ImportGui.DIST | ImportGui.INCLUDEFOUND | ImportGui.HOST, ImportGui.DESCRIPTIONIMAGE | ImportGui.SPOILERIMAGE | ImportGui.LOGIMAGE);
        if (importGui.execute() == FormBase.IDCANCEL) {
            return;
        }
        downloadPics = importGui.downloadDescriptionImages;
        Vm.showWait(true);
        String dist = importGui.maxDistanceInput.getText();
        incFinds = !importGui.foundCheckBox.getState();
        if (importGui.domains.getSelectedItem() != null) {
            hostname = (String) importGui.domains.getSelectedItem();
            Preferences.itself().lastOCSite = hostname;
        }
        if (dist.length() == 0)
            return;
        dist = Common.DoubleToString(Common.parseDouble(dist), 0, 1);
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
            ch.setLogUpdated(false);
        }
        picCnt = 0;
        String okapiUrl = "https://www.opencaching.de/okapi/services/caches/search/nearest?center=" + centre.getLatDeg(TransformCoordinates.DD) + "%7C" + centre.getLonDeg(TransformCoordinates.DD) + "&consumer_key=EgcYTe8ZZsWd4PqGXNu6&radius=" + dist;

        Preferences.itself ().log ("Downloading from OC:" + okapiUrl);
        inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608, "downloading data\n from opencaching"), InfoBox.PROGRESS_WITH_WARNINGS, false);
        inf.relayout(false);
        inf.exec();

        isSyncSingle = false;
        success = syncOkapi(okapiUrl);
        //TODO: lastSync setzen und beim importieren zum Abgleich verwenden:
 
        MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR, Profile.FORCESAVE);
        Vm.showWait(false);
        if (success) {
            MainForm.profile.setLast_sync_opencaching(/*TODO: dateOfthisSync.format("yyyyMMddHHmmss")*/"20200731165908");
            // Preferences.itself().savePreferences();
            finalMessage = MyLocale.getMsg(1607, "Update from opencaching successful");
            inf.addWarning("Number of" + "\n...caches new/updated: " + numCacheImported + " / " + numCacheUpdated + "\n...cache descriptions new/updated: " + numDescImported + "\n...logs new/updated: " + numLogImported);
            inf.setInfo(finalMessage);
        }
        inf.showButton(FormBase.YESB);
    }

    private String getUserUuid () throws JSONException, IOException{
        final String username = URL.encodeURL(Preferences.itself().myAlias,true);
        String url = "https://www.opencaching.de/okapi/services/users/by_username?consumer_key=EgcYTe8ZZsWd4PqGXNu6&username="+username+"&fields=uuid";
        final String response = UrlFetcher.fetch(url);
        final JSONObject responseJson = new JSONObject(response);
        Preferences.itself().log ("Users UUID is: " + responseJson);
        return responseJson.getString("uuid");
    }

    private boolean syncOkapi(String url){
        String finalMessage = "Import successful";
        try{
            final String listOfAllCaches = UrlFetcher.fetch (url);
            Preferences.itself().log ("The result from cachesearch: \n" + listOfAllCaches + "\n\n");
            final JSONObject response = new JSONObject(listOfAllCaches);
            final JSONArray results = response.getJSONArray("results");
            for (int index = 0; index < results.length(); index++) {
                //einzelnen Cache einlesen
                final Object ocCode = results.get(index);
                updateOkapi (ocCode.toString());
            }
            
            return true;
        }
        catch (IOException e){
            finalMessage = "Could not get cache-list:" + e.getMessage();
            return false;
        }
        catch (JSONException e){
            finalMessage = "Could not parse JSON" + e.getMessage();
            return false;
        }
        finally{
            inf.setInfo(finalMessage);
        }
    }

    private boolean updateOkapi(final String ocCode) throws IOException, JSONException{
        //TODO: is_found mit User-Id und Anzahl logs.
        final String detailUrl = ("https://www.opencaching.de/okapi/services/caches/geocache?"+
                                  "cache_code="+ocCode+
                                  "&langpref=de&"+
                                  "fields=name|location|type|status|owner|gc_code|size2|difficulty|terrain|short_description|description|hints2|images|trackables|alt_wpts|attr_acodes|date_hidden|internal_id|code|recommendations|latest_logs|is_found&"+
                                  "user_uuid="+(URL.encodeURL (getUserUuid(), true))+
                                  "&consumer_key=EgcYTe8ZZsWd4PqGXNu6"+
                                  "&lpc=all").replaceAll("\\|","%7C");
        Preferences.itself().log ("CacheDetail-URL: [" + detailUrl + "]");
        final String cacheAsJsonString = UrlFetcher.fetch(detailUrl,true);
        Preferences.itself().log ("CacheDetails: " + cacheAsJsonString);

        //------------
        final int index = cacheDB.getIndex(ocCode);
        final CacheHolder syncHolder;
        if (index == -1) {
            syncHolder = new CacheHolder();
	    syncHolder.setCode (ocCode);
            Preferences.itself().log("Importing new Cache!");
            numCacheImported++;
            syncHolder.setNew(true);
            cacheDB.add(syncHolder);
            DBindexID.put(syncHolder.getIdOC(), syncHolder.getCode());
        }
        // update (overwrite) data
        else {
            syncHolder = cacheDB.get(index);

            Preferences.itself().log("Updating existing Cache!");
            numCacheUpdated++;
            syncHolder.setNew(false);
            syncHolder.setIncomplete(false);
            cacheDB.get(index).update(syncHolder);
            DBindexID.put(syncHolder.getIdOC(), syncHolder.getCode());
        }
	syncHolder.getDetails().setURL("https://opencaching.de/" + ocCode.toUpperCase());
        // clear data (picture, logs) if we do a complete Update
        if (!incUpdate) {
            syncHolder.getDetails().getCacheLogs().clear();
        }
	//Images will always be (re)loaded
	syncHolder.getDetails().getImages().clear();
	//TODO: Delete existing images from filesystem

        JSONObject cacheAsJson = new JSONObject(cacheAsJsonString);
        syncHolder.setName(cacheAsJson.getString("name"));
        final JSONObject ownerObject = cacheAsJson.getJSONObject("owner");
        syncHolder.setOwner(ownerObject.getString("username"));
        final String locationText = cacheAsJson.getString("location");
        syncHolder.getWpt().latDec = Common.parseDouble(locationText.substring(0, locationText.indexOf('|')));
        syncHolder.getWpt().lonDec = Common.parseDouble(locationText.substring(locationText.indexOf('|')+1));
        syncHolder.setDifficulty ((byte)(cacheAsJson.getDouble("difficulty")*10));
        syncHolder.setTerrain ((byte)(cacheAsJson.getDouble("terrain")*10));
        final String hiddenText = cacheAsJson.getString("date_hidden");
        syncHolder.setHidden(hiddenText.substring(0, hiddenText.indexOf('T')));
        syncHolder.setCode(cacheAsJson.get("code").toString());
        final String statusText = cacheAsJson.getString("status");
        //Order is important: setAvailable overwrites archived if the value is true:
        syncHolder.setAvailable (!("Temporarily unavailable".equals(statusText)));
        syncHolder.setArchived("Archived".equals(statusText));
        boolean isFound = cacheAsJson.getBoolean("is_found");
        syncHolder.setFound(isFound);

        final String typeString = cacheAsJson.getString("type");
        syncHolder.setType(translateType(typeString));
        //result.idOC = (String) attributes.get("ocCacheID"); ???
        final Time lastSync = new Time();
        Preferences.itself().log("Aktuelle Zeit ist: " + lastSync.format("yyyyMMddHHmmss"));
        syncHolder.setLastSync (lastSync.format("yyyyMMddHHmmss"));
        syncHolder.setNumRecommended (cacheAsJson.getInt("recommendations"));
        final String sizeText = cacheAsJson.getString("size2");
        syncHolder.setSize(translateSize(sizeText));

        //Attributes setzen:
        setAttribute (syncHolder, cacheAsJson.getJSONArray("attr_acodes"));

        syncHolder.getDetails().setLongDescription(cacheAsJson.getString("description"));
        final JSONObject hintsObject = cacheAsJson.getJSONObject("hints2");
        if (hintsObject.has("de")){
            final String hintsText = hintsObject.getString("de");
            syncHolder.getDetails().setHints(Common.rot13(hintsText));
        }

        LogList cacheLogs = readLogList(cacheAsJson.getJSONArray("latest_logs"));
        syncHolder.getDetails().getCacheLogs().purgeLogs();
        for (int i=0; i < cacheLogs.size(); i++){
            syncHolder.getDetails().getCacheLogs().merge(cacheLogs.getLog(i));
        }
        CacheImages imageList = readImageList(cacheAsJson.getJSONArray("images"));
        loadPictures (syncHolder, imageList);

	loadAdditionalWaypoints(cacheAsJson.getJSONArray ("alt_wpts"));

        // save all
        syncHolder.getDetails().saveCacheXML(MainForm.profile.dataDir);
        syncHolder.getDetails().setUnsaved(true); // this makes CachHolder save the details in case that they are unloaded from memory

        return true;
    }

    private void loadPictures (final CacheHolder cacheHolder, final CacheImages cacheImages){
        getImageNamesFromDescription(cacheHolder);
        for (int i=0; i < cacheImages.size();i++){
            CacheImage cacheImage = cacheImages.get(i);
            getPic(cacheHolder, cacheImage);
        }
    }

    private CacheImages readImageList(final JSONArray listOfJsonImages) throws JSONException{
        final CacheImages result = new CacheImages();
        for (int i=0; i < listOfJsonImages.length(); i++){
            JSONObject imageAsJson = listOfJsonImages.getJSONObject(i);
            final String url = imageAsJson.getString("url");
            final String caption = imageAsJson.getString("caption");
            final boolean isSpoiler = imageAsJson.getBoolean("is_spoiler");
            final CacheImage cacheImage = new CacheImage(isSpoiler?CacheImage.FROMSPOILER:CacheImage.FROMDESCRIPTION);
            cacheImage.setTitle(caption);
            cacheImage.setURL(url);
            result.add(cacheImage);
        }
        return result;
    }
    
    private LogList readLogList (final JSONArray listOfJsonLogs) throws JSONException{
        final LogList result = new LogList();
        for (int i=0; i < listOfJsonLogs.length(); i++){
            JSONObject logAsJson = listOfJsonLogs.getJSONObject(i);
            JSONObject user = logAsJson.getJSONObject("user");
            String logID = logAsJson.getString("uuid");
            String finderId= user.getString("uuid");
            String icon = translateIcon(logAsJson.getString("type"));
            String date = logAsJson.getString("date").substring(0, 10);
            String logger = user.getString("username");
            String message = logAsJson.getString("comment");
            Log log = new Log(logID, finderID, icon, date, logger, message);
            result.add (log);
        }

        return result;
    }

    private String translateIcon(final String input){
        if (input.equals("Found it")){
                return Log.typeText2Image("Found");
        }
        else if(input.equals("Didn't find it")){
                    return Log.typeText2Image("Not Found");
        }
        else{
             return Log.typeText2Image("Note");
        }
    }

    private byte translateType(final String input){
        byte result = CacheType.ocType2CwType(input);
        if (result == -1){
            throw new IllegalArgumentException ("Can not handle type " + input);
        }
        else{
            return result;
        }
    }

    private byte translateSize(final String input){
        byte result = CacheSize.ocXmlString2Cw(input);
        return result;
    }

    private void setAttribute(CacheHolder holder, JSONArray attributes) throws JSONException{
        for (int i=0; i < attributes.length();i++){
            final String attributeName = attributes.getString(i);
            holder.getDetails()
                .getAttributes()
                .addByOcId (attributeName);
        }
    }

    private void getImageNamesFromDescription(final CacheHolder syncHolder) {
        String fetchUrl;
        String imgTag;
        String imgAltText;
        final Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); // Ergebnis enthaelt keine Anfuehrungszeichen
        final Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get alternative text for Pic
        imgRegexAlt.setIgnoreCase(true);
        imgRegexUrl.setIgnoreCase(true);
        int descIndex = 0;
        int numDownloaded = 1;
        while (imgRegexUrl.searchFrom(syncHolder.getDetails().getLongDescription(), descIndex)) { // "img" found
            imgTag = imgRegexUrl.stringMatched(1); // (1) enthlt das gesamte <img ...>-tag
            fetchUrl = imgRegexUrl.stringMatched(2); // URL in Anfhrungszeichen in (2) falls ohne in (3) Ergebnis ist auf jeden Fall ohne Anfhrungszeichen
            if (fetchUrl == null) {
                fetchUrl = imgRegexUrl.stringMatched(3);
            }
            if (fetchUrl == null) {
                // TODO Fehler ausgeben: nicht abgedeckt ist der Fall, dass in einem Cache Links auf Bilder mit unterschiedlichen URL, aber gleichem Dateinamen sind.
                inf.addWarning(MyLocale.getMsg(1617, "Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache " + syncHolder.getCode()));
                continue;
            }
            inf.setInfo(MyLocale.getMsg(1611, "Importing cache description:") + " " + numDescImported + "\n" + MyLocale.getMsg(1620, "downloading embedded images: ") + numDownloaded++);
            if (imgRegexAlt.search(imgTag)) {
                imgAltText = imgRegexAlt.stringMatched(1);
                if (imgAltText == null)
                    imgAltText = imgRegexAlt.stringMatched(2);
                // no alternative text as image title -> use filename
            }
            else {
                if (fetchUrl.toLowerCase().indexOf("opencaching.") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0){
                    // wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend
                    imgAltText = "No image title";
                }
                else{
                    imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/') + 1);
                }
            }
            descIndex = imgRegexUrl.matchedTo();
            try {
                // TODO this is not quite correct: actually the "base" URL must be known...
                // but anyway a different baseURL should not happen very often - it doesn't in my area
                if (!fetchUrl.startsWith("https://")) {
                    fetchUrl = new URL(new URL("https://" + hostname + "/"), fetchUrl).toString();
                }
            } catch (final MalformedURLException e) {
                final String ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + syncHolder.getCode() + ": ignoring MalformedUrlException: " + e.getMessage() + " while downloading from URL:" + fetchUrl;
                inf.addWarning("\n" + ErrMessage);
                Preferences.itself().log(ErrMessage, e);
            }
            final CacheImage imageInfo = new CacheImage(CacheImage.FROMDESCRIPTION);
            imageInfo.setURL(fetchUrl);
            imageInfo.setTitle(imgAltText);
            getPic(syncHolder, imageInfo);
        }
    }

    private void getPic(final CacheHolder cacheHolder, final CacheImage imageInfo) { // TODO handling of relativ URLs
        String fileName = cacheHolder.getCode() + "_" + imageInfo.getURL().substring(imageInfo.getURL().lastIndexOf('/') + 1);
        fileName = Common.ClearForFileName(fileName).toLowerCase();
        final String target = MainForm.profile.dataDir + fileName;
        imageInfo.setFilename(fileName);
        try {
            File ftest = new File(target);
            if (ftest.exists()) {
                if (ftest.length() == 0) {
                    ftest.delete();
                }
                else {
                    cacheHolder.getDetails().getImages().add(imageInfo);
                }
            }
            else {
                if (downloadPics) {
                    UrlFetcher.fetchDataFile(imageInfo.getURL(), target);
                    ftest = new File(target);
                    if (ftest.exists()) {
                        if (ftest.length() > 0) {
                            cacheHolder.getDetails().getImages().add(imageInfo);
                        }
                        else {
                            ftest.delete();
                        }
                    }
                }
            }
        }
        catch (final IOException e) {
            String ErrMessage;
            String wp, n;
            if (cacheHolder != null && cacheHolder.getCode() != null)
                wp = cacheHolder.getCode();
            else
                wp = "WP???";
            if (cacheHolder != null && cacheHolder.getName() != null)
                n = cacheHolder.getName();
            else
                n = "name???";

            String m;
            try {
                m = e.getMessage();
                if (m == null)
                    m = "";
            }
            catch (Exception e2) {
                m = "";
            }

            if (m.length() == 0)
                ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: " + fileName + " from URL:" + imageInfo.getURL();
            else {
                if (m.equalsIgnoreCase("could not connect") || m.equalsIgnoreCase("unkown host")) {
                    // is there a better way to find out what happened?
                    ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + ")" + MyLocale.getMsg(1619, ": could not download image from URL: ") + imageInfo.getURL();
                }
                else {
                    ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + "): ignoring IOException: " + m + " while downloading picture:" + fileName + " from URL:" + imageInfo.getURL();
                }
            }
            inf.addWarning(ErrMessage);
            Preferences.itself().log(ErrMessage, e, true);
        }
    }

    private void loadAdditionalWaypoints(final JSONArray altWptList) throws JSONException{
	if (altWptList == null){
	    return;
	}
       
	//TODO: Clean-Code: Seems to be duplicated from above. Check and extract methods for the same:
	for (int i = 0; i < altWptList.length (); i++){
	    JSONObject altWaypoint = (JSONObject) altWptList.get(i);
	    Preferences.itself().log("Additional waypoint " +altWaypoint);
	    final String ocCode = altWaypoint.getString("name");
	    //--
	    final int index = cacheDB.getIndex(ocCode);
	    final CacheHolder syncHolder;
	    if (index == -1) {
		syncHolder = new CacheHolder();
		syncHolder.setCode (ocCode);
		Preferences.itself().log("Importing new additional waypoint!");
		syncHolder.setNew(true);
		cacheDB.add(syncHolder);
		DBindexID.put(syncHolder.getIdOC(), syncHolder.getCode());
	    }
	    // update (overwrite) data
	    else {
		syncHolder = cacheDB.get(index);
		Preferences.itself().log("Updating existing additional waypoint!");
		syncHolder.setNew(false);
		syncHolder.setIncomplete(false);
		cacheDB.get(index).update(syncHolder);
		DBindexID.put(syncHolder.getIdOC(), syncHolder.getCode());
	    }
	    //-- TODO: Duplicated code: The same has been implemented above
	    final String locationText = altWaypoint.getString("location");
	    syncHolder.getWpt().latDec = Common.parseDouble(locationText.substring(0, locationText.indexOf('|')));
	    syncHolder.getWpt().lonDec = Common.parseDouble(locationText.substring(locationText.indexOf('|')+1));
	    

	    syncHolder.setType (translateType(altWaypoint.getString("gc_type")));
	    syncHolder.setName (altWaypoint.getString("type_name"));
	    syncHolder.getDetails().saveCacheXML(MainForm.profile.dataDir);
	    syncHolder.getDetails().setUnsaved(true); // this makes CachHolder save the details in case that they are unloaded from memory

	}
	    /*
	      name	"OC13356-2"
	      location	"50.23245|8.543917"
	      type	"parking"
	      type_name	"Parkplatz"
	      gc_type	"Parking Area"
	      sym	"Parking Area"
	      description	"Forellengut"
	    */
    }
}
