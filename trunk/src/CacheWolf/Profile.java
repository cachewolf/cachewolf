package CacheWolf;

import CacheWolf.imp.OCXMLImporter;
import CacheWolf.navi.Area;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.FileBugfix;
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
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;

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

	/** path to the maps of the profile relative to the maps root */
	private String relativeCustomMapsPath = new String();

	public final static boolean SHOW_PROGRESS_BAR = true;
	public final static boolean NO_SHOW_PROGRESS_BAR = false;

	private FilterData currentFilter = new FilterData();
	private int filterActive = Filter.FILTER_INACTIVE;
	private boolean filterInverted = false;
	private boolean showBlacklisted = false;
	private boolean showSearchResult = false;

	public boolean selectionChanged = true; // ("H�ckchen") used by movingMap to get to knao if it should update the caches in the map 
	/** True if the profile has been modified and not saved
	 * The following modifications set this flag: New profile centre, Change of waypoint data 
	 */
	private boolean hasUnsavedChanges = false;
	public boolean byPassIndexActive = false;
	private int indexXmlVersion;
	/** version number of current format for index.xml and waypoint.xml */
	protected static int CURRENTFILEFORMAT = 3;

	//TODO Add other settings, such as max. number of logs to spider
	//TODO Add settings for the preferred mapper to allow for maps other than expedia and other resolutions

	/**
	 * Constructor for a profile
	 *
	 */
	public Profile(){ // public constructor
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
		setRelativeCustomMapsPath("");
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
			if (backup.exists()) {
				backup.delete();
			}
			File index=new File(dataDir+"index.xml");
			index.rename("index.bak");
		} catch (Exception ex) {
			pref.log("Error deleting backup or renaming index.xml");
		}
		try{
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(new FileBugfix(dataDir + "index.xml").getAbsolutePath())));
		} catch (Exception e) {
			Vm.debug("Problem creating index file "+e.toString()+"\nFilename="+dataDir + "index.xml");
			return;
		}
		CWPoint savedCentre=centre;
		if (centre==null || !centre.isValid() || (savedCentre.latDec==0.0 && savedCentre.lonDec==0.0)) savedCentre=pref.curCentrePt;

		try{
			detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			detfile.print("<CACHELIST format=\"decimal\">\n");
			detfile.print("    <VERSION value = \"3\"/>\n");
			if (savedCentre.isValid())
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

			// If the current filter is a CacheTour filter, then save it as
			// normal filter, because after loading there is no cache tour defined
			// which could be used as filter criterium.
			int activeFilterForSave;
			if (getFilterActive()==Filter.FILTER_CACHELIST) {
				activeFilterForSave = Filter.FILTER_ACTIVE;
			} else {
				activeFilterForSave = getFilterActive();
			}
			detfile.print("    <FILTERCONFIG status = \""+activeFilterForSave+
					(isFilterInverted()?"T":"F")+"\" showBlacklist = \""+showBlacklisted()+"\" />\n");
			detfile.print(this.getCurrentFilter().toXML(""));
			detfile.print("    <SYNCOC date = \""+getLast_sync_opencaching()+"\" dist = \""+getDistOC()+"\"/>\n");
			detfile.print("    <SPIDERGC dist = \"" + getDistGC() + "\"/>\n");
			detfile.print("    <mapspath relativeDir = \"" + SafeXML.clean(relativeCustomMapsPath) + "\"/>\n");
			int size = cacheDB.size();
			for (int i = 0; i < size; i++) {
				if (showprogress) {
					h.progress = (float) i / (float) size;
					h.changed();
				}
				ch = cacheDB.get(i);
				// //Vm.debug("Saving: " + ch.CacheName);
				if (ch.getWayPoint().length() > 0) { 
					detfile.print(ch.toXML());
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

	public void readIndex() {
		readIndex(null);
	}
	/**
	 *	Method to read the index.xml file that holds the total information
	 *	on available caches in the database. The database in nothing else
	 *	than the collection of caches in a directory.
	 */
	public void readIndex(InfoBox infoBox) {

		try {
			selectionChanged = true;
			boolean fmtDec = false;
			String mainInfoText = MyLocale.getMsg(5000,"Loading Cache-List");
			int wptNo=1;
			int lastShownWpt=0;
			char decSep = MyLocale.getDigSeparator().charAt(0);
			char notDecSep = decSep == '.' ? ',' : '.';
			FileBugfix indexFile = new FileBugfix(dataDir + "index.xml");
			FileReader in = new FileReader(indexFile.getAbsolutePath());
			indexXmlVersion = 1; // Initial guess
			in.readLine(); // <?xml version= ...
			String text = in.readLine(); // <CACHELIST>
			if (text!=null && text.indexOf("decimal")>0) fmtDec=true;
			Extractor ex = new Extractor(null, " = \"", "\" ", 0, true);
			
			//ewe.sys.Time startT=new ewe.sys.Time();
			boolean convertWarningDisplayed = false;
			while ((text = in.readLine()) != null){
				// Check for Line with cache data
				if (text.indexOf("<CACHE ")>=0){
					if (indexXmlVersion < CURRENTFILEFORMAT && !convertWarningDisplayed) {
						if (indexXmlVersion < CURRENTFILEFORMAT) {
							convertWarningDisplayed = true;
							new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(4407, "The profile files are not in the current format.%0aTherefore they are now converted to the current format. Depending of the size of the profile and the computer involved this may take some minutes. Please bear with us until the conversion is done."), FormBase.OKB).execute();
						}
					}
					if (infoBox!=null) {
						if (wptNo - 10 >= lastShownWpt) {
							infoBox.setInfo(mainInfoText+"\n"+String.valueOf(wptNo));
							lastShownWpt = wptNo;
						}
						wptNo++;
					}
					CacheHolder ch=new CacheHolder(text,indexXmlVersion);
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
						centre.set(lat+" "+lon,TransformCoordinates.CW); // Fast parse
					}	
				} else if (text.indexOf("<VERSION")>=0) {
					int start=text.indexOf("value = \"")+9;
					indexXmlVersion  = Integer.valueOf(text.substring(start,text.indexOf("\"",start))).intValue();
					if (indexXmlVersion > CURRENTFILEFORMAT) {
						Global.getPref().log("unsupported file format");
						clearProfile();
						return;
					}
				} else if (text.indexOf("<SYNCOC")>=0) {
					int start=text.indexOf("date = \"")+8;
					setLast_sync_opencaching(text.substring(start,text.indexOf("\"",start)));
					start=text.indexOf("dist = \"")+8;
					setDistOC(text.substring(start,text.indexOf("\"",start)));
				} else if (text.indexOf("mapspath")>=0) {
					int start=text.indexOf("relativeDir = \"")+15;
					setRelativeCustomMapsPath(SafeXML.cleanback(text.substring(start,text.indexOf("\"",start))).replace('\\','/'));
				} else if (text.indexOf("<SPIDERGC")>=0) {
					int start=text.indexOf("dist = \"")+8;
					setDistGC(text.substring(start,text.indexOf("\"",start)));
				} else if (indexXmlVersion <=2 && text.indexOf("<FILTER")>=0){
					// Read filter data of file versions 1 and 2. (Legacy code)
					ex.setSource(text.substring(text.indexOf("<FILTER")));
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
				} else if (text.indexOf("<FILTERDATA")>=0){
					ex.setSource(text.substring(text.indexOf("<FILTERDATA")));
					setFilterRose(ex.findNext());
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
					setFilterAttrYes(Convert.parseLong(attr));
					attr = ex.findNext();
					setFilterAttrNo(Convert.parseLong(attr));
					attr = ex.findNext();
					setFilterAttrChoice(Convert.parseInt(attr));
					setFilterStatus(SafeXML.cleanback(ex.findNext()));
					setFilterUseRegexp(Boolean.valueOf(ex.findNext()).booleanValue());
					attr = ex.findNext();
  				if (attr != null && !attr.equals("")) {
  					setFilterNoCoord(Boolean.valueOf(attr).booleanValue());
          } else {
            setFilterNoCoord(true);
          }
				} else if (text.indexOf("<FILTERCONFIG")>=0){
					ex.setSource(text.substring(text.indexOf("<FILTERCONFIG")));
					String temp=ex.findNext();
					setFilterActive(Common.parseInt(temp.substring(0,1)));
					setFilterInverted(temp.charAt(1)=='T');
					setShowBlacklisted(Boolean.valueOf(ex.findNext()).booleanValue());
				}
			}
			in.close();
			//ewe.sys.Time endT=new ewe.sys.Time();
			//Vm.debug("Time="+((((endT.hour*60+endT.minute)*60+endT.second)*1000+endT.millis)-(((startT.hour*60+startT.minute)*60+startT.second)*1000+startT.millis)));
			//Vm.debug("Start:"+startT.format("H:mm:ss.SSS"));
			//Vm.debug("End  :"+endT.format("H:mm:ss.SSS"));	
			// Build references between caches and addi wpts
			if (infoBox!=null) {
				infoBox.setInfo(MyLocale.getMsg(5004,"Building references..."));
			}
			buildReferences();
			if (indexXmlVersion < CURRENTFILEFORMAT) {
				saveIndex(Global.getPref(), true);
			}
		} catch (FileNotFoundException e) {
			Global.getPref().log("index.xml not found in directory "+dataDir); // Normal when profile is opened for first time
			//e.printStackTrace();
		} catch (IOException e){
			Global.getPref().log("Problem reading index.xml in dir: "+dataDir,e,true); 
		}
		// TODO Brauchen wir das noch?
		this.getCurrentFilter().normaliseFilters();
		resetUnsavedChanges();
	}

	/** Restore the filter to the values stored in this profile 
	 *  Called from Main Form and MainMenu 
	 *  The values of Filter.isActive and Filter.isInactive are set by the filter 
	 **/
	public void restoreFilter() {
		restoreFilter( true );		
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
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt())
			mainindex = getCacheIndex(OCXMLImporter.OPENCACHING_CZ_PATTERN + mainwpt);
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt())
			mainindex = getCacheIndex(OCXMLImporter.OPENCACHING_DE_PATTERN + mainwpt);
