    /*
    GNU General Public License
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
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;

import ewe.io.AsciiCodec;
import ewe.io.ByteArrayInputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.net.URL;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Properties;

public class UrlFetcher {
	public static String fetchString(String address) throws IOException
	{
		ByteArray daten = fetchByteArray(address, null);
		JavaUtf8Codec codec = new JavaUtf8Codec();
		CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
		return c_data.toString();
	}

	public static Properties fetchPropertyList(String url) throws IOException {
		CharArray t = new CharArray();
		ByteArray doc = fetchByteArray(url, t);
		Properties props = new Properties();
		props.load(new ByteArrayInputStream(doc));
		return props; 
	}

	public static ByteArray fetchByteArray(String url, CharArray realurl) throws IOException {
		Handle[] hndl = new Handle[1];
		try {
		return fetchByteArray(url, realurl, hndl);
		} catch ( InterruptedException e) {
			throw new IOException("Error reading data. i :"+url);
		} catch ( HandleStoppedException e) {
			throw new IOException("Error reading data. s :"+url);
		}
	}

	/**
	 * @param url - if url-not-allowed chars are contained, they will be automatically encoded
	 * @param if non null, realurl will be filled with the real url, which can differ from the given url, in case url returns a http-redirect
	 * @return
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws HandleStoppedException 
	 */
	public static ByteArray fetchByteArray(String url, CharArray realurl, Handle[] hndl) 
	throws IOException, HandleStoppedException, InterruptedException {	
		final int maxRedirections = 5;
		HttpConnection conn = null;
		Socket sock = null;
		int i=0;
		String urltmp = new String(url);
		do  { // allow max 5 redirections (http 302 location)
			if (realurl != null) realurl.copyFrom(new String(urltmp));
			i++;
			conn = new HttpConnection(urltmp);
			conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = isUrlEncoded(urltmp);
			hndl[0] = conn.connectAsync();
			hndl[0].waitOn(Handle.Success);
			sock = (Socket)hndl[0].returnValue; //"Could not connect.");
			if (conn.responseCode >= 400) throw new IOException("URL: "+ urltmp + "\nhttp response code: " + conn.responseCode);
			urltmp = conn.getRedirectTo();
			if(urltmp!=null){
				URL eweUrl = new URL(url);
				if(urltmp.indexOf(eweUrl.getHost())<0){
					urltmp = new URL(eweUrl.getProtocol(), eweUrl.getHost(),eweUrl.getPort(), urltmp).url;
				}
				urltmp = STRreplace.replace(urltmp, eweUrl.getHost() + "/\\.\\./", eweUrl.getHost() + "/");
			}
		} while (urltmp != null && i <= maxRedirections ); 
		if (i > maxRedirections) throw new IOException("too many http redirections while trying to fetch: "+url + " only "+maxRedirections+" are allowed");
		hndl[0] = conn.readInData();
		ByteArray daten;
		try{
			hndl[0].waitOn(Handle.Success);
		}finally {
			sock.close();
		}
		daten = (ByteArray)hndl[0].returnValue;
		// ByteArray daten = conn.readData(sock);
		return daten;
	}

	/**
	 * @param url
	 * @return true, if the string seems to be already URL encoded (that is, it contains only url-allowd chars), false otherwise
	 */
	public static boolean isUrlEncoded(String url) {
		final String allowed = new String ("-_.~!*'();:@&=+$,/?%#[]");
		char [] src = ewe.sys.Vm.getStringChars(url);
		char c;
		for (int i = 0; i<src.length; i++){
			c = src[i];
			if (       (c >= 'A' && c <= 'Z') 
					|| (c >= 'a' && c <= 'z') 
					|| (c >= '0' && c <= '9')
					|| (allowed.indexOf(c) >= 0)
			) continue;
			else return false;
		}
		return true;
	}
	/**
	 * This method encodes an URL containing special characters
	 * using the UTF-8 codec in %nn%nn notation<br>
	 * Note that the encoding for URLs is not generally defined. Usually
	 * cp1252 or UTF-8 is used. It depends on what the server expects,
	 * what encoding you must use.
	 * @param cc
	 * @return
	 * @throws IOException
	 */
	public final static String toUtf8Url(String cc) throws IOException {
		JavaUtf8Codec coder = new JavaUtf8Codec();
		ByteArray utf8 = new ByteArray();
		coder.encodeText(cc.toCharArray(), 0, cc.length(), true, utf8);
		AsciiCodec asciicod = new AsciiCodec();
		CharArray utf8bytes = new CharArray();
		asciicod.decodeText(utf8.data, 0, utf8.length, true, utf8bytes);
		return encodeURL(utf8bytes.toString(), false);
	}
	
	final static String hex = ewe.util.TextEncoder.hex;
	/**
	 * Encode the URL using %## notation.
	 * Note: this fixes a bug in ewe.net.URL.encodeURL(): that routine
	 * assumes all chars to be < 127.
	 * This method is mainly copied from there
	 * @param url The unencoded URL.
	 * @param spaceToPlus true if you wish a space to be encoded as a '+', false to encode it as %20
	 * @return The encoded URL.
	 */
	//===================================================================
	public static String encodeURL(String url, boolean spaceToPlus)
	//===================================================================
	{
		char [] what = ewe.sys.Vm.getStringChars(url);
		int max = what.length;
		char [] dest = new char[max+max/2];
		char d = 0;
		for (int i = 0; i<max; i++){
			if (d >= dest.length-2) {
				char [] n = new char[dest.length+dest.length/2+3];
				ewe.sys.Vm.copyArray(dest,0,n,0,d);
				dest = n;
			}
			char c = what[i];
			if (spaceToPlus && c == ' ') c = '+';
			else if (c <= ' ' || c >= 127 || c == '+' || c == '&' || c == '%' || c == '=' || c == '|' || c == '{' || c == '}'){
				dest[d++] = '%';
				dest[d++] = hex.charAt((c >> 4) & 0xf);
				dest[d++] = hex.charAt(c & 0xf);
				continue;
			}
			dest[d++] = c;
		}
		return new String(dest,0,d);
	}

}