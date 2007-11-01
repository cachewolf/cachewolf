package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Matrix;

import java.lang.Math;

/**
 * Class to transform coordinates and shift datums
 * it uses the 7 parameter Helmert Transformation
 * programmed according to http://www.geoclub.de/files/GK_nach_GPS.xls 
 * and http://www.geoclub.de/files/GPS_nach_GK.xls
 * The only difference to the excel-model is that shifting is done before rotation
 * this makes calculations easier, without changing the output.
 * @author Robert Arnold
 *
 */
public class TransformCoordinates {
	
	private static final Ellipsoid BESSEL = new Ellipsoid(6377397.155, 6356078.962);
	private static final Ellipsoid WGS84 = new Ellipsoid(6378137.000, 6356752.314);
	

	//	 taken from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 4 = Deutschland Nord"
	private static final TransformParameters GK_NORD_GERMANY_TO_WGS84 = new TransformParameters(590.5, 69.5, 411.6, 0.796, 0.052, 3.601, 8.300, false);
	/** use this for nord Germany, maximum deviation unknown */
	public static final TransformParameters GK_NORD_GERMANY =  GK_NORD_GERMANY_TO_WGS84; 

	//	 taken from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 3 = Mitte Deutschland"
	private static final TransformParameters GK_MID_GERMANY_TO_WGS84 = new TransformParameters(584.8, 67.0, 400.3, -0.105, -0.013, 2.378, 10.290, false);
	/** use this for mid Germany, maximum deviation unknown */
	public static final TransformParameters GK_MID_GERMANY =  GK_MID_GERMANY_TO_WGS84; 

	//	 taken from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 5 = Deutschland Süd"
	private static final TransformParameters GK_SOUTH_GERMANY_TO_WGS84 = new TransformParameters(597.1, 71.4, 412.1, -0.894, -0.068, 1.563, 7.580, false);
	/** use this for south Germany, maximum deviation unknown */
	public static final TransformParameters GK_SOUTH_GERMANY =  GK_SOUTH_GERMANY_TO_WGS84; 

	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 34cm.
	private static final TransformParameters GK_NRW_GERMANY_TO_WGS84 = new TransformParameters(566.1, 116.3, 390.1, -1.11, -0.24, 3.76, 12.6, false);
	/** use this for NRW in Germany. Deviations less than 34 cm */
	public static final TransformParameters GK_NRW_GERMANY =  GK_NRW_GERMANY_TO_WGS84; 
	
	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 113cm.
	// these matches to  http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 3 = Deutschland 1995"
	private static final TransformParameters GK_GERMANY_1995_TO_WGS84 = new TransformParameters(582, 105, 414, -1.04, -0.35, +3.08, 8.3, false);
	/** Use this for Germany if there is no more specific available. Deviations less than 113 cm */
	public static final TransformParameters GK_GERMANY_1995 =  GK_GERMANY_1995_TO_WGS84; 

	// taken from http://www.geodatenzentrum.de/geodaten/gdz_home1.gdz_home_start?gdz_home_para1=Technische%A0Hinweise&gdz_home_para2=Technische%A0Hinweise&gdz_home_menu_nr=10&gdz_home_menu_nr2=1&gdz_home_para3=/auftrag/html/gdz_tech_geo_deu.htm&gdz_home_spr=deu&gdz_home_para0=0
	private static final TransformParameters GK_GERMANY_BKG_TO_WGS84 = new TransformParameters(586, 87, 409, -0.52, -0.15, 2.82, 9, false);
	/** Use this for Germany if there is no more specific available. Deviations unknown. Data source: Bundesamt für Kartographie und Geodäsie, taken from website on: 1-11-2007 */
	public static final TransformParameters GK_GERMANY_BKG =  GK_GERMANY_BKG_TO_WGS84; 

	// take from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 2 = Deutschland 2001"
	private static final TransformParameters GK_GERMANY_2001_TO_WGS84 = new TransformParameters(598.1, 73.7, 418.2, -0.202, -0.045, 2.455, 6.700, false);
	/** Use this for Germany if there is no more specific available. maximal deviations unknown */
	public static final TransformParameters GK_GERMANY_2001 =  GK_GERMANY_2001_TO_WGS84; 

	

	private TransformCoordinates() {} // as all members are static, so avoid instantiation
	
