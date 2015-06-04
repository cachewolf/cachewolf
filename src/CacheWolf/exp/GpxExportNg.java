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
import CacheWolf.MainTab;
import CacheWolf.Preferences;
import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.Attribute;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheImages;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.Log;
import CacheWolf.database.LogList;
import CacheWolf.database.Travelbug;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.DateFormat;
import CacheWolf.utils.FileBugfix;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.URLUTF8Encoder;

import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;

import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Sound;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.PrintWriter;
import ewe.io.StreamReader;
import ewe.sys.Date;
import ewe.sys.Handle;
import ewe.sys.Process;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

/**
 * GPX exporter that should better handle the various tasks that can be accomplished with GPX
 */
public class GpxExportNg {
    final static String newLine = "\r\n";

    final static String WPNAMESTYLE = "wpNameStyle";
    final static String GPXVERSION = "GPXVersion";
    final static String PQVERSION = "PQVersion";
    final static String WITHGARMINEXTENSIONS = "withGarminExtensions";
    final static String WITHGSAKEXTENSIONS = "withGSAKExtensions";
    final static String SPLITSIZE = "splitSize";
    final static String EXPORTADDIWITHINVALIDCOORDS = "exportAddiWithInvalidCoords";
    final static String USECUSTOMICONS = "useCustomIcons";
    final static String SENDTOGARMIN = "sendToGarmin";
    final static String EXPORTLOGASPLAINTEXT = "exportLogsAsPlainText";
    final static String ATTRIB2LOG = "attrib2Log";
    final static String MAXNUMBEROFLOGSTOEXPORT = "maxNumberOfLogsToExport";
    final static String PREFIX = "prefix";

    /** write single GPX file */
    final static int OUTPUT_SINGLE = 0;
    /** write one gpx file per "type" as determined by garminmap.xml */
    final static int OUTPUT_SEPARATE = 1;
    /** write one gpi file per "type" as determined by garminmap.xml */
    final static int OUTPUT_POI = 2;

    /** export is without groundspeak extensions */
    final static int STYLE_COMPACT = 0;
    /** export is like groundspeak pocket query (PQ) */
    final static int STYLE_PQEXTENSIONS = 1;
    /** export follows gc.com MyFinds format */
    final static int STYLE_MYFINDS = 2;

    /** export uses only waypoint id */
    final static int WPNAME_ID_CLASSIC = 0;
    /** export uses waypointid + type, terrain, difficulty, size */
    final static int WPNAME_ID_SMART = 1;
    /** export uses cache names (will be made unique by gpsbabel) */
    final static int WPNAME_NAME_SMART = 2;

    /** name used as key when storing preferences */
    final static String exporterName = "GpxExportNG";
    /** string representation of true */
    final static String TRUE = "True";
    /** string representation of false */
    final static String FALSE = "False";

    /** object used to determine custom symbols and POI categories */
    private GarminMap garminMap;
    /** number of errors / warnings during export */
    private int exportErrors = 0;
    /**  */
    private String finderid;
    /*
     * groundspeak PQ Extensions
     * 1.0 Basic Definition of PQ Extensions
     * 1.0.1 Extensions
     * added the inc for <groundspeak:attributes>: attributes like 'dog-friendly' or 'handicapped access' will be listed. ID corresponds to an enum in the Geocaching.com database
     * <groundspeak:attribute id="1" inc="1">Dogs allowed</groundspeak:attribute>
     * 1.1 Extensions
     * Owner : Added GUID for transition to database redesign. Both GUID and ID are now optional  Owner ID corresponds to an account on Geocaching.com.
     * Added <groundspeak:lastUpdated>:  "xs:dateTime" is the last time the cache has been edited by the user
     * Added <groundspeak:exported>: ="xs:dateTime" for the benefit of splitting out caches -->
    */
    // we need to fake desc to make clients like GSAK accept additional waypoints together with caches
    final static String GPXHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + newLine//
	    + "<gpx"//
	    + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""//
	    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""//
	    + " version=\"1.0\""//
	    + " creator=\"CacheWolf\""//
	    + " xsi:schemaLocation=\""//
	    + "@@GPX@@"//
	    + " @@PQEXTENSION@@" //
	    + " @@GSAKEXTENSION@@" //
	    + " @@GPXXEXTENSION@@" //
	    + "\""//
	    + " xmlns=\"http://www.topografix.com/GPX/1/0\""//
	    + ">"//
	    + newLine//
	    + "<name>@@NAME@@</name>" + newLine//
	    + "<desc>This is an individual cache generated from Geocaching.com</desc>" + newLine//
	    + "<author>Various users from geocaching.com and/or opencaching.de</author>" + newLine//
	    + "<email>contact@cachewolf.de</email>" + newLine//
	    + "<url>http://www.cachewolf.de/</url>" + newLine//
	    + "<urlname>CacheWolf - Paperless Geocaching</urlname>" + newLine//
	    + "<time>@@CREATEDATE@@T07:00:00Z</time>" + newLine//
	    + "<keywords>cache, geocache, waypoints</keywords>" + newLine//
    // TODO: is it worth a second loop?
    // +("<bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\" maxlon=\"6.918817\" />")
    ;
    final static String XMLNSGPX = "http://www.topografix.com/GPX/@";
    private String xmlnsgpx;
    final static String XMLNSGSAK = "http://www.gsak.net/xmlv1/@";
    private String xmlnsgsak;
    final static String XMLNSPQ = "http://www.groundspeak.com/cache/@";
    private String xmlnspq;
    final static String XMLNSGPXX = "http://www.garmin.com/xmlschemas/GpxExtensionsv@";
    // http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd
    private String xmlnsgpxx;
    final static String GPXLOG = "\t\t\t\t<groundspeak:log id=\"@@LOGID@@\">" + newLine//
	    + ("\t\t\t\t\t<groundspeak:date>@@LOGDATE@@T19:00:00Z</groundspeak:date>") + newLine//
	    + ("\t\t\t\t\t<groundspeak:type>@@LOGTYPE@@</groundspeak:type>") + newLine//
	    + ("\t\t\t\t\t<groundspeak:finder id=\"@@LOGFINDERID@@\">@@LOGFINDER@@</groundspeak:finder>") + newLine//
	    + ("\t\t\t\t\t<groundspeak:text encoded=\"@@LOGENCODE@@\">@@LOGTEXT@@</groundspeak:text>") + newLine//
	    + ("\t\t\t\t</groundspeak:log>") + newLine;//

    final static String GPXTB = "\t\t\t\t<groundspeak:travelbug id=\"@@TBID@@\" ref=\"@@TBREF@@\">" + newLine//
	    + ("\t\t\t\t\t<groundspeak:name>@@TBNAME@@</groundspeak:name>") + newLine//
	    + ("\t\t\t\t</groundspeak:travelbug>") + newLine;//

    // FIXME: don't use this until GPX import can strip this off as well
    final static String GPXADDIINMAIN = "@@ADDIID@@ - @@ADDISHORT@@@@ADDIDELIM@@"//
	    + ("@@ADDILAT@@ @@ADDILON@@@@ADDIDELIM@@")//
	    + ("@@ADDILONG@@@@ADDIDELIM@@");//

    private static boolean attrib2Log;
    private static int maxLogs;

    private int exportStyle;
    private int outputStyle;

    private static boolean hasBitmaps;
    private static boolean hasGpsbabel;

    private static String bitmapFileName;
    private Transformer handleLinebreaks;
    private Transformer removeHTMLTags;
    private Transformer removeNumericEntities;

    public GpxExportNg() {
	garminMap = new GarminMap();

	bitmapFileName = FileBase.getProgramDirectory() + "/exporticons/GarminPOI.zip"; // own version
	if (!(hasBitmaps = new File(bitmapFileName).exists())) {
	    // cw default version
	    bitmapFileName = FileBase.getProgramDirectory() + "/exporticons/exporticons/GarminPOI.zip";
	    hasBitmaps = new File(bitmapFileName).exists();
	}

	hasGpsbabel = Preferences.itself().gpsbabel != null;

	finderid = Preferences.itself().gcMemberId;
	if (finderid.equals(""))
	    Preferences.itself().log("GPX Export: warning gcmemberid not set, check pref.xml", null);

	handleLinebreaks = new Transformer(true);
	handleLinebreaks.add(new Regex("\r", ""));
	handleLinebreaks.add(new Regex("\n", " "));
	handleLinebreaks.add(new Regex("<br>", "\n"));
	handleLinebreaks.add(new Regex("<p>", "\n"));
	handleLinebreaks.add(new Regex("<hr>", "\n"));
	handleLinebreaks.add(new Regex("<br />", "\n"));
	// handleLinebreaks.add(new Regex("<(.*?)>", "")); // check if this is needed twice

	removeHTMLTags = new Transformer(true);
	removeHTMLTags.add(new Regex("<(.*?)>", ""));

	removeNumericEntities = new Transformer(true);
	removeNumericEntities.add(new Regex("&#([xX]?)([a-fA-F0-9]*?);", ""));

    }

