package CacheWolf;
import ewe.util.*;
import ewe.sys.*;

/**
*	This class handles the sorting for most of the sorting tasks. If a cache is 
*	to be displayed in the table or not is handled in the table model
*	@see MyTableModel
*	@see DistComparer
*/
public class MyComparer implements Comparer{
	Vector cacheDB;
	
	public MyComparer(Vector cacheDB, int colToCompare, int visibleSize){
		//visibleSize=Global.mainTab.tbP.myMod.numRows;
		if (visibleSize<2) return;
		for (int i=visibleSize; i<cacheDB.size(); i++) {
			CacheHolder ch=(CacheHolder) cacheDB.get(i);
			ch.sort="\uFFFF";
		}
		if (colToCompare==1) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.type;
			}
		} else if (colToCompare==2) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.hard;
			}
		} else if (colToCompare==3) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.terrain;
			}
		} else if (colToCompare==4) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.wayPoint.toUpperCase();
			}
		} else if (colToCompare==5) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheName.toLowerCase();
			}
		} else if (colToCompare==6) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.LatLon;
			}
		} else if (colToCompare==7) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheOwner.toLowerCase();
			}
		} else if (colToCompare==8) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.DateHidden;
			}
		} else if (colToCompare==9) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheStatus;
			}
		} else if (colToCompare==10) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				int p=ch.distance.indexOf(",");
				if (p<0) p=ch.distance.indexOf(".");
				if (p>=0 && p<=5)
					ch.sort="00000".substring(0,5-p)+ch.distance;
				else
					ch.sort=ch.distance;
			}
		} else if (colToCompare==11) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.bearing;
			}
			
		} else if (colToCompare==12) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				if (ch.CacheSize.length()==0) ch.sort="?";
				else switch (ch.CacheSize.charAt(0)) {
					case 'M': ch.sort="1"; break;
					case 'S': ch.sort="2"; break;
					case 'R': ch.sort="3"; break;
					case 'L': ch.sort="4"; break; 
					case 'V': ch.sort="5"; break;
					default: ch.sort="?";
				}
			}
		} else if (colToCompare==13) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				if (ch.wayPoint.startsWith("OC"))
					ch.sort=MyLocale.formatLong((long)ch.numRecommended,"00000");
				else
					ch.sort="\uFFFF";
			}			
		} else if (colToCompare==14) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				if (ch.wayPoint.startsWith("OC"))
					ch.sort=MyLocale.formatLong((long)ch.recommendationScore,"00000");
				else
					ch.sort="\uFFFF";
			}			
		}
	}
	
	public int compare(Object o1, Object o2){
		CacheHolder oo1 = (CacheHolder)o1;
		CacheHolder oo2 = (CacheHolder)o2;
		return oo1.sort.compareTo(oo2.sort);
	}
}
