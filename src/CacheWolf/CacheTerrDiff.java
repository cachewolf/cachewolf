    /*
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

/**
 * Handles all aspects of converting terrain and difficulty
 * informations from legacy file versions and various im-
 * and exporters
 * 
 * Only use the class in a static way, do not instantiate it
 */
public final class CacheTerrDiff {
	
	/** terrain or difficulty 1.0 */
	public static final byte CW_DT_10 = 10;
	/** terrain or difficulty 1.5 */
	public static final byte CW_DT_15 = 15;
	/** terrain or difficulty 2.0 */
	public static final byte CW_DT_20 = 20;
	/** terrain or difficulty 2.5 */
	public static final byte CW_DT_25 = 25;
	/** terrain or difficulty 3.0 */
	public static final byte CW_DT_30 = 30;
	/** terrain or difficulty 3.5 */
	public static final byte CW_DT_35 = 35;
	/** terrain or difficulty 4.0 */
	public static final byte CW_DT_40 = 40;
	/** terrain or difficulty 4.5 */
	public static final byte CW_DT_45 = 45;
	/** terrain or difficulty 5.0 */
	public static final byte CW_DT_50 = 50;
	/** wrong terrain or difficulty */
	public static final byte CW_DT_ERROR = -1;
	/** terrain or difficulty for additional/custom waypoints */
	public static final byte CW_DT_UNSET = 0;

	private CacheTerrDiff() { 
		// Nothing to do
	}
	
	/**
	 * convert "old style" terrain and difficulty information to the new format.
	 * 
	 * since it is also used by the importers it is not flagged as depreciated
	 * @param v1TerrDiff a string representation of terrain or difficulty
	 * @return internal representation of terrain or difficulty
	 * @throws IllegalArgumentException if <code>v1TerrDiff</code> can not be mapped
	 */
	public static byte v1Converter(String v1TerrDiff) throws IllegalArgumentException {
		if (v1TerrDiff == null) {
			throw new IllegalArgumentException("error mapping terrain or difficulty");
		}
		v1TerrDiff = v1TerrDiff.replace(',', '.');
		if (v1TerrDiff.equals("1") || v1TerrDiff.equals("1.0")) { return CW_DT_10; }
		if (v1TerrDiff.equals("2") || v1TerrDiff.equals("2.0")) { return CW_DT_20; }
		if (v1TerrDiff.equals("3") || v1TerrDiff.equals("3.0")) { return CW_DT_30; }
		if (v1TerrDiff.equals("4") || v1TerrDiff.equals("4.0")) { return CW_DT_40; }
		if (v1TerrDiff.equals("5") || v1TerrDiff.equals("5.0")) { return CW_DT_50; }
		
		if (v1TerrDiff.equals("1.5")) { return CW_DT_15; }
		if (v1TerrDiff.equals("2.5")) { return CW_DT_25; }
		if (v1TerrDiff.equals("3.5")) { return CW_DT_35; }
		if (v1TerrDiff.equals("4.5")) { return CW_DT_45; }
		
		if (v1TerrDiff.equals("-1")) { return CW_DT_UNSET; }
		
		throw new IllegalArgumentException("error mapping terrain or difficulty");
	}
	
	/**
	 * generate strings of terrain and difficulty for general use
	 * @param terrdiff internal terrain or difficulty value
	 * @return long version of terrain or difficulty (including .0)
	 * @throws IllegalArgumentException
	 */
	public static String longDT(final byte terrdiff) throws IllegalArgumentException {
		switch(terrdiff) {
		case CW_DT_10: return "1.0";
		case CW_DT_15: return "1.5";
		case CW_DT_20: return "2.0";
		case CW_DT_25: return "2.5";
		case CW_DT_30: return "3.0";
		case CW_DT_35: return "3.5";
		case CW_DT_40: return "4.0";
		case CW_DT_45: return "4.5";
		case CW_DT_50: return "5.0";
		case CW_DT_UNSET: return "-.-";
		default: throw new IllegalArgumentException("unmapped terrain or diffulty "+terrdiff);
		}
	}

	/**
	 * generate strings of terrain and difficulty information for GC.com-like GPX exports
	 * @param terrdiff internal terrain or difficulty value
	 * @return short version of terrain or difficulty (omit .0)
	 * @throws IllegalArgumentException
	 */
	public static String shortDT(final byte terrdiff) throws IllegalArgumentException {
		switch(terrdiff) {
		case CW_DT_10: return "1";
		case CW_DT_15: return "1.5";
		case CW_DT_20: return "2";
		case CW_DT_25: return "2.5";
		case CW_DT_30: return "3";
		case CW_DT_35: return "3.5";
		case CW_DT_40: return "4";
		case CW_DT_45: return "4.5";
		case CW_DT_50: return "5";
		case CW_DT_UNSET: return "-1";
		default: throw new IllegalArgumentException("unmapped terrain or diffulty "+terrdiff);
		}
	}
	
	/**
	 * check if a given difficulty or terrain is valid
	 * takes about 1/20th of the time a try {} catch{} block needs
	 * so use this function instead
	 * @param terrdiff terrain or difficulty to check
	 * @return true if terrain or difficulty is valid, false otherwise
	 */
	public static boolean isValidTD(final byte terrdiff) {
		switch (terrdiff) {
		case CW_DT_10: return true;
		case CW_DT_15: return true;
		case CW_DT_20: return true;
		case CW_DT_25: return true;
		case CW_DT_30: return true;
		case CW_DT_35: return true;
		case CW_DT_40: return true;
		case CW_DT_45: return true;
		case CW_DT_50: return true;
		case CW_DT_UNSET: return true;
		default: return false;
		}
	}
}
