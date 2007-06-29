package exp;
import ewe.fx.Color;
import ewe.io.File;
import ewe.sys.Convert;
import CacheWolf.*;

/**
*	Class to export the cache database (index) to an KML-File
*	which can be read by Google Earth   
*   
*/
public class KMLExporter extends Exporter {
	private static final String COLOR_FOUND = "ff98fb98"; 
	private static final String COLOR_OWNED = "ffffaa55"; 
	private static final String COLOR_AVAILABLE = "ffffffff";
	private static final String COLOR_ARCHIVED = "ff0000ff";


	public KMLExporter(){
		super();
		this.setMask("*.kml");
		this.setHowManyParams(LAT_LON);
	}

	
	public KMLExporter(Preferences p, Profile prof){
			super();
			this.setMask("*.kml");
	}
	
	public String header () {
		StringBuffer strBuf = new StringBuffer(200);
				
		strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		strBuf.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");
		strBuf.append("<Folder>\r\n");
		strBuf.append("<name>CacheWolf</name>\r\n");
		strBuf.append("<open>1</open>\r\n");

		return strBuf.toString();
	}
	
	public String record(CacheHolderDetail ch, String lat, String lon){
		StringBuffer strBuf = new StringBuffer(200);
		
		strBuf.append("   <Placemark>\r\n");
		strBuf.append("      <description>http://www.geocaching.com/seek/cache_details.aspx?wp="+ch.wayPoint+"</description>\r\n");
		strBuf.append("      <name>"+ ch.wayPoint + " - " + SafeXML.clean(ch.CacheName) +"</name>\r\n");
		strBuf.append("      <LookAt>\r\n");
		strBuf.append("         <latitude>" + lat + "</latitude>\r\n");
		strBuf.append("         <longitude>" + lon + "</longitude>\r\n");
		strBuf.append("         <range>10000</range><tilt>0</tilt><heading>0</heading>\r\n");
		strBuf.append("      </LookAt>\r\n");
		strBuf.append("      <Point>\r\n");
		strBuf.append("         <coordinates>"  + lon + "," + lat + "</coordinates>\r\n");
		strBuf.append("      </Point>\r\n");
		strBuf.append("      <Style>\r\n");
		strBuf.append("      <IconStyle>\r\n");
		strBuf.append("         <Icon>\r\n");
		strBuf.append("            <href>"+ File.getProgramDirectory()+ "/" + CacheType.type2pic(Convert.parseInt(ch.type))+ "</href>\r\n");
		strBuf.append("         </Icon>\r\n");
		strBuf.append("      </IconStyle>\r\n");
		strBuf.append("      <LabelStyle>\r\n");
		strBuf.append("         <color>" + getColor(ch) + "</color>\r\n");
		strBuf.append("      </LabelStyle>\r\n");
		strBuf.append("      </Style>\r\n");
		strBuf.append("   </Placemark>\r\n");
	
		return strBuf.toString();
	}
	
	public String trailer(){
		StringBuffer strBuf = new StringBuffer(50);

		strBuf.append("</Folder>\r\n");
		strBuf.append("</kml>\r\n");

		return strBuf.toString();
	}
	
	private String getColor(CacheHolderDetail ch){
		if (ch.is_found) return COLOR_FOUND;
		if (ch.is_owned) return COLOR_OWNED;
		if (ch.is_archived) return COLOR_ARCHIVED;
		
		return COLOR_AVAILABLE;
	}
	
}
