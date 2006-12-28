package CacheWolf;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import com.stevesoft.ewe_pat.*;

/**
*	Class that actually filters the cache database.<br>
*	The class that uses this filter must set the different public variables.
*/
public class Filter{
	public static final int SMALLER = -1;
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
	public boolean ownedByMe;
	//private boolean is_changed;
	public int filterKriteria = 0;
	
	boolean archived = false;
	boolean notAvailable = false;
	
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
			String line = new String();
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
				
					if(rex.stringMatched(1).equals("S") || rex.stringMatched(1).equals("s")) lat = lat * -1;
					if(rex.stringMatched(6).equals("W") || rex.stringMatched(6).equals("w")) lon = lon * -1;	
				
					cwp = new CWPoint(lat, lon);
					
					wayPoints.add(cwp);
				}
			}
			//initialize database
			for(int i = 0;i<cacheDB.size(); i++){
				ch = (CacheHolder)cacheDB.get(i);
				ch.is_filtered = true;
				cacheDB.set(i, ch);
			}
			// for each segment of the route...
			for(int z=0;z<wayPoints.size()-1;z++){
				fromPoint = new CWPoint();
				toPoint = new CWPoint();
				fromPoint = (CWPoint)wayPoints.get(z);
				toPoint = (CWPoint)wayPoints.get(z+1);
				//... go through the current cache database
				for(int i = 0;i<cacheDB.size(); i++){
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
					cacheDB.set(i, ch);
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
		double px = cwp.lonDec * java.lang.Math.PI / 180;
		double py = cwp.latDec * java.lang.Math.PI / 180;
		double X1 = fromPoint.lonDec * java.lang.Math.PI / 180;
		double Y1 = fromPoint.latDec * java.lang.Math.PI / 180;
		double X2 = toPoint.lonDec * java.lang.Math.PI / 180;
		double Y2 = toPoint.latDec * java.lang.Math.PI / 180;
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
	*	Apply the filter. Caches that match a criteria are flagged
	*	is_filtered = true. The table model is responsible for displaying or
	*	not displaying a cache that is filtered.
	*/
	public void doFilter(Vector cacheDB, String dir){
		CacheHolder ch = new CacheHolder();
		int cacheTypePattern = 0;
		int cacheRosePattern = 0;
		double dummyd1, dummyd2;
		String dummy = new String();
		//Loop db and math once against type pattern and once against rose pattern
		//Default is_filtered = true, means will not be displayed!
		//If cache matches type and rose pattern then is_filtered is set to false
		//Still in the loop check aginst diff, terr, dist, found by, found last
		for(int i = 0; i < cacheDB.size(); i++){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered = true;
			cacheTypePattern = 0;
			if(ch.type.equals("2")) cacheTypePattern |= TRADITIONAL;
			if(ch.type.equals("3")) cacheTypePattern |= MULTI;
			if(ch.type.equals("4")) cacheTypePattern |= VIRTUAL;
			if(ch.type.equals("5")) cacheTypePattern |= LETTER;
			if(ch.type.equals("6")|| ch.type.equals("453")) cacheTypePattern |= EVENT;
			if(ch.type.equals("8")) cacheTypePattern |= MYSTERY;
			if(ch.type.equals("11")) cacheTypePattern |= WEBCAM;
			if(ch.type.equals("12")) cacheTypePattern |= LOCLESS;
			if (CacheType.isAddiWpt(ch.type)) cacheTypePattern |= ADDIWPT;
			
			cacheRosePattern = 0;
			if(ch.bearing.equals("NW")) cacheRosePattern |= NW;
			if(ch.bearing.equals("NNW")) cacheRosePattern |= NNW;
			if(ch.bearing.equals("N")) cacheRosePattern |= N;
			if(ch.bearing.equals("NNE")) cacheRosePattern |= NNE;
			if(ch.bearing.equals("NE")) cacheRosePattern |= NE;
			if(ch.bearing.equals("WNW")) cacheRosePattern |= WNW;
			if(ch.bearing.equals("ENE")) cacheRosePattern |= ENE;
			if(ch.bearing.equals("W")) cacheRosePattern |= W;
			if(ch.bearing.equals("E")) cacheRosePattern |= E;
			if(ch.bearing.equals("WSW")) cacheRosePattern |= WSW;
			if(ch.bearing.equals("ESE")) cacheRosePattern |= ESE;
			if(ch.bearing.equals("SW")) cacheRosePattern |= SW;
			if(ch.bearing.equals("SSW")) cacheRosePattern |= SSW;
			if(ch.bearing.equals("S")) cacheRosePattern |= S;
			if(ch.bearing.equals("SSE")) cacheRosePattern |= SSE;
			if(ch.bearing.equals("SE")) cacheRosePattern |= SE;
/*			
			Vm.debug(ch.wayPoint);
			Vm.debug("Type Pattern: " + cacheTypePattern);
			Vm.debug("Type Match Pattern: " + typeMatchPattern);
			Vm.debug("AND: " + (cacheTypePattern & typeMatchPattern));
			*/ 
			if((cacheTypePattern & typeMatchPattern) == 0 &&(cacheRosePattern & roseMatchPattern) == 0) ch.is_filtered = false;
			
			/*
			if(dist.length()>0 && ch.is_filtered == false){
				dummy = ch.distance.substring(0,ch.distance.length()-3);
				dummyd1 = Common.parseDouble(dummy); 
				dummyd2 = Common.parseDouble(dist); 
				if(distdirec == SMALLER && dummyd2 >= dummyd1) ch.is_filtered = true;
				if(distdirec == GREATER && dummyd2 <= dummyd1) ch.is_filtered = true;
			}
			
			if(diff.length()>0 && ch.is_filtered == false){
				dummyd1 = Common.parseDouble(ch.hard);
				dummyd2 = Common.parseDouble(diff);
				if(diffdirec == SMALLER && dummyd2 >= dummyd1) ch.is_filtered = true;
				if(diffdirec == GREATER && dummyd2 <= dummyd1) ch.is_filtered = true;
			}
			
			if(terr.length()>0 && ch.is_filtered == false){
				dummyd1 = Common.parseDouble(ch.terrain);
				dummyd2 = Common.parseDouble(terr);
				if(terrdirec == SMALLER && dummyd2 >= dummyd1) ch.is_filtered = true;
				if(terrdirec == GREATER && dummyd2 <= dummyd1) ch.is_filtered = true;
			}
			
			if(ch.is_found == false && foundByMe == true && ch.is_filtered == false) ch.is_filtered = true;
			if(ch.is_owned == false && ownedByMe == true && ch.is_filtered == false) ch.is_filtered = true;
			
			if(ch.is_archived == false && archived == true && ch.is_filtered == false) ch.is_filtered = true;
			if(ch.is_available == false && notAvailable == true && ch.is_filtered == false) ch.is_filtered = true;
			*/
			cacheDB.set(i,ch);
		} // for
	}
	
	
	public void doFilter_old(Vector cacheDB, String dir){
		byVec = mString.split(by, ',');
		CacheHolder ch;
		//CacheReaderWriter crw = new CacheReaderWriter();
		String dummy = new String();
		FileReader in;
		String text = new String();
		String logStr = new String();
		String foundStr = new String();
		Extractor ex, ex2;
		double dummyd1, dummyd2;
		
		for(int i = 0; i < cacheDB.size(); i++){
			//is_changed = false;
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered = false;
			if(bearing.indexOf("|"+ch.bearing+"|")>=0) ch.is_filtered = true;
			if(type.indexOf("|"+ch.type+"|") >= 0) ch.is_filtered = true;
			
			//Vm.debug("Dist: " + Convert.toString(distdirec));
			if(dist.length()>0){
				dummy = ch.distance.substring(0,ch.distance.length()-3);
				dummyd1 = Common.parseDouble(dummy); 
				dummyd2 = Common.parseDouble(dist); 
				if(distdirec == SMALLER && dummyd2 <= dummyd1) ch.is_filtered = true;
				if(distdirec == GREATER && dummyd2 >= dummyd1) ch.is_filtered = true;
			}
			
			if(diff.length()>0){
				dummyd1 = Common.parseDouble(ch.hard);
				dummyd2 = Common.parseDouble(diff);
				if(diffdirec == SMALLER && dummyd2 <= dummyd1) ch.is_filtered = true;
				if(diffdirec == GREATER && dummyd2 >= dummyd1) ch.is_filtered = true;
			}
			
			if(terr.length()>0){
				dummyd1 = Common.parseDouble(ch.terrain);
				dummyd2 = Common.parseDouble(terr);
				if(terrdirec == SMALLER && dummyd2 <= dummyd1) ch.is_filtered = true;
				if(terrdirec == GREATER && dummyd2 >= dummyd1) ch.is_filtered = true;
			}
			
			
			if(ch.is_found == true && foundByMe == true) ch.is_filtered = true;
			if(ch.is_owned == true && ownedByMe == true) ch.is_filtered = true;
			
			if(ch.is_archived == true && archived == true) ch.is_filtered = true;
			if(ch.is_available == false && notAvailable == true) ch.is_filtered = true;
			
			// now the filters that require we scan the detail cache pages
			// found by
			if(by.length()>0 || days.length()>0){
				try{
				in = new FileReader(dir+ch.wayPoint+".xml");
				text = new String();
				text = in.readAll();
				in.close();
				}catch(Exception e){
					//Vm.debug("Error reading xml file");
				}
			}
			if(by.length()>0){
				ex = new Extractor(text, "<LOGS>", "</LOGS>", 0, true);
				logStr = ex.findNext();
				ex2 = new Extractor(logStr, "<img src='icon_smile.gif'>&nbsp;", "</", 0, true);
				foundStr = (ex2.findNext()).toUpperCase();
				while(ex2.endOfSearch() == false){
					for(int p = 0; p<byVec.length;p++){
						//Vm.debug(byVec[p]);
						if(foundStr.indexOf(byVec[p].toUpperCase()) >= 0) ch.is_filtered = true;
					}
					foundStr = ex2.findNext();
				} //while
				
			}
			// found or not found in the last days....
			if(days.length()>0){
				try{
					int tage = Convert.parseInt(days);
					// what is todays date?
					Time dtm = new Time();
					DateChange dca = new DateChange();
					dtm.getTime();
					
					ex = new Extractor(text, "<LOGS>", "</LOGS>", 0, true);
					logStr = ex.findNext();
					Extractor annex;
					Extractor trex;
					int tag,monat,jahr = 0;
					String logLogStr = new String();
					annex = new Extractor(logStr, "<img src='icon_smile.gif'>&nbsp;", " by",0,true);
					logLogStr = annex.findNext();
					while(!annex.endOfSearch()){
						logLogStr = "-" + logLogStr + "-";
						//Vm.debug("Log: " + logLogStr + " for " + ch.wayPoint);
						trex = new Extractor(logLogStr,"-","-",0,true);
						jahr = Convert.parseInt(trex.findNext());
						monat = Convert.parseInt(trex.findNext());
						tag = Convert.parseInt(trex.findNext());
						dca = Time.dateDifference(dtm,new Time(tag, monat, jahr), dca);
						//Vm.debug("tag: " +tag + " monat: " + monat + " jahr: " +jahr);
						//Vm.debug("tag: " +dtm.day + " monat: " + dtm.month + " jahr: " +dtm.year);
						//Vm.debug("Diff: " + dca.totalDays + " Tage + " + tage + " filt: " +ch.is_filtered);
						if(daysdirec == FOUND && dca.totalDays <= tage) ch.is_filtered = true;
						if(daysdirec == NOTFOUND && dca.totalDays >= tage) ch.is_filtered = true;
						//Vm.debug("filt: " + ch.is_filtered);
						if(ch.is_filtered == true) break;
						logLogStr = annex.findNext();
					}
					//if(ch.is_filtered == true) break;
				} catch (NumberFormatException nfex){
					//Vm.debug(nfex.toString());
				}
			}
			cacheDB.set(i,ch);
		}
		
		//sort filtered and adapt the count in the table!
	}
	
	/**
	*	Invert is_filtered flag on all caches
	*/
	public void invertFilter(Vector cacheDB){
		CacheHolder ch;
		for(int i = 0; i < cacheDB.size(); i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_filtered == true) ch.is_filtered = false;
			else ch.is_filtered = true;
			cacheDB.set(i, ch);
		}
	}
	
	/**
	*	Clear the is_filtered flag from the cache database.
	*/
	public void clearFilter(Vector cacheDB){
		CacheHolder ch;
		for(int i = 0; i < cacheDB.size(); i++){
			ch = (CacheHolder)cacheDB.get(i);
			ch.is_filtered = false;
			cacheDB.set(i, ch);
		}
	}
}

