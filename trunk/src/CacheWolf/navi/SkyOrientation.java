/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.navi;

import CacheWolf.MyLocale;
import ewe.sys.Convert;

/**
 * Class to caculate positions of luminaries all methods are static usage: call utc2juliandate and then getLuminaryDir in ressources/cachewolf.languages messege numbers from 6100
 * 
 * @author Pfeffer
 * 
 */
public class SkyOrientation {

    public final static int SUN = 0;
    public final static int MOON = 1;
    public static final int ALIOTH = 2; // brightest star in Grater Bear (Grosser Wagen) Rektaszension 12 h 54 m 2 s Deklination +55 Grad 57' 36"
    public static final int GREATER_BEAR = ALIOTH;
    public static final int ALNILAM = 3; //Orion = Alnilam = mittlerer Guertelstern Aequinoktium 2000): Rektaszension 5h36m13s; Deklination -1 Grad 12'7"
    public static final int ORION = ALNILAM;
    public static final int CASSIOPEIA_GAMMA = 4; // Kassiopeia Gamma: 00h 56m 42.50s	+60 Grad 43' 00.3"
    public static final int CASSIOPEIA = CASSIOPEIA_GAMMA;
    public static final int DENEB = 5;
    public static final int CYGNUS = DENEB; // Cygnus = Schwan
    public static final int MIMOSA = 6; // second brightest star in Southern Cross
    public static final int SOUTHERN_CROSS = MIMOSA; // SOUTHERN_CROSS = Kreus des Südens = Crux australia

    public static final CWPoint[] STARS = {
	    // (Deklination, Rektaszension)
	    /*ALIOTH*/new CWPoint(55. + 57. / 60. + 36. / 3600., (12. + 54. / 60. + 2. / 3600.) * 15.), // ALIOTH: Rektaszension 12 h 54 m 2 s Deklination +55 Grad 57' 36"
	    /*ALNILAM*/new CWPoint(-1. - 12. / 60. - 7. / 3600., (5. + 36. / 60. + 13. / 3600.) * 15.), // (-1. -12./60. -7./3600., (5. + 36./60. + 13./3600.)*15.) <- wikipedia // -1.19748, 5.60978 * 15.) <- www.... // (-1. -11./60. -52./3600., (5. + 36./60. + 35./3600.)*15.)  <- Stellarium 
	    /*Cassiopeia*/new CWPoint(60. + 43. / 60. + 0.3 / 3600., (0 + 56. / 60. + 42.5 / 3600.) * 15.), // CASSIOPALA_GAMMA 00h 56m 42.50s, 60 Grad 43' 00.3" <-- wikipedia, Stellarium: 57m 11s, 60 Grad 45' 29"
	    /*Deneb*/new CWPoint(45. + 16. / 60. + 49.2 / 3600., (20 + 41. / 60. + 25.6 / 3600.) * 15.), // im Schwan (Sommerdreieck) Quelle: Stellarium
	    /*Mimosa*/new CWPoint(-59. - 41. / 60. - 19. / 3600., (12 + 47. / 60. + 43.2 / 3600.) * 15.) // im Schwan (Sommerdreieck) Quelle: Stellarium
    // Sirius
    };

    public static String[] LUMINARY_NAMES = { MyLocale.getMsg(6100, "Sun"), MyLocale.getMsg(6101, "Moon"), MyLocale.getMsg(6102, "Grater Bear"), MyLocale.getMsg(6103, "Orion"), MyLocale.getMsg(6104, "Cassiopeia"), MyLocale.getMsg(6105, "Cygnus"),
	    MyLocale.getMsg(6106, "Southern Cross") };

    public static String[] LUMINARY_DESC = { MyLocale.getMsg(6100, "Sun"), MyLocale.getMsg(6101, "Moon"), MyLocale.getMsg(6122, "Alioth in Greater Bear"), MyLocale.getMsg(6123, "Alnilam in Orion"), MyLocale.getMsg(6124, "Cassiopeia Gamma"),
	    MyLocale.getMsg(6125, "Deneb in Cygnus"), MyLocale.getMsg(6126, "Becrux in Southern Cross") };

    /**
     * Get the friendly name of the luminary
     * 
     * @param luminary
     * @return
     */
    public static String getLuminaryName(int luminary) {
	return LUMINARY_NAMES[luminary];
    }

    /**
     * Get a more exact description of the luminary
     * 
     * @param lu
     * @return
     */
    public static String getLuminaryDesc(int lu) {
	return LUMINARY_DESC[lu];
    }

