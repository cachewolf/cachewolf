package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Matrix;
import CacheWolf.MyLocale;

import java.lang.Math;

import ewe.database.GetSearchCriteria;

/**
 * Class to transform coordinates and shift datums
 * it uses the 7 parameter Helmert Transformation
 * programmed according to http://www.geoclub.de/files/GK_nach_GPS.xls 
 * and http://www.geoclub.de/files/GPS_nach_GK.xls
 * The only difference to the excel-model is that shifting is done before rotation
 * this makes calculations easier, without changing the output.
 * 
 * For verification data see: 
 *  * http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007testdaten.csv
 *  * http://www.lverma.nrw.de/produkte/raumbezug/koordinatentransformation/Koordinatentransformation.htm
 * Now, that this is completed: there is a much more precise method right now published
 * by the Bundesamt für Kartographie und Geodäsie for whole Germany: see:
 *  * http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007dokumentation.pdf
 *  * http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (BeTA, 2007) to ETRS89
 *  
 *  Start offset in languages file: 4900
 * @author Pfeffer
 *
 */
public class TransformCoordinates {

	public static final int EPSG_WGS84 = 4326; 
	public static final int EPSG_ETRS89 = 25832; // TODO support it anyhow 
	public static final int EPSG_GK2 = 31466; 
	public static final int EPSG_GK3 = 31467; 
	public static final int EPSG_GK4 = 31468; 
	public static final int EPSG_GK5 = 31469; 
	
	private static final Ellipsoid BESSEL = new Ellipsoid(6377397.155, 6356078.962);
	public static final Ellipsoid WGS84 = new Ellipsoid(6378137.000, 6356752.314);

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (North) to ETRS89
	//	 they are the same as http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 4 = Deutschland Nord"
	private static final TransformParameters GK_NORD_GERMANY_TO_WGS84 = new TransformParameters(590.5, 69.5, 411.6, 0.796, 0.052, 3.601, 8.300, false);
	/** use this for nord Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 52°20' N ... 55°00' N */
	public static final TransformParameters GK_NORD_GERMANY =  GK_NORD_GERMANY_TO_WGS84; 

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (Middle) to ETRS89
	private static final TransformParameters GK_MID_GERMANY_TO_WGS84 = new TransformParameters(584.8, 67.0, 400.3, -0.105, -0.013, 2.378, 10.290, false);
	/** use this for mid-Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 50°20' N ... 52°20' N */
	public static final TransformParameters GK_MID_GERMANY =  GK_MID_GERMANY_TO_WGS84; 

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (South) to ETRS89
	private static final TransformParameters GK_SOUTH_GERMANY_TO_WGS84 = new TransformParameters(597.1, 71.4, 412.1, -0.894, -0.068, 1.563, 7.580, false);
	/** use this for south Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 47°00' N ... 50°20' N */
	public static final TransformParameters GK_SOUTH_GERMANY =  GK_SOUTH_GERMANY_TO_WGS84; 

	public static Area FORMER_GDR = new Area(new CWPoint(54.923414, 10.503013), new CWPoint(50.402578, 14.520637)); 
	
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

	public static boolean isGermanGk(int epsgcode) {
		boolean ret = false;
		switch (epsgcode) {
		case EPSG_GK2:
		case EPSG_GK3:
		case EPSG_GK4:
		case EPSG_GK5: ret = true; 
		}
		return ret;
	}

	public static boolean isSupported(int epsgcode) {
		boolean ret = false;
		switch (epsgcode) {
		case EPSG_WGS84:
		case EPSG_GK2:
		case EPSG_GK3:
		case EPSG_GK4:
		case EPSG_GK5: ret = true; 
		}
		return ret;
	}
		
	/**
	 * This is the most abstract method: If you don't know 
	 * when to use another one (if you are in need to do so, you will
	 * know), use this one.
	 * @param gk
	 * @return
	 */
	public static CWPoint germanGkToWgs84(GkPoint gk) {
		if (gk.northing <= 6089288.064 && gk.northing >= 5585291.767 && // these coordinates are transformed ones from the invers routine
				( gk.getStripe() == 4 && gk.getGkEasting() >= 4404124.247 && gk.getGkEasting() <= 4679300.398) ||
				( gk.getStripe() == 5 && gk.getGkEasting() >= 5211904.597 && gk.getGkEasting() <= 5466056.603)
			) return gkToWgs84(gk, GK_GERMANY_2001);
		if (gk.northing <= 6097247.910 && gk.northing >= 5800464.725 )return gkToWgs84(gk, GK_NORD_GERMANY);
		if (gk.northing <= 5800464.725 && gk.northing >= 5577963.555 )return gkToWgs84(gk, GK_MID_GERMANY);
		if (gk.northing <= 5577963.555 && gk.northing >= 5207294.028 )return gkToWgs84(gk, GK_SOUTH_GERMANY);
		return  gkToWgs84(gk, GK_GERMANY_2001); 	//TODO use more lokalized transformparameters, which can be obtained from the Landesvermessungsämter
	}

