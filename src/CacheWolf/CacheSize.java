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
	 */
	static final protected String GC_SIZE_MICRO = "Micro";
	static final protected String GC_SIZE_SMALL = "Small";
	static final protected String GC_SIZE_REGULAR = "Regular";
	static final protected String GC_SIZE_LARGE = "Large";
	static final protected String GC_SIZE_NOTCHOSEN = "Not chosen";
	static final protected String GC_SIZE_OTHER = "Other";
	static final protected String GC_SIZE_VIRTUAL = "Virtual";

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
	 * images to show in CW index panel we use less images than sizes since all
	 * non physical caches are represented by the same symbol
	 */
	static final protected String CW_GUIIMG_MICRO = "sizeMicro.png";
	static final protected String CW_GUIIMG_SMALL = "sizeSmall.png";
	static final protected String CW_GUIIMG_NORMAL = "sizeReg.png";
	static final protected String CW_GUIIMG_LARGE = "sizeLarge.png";
	static final protected String CW_GUIIMG_NONPHYSICAL = ""; // TODO: create image for nonphysical caches
	static final protected String CW_GUIIMG_VERYLARGE = "sizeVLarge.png";

	/*
	 * images to use in exports (notably HTML export)
	 */
/*	// do not compile until needed (also see below exportSizeImage())
	// when needed make sure to add appropriate images to resources
	static final protected String CW_EXPIMG_MICRO = "dummy.png";
	static final protected String CW_EXPIMG_SMALL = "dummy.png"; 
	static final protected String CW_EXPIMG_NORMAL = "dummy.png";
	static final protected String CW_EXPIMG_LARGE = "dummy.png";
	static final protected String CW_EXPIMG_VIRTUAL = "dummy.png";
	static final protected String CW_EXPIMG_NOTCHOSEN = "dummy.png";
	static final protected String CW_EXPIMG_OTHER = "dummy.png";
	static final protected String CW_EXPIMG_VERYLARGE = "dummy.png";
	static final protected String CW_EXPIMG_NONE = "dummy.png";*/

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
	 * @param cwsize
	 *            CW internal representation of cache size
	 * @return string representation of CacheWolf internal cache size
	 * @throws IllegalArgumentException
	 *             if cwsize can not be mapped to a CW_SIZE constant
	 */
	public String cw2GcString(byte cwsize) {
		switch (cwsize) {
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
		default:
			throw (new IllegalArgumentException("unmatched argument " + cwsize
					+ " in CacheSize cw2GcString()"));
		}
	}

	/**
	 * convert the cache size information from a GPX import to internal
	 * representation
	 * 
	 * @param gcstring
	 *            size information extracted from a GPX inport
	 * @return CacheWolf internal representation of size information
	 * @throws IllegalArgumentException
	 *             if gcstring can not be mapped to internal representation
	 *             (CW_SIZE_*)
	 */

	public byte gcGpxString2Cw(String gcstring) {
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
	public byte gcSpiderString2Cw(String spiderstring) {
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
	public byte ocXmlString2Cw(String ocxmlstring) {
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
	 * @param size
	 * @return
	 * @throws IllegalArgumentException
	 *             if size can not be mapped
	 */
	public String guiSizeImage(byte size) {
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

/*	// do not compile until there is an expoter that can make use of this
	public String exportSizeImage(byte size) {
		switch (size) {
		case CW_SIZE_MICRO:
			return CW_EXPIMG_MICRO;
		case CW_SIZE_SMALL:
			return CW_EXPIMG_SMALL;
		case CW_SIZE_REGULAR:
			return CW_EXPIMG_NORMAL;
		case CW_SIZE_LARGE:
			return CW_EXPIMG_LARGE;
		case CW_SIZE_NOTCHOSEN:
			return CW_EXPIMG_NOTCHOSEN;
		case CW_SIZE_OTHER:
			return CW_EXPIMG_OTHER;
		case CW_SIZE_VIRTUAL:
			return CW_EXPIMG_VIRTUAL;
		case CW_SIZE_VERYLARGE:
			return CW_EXPIMG_VERYLARGE;
		case CW_SIZE_NONE:
			return CW_EXPIMG_NONE;
		default:
			throw (new IllegalArgumentException("unmatched argument " 
				+ size + " in CacheSize exportSizeImage()"));
		}
	}*/

}
