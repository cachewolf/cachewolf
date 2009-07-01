package exp;

import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.Global;
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
class GarminMap extends MinML {

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
					atts.getValue("status"),atts.getValue("poiid"),atts.getValue("ozicolor")));
		}
	}

	public String getIcon(CacheHolder ch) {
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
			if (match) return icon.name;
		}

		// If it is not a mapped type, just use the standard mapping
		if (ch.is_found())
			return "Geocache Found";
		else
			return "Geocache";
	}
	
	public String getPoiId(CacheHolder ch) {
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
		return null;
	}
	
	public String ozicolor(CacheHolder ch) {
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
		return null;
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