	/**
	 * This is the most abstract method: If you don't know 
	 * when to use another one (if you are in need to do so, you will
	 * know), use this one. This routine chooses automatically the best known
	 * transformation parameters. Currently the maximam deviation is 1m for the
	 * former BRD and 1.13m for the former GDR 
	 * It also chooses automatically the correct stripe
	 * @param gk
	 * @return
	 */
	public static GkPoint wgs84ToGermanGk(CWPoint ll) {
		return  wgs84ToGk(ll, getGermanGkTransformParameters(ll)); 	
	}
	
	public static TransformParameters getGermanGkTransformParameters(TrackPoint ll) {
		if (FORMER_GDR.isInBound(ll)) return GK_GERMANY_2001; // exlcude former GDR from the splitting germany in north/middel/south
		if (ll.latDec <= 55 && ll.latDec >= 52.33333334 ) return  GK_NORD_GERMANY;
		if (ll.latDec <= 52.33333334  && ll.latDec >= 50.33333334 ) return  GK_MID_GERMANY;
		if (ll.latDec <= 50.33333334  && ll.latDec >= 47) return  GK_SOUTH_GERMANY;
		return GK_GERMANY_2001;
	}
	
	/**
	 * Standard Gauß-Krüger: stripewidth = 3, stripe automatically chosen
	 * @param ll
	 * @param gk2wgs84
	 * @return
	 */
	public static GkPoint wgs84ToGk(CWPoint ll, TransformParameters gk2wgs84) {
		return wgs84ToGk(ll, gk2wgs84, -1, 3);
	}
	
	
	/**
	 * This function returns the position in the list of the given epsg code list
	 * which corresondes to the stripe used in Gauß-Krüger Point gk
	 * @param epsgcodes list of epsgcodes
	 * @param gk
	 * @return postion in array of epsgcodes, -1 if not found
	 */
	public static int whichEpsg(int[] epsgcodes, GkPoint gk) {
		int stripe = gk.getStripe();
		int i;
		for (i = 0; i < epsgcodes.length; i++) {
			if (getGermanGkStripeEpsg(epsgcodes[i]) == stripe) break;
		}
		if (i >= epsgcodes.length) return -1;
		return i;
	}
	
	/**
	 * Call this routine to convert from wgs84 into German Gauß-Krüger-Coordinates 
	 * using the Gauß-Krüger Projection and the Bessel ellipsoid
	 * If you want the Gauß-Krüger-Coordinates in a certain stripe, provide the
	 * stripe and stripe width, otherwise set stripe to -1, then the stripe 
	 * will be automatically determined
	 * @param ll
	 * @param Gauß-Krüger-to-WGS84 transformation parameters, they will be automatically inverted
	 * @param stripe stripe to force to, otherwise -1 will determine the stripe automatically
	 * @return
	 */ // TODO find out what about the Krassowski in former GDR?
	public static GkPoint wgs84ToGk(TrackPoint ll, TransformParameters gk2wgs84, int stripe, int stripewidth) {
		XyzCoordinates wgsxyz = latLon2xyz(ll, 0, WGS84);
		XyzCoordinates gkxyz = transform(wgsxyz, new TransformParameters(gk2wgs84, true));
		CWPoint gkll = xyz2Latlon(gkxyz, BESSEL);
		if (stripe == -1)	return projectLatlon2GkStripeauto(gkll, BESSEL, stripewidth);
		else return projectLatlon2GK(gkll, BESSEL, stripewidth, stripe); 
	}
	/**
	 * Call this method to convert any Gauß-Krüger coordinates into
	 * wgs84.
	 * @param gk point to convert
	 * @param GK2WGS84 Gauß-Krüger-to-WGS84 transformation parameters
	 * @return
	 */
	public static CWPoint gkToWgs84(GkPoint gk, TransformParameters gk2wgs84) {
		CWPoint gkll = gk2LatLon(gk, BESSEL, 3);
		XyzCoordinates wgsxyz = latLon2xyz(gkll, 0, BESSEL);
		XyzCoordinates wgs84xyz = transform(wgsxyz, gk2wgs84);
		CWPoint wgsll = xyz2Latlon(wgs84xyz, WGS84);
		return wgsll;
	}
	
