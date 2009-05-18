package CacheWolf;

/**
 * Handles all aspects of converting terrain and difficulty
 * infromations from legacy file versions and various im-
 * and expoters
 * 
 * Only use the class in a static way, do not instantiate it
 */
public class CacheTerrDiff {
	
	/** terrain or difficulty 1.0 */
	protected static final byte CW_DT_10 = 10;
	/** terrain or difficulty 1.5 */
	protected static final byte CW_DT_15 = 15;
	/** terrain or difficulty 2.0 */
	protected static final byte CW_DT_20 = 20;
	/** terrain or difficulty 2.5 */
	protected static final byte CW_DT_25 = 25;
	/** terrain or difficulty 3.0 */
	protected static final byte CW_DT_30 = 30;
	/** terrain or difficulty 3.5 */
	protected static final byte CW_DT_35 = 35;
	/** terrain or difficulty 4.0 */
	protected static final byte CW_DT_40 = 40;
	/** terrain or difficulty 4.5 */
	protected static final byte CW_DT_45 = 45;
	/** terrain or difficulty 5.0 */
	protected static final byte CW_DT_50 = 50;
	/** wrong terrain or difficulty */
	protected static final byte CW_DT_ERROR = -1;

	/** constructor dies nothing */
	public CacheTerrDiff() {
	}
	
	/**
	 * convert "old style" terran and difficulty information to the new format.
	 * 
	 * since it is also used by the importes it is not flagged as deprecated
	 * @param v1TerrDiff a string representation of terrain or difficulty
	 * @return internal representation of terrain or difficulty
	 * @throws IllegalArgumentException if <code>v1TerrDiff</code> can not be mapped
	 */
	static final byte v1Converter(String v1TerrDiff) throws IllegalArgumentException {
		if (v1TerrDiff == null) {
			throw new IllegalArgumentException("error mapping terrain or difficulty");
		}
		v1TerrDiff = v1TerrDiff.replace(',', '.');
		if (v1TerrDiff.equals("1") || v1TerrDiff.equals("1.0")) return CW_DT_10;
		if (v1TerrDiff.equals("2") || v1TerrDiff.equals("2.0")) return CW_DT_20;
		if (v1TerrDiff.equals("3") || v1TerrDiff.equals("3.0")) return CW_DT_30;
		if (v1TerrDiff.equals("4") || v1TerrDiff.equals("4.0")) return CW_DT_40;
		if (v1TerrDiff.equals("5") || v1TerrDiff.equals("5.0")) return CW_DT_50;
		
		if (v1TerrDiff.equals("1.5")) return CW_DT_15;
		if (v1TerrDiff.equals("2.5")) return CW_DT_25;
		if (v1TerrDiff.equals("3.5")) return CW_DT_35;
		if (v1TerrDiff.equals("4.5")) return CW_DT_45;
		
		throw new IllegalArgumentException("error mapping terrain or difficulty");
	}
	
	/**
	 * generate strings of terrain and difficulty for general use
	 * @param td internal terrain or difficulty value
	 * @return long version of terrain or difficulty (includeing .0)
	 * @throws IllegalArgumentException
	 */
	public static final String longDT(byte td) throws IllegalArgumentException {
		switch(td) {
		case CW_DT_10: return "1.0";
		case CW_DT_15: return "1.5";
		case CW_DT_20: return "2.0";
		case CW_DT_25: return "2.5";
		case CW_DT_30: return "3.0";
		case CW_DT_35: return "3.5";
		case CW_DT_40: return "4.0";
		case CW_DT_45: return "4.5";
		case CW_DT_50: return "5.0";
		default: throw new IllegalArgumentException("unmapped terrain or diffulty "+td);
		}
	}

	/**
	 * generate strings of terrain and difficulty information for GC.com-like GPX exports
	 * @param td internal terrain or difficulty value
	 * @return short version of terrainor difficulty (omit .0)
	 * @throws IllegalArgumentException
	 */
	public static final String shortDT(byte td) throws IllegalArgumentException {
		switch(td) {
		case CW_DT_10: return "1";
		case CW_DT_15: return "1.5";
		case CW_DT_20: return "2";
		case CW_DT_25: return "2.5";
		case CW_DT_30: return "3";
		case CW_DT_35: return "3.5";
		case CW_DT_40: return "4";
		case CW_DT_45: return "4.5";
		case CW_DT_50: return "5";
		default: throw new IllegalArgumentException("unmapped terrain or diffulty "+td);
		}
	}
}
