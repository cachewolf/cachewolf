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
	public int visible(){
		CacheHolder holder;
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = cacheDB.get(i);
			if(holder.isVisible()){
				if(holder.getWayPoint().startsWith("GC") || holder.isOC()) counter++;
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
			holder = cacheDB.get(i);
			if(holder.is_black() == false){
				if(holder.getWayPoint().startsWith("GC") || holder.isOC()) counter++;
			}
		}
		return counter;
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