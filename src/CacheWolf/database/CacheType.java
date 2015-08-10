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

import CacheWolf.controls.GuiImageBroker;
import CacheWolf.utils.MyLocale;
import ewe.fx.Image;

/**
 * Handles all aspects of converting cache type information
 * from and to the various im- and exporters ...
 * converting legacy profiles to current standard
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
    /** traditional cache (GC,OC) */
    public static final byte CW_TYPE_TRADITIONAL = 2;
    /** multi cache (GC,OC) */
    public static final byte CW_TYPE_MULTI = 3;
    /** virtual cache (GC,OC) */
    public static final byte CW_TYPE_VIRTUAL = 4;
    /** letterbox cache (GC) */
    public static final byte CW_TYPE_LETTERBOX = 5;
    /** event cache (GC,OC) */
    public static final byte CW_TYPE_EVENT = 6;
    /** unknown cache - Mystery (GC) */
    public static final byte CW_TYPE_MYSTERY = 8;
    /** drive in cache (OC) */
    public static final byte CW_TYPE_DRIVE_IN = 10;
    /** webcam cache (GC,OC) */
    public static final byte CW_TYPE_WEBCAM = 11;
    /** locationless cache (GC) */
    public static final byte CW_TYPE_LOCATIONLESS = 12;
    /** CITO cache (GC) */
    public static final byte CW_TYPE_CITO = 13;
    /** Mega Event Cache (GC) */
    public static final byte CW_TYPE_MEGA_EVENT = 100;
    /** WhereIGo Cache (GC) */
    public static final byte CW_TYPE_WHEREIGO = 101;
    /** Earth Cache (GC) */
    public static final byte CW_TYPE_EARTH = 104;
    /** Additional Waypoint Parking (GC) */
    public static final byte CW_TYPE_PARKING = 50;
    /** Additional Waypoint Stage of a Multi (GC) --> Physical Stage*/
    public static final byte CW_TYPE_STAGE = 51;
    /** Additional Waypoint Question to answer (GC) --> Virtual Stage*/
    public static final byte CW_TYPE_QUESTION = 52;
    /** Additional Waypoint Final (GC) */
    public static final byte CW_TYPE_FINAL = 53;
    /** Additional Waypoint Trailhead (GC) */
    public static final byte CW_TYPE_TRAILHEAD = 54;
    /** Additional Waypoint Reference (GC) */
    public static final byte CW_TYPE_REFERENCE = 55;
    /** unrecognized cache type or missing information */
    public static final byte CW_TYPE_ERROR = -1;
    public static final byte CW_TYPE_APE = 102;
    public static final byte CW_TYPE_MAZE = 103;
    public static final byte CW_TYPE_LAB = 105;
    public static final byte CW_TYPE_GIGA_EVENT = 106;
    String[] ggpx = { "Geocache|Custom", "Custom", "Custom", "" };
    private static final byte found = 0;
    private static final byte archived = 1;
    private static final byte disabled = 2;
    private static final byte solved = 3;
    private static final byte bonus = 4;
    private static final byte owned = 5;
    private static final byte dnf = 6;
    //
    private static final CTyp[] cTypRef = {
	    // custom waypoints
	    new CTyp(CW_TYPE_CUSTOM, (byte) 0, 'P', "0", "", "", (byte) -128, 'C', "Custom", new String[] { "Geocache|Custom", "Custom", "Custom", "" }, 1, 10, 0x000100),
	    // Cache waypoints
	    new CTyp(CW_TYPE_TRADITIONAL, (byte) 2, 'C', "2", "2", "2", (byte) -126, 'T', "Tradi", new String[] { "Geocache|Traditional Cache", "Geocache", "Traditional Cache", "Traditional|Classic" }, 2, 0, 0x000001),
	    new CTyp(CW_TYPE_MULTI, (byte) 3, 'C', "3", "3", "3", (byte) -125, 'M', "Multi", new String[] { "Geocache|Multi-cache", "Geocache", "Multi-cache", "Multi|Offset" }, 3, 1, 0x000002),
	    new CTyp(CW_TYPE_VIRTUAL, (byte) 4, 'C', "4", "4", "4", (byte) -124, 'V', "Virtual", new String[] { "Geocache|Virtual Cache", "Geocache", "Virtual Cache", "Virtual" }, 4, 2, 0x000004),
	    new CTyp(CW_TYPE_LETTERBOX, (byte) 5, 'C', "5", "5", "", (byte) -123, 'L', "Letterbox", new String[] { "Geocache|Letterbox Hybrid", "Geocache", "Letterbox Hybrid", "Letterbox" }, 5, 3, 0x000008),
	    new CTyp(CW_TYPE_EVENT, (byte) 6, 'C', "6", "6", "6", (byte) -122, 'X', "Event", new String[] { "Geocache|Event Cache", "Geocache", "Event Cache", "Event" }, 6, 4, 0x000010),
	    new CTyp(CW_TYPE_MEGA_EVENT, (byte) 100, 'C', "453", "453", "", (byte) 101, 'X', "Megaevent", new String[] { "Geocache|Mega-Event Cache", "Geocache", "Mega-Event Cache", "Mega" }, 14, 9, 0x000200),
	    new CTyp(CW_TYPE_WEBCAM, (byte) 11, 'C', "11", "11", "5", (byte) -117, 'W', "Webcam", new String[] { "Geocache|Webcam Cache", "Geocache", "Webcam Cache", "Webcam" }, 11, 5, 0x000020),
	    new CTyp(CW_TYPE_MYSTERY, (byte) 8, 'C', "8", "8", "", (byte) -120, 'U', "Mystery", new String[] { "Geocache|Unknown Cache", "Geocache", "Unknown Cache", "Mystery" }, 8, 6, 0x000040),
	    new CTyp(CW_TYPE_LOCATIONLESS, (byte) 12, 'C', "12", "12", "", (byte) -116, 'O', "Locless", new String[] { "Geocache|Locationless (Reverse) Cache", "Geocache", "Locationless (Reverse) Cache", "Locationless" }, 12, 8, 0x000080),
	    new CTyp(CW_TYPE_CITO, (byte) 13, 'C', "13", "13", "", (byte) -115, 'X', "Cito", new String[] { "Geocache|Cache In Trash Out Event", "Geocache", "Cache In Trash Out Event", "CITO" }, 13, 17, 0x020000),
	    new CTyp(CW_TYPE_EARTH, (byte) 104, 'C', "137", "137", "", (byte) 9, 'E', "Earth", new String[] { "Geocache|Earthcache", "Geocache", "Earthcache", "Earth" }, 18, 7, 0x000400),
	    new CTyp(CW_TYPE_WHEREIGO, (byte) 101, 'C', "1858", "1858", "", (byte) 100, 'G', "Whereigo", new String[] { "Geocache|Wherigo Cache", "Geocache", "Wherigo Cache", "Wherigo" }, 15, 18, 0x040000),
	    // todo definitions for Filter and Filterselection for APE, MAZE, ...
	    new CTyp(CW_TYPE_APE, (byte) 102, 'C', "", "9", "", (byte) -1, 'A', "Ape", new String[] { "Geocache|Project APE Cache", "Geocache", "Project APE Cache", "APE" }, 16, 19, 0x080000),
	    new CTyp(CW_TYPE_MAZE, (byte) 103, 'C', "", "1304", "", (byte) -1, 'Z', "Maze", new String[] { "Geocache|GPS Adventures Exhibit", "Geocache", "GPS Adventures Exhibit", "MAZE" }, 17, 20, 0x100000),
	    new CTyp(CW_TYPE_GIGA_EVENT, (byte) 106, 'C', "", "7005", "", (byte) -1, 'X', "Gigaevent", new String[] { "Geocache|Giga-Event Cache", "Geocache", "Giga-Event Cache", "Giga" }, 22, 9, 0x000200),
	    // additional waypoints
	    new CTyp(CW_TYPE_PARKING, (byte) 50, 'A', "50", "Parking Area", "", (byte) -78, 'P', "Parking", new String[] { "Waypoint|Parking Area", "Parking Area", "Parking Area", "" }, 50, 11, 0x000800),
	    new CTyp(CW_TYPE_STAGE, (byte) 51, 'A', "51", "Physical Stage", "", (byte) -77, 'S', "Stage", new String[] { "Waypoint|Physical Stage", "Physical Stage", "Physical Stage", "" }, 51, 12, 0x001000),
	    new CTyp(CW_TYPE_QUESTION, (byte) 52, 'A', "52", "Virtual Stage", "", (byte) -76, 'Q', "Question", new String[] { "Waypoint|Virtual Stage", "Virtual Stage", "Virtual Stage", "" }, 52, 13, 0x002000),
	    new CTyp(CW_TYPE_FINAL, (byte) 53, 'A', "53", "Final Location", "", (byte) -75, 'F', "Final", new String[] { "Waypoint|Final Location", "Final Location", "Final Location", "" }, 53, 14, 0x004000),
	    new CTyp(CW_TYPE_TRAILHEAD, (byte) 54, 'A', "54", "Trailhead", "", (byte) -74, 'H', "Trailhead", new String[] { "Waypoint|Trailhead", "Trailhead", "Trailhead", "" }, 54, 15, 0x008000),
	    new CTyp(CW_TYPE_REFERENCE, (byte) 55, 'A', "55", "Reference Point", "", (byte) -73, 'R', "Reference", new String[] { "Waypoint|Reference Point", "Reference Point", "Reference Point", "" }, 55, 16, 0x010000),
	    // perhaps for gpx - compatibility
	    new CTyp(CW_TYPE_STAGE, CW_TYPE_STAGE, 'A', "51", "Physical Stage", "", (byte) -77, 'S', "Stage", new String[] { "Waypoint|Stages of a Multicache", "Stages of a Multicache", "Stages of a Multicache", "" }, 51, 12, 0x001000),
	    new CTyp(CW_TYPE_QUESTION, CW_TYPE_QUESTION, 'A', "52", "Virtual Stage", "", (byte) -76, 'Q', "Question", new String[] { "Waypoint|Question to Answer", "Question to Answer", "Question to Answer", "" }, 52, 13, 0x002000),
	    // error on waypoint
	    new CTyp(CW_TYPE_ERROR, CW_TYPE_ERROR, 'E', "", "", "", (byte) -1, '-', "guiError", new String[] { "", "", "", "" }, 49, -1, 0),
	    // mapped types (recognized on input from gpx or download-spider / or cw - version)
	    new CTyp(CW_TYPE_MYSTERY, (byte) 1, 'C', "", "", "1", (byte) -1, 'U', "", new String[] { "Geocache|Other", "Geocache", "Other", "Other" }, 21, -1, 0),
	    new CTyp(CW_TYPE_MYSTERY, (byte) 7, 'C', "7", "", "7", (byte) -121, 'U', "", new String[] { "Geocache|Quiz", "Geocache", "Quiz", "Quiz" }, 7, -1, 0),
	    new CTyp(CW_TYPE_MYSTERY, (byte) 9, 'C', "9", "", "9", (byte) -119, 'U', "", new String[] { "Geocache|Moving", "Geocache", "Moving", "Moving" }, 9, -1, 0),
	    new CTyp(CW_TYPE_LAB, (byte) 105, 'P', "0", "", "", (byte) -1, 'Z', "Lab", new String[] { "Geocache|Lab Cache", "Geocache", "Geocache|Lab Cache", "LAB" }, 23, 0, 0x000001),
	    new CTyp(CW_TYPE_LAB, (byte) 0, 'P', "0", "", "", (byte) -1, 'Z', "Lab", new String[] { "Groundspeak Lost and Found Celebration", "Geocache", "Groundspeak Lost and Found Celebration", "LAB" }, 2, 0, 0x000001),
	    new CTyp(CW_TYPE_TRADITIONAL, (byte) 10, 'C', "10", "", "10", (byte) -118, 'U', "", new String[] { "Geocache|DriveIn", "Geocache", "DriveIn", "DriveIn" }, 10, -1, 0),
	    new CTyp(CW_TYPE_EVENT, (byte) 14, 'C', "", "3653", "", (byte) -1, 'X', "", new String[] { "Geocache|Lost and Found Event Cache", "Geocache", "Lost and Found Event Cache", "" }, 6, -1, 0),
	    new CTyp(CW_TYPE_MYSTERY, (byte) 108, 'P', "", "", "8", (byte) -1, 'U', "", new String[] { "only on OC download", "", "", "" }, 19, -1, 0),
	    new CTyp(CW_TYPE_WHEREIGO, (byte) 15, 'P', "", "", "", (byte) -62, 'G', "", new String[] { "Hack for V2 Typ", "", "", "" }, -1, -1, 0), };
    // public static final int anzCacheTyps=cTypRef.length;
    public static final byte maxCWCType = 110;
    static final byte[] Ref_Index = new byte[maxCWCType];
    static {
	// +1 cause error is -1 and array starts at 0
	for (byte i = (byte) (cTypRef.length - 1); i >= 0; i--) {
	    Ref_Index[1 + cTypRef[i]._cwCType] = i;
	}
    }

    public static byte Ref_Index(final byte type) {
	final byte ret = Ref_Index[cTypRef[Ref_Index[type + 1]]._cwMappedCType + 1];
	return ret;
    }

    /**
     * check if a given waypoint type is an additional waypoint
     * 
     * @param type
     *            waypoint type to check
     * @return true if it is an additional waypoint, false otherwise
     */
    public static boolean isAddiWpt(final byte type) {
	return cTypRef[Ref_Index(type)]._cwCGroup == 'A';
    }

    /**
     * check if a given waypoint type is an cache waypoint
     * 
     * @param type
     *            waypoint type to check
     * @return true if it is an Cache waypoint, false otherwise
     */
    public static boolean isCacheWpt(final byte type) {
	return cTypRef[Ref_Index(type)]._cwCGroup == 'C';
    }

    /**
     * check if a given waypoint type is an Custom waypoint
     * 
     * @param type
     *            waypoint type to check
     * @return true if it is an Custom waypint, false otherwise
     */
    public static boolean isCustomWpt(final byte type) {
	return cTypRef[Ref_Index(type)]._cwCGroup == 'P';
    }

    public static byte[] guiOrder = { CacheType.CW_TYPE_CUSTOM, CacheType.CW_TYPE_TRADITIONAL, CacheType.CW_TYPE_MULTI, CacheType.CW_TYPE_VIRTUAL, CacheType.CW_TYPE_LETTERBOX //
	    , CacheType.CW_TYPE_EVENT, CacheType.CW_TYPE_MEGA_EVENT, CacheType.CW_TYPE_WEBCAM, CacheType.CW_TYPE_MYSTERY, CacheType.CW_TYPE_LOCATIONLESS //
	    , CacheType.CW_TYPE_CITO, CacheType.CW_TYPE_EARTH, CacheType.CW_TYPE_WHEREIGO, CacheType.CW_TYPE_APE, CacheType.CW_TYPE_MAZE //
	    , CacheType.CW_TYPE_GIGA_EVENT, CacheType.CW_TYPE_LAB //
	    , CacheType.CW_TYPE_PARKING, CacheType.CW_TYPE_STAGE, CacheType.CW_TYPE_QUESTION, CacheType.CW_TYPE_FINAL, CacheType.CW_TYPE_TRAILHEAD, CacheType.CW_TYPE_REFERENCE //
    };

    //

    // done for DetailsPanel.java and KML- and TomTom-Exporter
    /**
     * create list of cache types to be shown in GUI drop down lists
     * 
     * @return list of cache types to be shown in GUI drop down list
     * @see guiSelect2Cw
     * @see cw2GuiSelect
     */
    public static String[] guiTypeStrings() {
	final String[] ret = new String[guiOrder.length];
	for (int i = 0; i < guiOrder.length; i++) {
	    ret[i] = MyLocale.getMsg(cTypRef[Ref_Index(guiOrder[i])]._msgNrCTypeName, "");
	}
	return ret;
    }

    /**
     * translate GUI drop down index selection back to internally stored type
     * 
     * @param selection
     *            index value from drop down list
     * @return internal type
     * @throws IllegalArgumentException
     *             if <code>selection</code> can not be matched
     * @see guiTypeStrings
     * @see cw2GuiSelect
     */
    public static byte guiSelect2Cw(final int selection) {
	return guiOrder[selection];
    }

    /**
     * translate cache type to position of index to highlight in GUI cache type drop down list
     * 
     * @param typeId
     *            internal id of cache type
     * @return index of the cache type in GUI list
     * @throws IllegalArgumentException
     *             if <code>id</code> can not be matched
     * @see guiTypeStrings
     * @see guiSelect2Cw
     */
    public static int cw2GuiSelect(final byte typeId) {
	for (int i = 0; i < guiOrder.length; i++) {
	    if (guiOrder[i] == typeId)
		return i;
	}
	return -1;
    }

    /**
     * convert the strings found in import of GPX from GC, OC or TC to internal cache type
     * 
     * @param gpxType
     *            type information found in GPX
     * @return internal cache type
     */
    public static byte gpxType2CwType(final String gpxType) throws IllegalArgumentException {
	for (byte i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._gpxWptTypeTag.equalsIgnoreCase(gpxType)) {
		return cTypRef[i]._cwMappedCType;
	    }
	    ;
	}
	for (byte i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._gpxWptGCextensionTypTag.equalsIgnoreCase(gpxType)) {
		return cTypRef[i]._cwMappedCType;
	    }
	    ;
	}
	final String lowerCaseGPXType = gpxType.toLowerCase();
	for (byte i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._gpxAlternativeWptTypTags.toLowerCase().indexOf(lowerCaseGPXType) != -1) {
		return cTypRef[i]._cwMappedCType;
	    }
	    ;
	}
	// TODO extend definition of _gpxAlternativeWptTypTags for all cases of Mystery
	// old code was : if (!(gpxType.indexOf("Mystery")==-1)) return CW_TYPE_UNKNOWN;
	return -1;
    }

    /**
     * convert the cache type information from an OC XML import to internal cache type
     * 
     * @param ocType
     *            cache type found in OC XML
     * @return internal cache type
     * @throws IllegalArgumentException
     *             if <code>ocType</code> can not be matched
     */
    public static byte ocType2CwType(final String ocType) {
	for (int i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._ocCTypeXmlImport.equals(ocType)) {
		return cTypRef[i]._cwMappedCType;
	    }
	}
	return -1;
    }

    /**
     * convert type information discovered by GC spider to internal type information
     * 
     * @param gcType
     *            type information from GC spider
     * @return internal representation of cache type
     * @throws IllegalArgumentException
     *             if <code>gcType</code> can not be matched
     */
    public static byte gcSpider2CwType(final String gcType) {
	for (int i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._gcCTypeSpider.equals(gcType)) {
		return cTypRef[i]._cwMappedCType;
	    }
	}
	return -1;
    }

    /**
     * translate cache type to a short version for compact exporters or "smart" cache names.
     * 
     * @param typeId
     *            CacheWolf internal type information
     * @return abbreviation of cache type
     */
    public static String getExportShortId(final byte typeId) {
	return "" + cTypRef[Ref_Index(typeId)]._gpxShortCType;
    }

    /**
     * map cache types to images
     * 
     * @param typeId
     *            internal cache type id
     * @return name of image with extension
     */
    public static String typeImageForId(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._imageName + ".png";
    }

    /**
     * map cache types to images
     * 
     * @param typeId
     *            internal cache type id
     * @return name of image without extension
     */
    public static String typeImageNameForId(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._imageName;
    }

    /**
     * generate type description matching those of GC for GPX export
     * 
     * @param typeId
     *            internal type id
     * @return type information in GC.com <type> GPX format
     */
    public static String type2TypeTag(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._gpxWptTypeTag;
    }

    /**
     * generate type description matching those of GC for GPX export
     * 
     * @param typeId
     *            internal type id
     * @return symb information in GC.com <sym> GPX format
     */
    public static String type2SymTag(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._gpxWptSymTag;
    }

    /**
     * generate type description matching those of GC for GPX export
     * 
     * @param typeId
     *            internal type id
     * @return type information in GC.com <groundspeak:type> GPX format
     */
    public static String type2GSTypeTag(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._gpxWptGCextensionTypTag;
    }

    /**
     * generate type description matching those of GC for GPX export
     * 
     * @param typeId
     *            internal type id
     * @return Gui - string for type
     */
    public static String type2Gui(final byte typeId) {
	return MyLocale.getMsg(cTypRef[Ref_Index(typeId)]._msgNrCTypeName, "");
    }

    /**
     * select image to be displayed for a given cache type
     * 
     * @param typeId
     *            internal cache type id
     * @return <code>Image</code> object to be displayed
     */
    public static Image getTypeImage(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._iconImage;
    }

    /**
     * select image to be displayed for a given cache type
     * 
     * @param typeId
     *            internal cache type id
     * @return <code>Image</code> object to be displayed
     */
    public static Image getBigCacheIcon(CacheHolder ch) {
	byte typeId = ch.getType();
	Image im = cTypRef[Ref_Index(typeId)]._mapImage;
	if (ch.isFound()) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[found] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[found] = newOverlayedImage(im, GuiImageBroker.found);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[found];
	} else if (ch.isArchived()) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[archived] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[archived] = newOverlayedImage(im, GuiImageBroker.archived);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[archived];
	} else if (!ch.isAvailable()) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[disabled] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[disabled] = newOverlayedImage(im, GuiImageBroker.disabled);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[disabled];
	} else if (ch.isOwned()) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[owned] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[owned] = newOverlayedImage(im, GuiImageBroker.owned);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[owned];
	} else if (ch.isSolved()) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[solved] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[solved] = newOverlayedImage(im, GuiImageBroker.solved);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[solved];
	} else if (ch.getStatus().indexOf(MyLocale.getMsg(319, "Not Found")) > -1) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[dnf] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[dnf] = newOverlayedImage(im, GuiImageBroker.dnf);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[dnf];
	} else if (ch.getName().toLowerCase().indexOf("bonus") > -1) {
	    if (cTypRef[Ref_Index(typeId)]._modImage[bonus] == null) {
		cTypRef[Ref_Index(typeId)]._modImage[bonus] = newOverlayedImage(im, GuiImageBroker.bonus);
	    }
	    im = cTypRef[Ref_Index(typeId)]._modImage[bonus];
	}
	return im;
    }

    // TODO do it better in Version 4
    public static int getCacheTypePattern(final byte typeId) {
	return cTypRef[Ref_Index(typeId)]._FilterPattern;
    }

    public static int Type_FilterString2Type_FilterPattern(final String Type_FilterString) {
	int typeMatchPattern = 0;
	for (int i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._FilterStringPos > -1) {
		if (Type_FilterString.charAt(cTypRef[i]._FilterStringPos) == '1') {
		    typeMatchPattern |= cTypRef[i]._FilterPattern;
		}
	    }
	}
	return typeMatchPattern;
    }

    public static boolean hasTypeMatchPattern(final int typeMatchPattern) {
	return typeMatchPattern != 0;
    }

    public static boolean hasMainTypeMatchPattern(final int typeMatchPattern) {
	int TYPE_MAIN = 0;
	for (int i = 0; i < cTypRef.length; i++) {
	    if (cTypRef[i]._cwCGroup == 'C' || cTypRef[i]._cwCGroup == 'P') {
		TYPE_MAIN |= cTypRef[i]._FilterPattern;
	    }
	}
	return (typeMatchPattern & TYPE_MAIN) != 0;
    }

    private static Image newOverlayedImage(Image imsrc, Image imovl) {
	// Overlay added at topleft
	int srcWidth = imsrc.getWidth();
	int srcHeight = imsrc.getHeight();
	int ovlWidth = imovl.getWidth();
	int ovlHeight = imovl.getHeight();
	if (srcWidth < ovlWidth || srcHeight < ovlHeight)
	    return imsrc;
	int[] srcPixels = imsrc.getPixels(null, 0, 0, 0, srcWidth, srcHeight, 0);
	int[] ovlPixels = imovl.getPixels(null, 0, 0, 0, ovlWidth, ovlHeight, 0);
	int offsrc;
	int offovl = 0;
	for (int y = 0; y < ovlHeight; y++) { // top down
	    offsrc = y * srcWidth;
	    for (int x = 0; x < ovlWidth; x++) {
		int alphaval = (ovlPixels[offovl] >> 24) & 0xff;
		if (alphaval > 0) {
		    srcPixels[offsrc] = ovlPixels[offovl];
		}
		offovl++;
		offsrc++;
	    }
	}
	Image modImage = new Image(imsrc, 0);
	modImage.setPixels(srcPixels, 0, 0, 0, srcWidth, srcHeight, 0);
	return modImage;
    }

    public static int getLogMsgNr(byte type) {
	int msgNr = 318; // normal found
	if (type == CW_TYPE_WEBCAM) {
	    msgNr = 361;
	} else if (type == CW_TYPE_EVENT || type == CW_TYPE_MEGA_EVENT || type == CW_TYPE_MAZE) {
	    msgNr = 355;
	}
	return msgNr;
    }

    public static String getFoundText(byte type) {
	return MyLocale.getMsg(CacheType.getLogMsgNr(type), "Found");
    }

}

