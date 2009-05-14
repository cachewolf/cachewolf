package CacheWolf;

public final class CacheType {
	
	/** custom waypoint */
	static protected final byte CW_TYPE_CUSTOM = 0;
	/** traditional cache (GC,OC) */
	static protected final byte CW_TYPE_TRADITIONAL = 2;
	/** multi cache (GC,OC) */
	static protected final byte CW_TYPE_MULTI = 3;
	/** virtual cache (GC) */
	static protected final byte CW_TYPE_VIRTUAL = 4;
	/** letterbox cache (GC) */
	static protected final byte CW_TYPE_LETTERBOX = 5;
	/** event cache (OC,GC) */
	static protected final byte CW_TYPE_EVENT = 6;
	/** quiz cache (OC) */
	static protected final byte CW_TYPE_QUIZ = 7;
	/** unknown cache (GC) */
	static protected final byte CW_TYPE_UNKNOWN = 8;
	/** math cache (OC) */
	static protected final byte CW_TYPE_MATH = 108;
	/** moving cache (OC) */
	static protected final byte CW_TYPE_MOVING = 9;
	/** drive in cache (OC) */
	static protected final byte CW_TYPE_DRIVE_IN = 10;
	/** webcam cache (GC,OC) */
	static protected final byte CW_TYPE_WEBCAM = 11;
	/** locationless cache (GC) */
	static protected final byte CW_TYPE_LOCATIONLESS = 12;
	/** CITO cache (GC,OC)*/
	static protected final byte CW_TYPE_CITO = 13;
	/** Additional Waypoint Parking (GC) */
	static protected final byte CW_TYPE_PARKING = 50;
	/** Additional Waypoint Stage of a Multi (GC) */
	static protected final byte CW_TYPE_STAGE = 51;
	/** Additional Waypoint Question to answer (GC) */
	static protected final byte CW_TYPE_QUESTION = 52;
	/** Additional Waypoint Final (GC) */
	static protected final byte CW_TYPE_FINAL = 53;
	/** Additional Waypoint Trailhead (GC) */
	static protected final byte CW_TYPE_TRAILHEAD = 54;
	/** Additional Waypoint Reference (GC) */
	static protected final byte CW_TYPE_REFERENCE = 55;
	/** Mega Event Cache (GC) */
	static protected final byte CW_TYPE_MEGA_EVENT = 100;
	/** WhereIGo Cache (GC) */
	static protected final byte CW_TYPE_WHEREIGO = 101;
	/** Project Ape cache (GC)*/
	static protected final byte CW_TYPE_APE = 102;
	/** Adenture Maze Exhibit (GC)*/
	static protected final byte CW_TYPE_MAZE = 103;
	/** Earth Cache (GC) */
	static protected final byte CW_TYPE_EARTH = 104;
	/** unparsable cache type or missing information, should throw IllegalArgumentExceptions when found */
	static protected final byte CW_TYPE_ERROR = -1;
	
	/** image for traditional cache (GC,OC) */
	static protected final String CW_GUIIMG_TRADITIONAL = "2.png";
	/** image for multi cache (GC,OC) */
	static protected final String CW_GUIIMG_MULTI = "3.png";
	/** image for virtual cache (GC) */
	static protected final String CW_GUIIMG_VIRTUAL = "4.png";
	/** image for letterbox cache (GC) */
	static protected final String CW_GUIIMG_LETTERBOX = "5.png";
	/** image for event cache (OC,GC) */
	static protected final String CW_GUIIMG_EVENT = "6.png";
	/** image for quiz cache (OC) */
	static protected final String CW_GUIIMG_QUIZ = "8.png";
	/** image for unknown cache (GC) */
	static protected final String CW_GUIIMG_UNKNOWN = "8.png";
	/** image for math cache (OC) */
	static protected final String CW_GUIIMG_MATH = "108.png";
	/** image for moving cache (OC) */
	static protected final String CW_GUIIMG_MOVING = "109.png";
	/** image for drive in cache (OC) */
	static protected final String CW_GUIIMG_DRIVE_IN = "110.png";
	/** image for webcam cache (GC,OC) */
	static protected final String CW_GUIIMG_WEBCAM = "11.png";
	/** image for locationless cache (GC) */
	static protected final String CW_GUIIMG_LOCATIONLESS = "12.png";
	/** image for CITO cache (GC,OC)*/
	static protected final String CW_GUIIMG_CITO = "13.png";
	/** image for Additional Waypoint Parking (GC) */
	static protected final String CW_GUIIMG_PARKING = "pkg.png";
	/** image for Additional Waypoint Stage of a Multi (GC) */
	static protected final String CW_GUIIMG_STAGE = "stage.png";
	/** image for Additional Waypoint Question to answer (GC) */
	static protected final String CW_GUIIMG_QUESTION = "puzzle.png";
	/** image for Additional Waypoint Final (GC) */
	static protected final String CW_GUIIMG_FINAL = "flag.png";
	/** image for Additional Waypoint Trailhead (GC) */
	static protected final String CW_GUIIMG_TRAILHEAD = "trailhead.png";
	/** image for Additional Waypoint Reference Point (GC) */
	static protected final String CW_GUIIMG_REFERENCE = "waypoint.png";
	/** image for Mega Event Cache (GC) */
	static protected final String CW_GUIIMG_MEGA_EVENT = "453.png";
	/** image for WhereIGo Cache (GC) */
	static protected final String CW_GUIIMG_WHEREIGO = "1858.png";
	/** image for Project Ape cache (GC)*/
	static protected final String CW_GUIIMG_APE = "typeApe.png";
	/** image for Adenture Maze Exhibit (GC)*/
	static protected final String CW_GUIIMG_MAZE = "typeMaze.png";
	/** image for Earth Cache (GC) */
	static protected final String CW_GUIIMG_EARTH = "137.png";
	
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_CUSTOM = "Custom";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_TRADI = "Tradi";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_MULTI = "Multi";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_VIRTUAL = "Virtual";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_LETTERBOX = "Letterbox";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_EVENT = "Event";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_MEGAEVENT = "Mega Event";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_WEBCAM = "Webcam";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_UNKNOWN = "Mystery";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_LOCATIONLESS = "Locationless";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_CITO = "CITO";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_EARTH = "Earthcache";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_WHEREIGO = "WherIGo";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_PARKING = "Addi: Parking";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_STAGE = "Addi: Stage";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_QUESTION = "Addi: Question";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_FINAL = "Addi: Final";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_TRAILHEAD = "Addi: Trailhead";
	/** GUI string for custom waypoit */
	static protected final String CW_GUISTR_REFERENCE = "Addi: Reference";
	
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
	 * @param size
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static String getExportShortId(byte size) throws IllegalArgumentException {
		throw new IllegalArgumentException("unmatched argument "+size+" in CacheSizeNew getExportShortId()");
	}
	
