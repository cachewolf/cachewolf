package CacheWolf;
/*
 *  CacheWolf - Local settings class
 * 
 */

import CacheWolf.utils.FileBugfix;
import ewe.fx.Rect;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.sys.Long;
import ewe.ui.SplittablePanel;
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
	/** This language used if the system language is not supported by CacheWolf */
	final static String standardLanguage = "EN";

	private static Locale l=null;
	private static LocalResource lr=null;
	private static Rect s = (Rect)Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new Rect(),0);
	private static String digSeparator=null;
	/** Read a non-standard language from the file language. If it is empty,
	 * the default language is used.
	 */
	public static String language=getLanguage();
	
	public static String initErrors;

	private static String getLocaleFileName(String languageshort) {
		return FileBase.makePath(FileBase.getProgramDirectory(), "languages/" + languageshort.toUpperCase() + ".cfg");
	}

	/**
	 * This is needed because of 2 Bugs (not supporting French and inconsistant LocaleID in the ewe-VM v1.49
	 * For details see comments in method body
	 * [maybe one is inherited from windows: not supporting french]
	 * @param language_ 2 digits of language code as specified in ISO
	 */
	private static void setLocale(String language_) {
		String country ="";
		/*
		int[] all=ewe.sys.Locale.getAllIDs(0);
		Locale ll = new Locale(true);
		Global.getPref().debug=true;
		for (int i = 0; i<all.length; i++){
			ll.set(all[i]);
			String lg = ll.getString(13,0,0);
			if (!language_.equalsIgnoreCase(lg)) continue;
			country = ll.getString(23,0,0);
			// Global.getPref().log(lg+"-"+country);
			if (!country.equals("")) break;
		}
		 */
		if (language_.equalsIgnoreCase("DE")) country="DEU"; 
		if (language_.equalsIgnoreCase("FR")) country="FRA"; 
		if (language_.equalsIgnoreCase("EN")) country="USA"; 
		if (language_.equalsIgnoreCase("NL")) country="NLD"; 
		if (language_.equalsIgnoreCase("PL")) country="POL"; 
		if (language_.equalsIgnoreCase("SV")) country="SWE"; 
		int tmp = Locale.createID(language_, country, 0); // in ewe-vm v1.49 this call is enough to set the locale correctly and this works even with not supported languages like FR (french), e.g. it works even if tmp == -1, call new Locale() instead of new Locale(tmp) then.
		tmp = (tmp >= 1024 ? tmp-1024 : tmp); // ewe-vm v1.49 some times returns the correct value + 1024
		// Vm.debug("spec-lang: " + tmp);
		if (tmp > -1)	l = new Locale(tmp);
		else 			l = Locale.createFor("EN", "", 0 /*Locale.FORCE_CREATION*/); // forcing the requiered language doesn't work, because Locale.numberformat and so on cannot determine the requested format then. BTW: if French is system language new Locale() works even in ewe-vm v1.49
		resourcelanguage = language_;
	}

	/** 
	 * This is used to determine the language file name - it is necessary because
	 * ewe-vm v1.49 doesn't support French  
	 */
	static String resourcelanguage;
	static boolean inInit = false;

	private static void init() throws IllegalThreadStateException {
		if (inInit) {
			throw new IllegalThreadStateException("init may not be run twice"); // this can happen, if ewe is loading another class in background, which causes a call to e.g. MyLocale.getDigSeperator (most likely in a static statement). Ewe-Vm v1.49 seems to be loading static classes ahead, causing the danger of this problem.
		}
		inInit = true;
		initErrors = "";
		// the following logic priority: 1. try to use specified language (if specified), 2. try to use system language, 3. try to use english, 4. use hard coded messages
		l = null;
		if ( ( language.length()!=0 ) && ( !language.equalsIgnoreCase("auto") ) ) { // Was a language explicitly specified?
			setLocale(language);
			if (! (new FileBugfix(getLocaleFileName(resourcelanguage)).exists()) ) {
				l = null; // language file not found
				initErrors += "Language " + language + " not found - using system language\n";// don't copy this messagebox into a language file, because it is only used if no languages file can be accessed
			}
		}
		if ( l == null ) { // no language specified OR specified language not available -> use system default
			setLocale(Vm.getLocale().getString(Locale.LANGUAGE_SHORT, 0, 0)); 
			// test if a localisation file for the system language exists
			if (!(new FileBugfix(getLocaleFileName(resourcelanguage)).exists())) {
				setLocale(standardLanguage);
				initErrors += "Your system language is not supported by cachewolf - using English\n You can choose a different language in the preferences\n";
				/*       //uncomment this code to print a list of all supported languge (Locales), remark: this differs from vm to vm _and_ from OS to OS
  					 Vm.debug("gewählte Sprache: " + resourcelanguage, 0, 0);
					 int [] all = Locale.getAllIDs(0);
					 Locale ltmp = new Locale(); 
					 for (int i = 0; i<all.length; i++){
						 ltmp.set(all[i]);
						 String lg = ltmp.getString(Locale.LANGUAGE_SHORT,0,0);
						 Vm.debug(i + "sprache: " + lg + " (" + ltmp.getString(Locale.LANGUAGE, 0, 0) + ", " + ltmp.getString(Locale.LANGUAGE_ENGLISH, 0, 0) + ") land: " + ltmp.getString(Locale.COUNTRY, 0, 0));
					 }
				 */
			}
		}
		lr = null;
		if (new FileBugfix(getLocaleFileName(resourcelanguage)).exists() ) {
			ewe.io.TreeConfigFile tcf = ewe.io.TreeConfigFile.getConfigFile(getLocaleFileName(resourcelanguage));
			if (tcf != null) {			 
				lr = tcf.getLocalResourceObject(new Locale() {
					public String getString(int what,int forValue,int options) {
						if (what == LANGUAGE_SHORT) return resourcelanguage; // this is necessary because French cannot be set in ewe-vm v1.49
						else return super.getString(what, forValue, options);
					}
				}, "cachewolf.Languages");
			}
		}
		if (lr == null) {
		//	Vm.debug("lr==null 1");
			initErrors += "Language file " + getLocaleFileName(resourcelanguage) + " couldn't be loaded - using hard coded messages";
		//	Vm.debug("lr==null 2");
			lr = new LocalResource(){
				public Object get(int id,Object data){return data;}
				public Object get(String id,Object data){return data;}
			};

		}
		double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		if(testA == 4.5) digSeparator = ","; else digSeparator = ".";
		inInit = false;
	}

	/**
	 * Return a localized string
	 * 
	 * The localized strings are stored in the configuration file (relative to
	 *  executable:<br>
	 *  	_config/cachewolf.Languages.cfg
	 * If the configuration file does not exist or a string cannot be found in
	 * the file, the defaultValue is returned.
	 *   
	 * @param resourceID   The unique number of the resource
	 * @param defaultValue The default value of the string (if not found in the configuration file)
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
		if (Vm.isMobile()) {
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
		if (Vm.isMobile()) {
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
		if (Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
	}

	/**
	 * This method modifies the thickness of splitter on modern PDAs with a VGA resolution or better.
	 * @param split
	 */
	public static void setSplitterSize(SplittablePanel split) {
		if (Vm.isMobile() && MyLocale.getScreenHeight() > 400) {
				split.theSplitter.thickness = 20;
			
		}
	}

	/**
	 * Read the language from the prefs and return the specified language (or empty
	 * string if none specified).
	 * @return Language (e.g. DE, EN etc.) or ""
	 */
	private static String getLanguage() {
		Preferences pref = Global.getPref();
		if ( pref != null ) {
			language = pref.language;
		} else {
			language = "";
		}
		if (language==null) language="";
		return language;
	}

	/**
	 * Write the override language 
	 * @param language The language to write
	 */
	public static void saveLanguage(String saveLanguage) {
		Preferences pref = Global.getPref();
		if ( pref != null ) {
			pref.language = saveLanguage;
			pref.savePreferences();
		}
	}

	/**
	 * Returns the path to a localized version of a help file
	 * @param basename
	 * @return
	 */
	public static String getLocalizedFile (String basename){
		String lang = MyLocale.getLocaleLanguage();
		String programmDirectory = FileBase.getProgramDirectory() + "/";
		int index = basename.lastIndexOf('.');
		String prefix = basename.substring(0,index);
		String suffix = basename.substring(index);
		File f = new File (programmDirectory, prefix+'_'+lang+suffix);
		if (f.exists()){
			return f.toString();
		}
		else{
			return new File (programmDirectory,basename).toString();
		}
	}


}

