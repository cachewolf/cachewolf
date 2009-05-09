/**
 * 
 */
package CacheWolf;

import ewe.fx.Image;
import ewe.sys.Vm;


/**
 * @author Kalle
 * Class for dealing with different cachetypes from gc and oc
 */

/**
*	Listing of types and mappings
*	
*	OC		GC		Comment		Regel
*	1		8		Other			1->8
*	2		2		Traditional		2->2
*	3		3		Multi			3->3
*	4		4		Virtual		4->4
*	5		11		Webcam		5->11
*	6		6		Event			6->6
*	7		8		Quiz			7->8
*	8		??		Math			8->108 (ok)
*	9		??		Moving		9->109 (ok)
*	10		??		Drive-In		10->110 (ok)
*/

public class CacheType {
	private static Image cacheImages[] = new Image[454]; // Images are used by TableControl
	public static final int WHERIGO=100; // The cache we mapped the wherigo to
	public static final int MEGA_EVENT=101; // Mapping for Mega Event 
	static {
		cacheImages[0] = new Image("0.png");
		//cacheImages[1] = new Image();
		cacheImages[2] = new Image("2.png");
		cacheImages[3] = new Image("3.png");
		cacheImages[4] = new Image("4.png");
		cacheImages[5] = new Image("5.png");
		cacheImages[6] = new Image("6.png");
		cacheImages[8] = new Image("8.png");
		//cacheImages[9] = new Image();
		//cacheImages[10] = new Image();
		cacheImages[11] = new Image("11.png");
		cacheImages[12] = new Image("12.png");
		cacheImages[13] = new Image("13.png");
		//additional waypoints, begin with 50
		cacheImages[50] = new Image("pkg.png");
		cacheImages[51] = new Image("stage.png");
		cacheImages[52] = new Image("puzzle.png");
		cacheImages[53] = new Image("flag.png");
		cacheImages[54] = new Image("trailhead.png");
		cacheImages[55] = new Image("waypoint.png");

		cacheImages[108] = new Image("108.png");
		cacheImages[109] = new Image("109.png");
		cacheImages[110] = new Image("110.png");
		cacheImages[137] = new Image("137.png");
		cacheImages[WHERIGO] = new Image("1858.png");  // Fudge as whereigo is really 1858
		cacheImages[MEGA_EVENT] = new Image("453.png");
	}

	//Types from gc.com
	static protected final int GC_TRADITIONAL = 2;
	static protected final int GC_MULTI = 3;
	static protected final int GC_VIRTUAL = 4;
	static protected final int GC_LETTERBOX = 5;
	static protected final int GC_EVENT = 6;
	static protected final int GC_UNKNOWN = 8;
	static protected final int GC_WEBCAM = 11;
	static protected final int GC_LOCATIONLESS = 12;
	static protected final int GC_CITO = 13;
	static protected final int GC_EARTH = 137;
	static protected final int GC_MEGA_EVENT = 453;
	// additional waypoints have no numbers in GPX Files, so lets use our own.
	static protected final int GC_AW_PARKING = 50;
	static protected final int GC_AW_STAGE_OF_MULTI = 51;
	static protected final int GC_AW_QUESTION = 52;
	static protected final int GC_AW_FINAL = 53;
	static protected final int GC_AW_TRAILHEAD = 54;
	static protected final int GC_AW_REFERENCE = 55;
	
	//Types from oc.de
	static protected final int OC_UNKNOWN = 1;
	static protected final int OC_TRADITIONAL = 2;
	static protected final int OC_MULTI = 3;
	static protected final int OC_VIRTUAL = 4;
	static protected final int OC_WEBCAM = 5;
	static protected final int OC_EVENT = 6;
	static protected final int OC_QUIZ = 7;
	static protected final int OC_MATH = 8;
	static protected final int OC_MOVING = 9;
	static protected final int OC_DRIVE_IN = 10;
	
	//Our own mapping
	static protected final int CW_TRADITIONAL = 2;
	static protected final int CW_MULTI = 3;
	static protected final int CW_VIRTUAL = 4;
	static protected final int CW_LETTERBOX = 5;
	static protected final int CW_EVENT = 6;
	static protected final int CW_QUIZ = 7;
	static protected final int CW_UNKNOWN = 8;
	static protected final int CW_MATH = 8;
	static protected final int CW_MOVING = 9;
	static protected final int CW_DRIVE_IN = 10;
	static protected final int CW_WEBCAM = 11;
	static protected final int CW_LOCATIONLESS = 12;
	static protected final int CW_CITO = 13;
	static protected final int CW_EARTH = 137;
	static protected final int CW_MEGA_EVENT = 453;
	static protected final int CW_WHERIGO = 1858;
	static protected final int CW_PARKING = 50;
	static protected final int CW_STAGE_OF_MULTI = 51;
	static protected final int CW_QUESTION = 52;
	static protected final int CW_FINAL = 53;
	static protected final int CW_TRAILHEAD = 54;
	static protected final int CW_REFERENCE = 55;
	static protected final int CW_CNT_TYPES = 20;

	//Sources
	static protected final int SRC_GC = 1;
	static protected final int SRC_OC = 2;
	static protected final int SRC_CW = 3;

