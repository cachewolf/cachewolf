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
package CacheWolf.database;

import CacheWolf.navi.GeodeticCalculator;
import CacheWolf.navi.ParseLatLon;
import CacheWolf.navi.ProjectedPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;

import com.stevesoft.ewe_pat.Regex;

/**
 * Class for getting an setting coords in different formats
 * and for doing projection and calculation of bearing and
 * distance
 * 
 */
public class CWPoint extends CoordinatePoint {

    /** Degrees/Radians conversion constant. */
    static private final double PiOver180 = Math.PI / 180.0;

    /**
     * Create CWPoint by using lat and lon
     * 
     * @param lat
     *            Latitude as decimal
     * @param lon
     *            Longitude as decimal
     */
    public CWPoint(double lat, double lon) {
	super(lat, lon);
    }

    /**
     * Creates an inValid CWPoint, use set methods for filling
     */
    public CWPoint() {
	super();
    }

    /**
     * Create CWPoint by using a CoordinatePoint
     * 
     * @param coordinatePoint
     */
    public CWPoint(CoordinatePoint coordinatePoint) {
	super(coordinatePoint.latDec, coordinatePoint.lonDec);
    }

    /**
     * Create CWPoint by using coordinates in "CacheWolf" format
     * 
     * @param coord
     *            String of type N 49° 33.167 E 011° 21.608
     * @param format
     *            only CWPoint.CW or CWPoint.REGEX is supported
     */
    public CWPoint(String coord, int format) {
	super(-361, -361);
	set(coord, format);
    }

    /**
     * set lat and lon by parsing coordinates with Regex
     * 
     * @param coord
     *            String like N 49° 33.167 E 011° 21.608
     */
    public CWPoint(String coord) {
	set(coord);
    }

    /*public boolean equals (CWPoint p) {
    	return super.equals(p);
    }*/

    /**
     * Set lat and lon
     * 
     * @param lat
     *            Latitude as decimal
     * @param lon
     *            Longitude as decimal
     */
    public void set(double lat, double lon) {
	this.latDec = lat;
	this.lonDec = lon;
    }

    /**
     * Set CWPoint by using a CWPoint
     * 
     * @param CWPoint
     *            cwPoint
     */

    public void set(CoordinatePoint cwPoint) {
	this.latDec = cwPoint.latDec;
	this.lonDec = cwPoint.lonDec;
    }

    /**
     * set lat and lon by using coordinates in "CacheWolf" format
     * 
     * @param coord
     *            String of type N 49° 33.167 E 011° 21.608
     * @param format
     *            only CWPoint.CW is supported
     */
    public void set(String coord, int format) {

	if (coord != null) {
	    switch (format) {
	    case TransformCoordinates.CW:
		ParseLatLon pll = new ParseLatLon(coord);
		try {
		    pll.parse();
		    this.latDec = pll.lat2;
		    this.lonDec = pll.lon2;
		} catch (Exception e) {
		    this.latDec = 91;
		    this.lonDec = 361;
		    break;
		}
	    case TransformCoordinates.REGEX:
		set(coord);
		break;

	    default:
		this.latDec = 91;
		this.lonDec = 361;
	    }
	} else {
	    this.latDec = 91;
	    this.lonDec = 361;
	}
    }

