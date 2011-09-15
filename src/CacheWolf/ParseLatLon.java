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

import ewe.sys.Convert;

/**
*	This class parses a string representation of longitude and latitude.
*	The result is a double value representing longitude and another representing
*	latitude (DD.dddd)
*/
public class ParseLatLon {
	
	String latlon;	
	/**
	*	After a calling the method parse()
	*	this variable holds the double value of latitude
	*/
	public double lat2;
	/**
	*	After a calling the method parse()
	*	this variable holds the double value of longitude
	*/
	public double lon2;

	private char digSep;

	/**
	*	Constructor to parse a lat lon string like:
	*	N 49 33.167 E 011 21.608
	*/
	public ParseLatLon (String ll){
		latlon = ll;
		digSep=MyLocale.getDigSeparator().charAt(0);
	}
	
	/**
	*	Constructor to parse a lat lon string like:
	*	N 49 33.167 E 011 21.608.
	*	Additionally you may pass the decimal symbol, i.e. "." or ","
	*/
	public ParseLatLon (String ll, String tr){
		latlon = ll;
		digSep=MyLocale.getDigSeparator().charAt(0);
	}
	
	private int start;
	private int end;
	
	/** Get the next non-blank part of the latlon String */
	String getNext() {
        start=end;
		while (latlon.charAt(start)==' ')start++; // skip blanks
        end=start;
        while (latlon.charAt(end)!=' ') end++; // collect non-blanks
		return latlon.substring(start,end);
	}
	
	/**
	* Parse a string that contains lat lon into it's lat and lon doubles. Class
	* variable latlon must have been set befor you call this method.
	*/
	public void parse() throws NumberFormatException {
		if (digSep==',') 
			latlon = latlon.replace('.', ',')+" ";
		else
			latlon = latlon.replace(',', '.')+" ";
		try {
			end=0;
			String latNS=getNext();
			String latDeg=getNext();
			String latMin=getNext();
			String lonEW=getNext();
			String lonDeg=getNext();
			String lonMin=getNext();
			if (lonDeg.endsWith("°")){
				lonDeg = lonDeg.substring(0,lonDeg.length()-1); // remove °
			}
			if (latDeg.endsWith("°")){
				latDeg = latDeg.substring(0,latDeg.length()-1);// remove °
			}
			lat2 = Convert.parseDouble(latDeg) + Convert.parseDouble(latMin)/60.0;
			if(latNS.charAt(0)=='S') lat2= -lat2 ;
			lon2 = Convert.parseDouble(lonDeg) + Convert.parseDouble(lonMin)/60.0;
			if(lonEW.charAt(0)=='W') lon2 = -lon2;
		} catch (Exception e) { 
			throw new NumberFormatException("Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"); 
		}

	}
}
