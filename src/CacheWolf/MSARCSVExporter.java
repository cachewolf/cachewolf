package CacheWolf;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.filechooser.*;

/**
*	Class to export the cache database (index) to an CSV File
*	which can bei easy importet bei MS AutoRoute (testet with AR 2001 German)   
*   Format of the file:
*   Name;Breitengrad;Längengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink
*   
*/
public class MSARCSVExporter{
	
	Vector cacheDB;
	Preferences myPreferences;
	
	public MSARCSVExporter(Vector db, Preferences pref){
		cacheDB = db;
		myPreferences = pref;
	}
	
	public void doIt(){
		CacheHolder holder;
		CacheReaderWriter crw = new CacheReaderWriter();
		ParseLatLon pll;
		int symCounter = 0;
		FileChooser fc = new FileChooser(FileChooser.SAVE, myPreferences.mydatadir);
		fc.setTitle("Select target file:");
		if(fc.execute() != fc.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
				//Create Header for German Version
				outp.println("Name;Breitengrad;Längengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink\r");
				//loop through database
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						try{crw.readCache(holder, myPreferences.mydatadir);
						}catch(Exception e){
							//Vm.debug("Problem reading cache page");
						}
						pll = new ParseLatLon(holder.LatLon, ".");
						pll.parse();
						outp.print("\"" + holder.wayPoint + " - " + holder.CacheName + "\";");
						outp.print(pll.getLatDeg().replace('.',',') + ";");
						outp.print(pll.getLonDeg().replace('.',',') + ";");
						outp.print("\"" + CacheType.transType(holder.type) + "\";");
						outp.print("\"" + holder.CacheSize + "\";");
						outp.print("\"" + holder.wayPoint + "\";");
						outp.print("\"" + holder.DateHidden + "\";");
						outp.print("\"" + holder.URL + "\"\r\n");
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
