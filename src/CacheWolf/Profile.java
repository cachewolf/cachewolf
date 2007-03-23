package CacheWolf;

import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.io.Stream;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.LocalResource;
import ewe.sys.Locale;
import ewe.sys.Vm;
import ewe.ui.*;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * This class holds a profile, i.e. a group of caches with a centre location
 * 
 * @author salzkammergut
 *
 */
public class Profile {

	/** The list of caches (CacheHolder objects). A pointer to this object exists in many classes in parallel to
	 *  this object, i.e. the respective class contains both a {@link Profile} object and a cacheDB Vector. 
	 */
	public Vector cacheDB=new Vector();
	/** The centre point of this group of caches. Read from ans stored to index.xml file */
	public CWPoint centre=new CWPoint();
	/** The name of the profile. The baseDir in preferences is appended this name to give the dataDir where
	 *  the index.xml and cache files live. (Excuse the English spelling of centre)     */
	public String name=new String();
	/** This is the directory for the profile. It contains a closing /.   	 */
	public String dataDir=new String();  
	/** Last sync date for opencaching caches */
	public String last_sync_opencaching = new String();
	/** Distance for opencaching caches */
	public String distOC = new String();

	public final static boolean SHOW_PROGRESS_BAR = true;
	public final static boolean NO_SHOW_PROGRESS_BAR = false;

	// When extending the filter check "normalizeFilters"
	// which ensures backward compatibility. Normally no change should be needed
	public final static String FILTERTYPE="111111111111111111";
	public final static String FILTERROSE="1111111111111111";
	public final static String FILTERVAR="11111111";
	public final static String FILTERSIZE="111111";
	public String filterType = new String(FILTERTYPE);
	public String filterRose = new String(FILTERROSE);
	public String filterSize = new String(FILTERSIZE);
	//filter settings for archived ... owner (section) in filterscreen
	public String filterVar = new String(FILTERVAR);
	public String filterDist=new String("L");
	public String filterDiff=new String("L");
	public String filterTerr=new String("L");
	// Saved filterstatus - is only refreshed from class Filter when Profile is saved
	public boolean filterActive=false;
	public boolean filterInverted=false;

	public boolean selectionChanged = true; // ("Häckchen") used by movingMap to get to knao if it should update the caches in the map 
	/** True if the profile has been modified and not saved
	 * The following modifications set this flag: New profile centre, Change of waypoint data 
	 */
	public boolean hasUnsavedChanges = false;

	//TODO Add other settings, such as max. number of logs to spider
	//TODO Add settings for the preferred mapper to allow for maps other than expedia and other resolutions

	/**
	 * Constructor for a profile
	 *
	 */
	public Profile(){
	}

	public void clearProfile() {
		cacheDB.clear();
		centre.set(-361,-361);
		name="";
		dataDir="";  
		last_sync_opencaching = "";
		distOC = "";
		hasUnsavedChanges=false;
	}

	/**
	 *	Method to save the index.xml file that holds the total information
	 *	on available caches in the database. The database is nothing else
	 *	than the collection of caches in a directory.
	 *   
	 *   Not sure whether we need to keep 'pref' in method signature. May eventually remove it. 
	 *   
	 *   Saves the index with the filter settings from Filter
	 */
	public void saveIndex(Preferences pref, boolean showprogress){
		saveIndex(pref,showprogress, Filter.filterActive,Filter.filterInverted);
	}

