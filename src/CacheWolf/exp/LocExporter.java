    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf.exp;
import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.Common;
import CacheWolf.Global;
import ewe.io.File;
import ewe.io.FileBase;


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
			} catch (Exception ex){ pref.log("[LocExporter:record]Invalid value for garmin.MaxWaypointLength",ex); }
		}
		strBuf.append("\"><![CDATA[");
		strBuf.append(simplifyString(ch.getCacheName()));
		if (Global.getPref().addDetailsToName) {
			if ( !Global.getPref().addDetailsToWaypoint ) {
				strBuf.append( getShortDetails( ch ) );
			}
			CacheHolderDetail det = ch.getCacheDetails(true);
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
}
