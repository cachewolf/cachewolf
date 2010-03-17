package CacheWolf;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import com.stevesoft.ewe_pat.*;
import CacheWolf.imp.*;
import CacheWolf.navi.TransformCoordinates;

/**
*	Class that actually filters the cache database.<br>
*	The class that uses this filter must set the different public variables.
*   @author BilboWolf (optimiert von salzkammergut)
*/
public class Filter{
	public static final int FILTER_INACTIVE=0;
	public static final int FILTER_ACTIVE=1;
	public static final int FILTER_CACHELIST=2;
	public static final int FILTER_MARKED_ONLY=3;
	
	/** Indicator whether a filter is inverted */
	//public static boolean filterInverted=false;
	/** Indicator whether a filter is active. Used in status bar to indicate filter status */
	//public static int filterActive=FILTER_INACTIVE;
	
	private static final int SMALLER = -1;
	private static final int EQUAL = 0;
	private static final int GREATER = 1;

	private static final int TRADITIONAL = 1;
	private static final int MULTI = 2;
	private static final int VIRTUAL = 4;
	private static final int LETTER = 8;
	private static final int EVENT = 16;
	private static final int WEBCAM = 32;
	private static final int MYSTERY = 64;
	private static final int LOCLESS = 128;
	private static final int CUSTOM = 256;
	private static final int MEGA = 512;
	private static final int EARTH = 1024;
	private static final int PARKING = 2048;
	private static final int STAGE = 4096;
	private static final int QUESTION = 8192;
	private static final int FINAL = 16384;
	private static final int TRAILHEAD = 32768;
	private static final int REFERENCE = 65536;
	private static final int CITO = 131072;
	private static final int WHERIGO = 262144;
	private static final int TYPE_ALL=TRADITIONAL|MULTI|VIRTUAL|LETTER|EVENT|WEBCAM|MYSTERY|LOCLESS|CUSTOM
	                                  |MEGA|EARTH|PARKING|STAGE|QUESTION|FINAL|TRAILHEAD|REFERENCE|CITO|WHERIGO;
	private static final int TYPE_MAIN=TRADITIONAL|MULTI|VIRTUAL|LETTER|EVENT|WEBCAM|MYSTERY|LOCLESS|CUSTOM|MEGA|EARTH|CITO|WHERIGO;

	private static final int N = 1;
	private static final int NNE = 2;
	private static final int NE = 4;
	private static final int ENE = 8;
	private static final int E = 16;
	private static final int ESE = 32;
	private static final int SE = 64;
	private static final int SSE = 128;
	private static final int SSW = 256;
	private static final int SW = 512;
	private static final int WSW = 1024;
	private static final int W = 2048;
	private static final int WNW = 4096;
	private static final int NW = 8192;
	private static final int NNW = 16384;
	private static final int S = 32768;
	private static final int ROSE_ALL= N|NNE|NE|ENE|E|ESE|SE|SSE|SSW|SW|WSW|W|WNW|NW|NNW|S;

	private int distdirec = 0;
	private int diffdirec = 0;
	private int terrdirec = 0;
	
	String[] byVec;
	
	
	private int roseMatchPattern;
	private boolean hasRoseMatchPattern;
	private int typeMatchPattern;
	private boolean hasTypeMatchPattern;
	private int sizeMatchPattern;
	private boolean hasSizeMatchPattern;
	
	private boolean foundByMe;
	private boolean notFoundByMe;

	private String cacheStatus;
	private boolean useRegexp;
	private boolean filterNoCoord;
	
	private boolean ownedByMe;
	private boolean notOwnedByMe;

	double fscDist;
	double fscTerr;
	double fscDiff;
	
	private boolean archived = false;
	private boolean notArchived = false;
	
	private boolean available=false;
	private boolean notAvailable = false;
	double pi180=java.lang.Math.PI / 180.0;

	private long[] attributesPattern = {0l,0l,0l,0l};
	private int attributesChoice = 0;
	
