package cachewolf.exp;
import cachewolf.*;
import cachewolf.utils.Common;
import cachewolf.utils.STRreplace;

import com.stevesoft.eve_pat.Regex;


import java.util.*;
import eve.sys.*;
import eve.ui.*;
import eve.ui.filechooser.*;
import HTML.*;
import java.io.*;


/**
*	Class to export cache information to individual HTML files.<br>
*	It uses the HTML package to parse template files. This makes the export
*	very flexible; enabling the user to customise the HTML files according
*	to thier liking.
*/
public class HTMLExporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	String [] template_init_index = {
	 		"filename",  "index.tpl",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 		//,"debug", "true"
	 	};
	String [] template_init_page = {
	 		"filename",  "page.tpl",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 	};
	//public final static String expName = "HTML";

	public HTMLExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
		CacheHolderDetail chD;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		//need directory only!!!!
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, Global.getProfile().htmlExportDirectory);
		fc.title=("Select target directory:");
		String targetDir;
		if(fc.execute() != FileChooser.IDCANCEL){
			targetDir = fc.getChosen() + "/";
			Global.getProfile().htmlExportDirectory=targetDir;
			//pref.setExportPath(expName, targetDir);
			Vector cache_index = new Vector();
			Vector cacheImg = new Vector();
			Vector logImg = new Vector();
			Vector mapImg = new Vector();
			Vector usrImg = new Vector();
			Vector logIcons = new Vector(15);
			String icon;

			Hashtable varParams;
			Hashtable imgParams;
			Hashtable logImgParams;
			Hashtable usrImgParams;
			Hashtable mapImgParams;

			//Generate index page
			int counter = 0;
			for(int i = 0; i<cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_black == false && ch.is_filtered == false) counter++;
			}
			
			pbf.showMainTask = false;
			pbf.setTask(h,"Exporting ...");
			pbf.exec();

			for(int i = 0; i<counter;i++){
				h.progress = (float)(i+1)/(float)counter;
				h.changed();

				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_black == false && ch.is_filtered == false){
					chD=ch.getCacheDetails(false,true);
					varParams = new Hashtable();
					varParams.put("TYPE", CacheType.transType(chD.type));
					varParams.put("SIZE", chD.getCacheSize());
					varParams.put("WAYPOINT", chD.wayPoint);
					varParams.put("NAME", chD.cacheName);
					varParams.put("OWNER", chD.cacheOwner);
					varParams.put("DIFFICULTY", chD.hard);
					varParams.put("TERRAIN", chD.terrain);
					varParams.put("DISTANCE", chD.distance);
					varParams.put("BEARING", chD.bearing);
					varParams.put("LATLON", chD.latLon);
					varParams.put("STATUS", chD.cacheStatus);
					varParams.put("DATE", chD.dateHidden);
					cache_index.add(varParams);
					//We can generate the individual page here!
					try{
						Template page_tpl = new Template(template_init_page);
						page_tpl.setParam("TYPE", CacheType.transType(chD.type));
						page_tpl.setParam("SIZE", chD.getCacheSize());
						page_tpl.setParam("WAYPOINT", chD.wayPoint);
						page_tpl.setParam("NAME", chD.cacheName);
						page_tpl.setParam("OWNER", chD.cacheOwner);
						page_tpl.setParam("DIFFICULTY", chD.hard);
						page_tpl.setParam("TERRAIN", chD.terrain);
						page_tpl.setParam("DISTANCE", chD.distance);
						page_tpl.setParam("BEARING", chD.bearing);
						page_tpl.setParam("LATLON", chD.latLon);
						page_tpl.setParam("STATUS", chD.cacheStatus);
						page_tpl.setParam("DATE", chD.dateHidden);
						if (chD.is_HTML)
							page_tpl.setParam("DESCRIPTION", modifyLongDesc(chD,targetDir));
						else {
							String dummyText;
							dummyText = STRreplace.replace(chD.longDescription, "\n", "<br>");
							page_tpl.setParam("DESCRIPTION",dummyText);
							
						}
						page_tpl.setParam("HINTS", chD.hints);
						page_tpl.setParam("DECRYPTEDHINTS", Common.rot13(chD.hints));
						StringBuffer sb=new StringBuffer(2000);
						for(int j = 0; j<chD.cacheLogs.size(); j++){
							sb.append(STRreplace.replace(chD.cacheLogs.getLog(j).toHtml(),"http://www.geocaching.com/images/icons/",null));
							sb.append("<br>");
							icon=chD.cacheLogs.getLog(j).getIcon();
							if (logIcons.indexOf(icon)<0) logIcons.add(icon); // Add the icon to list of icons to copy to dest directory
						}
						page_tpl.setParam("LOGS", sb.toString());
						page_tpl.setParam("NOTES", STRreplace.replace(chD.cacheNotes, "\n","<br>")); 
						// Cache Images
						cacheImg.clear();
						for(int j = 0; j<chD.images.size(); j++){
							imgParams = new Hashtable();
							String imgFile = (String)chD.images.get(j);
							imgParams.put("FILE", imgFile);
							if (j < chD.imagesText.size())
								imgParams.put("TEXT",chD.imagesText.get(j));
							else
								imgParams.put("TEXT",imgFile);
							DataMover.copy(profile.dataDir + imgFile,targetDir + imgFile);
							cacheImg.add(imgParams);
						}
						page_tpl.setParam("cacheImg", cacheImg);
						// Log images
						logImg.clear();
						for(int j = 0; j<chD.logImages.size(); j++){
							logImgParams = new Hashtable();
							String logImgFile = (String) chD.logImages.get(j);
							logImgParams.put("FILE", logImgFile);
							if (j < chD.logImagesText.size())
								logImgParams.put("TEXT",chD.logImagesText.get(j));
							else
								logImgParams.put("TEXT",logImgFile);
							DataMover.copy(profile.dataDir + logImgFile,targetDir + logImgFile);
							logImg.add(logImgParams);
						}
						page_tpl.setParam("logImg", logImg);
						// User images
						usrImg.clear();
						for(int j = 0; j<chD.userImages.size(); j++){
							usrImgParams = new Hashtable();
							String usrImgFile = (String)chD.userImages.get(j);
							usrImgParams.put("FILE", usrImgFile);
							if (j < chD.userImagesText.size())
								usrImgParams.put("TEXT",chD.userImagesText.get(j));
							else
								usrImgParams.put("TEXT",usrImgFile);
							DataMover.copy(profile.dataDir + usrImgFile,targetDir + usrImgFile);
							usrImg.add(usrImgParams);
						}
						page_tpl.setParam("userImg", usrImg);

						// Map images
						mapImg.clear();
						mapImgParams = new Hashtable();
						String mapImgFile = chD.wayPoint + "_map.gif";
						// check if map file exists
						java.io.File test = new java.io.File(profile.dataDir + mapImgFile);
						if (test.exists()) {
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
							
							mapImgParams = new Hashtable();
							mapImgFile = chD.wayPoint + "_map_2.gif";
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
	
							page_tpl.setParam("mapImg", mapImg);
						}

						
						PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + chD.wayPoint+".html")));
						pagefile.print(page_tpl.output());
						pagefile.close();
					}catch(Exception e){
						Vm.debug("Problem writing waypoint html file");
					}
				}//if is black, filtered
			}
			// Copy the log-icons to the destination directory
			for (int j=0; j<logIcons.size(); j++) {
				icon=(String) logIcons.elementAt(j);
				// Copy icons only if they are not the "virtual" MAXLOGICON
				if (!icon.equals(Log.MAXLOGICON)) {
				    DataMover.copy(eve.io.File.getProgramDirectory() + "/"+icon,targetDir + icon);
				}
			}
			DataMover.copy(eve.io.File.getProgramDirectory() + "/recommendedlog.gif",targetDir + "recommendedlog.gif");
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
				Vm.debug("Problem writing HTML files\n");
				e.printStackTrace();
			}//try
			
		}//if
		pbf.exit(0);
	}
	
	/**
	 * Modify the image links in the long description so that they point to image files in the local directory
	 * Also copy the image file to the target directory so that it can be displayed.
	 * @param chD CacheHolderDetail
	 * @return The modified long description
	 */
	private String modifyLongDesc(CacheHolderDetail chD, String targetDir) {
		StringBuffer s=new StringBuffer(chD.longDescription.length());
		int start=0;
		int pos;
		int imageNo=0;
		Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
		while (start>=0 && (pos=chD.longDescription.indexOf("<img",start))>0) {
			s.append(chD.longDescription.substring(start,pos));
			imgRex.searchFrom(chD.longDescription,pos);
			String imgUrl=imgRex.stringMatched(1);
			//Vm.debug("imgUrl "+imgUrl);
			if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
				String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
				// If we have an image which we stored when spidering, we can display it
				if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe") && !imgType.startsWith(".pl")){
					s.append("<img src=\""+chD.images.get(imageNo)+"\">");
					// The actual immages are copied elswhere
					//DataMover.copy(profile.dataDir + chD.Images.get(imageNo),targetDir + chD.Images.get(imageNo));
					imageNo++;
				}
			}
			start=chD.longDescription.indexOf(">",pos);
			if (start>=0) start++;
			if (imageNo >= chD.images.size())break;
		}
		if (start>=0) s.append(chD.longDescription.substring(start));
		return s.toString();
	}
	
	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field){
		PrintWriter detfile; 
		
		eve.util.Utils.sort(new Handle(),list, new HTMLComparer(field), false);
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
		}
	}

	
	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field, boolean fullCompare){
		Vector navi_index;
		PrintWriter detfile; 
		
		eve.util.Utils.sort(new Handle(), list,new HTMLComparer(field),false);
		navi_index = addAnchorString(list,field, fullCompare);
		if (navi_index != null){
			tmpl.setParam("navi_index",navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
		}
	}
	
	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field, double diff){
		Vector navi_index;
		PrintWriter detfile; 
		
		eve.util.Utils.sort(new Handle(), list,new HTMLComparer(field),false);
		navi_index = addAnchorString(list,field, diff);
		if (navi_index != null){
			tmpl.setParam("navi_index",navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
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

			try {
				if (fullCompare) {
					if (currValue == null || currValue.equals("")) continue;
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
				list.setElementAt(currEntry,i);
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
			list.setElementAt(currEntry,i);
		}
		return topIndex;
	}

	/**
	 * @author Kalle
	 * Comparer for sorting the vector for the index.html file
	 */
	private class HTMLComparer implements eve.util.Comparer {
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
				return 0;
			}
			return str1.compareTo(str2);
		}
	}

	
}
