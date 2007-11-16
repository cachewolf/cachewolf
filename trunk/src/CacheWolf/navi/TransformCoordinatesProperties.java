package CacheWolf.navi;

import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.sys.Convert;
import ewe.util.Properties;
import CacheWolf.CWPoint;

public class TransformCoordinatesProperties extends Properties {
	public int epsgCode;
	
	public TransformCoordinatesProperties(InputStream is) throws IOException {
		super();
		load(is);
		epsgCode = Convert.toInt(getProperty("EpsgCode", "-1"));
		if (epsgCode == -1) throw new IllegalArgumentException("EPSG code missing in: " + is.getName());
	}
	
	public TransformCoordinatesProperties(int epsgcodei) {
		if (!TransformCoordinates.isSupported(epsgcodei)) throw new IllegalArgumentException("EPSG code " + epsgcodei + "not supported");
		epsgCode = epsgcodei;
	}

	/**
	 * return ll transformed into the desired coordinate reference system
	 * if the prjection is Gauß-Krüger, easting will be put in lonDec and
	 * northing in latDec
	 * @param ll
	 * @return
	 */
	public static TrackPoint fromWgs84(TrackPoint ll, int epsgCode) {
		TrackPoint ret;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = ll;
			break;
		case TransformCoordinates.EPSG_GK2:
		case TransformCoordinates.EPSG_GK3:
		case TransformCoordinates.EPSG_GK4:
		case TransformCoordinates.EPSG_GK5:
			GkPoint xy = TransformCoordinates.wgs84ToGermanGk(ll, epsgCode);
			ret = new CWPoint(xy.northing, xy.getGkEasting());
			break;
		default: throw new IllegalArgumentException("fromWgs84: EPSG code " + epsgCode + "not supported");
		}
		return ret;
	}

	/**
	 * convert any supported coordinate reference system WGS84
	 * if p is a Gauß-Krüger point, put latdec = northing, londec = easting 
	 * @param p
	 * @return
	 */
	public static CWPoint toWgs84(CWPoint p, int epsgCode) {
		CWPoint ret;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = p;
			break;
		case TransformCoordinates.EPSG_GK2:
		case TransformCoordinates.EPSG_GK3:
		case TransformCoordinates.EPSG_GK4:
		case TransformCoordinates.EPSG_GK5:
			GkPoint xy = new GkPoint(p.lonDec, p.latDec);
			ret = TransformCoordinates.germanGkToWgs84(xy);
			break;
		default: throw new IllegalArgumentException("ToWgs84: EPSG code " + epsgCode + "not supported");
		}
		return ret;
	}
}