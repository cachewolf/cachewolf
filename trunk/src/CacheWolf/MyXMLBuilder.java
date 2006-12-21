package CacheWolf;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.data.PropertyList;

/**
*	Class to parse the index.xml file. The methods are rather straight forward.
*	This class could probably be substituted by an Extractor class.
*   index.xml is saved by class CacheReaderWriter.
*/
public class MyXMLBuilder extends MinML {
	
	Vector cacheDB;
	CacheHolder holder;
	public XMLElement document;
	//private XMLElement current;
	//private String currentText = new String();
	private String path = new String();
	static protected final int NAME 	= 0;
	static protected final int OWNER 	= 1;
	static protected final int LATLON 	= 2;
	static protected final int HIDDEN 	= 3;
	static protected final int WAYP 	= 4;
	static protected final int STATUS 	= 5;
	static protected final int TYPE 	= 6;
	static protected final int DIF 		= 7;
	static protected final int TERRAIN 	= 8;
	static protected final int DIRTY 	= 9;
	static protected final int SIZE 	= 10;
	static protected final int ONLINE 	= 11;
	static protected final int ARCHIVED = 12;
	static protected final int HAS_BUG 	= 13;
	static protected final int BLACK 	= 14;
	static protected final int OWNED 	= 15;
	static protected final int FOUND 	= 16;
	static protected final int IS_NEW 	= 17;
	static protected final int IS_LOG_UPDATE = 18;
	static protected final int IS_UPDATE = 19;
	static protected final int IS_HTML 	= 20;
	static protected final int DNFLOGS 	= 21;
	static protected final int OCCACHEID = 22;
	
	
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
		if(name.equals("CACHE")){
			holder = new CacheHolder();
			holder.CacheName = atts.getValue(NAME);
			holder.CacheOwner = atts.getValue(OWNER);
			holder.LatLon = atts.getValue(LATLON);
			holder.DateHidden = atts.getValue(HIDDEN);
			holder.wayPoint = atts.getValue(WAYP);
			holder.CacheStatus = atts.getValue(STATUS);
			holder.type = atts.getValue(TYPE);
			holder.hard = atts.getValue(DIF);
			holder.terrain = atts.getValue(TERRAIN);
			holder.dirty = atts.getValue(DIRTY);
			holder.CacheSize = atts.getValue(SIZE);
			  
			holder.is_available = (atts.getValue(ONLINE).equals("true") ? true : false);
			holder.is_archived = (atts.getValue(ARCHIVED).equals("true") ? true : false);
			holder.has_bug = (atts.getValue(HAS_BUG).equals("true") ? true : false);
			holder.is_black = (atts.getValue(BLACK).equals("true") ? true : false);
			holder.is_owned = (atts.getValue(OWNED).equals("true") ? true : false);
			holder.is_found = (atts.getValue(FOUND).equals("true") ? true : false);
			holder.is_new = (atts.getValue(IS_NEW).equals("true") ? true : false);
			holder.is_log_update = (atts.getValue(IS_LOG_UPDATE).equals("true") ? true : false);
			holder.is_update = (atts.getValue(IS_UPDATE).equals("true") ? true : false);
			  // for backwards compatibility set value to true, if it is not in the file
			holder.is_HTML = (atts.getValue(IS_HTML).equals("false") ? false : true);
			holder.noFindLogs = Convert.toInt(atts.getValue(DNFLOGS));
			holder.ocCacheID = atts.getValue(OCCACHEID);
		}

/*		for (int i = 0; i < atts.getLength(); i++) {
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
*/	}
	
	public void endElement(String name){
		if(name.equals("CACHE"))	cacheDB.add(holder);
	}
	
}
