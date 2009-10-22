package CacheWolf;

/**
 * Handles all aspects of converting cache type information from
 * and to the various im- and exporters as well as for converting
 * legacy profiles to current standard
 * 
 * Do not instantiate this class, only use it in a static way
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
	/** unrecognized cache type or missing information, should throw IllegalArgumentExceptions when found */
	public static final byte CW_TYPE_ERROR = -1;
	
	/** image for custom waypoints */
	public static final String CW_GUIIMG_CUSTOM = "typeCustom.png";
	/** image for traditional cache (GC,OC) */
	public static final String CW_GUIIMG_TRADITIONAL = "typeTradi.png";
	/** image for multi cache (GC,OC) */
	public static final String CW_GUIIMG_MULTI = "typeMulti.png";
	/** image for virtual cache (GC) */
	public static final String CW_GUIIMG_VIRTUAL = "typeVirtual.png";
	/** image for letterbox cache (GC) */
	public static final String CW_GUIIMG_LETTERBOX = "typeLetterbox.png";
	/** image for event cache (OC,GC) */
	public static final String CW_GUIIMG_EVENT = "typeEvent.png";
	/** image for quiz cache (OC) */
	public static final String CW_GUIIMG_QUIZ = "typeUnknown.png";
	/** image for unknown cache (GC) */
	public static final String CW_GUIIMG_UNKNOWN = "typeUnknown.png";
	/** image for math cache (OC) */
	public static final String CW_GUIIMG_MATH = "typeMath.png";
	/** image for moving cache (OC) */
	public static final String CW_GUIIMG_MOVING = "typeMoving.png";
	/** image for drive in cache (OC) */
	public static final String CW_GUIIMG_DRIVE_IN = "typeDrivein.png";
	/** image for webcam cache (GC,OC) */
	public static final String CW_GUIIMG_WEBCAM = "typeWebcam.png";
	/** image for locationless cache (GC) */
	public static final String CW_GUIIMG_LOCATIONLESS = "typeLocless.png";
	/** image for CITO cache (GC,OC)*/
	public static final String CW_GUIIMG_CITO = "typeCito.png";
	/** image for Additional Waypoint Parking (GC) */
	public static final String CW_GUIIMG_PARKING = "typeParking.png";
	/** image for Additional Waypoint Stage of a Multi (GC) */
	public static final String CW_GUIIMG_STAGE = "typeStage.png";
	/** image for Additional Waypoint Question to answer (GC) */
	public static final String CW_GUIIMG_QUESTION = "typeQuestion.png";
	/** image for Additional Waypoint Final (GC) */
	public static final String CW_GUIIMG_FINAL = "typeFinal.png";
	/** image for Additional Waypoint Trailhead (GC) */
	public static final String CW_GUIIMG_TRAILHEAD = "typeTrailhead.png";
	/** image for Additional Waypoint Reference Point (GC) */
	public static final String CW_GUIIMG_REFERENCE = "typeReference.png";
	/** image for Mega Event Cache (GC) */
	public static final String CW_GUIIMG_MEGA_EVENT = "typeMegaevent.png";
	/** image for WhereIGo Cache (GC) */
	public static final String CW_GUIIMG_WHEREIGO = "typeWhereigo.png";
	/** image for Project Ape cache (GC)*/
	public static final String CW_GUIIMG_APE = "typeApe.png";
	/** image for Adenture Maze Exhibit (GC)*/
	public static final String CW_GUIIMG_MAZE = "typeMaze.png";
	/** image for Earth Cache (GC) */
	public static final String CW_GUIIMG_EARTH = "typeEarth.png";
	
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_CUSTOM = "Custom";
	/** GUI string for custom waypoit */
	public static final String CW_GUISTR_TRADI = "Traditional";
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
	
	/** GPX identifier for Traditional caches */
	public static final String GC_GPX_TRADITIONAL = "Traditional Cache";
	/** GPX identifier for Multi caches */
	public static final String GC_GPX_MULTI = "Multi-cache";
	/** GPX identifier for virtual caches */
	public static final String GC_GPX_VIRTUAL = "Virtual Cache";
	/** GPX identifier for Letterbox hybrids */
	public static final String GC_GPX_LETTERBOX = "Letterbox Hybrid";
	/** GPX identifier for Event caches */
	public static final String GC_GPX_EVENT = "Event Cache";
	/** GPX identifier for Unknown or Mystery caches */ 
	public static final String GC_GPX_UNKNOWN = "Unknown Cache";
	/** GPX identifier for Webcam caches */
	public static final String GC_GPX_WEBCAM = "Webcam Cache";
	/** GPX identifier for Locationless caches */
	public static final String GC_GPX_LOCATIONLESS = "Locationless (Reverse) Cache";
	/** GPX identifier for CITO events */
	public static final String GC_GPX_CITO = "Cache In Trash Out Event";
	/** GPX identifier for Earth caches */
	public static final String GC_GPX_EARTH = "Earthcache";
	/** GPX identifier for Mega Events */
	public static final String GC_GPX_MEGA_EVENT = "Mega-Event Cache";
	/** GPX identifier for WhereIGo caches */
	public static final String GC_GPX_WHEREIGO = "Wherigo Cache";
	/** GPX identifier for additional waypoint Parking */
	public static final String GC_GPX_PARKING = "Waypoint|Parking Area";
	/** GPX identifier for additional waypoint Stage */
	public static final String GC_GPX_STAGE = "Waypoint|Stages of a Multicache";
	/** GPX identifier for additional waypoint QTA */
	public static final String GC_GPX_QUESTION = "Waypoint|Question to Answer";
	/** GPX identifier for additional waypoint Final */
	public static final String GC_GPX_FINAL = "Waypoint|Final Location";
	/** GPX identifier for additional waypoint Trailhead */
	public static final String GC_GPX_TRAILHEAD = "Waypoint|Trailhead";
	/** GPX identifier for additional waypoint Reference Point */
	public static final String GC_GPX_REFERENCE = "Waypoint|Reference Point";
	/** GPX identifier for additional waypoint Adventure Maze Exhibit Events */
	public static final String GC_GPX_MAZE = "GPS Adventures Exhibit"; 
	/** GPX identifier for additional waypoint Project Ape caches */
	public static final String GC_GPX_APE = "Project APE Cache";

	/** thou shallst not instantiate this object */
	private CacheType() { 
		// Nothing to do
	}
	

	/**
	 * translate cache type to a short version for compact exporters or "smart" cache names. 
	 * @param type CacheWolf internal type information
	 * @return abbreviation of cache type
	 * @throws IllegalArgumentException if <code>type</code> can not be mapped
	 */
	public static String getExportShortId(final byte type) throws IllegalArgumentException {
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
		default: throw new IllegalArgumentException("unmatched argument "+type+" in CacheSize getExportShortId()");
		}
	}
	
	/**
	 * convert version1 type information to current values
	 * @param type version1 cache type information
	 * @return current version cache type information
	 * @throws IllegalArgumentException if <code>size</code> can not be mapped to internal representation
	 * @deprecated remove once v1 file version compatibility is abandoned
	 */
	public static byte v1Converter(final String type) throws IllegalArgumentException  {
		if (type.equals("0")) return CW_TYPE_CUSTOM;
		if (type.equals("2")) return CW_TYPE_TRADITIONAL;
		if (type.equals("3")) return CW_TYPE_MULTI;
		if (type.equals("4")) return CW_TYPE_VIRTUAL;
		if (type.equals("5")) return CW_TYPE_LETTERBOX;
		if (type.equals("6")) return CW_TYPE_EVENT;
		if (type.equals("8")) return CW_TYPE_UNKNOWN;
		if (type.equals("11")) return CW_TYPE_WEBCAM;
		if (type.equals("12")) return CW_TYPE_LOCATIONLESS;
		if (type.equals("13")) return CW_TYPE_CITO;
		if (type.equals("50")) return CW_TYPE_PARKING;
		if (type.equals("51")) return CW_TYPE_STAGE;
		if (type.equals("52")) return CW_TYPE_QUESTION;
		if (type.equals("53")) return CW_TYPE_FINAL;
		if (type.equals("54")) return CW_TYPE_TRAILHEAD;
		if (type.equals("55")) return CW_TYPE_REFERENCE;
		if (type.equals("453")) return CW_TYPE_MEGA_EVENT;
		if (type.equals("1858")) return CW_TYPE_WHEREIGO;
		if (type.equals("137")) return CW_TYPE_EARTH;
		
		throw new IllegalArgumentException("unmatched argument "+type+" in CacheSize v1Converter()");
	}

	/**
	 * convert version1 type information to current values
	 * @param type version2 cache type information
	 * @return current version cache type information
	 * @throws IllegalArgumentException if <code>size</code> can not be mapped to internal representation
	 * @deprecated remove once v2 file version compatibility is abandoned
	 */
	public static byte v2Converter(final byte type) throws IllegalArgumentException  {
		switch (type) {
		case -128: return CW_TYPE_CUSTOM;
		case -126: return CW_TYPE_TRADITIONAL;
		case -125: return CW_TYPE_MULTI;
		case -124: return CW_TYPE_VIRTUAL;
		case -123: return CW_TYPE_LETTERBOX;
		case -122: return CW_TYPE_EVENT;
		case -121: return CW_TYPE_QUIZ;
		case -120: return CW_TYPE_UNKNOWN;
		case -119: return CW_TYPE_MOVING;
		case -118: return CW_TYPE_DRIVE_IN;
		case -117: return CW_TYPE_WEBCAM;
		case -116: return CW_TYPE_LOCATIONLESS;
		case -115: return CW_TYPE_CITO;
		case -78: return CW_TYPE_PARKING;
		case -77: return CW_TYPE_STAGE;
		case -76: return CW_TYPE_QUESTION;
		case -75: return CW_TYPE_FINAL;
		case -74: return CW_TYPE_TRAILHEAD;
		case -73: return CW_TYPE_REFERENCE;
		case 101: return CW_TYPE_MEGA_EVENT;
		case -62: return CW_TYPE_WHEREIGO; // yes, it can be either of these
		case 100: return CW_TYPE_WHEREIGO; // yes, it can be either of these
		case 9: return CW_TYPE_EARTH;
		default: throw new IllegalArgumentException("unmatched argument "+type+" in CacheSize v2Converter()");
		}
	}
	
	/**
	 * check if a given waypoint type is an additional waypoint
	 * @param type waypoint type to check 
	 * @return true if it is an additional waypint, false otherwise
	 */
	public static boolean isAddiWpt(final byte type) {
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
	 * this is the same as !isAddiWpt except that CW_TYPE_CUSTUM and CW_TYPE_ERROR return false
	 * I (pfeffer) don't know if this behaviour is intended, I guess it is.
	 * @param type
	 * @return
	 */
	public static boolean isCacheWpt(final byte type) {
		switch (type) {
		case CW_TYPE_CUSTOM:
		case CW_TYPE_ERROR:
			return false;
		default: return !isAddiWpt(type);
		}
	}
	
	public static boolean isCustomWpt(final byte type) {
		return type == CW_TYPE_CUSTOM;
	}
	
	/**
	 * create list of cache types to be shown in GUI drop down lists
	 * @return list of cache types to be shown in GUI drop down list
	 * @see guiSelect2Cw
	 * @see cw2GuiSelect
	 */
	//TODO: move to a class "closer" to the gui?
	public static String[] guiTypeStrings() {
		return new String[] {
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
	}
	
	/**
	 * translate GUI drop down index selection back to internally stored type
	 * @param selection index value from drop down list
	 * @return internal type
	 * @throws IllegalArgumentException if <code>selection</code> can not be matched
	 * @see guiTypeStrings
	 * @see cw2GuiSelect
	 */
	//TODO: move to a class "closer" to the gui?
	public static byte guiSelect2Cw(final int selection) throws IllegalArgumentException {
		// make sure to reflect the order of guiTypeStrings()
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
	 * translate cache type to position of index to highlight in GUI cache type drop down list 
	 * @param typeId internal id of cache type
	 * @return index of the cache type in GUI list
	 * @throws IllegalArgumentException if <code>id</code> can not be matched
	 * @see guiTypeStrings
	 * @see guiSelect2Cw
	 */
	//TODO: move to a class "closer" to the gui?
	public static int cw2GuiSelect(final byte typeId) throws IllegalArgumentException {
		switch (typeId) {
		case CW_TYPE_CUSTOM: return 0;
		case CW_TYPE_TRADITIONAL: return 1;
		case CW_TYPE_MULTI: return 2;
		case CW_TYPE_VIRTUAL: return 3;
		case CW_TYPE_LETTERBOX: return 4;
		case CW_TYPE_EVENT: return 5;
		case CW_TYPE_MEGA_EVENT: return 6;
		case CW_TYPE_WEBCAM: return 7;
		case CW_TYPE_UNKNOWN: return 8;
		case CW_TYPE_LOCATIONLESS: return 9;
		case CW_TYPE_CITO: return 10;
		case CW_TYPE_EARTH: return 11;
		case CW_TYPE_WHEREIGO: return 12;
		case CW_TYPE_PARKING: return 13;
		case CW_TYPE_STAGE: return 14;
		case CW_TYPE_QUESTION: return 15;
		case CW_TYPE_FINAL: return 16;
		case CW_TYPE_TRAILHEAD: return 17;
		case CW_TYPE_REFERENCE: return 18;
		default: throw new IllegalArgumentException("unmatched argument "+typeId+" in CacheSize cw2GuiSelect()");
		}
	}
	
	/**
	 * convert the strings found in import of GPX from GC, OC or TC to internal cache type 
	 * @param gpxType type information found in GPX
	 * @return internal cache type
	 */
	public static byte gpxType2CwType(final String gpxType) throws IllegalArgumentException {
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
		if (gpxType.equals(GC_GPX_FINAL)||gpxType.equals("Waypoint|Final Coordinates")) return CW_TYPE_FINAL;
		if (gpxType.equals(GC_GPX_TRAILHEAD)) return CW_TYPE_TRAILHEAD;
		if (gpxType.equals(GC_GPX_REFERENCE)) return CW_TYPE_REFERENCE;
		if (gpxType.equals(GC_GPX_MAZE)) return CW_TYPE_MAZE;
		if (gpxType.equals(GC_GPX_APE)) return CW_TYPE_APE;
		return CW_TYPE_CUSTOM;
	}
	
	/**
	 * convert the cache type information from an OC XML import to internal cache type
	 * @param ocType cache type found in OC XML
	 * @return internal cache type
	 * @throws IllegalArgumentException if <code>ocType</code> can not be matched
	 */
	public static byte ocType2CwType(final String ocType) throws IllegalArgumentException {
		if(ocType.equals("1")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("2")) return CW_TYPE_TRADITIONAL;
		if(ocType.equals("3")) return CW_TYPE_MULTI;	
		if(ocType.equals("4")) return CW_TYPE_VIRTUAL;
		if(ocType.equals("5")) return CW_TYPE_WEBCAM;
		if(ocType.equals("6")) return CW_TYPE_EVENT;
		if(ocType.equals("7")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("8")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("9")) return CW_TYPE_UNKNOWN;
		if(ocType.equals("10")) return CW_TYPE_TRADITIONAL; // drive in
		throw new IllegalArgumentException("unmatched argument "+ocType+" in CacheSize ocType2CwType()");
	}
	
	/**
	 * convert type information discovered by GC spider to internal type information
	 * @param gcType type information from GC spider
	 * @return internal representation of cache type
	 * @throws IllegalArgumentException if <code>gcType</code> can not be matched
	 */
	public static byte gcSpider2CwType(final String gcType) throws IllegalArgumentException {
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
		if (gcType.equals("1304")) { return CW_TYPE_MAZE; }
		if (gcType.equals("1858")) { return CW_TYPE_WHEREIGO; }
		throw new IllegalArgumentException("unmatched argument "+gcType+" in CacheSize gcSpider2CwType()");
	}
	
	/**
	 * map cache types to images
	 * @param typeId internal cache type id
	 * @return non qualified name of image
	 * @throws IllegalArgumentException if <code>id</code> can not be matched
	 */
	public static String typeImageForId(final byte typeId) throws IllegalArgumentException {
		switch (typeId) {
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
		default: throw new IllegalArgumentException("unmatched argument "+typeId+" in CacheSize typeImageForId()");
		}
	}
	
	/**
	 * generate type description matching those of GC for GPX export
	 * @param typeId internal type id
	 * @return type information in GC.com GPX format 
	 * @throws IllegalArgumentException
	 */
	public static String id2GpxString(final byte typeId) throws IllegalArgumentException {
		switch (typeId) {
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
		case CW_TYPE_CUSTOM: return CW_GUISTR_CUSTOM;
		default: throw new IllegalArgumentException("unmatched argument "+typeId+" in CacheSize id2GpxString()");
		}
	}
	
	//TODO: do we actually need this one?
	/**
	 * generate human readable type description for exporters
	 * @param typeId internal type id
	 * @return human readable description of waypoint type for exporters  
	 * @throws IllegalArgumentException if <code>id</code> is not a valid cache type
	 */
	public static String cw2ExportString(final byte typeId) throws IllegalArgumentException {
		String ret;
		try {
			ret = id2GpxString(typeId);
			// check for | in additional waypoints and only use the string after |
			final int pipePosistion = ret.indexOf('|');
			if (pipePosistion > -1) {
				ret = ret.substring(pipePosistion+1);
			} //TODO: check for exceeding max length
		} catch (IllegalArgumentException ex) {
			ret = "";
		}
		return ret;
	}
	
	/**
	 * checks if the given type would be valid for internal use in cachewolf
	 * @param type type value to be checked
	 * @return true if <code>type</code> matches on of the CacheWolf types, false otherwise
	 */
	public static boolean isValidType(final byte type) {
		switch (type) {
		case CW_TYPE_TRADITIONAL: return true;
		case CW_TYPE_MULTI: return true;
		case CW_TYPE_VIRTUAL: return true;
		case CW_TYPE_LETTERBOX: return true;
		case CW_TYPE_EVENT: return true;
		case CW_TYPE_UNKNOWN: return true;
		case CW_TYPE_WEBCAM: return true;
		case CW_TYPE_LOCATIONLESS: return true;
		case CW_TYPE_CITO: return true;
		case CW_TYPE_EARTH: return true;
		case CW_TYPE_MEGA_EVENT: return true;
		case CW_TYPE_WHEREIGO: return true;
		case CW_TYPE_PARKING: return true;
		case CW_TYPE_STAGE: return true;
		case CW_TYPE_QUESTION: return true;
		case CW_TYPE_FINAL: return true;
		case CW_TYPE_TRAILHEAD: return true;
		case CW_TYPE_REFERENCE: return true;
		case CW_TYPE_MAZE: return true;
		case CW_TYPE_APE: return true;
		case CW_TYPE_CUSTOM: return true;
		default: return false;
		}
	}
}
