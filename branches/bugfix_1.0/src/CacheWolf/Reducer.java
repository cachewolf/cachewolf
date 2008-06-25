package CacheWolf;
import ewe.util.*;

/**
*	Class to reduce a string to its main components,
*	so that a human may still understand what it means.
*	This is usefull for displaying cache names in a GPSR
*	or for POIs in a navigation software.
*
*	The idea is:
*	1) Throw out all 3 letter words,
*	2) Remove all vowels from a word, except if it is
*      the first letter of a word
*	3) Remove all Whitespace
*	4) If requested truncate the string to a given number of
*	   characters.
*/
public class Reducer{
	
	public static String convert(String origStr, boolean trun, int len){
		String[] parts = mString.split(origStr, ' ');
		String dummy = new String();
		String finalStr = new String();
		for(int i = 0; i < parts.length; i++){
			if(parts[i].length() != 3){
				dummy = removeVow(parts[i]);
				finalStr = finalStr + dummy;
			}
		}//for
		//Vm.debug(Convert.toString(trun));
		if(trun == true){
			finalStr = finalStr + "                                         ";
			finalStr = finalStr.substring(0,len);
			//Vm.debug(finalStr);
		}
		return finalStr;
	}
	
	private static String removeVow(String str){
		String dummy = str.substring(1);
		dummy = STRreplace.replace(dummy,"a", "");
		dummy = STRreplace.replace(dummy,"e", "");
		dummy = STRreplace.replace(dummy,"i", "");
		dummy = STRreplace.replace(dummy,"o", "");
		dummy = STRreplace.replace(dummy,"u", "");
		dummy = STRreplace.replace(dummy,"A", "");
		dummy = STRreplace.replace(dummy,"E", "");
		dummy = STRreplace.replace(dummy,"I", "");
		dummy = STRreplace.replace(dummy,"O", "");
		dummy = STRreplace.replace(dummy,"U", "");
		dummy = STRreplace.replace(dummy,",","");
		dummy = str.substring(0,1) + dummy;
		return dummy;
	}
}
