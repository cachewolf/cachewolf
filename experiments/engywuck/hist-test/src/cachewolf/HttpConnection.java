package cachewolf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import cachewolf.utils.Common;

import eve.net.ChunkedDataInputStream;
import eve.data.Property;
import eve.data.PropertyList;
import eve.io.AsciiCodec;
import eve.io.File;
import eve.io.PartialInputStream;
import eve.io.StreamUtils;
import eve.io.TextCodec;
import eve.sys.Convert;
import eve.sys.Handle;
import eve.sys.Task;
import eve.sys.TimeOut;
import eve.sys.Vm;
import eve.util.ByteArray;
import eve.util.CharArray;
import eve.util.SubString;
import eve.net.Net;
import eve.net.URL;


/**
Use this class to create an HttpConnection with a Web Server and to read
in the data for the connection.<p>
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
//##################################################################
/**
* The host to connect to.
**/
public String host;
/**
* The port to connect to.
**/
public int port;
/**
* The document to fetch/submit.
**/
public String document;
/**
* This is the command to be sent to the server. By default it is "GET". If you call
* setPostData() and command is "GET" then the command will be replaced by "POST".
**/
public String command = "GET";
/**
* This is the version sent to the server. By default it is "HTTP/1.1". You could
* change it to something else if necessary.
**/
public String requestVersion = "HTTP/1.1";
/**
* These are the properties that will be sent to the WebServer. These are sent after the
* initial GET/POST line. This is initially null, so you will have to create a new PropertyList
* for it, or use one of the setRequestorProperty() or addRequestorProperty() methods.
**/
public PropertyList requestorProperties;
/**
* This is the list of properties for the server and document. It is only valid after a connection has
* been made since it is sent by the server to the requestor. One properties that will always be in
* this list will be "response" (the first line sent by the server in response to the request).
* All other properties will be as specified by the server, and <b>the property names will be
* converted to all lowercase letters</b>.
**/
public PropertyList documentProperties;
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
public boolean keepAliveMode;
/**
* This is the length of the document <b>read in</b>, valid after a connection call. If it is -1, then the
* web server has not provided the length of the document.
**/
public int contentLength = -1;
/**
* This is the codec used when sending data to the server. 
**/
public TextCodec textCodec;

InputStream bytesToPost;
Object originalPostData;

	/**
	 * Set these when the class is instantiated the first time.
	 * afterwards you don't need to set proxy parameters anymore
	 */
	private static String proxy = Global.getPref().myproxy;
	private static int proxyPort = Common.parseInt(Global.getPref().myproxyport);
	private static boolean useProxy = Global.getPref().proxyActive;
	
	public static void setProxy(String proxyi, int proxyporti, boolean useproxyi) {
		proxy = proxyi;
		proxyPort = proxyporti;
		useProxy = useproxyi;
	}



/**
 * This returns true if post data has been set for this connection.
 */
public boolean hasPostData()
{
	return bytesToPost != null;
}
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
public HttpConnection getRedirectedConnection(String redirectTo)
{
	HttpConnection c = new HttpConnection(redirectTo);
	c.keepAliveMode = keepAliveMode;
	c.contentLength = contentLength;
	c.getRequestorProperties().set(getRequestorProperties());
	if (originalPostData != null) c.setPostData(originalPostData);
	c.documentIsEncoded = documentIsEncoded;
	c.command = command;
	c.textCodec = textCodec == null ? null : (TextCodec)textCodec.getCopy();
	c.requestVersion = requestVersion;
	return c;
}
/**
Returns the requestor properties. These are the property commands sent to the server when the
connection is made. You can add directly to this OR you can call setRequestorProperty() or
addRequestorProperty();
 */
//===================================================================
public PropertyList getRequestorProperties()
//===================================================================
{
	if (requestorProperties == null) requestorProperties = new PropertyList();
	return requestorProperties;
}
/**
 * Set the data to post out as either a Stream, InputStream,byte[],ByteArray or String.
 * If the data is a Stream or InputStream then you must also call setPostDataLength() 
 * which in turn sets the "Content-Length" property of the requestor properties - otherwise
 * if "Content-Length" is not already set it will be set to the length of the byte[] or ByteArray.
 * @param data the data to post either as a Stream, InputStream, byte[] or ByteArray
 */