    /**
     * get azimuth from north and elevation for horizont for a given Luminary (planet or star)
     * 
     * @param luminary
     *            one of SUN, MOON, ALIOTH, GRAETER_BEAR, ALNILAM, ORION, CASSIOPEIA_GAMMA, CASSIOPEIA
     * @param jd
     *            julian date must be calculated in advance e.g. from utc2julian
     * @param onEarth
     *            place on earth of the observer
     * @return lon = azimuth from north, lat = elevation from horizont
     */
    public static CWPoint getLuminaryDir(int luminary, double jd, CWPoint onEarth) {
	switch (luminary) {
	case SUN:
	    return getSunDir(jd, onEarth);
	case MOON:
	    return getMoonDir(jd, onEarth);
	default:
	    return equatorial2AzimutCoos(onEarth, jd, STARS[luminary - MOON - 1]);
	}
    }

    /**
     * @param utc
     *            in the format as it comes from gps DDMMYY
     * @param datum
     *            in the format as it comes from gps HHMMSS
     * @return juliandate
     * @throws NumberFormatException
     *             if utc / datum could not be parsed successfully
     */
    public static double utc2juliandate(String utc, String datum) {
	try {
	    int tag, monat, jahr, stunde, minute, sekunde;
	    tag = Convert.parseInt(datum.substring(0, 2));
	    monat = Convert.parseInt(datum.substring(2, 4));
	    jahr = Convert.parseInt(datum.substring(4, 6)) + 2000;
	    stunde = Convert.parseInt(utc.substring(0, 2));
	    minute = Convert.parseInt(utc.substring(2, 4));
	    sekunde = Convert.parseInt(utc.substring(4, 6)); // Kommastellen werden abgeschnitten
	    // julianisches "Datum" jd berechnen (see http://de.wikipedia.org/wiki/Julianisches_Datum )
	    if (monat < 2) {
		jahr--;
		monat += 12;
	    } // verlegung des Jahres Endes auf Feb macht Berechnung von SChaltjahren einfacher
	    double a = (int) java.lang.Math.floor(jahr / 100.); // Alle hundert Jahre kein Schlatjahr (abrunden)
	    double b = 2 - a + java.lang.Math.floor(a / 4.);
	    double jd = java.lang.Math.floor(365.25 * (jahr + 4716.)) + java.lang.Math.floor(30.6001 * (monat + 1.)) + tag + (double) stunde / 24 + (double) minute / 1440 + (double) sekunde / 86400 + b - 1524.5;
	    return jd;
	    //double jd0 = java.lang.Math.floor(365.25*(jahr + 4716.)) + java.lang.Math.floor(30.6001*((double)monat+1.)) +(double)tag + b - 1524.5;
	} catch (IndexOutOfBoundsException e) {
	    // wird von substring geworfen wenn datum / utc nicht genug Ziffern haben
	    // NumberFormatException wird außerdem von Convert.ParseInt direkt geworfen wenn
	    // nicht in Int konvertiert werden kann
	    throw new NumberFormatException();
	}
    }

