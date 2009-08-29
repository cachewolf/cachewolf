package CacheWolf.navi;

import CacheWolf.CWPoint;

public final class ProjectedPoint {

	public static final LambertProjection PJ_AUSTRIAN_LAMBERT_OLD 	= new LambertProjection(TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_OLD, TransformCoordinates.BESSEL);
	public static final LambertProjection PJ_AUSTRIAN_LAMBERT_NEW 	= new LambertProjection(TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_NEW,  TransformCoordinates.WGS84);
	public static final LambertProjection PJ_FRENCH_LAMBERT_NTF_II 	= new LambertProjection(TransformCoordinates.EPSG_FRENCH_LAMBERT_NTF_II, TransformCoordinates.CLARKE1880IGN);
	public static final LambertProjection PJ_TEST 	                = new LambertProjection(TransformCoordinates.EPSG_TEST, TransformCoordinates.CLARKE1866); 
	{ 
		PJ_AUSTRIAN_LAMBERT_OLD.setup ( 400000, 400000, 49.0, 46.0, 1         , 47.5, 13.333333); // actually this should be done inside the constructor. But Ewe doesn't support more than 8 parameters (at least for constructors)
		PJ_AUSTRIAN_LAMBERT_NEW.setup ( 400000, 400000, 49.0, 46.0, 1         , 47.5, 13.333333);
		PJ_FRENCH_LAMBERT_NTF_II.setup(2200000, 600000, 46.8, 46.8, 0.99987742, 46.8, 2.337229172 /*(2+20/60+14.025/3600) */); 
		PJ_TEST.setup( 150000, 250000, 18, 18, 1         , 18, -77); 
	}
	public static final GkProjection PJ_GERMAN_GK  = new GkProjection(TransformCoordinates.EPSG_GERMAN_GK2 -2           , 0, 500000, 3, 1000000, 0, 1     , TransformCoordinates.BESSEL);
	public static final GkProjection PJ_ITALIAN_GB = new GkProjection(TransformCoordinates.EPSG_ITALIAN_GB_EW1 -1, 0, 500000, 6, 1000000, 0, 0.9996, TransformCoordinates.HAYFORD1909);
	public static final UTMProjection PJ_UTM_WGS84 = new UTMProjection(TransformCoordinates.WGS84);

	protected double northing; // TODO make these private
	protected double easting; // because it is not clear for routines from outside if the stripe number is included, make this available only through methods
	protected int zone;
	public Projection projection;

	public ProjectedPoint() { super(); }

	public ProjectedPoint(Projection p) {
		projection = p;
	}

	public ProjectedPoint(ProjectedPoint pp) {
		northing   = pp.northing;
		easting    = pp.easting;
		zone       = pp.zone;
		projection = pp.projection;
	}

	public ProjectedPoint(CWPoint wgs84, Projection projection_) {
		projection = projection_;
		projection.project(wgs84, this);
	}


	/** 
	 * automatically projects wgs84 onto epsg OR
	 * creates an ProjectedPoint with <br>
	 * lat/lon = northin/easting (in local notaion)
	 * 
	 * @param p Point to be projected OR lat/lon = northing/easting
	 * @param epsg_localsystem EPSG-Code OR ProjecetPoint.LOCALSYSTEM_XXX
	 * @param isProjected if true, p contains northing in lat and easting in lon <br>
	 * if false p will be projected to epsg
	 */
	public ProjectedPoint(CWPoint p, int epsg_localsystem, boolean isProjected, boolean isLocalsystem) {
		if (isProjected) set(p, null, epsg_localsystem, isLocalsystem); 
		else {
			projection = (isLocalsystem ? getProjectionFromLs(epsg_localsystem) : getProjection(epsg_localsystem) );
			if (isLocalsystem)	projection.project(p, this);
			else				projection.project(p, this, epsg_localsystem); // the epsg is requiered here because each zone has a different epsg, so the zone is already fixed
		}
	}
	
	public ProjectedPoint(CWPoint p, String zone, int epsg_localsystem, boolean isLocalsystem) {
		set(p, zone, epsg_localsystem, isLocalsystem); 
	}
	



	public static Projection getProjection(int epsg) {
		switch (epsg) {
		case TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_OLD: return PJ_AUSTRIAN_LAMBERT_OLD;
		case TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_NEW: return PJ_AUSTRIAN_LAMBERT_NEW;
		case TransformCoordinates.EPSG_GERMAN_GK2: 
		case TransformCoordinates.EPSG_GERMAN_GK3: 
		case TransformCoordinates.EPSG_GERMAN_GK4: return PJ_GERMAN_GK;
		case TransformCoordinates.EPSG_ITALIAN_GB_EW1:
		case TransformCoordinates.EPSG_ITALIAN_GB_EW2: return PJ_ITALIAN_GB;
		case TransformCoordinates.EPSG_FRENCH_LAMBERT_NTF_II : return PJ_FRENCH_LAMBERT_NTF_II;
		default: throw new IllegalArgumentException("ProjectedPoint.getProjection: epsg-code: " + epsg + "not supported");
		}
	}
	
