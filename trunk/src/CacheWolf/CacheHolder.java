package CacheWolf;
import ewe.ui.InputBox;
import ewe.util.*;
import ewe.filechooser.FileChooser;
import ewe.io.File;

/**
*	A class to hold information on a cache.<br>
*	Not all attributes are filled at once. You will have to look at other
*	classes and methods to get more information.
*	@see CacheReaderWriter
*/
public class CacheHolder {
  public String UUID = new String();
  public String wayPoint = new String();
  public String LongDescription = new String();
  public String CacheName = new String();
  public String CacheOwner = new String();
  public String LatLon = new String();
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
  
  public void addUserImage(Preferences pref){
	  File imgFile;
	  String imgDesc, imgDestName;
	  
	  //Get Image and description
		FileChooser fc = new FileChooser(FileChooser.OPEN, pref.mydatadir);
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
			DataMover.copy(imgFile.getFullPath(),pref.mydatadir + imgDestName);
			// Save Data
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveCacheDetails(this, pref.mydatadir);
		}
  }
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
  
}