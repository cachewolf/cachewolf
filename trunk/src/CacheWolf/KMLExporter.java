package CacheWolf;
import ewe.util.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.filechooser.*;

/**
*	Class to export the cache database (index) to an KML-File
*	which can be read by Google Earth   
*   
*/
public class KMLExporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences pref;
	Profile profile;
	
	public KMLExporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}
	
	public void doIt(){
		CacheHolder holder;
		ParseLatLon pll;
		FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
		fc.setTitle("Select target file:");
		if(fc.execute() != fc.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
				//Create Header for KML-File
				outp.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
				outp.println("<kml xmlns=\"http://earth.google.com/kml/2.0\">");
				outp.println("<Folder>");
				outp.println("<name>CacheWolf</name>");
				outp.println("<open>1</open>");
				//loop through database
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon,".");
						pll.parse();
						outp.println("   <Placemark>");
						outp.println("      <description>http://www.geocaching.com/seek/cache_details.aspx?wp="+holder.wayPoint+"</description>");
						outp.println("      <name>"+ holder.wayPoint + " - " + SafeXML.clean(holder.CacheName) +"</name>");
						outp.println("      <LookAt>");
						outp.println("         <latitude>" + pll.getLatDeg() + "</latitude>");
						outp.println("         <longitude>" + pll.getLonDeg() + "</longitude>");
						outp.println("         <range>10000</range><tilt>0</tilt><heading>0</heading>");
						outp.println("      </LookAt>");
						outp.println("      <Point>");
						outp.println("         <coordinates>"  + pll.getLonDeg() + "," + pll.getLatDeg() + "</coordinates>");
						outp.println("      </Point>");
						outp.println("   </Placemark>");
					}//if holder...
				}//for ... i < cacheDB ...			
				// footer
				outp.println("</Folder>");
				outp.println("</kml>");
				outp.close();
			}catch(Exception e){
				//Vm.debug("Error writing to OVL file!");
			}
		} // if execute
	}
}
