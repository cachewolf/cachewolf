package cachewolf.utils;

import cachewolf.MyLocale;
import eve.io.File;

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

	public static int parseInt(String value){
		try {
			return java.lang.Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
	}
	/**
	 * Format a double as string with a given number of decimal places
	 * @param d The double to format
	 * @param decimalplaces number of digits after the decimal point
	 * @return Formatted number
	 */
	public static String doubleToString(double d, int decimalplaces) {
		String format;
		if (decimalplaces > 0) format="0.0000000000".substring(0,decimalplaces+2);
		else format = "0";
 		return MyLocale.formatDouble(d, format).replace(',', '.');
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
			else {
				dummy[i] = c;
			}
		}// for
		return new String(dummy);
	}

/* Not used. SKG 20080316
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
	public static String clearForFileName(String str) {
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
		File tmp;
		String[] t = {".png", ".gif", ".jpg", ".bmp"};
		int i;
		for (i = 0; i<t.length; i++) {
			tmp = new File(name+t[i]);
			if (tmp.exists()) break;
		}
		if (i >=t.length) fileName = null;
		else fileName = name+t[i];
		return fileName;
	}
	/** get the extension of a filename, including "."
	 * remark: eve.io.File.getFileExtension return name + extension
	 * @param fn
	 * @return
	 */
	public static String getFilenameExtension (String fn) {
		if (fn == null || fn.length() == 0) return "";
		int dot = fn.lastIndexOf(".");
		if (dot < 0) return "";
		return fn.substring(dot, fn.length());
	}

	public static String fixSerialPortName(String name) {
		if (name.startsWith("/"))
			return new String(".."+name); // on linux (*nix) machines it is quite usual to give the complete file path to the serial port, but ewe expects only "ttyS0" or similar
		else
			return name;
	}

}
