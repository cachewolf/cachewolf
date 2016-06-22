/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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
package CacheWolf.utils;

import com.jcraft.jzlib.GZIPInputStream;

import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.io.AsciiCodec;
import ewe.io.ByteArrayInputStream;
import ewe.io.File;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.TextReader;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.mString;

public class UrlFetcher {
    static HttpConnection conn;
    static int maxRedirections = 5;
    static PropertyList requestorProperties = null;
    static PropertyList permanentRequestorProperties = null;
    static PropertyList cookies = null;
    static String postData = null;
    static boolean forceRedirect = false;

    public static PropertyList getDocumentProperties() {
	if (conn != null)
	    return conn.documentProperties;
	else
	    return null;
    }

    public static void setMaxRedirections(int value) {
	maxRedirections = value;
    };

    public static void setForceRedirect(boolean value) {
	forceRedirect = value;
    };

    public static void setRequestorProperties(PropertyList value) {
	requestorProperties = value;
    };

    public static void setRequestorProperty(String name, String value) {
	if (requestorProperties == null)
	    requestorProperties = new PropertyList();
	requestorProperties.set(name, value);
    }

    private static void initPermanentRequestorProperty() {
	permanentRequestorProperties = new PropertyList();
	permanentRequestorProperties.set("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
	//permanentRequestorProperties.set("Connection", "close");
	permanentRequestorProperties.set("Connection", "keep-alive");
    }

    public static void setPermanentRequestorProperty(String name, String value) {
	if (permanentRequestorProperties == null)
	    initPermanentRequestorProperty();
	if (value != null)
	    permanentRequestorProperties.set(name, value);
	else {
	    int index = permanentRequestorProperties.find(name);
	    if (index >= 0)
		permanentRequestorProperties.del(index);
	}
    }

    public static void clearCookies() {
	if (cookies == null) {
	    cookies = new PropertyList();
	} else
	    cookies.clear();
    }

    public static void setCookie(String name, String value) {
	if (cookies == null) {
	    cookies = new PropertyList();
	}
	if (name != null)
	    if (value != null) {
		cookies.set(name, value);
	    }
    }

    public static void delCookie(String name) {
	if (cookies == null) {
	    cookies = new PropertyList();
	}
	if (name != null) {
	    int index = cookies.find(name);
	    if (index >= 0)
		cookies.del(index);
	}
    }

    public static String getCookie(String name) {
	Property p = cookies.get(name);
	if (p != null) {
	    return (String) p.value;
	} else
	    return null;
    }

    public static String getCookieValue(String SetValue) {
	String[] theCookie = mString.split((String) SetValue, ';');
	if (theCookie.length > 1) {
	    String[] rp = mString.split(theCookie[0], '=');
	    if (rp.length == 2) {
		return rp[1];
	    }
	}
	return null;
    }

    private static void addCookies2RequestorProperties() {
	String value = "";
	if (cookies == null) {
	    cookies = new PropertyList();
	}
	for (int i = 0; i < cookies.size(); i++) {
	    final Property cookie = (Property) cookies.get(i);
	    // so war es
	    String cd[] = mString.split(cookie.name, ';');
	    // ist das cookie für diesen host?
	    if (cd[1].equalsIgnoreCase(conn.getHost())) {
		value = value + cd[0] + "=" + getCookieValue((String) cookie.value) + "; ";
	    }
	}
	if (value.length() > 0) {
	    conn.setRequestorProperty("Cookie", value);
	    //Preferences.itself().log("Cookies sent for " + conn.getHost() + " : " + value);
	}
    }

    private static void addPermanent2RequestorProperties() {
	if (permanentRequestorProperties == null)
	    initPermanentRequestorProperty();
	conn.setRequestorProperty(permanentRequestorProperties);
    }

    private static void add2RequestorProperties() {
	if (requestorProperties != null)
	    conn.setRequestorProperty(requestorProperties);
    }

    public static void setpostData(String value) {
	postData = value;
    };

    public static String fetch(String address) throws IOException {
	return fetch(address, true);
    }

    public static String fetch(String address, boolean useGZip) throws IOException {
	if (useGZip) {
	    setRequestorProperty("Accept-Encoding", "gzip");
	}
	ByteArray daten = fetchByteArray(address);
	boolean gzip = false;
	if (conn != null) {
	    if (conn.documentProperties != null) {
		Property p = conn.documentProperties.get("Content-Encoding");
		if (p != null) {
		    if (p.value.toString().equalsIgnoreCase("gzip")) {
			gzip = true;
		    }
		}
	    }
	    if (gzip) {
		ByteArrayInputStream bis = new ByteArrayInputStream(daten.data);
		GZIPInputStream zis = new GZIPInputStream(bis);
		TextReader br = new TextReader(zis);
		br.codec = new BetterUTF8Codec();
		String line;
		StringBuffer sb = new StringBuffer();
		try {
		    while ((line = br.readLine()) != null) {
			sb.append(line.trim() + "\n");
		    }
		} catch (Exception e) {
		} finally {
		    if (br != null) {
			try {
			    br.close();
			} catch (Exception e) {
			}
		    }
		}
		return sb.toString();
	    } else {
		return new BetterUTF8Codec().decodeUTF8(daten.data, 0, daten.length).toString();
	    }
	}
	throw new IOException("got no data from web");
    }

    public static void fetchDataFile(String address, String target) throws IOException {
	FileOutputStream outp = null;
	try {
	    byte[] buffer = fetchByteArray(address).toBytes();
	    File f = new File(target);
	    outp = new FileOutputStream(f);
	    outp.write(buffer);
	} finally {
	    if (outp != null)
		outp.close();
	}
    }

    /**
     * @param url
     * @return ByteArray
     * @throws IOException
     */
    public static ByteArray fetchByteArray(String url) throws IOException {
	conn = new HttpConnection(url);
	String urltmp = url;

	conn.documentIsEncoded = isUrlEncoded(urltmp);

	addPermanent2RequestorProperties();
	addCookies2RequestorProperties();
	add2RequestorProperties();

	if (postData != null) {
	    conn.setPostData(postData);
	    conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
	}

	int redirectionCounter = 0;
	do {
	    redirectionCounter++;

	    conn.connect();

	    if (conn.responseCode < 300 || conn.responseCode > 399) {
		if (conn.responseCode > 399) {
		    // abort with error
		    maxRedirections = 5;
		    requestorProperties = null;
		    postData = null;
		    forceRedirect = false;
		    throw new IOException("URL: " + urltmp + "\nhttp response code: " + conn.responseCode);
		} else {
		    if (forceRedirect) {
			// hack for expedia, doing the original url again. (forceRedirect == true)
			// expedia always must redirect >=1 time, but sometimes that is missed
			// see also: http://www.geoclub.de/viewtopic.php?p=305071#305071
			urltmp = url;
			redirectionCounter = redirectionCounter - 1;
			forceRedirect = false;
		    } else {
			// now can get data
			urltmp = null;
		    }
		}
	    } else {
		//  redirection
		urltmp = conn.documentProperties.getString("location", null);
		// Preferences.itself().log("Url Redirected to " + urltmp);
		rememberCookies();
		addCookies2RequestorProperties();
		conn.disconnect();
		conn = conn.getRedirectedConnection(urltmp);
		if (redirectionCounter > maxRedirections)
		    throw new IOException("too many http redirections while trying to fetch: " + url + " only " + maxRedirections + " are allowed");
	    }
	} while (urltmp != null);

	ByteArray daten;
	if (conn.isOpen()) {
	    daten = conn.readData();
	    conn.disconnect();
	} else
	    daten = null;
	maxRedirections = 5;
	requestorProperties = null;
	postData = null;
	forceRedirect = false;
	return daten;
    }

    public static void rememberCookies() {
	final PropertyList pl = getDocumentProperties();
	// collect Set-Cookie
	for (int j = 0; j < pl.size(); j++) {
	    final Property p = (Property) pl.get(j);
	    if (p.name.equalsIgnoreCase("Set-Cookie")) {
		String[] theCookie = mString.split((String) p.value, ';');
		if (theCookie.length > 1) {
		    String[] rp = mString.split(theCookie[0], '=');
		    if (rp.length == 2) {
			setCookie(rp[0] + ";" + conn.getHost(), (String) p.value); // alles (wegen Ablaufdatum speichern)
		    }
		}
	    }
	}
    }

    /**
     * @param url
     * @return true, if the string seems to be already URL encoded (that is, it contains only url-allowd chars), false otherwise
     */
    private static boolean isUrlEncoded(String url) {
	final String allowed = "-_.~!*'();:@&=+$,/?%#[]";
	char[] src = ewe.sys.Vm.getStringChars(url);
	char c;
	for (int i = 0; i < src.length; i++) {
	    c = src[i];
	    if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (allowed.indexOf(c) >= 0))
		continue;
	    else
		return false;
	}
	return true;
    }

