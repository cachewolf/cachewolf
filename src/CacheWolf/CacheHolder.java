package CacheWolf;

import CacheWolf.navi.Metrics;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.IOException;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Vector;

/**
 *	A class to hold information on a cache.<br>
 *	Not all attributes are filled at once. You will have to look at other
 *	classes and methods to get more information.
 *	
 */
/**
 * @author torsti
 *
 */
public class CacheHolder {
	protected static final String NOBEARING = "?";
	protected static final String EMPTY = "";

	/** Cachestatus is Found, Not found or a date in format yyyy-mm-dd hh:mm for found date */
	public String CacheStatus = EMPTY;
	/** The name of the waypoint typicall GC.... or OC.... or CW...... (can be any characters) */
	public String wayPoint = EMPTY;
	/** The name of the cache (short description) */
	public String CacheName = EMPTY;
	/** The alias of the owner */
	public String CacheOwner = EMPTY;
	/** The coordinates of the cache */
	public CWPoint pos = new CWPoint();
	/** The coordinates of the cache */
	public String LatLon = pos.toString();
	/** The date when the cache was hidden in format yyyy-mm-dd */
	public String DateHidden = EMPTY;
	/** The size of the cache (as per GC cache sizes Micro, Small, ....) */
	public String CacheSize = "None";
	/** The distance from the centre in km */
	public double kilom = -1; int bla = 0;
	public double lastKilom = -2; // Cache last value
	public int lastMetric = -1; // Cache last metric
	public String lastDistance = ""; // Cache last distance
	/** The bearing N, NNE, NE, ENE ... from the current centre to this point */
	public String bearing = NOBEARING;
	/** The angle (0=North, 180=South) from the current centre to this point */
	public double degrees = 0;
	/** The difficulty of the cache from 1 to 5 in .5 incements */ 
	public String hard = EMPTY;
	/** The terrain rating of the cache from 1 to 5 in .5 incements */
	public String terrain = EMPTY;
	/** The cache type (@see CacheType for translation table)  */
	public int type = 0; 
	/** True if the cache has been archived */
	public boolean is_archived = false;
	/** True if the cache is available for searching */
	public boolean is_available = true;
	/** True if we own this cache */
	public boolean is_owned = false;
	/** True if we have found this cache */
	public boolean is_found = false;
	/** If this is true, the cache has been filtered (is currently invisible) */
	public boolean is_filtered = false;
	/** True if the number of logs for this cache has changed */
	public boolean is_log_update = false;
	/** True if cache details have changed: longDescription, Hints,  */
	public boolean is_update = false;
	/** True if the cache data is incomplete (e.g. an error occurred during spidering */
	public boolean is_incomplete = false;
	/** True if the cache is blacklisted */
	public boolean is_black = false;
	/** True if the cache is new */
	public boolean is_new = false;
	/** True if the cache is part of the results of a search */
	public boolean is_flaged = false;
	/** True if the cache has been selected using the tick box in the list view */
	public boolean is_Checked = false;
	/** Not used: This attribute is saved with the cache and read back but never set */
//	public String dirty = EMPTY;
	/** The unique OC cache ID */
	public String ocCacheID = EMPTY;
	/** The number of times this cache has not been found (max. 5) */
	public int noFindLogs = 0;
	/** Number of recommendations (from the opencaching logs) */
	public int numRecommended = 0;
	/** Number of Founds since start of recommendations system */
	public int numFoundsSinceRecommendation = 0;
	/** Recommendation score: calculated as rations  numRecommended / numLogsSinceRecommendation * 100 */
	public int recommendationScore = 0;
	/** True if this cache has travelbugs */
	public boolean has_bug = false;
	/** True if the cache description is stored in HTML format */
	public boolean is_HTML = true;
	/** List of additional waypoints associated with this waypoint */
	public Vector addiWpts = new Vector();
	/** in range is used by the route filter to identify caches in range of a segment*/
	public boolean in_range = false;
	/** If this is an additional waypoint, this links back to the main waypoint */
	public CacheHolder mainCache;
	/** The date this cache was last synced with OC in format yyyyMMddHHmmss */
	public String lastSyncOC = EMPTY;
	public CacheHolderDetail details = null;
	/** When sorting the cacheDB this field is used. The relevant field is copied here and
	 *  the sort is always done on this field to speed up the sorting process 
	 */
	public String sort;
	private static StringBuffer sb=new StringBuffer(530); // Used in toXML()

