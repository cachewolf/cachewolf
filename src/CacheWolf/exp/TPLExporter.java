/*
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
    the Free Software Foundation version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */

package CacheWolf.exp;

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Global;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.Common;
import HTML.Template;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.*;
import ewe.sys.*;
import ewe.ui.*;
import ewe.util.*;
import com.stevesoft.ewe_pat.*;

/**
 * @author Kalle
 * class to export cachedata using a template
 */
class TplFilter implements HTML.Tmpl.Filter
{
	private int type=SCALAR;
	private String newLine="\n";
	TextCodec codec = new AsciiCodec();
	String badChars;
	String decSep = ".";
	int shortNameLength=30;
	int shortWaypointLength=3;
	String out="*.gpx";


	public TplFilter(){
		codec = new AsciiCodec(AsciiCodec.STRIP_CR);
		return;
	}

	public int format() {
		return this.type;
	}

	public String parse(String t) {
		//Vm.debug(t);
		Regex rex, rex1;
		String param, value;
		// Filter newlines
		rex = new Regex("(?m)\n$","");
		t = rex.replaceAll(t);

		// Filter comments <#-- and -->
		rex = new Regex("<#--.*-->","");
		t = rex.replaceAll(t);

		// replace <br> or <br /> with newline
		rex = new Regex("<br.*>","");
		rex.search(t);
		if (rex.didMatch()){
			t = rex.replaceAll(t);
			t += newLine;
		}

		// search for parameters
		rex = new Regex("(?i)<tmpl_par.*>");
		rex.search(t);
		if (rex.didMatch()){
			// get parameter
			rex1 = new Regex("(?i)name=\"(.*)\"\\svalue=\"(.*)\"[?\\s>]");
			rex1.search(t);
			param = rex1.stringMatched(1);
			value = rex1.stringMatched(2);
			//Vm.debug("param=" + param + "\nvalue=" + value);
			//clear t, because we allow only one parameter per line
			t = "";

			// get the values
			if (param.equals("charset")) {
				if (value.equals("ASCII")) {codec = new AsciiCodec();}
				else if (value.equals("UTF8")) {codec = new JavaUtf8Codec();}
				else {codec = new NoCodec();}
			}
			if (param.equals("badchars")) {
				badChars = value;
			}
			if (param.equals("newline")){
				newLine = "";
				if (value.indexOf("CR") >= 0) newLine += "\r";
				if (value.indexOf("LF") >= 0) newLine += "\n";
			}
			if (param.equals("decsep")) {
				decSep = value;
			}
			if (param.equals("ShortNameLength")) {
				shortNameLength = Integer.valueOf(value).intValue();
			}
			if (param.equals("WaypointLength")) {
				shortWaypointLength = Integer.valueOf(value).intValue();
			}
			if (param.equals("Out")) {
				out = value;
			}


		}
		return t;
	}


	public String [] parse(String [] t) {
		throw new UnsupportedOperationException();
	}
}


public class TPLExporter {
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String tplFile;
	String expName;
	Regex rex=null;
	private static GarminMap gm=null;

	public TPLExporter(Preferences p, Profile prof, String tpl){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
		tplFile = tpl;
		File tmpFile = new File(tpl);
		expName = tmpFile.getName();
		expName = expName.substring(0, expName.indexOf("."));
		gm=new GarminMap();
		gm.readGarminMap();
	}

