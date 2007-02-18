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
*	It uses a generic parse tree to parse the page and build a gpx file.
*/
public class SpiderGC{
	private static Preferences pref;
	private Profile profile;
	static String viewstate = new String();
	static String passwort = new String();
	static String cookieID = new String();
	static String cookieSession = new String();
	static double distance = 0;
	Regex inRex = new Regex();
	Vector cacheDB;
	Vector cachesToLoad = new Vector();
	Hashtable indexDB = new Hashtable();
	
	/**
	 * Method to login the user to gc.com
	 * It will request a password and use the alias defined in preferences
	 */
	public int login(){
		pref.logInit();
		//Access the page once to get a viewstate
		String start = new String();
		String doc = new String();
		//Get password
		InfoBox infB = new InfoBox("Password", "Enter password:", InfoBox.INPUT);
		int code = infB.execute();
		if(code == Form.IDOK){
			passwort = infB.getInput();
		} else return code;
		infB.close(0);
		infB = new InfoBox("Status", "Logging in...");
		infB.exec();
		try{
			pref.log("Fetching login page");
			start = fetch("http://www.geocaching.com/login/Default.aspx");
		}catch(Exception ex){
			Vm.debug("Could not fetch: gc.com start page");
		}
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
			pref.log("Logging in");
			doc = URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false);
			doc += "&" + URL.encodeURL("myUsername",false) +"="+ URL.encodeURL(pref.myAlias,false);
			doc += "&" + URL.encodeURL("myPassword",false) +"="+ URL.encodeURL(passwort,false);
			doc += "&" + URL.encodeURL("cookie",false) +"="+ URL.encodeURL("on",false);
			doc += "&" + URL.encodeURL("Button1",false) +"="+ URL.encodeURL("Login",false);
			start = fetch_post("http://www.geocaching.com/login/Default.aspx", doc, "/login/default.aspx");
			pref.log("Login successfull");
		}catch(Exception ex){
			Vm.debug("Could not login: gc.com start page");
			pref.log("Login failed.");
		}
		
		rex.search(start);
		viewstate = rex.stringMatched(1);
		rexCookieID.search(start);
		cookieID = rexCookieID.stringMatched(1);
		//Vm.debug(cookieID);
		rexCookieSession.search(start);
		cookieSession = rexCookieSession.stringMatched(1);
		//Vm.debug(cookieSession);
		infB.close(0);
		return Form.IDOK;
	}
	
	/**
	 * Method to spider a single cache.
	 * It assumes a login has already been performed!
	 */
	public void spiderSingle(int number){

		CacheHolder ch = (CacheHolder)cacheDB.get(number);
		if (ch.isAddiWpt()) return;  // No point spidering an addi waypoint, comes with parent
		Vm.showWait(true);
		String notes = new String();
		String start = new String();
		String origLong = new String();
		try{
			ch.readCache(profile.dataDir);
		}catch(IOException ioex){
			pref.log("Could not load " + ch.wayPoint + "file in spiderSingle");
		}
		notes = ch.CacheNotes;
		InfoBox infB = new InfoBox("Info", "Loading");
		infB.setInfo("Loading: " + ch.wayPoint);
		infB.show();
		String doc = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + ch.wayPoint +"&log=y";
		try{
			pref.log("Fetching: " + ch.wayPoint);
			start = fetch(doc);
		}catch(Exception ex){
			pref.log("Could not fetch " + ch.wayPoint);
			//Vm.debug("Couldn't get cache detail page");
		}
		ch.is_new = false;
		ch.is_update = false;
		ch.is_HTML = true;
		ch.is_available = true;
		ch.is_archived = false;
		//Vm.debug(ch.wayPoint);
		
		if(start.indexOf("This cache is temporarily unavailable") >= 0) ch.is_available = false;
		if(start.indexOf("This cache has been archived") >= 0) ch.is_archived = true;
		pref.log("Trying logs");
		int logsz = ch.CacheLogs.size();
		ch.CacheLogs = getLogs(start, ch);
		int z = 0;
		String loganal = new String();
		while(z < ch.CacheLogs.size() && z < 5){
			loganal = (String)ch.CacheLogs.get(z);
			if(loganal.indexOf("icon_sad")>0) {
				z++;
			}else break;
		}
		ch.noFindLogs = z;
		ch.is_log_update = false;
		if(ch.CacheLogs.size()>logsz) ch.is_log_update = true;
		pref.log("Found logs");
		ch.LatLon = getLatLon(start);
		ch.pos.set(ch.LatLon);
		//Vm.debug("LatLon: " + ch.LatLon);
		pref.log("Trying description");
		origLong = ch.LongDescription;
		ch.LongDescription = getLongDesc(start);
		if(!ch.LongDescription.equals(origLong)) ch.is_update = true;
		pref.log("Got description");
		pref.log("Getting cache name");
		ch.CacheName = SafeXML.cleanback(getName(start));
		pref.log("Got cache name");
		//Vm.debug("Name: " + ch.CacheName);
		pref.log("Trying owner");
		ch.CacheOwner = SafeXML.cleanback(getOwner(start));
		if(ch.CacheOwner.equals(pref.myAlias + " ")) ch.is_owned = true;
		pref.log("Got owner");
		//Vm.debug("Owner: " + ch.CacheOwner);
		pref.log("Trying date hidden");
		ch.DateHidden = getDateHidden(start);
		pref.log("Got date hidden");
		//Vm.debug("Hidden: " + ch.DateHidden);
		pref.log("Trying hints");
		ch.Hints = getHints(start);
		pref.log("Got hints");
		//Vm.debug("Hints: " + ch.Hints);
		//Vm.debug("Got the hints");
		pref.log("Trying size");
		ch.CacheSize = getSize(start);
		pref.log("Got size");
		//Vm.debug("Size: " + ch.CacheSize);
		pref.log("Trying difficulty");
		ch.hard = getDiff(start);
		pref.log("Got difficulty");
		//Vm.debug("Hard: " + ch.hard);
		pref.log("Trying terrain");
		ch.terrain = getTerr(start);
		pref.log("Got terrain");
		ch.Bugs = getBugs(start);
		if(ch.Bugs.length()>0) ch.has_bug = true; else ch.has_bug = false;
		//Vm.debug("Terr: " + ch.terrain);
		pref.log("Trying cache type");
		ch.type = getType(start);
		pref.log("Got cache type");
		//Vm.debug("Type: " + ch.type);
		pref.log("Trying images");
		getImages(start, ch);
		pref.log("Got images");
		//pref.log("Trying maps");
		//getMaps(ch);
		//pref.log("Got maps");
		pref.log("Getting additional waypoints");

		getAddWaypoints(start, ch);

		pref.log("Got additional waypoints");
		ch.CacheNotes = notes;
		ch.saveCacheDetails(profile.dataDir);
		
		cacheDB.set(number, ch);
		profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
		infB.close(0);
		Vm.showWait(false);
	}
	
	/**
	*	Method to start the spider for a search around the center coordinates
	*/
	public void doIt(){
		CWPoint origin = pref.curCentrePt; // No need to copy curCentrePt as it is only read and not written
		if (!origin.isValid()) {
			(new MessageBox("Error", "Coordinates for center must be set", MessageBox.OKB)).execute();
			return;
		}
		Vm.showWait(true);
		String start = new String();
		Regex rex = new Regex("name=\"__VIEWSTATE\" value=\"(.*)\" />");
		String doc = new String();
		
		int ok = login();
		if(ok == Form.IDCANCEL) {
			Vm.showWait(false);
			return;
		}
		
		OCXMLImporterScreen options = new OCXMLImporterScreen("Spider Options", OCXMLImporterScreen.INCLUDEFOUND);
		options.distanceInput.setText("");
		if (options.execute() == OCXMLImporterScreen.IDCANCEL) {Vm.showWait(false);	return; }
		String dist = options.distanceInput.getText();
		if (dist.length()== 0) return;
		distance = Convert.toDouble(dist);
		//boolean getMaps = options.mapsCheckBox.getState();
		boolean getFound = options.foundCheckBox.getState();
		Vm.debug("Get found? "+getFound);
		boolean getImages = options.imagesCheckBox.getState();
		options.close(0);
		
		
		InfoBox infB = new InfoBox("Status", "Fetching first page...");
		infB.exec();
		//Get first page
		try{
			String ln = new String("http://www.geocaching.com/seek/nearest.aspx?lat=" + origin.getLatDeg(CWPoint.DD) + "&lon=" +origin.getLonDeg(CWPoint.DD));
			if(getFound) ln = ln + "&f=1";
			start = fetch(ln);
			pref.log("First page: " + start);
		}catch(Exception ex){
			pref.log("Error fetching first list page");
			Vm.debug("Could not get list");
		}
		String dummy = new String();
		//String lineBlck = new String();
		int page_number = 4;		
		Regex rexLine = new Regex("<tr bgcolor=((?s).*?)</tr>");
		int found_on_page = 0;
		//Loop till maximum distance has been found or no more caches are in the list
		while(distance > 0){
			rex.search(start);
			viewstate = rex.stringMatched(1);
			//Vm.debug("In loop");
			dummy = getListBlock(start);
			/*
			try{
				  PrintWriter detfile = new PrintWriter(new BufferedWriter(new FileWriter("debug.txt")));
				  detfile.print(dummy);
				  detfile.close();
				} catch (Exception e) {
					Vm.debug("Problem opening details file");
				}
			*/
			rexLine.search(dummy);
			while(rexLine.didMatch()){
				//Vm.debug(getDist(rexLine.stringMatched(1)) + " / " +getWP(rexLine.stringMatched(1)));
				found_on_page++;
				if(getDist(rexLine.stringMatched(1)) <= distance){
					if(indexDB.get((String)getWP(rexLine.stringMatched(1))) == null){
						cachesToLoad.add(getWP(rexLine.stringMatched(1)));
					} else pref.log(getWP(rexLine.stringMatched(1))+" already in DB");
				} else distance = 0;
				rexLine.searchFrom(dummy, rexLine.matchedTo());
			}
			infB.setInfo("Found " + cachesToLoad.size() + " caches");
			if(found_on_page < 20) distance = 0;
			if(distance > 0){
				page_number++;
				if(page_number >= 15) page_number = 5;
				doc = URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false);
				doc += "&" + URL.encodeURL("lat",false) +"="+ URL.encodeURL(origin.getLatDeg(CWPoint.DD),false);
				doc += "&" + URL.encodeURL("lon",false) +"="+ URL.encodeURL(origin.getLonDeg(CWPoint.DD),false);
				doc += "&f=1";
				doc += "&" + URL.encodeURL("__EVENTTARGET",false) +"="+ URL.encodeURL("ResultsPager:_ctl"+page_number,false);
				doc += "&" + URL.encodeURL("__EVENTARGUMENT",false) +"="+ URL.encodeURL("",false);
				try{
					pref.log("Fetching next list page");
					start = fetch_post("http://www.geocaching.com/seek/nearest.aspx", doc, "/seek/nearest.aspx");
				}catch(Exception ex){
					Vm.debug("Couldn't get the next page");
				}
			}
			//Vm.debug("Distance is now: " + distance);
			found_on_page = 0;
		}
		pref.log("Found " + cachesToLoad.size() + " caches");
		infB.setInfo("Found " + cachesToLoad.size() + " caches");
		// Now ready to spider each cache
		String wpt = new String();
		CacheHolder ch;
		for(int i = 0; i<cachesToLoad.size(); i++){
			ch = new CacheHolder();
			wpt = (String)cachesToLoad.get(i);
			// Get only caches not already available in the DB
			if(searchWpt(wpt) == -1){
				infB.setInfo("Loading: " + wpt +"(" + (i+1) + " / " + cachesToLoad.size() + ")");
				doc = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + wpt +"&log=y";
				try{
					pref.log("Fetching: " + wpt);
					start = fetch(doc);
				}catch(Exception ex){
					pref.log("Could not fetch " + wpt);
					Vm.debug("Couldn't get cache detail page");
				}
				ch.is_new = true;
				ch.is_HTML = true;
				ch.is_available = true;
				ch.is_archived = false;
				ch.wayPoint = wpt;
				if(start.indexOf("This cache is temporarily unavailable") >= 0) ch.is_available = false;
				if(start.indexOf("This cache has been archived") >= 0) ch.is_archived = true;
				//Vm.debug(ch.wayPoint);
				try{
					pref.log("Trying logs");
					ch.CacheLogs = getLogs(start, ch);
					int z = 0;
					String loganal = new String();
					while(z < ch.CacheLogs.size() && z < 5){
						loganal = (String)ch.CacheLogs.get(z);
						if(loganal.indexOf("icon_sad")>0) {
							z++;
						}else break;
					}
					ch.noFindLogs = z;
					pref.log("Found logs");
					ch.LatLon = getLatLon(start);
					ch.pos.set(ch.LatLon); // Slow parse no problem
					//Vm.debug("LatLon: " + ch.LatLon);
					pref.log("Trying description");
					ch.LongDescription = getLongDesc(start);
					
					pref.log("Got description");
					pref.log("Getting cache name");
					ch.CacheName = SafeXML.cleanback(getName(start));
					pref.log("Got cache name");
					//Vm.debug("Name: " + ch.CacheName);
					pref.log("Trying owner");
					ch.CacheOwner = SafeXML.cleanback(getOwner(start));
					if(ch.CacheOwner.equals(pref.myAlias+" ")) ch.is_owned = true;
					pref.log("Got owner");
					//Vm.debug("Owner: " + ch.CacheOwner);
					pref.log("Trying date hidden");
					ch.DateHidden = getDateHidden(start);
					pref.log("Got date hidden");
					//Vm.debug("Hidden: " + ch.DateHidden);
					pref.log("Trying hints");
					ch.Hints = getHints(start);
					pref.log("Got hints");
					//Vm.debug("Hints: " + ch.Hints);
					//Vm.debug("Got the hints");
					pref.log("Trying size");
					ch.CacheSize = getSize(start);
					pref.log("Got size");
					//Vm.debug("Size: " + ch.CacheSize);
					pref.log("Trying difficulty");
					ch.hard = getDiff(start);
					pref.log("Got difficulty");
					//Vm.debug("Hard: " + ch.hard);
					pref.log("Trying terrain");
					ch.terrain = getTerr(start);
					pref.log("Got terrain");
					//Vm.debug("Terr: " + ch.terrain);
					pref.log("Trying cache type");
					ch.type = getType(start);
					pref.log("Got cache type");
					//Vm.debug("Type: " + ch.type);
					if(getImages){
						pref.log("Trying images");
						getImages(start, ch);
						pref.log("Got images");
					}
					ch.Bugs = getBugs(start);
					if(ch.Bugs.length()>0) ch.has_bug = true; else ch.has_bug = false;
					pref.log("Getting additional waypoints");
					getAddWaypoints(start, ch);
					pref.log("Got additional waypoints");
					ch.saveCacheDetails(profile.dataDir);
					cacheDB.add(ch);
					profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
				}catch(Exception ex){
					pref.log("There was an error in the last step:");
					pref.log("Cache was: " + wpt);
					pref.log("Error was: " + ex.toString());
				}finally{
					//just continue please!
					pref.log("Continuing with next cache.");
				}
			}
		}
		profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
		infB.close(0);
		Vm.showWait(false);
		/*
		try{
		  PrintWriter detfile = new PrintWriter(new BufferedWriter(new FileWriter("debug.txt")));
		  detfile.print(start);
		  detfile.close();
		} catch (Exception e) {
			Vm.debug("Problem opening details file");
		}
		
		*/
		
		
	}
	private int searchWpt(String wpt){
		Integer INTR = (Integer)indexDB.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}
	
	public String getBugs(String doc){	
		Extractor exBlock = new Extractor(doc, "Inventory","What is a Travel Bug?",0,Extractor.EXCLUDESTARTEND);
		String bugBlock = exBlock.findNext();
		Vm.debug("Bugblock: "+bugBlock);
		Extractor exBug = new Extractor(bugBlock, "'>", "</a></strong></td>",0,Extractor.EXCLUDESTARTEND);
		String bug = new String();
		String result = new String();
		while(exBug.endOfSearch() == false){
			bug= exBug.findNext();
			if(bug.length()>0) result = result + "<b>Name:</b> "+ bug + "<br><hr>";
			Vm.debug("B: " + bug);
			Vm.debug("End? " + exBug.endOfSearch());
		}
		return result;
	}
	
	public void getAddWaypoints(String doc, CacheHolder ch){
		Extractor exWayBlock = new Extractor(doc, "<strong>Additional Waypoints</strong><br>", "</table>", 0, false);
		String wayBlock = new String();
		String rowBlock = new String();
		wayBlock = exWayBlock.findNext();
		Regex nameRex = new Regex("&RefDS=1\">(.*)</a>");
		Regex koordRex = new Regex("align=\"left\">([NSns] [0-9]{1,2}..[0-9]{1,2}.[0-9]{1,3} [EWew] [0-9]{1,3}..[0-9]{1,2}.[0-9]{1,3})</td>");
		Regex descRex = new Regex("colspan=\"4\">(.*)</td>");
		Regex typeRex = new Regex("</a> \\((.*)\\)</td>");
		int counter = 0;
		if(exWayBlock.endOfSearch() == false && wayBlock.indexOf("No additional waypoints to display.")<0){
			Extractor exRowBlock = new Extractor(wayBlock, "<tr", "</tr>", 0, false);
			rowBlock = exRowBlock.findNext();
			rowBlock = exRowBlock.findNext();
			while(exRowBlock.endOfSearch()==false){
				CacheHolder cx = new CacheHolder();
				
				nameRex.search(rowBlock);
				koordRex.search(rowBlock);
				typeRex.search(rowBlock);
				cx.CacheName = nameRex.stringMatched(1);
				if(koordRex.didMatch()) cx.LatLon = koordRex.stringMatched(1); 
				else cx.LatLon = "---"; 
				cx.pos.set(cx.LatLon);
				if(typeRex.didMatch()) cx.type = CacheType.typeText2Number("Waypoint|"+typeRex.stringMatched(1));
				rowBlock = exRowBlock.findNext();
				descRex.search(rowBlock);
				cx.wayPoint = MyLocale.formatLong(counter, "00") + ch.wayPoint.substring(2);
				counter++;
				cx.LongDescription = descRex.stringMatched(1); 
				//Vm.debug(descRex.stringMatched(1));
				int idx=profile.getCacheIndex(cx.wayPoint);
				cx.is_found = ch.is_found;
				if (idx<0)
					cacheDB.add(cx); // Addi is not in database
				else if (((CacheHolder) cacheDB.get(idx)).is_Checked && // Only re-spider existing addi waypoints that are ticked
						!((CacheHolder) cacheDB.get(idx)).is_filtered) // and are visible (i.e.  not filtered)
					((CacheHolder) cacheDB.get(idx)).update(cx);
				cx.saveCacheDetails(profile.dataDir);
				
				rowBlock = exRowBlock.findNext();
			}
		}
	}
	
	public void getImages(String doc, CacheHolder ch){
		int imgCounter = 0;
		String imgName = new String();
		String imgType = new String();
		String imgUrl = new String();
		//In the long description
		String longDesc = new String();
		longDesc = getLongDesc(doc);
		longDesc = STRreplace.replace(longDesc, "img", "IMG");
		longDesc = STRreplace.replace(longDesc, "src", "SRC");
		longDesc = STRreplace.replace(longDesc, "'", "\"");
		//longDesc = STRreplace.replace(longDesc, "SRC =", "SRC=");
		//longDesc = STRreplace.replace(longDesc, "SRC= \"", "SRC=\"");
		//longDesc = STRreplace.replace(longDesc, "\n", " ");
		//longDesc = STRreplace.replace(longDesc, " ", "");
		Extractor exImgBlock = new Extractor(longDesc, "<IMG", ">", 0, false);
		//Vm.debug("In getImages: Have longDesc" + longDesc);
		String tst = new String();
		tst = exImgBlock.findNext();
		//Vm.debug("Test: \n" + tst);
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while(exImgBlock.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				try{
					imgType = imgUrl.substring(imgUrl.lastIndexOf("."));
					if(!imgType.equals("com") && !imgType.equals("php") && !imgType.equals("exe")){
						imgName = ch.wayPoint + "_" + Convert.toString(imgCounter);
						pref.log("Loading image: " + imgUrl);
						spiderImage(imgUrl, imgName+imgType);
						imgCounter++;
						ch.Images.add(imgName+imgType);
						ch.ImagesText.add(imgName);
					}
				} catch (IndexOutOfBoundsException e) { 
					Vm.debug("IndexOutOfBoundsException not in image span"+e.toString()+"imgURL:"+imgUrl);
					pref.log("Problem loading image");
				}
				}
			exImgSrc.setSource(exImgBlock.findNext());
		}

		//In the image span

		Extractor spanBlock = new Extractor(doc, "<span id=\"Images\"", "</span>", 0 , true);
		tst = spanBlock.findNext();
		Extractor exImgName = new Extractor(tst, "style='text-decoration: underline;'>", "</A><br>", 0 , true);
		exImgSrc = new Extractor(tst, "&nbsp;<A HREF='http://", "' target=", 0, true);
		while(exImgSrc.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			//Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				try{
					imgType = imgUrl.substring(imgUrl.lastIndexOf("."));
					if(!imgType.equals("com") && !imgType.equals("php") && !imgType.equals("exe")){
						imgName = ch.wayPoint + "_" + Convert.toString(imgCounter);
						spiderImage(imgUrl, imgName+imgType);
						imgCounter++;
						ch.Images.add(imgName+imgType);
						ch.ImagesText.add(exImgName.findNext());
					}
				} catch (IndexOutOfBoundsException e) { 
					Vm.debug("IndexOutOfBoundsException in image span"+e.toString()+"imgURL:"+imgUrl); 
				}
			}
		}
	}
	
	private void spiderImage(String quelle, String target){
		HttpConnection connImg;
		Socket sockImg;
		//InputStream is;
		FileOutputStream fos;
		//int bytes_read;
		//byte[] buffer = new byte[9000];
		ByteArray daten;
		String datei = new String();
		datei = profile.dataDir + target;
		if(pref.myproxy.length()>0){
			connImg = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), quelle);
		}else{
			connImg = new HttpConnection(quelle);
		}
		connImg.setRequestorProperty("Connection", "close");
		try{
			pref.log("Trying to fetch image from: " + quelle);
			sockImg = connImg.connect();
			daten = connImg.readData(connImg.connect());
			fos = new FileOutputStream(new File(datei));
			fos.write(daten.toBytes());
			fos.close();
			sockImg.close();
		} catch (UnknownHostException e) {
			pref.log("Host not there...");
			//Vm.debug("Host not there...");
		}catch(IOException ioex){
			pref.log("File not found!");
			//Vm.debug("File not found!");
		} catch (Exception ex){
			pref.log("Some other problem while fetching image");
			//Vm.debug("Some kind of problem!");
		} finally {
			//Continue with the spider
		}
	}		
	
	private String getType(String doc){
		inRex = new Regex("<img src=\"../images/WptTypes/(.*?)\\.gif");
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}
	
	private String getDiff(String doc){
		inRex = new Regex("<span id=\"Difficulty\">.*?alt=\"(.*?) out of");
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}
	
	private String getTerr(String doc){
		inRex = new Regex("<span id=\"Terrain\">.*?alt=\"(.*?) out of");
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "";
	}
	
	private String getSize(String doc){
		inRex = new Regex("This is a <strong>((?s).*?)</strong> cache");
		inRex.search(doc);
		if(inRex.didMatch()) return inRex.stringMatched(1);
		else return "None";
	}
	
	private String getHints(String doc){
		inRex = new Regex("<span id=\"Hints\" class=\"displayMe\">((?s).*?)</span>");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	private String getDateHidden(String doc){
		inRex = new Regex("<span id=\"DateHidden\">((?s).*?)</span>");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	private String getLatLon(String doc){
		inRex = new Regex("<span id=\"LatLon\"><.*?>((?s).*?)</STRONG>");
		inRex.search(doc);
		
		//Vm.debug("LatLon: " + inRex.stringMatched(1));
		
		return inRex.stringMatched(1);
	}
	
	private String getOwner(String doc){
		inRex = new Regex("<span id=\"CacheOwner\".*?by ((?s).*?)\\[<A HREF=");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	private String getName(String doc){
		inRex = new Regex("<span id=\"CacheName\">((?s).*?)</span>");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	private String getLongDesc(String doc){
		String res = new String();
		inRex = new Regex("<span id=\"ShortDescription\">((?s).*?)</span>");
		Regex rex2 = new Regex("<span id=\"LongDescription\">((?s).*?)<strong>Additional Hints");
		inRex.search(doc);
		rex2.search(doc);
		res = inRex.stringMatched(1) + "<br>";
		res += rex2.stringMatched(1); 
		return SafeXML.cleanback(res);
	}
	
	private String getListBlock(String doc){
		inRex = new Regex("<table id=\"dlResults\"((?s).*?)</table>");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	
	private String getWP(String doc){
		inRex = new Regex("</a> \\((.*?)\\)<br>");
		inRex.search(doc);
		return inRex.stringMatched(1);
	}
	private double getDist(String doc){
		inRex = new Regex("<br />(.*?)(km|mi)</td>");
		inRex.search(doc);
		if(doc.indexOf("Here") > 0) return(0);
		if(pref.digSeparator.equals(",")) return Convert.toDouble(inRex.stringMatched(1).replace('.',','));
		return Convert.toDouble(inRex.stringMatched(1));
	}
	
	private Vector getLogs(String doc, CacheHolder ch){
		String icon = new String();
		String name = new String();
		Vector reslts = new Vector();
		Regex block = new Regex("<span id=\"CacheLogs\">((?s).*?)</span>");
		block.search(doc);
		doc = block.stringMatched(1);
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
		String singleLog = new String();
		Extractor exSingleLog = new Extractor(doc, "<STRONG>", "[<A href=", 0, false); // maybe here is some change neccessary because findnext now gives the whole endstring back??? 
		singleLog = exSingleLog.findNext();
		Extractor exIcon = new Extractor(singleLog, "http://www.geocaching.com/images/icons/", "' align='abs", 0, true);
		Extractor exNameTemp = new Extractor(singleLog, "<A HREF=\"", "/A>", 0 , true);
		String nameTemp = new String();
		nameTemp = exNameTemp.findNext();
		Extractor exName = new Extractor(nameTemp, ">", "<", 0 , true);
		Extractor exDate = new Extractor(singleLog, "align='absmiddle'>&nbsp;", " by <", 0 , true);
		Extractor exLog = new Extractor(singleLog, "found)", "<br>[", 0, true);
		//Vm.debug("Log Block: " + singleLog);
		while(exSingleLog.endOfSearch() == false){
			//Vm.debug("--------------------------------------------");
			//Vm.debug("Log Block: " + singleLog);
			//Vm.debug("Icon: "+exIcon.findNext());
			//Vm.debug(exName.findNext());
			//Vm.debug(exDate.findNext());
			//Vm.debug(exLog.findNext());
			//Vm.debug("--------------------------------------------");
			icon = exIcon.findNext();
			name = exName.findNext();
			if((icon.equals("icon_smile.gif") || icon.equals("icon_camera.gif")) && name.equals(pref.myAlias)) {
				ch.is_found = true;
				ch.CacheStatus = MyLocale.getMsg(318,"Found");
			}
			reslts.add("<img src='"+ icon +"'>&nbsp;" + exDate.findNext()+ " " + name + exLog.findNext());
			
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
	
	public SpiderGC(Preferences prf, Profile profile){
		this.profile=profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		indexDB = new Hashtable(cacheDB.size());
		CacheHolder ch;
		//index the database for faster searching!
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			indexDB.put((String)ch.wayPoint, new Integer(i));
			ch.is_new = false;
			//cacheDB.set(i, ch);
		}
	}
	
	/**
	*	Performs an initial fetch to a given address. In this case
	*	it will be a gc.com address. This method is used to obtain
	*	the result of a search for caches screen.
	*/
	private static String fetch(String address) throws IOException
	   	{
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
			CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			////Vm.debug(c_data.toString());
			sock.close();
			return c_data.toString();
		}
	
	/**
	*	After a fetch to gc.com the next fetches have to use the post method.
	*	This method does exactly that. Actually this method is generic in the sense
	*	that it can be used to post to a URL using http post.
	*/
	private static String fetch_post(String address, String document, String path) throws IOException 
	   	{
			
			//String line = new String();
			String totline = new String();
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