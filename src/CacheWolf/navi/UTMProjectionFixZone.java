package CacheWolf.navi;

import CacheWolf.database.CWPoint;

public class UTMProjectionFixZone extends UTMProjection {

    // ctor
    public UTMProjectionFixZone(Ellipsoid el) {
	super(el);
	zoneSeperately = false;
	epsgCode = 0; // =0 causes Projected Point to set the epsgCode before calling set() which is necessary to be able to determine the stripe
    }

    //Overrides: project(...) in UTMProjection
    public ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp, int epsg) {
	int stripe = getStripeByEpsg(epsg) - 1; // official stripes start by 1, we internally start by 0
	if (stripe == Integer.MAX_VALUE)
	    throw new UnsupportedOperationException("UTMProjectionFixZone.project: project by epsg-code " + epsg + " not supported");
	if (stripe < 0)
	    stripe += 60;
	GkProjection.project(wgs84, ellip, 6, (stripe >= 30 ? stripe - 30 : stripe + 30), 3, 0.9996, pp);
	pp.zone = stripe + (int) (Math.floor((wgs84.latDec) / 8) + 13) * 200;
	return pp;
    }

    //Overrides: project(...) in UTMProjection
    public ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp) {
	return project(wgs84, pp, epsgCode);
    }

    //Overrides: set(...) in Projection
    public ProjectedPoint set(double northing, double easting, ProjectedPoint pp) {
	if (epsgCode == 0)
	    throw new UnsupportedOperationException("UTMProjectionFixZone.set: set() requires zone/epsg code, set projection.epsgcode first");
	int stripe = getStripeByEpsg(epsgCode);
	int zoneletterNumber = (int) ((northing + 10000000) / 1000000); // calc from northing
	return set(northing, easting, stripe, zoneletterNumber, pp);
    }

    /**
     * returns the UTM Stripe number, and Integer.MAX_VALUE if the given epsg code is not supported
     * It returns the official stripe number which start with 1 - remark: internally we start with stripe 0.
     * @param epsg
     * @return
     */
    private int getStripeByEpsg(int epsg) {
	if (epsg >= 25828 && epsg <= 25838)
	    return epsg - 25800;
	switch (epsg) {
	case TransformCoordinates.EPSG_SwedenUTM:
	    return 33;
	default:
	    throw new IllegalArgumentException("UTMProjectionFixZone.getStripeByEpsg: epsg-code " + epsgCode + " not supported");
	}
    }

    //Overrides: getEpsgcode(...) in Projection
    public int getEpsgcode(int localsystem) {
	switch (localsystem) {
	case TransformCoordinates.LOCALSYSTEM_SWEDEN:
	    return TransformCoordinates.EPSG_SwedenUTM;
	case TransformCoordinates.LOCALSYSTEM_UTM28Nto38N:
	    return TransformCoordinates.EPSG_25828to25838;
	default:
	    throw new IllegalArgumentException("UTMProjectionFixZone: local system: " + localsystem + " not implemented.");
	}
    }

}
