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
package CacheWolf.utils;

/**
 * This is a powerfull class that is used very often. It is quicker than
 * XML parsing and should be used whenever possible to find and extract
 * parts of a string in a string.
 */
public class Extractor {
    public static boolean INCLUDESTARTEND = false;
    public static boolean EXCLUDESTARTEND = true;
    int _startOffset;
    String _searchText;
    String leftDelimiter;
    String rightDelimiter;
    boolean _betweenonly;

    /**
     * use set String searchText, String st, String e, int startOffset, boolean betweenonly afterwards
     */
    public Extractor() {
    }

    /**
     * Create an extractor.
     * searchText = The string to search through.<br>
     * st = The string that denotes the start of the string to extract<br>
     * e = The string that denotes the end of the string to extract<br>
     * startOffset = The beginning offset from which to start the search in sTxt<br>
     * betweenonly = if false the string returned will inlcude st and e;
     * if true it will not include st and e.
     */
    public Extractor(String searchText, String st, String e, int startOffset, boolean betweenonly) {
        _startOffset = startOffset;
        _searchText = searchText;
        rightDelimiter = e;
        leftDelimiter = st;
        _betweenonly = betweenonly;
    }

    /**
     * Mehtod to set the source text to be searched through
     */
    public Extractor set(String searchText, String st, String e, int startOffset, boolean betweenonly) {
        _startOffset = startOffset;
        _searchText = searchText;
        rightDelimiter = e;
        leftDelimiter = st;
        _betweenonly = betweenonly;
        return this;
    }

    public Extractor set(String st, String e, int startOffset) {
        _startOffset = startOffset;
        rightDelimiter = e;
        leftDelimiter = st;
        return this;
    }

    public Extractor set(String searchText) {
        _searchText = searchText;
        _startOffset = 0;
        return this;
    }

    public String findFirst(String searchText) {
        _searchText = searchText;
        _startOffset = 0;
        return findNext();
    }

    public String findNext(String startText) {
        leftDelimiter = startText;
        return findNext();
    }

    public String findNext(String startText, String endText) {
        leftDelimiter = startText;
        rightDelimiter = endText;
        return findNext();
    }

    public int searchedFrom() {
        return _startOffset;
    }

    /**
     * Method to find the next occurance of a string that is enclosed by
     * that start (st) and end string (e).
     * if end is not found empty string is returned.
     * _startOffset for search is at end of extracted string (ret without rightDelimiter),
     * so a delimiter string(rightDelimiter) can be used twice: first for rightDelimiter and then for leftDelimiter
     */
    public String findNext() {
        String ret = "";
        if (_searchText != null && _searchText.length() > _startOffset + leftDelimiter.length() + rightDelimiter.length()) {
            int idxLeftDelimiter = _searchText.indexOf(leftDelimiter, _startOffset);
            int idxRightDelimiter = -1;
            if (idxLeftDelimiter > -1) {
                idxRightDelimiter = _searchText.indexOf(rightDelimiter, idxLeftDelimiter + leftDelimiter.length());
                if (idxRightDelimiter > -1) {
                    _startOffset = idxRightDelimiter;
                    ret = _searchText.substring(idxLeftDelimiter + leftDelimiter.length(), idxRightDelimiter);
                    if (!this._betweenonly)
                        ret = leftDelimiter + ret + rightDelimiter;
                }
            }
            if (idxRightDelimiter == -1) {
                _startOffset = _searchText.length(); // Schluss
            }
        }
        return ret;
    }
}
