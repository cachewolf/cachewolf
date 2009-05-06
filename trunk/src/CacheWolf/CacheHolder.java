package CacheWolf;

import CacheWolf.navi.Metrics;

import com.stevesoft.ewe_pat.Regex;

import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.io.IOException;
import ewe.sys.Convert;
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
	private String cacheStatus = EMPTY;
	/** The name of the waypoint typicall GC.... or OC.... or CW...... (can be any characters) */
	private String wayPoint = EMPTY;
	/** The name of the cache (short description) */
	private String cacheName = EMPTY;
	/** The alias of the owner */
	private String cacheOwner = EMPTY;
	/** The coordinates of the cache */
	public CWPoint pos = new CWPoint();
	/** The coordinates of the cache */
	public String LatLon = pos.toString();
	/** The date when the cache was hidden in format yyyy-mm-dd */
	private String dateHidden = EMPTY;
	/** The size of the cache (as per GC cache sizes Micro, Small, ....) */
	private String cacheSize = "None";
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
	private String hard = EMPTY;
	/** The terrain rating of the cache from 1 to 5 in .5 incements */
	private String terrain = EMPTY;
	/** The cache type (@see CacheType for translation table)  */
	private int type = 0; 
	/** True if the cache has been archived */
	private boolean archived = false;
	/** True if the cache is available for searching */
	private boolean available = true;
	/** True if we own this cache */
	private boolean owned = false;
	/** True if we have found this cache */
	private boolean found = false;
	/** If this is true, the cache has been filtered (is currently invisible) */
	private boolean filtered = false;
	/** True if the number of logs for this cache has changed */
	private boolean log_updated = false;
	/** True if cache details have changed: longDescription, Hints,  */
	private boolean cache_updated = false;
	/** True if the cache data is incomplete (e.g. an error occurred during spidering */
	private boolean incomplete = false;
	/** True if the cache is blacklisted */
	private boolean black = false;
	/** True if the cache is new */
	private boolean newCache = false;
	/** True if the cache is part of the results of a search */
	public boolean is_flaged = false;
	/** True if the cache has been selected using the tick box in the list view */
	public boolean is_Checked = false;
	/** Not used: This attribute is saved with the cache and read back but never set */
