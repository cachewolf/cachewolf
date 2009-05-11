package CacheWolf;

public final class CacheSize {

	/*
	 * internal representation of cache sizes in CacheHolder 
	 * we just made them up ;-)
	 */
	static final protected byte CW_SIZE_NOTCHOSEN = 0;
	static final protected byte CW_SIZE_OTHER = 1;
	static final protected byte CW_SIZE_MICRO = 2;
	static final protected byte CW_SIZE_SMALL = 3;
	static final protected byte CW_SIZE_REGULAR = 4;
	static final protected byte CW_SIZE_LARGE = 5;
	static final protected byte CW_SIZE_VERYLARGE = 6;
	static final protected byte CW_SIZE_NONE = 7;
	static final protected byte CW_SIZE_VIRTUAL = 8;

	/*
	 * geocaching.com size string as found by analyzing GPX files
	 * plus OC/TC Very large
	 */
	static final protected String GC_SIZE_MICRO = "Micro";
	static final protected String GC_SIZE_SMALL = "Small";
	static final protected String GC_SIZE_REGULAR = "Regular";
	static final protected String GC_SIZE_LARGE = "Large";
	static final protected String GC_SIZE_NOTCHOSEN = "Not chosen";
	static final protected String GC_SIZE_OTHER = "Other";
	static final protected String GC_SIZE_VIRTUAL = "Virtual";
	static final protected String OCTC_SIZE_VERYLARGE = "Very large";

	/*
	 * OpenCaching Size IDs
	 * see http://oc-server.svn.sourceforge.net/viewvc/oc-server/doc/sql/static-data/data.sql?view=markup
	 */
	static final protected String OC_SIZE_OTHER = "1";
	static final protected String OC_SIZE_MICRO = "2";
	static final protected String OC_SIZE_SMALL = "3";
	static final protected String OC_SIZE_NORMAL = "4";
	static final protected String OC_SIZE_LARGE = "5";
	static final protected String OC_SIZE_VERYLARGE = "6";
	static final protected String OC_SIZE_NONE = "7";
	
	/*
	 * TerraCaching Size IDs
	 * taken from old GPXimporter (?? reliable source ??)
	 */
	static final protected String TC_SIZE_MICRO = "1";
	static final protected String TC_SIZE_MEDIUM = "2";
	static final protected String TC_SIZE_REGULAR = "3";
	static final protected String TC_SIZE_LARGE = "4";
	static final protected String TC_SIZE_VERYLARGE = "4";

	/*
	 * images to show in CW index panel we use less images than sizes since all
	 * non physical caches are represented by the same symbol
	 */
	static final protected String CW_GUIIMG_MICRO = "sizeMicro.png";
	static final protected String CW_GUIIMG_SMALL = "sizeSmall.png";
	static final protected String CW_GUIIMG_NORMAL = "sizeReg.png";
	static final protected String CW_GUIIMG_LARGE = "sizeLarge.png";
	static final protected String CW_GUIIMG_NONPHYSICAL = "sizeNonPhysical.png"; 
	static final protected String CW_GUIIMG_VERYLARGE = "sizeVLarge.png";
	
	/*
	 * bit masks to be used with the filter function
	 */
	static final protected byte CW_FILTER_MICRO = 0x01<<0;
	static final protected byte CW_FILTER_SMALL = 0x01<<1;
	static final protected byte CW_FILTER_NORMAL = 0x01<<2;
	static final protected byte CW_FILTER_LARGE = 0x01<<3;
	static final protected byte CW_FILTER_VERYLARGE = 0x01<<4;
	static final protected byte CW_FILTER_NONPHYSICAL = 0x01<<5;
	static final protected byte CW_FILTER_ALL = CW_FILTER_MICRO|CW_FILTER_SMALL|CW_FILTER_NORMAL|CW_FILTER_LARGE|CW_FILTER_NONPHYSICAL|CW_FILTER_VERYLARGE;

	/**
	 * the constructor does nothing
	 */
	public CacheSize() {
		// do nothing
	}

