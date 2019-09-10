/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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

import CacheWolf.Preferences;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;

/**
 * Class to export the cache database into an geocaching .loc file that may be exported
 * by GPSBabel to a Garmin GPS.
 * <p>
 * Now includes mapping of cachetypes to user defined icons (as defined in file garminmap.xml).
 */
public class LocExporter extends Exporter {
    /**
     * Defines how certain cachetypes are mapped to user icons
     */
    private static GarminMap gm = null;

    public LocExporter() {
        super();
        this.setOutputFileExtension("*.loc");
        this.setExportMethod(EXPORT_METHOD_NO_PARAMS);
        gm = new GarminMap();
    }

    public String header() {
        return "<?xml version=\"1.0\"?><loc version=\"1.0\" src=\"EasyGPS\">\r\n";
    }

    public String record(CacheHolder ch) {

        // filter out not valid coords
        if (!ch.getWpt().isValid())
            return null;
        StringBuffer strBuf = new StringBuffer(200);
        strBuf.append("<waypoint>\r\n   <name id=\"");
        String wptName = simplifyString(ch.getCode());
        if (Preferences.itself().addDetailsToWaypoint) {
            wptName += getShortDetails(ch);
        }
        if (Preferences.itself().garminMaxLen == 0)
            strBuf.append(wptName);
        else {
            try {
                strBuf.append(wptName.substring(wptName.length() - Preferences.itself().garminMaxLen));
            } catch (Exception ex) {
                Preferences.itself().log("[LocExporter:record]Invalid value for garmin.MaxWaypointLength", ex);
            }
        }
        strBuf.append("\"><![CDATA[");
        strBuf.append(simplifyString(ch.getName()));
        if (Preferences.itself().addDetailsToName) {
            if (!Preferences.itself().addDetailsToWaypoint) {
                strBuf.append(getShortDetails(ch));
            }
            CacheHolderDetail det = ch.getDetails();
            if ((!det.getHints().equals("null")) && (det.getHints().length() > 0)) {
                strBuf.append(":");
                strBuf.append(simplifyString(Common.rot13(det.getHints())));
            }
        }
        strBuf.append("]]></name>\r\n   <coord lat=\"");
        strBuf.append(ch.getWpt().getLatDeg(TransformCoordinates.DD));
        strBuf.append("\" lon=\"");
        strBuf.append(ch.getWpt().getLonDeg(TransformCoordinates.DD));
        strBuf.append("\"/>\r\n   <type>");
        strBuf.append(gm.getIcon(ch));
        strBuf.append("</type>\r\n</waypoint>\r\n");
        return strBuf.toString();
    }

    public String trailer() {
        return "</loc>\r\n";
    }
}
