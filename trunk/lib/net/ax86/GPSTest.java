package net.ax86;

import ewe.sys.*;

import net.ax86.GPSException;
import net.ax86.GPS;
import org.json.*;

/**
 * Quick and dirty (but convenient) test class/program for the {@link GPS}
 * class.
 *
 * @author Tilman Blumenbach
 */
public class GPSTest
{
	public static void main( String args[] )
	{
		JSONObject JSON = null, two;
		JSONArray arr;
		GPS g = null;
		int maj = 0, min = 0;

		Vm.startEwe(args);

		try
		{
			g = new GPS( "127.0.0.1" );
			g.stream( GPS.WATCH_ENABLE );
			JSON = g.read();
		}
		catch( Exception e )
		{
			Vm.debug( "Could not connect to gpsd: " + e.getMessage() );
			Vm.exit(1);
		}

		try
		{
			maj = JSON.getInt( "proto_major" );
			min = JSON.getInt( "proto_minor" );
		}
		catch( JSONException e )
		{
			Vm.debug( "JSON error: " + e.getMessage() );
			Vm.exit(1);
		}
		
		Vm.debug( "Major: " + maj + ", minor: " + min );

		try
		{
			while( true )
			{
				// Make sure we have enough data:
				//Vm.debug( ">> POLLING!" );

				JSON = g.read();
				Vm.debug( ">> Got JSON [" + JSON.getString( "class" ) + "]: " );
				Vm.debug( ">>> " + JSON.toString() );

				if( JSON.getString( "class" ).equals( "DEVICE" ) &&
				    JSON.has( "activated" ) && JSON.getDouble( "activated" ) != 0 )
				{
					Vm.debug( ">> Keeping up." );
					g.stream( GPS.WATCH_ENABLE );
				}
				else if( JSON.getString( "class" ).equals( "POLL" ) )
				{
					arr = JSON.getJSONArray( "fixes" );
					for( int i = 0; i < arr.length(); i++ )
					{
						two = arr.getJSONObject( i );
						if( two.has( "lat" ) && two.has( "lon" ) )
						{
							Vm.debug( ">> LAT: " + two.getDouble( "lat" ) +
									" LON: " + two.getDouble( "lon" ) );
						}
					}
				}

				g.poll();
				Thread.sleep( 3000 );
			}
		}
		catch( Exception e )
		{
			Vm.debug( "EOF?" );
			e.printStackTrace();
			Vm.exit(1);
		}

		Vm.exit(0);
	}
}
