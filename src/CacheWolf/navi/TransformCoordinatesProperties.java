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