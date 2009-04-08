package CacheWolf;

import CacheWolf.navi.Area;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.ui.ProgressBarForm;
import ewe.util.*;

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
	public CacheDB cacheDB=new CacheDB();
	/** The centre point of this group of caches. Read from ans stored to index.xml file */
	public CWPoint centre=new CWPoint();
	/** The name of the profile. The baseDir in preferences is appended this name to give the dataDir where
	 *  the index.xml and cache files live. (Excuse the English spelling of centre)     */
	public String name=new String();
	/** This is the directory for the profile. It contains a closing /.   	 */
	public String dataDir = new String();

	/** Last sync date for opencaching caches */
	private String last_sync_opencaching = new String();

	/** Distance for opencaching caches */
	private String distOC = new String();

	/** Distance for geocaching caches */
	private String distGC = new String();

	public final static boolean SHOW_PROGRESS_BAR = true;
	public final static boolean NO_SHOW_PROGRESS_BAR = false;

	// When extending the filter check "normaliseFilters"
	// which ensures backward compatibility. Normally no change should be needed
	public final static String FILTERTYPE = "1111111111111111111";
	public final static String FILTERROSE = "1111111111111111";
	public final static String FILTERVAR = "11111111";
	public final static String FILTERSIZE = "111111";

	private String filterType = new String(FILTERTYPE);
	private String filterRose = new String(FILTERROSE);
	private String filterSize = new String(FILTERSIZE);

	// filter settings for archived ... owner (section) in filterscreen
	private String filterVar = new String(FILTERVAR);
	private String filterDist = new String("L");
	private String filterDiff = new String("L");
	private String filterTerr = new String("L");

	// Saved filterstatus - is only refreshed from class Filter when Profile is saved
	private int filterActive = Filter.FILTER_INACTIVE;
	private boolean filterInverted = false;
	private boolean showBlacklisted = false;

	private long filterAttrYes = 0l;
	private long filterAttrNo = 0l;
	private int filterAttrChoice = 0;

	public boolean selectionChanged = true; // ("Häckchen") used by movingMap to get to knao if it should update the caches in the map 
	/** True if the profile has been modified and not saved
	 * The following modifications set this flag: New profile centre, Change of waypoint data 
	 */
	private boolean hasUnsavedChanges = false;
	public boolean byPassIndexActive = false;

	//TODO Add other settings, such as max. number of logs to spider
	//TODO Add settings for the preferred mapper to allow for maps other than expedia and other resolutions

	/**
	 * Constructor for a profile
	 *
	 */
	public Profile(){
	}


	/**
	 * Returns <code>true</code> if profile needs to be changed when profile is left. Returns
	 * <code>false</code> if no relevant changes have been made.
	 * 
	 * @return hasUnsavedChanges
	 */
	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}

	/**
	 * Remember that profile needs to be saved. Flag is set <code>true</code> when parameter is
	 * true, but it's not set to <code>false</code> when parameter is <code>false</code>.<br>
	 * This is only done internally on saving the cache.
	 * 
	 * @param hasUnsavedChanges
	 *            the hasUnsavedChanges to set
	 */
	public void notifyUnsavedChanges(boolean changes) {
		hasUnsavedChanges = hasUnsavedChanges || changes;
	}
	
	public void resetUnsavedChanges() {
		hasUnsavedChanges = false;
	}

	public void clearProfile() {
		CacheHolder.removeAllDetails();
		cacheDB.clear();
		centre.set(-361, -361);
		name = "";
		dataDir = "";
		setLast_sync_opencaching("");
		setDistOC("");
		setDistGC("");
		resetUnsavedChanges();
	}

	public void setCenterCoords(CWPoint coords) {
		this.notifyUnsavedChanges(coords.equals(this.centre));
		this.centre.set(coords);
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
//	public void saveIndex(Preferences pref, boolean showprogress){
//		saveIndex(pref,showprogress, Filter.filterActive,Filter.filterInverted);
//	}

	
	/** Save index with filter settings given */ 
	public void saveIndex(Preferences pref, boolean showprogress) { 
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		if(showprogress){
			pbf.showMainTask = false;
			pbf.setTask(h,"Saving Index");
			pbf.exec();
		}
		CacheHolder.saveAllModifiedDetails(); // this must be called first as it makes some calculations
		PrintWriter detfile;
		CacheHolder ch;
		try {
			File backup=new File(dataDir+"index.bak");
			if (backup.exists()) backup.delete();
			File index=new File(dataDir+"index.xml");
			index.rename("index.bak");
		} catch (Exception ex) {
			pref.log("Error deleting backup or renaming index.xml");
		}
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
			if(getLast_sync_opencaching() == null || getLast_sync_opencaching().endsWith("null") || getLast_sync_opencaching().equals("")){
				setLast_sync_opencaching("20050801000000");
			}
			if (getDistOC() == null || getDistOC().endsWith("null") || getDistOC().equals("")) {
				setDistOC("0.0");
			}
			if (getDistGC() == null || getDistGC().endsWith("null") || getDistGC().equals("")) {
				setDistGC("0.0");
			}

			detfile.print("    <FILTER status = \""+getFilterActive()+(isFilterInverted()?"T":"F")+ 
					"\" rose = \""+getFilterRose()+"\" type = \""+getFilterType()+
					"\" var = \""+getFilterVar()+"\" dist = \""+getFilterDist().replace('"',' ')+"\" diff = \""+
					getFilterDiff()+"\" terr = \""+getFilterTerr()+"\" size = \""+getFilterSize()+"\" attributesYes = \""+getFilterAttrYes()+
					"\" attributesNo = \""+getFilterAttrNo()+"\" attributesChoice = \""+getFilterAttrChoice()+"\" showBlacklist = \""+showBlacklisted()+"\" />\n");
			detfile.print("    <SYNCOC date = \""+getLast_sync_opencaching()+"\" dist = \""+getDistOC()+"\"/>\n");
			detfile.print("    <SPIDERGC dist = \"" + getDistGC() + "\"/>\n");
			int size = cacheDB.size();
			for (int i = 0; i < size; i++) {
				if (showprogress) {
					h.progress = (float) i / (float) size;
					h.changed();
				}
				ch = cacheDB.get(i);
				// //Vm.debug("Saving: " + ch.CacheName);
				if (ch.getWayPoint().length() > 0) { // TODO && ch.LongDescription.equals("An
/*					detfile.print("    <CACHE name = \""+SafeXML.clean(ch.CacheName)+"\" owner = \""+SafeXML.clean(ch.CacheOwner)+
							//"\" lat = \""+ SafeXML.clean(ch.LatLon) +
							"\" lat = \""+ ch.pos.latDec + "\" lon = \""+ch.pos.lonDec+
							"\" hidden = \""+ch.DateHidden+"\" wayp = \""+SafeXML.clean(ch.wayPoint)+"\" status = \""+ch.CacheStatus+"\" type = \""+ch.type+"\" dif = \""+ch.hard+"\" terrain = \"" + ch.terrain + "\" dirty = \"false" + // ch.dirty + dirty is not used, so we save it as false 
							"\" size = \""+ch.CacheSize+"\" online = \"" + Convert.toString(ch.is_available) + "\" archived = \"" + Convert.toString(ch.is_archived) + "\" has_bug = \"" + Convert.toString(ch.has_bug) + "\" black = \"" + Convert.toString(ch.is_black) + "\" owned = \"" + Convert.toString(ch.is_owned) + "\" found = \"" + Convert.toString(ch.is_found) + "\" is_new = \"" + Convert.toString(ch.is_new) +"\" is_log_update = \"" + Convert.toString(ch.is_log_update) + "\" is_update = \"" + Convert.toString(ch.is_update) + "\" is_HTML = \"" + Convert.toString(ch.is_HTML) + "\" DNFLOGS = \"" + ch.noFindLogs + "\" ocCacheID = \"" + ch.ocCacheID + "\" is_INCOMPLETE = \""+Convert.toString(ch.is_incomplete)+ "\" lastSyncOC = \"" + ch.lastSyncOC + "\" />\n");
*/					detfile.print(ch.toXML());
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
		resetUnsavedChanges();
	}

	/**
	 *	Method to read the index.xml file that holds the total information
	 *	on available caches in the database. The database in nothing else
	 *	than the collection of caches in a directory.
	 */
	public void readIndex() {

		try {
			selectionChanged = true;
			boolean fmtDec = false;
			char decSep = MyLocale.getDigSeparator().charAt(0);
			char notDecSep = decSep == '.' ? ',' : '.';
			FileReader in = new FileReader(dataDir + "index.xml");
			in.readLine(); // <?xml version= ...
			String text = in.readLine(); // <CACHELIST>
			if (text!=null && text.indexOf("decimal")>0) fmtDec=true;
			Extractor ex = new Extractor(null, " = \"", "\" ", 0, true);
			
			//ewe.sys.Time startT=new ewe.sys.Time();
			while ((text = in.readLine()) != null){
				// Check for Line with cache data
				if (text.indexOf("<CACHE ")>=0){
					CacheHolder ch=new CacheHolder(text);
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
					setLast_sync_opencaching(text.substring(start,text.indexOf("\"",start)));
					start=text.indexOf("dist = \"")+8;
					setDistOC(text.substring(start,text.indexOf("\"",start)));
				} else if (text.indexOf("<SPIDERGC")>=0) {
					int start=text.indexOf("dist = \"")+8;
					setDistGC(text.substring(start,text.indexOf("\"",start)));
				} else if (text.indexOf("<FILTER")>=0){
					ex.setSource(text);
					String temp=ex.findNext(); // Filter status is now first, need to deal with old versions which don't have filter status
					if (temp.length()==2) {
						// Compatibility with previous versions
						if (temp.charAt(0)=='T') 
							setFilterActive(Filter.FILTER_ACTIVE);
						else
							setFilterActive(Common.parseInt(temp.substring(0,1)));
						setFilterInverted(temp.charAt(1)=='T');
						setFilterRose(ex.findNext());
					} else 
						setFilterRose(temp);
					setFilterType(ex.findNext());
					//Need this to stay "downward" compatible. New type introduced
					//if(filterType.length()<=17) filterType = filterType + "1";
					//Vm.debug("fil len: " +filterType.length());
					//This is handled by "normaliseFilters" which is called at the end.
					setFilterVar(ex.findNext());
					setFilterDist(ex.findNext());
					setFilterDiff(ex.findNext());
					setFilterTerr(ex.findNext());
					setFilterSize(ex.findNext());
					String attr = ex.findNext();
					if (attr != null && !attr.equals(""))
						setFilterAttrYes(Convert.parseLong(attr));
					attr = ex.findNext();
					if (attr != null && !attr.equals(""))
						setFilterAttrNo(Convert.parseLong(attr));
					attr = ex.findNext();
					if (attr != null && !attr.equals(""))
						setFilterAttrChoice(Convert.parseInt(attr));
					setShowBlacklisted(Boolean.valueOf(ex.findNext()).booleanValue());
				}
			}
			in.close();
			//ewe.sys.Time endT=new ewe.sys.Time();
			//Vm.debug("Time="+((((endT.hour*60+endT.minute)*60+endT.second)*1000+endT.millis)-(((startT.hour*60+startT.minute)*60+startT.second)*1000+startT.millis)));
			//Vm.debug("Start:"+startT.format("H:mm:ss.SSS"));
			//Vm.debug("End  :"+endT.format("H:mm:ss.SSS"));	
			// Build references between caches and addi wpts
			buildReferences();
		} catch (FileNotFoundException e) {
			Global.getPref().log("index.xml not found in directory "+dataDir); // Normal when profile is opened for first time
			//e.printStackTrace();
		} catch (IOException e){
			Global.getPref().log("Problem reading index.xml in dir: "+dataDir,e,true); 
		}
		normaliseFilters();
		resetUnsavedChanges();
	}

	/** Restore the filter to the values stored in this profile 
	 *  Called from Main Form and MainMenu 
	 *  The values of Filter.isActive and Filter.isInactive are set by the filter 
	 **/
	void restoreFilter() {
		restoreFilter( false );		
	}
	
	void restoreFilter(boolean clearIfInactive) {
		boolean inverted=isFilterInverted(); // Save it as doFilter will clear filterInverted
		Filter flt=new Filter();
		if (getFilterActive()==Filter.FILTER_ACTIVE) {
			flt.setFilter();
			flt.doFilter();
			if (inverted) {
				flt.invertFilter();
				setFilterInverted(true); // Needed because previous line inverts filterInverted
			}
		} else if (getFilterActive()==Filter.FILTER_CACHELIST) {
			Global.mainForm.cacheList.applyCacheList();
			//flt.filterActive=filterActive;
		} else if (getFilterActive()==Filter.FILTER_INACTIVE) {
			if (clearIfInactive) {
				flt.clearFilter();
			}
		}
	}
	
	void checkBlacklistStatus() {
		CacheHolder ch;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = cacheDB.get(i);
			if (ch.is_black() ^ showBlacklisted()) {
				ch.setFiltered(true);
				selectionChanged = true;
			}
		}
	}

	public int getCacheIndex(String wp) {
		return cacheDB.getIndex(wp);
	}

	/** Get a unique name for a new waypoint */
	public String getNewWayPointName() {
		String strWp = null;
		long lgWp = 0;
		int s = cacheDB.size();
		if (s == 0)
			return "CW0000";
		// Create new waypoint,look if not in db
		do {
			lgWp++;
			strWp = "CW" + MyLocale.formatLong(lgWp, "0000");
		} while (cacheDB.getIndex(strWp) >= 0);
		return strWp;
	}

	/**
	 * 
	 * @param forcache maincache
	 * @return
	 */
	public String getNewAddiWayPointName(String forcache) {
		int wptNo = -1;
		String waypoint;
		do {
			waypoint = MyLocale.formatLong(++wptNo, "00") + forcache.substring(2);
		} while (Global.getProfile().getCacheIndex(waypoint) >= 0);
		return waypoint;
	}

	/**
	 * Call this after getNewAddiWayPointName to set the references between main and addi correctly
	 * 
	 * @param ch
	 */
	public void setAddiRef(CacheHolder ch) {
		String mainwpt = ch.getWayPoint().substring(2);
		int mainindex = getCacheIndex("GC" + mainwpt);
		if (mainindex < 0)
			mainindex = getCacheIndex("OC" + mainwpt);
		if (mainindex < 0)
			mainindex = getCacheIndex("CW" + mainwpt);
		if (mainindex < 0)
			throw new IllegalArgumentException("no main cache found for: " + ch.getWayPoint());
		CacheHolder mainch = cacheDB.get(mainindex);
		mainch.addiWpts.add(ch);
		ch.mainCache = mainch;
	}

	public String toString() {
		return "Profile: Name="+name+"\nCentre="+centre.toString()+"\ndataDir="+dataDir+"\nlastSyncOC="+
		getLast_sync_opencaching()+"\ndistOC="+getDistOC()+"\ndistGC="+getDistGC();
	}

	public void setSelectForAll(boolean selectStatus) {
		selectionChanged = true;
		CacheHolder ch;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			if (ch.is_filtered() == false)
				ch.is_Checked = selectStatus;
		}
	}

	public int numCachesInArea; // only valid after calling getSourroundingArea

	public Area getSourroundingArea(boolean onlyOfSelected) {
		if (cacheDB == null || cacheDB.size() == 0) return null;
		CacheHolder ch;
		CWPoint topleft = null;
		CWPoint bottomright = null;
		CWPoint tmpca = new CWPoint();
		numCachesInArea = 0;
		boolean isAddi = false;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
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
		int anz = cacheDB.size();
		CacheHolder ch;
		// Jetzt durch die CacheDaten schleifen
		while(--anz >= 0){
			ch = cacheDB.get(anz); // This returns a pointer to the CacheHolder object
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

		// Build index for faster search and clear all references
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			ch.addiWpts.clear();
			ch.mainCache = null;
		}
		
		// Build references
		int max = cacheDB.size();
		for (int i = 0; i < max; i++) {
			ch = cacheDB.get(i);
			if (ch.isAddiWpt()) {
				// search main cache
				mainCh = cacheDB.get("GC" + ch.getWayPoint().substring(2));
				if (mainCh == null)  // TODO save the source (GC or OC or Custom) of the maincache somewhere else to avoid ambiguity of addi-wpt-names
					mainCh = cacheDB.get("OC" + ch.getWayPoint().substring(2));
				if (mainCh == null)  // TODO save the source (GC or OC or Custom) of the maincache somewhere else to avoid ambiguity of addi-wpt-names
					mainCh = cacheDB.get("CW" + ch.getWayPoint().substring(2));

				if (mainCh != null) {
					mainCh.addiWpts.add(ch);
					ch.mainCache = mainCh;
					ch.setAttributesFromMainCache(mainCh);
				}// if
			}// if
		}// for
		// sort addi wpts
		for (int i = 0; i < max; i++) {
			ch = cacheDB.get(i);
			if (ch.hasAddiWpt() && (ch.addiWpts.size() > 1)) {
				// ch.addiWpts.sort(new
				// MyComparer(ch.addiWpts,MyLocale.getMsg(1002,"Waypoint"),ch.addiWpts.size()),
				// false);
				ch.addiWpts.sort(new ewe.util.Comparer() {
					public int compare(Object o1, Object o2) {
						return ((CacheHolder) o1).getWayPoint().compareTo(
						        ((CacheHolder) o2).getWayPoint());
					}
				}, false);
			}
		}

	}

	/**
	 * Ensure that all filters have the proper length so that the 'charAt' access in the filter do
	 * not cause nullPointer Exceptions
	 */
	private void normaliseFilters() {
		String manyOnes = "11111111111111111111111111111";
		if (getFilterRose().length() < FILTERROSE.length()) {
			setFilterRose((getFilterRose() + manyOnes).substring(0, FILTERROSE.length()));
		}
		if (getFilterVar().length() < FILTERVAR.length()) {
			setFilterVar((getFilterVar() + manyOnes).substring(0, FILTERVAR.length()));
		}
		if (getFilterType().length() < FILTERTYPE.length()) {
			setFilterType((getFilterType() + manyOnes).substring(0, FILTERTYPE.length()));
		}
		if (getFilterSize().length() < FILTERSIZE.length()) {
			setFilterSize((getFilterSize() + manyOnes).substring(0, FILTERSIZE.length()));
		}
		if (getFilterDist().length() == 0)
			setFilterDist("L");
		if (getFilterDiff().length() == 0)
			setFilterDiff("L");
		if (getFilterTerr().length() == 0)
			setFilterTerr("L");
	}

	// Getter and Setter for private properties

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.notifyUnsavedChanges(!filterType.equals(this.filterType));
		this.filterType = filterType;
	}

	public String getFilterRose() {
		return filterRose;
	}

	public void setFilterRose(String filterRose) {
		this.notifyUnsavedChanges(!filterRose.equals(this.filterRose));
		this.filterRose = filterRose;
	}

	public String getFilterSize() {
		return filterSize;
	}

	public void setFilterSize(String filterSize) {
		this.notifyUnsavedChanges(!filterSize.equals(this.filterSize));
		this.filterSize = filterSize;
	}

	public String getFilterVar() {
		return filterVar;
	}

	public void setFilterVar(String filterVar) {
		this.notifyUnsavedChanges(!filterVar.equals(this.filterVar));
		this.filterVar = filterVar;
	}

	public String getFilterDist() {
		return filterDist;
	}

	public void setFilterDist(String filterDist) {
		this.notifyUnsavedChanges(!filterDist.equals(this.filterDist));
		this.filterDist = filterDist;
	}

	public String getFilterDiff() {
		return filterDiff;
	}

	public void setFilterDiff(String filterDiff) {
		this.notifyUnsavedChanges(!filterDiff.equals(this.filterDiff));
		this.filterDiff = filterDiff;
	}

	public String getFilterTerr() {
		return filterTerr;
	}

	public void setFilterTerr(String filterTerr) {
		this.notifyUnsavedChanges(!filterTerr.equals(this.filterTerr));
		this.filterTerr = filterTerr;
	}

	public int getFilterActive() {
		return filterActive;
	}

	public void setFilterActive(int filterActive) {
		this.notifyUnsavedChanges(filterActive != this.filterActive);
		this.setFilterInverted(false);
		this.filterActive = filterActive;
	}

	public boolean isFilterInverted() {
		return filterInverted;
	}

	public void setFilterInverted(boolean filterInverted) {
		this.notifyUnsavedChanges(filterInverted != this.filterInverted);
		this.filterInverted = filterInverted;
	}

	public boolean showBlacklisted() {
		return showBlacklisted;
	}

	public void setShowBlacklisted(boolean showBlacklisted) {
		this.notifyUnsavedChanges(showBlacklisted != this.showBlacklisted);
		this.showBlacklisted = showBlacklisted;
	}

	public long getFilterAttrYes() {
		return filterAttrYes;
	}

	public void setFilterAttrYes(long filterAttrYes) {
		this.notifyUnsavedChanges(filterAttrYes != this.filterAttrYes);
		this.filterAttrYes = filterAttrYes;
	}

	public long getFilterAttrNo() {
		return filterAttrNo;
	}

	public void setFilterAttrNo(long filterAttrNo) {
		this.notifyUnsavedChanges(filterAttrNo != this.filterAttrNo);
		this.filterAttrNo = filterAttrNo;
	}

	public int getFilterAttrChoice() {
		return filterAttrChoice;
	}

	public void setFilterAttrChoice(int filterAttrChoice) {
		this.notifyUnsavedChanges(filterAttrChoice != this.filterAttrChoice);
		this.filterAttrChoice = filterAttrChoice;
	}

	public String getLast_sync_opencaching() {
		return last_sync_opencaching;
	}

	public void setLast_sync_opencaching(String last_sync_opencaching) {
		this.notifyUnsavedChanges(!last_sync_opencaching.equals(this.last_sync_opencaching));
		this.last_sync_opencaching = last_sync_opencaching;
	}

	public String getDistOC() {
		return distOC;
	}

	public void setDistOC(String distOC) {
		this.notifyUnsavedChanges(!distOC.equals(this.distOC));
		this.distOC = distOC;
	}

	public String getDistGC() {
		return distGC;
	}

	public void setDistGC(String distGC) {
		this.notifyUnsavedChanges(!distGC.equals(this.distGC));
		this.distGC = distGC;
	}

}
