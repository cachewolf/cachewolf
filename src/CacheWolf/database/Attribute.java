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
package CacheWolf.database;

import CacheWolf.Preferences;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import ewe.fx.mImage;
import ewe.io.FileBase;

/**
 * This class represents a single attribute
 *
 * @author skg
 */
public class Attribute {
    private final static int BIT_NR = 0;
    private final static int MSG_NR = 1;
    private final static int PIC_NAME = 2;
    private final static int OC_ID = 3; // OC - XML
    private final static int GC_ID = 4; // auch OC neues gpx
    private final static int GC_TEXT = 5; // for export , didn't extract by myself, copied from forum
    private static final String[][] attRef = { //
            {"00", "2502", "available", "A39", "13", "Available at all times"},//
            {"01", "2504", "bicycles", "0", "32", "Bicycles"},//
            {"02", "2506", "boat", "52", "4", "Boat"},//
            // {"03","2508","cactus","0","0",""},//
            {"04", "2510", "campfires", "0", "38", "Campfires"},//
            {"05", "2512", "camping", "0", "31", "Camping available"},//
            {"06", "2514", "cliff", "A61", "21", "Cliff / falling rocks"},//
            {"07", "2516", "climbing", "A24", "10", "Difficult climbing"},//
            {"08", "2518", "compass", "47", "147", "Compass"}, // OC special
            {"09", "2520", "cow", "0", "43", "Watch for livestock"},//
            {"10", "2522", "danger", "A59", "23", "Dangerous area"},//
            {"11", "2524", "dogs", "0", "1", "Dogs"},//
            {"12", "2526", "fee", "36", "2", "Access or parking fee"},//
            {"13", "2528", "hiking", "A21", "9", "Significant hike"},//
            //{ "13", "2528", "hiking", "25", "125", "Long walk" }, // OC special
            {"14", "2530", "horses", "0", "37", "Horses"},//
            {"15", "2532", "hunting", "A62", "22", "Hunting"},//
            {"16", "2534", "jeeps", "0", "35", "Off-road vehicles"},//
            {"17", "2536", "kids", "59", "6", "Recommended for kids"},//
            {"18", "2538", "mine", "A65", "20", "Abandoned mines"},//
            {"19", "2540", "motorcycles", "0", "33", "Motorcycles"},//
            {"20", "2542", "night", "1", "14", "Recommended at night"},//
            {"21", "2544", "onehour", "0", "7", "Takes less than an hour"},//
            {"22", "2546", "parking", "A33", "25", "Parking available"},//
            {"23", "2548", "phone", "A37", "29", "Telephone nearby"},//
            {"24", "2550", "picnic", "0", "30", "Picnic tables nearby"},//
            {"25", "2552", "poisonoak", "A66", "17", "Poison plants"},//
            {"26", "2554", "public", "A34", "26", "Public transportation"},//
            {"27", "2556", "quads", "0", "34", "Quads"},//
            {"28", "2558", "rappelling", "A53", "3", "Climbing gear"},//
            {"29", "2560", "restrooms", "21", "28", "Public restrooms nearby"},//
            {"30", "2562", "scenic", "0", "8", "Scenic view"},//
            {"31", "2564", "scuba", "51", "5", "Scuba gear"},//
            // {"32","2566","snakes","0","18","Snakes"},//replaced by Dangerous Animals 14.08.10
            {"32", "2566", "dangerousanimals", "0", "18", "Dangerous Animals"},//
            {"33", "2568", "snowmobiles", "0", "36", "Snowmobiles"},//
            {"34", "2570", "stealth", "0", "40", "Stealth required"},//
            {"35", "2572", "stroller", "0", "41", "Stroller accessible"},//
            {"36", "2574", "swimming", "29", "12", "May require swimming"},//
            {"37", "2576", "thorn", "A63", "39", "Thorns"},//
            {"38", "2578", "ticks", "A64", "19", "Ticks"},//
            {"39", "2580", "wading", "A22", "11", "May require wading"},//
            {"40", "2582", "water", "20", "27", "Drinking water nearby"},//
            {"41", "2584", "wheelchair", "0", "24", "Wheelchair accessible"},//
            {"42", "2586", "winter", "A47", "15", "Available during winter"},//
            {"43", "2588", "firstaid", "0", "42", "Firstaid"},// GC: Cachewartung notwendig (Auto Attribut) , OC: erste Hilfe
            {"44", "2590", "flashlight", "A52", "44", "Flashlight required"},//
            {"45", "2592", "aircraft", "53", "153", "Aircraft"},// OC special
            {"46", "2594", "animals", "17", "0", ""},// OC ?
            {"47", "2596", "arith_prob", "56", "156", "Arithmetical problem"}, // OC special
            {"48", "2598", "ask", "58", "158", "Ask owner for start conditions"}, // OC special
            {"49", "2600", "car", "24", "0", ""},//
            {"50", "2602", "cave", "50", "150", "Cave equipment"}, // OC special
            {"51", "2604", "date", "A44", "142", "All seasons"}, // OC special
            {"52", "2606", "day", "A41", "140", "by day only"}, // OC special
            {"53", "2608", "indoor", "33", "133", "Within enclosed rooms (caves, buildings etc.)"}, // OC special
            {"54", "2610", "interestsign", "A30", "130", "Point of interest"}, // OC special
            {"55", "2612", "letter", "8", "108", "Letterbox (needs stamp)"}, // OC special
            {"56", "2614", "moving", "31", "131", "Moving target"}, // OC special
            {"57", "2616", "naturschutz", "43", "143", "Breeding season / protected nature"}, // OC special
            {"58", "2618", "nogps", "A58", "135", "Without GPS (letterboxes, cistes, compass juggling ...)"}, // OC special
            {"59", "2620", "oconly", "A1", "106", "Only loggable at Opencaching"},//
            {"60", "2622", "othercache", "57", "157", "Other cache type"}, // OC special
            {"61", "2624", "overnight", "37", "137", "Overnight stay necessary"}, // OC special
            {"62", "2644", "train", "A60", "10", "Active railway nearby"}, // OC special
            {"63", "2630", "riddle", "55", "0", ""},//OC ?
            {"64", "2646", "webcam", "A12", "132", "Webcam"}, // OC special
            {"65", "2634", "steep", "A23", "127", "Hilly area"}, // OC special
            {"66", "2636", "submerged", "34", "134", "In the water"}, // OC special
            {"67", "2638", "tide", "41", "141", "Tide"}, // OC special
            {"68", "2640", "time", "A40", "139", "Only available at specified times"}, // OC special
            {"69", "2642", "tools", "46", "0", "Special Tool required"},//
            {"70", "2648", "wiki", "A14", "154", "Investigation"}, // OC special
            {"71", "2650", "wwwlink", "7", "107", "Hyperlink to another caching portal only"}, // OC special
            {"72", "2652", "landf", "0", "45", "Lost And Found Tour"}, //
            {"73", "2654", "rv", "0", "46", "Truck Driver/RV"}, //
            {"74", "2656", "field_puzzle", "A15", "47", "Field Puzzle"},//
            {"75", "2658", "UV", "0", "48", "UV Light required"}, //
            {"76", "2660", "snowshoes", "0", "49", "Snowshoes"}, //
            {"77", "2662", "skiis", "0", "50", "Cross Country Skis"}, //
            {"78", "2664", "s-tool", "0", "51", "Special Tool required"}, //
            {"79", "2666", "nightcache", "0", "52", "Night Cache"}, //
            {"80", "2668", "parkngrab", "A19", "53", "Park and grab"}, //
            {"81", "2670", "abandonedbuilding", "0", "54", "Abandoned structure"}, //
            {"82", "2672", "hike_short", "0", "55", "Short hike"}, //
            {"83", "2674", "hike_med", "0", "56", "Medium Hike"}, //
            {"84", "2676", "hike_long", "0", "57", "Long Hike"}, //
            {"85", "2678", "fuel", "0", "58", "Fuel nearby"}, //
            {"86", "2680", "food", "0", "59", "Food nearby"}, //
            {"87", "2682", "wirelessbeacon", "0", "60", "Wireless Beacon"}, //
            {"88", "2684", "firstaid", "23", "123", "First aid available"}, // OC special
            {"89", "2686", "partnership", "0", "61", "Partnership Cache"}, // previous : sponsored
            {"90", "2688", "frontyard", "0", "65", "Front Yard (Private Residence)"}, //
            {"91", "2690", "seasonal", "A44", "62", "Seasonal Access"}, //
            {"92", "2692", "teamwork", "0", "66", "Teamwork Required"}, //
            {"93", "2694", "touristOK", "0", "63", "Tourist Friendly"}, //
            {"94", "2696", "treeclimbing", "0", "64", "Tree Climbing"}, //
            {"95", "2698", "geotour", "0", "67", "GeoTour"}, //
            {"96", "2700", "bonuscache", "0", "69", "Bonus cache"}, //
            {"97", "2702", "powertrail", "0", "70", "Power trail"}, //
            {"98", "2704", "challengecache", "0", "71", "Challenge cache"}, //
            {"99", "2706", "hqsolutionchecker", "0", "72", "Geocaching.com solution checker"}, //
            {"100", "2707", "safari", "A72", "0", "Safari Virtual Cache"},
            {"101", "2708", "children", "A71", "0", "Suitable for children (10-12 years)"},
            {"102", "2709", "calendar", "A45", "0", "Only available during specified seasons"}
            // {"-1","2500","error","0","0",""}, //
    };
    private static final String IMAGEDIR = STRreplace.replace(FileBase.getProgramDirectory() + "/attributes/", "//", "/");
    private static final mImage errorImage = new mImage(IMAGEDIR + "error.gif");
    public static int maxAttRef = attRef.length;
    private static final mImage[] yesImages = new mImage[maxAttRef];
    private static final mImage[] noImages = new mImage[maxAttRef];
    private static final mImage[] nonImages = new mImage[maxAttRef];
    private int _Id;
    private int _Inc; // Yes=1 No=0 non=2
    private String _ImageName;
    private long[] _bit = {0L, 0L};

