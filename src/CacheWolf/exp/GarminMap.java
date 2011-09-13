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

import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.Global;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.util.Vector;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * This class implements user defined icons which depend on the cache type and the found status.
 * See also http://www.geoclub.de/ftopic10413.html
 * @author salzkammergut
 *
 */
public class GarminMap extends MinML {
	private Vector symbols=new Vector(24);
	String lastName;
	public boolean exists=false;
	public GarminMap(){
		try{
			String datei = FileBase.getProgramDirectory() + "/exporticons/garminmap.xml"; //own version
			if (!new File(datei).exists()) {
				datei=FileBase.getProgramDirectory() + "/exporticons/exporticons/garminmap.xml"; //cw default version
				if (!new File(datei).exists()) {
					return;
				}
			}
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(datei));
			parse(r);
			r.close();
			exists=true;
		}catch(Exception e){
			exists=false;
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
					atts.getValue("status"),atts.getValue("poiid"),atts.getValue("ozicolor")));
		}
	}
	public String getIcon(CacheHolder ch) {
		if (exists) {
			int mapSize=symbols.size();
			// Try each icon in turn
			for (int i=0; i<mapSize; i++) {
				IconMap icon=(IconMap) symbols.get(i);
				boolean match=true;
				// If a certain attribute is not null it must match the current caches values
				match=match && ((icon.type==null) || icon.type.equals(String.valueOf(ch.getType())));
				match=match && ((icon.size==null) || ch.getCacheSize()==0 || icon.size.equalsIgnoreCase(CacheSize.getExportShortId(ch.getCacheSize())));
				match=match && ((icon.terrain==null) || ch.getTerrain()==0 || icon.terrain.equals(CacheTerrDiff.shortDT(ch.getTerrain())));
				match=match && ((icon.difficulty==null) ||  ch.getHard()==0 || icon.difficulty.equals(CacheTerrDiff.shortDT(ch.getHard())));
				match=match && ((icon.status==null) ||  ch.getCacheStatus().startsWith(icon.status));
				match=match && ((icon.found==null) || ch.is_found());
				if (match) return icon.name;
			}
		}

		// If it is not a mapped type, just use the standard mapping
		if (ch.is_found())
			return "Geocache Found";
		else
			return "Geocache";
	}
	public String getPoiId(CacheHolder ch) {
		if (exists) {
			int mapSize=symbols.size();
			// Try each icon in turn
			for (int i=0; i<mapSize; i++) {
				IconMap icon=(IconMap) symbols.get(i);
				boolean match=true;
				// If a certain attribute is not null it must match the current caches values
				match=match && ((icon.type==null) || ch.getType()==0 || icon.type.equals(String.valueOf(ch.getType())));
				match=match && ((icon.size==null) || ch.getCacheSize()==0 || icon.size.equalsIgnoreCase(CacheSize.getExportShortId(ch.getCacheSize())));
				match=match && ((icon.terrain==null) || ch.getTerrain()==0 || icon.terrain.equals(CacheTerrDiff.shortDT(ch.getTerrain())));
				match=match && ((icon.difficulty==null) ||  ch.getHard()==0 || icon.difficulty.equals(CacheTerrDiff.shortDT(ch.getHard())));
				match=match && ((icon.status==null) ||  ch.getCacheStatus().startsWith(icon.status));
				match=match && ((icon.found==null) || ch.is_found());
				if (match) return icon.poiId;
			}
		}
		return null;
	}
	public String ozicolor(CacheHolder ch) {
		if (exists) {
			int mapSize=symbols.size();
			// Try each icon in turn
			for (int i=0; i<mapSize; i++) {
				IconMap icon=(IconMap) symbols.get(i);
				boolean match=true;
				// If a certain attribute is not null it must match the current caches values
				match=match && ((icon.type==null) || ch.getType()==0 || icon.type.equals(String.valueOf(ch.getType())));
				match=match && ((icon.size==null) || ch.getCacheSize()==0 || icon.size.equalsIgnoreCase(CacheSize.getExportShortId(ch.getCacheSize())));
				match=match && ((icon.terrain==null) || ch.getTerrain()==0 || icon.terrain.equals(CacheTerrDiff.shortDT(ch.getTerrain())));
				match=match && ((icon.difficulty==null) ||  ch.getHard()==0 || icon.difficulty.equals(CacheTerrDiff.shortDT(ch.getHard())));
				match=match && ((icon.status==null) ||  ch.getCacheStatus().startsWith(icon.status));
				match=match && ((icon.found==null) || ch.is_found());
				if (match) return icon.ozicolor;
			}
		}
		return "16777215"; // default color
	}
	private class IconMap {
		public String type;
		public String name;
		public String size;
		public String terrain;
		public String difficulty;
		public String found;
		public String status;
		public String poiId;
		public String ozicolor;

		IconMap(String type, String name, String found, String size, String terrain, String difficulty, String status, String poiId, String ozicolor) {
			this.type=type;
			this.name=name;
			this.found=found;
			this.size=size;
			this.terrain=terrain;
			this.difficulty=difficulty;
			this.status=status;
			this.poiId = poiId;
			this.ozicolor = ozicolor;
		}
	}
}
