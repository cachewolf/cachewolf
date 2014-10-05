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
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
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
import CacheWolf.utils.SafeXML;

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
 * experimental GPX exporter that should better handle the various tasks that can be accomplished with GPX
 */
public class GpxExportNg {
    final static String newLine = "\r\n";
    final static String WPNAMESTYLE = "wpNameStyle";
    final static String SPLITSIZE = "splitSize";
    final static String EXPORTADDIWITHINVALIDCOORDS = "exportAddiWithInvalidCoords";
    final static String USECUSTOMICONS = "useCustomIcons";
    final static String SENDTOGARMIN = "sendToGarmin";
    final static String EXPORTLOGASPLAINTEXT = "exportLogsAsPlainText";
    final static String ATTRIB2LOG = "attrib2Log";
    final static String MAXNUMBEROFLOGSTOEXPORT = "maxNumberOfLogsToExport";
    final static String PREFIX = "prefix";
    /** write compcat single GPX file */
    final static int STYLE_COMPCAT_OUTPUT_SINGLE = 0;
    /** write compact one file per "type" as determined by garminmap.xml */
    final static int STYLE_COMPCAT_OUTPUT_SEPARATE = 1;
    /** generate GPI files with gpsbabel using garminmap.xml types */
    final static int STYLE_COMPCAT_OUTPUT_POI = 2;
    /** export is PQ like */
    final static int STYLE_GPX_PQLIKE = 3;
    /** export follows gc.com MyFinds format */
    final static int STYLE_GPX_MYFINDS = 4;
    /** export uses only waypoint id */
    final static int WPNAME_ID_CLASSIC = 0;
    /** export uses waypointid + type, terrain, difficulty, size */
    final static int WPNAME_ID_SMART = 1;
    /** export uses cache names (will be made unique by gpsbabel) */
    final static int WPNAME_NAME_SMART = 2;
    /** name used as key when storing preferences */
    final static String expName = "GpxExportNG";
    /** string representation of true */
    final static String TRUE = "True";
    /** string representation of false */
    final static String FALSE = "False";
    /** object used to determine custom symbols and POI categories */
    private static GarminMap garminMap = new GarminMap();
    /** number of errors / warnings during export */
    private int exportErrors = 0;
    /**  */
    private String finderid;

