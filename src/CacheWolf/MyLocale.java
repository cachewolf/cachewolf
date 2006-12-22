package CacheWolf;
/*
 *  CacheWolf - Local settings class
 * 
 */
import ewe.fx.Rect;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.ui.Gui;
import ewe.ui.Window;
/**
 *  This class handles internationalisation and some other local stuff like
 *  decimal separator, screen dimensions etc.
 *  
 *  The methods are static, the class does not need initialisation.
 *
 *  @author salzkammergut
 *  Changes:
 *    20061122 Changed name to MyLocale. Added screen width & height, formatLong, SIP functions
 *    20061124 Added formatDouble
 */
public class MyLocale {
	private static Locale l=null;
	private static LocalResource lr=null;
	private static Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
	private static String digSeparator=null;
	
	private static void init() {
		 l = Vm.getLocale();
		 lr = l.getLocalResource("cachewolf.Languages",true);

		 double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		 if(testA == 4.5) digSeparator = ","; else digSeparator = ".";
	}

	/**
	 * Return a localized string
	 * 
     * The localized strings are stored in the configuration file (relative to
     *  executable:<br>
     *  	_config/cachewolf.Languages.cfg
     * If the configuration file does not exist or a string cannot be found in
     * the file, the defaultValue is resurned.
     *   
	 * @param resourceID   The unique number of the resource
	 * @param defaultValue The default value of the string (if not found in the config file)
	 * @return The localized string 
	 */
	public static String getMsg(int resourceID, String defaultValue) {
		if (l==null) 
			init();
		if (lr!=null) { 
			String res;
			res=(String) lr.get(resourceID,defaultValue);
			if (res!=null) 
			   return res;
			//Fallthrough to default value if string does not exist in file
		}
		return defaultValue;
	}

	/**
	 * Get the ISO two letter (lowercase) name of the locale language
	 * 
	 * @return ISO two letter abbreviation of the locale language
	 */
	public static String getLocaleLanguage() {
		if (l==null) init();
		return l.getString(Locale.LANGUAGE_SHORT, 0, 0);		
	}

	/**
	 * Get the three letter (uppercase) ISO country code
	 * 
	 * @return The three letter (uppercase) ISO country code
	 */
	public static String getLocaleCountry() {
		if (l==null) init();
		return l.getString(Locale.COUNTRY_SHORT, 0, 0);		
	}

	/**
	 * Get the screen width
	 * @return Width of screen in pixels
	 */
	public static int getScreenWidth() {
		return s.width;
	}
	
	/**
	 * Get the screen height
	 * @return Height of screen in pixels
	 */
	public static int getScreenHeight() {
		return s.height;
	}

	/**
	 * Get the decimal separator for this machine
	 * @return decimal point ("." or ",")
	 */
	public static String getDigSeparator() {
		if (digSeparator==null) init();
		return digSeparator;
	}

	/**
	 * Formats a Long integer to a given format specifier
	 * @param number A Long which contains the number to be formatted
	 * @param fmt A string containing the format specification</br>
	 * '$' indicates that a currency symbol should be used. </br>
	 * ',' indicates that thousands groupings should be used. </br>
	 * '.' separates formatting before the decimal point and after the decimal point.</br>
	 * '0' before the decimal point indicates the number of digits before the decimal point.</br>
	 * @return The formatted number
	 */
	public static String formatLong(ewe.sys.Long number, String fmt) {
		if (l==null) init();
		return l.format(Locale.FORMAT_PARSE_NUMBER,number,fmt);
	}

	/**
	 * Formats a Double to a given format specifier
	 * @param number A Double containing the number to be formatted
	 * @param fmt A string containing the format specification</br> 
	 * @return The formatted number
	 */
	public static String formatDouble(ewe.sys.Double number, String fmt) {
		if (l==null) init();
		return l.format(Locale.FORMAT_PARSE_NUMBER,number,fmt);
	}
	
	/**
	 * Formats a Double to a given format specifier
	 * @param number A double containing the number to be formatted
	 * @param fmt A string containing the format specification</br> 
	 * @return The formatted number
	 */
	public static String formatDouble(double number, String fmt) {
		Double d=new Double();
		d.set(number);
		return formatDouble(d,fmt);
	}
	
	/**
	 * This function checks whether the device supports a
	 * supplementary input panel (SIP) and if yes, shows it.
	 *
	 */
	public static void setSIPOn() {
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_ON);
		}
	}
	
	/**
	 * This function checks whether the device supports a
	 * supplementary input panel (SIP) and if yes, hides it and
	 * also hides the button.
	 *
	 */
	public static void setSIPOff() {
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(0);
		}
	}
	
	/**
	 * This function checks whether the device supports a
	 * supplementary input panel (SIP) and if yes, hides it and just
	 * shows the button.
	 *
	 */
	public static void setSIPButton() {
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
	}
}

