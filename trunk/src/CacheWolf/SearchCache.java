package CacheWolf;
import ewe.util.*;

/**
*	A class to perform a search on the cache database.
*	The searchstr is searched for in the waypoint
*	and the name of the cache.
*	A method is also provided to erase the search results.
*/
public class SearchCache {

	Vector cacheDB;
	
	public SearchCache(Vector DB){
		cacheDB = DB;
	}
	
	/**
	* Method to iterate through the cache database.
	* Each cache where the search string is found (in waypoint
	* and / or cache name) is flagged as matching. The search only
	* acts on the filtered (=visible) set of caches
	*/
	public void search(String searchStr){
		if(searchStr.length()>0){
			Global.getProfile().selectionChanged = true;
			searchStr = searchStr.toUpperCase();
			CacheHolder ch;
			//Search through complete database
			//Mark finds by setting is_flaged
			//TableModel will be responsible for displaying
			//marked caches.
			for(int i = 0;i < cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				if (ch.is_filtered) break; // Reached end of visible records
				if(ch.wayPoint.toUpperCase().indexOf(searchStr) <0 && 
				   ch.CacheName.toUpperCase().indexOf(searchStr) <0 && 
				   ch.CacheStatus.toUpperCase().indexOf(searchStr)<0){
					ch.is_flaged = false;
					ch.is_filtered = true;
				} else
					ch.is_flaged=true;
			} // for
		     Global.mainTab.tbP.selectRow(0);
		} // if
	}
	
	/**
	* Method to remove the flag from all caches in the 
	* cache database. Restore to the state of the filter
	*/
	public void clearSearch(){
		Global.getProfile().selectionChanged = true;
		for(int i = cacheDB.size()-1;i >=0;i--){
			CacheHolder ch=((CacheHolder)cacheDB.get(i));
			ch.is_flaged=false;
			ch.is_filtered=(ch.is_black^Filter.showBlacklisted) ;
		}
		//Global.getProfile().filterActive=Filter.filterActive; //TODO This is a hack. Need to tidy this up
		//Global.getProfile().filterInverted=Filter.filterInverted;
		Global.getProfile().restoreFilter();
	}
}
