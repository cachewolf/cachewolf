﻿/*
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

import ewe.fx.mImage;
import ewe.io.FileBase;

/**
 * This class represents a single attribute
 * 
 * @author skg
 * 
 */
public class Attribute {
	// Constructors
	public Attribute(int id, int inc) {
		_Id = id;
		setInc(inc);
		setIdBit();
	}

	public Attribute(String attributeName) {
		attName2attNo(attributeName);
		setIdBit();
	}

	public Attribute(int attIdOC) {
		OCAttNo2attNo(attIdOC);
		setIdBit();
	}

	public Attribute(int attIdGC, String Yes1No0) {
		GCAttNo2attNo(attIdGC, Yes1No0);
		setIdBit();
	}

	// Constructors end
	private int _Id;
	private int _Inc; // Yes=1 No=0 non=2
	private String _ImageName;
	private long[] _bit = { 0l, 0l };

	// for GC Constructor Spider
	private void attName2attNo(String attributeName) {
		for (int i = 0; i < maxAttRef; i++) {
			if (attributeName.toLowerCase().startsWith(attRef[i][PIC_NAME])) {
				_Id = i;
				_Inc = attributeName.toLowerCase().endsWith("-no.gif") ? 0 : 1;
				_ImageName = attRef[i][PIC_NAME] + (_Inc == 0 ? "-no.gif" : "-yes.gif");
				return;
			}
		}
		_Id = -1; // Error
		_ImageName = "error.gif";
	}

	// for OC Constructor
	private void OCAttNo2attNo(int attIdOC) {
		for (int i = 0; i < maxAttRef; i++) {
			if (attIdOC == Common.parseInt(attRef[i][OC_ID])) {
				_Id = i;
				_Inc = 1;
				_ImageName = attRef[i][PIC_NAME] + "-yes.gif";
				return;
			}
		}
		_Id = -1; // Error
		_ImageName = "error.gif";
	}

	// for GC Constructor gpx-Import
	private void GCAttNo2attNo(int attIdGC, String Yes1No0) {
		for (int i = 0; i < maxAttRef; i++) {
			if (attIdGC == Common.parseInt(attRef[i][GC_ID])) {
				_Id = i;
				_Inc = Yes1No0.equals("1") ? 1 : 0;
				_ImageName = attRef[i][PIC_NAME] + (_Inc == 0 ? "-no.gif" : "-yes.gif");
				return;
			}
		}
		_Id = -1; // Error
		_ImageName = "error.gif";
	}

	// used by all Constructors
	private void setIdBit() {
		_bit = getIdBit(_Id);
	}

	// checking for array limits (caus -1 is possible value)
	private static String attRef(int row, int column) {
		if (row > -1 && row < maxAttRef) {
			return attRef[row][column];
		} else {
			return "";
		}
	}

	// *** public part
	public static long[] getIdBit(int id) {
		long[] bit = new long[2];
		if (id > -1 && id < maxAttRef) {
			int b = Common.parseInt(attRef[id][BIT_NR]);
			bit[0] = b > 63 ? 0l : (1L << b);
			bit[1] = b > 63 ? (1L << b - 64) : 0;
		} else {
			bit[0] = 0;
			bit[1] = 0;
		}
		return bit;
	}

	/**
	 * get GC_TEXT string
	 */
	public String getGCText() {
		return attRef(_Id, GC_TEXT);
	}

	/*
     * 
     */
	public static String getIdFromGCText(String t) {
		for (int i = 0; i < maxAttRef; i++) {
			if (attRef[i][GC_TEXT].equals(t)) {
				return attRef[i][GC_ID];
			}
		}
		return "-1";
	}

	/**
	 * get GC_ID string
	 */
	public String getGCId() {
		return attRef(_Id, GC_ID);
	}

	/**
	 * getting attribute given=1,negative=0,not specified=2
	 */
	public int getInc() {
		return _Inc;
	}

	/**
	 * getting CW internal attribute number (-1..127)
	 */
	public int getId() {
		return _Id;
	}

	/**
	 * setting/changing attribute given=1,negative=0,not specified=2
	 */
	public void setInc(int inc) {
		_Inc = inc;
		if (_Id < 0 || _Id >= maxAttRef) {
			_ImageName = "error.gif";
		} else {
			_ImageName = attRef(_Id, PIC_NAME);
			if (inc == 0)
				_ImageName += "-no.gif";
			else if (inc == 1)
				_ImageName += "-yes.gif";
			else
				_ImageName += "-non.gif";
		}
	}

