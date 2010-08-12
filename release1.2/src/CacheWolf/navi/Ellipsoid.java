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
	
	   /**
	    * Get semi-major axis.
	    * @return semi-major axis (in meters).
	    */
	   public double getSemiMajorAxis()
	   {
	     return a;
	   }

	   /**
	    * Get semi-minor axis.
	    * @return semi-minor axis (in meters).
	    */
	   public double getSemiMinorAxis()
	   {
	     return b;
	   }

	   /**
	    * Get flattening
	    * @return
	    */
	   public double getFlattening()
	   {
	     return (a - b) / a;
	   }

	   /**
	    * Get inverse flattening.
	    * @return
	    */
	   public double getInverseFlattening()
	   {
	     return a / (a - b);
	   }}