	public void doIt(){
		CacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		ewe.sys.Handle h = new ewe.sys.Handle();
		int counter = cacheDB.countVisible();
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();
		// Vm.gc(); // all this doesn't really work :-(
		// System.runFinalization();
		// Vm.gc();
		//Vm.debug("v: "+Vm.countObjects(true));
		String selbstLaute="aeiouAEIOU";
		StringBuffer lower=new StringBuffer(26);// region/language dependent ?
		for (int i=97; i<=122; i++ ) {
			lower.append((char) i);
		}
		String mitLauteKlein=lower.toString();
		try {
			Vector cache_index = new Vector(); // declare variables inside try {} -> in case of OutOfMemoryError, they can be garbage collected - anyhow it doesn't work :-(
			Hashtable varParams;
			TplFilter myFilter;
			Hashtable args = new Hashtable();
			myFilter = new TplFilter();
			//args.put("debug", "true");
			args.put("filename", tplFile);
			args.put("case_sensitive", "true");
			args.put("loop_context_vars", Boolean.TRUE);
			args.put("max_includes", new Integer(5));
			args.put("filter", myFilter);
			Template tpl = new Template(args);

			FileChooser fc = new FileChooser(FileChooserBase.SAVE, pref.getExportPath(expName));
			fc.setTitle("Select target file:");
			fc.addMask(myFilter.out);
			if(fc.execute() == FormBase.IDCANCEL) return;
			File saveTo = fc.getChosenFile();
			pref.setExportPath(expName, saveTo.getPath());
			Time nowdate = new Time();
			nowdate = nowdate.setFormat("yyyy-MM-dd");
			Time nowtime = new Time();
			nowtime = nowtime.setFormat("HH:mm");

			for(int i = 0; i<counter;i++){
				ch = cacheDB.get(i);
				det = ch.getExistingDetails();
				h.progress = (float)i/(float)counter;
				h.changed();
				if(ch.isVisible()){
					if (ch.pos.isValid() == false) continue;
					try {
						Regex dec = new Regex("[,.]",myFilter.decSep);
						if (myFilter.badChars != null) rex = new Regex("["+myFilter.badChars+"]","");
						varParams = new Hashtable();
						varParams.put("TYPE", CacheType.type2TypeTag(ch.getType())); //<type>
						varParams.put("SYM", CacheType.type2SymTag(ch.getType())); //<sym>
						varParams.put("GSTYPE", CacheType.type2GSTypeTag(ch.getType())); //<groundspeak:type>
						varParams.put("SHORTTYPE", CacheType.getExportShortId(ch.getType()));
						varParams.put("SIZE", CacheSize.cw2ExportString(ch.getCacheSize()));
						varParams.put("SHORTSIZE", CacheSize.getExportShortId(ch.getCacheSize()));
						if (ch.isAddiWpt()) {
							varParams.put("MAINWP",ch.mainCache.getWayPoint());
						}
						else {
							varParams.put("MAINWP", "");
						}
						if (ch.isCustomWpt()) {

						}
						String wp = ch.getWayPoint();
						varParams.put("WAYPOINT", wp); //<name>
						int wpl = wp.length();
						int wps = (wpl < myFilter.shortWaypointLength) ? 0 : wpl - myFilter.shortWaypointLength;
						varParams.put("SHORTWAYPOINT", wp.substring(wps, wpl));
						varParams.put("OWNER", ch.getCacheOwner());
						byte chGetHard=ch.getHard();
						varParams.put("DIFFICULTY", (ch.isAddiWpt() || ch.isCustomWpt() || chGetHard < 0)?"":dec.replaceAll(CacheTerrDiff.longDT(chGetHard)));
						String sHard = Integer.toString(chGetHard);
						varParams.put("SHORTDIFFICULTY", (ch.isAddiWpt() || ch.isCustomWpt() || chGetHard < 0)?"":sHard);
						varParams.put("SHDIFFICULTY", (ch.isAddiWpt() || ch.isCustomWpt() || chGetHard < 0)?"":sHard.substring(0,1));
						byte chGetTerrain=ch.getTerrain();
						varParams.put("TERRAIN", (ch.isAddiWpt() || ch.isCustomWpt() || chGetTerrain < 0)?"":dec.replaceAll(CacheTerrDiff.longDT(chGetTerrain)));
						String sTerrain = Integer.toString(chGetTerrain);
						varParams.put("SHORTTERAIN", (ch.isAddiWpt() || ch.isCustomWpt() || chGetTerrain < 0)?"":sTerrain);
						varParams.put("SHTERRAIN", (ch.isAddiWpt() || ch.isCustomWpt() || chGetTerrain < 0)?"":sTerrain.substring(0,1));
						varParams.put("DISTANCE", dec.replaceAll(ch.getDistance()));
						varParams.put("BEARING", ch.bearing);
						varParams.put("LATLON", ch.LatLon);
						varParams.put("LAT", dec.replaceAll(ch.pos.getLatDeg(CWPoint.DD)));
						varParams.put("LON", dec.replaceAll(ch.pos.getLonDeg(CWPoint.DD)));
						varParams.put("STATUS", ch.getCacheStatus());
						varParams.put("GC_LOGTYPE", ch.GetGCLogType());
						varParams.put("STATUS_DATE", ch.GetStatusDate());
						varParams.put("STATUS_TIME", ch.GetStatusTime());
						varParams.put("DATE", ch.getDateHidden());
						varParams.put("URL", det != null ? det.URL : "");
						varParams.put("DESCRIPTION", det != null ? det.LongDescription : "");
						String cacheName=ch.getCacheName();
						if (myFilter.codec instanceof AsciiCodec) {
							cacheName=Exporter.simplifyString(cacheName);
						}
						if (myFilter.badChars != null) {
							cacheName=rex.replaceAll(cacheName);
							varParams.put("NOTES", det != null ? rex.replaceAll(det.getCacheNotes()): "");
							varParams.put("HINTS", det != null ? rex.replaceAll(det.Hints): "");
							varParams.put("DECRYPTEDHINTS", det != null ? rex.replaceAll(Common.rot13(det.Hints)): "");
						} else {
							varParams.put("NOTES", det != null ? det.getCacheNotes(): "");
							varParams.put("HINTS", det != null ? det.Hints: "");
							varParams.put("DECRYPTEDHINTS", det != null ? Common.rot13(det.Hints): "");
						}
						varParams.put("NAME", cacheName);
						String shortName=removeCharsfromString(cacheName, myFilter.shortNameLength, selbstLaute);
						if (shortName.length()>myFilter.shortNameLength) {
							shortName=removeCharsfromString(shortName, myFilter.shortNameLength, mitLauteKlein);
						}
						varParams.put("SHORTNAME", shortName);
						varParams.put("TRAVELBUG", (ch.has_bugs()?"Y":"N"));
						varParams.put("GMTYPE", gm.getIcon(ch));
						varParams.put("NOW_DATE",nowdate.setToCurrentTime().toString());
						varParams.put("NOW_TIME",nowtime.setToCurrentTime().toString());

						cache_index.add(varParams);
					}catch(Exception e){
						Vm.debug("Problem getting Parameter, Cache: " + ch.getWayPoint());
						e.printStackTrace();
						Global.getPref().log("Exception in TplExporter = Problem getting Parameter, Cache: " + ch.getWayPoint(), e, true);
					}
				}
			}
			tpl.setParam("cache_index", cache_index);
			PrintWriter detfile;
			FileWriter fw = new FileWriter(saveTo);
			fw.codec = myFilter.codec;
			detfile = new PrintWriter(new BufferedWriter(fw));
			tpl.printTo(detfile);
			//detfile.print(tpl.output());
			detfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			Global.getPref().log("Exception in TplExporter", e, true);
		} catch (OutOfMemoryError e) {
			// Global.getPref().log("OutOfMemeory in TplExporter", e, true);
			// Vm.gc(); // this doesn't help :-(
			// System.runFinalization();
			// Vm.gc(); // this doesn't help :-( - I don't know why :-(
			// Vm.debug("n: "+Vm.countObjects(true));
			(new MessageBox("Error", "Not enough memory available to load all cache data (incl. description and logs)\nexport aborted\nFilter caches to minimise memory needed for TPL-Export\nWe recommend to restart CacheWolf now", FormBase.OKB)).execute();
			// Vm.debug("n: "+Vm.countObjects(true));
		}
		pbf.exit(0);
	}