	/**
	 * This is the most abstract method: If you don't know 
	 * when to use another one (if you are in need to do so, you will
	 * know), use this one.
	 * @param gk
	 * @return
	 */
	public static CWPoint gkGermanyToWgs84(GkPoint gk) {
		return  gkToWgs84(gk, GK_GERMANY_2001); 	//TODO use more lokalized transformparameters, which can be obtained from the Landesvermessungsämter
	}
	
	/**
	 * This is the most abstract method: If you don't know 
	 * when to use another one (if you are in need to do so, you will
	 * know), use this one.
	 * @param gk
	 * @return
	 */
	public static GkPoint wgs84ToGkGermany(CWPoint ll) {
		return  wgs84ToGk(ll, GK_GERMANY_2001); 	//TODO use more lokalized transformparameters, which can be obtained from the Landesvermessungsämter
	}

	/**
	 * Call this routine to convert from wgs84 into German Gauß-Krüger-Coordinates 
	 * using the Gauß-Krüger Projection and the Bessel ellipsoid
	 * 	 *  
	 * @param ll
	 * @param Gauß-Krüger-to-WGS84 transformation parameters, they will be automatically inverted
	 * @return
	 */
	public static GkPoint wgs84ToGk(CWPoint ll, TransformParameters gk2wgs84) {
		XyzCoordinates wgsxyz = latLon2xyz(ll, 0, WGS84);
		XyzCoordinates gkxyz = transform(wgsxyz, new TransformParameters(gk2wgs84, true));
		CWPoint gkll = Xyz2Latlon(gkxyz, BESSEL);
		return projectLatlon2GK(gkll, BESSEL, 3);
	}
	/**
	 * Call this method to convert any Gauß-Krüger coordinates into
	 * wgs84.
	 * @param gk point to convert
	 * @param GK2WGS84 Gauß-Krüger-to-WGS84 transformation parameters
	 * @return
	 */
	public static CWPoint gkToWgs84(GkPoint gk, TransformParameters gk2wgs84) {
		CWPoint gkll = GK2LatLon(gk, BESSEL, 3);
		XyzCoordinates wgsxyz = latLon2xyz(gkll, 0, BESSEL);
		XyzCoordinates wgs84xyz = transform(wgsxyz, gk2wgs84);
		CWPoint wgsll = Xyz2Latlon(wgs84xyz, WGS84);
		return wgsll;
	}
	
	private static XyzCoordinates latLon2xyz(CWPoint ll, double alt, Ellipsoid ellipsoid) {
		if (!ll.isValid()) throw new IllegalArgumentException("latLon2xyz: invalid lat-lon");
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double N = ellipsoid.a/ Math.sqrt(1 - e2 * Math.pow(Math.sin(ll.latDec / 180*Math.PI), 2));
		XyzCoordinates ret = new XyzCoordinates(0,0,0);
		ret.x = (N+alt) * Math.cos(ll.latDec /180*Math.PI) * Math.cos(ll.lonDec /180*Math.PI);
		ret.y = (N+alt) * Math.cos(ll.latDec /180*Math.PI) * Math.sin(ll.lonDec /180*Math.PI);
		ret.z = (N * Math.pow(ellipsoid.b, 2) / Math.pow(ellipsoid.a , 2) + alt) * Math.sin(ll.latDec /180*Math.PI);
		return ret;
	}
	
	private static XyzCoordinates transform(XyzCoordinates from, TransformParameters transParams) {
		Matrix coos = new Matrix(3, 1);
		coos.matrix[0][0] = from.x;
		coos.matrix[1][0] = from.y;
		coos.matrix[2][0] = from.z;

		Matrix shift = new Matrix(3,1);
		shift.matrix[0][0] = transParams.dx;
		shift.matrix[1][0] = transParams.dy;
		shift.matrix[2][0] = transParams.dz;
		
		coos.add(shift);
		
		Matrix rotate = new Matrix(3,3);
		rotate.matrix[0][0] = 1;
		rotate.matrix[1][1] = 1;
		rotate.matrix[2][2] = 1;
		rotate.matrix[0][1] = transParams.ez; 
		rotate.matrix[0][2] = - transParams.ey;
		rotate.matrix[1][0] = - rotate.matrix[0][1];
		rotate.matrix[1][2] = transParams.ex;
		rotate.matrix[2][0] = - rotate.matrix[0][2];
		rotate.matrix[2][1] = - rotate.matrix[1][2];
		
		rotate.Multiply(coos);
		coos = rotate;
		coos.MultiplyByScalar(transParams.s); // scale
		
		return new XyzCoordinates(coos.matrix[0][0], coos.matrix[1][0], coos.matrix[2][0]);
	}
	
