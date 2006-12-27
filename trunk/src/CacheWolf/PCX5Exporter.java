package CacheWolf;
import ewe.filechooser.*;
import ewe.io.*;
import ewe.sys.*;
import ewe.util.*;

/**
*	Class to export the cache database into an ascii file that may be imported
*	ba Mapsource (c) by Garmin.
*/
public class PCX5Exporter{
//	TODO Exportanzahl anpassen: Bug: 7351
	Vector cacheDB;
	Preferences pref;
	Profile profile;

	public static int MODE_AUTO = 0;
	public static int MODE_ASK = 1;
	
	public PCX5Exporter(Preferences p, Profile prof){
		pref = p;
		profile=prof;
		cacheDB = profile.cacheDB;
	}

	
	public void doIt(int mode){
		CacheHolder holder = new CacheHolder();
		String saveStr = new String();
		String latlonstr = new String();
		String dummy = new String();
		String cwd = new String();
		cwd = File.getProgramDirectory();
		File saveTo = new File(cwd + "/temp.pcx");
		if(mode == MODE_ASK) {
			FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
			fc.setTitle("Select target file:");
			if(fc.execute() != fc.IDCANCEL) saveTo = fc.getChosenFile();
		}
		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
			outp.print("H  SOFTWARE NAME & VERSION\n");
			outp.print("I  PCX5 2.09\n");
			outp.print("\n");
			outp.print("H  R DATUM                IDX DA            DF            DX            DY            DZ\n");
			outp.print("M  G WGS 84               121 +0.000000e+00 +0.000000e+00 +0.000000e+00 +0.000000e+00 +0.000000e+00\n");
			outp.print("\n");
			outp.print("H  COORDINATE SYSTEM\n");
			outp.print("U  LAT LON DM\n");
			outp.print("\n");
			outp.print("H  IDNT   LATITUDE  LONGITUDE      DATE      TIME     ALT   DESCRIPTION                              PROXIMITY     SYMBOL ;waypts\r\n");
			
			for(int i = 0; i<cacheDB.size(); i++){
				holder=(CacheHolder)cacheDB.get(i);
				if(holder.is_black == false && holder.is_filtered == false){
					  //Vm.debug(Convert.toString(i));
					  //dummy = holder.CacheName;
					  //dummy = replace(dummy, ",", "");
					  //dummy = Reducer.convert(dummy, true, 6);
					  saveStr = "W  " + holder.wayPoint + " ";
					  latlonstr = replace(holder.LatLon, "°", " ");
					  latlonstr = replace(latlonstr, " ", "");
					  latlonstr = replace(latlonstr, "E", " E");
					  latlonstr = replace(latlonstr, "W", " W");
					  saveStr = saveStr + latlonstr + "     ";
					  latlonstr = holder.CacheName;
					  // has 42 characters
					  while(latlonstr.length() < 41){
						  latlonstr = latlonstr + " ";
					  }
					  if(latlonstr.length() > 41){
						  latlonstr = latlonstr.substring(0,40);
					  }
					  saveStr = saveStr + "01-JAN-04 01:00:00 -0000 " + latlonstr + " 0.000000e+000  ";
					  if(holder.is_found){
						saveStr = saveStr +"8256\r\n";
					  } else {
						saveStr = saveStr +"8255\r\n";
					  }
					  outp.print(saveStr);
				  } //if
				} // for
				outp.close();
		} catch (Exception e){
			//Vm.debug("Problem exporting to pcx5 file");
		}
	} //end of method
		
	private String replace( String s, String f, String r )
	{
	   if (s == null)  return s;
	   if (f == null)  return s;
	   if (r == null)  r = "";
	
	   int index01 = s.indexOf( f );
	   while (index01 != -1)
	   {
		  s = s.substring(0,index01) + r + s.substring(index01+f.length());
		  index01 += r.length();
		  index01 = s.indexOf( f, index01 );
	   }
	   return s;
	}
}