//		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt())
//			mainindex = getCacheIndex(OCXMLImporter.OPENCACHING_PL_PATTERN + mainwpt);
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt())
			mainindex = getCacheIndex(OCXMLImporter.OPENCACHING_UK_PATTERN + mainwpt);
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt())
			mainindex = getCacheIndex("CW" + mainwpt);
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt()) {
			ch.setIncomplete(true);
		} else {
			CacheHolder mainch = cacheDB.get(mainindex);
			if (mainch.getWayPoint().equals(ch.getWayPoint())) {
				ch.setIncomplete(true);
			} else {
				mainch.addiWpts.add(ch);
				ch.mainCache = mainch;
				ch.setAttributesFromMainCache();
			}
		}
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
			if (ch.isVisible())
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
				if (ch.pos.isValid() ){ // done: && ch.pos.latDec != 0 && ch.pos.lonDec != 0 TO-DO != 0 sollte rausgenommen werden sobald in der Liste vern�nftig mit nicht gesetzten pos umgegangen wird
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
		CacheHolder ch;
		MyComparer myComparer = new MyComparer();

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
			if (ch.isAddiWpt()) 
				setAddiRef(ch);
		}
		// sort addi wpts
		for (int i = 0; i < max; i++) {
			ch = cacheDB.get(i);
			if (ch.hasAddiWpt() && (ch.addiWpts.size() > 1)) {
				ch.addiWpts.sort(myComparer, false);
			}
		}

	}

	// Getter and Setter for private properties

	public String getFilterType() {
		return currentFilter.getFilterType();
	}

	public void setFilterType(String filterType) {
		this.notifyUnsavedChanges(!filterType.equals(this.getFilterType()));
		this.currentFilter.setFilterType(filterType);
	}

	public String getFilterRose() {
		return currentFilter.getFilterRose();
	}

	public void setFilterRose(String filterRose) {
		this.notifyUnsavedChanges(!filterRose.equals(this.getFilterRose()));
		this.currentFilter.setFilterRose(filterRose);
	}

	public String getFilterSize() {
		return currentFilter.getFilterSize();
	}

	public void setFilterSize(String filterSize) {
		this.notifyUnsavedChanges(!filterSize.equals(this.getFilterSize()));
		this.currentFilter.setFilterSize(filterSize);
	}

	public String getFilterVar() {
		return currentFilter.getFilterVar();
	}

	public void setFilterVar(String filterVar) {
		this.notifyUnsavedChanges(!filterVar.equals(this.getFilterVar()));
		this.currentFilter.setFilterVar(filterVar);
	}

	public String getFilterDist() {
		return currentFilter.getFilterDist();
	}

	public void setFilterDist(String filterDist) {
		this.notifyUnsavedChanges(!filterDist.equals(this.getFilterDist()));
		this.currentFilter.setFilterDist(filterDist);
	}

	public String getFilterDiff() {
		return currentFilter.getFilterDiff();
	}

	public void setFilterDiff(String filterDiff) {
		this.notifyUnsavedChanges(!filterDiff.equals(this.getFilterDiff()));
		this.currentFilter.setFilterDiff(filterDiff);
	}

	public String getFilterTerr() {
		return currentFilter.getFilterTerr();
	}

	public void setFilterTerr(String filterTerr) {
		this.notifyUnsavedChanges(!filterTerr.equals(this.getFilterTerr()));
		this.currentFilter.setFilterTerr(filterTerr);
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

	/**
	 * If <code>true</code> then the cache list will only display the
	 * caches that are result of a search.   
	 * @return <code>True</code> if list should only display search results
	 */
	public boolean showSearchResult() {
		return showSearchResult;
	}
	
	/**
	 * Sets parameter if cache list should only display the caches that are
	 * results of a search.
	 * @param showSearchResult <code>True</code>: List should only display search
	 * results.
	 */
	public void setShowSearchResult(boolean showSearchResult){
		this.showSearchResult = showSearchResult;
	}

	public long getFilterAttrYes() {
		return currentFilter.getFilterAttrYes();
	}

	public void setFilterAttrYes(long filterAttrYes) {
		this.notifyUnsavedChanges(filterAttrYes != this.getFilterAttrYes());
		this.currentFilter.setFilterAttrYes(filterAttrYes);
	}

	public long getFilterAttrNo() {
		return currentFilter.getFilterAttrNo();
	}

	public void setFilterAttrNo(long filterAttrNo) {
		this.notifyUnsavedChanges(filterAttrNo != this.getFilterAttrNo());
		this.currentFilter.setFilterAttrNo(filterAttrNo);
	}

	public int getFilterAttrChoice() {
		return currentFilter.getFilterAttrChoice();
	}

	public void setFilterAttrChoice(int filterAttrChoice) {
		this.notifyUnsavedChanges(filterAttrChoice != this.getFilterAttrChoice());
		this.currentFilter.setFilterAttrChoice(filterAttrChoice);
	}

	public String getFilterStatus() {
    	return currentFilter.getFilterStatus();
    }

	public void setFilterStatus(String filterStatus) {
		this.notifyUnsavedChanges(filterStatus != this.getFilterStatus());
    	this.currentFilter.setFilterStatus(filterStatus);
    }

	public boolean getFilterUseRegexp() {
    	return currentFilter.useRegexp();
    }

	public void setFilterUseRegexp(boolean useRegexp) {
		this.notifyUnsavedChanges(useRegexp != this.getFilterUseRegexp());
    	this.currentFilter.setUseRegexp(useRegexp);
    }

	public boolean getFilterNoCoord() {
    	return currentFilter.getFilterNoCoord();
    }

	public void setFilterNoCoord(boolean filterNoCoord) {
		this.notifyUnsavedChanges(filterNoCoord != this.getFilterNoCoord());
    	this.currentFilter.setFilterNoCoord(filterNoCoord);
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

	public String getRelativeCustomMapsPath() {
		return relativeCustomMapsPath;
	}
	
	public void setRelativeCustomMapsPath(String rCMPath) {
		this.notifyUnsavedChanges(!rCMPath.equals(this.relativeCustomMapsPath));
		this.relativeCustomMapsPath = rCMPath;
	}
	/**
	 * Returns the currently active FilterData object for the profile.
	 * @return Object representing the setting of the filter
	 */
	public FilterData getCurrentFilter() {
    	return currentFilter;
    }

	public void setCurrentFilter(FilterData currentFilter) {
    	this.currentFilter = currentFilter;
    }
	
	private class MyComparer implements ewe.util.Comparer {

		public int compare(Object o1, Object o2) {
			return ((CacheHolder) o1).getWayPoint().compareTo(
			        ((CacheHolder) o2).getWayPoint());
		}
		
	}
}
