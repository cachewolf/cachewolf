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

package cachewolf.exp;

import HTML.Template;
import eve.ui.filechooser.FileChooser;
import java.io.*;
import eve.io.*;
import eve.sys.*;
import eve.ui.*;
import java.util.*;


import cachewolf.CWPoint;
import cachewolf.CacheHolder;
import cachewolf.CacheHolderDetail;
import cachewolf.CacheType;
import cachewolf.Global;
import cachewolf.Preferences;
import cachewolf.Profile;
import cachewolf.utils.Common;

import com.stevesoft.eve_pat.*;

/**
 * @author Kalle
 * class to export cachedata using a template
 */
class TplFilter implements HTML.Tmpl.Filter
{
	private int type=SCALAR;
	private String newLine="\n";
	TextCodec codec;
	String badChars;
	String decSep = ".";


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
				if (value.equals("ASCII")) codec = new AsciiCodec();
				if (value.equals("UTF8")) codec = new JavaUtf8Codec();
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
			if (param.equals("debug"))
				HTML.Tmpl.Util.debug=true;
		}
		return t;
	}


	public String [] parse(String [] t) {
		throw new UnsupportedOperationException();
	}
}


public class TPLExporter {
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	String tplFile;
	String expName;
	Regex rex=null;

	public TPLExporter(Preferences p, Profile prof, String tpl){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
		tplFile = tpl;
		java.io.File tmpFile = new java.io.File(tpl);
		expName = tmpFile.getName();
		expName = expName.substring(0, expName.indexOf("."));
	}

	public void doIt(){
		CacheHolderDetail chD;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		eve.sys.Handle h = new eve.sys.Handle();

		FileChooser fc = new FileChooser(FileChooser.SAVE, pref.getExportPath(expName));
		fc.title=("Select target file:");
		if(fc.execute() == FileChooser.IDCANCEL) return;
		String saveTo = fc.getChosen();
		pref.setExportPath(expName, saveTo);
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_black == false && ch.is_filtered == false) counter++;
		}
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();
		java.lang.Runtime.getRuntime().gc(); // all this doesn't really work :-(
		System.runFinalization();
		java.lang.Runtime.getRuntime().gc();
		//Vm.debug("v: "+Vm.countObjects(true));
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
			args.put("max_includes", Integer.toString(5));
			args.put("filter", myFilter);
			Template tpl = new Template(args);

			for(int i = 0; i<counter;i++){
				ch = (CacheHolder)cacheDB.get(i);
				h.progress = (float)i/(float)counter;
				h.changed();
				if(ch.is_black == false && ch.is_filtered == false){
					if (ch.pos.isValid() == false) continue;
					chD=new CacheHolderDetail(ch);
					try{
						chD.readCache(profile.dataDir);
					}catch(Exception e){
						Vm.debug("Problem reading cache page");
						Global.getPref().log("Exception in TplExporter = Problem reading cache page, Cache: " + chD.wayPoint, e, true);
					}
					try {
						Regex dec = new Regex("[,.]",myFilter.decSep);
						if (myFilter.badChars != null) rex = new Regex("["+myFilter.badChars+"]","");
						varParams = new Hashtable();
						varParams.put("TYPE", CacheType.transType(chD.type));
						varParams.put("SHORTTYPE", CacheType.transType(chD.type).substring(0,1));
						varParams.put("SIZE", chD.getCacheSize());
						varParams.put("SHORTSIZE", chD.getCacheSize().substring(0,1));
						varParams.put("WAYPOINT", chD.wayPoint);
						varParams.put("OWNER", chD.cacheOwner);
						varParams.put("DIFFICULTY", dec.replaceAll(chD.hard));
						varParams.put("TERRAIN", dec.replaceAll(chD.terrain));
						varParams.put("DISTANCE", dec.replaceAll(chD.distance));
						varParams.put("BEARING", chD.bearing);
						varParams.put("LATLON", chD.latLon);
						varParams.put("LAT", dec.replaceAll(chD.pos.getLatDeg(CWPoint.DD)));
						varParams.put("LON", dec.replaceAll(chD.pos.getLonDeg(CWPoint.DD)));
						varParams.put("STATUS", chD.cacheStatus);
						varParams.put("STATUS_DATE", chD.getStatusDate());
						varParams.put("STATUS_TIME", chD.getStatusTime());
						varParams.put("DATE", chD.dateHidden);
						varParams.put("URL", chD.URL);
						varParams.put("NOTES", chD.cacheNotes);
						varParams.put("DESCRIPTION", chD.longDescription);
                        if (myFilter.badChars != null) {
                            varParams.put("NAME", rex.replaceAll(chD.cacheName));
                            varParams.put("NOTES", rex.replaceAll(chD.cacheNotes));
                            varParams.put("HINTS", rex.replaceAll(chD.hints));
                            varParams.put("DECRYPTEDHINTS", rex.replaceAll(Common.rot13(chD.hints)));
                    } else {
                            varParams.put("NAME", chD.cacheName);
                            varParams.put("NOTES", chD.cacheNotes);
                            varParams.put("HINTS", chD.hints);
                            varParams.put("DECRYPTEDHINTS", Common.rot13(chD.hints));
                    }
						cache_index.add(varParams);
					}catch(Exception e){
						Vm.debug("Problem getting Parameter, Cache: " + chD.wayPoint);
						e.printStackTrace();
						Global.getPref().log("Exception in TplExporter = Problem getting Parameter, Cache: " + chD.wayPoint, e, true);
					}
				}
			}

			tpl.setParam("cache_index", cache_index);
			PrintWriter detfile;
			detfile=new PrintWriter(new BufferedWriter(new TextWriter(new FileOutputStream(saveTo),false,myFilter.codec)));
			tpl.printTo(detfile);
			detfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			Global.getPref().log("Exception in TplExporter", e, true);
		} catch (OutOfMemoryError e) {
			//Global.getPref().log("OutOfMemeory in TplExporter", e, true);
			java.lang.Runtime.getRuntime().gc(); // this doesn't help :-(
			System.runFinalization();
			java.lang.Runtime.getRuntime().gc(); // this doesn't help :-( - I don't know why :-(
			//Vm.debug("n: "+Vm.countObjects(true));
			(new MessageBox("Error", "Not enough memory available to load all cache data (incl. description and logs)\nexport aborted\nFilter caches to minimise memory needed for TPL-Export\nWe recommend to restart CacheWolf now", MessageBox.OKB)).execute();
			//Vm.debug("n: "+Vm.countObjects(true));
		}
		pbf.exit(0);
	}


}
