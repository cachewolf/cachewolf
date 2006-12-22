package CacheWolf;

import ewe.sys.Convert;

public final class Common {

	public static double parseDouble(String value){
		String a = new String();
		String b = new String();
		char separator = '.';
		double aDbl, bDbl;
		
		
		if (value.indexOf('.') < 0) {
			if (value.indexOf(',') < 0) value = value + ".0"; // no separartor
			else separator = ',';
		}
		else separator = '.';
		
		a = value.substring(0, value.indexOf(separator));
		aDbl = Convert.toDouble(a);

		
		b = value.substring(value.indexOf(separator)+1);
		bDbl = Convert.toDouble(b);
		// Calc Minutes
		bDbl = bDbl / java.lang.Math.pow((double)10,(double)b.length());
		
		return aDbl>=0?aDbl + bDbl:aDbl - bDbl;
	}
	public static String rot13 (String text) {
		String dummy = new String();
		for(int i = 0; i < text.length(); i++){
		  switch (text.charAt(i)){
			case 'A': dummy = dummy + "N"; break;
			case 'B': dummy = dummy + "O"; break;
			case 'C': dummy = dummy + "P"; break;
			case 'D': dummy = dummy + "Q"; break;
			case 'E': dummy = dummy + "R"; break;
			case 'F': dummy = dummy + "S"; break;
			case 'G': dummy = dummy + "T"; break;
			case 'H': dummy = dummy + "U"; break;
			case 'I': dummy = dummy + "V"; break;
			case 'J': dummy = dummy + "W"; break;
			case 'K': dummy = dummy + "X"; break;
			case 'L': dummy = dummy + "Y"; break;
			case 'M': dummy = dummy + "Z"; break;
			case 'N': dummy = dummy + "A"; break;
			case 'O': dummy = dummy + "B"; break;
			case 'P': dummy = dummy + "C"; break;
			case 'Q': dummy = dummy + "D"; break;
			case 'R': dummy = dummy + "E"; break;
			case 'S': dummy = dummy + "F"; break;
			case 'T': dummy = dummy + "G"; break;
			case 'U': dummy = dummy + "H"; break;
			case 'V': dummy = dummy + "I"; break;
			case 'W': dummy = dummy + "J"; break;
			case 'X': dummy = dummy + "K"; break;
			case 'Y': dummy = dummy + "L"; break;
			case 'Z': dummy = dummy + "M"; break;
			case 'a': dummy = dummy + "n"; break;
			case 'b': dummy = dummy + "o"; break;
			case 'c': dummy = dummy + "p"; break;
			case 'd': dummy = dummy + "q"; break;
			case 'e': dummy = dummy + "r"; break;
			case 'f': dummy = dummy + "s"; break;
			case 'g': dummy = dummy + "t"; break;
			case 'h': dummy = dummy + "u"; break;
			case 'i': dummy = dummy + "v"; break;
			case 'j': dummy = dummy + "w"; break;
			case 'k': dummy = dummy + "x"; break;
			case 'l': dummy = dummy + "y"; break;
			case 'm': dummy = dummy + "z"; break;
			case 'n': dummy = dummy + "a"; break;
			case 'o': dummy = dummy + "b"; break;
			case 'p': dummy = dummy + "c"; break;
			case 'q': dummy = dummy + "d"; break;
			case 'r': dummy = dummy + "e"; break;
			case 's': dummy = dummy + "f"; break;
			case 't': dummy = dummy + "g"; break;
			case 'u': dummy = dummy + "h"; break;
			case 'v': dummy = dummy + "i"; break;
			case 'w': dummy = dummy + "j"; break;
			case 'x': dummy = dummy + "k"; break;
			case 'y': dummy = dummy + "l"; break;
			case 'z': dummy = dummy + "m"; break;
			default: dummy = dummy + text.charAt(i);
		  }//switch
		}// for
		return dummy;
	}
	
	public static String stringToHex(String str){
		StringBuffer strBuf = new StringBuffer();
		StringBuffer strHex = new StringBuffer();
		StringBuffer strTxt = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			strHex.append(Convert.longToHexString(str.charAt(i)) + " ");
			strTxt.append(str.charAt(i)+ "  ");
		}
		strBuf.append(strTxt);
		strBuf.append("\n");
		strBuf.append(strHex);
		return strBuf.toString();
	}


}