	public long attributesYes = 0;
	public long attributesNo  = 0;

//	static int nObjects=0;
	public CacheHolder() {//nObjects++;Vm.debug("CacheHolder() nO="+nObjects);
	}

	public CacheHolder(CacheHolder ch) {//nObjects++;Vm.debug("CacheHolder(ch) nO="+nObjects);
		update(ch);
	}

	static char decSep,notDecSep;
	static {
		decSep=MyLocale.getDigSeparator().charAt(0);
		notDecSep=decSep=='.'?',':'.';
	}

	public CacheHolder(String xmlString) {
		int start,end;
		try {
			start=xmlString.indexOf('"'); end=xmlString.indexOf('"',start+1);
			CacheName = SafeXML.cleanback(xmlString.substring(start+1,end));
			
			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
            CacheOwner = SafeXML.cleanback(xmlString.substring(start+1,end));
			// Assume coordinates are in decimal format
			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			double lat=Convert.parseDouble(xmlString.substring(start+1,end).replace(notDecSep,decSep));

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			double lon=Convert.parseDouble(xmlString.substring(start+1,end).replace(notDecSep,decSep));
			pos=new CWPoint(lat,lon);
			LatLon=pos.toString();

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			DateHidden = xmlString.substring(start+1,end); 
			// Convert the US format to YYYY-MM-DD if necessary
			if (DateHidden.indexOf('/')>-1) DateHidden=DateFormat.MDY2YMD(DateHidden);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			wayPoint = SafeXML.cleanback(xmlString.substring(start+1,end));

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			CacheStatus = xmlString.substring(start+1,end);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			type = new Integer(xmlString.substring(start+1,end)).intValue();

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			hard = xmlString.substring(start+1,end);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			terrain = xmlString.substring(start+1,end);

			// The next item was 'dirty' but this is no longer used.
			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_filtered = xmlString.substring(start+1,end).equals("true"); 

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			CacheSize = xmlString.substring(start+1,end);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_available = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_archived = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			has_bug = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_black = xmlString.substring(start+1,end).equals("true");
			if(is_black!=Global.getProfile().showBlacklisted) is_filtered = true;

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_owned = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_found = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_new = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_log_update = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_update = xmlString.substring(start+1,end).equals("true");

			// for backwards compatibility set value to true, if it is not in the file
			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_HTML = !xmlString.substring(start+1,end).equals("false");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			noFindLogs = Convert.toInt(xmlString.substring(start+1,end));

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			ocCacheID = xmlString.substring(start+1,end);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			is_incomplete = xmlString.substring(start+1,end).equals("true");

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			lastSyncOC = xmlString.substring(start+1,end);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			numRecommended = Convert.toInt(xmlString.substring(start+1,end));

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			numFoundsSinceRecommendation = Convert.toInt(xmlString.substring(start+1,end));
			recommendationScore = LogList.getScore(numRecommended, numFoundsSinceRecommendation);

			start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
			if (start > -1 && end > -1) {
				attributesYes = Convert.parseLong(xmlString.substring(start+1,end));

				start=xmlString.indexOf('"',end+1); end=xmlString.indexOf('"',start+1);
				if (start > -1 && end > -1)
					attributesNo = Convert.parseLong(xmlString.substring(start+1,end));
			}
		} catch (Exception ex) {

		}
	}

