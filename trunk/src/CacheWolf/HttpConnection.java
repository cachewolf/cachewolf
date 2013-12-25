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
package CacheWolf;

import org.bouncycastle.crypto.tls.AlwaysValidVerifyer;
import org.bouncycastle.crypto.tls.TlsSocket;

import CacheWolf.utils.CWPartialInputStream;
import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.io.AsciiCodec;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.io.IOHandle;
import ewe.io.InputStream;
import ewe.io.MemoryFile;
import ewe.io.MemoryStream;
import ewe.io.OutputStream;
import ewe.io.Stream;
import ewe.io.StreamAdapter;
import ewe.io.StreamUtils;
import ewe.io.TextCodec;
import ewe.net.Socket;
import ewe.net.URL;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.SubString;
import ewe.util.Vector;
import ewe.util.mString;

/**
Use this class to create an HttpConnection with a Web Server and to read
in the data for the connection.<p>
This a a modified version of XXX. This version automatically makes use of a proxy server
if once for all proxy is set.
To use this do the following:
<ol>
<li>Create an HttpConnection object with a URL or specify the host, port and document to get.
<li>Change any of the HttpConnection parameters (including documentIsEncoded if your
document is URL encoded - i.e. it has '?' type data within it) and set requestor properties as needed.
<li>Call connectAsync() or connect() to make a Socket that is connected with the server. These
methods will also send the Http request (e.g. GET or POST), send the requestor parameters and
then read in and parse the server response and server parameters. When these methods are
complete the next data to be read in from the Socket will be the actual data bytes for the
http tranfer.
<li>Call redirectTo() to see if the request resulted in a redirection response from the server.
If redirectTo() returns a new HttpConnection object, then close the open Socket and go back
to step 2 using the new HttpConnection object instead.
<li>Call readInData() with the connected Socket to read in the raw data bytes of the requested
document or call readInText() to read in and convert the document to text.
</ol>
 **/
//##################################################################
public class HttpConnection {
    //	##################################################################
    /**
     * The host to connect to.
     **/
    private String host;
    /**
     * The port to connect to.
     **/
    private int port;

    private boolean useSslTls;
    /**
     * The document to fetch/submit.
     **/
    private String document;
    /**
     * This is the command to be sent to the server. By default it is "GET". If you call
     * setPostData() and command is "GET" then the command will be replaced by "POST".
     **/
    private String command = "GET";
    /**
     * This is the version sent to the server. By default it is "HTTP/1.1". You could
     * change it to something else if necessary.
     **/
    private String requestVersion = "HTTP/1.1";
    /**
     * These are the properties that will be sent to the WebServer. These are sent after the
     * initial GET/POST line. This is initially null, so you will have to create a new PropertyList
     * for it, or use one of the setRequestorProperty() or addRequestorProperty() methods.
     **/
    private ewe.data.PropertyList requestorProperties;
    /**
     * This is the list of properties for the server and document. It is only valid after a connection has
     * been made since it is sent by the server to the requestor. One properties that will always be in
     * this list will be "response" (the first line sent by the server in response to the request).
     * All other properties will be as specified by the server, and <b>the property names will be
     * converted to all lowercase letters</b>.
     **/
    public ewe.data.PropertyList documentProperties;
    /**
     * This is the response code from the server. It is only valid after a connection has
     * been made.
     **/
    public int responseCode;
    /**
     * If the document you supplied is already URL encoded, set this to true.
     **/
    public boolean documentIsEncoded;
    /**
     * Set this to true for keep alive mode requests.
     **/
    private boolean keepAliveMode;
    /**
     * This is the length of the document <b>read in</b>, valid after a connection call. If it is -1, then the
     * web server has not provided the length of the document.
     **/
    private int contentLength = -1;
    /**
     * This is the codec used when sending data to the server.
     **/
    private TextCodec textCodec;

    private Stream bytesToPost;
    private Object originalPostData;

    /**
     * Set these when the class is instantiated the first time.
     * afterwards you don't need to set proxy parameters anymore
     */

