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

import CacheWolf.Preferences;
import CacheWolf.database.*;
import CacheWolf.imp.GCImporter;
import CacheWolf.utils.DateFormat;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.STRreplace;
import ewe.sys.Time;
import ewe.util.Enumeration;
import ewe.util.Hashtable;

/**
 * Class to export the cache database
 */
public class NewCSVExporter extends Exporter {
    public static final int CHECKOWNLOG = 0;
    public static final int DIFFERENTREVIEWERS = 1;
    public static final int SUMPERDAY = 2;
    public static final int NOFOUNDSFORXDAYS = 3;
    public static final int POSSIBLE_FTF = 4;
    public static final int PLACEDEQUALSFOUNDDAYMONTH = 5;
    public static final int GSAKLOG2NOTES = 6;
    static int daysOfMonth[] = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private int what;
    /* */
    // Pr�fung ownlog
    private GCImporter spider = null;
    private boolean doit = true;
    /* */
    // Cache mit unterschiedlichen Reviewern
    private Hashtable reviewers = new Hashtable(50);
    /* */
    // Funde pro Tag
    private Hashtable sumPerDay = new Hashtable(365);
    /* */
    private int sum = 0;
    private int forXDays = 186; // days (6=31, half year)

    public NewCSVExporter(int what) {
        super();
        this.setOutputFileExtension("*.txt");
        this.decimalSeparator = ',';
        this.setExportMethod(EXPORT_METHOD_NO_PARAMS);
        this.what = what;
    }
    // Ende Pr�fung ownlog
    /* */

    private static int getDays(int day, int month, int year)
    //-------------------------------------------------------------------
    {
        int ret = -1;
        if (year < 1600) {
            return ret;
        }
        int yearsPast = (year - 1600 - 1);
        int leaps = yearsPast / 4 - (yearsPast / 100) + (yearsPast / 400);
        if (yearsPast >= 1) {
            leaps++;
        }
        if (month > 2 && Time.isLeapYear(year)) {
            leaps++;
        }
        int daysPast = leaps;
        for (int i = 1; i < month; i++) {
            daysPast += daysOfMonth[i - 1];
        }
        daysPast += day - 1;
        daysPast += (yearsPast + 1) * 365;
        return daysPast;
    }

    private static int getDays(Time t) {
        return getDays(t.day, t.month, t.year);
    }
    /* */

    @Override
    public String header() {
        return null;
    }

    @Override
    public String record(CacheHolder ch) {
        switch (what) {
            case CHECKOWNLOG:
                return checkOwnLog(ch);
            case DIFFERENTREVIEWERS:
                return differentReviewers(ch);
            case SUMPERDAY:
                return sumPerDay(ch);
            case NOFOUNDSFORXDAYS:
                return noFoundsForXDays(ch);
            case POSSIBLE_FTF:
                return possibleFTF(ch);
            case PLACEDEQUALSFOUNDDAYMONTH:
                return placedEqualsFoundDayMonth(ch);
            case GSAKLOG2NOTES:
                return GsakLog2Notes(ch);
            default:
                return null;
        }
    }

    @Override
    public String trailer() {
        switch (what) {
            case SUMPERDAY:
                return sumPerDayResult();
            default:
                return "\r\n";
        }
    }

    private String checkOwnLog(CacheHolder ch) {
        if (spider == null) {
            spider = new GCImporter();
        }
        if (doit) {
            Preferences.itself().log(ch.getCode(), null);
            if (spider.isFoundByMe(ch)) {
                return " "; // for showing percent of task
            }
            return ch.getCode() + " not found";
        }
        return null;
    }

    private String checkOwn_Log(CacheHolder ch) {
        CacheHolderDetail chD = ch.getDetails();
        if (chD != null) {
            Log ownLog = chD.getOwnLog();
            if (ownLog != null) {
                String ownLogId = ownLog.getLogID();
                int anz = chD.getCacheLogs().size();
                for (int i = anz - 1; i >= 0; i--) {
                    Log log = chD.getCacheLogs().getLog(i);
                    if (log.getLogID().equals(ownLogId)) {
                        if (!(log.getFinderID().equals(Preferences.itself().gcMemberId))) {
                            return "wrong own log" + ch.getCode() + "\r\n";
                        } else {
                            return null;
                        }
                    }
                }
            } else {
                return "no own log" + ch.getCode() + "\r\n";
            }
        }
        return "own log not found" + ch.getCode() + "\r\n";
    }
    //_finddays_between_ownLog_previousLog

    public String differentReviewers(CacheHolder ch) {
        CacheHolderDetail chD = ch.getDetails();
        //if (chD.State.equals("Baden-W�rttemberg")) {
        int anz = chD.getCacheLogs().size();
        for (int i = anz - 1; i >= 0; i--) {
            Log log = chD.getCacheLogs().getLog(i);
            if (log.isPublishLog()) {
                if (reviewers.containsKey(log.getLogger())) {
                    return null;
                } else {
                    reviewers.put(log.getLogger(), ch);
                    //String ret = ch.getCode() + " published by " + log.getLogger() + ".\r\n";
                    return null;
                }
            }
        }
        //}
        return ch.getCode() + "\r\n";
    }
    /* */