	/**
	*	Apply a route filter. Each waypoint is on a seperate line.
	*	We use a regex method to allow for different formats of waypoints:
	*	possible is currently: DD MM.mmm
	*/
	public void doFilterRoute(File routeFile, double distance){
		Global.getProfile().selectionChanged = true;
	    CacheDB cacheDB=Global.getProfile().cacheDB;
		//load file into a vector:
		Vector wayPoints = new Vector();
		Regex rex = new Regex("(N|S).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3}).*?(E|W).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3})");
		CWPoint cwp, fromPoint, toPoint;
		CacheHolder ch;
		double lat,lon, calcDistance = 0;
		try{
			if((routeFile.getFullPath()).indexOf(".kml") > 0){
				KMLImporter kml = new KMLImporter(routeFile.getFullPath());
				kml.importFile();
				wayPoints = kml.getPoints();
			} else {
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
			}
			//initialise database
			for(int i = cacheDB.size()-1; i >=0 ; i--){
				ch = cacheDB.get(i);
				ch.in_range = false;
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
					ch = cacheDB.get(i);
					cwp = new CWPoint(ch.getLatLon(), TransformCoordinates.CW);
					calcDistance = DistToSegment(fromPoint, toPoint, cwp);
					calcDistance = (calcDistance*180*60)/java.lang.Math.PI;
					calcDistance = calcDistance * 1.852;
					//Vm.debug("Distcalc: " + calcDistance + "Cache: " +ch.CacheName + " / z is = " + z);
					if(calcDistance <= distance) {
						//Vm.debug("Distcalc: " + calcDistance + "Cache: " +ch.CacheName + " / z is = " + z);
						ch.in_range = true;
					}
					//cacheDB.set(i, ch);
				} // for database
			} // for segments
			for(int i = cacheDB.size()-1; i >=0 ; i--){
				ch = cacheDB.get(i);
				if(ch.is_filtered() == false && ch.in_range == false) ch.setFiltered(true);
			}
		}catch(FileNotFoundException fnex){
			(new MessageBox("Error", "File not found", FormBase.OKB)).execute();
		}catch(IOException ioex){
			(new MessageBox("Error", "Problem reading file!", FormBase.OKB)).execute();
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
	 * Set the filter from the filter data stored in the profile
	 * (the filterscreen also updates the profile)
	 */
	public void setFilter() {
		Profile profile=Global.getProfile();
		archived     = profile.getFilterVar().charAt(0) == '1';
		available    = profile.getFilterVar().charAt(1) == '1';
		foundByMe    = profile.getFilterVar().charAt(2) == '1';
		ownedByMe    = profile.getFilterVar().charAt(3) == '1';
		notArchived  = profile.getFilterVar().charAt(4) == '1';
		notAvailable = profile.getFilterVar().charAt(5) == '1';
		notFoundByMe = profile.getFilterVar().charAt(6) == '1';
		notOwnedByMe = profile.getFilterVar().charAt(7) == '1';
		typeMatchPattern=0;
		cacheStatus  = profile.getFilterStatus();
		useRegexp    = profile.getFilterUseRegexp();
		filterNoCoord = profile.getFilterNoCoord();
		
		String filterType=profile.getFilterType();
		if (filterType.charAt(0) == '1') typeMatchPattern|=TRADITIONAL;
		if (filterType.charAt(1) == '1') typeMatchPattern|=MULTI;
		if (filterType.charAt(2) == '1') typeMatchPattern|=VIRTUAL;
		if (filterType.charAt(3) == '1') typeMatchPattern|=LETTER;
		if (filterType.charAt(4) == '1') typeMatchPattern|=EVENT;
		if (filterType.charAt(5) == '1') typeMatchPattern|=WEBCAM;
		if (filterType.charAt(6) == '1') typeMatchPattern|=MYSTERY;
		if (filterType.charAt(7) == '1') typeMatchPattern|=EARTH;
		if (filterType.charAt(8) == '1') typeMatchPattern|=LOCLESS;
		if (filterType.charAt(9) == '1') typeMatchPattern|=MEGA;
		if (filterType.charAt(10) == '1') typeMatchPattern|=CUSTOM;
		if (filterType.charAt(11) == '1') typeMatchPattern|=PARKING;
		if (filterType.charAt(12) == '1') typeMatchPattern|=STAGE;
		if (filterType.charAt(13) == '1') typeMatchPattern|=QUESTION;
		if (filterType.charAt(14) == '1') typeMatchPattern|=FINAL;
		if (filterType.charAt(15) == '1') typeMatchPattern|=TRAILHEAD;
		if (filterType.charAt(16) == '1') typeMatchPattern|=REFERENCE;
		if (filterType.charAt(17) == '1') typeMatchPattern|=CITO;
		if (filterType.charAt(18) == '1') typeMatchPattern|=WHERIGO;
		hasTypeMatchPattern= typeMatchPattern!=TYPE_ALL;
		roseMatchPattern=0;
		String filterRose=profile.getFilterRose();
		if (filterRose.charAt(0) == '1') roseMatchPattern|=NW;
		if (filterRose.charAt(1) == '1') roseMatchPattern|=NNW;
		if (filterRose.charAt(2) == '1') roseMatchPattern|=N;
		if (filterRose.charAt(3) == '1') roseMatchPattern|=NNE;
		if (filterRose.charAt(4) == '1') roseMatchPattern|=NE;
		if (filterRose.charAt(5) == '1') roseMatchPattern|=ENE;
		if (filterRose.charAt(6) == '1') roseMatchPattern|=E;
		if (filterRose.charAt(7) == '1') roseMatchPattern|=ESE;
		if (filterRose.charAt(8) == '1') roseMatchPattern|=SE;
		if (filterRose.charAt(9) == '1') roseMatchPattern|=SSE;
		if (filterRose.charAt(10) == '1') roseMatchPattern|=S;
		if (filterRose.charAt(11) == '1') roseMatchPattern|=SSW;
		if (filterRose.charAt(12) == '1') roseMatchPattern|=SW;
		if (filterRose.charAt(13) == '1') roseMatchPattern|=WSW;
		if (filterRose.charAt(14) == '1') roseMatchPattern|=W;
		if (filterRose.charAt(15) == '1') roseMatchPattern|=WNW;
		hasRoseMatchPattern=roseMatchPattern!=ROSE_ALL;
		sizeMatchPattern=0;
		String filterSize=profile.getFilterSize();
		if (filterSize.charAt(0) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_MICRO;
		if (filterSize.charAt(1) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_SMALL;
		if (filterSize.charAt(2) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_NORMAL;
		if (filterSize.charAt(3) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_LARGE;
		if (filterSize.charAt(4) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_VERYLARGE;
		if (filterSize.charAt(5) == '1') sizeMatchPattern|=CacheSize.CW_FILTER_NONPHYSICAL;
		hasSizeMatchPattern=sizeMatchPattern!=CacheSize.CW_FILTER_ALL;
		distdirec = profile.getFilterDist().charAt(0) == 'L' ? SMALLER : GREATER; 
		fscDist = Common.parseDouble(profile.getFilterDist().substring(1));  // Distance
		diffdirec = profile.getFilterDiff().charAt(0) == 'L' ? SMALLER : 
					(profile.getFilterDiff().charAt(0) == '=' ? EQUAL : GREATER );
		fscDiff = Common.parseDouble(profile.getFilterDiff().substring(1));  // Difficulty
		terrdirec = profile.getFilterTerr().charAt(0) == 'L' ? SMALLER : 
				(profile.getFilterTerr().charAt(0) == '=' ? EQUAL : GREATER );
		fscTerr = Common.parseDouble(profile.getFilterTerr().substring(1));  // Terrain
		attributesPattern = profile.getFilterAttr();
		attributesChoice = profile.getFilterAttrChoice();
	}
	
	/**
	*	Apply the filter. Caches that match a criteria are flagged
	*	is_filtered = true. The table model is responsible for displaying or
	*	not displaying a cache that is filtered.
	*/
	public void doFilter(){
		CacheDB cacheDB=Global.getProfile().cacheDB;
		Hashtable examinedCaches;
		if (cacheDB.size()==0) return;
		if (!hasFilter()) { // If the filter was completely reset, we can just clear it
			clearFilter();
			return;
		}
		Global.getProfile().selectionChanged = true;
		CacheHolder ch;
		examinedCaches = new Hashtable(cacheDB.size());
		
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = cacheDB.get(i);
			if (examinedCaches.containsKey(ch)) continue;
			
			boolean filterCache = excludedByFilter(ch);
			if (!filterCache && ch.mainCache!=null && ((typeMatchPattern & TYPE_MAIN) != 0)) {
				if (examinedCaches.containsKey(ch.mainCache)) {
					filterCache = ch.mainCache.is_filtered();
				} else {
					ch.mainCache.setFiltered(excludedByFilter(ch.mainCache));
					filterCache = ch.mainCache.is_filtered();
					examinedCaches.put(ch.mainCache, null);
				}
			}
			ch.setFiltered(filterCache);			
		}
		Global.getProfile().setFilterActive(FILTER_ACTIVE);
		examinedCaches = null;
		//Global.getProfile().hasUnsavedChanges=true;
	}

	public boolean excludedByFilter(CacheHolder ch) {
		//Match once against type pattern and once against rose pattern
		//Default is_filtered = false, means will be displayed!
		//If cache does not match type or rose pattern then is_filtered is set to true
		// and we proceed to next cache (no further tests needed)
		//Then we check the other filter criteria one by one: As soon as one is found that
		// eliminates the cache (i.e. sets is_filtered to true), we can skip the other tests
		// A cache is only displayed (i.e. is_filtered = false) if it meets all 9 filter criteria
	    int cacheTypePattern;
	    int cacheRosePattern;
	    int cacheSizePattern;
	    double dummyd1;
	    boolean cacheFiltered=false;
	    do {
	        ///////////////////////////////
	        // Filter criterium 1: Cache type
	        ///////////////////////////////
	        if (hasTypeMatchPattern) { // Only do the checks if we have a filter
		        cacheTypePattern = 0;
		        // As each cache can only have one type, we can use else if and set the type
		        if (ch.getType() == CacheType.CW_TYPE_CUSTOM)
			        cacheTypePattern = CUSTOM;
		        else if (ch.getType() == CacheType.CW_TYPE_TRADITIONAL)
			        cacheTypePattern = TRADITIONAL;
		        else if (ch.getType() == CacheType.CW_TYPE_MULTI)
			        cacheTypePattern = MULTI;
		        else if (ch.getType() == CacheType.CW_TYPE_VIRTUAL)
			        cacheTypePattern = VIRTUAL;
		        else if (ch.getType() == CacheType.CW_TYPE_LETTERBOX)
			        cacheTypePattern = LETTER;
		        else if (ch.getType() == CacheType.CW_TYPE_EVENT)
			        cacheTypePattern = EVENT;
		        else if (ch.getType() == CacheType.CW_TYPE_UNKNOWN)
			        cacheTypePattern = MYSTERY;
		        else if (ch.getType() == CacheType.CW_TYPE_WEBCAM)
			        cacheTypePattern = WEBCAM;
		        else if (ch.getType() == CacheType.CW_TYPE_LOCATIONLESS)
			        cacheTypePattern = LOCLESS;
		        else if (ch.getType() == CacheType.CW_TYPE_EARTH)
			        cacheTypePattern = EARTH;
		        else if (ch.getType() == CacheType.CW_TYPE_MEGA_EVENT)
			        cacheTypePattern = MEGA;
		        else if (ch.getType() == CacheType.CW_TYPE_PARKING)
			        cacheTypePattern = PARKING;
		        else if (ch.getType() == CacheType.CW_TYPE_STAGE)
			        cacheTypePattern = STAGE;
		        else if (ch.getType() == CacheType.CW_TYPE_QUESTION)
			        cacheTypePattern = QUESTION;
		        else if (ch.getType() == CacheType.CW_TYPE_FINAL)
			        cacheTypePattern = FINAL;
		        else if (ch.getType() == CacheType.CW_TYPE_TRAILHEAD)
			        cacheTypePattern = TRAILHEAD;
		        else if (ch.getType() == CacheType.CW_TYPE_REFERENCE)
			        cacheTypePattern = REFERENCE;
		        else if (ch.getType() == CacheType.CW_TYPE_CITO)
			        cacheTypePattern = CITO;
		        else if (ch.getType() == CacheType.CW_TYPE_WHEREIGO)
			        cacheTypePattern = WHERIGO;
		        if ((cacheTypePattern & typeMatchPattern) == 0) {
			        cacheFiltered = true; break;
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 2: Bearing from centre
	        ///////////////////////////////
	        // The optimal number of comparisons to identify one of 16 objects is 4 (=log2(16))
	        // By using else if we can reduce the number of comparisons from 16 to just over 8
	        // By first checking the first letter, we can reduce the average number further to
	        // just under 5
	        if (hasRoseMatchPattern) {
		        if (ch.getBearing().startsWith("N")) {
			        if (ch.getBearing().equals("NW"))
				        cacheRosePattern = NW;
			        else if (ch.getBearing().equals("NNW"))
				        cacheRosePattern = NNW;
			        else if (ch.getBearing().equals("N"))
				        cacheRosePattern = N;
			        else if (ch.getBearing().equals("NNE"))
				        cacheRosePattern = NNE;
			        else
				        cacheRosePattern = NE;
		        } else if (ch.getBearing().startsWith("E")) {
			        if (ch.getBearing().equals("ENE"))
				        cacheRosePattern = ENE;
			        else if (ch.getBearing().equals("E"))
				        cacheRosePattern = E;
			        else
				        cacheRosePattern = ESE;
		        } else if (ch.getBearing().startsWith("S")) {
			        if (ch.getBearing().equals("SW"))
				        cacheRosePattern = SW;
			        else if (ch.getBearing().equals("SSW"))
				        cacheRosePattern = SSW;
			        else if (ch.getBearing().equals("S"))
				        cacheRosePattern = S;
			        else if (ch.getBearing().equals("SSE"))
				        cacheRosePattern = SSE;
			        else
				        cacheRosePattern = SE;
		        } else {
			        if (ch.getBearing().equals("WNW"))
				        cacheRosePattern = WNW;
			        else if (ch.getBearing().equals("W"))
				        cacheRosePattern = W;
			        else if (ch.getBearing().equals("WSW"))
				        cacheRosePattern = WSW;
			        else
				        cacheRosePattern = 0;
		        }
		        if ((cacheRosePattern != 0) && ((cacheRosePattern & roseMatchPattern) == 0)) {
			        cacheFiltered = true; break;
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 3: Distance
	        ///////////////////////////////
	        if (fscDist > 0.0) {
		        dummyd1 = ch.kilom;
		        if (distdirec == SMALLER && dummyd1 > fscDist) {
			        cacheFiltered = true; break;
		        }
		        if (distdirec == GREATER && dummyd1 < fscDist) {
			        cacheFiltered = true; break;
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 4: Difficulty
	        ///////////////////////////////
	        if (fscDiff > 0.0) {
		        dummyd1 = ch.getHard() / 10D;
		        if (diffdirec == SMALLER && dummyd1 > fscDiff) {
			        cacheFiltered = true; break;
		        }
		        if (diffdirec == EQUAL && dummyd1 != fscDiff) {
			        cacheFiltered = true; break;
		        }
		        if (diffdirec == GREATER && dummyd1 < fscDiff) {
			        cacheFiltered = true; break;
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 5: Terrain
	        ///////////////////////////////
	        if (fscTerr > 0.0) {
	        	dummyd1 = ch.getTerrain() / 10D;
		        if (terrdirec == SMALLER && dummyd1 > fscTerr) {
			        cacheFiltered = true; break;
		        }
		        if (terrdirec == EQUAL && dummyd1 != fscTerr) {
			        cacheFiltered = true; break;
		        }
		        if (terrdirec == GREATER && dummyd1 < fscTerr) {
			        cacheFiltered = true; break;
		        }
	        }

	        ///////////////////////////////
	        // Filter criterium 6: Found by me
	        ///////////////////////////////
	        if ((ch.is_found() && !foundByMe) || (!ch.is_found() && !notFoundByMe)) {
		        cacheFiltered = true; break;
	        }
	        ///////////////////////////////
	        // Filter criterium 7: Owned by me
	        ///////////////////////////////
	        if ((ch.is_owned() && !ownedByMe) || (!ch.is_owned() && !notOwnedByMe)) {
		        cacheFiltered = true; break;
	        }
	        ///////////////////////////////
	        // Filter criterium 8: Archived
	        ///////////////////////////////
	        if ((ch.is_archived() && !archived) || (!ch.is_archived() && !notArchived)) {
		        cacheFiltered = true; break;
	        }
	        ///////////////////////////////
	        // Filter criterium 9: Unavailable
	        ///////////////////////////////
	        if ((ch.is_available() && !available) || (!ch.is_available() && !notAvailable)) {
		        cacheFiltered = true; break;
	        }
	        ///////////////////////////////
	        // Filter criterium 10: Size
	        ///////////////////////////////
	        if (hasSizeMatchPattern) {
		        cacheSizePattern = CacheSize.getFilterPattern(ch.getCacheSize());
		        if ((cacheSizePattern & sizeMatchPattern) == 0) {
			        cacheFiltered = true; break;
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 11: Attributes
	        ///////////////////////////////
	        if ((attributesPattern[0] != 0 || attributesPattern[1] != 0 || attributesPattern[2] != 0 || attributesPattern[3] != 0) 
	        		&& ch.mainCache == null) {
	        	long[] chAtts=ch.getAttributesBits();
		        if (attributesChoice == 0) {
			        // AND-condition:
			        if ((chAtts[0] & attributesPattern[0]) != attributesPattern[0] ||
				        	(chAtts[1] & attributesPattern[1]) != attributesPattern[1] ||
				        	(chAtts[2] & attributesPattern[2]) != attributesPattern[2] ||
				        	(chAtts[3] & attributesPattern[3]) != attributesPattern[3] )
				        {
				        cacheFiltered = true;
				        break;
			        }
		        } else if (attributesChoice == 1) {
			        // OR-condition:
			        if ((chAtts[0] & attributesPattern[0]) == 0 ||
				        	(chAtts[1] & attributesPattern[1]) == 0 ||
				        	(chAtts[2] & attributesPattern[2]) == 0 ||
				        	(chAtts[3] & attributesPattern[3]) == 0 )
				        {
				        cacheFiltered = true;
				        break;
			        }
		        } else {
			        // NOT-condition:
			        if ((chAtts[0] & attributesPattern[0]) != 0 ||
				        	(chAtts[1] & attributesPattern[1]) != 0 ||
				        	(chAtts[2] & attributesPattern[2]) != 0 ||
				        	(chAtts[3] & attributesPattern[3]) != 0 )
				        {
				        cacheFiltered = true;
				        break;
			        }
		        }
	        }
	        ///////////////////////////////
	        // Filter criterium 12: Status
	        ///////////////////////////////
	        if (!cacheStatus.equals("")) {
	        	if (!useRegexp) {
	        		if (ch.getCacheStatus().toLowerCase().indexOf(cacheStatus.toLowerCase())<0) {
	        			cacheFiltered = true;
	        			break;
	        		}
	        	} else {
	        		Regex rex=new Regex(cacheStatus.toLowerCase());
	        		rex.search(ch.getCacheStatus().toLowerCase());
	        		if (rex.stringMatched()==null) {
	        			cacheFiltered = true;
	        			break;
	        		}
	        	}
	        }
	        ///////////////////////////////
	        // Filter criterium 11: NoCoord
	        ///////////////////////////////
	        if (!filterNoCoord && !ch.pos.isValid()) {
	          cacheFiltered = true;
	        	break;
        }

	        break;
        } while (true);
		return cacheFiltered;
    }
	
	/**
	*	Switches flag to invert filter property.
	*/
	public void invertFilter(){
		Global.getProfile().setFilterInverted(!Global.getProfile().isFilterInverted());
	}
	
	/**
	*	Clear the is_filtered flag from the cache database.
	*/
	public void clearFilter(){
		Global.getProfile().selectionChanged = true;
		CacheDB cacheDB=Global.getProfile().cacheDB;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			CacheHolder ch = cacheDB.get(i);
			ch.setFiltered(false);
		}
		Global.getProfile().setFilterActive(FILTER_INACTIVE);
	}

		public boolean hasFilter() {
		Profile prof=Global.getProfile();
		long[] attribs=prof.getFilterAttr();
		return !(prof.getFilterType().equals(FilterData.FILTERTYPE) &&
		    prof.getFilterRose().equals(FilterData.FILTERROSE) &&
		    prof.getFilterVar().equals(FilterData.FILTERVAR) &&
		    prof.getFilterSize().equals(FilterData.FILTERSIZE) &&
		    prof.getFilterDist().equals("L") &&
		    prof.getFilterDiff().equals("L") &&
		    prof.getFilterTerr().equals("L") &&
		    attribs[0] == 0l &&
		    attribs[1] == 0l &&
		    attribs[2] == 0l &&
		    attribs[3] == 0l &&
		    prof.getFilterStatus().equals("") &&
        prof.getFilterNoCoord());
	}

}


