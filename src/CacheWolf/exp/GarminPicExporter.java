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
package CacheWolf.exp;

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheImage;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Files;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;

import com.stevesoft.ewe_pat.Regex;

import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.File;
import ewe.sys.Handle;
import ewe.ui.CellConstants;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Panel;
import ewe.ui.ProgressBarForm;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.util.Hashtable;

public class GarminPicExporter {
    /************************************************************************************
     * Exports pictures and spoiler pictures into a directory structure * See: http://garmin.blogs.com/softwareupdates/2012/01/geocaching-with-photos.html *
     ************************************************************************************/
    private static String SPOILERREGEX = "Spoiler|Hilfe|Help|Hinweis|Hint[^a-zA-Z]";

    CacheDB cacheDB;
    Hashtable picsCopied = new Hashtable(40);
    /* This table is used by copyImages to check whether a picture has been copied already.
     * Normally it should be created in copyImages (and destroyed upon return). To avoid
     * unneeded object generation, it is created here and cleared in copyImages (which is a
     * much faster operation).
     */
    int nonJPGimages = 0;
    int whichPics; // 0=ALL, 1=SPOILER ONLY, 2=OTHERS ONLY, 3=SPOILERS + ALL PICS for non TRADIS
    boolean removeGC;
    boolean resizeLongEdge;
    int maxLongEdge;
    Regex spoilerRex;

    public GarminPicExporter() {
	cacheDB = MainForm.profile.cacheDB;
    }

    public final static String expName = "GARMIN";

    public void doIt() {
	// Select destination directory
	FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().getExportPath(expName));
	fc.setTitle("Select target directory:");
	String targetDir;
	if (fc.execute() == FormBase.IDCANCEL)
	    return;
	targetDir = fc.getChosen() + "/";
	Preferences.itself().setExportPref(expName, targetDir);

	// Select export options
	OptionsForm options = new OptionsForm();
	if (options.execute() == FormBase.IDCANCEL)
	    return;
	whichPics = options.getWhichPics();
	resizeLongEdge = options.getResizeLongEdge();
	maxLongEdge = options.getMaxLongEdge();
	spoilerRex = new Regex(options.getSplrRegex());
	spoilerRex.setIgnoreCase(true);

	// Keep user updated about our progress
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();
	pbf.showMainTask = false;
	pbf.setTask(h, "Exporting ...");
	pbf.exec();
	int exportErrors = 0;
	int counter = cacheDB.countVisible();

	// Main loop over visible caches
	for (int i = 0; i < counter; i++) {
	    h.progress = (float) (i + 1) / (float) counter;
	    h.changed();

	    CacheHolder ch = cacheDB.get(i);
	    if (ch.isVisible()) {
		if (ch.isIncomplete()) {
		    exportErrors++;
		    Preferences.itself().log("GarminPicExporter: skipping export of incomplete waypoint " + ch.getCode());
		    continue;
		}
		exportErrors += copyImages(ch, targetDir);
	    }//if is black, filtered
	}
	pbf.exit(0);

