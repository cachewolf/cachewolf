/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf;

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

	/**
	 * The list of caches (CacheHolder objects). A pointer to this object exists in many classes in parallel to this object, i.e. the respective class contains both a {@link Profile} object and a cacheDB Vector.
	 */
	public CacheDB cacheDB = new CacheDB();
	/** The centre point of this group of caches. Read from ans stored to index.xml file */
	public CWPoint centre = new CWPoint();
	/**
	 * The name of the profile. The baseDir in preferences is appended this name to give the dataDir where the index.xml and cache files live. (Excuse the English spelling of centre)
	 */
	public String name = "";
	/** This is the directory for the profile. It contains a closing /. */
	public String dataDir = "";

	/** Last sync date for opencaching caches */
	private String last_sync_opencaching = "";

	/** Distance for opencaching caches */
	private String distOC = "";

	/** Distance for geocaching caches */
	private String distGC = "";
	private String minDistGC = "";
	/** Direction for geocaching caches */
	private String directionGC = "";

	private String gpxStyle = new String();
	private String gpxTarget = new String();
	private String gpxId = new String();

	/** path to the maps of the profile relative to the maps root */
	private String relativeCustomMapsPath = "";

	public final static boolean SHOW_PROGRESS_BAR = true;
	public final static boolean NO_SHOW_PROGRESS_BAR = false;

	private FilterData currentFilter = new FilterData();
	private int filterActive = Filter.FILTER_INACTIVE;
	private boolean filterInverted = false;
	private boolean showBlacklisted = false;
	private boolean showSearchResult = false;

	public boolean selectionChanged = true; // ("Häckchen") used by movingMap to get to knao if it should update the caches in the map
	/**
	 * True if the profile has been modified and not saved The following modifications set this flag: New profile centre, Change of waypoint data
	 */
	private boolean hasUnsavedChanges = false;
	public boolean byPassIndexActive = false;
	private int indexXmlVersion;
	/** version number of current format for index.xml and waypoint.xml */
	protected static int CURRENTFILEFORMAT = 3;

	// TODO Add other settings, such as max. number of logs to spider
	// TODO Add settings for the preferred mapper to allow for maps other than expedia and other resolutions

	/**
	 * Constructor for a profile
	 * 
	 */
	public Profile() { // public constructor
	}

	/**
	 * Returns <code>true</code> if profile needs to be changed when profile is left. Returns <code>false</code> if no relevant changes have been made.
	 * 
	 * @return hasUnsavedChanges
	 */
	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}

	/**
	 * Remember that profile needs to be saved. Flag is set <code>true</code> when parameter is true, but it's not set to <code>false</code> when parameter is <code>false</code>.<br>
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
		setMinDistGC("");
		setDirectionGC("");
		setGpxId("0");
		setGpxStyle("0");
		setGpxTarget("0");
		setRelativeCustomMapsPath("");
		resetUnsavedChanges();
	}

	public void setCenterCoords(CWPoint coords) {
		this.notifyUnsavedChanges(coords.equals(this.centre));
		this.centre.set(coords);
	}

	/**
	 * Method to save the index.xml file that holds the total information on available caches in the database. The database is nothing else than the collection of caches in a directory.
	 * 
	 * Not sure whether we need to keep 'pref' in method signature. May eventually remove it.
	 * 
	 * Saves the index with the filter settings from Filter
	 */
	// public void saveIndex(Preferences pref, boolean showprogress){
	// saveIndex(pref,showprogress, Filter.filterActive,Filter.filterInverted);
	// }
	/** Save index with filter settings given */
	public void saveIndex(Preferences pref, boolean showprogress) {
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int updFrequ = Vm.isMobile() ? 10 : 40; // Number of caches between screen updates
		if (showprogress) {
			pbf.showMainTask = true;// false;
			pbf.setTask(h, "Saving Index");
			pbf.exec();
		}
		CacheHolder.saveAllModifiedDetails(); // this must be called first as it makes some calculations
		PrintWriter detfile;
		CacheHolder ch;
		try {
			File backup = new File(dataDir + "index.bak");
			if (backup.exists()) {
				backup.delete();
			}
			File index = new File(dataDir + "index.xml");
			index.rename("index.bak");
		} catch (Exception ex) {
			pref.log("[Profile:saveIndex]Error deleting backup or renaming index.xml");
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(new FileBugfix(dataDir + "index.xml").getAbsolutePath())));
		} catch (Exception e) {
			pref.log("Problem creating index.xml " + dataDir, e);
			return;
		}
		CWPoint savedCentre = centre;
		if (centre == null || !centre.isValid() || (savedCentre.latDec == 0.0 && savedCentre.lonDec == 0.0)) savedCentre = pref.getCurCentrePt();

		try {
			detfile.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			detfile.print("<CACHELIST format=\"decimal\">\n");
			detfile.print("    <VERSION value = \"3\"/>\n");
			if (savedCentre.isValid()) detfile.print("    <CENTRE lat=\"" + savedCentre.latDec + "\" lon=\"" + savedCentre.lonDec + "\"/>\n");
			if (getLast_sync_opencaching() == null || getLast_sync_opencaching().endsWith("null") || getLast_sync_opencaching().equals("")) {
				setLast_sync_opencaching("20050801000000");
			}
			if (getDistOC() == null || getDistOC().endsWith("null") || getDistOC().equals("")) {
				setDistOC("0.0");
			}
			if (getDistGC() == null || getDistGC().endsWith("null") || getDistGC().equals("")) {
				setDistGC("0.0");
			}
			if (getMinDistGC() == null || getMinDistGC().endsWith("null") || getMinDistGC().equals("")) {
				setMinDistGC("0.0");
			}
			if (getDirectionGC() == null || getDirectionGC().endsWith("null") || getDirectionGC().equals("")) {
				setDirectionGC("");
			}

			// If the current filter is a CacheTour filter, then save it as
			// normal filter, because after loading there is no cache tour defined
			// which could be used as filter criterium.
			int activeFilterForSave;
			if (getFilterActive() == Filter.FILTER_CACHELIST) {
				activeFilterForSave = Filter.FILTER_ACTIVE;
			} else {
				activeFilterForSave = getFilterActive();
			}
			detfile.print("    <FILTERCONFIG status = \"" + activeFilterForSave + (isFilterInverted() ? "T" : "F") + "\" showBlacklist = \"" + showBlacklisted() + "\" />\n");
			detfile.print(this.getCurrentFilter().toXML(""));
			detfile.print("    <SYNCOC date = \"" + getLast_sync_opencaching() + "\" dist = \"" + getDistOC() + "\"/>\n");
			detfile.print("    <SPIDERGC dist = \"" + getDistGC() + "\" mindist = \"" + getMinDistGC() + "\" direction = \"" + getDirectionGC() + "\"/>\n");
			detfile.print("    <EXPORT style = \"" + getGpxStyle() + "\" target = \"" + getGpxTarget() + "\" id = \"" + getGpxId() + "\"/>\n");
			detfile.print("    <mapspath relativeDir = \"" + SafeXML.clean(relativeCustomMapsPath) + "\"/>\n");
			int size = cacheDB.size();
			for (int i = 0; i < size; i++) {
				if (showprogress) {
					h.progress = (float) i / (float) size;
					if ((i % updFrequ) == 0) h.changed();
				}
				ch = cacheDB.get(i);
				if (ch.getWayPoint().length() > 0) {
					detfile.print(ch.toXML());
				}
			}
			detfile.print("</CACHELIST>\n");
			detfile.close();
			buildReferences(); // TODO Why is this needed here?
			if (showprogress) pbf.exit(0);
		} catch (Exception e) {
			pref.log("Problem writing to index file ", e);
			detfile.close();
			if (showprogress) pbf.exit(0);
		}
		resetUnsavedChanges();
	}

	public void readIndex() {
		readIndex(null);
	}

	/**
	 * Method to read the index.xml file that holds the total information on available caches in the database. The database in nothing else than the collection of caches in a directory.
	 */
	public void readIndex(InfoBox infoBox) {
		int updFrequ = Vm.isMobile() ? 10 : 40; // Number of caches between screen updates
		try {
			selectionChanged = true;
			boolean fmtDec = false;
			String mainInfoText = MyLocale.getMsg(5000, "Loading Cache-List");
			int wptNo = 1;
			int lastShownWpt = 0;
			char decSep = MyLocale.getDigSeparator().charAt(0);
			char notDecSep = decSep == '.' ? ',' : '.';
			FileBugfix indexFile = new FileBugfix(dataDir + "index.xml");
			FileReader in = new FileReader(indexFile.getAbsolutePath());
			indexXmlVersion = 1; // Initial guess
			in.readLine(); // <?xml version= ...
			String text = in.readLine(); // <CACHELIST>
			if (text != null && text.indexOf("decimal") > 0) fmtDec = true;
			Extractor ex = new Extractor(null, " = \"", "\" ", 0, true);

			// ewe.sys.Time startT=new ewe.sys.Time();
			boolean convertWarningDisplayed = false;
			while ((text = in.readLine()) != null) {
				// Check for Line with cache data
				if (text.indexOf("<CACHE ") >= 0) {
					if (indexXmlVersion < CURRENTFILEFORMAT && !convertWarningDisplayed) {
						if (indexXmlVersion < CURRENTFILEFORMAT) {
							convertWarningDisplayed = true;
							int res = new MessageBox(
									MyLocale.getMsg(144, "Warning"),
									MyLocale
											.getMsg(
													4407,
													"The profile files are not in the current format.%0aTherefore they are now converted to the current format. Depending of the size of the profile and the computer involved this may take some minutes. Please bear with us until the conversion is done."),
									FormBase.YESB | FormBase.NOB).execute();
							if (res == MessageBox.NOB) {
								ewe.sys.Vm.exit(0);
							}
						}
					}
					if (infoBox != null) {
						if (wptNo - updFrequ >= lastShownWpt) {
							infoBox.setInfo(mainInfoText + "\n" + String.valueOf(wptNo));
							lastShownWpt = wptNo;
						}
						wptNo++;
					}
					CacheHolder ch = new CacheHolder(text, indexXmlVersion);
					cacheDB.add(ch);
				} else if (text.indexOf("<CENTRE") >= 0) { // lat= lon=
					if (fmtDec) {
						int start = text.indexOf("lat=\"") + 5;
						String lat = text.substring(start, text.indexOf("\"", start)).replace(notDecSep, decSep);
						start = text.indexOf("lon=\"") + 5;
						String lon = text.substring(start, text.indexOf("\"", start)).replace(notDecSep, decSep);
						centre.set(Convert.parseDouble(lat), Convert.parseDouble(lon));
					} else {
						int start = text.indexOf("lat=\"") + 5;
						String lat = SafeXML.cleanback(text.substring(start, text.indexOf("\"", start)));
						start = text.indexOf("long=\"") + 6;
						String lon = SafeXML.cleanback(text.substring(start, text.indexOf("\"", start)));
						centre.set(lat + " " + lon, TransformCoordinates.CW); // Fast parse
					}
				} else if (text.indexOf("<VERSION") >= 0) {
					int start = text.indexOf("value = \"") + 9;
					indexXmlVersion = Integer.valueOf(text.substring(start, text.indexOf("\"", start))).intValue();
					if (indexXmlVersion > CURRENTFILEFORMAT) {
						Global.getPref().log("[Profile:readIndex]unsupported file format");
						clearProfile();
						return;
					}
				} else if (text.indexOf("<SYNCOC") >= 0) {
					int start = text.indexOf("date = \"") + 8;
					setLast_sync_opencaching(text.substring(start, text.indexOf("\"", start)));
					start = text.indexOf("dist = \"") + 8;
					setDistOC(text.substring(start, text.indexOf("\"", start)));
				} else if (text.indexOf("mapspath") >= 0) {
					int start = text.indexOf("relativeDir = \"") + 15;
					setRelativeCustomMapsPath(SafeXML.cleanback(text.substring(start, text.indexOf("\"", start))).replace('\\', '/'));
				} else if (text.indexOf("<SPIDERGC") >= 0) {
					int start = text.indexOf("dist = \"") + 8;
					setDistGC(text.substring(start, text.indexOf("\"", start)));
					start = text.indexOf("mindist = \"") + 11;
					if (start == 10) {
						setMinDistGC("0");
					} else
						setMinDistGC(text.substring(start, text.indexOf("\"", start)));
					start = text.indexOf("direction = \"") + 13;
					if (start == 12) {
						setDirectionGC("");
					} else
						setDirectionGC(text.substring(start, text.indexOf("\"", start)));
				} else if (text.indexOf("<EXPORT") >= 0) {
					int start = text.indexOf("style = \"") + 9;
					if (start == 8) {
						setGpxStyle("0");
					} else
						setGpxStyle(text.substring(start, text.indexOf("\"", start)));
					start = text.indexOf("target = \"") + 10;
					if (start == 9) {
						setGpxTarget("0");
					} else
						setGpxTarget(text.substring(start, text.indexOf("\"", start)));
					start = text.indexOf("id = \"") + 6;
					if (start == 5) {
						setGpxId("0");
					} else
						setGpxId(text.substring(start, text.indexOf("\"", start)));
				} else if (indexXmlVersion <= 2 && text.indexOf("<FILTER") >= 0) {
					// Read filter data of file versions 1 and 2. (Legacy code)
					String temp = ex.findFirst(text.substring(text.indexOf("<FILTER"))); // Filter status is now first, need to deal with old versions which don't have filter status
					if (temp.length() == 2) {
						// Compatibility with previous versions
						if (temp.charAt(0) == 'T')
							setFilterActive(Filter.FILTER_ACTIVE);
						else
							setFilterActive(Common.parseInt(temp.substring(0, 1)));
						setFilterInverted(temp.charAt(1) == 'T');
						setFilterRose(ex.findNext());
					} else
						setFilterRose(temp);
					setFilterType(ex.findNext());
					setFilterVar(ex.findNext());
					setFilterDist(ex.findNext());
					setFilterDiff(ex.findNext());
					setFilterTerr(ex.findNext());
					setFilterSize(ex.findNext());
					String attr = ex.findNext();
					long[] filterAttr = { 0l, 0l, 0l, 0l };
					if (attr != null && !attr.equals("")) filterAttr[0] = Convert.parseLong(attr);
					attr = ex.findNext();
					if (attr != null && !attr.equals("")) filterAttr[2] = Convert.parseLong(attr);
					attr = ex.findNext();
					setFilterAttr(filterAttr);
					if (attr != null && !attr.equals("")) setFilterAttrChoice(Convert.parseInt(attr));
					setShowBlacklisted(Boolean.valueOf(ex.findNext()).booleanValue());
				} else if (text.indexOf("<FILTERDATA") >= 0) {
					setFilterRose(ex.findFirst(text.substring(text.indexOf("<FILTERDATA"))));
					setFilterType(ex.findNext());
					setFilterVar(ex.findNext());
					setFilterDist(ex.findNext());
					setFilterDiff(ex.findNext());
					setFilterTerr(ex.findNext());
					setFilterSize(ex.findNext());
					String attr = ex.findNext();
					long[] filterAttr = { 0l, 0l, 0l, 0l };
					if (attr != null && !attr.equals("")) filterAttr[0] = Convert.parseLong(attr);
					attr = ex.findNext();
					if (attr != null && !attr.equals("")) filterAttr[2] = Convert.parseLong(attr);
					setFilterAttr(filterAttr);
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
					attr = ex.findNext();
					if (attr != null && !attr.equals("")) filterAttr[1] = Convert.parseLong(attr);
					attr = ex.findNext();
					if (attr != null && !attr.equals("")) filterAttr[3] = Convert.parseLong(attr);
					setFilterAttr(filterAttr);
				} else if (text.indexOf("<FILTERCONFIG") >= 0) {
					String temp = ex.findFirst(text.substring(text.indexOf("<FILTERCONFIG")));
					setFilterActive(Common.parseInt(temp.substring(0, 1)));
					setFilterInverted(temp.charAt(1) == 'T');
					setShowBlacklisted(Boolean.valueOf(ex.findNext()).booleanValue());
				}
			}
			in.close();
			// Build references between caches and addi wpts
			if (infoBox != null) {
				infoBox.setInfo(MyLocale.getMsg(5004, "Building references..."));
			}
			buildReferences();
			if (indexXmlVersion < CURRENTFILEFORMAT) {
				saveIndex(Global.getPref(), true);
			}
		} catch (FileNotFoundException e) {
			Global.getPref().log("index.xml not found in directory " + dataDir, e);
		} catch (IOException e) {
			Global.getPref().log("Problem reading index.xml in dir: " + dataDir, e, true);
		}
		this.getCurrentFilter().normaliseFilters();
		resetUnsavedChanges();
	}

	/**
	 * Restore the filter to the values stored in this profile Called from Main Form and MainMenu The values of Filter.isActive and Filter.isInactive are set by the filter
	 */
	public void restoreFilter() {
		restoreFilter(true);
	}

	void restoreFilter(boolean clearIfInactive) {
		boolean inverted = isFilterInverted(); // Save it as doFilter will clear filterInverted
		Filter flt = new Filter();
		if (getFilterActive() == Filter.FILTER_ACTIVE) {
			flt.setFilter();
			flt.doFilter();
			if (inverted) {
				flt.invertFilter();
				setFilterInverted(true); // Needed because previous line inverts filterInverted
			}
		} else if (getFilterActive() == Filter.FILTER_CACHELIST) {
			Global.mainForm.cacheList.applyCacheList();
			// flt.filterActive=filterActive;
		} else if (getFilterActive() == Filter.FILTER_INACTIVE) {
			if (clearIfInactive) {
				flt.clearFilter();
			}
		}
	}

	public int getCacheIndex(String wp) {
		return cacheDB.getIndex(wp);
	}

	/** Get a unique name for a new waypoint */
	public String getNewWayPointName(String prefix) {
		String strWp = null;
		long lgWp = 0;
		int s = cacheDB.size();
		if (s == 0) return prefix + "0000";
		// Create new waypoint,look if not in db
		do {
			lgWp++;
			strWp = prefix + MyLocale.formatLong(lgWp, "0000");
		} while (cacheDB.getIndex(strWp) >= 0);
		return strWp;
	}

	/**
	 * 
	 * @param forcache
	 *            maincache
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
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt()) {
			for (int i = 0; i < OC.OCSites.length; i++) {
				mainindex = getCacheIndex(OC.OCSites[i][OC.OC_PREFIX] + mainwpt);
				if (mainindex >= 0 && cacheDB.get(mainindex).isCacheWpt()) {
					break;
				}
			}
		}
		if (mainindex < 0 || !cacheDB.get(mainindex).isCacheWpt()) mainindex = getCacheIndex("CW" + mainwpt);
		if (mainindex < 0 /* || !cacheDB.get(mainindex)..isCacheWpt() */) {
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
		return "Profile: Name=" + name + "\nCentre=" + centre.toString() + "\ndataDir=" + dataDir + "\nlastSyncOC=" + getLast_sync_opencaching() + "\ndistOC=" + getDistOC() + "\ndistGC=" + getDistGC();
	}

	/**
	 * Sets the selection state for all caches to the given state <code>selectStatus</code>. There is a little distinction for the <code>true</code> and <code>false</code> case:<br>
	 * selectStatus <code>true</code>: All <i>visible</i> caches are checked, and their addi wpts, regardless if they are visible or not.<br>
	 * selectStatus <code>false</code>: All caches are unchecked, regardless if they are visible or not.
	 * 
	 * @param selectStatus
	 *            If <code>true</code> all caches are checked, if <code>false</code> all caches are unchecked.
	 */
	public void setSelectForAll(boolean selectStatus) {
		CacheHolder ch;
		selectionChanged = true;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			if (selectStatus) {
				if (ch.isVisible()) {
					ch.is_Checked = selectStatus; // set the ceckbox also for addi wpts
					if (ch.hasAddiWpt()) {
						CacheHolder addiWpt;
						int addiCount = ch.addiWpts.getCount();
						for (int j = 0; j < addiCount; j++) {
							addiWpt = (CacheHolder) ch.addiWpts.get(j);
							addiWpt.is_Checked = selectStatus;
						}
					}
				}
			} else /* selectStatus==false */{
				ch.is_Checked = selectStatus;
			}
		}
	}

	public int numCachesInArea; // only valid after calling getSourroundingArea

	public Area getSourroundingArea(boolean onlyOfSelected) {
		if (cacheDB == null || cacheDB.size() == 0) return null;
		CacheHolder ch;
		CWPoint topleft = null;
		CWPoint bottomright = null;
		numCachesInArea = 0;
		boolean isAddi = false;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			if (!onlyOfSelected || ch.is_Checked) {
				if (ch.getPos().isValid()) { // done: && ch.pos.latDec != 0 && ch.pos.lonDec != 0 TO-DO != 0 sollte rausgenommen werden sobald in der Liste vernünftig mit nicht gesetzten pos umgegangen wird
					isAddi = ch.isAddiWpt();
					// test for plausiblity of coordinates of Additional Waypoints: more then 1000 km away from main Waypoint is unplausible ->
					// ignore it //
					// && ch.mainCache != null is only necessary because the data base may be corrupted
					if (!isAddi || (isAddi && ch.mainCache != null && ch.getPos().getDistance(ch.mainCache.getPos()) < 1000)) {
						if (topleft == null) topleft = new CWPoint(ch.getPos());
						if (bottomright == null) bottomright = new CWPoint(ch.getPos());
						if (topleft.latDec < ch.getPos().latDec) topleft.latDec = ch.getPos().latDec;
						if (topleft.lonDec > ch.getPos().lonDec) topleft.lonDec = ch.getPos().lonDec;
						if (bottomright.latDec > ch.getPos().latDec) bottomright.latDec = ch.getPos().latDec;
						if (bottomright.lonDec < ch.getPos().lonDec) bottomright.lonDec = ch.getPos().lonDec;
						numCachesInArea++;
					}
				}
			}
		}
		if (topleft != null && bottomright != null)
			return new Area(topleft, bottomright);
		else
			return null;
	}

	/**
	 * Method to calculate bearing and distance of a cache in the index list.
	 * 
	 * @see CacheHolder
	 * @see Extractor
	 */
	public void updateBearingDistance() {
		CWPoint centerPoint = new CWPoint(Global.getPref().getCurCentrePt()); // Clone current centre to be sure
		int anz = cacheDB.size();
		CacheHolder ch;
		// Jetzt durch die CacheDaten schleifen
		while (--anz >= 0) {
			ch = cacheDB.get(anz); // This returns a pointer to the CacheHolder object
			ch.calcDistance(centerPoint);
		}
		// The following call is not very clean as it mixes UI with base classes
		// However, calling it from here allows us to recenter the
		// radar panel with only one call
		if (Global.mainTab != null) Global.mainTab.radarP.recenterRadar();
	} // updateBearingDistance

	/**
	 * Method to build the reference between addi wpt and main cache.
	 */
	public void buildReferences() {
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
			if (ch.isAddiWpt()) setAddiRef(ch);
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
	 * If <code>true</code> then the cache list will only display the caches that are result of a search.
	 * 
	 * @return <code>True</code> if list should only display search results
	 */
	public boolean showSearchResult() {
		return showSearchResult;
	}

	/**
	 * Sets parameter if cache list should only display the caches that are results of a search.
	 * 
	 * @param showSearchResult
	 *            <code>True</code>: List should only display search results.
	 */
	public void setShowSearchResult(boolean showSearchResult) {
		this.showSearchResult = showSearchResult;
	}

	public void setFilterAttr(long[] filterAttr) {
		this.notifyUnsavedChanges(filterAttr != this.getFilterAttr());
		this.currentFilter.setFilterAttr(filterAttr);
	}

	public long[] getFilterAttr() {
		return currentFilter.getFilterAttr();
	}

	public int getFilterAttrChoice() {
		return this.currentFilter.getFilterAttrChoice();
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

	public String getMinDistGC() {
		return minDistGC;
	}

	public String getDirectionGC() {
		return directionGC;
	}

	public int getGpxStyle() {
		return Convert.toInt(gpxStyle);
	}

	public int getGpxTarget() {
		return Convert.toInt(gpxTarget);
	}

	public int getGpxId() {
		return Convert.toInt(gpxId);
	}

	//
	public void setMinDistGC(String minDistGC) {
		this.notifyUnsavedChanges(!minDistGC.equals(this.minDistGC));
		this.minDistGC = minDistGC;
	}

	public void setDistGC(String distGC) {
		this.notifyUnsavedChanges(!distGC.equals(this.distGC));
		this.distGC = distGC;
	}

	public void setDirectionGC(String directionGC) {
		this.notifyUnsavedChanges(!directionGC.equals(this.directionGC));
		this.directionGC = directionGC;
	}

	public void setGpxStyle(String style) {
		this.notifyUnsavedChanges(!style.equals(this.gpxStyle));
		this.gpxStyle = style;
	}

	public void setGpxTarget(String target) {
		this.notifyUnsavedChanges(!target.equals(this.gpxTarget));
		this.gpxTarget = target;
	}

	public void setGpxId(String id) {
		this.notifyUnsavedChanges(!id.equals(this.gpxId));
		this.gpxId = id;
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
	 * 
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
			return ((CacheHolder) o1).getWayPoint().compareTo(((CacheHolder) o2).getWayPoint());
		}

	}
}