	//pictures
	static protected final String CW_PIC_UNKNOWN = "8.png";
	static protected final String CW_PIC_TRADITIONAL = "2.png";
	static protected final String CW_PIC_MULTI = "3.png";
	static protected final String CW_PIC_VIRTUAL = "4.png";
	static protected final String CW_PIC_WEBCAM = "11.png";
	static protected final String CW_PIC_EVENT = "6.png";
	static protected final String CW_PIC_QUIZ = "8.png";
	static protected final String CW_PIC_MATH = "108.png";
	static protected final String CW_PIC_MOVING = "109.png";
	static protected final String CW_PIC_DRIVE_IN = "110.png";
	static protected final String CW_PIC_LETTERBOX = "5.png";
	static protected final String CW_PIC_LOCATIONLESS = "12.png";
	static protected final String CW_PIC_CITO = "13.png";
	static protected final String CW_PIC_EARTH = "137.png";
	static protected final String CW_PIC_MEGA_EVENT = "453.png";
	static protected final String CW_PIC_WHERIGO = "1858.png";
	static protected final String CW_PIC_PARKING = "pkg.png";
	static protected final String CW_PIC_STAGE_OF_MULTI = "stage.png";
	static protected final String CW_PIC_QUESTION = "puzzle.png";
	static protected final String CW_PIC_FINAL = "flag.png";
	static protected final String CW_PIC_TRAILHEAD = "trailhead.png";
	static protected final String CW_PIC_REFERENCE = "waypoint.png";
	
	//fields
	int type = 0;
	
	// array with names and types for exporters
	public final static int WPT_TEXT = 0;
	public final static int WPT_NUM = 1;

	public static String[][] wayType = {{"Custom","0"},{"Traditional","2"},
			{"Multi","3"}, {"Virtual","4"},
			{"Letterbox","5"},{"Event","6"},
			{"Mega Event","453"}, {"Mystery","8"},
			{"Webcam","11"},{"Locationless","12"},
			{"CITO","13"},{"Earthcache","137"},
			{"Parking","50"},{"Stage","51"},
			{"Question","52"},{"Final","53"},
			{"Trailhead","54"},{"Reference","55"},{"WhereIGo","1858"}};

	public static String shortType(int typeNum){
		String shortType = new String("O");
		if(typeNum == 0) shortType = "C";
		if(typeNum == 2) shortType = "T";
		if(typeNum == 3) shortType = "M";
		if(typeNum == 4) shortType = "V";
		if(typeNum == 5) shortType = "L";
		if(typeNum == 6) shortType = "O";
		if(typeNum == 11) shortType = "W";
		if(typeNum == 8) shortType = "U";
		if(typeNum == 12) shortType = "O";
		if(typeNum == 13) shortType = "O";
		if(typeNum == 137) shortType = "E";
		if(typeNum == 453) shortType = "O";
		if(typeNum == 1858) shortType = "O";
		if(typeNum == 50) shortType = "P";
		if(typeNum == 51) shortType = "S";
		if(typeNum == 52) shortType = "Q";
		if(typeNum == 53) shortType = "F";
		if(typeNum == 54) shortType = "H";
		if(typeNum == 55) shortType = "R";
		return shortType;
	}
	public static String transType(int geoNum){
		String geo = new String("Unknown");
		if(geoNum == 2) geo = "Traditional Cache";
		if(geoNum == 3) geo = "Multi-cache";
		if(geoNum == 4) geo = "Virtual Cache";
		if(geoNum == 5) geo = "Letterbox Hybrid";
		if(geoNum == 6) geo = "Event Cache";
		if(geoNum == 11) geo = "Webcam Cache";
		if(geoNum == 8) geo = "Unknown Cache";
		if(geoNum == 12) geo = "Locationless Cache";
		if(geoNum == 13) geo = "Cache In Trash Out Event";
		if(geoNum == 137) geo = "Earthcache";
		if(geoNum == 453) geo = "Mega Event Cache";
		if(geoNum == 1858) geo = "WhereIGo Cache";
		if(geoNum == 50) geo = "Parking Area";
		if(geoNum == 51) geo = "Stages of a Multicache";
		if(geoNum == 52) geo = "Question to Answer";
		if(geoNum == 53) geo = "Final Location";
		if(geoNum == 54) geo = "Trailhead";
		if(geoNum == 55) geo = "Reference Point";
		return geo;
	}
	
