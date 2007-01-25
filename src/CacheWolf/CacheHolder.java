package CacheWolf;
import ewe.sys.Vm;
import ewe.ui.InputBox;
import ewe.util.*;
import ewe.filechooser.FileChooser;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;

/**
*	A class to hold information on a cache.<br>
*	Not all attributes are filled at once. You will have to look at other
*	classes and methods to get more information.
*	
*/
public class CacheHolder {
  public String UUID = new String();
  public String wayPoint = new String();
  public String LongDescription = new String();
  public String CacheName = new String();
  public String CacheOwner = new String();
  public String LatLon = new String();
  public CWPoint pos = null;
  public String DateHidden = new String();
  public String LastUpdate = new String();
  public String Hints = new String();
  public Vector CacheLogs = new Vector(0);
  public String CacheNotes = new String();
  public String CacheStatus = new String("");
  public String CacheSize = new String("None");
  public String distance = new String();
  public String bearing = new String();
  public double degrees = 0;
  public double kilom = 0;
  public String hard = new String();
  public String terrain =new String();
  public String type = new String("0");
  public String dirty = new String();
  public Vector Images = new Vector();
  public Vector ImagesText = new Vector();
  public Vector LogImages = new Vector();
  public Vector LogImagesText = new Vector();
  public Vector UserImages = new Vector();
  public Vector UserImagesText = new Vector();
  public Vector attributes = new Vector();
  public Vector CacheIcons = new Vector();
  public String Bugs = new String();
  public String URL = new String();
  public String ocCacheID = new String();
  public int noFindLogs = 0;
  public boolean is_archived = false;
  public boolean is_available = true;
  public boolean has_bug = false;
  public boolean is_black = false;
  public boolean is_filtered = false;
  public boolean is_flaged = false;
  public boolean is_owned = false;
  public boolean is_found = false;
  public boolean is_new = false;
  public boolean is_log_update = false;
  public boolean is_update = false;
  public boolean is_selected = false;
  public boolean is_HTML = true;
  public boolean is_Checked = false;
  public Vector addiWpts = new Vector();
  public CacheHolder mainCache;
    
  
  /**
 * Method to update an existing cache with new data. This is
 * necessary to avoid missing old logs.
 * @param newCh new cache data
 * @return CacheHolder with updated data
 */
public CacheHolder update(CacheHolder newCh){
	  // flags
	  this.is_available = newCh.is_available;
	  this.is_archived = newCh.is_archived;
	  // update is_owned only if not the owner ????
	  if (this.is_owned == false) this.is_owned = newCh.is_owned;
	  // update is_found if not already found
	  if (this.is_found == false) this.is_found = newCh.is_found;
	  
	  this.is_new = false;
	  this.is_update = false;
	  this.is_log_update = false;
	  
	  //name and owner
	  this.CacheName = newCh.CacheName;
	  this.CacheOwner = newCh.CacheOwner;

	  //classification
	  this.hard = newCh.hard;
	  this.terrain = newCh.terrain;
	  this.type = newCh.type;
	  
	  //travelbugs: overriding is OK, since GPX-File contains all actual travelbugs
	  this.has_bug = newCh.has_bug;
	  this.Bugs = newCh.Bugs;
	  
	  // URL
	  this.URL = newCh.URL;
	  
	  //coords
	  this.LatLon = newCh.LatLon;

	  // check only length of the description to see, if there was an update
	  if (this.LongDescription.length() != newCh.LongDescription.length()){
		  this.is_update = true;
	  }
	  // same for hints
	  if (this.Hints.length() != newCh.Hints.length()){
		  this.is_update = true;
	  }
	  
	  // description & hints
	  this.is_HTML = newCh.is_HTML;
	  this.LongDescription = newCh.LongDescription;
	  this.Hints = newCh.Hints;

	  //Logs
	  //<img src='icon_smile.gif'>&nbsp;2005-10-30 by Schatzpirat</strong><br>
	  //get Date of latest log in old cachedata
	  Extractor extOldDate;
	  String oldLogDate = new String();
	  if(this.CacheLogs.size()>0){
		extOldDate = new Extractor((String) this.CacheLogs.get(0), ";"," by", 0, true);
		oldLogDate= new String(extOldDate.findNext());
	  }
	  // now loop through new cachedata and compare logentries, 
	  // starting with oldest log
	  
	  //Vm.debug("made it to here!");
	  int currLog = newCh.CacheLogs.size()-1;
	  String newLogDate = new String();
	  while (currLog >= 0 ){
		  Extractor extNewDate = new Extractor((String) newCh.CacheLogs.get(currLog), ";"," by", 0, true);
		  newLogDate = extNewDate.findNext();
		  if (newLogDate.compareTo(oldLogDate)> 0){
	  		  // oldest log from new cachedata is younger than stored data
	  		  // put the new logs in front of old logs
			  //Vm.debug(newCh.wayPoint + " New: " + newLogDate + " Old: " + oldLogDate + " cmp: " + newLogDate.compareTo(oldLogDate));
			  while (currLog >= 0) this.CacheLogs.add(0, newCh.CacheLogs.get(currLog--));
			  this.is_log_update = true;
		  }
		  else currLog--;
	  }//while
   	 //Check for number sukzessive DNF logs
	 int z = 0;
	 String loganal = new String();
	 // Vm.debug("Checking size: ");
	 //int sz = newCh.CacheLogs.size();
	 //Vm.debug("log size: " + sz);
 	 while(z < newCh.CacheLogs.size() && z < 5){
		loganal = (String)newCh.CacheLogs.get(z);
		if(loganal.indexOf("icon_sad")>0) {
			z++;
		}else break;
	 }
	 noFindLogs = z;
 	return this;
  }
  
