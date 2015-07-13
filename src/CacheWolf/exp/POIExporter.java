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
import CacheWolf.Preferences;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheImages;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.URLUTF8Encoder;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.sys.Time;
import ewe.ui.FormBase;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

/**
 * 
 * @author Kalle
 *         Class to create a gpx-File with links to the pictures of a
 *         cache, which is used as input for the POILoader from Garmin.
 */

public class POIExporter extends Exporter {
    private final String endLine = "\r\n"; //<br>
    private final char splitter = ',';
    private final String marker = "%";
    private boolean onlySpoiler, noPictures;
    private int anzLogs;
    Hashtable ht;
    String[] nameTagElements, cmtTagElements, descTagElements;
    String[] addiNameTagElements, addiCmtTagElements, addiDescTagElements;
    StringBuffer result;
    int picsCounter;
    TemplateTable tt;

    public POIExporter() {
	super();
	this.outputFileExtension = "*.gpx";
	this.exportMethod = LAT_LON;
	result = new StringBuffer(1000);
	tt = new TemplateTable();
    }

    public void doIt() {
	POIExporterScreen gui = new POIExporterScreen(expName);
	if (gui.execute() == FormBase.IDCANCEL)
	    return;
	this.onlySpoiler = gui.onlySpoiler();
	this.noPictures = gui.noPictures();
	this.anzLogs = gui.getAnzLogs();
	this.nameTagElements = split(gui.getNameTagDefinitions());
	this.cmtTagElements = split(gui.getCmtTagDefinitions());
	this.descTagElements = split(gui.getDescTagDefinitions());
	this.addiNameTagElements = split(gui.getAddiNameTagDefinitions());
	this.addiCmtTagElements = split(gui.getAddiCmtTagDefinitions());
	this.addiDescTagElements = split(gui.getAddiDescTagDefinitions());
	if (gui.getAutoSplitByType())
	    super.doIt(POIExporter.ASK_PATH);
	else
	    super.doIt(POIExporter.ASK_FILE);
    }

    /**
     * copy the bitmap identified by <code>prefix</code> and <code>type</code> from <code>poiZip</code> to <code>outdir</code>
     * 
     * @param outdir
     * @param type
     * @param prefix
     * @param poiZip
     * @return true on success, false otherwise
     */
    private boolean copyPoiIcon(String outdir, String type, String prefix, ZipFile poiZip) {
	ZipEntry icon;
	byte[] buff;
	int len;

	try {
	    icon = poiZip.getEntry(type + ".bmp");
	    if (icon == null)
		return false; // icon not found in archive

	    buff = new byte[icon.getSize()];
	    InputStream fis = poiZip.getInputStream(icon);
	    FileOutputStream fos = new FileOutputStream(outdir + (FileBase.separator) + prefix + type + ".bmp");
	    while (0 < (len = fis.read(buff)))
		fos.write(buff, 0, len);
	    fos.flush();
	    fos.close();
	    fis.close();
	} catch (ZipException e) {
	    Preferences.itself().log("failed to copy icon " + type + ".bmp", e, true);
	    return false;
	} catch (IOException e) {
	    Preferences.itself().log("failed to copy icon " + type + ".bmp", e, true);
	    return false;
	}
	return true;
    }

    private String[] split(String elements) {
	return mString.split(STRreplace.replace(STRreplace.replace(elements, "\\r", "\r"), "\\n", "\n"), splitter);
    }

