package exp;
import CacheWolf.*;
import ewe.util.Hashtable;

/**
*	Class to export the cache database into an geocaching .loc file that may be exported
*	by GPSBabel to a Garmin GPS.
*/
public class LocExporter extends Exporter{
	public static int MODE_AUTO = TMP_FILE;
	
	public LocExporter(){
		super();
		this.setMask("*.loc");
		this.setHowManyParams(NO_PARAMS);
	}
	
	public String header () {
		return "<?xml version=\"1.0\"?><loc version=\"1.0\" src=\"EasyGPS\">\r\n";
	}
	
	public String record(CacheHolder ch){
		StringBuffer strBuf = new StringBuffer(200);
		strBuf.append("<waypoint>\r\n   <name id=\"");
		strBuf.append(simplifyString(ch.wayPoint));
		strBuf.append("\"><![CDATA[");
		strBuf.append(simplifyString(ch.CacheName));
		strBuf.append("]]></name>\r\n   <coord lat=\"");
		strBuf.append(ch.pos.getLatDeg(CWPoint.DD));
		strBuf.append("\" lon=\"");
		strBuf.append(ch.pos.getLonDeg(CWPoint.DD));
		strBuf.append("\"/>\r\n   <type>");
		if (ch.is_found)
			strBuf.append("Geocache Found");
		else
			strBuf.append("Geocache");
		strBuf.append("</type>\r\n</waypoint>\r\n");
		return strBuf.toString();
	}
	public String trailer(){
		return "</loc>\r\n";
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
	
	
	
	
	
	
}
