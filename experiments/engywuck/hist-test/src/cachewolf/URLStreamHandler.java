package cachewolf;

//Only needed for OCXMLImporter

/* URL.java -- Uniform Resource Locator Class
Copyright (C) 1998, 1999, 2000, 2002, 2003  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


import eve.io.File;
import java.io.IOException;

//##################################################################
public class URLStreamHandler{
//##################################################################
/**
* Provides the default equals calculation. May be overidden by handlers for
* other protocols that have different requirements for equals(). This method
* requires that none of its arguments is null. This is guaranteed by the
* fact that it is only called by java.net.URL class.
*
* @param url1 An URL object
* @param url2 An URL object
*/
protected boolean equals (URL url1, URL url2)
{
 // This comparison is very conservative.  It assumes that any
 // field can be null.
 return (url1.getPort () == url2.getPort ()
	    && ((url1.getProtocol () == null && url2.getProtocol () == null)
		|| (url1.getProtocol () != null
			&& url1.getProtocol ().equals (url2.getProtocol ())))
	    && ((url1.getUserInfo () == null && url2.getUserInfo () == null)
             || (url1.getUserInfo () != null
			&& url1.getUserInfo ().equals(url2.getUserInfo ())))
	    && ((url1.getAuthority () == null && url2.getAuthority () == null)
             || (url1.getAuthority () != null
			&& url1.getAuthority ().equals(url2.getAuthority ())))
	    && ((url1.getHost () == null && url2.getHost () == null)
		|| (url1.getHost () != null
			&& url1.getHost ().equals(url2.getHost ())))
	    && ((url1.getPath () == null && url2.getPath () == null)
		|| (url1.getPath () != null
			&& url1.getPath ().equals (url2.getPath ())))
	    && ((url1.getQuery () == null && url2.getQuery () == null)
             || (url1.getQuery () != null
			&& url1.getQuery ().equals(url2.getQuery ())))
	    && ((url1.getRef () == null && url2.getRef () == null)
		|| (url1.getRef () != null
			&& url1.getRef ().equals(url2.getRef ()))));
}
/**
* Returns the default port for a URL parsed by this handler. This method is
* meant to be overidden by handlers with default port numbers.
*/
protected int getDefaultPort ()
{
 return -1;
}

/**
* Provides the default hash calculation. May be overidden by handlers for
* other protocols that have different requirements for hashCode calculation.
*/
protected int hashCode (URL url)
{
 return url.getProtocol ().hashCode () +
        ((url.getHost () == null) ? 0 : url.getHost ().hashCode ()) +
	   url.getFile ().hashCode() +
	   url.getPort ();
}

