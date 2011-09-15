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

import CacheWolf.CWPoint;
import CacheWolf.MyLocale;

/**
 * Class to load the parameters of a datum shift of a map and
 * the projection parameters from an Inputstream by the corresponding
 * EPSG code
 * After instantiation you can simply use to and fromWGS84 to
 * convert between WGS84 and the given Coordinate reference system, given
 * by the EPSG code
 * Start offset in the language file: 4920  
 * @author Pfeffer
 *
 */
public final class TransformCoordinatesProperties {

	/**
	 * return ll transformed into the desired coordinate reference system
	 * if the prjection is Gauß-Krüger, easting will be put in lonDec and
	 * northing in latDec
	 * @param ll
	 * @return
	 */
	public final static TrackPoint fromWgs84(TrackPoint ll, int epsgCode) {
		TrackPoint ret = null;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = ll;
		}
		if (ret == null) {
			int localsystem = TransformCoordinates.getLocalProjectionSystem(epsgCode);
			if (localsystem > 0) {
				ProjectedPoint xy = TransformCoordinates.wgs84ToEpsg(ll, epsgCode);
				ret = xy.toTrackPoint(localsystem);
			} else {
				throw new IllegalArgumentException(
						MyLocale.getMsg(4923, "fromWgs84: EPSG code ") 
						+ epsgCode 
						+ MyLocale.getMsg(4921, " not supported"));
			}
		}
		return ret;
	}

	/**
	 * convert any supported coordinate reference system WGS84
	 * if p is a Gauß-Krüger point, put latdec = northing, londec = easting 
	 * @param p
	 * @return
	 */
	public final static CWPoint toWgs84(CWPoint p, int epsgCode) {
		CWPoint ret = null;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = p;
			break;
		}
		if (ret == null) {
			ProjectedPoint xy = new ProjectedPoint(p, epsgCode, true, false);
			ret = TransformCoordinates.ProjectedEpsgToWgs84(xy, epsgCode);
		}
		return ret;
	}
}