    // we need to fake desc to make clients like GSAK accept additional waypoints together with caches
    final static String GPXHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + (newLine)//
	    + ("<gpx")//
	    + (" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")//
	    + (" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"")//
	    + (" version=\"1.0\"")//
	    + (" creator=\"CacheWolf\"")//
	    + (" xsi:schemaLocation=\"")//
	    + ("http://www.topografix.com/GPX/1/0 ")//
	    + ("http://www.topografix.com/GPX/1/0/gpx.xsd ")//
	    + ("http://www.groundspeak.com/cache/1/0/1 ")//
	    + ("http://www.groundspeak.com/cache/1/0/1/cache.xsd")//
	    + ("\"")//
	    + (" xmlns=\"http://www.topografix.com/GPX/1/0\"")//
	    + (">")//
	    + (newLine)//
	    + ("<name>@@NAME@@</name>") + (newLine)//
	    + ("<desc>This is an individual cache generated from Geocaching.com</desc>") + (newLine)//
	    + ("<author>Various users from geocaching.com and/or opencaching.de</author>") + (newLine)//
	    + ("<email>contact@cachewolf.de</email>") + (newLine)//
	    + ("<url>http://www.cachewolf.de/</url>") + (newLine)//
	    + ("<urlname>CacheWolf - Paperless Geocaching</urlname>") + (newLine)//
	    + ("<time>@@CREATEDATE@@T07:00:00Z</time>") + (newLine)//
	    + ("<keywords>cache, geocache, waypoints</keywords>") + (newLine)//
    // TODO: is it worth a second loop?
    // +("<bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\" maxlon=\"6.918817\" />")
    ;

    final static String GPXLOG = "\t\t\t\t<groundspeak:log id=\"@@LOGID@@\">" + (newLine)//
	    + ("\t\t\t\t\t<groundspeak:date>@@LOGDATE@@T00:00:00</groundspeak:date>") + (newLine)//
	    + ("\t\t\t\t\t<groundspeak:type>@@LOGTYPE@@</groundspeak:type>") + (newLine)//
	    + ("\t\t\t\t\t<groundspeak:finder id=\"@@LOGFINDERID@@\">@@LOGFINDER@@</groundspeak:finder>") + (newLine)//
	    + ("\t\t\t\t\t<groundspeak:text encoded=\"@@LOGENCODE@@\">@@LOGTEXT@@</groundspeak:text>") + (newLine)//
	    + ("\t\t\t\t</groundspeak:log>") + (newLine);//

    final static String GPXTB = "\t\t\t\t<groundspeak:travelbug id=\"@@TBID@@\" ref=\"@@TBREF@@\">" + (newLine)//
	    + ("\t\t\t\t\t<groundspeak:name>@@TBNAME@@</groundspeak:name>") + (newLine)//
	    + ("\t\t\t\t</groundspeak:travelbug>") + (newLine);//

    // FIXME: don't use this until GPX import can strip this off as well
    final static String GPXADDIINMAIN = "@@ADDIID@@ - @@ADDISHORT@@@@ADDIDELIM@@"//
	    + ("@@ADDILAT@@ @@ADDILON@@@@ADDIDELIM@@")//
	    + ("@@ADDILONG@@@@ADDIDELIM@@");//

    private static boolean sendToGarmin;
    private static boolean attrib2Log;
    private static int maxLogs;

    private static int exportStyle;

    private static boolean hasBitmaps;
    private static boolean hasGpsbabel;

    private static String bitmapFileName;

    public GpxExportNg() {
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
    }

    private GpxExportNgForm exportOptions;
    private StringBuffer theLogs = new StringBuffer();

    public void doit() {

	exportOptions = new GpxExportNgForm(garminMap.exists, hasBitmaps, hasGpsbabel);
	if (exportOptions.execute() == FormBase.IDCANCEL) {
	    return;
	}

	exportStyle = exportOptions.getGpxStyle();
	sendToGarmin = exportOptions.getSendToGarmin();
	if (exportStyle == STYLE_GPX_PQLIKE) {
	    maxLogs = exportOptions.getMaxLogs();
	    attrib2Log = exportOptions.getAttrib2Log();
	}

	if (exportStyle == STYLE_COMPCAT_OUTPUT_SEPARATE || exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
	    final Hashtable fileHandles = new Hashtable();
	    final String outDir;
	    final String tempDir;
	    final String baseDir = FileBase.getProgramDirectory();
	    final String prefix = exportOptions.getPrefix();
	    final FileChooser fc;
	    ZipFile poiZip = null;

	    if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
		fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().getExportPath(expName + "-POI"));
	    } else {
		fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().getExportPath(expName + "-GPI"));
	    }
	    fc.setTitle("Select target directory:");
	    if (fc.execute() == FormBase.IDCANCEL)
		return;
	    outDir = fc.getChosenFile().getFullPath();
	    if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
		Preferences.itself().setExportPath(expName + "-POI", outDir);
	    } else {
		Preferences.itself().setExportPath(expName + "-GPI", outDir);
	    }

	    if (!garminMap.exists) {
		Preferences.itself().log("GPX Export: unable to load garminmap.xml", null);
		new InfoBox(MyLocale.getMsg(5500, "Error"), "unable to load garminmap.xml").wait(FormBase.OKB);
		return;
	    }

	    if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
		// FIXME: create proper tempdir
		tempDir = baseDir + FileBase.separator + "GPXExporterNG.tmp";
		new File(tempDir).mkdir();
	    } else {
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
		    CacheHolder ch = MainForm.profile.cacheDB.get(i);
		    if (!ch.isVisible()) {
			continue;
		    } else if (ch.isIncomplete()) {
			Preferences.itself().log("skipping export of incomplete waypoint " + ch.getWayPoint(), null);
		    } else {
			String poiId = garminMap.getPoiId(ch);
			if (null == poiId) {
			    Preferences.itself().log("GPX Export: unmatched POI ID for " + ch.getWayPoint() + " of type " + ch.getType(), null);
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
			    String strOut = formatCache(ch);
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

		if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
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
		    if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
			poiCounter++;
			h.progress = (float) poiCounter / (float) poiCategories;
			h.changed();
		    }
		    if (poiZip != null) {
			if (!copyPoiIcon(tempDir, key, prefix, poiZip)) {
			    exportErrors++;
			    continue;
			}

			if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
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

		if (exportStyle == STYLE_COMPCAT_OUTPUT_POI) {
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
	} else {
	    if (exportOptions.getUseCustomIcons()) {
		if (!garminMap.exists) {
		    Preferences.itself().log("unable to load garminmap.xml", null);
		}
	    }

	    final File file;

	    if (!sendToGarmin) {
		final FileChooser fc = new FileChooser(FileChooserBase.SAVE, Preferences.itself().getExportPath(expName + "-GPX"));

		fc.setTitle("Select target GPX file:");
		fc.addMask("*.gpx");

		if (fc.execute() == FormBase.IDCANCEL)
		    return;

		file = fc.getChosenFile();
		Preferences.itself().setExportPath(expName + "-GPX", file.getPath());
	    } else {
		file = new File("");
		file.createTempFile("gpxexport", null, null);
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
		CacheDB cDB = MainForm.profile.cacheDB;
		for (int i = 0; i < cDB.size(); i++) {
		    CacheHolder ch = cDB.get(i);
		    if (!ch.isVisible()) {
			continue;
		    } else if (ch.isIncomplete()) {
			exportErrors++;
			infB.addWarning("Skipping export of incomplete waypoint " + ch.getWayPoint());
			Preferences.itself().log("GPX Export: skipping export of incomplete waypoint " + ch.getWayPoint(), null);
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
			String strOut = formatCache(ch);
			if (!strOut.equals("")) {
			    outp.print(strOut);
			    expCount++;
			}
		    }
		    if (expCount % 100 == 0) {
			infB.setInfo("Exporting " + expCount + " ( " + totalCount + " )");
			infB.redisplay();
			MainTab.itself.tablePanel.updateStatusBar(" " + ((expCount * 100) / totalCount) + "% ");
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
	if (exportErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), exportErrors + " errors during export. Check log for details.").wait(FormBase.OKB);
	}
    }

    /**
     * wrapper for formatting a cache. will call some subroutines to do the actual work
     * 
     * @param ch
     * @return
     */
    private String formatCache(CacheHolder ch) {
	// no addis or custom in MyFindsPq - and of course only finds
	if ((STYLE_GPX_MYFINDS == exportStyle) && (ch.isCustomWpt() || ch.isAddiWpt() || !ch.isFound()))
	    return "";

	if (!ch.getPos().isValid()) {
	    if (!ch.isAddiWpt()) {
		Preferences.itself().log("[GPX Export:formatCache] " + ch.getWayPoint() + " has invalid coords.");
		return "";
	    }
	    if (!exportOptions.getExportAddiWithInvalidCoords()) {
		return "";
	    }
	}

	StringBuffer ret = new StringBuffer();
	ch.getCacheDetails(true);
	try {
	    ret.append(formatCompact(ch));

	    if (exportStyle == STYLE_GPX_PQLIKE || exportStyle == STYLE_GPX_MYFINDS) {
		ret.append(formatPqExtensions(ch));
	    }

	    ret.append("  </wpt>").append(newLine);
	} catch (IllegalArgumentException e) {
	    exportErrors++;
	    ch.checkIncomplete(); // ch.setIncomplete(true);
	    Preferences.itself().log("GPX Export: " + ch.getWayPoint() + " check incomplete ", e, true);
	    return "";
	} catch (Exception e) {
	    exportErrors++;
	    Preferences.itself().log("GPX Export: " + ch.getWayPoint() + " caused ", e, true);
	    return "";
	}

	return ret.toString();
    }

    /**
     * generate minimal waypoint information according to GPX specification
     * 
     * @param ch
     * @return
     */
    private String formatCompact(CacheHolder ch) {

	StringBuffer ret = new StringBuffer();

	// .append("\t\t<desc>@@WPDESC@@</desc>").append(newLine)

	ret.append("  <wpt lat=\"" + ch.getPos().getLatDeg(TransformCoordinates.DD) + "\" lon=\"" + ch.getPos().getLonDeg(TransformCoordinates.DD) + "\">").append(newLine);

	if (exportStyle == STYLE_GPX_PQLIKE || exportStyle == STYLE_GPX_MYFINDS) {
	    if (ch.isAddiWpt()) {
		try {
		    ret.append("    <time>" + ch.mainCache.getDateHidden() + "T07:00:00Z</time>").append(newLine);
		} catch (Exception e) {
		    Preferences.itself().log(ch.getWayPoint() + " has no parent", null);
		    exportErrors++;
		    ret.append("    <time>1970-01-01T00:00:00</time>").append(newLine);
		}
	    } else if (ch.isCustomWpt()) {
		ret.append("    <time>1970-01-01T00:00:00</time>").append(newLine);
	    } else {
		ret.append("    <time>" + ch.getDateHidden() + "T00:00:00</time>").append(newLine);
	    }
	}

	if (exportOptions.getWpNameStyle() == WPNAME_ID_SMART) {
	    if (ch.isAddiWpt()) {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.mainCache.getWayPoint())).append(" ").append(ch.getWayPoint().substring(0, 2)).append("</name>").append(newLine);
	    } else if (ch.isCustomWpt()) {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.getWayPoint())).append("</name>").append(newLine);
	    } else {
		ret.append("    <name>").append(SafeXML.cleanGPX(ch.getWayPoint()))//
			.append(" ")//
			.append(CacheType.getExportShortId(ch.getType()))//
			.append(String.valueOf(ch.getHard()))//
			.append(String.valueOf(ch.getTerrain()))//
			.append(CacheSize.getExportShortId(ch.getCacheSize()))//
			.append(String.valueOf(ch.getNoFindLogs()))//
			.append("</name>").append(newLine);
	    }
	} else if (exportOptions.getWpNameStyle() == WPNAME_NAME_SMART) {
	    // TBD
	} else {
	    ret.append("    <name>").append(SafeXML.cleanGPX(ch.getWayPoint())).append("</name>").append(newLine);
	}

	// no <cmt> for custom
	if (!ch.isCustomWpt()) {
	    if (exportOptions.getWpNameStyle() == WPNAME_ID_SMART && (exportStyle == STYLE_COMPCAT_OUTPUT_SINGLE || exportStyle == STYLE_COMPCAT_OUTPUT_SEPARATE || exportStyle == STYLE_COMPCAT_OUTPUT_POI)) {
		if (ch.isAddiWpt()) {
		    ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getCacheName() + " " + ch.getCacheDetails(true).LongDescription)).append("</cmt>").append(newLine);
		} else {
		    ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getCacheName() + " " + Common.rot13(ch.getCacheDetails(true).Hints))).append("</cmt>").append(newLine);
		}
	    } else if (exportOptions.getWpNameStyle() == WPNAME_NAME_SMART) {
		// TBD
	    } else {
		if (ch.isAddiWpt()) {
		    ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getCacheDetails(true).LongDescription)).append("</cmt>").append(newLine);
		} // caches have no <cmt> in gc.com PQs
	    }
	}

	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    ret.append("    <desc>").append(SafeXML.cleanGPX(ch.getCacheName())).append("</desc>").append(newLine);
	} else {
	    ret.append("    <desc>").append(SafeXML.cleanGPX(ch.getCacheName()))//
		    .append(" by ")//
		    .append(SafeXML.cleanGPX(ch.getCacheOwner()))//
		    .append(", ")//
		    .append(CacheType.type2GSTypeTag(ch.getType()))//
		    .append(" (")//
		    .append(CacheTerrDiff.shortDT(ch.getHard()))//
		    .append("/")//
		    .append(CacheTerrDiff.shortDT(ch.getTerrain()))//
		    .append(")")//
		    .append("</desc>").append(newLine);
	}

	if (exportStyle == STYLE_GPX_PQLIKE || exportStyle == STYLE_GPX_MYFINDS) {
	    if (!ch.isCustomWpt()) {
		ret.append("    <url>").append(ch.getCacheDetails(true).URL).append("</url>").append(newLine);
		ret.append("    <urlname>").append(SafeXML.cleanGPX(ch.getCacheName())).append("</urlname>").append(newLine);
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

	if (exportStyle == STYLE_GPX_PQLIKE || exportStyle == STYLE_GPX_MYFINDS) {
	    ret.append("    <type>").append(CacheType.type2TypeTag(ch.getType())).append("</type>").append(newLine);
	}

	return ret.toString();
    }

    /**
     * format gc.com extended cache information as found in a PQ
     * 
     * @param ch
     *            cacheholder
     * @return formatted cache information for cache waypoints or emty string for all other waypoints (additional / custom)
     */
    private String formatPqExtensions(CacheHolder ch) {

	// no details for addis or custom waypoints
	// if (ch.isCustomWpt() || ch.isAddiWpt())
	if (ch.isAddiWpt())
	    return "";
	StringBuffer ret = new StringBuffer();
	ret.append("    <groundspeak:cache id=\"").append(ch.GetCacheID())//
		.append("\" available=\"").append(ch.isAvailable() ? TRUE : FALSE)//
		.append("\" archived=\"").append(ch.isArchived() ? TRUE : FALSE)//
		.append("\" xmlns:groundspeak=\"http://www.groundspeak.com/cache/1/0\">").append(newLine)//
		.append("      <groundspeak:name>").append(SafeXML.cleanGPX(ch.getCacheName())).append("</groundspeak:name>").append(newLine)//
		.append("      <groundspeak:placed_by>").append(SafeXML.cleanGPX(ch.getCacheOwner())).append("</groundspeak:placed_by>").append(newLine)//
		.append("      <groundspeak:owner id=\"").append("31415").append("\">").append(SafeXML.cleanGPX(ch.getCacheOwner())).append("</groundspeak:owner>").append(newLine)//
		.append("      <groundspeak:type>").append(CacheType.type2GSTypeTag(ch.getType())).append("</groundspeak:type>").append(newLine)//
		.append("      <groundspeak:container>").append(CacheSize.cw2ExportString(ch.getCacheSize())).append("</groundspeak:container>").append(newLine)//
		.append("      <groundspeak:attributes>").append(newLine)//
		.append(formatAttributes(ch))//
		.append("      </groundspeak:attributes>").append(newLine)//
		.append("      <groundspeak:difficulty>").append(CacheTerrDiff.shortDT(ch.getHard())).append("</groundspeak:difficulty>").append(newLine)//
		.append("      <groundspeak:terrain>").append(CacheTerrDiff.shortDT(ch.getTerrain())).append("</groundspeak:terrain>").append(newLine)//
		.append("      <groundspeak:country>").append(SafeXML.cleanGPX(ch.getCacheDetails(true).Country)).append("</groundspeak:country>").append(newLine)//
		.append("      <groundspeak:state>").append(SafeXML.cleanGPX(ch.getCacheDetails(true).State)).append("</groundspeak:state>").append(newLine)//
		.append("      <groundspeak:short_description html=\"").append(ch.isHTML() ? TRUE : FALSE).append("\"></groundspeak:short_description>").append(newLine)//
		.append("      <groundspeak:long_description html=\"").append(ch.isHTML() ? TRUE : FALSE).append("\">").append(SafeXML.cleanGPX(formatLongDescription(ch))).append("</groundspeak:long_description>").append(newLine)//
		.append("      <groundspeak:encoded_hints>").append(SafeXML.cleanGPX(Common.rot13(ch.getCacheDetails(true).Hints))).append("</groundspeak:encoded_hints>").append(newLine)//
		.append("      <groundspeak:logs>").append(newLine)//
		.append(formatLogs(ch))//
		.append("      </groundspeak:logs>").append(newLine)//
		.append("      <groundspeak:travelbugs>").append(newLine)//
		.append(formatTbs(ch))//
		.append("      </groundspeak:travelbugs>").append(newLine)//
		.append("    </groundspeak:cache>").append(newLine);//
	return ret.toString();
    }

    /**
     *
     */
    private String formatTbs(CacheHolder ch) {
	StringBuffer ret = new StringBuffer();
	Travelbug Tb;
	for (int i = 0; i < ch.getCacheDetails(true).Travelbugs.size(); i++) {
	    Tb = ch.getCacheDetails(true).Travelbugs.getTB(i);
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
    private String formatAttributes(CacheHolder ch) {
	StringBuffer ret = new StringBuffer();
	Attribute attrib;
	for (int i = 0; i < ch.getCacheDetails(true).attributes.count(); i++) {
	    // <groundspeak:attribute id="X" inc="Y">text für X</groundspeak:attribute>
	    attrib = ch.getCacheDetails(true).attributes.getAttribute(i);
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

    private String formatLogs(CacheHolder ch) {
	CacheHolderDetail chD = ch.getCacheDetails(false);
	LogList logs = chD.CacheLogs;
	theLogs.setLength(0);

	if (exportStyle == STYLE_GPX_MYFINDS) {
	    if (ch.isFound()) {
		// perhaps there is no Ownlog yet
		if (chD.OwnLog == null) {
		    Preferences.itself().log(chD.getParent().getWayPoint() + " missing own Log", null);
		    return "";
		} else {
		    addLog(chD.OwnLog);
		}
	    }
	} else { // it is PQ

	    int exportlogs;
	    if (maxLogs < logs.size()) {
		exportlogs = maxLogs;
	    } else {
		exportlogs = logs.size();
	    }

	    final String cacheID = ch.GetCacheID();

	    if (attrib2Log) {
		// with attributes as log
		Log attrLog = createAttrLog(ch);
		attrLog.setFinderID("-2"); // default is ""
		attrLog.setLogID(cacheID + Integer.toString(exportlogs));
		addLog(attrLog);
	    }

	    // CW doesn't save the LogID (upto version ~1.3.3394). 
	    // So we generate one by ch.GetCacheID() + Integer.toString(i)
	    for (int i = 0; i < exportlogs; i++) {
		Log theLog = logs.getLog(i);
		String logID = theLog.getLogID();
		if (logID.length() == 0) {
		    theLog.setLogID(cacheID + Integer.toString(i));
		}
		addLog(theLog);
	    }

	}
	return theLogs.toString();
    }

    private Log createAttrLog(CacheHolder ch) {
	Attribute attrib;
	StringBuffer logText = new StringBuffer();
	for (int i = 0; i < ch.getCacheDetails(true).attributes.count(); i++) {
	    attrib = ch.getCacheDetails(true).attributes.getAttribute(i);
	    logText.append(attrib.getInc() == 1 ? "Yes: " : "No: ").append(attrib.getMsg()).append("<br />").append(newLine);
	}
	if (ch.hasNote()) {
	    logText.append(SafeXML.cleanGPX(ch.getCacheDetails(true).getCacheNotes())).append("<br />").append(newLine);
	}
	if (logText.length() == 0 && !ch.getLastSync().equals(""))
	    logText.append(MyLocale.getMsg(1051, "Last sync date"));
	Log log = new Log("", "", "icon_note.gif", DateFormat.yyyyMMddHHmmss2gpxLogdate(ch.getLastSync()), "CacheWolf", logText.toString());
	return log;
    }

    private void addLog(Log log) {

	String logMessage = log.getMessage();
	Transformer trans = new Transformer(true);

	if (exportOptions.getExportLogsAsPlainText()) {
	    trans.add(new Regex("\r", ""));
	    trans.add(new Regex("\n", " "));
	    trans.add(new Regex("<br>", "\n"));
	    trans.add(new Regex("<p>", "\n"));
	    trans.add(new Regex("<hr>", "\n"));
	    trans.add(new Regex("<br />", "\n"));
	    trans.add(new Regex("<(.*?)>", ""));
	    Transformer ttrans = new Transformer(true);
	    ttrans.add(new Regex("<(.*?)>", ""));
	    logMessage = ttrans.replaceAll(trans.replaceAll(logMessage));
	}

	trans = new Transformer(true);
	trans.add(new Regex("@@LOGID@@", log.getLogID()));
	trans.add(new Regex("@@LOGDATE@@", log.getDate()));
	trans.add(new Regex("@@LOGTYPE@@", CacheHolder.image2TypeText(log.getIcon())));
	trans.add(new Regex("@@LOGFINDERID@@", log.getFinderID()));
	trans.add(new Regex("@@LOGFINDER@@", SafeXML.cleanGPX(log.getLogger())));
	trans.add(new Regex("@@LOGENCODE@@", ""));
	trans.add(new Regex("@@LOGTEXT@@", SafeXML.cleanGPX(logMessage)));
	theLogs.append(trans.replaceAll(GPXLOG));
    }

    /**
     * format the header of the GPX file
     * 
     * @return
     */
    private String formatHeader() {
	// FIXME: extend MainForm.profile to add <bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\"
	// maxlon=\"6.918817\" />
	// MainForm.profile.getSourroundingArea(false);
	Transformer trans = new Transformer(true);
	trans.add(new Regex("@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString()));
	if (exportStyle == STYLE_GPX_MYFINDS) {
	    trans.add(new Regex("@@NAME@@", "My Finds Pocket Query"));
	} else {
	    trans.add(new Regex("@@NAME@@", "Waypoints for Cache Listings, Generated by CacheWolf"));
	}
	return trans.replaceFirst(GPXHEADER);
	/*
	 * String ret = STRreplace.replace(GPXHEADER,"@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString());
	 * if (exportStyle==STYLE_GPX_MYFINDS)
	 * { ret=STRreplace.replace(ret,"@@NAME@@","My Finds Pocket Query");}
	 * else { ret=STRreplace.replace(ret,"@@NAME@@","Waypoints for Cache Listings, Generated by CacheWolf");}
	 * return ret;
	 */
    }

    /**
     * format a long description as found in the gc.com GPX files
     * 
     * @param ch
     *            CacheHolder to format
     * @return formatted output
     */
    private String formatLongDescription(CacheHolder ch) {
	if (ch.isAddiWpt() || ch.isCustomWpt()) {
	    return ch.getCacheDetails(true).LongDescription;
	} else {
	    StringBuffer ret = new StringBuffer();
	    String delim = "";
	    ret.append(ch.getCacheDetails(true).LongDescription);
	    if (ch.isHTML()) {
		delim = "<br />";
	    } else {
		delim = newLine;
	    }
	    // FIXME: format is not quite right yet
	    // FIXME: cut Addis off in GPXimporter otherwise people who use GPX to feed CacheWolf have them doubled
	    if (ch.addiWpts.size() > 0 && exportStyle != STYLE_GPX_MYFINDS) {
		if (ch.isHTML()) {
		    ret.append(newLine).append(newLine).append("<p>Additional Waypoints</p>");
		} else {
		    ret.append(newLine).append(newLine).append("Additional Waypoints").append(newLine);
		}

		Iterator iter = ch.addiWpts.iterator();
		while (iter.hasNext()) {
		    CacheHolder addi = (CacheHolder) iter.next();
		    Transformer trans = new Transformer(true);
		    trans.add(new Regex("@@ADDIID@@", addi.getWayPoint()));
		    trans.add(new Regex("@@ADDISHORT@@", addi.getCacheName()));
		    trans.add(new Regex("@@ADDIDELIM@@", delim));
		    trans.add(new Regex("@@ADDILAT@@", formatAddiLatLon(addi.getPos())));
		    trans.add(new Regex("@@ADDILON@@", ""));
		    trans.add(new Regex("@@ADDILONG@@", addi.getCacheDetails(true).LongDescription));
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

	private mLabel lblWpNameStyle, lblAddiWithInvalidCoords, lblSplitSize, lblUseCustomIcons, lblSendToGarmin, lblMaxLogs, lblExportLogsAsPlainText, lblAttrib2Log, lblPrefix;
	private int gpxStyle;
	private mCheckBox cbUseCustomIcons, cbSendToGarmin, cbAddiWithInvalidCoords, cbAttrib2Log, cbExportLogsAsPlainText;
	private mInput ibMaxLogs, ibSplitSize, ibPrefix;
	private mChoice chStyle, chWpNameStyle;
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

	    hasIcons = _hasBitmaps;
	    hasGarminMap = _hasGarminMap;
	    isGpsBabelInstalled = _hasGpsbabel;

	    addNext(new mLabel(MyLocale.getMsg(2013, "GPX Style")));
	    chStyle = new mChoice();
	    chStyle.dontSearchForKeys = true;
	    // if you change the order of strings make sure to fix the event handler as well
	    chStyle.addItem(MyLocale.getMsg(2004, "Compact") + ": " + MyLocale.getMsg(2007, "Single GPX")); // index 0
	    chStyle.addItem(MyLocale.getMsg(2004, "Compact") + ": " + MyLocale.getMsg(2008, "Separate GPX")); // index 1
	    chStyle.addItem(MyLocale.getMsg(2004, "Compact") + ": " + MyLocale.getMsg(2009, "POI")); // index 2
	    chStyle.addItem(MyLocale.getMsg(2005, "PQ like")); // index 3
	    chStyle.addItem(MyLocale.getMsg(2006, "MyFinds")); // index 4
	    chStyle.select(gpxStyle);
	    addLast(chStyle);

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
	    cbAddiWithInvalidCoords = new mCheckBox("");
	    addLast(cbAddiWithInvalidCoords);

	    addNext(lblUseCustomIcons = new mLabel(MyLocale.getMsg(2012, "Custom Icons")));
	    cbUseCustomIcons = new mCheckBox("");
	    addLast(cbUseCustomIcons);

	    addNext(lblSendToGarmin = new mLabel(MyLocale.getMsg(2011, "send to Garmin")));
	    cbSendToGarmin = new mCheckBox("");
	    addLast(cbSendToGarmin);

	    addNext(lblExportLogsAsPlainText = new mLabel(MyLocale.getMsg(2010, "HTML - Tags aus Logs entfernen")));
	    cbExportLogsAsPlainText = new mCheckBox("");
	    addLast(cbExportLogsAsPlainText);

	    addNext(lblAttrib2Log = new mLabel(MyLocale.getMsg(2017, "Attrib.->Log")));
	    cbAttrib2Log = new mCheckBox("");
	    addLast(cbAttrib2Log);

	    addNext(lblMaxLogs = new mLabel(MyLocale.getMsg(2018, "Max Logs")));
	    ibMaxLogs = new mInput("");
	    addLast(ibMaxLogs);

	    addNext(lblPrefix = new mLabel(MyLocale.getMsg(2016, "Prefix")));
	    ibPrefix = new mInput("");
	    addLast(ibPrefix);

	    executePanel = new ExecutePanel(this);

	    checkStyle();
	}

	private void setFromPreferences() {
	    chWpNameStyle.select(Common.parseInt(getExportValue(WPNAMESTYLE)));
	    int splitSize = Common.parseInt(getExportValue(SPLITSIZE));
	    ibSplitSize.setText((splitSize == -1) ? "" : String.valueOf(splitSize));
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

	public boolean getSendToGarmin() {
	    return cbSendToGarmin.getState();
	}

	public String getPrefix() {
	    return ibPrefix.text;
	}

	public int getWpNameStyle() {
	    return chWpNameStyle.selectedIndex;
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

	private void checkStyle() {
	    gpxStyle = chStyle.selectedIndex;
	    exportValues = Preferences.itself().getGpxExportPreferences(Preferences.itself().gpxStyles[gpxStyle]);
	    setFromPreferences();
	    if (gpxStyle == STYLE_GPX_MYFINDS) {
		chWpNameStyle.select(0);
		disable(lblWpNameStyle, chWpNameStyle);
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
	    } else if (gpxStyle == STYLE_GPX_PQLIKE) {
		enable(lblWpNameStyle, chWpNameStyle);
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
	    } else { // compact export
		enable(lblWpNameStyle, chWpNameStyle);
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
		if (gpxStyle == STYLE_COMPCAT_OUTPUT_SINGLE) {
		    if (isGpsBabelInstalled)
			enable(lblSendToGarmin, cbSendToGarmin);
		    else {
			disable(lblSendToGarmin, cbSendToGarmin);
			cbSendToGarmin.setState(false);
		    }
		    disable(lblPrefix, ibPrefix);

		} else if (gpxStyle == STYLE_COMPCAT_OUTPUT_SEPARATE) {
		    disable(lblSendToGarmin, cbSendToGarmin);
		    cbSendToGarmin.setState(false);
		    if (hasIcons)
			enable(lblUseCustomIcons, cbUseCustomIcons);
		    else {
			disable(lblUseCustomIcons, cbUseCustomIcons);
			cbUseCustomIcons.setState(false);
		    }
		    enable(lblPrefix, ibPrefix);
		} else if (gpxStyle == STYLE_COMPCAT_OUTPUT_POI) {
		    disable(lblUseCustomIcons, cbUseCustomIcons);
		    cbUseCustomIcons.setState(false);
		    disable(lblSendToGarmin, cbSendToGarmin);
		    cbSendToGarmin.setState(false);
		    enable(lblPrefix, ibPrefix);
		}
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

	/**
	 * react to GUI events and toogle access to the checkboxes according to radio button settings pass everything else to <code>super()</code>
	 */
	public void onEvent(Event ev) {
	    if (ev instanceof DataChangeEvent && ev.type == DataChangeEvent.DATA_CHANGED) {
		if (ev.target == chStyle && chStyle.selectedIndex != gpxStyle) {
		    checkStyle();
		}
	    } else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
		if (ev.target == executePanel.cancelButton) {
		    close(-1);
		} else if (ev.target == executePanel.applyButton) {
		    boolean mayclose = true; // if plausibility checks fail: set to false
		    if (gpxStyle == GpxExportNg.STYLE_GPX_PQLIKE) {
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
			MainForm.profile.setProfilesLastUsedGpxStyle(gpxStyle);
			setPreferences();
		    }
		}
	    }

	    super.onEvent(ev);
	}

	private void setPreferences() {
	    switch (gpxStyle) {
	    case STYLE_COMPCAT_OUTPUT_SINGLE:
		exportValues.put(WPNAMESTYLE, "" + chWpNameStyle.selectedIndex);
		exportValues.put(USECUSTOMICONS, SafeXML.strxmlencode(cbUseCustomIcons.getState()));
		exportValues.put(SENDTOGARMIN, SafeXML.strxmlencode(cbSendToGarmin.getState()));
		break;
	    case STYLE_COMPCAT_OUTPUT_SEPARATE:
		exportValues.put(WPNAMESTYLE, "" + chWpNameStyle.selectedIndex);
		exportValues.put(USECUSTOMICONS, SafeXML.strxmlencode(cbUseCustomIcons.getState()));
		exportValues.put(PREFIX, ibPrefix.text);
		break;
	    case STYLE_COMPCAT_OUTPUT_POI:
		exportValues.put(WPNAMESTYLE, "" + chWpNameStyle.selectedIndex);
		exportValues.put(PREFIX, ibPrefix.text);
		break;
	    case STYLE_GPX_PQLIKE:
		exportValues.put(WPNAMESTYLE, "" + chWpNameStyle.selectedIndex);
		exportValues.put(SPLITSIZE, ibSplitSize.text);
		exportValues.put(ATTRIB2LOG, SafeXML.strxmlencode(cbAttrib2Log.getState()));
		exportValues.put(EXPORTADDIWITHINVALIDCOORDS, SafeXML.strxmlencode(cbAddiWithInvalidCoords.getState()));
		exportValues.put(USECUSTOMICONS, SafeXML.strxmlencode(cbUseCustomIcons.getState()));
		exportValues.put(SENDTOGARMIN, SafeXML.strxmlencode(cbSendToGarmin.getState()));
		exportValues.put(EXPORTLOGASPLAINTEXT, SafeXML.strxmlencode(cbExportLogsAsPlainText.getState()));
		exportValues.put(MAXNUMBEROFLOGSTOEXPORT, ibMaxLogs.text);
		break;
	    case STYLE_GPX_MYFINDS:
		exportValues.put(EXPORTLOGASPLAINTEXT, SafeXML.strxmlencode(cbExportLogsAsPlainText.getState()));
		break;
	    }
	    // save is done after input of outputfile
	}
    }
}
