package CacheWolf;
/*
 *  CacheWolf - Local settings class
 * 
 */
import ewe.fx.Rect;
import ewe.io.BufferedReader;
import ewe.io.BufferedWriter;
import ewe.io.FileBase;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.sys.Long;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.MessageBox;
import ewe.ui.Window;
import ewe.ui.WindowConstants;
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
	private static Rect s = (Rect)Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new Rect(),0);
	private static String digSeparator=null;
	/** Read a non-standard language from the file language. If it is empty,
	 * the default language is used.
	 */
	public static String language=getLanguage();
	
	private static void init() {
		 if ( ( language.length()==0 ) || ( language.equalsIgnoreCase("auto") ) ) // Was a non-standard language specified
			 l = Vm.getLocale();
		 else {
			 int tmp = Locale.createID(language,"",0);
			 if (tmp > -1) l=new Locale(tmp);
			 else { // language not found
				 (new mThread() { // start a new thread is necessary because the simple ewe v1.49 threading model doesn't allow displaying of a messageBox in this kind of thread
					 public void run() {
						 (new MessageBox("Error", "Language " + language + " not found - using standard language", FormBase.OKB)).execute(); // don't copy this messagebox into a language file, because it is only used if no languages file can be accessed
					 }
				 }).start();
				 l = Vm.getLocale(); 
			 }
		 }
		 ewe.io.TreeConfigFile tcf = ewe.io.TreeConfigFile.getConfigFile("languages/" + getLocaleLanguage().toUpperCase() + ".cfg");
		 if (tcf != null){			 
			 lr = tcf.getLocalResourceObject(l,"cachewolf.Languages");
		 }
		 else {
			 lr = new LocalResource(){
				 public Object get(int id,Object data){return data;}
				 public Object get(String id,Object data){return data;}
			 };
			 (new mThread() { // start a new thread is necessary because the simple ewe v1.49 threading model doesn't allow displaying of a messageBox in this kind of thread
				 public void run() {
					 (new MessageBox("Error", "Language file languages/" + getLocaleLanguage().toUpperCase() + ".cfg couldn't be loaded - using hard coded messages", FormBase.OKB)).execute();
				 }
			 }).start();
		 }

		 double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		 if(testA == 4.5) digSeparator = ","; else digSeparator = ".";
	}

	/**
	 * Return a localised string
	 * 
     * The localised strings are stored in the configuration file (relative to
     *  executable:<br>
     *  	_config/cachewolf.Languages.cfg
     * If the configuration file does not exist or a string cannot be found in
     * the file, the defaultValue is resurned.
     *   
	 * @param resourceID   The unique number of the resource
	 * @param defaultValue The default value of the string (if not found in the config file)
	 * @return The localised string 
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
	public static String formatLong(Long number, String fmt) {
		if (l==null) init();
		return l.format(Locale.FORMAT_PARSE_NUMBER,number,fmt);
	}

	/**
	 * Formats a long to a given format specifier
	 * @param number A long containing the number to be formatted
	 * @param fmt A string containing the format specification</br> 
	 * @return The formatted number
	 */
	public static String formatLong(long number, String fmt) {
		Long L=new Long();
		L.set(number);
		return formatLong(L,fmt);
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
	
	/*=================================================================
	 * During initialisation the file "language" in the program directory
	 * is read to check whether the user wishes to ovverride the default
	 * language. This language cannot be stored in the pref.xml file, due
	 * to an initialisation conflict (pref.xml needs MyLocale). A better
	 * solution may be to read the override language from the command line,
	 * but I do not know how to specify command line parameters on a PDA
	 ==================================================================*/
	/**
	 * Read the language file and return the specified language (or empty
	 * string if none specified).
	 * @return Language (e.g. DE, EN etc.) or ""
	 */
	private static String getLanguage() {
		try {
			BufferedReader bufread=new BufferedReader(new FileReader(FileBase.getProgramDirectory() + "/" + "language"));
			language=bufread.readLine();
			bufread.close();
		} catch (Exception ex) {
			language="";
		}
		if (language==null) language="";
		return language;
	}
	
	/**
	 * Write the override language 
	 * @param language The language to write
	 */
	public static void saveLanguage(String language) {
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(FileBase.getProgramDirectory() + "/" + "language"));
			out.write(language);
			out.close();
		}catch (Exception e){ Vm.debug("Exception "+e);}
	}
	
}