    private static String proxy = Global.pref.myproxy;
    private static int proxyPort = Common.parseInt(Global.pref.myproxyport);
    private static boolean useProxy = Global.pref.proxyActive;

    //	FIXME: why is this called immediately from preferences screen? shouldn't we read it from preferences instead?
    public static void setProxy(String proxyi, int proxyporti, boolean useproxyi) {
	proxy = proxyi;
	proxyPort = proxyporti;
	useProxy = useproxyi;
    }

    /**
     * This returns true if post data has been set for this connection.
     * FIXME: unreferenced!
     */
    //	public boolean hasPostData()
    //	{
    //	return bytesToPost != null;
    //	}
    /**
     * Get a new HttpConnection whose parameters are copied from this HttpConnection
     * but which directs its request to a different host/document as directed by
     * a redirect response from an initial request.<p>
     * Note that if this is a POST request and if the post-data source is an InputStream
     * or Stream, then you will need to call setPostData() again to set up the post data
     * for the new connection. Otherwise, the post data will be copied to this device.
     * Call hasPostData() to determine if the post data was copied across successfully.
     *
     * @param redirectTo the URL that the server instructed the client to redirect to.
     * @return a new HttpConnection with parameters copied from this one.
     */
    public HttpConnection getRedirectedConnection(String redirectTo) {
	if (!redirectTo.startsWith("http")) {
	    if (!redirectTo.startsWith("/"))
		redirectTo = "/" + redirectTo;
	    if (this.useSslTls || port == 443) {
		redirectTo = "https://" + host + redirectTo;
	    } else {
		redirectTo = "http://" + host + redirectTo;
	    }
	}
	HttpConnection c = new HttpConnection(redirectTo);
	c.keepAliveMode = keepAliveMode;
	c.contentLength = contentLength;
	c.getRequestorProperties().set(getRequestorProperties());
	c.command = command;
	//if (command.equalsIgnoreCase("POST")) {
	if (originalPostData != null) {
	    c.setPostData(originalPostData);
	    if (responseCode == 302) {
		// reflect the Post/Redirect/Get Method
		c.command = "GET";
	    }
	}
	c.documentIsEncoded = documentIsEncoded;
	c.textCodec = textCodec == null ? null : (TextCodec) textCodec.getCopy();
	c.requestVersion = requestVersion;
	return c;
    }

    /**
    Returns the requestor properties. These are the property commands sent to the server when the
    connection is made. You can add directly to this OR you can call setRequestorProperty() or
    addRequestorProperty();
     */
    //	===================================================================
    private PropertyList getRequestorProperties()
    //	===================================================================
    {
	if (requestorProperties == null)
	    requestorProperties = new PropertyList();
	return requestorProperties;
    }

    /**
     * Set the data to post out as either a Stream, InputStream,byte[],ByteArray or String.
     * If the data is a Stream or InputStream then you must also call setPostDataLength()
     * which in turn sets the "Content-Length" property of the requestor properties - otherwise
     * if "Content-Length" is not already set it will be set to the length of the byte[] or ByteArray.
     * @param data the data to post either as a Stream, InputStream, byte[] or ByteArray
     */
    //	===================================================================
    public void setPostData(Object data)
    //	===================================================================
    {
	if (data instanceof Stream)
	    bytesToPost = (Stream) data;
	else if (data instanceof ByteArray) {
	    originalPostData = data;
	    bytesToPost = new MemoryFile((ByteArray) data);
	    getRequestorProperties().defaultTo("Content-Length", Convert.toString(((ByteArray) data).length));
	} else if (data instanceof byte[]) {
	    originalPostData = data;
	    bytesToPost = new MemoryFile(new ByteArray((byte[]) data));
	    getRequestorProperties().defaultTo("Content-Length", Convert.toString(((byte[]) data).length));
	} else if (data instanceof String) {
	    String s = (String) data;
	    TextCodec td = textCodec;
	    if (td == null)
		td = new AsciiCodec();
	    try {
		ByteArray got = td.encodeText(Vm.getStringChars(s), 0, s.length(), true, null);
		setPostData(got.toBytes());
	    } catch (IOException e) {
		// Global.getPref().log("Ignored exception", e, true);
	    }
	} else if (data instanceof InputStream)
	    bytesToPost = new StreamAdapter((InputStream) data);
	if (bytesToPost != null && command.equalsIgnoreCase("get"))
	    command = "POST";
    }

