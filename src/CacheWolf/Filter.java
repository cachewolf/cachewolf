package CacheWolf;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import com.stevesoft.ewe_pat.*;

/**
*	Class that actually filters the cache database.<br>
*	The class that uses this filter must set the different public variables.
*   @author BilboWolf (optimiert von salzkammergut)
*/
public class Filter{
	
	public static final int SMALLER = -1;
	public static final int EQUAL = -1;
	public static final int GREATER = 1;
	public static final int FOUND = 2;
	public static final int NOTFOUND = 3;

	public static final int TRADITIONAL = 1;
	public static final int MULTI = 2;
	public static final int VIRTUAL = 4;
	public static final int LETTER = 8;
	public static final int EVENT = 16;
	public static final int WEBCAM = 32;
	public static final int MYSTERY = 64;
	public static final int LOCLESS = 128;
	public static final int ADDIWPT = 256;
	public static final int MEGA = 512;
	public static final int EARTH = 1024;
	
	// End of type declares
	public static final int N = 1;
	public static final int NNE = 2;
	public static final int NE = 4;
	public static final int ENE = 8;
	public static final int E = 16;
	public static final int ESE = 32;
	public static final int SE = 64;
	public static final int SSE = 128;
	public static final int SSW = 256;
	public static final int SW = 512;
	public static final int WSW = 1024;
	public static final int W = 2048;
	public static final int WNW = 4096;
	public static final int NW = 8192;
	public static final int NNW = 16384;
	public static final int S = 32768;
	//end of direction declares
	
	
	
	public String dist = new String();
	public File routeFile;
	public int distdirec = 0;
	public String diff = new String();
	public int diffdirec = 0;
	public String terr = new String();
	public int terrdirec = 0;
	
	public String days = new String();
	public int daysdirec = 0;
	public String by = new String();
	String[] byVec;
	
	public String type = new String();
	public String bearing = new String();
	
	public int roseMatchPattern = 0;
	public int typeMatchPattern = 0;
	
	public boolean foundByMe;
	public boolean notFoundByMe;
	
	public boolean ownedByMe;
	public boolean notOwnedByMe;
	
	//private boolean is_changed;
	public int filterKriteria = 0;
	
	public boolean archived = false;
	public boolean notArchived = false;
	
	public boolean available=false;
	public boolean notAvailable = false;
	double pi180=java.lang.Math.PI / 180.0;
	
