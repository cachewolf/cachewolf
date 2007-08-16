package CacheWolf;
import ewe.util.Vector;

/**
*	A class to hold information on a cache.<br>
*	Not all attributes are filled at once. You will have to look at other
*	classes and methods to get more information.
*	
*/
public class CacheHolder {
protected String NODISTANCE = "? km";
protected String NOBEARING = "?";
protected String EMPTY = "";

public String CacheStatus = EMPTY;
public String wayPoint = EMPTY;
public String CacheName = EMPTY;
public String CacheOwner = EMPTY;
public CWPoint pos = new CWPoint();
public String LatLon = pos.toString();
public String DateHidden = EMPTY;
public String CacheSize = "None";
public double kilom = 0;
public String distance = NODISTANCE;
public String bearing = NOBEARING;
public double degrees = 0;
public String hard = EMPTY;
public String terrain = EMPTY;
public String type = "0";
public boolean is_archived = false;
public boolean is_available = true;
public boolean is_owned = false;
public boolean is_found = false;
public boolean is_filtered = false;
public boolean is_log_update = false;
public boolean is_update = false;
public boolean is_selected = false;
public boolean is_incomplete = false;
public boolean is_black = false;
public boolean is_new = false;
public boolean is_flaged = false;
public boolean is_Checked = false;
public String dirty = EMPTY;
public String ocCacheID = EMPTY;
public int noFindLogs = 0;
public boolean has_bug = false;
public boolean is_HTML = true;
public Vector addiWpts = new Vector();
public CacheHolder mainCache;
public String sort;
public String lastSyncOC = EMPTY;

//static int nObjects=0;
CacheHolder() {//nObjects++;Vm.debug("CacheHolder() nO="+nObjects);
}

CacheHolder(CacheHolder ch) {//nObjects++;Vm.debug("CacheHolder(ch) nO="+nObjects);
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
	this.is_selected = ch.is_selected;
	this.is_incomplete = ch.is_incomplete;
	this.is_black=ch.is_black;
	this.addiWpts = ch.addiWpts;
	this.mainCache=ch.mainCache;
	this.is_new=ch.is_new;
	this.is_flaged = ch.is_flaged;
	this.is_Checked = ch.is_Checked;
    this.dirty = ch.dirty;
	this.ocCacheID = ch.ocCacheID;
	this.noFindLogs = ch.noFindLogs;
	this.has_bug = ch.has_bug;
	this.is_HTML = ch.is_HTML;
	this.sort=ch.sort;
	this.lastSyncOC = ch.lastSyncOC;
	
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