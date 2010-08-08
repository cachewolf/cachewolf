package net.ax86;

import org.json.JSONObject;

/**
 * Hook interface supposed to be implemented by classes who want to be
 * used as a "callback" on certain {@link GPS} events.
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
 * @version 1.0
 *
 * @see GPS
 * @see GPS#setHook( GPSHook )
 */
public interface GPSHook
{
	/**
	 * Gets called e. g. by {@link GPS#read()}.
	 *
	 * @param JSONObject The parsed JSON data received from gpsd.
	 * @param String     The raw (unparsed) JSON data from gpsd.
	 *
	 * @see GPS#read()
	 * @see JSONObject
	 */
	public void onRawData( JSONObject parsedData, String rawData );
}
