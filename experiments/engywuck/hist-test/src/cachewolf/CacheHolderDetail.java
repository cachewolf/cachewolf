package cachewolf;

import eve.ui.filechooser.FileChooser;
import java.io.BufferedWriter;
import eve.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import cachewolf.utils.Extractor;


import eve.ui.data.*;

public class CacheHolderDetail extends CacheHolder {
	  public String longDescription = EMPTY;
	  public String lastUpdate = EMPTY;
	  public String hints = EMPTY;
	  public LogList cacheLogs=new LogList();
	  public String cacheNotes = EMPTY;
	  public Vector images = new Vector();
	  public Vector imagesText = new Vector();
	  public Vector imagesInfo = new Vector();
	  public Vector logImages = new Vector();
	  public Vector logImagesText = new Vector();
	  public Vector userImages = new Vector();
	  public Vector userImagesText = new Vector();
	  public Attributes attributes=new Attributes();
	  public Vector cacheIcons = new Vector();
	  public TravelbugList travelbugs=new TravelbugList();
	  //public String Bugs = EMPTY; Superceded by Travelbugs
	  public String URL = EMPTY;
	  public String solver = EMPTY;
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
	 	if (this.longDescription.equals("")) is_new=true;
	 	else if (!stripControlChars(this.longDescription).equals(stripControlChars(longDescription))) is_update=true;
	 	this.longDescription = longDescription;
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
	 	if (!this.hints.equals(hints)) is_update=true;
	 	this.hints = hints;
	 }
	 
	 public void setCacheLogs(LogList newLogs) {
		 int size=newLogs.size();
		 for (int i=size-1; i>=0; i--) { // Loop over all new logs, must start with oldest log
			 if (cacheLogs.merge(newLogs.getLog(i))>=0) this.is_log_update=true;
		 }
		 //CacheLogs=logs;
		 noFindLogs=cacheLogs.countNotFoundLogs();
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
		  if (this.is_found == true && this.cacheStatus.equals("")) this.cacheStatus = MyLocale.getMsg(318,"Found");

		  //travelbugs:GPX-File contains all actual travelbugs but not the missions
		  //  we need to check whether the travelbug is already in the existing list
		  this.has_bug = newCh.travelbugs.size()>0;
		  for (int i=newCh.travelbugs.size()-1; i>=0; i--) {
			 Travelbug tb=newCh.travelbugs.getTB(i);  
		     Travelbug oldTB=this.travelbugs.find(tb.getName());
		     // If the bug is already in the cache, we keep it
		     if (oldTB!=null)
		    	 newCh.travelbugs.replace(i,oldTB);
		    
		  }
		  this.travelbugs = newCh.travelbugs;
		  
		  // URL
		  this.URL = newCh.URL;
		  
		  setLongDescription(newCh.longDescription);
		  setHints(newCh.hints);
		  setCacheLogs(newCh.cacheLogs);
		  
		  if (newCh.solver.length()>0) this.solver=newCh.solver;
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
			fc.title="Select image file:";
			if(fc.execute() != FileChooser.IDCANCEL){
				imgFile = fc.getChosenFile();
				imgDesc = new InputBox("Description").input("",10);
				//Create Destination Filename
				String ext = imgFile.getFileExt().substring(imgFile.getFileExt().lastIndexOf("."));
				imgDestName = this.wayPoint + "_U_" + (this.userImages.size()+1) + ext;
				
				this.userImages.add(imgDestName);
				this.userImagesText.add(imgDesc);
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
eve.sys.Vm.debug("===chD:readDetail: "+wayPoint);
			String dummy;
			String filename=dir+wayPoint.toLowerCase()+".xml";
			if (!new File(filename).exists()) {
				filename=dir+wayPoint+".xml";
				if (!new File(filename).exists()) new java.io.FileNotFoundException(dir+wayPoint.toLowerCase()+".xml");
			}
			char buf[]=new char[(int) (new File(filename)).getLength()];
			java.io.InputStreamReader in = new java.io.InputStreamReader(new java.io.FileInputStream(filename),"UTF8");
			int len=in.read(buf);
			in.close();
			eve.util.CharArray ca=new eve.util.CharArray(buf); ca.setLength(len);
			String text=(ca).toString();
			Extractor ex = new Extractor(text, "<DETAILS><![CDATA[", "]]></DETAILS>", 0, true);		
			longDescription = ex.findNext();
			ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
			hints = ex.findNext();
			// Attributes
			ex = new Extractor(text,"<ATTRIBUTES>","</ATTRIBUTES>",0,true);
			attributes.xmlAttributesEnd(ex.findNext());
			
			ex = new Extractor(text, "<LOGS>","</LOGS>", 0, true);
			dummy = ex.findNext();
			cacheLogs.clear();
			ex = new Extractor(dummy, "<LOG>","</LOG>", 0, true);
			
			dummy = ex.findNext();
			while(ex.endOfSearch()==false){
				cacheLogs.add(new Log(dummy));
				dummy = ex.findNext();
			}
			ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
			cacheNotes = ex.findNext();
			images.clear();
			ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				images.add(dummy);
				dummy = ex.findNext();
			}
			imagesText.clear();
			ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				int pos=dummy.indexOf("<DESC>");
				if (pos>0) {
					imagesText.add(dummy.substring(0,pos));
					imagesInfo.add(dummy.substring(pos+6,dummy.indexOf("</DESC>")));
				} else {
					imagesText.add(dummy);
					imagesInfo.add(null);
				}
				dummy = ex.findNext();
			}

			// Logimages
			logImages.clear();
			ex = new Extractor(text, "<LOGIMG>", "</LOGIMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				logImages.add(dummy);
				dummy = ex.findNext();
			}
			logImagesText.clear();
			ex = new Extractor(text, "<LOGIMGTEXT>", "</LOGIMGTEXT>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				logImagesText.add(dummy);
				dummy = ex.findNext();
			}

			userImages.clear();
			ex = new Extractor(text, "<USERIMG>", "</USERIMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				userImages.add(dummy);
				dummy = ex.findNext();
			}
			userImagesText.clear();
			ex = new Extractor(text, "<USERIMGTEXT>", "</USERIMGTEXT>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				userImagesText.add(dummy);
				dummy = ex.findNext();
			}

			ex = new Extractor(text, "<TRAVELBUGS>", "</TRAVELBUGS>", 0, false);
			dummy=ex.findNext();
			if (ex.endOfSearch()) {
				ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
				String Bugs = ex.findNext();
				travelbugs.addFromHTML(Bugs);
			} else
				travelbugs.addFromXML(dummy);
			
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
			solver=ex.findNext();
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
		        detfile = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + wayPoint.toLowerCase() + ".xml"), "UTF8")));
			} catch (Exception e) {
				Global.getPref().log("Problem creating details file",e,true);
				return;
			}
			try{
				if(wayPoint.length()>0){
				  detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
				  detfile.print("<CACHEDETAILS>\r\n");
				  detfile.print("<DETAILS><![CDATA["+longDescription+"]]></DETAILS>\r\n");
				  detfile.print(attributes.xmlAttributesWrite());
				  detfile.print("<HINTS><![CDATA["+hints+"]]></HINTS>\r\n");
				  detfile.print("<LOGS>\r\n");
				  for(int i = 0; i < cacheLogs.size(); i++){
					  detfile.print(cacheLogs.getLog(i).toXML());
				  }
				  detfile.print("</LOGS>\r\n");
			
				  detfile.print("<NOTES><![CDATA["+cacheNotes+"]]></NOTES>\n");
				  detfile.print("<IMAGES>");
				  String stbuf;
				  for(int i = 0;i<images.size();i++){
						detfile.print("    <IMG>"+images.get(i)+"</IMG>\n");
				  }
				  int iis = imagesInfo.size();
				  for(int i = 0;i<imagesText.size();i++){
						stbuf = (String)imagesText.get(i);
						if (i < iis && imagesInfo.get(i) != null)
							detfile.print("    <IMGTEXT>"+stbuf+"<DESC>"+imagesInfo.get(i)+"</DESC></IMGTEXT>\n");
						else 
							detfile.print("    <IMGTEXT>"+stbuf+"</IMGTEXT>\n");
				  }
				  for(int i = 0;i<logImages.size();i++){
						detfile.print("    <LOGIMG>"+logImages.get(i)+"</LOGIMG>\n");
				  }
				  for(int i = 0;i<logImagesText.size();i++){
						detfile.print("    <LOGIMGTEXT>"+logImagesText.get(i)+"</LOGIMGTEXT>\n");
				  }
				  for(int i = 0;i<userImages.size();i++){
						detfile.print("    <USERIMG>"+userImages.get(i)+"</USERIMG>\n");
				  }
				  for(int i = 0;i<userImagesText.size();i++){
						detfile.print("    <USERIMGTEXT>"+userImagesText.get(i)+"</USERIMGTEXT>\n");
				  }


				  detfile.print("</IMAGES>\n");
				  //detfile.print("<BUGS><![CDATA[\n");
				  //detfile.print(Bugs+"\n");
				  //detfile.print("]]></BUGS>\n");
				  detfile.print(travelbugs.toXML());
				  detfile.print("<URL><![CDATA["+URL+"]]></URL>\r\n");
				  detfile.print("<SOLVER><![CDATA["+solver+"]]></SOLVER>\r\n");
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
/*		public boolean belongsTo (CacheHolder ch) {
			
			// avoid self referencing
			if (this.wayPoint.equals(ch.wayPoint)) return false;

			return this.wayPoint.endsWith(ch.wayPoint.substring(2));
		}
*/		
		/**
		 * Return true if this cache has additional info for some pictures
		 * @return true if cache has additional info, false otherwise
		 */
		public boolean hasImageInfo() {
			for (int i=imagesInfo.size()-1; i>=0; i--)
				if (imagesInfo.get(i)!=null) return true;
			return false;
		}

		

//	   public void finalize() {
//		   super.finalize();
//		   Vm.debug("Destroying CacheHolder "+wayPoint);
//	   }

}