	if (exportErrors > 0) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), exportErrors + " errors during export. See log for details.").wait(FormBase.OKB);
	}
	if (nonJPGimages > 0) {
	    new InfoBox("Some pictures not copied", nonJPGimages + " pictures were not copied because Garmin GPS can only handle the JPG format. See log for details.").wait(FormBase.OKB);
	}

    }

    private int copyImages(CacheHolder ch, String targetDir) {
	String dirName = "";
	CacheHolderDetail det = ch.getDetails();
	if (det == null)
	    return 1; // No details; increment export errors
	int nImg = det.images.size();
	if (nImg == 0)
	    return 0; // Nothing to copy
	int retCode = 0;
	boolean need2CreateDir = true; // Flag to remember whether dirs have been created

	picsCopied.clear(); // Clear the hashtable which keeps track of pictures copied
	for (int i = nImg - 1; i >= 0; i--) { // Start from top to get more pics with sensible names
	    // The pictures embedded in the text description have no title and are at the beginning
	    CacheImage imgInfo = det.images.get(i);
	    // Skip this pic if it was already copied
	    if (picsCopied.containsKey(imgInfo.getFilename())) {
		continue;
	    }
	    try {
		boolean copyPic = false; // Does the pic satisfy the criteria for copying?
		boolean isSpoiler = false;
		if (spoilerRex.search(imgInfo.getTitle())) {
		    isSpoiler = true;
		    if (whichPics != 2)
			copyPic = true;
		} else { // Normal Pic
		    if (whichPics == 0 || whichPics == 2 || (whichPics == 3 && ch.getType() != CacheType.CW_TYPE_TRADITIONAL))
			copyPic = true;
		}
		if (copyPic) { // We have to copy this picture
		    if (!imgInfo.getFilename().endsWith("jpg")) { // relies on filename being lower case
			// Garmin GPS can only handle jpg files
			Preferences.itself().log("GarminPicExporter: Warning: Picture " + imgInfo.getFilename() + " not copied as Garmin GPS can only handle jpg files");
			nonJPGimages++;
			continue; // Move to next pic
		    }
		    // Now we have a jpg and need to ensure that the directory structure
		    // has been created before copying the picture
		    if (need2CreateDir) {
			dirName = createPicDir(targetDir, ch.getCode());
			if (dirName == null)
			    return 1; // Failed to create dir
			need2CreateDir = false;
		    }
		    picsCopied.put(imgInfo.getFilename(), null); // Remember that we copied this picture
		    if (isSpoiler) {
			appendDir(dirName, "Spoilers/");
			Files.copy(MainForm.profile.dataDir + imgInfo.getFilename(), dirName + "Spoilers/" + sanitizeFileName(imgInfo.getTitle()) + ".JPG");
		    } else {
			Files.copy(MainForm.profile.dataDir + imgInfo.getFilename(), dirName + sanitizeFileName(imgInfo.getTitle()) + ".JPG");
		    }
		}
	    } catch (Exception ex) {
		Preferences.itself().log("GarminPicExporter: Error copying file " + imgInfo.getFilename());
		retCode = 1; // Signal error to calling program
	    }
	}
	return retCode;
    }

    private String sanitizeFileName(String fileName) {
	// The next line should not be necessary as picture titles should be correctly stored in UTF8 internally
	// Unfortunately this is not the case, e.g. GC1ZHRK
	if (fileName.indexOf("&") >= 0)
	    fileName = SafeXML.html2iso8859s1(fileName);
	int len = fileName.length();
	StringBuffer s = new StringBuffer(len);
	for (int i = 0; i < len; i++) {
	    char ch = fileName.charAt(i);
	    if ((ch == ' ') || (ch == '_') || ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) || ((ch >= '0') && (ch <= '9')) || (ch == 'ä') || (ch == 'Ä') || (ch == 'ü') || (ch == 'Ü') || (ch == 'ö') || (ch == 'Ö') || (ch == 'ß'))
		s.append(ch);
	}
	return s.toString();
    }

    /**
     * Create the directories to receive the pictures
     * 
     * Photos \exportDir\Last Character\Second To Last Character\Full Code\
     * 
     * Spoiler Photos <Photos Path>\Spoilers\
     * 
     * If the geocache has only three characters total, a 0 (zero) is used for the second to last character.
     * 
     * The exportDir has to be copied to
     */
    private String createPicDir(String targetDir, String wayPoint) {
	String dirName;
	int len = wayPoint.length();
	String dir1, dir2;
	dir1 = wayPoint.charAt(len - 1) + "/";
	if (len > 3)
	    dir2 = wayPoint.charAt(len - 2) + "/";
	else
	    dir2 = "0";
	try {
	    String GCF = "GeocachePhotos/";
	    dirName = appendDir(appendDir(appendDir(appendDir(targetDir, GCF), dir1), dir2), wayPoint);
	} catch (Exception ex) {
	    Preferences.itself().log("GarminPicExporter: Error creating directories for cache " + wayPoint + "\n" + ex.getMessage());
	    return null; // Signal error to calling program
	}
	return dirName;
    }

    /**
     * Creates a sub-directory within a base directory
     * 
     * @param baseDir
     *            The full path of the base directory to be extended
     * @param appendDir
     *            The name of the sub directory
     * @return The full path pointing to the newly created sub-directory
     */
    private String appendDir(String baseDir, String appendDir) {
	File file = new File(baseDir + appendDir);
	if (!file.exists())
	    file.createDir();
	return file.getAbsolutePath() + "/";
    }

    /**********************************************
     * Form for options
     ********************************************** 
     */

    private class OptionsForm extends Form {
	Panel pnlWhichPics = new Panel();
	mCheckBox chkAllPics, chkSplrOnly, chkPicsOnly, chkSplrPlusNonTradi;
	CheckBoxGroup chkPics2Copy = new CheckBoxGroup();
	mInput inpSplrRegex;
	mCheckBox chkResizeLongEdge;
	mLabel lblMaxLongEdge;
	mInput inpMaxLongEdge;
	private final ExecutePanel executePanel;

	public OptionsForm() {
	    super();
	    //this.setPreferredSize(400,300);
	    this.setTitle("Export Options");
	    // Which pictures to export (Radiobuttons)
	    pnlWhichPics.addLast(chkAllPics = new mCheckBox("All"), CellConstants.DONTSTRETCH, CellConstants.WEST);
	    pnlWhichPics.addLast(chkSplrOnly = new mCheckBox("Only Spoilers"), CellConstants.DONTSTRETCH, CellConstants.WEST);
	    pnlWhichPics.addLast(chkPicsOnly = new mCheckBox("Only Pictures"), CellConstants.DONTSTRETCH, CellConstants.WEST);
	    pnlWhichPics.addLast(chkSplrPlusNonTradi = new mCheckBox("All Spoilers plus all pics for non-tradi Caches"), CellConstants.DONTSTRETCH, CellConstants.WEST);
	    chkAllPics.setGroup(chkPics2Copy);
	    chkSplrOnly.setGroup(chkPics2Copy);
	    chkPicsOnly.setGroup(chkPics2Copy);
	    chkSplrPlusNonTradi.setGroup(chkPics2Copy);
	    chkPics2Copy.setInt(0);
	    addLast(new mLabel("Which pictures to export"), CellConstants.DONTSTRETCH, CellConstants.TOP | CellConstants.WEST);
	    pnlWhichPics.setBorder(BDR_OUTLINE | BF_RECT, 3);

	    addLast(pnlWhichPics, CellConstants.HSTRETCH, CellConstants.TOP | CellConstants.WEST);
	    addLast(new mLabel("Regular Expression to identify spoiler pictures (ignore case):"));
	    addLast(inpSplrRegex = new mInput(SPOILERREGEX), CellConstants.HSTRETCH, CellConstants.HFILL | CellConstants.TOP | CellConstants.WEST);

	    // Should the longest edge be resized and if so to which max length
	    //TODO addLast(chkResizeLongEdge=new mCheckBox("Resize long edge of large pictures (takes much longer)"));
	    //addNext(lblMaxLongEdge=new mLabel("Max. long edge:"),CellConstants.DONTSTRETCH,CellConstants.TOP|CellConstants.WEST);
	    //addLast(inpMaxLongEdge=new mInput("1000"),CellConstants.DONTSTRETCH,CellConstants.TOP|CellConstants.WEST);
	    //lblMaxLongEdge.set(Disabled,true);
	    //inpMaxLongEdge.set(Disabled,true);

	    // Buttons to control closing of the form
	    executePanel = new ExecutePanel(this);
	}

	public void onEvent(Event ev) {
	    if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
		if (ev.target == executePanel.cancelButton) {
		    this.close(FormBase.IDCANCEL);
		}
		if (ev.target == executePanel.applyButton) {
		    this.close(FormBase.IDOK);
		}
		if (ev.target == chkResizeLongEdge) {
		    lblMaxLongEdge.set(Disabled, !chkResizeLongEdge.getState());
		    inpMaxLongEdge.set(Disabled, !chkResizeLongEdge.getState());
		    this.repaint();
		}
	    }
	    super.onEvent(ev);
	}

	public int getWhichPics() {
	    return chkPics2Copy.getInt();
	}

	public String getSplrRegex() {
	    return inpSplrRegex.getText();
	}

	public boolean getResizeLongEdge() {
	    return false;
	    //TODO return chkResizeLongEdge.getState();
	}

	public int getMaxLongEdge() {
	    return 100000;
	    //TODO return Common.parseInt(inpMaxLongEdge.getText());
	}
    }

}
