package CacheWolf;

/**
 * @author Marc
 * Use this class to obtain statistics or information on a cache database.
 */
public class DBStats {
	CacheDB cacheDB = null;
	
	public DBStats(CacheDB db){
		cacheDB = db;
	}
	
	/**
	 * Method to get the number of caches displayed in the list.
	 * It will count waypoints only that start with
	 * GC,or
	 * OC
	 * @return
	 */
	public String visible(boolean big){
		CacheHolder holder;
		int counter = 0;
		int whiteCaches = 0;
		int whiteWaypoints = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = cacheDB.get(i);
			if(holder.isVisible()){
				counter++;
				if (CacheType.isAddiWpt(holder.getType())) {
					whiteWaypoints++;
				}
				else {				
					whiteCaches++;
				}		
			}
		}
		if (big)
			return counter+"("+whiteCaches+"/"+whiteWaypoints+")";
		else
			return ""+whiteCaches;
		
	}
	
	/**
	 * Method to get the number of caches available for display
	 * @return
	 */
	public String total(boolean big){
		CacheHolder holder;
		int all = cacheDB.size();
		int whiteCaches = 0;
		int whiteWaypoints = 0;
		int blackCaches = 0;
		int blackWaypoints = 0;
		for(int i = 0; i<all;i++){
			holder = cacheDB.get(i);
			if(holder.is_black()){
			  if (CacheType.isAddiWpt(holder.getType())) {
				  blackWaypoints++;  
			  }
			  else {
				  blackCaches++;
			  }
			}
			else {
				if (CacheType.isAddiWpt(holder.getType())) {
					whiteWaypoints++;
				}
				else {				
					whiteCaches++;
				}		
			}
		}
		if (big)
			return all+"("+whiteCaches+"/"+whiteWaypoints+"+"+blackCaches+"/"+blackWaypoints+")";
		else
			return ""+whiteCaches;
	}
	
	public int totalFound(){
		CacheHolder holder;
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = cacheDB.get(i);
			if(holder.is_found() == true) {
				if(holder.getWayPoint().startsWith("GC") || holder.isOC()) counter++;
			}
		}
		return counter;
	}
}