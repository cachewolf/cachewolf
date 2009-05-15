package CacheWolf;

/**
 * Handels all aspects of converting cache type information from
 * and to the various im- and exporters as well as for converting
 * legavy profiles to current standard
 */
public final class CacheType {
	
	/** custom waypoint */
	public static final byte CW_TYPE_CUSTOM = 0;
	/** traditional cache (GC,OC) */
	public static final byte CW_TYPE_TRADITIONAL = 2;
	/** multi cache (GC,OC) */
	public static final byte CW_TYPE_MULTI = 3;
	/** virtual cache (GC) */
	public static final byte CW_TYPE_VIRTUAL = 4;
	/** letterbox cache (GC) */
	public static final byte CW_TYPE_LETTERBOX = 5;
	/** event cache (OC,GC) */
	public static final byte CW_TYPE_EVENT = 6;
	/** quiz cache (OC) */
	public static final byte CW_TYPE_QUIZ = 7;
	/** unknown cache (GC) */
	public static final byte CW_TYPE_UNKNOWN = 8;
	/** math cache (OC) */
	public static final byte CW_TYPE_MATH = 108;
	/** moving cache (OC) */
	public static final byte CW_TYPE_MOVING = 9;
	/** drive in cache (OC) */
	public static final byte CW_TYPE_DRIVE_IN = 10;
	/** webcam cache (GC,OC) */
	public static final byte CW_TYPE_WEBCAM = 11;
	/** locationless cache (GC) */
	public static final byte CW_TYPE_LOCATIONLESS = 12;
	/** CITO cache (GC,OC)*/
	public static final byte CW_TYPE_CITO = 13;
	/** Additional Waypoint Parking (GC) */
	public static final byte CW_TYPE_PARKING = 50;
	/** Additional Waypoint Stage of a Multi (GC) */
	public static final byte CW_TYPE_STAGE = 51;
	/** Additional Waypoint Question to answer (GC) */
	public static final byte CW_TYPE_QUESTION = 52;
	/** Additional Waypoint Final (GC) */
	public static final byte CW_TYPE_FINAL = 53;
	/** Additional Waypoint Trailhead (GC) */
	public static final byte CW_TYPE_TRAILHEAD = 54;
	/** Additional Waypoint Reference (GC) */
	public static final byte CW_TYPE_REFERENCE = 55;
	/** Mega Event Cache (GC) */
	public static final byte CW_TYPE_MEGA_EVENT = 100;
	/** WhereIGo Cache (GC) */
	public static final byte CW_TYPE_WHEREIGO = 101;
	/** Project Ape cache (GC)*/
	public static final byte CW_TYPE_APE = 102;
	/** Adenture Maze Exhibit (GC)*/
	public static final byte CW_TYPE_MAZE = 103;
	/** Earth Cache (GC) */
	public static final byte CW_TYPE_EARTH = 104;
	/** unparsable cache type or missing information, should throw IllegalArgumentExceptions when found */
	public static final byte CW_TYPE_ERROR = -1;
	
