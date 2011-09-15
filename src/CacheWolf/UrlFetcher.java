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

import ewe.data.PropertyList;
import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.util.ByteArray;
import ewe.util.CharArray;

public class UrlFetcher {
	static HttpConnection conn;
	static int maxRedirections = 5;
	static PropertyList requestorProperties = null;
	static PropertyList permanentRequestorProperties = null;
	static String postData = null;
	static String urltmp = null;
	static String realUrl = null;
	static boolean forceRedirect = false;

	public static PropertyList getDocumentProperties() {
		if (conn != null)
			return conn.documentProperties;
		else
			return null;
	}

	public static String getRealUrl() {
		return realUrl;
	};

	public static void setMaxRedirections(int value) {
		maxRedirections = value;
	};

	public static void setForceRedirect() {
		forceRedirect = true;
	};

	public static void setRequestorProperties(PropertyList value) {
		requestorProperties = value;
	};

	public static void setRequestorProperty(String name, String property) {
		if (requestorProperties == null)
			requestorProperties = new PropertyList();
		requestorProperties.set(name, property);
	}

	private static void initPermanentRequestorProperty() {
		permanentRequestorProperties = new PropertyList();
		permanentRequestorProperties.add("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
		permanentRequestorProperties.add("Connection", "close");
		// permanentRequestorProperties.add("Connection", "keep-alive");
	}

	public static void setPermanentRequestorProperty(String name, String property) {
		if (permanentRequestorProperties == null)
			initPermanentRequestorProperty();
		if (property != null)
			permanentRequestorProperties.set(name, property);
		else {
			int index = permanentRequestorProperties.find(name);
			if (index >= 0)
				permanentRequestorProperties.del(index);
		}
	}

	public static void setpostData(String value) {
		postData = value;
	};

	public static String fetch(String address) throws IOException {
		ByteArray daten = fetchByteArray(address);
		JavaUtf8Codec codec = new JavaUtf8Codec();
		CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
		return c_data.toString();
	}

	public static ByteArray fetchData(String address) throws IOException {
		return fetchByteArray(address);
	}

	public static void fetchDataFile(String address, String target) throws IOException {
		FileOutputStream outp = null;
		try {
		outp = new FileOutputStream(new File(target));
		outp.write(fetchByteArray(address).toBytes());
		} finally {
		if (outp != null) outp.close();
		}
	}

	/**
	 * @param url
	 * @return ByteArray
	 * @throws IOException
	 */
	public static ByteArray fetchByteArray(String url) throws IOException {
		int i = 0;
		conn = new HttpConnection(url); // todo reuse: don#t reuse, some params are not correctly reset with SetUrl
		urltmp = url;
		do { // allow max 5 redirections (http 302 location)
			i++;
			if (urltmp == null) {
				// hack for expedia, doing the original url again.
				// expedia always must redirect >=1 time, but sometimes that is missed
				// see also: http://www.geoclub.de/viewtopic.php?p=305071#305071
				urltmp = url;
				i = i - 1;
			}
			realUrl = urltmp;
			if (!( urltmp.startsWith("http") || urltmp.startsWith("https") )) {
				url = FileBase.fixupPath(url);
				String uu = url.toLowerCase();
				String host;
				uu = url.replace('\\', '/');
				host = uu.substring(7);
				int first = host.indexOf('/');
				if (first != -1) {
					host = host.substring(0, first);
				}
				if (!urltmp.startsWith("/"))
					host = host + "/";
				urltmp = "http://" + host + urltmp; // TODO https?
			}
			conn.setUrl(urltmp);
			conn.documentIsEncoded = isUrlEncoded(urltmp);
			if (permanentRequestorProperties == null)
				initPermanentRequestorProperty();
			if (postData != null) {
				conn.setPostData(postData);
				conn.setRequestorProperty("Content-Type", "application/x-www-form-urlencoded");
			}
			conn.setRequestorProperty(permanentRequestorProperties);
			if (requestorProperties != null)
				conn.setRequestorProperty(requestorProperties);
			conn.connect();
			if (conn.responseCode >= 400) {
				maxRedirections = 5;
				requestorProperties = null;
				postData = null;
				forceRedirect = false;
				throw new IOException("URL: " + urltmp + "\nhttp response code: " + conn.responseCode);
			}
			urltmp = conn.getRedirectTo();
			if (urltmp != null) {
				conn.disconnect();
				// mainly implemented for opencaching.de ... login
				final PropertyList pl = UrlFetcher.getDocumentProperties();
				if (pl != null) {
					String cookie = (String) pl.getValue("Set-Cookie", "");
					if (cookie.length() > 0) {
						if (postData == null)
							// do not overwrite existing cookie (mostly for geocaching.com)
							// normally a cookie exists for a website
							// we do not handle that correct
							setRequestorProperty("Cookie", cookie);
						else
							// needed for opencaching.de ... login
							setPermanentRequestorProperty("Cookie", cookie);
					}
				}
				conn = conn.getRedirectedConnection(urltmp);
				forceRedirect = false; // one time or more redirected
			}
		} while (((urltmp != null) || (urltmp == null) && forceRedirect) && i <= maxRedirections);
		if (i > maxRedirections)
			throw new IOException("too many http redirections while trying to fetch: " + url + " only " + maxRedirections + " are allowed");
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

	/**
	 * @param url
	 * @return true, if the string seems to be already URL encoded (that is, it contains only url-allowd chars), false
	 *         otherwise
	 */
	private static boolean isUrlEncoded(String url) {
		final String allowed = new String("-_.~!*'();:@&=+$,/?%#[]");
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
	 * Note that the encoding for URLs is not generally defined. Usually cp1252 or UTF-8 is used. It depends on what the
	 * server expects, what encoding you must use.
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
	 * Encode the URL using %## notation. Note: this fixes a bug in ewe.net.URL.encodeURL(): that routine assumes all
	 * chars to be < 127. This method is mainly copied from there
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