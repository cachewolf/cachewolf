package CacheWolf;

import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.fx.Color;
import ewe.io.*;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.ui.MessageBox;
import ewe.util.*;
import ewe.net.*;
import ewe.util.zip.*;

/**
*	Class to import Data from an GPX File. If cache data exists, the data from 
*	the GPX-File is ignored.
*	Class ID = 4000
*/
public class GPXImporter extends MinML {
	
	static Preferences pref;
	Profile profile;
	Vector cacheDB;
	CacheHolderDetail chD;
	String strData, saveDir, logData, logIcon, logDate, logFinder;
	boolean inWpt, inCache, inLogs, inBug;
	public XMLElement document;
	private Vector files = new Vector();
	private boolean debugGPX = false; 
	InfoBox infB;
	boolean spiderOK = true;
	boolean doSpider = false;
	boolean fromOC = false;
	boolean fromTC = false;
	boolean nameFound = false;
	int zaehlerGel = 0;
	Hashtable DBindex = new Hashtable();
	public static final int DOIT_ASK = 0;
	public static final int DOIT_NOSPOILER = 1;
	public static final int DOIT_WITHSPOILER = 2;
	boolean getMaps = false;
	SpiderGC imgSpider;
	StringBuffer strBuf;
	
	public GPXImporter(Preferences p, Profile prof, String f )
	{
		profile=prof;
		pref = p;
		cacheDB = profile.cacheDB;
		//file = f;
		files.add(f);
		saveDir = profile.dataDir;
		//msgA = msgArea;
		inWpt = false;
		inCache = false;
		inLogs = false;
		inBug =false;
		//index db for faster search
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindex.put((String)ch.wayPoint, new Integer(i));
		}//for
	}
