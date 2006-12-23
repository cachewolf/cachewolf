package CacheWolf;
import ewe.net.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.ui.*;

/**
*	The class to spider:<br>
*	a) the cache list<br>
*	b) the cache details<br><br>
*	This class still needs thorough documentation<br>
*	Class ID=800
*/

/**
*	Spider Travelbugs:
*	Das hier für welche abgelegt sind (auf Cache Detailseite)
/////////////////////
*	<span id="lnkTravelBugs"><tr><td VALIGN="TOP" ALIGN="LEFT"><img SRC="../images/wpttypes/21.gif" WIDTH="22" HEIGHT="30"></td><td VALIGN="TOP" ALIGN="LEFT" COLSPAN="2"><font face="Verdana" size="2"><FONT SIZE="1">Travel Bugs have been seen in this cache -</FONT><br>
*<A HREF="../track/details.aspx?guid=8e52e5d8-6922-40f4-9b09-5b521297468e">BlackLog</A><br>
*<A HREF="../track/details.aspx?guid=bc5f6513-ad4c-41b1-b299-759cf4b78d84">Ostrov Hotel</A><br>
*<A HREF="../track/details.aspx?guid=ec0a978e-3491-4315-8ab0-7e2c417b0d84">TB - Alan Stewart </A>
*<br><A HREF="../track/details.aspx?guid=bdd919d9-73a1-47b7-8ba8-5f0584e7f33e">Theodore the Traveling Toad</A>
*<br><A HREF="../track/details.aspx?guid=092d0214-a601-407e-84cb-cd514598106c">Forgetful Jones - Denkedran Jost</A><br>
*<br><FONT SIZE=1><A HREF="../track/">What is a Travel Bug?</A><br><a href="../track/search.aspx?wid=a4ff6781-84fa-466c-ba0f-028128e06a7a">View Bug History</a></font></font></td></tr></span>
/////////////////////
*
*	Bild von dem Bug:
*       <img id="BugDetail_BugImage" src="http://img.groundspeak.com/track/display/35c891d1-b4e4-4f85-b88e-176bb5b13ceb.jpg" alt="Our 2nd TravelBug - BlackLog" align="Right" border="0" />
*
*	Ziel des Bugs:
*	<span id="BugDetail_BugGoal">
*	...
*	</span>
*	und!
*	<span id="BugDetail_BugDetails">
*	...
*	</span>
*
*/
public class Spider extends TaskObject{
	public static int SPIDERMULTI = 0;
	public static int SPIDERNEAREST = 1;
	public static int SPIDERLOC = 2;
	public static String proxy;
	public static String port;
	private int SpiderType = -1;
	private Vector caches_identified = new Vector();
	private String caches_available = new String();
	Vector cacheDB;
	Preferences pref = new Preferences();
	ProgressBarForm pbf = new ProgressBarForm();
	MessageArea msgA;
	Vector data;
	String distance;
	
	protected void doRun(){
		switch(SpiderType){
			case 0: SpiderMulti(data, distance);
				break;
			case 1: SpiderNearest(distance);
				break;
			case 2: SpiderLOC(data);
				break;
		}
	}
	
