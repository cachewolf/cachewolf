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
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.MyLocale;

import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;

import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.PrintWriter;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

/**
 * @author Kalle Base class for exporter, handles basic things like selecting outputfile, display a counter etc. A new Exporter must only override the header(), record() and trailer() methods. The member howManyParams must be set to identify which
 *         ethod should be called
 */

public class Exporter {
    // starts with no ui for file selection
    public final static int TMP_FILE = 0;
    // brings up a screen to select a file
    public final static int ASK_FILE = 1;
    public final static int ASK_PATH = 2;

    // export methods
    final static int NO_PARAMS = 0;
    final static int LAT_LON = 1;
    final static int COUNT = 2;

    CacheDB cacheDB;
    // mask in file chooser
    String outputFileExtension = "*.*";
    // file name, if no file chooser is used
    String tmpFileName;
    // decimal separator for lat- and lon-String
    char decimalSeparator = '.';
    // selection, which export method should be called
    int exportMethod = 0;

    // name of exporter for saving pathname
    protected String expName;

    public Exporter() {
	cacheDB = MainForm.profile.cacheDB;
	exportMethod = LAT_LON;
	expName = this.getClass().getName();
	// remove package
	expName = expName.substring(expName.indexOf(".") + 1);
    }

    public void doIt() {
	this.doIt(ASK_FILE);
    }