    /**
     * old version, gives the same as the new one
     * 
     * @param utc
     * @param datum
     * @param lat
     * @param lon
     * @return
     */
    public static float getSunAzimut(String utc, String datum, double lat, double lon) {
	try {
	    int tag, monat, jahr, stunde, minute, sekunde;
	    tag = Convert.parseInt(datum.substring(0, 2));
	    monat = Convert.parseInt(datum.substring(2, 4));
	    jahr = Convert.parseInt(datum.substring(4, 6)) + 2000;
	    stunde = Convert.parseInt(utc.substring(0, 2));
	    minute = Convert.parseInt(utc.substring(2, 4));
	    sekunde = Convert.parseInt(utc.substring(4, 6)); // Kommastellen werden abgeschnitten
	    // julianisches "Datum" jd berechnen (see http://de.wikipedia.org/wiki/Julianisches_Datum )
	    if (monat < 2) {
		jahr--;
		monat += 12;
	    } // verlegung des Jahres Endes auf Feb macht Berechnung von SChaltjahren einfacher
	    double a = (int) java.lang.Math.floor(jahr / 100.); // Alle hundert Jahre kein Schlatjahr (abrunden)
	    double b = 2 - a + java.lang.Math.floor(a / 4.);
	    double jd = java.lang.Math.floor(365.25 * (jahr + 4716.)) + java.lang.Math.floor(30.6001 * (monat + 1.)) + tag + (double) stunde / 24 + (double) minute / 1440 + (double) sekunde / 86400 + b - 1524.5;
	    double jd0 = java.lang.Math.floor(365.25 * (jahr + 4716.)) + java.lang.Math.floor(30.6001 * (monat + 1.)) + tag + b - 1524.5;
	    // Ekliptikalkoordinaten der Sonne berechnen (see http://de.wikipedia.org/wiki/Sonnenstand )
	    double n = jd - 2451545.0;
	    double l = 280.46 + 0.9856474 * n;
	    double g = 357.528 + 0.9856003 * n;
	    double d = l + 1.915 * java.lang.Math.sin(g / 180 * java.lang.Math.PI) + 0.02 * java.lang.Math.sin(2 * g / 180 * java.lang.Math.PI);
	    // Rektaszension alpha und Deklination delta der Sonne berechnen
	    double e = 23.439 - 0.0000004 * n;
	    double alphaNenner = java.lang.Math.cos(d / 180 * java.lang.Math.PI);
	    double alpha = 180 / java.lang.Math.PI * java.lang.Math.atan(java.lang.Math.cos(e / 180 * java.lang.Math.PI) * java.lang.Math.sin(d / 180 * java.lang.Math.PI) / alphaNenner);
	    double delta = 180 / java.lang.Math.PI * java.lang.Math.asin(java.lang.Math.sin(e / 180 * java.lang.Math.PI) * java.lang.Math.sin(d / 180 * java.lang.Math.PI));
	    if (alphaNenner < 0) {
		alpha += 180;
	    }
	    // Azimut
	    double t0 = (jd0 - 2451545.) / 36525.; // schon in t0 bzw jd0 richtig berechnet?
	    double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * (stunde + minute / 60. + sekunde / 3600.);
	    double theta = thetaHG * 15. + lon;
	    double azimutNenner = java.lang.Math.cos((theta - alpha) / 180 * java.lang.Math.PI) * java.lang.Math.sin(lat / 180 * java.lang.Math.PI) - java.lang.Math.tan(delta / 180 * java.lang.Math.PI)
		    * java.lang.Math.cos(lat / 180 * java.lang.Math.PI);
	    float azimut = (float) java.lang.Math.atan(java.lang.Math.sin((theta - alpha) / 180 * java.lang.Math.PI) / azimutNenner);
	    azimut = (float) (azimut * 180f / java.lang.Math.PI);
	    if (azimutNenner < 0)
		azimut += 180.;
	    // null = Sueden auf Null = Norden umrechnen
	    azimut += 180.;
	    if (azimut > 360.)
		azimut -= 360.;
	    return azimut;
	} catch (IndexOutOfBoundsException e) {
	    // wird von substring geworfen wenn datum / utc nicht genug Ziffern haben
	    // NumberFormatException wird ausserdem von Convert.ParseInt direkt geworfen wenn
	    // nicht in Int konvertiert werden kann
	    throw new NumberFormatException();
	}
    }

    public static CWPoint getSunAzimut2(String utc, String datum, double lat, double lon) {
	double jd = utc2juliandate(utc, datum);
	CWPoint eclCoos = getSunEclipticCoos(jd);
	// calculate ecliptic coos
	// convert coos
	return ecliptic2AzimutCoos(new CWPoint(lat, lon), jd, eclCoos);
    }

    public static CWPoint getSunDir(double jd, CWPoint onEarth) {
	CWPoint eclCoos = getSunEclipticCoos(jd);
	// calculate ecliptic coos
	// convert coos
	return ecliptic2AzimutCoos(onEarth, jd, eclCoos);
    }

    public static CWPoint getMoonDir(double jd, CWPoint onEarth) {
	CWPoint eclCoo = getMoonEclipticCoos(jd);
	return ecliptic2AzimutCoos(onEarth, jd, eclCoo);
    }

    public static CWPoint getAlnilamDir(double jd, CWPoint onEarth) {
	// Koordinaten Alnilam (mittlerer Guertelstern des Orion), Rektaszension 5h36m13s; Deklination -1°12'7 TODO Aequinoktium 2000
	// Source: wikipedia
	return equatorial2AzimutCoos(onEarth, jd, new CWPoint(-1. - 12. / 60. - 7. / 3600., (5. + 36. / 60. + 13. / 3600.) * 15.)); // (-1. -12./60. -7./3600., (5. + 36./60. + 13./3600.)*15.) <- wikipedia // -1.19748, 5.60978 * 15.) <- www.... // (-1. -11./60. -52./3600., (5. + 36./60. + 35./3600.)*15.)  <- Stellarium
    }

