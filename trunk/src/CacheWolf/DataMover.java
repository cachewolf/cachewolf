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

import CacheWolf.utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.FileOutputStream;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.ProgressBarForm;
import ewe.util.Iterator;


/**
*	This class moves or copies the database files of selected caches from one directory to
*   another. It provides also the possibility to delete cachefiles.
*/
public class DataMover {

	private CacheDB srcDB;
	private Preferences pref;
	private Profile profile;

	public DataMover(){
		pref = Global.getPref();
		profile=Global.getProfile();
		srcDB = profile.cacheDB;
	}
	public void deleteCaches(){

		int mode = showMessageBox(251,"All waypoints will be deleted");
		if (mode == -1){
			return;
		}

		processCaches(new Deleter(MyLocale.getMsg(143, "Delete")), mode);
		// write indexfiles
		profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
	}

	public void copyCaches(){
		Profile dstProfile=new Profile();

		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;

		//Von Andi P
		int mode = showMessageBox(253,"All waypoints will be copied");
		if (mode == -1){
			return;
		}
		//Ende

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();
		}
		processCaches(new Copier(MyLocale.getMsg(141, "Copy"),dstProfile), mode);
		// write indexfiles and keep the filter status
		dstProfile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
		//Now repair the cache-Vector:
		for(int i =0; i < srcDB.size();i++){
			CacheHolder holder = srcDB.get(i);
			for(Iterator j=holder.addiWpts.iterator(); j.hasNext();){
				CacheHolder element = (CacheHolder) j.next();
				element.mainCache = holder;
			}
		}
	}

	/**
	 * Shows the messagebox before copying/moving/deleting waypoints.
	 * It returns the mode selected by the user.
	 * 0 means all visible
	 * 1 means all ticked
	 * 2 means all visible and ticked cache
	 * -1 means the user has pressed `Cancel' and no action has to be taken.
	 * @return mode selected by the user
	 */
	private int showMessageBox(int actionTextNr, String defaultValue) {
		DataMoverForm cpf = new DataMoverForm (makeTickedText (), makeVisibleText (),makeVisibleTickedText (), MyLocale.getMsg(actionTextNr, defaultValue) );
		int dialogResult = cpf.execute(null, Gui.CENTER_FRAME);
		if (dialogResult != FormBase.IDOK){
			return -1;
		}
		int mode = cpf.getMode();
		return mode;
	}

	private String makeTickedText() {
		int size=srcDB.size();
		int countMainWP=0;
		int countAddiWP=0;
		// Count the number of caches to move/delete/copy
		for(int i = 0; i<size; i++) {
			if(srcDB.get(i).is_Checked && !srcDB.get(i).isAddiWpt()){
				countMainWP++;
			}
			else if (srcDB.get(i).is_Checked && srcDB.get(i).isAddiWpt()){
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(255, "All ticked") + " ("+countMainWP+
			' ' + MyLocale.getMsg(257, "Main") + ", "+countAddiWP +
			MyLocale.getMsg(258, " Addi") +')';
	}

	private String makeVisibleText() {
		int size=srcDB.size();
		int countMainWP=0;
		int countAddiWP=0;
		// Count the number of caches to move/delete/copy
		for(int i = 0; i<size; i++) {
			if(srcDB.get(i).isVisible() && !srcDB.get(i).isAddiWpt()){
				countMainWP++;
			}
			else if (srcDB.get(i).isVisible() && srcDB.get(i).isAddiWpt()){
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(254, "All visible") + " ("+countMainWP+
			' ' + MyLocale.getMsg(257, "Main") + ", "+countAddiWP +
			MyLocale.getMsg(258, " Addi") +')';
	}

	private String makeVisibleTickedText() {
		int size=srcDB.size();
		int countMainWP=0;
		int countAddiWP=0;
		// Count the number of caches to move/delete/copy
		for(int i = 0; i<size; i++) {
			if(srcDB.get(i).isVisible() && srcDB.get(i).is_Checked && !srcDB.get(i).isAddiWpt()){
				countMainWP++;
			}
			else if (srcDB.get(i).isVisible() && srcDB.get(i).is_Checked && srcDB.get(i).isAddiWpt()){
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(256, "All visible and ticked") + " ("+countMainWP+
		' ' + MyLocale.getMsg(257, "Main") + ", "+countAddiWP +
		MyLocale.getMsg(258, " Addi") +')';
	}


	public void moveCaches() {
		Profile dstProfile=new Profile();
		// Select destination directory
		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;

		int mode = showMessageBox(252, "All waypoints will be moved");
		if (mode == -1){
			return;
		}

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();
		}
		processCaches(new Mover(MyLocale.getMsg(142, "Move"),dstProfile), mode);
		// write indexfiles
		dstProfile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
		profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
	}

	 /**
	  * This function carries out the copy/delete/move with a progress bar.
	  * The Executor class defines what operation is to be carried out.
	  * mode defines if visible/marked or visible and markes caches are be processed.
	  * 0 means all visible
	  * 1 means all marked
	  * 2 means all visible and marked
	  * @param exec
	 * @param tickeOnly if set to <code>true</true> only caches with the checked-flag will be handled.
	  */
	 private void processCaches(Executor exec, int mode) {
		// First empty the cache so that the correct cache details are on disk
		CacheHolder.saveAllModifiedDetails();
		int size=srcDB.size();
		int count=0;
		// Count the number of caches to move/delete/copy
		// and remember the index of the files process, makes it a little bit easier
		boolean processSet[] = new boolean[size];
		for(int i = 0; i<size; i++) {
			switch (mode){
			case 0:
				if (srcDB.get(i).isVisible()){
					count++;
					processSet[i]=true;
				}
				break;
			case 1:
				if (srcDB.get(i).is_Checked){
					count++;
					processSet[i]=true;
				}
				break;
			case 2:
				if (srcDB.get(i).isVisible() && srcDB.get(i).is_Checked){
					count++;
					processSet[i]=true;
				}
				break;
			}
		}
		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();
		pbf.setTask(h,exec.title);
		pbf.exec();

		int nProcessed=0;
		// Now do the actual work
		for(int i = size-1; i>=0; i--){
			CacheHolder srcHolder=srcDB.get(i);
			if(processSet[i]){
				h.progress = ((float)nProcessed++)/(float)count;
				h.changed();
				//Now do the copy/delete/move of the cache
				exec.doIt(i,srcHolder);
			}
			if (pbf.isClosed) break;
		}
		pbf.exit(0);
	 }

	 class myProgressBarForm extends ProgressBarForm {
		 boolean isClosed=false;
		 protected boolean canExit(int exitCode) {
			isClosed=true;
			return true;
		 }
	 }

	//////////////////////////////////////////////////////////////////////
	// Utility functions
	//////////////////////////////////////////////////////////////////////

	private String selectTargetDir() {
		// Select destination directory
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.absoluteBaseDir);
		fc.setTitle(MyLocale.getMsg(148,"Select Target directory"));
		if(fc.execute() != FormBase.IDCANCEL){
			return fc.getChosen() + "/";
		}
		else return "";
	}

	public void deleteCacheFiles(String wpt,String dir, String[] tmp ){
		if (wpt.length() == 0){
			return;
		}

		// delete files in dstDir to clean up trash
		wpt=wpt.toLowerCase();
		for (int i=0; i < tmp.length;i++){
			if (tmp[i].toLowerCase().startsWith(wpt+'.') || tmp[i].toLowerCase().startsWith(wpt+'_')){
				File tmpFile = new File(dir + tmp[i]);
				tmpFile.delete();
			}
		}
	}

	private void moveCacheFiles(String wpt, String srcDir, String dstDir, String[] srcFiles){
		if (wpt.length() == 0){
			return;
		}
		wpt=wpt.toLowerCase();
		for (int i=0; i < srcFiles.length;i++){
			if (srcFiles[i].toLowerCase().startsWith(wpt+'.') || srcFiles[i].toLowerCase().startsWith(wpt+'_')){
				File srcFile = new File(srcDir + srcFiles[i]);
				File dstFile = new File(dstDir + srcFiles[i]);
				srcFile.move(dstFile);
			}
		}
	}

	private void copyCacheFiles(String wpt, String srcDir, String dstDir, String[] srcFiles){
		if (wpt.length() == 0){
			return;
		}
		wpt=wpt.toLowerCase();
		for (int i=0; i < srcFiles.length;i++){
			if (srcFiles[i].toLowerCase().startsWith(wpt+'.') || srcFiles[i].toLowerCase().startsWith(wpt+'_')){
				copy(srcDir + srcFiles[i],dstDir + srcFiles[i]);
			}
		}
	}

	/**
	 * copy a file
	 * @param sFileSrc source file name
	 * @param sFileDst destination file name
	 * @return true on success, false if an error occurred
	 */
	public static boolean copy( String sFileSrc, String sFileDst) {
		try {
			File   fSrc = new File( sFileSrc );
			int    len  = 32768;
			byte[] buff = new byte[ (int)java.lang.Math.min( len, fSrc.length() ) ];
			FileInputStream  fis = new FileInputStream(  fSrc );
			FileOutputStream fos = new FileOutputStream( sFileDst);
			while( 0 < (len = fis.read( buff )) )
				fos.write( buff, 0, len );
			fos.flush();
			fos.close();
			fis.close();
		}
		catch (Exception ex){
			Global.getPref().log("Filecopy failed: "+sFileSrc+"=>"+sFileDst,ex,true);
			return false;
		}
		return true;
	}

	//////////////////////////////////////////////////////////////////////
	// Executor
	//////////////////////////////////////////////////////////////////////

	private abstract class Executor {
		String title;
		Profile dstProfile;
		String[] destFileList;
		String[] srcFileList;
		public abstract void doIt(int i, CacheHolder srcHolder);
	}

	private class Deleter extends Executor {
		 Deleter(String title) {
			 this.title=title;
			 myProgressBarForm pbf = new myProgressBarForm();
			 Handle h = new Handle();
			 pbf.setTask(h,"Be patient. Reading Source Directory");
			 pbf.exec();
			 h.progress=(float) 0.5;
			 h.changed();
			 FileBugfix destPath=new FileBugfix(profile.dataDir);
			 destFileList= destPath.list(null,FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
			 pbf.exit(0);
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
			 srcDB.removeElementAt(i);
			 deleteCacheFiles(srcHolder.getWayPoint(),profile.dataDir, destFileList);
		 }
	}

	private class Copier extends Executor {
		 Copier(String title, Profile dstProfile) {
			 this.title=title;
			 this.dstProfile=dstProfile;
			 myProgressBarForm pbf = new myProgressBarForm();
			 Handle h = new Handle();
			 pbf.setTask(h,"Be patient. Reading Source Directory");
			 pbf.exec();
			 h.progress=(float) 0.33;
			 h.changed();
			 FileBugfix srcPath=new FileBugfix(profile.dataDir);
			 srcFileList= srcPath.list(null,FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
			 pbf.setTask(h,"Be patient. Reading Destination Directory");
			 h.progress=(float) 0.66;
			 h.changed();
			 FileBugfix destPath=new FileBugfix(dstProfile.dataDir);
			 destFileList= destPath.list(null,FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
			 pbf.exit(0);
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
			srcHolder.save();
			deleteCacheFiles(srcHolder.getWayPoint(),dstProfile.dataDir, destFileList);
			copyCacheFiles(srcHolder.getWayPoint(),profile.dataDir ,dstProfile.dataDir, srcFileList);
			// does cache exists in destDB ?
			// Update database
			//*wall* when copying addis without their maincache, the maincache in the srcDB will be set to null on saving the dstProfile later.
			//Therefore it will be shown twice in the cachelist.
			//To prevent this, addis will be cloned and to save memory only addis will be clones.
			//TODO clone addis only when the maincache will not be copied.
			//			if (srcHolder.isAddiWpt()){
			//			try {
			//				srcHolder = (CacheHolder) srcHolder.clone();
			//			} catch (CloneNotSupportedException e) {
			//				//ignore, CacheHolder implements Cloneable ensures this methods
			//			}
			//		}
			int dstPos = dstProfile.getCacheIndex(srcHolder.getWayPoint());
			if (dstPos >= 0){
				dstProfile.cacheDB.set(dstPos,srcHolder);
			}
			else {
				dstProfile.cacheDB.add(srcHolder);
			}
		}
	}

	private class Mover extends Executor {
		 Mover(String title, Profile dstProfile) {
			 this.title=title;
			 this.dstProfile=dstProfile;
			 myProgressBarForm pbf = new myProgressBarForm();
			 Handle h = new Handle();
			 pbf.setTask(h,"Be patient. Reading Source Directory");
			 pbf.exec();
			 h.progress=(float) 0.33;
			 h.changed();
			 FileBugfix srcPath=new FileBugfix(profile.dataDir);
			 srcFileList= srcPath.list(null,FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
			 pbf.setTask(h,"Be patient. Reading Destination Directory");
			 h.progress=(float) 0.66;
			 h.changed();
			 FileBugfix destPath=new FileBugfix(dstProfile.dataDir);
			 destFileList= destPath.list(null,FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
			 pbf.exit(0);
		 }
		 
		 public void doIt(int i,CacheHolder srcHolder) {
			 srcDB.removeElementAt(i);
			 deleteCacheFiles(srcHolder.getWayPoint(),dstProfile.dataDir, destFileList);
			 moveCacheFiles(srcHolder.getWayPoint(),profile.dataDir ,dstProfile.dataDir, srcFileList);
			// does cache exists in destDB ?
			 // Update database
			int dstPos = dstProfile.getCacheIndex(srcHolder.getWayPoint());
			if (dstPos >= 0){
				dstProfile.cacheDB.set(dstPos,srcHolder);
			}
			else {
				// Update database
				dstProfile.cacheDB.add(srcHolder);
			}
			i--;
		 }
	}
}
