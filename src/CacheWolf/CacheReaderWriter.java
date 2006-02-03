package CacheWolf;

import ewe.util.*;
import ewesoft.xml.*;
import ewe.io.*;
import ewe.sys.*;

/**
*	This class is responsible for reading and writing cache information to or
*	from a file. It does not! read the index.xml file but it does create it.
*/
public class CacheReaderWriter {
	
	CacheHolder ch = new CacheHolder();
	PrintWriter detfile;
	
	/**
	*	Method to parse a specific cache.xml file.
	*	It fills information on cache details, hints, logs, notes and
	*	images.
	*/
	public void readCache(CacheHolder cache, String dir) throws Exception{
		String dummy = new String();
		FileReader in = new FileReader(dir+cache.wayPoint+".xml");
		String text = new String();
		text = in.readAll();
		in.close();
		Extractor ex = new Extractor(text, "<DETAILS><![CDATA[", "]]></DETAILS>", 0, true);		
		cache.LongDescription = ex.findNext();
		ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
		cache.Hints = ex.findNext();
		ex = new Extractor(text, "<LOGS>","</LOGS>", 0, true);
		dummy = ex.findNext();
		cache.CacheLogs.clear();
		ex = new Extractor(dummy, "<LOG><![CDATA[","]]></LOG>", 0, true);
		
		dummy = ex.findNext();
		while(ex.endOfSearch()==false){
			cache.CacheLogs.add(dummy);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
		cache.CacheNotes = ex.findNext();
		cache.Images.clear();
		ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			cache.Images.add(dummy);
			dummy = ex.findNext();
		}
		
		cache.ImagesText.clear();
		ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			cache.ImagesText.add(dummy);
			dummy = ex.findNext();
		}

		cache.LogImages.clear();
		ex = new Extractor(text, "<LOGIMG>", "</LOGIMG>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			cache.LogImages.add(dummy);
			dummy = ex.findNext();
		}
		cache.LogImagesText.clear();
		ex = new Extractor(text, "<LOGIMGTEXT>", "</LOGIMGTEXT>", 0, true);
		dummy = ex.findNext();
		while(ex.endOfSearch() == false){
			cache.LogImagesText.add(dummy);
			dummy = ex.findNext();
		}

		ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
		cache.Bugs = ex.findNext();
		
