package CacheWolf;

import ewe.sys.*;

/**
*	This is a powerfull class that is used very often. It is quicker than
*	XML parsing and should be used whenever possible to find and extract
*	parts of a string in a string.
*/
public class Extractor  {
		int startOffset = 0;
		String searchText = new String();
		String start = new String();
		String end = new String();
		String tst = new String();
		boolean betweenonly = false;
		
		/**
		*	Create an extractor.
		*	sTxt = The string to search through.<br>
		*	st = The string that denotes the start of the string to extract<br>
		*	e = The string that denotes the end of the string to extract<br>
		*	sOff = The beginning offset from which to start the search in sTxt<br>
		*	only = if false the string returned will inlcude st and e; if true
		*	it will not include st and e.
		*
		*/
		public Extractor(String sTxt, String st, String e, int sOff, boolean only){
			startOffset = sOff;
			searchText = sTxt;
			//Vm.debug("Start " + st);
			end = e;
			//Vm.debug("End: " + e);
			start = st;
			betweenonly = only;
		}
		
		/**
		 * Mehtod to set the source text to be searched through
		 * 
		 */
		public void setSource(String sTxt){
			searchText = sTxt;
			//Vm.debug("Searchtext: " + searchText);
			startOffset = 0;
		}
		
		/**
		* Method that informs if the search has encountered the end of the string
		* that is being searched through.
		*/
		public boolean endOfSearch(){
			if(startOffset >= searchText.length()) return true;
			else return false;
		}
		
		/**
		*	Method to find the next occurance of a string that is enclosed by
		*	that start (st) and end string (e).
		*/
		public String findNext(){
			int idxStart = searchText.indexOf(start,startOffset);
			int idxEnd = searchText.indexOf(end, idxStart+start.length());
			////Vm.debug("Start: " + Convert.toString(idxStart) + " End: " + Convert.toString(idxEnd));
			if(idxEnd == -1) idxEnd = searchText.length();
			startOffset = idxEnd;
			tst = new String();
			if(idxStart > -1){
				if(betweenonly == false){
					tst = searchText.substring(idxStart,idxEnd+1);
				}else{ 
					tst = searchText.substring(idxStart+start.length(),idxEnd);
				}
			} else {
				startOffset = searchText.length();
			}
			return tst;
		}
}