//===================================================================
public void setPostData(Object data)
//===================================================================
{
	if (data instanceof InputStream) bytesToPost = (InputStream)data;
	else if (data instanceof ByteArray) {
		originalPostData = data;
		ByteArray ba = (ByteArray)data;
		bytesToPost = new ByteArrayInputStream(ba.data,0,ba.length);
		getRequestorProperties().defaultTo("Content-Length",Convert.toString(((ByteArray)data).length));
	}
	else if (data instanceof byte[]) {
		originalPostData = data;
		bytesToPost = new ByteArrayInputStream((byte[])data);
		getRequestorProperties().defaultTo("Content-Length",Convert.toString(((byte[])data).length));
	}else if (data instanceof String){
		String s = (String)data;
		TextCodec td = textCodec;
		if (td == null) td = new AsciiCodec();
		try{
			ByteArray got = td.encodeText(Vm.getStringChars(s),0,s.length(),true,null);
			setPostData(got.toBytes());
		}catch(IOException e){
			
		}
	}
	if (bytesToPost != null && command.equalsIgnoreCase("get"))
		command = "POST";
}
/**
 * This sets the "Content-Length" requestor property to be the specified length.
 * @param length the number of bytes to be posted.
 */
//===================================================================
public void setPostDataLength(int length)
//===================================================================
{
	getRequestorProperties().set("Content-Length",Convert.toString(length));
}
/**
 * Set an exclusive requestor property. These are sent to the web server after the initial request line.
 * @param name The name of the property.
 * @param property The value of the property.
 */
//===================================================================
public void setRequestorProperty(String name, String property)
//===================================================================
{
	getRequestorProperties().set(name,property);
}
/**
 * Add a non-exclusive requestor property. These are sent to the web server after the initial request line.
 * @param name The name of the property.
 * @param property The value of the property.
 */
//===================================================================
public void addRequestorProperty(String name, String property)
//===================================================================
{
	getRequestorProperties().add(name,property);
}
/**
 * Set the default value of a requestor property. If the value is already set
 * this will have no effect. Otherwise the value will be set to defaultValue.
 * @param name the name of the property.
 * @param defaultValue the value to default to.
 */
public void defaultRequestorProperty(String name, String defaultValue)
{
	getRequestorProperties().defaultTo(name,defaultValue);
}
protected Socket openSocket;
protected Socket connectedSocket;

/**
* If a connection has already been made to the server, then you can call
* this method and the HttpConnection protocol will be done over this Socket.
* @param sock The already connected socket.
*/
//===================================================================
public void setAlreadyOpenSocket(Socket sock)
//===================================================================
{	
	openSocket = sock;
}

/**
 * Create a new HttpConnection to the specified host and port to fetch the specified document.
 * @param host The host to connect to.
 * @param port The port to connect on.
 * @param document the document to get.
 */
//===================================================================
public HttpConnection(String host, int port, String document)
//===================================================================
{
	this.host = host;
	this.port = port;
	this.document = document;
}
/**
 * Create an HttpConnection with an http:// URL.
 * @param url The full url, starting with http://
 */
//===================================================================
public HttpConnection(String url)
//===================================================================
{
		if (useProxy) { 
			host = proxy;
			port = proxyPort;
			document = url;
		}	else {
			url = File.fixupPath(url);
			//ewe.sys.Vm.debug("url: "+url);
			port = 80;
			String uu = url.toLowerCase();
			if (uu.startsWith("http://")){
				uu = url.replace('\\','/');
				host = uu.substring(7);
				int first = host.indexOf('/');
				if (first == -1) document = "/";
				else {
					document = host.substring(first);
					host = host.substring(0,first);
				}
				int colon = host.indexOf(':');
				if (colon != -1){
					port = Convert.toInt(host.substring(colon+1));
					host = host.substring(0,colon);
				}
			}
		}
}