	/** image for custom waypoints */
	public static final String CW_GUIIMG_CUSTOM = "0.png";
	/** image for traditional cache (GC,OC) */
	public static final String CW_GUIIMG_TRADITIONAL = "2.png";
	/** image for multi cache (GC,OC) */
	public static final String CW_GUIIMG_MULTI = "3.png";
	/** image for virtual cache (GC) */
	public static final String CW_GUIIMG_VIRTUAL = "4.png";
	/** image for letterbox cache (GC) */
	public static final String CW_GUIIMG_LETTERBOX = "5.png";
	/** image for event cache (OC,GC) */
	public static final String CW_GUIIMG_EVENT = "6.png";
	/** image for quiz cache (OC) */
	public static final String CW_GUIIMG_QUIZ = "8.png";
	/** image for unknown cache (GC) */
	public static final String CW_GUIIMG_UNKNOWN = "8.png";
	/** image for math cache (OC) */
	public static final String CW_GUIIMG_MATH = "108.png";
	/** image for moving cache (OC) */
	public static final String CW_GUIIMG_MOVING = "109.png";
	/** image for drive in cache (OC) */
	public static final String CW_GUIIMG_DRIVE_IN = "110.png";
	/** image for webcam cache (GC,OC) */
	public static final String CW_GUIIMG_WEBCAM = "11.png";
	/** image for locationless cache (GC) */
	public static final String CW_GUIIMG_LOCATIONLESS = "12.png";
	/** image for CITO cache (GC,OC)*/
	public static final String CW_GUIIMG_CITO = "13.png";
	/** image for Additional Waypoint Parking (GC) */
	public static final String CW_GUIIMG_PARKING = "pkg.png";
	/** image for Additional Waypoint Stage of a Multi (GC) */
	public static final String CW_GUIIMG_STAGE = "stage.png";
	/** image for Additional Waypoint Question to answer (GC) */
	public static final String CW_GUIIMG_QUESTION = "puzzle.png";
	/** image for Additional Waypoint Final (GC) */
	public static final String CW_GUIIMG_FINAL = "flag.png";
	/** image for Additional Waypoint Trailhead (GC) */
	public static final String CW_GUIIMG_TRAILHEAD = "trailhead.png";
	/** image for Additional Waypoint Reference Point (GC) */
	public static final String CW_GUIIMG_REFERENCE = "waypoint.png";
	/** image for Mega Event Cache (GC) */
	public static final String CW_GUIIMG_MEGA_EVENT = "453.png";
	/** image for WhereIGo Cache (GC) */
	public static final String CW_GUIIMG_WHEREIGO = "1858.png";
	/** image for Project Ape cache (GC)*/
	public static final String CW_GUIIMG_APE = "typeApe.png";
	/** image for Adenture Maze Exhibit (GC)*/
	public static final String CW_GUIIMG_MAZE = "typeMaze.png";
	/** image for Earth Cache (GC) */
	public static final String CW_GUIIMG_EARTH = "137.png";
	
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_CUSTOM = "Custom";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_TRADI = "Tradi";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_MULTI = "Multi";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_VIRTUAL = "Virtual";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_LETTERBOX = "Letterbox";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_EVENT = "Event";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_MEGAEVENT = "Mega Event";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_WEBCAM = "Webcam";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_UNKNOWN = "Mystery";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_LOCATIONLESS = "Locationless";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_CITO = "CITO";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_EARTH = "Earthcache";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_WHEREIGO = "WherIGo";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_PARKING = "Addi: Parking";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_STAGE = "Addi: Stage";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_QUESTION = "Addi: Question";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_FINAL = "Addi: Final";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_TRAILHEAD = "Addi: Trailhead";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_REFERENCE = "Addi: Reference";
	
	public static final String GC_GPX_TRADITIONAL = "Traditional Cache";
	public static final String GC_GPX_MULTI = "Multi-cache";
	public static final String GC_GPX_VIRTUAL = "Virtual Cache";
	public static final String GC_GPX_LETTERBOX = "Letterbox Hybrid";
	public static final String GC_GPX_EVENT = "Event Cache";
	public static final String GC_GPX_UNKNOWN = "Unknown Cache";
	public static final String GC_GPX_WEBCAM = "Webcam Cache";
	public static final String GC_GPX_LOCATIONLESS = "Locationless (Reverse) Cache";
	public static final String GC_GPX_CITO = "Cache In Trash Out Event";
	public static final String GC_GPX_EARTH = "Earthcache";
	public static final String GC_GPX_MEGA_EVENT = "Mega-Event Cache";
	public static final String GC_GPX_WHEREIGO = "Wherigo Cache";
	public static final String GC_GPX_PARKING = "Waypoint|Parking Area";
	public static final String GC_GPX_STAGE = "Waypoint|Stages of a Multicache";
	public static final String GC_GPX_QUESTION = "Waypoint|Question to Answer";
	public static final String GC_GPX_FINAL = "Waypoint|Final Coordinates";
	public static final String GC_GPX_TRAILHEAD = "Waypoint|Trailhead";
	public static final String GC_GPX_REFERENCE = "Waypoint|Reference Point";
	public static final String GC_GPX_MAZE = "FIXME"; //FIXME: insert right string
	public static final String GC_GPX_APE = "FIXME"; //FIXME: insert right string
	
