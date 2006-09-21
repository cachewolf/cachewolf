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

/**
*	Class to spider caches from gc.com
*	It uses a generic parse tree to parse the page and build a gpx file.
*/
public class SpiderGC{
	private static Preferences pref = new Preferences();
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
	*	Method to start the spider
	*/
	public void doIt(){
		//Access the page once to get a viewstate
		String start = new String();
		CWPoint origin = new CWPoint(pref.mylgNS + " " +pref.mylgDeg + " " + pref.mylgMin + " " + pref.mybrWE + " " +pref.mybrDeg + " " + pref.mybrMin, CWPoint.CW);
		String doc = new String();
		//Get password
		InfoBox infB = new InfoBox("Password", "Enter password:", InfoBox.INPUT);
		infB.execute();
		passwort = infB.getInput();
		infB.close(0);
		infB = new InfoBox("Distance", "Max distance:", InfoBox.INPUT);
		infB.execute();
		distance = Convert.toDouble(infB.getInput());
		infB.close(0);
		infB = new InfoBox("Status", "Logging in...");
		infB.show();
		try{
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
			doc = URL.encodeURL("__VIEWSTATE",false) +"="+ URL.encodeURL(viewstate,false);
			doc += "&" + URL.encodeURL("myUsername",false) +"="+ URL.encodeURL(pref.myAlias,false);
			doc += "&" + URL.encodeURL("myPassword",false) +"="+ URL.encodeURL(passwort,false);
			doc += "&" + URL.encodeURL("cookie",false) +"="+ URL.encodeURL("on",false);
			doc += "&" + URL.encodeURL("Button1",false) +"="+ URL.encodeURL("Login",false);
			start = fetch_post("http://www.geocaching.com/login/Default.aspx", doc, "/login/default.aspx");
		}catch(Exception ex){
			Vm.debug("Could not login: gc.com start page");
		}
		
		rex.search(start);
		viewstate = rex.stringMatched(1);
		rexCookieID.search(start);
		cookieID = rexCookieID.stringMatched(1);
		//Vm.debug(cookieID);
		rexCookieSession.search(start);
		cookieSession = rexCookieSession.stringMatched(1);
		//Vm.debug(cookieSession);
		
		//Get first page
		infB.setInfo("Fetching first page...");
		try{
			start = fetch("http://www.geocaching.com/seek/nearest.aspx?lat=" + origin.getLatDeg(CWPoint.DD) + "&lon=" +origin.getLonDeg(CWPoint.DD) + "&f=1");
			
		}catch(Exception ex){
			Vm.debug("Could not get list");
		}
		String dummy = new String();
		//String lineBlck = new String();
		int page_number = 5;		
		Regex rexLine = new Regex("<tr bgcolor=((?s).*?)</tr>");
		int found_on_page = 0;
		//Loop till maximum distance has been found or no more caches are in the list
		while(distance > 0){
			rex.search(start);
			viewstate = rex.stringMatched(1);
			Vm.debug("In loop");
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
				Vm.debug(getDist(rexLine.stringMatched(1)) + " / " +getWP(rexLine.stringMatched(1)));
				found_on_page++;
				if(getDist(rexLine.stringMatched(1)) <= distance){
					if(indexDB.get((String)getWP(rexLine.stringMatched(1))) == null){
						cachesToLoad.add(getWP(rexLine.stringMatched(1)));
					}
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
					start = fetch_post("http://www.geocaching.com/seek/nearest.aspx", doc, "/seek/nearest.aspx");
				}catch(Exception ex){
					Vm.debug("Couldn't get the next page");
				}
			}
			Vm.debug("Distance is now: " + distance);
			found_on_page = 0;
		}
		infB.setInfo("Found " + cachesToLoad.size() + " caches");
		// Now ready to spider each cache
		String wpt = new String();
		CacheReaderWriter crw = new CacheReaderWriter();
		CacheHolder ch = new CacheHolder();
		for(int i = 0; i<cachesToLoad.size(); i++){
			ch = new CacheHolder();
			wpt = (String)cachesToLoad.get(i);
			// Get only caches not already available in the DB
			if(searchWpt(wpt) == -1){
				infB.setInfo("Loading: " + wpt +"(" + (i+1) + " / " + cachesToLoad.size() + ")");
				doc = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + wpt +"&log=y";
				try{
					start = fetch(doc);
				}catch(Exception ex){
					Vm.debug("Couldn't get cache detail page");
				}
				ch.is_new = true;
				ch.is_HTML = true;
				ch.wayPoint = wpt;
				Vm.debug(ch.wayPoint);
				
				ch.CacheLogs = getLogs(start);
				
				ch.LatLon = getLatLon(start);
				Vm.debug("LatLon: " + ch.LatLon);
				
				ch.LongDescription = getLongDesc(start);
				
				ch.CacheName = getName(start);
				Vm.debug("Name: " + ch.CacheName);
				
				ch.CacheOwner = getOwner(start);
				
				Vm.debug("Owner: " + ch.CacheOwner);
				ch.DateHidden = getDateHidden(start);
				
				Vm.debug("Hidden: " + ch.DateHidden);
				ch.Hints = getHints(start);
				
				Vm.debug("Hints: " + ch.Hints);
				
				
				Vm.debug("Got the hints");
				ch.CacheSize = getSize(start);
				
				Vm.debug("Size: " + ch.CacheSize);
				ch.hard = getDiff(start);
				
				Vm.debug("Hard: " + ch.hard);
				ch.terrain = getTerr(start);
				
				Vm.debug("Terr: " + ch.terrain);
				ch.type = getType(start);
				
				Vm.debug("Type: " + ch.type);
				getImages(start, ch);
				
				getMaps(ch);
				crw.saveCacheDetails(ch, pref.mydatadir);
				
				cacheDB.add(ch);

			}
		}
		crw.saveIndex(cacheDB,pref.mydatadir);
		infB.close(0);
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
	