	/**
	 * getting name of corresponding image stored in attributes subdirectory
	 */
	public String getImageName() {
		return _ImageName;
	}

	/**
	 * getting path+name of corresponding image stored in attributes subdirectory
	 */
	public String getPathAndImageName() {
		return IMAGEDIR + _ImageName;
	}

	/**
	 * set/unset the bit in the long array that belongs to the Id of the attribute
	 */
	public long[] getYesBit(long[] yes) {
		if (_Inc == 1) {
			yes[0] |= _bit[0];
			yes[1] |= _bit[1];
		} else {
			yes[0] &= ~_bit[0];
			yes[1] &= ~_bit[1];
		}
		return yes;
	}

	/**
	 * set/unset the bit in the long array that belongs to the Id of the attribute
	 */
	public long[] getNoBit(long[] no) {
		if (_Inc == 0) {
			no[0] |= _bit[0];
			no[1] |= _bit[1];
		} else {
			no[0] &= ~_bit[0];
			no[1] &= ~_bit[1];
		}
		return no;
	}

	/**
	 * get the language dependant description of the attribute
	 */
	public String getMsg() {
		return getMsg(_Id, _Inc);
	}

	private final static int BIT_NR = 0;
	private final static int MSG_NR = 1;
	private final static int PIC_NAME = 2;
	private final static int OC_ID = 3; // OC - XML
	private final static int GC_ID = 4; // auch OC neues gpx
	private final static int GC_TEXT = 5; // for export , didn't extract by myself, copied from forum
	private static final String[][] attRef = { { "00", "2502", "available", "38", "13", "Available at all times" },// 02 available 24-7
			{ "01", "2504", "bicycles", "0", "32", "Bicycles" },// 04 bikes allowed
			{ "02", "2506", "boat", "52", "4", "Boat" },// 06 Wasserfahrzeug
			// {"03","2508","cactus","0","0",""},//08 removed 14.08.10 araber95
			{ "04", "2510", "campfires", "0", "38", "Campfires" },// 10 campfires allowed
			{ "05", "2512", "camping", "0", "31", "Camping available" },// 12 Camping allowed
			{ "06", "2514", "cliff", "11", "21", "Cliff / falling rocks" },// 14 falling-rocks nearby
			{ "07", "2516", "climbing", "28", "10", "Difficult climbing" },// 16 easy climbing(OC-28), difficult climbing(GC-10)
			{ "08", "2518", "compass", "47", "147", "Compass" }, // OC special
			{ "09", "2520", "cow", "0", "43", "Watch for livestock" },// 20 watch for livestock
			{ "10", "2522", "danger", "9", "23", "Dangerous area" },// 22 dangerous area
			{ "11", "2524", "dogs", "0", "1", "Dogs" },// 24 dogs allowed
			{ "12", "2526", "fee", "36", "2", "Access or parking fee" },// 26 access/parking fees
			// {"13","2528","hiking","0","9","Significant hike"},//28 significant hike : removed
			{ "13", "2528", "hiking", "25", "125", "Long walk" }, // OC special
			{ "14", "2530", "horses", "0", "37", "Horses" },// 30 horses allowed
			{ "15", "2532", "hunting", "12", "22", "Hunting" },// 32 hunting area
			{ "16", "2534", "jeeps", "0", "35", "Off-road vehicles" },// 34 off-road vehicles allowed
			{ "17", "2536", "kids", "59", "6", "Recommended for kids" },// 36 kid friendly
			{ "18", "2538", "mine", "15", "20", "Abandoned mines" },// 38
			{ "19", "2540", "motorcycles", "0", "33", "Motorcycles" },// 40 motorcycles allowed
			{ "20", "2542", "night", "1", "14", "Recommended at night" },// 42 recommended at night
			{ "21", "2544", "onehour", "0", "7", "Takes less than an hour" },// 44 takes less than one hour
			{ "22", "2546", "parking", "18", "25", "Parking available" },// 46 parking available
			{ "23", "2548", "phone", "22", "29", "Telephone nearby" },// 48 telephone nearby
			{ "24", "2550", "picnic", "0", "30", "Picnic tables nearby" },// 50 picnic tables available
			{ "25", "2552", "poisonoak", "16", "17", "Poison plants" },// 52 Giftige Pflanzen
			{ "26", "2554", "public", "19", "26", "Public transportation" },// 54 public transit available
			{ "27", "2556", "quads", "0", "34", "Quads" },// 56 quads allowed
			{ "28", "2558", "rappelling", "49", "3", "Climbing gear" },// 58 climbing gear Kletterausrüstung
			{ "29", "2560", "restrooms", "21", "28", "Public restrooms nearby" },// 60 restrooms available
			{ "30", "2562", "scenic", "0", "8", "Scenic view" },// 62 scenic view
			{ "31", "2564", "scuba", "51", "5", "Scuba gear" },// 64 Tauchausrüstung
			// {"32","2566","snakes","0","18","Snakes"},//66 araber95 replaced by Dangerous Animals 14.08.10
			{ "32", "2566", "dangerousanimals", "0", "18", "Dangerous Animals" },// 66
			{ "33", "2568", "snowmobiles", "0", "36", "Snowmobiles" },// 68
			{ "34", "2570", "stealth", "0", "40", "Stealth required" },// 70 stealth required (Heimlich,List,Schläue)
			{ "35", "2572", "stroller", "0", "41", "Stroller accessible" },// 72 stroller accessible
			{ "36", "2574", "swimming", "29", "12", "May require swimming" },// 74
			{ "37", "2576", "thorn", "13", "39", "Thorns" },// 76 thorns!
			{ "38", "2578", "ticks", "14", "19", "Ticks" },// 78 ticks!
			{ "39", "2580", "wading", "26", "11", "May require wading" },// 80 may require wading
			{ "40", "2582", "water", "20", "27", "Drinking water nearby" },// 82 drinking water nearby
			{ "41", "2584", "wheelchair", "0", "24", "Wheelchair accessible" },// 84 wheelchair accessible
			{ "42", "2586", "winter", "44", "15", "Available during winter" },// 86 available in winter 132 Schneesicheres Versteck
			{ "43", "2588", "firstaid", "0", "42", "Firstaid" }, // GC: Cachewartung notwendig (Auto Attribut) , OC: erste Hilfe
			{ "44", "2590", "flashlight", "48", "44", "Flashlight required" }, // 90 Flashlight required
			{ "45", "2592", "aircraft", "53", "153", "Aircraft" }, // OC special //38 GC removed
			{ "46", "2594", "animals", "17", "0", "" },// 94 Giftige/gef%e4hrliche Tiere
			{ "47", "2596", "arith_prob", "56", "156", "Arithmetical problem" }, // OC special
			{ "48", "2598", "ask", "58", "158", "Ask owner for start conditions" }, // OC special
			{ "49", "2600", "car", "24", "0", "" },// 100 Nahe beim Auto
			{ "50", "2602", "cave", "50", "150", "Cave equipment" }, // OC special
			{ "51", "2604", "date", "42", "142", "All seasons" }, // OC special
			{ "52", "2606", "day", "40", "140", "by day only" }, // OC special
			{ "53", "2608", "indoor", "33", "133", "Within enclosed rooms (caves, buildings etc.)" }, // OC special
			{ "54", "2610", "interestsign", "30", "130", "Point of interest" }, // OC special
			{ "55", "2612", "letter", "8", "108", "Letterbox (needs stamp)" }, // OC special
			{ "56", "2614", "moving", "31", "131", "Moving target" }, // OC special
			{ "57", "2616", "naturschutz", "43", "143", "Breeding season / protected nature" }, // OC special
			{ "58", "2618", "nogps", "35", "135", "Without GPS (letterboxes, cistes, compass juggling ...)" }, // OC special
			{ "59", "2620", "oconly", "6", "106", "Only loggable at Opencaching" },// 120 Nur bei Opencaching logbar
			{ "60", "2622", "othercache", "57", "157", "Other cache type" }, // OC special
			{ "61", "2624", "overnight", "37", "137", "Overnight stay necessary" }, // OC special
			{ "62", "2644", "train", "10", "110", "Active railway nearby" }, // OC special
			{ "63", "2630", "riddle", "55", "0", "" },// 130 Rätsel
			{ "64", "2646", "webcam", "32", "132", "Webcam" }, // OC special
			{ "65", "2634", "steep", "27", "127", "Hilly area" }, // OC special
			{ "66", "2636", "submerged", "34", "134", "In the water" }, // OC special
			{ "67", "2638", "tide", "41", "141", "Tide" }, // OC special
			{ "68", "2640", "time", "39", "139", "Only available at specified times" }, // OC special
			{ "69", "2642", "tools", "46", "0", "Special Tool required" },// 142 Spezielle Ausrüstung
			{ "70", "2648", "wiki", "54", "154", "Investigation" }, // OC special
			{ "71", "2650", "wwwlink", "7", "107", "Hyperlink to another caching portal only" }, // OC special
			{ "72", "2652", "landf", "0", "45", "Lost And Found Tour" }, // thx to Kappler and MiK
			{ "73", "2654", "rv", "0", "46", "Truck Driver/RV" },// changed by Moorteufel 12.07.10
			{ "74", "2656", "field_puzzle", "0", "47", "Field Puzzle" },// changed by Moorteufel 12.07.10
			{ "75", "2658", "uv", "0", "48", "UV Light required" }, // added by araber95 14.8.10
			{ "76", "2660", "snowshoes", "0", "49", "Snowshoes" }, // added by araber95 14.8.10"
			{ "77", "2662", "skiis", "0", "50", "Cross Country Skis" }, // added by araber95 14.8.10
			{ "78", "2664", "s-tool", "0", "51", "Special Tool required" }, // added by araber95 14.8.10
			{ "79", "2666", "nightcache", "0", "52", "Night Cache" }, // added by araber95 14.8.10
			{ "80", "2668", "parkngrab", "0", "53", "Park and grab" }, // added by araber95 14.8.10
			{ "81", "2670", "abandonedbuilding", "0", "54", "Abandoned structure" }, // added by araber95 14.8.10
			{ "82", "2672", "hike_short", "0", "55", "Short hike" }, // added by araber95 14.8.10
			{ "83", "2674", "hike_med", "0", "56", "Medium Hike" }, // added by araber95 14.8.10
			{ "84", "2676", "hike_long", "0", "57", "Long Hike" }, // added by araber95 14.8.10
			{ "85", "2678", "fuel", "0", "58", "Fuel nearby" }, // changed by araber95 14.08.10
			{ "86", "2680", "food", "0", "59", "Food nearby" }, // changed by araber95 14.08.10
			{ "87", "2681", "wirelessbeacon", "0", "60", "Wireless Beacon" }, // added by araber95 27.10.10
			{ "88", "2588", "firstaid", "23", "123", "First aid available" }, // OC special
			{ "89", "2685", "sponsored", "0", "61", "Sponsored Cache" },
	// {"-1","2500","error","0","0",""}, //

	};
	public static int maxAttRef = attRef.length;
	private static String IMAGEDIR = STRreplace.replace(FileBase.getProgramDirectory() + "/attributes/", "//", "/");

