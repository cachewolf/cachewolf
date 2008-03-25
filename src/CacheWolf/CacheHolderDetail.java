package CacheWolf;

import ewe.filechooser.FileChooser;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Vm;
import ewe.ui.InputBox;
import ewe.util.Vector;

public class CacheHolderDetail extends CacheHolder {
	  public String LongDescription = EMPTY;
	  public String LastUpdate = EMPTY;
	  public String Hints = EMPTY;
	  public LogList CacheLogs=new LogList();
	  public String CacheNotes = EMPTY;
	  public Vector Images = new Vector();
	  public Vector ImagesText = new Vector();
	  public Vector ImagesInfo = new Vector();
	  public Vector LogImages = new Vector();
	  public Vector LogImagesText = new Vector();
	  public Vector UserImages = new Vector();
	  public Vector UserImagesText = new Vector();
	  public Attributes attributes=new Attributes();
	  public Vector CacheIcons = new Vector();
	  public TravelbugList Travelbugs=new TravelbugList();
	  //public String Bugs = EMPTY; Superceded by Travelbugs
	  public String URL = EMPTY;
	  public String Solver = EMPTY;
	  /** For faster cache import (from opencaching) changes are only written when the details are freed from memory 
	   * If you want to save the changes automatically when the details are unloaded, set this to true */ 
	  public boolean hasUnsavedChanges = false;
	  
	 public CacheHolderDetail() {
	 }
	 //public CacheHolderDetail(String wpt) {super(wpt); }
	 public CacheHolderDetail(CacheHolder ch) {
		 super(ch);
	 }

	 public void setLongDescription(String longDescription) {
	 	if (LongDescription.equals("")) is_new=true;
	 	else if (!stripControlChars(LongDescription).equals(stripControlChars(longDescription))) is_update=true;
	 	LongDescription = longDescription;
	 }
	 
	 private String stripControlChars(String desc) {
		 StringBuffer sb=new StringBuffer(desc.length());
		 for (int i=0; i<desc.length(); i++) {
			char c=desc.charAt(i);
			if (c>' ') sb.append(c);
		 }
		 return sb.toString();
	 }
	 
	 public void setHints(String hints) {
	 	if (!Hints.equals(hints)) is_update=true;
	 	Hints = hints;
	 }
	 
