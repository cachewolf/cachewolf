package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Matrix;
import CacheWolf.MyLocale;

import java.lang.Math;

import ewe.sys.Vm;

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
public final class TransformCoordinates {

	public static final int EPSG_WGS84 = 4326; 
	public static final int EPSG_ETRS89 = 25832; // TODO support it anyhow 
	public static final int EPSG_GK2 = 31466; 
	public static final int EPSG_GK3 = 31467; 
	public static final int EPSG_GK4 = 31468; 
	public static final int EPSG_GK5 = 31469;
	/** Gauß-Boaga, Monte Mario, Roma 1940, IT_ROMA1940 */
	public static final int EPSG_ITALIAN_GB_EW1 = 3003; 
	public static final int EPSG_ITALIAN_GB_EW2 = 3004;
	/** Austrian Lambert, Bessel 1841, Hermannskogel */
	public static final int EPSG_AUSTRIAN_LAMBERT_OLD = 31287;
	/** Austrian Lambert, ETRS89 */
	public static final int EPSG_AUSTRIAN_LAMBERT_NEW = 3416;

	public static final Ellipsoid BESSEL      = new Ellipsoid(6377397.155, 6356078.962, true);
	public static final Ellipsoid WGS84       = new Ellipsoid(6378137.000, 6356752.314, true);
	public static final Ellipsoid HAYFORD1909 = new Ellipsoid(6378388    , 297        , false);

	//	 taken from http://www.crs-geo.eu/crseu/EN/Home/homepage__node.html?__nnn=true click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (North) to ETRS89
	//	 they are the same as http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 4 = Deutschland Nord" (rotation *-1)
	/** use this for nord Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 52°20' N ... 55°00' N */
	private static final TransformParameters GK_NORD_GERMANY_TO_WGS84 = new TransformParameters(590.5, 69.5, 411.6, 0.796, 0.052, 3.601, 8.300, BESSEL);

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (Middle) to ETRS89 (rotation *-1)
	/** use this for mid-Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 50°20' N ... 52°20' N */
	private static final TransformParameters GK_MID_GERMANY_TO_WGS84 = new TransformParameters(584.8, 67.0, 400.3, -0.105, -0.013, 2.378, 10.290, BESSEL);

	//	 taken from http://crs.bkg.bund.de/crs-eu/ click on "national CRS" -> germany -> DE_DHDN / GK_3 -> DE_DHDN (South) to ETRS89 (rotation *-1)
	/** use this for south Germany, maximum deviation sub meter, valid in the former BRD (west germany) in 47°00' N ... 50°20' N */
	private static final TransformParameters GK_SOUTH_GERMANY_TO_WGS84 = new TransformParameters(597.1, 71.4, 412.1, -0.894, -0.068, 1.563, -7.580, BESSEL);

	public static Area FORMER_GDR = new Area(new CWPoint(54.923414, 10.503013), new CWPoint(50.402578, 14.520637)); 

	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 34cm.
	/** use this for NRW in Germany. Deviations less than 34 cm */
	private static final TransformParameters GK_NRW_GERMANY_TO_WGS84 = new TransformParameters(566.1, 116.3, 390.1, -1.11, -0.24, 3.76, -12.6, BESSEL);

	// taken from http://www.lverma.nrw.de/produkte/druckschriften/verwaltungsvorschriften/images/gps/TrafopsNRW.pdf for NRW this transform has deviations lower than 113cm.
	// these matches to  http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 3 = Deutschland 1995"
	/** Use this for Germany if there is no more specific available. Deviations less than 113 cm */
	private static final TransformParameters GK_GERMANY_1995_TO_WGS84 = new TransformParameters(582, 105, 414, -1.04, -0.35, +3.08, -8.3, BESSEL);

	// taken from http://www.geodatenzentrum.de/geodaten/gdz_home1.gdz_home_start?gdz_home_para1=Technische%A0Hinweise&gdz_home_para2=Technische%A0Hinweise&gdz_home_menu_nr=10&gdz_home_menu_nr2=1&gdz_home_para3=/auftrag/html/gdz_tech_geo_deu.htm&gdz_home_spr=deu&gdz_home_para0=0
	/** Use this for Germany if there is no more specific available. Deviations unknown. Data source: Bundesamt für Kartographie und Geodäsie, taken from website on: 1-11-2007 */
	private static final TransformParameters GK_GERMANY_BKG_TO_WGS84 = new TransformParameters(586, 87, 409, -0.52, -0.15, 2.82, -9, BESSEL);

