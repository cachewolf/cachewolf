package CacheWolf;

/**
 * Handles all aspects of converting cache type information from
 * and to the various im- and exporters as well as for converting
 * legacy profiles to current standard
 *
 * Do not instantiate this class, only use it in a static way
 */
public final class CacheType {

	/** thou shallst not instantiate this object */
	private CacheType() {
		// Nothing to do
	}

	/** custom waypoint */
	public static final byte CW_TYPE_CUSTOM = 0;
	/** Project Ape cache (GC)*/
	public static final byte CW_TYPE_APE = 102;
	/** CITO cache (GC,OC)*/
	public static final byte CW_TYPE_CITO = 13;
	/** drive in cache (OC) */
	public static final byte CW_TYPE_DRIVE_IN = 10;
	/** Earth Cache (GC) */
	public static final byte CW_TYPE_EARTH = 104;

	/** event cache (OC,GC) */
	public static final byte CW_TYPE_EVENT = 6;
	/** Additional Waypoint Final (GC) */
	public static final byte CW_TYPE_FINAL = 53;
	/** letterbox cache (GC) */
	public static final byte CW_TYPE_LETTERBOX = 5;
	/** locationless cache (GC) */
	public static final byte CW_TYPE_LOCATIONLESS = 12;
	/** Adenture Maze Exhibit (GC)*/
	public static final byte CW_TYPE_MAZE = 103;

	/** Mega Event Cache (GC) */
	public static final byte CW_TYPE_MEGA_EVENT = 100;
	/** multi cache (GC,OC) */
	public static final byte CW_TYPE_MULTI = 3;
	/** Additional Waypoint Parking (GC) */
	public static final byte CW_TYPE_PARKING = 50;
	/** Additional Waypoint Question to answer (GC) */
	public static final byte CW_TYPE_QUESTION = 52;
	/** Additional Waypoint Reference (GC) */
	public static final byte CW_TYPE_REFERENCE = 55;

	/** Additional Waypoint Stage of a Multi (GC) */
	public static final byte CW_TYPE_STAGE = 51;
	/** traditional cache (GC,OC) */
	public static final byte CW_TYPE_TRADITIONAL = 2;
	/** Additional Waypoint Trailhead (GC) */
	public static final byte CW_TYPE_TRAILHEAD = 54;
	/** unknown cache (GC) */
	public static final byte CW_TYPE_UNKNOWN = 8;
	/** virtual cache (GC) */
	public static final byte CW_TYPE_VIRTUAL = 4;

	/** webcam cache (GC,OC) */
	public static final byte CW_TYPE_WEBCAM = 11;
	/** WhereIGo Cache (GC) */
	public static final byte CW_TYPE_WHEREIGO = 101;

	/** quiz cache (OC) */
	public static final byte CW_TYPE_QUIZ = 7;
	/** math cache (OC) */
	public static final byte CW_TYPE_MATH = 108;
	/** moving cache (OC) */
	public static final byte CW_TYPE_MOVING = 9;

	/** unrecognized cache type or missing information, should throw IllegalArgumentExceptions when found */
	public static final byte CW_TYPE_ERROR = -1;

	static final byte[] CT={
		CW_TYPE_CUSTOM, CW_TYPE_APE, CW_TYPE_CITO, CW_TYPE_DRIVE_IN, CW_TYPE_EARTH,
		CW_TYPE_EVENT, CW_TYPE_FINAL, CW_TYPE_LETTERBOX, CW_TYPE_LOCATIONLESS, CW_TYPE_MAZE,
		CW_TYPE_MEGA_EVENT, CW_TYPE_MULTI, CW_TYPE_PARKING, CW_TYPE_QUESTION, CW_TYPE_REFERENCE,
		CW_TYPE_STAGE, CW_TYPE_TRADITIONAL, CW_TYPE_TRAILHEAD, CW_TYPE_UNKNOWN, CW_TYPE_VIRTUAL,
		CW_TYPE_WEBCAM, CW_TYPE_WHEREIGO,
		CW_TYPE_QUIZ, CW_TYPE_MATH, CW_TYPE_MOVING,
		CW_TYPE_ERROR
};
	// 0=Custom - 1=APE ("Project APE Cache") - 2=CITO - 3=Drive_In - 4=Earthcache
	// 5=Event - 6=Final - 7=Letterbox - 8=Locationless - 9=Maze ("Adventure Maze Exhibit")
	// 10=Megaevent - 11=Multi - 12=Parking - 13=Question - 14=Reference
	// 15=Stage - 16=Traditional - 17=Trailhead - 18=Unknown(Mysterie) - 19=Virtual
	// 20=Webcam - 21=Wherigo
	// 22=Quiz(Unknown) - 23=Math - 24=Moving
	// 25=Error
	/*
	static final byte[] CTn= {
			0,102,13,10,104,
			6,53,5,12,103,
			100,3,50,52,55,
			51,2,54,8,4,
			11,101,
			7,108,9,
			-1
	};
	*/
	static final byte[] CT_Index = new byte[110];
	static {
	  for (byte i=0; i<CT.length; i++) {
	    CT_Index[CT[i]+1]=i;
	   }
	}
	public static byte CT_Index(final byte type) {
		return CT_Index[type+1];
	}