/**
* This method converts a URL object into a String.  This method creates
* Strings in the mold of http URL's, so protocol handlers which use URL's
* that have a different syntax should override this method
*
* @param url The URL object to convert
*/
protected String toExternalForm(URL u)
{
 String protocol, host, file, ref;
 int port;

 protocol = u.getProtocol();

 // JDK 1.2 online doc infers that host could be null because it
 // explicitly states that file cannot be null, but is silent on host.
 host = u.getHost();
 if (host == null)
   host = "";

 port = u.getPort();
 file = u.getFile();
 ref = u.getRef();

 // Guess a reasonable size for the string buffer so we have to resize
 // at most once.
 int size = protocol.length() + host.length() + file.length() + 24;
 StringBuffer sb = new StringBuffer(size);

 if (protocol != null && protocol.length() > 0)
   {
	sb.append(protocol);
	sb.append(":");
   }

 if (host.length() != 0)
   sb.append("//").append(host);

 // Note that this produces different results from JDK 1.2 as JDK 1.2
 // ignores a non-default port if host is null or "".  That is inconsistent
 // with the spec since the result of this method is spec'ed so it can be
 // used to construct a new URL that is equivalent to the original.
 boolean port_needed = port > 0 && port != getDefaultPort();
 if (port_needed)
   sb.append(':').append(port);

 sb.append(file);

 if (ref != null)
   sb.append('#').append(ref);

 return sb.toString();
}
/**
* This method parses the string passed in as a URL and set's the
* instance data fields in the URL object passed in to the various values
* parsed out of the string.  The start parameter is the position to start
* scanning the string.  This is usually the position after the ":" which
* terminates the protocol name.  The end parameter is the position to
* stop scanning.  This will be either the end of the String, or the
* position of the "#" character, which separates the "file" portion of
* the URL from the "anchor" portion.
* <p>
* This method assumes URL's are formatted like http protocol URL's, so 
* subclasses that implement protocols with URL's the follow a different 
* syntax should override this method.  The lone exception is that if
* the protocol name set in the URL is "file", this method will accept
* an empty hostname (i.e., "file:///"), which is legal for that protocol
*
* @param url The URL object in which to store the results
* @param spec The String-ized URL to parse
* @param start The position in the string to start scanning from
* @param end The position in the string to stop scanning
*/
protected void parseURL(URL url, String spec, int start, int end)
{
 String host = url.getHost();
 int port = url.getPort();
 String file = url.getFile();
 String ref = url.getRef();
 
 if (spec.regionMatches (start, "//", 0, 2))
   {
	int hostEnd;
	int colon;

	start += 2;
	int slash = spec.indexOf('/', start);
	if (slash >= 0) 
	  hostEnd = slash;
     else
	  hostEnd = end;

	host = spec.substring (start, hostEnd);
	
	// Look for optional port number.  It is valid for the non-port
	// part of the host name to be null (e.g. a URL "http://:80").
	// TBD: JDK 1.2 in this case sets host to null rather than "";
	// this is undocumented and likely an unintended side effect in 1.2
	// so we'll be simple here and stick with "". Note that
	// "http://" or "http:///" produce a "" host in JDK 1.2.
	if ((colon = host.indexOf(':')) >= 0)
	  {
			port = eve.sys.Convert.toInt(host.substring(colon + 1));
	    host = host.substring(0, colon);
	  }
	file = null;
	start = hostEnd;
   } 
 else if (host == null) 
   host = "";

 if (file == null || file.length() == 0
	|| (start < end && spec.charAt(start) == '/')) 
   {
	// No file context available; just spec for file.
	// Or this is an absolute path name; ignore any file context.
	file = spec.substring(start, end);
	ref = null;
   } 
 else if (start < end)
   {
     // Context is available, but only override it if there is a new file.
     char sepChar = '/';
     int lastSlash = file.lastIndexOf (sepChar);
     if (lastSlash < 0 /*&& File.separatorChar != sepChar*/
         && url.getProtocol ().equals ("file"))
       {
         // On Windows, even '\' is allowed in a "file" URL.
         /*sepChar = File.separatorChar;*/
         lastSlash = file.lastIndexOf (sepChar);
       }
     
				if (lastSlash == -1) file = file + sepChar + spec.substring(start,end);
     else file = file.substring(0, lastSlash)
             + sepChar + spec.substring (start, end);

     if (url.getProtocol ().equals ("file"))
       {
         // For "file" URLs constructed relative to a context, we
         // need to canonicalise the file path.
         try
           {
             file = new File (file).getCanonicalPath ();
           }
         catch (IOException e)
           {
           }
       }

	ref = null;
   }

 if (ref == null)
   {
	// Normally there should be no '#' in the file part,
	// but we are nice.
	int hash = file.indexOf('#');
	if (hash != -1)
	  {
	    ref = file.substring(hash + 1, file.length());
	    file = file.substring(0, hash);
	  }
   }

 // XXX - Classpath used to call PlatformHelper.toCanonicalForm() on
 // the file part. It seems like overhead, but supposedly there is some
 // benefit in windows based systems (it also lowercased the string).

 setURL(url, url.getProtocol(), host, port, file, ref);
}

/**
* This methods sets the instance variables representing the various fields
* of the URL to the values passed in.
*
* @param u The URL to modify
* @param protocol The protocol to set
* @param host The host name to et
* @param port The port number to set
* @param file The filename to set
* @param ref The reference
*
* @exception SecurityException If the protocol handler of the URL is
* different from this one
*
* @deprecated 1.2 Please use
* #setURL(URL,String,String,int,String,String,String,String);
*/
protected void setURL(URL u, String protocol, String host, int port,
			String file, String ref)
{
 u.set(protocol, host, port, file, ref);
}


//##################################################################
}
//##################################################################