  /**
   * Adds a user image to the cache data
   * @param profile
   */
	public void addUserImage(Profile profile){
	  File imgFile;
	  String imgDesc, imgDestName;
	  
	  //Get Image and description
		FileChooser fc = new FileChooser(FileChooser.OPEN, profile.dataDir);
		fc.setTitle("Select image file:");
		if(fc.execute() != FileChooser.IDCANCEL){
			imgFile = fc.getChosenFile();
			imgDesc = new InputBox("Description").input("",10);
			//Create Destination Filename
			String ext = imgFile.getFileExt().substring(imgFile.getFileExt().lastIndexOf("."));
			imgDestName = this.wayPoint + "_U_" + (this.UserImages.size()+1) + ext;
			
			this.UserImages.add(imgDestName);
			this.UserImagesText.add(imgDesc);
			// Copy File
			DataMover.copy(imgFile.getFullPath(),profile.dataDir + imgDestName);
			// Save Data
			saveCacheDetails(profile.dataDir);
		}
  }
  /**
   * Adds a new log to the cachedata 
   * @param logEntry
   */
  public void addLog(String logEntry){
	  //Logs
	  //<img src='icon_smile.gif'>&nbsp;2005-10-30 by Schatzpirat</strong><br>
	  //get Date of latest log in old cachedata
	  Extractor extOldDate;
	  String oldLogDate = new String();
	  if(this.CacheLogs.size()>0){
		extOldDate = new Extractor((String) this.CacheLogs.get(0), ";"," by", 0, true);
		oldLogDate= new String(extOldDate.findNext());
	  }

	  String newLogDate;
	  Extractor extNewDate = new Extractor(logEntry, ";"," by", 0, true);
	  newLogDate = extNewDate.findNext();
	  if (newLogDate.compareTo(oldLogDate)> 0){
  		  // oldest log from new cachedata is younger than stored data
  		  // put the new logs in front of old logs
		  //Vm.debug(newCh.wayPoint + " New: " + newLogDate + " Old: " + oldLogDate + " cmp: " + newLogDate.compareTo(oldLogDate));
		  this.CacheLogs.add(0, logEntry);
		  this.is_log_update = true;
		  if (logEntry.indexOf("icon_sad")> 0) this.noFindLogs++;
		  return;
	  }
	  if (newLogDate.compareTo(oldLogDate)== 0){
		  // logdate is equal, so check, if finder is equal
		  String newLogFinder, oldLogFinder;
		  Extractor extOldFinder = new Extractor((String) this.CacheLogs.get(0), "by ","<", 0, true);
		  oldLogFinder = extOldFinder.findNext().toLowerCase();
		  
		  Extractor extNewFinder = new Extractor(logEntry, "by ","<", 0, true);
		  newLogFinder = extNewFinder.findNext().toLowerCase();
		  
		  if (newLogFinder.compareTo(oldLogFinder)!= 0){
			  this.CacheLogs.add(0, logEntry);
			  this.is_log_update = true;
			  if (logEntry.indexOf("icon_sad")> 0) this.noFindLogs++;
		  }
	  }
  }

  
	/**
	*	Method to parse a specific cache.xml file.
	*	It fills information on cache details, hints, logs, notes and
	*	images.
	*/
	public void readCache(String dir) throws IOException{
		String dummy;
		FileReader in = new FileReader(dir+wayPoint+".xml");
		String text= in.readAll();
		in.close();
		Extractor ex = new Extractor(text, "<DETAILS><![CDATA[", "]]></DETAILS>", 0, true);		
		LongDescription = ex.findNext();
		ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
		Hints = ex.findNext();
		ex = new Extractor(text, "<LOGS>","</LOGS>", 0, true);
		dummy = ex.findNext();
		CacheLogs.clear();
		ex = new Extractor(dummy, "<LOG><![CDATA[","]]></LOG>", 0, true);
		
		dummy = ex.findNext();
		while(ex.endOfSearch()==false){
			CacheLogs.add(dummy);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
		CacheNotes = ex.findNext();
		Images.clear();
		ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			Images.add(dummy);
			dummy = ex.findNext();
		}
		ImagesText.clear();
		ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			ImagesText.add(dummy);
			dummy = ex.findNext();
		}
		// Logimages
		LogImages.clear();
		ex = new Extractor(text, "<LOGIMG>", "</LOGIMG>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			LogImages.add(dummy);
			dummy = ex.findNext();
		}
		LogImagesText.clear();
		ex = new Extractor(text, "<LOGIMGTEXT>", "</LOGIMGTEXT>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			LogImagesText.add(dummy);
			dummy = ex.findNext();
		}

