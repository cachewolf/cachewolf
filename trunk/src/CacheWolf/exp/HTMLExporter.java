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

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.Common;
import CacheWolf.DataMover;
import CacheWolf.Global;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import HTML.Template;

import com.stevesoft.ewe_pat.Regex;

import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.AsciiCodec;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.Comparer;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
*	Class to export cache information to individual HTML files.<br>
*	It uses the HTML package to parse template files. This makes the export
*	very flexible; enabling the user to customise the HTML files according
*	to thier liking.
*/
public class HTMLExporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String [] template_init_index = {
	 		"filename",  FileBase.getProgramDirectory()+FileBase.separator+FileBase.separator+"indextpl.html",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 		//,"debug", "true"
	 	};
	String [] template_init_page = {
	 		"filename",  FileBase.getProgramDirectory()+FileBase.separator+FileBase.separator+"pagetpl.html",
	 		"case_sensitive", "true",
	 		"loop_context_vars", "true",
	 		"max_includes",   "5"
	 	};
	public final static String expName = "HTML";

	public HTMLExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}

	public void doIt(){
		CacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int exportErrors = 0;

		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.getExportPath(expName));
		fc.setTitle("Select target directory:");
		String targetDir;
		if(fc.execute() != FormBase.IDCANCEL){
			targetDir = fc.getChosen() + "/";
			pref.setExportPath(expName, targetDir);
			Vector cache_index = new Vector();
			Vector cacheImg = new Vector();
			Vector logImg = new Vector();
			Vector mapImg = new Vector();
			Vector usrImg = new Vector();
			Vector logIcons = new Vector(15);
			String icon;

			Hashtable varParams;
			Hashtable logImgParams;
			Hashtable usrImgParams;
			Hashtable mapImgParams;

			//Generate index page
			int counter = cacheDB.countVisible();

			pbf.showMainTask = false;
			pbf.setTask(h,"Exporting ...");
			pbf.exec();

			String decSep = "."; // myFilter.decSep
			Regex dec = new Regex("[,.]",decSep);

			for(int i = 0; i<counter;i++){
				h.progress = (float)(i+1)/(float)counter;
				h.changed();

				ch = cacheDB.get(i);
				if(	ch.isVisible()){
					if (ch.is_incomplete()) {
						exportErrors++;
						Global.getPref().log("HTMLExport: skipping export of incomplete waypoint "+ch.getWayPoint());
						continue;
					}
					det=ch.getCacheDetails(false);
					varParams=ch.toHashtable(dec, null, 0, 30, -1, new AsciiCodec(), null, false, 2, expName);
					cache_index.add(varParams);
					//We can generate the individual page here!
					try{
						Template page_tpl = new Template(template_init_page);
						page_tpl.setParams(varParams);

						if (det != null) {

							// Add the icon to list of icons to copy to dest directory
							for(int j = 0; j<det.CacheLogs.size()-1; j++){
								icon=det.CacheLogs.getLog(j).getIcon();
								if (logIcons.find(icon)<0) {
									logIcons.add(icon); 
								}
							}

							// Log images
							logImg.clear();
							for(int j = 0; j<det.logImages.size(); j++){
								logImgParams = new Hashtable();
								String logImgFile = det.logImages.get(j).getFilename();
								logImgParams.put("FILE", logImgFile);
								logImgParams.put("TEXT",det.logImages.get(j).getTitle());
								if (DataMover.copy(profile.dataDir + logImgFile,targetDir + logImgFile))
									logImg.add(logImgParams);
								else {
									pref.log("[HTMLExporter:DataMover]"+logImgFile+" "+ch.getWayPoint());
									exportErrors++;
								}
							}
							page_tpl.setParam("logImg", logImg);

							// User images
							usrImg.clear();
							for(int j = 0; j<det.userImages.size(); j++){
								usrImgParams = new Hashtable();
								String usrImgFile = new String(det.userImages.get(j).getFilename());
								usrImgParams.put("FILE", usrImgFile);
								usrImgParams.put("TEXT",det.userImages.get(j).getTitle());
								if (DataMover.copy(profile.dataDir + usrImgFile,targetDir + usrImgFile))
									usrImg.add(usrImgParams);
								else {
									pref.log("[HTMLExporter:DataMover]"+usrImgFile+" "+ch.getWayPoint());
									exportErrors++;
								}
							}
							page_tpl.setParam("userImg", usrImg);

							// Map images
							mapImg.clear();
							mapImgParams = new Hashtable();

							String mapImgFile = new String(ch.getWayPoint() + "_map.gif");
							// check if map file exists
							File test = new File(profile.dataDir + mapImgFile);

							if (test.exists()) {
								mapImgParams.put("FILE", mapImgFile);
								mapImgParams.put("TEXT",mapImgFile);
								if (DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile))
									mapImg.add(mapImgParams);
								else {
									pref.log("[HTMLExporter:DataMover]"+mapImgFile+" "+ch.getWayPoint());
									exportErrors++;
								}
								mapImgParams = new Hashtable();
								mapImgFile = ch.getWayPoint() + "_map_2.gif";
								mapImgParams.put("FILE", mapImgFile);
								mapImgParams.put("TEXT",mapImgFile);
								if (DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile))
									mapImg.add(mapImgParams);
								else {
									pref.log("[HTMLExporter:DataMover]"+mapImgFile+" "+ch.getWayPoint());
									exportErrors++;
								}
								page_tpl.setParam("mapImg", mapImg);
							}
						} else {
							page_tpl.setParam("DESCRIPTION", "");
							page_tpl.setParam("LOGS", "");
							page_tpl.setParam("NOTES", "");
							page_tpl.setParam("cacheImg", cacheImg);
							page_tpl.setParam("logImg", ""); // ???
							page_tpl.setParam("userImg", ""); // ???
							page_tpl.setParam("mapImg", ""); // ???
							pref.log("[HTMLExporter:DoIt]Error "+ch.getWayPoint());
							exportErrors++;
						}

						PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + ch.getWayPoint()+".html")));
						pagefile.print(page_tpl.output());
						pagefile.close();
					} catch (IllegalArgumentException e) {
						pref.log("[HTMLExporter:DoIt]"+ch.getWayPoint()+" is incomplete reason: ",e,true);
						exportErrors++;
						ch.setIncomplete(true);
					} catch(Exception e){
						exportErrors++;
						pref.log("[HTMLExporter:DoIt]"+ch.getWayPoint(),e,true);
					}
				}//if is black, filtered
			}

			// Copy the log-icons to the destination directory
			for (int j=0; j<logIcons.size(); j++) {
				icon=(String) logIcons.elementAt(j);
				if (!DataMover.copy(FileBase.getProgramDirectory() + "/"+icon,targetDir + icon)) {
					pref.log("[HTMLExporter:DataMover]"+icon,null);
					exportErrors++;
				}

			}
			if (!DataMover.copy(FileBase.getProgramDirectory() + "/recommendedlog.gif",targetDir + "recommendedlog.gif")) {
				pref.log("[HTMLExporter:DataMover]recommendedlog.gif",null);
				exportErrors++;	
			}

			try{
				Template tpl = new Template(template_init_index);
				tpl.setParam("cache_index", cache_index);
				PrintWriter detfile;
				detfile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + "/index.html")));
				detfile.print(tpl.output());
				detfile.close();
				// sort by waypoint
				sortAndPrintIndex(tpl, cache_index,targetDir + "/index_wp.html", "WAYPOINT");
				// sort by name
				sortAndPrintIndex(tpl, cache_index,targetDir + "/index_alpha.html", "NAME", false);
				// sort by type
				sortAndPrintIndex(tpl, cache_index,targetDir + "/index_type.html", "TYPE", true);
				// sort by size
				sortAndPrintIndex(tpl, cache_index,targetDir + "/index_size.html", "SIZE", true);
				// sort by distance
				sortAndPrintIndex(tpl, cache_index,targetDir + "/index_dist.html", "DISTANCE", 10.0);
			}catch(Exception e){
				pref.log("[HTMLExporter:writeIndexFiles]Problem writing HTML files",e,true);
			}//try

		}//if
		pbf.exit(0);

		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors+" errors during export. See log for details.", FormBase.OKB).execute();
		}

	}

	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field){
		PrintWriter detfile;

		list.sort(new HTMLComparer(field),false);
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			pref.log("[HTMLExporter:sortAndPrintIndex]Problem writing HTML file:"+file,e,true);
		}
	}


	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field, boolean fullCompare){
		Vector navi_index;
		PrintWriter detfile;

		list.sort(new HTMLComparer(field),false);
		navi_index = addAnchorString(list, field, fullCompare);
		if (navi_index != null){
			tmpl.setParam("navi_index",navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			pref.log("[HTMLExporter:writeIndexFile]Problem writing HTML file:"+file,e,true);
		}
	}

	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field, double diff){
		Vector navi_index;
		PrintWriter detfile;

		list.sort(new HTMLComparer(field),false);
		navi_index = addAnchorString(list,field, diff);
		if (navi_index != null){
			tmpl.setParam("navi_index",navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			pref.log("[HTMLExporter:writeIndexFile]Problem writing HTML file:"+file,e,true);
		}

	}


	private Vector addAnchorString(Vector list, String field, boolean fullCompare){
		Vector topIndex = new Vector();
		Hashtable topIndexParms, currEntry;
		String lastValue, currValue;

		if (list.size() == 0) return null;

		currEntry = (Hashtable) list.get(0);
		lastValue = (String) currEntry.get(field);
		if (lastValue == null || lastValue.length() == 0) lastValue = "  ";
		lastValue = lastValue.toUpperCase();

		for (int i=1; i<list.size(); i++){
			currEntry = (Hashtable) list.get(i);
			currValue = (String) currEntry.get(field);
			currValue = currValue.toUpperCase();
			if (currValue == null || currValue == "") continue;
			try {
				if (fullCompare) {
					if (lastValue.compareTo(currValue)!= 0){
						// Values for navigation line
						topIndexParms = new Hashtable();
						topIndexParms.put("HREF", Convert.toString(i));
						topIndexParms.put("TEXT", currValue);
						topIndex.add(topIndexParms);
						// add anchor entry to list
						currEntry.put("ANCHORNAME", Convert.toString(i));
						currEntry.put("ANCHORTEXT", currValue);
					}
					else {
						// clear value from previous run
						currEntry.put("ANCHORNAME", "");
						currEntry.put("ANCHORTEXT", "");
					}
				}
				else {
					if (lastValue.charAt(0)!= currValue.charAt(0)){
						// Values for navigation line
						topIndexParms = new Hashtable();
						topIndexParms.put("HREF", Convert.toString(i));
						topIndexParms.put("TEXT", currValue.charAt(0)+ " ");
						topIndex.add(topIndexParms);
						// add anchor entry to list
						currEntry.put("ANCHORNAME", Convert.toString(i));
						currEntry.put("ANCHORTEXT", currValue.charAt(0)+ " ");
					}
					else {
						// clear value from previous run
						currEntry.put("ANCHORNAME", "");
						currEntry.put("ANCHORTEXT", "");
					}
				}
				list.set(i,currEntry);
				lastValue = currValue;
			} catch (Exception e){
				continue;
			}
		}
		return topIndex;
	}
	private Vector addAnchorString(Vector list, String field, double diff){
		Vector topIndex = new Vector();
		Hashtable topIndexParms, currEntry;
		double lastValue, currValue;

		if (list.size() == 0) return null;

		currEntry = (Hashtable) list.get(0);
		lastValue = Common.parseDouble((String) currEntry.get(field)) + diff;

		for (int i=1; i<list.size(); i++){
			currEntry = (Hashtable) list.get(i);
			currValue = Common.parseDouble((String) currEntry.get(field));
			if (currValue >= lastValue ){
				// Values for navigation line
				topIndexParms = new Hashtable();
				topIndexParms.put("HREF", Convert.toString(i));
				topIndexParms.put("TEXT", Convert.toString(lastValue));
				topIndex.add(topIndexParms);
				// add anchor entry to list
				currEntry.put("ANCHORNAME", Convert.toString(i));
				currEntry.put("ANCHORTEXT", Convert.toString(lastValue));
				lastValue = currValue + diff;
			}
			else {
				// clear value from previous run
				currEntry.put("ANCHORNAME", "");
				currEntry.put("ANCHORTEXT", "");
			}
			list.set(i,currEntry);
		}
		return topIndex;
	}

	/**
	 * @author Kalle
	 * Comparer for sorting the vector for the index.html file
	 */
	private class HTMLComparer implements Comparer {
		String compareWhat;

		public HTMLComparer (String what){
			this.compareWhat = what;
		}

		public int compare(Object o1, Object o2){
			Hashtable hash1 = (Hashtable)o1;
			Hashtable hash2 = (Hashtable)o2;
			String str1, str2;
			double dbl1, dbl2;

			str1 = hash1.get(compareWhat).toString().toLowerCase();
			str2 = hash2.get(compareWhat).toString().toLowerCase();

			if (this.compareWhat.equals("WAYPOINT")){
				str1 = hash1.get(compareWhat).toString().substring(2).toLowerCase();
				str2 = hash2.get(compareWhat).toString().substring(2).toLowerCase();
			}

			if (this.compareWhat.equals("DISTANCE")){
				dbl1 = Common.parseDouble(str1.substring(0,str1.length()-3));
				dbl2 = Common.parseDouble(str2.substring(0,str2.length()-3));
				if (dbl1 > dbl2) return 1;
				if (dbl1 < dbl2) return -1;
				else return 0;
			}
			else {
				return str1.compareTo(str2);
			}
		}
	}


}
