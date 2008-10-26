package cachewolf.navi;
import cachewolf.utils.Common;

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
	float lengthOfStripe0; // e.g. in italien GK stripe 1 is at 9 degree
	public GkPoint() { super(); }
	
	public GkPoint(GkPoint p) {
		set(p.easting, p.northing, p.stripe, p.stripewidth, p.lengthOfStripe0);
	}
	
	/**
	 * e containing the number of the stripe
	 * @param e
	 * @param n
	 */
	public GkPoint(double e, double n, int stripewidthi, float degreeOfStripeZero) {
		stripe = (int) Math.floor(e / 1000000);
		set(e - 1000000 * stripe - 500000, n, stripe, stripewidthi, degreeOfStripeZero);
		set(e - 1000000 * stripe - 500000, n, (int) Math.floor(e / 1000000), stripewidthi);
	}
	
	/**
	 * use this to set normal german Gauß-Krüger coordinates
	 * (they contain the stripe numer in the easting value and
	 * have a stripe with of 3 degrees)
	 * @param e
	 * @param n
	 */
	public GkPoint(double e, double n, int region) {
		switch (region) {
			case GERMAN_GK:	set(e, n, 3, 0); break;
			case ITALIAN_GB:	set(e, n, 6, 3); break;
			default: throw new IllegalArgumentException("GkPoint (double, double, int): region: " + region + " not supported");
		}
	}
	
	public GkPoint(double e, double n, int stripei, int stripewidthi, float degreeOfStripeZero) {
		set(e, n, stripei, stripewidthi, degreeOfStripeZero);
	}
		
	/**
	 * 
	 * @param e containing the stripe number
	 * @param n
	 * @param stripewidthi
	 */
	public void set(double e, double n, int stripewidthi, float degreeOfStripeZero) {
		double stripei = Math.floor(e / 1000000);
		set(e - 1000000 * stripei - 500000, n, (int) stripei, stripewidthi, degreeOfStripeZero);
	}
	
	/**
	 * @param e in meters from center of stripe, it may not contain the stripenumber
	 */
	public void set(double e, double n, int stripei, int stripewidthi, float lenthOfStripeZero_) {
		stripe = stripei;
		stripewidth = stripewidthi;
		easting = e;
		northing = n;
		lengthOfStripe0 = lenthOfStripeZero_;
	}
	
	public double getStripeLon() {
		return stripe * stripewidth+ lengthOfStripe0; // TODO + stripeoffset
	}
	
	public int getStripe() {
		return stripe;
	}
	public TrackPoint toTrackPoint(int region) {
		return new TrackPoint(northing, getGkEasting(region));
	}
	/**
	 * This will give you the normal Gauß-Krüger easting value
	 * (that means including the stripe number)
	 * @return
	 */
	public static final int GERMAN_GK = 4900;
	public static final int ITALIAN_GB = 3900; 
	public static final int DEFAULT_GK = GERMAN_GK;
	
	/**
	 * 
	 * @param region international telephone area code * 100  
	 * @return
	 * @throws IllegalArgumentException if region is not supported
	 */
	public double getGkEasting(int region) {
		double e;
		switch (region) {
		case GERMAN_GK: e = easting + 500000 + stripe * 1000000; break;
		case ITALIAN_GB:	
			e = easting + 500000 + stripe * 1000000;
			if (stripe == 2) e += 20000; // because of an unknown reason the second stripe in EPSG:3004 has an false easting of 2520000
		break;
		default: throw new IllegalArgumentException("getGkEasting: area code " + region + "not supported");
		}
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
	
	/**
	 * assumes _German_ Gauß-Krüger
	 */
	/*public String toString() {
		return toString(0, "R: ", " H: ", GERMAN_GK);
	}*/

	
	public String toString(int decimalplaces, String prefix, String seperator, int region) {
		return prefix + Common.doubleToString(getGkEasting(region),decimalplaces).replace(',', '.') + seperator + Common.doubleToString(northing,decimalplaces).replace(',', '.');
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
