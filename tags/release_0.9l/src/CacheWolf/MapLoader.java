package CacheWolf;

import ewe.ui.*;
import ewe.io.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.net.*;

/**
*
*/

// Um Karten zu holen!
// http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.09901667,11.35688333&Lang=EUR0407&Alti=1&Size=600,600&Offs=0.000000,0.000000&Pins=|5748|
// oder
// http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.15,11.5833&Alti=2&Lang=EUR0407&Size=900,900&Offs=0,0&MapS=0&Pins=|48.15,11.5833|4|48.15,11.5833&Pins=|48.15,11.5833|1|48.15,%2011.5833||


public class MapLoader{
	String proxy = new String();
	String port = new String();
	String lat = new String();
	String lon = new String();
	String zone = new String();
	public MapLoader(String lt, String ln, String prxy, String prt){
		port = prt;
		proxy = prxy;
		lat = lt;
		lon = ln;
		zone = "EUR0407";
		try{
			if(Convert.parseDouble(ln) <= -10) zone = "USA0409";
		}catch(Exception ex){
			ln = ln.replace('.',',');
			//Vm.debug("Nach änderung: " +ln);
			if(Convert.parseDouble(ln) <= -10) zone = "USA0409";
		}
	}
	
	public void loadTo(String datei, String alti){
		HttpConnection connImg, conn2;
		Socket sockImg, sock2;
		InputStream is;
		FileOutputStream fos;
		ByteArray daten;
		String quelle = new String();
		
		quelle = "http://www.expedia.de/pub/agent.dll?qscr=mrdt";
		quelle = quelle + "&ID=3kQaz.";
		quelle = quelle + "&CenP=" + lat + "," + lon;
		quelle = quelle + "&Alti="+alti+"&Lang="+zone+"&Size=500,500&Offs=0,0&MapS=0&Pins=|" + lat + "," + lon + "|5|";
		//Vm.debug(lat + "," + lon);
		if(proxy.length()>0){
			connImg = new HttpConnection(proxy, Convert.parseInt(port), quelle);
			//Vm.debug("Loading quelle: " + quelle);
		}else{
			connImg = new HttpConnection(quelle);
		}
		//datei = "d:\\temp\\test_map.bmp";
		connImg.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
		connImg.setRequestorProperty("Connection", "close");
		connImg.setRequestorProperty("Cookie", "jscript=1; path=/;");
		connImg.documentIsEncoded = true;
		try{
			File dateiF = new File(datei);
			if(!dateiF.exists()){
				sockImg = connImg.connect();
				//Vm.debug("Redirect: " + connImg.getRedirectTo());
				quelle = connImg.getRedirectTo();
				sockImg.close();
				if(proxy.length()>0){
					connImg = new HttpConnection(proxy, Convert.parseInt(port), quelle);
				}else{
					connImg = new HttpConnection(quelle);
				}
				connImg.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
				connImg.setRequestorProperty("Connection", "close");
				connImg.setRequestorProperty("Cookie", "jscript=1; path=/;");
				connImg.documentIsEncoded = true;
				sock2 = connImg.connect();
				daten = connImg.readData(sock2);
				fos = new FileOutputStream(dateiF);
				fos.write(daten.toBytes());
				fos.close();
				sock2.close();
			}
			//Vm.debug("done");
		}catch(Exception ex){
			//Vm.debug("Problem loading map: " + ex.toString());
		}
	}
}