    // Constructors
    private Attribute() {
        //empty
    }

    public Attribute(int id, int inc) {
        _Id = id;
        setInc(inc);
        setIdBit();
    }

    public Attribute(String attributeName) {
        attName2attNo(attributeName);
        setIdBit();
    }

    public Attribute(int attIdGC, String Yes1No0) {
        GCAttNo2attNo(attIdGC, Yes1No0);
        setIdBit();
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
        long[] bit = new long[2]; // a long is inizialized with 0
        if (id > -1 && id < maxAttRef) {
            int b = Common.parseInt(attRef[id][BIT_NR]);
            bit[0] = b > 63 ? 0L : (1L << b);
            bit[1] = b > 63 ? (1L << b - 64) : 0;
        }
        return bit;
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

    /**
     * Returns the width of the attribute icons
     *
     * @return The width of the images
     */
    public static int getImageWidth() {
        return errorImage.image.getWidth();
    }

    public static Attribute FromOcId(final String attIdOC) {
        Attribute result = new Attribute();
        for (int i = 0; i < maxAttRef; i++) {
            if (attRef[i][OC_ID].equals(attIdOC)) {
                result._Id = i;
                result._Inc = 1;
                result._ImageName = attRef[i][PIC_NAME] + "-yes.gif";
                return result;
            }
        }
        result._Id = -1; // Error
        result._ImageName = "error.gif";
        return result;
    }

    // for GC Constructor Spider
    private void attName2attNo(String attributeName) {
        String an = attributeName.substring(0, attributeName.length() - 4);
        for (int i = 0; i < maxAttRef; i++) {
            if (an.startsWith(attRef[i][PIC_NAME] + "-")) {
                _Id = i;
                _Inc = an.endsWith("-no") ? 0 : 1;
                _ImageName = attRef[i][PIC_NAME] + (_Inc == 0 ? "-no.gif" : "-yes.gif");
                return;
            }
        }
        _Id = -1; // Error
        _ImageName = "error.gif";
        Preferences.itself().log("Error converting Attribute " + attributeName);
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

    /**
     * get GC_TEXT string
     */
    public String getGCText() {
        return attRef(_Id, GC_TEXT);
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
     * getting CW internal attribute number (-1..127)
     */
    public int getId() {
        return _Id;
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

    /**
     * Get the image for a given attribute number. We use lazy initialisation here, i.e. the images are only loaded when they are requested.
     *
     * @return ?
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
