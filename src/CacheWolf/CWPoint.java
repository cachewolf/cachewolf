
package CacheWolf;


import ewe.sys.Double;
import ewe.sys.Locale;
import ewe.sys.Convert;
import com.bbn.openmap.proj.coords.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.*;
import ewe.sys.Vm;




/**
 * Class for getting an setting coords in different formats
 * and for doing projection and calculation of bearing and
 * distance
 *
 */
public class CWPoint {
	public double latDec, lonDec;
	MGRSPoint utm = new MGRSPoint();
	boolean utmValid = false;
	Locale l = new Locale();

	static protected final int DD = 0;
	static protected final int DMM = 1;
	static protected final int DMS = 2;
	static protected final int UTM = 3;
	static protected final int CW = 4;

	
	/**
	 * Create CWPoint by using lat and lon 
	 * @param lat Latitude as decimal
	 * @param lon Longitude as decimal
	 */
	public CWPoint(double lat, double lon) {
		this.latDec = lat;
		this.lonDec = lon;
		this.utmValid = false;
	}

	/**
	 * Creates an empty CWPoint, use set methods for filling 
	 */
	
	public CWPoint() {
		this.latDec = 0;
		this.lonDec = 0;
		this.utmValid = false;
		
	}

	/**
	 * Create CWPoint by using a LatLonPoint 
	 * @param CWPoint LatLonPoint
	 */

	public CWPoint(LatLonPoint llPoint){
		this.latDec = llPoint.getLatitude();
		this.lonDec = llPoint.getLongitude();
		this.utmValid = false;
	}

	/**
	 * Create CWPoint by using a CWPoint 
	 * @param CWPoint LatLonPoint
	 */

	public CWPoint(CWPoint cwPoint){
		this.latDec = cwPoint.latDec;
		this.lonDec = cwPoint.lonDec;
		this.utmValid = false;
	}

	
	/**
	 * Create CWPoint by using coordinates in "CacheWolf" format 
	 * @param coord  String of type N 49° 33.167 E 011° 21.608
	 * @param format only CWPoint.CW is supported
	 */
	public CWPoint(String coord, int format) {
		switch (format){
		case CW: 	ParseLatLon pll = new ParseLatLon (coord);
					pll.parse();
					this.latDec = pll.lat2;
					this.lonDec = pll.lon2;
					break;
		default: 	this.latDec = 0; this.lonDec = 0;
		}
		this.utmValid = false;
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
	public CWPoint(String strLatNS, String strLatDeg, String strLatMin, String strLatSec,
			     String strLonEW, String strLonDeg, String strLonMin, String strLonSec,
			     int format) {
		set(strLatNS, strLatDeg, strLatMin, strLatSec,
			strLonEW, strLonDeg, strLonMin, strLonSec,
			format);	
	}
	
	
	/**
	 * Create CWPoint using UTM coordinates  
	 * @param strZone UTM-zone, e.g. 32U
	 * @param strNorthing Northing component
	 * @param strEasting  Easting component
	 */
	public CWPoint ( String strZone, String strNorthing, String strEasting){
		set(strZone, strNorthing, strEasting);
	}

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
		switch (format){
		case CW: 	ParseLatLon pll = new ParseLatLon (coord);
					pll.parse();
					this.latDec = pll.lat2;
					this.lonDec = pll.lon2;

					break;
		default: 	this.latDec = 0; this.lonDec = 0;
		}
		this.utmValid = false;
	}

