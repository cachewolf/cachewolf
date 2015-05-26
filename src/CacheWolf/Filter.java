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
package CacheWolf;

import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Common;

import com.stevesoft.ewe_pat.Regex;

import ewe.util.Hashtable;

/**
 * Class that actually filters the cache database.<br>
 * The class that uses this filter must set the different public variables.
 *
 * @author BilboWolf (optimiert von salzkammergut)
 */
public class Filter {
    public static final int FILTER_INACTIVE = 0;
    public static final int FILTER_ACTIVE = 1;
    public static final int FILTER_CACHELIST = 2;
    public static final int FILTER_MARKED_ONLY = 3;

    /** Indicator whether a filter is inverted */
    // public static boolean filterInverted=false;
    /** Indicator whether a filter is active. Used in status bar to indicate filter status */
    // public static int filterActive=FILTER_INACTIVE;

    private static final int SMALLER = -1;
    private static final int EQUAL = 0;
    private static final int GREATER = 1;

    private static final int N = 1;
    private static final int NNE = 2;
    private static final int NE = 4;
    private static final int ENE = 8;
    private static final int E = 16;
    private static final int ESE = 32;
    private static final int SE = 64;
    private static final int SSE = 128;
    private static final int SSW = 256;
    private static final int SW = 512;
    private static final int WSW = 1024;
    private static final int W = 2048;
    private static final int WNW = 4096;
    private static final int NW = 8192;
    private static final int NNW = 16384;
    private static final int S = 32768;
    private static final int ROSE_ALL = N | NNE | NE | ENE | E | ESE | SE | SSE | SSW | SW | WSW | W | WNW | NW | NNW | S;

    private int distdirec = 0;
    private int diffdirec = 0;
    private int terrdirec = 0;

    String[] byVec;

    private int roseMatchPattern;
    private boolean hasRoseMatchPattern;
    private int typeMatchPattern;
    private boolean hasTypeMatchPattern;
    private int sizeMatchPattern;
    private boolean hasSizeMatchPattern;

    private boolean foundByMe;
    private boolean notFoundByMe;

    private String cacheStatus;
    private boolean useRegexp;
    private boolean filterNoCoord;

    private boolean ownedByMe;
    private boolean notOwnedByMe;

    double fscDist;
    double fscTerr;
    double fscDiff;

    private boolean archived = false;
    private boolean notArchived = false;

    private boolean premium = false;
    private boolean noPremium = false;

    private boolean solved = false;
    private boolean notSolved = false;

    private boolean available = false;
    private boolean notAvailable = false;
    double pi180 = java.lang.Math.PI / 180.0;

    private long[] attributesPattern = { 0l, 0l, 0l, 0l };
    private int attributesChoice = 0;

    private String syncDate = "";
    private String namePattern = "";
    private int nameCompare = 0;
    private boolean nameCaseSensitive = true;

