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
import CacheWolf.utils.Extractor;
import CacheWolf.utils.Files;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.ui.FormBase;
import ewe.ui.InputBox;
import ewe.util.Vector;

public class CacheHolderDetail {
    /** CacheHolder which holds the detail. <b>Only</b> set by CacheHolder when creating detail! **/
    private CacheHolder parent = null;
    public String LongDescription = CacheHolder.EMPTY;
    public String LastUpdate = CacheHolder.EMPTY;
    public String Hints = CacheHolder.EMPTY;
    public LogList CacheLogs = new LogList();
    private String CacheNotes = CacheHolder.EMPTY;
    public CacheImages images = new CacheImages();
    public CacheImages logImages = new CacheImages();
    public CacheImages userImages = new CacheImages();
    public Attributes attributes = new Attributes();
    public Vector CacheIcons = new Vector();
    public TravelbugList Travelbugs = new TravelbugList();
    // public String Bugs = EMPTY; Superceded by Travelbugs
    public String URL = CacheHolder.EMPTY;
    private String Solver = CacheHolder.EMPTY;
    public String OwnLogId = CacheHolder.EMPTY;
    public Log OwnLog = null;
    public String Country = CacheHolder.EMPTY;
    public String State = CacheHolder.EMPTY;
    /**
     * For faster cache import (from opencaching) changes are only written when the details are freed from memory
     * If you want to save the changes automatically when the details are unloaded, set this to true
     */
    public boolean hasUnsavedChanges = false;

    public CacheHolderDetail(CacheHolder ch) {
	parent = ch;
    }

    public CacheHolder getParent() {
	return parent;
    }