	 public void setCacheLogs(LogList newLogs) {
		 int size=newLogs.size();
		 for (int i=size-1; i>=0; i--) { // Loop over all new logs, must start with oldest log
			 if (CacheLogs.merge(newLogs.getLog(i))>=0) this.is_log_update=true;
		 }
		 //CacheLogs=logs;
		 noFindLogs=CacheLogs.countNotFoundLogs();
	 }

	 
	  /**
	 * Method to update an existing cache with new data. This is
	 * necessary to avoid missing old logs. Called from GPX Importer
	 * @param newCh new cache data
	 * @return CacheHolder with updated data
	 */
	public CacheHolderDetail update(CacheHolderDetail newCh){
		  super.update(newCh);
		  // flags
		  if (this.is_found == true && this.CacheStatus.equals("")) this.CacheStatus = MyLocale.getMsg(318,"Found");

		  //travelbugs:GPX-File contains all actual travelbugs but not the missions
		  //  we need to check whether the travelbug is already in the existing list
		  this.has_bug = newCh.Travelbugs.size()>0;
		  for (int i=newCh.Travelbugs.size()-1; i>=0; i--) {
			 Travelbug tb=newCh.Travelbugs.getTB(i);  
		     Travelbug oldTB=this.Travelbugs.find(tb.getName());
		     // If the bug is already in the cache, we keep it
		     if (oldTB!=null)
		    	 newCh.Travelbugs.replace(i,oldTB);
		    
		  }
		  this.Travelbugs = newCh.Travelbugs;
		  
		  // URL
		  this.URL = newCh.URL;
		  
		  setLongDescription(newCh.LongDescription);
		  setHints(newCh.Hints);
		  setCacheLogs(newCh.CacheLogs);
		  
		  if (newCh.Solver.length()>0) this.Solver=newCh.Solver;
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
			// Attributes
			ex = new Extractor(text,"<ATTRIBUTES>","</ATTRIBUTES>",0,true);
			attributes.XmlAttributesEnd(ex.findNext());
			
			ex = new Extractor(text, "<LOGS>","</LOGS>", 0, true);
			dummy = ex.findNext();
			CacheLogs.clear();
			ex = new Extractor(dummy, "<LOG>","</LOG>", 0, true);
			
			dummy = ex.findNext();
			while(ex.endOfSearch()==false){
				CacheLogs.add(new Log(dummy));
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
				int pos=dummy.indexOf("<DESC>");
				if (pos>0) {
					ImagesText.add(dummy.substring(0,pos));
					ImagesInfo.add(dummy.substring(pos+6,dummy.indexOf("</DESC>")));
				} else {
					ImagesText.add(dummy);
					ImagesInfo.add(null);
				}
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

			ex = new Extractor(text, "<TRAVELBUGS>", "</TRAVELBUGS>", 0, false);
			dummy=ex.findNext();
			if (ex.endOfSearch()) {
				ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
				String Bugs = ex.findNext();
				Travelbugs.addFromHTML(Bugs);
			} else
				Travelbugs.addFromXML(dummy);
			
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
			ex = new Extractor(text, "<SOLVER><![CDATA[", "]]></SOLVER>", 0, true);
			Solver=ex.findNext();
		}
		
		/**
		*	Method to save a cache.xml file.
		*/
		public void saveCacheDetails(String dir){
			PrintWriter detfile;
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
				Global.getPref().log("Problem opening details file",e,true);
				return;
			}
			try{
				if(wayPoint.length()>0){
				  detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
				  detfile.print("<CACHEDETAILS>\r\n");
				  detfile.print("<DETAILS><![CDATA["+LongDescription+"]]></DETAILS>\r\n");
				  detfile.print(attributes.XmlAttributesWrite());
				  detfile.print("<HINTS><![CDATA["+Hints+"]]></HINTS>\r\n");
				  detfile.print("<LOGS>\r\n");
				  for(int i = 0; i < CacheLogs.size(); i++){
					  detfile.print(CacheLogs.getLog(i).toXML());
				  }
				  detfile.print("</LOGS>\r\n");
			
				  detfile.print("<NOTES><![CDATA["+CacheNotes+"]]></NOTES>\n");
				  detfile.print("<IMAGES>\n");
				  String stbuf = new String();
				  for(int i = 0;i<Images.size();i++){
						stbuf = (String)Images.get(i);
						detfile.print("    <IMG>"+stbuf+"</IMG>\n");
				  }
				  int iis = ImagesInfo.size();
				  for(int i = 0;i<ImagesText.size();i++){
						stbuf = (String)ImagesText.get(i);
						if (iis < i && ImagesInfo.get(i) != null)
							detfile.print("    <IMGTEXT>"+stbuf+"<DESC>"+ImagesInfo.get(i)+"</DESC></IMGTEXT>\n");
						else 
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
				  //detfile.print("<BUGS><![CDATA[\n");
				  //detfile.print(Bugs+"\n");
				  //detfile.print("]]></BUGS>\n");
				  detfile.print(Travelbugs.toXML());
				  detfile.print("<URL><![CDATA["+URL+"]]></URL>\r\n");
				  detfile.print("<SOLVER><![CDATA["+Solver+"]]></SOLVER>\r\n");
				  detfile.print(toXML()); // This will allow restoration of index.xml
				  detfile.print("</CACHEDETAILS>\n");
				} // if length
			} catch (Exception e){
				Global.getPref().log("Problem waypoint " + wayPoint + " writing to a details file: " + e.getMessage());
			}
			try{
			  detfile.close();
			} catch (Exception e){
				Global.getPref().log("Problem waypoint " + wayPoint + " writing to a details file: " + e.getMessage());
			}
			hasUnsavedChanges = false;
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
		
		/**
		 * Return true if this cache has additional info for some pictures
		 * @return true if cache has additional info, false otherwise
		 */
		public boolean hasImageInfo() {
			for (int i=ImagesInfo.size()-1; i>=0; i--)
				if (ImagesInfo.get(i)!=null) return true;
			return false;
		}

//	   public void finalize() {
//		   super.finalize();
//		   Vm.debug("Destroying CacheHolder "+wayPoint);
//	   }

}