	public void setUp(Vector dta){
		data = dta;
	}
	public void setUp(String dst){
		distance = dst;
	}
	public void setUp(Vector vct, String dist){
		data = vct;
		distance = dist;
	}
	/**
	*	Initializes a spider. It also identifies which caches
	*	are already available in the database.
	*/
	public Spider(Vector DB, Preferences p, MessageArea msg, int SpT){
		msgA = msg;
		SpiderType = SpT;
		cacheDB = DB;
		pref = p;
		proxy = pref.myproxy;
		port = pref.myproxyport;
		CacheHolder ch;
		for(int i = 0; i<DB.size(); i++){
			ch = (CacheHolder)DB.get(i);
			caches_available = caches_available + ";" + ch.wayPoint;
		}
		//Vm.debug("Setup caches avail: " + caches_available);
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
			if(proxy.length() > 0){
				conn = new HttpConnection(proxy, Convert.parseInt(port), address);
				//Vm.debug(address);
			} else {
				conn = new HttpConnection(address);
			}
			conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			Socket sock = conn.connect();
			ByteArray daten = conn.readData(sock);
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
	private static String fetch_post(String address, String document) throws IOException 
	   	{
			
			String line = new String();
			String totline = new String();
			if(proxy.length()==0){
				try {
					// Create a socket to the host
					String hostname = "www.geocaching.com";
					int port = 80;
					//InetAddress addr = InetAddress.getByName(hostname);
					Socket socket = new Socket(hostname, port);
				
					// Send header
					String path = "/seek/nearest.aspx";
					BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
					BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					wr.write("POST "+path+" HTTP/1.1\r\n");
					wr.write("Host: www.geocaching.com\r\n");
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
				} catch (Exception e) {
				}
			} else {
				HttpConnection conn;
				conn = new HttpConnection(proxy, Convert.parseInt(port), address);
				JavaUtf8Codec codec = new JavaUtf8Codec();
				conn.documentIsEncoded = true;
				//Vm.debug(address + " / " + document);
				//document = document + "\r\n";
				//conn.setPostData(document.toCharArray());
				conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
				conn.setPostData(codec.encodeText(document.toCharArray(),0,document.length(),true,null));
				conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
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
		
	/**
	*	This mehtod is to be called after LOC files from gc.com
	*	have been analysed. It spiders each identified cache step by step.
	*/
	public void SpiderLOC(Vector locs){
		for(int i = 0; i<locs.size();i++){
			caches_identified.add((String)locs.get(i));
		}
		try{
			doSpider(Convert.toString(locs.size()), false);
		}catch (Exception e){
			//Vm.debug("Problem with spider");
		}
	}
	
	/**
	*	This method spiders all caches in a radius of PERIP. It
	*	converts to km to identify those caches outside of PERIP.
	*	Center point is what is currently set in preferences.
	*/
	public void SpiderNearest(String perip){
			//ParseLatLon pll;
			//pbf.display(MyLocale.getMsg(800,"Collecting list"), MyLocale.getMsg(801,"collecting..."), pForm);
			if(msgA != null)  msgA.setText(MyLocale.getMsg(801,"collecting..."));
			double periInt = Convert.parseDouble(perip);
			double distDbl;
			int cacheCounter = 0;
			int page_number = 5; //gc.com starts with 5! for page 2...
			// initial fetch
			//String url_for_next_page = new String();
			String sourcePage = new String();
			String listString = new String();
			String v_state = new String();
			//String ev_argument = new String(); //not used?
			String ev_target = new String();
			String cache = new String();
			String dist = new String();
			//String document = new String();
			String NS = pref.curCentrePt.getNSLetter().equals("N") ? "1" : "-1";
			String WE = pref.curCentrePt.getEWLetter().equals("E") ? "1" : "-1";
			String searchStr = 
				        "lat_ns="     + NS +
						"&lat_h="     + pref.curCentrePt.getLatDeg(CWPoint.DMM) +
						"&lat_mmss="  + pref.curCentrePt.getLatMin(CWPoint.DMM) +
						"&long_ew="   + WE +
						"&long_h="    + pref.curCentrePt.getLonDeg(CWPoint.DMM) +
						"&long_mmss=" + pref.curCentrePt.getLatMin(CWPoint.DMM);
			boolean more = true;
			Extractor sourceEx, listEx, listExDist, listExCache;
			try{
				sourcePage = fetch("http://www.geocaching.com/seek/nearest.aspx?"+searchStr);
			} catch (Exception ex){
				ex.printStackTrace();
				//Vm.debug(ex.toString());
			}
			//do we have a valid page or maybe there are no caches in required distance?
			if(sourcePage.indexOf("Sorry, no results were found for this search")==-1){
				do{
					// analyse page
					// first get relevant section
					listEx = new Extractor(sourcePage, "Last Found", "Distances measured", 0, true);
					listString = listEx.findNext();
					//Vm.debug(listString);
					// now get the details within the section
					listExDist = new Extractor(listString, "<br />", "</td>", 0, true);
					listExCache = new Extractor(listString, "</a> (", ")<br>", 0, true);
					cache = listExCache.findNext();
					dist = listExDist.findNext();
					while (listExDist.endOfSearch() == false) {
						dist = dist.substring(0, dist.length()-2);
						if(dist.indexOf(".") == -1) dist = "0." + dist;
						if(pref.digSeparator.equals(",")) dist = dist.replace('.',',');
						distDbl = 0;
						try{
							distDbl = Convert.parseDouble(dist);
							distDbl = distDbl * 1.6093;
						} catch (NumberFormatException nfe){
							Vm.debug("Number format exception: " +cache);
						}
						if( distDbl <= periInt) {
							//add to list to spider
							caches_identified.add(cache);
							cacheCounter++;
							//pbf.display(MyLocale.getMsg(800,"Collecting list"), Convert.toString(cacheCounter) + MyLocale.getMsg(802," caches identified: collecting more..."), pForm);
							if(msgA != null)  msgA.setText(Convert.toString(cacheCounter) + MyLocale.getMsg(802," caches identified: collecting more..."));
							//more = true;
							//Vm.debug(dist +" : " +cache);
						} else {
							// reached edge... stop getting list pages
							more = false;
						}
						cache = listExCache.findNext();
						dist = listExDist.findNext();
					}
					// get next page
					if(more == true){
						sourceEx = new Extractor(sourcePage, "<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"", "\" />",0, true);
						v_state = sourceEx.findNext();
						ev_target = "ResultsPager:_ctl" + Convert.toString(page_number);
						page_number++;
						if(page_number >= 15) page_number = 5;
						String data = new String();
						data = URL.encodeURL("lat_ns",false) + "=" + URL.encodeURL(NS,false);
						data += "&" + URL.encodeURL("lat_h",false) +"="+ URL.encodeURL(pref.curCentrePt.getLatDeg(CWPoint.DMM),false);
						data += "&" + URL.encodeURL("lat_mmss",false) +"="+ URL.encodeURL(pref.curCentrePt.getLatMin(CWPoint.DMM),false);
						data += "&" + URL.encodeURL("long_ew",false) +"="+ URL.encodeURL(WE,false);
						data += "&" + URL.encodeURL("long_h",false) +"="+ URL.encodeURL(pref.curCentrePt.getLonDeg(CWPoint.DMM),false);
						data += "&" + URL.encodeURL("long_mmss",false) +"="+ URL.encodeURL(pref.curCentrePt.getLonMin(CWPoint.DMM),false);
						data += "&" + URL.encodeURL("__EVENTTARGET",false) +"="+ URL.encodeURL(ev_target,false);
						data += "&" + URL.encodeURL("__EVENTARGUMENT",false) +"="+ URL.encodeURL("",false);
						data += "&" + URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(v_state,false);
						//Vm.debug("This is viewstate: " + v_state);
						//document = "origin_lat=" +URL.encodeURL("48",false) +"&origin_lon=" + URL.encodeURL("11",false) +"&submit3=" + URL.encodeURL("Submit",false) +	"&__EVENTTARGET="+URL.encodeURL(ev_target,false) +"&__EVENTARGUMENT=&__VIEWSTATE="+URL.encodeURL(v_state,false);
						//document = "origin_lat=48&origin_lon=11&submit3=Submit&__EVENTTARGET="+ev_target +"&__EVENTARGUMENT=&__VIEWSTATE="+v_state;
						//document = URL.encodeURL("lat_ns="+NS+"&lat_h="+myPref.mylgDeg+ "&lat_mmss=" +myPref.mylgMin+ "&long_ew=" + WE+ "&long_h=" + myPref.mybrDeg+ "&long_mmss=" + myPref.mybrMin +"&submit3=Submit&__EVENTTARGET="+ev_target +"&__EVENTARGUMENT=&__VIEWSTATE="+v_state,false);
						//document = "?lat_ns=1&amp;lat_h=48&amp;lat_mmss=07.915&amp;long_ew=1&amp;long_h=11&amp;long_mmss=35.597";
						//document = "lat_ns="+URL.encodeURL(NS,false)+"&lat_h="+URL.encodeURL(myPref.mylgDeg,false) + "&lat_mmss=" +URL.encodeURL(myPref.mylgMin,false) + "&long_ew=" + URL.encodeURL(WE,false)+ "&long_h=" + URL.encodeURL(myPref.mybrDeg,false) + "&long_mmss=" + URL.encodeURL(myPref.mybrMin,false) +"&submit3=" + URL.encodeURL("Submit",false) +	"&__EVENTTARGET="+URL.encodeURL(ev_target,false) +"&__EVENTARGUMENT="+URL.encodeURL("",false) +	"&__VIEWSTATE="+URL.encodeURL(v_state,false);
						//document = "__EVENTTARGET="+URL.encodeURL(ev_target,false) +"&__EVENTARGUMENT="+URL.encodeURL("",false) +	"&__VIEWSTATE="+URL.encodeURL(v_state,false);
						//document = "__VIEWSTATE="+URL.encodeURL(v_state,false)+"&__EVENTTARGET="+ev_target;
						try{
							sourcePage = "";
							sourcePage = fetch_post("http://www.geocaching.com/seek/nearest.aspx", data);
							//sourcePage = fetch_post("http://localhost/post.php", data);
							//more = false;
							//Vm.debug(sourcePage);
						} catch (Exception ex){
							ex.printStackTrace();
							////Vm.debug(ex.toString());
						}
					}
					//Vm.debug("running....");
					if(shouldStop == true) break;
				} while (more == true);
			}//if sourcepage valid
			//pbf.display(MyLocale.getMsg(800,"Collecting list"), Convert.toString(cacheCounter) + MyLocale.getMsg(803," Done!"), pForm);
			if(msgA != null) msgA.setText(Convert.toString(cacheCounter) + MyLocale.getMsg(803," Done!"));
			if(shouldStop == false){
				try{
					doSpider(Convert.toString(cacheCounter), true);
				}catch(IOException ex){
					//Vm.debug("Problem with Spider");
				}
			}
		}
		
		/**
		*	Call this method to find out if a cache has already been
		*	spidered.
		*/
		/*skg 20061223: Not used
		private boolean isAlreadyFetched(String wp){
			if(caches_available.indexOf(wp) == -1) return false;
			else return true;
		}
		*/
		/**
		*	Method that performs a multi point spider.
		*	User will choose this option when identification of caches
		*	along a route is required.
		*/
		public void SpiderMulti(Vector wpts, String dist){
			//first save current preference settings
			CWPoint SavedCentre=new CWPoint(pref.curCentrePt);
			
			//loop through the vector and perform a spidernearest
			for(int i = 0; i<wpts.size(); i++){
				pref.curCentrePt.set((String)wpts.get(i)); // Can use Regex here as time is not of concern
				if(shouldStop == true) break;
				//do the spider
				SpiderNearest(dist);
			}
			
			//save back preferences
			pref.curCentrePt.set(SavedCentre);
		}
		
		/**
		*	Method that actually gathers details on a cache.
		*	The string total is just for information purposes.
		*	This method actually loops through the vector
		*	"caches_identified", that holds caches that are not
		*	available in the database.
		*/
		private void doSpider(String total, boolean update_existing) throws IOException {
			MapLoader mpl;
			ParseLatLon pll;
			Extractor cacheEx;
			Extractor tempEx, tempEx2;
			CacheHolder ch;
			CacheReaderWriter crw = new CacheReaderWriter();
			int doneCounter = 1;
			String wp = new String();
			String document = new String();
			String imgLoc = new String();
			String dummy = new String();
			String dummy2 = new String();
			String dummy3 = new String();
			String dummy4 = new String();
			String dummy5 = new String();
			String dummy6 = new String();
			boolean found = false;
			//time to add existing caches to the identified list, ommitting double entries
			if(shouldStop == false){
				if(update_existing == true){
					for(int x = 0; x < cacheDB.size(); x++){
						found = false;
						ch = new CacheHolder();
						ch = (CacheHolder)cacheDB.get(x);
						for(int y = 0; y<caches_identified.size(); y++){
							dummy = new String();
							dummy = (String)caches_identified.get(y);
							dummy = dummy.trim();
							if(ch.wayPoint.toUpperCase().equals(dummy.toUpperCase()) == true) found = true;
						}
						if(found == false && ch.is_found == false) caches_identified.add(ch.wayPoint);
					}
					total = Convert.toString(caches_identified.size());
				}
			}
			for(int i = 0; i<caches_identified.size(); i++){
				ch = new CacheHolder();
				wp = (String)caches_identified.get(i);
				wp = wp.trim();
				//make sure we do not get doubles!
				//Vm.debug(caches_available);
				
				//Vm.debug("Do not have going in for: " + wp);
				//pbf.display(MyLocale.getMsg(804,"Collecting Cache Details"), MyLocale.getMsg(805,"Fetching: ") + wp + " (" +Convert.toString(doneCounter)+" of "+total+")", pForm);
				if(msgA != null) msgA.setText(MyLocale.getMsg(805,"Fetching: ") + wp + " (" +Convert.toString(doneCounter)+" of "+total+")");
				try{
					document = fetch("http://www.geocaching.com/seek/cache_details.aspx?wp="+wp+"&Submit6=Find&log=y");
				}catch(Exception ex){
					//Vm.debug("Problem with page: "+wp);
				}
				//if(caches_available.indexOf(wp) == -1){	
				cacheEx = new Extractor(document, "<span id=\"CacheName\">", "</span>",0,true);
				ch.CacheName = cacheEx.findNext();
				cacheEx = new Extractor(document, "<span id=\"ErrorText\">", "</span>",0,true);
				dummy = cacheEx.findNext();
				if(dummy.indexOf("has been archived")>=0) ch.is_archived = true;
				if(dummy.indexOf("temporarily unavailable")>=0) ch.is_available = false;
				
				cacheEx = new Extractor(document, "<span id=\"CacheOwner\"><b><font size=\"2\">by ", "[<A",0,true);
				ch.CacheOwner = cacheEx.findNext();
				//Vm.debug(ch.CacheOwner);
				if(ch.CacheOwner.indexOf(pref.myAlias)>=0) ch.is_owned = true;
				if(ch.is_owned == true) ch.CacheStatus = "Owner";
				cacheEx = new Extractor(document, "<span id=\"LatLon\"><font size=\"3\">", "</STRONG><br /><STRONG></font></span>",0,true);
				ch.LatLon = cacheEx.findNext();
				ch.LatLon = replace(ch.LatLon, "°", "&#176;");
				cacheEx = new Extractor(document, "<span id=\"DateHidden\">", "</span>",0,true);
				ch.DateHidden = cacheEx.findNext();
				ch.wayPoint = wp;
				cacheEx = new Extractor(document, "<span id=\"ShortDescription\">", "</span>",0,true);
				ch.LongDescription = cacheEx.findNext();
				cacheEx = new Extractor(document, "<span id=\"LongDescription\">", "<STRONG>Additional Hints&nbsp;",0,true);
				ch.LongDescription += cacheEx.findNext();
				cacheEx = new Extractor(document, "<span id=\"Hints\">", "</span>",0,true);
				ch.Hints = cacheEx.findNext();
				cacheEx = new Extractor(document, "<img src=\"../images/WptTypes/", ".gif\"",0,true);
				ch.type = cacheEx.findNext();
				if(ch.type.equals("11") == false){
					cacheEx = new Extractor(document, "This is a <strong>", "</strong> cache.",0,true);
					ch.CacheSize = cacheEx.findNext();
				}
				
				cacheEx = new Extractor(document, "<span id=\"CacheLogs\">", "</span>",0,true);
				dummy = cacheEx.findNext();
				//is found?
				if(pref.myAlias.length()>0){
					tempEx = new Extractor(dummy, "icon_smile.gif", "</A>",0,true);
					dummy3 = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						if(dummy3.indexOf(pref.myAlias)>0) ch.is_found = true;
						dummy3 = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "icon_camera.gif", "</A>",0,true);
					dummy3 = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						if(dummy3.indexOf(pref.myAlias)>0) ch.is_found = true;
						dummy3 = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "icon_attended.gif", "</A>",0,true);
					dummy3 = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						if(dummy3.indexOf(pref.myAlias)>0) ch.is_found = true;
						dummy3 = tempEx.findNext();
					}
				}
				if(ch.is_found == true) ch.CacheStatus = "Found";
				
				cacheEx = new Extractor(dummy, "<STRONG><IMG SRC='http:", "</font></td></tr><tr><td VALIGN='TOP' ALIGN='LEFT'><font face='Verdana' size='2'>",0,true);
				dummy = cacheEx.findNext();
				//int maxLogs = myPref.nLogs;
				
				while(cacheEx.endOfSearch() == false){
					//maxLogs--;
					//Vm.debug(dummy);
					//extract any images if they exist
					tempEx = new Extractor(dummy, "<A HREF='./log.aspx?IID=", "&", 0 ,true);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						ch.LogImages.add(imgLoc);
						//Vm.debug(imgLoc);
						imgLoc = tempEx.findNext();
					}
					
					//Erase stuff in [...]
					tempEx = new Extractor(dummy, "[", "]", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					
					//rename image link to [[name_of_image]]
					tempEx = new Extractor(dummy, "<A HREF='./log.aspx?IID=", "</A>", 0 ,false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						tempEx2 = new Extractor(imgLoc, "'>", "</", 0 ,true);
						dummy2 = tempEx2.findNext();
						dummy2 = dummy2.substring(0, dummy2.length()-1);
						ch.LogImagesText.add(dummy2);
						dummy = replace(dummy, imgLoc, " [[ "+dummy2+" ]] ");
						imgLoc = tempEx.findNext();
					}
					//repoint log icons
					dummy = replace(dummy, "<IMG SRC='../images/icon_camera.gif' align=left>", "<img src='icon_camera.gif'>");
					dummy = replace(dummy, "<IMG SRC='../images/icon_smile.gif' align=left>", "<img src='icon_smile.gif'>");
					dummy = replace(dummy, "<IMG SRC='../images/icon_attended.gif' align=left>", "<img src='icon_attended.gif'>");
					
					dummy = replace(dummy, "//www.geocaching.com/images/icons/", "<img src='");
					
					//Erase HTML stuff in logs
					//table tags
					tempEx = new Extractor(dummy, "<table", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "<TABLE", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "<td", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "<TD", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					dummy = replace(dummy, "<tr>", "");
					dummy = replace(dummy, "<TR>", "");
					dummy = replace(dummy, "</tr>", "");
					dummy = replace(dummy, "</TR>", "");
					dummy = replace(dummy, "</td>", "");
					dummy = replace(dummy, "</TD>", "");
					dummy = replace(dummy, "</table>", "");
					dummy = replace(dummy, "</TABLE>", "");
					// a href
					tempEx = new Extractor(dummy, "<A HREF=", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					tempEx = new Extractor(dummy, "<A NAME=", ">", 0, false);
					imgLoc = new String();
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						dummy = replace(dummy, imgLoc, "");
						imgLoc = tempEx.findNext();
					}
					dummy = replace(dummy, "</A>", "");
					//End of erase html stuff in logs
					
					ch.CacheLogs.add(dummy);
					dummy = cacheEx.findNext();
				} //while
				
				
				// Search for Travelbugs
				
				cacheEx = new Extractor(document, "<span id=\"lnkTravelBugs\">","</span>",0,true);
				dummy = cacheEx.findNext();
				if(dummy.length()>0){
					cacheEx = new Extractor(dummy, "<A HREF","</A>",0,true);
					dummy2 = cacheEx.findNext();
					while(cacheEx.endOfSearch() == false){
						tempEx2 = new Extractor(dummy2, "guid=","\">",0,true);
						//we need only those where guid exist
						dummy4 = tempEx2.findNext();
						if(dummy4.length()>0){
							// Have a bug, lets get description and picture
							dummy5 = fetch("http://www.geocaching.com//track/details.aspx?guid="+dummy4);
							tempEx = new Extractor(dummy5,"<span id=\"lbHeading\">","</span>",0,true);
							dummy6 = tempEx.findNext();
							ch.Bugs += "<b>Name:</b> " + dummy6 + "<br>";
							tempEx = new Extractor(dummy5,"<span id=\"BugDetail_BugGoal\">","</span>",0,true);
							dummy6 = tempEx.findNext();
							ch.Bugs += "<b>Goal: </b>" + dummy6 + "<br>";
							tempEx = new Extractor(dummy5,"<span id=\"BugDetail_BugDetails\">","</span>",0,true);
							dummy6 = tempEx.findNext();
							ch.Bugs += "<b>Details:</b> " + dummy6 + "<br><br><hr>";
							ch.has_bug = true;
						}
						dummy2 = cacheEx.findNext();
					}
				}
				// End of search for travelbugs
				
				cacheEx = new Extractor(document, "<span id=\"Terrain\">", "</span>",0,true);
				dummy = cacheEx.findNext();
				cacheEx = new Extractor(dummy, "/stargreen", "gif",0,true);
				double counter = 0;
				cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					counter++;
					cacheEx.findNext();
				}
				cacheEx = new Extractor(dummy, "/halfstargreen", "gif",0,true);
				cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					counter = counter + 0.5;
					cacheEx.findNext();
				}
				ch.terrain = Convert.toString(counter);

				counter = 0;				
				cacheEx = new Extractor(document, "<span id=\"Difficulty\">", "</span>",0,true);
				dummy = new String();
				dummy = cacheEx.findNext();
				cacheEx = new Extractor(dummy, "/staryellow", "gif",0,true);
				cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					counter++;
					cacheEx.findNext();
				}
				cacheEx = new Extractor(dummy, "/halfstaryellow", "gif",0,true);
				cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					counter = counter + 0.5;
					cacheEx.findNext();
				}
				ch.hard = Convert.toString(counter);
				////Vm.debug("Hard: " +ch.hard + " / Terrain: " + ch.terrain);
				// Replace image tags in description
				// get unique images from everywhere
				// Replace image tags in logs
				// Get images in logs

				//hier ist ein Problem....
				//c) dann in LongDescription
				//d) dann in den logs
				
				//first find cacheattributes
				/*
				cacheEx = new Extractor(document, "<img src=\"../images/attributes/", "\" Alt",0,true);
				
				img = cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					ch.attributes.add(img);
					img = cacheEx.findNext();
				}
				*/
				
				//get the images in the image span
				cacheEx = new Extractor(document, "<span id=\"Images\">", "</span>",0,true);
				imgLoc = new String();
				String tmpDoc = new String();
				tmpDoc = cacheEx.findNext();
				if(cacheEx.endOfSearch() == false){
					tempEx = new Extractor(tmpDoc, "<A HREF=\"", "\" target=", 0, true);
					imgLoc = tempEx.findNext();
					while(tempEx.endOfSearch() == false){
						ch.Images.add(imgLoc);
						////Vm.debug("Got: " + imgLoc);
						imgLoc = tempEx.findNext();
					}
				}
				
				//get and replace images in the longdescription
				
				////Vm.debug("1");
				ch.LongDescription = " " + ch.LongDescription;
				cacheEx = new Extractor(ch.LongDescription, "<img src = \"", "\"", 0 ,true);
				imgLoc = new String();
				imgLoc = cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					ch.Images.add(imgLoc);
					imgLoc = cacheEx.findNext();
				}
				////Vm.debug("2");
				cacheEx = new Extractor(ch.LongDescription, "<img src = \"", ">", 0 ,false);
				imgLoc = new String();
				imgLoc = cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					ch.LongDescription = replace(ch.LongDescription, imgLoc, "");
					imgLoc = cacheEx.findNext();
				}
				
				////Vm.debug("3--------------------------");
				cacheEx = new Extractor(ch.LongDescription, "<img src=\"", "\"", 0 ,true);
				imgLoc = new String();
				imgLoc = cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					ch.Images.add(imgLoc);
					imgLoc = cacheEx.findNext();
				}
				
				////Vm.debug("4");
				cacheEx = new Extractor(ch.LongDescription, "<img src=\"", ">", 0 ,false);
				imgLoc = new String();
				imgLoc = cacheEx.findNext();
				while(cacheEx.endOfSearch() == false){
					ch.LongDescription = replace(ch.LongDescription, imgLoc, "");
					imgLoc = cacheEx.findNext();
				}
				// have all the images, now spider them!
				HttpConnection connImg;
				Socket sockImg;
				FileOutputStream fos;
				String datei = new String();
				String imageOrig = new String();
				String imageType = new String();
				String imageList = new String();
				ByteArray daten;
				int imgCounter = 0;
				
				// Images 
				//Vm.debug("Have images: " + Convert.toString(ch.Images.size()));
				for(int p = 0; p<ch.Images.size(); p++){
					imgCounter++;
					imageOrig = (String)ch.Images.get(p);
					//Vm.debug("Checking: " + imageOrig);
					//sleep(1500);
					//make sure we do not get the images more than once!
					if(imageList.indexOf(imageOrig) == -1){
						//some images are fetched using PHP
						//we don't want these images
						if(imageOrig.lastIndexOf(".php") <= 0){
							imageList = imageList + ";" + imageOrig;
							if(proxy.length()>0){
							connImg = new HttpConnection(proxy, Convert.parseInt(port), imageOrig);
							}else{
								connImg = new HttpConnection(imageOrig);
							}
							imageType = imageOrig.substring(imageOrig.lastIndexOf("."), imageOrig.lastIndexOf(".")+4);
							datei = pref.mydatadir + ch.wayPoint + "_" + Convert.toString(imgCounter)+ imageType;
							ch.Images.set(p, ch.wayPoint + "_" + Convert.toString(imgCounter) + imageType);
							//connImg.keepAliveMode = true;
							connImg.setRequestorProperty("Connection", "close");
							//Vm.debug("Connecting...");
							try{
								sockImg = connImg.connect();
								daten = connImg.readData(sockImg);
								fos = new FileOutputStream(new File(datei));
								fos.write(daten.toBytes());
								fos.close();
								sockImg.close();
							}catch(IOException ioex){
								//Vm.debug("File not found!");
								ch.Images.removeElementAt(p);
							}
						}
					} else {
						ch.Images.removeElementAt(p);
					}
				}
				//Log Images
				//Vm.debug("Going into log images");
				
				for(int p = 0; p<ch.LogImages.size(); p++){
					imgCounter++;
					imageOrig = (String)ch.LogImages.get(p);
					//Vm.debug("Fetching Log Image: " +imageOrig);
					datei = pref.mydatadir + ch.wayPoint + "_L_" + Convert.toString(imgCounter)+ ".jpg";
					File dateiF = new File(datei);
					//no need to save the file if it already exists!
					ch.LogImages.set(p, ch.wayPoint + "_L_" + Convert.toString(imgCounter) + ".jpg");
					if(!dateiF.exists()){
						imageOrig = "http://img.groundspeak.com/cache/log/"+imageOrig+".jpg";
						if(proxy.length()>0){
							connImg = new HttpConnection(proxy, Convert.parseInt(port), imageOrig);
						}else{
							connImg = new HttpConnection(imageOrig);
						}
						//connImg.keepAliveMode = true;
						connImg.setRequestorProperty("Connection", "close");
						sockImg = connImg.connect();
						daten = connImg.readData(sockImg);
						fos = new FileOutputStream(dateiF);
						fos.write(daten.toBytes());
						
						fos.close();
						sockImg.close();
					} //if file does not exist
				}
				
				//Vm.debug("Out of log images...");
				
				//Now check if cache has already been spidered earlier.
				//If yes change the status flags accordingly
				if(caches_available.indexOf(ch.wayPoint) < 0){ //no, this is a new cache
					//Vm.debug("Cache is new exists!!!!!!!!!!!!");
					ch.is_new = true;
					ch.is_update = false;
					ch.is_log_update = false;
					cacheDB.add(ch);
					caches_available = caches_available + ";" +wp;
				}else{ //yes cache exists
					//Vm.debug("Cache exists!!!!!!!!!!!!");
					CacheHolder checkCache = getCache(ch.wayPoint);
					ch.is_new = false;
					ch.is_update = false;
					ch.is_log_update = false;
					//Check if description changed
					if(ch.LongDescription.equals(checkCache.LongDescription) == false){
						ch.is_update = true;
						//Vm.debug(ch.LongDescription + "\n\n--------------\n\n");
						//Vm.debug(checkCache.LongDescription);
					}
					//Check if there are new logs
					/*
					if(ch.CacheLogs.size() != checkCache.CacheLogs.size()){
						ch.is_log_update = true;
						//Vm.debug("Log check: spidered: " + ch.CacheLogs.size() + " old: "+ checkCache.CacheLogs.size());
					}*/
					changeCache(ch);
				}
				
				crw.saveCacheDetails(ch, pref.mydatadir);	
				ch.CacheName = SafeXML.cleanback(ch.CacheName);
				if(ch.CacheName.equals("An Error Has Occured")){
					ch.type = "0";
				}
				ch.LatLon = SafeXML.cleanback(ch.LatLon);
				ch.CacheOwner = SafeXML.cleanback(ch.CacheOwner);
				//} // if cache exists				
				//Save cache index list (index.xml)
				//after each spider
				document = new String();
				doneCounter++;
				crw.saveIndex(cacheDB, pref.mydatadir);
				if(!ch.CacheName.equals("An Error Has Occured") && ch.LatLon.length() > 1){
					pll = new ParseLatLon(ch.LatLon,".");
					pll.parse();
					mpl = new MapLoader(pll.getLatDeg(),pll.getLonDeg(), pref.myproxy, pref.myproxyport);
					mpl.loadTo(pref.mydatadir + "/" + ch.wayPoint + "_map.gif", "3");
					mpl.loadTo(pref.mydatadir + "/" + ch.wayPoint + "_map_2.gif", "10");
				}
				if(shouldStop == true) break;
			} // for
			/*
			for(int i = 0; i < cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				ch.CacheName = SafeXML.cleanback(ch.CacheName);
				ch.LatLon = SafeXML.cleanback(ch.LatLon);
				ch.CacheOwner = SafeXML.cleanback(ch.CacheOwner);
				cacheDB.set(i, ch);
			}*/

			//pbf.clear();
		}
		
		private void changeCache(CacheHolder ch){
			CacheHolder retCH = new CacheHolder();
			String waypoint = new String();
			waypoint = ch.wayPoint;
			int i = 0;
			for(i = 0; i<cacheDB.size();i++){
				retCH = new CacheHolder();
				retCH = (CacheHolder)cacheDB.get(i);
				if(retCH.wayPoint.equals(waypoint)) break;
			}
			cacheDB.set(i,ch);
		}
		
		/**
		*	This method will load all information available for a
		*	cache. It returns a CacheHolder object.
		*/
		private CacheHolder getCache(String waypoint){
			CacheReaderWriter crw = new CacheReaderWriter();
			CacheHolder retCH = new CacheHolder();
			for(int i = 0; i<cacheDB.size();i++){
				retCH = new CacheHolder();
				retCH = (CacheHolder)cacheDB.get(i);
				if(retCH.wayPoint.equals(waypoint)) {
					try{
						crw.readCache(retCH, pref.mydatadir);
					}catch(Exception ex){};
					return retCH;
				}
			}
			return retCH;
		}
		
		/**
		 *   Replace all instances of a String in a String.
		 *   @param  s  String to alter.
		 *   @param  f  String to look for.
		 *   @param  r  String to replace it with, or null to just remove it.
		 */ 
		private String replace( String s, String f, String r )
		{
		   if (s == null)  return s;
		   if (f == null)  return s;
		   if (r == null)  r = "";
		
		   int index01 = s.indexOf( f );
		   while (index01 != -1)
		   {
			  s = s.substring(0,index01) + r + s.substring(index01+f.length());
			  index01 += r.length();
			  index01 = s.indexOf( f, index01 );
		   }
		   return s;
		}
}