	private static CWPoint Xyz2Latlon(XyzCoordinates from, Ellipsoid ellipsoid) {
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double s = Math.sqrt( Math.pow(from.x,2) + Math.pow(from.y,2));
		double T = Math.atan( from.z * ellipsoid.a / (s * ellipsoid.b));
		double B = Math.atan( (from.z + e2 * Math.pow(ellipsoid.a, 2) / ellipsoid.b * Math.pow(Math.sin(T), 3) )/(s - e2 * ellipsoid.a * Math.pow(Math.cos(T),3)));
		double L = Math.atan(from.y / from.x);
		double N = ellipsoid.a / Math.sqrt(1 - e2 * Math.pow(Math.sin(B),2));
		double h = s / Math.cos(B)- N;
		CWPoint ret = new CWPoint();
		ret.latDec = B * 180/Math.PI;
		ret.lonDec = L * 180/Math.PI;
		//ret.alt = h;
		return ret;
	}
	
	/**
	 * Project latlon to Gauß-Krüger-Coordinates on ellipsoid
	 * @param latlon
	 * @param ellipsoid
	 * @return
	 */
	private static GkPoint projectLatlon2GK(CWPoint latlon, Ellipsoid ellipsoid, int stripewidth) {
		if (!latlon.isValid()) throw new IllegalArgumentException("projectLatlon2GK: lat-lon not valid");
		CWPoint ll = new CWPoint(latlon); // copy the point, in order to avoid modifying the parameter latlon
		if (ll.lonDec < 0) ll.lonDec += 360;
		int stripe;
		for (stripe = 0; stripe <= 360; stripe += stripewidth) {
			if (Math.abs(ll.lonDec - stripe) <= ((float)stripewidth) / 2) break;
		}
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double l = (ll.lonDec - stripe) /180*Math.PI; // TODO see is int to double works
		double B = ll.latDec /180*Math.PI;
		double N = ellipsoid.a/ Math.sqrt(1- e2 * Math.pow(Math.sin(B),2));
		double nue = Math.sqrt(Math.pow(ellipsoid.a, 2) / Math.pow(ellipsoid.b, 2)* e2 * Math.pow(Math.cos(B), 2));
		double t = Math.tan(B);
		
		double n1 = (ellipsoid.a-ellipsoid.b)/(ellipsoid.a+ellipsoid.b);
		double n2 = (ellipsoid.a+ellipsoid.b)/2 * (1+ Math.pow(n1, 2)/4 + Math.pow(n1, 4)/64);
		double n3 = n1 * -3/2 + Math.pow(n1, 3) * 9/16  - Math.pow(n1, 5) * 3/32;
		double n4 = Math.pow(n1, 2) * 15/16 - Math.pow(n1, 4) * 15/32;
		double n5 = Math.pow(n1, 3) * -35/48 + Math.pow(n1, 5) * 105/256;
		double n6 = Math.pow(n1, 4) * 315/512;
		double arclength = n2 * (B + n3 * Math.sin(B*2) + n4 * Math.sin(B*4) + n5 * Math.sin(B*6) + n6 * Math.sin(B*8));
		
		double h1 = t/2 * N * Math.pow(Math.cos(B), 2) * l*l;
		double h2 = t/24 * N * Math.pow(Math.cos(B),4) * (5 - t*t + 9 * nue*nue + 4*Math.pow(nue, 4)) * Math.pow(l,4);
		double northing = arclength + h1 + h2;
		
		double r1 = N * Math.cos(B) * l;
		double r2 = N/6 * Math.pow(Math.cos(B), 3) * (1-t*t+nue*nue)*l*l*l;
		double easting = r1 + r2 + stripe / stripewidth * 1000000 + 500000;
		GkPoint ret = new GkPoint();
		ret.easting = easting;
		ret.northing = northing;
		return ret;
	}
	