/*	skg: This Constructor is not referenced, therefore commented out 
	public GPXImporter(Vector DB, String[] f,String d, Preferences p)
	{
		pref = p;
		cacheDB = DB;
		saveDir = pref.mydatadir;
		for (int i=0;i<f.length;i++){
			files.add(d + "/" + f[i]);
		}
		
		//msgA = msgArea;
		inWpt = false;
		inCache = false;
		inLogs = false;
		inBug =false;
		strData = new String();
		//index db for faster search
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindex.put((String)ch.wayPoint, new Integer(i));
		}//for
	}
*/	
	public void doIt(int how){
		Filter flt = new Filter();
		boolean wasFiltered = (profile.filterActive==Filter.FILTER_ACTIVE);
		flt.clearFilter();
		try{
			ewe.io.Reader r;
			String file;
			
			OCXMLImporterScreen options = new OCXMLImporterScreen("Spider Options", OCXMLImporterScreen.IMAGES);
			if (options.execute() == OCXMLImporterScreen.IDCANCEL) {	return; }
			//String dist = options.distanceInput.getText();
			//if (dist.length()== 0) return;
			//getMaps = options.mapsCheckBox.getState();
			boolean getImages = options.imagesCheckBox.getState();
			doSpider = false;
			if(getImages){
				doSpider = true;
				imgSpider = new SpiderGC(pref, profile, false);
			}
			options.close(0);
			
			//Vm.debug("State of: " + doSpider);
			Vm.showWait(true);
			for (int i=0; i<files.size();i++){
				//Test for zip.file
				file = (String)files.get(i);
				if (file.indexOf(".zip") > 0){
					ZipFile zif = new ZipFile (file);
					ZipEntry zipEnt;
					Enumeration zipEnum = zif.entries();
					// there could be more than one file in the archive
					while (zipEnum.hasMoreElements())
					{
						zipEnt = (ZipEntry) zipEnum.nextElement();
						// skip over PRC-files
						if (zipEnt.getName().endsWith("gpx")){
							r = new ewe.io.InputStreamReader(zif.getInputStream(zipEnt));
							infB = new InfoBox(zipEnt.toString(),(MyLocale.getMsg(4000,"Loaded caches") + ":" + zaehlerGel));
							infB.exec();
							parse(r);
							r.close();
							infB.close(0);
						}
					}
				}
				else {
					r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(file));
					infB = new InfoBox("Info",(MyLocale.getMsg(4000,"Loaded caches") + ":" + zaehlerGel));
					infB.show();
					parse(r);
					r.close();
					infB.close(0);
				}
				// save Index 
				profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
				infB.close(0);
			}
				Vm.showWait(false);
			}catch(Exception e){
				e.printStackTrace();
				Vm.showWait(false);
			}
		if(wasFiltered){
			flt.setFilter();
			flt.doFilter();
		}
	}
	public void startElement(String name, AttributeList atts){
		strBuf=new StringBuffer(300);
		if (name.equals("gpx")){
			// check for opencaching
			if (atts.getValue("creator").indexOf("opencaching")> 0) fromOC = true;
			else fromOC = false;
			if (atts.getValue("creator").startsWith("TerraCaching")) fromTC = true;
			else fromTC = false;

			if (fromOC && doSpider) (new MessageBox("Warnung", "GPX-Dateien von Opencaching enthalten keine Informationen zu Bildern, sie werden nicht heruntergeladen. Am besten Caches von Opencaching holen per Menü /Anwendung/Download von Opencaching", MessageBox.OKB)).execute();
			zaehlerGel = 0;
		}
		if (name.equals("wpt")) {
			chD = new CacheHolderDetail();
			chD.pos.set(Common.parseDouble(atts.getValue("lat")),Common.parseDouble(atts.getValue("lon")));
			chD.LatLon=chD.pos.toString();
			inWpt = true;
			inLogs = false;
			inBug = false;
			nameFound = false;
			return;
		}
		
		if (name.equals("link")&& inWpt){
			chD.URL = atts.getValue("href");
			return;
		}

		if (name.equals("groundspeak:cache")) {
			inCache = true;
			if (atts.getValue("available").equals("True"))
				chD.is_available = true;
			else 
				chD.is_available = false;
			if (atts.getValue("archived").equals("True"))
				chD.is_archived = true;
			else
				chD.is_archived = false;
			return;
		}

		if (name.equals("geocache")) {
			inCache=true;
			// get status
			String status = new String(atts.getValue("status"));
			chD.is_available = false;
			chD.is_archived = false;
			if (status.equals("Available")) chD.is_available = true;
			if (status.equals("Unavailable")) chD.is_available = false;
			if (status.equals("Draft")) chD.is_available = false;
			if (status.equals("Archived")) chD.is_archived = true;
			return;
		}
		
		if (name.equals("terra:terracache")) {
			inCache=true;
		}

		
		if (name.equals("groundspeak:long_description")) {
			if (atts.getValue("html").toLowerCase().equals("true"))
				chD.is_HTML= true;
			else 
				chD.is_HTML = false;
			
		}
		if (name.equals("description") || name.equals("terra:description") ) {
			//set HTML always to true if from oc.de or TC
			chD.is_HTML= true;
		}

		if (name.equals("groundspeak:logs") || name.equals("log") || name.equals("terra:logs")) {
			inLogs = true;
			return;
		}
		if (name.equals("groundspeak:travelbugs")) {
			inBug = true;
			return;
		}
		if (debugGPX){
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug("Type: " + atts.getType(i) + " Name: " + atts.getName(i)+ " Value: "+atts.getValue(i));
			}
		}
	}
	
	public void endElement(String name){
		strData=strBuf.toString();
		//Vm.debug("Ende: " + name);
		
		// logs
		if (inLogs){
			if (name.equals("groundspeak:date")|| name.equals("time")|| name.equals("terra:date"))  {
				logDate = new String(strData.substring(0,10));
				return;
			}
			if (name.equals("groundspeak:type") || name.equals("type") || name.equals("terra:type")){
				logIcon = new String(typeText2Image(strData));
				return;
			}
			if (name.equals("groundspeak:finder")|| name.equals("geocacher")|| name.equals("terra:user")){
				logFinder = new String(strData);
				return;
			}
			if (name.equals("groundspeak:text") || name.equals("text") || name.equals("terra:entry")){ 
				logData = new String(strData);
				return;
			}
			if (name.equals("groundspeak:log") || name.equals("log") || name.equals("terra:log") ) {
				chD.CacheLogs.add(new Log(logIcon,logDate,logFinder,logData));
				if(logIcon.equals("icon_smile.gif") && 
						  (logFinder.equalsIgnoreCase(pref.myAlias) || (pref.myAlias2.length()>0 && logFinder.equalsIgnoreCase(pref.myAlias2)))) {
							chD.CacheStatus=logDate;
							chD.is_found=true;
				}
				return;
			}
		}
		
		if (name.equals("wpt")){
			// Add cache Data only, if waypoint not already in database
			//if (searchWpt(cacheDB, holder.wayPoint)== -1){
			int index=searchWpt(chD.wayPoint);
			//Vm.debug("here ?!?!?");
			//Vm.debug("chould be new!!!!");
			if (index == -1){
				chD.noFindLogs=chD.CacheLogs.countNotFoundLogs();
				chD.is_new = true;
				cacheDB.add(new CacheHolder(chD));
				// don't spider additional waypoints, so check
				// if waypoint starts with "GC"
				if(doSpider == true) {
					if(spiderOK == true && chD.is_archived == false){
							if(chD.LatLon.length() > 1){
							if(getMaps){
								ParseLatLon pll = new ParseLatLon(chD.LatLon,".");
								pll.parse();
								//MapLoader mpl = new MapLoader(pref.myproxy, pref.myproxyport);
								//mpl.loadTo(profile.dataDir + "/" + holder.wayPoint + "_map.gif", "3");
								//mpl.loadTo(profile.dataDir + "/" + holder.wayPoint + "_map_2.gif", "10");
							}
						}
					if(chD.wayPoint.startsWith("GC")|| fromTC) {
						//spiderImages();
						spiderImagesUsingSpider();
						//Rename image sources
						String text;
						String orig;
						String imgName;
						orig = chD.LongDescription;
						Extractor ex = new Extractor(orig, "<img src=\"", ">", 0, false);
						text = ex.findNext();
						int num = 0;
						while(ex.endOfSearch() == false && spiderOK == true){
							//Vm.debug("Replacing: " + text);
							if (num >= chD.ImagesText.getCount())break;
							imgName = (String)chD.ImagesText.get(num);
							chD.LongDescription = replace(chD.LongDescription, text, "[[Image: " + imgName + "]]");
							num++;
							text = ex.findNext();
						}
					}
						
					}
				}
				chD.saveCacheDetails(saveDir);
				//crw.saveIndex(cacheDB,saveDir);
			}
			//Update cache data
			else {
				//Vm.debug("it is not new!");
				CacheHolderDetail oldCh= new CacheHolderDetail((CacheHolder) cacheDB.get(index));
				try {
					//Vm.debug("Try to load");
					oldCh.readCache(saveDir);
					//Vm.debug("Done loading");
				} catch (Exception e) {Vm.debug("Could not open file: " + e.toString());};
				oldCh.update(chD);
				oldCh.saveCacheDetails(saveDir);
				cacheDB.set(index, new CacheHolder(oldCh));
				//crw.saveIndex(cacheDB,saveDir);
			}
			
			inWpt = false;
			return;
		}
		if (name.equals("sym")&& strData.endsWith("Found")) {
			chD.is_found = true;
			chD.CacheStatus = MyLocale.getMsg(318,"Found");
			return;
		}
		if (name.equals("groundspeak:travelbugs")) {
			inBug = false;
			return;
		}

		if (name.equals("groundspeak:name")&& inBug) {
			Travelbug tb=new Travelbug(strData);
			chD.Travelbugs.add(tb);
			//holder.Bugs += "<b>Name:</b> " + strData + "<br><hr>";
			chD.has_bug = true;
			return;
		}
		
		if (name.equals("time") && inWpt) {
			//String Date = new String();
			//Date = strData.substring(5,7); // month
			//Date += "/" + strData.substring(8,10); // day
			//Date += "/" + strData.substring(0,4); // year
			chD.DateHidden = strData.substring(0,10); //Date;
			return;
		}
		// cache information
		if (name.equals("groundspeak:cache") || name.equals("geocache")|| name.equals("terra:terracache")) {
			inCache = false;
		}
		
		if (name.equals("name") && inWpt && !inCache) {
			chD.wayPoint = strData;
			//msgA.setText("import " + strData);
			return;
		}
		//Vm.debug("Check: " + inWpt + " / " + fromOC);
		//if (name.equals("desc") && inWpt && fromOC) {
		// fill name with contents of <desc>, in case of gc.com the name is
		// later replaced by the contents of <groundspeak:name> which is shorter
		if (name.equals("desc")&& inWpt ) {
			chD.CacheName = strData;
			//Vm.debug("CacheName: " + strData);
			//msgA.setText("import " + strData);
			return;
		}
		if (name.equals("url")&& inWpt){
			chD.URL = strData;
			return;
		}
		
		// Text for additional waypoints, no HTML
		if (name.equals("cmt")&& inWpt){
			chD.LongDescription = strData;
			chD.is_HTML = false;
			return;
		}
		
		// aditional wapypoint
		if (name.equals("type")&& inWpt && !inCache && strData.startsWith("Waypoint")){
			chD.type= CacheType.typeText2Number(strData);
			chD.CacheSize = "None";
		}

		
		if ((name.equals("groundspeak:name")|| name.equals("terra:name")) && inCache) {
			chD.CacheName = strData;
			return;
		}
		if (name.equals("groundspeak:owner") || name.equals("owner")||name.equals("terra:owner")) {
			chD.CacheOwner = strData;
			if(pref.myAlias.equals(strData)) chD.is_owned = true;
			return;
		}
		if (name.equals("groundspeak:difficulty") || name.equals("difficulty") || name.equals("terra:mental_challenge")) {
			chD.hard = strData.replace('.',',');
			return;
		}
		if (name.equals("groundspeak:terrain")|| name.equals("terrain")|| name.equals("terra:physical_challenge")) {
			chD.terrain = strData.replace('.',',');
			return;
		}
		if ((name.equals("groundspeak:type") || name.equals("type")|| name.equals("terra:style"))&& inCache){
			chD.type= CacheType.typeText2Number(strData);
			return;
		}
		if (name.equals("groundspeak:container")|| name.equals("container")){
			chD.CacheSize = strData;
			return;
		}
		
		if (name.equals("terra:size")){
			chD.CacheSize = TCSizetoText(strData);
		}

		if (name.equals("groundspeak:short_description")|| name.equals("summary")) {
			if (chD.is_HTML)	chD.LongDescription =SafeXML.cleanback(strData)+"<br>"; // <br> needed because we also use a <br> in SpiderGC. Without it the comparison in ch.update fails
			else chD.LongDescription =strData+"\n";
			return;
		}

		if (name.equals("groundspeak:long_description")|| name.equals("description")|| name.equals("terra:description")) {
			if (chD.is_HTML)	chD.LongDescription +=SafeXML.cleanback(strData);
			else chD.LongDescription +=strData;
			return;
		}
		if (name.equals("groundspeak:encoded_hints") || name.equals("hints")) {
			chD.Hints = Common.rot13(strData);
			return;
		}
		
		if (name.equals("terra:hint")) {
			// remove "&lt;br&gt;<br>" from the end
			int indexTrash = strData.indexOf("&lt;br&gt;<br>");
			if (indexTrash > 0)	chD.Hints = Common.rot13(strData.substring(0,indexTrash));
			return;
		}


	}
	public void characters(char[] ch,int start,int length){
		strBuf.append(ch,start,length);
		if (debugGPX) Vm.debug("Char: " + strBuf.toString());
	}
	

	public static String typeText2Image(String typeText){
		if (typeText.equals("Found it")||typeText.equals("Found")||typeText.equals("find")) return "icon_smile.gif";
		if (typeText.equals("Didn't find it")||typeText.equals("Not Found")||typeText.equals("no_find")) return "icon_sad.gif";
		if (typeText.equals("Write note")||typeText.equals("Note")||typeText.equals("note")
			||typeText.equals("Not Attempted")||typeText.equals("Other")) return "icon_note.gif";
		if (typeText.equals("Enable Listing")) return "icon_enabled.gif";
		if (typeText.equals("Temporarily Disable Listing")) return "icon_disabled.gif";
		if (typeText.equals("Webcam Photo Taken")) return "11.png";
		if (typeText.equals("Attended")) return "icon_attended.gif";
		if (typeText.equals("Publish Listing")) return "green.png";
		if (typeText.equals("Will Attend")) return "icon_rsvp.gif";
		if (typeText.equals("Post Reviewer Note")) return "big_smile.gif";
		if (typeText.equals("Unarchive")) return "traffic_cone.gif";
		if (typeText.equals("Archive (show)")) return "traffic_cone.gif";
		if (typeText.equals("Owner Maintenance")) return "icon_maint.gif";
		if (typeText.equals("Needs Maintenance")) return "icon_needsmaint.gif";
		if (typeText.equals("Update Coordinates")) return "coord_update.gif";
		//Vm.debug("Unknown Log Type:" + typeText);
		return typeText;
	}
	
	public static String TCSizetoText(String size){
		if (size.equals("1")) return "Micro";
		if (size.equals("2")) return "Medium";
		if (size.equals("3")) return "Regular";
		if (size.equals("4")) return "Large";
		if (size.equals("5")) return "Very Large";

		return "None";
	}

	/**
	* Method to iterate through cache database and look for waypoint.
	* Returns value >= 0 if waypoint is found, else -1
	*/
	/*
	private int searchWpt(Vector db, String wpt){
		if(wpt.length()>0){
			wpt = wpt.toUpperCase();
			CacheHolder ch = new CacheHolder();
			//Search through complete database
			for(int i = 0;i < db.size();i++){
				ch = (CacheHolder)db.get(i);
				if(ch.wayPoint.indexOf(wpt) >=0 ){
					return i;
				}
			} // for
		} // if
		return -1;
	}
	*/
	
	private int searchWpt(String wpt){
		Integer INTR = (Integer)DBindex.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} else return -1;
	}
	private void spiderImagesUsingSpider(){
		String addr;
		String cacheText;
		
		// just to be sure to have a spider object
		if (imgSpider == null) imgSpider = new SpiderGC(pref, profile, false);
		
		if (fromTC) {
				imgSpider.getImages(chD.LongDescription, chD);
		}
		else {
			addr = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + chD.wayPoint ;
			//Vm.debug(addr + "|");
			cacheText = SpiderGC.fetch(addr);
			imgSpider.getImages(cacheText, chD);
		}
	}
	
	public static String replace(String source, String pattern, String replace){
		if (source!=null)
		{
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;
		
			while( (found = source.indexOf(pattern, start) ) != -1) {
			    sb.append(source.substring(start, found));
			    sb.append(replace);
			    start = found + len;
			}
		
			sb.append(source.substring(start));
		
			return sb.toString();
		}
		else return "";
	}
}
