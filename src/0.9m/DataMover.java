package CacheWolf;

import ewe.filechooser.FileChooser;
import ewe.io.*;
import ewe.ui.*;
import ewe.util.Vector;
import ewe.sys.*;


/**
*	This class moves or copies the database files of selected caches from one directory to
*   another. It provides also the possibility to delete cachefiles. 	
*/
public class DataMover {

	Vector srcDB, dstDB;
	Preferences srcPreferences;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public DataMover(Vector db, Preferences pref){
		srcDB = db;
		dstDB = new Vector();
		srcPreferences = pref;
	}
	public void deleteCaches(){
		
		MessageBox mBox = new MessageBox ((String)lr.get(144,"Warning"),(String)lr.get(145,"Cachedata will be deleted! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}

		// Loop through database
		for(int i = 0; i<srcDB.size(); i++){
			CacheHolder srcHolder=(CacheHolder)srcDB.get(i);
			if(srcHolder.is_black == false && srcHolder.is_filtered == false){
				deleteCacheFiles(srcHolder.wayPoint, srcPreferences.mydatadir);
				srcDB.removeElementAt(i);
				i--;
			}//if srcHolder...
		}//for ... i < srcDB ...
		// write indexfiles
		CacheReaderWriter crw = new CacheReaderWriter();
		crw.saveIndex(srcDB, srcPreferences.mydatadir);
	}

	public void copyCaches(){
		String dstDir;
		int dstPos;

		
		// Select destination directory
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, srcPreferences.mydatadir);
		fc.setTitle((String)lr.get(148,"Select Targetdirectory"));
		if(fc.execute() != fc.IDCANCEL){
			dstDir = fc.getChosen() + "/";
		}
		else return;
		MessageBox mBox = new MessageBox ((String)lr.get(144,"Warning"),(String)lr.get(146,"Cachedata will be copied! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}
		
		// Read indexfile of destination, if one exists
		File ftest = new File(dstDir + "index.xml");
		if(ftest.exists()){
			MyXMLBuilder mb = new MyXMLBuilder(dstDB, dstDir);
			mb.doIt();
		}
		// Loop through database
		for(int i = 0; i<srcDB.size(); i++){
			CacheHolder srcHolder=(CacheHolder)srcDB.get(i);
			if(srcHolder.is_black == false && srcHolder.is_filtered == false){
				// does cache exists in destDB ?
				dstPos = searchWpt(dstDB, srcHolder.wayPoint);
				if (dstPos >= 0){
					deleteCacheFiles(srcHolder.wayPoint, dstDir);
					copyCacheFiles(srcHolder.wayPoint,srcPreferences.mydatadir, dstDir);
					// Update database
					dstDB.set(dstPos,srcHolder);
				}
				else {
					deleteCacheFiles(srcHolder.wayPoint, dstDir);
					copyCacheFiles(srcHolder.wayPoint,srcPreferences.mydatadir, dstDir);
					// Update database
					dstDB.add(srcHolder);
				}
			}//if srcHolder...
		}//for ... i < srcDB ...
		// write indexfiles
		CacheReaderWriter crw = new CacheReaderWriter();
		crw.saveIndex(dstDB, dstDir);
	}
	
	public void moveCaches() {
		String dstDir;
		int dstPos;
		
		// Select destination directory
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, srcPreferences.mydatadir);
		fc.setTitle((String)lr.get(148,"Select Targetdirectory"));
		if(fc.execute() != fc.IDCANCEL){
			dstDir = fc.getChosen() + "/";
		}
		else return;
		MessageBox mBox = new MessageBox ((String)lr.get(144,"Warning"),(String)lr.get(147,"Cachedata will be moved! Continue?"), MessageBox.IDYES |MessageBox.IDNO);
		if (mBox.execute() != MessageBox.IDOK){
			return;
		}
		
		// Read indexfile of destination, if one exists
		File ftest = new File(dstDir + "index.xml");
		if(ftest.exists()){
			MyXMLBuilder mb = new MyXMLBuilder(dstDB, dstDir);
			mb.doIt();
		}
		// Loop through database
		for(int i = 0; i<srcDB.size(); i++){
			CacheHolder srcHolder=(CacheHolder)srcDB.get(i);
			if(srcHolder.is_black == false && srcHolder.is_filtered == false){
				// does cache exists in destDB ?
				dstPos = searchWpt(dstDB, srcHolder.wayPoint);
				if (dstPos >= 0){
					deleteCacheFiles(srcHolder.wayPoint, dstDir);
					moveCacheFiles(srcHolder.wayPoint,srcPreferences.mydatadir, dstDir);
					// Update database
					dstDB.set(dstPos,srcHolder);
					srcDB.removeElementAt(i);
					i--;
				}
				else {
					deleteCacheFiles(srcHolder.wayPoint, dstDir);
					moveCacheFiles(srcHolder.wayPoint,srcPreferences.mydatadir, dstDir);
					// Update database
					dstDB.add(srcHolder);
					srcDB.removeElementAt(i);
					i--;
				}
			}//if srcHolder...
		}//for ... i < srcDB ...
		// write indexfiles
		CacheReaderWriter crw = new CacheReaderWriter();
		crw.saveIndex(dstDB, dstDir);
		crw.saveIndex(srcDB, srcPreferences.mydatadir);
	}
	/**
	* Method to iterate through cache database and look for waypoint.
	* Returns true if waypoint is found, else false
	*/
	private int searchWpt(Vector db, String wpt){
		if(wpt.length()>0){
			wpt = wpt.toUpperCase();
			CacheHolder ch = new CacheHolder();
			//Search through complete database
			for(int i = 0;i < db.size();i++){
				ch = (CacheHolder)db.get(i);
				if(ch.wayPoint.indexOf(wpt) >=0 ){
					return i;
				}
			} // for
		} // if
		return -1;
	}

	public void deleteCacheFiles(String wpt, String dir){
		// delete files in dstDir to clean up trash
		String tmp[] = new File(dir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i=0; i < tmp.length;i++){
			File tmpFile = new File(dir + tmp[i]);
			tmpFile.delete();
		}
	}

	public void moveCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new File(srcDir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
		for (int i=0; i < srcFiles.length;i++){
			File srcFile = new File(srcDir + srcFiles[i]);
			File dstFile = new File(dstDir + srcFiles[i]);
			srcFile.move(dstFile);
		}
	}

	public void copyCacheFiles(String wpt, String srcDir, String dstDir){
		String srcFiles[] = new File(srcDir).list(wpt + "*.*", ewe.io.FileBase.LIST_FILES_ONLY);
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
	    	Vm.debug("Filecopy failed");
	    }
	  }

	 
}