	// take from http://www.geoclub.de/files/GK_nach_GPS.xls "Parametersatz 2 = Deutschland 2001" (rotation *-1)
	/** Use this for Germany if there is no more specific available. maximal deviations unknown */
	private static final TransformParameters GK_GERMANY_2001_TO_WGS84 = new TransformParameters(598.1, 73.7, 418.2, -0.202, -0.045, 2.455, 6.700, BESSEL);

	// taken from http://crs.bkg.bund.de/crs-eu/ -> italy -> ROMA40 (change the sign of the rotation parameters!)
	/** The italian variant of Gauß-Krüger (Gauß-Boaga) */
	public static final TransformParameters GB_ITALIAN_PENINSULAR_TO_WGS84 =  new TransformParameters(-104.1, -49.1, -9.9, -0.971, 2.917, -0.714, -11.68, HAYFORD1909);
	//static final Area ITALY_PENINSULAR = new Area(new CWPoint());
	public static final TransformParameters GB_ITALIAN_SARDINIA_TO_WGS84 =  new TransformParameters(-168.6, -34.0, 38.6, 0.374, 0.679, 1.379, 9.48, HAYFORD1909);
	static final Area ITALY_SARDINIA = new Area(new CWPoint(42, 6), new CWPoint(38, 11));
	static final Area ITALY_SARDINIA_GK = new Area(wgs84ToEpsg(ITALY_SARDINIA.topleft, EPSG_ITALIAN_GB_EW1).toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB),
			wgs84ToEpsg(ITALY_SARDINIA.buttomright, EPSG_ITALIAN_GB_EW1).toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB));

	public static final TransformParameters GB_ITALIAN_SICILIA_TO_WGS84 =  new TransformParameters(-50.2, -50.4, 84.8, 0.690, 2.012, -0.459, 28.08, HAYFORD1909);
	static final Area ITALY_SICILIA = new Area(new CWPoint(39, 12), new CWPoint(36.3, 15.6));
	static final Area ITALY_SICILIA_GK = new Area(wgs84ToEpsg(ITALY_SICILIA.topleft, EPSG_ITALIAN_GB_EW2).toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB),
			wgs84ToEpsg(ITALY_SICILIA.buttomright, EPSG_ITALIAN_GB_EW2).toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB));

	// see also http://hal.gis.univie.ac.at/karto/lehr/fachbereiche/geoinfo/givi0304/tutorials/ersteschritte/projectionen.htm#ParMGIWGS84 
	// taken from taken from http://www.crs-geo.eu/crseu/EN/Home/homepage__node.html?__nnn=true click on "national CRS" -> Austria -> AT (translation *-1 as of 11-8-2009)
	/** Austria Datum Hermannskogel, AT_MGI accuracy about 1.5m */
	public static final TransformParameters LAMBERT_AUSTRIAN_OLD_TO_WGS84 = new TransformParameters(577.326, 90.129, 463.919, -5.136599, -1.4742, -5.297044, 2.4232, BESSEL);
	public static final TransformParameters LAMBERT_AUSTRIAN_NEW_TO_WGS84 = new TransformParameters(0      ,0      ,0       ,0         ,0       ,0         ,0      , WGS84);
	// Übersicht über alle Transformparameter und EPSG-COdes und Projektionenm (PORJ4):
	// http://svn.osgeo.org/metacrs/proj/trunk/proj/nad/epsg
	//public static final TransformParameters WGS72_TO_WGS84 =  new TransformParameters(0, 0, 4.5, 0, 0, -0.554, 0.219);

	private TransformCoordinates() {
		// as all members are static, so avoid instantiation
	} 


	/**
	 * 
	 * @param epsgcode
	 * @return region code as needed for GkPoint, -1 if not Gauß-Krüger or not supported
	 * Inside one ProjectedRegion the epsg-code (zone / stripe) can be automatically choosen
	 * depending on lat / lon.
	 */
	public static int getLocalProjectionSystem(int epsgcode) {
		int ret;
		switch (epsgcode) {
		case EPSG_GK2:
		case EPSG_GK3:
		case EPSG_GK4:
		case EPSG_GK5: 					ret = ProjectedPoint.LOCALSYSTEM_GERMAN_GK; break;
		case EPSG_ITALIAN_GB_EW1:
		case EPSG_ITALIAN_GB_EW2:       ret = ProjectedPoint.LOCALSYSTEM_ITALIAN_GB; break;
		case EPSG_AUSTRIAN_LAMBERT_OLD: ret = ProjectedPoint.LOCALSYSTEM_AUSTRIAN_LAMBERT_OLD; break;
		case EPSG_AUSTRIAN_LAMBERT_NEW: ret = ProjectedPoint.LOCALSYSTEM_AUSTRIAN_LAMBERT_NEW; break;
		default: ret = -1;
		}
		return ret;
	}

	public static boolean isSupported(int epsgcode) {
		if ((epsgcode == EPSG_WGS84) || epsgcode == EPSG_ETRS89) return true;
		return (getLocalProjectionSystem(epsgcode) >= 0);
	}


	public static CWPoint ProjectedEpsgToWgs84(ProjectedPoint lp, int epsg) {
		return ProjectedToWgs84(lp, epsg, false);
	}

	public static CWPoint ProjectedToWgs84(ProjectedPoint lp, int epsg_localsystem, boolean isLocalSystem) {
		CWPoint ll = lp.unproject();
		int ls = (isLocalSystem ? epsg_localsystem : getLocalProjectionSystem(epsg_localsystem));
		TransformParameters transparams;
		switch (ls) {
		case ProjectedPoint.LOCALSYSTEM_GERMAN_GK:
			transparams = getGermanTransformParams(lp);  break;
		case ProjectedPoint.LOCALSYSTEM_ITALIAN_GB: 
			transparams = getItalianTransformParams(lp); break;
		case ProjectedPoint.LOCALSYSTEM_AUSTRIAN_LAMBERT_OLD:	
			transparams = LAMBERT_AUSTRIAN_OLD_TO_WGS84; break; 
		case ProjectedPoint.LOCALSYSTEM_AUSTRIAN_LAMBERT_NEW: 	
			transparams = LAMBERT_AUSTRIAN_NEW_TO_WGS84; break;
		default: throw new IllegalArgumentException("ProjectedToWGS84: EPSG-code: " + epsg_localsystem + " not supported");
		}
		XyzCoordinates xyzorig = latLon2xyz(ll, 0, transparams.ellip);
		XyzCoordinates xyzwgs84 = transform(xyzorig, transparams);
		return xyz2Latlon(xyzwgs84, WGS84);
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


	public static TransformParameters getGermanGkTransformParameters(TrackPoint ll) {
		if (FORMER_GDR.isInBound(ll)) return GK_GERMANY_2001_TO_WGS84; // exlcude former GDR from the splitting germany in north/middel/south
		if (ll.latDec <= 55 && ll.latDec >= 52.33333334 ) return  GK_NORD_GERMANY_TO_WGS84;
		if (ll.latDec <= 52.33333334  && ll.latDec >= 50.33333334 ) return  GK_MID_GERMANY_TO_WGS84;
		if (ll.latDec <= 50.33333334  && ll.latDec >= 47) return  GK_SOUTH_GERMANY_TO_WGS84;
		return GK_GERMANY_2001_TO_WGS84;
	}
	public static TransformParameters getGermanTransformParams(ProjectedPoint gk) {
		double n = gk.getNorthing();
		if (n <= 6089288.064 && n >= 5585291.767 && // these coordinates are transformed ones from the invers routine
				( gk.zone == 4 && gk.getEasting() >= 4404124.247 && gk.getEasting() <= 4679300.398) ||
				( gk.zone == 5 && gk.getEasting() >= 5211904.597 && gk.getEasting() <= 5466056.603)
		) return GK_GERMANY_2001_TO_WGS84;
		if (n <= 6097247.910 && n >= 5800464.725 )return GK_NORD_GERMANY_TO_WGS84;
		if (n <= 5800464.725 && n >= 5577963.555 )return GK_MID_GERMANY_TO_WGS84;
		if (n <= 5577963.555 && n >= 5207294.028 )return GK_SOUTH_GERMANY_TO_WGS84;
		return GK_GERMANY_2001_TO_WGS84;
	}

	public static TransformParameters getItalianGkTransformParameters(TrackPoint ll) {
		if (ITALY_SARDINIA.isInBound(ll)) return GB_ITALIAN_SARDINIA_TO_WGS84;
		if (ITALY_SICILIA.isInBound(ll)) return GB_ITALIAN_SICILIA_TO_WGS84;
		else return GB_ITALIAN_PENINSULAR_TO_WGS84;
	}
	public static TransformParameters getItalianTransformParams(ProjectedPoint gk) {
		if (ITALY_SARDINIA_GK.isInBound(gk.toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB))) return GB_ITALIAN_SARDINIA_TO_WGS84;
		if (ITALY_SICILIA_GK.isInBound(gk.toTrackPoint(ProjectedPoint.LOCALSYSTEM_ITALIAN_GB))) return GB_ITALIAN_SICILIA_TO_WGS84;
		else return GB_ITALIAN_PENINSULAR_TO_WGS84;
	}


	public static ProjectedPoint wgs84ToEpsg(TrackPoint wgs84, int epsg) throws IllegalArgumentException {
		return wgs84ToEpsgLocalsystem(wgs84, epsg, false);
	}

	public static ProjectedPoint wgs84ToLocalsystem(TrackPoint wgs84, int localsystem) throws IllegalArgumentException {
		return wgs84ToEpsgLocalsystem(wgs84, localsystem, true);
	}

	/**
	 * this routine gives the correct Gauß-Krüger coordinates
	 * in the stripe specified by EPSG-Code
	 * @param wgs84
	 * @param epsg_localsystem
	 * @return
	 * @throws IllegalArgumentException if EPSG code is not supported GK or unsupported
	 */
	private static ProjectedPoint wgs84ToEpsgLocalsystem(TrackPoint wgs84, int epsg_localsystem, boolean isLocalsystem) throws IllegalArgumentException {
		//wgs84.latDec = 47.07472; // Testkoordinaten von http://www.geoclub.de/viewtopic.php?f=54&t=23912&start=30 
		//wgs84.lonDec = 12.69417;
		// xyzWgs.x = 3657660.66; // test case http://www.epsg.org/ p. 109 WGS72_TO_WGS84
		// xyzWgs.y =  255768.55;
		// xyzWgs.z = 5201382.11; 
		XyzCoordinates xyzWgs = latLon2xyz(wgs84, 0, WGS84);
		int lps = (isLocalsystem ? epsg_localsystem : getLocalProjectionSystem(epsg_localsystem));
		TransformParameters transparams = getTransParams(wgs84, lps);
		XyzCoordinates xyztarget = transform(xyzWgs, transparams.inverted);
		CWPoint tll = xyz2Latlon(xyztarget, transparams.ellip);
		ProjectedPoint ret = new ProjectedPoint(tll, epsg_localsystem, false, isLocalsystem);
		//Vm.debug("wgs84: " + wgs84.latDec + " lon: " + wgs84.lonDec + ", Projected: " + ret.toString());
		return ret;
	}

	private static TransformParameters getTransParams(TrackPoint wgs84, int localsystem) {
		switch(localsystem) {
		case ProjectedPoint.LOCALSYSTEM_GERMAN_GK: 
			return getGermanGkTransformParameters(wgs84); 
		case ProjectedPoint.LOCALSYSTEM_ITALIAN_GB: 
			return getItalianGkTransformParameters(wgs84);
		case ProjectedPoint.LOCALSYSTEM_AUSTRIAN_LAMBERT_OLD:
			return LAMBERT_AUSTRIAN_OLD_TO_WGS84;
		default: 
			throw new IllegalArgumentException("getTransParams: localsystem: " + localsystem + "not supported");
		}
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


		Matrix rotate = new Matrix(3,3);
		rotate.matrix[0][0] = 1;						rotate.matrix[0][1] = transParams.ez;			rotate.matrix[0][2] = - transParams.ey;
		rotate.matrix[1][0] = - rotate.matrix[0][1];	rotate.matrix[1][1] = 1;						rotate.matrix[1][2] = transParams.ex;
		rotate.matrix[2][0] = - rotate.matrix[0][2];	rotate.matrix[2][1] = - rotate.matrix[1][2];	rotate.matrix[2][2] = 1;

		rotate.MultiplyByScalar(transParams.s); // scale

		rotate.Multiply(coos);
		coos = rotate;
		coos.add(shift);

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
	Ellipsoid ellip;

	/**
	 * 
	 * @param d shift in meter
	 * @param exi rotation in seconds (change the sign of the values from http://crs.bkg.bund.de/crs-eu/ )
	 * @param si deviation of scale multiplied by 10^6 
	 * @param addinverted
	 */
	public TransformParameters(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si, Ellipsoid ellip_) {
		set (dxi, dyi, dzi, exi, eyi, ezi, si, true);
		ellip = ellip_;
	}

	protected void set(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si, boolean addinverted) {
		dx = dxi; dy = dyi; dz = dzi; 
		ex = exi * Math.PI/180/3600;
		ey = eyi * Math.PI/180/3600; 
		ez = ezi * Math.PI/180/3600;
		s = 1 + si* Math.pow(10, -6); // 1/(1 - si * Math.pow(10, -6));
		if (addinverted) {
			inverted = new TransformParameters(this, false);
			inverted.invert();
		} else inverted = null;
	}

	public TransformParameters(TransformParameters tp, boolean invert) {
		dx = tp.dx;	dy = tp.dy;	dz = tp.dz;
		ex = tp.ex;	ey = tp.ey;	ez = tp.ez;
		s = tp.s;
		ellip = tp.ellip;
		if (invert) invert();
	}

	public void invert() {
		dx *= -1; dy *= -1;	dz *= -1;
		ex *= -1; ey *= -1;	ez *= -1;
		s = 1/s;
	}
	public TransformParameters inverted = null;
}