	/**
	 * Converts Gauß-Krüger-coordinates into lat/lon on the respective ellipsoid
	 * @param gkp
	 * @param ellipsoid
	 * @param stripewidth width in degree of the stripe of the Gauß-Krüger-System (3 degreee usually used in Gauß-Krüger, 6 degree usually in UTM)
	 * @return
	 */
	private static CWPoint GK2LatLon (GkPoint gkp, Ellipsoid ellipsoid, int stripewidth) {
		double Y0 = Math.floor(gkp.easting / 1000000);
		double L0 = Y0 * stripewidth; // decimal degree of the center of the stripe
		double y = gkp.easting - 1000000 * Y0 - 500000;
		
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		// note: n1-n6 are similiar to the n1-n6 in projectLatlon2GK, but some term have different factors
		double n1 = (ellipsoid.a-ellipsoid.b)/(ellipsoid.a+ellipsoid.b);
		double n2 = (ellipsoid.a+ellipsoid.b)/2 * (1+ Math.pow(n1, 2)/4 + Math.pow(n1, 4)/64);
		double n3 = n1 * 3/2 - Math.pow(n1, 3) * 27/32  + Math.pow(n1, 5) * 269/32;
		double n4 = Math.pow(n1, 2) * 21/16 - Math.pow(n1, 4) * 55/32;
		double n5 = Math.pow(n1, 3) * 151/96 - Math.pow(n1, 5) * 417/128;
		double n6 = Math.pow(n1, 4) * 1097/512;

		double B0 = gkp.northing / n2;
		double Bf = B0 + n3 * Math.sin(B0*2) + n4 * Math.sin(B0*4) + n5 * Math.sin(B0*6) + n6 * Math.sin(B0*8);
		
		double Nf = ellipsoid.a / Math.sqrt (1- e2 * Math.pow(Math.sin(Bf), 2));
		double nuef = Math.sqrt(ellipsoid.a * ellipsoid.a / ellipsoid.b / ellipsoid.b * e2 * Math.pow(Math.cos(Bf), 2));
		double tf = Math.tan(Bf);
		
		double la1 = tf / 2 / Nf/Nf * (-1-nuef*nuef) * y*y;
		double la2 = tf /24 / Math.pow(Nf, 4) * (5 + 3*tf*tf + 6*nuef*nuef - 6*tf*tf * nuef*nuef - 4*Math.pow(nuef, 4) - 9*tf*tf*Math.pow(nuef, 4)) * Math.pow(y, 4);
	// these deal this less than the overall calculation precision double la3 = tf /720 / Math.pow(Nf, 6) * (-61 - 90*tf*tf - 45*Math.pow(tf,4) - 107*nuef*nuef + 162*tf*tf * Math.pow(nuef, 2) + 45*Math.pow(tf,4)*tf*Math.pow(nuef, 2)) * Math.pow(y, 6);
	// these deal this less than the overall calculation precision double la4 = tf /40320 / Math.pow(Nf, 8) * (1385+3663*tf*tf - 4095*Math.pow(tf,4) + 1575*Math.pow(nuef, 6)) * Math.pow(y, 8);
		double lat = (Bf + la1 + la2) * 180 / Math.PI;

		double lo1 = 1 / Nf / Math.cos(Bf) * y;
		double lo2 = 1 / Math.pow(Nf, 3) / Math.cos(Bf) *  (-1 -tf*tf*2 - nuef*nuef) * Math.pow(y, 3) / 6;
		double lon = L0 + (lo1 + lo2) * 180/Math.PI;
		return new CWPoint(lat, lon);
	}
	
}

class XyzCoordinates {
	double x, y, z;
	public XyzCoordinates (double xi, double yi, double zi) {
		x = xi;
		y = yi;
		z = zi;
	}
}

class Ellipsoid {
	double a, b;
	public Ellipsoid(double ai, double bi) {
		a = ai;
		b = bi;
	}
}

class TransformParameters {
	// shift parameter in meter
	double dx, dy, dz, 
	// rotation parameter in rad
	ex, ey, ez,
	// scale as multiplicator
	s;
	
	/**
	 * 
	 * @param d shift in meter
	 * @param exi rotation in seconds
	 * @param si deviation of scale multiplied by 10^6 
	 * @param invert
	 */
	public TransformParameters(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si, boolean invert) {
		dx = dxi; dy = dyi; dz = dzi; 
		ex = exi * Math.PI/180/3600;
		ey = eyi * Math.PI/180/3600; 
		ez = ezi * Math.PI/180/3600;
		s = 1/(1 - si * Math.pow(10, -6));
		if (invert) invert();
	}
	
	public TransformParameters(TransformParameters tp, boolean invert) {
		dx = tp.dx;	dy = tp.dy;	dz = tp.dz;
		ex = tp.ex;	ey = tp.ey;	ez = tp.ez;
		s = tp.s;
		if (invert) invert();
	}

	public void invert() {
		dx *= -1; dy *= -1;	dz *= -1;
		ex *= -1; ey *= -1;	ez *= -1;
		s = 1/s;
	}
}


