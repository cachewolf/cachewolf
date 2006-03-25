package CacheWolf;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.data.PropertyList;

/**
*	Class to parse the index file. The methods are rather straight forward.
*	This class could probably be substituted by an Extractor class.
*/
public class MyXMLBuilder extends MinML {
	
	Vector cacheDB;
	CacheHolder holder;
	public XMLElement document;
	private XMLElement current;
	private String currentText = new String();
	private String path = new String();
	
	public MyXMLBuilder(Vector DB, String p)
	{
		cacheDB = DB;
		path = p;
	}
	
	public void doIt(){
		try{
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(path + "index.xml"));
			parse(r);
			r.close();
		}catch(Exception e){
			//Vm.debug(e.toString());
		}
	}
	public void startElement(String name, AttributeList atts){
		holder = new CacheHolder();
		for (int i = 0; i < atts.getLength(); i++) {
		  if(atts.getName(i).equals("name")) holder.CacheName = atts.getValue(i);
		  if(atts.getName(i).equals("owner")) holder.CacheOwner = atts.getValue(i);
		  if(atts.getName(i).equals("latlon")) holder.LatLon = atts.getValue(i);
		  if(atts.getName(i).equals("hidden")) holder.DateHidden = atts.getValue(i);
		  if(atts.getName(i).equals("wayp")) holder.wayPoint = atts.getValue(i);
		  if(atts.getName(i).equals("status")) holder.CacheStatus = atts.getValue(i);
		  if(atts.getName(i).equals("type")) holder.type = atts.getValue(i);
		  if(atts.getName(i).equals("dif")) holder.hard = atts.getValue(i);
		  if(atts.getName(i).equals("terrain")) holder.terrain = atts.getValue(i);
		  if(atts.getName(i).equals("dirty")) holder.dirty = atts.getValue(i);
		  if(atts.getName(i).equals("size")) holder.CacheSize = atts.getValue(i);
		  
		  if(atts.getName(i).equals("online")) holder.is_available = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("archived")) holder.is_archived = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("has_bug")) holder.has_bug = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("black")) holder.is_black = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("owned")) holder.is_owned = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("found")) holder.is_found = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("is_new")) holder.is_new = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("is_log_update")) holder.is_log_update = (atts.getValue(i).equals("true") ? true : false);
		  if(atts.getName(i).equals("is_update")) holder.is_update = (atts.getValue(i).equals("true") ? true : false);
		  // for backwards compatibility set value to true, if it is not in the file
		  if(atts.getName(i).equals("is_HTML")) holder.is_HTML = (atts.getValue(i).equals("false") ? false : true);
		  if(atts.getName(i).equals("DNFLOGS")) holder.noFindLogs = Convert.toInt(atts.getValue(i));
		  if(atts.getName(i).equals("ocCacheID")) holder.ocCacheID = atts.getValue(i);
		}
	}
	
	public void endElement(String name){
		if(name.equals("CACHE"))	cacheDB.add(holder);
	}
	
}