    /**
     * get the ecliptic coordinates of the sun
     * 
     * @param juliandate
     * @return
     */
    public static CWPoint getSunEclipticCoos(double juliandate) {
	double n = juliandate - 2451545.0;
	double l = 280.46 + 0.9856474 * n;
	double g = 357.528 + 0.9856003 * n;
	double lambda = l + 1.915 * java.lang.Math.sin(g / 180 * java.lang.Math.PI) + 0.02 * java.lang.Math.sin(2 * g / 180 * java.lang.Math.PI);
	return new CWPoint(0, lambda);
    }

    // the following code is adopted from http://lexikon.astronomie.info/java/sunmoon/sunmoon.html
    // ignores the time difference between juliandate and TDT, which is something like 1 minute
    public static CWPoint getMoonEclipticCoos(double julianDate) {
	final double DEG = Math.PI / 180;
	final double RAD = 1 / DEG;
	double sunAnomalyMean = 360 * DEG / 365.242191 * (julianDate - 2447891.5) + 279.403303 * DEG - 282.768422 * DEG;
	double D = julianDate - 2447891.5;

	// Mean Moon orbit elements as of 1990.0
	double l0 = 318.351648 * DEG;
	double P0 = 36.340410 * DEG;
	double N0 = 318.510107 * DEG;
	double i = 5.145396 * DEG;

	double l = 13.1763966 * DEG * D + l0;
	double MMoon = l - 0.1114041 * DEG * D - P0; // Moon's mean anomaly M
	double N = N0 - 0.0529539 * DEG * D; // Moon's mean ascending node longitude

	double sunlon = getSunEclipticCoos(julianDate).lonDec;
	double C = l - sunlon;
	double Ev = 1.2739 * DEG * Math.sin(2 * C - MMoon);
	double Ae = 0.1858 * DEG * Math.sin(sunAnomalyMean);
	double A3 = 0.37 * DEG * Math.sin(sunAnomalyMean);

	double MMoon2 = MMoon + Ev - Ae - A3; // corrected Moon anomaly
	double Ec = 6.2886 * DEG * Math.sin(MMoon2); // equation of centre
	double A4 = 0.214 * DEG * Math.sin(2 * MMoon2);
	double l2 = l + Ev + Ec - Ae + A4; // corrected Moon's longitude
	double V = 0.6583 * DEG * Math.sin(2 * (l2 - sunlon));

	double l3 = l2 + V; // true orbital longitude;
	double N2 = N - 0.16 * DEG * Math.sin(sunAnomalyMean);

	CWPoint moonCoor = new CWPoint();
	moonCoor.lonDec = ((N2 + Math.atan2(Math.sin(l3 - N2) * Math.cos(i), Math.cos(l3 - N2))) * RAD) % 360;
	moonCoor.latDec = Math.asin(Math.sin(l3 - N2) * Math.sin(i)) * RAD;
	//moonCoor.orbitLon = l3;
	return moonCoor;

	/*
	double e  = 0.054900;
	double a  = 384401; // km
	double diameter0 = 0.5181*DEG; // angular diameter of Moon at a distance
	double parallax0 = 0.9507*DEG; // parallax at distance a

	  // relative distance to semi mayor axis of lunar oribt
	  moonCoor.distance = (1-sqr(e)) / (1+e*Math.cos(MMoon2+Ec) );
	  moonCoor.diameter = diameter0/moonCoor.distance; // angular diameter in radians
	  moonCoor.parallax = parallax0/moonCoor.distance; // horizontal parallax in radians
	  moonCoor.distance *= a;	// distance in km

	  // Age of Moon in radians since New Moon (0) - Full Moon (pi)
	  moonCoor.moonAge = Mod2Pi(l3-sunCoor.lon);   
	  moonCoor.phase   = 0.5*(1-Math.cos(moonCoor.moonAge)); // Moon phase, 0-1

	  var phases = new Array("Neumond", "Zunehmende Sichel", "Erstes Viertel", "Zunnehmender Mond", 
	  	"Vollmond", "Abnehmender Mond", "Letztes Viertel", "Abnehmende Sichel", "Neumond");
	  var mainPhase = 1./29.53*360*DEG; // show 'Newmoon, 'Quarter' for +/-1 day arond the actual event
	  var p = Mod(moonCoor.moonAge, 90.*DEG);
	  if (p < mainPhase || p > 90*DEG-mainPhase) p = 2*Math.round(moonCoor.moonAge / (90.*DEG));
	  else p = 2*Math.floor(moonCoor.moonAge / (90.*DEG))+1;
	  moonCoor.moonPhase = phases[p];

	  moonCoor.sign = Sign(moonCoor.lon);
	  return (float) moonCoor.lonDec;
	return 0;
	}
	 */
    }

