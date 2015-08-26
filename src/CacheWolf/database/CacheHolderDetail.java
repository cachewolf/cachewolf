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
package CacheWolf.database;

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.controls.InfoBox;
import CacheWolf.utils.Extractor;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;

import com.stevesoft.ewe_pat.Regex;

import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.PrintWriter;
import ewe.ui.FormBase;
import ewe.util.mString;

public class CacheHolderDetail {
    private static final String EMPTY = "";

    /** CacheHolder of the detail. <b>Only</b> set by CacheHolder when creating detail! **/
    private CacheHolder parent = null;
    private String version = "";
    public String LongDescription = EMPTY;
    public String LastUpdate = EMPTY;
    public String Hints = EMPTY;
    public LogList CacheLogs = new LogList();
    private String CacheNotes = EMPTY;
    public CacheImages images = new CacheImages();
    public CacheImages logImages = new CacheImages();
    public CacheImages userImages = new CacheImages();
    public Attributes attributes = new Attributes();
    public TravelbugList Travelbugs = new TravelbugList();
    // public String Bugs = EMPTY; Superceded by Travelbugs
    public String URL = EMPTY;
    private String solver;
    private Log ownLog;
    private String country;
    private String state;
    /**
     * For faster cache import (from opencaching) changes are only written when the details are freed from memory
     * If you want to save the changes automatically when the details are unloaded, set this to true
     */
    public boolean hasUnsavedChanges = false;

    public CacheHolderDetail(CacheHolder ch) {
	parent = ch;
	solver = EMPTY;
	ownLog = null;
	country = EMPTY;
	state = EMPTY;
    }

    // quick debug info
    public String toString() {
	if (this.parent == null)
	    return "empty unassigned";
	else if (parent.mainCache == null)
	    return parent.toString();
	else
	    return parent + "(" + parent.mainCache + ")";
    }

    public CacheHolder getParent() {
	return parent;
    }

    public String getSolver() {
	return this.solver;
    }

    public void setSolver(String solver) {
	if (!this.solver.equals(solver))
	    parent.setUpdated(true);
	parent.setHasSolver(!solver.trim().equals(""));
	this.solver = solver;
    }

    public Log getOwnLog() {
	return this.ownLog;
    }

    public void setOwnLog(Log ownLog) {
	this.ownLog = ownLog;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public void setLongDescription(String longDescription) {
	String s = stripControlChars(longDescription);
	if (LongDescription.equals(""))
	    parent.setNew(true);
	else {
	    if (!s.equals(LongDescription)) {
		parent.setUpdated(true);
	    }
	}
	LongDescription = s;
    }

    private String stripControlChars(String desc) {
	StringBuffer sb = new StringBuffer(desc.length());
	for (int i = 0; i < desc.length(); i++) {
	    char c = desc.charAt(i);
	    if (c >= ' ' || c == 10 || c == 13)
		sb.append(c);
	}
	return sb.toString();
    }

    public void setHints(String hints) {
	if (!Hints.equals(hints))
	    parent.setUpdated(true);
	Hints = hints;
    }

    private String gCNotes = "";

    public String getGCNotes() {
	return gCNotes;
    }

    public void setGCNotes(String notes) {
	gCNotes = notes;
    }

    public boolean setCacheNotes(String notes) {
	boolean ret = !this.CacheNotes.equals(notes);
	if (ret) {
	    parent.setUpdated(true);
	    this.CacheNotes = notes;
	    parent.setHasNote(!this.CacheNotes.trim().equals(""));
	}
	return ret;
    }

    public String getCacheNotes() {
	return this.CacheNotes;
    }

    public void setCacheLogs(LogList newLogs) {
	if (Preferences.itself().overwriteLogs) {
	    CacheLogs = newLogs;
	    parent.setLogUpdated(true);
	    hasUnsavedChanges = true;
	} else {
	    int size = newLogs.size();
	    for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must start with oldest log
		if (CacheLogs.merge(newLogs.getLog(i)) >= 0)
		    parent.setLogUpdated(true);
	    }
	}
	if (CacheLogs.purgeLogs() > 0)
	    hasUnsavedChanges = true;
	parent.setNoFindLogs(CacheLogs.countNotFoundLogs());
    }

