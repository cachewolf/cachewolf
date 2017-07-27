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

import com.stevesoft.ewe_pat.Regex;

import CacheWolf.Filter;
import CacheWolf.MainForm;
import CacheWolf.MyTableModel;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.utils.Common;
import CacheWolf.utils.Metrics;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.util.Vector;

/**
 * A class to hold information on a cache.<br>
 * Not all attributes are filled at once. You will have to look at other classes and methods to get more information.
 * 
 */
public class CacheHolder {
    private static final String EMPTY = "";
    private static final String NOBEARING = "?";

    private static final byte ISLOGUPDATED = 1;
    private static final byte ISUPDATED = 2;
    private static final byte ISNEW = 3;
    private static final byte ISINCOMPLETE = 4;

    /** The coordinates of the waypoint */
    private CWPoint wpt = new CWPoint();
    /** The date when the cache was hidden in format yyyy-mm-dd */
    private String hidden = EMPTY;
    /** The code of the waypoint, beginning with 2 chars like GC or OC or CW (or any for Addis ) */
    private String code = EMPTY;
    /** The name of the cache */
    private String name = EMPTY;
    /** Byte 3: The cache type (@see CacheType for translation table) */
    private byte type;
    /** The alias of the owner */
    private String owner = EMPTY;
    /** Byte 1: The difficulty of the cache from 10 to 50 in 5 incements */
    private byte difficulty = CacheTerrDiff.CW_DT_UNSET;
    /** Byte 2: The terrain rating of the cache from 10 to 50 in 5 incements */
    private byte terrain = CacheTerrDiff.CW_DT_UNSET;
    /** Byte 4: The size of the cache (as per GC cache sizes Micro, Small, ....) */
    private byte size = CacheSize.CW_SIZE_NOTCHOSEN;
    /** status is Found, Not found or a date in format yyyy-mm-dd hh:mm for found date */
    private String status = EMPTY;
    /** attributes */
    private long[] attributesBits = { 0l, 0l, 0l, 0l };

    /** Bit 2: True if the cache is available for searching */
    private boolean isAvailable = true;
    /** Bit 3: True if the cache has been archived */
    private boolean isArchived = false;
    /** Bit 4: True if this cache has travelbugs */
    private boolean hasBugs = false;
    /** Bit 5: True if the cache is blacklisted */
    private boolean isBlack = false;
    /** Bit 6: True if we own this cache */
    private boolean isOwned = false;
    /** Bit 7: True if we have found this cache */
    private boolean isFound = false;
    /** Bit 8: True if the cache is new */
    private boolean isNew = false;
    /** Bit 9: True if the number of logs for this cache has changed */
    private boolean isLogUpdated = false;
    /** Bit 10: True if cache details have changed: longDescription, Hints, ...*/
    private boolean isUpdated = false;
    /** Bit 11: True if the cache description is stored in HTML format */
    private boolean isHTML = true;
    /** Bit 12: True if the cache data is incomplete (e.g. an error occurred during spidering */
    private boolean isIncomplete = false;
    /** Bit 13: True if a note is entered for the cache */
    private boolean hasNote = false;
    /** Bit 14: True if cache has solver entry */
    private boolean hasSolver = false;
    /**  Bit 15:*/
    private boolean isPMCache = false;
    /**  Bit 16:*/
    private boolean isSolved = false;

    /** If this is true, the cache has been filtered (is currently invisible) */
    private boolean isFiltered = false;
    /** True if the cache is part of the results of a search */
    public boolean isFlagged = false;
    /** True if the cache has been selected using the tick box in the list view */
    public boolean isChecked = false;
    /** True if additional waypoints for this cache should be displayed regardless of the filter settings */
    private boolean showAddis = false;

    /** The unique OC cache ID */
    private String idOC = EMPTY;

    /** Byte 5: The number of times this cache has not been found (max. 5) */
    private byte noFindLogs = 0;

    /** Number of recommendations (from the opencaching logs) */
    private int numRecommended = 0;
    /** Number of Founds since start of recommendations system */
    private int numFoundsSinceRecommendation = 0;

    /** List of additional waypoints associated with this waypoint */
    public Vector addiWpts = new Vector();

    /** If this is an additional waypoint, this links back to the main waypoint */
    public CacheHolder mainCache;

    public CacheHolderDetail details = null;
    /** The date this cache was last synced with OC in format yyyyMMddHHmmss */
    private String lastSync = EMPTY;

    /** CacheHolder.ISINCOMPLETE, CacheHolder.ISNEW, CacheHolder.ISUPDATED, CacheHolder.ISLOGUPDATED */
    private int modificationLevel = 0;
    private IconAndText modificationIcon = null;

    /**
     * When sorting the cacheDB this field is used.<br>
     * The relevant field is copied here.<br>
     * The sort is always done on this field to speed up the sorting process<br>
     */
    public String sort;

    /** The distance from the centre in km */
    public double kilom = -1;
    public double lastKilom = -2;
    public int lastMetric = -1;
    public String lastDistance = "";
    /** The bearing N, NNE, NE, ENE ... from the current centre to this point */
    private String bearing = NOBEARING;
    /** The angle (0=North, 180=South) from the current centre to this point */
    public double degrees = 0;

    private final static String SOLVED = MyLocale.getMsg(362, "Solved");

    public CacheHolder() {
    }

    public CacheHolder(String code) {
	this.code = code;
	type = CacheType.CW_TYPE_ERROR;
    }

