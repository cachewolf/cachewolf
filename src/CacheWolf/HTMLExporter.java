package CacheWolf;
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
	public HTMLExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
//		ProgressBarForm pbf = new ProgressBarForm();
		CacheHolder holder = new CacheHolder();
		//need directory only!!!!
		String dummy = new String();
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, profile.dataDir);
		fc.setTitle("Select target directory:");
		String targetDir;
		if(fc.execute() != FileChooser.IDCANCEL){
			targetDir = fc.getChosen() + "/";
			Vector cache_index = new Vector();
			Vector cacheImg = new Vector();
			Vector logImg = new Vector();
			Vector mapImg = new Vector();
			Vector usrImg = new Vector();
			

			Hashtable varParams;
			Hashtable imgParams;
			Hashtable logImgParams;
			Hashtable usrImgParams;
			Hashtable mapImgParams;

			//Generate index page
			int counter = 0;
			for(int i = 0; i<cacheDB.size();i++){
				holder = (CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false) counter++;
			}
			for(int i = 0; i<counter;i++){
				if (i%5 == 0){
					ProgressBarForm.display("Exporting...", "Exporting " + Convert.toString(i) + " of " + counter, null);
				}
				holder = (CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false){
					//KHF read cachedata only if needed
					try{
						holder.readCache( profile.dataDir);
					}catch(Exception e){
						//Vm.debug("Problem reading cache page");
					}
					varParams = new Hashtable();
					varParams.put("TYPE", CacheType.transType(holder.type));
					varParams.put("SIZE", holder.CacheSize);
					varParams.put("WAYPOINT", holder.wayPoint);
					varParams.put("NAME", holder.CacheName);
					varParams.put("OWNER", holder.CacheOwner);
					varParams.put("DIFFICULTY", holder.hard);
					varParams.put("TERRAIN", holder.terrain);
					varParams.put("DISTANCE", holder.distance);
					varParams.put("BEARING", holder.bearing);
					varParams.put("LATLON", holder.LatLon);
					varParams.put("STATUS", holder.CacheStatus);
					cache_index.add(varParams);
					//We can generate the individual page here!
					try{
						Template page_tpl = new Template(template_init_page);
						page_tpl.setParam("TYPE", CacheType.transType(holder.type));
						page_tpl.setParam("SIZE", holder.CacheSize);
						page_tpl.setParam("WAYPOINT", holder.wayPoint);
						page_tpl.setParam("NAME", holder.CacheName);
						page_tpl.setParam("OWNER", holder.CacheOwner);
						page_tpl.setParam("DIFFICULTY", holder.hard);
						page_tpl.setParam("TERRAIN", holder.terrain);
						page_tpl.setParam("DISTANCE", holder.distance);
						page_tpl.setParam("BEARING", holder.bearing);
						page_tpl.setParam("LATLON", holder.LatLon);
						page_tpl.setParam("STATUS", holder.CacheStatus);
						if (holder.is_HTML)
							page_tpl.setParam("DESCRIPTION", holder.LongDescription);
						else {
							String dummyText = new String();
							dummyText = STRreplace.replace(holder.LongDescription, "\n", "<br>");
							page_tpl.setParam("DESCRIPTION",dummyText);
							
						}
						page_tpl.setParam("HINTS", holder.Hints);
						page_tpl.setParam("DECRYPTEDHINTS", Common.rot13(holder.Hints));
						dummy = new String();
						for(int j = 0; j<holder.CacheLogs.size(); j++){
							dummy = dummy + (String)holder.CacheLogs.get(j)+"<br>";
						}
						page_tpl.setParam("LOGS", dummy);
						page_tpl.setParam("NOTES", STRreplace.replace(holder.CacheNotes, "\n","<br>"));
						// Cache Images
						cacheImg.clear();
						for(int j = 0; j<holder.Images.size(); j++){
							imgParams = new Hashtable();
							String imgFile = new String((String)holder.Images.get(j));
							imgParams.put("FILE", imgFile);
							if (j < holder.ImagesText.size())
								imgParams.put("TEXT",(String)holder.ImagesText.get(j));
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
							String logImgFile = new String((String)holder.LogImages.get(j));
							logImgParams.put("FILE", logImgFile);
							if (j < holder.LogImagesText.size())
								logImgParams.put("TEXT",(String)holder.LogImagesText.get(j));
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
								usrImgParams.put("TEXT",(String)holder.UserImagesText.get(j));
							else
								usrImgParams.put("TEXT",usrImgFile);
							DataMover.copy(profile.dataDir + usrImgFile,targetDir + usrImgFile);
							usrImg.add(usrImgParams);
						}
						page_tpl.setParam("userImg", usrImg);

						// Map images
						mapImg.clear();
						mapImgParams = new Hashtable();
						String mapImgFile = new String((String)holder.wayPoint + "_map.gif");
						// check if map file exists
						File test = new File(profile.dataDir + mapImgFile);
						if (test.exists()) {
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
							
							mapImgParams = new Hashtable();
							mapImgFile = (String)holder.wayPoint + "_map_2.gif";
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(profile.dataDir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
	
							page_tpl.setParam("mapImg", mapImg);
						}

						
						PrintWriter pagefile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + holder.wayPoint+".html")));
						pagefile.print(page_tpl.output());
						pagefile.close();
					}catch(Exception e){
						Vm.debug("Problem writing waypoint html file");
					}
				}//if is black, filtered
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
				Vm.debug("Problem writing HTML files\n");
				e.printStackTrace();
			}//try
			
		}//if
		ProgressBarForm.clear();
	}
	private void sortAndPrintIndex(Template tmpl, Vector list, String file, String field){
		Vector navi_index;
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

	
}