final class CTyp {
    public byte _cwMappedCType; // CW Cache Typ intern
    public byte _cwCType; // CW Cache Typ intern
    public char _cwCGroup; // Cache Typ Group intern
    public String _cwCTypeV1; // V1 Cache Typ
    public String _gcCTypeSpider; // GC Type on Spider Import from GC.com
    public String _ocCTypeXmlImport; // GC Type on Spider Import from GC.com
    public byte _cwCTypeV2; // V2 Cache Typ
    public char _gpxShortCType; // Short Typ (one char abbreviation)
    public String _imageName; // name of imageName for Icon, "showCacheInBrowser" and "KML Export"
    public String _gpxWptTypeTag; // gpx wpt <type> tag
    public String _gpxWptSymTag; // gpx wpt <sym> tag
    public String _gpxWptGCextensionTypTag; // gpx cache extension <groundspeak:type> tag
    public String _gpxAlternativeWptTypTags; // alternative typ - names for gpx from other sources
    public int _msgNrCTypeName; // message number for gui cache Typ name
    public int _FilterStringPos; // BitNr in Filter String (profile)
    public int _FilterPattern; // 2**BitNr in Filter int (does not correspond with BitNr in String)
    public Image _iconImage;
    public Image _mapImage;
    public Image[] _modImage = { null, null, null, null, null, null, null };

