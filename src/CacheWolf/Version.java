
package CacheWolf;

import ewe.net.*;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.*;

public class Version {
	static final String VER_MAJOR = "0.9n";
	static final String VER_MINOR = "";
	static final String VER_BUILD = " RC ";
	static final String VER_SVN ="$LastChangedRevision$";  //the  number is automatically replaced by subversion to the latest versionnumer of this file (svn:keywords LastChangedRevision)
	
	/**
	 * @return
	 */
	public static String getRelease() {
		 // habe die SVN-Nummer doch aus der Anzeige erstmal wieder herausgenommen, weil es in einem final Release doch recht seltsam aussähe.
		 // Sinnvoll wäre daher vielleicht, eine Methode getReleaseDatail, die die SVN-Versionnummer mit angibt und z.B. im "über"-Dialog angezeigt werden könnte.
		return VER_MAJOR + VER_MINOR + VER_BUILD + VER_SVN.substring(VER_SVN.indexOf(" "), VER_SVN.lastIndexOf(" "));
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
