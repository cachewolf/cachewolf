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

import CacheWolf.MainForm;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheImages;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.URLUTF8Encoder;
import ewe.io.File;
import ewe.sys.Time;
import ewe.ui.FormBase;

/**
 * 
 * @author Kalle
 *         Class to create a gpx-File with links to the pictures of a
 *         cache, which is used as input for the POILoader from Garmin.
 */

public class POIExporter extends Exporter {
    private POIExporterScreen infoScreen;
    private boolean onlySpoiler;
    StringBuffer strBuf;

    public POIExporter() {
	super();
	this.setMask("*.gpx");
	this.setHowManyParams(LAT_LON);
	strBuf = new StringBuffer(1000);
    }

    public void doIt() {
	infoScreen = new POIExporterScreen(MyLocale.getMsg(2200, "POI Exporter"));
	if (infoScreen.execute() == FormBase.IDCANCEL)
	    return;
	onlySpoiler = infoScreen.getOnlySpoiler();
	super.doIt();
    }

    public String header() {
	strBuf.setLength(0);
	Time tim = new Time();

	strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"); // or ISO-8859-1
	strBuf.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"CacheWolf\" version=\"1.1\"" //
		+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //
		+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n");
	strBuf.append("<metadata>\r\n");
	strBuf.append("<link href=\"http://www.cachewolf.de\"><text>CacheWolf</text></link>\r\n");
	tim = tim.setFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
	tim = tim.setToCurrentTime();
	strBuf.append("<time>" + tim.toString() + "</time>\r\n");
	strBuf.append("</metadata>\r\n");
	return strBuf.toString();
    }

    public String record(CacheHolder ch, String lat, String lon) {
	strBuf.setLength(0);

	if (ch.isAddiWpt()) {
	    formatAddi(ch, lat, lon);
	    return strBuf.toString();
	} else {
	    // First check, if there a any pictures in the db for the wpt
	    ch.getDetails();
	    if (!ch.detailsLoaded())
		return null;
	    if (ch.getDetails().images.size() == 0) {
		formatMain(ch, lat, lon, "", "");
		return strBuf.toString();
	    }
	}

	CacheImages images = ch.getDetails().images.getDisplayImages(ch.getCode());
	for (int i = 0; i < images.size(); i++) {
	    String filename = images.get(i).getFilename();
	    String comment = images.get(i).getTitle();
	    String url = MainForm.profile.dataDir + filename;

	    // POILoader can only work with JPG-Files ?convert to jpg?
	    if (!filename.endsWith(".jpg"))
		continue;
	    // Try to export only Spoiler
	    if (onlySpoiler && (comment.toLowerCase().indexOf("oiler") < 1))
		continue;
	    // check if the file is not deleted
	    if (!(new File(url)).exists())
		continue;
	    formatMain(ch, lat, lon, url, comment);
	}

	return strBuf.toString();
    }

    private void formatAddi(CacheHolder ch, String lat, String lon) {

	strBuf.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">\r\n");
	strBuf.append("<name>");
	strBuf.append(SafeXML.cleanGPX(ch.getName()) + "\r\n");
	strBuf.append(CacheType.type2GSTypeTag(ch.getType()) + "\r\n"); //.getExportShortId
	strBuf.append(ch.getCode() + "\r\n");
	if (ch.getDetails().Hints.length() > 0) {
	    strBuf.append("Hint: " + SafeXML.cleanGPX(Common.rot13(ch.getDetails().Hints)) + "\r\n");
	}
	strBuf.append("</name>\r\n");

	strBuf.append("<cmt/>\r\n");
	strBuf.append("<desc>GCcode: " + ch.getCode() + " </desc>\r\n");
	appendLastPart();

	return;
    }

    private void formatMain(CacheHolder ch, String lat, String lon, String url, String comment) {

	strBuf.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">\r\n");
	strBuf.append("<name>");
	strBuf.append(SafeXML.cleanGPX(ch.getName()) + "\r\n");
	strBuf.append(CacheType.type2GSTypeTag(ch.getType()) + "\r\n"); //.getExportShortId
	strBuf.append("D:" + CacheTerrDiff.shortDT(ch.getDifficulty()) + " T:" + CacheTerrDiff.shortDT(ch.getTerrain()) + " S:" + CacheSize.cw2ExportString(ch.getSize()) + "\r\n");
	strBuf.append(ch.getCode() + " von " + SafeXML.cleanGPX(ch.getOwner()) + "\r\n");
	if (ch.getDetails().Hints.length() > 0) {
	    strBuf.append("Hint: " + SafeXML.cleanGPX(Common.rot13(ch.getDetails().Hints)) + "\r\n");
	}
	strBuf.append("</name>\r\n");
	strBuf.append("<cmt/>\r\n");
	strBuf.append("<desc>");
	strBuf.append("GCcode: " + ch.getCode() + "\r\n");
	strBuf.append(SafeXML.cleanGPX(SafeXML.html2iso8859s1(ch.getDetails().LongDescription)) + "\r\n"); // remove html from String
	strBuf.append("</desc>\r\n");
	if (url.length() > 0) {
	    strBuf.append("<link" + "href=\"" + URLUTF8Encoder.encode(url, false) + "\">");
	    strBuf.append("<text>" + SafeXML.cleanGPX(SafeXML.html2iso8859s1(comment)) + "</text>");
	    strBuf.append("</link>\r\n");
	}
	appendLastPart();
	return;
    }

    private void appendLastPart() {
	strBuf.append("<sym>Scenic Area</sym>\r\n");
	strBuf.append("<extensions>\r\n");
	strBuf.append("   <gpxx:WaypointExtension xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\">\r\n");
	strBuf.append("      <gpxx:DisplayMode>SymbolAndName</gpxx:DisplayMode>\r\n");
	strBuf.append("   </gpxx:WaypointExtension>\r\n");
	strBuf.append("</extensions>\r\n");
	strBuf.append("</wpt>\r\n");
	strBuf.append("\r\n");
    }

    public String trailer() {
	return "</gpx>\r\n";
    }
}