    public void setLongDescription(String longDescription) {
	String s = stripControlChars(longDescription);
	if (LongDescription.equals(""))
	    getParent().setNew(true);
	else {
	    if (!s.equals(LongDescription)) {
		getParent().setUpdated(true);
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
	    getParent().setUpdated(true);
	Hints = hints;
    }

    public void setSolver(String solver) {
	if (!Solver.equals(solver))
	    getParent().setUpdated(true);
	getParent().setHasSolver(!solver.trim().equals(""));
	Solver = solver;
    }

    public String getSolver() {
	return this.Solver;
    }

    private String gCNotes = "";

    public String getGCNotes() {
	return gCNotes;
    }

    public void setGCNotes(String notes) {
	gCNotes = notes;
    }

    public void setCacheNotes(String notes) {
	if (!this.CacheNotes.equals(notes)) {
	    getParent().setUpdated(true);
	    this.CacheNotes = notes;
	    getParent().setHasNote(!this.CacheNotes.trim().equals(""));
	}
    }

    public String getCacheNotes() {
	return this.CacheNotes;
    }

    public void setCacheLogs(LogList newLogs) {
	if (Preferences.itself().overwriteLogs) {
	    CacheLogs = newLogs;
	    getParent().setLog_updated(true);
	    hasUnsavedChanges = true;
	} else {
	    int size = newLogs.size();
	    for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must start with oldest log
		if (CacheLogs.merge(newLogs.getLog(i)) >= 0)
		    getParent().setLog_updated(true);
	    }
	}
	if (CacheLogs.purgeLogs() > 0)
	    hasUnsavedChanges = true;
	getParent().setNoFindLogs(CacheLogs.countNotFoundLogs());
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
	if (newChD.OwnLogId.length() > 0)
	    this.OwnLogId = newChD.OwnLogId;
	if (newChD.OwnLog != null) {
	    this.OwnLog = newChD.OwnLog;
	}
	if (newChD.Country.length() > 0)
	    this.Country = newChD.Country;
	if (newChD.State.length() > 0)
	    this.State = newChD.State;
	if (newChD.getSolver().length() > 0)
	    this.setSolver(newChD.getSolver());
	return this;
    }

    /**
     * Adds a user image to the cache data
     * 
     * @param profile
     */
    public void addUserImage() {
	File imgFile;
	String imgDesc, imgDestName;

	// Get Image and description
	FileChooser fc = new FileChooser(FileChooserBase.OPEN, MainForm.profile.dataDir);
	fc.setTitle("Select image file:");
	if (fc.execute() != FormBase.IDCANCEL) {
	    imgFile = fc.getChosenFile();
	    imgDesc = new InputBox("Description").input("", 10);
	    // Create Destination Filename
	    String ext = imgFile.getFileExt().substring(imgFile.getFileExt().lastIndexOf('.'));
	    imgDestName = getParent().getWayPoint() + "_U_" + (this.userImages.size() + 1) + ext;

	    CacheImage userCacheImage = new CacheImage();
	    userCacheImage.setFilename(imgDestName);
	    userCacheImage.setTitle(imgDesc);
	    this.userImages.add(userCacheImage);
	    // Copy File
	    Files.copy(imgFile.getFullPath(), MainForm.profile.dataDir + imgDestName);
	    // Save Data
	    saveCacheDetails(MainForm.profile.dataDir);
	}
    }

    /**
     * Method to parse a specific cache.xml file.
     * It fills information on cache details, hints, logs, notes and
     * images.
     */
    void readCache(String dir) throws IOException {
	FileReader in = null;
	CacheImage imageInfo;
	// If parent cache has empty waypoint then don't do anything. This might happen
	// when a cache object is freshly created to serve as container for imported data
	if (this.getParent().getWayPoint().equals(CacheHolder.EMPTY))
	    return;
	File cacheFile = new File(dir + getParent().getWayPoint().toLowerCase() + ".xml");
	if (cacheFile.exists()) {
	    try {
		in = new FileReader(cacheFile.getAbsolutePath());
	    } catch (FileNotFoundException e) {
		in = null; // exception is thrown again below, if file could not be found in upper case, too
	    }
	}
	if (in == null) {
	    cacheFile = new File(dir + getParent().getWayPoint() + ".xml");
	    if (cacheFile.exists()) {
		in = new FileReader(cacheFile.getAbsolutePath());
	    }
	}

	if (in == null)
	    throw new FileNotFoundException(dir + getParent().getWayPoint().toLowerCase() + ".xml");
	// Preferences.itself().log("Reading file " + getParent().getWayPoint() + ".xml");
	String text = in.readAll();
	in.close();

	Extractor ex = new Extractor(text, "<DETAILS><![CDATA[", "]]></DETAILS>", 0, true);
	LongDescription = ex.findNext();
	Country = ex.findNext("<COUNTRY><![CDATA[", "]]></COUNTRY>");
	State = ex.findNext("<STATE><![CDATA[", "]]></STATE>");
	attributes.XmlAttributesEnd(ex.findNext("<ATTRIBUTES>", "</ATTRIBUTES>"));
	Hints = ex.findNext("<HINTS><![CDATA[", "]]></HINTS>");

	Extractor subex = new Extractor(ex.findNext("<LOGS>", "</LOGS>"), "<OWNLOGID>", "</OWNLOGID>", 0, true);
	OwnLogId = subex.findNext();
	String ownLogText = subex.findNext("<OWNLOG><![CDATA[", "]]></OWNLOG>");
	if (ownLogText.length() > 0) {
	    if (ownLogText.indexOf("<img src='") >= 0) {
		OwnLog = new Log(ownLogText + "]]>");
		OwnLog.setLogID(OwnLogId);
		OwnLog.setFinderID(Preferences.itself().gcMemberId);
	    } else {
		OwnLog = new Log(OwnLogId, Preferences.itself().gcMemberId, "2.png", "1900-01-01", Preferences.itself().myAlias, ownLogText);
	    }
	} else {
	    OwnLog = null;
	}
	CacheLogs.clear();
	String dummy = subex.findNext("<LOG>", "</LOG>");
	while (dummy.length() > 0) {
	    CacheLogs.add(new Log(dummy));
	    dummy = subex.findNext();
	}

	CacheNotes = ex.findNext("<NOTES><![CDATA[", "]]></NOTES>");
	gCNotes = new Extractor(CacheNotes, "<GC>", "</GC>", 0, false).findNext();

	images.clear();

	int searchStart = 0;
	subex.set(ex.findNext("<IMAGES>", "</IMAGES"), "<IMG>", "</IMG>", 0, true);
	while ((dummy = subex.findNext()).length() > 0) {
	    imageInfo = new CacheImage();
	    int pos = dummy.indexOf("<URL>");
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
		images.add(new CacheImage()); // this (more IMGTEXT than IMG in the <cache>.xml, but it happens. So avoid an ArrayIndexOutOfBoundException and add an CacheImage gracefully
		Preferences.itself().log("Error reading " + this.getParent().getWayPoint() + "More IMGTEXT tags than IMG tags");
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
	    imageInfo = new CacheImage();
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
	    imageInfo = new CacheImage();
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
	    if (getParent().getWayPoint().startsWith("GC")) {
		URL = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + getParent().getWayPoint();
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

    /**
     * Method to save a cache.xml file.
     */
    public void saveCacheDetails(String dir) {
	PrintWriter detfile;
	deleteFile(dir + getParent().getWayPoint() + ".xml");
	try {
	    detfile = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir + getParent().getWayPoint().toLowerCase() + ".xml").getAbsolutePath())));
	} catch (Exception e) {
	    Preferences.itself().log("Problem creating details file", e, true);
	    return;
	}
	try {
	    if (getParent().getWayPoint().length() > 0) {
		detfile.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		detfile.print("<CACHEDETAILS>\r\n");
		detfile.print("<VERSION value = \"3\"/>\n");
		detfile.print("<DETAILS><![CDATA[" + LongDescription + "]]></DETAILS>\r\n");
		detfile.print("<COUNTRY><![CDATA[" + Country + "]]></COUNTRY>\n");
		detfile.print("<STATE><![CDATA[" + State + "]]></STATE>\n");
		detfile.print(attributes.XmlAttributesWrite());
		detfile.print("<HINTS><![CDATA[" + Hints + "]]></HINTS>\r\n");
		detfile.print("<LOGS>\r\n");
		detfile.print("<OWNLOGID>" + OwnLogId + "</OWNLOGID>\r\n");
		if (OwnLog != null) {
		    detfile.print("<OWNLOG><![CDATA[" + OwnLog.toHtml() + "]]></OWNLOG>\r\n");
		} else {
		    detfile.print("<OWNLOG><![CDATA[]]></OWNLOG>\r\n");
		}
		for (int i = 0; i < CacheLogs.size(); i++) {
		    detfile.print(CacheLogs.getLog(i).toXML());
		}
		detfile.print("</LOGS>\r\n");

		detfile.print("<NOTES><![CDATA[" + CacheNotes + "]]></NOTES>\n");
		detfile.print("<IMAGES>\n");
		String stbuf;
		for (int i = 0; i < images.size(); i++) {
		    stbuf = images.get(i).getFilename();
		    String urlBuf = images.get(i).getURL();
		    if (urlBuf != null && !urlBuf.equals("")) {
			detfile.print("    <IMG>" + SafeXML.string2Html(stbuf) + "<URL>" + SafeXML.string2Html(urlBuf) + "</URL></IMG>\n");
		    } else {
			detfile.print("    <IMG>" + SafeXML.string2Html(stbuf) + "</IMG>\n");
		    }
		}
		int iis = images.size();
		for (int i = 0; i < iis; i++) {
		    stbuf = images.get(i).getTitle();
		    if (i < iis && !images.get(i).getComment().equals(""))
			detfile.print("    <IMGTEXT>" + stbuf + "<DESC>" + images.get(i).getComment() + "</DESC></IMGTEXT>\n");
		    else
			detfile.print("    <IMGTEXT>" + stbuf + "</IMGTEXT>\n");
		}

		for (int i = 0; i < logImages.size(); i++) {
		    stbuf = logImages.get(i).getFilename();
		    detfile.print("    <LOGIMG>" + stbuf + "</LOGIMG>\n");
		}
		for (int i = 0; i < logImages.size(); i++) {
		    stbuf = logImages.get(i).getTitle();
		    detfile.print("    <LOGIMGTEXT>" + stbuf + "</LOGIMGTEXT>\n");
		}
		for (int i = 0; i < userImages.size(); i++) {
		    stbuf = userImages.get(i).getFilename();
		    detfile.print("    <USERIMG>" + stbuf + "</USERIMG>\n");
		}
		for (int i = 0; i < userImages.size(); i++) {
		    stbuf = userImages.get(i).getTitle();
		    detfile.print("    <USERIMGTEXT>" + stbuf + "</USERIMGTEXT>\n");
		}

		detfile.print("</IMAGES>\n");
		detfile.print(Travelbugs.toXML());
		detfile.print("<URL><![CDATA[" + URL + "]]></URL>\r\n");
		detfile.print("<SOLVER><![CDATA[" + getSolver() + "]]></SOLVER>\r\n");
		detfile.print(getParent().toXML()); // This will allow restoration of index.xml
		detfile.print("</CACHEDETAILS>\n");
		Preferences.itself().log("Writing file: " + getParent().getWayPoint().toLowerCase() + ".xml");
	    } // if length
	} catch (Exception e) {
	    Preferences.itself().log("Problem waypoint " + getParent().getWayPoint() + " writing to a details file: ", e);
	}
	try {
	    detfile.close();
	} catch (Exception e) {
	    Preferences.itself().log("Problem waypoint " + getParent().getWayPoint() + " writing to a details file: ", e);
	}
	hasUnsavedChanges = false;
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
	int oldWptLength = getParent().getWayPoint().length();

	// just in case ... (got the pun? ;) )
	newWptId = newWptId.toUpperCase();

	// update image information
	for (int i = 0; i < images.size(); i++) {
	    String filename = images.get(i).getFilename();
	    String comment = images.get(i).getComment();
	    String title = images.get(i).getTitle();
	    if (filename.indexOf(getParent().getWayPoint()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		images.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(getParent().getWayPoint()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		images.get(i).setComment(comment);
	    }
	    if (title.indexOf(getParent().getWayPoint()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		images.get(i).setTitle(title);
	    }
	}
	for (int i = 0; i < logImages.size(); i++) {
	    String filename = logImages.get(i).getFilename();
	    String comment = logImages.get(i).getComment();
	    String title = logImages.get(i).getTitle();
	    if (filename.indexOf(getParent().getWayPoint()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		logImages.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(getParent().getWayPoint()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		logImages.get(i).setComment(comment);
	    }
	    if (title.indexOf(getParent().getWayPoint()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		logImages.get(i).setTitle(title);
	    }
	}
	for (int i = 0; i < userImages.size(); i++) {
	    String filename = userImages.get(i).getFilename();
	    String comment = userImages.get(i).getComment();
	    String title = userImages.get(i).getTitle();
	    if (filename.indexOf(getParent().getWayPoint()) == 0) {
		filename = newWptId.concat(filename.substring(oldWptLength));
		userImages.get(i).setFilename(filename);
	    }
	    if (comment.indexOf(getParent().getWayPoint()) == 0) {
		comment = newWptId.concat(comment.substring(oldWptLength));
		userImages.get(i).setComment(comment);
	    }
	    if (title.indexOf(getParent().getWayPoint()) == 0) {
		title = newWptId.concat(title.substring(oldWptLength));
		userImages.get(i).setTitle(title);
	    }
	}

	// rename the files
	try {
	    // since we use *.* we do not need FileBugFix
	    String srcFiles[] = new File(profiledir).list(getParent().getWayPoint().concat("*.*"), ewe.io.FileBase.LIST_FILES_ONLY);
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
