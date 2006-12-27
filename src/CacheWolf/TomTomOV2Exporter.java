package CacheWolf;
import ewe.ui.mChoice;
import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;
import ewe.sys.*;

/**
*
*	Class to export the cache database (index) to an TomTom OV2 File
*
**/
public class TomTomOV2Exporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	
	public TomTomOV2Exporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
		CacheHolder holder;
		ParseLatLon pll;
		int symCounter = 0;
		double latlon;
		int a,b,c,d,data;
		RandomAccessFile outp;
		File dfile; 
		String[] wayType = {"Custom", "Traditional", "Multi", "Virtual", "Letterbox", "Event", "Mega Event", "Mystery", "Webcam", "Locationless", "CITO", "Earthcache", "Parking", "Stage", "Question", "Final","Trailhead","Reference"};
		String ctype = "";
		//need directory only!!!!
		String dummy = new String();
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, profile.dataDir);
		fc.setTitle("Select Tomtom Map directory:");
		String targetDir;
		if(fc.execute() != fc.IDCANCEL){
			targetDir = fc.getChosen();
			try{
				//loop through type
				for(int j = 0; j <= 15; j++){
					switch (j){
						case 0: ctype = "0"; break;
						case 1: ctype = "2"; break;
						case 2: ctype = "3"; break;
						case 3: ctype = "4"; break;
						case 4: ctype = "5"; break;
						case 5: ctype = "6"; break;
						case 6: ctype = "453"; break;
						case 7: ctype = "8"; break;
						case 8: ctype = "11"; break;
						case 9: ctype = "12"; break;
						case 10: ctype = "13"; break;
						case 11: ctype = "137"; break;
						case 12: ctype = "50";break;
						case 13: ctype = "51";break;
						case 14: ctype = "52";break;
						case 15: ctype = "53";break;
						case 16: ctype = "54";break;
						case 17: ctype = "55";break;
					} 
					dfile = new File(targetDir + "/GC-" + wayType[j] + ".ov2");
					dfile.delete();
					outp =  new RandomAccessFile(targetDir + "/GC-" + wayType[j] + ".ov2","rw");
					//loop through database
					for(int i = 0; i<cacheDB.size(); i++){
						holder=(CacheHolder)cacheDB.get(i);
						if( ctype.equals(holder.type) && holder.is_black == false && holder.is_filtered == false){
							pll = new ParseLatLon(holder.LatLon);
							pll.parse();
							d = 2;
							outp.writeByte((byte)d);
							data = holder.wayPoint.length()+holder.CacheName.length()+holder.CacheOwner.length()+holder.hard.length()+holder.terrain.length()+holder.CacheSize.length()+27;
							d = 0;
							c = data / 65536;
							b = (data - c *65536) / 256;
							a = (data - c * 65536 - b * 256);
							outp.writeByte((byte)a);
							outp.writeByte((byte)b);
							outp.writeByte((byte)c);
							outp.writeByte((byte)d);
							latlon = Float.valueOf(pll.getLonDeg()).floatValue();
							latlon *=100000;
							data = (int) latlon;
							d = 0;
							c = data / 65536;
							b = (data - c *65536) / 256;
							a = (data - c * 65536 - b * 256);
							outp.writeByte((byte)a);
							outp.writeByte((byte)b);
							outp.writeByte((byte)c);
							outp.writeByte((byte)d);
							latlon = Float.valueOf(pll.getLatDeg()).floatValue();
							latlon *=100000;
							data = (int) latlon;
							d = 0;
							c = data / 65536;
							b = (data - c *65536) / 256;
							a = (data - c * 65536 - b * 256);
							outp.writeByte((byte)a);
							outp.writeByte((byte)b);
							outp.writeByte((byte)c);
							outp.writeByte((byte)d);
							outp.writeBytes(holder.wayPoint);
							outp.writeBytes(" - ");
							outp.writeBytes(holder.CacheName);
							outp.writeBytes(" by ");
							outp.writeBytes(holder.CacheOwner);
							//Wenn Leerzeichen am Ende von Cache.Owner entfernt: 
							//Hier wieder einfügen
							//und data = holder.wayPoint.length()+holder.CacheName.length()+.....
							//wider um 1 erhöhen
							outp.writeBytes("- ");             
							outp.writeBytes(holder.hard);
							outp.writeBytes("/");
							outp.writeBytes(holder.terrain);
							outp.writeBytes(" - ");
							outp.writeBytes(holder.CacheSize);
							outp.writeByte((byte)d);
							symCounter++;
						}//if holder...
					}//for ... i < cacheDB ...			
					// overlay section
					outp.close();
				}
			}catch(Exception e){
				//Vm.debug("Error writing to OVL file!");
			}
		} // if execute
	}
}
