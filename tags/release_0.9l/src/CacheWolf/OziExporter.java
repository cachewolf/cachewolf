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
public class OziExporter{
	
	Vector cacheDB;
	Preferences myPreferences;
	
	public OziExporter(Vector db, Preferences pref){
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
				//header
				outp.println("OziExplorer CE Waypoint File Version 1.2");
				outp.println("WGS 84");
				outp.println("Reserved 2");
				outp.println("Reserved 3");
				//loop through database
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon,".");
						pll.parse();
						// Field 1 : Number - this is the location in the array (max 1000), must be unique, 
						// usually start at 1 and increment. Can be set to -1 (minus 1) and the number will be auto generated.
						outp.print("-1,");
						// Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
						outp.print(holder.wayPoint + ",");
						// Field 3 : Latitude - decimal degrees.
						outp.print(pll.getLatDeg()+",");
						// Field 4 : Longitude - decimal degrees.
						outp.print(pll.getLonDeg()+",");
						// Field 5 : Date - see Date Format below, if blank a preset date will be used
						outp.print(",");
						// Field 6 : Symbol - 0 to number of symbols in GPS
						outp.print("0,");
						// Field 7 : Status - always set to 1
						outp.print("1,");
						// Field 8 : Map Display Format
						outp.print("0,");
						// Field 9 : Foreground Color (RGB value)
						outp.print("0,");
						// Field 10 : Background Color (RGB value)
						outp.print("16777215,");
						// Field 11 : Description (max 40), no commas
						if (holder.CacheName.length() <= 40){
							outp.print(holder.CacheName + ",");
						}
						else {
							outp.print(holder.CacheName.substring(0,40) + ",");
						}
						// Field 12 : Pointer Direction
						outp.print("0,");
						// Field 13 : Garmin Display Format
						outp.print("0,");
						// Field 14 : Proximity Distance - 0 is off any other number is valid
						outp.print("0,");
						// Field 15 : Altitude - in feet (-777 if not valid)
						outp.print("-777,");
						// Field 16 : Font Size - in points
						outp.print("8,");
						// Field 17 : Font Style - 0 is normal, 1 is bold.
						outp.print("1,");
						// Field 18 : Symbol Size - 17 is normal size
						outp.print("17,");
						// Field 19 : Proximity Symbol Position
						outp.print("0,");
						// Field 20 : Proximity Time
						outp.print("  10.0,");
					    // Field 21 : Proximity or Route or Both
						outp.print("2,");
						// Field 22 : File Attachment Name
						outp.print(",");
						// Field 23 : Proximity File Attachment Name
						outp.print(",");
						// Field 24 : Proximity Symbol Name
						outp.println(" ");
					}//if holder...
				}//for ... i < cacheDB ...			
				// overlay section
				
				outp.close();
			}catch(Exception e){
				//Vm.debug("Error writing to OZI file!");
			}
		} // if execute
	}
}
