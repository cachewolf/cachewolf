package CacheWolf;
import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;

/**
*	Class to export cache database to an ASCII (CSV!) file.
*   This file can be used by I2C's POI Converter to generate
*   POIs for different routing programmes, especially for
*	Destinator ;-) !
*/
public class ASCExporter{
	//TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	
	public ASCExporter(Preferences p,Profile prof){
		pref = p;
		profile=prof;
		cacheDB = prof.cacheDB;
	}
	
	public void doIt(){
		String str;
		String dummy;
		CacheHolder holder;
		ParseLatLon pll;
		FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
		fc.setTitle("Select target file:");
		fc.addMask("*.csv");
		fc.defaultExtension="csv";
		if(fc.execute() != FileChooser.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
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
				ewe.sys.Vm.debug("Problem writing to ASC file! "+e.toString());
			}//try
		} //if else {
		}
	}
