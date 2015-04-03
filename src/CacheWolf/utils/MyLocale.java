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
package CacheWolf.utils;

/*
 *  CacheWolf - Local settings class
 * 
 */

import ewe.fx.Rect;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.TreeConfigFile;
import ewe.io.TreeConfigNode;
import ewe.sys.Double;
import ewe.sys.LocalResource;
import ewe.sys.Locale;
import ewe.sys.Long;
import ewe.sys.Vm;
import ewe.ui.SplittablePanel;
import ewe.ui.Window;
import ewe.ui.WindowConstants;

/**
 * This class handles internationalisation and some other local stuff like
 * decimal separator, screen dimensions etc.
 * 
 * The methods are static, the class does not need initialisation.
 * 
 * @author salzkammergut
 *         Changes:
 *         20061122 Changed name to MyLocale. Added screen width & height, formatLong, SIP functions
 *         20061124 Added formatDouble
 */
public class MyLocale {
    /** This language used if the system language is not supported by CacheWolf */
    private final static String standardLanguage = "EN";
    /**
     * must be set once before (first implicit) call of init()<br>
     * get it from Preferences two Char language Code or AUTO
     */
    public static String language = "EN";

    private static Locale l = null;
    private static LocalResource lr = null;
    // size of maximized window
    private static Rect screenSize = (Rect) Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT, null, new Rect(), 0);

    public static String initErrors;

    private static String getLocaleFileName(String languageshort) {
	return FileBase.makePath(FileBase.getProgramDirectory(), "languages/" + languageshort.toUpperCase() + ".cfg");
    }

    /**
     * This is needed because of 2 Bugs (not supporting French and inconsistant LocaleID in the ewe-VM v1.49
     * For details see comments in method body
     * [maybe one is inherited from windows: not supporting french]
     * 
     * @param language_
     *            2 digits of language code as specified in ISO
     */
    private static void setLocale(String language_) {
	String country = "";
	if (language_.equalsIgnoreCase("DE"))
	    country = "DEU";
	if (language_.equalsIgnoreCase("FR"))
	    country = "FRA";
	if (language_.equalsIgnoreCase("EN"))
	    country = "USA";
	if (language_.equalsIgnoreCase("NL"))
	    country = "NLD";
	if (language_.equalsIgnoreCase("PL"))
	    country = "POL";
	if (language_.equalsIgnoreCase("SV"))
	    country = "SWE";
	int tmp = Locale.createID(language_, country, 0); // in ewe-vm v1.49 this call is enough to set the locale correctly and this works even with not supported languages like FR (french), e.g. it works even if tmp == -1, call new Locale() instead of new Locale(tmp) then.
	tmp = (tmp >= 1024 ? tmp - 1024 : tmp); // ewe-vm v1.49 some times returns the correct value + 1024
	if (tmp > -1)
	    l = new Locale(tmp);
	else
	    l = Locale.createFor("EN", "", 0 /*Locale.FORCE_CREATION*/); // forcing the requiered language doesn't work, because Locale.numberformat and so on cannot determine the requested format then. BTW: if French is system language new Locale() works even in ewe-vm v1.49
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
	    // this can happen, if ewe is loading another class in background, which causes a call to e.g. MyLocale.getDigSeperator (most likely in a static statement). 
	    // Ewe-Vm v1.49 seems to be loading static classes ahead, causing the danger of this problem.
	    throw new IllegalThreadStateException("init may not be run twice");
	}
	inInit = true;
	initErrors = "";
	// the following logic priority: 1. try to use specified language (if specified), 2. try to use system language, 3. try to use english, 4. use hard coded messages
	l = null;
	if ((language.length() != 0) && (!language.equalsIgnoreCase("auto"))) { // Was a language explicitly specified?
	    setLocale(language);
	    if (!(new File(getLocaleFileName(resourcelanguage)).exists())) {
		l = null; // language file not found
		initErrors += "Language " + language + " not found - using system language\n";
		// don't copy this into a language file, because it is only used if no languages file can be accessed
	    }
	}
	if (l == null) {
	    // no language specified OR specified language not available -> use system default
	    setLocale(Vm.getLocale().getString(Locale.LANGUAGE_SHORT, 0, 0));
	    // test if a localisation file for the system language exists
	    if (!(new File(getLocaleFileName(resourcelanguage)).exists())) {
		setLocale(standardLanguage);
		initErrors += "Your system language is not supported by cachewolf - using English\n You can choose a different language in the preferences\n";
	    }
	}
	lr = null;
	if (new File(getLocaleFileName(resourcelanguage)).exists()) {
	    // ewe.io.TreeConfigFile.getConfigFile(getLocaleFileName(resourcelanguage));
	    // above replaced for to change resourcefile to have UTF-8 Codec
	    TreeConfigFile tcf = getConfigFile(getLocaleFileName(resourcelanguage));

	    if (tcf != null) {
		lr = tcf.getLocalResourceObject(new Locale() {
		    public String getString(int what, int forValue, int options) {
			// this is necessary because French cannot be set in ewe-vm v1.49
			if (what == LANGUAGE_SHORT)
			    return resourcelanguage;
			else
			    return super.getString(what, forValue, options);
		    }
		}, "cachewolf.Languages");
	    }
	}
	if (lr == null) {
	    initErrors += "Language file " + getLocaleFileName(resourcelanguage) + " couldn't be loaded - using hard coded messages";
	    lr = new LocalResource() {
		public Object get(int id, Object data) {
		    return data;
		}

		public Object get(String id, Object data) {
		    return data;
		}
	    };

	}

	inInit = false;
    }

    /**
     * Return a localized string
     * 
     * The localized strings are stored in the configuration file (relative to
     * executable:<br>
     * _config/cachewolf.Languages.cfg
     * If the configuration file does not exist or a string cannot be found in
     * the file, the defaultValue is returned.
     * 
     * @param resourceID
     *            The unique number of the resource
     * @param defaultValue
     *            The default value of the string (if not found in the configuration file)
     * @return The localized string
     */
    public static String getMsg(int resourceID, String defaultValue) {
	if (l == null)
	    init();
	if (lr != null) {
	    String res;
	    res = (String) lr.get(resourceID, defaultValue);
	    if (res != null)
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
	if (l == null)
	    init();
	return l.getString(Locale.LANGUAGE_SHORT, 0, 0);
    }

    /**
     * Get the three letter (uppercase) ISO country code
     * 
     * @return The three letter (uppercase) ISO country code
     */
    public static String getLocaleCountry() {
	if (l == null)
	    init();
	return l.getString(Locale.COUNTRY_SHORT, 0, 0);
    }

    /**
     * Formats a Long integer to a given format specifier
     * 
     * @param number
     *            A Long which contains the number to be formatted
     * @param fmt
     *            A string containing the format specification</br>
     *            '$' indicates that a currency symbol should be used. </br>
     *            ',' indicates that thousands groupings should be used. </br>
     *            '.' separates formatting before the decimal point and after the decimal point.</br>
     *            '0' before the decimal point indicates the number of digits before the decimal point.</br>
     * @return The formatted number
     */
    public static String formatLong(Long number, String fmt) {
	if (l == null)
	    init();
	return l.format(Locale.FORMAT_PARSE_NUMBER, number, fmt);
    }

    /**
     * Formats a long to a given format specifier
     * 
     * @param number
     *            A long containing the number to be formatted
     * @param fmt
     *            A string containing the format specification</br>
     * @return The formatted number
     */
    public static String formatLong(long number, String fmt) {
	Long L = new Long();
	L.set(number);
	return formatLong(L, fmt);
    }

    /**
     * Formats a Double to a given format specifier
     * 
     * @param number
     *            A Double containing the number to be formatted
     * @param fmt
     *            A string containing the format specification</br>
     * @return The formatted number
     */
    public static String formatDouble(ewe.sys.Double number, String fmt) {
	if (l == null)
	    init();
	return l.format(Locale.FORMAT_PARSE_NUMBER, number, fmt);
    }

    /**
     * Formats a Double to a given format specifier
     * 
     * @param number
     *            A double containing the number to be formatted
     * @param fmt
     *            A string containing the format specification</br>
     * @return The formatted number
     */
    public static String formatDouble(double number, String fmt) {
	Double d = new Double();
	d.set(number);
	return formatDouble(d, fmt);
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
     * 
     * @param split
     */
    public static void setSplitterSize(SplittablePanel split) {
	if (Vm.isMobile() && screenSize.height > 400) {
	    split.theSplitter.thickness = 20;
	}
    }

    /**
     * Returns the path to a localized version of a help file
     * 
     * @param basename
     * @return
     */
    public static String getLocalizedFile(String basename) {
	String lang = MyLocale.getLocaleLanguage();
	String programmDirectory = FileBase.getProgramDirectory() + "/";
	int index = basename.lastIndexOf('.');
	String prefix = basename.substring(0, index);
	String suffix = basename.substring(index);
	File f = new File(programmDirectory, prefix + '_' + lang + suffix);
	if (f.exists()) {
	    return f.toString();
	} else {
	    return new File(programmDirectory, basename).toString();
	}
    }

    //===================================================================
    static TreeConfigFile getConfigFile(String path)
    //===================================================================
    {
	try {
	    ewe.io.TreeConfigFile tcf = new TreeConfigFile();
	    tcf.configFileName = path;
	    TreeConfigNode root = (TreeConfigNode) tcf.getRoot();
	    FileReader r = null;
	    r = new FileReader(path);
	    r.codec = new ewe.io.JavaUtf8Codec();
	    r.read(); // should skip the UTF-8 BOM first 3 Bytes, but doesn't
	    TreeConfigNode tcn = root;
	    while (true) {
		String got = r.readLine();
		if (got == null)
		    break;
		got = got.trim();
		if (got.startsWith(";")) {
		    continue;
		}
		if (got.startsWith("#")) {
		    continue;
		}
		if (got.startsWith("{..}")) {
		    if (tcn == root)
			break;
		    else
			tcn = (TreeConfigNode) tcn.getParent();
		    continue;
		}
		if (got.startsWith("{")) {
		    int idx = got.indexOf('}');
		    if (idx == -1)
			idx = got.length();
		    TreeConfigNode nn = new TreeConfigNode(got.substring(1, idx));
		    tcn.addChild(nn);
		    tcn = nn;
		    continue;
		}
		int eq = got.indexOf(',');
		if (eq != -1) {
		    // split at first ,
		    String key = got.substring(0, eq).trim();
		    String value = got.substring(eq + 1).trim();
		    // remove surrounding "
		    value = value.substring(1, value.length() - 1);
		    // handle escaped " (\"), simplifying copy+paste to/from java/eclipse
		    value = STRreplace.replace(value, "\\\"", "\"");
		    // handle newLine (\n)
		    value = STRreplace.replace(value, "\\n", "\n");
		    tcn.getProperties().add(key, value);
		}
	    }
	    r.close();
	    return tcf;
	} catch (IOException e) {
	    return null;
	}
    }
}