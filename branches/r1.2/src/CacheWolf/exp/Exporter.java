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
package CacheWolf.exp;

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheType;
import CacheWolf.Global;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;

/**
 * @author Kalle
 * Base class for exporter, handles basic things like selecting
 * outputfile, display a counter etc.
 * A new Exporter must only override the header(), record() and 
 * trailer() methods. The member howManyParams must be set to identify
 * which ethod should be called  
 */

public class Exporter {
	// starts with no ui for file selection
	final static int TMP_FILE = 0;
	// brings up a screen to select a file
	final static int ASK_FILE = 1;
	
	// selection, which method should be called
	final static int NO_PARAMS 	= 0;
	final static int LAT_LON 	= 1;
	final static int COUNT 		= 2;
	
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	// mask in file chooser
	String mask = "*.*";
	// file name, if no file chooser is used
	String tmpFileName;
	// decimal separator for lat- and lon-String
	char decimalSeparator='.';
	// if  true, the complete cache details are read
	// before a call to the record method is made 
	boolean needCacheDetails = false;
	// selection, which method should be called
	int howManyParams = 0;
	
	//name of exporter for saving pathname
	String expName;
	
	public Exporter() {
		profile = Global.getProfile();
		pref = Global.getPref();
		cacheDB = profile.cacheDB;
		howManyParams = LAT_LON;
		expName = this.getClass().getName(); 
		// remove package
		expName = expName.substring(expName.indexOf(".") + 1);
	}
	
	public void doIt(){
		this.doIt(ASK_FILE);
	}

	/**
	 * Does the most work for exporting data
	 * @param variant 0, if no filechooser
	 *                1, if filechooser
	 */
	public void doIt(int variant){
		File outFile;
		String str;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();


		if (variant == ASK_FILE) {
			outFile = getOutputFile();
			if (outFile == null) return;
		} else {
			outFile = new File(tmpFileName);
		}

		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		int counter = cacheDB.countVisible();
		int expCount = 0;

		try{
			int incompleteWaypoints = 0;
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			str = this.header();
			if (str != null) outp.print(str);
			for(int i = 0; i<cacheDB.size(); i++){
				ch=cacheDB.get(i);
				if(ch.isVisible()){
					if (ch.is_incomplete()) {
						Global.getPref().log("skipping export of incomplete waypoint "+ch.getWayPoint());
						incompleteWaypoints++;
						continue;
					}
					expCount++;
					h.progress = (float)expCount/(float)counter;
					h.changed();
					switch (this.howManyParams) {
					case NO_PARAMS:
						str = record(ch);
						break;
					case LAT_LON:
						if (ch.pos.isValid() == false) continue;
						str = record(ch, ch.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
								     ch.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator));
						break;
					case LAT_LON|COUNT: 
						if (ch.pos.isValid() == false) continue;
						str = record(ch, ch.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
									 ch.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator),
											 i);
						break;
					default:
						str = null;
						break;
					}
					if (str != null) outp.print(str);
				}//if
			}//for
			switch (this.howManyParams & COUNT) {
			case NO_PARAMS: 
				str = trailer();
				break;
			case COUNT:
				str = trailer(counter);
				break;
			default:
				str = null;
				break;
			}
			if (str != null) outp.print(str);
			outp.close();
			pbf.exit(0);
			if (incompleteWaypoints > 0) {
				new MessageBox("Export Error", incompleteWaypoints+" incomplete waypoints have not been exported. See log for details.", FormBase.OKB).execute();
			}
		} catch (IOException ioE){
			pref.log("Error opening " + outFile.getName(),ioE);
		}
		//try
	}
	/**
	 * sets mask for filechooser
	 * @param mask 
	 */
	public void setMask(String mask){
		this.mask = mask;
	}
	/**
	 * sets decimal separator for lat/lon-string
	 * @param sep
	 */
	public void setDecimalSeparator (char sep){
		this.decimalSeparator = sep;
	}
	/**
	 *  sets needCacheDetails
	 * @param how
	 */
	public void setNeedCacheDetails(boolean how){
		this.needCacheDetails = how;
	}
	
	/**
	 * sets howManyParams
	 * @param paramBits
	 */
	public void setHowManyParams(int paramBits){
		this.howManyParams = paramBits;
	}
	/**
	 * sets tmpFileName
	 * @param fName
	 */
	public void setTmpFileName(String fName){
		this.tmpFileName = fName;
	}

	/**
	 * uses a filechooser to get the name of the export file
	 * @return
	 */
	public File getOutputFile (){
		File file;
		FileChooser fc = new FileChooser(FileChooserBase.SAVE, pref.getExportPath(expName));
		fc.setTitle("Select target file:");
		fc.addMask(mask);
		if(fc.execute() != FormBase.IDCANCEL){
			file = fc.getChosenFile();
			pref.setExportPath(expName, file.getPath());
			return file;
		} else {
			return null;
		}
	}
	/**
	 * this method can be overided by an exporter class
	 * @return formated header data
	 */	
	public String header () {
		return null;
	}

	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @return formated cache data
	 */	
	public String record(CacheHolder chD){
		return null;
	}

	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @param lat	
	 * @param lon
	 * @return formated cache data
	 */
	public String record(CacheHolder ch, String lat, String lon){
		return null;
	}
	/**
	 * this method can be overided by an exporter class
	 * @param ch	cachedata
	 * @param lat	
	 * @param lon
	 * @param count of actual record
	 * @return formated cache data
	 */
	public String record(CacheHolder ch, String lat, String lon, int count){
		return null;
	}
	
	/**
	 * this method can be overided by an exporter class
	 * @return formated trailer data
	 */	
	public String trailer(){
		return null;
	}
	/**
	 * this method can be overided by an exporter class
	 * @param total count of exported caches
	 * @return
	 */
	public String trailer(int total){
		return null;
	}

