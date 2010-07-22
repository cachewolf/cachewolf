package CacheWolf.exp;

import CacheWolf.Attribute;
import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
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
 * experimental GPX exporter that should better handle the various tasks that
 * can be accomplished with GPX
 */
public class GpxExportNg {
	/** new line */
	final static String newLine="\r\n";
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
	private static GarminMap poiMapper;
	/** maximum number of logs to export. can be overwritten with preferences, default unlimited*/
	private int maxLogs = ewe.math.Number.INTEGER_MAX_VALUE;
	/** number of errors / warnings during export */
	private int exportErrors = 0;
	/**  */
	private String finderid;

	// we need to fake desc to make clients like GSAK accept additional waypoints together with caches
	final static String GPXHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+newLine
			.concat("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\" creator=\"CacheWolf\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0 http://www.groundspeak.com/cache/1/0/cache.xsd\" xmlns=\"http://www.topografix.com/GPX/1/0\">".concat(newLine))
			.concat("<name>@@NAME@@</name>".concat(newLine))
			.concat("<desc>This is an individual cache generated from Geocaching.com</desc>".concat(newLine))
			.concat("<author>Various users from geocaching.com and/or opencaching.de</author>".concat(newLine))
			.concat("<email>contact@cachewolf.de</email>".concat(newLine))
			.concat("<url>http://www.cachewolf.de/</url>".concat(newLine))
			.concat("<urlname>CacheWolf - Paperless Geocaching</urlname>".concat(newLine))
			.concat("<time>@@CREATEDATE@@T07:00:00Z</time>".concat(newLine))
			.concat("<keywords>cache, geocache, waypoints</keywords>".concat(newLine))
	// TODO: is it worth a second loop?
	// .concat("<bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\" maxlon=\"6.918817\" />")
	;

	final static String GPXLOG = "\t\t\t\t<groundspeak:log id=\"@@LOGID@@\">".concat(newLine)
			.concat("\t\t\t\t\t<groundspeak:date>@@LOGDATE@@T00:00:00</groundspeak:date>".concat(newLine))
			.concat("\t\t\t\t\t<groundspeak:type>@@LOGTYPE@@</groundspeak:type>".concat(newLine))
			.concat("\t\t\t\t\t<groundspeak:finder id=\"@@LOGFINDERID@@\">@@LOGFINDER@@</groundspeak:finder>".concat(newLine))
			.concat("\t\t\t\t\t<groundspeak:text encoded=\"@@LOGENCODE@@\">@@LOGTEXT@@</groundspeak:text>".concat(newLine))
			.concat("\t\t\t\t</groundspeak:log>".concat(newLine));

	final static String GPXTB = "\t\t\t\t<groundspeak:travelbug id=\"@@TBID@@\" ref=\"@@TBREF@@\">".concat(newLine)
			.concat("\t\t\t\t\t<groundspeak:name>@@TBNAME@@</groundspeak:name>".concat(newLine))
			.concat("\t\t\t\t</groundspeak:travelbug>".concat(newLine));

	// FIXME: don't use this until GPX import can strip this off as well
	final static String GPXADDIINMAIN = "@@ADDIID@@ - @@ADDISHORT@@@@ADDIDELIM@@"
			.concat("@@ADDILAT@@ @@ADDILON@@@@ADDIDELIM@@")
			.concat("@@ADDILONG@@@@ADDIDELIM@@");

	private static boolean customIcons;
	private static boolean sendToGarmin;
	private static boolean attrib2Log;

	private static int exportIds;
	private static int exportTarget;
	private static int exportStyle;

	private static boolean hasBitmaps;
	private static boolean hasGarminMap;
	private static boolean hasGpsbabel;

	private static String bitmapFileName;
	private static String garminMapFileName;

	public GpxExportNg() {
		garminMapFileName = FileBase.getProgramDirectory() + "/garminmap.xml";
		bitmapFileName = FileBase.getProgramDirectory() + "/GarminPOI.zip";

		hasGarminMap = new File(garminMapFileName).exists();
		hasBitmaps = new File(bitmapFileName).exists();
		hasGpsbabel = Global.getPref().gpsbabel != null;

		finderid = Global.getPref().gcMemberId;
		if (finderid.equals("")) Global.getPref().log("GPX Export: warning gcmemberid not set, check pref.xml");
	}

