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
package CacheWolf.imp;

import CacheWolf.CWPoint;
import ewe.io.FileReader;
import ewe.io.Reader;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.util.StringTokenizer;
import ewe.util.Vector;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import coordinates from a KML file generated from
 * google earth.
 * it looks for <placemark><MultiGeometry><LineString><coordinates>
 * and gathers all coordinated in a vector
 * @author Bilbowolf
 *
 */
public class KMLImporter extends MinML {
	
	public Vector points = new Vector();
	private CWPoint point = new CWPoint();
	private String file = "";
	String strData = "";
	private int status = 0;
	private static int MultiGeometry = 1;
	private static int LineString = 2;
	private static int coordinates = 3;
	
	public KMLImporter(String file){
		this.file = file;
	}
	
	public void importFile() {
		try{
			Reader r;
			Vm.showWait(true);
			r = new FileReader(file);
			parse(r);
			r.close();
			Vm.showWait(false);
		}catch(Exception e){
			Vm.showWait(false);
		}
	}
	
	public Vector getPoints(){
		return points;
	}
	
	public void startElement(String name, AttributeList atts){
		strData = "";
		if(name.equals("MultiGeometry")) status = MultiGeometry;
		if(name.equals("LineString") && status == MultiGeometry) status = LineString;
		if(name.equals("coordinates") && status == LineString) status = coordinates;
	}
	
	public void endElement(String name){
		if(name.equals("coordinates") && status == coordinates){
			parseCoordinatesLine();
			//10.09052,49.78188000000001,0
		}
		if(name.equals("LineString") && status == coordinates) status = LineString;
		if(name.equals("MultiGeometry") && status == LineString) status = 0;
	}
	
	public void characters(char[] ch,int start,int length){
		String chars = new String(ch,start,length);
		strData += chars;
	}
	
	private void parseCoordinatesLine(){
		StringTokenizer exBlock = new StringTokenizer(strData, " ");
		StringTokenizer numbers;
		String lat = "";
		String lon = "";
		
		String test = "";
		
		while(exBlock.hasMoreTokens()){
			test = exBlock.nextToken();
			numbers = new StringTokenizer(test, ",");
			lon = numbers.nextToken();
			lat = numbers.nextToken();
			point = new CWPoint(Convert.parseDouble(lat),Convert.parseDouble(lon));
			points.add(point);
		}
	}
}