	/**
	 * Returns the distance in formatted output. Using kilometers when metric system is active,
	 * using miles when imperial system is active.
	 * 
	 * @return The current distance.
	 */
	public String getDistance() {
		String result = null;
		String newUnit = null;

		if (this.kilom == this.lastKilom && Global.getPref().metricSystem == this.lastMetric) {
			result = this.lastDistance;
		} else {
			if (this.kilom >= 0) {
				double newValue = 0;
				switch (Global.getPref().metricSystem) {
				case Metrics.IMPERIAL:
					newValue = Metrics.convertUnit(this.kilom, Metrics.KILOMETER, Metrics.MILES);
					newUnit = Metrics.getUnit(Metrics.MILES);
					break;
				case Metrics.METRIC:
				default:
					newValue = this.kilom;
					newUnit = Metrics.getUnit(Metrics.KILOMETER);
					break;
				}
				result = MyLocale.formatDouble(newValue, "0.00") + " " + newUnit;
			} else {
				result = "? "
				        + (Global.getPref().metricSystem == Metrics.IMPERIAL ? Metrics.getUnit(Metrics.MILES) : Metrics.getUnit(Metrics.KILOMETER));
			}
			// Caching values, so reevaluation is only done when really needed
			this.lastKilom = this.kilom;
			this.lastMetric = Global.getPref().metricSystem;
			this.lastDistance = result;
		}
		return result;
	}
	
	public void update(CacheHolder ch) {
		update(ch, false);
	}
	public void update(CacheHolder ch, boolean overwrite) {
		this.recommendationScore = ch.recommendationScore;
		this.numFoundsSinceRecommendation = ch.numFoundsSinceRecommendation;
		this.numRecommended = ch.numRecommended;
		if (overwrite) {
			this.CacheStatus=ch.CacheStatus;
			this.is_found = ch.is_found;
			this.pos = ch.pos;
			this.LatLon = ch.LatLon;
		} else {
			/* Here we have to distinguish several cases:
	   this.is_found       this                ch               Update 'this'
	   --------------------------------------------------------------------
	   false               empty               yyyy-mm-dd       yes
	   true                "Found"             yyyy-mm-dd       yes
	   true                yyyy-mm-dd          yyyy-mm-dd       no (or yes)
	   true                yyyy-mm-dd hh:mm    yyyy-mm-dd       no
			 */
			if (!this.is_found || this.CacheStatus.indexOf(":")<0) {
				this.CacheStatus=ch.CacheStatus;
				this.is_found = ch.is_found;
			}
			// Don't overwrite valid coordinates with invalid ones
			if (ch.pos.isValid() || !this.pos.isValid()) {
				this.pos = ch.pos;
				this.LatLon = ch.LatLon;
			}
		}
		this.wayPoint = ch.wayPoint;
		this.CacheName = ch.CacheName;
		this.CacheOwner = ch.CacheOwner;

		this.DateHidden = ch.DateHidden;
		this.CacheSize = ch.CacheSize;
		this.kilom = ch.kilom;
		this.bearing = ch.bearing;
		this.degrees = ch.degrees;
		this.hard = ch.hard;
		this.terrain = ch.terrain;
		this.type = ch.type;
		this.is_archived = ch.is_archived;
		this.is_available = ch.is_available;
		this.is_owned = ch.is_owned;
		this.is_filtered = ch.is_filtered;
		this.is_log_update = ch.is_log_update;
		this.is_update = ch.is_update;
		this.is_incomplete = ch.is_incomplete;
		this.is_black=ch.is_black;
		this.addiWpts = ch.addiWpts;
		this.mainCache=ch.mainCache;
		this.is_new=ch.is_new;
		this.is_flaged = ch.is_flaged;
		this.is_Checked = ch.is_Checked;
		//this.dirty = ch.dirty;
		this.ocCacheID = ch.ocCacheID;
		this.noFindLogs = ch.noFindLogs;
		this.has_bug = ch.has_bug;
		this.is_HTML = ch.is_HTML;
		this.sort=ch.sort;
		this.lastSyncOC = ch.lastSyncOC;

		this.attributesYes = ch.attributesYes;
		this.attributesNo = ch.attributesNo;
	}
	/**
	 * Call it only when necessary, it takes time, because all logs must be parsed
	 *
	 */
	public void calcRecommendationScore() {
		if (wayPoint.toLowerCase().startsWith("oc") ) {
			CacheHolderDetail chD;
			if (this instanceof CacheHolderDetail)	chD = (CacheHolderDetail)this;
			else chD = getCacheDetails(true, false);
			if (chD != null) {
				chD.CacheLogs.calcRecommendations();
				recommendationScore = chD.CacheLogs.recommendationRating;
				numFoundsSinceRecommendation = chD.CacheLogs.foundsSinceRecommendation;
				numRecommended = chD.CacheLogs.numRecommended;
			} else { // cache doesn't have details
				recommendationScore = -1;
				numFoundsSinceRecommendation = -1;
				numRecommended = -1;
			}
		} else {
			recommendationScore = -1;
			numFoundsSinceRecommendation = -1;
			numRecommended = -1;
		}
		if (details != null) {
		details.recommendationScore = recommendationScore;
		details.numFoundsSinceRecommendation = numFoundsSinceRecommendation;
		details.numRecommended = numRecommended;
		}
	}
	
