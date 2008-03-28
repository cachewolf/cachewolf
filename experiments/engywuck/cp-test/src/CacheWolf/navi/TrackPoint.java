package CacheWolf.navi;

/**
 * this is not CWPoint because it should be as small as possible
 * @author pfeffer
 *
 */

public class TrackPoint  {
	public double latDec;
	public double lonDec;
	
	public TrackPoint(){
		latDec = -91;
		lonDec = -361;
	}
	
	public TrackPoint(TrackPoint t) {
		latDec = t.latDec;
		lonDec = t.lonDec;
	}
	public TrackPoint(double lat, double lon) {
		latDec = lat;
		lonDec = lon;
	}
	public boolean equals(TrackPoint tp) {
		return latDec == tp.latDec && lonDec == tp.lonDec;
	}
	
	/**
	 * Returns true if the coordinates are valid
	 */
	public boolean isValid() {
		return 	latDec <= 90.0 && latDec >= -90.0 &&
				lonDec <= 360 && lonDec >= -360;
	}


}

