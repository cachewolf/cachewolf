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
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.Log;
import CacheWolf.database.LogList;
import CacheWolf.utils.DateFormat;
import ewe.sys.Time;

/**
 * Class to export the cache database (index) to an CSV File which can bei easy
 * importet bei MS AutoRoute (testet with AR 2001 German) Format of the file:
 * Name;Breitengrad;Längengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink
 * 
 */
public class NewCSVExporter extends Exporter {

    public NewCSVExporter() {
	super();
	this.setMask("*.cl");
	this.setDecimalSeparator(',');
	this.setNeedCacheDetails(true);
	this.setHowManyParams(LAT_LON);
    }

    public String header() {
	return ">";
    }

    private int sum = 0;

    public String record(CacheHolder ch, String lat, String lon) {
	CacheHolderDetail chD = ch.getCacheDetails(false);
	if (chD != null) {
	    Log ownLog = chD.OwnLog;
	    if (ownLog != null) {
		Time ownDate = DateFormat.toDate(ownLog.getDate());
		if (ownDate.year == 1900) {
		    return ch.getWayPoint() + "\n";
		}
	    }
	}
	return null;
    }

    public String record_finddays(CacheHolder ch, String lat, String lon) {
	CacheHolderDetail chD = ch.getCacheDetails(false);
	LogList logs = chD.CacheLogs;
	Time ownDate = DateFormat.toDate(chD.OwnLog.getDate());
	int diffDays = 0;
	int ownDays = getDays(ownDate);
	String strLogDate = "";
	boolean unarchiveStatus = false;
	for (int i = 0; i < logs.size(); i++) {
	    Log theLog = logs.getLog(i);
	    strLogDate = theLog.getDate();
	    if (theLog.isFoundLog() || theLog.isPublishLog()) {
		Time logDate = DateFormat.toDate(strLogDate);
		if (ownDate.after(logDate)) {
		    diffDays = ownDays - getDays(logDate);
		    break;
		}
	    }
	    if (theLog.isUnArchivedLog()) {
		unarchiveStatus = true;
	    }
	    if (theLog.isPublishLog() || (theLog.isArchivedLog() && !unarchiveStatus)) {
		// ownDate before logDate
		// es könnte noch ein foundlog vor dem publish kommen
		// es könnte ein log nach dem archive kommen der aber durch unarchive aufgehoben ist
		diffDays = 0;
		break;
	    }
	}
	if (diffDays > 6 * 31) {
	    sum = sum + diffDays;
	    StringBuffer str = new StringBuffer(200);
	    str.append("\"" + ch.getCacheName() + "\";");
	    str.append("\"" + chD.OwnLog.getDate() + "\";");
	    str.append("\"" + strLogDate + "\";");
	    str.append("\"" + diffDays + "\";");
	    str.append("\"" + sum + "\";");
	    str.append("\"" + ch.getWayPoint() + "\"\r\n");

	    return str.toString();
	}
	return "";
    }

    static int dim[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private static int getDays(int day, int month, int year)
    //-------------------------------------------------------------------
    {
	int ret = -1;
	if (year < 1600)
	    return ret;
	int yearsPast = (year - 1600 - 1);
	int leaps = yearsPast / 4 - (yearsPast / 100) + (yearsPast / 400);
	if (yearsPast >= 1)
	    leaps++;
	if (month > 2 && Time.isLeapYear(year))
	    leaps++;
	int daysPast = leaps;
	for (int i = 1; i < month; i++)
	    daysPast += dim[i - 1];
	daysPast += day - 1;
	daysPast += (yearsPast + 1) * 365;
	return daysPast;
    }

    private static int getDays(Time t) {
	return getDays(t.day, t.month, t.year);
    }
}
