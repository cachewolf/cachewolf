/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

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

import CacheWolf.Attribute;
import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.DateFormat;
import CacheWolf.Global;
import CacheWolf.Log;
import CacheWolf.LogList;
import CacheWolf.MyLocale;
import CacheWolf.SafeXML;
import CacheWolf.Travelbug;
import CacheWolf.utils.FileBugfix;

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
import ewe.sys.Convert;
import ewe.sys.Date;
import ewe.sys.Handle;
import ewe.sys.Process;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
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
 * experimental GPX exporter that should better handle the various tasks that can be accomplished with GPX
 */
public class GpxExportNg {
	/** new line */
	final static String newLine = "\r\n";
	/** decimal separator for lat- and lon-String */
	// final static char decimalSeparator='.';
	/** export is in compact format */
	final static int STYLE_GPX_COMPACT = 0;
	/** export is PQ like */
	final static int STYLE_GPX_PQLIKE = 1;
	/** export follows gc.com MyFinds format */
	final static int STYLE_GPX_MYFINDS = 2;
	/** export uses only waypoint id */
	final static int WPNAME_ID_CLASSIC = 0;
	/** export uses waypointid + type, terrain, difficulty, size */
	final static int WPNAME_ID_SMART = 1;
	/** export uses cache names (will be made unique by gpsbabel) */
	final static int WPNAME_NAME_SMART = 2;
	/** write single GPX file */
	final static int OUTPUT_SINGLE = 0;
	/** write one file per "type" as determined by garminmap.xml */
	final static int OUTPUT_SEPARATE = 1;
	/** generate GPI files with gpsbabel using garminmap.xml types */
	final static int OUTPUT_POI = 2;
	/** name used as key when storing preferences */
	final static String expName = "GpxExportNG";
	/** string representation of true */
	final static String TRUE = "True";
	/** string representation of false */
	final static String FALSE = "False";
	/** object used to determine custom symbols and POI categories */
	private static GarminMap poiMapper = new GarminMap();
	/** maximum number of logs to export. can be overwritten with preferences, default unlimited */
	private int maxLogs = ewe.math.Number.INTEGER_MAX_VALUE;
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

	private static boolean customIcons;
	private static boolean sendToGarmin;
	private static boolean attrib2Log;

	private static int exportIds;
	private static int exportTarget;
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

		hasGpsbabel = Global.getPref().gpsbabel != null;