    private static String removeCharsfromString( String text, int MaxLength, String chars ) {
        if ( text == null ) return null;
        int originalTextLength = text.length();
        int anzToRemove=originalTextLength-MaxLength;
        if (anzToRemove<=0) return text;
        int anzRemoved=0;
        StringBuffer sb = new StringBuffer( 50 );
        for ( int i = originalTextLength-1; i >= 0; i-- ) {
            char c = text.charAt( i );
            if (chars.indexOf(c) == -1) {
            	sb.insert(0,c);
            }
            else {
            	anzRemoved++;
            	if (anzRemoved==anzToRemove) {
            		sb.insert(0, text.substring(0,i));
            		i=0; // exit for
            	}
            }
        }
        return sb.toString();
    }
}

//##################################################################
class NoCodec implements TextCodec{
//##################################################################

/**
* This is a creation option. It specifies that CR characters should be removed when
* encoding text into UTF.
**/
public static final int STRIP_CR_ON_DECODE = 0x1;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from UTF.
**/
public static final int STRIP_CR_ON_ENCODE = 0x2;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from UTF AND encoding text to UTF.
**/
public static final int STRIP_CR = STRIP_CR_ON_DECODE|STRIP_CR_ON_ENCODE;

private int flags = 0;

//===================================================================
public NoCodec(int options)
//===================================================================
{
	flags = options;
}
//===================================================================
public NoCodec()
//===================================================================
{
	this(0);
}
//===================================================================
public ByteArray encodeText(char [] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	int size = length == 0 ? 2 : 2+text.length*2;
	if (dest.data == null || dest.data.length < size)
		dest.data = new byte[size];
	byte [] destination = dest.data;
	int s = 0;
	if (length>0){
		destination[s++] = (byte) 0xFF;
		destination[s++] = (byte) 0xFE;
	}
	for (int i = 0; i<length; i++){
		char c = text[i+start];
		if (c == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0)) continue;
		destination[s++] = (byte)(c & 0xFF);
		destination[s++] = (byte)((c>>8) & 0xFF);
	}
	dest.length = s;
	return dest;
}

//===================================================================
public CharArray decodeText(byte [] encoded, int start, int length, boolean endOfData, CharArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new CharArray();
	dest.length = 0;
	return dest;
}

//===================================================================
public void closeCodec() throws IOException
//===================================================================
{
}

//===================================================================
public Object getCopy()
//===================================================================
{
	return new NoCodec(flags);
}
//##################################################################
}
//##################################################################
