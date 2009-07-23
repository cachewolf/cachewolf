package CacheWolf;

import utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.ui.FormBase;
import ewe.ui.InputBox;
import ewe.util.Vector;

public class CacheHolderDetail {
	  
	  
	 /** CacheHolder which holds the detail. <b>Only</b> set by CacheHolder when creating detail! **/
	  private CacheHolder parent = null;
	  public String LongDescription = CacheHolder.EMPTY;
	  public String LastUpdate = CacheHolder.EMPTY;
	  public String Hints = CacheHolder.EMPTY;
	  public LogList CacheLogs=new LogList();
	  private String CacheNotes = CacheHolder.EMPTY;
	  public CacheImages images = new CacheImages();
	  public CacheImages logImages = new CacheImages();
	  public CacheImages userImages = new CacheImages();
	  public Attributes attributes=new Attributes();
	  public Vector CacheIcons = new Vector();
	  public TravelbugList Travelbugs=new TravelbugList();
	  //public String Bugs = EMPTY; Superceded by Travelbugs
	  public String URL = CacheHolder.EMPTY;
	  private String Solver = CacheHolder.EMPTY;
	  public String OwnLogId = CacheHolder.EMPTY;
	  public Log OwnLog = null;
	  public String Country = CacheHolder.EMPTY;
	  public String State = CacheHolder.EMPTY;
	  /** For faster cache import (from opencaching) changes are only written when the details are freed from memory 
	   * If you want to save the changes automatically when the details are unloaded, set this to true */ 
	  public boolean hasUnsavedChanges = false;
	  
	 public CacheHolderDetail(CacheHolder ch) {
		 parent = ch;
	 }

	 public CacheHolder getParent() {
		 return parent;
	 }
	 public void setLongDescription(String longDescription) {
	 	if (LongDescription.equals("")) getParent().setNew(true);
	 	else if (!stripControlChars(LongDescription).equals(stripControlChars(longDescription))) getParent().setUpdated(true);
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
	 	if (!Hints.equals(hints)) getParent().setUpdated(true);
	 	Hints = hints;
	 }
	 
	 public void setSolver(String solver) {
		 if (!Solver.equals(solver)) getParent().setUpdated(true);
		 getParent().setHasSolver(!solver.trim().equals(""));
		 Solver = solver;
	 }
	 
	 public String getSolver() {
		 return this.Solver;
	 }

	 public void setCacheNotes(String notes) {
		 if (!CacheNotes.equals(notes)) getParent().setUpdated(true);
		 getParent().setHasNote(!notes.trim().equals(""));
		 CacheNotes = notes;
	 }
	 
	 public String getCacheNotes() {
		 return this.CacheNotes;
	 }
	 
