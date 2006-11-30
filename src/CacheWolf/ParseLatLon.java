package CacheWolf;
import com.stevesoft.ewe_pat.Regex;

import ewe.sys.*;
import ewe.sys.Double;

/**
*	This class parses a string representation of longitude and latitude.
*	The result is a double vlaue representing longitude and another representing
*	latitude (DD.dddd)
*	It should be extended to convert to and from UTM... but that is future
*	functionality.
*/
public class ParseLatLon {
	
	String latlon;	
	String br2 = new String(); 
	String lg2 = new String();
	String br2_buf = new String(); 
	String lg2_buf = new String();
	String br3_buf = new String(); 
	String lg3_buf = new String();
	String br2NS = new String();
	String lg2WE = new String();
	public static final int NS = 1;
	public static final int WE = -1;
		
	private String trenner = new String();
	/**
	*	After a calling the method parse()
	*	this variable holds the double value of latitude
	*/
	public double lat2;
	/**
	*	After a calling the method parse()
	*	this variable holds the double value of longitude
	*/
	public double lon2;
	
	/**
	*	Constructor to parse a lat lon string like:
	*	N 49 33.167 E 011 21.608
	*/
	public ParseLatLon (String ll){
		latlon = ll;
	}
	
	/**
	*	Constructor to parse a lat lon string like:
	*	N 49 33.167 E 011 21.608.
	*	Additionally you may pass the decimal symbol, i.e. "." or ","
	*/
	public ParseLatLon (String ll, String tr){
		trenner = tr;
		latlon = ll;
	}
	
	/**
	* Parse a string that contains lat lon into it's lat and lon doubles. Class
	* variable latlon must have been set befor you call this method.
	*/
	public void parse() throws NumberFormatException {
		latlon=STRreplace.replace(latlon, ",", ".");
	
		/* diese routine steht in map.java - gefaellt mir (pfeffer) irgendwie besser :-) - Aber vielleicht ist sie langsamer
		Regex rex = new Regex("(N|S).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3}).*?(E|W).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3})");
		try {
			rex.search(latlon);
			if(rex.didMatch()){
				double lat = Convert.toDouble(rex.stringMatched(2)) + Convert.toDouble(rex.stringMatched(3))/60 + Convert.toDouble(rex.stringMatched(5))/60000;
				double lon = Convert.toDouble(rex.stringMatched(7)) + Convert.toDouble(rex.stringMatched(8))/60 + Convert.toDouble(rex.stringMatched(10))/60000;
				if(rex.stringMatched(1).equals("S") || rex.stringMatched(1).equals("s")) lat = lat * -1;
				if(rex.stringMatched(6).equals("W") || rex.stringMatched(6).equals("w")) lon = lon * -1;	
			} else throw new NumberFormatException("Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM");
		*/
		try {
		int counter = 0;
		Extractor ex = new Extractor(" " + latlon, " ", " ", 0, true);
		br2 = new String(); 
		lg2 = new String();
		br2_buf = new String(); 
		lg2_buf = new String();
		br3_buf = new String(); 
		lg3_buf = new String();
		br2NS = new String();
		lg2WE = new String();
		while(ex.endOfSearch() != true){
			if(counter == 0) br2NS = ex.findNext();
			if(counter == 1) br2 = ex.findNext();
			if(counter == 2) br2_buf = ex.findNext();
			if(counter == 3) lg2WE = ex.findNext();
			if(counter == 4) lg2 = ex.findNext();
			if(counter == 5) lg2_buf = ex.findNext();
			counter++;
			if(counter >= 10) break;
		}
		
		
		/*
		//Vm.debug(lg2 + " / " +br2);
		lg2_buf = lg2_buf.replace('.',',');
		br2_buf = br2_buf.replace('.',',');
		try{
			lon2 = Convert.parseDouble(lg2) + Convert.parseDouble(lg2_buf)/60;
			lat2 = Convert.parseDouble(br2) + Convert.parseDouble(br2_buf)/60;
		}catch(NumberFormatException nfex){
			lg2_buf = lg2_buf.replace(',','.');
			br2_buf = br2_buf.replace(',','.');
			try{
				lon2 = Convert.parseDouble(lg2) + Convert.parseDouble(lg2_buf)/60;
				lat2 = Convert.parseDouble(br2) + Convert.parseDouble(br2_buf)/60;
			}catch(NumberFormatException nfex2){
				//Vm.debug(nfex2.toString());
			}
		}
		*/
		lg3_buf = lg2_buf.substring(lg2_buf.indexOf(".")+1); // copy from '.'
		lg2_buf = lg2_buf.substring(0,lg2_buf.indexOf(".")); // copy until '.' // TODO handle IndexOutOfBoundsException
		if (lg2.indexOf("°") > 0){
			lg2 = lg2.substring(0,lg2.length()-1); // remove °
		}
		
		br3_buf = br2_buf.substring(br2_buf.indexOf(".")+1); // copy from '.'
		br2_buf = br2_buf.substring(0,br2_buf.indexOf(".")); // copy until '.'
		if (br2.indexOf("°")>0){
			br2 = br2.substring(0,br2.length()-1);// remove °
		}
		
		lat2 = Convert.toDouble(br2) + Convert.toDouble(br2_buf)/60 + Convert.toDouble(br3_buf)/60000;
		if(br2NS.trim().equals("S")) lat2 *= -1 ;
		lon2 = Convert.toDouble(lg2) + Convert.toDouble(lg2_buf)/60 + Convert.toDouble(lg3_buf)/60000;
		if(lg2WE.trim().equals("W")) lon2 *= -1;
		} catch (IndexOutOfBoundsException e) { throw new NumberFormatException("Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"); }

	}
	