	/** Return a XML string containing all the cache data for storing in index.xml */
	public String toXML() {
		if (this instanceof CacheHolderDetail || (details != null && details.hasUnsavedChanges)) calcRecommendationScore(); 
		sb.delete(0,sb.length());
		sb.append("    <CACHE name = \"");
		sb.append(SafeXML.clean(CacheName));
		sb.append("\" owner = \"");		sb.append(SafeXML.clean(CacheOwner));
		sb.append("\" lat = \""); 		sb.append(pos.latDec ); 
		sb.append("\" lon = \"");		sb.append(pos.lonDec);
		sb.append("\" hidden = \"");	sb.append(DateHidden);
		sb.append("\" wayp = \"");		sb.append(SafeXML.clean(wayPoint));
		sb.append("\" status = \"");	sb.append(CacheStatus);
		sb.append("\" type = \"");		sb.append(type);
		sb.append("\" dif = \"");		sb.append(hard);
		sb.append("\" terrain = \"" );	sb.append(terrain ); 
		sb.append("\" filtered = \"" ); sb.append(is_filtered); // This was 'dirty', but dirty is not used
		sb.append("\" size = \"");		sb.append(CacheSize);
		sb.append("\" online = \"" );	sb.append(is_available); 
		sb.append("\" archived = \"" );	sb.append(is_archived); 
		sb.append("\" has_bug = \"" ); 	sb.append(has_bug); 
		sb.append("\" black = \"" ); 	sb.append(is_black); 
		sb.append("\" owned = \"" ); 	sb.append(is_owned); 
		sb.append("\" found = \"" ); 	sb.append(is_found); 
		sb.append("\" is_new = \"" );	sb.append(is_new);
		sb.append("\" is_log_update = \"" );sb.append(is_log_update); 
		sb.append("\" is_update = \"" );sb.append(is_update); 
		sb.append("\" is_HTML = \"" ); 	sb.append(is_HTML); 
		sb.append("\" DNFLOGS = \"" ); 	sb.append(noFindLogs ); 
		sb.append("\" ocCacheID = \"" );sb.append(ocCacheID ); 
		sb.append("\" is_INCOMPLETE = \"");sb.append(is_incomplete); 
		sb.append("\" lastSyncOC = \"" );sb.append(lastSyncOC ); 
		sb.append("\" num_recommended = \"");sb.append(Convert.formatInt(numRecommended)); 
		sb.append("\" num_found = \"" );sb.append(Convert.formatInt(numFoundsSinceRecommendation));
		sb.append("\" attributesYes = \"" ); sb.append(Convert.formatLong(attributesYes));
		sb.append("\" attributesNo = \"" ); sb.append(Convert.formatLong(attributesNo));
		sb.append("\" />\n");
		return sb.toString();
	}

