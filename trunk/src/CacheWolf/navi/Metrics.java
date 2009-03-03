/**
 * 
 */
package CacheWolf.navi;

import CacheWolf.Global;

/**
 * @author Engywuck
 *
 */
public final class Metrics {

	/** Constant for use of metric units */
	public static final int METRIC = 1;
	/** Constant for use of imperial units */
	public static final int IMPERIAL = 2;
	
	public static final int KILOMETER = 10;
	public static final int METER  = 11;
	
	public static final int MILES = 20;
	public static final int YARDS = 21;
	public static final int FEET  = 22;
	
	private static final double FCT_MILE2KILOMETER = 1.609344;
	private static final double FCT_MILE2YARD = 1760;
	private static final double FCT_MILE2FOOT = 5280;
	
	/**
	 * Converts values from one to another unit.
	 * @param value Value to convert
	 * @param sourceUnit Constant from class Metrics to represent unit in which 
	 * <code>value</code> is meant. 
	 * @param targetUnit Constant from class Metrics to represent the unit in 
	 * which <code>value</code> has to be transformed
	 * @return The new converted value
	 * @throws UnsupportedOperationException when the conversion between the two units
	 * is not programmed.
	 */
	public static final double convertUnit(double value, int sourceUnit, int targetUnit) {
		double result = Double.NaN;
		if (sourceUnit == targetUnit) {
			result = value;
		} else {
			switch (sourceUnit) {
			case KILOMETER:
				switch (targetUnit) {
				case METER:
					result = value * 1000;
					break;
				case MILES:
					result = value / FCT_MILE2KILOMETER;
					break;
				case YARDS:
					result = (value / FCT_MILE2KILOMETER) * FCT_MILE2YARD;
					break;
				case FEET:
					result = (value / FCT_MILE2KILOMETER) * FCT_MILE2FOOT;
					break;
				}
				break;
			case METER:
				switch (targetUnit) {
				case KILOMETER:
					result = value / 1000;
					break;
				case MILES:
					result = (value / 1000) / FCT_MILE2KILOMETER;
					break;
				case YARDS:
					result = ((value / 1000) / FCT_MILE2KILOMETER) * FCT_MILE2YARD;
					break;
				case FEET:
					result = ((value / 1000) / FCT_MILE2KILOMETER) * FCT_MILE2FOOT;
					break;
				}
				break;
			case MILES:
				switch (targetUnit) {
				case KILOMETER:
					result = value * FCT_MILE2KILOMETER;
					break;
				case METER:
					result = value * FCT_MILE2KILOMETER * 1000;
					break;
				case YARDS:
					result = value * FCT_MILE2YARD;
					break;
				case FEET:
					result = value * FCT_MILE2FOOT;
					break;
				}
				break;
			case YARDS:
				switch (targetUnit) {
				case KILOMETER:
					result = (value / FCT_MILE2YARD) * FCT_MILE2KILOMETER;
					break;
				case METER:
					result = (value / FCT_MILE2YARD) * FCT_MILE2KILOMETER * 1000;
					break;
				case MILES:
					result = value / FCT_MILE2YARD;
					break;
				case FEET:
					result = value / FCT_MILE2YARD * FCT_MILE2FOOT;
					break;
				}
				break;
			case FEET:
				switch (targetUnit) {
				case KILOMETER:
					result = (value / FCT_MILE2FOOT) * FCT_MILE2KILOMETER;
					break;
				case METER:
					result = (value / FCT_MILE2FOOT) * FCT_MILE2KILOMETER * 1000;
					break;
				case MILES:
					result = value / FCT_MILE2FOOT;
					break;
				case YARDS:
					result = value * FCT_MILE2YARD / FCT_MILE2FOOT;
					break;
				}
				break;
			}
		}
		if (String.valueOf(result).equals("NaN")) {
			Global.getPref().log("Cannot convert "+getUnit(sourceUnit)+ " to "+getUnit(targetUnit));
			throw new UnsupportedOperationException("Cannot convert "+getUnit(sourceUnit)+ " to "+targetUnit);
		}
		return result;
	}
	
	public static final String getUnit(int unit) {
		String result=null;
		switch (unit) {
		case KILOMETER:
			result = "km";
			break;
		case METER:
			result = "m";
			break;
		case MILES:
			result = "mi.";
			break;
		case YARDS:
			result = "yd.";
			break;
		case FEET:
			result = "ft.";
			break;
		default:
			throw new UnsupportedOperationException("Unknown unit: "+String.valueOf(unit));			
		}
		return result;
	}
}
