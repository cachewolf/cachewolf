package exp;
import CacheWolf.*;
import ewe.sys.Vm;
import ewe.util.Hashtable;

/**
*	Class to export the cache database into an geocaching .loc file that may be exported
*	by GPSBabel to a Garmin GPS.
*/
public class LocExporter extends Exporter{
	public static int MODE_AUTO = TMP_FILE;
	
	public LocExporter(){
		super();
		this.setMask("*.loc");
		this.setHowManyParams(NO_PARAMS);
	}
	
	public String header () {
		return "<?xml version=\"1.0\"?><loc version=\"1.0\" src=\"EasyGPS\">\r\n";
	}
	
	public String record(CacheHolderDetail ch){
		// filter out not valid coords
		if (!ch.pos.isValid()) return null;
		StringBuffer strBuf = new StringBuffer(200);
		strBuf.append("<waypoint>\r\n   <name id=\"");
		strBuf.append(simplifyString(ch.wayPoint));
		strBuf.append("\"><![CDATA[");
		strBuf.append(simplifyString(ch.CacheName));
		strBuf.append("]]></name>\r\n   <coord lat=\"");
		strBuf.append(ch.pos.getLatDeg(CWPoint.DD));
		strBuf.append("\" lon=\"");
		strBuf.append(ch.pos.getLonDeg(CWPoint.DD));
		strBuf.append("\"/>\r\n   <type>");
		if (ch.is_found)
			strBuf.append("Geocache Found");
		else
			strBuf.append("Geocache");
		strBuf.append("</type>\r\n</waypoint>\r\n");
		return strBuf.toString();
	}
	public String trailer(){
		return "</loc>\r\n";
	}
	
}
