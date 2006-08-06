package CacheWolf;
import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;
import ewe.ui.*;

/**
*	Class to export cache database to an ASCII (CSV!) file.
*   This file can be used by I2C's POI Converter to generate
*   POIs for different routing programmes, especially for
*	Destinator ;-) !
*/
public class ASCExporter{
	
	Vector cacheDB;
	Preferences myPreferences;
	
	public ASCExporter(Vector db, Preferences pref){
		cacheDB = db;
		myPreferences = pref;
	}
	
	public void doIt(){
		String str = new String();
		String dummy = new String();
		CacheHolder holder = new CacheHolder();
		ParseLatLon pll;
		FileChooser fc = new FileChooser(FileChooser.SAVE, myPreferences.mydatadir);
		fc.setTitle("Select target file:");
		if(fc.execute() != FormBase.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo+".csv")));
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon, ".");
						pll.parse();
						dummy = holder.CacheName;
						dummy = dummy.replace(',', ' ');
						str = dummy+","+dummy+","+ pll.getLonDeg()+"," + pll.getLatDeg()+",,,,";
						outp.print(str+"\r\n");
					}//if
				}//for
				outp.close();
			}catch (Exception e){
				//Vm.debug("Problem writing to ASC file!");
			}//try
		} //if else {
		}
	}
}
