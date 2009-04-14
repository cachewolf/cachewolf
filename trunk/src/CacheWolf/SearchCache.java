package CacheWolf;
import ewe.util.*;

/**
*	A class to perform a search on the cache database.
*	The searchstr is searched for in the waypoint
*	and the name of the cache.
*	A method is also provided to erase the search results.
*/
public class SearchCache {

	CacheDB cacheDB;
	
	public SearchCache(CacheDB DB){
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
				ch = cacheDB.get(i);
				if (ch.is_filtered()) break; // Reached end of visible records
				if(ch.getWayPoint().toUpperCase().indexOf(searchStr) <0 && 
				   ch.getCacheName().toUpperCase().indexOf(searchStr) <0 && 
				   ch.getCacheStatus().toUpperCase().indexOf(searchStr)<0){
					ch.is_flaged = false;
					ch.setFiltered(true);
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
	    Profile profile = Global.getProfile();
		profile.selectionChanged = true;
		boolean filter_marked_only = profile.getFilterActive() == Filter.FILTER_MARKED_ONLY;
		for(int i = cacheDB.size()-1;i >=0;i--){
			CacheHolder ch=cacheDB.get(i);
			ch.is_flaged=false;
			if (filter_marked_only) ch.setFiltered((ch.is_black()^Global.getProfile().showBlacklisted())) ;
		}
		//Global.getProfile().filterActive=Filter.filterActive; //TODO This is a hack. Need to tidy this up
		//Global.getProfile().filterInverted=Filter.filterInverted;
		Global.getProfile().restoreFilter();
	}
}