	public static Projection getProjectionFromLs(int localsystem) {
		switch (localsystem) {
		case TransformCoordinates.LOCALSYSTEM_AUSTRIAN_LAMBERT_OLD:	return PJ_AUSTRIAN_LAMBERT_OLD;
		case TransformCoordinates.LOCALSYSTEM_AUSTRIAN_LAMBERT_NEW:	return PJ_AUSTRIAN_LAMBERT_NEW;
		case TransformCoordinates.LOCALSYSTEM_GERMAN_GK:	          	return PJ_GERMAN_GK; 
		case TransformCoordinates.LOCALSYSTEM_ITALIAN_GB:			return PJ_ITALIAN_GB;
		case TransformCoordinates.LOCALSYSTEM_FRANCE_LAMBERT_IIE:		return PJ_FRENCH_LAMBERT_NTF_II;
		case TransformCoordinates.LOCALSYSTEM_UTM_WGS84:				return PJ_UTM_WGS84;
		default: throw new IllegalArgumentException("ProjectedPoint(CWPoint, int): region "+localsystem+" not supported");
		}
	}


	/**
	 * 
	 * @param northing: raw, without false northing, e.g. can be negative
	 * @param easting
	 * @param pj
	 */

	/*	
	public ProjectedPoint(double northing_, double easting_, Projection pj) {
		northing = northing_;
		easting = easting_;
		projection = pj;
	}
	 */
	public double getNorthing() {
		return projection.getNorthing(this);
	}
	public double getEasting() {
		return projection.getEasting(this);
	}

	public ProjectedPoint cloneIt() {
		return new ProjectedPoint(this);
	}


	/**
	 * This will give you the normal projected (e.g.Gau�-Kr�ger) easting value
	 * (that means including the stripe number)
	 * @return
	 */
	public TrackPoint toTrackPoint(int region) {
		return new TrackPoint(getNorthing(), getEasting());
	}

	/**
	 * easting measured in meters from stripe middle
	 * @return
	 */
	public double getRawEasting() {
		return easting;
	}

	/**
	 * easting measured in meters from stripe middle
	 * @return
	 */
	public double getRawNorthing() {
		return northing;
	}

	public void setRaw(double northing_, double easting_) {
		northing = northing_;
		easting = easting_;
	}

	public void setzone(int z){
		zone = z;
	}

	public int getZone() {
		return zone;
	}
	
	public String getZoneString() {
		if (projection.zoneSeperately) return projection.getZone(this);
		else return "";
	}
	
	public int getEpsgCode() {
		return projection.getEpsgcode(this);
	}

	/**
	 * Set with local notation, incl. falsenorthing and -easting
	 * @param northing_ 
	 * @param easting_
	 * @param zone only put something here if the zone is not included in easting or northing and must be known, otherwise zone should be null
	 */
	public void set(double northing_, double easting_, String zone) {
		if (zone == null) projection.set(northing_, easting_, this);
		else              projection.set(northing_, easting_, zone, this);
	}

	/**
	 * Set with local notation, incl. falsenorthing and -easting
	 * @param northing_ 
	 * @param easting_
	 * @param zone only put something here if the zone is not included in easting or northing and must be known, otherwise zone should be null
	 */
	public void set(CWPoint projected, String zone, int epsg_localsystem, boolean isLocalsystem) {
		projection = (isLocalsystem ? getProjectionFromLs(epsg_localsystem) : getProjection(epsg_localsystem) );
		set(projected.latDec, projected.lonDec, zone);
	}

	public String toString() {
		return toString(2, "", " ");
	}
	
	public String toHumanReadableString() {
		return projection.toHumanReadableString(this);
	}

	public CWPoint unproject() {
		return projection.unproject(this);
	}

	public String toString(int decimalplaces, String prefix, String seperator) {
		ewe.sys.Double n = new ewe.sys.Double();
		ewe.sys.Double e = new ewe.sys.Double();
		n.set(projection.getNorthing(this));
		e.set(projection.getEasting(this));
		n.decimalPlaces = decimalplaces;
		e.decimalPlaces = decimalplaces;
		String z = (projection.zoneSeperately ? projection.getZone(this)+" " : "");
		return z + prefix + e.toString().replace(',', '.') + seperator + n.toString().replace(',', '.');
	}

	/**
	 * shift the point
	 * @param meters positive to north (east), negative to south (west)
	 * @param direction 0 north-south, 1 east-west
	 */
	public void shift(double meters, int direction) {
		switch (direction) { // TODO this works correctly only within a stripe/zone
		case 0: northing += meters; return;
		case 1: easting += meters; return;
		}
	}
}