	static final String[] CT_FILENAME={
		"typeCustom.png", "typeApe.png", "typeCito.png", "typeDrivein.png", "typeEarth.png",
		"typeEvent.png", "typeFinal.png", "typeLetterbox.png", "typeLocless.png", "typeMaze.png",
		"typeMegaevent.png", "typeMulti.png", "typeParking.png", "typeQuestion.png", "typeReference.png",
		"typeStage.png", "typeTradi.png", "typeTrailhead.png", "typeUnknown.png", "typeVirtual.png",
		"typeWebcam.png", "typeWhereigo.png",
		"typeUnknown.png", "typeMath.png", "typeMoving.png",
		"guiError.png"
	};
	static final String[] CT_TYPETAG = {
		"Geocache|Custom", "Geocache|Project APE Cache", "Geocache|Cache In Trash Out Event", "Geocache|DriveIn", "Geocache|Earthcache",
		"Geocache|Event Cache", "Waypoint|Final Location", "Geocache|Letterbox Hybrid", "Geocache|Locationless (Reverse) Cache", "Geocache|GPS Adventures Exhibit",
		"Geocache|Mega-Event Cache", "Geocache|Multi-cache", "Waypoint|Parking Area", "Waypoint|Question to Answer", "Waypoint|Reference Point",
		"Waypoint|Stages of a Multicache", "Geocache|Traditional Cache", "Waypoint|Trailhead", "Geocache|Unknown Cache", "Geocache|Virtual Cache",
		"Geocache|Webcam Cache", "Geocache|Wherigo Cache",
		"Geocache|Unknown Cache", "Geocache|Math", "Geocache|Moving",
		"Fehler"
	};
	static final String[] CT_SYMTAG = {
		"Custom", "Geocache", "Geocache", "Geocache", "Geocache",
		"Geocache", "Final Location", "Geocache", "Geocache", "Geocache",
		"Geocache", "Geocache", "Parking Area", "Question to Answer", "Reference Point",
		"Stages of a Multicache", "Geocache", "Trailhead", "Geocache", "Geocache",
		"Geocache", "Geocache",
		"Geocache", "Geocache", "Geocache",
		"Fehler"
	};
	static final String[] CT_GSTYPETAG = {
		"Custom", "Project APE Cache", "Cache In Trash Out Event", "DriveIn", "Earthcache",
		"Event Cache", "Final Location", "Letterbox Hybrid", "Locationless (Reverse) Cache", "GPS Adventures Exhibit",
		"Mega-Event Cache", "Multi-cache", "Parking Area", "Question to Answer", "Reference Point",
		"Stages of a Multicache", "Traditional Cache", "Trailhead", "Unknown Cache", "Virtual Cache",
		"Webcam Cache", "Wherigo Cache",
		"Unknown Cache", "Math", "Moving",
		"Fehler"
	};
	//TODO: texts from mylocale ?
	public static final String[] CT_GUI={
		"Custom", "APE", "CITO", "DriveIn", "Earthcache",
		"Event", "Addi: Final", "Letterbox", "Locationless", "MAZE",
		"Mega Event", "Multi", "Addi: Parking", "Addi: Question", "Addi: Reference",
		"Addi: Stage", "Traditional", "Addi: Trailhead", "Mystery", "Virtual",
		"Webcam", "WherIGo",
		"Quiz", "Math", "Moving",
		"Fehler"
	};

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
				CT_GUI[CT_Index(CW_TYPE_CUSTOM)],
				CT_GUI[CT_Index(CW_TYPE_TRADITIONAL)],
				CT_GUI[CT_Index(CW_TYPE_MULTI)],
				CT_GUI[CT_Index(CW_TYPE_VIRTUAL)],
				CT_GUI[CT_Index(CW_TYPE_LETTERBOX)],
				CT_GUI[CT_Index(CW_TYPE_EVENT)],
				CT_GUI[CT_Index(CW_TYPE_MEGA_EVENT)],
				CT_GUI[CT_Index(CW_TYPE_WEBCAM)],
				CT_GUI[CT_Index(CW_TYPE_UNKNOWN)],
				CT_GUI[CT_Index(CW_TYPE_LOCATIONLESS)],
				CT_GUI[CT_Index(CW_TYPE_CITO)],
				CT_GUI[CT_Index(CW_TYPE_EARTH)],
				CT_GUI[CT_Index(CW_TYPE_WHEREIGO)],
				CT_GUI[CT_Index(CW_TYPE_PARKING)],
				CT_GUI[CT_Index(CW_TYPE_STAGE)],
				CT_GUI[CT_Index(CW_TYPE_QUESTION)],
				CT_GUI[CT_Index(CW_TYPE_FINAL)],
				CT_GUI[CT_Index(CW_TYPE_TRAILHEAD)],
				CT_GUI[CT_Index(CW_TYPE_REFERENCE)],
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
		for (byte i=0; i<CT.length; i++) {
			if (CT_TYPETAG[i].equals(gpxType)) {return CT[i];};
		}
		/*
		for (byte i=0; i<CT.length; i++) {
			if (CT_SYMTAG[i].equals(gpxType)) {return CT[i];};
		}
		 */
		for (byte i=0; i<CT.length; i++) {
			if (CT_GSTYPETAG[i].equals(gpxType)) {return CT[i];};
		}
		if (gpxType.equals("Traditional")|| gpxType.equals("Classic")) return CW_TYPE_TRADITIONAL;
		if (gpxType.equals("Multi") || gpxType.equals("Offset")) return CW_TYPE_MULTI;
		if (gpxType.equals("Virtual")) return CW_TYPE_VIRTUAL;
		if (gpxType.equals("Event")) return CW_TYPE_EVENT;
		if (gpxType.equals("Other") || gpxType.equals("Quiz")) return CW_TYPE_UNKNOWN;
		if (gpxType.equals("Webcam")) return CW_TYPE_WEBCAM;
		if (gpxType.equals("Earth")) return CW_TYPE_EARTH;
		return CW_TYPE_ERROR;
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
		if (gcType.equals("9")) { return CW_TYPE_APE; } //102
		if (gcType.equals("11")) { return CW_TYPE_WEBCAM; }
		if (gcType.equals("12")) { return CW_TYPE_LOCATIONLESS; }
		if (gcType.equals("13")) { return CW_TYPE_CITO; }
		if (gcType.equals("137")) { return CW_TYPE_EARTH; } //104
		if (gcType.equals("453")) { return CW_TYPE_MEGA_EVENT; } //100
		if (gcType.equals("1304")) { return CW_TYPE_MAZE; } //103
		if (gcType.equals("1858")) { return CW_TYPE_WHEREIGO; } //101
		throw new IllegalArgumentException("unmatched argument "+gcType+" in CacheSize gcSpider2CwType()");
	}

	/**
	 * map cache types to images
	 * @param typeId internal cache type id
	 * @return non qualified name of image
	 */
	public static String typeImageForId(final byte typeId) {
		return CT_FILENAME[CT_Index(typeId)];
	}

	/**
	 * generate type description matching those of GC for GPX export
	 * @param typeId internal type id
	 * @return type information in GC.com <type> GPX format
	 */
	public static String type2TypeTag(final byte typeId) {
		return CT_TYPETAG[CT_Index(typeId)];
	}
	/**
	 * generate type description matching those of GC for GPX export
	 * @param typeId internal type id
	 * @return symb information in GC.com <sym> GPX format
	 */
	public static String type2SymTag(final byte typeId) {
		return CT_SYMTAG[CT_Index(typeId)];
	}
	/**
	 * generate type description matching those of GC for GPX export
	 * @param typeId internal type id
	 * @return type information in GC.com <groundspeak:type> GPX format
	 */
	public static String type2GSTypeTag(final byte typeId) {
		return CT_GSTYPETAG[CT_Index(typeId)];
	}
	public static String type2Gui(final byte typeId) {
		return CT_GUI[CT_Index(typeId)];
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
