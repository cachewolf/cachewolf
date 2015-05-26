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
package CacheWolf.exp;

import CacheWolf.database.CacheHolder;
import CacheWolf.database.CWPoint;
import ewe.util.Comparer;

public class DistanceComparer implements Comparer {

    CWPoint centre;

    public DistanceComparer(CWPoint centre) {
	this.centre = centre;
    }

    public int compare(Object one, Object two) {
	if ((!(one instanceof CacheHolder)) && (!(two instanceof CacheHolder))) {
	    return 0;
	} else {
	    CacheHolder a = (CacheHolder) one;
	    CacheHolder b = (CacheHolder) two;
	    return (int) ((a.getWpt().getDistance(centre) - b.getWpt().getDistance(centre)) * 1000);
	}
    }

}
