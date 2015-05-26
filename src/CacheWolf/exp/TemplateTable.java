package CacheWolf.exp;

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.database.Log;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.Files;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.AsciiCodec;
import ewe.io.FileBase;
import ewe.io.TextCodec;
import ewe.sys.Time;
import ewe.util.Hashtable;
import ewe.util.Vector;

public class TemplateTable {
    CacheHolder cache;

    public TemplateTable() {
	this.cache = null;
    }

    public void set(CacheHolder cache) {
	this.cache = cache;
    }

    /** Return a Hashtable containing all the cache data for Templates */
    public Hashtable toHashtable(Regex decSep, Regex badChars, int shortWaypointLength, int shortNameLength, int nrOfLogs, TextCodec codec, GarminMap gm, boolean withFoundText, int ModTyp, String expName) {
	Hashtable varParams = new Hashtable();
	byte type = cache.getType();
	byte difficulty;
	byte terrain;
	byte size;
	String code = cache.getCode();
	CWPoint wpt = cache.getWpt();
	CacheHolderDetail chD = cache.getDetails();
	varParams.put("PROGDIR", FileBase.getProgramDirectory());
	varParams.put("PROFILDIR", MainForm.profile.dataDir);
	varParams.put("ALIAS", Preferences.itself().myAlias);
	varParams.put("TYPE", CacheType.type2TypeTag(type)); // <type>
	varParams.put("TYPENO", "" + type);
	varParams.put("SYM", CacheType.type2SymTag(type)); // <sym>
	varParams.put("GSTYPE", CacheType.type2GSTypeTag(type)); // <groundspeak:type>
	varParams.put("SHORTTYPE", CacheType.getExportShortId(type));
	if (cache.isAddiWpt()) {
	    CacheHolder ch = cache.mainCache;
	    difficulty = ch.getDifficulty();
	    terrain = ch.getTerrain();
	    size = ch.getSize();
	    varParams.put("MAINWP", ch.getCode());
	    String cn = ch.getName();
	    if (codec instanceof AsciiCodec) {
		cn = Exporter.simplifyString(cn);
	    } // use for "NAME"
	    if (badChars != null) {
		cn = badChars.replaceAll(cn);
	    } // use for "NAME"
	    varParams.put("MAINWPNAME", cn);
	    varParams.put("DIFFICULTY", (difficulty < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(difficulty)));
	    String sHard = Integer.toString(difficulty);
	    varParams.put("SHORTDIFFICULTY", (difficulty < 0) ? "" : sHard);
	    varParams.put("SHDIFFICULTY", (difficulty < 0) ? "" : sHard.substring(0, 1));
	    varParams.put("TERRAIN", (terrain < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(terrain)));
	    String sTerrain = Integer.toString(terrain);
	    varParams.put("SHORTTERRAIN", (terrain < 0) ? "" : sTerrain);
	    varParams.put("SHTERRAIN", (terrain < 0) ? "" : sTerrain.substring(0, 1));
	    varParams.put("SIZE", CacheSize.cw2ExportString(size));
	    varParams.put("SHORTSIZE", CacheSize.getExportShortId(size));
	    varParams.put("OWNER", (ModTyp == 0) ? SafeXML.cleanGPX(ch.getOwner()) : ch.getOwner());
	    varParams.put("DATE", ch.getHidden());
	} else {
	    difficulty = cache.getDifficulty();
	    terrain = cache.getTerrain();
	    size = cache.getSize();
	    varParams.put("MAINWP", "");
	    varParams.put("MAINWPNAME", "");
	    if (cache.isCustomWpt()) {
		varParams.put("DIFFICULTY", "");
		varParams.put("SHORTDIFFICULTY", "");
		varParams.put("SHDIFFICULTY", "");
		varParams.put("TERRAIN", "");
		varParams.put("SHORTTERRAIN", "");
		varParams.put("SHTERRAIN", "");
	    } else {
		varParams.put("DIFFICULTY", (difficulty < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(difficulty)));
		String sHard = Integer.toString(difficulty);
		varParams.put("SHORTDIFFICULTY", (difficulty < 0) ? "" : sHard);
		varParams.put("SHDIFFICULTY", (difficulty < 0) ? "" : sHard.substring(0, 1));
		varParams.put("TERRAIN", (terrain < 0) ? "" : decSep.replaceAll(CacheTerrDiff.longDT(terrain)));
		String sTerrain = Integer.toString(terrain);
		varParams.put("SHORTTERRAIN", (terrain < 0) ? "" : sTerrain);
		varParams.put("SHTERRAIN", (terrain < 0) ? "" : sTerrain.substring(0, 1));
	    }
	    varParams.put("SIZE", CacheSize.cw2ExportString(size));
	    varParams.put("SHORTSIZE", CacheSize.getExportShortId(size));
	    varParams.put("OWNER", (ModTyp == 0) ? SafeXML.cleanGPX(cache.getOwner()) : cache.getOwner());
	    varParams.put("DATE", cache.getHidden());
	}
	varParams.put("WAYPOINT", code); // <name>
	int wpl = code.length();
	int wps = (wpl < shortWaypointLength) ? 0 : wpl - shortWaypointLength;
	String s = "";
	for (int i = code.length() - 1; i >= 0; i--) {
	    s = s + code.substring(i, i + 1);
	}
	s = code.substring(code.length() - 2, code.length());
	varParams.put("INVERS", s);
	varParams.put("SHORTWAYPOINT", code.substring(wps, wpl));
	varParams.put("DISTANCE", decSep.replaceAll(cache.getDistance()));
	varParams.put("BEARING", cache.getBearing());
	if ((wpt != null && wpt.isValid())) {
	    varParams.put("LATLON", decSep.replaceAll(wpt.toString()));
	    varParams.put("LAT", decSep.replaceAll(wpt.getLatDeg(TransformCoordinates.DD)));
	    varParams.put("LON", decSep.replaceAll(wpt.getLonDeg(TransformCoordinates.DD)));
	} else {
	    varParams.put("LATLON", "unknown");
	    varParams.put("LAT", "");
	    varParams.put("LON", "");
	}
	if (withFoundText) {
	    varParams.put("STATUS", cache.getStatusText());
	} else
	    varParams.put("STATUS", cache.getStatus());
	varParams.put("GC_LOGTYPE", CacheHolder.getGCLogType(type, cache.isFound(), cache.getStatus()));
	varParams.put("STATUS_DATE", cache.getStatusDate());
	varParams.put("STATUS_TIME", cache.getStatusTime());
	varParams.put("STATUS_UTC_DATE", cache.getStatusUtcDate());
	varParams.put("STATUS_UTC_TIME", cache.getStatusUtcTime());
	String cn = cache.getName();
	varParams.put("CACHE_NAME", cn);
	if (codec instanceof AsciiCodec) {
	    cn = Exporter.simplifyString(cn);
	} // use for "NAME"
	if (badChars != null) {
	    cn = badChars.replaceAll(cn);
	} // use for "NAME"
	varParams.put("NAME", cn);
	String shortName = shortenName(cn, shortNameLength);
	varParams.put("SHORTNAME", shortName);
	varParams.put("TRAVELBUG", (cache.hasBugs() ? "Y" : "N"));
	if (gm != null)
	    varParams.put("GMTYPE", gm.getIcon(cache));
	varParams.put("NOW_DATE", nowdate().setToCurrentTime().toString());
	varParams.put("NOW_TIME", nowtime().setToCurrentTime().toString());
	varParams.put("CACHEID", cache.getCacheID());
	varParams.put("AVAILABLE", cache.isAvailable() ? "TRUE" : "FALSE");
	varParams.put("ARCHIVED", cache.isArchived() ? "TRUE" : "FALSE");
	varParams.put("SOLVED", cache.isSolved() ? "TRUE" : "FALSE");
	varParams.put("HTML", cache.isHTML() ? "TRUE" : "FALSE");
	varParams.put("VOTE", cache.getRecommended());
	// () ? TRUE : FALSE
	if (chD == null) {
	    varParams.put("URL", "");
	    varParams.put("DESCRIPTION", "");
	    varParams.put("NOTES", "");
	    varParams.put("HINTS", "");
	    varParams.put("DECRYPTEDHINTS", "");
	    varParams.put("COUNTRY", "");
	    varParams.put("STATE", "");
	    varParams.put("OWNLOG", "");
	} else {
	    // todo &lt;br&gt;
	    varParams.put("URL", chD.URL);
	    if (cache.isHTML()) {
		if (ModTyp == 0) {
		    varParams.put("DESCRIPTION", SafeXML.cleanGPX(chD.LongDescription));
		} else {
		    varParams.put("DESCRIPTION", modifyLongDesc(chD, ModTyp));
		}
	    } else {
		// what was the reason? replace or no replace? I dont remember
		varParams.put("DESCRIPTION", STRreplace.replace(chD.LongDescription, "\n", "<br>"));
	    }

	    if (badChars != null) {
		if (ModTyp == 0) {
		    varParams.put("NOTES", badChars.replaceAll(chD.getCacheNotes()));
		} else {
		    varParams.put("NOTES", STRreplace.replace(badChars.replaceAll(chD.getCacheNotes()), "\n", "<br>"));
		}
		varParams.put("HINTS", (ModTyp == 0) ? SafeXML.cleanGPX(badChars.replaceAll(chD.Hints)) : badChars.replaceAll(chD.Hints));
		varParams.put("DECRYPTEDHINTS", (ModTyp == 0) ? SafeXML.cleanGPX(badChars.replaceAll(Common.rot13(chD.Hints))) : badChars.replaceAll(Common.rot13(chD.Hints)));
	    } else {
		if (ModTyp == 0) {
		    varParams.put("NOTES", SafeXML.cleanGPX(chD.getCacheNotes()));
		} else {
		    varParams.put("NOTES", STRreplace.replace(chD.getCacheNotes(), "\n", "<br>"));
		}
		varParams.put("HINTS", (ModTyp == 0) ? SafeXML.cleanGPX(chD.Hints) : chD.Hints);
		varParams.put("DECRYPTEDHINTS", (ModTyp == 0) ? SafeXML.cleanGPX(Common.rot13(chD.Hints)) : Common.rot13(chD.Hints));
	    }
	    if (chD.Travelbugs.size() > 0)
		varParams.put("BUGS", (ModTyp == 0) ? SafeXML.cleanGPX(chD.Travelbugs.toHtml()) : chD.Travelbugs.toHtml());
	    if (chD.getSolver() != null && chD.getSolver().trim().length() > 0)
		varParams.put("SOLVER", STRreplace.replace(chD.getSolver(), "\n", "<br/>\n"));
	    varParams.put("COUNTRY", chD.Country);
	    varParams.put("STATE", chD.State);

	    // attributes
	    Vector attVect = new Vector(chD.attributes.count() + 1);
	    for (int i = 0; i < chD.attributes.count(); i++) {
		Hashtable atts = new Hashtable();
		atts.put("PATHANDIMAGE", chD.attributes.getAttribute(i).getPathAndImageName());
		atts.put("IMAGE", chD.attributes.getAttribute(i).getImageName());
		atts.put("GCID", chD.attributes.getAttribute(i).getGCId());
		atts.put("INC", "" + chD.attributes.getAttribute(i).getInc());
		atts.put("INC2TXT", chD.attributes.getAttribute(i).getInc() == 1 ? "YES:" : "NO:");
		if (i % 5 == 4)
		    atts.put("BR", "<br/>");
		else
		    atts.put("BR", "");
		atts.put("INFO", chD.attributes.getAttribute(i).getMsg());
		atts.put("GCINFO", chD.attributes.getAttribute(i).getGCText());
		attVect.add(atts);
	    }
	    varParams.put("ATTRIBUTES", attVect);

	    // logs
	    Vector logVect = new Vector(chD.CacheLogs.size());
	    int maxlogs = chD.CacheLogs.size();
	    if (nrOfLogs > -1 && nrOfLogs < maxlogs)
		maxlogs = nrOfLogs;
	    for (int i = 0; i < maxlogs; i++) {
		Hashtable logs = new Hashtable();
		String stmp;
		if (chD.CacheLogs.getLog(i).getIcon().equals(Log.MAXLOGICON)) {
		    logs.put("WAYPOINT", code);
		    logs.put("ICON", Log.MAXLOGICON);
		    logs.put("LOGTYPE", "");
		    logs.put("DATE", "");
		    logs.put("LOGGER", "");
		    stmp = "<hr>" + MyLocale.getMsg(736, "Too many logs") + "<hr>";
		} else {
		    logs.put("WAYPOINT", code);
		    logs.put("ICON", chD.CacheLogs.getLog(i).getIcon());
		    logs.put("LOGTYPE", chD.CacheLogs.getLog(i).icon2GPXType());
		    logs.put("DATE", chD.CacheLogs.getLog(i).getDate());
		    logs.put("LOGGER", (ModTyp == 0) ? SafeXML.cleanGPX(chD.CacheLogs.getLog(i).getLogger()) : chD.CacheLogs.getLog(i).getLogger());
		    stmp = STRreplace.replace(chD.CacheLogs.getLog(i).getMessage().trim(), "http://www.geocaching.com/images/icons/", null);
		}
		logs.put("MESSAGE", (ModTyp == 0) ? SafeXML.cleanGPX(stmp) : stmp);
		logVect.add(logs);
	    }
	    varParams.put("LOGS", logVect);

	    if (chD.OwnLog != null) {
		varParams.put("OWNLOG", (ModTyp == 0) ? SafeXML.cleanGPX(STRreplace.replace(chD.OwnLog.getMessage(), "<br />", "\n")) : chD.OwnLog.getMessage());
	    } else {
		varParams.put("OWNLOG", "");
	    }

	    Vector addiVect = new Vector(cache.addiWpts.size());
	    for (int i = 0; i < cache.addiWpts.size(); i++) {
		Hashtable addis = new Hashtable();
		CacheHolder ch = (CacheHolder) cache.addiWpts.get(i);
		addis.put("WAYPOINT", ch.getCode());
		addis.put("NAME", (ModTyp == 0) ? SafeXML.cleanGPX(ch.getName()) : ch.getName());
		if ((ch.getWpt() != null && ch.getWpt().isValid())) {
		    addis.put("LATLON", decSep.replaceAll(ch.getWpt().toString()));
		    addis.put("LAT", decSep.replaceAll(ch.getWpt().getLatDeg(TransformCoordinates.DD)));
		    addis.put("LON", decSep.replaceAll(ch.getWpt().getLonDeg(TransformCoordinates.DD)));
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
		addis.put("LONGDESC", (ModTyp == 0) ? SafeXML.cleanGPX(ch.getDetails().LongDescription) : ch.getDetails().LongDescription);
		addiVect.add(addis);
	    }
	    varParams.put("ADDIS", addiVect);

	    Vector imgVect = new Vector(chD.images.size());
	    for (int i = 0; i < chD.images.size(); i++) {
		Hashtable imgs = new Hashtable();
		String imgFile = chD.images.get(i).getFilename();
		boolean doit = true;
		for (int j = i + 1; j < chD.images.size(); j++) {
		    String jmgFile = chD.images.get(j).getFilename();
		    if (imgFile.equals(jmgFile)) {
			doit = false;
			break;
		    }
		}
		if (doit) {
		    imgs.put("FILENAME", imgFile);
		    String title = chD.images.get(i).getTitle();
		    imgs.put("TEXT", title);
		    String comment = chD.images.get(i).getComment();
		    imgs.put("COMMENT", comment);
		    imgs.put("URL", chD.images.get(i).getURL());
		    if (!expName.equals("")) {
			String src = MainForm.profile.dataDir + imgFile;
			String dest;
			if (expName.endsWith("*")) {
			    String d1 = Preferences.itself().getExportPath(expName.substring(0, expName.length() - 1));
			    d1 = d1 + imgFile.substring(0, 4).toUpperCase() + "/";
			    dest = d1 + imgFile.toUpperCase();
			    if (imgFile.toUpperCase().startsWith(title.toUpperCase())) {
				d1 = Common.getPathAndFilename(dest);
			    } else {
				d1 = Common.getPathAndFilename(dest) + " - " + Common.ClearForFileName(title);
			    }
			    dest = d1 + Common.getExtension(dest).toLowerCase();
			} else {
			    dest = Preferences.itself().getExportPath(expName) + imgFile;
			}
			if (!Files.copy(src, dest)) {
			    Preferences.itself().log("[CacheHolder:toHashtable]error copying " + imgFile + " to " + Preferences.itself().getExportPath(expName));
			}
		    }
		    if (!title.toLowerCase().startsWith(code.toLowerCase())) {
			imgVect.add(imgs);
		    }
		}
	    }
	    varParams.put("cacheImg", imgVect);
	}
	return varParams;
    }

    /**
     * Modify the image links in the long description so that they point to image files in the local directory<br>
     * Also copy the image file to the target directory so that it can be displayed.<br>
     * 
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
	    imgsrc = "file://" + MainForm.profile.dataDir;
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

    private String shortenName(String Name, int maxLength) {
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

}
