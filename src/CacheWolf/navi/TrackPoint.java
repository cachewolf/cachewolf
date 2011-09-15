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
package CacheWolf.navi;

/**
 * this is not CWPoint because it should be as small as possible
 * @author pfeffer
 *
 */

public class TrackPoint  {
	public double latDec;
	public double lonDec;
	
	public TrackPoint(){
		latDec = -91;
		lonDec = -361;
	}
	
	public TrackPoint(TrackPoint t) {
		latDec = t.latDec;
		lonDec = t.lonDec;
	}
	public TrackPoint(double lat, double lon) {
		latDec = lat;
		lonDec = lon;
	}
	public boolean equals(TrackPoint tp) {
		return latDec == tp.latDec && lonDec == tp.lonDec;
	}
	
	/**
	 * Returns true if the coordinates are valid
	 */
	public boolean isValid() {
		return 	latDec <= 90.0 && latDec >= -90.0 &&
				lonDec <= 360 && lonDec >= -360;
	}


}

