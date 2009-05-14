package CacheWolf;

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
	public void search(String searchStr, boolean searchInDescriptionAndNotes){
		if(searchStr.length()>0){
			Global.getProfile().selectionChanged = true;
			searchStr = searchStr.toUpperCase();
			CacheHolder ch;
			int counter = 0;
			if (searchInDescriptionAndNotes) {
				for(int i=0 ; i<cacheDB.size(); i++) {
					if (!cacheDB.get(i).isVisible()) break;
					counter++;
				}
			}
			CWProgressBar cwp = new CWProgressBar(MyLocale.getMsg(219,"Searching..."), 0, counter, searchInDescriptionAndNotes);
			cwp.exec();
			cwp.allowExit(true);
			//Search through complete database
			//Mark finds by setting is_flaged
			//TableModel will be responsible for displaying
			//marked caches.
			for(int i = 0;i < cacheDB.size();i++){
				cwp.setPosition(i);
				ch = cacheDB.get(i);
				if (!ch.isVisible()) break; // Reached end of visible records
				if(ch.getWayPoint().toUpperCase().indexOf(searchStr) <0 && 
				   ch.getCacheName().toUpperCase().indexOf(searchStr) <0 && 
				   ch.getCacheStatus().toUpperCase().indexOf(searchStr)<0 &&
				   (! searchInDescriptionAndNotes || 
					ch.getExistingDetails().LongDescription.toUpperCase().indexOf(searchStr)<0 &&
					ch.getExistingDetails().getCacheNotes().toUpperCase().indexOf(searchStr)<0)){
					ch.is_flaged = false;
				} else
					ch.is_flaged=true;
				if (cwp.isClosed()) break;
			} // for
			cwp.exit(0);
			Global.getProfile().setShowSearchResult(true);
		    Global.mainTab.tbP.selectRow(0);
		} // if
	}
	
	/**
	* Method to remove the flag from all caches in the 
	* cache database.
	*/
	public void clearSearch(){
	    Profile profile = Global.getProfile();
		profile.selectionChanged = true;
		profile.setShowSearchResult(false);
		for(int i = cacheDB.size()-1;i >=0;i--){
			cacheDB.get(i).is_flaged=false;
		}
	}
}
