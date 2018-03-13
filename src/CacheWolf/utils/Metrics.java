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
package CacheWolf.utils;

/**
 * @author Engywuck
 */
public final class Metrics {

    /**
     * Constant for use of metric units
     */
    public static final int METRIC = 1;
    /**
     * Constant for use of imperial units
     */
    public static final int IMPERIAL = 2;

    public static final int KILOMETER = 10;
    public static final int METER = 11;

    public static final int MILES = 20;
    public static final int YARDS = 21;
    public static final int FEET = 22;

    private static final double FCT_MILE2KILOMETER = 1.609344;
    private static final double FCT_MILE2YARD = 1760;
    private static final double FCT_MILE2FOOT = 5280;

    /**
     * Converts values from one to another unit.
     *
     * @param value      Value to convert
     * @param sourceUnit Constant from class Metrics to represent unit in which <code>value</code> is
     *                   meant.
     * @param targetUnit Constant from class Metrics to represent the unit in which <code>value</code>
     *                   has to be transformed
     * @return The new converted value
     * @throws UnsupportedOperationException when the conversion between the two units is not programmed.
     */
    public static final double convertUnit(double value, int sourceUnit, int targetUnit) {
        /*
         * Strategy: For each metrical system we define a standard unit: km and mi. In a first step
         * any input value is converted to the standard unit in its system Then (if required) the
         * standard units are converted between the systems. Last step: The converted unit is
         * converted to the target unit (which is in the same metrical system).
         */

        double result = Double.NaN;
        if (sourceUnit == targetUnit) {
            result = value;
        } else {
            double stdValue;
            int sourceStdUnit = getMetricSystemStdUnit(sourceUnit);
            int targetStdUnit = getMetricSystemStdUnit(targetUnit);
            switch (sourceUnit) {
                case KILOMETER:
                case MILES:
                    stdValue = value;
                    break;
                case METER:
                    stdValue = value / 1000.0;
                    break;
                case YARDS:
                    stdValue = value / FCT_MILE2YARD;
                    break;
                case FEET:
                    stdValue = value / FCT_MILE2FOOT;
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot convert unit" + getUnit(sourceUnit));
            }

            // Convert between standard units of imperial systems
            if (sourceStdUnit == KILOMETER && targetStdUnit == MILES) {
                stdValue = stdValue / FCT_MILE2KILOMETER;
            } else if (sourceStdUnit == MILES && targetStdUnit == KILOMETER) {
                stdValue = stdValue * FCT_MILE2KILOMETER;
            }

            // Convert to unit from standard unit
            switch (targetUnit) {
                case KILOMETER:
                case MILES:
                    result = stdValue;
                    break;
                case METER:
                    result = stdValue * 1000.0;
                    break;
                case YARDS:
                    result = stdValue * FCT_MILE2YARD;
                    break;
                case FEET:
                    result = stdValue * FCT_MILE2FOOT;
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot convert unit" + getUnit(targetUnit));
            }
        }
        return result;
    }

    public static final String getUnit(int unit) {
        String result = null;
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
                throw new UnsupportedOperationException("Unknown unit: " + String.valueOf(unit));
        }
        return result;
    }

    /**
     * Returns the constant for the metric systems standard unit (km or mi.) a given unit belongs to.
     *
     * @param unit Unit to examine
     * @return Constant of the standard unit
     */
    private static int getMetricSystemStdUnit(int unit) {
        switch (unit) {
            case KILOMETER:
            case METER:
                return KILOMETER;
            case MILES:
            case YARDS:
            case FEET:
                return MILES;
            default:
                throw new UnsupportedOperationException("Unknown unit: " + String.valueOf(unit));
        }
    }
}