///////////////////////////////////////////////////
//  Helper functions for string sanitisation
///////////////////////////////////////////////////
	
	private static Hashtable iso2simpleMappings = new Hashtable(250);
	static {
		String[] mappingArray = new String[] {
				"34",  "'",
				"160", " ",
				"161", "i",
				"162", "c",
				"163", "$",
				"164", "o",
				"165", "$",
				"166", "!",
				"167", "$",
				"168", " ",
				"169", " ",
				"170", " ",
				"171", "<",
				"172", " ",
				"173", "-",
				"174", " ",
				"175", "-",
				"176", " ",
				"177", "+/-",
				"178", "2",
				"179", "3",
				"180", "'",
				"181", " ",
				"182", " ",
				"183", " ",
				"184", ",",
				"185", "1",
				"186", " ",
				"187", ">",
				"188", "1/4",
				"189", "1/2",
				"190", "3/4",
				"191", "?",
				"192", "A",
				"193", "A",
				"194", "A",
				"195", "A",
				"196", "Ae",
				"197", "A",
				"198", "AE",
				"199", "C",
				"200", "E",
				"201", "E",
				"202", "E",
				"203", "E",
				"204", "I",
				"205", "I",
				"206", "I",
				"207", "I",
				"208", "D",
				"209", "N",
				"210", "O",
				"211", "O",
				"212", "O",
				"213", "O",
				"214", "Oe",
				"215", "x",
				"216", "O",
				"217", "U",
				"218", "U",
				"219", "U",
				"220", "Ue",
				"221", "Y",
				"222", " ",
				"223", "ss",
				"224", "a",
				"225", "a",
				"226", "a",
				"227", "a",
				"228", "ae",
				"229", "a",
				"230", "ae",
				"231", "c",
				"232", "e",
				"233", "e",
				"234", "e",
				"235", "e",
				"236", "i",
				"237", "i",
				"238", "i",
				"239", "i",
				"240", "o",
				"241", "n",
				"242", "o",
				"243", "o",
				"244", "o",
				"245", "o",
				"246", "oe",
				"247", "/",
				"248", "o",
				"249", "u",
				"250", "u",
				"251", "u",
				"252", "ue",
				"253", "y",
				"254", "p",
				"255", "y"
		};
		for (int i = 0; i < mappingArray.length; i = i + 2) {
			iso2simpleMappings.put( Integer.valueOf( mappingArray[i]), mappingArray[i+1]);
		}
	}

	
	protected static String char2simpleChar( char c )
    {
        if ( c < 127 ) {
            // leave alone as equivalent string.
            return null;
        } else {
            String s=(String) iso2simpleMappings.get( new Integer(c));
            if (s==null) // not in table, replace with empty string just to be sure
            	return "";
            else
            	return s;
        }
    } // end charToEntity
	
    public static String simplifyString( String text ) {
        if ( text == null ) return null;
        int originalTextLength = text.length();
        StringBuffer sb = new StringBuffer( 50 );
        int charsToAppend = 0;
        for ( int i = 0; i < originalTextLength; i++ ) {
            char c = text.charAt( i );
            String entity = char2simpleChar( c );
            if ( entity == null ) {
                // we could sb.append( c ), but that would be slower
                // than saving them up for a big append.
                charsToAppend++;
            } else {
                if ( charsToAppend != 0 ) {
                    sb.append( text.substring( i - charsToAppend, i ) );
                    charsToAppend = 0;
                }
                sb.append( entity );
            }
        } // end for
        // append chars to the right of the last entity.
        if ( charsToAppend != 0 ) {
            sb.append( text.substring( originalTextLength - charsToAppend,
                                       originalTextLength ) );
        }
        // if result is not longer, we did not do anything. Save RAM.
        return ( sb.length() == originalTextLength ) ? text : sb.toString();
    } // end insertEntities

    public static String getShortDetails( CacheHolder ch ) {
    	StringBuffer strBuf = new StringBuffer(7);
    	strBuf.append(CacheType.getExportShortId(ch.getType()).toLowerCase());			
    	if (!ch.isAddiWpt()) {
   			strBuf.append(ch.getHard());
   			strBuf.append("/");
   			strBuf.append(ch.getTerrain());
    		strBuf.append(CacheSize.getExportShortId(ch.getCacheSize()));
    	}

    	return strBuf.toString();
    }

}