	/**
	*	Apply a route filter. Each waypoint is on a seperate line.
	*	We use a regex method to allow for different formats of waypoints:
	*	possible is currently: DD MM.mmm
	*/
	public void doFilterRoute(Vector cacheDB, String dir, double distance){
		//load file into a vector:
		Vector wayPoints = new Vector();
		Regex rex = new Regex("(N|S).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3}).*?(E|W).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3})");
		CWPoint cwp, fromPoint, toPoint;
		CacheHolder ch;
		double lat,lon, calcDistance = 0;
		try{
			FileReader in = new FileReader(routeFile);
			String line; 
			while((line = in.readLine()) != null){
				rex.search(line);
				/*
				Vm.debug(line);
				Vm.debug(rex.stringMatched(1));
				Vm.debug(rex.stringMatched(2));
				Vm.debug(rex.stringMatched(3));
				Vm.debug(rex.stringMatched(5));
				
				Vm.debug(rex.stringMatched(6));
				Vm.debug(rex.stringMatched(7));
				Vm.debug(rex.stringMatched(8));
				Vm.debug(rex.stringMatched(10));
				Vm.debug(" ");
				*/
				// parse the route file
				if(rex.didMatch()){
					lat = Convert.toDouble(rex.stringMatched(2)) + Convert.toDouble(rex.stringMatched(3))/60 + Convert.toDouble(rex.stringMatched(5))/60000;
					lon = Convert.toDouble(rex.stringMatched(7)) + Convert.toDouble(rex.stringMatched(8))/60 + Convert.toDouble(rex.stringMatched(10))/60000;
				
					if(rex.stringMatched(1).equals("S") || rex.stringMatched(1).equals("s")) lat = -lat;
					if(rex.stringMatched(6).equals("W") || rex.stringMatched(6).equals("w")) lon = -lon;	
				
					cwp = new CWPoint(lat, lon);
					
					wayPoints.add(cwp);
				}
			}
			//initialize database
			for(int i = cacheDB.size()-1; i >=0 ; i--){
				ch = (CacheHolder)cacheDB.get(i);
				ch.is_filtered = true;
				//cacheDB.set(i, ch);
			}
			// for each segment of the route...
			for(int z=0;z<wayPoints.size()-1;z++){
				fromPoint = new CWPoint();
				toPoint = new CWPoint();
				fromPoint = (CWPoint)wayPoints.get(z);
				toPoint = (CWPoint)wayPoints.get(z+1);
				//... go through the current cache database
				for(int i = cacheDB.size()-1; i >=0 ; i--){
					ch = (CacheHolder)cacheDB.get(i);
					cwp = new CWPoint(ch.LatLon, CWPoint.CW);
					calcDistance = DistToSegment(fromPoint, toPoint, cwp);
					calcDistance = (calcDistance*180*60)/java.lang.Math.PI;
					calcDistance = calcDistance * 1.852;
					//Vm.debug("Distcalc: " + calcDistance + "Cache: " +ch.CacheName + " / z is = " + z);
					if(calcDistance <= distance) {
						Vm.debug("Distcalc: " + calcDistance + "Cache: " +ch.CacheName + " / z is = " + z);
						ch.is_filtered = false;
					}
					//cacheDB.set(i, ch);
				} // for database
			} // for segments
			
		}catch(FileNotFoundException fnex){
			(new MessageBox("Error", "File not found", MessageBox.OKB)).execute();
		}catch(IOException ioex){
			(new MessageBox("Error", "Problem reading file!", MessageBox.OKB)).execute();
		}
	}
	
	/**
	*	Method to calculate the distance of a point to a segment
	*/
	private double DistToSegment(CWPoint fromPoint, CWPoint toPoint, CWPoint cwp){
		
		/*
		double XTD = 0;
		double dist = 0;
		
		double crs_AB = fromPoint.getBearing(toPoint);
		crs_AB = crs_AB * java.lang.Math.PI / 180;
		double crs_AD = fromPoint.getBearing(cwp);
		crs_AD = crs_AD * java.lang.Math.PI / 180;
		double dist_AD = fromPoint.getDistance(cwp);
		Vm.debug("Distance: "+dist_AD);
		dist_AD = dist_AD / 1.852;
		dist_AD = (java.lang.Math.PI/(180*60))*dist_AD;
		XTD =java.lang.Math.asin(java.lang.Math.sin(dist_AD)*java.lang.Math.sin(crs_AD-crs_AB));
		return java.lang.Math.abs(XTD);
		*/
		double dist = 0;
		double px = cwp.lonDec * pi180;
		double py = cwp.latDec * pi180;
		double X1 = fromPoint.lonDec * pi180;
		double Y1 = fromPoint.latDec * pi180;
		double X2 = toPoint.lonDec * pi180;
		double Y2 = toPoint.latDec * pi180;
		double dx = X2 - X1;
		double dy = Y2 - Y1;
		if(dx == 0 && dy == 0){
			// have a point and not a segment!
			dx = px - X1;
			dy = py - Y1;
			return java.lang.Math.sqrt(dx*dx + dy*dy);
		}
		dist = Matrix.cross(X1,Y1,X2,Y2,px,py) / Matrix.dist(X1,Y1,X2,Y2);
		double dot1 = Matrix.dot(X1,Y1,X2,Y2,px,py);
		if(dot1 > 0) return Matrix.dist(X2,Y2,px,py);
		double dot2 = Matrix.dot(X2,Y2,X1,Y1,px,py);
		if(dot2 > 0) return Matrix.dist(X1,Y1,px,py);
		dist = java.lang.Math.abs(dist);
		return dist;
		
	}
	
