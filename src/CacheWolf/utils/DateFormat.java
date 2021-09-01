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

/* Several date formats are used by GC.COM
 *    2/27/2004   - Hidden dates are in format mm/dd/yyyy (=US style)
 *    February 27 - Found dates which happened this year
 *    February 27, 2004 - Found date in previous year
 * The internal standard is sortable:
 *    2004-02-27    - YYYY-MM-DD
 */

import CacheWolf.Preferences;
import ewe.sys.Time;

public class DateFormat {

    final private static String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Convert the US Format DateHidden/DateVisited into a sortable format
     */
    public static String toYYMMDD(String date) {
        return toYYMMDD(toDate(date));
    }

    public static Time toDate(String input, String dateFormat) {
        Time result = new Time();
        try {
            if (input != null) {
                if (input.length() > 0) {
                    input = input.toLowerCase();
                    if (input.indexOf("yesterday") > -1) {
                        result.setTime(result.getTime() - (24L * 3600L * 1000L));
                    } else if (input.indexOf("days ago") > -1) {
                        String daysString = input.substring(0, input.indexOf("days") - 1);
                        int days = Integer.parseInt(daysString);
                        result.setTime(result.getTime() - (days * 24L * 3600L * 1000L));
                    } else if (input.indexOf("today") < 0) {
                        result.parse(input, dateFormat);
                    }
                }
            }
        } catch (Exception ex) {
            Preferences.itself().log("Error in Date Konversion of: " + input);
        }
        return result;
    }

    public static Time toDate(String ds) {
        String dateFormat = Preferences.itself().getGcDateFormat();
        return toDate(ds.trim(), dateFormat);
    }


    private static int monthName2int(String month) {
        for (int m = 0; m < 12; m++) {
            if (MONTH_NAMES[m].startsWith(month)) {
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
        }
        return d.format("yyyy-MM-dd"); // +d.format("HH:mm:ss"); is set to 00:00:00 at gpxExport
    }

    public static String formatLastSyncDate(String timeRepresentation, String format) {
        if (!timeRepresentation.equals("")) {
            try {
                Time t = new Time();
                t.parse(timeRepresentation, "yyyyMMddHHmmss");
                if (format.length() == 0)
                    return t.toString();
                else
                    return t.format(format);
            } catch (Exception e) {
            }
            return "";
        } else {
            return "";
        }

    }
}
