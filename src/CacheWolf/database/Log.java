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

import CacheWolf.Preferences;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.MyLocale;

import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;

public class Log {
    public final static String MAXLOGICON = "MAXLOG";
    private static String INVALIDLOGICON = null;
    private Transformer handleLinebreaks;
    private Transformer removeHTMLTags;
    /** The icon which describes the log e.g. icon_sad */
    private String icon;
    /** The date in format yyyy-mm-dd */
    private String date;
    /** The person who logged the cache */
    private String finder;
    /** The logged message */
    private String message;
    /** true, if the logger recommended the cache */
    private boolean recommended = false;
    /** GC LogID */
    private String logID;
    /** GC FinderID */
    private String finderID;

    /**
     * Create a log from a single line in format<br>
     * 
     * <pre>
     * RECOMMENDED=&quot;1&quot;&lt;img src='ICON'&gt; DATE LOGGER
     * <br>
     * MESSAGE
     * or &lt;img src='ICON'&gt; DATE by LOGGER
     * <br>
     * MESSAGE
     * </pre>
     * 
     * @param logLine
     */
    public Log(String logLine) {
	// RECOMMENDED="1"<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
	try {
	    Extractor ex = new Extractor(logLine, "logID=\"", "\"", 0, true);
	    logID = ex.findNext();
	    ex.set("finderID=\"", "\"", 0);
	    finderID = ex.findNext();
	    if (logLine.indexOf("RECOMMENDED=\"1\"") >= 0)
		recommended = true;
	    else
		recommended = false;
	    icon = ex.set("<img src='", "'", 0).findNext();
	    date = ex.findNext("&nbsp;", " ");
	    finder = ex.findNext("by ", "<br>");
	    message = ex.findNext("<br>", "]]>");
	} catch (Exception ex) {
	    if (logLine.indexOf("<img") < 0) { // Have we reached the line that states max logs reached
		icon = MAXLOGICON;
	    } else {
		Preferences.itself().log("Error parsing log: " + logLine, ex);
		icon = INVALIDLOGICON;
	    }
	    date = "1900-03-03";
	    finder = message = "";
	    logID = "";
	    finderID = "";
	}
    }

    public Log(String logID, String finderID, String icon, String date, String logger, String message) {
	this(logID, finderID, icon, date, logger, message, false);
    }

    public Log(String logID, String finderID, String icon, String date, String logger, String message, boolean recommended_) {
	this.icon = icon;
	this.date = date;
	this.finder = logger;
	this.message = stripControlChars(message.trim());
	this.recommended = recommended_;
	this.logID = logID;
	this.finderID = finderID;
    }

    private String stripControlChars(String desc) {
	StringBuffer sb = new StringBuffer(desc.length());
	for (int i = 0; i < desc.length(); i++) {
	    char c = desc.charAt(i);
	    if (c >= ' ' || c == 10 || c == 13)
		sb.append(c);
	}
	return sb.toString();
    }

    public static Log maxLog() {
	return new Log("", "", MAXLOGICON, "1900-00-00", "", "");
    }

    public String getLogID() {
	return logID;
    }

    public void setLogID(String value) {
	logID = value;
    }

    public String getFinderID() {
	return finderID;
    }

    public void setFinderID(String value) {
	finderID = value;
    }

    public String getIcon() {
	return icon;
    }

    public String getDate() {
	return date;
    }

    public String getLogger() {
	return finder;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String value) {
	message = value;
    }

    public String getMessageWithoutHTML() {
	handleLinebreaks = new Transformer(true);
	handleLinebreaks.add(new Regex("\r", ""));
	handleLinebreaks.add(new Regex("\n", " "));
	handleLinebreaks.add(new Regex("<br>", "\n"));
	handleLinebreaks.add(new Regex("<p>", "\n"));
	handleLinebreaks.add(new Regex("<hr>", "\n"));
	handleLinebreaks.add(new Regex("<br />", "\n"));

	removeHTMLTags = new Transformer(true);
	removeHTMLTags.add(new Regex("<(.*?)>", ""));

	return removeHTMLTags.replaceAll(handleLinebreaks.replaceAll(message));
    }

    public boolean isRecomended() {
	return recommended;
    }