    /**
     * set lat and lon by parsing coordinates with regular expression
     * 
     * @param coord
     *            String of type N 49° 33.167 E 011° 21.608
     *            or -12.3456 23.4567
     *            or 32U 2345234 8902345
     */
    public void set(String coord) {
	//replace non-breaking-spaces by normal spaces
	coord = coord.replace((char) 0xA0, ' ');
	/*		(?: 
				([NSns])\s*([0-9]{1,2})[\s°]+([0-9]{1,2})(?:\s+([0-9]{1,2}))?[,.]([0-9]{1,8})\s* 
				([EWewOo])\s*([0-9]{1,3})[\s°]+([0-9]{1,2})(?:\s+([0-9]{1,2}))?[,.]([0-9]{1,8}) 
				)|(?: 
				  ([+-NnSs]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=\+)|(?=-)|\s+|\s*°\s*)([+-WwEeOo]?[0-9]{1,3})[,.]([0-9]{1,8})\s*[°]? 
				)|(?: 
				   ([0-9]{1,2}[C-HJ-PQ-X])\s*[EeOo]?\s*([0-9]{1,7})\s+[Nn]?\s*([0-9]{1,7}) 
				)
	*/
	String crsid = null;
	if ((coord.length() >= 2) && (coord.charAt(2) == '.') && (coord.indexOf(' ') >= 0)) {
	    // first 2 letters = Internet domain of projected area
	    crsid = coord.substring(0, coord.indexOf(' '));
	    if (TransformCoordinates.getLocalSystemCode(crsid) != -1) {
		coord = coord.substring(coord.indexOf(' ') + 1, coord.length());
	    }
	}
	Regex rex = new Regex("(?:" + "([NSns])\\s*([0-9]{1,2})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?\\s*"
		+ "[,./_;+:-]*\\s*"
		+ // allow N xx xx.xxx / E xxx xx.xxx
		"([EWewOo])\\s*([0-9]{1,3})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?" + ")|(?:" + "(?:([NnSs])\\s*(?![+-]))?"
		+ "([+-]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=[+-EeWwOo])|\\s+|\\s*[°\uC2B0]\\s*)" + "[,./_;:]*\\s*" + "(?:([EeWwOo])\\s*(?![+-]))?" + "([+-]?[0-9]{1,3})[,.]([0-9]{1,8})\\s*[°\uC2B0]?" + ")|(?:"
		+ "([0-9]{1,2}[C-HJ-PQ-X])\\s*[EeOo]?\\s*([0-9]{1,7})\\s+[Nn]?\\s*([0-9]{1,7})" + ")|(?:" + "[Rr]:?\\s*([+-]?[0-9]{1,7})\\s+[Hh]:?\\s*([+-]?[0-9]{1,7})" + ")|(?:" + "([\\-]{0,1}[0-9]{1,8})\\s+([\\-]{0,1}[0-9]{1,8})" + // projected easting northing  
		")");
	this.makeInvalid(); //return unset / unvalid values if parsing was not successfull
	rex.search(coord);
	if (rex.stringMatched(1) != null) { // Std format
	    // Handle "E" or "O" for longitiude
	    String strEW = rex.stringMatched(6).toUpperCase();
	    if (!strEW.equals("W"))
		strEW = "E";
	    if (rex.stringMatched(4) != null) { //Seconds available
		set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2), rex.stringMatched(3), rex.stringMatched(4) + "." + rex.stringMatched(5), strEW, rex.stringMatched(7), rex.stringMatched(8),
			rex.stringMatched(9) + "." + rex.stringMatched(10), TransformCoordinates.DMS);
	    } else {
		set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2), rex.stringMatched(3) + "." + rex.stringMatched(5), null, strEW, rex.stringMatched(7), rex.stringMatched(8) + "." + rex.stringMatched(10), null, TransformCoordinates.DMM);
	    }

	} else if (rex.stringMatched(12) != null) { // Decimal
	    set(rex.stringMatched(11) == null ? "N" : rex.stringMatched(11).toUpperCase(), rex.stringMatched(12) + "." + rex.stringMatched(13), null, null, rex.stringMatched(14) == null ? "E" : rex.stringMatched(14).toUpperCase(),
		    rex.stringMatched(15) + "." + rex.stringMatched(16), null, null, TransformCoordinates.DD);
	} else if (rex.stringMatched(17) != null) { // UTM
	    set(rex.stringMatched(19), rex.stringMatched(18), rex.stringMatched(17)); //parse sequence is E N, but set needs N E
	} else if (rex.stringMatched(20) != null) { // GK
	    set(rex.stringMatched(21), rex.stringMatched(20), TransformCoordinates.LOCALSYSTEM_DEFAULT);
	} else if (rex.stringMatched(22) != null) { // general projected coordinate reference system
	    if (crsid != null) {
		int ls = TransformCoordinates.getLocalSystemCode(crsid);
		if (ls == -1)
		    makeInvalid();
		else
		    set(rex.stringMatched(23), rex.stringMatched(22), ls);
	    }
	}
    }

    /**
     * set lat and lon
     * 
     * @param strLatNS
     *            "N" or "S"
     * @param strLatDeg
     *            Degrees of Latitude
     * @param strLatMin
     *            Minutes of Latitude
     * @param strLatSec
     *            Seconds of Latitude
     * @param strLonEW
     *            "E" or "W"
     * @param strLonDeg
     *            Degrees of Longitude
     * @param strLonMin
     *            Minutes of Longitude
     * @param strLonSec
     *            Seconds of Longitude
     * @param format
     *            Format: DD, DMM, DMS
     */
    public void set(String strLatNS, String strLatDeg, String strLatMin, String strLatSec, String strLonEW, String strLonDeg, String strLonMin, String strLonSec, int format) {
	switch (format) {
	case TransformCoordinates.DD:
	    this.latDec = Common.parseDouble(strLatDeg);
	    this.lonDec = Common.parseDouble(strLonDeg);
	    break;
	case TransformCoordinates.DMM:
	    this.latDec = Math.abs(Common.parseDouble(strLatDeg)) + Math.abs((Common.parseDouble(strLatMin) / 60));
	    this.lonDec = Math.abs(Common.parseDouble(strLonDeg)) + Math.abs((Common.parseDouble(strLonMin) / 60));
	    break;
	case TransformCoordinates.DMS:
	    this.latDec = Math.abs(Common.parseDouble(strLatDeg)) + Math.abs((Common.parseDouble(strLatMin) / 60)) + Math.abs((Common.parseDouble(strLatSec) / 3600));
	    this.lonDec = Math.abs(Common.parseDouble(strLonDeg)) + Math.abs((Common.parseDouble(strLonMin) / 60)) + Math.abs((Common.parseDouble(strLonSec) / 3600));
	    break;

	default:
	    this.latDec = 91;
	    this.lonDec = 361;
	}
	//makeValid();
	// To avoid changing sign twice if we have something like W -34.2345
	if (strLatNS.trim().equals("S") && this.latDec > 0)
	    this.latDec *= -1;
	if (strLonEW.trim().equals("W") && this.lonDec > 0)
	    this.lonDec *= -1;
    }

    /**
     * sets by UTM with respect to WGS84
     * 
     * @param UTMNorthing
     * @param UTMEasting
     * @param utmzone
     */
    public void set(String UTMNorthing, String UTMEasting, String utmzone) {
	try {
	    ProjectedPoint utm = new ProjectedPoint(new CWPoint(Common.parseDouble(UTMNorthing), Common.parseDouble(UTMEasting)), utmzone, TransformCoordinates.LOCALSYSTEM_UTM_WGS84, true);
	    set(TransformCoordinates.ProjectedToWgs84(utm, TransformCoordinates.LOCALSYSTEM_UTM_WGS84, true));
	} catch (Exception e) {
	    makeInvalid();
	}
    }

    /**
     * shift the point
     * 
     * @param meters
     *            positiv to north (east), negativ to south (west)
     * @param direction
     *            0 north(meters negative=south), 1 east(meters negative=west) 2 south(meters negative=north) 3 west(meters negative=east)
     */
    public void shift(double meters, int direction) {
	double meters2deglon = 1 / (1000 * (new CWPoint(0, 0)).getDistance(new CWPoint(1, 0)));
	switch (direction) {
	// TODO use ellipsoid distance calculations for better accuracy
	case 0:
	    latDec += meters * meters2deglon;
	    return;
	case 1:
	    lonDec += meters * (meters2deglon / Math.cos(latDec / 180 * Math.PI));
	    return;
	case 2:
	    latDec += -meters * meters2deglon;
	    return;
	case 3:
	    lonDec += -meters * (meters2deglon / Math.cos(latDec / 180 * Math.PI));
	    return;
	}
    }

    /**
     * mark the Point as invalid
     * 
     */
    public void makeInvalid() {
	latDec = -361;
	lonDec = 91;
    }

    /**
     * set lat and lon by using a local coordinates system
     * 
     * @param strEasting
     *            Easting component
     * @param strNorthing
     *            Northing component
     * @param localCooSystem
     *            one of TransformCoordinates.LOCALSYSTEM_XXX
     */
    public void set(String strNorthing, String strEasting, int localCooSystem) {
	try {
	    CWPoint pp = new CWPoint(Common.parseDouble(strNorthing), Common.parseDouble(strEasting));
	    ProjectedPoint gk = new ProjectedPoint(pp, localCooSystem, true, true);
	    set(TransformCoordinates.ProjectedToWgs84(gk, localCooSystem, true));
	} catch (Exception e) {
	    makeInvalid();
	}
    }

    /**
     * set lat and lon by using UTM coordinates
     * 
     * @param strEasting
     *            Easting component
     * @param strNorthing
     *            Northing component
     * @param localCooSystem
     *            one of TransformCoordinates.LOCALSYSTEM_XXX which requires an explicit zone
     */
    public void set(String strNorthing, String strEasting, String zone, int localCooSystem) {
	try {
	    CWPoint pp = new CWPoint(Common.parseDouble(strNorthing), Common.parseDouble(strEasting));
	    ProjectedPoint gk = new ProjectedPoint(pp, zone, localCooSystem, true);
	    set(TransformCoordinates.ProjectedToWgs84(gk, localCooSystem, true));
	} catch (Exception e) {
	    makeInvalid();
	}
    }

    /**
     * Get degrees of latitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLatDeg(int format) {
	switch (format) {
	case TransformCoordinates.DD:
	    return MyLocale.formatDouble(this.latDec, "00.00000").replace(',', '.');
	case TransformCoordinates.DMM:
	case TransformCoordinates.DMS:
	    return getDMS(latDec, 0, format);
	default:
	    return "";
	}
    }

    /**
     * Get degrees of longitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLonDeg(int format) {
	switch (format) {
	case TransformCoordinates.DD:
	    return MyLocale.formatDouble(this.lonDec, "000.00000").replace(',', '.');
	case TransformCoordinates.DMM:
	case TransformCoordinates.DMS:
	    return (((lonDec < 100.0) && (lonDec > -100.0)) ? "0" : "") + getDMS(lonDec, 0, format);
	default:
	    return "";
	}
    }

    /**
     * Get minutes of latitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLatMin(int format) {
	return getDMS(latDec, 1, format);
    }

    /**
     * Get minutes of longitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLonMin(int format) {
	return getDMS(lonDec, 1, format);
    }

    /**
     * Get seconds of latitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLatSec(int format) {
	return getDMS(latDec, 2, format);
    }

    /**
     * Get seconds of longitude in different formats
     * 
     * @param format
     *            Format: DD, DMM, DMS,
     */
    public String getLonSec(int format) {
	return getDMS(lonDec, 2, format);
    }

    /**
     * Returns the degrees or minutes or seconds (depending on parameter what) formatted as a string
     * To determine the degrees, we need to calculate the minutes (and seconds) just in case rounding errors
     * propagate. Equally we need to know the seconds to determine the minutes value.
     * 
     * @param deg
     *            The coordinate in degrees
     * @param what
     *            0=deg, 1=min, 2=sec
     * @param format
     *            DD,CW,DMM,DMS
     * @return
     */
    private String getDMS(double deg, int what, int format) {
	deg = Math.abs(deg);
	long iDeg = (int) deg;
	double tmpMin, tmpSec;
	tmpMin = (deg - iDeg) * 60.0;
	switch (format) {
	case TransformCoordinates.DD:
	    return "";
	case TransformCoordinates.DMM:
	    // Need to check if minutes would round up to 60
	    if (java.lang.Math.round(tmpMin * 1000.0) == 60000) {
		tmpMin = 0;
		iDeg++;
	    }
	    switch (what) {
	    case 0:
		return MyLocale.formatLong(iDeg, "00");
	    case 1:
		return MyLocale.formatDouble(tmpMin, "00.000").replace(',', '.');
	    case 2:
		return "";
	    }
	case TransformCoordinates.DMS:
	    tmpSec = (tmpMin - (int) tmpMin) * 60.0;
	    tmpMin = (int) tmpMin;
	    // Check if seconds round up to 60 
	    if (java.lang.Math.round(tmpSec * 10.0) == 600) {
		tmpSec = 0;
		tmpMin = tmpMin + 1.0;
	    }
	    // Check if minutes round up to 60
	    if (java.lang.Math.round(tmpMin) == 60) {
		tmpMin = 0;
		iDeg++;
	    }
	    switch (what) {
	    case 0:
		return MyLocale.formatLong(iDeg, "00");
	    case 1:
		return MyLocale.formatDouble(tmpMin, "00");
	    case 2:
		return MyLocale.formatDouble(tmpSec, "00.00").replace(',', '.');
	    }
	}
	return ""; // Dummy to keep compiler happy
    }

    /**
     * Get "N" or "S" letter for latitude
     */
    public String getNSLetter() {
	String result = "N";
	if (this.latDec >= -90 && this.latDec < 0) {
	    result = "S";
	}
	return result;
    }

    /**
     * Get "E" or "W" letter for latitude
     */
    public String getEWLetter() {
	String result = "E";
	if (this.lonDec >= -180 && this.lonDec < 0) {
	    result = "W";
	}
	return result;
    }

    /**
     * Method to calculate a projected waypoint
     * 
     * @param degrees
     *            Bearing
     * @param distance
     *            Distance in km
     * @return projected waypoint
     */
    public CWPoint project(double degrees, double distance) {
	return new CWPoint(GeodeticCalculator.calculateEndingGlobalCoordinates(TransformCoordinates.WGS84, this, degrees, distance * 1000.0));
    }

    /**
     * Method to calculate the bearing of a waypoint
     * 
     * @param dest
     *            waypoint
     * @return bearing of waypoint 361 if this or dest is not valid
     */
    public double getBearing(CWPoint dest) {
	if (!this.isValid() || dest == null || !dest.isValid())
	    return 361;

	return GeodeticCalculator.calculateBearing(TransformCoordinates.WGS84, this, dest);
    }

    /**
     * Method to identify one of 16 compass directions based
     * on the bearing.
     * 
     * @param degrees
     *            bearing
     * @return direction
     */
    public static String getDirection(double degrees) {
	return getDirectionFromBearing(degrees);
    }

    /**
     * Method to identify one of 16 compass directions based
     * on the bearing of the destination waypoint
     * 
     * @param dest
     *            waypoint
     * @return direction
     */
    public String getDirection(CWPoint dest) {
	return getDirectionFromBearing(getBearing(dest));
    }

    /**
     * Method to calculate the distance to a waypoint
     * 
     * @param dest
     *            waypoint
     * @return distance to waypoint in KM
     */
    public double getDistance(CWPoint dest) {
	return GeodeticCalculator.calculateDistance(TransformCoordinates.WGS84, this, dest) / 1000.0;
    }

    /**
     * Method to calculate the distance to a waypoint
     * 
     * @param dest
     *            lat, lon
     * @return distance to waypoint in KM
     */
    public double getDistance(double latDecD, double lonDecD) {
	return getDistance(new CWPoint(latDecD, lonDecD));
    }

    /**
     * Method to calculate the distance to a waypoint
     * 
     * @param dest
     *            lat, lon
     * @return distance to waypoint in Rad
     */
    public double getDistanceRad(double latDecD, double lonDecD) {
	double phi1 = this.latDec * PiOver180;
	double lambda0 = this.lonDec * PiOver180;
	double phi = latDecD * PiOver180;
	double lambda = lonDecD * PiOver180;
	double pdiff = Math.sin(((phi - phi1) / 2.0));
	double ldiff = Math.sin((lambda - lambda0) / 2.0);
	double rval = Math.sqrt((pdiff * pdiff) + Math.cos(phi1) * Math.cos(phi) * (ldiff * ldiff));

	return 2.0 * Math.asin(rval);
    }

    public double getDistanceRad(CWPoint ll) {
	return getDistanceRad(ll.latDec, ll.lonDec);
    }

    /**
     * Returns the string reprenstation of the CWPoint
     * Format ist CacheWolf (N 49° 33.167 E 011° 21.608), which can be used
     * with parseLatLon
     * 
     * @return string like N 49° 33.167 E 011° 21.608
     */
    public String toString() {
	return toString(TransformCoordinates.CW);

    }

    /**
     * Returns the string representation of the CWPoint
     * Formats DD, DMM (same as CW), DMS, UTM
     * 
     * @return string representation of CWPoint
     */
    public String toString(int format) {
	if (!isValid())
	    return MyLocale.getMsg(999, "not set");
	switch (format) {
	case TransformCoordinates.DD:
	    return getNSLetter() + " " + STRreplace.replace(getLatDeg(format), "-", "") + "° " + getEWLetter() + " " + STRreplace.replace(getLonDeg(format), "-", "") + "°";
	case TransformCoordinates.DMM:
	    return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + " " + getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format);
	case TransformCoordinates.DMS:
	    return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + "\' " + getLatSec(format) + "\" " + getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format) + "\' " + getLonSec(format) + "\"";
	case TransformCoordinates.LAT_LON:
	    return getLatDeg(TransformCoordinates.DD) + "," + getLonDeg(TransformCoordinates.DD);
	case TransformCoordinates.LON_LAT:
	    return getLonDeg(TransformCoordinates.DD) + "," + getLatDeg(TransformCoordinates.DD);
	    //case TransformCoordinates.CUSTOM:	return getGermanGkCoordinates();
	default:
	    return TransformCoordinates.getLocalSystem(format).id + " " + TransformCoordinates.wgs84ToLocalsystem(this, format).toHumanReadableString();
	    //return "Unknown Format: " + format;

	}

    }

    /**
     * Method to identify one of 16 compass directions based
     * on the bearing.
     */
    private static String getDirectionFromBearing(double wert) {
	//System.out.println(wert);
	String strBear = new String();
	double stVal = -11.25;
	if (wert >= stVal)
	    strBear = "N";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "NNE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "NE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "ENE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "E";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "ESE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "SE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "SSE";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "S";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "SSW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "SW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "WSW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "W";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "WNW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "NW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "NNW";
	stVal += 22.5;
	if (wert >= stVal)
	    strBear = "N";
	stVal += 22.5;
	return strBear;
    } //getBearing

}
