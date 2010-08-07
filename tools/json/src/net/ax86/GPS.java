package net.ax86;

import ewe.io.IOException;
import org.json.JSONException;

import ewe.io.InputStreamReader;
import ewe.io.BufferedReader;
import ewe.io.OutputStreamWriter;
import ewe.io.BufferedWriter;
import ewe.net.Socket;

import org.json.JSONObject;

/**
 * gpsd client library.
 *
 * This a simple library that allows low-level communication with the
 * GPS daemon.
 *
 * Things that are in C libgps but not in this library (or are done in a
 * different way):
 *  - GPS::stream():
 *     - Works with JSON data only. That means there is no option to change
 *       the reporting format (no WATCH_JSON, WATCH_NMEA, WATCH_RARE,
 *       WATCH_RAW flags).
 *     - Always uses the new protocol (no WATCH_NEWSTYLE, WATCH_OLDSTYLE
 *       flags).
 *     - Non-blocking mode is not yet implemented (no POLL_NONBLOCK flag).
 *       I think this is the only real limitation of this implementation.
 *
 * Copyright (c) 2010 by Tilman Blumenbach.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author Tilman Blumenbach
 * @version 0.9_ewe
 *
 * @see <a href="http://gpsd.berlios.de/gpsd.html">gpsd manpage for more information</a>
 */
public class GPS
{
	/**
	 * The major version of this API (NOT the gpsd API!).
	 */
	public static final int API_MAJOR     = 0;
	/**
	 * The minor version of this API (NOT the gpsd API!).
	 */
	public static final int API_MINOR     = 9;

	/**
	 * Disable watcher mode and other given WATCH_* flags.
	 *
	 * @see #stream( int )
	 */
	public static final int WATCH_DISABLE = 1<<0;
	/**
	 * Enable watcher mode and other given WATCH_* flags.
	 *
	 * @see #stream( int )
	 */
	public static final int WATCH_ENABLE  = 1<<1;
	/**
	 * When reporting AIS data, scale integer quantities to floats if
	 * they have a divisor or rendering formula assosiated with them.
	 *
	 * @see #stream( int )
	 */
	public static final int WATCH_SCALED  = 1<<2;
	/**
	 * Restrict watching to a speciied device, patch given as second
	 * argument to {@link #stream( int, String ) stream()}.
	 *
	 * @see #stream( int, String )
	 */
	public static final int WATCH_DEVICE  = 1<<3;

	/**
	 * DEVICE flags: GPS data has been seen on this device.
	 */
	public static final int SEEN_GPS      = 0x01;
	/**
	 * DEVICE flags: RTCM2 data has been seen on this device.
	 */
	public static final int SEEN_RTCM2    = 0x02;
	/**
	 * DEVICE flags: RTCM3 data has been seen on this device.
	 */
	public static final int SEEN_RTCM3    = 0x04;
	/**
	 * DEVICE flags: AIS data has been seen on this device.
	 */
	public static final int SEEN_AIS      = 0x08;

	/**
	 * The Socket used for communication with gpsd.
	 */
	private Socket sock;
	/**
	 * BufferedReader for the {@link #sock Socket}.
	 *
	 * @see #sock
	 */
	protected BufferedReader in;
	/**
	 * BufferedWriter for the {@link #sock Socket}.
	 *
	 * @see #sock
	 */
	protected BufferedWriter out;

	/**
	 * A {@link GPSHook} object used for certain callbacks.
	 *
	 * @see GPSHook
	 * @see #setHook( GPSHook )
	 */
	protected GPSHook hook = null;


	/**
	 * Constructor: Creates a new GPS object and connects its
	 * {@link #sock Socket} to a gpsd.
	 *
	 * @param host Host name or IP address to connect to.
	 * @param port Port to connect to.
	 *
	 * @throws IOException E. g. on a connection failure.
	 *
	 * @see #cleanup() Call the <code>cleanup()</code> method when you
	 *                 do not need this object anymore.
	 */
	public GPS( String host, int port )
		throws IOException
	{
		this.sock = new Socket( host, port );
		this.in   = new BufferedReader( new InputStreamReader( this.sock.getInputStream() ) );
		this.out  = new BufferedWriter( new OutputStreamWriter( this.sock.getOutputStream() ) );
	}


	/**
	 * Constructor: Creates a new GPS object and connects its
	 * {@link #sock Socket} to a gpsd.
	 *
	 * This simply calls {@link #constructor( String, int )} with the
	 * default gpsd port 2947.
	 *
	 * @param host Host name or IP address to connect to.
	 *
	 * @throws IOException E. g. on a connection failure.
	 *
	 * @see #constructor( String, int )
	 * @see #cleanup() Call the <code>cleanup()</code> method when you
	 *                 do not need this object anymore.
	 */
	public GPS( String host )
		throws IOException
	{
		this( host, 2947 );
	}


	/**
	 * Sends data to gpsd.
	 *
	 * @param data The data to send.
	 *
	 * @throws IOException When data could not be written to the stream.
	 */
	public void send( String data )
		throws IOException
	{
		this.out.write( data, 0, data.length() );
		// Make sure it gets through:
		this.out.flush();
	}


