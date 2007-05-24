package CacheWolf;
import ewe.util.*;
import ewe.sys.*;

/**
 * @author Marc
 * Use this class to obtain statistics or information on a cache database.
 */
public class DBStats {
	Vector cacheDB = new Vector();
	
	public DBStats(Vector db){
		cacheDB = db;
	}
	
	/**
	 * Method to get the number of caches displayed in the list.
	 * It will count waypoints only that start with
	 * GC,or
	 * OC
	 * @return
	 */
	public int visible(){
		CacheHolder holder;
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false && holder.is_filtered == false){
				if(holder.wayPoint.startsWith("GC") || holder.wayPoint.startsWith("OC")) counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Method to get the number of caches available for display
	 * @return
	 */
	public int total(){
		CacheHolder holder;
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false){
				if(holder.wayPoint.startsWith("GC") || holder.wayPoint.startsWith("OC")) counter++;
			}
		}
		return counter;
	}
	
	public int totalFound(){
		CacheHolder holder;
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_found == true) {
				if(holder.wayPoint.startsWith("GC") || holder.wayPoint.startsWith("OC")) counter++;
			}
		}
		return counter;
	}
}

// TODO ASCExporter.java
//TODO GPXExporter.java
//TODO HTMLExporter.java
//TODO KMLExporter.java
//TODO MSARCSVExporter.java
//TODO OVLExporter.java
//TODO OziExporter.java
//TODO PCX5Exporter.java
//TODO TomTomASCExporter.ja
//TODO TomTomOV2Exporter.ja