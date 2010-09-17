    /*
    GNU General Public License
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

/* Several date formats are used by GC.COM
 *    2/27/2004   - Hidden dates are in format mm/dd/yyyy (=US style)
 *    February 27 - Found dates which happened this year
 *    February 27, 2004 - Found date in previous year
 * The internal standard is sortable:
 *    2004-02-27    - YYYY-MM-DD   
 */

import ewe.sys.Convert;
import ewe.sys.Time;

public class DateFormat {

	/** Convert the US Format into a sortable format */
	public static String MDY2YMD(String date) {
		// Dates are in format M/D/Y
		int p1, p2 = -1, p3;
		p1 = date.indexOf("/");
		if (p1==-1){
			//dayofweek, dayofmonth month year (Monday, 07 June 2010)
			p1 = date.indexOf(",");			
			p2 = date.indexOf(" ", p1 + 2);
			p3 = date.indexOf(" ", p2 + 1);
			final String monthNames[] = { "January", "February", "March", "April", "May",
					"June", "July", "August", "September", "October", "November",
					"December" };
			for (int m = 0; m < 12; m++) {
				if (monthNames[m].equals(date.substring(p2+1,p3))) {
					String mm = Integer.toString(m+1);
					if (mm.length()==1) {mm=0+mm;}
					return date.substring(p3+1,p3+5) + "-" + mm + "-" + date.substring(p1+2, p1+4);
				}
			}
			return date;
		}
		else {
			if (p1 > 0)
				p2 = date.indexOf("/", p1 + 1);
			if (p1 > 0 && p2 > 0) {
				return date.substring(p2 + 1) + "-" + (p1 == 1 ? "0" : "")
						+ date.substring(0, p1) + "-" + (p1 + 2 == p2 ? "0" : "")
						+ date.substring(p1 + 1, p2);
			} else
				return date;
		}
	}

	/* Convert the sortable date into a US date */
	// static String YMD2MDY(String date) {
	// return
	// date.substring(4,6)+"/"+date.substring(6,8)+"/"+date.substring(0,4);
	// }
	/** Convert the log format into a sortable format */
	public static String logdate2YMD(String logdate) {
		String monthNames[] = { "January", "February", "March", "April", "May",
				"June", "July", "August", "September", "October", "November",
				"December" };
		Time t = new Time();
		String year, month, day;
		int i, m;
		logdate += ", " + t.year; // If logdate already has a year, this one is
									// ignored
		i = logdate.indexOf(',');
		year = logdate.substring(i + 2, i + 6);
		for (m = 0; m < 12; m++) {
			if (logdate.startsWith(monthNames[m])) {
				month = (m < 9 ? "0" : "") + Convert.formatInt(m + 1);
				day = logdate.substring(monthNames[m].length() + 1, i);
				if (day.length() == 1)
					day = "0" + day;
				return year + "-" + month + "-" + day;
			}
		}
		return "";
	}

}
