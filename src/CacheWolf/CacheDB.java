package CacheWolf;

import utils.MutableInteger;
import ewe.util.Comparer;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * @author torsti
 *
 */
/**
 * @author torsti
 *
 */
public class CacheDB {
	
	/**
	 * Stores the CacheHolder objects
	 */
	private Vector vectorDB = new Vector();
	/**
	 * Stores the reference of waypoints to index positions (in vectorDB).
	 */
	private Hashtable hashDB = new Hashtable();
	
	/** Gets the existing MutableInteger object from a given waypoint, or,
	 * if not existant, creates a new one, and fills it with the given integer. 
	 * @param waypoint The waypoint whos MutableIntger object is to use
	 * @param newValue The integer value you want to assign to the object
	 * @return The newly created or reused and freshly assigned object
	 */
	private MutableInteger getIntObj(String waypoint, int newValue) {
		MutableInteger obj = (MutableInteger) hashDB.get(waypoint);
		if (obj == null) {
			obj = new MutableInteger();			
		}
		obj.setInt(newValue);
		return obj;
	}
	
	/** Gets the stored CacheHolder object by its position in the Cache List.
	 * @param index Index of cache 
	 * @return CacheHolder object with corresponding index
	 */
	public CacheHolder get(int index) {
		return (CacheHolder) vectorDB.get(index);
	}

	/** Gets the stored CacheHolder object by its waypoint. If no such Cache exists,
	 * null is returned.
	 * @param waypoint Waypoint of cache we want
	 * @return CacheHolder object with corresponding waypoint
	 */
	public CacheHolder get(String waypoint){
		int idx = this.getIndex(waypoint);
		if (idx < 0) return null;
		return this.get(idx);
	}
	
	/** Gets the index of the cache with a given waypoint. 
	 * @param waypoint Waypoint of cache we want
	 * @return Index of CacheHolder object in cache list.
	 */
	public int getIndex(String waypoint) {
		Object obj = hashDB.get(waypoint);
		int result;
		if (obj == null) {
			result = -1;
		} else {
			result = ((MutableInteger)obj).getInt();
		}
		return result;
	}

	/** Gets the index of a certain CacheHolder object. 
	 * @param ch CacheHolder object
	 * @return Index of CacheHolder object in cache list.
	 */
	public int getIndex(CacheHolder ch) {
		return getIndex(ch.getWayPoint());
	}

	/** Sets a CacheHolder object at a certain position in the cache list. If this position 
	 * is already occupied by a cache object, this one discarded.
	 * @param index Index where to set object
	 * @param ch CacheHolder object to set
	 */
	public void set(int index, CacheHolder ch) {
		CacheHolder oldObj = (CacheHolder) vectorDB.get(index);
	    vectorDB.set(index, ch);
	    hashDB.put(ch.getWayPoint(), this.getIntObj(ch.getWayPoint(), index));
	    if (oldObj!=null && ! oldObj.getWayPoint().equals(oldObj.getWayPoint())) {
	    	hashDB.remove(oldObj.getWayPoint());
	    }
    }

	/** Append a CacheHolder object at the end of the cache list. If a cache with same waypoint
	 * is already existant in the cache list, then the old object is overwritten and the new object
	 * is positioned at the position of the old object (so in this case <code>add</code> acts like
	 * <code>set</code>.
	 * @param ch CacheHolder object to append
	 */
	public void add(CacheHolder ch) {
		if (this.getIndex(ch)>0) {
			this.set(this.getIndex(ch), ch);
		} else {
			vectorDB.add(ch);
			hashDB.put(ch.getWayPoint(), this.getIntObj(ch.getWayPoint(), vectorDB.size()-1));
		}
    }

	/** The number of caches in the cache list.
	 * @return number
	 */
	public int size() {
	    return vectorDB.size();
    }

	/**
	 * Removes all cache objects from the list.
	 */
	public void clear() {
	    hashDB.clear();
	    vectorDB.clear();
    }

	/** Removes a CacheHolder object at the specified position in the cache list. The following
	 * elements are renumbered.<br>
	 * Additionally the cache details are unloaded and saved to file, if necessary.
	 * @param index The index of element to remove
	 */
	public void removeElementAt(int index) {
		CacheHolder ch = this.get(index);
		ch.releaseCacheDetails();
	    vectorDB.removeElementAt(index);
	    hashDB.remove(ch.getWayPoint());
	    // When one element has been removed, we have to update the index
	    // references in the hashtable, as the indexes of waypoints changed.
	    for (int i=index; i<vectorDB.size(); i++) {
	    	CacheHolder ch2 = this.get(i);
	    	hashDB.put(ch2.getWayPoint(), this.getIntObj(ch2.getWayPoint(), i));
	    }
    }

	/**Sorts the caches in the list
	 * @param comparer Comparer object
	 * @param descending descending or not
	 */
	public void sort(Comparer comparer, boolean descending) {
	    vectorDB.sort(comparer, descending);
	    // When elements have been sorted we have to update the index
	    // references in the hashtable, as the indexes of waypoints changed.
	    for (int i=0; i<vectorDB.size(); i++) {
	    	CacheHolder ch = this.get(i);
	    	hashDB.put(ch.getWayPoint(), this.getIntObj(ch.getWayPoint(), i));
	    }
    }

	/** Adds the caches of one CacheDB to current one. Caches are appended at the end.
	 * @param caches CacheDB to append
	 */
	public void addAll(CacheDB caches) {
		addAll(caches.vectorDB);
	}

	/** Adds a Vector of CacheHolder objects to current database. Caches are appended at the end.
	 * @param caches Vector of caches to append
	 */
	public void addAll(Vector caches) {
		int oldSize = vectorDB.size();
		vectorDB.addAll(caches);
		for (int i=0; i<caches.size(); i++) {
			int pos;
			CacheHolder currCache = (CacheHolder) vectorDB.get(i+oldSize);
			pos = i+oldSize;
			hashDB.put(currCache.getWayPoint(), this.getIntObj(currCache.getWayPoint(), pos));
		}
	}
	
}
