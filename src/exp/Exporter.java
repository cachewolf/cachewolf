package exp;

import CacheWolf.*;
import CacheWolf.CWPoint;
import ewe.sys.*;
import ewe.filechooser.FileChooser;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileWriter;
import ewe.io.PrintWriter;
import ewe.ui.ProgressBarForm;
import ewe.util.*;
import ewe.io.IOException;

/**
 * @author Kalle
 * Base class for exporter, handles basic things like selecting
 * outputfile, display a counter etc.
 * A new Exporter must only override the header(), record() and 
 * trailer() methods. The member howManyParams must be set to identify
 * which ethod should be called  
 */

public class Exporter {
	// starts with no ui for file selection
	final static int TMP_FILE = 0;
	// brings up a screen to select a file
	final static int ASK_FILE = 1;
	
	// selection, which method should be called
	final static int NO_PARAMS 	= 0;
	final static int LAT_LON 	= 1;
	final static int COUNT 		= 2;
	
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	// mask in file chooser
	String mask = "*.*";
	// file name, if no file chooser is used
	String tmpFileName;
	// decimal separator for lat- and lon-String
	char decimalSeparator='.';
	// if  true, the complete cache details are read
	// before a call to the record method is made 
	boolean needCacheDetails = false;
	// selection, which method should be called
	int howManyParams = 0;
	
	public Exporter() {
		profile = Global.getProfile();
		pref = Global.getPref();
		cacheDB = profile.cacheDB;
		howManyParams = LAT_LON;
	}
	
	public void doIt(){
		this.doIt(ASK_FILE);
	}

	/**
	 * Does the most work for exporting data
	 * @param variant 0, if no filechooser
	 *                1, if filechooser
	 */
	public void doIt(int variant){
		File outFile;
		String str;
		CacheHolder holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();


		if (variant == ASK_FILE) {
			outFile = getOutputFile();
			if (outFile == null) return;
		} else {
			outFile = new File(tmpFileName);
		}

		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		int counter = 0;
		int expCount = 0;
		for(int i = 0; i<cacheDB.size();i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false && holder.is_filtered == false) counter++;
		}

		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			str = this.header();
			if (str != null) outp.print(str);
			for(int i = 0; i<cacheDB.size(); i++){
				holder=(CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false){
					expCount++;
					h.progress = (float)expCount/(float)counter;
					h.changed();
					try {
						if (needCacheDetails) {
							holder.readCache(profile.dataDir);
						}
					} catch (IOException e) {
						continue;
					}
					switch (this.howManyParams) {
					case NO_PARAMS: 
						str = record(holder);
						break;
					case LAT_LON:
						if (holder.pos.isValid() == false) continue;
						str = record(holder, holder.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
								     holder.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator));
						break;
					case LAT_LON|COUNT: 
						if (holder.pos.isValid() == false) continue;
						str = record(holder, holder.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
									 holder.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator),
											 i);
						break;
					default:
						str = null;
						break;
					}
					if (str != null) outp.print(str);
				}//if
			}//for
			switch (this.howManyParams & COUNT) {
			case NO_PARAMS: 
				str = trailer();
				break;
			case COUNT:
				str = trailer(counter);
				break;
			default:
				str = null;
				break;
			}
			if (str != null) outp.print(str);
			outp.close();
			pbf.exit(0);
		} catch (IOException ioE){
			Vm.debug("Error opening " + outFile.getName());
		}
		//try
	}
	/**
	 * sets mask for filechooser
	 * @param mask 
	 */
	public void setMask(String mask){
		this.mask = mask;
	}
	/**
	 * sets decimal separator for lat/lon-string
	 * @param sep
	 */
	public void setDecimalSeparator (char sep){
		this.decimalSeparator = sep;
	}
	/**
	 *  sets needCacheDetails
	 * @param how
	 */
	public void setNeedCacheDetails(boolean how){
		this.needCacheDetails = how;
	}
	
	/**
	 * sets howManyParams
	 * @param paramBits
	 */
	public void setHowManyParams(int paramBits){
		this.howManyParams = paramBits;
	}
	/**
	 * sets tmpFileName
	 * @param fName
	 */
	public void setTmpFileName(String fName){
		this.tmpFileName = fName;
	}

	/**
	 * uses a filechooser to get the name of the export file
	 * @return
	 */
	public File getOutputFile (){
		FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
		fc.setTitle("Select target file:");
		fc.addMask(mask);
		if(fc.execute() != FileChooser.IDCANCEL){
			return  fc.getChosenFile();
		} else {
			return null;
		}
	}
	/**
	 * this method can be overided by an exporter class
	 * @return formated header data
	 */	
	public String header () {
		return null;
	}

	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @return formated cache data
	 */	
	public String record(CacheHolder ch){
		return null;
	}

	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @param lat	
	 * @param lon
	 * @return formated cache data
	 */
	public String record(CacheHolder ch, String lat, String lon){
		return null;
	}
	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @param lat	
	 * @param lon
	 * @param count of actual record
	 * @return formated cache data
	 */
	public String record(CacheHolder ch, String lat, String lon, int count){
		return null;
	}
	
	/**
	 * this method can be overided by an exporter class
	 * @return formated trailer data
	 */	
	public String trailer(){
		return null;
	}
	/**
	 * this method can be overided by an exporter class
	 * @param total count of exported caches
	 * @return
	 */
	public String trailer(int total){
		return null;
	}


}