    /**
     * Method to update an existing cache with new data. This is
     * necessary to avoid missing old logs. Called from GPX Importer
     * 
     * @param newChD
     *            new cache data
     * @return CacheHolder with updated data
     */
    public CacheHolderDetail update(CacheHolderDetail newChD) {
	// flags
	CacheHolder ch = parent;
	// travelbugs:GPX-File contains all actual travelbugs but not the missions
	// we need to check whether the travelbug is already in the existing list
	ch.hasBugs(newChD.Travelbugs.size() > 0);
	for (int i = newChD.Travelbugs.size() - 1; i >= 0; i--) {
	    Travelbug tb = newChD.Travelbugs.getTB(i);
	    Travelbug oldTB = this.Travelbugs.find(tb.getName());
	    // If the bug is already in the cache, we keep it
	    if (oldTB != null) {
		if (tb.getMission().length() > 0)
		    oldTB.setMission(tb.getMission());
		if (tb.getGuid().length() > 0)
		    oldTB.setGuid(tb.getGuid());
		newChD.Travelbugs.replace(i, oldTB);
	    }
	}
	this.Travelbugs = newChD.Travelbugs;

	if (newChD.attributes.count() > 0)
	    this.attributes = newChD.attributes;
	this.URL = newChD.URL;
	if (this.gCNotes.length() > 0) {
	    this.setCacheNotes(STRreplace.replace(this.CacheNotes, this.gCNotes, newChD.getGCNotes()));
	} else {
	    this.setCacheNotes(this.CacheNotes + newChD.getGCNotes());
	}
	this.images = newChD.images;
	setLongDescription(newChD.LongDescription);
	setHints(newChD.Hints);
	setCacheLogs(newChD.CacheLogs);
	if (newChD.ownLog != null) {
	    this.ownLog = newChD.ownLog;
	}
	if (newChD.country.length() > 0)
	    this.country = newChD.country;
	if (newChD.state.length() > 0)
	    this.state = newChD.state;
	if (newChD.getSolver().length() > 0)
	    this.setSolver(newChD.getSolver());
	return this;
    }

    /**
     * Method to parse a specific cache.xml file.<br>
     * It fills information on cache details, hints, logs, notes and images.<br>
     * 
     * @param dir
     */
    void readCache(String dir) {
	FileReader in = null;
	CacheImage imageInfo;
	// If parent cache has empty waypoint then don't do anything.<br>
	// This might happen when a cache object is freshly created to serve as container for imported data
	if (this.parent.getCode().length() == 0)
	    return;
	File cacheFile = new File(dir + parent.getCode().toLowerCase() + ".xml"); // Kleinschreibung
	if (cacheFile.exists()) {
	    try {
		in = new FileReader(cacheFile.getAbsolutePath());
	    } catch (Exception e) {
		in = null;
	    }
	}
	if (in == null) {
	    cacheFile = new File(dir + parent.getCode() + ".xml"); // gespeicherte Schreibweise
	    if (cacheFile.exists()) {
		try {
		    in = new FileReader(cacheFile.getAbsolutePath());
		} catch (Exception e) {
		}
	    } else {
		return; // leerer neuer Wegpunkt
	    }
	}

	String text = "";
	try {
	    text = in.readAll();
	} catch (Exception e) {
	}

	try {
	    in.close();
	} catch (Exception e) {
	}

	if (text.length() == 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(31415, "Could not read cache details for cache: ") + parent.getCode()).wait(FormBase.OKB);
	    return;
	}

	Extractor ex = new Extractor(text, "<VERSION value = \"", "\"/>", 0, true);
	version = ex.findNext();
	LongDescription = ex.findNext("<DETAILS><![CDATA[", "]]></DETAILS>");
	country = ex.findNext("<COUNTRY><![CDATA[", "]]></COUNTRY>");
	state = ex.findNext("<STATE><![CDATA[", "]]></STATE>");
	attributes.XmlAttributesEnd(ex.findNext("<ATTRIBUTES>", "</ATTRIBUTES>"));
	Hints = ex.findNext("<HINTS><![CDATA[", "]]></HINTS>");

	Extractor subex = new Extractor(ex.findNext("<LOGS>", "</LOGS>"), "<OWNLOGID>", "</OWNLOGID>", 0, true);
	String OwnLogId = subex.findNext();
	String ownLogText = subex.findNext("<OWNLOG><![CDATA[", "]]></OWNLOG>");
	if (ownLogText.length() > 0) {
	    if (ownLogText.indexOf("<img src='") >= 0) {
		ownLog = new Log(ownLogText + "]]>");
		ownLog.setLogID(OwnLogId);
		ownLog.setFinderID(Preferences.itself().gcMemberId);
	    } else {
		ownLog = new Log(OwnLogId, Preferences.itself().gcMemberId, "2.png", "1900-02-02", Preferences.itself().myAlias, ownLogText);
	    }
	} else {
	    ownLog = null;
	}
	CacheLogs.clear();
	String dummy = subex.findNext("<LOG>", "</LOG>");
	while (dummy.length() > 0) {
	    CacheLogs.add(new Log(dummy));
	    dummy = subex.findNext();
	}