    /**
     * This sets the "Content-Length" requestor property to be the specified length.
     * @param length the number of bytes to be posted.
     * FIXME: not referenced
     */
    //	===================================================================
    //	public void setPostDataLength(int length)
    ////	===================================================================
    //	{
    //	getRequestorProperties().set("Content-Length",Convert.toString(length));
    //	}
    /**
     * Set an exclusive requestor property. These are sent to the web server after the initial request line.
     * @param name The name of the property.
     * @param property The value of the property.
     */
    //	===================================================================
    public void setRequestorProperty(String name, String property)
    //	===================================================================
    {
	getRequestorProperties().set(name, property);
    }

    public void setRequestorProperty(PropertyList pl)
    //	===================================================================
    {
	getRequestorProperties().set(pl);
    }

    /**
     * Add a non-exclusive requestor property. These are sent to the web server after the initial request line.
     * @param name The name of the property.
     * @param property The value of the property.
     * FIXME: not referenced
     */
    ////	===================================================================
    //	public void addRequestorProperty(String name, String property)
    ////	===================================================================
    //	{
    //	getRequestorProperties().add(name,property);
    //	}
    /**
     * Set the default value of a requestor property. If the value is already set
     * this will have no effect. Otherwise the value will be set to defaultValue.
     * @param name the name of the property.
     * @param defaultValue the value to default to.
     * FIXME: not referenced
     */
    //	public void defaultRequestorProperty(String name, String defaultValue)
    //	{
    //	getRequestorProperties().defaultTo(name,defaultValue);
    //	}
    protected TlsSocket openSocket;
    protected TlsSocket connectedSocket;

    /**
     * If a connection has already been made to the server, then you can call
     * this method and the HttpConnection protocol will be done over this Socket.
     * @param sock The already connected socket.
     * FIXME: not referenced
     */
    ////	===================================================================
    //	public void setAlreadyOpenSocket(Socket sock)
    ////	===================================================================
    //	{
    //	openSocket = sock;
    //	}

    /**
     * Create a new HttpConnection to the specified host and port to fetch the specified document.
     * @param host The host to connect to.
     * @param port The port to connect on.
     * @param document the document to get.
     * FIXME: not referenced
     */
    ////	===================================================================
    //	public HttpConnection(String host, int port, String document)
    ////	===================================================================
    //	{
    //	this.host = host;
    //	this.port = port;
    //	this.document = document;
    //	}
    /**
     * Create an HttpConnection with an http:// URL.
     * @param url The full url, starting with http://
     */
    //	===================================================================
    public HttpConnection(String url)
    //	===================================================================
    {
	setUrl(url);
    }

    public void setUrl(String url) {
	if (useProxy) {
	    host = proxy;
	    port = proxyPort;
	    document = url;
	} else {
	    url = FileBase.fixupPath(url);
	    port = 80;
	    String uu = url;
	    if (uu.toLowerCase().startsWith("https://")) {
		useSslTls = true;
		port = 443;
		uu = "http://" + uu.substring(8);
	    } else
		useSslTls = false;
	    if (uu.toLowerCase().startsWith("http://")) {
		uu = uu.replace('\\', '/');
		host = uu.substring(7);
		int first = host.indexOf('/');
		if (first == -1)
		    document = "/";
		else {
		    document = host.substring(first);
		    host = host.substring(0, first);
		}
		int colon = host.indexOf(':');
		if (colon != -1) {
		    port = ewe.sys.Convert.toInt(host.substring(colon + 1));
		    host = host.substring(0, colon);
		}
	    }
	}
	getRequestorProperties().clear();
	command = "GET";
    }

