package CacheWolf;

import ewe.net.*;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.*;

public class Version {
	static final String VER_MAJOR = "0.9";
	static final String VER_MINOR = " m";
	static final String VER_BUILD = " RC5";
	
	public static String getRelease() {
		return VER_MAJOR + VER_MINOR + VER_BUILD;
	}
	
	public boolean newVersionAvailable(Preferences pref){
		boolean newvers = false;
		String vers = new String();
		try{
			vers = fetch("http://download.berlios.de/cachewolf/currentversion.txt", pref);
		}catch(Exception ex){
			Vm.debug("No load berlios?!");
		}
		if(!vers.equals(getRelease()) && vers.length()>0){
			newvers = true;
		}
		return newvers;
	}
	
	private static String fetch(String address, Preferences pref) throws IOException
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
}
