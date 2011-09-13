    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;
import ewe.util.mString;

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
		String dummy;
		String finalStr="";
		for(int i = 0; i < parts.length; i++){
			if(parts[i].length() != 3){
				dummy = removeVow(parts[i]);
				finalStr = finalStr + dummy;
			}
		}
		if(trun == true){
			finalStr = finalStr + "                                         ";
			finalStr = finalStr.substring(0,len);
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
