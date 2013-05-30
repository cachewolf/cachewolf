    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
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


import CacheWolf.utils.FileBugfix;

public final class Common {

	private static char digSep=MyLocale.getDigSeparator().charAt(0);
	private static char notDigSep=MyLocale.getDigSeparator().charAt(0)=='.'?',':'.';
	
	/**
	 * get double value from string. It interpretes "." and "," as decimal separator
	 * when the string cannot be interpreted, return 0.
	 * @param value
	 * @return
	 */
	public static double parseDouble(String value){
		// returns 0 for invalid arguments
		try {
			return parseDoubleException (value);
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * throws some exception if the string could not be converted to double
	 * @param value
	 * @return
	 */
	public static double parseDoubleException (String value) {
			return java.lang.Double.parseDouble(value.replace(notDigSep,digSep));
	}
	
	/**
	 * throws no exception if the string could not be converted to int.
	 * returns 0 in that case
	 * @param String value
	 * @return int java.lang.Integer.parseInt(value)
	 */
	public static int parseInt(String value){
		try {
			return java.lang.Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * (De)codes the given text with rot13.
	 * Text in [] won't be (de)coded.
	 * @param text will be (de)coded in rot13
	 * @return rot13 of text
	 */
	public static String rot13(String text) {
		char[] dummy = new char[text.length()];
		boolean convert = true;
		char c;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);

			if (convert && ((c >= 'a' && c <= 'm') || (c >= 'A' && c <= 'M'))) {
				dummy[i] = (char) (c + 13);
			} 
			else if (convert && ((c >= 'n' && c <= 'z') || (c >= 'N' && c <= 'Z'))) {
				dummy[i] = (char) (c - 13);
			} 
			else if (c == '[') {
				convert = false;
				dummy[i] = '[';
			} 
			else if (c == ']') {
				convert = true;
				dummy[i] = ']';
			}
			else if (c == '<') {
				dummy[i]='<';
				// reflecting html break (newline) only lowercase
				if (i+3<text.length() && text.charAt(i+1)=='b' && text.charAt(i+2)=='r' && text.charAt(i+3)=='>') {
					i++;
					dummy[i]='b';
					i++;
					dummy[i]='r';
					i++;
					dummy[i]='>';
				}
				else {
					if (i+2<text.length() && text.charAt(i+1)=='p' && text.charAt(i+2)=='>') {
						i++;
						dummy[i]='p';
						i++;
						dummy[i]='>';
					}
				}
			}
			else {
				dummy[i] = c;
			}
		}// for
		return new String(dummy);
	}
/*	
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
*/	
	public static String ClearForFileName(String str) {
		String ret = str.replace('?', '_');
		ret = ret.replace(' ', '-');
		ret = ret.replace(':', '-');
		return ret;
	}
	
	/**
	 * finds the correct (existing) extension to an image filename
	 * @param filename without extension
	 * @return filename with extension 
	 */
	static public String getImageName(String name) {
		String fileName;
		FileBugfix tmp = null;
		String[] t = {".png", ".gif", ".jpg", ".bmp"};
		int i;
		for (i = 0; i<t.length; i++) {
			if (tmp == null) tmp = new FileBugfix(name+t[i]);
			else tmp.setText(name+t[i]);
			if (tmp.exists()) break;
		}
		if (i >=t.length) fileName = null;
		else fileName = name+t[i];
		return fileName;
	}
	/** get the extension of a filename, including "."
	 * remark: ewe.io.File.getFileExtension return name + extension
	 * @param fn
	 * @return
	 */
	public static String getFilenameExtension (String fn) {
		if (fn == null || fn.length() == 0) return "";
		int dot = fn.lastIndexOf('.');
		if (dot < 0) return "";
		return fn.substring(dot, fn.length());
	}
	/** get the File.getDrivePath of a filename
	 * @param fn
	 * @return
	 */
	public static String getFilename (String fn) {
		if (fn == null || fn.length() == 0) return "";
		int dot = fn.lastIndexOf('.');
		if (dot < 0) return fn;
		return fn.substring(0, dot);
	}
	
	public static String DoubleToString(double d, int length, int decimalplaces) {
		ewe.sys.Double e = new ewe.sys.Double();
		e.set(d);
		return e.toString(length,decimalplaces,0).replace(',', '.');

	}
	
	public static String fixSerialPortName(String name) {
		if (name.startsWith("/")) return ".."+name; // on linux (*nix) machines it is quite usual to give the complete file path to the serial port, but ewe expects only "ttyS0" or similar
		else                      return name;
	}
	
	public static String arrayToString(String[] a, String sep) {
		if (a == null || a.length == 0) return "";
		StringBuffer sb = new StringBuffer();
		sb.append(a[0]);
		for (int i=1; i < a.length; i++) {
			sb.append(sep).append(a[i]);
		}
		return sb.toString();
	}
	
	/**
	 * right pad with spaces
	 * @param s
	 * @param length
	 * @return
	 */
	public static String rightPad(String s, int length) {
		String p = "                                                          ";
		return s + p.substring(s.length()+(p.length()-length));
	}


}
