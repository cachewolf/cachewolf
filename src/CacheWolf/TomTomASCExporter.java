package CacheWolf;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.filechooser.*;

/**
*	Class to export the cache database (index) to an TomTom ASC File
*   Format of the file:
*   Lon,Lat,"Description"
*   
*   Example for one entry:
*	8.635,50.386,"Adlerhorst"
*/
public class TomTomASCExporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences myPreferences;
	
	public TomTomASCExporter(Vector db, Preferences pref){
		cacheDB = db;
		myPreferences = pref;
	}
	
	public void doIt(){
		CacheHolder holder;
		ParseLatLon pll;
		int symCounter = 0;
		FileChooser fc = new FileChooser(FileChooser.SAVE, myPreferences.mydatadir);
		fc.setTitle("Select target file:");
		if(fc.execute() != fc.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
				//loop through database
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon, ".");
						pll.parse();
						outp.print(pll.getLonDeg()+",");
						outp.print(pll.getLatDeg()+",");
						outp.println("\"" + holder.CacheName + "\"\r");
						symCounter++;
					}//if holder...
				}//for ... i < cacheDB ...			
				// overlay section
				
				outp.close();
			}catch(Exception e){
				//Vm.debug("Error writing to OVL file!");
			}
		} // if execute
	}
}
