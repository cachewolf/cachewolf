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
	String compareWhat;
	String nmQuest, nmD,nmT,nmWay,nmName,nmLoc,nmOwn,nmHid,nmStat,nmDist,nmBear = new String();
	int visibleSize;
	Vector cacheDB;
	
	public MyComparer(Vector cacheDB, String what, int visibleSize){
		compareWhat = what;
		nmQuest = "?";
		nmD = MyLocale.getMsg(1000,"D");
		nmT = MyLocale.getMsg(1001,"T");
		nmWay = MyLocale.getMsg(1002,"Waypoint");
		nmName = MyLocale.getMsg(1003,"Name");
		nmLoc = MyLocale.getMsg(1004,"Location");
		nmOwn = MyLocale.getMsg(1005,"Owner");
		nmHid = MyLocale.getMsg(1006,"Hidden");
		nmStat = MyLocale.getMsg(1007,"Status");
		nmDist = MyLocale.getMsg(1008,"Dist");
		nmBear = MyLocale.getMsg(1009,"Bear");
		//visibleSize=Global.mainTab.tbP.myMod.numRows;
		if (visibleSize<2) return;
		for (int i=visibleSize; i<cacheDB.size(); i++) {
			CacheHolder ch=(CacheHolder) cacheDB.get(i);
			ch.sort="\uFFFF";
		}
		if (what.equals(nmQuest)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.type;
			}
		} else if (what.equals(nmD)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.hard;
			}
		} else if (what.equals(nmT)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.terrain;
			}
		} else if (what.equals(nmWay)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.wayPoint.toUpperCase();
			}
		} else if (what.equals(nmName)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheName.toLowerCase();
			}
		} else if (what.equals(nmLoc)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.LatLon;
			}
		} else if (what.equals(nmOwn)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheOwner.toLowerCase();
			}
		} else if (what.equals(nmHid)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				// Dates are in format M/D/Y
				int p1,p2=-1;
				p1=ch.DateHidden.indexOf("/");
				if (p1>0) p2=ch.DateHidden.indexOf("/",p1+1);
				if (p1>0 && p2>0) {
					ch.sort=ch.DateHidden.substring(p2+1)+
					        (p1==1?"0":"")+ch.DateHidden.substring(0,p1)+
					        (p1+2==p2?"0":"")+ch.DateHidden.substring(p1+1,p2);
				} else
					ch.sort="";
			}
		} else if (what.equals(nmStat)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.CacheStatus;
			}
		} else if (what.equals(nmDist)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				int p=ch.distance.indexOf(",");
				if (p<0) p=ch.distance.indexOf(".");
				if (p>=0 && p<=5)
					ch.sort="00000".substring(0,5-p)+ch.distance;
				else
					ch.sort=ch.distance;
			}
		} else if (what.equals(nmBear)) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=(CacheHolder) cacheDB.get(i);
				ch.sort=ch.bearing;
			}
			
		}
	}
	
	public int compare(Object o1, Object o2){
		CacheHolder oo1 = (CacheHolder)o1;
		CacheHolder oo2 = (CacheHolder)o2;
		return oo1.sort.compareTo(oo2.sort);
	}
}