	CacheNotes = ex.findNext("<NOTES><![CDATA[", "]]></NOTES>");
	gCNotes = new Extractor(CacheNotes, "<GC>", "</GC>", 0, false).findNext();

	if (version.equals("3")) {
	    images.clear();

	    int searchStart = 0;
	    subex.set(ex.findNext("<IMAGES>", "</IMAGES"), "<IMG>", "</IMG>", 0, true);
	    while ((dummy = subex.findNext()).length() > 0) {
		int pos = dummy.indexOf("<URL>");
		imageInfo = new CacheImage(CacheImage.FROMUNKNOWN);
		if (pos > 0) {
		    imageInfo.setFilename(SafeXML.html2iso8859s1(dummy.substring(0, pos)));
		    imageInfo.setURL(SafeXML.html2iso8859s1((dummy.substring(pos + 5, dummy.indexOf("</URL>")))));
		} else {
		    imageInfo.setFilename(SafeXML.html2iso8859s1(dummy));
		}
		this.images.add(imageInfo);
		searchStart = subex.searchedFrom();
	    }

	    subex.set("<IMGTEXT>", "</IMGTEXT>", searchStart);
	    int imgNr = 0;
	    while ((dummy = subex.findNext()).length() > 0) {
		if (imgNr >= this.images.size()) {
		    // this (more IMGTEXT than IMG in the <cache>.xml, but it happens. So avoid an ArrayIndexOutOfBoundException and add an CacheImage gracefully
		    images.add(new CacheImage(CacheImage.FROMUNKNOWN));
		    Preferences.itself().log("Error reading " + this.parent.getCode() + "More IMGTEXT tags than IMG tags");
		}
		imageInfo = this.images.get(imgNr);
		int pos = dummy.indexOf("<DESC>");
		if (pos > 0) {
		    imageInfo.setTitle(dummy.substring(0, pos));
		    imageInfo.setComment(dummy.substring(pos + 6, dummy.indexOf("</DESC>")));
		} else {
		    imageInfo.setTitle(dummy);
		}
		imgNr = imgNr + 1;
		searchStart = subex.searchedFrom();
	    }

	    logImages.clear();
	    subex.set("<LOGIMG>", "</LOGIMG>", searchStart);
	    while ((dummy = subex.findNext()).length() > 0) {
		imageInfo = new CacheImage(CacheImage.FROMLOG);
		imageInfo.setFilename(dummy);
		logImages.add(imageInfo);
		searchStart = subex.searchedFrom();
	    }
	    subex.set("<LOGIMGTEXT>", "</LOGIMGTEXT>", searchStart);
	    imgNr = 0;
	    while ((dummy = subex.findNext()).length() > 0) {
		imageInfo = logImages.get(imgNr++);
		imageInfo.setTitle(dummy);
		searchStart = subex.searchedFrom();
	    }

	    userImages.clear();
	    subex.set("<USERIMG>", "</USERIMG>", searchStart);
	    while ((dummy = subex.findNext()).length() > 0) {
		imageInfo = new CacheImage(CacheImage.FROMUSER);
		imageInfo.setFilename(dummy);
		userImages.add(imageInfo);
		searchStart = subex.searchedFrom();
	    }
	    subex.set("<USERIMGTEXT>", "</USERIMGTEXT>", searchStart);
	    imgNr = 0;
	    while ((dummy = subex.findNext()).length() > 0) {
		imageInfo = userImages.get(imgNr++);
		imageInfo.setTitle(dummy);
		searchStart = subex.searchedFrom();
	    }
	} else if (version.equals("4")) {
	    String tmp = ex.findNext("<IMAGES>", "</IMAGES");
	    subex.set(tmp, "<IMG ", "</IMG>", 0, true);
	    this.images.clear();
	    this.logImages.clear();
	    this.userImages.clear();
	    Regex getImageInfos = new Regex("SRC=\"(.*?)\" URL=\"(.*?)\"( TITLE=\"(.*?)\")?( CMT=\"(.*?)\")?");
	    while ((dummy = subex.findNext()).length() > 0) {
		String[] parts = mString.split(dummy, '>');
		getImageInfos.search(parts[0]);
		char src = getImageInfos.stringMatched(1).charAt(0);
		imageInfo = new CacheImage(src);
		imageInfo.setFilename(SafeXML.html2iso8859s1(parts[1]));
		imageInfo.setURL(SafeXML.html2iso8859s1(getImageInfos.stringMatched(2)));
		imageInfo.setTitle(getImageInfos.stringMatched(4));
		imageInfo.setComment(getImageInfos.stringMatched(6));
		switch (src) {
		case CacheImage.FROMDESCRIPTION:
		    this.images.add(imageInfo);
		    break;
		case CacheImage.FROMLOG:
		    this.logImages.add(imageInfo);
		    break;
		case CacheImage.FROMSPOILER:
		    this.images.add(imageInfo);
		    break;
		case CacheImage.FROMUSER:
		    this.userImages.add(imageInfo);
		    break;
		default:
		    continue;
		}
	    }
	}

