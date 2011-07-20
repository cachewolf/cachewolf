/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

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

import CacheWolf.exp.Exporter;
import CacheWolf.exp.GarminMap;
import CacheWolf.navi.Metrics;

import com.stevesoft.ewe_pat.Regex;

import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.io.AsciiCodec;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.io.TextCodec;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * A class to hold information on a cache.<br>
 * Not all attributes are filled at once. You will have to look at other classes and methods to get more information.
 * 
 */
public class CacheHolder {
	protected static final String NOBEARING = "?";
	protected static final String EMPTY = "";

	/** Cachestatus is Found, Not found or a date in format yyyy-mm-dd hh:mm for found date */
	private String cacheStatus = EMPTY;
	/** The name of the waypoint, typically GC.... or OC.... or CW...... (can be any characters) */
	private String wayPoint = EMPTY;
	/** The name of the cache (short description) */
	private String cacheName = EMPTY;
	/** The alias of the owner */
	private String cacheOwner = EMPTY;
	/** The coordinates of the cache */
	public CWPoint pos = new CWPoint();
	/** The coordinates of the cache */
	private String LatLon = pos.toString();
	/** The date when the cache was hidden in format yyyy-mm-dd */
	private String dateHidden = EMPTY;
	/** The size of the cache (as per GC cache sizes Micro, Small, ....) */
	private byte cacheSize = CacheSize.CW_SIZE_NOTCHOSEN;
	/** The distance from the centre in km */
	public double kilom = -1;
	public double lastKilom = -2; // Cache last value
	public int lastMetric = -1; // Cache last metric
	public String lastDistance = ""; // Cache last distance
	/** The bearing N, NNE, NE, ENE ... from the current centre to this point */
	private String bearing = NOBEARING;
	/** The angle (0=North, 180=South) from the current centre to this point */
	public double degrees = 0;
	/** The difficulty of the cache from 1 to 5 in .5 incements */
	private byte hard = CacheTerrDiff.CW_DT_UNSET;
	/** The terrain rating of the cache from 1 to 5 in .5 incements */
	private byte terrain = CacheTerrDiff.CW_DT_UNSET;
	/** The cache type (@see CacheType for translation table) */
	private byte type;
	/** True if the cache has been archived */
	private boolean archived = false;
	/** True if the cache is available for searching */
	private boolean available = true;
	/** True if we own this cache */
	private boolean owned = false;
	/** True if we have found this cache */
	private boolean found = false;
	/** If this is true, the cache has been filtered (is currently invisible) */
	private boolean filtered = false;
	/** True if the number of logs for this cache has changed */
	private boolean log_updated = false;
	/** True if cache details have changed: longDescription, Hints, */
	private boolean cache_updated = false;
	/** True if the cache data is incomplete (e.g. an error occurred during spidering */
	private boolean incomplete = false;
	/** True if the cache is blacklisted */
	private boolean black = false;
	/** True if the cache is new */
	private boolean newCache = false;
	/** True if the cache is part of the results of a search */
	public boolean is_flaged = false;
	/** True if additional waypoints for this cache should be displayed regardless of the filter settings */
	private boolean showAddis = false;
	/** True if the cache has been selected using the tick box in the list view */
	public boolean is_Checked = false;
	/** The unique OC cache ID */
	private String ocCacheID = EMPTY;
	/** The number of times this cache has not been found (max. 5) */
	private byte noFindLogs = 0;
	/** Number of recommendations (from the opencaching logs) */
	private int numRecommended = 0;
	/** Number of Founds since start of recommendations system */
	private int numFoundsSinceRecommendation = 0;
	/** Recommendation score: calculated as rations numRecommended / numLogsSinceRecommendation * 100 */
	public int recommendationScore = 0;
	/** True if this cache has travelbugs */
	private boolean bugs = false;
	/** True if the cache description is stored in HTML format */
	private boolean html = true;
	/** List of additional waypoints associated with this waypoint */
	public Vector addiWpts = new Vector();
	/** in range is used by the route filter to identify caches in range of a segment */
	public boolean in_range = false;
	/** If this is an additional waypoint, this links back to the main waypoint */
	public CacheHolder mainCache;
	/** The date this cache was last synced with OC in format yyyyMMddHHmmss */
	private String lastSync = EMPTY;
	/** True if cache has solver entry */
	private boolean hasSolver = false;
	/** True if a note is entered for the cache */
	private boolean hasNote = false;
	private CacheHolderDetail details = null;
	/**
	 * When sorting the cacheDB this field is used. The relevant field is copied here and the sort is always done on this field to speed up the sorting process
	 */
	public String sort;
	private static StringBuffer sb = new StringBuffer(530); // Used in toXML()

	private long[] attributesBits = { 0l, 0l, 0l, 0l };

	private IconAndText iconAndTextWP = null;
	private int iconAndTextWPLevel = 0;

	static char decSep, notDecSep;
	static {
		decSep = MyLocale.getDigSeparator().charAt(0);
		notDecSep = (decSep == '.' ? ',' : '.');
	}

	public CacheHolder() {
		// do nothing
	}

	public CacheHolder(String wp) {
		wayPoint = wp;
		type = CacheType.CW_TYPE_ERROR;
	}