	public void getMaps(CacheHolder holder){
		if(holder.LatLon.length() > 1){
			ParseLatLon pll = new ParseLatLon(holder.LatLon,".");
			pll.parse();
			MapLoader mpl = new MapLoader(pll.getLatDeg(),pll.getLonDeg(), pref.myproxy, pref.myproxyport);
			mpl.loadTo(pref.mydatadir + "/" + holder.wayPoint + "_map.gif", "3");
			mpl.loadTo(pref.mydatadir + "/" + holder.wayPoint + "_map_2.gif", "10");
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
		//longDesc = STRreplace.replace(longDesc, "SRC =", "SRC=");
		//longDesc = STRreplace.replace(longDesc, "SRC= \"", "SRC=\"");
		//longDesc = STRreplace.replace(longDesc, "\n", " ");
		//longDesc = STRreplace.replace(longDesc, " ", "");
		Extractor exImgBlock = new Extractor(longDesc, "<IMG", ">", 0, false);
		//Vm.debug("In getImages: Have longDesc" + longDesc);
		String tst = new String();
		tst = exImgBlock.findNext();
		Vm.debug("Test: \n" + tst);
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while(exImgBlock.endOfSearch() == false){
			imgUrl = exImgSrc.findNext();
			Vm.debug("Img Url: " +imgUrl);
			imgUrl = "http://" + imgUrl;
			if(imgUrl.length()>0){
				imgType = imgUrl.substring(imgUrl.lastIndexOf("."), imgUrl.lastIndexOf(".")+4);
				imgName = ch.wayPoint + "_" + Convert.toString(imgCounter);
				spiderImage(imgUrl, imgName+"."+imgType);
				imgCounter++;
				ch.Images.add(imgName+"."+imgType);
				ch.ImagesText.add(imgName);
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
			Vm.debug("Img Url: " +imgUrl);
			if(imgUrl.length()>0){
				imgUrl = "http://" + imgUrl;
				imgType = imgUrl.substring(imgUrl.lastIndexOf("."), imgUrl.lastIndexOf(".")+4);
				imgName = ch.wayPoint + "_" + Convert.toString(imgCounter);
				spiderImage(imgUrl, imgName+"."+imgType);
				imgCounter++;
				ch.Images.add(imgName+"."+imgType);
				ch.ImagesText.add(exImgName.findNext());
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
		datei = pref.mydatadir + target;
		if(pref.myproxy.length()>0){
			connImg = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), quelle);
		}else{
			connImg = new HttpConnection(quelle);
		}
		connImg.setRequestorProperty("Connection", "close");
		try{
			sockImg = connImg.connect();
			daten = connImg.readData(connImg.connect());
			fos = new FileOutputStream(new File(datei));
			fos.write(daten.toBytes());
			fos.close();
			sockImg.close();
		} catch (UnknownHostException e) { 
			Vm.debug("Host not there...");
		}catch(IOException ioex){
			Vm.debug("File not found!");
		} catch (Exception ex){
			Vm.debug("Some kind of problem!");
		} finally {
			//Vm.debug("This is stupid!!");
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
		else return "";
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
		return inRex.stringMatched(1);
	}
	
	private String getOwner(String doc){
		inRex = new Regex("<span id=\"CacheOwner\".*?by((?s).*?)\\[<A HREF=");
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
		Regex rex2 = new Regex("<span id=\"LongDescription\">((?s).*?)</span>");
		inRex.search(doc);
		rex2.search(doc);
		res = inRex.stringMatched(1) + "<br>";
		res += rex2.stringMatched(1); 
		return res;
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
	
	private Vector getLogs(String doc){
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
		Extractor exSingleLog = new Extractor(doc, "<STRONG>", "[<A href=", 0, false);
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
			reslts.add("<img src='"+ exIcon.findNext() +"'>&nbsp;" + exDate.findNext()+ " " + exName.findNext()+ exLog.findNext());
			
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
	
	public SpiderGC(Preferences prf, Vector cacheDB){
		this.cacheDB = cacheDB;
		pref = prf;
		indexDB = new Hashtable(cacheDB.size());
		CacheHolder ch = new CacheHolder();
		//index the database for faster searching!
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			indexDB.put((String)ch.wayPoint, new Integer(i));
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
				conn = new HttpConnection(pref.myproxy, Convert.parseInt(pref.myproxyport), address);
				//Vm.debug(address);
			} else {
				conn = new HttpConnection(address);
			}
			conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			if(cookieSession.length()>0){
				conn.setRequestorProperty("Cookie: ", "ASP.NET_SessionId="+cookieSession +"; userid="+cookieID);
			}
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
	
	/*
	private static String replace(String source, String pattern, String replace){
		if (source!=null)
		{
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;
		
			while( (found = source.indexOf(pattern, start) ) != -1) {
			    sb.append(source.substring(start, found));
			    sb.append(replace);
			    start = found + len;
			}
		
			sb.append(source.substring(start));
		
			return sb.toString();
		}
		else return "";
	}
	*/
}