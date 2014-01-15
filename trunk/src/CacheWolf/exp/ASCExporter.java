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

/**
 * Class to export cache database to an ASCII (CSV!) file.
 * This file can be used by I2C's POI Converter to generate
 * POIs for different routing programmes, especially for
 * Destinator ;-) !
 */
public class ASCExporter extends Exporter {

	public ASCExporter() {
		super();
		this.setMask("*.csv");
		this.setHowManyParams(LAT_LON);
	}

	public String record(CacheHolder holder, String lat, String lon) {
		StringBuffer strBuf = new StringBuffer(100);
		String dummy;
		dummy = holder.getCacheName();
		dummy = dummy.replace(',', ' ');
		strBuf.append(dummy);
		strBuf.append(",");
		strBuf.append(dummy);
		strBuf.append(",");
		strBuf.append(lon);
		strBuf.append(",");
		strBuf.append(lat);
		strBuf.append(",,,,\r\n");
		return strBuf.toString();
	}
}