    public boolean isFoundLog() {
	return icon.toLowerCase().equals("2.png") || icon.toLowerCase().equals("10.png") || icon.toLowerCase().equals("11.png");
    }

    public boolean isPublishLog() {
	return icon.toLowerCase().equals("24.png");
    }

    public boolean isArchivedLog() {
	return icon.toLowerCase().equals("5.png");
    }

    public boolean isUnArchivedLog() {
	return icon.toLowerCase().equals("12.png");
    }

    // if you change any of these make sure to check image2TypeText in the GPX exporters
    public static String typeText2Image(String typeText) {
	if (typeText.equals("Found it") || typeText.equals("Found") || typeText.equals("find"))
	    return "icon_smile.gif";
	if (typeText.equals("Didn't find it") || typeText.equals("Not Found") || typeText.equals("no_find"))
	    return "icon_sad.gif";
	if (typeText.equals("Write note") || typeText.equals("Note") || typeText.equals("note") || typeText.equals("Not Attempted") || typeText.equals("Other"))
	    return "icon_note.gif";
	if (typeText.equals("Enable Listing"))
	    return "icon_enabled.gif";
	if (typeText.equals("Temporarily Disable Listing"))
	    return "icon_disabled.gif";
	if (typeText.equals("Webcam Photo Taken"))
	    return "icon_camera.gif";
	if (typeText.equals("Attended"))
	    return "icon_attended.gif";
	if (typeText.equals("Publish Listing"))
	    return "icon_greenlight.gif";
	if (typeText.equals("Will Attend"))
	    return "icon_rsvp.gif";
	if (typeText.equals("Post Reviewer Note"))
	    return "big_smile.gif";
	if (typeText.equals("Unarchive"))
	    return "traffic_cone.gif";
	if (typeText.equals("Archive"))
	    return "traffic_cone.gif";
	if (typeText.equals("Owner Maintenance"))
	    return "icon_maint.gif";
	if (typeText.equals("Needs Maintenance"))
	    return "icon_needsmaint.gif";
	if (typeText.equals("Needs Archived"))
	    return "icon_remove.gif";
	if (typeText.equals("Update Coordinates"))
	    return "coord_update.gif";
	if (typeText.equals("Retract Listing"))
	    return "img_redlight.gif";
	if (typeText.equals("Announcement"))
	    return "74.png";
	if (typeText.equals("Unarchived"))
	    return "12.png";
	Preferences.itself().log("GPX Import: warning, unknown logtype " + typeText + " assuming Write note", null);
	return "icon_note.gif";
    }

    /**
     * generate a gc.com compatible string representation of log
     * 
     * @return log type. will default to "Write note" for unknown logtypes
     */
    public final String icon2GPXType() {
	if (icon.equals("icon_smile.gif"))
	    return "Found it";
	if (icon.equals("icon_sad.gif"))
	    return "Didn't find it";
	if (icon.equals("icon_note.gif"))
	    return "Write note";
	if (icon.equals("icon_enabled.gif"))
	    return "Enable Listing";
	if (icon.equals("icon_disabled.gif"))
	    return "Temporarily Disable Listing";
	if (icon.equals("icon_camera.gif"))
	    return "Webcam Photo Taken";
	if (icon.equals("icon_attended.gif"))
	    return "Attended";
	if (icon.equals("icon_greenlight.gif"))
	    return "Publish Listing";
	if (icon.equals("icon_rsvp.gif"))
	    return "Will Attend";
	if (icon.equals("big_smile.gif"))
	    return "Post Reviewer Note";
	if (icon.equals("traffic_cone.gif"))
	    return "Archive";
	if (icon.equals("icon_maint.gif"))
	    return "Owner Maintenance";
	if (icon.equals("icon_needsmaint.gif"))
	    return "Needs Maintenance";
	if (icon.equals("coord_update.gif"))
	    return "Update Coordinates";
	if (icon.equals("icon_remove.gif"))
	    return "Needs Archived";
	if (icon.equals("icon_redlight.gif"))
	    return "Retract Listing";

	if (icon.equals("2.png"))
	    return "Found it";
	if (icon.equals("3.png"))
	    return "Didn't find it";
	if (icon.equals("4.png"))
	    return "Write note";
	if (icon.equals("5.png"))
	    return "Archive";
	if (icon.equals("7.png"))
	    return "Needs Archived";
	if (icon.equals("9.png"))
	    return "Will Attend";
	if (icon.equals("10.png"))
	    return "Attended";
	if (icon.equals("11.png"))
	    return "Webcam Photo Taken";
	if (icon.equals("12.png"))
	    return "Unarchived"; // new todo check gpx-export/import
	if (icon.equals("18.png"))
	    return "Post Reviewer Note";
	if (icon.equals("22.png"))
	    return "Temporarily Disable Listing";
	if (icon.equals("23.png"))
	    return "Enable Listing";
	if (icon.equals("24.png"))
	    return "Publish Listing";
	if (icon.equals("25.png"))
	    return "Retract Listing";
	if (icon.equals("45.png"))
	    return "Needs Maintenance";
	if (icon.equals("46.png"))
	    return "Owner Maintenance";
	if (icon.equals("47.png"))
	    return "Update Coordinates";
	if (icon.equals("74.png"))
	    return "Announcement"; // new todo check gpx-export/import

	return "unknown logtype " + icon;
    }

