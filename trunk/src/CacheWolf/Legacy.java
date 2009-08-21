/**
 * 
 */
package CacheWolf;

import CacheWolf.utils.MutableInteger;

/**
 * Utility class for converting data from old version files to current version files. This class
 * is used to keep legacy support for old formats out of the "current" classes, so that they are
 * not cluttered with old stuff which is seldom needed.
 *
 */
public final class Legacy {
	
	private static final MutableInteger mInt = new MutableInteger(0);
	
	public static final int TYPE_CACHESIZE=0;
	public static final int TYPE_CACHETYPE=1;
	
	/**
	 * General purpose method to convert old version integer values to new version byte values.
	 * To distinguish the different purposes the parameter <code>type</code> is used.
	 * @param value Integer value to convert to new value
	 * @param type Constant to distinguish the different purposes of the method
	 * @param oldVersion Version number of the file the value is taken from
	 * @param newVersion Version number of the file the converted number is intended for. Nearly  
	 *   always this number should be the last supported version - unless we decide to support
	 *   older version files also for writing.
	 * @return The converted byte value
	 */
	public static byte toByte(int value, int type, int oldVersion, int newVersion) {
		mInt.setInt(value);
		return toByte(mInt, type, oldVersion, newVersion);
	}

	/**
	 * General purpose method to convert old version String values to new version byte values.
	 * To distinguish the different purposes the parameter <code>type</code> is used.
	 * @param value String value to convert to new value
	 * @param type Constant to distinguish the different purposes of the method
	 * @param oldVersion Version number of the file the value is taken from
	 * @param newVersion Version number of the file the converted number is intended for. Nearly  
	 *   always this number should be the last supported version - unless we decide to support
	 *   older version files also for writing.
	 * @return The converted byte value
	 */
	public static byte toByte(String value, int type, int oldVersion, int newVersion) {
		return toByte(value, type, oldVersion, newVersion);
	}

	private static byte toByte(Object obj, int type, int oldVersion, int newVersion) {
		switch (type) {
		case TYPE_CACHESIZE:
			// For CacheSizes: The object is a plain string.
			String oldS = (String)obj;
			if (oldVersion == 1 && newVersion==3) {
				if (oldS.equals("Micro")) return CacheSize.CW_SIZE_MICRO;
				else if (oldS.equals("Small")) return CacheSize.CW_SIZE_SMALL;
				else if (oldS.equals("Regular")) return CacheSize.CW_SIZE_REGULAR;
				else if (oldS.equals("Large")) return CacheSize.CW_SIZE_LARGE;
				else if (oldS.equals("Very large")) return CacheSize.CW_FILTER_VERYLARGE;
				else if (oldS.equals("Other")) return CacheSize.CW_SIZE_OTHER;
				else if (oldS.equals("Virtual")) return CacheSize.CW_SIZE_VIRTUAL;
				else if (oldS.equals("Not chosen")) return CacheSize.CW_SIZE_NOTCHOSEN;
				else if (oldS.equals("None")) return CacheSize.CW_SIZE_NONE;
				else throw new IllegalArgumentException("Can't convert size '"+oldS+
						"' from version 1 to 3"+String.valueOf(oldVersion)+" to version "+
						String.valueOf(newVersion)+".");
			}
			else throw new IllegalArgumentException("Can't convert any size from version "+
					String.valueOf(oldVersion)+" to version "+String.valueOf(newVersion)+".");
		case TYPE_CACHETYPE:
			// For CacheTypes: The object is a MutableInteger
			int oldI = ((MutableInteger)obj).getInt();
			if (oldVersion == 1 && newVersion==3) {
				// TODO Depends, has to be filled
			} else if (oldVersion == 2 && newVersion==3) {
				// There is an easy mapping to convert cache type values of version 2 to 
				// cache type values of version 1. This conversion is done and then the values
				// are treated as if they were of version 1. 
				int versionOneValue;
				switch (oldI) {
					case 100: versionOneValue = 1848; break;
					case 101: versionOneValue = 453; break;
					default: versionOneValue = type + 128;
				}
				return toByte(versionOneValue, TYPE_CACHETYPE, 1, 3);
			} else throw new IllegalArgumentException("Can't convert any type from version "+
				String.valueOf(oldVersion)+" to version "+String.valueOf(newVersion)+".");
		default: 
			throw new IllegalArgumentException("Can't convert type "+String.valueOf(type)+".");
		}
    }
}