    /** only for reading from index.xml */
    public CacheHolder(String cache, int version) {
	int start, end;
	try {
	    if (version == 3 || version == 4) {
		start = cache.indexOf('"') + 1;
		end = cache.indexOf('"', start);
		this.name = SafeXML.html2iso8859s1(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.owner = SafeXML.html2iso8859s1(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.wpt.latDec = Common.parseDouble(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.wpt.lonDec = Common.parseDouble(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.hidden = cache.substring(start, end);

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.code = SafeXML.html2iso8859s1(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.status = cache.substring(start, end);

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.idOC = cache.substring(start, end);

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.lastSync = cache.substring(start, end);

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.numRecommended = Convert.toInt(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.numFoundsSinceRecommendation = Convert.toInt(cache.substring(start, end));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.attributesBits[0] = (Convert.parseLong(cache.substring(start, end)));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.attributesBits[2] = (Convert.parseLong(cache.substring(start, end)));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.long2boolFields(Convert.parseLong(cache.substring(start, end)));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.long2byteFields(Convert.parseLong(cache.substring(start, end)));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.attributesBits[1] = (Convert.parseLong(cache.substring(start, end)));

		start = cache.indexOf('"', end + 1) + 1;
		end = cache.indexOf('"', start);
		this.attributesBits[3] = (Convert.parseLong(cache.substring(start, end)));
		// move from Status to Bit
		if (this.status.indexOf("PM") > -1) {
		    this.isPMCache = true;
		    this.status = new Regex(",?\\s*PM\\s*,?", "").replaceFirst(this.status);
		}
		if (this.status.indexOf(SOLVED) > -1) {
		    this.isSolved = true;
		    this.status = new Regex(",?\\s*" + SOLVED + "\\s*,?", "").replaceFirst(this.status);
		}
	    }
	    if (version < Profile.CURRENTFILEFORMAT) {
		// forceload of details, creates waypoint.xml if missing
		details = getDetails();
		// make sure details get (re)written in new format
		details.hasUnsavedChanges = true;
		// update information on notes and solver info
		setHasNote(!details.getCacheNotes().equals(""));
		setHasSolver(!details.getSolver().equals(""));
	    }
	} catch (Exception ex) {
	    // Preferences.itself().log("Ignored Exception in CacheHolder()", ex, true);
	}
    }

    // quick debug info
    public String toString() {
	return this.code;
    }

    /** The coordinates of the waypoint */
    public CWPoint getWpt() {
	return this.wpt;
    }

    /**
     * The coordinates of the waypoint
     * @param wpt
     */
    public void setWpt(CoordinatePoint wpt) {
	if (!this.wpt.equals(wpt)) {
	    setUpdated(true);
	    this.wpt.set(wpt);
	}
    }

    /** The date when the cache was hidden in format yyyy-mm-dd */
    public String getHidden() {
	return this.hidden;
    }

    /**
     * The date when the cache was hidden in format yyyy-mm-dd
     * @param hidden
     */
    public void setHidden(String hidden) {
	MainForm.profile.notifyUnsavedChanges(!hidden.equals(this.hidden));
	this.hidden = hidden;
    }

    /** The code of the waypoint, beginning with 2 chars like GC or OC or CW (or any for Addis ) */
    public String getCode() {
	return this.code;
    }

    /**
     * The code of the waypoint, beginning with 2 chars like GC or OC or CW (or any for Addis )
     * @param code
     * @return true if changed, false if equal 
     */
    public boolean setCode(String code) {
	boolean ret = !code.equals(this.code);
	if (ret) {
	    MainForm.profile.notifyUnsavedChanges(ret);
	    this.code = code;
	}
	return ret;
    }

    /** The name of the cache */
    public String getName() {
	return this.name;
    }

    /**
     * The name of the cache
     * @param name
     * @return true if changed, false if equal 
     */
    public boolean setName(String name) {
	boolean ret = !name.equals(this.name);
	if (ret) {
	    MainForm.profile.notifyUnsavedChanges(ret);
	    this.name = name;
	}
	return ret;
    }

    /**Byte 3: The cache type as byte (@see CacheType for translation table) */
    public byte getType() {
	return this.type;
    }

    /**
     * Byte 3: Sets the type of the cache. 
     * As the cache type values are int for the rest of CacheWolf and byte internally of CacheHolder, some conversion has to be done.
     * 
     * @param type
     *            cacheType
     */
    public void setType(byte type) {
	MainForm.profile.notifyUnsavedChanges(this.type != type);
	this.type = type;
    }

    /** The alias of the owner */
    public String getOwner() {
	return this.owner;
    }

    /**
     * The alias of the owner
     * @param owner
     */
    public void setOwner(String owner) {
	if (!this.owner.equals(owner)) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.owner = owner;
	    this.isOwned = (this.owner.length() > 0) && (this.owner.equalsIgnoreCase(Preferences.itself().myAlias) || this.owner.equalsIgnoreCase(Preferences.itself().myAlias2));
	}
    }

    /**Byte 1: The difficulty of the cache from 10 to 50 in 5 incements */
    public byte getDifficulty() {
	return this.difficulty;
    }

    /**
     *Byte 1: The difficulty of the cache from 10 to 50 in 5 incements
     * @param difficulty
     */
    public void setDifficulty(byte difficulty) {
	MainForm.profile.notifyUnsavedChanges(difficulty != this.difficulty);
	this.difficulty = difficulty;
    }

    /**Byte 2: The terrain rating of the cache from 10 to 50 in 5 incements */
    public byte getTerrain() {
	return terrain;
    }

    /**
     *Byte 2: The terrain rating of the cache from 10 to 50 in 5 incements
     * @param terrain
     */
    public void setTerrain(byte terrain) {
	MainForm.profile.notifyUnsavedChanges(terrain != this.terrain);
	this.terrain = terrain;
    }

    /**Byte 4: The size of the cache (as per GC cache sizes Micro, Small, ....) */
    public byte getSize() {
	return size;
    }

    /**
     *Byte 4: The size of the cache (as per GC cache sizes Micro, Small, ....)
     * @param size
     */
    public void setSize(byte size) {
	MainForm.profile.notifyUnsavedChanges(size != this.size);
	this.size = size;
    }

    /** Cachestatus is Found, Not found or a date in format yyyy-mm-dd hh:mm for found date */
    public String getStatus() {
	return this.status;
    }

    /**
     * Cachestatus is Found, Not found or a date in format yyyy-mm-dd hh:mm for found date
     * @param status
     */
    public void setStatus(String status) {
	if (!status.equals(this.status)) {
	    this.status = status.trim();
	    MainForm.profile.notifyUnsavedChanges(true);
	    if ((this.getType() == CacheType.CW_TYPE_FINAL) && (this.mainCache != null)) {
		this.mainCache.setStatus(this.status);
		// change the addi's in setFound
	    }
	}
    }

    /**
     * The method takes into account blacklist, filters, search results - everything that determines if a cache is visible in the list or not.<br>
     * 
     * @return
     *  <code>true</code> if the waypoint should appear in the cache list.<br>
     */
    public boolean isVisible() {
	int filter = MainForm.profile.getFilterActive();
	boolean noShow = MainForm.profile.showBlacklisted() != this.isBlack();
	noShow = noShow || MainForm.profile.showSearchResult() && !this.isFlagged;
	noShow = noShow || ((filter == Filter.FILTER_ACTIVE || filter == Filter.FILTER_MARKED_ONLY) && this.isFiltered() ^ MainForm.profile.isFilterInverted());
	noShow = noShow || (filter == Filter.FILTER_CACHELIST) && !MainForm.itself.contains(this.code); // only from CacheTour
	boolean showAddi = this.showAddis() && this.mainCache != null && this.mainCache.isVisible();
	noShow = noShow && !showAddi;
	return !noShow;
    }

    /**
     * <b><u>Important</u></b>: This flag no longer indicates if a cache is visible in the list.<br>
     * The new method for deciding if a cache is visible or not is <code>isVisible()</code>.<br>
     * Instead, it now <u>only</u> flags if the cache is filtered out by filter criteria.<br>
     * <br>
     * This property is affected by the following features:
     * <ul>
     * <li>"Defining and applying" a filter</li>
     * <li>Filtering out checked or unchecked caches</li>
     * </ul>
     * It is <u>not</u> affected by:
     * <ul>
     * <li>Inverting a filter</li>
     * <li>Removing a filter</li>
     * <li>Applying a filter</li>
     * <li>Applying a cache tour filter</li>
     * <li>Switching between normal view and blacklist view</li>
     * <li>Performing searches</li>
     * <li>Anything else that isn't directly connected to filters in it's proper sense.</li>
     * </ul>
     * 
     * @return <code>True</code> if filter criteria are matched
     */
    public boolean isFiltered() {
	return this.isFiltered;
    }

    /**
     * 
     * @param isFiltered
     */
    public void setFiltered(boolean isFiltered) {
	MainForm.profile.notifyUnsavedChanges(isFiltered != this.isFiltered);
	this.isFiltered = isFiltered;
    }

    /** Bit 2: True if the cache is available for searching */
    public boolean isAvailable() {
	return this.isAvailable;
    }

    /**
     * Bit 2: True if the cache is available for searching
     * @param isAvailable
     */
    public void setAvailable(boolean isAvailable) {
	MainForm.profile.notifyUnsavedChanges(isAvailable != this.isAvailable);
	this.isAvailable = isAvailable;
	if (this.isAvailable) {
	    this.isArchived = false;
	}
    }

    /** Bit 3: True if the cache has been archived */
    public boolean isArchived() {
	return this.isArchived;
    }

    /**
     * Bit 3: True if the cache has been archived
     * @param isArchived
     */
    public void setArchived(boolean isArchived) {
	MainForm.profile.notifyUnsavedChanges(isArchived != this.isArchived);
	this.isArchived = isArchived;
	if (this.isArchived) {
	    this.isAvailable = false;
	}
    }

    /** Bit 4: True if this cache has travelbugs */
    public boolean hasBugs() {
	return this.hasBugs;
    }

    /**
     * Bit 4: True if this cache has travelbugs
     * @param b
     */
    public void hasBugs(boolean b) {
	if (b != this.hasBugs) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.hasBugs = b;
	}
    }

    /**
     * Bit 5: True if the cache is blacklisted<br>
     * Do not use this method to check if the cache should be displayed.<br> 
     * Use <code>isVisible()</code> for this, which already does this (and other) checks.<br>
     * Only use this method if you really want to inform yourself about the black status of the cache!<br>
     * 
     * @return <code>true</code> if the black status of the cache is set.
     */
    public boolean isBlack() {
	return this.isBlack;
    }

    /**
     * Bit 5: True if the cache is blacklisted<br>
     * @param b
     */
    public void setBlack(boolean b) {
	if (b != this.isBlack) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isBlack = b;
	}
    }

    /** Bit 6: True if we own this cache */
    public boolean isOwned() {
	return this.isOwned;
    }

    /**
     * Bit 6: True if we own this cache
     * @param isOwned
     */
    public void setOwned(boolean isOwned) {
	if (this.isOwned != isOwned) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isOwned = isOwned;
	    if (isOwned) {
		if (this.owner.length() == 0) {
		    this.isOwned = false;
		} else {
		    // owner ist nicht myAlias oder myAlias2
		    if (!(this.owner.equalsIgnoreCase(Preferences.itself().myAlias) || this.owner.equalsIgnoreCase(Preferences.itself().myAlias2))) {
			this.isOwned = false;
		    }
		}
	    }
	}
    }

    /** Bit 7: True if we have found this cache */
    public boolean isFound() {
	return this.isFound;
    }

    /**
     * Bit 7: True if we have found this cache.<br>
     * done in setCacheStatus this.mainCache.setCacheStatus(this.getCacheStatus());<br>
     * so setFound should be called after setCacheStatus<br>
     * @param isFound
     */
    public void setFound(boolean isFound) {
	if (isFound != this.isFound) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isFound = isFound;
	    if ((this.getType() == CacheType.CW_TYPE_FINAL) && (this.mainCache != null)) {
		this.mainCache.setFound(isFound);
		if (isFound)
		    this.mainCache.setAttributesFromMainCacheToAddiWpts();
	    }
	}
    }

    /** Bit 8: True if the cache is new */
    public boolean isNew() {
	return isNew;
    }

    /**
     * Bit 8: True if the cache is new
     * @param isNew
     */
    public void setNew(boolean isNew) {
	if (isNew != this.isNew) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isNew = isNew;
	}
    }