	/**
	 * convert the size info from a CacheHolder to a string suitable for GPX
	 * export
	 * 
	 * @param size
	 *            CW internal representation of cache size
	 * @return string representation of CacheWolf internal cache size
	 * @throws IllegalArgumentException
	 *             if cwsize can not be mapped to a CW_SIZE constant
	 */
	public static String cw2ExportString(byte size) {
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
		default:
			throw (new IllegalArgumentException("unmatched argument " + size
					+ " in CacheSize cw2GcString()"));
		}
	}

	/**
	 * convert the cache size information from a TerraCaching GPX import to internal representation
	 * 
	 * @param tcstring
	 *            size information extracted from a TC GPX inport
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if tcstring can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */

	public static byte tcGpxString2Cw(String tcstring) {
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
			throw (new IllegalArgumentException("unmatched argument "
					+ tcstring + " in CacheSize tcGpxString2Cw()"));
		}
	}
	
	/**
	 * convert the cache size information from a GC GPX import to internal representation
	 * 
	 * @param gcstring
	 *            size information extracted from a GPX inport
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if gcstring can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */

	public static byte gcGpxString2Cw(String gcstring) {
		if (gcstring.equals(GC_SIZE_MICRO)) {
			return CW_SIZE_MICRO;
		} else if (gcstring.equals(GC_SIZE_SMALL)) {
			return CW_SIZE_SMALL;
		} else if (gcstring.equals(GC_SIZE_REGULAR)) {
			return CW_SIZE_REGULAR;
		} else if (gcstring.equals(GC_SIZE_LARGE)) {
			return CW_SIZE_LARGE;
		} else if (gcstring.equals(GC_SIZE_NOTCHOSEN)) {
			return CW_SIZE_NOTCHOSEN;
		} else if (gcstring.equals(GC_SIZE_OTHER)) {
			return CW_SIZE_OTHER;
		} else if (gcstring.equals(GC_SIZE_VIRTUAL)) {
			return CW_SIZE_VIRTUAL;
		} else {
			throw (new IllegalArgumentException("unmatched argument "
					+ gcstring + " in CacheSize gcGpxString2Cw()"));
		}
	}

	/**
	 * convert the cache size information from GCSpider to internal
	 * representation for CacheHolder
	 * 
	 * @param spiderstring
	 *            string identified by the spider as containing size iformation
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if spiderstring can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */
	public static byte gcSpiderString2Cw(String spiderstring) {
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
	 * @trows IllegalArgumentException if ocxmlstring can not be mapped to a
	 *        CW_SIZE_*
	 */
	public static byte ocXmlString2Cw(String ocxmlstring) {
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
			throw (new IllegalArgumentException("unmatched argument "
					+ ocxmlstring + " in CacheSize ocXmlString2Cw()"));
		}
	}

	/**
	 * get name of the image to be displayed in CW index panel
	 * 
	 * @param size CW internal representation of cache size
	 * @return filename of image to be displayed in main panel as size icon
	 * @throws IllegalArgumentException
	 *             if size can not be mapped
	 */
	public static String guiSizeImage(byte size) {
		switch (size) {
		case CW_SIZE_MICRO:
			return CW_GUIIMG_MICRO;
		case CW_SIZE_SMALL:
			return CW_GUIIMG_SMALL;
		case CW_SIZE_REGULAR:
			return CW_GUIIMG_NORMAL;
		case CW_SIZE_LARGE:
			return CW_GUIIMG_LARGE;
		case CW_SIZE_NOTCHOSEN:
			return CW_GUIIMG_NONPHYSICAL;
		case CW_SIZE_OTHER:
			return CW_GUIIMG_NONPHYSICAL;
		case CW_SIZE_VIRTUAL:
			return CW_GUIIMG_NONPHYSICAL;
		case CW_SIZE_VERYLARGE:
			return CW_GUIIMG_VERYLARGE;
		case CW_SIZE_NONE:
			return CW_GUIIMG_NONPHYSICAL;
		default:
			throw (new IllegalArgumentException("unmatched argument " 
					+ size + " in CacheSize guiSizeImage()"));
		}
	}
	
	/**
	 * return a bit mask representing the caches size for use in the Filter
	 * 
	 * @param size CW internal representation of cache size
	 * @return a bit mask for the filter function
	 * @throws IllegalArgumentException if size can not be mapped to a bit mask
	 */
	
	public static byte getFilterPattern(byte size) {
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
			throw (new IllegalArgumentException("unmatched argument " 
					+ size + " in CacheSize getFilterPattern()"));
		}
	}
	
	/**
	 * provides abbreviated representations of CacheSize for compact exporters
	 * 
	 * @param size CW internal representation of cache size
	 * @return a one letter String for cache size 
	 * @throws IllegalArgumentException  if size can not be mapped
	 */
	
	public static String getExportShortId(byte size) {
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
			throw (new IllegalArgumentException("unmatched argument " 
					+ size + " in CacheSize getExportShortId()"));
		}
	}
}
