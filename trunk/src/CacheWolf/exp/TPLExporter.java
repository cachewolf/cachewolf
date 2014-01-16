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

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import HTML.Template;

import com.stevesoft.ewe_pat.Regex;

import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.AsciiCodec;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.PrintWriter;
import ewe.io.TextCodec;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * @author Kalle class to export cachedata using a template
 */
class TplFilter implements HTML.Tmpl.Filter {
    private int type = SCALAR;
    private String newLine = "\n";
    TextCodec codec = new AsciiCodec(); // codec = new AsciiCodec(AsciiCodec.STRIP_CR);
    String badChars;
    String decSep = ".";
    int shortNameLength = 30;
    int shortWaypointLength = 3;
    int noOfLogs = -1; // means all
    boolean single = true;
    int formatModifier = 0;
    int sortedBy = -1;
    boolean getAddiWp = false;
    boolean getMainWp = false;
    boolean getParking = false;
    int copyCacheImages = 0;
    Hashtable additionalVarParams = new Hashtable();
    String userValue = "";
    String out = "*.gpx";

    public TplFilter() {
	return;
    }

    public int format() {
	return this.type;
    }

    public String parse(String t) {

	Regex rex, rex1;
	String param, value;

	// search for parameters
	rex = new Regex("(?i)<tmpl_par.*>");
	rex.search(t);
	if (rex.didMatch()) {
	    // get parameter
	    rex1 = new Regex("(?i)name=\"(.*)\"\\svalue=\"(.*)\"[?\\s>]");
	    rex1.search(t);
	    param = rex1.stringMatched(1);
	    value = rex1.stringMatched(2);

	    if (param.equals("charset")) {
		if (value.equals("ASCII")) {
		    codec = new AsciiCodec();
		} else if (value.equals("UTF8")) {
		    codec = new JavaUtf8Codec();
		} else {
		    codec = new NoCodec();
		}
	    } else if (param.equals("badchars")) {
		badChars = value;
	    } else if (param.equals("newline")) {
		newLine = "";
		if (value.indexOf("CR") >= 0)
		    newLine += "\r";
		if (value.indexOf("LF") >= 0)
		    newLine += "\n";
	    } else if (param.equals("decsep")) {
		decSep = value;
	    } else if (param.equals("ShortNameLength")) {
		shortNameLength = Integer.valueOf(value).intValue();
	    } else if (param.equals("WaypointLength")) {
		shortWaypointLength = Integer.valueOf(value).intValue();
	    } else if (param.equals("NrLogs")) {
		noOfLogs = Integer.valueOf(value).intValue();
	    } else if (param.equals("singleFile")) {
		single = value.equals("yes") ? true : false;
	    } else if (param.equals("formatModifier")) {
		formatModifier = Integer.valueOf(value).intValue();
	    } else if (param.equals("Out")) {
		out = value;
	    } else if (param.equals("takeOnlyWp")) {
		if (value.equals("main")) {
		    getMainWp = true;
		} else if (value.equals("addi")) {
		    getAddiWp = true;
		} else if (value.equals("parking")) {
		    getParking = true;
		}
	    } else if (param.equals("sortedBy")) {
		sortedBy = Integer.valueOf(value).intValue();
	    } else if (param.equals("CopyCacheImages")) {
		if (value.equals("yes"))
		    copyCacheImages = 1;
		if (value.equals("CBX"))
		    copyCacheImages = 2;
	    } else if (param.startsWith("input")) {
		String par = param.substring(5);
		InfoBox inf = new InfoBox("Eingabe", par, InfoBox.INPUT);
		inf.setInput(value);
		if (inf.execute() == FormBase.IDOK) {
		    additionalVarParams.put(par, inf.getInput());
		}
	    } else if (param.startsWith("const")) {
		additionalVarParams.put(param.substring(5), value);
	    }
	    return "";
	}

	if (formatModifier == 0) {
	    // for gpx output
	    // Filter newlines
	    rex = new Regex("(?m)\n$", "");
	    t = rex.replaceAll(t);

	    // Filter comments <#-- and -->
	    rex = new Regex("<#--.*-->", "");
	    t = rex.replaceAll(t);

	    // replace <br> or <br /> with newline
	    rex = new Regex("<br.*>", "");
	    rex.search(t);
	    if (rex.didMatch()) {
		t = rex.replaceAll(t);
		t += newLine;
	    }
	}

	return t;
    }

