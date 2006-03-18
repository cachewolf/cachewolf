package CacheWolf;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.filechooser.*;

/**
*	Class to export the cache database (index) to an ascii overlay file for
*	the TOP50 map products (mainly available in german speaking countries).
*/
public class OVLExporter{
	
	Vector cacheDB;
	Preferences myPreferences;
	
	public OVLExporter(Vector db, Preferences pref){
		cacheDB = db;
		myPreferences = pref;
	}
	
	public void doIt(){
		CacheHolder holder;
		ParseLatLon pll;
		int symCounter = 1;
		FileChooser fc = new FileChooser(FileChooser.SAVE, myPreferences.mydatadir);
		fc.setTitle("Select target file:");
		if(fc.execute() != fc.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
				//symbols section, loop through database
				//a circle and text per cache is created
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon,".");
						pll.parse();
						//the circle!
						outp.print("[Symbol "+Convert.toString(symCounter)+"]\r\n");
						outp.print("Typ=6\r\n");
						outp.print("Width=15\r\n");
						outp.print("Height=15\r\n");
						outp.print("Col=1\r\n");
						outp.print("Zoom=1\r\n");
						outp.print("Size=2\r\n");
						outp.print("Area=2\r\n");
						outp.print("XKoord="+pll.getLonDeg()+"\r\n");
						outp.print("YKoord="+pll.getLatDeg()+"\r\n");
						symCounter++;
						//the text
						outp.print("[Symbol "+Convert.toString(symCounter)+"]\r\n");
						outp.print("Typ=2\r\n");
						outp.print("Col=1\r\n");
						outp.print("Zoom=1\r\n");
						outp.print("Size=2\r\n");
						outp.print("Area=2\r\n");
						outp.print("Font=3\r\n");
						outp.print("Dir=1\r\n");
						outp.print("XKoord="+Convert.toString(pll.lon2+0.002).replace(',', '.')+"\r\n");
						outp.print("YKoord="+Convert.toString(pll.lat2+0.001).replace(',', '.')+"\r\n");
						outp.print("Text="+holder.wayPoint+"\r\n");
						symCounter++;
					}//if holder...
				}//for ... i < cacheDB ...			
				// overlay section
				outp.print("[Overlay]\r\n");
				outp.print("Symbols="+Convert.toString(symCounter-1)+"\r\n");
				// maplage section
				outp.print("[MapLage]\r\n");
				outp.print("MapName=Gesamtes Bundesgebiet (D1000)\r\n");
				outp.print("DimmFc=100\r\n");
				outp.print("ZoomFc=100\r\n");
				outp.print("CenterLat="+myPreferences.mybrDeg+".00\r\n");
				outp.print("CenterLong="+myPreferences.mylgDeg+".00\r\n");
				outp.print("RefColor=255\r\n");
				outp.print("RefRad=58\r\n");
				outp.print("RefLine=6\r\n");
				outp.print("RefOn=0\r\n");
				outp.print("\r\n");
				
				outp.close();
			}catch(Exception e){
				//Vm.debug("Error writing to OVL file!");
			}
		} // if execute
	}
}