	public void setLatLon(String latLon) {
		latLon=latLon.trim();
		if (!latLon.equals(LatLon.trim())) is_update=true;
		LatLon = latLon;
		pos.set(latLon);
	}

	public boolean isAddiWpt() {
		return CacheType.isAddiWpt(this.type);
	}

	public boolean hasAddiWpt() {
		if (this.addiWpts.getCount()>0) return true;
		else return false;
	}


	public void calcDistance(CWPoint toPoint) {	
		if(pos.isValid()){
			kilom = pos.getDistance(toPoint);
			degrees = toPoint.getBearing(pos);
			bearing = CWPoint.getDirection(degrees);
		} else {
			kilom = -1;
			bearing = NOBEARING;
		}
	}
	public void setAttributesFromMainCache(CacheHolder mainCh){
		this.CacheOwner = mainCh.CacheOwner;
		this.CacheStatus = mainCh.CacheStatus;
		this.is_archived = mainCh.is_archived;
		this.is_available = mainCh.is_available;
		this.is_black = mainCh.is_black;
		this.is_owned = mainCh.is_owned;
		this.is_new = mainCh.is_new;
		this.is_found = mainCh.is_found;
	}

	public void setAttributesToAddiWpts(){
		if (this.hasAddiWpt()){
			CacheHolder addiWpt;
			for (int i= this.addiWpts.getCount() - 1;  i>=0; i--){
				addiWpt = (CacheHolder) this.addiWpts.get(i);
				addiWpt.setAttributesFromMainCache(this);
			}
		}
	}

	/**
	 * True if ch and this belong to the same main cache. 
	 * @param ch
	 * @return
	 */
	public boolean hasSameMainCache(CacheHolder ch) {
		if (this == ch) return true;
		if (ch == null) return false;
		if ((!this.isAddiWpt()) && (!ch.isAddiWpt())) return false;
		CacheHolder main1, main2;
		if (this.isAddiWpt()) main1 = this.mainCache;  else main1 = this;
		if (ch instanceof CacheHolderDetail) {
			if (ch.isAddiWpt()) 
				main2=ch.mainCache;
			else 
				return main1.wayPoint.equals(ch.wayPoint);
		} else { // ch instanceof CacheHolder 
			if (ch.isAddiWpt()) main2 = ch.mainCache; else main2 = ch; 
		}
		return main1 == main2;
	}

	/** 
	 * Call this method to get the long-description and so on.
	 * If the according .xml-file is already read, it will return
	 * that one, otherwise it will be loaded.
	 * To avoid memory problems this routine loads not for more caches than maxDetails
	 * the details. If maxdetails is reached, it will remove from RAM the details 
	 * of the 5 caches that were loaded most long ago.
	 */
	public CacheHolderDetail getCacheDetails(boolean maybenew) {
		return getCacheDetails(maybenew, true);
	}
	
	/** 
	 * Call this method to get the long-description and so on.
	 * If the according .xml-file is already read, it will return
	 * that one, otherwise it will be loaded.
	 * To avoid memory problems this routine loads not for more caches than maxDetails
	 * the details. If maxdetails is reached, it will remove from RAM the details 
	 * of the 5 caches that were loaded most long ago.
	 * 
	 * @param alarmuser if true an error message will be displayed to the user, if the details could not be read
	 * @return the respective CacheHolderDetail, null if according xml-file could not be read
	 */
		
	public CacheHolderDetail getCacheDetails(boolean maybenew, boolean alarmuser) {
		if (details != null) {
			if (details.hasUnsavedChanges) this.update(details);
			else details.update(this);
			return details;
		}
		details = new CacheHolderDetail(this);
		try {
			details.readCache(Global.getProfile().dataDir);
		} catch (IOException e) {
			if (maybenew) details.update(this);
			else {
				if (alarmuser) (new MessageBox("Error", "Could not read cache details for cache: "+this.wayPoint, FormBase.OKB)).execute();
				return null;
			} 
		}
		detailsAdded();
		return details;
	}