    public String[] parse(String[] t) {
	throw new UnsupportedOperationException();
    }
}

public class TPLExporter {
    CacheDB cacheDB;
    String tplFile;
    String expName;
    Regex rex = null;
    private static GarminMap gm = null;

    public TPLExporter(String tpl) {
	cacheDB = Global.profile.cacheDB;
	tplFile = tpl;
	File tmpFile = new File(tpl);
	expName = tmpFile.getName();
	expName = expName.substring(0, expName.indexOf("."));
	gm = new GarminMap();
    }

    public void doIt() {

	ProgressBarForm pbf = new ProgressBarForm();
	ewe.sys.Handle h = new ewe.sys.Handle();
	int counter = cacheDB.countVisible();
	pbf.showMainTask = false;
	pbf.setTask(h, "Exporting ...");
	pbf.exec();

	try {
	    TplFilter myFilter = new TplFilter();
	    Hashtable args = new Hashtable();
	    // args.put("debug", "true");
	    args.put("filename", tplFile);
	    args.put("case_sensitive", "true");
	    args.put("loop_context_vars", Boolean.TRUE);
	    args.put("max_includes", new Integer(5));
	    args.put("filter", myFilter);
	    Template tpl = new Template(args);

	    FileChooser fc = new FileChooser(FileChooserBase.SAVE, Global.pref.getExportPath(expName));
	    fc.setTitle("Select target file:");
	    fc.addMask(myFilter.out);
	    if (fc.execute() == FormBase.IDCANCEL) {
		pbf.exit(0);
		return;
	    }
	    File saveTo = fc.getChosenFile();
	    Global.pref.setExportPath(expName, saveTo.getPath());

	    if (myFilter.sortedBy != -1) {
		Global.mainTab.tablePanel.myTableModel.sortTable(myFilter.sortedBy, true);
	    }

	    Regex dec = new Regex("[,.]", myFilter.decSep);
	    if (myFilter.badChars != null)
		rex = new Regex("[" + myFilter.badChars + "]", "");

	    Vector cache_index = new Vector();
	    String imgExpName = "";
	    if (myFilter.copyCacheImages == 1)
		imgExpName = expName;
	    if (myFilter.copyCacheImages == 2)
		imgExpName = expName + "*";
	    for (int i = 0; i < counter; i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isVisible() && (ch.getPos().isValid() || myFilter.formatModifier > 0)) {
		    boolean get = true;
		    if (myFilter.getAddiWp) {
			get = ch.isAddiWpt();
		    } else if (myFilter.getMainWp) {
			get = !ch.isAddiWpt();
		    } else if (myFilter.getParking) {
			get = (ch.getType() == CacheType.CW_TYPE_PARKING)// parking
				|| (!ch.isAddiWpt() && !hasParking(ch));// oder main ohne Parkplatz
		    }
		    if (get) {
			h.progress = (float) i / (float) counter;
			h.changed();
			try {
			    Hashtable varParams = ch.toHashtable(dec, rex, myFilter.shortWaypointLength, myFilter.shortNameLength, myFilter.noOfLogs, myFilter.codec, gm, false, myFilter.formatModifier, imgExpName);

			    Enumeration e = myFilter.additionalVarParams.keys();
			    while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				Object value = myFilter.additionalVarParams.get(key);
				varParams.put(key, value);
			    }

			    if (myFilter.single) {
				cache_index.add(varParams);
			    }

			    else {
				tpl.setParams(varParams);
				String ext = (myFilter.out.substring(myFilter.out.lastIndexOf(".")).toLowerCase() + "    ").trim();
				FileWriter fw = new FileWriter(saveTo.getPath() + ch.getWayPoint() + ext);
				fw.codec = myFilter.codec;
				PrintWriter detfile = new PrintWriter(new BufferedWriter(fw));
				tpl.printTo(detfile);
				detfile.close();
			    }
			} catch (Exception e) {
			    Global.pref.log("[TplExporter:doIt]" + ch.getWayPoint(), e, true);
			}
		    }
		}
	    }
	    if (myFilter.single) {
		tpl.setParam("cache_index", cache_index);
		FileWriter fw = new FileWriter(saveTo);
		fw.codec = myFilter.codec;
		PrintWriter detfile = new PrintWriter(new BufferedWriter(fw));
		tpl.printTo(detfile);
		// oder detfile.print(tpl.output());
		detfile.close();
	    }
	} catch (Exception e) {
	    Global.pref.log("[TplExporter:doIt]", e, true);
	} catch (OutOfMemoryError e) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "Not enough memory available to load all cache data (incl. description and logs)\nexport aborted\nFilter caches to minimise memory needed for TPL-Export\nWe recommend to restart CacheWolf now")
		    .wait(FormBase.OKB);
	}
	pbf.exit(0);
    }

    private boolean hasParking(CacheHolder ch) {
	boolean ret = false;
	if (ch.hasAddiWpt()) {
	    for (int i = 0; i < ch.addiWpts.size(); i++) {
		CacheHolder chwp = (CacheHolder) ch.addiWpts.get(i);
		if (chwp.getType() == CacheType.CW_TYPE_PARKING)
		    return true;
	    }
	}
	return ret;
    }
}

