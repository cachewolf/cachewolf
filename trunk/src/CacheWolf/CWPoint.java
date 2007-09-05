
package CacheWolf;


import ewe.sys.Double;
import ewe.sys.Locale;
import ewe.sys.Convert;
import CacheWolf.navi.TrackPoint;

import com.bbn.openmap.proj.coords.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.*;
import com.stevesoft.ewe_pat.Regex;

import ewe.sys.Vm;




/**
 * Class for getting an setting coords in different formats
 * and for doing projection and calculation of bearing and
 * distance
 *
 */
public class CWPoint extends TrackPoint{
	public MGRSPoint utm = new MGRSPoint();
	public boolean utmValid = false;

	public static final int DD = 0;
	public static final int DMM = 1;
	public static final int DMS = 2;
	public static final int UTM = 3;
	public static final int CW = 4;
	public static final int REGEX = 5;
	
	/**
	 * Create CWPoint by using lat and lon 
	 * @param lat Latitude as decimal
	 * @param lon Longitude as decimal
	 */
	public CWPoint(double lat, double lon) {
		super(lat, lon);
		this.utmValid = false;
	}

	/**
	 * Creates an empty CWPoint, use set methods for filling 
	 */
	
	public CWPoint() {
		super(-361,-361); // construct with unvalid == unset lat/lon 
		this.utmValid = false;
		
	}

	/**
	 * Create CWPoint by using a LatLonPoint 
	 * @param CWPoint LatLonPoint
	 */

	public CWPoint(LatLonPoint llPoint){
		super (llPoint.getLatitude(), llPoint.getLongitude());
		this.utmValid = false;
	}

	/**
	 * Create CWPoint by using a CWPoint 
	 * @param CWPoint LatLonPoint
	 */

	public CWPoint(CWPoint cwPoint){
		super(cwPoint.latDec, cwPoint.lonDec);
		this.utmValid = false;
	}

	
	/**
	 * Create CWPoint by using coordinates in "CacheWolf" format 
	 * @param coord  String of type N 49° 33.167 E 011° 21.608
	 * @param format only CWPoint.CW or CWPoint.REGEX is supported
	 */
	public CWPoint(String coord, int format) {
		super(-361,-361);
		set(coord, format);
	}

		
	/**
	 * Create CWPoint 
	 * @param strLatNS "N" or "S"
	 * @param strLatDeg	Degrees of Latitude
	 * @param strLatMin	Minutes of Latitude
	 * @param strLatSec	Seconds of Latitude
	 * @param strLonEW	"E" or "W"
	 * @param strLonDeg	Degrees of Longitude
	 * @param strLonMin	Minutes of Longitude
	 * @param strLonSec	Seconds of Longitude
	 * @param format	Format: DD, DMM, DMS, CW, UTM
	 */
	//TODO Remove ? Only used in OCXMLImporter and TablePanel when reading preferences
	public CWPoint(String strLatNS, String strLatDeg, String strLatMin, String strLatSec,
			     String strLonEW, String strLonDeg, String strLonMin, String strLonSec,
			     int format) {
		set(strLatNS, strLatDeg, strLatMin, strLatSec,
			strLonEW, strLonDeg, strLonMin, strLonSec,
			format);	
	}
	
	
	/**
	 * set lat and lon by parsing coordinates with Regex 
	 * @param coord  String like N 49° 33.167 E 011° 21.608
	 */
	public CWPoint(String coord) {
		set(coord);
	}
	/*public boolean equals (CWPoint p) {
		return super.equals(p);
	}*/

	/**
	 * Set lat and lon 
	 * @param lat Latitude as decimal
	 * @param lon Longitude as decimal
	 */
	public void set (double lat, double lon){
		this.latDec = lat;
		this.lonDec = lon;
		this.utmValid = false;
	}

	/**
	 * Set CWPoint by using a LatLonPoint 
	 * @param CWPoint LatLonPoint
	 */

	public void set (LatLonPoint llPoint){
		this.latDec = llPoint.getLatitude();
		this.lonDec = llPoint.getLongitude();
		this.utmValid = false;
	}