    public String getHost() {
	return host;
    }

    //	FIXME: never referenced
    ////	===================================================================
    //	public HttpConnection(URL url)
    ////	===================================================================
    //	{
    //	this(url.toString());
    //	documentIsEncoded = true;
    //	}
    //	private static char [] space = {' '}, percentSpace = {'%','2','0'};

    //	FIXME: never referenced
    ////	===================================================================
    //	public String toURLString()
    ////	===================================================================
    //	{
    //	return "http://"+host+":"+port+document;
    //	}
    //	===================================================================
    private String getEncodedDocument()
    //	===================================================================
    {
	if (documentIsEncoded)
	    return document;
	else
	    return URL.encodeURL(document, false);
    }

    //	===================================================================
    private Object waitOnIO(Handle h, String errorMessage) throws IOException
    //	===================================================================
    {
	try {
	    h.waitOn(Handle.Success);
	    return h.returnValue;
	} catch (Exception e) {
	    if (h.errorObject instanceof IOException)
		throw (IOException) h.errorObject;
	    else
		throw new IOException(errorMessage);
	}
    }

    private static final int SocketConnected = 0x1;
    //	never referenced
    //	private static final int DataReady = 0x2;

    private static Vector lines;
    private static SubString data;

    //	===================================================================
    private int makeRequest(InputStream is, OutputStream os, TextCodec td) throws IOException
    //	===================================================================
    {
	responseCode = -1;
	if (td == null)
	    td = textCodec;
	if (td == null)
	    td = new AsciiCodec();
	PropertyList pl = new PropertyList();
	if (requestorProperties != null)
	    pl.set(requestorProperties);
	pl.defaultTo("Connection", "close");
	pl.defaultTo("Host", host);
	StringBuffer sb = new StringBuffer();
	sb.append(command + " " + getEncodedDocument() + " " + requestVersion + "\r\n");
	for (int i = 0; i < pl.size(); i++) {
	    Property p = (Property) pl.get(i);
	    if (p.value != null)
		sb.append(p.name + ": " + p.value + "\r\n");
	}
	sb.append("\r\n");
	String req = sb.toString();
	char[] rc = ewe.sys.Vm.getStringChars(req);
	ByteArray ba = ((TextCodec) td.getCopy()).encodeText(rc, 0, rc.length, true, null);
	os.write(ba.data, 0, ba.length);
	os.flush();
	//
	if (bytesToPost != null) {
	    //			IOTransfer iot = new IOTransfer();
	    //			iot.transfer(bytesToPost,sock);
	    transfer(bytesToPost, os);
	    os.flush();
	    bytesToPost.close();
	    /*
	    // For debugging - output eol and a blank line.
	    byte[] ret = new byte[]{(byte)'\r',(byte)'\n'};
	    sock.write(ret);
	    sock.write(ret);
	    sock.flush();
	     */
	}
	//
	int lastReceived = -1;
	//
	ba.clear();
	while (true) {
	    int got = is.read();
	    if (got == -1)
		throw new IOException("Unexpected end of stream." + ba.toString());
	    if (got == 10) {
		if (lastReceived == 10)
		    break; //Got all the data now.
	    } else if (got == 13)
		continue; //Ignore CR.
	    ba.append((byte) got);
	    lastReceived = got;
	}
	//
	CharArray all = ((TextCodec) td.getCopy()).decodeText(ba.data, 0, ba.length, true, null);
	if (data == null) {
	    data = new SubString();
	    lines = new Vector();
	}
	data.set(all.data, 0, all.length);
	int got = data.split('\n', lines);
	documentProperties = new ewe.data.PropertyList();
	if (got == 0)
	    throw new IOException("No response");

	String response = lines.get(0).toString();
	documentProperties.set("response", response);
	{
	    int idx = response.indexOf(' ');
	    if (idx != -1) {
		int id2 = response.indexOf(' ', idx + 1);
		if (id2 != -1) {
		    responseCode = ewe.sys.Convert.toInt(response.substring(idx + 1, id2));
		}
	    }
	}
	for (int i = 1; i < got; i++) {
	    String s = lines.get(i).toString();
	    int idx = s.indexOf(':');
	    if (idx == -1)
		continue;
	    String name = s.substring(0, idx).trim().toLowerCase();
	    String value = s.substring(idx + 1).trim();
	    documentProperties.add(name, value);
	}
	contentLength = documentProperties.getInt("content-length", -1);
	return responseCode;
    }

