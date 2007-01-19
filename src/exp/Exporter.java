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

/**
 * @author Kalle
 * Base class for exporter, handles basic things like selecting
 * outputfile, display a counter etc.
 * 
 */
public class Exporter {
	final static int TMP_FILE = 0;
	final static int ASK_FILE = 1;
	
	final static int NO_PARAMS 	= 0;
	final static int LAT_LON 	= 1;
	final static int COUNT 		= 2;
	
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	String mask = "*.*";
	String tmpFileName;
	char decimalSeparator='.';
	boolean needCacheDetails = false;
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

	
	public void doIt(int variant){
		File outFile;
		String str;
		CacheHolder holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		CWPoint coords = new CWPoint();


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
					if (needCacheDetails) holder.readCache(profile.dataDir);
					switch (this.howManyParams) {
					case NO_PARAMS: 
						str = record(holder);
						break;
					case LAT_LON:	
						coords.set(holder.LatLon, CWPoint.CW);
						str = record(holder, coords.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
						             		 coords.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator));
						break;
					case LAT_LON|COUNT: 
						coords.set(holder.LatLon, CWPoint.CW);
						str = record(holder, coords.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
											 coords.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator),
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
		}catch (Exception e){
			Vm.debug("Problem writing to file! "+e.toString());
		}//try
	}
	
	public void setMask(String mask){
		this.mask = mask;
	}
	
	public void setDecimalSeparator (char sep){
		this.decimalSeparator = sep;
	}
	
	public void setNeedCacheDetails(boolean how){
		this.needCacheDetails = how;
	}
	
	public void setHowManyParams(int paramBits){
		this.howManyParams = paramBits;
	}
	
	public void setTmpFileName(String fName){
		this.tmpFileName = fName;
	}

	
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
		
	public String header () {
		return null;
	}
	
	public String record(CacheHolder ch){
		return null;
	}

	public String record(CacheHolder ch, String lat, String lon){
		return null;
	}
	
	public String record(CacheHolder ch, String lat, String lon, int count){
		return null;
	}
	
	public String trailer(){
		return null;
	}
	
	public String trailer(int total){
		return null;
	}


}
