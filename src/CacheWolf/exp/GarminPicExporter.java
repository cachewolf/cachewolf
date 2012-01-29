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

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.DataMover;
import CacheWolf.Global;
import CacheWolf.ImageInfo;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.FileBase;
import ewe.io.File;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;

public class GarminPicExporter {
/************************************************************************************
 * Exports pictures and spoiler pictures into a directory structure                 *
 * See: http://garmin.blogs.com/softwareupdates/2012/01/geocaching-with-photos.html *
 ************************************************************************************/
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	Hashtable picsCopied=new Hashtable(40);
	/* This table is used by copyImages to check whether a picture has been copied already.
	 * Normally it should be created in copyImages (and destroyed upon reeturn). To avoid
	 * unneeded object generation, it is created here and cleared in copyImages (which is a
	 * much faster operation).
	 */
	int nonJPGimages=0;

	public GarminPicExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}

	public final static String expName = "GARMIN";

	public void doIt(){
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int exportErrors = 0;

		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.getExportPath(expName));
		fc.setTitle("Select target directory:");
		String targetDir;
		if(fc.execute() != FormBase.IDCANCEL){
			targetDir = fc.getChosen() + "/";
			pref.setExportPath(expName, targetDir);

			int counter = cacheDB.countVisible();

			pbf.showMainTask = false;
			pbf.setTask(h,"Exporting ...");
			pbf.exec();

			for(int i = 0; i<counter;i++){
				h.progress = (float)(i+1)/(float)counter;
				h.changed();

				ch = cacheDB.get(i);
				if(	ch.isVisible()){
					if (ch.is_incomplete()) {
						exportErrors++;
						Global.getPref().log("GarminPicExporter: skipping export of incomplete waypoint "+ch.getWayPoint());
						continue;
					}
					exportErrors+=copyImages(ch, targetDir);
				}//if is black, filtered
			}
		}//if
		pbf.exit(0);

		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors+" errors during export. See log for details.", FormBase.OKB).execute();
		}
		if (nonJPGimages > 0) {
			new MessageBox("Some pictures not copied", nonJPGimages+" pictures were not copied because Garmin GPS can only handle the JPG format. See log for details.", FormBase.OKB).execute();
		}

	}

	private int copyImages(CacheHolder ch, String targetDir) {
		String dirName;
		CacheHolderDetail det=ch.getCacheDetails(false);
		if (det==null) return 1; // No details; increment export errors
		int nImg=det.images.size();
		if (nImg==0) return 0;  // Nothing to copy
		int retCode=0;
		dirName=createPicDir(targetDir, ch.getWayPoint());
		if (dirName==null) return 1; // Failed to create dir

		picsCopied.clear(); // Clear the hashtable which keeps track of pictures copied
		for (int i=0; i<nImg; i++) {
			ImageInfo imgInfo=det.images.get(i);
			if (!imgInfo.getFilename().endsWith("jpg")) { // relies on filename being lower case
				// Garmin GPS can only handle jpg files
				Global.getPref().log("GarminPicExporter: Warning: Picture "+imgInfo.getFilename()+" not copied as Garmin GPS can only handle jpg files");
				nonJPGimages++;
				continue; // Move to next pic
			}
			// Skip this pic if it was already copied
			if (picsCopied.containsKey(imgInfo.getFilename())) {
				continue;
			}
			picsCopied.put(imgInfo.getFilename(), null); // Remember that we copied this picture
			try {
				if (imgInfo.getTitle().toUpperCase().indexOf("SPOILER")>=0) {
					// Copy image to spoiler dir
					appendDir(dirName,"Spoilers/");

					DataMover.copy(profile.dataDir +imgInfo.getFilename(),dirName+"Spoilers/"+imgInfo.getFilename());
				} else {
					DataMover.copy(profile.dataDir +imgInfo.getFilename(),dirName+imgInfo.getFilename());
				}
			} catch (Exception ex) {
				Global.getPref().log("GarminPicExporter: Error copying file "+imgInfo.getFilename());
				retCode=1; // Signal error to calling program
			}
		}
		return retCode;
	}

	/**
	 * Create the directories to receive the pictures
	 *
	 * Photos
	 * \exportDir\Last Character\Second To Last Character\Full Code\
	 *
	 * Spoiler Photos
	 * <Photos Path>\Spoilers\
	 *
	 * If the geocache has only three characters total, a 0 (zero) is used for the second to last character.
	 *
	 * The exportDir has to be copied to
	 */
	private String createPicDir(String targetDir, String wayPoint) {
		String dirName;
		int len=wayPoint.length();
		String dir1,dir2;
		dir1=wayPoint.charAt(len-1)+"/";
		if (len>3)
			dir2=wayPoint.charAt(len-2)+"/";
		else
			dir2="0";
		try {
			String GCF="GeocachePhotos/";
			dirName=appendDir(appendDir(appendDir(appendDir(targetDir,GCF),dir1),dir2),wayPoint);
		} catch (Exception ex) {
			Global.getPref().log("GarminPicExporter: Error creating directories for cache "+wayPoint+"\n"+
					ex.getMessage());
			return null; // Signal error to calling program
		}
		return dirName;
	}

	private String appendDir(String baseDir, String appendDir) {
		File file=new FileBugfix(baseDir+appendDir);
		if (!file.exists()) file.createDir();
		return file.getAbsolutePath()+"/";
	}
}
