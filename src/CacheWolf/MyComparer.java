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
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	String nmQuest, nmD,nmT,nmWay,nmName,nmLoc,nmOwn,nmHid,nmStat,nmDist,nmBear = new String();
	
	public MyComparer(String what){
		compareWhat = what;
		nmQuest = "?";
		nmD = (String)lr.get(1000,"D");
		nmT = (String)lr.get(1001,"T");
		nmWay = (String)lr.get(1002,"Waypoint");
		nmName = (String)lr.get(1003,"Name");
		nmLoc = (String)lr.get(1004,"Location");
		nmOwn = (String)lr.get(1005,"Owner");
		nmHid = (String)lr.get(1006,"Hidden");
		nmStat = (String)lr.get(1007,"Status");
		nmDist = (String)lr.get(1008,"Dist");
		nmBear = (String)lr.get(1009,"Bear");
	}
	
	public int compare(Object o1, Object o2){
		CacheHolder oo1 = (CacheHolder)o1;
		CacheHolder oo2 = (CacheHolder)o2;
		if(oo1.is_filtered == false && oo2.is_filtered == false){
			
			String str01 = new String();
			String str02 = new String();
			if(compareWhat.equals(nmQuest)){
				str01 = oo1.type;
				str02 = oo2.type;
			}
			if(compareWhat.equals(nmD)){
				str01 = oo1.hard;
				str02 = oo2.hard;
			}
			if(compareWhat.equals(nmT)){
				str01 = oo1.terrain;
				str02 = oo2.terrain;
			}
			if(compareWhat.equals(nmWay)){
				str01 = oo1.wayPoint;
				str02 = oo2.wayPoint;
			}
			if(compareWhat.equals(nmName)){
				str01 = oo1.CacheName;
				str02 = oo2.CacheName;
			}
			if(compareWhat.equals(nmLoc)){
				str01 = oo1.LatLon;
				str02 = oo2.LatLon;
			}
			if(compareWhat.equals(nmOwn)){
				str01 = oo1.CacheOwner;
				str02 = oo2.CacheOwner;
			}
			if(compareWhat.equals(nmHid)){
				str01 = oo1.DateHidden;
				str02 = oo2.DateHidden;
			}
			if(compareWhat.equals(nmStat)){
				str01 = oo1.CacheStatus;
				str02 = oo2.CacheStatus;
			}
			if(compareWhat.equals(nmBear)){
				str01 = oo1.bearing;
				str02 = oo2.bearing;
			}
			if(compareWhat.equals("filter")){
				str01 = Convert.toString(oo1.is_filtered);
				str02 = Convert.toString(oo2.is_filtered);
			}
			return str01.toLowerCase().compareTo(str02.toLowerCase());
		}else{
			int retval = 0;
			if(oo1.is_filtered == false && oo2.is_filtered == true) retval = -1;
			if(oo1.is_filtered == true && oo2.is_filtered == false) retval = 1;
			
			return retval;
		}
		
	}
}