//	public String dirty = EMPTY;
	/** The unique OC cache ID */
	private String ocCacheID = EMPTY;
	/** The number of times this cache has not been found (max. 5) */
	private int noFindLogs = 0;
	/** Number of recommendations (from the opencaching logs) */
	private int numRecommended = 0;
	/** Number of Founds since start of recommendations system */
	private int numFoundsSinceRecommendation = 0;
	/** Recommendation score: calculated as rations  numRecommended / numLogsSinceRecommendation * 100 */
	public int recommendationScore = 0;
	/** True if this cache has travelbugs */
	private boolean bugs = false;
	/** True if the cache description is stored in HTML format */
	private boolean html = true;
	/** List of additional waypoints associated with this waypoint */
	public Vector addiWpts = new Vector();
	/** in range is used by the route filter to identify caches in range of a segment*/
	public boolean in_range = false;
	/** If this is an additional waypoint, this links back to the main waypoint */
	public CacheHolder mainCache;
	/** The date this cache was last synced with OC in format yyyyMMddHHmmss */
	private String lastSyncOC = EMPTY;
	public CacheHolderDetail details = null;
	/** When sorting the cacheDB this field is used. The relevant field is copied here and
	 *  the sort is always done on this field to speed up the sorting process 
	 */
	public String sort;
	private static StringBuffer sb=new StringBuffer(530); // Used in toXML()

	private long attributesYes = 0;
	private long attributesNo  = 0;
	
	private IconAndText iconAndTextWP = null;
	private int iconAndTextWPLevel = 0;

		static char decSep,notDecSep;
	static {
		decSep=MyLocale.getDigSeparator().charAt(0);
		notDecSep=decSep=='.'?',':'.';
	}

	public CacheHolder() {  // Just a public constructor
	}
	
	public CacheHolder(String xmlString) {
		int start,end;
		if (xmlString.length() <= 10) {
			this.wayPoint = xmlString;
		} else	
	        try {
		        start = xmlString.indexOf('"');
		        end = xmlString.indexOf('"', start + 1);
		        setCacheName(SafeXML.cleanback(xmlString.substring(start + 1, end)));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setCacheOwner(SafeXML.cleanback(xmlString.substring(start + 1, end)));
		        // Assume coordinates are in decimal format
		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        double lat = Convert.parseDouble(xmlString.substring(start + 1, end).replace(
		                notDecSep, decSep));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        double lon = Convert.parseDouble(xmlString.substring(start + 1, end).replace(
		                notDecSep, decSep));
		        pos = new CWPoint(lat, lon);
		        LatLon = pos.toString();

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setDateHidden(xmlString.substring(start + 1, end));
		        // Convert the US format to YYYY-MM-DD if necessary
		        if (getDateHidden().indexOf('/') > -1)
			        setDateHidden(DateFormat.MDY2YMD(getDateHidden()));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setWayPoint(SafeXML.cleanback(xmlString.substring(start + 1, end)));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setCacheStatus(xmlString.substring(start + 1, end));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setType(new Integer(xmlString.substring(start + 1, end)).intValue());

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setHard(xmlString.substring(start + 1, end));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setTerrain(xmlString.substring(start + 1, end));

		        // The next item was 'dirty' but this is no longer used.
		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setFiltered(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setCacheSize(xmlString.substring(start + 1, end));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setAvailable(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setArchived(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setHas_bugs(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setBlack(xmlString.substring(start + 1, end).equals("true"));
		        if (is_black() != Global.getProfile().showBlacklisted())
			        setFiltered(true);

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setOwned(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setFound(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setNew(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setLog_updated(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setUpdated(xmlString.substring(start + 1, end).equals("true"));

		        // for backwards compatibility set value to true, if it is not in the file
		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setHTML(!xmlString.substring(start + 1, end).equals("false"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setNoFindLogs(Convert.toInt(xmlString.substring(start + 1, end)));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setOcCacheID(xmlString.substring(start + 1, end));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setIncomplete(xmlString.substring(start + 1, end).equals("true"));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setLastSyncOC(xmlString.substring(start + 1, end));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setNumRecommended(Convert.toInt(xmlString.substring(start + 1, end)));

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        setNumFoundsSinceRecommendation(Convert.toInt(xmlString.substring(start + 1, end)));
		        recommendationScore = LogList.getScore(getNumRecommended(),
		                getNumFoundsSinceRecommendation());

		        start = xmlString.indexOf('"', end + 1);
		        end = xmlString.indexOf('"', start + 1);
		        if (start > -1 && end > -1) {
			        setAttributesYes(Convert.parseLong(xmlString.substring(start + 1, end)));

			        start = xmlString.indexOf('"', end + 1);
			        end = xmlString.indexOf('"', start + 1);
			        if (start > -1 && end > -1)
				        setAttributesNo(Convert.parseLong(xmlString.substring(start + 1, end)));
		        }
	        } catch (Exception ex) {
	        	Global.getPref().log("Ignored Exception", ex, true);
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
	/** 
	 * Updates Cache information with information provided by cache given as argument. This is used
	 * to update the cache with the information retrieved from files or web: The argument cache
	 * is the one that is filled with the read information, <code>this</code> is the cache that
	 * is already in the database and subject to update. 
	 * @param ch The cache who's information is updating the current one
	 * @param overwrite If <code>true</code>, then <i>status</i>, <i>is_found</i> and <i>position</i>
	 * is updated, otherwise not.
	 */
	public void update(CacheHolder ch, boolean overwrite) {
		this.recommendationScore = ch.recommendationScore;
		this.setNumFoundsSinceRecommendation(ch.getNumFoundsSinceRecommendation());
		this.setNumRecommended(ch.getNumRecommended());
		if (overwrite) {
			this.setCacheStatus(ch.getCacheStatus());
			this.setFound(ch.is_found());
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
	   any                 any                 empty            no
			 */
			if (!this.is_found() || this.getCacheStatus().indexOf(":")<0) {
				// don't overwrite with empty data
				if (!ch.getCacheStatus().trim().equals("")) {
					this.setCacheStatus(ch.getCacheStatus());
				}
				this.setFound(ch.is_found());
			}
			// Don't overwrite valid coordinates with invalid ones
			if (ch.pos.isValid() || !this.pos.isValid()) {
				this.pos = ch.pos;
				this.LatLon = ch.LatLon;
			}
		}
		this.setWayPoint(ch.getWayPoint());
		this.setCacheName(ch.getCacheName());
		this.setCacheOwner(ch.getCacheOwner());

		this.setDateHidden(ch.getDateHidden());
		this.setCacheSize(ch.getCacheSize());
		this.kilom = ch.kilom;
		this.bearing = ch.bearing;
		this.degrees = ch.degrees;
		this.setHard(ch.getHard());
		this.setTerrain(ch.getTerrain());
		this.setType(ch.getType());
		this.setArchived(ch.is_archived());
		this.setAvailable(ch.is_available());
		this.setOwned(ch.is_owned());
		this.setFiltered(ch.is_filtered());
//		this.setLog_updated(ch.is_log_updated());
//		this.setUpdated(ch.is_updated());
		this.setIncomplete(ch.is_incomplete());
		this.setBlack(ch.is_black());
		this.addiWpts = ch.addiWpts;
		this.mainCache=ch.mainCache;
//		this.setNew(ch.is_new());
		// I don't think that updating a cache with current data should affect the state
		// if a cache is checked or a search result. So the following two assignments are
		// removed.
//		this.is_flaged = ch.is_flaged;
//		this.is_Checked = ch.is_Checked;
		this.setOcCacheID(ch.getOcCacheID());
		this.setNoFindLogs(ch.getNoFindLogs());
		this.setHas_bugs(ch.has_bugs());
		this.setHTML(ch.is_HTML());
		this.sort=ch.sort;
		this.setLastSyncOC(ch.getLastSyncOC());

		this.setAttributesYes(ch.getAttributesYes());
		this.setAttributesNo(ch.getAttributesNo());
		if (ch.detailsLoaded()) {
			this.getFreshDetails().update(ch.getFreshDetails());
		}	
	}
	/**
	 * Call it only when necessary, it takes time, because all logs must be parsed
	 */
	public void calcRecommendationScore() {
		if (getWayPoint().toLowerCase().startsWith("oc")) {
			// Calculate recommendation score only when details
			// are already loaded. When they aren't loaded, then we assume
			// that there is no change, so nothing to do.
			if (this.detailsLoaded()) {
				CacheHolderDetail chD = getCacheDetails(true, false);
				if (chD != null) {
					chD.CacheLogs.calcRecommendations();
					recommendationScore = chD.CacheLogs.recommendationRating;
					setNumFoundsSinceRecommendation(chD.CacheLogs.foundsSinceRecommendation);
					setNumRecommended(chD.CacheLogs.numRecommended);
				} else { // cache doesn't have details
					recommendationScore = -1;
					setNumFoundsSinceRecommendation(-1);
					setNumRecommended(-1);
				}
			}
		} else {
			recommendationScore = -1;
			setNumFoundsSinceRecommendation(-1);
			setNumRecommended(-1);
		}
	}
	
	/** Return a XML string containing all the cache data for storing in index.xml */
	public String toXML() {
		calcRecommendationScore(); 
		sb.delete(0,sb.length());
		sb.append("    <CACHE ");
		sb.append(" name = \"");        sb.append(SafeXML.clean(getCacheName()));
		sb.append("\" owner = \"");		sb.append(SafeXML.clean(getCacheOwner()));
		sb.append("\" lat = \""); 		sb.append(pos.latDec ); 
		sb.append("\" lon = \"");		sb.append(pos.lonDec);
		sb.append("\" hidden = \"");	sb.append(getDateHidden());
		sb.append("\" wayp = \"");		sb.append(SafeXML.clean(getWayPoint()));
		sb.append("\" status = \"");	sb.append(getCacheStatus());
		sb.append("\" type = \"");		sb.append(getType());
		sb.append("\" dif = \"");		sb.append(getHard());
		sb.append("\" terrain = \"" );	sb.append(getTerrain() ); 
		sb.append("\" filtered = \"" ); sb.append(is_filtered()); // This was 'dirty', but dirty is not used
		sb.append("\" size = \"");		sb.append(getCacheSize());
		sb.append("\" online = \"" );	sb.append(is_available()); 
		sb.append("\" archived = \"" );	sb.append(is_archived()); 
		sb.append("\" has_bug = \"" ); 	sb.append(has_bugs()); 
		sb.append("\" black = \"" ); 	sb.append(is_black()); 
		sb.append("\" owned = \"" ); 	sb.append(is_owned()); 
		sb.append("\" found = \"" ); 	sb.append(is_found()); 
		sb.append("\" is_new = \"" );	sb.append(is_new());
		sb.append("\" is_log_update = \"" );sb.append(is_log_updated()); 
		sb.append("\" is_update = \"" );sb.append(is_updated()); 
		sb.append("\" is_HTML = \"" ); 	sb.append(is_HTML()); 
		sb.append("\" DNFLOGS = \"" ); 	sb.append(getNoFindLogs()); 
		sb.append("\" ocCacheID = \"" );sb.append(getOcCacheID()); 
		sb.append("\" is_INCOMPLETE = \"");sb.append(is_incomplete()); 
		sb.append("\" lastSyncOC = \"" );sb.append(getLastSyncOC()); 
		sb.append("\" num_recommended = \"");sb.append(Convert.formatInt(getNumRecommended())); 
		sb.append("\" num_found = \"" );sb.append(Convert.formatInt(getNumFoundsSinceRecommendation()));
		sb.append("\" attributesYes = \"" ); sb.append(Convert.formatLong(getAttributesYes()));
		sb.append("\" attributesNo = \"" ); sb.append(Convert.formatLong(getAttributesNo()));
		sb.append("\" />\n");
		return sb.toString();
	}

	public void setLatLon(String latLon) {
		latLon=latLon.trim();
		if (!latLon.equals(LatLon.trim())) setUpdated(true);
		LatLon = latLon;
		pos.set(latLon);
	}

	public boolean isAddiWpt() {
		return CacheType.isAddiWpt(this.getType());
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
	public void setAttributesFromMainCache(){
		CacheHolder mainCh = this.mainCache;
		this.setCacheOwner(mainCh.getCacheOwner());
		this.setCacheStatus(mainCh.getCacheStatus());
		this.setArchived(mainCh.is_archived());
		this.setAvailable(mainCh.is_available());
		this.setBlack(mainCh.is_black());
		this.setOwned(mainCh.is_owned());
		this.setNew(mainCh.is_new());
		this.setFound(mainCh.is_found());
	}

	public void setAttributesToAddiWpts(){
		if (this.hasAddiWpt()){
			CacheHolder addiWpt;
			for (int i= this.addiWpts.getCount() - 1;  i>=0; i--){
				addiWpt = (CacheHolder) this.addiWpts.get(i);
				addiWpt.setAttributesFromMainCache();
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
		if (ch.isAddiWpt()) main2 = ch.mainCache; else main2 = ch; 
		return main1 == main2;
	}

	/** Find out of detail object of Cache is loaded. Returns <code>true</code> if this is the case.
	 * @return True when details object is present
	 */
	public boolean detailsLoaded() {
		return details!=null;
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
	 * Gets the detail object of a cache. The detail object stores information which is not needed
	 * for every cache instantaneously, but can be loaded if the user decides to look at this cache.
	 * If the cache object is already existing, the method will return this object, otherwise it 
	 * will create it and try to read it from the corresponding <waypoint>.xml file.
	 * Depending on the parameters it is allowed that the <waypoint>.xml file does not yet exist,
	 * or the user is warned that the file doesn't exist.
	 * If more than <code>maxdetails</code> details are loaded, then the 5 last recently loaded 
	 * caches are unloaded (to save ram). 
	 * 
	 * @param maybenew
	 * 			  If true and the cache file could not be read, then an empty detail object is 
	 *            returned.
	 * @param alarmuser
	 *            If true an error message will be displayed to the user, if the details could not
	 *            be read, and the method returns null 
	 * @return The respective CacheHolderDetail, or null
	 */

	public CacheHolderDetail getCacheDetails(boolean maybenew, boolean alarmuser) {
		if (details == null) {

			details = new CacheHolderDetail(this);
			try {
				details.readCache(Global.getProfile().dataDir);
			} catch (IOException e) {
				if (alarmuser && !maybenew) {
					(new MessageBox("Error", "Could not read cache details for cache: "
					        + this.getWayPoint(), FormBase.OKB)).execute();
				}
				if (!maybenew) details = null;
			}
			if (details != null
					  // for importing/spidering reasons helper objects with same waypoint are created
					&& !cachesWithLoadedDetails.contains(this.getWayPoint())
					  // helper objects may have empty waypoint
					&& !this.getWayPoint().equals(CacheHolder.EMPTY)) {
				cachesWithLoadedDetails.add(this.getWayPoint());
				if (cachesWithLoadedDetails.size() >= Global.getPref().maxDetails) removeOldestDetails();
			}
		}
		return details;
	}
	
	/**
	 * Gets a detail object for the cache. If the object is already created, then this object is
	 * returned, otherwise it's created from the cache.xml file. If no such file is found, an empty
	 * object is returned.
	 * @return The object representing the cache details
	 */
	public CacheHolderDetail getFreshDetails() {
		return this.getCacheDetails(true, false);
	}
	/**
	 * Gets a detail object for the cache. If the object is already created, then this object is
	 * returned, otherwise it's created from the cache.xml file. If no such file is found, an error
	 * message is displayed and <code>null</code> is returned.
	 * @return The object representing the cache details, or <code>null</code>.
	 */
	public CacheHolderDetail getExistingDetails() {
		return this.getCacheDetails(false, true);
	}

	/**
	 * Saves the cache to the corresponding <waypoint>.xml file, located in the profiles
	 * directory. The waypoint of the 
	 * cache should be set to do so.
	 */
	public void save() {
		this.getFreshDetails().saveCacheDetails(Global.getProfile().dataDir);
	}
	
	void releaseCacheDetails() {
		if (details != null && details.hasUnsavedChanges){
			details.saveCacheDetails(Global.getProfile().dataDir);
		}
		details = null;
		cachesWithLoadedDetails.remove(this.getWayPoint());
	}

	//final static int maxDetails = 50; 
	static Vector cachesWithLoadedDetails = new Vector(Global.getPref().maxDetails);

	public static void removeOldestDetails() {
		for (int i=0; i<Global.getPref().deleteDetails; i++) {
			String wp = (String) cachesWithLoadedDetails.get(i);
			CacheHolder ch = Global.getProfile().cacheDB.get(wp);
			if (ch!=null) ch.releaseCacheDetails();
		}	
	}

	public static void removeAllDetails() {
		for (int i=cachesWithLoadedDetails.size()-1; i>=0; i--) {
			String wp = (String) cachesWithLoadedDetails.get(i);
			CacheHolder ch = Global.getProfile().cacheDB.get(wp);
			if (ch!=null && ch.detailsLoaded()) ch.releaseCacheDetails();
		}
	}

	/**
	 * when importing caches you can set details.saveChanges = true
	 * when the import is finished call this method to save the pending changes
	 */
	public static void saveAllModifiedDetails() {
		CacheHolder ch;
		CacheHolderDetail chD;
		for (int i=cachesWithLoadedDetails.size()-1; i>=0; i--) {
			String wp = (String) cachesWithLoadedDetails.get(i);
			ch = Global.getProfile().cacheDB.get(wp);
			if (ch != null) {
	            chD = ch.getExistingDetails();
	            if (chD!=null && chD.hasUnsavedChanges) {
		            //ch.calcRecommendationScore();
		            chD.saveCacheDetails(Global.getProfile().dataDir);
	            }
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
		
		if (is_found()) {
			Regex rexDate=new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
			rexDate.search(getCacheStatus());
			if (rexDate.stringMatched(1)!= null) {
				statusDate = rexDate.stringMatched(1);
			}
		}

		return statusDate;		
	}
	
	public String GetStatusTime() {
		String statusTime = "";

		if (is_found()) {
			Regex rexTime=new Regex("([0-9]{1,2}:[0-9]{2})");
			rexTime.search(getCacheStatus());
			if (rexTime.stringMatched(1)!= null) {
				statusTime = rexTime.stringMatched(1);
			}
			else {
				Regex rexDate=new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
				rexDate.search(getCacheStatus());
				if (rexDate.stringMatched(1)!= null) {
					statusTime = "00:00";
				}
			}
		}

		return statusTime;		
	}
		
	public String GetCacheID() {
		String result = "";
		
		if ( getWayPoint().toUpperCase().startsWith( "GC" ) ) {
			int gcId = 0;

			String sequence = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
			
			String rightPart = getWayPoint().substring( 2 ).toUpperCase();
			
			int base = 31;
			if ((rightPart.length() < 4) || (rightPart.length() == 4 && sequence.indexOf(rightPart.charAt(0)) < 16)) {
				base = 16;
			}
			
			for ( int p = 0; p < rightPart.length(); p++ ) {
				gcId *= base;
				gcId += sequence.indexOf(rightPart.charAt(p));
			}
			
	        if ( base == 31 ) {
	        	gcId += java.lang.Math.pow(16, 4) - 16 * java.lang.Math.pow(31, 3);
	        }
	        
	        result = Integer.toString(gcId);	        
		} else if ( getWayPoint().toUpperCase().startsWith( "OC" ) ) {
        	result = getOcCacheID();
        }

		return result;
	}
	
	/**
	 * Initializes the caches states (and its addis) before updating, so that the "new", "updated",
	 * "log_updated" and "incomplete" properties are properly set. 
	 * @param pNewCache <code>true</code> if it is a new cache (i.e. a cache not existing in CacheDB),
	 * <code>false</code> otherwise.
	 */
	public void initStates(boolean pNewCache) {
		this.setNew(pNewCache);
		this.setUpdated(false);
		this.setLog_updated(false);
		this.setIncomplete(false);
		if (!pNewCache && this.hasAddiWpt()) {
			for (int i=0; i<this.addiWpts.size(); i++) {
				((CacheHolder)this.addiWpts.get(i)).initStates(pNewCache);
			}
		}
	}

	// Getter and Setter for private properties

	/**
	 * Gets an IconAndText object for the cache. If the level of the Icon is equal to the 
	 * last call of the method, the same (cached) object is returned. If the object is
	 * null or the level is different, a new object is created.<br> 
	 * @param level 4=is_incomplete(), 3=is_new(), 2=is_updated(), 1=is_log_updated
	 * @param fm Font metrics
	 * @return New or old IconAndText object
	 */
	public IconAndText getIconAndTextWP(int level, FontMetrics fm) {
		if (level != iconAndTextWPLevel || iconAndTextWP == null) {
			switch (level) {
				case 4: iconAndTextWP = new IconAndText(myTableModel.skull, this.getWayPoint(), fm); break;
				case 3: iconAndTextWP = new IconAndText(myTableModel.yellow, this.getWayPoint(), fm); break;
				case 2: iconAndTextWP = new IconAndText(myTableModel.red, this.getWayPoint(), fm); break;
				case 1: iconAndTextWP = new IconAndText(myTableModel.blue, this.getWayPoint(), fm); break;
			}
			iconAndTextWPLevel = level;
		}
		return iconAndTextWP;
	}
	
	public String getCacheStatus() {
    	return cacheStatus;
    }

	public void setCacheStatus(String cacheStatus) {
		Global.getProfile().notifyUnsavedChanges(!cacheStatus.equals(this.cacheStatus));		
    	this.cacheStatus = cacheStatus;
    }

	public String getWayPoint() {
    	return wayPoint;
    }

	public void setWayPoint(String wayPoint) {
		Global.getProfile().notifyUnsavedChanges(!wayPoint.equals(this.wayPoint));		
    	this.wayPoint = wayPoint;
    }

	public String getCacheName() {
    	return cacheName;
    }

	public void setCacheName(String cacheName) {
		Global.getProfile().notifyUnsavedChanges(!cacheName.equals(this.cacheName));		
    	this.cacheName = cacheName;
    }

	public String getCacheOwner() {
    	return cacheOwner;
    }

	public void setCacheOwner(String cacheOwner) {
		Global.getProfile().notifyUnsavedChanges(!cacheOwner.equals(this.cacheOwner));		
    	this.cacheOwner = cacheOwner;
    }

	public String getDateHidden() {
    	return dateHidden;
    }

	public void setDateHidden(String dateHidden) {
		Global.getProfile().notifyUnsavedChanges(!dateHidden.equals(this.dateHidden));		
    	this.dateHidden = dateHidden;
    }

	public String getCacheSize() {
    	return cacheSize;
    }

	public void setCacheSize(String cacheSize) {
		Global.getProfile().notifyUnsavedChanges(!cacheSize.equals(this.cacheSize));		
    	this.cacheSize = cacheSize;
    }

	public String getHard() {
    	return hard;
    }

	public void setHard(String hard) {
		Global.getProfile().notifyUnsavedChanges(!hard.equals(this.hard));		
    	this.hard = hard;
    }

	public String getTerrain() {
    	return terrain;
    }

	public void setTerrain(String terrain) {
		Global.getProfile().notifyUnsavedChanges(!terrain.equals(this.terrain));		
    	this.terrain = terrain;
    }

	public int getType() {
    	return type;
    }

	public void setType(int type) {
		Global.getProfile().notifyUnsavedChanges(type != this.type);		
    	this.type = type;
    }

	public boolean is_archived() {
    	return archived;
    }

	public void setArchived(boolean is_archived) {
		Global.getProfile().notifyUnsavedChanges(is_archived != this.archived);		
    	this.archived = is_archived;
    }

	public boolean is_available() {
    	return available;
    }

	public void setAvailable(boolean is_available) {
		Global.getProfile().notifyUnsavedChanges(is_available != this.available);		
    	this.available = is_available;
    }

	public boolean is_owned() {
    	return owned;
    }

	public void setOwned(boolean is_owned) {
		Global.getProfile().notifyUnsavedChanges(is_owned != this.owned);		
    	this.owned = is_owned;
    }

	public boolean is_found() {
    	return found;
    }

	public void setFound(boolean is_found) {
		Global.getProfile().notifyUnsavedChanges(is_found != this.found);		
    	this.found = is_found;
    }

	public boolean is_filtered() {
    	return filtered;
    }

	public void setFiltered(boolean is_filtered) {
		Global.getProfile().notifyUnsavedChanges(is_filtered != this.filtered);		
    	this.filtered = is_filtered;
    }

	public boolean is_log_updated() {
    	return log_updated;
    }

	public void setLog_updated(boolean is_log_updated) {
		Global.getProfile().notifyUnsavedChanges(is_log_updated != this.log_updated);		
		if (is_log_updated && iconAndTextWPLevel==1) iconAndTextWP = null;
    	this.log_updated = is_log_updated;
    }

	public boolean is_updated() {
    	return cache_updated;
    }

	public void setUpdated(boolean is_updated) {
		Global.getProfile().notifyUnsavedChanges(is_updated != this.cache_updated);		
		if (is_updated && iconAndTextWPLevel==2) iconAndTextWP = null;
    	this.cache_updated = is_updated;
    }

	public boolean is_incomplete() {
    	return incomplete;
    }

	public void setIncomplete(boolean is_incomplete) {
		Global.getProfile().notifyUnsavedChanges(is_incomplete != this.incomplete);	
		if (is_incomplete && iconAndTextWPLevel==4) iconAndTextWP = null;
    	this.incomplete = is_incomplete;
    }

	public boolean is_black() {
    	return black;
    }

	public void setBlack(boolean is_black) {
		Global.getProfile().notifyUnsavedChanges(is_black != this.black);		
    	this.black = is_black;
    }

	public boolean is_new() {
    	return newCache;
    }

	public void setNew(boolean is_new) {
		Global.getProfile().notifyUnsavedChanges(is_new != this.newCache);		
		if (is_new && iconAndTextWPLevel==3) iconAndTextWP = null;
		this.newCache = is_new;
    }

	public String getOcCacheID() {
    	return ocCacheID;
    }

	public void setOcCacheID(String ocCacheID) {
		Global.getProfile().notifyUnsavedChanges(!ocCacheID.equals(this.ocCacheID));		
    	this.ocCacheID = ocCacheID;
    }

	public int getNoFindLogs() {
    	return noFindLogs;
    }

	public void setNoFindLogs(int noFindLogs) {
		Global.getProfile().notifyUnsavedChanges(noFindLogs != this.noFindLogs);		
    	this.noFindLogs = noFindLogs;
    }

	public int getNumRecommended() {
    	return numRecommended;
    }

	public void setNumRecommended(int numRecommended) {
		Global.getProfile().notifyUnsavedChanges(numRecommended != this.numRecommended);		
    	this.numRecommended = numRecommended;
    }

	public int getNumFoundsSinceRecommendation() {
    	return numFoundsSinceRecommendation;
    }

	public void setNumFoundsSinceRecommendation(int numFoundsSinceRecommendation) {
		Global.getProfile().notifyUnsavedChanges(numFoundsSinceRecommendation != this.numFoundsSinceRecommendation);		
    	this.numFoundsSinceRecommendation = numFoundsSinceRecommendation;
    }

	public boolean has_bugs() {
    	return bugs;
    }

	public void setHas_bugs(boolean has_bug) {
		Global.getProfile().notifyUnsavedChanges(has_bug != this.bugs);		
    	this.bugs = has_bug;
    }

	public boolean is_HTML() {
    	return html;
    }

	public void setHTML(boolean is_HTML) {
		Global.getProfile().notifyUnsavedChanges(is_HTML != this.html);		
    	this.html = is_HTML;
    }

	public String getLastSyncOC() {
    	return lastSyncOC;
    }

	public void setLastSyncOC(String lastSyncOC) {
		Global.getProfile().notifyUnsavedChanges(!lastSyncOC.equals(this.lastSyncOC));		
    	this.lastSyncOC = lastSyncOC;
    }

	public long getAttributesYes() {
    	return attributesYes;
    }

	public void setAttributesYes(long attributesYes) {
		Global.getProfile().notifyUnsavedChanges(attributesYes != this.attributesYes);		
    	this.attributesYes = attributesYes;
    }

	public long getAttributesNo() {
    	return attributesNo;
    }

	public void setAttributesNo(long attributesNo) {
		Global.getProfile().notifyUnsavedChanges(attributesNo != this.attributesNo);		
    	this.attributesNo = attributesNo;
    }

}

