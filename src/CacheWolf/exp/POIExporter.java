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
import CacheWolf.database.CacheType;
import CacheWolf.utils.FileBugfix;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.URLUTF8Encoder;
import CacheWolf.utils.W1252Codec;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.sys.Process;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Map.MapEntry;
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
    boolean hasBitmaps = false;
    ZipFile poiZip = null;

    final String[] categoryNames = { "_Archived", "_Disabled", "Available", "_Found", "_Owned", "_UNKNOWN" };
    final byte maxIndex = 6;
    final byte indexOfArchived = -6;
    final byte indexOfDisabled = -5;
    final byte indexOfAvailable = -4;
    final byte indexOfFound = -3;
    final byte indexOfOwn = -2;
    final byte indexOfUnknown = -1;
    Hashtable tableOfCategories = new Hashtable();

    public POIExporter() {
	super();
	this.outputFileExtension = "*.gpx";
	this.recordMethod = EXPORT_METHOD_LAT_LON;
	result = new StringBuffer(1000);
	tt = new TemplateTable();
	useCodec = new W1252Codec();
    }

    private void buildOutDBs() {
	// create the table for the different categories
	for (int i = 0; i < categoryNames.length; i++) {
	    tableOfCategories.put(categoryNames[i], new Hashtable());
	}
	// split profileDB by categories and CacheType
	Vector profileDB = MainForm.profile.cacheDB.getVectorDB();
	for (int i = 0; i < profileDB.size(); i++) {
	    Hashtable tableOfCategory;
	    CacheHolder ch = (CacheHolder) profileDB.get(i);
	    if (ch.isVisible()) {
		Byte type = new Byte(ch.getType());
		if (ch.isFound()) {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfFound]);
		    type = new Byte((byte) (indexOfFound));
		} else if (ch.isOwned()) {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfOwn]);
		    type = new Byte((byte) (indexOfOwn));
		} else if (ch.isArchived()) {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfArchived]);
		    type = new Byte((byte) (indexOfArchived));
		} else if (!ch.isAvailable()) {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfDisabled]);
		    type = new Byte((byte) (indexOfDisabled));
		} else if (ch.isAvailable()) {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfDisabled]);
		    // available Caches are split by type
		} else {
		    tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[maxIndex + indexOfUnknown]);
		    type = new Byte((byte) (indexOfUnknown));
		}
		Vector dbOfCacheTypeforCategory = (Vector) tableOfCategory.get(type);
		if (dbOfCacheTypeforCategory == null) {
		    dbOfCacheTypeforCategory = new Vector();
		    tableOfCategory.put(type, dbOfCacheTypeforCategory);
		}
		dbOfCacheTypeforCategory.add(ch);
	    }
	}
    }

    public void doIt() {
	POIExporterScreen gui = new POIExporterScreen(exporterName);
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
	if (gui.getAutoSplitByType()) {
	    doItStart();
	    String targetDir = this.getOutputPath();
	    if (targetDir.length() > 0) {
		buildOutDBs();

		try {
		    String bitmapFileName = FileBase.getProgramDirectory() + "/exporticons/GarminPOI.zip"; // own version
		    if (!(hasBitmaps = new File(bitmapFileName).exists())) {
			// cw default version
			bitmapFileName = FileBase.getProgramDirectory() + "/exporticons/exporticons/GarminPOI.zip";
			hasBitmaps = new File(bitmapFileName).exists();
		    }
		    if (hasBitmaps)
			poiZip = new ZipFile(bitmapFileName);
		} catch (IOException e) {
		    Preferences.itself().log("GPX Export: warning GarminPOI.zip not found", e, true);
		}

		String profileName;
		if (gui.getCreateProfileDir()) {
		    profileName = MainForm.profile.name;
		} else {
		    profileName = "";
		}
		targetDir = targetDir + profileName + "/";
		FileBugfix f = new FileBugfix(targetDir);
		f.createDir();

		if (gui.clearOutput()) {
		    Enumeration ite = this.poiZip.entries();
		    while (ite.hasMoreElements()) {
			ZipEntry aZippedFile = (ZipEntry) ite.nextElement();
			String fname = aZippedFile.getName();
			FileBugfix fn = new FileBugfix(targetDir + fname);
			fn.delete();
			fname = STRreplace.replace(fname, ".bmp", ".gpx");
			fn = new FileBugfix(targetDir + fname);
			fn.delete();
		    }
		}

		for (int i = 0; i < categoryNames.length; i++) {
		    Hashtable tableOfCategory = (Hashtable) tableOfCategories.get(categoryNames[i]);
		    if (tableOfCategory.size() > 0) {
			Iterator cacheTypesOfCategory = tableOfCategory.entries();
			while (cacheTypesOfCategory.hasNext()) {
			    MapEntry cacheTypeEntry = (MapEntry) cacheTypesOfCategory.next();
			    DB = (Vector) cacheTypeEntry.getValue();
			    // skip over empty cachetypes
			    if (DB.size() > 0) {
				// make the name  for the gpx and icon
				byte cacheType = ((Byte) cacheTypeEntry.getKey()).byteValue();
				String name = "";
				if (cacheType >= 0) {
				    name = CacheType.typeImageNameForId(cacheType);
				} else {
				    if (cacheType == (indexOfFound))
					name = categoryNames[maxIndex + indexOfFound];
				    else if (cacheType == (indexOfOwn))
					name = categoryNames[maxIndex + indexOfOwn];
				    else if (cacheType == (indexOfUnknown))
					name = categoryNames[maxIndex + indexOfUnknown];
				    else if (cacheType == (indexOfArchived))
					name = categoryNames[maxIndex + indexOfArchived];
				    else if (cacheType == (indexOfDisabled))
					name = categoryNames[maxIndex + indexOfDisabled];
				}
				if (hasBitmaps)
				    copyPoiIcon(targetDir, name, "", poiZip);
				this.setOutputFile(targetDir + name + outputFileExtension.substring(1));
				export();
			    }
			}
		    }
		}
		doItEnd();
		if (hasBitmaps)
		    try {
			poiZip.close();
		    } catch (IOException e) {
		    }
	    }
	} else {
	    DB = MainForm.profile.cacheDB.getVectorDB();
	    askForOutputFile();
	    if (outFile == null)
		return;
	    super.doIt();
	}

	if (gui.doPOILoader()) {
	    String[] cmd;
	    if (gui.doPOILoaderSilent()) {
		cmd = new String[2];
		cmd[0] = gui.POILoaderExe();
		cmd[1] = "/silent";
	    } else {
		cmd = new String[1];
		cmd[0] = gui.POILoaderExe();
	    }
	    startProcess(cmd);
	}
    }

    /**
     * Execute the command defined by cmd
     * 
     * @param cmd
     *            command and options to execute. if command or options include a space quatation marks are added. this will not wirk with the java version on unix systems
     * @return a handle to the process on success or null otherwise
     */
    Process startProcess(String[] cmd) {
	String command = "";
	for (int i = 0; i < cmd.length; i++) {
	    if (cmd[i].indexOf(" ") > -1) {
		cmd[i] = "\"" + cmd[i] + "\"";
	    }
	    command = command + cmd[i] + " ";
	}

	try {
	    return Vm.exec(command);
	} catch (IOException e) {
	    return null;
	}
    }

    //Overrides: export() in Exporter
    public void export() {
	exportHeader();
	exportBody();
	exportTrailer();
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
	ht = tt.toHashtable(new Regex("[,.]", "."), null, 0, 20, this.anzLogs, true, null, true, 1, "");
	result.setLength(0);
	picsCounter = 0;
	if (ch.isAddiWpt()) {
	    formatAddi(ch, lat, lon);
	    return result.toString();
	} else {
	    ch.getDetails();
	    if (!ch.detailsLoaded())
		return null;
	}

	if (noPictures) {
	    formatMain(ch, lat, lon, "", "");
	    return result.toString();
	} else {
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
	    if (this.picsCounter == 0) {
		formatMain(ch, lat, lon, "", "");
	    }
	}
	return result.toString();
    }

    private void formatAddi(CacheHolder ch, String lat, String lon) {

	result.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">").append(endLine);

	result.append("<name>");
	this.appendToResult(addiNameTagElements);
	result.append("</name>").append(endLine);

	result.append("<cmt>");
	this.appendToResult(addiCmtTagElements);
	result.append("</cmt>").append(endLine);

	result.append("<desc>");
	this.appendToResult(addiDescTagElements);
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

    // Overrides: trailer() in Exporter
    public String trailer() {
	return "</gpx>" + endLine;
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
		result.append(SafeXML.cleanGPX(removeHtmlTags((String) obj)));
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
				.append(": ").append(SafeXML.cleanGPX(removeHtmlTags((String) log.get("MESSAGE")))) //
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