	/**
	 * set lat and lon 
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
	public void set(String strLatNS, String strLatDeg, String strLatMin, String strLatSec,
		     String strLonEW, String strLonDeg, String strLonMin, String strLonSec,
		     int format) {
		switch (format){
		case DD: 	this.latDec = Common.parseDouble(strLatDeg);
					this.lonDec = Common.parseDouble(strLonDeg);
					break;
		case DMM: 	this.latDec = Common.parseDouble(strLatDeg) + (Common.parseDouble(strLatMin)/60);
					this.lonDec = Common.parseDouble(strLonDeg) + (Common.parseDouble(strLonMin)/60);
					break;
		case DMS: 	this.latDec = Common.parseDouble(strLatDeg) + (Common.parseDouble(strLatMin)/60)+(Common.parseDouble(strLatSec)/3600);;
					this.lonDec = Common.parseDouble(strLonDeg) + (Common.parseDouble(strLonMin)/60)+(Common.parseDouble(strLonSec)/3600);
					break;
					
		default: 	this.latDec = 0; this.lonDec = 0;
		}
		if (strLatNS.trim().equals("S")) this.latDec *= -1;
		if (strLonEW.trim().equals("W")) this.lonDec *= -1;
		this.utmValid = false;
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
		
		ll = utm.toLatLonPoint();
		this.utmValid = true;
		this.latDec = ll.getLatitude();
		this.lonDec = ll.getLongitude();
	}

	/**
	 * Get degrees of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatDeg(int format) {
		Double latDeg = new Double();
		
		latDeg.set(java.lang.Math.abs(this.latDec));
		
		switch (format) {
		case DD: 	return l.format(Locale.FORMAT_PARSE_NUMBER,latDeg, "00.00000").replace(',','.');
		case CW:
		case DMM:
		case DMS:	return latDeg.toString(2,0,Double.TRUNCATE|Double.ZERO_FILL);
		default: return "";
		}
	}
	/**
	 * Get degrees of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonDeg(int format) {
		Double lonDeg = new Double();
		
		//lonDeg.set(java.lang.Math.abs(this.lonDec));
		lonDeg.set(this.lonDec);
		
		switch (format) {
		case DD: 	return l.format(Locale.FORMAT_PARSE_NUMBER,lonDeg, "000.00000").replace(',','.');
		case CW:
		case DMM:
		case DMS:	return lonDeg.toString(3,0,Double.TRUNCATE|Double.ZERO_FILL);
		default: 	return "";
		}
	}

	/**
	 * Get minutes of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatMin(int format) {
		Double latMin = new Double();
		double tmp, lat;
		lat = this.latDec<0?-1*this.latDec:this.latDec;
		tmp = (int)java.lang.Math.abs(lat);
		latMin.set((lat - tmp) * 60);

		
		switch (format) {
		case DD: 	return "";
		case CW:
		case DMM:	return l.format(Locale.FORMAT_PARSE_NUMBER,latMin, "00.000").replace(',','.');
		case DMS:	return latMin.toString(2,0,Double.TRUNCATE|Double.ZERO_FILL);	
		default: return "";
		}
	}
	/**
	 * Get minutes of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonMin(int format) {
		Double lonMin = new Double();
		double tmp, lon;
		lon = this.lonDec<0?-1*this.lonDec:this.lonDec;
		tmp = (int)java.lang.Math.abs(lon);
		lonMin.set((lon - tmp) * 60);
		
		switch (format) {
		case DD: 	return "";
		case CW:
		case DMM:	return l.format(Locale.FORMAT_PARSE_NUMBER,lonMin, "00.000").replace(',','.');
		case DMS:	return lonMin.toString(2,0,Double.TRUNCATE|Double.ZERO_FILL);
		default: return "";
		}
	}

	/**
	 * Get seconds of latitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLatSec(int format) {
		Double latSec = new Double();
		double tmpMin, tmpSec, tmp, lat;

		lat = this.latDec<0?-1*this.latDec:this.latDec;
		tmp = (int)java.lang.Math.abs(lat);
		tmpMin = (lat - tmp) * 60;
		tmpSec = (int)java.lang.Math.abs(tmpMin);
		latSec.set((tmpMin - tmpSec) * 60);
		
		switch (format) {
		case DD: 	
		case CW:
		case DMM: 	return "";
		case DMS:	return l.format(Locale.FORMAT_PARSE_NUMBER,latSec, "00.0").replace(',','.');
		default: return "";
		}
	}

	/**
	 * Get seconds of longitude in different formats
	 * @param format	Format: DD, DMM, DMS,
	 */
	public String getLonSec(int format) {
		Double lonSec = new Double();
		double tmpMin, tmpSec, tmp, lon;

		lon = this.lonDec<0?-1*this.lonDec:this.lonDec;
		tmp = (int)java.lang.Math.abs(lon);
		tmpMin = (lon - tmp) * 60;
		tmpSec = (int)java.lang.Math.abs(tmpMin);
		lonSec.set((tmpMin - tmpSec) * 60);
		
		switch (format) {
		case DD: 	
		case CW:
		case DMM: 	return "";
		case DMS:	return l.format(Locale.FORMAT_PARSE_NUMBER,lonSec, "00.0").replace(',','.');
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
	 * @return  bearing of waypoint
	 */	
	public double getBearing(CWPoint dest){
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
	 * @param dest waypoint
	 * @return  distance to waypoint in KM
	 */	
	public double getDistance (double latDecD, double lonDecD){
		return Length.KM.fromRadians(getDistanceRad(latDecD, lonDecD));
	}

	/**
	 * Method to calculate the distance to a waypoint
	 * @param dest waypoint
	 * @return  distance to waypoint in KM
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
		
		switch (format) {
		case DD:	return getNSLetter() + " " + getLatDeg(format) + "° "
						+  getEWLetter() + " " + getLonDeg(format)+ "° ";
		case CW:	format = DMM;	
					return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + " "
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format);
		case DMM:	return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + " "
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format);
		case DMS:	return getNSLetter() + " " + getLatDeg(format) + "° " + getLatMin(format) + "\' " + getLatSec(format) + "\" " 
						+  getEWLetter() + " " + getLonDeg(format) + "° " + getLonMin(format) + "\' " + getLonSec(format) + "\" ";
		case UTM:	return getUTMZone()  + " N " + getUTMNorthing()+ " E " + getUTMEasting() ;

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
