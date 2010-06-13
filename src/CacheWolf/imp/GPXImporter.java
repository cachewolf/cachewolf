package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.Extractor;
import CacheWolf.Filter;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.MyLocale;
import CacheWolf.ParseLatLon;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.SafeXML;
import CacheWolf.Travelbug;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Enumeration;
import ewe.util.Vector;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.AttributeList;

/**
*	Class to import Data from an GPX File. If cache data exists, the data from 
*	the GPX-File is ignored.
*	Class ID = 4000
*/
public class GPXImporter extends MinML {
	
	static Preferences pref;
	Profile profile;
	CacheDB cacheDB;
	CacheHolder holder;
	String strData, saveDir, logData, logIcon, logDate, logFinder, logId;
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
	static final Time gpxDate = new Time();
	int zaehlerGel = 0;
	public static final int DOIT_ASK = 0;
	public static final int DOIT_NOSPOILER = 1;
	public static final int DOIT_WITHSPOILER = 2;
	SpiderGC imgSpider;
	StringBuffer strBuf;
	private int doitHow;
	
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
		inBug = false;
		doitHow = DOIT_ASK;
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
		doitHow = how;
		Filter flt = new Filter();
		boolean wasFiltered = (profile.getFilterActive()==Filter.FILTER_ACTIVE);
		flt.clearFilter();
		try{
			ewe.io.Reader r;
			String file;
			if (how == DOIT_ASK) {
				OCXMLImporterScreen options = new OCXMLImporterScreen(MyLocale.getMsg(5510,"Spider Options"), OCXMLImporterScreen.IMAGES| OCXMLImporterScreen.ISGC);
				if (options.execute() == FormBase.IDCANCEL) { return; }
				doSpider = options.imagesCheckBox.getState();
				options.close(0);
			} else if (how == DOIT_NOSPOILER) {
				doSpider = false;
			} else {
				doSpider = true;
			}
			if (doSpider) {
				imgSpider = new SpiderGC(pref, profile, false);
				doitHow = DOIT_WITHSPOILER;
			} else {
				doitHow = DOIT_NOSPOILER;
			}
			
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
							infB = new InfoBox(zipEnt.toString(),(MyLocale.getMsg(4000,"Loaded caches: ") + zaehlerGel));
							infB.exec();
							if (r.read() != 65279)
								r = new ewe.io.InputStreamReader(zif.getInputStream(zipEnt));
							parse(r);
							r.close();
							infB.close(0);
						}
					}
				}
				else {
					r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(file));
					infB = new InfoBox("Info",(MyLocale.getMsg(4000,"Loaded caches: ") + zaehlerGel));
					infB.show();
					if (r.read() != 65279)
						r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(file));
					parse(r);
					r.close();
					infB.close(0);
				}
				// save Index 
				profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
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

			if (fromOC && doSpider) (new MessageBox("Warnung", MyLocale.getMsg(4001, "GPX files from opencaching don't contain information of images, they cannot be laoded. Best you get caches from opencaching by menu /Application/Import/Download from Opencaching"), FormBase.OKB)).execute();
			zaehlerGel = 0;
		}
		if (name.equals("wpt")) {
			holder = new CacheHolder();
			holder.pos.set(Common.parseDouble(atts.getValue("lat")),Common.parseDouble(atts.getValue("lon")));
			holder.setLatLon(holder.pos.toString());
			inWpt = true;
			inLogs = false;
			inBug = false;
			nameFound = false;
			zaehlerGel++;
			infB.setInfo(MyLocale.getMsg(4000,"Loaded caches: ") + zaehlerGel);
			return;
		}
		
		if (name.equals("link")&& inWpt){
			holder.getCacheDetails(false).URL = atts.getValue("href");
			return;
		}

		if (name.equals("groundspeak:cache")) {
			inCache = true;
			holder.setAvailable(atts.getValue("available").equals("True"));
			holder.setArchived(atts.getValue("archived").equals("True"));
			return;
		}
		// OC
		if (name.equals("geocache")|| name.equals("cache")) {
			boolean available = false;
			boolean archived  = false;
			inCache=true;
			// get status
			String status = new String(atts.getValue("status"));
			if (status.equals("Available")) available = true;
			else if (status.equals("Unavailable")) available = false;
			else if (status.equals("Draft")) available = false;
			else if (status.equals("Archived")) archived = true;
			holder.setArchived(archived);
			holder.setAvailable(available);
			return;
		}
		
		if (name.equals("terra:terracache")) {
			inCache=true;
		}

		
		if (name.indexOf("long_description") > -1) {
			holder.setHTML(atts.getValue("html").toLowerCase().equals("true"));
		}
		if (name.equals("description") || name.equals("terra:description") ) {
			//set HTML always to true if from oc.de or TC
			holder.setHTML(true);
		}

		if (name.equals("groundspeak:logs") || name.equals("logs") || name.equals("terra:logs")) {
			inLogs = true;
			return;
		}
		if (name.equals("groundspeak:log") || name.equals("log") || name.equals("terra:log")) {
			inLogs = true;
			logId = atts.getValue("id");
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
		if (name.equals("groundspeak:attribute")) {
			int id = Integer.parseInt(atts.getValue("id"));
			holder.getCacheDetails(false).attributes.add(id,atts.getValue("inc")); // from GC!
			holder.setAttribsAsBits(holder.getCacheDetails(false).attributes.getAttribsAsBits());
			return;
		}		
	}
	
	public void endElement(String name){
		strData=strBuf.toString();
		//Vm.debug("Ende: " + name);
		
		// logs
		if (inLogs){
			if (name.equals("groundspeak:date")|| name.equals("time")|| name.equals("date")|| name.equals("terra:date"))  {
				logDate = new String(strData.substring(0,10));
				return;
			}
			if (name.equals("groundspeak:type") || name.equals("type") || name.equals("terra:type")){
				logIcon = new String(typeText2Image(strData));
				return;
			}
			if (name.equals("groundspeak:finder")|| name.equals("geocacher")|| name.equals("finder")|| name.equals("terra:user")){
				logFinder = new String(strData);
				return;
			}
			if (name.equals("groundspeak:text") || name.equals("text") || name.equals("terra:entry")){ 
				logData = new String(strData);
				return;
			}
			if (name.equals("groundspeak:log") || name.equals("log") || name.equals("terra:log") ) {
				holder.getCacheDetails(false).CacheLogs.add(new Log(logIcon,logDate,logFinder,logData));
				if((logIcon.equals("icon_smile.gif") || logIcon.equals("icon_camera.gif") || logIcon.equals("icon_attended.gif")) && 
						  (logFinder.equalsIgnoreCase(pref.myAlias) || (pref.myAlias2.length()>0 && logFinder.equalsIgnoreCase(pref.myAlias2)))) {
							holder.setCacheStatus(logDate);
							holder.setFound(true);
							holder.getCacheDetails(false).OwnLogId = logId;
							holder.getCacheDetails(false).OwnLog = new Log(logIcon,logDate,logFinder,logData);
				}
				return;
			}
		}
		
		if (name.equals("wpt")){
			
			int index=cacheDB.getIndex(holder.getWayPoint());
			if (index == -1){// Add cache Data only, if waypoint not already in database
				holder.setNoFindLogs(holder.getCacheDetails(false).CacheLogs.countNotFoundLogs());
				holder.setNew(true);
				cacheDB.add(holder);
				// don't spider additional waypoints, so check
				// if waypoint starts with "GC"
				if(doSpider) {
					if(spiderOK && holder.is_archived() == false){
							if(holder.getLatLon().length() > 1){
						}
						if(holder.getWayPoint().startsWith("GC")|| fromTC) {
							//spiderImages();
							spiderImagesUsingSpider();
							//Rename image sources
							String text;
							String orig;
							String imgName;
							orig = holder.getCacheDetails(false).LongDescription;
							Extractor ex = new Extractor(orig, "<img src=\"", ">", 0, false);
							text = ex.findNext();
							int num = 0;
							while(ex.endOfSearch() == false && spiderOK){
								//Vm.debug("Replacing: " + text);
								if (num >= holder.getCacheDetails(false).images.size())break;
								imgName = holder.getCacheDetails(false).images.get(num).getTitle();
								holder.getCacheDetails(false).LongDescription = replace(holder.getCacheDetails(false).LongDescription, text, "[[Image: " + imgName + "]]");
								num++;
								text = ex.findNext();
							}
						}
					}
				}
				holder.save();
				//crw.saveIndex(cacheDB,saveDir);
			}
			//Update cache data
			else {
				CacheHolder oldCh= cacheDB.get(index);
				// Preserve images: Copy images from old cache version because here we didn't add
				// any image information to the holder object.
				holder.getCacheDetails(false).images = oldCh.getCacheDetails(true).images;
				oldCh.initStates(false);
				oldCh.update(holder);
				oldCh.save();
			}
			
			inWpt = false;
			return;
		}
		if (name.equals("sym")&& strData.endsWith("Found")) {
			holder.setFound(true);
			holder.setCacheStatus(MyLocale.getMsg(318,"Found"));
			return;
		}
		if (name.equals("groundspeak:travelbugs")) {
			inBug = false;
			return;
		}

		if (name.equals("groundspeak:name")&& inBug) {
			Travelbug tb=new Travelbug(strData);
			holder.getCacheDetails(false).Travelbugs.add(tb);
			holder.setHas_bugs(true);
			return;
		}
		
		if (name.equals("time") && !inWpt) {		    
			try {
			    gpxDate.parse(strData.substring(0,19),"yyyy-MM-dd'T'HH:mm:ss");
			} catch (IllegalArgumentException e) {
			    gpxDate.setTime(0);
			    Global.getPref().log("Error parsing date: '"+strData+"'. Ignoring.");
			}
			return;
		}

		if (name.equals("time") && inWpt) {
			holder.setDateHidden(strData.substring(0,10)); //Date;
			return;
		}
		// cache information
		if (name.equals("groundspeak:cache") || name.equals("geocache")|| name.equals("cache")|| name.equals("terra:terracache")) {
			inCache = false;
		}
		
		if (name.equals("name") && inWpt && !inCache) {
			holder.setWayPoint(strData);
			if (gpxDate.getTime()!=0) {
			    holder.setLastSync(gpxDate.format("yyyyMMddHHmmss"));
			} else {
			    holder.setLastSync("");
			}    
			//msgA.setText("import " + strData);
			return;
		}
		//Vm.debug("Check: " + inWpt + " / " + fromOC);

		// fill name with contents of <desc>, in case of gc.com the name is
		// later replaced by the contents of <groundspeak:name> which is shorter
		if (name.equals("desc")&& inWpt ) {
			holder.setCacheName(strData);
			//Vm.debug("CacheName: " + strData);
			//msgA.setText("import " + strData);
			return;
		}
		if (name.equals("url")&& inWpt){
			holder.getCacheDetails(false).URL = strData;
			return;
		}
		
		// Text for additional waypoints, no HTML
		if (name.equals("cmt")&& inWpt){
			holder.getCacheDetails(false).LongDescription = strData;
			holder.setHTML(false);
			return;
		}
		
		// aditional wapypoint
		if (name.equals("type")&& inWpt && !inCache && strData.startsWith("Waypoint")){
			holder.setType(CacheType.gpxType2CwType(strData));
			holder.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
			holder.setHard(CacheTerrDiff.CW_DT_UNSET);
			holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
			holder.setLastSync("");
		}
		
		if (name.indexOf("name")>-1 && inCache) {
			holder.setCacheName(strData);
			return;
		}
		if (name.equals("groundspeak:owner") || name.equals("owner")||name.equals("terra:owner")) {
			holder.setCacheOwner(strData);
			if(pref.myAlias.equals(strData)) holder.setOwned(true);
			return;
		}
		if (name.equals("groundspeak:difficulty") || name.equals("difficulty") || name.equals("terra:mental_challenge")) {
			holder.setHard(CacheTerrDiff.v1Converter(strData));
			return;
		}
		if (name.equals("groundspeak:terrain")|| name.equals("terrain")|| name.equals("terra:physical_challenge")) {
			holder.setTerrain(CacheTerrDiff.v1Converter(strData));
			return;
		}
		if ((name.equals("groundspeak:type") || name.equals("type")|| name.equals("terra:style"))&& inCache){
			holder.setType(CacheType.gpxType2CwType(strData));
			if (holder.isCustomWpt()) {
				holder.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				holder.setHard(CacheTerrDiff.CW_DT_UNSET);
				holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
			}
			return;
		}
		if (name.equals("groundspeak:container")|| name.equals("container")){
			holder.setCacheSize(CacheSize.gcGpxString2Cw(strData));
			return;
		}
		if (name.equals("groundspeak:country")|| name.equals("country")){
			holder.getCacheDetails(false).Country = strData;
			return;
		}
		if (name.equals("groundspeak:state")|| name.equals("state")){
			holder.getCacheDetails(false).State = strData;
			return;
		}
		if (name.equals("terra:size")){
			holder.setCacheSize(CacheSize.tcGpxString2Cw(strData));
		}

		if (name.indexOf("short_description") > -1|| name.equals("summary")) {
			if (holder.is_HTML())	holder.getCacheDetails(false).LongDescription =SafeXML.cleanback(strData)+"<br>"; // <br> needed because we also use a <br> in SpiderGC. Without it the comparison in ch.update fails
			else holder.getCacheDetails(false).LongDescription =strData+"\n";
			return;
		}

		if (name.indexOf("long_description") > -1 || name.equals("description")|| name.equals("terra:description")) {
			if (holder.is_HTML())	holder.getCacheDetails(false).LongDescription +=SafeXML.cleanback(strData);
			else holder.getCacheDetails(false).LongDescription +=strData;
			return;
		}
		if (name.indexOf("encoded_hints") > -1 || name.equals("hints")) {
			holder.getCacheDetails(false).Hints = Common.rot13(strData);
			return;
		}
		
		if (name.equals("terra:hint")) {
			// remove "&lt;br&gt;<br>" from the end
			int indexTrash = strData.indexOf("&lt;br&gt;<br>");
			if (indexTrash > 0)	holder.getCacheDetails(false).Hints = Common.rot13(strData.substring(0,indexTrash));
			return;
		}


	}
	public void characters(char[] ch,int start,int length){
		strBuf.append(ch,start,length);
		if (debugGPX) Vm.debug("Char: " + strBuf.toString());
	}
	
	// if you change any of these make sure to check image2TypeText in the GPX exporters
	public static String typeText2Image(String typeText){
		if (typeText.equals("Found it")||typeText.equals("Found")||typeText.equals("find")) return "icon_smile.gif";
		if (typeText.equals("Didn't find it")||typeText.equals("Not Found")||typeText.equals("no_find")) return "icon_sad.gif";
		if (typeText.equals("Write note")||typeText.equals("Note")||typeText.equals("note")
			||typeText.equals("Not Attempted")||typeText.equals("Other")) return "icon_note.gif";
		if (typeText.equals("Enable Listing")) return "icon_enabled.gif";
		if (typeText.equals("Temporarily Disable Listing")) return "icon_disabled.gif";
		if (typeText.equals("Webcam Photo Taken")) return "icon_camera.gif";
		if (typeText.equals("Attended")) return "icon_attended.gif";
		if (typeText.equals("Publish Listing")) return "icon_greenlight.gif";
		if (typeText.equals("Will Attend")) return "icon_rsvp.gif";
		if (typeText.equals("Post Reviewer Note")) return "big_smile.gif";
		if (typeText.equals("Unarchive")) return "traffic_cone.gif";
		if (typeText.equals("Archive")) return "traffic_cone.gif";
		if (typeText.equals("Owner Maintenance")) return "icon_maint.gif";
		if (typeText.equals("Needs Maintenance")) return "icon_needsmaint.gif";
		if (typeText.equals("Needs Archived")) return "icon_remove.gif";
		if (typeText.equals("Update Coordinates")) return "coord_update.gif";
		if (typeText.equals("Retract Listing")) return "img_redlight.gif";
		Global.getPref().log("GPX Import: warning, unknown logtype "+typeText+" assuming Write note");
		return "icon_note.gif";
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
	
	private void spiderImagesUsingSpider(){
		String addr;
		String cacheText;
		
		// just to be sure to have a spider object
		if (imgSpider == null) imgSpider = new SpiderGC(pref, profile, false);
		
		if (fromTC) {
				imgSpider.getImages(holder.getCacheDetails(false).LongDescription, holder.getCacheDetails(false));
		}
		else {
			addr = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + holder.getWayPoint() ;
			//Vm.debug(addr + "|");
			cacheText = SpiderGC.fetch(addr);
			imgSpider.getImages(cacheText, holder.getCacheDetails(false));
			try {
				imgSpider.getAttributes(cacheText, holder.getCacheDetails(false));
			} catch (Exception e) {
				if (Global.getPref().debug) Global.getPref().log("unable to fetch attrivbutes for"+holder.getWayPoint(), e);
			}
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
	
	public int getHow() {
		return doitHow;
	}
}
