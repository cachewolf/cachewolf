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

import CacheWolf.utils.SafeXML;

/**
 * This class represents the settings of the filter that can be done when the users changes the
 * filter in CacheWolf.
 * 
 */
public class FilterData {

    // When extending the filter check "normaliseFilters"
    // which ensures backward compatibility. Normally no change should be needed
    public final static String FILTERTYPE = "111111111111111111111";
    public final static String FILTERROSE = "1111111111111111";
    public final static String FILTERVAR = "111111111111";
    public final static String FILTERSIZE = "11111111";

    private String filterType = FILTERTYPE;
    private String filterRose = FILTERROSE;
    private String filterSize = FILTERSIZE;
    private boolean filterNoCoord = true;

    // filter settings for archived ... owner (section) in filterscreen
    private String filterVar = FILTERVAR;
    private String filterDist = "L";
    private String filterDiff = "L";
    private String filterTerr = "L";

    private long[] filterAttr = { 0l, 0l, 0l, 0l };
    private int filterAttrChoice = 0;

    // filter setting for state of cache
    private String filterStatus = "";
    private boolean useRegexp = false;

    // filter items of the search panel
    private String syncDate = "";
    private String namePattern = "";
    private int nameCompare = 0;
    private boolean nameCaseSensitive = false;

    /**
     * Constructor for a profile
     * 
     */
    public FilterData() { // public constructor
    }

    /**
     * Returns an XML representation of the filter data.
     * If a non empty String is passed as parameter, then this String is used as ID-tag for the filter.
     * If it is empty, then the ID tag will not appear in the cache data.
     * The ID tag is the string which is used in the filter screen to appear in the filter list.
     * 
     * @param ID
     *            ID tag of filter
     * @return XML represenation of filter
     */
    public String toXML(String ID) {
	// do not change order, cause reading this is done in simple way
	String saveID = "";
	if (ID != null && !ID.equals("")) {
	    saveID = "id = \"" + SafeXML.string2Html(ID) + "\" ";
	}
	// '|' is splitter, it'll not work correctly if contained in any search item
	// alternative: '\u0399'
	String searchSeparator = "|";
	// just one entry for search to make it easier extendable later
	return "    <FILTERDATA " + saveID + "rose = \"" + getFilterRose() + "\"" + //
		" type = \"" + getFilterType() + "\"" + //
		" var = \"" + getFilterVar() + "\"" + //
		" dist = \"" + getFilterDist().replace('"', ' ') + "\"" + //
		" diff = \"" + getFilterDiff() + "\"" + //
		" terr = \"" + getFilterTerr() + "\"" + //
		" size = \"" + getFilterSize() + "\"" + //
		" attributesYes = \"" + filterAttr[0] + "\"" + //
		" attributesNo = \"" + filterAttr[2] + "\"" + //
		" attributesChoice = \"" + getFilterAttrChoice() + "\"" + //
		" status = \"" + SafeXML.string2Html(getFilterStatus()) + "\"" + //
		" useRegexp = \"" + useRegexp() + "\"" + //
		" noCoord = \"" + getFilterNoCoord() + "\"" + //
		" attributesYes1 = \"" + filterAttr[1] + "\"" + //
		" attributesNo1 = \"" + filterAttr[3] + "\"" + //
		" search = \"" + syncDate + searchSeparator + namePattern + searchSeparator + nameCompare + (nameCaseSensitive ? "1" : "0") + "\"" + //
		" />\n";
    }

    /**
     * Ensure that all filters have the proper length so that the 'charAt' access in the filter do
     * not cause nullPointer Exceptions
     */
    public void normaliseFilters() {
	String manyOnes = "11111111111111111111111111111";
	if (getFilterRose().length() < FILTERROSE.length()) {
	    setFilterRose((getFilterRose() + manyOnes).substring(0, FILTERROSE.length()));
	}
	if (getFilterVar().length() < FILTERVAR.length()) {
	    setFilterVar((getFilterVar() + manyOnes).substring(0, FILTERVAR.length()));
	}
	if (getFilterType().length() < FILTERTYPE.length()) {
	    setFilterType((getFilterType() + manyOnes).substring(0, FILTERTYPE.length()));
	}
	if (getFilterSize().length() < FILTERSIZE.length()) {
	    setFilterSize((getFilterSize() + manyOnes).substring(0, FILTERSIZE.length()));
	}
	if (getFilterDist().length() == 0)
	    setFilterDist("L");
	if (getFilterDiff().length() == 0)
	    setFilterDiff("L");
	if (getFilterTerr().length() == 0)
	    setFilterTerr("L");
    }

    // Getter and Setter for private properties

    public String getFilterType() {
	return filterType;
    }

    public void setFilterType(String filterType) {
	this.filterType = filterType;
    }

    public String getFilterRose() {
	return filterRose;
    }

    public void setFilterRose(String filterRose) {
	this.filterRose = filterRose;
    }

    public String getFilterSize() {
	return filterSize;
    }

    public void setFilterSize(String filterSize) {
	this.filterSize = filterSize;
    }

    public String getFilterVar() {
	return filterVar;
    }

    public void setFilterVar(String filterVar) {
	this.filterVar = filterVar;
    }

    public String getFilterDist() {
	return filterDist;
    }

    public void setFilterDist(String filterDist) {
	this.filterDist = filterDist;
    }

    public String getFilterDiff() {
	return filterDiff;
    }

    public void setFilterDiff(String filterDiff) {
	this.filterDiff = filterDiff;
    }

    public String getFilterTerr() {
	return filterTerr;
    }

    public void setFilterTerr(String filterTerr) {
	this.filterTerr = filterTerr;
    }

    public long[] getFilterAttr() {
	return filterAttr;
    }

    public void setFilterAttr(long[] filterAttr) {
	this.filterAttr = filterAttr;
    }

    public int getFilterAttrChoice() {
	return filterAttrChoice;
    }

    public void setFilterAttrChoice(int filterAttrChoice) {
	this.filterAttrChoice = filterAttrChoice;
    }

    public String getFilterStatus() {
	return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
	this.filterStatus = filterStatus;
    }

    public boolean useRegexp() {
	return useRegexp;
    }

    public void setUseRegexp(boolean useRegexp) {
	this.useRegexp = useRegexp;
    }

    public boolean getFilterNoCoord() {
	return filterNoCoord;
    }

    public void setFilterNoCoord(boolean filterNoCoord) {
	this.filterNoCoord = filterNoCoord;
    }

    public String getSyncDate() {
	return this.syncDate;
    }

    public void setSyncDate(String date) {
	this.syncDate = date;
    }

    public String getNamePattern() {
	return this.namePattern;
    }

    public void setNamePattern(String pattern) {
	this.namePattern = pattern;
    }

    public int getNameCompare() {
	return this.nameCompare;
    }

    public void setNameCompare(int compare) {
	this.nameCompare = compare;
    }

    public boolean getNameCaseSensitive() {
	return this.nameCaseSensitive;
    }

    public void setNameCaseSensitive(boolean caseSensitiv) {
	this.nameCaseSensitive = caseSensitiv;
    }
}
