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

package CacheWolf.imp;
import ewe.net.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.util.*;
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
import CacheWolf.navi.Metrics;
import CacheWolf.navi.TransformCoordinates;

import com.stevesoft.ewe_pat.*;
import ewe.ui.*;
import ewe.data.Property;
import ewe.data.PropertyList;

/**
*	Class to spider caches from gc.com
*/
public class SpiderGC{

	/**
	 * The maximum number of logs that will be stored
	 */
	public static String passwort = ""; // Can be pre-set from preferences
	public static boolean loggedIn = false;

	// Return values for spider action
	/** Ignoring a premium member cache when spidering from a non premium account */
	public static int SPIDER_IGNORE_PREMIUM = -2;
	/** Canceling spider process */
	public static int SPIDER_CANCEL = -1;
	/** Error occured while spidering */
	public static int SPIDER_ERROR = 0;
	/** Cache was spidered without problems */
	public static int SPIDER_OK = 1;

	private static int ERR_LOGIN = -10;
	private static Preferences pref;
	private Profile profile;
	private static String viewstate = "";
	private static String viewstate1 = "";
	//FIXME Field is never read. Needed?
	//private static String eventvalidation = "";
	private static String cookieID = "";
	private static String cookieSession = "";
	private static double distance = 0;
	private Regex inRex = new Regex();
	private CacheDB cacheDB;
	private Vector cachesToLoad = new Vector();
	private InfoBox infB;
	private static SpiderProperties p=null;

	public SpiderGC(Preferences prf, Profile profile, boolean bypass){
		this.profile=profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		if (p==null) {
			pref.logInit();
			p=new SpiderProperties();
		}
	}

