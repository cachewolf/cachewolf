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

/**
 * This is a powerfull class that is used very often. It is quicker than
 * XML parsing and should be used whenever possible to find and extract
 * parts of a string in a string.
 */
public class Extractor {
	int _startOffset;
	String _searchText;
	String start;
	String end;
	boolean _betweenonly;
	public static boolean INCLUDESTARTEND = false;
	public static boolean EXCLUDESTARTEND = true;

	/**
	 * Create an extractor.
	 * searchText = The string to search through.<br>
	 * st = The string that denotes the start of the string to extract<br>
	 * e = The string that denotes the end of the string to extract<br>
	 * startOffset = The beginning offset from which to start the search in sTxt<br>
	 * betweenonly = if false the string returned will inlcude st and e;
	 * if true it will not include st and e.
	 *
	 */
	public Extractor(String searchText, String st, String e, int startOffset, boolean betweenonly) {
		_startOffset = startOffset;
		_searchText = searchText;
		end = e;
		start = st;
		_betweenonly = betweenonly;
	}

	/**
	 * Mehtod to set the source text to be searched through
	 *
	 */
	public void set(String searchText, String st, String e, int startOffset, boolean betweenonly) {
		_startOffset = startOffset;
		_searchText = searchText;
		end = e;
		start = st;
		_betweenonly = betweenonly;
	}

	public void set(String searchText) {
		_searchText = searchText;
		_startOffset = 0;
	}

	public String findFirst(String searchText) {
		_searchText = searchText;
		_startOffset = 0;
		return findNext();
	}

	public String findNext(String startText) {
		start = startText;
		return findNext();
	}

	public String findNext(String startText, String endText) {
		start = startText;
		end = endText;
		return findNext();
	}

	/**
	 * Method to find the next occurance of a string that is enclosed by
	 * that start (st) and end string (e).
	 * if end is not found empty string is returned.
	 */
	public String findNext() {
		String ret = "";
		if (_searchText != null && _searchText.length() > _startOffset + start.length() + end.length()) {
			int idxStart = _searchText.indexOf(start, _startOffset);
			int idxEnd = -1;
			if (idxStart > -1) {
				idxEnd = _searchText.indexOf(end, idxStart + start.length());
				if (idxEnd > -1) {
					_startOffset = idxEnd;
					ret = _searchText.substring(idxStart + start.length(), idxEnd);
					if (!this._betweenonly)
						ret = start + ret + end;
				}
			}
			if (idxEnd == -1) {
				_startOffset = _searchText.length(); // Schluss
			}
		}
		return ret;
	}
}