	/**
	 * 
	 * @param size
	 * @return
	 * @throws IllegalArgumentException if size can not be mapped to internal representation
	 * @deprecated remove once v1 file version compatibility is abandoned
	 */
	public static final byte v1Converter(String size) throws IllegalArgumentException  {
		throw new IllegalArgumentException("unmatched argument "+size+" in CacheSizeNew v1Converter()");
	}

	/**
	 * 
	 * @param size
	 * @return
	 * @throws IllegalArgumentException if size can not be mapped to internal representation
	 * @deprecated remove once v2 file version compatibility is abandoned
	 */
	public static final byte v2Converter(byte size) throws IllegalArgumentException  {
		throw new IllegalArgumentException("unmatched argument "+size+" in CacheSizeNew v2Converter()");
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
	
	/**
	 * 
	 * @param gpxType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final byte gpxType2CwType(String gpxType) throws IllegalArgumentException {
		if (gpxType.equals("Traditional Cache") || gpxType.equals("Traditional")|| gpxType.equals("Classic")) return CW_TYPE_TRADITIONAL;
		if (gpxType.equals("Multi-cache") || gpxType.equals("Multi") || gpxType.equals("Offset")) return CW_TYPE_MULTI;
		if (gpxType.equals("Virtual Cache") || gpxType.equals("Virtual")) return CW_TYPE_VIRTUAL;
		if (gpxType.equals("Letterbox Hybrid")) return CW_TYPE_LETTERBOX;
		if (gpxType.equals("Event Cache") || gpxType.equals("Event")) return CW_TYPE_EVENT;
		if (gpxType.equals("Unknown Cache") || gpxType.equals("Other") || gpxType.equals("Quiz")) return CW_TYPE_UNKNOWN;
		if (gpxType.equals("Webcam Cache") || gpxType.equals("Webcam")) return CW_TYPE_WEBCAM;
		if (gpxType.equals("Locationless (Reverse) Cache")) return CW_TYPE_LOCATIONLESS;
		if (gpxType.equals("Cache In Trash Out Event")) return CW_TYPE_CITO;
		if (gpxType.equals("Earthcache") || gpxType.equals("Earth")) return CW_TYPE_EARTH;
		if (gpxType.equals("Mega-Event Cache")) return CW_TYPE_MEGA_EVENT;
		if (gpxType.equals("Wherigo Cache")) return CW_TYPE_WHEREIGO;
		if (gpxType.equals("Waypoint|Parking Area")) return CW_TYPE_PARKING;
		if (gpxType.equals("Waypoint|Stages of a Multicache")) return CW_TYPE_STAGE;
		if (gpxType.equals("Waypoint|Question to Answer")) return CW_TYPE_QUESTION;
		if (gpxType.equals("Waypoint|Final Coordinates")||gpxType.equals("Waypoint|Final Location")) return CW_TYPE_FINAL;
		if (gpxType.equals("Waypoint|Trailhead")) return CW_TYPE_TRAILHEAD;
		if (gpxType.equals("Waypoint|Reference Point")) return CW_TYPE_REFERENCE;
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
	
	// cache to image
	
}
