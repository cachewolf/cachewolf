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
package CacheWolf.navi;

import CacheWolf.database.CWPoint;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.utils.MyLocale;

/**
 * Class to load the parameters of a datum shift of a map and
 * the projection parameters from an Inputstream by the corresponding
 * EPSG code
 * After instantiation you can simply use to and fromWGS84 to
 * convert between WGS84 and the given Coordinate reference system, given
 * by the EPSG code
 * Start offset in the language file: 4920
 *
 * @author Pfeffer
 */
public final class TransformCoordinatesProperties {

    /**
     * return ll transformed into the desired coordinate reference system<br>
     * if the projection is Gau�-Kr�ger, easting will be put in lonDec and northing in latDec
     *
     * @param ll
     * @param epsgCode
     * @return
     */
    public final static CoordinatePoint fromWgs84(CoordinatePoint ll, int epsgCode) {
        CoordinatePoint ret = null;
        switch (epsgCode) {
            case TransformCoordinates.EPSG_WGS84:
                ret = ll;
                break;
            case TransformCoordinates.EPSG_Mercator_1SP_Google:
                ret = new CoordinatePoint();
                ret.lonDec = ll.lonDec * 20037508.34 / 180;
                double y = Math.log(Math.tan((90 + ll.latDec) * Math.PI / 360)) / (Math.PI / 180);
                ret.latDec = y * 20037508.34 / 180;
                break;
        }
        if (ret == null) {
            int localsystem = TransformCoordinates.getLocalProjectionSystem(epsgCode);
            if (localsystem > 0) {
                ProjectedPoint xy = TransformCoordinates.wgs84ToEpsg(ll, epsgCode);
                ret = xy.toCoordinatePoint();
            } else {
                throw new IllegalArgumentException(MyLocale.getMsg(4923, "fromWgs84: EPSG code ") + epsgCode + MyLocale.getMsg(4921, " not supported"));
            }
        }
        return ret;
    }

    /**
     * convert any supported coordinate reference system to WGS84<br>
     * if p is a Gau�-Kr�ger point, put latdec = northing, londec = easting
     *
     * @param p
     * @param epsgCode
     * @return
     */
    public final static CWPoint toWgs84(CWPoint p, int epsgCode) {
        switch (epsgCode) {
            case TransformCoordinates.EPSG_WGS84:
                return p;
            case TransformCoordinates.EPSG_Mercator_1SP_Google:
                CWPoint ret = new CWPoint();
                ret.lonDec = (p.lonDec / 20037508.34) * 180;
                double lat = (p.latDec / 20037508.34) * 180;
                ret.latDec = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
                return ret;
            default:
                Projection projection = ProjectedPoint.getProjection(epsgCode);
                ProjectedPoint lp = new ProjectedPoint(projection);
                projection.set(p.latDec, p.lonDec, lp);
                CWPoint ll = projection.unproject(lp);

                int ls = TransformCoordinates.getLocalProjectionSystem(epsgCode);
                TransformParameters transparams = TransformCoordinates.getTransParams(lp, ls);

                if (transparams == TransformCoordinates.NO_DATUM_SHIFT)
                    return ll;
                else {
                    XyzCoordinates xyzorig = TransformCoordinates.latLon2xyz(ll, 0, transparams.ellip);
                    XyzCoordinates xyzwgs84 = TransformCoordinates.transform(xyzorig, transparams);
                    return TransformCoordinates.xyz2Latlon(xyzwgs84, TransformCoordinates.WGS84);
                }
                // return TransformCoordinates.ProjectedEpsgToWgs84(new ProjectedPoint(p, epsgCode, true, false), epsgCode);
        }
    }
}