    public String header() {
	result.setLength(0);
	Time tim = new Time();

	result.append("<?xml version=\"1.0\" encoding=\"Windows-1252\"?>" + endLine);
	result.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"CacheWolf\" version=\"1.1\"" //
		+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //
		+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">" + endLine);
	result.append("<metadata>" + endLine);
	result.append("<link href=\"http://www.cachewolf.de\"><text>CacheWolf</text></link>" + endLine);
	tim = tim.setFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
	tim = tim.setToCurrentTime();
	result.append("<time>" + tim.toString() + "</time>" + endLine);
	result.append("</metadata>" + endLine);
	return result.toString();
    }

    public String record(CacheHolder ch, String lat, String lon) {
	tt.set(ch);
	ht = tt.toHashtable(new Regex("[,.]", "."), null, 0, 20, this.anzLogs, new AsciiCodec(), null, true, 1, "");
	result.setLength(0);
	picsCounter = 0;
	if (ch.isAddiWpt()) {
	    formatAddi(ch, lat, lon);
	    return result.toString();
	} else {
	    // First check, if there are any pictures in the db for the wpt
	    ch.getDetails();
	    if (!ch.detailsLoaded())
		return null;
	    if (noPictures || ch.getDetails().images.size() == 0) {
		formatMain(ch, lat, lon, "", "");
		return result.toString();
	    }
	}

	if (!noPictures) {
	    CacheImages images = ch.getDetails().images.getDisplayImages(ch.getCode());
	    String alreadyDone = "";
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
		if (alreadyDone.indexOf(url) == -1) {
		    alreadyDone = alreadyDone + url;
		    picsCounter++;
		    formatMain(ch, lat, lon, url, comment);
		}
	    }
	}
	return result.toString();
    }

    private void formatAddi(CacheHolder ch, String lat, String lon) {

	result.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">").append(endLine);

	result.append("<name>");
	this.appendToResult(nameTagElements);
	result.append("</name>").append(endLine);

	result.append("<cmt>");
	this.appendToResult(cmtTagElements);
	result.append("</cmt>").append(endLine);

	result.append("<desc>");
	this.appendToResult(descTagElements);
	result.append("</desc>").append(endLine);

	appendLastPart();

	return;
    }

    private void formatMain(CacheHolder ch, String lat, String lon, String url, String comment) {

	result.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">").append(endLine);

	result.append("<name>");
	this.appendToResult(nameTagElements);
	result.append("</name>").append(endLine);

	result.append("<cmt>");
	this.appendToResult(cmtTagElements);
	result.append("</cmt>").append(endLine);

	result.append("<desc>");
	this.appendToResult(descTagElements);
	result.append("</desc>").append(endLine);

	if (url.length() > 0) {
	    result.append("<link href=\"" + URLUTF8Encoder.encode(url, false) + "\"/>").append(endLine);
	}
	appendLastPart();
	return;
    }

    private void appendLastPart() {
	result.append("<sym>Scenic Area</sym>").append(endLine) //
		.append("<extensions>").append(endLine) //
		.append("   <gpxx:WaypointExtension xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\">").append(endLine) //
		.append("      <gpxx:DisplayMode>SymbolAndName</gpxx:DisplayMode>").append(endLine) //
		.append("   </gpxx:WaypointExtension>").append(endLine) //
		.append("</extensions>").append(endLine) //
		.append("</wpt>").append(endLine) //
		.append(endLine);
    }

    public String trailer() {
	return "</gpx>" + endLine;
    }

    private void appendToResult(String[] elements) {
	for (int i = 0; i < elements.length; i++) {
	    if (elements[i].startsWith(marker)) {
		getElementValue(elements[i].substring(1));
	    } else {
		result.append(SafeXML.cleanGPX(elements[i]));
	    }
	}
    }

    private void getElementValue(String element) {
	Object obj = ht.get(element);
	if (obj != null) {
	    if (obj instanceof String) {
		result.append(SafeXML.cleanGPX((String) obj));
	    } else {
		if (element.equals("ATTRIBUTES")) {
		    Vector attributes = (Vector) obj;
		    int i = 0;
		    for (Iterator ite = attributes.iterator(); ite.hasNext();) {
			if (i != 0)
			    result.append(",");
			Hashtable attribute = (Hashtable) ite.next();
			result.append(SafeXML.cleanGPX((String) attribute.get("INFO")));
			i++;
		    }
		} else if (element.equals("LOGS")) {
		    Vector logs = (Vector) obj;
		    int i = 0;
		    for (Iterator ite = logs.iterator(); ite.hasNext();) {
			Hashtable log = (Hashtable) ite.next();
			result.append(SafeXML.cleanGPX((String) log.get("LOGGER"))) //
				.append(" ").append(SafeXML.cleanGPX((String) log.get("LOGTYPE"))) //
				.append(" on ").append((String) log.get("DATE")) //
				.append(": ").append(SafeXML.cleanGPX((String) log.get("MESSAGE"))) //
				.append(this.endLine);
			i++;
		    }
		}
	    }
	} else if (element.equals("PIC#")) {
	    if (picsCounter > 0)
		result.append("" + picsCounter);
	}
    }
}
