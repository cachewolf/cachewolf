package CacheWolf.exp;
import ewe.io.File;
import ewe.io.FileBase;
import CacheWolf.*;

/**
*	Class to export the cache database (index) to an OziExplorer File
*/
public class OziExporter extends Exporter{
	
	GarminMap poiMapper;
	
	public OziExporter(Preferences p, Profile prof){
		super();
		this.setHowManyParams(LAT_LON);
		this.setMask("*.wpt");
		if ((new File(FileBase.getProgramDirectory()+"/garminmap.xml")).exists()) {
			poiMapper=new GarminMap();
			poiMapper.readGarminMap();
		} else {
			poiMapper = null;
		}
	}
	
	public String header () {
		StringBuffer strBuf = new StringBuffer(200);
		
		strBuf.append("OziExplorer CE Waypoint File Version 1.2\r\n");
		strBuf.append("WGS 84\r\n");
		strBuf.append("Reserved 2\r\n");
		strBuf.append("Reserved 3\r\n");

		return strBuf.toString();
	}

	public String record(CacheHolder ch, String lat, String lon){
		StringBuffer strBuf = new StringBuffer(200);
		String tmpName;

		// Field 1 : Number - this is the location in the array (max 1000), must be unique, 
		// usually start at 1 and increment. Can be set to -1 (minus 1) and the number will be auto generated.
		strBuf.append("-1,");
		// Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
		if (ch.isCustomWpt() || ch.isAddiWpt()) {
			strBuf.append(ch.getWayPoint() + ",");
		} else {
			strBuf.append(ch.getWayPoint()
			.concat(" ")
			.concat(CacheType.getExportShortId(ch.getType()))
			.concat(String.valueOf(ch.getHard()))
			.concat(String.valueOf(ch.getTerrain()))
			.concat(CacheSize.getExportShortId(ch.getCacheSize()))
			.concat(",")
			);
		}
		// Field 3 : Latitude - decimal degrees.
		strBuf.append(lat+",");
		// Field 4 : Longitude - decimal degrees.
		strBuf.append(lon+",");
		// Field 5 : Date - see Date Format below, if blank a preset date will be used
		strBuf.append(",");
		// Field 6 : Symbol - 0 to number of symbols in GPS
		strBuf.append("0,");
		// Field 7 : Status - always set to 1
		strBuf.append("1,");
		// Field 8 : Map Display Format
		strBuf.append("3,");
		// Field 9 : Foreground Color (RGB value)
		strBuf.append("0,");
		// Field 10 : Background Color (RGB value)
		String color="16777215";
		if (poiMapper != null) {
			String tmpcolor = poiMapper.ozicolor(ch);
			if (tmpcolor != null)
				color = tmpcolor;
		}
		strBuf.append(color+",");
		// Field 11 : Description (max 40), no commas
		tmpName = simplifyString(ch.getCacheName()).replace(',', ' ');
		if (tmpName.length() <= 40){
			strBuf.append(tmpName + ",");
		}
		else {
			strBuf.append(tmpName.substring(0,40) + ",");
		}
		// Field 12 : Pointer Direction
		strBuf.append("0,");
		// Field 13 : Garmin Display Format
		strBuf.append("0,");
		// Field 14 : Proximity Distance - 0 is off any other number is valid
		strBuf.append("0,");
		// Field 15 : Altitude - in feet (-777 if not valid)
		strBuf.append("-777,");
		// Field 16 : Font Size - in points
		strBuf.append("7,");
		// Field 17 : Font Style - 0 is normal, 1 is bold.
		strBuf.append("0,");
		// Field 18 : Symbol Size - 17 is normal size
		strBuf.append("17,");
		// Field 19 : Proximity Symbol Position
		strBuf.append("0,");
		// Field 20 : Proximity Time
		strBuf.append("10.0,");
	    // Field 21 : Proximity or Route or Both
		strBuf.append("2,");
		// Field 22 : File Attachment Name
		strBuf.append(",");
		// Field 23 : Proximity File Attachment Name
		strBuf.append(",");
		// Field 24 : Proximity Symbol Name
		strBuf.append(" \r\n");

		return strBuf.toString();
	}
}