	/**  constructor does nothing */
	public CacheType() {

	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int cwTypeId2GuiTypeId(byte id) throws IllegalArgumentException {
		throw new IllegalArgumentException("unmatched argument "+id+" in CacheSizeNew cwTypeId2GuiTypeId()");
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static String getExportShortId(byte type) throws IllegalArgumentException {
		switch (type){
		case CW_TYPE_CUSTOM: return "C";
		case CW_TYPE_TRADITIONAL: return "T";
		case CW_TYPE_MULTI: return "M";
		case CW_TYPE_VIRTUAL: return "V";
		case CW_TYPE_LETTERBOX: return "L";
		case CW_TYPE_EVENT: return "X";
		case CW_TYPE_WEBCAM: return "W";
		case CW_TYPE_UNKNOWN: return "U";
		case CW_TYPE_LOCATIONLESS: return "O";
		case CW_TYPE_CITO: return "X";
		case CW_TYPE_EARTH: return "E";
		case CW_TYPE_MEGA_EVENT: return "X";
		case CW_TYPE_WHEREIGO: return "G";
		case CW_TYPE_PARKING: return "P";
		case CW_TYPE_STAGE: return "S";
		case CW_TYPE_QUESTION: return "Q";
		case CW_TYPE_FINAL: return "F";
		case CW_TYPE_TRAILHEAD: return "H";
		case CW_TYPE_REFERENCE: return "R";
		case CW_TYPE_APE: return "T";
		case CW_TYPE_MAZE: return "X";
		default: throw new IllegalArgumentException("unmatched argument "+type+" in CacheSizeNew getExportShortId()");
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws IllegalArgumentException if size can not be mapped to internal representation
	 * @deprecated remove once v1 file version compatibility is abandoned
	 */
	public static final byte v1Converter(String type) throws IllegalArgumentException  {
		throw new IllegalArgumentException("unmatched argument "+type+" in CacheSizeNew v1Converter()");
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws IllegalArgumentException if size can not be mapped to internal representation
	 * @deprecated remove once v2 file version compatibility is abandoned
	 */
	public static final byte v2Converter(byte type) throws IllegalArgumentException  {
		switch (type) {
		default: throw new IllegalArgumentException("unmatched argument "+type+" in CacheSizeNew v2Converter()");
		}
	}
	
	/**
	 * check if a given waypoint type is an additional waypoint
	 * @param type waypoint type to check 
	 * @return true if it is an addition waypint, false otherwise
	 */
	public static final boolean isAddiWpt(byte type) {
		switch (type) {
		case CW_TYPE_PARKING: // fall through
		case CW_TYPE_STAGE: // fall through
		case CW_TYPE_QUESTION: // fall through
		case CW_TYPE_FINAL: // fall through
		case CW_TYPE_TRAILHEAD: // fall through
		case CW_TYPE_REFERENCE: return true;
		default: return false;
		}
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String[] guiTypeStrings() {
		String ret[] = new String[] {
				CW_GUISTR_CUSTOM,
				CW_GUISTR_TRADI,
				CW_GUISTR_MULTI,
				CW_GUISTR_VIRTUAL,
				CW_GUISTR_LETTERBOX,
				CW_GUISTR_EVENT,
				CW_GUISTR_MEGAEVENT,
				CW_GUISTR_WEBCAM,
				CW_GUISTR_UNKNOWN,
				CW_GUISTR_LOCATIONLESS,
				CW_GUISTR_CITO,
				CW_GUISTR_EARTH,
				CW_GUISTR_WHEREIGO,
				CW_GUISTR_PARKING,
				CW_GUISTR_STAGE,
				CW_GUISTR_QUESTION,
				CW_GUISTR_FINAL,
				CW_GUISTR_TRAILHEAD,
				CW_GUISTR_REFERENCE
			};
		return ret;
	}
	
	/**
	 * 
	 * @param selection
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final byte guiSelect2Cw(int selection) throws IllegalArgumentException {
		// make sure to refelect the order of guiTypeStrings()
		switch (selection) {
		case  0: return CW_TYPE_CUSTOM;
		case  1: return CW_TYPE_TRADITIONAL;
		case  2: return CW_TYPE_MULTI;
		case  3: return CW_TYPE_VIRTUAL;
		case  4: return CW_TYPE_LETTERBOX;
		case  5: return CW_TYPE_EVENT;
		case  6: return CW_TYPE_MEGA_EVENT;
		case  7: return CW_TYPE_WEBCAM;
		case  8: return CW_TYPE_UNKNOWN;
		case  9: return CW_TYPE_LOCATIONLESS;
		case 10: return CW_TYPE_CITO;
		case 11: return CW_TYPE_EARTH;
		case 12: return CW_TYPE_WHEREIGO;
		case 13: return CW_TYPE_PARKING;
		case 14: return CW_TYPE_STAGE;
		case 15: return CW_TYPE_QUESTION;
		case 16: return CW_TYPE_FINAL;
		case 17: return CW_TYPE_TRAILHEAD;
		case 18: return CW_TYPE_REFERENCE;
		default: throw new IllegalArgumentException("unmatched argument "+selection+" in CacheSize guiSelect2Cw()");
		}
	}
	
	public static final int cw2GuiSelect(byte id) throws IllegalArgumentException {
		switch (id) {
		case  CW_TYPE_CUSTOM: return 0;
		case  CW_TYPE_TRADITIONAL: return 1;
		case  CW_TYPE_MULTI: return 2;
		case  CW_TYPE_VIRTUAL: return 3;
		case  CW_TYPE_LETTERBOX: return 4;
		case  CW_TYPE_EVENT: return 5;
		case  CW_TYPE_MEGA_EVENT: return 6;
		case  CW_TYPE_WEBCAM: return 7;
		case  CW_TYPE_UNKNOWN: return 8;
		case  CW_TYPE_LOCATIONLESS: return 9;
		case CW_TYPE_CITO: return 10;
		case CW_TYPE_EARTH: return 11;
		case CW_TYPE_WHEREIGO: return 12;
		case CW_TYPE_PARKING: return 13;
		case CW_TYPE_STAGE: return 14;
		case CW_TYPE_QUESTION: return 15;
		case CW_TYPE_FINAL: return 16;
		case CW_TYPE_TRAILHEAD: return 17;
		case CW_TYPE_REFERENCE: return 18;
		default: throw new IllegalArgumentException("unmatched argument "+id+" in CacheSize cw2GuiSelect()");
		}
	}
	
	/**
	 * 
	 * @param gpxType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final byte gpxType2CwType(String gpxType) throws IllegalArgumentException {
		// TODO: add ape
		// TODO: add maze
		if (gpxType.equals(GC_GPX_TRADITIONAL) || gpxType.equals("Traditional")|| gpxType.equals("Classic")) return CW_TYPE_TRADITIONAL;
		if (gpxType.equals(GC_GPX_MULTI) || gpxType.equals("Multi") || gpxType.equals("Offset")) return CW_TYPE_MULTI;
		if (gpxType.equals(GC_GPX_VIRTUAL) || gpxType.equals("Virtual")) return CW_TYPE_VIRTUAL;
		if (gpxType.equals(GC_GPX_LETTERBOX)) return CW_TYPE_LETTERBOX;
		if (gpxType.equals(GC_GPX_EVENT) || gpxType.equals("Event")) return CW_TYPE_EVENT;
		if (gpxType.equals(GC_GPX_UNKNOWN) || gpxType.equals("Other") || gpxType.equals("Quiz")) return CW_TYPE_UNKNOWN;
		if (gpxType.equals(GC_GPX_WEBCAM) || gpxType.equals("Webcam")) return CW_TYPE_WEBCAM;
		if (gpxType.equals(GC_GPX_LOCATIONLESS)) return CW_TYPE_LOCATIONLESS;
		if (gpxType.equals(GC_GPX_CITO)) return CW_TYPE_CITO;
		if (gpxType.equals(GC_GPX_EARTH) || gpxType.equals("Earth")) return CW_TYPE_EARTH;
		if (gpxType.equals(GC_GPX_MEGA_EVENT)) return CW_TYPE_MEGA_EVENT;
		if (gpxType.equals(GC_GPX_WHEREIGO)) return CW_TYPE_WHEREIGO;
		if (gpxType.equals(GC_GPX_PARKING)) return CW_TYPE_PARKING;
		if (gpxType.equals(GC_GPX_STAGE)) return CW_TYPE_STAGE;
		if (gpxType.equals(GC_GPX_QUESTION)) return CW_TYPE_QUESTION;
		if (gpxType.equals(GC_GPX_FINAL)||gpxType.equals("Waypoint|Final Location")) return CW_TYPE_FINAL;
		if (gpxType.equals(GC_GPX_TRAILHEAD)) return CW_TYPE_TRAILHEAD;
		if (gpxType.equals(GC_GPX_REFERENCE)) return CW_TYPE_REFERENCE;
		if (gpxType.equals(GC_GPX_MAZE)) return CW_TYPE_MAZE;
		if (gpxType.equals(GC_GPX_APE)) return CW_TYPE_APE;
		throw new IllegalArgumentException("unmatched argument "+gpxType+" in CacheSize gpxType2CwType()");
	}
	
	/**
	 * 
	 * @param ocType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final byte ocType2CwType(String ocType) throws IllegalArgumentException {
		if(ocType.equals("1")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("2")) return CW_TYPE_TRADITIONAL;
		if(ocType.equals("3")) return CW_TYPE_MULTI;	
		if(ocType.equals("4")) return CW_TYPE_VIRTUAL;
		if(ocType.equals("5")) return CW_TYPE_WEBCAM;
		if(ocType.equals("6")) return CW_TYPE_EVENT;
		if(ocType.equals("7")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("8")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("9")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("10")) return CW_TYPE_UNKNOWN;
		throw new IllegalArgumentException("unmatched argument "+ocType+" in CacheSize ocType2CwType()");
	}
	
	/**
	 * 
	 * @param gcType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final byte gcSpider2CwType(String gcType) throws IllegalArgumentException {
		if (gcType.equals("2")) { return CW_TYPE_TRADITIONAL; }
		if (gcType.equals("3")) { return CW_TYPE_MULTI; }
		if (gcType.equals("4")) { return CW_TYPE_VIRTUAL; }
		if (gcType.equals("5")) { return CW_TYPE_LETTERBOX; }
		if (gcType.equals("6")) { return CW_TYPE_EVENT; }
		if (gcType.equals("8")) { return CW_TYPE_UNKNOWN; }
		if (gcType.equals("9")) { return CW_TYPE_APE; }
		if (gcType.equals("11")) { return CW_TYPE_WEBCAM; }
		if (gcType.equals("12")) { return CW_TYPE_LOCATIONLESS; }
		if (gcType.equals("13")) { return CW_TYPE_CITO; }
		if (gcType.equals("137")) { return CW_TYPE_EARTH; }
		if (gcType.equals("453")) { return CW_TYPE_MEGA_EVENT; }
		if (gcType.equals("1858")) { return CW_TYPE_WHEREIGO; }
		if (gcType.equals("1304")) { return CW_TYPE_MAZE; }
		throw new IllegalArgumentException("unmatched argument "+gcType+" in CacheSize gcSpider2CwType()");
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final String typeImageForId(byte id) throws IllegalArgumentException {
		switch (id) {
		case CW_TYPE_CUSTOM: return "CW_GUIIMG_CUSTOM";
		case CW_TYPE_TRADITIONAL: return CW_GUIIMG_TRADITIONAL;
		case CW_TYPE_MULTI: return CW_GUIIMG_MULTI;
		case CW_TYPE_VIRTUAL: return CW_GUIIMG_VIRTUAL;
		case CW_TYPE_LETTERBOX: return CW_GUIIMG_LETTERBOX;
		case CW_TYPE_EVENT: return CW_GUIIMG_EVENT;
		case CW_TYPE_WEBCAM: return CW_GUIIMG_WEBCAM;
		case CW_TYPE_UNKNOWN: return CW_GUIIMG_UNKNOWN;
		case CW_TYPE_LOCATIONLESS: return CW_GUIIMG_LOCATIONLESS;
		case CW_TYPE_CITO: return CW_GUIIMG_CITO;
		case CW_TYPE_EARTH: return CW_GUIIMG_EARTH;
		case CW_TYPE_MEGA_EVENT: return CW_GUIIMG_MEGA_EVENT;
		case CW_TYPE_WHEREIGO: return CW_GUIIMG_WHEREIGO;
		case CW_TYPE_PARKING: return CW_GUIIMG_PARKING;
		case CW_TYPE_STAGE: return CW_GUIIMG_STAGE;
		case CW_TYPE_QUESTION: return CW_GUIIMG_QUESTION;
		case CW_TYPE_FINAL: return CW_GUIIMG_FINAL;
		case CW_TYPE_TRAILHEAD: return CW_GUIIMG_TRAILHEAD;
		case CW_TYPE_REFERENCE: return CW_GUIIMG_REFERENCE;
		case CW_TYPE_APE: return CW_GUIIMG_APE;
		case CW_TYPE_MAZE: return CW_GUIIMG_MAZE;
		default: throw new IllegalArgumentException("unmatched argument "+id+" in CacheSize typeImageForId()");
		}
	}
	
	public static final String id2GpxString(byte id) throws IllegalArgumentException {
		switch (id) {
		case CW_TYPE_TRADITIONAL: return GC_GPX_TRADITIONAL;
		case CW_TYPE_MULTI: return GC_GPX_MULTI;
		case CW_TYPE_VIRTUAL: return GC_GPX_VIRTUAL;
		case CW_TYPE_LETTERBOX: return GC_GPX_LETTERBOX;
		case CW_TYPE_EVENT: return GC_GPX_EVENT;
		case CW_TYPE_UNKNOWN: return GC_GPX_UNKNOWN;
		case CW_TYPE_WEBCAM: return GC_GPX_WEBCAM;
		case CW_TYPE_LOCATIONLESS: return GC_GPX_LOCATIONLESS;
		case CW_TYPE_CITO: return GC_GPX_CITO;
		case CW_TYPE_EARTH: return GC_GPX_EARTH;
		case CW_TYPE_MEGA_EVENT: return GC_GPX_MEGA_EVENT;
		case CW_TYPE_WHEREIGO: return GC_GPX_WHEREIGO;
		case CW_TYPE_PARKING: return GC_GPX_PARKING;
		case CW_TYPE_STAGE: return GC_GPX_STAGE;
		case CW_TYPE_QUESTION: return GC_GPX_QUESTION;
		case CW_TYPE_FINAL: return GC_GPX_FINAL;
		case CW_TYPE_TRAILHEAD: return GC_GPX_TRAILHEAD;
		case CW_TYPE_REFERENCE: return GC_GPX_REFERENCE;
		case CW_TYPE_MAZE: return GC_GPX_MAZE;
		case CW_TYPE_APE: return GC_GPX_APE;
		default: throw new IllegalArgumentException("unmatched argument "+id+" in CacheSize id2GpxString()");
		}
		
	}
	
	//TODO: de we actually need this one
	public static final String cw2ExportString(byte id) throws IllegalArgumentException {
		String ret;
		try {
			ret = id2GpxString(id);
			// check for | in additional waypoints and only use the string after |
			int pipePosistion = ret.indexOf("|");
			if (pipePosistion > -1) {
				ret = ret.substring(pipePosistion);
			}
		} catch (IllegalArgumentException ex) {
			ret = "";
		}
		return ret;
	}
	
	// cache to image
	
}