    /**
     * This method encodes an URL containing special characters using the UTF-8 codec in %nn%nn notation<br>
     * Note that the encoding for URLs is not generally defined. Usually cp1252 or UTF-8 is used. It depends on what the server expects, what encoding you must use.
     * 
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
	return encodeURL(utf8bytes.toString(), true);
    }

    final static String hex = ewe.util.TextEncoder.hex;

    /**
     * Encode the URL using %## notation. Note: this fixes a bug in ewe.net.URL.encodeURL(): that routine assumes all chars to be < 127. This method is mainly copied from there
     * It also encodes the /. This is necessary for the __VIEWSTATEs of GC
     * 
     * @param url
     *            The unencoded URL.
     * @param spaceToPlus
     *            true if you wish a space to be encoded as a '+', false to encode it as %20
     * @return The encoded URL.
     */
    // ===================================================================
    public static String encodeURL(String url, boolean spaceToPlus)
    // ===================================================================
    {
	char[] what = ewe.sys.Vm.getStringChars(url);
	int max = what.length;
	char[] dest = new char[max + max / 2];
	char d = 0;
	for (int i = 0; i < max; i++) {
	    if (d >= dest.length - 2) {
		char[] n = new char[dest.length + dest.length / 2 + 3];
		ewe.sys.Vm.copyArray(dest, 0, n, 0, d);
		dest = n;
	    }
	    char c = what[i];
	    // added || c == '$' || c == '/' || c == ','
	    if (spaceToPlus && c == ' ')
		c = '+';
	    else if (c <= ' ' || c >= 127 || c == '+' || c == '&' || c == '%' || c == '=' || c == '|' || c == '{' || c == '}' || c == '$' || c == '/' || c == ',') {
		dest[d++] = '%';
		dest[d++] = hex.charAt((c >> 4) & 0xf);
		dest[d++] = hex.charAt(c & 0xf);
		continue;
	    }
	    dest[d++] = c;
	}
	return new String(dest, 0, d);
    }

}