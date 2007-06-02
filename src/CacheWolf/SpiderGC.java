    /*
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
    the Free Software Foundation version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */

package CacheWolf;
import ewe.net.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;
import com.stevesoft.ewe_pat.*;
import ewe.ui.*;

/**
*	Class to spider caches from gc.com
*/
public class SpiderGC{
	
	/**
	 * The maximum number of logs that will be stored
	 */
	public static int MAXLOGS=250;
	
	private static int ERR_LOGIN = -10;
	private static Preferences pref;
	private Profile profile;
	static String viewstate = "";
	static String passwort = "";
	static String cookieID = "";
	static String cookieSession = "";
	static double distance = 0;
	Regex inRex = new Regex();
	Vector cacheDB;
	Vector cachesToLoad = new Vector();
	Hashtable indexDB;
	InfoBox infB;
	private static Properties p=null; 
	
	public SpiderGC(Preferences prf, Profile profile, boolean bypass){
		this.profile=profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		try {
			if (p==null) {
				p=new Properties();
				p.load(new FileInputStream(File.getProgramDirectory()+"/spider.def"));
			}
		} catch (Exception ex) {
			p=null;
			pref.log("Failed to load spider.def",ex);
			// We don't display an error message box here, as the call to 
			// spiderSingle or doIt will do this
		}
	}
	