	/**
	 * Filters the cachelist for those caches marked is_black == true.
	 * Only caches matching are then displayed in the table
	 * @param cacheDB
	 */
	public void showBlacklist(Vector cacheDB){
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered=!ch.is_black;
		}
	}
	
	/**
	*	Apply the filter. Caches that match a criteria are flagged
	*	is_filtered = true. The table model is responsible for displaying or
	*	not displaying a cache that is filtered.
	*/
	public void doFilter(Vector cacheDB, String dir){
		CacheHolder ch;
		int cacheTypePattern;
		int cacheRosePattern;
		// Values from filterscreen are parsed outside the main filter loop (fsc=FilterSCreen)
		double fscDist=Common.parseDouble(dist);  // Distance
		double fscTerr=Common.parseDouble(terr);  // Terrain
		double fscDiff=Common.parseDouble(diff);  // Difficulty
		double dummyd1;
		//Loop db and match once against type pattern and once against rose pattern
		//Default is_filtered = false, means will be displayed!
		//If cache does not match type or rose pattern then is_filtered is set to true
		// and we proceed to next cache (no further tests needed)
		//Then we check the other filter criteria one by one: As soon as one is found that
		// eliminates the cache (i.e. sets is_filtered to true), we can skip the other tests
		// A cache is only displayed (i.e. is_filtered = false) if it meets all 9 filter criteria
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered = false;
			///////////////////////////////
			// Filter criterium 1: Cache type
			///////////////////////////////
			cacheTypePattern = 0;
			// As each cache can only have one type, we can use else if and set the type
			if(ch.type.equals("2")) cacheTypePattern = TRADITIONAL;
			else if(ch.type.equals("3")) cacheTypePattern = MULTI;
			else if(ch.type.equals("4")) cacheTypePattern = VIRTUAL;
			else if(ch.type.equals("5")) cacheTypePattern = LETTER;
			else if(ch.type.equals("6")|| ch.type.equals("453")) cacheTypePattern = EVENT;
			else if(ch.type.equals("8")) cacheTypePattern = MYSTERY;
			else if(ch.type.equals("11")) cacheTypePattern = WEBCAM;
			else if(ch.type.equals("12")) cacheTypePattern = LOCLESS;
			else if(ch.type.equals("137"))cacheTypePattern = EARTH;
			else if(ch.type.equals("453"))cacheTypePattern = MEGA;
			if (CacheType.isAddiWpt(ch.type)) cacheTypePattern |= ADDIWPT;
			if ((cacheTypePattern & typeMatchPattern) == 0) { ch.is_filtered=true; continue; }
			
			///////////////////////////////
			// Filter criterium 2: Bearing from centre
			///////////////////////////////
			// The optimal number of comparisons to identify one of 16 objects is 4 (=log2(16))
			// By using else if we can reduce the number of comparisons from 16 to just over 8
			// By first checking the first letter, we can reduce the average number further to
			// just under 5
			if (ch.bearing.startsWith("N")) {
				if(ch.bearing.equals("NW")) cacheRosePattern = NW;
				else if(ch.bearing.equals("NNW")) cacheRosePattern = NNW;
				else if(ch.bearing.equals("N")) cacheRosePattern = N;
				else if(ch.bearing.equals("NNE")) cacheRosePattern = NNE;
				else cacheRosePattern = NE;
			} else if (ch.bearing.startsWith("E")) {
				if(ch.bearing.equals("ENE")) cacheRosePattern = ENE;
				else if(ch.bearing.equals("E")) cacheRosePattern = E;
				else cacheRosePattern = ESE;
			} else if (ch.bearing.startsWith("S")) {
				if(ch.bearing.equals("SW")) cacheRosePattern = SW;
				else if(ch.bearing.equals("SSW")) cacheRosePattern = SSW;
				else if(ch.bearing.equals("S")) cacheRosePattern = S;
				else if(ch.bearing.equals("SSE")) cacheRosePattern = SSE;
				else cacheRosePattern = SE;
			} else {
				if(ch.bearing.equals("WNW")) cacheRosePattern = WNW;
				else if(ch.bearing.equals("W")) cacheRosePattern = W;
				else cacheRosePattern = WSW;
			}
			if ((cacheRosePattern & roseMatchPattern) == 0) { ch.is_filtered=true; continue; }
			
			///////////////////////////////
			// Filter criterium 3: Distance
			///////////////////////////////
			if(fscDist>0.0){
				dummyd1 = Common.parseDouble(ch.distance.substring(0,ch.distance.length()-3)); 
				if(distdirec == SMALLER && dummyd1 > fscDist)  { ch.is_filtered=true; continue; }
				if(distdirec == GREATER && dummyd1 < fscDist)  { ch.is_filtered=true; continue; }
			}
			///////////////////////////////
			// Filter criterium 4: Difficulty
			///////////////////////////////
			if(fscDiff>0.0){
				dummyd1 = Common.parseDouble(ch.hard);
				if(diffdirec == SMALLER && dummyd1 > fscDiff) { ch.is_filtered=true; continue; }
				if(diffdirec == EQUAL && dummyd1 != fscDiff) { ch.is_filtered=true; continue; }
				if(diffdirec == GREATER && dummyd1 < fscDiff) { ch.is_filtered=true; continue; }
			}
			///////////////////////////////
			// Filter criterium 5: Terrain
			///////////////////////////////
			if(fscTerr>0.0){
				dummyd1 = Common.parseDouble(ch.terrain);
				if(terrdirec == SMALLER &&  dummyd1 > fscTerr) { ch.is_filtered=true; continue; }
				if(terrdirec == EQUAL && dummyd1 != fscTerr) { ch.is_filtered=true; continue; }
				if(terrdirec == GREATER &&  dummyd1 < fscTerr) { ch.is_filtered=true; continue; }
			}
			///////////////////////////////
			// Filter criterium 6: Found by me
			///////////////////////////////
			if((ch.is_found && !foundByMe) ||
			   (!ch.is_found && !notFoundByMe)){ ch.is_filtered=true; continue; }

			///////////////////////////////
			// Filter criterium 7: Owned by me
			///////////////////////////////
			if((ch.is_owned && !ownedByMe) ||
			   (!ch.is_owned && !notOwnedByMe)) { ch.is_filtered=true; continue; }
			
			///////////////////////////////
			// Filter criterium 8: Archived
			///////////////////////////////
			if((ch.is_archived && !archived) ||
			   (!ch.is_archived && !notArchived)){ ch.is_filtered=true; continue; }

			///////////////////////////////
			// Filter criterium 9: Unavailable
			///////////////////////////////
			if((ch.is_available && !available) ||
			   (!ch.is_available && !notAvailable)) { ch.is_filtered=true; continue; }
			
		} // for
	}
	
	/**
	*	Invert is_filtered flag on all caches
	*/
	public void invertFilter(Vector cacheDB){
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered=!ch.is_filtered; // skg: More efficient
		}
	}
	
	/**
	*	Clear the is_filtered flag from the cache database.
	*/
	public void clearFilter(Vector cacheDB){
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered=ch.is_black; // Always filter blacklisted caches
		}
		Profile prof=Global.getProfile();
		prof.filterType = new String(Profile.FILTERTYPE);
		prof.filterRose = new String(Profile.FILTERROSE);
		prof.filterVar = new String(Profile.FILTERVAR);
		prof.filterDist="";
		prof.filterDiff="";
		prof.filterTerr="";
		
	}
}

