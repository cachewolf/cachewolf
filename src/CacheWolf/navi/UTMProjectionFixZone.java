package CacheWolf.navi;

import CacheWolf.CWPoint;

public class UTMProjectionFixZone extends UTMProjection {

	public UTMProjectionFixZone(Ellipsoid el) {
		super(el);
		zoneSeperately = false;
		epsgCode = 0; // =0 causes Projected Point to set the epsgCode before calling set() which is necessary to be able to determine the stripe
	}

	public ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp, int epsg) {
		int stripe = getStripeByEpsg(epsg) -1; // official stripes start by 1, we internally start by 0
		if (stripe == Integer.MAX_VALUE) throw new UnsupportedOperationException("UTMProjectionFixZone.project: project by epsg-code " + epsg + " not supported");
		if (stripe < 0) stripe += 60;
		GkProjection.project(wgs84, ellip, 6, (stripe >= 30 ? stripe -30 : stripe +30), 3, 0.9996, pp);
		pp.zone = stripe;
		pp.zone += (int) (Math.floor((wgs84.latDec)/8)+13) * 200; // zone letter
		return pp;
	}
	
	public ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp) {
		return project(wgs84, pp, epsgCode);
	}
	
	public ProjectedPoint set(double northing, double easting, ProjectedPoint pp) {
		if (epsgCode == 0)	throw new UnsupportedOperationException("UTMProjectionFixZone.set: set() requires zone/epsg code, set projection.epsgcode first");
		int stripe = getStripeByEpsg(epsgCode);
		if (stripe == Integer.MAX_VALUE) throw new UnsupportedOperationException("UTMProjectionFixZone.set: set by epsg-code " + epsgCode + " not supported");
		return set(northing, easting, stripe, 0, pp);
	}

	/**
	 * returns the UTM Stripe number, and Integer.MAX_VALUE if the given epsg code is not supported
	 * It returns the official stripe number which start with 1 - remark: internally we start with stripe 0.
	 * @param epsg
	 * @return
	 */
	public int getStripeByEpsg(int epsg) {
		switch (epsg) {
		case TransformCoordinates.EPSG_SwedenUTM:  return 33;
		case TransformCoordinates.EPSG_DenmarkUTM: return 32;
		default: return Integer.MAX_VALUE;
		}
	}
	
	public int getEpsgcode(int localsystem) {
		switch (localsystem) {
		case TransformCoordinates.LOCALSYSTEM_SWEDEN:  return TransformCoordinates.EPSG_SwedenUTM; 
		case TransformCoordinates.LOCALSYSTEM_DENMARK: return TransformCoordinates.EPSG_DenmarkUTM; 
		default:
			throw new IllegalArgumentException("UTMProjectionFixZone: local system: " + localsystem + " not implemented.");
		}
	}
	
}