    private String sumPerDay(CacheHolder ch) {
        String sdate = ch.getStatusDate();
        if (sumPerDay.containsKey(sdate)) {
            Integer tillNow = (Integer) sumPerDay.get(sdate);
            tillNow = new Integer(tillNow.intValue() + 1);
            sumPerDay.put(sdate, tillNow);
        } else {
            sumPerDay.put(sdate, new Integer(1));
        }
        return null;
    }

    private String sumPerDayResult() {
        String ret = "";
        for (final Enumeration e = sumPerDay.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            int i = ((Integer) sumPerDay.get(key)).intValue();
            if (i >= 12) {
                ret = ret + key + " Anz: " + i + "\r\n";
            }
        }
        return ret;
    }

    private String noFoundsForXDays(CacheHolder ch) {
        if (ch.isAddiWpt()) {
            return null;
        }
        CacheHolderDetail chD = ch.getDetails();
        LogList logs = chD.getCacheLogs();
        Time ownDate = DateFormat.toDate(chD.getOwnLog().getDate(), Log.DATEFORMAT);
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
        if (diffDays > forXDays) {
            sum = sum + diffDays;
            StringBuffer str = new StringBuffer(200);
            str.append("\"" + ch.getName() + "\";");
            str.append("\"" + chD.getOwnLog().getDate() + "\";");
            str.append("\"" + strLogDate + "\";");
            str.append("\"" + diffDays + "\";");
            str.append("\"" + sum + "\";");
            str.append("\"" + ch.getCode() + "\"\r\n");

            return str.toString();
        }
        return "";
    }

    private String possibleFTF(CacheHolder ch) {
        if (ch.isAddiWpt()) {
            return null;
        }
        byte chType = ch.getType();
        if (chType == CacheType.CW_TYPE_CITO || chType == CacheType.CW_TYPE_EVENT || chType == CacheType.CW_TYPE_GIGA_EVENT || chType == CacheType.CW_TYPE_MEGA_EVENT) {
            return null;
        }
        CacheHolderDetail chD = ch.getDetails();
        LogList logs = chD.getCacheLogs();
        Time ownDate;
        try {
            String myLogDate = chD.getOwnLog().getDate();
            if (myLogDate == null || myLogDate.length() == 0) {
                return "missing own Logdate:" + ch.getCode() + "\r\n";
            }
            ownDate = DateFormat.toDate(myLogDate, Log.DATEFORMAT);
        } catch (Exception e) {
            return "wrong own Logdate:" + ch.getCode() + "\r\n";
        }
        for (int i = 0; i < logs.size(); i++) {
            Log theLog = logs.getLog(i);
            if (theLog.isFoundLog()) {
                if (ownDate.after(DateFormat.toDate(theLog.getDate(), Log.DATEFORMAT))) {
                    return null;
                }
            }
            else if (theLog.isPublishLog()) {
                StringBuffer str = new StringBuffer(200);
                str.append("\"" + ch.getCode() + "\";");
                str.append("\"" + ch.getName() + "\";");
                str.append("\"" + chD.getOwnLog().getDate() + "\"\r\n");
                return str.toString();
            }
        }
        return "missing publishlog: " + ch.getCode() + "\r\n";
    }

    private String placedEqualsFoundDayMonth(CacheHolder ch) {
        if (ch.isAddiWpt()) {
            return null;
        }
        CacheHolderDetail chD = ch.getDetails();
        String sOwnDate;
        try {
            sOwnDate = chD.getOwnLog().getDate();
        } catch (Exception e) {
            return "missing own log: " + ch.getCode() + "\r\n";
        }
        Time ownDate = DateFormat.toDate(sOwnDate, Log.DATEFORMAT);
        String sPlacedDate = ch.getHidden();
        Time placedDate = DateFormat.toDate(sPlacedDate, Log.DATEFORMAT);
        if (ownDate.month == placedDate.month && ownDate.day == placedDate.day) {
            if (ownDate.year != placedDate.year) {
                return ch.getCode() + ";" + ch.getName() + ";" + sOwnDate + ";" + sPlacedDate + ";" + (ownDate.year - placedDate.year) + ";" + CacheType.type2Gui(ch.getType()) + "\r\n";
            }
        }
        return null;
    }

    private String GsakLog2Notes(CacheHolder ch) {
        if (ch.isAddiWpt()) {
            return null;
        }
        CacheHolderDetail chD = ch.getDetails();
        LogList logs = chD.getCacheLogs();
        for (int i = 0; i < logs.size(); i++) {
            Log theLog = logs.getLog(i);
            if (theLog.getLogger().toUpperCase().equals("GSAK")) {
                String gsakNotes = new Extractor(chD.getCacheNotes(), "<GSAK>", "</GSAK>", 0, false).findNext();
                String gsakLog = "<GSAK>" + theLog.getMessage() + "</GSAK>";
                if (gsakNotes.length() > 0) {
                    if (chD.setCacheNotes(STRreplace.replace(chD.getCacheNotes(), gsakNotes, gsakLog))) {
                        // only if changed
                        ch.saveCacheDetails();
                    }
                } else {
                    chD.setCacheNotes(chD.getCacheNotes() + gsakLog);
                    ch.saveCacheDetails();
                }
                return null;
            }
        }
        return null;
    }
}