		ex = new Extractor(text, "<URL><![CDATA[", "]]></URL>", 0, true);
		// if no URL is stored, set default URL (at this time only possible for gc.com)
		dummy = ex.findNext();
		if (dummy.length() > 10){
			cache.URL = dummy;
		}
		else {
			if (cache.wayPoint.startsWith("GC")) {
				cache.URL = "http://www.geocaching.com/seek/cache_details.aspx?wp="+ cache.wayPoint + "&Submit6=Find&log=y";
			}
		}

	}
	
	/**
	*	Method to save a cache.xml file.
	*/
	public void saveCacheDetails(CacheHolder ch, String dir){
		String dummy = new String();
		//File exists?
		boolean exists = (new File(dir + ch.wayPoint + ".xml")).exists();
		//yes: then delete
		if (exists) {
			boolean success = (new File(dir + ch.wayPoint + ".xml")).delete();
		}
		boolean exists2 = (new File(dir + ch.wayPoint.toLowerCase() + ".xml")).exists();
		//yes: delete
		if (exists2) {
			boolean success2 = (new File(dir + ch.wayPoint.toLowerCase() + ".xml")).delete();
		}
		//Vm.debug("Writing to: " +dir + "for: " + ch.wayPoint);
		try{
		  detfile = new PrintWriter(new BufferedWriter(new FileWriter(dir + ch.wayPoint + ".xml")));
		} catch (Exception e) {
			Vm.debug("Problem opening details file");
		}
		try{
			if(ch.wayPoint.length()>0){
			  detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
			  detfile.print("<CACHEDETAILS>\r\n");
			  detfile.print("<DETAILS><![CDATA["+ch.LongDescription+"]]></DETAILS>\r\n");
			  detfile.print("<HINTS><![CDATA["+ch.Hints+"]]></HINTS>\r\n");
			  detfile.print("<LOGS>\r\n");
			  for(int i = 0; i < ch.CacheLogs.size(); i++){
				  dummy = (String)ch.CacheLogs.get(i);
				  detfile.print("<LOG><![CDATA[\r\n");
				  detfile.print(dummy);
				  detfile.print("\r\n]]></LOG>\r\n");
			  }
			  detfile.print("</LOGS>\r\n");
		
			  detfile.print("<NOTES><![CDATA["+ch.CacheNotes+"]]></NOTES>\n");
			  detfile.print("<IMAGES>");
			  String stbuf = new String();
			  for(int i = 0;i<ch.Images.size();i++){
					stbuf = (String)ch.Images.get(i);
					detfile.print("    <IMG>"+stbuf+"</IMG>\n");
			  }
			  for(int i = 0;i<ch.ImagesText.size();i++){
					stbuf = (String)ch.ImagesText.get(i);
					detfile.print("    <IMGTEXT>"+stbuf+"</IMGTEXT>\n");
			  }

			  for(int i = 0;i<ch.LogImages.size();i++){
					stbuf = (String)ch.LogImages.get(i);
					detfile.print("    <LOGIMG>"+stbuf+"</LOGIMG>\n");
			  }
			  for(int i = 0;i<ch.LogImagesText.size();i++){
					stbuf = (String)ch.LogImagesText.get(i);
					detfile.print("    <LOGIMGTEXT>"+stbuf+"</LOGIMGTEXT>\n");
			  }

			  detfile.print("</IMAGES>\n");
			  detfile.print("<BUGS><![CDATA[\n");
			  detfile.print(ch.Bugs+"\n");
			  detfile.print("]]></BUGS>\n");
			  detfile.print("<URL><![CDATA["+ch.URL+"]]></URL>\r\n");
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
	*	Method to save the index.xml file that holds the total information
	*	on available caches in the database. The database in nothing else
	*	than the collection of caches in a directory.
	*/
	public void saveIndex(Vector DB, String dir){
		try{
		  detfile = new PrintWriter(new BufferedWriter(new FileWriter(dir + "index.xml")));
		} catch (Exception e) {
		  //Vm.debug("Problem creating index file");
		}
		try{
			detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			detfile.print("<CACHELIST>\n");
			for(int i = 0; i<DB.size();i++){
				ch = (CacheHolder)DB.get(i);
				////Vm.debug("Saving: " + ch.CacheName);
				if(ch.wayPoint.length()>0 && ch.LongDescription.equals("An Error Has Occured") == false){
					detfile.print("    <CACHE name = \""+SafeXML.clean(ch.CacheName)+"\" owner = \""+SafeXML.clean(ch.CacheOwner)+"\" latlon = \""+ch.LatLon+"\" hidden = \""+ch.DateHidden+"\" wayp = \""+ch.wayPoint+"\" status = \""+ch.CacheStatus+"\" type = \""+ch.type+"\" dif = \""+ch.hard+"\" terrain = \"" + ch.terrain + "\" dirty = \"" + ch.dirty + "\" size = \""+ch.CacheSize+"\" online = \"" + Convert.toString(ch.is_available) + "\" archived = \"" + Convert.toString(ch.is_archived) + "\" has_bug = \"" + Convert.toString(ch.has_bug) + "\" black = \"" + Convert.toString(ch.is_black) + "\" owned = \"" + Convert.toString(ch.is_owned) + "\" found = \"" + Convert.toString(ch.is_found) + "\" is_new = \"" + Convert.toString(ch.is_new) +"\" is_log_update = \"" + Convert.toString(ch.is_log_update) + "\" is_update = \"" + Convert.toString(ch.is_update) + "\" is_HTML = \"" + Convert.toString(ch.is_HTML) + "\"DNFLOGS = \"" + ch.noFindLogs + "\"/>\n");
				}
			}
			detfile.print("</CACHELIST>\n");
			detfile.close();
		}catch(Exception e){
			//Vm.debug("Problem writing to index file");
		}
		
	}
	
		/** Replace all instances of a String in a String.
		 *   @param  s  String to alter.
		 *   @param  f  String to look for.
		 *   @param  r  String to replace it with, or null to just remove it.
		 */ 
		private String replace( String s, String f, String r )
		{
		   if (s == null)  return s;
		   if (f == null)  return s;
		   if (r == null)  r = "";
		
		   int index01 = s.indexOf( f );
		   while (index01 != -1)
		   {
			  s = s.substring(0,index01) + r + s.substring(index01+f.length());
			  index01 += r.length();
			  index01 = s.indexOf( f, index01 );
		   }
		   return s;
		}
}