    private static final String[] encodings = { "transfer-coding", "transfer-encoding" };
    private byte[] buffer;

    /**
     * Copy from the "in" stream to the "out" stream. The streams are NOT closed.
     **/
    //	===================================================================
    public void transfer(Stream in, OutputStream out) throws IOException
    //	===================================================================
    {
	int bufferSize = 1024;
	byte[] buff = new byte[bufferSize];
	while (true) {
	    /**
	     * This readBytes method will block the current Coroutine until at
	     * least one byte is read. It will let other Coroutines run if it
	     * has to wait.
	     **/
	    int read = in.read(buff, 0, buff.length);
	    if (read == -1)
		break;
	    if (read == 0)
		continue;
	    /**
	     * This writeBytes method will block the current Coroutine until
	     * all bytes are written. It will let other Coroutines run if it
	     * has to wait.
	     **/
	    out.write(buff, 0, read);
	    // copied += read;
	    /**
	     * Allow other threads to have some time to execute.
	     **/
	}
	out.flush();
    }

    /**
     * Call this after a successful connection. If the server requested a redirect (a 3xx code) then
     * this will return an HttpConnection to the new location which you can connect to again. You must
     * setup any post data or requestor properties again before re-connecting.
     * Alternatively you could also call getRedirectTo() and then if that returns a non-null
     * String, you can call getRedirectedConnection() to get copies.
     * If there is no redirection required or possible the method will return this same HttpConnection.
     *
     * FIXME: never referenced
     */
    ////	===================================================================
    //	public HttpConnection redirectTo()
    ////	===================================================================
    //	{
    //	if (responseCode < 300 || responseCode > 399) return this;
    //	String newURL = documentProperties.getString("location",null);
    //	if (newURL == null) return this;
    //	return new HttpConnection(newURL);
    //	}

    /**
     * Call this after a success connection. If it returns a non-null String then
     * you need to redirect the connection to the new location. If this returns non-null
     * you can call getRedirectedConnection() to get a new HttpConnection that you can
     * use to redirect the connection without having to setup the connection parameters
     * again.
     * @return null if no redirection is needed, otherwise the location directed to.
     */
    public String getRedirectTo() {
	if (responseCode < 300 || responseCode > 399)
	    return null;
	return documentProperties.getString("location", null);
    }

    //	===================================================================
    private int readInChunkedHeader(InputStream connection, ByteArray buff, CharArray chBuff) throws IOException
    //	===================================================================
    {
	if (buffer == null)
	    buffer = new byte[10240];
	if (buff == null)
	    buff = new ByteArray();
	buff.clear();
	while (true) {
	    int got = connection.read();
	    if (got == -1)
		throw new IOException();
	    if (got == '\n')
		break;
	    buff.append((byte) got);
	}
	chBuff = new AsciiCodec().decodeText(buff.data, 0, buff.length, true, chBuff);
	String s = new String(chBuff.data, 0, chBuff.length);
	String length = mString.leftOf(s, ';').trim().toUpperCase();
	int clen = 0;
	for (int i = 0; i < length.length(); i++) {
	    char c = length.charAt(i);
	    clen *= 16;
	    clen += c <= '9' ? c - '0' : c - 'A' + 10;
	}
	return clen;
    }

