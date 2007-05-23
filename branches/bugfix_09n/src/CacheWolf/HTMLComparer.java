    /*
    CacheWolf is a software for PocketPC, Win and Linux that 
    enables paperless caching. 
    It supports the sites geocaching.com and opencaching.de
    
    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */

package CacheWolf;
import ewe.util.*;

/**
 * @author Kalle
 * Comparer for sorting the vector for the index.html file
 */
public class HTMLComparer implements Comparer {
	String compareWhat;

	public HTMLComparer (String what){
		this.compareWhat = what;
	}
	
	public int compare(Object o1, Object o2){
		Hashtable hash1 = (Hashtable)o1;
		Hashtable hash2 = (Hashtable)o2;
		String str1, str2;
		double dbl1, dbl2;

		str1 = hash1.get(compareWhat).toString().toLowerCase();
		str2 = hash2.get(compareWhat).toString().toLowerCase();
		
		if (this.compareWhat.equals("WAYPOINT")){
			str1 = hash1.get(compareWhat).toString().substring(2).toLowerCase();
			str2 = hash2.get(compareWhat).toString().substring(2).toLowerCase();
		}
		
		if (this.compareWhat.equals("DISTANCE")){
			dbl1 = Common.parseDouble(str1.substring(0,str1.length()-3));
			dbl2 = Common.parseDouble(str2.substring(0,str2.length()-3));
			if (dbl1 > dbl2) return 1;
			if (dbl1 < dbl2) return -1;
			else return 0;
		}
		else {
			return str1.compareTo(str2);
		}
	}
}
