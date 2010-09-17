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
package CacheWolf;

import ewe.io.ByteArrayInputStream;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.util.ByteArray;
import ewe.util.Vector;
import ewesoft.xml.XMLDecoder;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.SAXException;

public class GeocoderOsm {

	private static final String geocoderUrl = "http://gazetteer.openstreetmap.org/namefinder/search.xml?max=1&find=";  

	public static Vector geocode(String address, Handle[] h) 
	throws SAXException, IOException, HandleStoppedException, InterruptedException {
		ByteArray answ = UrlFetcher.fetchByteArray((geocoderUrl+UrlFetcher.toUtf8Url(address)), null, h);
		XMLDecoder xmldec = new XMLDecoder();
		xmldec.parse(new InputStreamReader(new ByteArrayInputStream(answ), IO.JAVA_UTF8_CODEC));
		Vector erg = new Vector();
		if ( "searchresults".equalsIgnoreCase((String)xmldec.document.tag) ) {
			XMLElement xe, xe2;
			String desc, lat, lon;
			desc = null;
			CWPoint where = new CWPoint();
			if (xmldec.document != null && xmldec.document.subElements != null) {
				for (int i=0;  i < xmldec.document.subElements.size(); i++) {
					xe = (XMLElement) xmldec.document.subElements.elementAt(i);
					if (xe.tag.equalsIgnoreCase("named")) {
						lat = (String) xe.attributes.getPropertyValues("lat").get(0);
						lon = (String) xe.attributes.getPropertyValues("lon").get(0);
						where.set(Common.parseDouble(lat), Common.parseDouble(lon));
						for (int j = 0; j < xe.subElements.size(); j++) {
							xe2 = (XMLElement) xe.subElements.elementAt(j);
							if ( xe2.tag.equalsIgnoreCase("description")) { desc = xe2.text; break; }  
						}
						erg.add(new GeocodeAnswer(where, desc));
					}
				}
			}
		}
		return erg;
	}

}

class GeocodeAnswer {
	String foundname;
	CWPoint where;
	public GeocodeAnswer(CWPoint where_, String desc_) {
		where = new CWPoint(where_);
		foundname = desc_;
	}
}

