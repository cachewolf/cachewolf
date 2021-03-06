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

import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;

/**
 * Class to export the cache database (index) to an CSV File which can bei easy
 * importet bei MS AutoRoute (testet with AR 2001 German) Format of the file:
 * Name;Breitengrad;Lšngengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink
 */
public class MSARCSVExporter extends Exporter {

    public MSARCSVExporter() {
        super();
        this.setOutputFileExtension("*.csv");
        this.decimalSeparator = ',';
        this.setExportMethod(EXPORT_METHOD_LAT_LON);
    }

    public String header() {
        return "Name;Breitengrad;L\u00E4ngengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink\r";
    }

    public String record(CacheHolder ch, String lat, String lon) {
        StringBuffer str = new StringBuffer(200);
        str.append("\"" + ch.getCode() + " - " + ch.getName() + "\";");
        str.append(lat + ";" + lon + ";");
        str.append("\"" + CacheType.type2SymTag(ch.getType()) + "\";");
        str.append("\"" + CacheSize.cw2ExportString(ch.getSize()) + "\";");
        str.append("\"" + ch.getCode() + "\";");
        str.append("\"" + ch.getHidden() + "\";");
        str.append("\"" + ch.getDetails().getURL() + "\"\r\n");

        return str.toString();
    }
}
