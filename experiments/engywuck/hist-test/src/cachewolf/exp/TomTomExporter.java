package cachewolf.exp;


import eve.ui.*;
import java.util.*;
import java.util.zip.*;
import eve.ui.filechooser.FileChooser;
import eve.sys.*;
import java.io.*;

import cachewolf.*;
import cachewolf.utils.Common;

import eve.util.ByteArray;


public class TomTomExporter {
	public final static int TT_ASC = 0;
	public final static int TT_OV2 = 1;
	public final static int TT_WPT_TEXT = 0;
	public final static int TT_WPT_NUM = 1;
	public final static String expName = "TomTom";

	Vector cacheDB;
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
		if (infoScreen.execute() == Form.IDCANCEL) return;
		fileFormat = infoScreen.getFormat();

		dirName = pref.getExportPath(expName);
		
		if (infoScreen.oneFilePerType()==true){
			FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, dirName);
			fc.title=("Select target dir:");
			if(fc.execute() == FileChooser.IDCANCEL) return;
			dirName = fc.getChosen();
			pref.setExportPath(expName, dirName);
			prefix = infoScreen.getPrefix();
			writeOneFilePerType(fileFormat, dirName, prefix);
		} else{
			FileChooser fc = new FileChooser(FileChooser.SAVE, dirName);
			fc.title=("Select target file:");
	
			if (fileFormat == TT_ASC) fc.addMask("*.asc");
			else fc.addMask("*.ov2");
			
			if(fc.execute() == FileChooser.IDCANCEL) return;
			fileName = fc.getChosen();
			pref.setExportPathFromFileName(expName, fileName);
			writeSingleFile(fileFormat, fileName);
		}
	}
	
	public void writeOneFilePerType(int format, String dirName, String prefix){
		RandomAccessFile out = null;
		java.io.File dfile;
		String ext, fileName = null;

		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int currExp, counter;
		
		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();
		
		currExp = 0;
		counter = 0;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_black == false && ch.is_filtered == false) counter++;
		}
		
		ext = format==TT_ASC?".asc":".ov2";
		try{
			//loop through type
			for(int j = 0; j < CacheType.wayType.length; j++){
				fileName = dirName + "/" + prefix + CacheType.wayType[j]+ ext;
				dfile = new java.io.File(fileName);
				dfile.delete();
				out =  new RandomAccessFile(fileName,"rw");
				for(int i = 0; i<cacheDB.size(); i++){
					ch=(CacheHolder)cacheDB.get(i);
					if(ch.type==CacheType.wayTypeNo[j] && ch.is_black == false && ch.is_filtered == false){
						currExp++;
						h.progress = (float)currExp/(float)counter;
						h.changed();
						if (ch.pos.isValid() == false) continue;
						if (format == TT_ASC){
							writeRecordASCII(out, ch,ch.pos.getLatDeg(CWPoint.DD),ch.pos.getLonDeg(CWPoint.DD));
						} else {
							writeRecordBinary(out, ch,ch.pos.getLatDeg(CWPoint.DD),ch.pos.getLonDeg(CWPoint.DD));
						}
					}//if
				}//for cacheDB
				out.close();
				// check for empty files and delete them
				dfile = new java.io.File(fileName);
				if (dfile.length()==0) {
					dfile.delete();
				} else {
					copyIcon(j, dirName + "/" + prefix + CacheType.wayType[j]); 
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
		java.io.File dfile;

		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		int counter = 0;
		int expCount = 0;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_black == false && ch.is_filtered == false) counter++;
		}

		try{
			dfile = new java.io.File(fileName);
			dfile.delete();
			out =  new RandomAccessFile(fileName,"rw");
			for(int i = 0; i<cacheDB.size(); i++){
				ch=(CacheHolder)cacheDB.get(i);
				if(ch.is_black == false && ch.is_filtered == false){
					expCount++;
					h.progress = (float)expCount/(float)counter;
					h.changed();
					if (ch.pos.isValid() == false) continue;
					if (format == TT_ASC){
						writeRecordASCII(out, ch,ch.pos.getLatDeg(CWPoint.DD),ch.pos.getLonDeg(CWPoint.DD));
					} else {
						writeRecordBinary(out, ch,ch.pos.getLatDeg(CWPoint.DD),ch.pos.getLonDeg(CWPoint.DD));
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
			outp.writeBytes(ch.wayPoint);
			outp.writeBytes(" - ");
			outp.writeBytes(ch.cacheName.replace(',',' '));
			outp.writeBytes(" by ");
			outp.writeBytes(ch.cacheOwner);
			outp.writeBytes("- ");             
			outp.writeBytes(ch.hard);
			outp.writeBytes("/");
			outp.writeBytes(ch.terrain);
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheSize());
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
			data = ch.wayPoint.length()+ch.cacheName.length()+ch.cacheOwner.length()+ch.hard.length()+ch.terrain.length()+ch.getCacheSize().length()+27;
			writeIntBinary(outp, data);
			latlon = Common.parseDouble(lon);
			latlon *=100000;
			writeIntBinary(outp, (int) latlon);
			latlon = Common.parseDouble(lat);;
			latlon *=100000;
			writeIntBinary(outp, (int) latlon);
			outp.writeBytes(ch.wayPoint);
			outp.writeBytes(" - ");
			outp.writeBytes(ch.cacheName);
			outp.writeBytes(" by ");
			outp.writeBytes(ch.cacheOwner);
			//Wenn Leerzeichen am Ende von Cache.Owner entfernt: 
			//Hier wieder einfügen
			//und data = holder.wayPoint.length()+holder.CacheName.length()+.....
			//wider um 1 erhöhen
			outp.writeBytes("- ");             
			outp.writeBytes(ch.hard);
			outp.writeBytes("/");
			outp.writeBytes(ch.terrain);
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheSize());
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
			ZipFile zif = new ZipFile (eve.io.File.getProgramDirectory() + "/POIIcons.zip");
			ZipEntry zipEnt;
			int len;
			String entName; 
			
			entName = "TomTomIcons/"+ "GC-" + CacheType.wayType[intWayType] + ".bmp";
			zipEnt = zif.getEntry(entName);
			if (zipEnt == null) return;
			
		    byte[] buff = new byte[ (int) zipEnt.getSize() ];
		    InputStream  fis = zif.getInputStream(zipEnt);
		    FileOutputStream fos = new FileOutputStream( filename + ".bmp");
		    while( 0 < (len = fis.read( buff )) )
		      fos.write( buff, 0, len );
		    fos.flush();
		    fos.close();
		    fis.close();
		} catch (ZipException e) {
			Vm.debug("Problem copying Icon" + "GC-" + CacheType.wayType[intWayType] + ".bmp" );
			e.printStackTrace();
		} catch (IOException e) {
			Vm.debug("Problem copying Icon" + "GC-" + CacheType.wayType[intWayType] + ".bmp" );
			e.printStackTrace();
		}

		
	}
	
}
