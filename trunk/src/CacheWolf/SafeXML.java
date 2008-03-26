package CacheWolf;

/**
*	A class to replace unsafe XML characters with characters that a user
*	"can read", and vice versa
* 20061222: skg Modified cleanback to speed up the new index.xml reader
*/

import ewe.util.Hashtable;

public class SafeXML{
	private static final char ENTITY_START = '&';
	private static final char ENTITY_END = ';';
	
	private static Hashtable iso2htmlMappings = new Hashtable(300);
	static {
		String[] mappingArray = new String[] {
				"&apos;",   "'",		// Added 20061227 - not a valid HTML entity but sometimes used
				"&quot;",   "\"",
				"&amp;",    "&",
				"&lt;",     "<",
				"&gt;",     ">",
				"&nbsp;",   " ",
				"&iexcl;",  "¡",
				"&cent;",   "¢",
				"&pound;",  "£",
				"&curren;", "¤",
				"&yen;",    "¥",
				"&brvbar;", "¦",
				"&sect;",   "§",
				"&uml;",    "¨",
				"&copy;",   "©",
				"&ordf;",   "ª",
				"&laquo;",  "«",
				"&not;",    "¬",
				"&shy;",    "­",
				"&reg;",    "®",
				"&macr;",   "¯",
				"&deg;",    "°",
				"&plusmn;", "±",
				"&sup2;",   "²",
				"&sup3;",   "³",
				"&acute;",  "´",
				"&micro;",  "µ",
				"&para;",   "¶",
				"&middot;", "·",
				"&cedil;",  "¸",
				"&sup1;",   "¹",
				"&ordm;",   "º",
				"&raquo;",  "»",
				"&frac14;", "¼",
				"&frac12;", "½",
				"&frac34;", "¾",
				"&iquest;", "¿",
				"&Agrave;", "À",
				"&Aacute;", "Á",
				"&Acirc;",  "Â",
				"&Atilde;", "Ã",
				"&Auml;",   "Ä",
				"&Aring;",  "Å",
				"&AElig;",  "Æ",
				"&Ccedil;", "Ç",
				"&Egrave;", "È",
				"&Eacute;", "É",
				"&Ecirc;",  "Ê",
				"&Euml;",   "Ë",
				"&Igrave;", "Ì",
				"&Iacute;", "Í",
				"&Icirc;",  "Î",
				"&Iuml;",   "Ï",
				"&ETH;",    "Ð",
				"&Ntilde;", "Ñ",
				"&Ograve;", "Ò",
				"&Oacute;", "Ó",
				"&Ocirc;",  "Ô",
				"&Otilde;", "Õ",
				"&Ouml;",   "Ö",
				"&times;",  "×",
				"&Oslash;", "Ø",
				"&Ugrave;", "Ù",
				"&Uacute;", "Ú",
				"&Ucirc;",  "Û",
				"&Uuml;",   "Ü",
				"&Yacute;", "Ý",
				"&THORN;",  "Þ",
				"&szlig;",  "ß",
				"&agrave;", "à",
				"&aacute;", "á",
				"&acirc;",  "â",
				"&atilde;", "ã",
				"&auml;",   "ä",
				"&aring;",  "å",
				"&aelig;",  "æ",
				"&ccedil;", "ç",
				"&egrave;", "è",
				"&eacute;", "é",
				"&ecirc;",  "ê",
				"&euml;",   "ë",
				"&igrave;", "ì",
				"&iacute;", "í",
				"&icirc;",  "î",
				"&iuml;",   "ï",
				"&eth;",    "ð",
				"&ntilde;", "ñ",
				"&ograve;", "ò",
				"&oacute;", "ó",
				"&ocirc;",  "ô",
				"&otilde;", "õ",
				"&ouml;",   "ö",
				"&divide;", "÷",
				"&oslash;", "ø",
				"&ugrave;", "ù",
				"&uacute;", "ú",
				"&ucirc;",  "û",
				"&uuml;",   "ü",
				"&yacute;", "ý",
				"&thorn;",  "þ",
				"&yuml;",   "ÿ",
				
				"&#34;",  "\"",
				"&#38;",  "&",
				"&#60;",  "<",
				"&#62;",  ">",
				"&#160;", " ",
				"&#161;", "¡",
				"&#162;", "¢",
				"&#163;", "£",
				"&#164;", "¤",
				"&#165;", "¥",
				"&#166;", "¦",
				"&#167;", "§",
				"&#168;", "¨",
				"&#169;", "©",
				"&#170;", "ª",
				"&#171;", "«",
				"&#172;", "¬",
				"&#173;", "­",
				"&#174;", "®",
				"&#175;", "¯",
				"&#176;", "°",
				"&#177;", "±",
				"&#178;", "²",
				"&#179;", "³",
				"&#180;", "´",
				"&#181;", "µ",
				"&#182;", "¶",
				"&#183;", "·",
				"&#184;", "¸",
				"&#185;", "¹",
				"&#186;", "º",
				"&#187;", "»",
				"&#188;", "¼",
				"&#189;", "½",
				"&#190;", "¾",
				"&#191;", "¿",
				"&#192;", "À",
				"&#193;", "Á",
				"&#194;", "Â",
				"&#195;", "Ã",
				"&#196;", "Ä",
				"&#197;", "Å",
				"&#198;", "Æ",
				"&#199;", "Ç",
				"&#200;", "È",
				"&#201;", "É",
				"&#202;", "Ê",
				"&#203;", "Ë",
				"&#204;", "Ì",
				"&#205;", "Í",
				"&#206;", "Î",
				"&#207;", "Ï",
				"&#208;", "Ð",
				"&#209;", "Ñ",
				"&#210;", "Ò",
				"&#211;", "Ó",
				"&#212;", "Ô",
				"&#213;", "Õ",
				"&#214;", "Ö",
				"&#215;", "×",
				"&#216;", "Ø",
				"&#217;", "Ù",
				"&#218;", "Ú",
				"&#219;", "Û",
				"&#220;", "Ü",
				"&#221;", "Ý",
				"&#222;", "Þ",
				"&#223;", "ß",
				"&#224;", "à",
				"&#225;", "á",
				"&#226;", "â",
				"&#227;", "ã",
				"&#228;", "ä",
				"&#229;", "å",
				"&#230;", "æ",
				"&#231;", "ç",
				"&#232;", "è",
				"&#233;", "é",
				"&#234;", "ê",
				"&#235;", "ë",
				"&#236;", "ì",
				"&#237;", "í",
				"&#238;", "î",
				"&#239;", "ï",
				"&#240;", "ð",
				"&#241;", "ñ",
				"&#242;", "ò",
				"&#243;", "ó",
				"&#244;", "ô",
				"&#245;", "õ",
				"&#246;", "ö",
				"&#247;", "÷",
				"&#248;", "ø",
				"&#249;", "ù",
				"&#250;", "ú",
				"&#251;", "û",
				"&#252;", "ü",
				"&#253;", "ý",
				"&#254;", "þ",
				"&#255;", "ÿ",
				"&#8208;", "-",
				"&#8209;", "-",
				"&#8210;", "-",
				"&#8211;", "-",
				"&#8212;", "-",
				"&#8213;", "-",
				"&#8216;", "'",
				"&#8217;", "'",
				"&#8218;", "'",
				"&#8219;", "'",
				"&#8220;", "\"",
				"&#8221;", "\"",
				"&#8222;", "\"",
				"&#8223;", "\"",
				"&#8242;", "'",
				"&#8243;", "\""
				};
		for (int i = 0; i < mappingArray.length; i = i + 2) {
			iso2htmlMappings.put( mappingArray[i], mappingArray[i+1]);
		}
	}
		
	
	