    /**
     * Read in all the data from the Socket.
     * @param connection The Inputstream returned by a connect() call.
     * @return A Handle with which you can monitor the connection. When the Handle
    reports Success, then the returnValue of the Handle will be a ewe.util.ByteArray
    object that holds the data read in.
     */
    //	===================================================================
    private Handle readInData(final InputStream connection)
    //	===================================================================
    {
	int length = documentProperties.getInt("content-length", -1);
	if (length == 0)
	    return new Handle(Handle.Succeeded, new ByteArray());
	getInputStream();
	return StreamUtils.readAllBytes(getInputStream(), null, length, 0);
    }

    /**
     * Read in all the data from the Socket.
     * @return A Handle with which you can monitor the connection. When the Handle
    reports Success, then the returnValue of the Handle will be a ewe.util.ByteArray
    object that holds the data read in.

    FIXME: never referenced
     */
    //	===================================================================
    public Handle readInData()
    //	===================================================================
    {
	return readInData(connectedSocket.inputStream);
    }

    /**
     * Get an InputStream to read in the data. This is a very important method as it is used by
     * the readInData() method.
     **/
    //	===================================================================
    private InputStream getInputStream()
    //	===================================================================
    {
	int length = documentProperties.getInt("content-length", -1);
	if ("chunked".equals(documentProperties.getValue(encodings, null)))
	    return new MemoryStream(true) {
		private byte[] buff = new byte[10240];
		private int leftInBlock = 0;
		private ByteArray ba = new ByteArray();
		private CharArray ca = new CharArray();

		//-------------------------------------------------------------------
		protected boolean loadAndPutDataBlock() throws IOException
		//-------------------------------------------------------------------
		{
		    if (leftInBlock <= 0) {
			leftInBlock = readInChunkedHeader(connectedSocket.inputStream, ba, ca);
			if (leftInBlock <= 0)
			    return false;
		    }
		    int toRead = leftInBlock;
		    if (toRead > buff.length)
			toRead = buff.length;
		    int got = connectedSocket.inputStream.read(buff, 0, toRead);
		    if (got == -1)
			throw new IOException();
		    leftInBlock -= got;
		    putInBuffer(buff, 0, got);
		    if (leftInBlock == 0) {
			while (true) {
			    got = connectedSocket.inputStream.read();
			    if (got == -1)
				throw new IOException();
			    if (got == '\n')
				break;
			}
		    }
		    return true;
		}
	    }.toInputStream();
	//throw new IOException("Cannot get input stream from this!");
	else
	    return new CWPartialInputStream(connectedSocket.inputStream, length).toInputStream();
    }

    /**
     * Read in the document body from the Socket. This method blocks until the complete
     * data is read in. readInData() is a non-blocking version.
     * @param connection The socket returned by a connect() call.
     * @return A ByteArray containing the read in data.
     */
    /*
    //===================================================================
    public ByteArray readData(Socket connection) throws IOException
    //===================================================================
    {
    return (ByteArray)waitOnIO(readInData(connection),"Error reading data.");
    }
     */
    public ByteArray readData() throws IOException
    //	===================================================================
    {
	return (ByteArray) waitOnIO(readInData(openSocket.inputStream), "Error reading data.");
    }

