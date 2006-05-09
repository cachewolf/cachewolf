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

package CacheWolf;

import HTML.Template;
import ewe.filechooser.FileChooser;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;
import com.stevesoft.ewe_pat.*;

/**
 * @author Kalle
 * class to export cachedata using a template
 */
class tplFilter implements HTML.Tmpl.Filter
{
	private int type=SCALAR;
	private String newLine="\n";

	public tplFilter(){
		return;
	}
	
	public int format() {
		return this.type;
	}
	
	public String parse(String t) {
		Vm.debug(t);
		Regex rex;
		// Filter newlines 
		rex = new Regex("(?m)\n$","");
		t = rex.replaceAll(t);
		// Filter comments <#-- and -->
		rex = new Regex("<#--.*-->","");
		t = rex.replaceAll(t);
		// add newlines if line is not empty now
		if (t.length()>0)t += newLine;
		return t;
	}
		
	
	public String [] parse(String [] t) {
		Vm.debug(t.toString());
		throw new UnsupportedOperationException();
	}
}
 

public class TPLExporter {
	Vector cacheDB;
	Preferences myPreferences;
	String tplFile;

	public TPLExporter(Vector db, Preferences pref, String tpl){
		cacheDB = db;
		myPreferences = pref;
		tplFile = tpl;
	}
	
	public void doIt(){
		CacheHolder holder;
		CacheReaderWriter crw = new CacheReaderWriter();
		Vector cache_index = new Vector();
		Hashtable varParams;

		FileChooser fc = new FileChooser(FileChooser.SAVE, myPreferences.mydatadir);
		fc.setTitle("Select target file:");
		if(fc.execute() == FileChooser.IDCANCEL) return;
		File saveTo = fc.getChosenFile();
		
		int counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false && holder.is_filtered == false) counter++;
		}
		
		for(int i = 0; i<counter;i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false && holder.is_filtered == false){
				try{crw.readCache(holder, myPreferences.mydatadir);
				}catch(Exception e){
					Vm.debug("Problem reading cache page");
				}
			}
			CWPoint point = new CWPoint(holder.LatLon, CWPoint.CW);
			varParams = new Hashtable();
			varParams.put("TYPE", CacheType.transType(holder.type));
			varParams.put("SHORTTYPE", CacheType.transType(holder.type).substring(0,1));
			varParams.put("SIZE", holder.CacheSize);
			varParams.put("SHORTSIZE", holder.CacheSize.substring(0,1));
			varParams.put("WAYPOINT", holder.wayPoint);
			varParams.put("NAME", holder.CacheName);
			varParams.put("OWNER", holder.CacheOwner);
			varParams.put("DIFFICULTY", holder.hard.replace(',','.'));
			varParams.put("TERRAIN", holder.terrain.replace(',','.'));
			varParams.put("DISTANCE", holder.distance);
			varParams.put("BEARING", holder.bearing);
			varParams.put("LATLON", holder.LatLon);
			varParams.put("LAT", point.getLatDeg(CWPoint.DD));
			varParams.put("LON", point.getLonDeg(CWPoint.DD));
			varParams.put("STATUS", holder.CacheStatus);
			cache_index.add(varParams);
		}

		Hashtable args = new Hashtable();
		//args.put("debug", "true");
		args.put("filename", tplFile);
		args.put("case_sensitive", "true");
		args.put("loop_context_vars", Boolean.TRUE);
		args.put("max_includes", new Integer(5));
		args.put("filter", new tplFilter());
		try {
			Template tpl = new Template(args);
			tpl.setParam("cache_index", cache_index);
			PrintWriter detfile; 
			FileWriter fw = new FileWriter(saveTo);
			//fw.codec = IO.getCodec(IO.ASCII_CODEC);
			fw.codec = new AsciiCodec(AsciiCodec.STRIP_CR);
			detfile = new PrintWriter(new BufferedWriter(fw));
			tpl.printTo(detfile);
			//detfile.print(tpl.output());
			detfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
