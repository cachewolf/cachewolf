package CacheWolf.navi;

/**
 * Point in Gauß-Krüger Format
 * @author Robert Arnold
 *
 */
public class GkPoint {
	double northing;
	double easting;

	public GkPoint() { super(); }
	
	public GkPoint(double e, double n) {
		easting = e;
		northing = n;
	}
	
	public String toString() {
		return toString(0, "R: ", " H: ");
	}

	
	public String toString(int decimalplaces, String prefix, String seperator) {
		ewe.sys.Double n = new ewe.sys.Double();
		ewe.sys.Double e = new ewe.sys.Double();
		n.set(northing);
		e.set(easting);
		n.decimalPlaces = decimalplaces;
		e.decimalPlaces = decimalplaces;
		return prefix + e.toString().replace(',', '.') + seperator + n.toString().replace(',', '.');
	}
	
	/**
	 * shift the point
	 * @param meters positiv to north (east), negativ to south (west)
	 * @param direction 0 north-south, 1 east-west
	 */
	public void shift(double meters, int direction) {
		switch (direction) { // TODO this works corectly only within an 3 degrees stripe
			case 0: northing += meters; return;
			case 1: easting += meters; return;
		}
	}
}