	/**
	 * Returns the image name of a given internal type
	 * @param type Type of cache
	 * @return The image name of the cache
	 */
	public static String type2pic(int type) {
		switch(type) {
			case CW_TRADITIONAL:	return CW_PIC_TRADITIONAL;
			case CW_MULTI:			return CW_PIC_MULTI;
			case CW_VIRTUAL:		return CW_PIC_VIRTUAL;
			case CW_LETTERBOX:		return CW_PIC_LETTERBOX;
			case CW_EVENT:			return CW_PIC_EVENT;
			case CW_QUIZ:			return CW_PIC_QUIZ;
			case CW_UNKNOWN:		return CW_PIC_UNKNOWN;
			case CW_MOVING:			return CW_PIC_MOVING;
			case CW_DRIVE_IN:		return CW_PIC_DRIVE_IN;
			case CW_WEBCAM:			return CW_PIC_WEBCAM;
			case CW_LOCATIONLESS:	return CW_PIC_LOCATIONLESS;
			case CW_CITO:			return CW_PIC_CITO;
			case CW_EARTH:			return CW_PIC_EARTH;
			case CW_MEGA_EVENT:		return CW_PIC_MEGA_EVENT;
			case CW_WHERIGO:	    return CW_PIC_WHERIGO;
			case CW_PARKING:		return CW_PIC_PARKING;
			case CW_STAGE_OF_MULTI:	return CW_PIC_STAGE_OF_MULTI;
			case CW_QUESTION:		return CW_PIC_QUESTION;
			case CW_FINAL:			return CW_PIC_FINAL;
			case CW_TRAILHEAD:		return CW_PIC_TRAILHEAD;
			case CW_REFERENCE:		return CW_PIC_REFERENCE;
			default:		return "no_picture.png";
		}
	}

	public static boolean isAddiWpt(int type){
		return (type >= 50 && type <= 55);
	}

	public static int typeText2Number(String typeText){
		if (typeText.equals("Traditional Cache") || typeText.equals("Traditional")|| typeText.equals("Classic")) return 2;
		if (typeText.equals("Multi-cache") || typeText.equals("Multi") || typeText.equals("Offset")) return 3;
		if (typeText.equals("Virtual Cache") || typeText.equals("Virtual")) return 4;
		if (typeText.equals("Letterbox Hybrid")) return 5;
		if (typeText.equals("Event Cache") || typeText.equals("Event")) return 6;
		if (typeText.equals("Unknown Cache") || typeText.equals("Other") || typeText.equals("Quiz")) return 8;
		if (typeText.equals("Webcam Cache") || typeText.equals("Webcam")) return 11;
		if (typeText.equals("Locationless (Reverse) Cache")) return 12;
		if (typeText.equals("Cache In Trash Out Event")) return 13;
		if (typeText.equals("Earthcache") || typeText.equals("Earth")) return 137;
		if (typeText.equals("Mega-Event Cache")) return 453;
		if (typeText.equals("Wherigo Cache")) return 1858;
		if (typeText.equals("Waypoint|Parking Area")) return 50;
		if (typeText.equals("Waypoint|Stages of a Multicache")) return 51;
		if (typeText.equals("Waypoint|Question to Answer")) return 52;
		if (typeText.equals("Waypoint|Final Coordinates")||typeText.equals("Waypoint|Final Location")) return 53;
		if (typeText.equals("Waypoint|Trailhead")) return 54;
		if (typeText.equals("Waypoint|Reference Point")) return 55;
		Vm.debug("Unknown Cache Type:" + typeText);
		return 0;
	}

	/**
		*	Method to translate opencaching types to geocaching types.
		*	Required to be "backwards" compatible :-(
		*	OC		GC		Comment		Regel
		*	1		8		Other			1->8
		*	2		2		Traditional		2->2
		*	3		3		Multi			3->3
		*	4		4		Virtual		4->4
		*	5		11		Webcam		5->11
		*	6		6		Event			6->6
		*	7		8		Quiz			7->8
		*	8		??		Math			8->108 (ok)
		*	9		??		Moving		9->109 (ok)
		*	10		??		Drive-In		10->110 (ok)
		*/
		public static int transOCType(int type){
			if(type == 1) return 8;
			if(type == 2) return 2;
			if(type == 3) return 3;	
			if(type == 4) return 4;
			if(type == 5) return 11;
			if(type == 6) return 6;
			if(type == 7) return 8;
			if(type >= 8 || type <= 10) return 8;
	/* Not supportet at the moment
			if(type.equals("8")) return "108";
			if(type.equals("9")) return "109";
			if(type.equals("10")) return "110";
	*/
			//no match found? return custom type!
			return 0;
		}

		
	public static Image cache2Img(int cacheType) {
		int index = cacheType;
		switch (cacheType) {
			case 1858: index =  WHERIGO; break;
			case 453:  index =  MEGA_EVENT; break;
		}
		return cacheImages[index];
	}
	
	/**
	 * Packs the cache type into the range of a byte. For the int values which don't fit in the 
	 * byte range, some conversion has to be done.
	 * @param cacheType Cache Type as integer
	 * @return A corresponding byte value. 
	 */
	public static byte toByte(int cacheType) {
		int result = cacheType-128;
		switch (cacheType) {
			case 1858: result =  WHERIGO; break;
			case 453:  result =  MEGA_EVENT; break;
			default: result = cacheType-128;
		}
		return (byte)(result);
	}
	
	/**
	 * Unpacks the cache type from byte to int. 
	 * @param cacheType Cache type as byte
	 * @return The cache type as int
	 */
	public static int toInt(byte cacheType) {
		int result;
		switch (cacheType) {
		case WHERIGO:     result =  1858; break;
		case MEGA_EVENT:  result =  453; break;
		default: result = cacheType+128;
		}
		return result;
	}
	

}