	/**
	 * Method to login the user to gc.com
	 * It will request a password and use the alias defined in preferences
	 * If the login page cannot be fetched, the password is cleared.
	 * If the login fails, an appropriate message is displayed.
	 */
	public int login(){
		loggedIn = false;
		String start,loginPage,loginSuccess,nextPage;
		try {
			loginPage=p.getProp("loginPage");
			loginSuccess=p.getProp("loginSuccess");
			nextPage=p.getProp("nextPage");
		} catch (Exception ex) { // Tag not found in spider.def
			return ERR_LOGIN;
		}
		//Get password
		InfoBox localInfB = new InfoBox(MyLocale.getMsg(5506,"Password"), MyLocale.getMsg(5505,"Enter Password"), InfoBox.INPUT);
		localInfB.feedback.setText(passwort); // Remember the PWD for next time
		localInfB.feedback.isPassword=true;
		int code=FormBase.IDOK;
		if (passwort.equals("")) {
			code = localInfB.execute();
			passwort = localInfB.getInput();
		}
		localInfB.close(0);
		if(code != FormBase.IDOK) return code;
		// Now start the login proper
		localInfB = new InfoBox(MyLocale.getMsg(5507,"Status"), MyLocale.getMsg(5508,"Logging in..."));
		localInfB.exec();
		try{
			pref.log("[login]:Fetching login page");
			//Access the page once to get a viewstate
			start = fetch(loginPage);   //http://www.geocaching.com/login/Default.aspx
			if (start.equals("")) {
				localInfB.close(0);
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5499,"Error loading login page.%0aPlease check your internet connection."), FormBase.OKB)).execute();
				pref.log("[login]:Could not fetch: gc.com login page");
				return ERR_LOGIN;
			}
		} catch(Exception ex){
			localInfB.close(0);
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5499,"Error loading login page.%0aPlease check your internet connection."), FormBase.OKB)).execute();
			pref.log("[login]:Could not fetch: gc.com login page",ex);
			return ERR_LOGIN;
		}
		if (!localInfB.isClosed) { // If user has not aborted, we continue
			Regex rexCookieID = new Regex("(?i)Set-Cookie: userid=(.*?);.*");
			Regex rexViewstate = new Regex("id=\"__VIEWSTATE\" value=\"(.*?)\" />");
			Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
			Regex rexEventvalidation = new Regex("id=\"__EVENTVALIDATION\" value=\"(.*?)\" />");
			Regex rexCookieSession = new Regex("(?i)Set-Cookie: ASP.NET_SessionId=(.*?);.*");
			rexViewstate.search(start);
			if(rexViewstate.didMatch()){
				viewstate = rexViewstate.stringMatched(1);
				//Vm.debug("ViewState: " + viewstate);
			} else
				pref.log("[login]:Viewstate not found before login");

			if(start.indexOf(loginSuccess) > 0)
				pref.log("[login]:Already logged in");
			else {
				rexEventvalidation.search(start);
				if(rexEventvalidation.didMatch()){
					// eventvalidation = rexEventvalidation.stringMatched(1);
					//Vm.debug("EVENTVALIDATION: " + eventvalidation);
				} else
					pref.log("[login]:Eventvalidation not found before login");
				//Ok now login!
				try{
					pref.log("[login]:Logging in as "+pref.myAlias);
					StringBuffer sb=new StringBuffer(1000);
					sb.append(URL.encodeURL("__VIEWSTATE",false));	sb.append("="); sb.append(URL.encodeURL(viewstate,false));
					sb.append("&ctl00%24ContentBody%24"); sb.append(URL.encodeURL("myUsername",false));
					sb.append("="); sb.append(encodeUTF8URL(Utils.encodeJavaUtf8String(pref.myAlias)));
					sb.append("&ctl00%24ContentBody%24"); sb.append(URL.encodeURL("myPassword",false));
					sb.append("="); sb.append(encodeUTF8URL(Utils.encodeJavaUtf8String(passwort)));
					sb.append("&ctl00%24ContentBody%24"); sb.append(URL.encodeURL("cookie",false));
					sb.append("="); sb.append(URL.encodeURL("on",false));
					sb.append("&ctl00%24ContentBody%24"); sb.append(URL.encodeURL("Button1",false));
					sb.append("="); sb.append(URL.encodeURL("Login",false));
//					sb.append("&"); sb.append(URL.encodeURL("__EVENTVALIDATION",false));
//					sb.append("="); sb.append(URL.encodeURL(eventvalidation,false));
					start = fetch_post(loginPage, sb.toString(), nextPage);  // /login/default.aspx
					if(start.indexOf(loginSuccess) > 0)
						pref.log("[login]:Login successful");
					else {
						pref.log("[login]:Login failed. Wrong Account or Password?");
						if (pref.debug) {
							pref.log("[login.LoginUrl]:"+sb.toString());
							pref.log("[login.Answer]:"+start);
						}
						localInfB.close(0);
						(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5501,"Login failed! Wrong account or password?"), FormBase.OKB)).execute();
						return ERR_LOGIN;
					}
				}catch(Exception ex){
					pref.log("[login]:Login failed with exception.", ex);
					localInfB.close(0);
					(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5501,"Login failed. Error loading page after login."), FormBase.OKB)).execute();
					return ERR_LOGIN;
				}
			}

			rexViewstate.search(start);
			if (!rexViewstate.didMatch()) {
				pref.log("[login]:Viewstate not found");
			}
			viewstate = rexViewstate.stringMatched(1);

			rexViewstate1.search(start);
			if (!rexViewstate1.didMatch()) {
				pref.log("[login]:Viewstate1 not found");
			}
			viewstate1 = rexViewstate1.stringMatched(1);

			rexCookieID.search(start);
			if (!rexCookieID.didMatch()) {
				pref.log("[login]:CookieID not found. Using old one.");
			} else
				cookieID = rexCookieID.stringMatched(1);
			//Vm.debug(cookieID);
			rexCookieSession.search(start);
			if (!rexCookieSession.didMatch()) {
				pref.log("[login]:CookieSession not found. Using old one.");
				//cookieSession="";
			} else
				cookieSession = rexCookieSession.stringMatched(1);
			//Vm.debug("cookieSession = " + cookieSession);
		}
		boolean loginAborted=localInfB.isClosed;
		localInfB.close(0);
		if (loginAborted)
			return FormBase.IDCANCEL;
		else {
			loggedIn = true;
			return FormBase.IDOK;
		}
	}

	/**
	 * Method to spider a single cache.
	 * It assumes a login has already been performed!
	 * @return 1 if spider was successful, -1 if spider was cancelled by closing the infobox, 0 error, but continue with next cache
	 */
	public int spiderSingle(int number, InfoBox pInfB, boolean forceLogin, boolean loadAllLogs){
		int ret=-1;
		this.infB = pInfB;
		CacheHolder ch = new CacheHolder(); // cacheDB.get(number);
		ch.setWayPoint(cacheDB.get(number).getWayPoint());
		if (ch.isAddiWpt()) return -1;  // No point re-spidering an addi waypoint, comes with parent

		// check if we need to login
		if (!loggedIn || forceLogin){
			if (this.login()!=FormBase.IDOK) return -1;
			// loggedIn is already set by this.login()
		}
		try{
			// Read the cache data from GC.COM and compare to old data
			ret=getCacheByWaypointName(ch,true,pref.downloadPics,pref.downloadTBs,false,loadAllLogs);
			// Save the spidered data
			if (ret == SPIDER_OK) {
				CacheHolder cacheInDB = cacheDB.get(number);
				cacheInDB.initStates(false);
				if (cacheInDB.is_found() && !ch.is_found() && ! loadAllLogs) {
					// If the number of logs to spider is 5 or less, then the "not found" information
					// of the spidered cache is not credible. In this case it should not overwrite
					// the "found" state of an existing cache.
					ch.setFound(true);
				}
				// preserve rating information
				ch.setNumRecommended(cacheInDB.getNumRecommended());
				if (pref.downloadPics) {
					// delete obsolete images when we have current set
					CacheImages.cleanupOldImages(cacheInDB.getExistingDetails().images, ch.getFreshDetails().images);
				} else {
					// preserve images if not downloaded
					ch.getFreshDetails().images = cacheInDB.getExistingDetails().images;
				}
				cacheInDB.update(ch);
				cacheInDB.save();
			}
		}catch(Exception ex){
			pref.log("Error spidering " + ch.getWayPoint() + " in spiderSingle");
		}
		return ret;
	} // spiderSingle

	/**
	 * Fetch the coordinates of a waypoint from GC
	 * @param wayPoint the name of the waypoint
	 * @return the cache coordinates
	 */
	public String getCacheCoordinates(String wayPoint) {
		String completeWebPage;
		// Check whether spider definitions could be loaded, if not issue appropriate message and terminate
		// Try to login. If login fails, issue appropriate message and terminate
		if (!loggedIn || Global.getPref().forceLogin) {
			if (login()!=FormBase.IDOK) {
				return "";
			}
		}
		InfoBox localInfB = new InfoBox("Info", "Loading", InfoBox.PROGRESS_WITH_WARNINGS);
		localInfB.exec();
		try{
			String doc = p.getProp("waypoint") + wayPoint;
			pref.log("Fetching: " + wayPoint);
			completeWebPage = fetch(doc);
		}catch(Exception ex){
			localInfB.close(0);
			pref.log("Could not fetch " + wayPoint,ex);
			return "";
		}
		localInfB.close(0);
		try {
			return getLatLon(completeWebPage);
		} catch (Exception ex) {
			return "????";
		}
	}

	/**
	*	Method to start the spider for a search around the centre coordinates
	*/
	public void doIt(){
		doIt(false);
	}
	public void doIt(boolean spiderAllFinds){
		String postStr, dummy, ln, wpt;
		Regex lineRex;
		CacheHolder holder;
		CWPoint origin = pref.getCurCentrePt(); // No need to copy curCentrePt as it is only read and not written
		if ( !spiderAllFinds && !origin.isValid()) {
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5509,"Coordinates for centre must be set"), FormBase.OKB)).execute();
			return;
		}
		if (System.getProperty("os.name")!=null)pref.log("Operating system: "+System.getProperty("os.name")+"/"+System.getProperty("os.arch"));
		if (System.getProperty("java.vendor")!=null)pref.log("Java: "+System.getProperty("java.vendor")+"/"+System.getProperty("java.version"));
		CacheHolder ch;
		// Reset states for all caches when spidering (http://tinyurl.com/dzjh7p)
		for(int i = 0; i<cacheDB.size();i++){
			ch = cacheDB.get(i);
			if (ch.mainCache==null) ch.initStates(false);
		}
		String start = "";
		Regex rexViewstate = new Regex("id=\"__VIEWSTATE\" value=\"(.*)\" />");
		Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*)\" />");
		Regex rexEventvalidation = new Regex("id=\"__EVENTVALIDATION\" value=\"(.*)\" />");
		String doc = "";

		if (!loggedIn || Global.getPref().forceLogin) {
			if(login() != FormBase.IDOK) return;
		}

		boolean doNotgetFound = false;

		OCXMLImporterScreen options;
		if (spiderAllFinds) {
			options = new OCXMLImporterScreen(MyLocale.getMsg(5510,"Spider Options"), OCXMLImporterScreen.MAXNUMBER|OCXMLImporterScreen.IMAGES| OCXMLImporterScreen.ISGC| OCXMLImporterScreen.TRAVELBUGS| OCXMLImporterScreen.MAXLOGS| OCXMLImporterScreen.TYPE);
			if (options.execute() == FormBase.IDCANCEL) {return; }

			distance = 1;
		} else {
			options = new OCXMLImporterScreen(MyLocale.getMsg(5510,"Spider Options"),	OCXMLImporterScreen.MAXNUMBER|OCXMLImporterScreen.INCLUDEFOUND | OCXMLImporterScreen.DIST| OCXMLImporterScreen.IMAGES| OCXMLImporterScreen.ISGC| OCXMLImporterScreen.TRAVELBUGS| OCXMLImporterScreen.MAXLOGS| OCXMLImporterScreen.TYPE);
			if (options.execute() == FormBase.IDCANCEL) {return; }
			String dist = options.distanceInput.getText();
			if (dist.length()== 0) return;
			distance = Common.parseDouble(dist);

			//save last radius to profile
			Double distDouble = new Double();
			distDouble.value = distance;
			dist = distDouble.toString(0, 1, 0).replace(',', '.');
			profile.setDistGC(dist);

			doNotgetFound = options.foundCheckBox.getState();
		}

		int maxNumber = -1;
		String maxNumberString = options.maxNumberInput.getText();
		if (maxNumberString.length()!= 0) {
			maxNumber = Common.parseInt(maxNumberString);
		}
		if (maxNumber != pref.maxSpiderNumber) {
			pref.maxSpiderNumber = maxNumber;
			pref.savePreferences();
		}
		if (maxNumber == 0) return;
		boolean maxNumberAbort = false;

		boolean getImages = options.imagesCheckBox.getState();
		boolean getTBs = options.travelbugsCheckBox.getState();

		String cacheTypeRestriction = options.getCacheTypeRestriction(p);

		options.close(0);

		//max distance in miles for URL, so we can get more than 80km
		double saveDistanceInMiles = distance;
		if ( Global.getPref().metricSystem != Metrics.IMPERIAL ) {
			saveDistanceInMiles = Metrics.convertUnit(distance, Metrics.KILOMETER, Metrics.MILES);
		}
		// add a mile to be save from different distance calculations in CW and at GC
		saveDistanceInMiles = java.lang.Math.ceil(saveDistanceInMiles) + 1;

		Hashtable cachesToUpdate = new Hashtable(cacheDB.size());

		if (pref.spiderUpdates != Preferences.NO) {
			double distanceInKm = distance;
			if ( Global.getPref().metricSystem == Metrics.IMPERIAL ) {
				distanceInKm = Metrics.convertUnit(distance, Metrics.MILES, Metrics.KILOMETER);
			}
			byte restrictedCacheType = options.getRestrictedCacheType(p);
			for(int i = 0; i<cacheDB.size();i++){
				ch = cacheDB.get(i);
				if (spiderAllFinds) {
					if ( (ch.getWayPoint().substring(0,2).equalsIgnoreCase("GC"))
					     && ( (restrictedCacheType == CacheType.CW_TYPE_ERROR) || (ch.getType() == restrictedCacheType) )
					     && !ch.is_black() ) {
						cachesToUpdate.put(ch.getWayPoint(), ch);
					}
				} else {
					if ( (!ch.is_archived())
						 && (ch.kilom <= distanceInKm)
						 && !(doNotgetFound && (ch.is_found() || ch.is_owned()))
						 && (ch.getWayPoint().substring(0,2).equalsIgnoreCase("GC"))
						 && ( (restrictedCacheType == CacheType.CW_TYPE_ERROR) || (ch.getType() == restrictedCacheType) )
						 && !ch.is_black() ) {
						cachesToUpdate.put(ch.getWayPoint(), ch);
					}
				}
			}
		}

		//=======
		// Prepare list of all caches that are to be spidered
		//=======
		Vm.showWait(true);
		infB = new InfoBox("Status", MyLocale.getMsg(5502,"Fetching first page..."));
		infB.exec();
		//Get first page
		try{
			if (spiderAllFinds) {
				ln = p.getProp("firstPageFinds") + encodeUTF8URL(Utils.encodeJavaUtf8String(pref.myAlias));
			} else {
				ln = p.getProp("firstPage") + origin.getLatDeg(TransformCoordinates.DD) + p.getProp("firstPage2") + origin.getLonDeg(TransformCoordinates.DD)
			                              + p.getProp("maxDistance") + Integer.toString( (int)saveDistanceInMiles );
				if(doNotgetFound) ln = ln + p.getProp("showOnlyFound");
			}
			ln = ln + cacheTypeRestriction;
			pref.log("Getting first page: "+ln);
			start = fetch(ln);
			pref.log("Got first page");
		}catch(Exception ex){
			pref.log("Error fetching first list page",ex,true);
			Vm.showWait(false);
			infB.close(0);
			(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5503,"Error fetching first list page."), FormBase.OKB)).execute();
			return;
		}
		dummy = "";
		//String lineBlck = "";
		int page_number = 1;
		try  {
			lineRex = new Regex(p.getProp("lineRex")); //"<tr bgcolor=((?s).*?)</tr>"
		} catch (Exception ex) {
			infB.close(0);
			Vm.showWait(false);
			return;
		}
		int page = 0;
		int found_on_page = 0;
		try {
			//Loop till maximum distance has been found or no more caches are in the list
			while(distance > 0){
				if (infB.isClosed){
					//don't update existing caches, because list is not correct when aborting
					cachesToUpdate.clear();

					break;
				}

				rexViewstate.search(start);
				if(rexViewstate.didMatch()){
					viewstate = rexViewstate.stringMatched(1);
					//Vm.debug("ViewState: " + viewstate);
				} else {
					viewstate = "";
					pref.log("Viewstate not found");
				}

				rexViewstate1.search(start);
				if(rexViewstate1.didMatch()){
					viewstate1 = rexViewstate1.stringMatched(1);
					//Vm.debug("ViewState: " + viewstate);
				} else {
					viewstate1 = "";
					pref.log("Viewstate1 not found");
				}

				rexEventvalidation.search(start);
				if(rexEventvalidation.didMatch()){
					// eventvalidation = rexEventvalidation.stringMatched(1);
					//Vm.debug("EVENTVALIDATION: " + eventvalidation);
				} else {
					// eventvalidation = "";
					// pref.log("Eventvalidation not found");
				}

				//Vm.debug("In loop");
				Regex listBlockRex = new Regex(p.getProp("listBlockRex")); // "<table id=\"dlResults\"((?s).*?)</table>"
				listBlockRex.search(start);
				dummy = listBlockRex.stringMatched(1);
				try{
					lineRex.search(dummy);
				}catch(NullPointerException nex){
					Global.getPref().log("Ignored Exception", nex, true);
				}
				while(lineRex.didMatch()){
					//Vm.debug(getDist(lineRex.stringMatched(1)) + " / " +getWP(lineRex.stringMatched(1)));
					found_on_page++;
					if(getDist(lineRex.stringMatched(1)) <= distance){
						String waypoint=getWP(lineRex.stringMatched(1));
						CacheHolder existingCache;
						if((existingCache=cacheDB.get(waypoint)) == null){
							if ( (maxNumber > 0) && (cachesToLoad.size() >= maxNumber) ) {
								maxNumberAbort = true;

								//add no more caches
								distance = 0;

								//don't update existing caches, because list is not correct when aborting
								cachesToUpdate.clear();
							} else {
								cachesToLoad.add(waypoint);

								//if we don't want to update caches, we can stop directly after adding the maximum of new caches.
								if ( (pref.spiderUpdates == Preferences.NO) && (maxNumber > 0) && (cachesToLoad.size() >= maxNumber)) {
									maxNumberAbort = true;

									//add no more caches
									distance = 0;

									//don't update existing caches, because list is not correct when aborting
									cachesToUpdate.clear();
								}
							}
						} else {
							pref.log(waypoint+" already in DB");
							ch=existingCache;
							// If the <strike> tag is used, the cache is marked as unavailable or archived
							boolean is_archived_GC=lineRex.stringMatched(1).indexOf("<strike><font color=\"red\">")!=-1;
							boolean is_available_GC=lineRex.stringMatched(1).indexOf("<strike>")==-1;
							if (ch.is_archived()!=is_archived_GC) { // Update the database with the cache status
								pref.log("Updating status of "+waypoint+" to "+(is_archived_GC?"archived":"not archived"));
								if ( ch.is_archived() ) {
									cachesToUpdate.put(ch.getWayPoint(), ch);
								}
								ch.setArchived(is_archived_GC);
							} else if (ch.is_available()!=is_available_GC) { // Update the database with the cache status
								pref.log("Updating status of "+waypoint+" to "+(is_available_GC?"available":"not available"));
								ch.setAvailable(is_available_GC);
							} else if (spiderAllFinds && !ch.is_found()) { // Update the database with the cache status
								pref.log("Updating status of "+waypoint+" to found");
								ch.setFound(true);
							} else {
								cachesToUpdate.remove( ch.getWayPoint() );
							}
						}
					} else distance = 0;
					lineRex.searchFrom(dummy, lineRex.matchedTo());
				}

				page++;
				infB.setInfo(MyLocale.getMsg(5521,"Page ") + page + "\n" + MyLocale.getMsg(5511,"Found ") + cachesToLoad.size() + MyLocale.getMsg(5512," caches"));

				if(found_on_page < 20) distance = 0;
				if (spiderAllFinds) {
					postStr = p.getProp("firstLine");
				} else {
					postStr = p.getProp("firstLine") + origin.getLatDeg(TransformCoordinates.DD) + p.getProp("firstLine2") + origin.getLonDeg(TransformCoordinates.DD)
							                             + p.getProp("maxDistance") + Integer.toString( (int)saveDistanceInMiles );
					if(doNotgetFound) postStr = postStr + p.getProp("showOnlyFound");
				}
				postStr = postStr + cacheTypeRestriction;
				if(distance > 0){
					page_number++;
					String strNextPage;
					/*
					if(page_number >= 15) page_number = 5;
					if (page_number < 10) {
						strNextPage = "ctl00$ContentBody$pgrTop$ctl0" + page_number;
					} else {
						strNextPage = "ctl00$ContentBody$pgrTop$ctl" + page_number;
					}
					*/
					strNextPage = "ctl00$ContentBody$pgrTop$ctl08";
					
					doc = URL.encodeURL("__EVENTTARGET",false) +"="+ URL.encodeURL(strNextPage,false)
					    + "&" + URL.encodeURL("__EVENTARGUMENT",false) +"="+ URL.encodeURL("",false)
//					    + "&" + URL.encodeURL("__VIEWSTATEFIELDCOUNT",false) +"=2"
					    + "&" + URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false);
//					    + "&" + URL.encodeURL("__VIEWSTATE1",false) +"="+ URL.encodeURL(viewstate1,false);
//					    + "&" + URL.encodeURL("__EVENTVALIDATION",false) +"="+ URL.encodeURL(eventvalidation,false);
					try{
						start = "";
						pref.log("Fetching next list page:" + doc);
						start = fetch_post(postStr, doc, p.getProp("nextListPage"));
					}catch(Exception ex){
						//Vm.debug("Couldn't get the next page");
						pref.log("Error getting next page");
					}
				}
				//Vm.debug("Distance is now: " + distance);
				found_on_page = 0;
			}
		} catch (Exception ex) { // Some tag missing from spider.def
			infB.close(0);
			Vm.showWait(false);
			return;
		}
		pref.log("Found " + cachesToLoad.size() + " new caches");
		pref.log("Found " + cachesToUpdate.size() + " caches for update");
		if (!infB.isClosed) infB.setInfo(MyLocale.getMsg(5511,"Found ") + cachesToLoad.size() + MyLocale.getMsg(5512," caches"));

		//=======
		// Now ready to spider each cache in the list
		//=======
		boolean loadAllLogs = (pref.maxLogsToSpider > 5) || spiderAllFinds;

		int spiderErrors = 0;

		if ( cachesToUpdate.size() > 0 ) {
			switch (pref.spiderUpdates) {
			case Preferences.NO:
				cachesToUpdate.clear();
				break;
			case Preferences.ASK:
				MessageBox mBox = new MessageBox(MyLocale.getMsg(5517,"Spider Updates?"), cachesToUpdate.size() + MyLocale.getMsg(5518," caches in database need an update. Update now?") , FormBase.IDYES |FormBase.IDNO);
				if (mBox.execute() != FormBase.IDOK){
					cachesToUpdate.clear();
				}
				break;
			}
		}

		int totalCachesToLoad = cachesToLoad.size() + cachesToUpdate.size();

		for(int i = 0; i<cachesToLoad.size(); i++){
			if (infB.isClosed) break;

			wpt = (String)cachesToLoad.get(i);
			// Get only caches not already available in the DB
			if(cacheDB.getIndex(wpt) == -1){
				infB.setInfo(MyLocale.getMsg(5513,"Loading: ") + wpt +" (" + (i+1) + " / " + totalCachesToLoad + ")");
				holder = new CacheHolder();
				holder.setWayPoint(wpt);
				int test = getCacheByWaypointName(holder,false,getImages,getTBs,doNotgetFound,loadAllLogs);
				if (test == SPIDER_CANCEL) {
					infB.close(0);
					break;
				} else if (test == SPIDER_ERROR) {
					spiderErrors++;
				} else if (test == SPIDER_OK){
					if (!holder.is_found() || !doNotgetFound ) {
						cacheDB.add(holder);
						holder.save();
					}
				} // For test==SPIDER_IGNORE_PREMIUM: Nothing to do
			}
		}

		if (!infB.isClosed) {
			int j = 1;
			for (Enumeration e = cachesToUpdate.elements() ; e.hasMoreElements() ; j++) {
				ch = (CacheHolder)e.nextElement();
				infB.setInfo(MyLocale.getMsg(5513,"Loading: ") + ch.getWayPoint() +" (" + (cachesToLoad.size()+j) + " / " + totalCachesToLoad + ")");
				infB.redisplay();

				int test = spiderSingle(cacheDB.getIndex(ch), infB,false,loadAllLogs);
				if (test == SPIDER_CANCEL) {
					break;
				} else if (test == SPIDER_ERROR) {
					spiderErrors++;
					Global.getPref().log("SpiderGC: could not spider "+ch.getWayPoint());
				} else {
					//profile.hasUnsavedChanges=true;
				}
			}
		}

		infB.close(0);
		Vm.showWait(false);
		if ( spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500,"Error"),spiderErrors + MyLocale.getMsg(5516," cache descriptions%0acould not be loaded."),FormBase.DEFOKB).execute();
		}
		if ( maxNumberAbort ) {
			new MessageBox(MyLocale.getMsg(5519,"Information"),MyLocale.getMsg(5520,"Only the given maximum of caches were loaded.%0aRepeat spidering later to load more caches.%0aNo already existing caches were updated."),FormBase.DEFOKB).execute();
		}
		Global.getProfile().restoreFilter();
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
	 * @param boolean fetchTBs True if the TBs are to be fetched
	 * @param boolean doNotGetFound True if the cache is not to be spidered if it has already been found
	 * @param boolean fetchAllLogs True if all logs are to be fetched (by adding option '&logs=y' to command line).
	 *     This is normally false when spidering from GPXImport as the logs are part of the GPX file, and true otherwise
	 * @return -1 if the infoBox was closed (cancel spidering), 0 if there was an error (continue with next cache), 1 if everything ok
	 */
	private int getCacheByWaypointName(CacheHolder ch, boolean isUpdate, boolean fetchImages, boolean fetchTBs, boolean doNotGetFound, boolean fetchAllLogs) {
		int ret = SPIDER_OK; // initialize value;
		while (true) {
			String completeWebPage;
			int spiderTrys=0;
			int MAX_SPIDER_TRYS=3;
			while (spiderTrys++<MAX_SPIDER_TRYS) {
				ret = SPIDER_OK; // initialize value;
				try{
					String doc = p.getProp("getPageByName") + ch.getWayPoint() +(fetchAllLogs?p.getProp("fetchAllLogs"):"");
					pref.log("Fetching: " + ch.getWayPoint());
					completeWebPage = fetch(doc);
					if	( completeWebPage.equals("")) {
						pref.log("Could not fetch " + ch.getWayPoint());
						if (!infB.isClosed) {
							continue;
						} else {
							ch.setIncomplete(true);
							return SPIDER_CANCEL;
						}
					}
				}catch(Exception ex){
					pref.log("Could not fetch " + ch.getWayPoint(),ex);
					if (!infB.isClosed) {
						continue;
					} else {
						ch.setIncomplete(true);
						return SPIDER_CANCEL;
					}
				}
				// Only analyse the cache data and fetch pictures if user has not closed the progress window
				if (!infB.isClosed) {
					try{
						ch.initStates(!isUpdate);

						//first check if coordinates are available to prevent deleting existing coorinates
						String latLon = getLatLon(completeWebPage);
						if (latLon.equals("???")) {
							if (completeWebPage.indexOf(p.getProp("premiumCachepage"))>0) {
								// Premium cache spidered by non premium member
								pref.log("Ignoring premium member cache: "+ch.getWayPoint());
								spiderTrys = MAX_SPIDER_TRYS;
								ret = SPIDER_IGNORE_PREMIUM;
								continue;
							} else {
								pref.log(">>>> Failed to spider Cache. Retry.");
								ret = SPIDER_ERROR;
								continue; // Restart the spider
							}
						}

						ch.setHTML(true);
						ch.setAvailable(true);
						ch.setArchived(false);
						ch.setIncomplete(true);
						// Save size of logs to be able to check whether any new logs were added
						//int logsz = chD.CacheLogs.size();
						//chD.CacheLogs.clear();
						ch.addiWpts.clear();
						ch.getFreshDetails().images.clear();

						if(completeWebPage.indexOf(p.getProp("cacheUnavailable")) >= 0) ch.setAvailable(false);
						if(completeWebPage.indexOf(p.getProp("cacheArchived")) >= 0) ch.setArchived(true);
						//==========
						// General Cache Data
						//==========
						ch.setLatLon(latLon);
						pref.log("LatLon: " + ch.LatLon);
						if (pref.debug) pref.log("chD.pos: " + ch.pos.toString());

						pref.log("Trying description");
						ch.getFreshDetails().setLongDescription(getLongDesc(completeWebPage));
						pref.log("Got description");

						pref.log("Getting cache name");
						ch.setCacheName(SafeXML.cleanback(getName(completeWebPage)));
						if (pref.debug) pref.log("Name: " + ch.getCacheName()); else pref.log("Got name");

						pref.log("Trying location (country/state)");
						String location = getLocation(completeWebPage);
						if (location.length() != 0) {
							int countryStart = location.indexOf(",");
							if (countryStart > -1) {
								ch.getFreshDetails().Country = SafeXML.cleanback(location.substring(countryStart + 1).trim());
								ch.getFreshDetails().State = SafeXML.cleanback(location.substring(0, countryStart).trim());
							} else {
								ch.getFreshDetails().Country = location.trim();
								ch.getFreshDetails().State = "";
							}
							pref.log("Got location (country/state)");
						} else {
							ch.getFreshDetails().Country = "";
							ch.getFreshDetails().State = "";
							pref.log("No location (country/state) found");
						}

						pref.log("Trying owner");
						ch.setCacheOwner(SafeXML.cleanback(getOwner(completeWebPage)).trim());
						if(ch.getCacheOwner().equals(pref.myAlias) || (pref.myAlias2.length()>0 && ch.getCacheOwner().equals(pref.myAlias2))) ch.setOwned(true);
						if (pref.debug) pref.log("Owner: " + ch.getCacheOwner() +"; is_owned = "+ch.is_owned()+";  alias1,2 = ["+pref.myAlias+"|"+pref.myAlias2+"]");
						else pref.log("Got owner");


						pref.log("Trying date hidden");
						ch.setDateHidden(DateFormat.MDY2YMD(getDateHidden(completeWebPage)));
						if (pref.debug) pref.log("Hidden: " + ch.getDateHidden());
						else pref.log("Got date hidden");

						pref.log("Trying hints");
						ch.getFreshDetails().setHints(getHints(completeWebPage));
						if (pref.debug) pref.log("Hints: " + ch.getFreshDetails().Hints);
						else pref.log("Got hints");

						pref.log("Trying size");
						ch.setCacheSize(CacheSize.gcSpiderString2Cw(getSize(completeWebPage)));
						if (pref.debug) pref.log("Size: " + ch.getCacheSize());
						else pref.log("Got size");

						pref.log("Trying difficulty");
						ch.setHard(CacheTerrDiff.v1Converter(getDiff(completeWebPage)));
						if (pref.debug) pref.log("Hard: " + ch.getHard());
						else pref.log("Got difficulty");

						pref.log("Trying terrain");
						ch.setTerrain(CacheTerrDiff.v1Converter(getTerr(completeWebPage)));
						if (pref.debug) pref.log("Terr: " + ch.getTerrain());
						else pref.log("Got terrain");

						pref.log("Trying cache type");
						ch.setType(getType(completeWebPage));
						if (pref.debug) pref.log("Type: " + ch.getType());
						else pref.log("Got cache type");

						//==========
						// Logs
						//==========
						pref.log("Trying logs");
						ch.getFreshDetails().setCacheLogs(getLogs(completeWebPage, ch.getFreshDetails()));
						pref.log("Found logs");

						// If the switch is set to not store found caches and we found the cache => return
						if (ch.is_found() && doNotGetFound) {
							if (infB.isClosed) {
								return SPIDER_CANCEL;
							} else {
								return SPIDER_OK;
							}
						}

						//==========
						// Bugs
						//==========
						// As there may be several bugs, we check whether the user has aborted
						if (!infB.isClosed && fetchTBs) getBugs(ch.getFreshDetails(),completeWebPage);
						ch.setHas_bugs(ch.getFreshDetails().Travelbugs.size()>0);

						//==========
						// Images
						//==========
						if(fetchImages){
							pref.log("Trying images");
							getImages(completeWebPage, ch.getFreshDetails());
							pref.log("Got images");
						}
						//==========
						// Addi waypoints
						//==========

						pref.log("Getting additional waypoints");
						getAddWaypoints(completeWebPage, ch.getWayPoint(), ch.is_found());
						pref.log("Got additional waypoints");

						//==========
						// Attributes
						//==========
						pref.log("Getting attributes");
						getAttributes(completeWebPage, ch.getFreshDetails());
						pref.log("Got attributes");
						//if (ch.is_new()) ch.setUpdated(false);
						//==========
						// Last sync date
						//==========
						ch.setLastSync((new Time()).format("yyyyMMddHHmmss"));

						ch.setIncomplete(false);
						break;
					}catch(Exception ex){
						pref.log("Error reading cache: "+ch.getWayPoint());
						pref.log("Exception in getCacheByWaypointName: ",ex);
					}
				} else {
					break;
				}
			} // spiderTrys
			if ( ( spiderTrys >= MAX_SPIDER_TRYS ) && ( ret == SPIDER_OK ) ) {
				pref.log(">>> Failed to spider cache. Number of retrys exhausted.");
				int decision = (new MessageBox(MyLocale.getMsg(5500,"Error"),MyLocale.getMsg(5515,"Failed to load cache.%0aPleas check your internet connection.%0aRetry?"),FormBase.DEFOKB|FormBase.NOB|FormBase.CANCELB)).execute();
				if ( decision == FormBase.IDOK ) {
					continue;
				} else if ( decision == FormBase.IDNO ){
					ret = SPIDER_ERROR;
				} else {
					ret = SPIDER_CANCEL;
				}
			}
			break;
		}//while(true)
		if (infB.isClosed) {// If the infoBox was closed before getting here, we return -1
			return SPIDER_CANCEL;
		}
		return ret;
	} // getCacheByWaypointName


	/**
	 * Get the Distance to the centre
	 * @param doc A previously fetched cachepage
	 * @return Distance
	 */
	private double getDist(String doc) throws Exception {
		inRex = new Regex(p.getProp("distRex"));
		inRex.search(doc);
		if(doc.indexOf("Here") >= 0) return(0);
		if (!inRex.didMatch()) return 0;
		if(MyLocale.getDigSeparator().equals(",")) return Convert.toDouble(inRex.stringMatched(1).replace('.',','));
		return Convert.toDouble(inRex.stringMatched(1));
	}

	/**
	 * Get the waypoint name
	 * @param doc A previously fetched cachepage
	 * @return Name of waypoint to add to list
	 */
	private String getWP(String doc) throws Exception {
		inRex = new Regex(p.getProp("waypointRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "???";
		return "GC"+inRex.stringMatched(1);
	}

	/**
	 * Get the coordinates of the cache
	 * @param doc A previously fetched cachepage
	 * @return Cache coordinates
	 */
	private String getLatLon(String doc) throws Exception{
		inRex = new Regex(p.getProp("latLonRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the long description
	 * @param doc A previously fetched cachepage
	 * @return the long description
	 */
	private String getLongDesc(String doc) throws Exception{
		String res = "";
		inRex = new Regex(p.getProp("shortDescRex"));
		Regex rex2 = new Regex(p.getProp("longDescRex"));
		inRex.search(doc);
		rex2.search(doc);
		res = ((inRex.stringMatched(1)==null)?"":inRex.stringMatched(1)) + "<br>";
		res += rex2.stringMatched(1);
		int spanEnd = res.lastIndexOf("</span>");
		if (spanEnd >= 0) {
			res = res.substring(0, spanEnd);
		}
		return res; // SafeXML.cleanback(res);
	}

	/**
	 * Get the cache location (country and state)
	 * @param doc A previously fetched cachepage
	 * @return the location (country and state) of the cache
	 */
	private String getLocation(String doc) throws Exception{
		inRex = new Regex(p.getProp("cacheLocationRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "";

		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache name
	 * @param doc A previously fetched cachepage
	 * @return the name of the cache
	 */
	private String getName(String doc) throws Exception{
		inRex = new Regex(p.getProp("cacheNameRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache owner
	 * @param doc A previously fetched cachepage
	 * @return the cache owner
	 */
	private String getOwner(String doc) throws Exception{
		inRex = new Regex(p.getProp("cacheOwnerRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the date when the cache was hidden
	 * @param doc A previously fetched cachepage
	 * @return Hidden date
	 */
	private String getDateHidden(String doc) throws Exception{
		inRex = new Regex(p.getProp("dateHiddenRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the hints
	 * @param doc A previously fetched cachepage
	 * @return Cachehints
	 */
	private String getHints(String doc) throws Exception{
		inRex = new Regex(p.getProp("hintsRex"));
		inRex.search(doc);
		if (!inRex.didMatch()) return "";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache size
	 * @param doc A previously fetched cachepage
	 * @return Cache size
	 */
	private String getSize(String doc) throws Exception{
		inRex = new Regex(p.getProp("sizeRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "None";
	}

	/**
	 * Get the Difficulty
	 * @param doc A previously fetched cachepage
	 * @return The cache difficulty
	 */
	private String getDiff(String doc) throws Exception{
		inRex = new Regex(p.getProp("difficultyRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}

	/**
	 * Get the terrain rating
	 * @param doc A previously fetched cachepage
	 * @return Terrain rating
	 */
	private String getTerr(String doc) throws Exception{
		inRex = new Regex(p.getProp("terrainRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}

	/**
	 * Get the waypoint type
	 * @param doc A previously fetched cachepage
	 * @return the waypoint type (Tradi, Multi, etc.)
	 */
	private byte getType(String doc) throws Exception {
		inRex = new Regex(p.getProp("cacheTypeRex"));
		inRex.search(doc);
		if(inRex.didMatch()) return CacheType.gcSpider2CwType(inRex.stringMatched(1));
		else return 0;
	}

	/**
	 * Get the logs
	 * @param doc A previously fetched cachepage
	 * @param chD Cache Details
	 * @return A HTML string containing the logs
	 */
	private LogList getLogs(String doc, CacheHolderDetail chD) throws Exception {
		String icon = "";
		String name = "";
		String logText = "";
		String logId = "";
		LogList reslts = new LogList();
		Regex blockRex = new Regex(p.getProp("blockRex"));
		blockRex.search(doc);
		doc = blockRex.stringMatched(1);
		String singleLog = "";
		Extractor exSingleLog = new Extractor(doc,p.getProp("singleLogExStart"), p.getProp("singleLogExEnd"), 0, false); // maybe here is some change neccessary because findnext now gives the whole endstring back???
		singleLog = exSingleLog.findNext();
		Extractor exIcon = new Extractor(singleLog,p.getProp("iconExStart"), p.getProp("iconExEnd"), 0, true);
		Extractor exNameTemp = new Extractor(singleLog,p.getProp("nameTempExStart"), p.getProp("nameTempExEnd"), 0 , true);
		String nameTemp = "";
		nameTemp = exNameTemp.findNext();
		Extractor exName = new Extractor(nameTemp, p.getProp("nameExStart"), p.getProp("nameExEnd"), 0 , true);
		Extractor exDate = new Extractor(singleLog,p.getProp("dateExStart"), p.getProp("dateExEnd"), 0 , true);
		Extractor exLog = new Extractor(singleLog, p.getProp("logExStart"), p.getProp("logExEnd"), 0, true);
		Extractor exLogId = new Extractor(singleLog, p.getProp("logIdExStart"), p.getProp("logIdExEnd"), 0, true);
		//Vm.debug("Log Block: " + singleLog);
		int nLogs=0;
		while(exSingleLog.endOfSearch() == false){
			nLogs++;
			//Vm.debug("--------------------------------------------");
			//Vm.debug("Log Block: " + singleLog);
			//Vm.debug("Icon: "+exIcon.findNext());
			//Vm.debug(exName.findNext());
			//Vm.debug(exDate.findNext());
			//Vm.debug(exLog.findNext());
			//Vm.debug("--------------------------------------------");
			icon = exIcon.findNext();
			name = exName.findNext();
			logText = exLog.findNext();
			logId = exLogId.findNext();
			String d=DateFormat.logdate2YMD(exDate.findNext());
			if((icon.equals(p.getProp("icon_smile")) || icon.equals(p.getProp("icon_camera")) || icon.equals(p.getProp("icon_attended"))) &&
				(name.equalsIgnoreCase(SafeXML.clean(pref.myAlias)) || (pref.myAlias2.length()>0 && name.equalsIgnoreCase(SafeXML.clean(pref.myAlias2)))) )  {
				chD.getParent().setFound(true);
				chD.getParent().setCacheStatus(d);
				chD.OwnLogId = logId;
				chD.OwnLog = new Log(icon,d,name,logText);
			}
			if (nLogs<=pref.maxLogsToSpider) reslts.add(new Log(icon,d,name,logText));

			singleLog = exSingleLog.findNext();
			exIcon.setSource(singleLog);
			exNameTemp.setSource(singleLog);
			nameTemp = exNameTemp.findNext();
			exName.setSource(nameTemp);
			exDate.setSource(singleLog);
			exLog.setSource(singleLog);
			exLogId.setSource(singleLog);
			// We cannot simply stop if we have reached MAXLOGS just in case we are waiting for
			// a log by our alias that happened earlier.
			if (nLogs>=pref.maxLogsToSpider && chD.getParent().is_found() && (chD.OwnLogId.length() != 0) && (chD.OwnLog != null) && !(chD.OwnLog.getDate().equals("1900-01-01"))) break;
		}
		if (nLogs>pref.maxLogsToSpider) {
			reslts.add(Log.maxLog());
			pref.log("Too many logs. MAXLOGS reached ("+pref.maxLogsToSpider+")");
		} else
			pref.log(nLogs+" logs found");
		return reslts;
	}

	/**
	 * Read the travelbug names from a previously fetched Cache page and then
	 * read the travelbug purpose for each travelbug
	 * @param doc The previously fetched cachepage
	 * @return A HTML formatted string with bug names and there purpose
	 */
	public void getBugs(CacheHolderDetail chD, String doc) throws Exception{
		Extractor exBlock = new Extractor(doc,p.getProp("blockExStart"),p.getProp("blockExEnd") ,0,Extractor.EXCLUDESTARTEND);
		String bugBlock = exBlock.findNext();
		//Vm.debug("Bugblock: "+bugBlock);
		Extractor exBug = new Extractor(bugBlock,p.getProp("bugExStart"),p.getProp("bugExEnd"),0,Extractor.EXCLUDESTARTEND);
		String link,bug,linkPlusBug,bugDetails;
		String oldInfoBox=infB.getInfo();
		chD.Travelbugs.clear();
		while(exBug.endOfSearch() == false){
			if (infB.isClosed) break; // Allow user to cancel by closing progress form
			linkPlusBug= exBug.findNext();
			int idx=linkPlusBug.indexOf("'>");
			if (idx<0) break; // No link/bug pair found
			link=linkPlusBug.substring(0,idx);
			bug=linkPlusBug.substring(idx+2);
			if(bug.length()>0) { // Found a bug, get its details
				Travelbug tb=new Travelbug(bug);
				try{
					infB.setInfo(oldInfoBox+MyLocale.getMsg(5514,"\nGetting bug: ")+SafeXML.cleanback(bug));
					pref.log("Fetching bug details: "+bug);
					bugDetails = fetch(link);
					Extractor exDetails = new Extractor(bugDetails,p.getProp("bugDetailsStart"),p.getProp("bugDetailsEnd"),0,Extractor.EXCLUDESTARTEND);
					tb.setMission(exDetails.findNext());
					Extractor exGuid = new Extractor(bugDetails,"details.aspx?guid=","\" id=\"aspnetForm",0,Extractor.EXCLUDESTARTEND); // TODO Replace with spider.def see also further down
					tb.setGuid(exGuid.findNext());
					chD.Travelbugs.add(tb);
				}catch(Exception ex){
					pref.log("Could not fetch bug details");
				}
			}
			//Vm.debug("B: " + bug);
			//Vm.debug("End? " + exBug.endOfSearch());
		}
		infB.setInfo(oldInfoBox);
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
		int spiderCounter = 0;
		String fileName, imgName, imgType, imgUrl, imgComment;
		Vector spideredUrls=new Vector(15);
		ImageInfo imageInfo=null;
		Extractor exImgBlock,exImgComment;
		int idxUrl; // Index of already spidered Url in list of spideredUrls
		CacheImages lastImages=null;

		// First: Get current image object of waypoint before spidering images.
		CacheHolder oldCh = Global.getProfile().cacheDB.get(chD.getParent().getWayPoint());
		if (oldCh != null) {
			lastImages = oldCh.getFreshDetails().images;
		}

		//========
		//In the long description
		//========
		String longDesc = "";
		try {
			if (chD.getParent().getWayPoint().startsWith("TC")) longDesc = doc;
			else
				longDesc = getLongDesc(doc);
			longDesc = STRreplace.replace(longDesc, "<img", "<IMG");
			longDesc = STRreplace.replace(longDesc, "src=", "SRC=");
			longDesc = STRreplace.replace(longDesc, "'", "\"");
			exImgBlock = new Extractor(longDesc,p.getProp("imgBlockExStart"),p.getProp("imgBlockExEnd"), 0, false);
		} catch (Exception ex) {//Missing property in spider.def
			return;
		}
		//Vm.debug("In getImages: Have longDesc" + longDesc);
		String tst;
		tst = exImgBlock.findNext();
		//Vm.debug("Test: \n" + tst);
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while(exImgBlock.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				// Optimize: img.groundspeak.com -> img.geocaching.com (for better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try{
					imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
					// imgType is now max 4 chars, starting with .
					if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
						// Check whether image was already spidered for this cache
						idxUrl=spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl<0) { // New image
							fileName = chD.getParent().getWayPoint().toLowerCase() + "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl, fileName+imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("Loading image: " + imgUrl+" as "+fileName+imgType);
								spiderImage(imgUrl, fileName+imgType);
								imageInfo.setFilename(fileName+imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("Already exising image: " + imgUrl+" as "+imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint() + "_" + Convert.toString(idxUrl);
							pref.log("Already loaded image: " + imgUrl+" as "+fileName+imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName+imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(imgName);
						imageInfo.setComment(null);
						imgCounter++;
						chD.images.add(imageInfo);
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
		Extractor spanBlock,exImgName;
		try {
			spanBlock = new Extractor(doc,p.getProp("imgSpanExStart"),p.getProp("imgSpanExEnd"), 0 , true);
			tst = spanBlock.findNext();
			exImgName = new Extractor(tst,p.getProp("imgNameExStart"),p.getProp("imgNameExEnd"), 0 , true);
			exImgSrc = new Extractor(tst,p.getProp("imgSrcExStart"),p.getProp("imgSrcExEnd"), 0, true);
			exImgComment = new Extractor(tst,p.getProp("imgCommentExStart"),p.getProp("imgCommentExEnd"), 0, true);
		} catch (Exception ex) { // Missing property in spider .def
			return;
		}
		while(exImgSrc.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			imgComment = exImgComment.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				try{
					imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
					// imgType is now max 4 chars, starting with .
					if(imgType.startsWith(".png") || imgType.startsWith(".jpg") || imgType.startsWith(".gif")){
						// Check whether image was already spidered for this cache
						idxUrl=spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl<0) { // New image
							fileName = chD.getParent().getWayPoint() + "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl, fileName+imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("Loading image: " + imgUrl+" as "+fileName+imgType);
								spiderImage(imgUrl, fileName+imgType);
								imageInfo.setFilename(fileName+imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("Already exising image: " + imgUrl+" as "+imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint() + "_" + Convert.toString(idxUrl);
							pref.log("Already loaded image: " + imgUrl+" as "+fileName+imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName+imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(exImgName.findNext());
						while (imgComment.startsWith("<br />")) imgComment=imgComment.substring(6);
						while (imgComment.endsWith("<br />")) imgComment=imgComment.substring(0,imgComment.length()-6);
						imageInfo.setComment(imgComment);
						chD.images.add(imageInfo);
					}
				} catch (IndexOutOfBoundsException e) {
					pref.log("IndexOutOfBoundsException in image span. imgURL:"+imgUrl,e);
				}
			}
		}
		//========
		//Final sweep to check for images in hrefs
		//========
		Extractor exFinal = new Extractor(longDesc, "http://", "\"", 0, true);
		while(exFinal.endOfSearch() == false){
			imgUrl = exFinal.findNext();
			if(imgUrl.length()>0){
				// Optimize: img.groundspeak.com -> img.geocaching.com (for better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try{
					imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
					// imgType is now max 4 chars, starting with . Delete characters in URL after the image extension
					imgUrl=imgUrl.substring(0,imgUrl.lastIndexOf(".")+imgType.length());
					if( imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")){
						// Check whether image was already spidered for this cache
						idxUrl=spideredUrls.find(imgUrl);
						if (idxUrl<0) { // New image
							imgName = chD.getParent().getWayPoint() + "_" + Convert.toString(imgCounter);
							fileName = chD.getParent().getWayPoint() + "_" + Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl, fileName+imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								pref.log("Loading image: " + imgUrl+" as "+fileName+imgType);
								spiderImage(imgUrl, fileName+imgType);
								imageInfo.setFilename(fileName+imgType);
								imageInfo.setURL(imgUrl);
							} else {
								pref.log("Already exising image: " + imgUrl+" as "+imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
							imageInfo.setTitle(imgName);
							imgCounter++;
							chD.images.add(imageInfo);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					pref.log("Problem loading image. imgURL:"+imgUrl);
				}
			}
		}
	}


	/**
	 * Read an image from the server
	 * @param imgUrl The Url of the image
	 * @param target The bytes of the image
	 */
	private void spiderImage(String imgUrl, String target){ // TODO implement a fetch(URL, filename) in HttpConnection and use that one
		HttpConnection connImg;
		Socket sockImg;
		//InputStream is;
		FileOutputStream fos;
		//int bytes_read;
		//byte[] buffer = new byte[9000];
		ByteArray daten;
		String datei = "";
		datei = profile.dataDir + target;
		connImg = new HttpConnection(imgUrl);
		if (imgUrl.indexOf('%')>=0) connImg.documentIsEncoded=true;
		connImg.setRequestorProperty("Connection", "close");
		//connImg.setRequestorProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12");
		//connImg.setRequestorProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		try{
			pref.log("Trying to fetch image from: " + imgUrl);
			String redirect=null;
			do {
				sockImg = connImg.connect();
				redirect=connImg.getRedirectTo();
				if (redirect!=null) {
					connImg=connImg.getRedirectedConnection(redirect);
					pref.log("Redirect to "+redirect);
				}
			} while(redirect!=null); // TODO this can end up in an endless loop if trying to load from a malicous site
			daten = connImg.readData(sockImg);
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
	public void getAddWaypoints(String doc, String wayPoint, boolean is_found) throws Exception{
		Extractor exWayBlock = new Extractor(doc,p.getProp("wayBlockExStart"),p.getProp("wayBlockExEnd"), 0, false);
		String wayBlock = "";
		String rowBlock = "";
		wayBlock = exWayBlock.findNext();
		Regex nameRex = new Regex(p.getProp("nameRex"));
		Regex koordRex = new Regex(p.getProp("koordRex"));
		Regex descRex = new Regex(p.getProp("descRex"));
		Regex typeRex = new Regex(p.getProp("typeRex"));
		int counter = 0;
		if(exWayBlock.endOfSearch() == false && wayBlock.indexOf("No additional waypoints to display.")<0){
			Extractor exRowBlock = new Extractor(wayBlock,p.getProp("rowBlockExStart"),p.getProp("rowBlockExEnd"), 0, false);
			rowBlock = exRowBlock.findNext();
			rowBlock = exRowBlock.findNext();
			while(exRowBlock.endOfSearch()==false){
				CacheHolder hd = null;
				Extractor exPrefix=new Extractor(rowBlock,p.getProp("prefixExStart"),p.getProp("prefixExEnd"),0,true);
				String prefix=exPrefix.findNext();
				String adWayPoint;
				if (prefix.length()==2)
					adWayPoint=prefix+wayPoint.substring(2);
				else
				    adWayPoint = MyLocale.formatLong(counter, "00") + wayPoint.substring(2);
				counter++;
				int idx=profile.getCacheIndex(adWayPoint);
				if (idx>=0) {
					// Creating new CacheHolder, but accessing old cache.xml file
					hd=new CacheHolder();
					hd.setWayPoint(adWayPoint);
					hd.getExistingDetails(); // Accessing Details reads file if not yet done
				} else {
					hd=new CacheHolder();
					hd.setWayPoint(adWayPoint);
				}
				hd.initStates(idx<0);
				nameRex.search(rowBlock);
				koordRex.search(rowBlock);
				typeRex.search(rowBlock);
				hd.setCacheName(nameRex.stringMatched(1));
				if(koordRex.didMatch()) hd.setLatLon(koordRex.stringMatched(1));
				if(typeRex.didMatch()) hd.setType(CacheType.gpxType2CwType("Waypoint|"+typeRex.stringMatched(1)));
				rowBlock = exRowBlock.findNext();
				descRex.search(rowBlock);
				hd.getFreshDetails().setLongDescription(descRex.stringMatched(1));
				hd.setFound(is_found);
				hd.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				hd.setHard(CacheTerrDiff.CW_DT_UNSET);
				hd.setTerrain(CacheTerrDiff.CW_DT_UNSET);
				if (idx<0){
					cacheDB.add(hd);
					hd.save();
				}else {
					CacheHolder cx=cacheDB.get(idx);
					if (cx.is_Checked && // Only re-spider existing addi waypoints that are ticked
				 	   cx.isVisible()) { // and are visible (i.e.  not filtered)
					   cx.initStates(false);
					   cx.update(hd);
					   cx.is_Checked=true;
					   cx.save();
					}
				}
				rowBlock = exRowBlock.findNext();

			}
		}
	}

	public void getAttributes(String doc, CacheHolderDetail chD) throws Exception {
		Extractor attBlock = new Extractor(doc,p.getProp("attBlockExStart"),p.getProp("attBlockExEnd"), 0 , true);
		String atts = attBlock.findNext();
		Extractor attEx = new Extractor(atts,p.getProp("attExStart"),p.getProp("attExEnd"), 0 , true);
		String attribute=attEx.findNext();
		chD.attributes.clear();
		while (attEx.endOfSearch()==false) {
			chD.attributes.add(attribute);
			attribute=attEx.findNext();
		}
		chD.getParent().setAttributesYes(chD.attributes.attributesYes);
		chD.getParent().setAttributesNo(chD.attributes.attributesNo);
	}


	/**
	*	Performs an initial fetch to a given address. In this case
	*	it will be a gc.com address. This method is used to obtain
	*	the result of a search for caches screen.
	*/
	public static String fetch(String address) {
		CharArray c_data;
		try{
			HttpConnection conn;
			if(pref.myproxy.length() > 0 && pref.proxyActive){
				pref.log("[fetch]:Using proxy: " + pref.myproxy + " / " +pref.myproxyport);
			}
			conn = new HttpConnection(address);
			conn.setRequestorProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			if(cookieSession.length()>0){
				conn.setRequestorProperty("Cookie", "ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
				pref.log("[fetch]:Cookie Zeug: " + "Cookie: ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
			} else
				pref.log("[fetch]:No Cookie found");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			if (pref.debug) pref.log("[fetch]:Connecting");
			Socket sock = conn.connect();
			if (pref.debug) pref.log("[fetch]:Connect ok!");
			ByteArray daten = conn.readData(sock);
			if (pref.debug) pref.log("[fetch]:Read data ok");
			JavaUtf8Codec codec = new JavaUtf8Codec();
			c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			sock.close();
			return getResponseHeaders(conn)+ c_data.toString();
		}catch(IOException ioex){
			pref.log("IOException in fetch", ioex);
		}finally{
			//continue
		}
		return "";
	}

	/**
	*	After a fetch to gc.com the next fetches have to use the post method.
	*	This method does exactly that. Actually this method is generic in the sense
	*	that it can be used to post to a URL using http post.
	*/
	private static String fetch_post(String address, String document, String path) {
		HttpConnection conn;
		try {
			conn = new HttpConnection(address);
			JavaUtf8Codec codec = new JavaUtf8Codec();
			conn.documentIsEncoded = true;
			conn.setRequestorProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setPostData(codec.encodeText(document.toCharArray(),0,document.length(),true,null));
			conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
			if(cookieSession.length()>0){
				conn.setRequestorProperty("Cookie", "ASP.NET_SessionId="+cookieSession+"; userid="+cookieID);
				pref.log("[fetch]:Cookie Zeug: " + "Cookie: ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
			} else {
				pref.log("[fetch]:No Cookie found");
			}
			conn.setRequestorProperty("Connection", "close");
			if (pref.debug) pref.log("[fetch]:Connecting");
			Socket sock = conn.connect();
			if (pref.debug) pref.log("[fetch]:Connect ok!");
			ByteArray daten = conn.readData(sock);
			if (pref.debug) pref.log("[fetch]:Read data ok");
			CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			sock.close();
			return getResponseHeaders(conn)+c_data.toString();
		} catch (Exception e) {
			Global.getPref().log("Ignored Exception", e, true);
		}
		return "";
	}

	private static String getResponseHeaders(HttpConnection conn) {
		PropertyList pl = conn.documentProperties;
		if (pl != null) {
			StringBuffer sb = new StringBuffer(1000);
			boolean gotany = false;

			for (int i = 0; i < pl.size(); i++) {
				Property currProp = (Property)pl.get(i);
				if (currProp.value != null) {
					sb.append(currProp.name).append(": ").append(currProp.value).append("\r\n");
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
		char [] dest = new char[6*max]; // Assume each char is a UTF char and encoded into 6 chars
		char d = 0;
		for (int i = 0; i<max; i++){
			char c = (char) what[i];
			if (c <= ' ' || c == '+' || c == '&' || c == '%' || c == '=' ||
				   c == '|' || c == '{' || c == '}' || c>0x7f ){
					dest[d++] = '%';
					dest[d++] = hex.charAt((c >> 4) & 0xf);
					dest[d++] = hex.charAt(c & 0xf);
			} else dest[d++] = c;
		}
		return new String(dest,0,d);
	}

	/**
	 * Load the bug id for a given name. This method is not ideal, as there are
	 * sometimes several bugs with identical names but different IDs. Normally
	 * the bug GUID is used which can be obtained from the cache page.<br>
	 * Note that each bug has both an ID and a GUID.
	 * @param name The name (or partial name) of a travelbug
	 * @return the id of the bug
	 */
	public String getBugId (String name) {
		String bugList;
		try{
			//infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
			pref.log("Fetching bugId: "+name);
			bugList = fetch(p.getProp("getBugByName")+STRreplace.replace(SafeXML.clean(name)," ","+"));
		}catch(Exception ex){
			pref.log("Could not fetch bug list");
			bugList="";
		}
		try {
			if (bugList.equals("") || bugList.indexOf(p.getProp("bugNotFound"))>=0) {
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(6020,"Travelbug not found."), FormBase.OKB)).execute();
				return "";
			}
			if (bugList.indexOf(p.getProp("bugTotalRecords"))<0) {
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(6021,"More than one travelbug found. Specify name more precisely."), FormBase.OKB)).execute();
				return "";
			}
			Extractor exGuid = new Extractor(bugList,p.getProp("bugGuidExStart"),p.getProp("bugGuidExEnd"),0,Extractor.EXCLUDESTARTEND); // TODO Replace with spider.def
			return exGuid.findNext();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Fetch a bug's mission for a given GUID or ID. If the guid String is longer
	 * than 10 characters it is assumed to be a GUID, otherwise it is an ID.
	 * @param guid the guid or id of the travelbug
	 * @return The mission
	 */
	public String getBugMissionByGuid(String guid) {
		String bugDetails;
		try{
			//infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
			pref.log("Fetching bug detailsById: "+guid);
			if (guid.length()>10)
				bugDetails = fetch(p.getProp("getBugByGuid")+guid);
			else
				bugDetails = fetch(p.getProp("getBugById")+guid);
		}catch(Exception ex){
			pref.log("Could not fetch bug details");
			bugDetails="";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound"))>=0) {
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(6020,"Travelbug not found."), FormBase.OKB)).execute();
				return "";
			}
			Extractor exDetails = new Extractor(bugDetails,p.getProp("bugDetailsStart"),p.getProp("bugDetailsEnd"),0,Extractor.EXCLUDESTARTEND);
			return exDetails.findNext();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Fetch a bug's mission for a given tracking number
	 * @param trackNr the tracking number of the travelbug
	 * @return The mission
	 */
	public String getBugMissionByTrackNr(String trackNr) {
		String bugDetails;
		try{
			pref.log("Fetching bug detailsByTrackNr: "+trackNr);
			bugDetails = fetch(p.getProp("getBugByTrackNr")+trackNr);
		}catch(Exception ex){
			pref.log("Could not fetch bug details");
			bugDetails="";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound"))>=0) {
//				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(6020,"Travelbug not found."), MessageBox.OKB)).execute();
				return "";
			}
			Extractor exDetails = new Extractor(bugDetails,p.getProp("bugDetailsStart"),p.getProp("bugDetailsEnd"),0,Extractor.EXCLUDESTARTEND);
			return exDetails.findNext();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Fetch a bug's mission and namefor a given tracking number
	 * @param TB the travelbug
	 * @return true if suceeded
	 */
	public boolean getBugMissionAndNameByTrackNr(Travelbug TB) {
		String bugDetails;
		String trackNr = TB.getTrackingNo();
		try{
			pref.log("Fetching bug detailsByTrackNr: "+trackNr);
			bugDetails = fetch(p.getProp("getBugByTrackNr")+trackNr);
		}catch(Exception ex){
			pref.log("Could not fetch bug details");
			bugDetails="";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound"))>=0) {
//				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(6020,"Travelbug not found."), MessageBox.OKB)).execute();
				return false;
			}
			Extractor exDetails = new Extractor(bugDetails,p.getProp("bugDetailsStart"),p.getProp("bugDetailsEnd"),0,Extractor.EXCLUDESTARTEND);
			TB.setMission( exDetails.findNext() );
			Extractor exName = new Extractor(bugDetails,p.getProp("bugNameStart"),p.getProp("bugNameEnd"),0,Extractor.EXCLUDESTARTEND);
			TB.setName( exName.findNext() );
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public class SpiderProperties extends Properties {
		SpiderProperties() {
			super();
			try {
				load(new FileInputStream(FileBase.getProgramDirectory()+"/spider.def"));
			} catch (Exception ex) {
				pref.log("Failed to load spider.def",ex);
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5504,"Could not load 'spider.def'"), FormBase.OKB)).execute();
			}
		}

		/**
		 * Gets an entry in spider.def by its key (tag)
		 * @param key The key which is attributed to a specific entry
		 * @return The value for the key
		 * @throws Exception When a key is requested which doesn't exist
		 */
		public String getProp(String key) throws Exception {
			String s=super.getProperty(key);
			if (s==null) {
				(new MessageBox(MyLocale.getMsg(5500,"Error"), MyLocale.getMsg(5497,"Error missing tag in spider.def") + ": "+key, FormBase.OKB)).execute();
				throw new Exception("Missing tag in spider.def: "+key);
			}
			return s;
		}

	}
}
