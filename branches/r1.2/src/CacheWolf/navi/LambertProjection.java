package CacheWolf.navi;

import CacheWolf.CWPoint;
public final class LambertProjection extends Projection{

	double falseNorthing;
	double falseEasting;
	//double firstStandardParallel;
	//double secondSandardParallel;
	double centralLat;
	double centralLon;
	Ellipsoid ellip;

	double e, n, F0, Rb;

	/**
	 * 
	 * @param falseNorthing: in meters
	 * @param falseEasting
	 * @param firstStandardParallel: in decimal degrees
	 * @param secondSandardParallel
	 * @param centralLat: in decimal degrees
	 * @param centralLon
	 */
	public LambertProjection(int epsgcode_, Ellipsoid ellip_) {
		epsgCode = epsgcode_;
		ellip = ellip_;
	}

	public void setup(double falseNorthing_, double falseEasting_, 
			double firstStandardParallel_, double secondSandardParallel_, double scale_,
			double centralLat_,	double centralLon_) {
		falseNorthing = falseNorthing_;
		falseEasting = falseEasting_;
		double firstStandardParallel = firstStandardParallel_ * java.lang.Math.PI / 180;
		double secondSandardParallel = secondSandardParallel_* java.lang.Math.PI / 180;
		centralLat = centralLat_ * java.lang.Math.PI / 180;
		centralLon = centralLon_ * java.lang.Math.PI / 180;

		double f = ellip.getFlattening();
		e = java.lang.Math.sqrt(2.0 * f - f*f);
		double m1 = java.lang.Math.cos(firstStandardParallel) / java.lang.Math.sqrt(1.0 - e*e * java.lang.Math.pow(java.lang.Math.sin(firstStandardParallel),2));
		double m2 = java.lang.Math.cos(secondSandardParallel) / java.lang.Math.sqrt(1.0 - e*e * java.lang.Math.pow(java.lang.Math.sin(secondSandardParallel),2));
		double t0 = java.lang.Math.tan(java.lang.Math.PI/4 - centralLat           / 2) / java.lang.Math.pow((1.0 - (e * java.lang.Math.sin(centralLat           ))) / (1.0 + (e * java.lang.Math.sin(centralLat           ))), e/2);
		double t1 = java.lang.Math.tan(java.lang.Math.PI/4 - firstStandardParallel/ 2) / java.lang.Math.pow((1.0 - (e * java.lang.Math.sin(firstStandardParallel))) / (1.0 + (e * java.lang.Math.sin(firstStandardParallel))), e/2);
		if (firstStandardParallel == secondSandardParallel) n = java.lang.Math.sin(centralLat); // one standard parallel
		else {
			double t2 = java.lang.Math.tan(java.lang.Math.PI/4 - secondSandardParallel/ 2) / java.lang.Math.pow((1.0 - (e * java.lang.Math.sin(secondSandardParallel))) / (1.0 + (e * java.lang.Math.sin(secondSandardParallel))), e/2);
			n = (java.lang.Math.log(m1) - java.lang.Math.log(m2)) / (java.lang.Math.log(t1) - java.lang.Math.log(t2));
		}
		// double nsin = java.lang.Math.sin(centralLat); // nsin and n should be equal
		// Vm.debug("n-log: " + n+ " n-sin phi: " + nsin);
		F0 = m1 / (n * java.lang.Math.pow(t1, n)) * scale_; // pow(t2???, n)
		Rb = ellip.a * F0 * java.lang.Math.pow(t0, n);
	}
	
	public ProjectedPoint project(CWPoint ll, ProjectedPoint pp, int epsg) {
		return project(ll, pp);
	}
	/**
	 * 
	 * @param ll
	 * @param pp: pp will be filled with the projected ll. If null, a new ProjectedPoint will be created
	 * @return
	 */
	public ProjectedPoint project(CWPoint ll, ProjectedPoint pp) {
		// formulas taken from http://surveying.wb.psu.edu/psu-surv/Projects/PASingleZone.pdf page 7-9 (Appendix I), see also http://www.geoclub.de/viewtopic.php?f=54&t=23912 (German)

		double lat = ll.latDec * java.lang.Math.PI / 180;
		double lon = ll.lonDec * java.lang.Math.PI / 180;
		double t = java.lang.Math.tan(java.lang.Math.PI /4 - lat / 2) / java.lang.Math.pow((1.0 - (e * java.lang.Math.sin(lat))) / (1.0 + (e * java.lang.Math.sin(lat))), e/2);
		// double m = java.lang.Math.cos(lat) / java.lang.Math.sqrt(1.0 - e*e * java.lang.Math.pow(java.lang.Math.sin(lat), 2));
		double R = ellip.a * F0 * java.lang.Math.pow(t, n);

		/* Solution */
		double gamma = n * (lon - centralLon);
		double easting = R * java.lang.Math.sin(gamma); //+ @False_Easting
		double northing = Rb - R * java.lang.Math.cos(gamma); // + @False_Northing
		if (pp == null) pp = new ProjectedPoint(this);
		pp.setRaw(northing, easting);
		//Vm.debug("project erg: "+ret.toString(0, "", " ", AUSTRIAN_LAMBERT));
		//Vm.debug("project: ll: " + unproject(TransformCoordinates.BESSEL).toString(CWPoint.DD));
		return pp;
	}

	public CWPoint unproject(ProjectedPoint pp) {
		//Vm.debug("unproject: "+pp.toString(0, "", " ", AUSTRIAN_LAMBERT));

		double ns = Rb - pp.northing;
		double es = pp.easting;
		double R = java.lang.Math.sqrt(es*es + ns*ns) * java.lang.Math.abs(n)/n;
		double t = java.lang.Math.pow(R/(ellip.a*F0), 1/n);
		double gamma = java.lang.Math.atan2(es, ns); // TODO unsure, whether always the correct sign is produced
		double lambda = centralLon + gamma/n;
		double phi0 = java.lang.Math.PI / 2 - 2* java.lang.Math.atan(t); // TODO unsure, whether always the correct sign is produced
		double phi1;
		boolean iterate;
		do {
			phi1 = java.lang.Math.PI / 2 - 2* java.lang.Math.atan(t*java.lang.Math.pow((1-e*java.lang.Math.sin(phi0))/(1+e*java.lang.Math.sin(phi0)),e/2));
			iterate = (java.lang.Math.abs(phi1 - phi0) > 0.000001);
			phi0 = phi1;
		} while (iterate);

		CWPoint ret = new CWPoint(phi1 * 180 / java.lang.Math.PI, lambda * 180 / java.lang.Math.PI);
		//Vm.debug("unproject: ret: " + ret.toString(CWPoint.DD));
		return ret;
	}

	public double getNorthing(ProjectedPoint pp) {
		return pp.northing + falseNorthing;
	}
	public double getEasting(ProjectedPoint pp) {
		return pp.easting + falseEasting;
	}

	public ProjectedPoint set(double northing_, double easting_, ProjectedPoint pp) {
		if (pp == null) {pp = new ProjectedPoint(); pp.projection = this; }
		pp.setRaw(northing_ - falseNorthing, easting_ - falseEasting);
		return pp;
	}

}