//===================================================================
public HttpConnection(URL url)
//===================================================================
{
	this(url.toString());
	documentIsEncoded = true;
}
static char [] space = {' '}, percentSpace = {'%','2','0'};

//===================================================================
public String toURLString()
//===================================================================
{
	return "http://"+host+":"+port+document;
}
//===================================================================
public String getEncodedDocument()
//===================================================================
{
	if (documentIsEncoded) return document;
	return URL.encodeURL(document,false);
}
//===================================================================
Object waitOnIO(Handle h,String errorMessage) throws IOException
//===================================================================
{
	try{
		h.waitOn(Handle.Success);
		return h.returnValue;
	}catch(Exception e){
		if (h.error instanceof IOException) 
			throw (IOException)h.error;
		throw new IOException(errorMessage);
	}
}
public static final int SocketConnected = 0x1;
public static final int DataReady = 0x2;

static Vector lines;
static SubString data;

//===================================================================
int makeRequest(Socket sock,TextCodec td) throws IOException
//===================================================================
{
	responseCode = -1;
	if (td == null) td = textCodec;
	if (td == null) td = new AsciiCodec();
	PropertyList pl = new PropertyList();
	if (requestorProperties != null) pl.set(requestorProperties);
	pl.defaultTo("Connection",keepAliveMode ? "keep-alive" : "close");
	pl.defaultTo("Host",host);
	StringBuffer sb = new StringBuffer();
	sb.append(command+" "+getEncodedDocument()+" "+requestVersion+"\r\n");
	for (int i = 0; i<pl.size(); i++){
		Property p = (Property)pl.get(i);
		if (p.value != null) sb.append(p.name+": "+p.value+"\r\n");
	}
	sb.append("\r\n");
	String req = sb.toString();
	char [] rc = Vm.getStringChars(req);
	ByteArray ba = ((TextCodec)td.getCopy()).encodeText(rc,0,rc.length,true,null);
	OutputStream os = sock.getOutputStream();
	os.write(ba.data,0,ba.length);
	os.flush();
	//
	if (bytesToPost != null){
		StreamUtils.transfer(null,bytesToPost,os);
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
	InputStream is = sock.getInputStream();
	while(true){
		int got = is.read();
		if (got == -1) throw new IOException("Unexpected end of stream.");
		if (got == 10){
			if (lastReceived == 10) break; //Got all the data now.
		}else if (got == 13) continue; //Ignore CR.
		ba.append((byte)got);
		lastReceived = got;
	}
	//
	CharArray all = ((TextCodec)td.getCopy()).decodeText(ba.data,0,ba.length,true,null);
	if (data == null){
		data = new SubString();
		lines = new Vector();
	}
	data.set(all.data,0,all.length);
	int got = data.split('\n',lines);
	documentProperties = new PropertyList();
	if (got == 0) throw new IOException("No response");


	String response = lines.get(0).toString();
	documentProperties.set("response",response);
	{
		int idx = response.indexOf(' ');
		if (idx != -1){
			int id2 = response.indexOf(' ',idx+1);
			if (id2 != -1){
				responseCode = Convert.toInt(response.substring(idx+1,id2));
			}
		}
	}
	for (int i = 1; i<got; i++){
		String s = lines.get(i).toString();
		int idx = s.indexOf(':');
		if (idx == -1) continue;
		String name = s.substring(0,idx).trim().toLowerCase();
		String value = s.substring(idx+1).trim();
		//if (document.endsWith("html")) ewe.sys.Vm.debug(document+": "+name+" = "+value);
		documentProperties.add(name,value);
	}
	contentLength = documentProperties.getInt("content-length",-1);
	//if (document.endsWith("?") || document.endsWith(".gif"))
	//ewe.sys.Vm.debug(documentProperties.toString());
	return responseCode;
}

static final String [] encodings = {"transfer-coding","transfer-encoding"};
//byte [] buffer;



/**
 * Call this after a successful connection. If the server requested a redirect (a 3xx code) then
 * this will return an HttpConnection to the new location which you can connect to again. You must
 * setup any post data or requestor properties again before re-connecting.
 * Alternatively you could also call getRedirectTo() and then if that returns a non-null
 * String, you can call getRedirectedConnection() to get copies.
 * If there is no redirection required or possible the method will return this same HttpConnection.
 */
//===================================================================
public HttpConnection redirectTo()
//===================================================================
{
	if (responseCode < 300 || responseCode > 399) return this;
	String newURL = documentProperties.getString("location",null);
	if (newURL == null) return this;
	return new HttpConnection(newURL);
}					

/**
 * Call this after a success connection. If it returns a non-null String then
 * you need to redirect the connection to the new location. If this returns non-null
 * you can call getRedirectedConnection() to get a new HttpConnection that you can
 * use to redirect the connection without having to setup the connection parameters
 * again.
 * @return null if no redirection is needed, otherwise the location directed to.
 */
public String getRedirectTo()
{
	if (responseCode < 300 || responseCode > 399) return null;
	return documentProperties.getString("location",null);
}
/**
 * Read in all the data from the Socket.
 * @param connection The socket returned by a connect() call.
 * @return A Handle with which you can monitor the connection. When the Handle
	reports Success, then the returnValue of the Handle will be a ewe.util.ByteArray
	object that holds the data read in.
 */
//===================================================================
public Handle readInData(Socket connection) 
//===================================================================
{
	int length = documentProperties.getInt("content-length",-1);
	if (length == 0)
		return new Handle(Handle.Succeeded,new ByteArray());
	//
	try{
		InputStream is = getInputStream(connection);
		return StreamUtils.readAllBytes(is,null,length,0);
	}catch(IOException e){
		Handle h = new Handle();
		h.fail(e);
		return h;
	}
}
/**
 * Read in all the data from the Socket.
 * @return A Handle with which you can monitor the connection. When the Handle
	reports Success, then the returnValue of the Handle will be a ewe.util.ByteArray
	object that holds the data read in.
 */
//===================================================================
public Handle readInData() 
//===================================================================
{
	return readInData(connectedSocket);
}
/**
* Get an InputStream to read in the data. This is a very important method as it is used by
* the readInData() method.
**/
//===================================================================
public InputStream getInputStream(Socket connectedSocket) throws IOException
//===================================================================
{
	//ewe.sys.Vm.debug(documentProperties.toString());
	int length = documentProperties.getInt("content-length",-1);
	if ("chunked".equals(documentProperties.getValue(encodings,null))){
		//System.out.println("Is chunked!");
		return new ChunkedDataInputStream(connectedSocket.getInputStream(),10240);
	}
	//System.out.println("Is not chunked!");
	return new PartialInputStream(connectedSocket.getInputStream(),length);
	
}
/**
* Get an InputStream to read in the data. This is a very important method as it is used by
* the readInData() method.
**/
//===================================================================
public InputStream getInputStream() throws IOException
//===================================================================
{
	return getInputStream(connectedSocket);
}
/**
 * Read in the document body from the Socket. This method blocks until the complete
 * data is read in. readInData() is a non-blocking version.
 * @param connection The socket returned by a connect() call.
 * @return A ByteArray containing the read in data.
 */
//===================================================================
public ByteArray readData(Socket connection) throws IOException
//===================================================================
{
	return (ByteArray)waitOnIO(readInData(connection),"Error reading data.");
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
//===================================================================
public Handle readInText(final Socket connection,TextCodec documentTextDecoder)
//===================================================================
{
	if (documentTextDecoder == null) documentTextDecoder = new AsciiCodec();
	final TextCodec cc = (TextCodec)documentTextDecoder.getCopy();
	return new Task(){
		protected void doRun(){
			Handle handle = this;
			try{
				Handle h = readInData(connection);
				if (!waitOnSuccess(h,TimeOut.Forever,true)) return;
				ByteArray ba = (ByteArray)h.returnValue;
				handle.returnValue = cc.decodeText(ba.data,0,ba.length,true,null);
				handle.set(Handle.Succeeded);
			}catch(Exception e){
				fail(e);
			}
		}
	}.start();
}
/**
 * Read in the document body from the Socket. This method blocks until the complete
 * data is read in. readInText() is a non-blocking version.
 * @param connection The socket returned by a connect() call.
 * @param documentTextDecoder The text codec to use to convert the bytes read in into text. If
 * this is null then a simple Ascii codec will be used.
 * @return A CharArray containing the text that was read in.
 */
//===================================================================
public CharArray readText(Socket connection,TextCodec documentTextDecoder) throws IOException
//===================================================================
{
	return (CharArray)waitOnIO(readInText(connection,documentTextDecoder),"Error reading data.");
}
/**
 * Connect asynchronously. This makes the connection, sends the request and requestor properties
 * reads in the reply and server properties and then returns the connected Socket ready for
 * for reading in the actual data.
 * @return A Handle used to monitor the connection. When the Handle reports a state of
 * Success, then the returnValue of the IOHandle will hold the connected socket.
 */
//===================================================================
public Handle connectAsync()
//===================================================================
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
//===================================================================
public Handle connectAsync(final TextCodec serverTextDecoder)
//===================================================================
{
	return new Task(){
		protected void doRun(){
			while(true){
			//
			// Create a Socket using an IOHandle.
			//
			Handle handle = this;
			try{
				Handle sh = (openSocket != null) ? new Handle(Handle.Succeeded,openSocket) : Net.newSocket(host,port); 
				try{
					//
					// Now wait until connected.
					//
					if (!waitOnSuccess(sh,TimeOut.Forever,true)){
						handle.fail(sh.error);
						return;
					}
					//ewe.sys.Vm.debug("Socket connected.");
					//
					// Report that the socket connection was made.
					// Now have to decode the data.
					//
					Socket sock = (Socket)sh.returnValue;
					handle.setFlags(SocketConnected,0);
					//
					makeRequest(sock,serverTextDecoder);
					//ewe.sys.Vm.debug("Request made.");
					handle.returnValue = connectedSocket = sock;
					handle.setFlags(Handle.Success,0);
					return;
				}catch(Exception e){
					if (openSocket == null){
						handle.fail(e);
						return;
					}
					openSocket = null;
					continue;
				}
			}catch(Exception e){
				handle.fail(e);
			}
		}}
	}.start();
}
/**
 * This makes the connection, blocking the current thread.
 * @return A Socket that you can read the data from. The document properties will be in
 * the document properties list.
 * @exception IOException if there was an error connecting or getting the data.
 */
//===================================================================
public Socket connect() throws IOException
//===================================================================
{
	return (Socket)waitOnIO(connectAsync(),"Could not connect.");
}
/**
 * Connect to the server and save the socket for later use as the "connectedSocket" field.
 * @return the connected socket - before any data is sent or read.
 * @exception IOException if a connection could not be made.
 */
//===================================================================
public Socket connectSocketOnly() throws IOException
//===================================================================
{
	//if (connectedSocket != null) return connectedSocket;
	//return connectedSocket = new Socket(host,port);
	if (openSocket != null) return openSocket;
	return openSocket = new Socket(host,port);
}

/*
//===================================================================
public Handle connectAsync2()
//===================================================================
{
	return new ewe.sys.Vm.TaskObject(){
		protected void doRun(){
			try{
				Socket sock = new Socket(host,port);
				handle.setFlags(SocketConnected,0);
								
			}catch(Exception e){
				handle.set(Handle.Failed);
				return;
			}
		}
	}.stratTask();
}
*/
/*
//===================================================================
public static void main(String args[]) throws IOException, InterruptedException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	HttpConnection hp = new HttpConnection("192.168.0.52",80,"/eweDemo.zip");
	Socket sock = hp.connect();
	Handle h = hp.readInData(sock);
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.showStop = true;
	pbf.execute(h,"Reading...");
	pbf.showMainTask = false;
	pbf.showSubTask = true;
	h.waitUntilStopped();
	if ((h.check() & h.Success) != 0) ewe.sys.Vm.debug("Success!!");
	sock.close();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