	/**
	 * Asks gpsd for data from the last-seen fixes on all active GPS devices.
	 *
	 * This is a very simple method that just calls
	 * {@link #send( String )} to send a "POLL" command to the
	 * server.
	 * 
	 * @throws IOException When data could not be written to the stream.
	 * 
	 * @see #send( String )
	 */
	public void poll()
		throws IOException
	{
		this.send( "?POLL;" );
	}


	/**
	 * Gets the next {@linkplain JSONObject JSON object} from gpsd.
	 *
	 * This call blocks when there is no data available. Use
	 * {@link #waiting()} to check if there is data waiting.
	 *
	 * If there is a {@linkplain GPSHook hook} set, its
	 * {@link GPSHook#onRawData( JSONObject, String )} method will be called.
	 *
	 * @return A {@link JSONObject} containing the data received from gpsd.
	 * 
	 * @throws IOException On a stream reading failure.
	 * @throws JSONException On a JSON parsing failure (e. g. syntax error).
	 *
	 * @see JSONObject
	 * @see #waiting()
	 * @see GPSHook#onRawData( JSONObject, String )
	 */
	public JSONObject read()
		throws IOException, JSONException
	{
		String rawJSON = this.in.readLine();
		JSONObject parsedJSON;

		if( rawJSON == null )
		{
			throw new IOException( "End Of Stream reached" );
		}

		parsedJSON = new JSONObject( rawJSON );

		// Call our hook, if set:
		if( this.hook != null )
		{
			this.hook.onRawData( parsedJSON, rawJSON );
		}

		return parsedJSON;
	}


	/**
	 * Checks whether there is data from gpsd to be {@linkplain #read() read}.
	 *
	 * Tblue> TODO: This does not seem to work correctly in Ewe (but it
	 *              does with Sun's JRE). Ewe bug?
	 * 
	 * @return True if there is data to be read, false otherwise.
	 */
	public boolean waiting()
	{
		try
		{
			return this.in.ready();
		}
		catch( IOException ignored )
		{
			return false;
		}
	}


	/**
	 * Enables/tunes gpsd watcher mode.
	 *
	 * @param flags Flags describing the action(s) to take.
	 * @param arg   A String treated as an argument to certain flags.
	 *
	 * @throws IOException E. g. on a {@link #sock Stream} write error.
	 * @throws JSONException If we made a huge mistake like passing a
	 *                       <code>null</code> String to one of the
	 *                       {@link JSONObject#put()} methods. Should not
	 *                       happen.
	 * @throws GPSException If <code>arg</code> is <code>null</code> but
	 *                      is needed by a flag.
	 *
	 * @see #WATCH_DISABLE
	 * @see #WATCH_ENABLE
	 * @see #WATCH_SCALED
	 * @see #WATCH_DEVICE
	 */
	public void stream( int flags, String arg )
		throws IOException, JSONException, GPSException
	{
		JSONObject GPSData = new JSONObject();

		GPSData.put( "class", "WATCH" );

		if( ( flags & WATCH_ENABLE ) != 0 )
		{
			GPSData.put( "enable", true );

			if( ( flags & WATCH_SCALED ) != 0 )
			{
				GPSData.put( "scaled", true );
			}
			if( ( flags & WATCH_DEVICE ) != 0 )
			{
				if( arg == null )
				{
					throw new GPSException( "No argument provided with WATCH_DEVICE" );
				}

				GPSData.put( "device", arg );
			}
		}
		else // WATCH_DISABLE?
		{
			GPSData.put( "enable", false );

			if( ( flags & WATCH_SCALED ) != 0 )
			{
				GPSData.put( "scaled", false );
			}
		}

		// Send request to the server:
		this.send( "?WATCH=" + GPSData.toString() + ";" );
	}


	/**
	 * Enables/tunes gpsd watcher mode.
	 *
	 * This simply calls {@link #stream( int, String )} with an <code>arg</code>
	 * parameter of <code>null</code>.
	 *
	 * @param int Flags describing the action(s) to take.
	 *
	 * @throws IOException E. g. on a {@link #sock Stream} write error.
	 * @throws JSONException If we made a huge mistake like passing a
	 *                       <code>null</code> String to one of the
	 *                       {@link JSONObject#put()} methods. Should not
	 *                       happen.
	 * @throws GPSException If <code>arg</code> is <code>null</code> but
	 *                      is needed by a flag.
	 *
	 * @see #stream( int, String ) stream( int, String ) for information
	 *                             about flags.
	 */
	public void stream( int flags )
		throws IOException, JSONException, GPSException
	{
		this.stream( flags, null );
	}


	/**
	 * Sets a {@linkplain GPSHook hook} to call on certain events.
	 *
	 * @param hook The {@link GPSHook} to set.
	 * @see GPSHook
	 */
	public void setHook( GPSHook hook )
	{
		this.hook = hook;
	}


	/**
	 * "Destructor": Does the final clean up.
	 *
	 * Call this method when you do not this object anymore.
	 */
	public void cleanup()
	{
		// Close our socket. Also closes its Input and Output streams.
		this.sock.close();
	}
}