    /**
     * Read in all the data from the Socket, converting it to text using the specified
     * codec.
     * @param connection The socket returned by a connect() call.
     * @param documentTextDecoder The text codec to use to convert the bytes read in into text. If
     * this is null then a simple Ascii codec will be used.
     * @return A Handle with which you can monitor the connection. When the Handle
    reports Success, then the returnValue of the Handle will be a ewe.util.CharArray
    object that holds the text read in.
     */
    /*
    //===================================================================
    private Handle readInText(final Socket connection,TextCodec documentTextDecoder)
    //===================================================================
    {
    if (documentTextDecoder == null) documentTextDecoder = new AsciiCodec();
    final TextCodec cc = (TextCodec)documentTextDecoder.getCopy();
    return new ewe.sys.TaskObject(){
    	protected void doRun(){
    		try{
    			Handle h = readInData(connection);
    			if (!waitOnSuccess(h,true)) return;
    			ByteArray ba = (ByteArray)h.returnValue;
    			handle.returnValue = cc.decodeText(ba.data,0,ba.length,true,null);
    			handle.set(Handle.Succeeded);
    		}catch(Exception e){
    			handle.errorObject = e;
    			handle.set(Handle.Failed);
    		}
    	}
    }.startTask();
    }
     */
    /**
     * Read in the document body from the Socket. This method blocks until the complete
     * data is read in. readInText() is a non-blocking version.
     * @param connection The socket returned by a connect() call.
     * @param documentTextDecoder The text codec to use to convert the bytes read in into text. If
     * this is null then a simple Ascii codec will be used.
     * @return A CharArray containing the text that was read in.
     */
    /*
    //===================================================================
    public CharArray readText(Socket connection,TextCodec documentTextDecoder) throws IOException
    //===================================================================
    {
    return (CharArray)waitOnIO(readInText(connection,documentTextDecoder),"Error reading data.");
    }
     */
    /**
     * Connect asynchronously. This makes the connection, sends the request and requestor properties
     * reads in the reply and server properties and then returns the connected Socket ready for
     * for reading in the actual data.
     * @return A Handle used to monitor the connection. When the Handle reports a state of
     * Success, then the returnValue of the IOHandle will hold the connected socket.
     */
    //	===================================================================
    public Handle connectAsync()
    //	===================================================================
    {
	return connectAsync(new AsciiCodec());
    }

    /**
     * Connect asynchronously. This makes the connection, sends the request and requestor properties
     * reads in the reply and server properties and then returns the connected Socket ready for
     * for reading in the actual data.
     * @param serverTextDecoder The text decoder to convert the server and requestor properties data into text.
     * @return A Handle used to monitor the connection. When the Handle reports a state of
     * Success, then the returnValue of the Handle will hold the connected socket.
     */
    //	===================================================================
    private Handle connectAsync(final TextCodec serverTextDecoder)
    //	===================================================================
    {
	return new ewe.sys.TaskObject() {
	    protected void doRun() {
		while (true) {
		    //
		    // Create a Socket using an IOHandle.
		    //
		    Handle sh;
		    Socket sock;
		    if (openSocket != null) {
			sh = new Handle(Handle.Succeeded, openSocket);
			sock = openSocket.socket;
		    } else {
			sh = new IOHandle();
			sock = new Socket(host, port, (IOHandle) sh);
			// openSocket = sock;
		    }
		    // Handle sh = (openSocket != null) ? new Handle(Handle.Succeeded,openSocket) : new IOHandle();
		    // Socket sock = (openSocket != null) ? openSocket : new Socket(host,port,(IOHandle)sh);
		    try {
			//
			// Now wait until connected.
			//
			if (!waitOnSuccess(sh, true))
			    return;
			//
			// Report that the socket connection was made.
			// Now have to decode the data.
			//
			handle.setFlags(SocketConnected, 0);

			TlsSocket tls = new TlsSocket((useSslTls ? new AlwaysValidVerifyer() : null), sock);
			makeRequest(tls.inputStream, tls.outputStream, serverTextDecoder);
			handle.returnValue = connectedSocket = tls;
			handle.setFlags(Handle.Success, 0);
			return;
		    } catch (Exception e) {
			if (openSocket == null) {
			    handle.failed(e);
			    return;
			} else {
			    openSocket = null;
			    continue;
			}
		    }
		}
	    }
	}.startTask();
    }

    /**
     * This makes the connection, blocking the current thread.
     * @return A Socket that you can read the data from. The document properties will be in
     * the document properties list.
     * @exception IOException if there was an error connecting or getting the data.
     */
    public TlsSocket connect() throws IOException {
	openSocket = (TlsSocket) waitOnIO(connectAsync(), "Could not connect.");
	return openSocket;
    }

    public void disconnect() {
	if (openSocket.socket.isOpen()) {
	    openSocket.close(); // releases the handles of the system
	}
    }

    public boolean isOpen() {
	return openSocket.socket.isOpen();
    }
}