	public CacheHolder(String xmlString, int version) {
		int start, end;
		try {
			if (version == 1) {
				start = xmlString.indexOf('"');
				end = xmlString.indexOf('"', start + 1);
				setCacheName(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheOwner(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				// Assume coordinates are in decimal format
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lat = Convert.parseDouble(xmlString.substring(start + 1, end).replace(notDecSep, decSep));
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lon = Convert.parseDouble(xmlString.substring(start + 1, end).replace(notDecSep, decSep));
				pos = new CWPoint(lat, lon);
				LatLon = pos.toString();

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setDateHidden(xmlString.substring(start + 1, end));
				// Convert the US format to YYYY-MM-DD if necessary
				if (getDateHidden().indexOf('/') > -1)
					setDateHidden(DateFormat.toYYMMDD(getDateHidden()));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setWayPoint(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheStatus(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setType(CacheType.v1Converter((xmlString.substring(start + 1, end))));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (isAddiWpt() || isCustomWpt()) {
					setHard(CacheTerrDiff.CW_DT_UNSET);
				} else {
					try {
						setHard(CacheTerrDiff.v1Converter(xmlString.substring(start + 1, end)));
					} catch (IllegalArgumentException ex) {
						setHard(CacheTerrDiff.CW_DT_ERROR);
						setIncomplete(true);
						Global.getPref().log(wayPoint, ex, true);
					}
				}
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (isAddiWpt() || isCustomWpt()) {
					setTerrain(CacheTerrDiff.CW_DT_UNSET);
				} else {
					try {
						setTerrain(CacheTerrDiff.v1Converter(xmlString.substring(start + 1, end)));
					} catch (IllegalArgumentException ex) {
						setTerrain(CacheTerrDiff.CW_DT_ERROR);
						setIncomplete(true);
						Global.getPref().log(wayPoint, ex, true);
					}
				}
				// The next item was 'dirty' but this is no longer used.
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setFiltered(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (isAddiWpt() || isCustomWpt()) {
					setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
				} else {
					try {
						setCacheSize(CacheSize.v1Converter(xmlString.substring(start + 1, end)));
					} catch (IllegalArgumentException ex) {
						setCacheSize(CacheSize.CW_SIZE_ERROR);
						setIncomplete(true);
						Global.getPref().log(wayPoint, ex, true);
					}
				}
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setAvailable(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setArchived(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setHas_bugs(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setBlack(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setOwned(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setFound(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNew(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setLog_updated(xmlString.substring(start + 1, end).equals("true"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setUpdated(xmlString.substring(start + 1, end).equals("true"));
				// for backwards compatibility set value to true, if it is not in the file

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setHTML(!xmlString.substring(start + 1, end).equals("false"));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNoFindLogs((byte) Convert.toInt(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setOcCacheID(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setIncomplete(xmlString.substring(start + 1, end).equals("true") || incomplete);

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setLastSync(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumRecommended(Convert.toInt(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumFoundsSinceRecommendation(Convert.toInt(xmlString.substring(start + 1, end)));
				recommendationScore = LogList.getScore(getNumRecommended(), getNumFoundsSinceRecommendation());

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				long[] attribsBits = { 0l, 0l, 0l, 0l };
				if (start > -1 && end > -1) {
					attribsBits[0] = (Convert.parseLong(xmlString.substring(start + 1, end)));

					start = xmlString.indexOf('"', end + 1);
					end = xmlString.indexOf('"', start + 1);
					if (start > -1 && end > -1)
						attribsBits[2] = (Convert.parseLong(xmlString.substring(start + 1, end)));
				}
				setAttribsAsBits(attribsBits);
			} else if (version == 3 || version == 2) {
				start = xmlString.indexOf('"');
				end = xmlString.indexOf('"', start + 1);
				setCacheName(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheOwner(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				// Assume coordinates are in decimal format
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lat = Convert.parseDouble(xmlString.substring(start + 1, end).replace(notDecSep, decSep));
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lon = Convert.parseDouble(xmlString.substring(start + 1, end).replace(notDecSep, decSep));
				pos = new CWPoint(lat, lon);
				LatLon = pos.toString();

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setDateHidden(xmlString.substring(start + 1, end));
				// Convert the US format to YYYY-MM-DD if necessary
				if (getDateHidden().indexOf('/') > -1)
					setDateHidden(DateFormat.toYYMMDD(getDateHidden()));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setWayPoint(SafeXML.cleanback(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheStatus(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setOcCacheID(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setLastSync(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumRecommended(Convert.toInt(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumFoundsSinceRecommendation(Convert.toInt(xmlString.substring(start + 1, end)));
				recommendationScore = LogList.getScore(getNumRecommended(), getNumFoundsSinceRecommendation());

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				long[] attribsBits = { 0l, 0l, 0l, 0l };
				if (start > -1 && end > -1) {
					attribsBits[0] = (Convert.parseLong(xmlString.substring(start + 1, end)));

					start = xmlString.indexOf('"', end + 1);
					end = xmlString.indexOf('"', start + 1);
					if (start > -1 && end > -1)
						attribsBits[2] = (Convert.parseLong(xmlString.substring(start + 1, end)));
				}

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				this.long2boolFields(Convert.parseLong(xmlString.substring(start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (version == 2) {
					long2byteFieldsv2(Convert.parseLong(xmlString.substring(start + 1, end)));
				} else {
					long2byteFields(Convert.parseLong(xmlString.substring(start + 1, end)));
				}
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (start > -1 && end > -1) {
					attribsBits[1] = (Convert.parseLong(xmlString.substring(start + 1, end)));

					start = xmlString.indexOf('"', end + 1);
					end = xmlString.indexOf('"', start + 1);
					if (start > -1 && end > -1)
						attribsBits[3] = (Convert.parseLong(xmlString.substring(start + 1, end)));
				}
				setAttribsAsBits(attribsBits);
			}
			if (version < Profile.CURRENTFILEFORMAT) {
				// forceload of details, creates waypoint.xml if missing
				details = getCacheDetails(false);
				// make sure details get (re)written in new format
				details.hasUnsavedChanges = true;
				// update information on notes and solver info
				setHasNote(!details.getCacheNotes().equals(""));
				setHasSolver(!details.getSolver().equals(""));
			}
		} catch (Exception ex) {
			// Global.getPref().log("Ignored Exception in CacheHolder()", ex, true);
		}
	}

	/**
	 * Returns the distance in formatted output. Using kilometers when metric system is active, using miles when imperial system is active.
	 * 
	 * @return The current distance.
	 */
	public String getDistance() {
		String result = null;
		String newUnit = null;

		if (this.kilom == this.lastKilom && Global.getPref().metricSystem == this.lastMetric) {
			result = this.lastDistance;
		} else {
			if (this.kilom >= 0) {
				double newValue = 0;
				switch (Global.getPref().metricSystem) {
				case Metrics.IMPERIAL:
					newValue = Metrics.convertUnit(this.kilom, Metrics.KILOMETER, Metrics.MILES);
					newUnit = Metrics.getUnit(Metrics.MILES);
					break;
				case Metrics.METRIC:
				default:
					newValue = this.kilom;
					newUnit = Metrics.getUnit(Metrics.KILOMETER);
					break;
				}
				result = MyLocale.formatDouble(newValue, "0.00") + " " + newUnit;
			} else {
				result = "? " + (Global.getPref().metricSystem == Metrics.IMPERIAL ? Metrics.getUnit(Metrics.MILES) : Metrics.getUnit(Metrics.KILOMETER));
			}
			// Caching values, so reevaluation is only done when really needed
			this.lastKilom = this.kilom;
			this.lastMetric = Global.getPref().metricSystem;
			this.lastDistance = result;
		}
		return result;
	}

	/**
	 * Updates Cache information with information provided by cache given as argument. This is used to update the cache with the information retrieved from files or web: The argument cache is the one that is filled with the read information,
	 * <code>this</code> is the cache that is already in the database and subject to update.
	 * 
	 * @param ch
	 *            The cache who's information is updating the current one
	 * @param overwrite
	 *            If <code>true</code>, then <i>status</i>, <i>is_found</i> and <i>position</i> is updated, otherwise not.
	 */
	public void update(CacheHolder ch) {
		this.recommendationScore = ch.recommendationScore;
		this.setNumFoundsSinceRecommendation(ch.getNumFoundsSinceRecommendation());
		this.setNumRecommended(ch.getNumRecommended());
		boolean mayChangeCoordinates = !this.cacheStatus.startsWith(MyLocale.getMsg(362, "solved"));
		/*
		 * Here we have to distinguish several cases: this.is_found this ch Update 'this' (values are empty or yyyy-mm-dd) ---------------------------------------------------------------------- false any yyyy-mm-dd yes true "Found" yyyy-mm-dd yes true
		 * yyyy-mm-dd yyyy-mm-dd yes (or no) true yyyy-mm-dd hh:mm yyyy-mm-dd no
		 */
		if (!this.found || this.cacheStatus.indexOf(":") < 0) {
			// don't overwrite with empty data
			if (!ch.getCacheStatus().trim().equals("")) {
				this.setCacheStatus(ch.getCacheStatus());
			}
			this.setFound(ch.is_found());
		}
		// Don't overwrite valid coordinates with invalid ones
		if (ch.pos.isValid() || !this.pos.isValid()) {
			if (mayChangeCoordinates) {
				this.pos = ch.pos;
				this.LatLon = ch.LatLon;
			}
		}
		this.setWayPoint(ch.getWayPoint());
		this.setCacheName(ch.getCacheName());
		this.setCacheOwner(ch.getCacheOwner());

		this.setDateHidden(ch.getDateHidden());
		this.setCacheSize(ch.getCacheSize());
		this.kilom = ch.kilom;
		this.bearing = ch.bearing;
		this.degrees = ch.degrees;
		this.setHard(ch.getHard());
		this.setTerrain(ch.getTerrain());
		this.setType(ch.getType());
		this.setArchived(ch.is_archived());
		this.setAvailable(ch.is_available());
		this.setOwned(ch.is_owned());
		this.setFiltered(ch.is_filtered());
		this.setIncomplete(ch.is_incomplete());
		this.addiWpts = ch.addiWpts;
		this.mainCache = ch.mainCache;
		if (ch.getOcCacheID().length() > 0)
			this.setOcCacheID(ch.getOcCacheID());
		this.setNoFindLogs(ch.getNoFindLogs());
		this.setHas_bugs(ch.has_bugs());
		this.setHTML(ch.is_HTML());
		this.sort = ch.sort;
		this.setLastSync(ch.getLastSync());

		this.setAttribsAsBits(ch.getAttributesBits());
		if (ch.detailsLoaded()) {
			this.getCacheDetails(false).update(ch.getCacheDetails(false));
		}
	}

	/**
	 * Call it only when necessary, it takes time, because all logs must be parsed
	 */
	public void calcRecommendationScore() {
		// String pattern = getWayPoint().toUpperCase();
		if (isOC()) {
			// Calculate recommendation score only when details
			// are already loaded. When they aren't loaded, then we assume
			// that there is no change, so nothing to do.
			if (this.detailsLoaded()) {
				CacheHolderDetail chD = getCacheDetails(true);
				if (chD != null) {
					chD.CacheLogs.calcRecommendations();
					recommendationScore = chD.CacheLogs.recommendationRating;
					setNumFoundsSinceRecommendation(chD.CacheLogs.foundsSinceRecommendation);
					setNumRecommended(chD.CacheLogs.numRecommended);
				} else { // cache doesn't have details
					recommendationScore = -1;
					setNumFoundsSinceRecommendation(-1);
					setNumRecommended(-1);
				}
			}
		} else {
			recommendationScore = -1;
			setNumFoundsSinceRecommendation(-1);
			// setNumRecommended(-1);
		}
	}

	/** Return a XML string containing all the cache data for storing in index.xml */
	public String toXML() {
		calcRecommendationScore();
		sb.delete(0, sb.length());
		sb.append("    <CACHE ");
		sb.append(" name = \"");
		sb.append(SafeXML.clean(getCacheName()));
		sb.append("\" owner = \"");
		sb.append(SafeXML.clean(getCacheOwner()));
		sb.append("\" lat = \"");
		sb.append(pos.latDec);
		sb.append("\" lon = \"");
		sb.append(pos.lonDec);
		sb.append("\" hidden = \"");
		sb.append(getDateHidden());
		sb.append("\" wayp = \"");
		sb.append(SafeXML.clean(getWayPoint()));
		sb.append("\" status = \"");
		sb.append(getCacheStatus());
		sb.append("\" ocCacheID = \"");
		sb.append(getOcCacheID());
		sb.append("\" lastSyncOC = \"");
		sb.append(getLastSync());
		sb.append("\" num_recommended = \"");
		sb.append(Convert.formatInt(getNumRecommended()));
		sb.append("\" num_found = \"");
		sb.append(Convert.formatInt(getNumFoundsSinceRecommendation()));
		long[] attribsBits = getAttributesBits();
		sb.append("\" attributesYes = \"");
		sb.append(Convert.formatLong(attribsBits[0]));
		sb.append("\" attributesNo = \"");
		sb.append(Convert.formatLong(attribsBits[2]));
		sb.append("\" boolFields=\"");
		sb.append(Convert.formatLong(this.boolFields2long()));
		sb.append("\" byteFields=\"");
		sb.append(Convert.formatLong(this.byteFields2long()));
		sb.append("\" attributesYes1 = \"");
		sb.append(Convert.formatLong(attribsBits[1]));
		sb.append("\" attributesNo1 = \"");
		sb.append(Convert.formatLong(attribsBits[3]));
		sb.append("\" />\n");
		return sb.toString();
	}

	/** Return a Hashtable containing all the cache data for Templates */
	public Hashtable toHashtable(Regex decSep, Regex badChars, int shortWaypointLength, int shortNameLength, int nrOfLogs, TextCodec codec, GarminMap gm, boolean withFoundText, int ModTyp, String expName) {
		Hashtable varParams = new Hashtable();
		CacheHolderDetail det = this.getCacheDetails(false);
		varParams.put("PROGDIR", FileBase.getProgramDirectory());
		varParams.put("PROFILDIR", Global.getProfile().dataDir);
		varParams.put("ALIAS", Global.getPref().myAlias);
		varParams.put("TYPE", CacheType.type2TypeTag(type)); // <type>
		varParams.put("TYPENO", "" + type);
		varParams.put("SYM", CacheType.type2SymTag(type)); // <sym>
		varParams.put("GSTYPE", CacheType.type2GSTypeTag(type)); // <groundspeak:type>
		varParams.put("SHORTTYPE", CacheType.getExportShortId(type));
		if (isAddiWpt()) {
			CacheHolder ch = this.mainCache;
			varParams.put("MAINWP", ch.getWayPoint());
			varParams.put("MAINWPNAME", ch.getCacheName());
			varParams.put("DIFFICULTY", (ch.hard < 0) ? "!<!" : decSep.replaceAll(CacheTerrDiff.longDT(ch.hard)));
			String sHard = Integer.toString(ch.hard);
			varParams.put("SHORTDIFFICULTY", (ch.hard < 0) ? "" : sHard);
			varParams.put("SHDIFFICULTY", (ch.hard < 0) ? "" : sHard.substring(0, 1));
			varParams.put("TERRAIN", (ch.terrain < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(ch.terrain)));
			String sTerrain = Integer.toString(ch.terrain);
			varParams.put("SHORTTERRAIN", (ch.terrain < 0) ? "" : sTerrain);
			varParams.put("SHTERRAIN", (ch.terrain < 0) ? "" : sTerrain.substring(0, 1));
			varParams.put("SIZE", CacheSize.cw2ExportString(ch.cacheSize));
			varParams.put("SHORTSIZE", CacheSize.getExportShortId(ch.cacheSize));
			varParams.put("OWNER", (ModTyp == 0) ? SafeXML.cleanGPX(ch.cacheOwner) : ch.cacheOwner);
			varParams.put("DATE", ch.dateHidden);
		} else {
			varParams.put("MAINWP", "");
			varParams.put("MAINWPNAME", "");
			varParams.put("DIFFICULTY", (isCustomWpt() || hard < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(hard)));
			String sHard = Integer.toString(hard);
			varParams.put("SHORTDIFFICULTY", (isCustomWpt() || hard < 0) ? "" : sHard);
			varParams.put("SHDIFFICULTY", (isCustomWpt() || hard < 0) ? "" : sHard.substring(0, 1));
			varParams.put("TERRAIN", (isCustomWpt() || terrain < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(terrain)));
			String sTerrain = Integer.toString(terrain);
			varParams.put("SHORTTERRAIN", (isCustomWpt() || terrain < 0) ? "" : sTerrain);
			varParams.put("SHTERRAIN", (isCustomWpt() || terrain < 0) ? "" : sTerrain.substring(0, 1));
			varParams.put("SIZE", CacheSize.cw2ExportString(cacheSize));
			varParams.put("SHORTSIZE", CacheSize.getExportShortId(cacheSize));
			varParams.put("OWNER", (ModTyp == 0) ? SafeXML.cleanGPX(cacheOwner) : cacheOwner);
			varParams.put("DATE", dateHidden);
		}
		if (isCustomWpt()) {

		}
		varParams.put("WAYPOINT", wayPoint); // <name>
		int wpl = wayPoint.length();
		int wps = (wpl < shortWaypointLength) ? 0 : wpl - shortWaypointLength;
		varParams.put("SHORTWAYPOINT", wayPoint.substring(wps, wpl));
		varParams.put("DISTANCE", decSep.replaceAll(getDistance()));
		varParams.put("BEARING", bearing);
		if ((pos != null && pos.isValid())) {
			varParams.put("LATLON", decSep.replaceAll(LatLon));
			varParams.put("LAT", decSep.replaceAll(pos.getLatDeg(CWPoint.DD)));
			varParams.put("LON", decSep.replaceAll(pos.getLonDeg(CWPoint.DD)));
		} else {
			varParams.put("LATLON", "unknown");
			varParams.put("LAT", "");
			varParams.put("LON", "");
		}
		if (withFoundText) {
			varParams.put("STATUS", getStatusText());
		} else
			varParams.put("STATUS", cacheStatus);
		varParams.put("GC_LOGTYPE", getGCLogType());
		varParams.put("STATUS_DATE", getStatusDate());
		varParams.put("STATUS_TIME", getStatusTime());
		varParams.put("CACHE_NAME", cacheName);
		String cn = cacheName;
		if (codec instanceof AsciiCodec) {
			cn = Exporter.simplifyString(cacheName);
		} // use for "NAME"
		if (badChars != null) {
			cn = badChars.replaceAll(cacheName);
		} // use for "NAME"
		varParams.put("NAME", cn);
		String shortName = shortenName(cn, shortNameLength);
		varParams.put("SHORTNAME", shortName);
		varParams.put("TRAVELBUG", (bugs ? "Y" : "N"));
		if (gm != null)
			varParams.put("GMTYPE", gm.getIcon(this));
		varParams.put("NOW_DATE", nowdate().setToCurrentTime().toString());
		varParams.put("NOW_TIME", nowtime().setToCurrentTime().toString());
		varParams.put("CACHEID", GetCacheID());
		varParams.put("AVAILABLE", available ? "TRUE" : "FALSE");
		varParams.put("ARCHIVED", archived ? "TRUE" : "FALSE");
		varParams.put("HTML", html ? "TRUE" : "FALSE");
		varParams.put("VOTE", getRecommended());
		// () ? TRUE : FALSE
		if (det == null) {
			varParams.put("URL", "");
			varParams.put("DESCRIPTION", "");
			varParams.put("NOTES", "");
			varParams.put("HINTS", "");
			varParams.put("DECRYPTEDHINTS", "");
			varParams.put("COUNTRY", "");
			varParams.put("STATE", "");
		} else {
			varParams.put("URL", det.URL);
			if (html) {
				if (ModTyp == 0) {
					varParams.put("DESCRIPTION", SafeXML.cleanGPX(det.LongDescription));
				} else {
					varParams.put("DESCRIPTION", modifyLongDesc(det, ModTyp));
				}
			} else {
				// what was the reason? replace or no replace? I dont remember
				varParams.put("DESCRIPTION", STRreplace.replace(det.LongDescription, "\n", "<br>"));
			}

			if (badChars != null) {
				if (ModTyp == 0) {
					varParams.put("NOTES", badChars.replaceAll(det.getCacheNotes()));
				} else {
					varParams.put("NOTES", STRreplace.replace(badChars.replaceAll(det.getCacheNotes()), "\n", "<br>"));
				}
				varParams.put("HINTS", (ModTyp == 0) ? SafeXML.cleanGPX(badChars.replaceAll(det.Hints)) : badChars.replaceAll(det.Hints));
				varParams.put("DECRYPTEDHINTS", (ModTyp == 0) ? SafeXML.cleanGPX(badChars.replaceAll(Common.rot13(det.Hints))) : badChars.replaceAll(Common.rot13(det.Hints)));
			} else {
				if (ModTyp == 0) {
					varParams.put("NOTES", SafeXML.cleanGPX(det.getCacheNotes()));
				} else {
					varParams.put("NOTES", STRreplace.replace(det.getCacheNotes(), "\n", "<br>"));
				}
				varParams.put("HINTS", (ModTyp == 0) ? SafeXML.cleanGPX(det.Hints) : det.Hints);
				varParams.put("DECRYPTEDHINTS", (ModTyp == 0) ? SafeXML.cleanGPX(Common.rot13(det.Hints)) : Common.rot13(det.Hints));
			}
			if (det.Travelbugs.size() > 0)
				varParams.put("BUGS", (ModTyp == 0) ? SafeXML.cleanGPX(det.Travelbugs.toHtml()) : det.Travelbugs.toHtml());
			if (det.getSolver() != null && det.getSolver().trim().length() > 0)
				varParams.put("SOLVER", STRreplace.replace(det.getSolver(), "\n", "<br/>\n"));
			varParams.put("COUNTRY", det.Country);
			varParams.put("STATE", det.State);

			// attributes
			Vector attVect = new Vector(det.attributes.count() + 1);
			for (int i = 0; i < det.attributes.count(); i++) {
				Hashtable atts = new Hashtable();
				atts.put("PATHANDIMAGE", det.attributes.getAttribute(i).getPathAndImageName());
				atts.put("IMAGE", det.attributes.getAttribute(i).getImageName());
				atts.put("GCID", det.attributes.getAttribute(i).getGCId());
				atts.put("INC", "" + det.attributes.getAttribute(i).getInc());
				atts.put("INC2TXT", det.attributes.getAttribute(i).getInc() == 1 ? "YES:" : "NO:");
				if (i % 5 == 4)
					atts.put("BR", "<br/>");
				else
					atts.put("BR", "");
				atts.put("INFO", det.attributes.getAttribute(i).getMsg());
				atts.put("GCINFO", det.attributes.getAttribute(i).getGCText());
				attVect.add(atts);
			}
			varParams.put("ATTRIBUTES", attVect);

			// logs
			Vector logVect = new Vector(det.CacheLogs.size());
			int maxlogs = det.CacheLogs.size();
			if (nrOfLogs > -1 && nrOfLogs < maxlogs)
				maxlogs = nrOfLogs;
			for (int i = 0; i < maxlogs; i++) {
				Hashtable logs = new Hashtable();
				String stmp;
				if (det.CacheLogs.getLog(i).getIcon().equals("MAXLOG")) {
					logs.put("WAYPOINT", wayPoint);
					logs.put("ICON", "");
					logs.put("LOGTYPE", "");
					logs.put("DATE", "");
					logs.put("LOGGER", "");
					stmp = "<hr>" + MyLocale.getMsg(736, "Too many logs") + "<hr>";
				} else {
					logs.put("WAYPOINT", wayPoint);
					logs.put("ICON", det.CacheLogs.getLog(i).getIcon());
					logs.put("LOGTYPE", image2TypeText(det.CacheLogs.getLog(i).getIcon()));
					logs.put("DATE", det.CacheLogs.getLog(i).getDate());
					logs.put("LOGGER", (ModTyp == 0) ? SafeXML.cleanGPX(det.CacheLogs.getLog(i).getLogger()) : det.CacheLogs.getLog(i).getLogger());
					stmp = STRreplace.replace(det.CacheLogs.getLog(i).getMessage().trim(), "http://www.geocaching.com/images/icons/", null);
				}
				logs.put("MESSAGE", (ModTyp == 0) ? SafeXML.cleanGPX(stmp) : stmp);
				logVect.add(logs);
			}
			varParams.put("LOGS", logVect);

			Vector addiVect = new Vector(addiWpts.size());
			for (int i = 0; i < addiWpts.size(); i++) {
				Hashtable addis = new Hashtable();
				CacheHolder ch = (CacheHolder) addiWpts.get(i);
				addis.put("WAYPOINT", ch.getWayPoint());
				addis.put("NAME", (ModTyp == 0) ? SafeXML.cleanGPX(ch.getCacheName()) : ch.getCacheName());
				if ((ch.pos != null && ch.pos.isValid())) {
					addis.put("LATLON", decSep.replaceAll(ch.pos.toString()));
					addis.put("LAT", decSep.replaceAll(ch.pos.getLatDeg(CWPoint.DD)));
					addis.put("LON", decSep.replaceAll(ch.pos.getLonDeg(CWPoint.DD)));
				} else {
					addis.put("LATLON", "unknown");
					addis.put("LAT", "");
					addis.put("LON", "");
				}
				addis.put("IMG", CacheType.typeImageForId(ch.getType()));
				addis.put("ICON", "" + ch.getType());
				addis.put("TYPENAME", CacheType.type2Gui(ch.getType()));
				addis.put("TYPE", CacheType.type2TypeTag(ch.getType())); // <type>
				addis.put("SYM", CacheType.type2SymTag(ch.getType())); // <sym>
				addis.put("GSTYPE", CacheType.type2GSTypeTag(ch.getType())); // <groundspeak:type>
				addis.put("LONGDESC", (ModTyp == 0) ? SafeXML.cleanGPX(ch.getCacheDetails(false).LongDescription) : ch.getCacheDetails(false).LongDescription);
				addiVect.add(addis);
			}
			varParams.put("ADDIS", addiVect);

			Vector imgVect = new Vector(det.images.size());
			for (int i = 0; i < det.images.size(); i++) {
				Hashtable imgs = new Hashtable();
				String imgFile = det.images.get(i).getFilename();
				imgs.put("FILENAME", imgFile);
				imgs.put("TEXT", det.images.get(i).getTitle());
				imgs.put("COMMENT", det.images.get(i).getComment());
				imgs.put("URL", det.images.get(i).getURL());
				if (!expName.equals("")) {
					String src = Global.getProfile().dataDir + imgFile;
					String dest = Global.getPref().getExportPath(expName) + imgFile;
					if (!DataMover.copy(src, dest)) {
						Global.getPref().log("[CacheHolder:toHashtable]error copying " + imgFile + " to " + Global.getPref().getExportPath(expName));
					}
				}
				imgVect.add(imgs);
			}
			varParams.put("cacheImg", imgVect);
		}
		return varParams;
	}

	/**
	 * generate a gc.com compatible string representation of log derived from the internally stored image
	 * 
	 * @param image
	 *            name of the image to display
	 * @return log type. will default to "Write note" for unknown logtypes
	 */
	public static final String image2TypeText(String image) {
		if (image.equals("icon_smile.gif"))
			return "Found it";
		if (image.equals("icon_sad.gif"))
			return "Didn't find it";
		if (image.equals("icon_note.gif"))
			return "Write note";
		if (image.equals("icon_enabled.gif"))
			return "Enable Listing";
		if (image.equals("icon_disabled.gif"))
			return "Temporarily Disable Listing";
		if (image.equals("icon_camera.gif"))
			return "Webcam Photo Taken";
		if (image.equals("icon_attended.gif"))
			return "Attended";
		if (image.equals("icon_greenlight.gif"))
			return "Publish Listing";
		if (image.equals("icon_rsvp.gif"))
			return "Will Attend";
		if (image.equals("big_smile.gif"))
			return "Post Reviewer Note";
		if (image.equals("traffic_cone.gif"))
			return "Archive";
		if (image.equals("icon_maint.gif"))
			return "Owner Maintenance";
		if (image.equals("icon_needsmaint.gif"))
			return "Needs Maintenance";
		if (image.equals("coord_update.gif"))
			return "Update Coordinates";
		if (image.equals("icon_remove.gif"))
			return "Needs Archived";
		if (image.equals("icon_redlight.gif"))
			return "Retract Listing";
		return "unknown logtype " + image;
	}

	/**
	 * Modify the image links in the long description so that they point to image files in the local directory Also copy the image file to the target directory so that it can be displayed.
	 * 
	 * @param chD
	 *            CacheHolderDetail
	 * @param int ModTypLongDesc == 1 get image from profile path, == 2 get image from html-path
	 * @return The modified long description
	 */
	private String modifyLongDesc(CacheHolderDetail chD, int ModTypLongDesc) {
		StringBuffer s = new StringBuffer(chD.LongDescription.length());
		int start = 0;
		int pos;
		int imageNo = 0;
		String imgsrc = "";
		if (ModTypLongDesc == 1)
			imgsrc = "file://" + Global.getProfile().dataDir;
		while (start >= 0 && (pos = chD.LongDescription.indexOf("<img", start)) > 0) {
			if (imageNo >= chD.images.size())
				break;
			s.append(chD.LongDescription.substring(start, pos));
			start = chD.LongDescription.indexOf(">", pos) + 1;
			String oldurl = chD.images.get(imageNo).getURL();
			String imgString = chD.LongDescription.substring(pos, start);
			imgString = STRreplace.replace(imgString, "\n", "");
			imgString = STRreplace.replace(imgString, "\r", "");
			imgString = STRreplace.replace(imgString, "groundspeak", "geocaching");
			if (imgString.indexOf(oldurl) == -1) {
				if (oldurl.startsWith("http://")) {
					int i = oldurl.indexOf("/", 7) + 1;
					oldurl = oldurl.substring(i);
				}
			}
			String newurl = imgsrc + chD.images.get(imageNo).getFilename();
			s.append(STRreplace.replace(imgString, oldurl, newurl));
			imageNo++;
		}
		if (start >= 0)
			s.append(chD.LongDescription.substring(start));
		return s.toString();
	}

	private final static Time nowdate() {
		Time nd = new Time();
		return nd.setFormat("yyyy-MM-dd");
	}

	private final static Time nowtime() {
		Time nt = new Time();
		return nt.setFormat("HH:mm");
	}

	private final static String selbstLaute = "aeiouAEIOU";

	private final static String mitLauteKlein() {
		final StringBuffer lower = new StringBuffer(26);// region/language dependent ?
		for (int i = 97; i <= 122; i++) {
			lower.append((char) i);
		}
		return lower.toString();
	}

	public String shortenName(String Name, int maxLength) {
		String shortName = removeCharsfromString(Name, maxLength, selbstLaute);
		return removeCharsfromString(shortName, maxLength, mitLauteKlein());
	}

	private static String removeCharsfromString(String text, int MaxLength, String chars) {
		if (text == null)
			return null;
		int originalTextLength = text.length();
		int anzToRemove = originalTextLength - MaxLength;
		if (anzToRemove <= 0)
			return text;
		int anzRemoved = 0;
		StringBuffer sb = new StringBuffer(50);
		for (int i = originalTextLength - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (chars.indexOf(c) == -1) {
				sb.insert(0, c);
			} else {
				anzRemoved++;
				if (anzRemoved == anzToRemove) {
					sb.insert(0, text.substring(0, i));
					i = 0; // exit for
				}
			}
		}
		return sb.toString();
	}

	/** return true if waypoint is an additional waypoint of a cache */
	public boolean isAddiWpt() {
		return CacheType.isAddiWpt(type);
	}

	/** return true if waypoint is a custom waypoint */
	public boolean isCustomWpt() {
		return CacheType.isCustomWpt(type);
	}

	/** return true if waypoint is a cache main waypoint */
	public boolean isCacheWpt() {
		return CacheType.isCacheWpt(type);
	}

	/** return true waypoint has one or more additional waypoints */
	public boolean hasAddiWpt() {
		return addiWpts.getCount() > 0;
	}

	public boolean isOC() {
		return OC.isOC(wayPoint);
	}

	public void calcDistance(CWPoint toPoint) {
		if (pos.isValid()) {
			kilom = pos.getDistance(toPoint);
			degrees = toPoint.getBearing(pos);
			bearing = CWPoint.getDirection(degrees);
		} else {
			kilom = -1;
			bearing = NOBEARING;
		}
	}

	public void setAttributesFromMainCache() {
		CacheHolder mainCh = this.mainCache;
		this.setCacheOwner(mainCh.getCacheOwner());
		if (mainCh.is_found()) {
			if (!this.found) {
				this.setCacheStatus(mainCh.getCacheStatus());
				this.setFound(true);
			}
			// else addi is already found (perhaps at other time)
		} else {
			// there may be a found addi , so don't overwrite
			if ((this.getType() == CacheType.CW_TYPE_FINAL)) {
				this.setCacheStatus(mainCh.getCacheStatus());
				this.setFound(false);
			}
		}
		this.setArchived(mainCh.is_archived());
		this.setAvailable(mainCh.is_available());
		this.setBlack(mainCh.is_black());
		this.setOwned(mainCh.is_owned());
		this.setNew(mainCh.is_new());
	}

	public void setAttributesToAddiWpts() {
		if (this.hasAddiWpt()) {
			CacheHolder addiWpt;
			for (int i = this.addiWpts.getCount() - 1; i >= 0; i--) {
				addiWpt = (CacheHolder) this.addiWpts.get(i);
				addiWpt.setAttributesFromMainCache();
			}
		}
	}

	/**
	 * True if ch and this belong to the same main cache.
	 * 
	 * @param ch
	 * @return
	 */
	public boolean hasSameMainCache(CacheHolder ch) {
		if (this == ch)
			return true;
		if (ch == null)
			return false;
		if ((!this.isAddiWpt()) && (!ch.isAddiWpt()))
			return false;
		CacheHolder main1, main2;
		if (this.isAddiWpt())
			main1 = this.mainCache;
		else
			main1 = this;
		if (ch.isAddiWpt())
			main2 = ch.mainCache;
		else
			main2 = ch;
		return main1 == main2;
	}

	/**
	 * Find out of detail object of Cache is loaded. Returns <code>true</code> if this is the case.
	 * 
	 * @return True when details object is present
	 */
	public boolean detailsLoaded() {
		return details != null;
	}

	/**
	 * Gets the detail object of a cache. The detail object stores information which is not needed for every cache instantaneously, but can be loaded if the user decides to look at this cache. If the cache object is already existing, the method will
	 * return this object, otherwise it will create it and try to read it from the corresponding <waypoint>.xml file. Depending on the parameters it is allowed that the <waypoint>.xml file does not yet exist, or the user is warned that the file doesn't
	 * exist. If more than <code>maxdetails</code> details are loaded, then the 5 last recently loaded caches are unloaded (to save ram).
	 * 
	 * @param alarmuser
	 *            If true an error message will be displayed to the user, if the details could not be read, and the method returns null
	 * @return The respective CacheHolderDetail, or null
	 */
	public CacheHolderDetail getCacheDetails(boolean alarmuser) {
		if (details == null) {
			details = new CacheHolderDetail(this);
			try {
				details.readCache(Global.getProfile().dataDir);
			} catch (IOException e) {
				if (alarmuser) {
					// FIXME: put a message to languages file
					(new MessageBox(MyLocale.getMsg(31415, "Error"), MyLocale.getMsg(31415, "Could not read cache details for cache: ") + this.getWayPoint(), FormBase.OKB)).execute();
					// details = null;
				}
				this.setIncomplete(true);
			}
			// for importing/spidering reasons helper objects with same waypoint are created
			if (details != null && !cachesWithLoadedDetails.contains(this)) {
				cachesWithLoadedDetails.add(this);
				if (cachesWithLoadedDetails.size() >= Global.getPref().maxDetails)
					removeOldestDetails();
			}
		}
		return details;
	}

	/**
	 * Saves the cache to the corresponding <waypoint>.xml file, located in the profiles directory. The waypoint of the cache should be set to do so.
	 */
	public void save() {
		checkIncomplete();
		this.getCacheDetails(false).saveCacheDetails(Global.getProfile().dataDir);
	}

	void releaseCacheDetails() {
		if (details != null && details.hasUnsavedChanges) {
			details.saveCacheDetails(Global.getProfile().dataDir);
		}
		details = null;
		cachesWithLoadedDetails.remove(this);
	}

	// final static int maxDetails = 50;
	static Vector cachesWithLoadedDetails = new Vector(Global.getPref().maxDetails);

	public static void removeOldestDetails() {
		CacheHolder ch;
		for (int i = 0; i < Global.getPref().deleteDetails; i++) {
			// String wp = (String) cachesWithLoadedDetails.get(i);
			// CacheHolder ch = Global.getProfile().cacheDB.get(wp);
			ch = (CacheHolder) cachesWithLoadedDetails.get(i);
			if (ch != null)
				ch.releaseCacheDetails();
		}
	}

	public static void removeAllDetails() {
		CacheHolder ch;
		for (int i = cachesWithLoadedDetails.size() - 1; i >= 0; i--) {
			// String wp = (String) cachesWithLoadedDetails.get(i);
			// CacheHolder ch = Global.getProfile().cacheDB.get(wp);
			ch = (CacheHolder) cachesWithLoadedDetails.get(i);
			if (ch != null && ch.detailsLoaded())
				ch.releaseCacheDetails();
		}
	}

	/**
	 * when importing caches you can set details.saveChanges = true when the import is finished call this method to save the pending changes
	 */
	public static void saveAllModifiedDetails() {
		CacheHolder ch;
		CacheHolderDetail chD;
		for (int i = cachesWithLoadedDetails.size() - 1; i >= 0; i--) {
			// String wp = (String) cachesWithLoadedDetails.get(i);
			// ch = Global.getProfile().cacheDB.get(wp);
			ch = (CacheHolder) cachesWithLoadedDetails.get(i);
			if (ch != null) {
				chD = ch.getCacheDetails(true);
				if (chD != null && chD.hasUnsavedChanges) {
					// ch.calcRecommendationScore();
					chD.saveCacheDetails(Global.getProfile().dataDir);
				}
			}
		}
	}

	public String getFoundText() {
		int msgNr = 318; // normal found
		if (type == CacheType.CW_TYPE_WEBCAM) {
			msgNr = 361;
		} else if (type == CacheType.CW_TYPE_EVENT || type == CacheType.CW_TYPE_MEGA_EVENT) {
			msgNr = 355;
		}
		return MyLocale.getMsg(msgNr, "Found");
	}

	public String getGCFoundText() {
		int msgNr = 318; // normal found
		if (type == CacheType.CW_TYPE_WEBCAM) {
			msgNr = 361;
		} else if (type == CacheType.CW_TYPE_EVENT || type == CacheType.CW_TYPE_MEGA_EVENT) {
			msgNr = 355;
		}
		for (int i = 0; i < _logType.length; i++) {
			if (("" + msgNr).equals(_logType[i][MSG_NR])) {
				return _logType[i][GC_MSG];
			}
		}
		return "";
	}

	public String getCWLogText(String s) {
		for (int i = 0; i < _logType.length; i++) {
			if ((s).equals(_logType[i][GC_MSG])) {
				return MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), "");
			}
		}
		return "";
	}

	public String getStatusText() {
		if ((cacheStatus.length() == 10 || cacheStatus.length() == 16) && cacheStatus.charAt(4) == '-') {
			return getFoundText() + " " + cacheStatus;
		} else {
			if (found) {
				return getFoundText();
			} else {
				return cacheStatus;
			}
		}
	}

	public String getStatusDate() {
		String statusDate = "";

		if (is_found() || getCacheStatus().indexOf(MyLocale.getMsg(319, "not found")) > 10) {
			Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
			rexDate.search(getCacheStatus());
			if (rexDate.stringMatched(1) != null) {
				statusDate = rexDate.stringMatched(1);
			}
		}

		return statusDate;
	}

	public String getStatusTime() {
		String statusTime = "";

		if (is_found() || getCacheStatus().indexOf(MyLocale.getMsg(319, "not found")) > 10) {
			Regex rexTime = new Regex("([0-9]{1,2}:[0-9]{2})");
			rexTime.search(getCacheStatus());
			if (rexTime.stringMatched(1) != null) {
				statusTime = rexTime.stringMatched(1);
			} else {
				Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
				rexDate.search(getCacheStatus());
				if (rexDate.stringMatched(1) != null) {
					statusTime = "00:00";
				}
			}
		}

		return statusTime;
	}

	public String GetCacheID() {
		String result = "";
		String pattern = wayPoint.toUpperCase();

		if (pattern.startsWith("GC")) {
			int gcId = 0;

			String sequence = "0123456789ABCDEFGHJKMNPQRTVWXYZ";

			String rightPart = getWayPoint().substring(2).toUpperCase();

			int base = 31;
			if ((rightPart.length() < 4) || (rightPart.length() == 4 && sequence.indexOf(rightPart.charAt(0)) < 16)) {
				base = 16;
			}

			for (int p = 0; p < rightPart.length(); p++) {
				gcId *= base;
				gcId += sequence.indexOf(rightPart.charAt(p));
			}

			if (base == 31) {
				gcId += java.lang.Math.pow(16, 4) - 16 * java.lang.Math.pow(31, 3);
			}

			result = Integer.toString(gcId);
		} else if (isOC()) {
			result = getOcCacheID();
		}

		return result;
	}

	private final static int MSG_NR = 0;
	private final static int GC_MSG = 1;
	private final static int IDX_WRITENOTE = 5;
	private final static String[][] _logType = { { "353", "" }, { "318", "Found it" }, { "355", "Attended" }, { "361", "Webcam Photo Taken" }, { "319", "Didn't find it" },
			{ "314", "Write note" }, // at change do change IDX_WRITENOTE = 5;
			{ "315", "Needs Archived" }, { "316", "Needs Maintenance" }, { "317", "Search" }, { "354", "Will Attend" }, { "320", "Owner" }, { "359", "Owner Maintenance" }, { "356", "Temporarily Disable Listing" }, { "357", "Enable Listing" },
			{ "358", "Post Reviewer Note" }, { "362", "Solved" }, { "313", "Flag 1" }, { "360", "Flag 2" }, };

	public final static String[] GetGuiLogTypes() {
		String[] ret = new String[_logType.length];
		for (int i = 0; i < _logType.length; i++) {
			ret[i] = MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), "");
		}
		return ret;
	}

	public String getGCLogType() {
		String gcLogType = _logType[IDX_WRITENOTE][GC_MSG];
		if (is_found()) {
			String msgNr = "318"; // normal found
			if (type == CacheType.CW_TYPE_WEBCAM) {
				msgNr = "361";
			} else if (type == CacheType.CW_TYPE_EVENT || type == CacheType.CW_TYPE_MEGA_EVENT) {
				msgNr = "355";
			}
			for (int i = 1; i < _logType.length; i++) {
				if (_logType[i][MSG_NR].equals(msgNr)) {
					gcLogType = _logType[i][GC_MSG];
					break;
				}
			}
		} else {
			String CacheStatus = getCacheStatus();
			for (int i = 1; i < _logType.length; i++) {
				if (CacheStatus.endsWith(MyLocale.getMsg(Common.parseInt(_logType[i][MSG_NR]), ""))) {
					gcLogType = _logType[i][GC_MSG];
					break;
				}
			}
		}
		return gcLogType;
	}

	/**
	 * Initializes the caches states (and its addis) before updating, so that the "new", "updated", "log_updated" and "incomplete" properties are properly set.
	 * 
	 * @param pNewCache
	 *            <code>true</code> if it is a new cache (i.e. a cache not existing in CacheDB), <code>false</code> otherwise.
	 */
	public void initStates(boolean pNewCache) {
		this.setNew(pNewCache);
		this.setUpdated(false);
		this.setLog_updated(false);
		this.setIncomplete(false);
		if (!pNewCache && this.hasAddiWpt()) {
			for (int i = 0; i < this.addiWpts.size(); i++) {
				((CacheHolder) this.addiWpts.get(i)).initStates(pNewCache);
			}
		}
	}

	/**
	 * Creates a bit field of boolean values of the cache, represented as a long value. Boolean value of <code>true</code> results in <code>1</code> in the long values bits, and, vice versa, 0 for false.
	 * 
	 * @return long value representing the boolean bit field
	 */
	private long boolFields2long() {
		// To get the same list of visible caches after loading a profile,
		// the property isVisible() is saved instead of is_filtered(), but at
		// the place where is_filtered() is read.
		long value = bool2BitMask(!this.isVisible(), 1) | bool2BitMask(this.is_available(), 2) | bool2BitMask(this.is_archived(), 3) | bool2BitMask(this.has_bugs(), 4) | bool2BitMask(this.is_black(), 5) | bool2BitMask(this.is_owned(), 6)
				| bool2BitMask(this.is_found(), 7) | bool2BitMask(this.is_new(), 8) | bool2BitMask(this.is_log_updated(), 9) | bool2BitMask(this.is_updated(), 10) | bool2BitMask(this.is_HTML(), 11) | bool2BitMask(this.is_incomplete(), 12)
				| bool2BitMask(this.hasNote(), 13) | bool2BitMask(this.hasSolver(), 14);
		return value;
	}

	/**
	 * Creates a field of byte values of certain properties of the cache, represented as a long value. As a long is 8 bytes wide, one might pack 8 bytes into a long, one every 8 bits. The position indicates the group of bits where the byte is packed,
	 * counting starting from one by the right side of the long.
	 * 
	 * @return long value representing the byte field
	 */
	private long byteFields2long() {
		long value = byteBitMask(hard, 1) | byteBitMask(terrain, 2) | byteBitMask(type, 3) | byteBitMask(cacheSize, 4) | byteBitMask(this.noFindLogs, 5);
		return value;
	}

	/**
	 * Evaluates byte values from a long value for certain properties of the cache.
	 * 
	 * @param value
	 *            The long value which contains up to 8 bytes.
	 */
	private void long2byteFields(long value) {
		setHard(byteFromLong(value, 1));
		setTerrain(byteFromLong(value, 2));
		setType(byteFromLong(value, 3));
		setCacheSize(byteFromLong(value, 4));
		setNoFindLogs((byteFromLong(value, 5)));

		if (getHard() == CacheTerrDiff.CW_DT_ERROR || getTerrain() == CacheTerrDiff.CW_DT_ERROR || getCacheSize() == CacheSize.CW_SIZE_ERROR || getType() == CacheType.CW_TYPE_ERROR) {
			setIncomplete(true);
		}
	}

	/**
	 * convert a v2 byte filed to the current structures
	 * 
	 * @param value
	 */
	private void long2byteFieldsv2(long value) {
		setHard(byteFromLong(value, 1));
		setTerrain(byteFromLong(value, 2));
		setType(CacheType.v2Converter(byteFromLong(value, 3)));
		setCacheSize(byteFromLong(value, 4));
		setNoFindLogs((byteFromLong(value, 5)));
		if (getHard() == -1 || getTerrain() == -1 || getCacheSize() == -1) {
			if (isAddiWpt() || isCustomWpt()) {
				// Addis don't have their own values for difficulty, terrain and size
				// Custom waypoints can't be updated to remove incomplete flag, so we
				// have to set reasonable values.
				if (getHard() == CacheTerrDiff.CW_DT_ERROR)
					setHard(CacheTerrDiff.CW_DT_UNSET);
				if (getTerrain() == CacheTerrDiff.CW_DT_ERROR)
					setTerrain(CacheTerrDiff.CW_DT_UNSET);
				if (getCacheSize() == CacheSize.CW_SIZE_ERROR)
					setCacheSize(CacheSize.CW_SIZE_NONE);
			} else {
				setIncomplete(true);
			}
		}
	}

	/**
	 * Extracts a byte from a long value. The position is the number of the 8-bit block of the long (which contains 8 8-bit blocks), counted from 1 to 8, starting from the right side of the long.
	 * 
	 * @param value
	 *            The long value which contains the bytes
	 * @param position
	 *            The position of the byte, from 1 to 8
	 * @return The decoded byte value
	 */
	private byte byteFromLong(long value, int position) {
		byte b = -1; // = 11111111
		return (byte) ((value & this.byteBitMask(b, position)) >>> (position - 1) * 8);
	}

	/**
	 * Evaluates boolean values from a long value, which is seen as bit field.
	 * 
	 * @param value
	 *            The bit field as long value
	 */
	private void long2boolFields(long value) {
		this.setFiltered((value & this.bool2BitMask(true, 1)) != 0);
		this.setAvailable((value & this.bool2BitMask(true, 2)) != 0);
		this.setArchived((value & this.bool2BitMask(true, 3)) != 0);
		this.setHas_bugs((value & this.bool2BitMask(true, 4)) != 0);
		this.setBlack((value & this.bool2BitMask(true, 5)) != 0);
		this.setOwned((value & this.bool2BitMask(true, 6)) != 0);
		this.setFound((value & this.bool2BitMask(true, 7)) != 0);
		this.setNew((value & this.bool2BitMask(true, 8)) != 0);
		this.setLog_updated((value & this.bool2BitMask(true, 9)) != 0);
		this.setUpdated((value & this.bool2BitMask(true, 10)) != 0);
		this.setHTML((value & this.bool2BitMask(true, 11)) != 0);
		this.setIncomplete(((value & this.bool2BitMask(true, 12)) != 0) || this.is_incomplete());
		this.setHasNote((value & this.bool2BitMask(true, 13)) != 0);
		this.setHasSolver((value & this.bool2BitMask(true, 14)) != 0);
	}

	/**
	 * Represents a bit mask as long value for a boolean value which is saved at a specified position in the long field.
	 * 
	 * @param value
	 *            The boolean value we want to code
	 * @param position
	 *            Position of the value in the bit mask
	 * @return The corresponding bit mask: A long value where all bits are set to 0 except for the one we like to represent: This is 1 if the value is true, 0 if not.
	 */
	private long bool2BitMask(boolean value, int position) {
		if (value) {
			return (1L << (position - 1));
		} else {
			return 0L;
		}
	}

	/**
	 * Coding a long field which has only the bits of the byte value set. The position is the number (from 1 to 8) of the byte block which is used from the long.
	 * 
	 * @param value
	 *            Byte to encode
	 * @param position
	 *            Position of the byte value in the long
	 * @return Encoded byte value as long
	 */
	private long byteBitMask(byte value, int position) {
		long result = (0xFF & (long) value) << ((position - 1) * 8);
		return result;
	}

	/**
	 * Returns <code>true</code> if the waypoint should appear in the cache list, <code>false</code> if it should not appear.<br>
	 * The method takes into account blacklist, filters, search results - everything that determines if a cache is visible in the list or not.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		Profile profile = Global.getProfile();
		int filter = profile.getFilterActive();
		boolean noShow = ((profile.showBlacklisted() != this.is_black()) || (profile.showSearchResult() && !this.is_flaged) || ((filter == Filter.FILTER_ACTIVE || filter == Filter.FILTER_MARKED_ONLY) && (this.is_filtered()) ^ profile.isFilterInverted()) || (filter == Filter.FILTER_CACHELIST)
				&& !Global.mainForm.cacheList.contains(this.getWayPoint()));
		boolean showAddi = this.showAddis() && this.mainCache != null && this.mainCache.isVisible();
		noShow = noShow && !showAddi;
		return !noShow;
	}

	// Getter and Setter for private properties

	public String getLatLon() {
		return LatLon;
	}

	public void setLatLon(String _LatLon) {
		_LatLon = _LatLon.trim();
		if (!_LatLon.equals(LatLon.trim()))
			setUpdated(true);
		LatLon = _LatLon;
		pos.set(_LatLon);
	}

	public String getBearing() {
		return bearing;
	}

	/**
	 * Gets an IconAndText object for the cache. If the level of the Icon is equal to the last call of the method, the same (cached) object is returned. If the object is null or the level is different, a new object is created.<br>
	 * 
	 * @param level
	 *            4=is_incomplete(), 3=is_new(), 2=is_updated(), 1=is_log_updated
	 * @param fm
	 *            Font metrics
	 * @return New or old IconAndText object
	 */
	public IconAndText getIconAndTextWP(int level, FontMetrics fm) {
		if (level != iconAndTextWPLevel || iconAndTextWP == null || !iconAndTextWP.text.equals(wayPoint)) {
			switch (level) {
			case 4:
				iconAndTextWP = new IconAndText(CacheType.getTypeImage(CacheType.CW_TYPE_ERROR), wayPoint, fm);
				break;
			case 3:
				iconAndTextWP = new IconAndText(myTableModel.yellow, wayPoint, fm);
				break;
			case 2:
				iconAndTextWP = new IconAndText(myTableModel.red, wayPoint, fm);
				break;
			case 1:
				iconAndTextWP = new IconAndText(myTableModel.blue, wayPoint, fm);
				break;
			}
			iconAndTextWPLevel = level;
		}
		return iconAndTextWP;
	}

	public String getCacheStatus() {
		return cacheStatus;
	}

	public void setCacheStatus(String cacheStatus) {
		if (!cacheStatus.equals(this.cacheStatus)) {
			this.cacheStatus = cacheStatus;
			Global.getProfile().notifyUnsavedChanges(true);
			if ((this.getType() == CacheType.CW_TYPE_FINAL) && (this.mainCache != null)) {
				this.mainCache.setCacheStatus(this.getCacheStatus());
				// change the addi's in setFound
			}
		}
	}

	public String getWayPoint() {
		return wayPoint;
	}

	public void setWayPoint(String wayPoint) {
		Global.getProfile().notifyUnsavedChanges(!wayPoint.equals(this.wayPoint));
		this.wayPoint = wayPoint;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		Global.getProfile().notifyUnsavedChanges(!cacheName.equals(this.cacheName));
		this.cacheName = cacheName;
	}

	public String getCacheOwner() {
		return cacheOwner;
	}

	public void setCacheOwner(String cacheOwner) {
		Global.getProfile().notifyUnsavedChanges(!cacheOwner.equals(this.cacheOwner));
		this.cacheOwner = cacheOwner;
	}

	public String getDateHidden() {
		return dateHidden;
	}

	public void setDateHidden(String dateHidden) {
		Global.getProfile().notifyUnsavedChanges(!dateHidden.equals(this.dateHidden));
		this.dateHidden = dateHidden;
	}

	public byte getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(byte cacheSize) {
		Global.getProfile().notifyUnsavedChanges(cacheSize != this.cacheSize);
		this.cacheSize = cacheSize;
	}

	public byte getHard() {
		return hard;
	}

	public void setHard(byte hard) {
		Global.getProfile().notifyUnsavedChanges(hard != this.hard);
		this.hard = hard;
	}

	public byte getTerrain() {
		return terrain;
	}

	public void setTerrain(byte terrain) {
		Global.getProfile().notifyUnsavedChanges(terrain != this.terrain);
		this.terrain = terrain;
	}

	/**
	 * Gets the type of cache as integer.
	 * 
	 * @return Cache type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * Sets the type of the cache. As the cache type values are int for the rest of CacheWolf and byte internally of CacheHolder, some conversion has to be done.
	 * 
	 * @param typeId
	 *            Cache Type
	 */
	public void setType(byte typeId) {
		Global.getProfile().notifyUnsavedChanges(typeId != type);
		type = typeId;
	}

	public boolean is_archived() {
		return archived;
	}

	public void setArchived(boolean is_archived) {
		Global.getProfile().notifyUnsavedChanges(is_archived != this.archived);
		this.archived = is_archived;
		if (this.archived) {
			this.available = false;
		}
	}

	public boolean is_available() {
		return available;
	}

	public void setAvailable(boolean is_available) {
		Global.getProfile().notifyUnsavedChanges(is_available != this.available);
		this.available = is_available;
		if (this.available) {
			this.archived = false;
		}
	}

	public boolean is_owned() {
		return owned;
	}

	public void setOwned(boolean is_owned) {
		Global.getProfile().notifyUnsavedChanges(is_owned != this.owned);
		this.owned = is_owned;
	}

	public boolean is_found() {
		return found;
	}

	public void setFound(boolean is_found) {
		if (is_found != this.found) {
			Global.getProfile().notifyUnsavedChanges(true);
			this.found = is_found;
			if ((this.getType() == CacheType.CW_TYPE_FINAL) && (this.mainCache != null)) {
				this.mainCache.setFound(is_found);
				// done in setCacheStatus this.mainCache.setCacheStatus(this.getCacheStatus());
				// so setFound should be called after setCacheStatus
				if (is_found)
					this.mainCache.setAttributesToAddiWpts();
			}
		}
	}

	/**
	 * If this returns <code>true</code>, then the additional waypoints for this cache should be displayed regardless how the filter is set. If it is <code>false</code>, then the normal filter settings apply.<br>
	 * This property is not saved in index.xml, so if you reload the data, then this information is gone.
	 * 
	 * @return <code>True</code>: Always display additional waypoints for cache.
	 */
	public boolean showAddis() {
		return this.showAddis;
	}

	/**
	 * Setter for <code>showAddis()</code>. If this returns <code>true</code>, then the additional waypoints for this cache should be displayed regardless how the filter is set. If it is <code>false</code>, then the normal filter settings apply.<br>
	 * This property is not saved in index.xml, so if you reload the data, then this information is gone.
	 * 
	 * @param value
	 *            <code>True</code>: Always display additional waypoints for cache.
	 */
	public void setShowAddis(boolean value) {
		// This value is always stored in the main cache and all addis.
		CacheHolder mc = null;
		if (this.mainCache == null) {
			mc = this;
		} else {
			mc = this.mainCache;
		}
		if (mc.showAddis != value) {
			mc.showAddis = value;
			for (int i = 0; i < mc.addiWpts.size(); i++) {
				CacheHolder ac = (CacheHolder) mc.addiWpts.get(i);
				ac.showAddis = value;
			}
		}
	}

	/**
	 * <b><u>Important</u></b>: This flag no longer indicates if a cache is visible in the list. Instead, it now <u>only</u> flags if the cache is filtered out by filter criteria. Use <code>isVisible()</code> instead.<br>
	 * This property is affected by the following features:
	 * <ul>
	 * <li>"Defining and applying" a filter</li>
	 * <li>Filtering out checked or unchecked caches</li>
	 * </ul>
	 * It is <u>not</u> affected by:
	 * <ul>
	 * <li>Inverting a filter</li>
	 * <li>Removing a filter</li>
	 * <li>Applying a filter</li>
	 * <li>Applying a cache tour filter</li>
	 * <li>Switching between normal view and blacklist view</li>
	 * <li>Performing searches</li>
	 * <li>Anything else that isn't directly connected to filters in it's proper sense.</li>
	 * </ul>
	 * The new method for deciding if a cache is visible or not is <code>isVisible()
	 * </code>.
	 * 
	 * @return <code>True</code> if filter criteria are matched
	 */
	public boolean is_filtered() {
		return filtered;
	}

	public void setFiltered(boolean is_filtered) {
		Global.getProfile().notifyUnsavedChanges(is_filtered != this.filtered);
		this.filtered = is_filtered;
	}

	public boolean is_log_updated() {
		return log_updated;
	}

	public void setLog_updated(boolean is_log_updated) {
		Global.getProfile().notifyUnsavedChanges(is_log_updated != this.log_updated);
		if (is_log_updated && iconAndTextWPLevel == 1)
			iconAndTextWP = null;
		this.log_updated = is_log_updated;
	}

	public boolean is_updated() {
		return cache_updated;
	}

	public void setUpdated(boolean is_updated) {
		Global.getProfile().notifyUnsavedChanges(is_updated != this.cache_updated);
		if (is_updated && iconAndTextWPLevel == 2)
			iconAndTextWP = null;
		this.cache_updated = is_updated;
	}

	public boolean is_incomplete() {
		return incomplete;
	}

	public void setIncomplete(boolean is_incomplete) {
		Global.getProfile().notifyUnsavedChanges(is_incomplete != this.incomplete);
		if (is_incomplete && iconAndTextWPLevel == 4)
			iconAndTextWP = null;
		this.incomplete = is_incomplete;
	}

	/** checks the waypoint data integrity to set a warning flag if something is missing */
	public boolean checkIncomplete() {
		// TODO: discuss if we should only check cache waypoints and silently "fix" everything else
		boolean ret;
		if (isCacheWpt()) {
			if (getWayPoint().length() < 3 || getHard() < CacheTerrDiff.CW_DT_UNSET || getTerrain() < CacheTerrDiff.CW_DT_UNSET || getCacheSize() == CacheSize.CW_SIZE_ERROR || getCacheOwner().length() == 0 || getDateHidden().length() == 0
					|| getCacheName().length() == 0)
				ret = true;
			else
				ret = false;
		} else if (isAddiWpt()) {
			// FIXME: do not check for mainCache == null, since it will be null during initial import
			// FIXME: find out why we only check waypoints with IDs of a certain length ???
			// if (mainCache == null
			// || getHard() != CacheTerrDiff.CW_DT_UNSET
			if (getHard() != CacheTerrDiff.CW_DT_UNSET || getCacheSize() != CacheSize.CW_SIZE_NOTCHOSEN || getTerrain() != CacheTerrDiff.CW_DT_UNSET || getWayPoint().length() < 3
			// || getCacheOwner().length() > 0
			// || getDateHidden().length() > 0
					|| getCacheName().length() == 0)
				ret = true;
			else
				ret = false;
		} else if (isCustomWpt()) {
			if (getHard() != CacheTerrDiff.CW_DT_UNSET || getTerrain() != CacheTerrDiff.CW_DT_UNSET || getCacheSize() != CacheSize.CW_SIZE_NOTCHOSEN || getWayPoint().length() < 3
			// || getCacheOwner().length() > 0
			// || getDateHidden().length() > 0
			// || getCacheName().length() == 0
			)
				ret = true;
			else
				ret = false;
		} else {
			// we should not get here, so let's set a warning just in case
			ret = true;
		}
		setIncomplete(ret);
		return ret;
	}

	/**
	 * Determines if the blacklist status is set for the cache. Do not use this method to check if the cache should be displayed. Use <code>isVisible()</code> for this, which already does this (and other) checks.<br>
	 * Only use this method if you really want to inform yourself about the black status of the cache!
	 * 
	 * @return <code>true</code> if he black status of the cache is set.
	 */
	public boolean is_black() {
		return black;
	}

	public void setBlack(boolean is_black) {
		Global.getProfile().notifyUnsavedChanges(is_black != this.black);
		this.black = is_black;
	}

	public boolean is_new() {
		return newCache;
	}

	public void setNew(boolean is_new) {
		Global.getProfile().notifyUnsavedChanges(is_new != this.newCache);
		if (is_new && iconAndTextWPLevel == 3)
			iconAndTextWP = null;
		this.newCache = is_new;
	}

	public String getOcCacheID() {
		return ocCacheID;
	}

	public void setOcCacheID(String ocCacheID) {
		Global.getProfile().notifyUnsavedChanges(!ocCacheID.equals(this.ocCacheID));
		this.ocCacheID = ocCacheID;
	}

	public byte getNoFindLogs() {
		return noFindLogs;
	}

	public void setNoFindLogs(byte noFindLogs) {
		Global.getProfile().notifyUnsavedChanges(noFindLogs != this.noFindLogs);
		this.noFindLogs = noFindLogs;
	}

	public int getNumRecommended() {
		return numRecommended;
	}

	public String getRecommended() {
		if (!isCacheWpt())
			return "";
		if (isOC()) {
			return Convert.formatInt(recommendationScore) + " (" + Convert.formatInt(numRecommended) + ")";
		} else {
			int gcVote = numRecommended;
			if (gcVote < 100) {
				return MyLocale.formatDouble((double) gcVote / 10.0, "0.0");
			} else {
				int votes = gcVote / 100;
				gcVote = gcVote - 100 * votes;
				return MyLocale.formatDouble((double) gcVote / 10.0, "0.0") + " (" + Convert.formatInt(votes) + ")";
			}
		}
	}

	public void setNumRecommended(int numRecommended) {
		Global.getProfile().notifyUnsavedChanges(numRecommended != this.numRecommended);
		this.numRecommended = numRecommended;
	}

	public int getNumFoundsSinceRecommendation() {
		return numFoundsSinceRecommendation;
	}

	public void setNumFoundsSinceRecommendation(int numFoundsSinceRecommendation) {
		Global.getProfile().notifyUnsavedChanges(numFoundsSinceRecommendation != this.numFoundsSinceRecommendation);
		this.numFoundsSinceRecommendation = numFoundsSinceRecommendation;
	}

	public boolean has_bugs() {
		return bugs;
	}

	public void setHas_bugs(boolean has_bug) {
		Global.getProfile().notifyUnsavedChanges(has_bug != this.bugs);
		this.bugs = has_bug;
	}

	public boolean is_HTML() {
		return html;
	}

	public void setHTML(boolean is_HTML) {
		Global.getProfile().notifyUnsavedChanges(is_HTML != this.html);
		this.html = is_HTML;
	}

	public String getLastSync() {
		return lastSync;
	}

	public void setLastSync(String lastSync) {
		Global.getProfile().notifyUnsavedChanges(!lastSync.equals(this.lastSync));
		this.lastSync = lastSync;
	}

	public long[] getAttributesBits() {
		return this.attributesBits;
	}

	public void setAttribsAsBits(long[] attributesBits) {
		Global.getProfile().notifyUnsavedChanges(attributesBits != this.attributesBits);
		this.attributesBits = attributesBits;
	}

	public boolean hasSolver() {
		return hasSolver;
	}

	public void setHasSolver(boolean hasSolver) {
		Global.getProfile().notifyUnsavedChanges(hasSolver != this.hasSolver);
		this.hasSolver = hasSolver;
	}

	public boolean hasNote() {
		return hasNote;
	}

	public void setHasNote(boolean hasNote) {
		Global.getProfile().notifyUnsavedChanges(hasNote != this.hasNote);
		this.hasNote = hasNote;
	}

	/**
	 * rename a waypoint and all its associated files
	 * 
	 * @param newWptId
	 *            new waypoint id (will be converted to upper case)
	 * @return true on success, false on error
	 */
	public boolean rename(String newWptId) {
		newWptId = newWptId.toUpperCase();
		getCacheDetails(false);
		if (details.rename(newWptId)) {
			setWayPoint(newWptId);
			save();
			Global.getProfile().notifyUnsavedChanges(true);
			return true;
		} else {
			return false;
		}
	}
}