	/*
	 * private static String getImageName(int cw_Id, int cw_Inc){ if (cw_Id<0 || cw_Id>maxAttRef) return "error.gif"; else { switch (cw_Inc) { case 1: return attRef[cw_Id][PIC_NAME]+"-yes.gif"; case 0: return attRef[cw_Id][PIC_NAME]+"-no.gif"; case 2:
	 * return attRef[cw_Id][PIC_NAME]+"-non.gif"; default:return "error.gif"; } } }
	 */
	private static String getMsg(int cw_Id, int cw_Inc) {
		if (cw_Id < 0 || cw_Id >= maxAttRef) {
			return MyLocale.getMsg(2500, "error attribute");
		}
		if (cw_Inc == 0)
			return MyLocale.getMsg(Common.parseInt(attRef[cw_Id][MSG_NR]) - 1, "");
		else
			return MyLocale.getMsg(Common.parseInt(attRef[cw_Id][MSG_NR]), "");
	}

	private static mImage[] yesImages = new mImage[maxAttRef];
	private static mImage[] noImages = new mImage[maxAttRef];
	private static mImage[] nonImages = new mImage[maxAttRef];
	private static final mImage errorImage = new mImage(IMAGEDIR + "error.gif");

	/**
	 * Returns the width of the attribute icons
	 * 
	 * @return The width of the images
	 */
	public static int getImageWidth() {
		return errorImage.image.getWidth();
	}

	/**
	 * Get the image for a given attribute number. We use lazy initialisation here, i.e. the images are only loaded when they are requested.
	 * 
	 * @return
	 */
	public mImage getImage() {
		if (_Id < 0 || _Id >= maxAttRef) {
			return errorImage;
		}
		if (_Inc == 1) {
			if (yesImages[_Id] == null) {
				yesImages[_Id] = new mImage(IMAGEDIR + getImageName());
			}
			return yesImages[_Id];
		} else if (_Inc == 0) {
			if (noImages[_Id] == null) {
				noImages[_Id] = new mImage(IMAGEDIR + getImageName());
			}
			return noImages[_Id];
		} else {
			if (nonImages[_Id] == null) {
				nonImages[_Id] = new mImage(IMAGEDIR + getImageName());
			}
			return nonImages[_Id];
		}
	}
}
