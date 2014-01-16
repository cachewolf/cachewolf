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
package CacheWolf.database;

/**
 * A travelbug journey starts in a cache (from....) where the tb is picked up and 
 * ends in another cache (to...) where the travelbug is dropped. For both transfers
 * the date/time is recorded and a flag (...Logged) is kept, indicating whether the
 * transfer has been logged to GC.
 * @author salzkammergut
 */
import CacheWolf.MyLocale;
import CacheWolf.SafeXML;
import ewe.fx.Image;
import ewe.sys.Convert;
import ewe.sys.Time;

public class TravelbugJourney {
    /** 
     * The travelbug concerned @see Travelbug 
     */
    private Travelbug tb;
    /** 
     * The profile from where the travelbug was picked up 
     */
    private String fromProfile;//4:
    /** 
     * The waypoint within the profile where the travelbug was picked up 
     */
    private String fromWaypoint;//5:
    /** 
     * The date and time when the travelbug was picked up 
     */
    private String fromDate; //6: 
    /** 
     * Flag that indicates whether the pick-up was logged with GC 
     */
    private boolean fromLogged;//7:
    /** 
     * The profile where the travelbug was dropped 
     */
    private String toProfile; //8:
    /** 
     * The waypoint within the profile where the travelbug was dropped 
     */
    private String toWaypoint; //9:
    /** 
     * The date and time when the travelbug was dropped 
     */
    private String toDate; //10:
    /** 
     * Flag that indicates whether the drop was logged with GC 
     */
    private boolean toLogged; //11:
    /**
     * When retrieving the elements of a travelbug journey by number, this
     * virtual column is used to retrieve the AND of fromLogged and toLogged. 
     * It thus returns true only if bith transactions have been logged.
     */
    public static final int BOTHLOGGED = 12;

    public TravelbugJourney(String id, String name, String trackingNo, String fromProfile, String fromWaypoint, String fromDate, String fromLogged, String toProfile, String toWaypoint, String toDate, String toLogged, String mission) {
	tb = new Travelbug(id, name, mission);
	tb.setTrackingNo(trackingNo);
	this.fromProfile = fromProfile;
	this.fromWaypoint = fromWaypoint;
	this.fromDate = fromDate;
	this.fromLogged = Convert.toBoolean(fromLogged);
	this.toProfile = toProfile;
	this.toWaypoint = toWaypoint;
	this.toDate = toDate;
	this.toLogged = Convert.toBoolean(toLogged);
    }

    public TravelbugJourney(String name) {
	tb = new Travelbug("", name, "");
	tb.setTrackingNo("");
	setFromProfile("");
	setFromWaypoint("");
	setFromDate("");
	setFromLogged("");
	setToProfile("");
	setToWaypoint("");
	setToDate("");
	setToLogged("");
    }

    public TravelbugJourney(Travelbug tb, String profile, String waypoint) {
	this.tb = tb;
	setFromProfile(profile);
	setFromWaypoint(waypoint);
	setFromDate(getDateTime());
	setToProfile("");
	setToWaypoint("");
	setToDate("");
	setFromLogged("");
	setToLogged("");
    }

    /** Drop a travelbug in a profile/waypoint and set the current date-time as
     * the drop date-time.
     * @param profile The profile where the tb is dropped
     * @param waypoint the waypoint where the tb is dropped
     */
    public void dropTravelbug(String profile, String waypoint) {
	setToProfile(profile);
	setToWaypoint(waypoint);
	setToDate(getDateTime());
    }

    private static Image checkboxTicked = new Image("checkboxTicked.png");
    private static Image checkboxUnticked = new Image("checkboxUnticked.png");

    /** Get an element of a TravelbugJourney by number. This is used when
     * displaying the journey in list format.
     * @param elementNo The element (=column) to get
     * @return The requested element as a String or Image
     */
    public Object getElementByNumber(int elementNo) {
	switch (elementNo) {
	//--- Travelbug ---
	case 0:
	    return tb.getGuid();
	case 1:
	    return tb.getName();
	case 2:
	    return tb.getTrackingNo();
	case 3:
	    return tb.getMission();
	    //--- TravelbugJourney ---
	case 4:
	    return getFromProfile();
	case 5:
	    return getFromWaypoint();
	case 6:
	    return getFromDate();
	case 7:
	    if (getFromLogged())
		return checkboxTicked;
	    else
		return checkboxUnticked;
	case 8:
	    return getToProfile();
	case 9:
	    return getToWaypoint();
	case 10:
	    return getToDate();
	case 11:
	    if (getToLogged())
		return checkboxTicked;
	    else
		return checkboxUnticked;
	    /* Special case 12: Return Z if both moves have been logged, blank otherwise
	     This allows the not logged tbJourneys to be sorted to the top.*/
	case 12:
	    return bothLogsDone() ? "Z" : " ";
	default:
	    return "?";
	}
    }

