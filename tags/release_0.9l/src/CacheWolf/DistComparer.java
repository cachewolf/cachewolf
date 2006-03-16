package CacheWolf;
import ewe.util.*;
import ewe.sys.*;

/**
*	This class handles sorting of caches according to distance.
*	All other sorts are handled by MyComparer.
*	@see MyComparer
*/
public class DistComparer implements Comparer{
	Locale l = Vm.getLocale();
	
	public int compare(Object o1, Object o2){
		int ret = 0;
		CacheHolder oo1 = (CacheHolder)o1;
		CacheHolder oo2 = (CacheHolder)o2;
		if(oo1.is_filtered == false && oo2.is_filtered == false){
			String str01 = new String();
			String str02 = new String();
			String strA = new String();
			String strB = new String();
			strA = oo1.distance.substring(0,oo1.distance.length()-3);
			strB = oo2.distance.substring(0,oo2.distance.length()-3);
			/*
			if(l.getString(Locale.LANGUAGE_SHORT ,0,0).equals("DE")){
				//Vm.debug(strA);
				strA = strA.replace(',', '.');
				strB = strB.replace(',', '.');
			}*/
			double A = Common.parseDouble(strA);
			double B = Common.parseDouble(strB);
			if(A >= B) ret =  -1;
			if(A < B) ret =  1;
			return ret;
		} else {
			int retval = 0;
			if(oo1.is_filtered == false && oo2.is_filtered == true) retval = -1;
			if(oo1.is_filtered == true && oo2.is_filtered == false) retval = 1;
			
			return retval;
		}
	}
}