    public static CWPoint ecliptic2AzimutCoos(CWPoint onEarth, double julianDate, CWPoint ecliptic) {
	CWPoint equat = ecliptic2Equatorial(ecliptic, julianDate);
	return equatorial2AzimutCoos(onEarth, julianDate, equat);
    }

    /**
     * convert rektaszension alpha and deklination delta to azimuth / elevation
     * 
     * @param onEarth
     *            pos. on earth for which the azimut is wanted
     * @param julianDate
     * @param equatorial
     *            : lonDec = rektaszension (alpha), latDec = Deklination (delta)
     * @return lonDec: azimuth in degrees from north, lat: elevation in degrees from horizont alogithism from wikipedia sonnenbahn
     */
    public static CWPoint equatorial2AzimutCoos(CWPoint onEarth, double julianDate, CWPoint equatorial) {
	double stunde = ((julianDate + 0.5) % 1) * 24;
	double jd0 = julianDate - stunde / 24; // julian date at UTC 0:00
	double t0 = (jd0 - 2451545.) / 36525.; // schon in t0 bzw jd0 richtig berechnet?
	double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * stunde; // + (double)minute/60.);
	double theta = thetaHG * 15. + onEarth.lonDec;
	double tau = (theta - equatorial.lonDec) / 180 * Math.PI;
	double phi = onEarth.latDec / 180 * Math.PI;
	double azimutNenner = Math.cos(tau) * Math.sin(phi) - Math.tan(equatorial.latDec / 180 * Math.PI) * Math.cos(onEarth.latDec / 180 * java.lang.Math.PI);
	float azimut = (float) java.lang.Math.atan(java.lang.Math.sin((theta - equatorial.lonDec) / 180 * Math.PI) / azimutNenner);
	azimut = (float) (azimut * 180f / java.lang.Math.PI);
	if (azimutNenner < 0)
	    azimut += 180.;
	double h = 180 / Math.PI * Math.asin(Math.cos(equatorial.latDec / 180 * Math.PI) * Math.cos(tau) * Math.cos(phi) + Math.sin(equatorial.latDec / 180 * Math.PI) * Math.sin(phi));
	// null = Sueden auf Null = Norden umrechnen
	azimut += 180.;
	if (azimut > 360.)
	    azimut -= 360.;
	return new CWPoint(h, azimut);
    }

    /**
     * convert from eliptical to equatorial coordinates
     * 
     * @param juliandate
     * @param eklipCoo
     *            ecliptic coos in degrees
     * @return lon: Deklination (delta), lat: Rektaszension (alpha) in degree this is adopted from http://lexikon.astronomie.info/java/sunmoon/sunmoon.html
     */
    public static CWPoint ecliptic2Equatorial(CWPoint eklipCoo, double juliandate) {
	double T = (juliandate - 2451545.0) / 36525.; // Epoch 2000 January 1.5
	double eps = (23. + (26 + 21.45 / 60) / 60 + T * (-46.815 + T * (-0.0006 + T * 0.00181)) / 3600) / 180 * java.lang.Math.PI; // schiefe der Ekliptik
	double coseps = Math.cos(eps);
	double sineps = Math.sin(eps);

	double sinlon = Math.sin(eklipCoo.lonDec / 180 * Math.PI);
	CWPoint equatorial = new CWPoint();
	equatorial.lonDec = (180 / Math.PI * Math.atan2((sinlon * coseps - Math.tan(eklipCoo.latDec / 180 * Math.PI) * sineps), Math.cos(eklipCoo.lonDec / 180 * Math.PI))) % 360; // rektaszension (alpha)
	equatorial.latDec = 180 / Math.PI * Math.asin(Math.sin(eklipCoo.latDec / 180 * Math.PI) * coseps + Math.cos(eklipCoo.latDec / 180 * Math.PI) * sineps * sinlon); // deklination (delta)

	return equatorial;
    }
}