    public final String icon2Message() {
	if (icon.equals("2.png"))
	    return MyLocale.getMsg(318, "Found it");
	if (icon.equals("3.png"))
	    return MyLocale.getMsg(319, "Didn't find it");
	if (icon.equals("4.png"))
	    return MyLocale.getMsg(314, "Write note");
	if (icon.equals("5.png"))
	    return MyLocale.getMsg(710, "Archived"); //Archive
	if (icon.equals("7.png"))
	    return MyLocale.getMsg(315, "Needs Archived");
	if (icon.equals("9.png"))
	    return MyLocale.getMsg(354, "Will Attend");
	if (icon.equals("10.png"))
	    return MyLocale.getMsg(355, "Attended");
	if (icon.equals("11.png"))
	    return MyLocale.getMsg(361, "Webcam Photo Taken");
	if (icon.equals("12.png"))
	    return "Unarchived"; // new todo check gpx-export/import
	if (icon.equals("18.png"))
	    return MyLocale.getMsg(358, "Post Reviewer Note");
	if (icon.equals("22.png"))
	    return MyLocale.getMsg(356, "Temporarily Disable Listing");
	if (icon.equals("23.png"))
	    return MyLocale.getMsg(357, "Enable Listing");
	if (icon.equals("24.png"))
	    return "Publish Listing";
	if (icon.equals("25.png"))
	    return "Retract Listing";
	if (icon.equals("45.png"))
	    return MyLocale.getMsg(316, "Needs Maintenance");
	if (icon.equals("46.png"))
	    return MyLocale.getMsg(359, "Owner Maintenance");
	if (icon.equals("47.png"))
	    return "Update Coordinates";
	if (icon.equals("74.png"))
	    return "Announcement";
	return "";
    }

    /** log was written by one of the aliases defined in preferences */
    public boolean isOwnLog() {
	return this.finder.equalsIgnoreCase(Preferences.itself().myAlias) || this.finder.equalsIgnoreCase(Preferences.itself().myAlias2);
    }

    /** Return XML representation of log for storing in cache.xml */
    public String toXML() {
	StringBuffer s = new StringBuffer(400);
	s.append("<LOG>");
	s.append("logID=\"" + logID + "\" ");
	s.append("finderID=\"" + finderID + "\" ");
	if (recommended)
	    s.append("RECOMMENDED=\"1\"");
	s.append("<![CDATA[");
	s.append(toHtml());
	s.append("]]>");
	s.append("</LOG>\r\n");
	return s.toString();
    }

    /** Return HTML representation of log for display on screen */
    public String toHtml() {
	// <img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
	if (icon.equals(MAXLOGICON))
	    message = "<hr>" + MyLocale.getMsg(736, "Too many logs") + "<hr>";
	StringBuffer s = new StringBuffer(300);
	s.append("<img src='" + icon + "'>");
	if (recommended)
	    s.append("<img src='recommendedlog.gif'>");
	s.append("&nbsp;");
	s.append(date);
	s.append(" by ");
	s.append(finder);
	s.append("<br>");
	s.append(message.trim());
	return s.toString();
    }
}