	dummy = ex.findNext("<TRAVELBUGS>", "</TRAVELBUGS>");
	if (dummy.length() > 10) {
	    Travelbugs.addFromXML(dummy);
	}

	dummy = ex.findNext("<URL><![CDATA[", "]]></URL>");
	if (dummy.length() > 10) {
	    URL = dummy;
	    int logpos = URL.indexOf("&"); // &Submit &log=y
	    if (logpos > 0)
		URL = URL.substring(0, logpos);
	} else {
	    // if no URL is stored, set default URL (at this time only possible for gc.com)
	    if (parent.isGC()) {
		URL = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + parent.getCode();
	    }
	}

	this.setSolver(ex.findNext("<SOLVER><![CDATA[", "]]></SOLVER>"));
    }

    public void deleteFile(String FileName) {
	// File exists?
	boolean exists = (new File(FileName)).exists();
	// yes: then delete
	if (exists) {
	    boolean ok = (new File(FileName)).delete();
	    if (ok)
		ok = true;
	}
	boolean exists2 = (new File(FileName.toLowerCase())).exists();
	// yes: delete
	if (exists2) {
	    boolean ok2 = (new File(FileName.toLowerCase())).delete();
	    if (ok2)
		ok2 = true;
	}
    }

    PrintWriter detfile;

    /**
     * Method to save a cache.xml file.
     */
    public void saveCacheDetails(String dir) {
	deleteFile(dir + parent.getCode() + ".xml");
	try {
	    detfile = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir + parent.getCode().toLowerCase() + ".xml").getAbsolutePath())));
	} catch (Exception e) {
	    Preferences.itself().log("Problem creating details file", e, true);
	    return;
	}
	try {
	    if (parent.getCode().length() > 0) {
		detfile.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		detfile.print("<CACHEDETAILS>\r\n");
		detfile.print("<VERSION value = \"" + Profile.CURRENTFILEFORMAT + "\"/>\n");
		detfile.print("<DETAILS><![CDATA[" + LongDescription + "]]></DETAILS>\r\n");
		detfile.print("<COUNTRY><![CDATA[" + country + "]]></COUNTRY>\n");
		detfile.print("<STATE><![CDATA[" + state + "]]></STATE>\n");
		detfile.print(attributes.XmlAttributesWrite());
		detfile.print("<HINTS><![CDATA[" + Hints + "]]></HINTS>\r\n");
		detfile.print("<LOGS>\r\n");
		if (ownLog != null) {
		    detfile.print("<OWNLOGID>" + ownLog.getLogID() + "</OWNLOGID>\r\n");
		    detfile.print("<OWNLOG><![CDATA[" + ownLog.toHtml() + "]]></OWNLOG>\r\n");
		} else {
		    detfile.print("<OWNLOGID></OWNLOGID>\r\n");
		    detfile.print("<OWNLOG><![CDATA[]]></OWNLOG>\r\n");
		}
		for (int i = 0; i < CacheLogs.size(); i++) {
		    detfile.print(CacheLogs.getLog(i).toXML());
		}
		detfile.print("</LOGS>\r\n");

		detfile.print("<NOTES><![CDATA[" + CacheNotes + "]]></NOTES>\n");
		detfile.print("<IMAGES>\n");
		printImages(images);
		printImages(logImages);
		printImages(userImages);
		detfile.print("</IMAGES>\n");

		detfile.print(Travelbugs.toXML());
		detfile.print("<URL><![CDATA[" + URL + "]]></URL>\r\n");
		detfile.print("<SOLVER><![CDATA[" + getSolver() + "]]></SOLVER>\r\n");
		detfile.print(parent.toXML()); // This will allow restoration of index.xml
		detfile.print("</CACHEDETAILS>\n");
		Preferences.itself().log("Writing file: " + parent.getCode().toLowerCase() + ".xml");
	    } // if length
	} catch (Exception e) {
	    Preferences.itself().log("Problem waypoint " + parent.getCode() + " writing to a details file: ", e);
	}
	try {
	    detfile.close();
	} catch (Exception e) {
	    Preferences.itself().log("Problem waypoint " + parent.getCode() + " writing to a details file: ", e);
	}
	hasUnsavedChanges = false;
    }

    private void printImages(CacheImages imgs) {
	for (int i = 0; i < imgs.size(); i++) {
	    CacheImage img = imgs.get(i);
	    detfile.print("<IMG");
	    detfile.print(addAttribute("SRC", String.valueOf(img.getSource())));
	    detfile.print(addAttribute("URL", SafeXML.string2Html(img.getURL())));
	    detfile.print(addAttribute("TITLE", img.getTitle()));
	    detfile.print(addAttribute("CMT", img.getComment()));
	    detfile.print(">");
	    detfile.print(SafeXML.string2Html(img.getFilename()));
	    detfile.print("</IMG>\n");
	}
    }

    private String addAttribute(String att, String attValue) {
	if (attValue.length() > 0) {
	    return " " + att + "=\"" + attValue + "\"";
	} else {
	    return "";
	}

    }

    /**
     * Return true if this cache has additional info for some pictures
     * 
     * @return true if cache has additional info, false otherwise
     */
    public boolean hasCacheImage() {
	for (int i = this.images.size() - 1; i >= 0; i--)
	    if (!this.images.get(i).getComment().equals(""))
		return true;
	return false;
    }

    /**
     * change id in waypoint details and rename associated files. Function should only be called by CacheHolder
     * 
     * @param newWptId
     *            new id of the waypoint
     * @return true on success, false for failure
     */
    protected boolean rename(String newWptId) {
	boolean success = false;
	String profiledir = MainForm.profile.dataDir;
	int oldWptLength = parent.getCode().length();

	// just in case ... (got the pun? ;) )
	newWptId = newWptId.toUpperCase();

	// update image information
	for (int i = 0; i < images.size(); i++) {
	    String filename = images.get(i).getFilename();
	    String comment = images.get(i).getComment();
	    String title = images.get(i).getTitle();
	    if (filename.indexOf(parent.getCode()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		images.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(parent.getCode()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		images.get(i).setComment(comment);
	    }
	    if (title.indexOf(parent.getCode()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		images.get(i).setTitle(title);
	    }
	}
	for (int i = 0; i < logImages.size(); i++) {
	    String filename = logImages.get(i).getFilename();
	    String comment = logImages.get(i).getComment();
	    String title = logImages.get(i).getTitle();
	    if (filename.indexOf(parent.getCode()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		logImages.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(parent.getCode()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		logImages.get(i).setComment(comment);
	    }
	    if (title.indexOf(parent.getCode()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		logImages.get(i).setTitle(title);
	    }
	}
	for (int i = 0; i < userImages.size(); i++) {
	    String filename = userImages.get(i).getFilename();
	    String comment = userImages.get(i).getComment();
	    String title = userImages.get(i).getTitle();
	    if (filename.indexOf(parent.getCode()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		userImages.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(parent.getCode()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		userImages.get(i).setComment(comment);
	    }
	    if (title.indexOf(parent.getCode()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		userImages.get(i).setTitle(title);
	    }
	}

	// rename the files
	try {
	    // since we use *.* we do not need FileBugFix
	    String srcFiles[] = new File(profiledir).list(parent.getCode().concat("*.*"), ewe.io.FileBase.LIST_FILES_ONLY);
	    for (int i = 0; i < srcFiles.length; i++) {
		String newfile = newWptId.concat(srcFiles[i].substring(oldWptLength));
		File srcFile = new File(profiledir.concat(srcFiles[i]));
		File dstFile = new File(profiledir.concat(newfile));
		srcFile.move(dstFile);
	    }
	    success = true;
	} catch (Exception e) {
	    Preferences.itself().log("Error renaming waypoint details", e, true);
	    // TODO: any chance of a roll back?
	    // TODO: should we ignore a file not found?
	}
	hasUnsavedChanges = true;
	return success;
    }
}
