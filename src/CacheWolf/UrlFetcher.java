package CacheWolf;

import ewe.io.ByteArrayInputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.net.URL;
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
	
	
	/**
	 * @param url - if url-not-allowed chars are contained, they will be automatically encoded
	 * @param if non null, realurl will be filled with the real url, which can differ from the given url, in case url returns a http-redirect
	 * @return
	 * @throws IOException
	 */
	public static ByteArray fetchByteArray(String url, CharArray realurl) throws IOException {	
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
			sock = conn.connect();
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
		ByteArray daten = conn.readData(sock);
		sock.close();
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
}