package exp;
import com.stevesoft.ewe_pat.Regex;

import CacheWolf.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.ui.*;
import ewe.filechooser.*;
import HTML.*;

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
	public final static String expName = "HTML";

	public HTMLExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
		CacheHolderDetail holder;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		new String();
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
			Hashtable imgParams;
			Hashtable logImgParams;
			Hashtable usrImgParams;
			Hashtable mapImgParams;

			//Generate index page
			int counter = 0;
			for(int i = 0; i<cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				if(ch.is_black() == false && ch.is_filtered() == false) counter++;
			}
			
			pbf.showMainTask = false;
			pbf.setTask(h,"Exporting ...");
			pbf.exec();

			for(int i = 0; i<counter;i++){
				h.progress = (float)(i+1)/(float)counter;
				h.changed();

				ch = (CacheHolder)cacheDB.get(i);
				if(	ch.is_black() == false && ch.is_filtered() == false){
					holder=ch.getCacheDetails(false,true);
					varParams = new Hashtable();
					varParams.put("TYPE", CacheType.transType(holder.getType()));
					varParams.put("SIZE", holder.getCacheSize());
					varParams.put("WAYPOINT", holder.getWayPoint());
					varParams.put("NAME", holder.getCacheName());
					varParams.put("OWNER", holder.getCacheOwner());
					varParams.put("DIFFICULTY", holder.getHard());
					varParams.put("TERRAIN", holder.getTerrain());
					varParams.put("DISTANCE", holder.getDistance());
					varParams.put("BEARING", holder.bearing);
					varParams.put("LATLON", holder.LatLon);
					varParams.put("STATUS", holder.getCacheStatus());
					varParams.put("DATE", holder.getDateHidden());
					cache_index.add(varParams);
					//We can generate the individual page here!
					try{
						Template page_tpl = new Template(template_init_page);
						page_tpl.setParam("TYPE", CacheType.transType(holder.getType()));
						page_tpl.setParam("SIZE", holder.getCacheSize());
						page_tpl.setParam("WAYPOINT", holder.getWayPoint());
						page_tpl.setParam("NAME", holder.getCacheName());
						page_tpl.setParam("OWNER", holder.getCacheOwner());
						page_tpl.setParam("DIFFICULTY", holder.getHard());
						page_tpl.setParam("TERRAIN", holder.getTerrain());
						page_tpl.setParam("DISTANCE", holder.getDistance());
						page_tpl.setParam("BEARING", holder.bearing);
						page_tpl.setParam("LATLON", holder.LatLon);
						page_tpl.setParam("STATUS", holder.getCacheStatus());
						page_tpl.setParam("DATE", holder.getDateHidden());
						if (holder.is_HTML())
							page_tpl.setParam("DESCRIPTION", modifyLongDesc(holder,targetDir));
						else {
							String dummyText = new String();
							dummyText = STRreplace.replace(holder.LongDescription, "\n", "<br>");
							page_tpl.setParam("DESCRIPTION",dummyText);
							
						}
						page_tpl.setParam("HINTS", holder.Hints);
						page_tpl.setParam("DECRYPTEDHINTS", Common.rot13(holder.Hints));
						StringBuffer sb=new StringBuffer(2000);
						for(int j = 0; j<holder.CacheLogs.size(); j++){
							sb.append(STRreplace.replace(holder.CacheLogs.getLog(j).toHtml(),"http://www.geocaching.com/images/icons/",null));
							sb.append("<br>");
							icon=holder.CacheLogs.getLog(j).getIcon();
							if (logIcons.find(icon)<0) logIcons.add(icon); // Add the icon to list of icons to copy to dest directory
						}
						page_tpl.setParam("LOGS", sb.toString());
						page_tpl.setParam("NOTES", STRreplace.replace(holder.CacheNotes, "\n","<br>")); 
						// Cache Images
						cacheImg.clear();
						for(int j = 0; j<holder.Images.size(); j++){
							imgParams = new Hashtable();
							String imgFile = new String((String)holder.Images.get(j));
							imgParams.put("FILE", imgFile);
							if (j < holder.ImagesText.size())
								imgParams.put("TEXT",holder.ImagesText.get(j));
							else
								imgParams.put("TEXT",imgFile);
							DataMover.copy(profile.dataDir + imgFile,targetDir + imgFile);
							cacheImg.add(imgParams);
						}
						page_tpl.setParam("cacheImg", cacheImg);
						// Log images
						logImg.clear();
						for(int j = 0; j<holder.LogImages.size(); j++){
							logImgParams = new Hashtable();
							String logImgFile = (String) holder.LogImages.get(j);
							logImgParams.put("FILE", logImgFile);
							if (j < holder.LogImagesText.size())
								logImgParams.put("TEXT",holder.LogImagesText.get(j));
							else
								logImgParams.put("TEXT",logImgFile);
							DataMover.copy(profile.dataDir + logImgFile,targetDir + logImgFile);
							logImg.add(logImgParams);
						}
						page_tpl.setParam("logImg", logImg);
						// User images
						usrImg.clear();
						for(int j = 0; j<holder.UserImages.size(); j++){
							usrImgParams = new Hashtable();
							String usrImgFile = new String((String)holder.UserImages.get(j));
							usrImgParams.put("FILE", usrImgFile);
							if (j < holder.UserImagesText.size())
								usrImgParams.put("TEXT",holder.UserImagesText.get(j));
							else
								usrImgParams.put("TEXT",usrImgFile);
							DataMover.copy(profile.dataDir + usrImgFile,targetDir + usrImgFile);
							usrImg.add(usrImgParams);
						}
						page_tpl.setParam("userImg", usrImg);

						// Map images
						mapImg.clear();
						mapImgParams = new Hashtable();
						String mapImgFile = new String(holder.getWayPoint() + "_map.gif");
						// check if map file exists
						File test = new File(profile.dataDir + mapImgFile);
						if (test.exists()) {
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
							
							mapImgParams = new Hashtable();
							mapImgFile = holder.getWayPoint() + "_map_2.gif";
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
	
							page_tpl.setParam("mapImg", mapImg);
						}

						
						PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + holder.getWayPoint()+".html")));
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
				DataMover.copy(FileBase.getProgramDirectory() + "/"+icon,targetDir + icon);
				
			}
			DataMover.copy(FileBase.getProgramDirectory() + "/recommendedlog.gif",targetDir + "recommendedlog.gif");
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
		StringBuffer s=new StringBuffer(chD.LongDescription.length());
		int start=0;
		int pos;
		int imageNo=0;
		Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
		while (start>=0 && (pos=chD.LongDescription.indexOf("<img",start))>0) {
			s.append(chD.LongDescription.substring(start,pos));
			imgRex.searchFrom(chD.LongDescription,pos);
			String imgUrl=imgRex.stringMatched(1);
			//Vm.debug("imgUrl "+imgUrl);
			if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
				String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
				// If we have an image which we stored when spidering, we can display it
				if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe") && !imgType.startsWith(".pl")){
					// It may occur that there are less local images than
					// image links in the description (eg. because of importing
					// GPX files). We have to allow for this situation.
					Object localImageSource = null;
					if (imageNo < chD.Images.size()) {
						localImageSource = chD.Images.get(imageNo);
					}
					if (localImageSource == null) localImageSource = imgUrl;
					s.append("<img src=\""+localImageSource+"\">");
					// The actual immages are copied elswhere
					//DataMover.copy(profile.dataDir + chD.Images.get(imageNo),targetDir + chD.Images.get(imageNo));
					imageNo++;
				}
			}
			start=chD.LongDescription.indexOf(">",pos);
			if (start>=0) start++;
			if (imageNo >= chD.Images.getCount())break;
		}
		if (start>=0) s.append(chD.LongDescription.substring(start));
		return s.toString();
	}
	
	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field){
		PrintWriter detfile; 
		
		list.sort(new HTMLComparer(field),false);
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
		
		list.sort(new HTMLComparer(field),false);
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
