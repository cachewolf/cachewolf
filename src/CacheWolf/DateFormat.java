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

import ewe.sys.Time;
import ewe.util.mString;

public class DateFormat {

	public static String GCDateFormat = "";

	/** Convert the US Format into a sortable format */
	public static String toYYMMDD(String date) {
		return toYYMMDD(toDate(date));
	}

	public static Time toDate(String ds) {
		if (ds == null || ds.equals("") || ds.indexOf("1900") > -1)
			return new Time(1, 1, 1900);
		final long adaylong = new Time(2, 1, 2000).getTime() - new Time(1, 1, 2000).getTime();
		Time d = new Time();
		d.hour = 0;
		d.minute = 0;
		d.second = 0;
		d.millis = 0;
		if (ds.indexOf("day") > 0) {
			if (ds.indexOf("Yesterday") > -1) {
				d.setTime(d.getTime() - adaylong);
			} else {
				d.setTime(d.getTime() - adaylong * Common.parseInt(ds.substring(0, 1)));
			}
		} else {
			String[] SDate;
			ds = STRreplace.replace(ds, ",", " ");
			ds = STRreplace.replace(ds, "  ", " ");
			SDate = mString.split(ds, ' ');
			if (SDate.length == 1) {
				if (ds.indexOf('/') > -1)
					SDate = mString.split(ds, '/');
				else if (ds.indexOf('-') > -1)
					SDate = mString.split(ds, '-');
				// trying to determine Dateformat
				int v0 = Common.parseInt(SDate[0]);
				int v1 = Common.parseInt(SDate[1]);
				int v2 = Common.parseInt(SDate[2]);
				int dd, mm, yy;
				if (v0 > 31) {
					// yyyy mm dd
					yy = v0;
					mm = v1;
					dd = v2;
				} else {
					yy = v2;
					if ((v0 == 0) || (v1 == 0)) {
						// month as text
						String month;
						if (v0 == 0) {
							month = SDate[0];
							dd = v1;
						} else {
							month = SDate[1];
							dd = v0;
						}
						mm = monthName2int(month);
					} else {
						// mm dd yyyy (doesn't work for dd mm yyyy)
						if (GCDateFormat.equals("dd/MM/yyyy")) {
							dd = v0;
							mm = v1;
						} else {
							mm = v0;
							dd = v1;
						}
					}

				}
				d.month = mm;
				d.day = dd;
				d.year = yy;
			} else {
				// starting with dayOfWeek or missing year
				int offs = SDate.length - 3;
				if (offs < 0)
					offs = 0;
				int v0 = Common.parseInt(SDate[offs]);
				if (v0 == 0) {
					d.day = Common.parseInt(SDate[offs + 1]);
					d.month = monthName2int(SDate[offs]);
				} else {
					d.day = Common.parseInt(SDate[offs]);
					d.month = monthName2int(SDate[offs + 1]);
				}
				if (SDate.length > 2) {
					int yy = Common.parseInt(SDate[offs + 2]);
					if (yy < 100)
						d.year = 2000 + yy;
					else
						d.year = yy;
				} else
					// missing year
					; // d.year = this year
			}
		}
		return d;
	}

	private static int monthName2int(String month) {
		final String enMonthNames[] = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
		for (int m = 0; m < 12; m++) {
			if (enMonthNames[m].startsWith(month)) {
				return m + 1;
			}
		}
		final String deMonthNames[] = { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };
		for (int m = 0; m < 12; m++) {
			if (deMonthNames[m].startsWith(month)) {
				return m + 1;
			}
		}
		return 1; // Januar if not detected / in other language
	}

	public static String toYYMMDD(Time d) {
		return toYYMMDD(d, '-');
	}

	public static String toYYMMDD(Time d, char separator) {
		// the CW Time Format is with separator
		String f = "yyyy" + separator + "MM" + separator + "dd";
		return d.format(f);
	}

	// from lastSyncDate (yyyyMMddHHmmss) to gpxLogdate (yyyy-MM-dd)
	// if no lastSyncDate returns current Date
	public static String yyyyMMddHHmmss2gpxLogdate(String yyyyMMddHHmmss) {
		Time d = new Time();
		try {
			d.parse(yyyyMMddHHmmss, "yyyyMMddHHmmss");
		} catch (IllegalArgumentException e) {
			d = new Time();
			d.parse(yyyyMMddHHmmss, "yyyyMMddHHmmss");
		}
		return d.format("yyyy-MM-dd"); // +d.format("HH:mm:ss"); is set to 00:00:00 at gpxExport
	}

}
