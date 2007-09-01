package CacheWolf;
import ewe.sys.Vm;
import ewe.util.Vector;

/**
*	A class to hold information on a cache.<br>
*	Not all attributes are filled at once. You will have to look at other
*	classes and methods to get more information.
*	
*/
public class CacheHolder {
protected static final String NODISTANCE = "? km";
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
/** The distance from the center in km */
public double kilom = 0;
/** The formatted distance such as "x.xx km" */
public String distance = NODISTANCE;
/** The bearing N, NNE, NE, ENE ... from the current center to this point */
public String bearing = NOBEARING;
/** The angle (0=North, 180=South) from the current center to this point */
public double degrees = 0;
/** The difficulty of the cache from 1 to 5 in .5 incements */ 
public String hard = EMPTY;
/** The terrain rating of the cache from 1 to 5 in .5 incements */
public String terrain = EMPTY;
/** The cache type (@see CacheType for translation table)  */
public String type = "0"; //TODO Should be an int
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
//public String dirty = EMPTY;
/** The unique OC cache ID */
public String ocCacheID = EMPTY;
/** The number of times this cache has not been found (max. 5) */
public int noFindLogs = 0;
/** True if this cache has travelbugs */
public boolean has_bug = false;
/** True if the cache description is stored in HTML format */
public boolean is_HTML = true;
/** List of additional waypoints associated with this waypoint */
public Vector addiWpts = new Vector();
/** If this is an additional waypoint, this links back to the main waypoint */
public CacheHolder mainCache;
/** The date this cache was last synced with OC in format yyyyMMddHHmmss */
public String lastSyncOC = EMPTY;
/** When sorting the cacheDB this field is used. The relevant field is copied here and
 *  the sort is always done on this field to speed up the sorting process 
 */
public String sort;
//static int nObjects=0;
CacheHolder() {//nObjects++;Vm.debug("CacheHolder() nO="+nObjects);
}

CacheHolder(CacheHolder ch) {//nObjects++;Vm.debug("CacheHolder(ch) nO="+nObjects);
	update(ch);
}

public void update(CacheHolder ch) {
	this.CacheStatus=ch.CacheStatus;
	this.wayPoint = ch.wayPoint;
	this.CacheName = ch.CacheName;
	this.CacheOwner = ch.CacheOwner;
	this.pos = ch.pos;
	this.LatLon = ch.LatLon;
	this.DateHidden = ch.DateHidden;
	this.CacheSize = ch.CacheSize;
	this.kilom = ch.kilom;
	this.distance = ch.distance;
	this.bearing = ch.bearing;
	this.degrees = ch.degrees;
	this.hard = ch.hard;
	this.terrain = ch.terrain;
	this.type = ch.type;
	this.is_archived = ch.is_archived;
	this.is_available = ch.is_available;
	this.is_owned = ch.is_owned;
	this.is_found = ch.is_found;
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
}

public void setLatLon(String latLon) {
	latLon=latLon.trim();
	if (!latLon.equals(LatLon)) is_update=true;
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
			distance = MyLocale.formatDouble(kilom,"0.00")+" km";
	   } else {
		   distance = NODISTANCE;
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
/*
public void finalize() {nObjects--;
   Vm.debug("Destroying CacheHolder "+wayPoint);
   Vm.debug("CacheHolder: "+nObjects+" objects left");
}
*/
}