	public void doit() {
		GpxExportNgForm exportOptions;
		int ret;

		exportOptions = new GpxExportNgForm(hasGarminMap, hasBitmaps, hasGpsbabel);
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
				fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT,
						Global.getPref().getExportPath(expName + "-GPI"));
			} else {
				fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT,
						Global.getPref().getExportPath(expName + "-POI"));
			}

			fc.setTitle("Select target directory:");

			if (fc.execute() == FormBase.IDCANCEL)
				return;

			outDir = fc.getChosenFile().getFullPath();
			if (exportTarget == OUTPUT_POI) {
				Global.getPref().setExportPath(expName + "-GPI", outDir);
			} else {
				Global.getPref().setExportPath(expName + "-POI", outDir);
			}

			if ((new File(baseDir + "/garminmap.xml")).exists()) {
				poiMapper = new GarminMap();
				poiMapper.readGarminMap();
			} else {
				Global.getPref().log("GPX Export: unable to load garminmap.xml");
				new MessageBox("Export Error", "unable to load garminmap.xml",
						FormBase.OKB).execute();
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
					FileBugfix tmpFile = new FileBugfix(tempDir	+ FileBase.separator + tmp[i]);
					tmpFile.delete();
				}
				tmp = new FileBugfix(tempDir).list(prefix + "*.bmp", ewe.io.FileBase.LIST_FILES_ONLY);
				for (int i = 0; i < tmp.length; i++) {
					FileBugfix tmpFile = new FileBugfix(tempDir	+ FileBase.separator + tmp[i]);
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
						Global.getPref().log(
								"skipping export of incomplete waypoint "
								+ ch.getWayPoint());
					} else {
						String poiId = poiMapper.getPoiId(ch);
						if (null == poiId) {
							Global.getPref().log(
									"GPX Export: unmatched POI ID for "
									+ ch.getWayPoint() + " of type "
									+ ch.getType());
							exportErrors++;
						} else {
							PrintWriter writer;
							if (fileHandles.containsKey(poiId)) {
								writer = (PrintWriter) fileHandles.get(poiId);
							} else {
								writer = new PrintWriter(new BufferedWriter(
										new FileWriter(new File(tempDir
												+ FileBase.separator + prefix
												+ poiId + ".gpx"))));
								fileHandles.put(poiId, writer);
								writer.print(formatHeader());
							}
							writer.print(formatCache(ch));
						}

					}
					expCount++;
					h.progress = (float) expCount / (float) totalCount;
					h.changed();
				}

				try {
					poiZip = new ZipFile(FileBase.getProgramDirectory()	+ FileBase.separator + "GarminPOI.zip");
				} catch (IOException e) {
					Global.getPref().log("GPX Export: warning GarminPOI.zip not found", e, Global.getPref().debug);
					exportErrors++;
				}

				if (exportTarget == OUTPUT_POI) {
					// only clean up output directory if user has chosen non empty prefix,
					// since otherwise all present POI would be deleted
					if (! prefix.equals("")) {
						String tmp[] = new FileBugfix(outDir).list(
								prefix + "*.gpi", ewe.io.FileBase.LIST_FILES_ONLY);
						for (int i = 0; i < tmp.length; i++) {
							FileBugfix tmpFile = new FileBugfix(outDir + FileBase.separator + tmp[i]);
							tmpFile.delete();
						}
					}
					pbf.exit(0);
					poiCategories=fileHandles.size();
					pbf.setTask(h, "Transfer");
					pbf.exec();
				}

				Enumeration keys = fileHandles.keys();
				while (keys.hasMoreElements()) {

					String key = (String) keys.nextElement();
					PrintWriter writer = (PrintWriter) fileHandles.get(key);

					writer.print("</gpx>".concat(newLine));
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
							cmdStack[0]=Global.getPref().gpsbabel;
							cmdStack[1]="-i";
							cmdStack[2]="gpx";
							cmdStack[3]="-f";
							cmdStack[4]=tempDir + FileBase.separator + prefix + key + ".gpx";
							cmdStack[5]="-o";
							cmdStack[6]="garmin_gpi,sleep=1,category="+prefix + key+",bitmap="+tempDir + FileBase.separator + prefix + key	+ ".bmp";
							cmdStack[7]="-F";
							cmdStack[8]=outDir + FileBase.separator + prefix + key + ".gpi";

							Process babelProcess = startProcess(cmdStack);
							StreamReader errorStream = new StreamReader(babelProcess.getErrorStream());
							while (errorStream.isOpen()) {
								String errorMsg = errorStream.readALine();
								if (errorMsg != null) {
									Global.getPref().log("GPX Export: " + errorMsg);
									exportErrors++;
								}
								try {
									babelProcess.exitValue();
									errorStream.close();
								} catch (IllegalThreadStateException e) {
									// still running
								}
							}
						}

					}
				}

				if (exportTarget == OUTPUT_POI) {
					FileBugfix tmpdir = new FileBugfix(tempDir);
					String tmp[] = new FileBugfix(tempDir).list(prefix + "*.*",
							ewe.io.FileBase.LIST_FILES_ONLY);
					for (int i = 0; i < tmp.length; i++) {
						FileBugfix tmpFile = new FileBugfix(tempDir	+ FileBase.separator + tmp[i]);
						tmpFile.delete();
					}
					tmpdir.delete();
				}

				pbf.exit(0);

			} catch (Exception e) {
				Global.getPref().log("GPX Export: unknown cause for ", e, Global.getPref().debug);
				exportErrors++;
				pbf.exit(0);
			}
		} else {
			if (customIcons) {
				if ((new File(FileBase.getProgramDirectory() + "/garminmap.xml")).exists()) {
					poiMapper = new GarminMap();
					poiMapper.readGarminMap();
				} else {
					customIcons = false;
					Global.getPref().log("unable to load garminmap.xml");
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
				final FileChooser fc = new FileChooser(FileChooserBase.SAVE,
						Global.getPref().getExportPath(expName + "-GPX"));

				fc.setTitle("Select target GPX file:");
				fc.addMask("*.gpx");

				if (fc.execute() == FormBase.IDCANCEL)
					return;

				file = fc.getChosenFile();
				Global.getPref()
						.setExportPath(expName + "-GPX", file.getPath());
			} else {
				file = new File("").createTempFile("gpxexport", null, null);
			}

			try {
				ProgressBarForm pbf = new ProgressBarForm();
				Handle h = new Handle();
				PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				int expCount = 0;
				int totalCount = Global.getProfile().cacheDB.countVisible();

				outp.print(formatHeader());

				pbf.showMainTask = false;
				pbf.setTask(h, "Exporting ...");
				pbf.exec();

				if (Global.getPref().debug) Vm.debug("start: "+new Time().getTime());

				for (int i = 0; i < Global.getProfile().cacheDB.size(); i++) {
					CacheHolder ch = Global.getProfile().cacheDB.get(i);
					if (!ch.isVisible()) {
						continue;
					} else if (ch.is_incomplete()) {
						exportErrors++;
						Global.getPref().log("GPX Export: skipping export of incomplete waypoint " + ch.getWayPoint());
					} else {
						outp.print(formatCache(ch));
					}
					expCount++;
					h.progress = (float) expCount / (float) totalCount;
					h.changed();
				}

				Global.getPref().log("stop: "+new Time().getTime());

				pbf.exit(0);

				outp.print("</gpx>".concat(newLine));
				outp.close();
			} catch (Exception ex) {
				exportErrors++;
				if (Global.getPref().debug)
					Global.getPref().log("GPX Export: unable to write output to " + file.toString(), ex, Global.getPref().debug);
				else
					Global.getPref().log("GPX Export: unable to write output to " + file.toString());

				new MessageBox("Export Error", "unable to write output to "	+ file.toString(), FormBase.OKB).execute();
				return;
			}

			if (sendToGarmin) {
				try {
					String[] cmdStack = new String[9];
					cmdStack[0]=Global.getPref().gpsbabel;
					cmdStack[1]="-i";
					cmdStack[2]="gpx";
					cmdStack[3]="-f";
					cmdStack[4]=file.getCreationName();
					cmdStack[5]="-o";
					cmdStack[6]="garmin";
					cmdStack[7]="-F";
					cmdStack[8]=Global.getPref().garminConn.concat(":");

					Process babelProcess = this.startProcess(cmdStack);
					if (babelProcess != null) {
						StreamReader errorStream = new StreamReader(babelProcess.getErrorStream());
						while (errorStream.isOpen()) {
							String errorMsg = errorStream.readALine();
							if (errorMsg != null) {
								Global.getPref().log("GPX Export: " + errorMsg);
								exportErrors++;
							}
							try {
								babelProcess.exitValue();
								errorStream.close();
							} catch (IllegalThreadStateException e) {
								// still running
							}
						}
					}
				} catch (Exception ex) {
					Global.getPref().log("GPX Export error :", ex, Global.getPref().debug);
				}
				file.delete();
			}
		}
		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors	+ " errors during export. Check log for details.",
					FormBase.OKB).execute();
		}
	}

	/**
	 * wrapper for formatting a cache. will call some subroutines to do the actual work
	 * @param ch
	 * @return
	 */
	private String formatCache(CacheHolder ch) {
		// no addis or custom in MyFindsPq - and of course only finds
		if ((STYLE_GPX_MYFINDS == exportStyle) && (ch.isCustomWpt() || ch.isAddiWpt() || !ch.is_found()))
			return "";

		if (!ch.pos.isValid())
			return "";

		StringBuffer ret = new StringBuffer();
		ch.getCacheDetails(true);

		try {
			ret.append(formatCompact(ch));

			if (exportStyle != STYLE_GPX_COMPACT && !(ch.isCustomWpt() || ch.isAddiWpt())) {
				ret.append(formatPqExtensions(ch));
			}

			ret.append("  </wpt>".concat(newLine));
		} catch (IllegalArgumentException e) {
			exportErrors++;
			ch.setIncomplete(true);
			Global.getPref().log("GPX Export: " + ch.getWayPoint() + " set to incomplete ",	e, Global.getPref().debug);
			return "";
		} catch (Exception e) {
			exportErrors++;
			Global.getPref().log("GPX Export: " + ch.getWayPoint() + " caused ", e,	Global.getPref().debug);
			return "";
		}

		return ret.toString();
	}

	/**
	 * generate minimal waypoint information according to GPX specification
	 * @param ch
	 * @return
	 */
	private String formatCompact(CacheHolder ch) {

		StringBuffer ret = new StringBuffer();

//			.concat("\t\t<desc>@@WPDESC@@</desc>".concat(newLine))

		ret.append("  <wpt lat=\""+ch.pos.getLatDeg(CWPoint.DD)+"\" lon=\""+ch.pos.getLonDeg(CWPoint.DD)+"\">".concat(newLine));

		if (exportStyle != STYLE_GPX_COMPACT) {
			if (ch.isAddiWpt()) {
				try {
					ret.append("    <time>"+ch.mainCache.getDateHidden()+"T07:00:00Z</time>".concat(newLine));
				} catch (Exception e) {
					Global.getPref().log(ch.getWayPoint() + " has no parent");
					exportErrors++;
					ret.append("    <time>1970-01-01T00:00:00</time>".concat(newLine));
				}
			} else if (ch.isCustomWpt()) {
				ret.append("    <time>1970-01-01T00:00:00</time>".concat(newLine));
			} else {
				ret.append("    <time>"+ch.getDateHidden()+"T00:00:00</time>".concat(newLine));
			}
		}

		if (exportIds == WPNAME_ID_SMART) {
			if (ch.isAddiWpt()) {
				ret.append("    <name>".concat(SafeXML.cleanGPX(ch.mainCache.getWayPoint().concat(" ").concat(ch.getWayPoint().substring(0, 2)))).concat("</name>".concat(newLine)));
			} else if (ch.isCustomWpt()) {
				ret.append("    <name>".concat(SafeXML.cleanGPX(ch.getWayPoint())).concat("</name>".concat(newLine)));
			} else {
				ret.append("    <name>".concat(SafeXML.cleanGPX(ch.getWayPoint())
						.concat(" ")
						.concat(CacheType.getExportShortId(ch.getType()))
						.concat(String.valueOf(ch.getHard()))
						.concat(String.valueOf(ch.getTerrain()))
						.concat(CacheSize.getExportShortId(ch.getCacheSize())))
						.concat(String.valueOf(ch.getNoFindLogs()))
						.concat("</name>".concat(newLine)));
			}
		} else if (exportIds == WPNAME_NAME_SMART) {
			// TBD
		} else {
			ret.append("    <name>".concat(SafeXML.cleanGPX(ch.getWayPoint())).concat("</name>".concat(newLine)));
		}

		// no <cmt> for custom
		if (!ch.isCustomWpt()) {
			if (exportIds == WPNAME_ID_SMART && exportStyle == STYLE_GPX_COMPACT) {
				if (ch.isAddiWpt()) {
					ret.append("    <cmt>".concat(SafeXML.cleanGPX(ch.getCacheName() + " " + ch.getCacheDetails(true).LongDescription)).concat("</cmt>".concat(newLine)));
				} else {
					ret.append("    <cmt>".concat(SafeXML.cleanGPX(ch.getCacheName() + " " + Common.rot13(ch.getCacheDetails(true).Hints))).concat("</cmt>".concat(newLine)));
				}
			} else if (exportIds == WPNAME_NAME_SMART) {
				// TBD
			} else {
				if (ch.isAddiWpt()) {
					ret.append("    <cmt>".concat(SafeXML.cleanGPX(ch.getCacheDetails(true).LongDescription)).concat("</cmt>".concat(newLine)));
				} // caches have no <cmt> in gc.com PQs
			}
		}

		if (ch.isAddiWpt() || ch.isCustomWpt()) {
			ret.append("    <desc>".concat(SafeXML.cleanGPX(ch.getCacheName())).concat("</desc>".concat(newLine)));
		} else {
			ret.append("    <desc>".concat(SafeXML.cleanGPX(ch.getCacheName().concat(" by ").concat(ch.getCacheOwner()).concat(", ")
					.concat(CacheType.type2GSTypeTag(ch.getType()))
					.concat(" (").concat(CacheTerrDiff.shortDT(ch.getHard()))
					.concat("/").concat(CacheTerrDiff.shortDT(ch.getTerrain())).concat(")")))
					.concat("</desc>".concat(newLine)));
		}

		if (exportStyle != STYLE_GPX_COMPACT) {
			if (!ch.isCustomWpt()) {
				ret.append("    <url>".concat(ch.getCacheDetails(true).URL).concat("</url>".concat(newLine)));
				ret.append("    <urlname>".concat(SafeXML.cleanGPX(ch.getCacheName())).concat("</urlname>".concat(newLine)));
			}
		}

		if (customIcons) {
			ret.append("    <sym>".concat(poiMapper.getIcon(ch)).concat("</sym>".concat(newLine)));
		} else {
			if (ch.isAddiWpt()) {
				ret.append("    <sym>".concat(CacheType.type2SymTag(ch.getType())).concat("</sym>".concat(newLine)));
			} else if (ch.isCustomWpt()) {
				ret.append("    <sym>Custom</sym>".concat(newLine));
			} else if (ch.is_found()) {
				ret.append("    <sym>Geocache found</sym>".concat(newLine));
			} else {
				ret.append("    <sym>Geocache</sym>".concat(newLine));
			}
		}

		if (exportStyle != STYLE_GPX_COMPACT) {
			ret.append("    <type>".concat(CacheType.type2TypeTag(ch.getType())).concat("</type>".concat(newLine)));
		}

		return ret.toString();
	}

	/**
	 * format gc.com extended cache information as found in a PQ
	 * @param ch cacheholder
	 * @return formatted cache information for cache waypoints or emty string for all other waypoints (additional / custom)
	 */
	private String formatPqExtensions(CacheHolder ch) {
		// no details pq details for addis or custom waypoints
		if (ch.isCustomWpt() || ch.isAddiWpt())
			return "";

		return "    <groundspeak:cache id=\"".concat(ch.GetCacheID()).concat("\" available=\"").concat(ch.is_available() ? TRUE : FALSE).concat("\" archived=\"").concat(ch.is_archived() ? TRUE : FALSE).concat("\" xmlns:groundspeak=\"http://www.groundspeak.com/cache/1/0\">".concat(newLine))
		.concat("      <groundspeak:name>").concat(SafeXML.cleanGPX(ch.getCacheName())).concat("</groundspeak:name>".concat(newLine))
		.concat("      <groundspeak:placed_by>").concat(SafeXML.cleanGPX(ch.getCacheOwner())).concat("</groundspeak:placed_by>".concat(newLine))
		.concat("      <groundspeak:owner id=\"").concat("31415").concat("\">").concat(SafeXML.cleanGPX(ch.getCacheOwner())).concat("</groundspeak:owner>".concat(newLine))
		.concat("      <groundspeak:type>").concat(CacheType.type2GSTypeTag(ch.getType())).concat("</groundspeak:type>".concat(newLine))
		.concat("      <groundspeak:container>").concat(CacheSize.cw2ExportString(ch.getCacheSize())).concat("</groundspeak:container>".concat(newLine))
		.concat("      <groundspeak:attributes>".concat(newLine))
		.concat(formatAttributes(ch))
		.concat("      </groundspeak:attributes>".concat(newLine))
		.concat("      <groundspeak:difficulty>").concat(CacheTerrDiff.shortDT(ch.getHard())).concat("</groundspeak:difficulty>".concat(newLine))
		.concat("      <groundspeak:terrain>").concat(CacheTerrDiff.shortDT(ch.getTerrain())).concat("</groundspeak:terrain>".concat(newLine))
		.concat("      <groundspeak:country>").concat(SafeXML.cleanGPX(ch.getCacheDetails(true).Country)).concat("</groundspeak:country>".concat(newLine))
		.concat("      <groundspeak:state>").concat(SafeXML.cleanGPX(ch.getCacheDetails(true).State)).concat("</groundspeak:state>".concat(newLine))
		.concat("      <groundspeak:short_description html=\"").concat(ch.is_HTML() ? TRUE : FALSE).concat("\"></groundspeak:short_description>".concat(newLine))
		.concat("      <groundspeak:long_description html=\"").concat(ch.is_HTML() ? TRUE : FALSE).concat("\">").concat(SafeXML.cleanGPX(formatLongDescription(ch))).concat("</groundspeak:long_description>".concat(newLine))
		.concat("      <groundspeak:encoded_hints>").concat(SafeXML.cleanGPX(Common.rot13(ch.getCacheDetails(true).Hints))).concat("</groundspeak:encoded_hints>".concat(newLine))
		.concat("      <groundspeak:logs>".concat(newLine))
		.concat(formatLogs(ch))
		.concat("      </groundspeak:logs>".concat(newLine))
		.concat("      <groundspeak:travelbugs>".concat(newLine))
		.concat(formatTbs(ch))
		.concat("      </groundspeak:travelbugs>".concat(newLine))
		.concat("    </groundspeak:cache>".concat(newLine));
	}
	/**
	 * 
	 */
	private String formatTbs(CacheHolder ch) {
		StringBuffer ret = new StringBuffer();
		Travelbug Tb;
		for (int i = 0; i < ch.getCacheDetails(true).Travelbugs.size(); i++) {
			Tb=ch.getCacheDetails(true).Travelbugs.getTB(i);
			ret.append("        <groundspeak:travelbug id=\"").
			append(Integer.toString(i)).
			append("\" ref=\"TB\">").
			// append(newLine).
			// append("          <groundspeak:name>").
			append("<groundspeak:name>").
			append(SafeXML.cleanGPX(Tb.getName())).
			append("</groundspeak:name>").
			// append(newLine).
			// append("        </groundspeak:travelbug>\r\n");
			append("</groundspeak:travelbug>\r\n");
		}
		return ret.toString();
	}
	/**
	 * 
	 */
	private String formatAttributes(CacheHolder ch){
		StringBuffer ret = new StringBuffer();
		Attribute attrib;
		for (int i = 0; i < ch.getCacheDetails(true).attributes.count(); i++) {
			// <groundspeak:attribute id="X" inc="Y">text für X</groundspeak:attribute>
			attrib=ch.getCacheDetails(true).attributes.getAttribute(i);
			ret.append("        <groundspeak:attribute id=\"").
			append(attrib.getGCId()).
			append("\" inc=\"").
			append(attrib.getInc()).
			append("\">").
			append(attrib.getGCText()).
			append("</groundspeak:attribute>").
			append(newLine);
		}
		return ret.toString();
	}
	/**
	 * format cache logs as found in a gc.com GPX file
	 * @param ch CacheHolder containing the logs
	 * @return formatted logs or empty string if no logs are present
	 */
	private String formatLogs(CacheHolder ch) {
		LogList logs = ch.getCacheDetails(true).CacheLogs;
		StringBuffer ret = new StringBuffer();
		int exportlogs;
		if (exportStyle == STYLE_GPX_PQLIKE && maxLogs < logs.size())
			{exportlogs = maxLogs;}
		else {exportlogs = logs.size();}
		if (exportStyle == STYLE_GPX_MYFINDS) {
			// only own log
			if (ch.getCacheDetails(true).OwnLogId.equals("")) 
				return "";
			if(ch.getCacheDetails(true).OwnLog == null) 
				return "";
			addLog(ch.getCacheDetails(true).OwnLogId, ch.getCacheDetails(true).OwnLog, finderid ,ret);
		}
		else {
			// add log with attributes
			if (attrib2Log){
				addLog (ch.GetCacheID()+Integer.toString(exportlogs), createAttrLog(ch),"",ret);
			}
			for (int i = 0; i < exportlogs; i++) {
				addLog(ch.GetCacheID()+Integer.toString(i), logs.getLog(i),"" ,ret);
			}
		}
		return ret.toString();
	}
	private Log createAttrLog(CacheHolder ch) {
		Attribute attrib;
		StringBuffer logText = new StringBuffer();
		for (int i = 0; i < ch.getCacheDetails(true).attributes.count(); i++) {
			attrib=ch.getCacheDetails(true).attributes.getAttribute(i);
			logText.append(attrib.getInc()==1?"Yes: ":"No: ").
			append(attrib.getMsg()).
			append("<br />").
			append(newLine);
		}
		Log log = new Log("icon_note.gif", "2000-01-01", "CacheWolf",logText.toString());
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
	 * @return
	 */
	private String formatHeader() {
		//FIXME: extend profile to add <bounds minlat=\"50.91695\" minlon=\"6.876383\" maxlat=\"50.935183\" maxlon=\"6.918817\" />
		//Global.getProfile().getSourroundingArea(false);
		Transformer trans = new Transformer(true);
		trans.add(new Regex("@@CREATEDATE@@", new Date().setToCurrentTime().setFormat("yyyy-MM-dd").toString()));
		if (exportStyle==STYLE_GPX_MYFINDS)
			 {trans.add(new Regex("@@NAME@@","My Finds Pocket Query"));}
		else {trans.add(new Regex("@@NAME@@","Waypoints for Cache Listings, Generated by CacheWolf"));}
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
	 * @param ch CacheHolder to format
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
					ret.append(newLine.concat(newLine)+"<p>Additional Waypoints</p>");
				} else {
					ret.append(newLine.concat(newLine)+"Additional Waypoints".concat(newLine));
				}

				Iterator iter = ch.addiWpts.iterator();
				while (iter.hasNext()) {
					CacheHolder addi = (CacheHolder) iter.next();
					Transformer trans = new Transformer(true);
					trans.add(new Regex("@@ADDIID@@", addi.getWayPoint()));
					trans.add(new Regex("@@ADDISHORT@@", addi.getCacheName()));
					trans.add(new Regex("@@ADDIDELIM@@", delim));
					trans.add(new Regex("@@ADDILAT@@",formatAddiLatLon(addi.pos)));
					trans.add(new Regex("@@ADDILON@@", ""));
					trans.add(new Regex("@@ADDILONG@@",addi.getCacheDetails(true).LongDescription));
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
	 * @return if position is valid return the cachewolf formatted position,
	 *         otherwise return teh string used in PQs
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
			FileOutputStream fos = new FileOutputStream(outdir
					.concat(FileBase.separator).concat(prefix).concat(type).concat(".bmp"));
			while (0 < (len = fis.read(buff)))
				fos.write(buff, 0, len);
			fos.flush();
			fos.close();
			fis.close();
		} catch (ZipException e) {
			Global.getPref().log("failed to copy icon " + type + ".bmp", e,Global.getPref().debug);
			return false;
		} catch (IOException e) {
			Global.getPref().log("failed to copy icon " + type + ".bmp", e,Global.getPref().debug);
			return false;
		}
		return true;
	}

	/**
	 * Execute the command defined by cmd
	 * @param cmd command and options to execute. if command or options include a space quatation marks are added. this will not wirk with the java version on unix systems
	 * @return a handle to the process on success or null otherwise
	 */
	Process startProcess(String[] cmd) {
		String command = "";
		if (cmd.length == 0) {
			exportErrors++;
			Global.getPref().log("GPX Export: empty gpsbabel command");
			return null;
		}

		for (int i = 0; i < cmd.length; i++) {
			if (cmd[i].indexOf(" ") > -1) {
				cmd[i]="\""+cmd[i]+"\"";
			}
			command = command.concat(cmd[i]).concat(" ");
		}

		try {
			return Vm.exec(command);
		} catch (IOException e) {
			Global.getPref().log("error excuting "+command, e, Global.getPref().debug);
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

			btnOk = new mButton(MyLocale.getMsg(1605,"OK"));
			btnCancel = new mButton(MyLocale.getMsg(1604,"Cancel"));

			chIds = new mChoice();
			chIds.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chIds.addItem(MyLocale.getMsg(31415,"Classic IDs")); // index 0
			chIds.addItem(MyLocale.getMsg(31415,"Smart IDs")); // index 1
//			chIds.addItem(MyLocale.getMsg(31415,"Smart Names")); // index 2
			chIds.select(chosenIds);

			chStyle = new mChoice();
			chStyle.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chStyle.addItem(MyLocale.getMsg(31415,"Compact")); // index 0
			chStyle.addItem(MyLocale.getMsg(31415,"PQ like")); // index 1
			chStyle.addItem(MyLocale.getMsg(31415,"MyFinds")); // index 2
			chStyle.select(chosenStyle);

			chTarget = new mChoice();
			chTarget.dontSearchForKeys = true;
			// if you change the order of strings make sure to fix the event handler as well
			chTarget.addItem(MyLocale.getMsg(31415,"Single GPX")); // index 0
			chTarget.addItem(MyLocale.getMsg(31415,"Separate GPX")); // index 1
			if (hasBitmaps && hasGarminMap && hasGpsbabel) {
				chTarget.addItem(MyLocale.getMsg(31415,"POI")); // index 2
			}
			chTarget.select(chosenTarget);

			ibPrefix = new mInput("GC-");
			ibPrefix.modify(ControlConstants.Disabled, 0);

			ibMaxLogs = new mInput(String.valueOf(Global.getPref().numberOfLogsToExport));
			ibMaxLogs.modify(ControlConstants.Disabled, 0);

			cbSeperateHints = new mCheckBox(MyLocale.getMsg(31415,"Separate Hints"));
			cbSeperateHints.modify(ControlConstants.Disabled, 0);

			cbSendToGarmin = new mCheckBox(MyLocale.getMsg(31415,"send to Garmin"));
			if (!hasGpsbabel) cbSendToGarmin.modify(ControlConstants.Disabled, 0);

			cbCustomIcons = new mCheckBox(MyLocale.getMsg(31415,"Custom Icons"));
			if (!hasGarminMap) cbCustomIcons.modify(ControlConstants.Disabled, 0);

			cbAttrib2Log = new mCheckBox(MyLocale.getMsg(31415,"Attrib.->Log"));
			
			addNext(new mLabel(MyLocale.getMsg(31415,"GPX Style")));
			addLast(chStyle);

			addNext(new mLabel(MyLocale.getMsg(31415,"WP Names")));
			addLast(chIds);

			addNext(new mLabel(MyLocale.getMsg(31415,"Output")));
			addLast(chTarget);

			addNext(cbCustomIcons);
			addLast(cbSendToGarmin);

//			addLast(cbSeperateHints);

            addNext(new mLabel(MyLocale.getMsg(31415,"Prefix")));
            addLast(ibPrefix);
            
            addLast(cbAttrib2Log);
            
            addNext(new mLabel(MyLocale.getMsg(31415,"Max Logs")));
            addLast(ibMaxLogs);

			addButton(btnOk);
			addButton(btnCancel);

			checkStyle();
			checkTarget();
			checkIds();
		}
		
		/**
		 * in  : chStyle.selectedIndex;
		 * out : chosenStyle
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
		 * in : chTarget.selectedIndex
		 * out: chosenTarget
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

				cbSeperateHints.state=false;
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
		 * react to GUI events and toogle access to the checkboxes according to
		 * radio button settings pass everything else to <code>super()</code>
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
		 * @return 0 Compact, 1 PQ like, 2 MyFinds
		 */
		private int getExportStyle () {
			return chosenStyle;
		}

		/**
		 * style of waypoint identifiers
		 * @return 0 Classic IDs, 1 Smart IDs, 3 Smart Names (should only be used with gpsbabel)
		 */
		private int getWpNameStyle() {
			return chosenIds;
		}

		/**
		 * what kind of output should be generated
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
		private boolean getAttrib2Log (){
			return cbAttrib2Log.state;
		}
	}
}
