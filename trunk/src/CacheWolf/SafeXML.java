package CacheWolf;

/**
*	A class to replace unsafe XML characters with characters that a user
*	"can read", and vice versa
* 20061222: skg Modified cleanback to speed up the new index.xml reader
*/

import ewe.util.Hashtable;
import ewe.sys.*;

public class SafeXML{
	private static final char ENTITY_START = '&';
	private static final char ENTITY_END = ';';
	
	private static Hashtable iso2htmlMappings = new Hashtable(250);
	static {
		String[] mappingArray = new String[] {
				"&apos;",   "'",		// Added 20061227 - not a valid HTML entity but sometimes used
				"&quot;",   "\"",
				"&amp;",    "&",
				"&lt;",     "<",
				"&gt;",     ">",
				"&nbsp;",   " ",
				"&iexcl;",  "�",
				"&cent;",   "�",
				"&pound;",  "�",
				"&curren;", "�",
				"&yen;",    "�",
				"&brvbar;", "�",
				"&sect;",   "�",
				"&uml;",    "�",
				"&copy;",   "�",
				"&ordf;",   "�",
				"&laquo;",  "�",
				"&not;",    "�",
				"&shy;",    "�",
				"&reg;",    "�",
				"&macr;",   "�",
				"&deg;",    "�",
				"&plusmn;", "�",
				"&sup2;",   "�",
				"&sup3;",   "�",
				"&acute;",  "�",
				"&micro;",  "�",
				"&para;",   "�",
				"&middot;", "�",
				"&cedil;",  "�",
				"&sup1;",   "�",
				"&ordm;",   "�",
				"&raquo;",  "�",
				"&frac14;", "�",
				"&frac12;", "�",
				"&frac34;", "�",
				"&iquest;", "�",
				"&Agrave;", "�",
				"&Aacute;", "�",
				"&Acirc;",  "�",
				"&Atilde;", "�",
				"&Auml;",   "�",
				"&Aring;",  "�",
				"&AElig;",  "�",
				"&Ccedil;", "�",
				"&Egrave;", "�",
				"&Eacute;", "�",
				"&Ecirc;",  "�",
				"&Euml;",   "�",
				"&Igrave;", "�",
				"&Iacute;", "�",
				"&Icirc;",  "�",
				"&Iuml;",   "�",
				"&ETH;",    "�",
				"&Ntilde;", "�",
				"&Ograve;", "�",
				"&Oacute;", "�",
				"&Ocirc;",  "�",
				"&Otilde;", "�",
				"&Ouml;",   "�",
				"&times;",  "�",
				"&Oslash;", "�",
				"&Ugrave;", "�",
				"&Uacute;", "�",
				"&Ucirc;",  "�",
				"&Uuml;",   "�",
				"&Yacute;", "�",
				"&THORN;",  "�",
				"&szlig;",  "�",
				"&agrave;", "�",
				"&aacute;", "�",
				"&acirc;",  "�",
				"&atilde;", "�",
				"&auml;",   "�",
				"&aring;",  "�",
				"&aelig;",  "�",
				"&ccedil;", "�",
				"&egrave;", "�",
				"&eacute;", "�",
				"&ecirc;",  "�",
				"&euml;",   "�",
				"&igrave;", "�",
				"&iacute;", "�",
				"&icirc;",  "�",
				"&iuml;",   "�",
				"&eth;",    "�",
				"&ntilde;", "�",
				"&ograve;", "�",
				"&oacute;", "�",
				"&ocirc;",  "�",
				"&otilde;", "�",
				"&ouml;",   "�",
				"&divide;", "�",
				"&oslash;", "�",
				"&ugrave;", "�",
				"&uacute;", "�",
				"&ucirc;",  "�",
				"&uuml;",   "�",
				"&yacute;", "�",
				"&thorn;",  "�",
				"&yuml;",   "�",
				
				"&#34;",  "\"",
				"&#38;",  "&",
				"&#60;",  "<",
				"&#62;",  ">",
				"&#160;", " ",
				"&#161;", "�",
				"&#162;", "�",
				"&#163;", "�",
				"&#164;", "�",
				"&#165;", "�",
				"&#166;", "�",
				"&#167;", "�",
				"&#168;", "�",
				"&#169;", "�",
				"&#170;", "�",
				"&#171;", "�",
				"&#172;", "�",
				"&#173;", "�",
				"&#174;", "�",
				"&#175;", "�",
				"&#176;", "�",
				"&#177;", "�",
				"&#178;", "�",
				"&#179;", "�",
				"&#180;", "�",
				"&#181;", "�",
				"&#182;", "�",
				"&#183;", "�",
				"&#184;", "�",
				"&#185;", "�",
				"&#186;", "�",
				"&#187;", "�",
				"&#188;", "�",
				"&#189;", "�",
				"&#190;", "�",
				"&#191;", "�",
				"&#192;", "�",
				"&#193;", "�",
				"&#194;", "�",
				"&#195;", "�",
				"&#196;", "�",
				"&#197;", "�",
				"&#198;", "�",
				"&#199;", "�",
				"&#200;", "�",
				"&#201;", "�",
				"&#202;", "�",
				"&#203;", "�",
				"&#204;", "�",
				"&#205;", "�",
				"&#206;", "�",
				"&#207;", "�",
				"&#208;", "�",
				"&#209;", "�",
				"&#210;", "�",
				"&#211;", "�",
				"&#212;", "�",
				"&#213;", "�",
				"&#214;", "�",
				"&#215;", "�",
				"&#216;", "�",
				"&#217;", "�",
				"&#218;", "�",
				"&#219;", "�",
				"&#220;", "�",
				"&#221;", "�",
				"&#222;", "�",
				"&#223;", "�",
				"&#224;", "�",
				"&#225;", "�",
				"&#226;", "�",
				"&#227;", "�",
				"&#228;", "�",
				"&#229;", "�",
				"&#230;", "�",
				"&#231;", "�",
				"&#232;", "�",
				"&#233;", "�",
				"&#234;", "�",
				"&#235;", "�",
				"&#236;", "�",
				"&#237;", "�",
				"&#238;", "�",
				"&#239;", "�",
				"&#240;", "�",
				"&#241;", "�",
				"&#242;", "�",
				"&#243;", "�",
				"&#244;", "�",
				"&#245;", "�",
				"&#246;", "�",
				"&#247;", "�",
				"&#248;", "�",
				"&#249;", "�",
				"&#250;", "�",
				"&#251;", "�",
				"&#252;", "�",
				"&#253;", "�",
				"&#254;", "�",
				"&#255;", "�"
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
	
	
	/**
	*	This method encodes special characters into
	*	HTML coded characters.
	*	see here: http://www.w3.org/MarkUp/html-spec/html-spec_13.html
	*/
	/*public static String clean(String str){
		String dummy = new String();
		
		dummy = replace(str,  "&", "&#38;"); // Must be first otherwise the & of the replaced strings will be replaced again
		dummy = replace(dummy, "<", "&#60;");
		dummy = replace(dummy, ">", "&#62;");
		dummy = replace(dummy,  "\"" ,"&#34;");
		dummy = replace(dummy,  "�","&#176;");
		dummy = replace(dummy, "'","&apos;");
//		dummy = replace(dummy, "'", "&#180;");
		dummy = replace(dummy, "�","&#252;");
		dummy = replace(dummy, "�","&#228;");
		dummy = replace(dummy, "�","&#246;");
		dummy = replace(dummy, "�","&#196;");
		dummy = replace(dummy,  "�","&#214;");
		dummy = replace(dummy,  "�","&#220;");
		dummy = replace(dummy,  "�","&#223;");
		//dummy = replace(dummy, "&","&amp;");
		return dummy;
	}
*/
	public static String cleanGPX(String str){
		String dummy = new String();
		
		dummy = replace(str, "&","&amp;");
		dummy = replace(dummy, "<", "&lt;");
		dummy = replace(dummy, ">", "&gt;");
		//dummy = replace(dummy, "&nbsp;", "&amp;nbsp;");
		dummy = replace(dummy, "\"", "&quot;");
		dummy = replace(dummy, "'","&apos;");
		dummy = replace(dummy, "]]>","]] >");

		return dummy;
	}

	
	/**
	*	This method cleans html coded characters into human
	*	readable characters.
	*/
	/*public static String cleanback(String str){
		String dummy = new String();
		if (str.indexOf('&')<0) return str; // If nothing to replace, return immediately
		dummy = replace(str,  "&#223;", "�"); // Start with the mor probable values
		dummy = replace(dummy, "&#252;","�"); 
		dummy = replace(dummy, "&#228;","�");
		dummy = replace(dummy, "&#246;","�");
		dummy = replace(dummy, "&#196;","�");
		dummy = replace(dummy, "&#214;","�");
		dummy = replace(dummy, "&#220;","�");
		dummy = replace(dummy, "&apos;", "'");
		dummy = replace(dummy, "&#180;", "'");
		if (dummy.indexOf("&")<0) return dummy; 
		dummy = replace(dummy, "&amp;", "&");
		dummy = replace(dummy,  "&#38;", "&");
		dummy = replace(dummy,  "&#34;", "\"");
		dummy = replace(dummy,  "&#60;","<");
		dummy = replace(dummy,  "&#62;",">");
		dummy = replace(dummy,  "&#176;","�");
		dummy = replace(dummy, "&quot;", "\"");	
		dummy = replace(dummy, "&lt;", "<");
		dummy = replace(dummy, "&gt;", ">");
	
		return dummy;
	}*/
	
	/* Replace all instances of a String in a String.
		 *   @param  s  String to alter.
		 *   @param  f  String to look for.
		 *   @param  r  String to replace it with, or null to just remove it.
		 */ 
		public static String replace( String s, String f, String r )
		{
		   if (s == null)  return s;
		   if (f == null)  return s;
		   if (r == null)  r = "";
		
		   int index01 = s.indexOf( f );
		   while (index01 != -1)
		   {
			  s = s.substring(0,index01) + r + s.substring(index01+f.length());
			  index01 += r.length();
			  index01 = s.indexOf( f, index01 );
		   }
		   return s;
		}
}