	/**
	 * Method to write the header for the index file.
	 * Should be used when memory becomes critical and storage
	 * in the cachedb vector poses a problem (see spiderGC)
	 *
	 */
	public void openIndex(Preferences pref){
		boolean saveFilterActive = Filter.filterActive;
		boolean saveFilterInverted = Filter.filterInverted;
		PrintWriter detfile;
		try{
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(dataDir + "index.xml")));
		} catch (Exception e) {
			Vm.debug("Problem creating index file "+e.toString()+"\nFilename="+dataDir + "index.xml");
			return;
		}
		CWPoint savedCentre=centre;
		if (centre==null || !centre.isValid() || (savedCentre.latDec==0.0 && savedCentre.lonDec==0.0)) savedCentre=pref.curCentrePt;
		try{
			detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			detfile.print("<CACHELIST format=\"decimal\">\n");
			if (savedCentre.isValid())
				detfile.print("    <CENTRE lat=\""+savedCentre.latDec+"\" lon=\""+savedCentre.lonDec+"\"/>\n");
			if(last_sync_opencaching == null || last_sync_opencaching.endsWith("null") || last_sync_opencaching.equals("")){
				last_sync_opencaching = "20050801000000";
			}
			if(distOC == null || distOC.endsWith("null") || distOC.equals("")){
				distOC = "0";
			}

			detfile.print("    <FILTER status = \""+(saveFilterActive?"T":"F")+(saveFilterInverted?"T":"F")+ 
					"\" rose = \""+filterRose+"\" type = \""+filterType+
					"\" var = \""+filterVar+"\" dist = \""+filterDist.replace('"',' ')+"\" diff = \""+
					filterDiff+"\" terr = \""+filterTerr+"\" size = \""+filterSize+"\" />\n");
			detfile.print("    <SYNCOC date = \""+last_sync_opencaching+"\" dist = \""+distOC+"\"/>\n");
			detfile.close();
		}catch(Exception e){
			Vm.debug("Problem writing to index file "+e.toString());
			detfile.close();
		}
	}
	
	/**
	 * Method to appen a single line of cachedate to the index file
	 * @param ch
	 */
	public void writeIndexLine(CacheHolder ch){
		Stream strout = null;
		File index = new File(dataDir + "index.xml");
		String cachedata = "    <CACHE name = \""+SafeXML.clean(ch.CacheName)+"\" owner = \""+SafeXML.clean(ch.CacheOwner)+
				"\" lat = \""+ ch.pos.latDec + "\" lon = \""+ch.pos.lonDec+
				"\" hidden = \""+ch.DateHidden+"\" wayp = \""+SafeXML.clean(ch.wayPoint)+"\" status = \""+ch.CacheStatus+"\" type = \""+ch.type+"\" dif = \""+ch.hard+"\" terrain = \"" + ch.terrain + "\" dirty = \"" + ch.dirty + "\" size = \""+ch.CacheSize+"\" online = \"" + Convert.toString(ch.is_available) + "\" archived = \"" + Convert.toString(ch.is_archived) + "\" has_bug = \"" + Convert.toString(ch.has_bug) + "\" black = \"" + Convert.toString(ch.is_black) + "\" owned = \"" + Convert.toString(ch.is_owned) + "\" found = \"" + Convert.toString(ch.is_found) + "\" is_new = \"" + Convert.toString(ch.is_new) +"\" is_log_update = \"" + Convert.toString(ch.is_log_update) + "\" is_update = \"" + Convert.toString(ch.is_update) + "\" is_HTML = \"" + Convert.toString(ch.is_HTML) + "\" DNFLOGS = \"" + ch.noFindLogs + "\" ocCacheID = \"" + ch.ocCacheID + "\" is_INCOMPLETE = \""+Convert.toString(ch.is_incomplete)+"\" />\n";
		try{
			//append data!
			strout = index.toWritableStream(true);
			byte[] data = cachedata.getBytes(); 
			strout.write(data);
		}catch(Exception ex){}
		finally{
			strout.close();
		}
	}
	
	/**
	 * Use this method to "finalize" the index file when using openIndex()
	 * and writeIndexLine()
	 *
	 */
	public void closeIndex(){
		Stream strout = null;
		File index = new File(dataDir + "index.xml");
		try{
			//append data!
			strout = index.toWritableStream(true);
			byte[] data = "</CACHELIST>\n".getBytes(); 
			strout.write(data);
			strout.close();
		}catch(Exception ex){};
	}
	
	/** Save index with filter settings given */ 
	public void saveIndex(Preferences pref, boolean showprogress, boolean saveFilterActive, boolean saveFilterInverted){
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		if(showprogress){
			pbf.showMainTask = false;
			pbf.setTask(h,"Saving Index");
			pbf.exec();
		}
		PrintWriter detfile;
		CacheHolder ch;
		try{
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(dataDir + "index.xml")));
		} catch (Exception e) {
			Vm.debug("Problem creating index file "+e.toString()+"\nFilename="+dataDir + "index.xml");
			return;
		}
		CWPoint savedCentre=centre;
		if (centre==null || !centre.isValid() || (savedCentre.latDec==0.0 && savedCentre.lonDec==0.0)) savedCentre=pref.curCentrePt;

		try{
			detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			detfile.print("<CACHELIST format=\"decimal\">\n");
			if (savedCentre.isValid())
//				detfile.print("    <CENTRE lat=\""+savedCentre.getNSLetter() + " " + savedCentre.getLatDeg(CWPoint.CW) + "&deg; " + savedCentre.getLatMin(CWPoint.CW)+ "\" "+
//				"long=\""+savedCentre.getEWLetter() + " " + savedCentre.getLonDeg(CWPoint.CW) + "&deg; " + savedCentre.getLonMin(CWPoint.CW)+"\"/>\n");
				detfile.print("    <CENTRE lat=\""+savedCentre.latDec+"\" lon=\""+savedCentre.lonDec+"\"/>\n");
			if(last_sync_opencaching == null || last_sync_opencaching.endsWith("null") || last_sync_opencaching.equals("")){
				last_sync_opencaching = "20050801000000";
			}
			if(distOC == null || distOC.endsWith("null") || distOC.equals("")){
				distOC = "0";
			}

			detfile.print("    <FILTER status = \""+(saveFilterActive?"T":"F")+(saveFilterInverted?"T":"F")+ 
					"\" rose = \""+filterRose+"\" type = \""+filterType+
					"\" var = \""+filterVar+"\" dist = \""+filterDist.replace('"',' ')+"\" diff = \""+
					filterDiff+"\" terr = \""+filterTerr+"\" size = \""+filterSize+"\" />\n");
			detfile.print("    <SYNCOC date = \""+last_sync_opencaching+"\" dist = \""+distOC+"\"/>\n");
			int size=cacheDB.size();
			for(int i = 0; i<size;i++){
				if(showprogress){
					h.progress = (float)i/(float)size;
					h.changed();
				}
				ch = (CacheHolder)cacheDB.get(i);
				////Vm.debug("Saving: " + ch.CacheName);
				if(ch.wayPoint.length()>0 && ch.LongDescription.equals("An Error Has Occured") == false){
					detfile.print("    <CACHE name = \""+SafeXML.clean(ch.CacheName)+"\" owner = \""+SafeXML.clean(ch.CacheOwner)+
							//"\" lat = \""+ SafeXML.clean(ch.LatLon) +
							"\" lat = \""+ ch.pos.latDec + "\" lon = \""+ch.pos.lonDec+
							"\" hidden = \""+ch.DateHidden+"\" wayp = \""+SafeXML.clean(ch.wayPoint)+"\" status = \""+ch.CacheStatus+"\" type = \""+ch.type+"\" dif = \""+ch.hard+"\" terrain = \"" + ch.terrain + "\" dirty = \"" + ch.dirty + "\" size = \""+ch.CacheSize+"\" online = \"" + Convert.toString(ch.is_available) + "\" archived = \"" + Convert.toString(ch.is_archived) + "\" has_bug = \"" + Convert.toString(ch.has_bug) + "\" black = \"" + Convert.toString(ch.is_black) + "\" owned = \"" + Convert.toString(ch.is_owned) + "\" found = \"" + Convert.toString(ch.is_found) + "\" is_new = \"" + Convert.toString(ch.is_new) +"\" is_log_update = \"" + Convert.toString(ch.is_log_update) + "\" is_update = \"" + Convert.toString(ch.is_update) + "\" is_HTML = \"" + Convert.toString(ch.is_HTML) + "\" DNFLOGS = \"" + ch.noFindLogs + "\" ocCacheID = \"" + ch.ocCacheID + "\" is_INCOMPLETE = \""+Convert.toString(ch.is_incomplete)+"\" />\n");
				}
			}
			detfile.print("</CACHELIST>\n");
			detfile.close();
			buildReferences(); //TODO Why is this needed here?
			if(showprogress) pbf.exit(0);
		}catch(Exception e){
			Vm.debug("Problem writing to index file "+e.toString());
			detfile.close();
			if(showprogress) pbf.exit(0);
		}
		hasUnsavedChanges=false;
	}

	/**
	 *	Method to read the index.xml file that holds the total information
	 *	on available caches in the database. The database in nothing else
	 *	than the collection of caches in a directory.
	 */
	public void readIndex(){
		try {
			selectionChanged = true;
			boolean fmtDec=false;
			char decSep=MyLocale.getDigSeparator().charAt(0);
			char notDecSep=decSep=='.'?',':'.';
			FileReader in = new FileReader(dataDir + "index.xml");
			in.readLine(); // <?xml version= ...
			String text=in.readLine(); // <CACHELIST>
			if (text!=null && text.indexOf("decimal")>0) fmtDec=true;
			Extractor ex = new Extractor(null, " = \"", "\" ", 0, true);
			while ((text = in.readLine()) != null){
				// Check for Line with cache data
				if (text.indexOf("<CACHE ")>=0){
					ex.setSource(text);
					CacheHolder ch = new CacheHolder();
					ch.CacheName = SafeXML.cleanback(ex.findNext());
					ch.CacheOwner = SafeXML.cleanback(ex.findNext());
					if (fmtDec) {
						double lat=Convert.parseDouble(ex.findNext().replace(notDecSep,decSep));
						double lon=Convert.parseDouble(ex.findNext().replace(notDecSep,decSep));
						ch.pos=new CWPoint(lat,lon);
						ch.LatLon=ch.pos.toString();
					} else {
						ch.LatLon = SafeXML.cleanback(ex.findNext());
						ch.pos.set(ch.LatLon,CWPoint.CW);
					}
					ch.DateHidden = ex.findNext();
					ch.wayPoint = SafeXML.cleanback(ex.findNext());
					ch.CacheStatus = ex.findNext();
					ch.type = ex.findNext();
					ch.hard = ex.findNext();
					ch.terrain = ex.findNext();
					ch.dirty = ex.findNext();
					ch.CacheSize = ex.findNext();
					ch.is_available = ex.findNext().equals("true") ? true : false;
					ch.is_archived = ex.findNext().equals("true") ? true : false;
					ch.has_bug = ex.findNext().equals("true") ? true : false;
					ch.is_black = ex.findNext().equals("true") ? true : false;
					if(ch.is_black) ch.is_filtered = true;
					ch.is_owned = ex.findNext().equals("true") ? true : false;
					ch.is_found = ex.findNext().equals("true") ? true : false;
					ch.is_new = ex.findNext().equals("true") ? true : false;
					ch.is_log_update = ex.findNext().equals("true") ? true : false;
					ch.is_update = ex.findNext().equals("true") ? true : false;
					// for backwards compatibility set value to true, if it is not in the file
					ch.is_HTML = ex.findNext().equals("false") ? false : true;
					ch.noFindLogs = Convert.toInt(ex.findNext());
					ch.ocCacheID = ex.findNext();
					ch.is_incomplete = ex.findNext().equals("true") ? true : false;
					// remove "/>
					ch.ocCacheID = STRreplace.replace(ch.ocCacheID,"\"/>", null);
					// remove additional " if present
					ch.ocCacheID = STRreplace.replace(ch.ocCacheID,"\"", null);
					cacheDB.add(ch);
				} else if (text.indexOf("<CENTRE")>=0) { // lat=  lon=
					if (fmtDec) {
						int start=text.indexOf("lat=\"")+5;
						String lat=text.substring(start,text.indexOf("\"",start)).replace(notDecSep,decSep);
						start=text.indexOf("lon=\"")+5;
						String lon=text.substring(start,text.indexOf("\"",start)).replace(notDecSep,decSep);
						centre.set(Convert.parseDouble(lat),Convert.parseDouble(lon));
					} else {	
						int start=text.indexOf("lat=\"")+5;
						String lat=SafeXML.cleanback(text.substring(start,text.indexOf("\"",start)));
						start=text.indexOf("long=\"")+6;
						String lon=SafeXML.cleanback(text.substring(start,text.indexOf("\"",start)));
						centre.set(lat+" "+lon,CWPoint.CW); // Fast parse
					}	
				} else if (text.indexOf("<SYNCOC")>=0) {
					int start=text.indexOf("date = \"")+8;
					last_sync_opencaching=text.substring(start,text.indexOf("\"",start));
					start=text.indexOf("dist = \"")+8;
					distOC=text.substring(start,text.indexOf("\"",start));
				} else if (text.indexOf("<FILTER")>=0){
					ex.setSource(text);
					String temp=ex.findNext(); // Filter status is now first, need to deal with old versions which don't have filter status
					if (temp.length()==2) {
						filterActive=temp.charAt(0)=='T';
						filterInverted=temp.charAt(1)=='T';
						filterRose = ex.findNext();
					} else 
						filterRose = temp;
					filterType = ex.findNext();
					//Need this to stay "downward" compatible. New type introduced
					//if(filterType.length()<=17) filterType = filterType + "1";
					//Vm.debug("fil len: " +filterType.length());
					//This is handled by "normalizeFilters" which is called at the end.
					filterVar = ex.findNext();
					filterDist = ex.findNext();
					filterDiff = ex.findNext();
					filterTerr = ex.findNext();
					filterSize = ex.findNext();
				}
			}
			in.close();
			// Build references between caches and addi wpts
			buildReferences();
		} catch (FileNotFoundException e) {
			Global.getPref().log("index.xml not found in directory "+dataDir); // Normal when profile is opened for first time
			//e.printStackTrace();
		} catch (IOException e){
			Global.getPref().log("Problem reading index.xml in dir: "+dataDir,e,true); 
		}
		normalizeFilters();
		hasUnsavedChanges=false;
	}

	/** Restore the filter to the values stored in this profile 
	 *  Called from Main Form and MainMenu 
	 *  The values of Filter.isActive and Filter.isInactive are set by the filter 
	 **/
	void restoreFilter() {
		Filter flt=new Filter();
		if (filterActive) {
			flt.setFilter();
			flt.doFilter();
		}
		if (filterInverted) 
			flt.invertFilter();
	}

	public int getCacheIndex(String wp){
		int retval = -1;
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.wayPoint.equals(wp)){
				return i;
			}
		}
		return retval;
	}

	/** Get a unique name for a new waypoint */
	//TODO Make more efficient
	public String getNewWayPointName(){
		String strWp=null;
		long  lgWp=1;
		if (cacheDB.size()==0 )
			return "CW0000";
		//Create new waypoint,look if not in db
		for(int i = 0;i < cacheDB.size();i++){
			strWp = "CW" + MyLocale.formatLong(lgWp, "0000");
			if(((CacheHolder)cacheDB.get(i)).wayPoint.indexOf(strWp) >=0 ){
				//waypoint exists in database
				lgWp++;
				i = -1; // Because i++ will be executed next, so we start the loop with 0
			}
		}
		return strWp;
	}

	public String toString() {
		return "Profile: Name="+name+"\nCentre="+centre.toString()+"\ndataDir="+dataDir+"\nlastSyncOC="+
		last_sync_opencaching+"\ndistOC="+distOC;
	}

	public void setSelectForAll(boolean selectStatus) {
		selectionChanged = true;
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i >=	0; i--){
			ch = (CacheHolder)cacheDB.get(i);
			if (ch.is_filtered == false) ch.is_Checked = selectStatus;
		}
	} 


	int numCachesInArea; // only valid after calling getSourroundingArea
	public Area getSourroundingArea(boolean onlyOfSelected) {
		if (cacheDB == null || cacheDB.size() == 0) return null;
		CacheHolder ch;
		CWPoint topleft = null;
		CWPoint bottomright = null;
		CWPoint tmpca = new CWPoint();
		numCachesInArea = 0;
		boolean isAddi = false;
		for (int i=cacheDB.size()-1; i >= 0; i--) {
			ch = (CacheHolder) cacheDB.get(i);
			if (!onlyOfSelected || ch.is_Checked) {
				if (ch.pos == null) { // this can not happen
					tmpca.set(ch.LatLon);
					ch.pos = new CWPoint(tmpca);
				}
				if (ch.pos.isValid() ){ // done: && ch.pos.latDec != 0 && ch.pos.lonDec != 0 TO-DO != 0 sollte rausgenommen werden sobald in der Liste vernünftig mit nicht gesetzten pos umgegangen wird
					isAddi = ch.isAddiWpt();
				if (!isAddi || (isAddi && ch.mainCache != null && ch.pos.getDistance(ch.mainCache.pos) < 1000)) { // test for plausiblity of coordinates of Additional Waypoints: more then 1000 km away from main Waypoint is unplausible -> ignore it // && ch.mainCache != null is only necessary because the data base may be corrupted
						if (topleft == null) topleft = new CWPoint(ch.pos);
						if (bottomright == null) bottomright = new CWPoint(ch.pos);
						if (topleft.latDec < ch.pos.latDec) topleft.latDec = ch.pos.latDec;
						if (topleft.lonDec > ch.pos.lonDec) topleft.lonDec = ch.pos.lonDec;
						if (bottomright.latDec > ch.pos.latDec) bottomright.latDec = ch.pos.latDec;
						if (bottomright.lonDec < ch.pos.lonDec) bottomright.lonDec = ch.pos.lonDec;
						numCachesInArea++;
					}
				}
			}
		}
		if (topleft != null && bottomright != null) 
			return new Area(topleft, bottomright);
		else return null;
	}

	/**
	 *	Method to calculate bearing and distance of a cache in the index
	 *	list.
	 *	@see	CacheHolder
	 *	@see	Extractor
	 */
	public void updateBearingDistance(){
		CWPoint centerPoint = new CWPoint(Global.getPref().curCentrePt); // Clone current centre to be sure
		int anz = cacheDB.getCount();
		CacheHolder ch;
		// Jetzt durch die CacheDaten schleifen
		while(--anz >= 0){
			ch = (CacheHolder)cacheDB.get(anz); // This returns a pointer to the CacheHolder object
			ch.calcDistance(centerPoint);
		}
		// The following call is not very clean as it mixes UI with base classes
		// However, calling it from here allows us to recenter the
		// radar panel with only one call
		if (Global.mainTab!=null) Global.mainTab.radarP.recenterRadar();
	} //updateBearingDistance

	/**
	 * Method to build the reference between addi wpt
	 * and main cache.
	 */
	public void buildReferences(){
		CacheHolder ch, mainCh;
		Hashtable dbIndex = new Hashtable((int)(cacheDB.size()/0.75f + 1), 0.75f); // initialize so that von rehashing is neccessary

		Integer index;
		// Build index for faster search and clear all references
		for(int i = cacheDB.size() -1; i >= 0;i--){
			ch = (CacheHolder)cacheDB.get(i);
			ch.addiWpts.clear();
			ch.mainCache = null;
			if (ch.wayPoint.startsWith("GC")) // Only put potential master caches into the index
				dbIndex.put((String)ch.wayPoint, new Integer(i));
		}
		// Build references
		int max = cacheDB.size();
		for(int i =  0; i < max ;i++){
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
					ch.setAttributesFromMainCache(mainCh);
				}// if
			}// if
		}// for
		// sort addi wpts
		for(int i =  0; i < max ;i++){
			ch = (CacheHolder)cacheDB.get(i);
			if (ch.hasAddiWpt() && (ch.addiWpts.size()> 1)){
				//ch.addiWpts.sort(new MyComparer(ch.addiWpts,MyLocale.getMsg(1002,"Waypoint"),ch.addiWpts.size()), false);
				ch.addiWpts.sort(
						new ewe.util.Comparer() {	
							public int compare(Object o1, Object o2){
								return ((CacheHolder) o1).wayPoint.compareTo(((CacheHolder)o2).wayPoint);
							}
						},false );
			}
		}

	}

	/** Ensure that all filters have the proper length so that the 'charAt' access in the filter
	 * do not cause nullPointer Exceptions
	 */
	private void normalizeFilters() {
		String manyOnes="11111111111111111111111111111";
		if (filterRose.length()<FILTERROSE.length()) { 
			filterRose=(filterRose+manyOnes).substring(0,FILTERROSE.length()); 
		}  
		if (filterVar.length()<FILTERVAR.length()) { 
			filterVar=(filterVar+manyOnes).substring(0,FILTERVAR.length()); 
		}  
		if (filterType.length()<FILTERTYPE.length()) { 
			filterType=(filterType+manyOnes).substring(0,FILTERTYPE.length());
		} 
		if (filterSize.length()<FILTERSIZE.length()) {
			filterSize=(filterSize+manyOnes).substring(0,FILTERSIZE.length());
		}
		if (filterDist.length()==0) filterDist="L";
		if (filterDiff.length()==0) filterDiff="L";
		if (filterTerr.length()==0) filterTerr="L";
	}

}
