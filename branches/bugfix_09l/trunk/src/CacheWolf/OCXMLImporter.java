package CacheWolf;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.ui.MessageArea;
import ewe.util.*;
import ewe.data.PropertyList;
import ewe.net.*;
import ewe.ui.*;

/**
*	Class to import Data from opencaching.de. 
*	It uses the lastmodified parameter to identify new or changed caches.
*	See here: http://www.opencaching.com/phpBB2/viewtopic.php?t=281
*	for more information.
*/
public class OCXMLImporter extends MinML {
	Vector cacheDB;
	CacheHolder holder;
	Preferences myPref = new Preferences();
	String strData = new String();
	int sync_year, sync_month,sync_day;
	Hashtable DBindex = new Hashtable();
	int zaehlerGel = 0;
	InfoBox infB;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public OCXMLImporter(Vector DB, Preferences pf)
	{
		cacheDB = DB;
		myPref = pf;
		if(myPref.last_sync_opencaching.length() == 0){
			myPref.last_sync_opencaching = "20050101000000";
		}
		CacheHolder ch = new CacheHolder();
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindex.put((String)ch.wayPoint, new Integer(i));
		}//for
		
	}
	
	public void doIt(){
		try{
			Vm.showWait(true);
			
			PrintWriter outp;
			String str = new String();
			String lastS =  myPref.last_sync_opencaching;
			infB = new InfoBox("Info", "Loading Users...");
			infB.show();
			//Fetch User List
			str = fetch("http://www.opencaching.de/xml/ocxml10.php?modifiedsince=20050101000000&users=1");
			str = cleanEntity(str);
			outp =  new PrintWriter(new BufferedWriter(new FileWriter("users.xml")));
			for(int i = 0; i < str.length();i++){
				if((int)str.charAt(i) == 0) outp.write(45);
				else outp.write(str.charAt(i));
			}
			outp.close();
			
			//Fetch cache list
			infB.setInfo("Loading Caches...");
			str = fetch("http://www.opencaching.de/xml/ocxml10.php?modifiedsince=" + lastS + "&caches=1");
			str = cleanEntity(str);
			outp =  new PrintWriter(new BufferedWriter(new FileWriter("caches.xml")));
			for(int i = 0; i < str.length();i++){
				if((int)str.charAt(i) == 0) outp.write(45);
				else outp.write(str.charAt(i));
			}
			outp.close();
			
			//Fetch descriptions
			infB.setInfo("Loading Cache Descriptions...");
			str = fetch("http://www.opencaching.de/xml/ocxml10.php?modifiedsince=" + lastS + "&descs=1");
			str = cleanEntity(str);
			outp =  new PrintWriter(new BufferedWriter(new FileWriter("descs.xml")));
			for(int i = 0; i < str.length();i++){
				if((int)str.charAt(i) == 0) outp.write(45);
				else outp.write(str.charAt(i));
			}
			outp.close();
			
			//Fetch pictures
			infB.setInfo("Loading Pictures...");
			str = fetch("http://www.opencaching.de/xml/ocxml10.php?modifiedsince=" + lastS + "&pictures=1");
			str = cleanEntity(str);
			outp =  new PrintWriter(new BufferedWriter(new FileWriter("pictures.xml")));
			for(int i = 0; i < str.length();i++){
				if((int)str.charAt(i) == 0) outp.write(45);
				else outp.write(str.charAt(i));
			}
			outp.close();
			
			//Fetch Log List
			infB.setInfo("Loading Logs...");
			str = fetch("http://www.opencaching.de/xml/ocxml10.php?modifiedsince=" + lastS + "&logs=1");
			str = cleanEntity(str);
			outp =  new PrintWriter(new BufferedWriter(new FileWriter("logs.xml")));
			for(int i = 0; i < str.length();i++){
				if((int)str.charAt(i) == 0) outp.write(45);
				else outp.write(str.charAt(i));
			}
			outp.close();
			
			//Parse users.xml
			//Parse caches.xml
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream("caches.xml"));
			parse(r);
			r.close();
			//Parse descriptions
			//Parse logs
			//Parse and download pictures
			
			ewe.sys.Time t = new ewe.sys.Time();
			t.setFormat("yyyyMMddHHmmss");
			myPref.last_sync_opencaching = t.toString();
			
		}catch (Exception e){
			Vm.debug(e.toString());
		}
		Vm.showWait(false);
	}
	
	public void startElement(String name, AttributeList atts){
		strData ="";
		if (name.equals("cache")) {
			holder = new CacheHolder();
			return;
		}
		if(name.equals("id")){
			holder.wayPoint = atts.getValue("id");
		}
		if(name.equals("type")){
			holder.type = transType(atts.getValue("id"));
		}
		if(name.equals("status")){
			if(atts.getValue("id").equals("1")) holder.is_available = true;
			if(atts.getValue("id").equals("2")) holder.is_available = false;
			if(atts.getValue("id").equals("3")) {
				holder.is_archived = true;
				holder.is_available = false;
			}
			if(atts.getValue("id").equals("4")) holder.is_available = false;
		}
		if(name.equals("size")){
			holder.CacheSize = transSize(atts.getValue("id"));
		}
		if(name.equals("userid")) holder.CacheOwner = atts.getValue("id");
		
	}
	
	public void endElement(String name){
		if (name.equals("cache")){
			// Add cache Data only, if waypoint not already in database
			if (searchWpt(holder.wayPoint)== -1){
				zaehlerGel++;
				//msgA.display("Info", ((String)lr.get(4000,"Loaded caches") + ":" + zaehlerGel), null);
				infB.setInfo(((String)lr.get(4000,"Loaded caches") + ":" + zaehlerGel));
				cacheDB.add(holder);
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveCacheDetails(holder,myPref.mydatadir);
				crw.saveIndex(cacheDB,myPref.mydatadir);
				DBindex.put((String)holder.wayPoint, new Integer(cacheDB.size()));
			}
		}
		if(name.equals("name")) holder.CacheName = strData;
		if(name.equals("longitude")){
			holder.LatLon = holder.LatLon + " " + londeg2min(strData);
		}
		if(name.equals("latitude")) {
			holder.LatLon = latdeg2min(strData) + holder.LatLon;
		}
		if(name.equals("difficulty")) {
			holder.hard = strData;
		}
		if(name.equals("terrain")) {
			holder.terrain = strData;
		}
		if(name.equals("datehidden")) {
			holder.DateHidden = strData;
		}
	}
	
	public String latdeg2min(String lat){
		String res = new String();
		String deg = new String();
		String min = new String();
		double minDbl;
		Double minDouble = new Double();
		
		// Get degrees
		if (lat.indexOf('.') < 0) lat = lat + ".0";
		deg = lat.substring(0, lat.indexOf('.'));
		if (deg.substring(0,1).equals("-"))res = "S" + deg.substring(1)+ "° "; 
		else  res = "N " + deg + "° ";
		// Get minutes
		min = lat.substring(lat.indexOf('.')+1);
		// fill with '0' up to 6 chars
		for (int i=min.length(); i<6; i++)
			min += "0";
		minDbl = Convert.toDouble(min);
		// Calc Minutes
		minDbl = minDbl * 60 / 1000000;
		minDouble.set(minDbl);
		minDouble.decimalPlaces = 3;
				
		// and back to string
		min = minDouble.toString().replace(',','.');
		// add leading '0'
		if (min.indexOf('.') == 1) min = "0" + min;
		// Build return string
		res += min;
		return res;
	}
	public String londeg2min(String lon){
		String res = new String();
		String deg = new String();
		String min = new String();
		double minDbl;
		Double minDouble = new Double();
		
		
		// Get degrees
		if (lon.indexOf('.') < 0) lon = lon + ".0";
		deg = lon.substring(0, lon.indexOf('.'));
		if (deg.substring(0,1).equals("-"))res = "W "; 
		else  res = "E ";
		// fill up leading '0'
		for (int i=deg.length();i<3;i++)
			res += "0";
		res += deg + "° ";
		// Get minutes
		min = lon.substring(lon.indexOf('.')+1);
		// fill with '0' up to 6 chars
		for (int i=min.length(); i<6; i++)
			min += "0";
		minDbl = Convert.toDouble(min);
		// Calc Minutes
		minDbl = minDbl * 60 / 1000000;
		minDouble.set(minDbl);
		minDouble.decimalPlaces = 3;
				
		// and back to string
		min = minDouble.toString().replace(',','.');
		// add leading '0'
		if (min.indexOf('.') == 1) min = "0" + min;
		// Build return string
		res += min;
		return res;
	}
	
	public void characters(char[] ch,int start,int length){
		String chars = new String(ch,start,length);
		strData += chars;
		Vm.debug(strData);
	}
	
	private String fetch(String address) throws IOException
	   	{
			//Vm.debug(address);
			HttpConnection conn;
			if(myPref.myproxy.length() > 0){
				conn = new HttpConnection(myPref.myproxy, Convert.parseInt(myPref.myproxyport), address);
				Vm.debug("Proxy here: " + address);
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
			/*
			DataInputStream dis = new DataInputStream(conn.getInputStream());
			int ch;
			StringBuffer message = new StringBuffer();
			while ( ( ch = dis.read() ) != -1 ) {
				message = message.append((char)ch);
			}
			Vm.debug(message.toString());
			return " ";
			*/
		}
		
	private String fetch_post(String address, String document) throws IOException 
	   	{
			
			String line = new String();
			String totline = new String();
			if(myPref.myproxy.length()==0){
				try {
					// Create a socket to the host
					String hostname = "www.geocaching.com";
					int port = 80;
					InetAddress addr = InetAddress.getByName(hostname);
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
				conn = new HttpConnection(myPref.myproxy, Convert.parseInt(myPref.myproxyport), address);
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
	*	Method to translate opencaching types to geocaching types.
	*	Required to be "backwards" compatible :-(
	*	OC		GC		Comment		Regel
	*	1		8		Other			1->8
	*	2		2		Traditional		2->2
	*	3		3		Multi			3->3
	*	4		4		Virtual		4->4
	*	5		11		Webcam		5->11
	*	6		6		Event			6->6
	*	7		8		Quiz			7->8
	*	8		??		Math			8->108 (ok)
	*	9		??		Moving		9->109 (ok)
	*	10		??		Drive-In		10->110 (ok)
	*/
	private String transType(String type){
		if(type.equals("1")) return "8";
		if(type.equals("2")) return "2";
		if(type.equals("3")) return "3";	
		if(type.equals("4")) return "4";
		if(type.equals("5")) return "11";
		if(type.equals("6")) return "6";
		if(type.equals("7")) return "8";
		if(type.equals("8")) return "108";
		if(type.equals("9")) return "109";
		if(type.equals("10")) return "110";
		//no match found? return custom type!
		return "0";
	}	

	/**
	*	Method to translate opencaching size types to geocaching types.
	*	Required to be "backwards" compatible :-(
	*	OC	GC	Comment	Rule
	*	1	5	other size	1->5
	*	2 	1	micro		2->1
	*	3 	2	small		3->2
	*	4	3	normal		4->3
	*	5 	4	large		5->4
	*	6	6	very large	6->6
	*	7	7	no container	7->7
	*/
	private String transSize(String type){
		if(type.equals("1")) return "5";
		if(type.equals("2")) return "1";
		if(type.equals("3")) return "2";
		if(type.equals("4")) return "3";
		if(type.equals("5")) return "4";
		if(type.equals("6")) return "6";
		if(type.equals("7")) return "7";
		return "0";
	}
	
	/**
	* Method to iterate through cache database and look for waypoint.
	* Returns value >= 0 if waypoint is found, else -1
	*/
	private int searchWpt(String wpt){
		Integer INTR = (Integer)DBindex.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}
	
	private String cleanEntity(String text){
		text = replace(text,"&quot;","&#34;");
		text = replace(text,"&amp;","&#38;");
		text = replace(text,"&lt;","&#60;");
		text = replace(text,"&gt;","&#62;");
		text = replace(text,"&nbsp;","&#160;");
		text = replace(text,"&iexcl;","&#161;");
		text = replace(text,"&cent;","&#162;");
		text = replace(text,"&pound;","&#163;");
		text = replace(text,"&curren;","&#164;");
		text = replace(text,"&yen;","&#165;");
		text = replace(text,"&brvbar;","&#166;");
		text = replace(text,"&brkbar;","");
		text = replace(text,"&sect;","&#167;");
		text = replace(text,"&uml;","&#168;");
		text = replace(text,"&die;","");
		text = replace(text,"&copy;","&#169;");
		text = replace(text,"&ordf;","&#170;");
		text = replace(text,"&laquo;","&#171;");
		text = replace(text,"&not;","&#172;");
		text = replace(text,"&shy;","&#173;");
		text = replace(text,"&reg;","&#174;");
		text = replace(text,"&macr;","&#175;");
		text = replace(text,"&hibar;","");
		text = replace(text,"&deg;","&#176;");
		text = replace(text,"&plusmn;","&#177;");
		text = replace(text,"&sup2;","&#178;");
		text = replace(text,"&sup3;","&#179;");
		text = replace(text,"&acute;","&#180;");
		text = replace(text,"&micro;","&#181;");
		text = replace(text,"&para;","&#182;");
		text = replace(text,"&middot;","&#183;");
		text = replace(text,"&cedil;","&#184;");
		text = replace(text,"&sup1;","&#185;");
		text = replace(text,"&ordm;","&#186;");
		text = replace(text,"&raquo;","&#187;");
		text = replace(text,"&frac14;","&#188;");
		text = replace(text,"&frac12;","&#189;");
		text = replace(text,"&frac34;","&#190;");
		text = replace(text,"&iquest;","&#191;");
		text = replace(text,"&Agrave;","&#192;");
		text = replace(text,"&Aacute;","&#193;");
		text = replace(text,"&Acirc;","&#194;");
		text = replace(text,"&Atilde;","&#195;");
		text = replace(text,"&Auml;","&#196;");
		text = replace(text,"&Aring;","&#197;");
		text = replace(text,"&AElig;","&#198;");
		text = replace(text,"&Ccedil;","&#199;");
		text = replace(text,"&Egrave;","&#200;");
		text = replace(text,"&Eacute;","&#201;");
		text = replace(text,"&Ecirc;","&#202;");
		text = replace(text,"&Euml;","&#203;");
		text = replace(text,"&Igrave;","&#204;");
		text = replace(text,"&Iacute;","&#205;");
		text = replace(text,"&Icirc;","&#206;");
		text = replace(text,"&Iuml;","&#207;");
		text = replace(text,"&ETH;","&#208;");
		text = replace(text,"&Ntilde;","&#209;");
		text = replace(text,"&Ograve;","&#210;");
		text = replace(text,"&Oacute;","&#211;");
		text = replace(text,"&Ocirc;","&#212;");
		text = replace(text,"&Otilde;","&#213;");
		text = replace(text,"&Ouml;","&#214;");
		text = replace(text,"&times;","&#215;");
		text = replace(text,"&Oslash;","&#216;");
		text = replace(text,"&Ugrave;","&#217;");
		text = replace(text,"&Uacute;","&#218;");
		text = replace(text,"&Ucirc;","&#219;");
		text = replace(text,"&Uuml;","&#220;");
		text = replace(text,"&Yacute;","&#221;");
		text = replace(text,"&THORN;","&#222;");
		text = replace(text,"&szlig;","&#223;");
		text = replace(text,"&agrave;","&#224;");
		text = replace(text,"&aacute;","&#225;");
		text = replace(text,"&acirc;","&#226;");
		text = replace(text,"&atilde;","&#227;");
		text = replace(text,"&auml;","&#228;");
		text = replace(text,"&aring;","&#229;");
		text = replace(text,"&aelig;","&#230;");
		text = replace(text,"&ccedil;","&#231;");
		text = replace(text,"&egrave;","&#232;");
		text = replace(text,"&eacute;","&#233;");
		text = replace(text,"&ecirc;","&#234;");
		text = replace(text,"&euml;","&#235;");
		text = replace(text,"&igrave;","&#236;");
		text = replace(text,"&iacute;","&#237;");
		text = replace(text,"&icirc;","&#238;");
		text = replace(text,"&iuml;","&#239;");
		text = replace(text,"&eth;","&#240;");
		text = replace(text,"&ntilde;","&#241;");
		text = replace(text,"&ograve;","&#242;");
		text = replace(text,"&oacute;","&#243;");
		text = replace(text,"&ocirc;","&#244;");
		text = replace(text,"&otilde;","&#245;");
		text = replace(text,"&ouml;","&#246;");
		text = replace(text,"&divide;","&#247;");
		text = replace(text,"&oslash;","&#248;");
		text = replace(text,"&ugrave;","&#249;");
		text = replace(text,"&uacute;","&#250;");
		text = replace(text,"&ucirc;","&#251;");
		text = replace(text,"&uuml;","&#252;");
		text = replace(text,"&yacute;","&#253;");
		text = replace(text,"&thorn;","&#254;");
		text = replace(text,"&yuml;","&#255;");		
		
		return text;
	}		
	
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
}