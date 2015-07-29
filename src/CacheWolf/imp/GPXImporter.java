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

package CacheWolf.imp;

import CacheWolf.Filter;
import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.Attribute;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheImage;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.database.Log;
import CacheWolf.database.Travelbug;
import CacheWolf.utils.BetterUTF8Codec;
import CacheWolf.utils.Common;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import CacheWolf.utils.UrlFetcher;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.TextReader;
import ewe.net.MalformedURLException;
import ewe.net.URL;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.Enumeration;
import ewe.util.Vector;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import Data from an GPX File. If cache data exists, the data from the GPX-File is ignored. Class ID = 4000
 */
public class GPXImporter extends MinML {
    CacheDB cacheDB;
    CacheHolder holder;
    String tagValue, saveDir, logData, logIcon, logDate, logFinder, logId, finderID;
    boolean inWpt, inPQExtension, inLogs, inBug;
    public XMLElement document;
    private Vector files = new Vector();
    //private boolean debugGPX = false;
    InfoBox infB;
    boolean spiderOK = true;
    boolean downloadPics = false;
    boolean fromOC = false;
    boolean fromTC = false;
    boolean nameFound = false;
    static final Time gpxDate = new Time();
    int zaehlerGel = 0;
    public static final int ASKFORLOADINGPICTURES = 0;
    public static final int DONOTLOADPICTURES = 1;
    public static final int DOLOADPICTURES = 2;
    public static final int WRITEONLYOWNLOG = 3;
    GCImporter imgSpider;
    StringBuffer stringBuffer;
    private int howToDoIt;
    private String attID;
    private String attInc;

    public GPXImporter(String f) {
	cacheDB = MainForm.profile.cacheDB;
	files.add(f);
	saveDir = MainForm.profile.dataDir;
	inWpt = false;
	inPQExtension = false;
	inLogs = false;
	inBug = false;
	howToDoIt = ASKFORLOADINGPICTURES;
    }

