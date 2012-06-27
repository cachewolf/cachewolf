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
package CacheWolf;

/**
 * Handles all aspects of converting cache size information from
 * and to the various im- and exporters as well as for converting
 * legacy profiles to current standard
 */
public final class CacheSize {

	/*
	 * internal representation of cache sizes in CacheHolder we just made them
	 * up ;-)
	 */
	/** container size not chosen */
	public static final byte CW_SIZE_NOTCHOSEN = 0;
	/** container size other */
	public static final byte CW_SIZE_OTHER = 1;
	/** container size micro */
	public static final byte CW_SIZE_MICRO = 2;
	/* container size small */
	public static final byte CW_SIZE_SMALL = 3;
	/** container size regular */
	public static final byte CW_SIZE_REGULAR = 4;
	/** container size large */
	public static final byte CW_SIZE_LARGE = 5;
	/** container size very large */
	public static final byte CW_SIZE_VERYLARGE = 6;
	/** container size none */
	public static final byte CW_SIZE_NONE = 7;
	/** container size virtual */
	public static final byte CW_SIZE_VIRTUAL = 8;
	/** unparsable size or missing size information should throw IllegalArgumentExceptions when found */
	public static final byte CW_SIZE_ERROR = -1;

	/*
	 * geocaching.com size strings as found by analyzing GPX files 
	 * plus opencaching & terracaching Very large + none
	 */
	public static final String GC_SIZE_MICRO = "Micro";
	public static final String GC_SIZE_SMALL = "Small";
	public static final String GC_SIZE_REGULAR = "Regular";
	public static final String GC_SIZE_MEDIUM= "Medium";
	public static final String GC_SIZE_LARGE = "Large";
	public static final String GC_SIZE_NOTCHOSEN = "Not chosen";
	public static final String GC_SIZE_OTHER = "Other";
	public static final String GC_SIZE_VIRTUAL = "Virtual";
	public static final String OCTC_SIZE_VERYLARGE = "Very large";
	public static final String OCTC_SIZE_NONE = "None";

	/*
	 * OpenCaching Size IDs see
	 * http://oc-server.svn.sourceforge.net/viewvc/oc-server/doc/sql/static-data/data.sql?view=markup
	 */
	public static final String OC_SIZE_OTHER = "1";
	public static final String OC_SIZE_MICRO = "2";
	public static final String OC_SIZE_SMALL = "3";
	public static final String OC_SIZE_NORMAL = "4";
	public static final String OC_SIZE_LARGE = "5";
	public static final String OC_SIZE_VERYLARGE = "6";
	public static final String OC_SIZE_NONE = "7";

	/*
	 * TerraCaching Size IDs taken from old GPXimporter (?? reliable source ??)
	 */
	public static final String TC_SIZE_MICRO = "1";
	public static final String TC_SIZE_MEDIUM = "2";
	public static final String TC_SIZE_REGULAR = "3";
	public static final String TC_SIZE_LARGE = "4";
	public static final String TC_SIZE_VERYLARGE = "4";

	/*
	 * images to show in CW index panel we use less images than sizes since all
	 * non physical caches are represented by the same symbol
	 */
	/** GUI image for micro caches */
	public static final String CW_GUIIMG_MICRO = "sizeMicro.png";
	/** GUI image for small caches */
	public static final String CW_GUIIMG_SMALL = "sizeSmall.png";
	/** GUI image for regular / normal caches */
	public static final String CW_GUIIMG_NORMAL = "sizeReg.png";
	/** GUI image for large caches */
	public static final String CW_GUIIMG_LARGE = "sizeLarge.png";
	/** GUI image for non physical caches */
	public static final String CW_GUIIMG_NONPHYSICAL = "sizeNonPhysical.png";
	/** GUI image for very large caches */
	public static final String CW_GUIIMG_VERYLARGE = "sizeVLarge.png";

	/*
	 * IDs for the sizePics[] array in TableModel therefore they must start with
	 * 0 and be consecutive
	 */
	public static final byte CW_GUIIMGID_MICRO = 0;
	public static final byte CW_GUIIMGID_SMALL = 1;
	public static final byte CW_GUIIMGID_NORMAL = 2;
	public static final byte CW_GUIIMGID_LARGE = 3;
	public static final byte CW_GUIIMGID_NONPHYSICAL = 4;
	public static final byte CW_GUIIMGID_VERYLARGE = 5;