    private GpxExportNgForm exportOptions;
    private StringBuffer theLogs = new StringBuffer();
    private CacheHolder ch;

    public void doit() {

	exportOptions = new GpxExportNgForm(garminMap.exists, hasBitmaps, hasGpsbabel);
	if (exportOptions.execute() == FormBase.IDCANCEL) {
	    return;
	}
	this.exportStyle = this.exportOptions.getGpxStyle();
	this.outputStyle = this.exportOptions.getOutputStyle();

	if (exportStyle == STYLE_PQEXTENSIONS) {
	    maxLogs = exportOptions.getMaxLogs();
	    attrib2Log = exportOptions.getAttrib2Log();
	}

	if (this.outputStyle == OUTPUT_SEPARATE || this.outputStyle == OUTPUT_POI) {
	    outputToDir();
	} else {
	    outputToGPXFile();
	}
	if (exportErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), exportErrors + " errors during export. Check log for details.").wait(FormBase.OKB);
	}
    }

    private void outputToDir() {
	final Hashtable fileHandles = new Hashtable();
	final String outDir = exportOptions.getGpxOutputTo();
	final String tempDir;
	final String baseDir = FileBase.getProgramDirectory();
	final String prefix = exportOptions.getPrefix();
	ZipFile poiZip = null;

	if (!garminMap.exists) {
	    Preferences.itself().log("GPX Export: unable to load garminmap.xml", null);
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "unable to load garminmap.xml").wait(FormBase.OKB);
	    return;
	}

	new File(outDir).mkdir();
	if (outputStyle == OUTPUT_POI) {
	    // hier werden erstmal die gpx erzeugt
	    tempDir = baseDir + FileBase.separator + "GPXExporterNG.tmp";
	    new File(tempDir).mkdir();
	} else {
	    // OUTPUT_SEPARATE die gpx werden gleich ins finale Verzeichnis geschrieben 
	    tempDir = outDir;
	    String tmp[] = new FileBugfix(tempDir).list(prefix + "*.gpx", ewe.io.FileBase.LIST_FILES_ONLY);
	    for (int i = 0; i < tmp.length; i++) {
		File tmpFile = new File(tempDir + FileBase.separator + tmp[i]);
		tmpFile.delete();
	    }
	    tmp = new FileBugfix(tempDir).list(prefix + "*.bmp", ewe.io.FileBase.LIST_FILES_ONLY);
	    for (int i = 0; i < tmp.length; i++) {
		File tmpFile = new File(tempDir + FileBase.separator + tmp[i]);
		tmpFile.delete();
	    }
	}

	ProgressBarForm pbf = new ProgressBarForm();
	int poiCounter = 0;
	int poiCategories = 0;
	try {
	    Handle h = new Handle();

	    int expCount = 0;
	    int totalCount = MainForm.profile.cacheDB.countVisible();

	    pbf.showMainTask = false;
	    pbf.setTask(h, "Exporting ...");
	    pbf.exec();

	    for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
		ch = MainForm.profile.cacheDB.get(i);
		if (!ch.isVisible()) {
		    continue;
		} else if (ch.isIncomplete()) {
		    Preferences.itself().log("skipping export of incomplete waypoint " + ch.getCode(), null);
		} else {
		    String poiId = garminMap.getPoiId(ch);
		    if (null == poiId) {
			Preferences.itself().log("GPX Export: unmatched POI ID for " + ch.getCode() + " of type " + ch.getType(), null);
			exportErrors++;
		    } else {
			PrintWriter writer;
			if (fileHandles.containsKey(poiId)) {
			    writer = (PrintWriter) fileHandles.get(poiId);
			} else {
			    writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(tempDir + FileBase.separator + prefix + poiId + ".gpx"))));
			    fileHandles.put(poiId, writer);
			    writer.print(formatHeader());
			}
			String strOut = formatCache();
			if (!strOut.equals("")) {
			    writer.print(strOut);
			}
		    }

		}
		expCount++;
		h.progress = (float) expCount / (float) totalCount;
		h.changed();
	    }

	    try {
		poiZip = new ZipFile(bitmapFileName);
	    } catch (IOException e) {
		Preferences.itself().log("GPX Export: warning GarminPOI.zip not found", e, true);
		exportErrors++;
	    }

	    if (outputStyle == OUTPUT_POI) {
		// only clean up output directory if user has chosen non empty prefix,
		// since otherwise all present POI would be deleted
		if (!prefix.equals("")) {
		    String tmp[] = new FileBugfix(outDir).list(prefix + "*.gpi", ewe.io.FileBase.LIST_FILES_ONLY);
		    for (int i = 0; i < tmp.length; i++) {
			File tmpFile = new File(outDir + FileBase.separator + tmp[i]);
			tmpFile.delete();
		    }
		}
		pbf.exit(0);
		poiCategories = fileHandles.size();
		pbf.setTask(h, "Transfer");
		pbf.exec();
	    }

	    Enumeration keys = fileHandles.keys();
	    while (keys.hasMoreElements()) {

		String key = (String) keys.nextElement();
		PrintWriter writer = (PrintWriter) fileHandles.get(key);

		writer.print("</gpx>" + newLine);
		writer.close();
		if (outputStyle == OUTPUT_POI) {
		    poiCounter++;
		    h.progress = (float) poiCounter / (float) poiCategories;
		    h.changed();
		}
		if (poiZip != null) {
		    if (!copyPoiIcon(tempDir, key, prefix, poiZip)) {
			exportErrors++;
			continue;
		    }

		    if (outputStyle == OUTPUT_POI) {
			String[] cmdStack = new String[9];
			cmdStack[0] = Preferences.itself().gpsbabel;
			cmdStack[1] = "-i";
			cmdStack[2] = "gpx";
			cmdStack[3] = "-f";
			cmdStack[4] = tempDir + FileBase.separator + prefix + key + ".gpx";
			cmdStack[5] = "-o";
			cmdStack[6] = "garmin_gpi,sleep=1,category=" + prefix + key + ",bitmap=" + tempDir + FileBase.separator + prefix + key + ".bmp";
			cmdStack[7] = "-F";
			cmdStack[8] = outDir + FileBase.separator + prefix + key + ".gpi";

			Process babelProcess = null;
			babelProcess = startProcess(cmdStack);
			StreamReader errorStream = new StreamReader(babelProcess.getErrorStream());
			babelProcess.waitFor();
			String errorMsg = errorStream.readALine();
			if (errorMsg != null) {
			    Preferences.itself().log("GPX Export: " + errorMsg, null);
			    exportErrors++;
			}
			errorStream.close();
		    }
		}
	    }

	    // temporäres Verzeichnis löschen (wird bei gpi nicht mehr gebraucht)
	    if (outputStyle == OUTPUT_POI) {
		File tmpdir = new File(tempDir);
		String tmp[] = new FileBugfix(tempDir).list(prefix + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i = 0; i < tmp.length; i++) {
		    File tmpFile = new File(tempDir + FileBase.separator + tmp[i]);
		    tmpFile.delete();
		}
		tmpdir.delete();
	    }

	    pbf.exit(0);

	} catch (Exception e) {
	    Preferences.itself().log("GPX Export: unknown cause for ", e, true);
	    exportErrors++;
	    pbf.exit(0);
	}
    }

    private void outputToGPXFile() {
	if (exportOptions.getUseCustomIcons()) {
	    if (!garminMap.exists) {
		Preferences.itself().log("unable to load garminmap.xml", null);
	    }
	}

	final File file;

	boolean sendToGarmin = exportOptions.getSendToGarmin();
	if (sendToGarmin) {
	    file = new File("");
	    file.createTempFile("gpxexport", null, null);
	} else {
	    file = new File(exportOptions.getGpxOutputTo());
	}

	Vm.showWait(true);
	InfoBox infB = new InfoBox("Info", "Exporting", InfoBox.PROGRESS_WITH_WARNINGS);
	try {
	    PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	    int expCount = 0;
	    int totalCount = MainForm.profile.cacheDB.countVisible();

	    infB.exec();
	    infB.addWarning(file.getFileExt());

	    outp.print(formatHeader());

	    Preferences.itself().log("start: " + new Time().getTime());
	    Time startZeit = new Time();
	    int oldProzent = 0;
	    for (int i = 0; i < MainForm.profile.cacheDB.size(); i++) {
		ch = MainForm.profile.cacheDB.get(i);
		if (!ch.isVisible()) {
		    continue;
		} else if (ch.isIncomplete()) {
		    exportErrors++;
		    infB.addWarning("Skipping export of incomplete waypoint " + ch.getCode());
		    Preferences.itself().log("GPX Export: skipping export of incomplete waypoint " + ch.getCode(), null);
		} else {
		    int splitSize = exportOptions.getSplitSize();
		    if (splitSize > 0) {
			if (expCount % splitSize == 0) {
			    if (expCount > 0) {
				// schliesse Ausgabedatei
				outp.print("</gpx>" + newLine);
				outp.close();
				// mache neue Ausgabedatei
				String newFileName = Common.getPathAndFilename(file.getAbsolutePath()) + (expCount / splitSize) + Common.getExtension(file.getFileExt());
				File newFile = new File(newFileName);
				outp = new PrintWriter(new BufferedWriter(new FileWriter(newFile)));
				outp.print(formatHeader());
				infB.addWarning(newFile.getFileExt());
			    }
			}
		    }
		    String strOut = formatCache();
		    if (!strOut.equals("")) {
			outp.print(strOut);
			expCount++;
		    }
		}
		Time endZeit = new Time();
		long benoetigteZeit = (endZeit.getTime() - startZeit.getTime()) / 1000; // sec
		if (benoetigteZeit > 1) {
		    startZeit = endZeit;
		    infB.setInfo("Exporting " + expCount + " ( " + totalCount + " )");
		    infB.redisplay();
		}
		int prozent = ((expCount * 100) / totalCount);
		if (prozent != oldProzent) {
		    oldProzent = prozent;
		    MainTab.itself.tablePanel.updateStatusBar(" " + prozent + "% ");
		}

		if (infB.isClosed()) {
		    break;
		}
	    }

	    Preferences.itself().log("stop: " + new Time().getTime());
	    MainTab.itself.tablePanel.updateStatusBar("done:" + expCount);

	    outp.print("</gpx>" + newLine);
	    outp.close();
	} catch (Exception ex) {
	    exportErrors++;
	    Preferences.itself().log("GPX Export: unable to write output to " + file.toString(), ex, true);
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "unable to write output to " + file.toString()).wait(FormBase.OKB);
	    return;
	} finally {
	    infB.close(0);
	    Vm.showWait(false);
	}

	if (sendToGarmin) {
	    try {
		String[] cmdStack = new String[9];
		cmdStack[0] = Preferences.itself().gpsbabel;
		cmdStack[1] = "-i";
		cmdStack[2] = "gpx";
		cmdStack[3] = "-f";
		cmdStack[4] = file.getCreationName();
		cmdStack[5] = "-o";
		cmdStack[6] = "garmin";
		cmdStack[7] = "-F";
		cmdStack[8] = Preferences.itself().garminConn + (":");
		Process babelProcess = null;
		babelProcess = startProcess(cmdStack);
		StreamReader errorStream = new StreamReader(babelProcess.getErrorStream());
		babelProcess.waitFor();
		String errorMsg = errorStream.readALine();
		if (errorMsg != null) {
		    Preferences.itself().log("GPX Export: " + errorMsg, null);
		    exportErrors++;
		}
		errorStream.close();
	    } catch (Exception ex) {
		Preferences.itself().log("GPX Export error :", ex, true);
	    }
	    file.delete();
	}
    }

    private String formatCache() {
	// no addis or custom in MyFindsPq - and of course only finds
	if (exportStyle == STYLE_MYFINDS) {
	    if ((!ch.isFound() || ch.isCustomWpt() || ch.isAddiWpt()))
		return "";
	}

	if (!ch.getWpt().isValid()) {
	    if (!ch.isAddiWpt()) {
		Preferences.itself().log("[GPX Export:formatCache] " + ch.getCode() + " has invalid coords.");
		return "";
	    }
	    if (!exportOptions.getExportAddiWithInvalidCoords()) {
		return "";
	    }
	}

	StringBuffer ret = new StringBuffer();
	ch.getDetails();
	try {
	    ret.append(formatCompact());
	    if (exportOptions.getGPXVersion() == 1)
		ret.append("  <extensions>" + newLine);
	    if (exportStyle == STYLE_PQEXTENSIONS || exportStyle == STYLE_MYFINDS) {
		ret.append(formatPqExtensions());
	    }
	    if (exportOptions.getWithGarminExtensions()) {
		ret.append(formatGarminExtensions());
	    }
	    if (exportOptions.getWithGSAKExtensions()) {
		ret.append(formatGSAKExtensions());
	    }
	    if (exportOptions.getGPXVersion() == 1)
		ret.append("  </extensions>" + newLine);
	    ret.append("  </wpt>").append(newLine);
	} catch (IllegalArgumentException e) {
	    exportErrors++;
	    ch.checkIncomplete(); // ch.setIncomplete(true);
	    Preferences.itself().log("GPX Export: " + ch.getCode() + " check incomplete ", e, true);
	    return "";
	} catch (Exception e) {
	    exportErrors++;
	    Preferences.itself().log("GPX Export: " + ch.getCode() + " caused ", e, true);
	    return "";
	}

	return ret.toString();
    }

    private String formatCompact() {

	StringBuffer ret = new StringBuffer();

	if (ch.getWpt().isValid())
	    ret.append("  <wpt lat=\"" + ch.getWpt().getLatDeg(TransformCoordinates.DD) + "\" lon=\"" + ch.getWpt().getLonDeg(TransformCoordinates.DD) + "\">").append(newLine);
	else
	    ret.append("  <wpt lat=\"" + "0" + "\" lon=\"" + "0" + "\">").append(newLine);

	if (exportStyle == STYLE_PQEXTENSIONS || exportStyle == STYLE_MYFINDS) {
	    if (ch.isAddiWpt()) {
		try {
		    ret.append("    <time>" + ch.mainCache.getHidden() + "T07:00:00Z</time>").append(newLine);
		} catch (Exception e) {
		    Preferences.itself().log(ch.getCode() + " has no parent", null);
		    exportErrors++;
		    ret.append("    <time>1970-01-01T19:00:00Z</time>").append(newLine);
		}
	    } else if (ch.isCustomWpt()) {
		ret.append("    <time>1970-01-01T19:00:00Z</time>").append(newLine);
	    } else {
		ret.append("    <time>" + ch.getHidden() + "T19:00:00Z</time>").append(newLine);
	    }
	}

	if (exportOptions.getWpNameStyle() == WPNAME_ID_SMART) {
	    if (ch.isAddiWpt()) {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.mainCache.getCode())).append(" ").append(ch.getCode().substring(0, 2)).append("</name>").append(newLine);
	    } else if (ch.isCustomWpt()) {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.getCode())).append("</name>").append(newLine);
	    } else {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.getCode()))//
			.append(" ")//
			.append(CacheType.getExportShortId(ch.getType()))//
			.append(String.valueOf(ch.getDifficulty()))//
			.append(String.valueOf(ch.getTerrain()))//
			.append(CacheSize.getExportShortId(ch.getSize()))//
			.append(String.valueOf(ch.getNoFindLogs()))//
			.append("</name>").append(newLine);
	    }
	} else { // WPNAME_ID_CLASSIC
	    ret.append("    <name>").append(SafeXML.cleanGPX(ch.getCode())).append("</name>").append(newLine);
	}

	// no <cmt> for custom
	if (!ch.isCustomWpt()) {
	    // no <cmt> in PQs / ?Myfinds
	    if (ch.isCacheWpt()) {
		if (exportStyle == STYLE_COMPACT) {
		    ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getName()));
		    ret.append("&lt;br /&gt;" + SafeXML.cleanGPX(Common.rot13(ch.getDetails().Hints)));
		    ret.append("&lt;br /&gt;" + SafeXML.cleanGPX(ch.getDetails().LongDescription));
		    ret.append("</cmt>").append(newLine);
		}
	    } else {
		ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getDetails().LongDescription)).append("</cmt>").append(newLine);
	    }
	}

	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    ret.append("    <desc>").append(SafeXML.cleanGPX(ch.getName())).append("</desc>").append(newLine);
	} else {
	    ret.append("    <desc>").append(SafeXML.cleanGPX(ch.getName()))//
		    .append(" by ")//
		    .append(SafeXML.cleanGPX(ch.getOwner()))//
		    .append(", ")//
		    .append(CacheType.type2GSTypeTag(ch.getType()))//
		    .append(" (")//
		    .append(CacheTerrDiff.shortDT(ch.getDifficulty()))//
		    .append("/")//
		    .append(CacheTerrDiff.shortDT(ch.getTerrain()))//
		    .append(")")//
		    .append("</desc>").append(newLine);
	}

	if (exportStyle == STYLE_PQEXTENSIONS || exportStyle == STYLE_MYFINDS) {
	    if (ch.isCacheWpt()) {
		if (!ch.isCustomWpt()) {
		    ret.append("    <url>").append(ch.getDetails().URL).append("</url>").append(newLine);
		    ret.append("    <urlname>").append(SafeXML.cleanGPX(ch.getName())).append("</urlname>").append(newLine);
		}
	    }
	}

	if (exportOptions.getGPXVersion() == 1) {
	    // link - tag
	    CacheImages images = ch.getDetails().images.getDisplayImages(ch.getCode());
	    if (ch.isCacheWpt()) {
		if (!ch.isCustomWpt()) {
		    for (int i = 0; i < images.size(); i++) {
			String filename = images.get(i).getFilename();
			String url = MainForm.profile.dataDir + filename;
			// POILoader can only work with JPG-Files
			if (!filename.endsWith(".jpg"))
			    continue;
			// check if the file is not deleted
			if (!(new File(url)).exists())
			    continue;
			ret.append("    <link ");
			String comment = images.get(i).getTitle();
			ret.append("text=\"" + SafeXML.cleanGPX(comment) + "\" ");
			ret.append("href=\"" + URLUTF8Encoder.encode(url, false) + "\"").append("/>").append(newLine);
		    }
		}
	    }
	}

	if (exportOptions.getUseCustomIcons()) {
	    ret.append("    <sym>").append(garminMap.getIcon(ch)).append("</sym>").append(newLine);
	} else {
	    if (ch.isAddiWpt()) {
		ret.append("    <sym>").append(CacheType.type2SymTag(ch.getType())).append("</sym>").append(newLine);
	    } else if (ch.isCustomWpt()) {
		ret.append("    <sym>Custom</sym>").append(newLine);
	    } else if (ch.isFound()) {
		ret.append("    <sym>Geocache Found</sym>").append(newLine);
	    } else {
		ret.append("    <sym>Geocache</sym>").append(newLine);
	    }
	}

	if (exportStyle == STYLE_PQEXTENSIONS || exportStyle == STYLE_MYFINDS) {
	    ret.append("    <type>").append(CacheType.type2TypeTag(ch.getType())).append("</type>").append(newLine);
	}

	return ret.toString();
    }

    /**
     * format gc.com extended cache information as found in a PQ
     * 
     * @return formatted cache information for cache waypoints or emty string for all other waypoints (additional / custom)
     */
    private String formatPqExtensions() {

	// no details for addis or custom waypoints
	// if (ch.isCustomWpt() || ch.isAddiWpt())
	if (ch.isAddiWpt())
	    return "";
	StringBuffer ret = new StringBuffer();
	ret.append("    <groundspeak:cache id=\"").append(ch.getCacheID())//
		.append("\" available=\"").append(ch.isAvailable() ? TRUE : FALSE)//
		.append("\" archived=\"").append(ch.isArchived() ? TRUE : FALSE)//
		.append("\" xmlns:groundspeak=\"" + xmlnspq + "\">").append(newLine)//
		.append("      <groundspeak:name>").append(SafeXML.cleanGPX(ch.getName())).append("</groundspeak:name>").append(newLine)//
		.append("      <groundspeak:placed_by>").append(SafeXML.cleanGPX(ch.getOwner())).append("</groundspeak:placed_by>").append(newLine)//
		.append("      <groundspeak:owner id=\"").append("31415").append("\">").append(SafeXML.cleanGPX(ch.getOwner())).append("</groundspeak:owner>").append(newLine)//
		.append("      <groundspeak:type>").append(CacheType.type2GSTypeTag(ch.getType())).append("</groundspeak:type>").append(newLine)//
		.append("      <groundspeak:container>").append(CacheSize.cw2ExportString(ch.getSize())).append("</groundspeak:container>").append(newLine)//
		.append("      <groundspeak:attributes>").append(newLine)//
		.append(formatAttributes())// ab pq Version 1/0/1
		.append("      </groundspeak:attributes>").append(newLine)//
		.append("      <groundspeak:difficulty>").append(CacheTerrDiff.shortDT(ch.getDifficulty())).append("</groundspeak:difficulty>").append(newLine)//
		.append("      <groundspeak:terrain>").append(CacheTerrDiff.shortDT(ch.getTerrain())).append("</groundspeak:terrain>").append(newLine)//
		.append("      <groundspeak:country>").append(SafeXML.cleanGPX(ch.getDetails().Country)).append("</groundspeak:country>").append(newLine)//
		.append("      <groundspeak:state>").append(SafeXML.cleanGPX(ch.getDetails().State)).append("</groundspeak:state>").append(newLine)//
		.append("      <groundspeak:short_description html=\"").append(ch.isHTML() ? TRUE : FALSE).append("\"></groundspeak:short_description>").append(newLine)//
		.append("      <groundspeak:long_description html=\"").append(ch.isHTML() ? TRUE : FALSE).append("\">").append(SafeXML.cleanGPX(formatLongDescription())).append("</groundspeak:long_description>").append(newLine)//
		.append("      <groundspeak:encoded_hints>").append(SafeXML.cleanGPX(Common.rot13(ch.getDetails().Hints))).append("</groundspeak:encoded_hints>").append(newLine)//
		.append("      <groundspeak:logs>").append(newLine)//
		.append(formatLogs())//
		.append("      </groundspeak:logs>").append(newLine)//
		.append("      <groundspeak:travelbugs>").append(newLine)//
		.append(formatTbs())//
		.append("      </groundspeak:travelbugs>").append(newLine)//
		.append("    </groundspeak:cache>").append(newLine);//
	return ret.toString();
    }

    private String formatGSAKExtensions() {
	if (ch.isAddiWpt())
	    return "";
	StringBuffer ret_____ = new StringBuffer();
	ret_____.append("    <gsak:wptExtension xmlns:gsak=\"" + xmlnsgsak + "\">").append(newLine); //
	if (ch.hasNote()) // eigentlich  nur die von GC, aber
	    ret_____.append("      <gsak:GcNote>").append(SafeXML.cleanGPX(ch.getDetails().getGCNotes())).append("</gsak:GcNote>").append(newLine); //
	if (ch.isSolved()) {
	    // wir kennen die OriginalKoordinaten nicht, aber es gibt wohl nichts für nur corrected coordinates
	    ret_____.append("      <gsak:LatBeforeCorrect>").append(ch.getWpt().getLatDeg(TransformCoordinates.DD)).append("</gsak:LatBeforeCorrect>").append(newLine) //
		    .append("      <gsak:LonBeforeCorrect>").append(ch.getWpt().getLonDeg(TransformCoordinates.DD)).append("</gsak:LonBeforeCorrect>").append(newLine); //
	}
	if (Preferences.itself().useGCFavoriteValue)
	    ret_____.append("      <gsak:FavPoints>").append("" + ch.getNumRecommended()).append("</gsak:FavPoints>").append(newLine); //
	ret_____.append("      <gsak:IsPremium>").append(SafeXML.strxmlencode(ch.isPMCache())).append("</gsak:IsPremium>").append(newLine); //
	// ret_____.append("      <gsak:CacheImages>").append("").append("</gsak:CacheImages>").append(newLine) // replace "" by format spoilers
	ret_____.append("    </gsak:wptExtension>").append(newLine);//
	return ret_____.toString();
    }

    private String formatGarminExtensions() {
	StringBuffer ret_____ = new StringBuffer();
	ret_____.append("    <gpxx:wptExtension xmlns:gpxx=\"" + xmlnsgpxx + "\">").append(newLine); //
	ret_____.append("      <gpxx:DisplayMode>").append("SymbolAndName").append("</gpxx:DisplayMode>").append(newLine); //
	ret_____.append("    </gpxx:wptExtension>").append(newLine);//
	return ret_____.toString();
    }

    /*
    private String formatCacheboxExtensions() {
    if (ch.isAddiWpt())
        return "";
    StringBuffer ret_____ = new StringBuffer();
    //cachebox-extension
    // /note
    // /solver
    // /clue = bei Wegpunkten Beschreibung 
    // /Parent = bei Wegpunkten - GCxxxx
    return ret_____.toString();
    }
    */

    private String formatTbs() {
	StringBuffer ret = new StringBuffer();
	Travelbug Tb;
	for (int i = 0; i < ch.getDetails().Travelbugs.size(); i++) {
	    Tb = ch.getDetails().Travelbugs.getTB(i);
	    ret.append("        <groundspeak:travelbug id=\"").//
		    append(Integer.toString(i)).//
		    append("\" ref=\"TB\">").//
		    // append(newLine).//
		    // append(" <groundspeak:name>").//
		    append("<groundspeak:name>").//
		    append(SafeXML.cleanGPX(Tb.getName())).//
		    append("</groundspeak:name>").//
		    // append(newLine).//
		    // append(" </groundspeak:travelbug>\r\n");//
		    append("</groundspeak:travelbug>\r\n");//
	}
	return ret.toString();
    }

    /**
     *
     */
    private String formatAttributes() {
	StringBuffer ret = new StringBuffer();
	Attribute attrib;
	for (int i = 0; i < ch.getDetails().attributes.count(); i++) {
	    // <groundspeak:attribute id="X" inc="Y">text für X</groundspeak:attribute>
	    attrib = ch.getDetails().attributes.getAttribute(i);
	    if (attrib.getGCId().length() > 0) {
		ret.append("        <groundspeak:attribute id=\"").//
			append(attrib.getGCId()).//
			append("\" inc=\"").//
			append(attrib.getInc()).//
			append("\">").//
			append(attrib.getGCText()).//
			append("</groundspeak:attribute>").//
			append(newLine);//
	    }
	}
	return ret.toString();
    }

    private String formatLogs() {
	theLogs.setLength(0);

	if (exportStyle == STYLE_MYFINDS) {
	    if (ch.isFound()) {
		CacheHolderDetail chD = ch.getDetails();
		// perhaps there is no Ownlog yet
		if (chD.OwnLog == null) {
		    Preferences.itself().log(chD.getParent().getCode() + " missing own Log", null);
		    return "";
		} else {
		    addLog(chD.OwnLog);
		}
	    }
	} else { // it is PQ
	    LogList logs = ch.getDetails().CacheLogs;

	    int exportlogs;
	    if (maxLogs < logs.size()) {
		if (maxLogs == -1)
		    exportlogs = logs.size();
		else
		    exportlogs = maxLogs;
	    } else {
		exportlogs = logs.size();
	    }

	    final String cacheID = ch.getCacheID();

	    // with a special log
	    addLog(createAttrLog(exportlogs));

	    // don't export the "dummy" lastLog (possibly accidently no Log.MAXLOGICON set, so check if empty)
	    if (exportlogs > 0) {
		Log lastLog = logs.getLog(exportlogs - 1);
		if (lastLog.getIcon().equals(Log.MAXLOGICON))
		    exportlogs = exportlogs - 1;
		else if (lastLog.getIcon().length() == 0)
		    exportlogs = exportlogs - 1;
	    }
	    int anzOwnLogs = 0;
	    // CW doesn't save the LogID (upto version ~1.3.3394). 
	    // So we generate one by ch.GetCacheID() + Integer.toString(i)
	    for (int i = 0; i < exportlogs; i++) {
		Log theLog = logs.getLog(i);
		String logID = theLog.getLogID();
		if (logID.length() == 0) {
		    theLog.setLogID(cacheID + Integer.toString(i));
		}
		addLog(theLog);
		if (theLog.isOwnLog()) {
		    anzOwnLogs = anzOwnLogs + 1;
		    if (anzOwnLogs > 1) {
			Preferences.itself().log("doppelter eigener Fund" + ch.getCode(), null);
		    }
		}
	    }
	}
	return theLogs.toString();
    }

    private Log createAttrLog(int exportlogs) {
	StringBuffer logText = new StringBuffer();
	if (attrib2Log) {
	    for (int i = 0; i < ch.getDetails().attributes.count(); i++) {
		Attribute attrib = ch.getDetails().attributes.getAttribute(i);
		logText.append(attrib.getInc() == 1 ? "Yes: " : "No: ").append(attrib.getMsg()).append("<br />").append(newLine);
	    }
	}

	if (ch.isSolved()) {
	    logText.append(MyLocale.getMsg(362, "solved"));
	}

	if (ch.hasNote()) {
	    logText.append(SafeXML.cleanGPX(ch.getDetails().getCacheNotes())).append("<br />").append(newLine);
	}

	/*
	if (!ch.getLastSync().equals(""))
	    logText.append(MyLocale.getMsg(1051, "Last sync date") + ": " + DateFormat.formatLastSyncDate(ch.getLastSync(), "")).append(newLine);
	*/
	if (logText.length() > 0) {
	    Log log = new Log(ch.getCacheID() + Integer.toString(exportlogs), "-2", "icon_note.gif", DateFormat.yyyyMMddHHmmss2gpxLogdate(ch.getLastSync()), "CacheWolf", logText.toString());
	    return log;
	} else
	    return null;
    }

    private void addLog(Log log) {
	if (log == null)
	    return;
	String logMessage = log.getMessage();

	if (exportOptions.getExportLogsAsPlainText()) {
	    logMessage = removeHTMLTags.replaceAll(handleLinebreaks.replaceAll(logMessage));
	}

	logMessage = removeNumericEntities.replaceAll(logMessage);

	Transformer replacePlaceholder = new Transformer(true);
	replacePlaceholder.add(new Regex("@@LOGID@@", log.getLogID()));
	replacePlaceholder.add(new Regex("@@LOGDATE@@", log.getDate()));
	replacePlaceholder.add(new Regex("@@LOGTYPE@@", log.icon2GPXType()));
	replacePlaceholder.add(new Regex("@@LOGFINDERID@@", log.getFinderID()));
	replacePlaceholder.add(new Regex("@@LOGFINDER@@", SafeXML.cleanGPX(log.getLogger())));
	replacePlaceholder.add(new Regex("@@LOGENCODE@@", ""));
	replacePlaceholder.add(new Regex("@@LOGTEXT@@", SafeXML.cleanGPX(logMessage)));
	theLogs.append(replacePlaceholder.replaceAll(GPXLOG));
    }

    private String formatHeader() {
	// FIXME: extend MainForm.profile to add <bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\" maxlon=\"6.918817\" />
	// MainForm.profile.getSourroundingArea(false);
	//http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd
	// GPX
	String gpx = "";
	String gpxVersion;
	switch (exportOptions.getGPXVersion()) {
	case 0:
	    gpxVersion = "1/0"; //ohne das <extensions> tag
	    break;
	default:
	    gpxVersion = "1/1"; //mit <extensions> tag
	}
	xmlnsgpx = STRreplace.replace(XMLNSGPX, "@", gpxVersion);
	gpx = xmlnsgpx + " " + xmlnsgpx + "/gpx.xsd";
	// PQ Groundspeak
	String pq = "";
	String pqVersion;
	/*
	switch (exportOptions.getPQVersion()) {
	case 0:
	    pqVersion = "1/0";
	    break;
	case 2:
	    pqVersion = "1/1";
	    break;
	default:
	    pqVersion = "1/0/1";
	}
	*/
	pqVersion = "1/0/1";
	xmlnspq = STRreplace.replace(XMLNSPQ, "@", pqVersion);
	pq = xmlnspq + " " + xmlnspq + "/cache.xsd";
	// GSAK
	String gsak = "";
	String gsakVersion = "6";
	xmlnsgsak = STRreplace.replace(XMLNSGSAK, "@", gsakVersion);
	if (this.exportOptions.getWithGSAKExtensions()) {
	    gsak = xmlnsgsak + " " + xmlnsgsak + "/gsak.xsd";
	}
	// Garmin gpxx
	String gpxx = "";
	String gpxxVersion = "3";
	xmlnsgpxx = STRreplace.replace(XMLNSGPXX, "@", gpxxVersion);
	if (this.exportOptions.getWithGarminExtensions()) {
	    gpxx = xmlnsgpxx + " " + xmlnsgpxx + ".xsd"; //GpxExtensionsv3.xsd
	}

	Transformer trans = new Transformer(true);
	if (exportStyle == STYLE_MYFINDS) {
	    trans.add(new Regex("@@NAME@@", "My Finds Pocket Query"));
	} else {
	    trans.add(new Regex("@@NAME@@", "Waypoints for Cache Listings, Generated by CacheWolf"));
	}
	trans.add(new Regex("@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString()));
	trans.add(new Regex("@@GPX@@", gpx));
	trans.add(new Regex("@@PQEXTENSION@@", pq));
	trans.add(new Regex("@@GSAKEXTENSION@@", gsak));
	trans.add(new Regex("@@GPXXEXTENSION@@", gpxx));
	return trans.replaceFirst(GPXHEADER);
    }

    /**
     * format a long description as found in the gc.com GPX files
     * 
     * @return formatted output
     */
    private String formatLongDescription() {
	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    return ch.getDetails().LongDescription;
	} else {
	    StringBuffer ret = new StringBuffer();
	    String delim = "";
	    ret.append(ch.getDetails().LongDescription);
	    if (ch.isHTML()) {
		delim = "<br />";
	    } else {
		delim = newLine;
	    }
	    // FIXME: format is not quite right yet
	    // FIXME: cut Addis off in GPXimporter otherwise people who use GPX to feed CacheWolf have them doubled
	    if (ch.addiWpts.size() > 0 && exportStyle != STYLE_MYFINDS) {
		if (ch.isHTML()) {
		    ret.append(newLine).append(newLine).append("<p>Additional Waypoints</p>");
		} else {
		    ret.append(newLine).append(newLine).append("Additional Waypoints").append(newLine);
		}

		Iterator iter = ch.addiWpts.iterator();
		while (iter.hasNext()) {
		    CacheHolder addi = (CacheHolder) iter.next();
		    Transformer trans = new Transformer(true);
		    trans.add(new Regex("@@ADDIID@@", addi.getCode()));
		    trans.add(new Regex("@@ADDISHORT@@", addi.getName()));
		    trans.add(new Regex("@@ADDIDELIM@@", delim));
		    trans.add(new Regex("@@ADDILAT@@", formatAddiLatLon(addi.getWpt())));
		    trans.add(new Regex("@@ADDILON@@", ""));
		    trans.add(new Regex("@@ADDILONG@@", addi.getDetails().LongDescription));
		    ret.append(trans.replaceAll(GPXADDIINMAIN));
		}
		ret.append(delim).append(newLine);
	    }
	    return ret.toString();
	}
    }

    /**
     * create a position information suitable for a gc.com PQlike export
     * 
     * @param pos
     *            position
     * @return if position is valid return the cachewolf formatted position, otherwise return teh string used in PQs
     */
    private String formatAddiLatLon(CWPoint pos) {
	if (pos.isValid()) {
	    return pos.toString();
	} else {
	    return "N/S  __ ° __ . ___ W/E ___ ° __ . ___";
	}
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
    boolean copyPoiIcon(String outdir, String type, String prefix, ZipFile poiZip) {
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

    /**
     * Execute the command defined by cmd
     * 
     * @param cmd
     *            command and options to execute. if command or options include a space quatation marks are added. this will not wirk with the java version on unix systems
     * @return a handle to the process on success or null otherwise
     */
    Process startProcess(String[] cmd) {
	String command = "";
	if (cmd.length == 0) {
	    exportErrors++;
	    Preferences.itself().log("GPX Export: empty gpsbabel command", null);
	    return null;
	}

	for (int i = 0; i < cmd.length; i++) {
	    if (cmd[i].indexOf(" ") > -1) {
		cmd[i] = "\"" + cmd[i] + "\"";
	    }
	    command = command + cmd[i] + " ";
	}

	try {
	    return Vm.exec(command);
	} catch (IOException e) {
	    Preferences.itself().log("error excuting " + command, e, true);
	    exportErrors++;
	    return null;
	}
    }

    /**
     * dialog to set the GPX exporter options
     */
    private class GpxExportNgForm extends Form {

	private mLabel lblGPXVersion, lblWithGSAKExtensions, lblWithGarminExtensions, lblWpNameStyle, lblAddiWithInvalidCoords, lblSplitSize, lblUseCustomIcons, lblSendToGarmin, lblMaxLogs, lblExportLogsAsPlainText, lblAttrib2Log, lblPrefix;
	private int gpxStyle, outputStyle, gpxVersion;
	private mCheckBox cbWithGSAKExtensions, cbWithGarminExtensions, cbUseCustomIcons, cbSendToGarmin, cbAddiWithInvalidCoords, cbAttrib2Log, cbExportLogsAsPlainText;
	private mInput ibMaxLogs, ibSplitSize, ibPrefix, ibFilename;
	private mChoice chStyle, chOutput, chGPXVersion, chWpNameStyle;
	private mButton btnFilename;
	private final ExecutePanel executePanel;

	private boolean hasIcons;
	private boolean hasGarminMap;
	private boolean isGpsBabelInstalled;

	private Hashtable exportValues;

	/**
	 * set up the form / dialog
	 */
	private GpxExportNgForm(boolean _hasGarminMap, boolean _hasBitmaps, boolean _hasGpsbabel) {

	    this.setTitle("GPX Export");
	    this.resizable = false;

	    gpxStyle = MainForm.profile.getProfilesLastUsedGpxStyle();
	    outputStyle = MainForm.profile.getProfilesLastUsedOutputStyle();

	    hasIcons = _hasBitmaps;
	    hasGarminMap = _hasGarminMap;
	    isGpsBabelInstalled = _hasGpsbabel;

	    lblGPXVersion = new mLabel(MyLocale.getMsg(2022, "GPX Version"));
	    addNext(lblGPXVersion);
	    chGPXVersion = new mChoice();
	    chGPXVersion.dontSearchForKeys = true;
	    chGPXVersion.addItem("1.0"); // index 0
	    chGPXVersion.addItem("1.1"); // index 1
	    chGPXVersion.select(gpxVersion);
	    addLast(chGPXVersion);

	    addNext(new mLabel(MyLocale.getMsg(2013, "With Groundspeak Extensions")));
	    chStyle = new mChoice();
	    chStyle.dontSearchForKeys = true;
	    // if you change the order of strings make sure to fix the event handler as well
	    chStyle.addItem(MyLocale.getMsg(2004, "No")); // index 0
	    chStyle.addItem(MyLocale.getMsg(2005, "PQ like")); // index 1
	    chStyle.addItem(MyLocale.getMsg(2006, "MyFinds")); // index 2
	    chStyle.select(gpxStyle);
	    addLast(chStyle);

	    addNext(lblWithGarminExtensions = new mLabel(MyLocale.getMsg(2025, "With Garmin Extensions")));
	    cbWithGarminExtensions = new mCheckBox(" ");
	    addLast(cbWithGarminExtensions);

	    addNext(lblWithGSAKExtensions = new mLabel(MyLocale.getMsg(2023, "With GSAK Extensions")));
	    cbWithGSAKExtensions = new mCheckBox(" ");
	    addLast(cbWithGSAKExtensions);

	    addNext(lblWpNameStyle = new mLabel(MyLocale.getMsg(2014, "WP Names")));
	    chWpNameStyle = new mChoice();
	    chWpNameStyle.dontSearchForKeys = true;
	    // if you change the order of strings make sure to fix the event handler as well
	    chWpNameStyle.addItem(MyLocale.getMsg(2002, "keep")); // index 0
	    chWpNameStyle.addItem(MyLocale.getMsg(2003, "add details")); // index 1
	    // chIds.addItem(MyLocale.getMsg(31415,"Smart Names")); // index 2
	    addLast(chWpNameStyle);

	    addNext(lblSplitSize = new mLabel(MyLocale.getMsg(2019, "SplitSize")));
	    ibSplitSize = new mInput("");
	    addLast(ibSplitSize);

	    addNext(lblAddiWithInvalidCoords = new mLabel(MyLocale.getMsg(2020, "Export Addis without coordinates?")));
	    cbAddiWithInvalidCoords = new mCheckBox(" ");
	    addLast(cbAddiWithInvalidCoords);

	    addNext(lblUseCustomIcons = new mLabel(MyLocale.getMsg(2012, "Custom Icons")));
	    cbUseCustomIcons = new mCheckBox(" ");
	    addLast(cbUseCustomIcons);

	    addNext(lblSendToGarmin = new mLabel(MyLocale.getMsg(2011, "send to Garmin")));
	    cbSendToGarmin = new mCheckBox(" ");
	    addLast(cbSendToGarmin);

	    addNext(lblExportLogsAsPlainText = new mLabel(MyLocale.getMsg(2010, "HTML - Tags aus Logs entfernen")));
	    cbExportLogsAsPlainText = new mCheckBox(" ");
	    addLast(cbExportLogsAsPlainText);

	    addNext(lblAttrib2Log = new mLabel(MyLocale.getMsg(2017, "Attrib.->Log")));
	    cbAttrib2Log = new mCheckBox(" ");
	    addLast(cbAttrib2Log);

	    addNext(lblMaxLogs = new mLabel(MyLocale.getMsg(2018, "Max Logs")));
	    ibMaxLogs = new mInput("");
	    addLast(ibMaxLogs);

	    addNext(new mLabel(MyLocale.getMsg(2024, "Output Style")));
	    chOutput = new mChoice();
	    chOutput.dontSearchForKeys = true;
	    // if you change the order of strings make sure to fix the event handler as well
	    chOutput.addItem(MyLocale.getMsg(2007, "Single GPX")); // index 0
	    chOutput.addItem(MyLocale.getMsg(2008, "Separate GPX")); // index 1
	    chOutput.addItem(MyLocale.getMsg(2009, "POI")); // index 2
	    chOutput.select(outputStyle);
	    addLast(chOutput);

	    addNext(lblPrefix = new mLabel(MyLocale.getMsg(2016, "Prefix")));
	    ibPrefix = new mInput("");
	    addLast(ibPrefix);

	    addNext(btnFilename = new mButton(MyLocale.getMsg(2021, "Ausgabedatei") + " ... "));
	    ibFilename = new mInput("");
	    addLast(ibFilename);
	    disable(new mLabel(""), ibFilename);

	    executePanel = new ExecutePanel(this);

	    checkStyle();
	}

	private void checkStyle() {
	    this.gpxStyle = this.chStyle.selectedIndex;
	    this.outputStyle = this.chOutput.selectedIndex;
	    exportValues = Preferences.itself().getGpxExportPreferences(Preferences.itself().gpxStyles[gpxStyle]);
	    setFromPreferences();
	    if (gpxStyle == STYLE_MYFINDS) {
		disable(lblGPXVersion, chGPXVersion);
		chWpNameStyle.select(0);
		disable(lblWpNameStyle, chWpNameStyle);
		disable(lblWithGarminExtensions, cbWithGarminExtensions);
		disable(lblWithGSAKExtensions, cbWithGSAKExtensions);
		disable(lblAddiWithInvalidCoords, cbAddiWithInvalidCoords);
		cbAddiWithInvalidCoords.setState(false);
		disable(lblUseCustomIcons, cbUseCustomIcons);
		cbUseCustomIcons.setState(false);
		disable(lblSendToGarmin, cbSendToGarmin);
		cbSendToGarmin.setState(false);
		enable(lblExportLogsAsPlainText, cbExportLogsAsPlainText);
		disable(lblMaxLogs, ibMaxLogs);
		disable(lblAttrib2Log, cbAttrib2Log);
		cbAttrib2Log.setState(false);
		disable(lblSplitSize, ibSplitSize);
		disable(lblPrefix, ibPrefix);
		outputStyle = OUTPUT_SINGLE;
	    } else if (gpxStyle == STYLE_PQEXTENSIONS) {
		enable(lblGPXVersion, chGPXVersion);
		enable(lblWpNameStyle, chWpNameStyle);
		enable(lblWithGarminExtensions, cbWithGarminExtensions);
		enable(lblWithGSAKExtensions, cbWithGSAKExtensions);
		enable(lblAddiWithInvalidCoords, cbAddiWithInvalidCoords);
		enable(lblSplitSize, ibSplitSize);
		if (hasGarminMap)
		    enable(lblUseCustomIcons, cbUseCustomIcons);
		else {
		    disable(lblUseCustomIcons, cbUseCustomIcons);
		    cbUseCustomIcons.setState(false);
		}
		if (isGpsBabelInstalled)
		    enable(lblSendToGarmin, cbSendToGarmin);
		else {
		    disable(lblSendToGarmin, cbSendToGarmin);
		    cbSendToGarmin.setState(false);
		}
		enable(lblExportLogsAsPlainText, cbExportLogsAsPlainText);
		enable(lblMaxLogs, ibMaxLogs);
		enable(lblAttrib2Log, cbAttrib2Log);
		disable(lblPrefix, ibPrefix);
	    } else { // without Groundspeak extension
		enable(lblGPXVersion, chGPXVersion);
		enable(lblWpNameStyle, chWpNameStyle);
		enable(lblWithGarminExtensions, cbWithGarminExtensions);
		enable(lblWithGSAKExtensions, cbWithGSAKExtensions);
		disable(lblAddiWithInvalidCoords, cbAddiWithInvalidCoords);
		cbAddiWithInvalidCoords.setState(false);
		disable(lblSplitSize, ibSplitSize);
		if (hasGarminMap)
		    enable(lblUseCustomIcons, cbUseCustomIcons);
		else {
		    disable(lblUseCustomIcons, cbUseCustomIcons);
		    cbUseCustomIcons.setState(false);
		}
		disable(lblExportLogsAsPlainText, cbExportLogsAsPlainText);
		cbExportLogsAsPlainText.setState(false);
		disable(lblMaxLogs, ibMaxLogs);
		disable(lblAttrib2Log, cbAttrib2Log);
	    }

	    if (outputStyle == OUTPUT_SINGLE) {
		if (isGpsBabelInstalled)
		    enable(lblSendToGarmin, cbSendToGarmin);
		else {
		    disable(lblSendToGarmin, cbSendToGarmin);
		    cbSendToGarmin.setState(false);
		}
		disable(lblPrefix, ibPrefix);
	    } else if (outputStyle == OUTPUT_SEPARATE) {
		disable(lblSendToGarmin, cbSendToGarmin);
		cbSendToGarmin.setState(false);
		if (hasIcons)
		    enable(lblUseCustomIcons, cbUseCustomIcons);
		else {
		    disable(lblUseCustomIcons, cbUseCustomIcons);
		    cbUseCustomIcons.setState(false);
		}
		enable(lblPrefix, ibPrefix);
	    } else if (outputStyle == OUTPUT_POI) {
		disable(lblUseCustomIcons, cbUseCustomIcons);
		cbUseCustomIcons.setState(false);
		disable(lblSendToGarmin, cbSendToGarmin);
		cbSendToGarmin.setState(false);
		enable(lblPrefix, ibPrefix);
	    }
	}

	private void disable(mLabel l, Control c) {
	    if (l.change(ControlConstants.Disabled, 0))
		l.repaint();
	    if (c.change(ControlConstants.Disabled, 0))
		c.repaint();
	}

	private void enable(mLabel l, Control c) {
	    if (l.change(0, ControlConstants.Disabled))
		l.repaint();
	    if (c.change(0, ControlConstants.Disabled))
		c.repaint();
	}

	private void setFromPreferences() {
	    chWpNameStyle.select(Common.parseInt(getExportValue(WPNAMESTYLE)));
	    chGPXVersion.select(Common.parseInt(getExportValue(GPXVERSION)));
	    cbWithGarminExtensions.setState(Boolean.valueOf(getExportValue(WITHGARMINEXTENSIONS)).booleanValue());
	    cbWithGSAKExtensions.setState(Boolean.valueOf(getExportValue(WITHGSAKEXTENSIONS)).booleanValue());
	    int splitSize = Common.parseInt(getExportValue(SPLITSIZE));
	    ibSplitSize.setText((splitSize < 1) ? "" : String.valueOf(splitSize));
	    cbAddiWithInvalidCoords.setState(Boolean.valueOf(getExportValue(EXPORTADDIWITHINVALIDCOORDS)).booleanValue());
	    cbUseCustomIcons.setState(Boolean.valueOf(getExportValue(USECUSTOMICONS)).booleanValue());
	    cbSendToGarmin.setState(Boolean.valueOf(getExportValue(SENDTOGARMIN)).booleanValue());
	    cbExportLogsAsPlainText.setState(Boolean.valueOf(getExportValue(EXPORTLOGASPLAINTEXT)).booleanValue());
	    cbAttrib2Log.setState(Boolean.valueOf(getExportValue(ATTRIB2LOG)).booleanValue());
	    String strMaxNumberOfLogsToExport = getExportValue(MAXNUMBEROFLOGSTOEXPORT);
	    int maxNumberOfLogsToExport = (strMaxNumberOfLogsToExport.length() == 0) ? 5 : Common.parseInt(strMaxNumberOfLogsToExport);
	    ibMaxLogs.setText((maxNumberOfLogsToExport == -1) ? "" : String.valueOf(maxNumberOfLogsToExport));
	    ibPrefix.setText(getExportValue(PREFIX));
	    if (ibPrefix.getText().length() == 0)
		ibPrefix.setText("GC-"); //default	
	    ibFilename.setText(MainForm.profile.getGpxOutputTo());
	}

	private String getExportValue(String item) {
	    if (exportValues == null)
		return "";
	    String ret = (String) exportValues.get(item);
	    if (ret == null) {
		return "";
	    } else {
		return ret;
	    }
	}

	public int getGpxStyle() {
	    return chStyle.selectedIndex;
	}

	public int getOutputStyle() {
	    return chOutput.selectedIndex;
	}

	public boolean getSendToGarmin() {
	    return cbSendToGarmin.getState();
	}

	public String getPrefix() {
	    return ibPrefix.text;
	}

	public int getWpNameStyle() {
	    return chWpNameStyle.selectedIndex;
	}

	public int getGPXVersion() {
	    return chGPXVersion.selectedIndex;
	}

	public boolean getWithGarminExtensions() {
	    return this.cbWithGarminExtensions.getState();
	}

	public boolean getWithGSAKExtensions() {
	    return this.cbWithGSAKExtensions.getState();
	}

	public boolean getUseCustomIcons() {
	    return cbUseCustomIcons.getState();
	}

	public boolean getExportLogsAsPlainText() {
	    return cbExportLogsAsPlainText.getState();
	}

	public boolean getExportAddiWithInvalidCoords() {
	    return cbAddiWithInvalidCoords.getState();
	}

	public int getMaxLogs() {
	    if (ibMaxLogs.getText().length() == 0) {
		ibMaxLogs.setText("-1");
		return -1;
	    } else
		return Common.parseInt(ibMaxLogs.getText());
	}

	public int getSplitSize() {
	    if (ibSplitSize.getText().length() == 0)
		return -1;
	    else
		return Common.parseInt(ibSplitSize.getText());
	}

	public boolean getAttrib2Log() {
	    return cbAttrib2Log.getState();
	}

	public String getGpxOutputTo() {
	    return ibFilename.getText();
	}

	/**
	 * react to GUI events and toogle access to the checkboxes according to radio button settings pass everything else to <code>super()</code>
	 */
	public void onEvent(Event ev) {
	    if (ev instanceof DataChangeEvent && ev.type == DataChangeEvent.DATA_CHANGED) {
		if (ev.target == chStyle && chStyle.selectedIndex != gpxStyle) {
		    checkStyle();
		}
		if (ev.target == chOutput && chOutput.selectedIndex != outputStyle) {
		    checkStyle();
		}
	    } else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
		if (ev.target == executePanel.applyButton) {
		    boolean mayclose = true; // if plausibility checks fail: set to false
		    if (gpxStyle == GpxExportNg.STYLE_PQEXTENSIONS) {
			int logs = getMaxLogs();
			if (logs >= -1) {
			    if (mayclose)
				close(1);
			} else {
			    ibMaxLogs.selectAll();
			    ibMaxLogs.takeFocus(0);
			    Sound.beep();
			}
		    } else {
			close(1);
		    }
		    if (mayclose) {
			MainForm.profile.setLastUsedGpxStyle(gpxStyle);
			MainForm.profile.setLastUsedOutputStyle(outputStyle);
			MainForm.profile.setGpxOutputTo(ibFilename.getText());
			setPreferences();
		    }
		} else if (ev.target == executePanel.cancelButton) {
		    close(-1);
		} else if (ev.target == this.btnFilename) {
		    String tmp;
		    switch (outputStyle) {
		    case OUTPUT_SEPARATE:
		    case OUTPUT_POI:
			tmp = this.getOutputTo(FileChooser.DIRECTORY_SELECT);
			break;
		    default:
			tmp = this.getOutputTo(FileChooser.SAVE | FileChooser.QUICK_SELECT);
		    }
		    if (tmp.length() > 0)
			this.ibFilename.setText(tmp);
		}
	    }
	    super.onEvent(ev);
	}

	private void setPreferences() {
	    exportValues.put(GPXVERSION, "" + chGPXVersion.selectedIndex);
	    exportValues.put(WITHGARMINEXTENSIONS, SafeXML.strxmlencode(cbWithGarminExtensions.getState()));
	    exportValues.put(WITHGSAKEXTENSIONS, SafeXML.strxmlencode(cbWithGSAKExtensions.getState()));
	    switch (outputStyle) {
	    case OUTPUT_SINGLE:
		exportValues.put(SENDTOGARMIN, SafeXML.strxmlencode(cbSendToGarmin.getState()));
		break;
	    case OUTPUT_SEPARATE:
		exportValues.put(PREFIX, ibPrefix.text);
		break;
	    case OUTPUT_POI:
		exportValues.put(PREFIX, ibPrefix.text);
		break;
	    }
	    switch (gpxStyle) {
	    case STYLE_COMPACT:
		exportValues.put(WPNAMESTYLE, "" + chWpNameStyle.selectedIndex);
		exportValues.put(USECUSTOMICONS, SafeXML.strxmlencode(cbUseCustomIcons.getState()));
	    case STYLE_PQEXTENSIONS:
		exportValues.put(SPLITSIZE, ibSplitSize.text);
		exportValues.put(ATTRIB2LOG, SafeXML.strxmlencode(cbAttrib2Log.getState()));
		exportValues.put(EXPORTADDIWITHINVALIDCOORDS, SafeXML.strxmlencode(cbAddiWithInvalidCoords.getState()));
		exportValues.put(USECUSTOMICONS, SafeXML.strxmlencode(cbUseCustomIcons.getState()));
		exportValues.put(EXPORTLOGASPLAINTEXT, SafeXML.strxmlencode(cbExportLogsAsPlainText.getState()));
		exportValues.put(MAXNUMBEROFLOGSTOEXPORT, ibMaxLogs.text);
		break;
	    case STYLE_MYFINDS:
		exportValues.put(EXPORTLOGASPLAINTEXT, SafeXML.strxmlencode(cbExportLogsAsPlainText.getState()));
		break;
	    }
	}

	private String getOutputTo(int what) {
	    FileChooser fc;
	    fc = new FileChooser(what, ibFilename.getText());
	    if (what == FileChooserBase.DIRECTORY_SELECT) {
		fc.setTitle(MyLocale.getMsg(616, "Verzeichnis auswählen"));
	    } else {
		fc.setTitle(MyLocale.getMsg(2021, "Ausgabedatei"));
		fc.addMask("*.gpx");
	    }
	    if (fc.execute() == FormBase.IDCANCEL)
		return "";
	    return fc.getChosenFile().getFullPath();
	}
    }
}
