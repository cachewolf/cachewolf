/**
 * 
 */
package CacheWolf;

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
	//Types from gc.com
	static protected final int GC_TRADITIONAL = 2;
	static protected final int GC_MULTI = 3;
	static protected final int GC_VIRTUAL = 4;
	static protected final int GC_LETTERBOX = 5;
	static protected final int GC_EVENT = 6;
	static protected final int GC_UNKNOWN = 8;
	static protected final int GC_WEBCAM = 11;
	static protected final int GC_LOCATIONLESS = 12;
	// additional waypoints have no numbers in GPX Files, so lets use our own.
	static protected final int GC_AW_PARKING = 50;
	static protected final int GC_AW_STAGE_OF_MULTI = 51;
	static protected final int GC_AW_QUESTION = 52;
	static protected final int GC_AW_FINAL = 53;
	static protected final int GC_AW_TRAILHEAD = 54;
	
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
	static protected final int CW_PARKING = 50;
	static protected final int CW_STAGE_OF_MULTI = 51;
	static protected final int CW_QUESTION = 52;
	static protected final int CW_FINAL = 53;
	static protected final int CW_TRAILHEAD = 54;
	static protected final int CW_CNT_TYPES = 17;

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
	static protected final String CW_PIC_PARKING = "pkg.png";
	static protected final String CW_PIC_STAGE_OF_MULTI = "stage.png";
	static protected final String CW_PIC_QUESTION = "puzzle.png";
	static protected final String CW_PIC_FINAL = "flag.png";
	static protected final String CW_PIC_TRAILHEAD = "trailhead.png";
	
	//fields
	int type = 0;

	public static String transType(String geoNum){
		String geo = new String("Unknown");
		if(geoNum.equals("2")) geo = "Traditional Cache";
		if(geoNum.equals("3")) geo = "Multi-cache";
		if(geoNum.equals("4")) geo = "Virtual Cache";
		if(geoNum.equals("5")) geo = "Letterbox Hybrid";
		if(geoNum.equals("6")) geo = "Event Cache";
		if(geoNum.equals("11")) geo = "Webcam Cache";
		if(geoNum.equals("8")) geo = "Unknown Cache";
		if(geoNum.equals("12")) geo = "Locationless Cache";
		if(geoNum.equals("50")) geo = "Parking Area";
		if(geoNum.equals("51")) geo = "Stages of a Multicache";
		if(geoNum.equals("52")) geo = "Question to Answer";
		if(geoNum.equals("53")) geo = "Final Coordinates";
		if(geoNum.equals("54")) geo = "Trailhead";
		return geo;
	}

	public static String typeText2Number(String typeText){
		if (typeText.equals("Traditional Cache") || typeText.equals("Traditional")) return "2";
		if (typeText.equals("Multi-cache") || typeText.equals("Multi")) return "3";
		if (typeText.equals("Virtual Cache") || typeText.equals("Virtual")) return "4";
		if (typeText.equals("Letterbox Hybrid")) return "5";
		if (typeText.equals("Event Cache") || typeText.equals("Event")) return "6";
		if (typeText.equals("Unknown Cache") || typeText.equals("Other") || typeText.equals("Quiz")) return "8";
		if (typeText.equals("Webcam Cache") || typeText.equals("Webcam")) return "11";
		if (typeText.equals("Locationless (Reverse) Cache")) return "12";
		if (typeText.equals("CITO")) return "13";
		if (typeText.equals("Earthcache")) return "137";
		if (typeText.equals("Waypoint|Parking Area")) return "50";
		if (typeText.equals("Waypoint|Stages of a Multicache")) return "51";
		if (typeText.equals("Waypoint|Question to Answer")) return "52";
		if (typeText.equals("Waypoint|Final Coordinates")) return "53";
		if (typeText.equals("Waypoint|Trailhead")) return "54";
		Vm.debug("Unknown Cache Type:" + typeText);
		return "0";
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
		public static String transOCType(String type){
			if(type.equals("1")) return "8";
			if(type.equals("2")) return "2";
			if(type.equals("3")) return "3";	
			if(type.equals("4")) return "4";
			if(type.equals("5")) return "11";
			if(type.equals("6")) return "6";
			if(type.equals("7")) return "8";
			if(type.equals("8")|| type.equals("9") ||type.equals("10")) return "8";
	/* Not supportet at the moment
			if(type.equals("8")) return "108";
			if(type.equals("9")) return "109";
			if(type.equals("10")) return "110";
	*/
			//no match found? return custom type!
			return "0";
		}
	
	

}