// ##################################################################
class NoCodec implements TextCodec {
    // ##################################################################

    /**
     * This is a creation option. It specifies that CR characters should be removed when encoding text into UTF.
     */
    public static final int STRIP_CR_ON_DECODE = 0x1;
    /**
     * This is a creation option. It specifies that CR characters should be removed when decoding text from UTF.
     */
    public static final int STRIP_CR_ON_ENCODE = 0x2;
    /**
     * This is a creation option. It specifies that CR characters should be removed when decoding text from UTF AND encoding text to UTF.
     */
    public static final int STRIP_CR = STRIP_CR_ON_DECODE | STRIP_CR_ON_ENCODE;

    private int flags = 0;

    // ===================================================================
    public NoCodec(int options)
    // ===================================================================
    {
	flags = options;
    }

    // ===================================================================
    public NoCodec()
    // ===================================================================
    {
	this(0);
    }

    // ===================================================================
    public ByteArray encodeText(char[] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
    // ===================================================================
    {
	if (dest == null)
	    dest = new ByteArray();
	int size = length == 0 ? 2 : 2 + text.length * 2;
	if (dest.data == null || dest.data.length < size)
	    dest.data = new byte[size];
	byte[] destination = dest.data;
	int s = 0;
	if (length > 0) {
	    destination[s++] = (byte) 0xFF;
	    destination[s++] = (byte) 0xFE;
	}
	for (int i = 0; i < length; i++) {
	    char c = text[i + start];
	    if (c == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0))
		continue;
	    destination[s++] = (byte) (c & 0xFF);
	    destination[s++] = (byte) ((c >> 8) & 0xFF);
	}
	dest.length = s;
	return dest;
    }

    // ===================================================================
    public CharArray decodeText(byte[] encoded, int start, int length, boolean endOfData, CharArray dest) throws IOException
    // ===================================================================
    {
	if (dest == null)
	    dest = new CharArray();
	dest.length = 0;
	return dest;
    }

    // ===================================================================
    public void closeCodec() throws IOException
    // ===================================================================
    {
    }

    // ===================================================================
    public Object getCopy()
    // ===================================================================
    {
	return new NoCodec(flags);
    }
    // ##################################################################
}
// ##################################################################
