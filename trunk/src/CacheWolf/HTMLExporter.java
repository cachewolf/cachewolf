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
	Vector cacheDB;
	Preferences myPreferences;
	String [] template_init_index = {
	 		"filename",  "index.tpl",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 	};
	String [] template_init_page = {
	 		"filename",  "page.tpl",
	 		"case_sensitive", "true",
	 		"max_includes",   "5"
	 	};
	public HTMLExporter(Vector db, Preferences pref){
		cacheDB = db;
		myPreferences = pref;
	}
	
	public void doIt(){
		ProgressBarForm pbf = new ProgressBarForm();
		CacheHolder holder = new CacheHolder();
		CacheReaderWriter crw = new CacheReaderWriter();
		//need directory only!!!!
		String dummy = new String();
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPreferences.mydatadir);
		fc.setTitle("Select target directory:");
		String targetDir;
		if(fc.execute() != fc.IDCANCEL){
			targetDir = fc.getChosen() + "/";
			Vector cache_index = new Vector();
			Vector cacheImg = new Vector();
			Vector logImg = new Vector();
			Vector mapImg = new Vector();
			

			Hashtable varParams;
			Hashtable imgParams;
			Hashtable logImgParams;
			Hashtable mapImgParams;

			//Generate index page
			int counter = 0;
			for(int i = 0; i<cacheDB.size();i++){
				holder = (CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false) counter++;
			}
			for(int i = 0; i<counter;i++){
				if (i%5 == 0){
					pbf.display("Exporting...", "Exporting " + Convert.toString(i) + " of " + counter, null);
				}
				holder = (CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false){
					//KHF read cachedata only if needed
					try{crw.readCache(holder, myPreferences.mydatadir);
					}catch(Exception e){
						//Vm.debug("Problem reading cache page");
					}
					varParams = new Hashtable();
					varParams.put("TYPE", Common.transType(holder.type));
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
						page_tpl.setParam("TYPE", Common.transType(holder.type));
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
							DataMover.copy(myPreferences.mydatadir + imgFile,targetDir + imgFile);
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
							DataMover.copy(myPreferences.mydatadir + logImgFile,targetDir + logImgFile);
							logImg.add(logImgParams);
						}
						page_tpl.setParam("logImg", logImg);
						// Map images
						mapImg.clear();
						mapImgParams = new Hashtable();
						String mapImgFile = new String((String)holder.wayPoint + "_map.gif");
						// check if map file exists
						File test = new File(myPreferences.mydatadir + mapImgFile);
						if (test.exists()) {
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(myPreferences.mydatadir + mapImgFile,targetDir + mapImgFile);
							mapImg.add(mapImgParams);
							
							mapImgParams = new Hashtable();
							mapImgFile = (String)holder.wayPoint + "_map_2.gif";
							mapImgParams.put("FILE", mapImgFile);
							mapImgParams.put("TEXT",mapImgFile);
							DataMover.copy(myPreferences.mydatadir + mapImgFile,targetDir + mapImgFile);
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
				PrintWriter detfile = new PrintWriter(new BufferedWriter(new FileWriter(targetDir + "/index.html")));
				detfile.print(tpl.output());
				detfile.close();
			}catch(Exception e){
				Vm.debug("Problem writing HTML files");
			}//try
			
		}//if
		pbf.clear();
	}
	
}