    /**
     * Set the filter from the filter data stored in the MainForm.profile (the filterscreen also updates the MainForm.profile)
     */
    public void setFilter() {
	archived = MainForm.profile.getFilterVar().charAt(0) == '1';
	available = MainForm.profile.getFilterVar().charAt(1) == '1';
	foundByMe = MainForm.profile.getFilterVar().charAt(2) == '1';
	ownedByMe = MainForm.profile.getFilterVar().charAt(3) == '1';
	notArchived = MainForm.profile.getFilterVar().charAt(4) == '1';
	notAvailable = MainForm.profile.getFilterVar().charAt(5) == '1';
	notFoundByMe = MainForm.profile.getFilterVar().charAt(6) == '1';
	notOwnedByMe = MainForm.profile.getFilterVar().charAt(7) == '1';
	premium = MainForm.profile.getFilterVar().charAt(8) == '1';
	noPremium = MainForm.profile.getFilterVar().charAt(9) == '1';
	solved = MainForm.profile.getFilterVar().charAt(10) == '1';
	notSolved = MainForm.profile.getFilterVar().charAt(11) == '1';
	cacheStatus = MainForm.profile.getFilterStatus();
	useRegexp = MainForm.profile.getFilterUseRegexp();
	filterNoCoord = MainForm.profile.getFilterNoCoord();

	typeMatchPattern = CacheType.Type_FilterString2Type_FilterPattern(MainForm.profile.getFilterType());
	hasTypeMatchPattern = CacheType.hasTypeMatchPattern(typeMatchPattern);
	roseMatchPattern = 0;
	String filterRose = MainForm.profile.getFilterRose();
	if (filterRose.charAt(0) == '1')
	    roseMatchPattern |= NW;
	if (filterRose.charAt(1) == '1')
	    roseMatchPattern |= NNW;
	if (filterRose.charAt(2) == '1')
	    roseMatchPattern |= N;
	if (filterRose.charAt(3) == '1')
	    roseMatchPattern |= NNE;
	if (filterRose.charAt(4) == '1')
	    roseMatchPattern |= NE;
	if (filterRose.charAt(5) == '1')
	    roseMatchPattern |= ENE;
	if (filterRose.charAt(6) == '1')
	    roseMatchPattern |= E;
	if (filterRose.charAt(7) == '1')
	    roseMatchPattern |= ESE;
	if (filterRose.charAt(8) == '1')
	    roseMatchPattern |= SE;
	if (filterRose.charAt(9) == '1')
	    roseMatchPattern |= SSE;
	if (filterRose.charAt(10) == '1')
	    roseMatchPattern |= S;
	if (filterRose.charAt(11) == '1')
	    roseMatchPattern |= SSW;
	if (filterRose.charAt(12) == '1')
	    roseMatchPattern |= SW;
	if (filterRose.charAt(13) == '1')
	    roseMatchPattern |= WSW;
	if (filterRose.charAt(14) == '1')
	    roseMatchPattern |= W;
	if (filterRose.charAt(15) == '1')
	    roseMatchPattern |= WNW;
	hasRoseMatchPattern = roseMatchPattern != ROSE_ALL;
	sizeMatchPattern = 0;
	String filterSize = MainForm.profile.getFilterSize();
	if (filterSize.charAt(0) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_MICRO;
	if (filterSize.charAt(1) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_SMALL;
	if (filterSize.charAt(2) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_NORMAL;
	if (filterSize.charAt(3) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_LARGE;
	if (filterSize.charAt(4) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_VERYLARGE;
	if (filterSize.charAt(5) == '1')
	    sizeMatchPattern |= CacheSize.CW_FILTER_NONPHYSICAL;
	hasSizeMatchPattern = sizeMatchPattern != CacheSize.CW_FILTER_ALL;
	distdirec = MainForm.profile.getFilterDist().charAt(0) == 'L' ? SMALLER : GREATER;
	fscDist = Common.parseDouble(MainForm.profile.getFilterDist().substring(1)); // Distance
	diffdirec = MainForm.profile.getFilterDiff().charAt(0) == 'L' ? SMALLER : (MainForm.profile.getFilterDiff().charAt(0) == '=' ? EQUAL : GREATER);
	fscDiff = Common.parseDouble(MainForm.profile.getFilterDiff().substring(1)); // Difficulty
	terrdirec = MainForm.profile.getFilterTerr().charAt(0) == 'L' ? SMALLER : (MainForm.profile.getFilterTerr().charAt(0) == '=' ? EQUAL : GREATER);
	fscTerr = Common.parseDouble(MainForm.profile.getFilterTerr().substring(1)); // Terrain
	attributesPattern = MainForm.profile.getFilterAttr();
	attributesChoice = MainForm.profile.getFilterAttrChoice();
	// items from search panel
	syncDate = MainForm.profile.getFilterSyncDate();
	nameCaseSensitive = MainForm.profile.getFilterNameCaseSensitive();
	if (nameCaseSensitive)
	    namePattern = MainForm.profile.getFilterNamePattern();
	else
	    namePattern = MainForm.profile.getFilterNamePattern().toLowerCase();
	nameCompare = MainForm.profile.getFilterNameCompare();
    }

    /**
     * Apply the filter. Caches that match a criteria are flagged is_filtered = true. The table model is responsible for displaying or not displaying a cache that is filtered.
     */
    public void doFilter() {
	CacheDB cacheDB = MainForm.profile.cacheDB;
	Hashtable examinedCaches;
	if (cacheDB.size() == 0)
	    return;
	if (!hasFilter()) { // If the filter was completely reset, we can just
			    // clear it
	    clearFilter();
	    return;
	}
	MainForm.profile.selectionChanged = true;
	CacheHolder ch;
	examinedCaches = new Hashtable(cacheDB.size());

	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    ch = cacheDB.get(i);
	    if (examinedCaches.containsKey(ch))
		continue;

	    boolean filterCache = excludedByFilter(ch);
	    if (!filterCache && ch.mainCache != null && CacheType.hasMainTypeMatchPattern(typeMatchPattern)) {
		if (examinedCaches.containsKey(ch.mainCache)) {
		    filterCache = ch.mainCache.isFiltered();
		} else {
		    ch.mainCache.setFiltered(excludedByFilter(ch.mainCache));
		    filterCache = ch.mainCache.isFiltered();
		    examinedCaches.put(ch.mainCache, null);
		}
	    }
	    ch.setFiltered(filterCache);
	}
	MainForm.profile.setFilterActive(FILTER_ACTIVE);
	examinedCaches = null;
	// MainForm.profile.hasUnsavedChanges=true;
    }

    public boolean excludedByFilter(CacheHolder ch) {
	// Match once against type pattern and once against rose pattern
	// Default is_filtered = false, means will be displayed!
	// If cache does not match type or rose pattern then is_filtered is set to true
	// and we proceed to next cache (no further tests needed)
	// Then we check the other filter criteria one by one: As soon as one is found that
	// eliminates the cache (i.e. sets is_filtered to true), we can skip the other tests
	// A cache is only displayed (i.e. is_filtered = false) if it meets all 9 filter criteria
	int cacheTypePattern;
	int cacheRosePattern;
	int cacheSizePattern;
	double dummyd1;
	boolean cacheFiltered = false;
	do {
	    // /////////////////////////////
	    // Filter criterium 1: Cache type
	    // /////////////////////////////
	    if (hasTypeMatchPattern) { // Only do the checks if we have a
		// filter
		cacheTypePattern = CacheType.getCacheTypePattern(ch.getType());
		if ((cacheTypePattern & typeMatchPattern) == 0) {
		    cacheFiltered = true;
		    break;
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 2: Bearing from centre
	    // /////////////////////////////
	    // The optimal number of comparisons to identify one of 16 objects is 4 (=log2(16))
	    // By using else if we can reduce the number of comparisons from 16 to just over 8
	    // By first checking the first letter, we can reduce the average number further to
	    // just under 5
	    if (hasRoseMatchPattern) {
		if (ch.getBearing().startsWith("N")) {
		    if (ch.getBearing().equals("NW"))
			cacheRosePattern = NW;
		    else if (ch.getBearing().equals("NNW"))
			cacheRosePattern = NNW;
		    else if (ch.getBearing().equals("N"))
			cacheRosePattern = N;
		    else if (ch.getBearing().equals("NNE"))
			cacheRosePattern = NNE;
		    else
			cacheRosePattern = NE;
		} else if (ch.getBearing().startsWith("E")) {
		    if (ch.getBearing().equals("ENE"))
			cacheRosePattern = ENE;
		    else if (ch.getBearing().equals("E"))
			cacheRosePattern = E;
		    else
			cacheRosePattern = ESE;
		} else if (ch.getBearing().startsWith("S")) {
		    if (ch.getBearing().equals("SW"))
			cacheRosePattern = SW;
		    else if (ch.getBearing().equals("SSW"))
			cacheRosePattern = SSW;
		    else if (ch.getBearing().equals("S"))
			cacheRosePattern = S;
		    else if (ch.getBearing().equals("SSE"))
			cacheRosePattern = SSE;
		    else
			cacheRosePattern = SE;
		} else {
		    if (ch.getBearing().equals("WNW"))
			cacheRosePattern = WNW;
		    else if (ch.getBearing().equals("W"))
			cacheRosePattern = W;
		    else if (ch.getBearing().equals("WSW"))
			cacheRosePattern = WSW;
		    else
			cacheRosePattern = 0;
		}
		if ((cacheRosePattern != 0) && ((cacheRosePattern & roseMatchPattern) == 0)) {
		    cacheFiltered = true;
		    break;
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 3: Distance
	    // /////////////////////////////
	    if (fscDist > 0.0) {
		dummyd1 = ch.kilom;
		if (distdirec == SMALLER && dummyd1 > fscDist) {
		    cacheFiltered = true;
		    break;
		}
		if (distdirec == GREATER && dummyd1 < fscDist) {
		    cacheFiltered = true;
		    break;
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 4: Difficulty
	    // /////////////////////////////
	    if (fscDiff > 0.0) {
		dummyd1 = ch.getDifficulty() / 10D;
		if (diffdirec == SMALLER && dummyd1 > fscDiff) {
		    cacheFiltered = true;
		    break;
		}
		if (diffdirec == EQUAL && dummyd1 != fscDiff) {
		    cacheFiltered = true;
		    break;
		}
		if (diffdirec == GREATER && dummyd1 < fscDiff) {
		    cacheFiltered = true;
		    break;
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 5: Terrain
	    // /////////////////////////////
	    if (fscTerr > 0.0) {
		dummyd1 = ch.getTerrain() / 10D;
		if (terrdirec == SMALLER && dummyd1 > fscTerr) {
		    cacheFiltered = true;
		    break;
		}
		if (terrdirec == EQUAL && dummyd1 != fscTerr) {
		    cacheFiltered = true;
		    break;
		}
		if (terrdirec == GREATER && dummyd1 < fscTerr) {
		    cacheFiltered = true;
		    break;
		}
	    }

	    // /////////////////////////////
	    // Filter criterium 6: Found by me
	    // /////////////////////////////
	    if ((ch.isFound() && !foundByMe) || (!ch.isFound() && !notFoundByMe)) {
		cacheFiltered = true;
		break;
	    }
	    // /////////////////////////////
	    // Filter criterium 7: Owned by me
	    // /////////////////////////////
	    if ((ch.isOwned() && !ownedByMe) || (!ch.isOwned() && !notOwnedByMe)) {
		cacheFiltered = true;
		break;
	    }
	    // /////////////////////////////
	    // Filter criterium 8: Archived
	    // /////////////////////////////
	    if ((ch.isArchived() && !archived) || (!ch.isArchived() && !notArchived)) {
		cacheFiltered = true;
		break;
	    }
	    // /////////////////////////////
	    // Filter criterium 9: Unavailable
	    // /////////////////////////////
	    if ((ch.isAvailable() && !available) || (!ch.isAvailable() && !notAvailable)) {
		cacheFiltered = true;
		break;
	    }
	    if ((ch.isPMCache() && !premium) || (!ch.isPMCache() && !noPremium)) {
		cacheFiltered = true;
		break;
	    }
	    if ((ch.isSolved() && !solved) || (!ch.isSolved() && !notSolved)) {
		cacheFiltered = true;
		break;
	    }
	    // /////////////////////////////
	    // Filter criterium 10: Size
	    // /////////////////////////////
	    if (hasSizeMatchPattern) {
		cacheSizePattern = CacheSize.getFilterPattern(ch.getSize());
		if ((cacheSizePattern & sizeMatchPattern) == 0) {
		    cacheFiltered = true;
		    break;
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 11: Attributes
	    // /////////////////////////////
	    if ((attributesPattern[0] != 0 || attributesPattern[1] != 0 || attributesPattern[2] != 0 || attributesPattern[3] != 0) && ch.mainCache == null) {
		long[] chAtts = ch.getAttributesBits();
		if (attributesChoice == 0) {
		    // AND-condition:
		    if ((chAtts[0] & attributesPattern[0]) != attributesPattern[0] || //
			    (chAtts[1] & attributesPattern[1]) != attributesPattern[1] || //
			    (chAtts[2] & attributesPattern[2]) != attributesPattern[2] || //                                                        
			    (chAtts[3] & attributesPattern[3]) != attributesPattern[3] //
		    ) {
			cacheFiltered = true;
			break;
		    }
		} else if (attributesChoice == 1) {
		    // OR-condition:
		    if ((chAtts[0] & attributesPattern[0]) == 0 && //
			    (chAtts[1] & attributesPattern[1]) == 0 && //
			    (chAtts[2] & attributesPattern[2]) == 0 && //
			    (chAtts[3] & attributesPattern[3]) == 0 //
		    ) {
			cacheFiltered = true;
			break;
		    }
		} else {
		    // NOT-condition:
		    if ((chAtts[0] & attributesPattern[0]) != 0 || //
			    (chAtts[1] & attributesPattern[1]) != 0 || //
			    (chAtts[2] & attributesPattern[2]) != 0 || //
			    (chAtts[3] & attributesPattern[3]) != 0 //
		    ) {
			cacheFiltered = true;
			break;
		    }
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 12: Status
	    // /////////////////////////////
	    if (!cacheStatus.equals("")) {
		if (!useRegexp) {
		    if (ch.getStatusText().toLowerCase().indexOf(cacheStatus.toLowerCase()) < 0) {
			cacheFiltered = true;
			break;
		    }
		} else {
		    Regex rex = new Regex(cacheStatus.toLowerCase());
		    rex.search(ch.getStatusText().toLowerCase());
		    if (rex.stringMatched() == null) {
			cacheFiltered = true;
			break;
		    }
		}
	    }
	    // /////////////////////////////
	    // Filter criterium 13: NoCoord
	    // /////////////////////////////
	    if (!filterNoCoord && !ch.getWpt().isValid()) {
		cacheFiltered = true;
		break;
	    }
	    // /////////////////////////////
	    // Filter criterium 14: Search
	    // /////////////////////////////
	    if (!syncDate.equals("")) {
		if (syncDate.length() >= 10) {
		    // First sign is <, =, >, followed by '-' and then yyyymmdd
		    String theOperator = syncDate.substring(0, 1);
		    String theDate = syncDate.substring(2, 10);
		    String cacheSyncDate = ch.getLastSync();
		    if (cacheSyncDate.length() >= 8) {
			// time will not be taken into account
			cacheSyncDate = cacheSyncDate.substring(0, 8);
			int diff = theDate.compareTo(cacheSyncDate);
			if (theOperator.equals("<")) {
			    if (diff <= 0) {
				cacheFiltered = true;
				break;
			    }
			} else if (theOperator.equals("=")) {
			    if (diff != 0) {
				cacheFiltered = true;
				break;
			    }
			} else {
			    if (diff >= 0) {
				cacheFiltered = true;
				break;
			    }
			}
		    }
		}
	    }
	    if (namePattern.length() > 0) {
		String cacheName;
		if (nameCaseSensitive)
		    cacheName = ch.getName();
		else
		    cacheName = ch.getName().toLowerCase();
		if (nameCompare == 0) {
		    if (!cacheName.startsWith(namePattern)) {
			cacheFiltered = true;
			break;
		    }
		} else if (nameCompare == 1) {
		    if (cacheName.indexOf(namePattern) < 0) {
			cacheFiltered = true;
			break;
		    }
		} else if (nameCompare == 2) {
		    if (!cacheName.endsWith(namePattern)) {
			cacheFiltered = true;
			break;
		    }
		} else if (nameCompare == 3) {
		    if (cacheName.indexOf(namePattern) >= 0) {
			cacheFiltered = true;
			break;
		    }
		}
	    }

	    break;
	} while (true);
	return cacheFiltered;
    }

    /**
     * Switches flag to invert filter property.
     */
    public void invertFilter() {
	MainForm.profile.setFilterInverted(!MainForm.profile.isFilterInverted());
    }

    /**
     * Clear the is_filtered flag from the cache database.
     */
    public void clearFilter() {
	MainForm.profile.selectionChanged = true;
	CacheDB cacheDB = MainForm.profile.cacheDB;
	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    CacheHolder ch = cacheDB.get(i);
	    ch.setFiltered(false);
	}
	MainForm.profile.setFilterActive(FILTER_INACTIVE);
    }

    public boolean hasFilter() {
	long[] attribs = MainForm.profile.getFilterAttr();
	return !(MainForm.profile.getFilterType().equals(FilterData.FILTERTYPE) && MainForm.profile.getFilterRose().equals(FilterData.FILTERROSE) && MainForm.profile.getFilterVar().equals(FilterData.FILTERVAR)
		&& MainForm.profile.getFilterSize().equals(FilterData.FILTERSIZE) && MainForm.profile.getFilterDist().equals("L") && MainForm.profile.getFilterDiff().equals("L") && MainForm.profile.getFilterTerr().equals("L") && attribs[0] == 0l
		&& attribs[1] == 0l && attribs[2] == 0l && attribs[3] == 0l && MainForm.profile.getFilterStatus().equals("") && MainForm.profile.getFilterSyncDate().equals("") && MainForm.profile.getFilterNamePattern().equals("") && MainForm.profile
		    .getFilterNoCoord());
    }
}