	/**
	 * Set CWPoint by using a CWPoint 
	 * @param CWPoint cwPointt
	 */

	public void set (CWPoint cwPoint){
		this.latDec = cwPoint.latDec;
		this.lonDec = cwPoint.lonDec;
		this.utmValid = false;
	}

	
	/**
	 * set lat and lon by using coordinates in "CacheWolf" format 
	 * @param coord  String of type N 49° 33.167 E 011° 21.608
	 * @param format only CWPoint.CW is supported
	 */
	public void set (String coord, int format) {

		if (coord!=null) {
			switch (format){
			case CW: 	ParseLatLon pll = new ParseLatLon (coord);
				try {
					pll.parse();
					this.latDec = pll.lat2;
					this.lonDec = pll.lon2;
				} catch (Exception e) {
					this.latDec = 91;
					this.lonDec = 361;
					break;
				}
			case REGEX: set(coord);
			break;

			default: 	this.latDec = 91; this.lonDec = 361;
			}
		} else { 
			this.latDec = 91; this.lonDec = 361;
		}
		this.utmValid = false;
	}



	/**
	 * set lat and lon by parsing coordinates with regular expression 
	 * @param coord  String of type N 49° 33.167 E 011° 21.608
	 * 				 	or 			-12.3456 23.4567
	 * 					or			32U 2345234 8902345
	 */
	public void set (String coord) {
		/*		(?: 
					([NSns])\s*([0-9]{1,2})[\s°]+([0-9]{1,2})(?:\s+([0-9]{1,2}))?[,.]([0-9]{1,8})\s* 
					([EWewOo])\s*([0-9]{1,3})[\s°]+([0-9]{1,2})(?:\s+([0-9]{1,2}))?[,.]([0-9]{1,8}) 
					)|(?: 
					  ([+-NnSs]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=\+)|(?=-)|\s+|\s*°\s*)([+-WwEeOo]?[0-9]{1,3})[,.]([0-9]{1,8})\s*[°]? 
					)|(?: 
					   ([0-9]{1,2}[C-HJ-PQ-X])\s*[EeOo]?\s*([0-9]{1,7})\s+[Nn]?\s*([0-9]{1,7}) 
					)
		*/		
				Regex rex=new Regex("(?:" +
									"([NSns])\\s*([0-9]{1,2})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?\\s*" +
									"([EWewOo])\\s*([0-9]{1,3})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?" +
									")|(?:" +
									"(?:([NnSs])\\s*(?![+-]))?"   +     "([+-]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=[+-EeWwOo])|\\s+|\\s*[°\uC2B0]\\s*)" +
								  	"(?:([EeWwOo])\\s*(?![+-]))?"    +     "([+-]?[0-9]{1,3})[,.]([0-9]{1,8})\\s*[°\uC2B0]?" +
									")|(?:" +
									"([0-9]{1,2}[C-HJ-PQ-X])\\s*[EeOo]?\\s*([0-9]{1,7})\\s+[Nn]?\\s*([0-9]{1,7})" +
									")"); 
				this.latDec = -91; // return unset / unvalid values if parsing was not successfull
				this.lonDec = -361;
				rex.search(coord);
				if (rex.stringMatched(1)!= null) { // Std format
					// Handle "E" oder "O" for longitiude
					String strEW = rex.stringMatched(6).toUpperCase();
					if (!strEW.equals("W")) strEW = "E";
					if (rex.stringMatched(4)!=null){ //Seconds available
						set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2),rex.stringMatched(3),rex.stringMatched(4) + "." + rex.stringMatched(5),
							strEW, rex.stringMatched(7),rex.stringMatched(8),rex.stringMatched(9) + "." + rex.stringMatched(10),DMS);
					} else {
						set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2),rex.stringMatched(3)+ "." + rex.stringMatched(5), null,
							strEW, rex.stringMatched(7),rex.stringMatched(8)+ "." + rex.stringMatched(10), null, DMM);
					}
						
				} else if (rex.stringMatched(12) != null){ // Decimal
					
					set(rex.stringMatched(11)==null?"N":rex.stringMatched(11).toUpperCase(), rex.stringMatched(12)+ "." + rex.stringMatched(13), null, null,
						rex.stringMatched(14)==null?"E":rex.stringMatched(14).toUpperCase(), rex.stringMatched(15)+ "." + rex.stringMatched(16), null, null, DD);
				} else if (rex.stringMatched(17) != null){ // UTM
					set(rex.stringMatched(17),rex.stringMatched(19),rex.stringMatched(18)); //parse sequence is E N, but set needs N E
				}
				//else Vm.debug("CWPoint: "+coord+" could not be parsed");
			}	/**
	 * set lat and lon 
	 * @param strLatNS "N" or "S"
	 * @param strLatDeg	Degrees of Latitude
	 * @param strLatMin	Minutes of Latitude
	 * @param strLatSec	Seconds of Latitude
	 * @param strLonEW	"E" or "W"
	 * @param strLonDeg	Degrees of Longitude
	 * @param strLonMin	Minutes of Longitude
	 * @param strLonSec	Seconds of Longitude
	 * @param format	Format: DD, DMM, DMS 
	 */
	public void set(String strLatNS, String strLatDeg, String strLatMin, String strLatSec,
		     String strLonEW, String strLonDeg, String strLonMin, String strLonSec,
		     int format) {
		switch (format){
			case DD: 	this.latDec = Common.parseDouble(strLatDeg);
						this.lonDec = Common.parseDouble(strLonDeg);
						break;
			case DMM: 	this.latDec = Math.abs(Common.parseDouble(strLatDeg)) + Math.abs((Common.parseDouble(strLatMin)/60));
						this.lonDec = Math.abs(Common.parseDouble(strLonDeg)) + Math.abs((Common.parseDouble(strLonMin)/60));
						break;
			case DMS: 	this.latDec = Math.abs(Common.parseDouble(strLatDeg)) + Math.abs((Common.parseDouble(strLatMin)/60))+Math.abs((Common.parseDouble(strLatSec)/3600));
						this.lonDec = Math.abs(Common.parseDouble(strLonDeg)) + Math.abs((Common.parseDouble(strLonMin)/60))+Math.abs((Common.parseDouble(strLonSec)/3600));
						break;
			
			default: 	this.latDec = 91; this.lonDec = 361;
		}
		//makeValid();
		// To avoid changing sign twice if we have something like W -34.2345
		if (strLatNS.trim().equals("S") && this.latDec>0) this.latDec *= -1;
		if (strLonEW.trim().equals("W") && this.lonDec>0) this.lonDec *= -1;
		this.utmValid = false;
	}

	/*
	 * Returns true if the coordinates are valid
	 */
	public boolean isValid() {
		return 	latDec <= 90.0 && latDec >= -90.0 &&
				lonDec <= 360 && lonDec >= -360;
	}
	

	/**
	 * set lat and lon by using UTM coordinates  
	 * @param strZone UTM-zone, e.g. 32U
	 * @param strNorthing Northing component
	 * @param strEasting  Easting component
	 */
	public void set ( String strZone, String strNorthing, String strEasting){
		LatLonPoint ll =  new LatLonPoint();
		
		utm.zone_letter = strZone.charAt(strZone.length()-1);
		utm.zone_number = Convert.toInt(strZone.substring(0,strZone.length()-1));
		utm.northing = (float) Common.parseDouble(strNorthing);
		utm.easting = (float) Common.parseDouble(strEasting);
		
		ll = utm.toLatLonPoint(); // returns null if unvalit UTM-coordinates
		if (ll != null) { 
			this.utmValid = true;
			this.latDec = ll.getLatitude();
			this.lonDec = ll.getLongitude();
		} else {this.latDec = 91; this.lonDec = 361; }
	}

	/**
	 * Get degrees of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatDeg(int format) {
		switch (format) {
		case DD: 	return MyLocale.formatDouble(this.latDec, "00.00000").replace(',','.');
		case CW:
		case DMM:
		case DMS:	return MyLocale.formatDouble((int) Math.abs(this.latDec),"00");
		default: return "";
		}
	}
	
	/**
	 * Get degrees of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonDeg(int format) {
		switch (format) {
		case DD: 	return MyLocale.formatDouble(this.lonDec, "000.00000").replace(',','.');
		case CW:
		case DMM:
		case DMS:	return MyLocale.formatDouble((int) Math.abs(this.lonDec),"000");
		default: 	return ""; 
		}
	}

	/**
	 * Get minutes of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatMin(int format) {
		double latMin=(Math.abs(latDec) - (int)Math.abs(latDec))*60.0;
		switch (format) {
			case DD: 	return "";
			case CW:
			case DMM:	return MyLocale.formatDouble(latMin, "00.000").replace(',','.');
			case DMS:	return MyLocale.formatDouble((int) Math.abs(latMin),"00");
			default: return "";
		}
	}
	/**
	 * Get minutes of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonMin(int format) {
		double lonMin=(Math.abs(lonDec) - (int)Math.abs(lonDec))*60.0;
		switch (format) {
			case DD: 	return "";
			case CW:
			case DMM:	return MyLocale.formatDouble(lonMin, "00.000").replace(',','.');
			case DMS:	return MyLocale.formatDouble((int) Math.abs(lonMin),"00");
			default: return "";
		}
	}

	/**
	 * Get seconds of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatSec(int format) {
		double tmpMin;

		tmpMin = (Math.abs(latDec) - (int)Math.abs(latDec)) * 60;
		switch (format) {
			case DD: 	
			case CW:
			case DMM: 	return "";
			case DMS:	return MyLocale.formatDouble((tmpMin - (int)Math.abs(tmpMin)) * 60, "00.0").replace(',','.');
			default: return "";
		}
	}

	/**
	 * Get seconds of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonSec(int format) {
		double tmpMin;

		tmpMin = (Math.abs(lonDec) - (int)Math.abs(lonDec)) * 60;
		switch (format) {
			case DD: 	
			case CW:
			case DMM: 	return "";
			case DMS:	return MyLocale.formatDouble((tmpMin - (int)Math.abs(tmpMin)) * 60, "00.0").replace(',','.');
			default: return "";
		}
	}

	/**
	 * Get "N" or "S" letter for latitude
	 */
	public String getNSLetter() {
		return 	this.latDec < 0?"S":"N";
	}

	/**
	 * Get "E" or "W" letter for latitude
	 */
	public String getEWLetter() {
		return  this.lonDec < 0?"W":"E";
	}

	/**
	 * Get UTMzonenumber, e.g. 32U
	 */
	public String getUTMZone(){
		checkUTMvalid();
		return Convert.toString(utm.zone_number) + utm.zone_letter;
	}
	
	/**
	 * Get UTM northing
	 */
	public String getUTMNorthing(){
		checkUTMvalid();
		return Convert.toString((long)utm.northing).replace(',','.');
	}

	/**
	 * Get UTM easting
	 */
	public String getUTMEasting() {
		checkUTMvalid();
		return Convert.toString((long)utm.easting).replace(',','.');
	}
	
	/**
	 * Method to calculate a projected waypoint
	 * @param degrees Bearing
	 * @param distance Distance in km
	 * @return projected waypoint
	 */
	public CWPoint project(double degrees, double distance){
		float c, az;
		
		LatLonPoint llsrc = new LatLonPoint(this.latDec, this.lonDec);
		c = (float)(distance/1.852);
		c = (float)(java.lang.Math.PI/(180*60))*c;
		az = (float)((degrees/180)*java.lang.Math.PI);

		LatLonPoint lldst = llsrc.getPoint(c,az);
		
		return new CWPoint(lldst);
	}

	/**
	 * Method to calculate the bearing of a waypoint
	 * @param dest waypoint
	 * @return  bearing of waypoint 361 if this or dest is not valid
	 */	
	public double getBearing(CWPoint dest){
		if (!this.isValid() || dest == null || !dest.isValid()) return 361;
		float az;
		LatLonPoint src = new LatLonPoint(this.latDec, this.lonDec);
		
		az = src.azimuth(new LatLonPoint(dest.latDec, dest.lonDec));
		if (az >= 0)
			return az * 180 /java.lang.Math.PI;
		else
			return (2 * Math.PI + az) * 180 /Math.PI; 
		
	}
	
	/**
	 *	Method to identify one of 16 compass directions based
	 * 	on the bearing. 
	 * @param degrees bearing
	 * @return  direction
	 */	
	public static String getDirection(double degrees){
		return getDirectionFromBearing(degrees);
	}
	
	/**
	 *	Method to identify one of 16 compass directions based
	 * 	on the bearing of the destination waypoint
	 * @param dest waypoint
	 * @return  direction
	 */	
	public String getDirection(CWPoint dest){
		return getDirectionFromBearing(getBearing(dest));
	}


	/**
	 * Method to calculate the distance to a waypoint
	 * @param dest waypoint
	 * @return  distance to waypoint in KM
	 */	
	public double getDistance (CWPoint dest){
		return getDistance(dest.latDec, dest.lonDec);
		
	}

	/**
	 * Method to calculate the distance to a waypoint
	 * @param dest lat, lon
	 * @return  distance to waypoint in KM
	 */	
	public double getDistance (double latDecD, double lonDecD){
		return Length.KM.fromRadians(getDistanceRad(latDecD, lonDecD));
	}

	/**
	 * Method to calculate the distance to a waypoint
	 * @param dest lat, lon
	 * @return  distance to waypoint in Rad
	 */	
	public double getDistanceRad (double latDecD, double lonDecD){
		LatLonPoint src = new LatLonPoint(this.latDec, this.lonDec);
		return src.distance(new LatLonPoint(latDecD, lonDecD));
	}

	

	/**
	 * Returns the string reprenstation of the CWPoint
	 * Format ist CacheWolf (N 49° 33.167 E 011° 21.608), which can be used 
	 * with parseLatLon
	 * @return  string like N 49° 33.167 E 011° 21.608 
	 */	
	public String toString(){
		return toString(CW);
		
	}
	/**
	 * Returns the string representation of the CWPoint
	 * Formats DD, DMM (same as CW), DMS, UTM  
	 * @return  string representation of CWPoint 
	 */	
	public String toString(int format){
		if (!isValid()) return MyLocale.getMsg(999,"not set");
		switch (format) {
		case DD:	return getNSLetter() + " " + STRreplace.replace(getLatDeg(format),"-","") + "° "
						+  getEWLetter() + " " + STRreplace.replace(getLonDeg(format),"-","")+ "°";
		case CW:	format = DMM;	
					return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + " "
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format);
		case DMM:	return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + " "
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format);
		case DMS:	return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + "\' " + getLatSec(format) + "\" " 
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format) + "\' " + getLonSec(format) + "\"";
		case UTM:	return getUTMZone()  + " E " + getUTMEasting()+ " N " + getUTMNorthing();

		default: return "Unknown Format: " + format;

		}

	}
	
	/**
	 * Checks, if the data of utm is valid, if not, utm ist calculated
	 */	
	private void checkUTMvalid() {
		if (this.utmValid) return;
		this.utm = MGRSPoint.LLtoMGRS(new LatLonPoint(this.latDec, this.lonDec));
		this.utmValid = true;
	}
	
	/**
	*	Method to identify one of 16 compass directions based
	* 	on the bearing.
	*/
	private static String getDirectionFromBearing(double wert){
		//System.out.println(wert);
		String strBear = new String();
		double stVal = -11.25;
		if(wert >= stVal) strBear = "N";
		stVal += 22.5;
		if(wert >= stVal) strBear = "NNE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "NE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "ENE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "E";
		stVal += 22.5;
		if(wert >= stVal) strBear = "ESE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "SE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "SSE";
		stVal += 22.5;
		if(wert >= stVal) strBear = "S";
		stVal += 22.5;
		if(wert >= stVal) strBear = "SSW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "SW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "WSW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "W";
		stVal += 22.5;
		if(wert >= stVal) strBear = "WNW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "NW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "NNW";
		stVal += 22.5;
		if(wert >= stVal) strBear = "N";
		stVal += 22.5;
		return strBear;
	} //getBearing

	
}
