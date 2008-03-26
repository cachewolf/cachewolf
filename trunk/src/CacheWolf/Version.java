
package CacheWolf;

import com.stevesoft.ewe_pat.Regex;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.*;

/**
 * Class to hold and handle version information of the cachewolf project
 * language localisation string at at number 7000 
 */

public class Version {
	static final int VER_MAJOR = 0;
	static final int VER_MINOR = 9;
	static final String VER_SVN ="$LastChangedRevision$"; // the number is automatically replaced by subversion to the latest versionnumer of this file (svn:keywords LastChangedRevision)
	static final int SVN_REVISION = Common.parseInt(VER_SVN.substring(VER_SVN.indexOf(" ")+1, VER_SVN.lastIndexOf(" ")));
	static final int VERSION_TYPE = 3;
	public static final String[] VERSION_TYPES = {
		"Release", 
		"ReleaseCandidate",
		"InDevelopmentStable",
		"InDevelopmentNewest"
	};
	
	/** only valid after calling checkForUpdates() */
	static int[] updateavailabe = {0,0,0,0,0}; 

	public static String getRelease() {
		return Convert.toString(VER_MAJOR) + "." + Convert.toString(VER_MINOR);
	}

	public static String getReleaseDetailed() {
		// habe die SVN-Nummer doch aus der Anzeige erstmal wieder herausgenommen, weil es in einem final Release doch recht seltsam aussähe.
		// Sinnvoll wäre daher vielleicht, eine Methode getReleaseDatail, die die SVN-Versionnummer mit angibt und z.B. im "über"-Dialog angezeigt werden könnte.
		return getRelease() + "." + Convert.toString(SVN_REVISION) + " " + VERSION_TYPES[VERSION_TYPE];
	}

	/**
	 * Checks if newer versions of cachewolf are available 
	 * @return [0] = recommended version type, [1]...[4]: 0: no update available, 1: newer version available, 2: version doesn't exists, 3: error 
	 * @throws IOException
	 */
	public static void checkForUpdates() throws IOException {
		Properties curvers = UrlFetcher.fetchPropertyList("http://svn.berlios.de/svnroot/repos/cachewolf/trunk/currentversions.txt");
		for (int i = updateavailabe.length-1; i >=1; i--) {
			updateavailabe[i] = (checkVersion(curvers, "T"+(i-1))); 
		}
		updateavailabe[0] = Convert.toInt(curvers.getProperty("RecommendedType", "0"));
	}

	/**
	 * you must call checkForUpdates() before this method
	 * @return
	 */
	public static String newVersionsArrayToString() {
		StringBuffer ret = new StringBuffer(500);
		for (int i=1; i <= updateavailabe.length -1; i++) {
			if (updateavailabe[i] != 2 || i-1 == VERSION_TYPE) {
				ret.append(MyLocale.getMsg(7000+i-1, VERSION_TYPES[i-1]));
				if (i == updateavailabe[0]) ret.append("*");
				if (i-1 == VERSION_TYPE ) ret.append("+");
				ret.append(": ");
				ret.append(MyLocale.getMsg(7010 + updateavailabe[i], Convert.toString(updateavailabe[i])));
				ret.append("\n");
			}
		}
		ret.append("* = ").append(MyLocale.getMsg(7020, "Recommended version type"));
		ret.append("\n+ = ").append(MyLocale.getMsg(7021, "This version type"));
		return ret.toString();
	}

	public static String getUpdateMessage() {
		try {
			checkForUpdates();
			return MyLocale.getMsg(7022, "Version type") +"\n"+ newVersionsArrayToString();
		} catch (IOException e) {
			return MyLocale.getMsg(7023, "Error getting current version information") +"\n" + e.getMessage();
		}
	}

	/**
	 * @param url
	 * @return: 1 = newer Version available, 0 = this is up to date, 3 = check failed
	 */

	private static int checkVersion(Properties curvers, String prefix) {
		try {
			int curv = Convert.toInt(curvers.getProperty(prefix + "VersionMajor", "0")); 
			if (curv > VER_MAJOR) return 1;
			if (curv < VER_MAJOR) return 0;
			curv = Convert.toInt(curvers.getProperty(prefix + "VersionMinor", "0"));
			if (curv > VER_MINOR) return 1;
			if (curv < VER_MINOR) return 0;
			String svnRString = curvers.getProperty(prefix + "SvnRevision","0");
			if (svnRString.startsWith("http")) {
				String tmp = UrlFetcher.fetchString(svnRString);
				Regex s = new Regex("(?i)Revision[\\s]*[:=][\\s]*[\\\\r]*[\\\\n]*[\\s]*([0-9]*)");
				s.search(tmp);
				if (!s.didMatch()) return 3;
				svnRString = s.stringMatched(1); 
			}
			if (Convert.toInt(svnRString) > SVN_REVISION) return 1;
			return 0;
		} catch (IOException e) {
			return 3;
		}
	}

}
