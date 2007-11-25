package CacheWolf.navi;

/**
 * Point in Gauß-Krüger Format
 * @author Robert Arnold
 *
 */
public class GkPoint {
	double northing; // TODO make these private
	private double easting; // because it is not clear for routines from outside if the stripe number is included, make this available only through methods
	int stripe;
	int stripewidth;

	public GkPoint() { super(); }
	
	public GkPoint(GkPoint p) {
		set(p.easting, p.northing, p.stripe, p.stripewidth);
	}
	
	/**
	 * e containing the number of the stripe
	 * @param e
	 * @param n
	 */
	public GkPoint(double e, double n, int stripewidthi) {
		set(e - 1000000 * stripe - 500000, n, (int) Math.floor(e / 1000000), stripewidthi);
	}
	
	/**
	 * use this to set normal german Gauß-Krüger coordinates
	 * (they contain the stripe numer in the easting value and
	 * have a stripe with of 3 degrees)
	 * @param e
	 * @param n
	 */
	public GkPoint(double e, double n) {
		set(e, n, 3);
	}
	
	public GkPoint(double e, double n, int stripei, int stripewidthi) {
		set(e, n, stripei, stripewidthi);
	}
		
	/**
	 * 
	 * @param e containing the stripe number
	 * @param n
	 * @param stripewidthi
	 */
	public void set(double e, double n, int stripewidthi) {
		double stripei = Math.floor(e / 1000000);
		set(e - 1000000 * stripei - 500000, n, (int) stripei, stripewidthi);
	}
	
	/**
	 * @param e in meters from center of stripe, it may not contain the stripenumber
	 */
	public void set(double e, double n, int stripei, int stripewidthi) {
		stripe = stripei;
		stripewidth = stripewidthi;
		easting = e;
		northing = n;
	}
	
	public double getStripeLon() {
		return stripe * stripewidth;
	}
	
	public int getStripe() {
		return stripe;
	}
	
	/**
	 * This will give you the normal Gauß-Krüger easting value
	 * (that means including the stripe number)
	 * @return
	 */
	public double getGkEasting() {
		double e = easting + 500000 + stripe * 1000000;
		return e;
	}
	
	/**
	 * easting measured in meters from stripe middle
	 * @return
	 */
	public double getRawEasting() {
		return easting;
	}
	
	/**
	 * easting measured in meters from stripe middle
	 * @return
	 */
	public double getNorthing() {
		return northing;
	}
	
	
	public String toString() {
		return toString(0, "R: ", " H: ");
	}

	
	public String toString(int decimalplaces, String prefix, String seperator) {
		ewe.sys.Double n = new ewe.sys.Double();
		ewe.sys.Double e = new ewe.sys.Double();
		n.set(northing);
		e.set(getGkEasting());
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
