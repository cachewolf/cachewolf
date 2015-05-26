/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.database;

import CacheWolf.utils.MutableInteger;
import ewe.util.Comparer;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Map.MapEntry;
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

    /** Stores the CacheHolder objects */
    private Vector vectorDB = new Vector();
    /** Stores the reference of waypoints to index positions (in vectorDB). */
    private Hashtable hashDB = new Hashtable();

    /**
     * Gets the existing MutableInteger object from a given waypoint, or,
     * if not existant, creates a new one, and fills it with the given integer.
     * 
     * @param waypoint
     *            The waypoint whos MutableIntger object is to use
     * @param newValue
     *            The integer value you want to assign to the object
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

    /**
     * Gets the stored CacheHolder object by its position in the Cache List.
     * 
     * @param index
     *            Index of cache
     * @return CacheHolder object with corresponding index
     */
    public CacheHolder get(int index) {
	if (vectorDB.size() > index) {
	    return (CacheHolder) vectorDB.get(index);
	} else
	    return null;
    }

    /**
     * Gets the stored CacheHolder object by its waypoint.<br>
     * If no such Cache exists, null is returned.<br>
     * 
     * @param waypoint
     *            Waypoint of cache we want
     * @return CacheHolder object with corresponding waypoint
     */
    public CacheHolder get(String waypoint) {
	MutableInteger obj = (MutableInteger) hashDB.get(waypoint);
	if (obj == null)
	    return null;
	// if there is a hash, there is also a vector
	return (CacheHolder) vectorDB.get(obj.getInt());
    }

    /**
     * Gets the index of the cache with a given waypoint.
     * 
     * @param waypoint
     *            Waypoint of cache we want
     * @return Index of CacheHolder object in cache list.
     */
    public int getIndex(String waypoint) {
	MutableInteger obj = (MutableInteger) hashDB.get(waypoint);
	int result;
	if (obj == null) {
	    result = -1;
	} else {
	    result = obj.getInt();
	}
	return result;
    }

    /**
     * Gets the index of a certain CacheHolder object.
     * 
     * @param ch
     *            CacheHolder object
     * @return Index of CacheHolder object in cache list.
     */
    public int getIndex(CacheHolder ch) {
	return getIndex(ch.getCode());
    }

    /**
     * Sets a CacheHolder object at a certain position in the cache list. If this position
     * is already occupied by a cache object, this one discarded.
     * 
     * @param index
     *            Index where to set object
     * @param ch
     *            CacheHolder object to set
     */
    public void set(int index, CacheHolder ch) {
	CacheHolder oldObj = (CacheHolder) vectorDB.get(index);
	vectorDB.set(index, ch);
	hashDB.put(ch.getCode(), this.getIntObj(ch.getCode(), index));
	if (oldObj != null && !oldObj.getCode().equals(oldObj.getCode())) {
	    hashDB.remove(oldObj.getCode());
	}
    }

    /**
     * Append a CacheHolder object at the end of the cache list. If a cache with same waypoint
     * is already existant in the cache list, then the old object is overwritten and the new object
     * is positioned at the position of the old object (so in this case <code>add</code> acts like <code>set</code>.
     * 
     * @param ch
     *            CacheHolder object to append
     */
    public void add(CacheHolder ch) {
	if (this.getIndex(ch) > 0) {
	    this.set(this.getIndex(ch), ch);
	} else {
	    vectorDB.add(ch);
	    hashDB.put(ch.getCode(), this.getIntObj(ch.getCode(), vectorDB.size() - 1));
	}
    }

    /**
     * The number of caches in the cache list.
     * 
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

    /**
     * Same as <br>
     * <code>clear();<br>addAll(cachesA);<br>addAll(cachesB);<br></code>but optimized
     * to reduce object creation. <br>
     * Thus builds cacheDB out of caches of vectors cachesA and cachesB, added in this order.
     * 
     * @param cachesA
     *            First Vector of CacheHolder object to add to CacheDB
     * @param cachesB
     *            Second Vector of CacheHolder object to add to CacheDB
     */
    public void rebuild(Vector cachesA, Vector cachesB) {
	int vectorSize = vectorDB.size();
	int cachesAsize = 0;
	int cachesBsize = 0;
	// First negate all hashtable position values, to distinguish the old from the new values
	Iterator iter = hashDB.entries();
	while (iter.hasNext()) {
	    MutableInteger mInt = (MutableInteger) ((MapEntry) iter.next()).getValue();
	    mInt.setInt(-mInt.getInt());
	}
	// Then set all vector elements at the proper position
	for (int abc = 1; abc <= 2; abc++) {
	    Vector cachesAB = null;
	    int offset = 0;
	    if (abc == 1) {
		cachesAB = cachesA;
		if (cachesA != null)
		    cachesAsize = cachesA.size();
	    } else {
		cachesAB = cachesB;
		if (cachesA != null)
		    offset = cachesA.size();
		if (cachesB != null)
		    cachesBsize = cachesB.size();
	    }
	    if (cachesAB == null)
		continue;
	    for (int i = offset; i < cachesAB.size() + offset; i++) {
		CacheHolder ch = (CacheHolder) cachesAB.get(i - offset);
		if (i < vectorSize) {
		    vectorDB.set(i, ch);
		} else {
		    vectorDB.add(ch);
		}
		hashDB.put(ch.getCode(), this.getIntObj(ch.getCode(), i));
	    }
	}
	// If there are more elements in vectorDB than in the sum of sizes of cachesA and cachesB
	// then the rest has to be deleted.
	for (int i = vectorDB.size() - 1; i >= cachesAsize + cachesBsize; i--) {
	    vectorDB.del(i);
	}
	// Now delete any element from hashDB which still has a negative position value
	Vector wpToDelete = null;
	MapEntry me = null;
	iter = hashDB.entries();
	while (iter.hasNext()) {
	    me = (MapEntry) iter.next();
	    MutableInteger mInt = (MutableInteger) me.getValue();
	    if (mInt.getInt() < 0) {
		if (wpToDelete == null)
		    wpToDelete = new Vector();
		String wp = (String) me.getKey();
		wpToDelete.add(wp);
	    }
	}
	if (wpToDelete != null) {
	    for (int i = 0; i < wpToDelete.size(); i++) {
		String wp = (String) wpToDelete.get(i);
		hashDB.remove(wp);
	    }
	}
    }

    /**
     * Removes a CacheHolder object at the specified position in the cache list. The following
     * elements are renumbered.<br>
     * Additionally the cache details are unloaded and saved to file, if necessary.
     * 
     * @param index
     *            The index of element to remove
     */
    public void removeElementAt(int index) {
	CacheHolder ch = this.get(index);
	ch.releaseCacheDetails();
	vectorDB.removeElementAt(index);
	hashDB.remove(ch.getCode());
	// When one element has been removed, we have to update the index
	// references in the hashtable, as the indexes of waypoints changed.
	for (int i = index; i < vectorDB.size(); i++) {
	    CacheHolder ch2 = this.get(i);
	    hashDB.put(ch2.getCode(), this.getIntObj(ch2.getCode(), i));
	}
    }

    /**
     * Sorts the caches in the list
     * 
     * @param comparer
     *            Comparer object
     * @param descending
     *            descending or not
     */
    public void sort(Comparer comparer, boolean descending) {
	vectorDB.sort(comparer, descending);
	// When elements have been sorted we have to update the index
	// references in the hashtable, as the indexes of waypoints changed.
	for (int i = 0; i < vectorDB.size(); i++) {
	    CacheHolder ch = this.get(i);
	    hashDB.put(ch.getCode(), this.getIntObj(ch.getCode(), i));
	}
    }

    /**
     * Adds the caches of one CacheDB to current one. Caches are appended at the end.
     * 
     * @param caches
     *            CacheDB to append
     */
    public void addAll(CacheDB caches) {
	addAll(caches.vectorDB);
    }

    /**
     * Adds a Vector of CacheHolder objects to current database. Caches are appended at the end.
     * 
     * @param caches
     *            Vector of caches to append
     */
    public void addAll(Vector caches) {
	int oldSize = vectorDB.size();
	vectorDB.addAll(caches);
	for (int i = 0; i < caches.size(); i++) {
	    int pos;
	    CacheHolder currCache = (CacheHolder) vectorDB.get(i + oldSize);
	    pos = i + oldSize;
	    hashDB.put(currCache.getCode(), this.getIntObj(currCache.getCode(), pos));
	}
    }

    /**
     * Returns the number of currently visible waypoints. <br>
     * As this number is not only dependent from
     * CacheHolder properties, but also from the state of the filter and so on, the determination
     * of this number always requires a count through all waypoints. So use with caution.
     * 
     * @return Number of currently visible waypoints.
     */
    public int countVisible() {
	int c = 0;
	for (int i = 0; i < vectorDB.size(); i++) {
	    if (this.get(i).isVisible())
		c++;
	}
	return c;
    }

}