	/**
	 * Call this after you added the cache with details to the 
	 * cacheDB <br> It is assumed that that details is set
	 * for an example see OCXMLImporter.endCache()
	 *
	 */
	public void detailsAdded() {
		cachesWithLoadedDetails.add(this);
		if (cachesWithLoadedDetails.size() >= Global.getPref().maxDetails) removeOldestDetails();
	}

	public void releaseCacheDetails() {
		if (details != null && details.hasUnsavedChanges){
			//calcRecommendationScore();
			details.saveCacheDetails(Global.getProfile().dataDir);
			this.update(details);
		}
		details = null;
		cachesWithLoadedDetails.remove(this);
	}

	//final static int maxDetails = 50; 
	static Vector cachesWithLoadedDetails = new Vector(Global.getPref().maxDetails);

	public static void removeOldestDetails() {
		for (int i=0; i<Global.getPref().deleteDetails; i++)
			((CacheHolder)(cachesWithLoadedDetails.get(0))).releaseCacheDetails();
	}

	public static void removeAllDetails() {
		for (int i=cachesWithLoadedDetails.size()-1; i>=0; i--)
			((CacheHolder)(cachesWithLoadedDetails.get(0))).releaseCacheDetails();
	}

	/**
	 * when importing caches you can set details.saveChanges = true
	 * when the import ist finished call this method to save the pending changes
	 *
	 */
	public static void saveAllModifiedDetails() {
		CacheHolder ch;
		CacheHolderDetail chD;
		for (int i=cachesWithLoadedDetails.size()-1; i>=0; i--) {
			ch = (CacheHolder)(cachesWithLoadedDetails.get(i));
			chD = ch.getCacheDetails(false);
			if (chD.hasUnsavedChanges) {
				//ch.calcRecommendationScore();
				chD.saveCacheDetails(Global.getProfile().dataDir);
				ch.update(chD);
			}
		}
	}

	/*
public void finalize() {nObjects--;
   Vm.debug("Destroying CacheHolder "+wayPoint);
   Vm.debug("CacheHolder: "+nObjects+" objects left");
}
	 */
	
	public String GetStatusDate() {
		String statusDate = "";
		
		if (is_found) {
			Regex rexDate=new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
			rexDate.search(CacheStatus);
			if (rexDate.stringMatched(1)!= null) {
				statusDate = rexDate.stringMatched(1);
			}
		}

		return statusDate;		
	}
	
	public String GetStatusTime() {
		String statusTime = "";

		if (is_found) {
			Regex rexTime=new Regex("([0-9]{1,2}:[0-9]{2})");
			rexTime.search(CacheStatus);
			if (rexTime.stringMatched(1)!= null) {
				statusTime = rexTime.stringMatched(1);
			}
			else {
				Regex rexDate=new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
				rexDate.search(CacheStatus);
				if (rexDate.stringMatched(1)!= null) {
					statusTime = "00:00";
				}
			}
		}

		return statusTime;		
	}
		
	public String GetCacheID() {
		String result = "";
		
		if ( wayPoint.toUpperCase().startsWith( "GC" ) ) {
			int gcId = 0;

			String sequence = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
			
			String rightPart = wayPoint.substring( 2 ).toUpperCase();
			
			int base = 31;
			if ((rightPart.length() < 4) || (rightPart.length() == 4 && sequence.indexOf(rightPart.charAt(0)) < 16)) {
				base = 16;
			}
			
			for ( int pos = 0; pos < rightPart.length(); pos++ ) {
				gcId *= base;
				gcId += sequence.indexOf(rightPart.charAt(pos));
			}
			
	        if ( base == 31 ) {
	        	gcId += java.lang.Math.pow(16, 4) - 16 * java.lang.Math.pow(31, 3);
	        }
	        
	        result = Integer.toString(gcId);	        
		} else if ( wayPoint.toUpperCase().startsWith( "OC" ) ) {
        	result = ocCacheID;
        }

		return result;
	}
}

