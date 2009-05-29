package exp;
import CacheWolf.*;
import ewe.io.File;
import ewe.io.FileBase;
import ewesoft.xml.*;
import ewesoft.xml.sax.*;
import ewe.util.Vector;


/**
*	Class to export the cache database into an geocaching .loc file that may be exported
*	by GPSBabel to a Garmin GPS.
*
*	Now includes mapping of cachetypes to user defined icons (as defined in file garminmap.xml).
*/
public class LocExporter extends Exporter{
	public static int MODE_AUTO = TMP_FILE;
	/**
	 * Defines how certain cachetypes are mapped to user icons
	 */
	private static GarminMap gm=null;

	public LocExporter(){
		super();
		this.setMask("*.loc");
		this.setHowManyParams(NO_PARAMS);
		if (Global.getPref().addDetailsToName) {
			this.setNeedCacheDetails(true);
		}
		if ((new File(FileBase.getProgramDirectory()+"/garminmap.xml")).exists()) {
			gm=new GarminMap();
			gm.readGarminMap();
		}
	}

	public String header () {
		return "<?xml version=\"1.0\"?><loc version=\"1.0\" src=\"EasyGPS\">\r\n";
	}

	public String record(CacheHolder ch){

		// filter out not valid coords
		if (!ch.pos.isValid()) return null;
		StringBuffer strBuf = new StringBuffer(200);
		strBuf.append("<waypoint>\r\n   <name id=\"");
		String wptName=simplifyString(ch.getWayPoint());
		if (Global.getPref().addDetailsToWaypoint) {
			wptName += getShortDetails( ch );
		}
		if (Global.getPref().garminMaxLen==0)
			strBuf.append(wptName);
		else {
			try {
				strBuf.append(wptName.substring(wptName.length()-Global.getPref().garminMaxLen));
			} catch (Exception ex){ pref.log("Invalid value for garmin.MaxWaypointLength"); }
		}
		strBuf.append("\"><![CDATA[");
		strBuf.append(simplifyString(ch.getCacheName()));
		if (Global.getPref().addDetailsToName) {
			if ( !Global.getPref().addDetailsToWaypoint ) {
				strBuf.append( getShortDetails( ch ) );
			}
			CacheHolderDetail det = ch.getExistingDetails();
			if ( (!det.Hints.equals("null")) && (det.Hints.length() > 0) ) {
				strBuf.append(":");
				strBuf.append( simplifyString(Common.rot13(det.Hints)) );
			}
		}
		strBuf.append("]]></name>\r\n   <coord lat=\"");
		strBuf.append(ch.pos.getLatDeg(CWPoint.DD));
		strBuf.append("\" lon=\"");
		strBuf.append(ch.pos.getLonDeg(CWPoint.DD));
		strBuf.append("\"/>\r\n   <type>");
		if (gm!=null) {
			strBuf.append(gm.getIcon(ch));
		} else {
			if (ch.is_found())
				strBuf.append("Geocache Found");
			else
				strBuf.append("Geocache");
		}
		strBuf.append("</type>\r\n</waypoint>\r\n");
		return strBuf.toString();
	}
	public String trailer(){
		return "</loc>\r\n";
	}

	/**
	 * This class implements user defined icons which depend on the cache type and the found status.
	 * See also http://www.geoclub.de/ftopic10413.html
	 * @author salzkammergut
	 *
	 */
	private class GarminMap extends MinML {

		private Vector symbols=new Vector(24);

		String lastName;
		public void readGarminMap(){
			try{
				String datei = FileBase.getProgramDirectory() + "/garminmap.xml";
				ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(datei));
				parse(r);
				r.close();
			}catch(Exception e){
				if (e instanceof NullPointerException)
					Global.getPref().log("Error reading garminmap.xml: NullPointerException in Element "+lastName +". Wrong attribute?",e,true);
				else
					Global.getPref().log("Error reading garminmap.xml: ", e);
			}
		}
		public void startElement(String name, AttributeList atts){
			lastName=name;
			if (name.equals("icon")) {
				symbols.add(new IconMap(atts.getValue("type"),atts.getValue("name"),atts.getValue("found"),
						atts.getValue("size"),atts.getValue("terrain"),atts.getValue("difficulty"),
						atts.getValue("status")));
			}
		}

		public String getIcon(CacheHolder ch) {
			int mapSize=symbols.size();
			// Try each icon in turn
			for (int i=0; i<mapSize; i++) {
				IconMap icon=(IconMap) symbols.get(i);
				boolean match=true;
				// If a certainattribute is not null it must match the current caches values
				match=match && (icon.type==null) || ch.getType()==0 || icon.type.equals(String.valueOf(ch.getType()));
				match=match && (icon.size==null) || ch.getCacheSize()==0 || icon.size.equalsIgnoreCase(CacheSize.getExportShortId(ch.getCacheSize()));
				match=match && (icon.terrain==null) || ch.getTerrain()==0 || icon.terrain.equals(CacheTerrDiff.shortDT(ch.getTerrain()));
				match=match && (icon.difficulty==null) ||  ch.getHard()==0 || icon.difficulty.equals(CacheTerrDiff.shortDT(ch.getHard()));
				match=match && (icon.status==null) ||  ch.getCacheStatus().startsWith(icon.status);
				match=match && (icon.found==null) || ch.is_found();
				if (match) return icon.name;
			}

			// If it is not a mapped type, just use the standard mapping
			if (ch.is_found())
				return "Geocache Found";
			else
				return "Geocache";
		}

		private class IconMap {
			public String type;
			public String name;
			public String size;
			public String terrain;
			public String difficulty;
			public String found;
			public String status;

			IconMap(String type, String name, String found, String size, String terrain, String difficulty, String status) {
				this.type=type;
				this.name=name;
				this.found=found;
				this.size=size;
				this.terrain=terrain;
				this.difficulty=difficulty;
				this.status=status;
			}
		}

	}

}
