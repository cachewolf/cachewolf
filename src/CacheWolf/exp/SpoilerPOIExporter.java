package CacheWolf.exp;
import ewe.sys.Time;
import ewe.ui.FormBase;
import CacheWolf.*;

/** 
 * 
 * @author Kalle
 * Class to create a gpx-File with links to the pictures of a 
 * cache, which is used as input for the POILoader from Garmin.
 */

public class SpoilerPOIExporter extends Exporter {
	private SpoilerPOIExporterScreen infoScreen;
	private boolean onlySpoiler;
	
	public SpoilerPOIExporter(Preferences p, Profile prof){
		super();
		this.setMask("*.gpx");
		this.setHowManyParams(LAT_LON);
		this.setNeedCacheDetails(true);
	}

	public void doIt() {
		infoScreen = new SpoilerPOIExporterScreen("SpoilerPOIExport");
		if (infoScreen.execute() == FormBase.IDCANCEL) return;
		onlySpoiler = infoScreen.getOnlySpoiler();
		super.doIt();
	}
	
	public String header () {
		StringBuffer strBuf = new StringBuffer(200);
		Time tim = new Time();

		strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n");
		strBuf.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"CacheWolf\" version=\"1.1\"" +
				      " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				      "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n");
		strBuf.append("  <metadata>\r\n");
		strBuf.append("    <link href=\"http://www.cachewolf.de\">\r\n");
		strBuf.append("      <text>CacheWolf</text>\r\n");
		strBuf.append("    </link>\r\n");
		tim = tim.setFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
		tim = tim.setToCurrentTime();
		strBuf.append("    <time>" + tim.toString() + "</time>\r\n");
		strBuf.append("  </metadata>\r\n");
		return strBuf.toString();
	}
	
	public String record(CacheHolder ch, String lat, String lon) {
		StringBuffer strBuf = new StringBuffer(1000);
		String comment,filename;
		
		// Makes only sense for main waypoints
		if (ch.isAddiWpt()) return null;
		
		// First check, if there a any pictures in the db for the wpt
		ch.getCacheDetails(false);
		if (! ch.detailsLoaded()) return null;
		if (ch.details.images.size() == 0) return null;
		
		for (int i=0; i < ch.details.images.size(); i++ ) {
			filename = ch.details.images.get(i).getFilename();
			comment = ch.details.images.get(i).getTitle();
			// POILoader can only work with JPG-Files
			if ( !filename.endsWith(".jpg")) continue;
			// Try to export only Spoiler
			if ( onlySpoiler && (comment.indexOf("oiler") < 1)) continue;
			
			strBuf.append("<wpt lat=\"" + lat + "\" lon=\"" + lon + "\">\r\n");
			strBuf.append("  <name>Sp " + i + ": " + SafeXML.cleanGPX(ch.cacheName) + "</name>\r\n");
			strBuf.append("  <cmt>\r\n");
			if (ch.details.Hints.length()> 0){
				strBuf.append("  Hint: " + SafeXML.cleanGPX(Common.rot13(ch.details.Hints)) + "\r\n");
			}
			if (comment != null){
				strBuf.append("  Comment: " + SafeXML.cleanGPX(comment) + "\r\n");
			}
			strBuf.append("  </cmt>\r\n");
			strBuf.append("  <desc>GCcode: " + ch.getWayPoint() + " </desc>\r\n");
			strBuf.append("   <link href=\"" + profile.dataDir + filename + "\"/>\r\n");
			strBuf.append("  <sym>Scenic Area</sym>\r\n");
			strBuf.append("  <extensions>\r\n");
			strBuf.append("     <gpxx:WaypointExtension xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\">\r\n");
			strBuf.append("        <gpxx:DisplayMode>SymbolAndName</gpxx:DisplayMode>\r\n");
			strBuf.append("     </gpxx:WaypointExtension>\r\n");
			strBuf.append("  </extensions>\r\n");
			strBuf.append("</wpt>\r\n");
			strBuf.append("\r\n");
		}
	
		return strBuf.toString();
	}
	
	public String trailer() {
		return "</gpx>\r\n";
	}
}