    /**
     * Does the most work for exporting data
     * 
     * @param variant<br>
     *            0 = TMP_FILE: use temporary file<br>
     *            1 = ASK_FILE: ask for filename (with filechooser)<br>
     */
    public void doIt(int variant) {
	File outFile = null;
	String outPath = "";
	byte[] cacheTypes;
	byte[] ct = { CacheType.CW_TYPE_CITO, CacheType.CW_TYPE_CUSTOM, CacheType.CW_TYPE_EARTH, CacheType.CW_TYPE_FINAL, CacheType.CW_TYPE_EVENT //
		, CacheType.CW_TYPE_GIGA_EVENT, CacheType.CW_TYPE_LAB, CacheType.CW_TYPE_LETTERBOX, CacheType.CW_TYPE_MAZE //
		, CacheType.CW_TYPE_MEGA_EVENT, CacheType.CW_TYPE_MULTI, CacheType.CW_TYPE_PARKING, CacheType.CW_TYPE_QUESTION //
		, CacheType.CW_TYPE_REFERENCE, CacheType.CW_TYPE_STAGE, CacheType.CW_TYPE_TRADITIONAL, CacheType.CW_TYPE_TRAILHEAD //
		, CacheType.CW_TYPE_UNKNOWN, CacheType.CW_TYPE_VIRTUAL, CacheType.CW_TYPE_WEBCAM, CacheType.CW_TYPE_WHEREIGO //
	};
	byte[] ca = { -1 };
	String[] ctName = { "CITO", "Custom", "Earthcache", "Final", "Event" //
		, "GigaEvent", "Lab", "Letterbox", "AdventureMaze" //
		, "MegaEvent", "Multi", "Parking", "Question" //
		, "Reference", "Stage", "Traditional", "Trailhead" //
		, "Mystery", "Virtual", "Webcam", "WhereIGo" };
	String str;
	boolean hasBitmaps = false;
	ZipFile poiZip = null;
	CacheHolder ch;
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();

	if (variant == ASK_FILE) {
	    outFile = getOutputFile();
	    if (outFile == null)
		return;
	    cacheTypes = ca;
	} else if (variant == ASK_PATH) {
	    outPath = this.getOutputPath();
	    cacheTypes = ct;
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

	} else {
	    outFile = new File(tmpFileName);
	    cacheTypes = ca;
	}

	pbf.showMainTask = false;
	pbf.setTask(h, "Exporting ...");
	pbf.exec();

	int counter = cacheDB.countVisible();
	int expCount = 0;

	try {
	    int incompleteWaypoints = 0;
	    for (int j = 0; j < cacheTypes.length; j++) {
		byte forType = cacheTypes[j];
		if (variant == ASK_PATH) {
		    outFile = new File(outPath + ctName[j] + ".gpx");
		    if (hasBitmaps)
			copyPoiIcon(outPath, ctName[j], "", poiZip);
		}
		PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
		str = this.header();
		if (str != null)
		    outp.print(str);
		for (int i = 0; i < cacheDB.size(); i++) {
		    ch = cacheDB.get(i);
		    if (ch.isVisible()) {
			if (ch.isIncomplete()) {
			    Preferences.itself().log("skipping export of incomplete waypoint " + ch.getCode());
			    incompleteWaypoints++;
			    continue;
			}
			if (forType != -1) {
			    if (ch.getType() != forType) {
				continue;
			    }
			}
			expCount++;
			h.progress = (float) expCount / (float) counter;
			h.changed();
			switch (this.exportMethod) {
			case NO_PARAMS:
			    str = record(ch);
			    break;
			case LAT_LON:
			    if (!ch.getWpt().isValid())
				continue;
			    str = record(ch, ch.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), ch.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator));
			    break;
			case LAT_LON | COUNT:
			    if (!ch.getWpt().isValid())
				continue;
			    str = record(ch, ch.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), ch.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), i);
			    break;
			default:
			    str = null;
			    break;
			}
			if (str != null)
			    outp.print(str);
		    }// if
		}// for
		switch (this.exportMethod & COUNT) {
		case NO_PARAMS:
		    str = trailer();
		    break;
		case COUNT:
		    str = trailer(counter);
		    break;
		default:
		    str = null;
		    break;
		}
		if (str != null)
		    outp.print(str);
		outp.close();

	    } // for 

	    pbf.exit(0);
	    if (hasBitmaps)
		poiZip.close();
	    if (incompleteWaypoints > 0) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), incompleteWaypoints + " incomplete waypoints have not been exported. See log for details.").wait(FormBase.OKB);
	    }
	} catch (IOException ioE) {
	    Preferences.itself().log("Error opening " + outFile.getName(), ioE);
	}
	// try
    }

    /**
     * sets mask for filechooser
     * 
     * @param mask
     */
    public void setOutputFileExtension(String mask) {
	this.outputFileExtension = mask;
    }

    /**
     * sets howManyParams
     * 
     * @param paramBits
     */
    public void setHowManyParams(int paramBits) {
	this.exportMethod = paramBits;
    }

    /**
     * sets tmpFileName
     * 
     * @param fName
     */
    public void setTmpFileName(String fName) {
	this.tmpFileName = fName;
    }

    /**
     * uses a filechooser to get the name of the export file
     * 
     * @return
     */
    public File getOutputFile() {
	File file;
	FileChooser fc = new FileChooser(FileChooserBase.SAVE, Preferences.itself().getExportPath(expName));
	fc.setTitle(MyLocale.getMsg(2102, "Choose target file"));
	fc.addMask(outputFileExtension);
	if (fc.execute() != FormBase.IDCANCEL) {
	    file = fc.getChosenFile();
	    Preferences.itself().setExportPref(expName, file.getPath());
	    return file;
	} else {
	    return null;
	}
    }

    public String getOutputPath() {
	FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().getExportPath(expName));
	fc.setTitle(MyLocale.getMsg(148, "Select Target directory"));
	String targetDir;
	if (fc.execute() == FormBase.IDCANCEL)
	    return "";
	targetDir = fc.getChosen() + "/";
	Preferences.itself().setExportPref(expName, targetDir);
	return targetDir;
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

    /**
     * this method can be overided by an exporter class
     * 
     * @return formated header data
     */
    public String header() {
	return null;
    }

    /**
     * this method can be overided by an exporter class
     * 
     * @param ch
     *            cachedata
     * @return formated cache data
     */
    public String record(CacheHolder chD) {
	return null;
    }

    /**
     * this method can be overided by an exporter class
     * 
     * @param ch
     *            cachedata
     * @param lat
     * @param lon
     * @return formated cache data
     */
    public String record(CacheHolder ch, String lat, String lon) {
	return null;
    }

    /**
     * this method can be overided by an exporter class
     * 
     * @param ch
     *            cachedata
     * @param lat
     * @param lon
     * @param count
     *            of actual record
     * @return formated cache data
     */
    public String record(CacheHolder ch, String lat, String lon, int count) {
	return null;
    }

    /**
     * this method can be overided by an exporter class
     * 
     * @return formated trailer data
     */
    public String trailer() {
	return null;
    }

    /**
     * this method can be overided by an exporter class
     * 
     * @param total
     *            count of exported caches
     * @return
     */
    public String trailer(int total) {
	return null;
    }

    // /////////////////////////////////////////////////
    // Helper functions for string sanitisation
    // /////////////////////////////////////////////////

    private static Hashtable iso2simpleMappings = new Hashtable(250);
    static {
	String[] mappingArray = new String[] { "34", "'", //
		"160", " ", "161", "i", "162", "c", "163", "$", "164", "o", "165", "$", "166", "!", "167", "$", "168", " ", "169", " ", //
		"170", " ", "171", "<", "172", " ", "173", "-", "174", " ", "175", "-", "176", " ", "177", "+/-", "178", "2", "179", "3", //
		"180", "'", "181", " ", "182", " ", "183", " ", "184", ",", "185", "1", "186", " ", "187", ">", "188", "1/4", "189", "1/2", //
		"190", "3/4", "191", "?", "192", "A", "193", "A", "194", "A", "195", "A", "196", "Ae", "197", "A", "198", "AE", "199", "C", //
		"200", "E", "201", "E", "202", "E", "203", "E", "204", "I", "205", "I", "206", "I", "207", "I", "208", "D", "209", "N", //
		"210", "O", "211", "O", "212", "O", "213", "O", "214", "Oe", "215", "x", "216", "O", "217", "U", "218", "U", "219", "U", //
		"220", "Ue", "221", "Y", "222", " ", "223", "ss", "224", "a", "225", "a", "226", "a", "227", "a", "228", "ae", "229", "a", //
		"230", "ae", "231", "c", "232", "e", "233", "e", "234", "e", "235", "e", "236", "i", "237", "i", "238", "i", "239", "i", //
		"240", "o", "241", "n", "242", "o", "243", "o", "244", "o", "245", "o", "246", "oe", "247", "/", "248", "o", "249", "u", //
		"250", "u", "251", "u", "252", "ue", "253", "y", "254", "p", "255", "y" };
	for (int i = 0; i < mappingArray.length; i = i + 2) {
	    iso2simpleMappings.put(Integer.valueOf(mappingArray[i]), mappingArray[i + 1]);
	}
    }

    protected static String char2simpleChar(char c) {
	if (c < 127) {
	    // leave alone as equivalent string.
	    return null;
	} else {
	    String s = (String) iso2simpleMappings.get(new Integer(c));
	    if (s == null) // 127..159 not in table, replace with empty string
		return "";
	    else
		return s;
	}
    } // end charToEntity

    public static String simplifyString(String text) {
	if (text == null)
	    return null;
	int originalTextLength = text.length();
	StringBuffer sb = new StringBuffer(50);
	int charsToAppend = 0;
	for (int i = 0; i < originalTextLength; i++) {
	    char c = text.charAt(i);
	    String entity = char2simpleChar(c);
	    if (entity == null) {
		// we could sb.append( c ), but that would be slower
		// than saving them up for a big append.
		charsToAppend++;
	    } else {
		if (charsToAppend != 0) {
		    sb.append(text.substring(i - charsToAppend, i));
		    charsToAppend = 0;
		}
		sb.append(entity);
	    }
	} // end for
	  // append chars to the right of the last entity.
	if (charsToAppend != 0) {
	    sb.append(text.substring(originalTextLength - charsToAppend, originalTextLength));
	}
	// if result is not longer, we did not do anything. Save RAM.
	return (sb.length() == originalTextLength) ? text : sb.toString();
    } // end insertEntities

    public static String getShortDetails(CacheHolder ch) {
	StringBuffer strBuf = new StringBuffer(7);
	strBuf.append(CacheType.getExportShortId(ch.getType()).toLowerCase());
	if (!ch.isAddiWpt()) {
	    strBuf.append(ch.getDifficulty());
	    strBuf.append("/");
	    strBuf.append(ch.getTerrain());
	    strBuf.append(CacheSize.getExportShortId(ch.getSize()));
	}

	return strBuf.toString();
    }

    protected static String removeHtmlTags(String inString) {

	Transformer removeNumericEntities = new Transformer(true);
	removeNumericEntities.add(new Regex("&#([xX]?)([a-fA-F0-9]*?);", ""));

	Transformer handleLinebreaks = new Transformer(true);
	handleLinebreaks.add(new Regex("\r", ""));
	handleLinebreaks.add(new Regex("\n", " "));
	handleLinebreaks.add(new Regex("<br>", "\n"));
	handleLinebreaks.add(new Regex("<p>", "\n"));
	handleLinebreaks.add(new Regex("<hr>", "\n"));
	handleLinebreaks.add(new Regex("<br />", "\n"));

	Transformer removeHTMLTags = new Transformer(true);
	removeHTMLTags.add(new Regex("<(.*?)>", ""));

	return removeHTMLTags.replaceAll(handleLinebreaks.replaceAll(removeNumericEntities.replaceAll(inString)));

    }
}
