package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Matrix;
import CacheWolf.MyLocale;

import java.lang.Math;

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
	/** Gauß-Boaga, Monte Mario, Roma 1940, IT_ROMA1940 */
	public static final int EPSG_ITALIAN_GB_EW1 = 3003; 
	public static final int EPSG_ITALIAN_GB_EW2 = 3004;
	
	private static final Ellipsoid BESSEL = new Ellipsoid(6377397.155, 6356078.962, true);
	public static final Ellipsoid WGS84 = new Ellipsoid(6378137.000, 6356752.314, true);
	public static final Ellipsoid HAYFORD1909 = new Ellipsoid(6378388, 297, false);
	
	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (North) to ETRS89
	//	 they are the same as http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 4 = Deutschland Nord"
	private static final TransformParameters GK_NORD_GERMANY_TO_WGS84 = new TransformParameters(590.5, 69.5, 411.6, 0.796, 0.052, 3.601, 8.300);
	/** use this for nord Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 52°20' N ... 55°00' N */
	public static final TransformParameters GK_NORD_GERMANY =  GK_NORD_GERMANY_TO_WGS84; 

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (Middle) to ETRS89
	private static final TransformParameters GK_MID_GERMANY_TO_WGS84 = new TransformParameters(584.8, 67.0, 400.3, -0.105, -0.013, 2.378, 10.290);
	/** use this for mid-Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 50°20' N ... 52°20' N */
	public static final TransformParameters GK_MID_GERMANY =  GK_MID_GERMANY_TO_WGS84; 

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (South) to ETRS89
	private static final TransformParameters GK_SOUTH_GERMANY_TO_WGS84 = new TransformParameters(597.1, 71.4, 412.1, -0.894, -0.068, 1.563, 7.580);
	/** use this for south Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 47°00' N ... 50°20' N */
	public static final TransformParameters GK_SOUTH_GERMANY =  GK_SOUTH_GERMANY_TO_WGS84; 

	public static Area FORMER_GDR = new Area(new CWPoint(54.923414, 10.503013), new CWPoint(50.402578, 14.520637)); 
	
	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 34cm.
	private static final TransformParameters GK_NRW_GERMANY_TO_WGS84 = new TransformParameters(566.1, 116.3, 390.1, -1.11, -0.24, 3.76, 12.6);
	/** use this for NRW in Germany. Deviations less than 34 cm */
	public static final TransformParameters GK_NRW_GERMANY =  GK_NRW_GERMANY_TO_WGS84; 

	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 113cm.
	// these matches to  http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 3 = Deutschland 1995"
	private static final TransformParameters GK_GERMANY_1995_TO_WGS84 = new TransformParameters(582, 105, 414, -1.04, -0.35, +3.08, 8.3);
	/** Use this for Germany if there is no more specific available. Deviations less than 113 cm */
	public static final TransformParameters GK_GERMANY_1995 =  GK_GERMANY_1995_TO_WGS84; 

	// taken from http://www.geodatenzentrum.de/geodaten/gdz_home1.gdz_home_start?gdz_home_para1=Technische%A0Hinweise&gdz_home_para2=Technische%A0Hinweise&gdz_home_menu_nr=10&gdz_home_menu_nr2=1&gdz_home_para3=/auftrag/html/gdz_tech_geo_deu.htm&gdz_home_spr=deu&gdz_home_para0=0
	private static final TransformParameters GK_GERMANY_BKG_TO_WGS84 = new TransformParameters(586, 87, 409, -0.52, -0.15, 2.82, 9);
	/** Use this for Germany if there is no more specific available. Deviations unknown. Data source: Bundesamt für Kartographie und Geodäsie, taken from website on: 1-11-2007 */
	public static final TransformParameters GK_GERMANY_BKG =  GK_GERMANY_BKG_TO_WGS84; 

	// take from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 2 = Deutschland 2001"
	private static final TransformParameters GK_GERMANY_2001_TO_WGS84 = new TransformParameters(598.1, 73.7, 418.2, -0.202, -0.045, 2.455, 6.700);
	/** Use this for Germany if there is no more specific available. maximal deviations unknown */
	public static final TransformParameters GK_GERMANY_2001 =  GK_GERMANY_2001_TO_WGS84;

	/** The italian variant of Gauß-Krüger (Gauß-Boaga) */
	// taken from http://crs.bkg.bund.de/crs-eu/ -> italy -> ROMA40 (change the sign of the rotation parameters!)
	public static final TransformParameters GB_ITALIAN_PENINSULAR_TO_WGS84 =  new TransformParameters(-104.1, -49.1, -9.9, -0.971, 2.917, -0.714, -11.68);
	//static final Area ITALY_PENINSULAR = new Area(new CWPoint());
	public static final TransformParameters GB_ITALIAN_SARDINIA_TO_WGS84 =  new TransformParameters(-168.6, -34.0, 38.6, 0.374, 0.679, 1.379, -9.48);
	static final Area ITALY_SARDINIA = new Area(new CWPoint(42, 6), new CWPoint(38, 11));
	static final Area ITALY_SARDINIA_GK = new Area(wgs84ToGaussKrueger(ITALY_SARDINIA.topleft, EPSG_ITALIAN_GB_EW1).toTrackPoint(GkPoint.ITALIAN_GB),
			wgs84ToGaussKrueger(ITALY_SARDINIA.buttomright, EPSG_ITALIAN_GB_EW1).toTrackPoint(GkPoint.ITALIAN_GB));

	public static final TransformParameters GB_ITALIAN_SICILIA_TO_WGS84 =  new TransformParameters(-50.2, -50.4, 84.8, 0.690, 2.012, -0.459, -28.08);
	static final Area ITALY_SICILIA = new Area(new CWPoint(39, 12), new CWPoint(36.3, 15.6));
	static final Area ITALY_SICILIA_GK = new Area(wgs84ToGaussKrueger(ITALY_SICILIA.topleft, EPSG_ITALIAN_GB_EW2).toTrackPoint(GkPoint.ITALIAN_GB),
			wgs84ToGaussKrueger(ITALY_SICILIA.buttomright, EPSG_ITALIAN_GB_EW2).toTrackPoint(GkPoint.ITALIAN_GB));

	private TransformCoordinates() {
		// as all members are static, so avoid instantiation
	} 

	/* replaced by getGkRegion
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
	*/
	
	/**
	 * 
	 * @param epsgcode
	 * @return region code as needed for GkPoint, -1 if not Gauß-Krüger or not supported
	 */
	public static int getGkRegion(int epsgcode) {
		int ret;
		switch (epsgcode) {
		case EPSG_GK2:
		case EPSG_GK3:
		case EPSG_GK4:
		case EPSG_GK5: ret = GkPoint.GERMAN_GK; break;
		case EPSG_ITALIAN_GB_EW1:
		case EPSG_ITALIAN_GB_EW2: ret = GkPoint.ITALIAN_GB; break;
		default: ret = -1;
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
		case EPSG_GK5: 
		case EPSG_ITALIAN_GB_EW1:
		case EPSG_ITALIAN_GB_EW2:
			ret = true; 
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
				( gk.getStripe() == 4 && gk.getGkEasting(GkPoint.GERMAN_GK) >= 4404124.247 && gk.getGkEasting(GkPoint.GERMAN_GK) <= 4679300.398) ||
				( gk.getStripe() == 5 && gk.getGkEasting(GkPoint.GERMAN_GK) >= 5211904.597 && gk.getGkEasting(GkPoint.GERMAN_GK) <= 5466056.603)
			) return gkToWgs84(gk, BESSEL, GK_GERMANY_2001, 1);
		if (gk.northing <= 6097247.910 && gk.northing >= 5800464.725 )return gkToWgs84(gk, BESSEL, GK_NORD_GERMANY, 1);
		if (gk.northing <= 5800464.725 && gk.northing >= 5577963.555 )return gkToWgs84(gk, BESSEL, GK_MID_GERMANY, 1);
		if (gk.northing <= 5577963.555 && gk.northing >= 5207294.028 )return gkToWgs84(gk, BESSEL, GK_SOUTH_GERMANY, 1);
		return  gkToWgs84(gk, BESSEL, GK_GERMANY_2001, 1);
	}
	public static CWPoint italianGkToWgs84(GkPoint gk) {
		if (ITALY_SARDINIA_GK.isInBound(gk.toTrackPoint(GkPoint.ITALIAN_GB))) return gkToWgs84(gk, HAYFORD1909, GB_ITALIAN_SARDINIA_TO_WGS84, 0.9996);
		if (ITALY_SICILIA_GK.isInBound(gk.toTrackPoint(GkPoint.ITALIAN_GB))) return gkToWgs84(gk, HAYFORD1909, GB_ITALIAN_SICILIA_TO_WGS84, 0.9996);
		else return gkToWgs84(gk, HAYFORD1909, GB_ITALIAN_PENINSULAR_TO_WGS84, 0.9996);
	}

	public static CWPoint GkToWgs84(GkPoint gk, int region) {
		switch (region) {
		case GkPoint.GERMAN_GK: return germanGkToWgs84(gk);
		case GkPoint.ITALIAN_GB: return italianGkToWgs84(gk);
		}
		throw new IllegalArgumentException("GkToWgs84: region: " + region + " not supported");
	}
	
	/**
	 * This is the most abstract method: If you don't know 
	 * when to use another one (if you are in need to do so, you will
	 * know), use this one. This routine chooses automatically the best known
	 * transformation parameters. Currently the maximal deviation is 1m for the
	 * former BRD and 1.13m for the former GDR 
	 * It also chooses automatically the correct stripe
	 * @param gk
	 * @return
	 */
	public static GkPoint wgs84ToGermanGk(CWPoint ll) {
		return  wgs84ToGk(ll, GkPoint.GERMAN_GK); 	
	}

	/**
	 * 
	 * @param ll
	 * @param region e.g. GkPoint.GERMAN_GK 
	 * @return
	 */
	public static GkPoint wgs84ToGk(TrackPoint ll, int region) {
		switch (region) {
		case GkPoint.GERMAN_GK:	return  wgs84ToGk(ll, BESSEL, getGermanGkTransformParameters(ll), -1, 3, 0, 1); 	
		case GkPoint.ITALIAN_GB:return  wgs84ToGk(ll, HAYFORD1909, getItalianGkTransformParameters(ll), -1, 6, 3, 0.9996); 	
		default: throw new IllegalArgumentException("wgs84ToGk(CWPoint, int): region: " + region + "not supported");
		}
	}
	
	public static TransformParameters getGermanGkTransformParameters(TrackPoint ll) {
		if (FORMER_GDR.isInBound(ll)) return GK_GERMANY_2001; // exlcude former GDR from the splitting germany in north/middel/south
		if (ll.latDec <= 55 && ll.latDec >= 52.33333334 ) return  GK_NORD_GERMANY;
		if (ll.latDec <= 52.33333334  && ll.latDec >= 50.33333334 ) return  GK_MID_GERMANY;
		if (ll.latDec <= 50.33333334  && ll.latDec >= 47) return  GK_SOUTH_GERMANY;
		return GK_GERMANY_2001;
	}

	public static TransformParameters getItalianGkTransformParameters(TrackPoint ll) {
		if (ITALY_SARDINIA.isInBound(ll)) return GB_ITALIAN_SARDINIA_TO_WGS84;
		if (ITALY_SICILIA.isInBound(ll)) return GB_ITALIAN_SICILIA_TO_WGS84;
		else return GB_ITALIAN_PENINSULAR_TO_WGS84;
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
			if (getGkStripeEpsg(epsgcodes[i]) == stripe) break;
		}
		if (i >= epsgcodes.length) return -1;
		return i;
	}
	
	/**
	 * Call this routine to convert from wgs84 into German Gauß-Krüger-Coordinates 
	 * using the Gauß-Krüger Projection and the ellipsoid forgk
	 * If you want the Gauß-Krüger-Coordinates in a certain stripe, provide the
	 * stripe and stripe width, otherwise set stripe to -1, then the stripe 
	 * will be automatically determined
	 * @param ll
	 * @param Gauß-Krüger-to-WGS84 transformation parameters, they will be automatically inverted
	 * @param stripe stripe to force to, otherwise -1 will determine the stripe automatically
	 * @return
	 */ // TODO find out what about the Krassowski in former GDR?
	public static GkPoint wgs84ToGk(TrackPoint ll, Ellipsoid forgk, TransformParameters gk2wgs84, int stripe, int stripewidth, int degreeOfStripe0, double scale) {
		XyzCoordinates wgsxyz = latLon2xyz(ll, 0, WGS84);
		XyzCoordinates gkxyz = transform(wgsxyz, gk2wgs84.inverted); 
		CWPoint gkll = xyz2Latlon(gkxyz, forgk);
		//ewe.sys.Vm.debug("wgs84-ll: " + new CWPoint(ll).toString(CWPoint.DMS));
		//ewe.sys.Vm.debug("gkll: " + gkll.toString(CWPoint.DMS));
		if (stripe == -1)	return projectLatlon2GkStripeauto(gkll, forgk, stripewidth, degreeOfStripe0, scale);
		else return projectLatlon2GK(gkll, forgk, stripewidth, stripe, degreeOfStripe0, scale); 
	}
	/**
	 * Call this method to convert any Gauß-Krüger coordinates into
	 * wgs84.
	 * @param gk point to convert
	 * @param GK2WGS84 Gauß-Krüger-to-WGS84 transformation parameters
	 * @return
	 */
	public static CWPoint gkToWgs84(GkPoint gk, Ellipsoid gkon, TransformParameters gk2wgs84, double scale) {
		CWPoint gkll = gk2LatLon(gk, gkon, scale);
		XyzCoordinates wgsxyz = latLon2xyz(gkll, 0, gkon);
		XyzCoordinates wgs84xyz = transform(wgsxyz, gk2wgs84);
		CWPoint wgsll = xyz2Latlon(wgs84xyz, WGS84);
		return wgsll;
	}
	
	/**
	 * this routine gives the correct Gauß-Krüger coordinates
	 * in the stripe specified by EPSG-Code
	 * @param wgs84
	 * @param epsgcode
	 * @return
	 * @throws IllegalArgumentException if EPSG code is not supported GK or unsupported
	 */
	public static GkPoint wgs84ToGaussKrueger(TrackPoint wgs84, int epsgcode) throws IllegalArgumentException {
		switch (getGkRegion(epsgcode)) {
		case GkPoint.GERMAN_GK: return wgs84ToGk(wgs84, BESSEL, getGermanGkTransformParameters(wgs84), getGkStripeEpsg(epsgcode), 3, 0, 1);
		case GkPoint.ITALIAN_GB: return wgs84ToGk(wgs84, HAYFORD1909, getItalianGkTransformParameters(wgs84), getGkStripeEpsg(epsgcode), 6, 3, 0.9996);
		}
		throw new IllegalArgumentException("wgs84ToGaussKrueger: epsg-code: " + epsgcode + "not supported");
	}
	
	private static int getGkStripeEpsg(int epsgcode) {
		int stripe;
		switch (epsgcode) {
		case EPSG_GK2: stripe = 2; break;
		case EPSG_GK3: stripe = 3; break;
		case EPSG_GK4: stripe = 4; break;
		case EPSG_GK5: stripe = 5; break;
		case EPSG_ITALIAN_GB_EW1: stripe = 1; break;
		case EPSG_ITALIAN_GB_EW2: stripe = 2; break;
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
		// not used: double N = ellipsoid.a / Math.sqrt(1 - e2 * Math.pow(Math.sin(B),2));
		// not used: double h = s / Math.cos(B)- N;
		CWPoint ret = new CWPoint();
		ret.latDec = B * 180/Math.PI;
		ret.lonDec = L * 180/Math.PI;
		//ret.alt = h;
		return ret;
	}

	private static GkPoint projectLatlon2GkStripeauto(CWPoint latlon, Ellipsoid ellipsoid, int stripewidth, float degreeOfStripe0, double scale) {
		if (!latlon.isValid()) throw new IllegalArgumentException("projectLatlon2GK: lat-lon not valid");
		double lonDec = latlon.lonDec;
		lonDec -= degreeOfStripe0;
		if (lonDec < 0) lonDec += 360;
		int stripe;
		for (stripe = 0; stripe <= 360; stripe += stripewidth) {
			if (Math.abs(lonDec - stripe) <= ((float)stripewidth) / 2) break;
		}
		return projectLatlon2GK(latlon, ellipsoid, stripewidth, stripe / stripewidth, degreeOfStripe0, scale);
	}

	/**
	 * Project latlon to Gauß-Krüger-Coordinates on ellipsoid
	 * @param latlon
	 * @param ellipsoid
	 * @return
	 */
	private static GkPoint projectLatlon2GK(CWPoint latlon, Ellipsoid ellipsoid, int stripewidth, int stripe, float degreeOfStripe0, double scale) {
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double l = (latlon.lonDec - degreeOfStripe0 - stripe * stripewidth) /180*Math.PI;
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
		double northing = (arclength + h1 + h2) * scale;

		double r1 = N * Math.cos(B) * l;
		double r2 = N/6 * Math.pow(Math.cos(B), 3) * (1-t*t+nue*nue)*l*l*l;
		double easting = (r1 + r2) * scale;		//+ stripe / stripewidth * 1000000 + 500000;
		GkPoint ret = new GkPoint();
		ret.set(easting, northing, stripe, stripewidth, degreeOfStripe0);
		return ret;
	}

	/**
	 * Converts Gauß-Krüger-coordinates into lat/lon on the respective ellipsoid
	 * @param gkp
	 * @param ellipsoid
	 * @param stripewidth width in degree of the stripe of the Gauß-Krüger-System (3 degreee usually used in Gauß-Krüger, 6 degree usually in UTM)
	 * @return
	 */
	private static CWPoint gk2LatLon (GkPoint gkp, Ellipsoid ellipsoid, double scale) {
		double L0 = gkp.getStripeLon(); // decimal degree of the center of the stripe
		double y = gkp.getRawEasting()/scale;

		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		// note: n1-n6 are similiar to the n1-n6 in projectLatlon2GK, but some term have different factors
		double n1 = (ellipsoid.a-ellipsoid.b)/(ellipsoid.a+ellipsoid.b);
		double n2 = (ellipsoid.a+ellipsoid.b)/2 * (1+ Math.pow(n1, 2)/4 + Math.pow(n1, 4)/64);
		double n3 = n1 * 3/2 - Math.pow(n1, 3) * 27/32  + Math.pow(n1, 5) * 269/32;
		double n4 = Math.pow(n1, 2) * 21/16 - Math.pow(n1, 4) * 55/32;
		double n5 = Math.pow(n1, 3) * 151/96 - Math.pow(n1, 5) * 417/128;
		double n6 = Math.pow(n1, 4) * 1097/512;

		double B0 = (gkp.northing / scale) / n2;
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
	 * @param exi rotation in seconds (change the sign of the values from http://crs.bkg.bund.de/crs-eu/ )
	 * @param si deviation of scale multiplied by 10^6 
	 * @param addinverted
	 */
	public TransformParameters(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si) {
		set (dxi, dyi, dzi, exi, eyi, ezi, si, true);
	}
		
	protected void set(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si, boolean addinverted) {
		dx = dxi; dy = dyi; dz = dzi; 
		ex = exi * Math.PI/180/3600;
		ey = eyi * Math.PI/180/3600; 
		ez = ezi * Math.PI/180/3600;
		s = 1/(1 - si * Math.pow(10, -6));
		if (addinverted) {
			inverted = new TransformParameters(this, false);
			inverted.invert();
		} else inverted = null;
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
	public TransformParameters inverted = null;
}