    /** Return the name of the journey element by number, i.e. the title column
     * of a list.
     * @param elementNo The element (=column) to get
     * @return The name as a String
     */
    public static String getElementNameByNumber(int elementNo) {
	switch (elementNo) {
	//--- Travelbug ---
	case 0:
	    return MyLocale.getMsg(6000, "Guid");
	case 1:
	    return MyLocale.getMsg(6001, "Name");
	case 2:
	    return MyLocale.getMsg(6002, "track#");
	case 3:
	    return MyLocale.getMsg(6003, "Mission");
	    //--- TravelbugJourney ---
	case 4:
	    return MyLocale.getMsg(6004, "From Prof");
	case 5:
	    return MyLocale.getMsg(6005, "From Wpt");
	case 6:
	    return MyLocale.getMsg(6006, "From Date");
	case 7:
	    return MyLocale.getMsg(6007, "From Log");
	case 8:
	    return MyLocale.getMsg(6008, "To Prof");
	case 9:
	    return MyLocale.getMsg(6009, "To Wpt");
	case 10:
	    return MyLocale.getMsg(6010, "To Date");
	case 11:
	    return MyLocale.getMsg(6011, "To Log");
	default:
	    return "?";
	}
    }

    /** Return the travelbug that defines the journey */
    public Travelbug getTb() {
	if (tb == null)
	    tb = new Travelbug("");
	return tb;
    }

    /** The date when the travelbug was picked up */
    public String getFromDate() {
	return fromDate;
    }

    /** The date when the travelbug was picked up */
    public void setFromDate(String fromDate) {
	this.fromDate = fromDate;
    }

    /** The profile where the travelbug was picked up */
    public String getFromProfile() {
	return fromProfile;
    }

    /** The profile where the travelbug was picked up */
    public void setFromProfile(String fromProfile) {
	this.fromProfile = fromProfile;
    }

    /** The waypoint where the travelbug was picked up */
    public String getFromWaypoint() {
	return fromWaypoint;
    }

    /** The waypoint where the travelbug was picked up */
    public void setFromWaypoint(String fromWaypoint) {
	this.fromWaypoint = fromWaypoint;
    }

    /** The log status of the travelbug pick-up transaction */
    public void setFromLogged(String fromLogged) {
	this.fromLogged = Convert.toBoolean(fromLogged);
    }

    /** The log status of the travelbug pick-up transaction */
    public void setFromLogged(boolean fromLogged) {
	this.fromLogged = fromLogged;
    }

    /** The log status of the travelbug pick-up transaction */
    public boolean getFromLogged() {
	return this.fromLogged;
    }

    /** The date when the travelbug was dropped */
    public String getToDate() {
	return toDate;
    }

    /** The date when the travelbug was dropped */
    public void setToDate(String toDate) {
	this.toDate = toDate;
    }

    /** The profile where the travelbug was dropped */
    public String getToProfile() {
	return toProfile;
    }

    /** The profile where the travelbug was dropped */
    public void setToProfile(String toProfile) {
	this.toProfile = toProfile;
    }

    /** The waypoint where the travelbug was dropped */
    public String getToWaypoint() {
	return toWaypoint;
    }

    /** The waypoint where the travelbug was dropped */
    public void setToWaypoint(String toWaypoint) {
	this.toWaypoint = toWaypoint;
    }

    /** The log status of the travelbug drop transaction */
    public void setToLogged(String toLogged) {
	this.toLogged = Convert.toBoolean(toLogged);
    }

    /** The log status of the travelbug drop transaction */
    public void setToLogged(boolean toLogged) {
	this.toLogged = toLogged;
    }

    /** The log status of the travelbug drop transaction */
    public boolean getToLogged() {
	return this.toLogged;
    }

    /** True if both transactions (pick-up and drop) have been logged with GC. */
    public boolean bothLogsDone() {
	return this.toLogged && this.fromLogged;
    }

    /**
     * Returns true if the travelbug is currently in my posession, i.e. it has
     * a pick-up date but no drop date.
     * @return The status to the travelbug
     */
    public boolean inMyPosession() {
	return !fromDate.equals("") && toDate.equals("");
    }

    /** Returns an XML representation of a TravelbugJourney for storing in a file */
    public String toXML() {
	StringBuffer s = new StringBuffer(200);
	s.append("  <tbj");
	appendElem(s, "id", tb.getGuid(), false);
	appendElem(s, "trackingNo", tb.getTrackingNo(), false);
	appendElem(s, "fromProfile", fromProfile, true);
	appendElem(s, "fromWaypoint", fromWaypoint, false);
	appendElem(s, "fromDate", fromDate, false);
	appendElem(s, "fromLogged", (new Boolean(fromLogged)).toString(), false);
	appendElem(s, "toProfile", toProfile, true);
	appendElem(s, "toWaypoint", toWaypoint, false);
	appendElem(s, "toDate", toDate, false);
	appendElem(s, "toLogged", (new Boolean(toLogged)).toString(), false);
	s.append("><name><![CDATA[");
	s.append(tb.getName());
	s.append("]]></name><![CDATA[");
	s.append(tb.getMission());
	s.append("]]></tbj>\n");
	return s.toString();
    }

    private void appendElem(StringBuffer s, String name, String value, boolean clean) {
	s.append(" ");
	s.append(name);
	s.append("=\"");
	if (clean)
	    s.append(SafeXML.clean(value));
	else
	    s.append(value);
	s.append("\"");
    }

    /** Returns the current date-time in format YYYY-MM-DD HH:MM */
    private String getDateTime() {
	Time t = new Time();
	return MyLocale.formatLong(t.year, "0000") + "-" + MyLocale.formatLong(t.month, "00") + "-" + MyLocale.formatLong(t.day, "00") + " " + MyLocale.formatLong(t.hour, "00") + ":" + MyLocale.formatLong(t.minute, "00");
    }

}
