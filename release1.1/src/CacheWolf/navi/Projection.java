package CacheWolf.navi;

import CacheWolf.CWPoint;

abstract class Projection{
	/** when implemented: <br>
	 * a) if the projection covers only one epsgCode here it should be written <br>
	 * b) if the projection covers several zones which have different epsg codes
	 * put here the figure which must be added to the zone number to get the 
	 * corresponding epsg-codes (see method getEpsgcode)
	 */
	public int epsgCode;
	public boolean zoneSeperately = false;

	/**
	 * The zone is automatically determined
	 * rember to set the zone in pp when you implement this method 
	 * @param ll
	 * @param pp: pp will be filled with the projected ll. If null, a new ProjectedPoint will be created
	 * @return
	 */
	public abstract ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp);

	/**
	 * Zone is fixed by epsg-code
	 * @param ll
	 * @param pp
	 * @param epsg
	 * @return
	 */
	public abstract ProjectedPoint project(CWPoint ll, ProjectedPoint pp, int epsg);
	public abstract CWPoint unproject(ProjectedPoint pp);
	/**
	 * Returns the projected Northing in local notation
	 * @param pp
	 * @return
	 */
	public abstract double getNorthing(ProjectedPoint pp);
	public abstract double getEasting(ProjectedPoint pp);
	public int getEpsgcode(ProjectedPoint pp) {
		return epsgCode + pp.zone;
	}
	public abstract ProjectedPoint set(double northing, double easting, ProjectedPoint pp);
	public ProjectedPoint set(double northing, double easting, String zone, ProjectedPoint pp) {
		throw new UnsupportedOperationException("Projection.set (double, double String, ProjectedPoint): This projection uses no seperate zones");
	}
	public String getZone(ProjectedPoint pp) {
		throw new UnsupportedOperationException("Projection.getZone (double, double String, ProjectedPoint): This projection uses no seperate zones");
		
	}

	public String toHumanReadableString(ProjectedPoint pp) {
	 	return pp.toString(0, "", " ");
	}
	
}
