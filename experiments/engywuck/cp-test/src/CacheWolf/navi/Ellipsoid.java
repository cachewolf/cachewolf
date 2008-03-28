package CacheWolf.navi;

public class Ellipsoid {
	public double a, b;
	/**
	 * 
	 * @param ai
	 * @param bi
	 * @param isminoraxis if true bi is interpreted as axis, if false bi is interpreted as flattening
	 */
	public Ellipsoid(double ai, double bi, boolean isminoraxis ) {
		a = ai;
		if (isminoraxis) b = bi; // flattening = (a - b) / a
		else {
			b = a - (1/bi) * a;
		}
	}
}
