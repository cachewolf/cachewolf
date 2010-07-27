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

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.Preferences;
import CacheWolf.Profile;
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
	int noOfLogs=-1; // means all
	boolean single = true;
	int formatModifier = 0;
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
			if (param.equals("NrLogs")) {
				noOfLogs = Integer.valueOf(value).intValue();
			}
			if (param.equals("singleFile")) {
				single = value.equals("yes") ? true : false ;
			}
			if (param.equals("formatModifier")) {
				formatModifier = Integer.valueOf(value).intValue();
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

		ProgressBarForm pbf = new ProgressBarForm();
		ewe.sys.Handle h = new ewe.sys.Handle();
		int counter = cacheDB.countVisible();
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		try {
			TplFilter myFilter = new TplFilter();			
			Hashtable args = new Hashtable();
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
			if(fc.execute() == FormBase.IDCANCEL) {pbf.exit(0); return; }
			File saveTo = fc.getChosenFile();
			pref.setExportPath(expName, saveTo.getPath());

			Regex dec = new Regex("[,.]",myFilter.decSep);
			if (myFilter.badChars != null) rex = new Regex("["+myFilter.badChars+"]","");

			Vector cache_index = new Vector(); 			
			for(int i = 0; i<counter;i++){
				CacheHolder ch = cacheDB.get(i);
				h.progress = (float)i/(float)counter;
				h.changed();
				if(ch.isVisible() && ch.pos.isValid()){
					try {
						Hashtable varParams=ch.toHashtable(dec, rex, myFilter.shortWaypointLength, myFilter.shortNameLength, myFilter.noOfLogs, myFilter.codec, gm, false, myFilter.formatModifier);
						if (myFilter.single) {
							cache_index.add(varParams);
						}
						else {
							cache_index.add(varParams);
							tpl.setParam("cache_index", cache_index);
							String ext = (myFilter.out.substring(myFilter.out.lastIndexOf(".")).toLowerCase()+"    ").trim();
							PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(saveTo.getPath() + ch.getWayPoint() + ext)));
							pagefile.print(tpl.output());
							pagefile.close();
							cache_index.clear();
						}
					}catch(Exception e){
						Vm.debug("Problem getting Parameter, Cache: " + ch.getWayPoint());
						e.printStackTrace();
						Global.getPref().log("Exception in TplExporter = Problem getting Parameter, Cache: " + ch.getWayPoint(), e, true);
					}
				}
			}
			if (myFilter.single) {
				tpl.setParam("cache_index", cache_index);
				PrintWriter detfile;
				FileWriter fw = new FileWriter(saveTo);
				fw.codec = myFilter.codec;
				detfile = new PrintWriter(new BufferedWriter(fw));
				tpl.printTo(detfile);
				//detfile.print(tpl.output());
				detfile.close();
			}
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
