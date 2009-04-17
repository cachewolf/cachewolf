package CacheWolf;

import utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.*;
import ewe.ui.*;
import ewe.util.Vector;
import ewe.sys.*;


/**
*	This class moves or copies the database files of selected caches from one directory to
*   another. It provides also the possibility to delete cachefiles. 	
*/
public class DataMover {

	CacheDB srcDB, dstDB;
	Preferences pref;
	Profile profile;
	
	public DataMover(){
		pref = Global.getPref();
		profile=Global.getProfile();
		srcDB = profile.cacheDB;
	}
	public void deleteCaches(){
		
		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(145,"Cachedata of ALL VISIBLE caches will be deleted! Continue?"), FormBase.IDYES |FormBase.IDNO);
		if (mBox.execute() != FormBase.IDOK){
			return;
		}
		processCaches(new Deleter(MyLocale.getMsg(143, "Delete")));
		// write indexfiles
		profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
	}

	public void copyCaches(){
		Profile dstProfile=new Profile();
		
		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;
		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(146,"Cachedata of ALL VISIBLE caches will be copied! Continue?"), FormBase.IDYES |FormBase.IDNO);
		if (mBox.execute() != FormBase.IDOK){
			return;
		}
		
		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();
		}
		processCaches(new Copier(MyLocale.getMsg(141, "Copy"),dstProfile));
		// write indexfiles and keep the filter status
		dstProfile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
	}
	
	public void moveCaches() {
		Profile dstProfile=new Profile();
		// Select destination directory
		dstProfile.dataDir=selectTargetDir();
		if (dstProfile.dataDir.equals(profile.dataDir) ||
			dstProfile.dataDir.equals("")) return;
		
		MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(147,"Cachedata of ALL VISIBLE caches will be moved! Continue?"), FormBase.IDYES |FormBase.IDNO);
		if (mBox.execute() != FormBase.IDOK){
			return;
		}
		
		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.dataDir + "index.xml");
		if(ftest.exists()){
			dstProfile.readIndex();		
		}
		processCaches(new Mover(MyLocale.getMsg(142, "Move"),dstProfile));
		// write indexfiles
		dstProfile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR); 
		profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR);
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
			if(srcDB.get(i).is_filtered()==false) count++;
		}
		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();
		pbf.setTask(h,exec.title);
		pbf.exec();
		
		int nProcessed=0;
		// Now do the actual work
		for(int i = size-1; i>=0; i--){
			CacheHolder srcHolder=srcDB.get(i);
			if(srcHolder.is_filtered()==false){
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
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.baseDir);
		fc.setTitle(MyLocale.getMsg(148,"Select Target directory"));
		if(fc.execute() != FormBase.IDCANCEL){
			return fc.getChosen() + "/";
		}
		else return "";
	}
	 
	public void deleteCacheFiles(String wpt, String dir){
		// delete files in dstDir to clean up trash
		String tmp[] = new FileBugfix(dir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i=0; i < tmp.length;i++){
			File tmpFile = new File(dir + tmp[i]);
			tmpFile.delete();
		}
	}

	public void moveCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new FileBugfix(srcDir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i=0; i < srcFiles.length;i++){
			File srcFile = new File(srcDir + srcFiles[i]);
			File dstFile = new File(dstDir + srcFiles[i]);
			srcFile.move(dstFile);
		}
	}

	public void copyCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new FileBugfix(srcDir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i=0; i < srcFiles.length;i++){
			copy(srcDir + srcFiles[i],dstDir + srcFiles[i]);
		}
	}

	public static void copy( String sFileSrc, String sFileDst)
	  {
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
			 srcDB.removeElementAt(i);
			 deleteCacheFiles(srcHolder.getWayPoint(),profile.dataDir);
		 }
	}
	 
	private class Copier extends Executor {
		 Copier(String title, Profile dstProfile) {
			 this.title=title;
			 this.dstProfile=dstProfile;
		 }
		 public void doIt(int i,CacheHolder srcHolder) {
				srcHolder.save();
				deleteCacheFiles(srcHolder.getWayPoint(), dstProfile.dataDir);
				copyCacheFiles(srcHolder.getWayPoint(),profile.dataDir, dstProfile.dataDir);
				// does cache exists in destDB ?
				// Update database
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
			 }
			 public void doIt(int i,CacheHolder srcHolder) {
				 srcDB.removeElementAt(i);
				 deleteCacheFiles(srcHolder.getWayPoint(), dstProfile.dataDir);
				 moveCacheFiles(srcHolder.getWayPoint(),profile.dataDir, dstProfile.dataDir);
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