		finderid = Global.getPref().gcMemberId;
		if (finderid.equals(""))
			Global.getPref().log("GPX Export: warning gcmemberid not set, check pref.xml", null);
	}

	public void doit() {
		GpxExportNgForm exportOptions;
		int ret;

		exportOptions = new GpxExportNgForm(poiMapper.exists, hasBitmaps, hasGpsbabel);
		ret = exportOptions.execute();

		if (FormBase.IDCANCEL == ret) {
			return;
		}

		exportStyle = exportOptions.getExportStyle();
		exportIds = exportOptions.getWpNameStyle();
		exportTarget = exportOptions.getOutputTarget();
		sendToGarmin = exportOptions.getSendToGarmin();
		customIcons = exportOptions.getCustomIcons();
		attrib2Log = exportOptions.getAttrib2Log();

		if (exportTarget == OUTPUT_SEPARATE || exportTarget == OUTPUT_POI) {
			final Hashtable fileHandles = new Hashtable();
			final String outDir;
			final String tempDir;
			final String baseDir = FileBase.getProgramDirectory();
			final String prefix = exportOptions.getPrefix();
			final FileChooser fc;
			ZipFile poiZip = null;

			if (exportTarget == OUTPUT_POI) {
				fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.getPref().getExportPath(expName + "-POI"));
			} else {
				fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.getPref().getExportPath(expName + "-GPI"));
			}

			fc.setTitle("Select target directory:");

			if (fc.execute() == FormBase.IDCANCEL)
				return;

			outDir = fc.getChosenFile().getFullPath();
			if (exportTarget == OUTPUT_POI) {
				Global.getPref().setExportPath(expName + "-POI", outDir);
			} else {
				Global.getPref().setExportPath(expName + "-GPI", outDir);
			}

			if (!poiMapper.exists) {
				Global.getPref().log("GPX Export: unable to load garminmap.xml", null);
				new MessageBox("Export Error", "unable to load garminmap.xml", FormBase.OKB).execute();
				return;
			}

			if (exportTarget == OUTPUT_POI) {
				// FIXME: create proper tempdir
				tempDir = baseDir + FileBase.separator + "GPXExporterNG.tmp";
				new File(tempDir).mkdir();
			} else {
				tempDir = outDir;
				String tmp[] = new FileBugfix(tempDir).list(prefix + "*.gpx", ewe.io.FileBase.LIST_FILES_ONLY);
				for (int i = 0; i < tmp.length; i++) {
					FileBugfix tmpFile = new FileBugfix(tempDir + FileBase.separator + tmp[i]);
					tmpFile.delete();
				}
				tmp = new FileBugfix(tempDir).list(prefix + "*.bmp", ewe.io.FileBase.LIST_FILES_ONLY);
				for (int i = 0; i < tmp.length; i++) {
					FileBugfix tmpFile = new FileBugfix(tempDir + FileBase.separator + tmp[i]);
					tmpFile.delete();
				}
			}

			ProgressBarForm pbf = new ProgressBarForm();
			int poiCounter = 0;
			int poiCategories = 0;
			try {
				Handle h = new Handle();

				int expCount = 0;
				int totalCount = Global.getProfile().cacheDB.countVisible();

				pbf.showMainTask = false;
				pbf.setTask(h, "Exporting ...");
				pbf.exec();

				for (int i = 0; i < Global.getProfile().cacheDB.size(); i++) {
					CacheHolder ch = Global.getProfile().cacheDB.get(i);
					if (!ch.isVisible()) {
						continue;
					} else if (ch.is_incomplete()) {
						Global.getPref().log("skipping export of incomplete waypoint " + ch.getWayPoint(), null);
					} else {
						String poiId = poiMapper.getPoiId(ch);
						if (null == poiId) {
							Global.getPref().log("GPX Export: unmatched POI ID for " + ch.getWayPoint() + " of type " + ch.getType(), null);
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
					Global.getPref().log("GPX Export: warning GarminPOI.zip not found", e, true);
					exportErrors++;
				}

				if (exportTarget == OUTPUT_POI) {
					// only clean up output directory if user has chosen non empty prefix,
					// since otherwise all present POI would be deleted
					if (!prefix.equals("")) {
						String tmp[] = new FileBugfix(outDir).list(prefix + "*.gpi", ewe.io.FileBase.LIST_FILES_ONLY);
						for (int i = 0; i < tmp.length; i++) {
							FileBugfix tmpFile = new FileBugfix(outDir + FileBase.separator + tmp[i]);
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
					if (exportTarget == OUTPUT_POI) {
						poiCounter++;
						h.progress = (float) poiCounter / (float) poiCategories;
						h.changed();
					}
					if (poiZip != null) {
						if (!copyPoiIcon(tempDir, key, prefix, poiZip)) {
							exportErrors++;
							continue;
						}

						if (exportTarget == OUTPUT_POI) {
							String[] cmdStack = new String[9];
							cmdStack[0] = Global.getPref().gpsbabel;
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
								Global.getPref().log("GPX Export: " + errorMsg, null);
								exportErrors++;
							}
							errorStream.close();
						}
					}
				}

				if (exportTarget == OUTPUT_POI) {
					FileBugfix tmpdir = new FileBugfix(tempDir);
					String tmp[] = new FileBugfix(tempDir).list(prefix + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
					for (int i = 0; i < tmp.length; i++) {
						FileBugfix tmpFile = new FileBugfix(tempDir + FileBase.separator + tmp[i]);
						tmpFile.delete();
					}
					tmpdir.delete();
				}

				pbf.exit(0);

			} catch (Exception e) {
				Global.getPref().log("GPX Export: unknown cause for ", e, true);
				exportErrors++;
				pbf.exit(0);
			}
		} else {
			if (customIcons) {
				if (!poiMapper.exists) {
					customIcons = false;
					Global.getPref().log("unable to load garminmap.xml", null);
				}
			}

			if (exportStyle == STYLE_GPX_PQLIKE) {
				maxLogs = exportOptions.getMaxLogs();
				if (maxLogs != Global.getPref().numberOfLogsToExport) {
					Global.getPref().numberOfLogsToExport = maxLogs;
					Global.getPref().dirty = true;
				}
			}

			final File file;

			if (!sendToGarmin) {
				final FileChooser fc = new FileChooser(FileChooserBase.SAVE, Global.getPref().getExportPath(expName + "-GPX"));

				fc.setTitle("Select target GPX file:");
				fc.addMask("*.gpx");

				if (fc.execute() == FormBase.IDCANCEL)
					return;

				file = fc.getChosenFile();
				Global.getPref().setExportPath(expName + "-GPX", file.getPath());
			} else {
				file = new File("").createTempFile("gpxexport", null, null);
			}

			try {
				Vm.showWait(true);
				// ProgressBarForm pbf = new ProgressBarForm();
				// Handle h = new Handle();
				PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				int expCount = 0;
				int totalCount = Global.getProfile().cacheDB.countVisible();

				outp.print(formatHeader());

				// pbf.showMainTask = false;
				// pbf.setTask(h, "Exporting ...");
				// pbf.exec();
				// h.progressResolution=0;

				Global.getPref().log("start: " + new Time().getTime());
				CacheDB cDB = Global.getProfile().cacheDB;
				for (int i = 0; i < cDB.size(); i++) {
					CacheHolder ch = cDB.get(i);
					if (!ch.isVisible()) {
						continue;
					} else if (ch.is_incomplete()) {
						exportErrors++;
						Global.getPref().log("GPX Export: skipping export of incomplete waypoint " + ch.getWayPoint(), null);
					} else {
						String strOut = formatCache(ch);
						if (!strOut.equals("")) {
							outp.print(strOut);
							expCount++;
						}
					}

					// h.progress = expCount / totalCount;
					// h.changed();

					if (Global.mainTab.statBar != null)
						Global.mainTab.statBar.updateDisplay(" " + ((expCount * 100) / totalCount) + "% ");
				}

				Global.getPref().log("stop: " + new Time().getTime());
				if (Global.mainTab.statBar != null)
					Global.mainTab.statBar.updateDisplay("done:" + expCount);
				// pbf.exit(0);

				outp.print("</gpx>" + newLine);
				outp.close();
			} catch (Exception ex) {
				exportErrors++;
				Global.getPref().log("GPX Export: unable to write output to " + file.toString(), ex, true);
				new MessageBox("Export Error", "unable to write output to " + file.toString(), FormBase.OKB).execute();
				return;
			} finally {
				Vm.showWait(false);
			}

			if (sendToGarmin) {
				try {
					String[] cmdStack = new String[9];
					cmdStack[0] = Global.getPref().gpsbabel;
					cmdStack[1] = "-i";
					cmdStack[2] = "gpx";
					cmdStack[3] = "-f";
					cmdStack[4] = file.getCreationName();
					cmdStack[5] = "-o";
					cmdStack[6] = "garmin";
					cmdStack[7] = "-F";
					cmdStack[8] = Global.getPref().garminConn + (":");
					Process babelProcess = null;
					babelProcess = startProcess(cmdStack);
					StreamReader errorStream = new StreamReader(babelProcess.getErrorStream());
					babelProcess.waitFor();
					String errorMsg = errorStream.readALine();
					if (errorMsg != null) {
						Global.getPref().log("GPX Export: " + errorMsg, null);
						exportErrors++;
					}
					errorStream.close();
				} catch (Exception ex) {
					Global.getPref().log("GPX Export error :", ex, true);
				}
				file.delete();
			}
		}
		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors + " errors during export. Check log for details.", FormBase.OKB).execute();
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
		if ((STYLE_GPX_MYFINDS == exportStyle) && (ch.isCustomWpt() || ch.isAddiWpt() || !ch.is_found()))
			return "";

		if (!ch.pos.isValid()) {
			Global.getPref().log("[GPX Export:formatCache] " + ch.getWayPoint() + " has invalid coords.");
			return "";
		}

		StringBuffer ret = new StringBuffer();
		ch.getCacheDetails(true);
		try {
			ret.append(formatCompact(ch));

			if (exportStyle != STYLE_GPX_COMPACT && !(ch.isCustomWpt() || ch.isAddiWpt())) {
				ret.append(formatPqExtensions(ch));
			}

			ret.append("  </wpt>").append(newLine);
		} catch (IllegalArgumentException e) {
			exportErrors++;
			ch.checkIncomplete(); // ch.setIncomplete(true);
			Global.getPref().log("GPX Export: " + ch.getWayPoint() + " check incomplete ", e, true);
			return "";
		} catch (Exception e) {
			exportErrors++;
			Global.getPref().log("GPX Export: " + ch.getWayPoint() + " caused ", e, true);
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

		ret.append("  <wpt lat=\"" + ch.pos.getLatDeg(CWPoint.DD) + "\" lon=\"" + ch.pos.getLonDeg(CWPoint.DD) + "\">").append(newLine);

		if (exportStyle != STYLE_GPX_COMPACT) {
			if (ch.isAddiWpt()) {
				try {
					ret.append("    <time>" + ch.mainCache.getDateHidden() + "T07:00:00Z</time>").append(newLine);
				} catch (Exception e) {
					Global.getPref().log(ch.getWayPoint() + " has no parent", null);
					exportErrors++;
					ret.append("    <time>1970-01-01T00:00:00</time>").append(newLine);
				}
			} else if (ch.isCustomWpt()) {
				ret.append("    <time>1970-01-01T00:00:00</time>").append(newLine);
			} else {
				ret.append("    <time>" + ch.getDateHidden() + "T00:00:00</time>").append(newLine);
			}
		}

		if (exportIds == WPNAME_ID_SMART) {
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
		} else if (exportIds == WPNAME_NAME_SMART) {
			// TBD
		} else {
			ret.append("    <name>").append(SafeXML.cleanGPX(ch.getWayPoint())).append("</name>").append(newLine);
		}

		// no <cmt> for custom
		if (!ch.isCustomWpt()) {
			if (exportIds == WPNAME_ID_SMART && exportStyle == STYLE_GPX_COMPACT) {
				if (ch.isAddiWpt()) {
					ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getCacheName() + " " + ch.getCacheDetails(true).LongDescription)).append("</cmt>").append(newLine);
				} else {
					ret.append("    <cmt>").append(SafeXML.cleanGPX(ch.getCacheName() + " " + Common.rot13(ch.getCacheDetails(true).Hints))).append("</cmt>").append(newLine);
				}
			} else if (exportIds == WPNAME_NAME_SMART) {
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

		if (exportStyle != STYLE_GPX_COMPACT) {
			if (!ch.isCustomWpt()) {
				ret.append("    <url>").append(ch.getCacheDetails(true).URL).append("</url>").append(newLine);
				ret.append("    <urlname>").append(SafeXML.cleanGPX(ch.getCacheName())).append("</urlname>").append(newLine);
			}
		}

		if (customIcons) {
			ret.append("    <sym>").append(poiMapper.getIcon(ch)).append("</sym>").append(newLine);
		} else {
			if (ch.isAddiWpt()) {
				ret.append("    <sym>").append(CacheType.type2SymTag(ch.getType())).append("</sym>").append(newLine);
			} else if (ch.isCustomWpt()) {
				ret.append("    <sym>Custom</sym>").append(newLine);
			} else if (ch.is_found()) {
				ret.append("    <sym>Geocache Found</sym>").append(newLine);
			} else {
				ret.append("    <sym>Geocache</sym>").append(newLine);
			}
		}

		if (exportStyle != STYLE_GPX_COMPACT) {
			ret.append("    <type>").append(CacheType.type2TypeTag(ch.getType())).append("</type>").append(newLine);
		}

		return ret.toString();
	}

	/**
	 * format gc.com extended cache information as found in a PQ
	 * 
	 * @param ch
	 *            cacheholder
	 * @return formatted cache information for cache waypoints or emty string for all other waypoints (additional /
	 *         custom)
	 */
	private String formatPqExtensions(CacheHolder ch) {

		// no details for addis or custom waypoints
		if (ch.isCustomWpt() || ch.isAddiWpt())
			return "";
		StringBuffer ret = new StringBuffer();
		ret.append("    <groundspeak:cache id=\"").append(ch.GetCacheID())//
				.append("\" available=\"").append(ch.is_available() ? TRUE : FALSE)//
				.append("\" archived=\"").append(ch.is_archived() ? TRUE : FALSE)//
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
				.append("      <groundspeak:short_description html=\"").append(ch.is_HTML() ? TRUE : FALSE).append("\"></groundspeak:short_description>").append(newLine)//
				.append("      <groundspeak:long_description html=\"").append(ch.is_HTML() ? TRUE : FALSE).append("\">").append(SafeXML.cleanGPX(formatLongDescription(ch))).append("</groundspeak:long_description>").append(newLine)//
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

	/**
	 * format cache logs as found in a gc.com GPX file
	 * 
	 * @param ch
	 *            CacheHolder containing the logs
	 * @return formatted logs or empty string if no logs are present
	 */
	private String formatLogs(CacheHolder ch) {
		CacheHolderDetail chD = ch.getCacheDetails(false);
		LogList logs = chD.CacheLogs;
		StringBuffer ret = new StringBuffer();
		int exportlogs;
		if (exportStyle == STYLE_GPX_PQLIKE && maxLogs < logs.size()) {
			exportlogs = maxLogs;
		} else {
			exportlogs = logs.size();
		}
		if (exportStyle == STYLE_GPX_MYFINDS) {
			// only own log
			if (chD.OwnLogId.equals("") || chD.OwnLog == null) {
				Global.getPref().log(chD.getParent().getWayPoint() + " missing own LogID", null);
				return "";
			}
			addLog(chD.OwnLogId, chD.OwnLog, finderid, ret);
		} else {
			// add log with attributes
			if (attrib2Log) {
				addLog(ch.GetCacheID() + Integer.toString(exportlogs), createAttrLog(ch), "", ret);
			}
			for (int i = 0; i < exportlogs; i++) {
				addLog(ch.GetCacheID() + Integer.toString(i), logs.getLog(i), "", ret);
			}
		}
		return ret.toString();
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
		Log log = new Log("icon_note.gif", DateFormat.yyyyMMddHHmmss2gpxLogdate(ch.getLastSync()), "CacheWolf", logText.toString());
		return log;
	}

	private StringBuffer addLog(String logId, Log log, String FinderID, StringBuffer ret) {
		Transformer trans = new Transformer(true);
		trans.add(new Regex("@@LOGID@@", logId));
		trans.add(new Regex("@@LOGDATE@@", log.getDate()));
		trans.add(new Regex("@@LOGTYPE@@", CacheHolder.image2TypeText(log.getIcon())));
		trans.add(new Regex("@@LOGFINDERID@@", FinderID));
		trans.add(new Regex("@@LOGFINDER@@", SafeXML.cleanGPX(log.getLogger())));
		trans.add(new Regex("@@LOGENCODE@@", ""));
		trans.add(new Regex("@@LOGTEXT@@", SafeXML.cleanGPX(log.getMessage())));
		ret.append(trans.replaceAll(GPXLOG));
		return ret;
	}

	/**
	 * format the header of the GPX file
	 * 
	 * @return
	 */
	private String formatHeader() {
		// FIXME: extend profile to add <bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\"
		// maxlon=\"6.918817\" />
		// Global.getProfile().getSourroundingArea(false);
		Transformer trans = new Transformer(true);
		trans.add(new Regex("@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString()));
		if (exportStyle == STYLE_GPX_MYFINDS) {
			trans.add(new Regex("@@NAME@@", "My Finds Pocket Query"));
		} else {
			trans.add(new Regex("@@NAME@@", "Waypoints for Cache Listings, Generated by CacheWolf"));
		}
		return trans.replaceFirst(GPXHEADER);
		/*
		String ret = STRreplace.replace(GPXHEADER,"@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString());
		if (exportStyle==STYLE_GPX_MYFINDS)
			 { ret=STRreplace.replace(ret,"@@NAME@@","My Finds Pocket Query");}
		else { ret=STRreplace.replace(ret,"@@NAME@@","Waypoints for Cache Listings, Generated by CacheWolf");}
		return ret;
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
			if (ch.is_HTML()) {
				delim = "<br />";
			} else {
				delim = newLine;
			}
			// FIXME: format is not quite right yet
			// FIXME: cut Addis off in GPXimporter otherwise people who use GPX to feed CacheWolf have them doubled
			if (ch.addiWpts.size() > 0 && exportStyle != STYLE_GPX_MYFINDS) {
				if (ch.is_HTML()) {
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
					trans.add(new Regex("@@ADDILAT@@", formatAddiLatLon(addi.pos)));
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
	 * copy the bitmap identified by <code>prefix</code> and <code>type</code> from <code>poiZip</code> to
	 * <code>outdir</code>
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
			Global.getPref().log("failed to copy icon " + type + ".bmp", e, true);
			return false;
		} catch (IOException e) {
			Global.getPref().log("failed to copy icon " + type + ".bmp", e, true);
			return false;
		}
		return true;
	}

	/**
	 * Execute the command defined by cmd
	 * 
	 * @param cmd
	 *            command and options to execute. if command or options include a space quatation marks are added. this
	 *            will not wirk with the java version on unix systems
	 * @return a handle to the process on success or null otherwise
	 */
	Process startProcess(String[] cmd) {
		String command = "";
		if (cmd.length == 0) {
			exportErrors++;
			Global.getPref().log("GPX Export: empty gpsbabel command", null);
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
			Global.getPref().log("error excuting " + command, e, true);
			exportErrors++;
			return null;
		}
	}

	/**
	 * dialog to set the GPX exporter options
	 */
	private class GpxExportNgForm extends Form {

		private mCheckBox cbCustomIcons, cbSendToGarmin, cbSeperateHints, cbAttrib2Log;
		private mInput ibMaxLogs, ibPrefix;
		private mButton btnOk, btnCancel;

		private boolean hasBitmapsFrm;
		private boolean hasGarminMapFrm;
		private boolean hasGpsbabelFrm;

		private mChoice chStyle, chTarget, chIds;
		private int chosenStyle, chosenTarget, chosenIds;

		/**
		 * set up the form / dialog
		 */
		private GpxExportNgForm(boolean hasGarminMap, boolean hasBitmaps, boolean hasGpsbabel) {

			this.hasBitmapsFrm = hasBitmaps;
			this.hasGarminMapFrm = hasGarminMap;
			this.hasGpsbabelFrm = hasGpsbabel;

			chosenStyle = Global.getProfile().getGpxStyle();
			chosenTarget = Global.getProfile().getGpxTarget();
			chosenIds = Global.getProfile().getGpxId();

			this.setTitle("GPX Export");
			this.resizable = false;

			btnOk = new mButton(MyLocale.getMsg(1605, "OK"));
			btnCancel = new mButton(MyLocale.getMsg(1604, "Cancel"));

			chIds = new mChoice();
			chIds.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chIds.addItem(MyLocale.getMsg(31415, "Classic IDs")); // index 0
			chIds.addItem(MyLocale.getMsg(31415, "Smart IDs")); // index 1
			// chIds.addItem(MyLocale.getMsg(31415,"Smart Names")); // index 2
			chIds.select(chosenIds);

			chStyle = new mChoice();
			chStyle.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chStyle.addItem(MyLocale.getMsg(31415, "Compact")); // index 0
			chStyle.addItem(MyLocale.getMsg(31415, "PQ like")); // index 1
			chStyle.addItem(MyLocale.getMsg(31415, "MyFinds")); // index 2
			chStyle.select(chosenStyle);

			chTarget = new mChoice();
			chTarget.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chTarget.addItem(MyLocale.getMsg(31415, "Single GPX")); // index 0
			chTarget.addItem(MyLocale.getMsg(31415, "Separate GPX")); // index 1
			if (hasBitmaps && hasGarminMap && hasGpsbabel) {
				chTarget.addItem(MyLocale.getMsg(31415, "POI")); // index 2
			}
			chTarget.select(chosenTarget);

			ibPrefix = new mInput("GC-");
			ibPrefix.modify(ControlConstants.Disabled, 0);

			ibMaxLogs = new mInput(String.valueOf(Global.getPref().numberOfLogsToExport));
			ibMaxLogs.modify(ControlConstants.Disabled, 0);

			cbSeperateHints = new mCheckBox(MyLocale.getMsg(31415, "Separate Hints"));
			cbSeperateHints.modify(ControlConstants.Disabled, 0);

			cbSendToGarmin = new mCheckBox(MyLocale.getMsg(31415, "send to Garmin"));
			if (!hasGpsbabel)
				cbSendToGarmin.modify(ControlConstants.Disabled, 0);

			cbCustomIcons = new mCheckBox(MyLocale.getMsg(31415, "Custom Icons"));
			if (!hasGarminMap)
				cbCustomIcons.modify(ControlConstants.Disabled, 0);

			cbAttrib2Log = new mCheckBox(MyLocale.getMsg(31415, "Attrib.->Log"));

			addNext(new mLabel(MyLocale.getMsg(31415, "GPX Style")));
			addLast(chStyle);

			addNext(new mLabel(MyLocale.getMsg(31415, "WP Names")));
			addLast(chIds);

			addNext(new mLabel(MyLocale.getMsg(31415, "Output")));
			addLast(chTarget);

			addNext(cbCustomIcons);
			addLast(cbSendToGarmin);

			// addLast(cbSeperateHints);

			addNext(new mLabel(MyLocale.getMsg(31415, "Prefix")));
			addLast(ibPrefix);

			addLast(cbAttrib2Log);

			addNext(new mLabel(MyLocale.getMsg(31415, "Max Logs")));
			addLast(ibMaxLogs);

			addButton(btnOk);
			addButton(btnCancel);

			checkStyle();
			checkTarget();
			checkIds();
		}

		/**
		 * in : chStyle.selectedIndex; out : chosenStyle
		 */
		private void checkStyle() {
			if (chStyle.selectedIndex == 2) { // my finds export
				chIds.select(0);
				if (chIds.change(ControlConstants.Disabled, 0))
					chIds.repaint();

				chTarget.select(0);
				if (chTarget.change(ControlConstants.Disabled, 0))
					chTarget.repaint();

				if (ibPrefix.change(ControlConstants.Disabled, 0))
					ibPrefix.repaint();

				if (ibMaxLogs.change(ControlConstants.Disabled, 0))
					ibMaxLogs.repaint();

				cbSendToGarmin.state = false;
				if (cbSendToGarmin.change(ControlConstants.Disabled, 0))
					cbSendToGarmin.repaint();

				cbCustomIcons.state = false;
				if (cbCustomIcons.change(ControlConstants.Disabled, 0))
					cbCustomIcons.repaint();

				cbAttrib2Log.state = false;
				if (cbAttrib2Log.change(ControlConstants.Disabled, 0))
					cbAttrib2Log.repaint();

				cbSeperateHints.state = false;
				if (cbSeperateHints.change(ControlConstants.Disabled, 0))
					cbSeperateHints.repaint();

				if (ibMaxLogs.change(ControlConstants.Disabled, 0))
					ibMaxLogs.repaint();

				if (ibPrefix.change(ControlConstants.Disabled, 0))
					ibPrefix.repaint();
			} else if (chStyle.selectedIndex == 1) { // PQ like export
				if (chIds.change(0, ControlConstants.Disabled))
					chIds.repaint();

				chTarget.select(0);
				if (chTarget.change(ControlConstants.Disabled, 0))
					chTarget.repaint();

				if (hasGpsbabelFrm && cbSendToGarmin.change(0, ControlConstants.Disabled))
					cbSendToGarmin.repaint();

				if (hasGarminMapFrm && cbCustomIcons.change(0, ControlConstants.Disabled))
					cbCustomIcons.repaint();

				if (cbAttrib2Log.change(0, ControlConstants.Disabled))
					cbAttrib2Log.repaint();

				cbSeperateHints.state = false;
				if (cbSeperateHints.change(ControlConstants.Disabled, 0))
					cbSeperateHints.repaint();

				if (ibMaxLogs.change(0, ControlConstants.Disabled))
					ibMaxLogs.repaint();

				if (ibPrefix.change(ControlConstants.Disabled, 0))
					ibPrefix.repaint();
			} else { // compact export
				if (chIds.change(0, ControlConstants.Disabled))
					chIds.repaint();

				if (chTarget.change(0, ControlConstants.Disabled))
					chTarget.repaint();

				if (hasGpsbabelFrm && cbSendToGarmin.change(0, ControlConstants.Disabled))
					cbSendToGarmin.repaint();

				if (hasGarminMapFrm && cbCustomIcons.change(0, ControlConstants.Disabled))
					cbCustomIcons.repaint();

				if (cbAttrib2Log.change(ControlConstants.Disabled, 0))
					cbAttrib2Log.repaint();

				cbSeperateHints.state = false;
				if (cbSeperateHints.change(ControlConstants.Disabled, 0))
					cbSeperateHints.repaint();

				if (ibMaxLogs.change(ControlConstants.Disabled, 0))
					ibMaxLogs.repaint();
			}
			chosenStyle = chStyle.selectedIndex;
			chosenTarget = chTarget.selectedIndex;
			chosenIds = chIds.selectedIndex;
		}

		/**
		 * in : chTarget.selectedIndex out: chosenTarget
		 */
		private void checkTarget() {
			if (chTarget.selectedIndex == 2) { // POI
				cbSendToGarmin.state = false;
				if (cbSendToGarmin.change(ControlConstants.Disabled, 0))
					cbSendToGarmin.repaint();

				cbCustomIcons.state = false;
				if (cbCustomIcons.change(ControlConstants.Disabled, 0))
					cbCustomIcons.repaint();

				if (cbSeperateHints.change(0, ControlConstants.Disabled))
					cbSeperateHints.repaint();

				if (ibPrefix.change(0, ControlConstants.Disabled))
					ibPrefix.repaint();
			} else if (chTarget.selectedIndex == 1) { // Separate File
				cbSendToGarmin.state = false;
				if (cbSendToGarmin.change(ControlConstants.Disabled, 0))
					cbSendToGarmin.repaint();

				if (hasBitmapsFrm && cbCustomIcons.change(0, ControlConstants.Disabled))
					cbCustomIcons.repaint();

				if (cbSeperateHints.change(0, ControlConstants.Disabled))
					cbSeperateHints.repaint();

				if (ibPrefix.change(0, ControlConstants.Disabled))
					ibPrefix.repaint();
			} else { // Single GPX
				if (hasGpsbabelFrm && cbSendToGarmin.change(0, ControlConstants.Disabled))
					cbSendToGarmin.repaint();

				if (hasGarminMapFrm && cbCustomIcons.change(0, ControlConstants.Disabled))
					cbCustomIcons.repaint();

				cbSeperateHints.state = false;
				if (cbSeperateHints.change(ControlConstants.Disabled, 0))
					cbSeperateHints.repaint();

				if (ibPrefix.change(ControlConstants.Disabled, 0))
					ibPrefix.repaint();
			}
			chosenStyle = chStyle.selectedIndex;
			chosenTarget = chTarget.selectedIndex;
			chosenIds = chIds.selectedIndex;
		}

		private void checkIds() {
			chosenStyle = chStyle.selectedIndex;
			chosenTarget = chTarget.selectedIndex;
			chosenIds = chIds.selectedIndex;
		}

		/**
		 * react to GUI events and toogle access to the checkboxes according to radio button settings pass everything
		 * else to <code>super()</code>
		 */
		public void onEvent(Event ev) {
			if (ev instanceof DataChangeEvent && ev.type == DataChangeEvent.DATA_CHANGED) {
				if (ev.target == chStyle && chStyle.selectedIndex != chosenStyle) {
					checkStyle();
				} else if (ev.target == chTarget && chTarget.selectedIndex != chosenTarget) {
					checkTarget();
				} else if (ev.target == chIds && chIds.selectedIndex != chosenIds) {
					checkIds();
				}
			} else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
				if (ev.target == btnCancel) {
					close(-1);
				} else if (ev.target == btnOk) {
					Global.getProfile().setGpxStyle(Convert.toString(chosenStyle));
					Global.getProfile().setGpxTarget(Convert.toString(chosenTarget));
					Global.getProfile().setGpxId(Convert.toString(chosenIds));
					if (chosenStyle == GpxExportNg.STYLE_GPX_PQLIKE) {
						try {
							int logs = getMaxLogs();
							if (logs > -1) {
								close(1);
							} else {
								ibMaxLogs.selectAll();
								ibMaxLogs.takeFocus(0);
								Sound.beep();
							}
						} catch (NumberFormatException e) {
							ibMaxLogs.selectAll();
							ibMaxLogs.takeFocus(0);
							Sound.beep();
						}
					} else {
						close(1);
					}
				}
			}

			super.onEvent(ev);
		}

		/**
		 * amount of data to be exported
		 * 
		 * @return 0 Compact, 1 PQ like, 2 MyFinds
		 */
		private int getExportStyle() {
			return chosenStyle;
		}

		/**
		 * style of waypoint identifiers
		 * 
		 * @return 0 Classic IDs, 1 Smart IDs, 3 Smart Names (should only be used with gpsbabel)
		 */
		private int getWpNameStyle() {
			return chosenIds;
		}

		/**
		 * what kind of output should be generated
		 * 
		 * @return 0 single file, 1 separate files, 2 POI (GPI) files
		 */
		private int getOutputTarget() {
			return chosenTarget;
		}

		/**
		 * check if user wants to send output straight to a Garmin GPSr
		 * 
		 * @return true for GPSr transfer, false otherwise
		 */
		private boolean getSendToGarmin() {
			return cbSendToGarmin.state;
		}

		/**
		 * check if user wants custom icons
		 * 
		 * @return true if user wants custom icons, false otherwise
		 */
		private boolean getCustomIcons() {
			return cbCustomIcons.state;
		}

		/**
		 * get the number of logs to export. used in PQlike export.
		 * 
		 * @return number of logs to export
		 */
		private int getMaxLogs() {
			return Convert.parseInt(ibMaxLogs.getText());
		}

		/**
		 * get prefix for separate file export
		 * 
		 * @return prefix for separate file export
		 */
		private String getPrefix() {
			return ibPrefix.getText();
		}

		/**
		 * check if user wants to export attributes as log
		 * 
		 * @return true if attributes should exported as log, false otherwise
		 */
		private boolean getAttrib2Log() {
			return cbAttrib2Log.state;
		}
	}
}
