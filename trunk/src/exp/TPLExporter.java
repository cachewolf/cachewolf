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

package exp;

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheSize;
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
	
	public TPLExporter(Preferences p, Profile prof, String tpl){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
		tplFile = tpl;
		File tmpFile = new File(tpl);
		expName = tmpFile.getName();
		expName = expName.substring(0, expName.indexOf("."));
	}
	
	public void doIt(){
		CacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		ewe.sys.Handle h = new ewe.sys.Handle();

		FileChooser fc = new FileChooser(FileChooserBase.SAVE, pref.getExportPath(expName));
		fc.setTitle("Select target file:");
		if(fc.execute() == FormBase.IDCANCEL) return;
		File saveTo = fc.getChosenFile();
		pref.setExportPath(expName, saveTo.getPath());
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			ch = cacheDB.get(i);
			if(cacheDB.get(i).isVisible() == true) counter++;
		}
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();
		Vm.gc(); // all this doesn't really work :-(
		System.runFinalization();
		Vm.gc();
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
			args.put("max_includes", new Integer(5));
			args.put("filter", myFilter);
			Template tpl = new Template(args);

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
						varParams.put("TYPE", CacheType.cw2ExportString(ch.getType()));
						varParams.put("SHORTTYPE", CacheType.getExportShortId(ch.getType()));
						varParams.put("SIZE", CacheSize.cw2ExportString(ch.getCacheSize()));
						varParams.put("SHORTSIZE", CacheSize.getExportShortId(ch.getCacheSize()));
						varParams.put("WAYPOINT", ch.getWayPoint());
						varParams.put("OWNER", ch.getCacheOwner());
						varParams.put("DIFFICULTY", String.valueOf(ch.getHard()));
						varParams.put("TERRAIN", String.valueOf(ch.getTerrain()));
						varParams.put("DISTANCE", dec.replaceAll(ch.getDistance()));
						varParams.put("BEARING", ch.bearing);
						varParams.put("LATLON", ch.LatLon);
						varParams.put("LAT", dec.replaceAll(ch.pos.getLatDeg(CWPoint.DD)));
						varParams.put("LON", dec.replaceAll(ch.pos.getLonDeg(CWPoint.DD)));
						varParams.put("STATUS", ch.getCacheStatus());
						varParams.put("STATUS_DATE", ch.GetStatusDate());
						varParams.put("STATUS_TIME", ch.GetStatusTime());
						varParams.put("DATE", ch.getDateHidden());
						varParams.put("URL", det.URL);
						varParams.put("GC_LOGTYPE", (ch.is_found()?"Found it":"Didn't find it"));
						varParams.put("DESCRIPTION", det.LongDescription);
						if (myFilter.badChars != null) {
							varParams.put("NAME", rex.replaceAll(ch.getCacheName()));
							varParams.put("NOTES", rex.replaceAll(det.getCacheNotes()));
							varParams.put("HINTS", rex.replaceAll(det.Hints));
							varParams.put("DECRYPTEDHINTS", rex.replaceAll(Common.rot13(det.Hints)));
						} else {
							varParams.put("NAME", ch.getCacheName());
							varParams.put("NOTES", det.getCacheNotes());
							varParams.put("HINTS", det.Hints);
							varParams.put("DECRYPTEDHINTS", Common.rot13(det.Hints));
						}
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
			//Global.getPref().log("OutOfMemeory in TplExporter", e, true);
			Vm.gc(); // this doesn't help :-(
			System.runFinalization();
			Vm.gc(); // this doesn't help :-( - I don't know why :-(
			//Vm.debug("n: "+Vm.countObjects(true));
			(new MessageBox("Error", "Not enough memory available to load all cache data (incl. description and logs)\nexport aborted\nFilter caches to minimise memory needed for TPL-Export\nWe recommend to restart CacheWolf now", FormBase.OKB)).execute();
			//Vm.debug("n: "+Vm.countObjects(true));
		}
		pbf.exit(0);
	}


}