	/**
	 * this routine gives the correct german Gauß-Krüger coordinates
	 * in the stripe specified by EPSG-Code
	 * @param wgs84
	 * @param epsgcode
	 * @return
	 * @throws IllegalArgumentException if EPSG code is not german GK or unsupported
	 */
	public static GkPoint wgs84ToGermanGk(TrackPoint wgs84, int epsgcode) throws IllegalArgumentException {
		return wgs84ToGk(wgs84, getGermanGkTransformParameters(wgs84), getGermanGkStripeEpsg(epsgcode), 3);
	}
	
	private static int getGermanGkStripeEpsg(int epsgcode) {
		int stripe;
		switch (epsgcode) {
		case EPSG_GK2: stripe = 2; break;
		case EPSG_GK3: stripe = 3; break;
		case EPSG_GK4: stripe = 4; break;
		case EPSG_GK5: stripe = 5; break;
		default: throw new IllegalArgumentException("wgs84ToGermanGk: epsgcode: " + epsgcode + MyLocale.getMsg(4900, " is not a german Gauss-Krueger coordinate"));
		}
		return stripe; 
	}
	
	private static XyzCoordinates latLon2xyz(TrackPoint ll, double alt, Ellipsoid ellipsoid) {
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

	private static CWPoint xyz2Latlon(XyzCoordinates from, Ellipsoid ellipsoid) {
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

	private static GkPoint projectLatlon2GkStripeauto(CWPoint latlon, Ellipsoid ellipsoid, int stripewidth) {
		if (!latlon.isValid()) throw new IllegalArgumentException("projectLatlon2GK: lat-lon not valid");
		CWPoint ll = new CWPoint(latlon); // copy the point, in order to avoid modifying the parameter latlon
		if (ll.lonDec < 0) ll.lonDec += 360;
		int stripe;
		for (stripe = 0; stripe <= 360; stripe += stripewidth) {
			if (Math.abs(ll.lonDec - stripe) <= ((float)stripewidth) / 2) break;
		}
		return projectLatlon2GK(latlon, ellipsoid, stripewidth, stripe / stripewidth);
	}

	/**
	 * Project latlon to Gauß-Krüger-Coordinates on ellipsoid
	 * @param latlon
	 * @param ellipsoid
	 * @return
	 */
	private static GkPoint projectLatlon2GK(CWPoint latlon, Ellipsoid ellipsoid, int stripewidth, int stripe) {
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double l = (latlon.lonDec - stripe * stripewidth) /180*Math.PI; // TODO see is int to double works
		double B = latlon.latDec /180*Math.PI;
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
		double easting = r1 + r2;		//+ stripe / stripewidth * 1000000 + 500000;
		GkPoint ret = new GkPoint();
		ret.set(easting, northing, stripe, stripewidth);
		return ret;
	}

	/**
	 * Converts Gauß-Krüger-coordinates into lat/lon on the respective ellipsoid
	 * @param gkp
	 * @param ellipsoid
	 * @param stripewidth width in degree of the stripe of the Gauß-Krüger-System (3 degreee usually used in Gauß-Krüger, 6 degree usually in UTM)
	 * @return
	 */
	private static CWPoint gk2LatLon (GkPoint gkp, Ellipsoid ellipsoid, int stripewidth) {
		double L0 = gkp.getStripeLon(); // decimal degree of the center of the stripe
		double y = gkp.getRawEasting();

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
		// these deal with less than the overall calculation precision: double la3 = tf /720 / Math.pow(Nf, 6) * (-61 - 90*tf*tf - 45*Math.pow(tf,4) - 107*nuef*nuef + 162*tf*tf * Math.pow(nuef, 2) + 45*Math.pow(tf,4)*tf*Math.pow(nuef, 2)) * Math.pow(y, 6);
		// these deal with less than the overall calculation precision: double la4 = tf /40320 / Math.pow(Nf, 8) * (1385+3663*tf*tf - 4095*Math.pow(tf,4) + 1575*Math.pow(nuef, 6)) * Math.pow(y, 8);
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