    /**
     * 
     * @param cwMappedCType CW Cache Typ intern
     * @param cwCType CW Cache Typ intern
     * @param cwCGroup Cache Typ Group intern
     * @param cwCTypeV1 V1 Cache Typ
     * @param gcCTypeSpider GC Type on Spider Import from GC.com
     * @param ocCTypeXmlImport GC Type on Spider Import from GC.com
     * @param cwCTypeV2 V2 Cache Typ
     * @param gpxShortCType Short Typ (one char abbreviation)
     * @param imageName name of imageName for Icon, "showCacheInBrowser" and "KML Export"
     * @param gpx gpx wpt <type> tag,  gpx wpt <sym> tag, gpx cache extension <groundspeak:type> tag, alternative typ - names for gpx from other sources
     * @param msgNrCTypeName message number for gui cache Typ name
     * @param filterStringPos BitNr in Filter String (profile)
     * @param filterPattern 2**BitNr in Filter int (does not correspond with BitNr in String)
     */
    public CTyp(byte cwMappedCType, byte cwCType, char cwCGroup, String cwCTypeV1, String gcCTypeSpider, String ocCTypeXmlImport, byte cwCTypeV2, char gpxShortCType, String imageName, String[] gpx, int msgNrCTypeName, int filterStringPos,
	    int filterPattern) {

	_cwMappedCType = cwMappedCType;
	_cwCType = cwCType;
	_cwCGroup = cwCGroup;
	_cwCTypeV1 = cwCTypeV1;
	_gcCTypeSpider = gcCTypeSpider;
	_ocCTypeXmlImport = ocCTypeXmlImport;
	_cwCTypeV2 = cwCTypeV2;
	_gpxShortCType = gpxShortCType;
	_imageName = imageName;
	_gpxWptTypeTag = gpx[0]; // <type>Waypoint|Physical Stage</type>
	_gpxWptSymTag = gpx[1]; //  <type>Geocache</type> Loc - Files
	_gpxWptGCextensionTypTag = gpx[2]; // <sym>Physical Stage</sym>
	_gpxAlternativeWptTypTags = gpx[3];
	_msgNrCTypeName = msgNrCTypeName;
	_FilterStringPos = filterStringPos;
	_FilterPattern = filterPattern;
	if (!_imageName.equals("")) {
	    _iconImage = GuiImageBroker.getCacheTypeImage(_imageName);
	    _mapImage = GuiImageBroker.getMapCacheTypeImage(_imageName);
	}
    }
}