    public void doIt(int how) {
	howToDoIt = how;
	Filter flt = new Filter();
	boolean wasFiltered = (MainForm.profile.getFilterActive() == Filter.FILTER_ACTIVE);
	flt.clearFilter();
	try {
	    String file;
	    if (how == ASKFORLOADINGPICTURES) {
		ImportGui importGui = new ImportGui(MyLocale.getMsg(5510, "Spider Options"), ImportGui.IMAGES | ImportGui.ISGC);
		if (importGui.execute() == FormBase.IDCANCEL) {
		    return;
		}
		downloadPics = importGui.downloadPics;
		importGui.close(0);
	    } else if (how == DONOTLOADPICTURES) {
		downloadPics = false;
	    } else {
		downloadPics = true;
	    }
	    if (howToDoIt != WRITEONLYOWNLOG) {
		if (downloadPics) {
		    imgSpider = new GCImporter();
		    imgSpider.setDownloadPics(downloadPics);
		    howToDoIt = DOLOADPICTURES;
		} else {
		    howToDoIt = DONOTLOADPICTURES;
		}
	    }
	    Vm.showWait(true);
	    infB = new InfoBox("Info", MyLocale.getMsg(4000, "Loaded caches: "));
	    infB.show();
	    stringBuffer = new StringBuffer(300);
	    for (int i = 0; i < files.size(); i++) {
		// Test for zip.file
		file = (String) files.get(i);
		if (file.indexOf(".zip") > 0) {
		    ZipFile zif = new ZipFile(file);
		    ZipEntry zipEnt;
		    Enumeration zipEnum = zif.entries();
		    // there could be more than one file in the archive
		    while (zipEnum.hasMoreElements()) {
			zipEnt = (ZipEntry) zipEnum.nextElement();
			// skip over PRC-files
			if (zipEnt.getName().endsWith("gpx")) {
			    // InputStreamReader r = new InputStreamReader(zif.getInputStream(zipEnt));		
			    TextReader tr = new TextReader(zif.getInputStream(zipEnt));
			    tr.codec = new AsciiCodec();
			    // infB.setTitle(zipEnt.toString());
			    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
			    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);

			    String lineRead = tr.readLine().toLowerCase();
			    if (lineRead.startsWith("ï»¿") || lineRead.indexOf("encoding=\"utf-8\"") > 0) {
				tr.close();
				// InputStreamReader r = new InputStreamReader(zif.getInputStream(zipEnt));								
				tr = new TextReader(zif.getInputStream(zipEnt));
				tr.codec = new BetterUTF8Codec();
				if (lineRead.startsWith("ï»¿")) {
				    // erste Zeile überlesen
				    // <?xml version="1.0" encoding="utf-8"?>
				    // weil der parser das erste Zeichen nicht mag und dann aussteigt 
				    tr.read();
				}
			    }
			    parse(tr);
			    tr.close();
			}
		    }
		    zif.close();
		} else {
		    TextReader tr = new TextReader(file);
		    tr.codec = new AsciiCodec();
		    // infB.setTitle("Info");
		    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
		    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);

		    String lineRead = tr.readLine().toLowerCase();
		    if (lineRead.startsWith("ï»¿") || lineRead.indexOf("encoding=\"utf-8\"") > 0) {
			tr.close();
			tr = new TextReader(file);
			tr.codec = new BetterUTF8Codec();
			if (lineRead.startsWith("ï»¿")) {
			    // erste Zeile überlesen
			    // <?xml version="1.0" encoding="utf-8"?>
			    // weil der parser das erste Zeichen nicht mag und dann aussteigt 
			    tr.read();
			}
		    }
		    parse(tr);
		    tr.close();
		}
		// save Index
		MainForm.profile.saveIndex(Profile.SHOW_PROGRESS_BAR);
	    }
	} catch (Exception e) {
	    if (holder == null) {
		Preferences.itself().log("[GPXImporter:DoIt] no holder LogID=" + logId, e, true);
	    } else if (holder.getCode() == null) {
		Preferences.itself().log("[GPXImporter:DoIt] no waypoint LogID=" + logId, e, true);
	    } else if (holder.getCode().length() > 0) {
		Preferences.itself().log("[GPXImporter:DoIt] " + holder.getCode() + " LogID=" + logId, e, true);
	    } else {
		Preferences.itself().log("[GPXImporter:DoIt] " + holder.getWpt().toString() + " LogID=" + logId, e, true);
	    }
	}
	Vm.showWait(false);
	infB.close(0);
	if (wasFiltered) {
	    flt.setFilter();
	    flt.doFilter();
	}
    }

    public void startElement(String tag, AttributeList atts) {
	stringBuffer.setLength(0);
	if (inPQExtension) {
	    if (inLogs) {
		if (tag.equals("groundspeak:log") || tag.equals("log") || tag.equals("terra:log")) {
		    logId = atts.getValue("id");
		    return;
		}
		if (tag.equals("groundspeak:finder") || tag.equals("geocacher") || tag.equals("finder") || tag.equals("terra:user")) {
		    finderID = atts.getValue("id");
		    return;
		}
	    } else {
		if (!inBug) {
		    if (tag.equals("groundspeak:attribute")) {
			attID = atts.getValue("id");
			attInc = atts.getValue("inc");
			return;
		    }
		    if (tag.indexOf("long_description") > -1) {
			holder.isHTML(atts.getValue("html").toLowerCase().equals("true"));
			return;
		    }
		    if (tag.equals("description") || tag.equals("terra:description")) {
			// set HTML always to true if from oc.de or TC
			holder.isHTML(true);
			return;
		    }
		    if (tag.equals("groundspeak:logs") || tag.equals("logs") || tag.equals("terra:logs")) {
			inLogs = true;
			return;
		    }
		    if (tag.equals("groundspeak:travelbugs")) {
			inBug = true;
			holder.getDetails().Travelbugs.clear();
			return;
		    }
		}
	    }
	} else { // not inPQExtensions
	    if (inWpt) {

		if (tag.equals("link")) {
		    holder.getDetails().URL = atts.getValue("href");
		    return;
		}

		if (tag.equals("groundspeak:cache")) {
		    inPQExtension = true;
		    String strAvailable = atts.getValue("available");
		    String strArchived = atts.getValue("archived");
		    holder.setAvailable(strAvailable != null && strAvailable.equalsIgnoreCase("True"));
		    holder.setArchived(strArchived != null && strArchived.equalsIgnoreCase("True"));
		    // OC now has GC - Format, get CacheID -- missing p.ex. on GcTour gpx
		    /* */
		    if (holder.isOC()) {
			for (int i = 0; i < atts.getLength(); i++) {
			    if (atts.getName(i).equals("id")) {
				holder.setIdOC(atts.getValue("id"));
				break;
			    }
			}
		    }
		    /* */
		    return;
		}

		// OC
		if (tag.equals("geocache") || tag.equals("cache")) {
		    boolean available = false;
		    boolean archived = false;
		    inPQExtension = true;
		    // get CacheID -- missing p.ex. on GcTour gpx
		    for (int i = 0; i < atts.getLength(); i++) {
			if (atts.getName(i).equals("id")) {
			    holder.setIdOC(atts.getValue("id"));
			    break;
			}
		    }
		    // get status
		    String status = atts.getValue("status");
		    if ("Available".equalsIgnoreCase(status))
			available = true;
		    else if ("Unavailable".equalsIgnoreCase(status))
			available = false;
		    else if ("Draft".equalsIgnoreCase(status))
			available = false;
		    else if ("Archived".equalsIgnoreCase(status))
			archived = true;
		    holder.setArchived(archived);
		    holder.setAvailable(available);
		    return;
		}

		if (tag.equals("terra:terracache")) {
		    inPQExtension = true;
		}

	    } else {

		if (tag.equals("wpt")) {
		    holder = new CacheHolder();
		    holder.setWpt(new CoordinatePoint(Common.parseDouble(atts.getValue("lat")), Common.parseDouble(atts.getValue("lon"))));

		    inWpt = true;
		    inLogs = false;
		    inBug = false;
		    inPQExtension = false;

		    nameFound = false;
		    zaehlerGel++;
		    // infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
		    MainTab.itself.tablePanel.updateStatusBar(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
		    logId = "";
		    return;
		}

		if (tag.equals("gpx")) {
		    // check for opencaching
		    String strCreator = atts.getValue("creator");
		    if (strCreator == null) {
			fromOC = false;
			fromTC = false;
		    } else {

			if (strCreator.indexOf("opencaching") > 0)
			    fromOC = true;
			else
			    fromOC = false;

			if (strCreator.startsWith("TerraCaching"))
			    fromTC = true;
			else
			    fromTC = false;
		    }
		    zaehlerGel = 0;
		    return;
		}

	    }
	}

	/*
	if (debugGPX) {
	    for (int i = 0; i < atts.getLength(); i++) {
		Preferences.itself().log("[GPXExporter:startElement]Type: " + atts.getType(i) + " Name: " + atts.getName(i) + " Value: " + atts.getValue(i), null);
	    }
	}
	*/
    }

    //Overrides: endElement(...) in MinML
    public void endElement(String tag) {
	tagValue = stringBuffer.toString();
	if (inPQExtension) {
	    if (inLogs) {
		if (tag.equals("groundspeak:date") || tag.equals("time") || tag.equals("date") || tag.equals("terra:date")) {
		    logDate = new String(tagValue.substring(0, 10));
		    return;
		}
		if (tag.equals("groundspeak:type") || tag.equals("type") || tag.equals("terra:type")) {
		    logIcon = new String(Log.typeText2Image(tagValue));
		    return;
		}
		if (tag.equals("groundspeak:finder") || tag.equals("geocacher") || tag.equals("finder") || tag.equals("terra:user")) {
		    logFinder = new String(tagValue);
		    return;
		}
		if (tag.equals("groundspeak:text") || tag.equals("text") || tag.equals("terra:entry")) {
		    logData = new String(tagValue);
		    return;
		}
		if (tag.equals("groundspeak:log") || tag.equals("log") || tag.equals("terra:log")) {
		    Log log = new Log(logId, finderID, logIcon, logDate, logFinder, logData);
		    holder.getDetails().CacheLogs.add(log);
		    if ((SafeXML.html2iso8859s1(logFinder).equalsIgnoreCase(Preferences.itself().myAlias) || (SafeXML.html2iso8859s1(logFinder).equalsIgnoreCase(Preferences.itself().myAlias2)))) {
			if ((logIcon.equals("icon_smile.gif") || logIcon.equals("icon_camera.gif") || logIcon.equals("icon_attended.gif"))) {
			    holder.setStatus(logDate);
			    holder.setFound(true);
			    holder.getDetails().setOwnLog(log);
			}
		    }
		    return;
		}
		if (tag.equals("groundspeak:logs") || tag.equals("logs") || tag.equals("terra:logs")) {
		    inLogs = false;
		    return;
		}
	    } else { // not inlogs
		if (inBug) {
		    if (tag.equals("groundspeak:name")) {
			Travelbug tb = new Travelbug(tagValue);
			holder.getDetails().Travelbugs.add(tb);
			holder.hasBugs(true);
			return;
		    }
		    if (tag.equals("groundspeak:travelbugs")) {
			inBug = false;
			return;
		    }
		} else { // not inBug
		    if (tag.equals("groundspeak:attribute")) {
			if (attID.equals("")) {
			    attID = Attribute.getIdFromGCText(tagValue);
			}
			if (attInc == null)
			    attInc = "1";
			int id = Integer.parseInt(attID);
			holder.getDetails().attributes.add(id, attInc);
			holder.setAttribsAsBits(holder.getDetails().attributes.getAttribsAsBits());
			return;
		    }
		    if (tag.equals("groundspeak:owner") || tag.equals("owner") || tag.equals("terra:owner")) {
			holder.setOwner(tagValue);
			if (Preferences.itself().myAlias.equals(SafeXML.html2iso8859s1(tagValue)) || (SafeXML.html2iso8859s1(tagValue).equalsIgnoreCase(Preferences.itself().myAlias2)))
			    holder.setOwned(true);
			return;
		    }
		    if (tag.equals("groundspeak:placed_by")) {
			if (holder.getOwner().equals("")) {
			    holder.setOwner(tagValue);
			    if (Preferences.itself().myAlias.equals(SafeXML.html2iso8859s1(tagValue)) || (SafeXML.html2iso8859s1(tagValue).equalsIgnoreCase(Preferences.itself().myAlias2)))
				holder.setOwned(true);
			}
			return;
		    }
		    if (tag.equals("groundspeak:difficulty") || tag.equals("difficulty") || tag.equals("terra:mental_challenge")) {
			try {
			    holder.setDifficulty(CacheTerrDiff.v1Converter(tagValue));
			} catch (IllegalArgumentException e) {

			    Preferences.itself().log(holder.getName() + ": illegal difficulty value: " + tagValue);
			}
			return;
		    }
		    if (tag.equals("groundspeak:terrain") || tag.equals("terrain") || tag.equals("terra:physical_challenge")) {
			try {
			    holder.setTerrain(CacheTerrDiff.v1Converter(tagValue));
			} catch (IllegalArgumentException e) {

			    Preferences.itself().log(holder.getName() + ": illegal terrain value: " + tagValue);
			}
			return;
		    }
		    if ((tag.equals("groundspeak:type") || tag.equals("type") || tag.equals("terra:style"))) {
			holder.setType(CacheType.gpxType2CwType(tagValue));
			if (holder.isCustomWpt()) {
			    holder.setSize(CacheSize.CW_SIZE_NOTCHOSEN);
			    holder.setDifficulty(CacheTerrDiff.CW_DT_UNSET);
			    holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
			}
			return;
		    }
		    if (tag.equals("groundspeak:container") || tag.equals("container")) {
			holder.setSize(CacheSize.gcGpxString2Cw(tagValue));
			return;
		    }
		    if (tag.equals("groundspeak:country") || tag.equals("country")) {
			holder.getDetails().setCountry(tagValue);
			return;
		    }
		    if (tag.equals("groundspeak:state") || tag.equals("state")) {
			holder.getDetails().setState(tagValue);
			return;
		    }
		    if (tag.indexOf("name") > -1) { // "groundspeak:name"
			holder.setName(tagValue);
			return;
		    }
		    if (tag.indexOf("short_description") > -1 || tag.equals("summary")) {
			if (holder.isHTML())
			    holder.getDetails().LongDescription = SafeXML.html2iso8859s1(tagValue) + "<br>"; // <br> needed because we also use a <br> in SpiderGC. Without it the comparison in ch.update fails
			else
			    holder.getDetails().LongDescription = tagValue + "\n";
			return;
		    }

		    if (tag.indexOf("long_description") > -1 || tag.equals("description") || tag.equals("terra:description")) {
			if (holder.isHTML())
			    holder.getDetails().LongDescription += SafeXML.html2iso8859s1(tagValue);
			else
			    holder.getDetails().LongDescription += tagValue;
			return;
		    }
		    if (tag.indexOf("encoded_hints") > -1 || tag.equals("hints")) {
			holder.getDetails().Hints = STRreplace.replace(STRreplace.replace(Common.rot13(tagValue), "\n", "<br>"), "\t", "");
			return;
		    }
		    if (tag.equals("terra:size")) {
			holder.setSize(CacheSize.tcGpxString2Cw(tagValue));
			return;
		    }

		    if (tag.equals("terra:hint")) {
			// remove "&lt;br&gt;<br>" from the end
			int indexTrash = tagValue.indexOf("&lt;br&gt;<br>");
			if (indexTrash > 0)
			    holder.getDetails().Hints = STRreplace.replace(STRreplace.replace(Common.rot13(tagValue.substring(0, indexTrash)), "\n", "<br>"), "\t", "");
			return;
		    }
		    if (tag.equals("groundspeak:cache") || tag.equals("geocache") || tag.equals("cache") || tag.equals("terra:terracache")) {
			inPQExtension = false;
			return;
		    }
		}
	    }
	} else { // not inPQExtension
	    if (inWpt) {
		if (tag.equals("time")) {
		    holder.setHidden(tagValue.substring(0, 10)); // Date;
		    if (howToDoIt == GPXImporter.WRITEONLYOWNLOG) {
			logDate = tagValue;
		    }
		    return;
		}
		if (tag.equals("name")) {
		    holder.setCode(tagValue);

		    if (gpxDate.getTime() != 0) {
			holder.setLastSync(gpxDate.format("yyyyMMddHHmmss"));
		    } else {
			holder.setLastSync(""); // could use now date ?
		    }
		    return;
		}
		// Text for additional waypoints, no HTML. Also used in POI
		if (tag.equals("cmt")) {
		    holder.getDetails().LongDescription = tagValue;
		    holder.isHTML(false);
		    return;
		}
		// fill name with contents of <desc>,
		// in case of gc.com the name is later replaced by the contents of <groundspeak:name> which is shorter
		if (tag.equals("desc")) {
		    holder.setName(tagValue);
		    return;
		}
		if (tag.equals("url")) {
		    holder.getDetails().URL = tagValue;
		    return;
		}
		if (tag.equals("sym") && tagValue.endsWith("Found")) {
		    holder.setFound(true);
		    holder.setStatus(CacheType.getFoundText(holder.getType()));
		    return;
		}
		// aditional wapypoint
		if (tag.equals("type")) {
		    if (tagValue.startsWith("Waypoint")) {
			holder.setType(CacheType.gpxType2CwType(tagValue));
			holder.setSize(CacheSize.CW_SIZE_NOTCHOSEN);
			holder.setDifficulty(CacheTerrDiff.CW_DT_UNSET);
			holder.setTerrain(CacheTerrDiff.CW_DT_UNSET);
			holder.setLastSync("");
		    } else {

		    }
		    return;
		}
		if (tag.equals("wpt")) {
		    if (infB.isClosed())
			return;
		    int index = cacheDB.getIndex(holder.getCode());
		    if (index == -1) {
			// not already in database
			holder.setNoFindLogs(holder.getDetails().CacheLogs.countNotFoundLogs());
			holder.setNew(true);
			cacheDB.add(holder);
			if (downloadPics && !holder.isAddiWpt()) {
			    if (spiderOK && !holder.isArchived()) {
				getImages();
				// Rename image sources
				String text;
				String orig;
				String imgName;
				orig = holder.getDetails().LongDescription;

				Extractor ex = new Extractor(orig, "<img src=\"", ">", 0, false);
				int num = 0;
				while ((text = ex.findNext()).length() > 0 && spiderOK) {
				    if (num >= holder.getDetails().images.size())
					break;
				    imgName = holder.getDetails().images.get(num).getTitle();
				    holder.getDetails().LongDescription = STRreplace.replace(holder.getDetails().LongDescription, text, "[[Image: " + imgName + "]]");
				    num++;
				}
			    }
			}
			holder.saveCacheDetails();
		    } else {
			// Update cache data
			CacheHolder oldCh = cacheDB.get(index);
			// Preserve images
			holder.getDetails().images = oldCh.getDetails().images;
			oldCh.initStates(false);
			if (!oldCh.isOC()) {
			    if (Preferences.itself().useGCFavoriteValue) {
				// todo get GC Favs from gpx if they exist there
				holder.setNumRecommended(oldCh.getNumRecommended());
			    } else {
				holder.setNumRecommended(oldCh.getNumRecommended()); // gcvote Bewertung bleibt erhalten			
			    }
			}
			if (howToDoIt == GPXImporter.WRITEONLYOWNLOG) {
			    CacheHolderDetail chD = holder.getDetails();
			    logIcon = oldCh.getGCFoundIcon();
			    Time logTime = new Time();
			    try {
				logTime.parse(logDate.substring(0, 19), "yyyy-MM-dd'T'HH:mm:ss");
				logDate = logTime.format("yyyy-MM-dd HH:mm");
			    } catch (Exception e) {
			    }
			    holder.setStatus(logDate);
			    Log log = new Log("", Preferences.itself().gcMemberId, logIcon, logDate, Preferences.itself().myAlias, STRreplace.replace(chD.LongDescription, "\n", "<br />"));
			    chD.setOwnLog(log);
			    oldCh.updateOwnLog(holder);
			} else {
			    oldCh.update(holder);
			}
			oldCh.saveCacheDetails();
		    }

		    inWpt = false;
		    return;
		}
	    } else { // in header
		if (tag.equals("time")) {
		    try {
			gpxDate.parse(tagValue.substring(0, 19), "yyyy-MM-dd'T'HH:mm:ss");
		    } catch (IllegalArgumentException e) {
			gpxDate.setTime(0);
			Preferences.itself().log("[GPXImporter:endElement]Error parsing Element time: '" + tagValue + "'. Ignoring.");
		    }
		    return;
		}
	    }
	}
    }

    // Overrides: characters(...) in MinML
    public void characters(char[] ch, int start, int length) {
	stringBuffer.append(ch, start, length);
	//if (debugGPX)
	//  Preferences.itself().log("Char: " + stringBuffer.toString(), null);
    }

    public static String TCSizetoText(String size) {
	if (size.equals("1"))
	    return "Micro";
	if (size.equals("2"))
	    return "Medium";
	if (size.equals("3"))
	    return "Regular";
	if (size.equals("4"))
	    return "Large";
	if (size.equals("5"))
	    return "Very Large";

	return "None";
    }

    private void getImages() {
	String addresse;
	String cacheText;
	CacheHolderDetail chD = holder.getDetails();

	try {
	    if (fromTC) {
		// special ;
		Preferences.itself().log("[gpx Import]Spider images from TerraCaching not implemented!", null);
	    } else {
		if (fromOC) {
		    chD.images.clear();
		    addresse = chD.URL;
		    cacheText = UrlFetcher.fetch(addresse);
		    Extractor exBeschreibung = new Extractor(cacheText, "<!-- Beschreibung -->", "<!-- End Beschreibung -->", 0, false);
		    String beschreibung = exBeschreibung.findNext();
		    getOCPictures(beschreibung);
		    Extractor exBilder = new Extractor(cacheText, "<!-- Bilder -->", "<!-- End Bilder -->", 0, false);
		    String bilder = exBilder.findNext();
		    getOCPictures(bilder);
		} else {
		    String wayPoint = holder.getCode();
		    if (wayPoint.startsWith("GC")) {
			imgSpider.fetchWayPointPage(wayPoint);
			chD.setLongDescription(imgSpider.getDescription());
			imgSpider.getImages(chD);
			// todo if Attributes are in the gpx (Version 1.1.0) : don't spider them
			imgSpider.getAttributes(chD);
		    }
		}
	    }
	} catch (Exception e1) {
	    // e1.printStackTrace();
	}
    }

    private void getOCPictures(String html) {
	Regex imgRegexUrl = new Regex("(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)");
	imgRegexUrl.setIgnoreCase(true);
	int descIndex = 0;
	while (imgRegexUrl.searchFrom(html, descIndex)) {
	    descIndex = imgRegexUrl.matchedTo();
	    String fetchUrl = imgRegexUrl.stringMatched(2); // URL in Anführungszeichen in (2)
	    if (fetchUrl == null) {
		fetchUrl = imgRegexUrl.stringMatched(3);
	    } // falls ohne in (3)
	    if (fetchUrl == null) {
		continue;
	    } // schlechtes html
	      // fetchUrl ist auf jeden Fall ohne Anführungszeichen
	    if (fetchUrl.startsWith("resource"))
		continue; //
	    if (fetchUrl.startsWith("images")) // z.B. Flaggen
		if (!fetchUrl.startsWith("images/uploads"))
		    continue;
	    if (fetchUrl.startsWith("thumbs"))
		continue; // z.B. Flaggen
	    try {
		// TODO this is not quite correct: actually the "base" URL must be known...
		// but anyway a different baseURL should not happen very often - it doesn't in my area
		String hostname = OC.getOCHostName(holder.getCode());
		if (!fetchUrl.startsWith("http://")) {
		    fetchUrl = new URL(new URL("http://" + hostname + "/"), fetchUrl).toString();
		}
	    } catch (MalformedURLException e) {
		// auch egal
		continue;
	    }
	    CacheImage imageInfo = new CacheImage();
	    imageInfo.setURL(fetchUrl);
	    imageInfo.setTitle(makeImageTitle(imgRegexUrl.stringMatched(1), fetchUrl));
	    getOCPicture(imageInfo);
	}

	Extractor exHref = new Extractor(html, "<a href=", "</a>", 0, true);
	String href = "";
	Extractor exHttp = new Extractor(href, "http://", "\"", 0, true);
	while ((href = exHref.findNext()).length() > 0) {
	    exHttp.set(href, "http://", "\"", 0, true);
	    String fetchUrl = exHttp.findNext();
	    if (fetchUrl.length() > 0) {
		try {
		    String imgType = (fetchUrl.substring(fetchUrl.lastIndexOf('.')).toLowerCase() + "    ").substring(0, 4).trim();
		    fetchUrl = "http://" + fetchUrl.substring(0, fetchUrl.lastIndexOf('.') + imgType.length());
		    if (imgType.startsWith(".jpg") || imgType.startsWith(".bmp") || imgType.startsWith(".png") || imgType.startsWith(".gif")) {
			CacheImage imageInfo = new CacheImage();
			imageInfo.setURL(fetchUrl);
			imageInfo.setTitle(makeImageTitle(href, fetchUrl));
			getOCPicture(imageInfo);
		    }
		} catch (IndexOutOfBoundsException e) {
		}
	    }
	}
    }

    private String makeImageTitle(String imgTag, String fetchUrl) {
	Regex imgRegexAlt = new Regex("(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))");
	imgRegexAlt.setIgnoreCase(true);
	String imgAltText;
	if (imgRegexAlt.search(imgTag)) {
	    imgAltText = imgRegexAlt.stringMatched(1);
	    if (imgAltText == null)
		imgAltText = imgRegexAlt.stringMatched(2);
	} else { // no alternative text as image title -> use --- or filename
		 // wenn von Opencaching oder geocaching ist Dateiname doch nicht so toll, weil nur aus Nummer bestehend
	    if (fetchUrl.toLowerCase().indexOf("opencaching.") > 0 || fetchUrl.toLowerCase().indexOf("geocaching.com") > 0)
		imgAltText = "---"; // no image title
	    else
		imgAltText = fetchUrl.substring(fetchUrl.lastIndexOf('/') + 1);
	}
	return imgAltText;
    }

    private void getOCPicture(CacheImage imageInfo) {
	String fileName = holder.getCode() + "_" + imageInfo.getURL().substring(imageInfo.getURL().lastIndexOf('/') + 1);
	fileName = Common.ClearForFileName(fileName).toLowerCase();
	String target = MainForm.profile.dataDir + fileName;
	imageInfo.setFilename(fileName);
	try {
	    File ftest = new File(target);
	    if (ftest.exists()) {
		if (ftest.length() == 0) {
		    ftest.delete();
		} else {
		    holder.getDetails().images.add(imageInfo);
		}
	    } else {
		UrlFetcher.fetchDataFile(imageInfo.getURL(), target);
		ftest = new File(target);
		if (ftest.exists()) {
		    if (ftest.length() > 0) {
			holder.getDetails().images.add(imageInfo);
		    } else {
			ftest.delete();
		    }
		}
	    }
	} catch (IOException e) {
	    String ErrMessage;
	    String wp, n;
	    if (holder != null && holder.getCode() != null)
		wp = holder.getCode();
	    else
		wp = "WP???";
	    if (holder != null && holder.getName() != null)
		n = holder.getName();
	    else
		n = "name???";

	    //if (e == null)
	    // ErrMessage = "Ignoring error: OCXMLImporter.getPic: IOExeption == null, while downloading picture: " + fileName + " from URL:" + imageInfo.getURL();
	    //else {
	    if (e.getMessage().equalsIgnoreCase("could not connect") || e.getMessage().equalsIgnoreCase("unkown host")) {
		// is there a better way to find out what happened?
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + ")" + MyLocale.getMsg(1619, ": could not download image from URL: ") + imageInfo.getURL();
	    } else
		ErrMessage = MyLocale.getMsg(1618, "Ignoring error in cache: ") + n + " (" + wp + "): ignoring IOException: " + e.getMessage() + " while downloading picture:" + fileName + " from URL:" + imageInfo.getURL();
	    //}
	    Preferences.itself().log(ErrMessage, e, true);
	}

    }

    public int getHow() {
	return howToDoIt;
    }
}
