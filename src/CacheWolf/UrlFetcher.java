package CacheWolf;

import ewe.data.PropertyList;
import ewe.io.ByteArrayInputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Properties;

public class UrlFetcher {
	public static String fetchString(String address) throws IOException // TODO follow http-redirections
	{
		ByteArray daten = fetchByteArray(address);
		JavaUtf8Codec codec = new JavaUtf8Codec();
		CharArray c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
		return c_data.toString();
	}

	public static Properties fetchPropertyList(String url) throws IOException {
		ByteArray doc = fetchByteArray(url);
		Properties props = new Properties();
		props.load(new ByteArrayInputStream(doc));
		return props; 
	}
	public static ByteArray fetchByteArray(String url) throws IOException {	
		HttpConnection conn;
		conn = new HttpConnection(url);
		conn.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
		conn.setRequestorProperty("Connection", "close");
		conn.documentIsEncoded = true;
		Socket sock = conn.connect();
		if (conn.responseCode >= 400) throw new IOException("URL: "+ url + "\nhttp response code: " + conn.responseCode);
		ByteArray daten = conn.readData(sock);
		sock.close();
		return daten;
	}
}