	 public void setCacheLogs(LogList newLogs) {
		 int size=newLogs.size();
		 for (int i=size-1; i>=0; i--) { // Loop over all new logs, must start with oldest log
			 if (CacheLogs.merge(newLogs.getLog(i))>=0) getParent().setLog_updated(true);
		 }
		 if (CacheLogs.purgeLogs() > 0) hasUnsavedChanges=true;
		 getParent().setNoFindLogs(CacheLogs.countNotFoundLogs());
	 }

	 
	  /**
	 * Method to update an existing cache with new data. This is
	 * necessary to avoid missing old logs. Called from GPX Importer
	 * @param newCh new cache data
	 * @return CacheHolder with updated data
	 */
	public CacheHolderDetail update(CacheHolderDetail newCh){
		  // flags
		  if (getParent().is_found() && getParent().getCacheStatus().equals("")) getParent().setCacheStatus(MyLocale.getMsg(318,"Found"));

		  //travelbugs:GPX-File contains all actual travelbugs but not the missions
		  //  we need to check whether the travelbug is already in the existing list
		  getParent().setHas_bugs(newCh.Travelbugs.size()>0);
		  for (int i=newCh.Travelbugs.size()-1; i>=0; i--) {
			 Travelbug tb=newCh.Travelbugs.getTB(i);  
		     Travelbug oldTB=this.Travelbugs.find(tb.getName());
		     // If the bug is already in the cache, we keep it
		     if (oldTB!=null)
		    	 newCh.Travelbugs.replace(i,oldTB);
		    
		  }
		  this.Travelbugs = newCh.Travelbugs;
		  
		  if (newCh.attributes.getCount() > 0) this.attributes = newCh.attributes;
		  
		  // URL
		  this.URL = newCh.URL;
		  
		  // Images
		  this.images = newCh.images;
		  
		  setLongDescription(newCh.LongDescription);
		  setHints(newCh.Hints);
		  setCacheLogs(newCh.CacheLogs);
		  
		  if (newCh.OwnLogId.length()>0) this.OwnLogId=newCh.OwnLogId;
		  if (newCh.OwnLog != null) this.OwnLog = newCh.OwnLog;
		  
		  if (newCh.Country.length()>0) this.Country=newCh.Country;
		  if (newCh.State.length()>0) this.State=newCh.State;
		  
		  if (newCh.getSolver().length()>0) this.setSolver(newCh.getSolver());
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
			FileChooser fc = new FileChooser(FileChooserBase.OPEN, profile.dataDir);
			fc.setTitle("Select image file:");
			if(fc.execute() != FormBase.IDCANCEL){
				imgFile = fc.getChosenFile();
				imgDesc = new InputBox("Description").input("",10);
				//Create Destination Filename
				String ext = imgFile.getFileExt().substring(imgFile.getFileExt().lastIndexOf("."));
				imgDestName = getParent().getWayPoint() + "_U_" + (this.userImages.size()+1) + ext;
				
				ImageInfo userImageInfo = new ImageInfo();
				userImageInfo.setFilename(imgDestName);
				userImageInfo.setTitle(imgDesc);
				this.userImages.add(userImageInfo);
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
		void readCache(String dir) throws IOException{
			String dummy;
			FileReader in = null;
			ImageInfo imageInfo;
			// If parent cache has empty waypoint then don't do anything. This might happen
			// when a cache object is freshly created to serve as container for imported data
			if (this.getParent().getWayPoint().equals(CacheHolder.EMPTY)) return;
			
			if (new FileBugfix(dir + getParent().getWayPoint().toLowerCase() + ".xml").exists()) in = new FileReader(dir+getParent().getWayPoint().toLowerCase() + ".xml");
			if (in == null) {
				if (new FileBugfix(dir + getParent().getWayPoint() + ".xml").exists()) in = new FileReader(dir+getParent().getWayPoint() + ".xml");
			}
			if (in == null) throw new FileNotFoundException(dir+getParent().getWayPoint().toLowerCase()+".xml");
			if (Global.getPref().debug) Global.getPref().log("Reading file "+getParent().getWayPoint() + ".xml");
			String text= in.readAll();
			in.close();
			Extractor ex = new Extractor(text, "<DETAILS><![CDATA[", "]]></DETAILS>", 0, true);		
			LongDescription = ex.findNext();
			ex = new Extractor(text, "<COUNTRY><![CDATA[", "]]></COUNTRY>", 0, true);
			Country = ex.findNext();
			ex = new Extractor(text, "<STATE><![CDATA[", "]]></STATE>", 0, true);
			State = ex.findNext();
			// Attributes
			ex = new Extractor(text,"<ATTRIBUTES>","</ATTRIBUTES>",0,true);
			attributes.XmlAttributesEnd(ex.findNext());
			
			ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
			Hints = ex.findNext();
			ex = new Extractor(text, "<LOGS>","</LOGS>", 0, true);
			dummy = ex.findNext();
			ex = new Extractor(dummy, "<OWNLOGID>","</OWNLOGID>", 0, true);
			OwnLogId = ex.findNext();
			ex = new Extractor(dummy, "<OWNLOG><![CDATA[", "]]></OWNLOG>", 0, true);
			String ownLogText = ex.findNext();
			if ( ownLogText.length() > 0 ) {
				if (ownLogText.indexOf("<img src='") >= 0) {
					OwnLog = new Log( ownLogText + "]]>" );
				} else {
					OwnLog = new Log( "icon_smile.gif", "1900-01-01", Global.getPref().myAlias, ownLogText );
				}
			} else {
				OwnLog = null;
			}
			CacheLogs.clear();
			ex = new Extractor(dummy, "<LOG>","</LOG>", 0, true);
			
			dummy = ex.findNext();
			while(ex.endOfSearch()==false){
				CacheLogs.add(new Log(dummy));
				dummy = ex.findNext();
			}
			ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
			CacheNotes = ex.findNext();
			images.clear();
			ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				imageInfo = new ImageInfo();
				int pos=dummy.indexOf("<URL>");
				if (pos>0) {
					imageInfo.setFilename(SafeXML.strxmldecode(dummy.substring(0,pos)));
					imageInfo.setURL(SafeXML.strxmldecode((dummy.substring(pos+5,dummy.indexOf("</URL>")))));
				} else {
					imageInfo.setFilename(SafeXML.strxmldecode(dummy));
				}
				this.images.add(imageInfo);
				dummy = ex.findNext();
			}
			ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
			dummy = ex.findNext();
			int imgNr = 0;
			while(ex.endOfSearch() == false){
				imageInfo = this.images.get(imgNr);
				int pos=dummy.indexOf("<DESC>");
				if (pos>0) {
					imageInfo.setTitle(dummy.substring(0,pos));
					imageInfo.setComment(dummy.substring(pos+6,dummy.indexOf("</DESC>")));
				} else {
					imageInfo.setTitle(dummy);
				}
				dummy = ex.findNext();
				imgNr = imgNr + 1;
			}
			// Logimages
			logImages.clear();
			ex = new Extractor(text, "<LOGIMG>", "</LOGIMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				imageInfo = new ImageInfo();
				imageInfo.setFilename(dummy);
				logImages.add(imageInfo);
				dummy = ex.findNext();
			}
			ex = new Extractor(text, "<LOGIMGTEXT>", "</LOGIMGTEXT>", 0, true);
			dummy = ex.findNext();
			imgNr = 0;
			while(ex.endOfSearch() == false){
				imageInfo = logImages.get(imgNr++);
				imageInfo.setTitle(dummy);
				dummy = ex.findNext();
			}

			userImages.clear();
			ex = new Extractor(text, "<USERIMG>", "</USERIMG>", 0, true);
			dummy = ex.findNext();
			while(ex.endOfSearch() == false){
				imageInfo = new ImageInfo();
				imageInfo.setFilename(dummy);
				userImages.add(imageInfo);
				dummy = ex.findNext();
			}
			ex = new Extractor(text, "<USERIMGTEXT>", "</USERIMGTEXT>", 0, true);
			dummy = ex.findNext();
			imgNr = 0;
			while(ex.endOfSearch() == false){
				imageInfo = userImages.get(imgNr++);
				imageInfo.setTitle(dummy);
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
				if (getParent().getWayPoint().startsWith("GC")) {
					URL = "http://www.geocaching.com/seek/cache_details.aspx?wp="+ getParent().getWayPoint();
				}
			}
			ex = new Extractor(text, "<SOLVER><![CDATA[", "]]></SOLVER>", 0, true);
			this.setSolver(ex.findNext());
		}
		
		/**
		*	Method to save a cache.xml file.
		*/
		public void saveCacheDetails(String dir){
			PrintWriter detfile;
			
			//File exists?
			boolean exists = (new File(dir + getParent().getWayPoint() + ".xml")).exists();
			//yes: then delete
			if (exists) {
				boolean ok = (new File(dir + getParent().getWayPoint() + ".xml")).delete();
				if(ok) ok = true;
			}
			boolean exists2 = (new File(dir + getParent().getWayPoint().toLowerCase() + ".xml")).exists();
			//yes: delete
			if (exists2) {
				boolean ok2 = (new File(dir + getParent().getWayPoint().toLowerCase() + ".xml")).delete();
				if(ok2) ok2=true;
			}
			//Vm.debug("Writing to: " +dir + "for: " + wayPoint);
			try{
			  detfile = new PrintWriter(new BufferedWriter(new FileWriter(dir + getParent().getWayPoint().toLowerCase() + ".xml")));
			} catch (Exception e) {
				Global.getPref().log("Problem creating details file",e,true);
				return;
			}
			try{
				if(getParent().getWayPoint().length()>0){
				  detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
				  detfile.print("<CACHEDETAILS>\r\n");
				  detfile.print("<VERSION value = \"3\"/>\n");
				  detfile.print("<DETAILS><![CDATA["+LongDescription+"]]></DETAILS>\r\n");
				  detfile.print("<COUNTRY><![CDATA["+Country+"]]></COUNTRY>\n");
				  detfile.print("<STATE><![CDATA["+State+"]]></STATE>\n");
				  detfile.print(attributes.XmlAttributesWrite());
				  detfile.print("<HINTS><![CDATA["+Hints+"]]></HINTS>\r\n");
				  detfile.print("<LOGS>\r\n");
				  detfile.print("<OWNLOGID>"+OwnLogId+"</OWNLOGID>\r\n");
				  if ( OwnLog != null ) {
					  detfile.print("<OWNLOG><![CDATA["+OwnLog.toHtml()+"]]></OWNLOG>\r\n");
				  } else {
					  detfile.print("<OWNLOG><![CDATA[]]></OWNLOG>\r\n");
				  }
				  for(int i = 0; i < CacheLogs.size(); i++){
					  detfile.print(CacheLogs.getLog(i).toXML());
				  }
				  detfile.print("</LOGS>\r\n");
			
				  detfile.print("<NOTES><![CDATA["+CacheNotes+"]]></NOTES>\n");
				  detfile.print("<IMAGES>\n");
				  String stbuf = new String();
				  for(int i = 0;i<images.size();i++){
						stbuf = images.get(i).getFilename();
						String urlBuf = images.get(i).getURL();
						if (urlBuf != null && !urlBuf.equals("")) {
							detfile.print("    <IMG>"+SafeXML.strxmlencode(stbuf)+"<URL>"+SafeXML.strxmlencode(urlBuf)+"</URL></IMG>\n");
						} else {
							detfile.print("    <IMG>"+SafeXML.strxmlencode(stbuf)+"</IMG>\n");
						}
				  }
				  int iis = images.size();
				  for(int i = 0;i<iis;i++){
						stbuf = images.get(i).getTitle();
						if (i< iis && images.get(i).getComment() != null)
							detfile.print("    <IMGTEXT>"+stbuf+"<DESC>"+images.get(i).getComment()+"</DESC></IMGTEXT>\n");
						else 
							detfile.print("    <IMGTEXT>"+stbuf+"</IMGTEXT>\n");
				  }

				  for(int i = 0;i<logImages.size();i++){
						stbuf = logImages.get(i).getFilename();
						detfile.print("    <LOGIMG>"+stbuf+"</LOGIMG>\n");
				  }
				  for(int i = 0;i<logImages.size();i++){
						stbuf = logImages.get(i).getTitle();
						detfile.print("    <LOGIMGTEXT>"+stbuf+"</LOGIMGTEXT>\n");
				  }
				  for(int i = 0;i<userImages.size();i++){
						stbuf = userImages.get(i).getFilename();
						detfile.print("    <USERIMG>"+stbuf+"</USERIMG>\n");
				  }
				  for(int i = 0;i<userImages.size();i++){
						stbuf = userImages.get(i).getTitle();
						detfile.print("    <USERIMGTEXT>"+stbuf+"</USERIMGTEXT>\n");
				  }


				  detfile.print("</IMAGES>\n");
				  //detfile.print("<BUGS><![CDATA[\n");
				  //detfile.print(Bugs+"\n");
				  //detfile.print("]]></BUGS>\n");
				  detfile.print(Travelbugs.toXML());
				  detfile.print("<URL><![CDATA["+URL+"]]></URL>\r\n");
				  detfile.print("<SOLVER><![CDATA["+getSolver()+"]]></SOLVER>\r\n");
				  detfile.print(getParent().toXML()); // This will allow restoration of index.xml
				  detfile.print("</CACHEDETAILS>\n");
				  Global.getPref().log("Writing file: "+getParent().getWayPoint().toLowerCase() + ".xml");
				} // if length
			} catch (Exception e){
				Global.getPref().log("Problem waypoint " + getParent().getWayPoint() + " writing to a details file: " + e.getMessage());
			}
			try{
			  detfile.close();
			} catch (Exception e){
				Global.getPref().log("Problem waypoint " + getParent().getWayPoint() + " writing to a details file: " + e.getMessage());
			}
			hasUnsavedChanges = false;
		}
				
		/**
		 * Return true if this cache has additional info for some pictures
		 * @return true if cache has additional info, false otherwise
		 */
		public boolean hasImageInfo() {
			for (int i=this.images.size()-1; i>=0; i--)
				if (this.images.get(i).getComment() != null) return true;
			return false;
		}

		/**
		 * change id in waypoint details and rename associated files. Function should only be called by CacheHolder
		 * @param newWptId new id of the waypoint
		 * @return true on success, false for failure
		 */
		protected boolean rename(String newWptId) {
			boolean success = false;
			String profiledir = Global.getProfile().dataDir;
			int oldWptLength = getParent().getWayPoint().length();
			
			// just in case ... (got the pun? ;) )
			newWptId = newWptId.toUpperCase();
			
			// update image information
			for(int i = 0;i<images.size();i++){
				String filename = images.get(i).getFilename();
				String comment = images.get(i).getComment();
				String title = images.get(i).getTitle();
				if (filename.indexOf(getParent().getWayPoint())==0) {
					filename=newWptId.concat(filename.substring(oldWptLength));
					images.get(i).setFilename(filename);
				}
				if (comment.indexOf(getParent().getWayPoint())==0) {
					comment=newWptId.concat(comment.substring(oldWptLength));
					images.get(i).setComment(comment);
				}
				if (title.indexOf(getParent().getWayPoint())==0) {
					title=newWptId.concat(title.substring(oldWptLength));
					images.get(i).setTitle(title);
				}
			}
			for(int i = 0;i<logImages.size();i++){
				String filename = logImages.get(i).getFilename();
				String comment = logImages.get(i).getComment();
				String title = logImages.get(i).getTitle();
				if (filename.indexOf(getParent().getWayPoint())==0) {
					filename=newWptId.concat(filename.substring(oldWptLength));
					logImages.get(i).setFilename(filename);
				}
				if (comment.indexOf(getParent().getWayPoint())==0) {
					comment=newWptId.concat(comment.substring(oldWptLength));
					logImages.get(i).setComment(comment);
				}
				if (title.indexOf(getParent().getWayPoint())==0) {
					title=newWptId.concat(title.substring(oldWptLength));
					logImages.get(i).setTitle(title);
				}
			}
			for(int i = 0;i<userImages.size();i++){
				String filename = userImages.get(i).getFilename();
				String comment = userImages.get(i).getComment();
				String title = userImages.get(i).getTitle();
				if (filename.indexOf(getParent().getWayPoint())==0) {
					filename=newWptId.concat(filename.substring(oldWptLength));
					userImages.get(i).setFilename(filename);
				}
				if (comment.indexOf(getParent().getWayPoint())==0) {
					comment=newWptId.concat(comment.substring(oldWptLength));
					userImages.get(i).setComment(comment);
				}
				if (title.indexOf(getParent().getWayPoint())==0) {
					title=newWptId.concat(title.substring(oldWptLength));
					userImages.get(i).setTitle(title);
				}
			}

			// rename the files
			try {
				// since we use *.* we do not need FileBugFix
				String srcFiles[] = new File(profiledir).list(getParent().getWayPoint().concat("*.*"), ewe.io.FileBase.LIST_FILES_ONLY);
				for (int i=0; i < srcFiles.length;i++){
					String newfile = newWptId.concat(srcFiles[i].substring(oldWptLength));
					File srcFile = new File(profiledir.concat(srcFiles[i]));
					File dstFile = new File(profiledir.concat(newfile));
					srcFile.move(dstFile);
				}
				success = true;
			} catch (Exception e) {
				Global.getPref().log("Error renaming waypoint details", e, Global.getPref().debug);
				//TODO: any chance of a roll back?
				//TODO: should we ignore a file not found?
			}
			
			return success;
		}
}


