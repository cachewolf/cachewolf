package CacheWolf.exp;

import com.stevesoft.ewe_pat.Regex;

import CacheWolf.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.ui.*;
import ewe.filechooser.*;
import HTML.*;
import HTML.Tmpl.Element.Element;

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
	 		"filename",  "templates"+FileBase.separator+"index.tpl",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 		//,"debug", "true"
	 	};
	String [] template_init_page = {
	 		"filename",  "templates"+FileBase.separator+"page.tpl",
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
		CacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int exportErrors = 0;

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
					varParams=ch.toHashtable(dec, null, 0, 30, new AsciiCodec(), null);
					cache_index.add(varParams);
					//We can generate the individual page here!
					try{
						Template page_tpl = new Template(template_init_page);
						
						Enumeration de = varParams.keys();
						while(de.hasMoreElements()) {
							String s = de.nextElement().toString();
							if (!s.equals("DESCRIPTION")) {
								page_tpl.setParam(s,varParams.get(s).toString());
							}
						}
						
						if (det != null) {
							if (ch.is_HTML()) {
								page_tpl.setParam("DESCRIPTION", modifyLongDesc(det,targetDir));
							} else {
								page_tpl.setParam("DESCRIPTION",STRreplace.replace(det.LongDescription, "\n", "<br>"));
							}

							StringBuffer sb=new StringBuffer(2000);
							for(int j = 0; j<det.CacheLogs.size(); j++){
								sb.append(STRreplace.replace(det.CacheLogs.getLog(j).toHtml(),"http://www.geocaching.com/images/icons/",null));
								sb.append("<br>");
								icon=det.CacheLogs.getLog(j).getIcon();
								if (logIcons.find(icon)<0) logIcons.add(icon); // Add the icon to list of icons to copy to dest directory
							}

							page_tpl.setParam("LOGS", sb.toString());
							page_tpl.setParam("NOTES", STRreplace.replace(det.getCacheNotes(), "\n","<br>"));

							cacheImg.clear();
							for(int j = 0; j<det.images.size(); j++){
								imgParams = new Hashtable();
								String imgFile = new String(det.images.get(j).getFilename());
								imgParams.put("FILE", imgFile);
								imgParams.put("TEXT",det.images.get(j).getTitle());
								if (DataMover.copy(profile.dataDir + imgFile,targetDir + imgFile))
									cacheImg.add(imgParams);
								else
									exportErrors++;
							}
							page_tpl.setParam("cacheImg", cacheImg);

							// Log images
							logImg.clear();
							for(int j = 0; j<det.logImages.size(); j++){
								logImgParams = new Hashtable();
								String logImgFile = det.logImages.get(j).getFilename();
								logImgParams.put("FILE", logImgFile);
								logImgParams.put("TEXT",det.logImages.get(j).getTitle());
								if (DataMover.copy(profile.dataDir + logImgFile,targetDir + logImgFile))
									logImg.add(logImgParams);
								else
									exportErrors++;
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
								else
									exportErrors++;
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
								else
									exportErrors++;

								mapImgParams = new Hashtable();
								mapImgFile = ch.getWayPoint() + "_map_2.gif";
								mapImgParams.put("FILE", mapImgFile);
								mapImgParams.put("TEXT",mapImgFile);
								if (DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile))
									mapImg.add(mapImgParams);
								else
									exportErrors++;

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
							exportErrors++;
						}

						PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + ch.getWayPoint()+".html")));
						pagefile.print(page_tpl.output());
						pagefile.close();
					} catch (IllegalArgumentException e) {
						exportErrors++;
						ch.setIncomplete(true);
						Global.getPref().log("HTMLExport: "+ch.getWayPoint()+" is incomplete reason: ",e,Global.getPref().debug);
					} catch(Exception e){
						exportErrors++;
						Global.getPref().log("HTMLExport: error wehen exporting "+ch.getWayPoint()+" reason: ",e,Global.getPref().debug);
					}
				}//if is black, filtered
			}

			// Copy the log-icons to the destination directory
			for (int j=0; j<logIcons.size(); j++) {
				icon=(String) logIcons.elementAt(j);
				if (!DataMover.copy(FileBase.getProgramDirectory() + "/"+icon,targetDir + icon))
					exportErrors++;

			}
			if (!DataMover.copy(FileBase.getProgramDirectory() + "/recommendedlog.gif",targetDir + "recommendedlog.gif"))
				exportErrors++;

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

		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors+" errors during export. See log for details.", FormBase.OKB).execute();
		}

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
					if (imageNo < chD.images.size()) {
						localImageSource = chD.images.get(imageNo).getFilename();
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
			if (imageNo >= chD.images.size()) break;
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
