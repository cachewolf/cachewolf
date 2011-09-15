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

import CacheWolf.utils.FileBugfix;
import ewe.io.ByteArrayInputStream;
import ewe.io.File;
import ewe.io.FileInputStream;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.io.StringStream;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.util.ByteArray;
import ewe.util.Vector;
import ewesoft.xml.XMLDecoder;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.SAXException;

public class GeocoderOsm {

	//private static final String geocoderUrl = "http://gazetteer.openstreetmap.org/namefinder/search.xml?max=1&find=";
	private static final String geocoderUrl = "http://nominatim.openstreetmap.org/search?"; //q=135+pilkington+avenue,+birmingham&format=xml&polygon=1&addressdetails=1

	public static Vector geocode(String city, String street) throws Exception 
	{
		String searchFor;
		if (street.equals("")) {
			searchFor=UrlFetcher.toUtf8Url(city);
		}
		else {
			searchFor=UrlFetcher.toUtf8Url(street) + "+" + UrlFetcher.toUtf8Url(city);
		}
		String answer = UrlFetcher.fetch(geocoderUrl + "q=" + searchFor + "&format=xml");
		answer=STRreplace.replace(answer,"\'","\' ");
		answer=STRreplace.replace(answer,"  "," ");
		XMLDecoder xmldec = new XMLDecoder();
		Vector erg = new Vector();
		try {
			xmldec.parse(new InputStreamReader(new StringStream(answer)));
			if ( "searchresults".equalsIgnoreCase((String)xmldec.document.tag) ) {
				XMLElement xe;
				String desc, lat, lon;
				desc = null;
				CWPoint where = new CWPoint();
				if (xmldec.document != null && xmldec.document.subElements != null) {
					for (int i=0;  i < xmldec.document.subElements.size(); i++) {
						xe = (XMLElement) xmldec.document.subElements.elementAt(i);
						if (xe.tag.equalsIgnoreCase("place")) {
							lat = (String) xe.attributes.getPropertyValues("lat").get(0);
							lon = (String) xe.attributes.getPropertyValues("lon").get(0);
							where.set(Common.parseDouble(lat.trim()), Common.parseDouble(lon.trim()));
							desc = (String) xe.attributes.getPropertyValues("display_name").get(0);
							erg.add(new GeocodeAnswer(where, desc));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
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