		UserImages.clear();
		ex = new Extractor(text, "<USERIMG>", "</USERIMG>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			UserImages.add(dummy);
			dummy = ex.findNext();
		}
		UserImagesText.clear();
		ex = new Extractor(text, "<USERIMGTEXT>", "</USERIMGTEXT>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			UserImagesText.add(dummy);
			dummy = ex.findNext();
		}


		ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
		Bugs = ex.findNext();
		
		ex = new Extractor(text, "<URL><![CDATA[", "]]></URL>", 0, true);
		// if no URL is stored, set default URL (at this time only possible for gc.com)
		dummy = ex.findNext();
		if (dummy.length() > 10){
			URL = dummy;
		}
		else {
			if (wayPoint.startsWith("GC")) {
				URL = "http://www.geocaching.com/seek/cache_details.aspx?wp="+ wayPoint + "&Submit6=Find&log=y";
			}
		}

	}
	
	/**
	*	Method to save a cache.xml file.
	*/
	public void saveCacheDetails(String dir){
		PrintWriter detfile;
		String dummy = new String();
		//File exists?
		boolean exists = (new File(dir + wayPoint + ".xml")).exists();
		//yes: then delete
		if (exists) {
			boolean ok = (new File(dir + wayPoint + ".xml")).delete();
			if(ok) ok = true;
		}
		boolean exists2 = (new File(dir + wayPoint.toLowerCase() + ".xml")).exists();
		//yes: delete
		if (exists2) {
			boolean ok2 = (new File(dir + wayPoint.toLowerCase() + ".xml")).delete();
			if(ok2) ok2=true;
		}
		//Vm.debug("Writing to: " +dir + "for: " + wayPoint);
		try{
		  detfile = new PrintWriter(new BufferedWriter(new FileWriter(dir + wayPoint + ".xml")));
		} catch (Exception e) {
			Vm.debug("Problem opening details file");
			return;
		}
		try{
			if(wayPoint.length()>0){
			  detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
			  detfile.print("<CACHEDETAILS>\r\n");
			  detfile.print("<DETAILS><![CDATA["+LongDescription+"]]></DETAILS>\r\n");
			  detfile.print("<HINTS><![CDATA["+Hints+"]]></HINTS>\r\n");
			  detfile.print("<LOGS>\r\n");
			  for(int i = 0; i < CacheLogs.size(); i++){
				  dummy = (String)CacheLogs.get(i);
				  detfile.print("<LOG><![CDATA[\r\n");
				  detfile.print(dummy);
				  detfile.print("\r\n]]></LOG>\r\n");
			  }
			  detfile.print("</LOGS>\r\n");
		
			  detfile.print("<NOTES><![CDATA["+CacheNotes+"]]></NOTES>\n");
			  detfile.print("<IMAGES>");
			  String stbuf = new String();
			  for(int i = 0;i<Images.size();i++){
					stbuf = (String)Images.get(i);
					detfile.print("    <IMG>"+stbuf+"</IMG>\n");
			  }
			  for(int i = 0;i<ImagesText.size();i++){
					stbuf = (String)ImagesText.get(i);
					detfile.print("    <IMGTEXT>"+stbuf+"</IMGTEXT>\n");
			  }

			  for(int i = 0;i<LogImages.size();i++){
					stbuf = (String)LogImages.get(i);
					detfile.print("    <LOGIMG>"+stbuf+"</LOGIMG>\n");
			  }
			  for(int i = 0;i<LogImagesText.size();i++){
					stbuf = (String)LogImagesText.get(i);
					detfile.print("    <LOGIMGTEXT>"+stbuf+"</LOGIMGTEXT>\n");
			  }
			  for(int i = 0;i<UserImages.size();i++){
					stbuf = (String)UserImages.get(i);
					detfile.print("    <USERIMG>"+stbuf+"</USERIMG>\n");
			  }
			  for(int i = 0;i<UserImagesText.size();i++){
					stbuf = (String)UserImagesText.get(i);
					detfile.print("    <USERIMGTEXT>"+stbuf+"</USERIMGTEXT>\n");
			  }


			  detfile.print("</IMAGES>\n");
			  detfile.print("<BUGS><![CDATA[\n");
			  detfile.print(Bugs+"\n");
			  detfile.print("]]></BUGS>\n");
			  detfile.print("<URL><![CDATA["+URL+"]]></URL>\r\n");
			  detfile.print("</CACHEDETAILS>\n");
			} // if length
		} catch (Exception e){
			Vm.debug("Problem writing to a details file");
		}
		try{
		  detfile.close();
		} catch (Exception e){
		  //Vm.debug("Problem closing details file");
		}
	}
	
	/**
	 * Method for checking if to caches belongs to each other, e.g.
	 * an additional waypoint belongs to the main cache.
	 * Works currently only, if the last 4 or 5 chars of the waypoint are
	 * the same, this is the gc.com way. 
	 * @param ch cache to check
	 * @return true if there is a relation, false otherwise
	 */
	public boolean belongsTo (CacheHolder ch) {
		
		// avoid self referencing
		if (this.wayPoint.equals(ch.wayPoint)) return false;

		return this.wayPoint.endsWith(ch.wayPoint.substring(2));
	}
	
   
   public boolean isAddiWpt(){
	   return CacheType.isAddiWpt(this.type);
   }
   
   public boolean hasAddiWpt() {
	   if (this.addiWpts.getCount()>0) return true;
	   else return false;
   }
   
   public static void buildReferences(Vector cacheDB){
	   CacheHolder ch, mainCh;
	   Hashtable dbIndex = new Hashtable((int)(cacheDB.size()/0.75f + 1), 0.75f); // initialize so that von rehashing is neccessary
	   Integer index;
	   // Build index for faster search and clear all references
	   for(int i = cacheDB.size() -1; i >= 0;i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.addiWpts.clear();
			ch.mainCache = null; 
			dbIndex.put((String)ch.wayPoint, new Integer(i));
	   }
	   // Build refeneces
	   for(int i = cacheDB.size() -1; i >= 0;i--){
			ch = (CacheHolder)cacheDB.get(i);
			if (ch.isAddiWpt()) {
				//search main cache
				if (ch.wayPoint.length() == 5){
					index = (Integer) dbIndex.get("GC"+ ch.wayPoint.substring(1));
				} 
				else {
					index = (Integer) dbIndex.get("GC"+ ch.wayPoint.substring(2));
				}
				if (index != null) {
					mainCh = (CacheHolder) cacheDB.get(index.intValue());
					mainCh.addiWpts.add(ch);
					ch.mainCache = mainCh;
				}// if
			}// if
	   }// for
   }
  
}