	/**
	 * Converts a <code>String</code> containing HTML entities to
	 * a <code>String</code> containing only ISO8859-1 characters.
	 * 
	 * Uses <a href="http://www.ramsch.org/martin/uni/fmi-hp/iso8859-1.html">ISO
	 * 8859-1 table by Martin Ramsch</a>.
	 * 
	 * @author <a href="mailto:ey@inweb.de">Christian Ey</a>
	 *
	 * @version 1.0
	 * @param htmlString The <code>String</code> containing HTML
	 * 	entities
	 * @return A <code>String</code> containing only ISO8859-1
	 * 	characters
	 */
	public static String cleanback( String htmlString) {
		int indexStart;
		// return immediately if string is null or does not contain &
		if (htmlString != null && (indexStart = htmlString.indexOf( ENTITY_START))>=0) {
			// copy everything from the beginning to entity start into buffer
			StringBuffer isoBuffer = new StringBuffer( htmlString.substring( 0, indexStart));
			while (indexStart >= 0) {
				int indexEnd = htmlString.indexOf( ENTITY_END, indexStart + 1);
				if (indexEnd >= 0) {
					int alternativeStart = htmlString.indexOf( ENTITY_START, indexStart + 1);
					if ((alternativeStart > indexStart) && (alternativeStart < indexEnd)) {
						// a second index start is found inbetween current index start
						// and index end
						
						// flush the html string inbetween
						isoBuffer.append( htmlString.substring( indexStart, alternativeStart));
						
						// use the second index start and loop again
						indexStart = alternativeStart;
					} else {
						String entity = htmlString.substring( indexStart, indexEnd + 1);
						String isoCharacter = (String) iso2htmlMappings.get( entity);
						if (isoCharacter != null) {
							// insert iso character instead of html entity
							isoBuffer.append( isoCharacter);
						} else {
							// illegal entity detected, ignore gracefully
							isoBuffer.append( entity);
						}
						indexStart = htmlString.indexOf( ENTITY_START, indexEnd + 1);
						if (indexStart >= 0) {
							// another entity start detected, flush the html string inbetween
							isoBuffer.append( htmlString.substring( indexEnd + 1, indexStart));
						} else {
							// no further entity start detected, flush rest of html string
							isoBuffer.append( htmlString.substring( indexEnd + 1));
						}
					}
				} else {
					// entity start without matching entity end detected, ignore gracefully
					isoBuffer.append( htmlString.substring( indexStart));
					break;
				}
			}
			return isoBuffer.toString();
		} else {
			// nothing to do
			return htmlString;
		}
	}