	/**
	* Returns a string representation of lat. Method parse() must be called
	* before!
	*/
	public String getLatDeg(){
		Double lat = new Double();
		lat.set(lat2);
		if(trenner.equals("."))	return lat.toString(0,5,Double.FREE_DECIMAL|Double.AT_LEAST_ONE_DECIMAL).replace(',','.');
		else return lat.toString(0,5,Double.FREE_DECIMAL|Double.AT_LEAST_ONE_DECIMAL);
	}
	
	/**
	* Returns a string representation of lon. Method parse() must be called
	* before!
	*/ 
	public String getLonDeg(){
		Double lon = new Double();
		lon.set(lon2);
		if(trenner.equals(".")) return lon.toString(0,5,Double.FREE_DECIMAL|Double.AT_LEAST_ONE_DECIMAL).replace(',','.');
		else return lon.toString(0,5,Double.FREE_DECIMAL|Double.AT_LEAST_ONE_DECIMAL);
	}
	
	/**
	*	Returns a string representation of deg in DD MM.mmm
	*	Setting latorlon = 1 returns N|S
	*	Setting latorlon = -1 returns E|W
	*	Setting latorlon = 0 returns the sign of deg.
	*/
	public String DegToDM(double deg, int latorlon){
		String retval = new String();
		int D = 0;
		double DM = 0;
		int sign = 0;
		
		if(deg < 0) sign = -1;
		else sign = 1;
		if(deg < 0) deg = deg * -1;
		D = (int)java.lang.Math.abs(deg);
		DM = (deg - D) * 60;
		
		if(D<10) retval = "0" + D + " " + DM;
		else retval = D + " " + DM;
		
		if(latorlon == 1 && sign > 0) retval = "N " + retval;
		if(latorlon == 1 && sign < 0) retval = "S " + retval;
		if(latorlon == -1 && sign > 0) retval = "W " + retval;
		if(latorlon == -1 && sign < 0) retval = "E " + retval;
		if(latorlon == 0 && sign > 0) retval = retval;
		if(latorlon == 0 && sign < 0) retval = "-" + retval;
		retval = retval.replace('.', trenner.charAt(0));
		retval = retval.replace(',', trenner.charAt(0));
		retval = retval.substring(0,10);
		return retval;
	}
}
