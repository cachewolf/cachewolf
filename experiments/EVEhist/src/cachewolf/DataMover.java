package cachewolf;

import eve.ui.filechooser.FileChooser;
import java.io.*;
import eve.ui.*;
import java.util.Vector;
import eve.sys.*;


/**
*	This class moves or copies the database files of selected caches from one directory to
*   another. It provides also the possibility to delete cachefiles.
*/
public class DataMover {

	Vector srcDB;
	Preferences pref;
	Profile profile;

	public DataMover(){
		pref = Global.getPref();
		profile=Global.getProfile();
		srcDB = profile.cacheDB;
	}
	public void deleteCaches(){

		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(145,"Cachedata of ALL VISIBLE caches will be deleted! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}
		processCaches(new Deleter(MyLocale.getMsg(143, "Delete")));
		// write indexfiles
		profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
	}

	public void copyCaches(){
		Profile dstProfile=new Profile();

		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;
		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(146,"Cachedata of ALL VISIBLE caches will be copied! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();
		}
		processCaches(new Copier(MyLocale.getMsg(141, "Copy"),dstProfile));
		// write indexfiles and keep the filter status
		dstProfile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
	}

	public void moveCaches() {
		Profile dstProfile=new Profile();

		// Select destination directory
		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;

		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(147,"Cachedata of ALL VISIBLE caches will be moved! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();
		}
		processCaches(new Mover(MyLocale.getMsg(142, "Move"),dstProfile));
		// write indexfiles
		dstProfile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
		profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
	}

	 /**
	  * This function carries out the copy/delete/move with a progress bar.
	  * The Executor class defines what operation is to be carried out.
	  * @param exec
	  */
	 private void processCaches(Executor exec) {
		 // First empty the cache so that the correct cache details are on disk
		 CacheHolder.saveAllModifiedDetails();
		int size=srcDB.size();
		int count=0;
		// Count the number of caches to move/delete/copy
		for(int i = 0; i<size; i++) {
			if(((CacheHolder)srcDB.get(i)).is_filtered==false) count++;
		}
		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();
		pbf.setTask(h,exec.title);
		pbf.exec();

		int nProcessed=0;
		// Now do the actual work
		for(int i = size-1; i>=0; i--){
			CacheHolder srcHolder=(CacheHolder)srcDB.get(i);
			if(srcHolder.is_filtered==false){
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

	public String selectTargetDir() {
		// Select destination directory
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
		fc.title=MyLocale.getMsg(148,"Select Target directory");
		if(fc.execute() != FormBase.IDCANCEL){
			return fc.getChosen() + "/";
		}
		return "";
	}

	public void deleteCacheFiles(String wpt, String dir){
		// delete files in dstDir to clean up trash
		//TODO Remove FileBugfix
		String tmp[] = (new eve.io.File(dir)).list(wpt + "*.*", eve.io.File.LIST_FILES_ONLY);
		for (int i=0; i < tmp.length;i++){
			File tmpFile = new File(dir + tmp[i]);
			tmpFile.delete();
		}
	}

	public void moveCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new eve.io.File(srcDir).list(wpt + "*.*", eve.io.File.LIST_FILES_ONLY);
		for (int i=0; i < srcFiles.length;i++){
			eve.io.File srcFile = new eve.io.File(srcDir + srcFiles[i]);
			eve.io.File dstFile = new eve.io.File(dstDir + srcFiles[i]);
			srcFile.move(dstFile);
		}
	}

	public void copyCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new eve.io.File(srcDir).list(wpt + "*.*", eve.io.File.LIST_FILES_ONLY);
		for (int i=0; i < srcFiles.length;i++){
			copy(srcDir + srcFiles[i],dstDir + srcFiles[i]);
		}
	}

	public static void copy( String sFileSrc, String sFileDst)
	  {
	    try {
			java.io.File   fSrc = new java.io.File( sFileSrc );
		    int    len  = 32768;
		    byte[] buff = new byte[ (int)java.lang.Math.min( len, fSrc.length() ) ];
		    java.io.FileInputStream  fis = new java.io.FileInputStream(  fSrc );
		    java.io.FileOutputStream fos = new java.io.FileOutputStream( sFileDst);
		    while( 0 < (len = fis.read( buff )) )
		      fos.write( buff, 0, len );
		    fos.flush();
		    fos.close();
		    fis.close();
	    }
	    catch (Exception ex){
	    	Vm.debug("Filecopy failed: "+sFileSrc+"=>"+sFileDst);
	    }
	}

	//////////////////////////////////////////////////////////////////////
	// Executor
	//////////////////////////////////////////////////////////////////////

	private abstract class Executor {
		String title;
		Profile dstProfile;
		public void doIt(int i, CacheHolder srcHolder){}
	}

	private class Deleter extends Executor {
		 Deleter(String title) {
			 this.title=title;
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
			srcHolder.releaseCacheDetails();
			deleteCacheFiles(srcHolder.wayPoint,profile.dataDir);
			srcDB.removeElementAt(i);
		 }
	}

	private class Copier extends Executor {
		 Copier(String title, Profile dstProfile) {
			 this.title=title;
			 this.dstProfile=dstProfile;
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
			srcHolder.releaseCacheDetails();
			// does cache exists in destDB ?
			int dstPos = dstProfile.getCacheIndex(srcHolder.wayPoint);
			if (dstPos >= 0){
				deleteCacheFiles(srcHolder.wayPoint, dstProfile.dataDir);
				copyCacheFiles(srcHolder.wayPoint,profile.dataDir, dstProfile.dataDir);
				// Update database
				dstProfile.cacheDB.setElementAt(srcHolder,dstPos);
			}
			else {
				deleteCacheFiles(srcHolder.wayPoint, dstProfile.dataDir);
				copyCacheFiles(srcHolder.wayPoint,profile.dataDir, dstProfile.dataDir);
				// Update database
				dstProfile.cacheDB.add(srcHolder);
			}
		 }
	}

	private class Mover extends Executor {
		 Mover(String title, Profile dstProfile) {
			 this.title=title;
			 this.dstProfile=dstProfile;
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
			// does cache exists in destDB ?
			srcHolder.releaseCacheDetails();
			int dstPos = dstProfile.getCacheIndex(srcHolder.wayPoint);
			if (dstPos >= 0){
				deleteCacheFiles(srcHolder.wayPoint, dstProfile.dataDir);
				moveCacheFiles(srcHolder.wayPoint,profile.dataDir, dstProfile.dataDir);
				// Update database
				dstProfile.cacheDB.setElementAt(srcHolder,dstPos);
				srcDB.removeElementAt(i);
				i--;
			}
			else {
				deleteCacheFiles(srcHolder.wayPoint, dstProfile.dataDir);
				moveCacheFiles(srcHolder.wayPoint,profile.dataDir, dstProfile.dataDir);
				// Update database
				dstProfile.cacheDB.add(srcHolder);
				srcDB.removeElementAt(i);
				i--;
			}
		 }
	}
}
