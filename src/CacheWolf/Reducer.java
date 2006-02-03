package CacheWolf;
import ewe.util.*;
import ewe.sys.*;

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
		dummy = replace(dummy,"a", "");
		dummy = replace(dummy,"e", "");
		dummy = replace(dummy,"i", "");
		dummy = replace(dummy,"o", "");
		dummy = replace(dummy,"u", "");
		dummy = replace(dummy,"A", "");
		dummy = replace(dummy,"E", "");
		dummy = replace(dummy,"I", "");
		dummy = replace(dummy,"O", "");
		dummy = replace(dummy,"U", "");
		dummy = replace(dummy,",","");
		dummy = str.substring(0,1) + dummy;
		return dummy;
	}
	
	/** Replace all instances of a String in a String.
		 *   @param  s  String to alter.
		 *   @param  f  String to look for.
		 *   @param  r  String to replace it with, or null to just remove it.
		 */ 
		private static String replace( String s, String f, String r )
		{
		   if (s == null)  return s;
		   if (f == null)  return s;
		   if (r == null)  r = "";
		
		   int index01 = s.indexOf( f );
		   while (index01 != -1)
		   {
			  s = s.substring(0,index01) + r + s.substring(index01+f.length());
			  index01 += r.length();
			  index01 = s.indexOf( f, index01 );
		   }
		   return s;
		}
}