    /** Bit 9: True if the number of logs for this cache has changed */
    public boolean isLogUpdated() {
	return this.isLogUpdated;
    }

    /**
     * Bit 9: True if the number of logs for this cache has changed
     * @param isLogUpdated
     */
    public void setLogUpdated(boolean isLogUpdated) {
	MainForm.profile.notifyUnsavedChanges(isLogUpdated != this.isLogUpdated);
	this.isLogUpdated = isLogUpdated;
    }

    /** Bit 10: True if cache details have changed: longDescription, Hints, ...*/
    public boolean isUpdated() {
	return this.isUpdated;
    }

    /**
     * Bit 10: True if cache details have changed: longDescription, Hints, ...
     * @param isUpdated
     */
    public void setUpdated(boolean isUpdated) {
	MainForm.profile.notifyUnsavedChanges(isUpdated != this.isUpdated);
	this.isUpdated = isUpdated;
    }

    /** Bit 11: True if the cache description is stored in HTML format */
    public boolean isHTML() {
	return this.isHTML;
    }

    /**
     * Bit 11: True if the cache description is stored in HTML format
     * @param b
     */
    public void isHTML(boolean b) {
	if (b != this.isHTML) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isHTML = b;
	}
    }

    /** Bit 12: True if the cache data is incomplete (e.g. an error occurred during spidering */
    public boolean isIncomplete() {
	return this.isIncomplete;
    }

    /**
     * Bit 12: True if the cache data is incomplete (e.g. an error occurred during spidering
     * @param isIncomplete
     */
    public void setIncomplete(boolean isIncomplete) {
	MainForm.profile.notifyUnsavedChanges(isIncomplete != this.isIncomplete);
	this.isIncomplete = isIncomplete;
    }

    /** Bit 13: True if a note is entered for the cache */
    public boolean hasNote() {
	return this.hasNote;
    }

    /**
     * Bit 13: True if a note is entered for the cache
     * @param b
     */
    public void setHasNote(boolean b) {
	if (b != this.hasNote) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.hasNote = b;
	}
    }

    /** Bit 14: True if cache has solver entry */
    public boolean hasSolver() {
	return this.hasSolver;
    }

    /**
     * Bit 14: True if cache has solver entry
     * @param b
     */
    public void setHasSolver(boolean b) {
	if (b != this.hasSolver) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.hasSolver = b;
	}
    }

    /**  Bit 15:*/
    public boolean isPremiumCache() {
	return this.isPMCache;
    }

    /**
     * Bit 15:
     * @param b
     */
    public void setIsPremiumCache(boolean b) {
	if (b != this.isPMCache) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isPMCache = b;
	}
    }

    /**  Bit 16:*/
    public boolean isSolved() {
	return this.isSolved;
    }

    /**
     * Bit 16: 
     * @param b
     */
    public void setIsSolved(boolean b) {
	if (b != this.isSolved) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.isSolved = b;
	}
    }

    /** attributes */
    public long[] getAttributesBits() {
	return this.attributesBits;
    }

    /**
     * attributes
     * @param attributesBits
     */
    public void setAttribsAsBits(long[] attributesBits) {
	MainForm.profile.notifyUnsavedChanges(attributesBits != this.attributesBits);
	this.attributesBits = attributesBits;
    }

    /** return true if waypoint is an additional waypoint of a cache */
    public boolean isAddiWpt() {
	return CacheType.isAddiWpt(type);
    }

    /** return true if waypoint is a custom waypoint */
    public boolean isCustomWpt() {
	return CacheType.isCustomWpt(type);
    }

    /** return true if waypoint is a cache main waypoint */
    public boolean isCacheWpt() {
	return CacheType.isCacheWpt(type);
    }

    /** return true if the waypoint has one or more additional waypoints */
    public boolean hasAddiWpt() {
	return addiWpts.getCount() > 0;
    }

    public boolean isOC() {
	return OC.isOC(this.code);
    }

    public boolean isGC() {
	return this.code.substring(0, 2).equalsIgnoreCase("GC");
    }

    /**
     * Returns the distance in formatted output. Using kilometers when metric system is active, using miles when imperial system is active.
     * 
     * @return The current distance.
     */
    public String getDistance() {
	String result = null;
	String newUnit = null;

	if (this.kilom == this.lastKilom && Preferences.itself().metricSystem == this.lastMetric) {
	    result = this.lastDistance;
	} else {
	    if (this.kilom >= 0) {
		double newValue = 0;
		switch (Preferences.itself().metricSystem) {
		case Metrics.IMPERIAL:
		    newValue = Metrics.convertUnit(this.kilom, Metrics.KILOMETER, Metrics.MILES);
		    newUnit = Metrics.getUnit(Metrics.MILES);
		    break;
		case Metrics.METRIC:
		default:
		    newValue = this.kilom;
		    newUnit = Metrics.getUnit(Metrics.KILOMETER);
		    break;
		}
		result = MyLocale.formatDouble(newValue, "0.00") + " " + newUnit;
	    } else {
		result = "? " + (Preferences.itself().metricSystem == Metrics.IMPERIAL ? Metrics.getUnit(Metrics.MILES) : Metrics.getUnit(Metrics.KILOMETER));
	    }
	    // Caching values, so reevaluation is only done when really needed
	    this.lastKilom = this.kilom;
	    this.lastMetric = Preferences.itself().metricSystem;
	    this.lastDistance = result;
	}
	return result;
    }

    /**
     * Updates Cache information with information provided by cache given as argument. This is used to update the cache with the information retrieved from files or web: The argument cache is the one that is filled with the read information,
     * <code>this</code> is the cache that is already in the database and subject to update.
     * 
     * @param ch
     *            The cache who's information is updating the current one
     * @param overwrite
     *            If <code>true</code>, then <i>status</i>, <i>isFound</i> and <i>position</i> is updated, otherwise not.
     */
    public void update(CacheHolder ch) {
	updateOwnLog(ch);
	this.setNumFoundsSinceRecommendation(ch.getNumFoundsSinceRecommendation());
	this.setNumRecommended(ch.getNumRecommended());
	this.setIsPremiumCache(ch.isPMCache);

	// Don't overwrite valid coordinates with invalid ones
	if (ch.getWpt().isValid() || !this.wpt.isValid()) {
	    if (!this.isSolved) {
		this.wpt = ch.getWpt();
	    }
	}
	// the new from GC if coords are changed there
	if (ch.isSolved) {
	    this.setIsSolved(ch.isSolved);
	}
	this.setCode(ch.getCode());
	this.setName(ch.getName());
	this.setOwner(ch.getOwner());
	this.setHidden(ch.getHidden());
	this.setSize(ch.getSize());
	this.kilom = ch.kilom;
	this.bearing = ch.bearing;
	this.degrees = ch.degrees;
	this.setDifficulty(ch.getDifficulty());
	this.setTerrain(ch.getTerrain());
	this.setType(ch.getType());
	this.setArchived(ch.isArchived());
	this.setAvailable(ch.isAvailable());
	this.setOwned(ch.isOwned());
	this.setFiltered(ch.isFiltered());
	this.setIncomplete(ch.isIncomplete());
	this.addiWpts = ch.addiWpts;
	this.mainCache = ch.mainCache;
	this.setIdOC(ch.getIdOC());
	this.setNoFindLogs(ch.getNoFindLogs());
	this.hasBugs(ch.hasBugs());
	this.isHTML(ch.isHTML());
	this.sort = ch.sort;
	this.setLastSync(ch.getLastSync());

	this.setAttribsAsBits(ch.getAttributesBits());
	this.getDetails().update(ch.getDetails());
    }

    public void updateOwnLog(CacheHolder ch) {
	ch.setStatus(ch.status.trim());
	if (ch.isFound()) {
	    if (ch.status.length() == 0) {
		// wenn kein Datum drin ist (also z.B. nicht von GC gespidert) 
		ch.setStatus(CacheType.getFoundText(ch.type));
	    }
	}
	/*
	 * Here we have to distinguish several cases: 
	 * this.isFound this.Status      ch.isFound   ch.Status    this.isFound this.Status 
	 * --------------------------------------------------------------------------------
	 *  true        Found            true         Date         =            ch.Status(if not empty ?== Date )
	 *  true        yyyy-mm-dd       true         Date         =            ch.Status(if not empty ?== Date )
	 *  true        yyyy-mm-dd hh:mm true         Date         =            ch.Status(Date)
	 *  false       something        false        something    =            ch.Status(if not empty ?merge somehow ) 
	 *  false       something        true         Date         true         ch.Status(if not empty ?== Date )
	 */
	if (this.isFound) {
	    if (this.status.indexOf(":") < 0) {
		if (ch.getStatus().length() > 0) {
		    // ch.isFound
		    this.setStatus(ch.getStatus());
		}
	    } else {
		if (!Preferences.itself().keepTimeOnUpdate) {
		    if (ch.getStatus().length() > 0) {
			// ch.isFound
			this.setStatus(ch.getStatus());
		    }
		}
	    }
	} else {
	    if (ch.getStatus().length() > 0) {
		this.setStatus(ch.getStatus());
		this.setFound(ch.isFound());
	    }
	}

	if (ch.getDetails().getOwnLog() != null) {
	    this.getDetails().setOwnLog(ch.getDetails().getOwnLog());
	}

    }

    /**
     * Call it only when necessary, it takes time, because all logs must be parsed
     */
    public void calcRecommendationScore() {
	// String pattern = getWayPoint().toUpperCase();
	if (isOC()) {
	    // Calculate recommendation score only when details are already loaded.
	    // When they aren't loaded, then we assume, that there is no change.
	    if (this.detailsLoaded()) {
		CacheHolderDetail chD = getDetails();
		if (chD != null) {
		    chD.CacheLogs.calcRecommendations();
		    setNumFoundsSinceRecommendation(chD.CacheLogs.getFoundsSinceRecommendation());
		    setNumRecommended(chD.CacheLogs.getNumRecommended());
		} else { // cache doesn't have details
		    setNumFoundsSinceRecommendation(-1);
		    setNumRecommended(-1);
		}
	    }
	} else {
	    setNumFoundsSinceRecommendation(-1);
	    // setNumRecommended(-1);
	}
    }

    /** Return a XML string containing all the cache data for storing in index.xml */
    public String toXML() {
	calcRecommendationScore();
	StringBuffer sb = new StringBuffer(530); // 390
	sb.append("<CACHE name=\"");
	sb.append(SafeXML.string2Html(this.name));
	sb.append("\" owner=\"");
	sb.append(SafeXML.string2Html(this.owner));
	sb.append("\" lat=\"");
	sb.append(this.wpt.latDec);
	sb.append("\" lon=\"");
	sb.append(this.wpt.lonDec);
	sb.append("\" hidden=\"");
	sb.append(this.hidden);
	sb.append("\" wayp=\"");
	if (!(code.equals(code.toUpperCase()))) {
	    code = code.toUpperCase();
	    // status = "aufGross";
	    this.saveCacheDetails();
	}
	sb.append(SafeXML.string2Html(code.toUpperCase()));
	sb.append("\" status=\"");
	sb.append(this.status);
	sb.append("\" ocCacheID=\"");
	sb.append(this.idOC);
	sb.append("\" lastSyncOC=\"");
	sb.append(this.lastSync);
	sb.append("\" num_recommended=\"");
	sb.append(Convert.formatInt(this.numRecommended));
	sb.append("\" num_found=\"");
	sb.append(Convert.formatInt(this.numFoundsSinceRecommendation));
	sb.append("\" attributesYes=\"");
	sb.append(Convert.formatLong(this.attributesBits[0]));
	sb.append("\" attributesNo=\"");
	sb.append(Convert.formatLong(this.attributesBits[2]));
	sb.append("\" boolFields=\"");
	sb.append(Convert.formatLong(this.boolFields2long()));
	sb.append("\" byteFields=\"");
	sb.append(Convert.formatLong(this.byteFields2long()));
	sb.append("\" attributesYes1=\"");
	sb.append(Convert.formatLong(this.attributesBits[1]));
	sb.append("\" attributesNo1=\"");
	sb.append(Convert.formatLong(this.attributesBits[3]));
	sb.append("\"/>\n");
	return sb.toString();
    }

    public void calcDistance(CWPoint toPoint) {
	if (this.wpt.isValid()) {
	    kilom = this.wpt.getDistance(toPoint);
	    degrees = toPoint.getBearing(this.wpt);
	    bearing = CWPoint.getDirection(degrees);
	} else {
	    kilom = -1;
	    bearing = NOBEARING;
	}
    }

    public void setAttributesFromMainCache() {
	CacheHolder mainCh = this.mainCache;
	if (!this.owner.equalsIgnoreCase(mainCh.getOwner())) {
	    this.owner = mainCh.getOwner();
	    MainForm.profile.notifyUnsavedChanges(true);
	}
	if (this.isOwned != mainCh.isOwned()) {
	    this.isOwned = mainCh.isOwned();
	    MainForm.profile.notifyUnsavedChanges(true);
	}
	if (mainCh.isFound()) {
	    if (!this.isFound) {
		this.setStatus(mainCh.getStatus());
		this.setFound(true);
	    }
	    // else addi is already found (perhaps at other time)
	} else {
	    // there may be a found addi , so don't overwrite
	    if ((this.getType() == CacheType.CW_TYPE_FINAL)) {
		if (this.getWpt().isValid()) {
		    this.setStatus(mainCh.getStatus());
		}
		this.setFound(false);
	    }
	}
	this.setArchived(mainCh.isArchived());
	this.setAvailable(mainCh.isAvailable());
	this.setBlack(mainCh.isBlack());
	this.setNew(mainCh.isNew());
    }

    public void setAttributesFromMainCacheToAddiWpts() {
	if (this.hasAddiWpt()) {
	    CacheHolder addiWpt;
	    for (int i = this.addiWpts.getCount() - 1; i >= 0; i--) {
		addiWpt = (CacheHolder) this.addiWpts.get(i);
		addiWpt.setAttributesFromMainCache();
	    }
	}
    }

    /**
     * True if ch and this belong to the same main cache.
     * 
     * @param ch
     * @return
     */
    public boolean hasSameMainCache(CacheHolder ch) {
	if (this == ch)
	    return true;
	if (ch == null)
	    return false;
	if ((!this.isAddiWpt()) && (!ch.isAddiWpt()))
	    return false;
	CacheHolder main1, main2;
	if (this.isAddiWpt())
	    main1 = this.mainCache;
	else
	    main1 = this;
	if (ch.isAddiWpt())
	    main2 = ch.mainCache;
	else
	    main2 = ch;
	return main1 == main2;
    }

    /**
     * Find out of detail object of Cache is loaded. Returns <code>true</code> if this is the case.
     * 
     * @return True when details object is present
     */
    public boolean detailsLoaded() {
	return details != null;
    }

    /**
     * Gets the CacheHolderDetail object of a cache.<br>
     * The detail object stores information which is not needed for every cache instantaneously, but can be loaded if the user decides to look at this cache.<br>
     * If the cache object is already existing, the method will return this object,<br>
     * otherwise it will create it and try to read it from the corresponding <waypoint>.xml file.<br>
     * Depending on the parameters it is allowed that the <waypoint>.xml file does not yet exist, or the user is warned that the file doesn't exist.<br>
     * If more than <code>maxdetails</code> details are loaded, then the 5 last recently loaded caches are unloaded (to save ram).
     * 
     * @return The respective CacheHolderDetail, or null
     */
    public CacheHolderDetail getDetails() {
	if (details == null) {
	    details = new CacheHolderDetail(this);
	    details.readCache(MainForm.profile.dataDir);
	    if (details != null && !cachesWithLoadedDetails.contains(this)) {
		cachesWithLoadedDetails.add(this);
		if (cachesWithLoadedDetails.size() >= Preferences.itself().maxDetails)
		    removeOldestDetails();
	    }
	}
	return details;
    }

    /**
     * Saves the cache to the corresponding <waypoint>.xml file, located in the profiles directory. The waypoint of the cache should be set to do so.
     */
    public void saveCacheDetails() {
	checkIncomplete();
	this.getDetails().saveCacheDetails(MainForm.profile.dataDir);
    }

    void releaseCacheDetails() {
	if (details != null && details.hasUnsavedChanges) {
	    details.saveCacheDetails(MainForm.profile.dataDir);
	}
	details = null;
	cachesWithLoadedDetails.remove(this);
    }

    // final static int maxDetails = 50;
    public static Vector cachesWithLoadedDetails = new Vector(Preferences.itself().maxDetails);

    private void removeOldestDetails() {
	CacheHolder ch;
	for (int i = 0; i < Preferences.itself().deleteDetails; i++) {
	    // String wp = (String) cachesWithLoadedDetails.get(i);
	    // CacheHolder ch = MainForm.profile.cacheDB.get(wp);
	    ch = (CacheHolder) cachesWithLoadedDetails.get(i);
	    if (ch != null && ch.details.getParent() != this)
		ch.releaseCacheDetails();
	}
    }

    public static void removeAllDetails() {
	CacheHolder ch;
	for (int i = cachesWithLoadedDetails.size() - 1; i >= 0; i--) {
	    // String wp = (String) cachesWithLoadedDetails.get(i);
	    // CacheHolder ch = MainForm.profile.cacheDB.get(wp);
	    ch = (CacheHolder) cachesWithLoadedDetails.get(i);
	    if (ch != null && ch.detailsLoaded())
		ch.releaseCacheDetails();
	}
    }

    /**
     * when importing caches you can set details.saveChanges = true when the import is finished call this method to save the pending changes
     */
    public static void saveAllModifiedDetails() {
	CacheHolder ch;
	CacheHolderDetail chD;
	for (int i = cachesWithLoadedDetails.size() - 1; i >= 0; i--) {
	    // String wp = (String) cachesWithLoadedDetails.get(i);
	    // ch = MainForm.profile.cacheDB.get(wp);
	    ch = (CacheHolder) cachesWithLoadedDetails.get(i);
	    if (ch != null) {
		chD = ch.getDetails();
		if (chD != null && chD.hasUnsavedChanges) {
		    // ch.calcRecommendationScore();
		    chD.saveCacheDetails(MainForm.profile.dataDir);
		}
	    }
	}
    }

    private final static int MSG_NR = 0;
    private final static int GC_MSG = 1;
    private final static int IDX_WRITENOTE = 5;
    private final static String[][] _logType = { { "353", "" }, { "319", "Didn't find it" }, //
	    { "318", "Found it" }, //
	    { "355", "Attended" }, //
	    { "361", "Webcam Photo Taken" }, //
	    { "314", "Write note" }, // at change do change IDX_WRITENOTE = 5;
	    { "315", "Needs Archived" }, { "316", "Needs Maintenance" }, { "317", "Search" }, { "354", "Will Attend" }, { "320", "Owner" }, { "359", "Owner Maintenance" }, { "356", "Temporarily Disable Listing" }, { "357", "Enable Listing" },
	    { "358", "Post Reviewer Note" }, { "313", "Flag 1" }, { "360", "Flag 2" }, };

    public final static String[] GetGuiLogTypes() {
	String[] ret = new String[_logType.length];
	for (int i = 0; i < _logType.length; i++) {
	    ret[i] = MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), "");
	}
	return ret;
    }

    public String getGCFoundText() {
	int msgNr = 318; // normal found
	if (type == CacheType.CW_TYPE_WEBCAM) {
	    msgNr = 361;
	} else if (type == CacheType.CW_TYPE_EVENT || type == CacheType.CW_TYPE_MEGA_EVENT || type == CacheType.CW_TYPE_MAZE) {
	    msgNr = 355;
	}
	for (int i = 0; i < _logType.length; i++) {
	    if (msgNr == Common.parseInt(_logType[i][MSG_NR])) {
		return _logType[i][GC_MSG];
	    }
	}
	return "";
    }

    public String getGCFoundIcon() {
	String iconName = "2.png";
	if (type == CacheType.CW_TYPE_WEBCAM) {
	    iconName = "11.png";
	} else if (type == CacheType.CW_TYPE_EVENT || type == CacheType.CW_TYPE_MEGA_EVENT || type == CacheType.CW_TYPE_MAZE) {
	    iconName = "10.png";
	}
	return iconName;
    }

    public String getCWLogText(String s) {
	for (int i = 0; i < _logType.length; i++) {
	    if ((s).equals(_logType[i][GC_MSG])) {
		return MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), "");
	    }
	}
	return "";
    }

    public final static String getGCLogType(byte type, boolean isFound, String CacheStatus) {
	String gcLogType = _logType[IDX_WRITENOTE][GC_MSG];
	if (isFound) {
	    for (int i = 1; i < _logType.length; i++) {
		if (Common.parseInt(_logType[i][MSG_NR]) == CacheType.getLogMsgNr(type)) {
		    gcLogType = _logType[i][GC_MSG];
		    break;
		}
	    }
	} else {
	    for (int i = 1; i < _logType.length; i++) {
		if (CacheStatus.endsWith(MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), ""))) {
		    gcLogType = _logType[i][GC_MSG];
		    break;
		}
	    }
	}
	return gcLogType;
    }

    public String getStatusText() {
	if ((status.length() == 10 || status.length() == 16) && status.charAt(4) == '-') {
	    return CacheType.getFoundText(type) + " " + status;
	} else {
	    if (isFound) {
		return CacheType.getFoundText(type);
	    } else {
		return status;
	    }
	}
    }

    public String getStatusDate() {
	String statusDate = "";

	if (isFound() || getStatus().indexOf(MyLocale.getMsg(319, "not found")) > 10) {
	    Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
	    rexDate.search(getStatus());
	    if (rexDate.stringMatched(1) != null) {
		statusDate = rexDate.stringMatched(1);
	    }
	}

	return statusDate;
    }

    public String getStatusTime() {
	String statusTime = "";

	if (isFound() || getStatus().indexOf(MyLocale.getMsg(319, "not found")) > 10) {
	    Regex rexTime = new Regex("([0-9]{1,2}:[0-9]{2})");
	    rexTime.search(getStatus());
	    if (rexTime.stringMatched(1) != null) {
		statusTime = rexTime.stringMatched(1);
	    } else {
		Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
		rexDate.search(getStatus());
		if (rexDate.stringMatched(1) != null) {
		    statusTime = "00:00";
		}
	    }
	}

	return statusTime;
    }

    public String getStatusUtcDate() {
	String statusDate = getStatusDate();

	long timeZoneOffset = MainForm.profile.getTimeZoneOffsetLong();

	if (timeZoneOffset != 0 || MainForm.profile.getTimeZoneAutoDST()) {
	    //convert to UTC only if time is set
	    Regex rexTime = new Regex("([0-9]{1,2}:[0-9]{2})");
	    rexTime.search(getStatus());
	    if (rexTime.stringMatched(1) != null) {
		String statusDateTime = (statusDate + " " + getStatusTime()).trim();
		if (statusDateTime.length() > 0) {
		    try {
			Time logTime = new Time();
			logTime.parse(statusDateTime, "yyyy-MM-dd HH:mm");

			long timeZoneOffsetMillis = 0;

			if (timeZoneOffset == 100) { //autodetect
			    timeZoneOffsetMillis = Time.convertSystemTime(logTime.getTime(), false) - logTime.getTime();
			} else {
			    timeZoneOffsetMillis = timeZoneOffset * 3600000;
			}

			if (MainForm.profile.getTimeZoneAutoDST()) {
			    int lsM = (byte) (31 - ((int) (5 * logTime.year / 4) + 4) % 7);//last Sunday in March
			    int lsO = (byte) (31 - ((int) (5 * logTime.year / 4) + 1) % 7);//last Sunday in October

			    Time dstStart = new Time(lsM, 3, logTime.year);
			    dstStart.hour = 2;
			    dstStart.setTime(dstStart.getTime() - timeZoneOffsetMillis);
			    Time dstEnd = new Time(lsO, 10, logTime.year);
			    dstEnd.hour = 1;
			    dstEnd.minute = 59;
			    dstEnd.setTime(dstEnd.getTime() - timeZoneOffsetMillis);

			    if (logTime.after(dstStart) && logTime.before(dstEnd)) {
				timeZoneOffsetMillis += 3600000;
			    }
			}

			logTime.setTime(logTime.getTime() - timeZoneOffsetMillis);
			statusDate = logTime.format("yyyy-MM-dd");
		    } catch (IllegalArgumentException e) {
		    }
		}
	    }
	}

	return statusDate;
    }

    public String getStatusUtcTime() {
	String statusTime = getStatusTime();

	long timeZoneOffset = MainForm.profile.getTimeZoneOffsetLong();

	if (timeZoneOffset != 0 || MainForm.profile.getTimeZoneAutoDST()) {
	    //convert to UTC only if time is set
	    Regex rexTime = new Regex("([0-9]{1,2}:[0-9]{2})");
	    rexTime.search(getStatus());
	    if (rexTime.stringMatched(1) != null) {
		String statusDateTime = (getStatusDate() + " " + statusTime).trim();
		if (statusDateTime.length() > 0) {
		    try {
			Time logTime = new Time();
			logTime.parse(statusDateTime, "yyyy-MM-dd HH:mm");

			long timeZoneOffsetMillis = 0;

			if (timeZoneOffset == 100) { //autodetect
			    timeZoneOffsetMillis = Time.convertSystemTime(logTime.getTime(), false) - logTime.getTime();
			} else {
			    timeZoneOffsetMillis = timeZoneOffset * 3600000;
			}

			if (MainForm.profile.getTimeZoneAutoDST()) {
			    int lsM = (byte) (31 - ((int) (5 * logTime.year / 4) + 4) % 7);//last Sunday in March
			    int lsO = (byte) (31 - ((int) (5 * logTime.year / 4) + 1) % 7);//last Sunday in October

			    Time dstStart = new Time(lsM, 3, logTime.year);
			    dstStart.hour = 2;
			    dstStart.setTime(dstStart.getTime() - timeZoneOffsetMillis);
			    Time dstEnd = new Time(lsO, 10, logTime.year);
			    dstEnd.hour = 1;
			    dstEnd.minute = 59;
			    dstEnd.setTime(dstEnd.getTime() - timeZoneOffsetMillis);

			    if (logTime.after(dstStart) && logTime.before(dstEnd)) {
				timeZoneOffsetMillis += 3600000;
			    }
			}

			logTime.setTime(logTime.getTime() - timeZoneOffsetMillis);
			statusTime = logTime.format("HH:mm");
		    } catch (IllegalArgumentException e) {
		    }
		}
	    }
	}

	return statusTime;
    }

    public String getCacheID() {
	String result = "";
	if (this.isGC()) {
	    int gcId = 0;

	    String sequence = "0123456789ABCDEFGHJKMNPQRTVWXYZ";

	    String rightPart = this.code.substring(2).toUpperCase();

	    int base = 31;
	    if ((rightPart.length() < 4) || (rightPart.length() == 4 && sequence.indexOf(rightPart.charAt(0)) < 16)) {
		base = 16;
	    }

	    for (int p = 0; p < rightPart.length(); p++) {
		gcId *= base;
		gcId += sequence.indexOf(rightPart.charAt(p));
	    }

	    if (base == 31) {
		gcId += java.lang.Math.pow(16, 4) - 16 * java.lang.Math.pow(31, 3);
	    }

	    result = Integer.toString(gcId);
	} else if (isOC()) {
	    result = getIdOC();
	}

	return result;
    }

    /**
     * Initializes the caches states (and its addis) before updating, so that the "new", "updated", "log_updated" and "incomplete" properties are properly set.
     * 
     * @param pNewCache
     *            <code>true</code> if it is a new cache (i.e. a cache not existing in CacheDB), <code>false</code> otherwise.
     */
    public void initStates(boolean isNewCache) {
	this.setNew(isNewCache);
	this.setUpdated(false);
	this.setLogUpdated(false);
	this.setIncomplete(false);
	if (!isNewCache && this.hasAddiWpt()) {
	    for (int i = 0; i < this.addiWpts.size(); i++) {
		((CacheHolder) this.addiWpts.get(i)).initStates(isNewCache);
	    }
	}
    }

    /**
     * Creates a bit field of boolean values of the cache, represented as a long value. Boolean value of <code>true</code> results in <code>1</code> in the long values bits, and, vice versa, 0 for false.
     * 
     * @return long value representing the boolean bit field
     */
    private long boolFields2long() {
	// To get the same list of visible caches after loading a profile,
	// the property isVisible() is saved instead of isFiltered(), but at
	// the place where isFiltered() is read.
	long value = bool2BitMask(!this.isVisible(), 1) //
		| bool2BitMask(this.isAvailable, 2) //
		| bool2BitMask(this.isArchived, 3) //
		| bool2BitMask(this.hasBugs, 4) //
		| bool2BitMask(this.isBlack, 5) //
		| bool2BitMask(this.isOwned, 6) //
		| bool2BitMask(this.isFound, 7) //
		| bool2BitMask(this.isNew, 8) //
		| bool2BitMask(this.isLogUpdated, 9) //
		| bool2BitMask(this.isUpdated, 10) //
		| bool2BitMask(this.isHTML, 11) //
		| bool2BitMask(this.isIncomplete, 12) //
		| bool2BitMask(this.hasNote, 13) //
		| bool2BitMask(this.hasSolver, 14) //
		| bool2BitMask(this.isPMCache, 15) //
		| bool2BitMask(this.isSolved, 16) //
	;
	return value;
    }

    /**
     * Creates a field of byte values of certain properties of the cache, represented as a long value.<br>
     * As a long is 8 bytes wide, one might pack 8 bytes into a long, one every 8 bits.<br>
     * The position indicates the group of bits where the byte is packed,<br>
     * counting starting from one by the right side of the long.<br>
     * 
     * @return long value representing the byte field
     */
    private long byteFields2long() {
	long value = byteBitMask(this.difficulty, 1) | byteBitMask(this.terrain, 2) | byteBitMask(this.type, 3) | byteBitMask(this.size, 4) | byteBitMask(this.noFindLogs, 5);
	return value;
    }

    /**
     * sets Difficulty, Terrain, Type, Size and NoFindLogs on reading DB
     * 
     * @param value
     *            The long value which contains up to 8 bytes.
     */
    private void long2byteFields(long value) {
	this.difficulty = byteFromLong(value, 1);
	this.terrain = byteFromLong(value, 2);
	this.type = byteFromLong(value, 3);
	this.size = byteFromLong(value, 4);
	this.noFindLogs = byteFromLong(value, 5);
	if (this.difficulty == CacheTerrDiff.CW_DT_ERROR || this.terrain == CacheTerrDiff.CW_DT_ERROR || this.size == CacheSize.CW_SIZE_ERROR || this.type == CacheType.CW_TYPE_ERROR) {
	    setIncomplete(true);
	}
    }

    /**
     * Extracts a byte from a long value. The position is the number of the 8-bit block of the long (which contains 8 8-bit blocks), counted from 1 to 8, starting from the right side of the long.
     * 
     * @param value
     *            The long value which contains the bytes
     * @param position
     *            The position of the byte, from 1 to 8
     * @return The decoded byte value
     */
    private byte byteFromLong(long value, int position) {
	byte b = -1; // = 11111111
	return (byte) ((value & this.byteBitMask(b, position)) >>> (position - 1) * 8);
    }

    /**
     * Evaluates boolean values from a long value, which is seen as bit field.
     * 
     * @param value
     *            The bit field as long value
     */
    private void long2boolFields(long value) {
	isFiltered = (value & this.bool2BitMask(true, 1)) != 0;
	isAvailable = (value & this.bool2BitMask(true, 2)) != 0;
	isArchived = (value & this.bool2BitMask(true, 3)) != 0;
	hasBugs = (value & this.bool2BitMask(true, 4)) != 0;
	isBlack = (value & this.bool2BitMask(true, 5)) != 0;
	isOwned = (value & this.bool2BitMask(true, 6)) != 0;
	isFound = (value & this.bool2BitMask(true, 7)) != 0;
	isNew = (value & this.bool2BitMask(true, 8)) != 0;
	isLogUpdated = (value & this.bool2BitMask(true, 9)) != 0;
	isUpdated = (value & this.bool2BitMask(true, 10)) != 0;
	isHTML = (value & this.bool2BitMask(true, 11)) != 0;
	isIncomplete = (value & this.bool2BitMask(true, 12)) != 0 || this.isIncomplete;
	hasNote = (value & this.bool2BitMask(true, 13)) != 0;
	hasSolver = (value & this.bool2BitMask(true, 14)) != 0;
	isPMCache = isPMCache || (value & this.bool2BitMask(true, 15)) != 0; // only used on reading the index, previously updated from Status 
	isSolved = isSolved || (value & this.bool2BitMask(true, 16)) != 0; //  only used on reading the index, previously updated from Status
    }

    /**
     * Represents a bit mask as long value for a boolean value which is saved at a specified position in the long field.
     * 
     * @param value
     *            The boolean value we want to code
     * @param position
     *            Position of the value in the bit mask
     * @return The corresponding bit mask:<br>
     * A long value where all bits are set to 0 except for the one we like to represent:<br>
     * This is 1 if the value is true, 0 if not.
     */
    private long bool2BitMask(boolean value, int position) {
	if (value) {
	    return (1L << (position - 1));
	} else {
	    return 0L;
	}
    }

    /**
     * Coding a long field which has only the bits of the byte value set. The position is the number (from 1 to 8) of the byte block which is used from the long.
     * 
     * @param value
     *            Byte to encode
     * @param position
     *            Position of the byte value in the long
     * @return Encoded byte value as long
     */
    private long byteBitMask(byte value, int position) {
	long result = (0xFF & (long) value) << ((position - 1) * 8);
	return result;
    }

    public String getBearing() {
	return bearing;
    }

    /**
     * Gets an IconAndText object for the cache.<br>
     * If the level of the Icon is equal to the last call of the method, the same (cached) object is returned.<br>
     * If the object is null or the level is different, a new object is created.<br>
     * 
     * @param fm
     *            Font metrics
     * @return New or old IconAndText object
     */
    public IconAndText getModificationIcon(FontMetrics fm) {
	if (this.isIncomplete) {
	    boolean doit = false;
	    if (this.modificationIcon == null)
		doit = true;
	    else {
		if (modificationLevel != CacheHolder.ISINCOMPLETE || !modificationIcon.text.equals(this.code)) {
		    doit = true;
		}
	    }
	    if (doit) {
		modificationIcon = new IconAndText(CacheType.getTypeImage(CacheType.CW_TYPE_ERROR), this.code, fm);
		modificationLevel = CacheHolder.ISINCOMPLETE;
	    }
	} else if (this.isNew) {
	    boolean doit = false;
	    if (this.modificationIcon == null)
		doit = true;
	    else {
		if (modificationLevel != CacheHolder.ISNEW || !modificationIcon.text.equals(this.code)) {
		    doit = true;
		}
	    }
	    if (doit) {
		modificationIcon = new IconAndText(MyTableModel.yellow, this.code, fm);
		modificationLevel = CacheHolder.ISNEW;
	    }
	} else if (this.isUpdated) {
	    boolean doit = false;
	    if (this.modificationIcon == null)
		doit = true;
	    else {
		if (modificationLevel != CacheHolder.ISUPDATED || !modificationIcon.text.equals(this.code)) {
		    doit = true;
		}
	    }
	    if (doit) {
		modificationIcon = new IconAndText(MyTableModel.red, this.code, fm);
		modificationLevel = CacheHolder.ISUPDATED;
	    }
	} else if (this.isLogUpdated) {
	    boolean doit = false;
	    if (this.modificationIcon == null)
		doit = true;
	    else {
		if (modificationLevel != CacheHolder.ISLOGUPDATED || !modificationIcon.text.equals(this.code)) {
		    doit = true;
		}
	    }
	    if (doit) {
		modificationIcon = new IconAndText(MyTableModel.blue, this.code, fm);
		modificationLevel = CacheHolder.ISLOGUPDATED;
	    }
	} else {
	    modificationLevel = 0;
	    modificationIcon = null;
	}
	return modificationIcon;
    }

    /**
     * If this returns <code>true</code>, then the additional waypoints for this cache should be displayed regardless how the filter is set. If it is <code>false</code>, then the normal filter settings apply.<br>
     * This property is not saved in index.xml, so if you reload the data, then this information is gone.
     * 
     * @return <code>True</code>: Always display additional waypoints for cache.
     */
    public boolean showAddis() {
	return this.showAddis;
    }

    /**
     * Setter for <code>showAddis()</code>. If this returns <code>true</code>, then the additional waypoints for this cache should be displayed regardless how the filter is set. If it is <code>false</code>, then the normal filter settings apply.<br>
     * This property is not saved in index.xml, so if you reload the data, then this information is gone.
     * 
     * @param value
     *            <code>True</code>: Always display additional waypoints for cache.
     */
    public void setShowAddis(boolean value) {
	// This value is always stored in the main cache and all addis.
	CacheHolder mc = null;
	if (this.mainCache == null) {
	    mc = this;
	} else {
	    mc = this.mainCache;
	}
	if (mc.showAddis != value) {
	    mc.showAddis = value;
	    for (int i = 0; i < mc.addiWpts.size(); i++) {
		CacheHolder ac = (CacheHolder) mc.addiWpts.get(i);
		ac.showAddis = value;
	    }
	}
    }

    /** checks the waypoint data integrity to set a warning flag if something is missing */
    public boolean checkIncomplete() {
	// TODO: discuss if we should only check cache waypoints and silently "fix" everything else
	boolean ret;
	if (isCacheWpt()) {
	    if (this.code.length() < 3 || getDifficulty() < CacheTerrDiff.CW_DT_UNSET || getTerrain() < CacheTerrDiff.CW_DT_UNSET || getSize() == CacheSize.CW_SIZE_ERROR || getOwner().length() == 0 || getHidden().length() == 0
		    || getName().length() == 0)
		ret = true;
	    else
		ret = false;
	} else if (isAddiWpt()) {
	    // FIXME: do not check for mainCache == null, since it will be null during initial import
	    // FIXME: find out why we only check waypoints with IDs of a certain length ???
	    // if (mainCache == null
	    // || getHard() != CacheTerrDiff.CW_DT_UNSET
	    if (getDifficulty() != CacheTerrDiff.CW_DT_UNSET || getSize() != CacheSize.CW_SIZE_NOTCHOSEN || getTerrain() != CacheTerrDiff.CW_DT_UNSET || this.code.length() < 3
	    // || getCacheOwner().length() > 0
	    // || getDateHidden().length() > 0
		    || getName().length() == 0)
		ret = true;
	    else
		ret = false;
	} else if (isCustomWpt()) {
	    if (getDifficulty() != CacheTerrDiff.CW_DT_UNSET || getTerrain() != CacheTerrDiff.CW_DT_UNSET || getSize() != CacheSize.CW_SIZE_NOTCHOSEN || this.code.length() < 3
	    // || getCacheOwner().length() > 0
	    // || getDateHidden().length() > 0
	    // || getCacheName().length() == 0
	    )
		ret = true;
	    else
		ret = false;
	} else {
	    // we should not get here, so let's set a warning just in case
	    ret = true;
	}
	setIncomplete(ret);
	return ret;
    }

    public String getIdOC() {
	return this.idOC;
    }

    public void setIdOC(String idOC) {
	if (!idOC.equals(this.idOC)) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.idOC = idOC;
	}
    }

    public byte getNoFindLogs() {
	return noFindLogs;
    }

    public void setNoFindLogs(byte b) {
	if (b != this.noFindLogs) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.noFindLogs = b;
	}
    }

    public int getNumRecommended() {
	return this.numRecommended;
    }

    public String getRecommended() {
	if (!isCacheWpt())
	    return "";
	if (isOC()) {
	    return Convert.formatInt(LogList.getScore(this.numRecommended, this.numFoundsSinceRecommendation)) + " (" + Convert.formatInt(numRecommended) + ")";
	} else {
	    if (Preferences.itself().useGCFavoriteValue) {
		return "" + numRecommended;
	    } else {
		int gcVote = numRecommended;
		if (gcVote < 100) {
		    // Durchschnittswert der Abstimmung 1, 1.5 ... 4.5, 5 (nur eine Stimme)
		    return MyLocale.formatDouble((double) gcVote / 10.0, "0.0");
		} else {
		    int votes = gcVote / 100; // Anzahl Stimmen
		    gcVote = gcVote - 100 * votes; // Durchschnittswert der Abstimmung 1, 1.5 ... 4.5, 5
		    return MyLocale.formatDouble((double) gcVote / 10.0, "0.0") + " (" + Convert.formatInt(votes) + ")";
		}
	    }
	}
    }

    public void setNumRecommended(int i) {
	if (i != this.numRecommended) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.numRecommended = i;
	}
    }

    public int getNumFoundsSinceRecommendation() {
	return this.numFoundsSinceRecommendation;
    }

    public void setNumFoundsSinceRecommendation(int i) {
	if (i != this.numFoundsSinceRecommendation) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.numFoundsSinceRecommendation = i;
	}
    }

    public String getLastSync() {
	return this.lastSync;
    }

    public void setLastSync(String s) {
	if (!s.equals(this.lastSync)) {
	    MainForm.profile.notifyUnsavedChanges(true);
	    this.lastSync = s;
	}
    }

    /**
     * rename a waypoint ?and all its associated files
     * 
     * @param newCode
     *            new waypoint id (will be converted to upper case)
     * @return true on success, false on error
     */
    public boolean rename(String newCode) {
	newCode = newCode.toUpperCase();
	details = getDetails();
	if (details.rename(newCode)) {
	    setCode(newCode);
	    saveCacheDetails(); // the xml-file
	    MainForm.profile.notifyUnsavedChanges(true);
	    return true;
	} else {
	    return false;
	}
    }
}