	/*
	 * total number of different size images will be used to set the dimension
	 * of sizePics[] array in TableModel
	 */
	public static final byte CW_TOTAL_SIZE_IMAGES = 6;

	/*
	 * bit masks to be used with the filter function
	 */
	public static final byte CW_FILTER_MICRO = 0x01 << 0;
	public static final byte CW_FILTER_SMALL = 0x01 << 1;
	public static final byte CW_FILTER_NORMAL = 0x01 << 2;
	public static final byte CW_FILTER_LARGE = 0x01 << 3;
	public static final byte CW_FILTER_VERYLARGE = 0x01 << 4;
	public static final byte CW_FILTER_NONPHYSICAL = 0x01 << 5;
	public static final byte CW_FILTER_ALL = CW_FILTER_MICRO
			| CW_FILTER_SMALL | CW_FILTER_NORMAL | CW_FILTER_LARGE
			| CW_FILTER_NONPHYSICAL | CW_FILTER_VERYLARGE;
	
	/** thou shallst not instantiate this object */
	private CacheSize() { // Nothing to do
	}

	
	/**
	 * map filenames of images for the different sizes to the ids used array
	 * index for sizePics[] in TableModel
	 * 
	 * @param size
	 *            size identifier matching the CW_GUIIMGID_ constants
	 * @return filename of image to be displayed for id
	 * @throws IllegalArgumentException
	 *             if there is no image associated to the <code>id</code>
	 */
	public static String sizeImageForId(final byte size) throws IllegalArgumentException {
		switch (size) {
			case CW_GUIIMGID_MICRO:
				return CW_GUIIMG_MICRO;
			case CW_GUIIMGID_SMALL:
				return CW_GUIIMG_SMALL;
			case CW_GUIIMGID_NORMAL:
				return CW_GUIIMG_NORMAL;
			case CW_GUIIMGID_LARGE:
				return CW_GUIIMG_LARGE;
			case CW_GUIIMGID_NONPHYSICAL:
				return CW_GUIIMG_NONPHYSICAL;
			case CW_GUIIMGID_VERYLARGE:
				return CW_GUIIMG_VERYLARGE;
			default:
				throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize cw2ExportString()"));
		}
	}

	/**
	 * convert the size info from a CacheHolder to a string suitable for GPX
	 * export
	 * 
	 * @param size
	 *            CW internal representation of cache size
	 * @return string representation of CacheWolf internal cache size
	 * @throws IllegalArgumentException
	 *             if <code>cwsize</code> can not be mapped to a CW_SIZE constant
	 */
	public static String cw2ExportString(final byte size) throws IllegalArgumentException {
		switch (size) {
			case CW_SIZE_MICRO:
				return GC_SIZE_MICRO;
			case CW_SIZE_SMALL:
				return GC_SIZE_SMALL;
			case CW_SIZE_REGULAR:
				return GC_SIZE_REGULAR;
			case CW_SIZE_LARGE:
				return GC_SIZE_LARGE;
			case CW_SIZE_NOTCHOSEN:
				return GC_SIZE_NOTCHOSEN;
			case CW_SIZE_OTHER:
				return GC_SIZE_OTHER;
			case CW_SIZE_VIRTUAL:
				return GC_SIZE_VIRTUAL;
			case CW_SIZE_VERYLARGE:
				return OCTC_SIZE_VERYLARGE;
			case CW_SIZE_NONE:
				return OCTC_SIZE_NONE;
			default:
				throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize cw2ExportString()"));
		}
	}

	/**
	 * convert the cache size information from a TerraCaching GPX import to
	 * internal representation
	 * 
	 * @param tcstring
	 *            size information extracted from a TC GPX import
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if <code>tcstring</code> can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */

	public static byte tcGpxString2Cw(final String tcstring) throws IllegalArgumentException {
		if (tcstring.equals(TC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (tcstring.equals(TC_SIZE_MEDIUM)) {
			return CW_SIZE_SMALL;
		} else if (tcstring.equals(TC_SIZE_REGULAR)) {
			return CW_SIZE_REGULAR;
		} else if (tcstring.equals(TC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (tcstring.equals(TC_SIZE_VERYLARGE)) {
			return CW_SIZE_VERYLARGE;
		} else {
			throw (new IllegalArgumentException("unmatched argument " + tcstring + " in CacheSize tcGpxString2Cw()"));
		}
	}

	/**
	 * convert the cache size information from a GC GPX import to internal
	 * representation
	 * 
	 * @param gcstring
	 *            size information extracted from a GPX import
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if <code>gcstring</code> can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */

	public static byte gcGpxString2Cw(final String gcstring) throws IllegalArgumentException {
		if (gcstring.equalsIgnoreCase(GC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_SMALL)) {
			return CW_SIZE_SMALL;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_REGULAR)||gcstring.equalsIgnoreCase(GC_SIZE_MEDIUM)) {
			return CW_SIZE_REGULAR;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_NOTCHOSEN)) {
			return CW_SIZE_NOTCHOSEN;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_OTHER)) {
			return CW_SIZE_OTHER;
		} else if (gcstring.equalsIgnoreCase(GC_SIZE_VIRTUAL)) {
			return CW_SIZE_VIRTUAL;
		// GSAK exports wrong type information
		} else if (gcstring.equalsIgnoreCase("Unknown")) {
			return CW_SIZE_NOTCHOSEN;
		} else if (gcstring.equalsIgnoreCase("not_chosen")) {
			return CW_SIZE_NOTCHOSEN;
		} else {
			throw (new IllegalArgumentException("unmatched argument " + gcstring + " in CacheSize gcGpxString2Cw()"));
		}
	}

	/**
	 * convert the cache size information from GCSpider to internal
	 * representation for CacheHolder
	 * 
	 * @param spiderstring
	 *            string identified by the spider as containing size information
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if <code>spiderstring</code> can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */
	public static byte gcSpiderString2Cw(final String spiderstring) throws IllegalArgumentException {
		// at the moment both sources use the same strings
		return gcGpxString2Cw(spiderstring);
	}

	/**
	 * map information from an Opencaching XML cache description suitable for
	 * CacheHolder
	 * 
	 * @param ocxmlstring
	 *            string extracted from OC-XML attribute size
	 * @return CacheWolf internal representation of size information
	 * @trows IllegalArgumentException if <code>ocxmlstring</code> can not be mapped to a
	 *        CW_SIZE_*
	 */
	public static byte ocXmlString2Cw(final String ocxmlstring) throws IllegalArgumentException {
		if (ocxmlstring.equals(OC_SIZE_OTHER)) {
			return CW_SIZE_OTHER;
		} else if (ocxmlstring.equals(OC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (ocxmlstring.equals(OC_SIZE_SMALL)) {
			return CW_SIZE_SMALL;
		} else if (ocxmlstring.equals(OC_SIZE_NORMAL)) {
			return CW_SIZE_REGULAR;
		} else if (ocxmlstring.equals(OC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (ocxmlstring.equals(OC_SIZE_VERYLARGE)) {
			return CW_SIZE_VERYLARGE;
		} else if (ocxmlstring.equals(OC_SIZE_NONE)) {
			return CW_SIZE_NOTCHOSEN;
		} else {
			throw (new IllegalArgumentException("unmatched argument " + ocxmlstring + " in CacheSize ocXmlString2Cw()"));
		}
	}

	/**
	 * get name of the image to be displayed in CW index panel
	 * 
	 * @param size
	 *            CW internal representation of cache size
	 * @return filename of image to be displayed in main panel as size icon
	 * @throws IllegalArgumentException
	 *             if <code>size</code> can not be mapped
	 */
	public static byte guiSizeImageId(final byte size) throws IllegalArgumentException {
		switch (size) {
			case CW_SIZE_MICRO:
				return CW_GUIIMGID_MICRO;
			case CW_SIZE_SMALL:
				return CW_GUIIMGID_SMALL;
			case CW_SIZE_REGULAR:
				return CW_GUIIMGID_NORMAL;
			case CW_SIZE_LARGE:
				return CW_GUIIMGID_LARGE;
			case CW_SIZE_NOTCHOSEN:
				return CW_GUIIMGID_NONPHYSICAL;
			case CW_SIZE_OTHER:
				return CW_GUIIMGID_NONPHYSICAL;
			case CW_SIZE_VIRTUAL:
				return CW_GUIIMGID_NONPHYSICAL;
			case CW_SIZE_VERYLARGE:
				return CW_GUIIMGID_VERYLARGE;
			case CW_SIZE_NONE:
				return CW_GUIIMGID_NONPHYSICAL;
			default:
				throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize guiSizeImage()"));
		}
	}

	/**
	 * convert v1 style size string to the new internal representation
	 * 
	 * @param v1Size
	 *            old size string
	 * @return CW internal representation of cache size
	 * @throws IllegalArgumentException if <code>v1Size</code> can not be mapped
	 * @deprecated remove once v1 file version compatibility is abandoned
	 */
	public static final byte v1Converter(final String v1Size) throws IllegalArgumentException {
		if (v1Size.equals(GC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (v1Size.equals(GC_SIZE_SMALL)) {
			return CW_SIZE_SMALL;
		} else if (v1Size.equals(GC_SIZE_REGULAR)) {
			return CW_SIZE_REGULAR;
		} else if (v1Size.equals(GC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (v1Size.equalsIgnoreCase(GC_SIZE_NOTCHOSEN)) {
			return CW_SIZE_NOTCHOSEN;
		} else if (v1Size.equals(GC_SIZE_OTHER)) {
			return CW_SIZE_OTHER;
		} else if (v1Size.equals(GC_SIZE_VIRTUAL)) {
			return CW_SIZE_VIRTUAL;
		} else if (v1Size.equals(OCTC_SIZE_NONE)) {
			return CW_SIZE_NONE;
		} else if (v1Size.equals(OCTC_SIZE_VERYLARGE)) {
			return CW_SIZE_VERYLARGE;
		} else if (v1Size.equals("")) {
			return CW_SIZE_NOTCHOSEN;
		} else {
			throw (new IllegalArgumentException("unmatched argument " + v1Size + " in v1Converter()"));
		}
	}
	
	/**
	 * return a bit mask representing the caches size for use in the Filter
	 * 
	 * @param size
	 *            CW internal representation of cache size
	 * @return a bit mask for the filter function
	 * @throws IllegalArgumentException
	 *             if <code>size</code> can not be mapped to a bit mask
	 */
	public static byte getFilterPattern(final byte size) throws IllegalArgumentException {
		switch (size) {
			case CW_SIZE_MICRO:
				return CW_FILTER_MICRO;
			case CW_SIZE_SMALL:
				return CW_FILTER_SMALL;
			case CW_SIZE_REGULAR:
				return CW_FILTER_NORMAL;
			case CW_SIZE_LARGE:
				return CW_FILTER_LARGE;
			case CW_SIZE_NOTCHOSEN:
				return CW_FILTER_NONPHYSICAL;
			case CW_SIZE_OTHER:
				return CW_FILTER_NONPHYSICAL;
			case CW_SIZE_VIRTUAL:
				return CW_FILTER_NONPHYSICAL;
			case CW_SIZE_VERYLARGE:
				return CW_FILTER_VERYLARGE;
			case CW_SIZE_NONE:
				return CW_FILTER_NONPHYSICAL;
			default:
				throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize getFilterPattern()"));
		}
	}

	/**
	 * provides abbreviated representations of CacheSize for compact exporters
	 * 
	 * @param size
	 *            CW internal representation of cache size
	 * @return a one letter String for cache size
	 * @throws IllegalArgumentException
	 *             if <code>size</code> can not be mapped
	 */

	public static String getExportShortId(final byte size) throws IllegalArgumentException {
		switch (size) {
			case CW_SIZE_MICRO:
				return "m";
			case CW_SIZE_SMALL:
				return "s";
			case CW_SIZE_REGULAR:
				return "r";
			case CW_SIZE_LARGE:
				return "l";
			case CW_SIZE_NOTCHOSEN:
				return "n";
			case CW_SIZE_OTHER:
				return "n";
			case CW_SIZE_VIRTUAL:
				return "n";
			case CW_SIZE_VERYLARGE:
				return "v";
			case CW_SIZE_NONE:
				return "n";
			default:
				throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize getExportShortId()"));
		}
	}

	/**
	 * generate a string array suitable to be used in DetalsPanel drop down list
	 * 
	 * @return strings to be displayed in the DetailsPanel Size DropDown
	 * @see guiSizeStrings2CwSize
	 * @see cwSizeId2GuiSizeId
	 */
	public static String[] guiSizeStrings() {
		// make sure strings appear in ascending order for CW_SIZE_*
		final String ret[] = new String[] { 
				GC_SIZE_NOTCHOSEN, 
				GC_SIZE_OTHER,
				GC_SIZE_MICRO, 
				GC_SIZE_SMALL, 
				GC_SIZE_REGULAR, 
				GC_SIZE_LARGE,
				OCTC_SIZE_VERYLARGE, 
				OCTC_SIZE_NONE, 
				GC_SIZE_VIRTUAL 
				};
		return ret;
	}

	/**
	 * map a string chosen from the DetailsPanel Size drop down list back to
	 * internal representation
	 * 
	 * @param size string selected in the list
	 * @return cw type information
	 * @throws IllegalArgumentException
	 *             if <code>id</code> can not be mapped
	 * @see cwSizeId2GuiSizeId
	 * @see guiSizeStrings
	 */
	public static byte guiSizeStrings2CwSize(final String size) throws IllegalArgumentException {
		// map the strings in guiSizeStrings() back to cw byte types
		if (size.equals(GC_SIZE_NOTCHOSEN)) {
			return CW_SIZE_NOTCHOSEN;
		} else if (size.equals(GC_SIZE_OTHER)) {
			return CW_SIZE_OTHER;
		} else if (size.equals(GC_SIZE_SMALL)) {
			return CW_SIZE_SMALL;
		} else if (size.equals(GC_SIZE_REGULAR)) {
			return CW_SIZE_REGULAR;
		} else if (size.equals(GC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (size.equals(OCTC_SIZE_VERYLARGE)) {
			return CW_SIZE_VERYLARGE;
		} else if (size.equals(OCTC_SIZE_NONE)) {
			return CW_SIZE_NONE;
		} else if (size.equals(GC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (size.equals(GC_SIZE_VIRTUAL)) {
			return CW_SIZE_VIRTUAL;
		} else {
			throw (new IllegalArgumentException("unmatched argument " + size + " in guiSizeStrings2CwSize()"));
		}
	}

	/**
	 * map internal representation to index used in the the DetailsPanel Size
	 * drop down list
	 * 
	 * @param size
	 *            internal id to be mapped
	 * @return index of internal size in array
	 * @throws IllegalArgumentException
	 *             if <code>id</code> can not be mapped
	 * @see guiSizeStrings2CwSize
	 * @see cwSizeId2GuiSizeId
	 */
	public static int cwSizeId2GuiSizeId(final byte size) throws IllegalArgumentException {
		switch (size) {
		case CW_SIZE_NOTCHOSEN:
			return 0;
		case CW_SIZE_OTHER:
			return 1;
		case CW_SIZE_MICRO:
			return 2;
		case CW_SIZE_SMALL:
			return 3;
		case CW_SIZE_REGULAR:
			return 4;
		case CW_SIZE_LARGE:
			return 5;
		case CW_SIZE_VERYLARGE:
			return 6;
		case CW_SIZE_NONE:
			return 7;
		case CW_SIZE_VIRTUAL:
			return 8;
		default:
			throw (new IllegalArgumentException("unmatched argument " + size + " in CacheSize ()"));
		}

	}
	
	/**
	 * checks if a given size information would be valid for use with CacheWolf.
	 * takes about 1/20th of the time a try {} catch {} block would need, so use this
	 * function if you just want to check
	 * @param size size information to check
	 * @return true if size is valid, false otherwise
	 */
	public static boolean isValidSize(final byte size) {
		switch (size) {
		case CW_SIZE_NOTCHOSEN: return true;
		case CW_SIZE_OTHER: return true;
		case CW_SIZE_MICRO: return true;
		case CW_SIZE_SMALL: return true;
		case CW_SIZE_REGULAR: return true;
		case CW_SIZE_LARGE: return true;
		case CW_SIZE_VERYLARGE: return true;
		case CW_SIZE_NONE: return true;
		case CW_SIZE_VIRTUAL: return true;
		default: return false;
		}
	}
}
