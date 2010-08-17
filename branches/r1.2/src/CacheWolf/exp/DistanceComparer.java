package CacheWolf.exp;

import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import ewe.util.Comparer;

public class DistanceComparer implements Comparer {
	
	CWPoint centre;
	
	public DistanceComparer(CWPoint centre) {
		this.centre = centre;
	}

	public int compare(Object one, Object two) {
		if ((! (one instanceof CacheHolder)) && (!(two instanceof CacheHolder))) {
			return 0;
		} else {
			CacheHolder a = (CacheHolder) one;
			CacheHolder b = (CacheHolder) two;
			return (int) ((a.pos.getDistance(centre) - b.pos.getDistance(centre)) * 1000);
		}
	}
	
}
