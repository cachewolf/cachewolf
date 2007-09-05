package CacheWolf.navi;

import ewe.io.BufferedWriter;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.ui.MessageBox;
import ewe.util.Utils;

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

}

