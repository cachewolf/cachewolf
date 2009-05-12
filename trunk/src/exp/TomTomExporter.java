package exp;


import CacheWolf.*;
import ewe.ui.*;
import ewe.util.*;
import ewe.util.zip.*;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.*;
import ewe.sys.*;

public class TomTomExporter {
	public final static int TT_ASC = 0;
	public final static int TT_OV2 = 1;
	public final static int TT_WPT_TEXT = 0;
	public final static int TT_WPT_NUM = 1;
	public final static String expName = "TomTom";

	CacheDB cacheDB;
	Preferences pref;
	Profile profile;

	

	
	public TomTomExporter() {
		profile = Global.getProfile();
		pref = Global.getPref();
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
		String fileName, dirName, prefix;
		int fileFormat;

		TomTomExporterScreen infoScreen = new TomTomExporterScreen("TomTomExport");
		if (infoScreen.execute() == FormBase.IDCANCEL) return;
		fileFormat = infoScreen.getFormat();

		dirName = pref.getExportPath(expName);
		
		if (infoScreen.oneFilePerType()==true){
			FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, dirName);
			fc.setTitle("Select target dir:");
			if(fc.execute() == FormBase.IDCANCEL) return;
			dirName = fc.getChosen();
			pref.setExportPath(expName, dirName);
			prefix = infoScreen.getPrefix();
			writeOneFilePerType(fileFormat, dirName, prefix);
		} else{
			FileChooser fc = new FileChooser(FileChooserBase.SAVE, dirName);
			fc.setTitle("Select target file:");
	
			if (fileFormat == TT_ASC) fc.addMask("*.asc");
			else fc.addMask("*.ov2");
			
			if(fc.execute() == FormBase.IDCANCEL) return;
			fileName = fc.getChosen();
			pref.setExportPathFromFileName(expName, fileName);
			writeSingleFile(fileFormat, fileName);
		}
	}
	
	public void writeOneFilePerType(int format, String dirName, String prefix){
		RandomAccessFile out = null;
		File dfile;
		String ext, fileName = null;

		CacheHolder holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int currExp, counter;
		
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();
		
		currExp = 0;
		counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			if(cacheDB.get(i).isVisible()) counter++;
		}
		
		ext = format==TT_ASC?".asc":".ov2";
		try{
			//loop through type
			for(int j = 0; j < CacheType.wayType.length; j++){
				fileName = dirName + "/" + prefix + CacheType.wayType[j][TT_WPT_TEXT]+ ext;
				dfile = new File(fileName);
				dfile.delete();
				out =  new RandomAccessFile(fileName,"rw");
				for(int i = 0; i<cacheDB.size(); i++){
					holder=cacheDB.get(i);
					if(holder.getType() == new Integer(CacheType.wayType[j][TT_WPT_NUM]).intValue() && holder.isVisible() == false){
						currExp++;
						h.progress = (float)currExp/(float)counter;
						h.changed();
						if (holder.pos.isValid() == false) continue;
						if (format == TT_ASC){
							writeRecordASCII(out, holder,holder.pos.getLatDeg(CWPoint.DD),holder.pos.getLonDeg(CWPoint.DD));
						} else {
							writeRecordBinary(out, holder,holder.pos.getLatDeg(CWPoint.DD),holder.pos.getLonDeg(CWPoint.DD));
						}
					}//if
				}//for cacheDB
				out.close();
				// check for empty files and delete them
				dfile = new File(fileName);
				if (dfile.length()==0) {
					dfile.delete();
				} else {
					copyIcon(j, dirName + "/" + prefix + CacheType.wayType[j][TT_WPT_TEXT]); 
				}
			}//for wayType
			pbf.exit(0);
		} catch (IOException e){
			Vm.debug("Problem creating file! " + fileName);
			e.printStackTrace();
		}//try
		
	}
	
	public void writeSingleFile(int format, String fileName){
		RandomAccessFile out = null;
		File dfile;

		CacheHolder holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		int counter = 0;
		int expCount = 0;
		for(int i = 0; i<cacheDB.size();i++){
			if(cacheDB.get(i).isVisible()) counter++;
		}

		try{
			dfile = new File(fileName);
			dfile.delete();
			out =  new RandomAccessFile(fileName,"rw");
			for(int i = 0; i<cacheDB.size(); i++){
				holder=cacheDB.get(i);
				if(holder.isVisible()){
					expCount++;
					h.progress = (float)expCount/(float)counter;
					h.changed();
					if (holder.pos.isValid() == false) continue;
					if (format == TT_ASC){
						writeRecordASCII(out, holder,holder.pos.getLatDeg(CWPoint.DD),holder.pos.getLonDeg(CWPoint.DD));
					} else {
						writeRecordBinary(out, holder,holder.pos.getLatDeg(CWPoint.DD),holder.pos.getLonDeg(CWPoint.DD));
					}
				}//if
			}//for
			out.close();
			copyIcon(0, fileName.substring(0,fileName.indexOf(".")));
			pbf.exit(0);
		}catch (Exception e){
			Vm.debug("Problem writing to file! " + fileName);
			e.printStackTrace();
		}//try
	}
	
	
	public void writeRecordASCII(RandomAccessFile outp, CacheHolder ch, String lat, String lon){
		try {
			outp.writeBytes(lon);
			outp.writeBytes(",");
			outp.writeBytes(lat);
			outp.writeBytes(",");
			//outp.writeBytes("\"" + ch.CacheName.replace(',',' ') + "\"\r\n");
			outp.writeBytes("\"");
			outp.writeBytes(ch.getWayPoint());
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheName().replace(',',' '));
			outp.writeBytes(" by ");
			outp.writeBytes(ch.getCacheOwner());
			outp.writeBytes("- ");             
			outp.writeBytes(ch.getHard());
			outp.writeBytes("/");
			outp.writeBytes(ch.getTerrain());
			outp.writeBytes(" - ");
			outp.writeBytes(CacheSize.cw2ExportString(ch.getCacheSize()));
			outp.writeBytes("\"\r\n");
		} catch (IOException e) {
			Vm.debug("Error writing to file");
			e.printStackTrace();
		}
		return;
	}

	
	public void writeRecordBinary(RandomAccessFile outp, CacheHolder ch, String lat, String lon){
		int d,data;
		double latlon;
		
		try {
			d = 2;
			outp.writeByte((byte)d);
			data = ch.getWayPoint().length()+ch.getCacheName().length()+ch.getCacheOwner().length()+ch.getHard().length()+ch.getTerrain().length()+CacheSize.cw2ExportString(ch.getCacheSize()).length()+27;
			writeIntBinary(outp, data);
			latlon = Common.parseDouble(lon);
			latlon *=100000;
			writeIntBinary(outp, (int) latlon);
			latlon = Common.parseDouble(lat);;
			latlon *=100000;
			writeIntBinary(outp, (int) latlon);
			outp.writeBytes(ch.getWayPoint());
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheName());
			outp.writeBytes(" by ");
			outp.writeBytes(ch.getCacheOwner());
			//Wenn Leerzeichen am Ende von Cache.Owner entfernt: 
			//Hier wieder einf�gen
			//und data = holder.wayPoint.length()+holder.CacheName.length()+.....
			//wider um 1 erh�hen
			outp.writeBytes("- ");             
			outp.writeBytes(ch.getHard());
			outp.writeBytes("/");
			outp.writeBytes(ch.getTerrain());
			outp.writeBytes(" - ");
			outp.writeBytes(CacheSize.cw2ExportString(ch.getCacheSize()));
			d = 0;
			outp.writeByte((byte)d);
		} catch (IOException e) {
			Vm.debug("Error writing to file");
			e.printStackTrace();
		}

		return;
	}

	public void writeIntBinary(RandomAccessFile outp, int data){
		
		ByteArray buf = new ByteArray();
		buf.appendInt(data);
		try {
			outp.writeByte(buf.data[3]);
			outp.writeByte(buf.data[2]);
			outp.writeByte(buf.data[1]);
			outp.writeByte(buf.data[0]);
		} catch (IOException e) {
			Vm.debug("Error writing to file");
			e.printStackTrace();
		}

		return;
	}
	
	public void copyIcon(int intWayType, String filename){
		try {
			ZipFile zif = new ZipFile (FileBase.getProgramDirectory() + "/POIIcons.zip");
			ZipEntry zipEnt;
			int len;
			String entName; 
			
			entName = "TomTomIcons/"+ "GC-" + CacheType.wayType[intWayType][TT_WPT_TEXT] + ".bmp";
			zipEnt = zif.getEntry(entName);
			if (zipEnt == null) return;
			
		    byte[] buff = new byte[ zipEnt.getSize() ];
		    InputStream  fis = zif.getInputStream(zipEnt);
		    FileOutputStream fos = new FileOutputStream( filename + ".bmp");
		    while( 0 < (len = fis.read( buff )) )
		      fos.write( buff, 0, len );
		    fos.flush();
		    fos.close();
		    fis.close();
		} catch (ZipException e) {
			Vm.debug("Problem copying Icon" + "GC-" + CacheType.wayType[intWayType][TT_WPT_TEXT] + ".bmp" );
			e.printStackTrace();
		} catch (IOException e) {
			Vm.debug("Problem copying Icon" + "GC-" + CacheType.wayType[intWayType][TT_WPT_TEXT] + ".bmp" );
			e.printStackTrace();
		}

		
	}
	
}