	/**
     * convert a single char to its equivalent HTML entity. Ordinary chars are
     * not changed. 160 -> &nbsp;
     *
     * @param c Char to convert
     *
     * @return equivalent string eg. &amp;, null means leave char as is.
     */
    protected static String charToEntity( char c )
        {
        switch ( c ) {
	        case 34 : return "&quot;";
	        case 38 : return "&amp;";
	        case 60 : return "&lt;";
	        case 62 : return "&gt;";
            default :
                if ( c < 127 ) {
                    // leave alone as equivalent string.
                    return null;
                    // faster than String.valueOf( c ).intern();
                } else {
                    // use the &#nnn; form
                    return "&#" + Integer.toString( c ) + ";";
                }
            } // end switch
        } // end charToEntity

    /**
     * Converts text to HTML by quoting dangerous characters. Text must not
     * already contain entities. e.g. " ==> &quot; < ==> &lt; ordinary text
     * passes unchanged. Does not convert space to &nbsp;
     *
     * @param text raw text to be processed. Must not be null.
     *
     * @return translated text, or null if input is null.
     */
    public static String clean( String text ) {
        if ( text == null ) return null;
        int originalTextLength = text.length();
        StringBuffer sb = new StringBuffer( originalTextLength * 110 / 100 );
        int charsToAppend = 0;
        for ( int i = 0; i < originalTextLength; i++ ) {
            char c = text.charAt( i );
            String entity = charToEntity( c );
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
	
	
	public static String cleanGPX(String str){
		String dummy = new String();
		
		dummy = STRreplace.replace(str, "&","&amp;");
		dummy = STRreplace.replace(dummy, "<", "&lt;");
		dummy = STRreplace.replace(dummy, ">", "&gt;");
		//dummy = replace(dummy, "&nbsp;", "&amp;nbsp;");
		dummy = STRreplace.replace(dummy, "\"", "&quot;");
		dummy = STRreplace.replace(dummy, "'","&apos;");
		dummy = STRreplace.replace(dummy, "]]>","]] >");

		return dummy;
	}

}
