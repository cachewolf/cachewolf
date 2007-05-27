package exp;
import CacheWolf.*;
import ewe.io.File;

/**
*	Class to export the cache database into an ascii file that may be imported
*	ba Mapsource (c) by Garmin.
*/
public class PCX5Exporter extends Exporter{
	public static int MODE_AUTO = TMP_FILE;
	public static int MODE_ASK = ASK_FILE;
	
	public PCX5Exporter(Preferences p, Profile prof){
		super();
		this.setMask("*.wpt");
		this.setTmpFileName(File.getProgramDirectory() + "/temp.pcx");
		this.setHowManyParams(NO_PARAMS);
	}
	
	public String header () {
		StringBuffer strBuf = new StringBuffer(200);

		strBuf.append("H  SOFTWARE NAME & VERSION\n");
		strBuf.append("I  PCX5 2.09\n");
		strBuf.append("\n");
		strBuf.append("H  R DATUM                IDX DA            DF            DX            DY            DZ\n");
		strBuf.append("M  G WGS 84               121 +0.000000e+00 +0.000000e+00 +0.000000e+00 +0.000000e+00 +0.000000e+00\n");
		strBuf.append("\n");
		strBuf.append("H  COORDINATE SYSTEM\n");
		strBuf.append("U  LAT LON DM\n");
		strBuf.append("\n");
		strBuf.append("H  IDNT   LATITUDE  LONGITUDE      DATE      TIME     ALT   DESCRIPTION                              PROXIMITY     SYMBOL ;waypts\r\n");
		
		return strBuf.toString();
	}
	
	public String record(CacheHolderDetail ch){
		StringBuffer strBuf = new StringBuffer(200);
		String latlonstr, dummy;

		  strBuf.append("W  " + ch.wayPoint + " ");
		  latlonstr = STRreplace.replace(ch.LatLon, "°", " ");
		  latlonstr = STRreplace.replace(latlonstr, " ", "");
		  latlonstr = STRreplace.replace(latlonstr, "E", " E");
		  latlonstr = STRreplace.replace(latlonstr, "W", " W");
		  strBuf.append(latlonstr + "     ");
		  strBuf.append("01-JAN-04 01:00:00 -0000 ");
		  // has 42 characters
		  dummy = ch.CacheName;
		  if (dummy.length() < 40){
			  strBuf.append(dummy);
			  int i = 40 - dummy.length();
			  for (; i > 0; i--){
				  strBuf.append(' ');
			  }
		  } else {
			  strBuf.append(dummy.substring(0,40));
		  }
		  strBuf.append(" 0.000000e+000 ");
		  if(ch.is_found) strBuf.append("  8256\r\n");
		  else  		  strBuf.append("  8255\r\n");
		return strBuf.toString();
	}
	
}