	/**
	 * Method to login the user to gc.com
	 * It will request a password and use the alias defined in preferences
	 */
	public int login(){
		pref.logInit();
		//Access the page once to get a viewstate
		String start,doc,loginPage;
		//Get password
		InfoBox infB = new InfoBox("Password", "Enter password:", InfoBox.INPUT);
		infB.feedback.setText(passwort); // Remember the PWD for next time
		int code = infB.execute();
		passwort = infB.getInput();
		infB.close(0);
		if(code != Form.IDOK)
			return code;
		infB = new InfoBox("Status", "Logging in...");
		infB.exec();
		try{
			pref.log("Fetching login page");
			start = fetch(loginPage=p.getProperty("loginPage"));   //http://www.geocaching.com/login/Default.aspx
		}catch(Exception ex){
			pref.log("Could not fetch: gc.com start page",ex);
			passwort="";
			return ERR_LOGIN;
		}
		if (!infB.isClosed) {
			Regex rexCookieID = new Regex("Set-Cookie: userid=(.*?);.*");
			Regex rex = new Regex("name=\"__VIEWSTATE\" value=\"(.*)\" />");
			Regex rexCookieSession = new Regex("Set-Cookie: ASP.NET_SessionId=(.*?);.*");
			rex.search(start);
			if(rex.didMatch()){
				viewstate = rex.stringMatched(1);
				//Vm.debug("ViewState: " + viewstate);
			}
			//Ok now login!
			try{
				pref.log("Logging in as "+pref.myAlias);
				doc = URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false)
					+ "&" + URL.encodeURL("myUsername",false) +"="+ URL.encodeURL(pref.myAlias,false)
				    + "&" + URL.encodeURL("myPassword",false) +"="+ URL.encodeURL(passwort,false)
				    + "&" + URL.encodeURL("cookie",false) +"="+ URL.encodeURL("on",false)
				    + "&" + URL.encodeURL("Button1",false) +"="+ URL.encodeURL("Login",false);
				start = fetch_post(loginPage, doc, p.getProperty("nextPage"));  // /login/default.aspx
				pref.log("Login successful");
			}catch(Exception ex){
				//Vm.debug("Could not login: gc.com start page");
				pref.log("Login failed.", ex);
				infB.close(0);
				return ERR_LOGIN;
			}
			
			rex.search(start);
			viewstate = rex.stringMatched(1);
			rexCookieID.search(start);
			cookieID = rexCookieID.stringMatched(1);
			//Vm.debug(cookieID);
			rexCookieSession.search(start);
			cookieSession = rexCookieSession.stringMatched(1);
			//Vm.debug(cookieSession);
		}
		boolean loginAborted=infB.isClosed;
		infB.close(0);
		if (loginAborted)
			return Form.IDCANCEL;
		else
			return Form.IDOK;
	}
	
	/**
	 * Method to spider a single cache.
	 * It assumes a login has already been performed!
	 * @return True if spider was successful, false if spider was cancelled by closing the infobox
	 */
	public boolean spiderSingle(int number, InfoBox infB){
		boolean ret=false;
		this.infB = infB;
		if (p==null) {
			(new MessageBox(MyLocale.getMsg(5500,"Error"), "Could not load 'spider.def'", MessageBox.OKB)).execute();
			return false;
		}
		CacheHolder ch = (CacheHolder)cacheDB.get(number);
		if (ch.isAddiWpt()) return false;  // No point re-spidering an addi waypoint, comes with parent

		CacheHolderDetail chD=new CacheHolderDetail(ch);
		try{
			// Get all existing details of the cache
			chD.readCache(profile.dataDir);
			// Read the cache data from GC.COM and compare to old data
			ret=getCacheByWaypointName(chD,true,true,false,true);
			// Save the spidered data 
			if (ret) {
				pref.log("Saving to:" + profile.dataDir);
				chD.saveCacheDetails(profile.dataDir);
				cacheDB.set(number, new CacheHolder(chD)); // TODO Could copy into existing object
			}
		}catch(IOException ioex){
			pref.log("Could not load " + chD.wayPoint + "file in spiderSingle");
		}
		return ret;
	} // spiderSingle
	
	/**
	*	Method to start the spider for a search around the center coordinates
	*/
	public void doIt(){
		String postStr, dummy, ln, wpt;
		Regex lineRex;
		CacheHolderDetail chD;
		if (p==null) {
			(new MessageBox(MyLocale.getMsg(5500,"Error"), "Could not load 'spider.def'", MessageBox.OKB)).execute();
			return;
		}
		CWPoint origin = pref.curCentrePt; // No need to copy curCentrePt as it is only read and not written
		if (!origin.isValid()) {
			(new MessageBox("Error", "Coordinates for center must be set", MessageBox.OKB)).execute();
			return;
		}
		// Prepare an index of caches for faster searching
		indexDB = new Hashtable(cacheDB.size());
		CacheHolder ch;
		//index the database for faster searching!
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			indexDB.put((String)ch.wayPoint, new Integer(i));
			ch.is_new = false;
		}
		String start = "";
		Regex rex = new Regex("name=\"__VIEWSTATE\" value=\"(.*)\" />");
		String doc = "";
		
		int ok = login();
		if(ok == Form.IDCANCEL) {
			return;
		}
		if(ok == ERR_LOGIN){
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5501,"Login failed!"), MessageBox.OKB)).execute();
			return;
		}
		OCXMLImporterScreen options = new OCXMLImporterScreen("Spider Options", OCXMLImporterScreen.INCLUDEFOUND);
		options.distanceInput.setText("");
		if (options.execute() == OCXMLImporterScreen.IDCANCEL) {return; }
		String dist = options.distanceInput.getText();
		if (dist.length()== 0) return;
		distance = Convert.toDouble(dist);
		boolean doNotgetFound = options.foundCheckBox.getState();
		boolean getImages = options.imagesCheckBox.getState();
		options.close(0);
		
		//=======
		// Prepare list of all caches that are to be spidered
		//=======
		Vm.showWait(true);
		infB = new InfoBox("Status", MyLocale.getMsg(5502,"Fetching first page..."));
		infB.exec();
		//Get first page
		try{
			ln = p.getProperty("firstPage") + origin.getLatDeg(CWPoint.DD) + p.getProperty("firstPage2") +origin.getLonDeg(CWPoint.DD);
			if(doNotgetFound) ln = ln + "&f=1";
			pref.log("Getting first page: "+ln);
			start = fetch(ln);
			pref.log("Got first page");
		}catch(Exception ex){
			infB.close(0);
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5503,"Error fetching first list page."), MessageBox.OKB)).execute();
			pref.log("Error fetching first list page",ex);
			Vm.showWait(false);
			return;
		}
		dummy = "";
		//String lineBlck = "";
		int page_number = 4;		
		lineRex = new Regex(p.getProperty("lineRex")); //"<tr bgcolor=((?s).*?)</tr>"
		int found_on_page = 0;
		//Loop till maximum distance has been found or no more caches are in the list
		while(distance > 0){
			if (infB.isClosed) break;
			rex.search(start);
			viewstate = rex.stringMatched(1);
			//Vm.debug("In loop");
			Regex listBlockRex = new Regex(p.getProperty("listBlockRex")); // "<table id=\"dlResults\"((?s).*?)</table>"
			listBlockRex.search(start);
			dummy = listBlockRex.stringMatched(1);
			try{
				lineRex.search(dummy);
			}catch(NullPointerException nex){}
			while(lineRex.didMatch()){
				//Vm.debug(getDist(rexLine.stringMatched(1)) + " / " +getWP(rexLine.stringMatched(1)));
				found_on_page++;
				if(getDist(lineRex.stringMatched(1)) <= distance){
					if(indexDB.get((String)getWP(lineRex.stringMatched(1))) == null){
						cachesToLoad.add(getWP(lineRex.stringMatched(1)));
					} else pref.log(getWP(lineRex.stringMatched(1))+" already in DB");
				} else distance = 0;
				lineRex.searchFrom(dummy, lineRex.matchedTo());
			}
			infB.setInfo("Found " + cachesToLoad.size() + " caches");
			if(found_on_page < 20) distance = 0;
			postStr = p.getProperty("firstLine") + origin.getLatDeg(CWPoint.DD) + "&" + origin.getLonDeg(CWPoint.DD);
			if(doNotgetFound) postStr = postStr + p.getProperty("showOnlyFound");
			if(distance > 0){
				page_number++;
				if(page_number >= 15) page_number = 5;
				doc = URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false)
				//if(doNotgetFound) doc += "&f=1";
				    + "&" + URL.encodeURL("__EVENTTARGET",false) +"="+ URL.encodeURL("ResultsPager:_ctl"+page_number,false)
				    + "&" + URL.encodeURL("__EVENTARGUMENT",false) +"="+ URL.encodeURL("",false);
				try{
					start = "";
					pref.log("Fetching next list page:" + doc);
					start = fetch_post(postStr, doc, p.getProperty("nextListPage"));
				}catch(Exception ex){
					//Vm.debug("Couldn't get the next page");
					pref.log("Error getting next page");
				}finally{
				}
			}
			//Vm.debug("Distance is now: " + distance);
			found_on_page = 0;
		}
		
		pref.log("Found " + cachesToLoad.size() + " caches");
		if (!infB.isClosed) infB.setInfo("Found " + cachesToLoad.size() + " caches");
		//=======
		// Now ready to spider each cache in the list
		//=======
		for(int i = 0; i<cachesToLoad.size(); i++){
			if (infB.isClosed) break;
			
			wpt = (String)cachesToLoad.get(i);
			// Get only caches not already available in the DB
			if(searchWpt(wpt) == -1){
				infB.setInfo("Loading: " + wpt +"(" + (i+1) + " / " + cachesToLoad.size() + ")");
				chD = new CacheHolderDetail();
				chD.wayPoint=wpt;
				if (!getCacheByWaypointName(chD,false,getImages,doNotgetFound,true)) break;
				if (!chD.is_found || !doNotgetFound ) {
					chD.saveCacheDetails(profile.dataDir);
					cacheDB.add(new CacheHolder(chD)); // TODO Could copy into existing object
				}					
			}
		}
		infB.close(0);
		Vm.showWait(false);
		Global.getProfile().saveIndex(Global.getPref(),true);
	}

	/**
	 * Read a complete cachepage from geocaching.com including all logs. This is used both when
	 * updating already existing caches (via spiderSingle) and when spidering around a centre. It
	 * is also used when reading a GPX file and fetching the images.
	 * 
	 * This is the workhorse function of the spider.
	 * 
	 * @param CacheHolderDetail chD The element wayPoint must be set to the name of a waypoint
	 * @param boolean isUpdate True if an existing cache is being updated, false if it is a new cache
	 * @param boolean fetchImages True if the pictures are to be fetched
	 * @param boolean doNotGetFound True if the cache is not to be spidered if it has already been found
	 * @param boolean fetchAllLogs True if all logs are to be fetched (by adding option '&logs=y' to command line).
	 *     This is normally false when spidering from GPXImport as the logs are part of the GPX file, and true otherwise
	 * @return false if the infoBox was closed
	 */
	private boolean getCacheByWaypointName(CacheHolderDetail chD, boolean isUpdate, boolean fetchImages, boolean doNotGetFound, boolean fetchAllLogs) {
		String completeWebPage,origLongDesc;
		String doc = p.getProperty("getPageByName") + chD.wayPoint +(fetchAllLogs?p.getProperty("fetchAllLogs"):"");
		try{
			pref.log("Fetching: " + chD.wayPoint);
			completeWebPage = fetch(doc);
		}catch(Exception ex){
			pref.log("Could not fetch " + chD.wayPoint,ex);
			chD.is_incomplete = true;
			return !infB.isClosed;
		}
		// Only analyse the cache data and fetch pictures if user has not closed the progress window
		if (!infB.isClosed) { 
			try{
				chD.is_new = !isUpdate;
				chD.is_update = false;
				chD.is_HTML = true;
				chD.is_available = true;
				chD.is_archived = false;
				chD.is_incomplete = false;
				chD.CacheLogs.clear();
				chD.addiWpts.clear();
				chD.Images.clear();
				chD.ImagesText.clear();
				
				if(completeWebPage.indexOf(p.getProperty("cacheUnavailable")) >= 0) chD.is_available = false;
				if(completeWebPage.indexOf(p.getProperty("cacheArchived")) >= 0) chD.is_archived = true;
				//==========
				// General Cache Data
				//==========
				chD.LatLon = getLatLon(completeWebPage);
				chD.pos.set(chD.LatLon);
				if (pref.debug) pref.log("LatLon: " + chD.LatLon);
				
				pref.log("Trying description");
				origLongDesc = chD.LongDescription;
				chD.LongDescription = getLongDesc(completeWebPage);
				if(isUpdate && !chD.LongDescription.equals(origLongDesc)) chD.is_update = true;
				pref.log("Got description");
				
				pref.log("Getting cache name");
				chD.CacheName = SafeXML.cleanback(getName(completeWebPage));
				pref.log("Got cache name");
				if (pref.debug) pref.log("Name: " + chD.CacheName);

				pref.log("Trying owner");
				chD.CacheOwner = SafeXML.cleanback(getOwner(completeWebPage)).trim();
				if(chD.CacheOwner.equals(pref.myAlias) || (pref.myAlias2.length()>0 && chD.CacheOwner.equals(pref.myAlias2))) chD.is_owned = true;
				pref.log("Got owner");
				if (pref.debug) pref.log("Owner: " + chD.CacheOwner +"; is_owned = "+chD.is_owned+";  alias1,2 = ["+pref.myAlias+"|"+pref.myAlias2+"]");
				
				pref.log("Trying date hidden");
				chD.DateHidden = DateFormat.MDY2YMD(getDateHidden(completeWebPage));
				pref.log("Got date hidden");
				if (pref.debug) pref.log("Hidden: " + chD.DateHidden);
				
				pref.log("Trying hints");
				chD.Hints = getHints(completeWebPage);
				pref.log("Got hints");
				if (pref.debug) pref.log("Hints: " + chD.Hints);

				pref.log("Trying size");
				chD.CacheSize = getSize(completeWebPage);
				pref.log("Got size");
				if (pref.debug) pref.log("Size: " + chD.CacheSize);
				
				pref.log("Trying difficulty");
				chD.hard = getDiff(completeWebPage);
				pref.log("Got difficulty");
				if (pref.debug) pref.log("Hard: " + chD.hard);
				
				pref.log("Trying terrain");
				chD.terrain = getTerr(completeWebPage);
				pref.log("Got terrain");
				if (pref.debug) pref.log("Terr: " + chD.terrain);

				pref.log("Trying cache type");
				chD.type = getType(completeWebPage);
				pref.log("Got cache type");
				if (pref.debug) pref.log("Type: " + chD.type);
				
				//==========
				// Logs
				//==========
				pref.log("Trying logs");
				int logsz = chD.CacheLogs.size();
				chD.CacheLogs = getLogs(completeWebPage, chD);
				// Count the number of not-found logs
				int countNoFoundLogs = 0;
				String loganal = "";
				while(countNoFoundLogs < chD.CacheLogs.size() && countNoFoundLogs < 5){
					loganal = (String)chD.CacheLogs.get(countNoFoundLogs);
					if(loganal.indexOf("icon_sad")>0) {
						countNoFoundLogs++;
					}else break;
				}
				chD.noFindLogs = countNoFoundLogs;
				chD.is_log_update = false;
				if(isUpdate && chD.CacheLogs.size()>logsz) chD.is_log_update = true;
				pref.log("Found logs");
				// If the switch is set to not store found caches and we found the cache => return
				if (chD.is_found && doNotGetFound) return !infB.isClosed; 
				
				//==========
				// Bugs
				//==========
				// As there may be several bugs, we check whether the user has aborted
				if (!infB.isClosed) chD.Bugs = getBugs(completeWebPage);
				chD.has_bug = chD.Bugs.length()>0;
				
				//==========
				// Images
				//==========
				if(fetchImages){
					pref.log("Trying images");
					getImages(completeWebPage, chD);
					pref.log("Got images");
				}
				//==========
				// Addi waypoints
				//==========
				
				pref.log("Getting additional waypoints");
				getAddWaypoints(completeWebPage, chD.wayPoint, chD.is_found);
				pref.log("Got additional waypoints");


			}catch(Exception ex){
				pref.log("Error reading cache: "+chD.wayPoint);
				pref.log("Exception in getCacheByWaypointName: ",ex);
			}
			finally{}
		}
		boolean ret=!infB.isClosed; // If the infoBox was closed before getting here, we return false
		return ret;
	} // getCacheByWaypointName
	
	/**
	 * Check whether a waypoint is in the database
	 * @param wpt Name of waypoint to check
	 * @return index of waypoint in database, -1 if it does not exist
	 */
	private int searchWpt(String wpt){
		Integer INTR = (Integer)indexDB.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}
	
	/**
	 * Get the Distance to the centre
	 * @param doc A previously fetched cachepage
	 * @return Distance
	 */
	private double getDist(String doc){
		inRex = new Regex(p.getProperty("distRex"));
		inRex.search(doc);
		if(doc.indexOf("Here") > 0) return(0);
		if(pref.digSeparator.equals(",")) return Convert.toDouble(inRex.stringMatched(1).replace('.',','));
		return Convert.toDouble(inRex.stringMatched(1));
	}

	/**
	 * Get the waypoint name
	 * @param doc A previously fetched cachepage
	 * @return Name of waypoint to add to list 
	 */
	private String getWP(String doc){
		inRex = new Regex(p.getProperty("waypointRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the coordinates of the cache
	 * @param doc A previously fetched cachepage
	 * @return Cache coordinates
	 */
	private String getLatLon(String doc){
		inRex = new Regex(p.getProperty("latLonRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the long description
	 * @param doc A previously fetched cachepage
	 * @return the long description
	 */
	private String getLongDesc(String doc){
		String res = "";
		inRex = new Regex(p.getProperty("shortDescRex"));
		Regex rex2 = new Regex(p.getProperty("longDescRex"));
		inRex.search(doc);
		rex2.search(doc);
		res = inRex.stringMatched(1) + "<br>";
		res += rex2.stringMatched(1); 
		return SafeXML.cleanback(res);
	}
	
	/**
	 * Get the cache name
	 * @param doc A previously fetched cachepage
	 * @return the name of the cache
	 */
	private String getName(String doc){
		inRex = new Regex(p.getProperty("cacheNameRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the cache owner
	 * @param doc A previously fetched cachepage
	 * @return the cache owner
	 */
	private String getOwner(String doc){
		inRex = new Regex(p.getProperty("cacheOwnerRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the date when the cache was hidden
	 * @param doc A previously fetched cachepage
	 * @return Hidden date
	 */
	private String getDateHidden(String doc){
		inRex = new Regex(p.getProperty("dateHiddenRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the hints
	 * @param doc A previously fetched cachepage
	 * @return Cachehints
	 */
	private String getHints(String doc){
		inRex = new Regex(p.getProperty("hintsRex"));
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	/**
	 * Get the cache size
	 * @param doc A previously fetched cachepage
	 * @return Cache size
	 */
	private String getSize(String doc){
		inRex = new Regex(p.getProperty("sizeRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "None";
	}
	
	/**
	 * Get the Difficulty
	 * @param doc A previously fetched cachepage
	 * @return The cache difficulty 
	 */
	private String getDiff(String doc){
		inRex = new Regex(p.getProperty("difficultyRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}
	
	/**
	 * Get the terrain rating
	 * @param doc A previously fetched cachepage
	 * @return Terrain rating
	 */
	private String getTerr(String doc){
		inRex = new Regex(p.getProperty("terrainRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}

	/**
	 * Get the waypoint type
	 * @param doc A previously fetched cachepage
	 * @return the waypoint type (Tradi, Multi, etc.)
	 */
	private String getType(String doc){
		inRex = new Regex(p.getProperty("cacheTypeRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}

	/**
	 * Get the logs 
	 * @param doc A previously fetched cachepage
	 * @param chD Cache Details
	 * @return A HTML string containing the logs
	 */
	private Vector getLogs(String doc, CacheHolderDetail chD){
		String icon = "";
		String name = "";
		Vector reslts = new Vector();
		Regex blockRex = new Regex(p.getProperty("blockRex"));
		blockRex.search(doc);
		doc = blockRex.stringMatched(1);
		//Vm.debug("Log Block: " + doc);
		/*
		Vm.debug("Setting log regex");
		inRex = new Regex("<STRONG><IMG SRC='http://www.geocaching.com/images/icons/((?s).*?)'((?s).*?)&nbsp;((?s).*?)<A NAME=\"((?s).*?)'text-decoration: underline;'>((?s).*?)<A HREF=\"((?s).*?)'text-decoration: underline;'>((?s).*?)</A></strong>((?s).*?)\\[<A href=");
		inRex.optimize();
		inRex.search(doc);
		Vm.debug("Log regex run...");
		while(inRex.didMatch()){
			Vm.debug("Logs:" + inRex.stringMatched(1) + " / " + inRex.stringMatched(3)+ " / " + inRex.stringMatched(7)+ " / " + inRex.stringMatched(8));
			//<img src='icon_smile.gif'>&nbsp;
			reslts.add("<img src='"+ inRex.stringMatched(1) +"'>&nbsp;" + inRex.stringMatched(3)+ inRex.stringMatched(7)+ inRex.stringMatched(8));
			inRex.searchFrom(doc, inRex.matchedTo());
		}
		*/
		String singleLog = "";
		Extractor exSingleLog = new Extractor(doc,p.getProperty("singleLogExStart"), p.getProperty("singleLogExEnd"), 0, false); // maybe here is some change neccessary because findnext now gives the whole endstring back??? 
		singleLog = exSingleLog.findNext();
		Extractor exIcon = new Extractor(singleLog,p.getProperty("iconExStart"), p.getProperty("iconExEnd"), 0, true);
		Extractor exNameTemp = new Extractor(singleLog,p.getProperty("nameTempExStart"), p.getProperty("nameTempExEnd"), 0 , true);
		String nameTemp = "";
		nameTemp = exNameTemp.findNext();
		Extractor exName = new Extractor(nameTemp, p.getProperty("nameExStart"), p.getProperty("nameExEnd"), 0 , true);
		Extractor exDate = new Extractor(singleLog,p.getProperty("dateExStart"), p.getProperty("dateExEnd"), 0 , true);
		Extractor exLog = new Extractor(singleLog, p.getProperty("logExStart"), p.getProperty("logExEnd"), 0, true);
		//Vm.debug("Log Block: " + singleLog);
		int nLogs=0;
		while(exSingleLog.endOfSearch() == false){
			nLogs++;
			if (nLogs>MAXLOGS) {
				reslts.add("<br\\>More than "+MAXLOGS+" logs.<br\\>");
				break;
			}
			//Vm.debug("--------------------------------------------");
			//Vm.debug("Log Block: " + singleLog);
			//Vm.debug("Icon: "+exIcon.findNext());
			//Vm.debug(exName.findNext());
			//Vm.debug(exDate.findNext());
			//Vm.debug(exLog.findNext());
			//Vm.debug("--------------------------------------------");
			icon = exIcon.findNext();
			name = exName.findNext();
			String d=DateFormat.logdate2YMD(exDate.findNext());
			if((icon.equals(p.getProperty("icon_smile")) || icon.equals(p.getProperty("icon_camera"))) && 
				(name.equals(pref.myAlias) || (pref.myAlias2.length()>0 && name.equals(pref.myAlias2))) )  {
				chD.is_found = true;
				chD.CacheStatus = d;
			}
			reslts.add("<img src='"+ icon +"'>&nbsp;" + d + " " + name + exLog.findNext());
			
			singleLog = exSingleLog.findNext();
			exIcon.setSource(singleLog);
			exNameTemp.setSource(singleLog);
			nameTemp = exNameTemp.findNext();
			exName.setSource(nameTemp);
			exDate.setSource(singleLog);
			exLog.setSource(singleLog);
		}
		return reslts;
	}
	
	/**
	 * Read the travelbug names from a previously fetched Cache page and then
	 * read the travelbug purpose for each travelbug
	 * @param doc The previously fetched cachepage
	 * @return A HTML formatted string with bug names and there purpose
	 */
	public String getBugs(String doc){	
		Extractor exBlock = new Extractor(doc,p.getProperty("blockExStart"),p.getProperty("blockExEnd") ,0,Extractor.EXCLUDESTARTEND);
		String bugBlock = exBlock.findNext();
		//Vm.debug("Bugblock: "+bugBlock);
		Extractor exBug = new Extractor(bugBlock,p.getProperty("bugExStart"),p.getProperty("bugExEnd"),0,Extractor.EXCLUDESTARTEND);
		String link,bug,linkPlusBug,bugDetails;
		String result = "";
		String oldInfoBox=infB.getInfo();
		while(exBug.endOfSearch() == false){
			if (infB.isClosed) break; // Allow user to cancel by closing progress form
			linkPlusBug= exBug.findNext();
			int idx=linkPlusBug.indexOf("'>");
			if (idx<0) break; // No link/bug pair found
			link=linkPlusBug.substring(0,idx);
			bug=linkPlusBug.substring(idx+2);
			if(bug.length()>0) { // Found a bug, get its details
				result = result + "<b>Name:</b> "+ bug + "<br>";
				try{
					infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
					pref.log("Fetching bug details: "+bug);
					bugDetails = fetch(link);
				}catch(Exception ex){
					pref.log("Could not fetch bug details");
					bugDetails="";
				}
				Extractor exDetails = new Extractor(bugDetails,p.getProperty("bugDetailsStart"),p.getProperty("bugDetailsEnd"),0,Extractor.EXCLUDESTARTEND);
				result+=exDetails.findNext()+"<hr>";
			}
			//Vm.debug("B: " + bug);
			//Vm.debug("End? " + exBug.endOfSearch());
		}
		infB.setInfo(oldInfoBox);
		return result;
	}
	
	/**
	 * Get the images for a previously fetched cache page. Images are extracted 
	 * from two areas: The long description and the pictures section (including
	 * the spoiler)
	 * @param doc The previously fetched cachepage
	 * @param chD The Cachedetails
	 */
	public void getImages(String doc, CacheHolderDetail chD){
		int imgCounter = 0;
		String imgName, oldImgName, imgType, imgUrl;
		Vector spideredUrls=new Vector(15);
		int idxUrl; // Index of already spidered Url in list of spideredUrls
		//========
		//In the long description
		//========
		String longDesc = "";
		longDesc = getLongDesc(doc);
		longDesc = STRreplace.replace(longDesc, "img", "IMG");
		longDesc = STRreplace.replace(longDesc, "src", "SRC");
		longDesc = STRreplace.replace(longDesc, "'", "\"");
		Extractor exImgBlock = new Extractor(longDesc,p.getProperty("imgBlockExStart"),p.getProperty("imgBlockExEnd"), 0, false);
		//Vm.debug("In getImages: Have longDesc" + longDesc);
		String tst;
		tst = exImgBlock.findNext();
		//Vm.debug("Test: \n" + tst);
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while(exImgBlock.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				try{
					imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
					// imgType is now max 4 chars, starting with .
					if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe")){
						// Check whether image was already spidered for this cache
						idxUrl=spideredUrls.find(imgUrl);
						imgName = chD.wayPoint + "_" + Convert.toString(imgCounter);
						if (idxUrl<0) { // New image
							pref.log("Loading image: " + imgUrl);
							spiderImage(imgUrl, imgName+imgType);
							chD.Images.add(imgName+imgType);
							spideredUrls.add(imgUrl);
						} else { // Image already spidered as wayPoint_'idxUrl'
							pref.log("Already loaded image: " + imgUrl);
							oldImgName = chD.wayPoint + "_" + Convert.toString(idxUrl);
							chD.Images.add(oldImgName+imgType); // Store name of old image as image to load
						}
						chD.ImagesText.add(imgName); // Keep the image name
						imgCounter++;
					}
				} catch (IndexOutOfBoundsException e) { 
					//Vm.debug("IndexOutOfBoundsException not in image span"+e.toString()+"imgURL:"+imgUrl);
					pref.log("Problem loading image. imgURL:"+imgUrl);
				}
				}
			exImgSrc.setSource(exImgBlock.findNext());
		}
		//========
		//In the image span
		//========
		Extractor spanBlock = new Extractor(doc,p.getProperty("imgSpanExStart"),p.getProperty("imgSpanExEnd"), 0 , true);
		tst = spanBlock.findNext();
		Extractor exImgName = new Extractor(tst,p.getProperty("imgNameExStart"),p.getProperty("imgNameExEnd"), 0 , true);
		exImgSrc = new Extractor(tst,p.getProperty("imgSrcExStart"),p.getProperty("imgSrcExEnd"), 0, true);
		while(exImgSrc.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				try{
					imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
					// imgType is now max 4 chars, starting with .
					if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe")){
						// Check whether image was already spidered for this cache
						idxUrl=spideredUrls.find(imgUrl);
						imgName = chD.wayPoint + "_" + Convert.toString(imgCounter);
						if (idxUrl<0) { // New image
							pref.log("Loading image: " + imgUrl);
							spiderImage(imgUrl, imgName+imgType);
							chD.Images.add(imgName+imgType);
						} else { // Image already spidered as wayPoint_ 'idxUrl'
							pref.log("Already loaded image: " + imgUrl);
							oldImgName = chD.wayPoint + "_" + Convert.toString(idxUrl);
							chD.Images.add(oldImgName+imgType); // Store name of old image as image to load
						}
						chD.ImagesText.add(exImgName.findNext()); // Keep the image description
						chD.ImagesText.add(imgName); 
						imgCounter++;
					}
				} catch (IndexOutOfBoundsException e) { 
					pref.log("IndexOutOfBoundsException in image span. imgURL:"+imgUrl,e); 
				}
			}
		}
	}
	
	/**
	 * Read an image from the server
	 * @param imgUrl The Url of the image
	 * @param target The bytes of the image
	 */
	private void spiderImage(String imgUrl, String target){
		HttpConnection connImg;
		Socket sockImg;
		//InputStream is;
		FileOutputStream fos;
		//int bytes_read;
		//byte[] buffer = new byte[9000];
		ByteArray daten;
		String datei = "";
		datei = profile.dataDir + target;
		if(pref.myproxy.length()>0){
			connImg = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), imgUrl);
		}else{
			connImg = new HttpConnection(imgUrl);
		}
		connImg.setRequestorProperty("Connection", "close");
		try{
			pref.log("Trying to fetch image from: " + imgUrl);
			sockImg = connImg.connect();
			daten = connImg.readData(connImg.connect());
			fos = new FileOutputStream(new File(datei));
			fos.write(daten.toBytes());
			fos.close();
			sockImg.close();
		} catch (UnknownHostException e) {
			pref.log("Host not there...");
		}catch(IOException ioex){
			pref.log("File not found!");
		} catch (Exception ex){
			pref.log("Some other problem while fetching image",ex);
		} finally {
			//Continue with the spider
		}
	}		

	/**
	 * Read all additional waypoints from a previously fetched cachepage.
	 * @param doc The previously fetched cachepage
	 * @param wayPoint The name of the cache
	 * @param is_found Found status of the cached (is inherited by the additional waypoints)
	 */
	public void getAddWaypoints(String doc, String wayPoint, boolean is_found){
		Extractor exWayBlock = new Extractor(doc,p.getProperty("wayBlockExStart"),p.getProperty("wayBlockExEnd"), 0, false);
		String wayBlock = "";
		String rowBlock = "";
		wayBlock = exWayBlock.findNext();
		Regex nameRex = new Regex(p.getProperty("nameRex"));
		Regex koordRex = new Regex(p.getProperty("koordRex"));
		Regex descRex = new Regex(p.getProperty("descRex"));
		Regex typeRex = new Regex(p.getProperty("typeRex"));
		int counter = 0;
		if(exWayBlock.endOfSearch() == false && wayBlock.indexOf("No additional waypoints to display.")<0){
			Extractor exRowBlock = new Extractor(wayBlock,p.getProperty("rowBlockExStart"),p.getProperty("rowBlockExEnd"), 0, false);
			rowBlock = exRowBlock.findNext();
			rowBlock = exRowBlock.findNext();
			while(exRowBlock.endOfSearch()==false){
				CacheHolderDetail cx = new CacheHolderDetail();
				
				nameRex.search(rowBlock);
				koordRex.search(rowBlock);
				typeRex.search(rowBlock);
				cx.CacheName = nameRex.stringMatched(1);
				//Vm.debug("Addi: " + cx.CacheName);
				if(koordRex.didMatch()) cx.pos.set(koordRex.stringMatched(1)); 
				cx.LatLon = cx.pos.toString(); 
				//cx.pos.set(cx.LatLon);
				if(typeRex.didMatch()) cx.type = CacheType.typeText2Number("Waypoint|"+typeRex.stringMatched(1));
				rowBlock = exRowBlock.findNext();
				descRex.search(rowBlock);
				cx.wayPoint = MyLocale.formatLong(counter, "00") + wayPoint.substring(2);
				counter++;
				cx.LongDescription = descRex.stringMatched(1); 
				//Vm.debug(descRex.stringMatched(1));
				int idx=profile.getCacheIndex(cx.wayPoint);
				cx.is_found = is_found;
				//Vm.debug("IDX: " + idx);
				if (idx<0){
					cacheDB.add(new CacheHolder(cx));
				}else if (((CacheHolder) cacheDB.get(idx)).is_Checked && // Only re-spider existing addi waypoints that are ticked
						!((CacheHolder) cacheDB.get(idx)).is_filtered) // and are visible (i.e.  not filtered)
					cacheDB.set(idx,new CacheHolder(
					    new CacheHolderDetail(((CacheHolder) cacheDB.get(idx))).update(cx)));
				cx.saveCacheDetails(profile.dataDir);
				rowBlock = exRowBlock.findNext();
			}
		}
	}
	

	/**
	*	Performs an initial fetch to a given address. In this case
	*	it will be a gc.com address. This method is used to obtain
	*	the result of a search for caches screen.
	*/
	public static String fetch(String address)
	   	{	
			CharArray c_data;
			String data = new String();
			try{
				//Vm.debug(address);
				HttpConnection conn;
				if(pref.myproxy.length() > 0){
					pref.log("Using proxy: " + pref.myproxy + " / " +pref.myproxyport);
					conn = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), address);
					//Vm.debug(address);
				} else {
					conn = new HttpConnection(address);
				}
				conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
				if(cookieSession.length()>0){
					conn.setRequestorProperty("Cookie: ", "ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
					pref.log("Cookie Zeug: " + "Cookie: ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
				}
				conn.setRequestorProperty("Connection", "close");
				conn.documentIsEncoded = true;
				pref.log("Connecting");
				Socket sock = conn.connect();
				pref.log("Connect ok!");
				ByteArray daten = conn.readData(sock);
				pref.log("Read socket ok");
				JavaUtf8Codec codec = new JavaUtf8Codec();
				c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
				data = c_data.toString();
				////Vm.debug(c_data.toString());
				sock.close();
			}catch(IOException ioex){
				pref.log("IOException in fetch", ioex);
			}finally{
				//continue
			}
			return data;
		}
	
	/**
	*	After a fetch to gc.com the next fetches have to use the post method.
	*	This method does exactly that. Actually this method is generic in the sense
	*	that it can be used to post to a URL using http post.
	*/
	private static String fetch_post(String address, String document, String path) throws IOException 
	   	{
			
			//String line = "";
			String totline = "";
			if(pref.myproxy.length()==0){
				try {
					/*
					// Create a socket to the host
					String hostname = "www.geocaching.com";
					int port = 80;
					InetAddress addr = InetAddress.getByName(hostname);
					Socket socket = new Socket(hostname, port);
					// Send header
					//String path = "/seek/nearest.aspx";
					BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
					BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					wr.write("POST "+path+" HTTP/1.1\r\n");
					wr.write("Host: www.geocaching.com\r\n");
					if(cookieSession.length()>0){
						wr.write("Cookie: ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
					}
					Vm.debug("Doc length: " + document.length());
					wr.write("Content-Length: "+document.length()+"\r\n");
					wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
					wr.write("Connection: close\r\n");
					wr.write("\r\n");
					// Send data
					wr.write(document);
					wr.write("\r\n");
					wr.flush();
					//Vm.debug("Sent the data!");
					// Get response
					while ((line = rd.readLine()) != null) {
						totline += line + "\n";
					}
					wr.close();
					rd.close();
					*/
					HttpConnection conn;
					conn = new HttpConnection(address);
					JavaUtf8Codec codec = new JavaUtf8Codec();
					conn.documentIsEncoded = true;
					//Vm.debug(address + " / " + document);
					//document = document + "\r\n";
					//conn.setPostData(document.toCharArray());
					conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
					conn.setPostData(codec.encodeText(document.toCharArray(),0,document.length(),true,null));
					conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
					if(cookieSession.length()>0){
						conn.setRequestorProperty("Cookie: ", "ASP.NET_SessionId="+cookieSession+"; userid="+cookieID);
					}
					conn.setRequestorProperty("Connection", "close");
					Socket sock = conn.connect();
					
					//Vm.debug("getting stuff!");
					ByteArray daten = conn.readData(sock);
					//Vm.debug("coming back!");
					CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
					sock.close();
					//Vm.debug(c_data.toString());
					totline =  c_data.toString();
				} catch (Exception e) {
				}
			} else {
				HttpConnection conn;
				conn = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), address);
				JavaUtf8Codec codec = new JavaUtf8Codec();
				conn.documentIsEncoded = true;
				//Vm.debug(address + " / " + document);
				//document = document + "\r\n";
				//conn.setPostData(document.toCharArray());
				conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
				conn.setPostData(codec.encodeText(document.toCharArray(),0,document.length(),true,null));
				conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
				if(cookieSession.length()>0){
					conn.setRequestorProperty("Cookie: ", "ASP.NET_SessionId="+cookieSession+"; userid="+cookieID);
				}
				conn.setRequestorProperty("Connection", "close");
				Socket sock = conn.connect();
				
				//Vm.debug("getting stuff!");
				ByteArray daten = conn.readData(sock);
				//Vm.debug("coming back!");
				CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
				sock.close();
				//Vm.debug(c_data.toString());
				totline =  c_data.toString();
			}
			return totline;
		}
}