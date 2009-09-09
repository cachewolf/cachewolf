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
				"&#255;", "�",
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
				"&#8226;", "�",
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
						appendEntityAsIsoChar(entity, isoBuffer); 
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

	protected static void appendEntityAsIsoChar(String entity, StringBuffer addto) {
		if (entity.startsWith("&#")) {
			try{
				if (entity.charAt(2)== 'x' || entity.charAt(2) == 'X') // number in hexadecimal // not tested because I don't have an XML containing hexadecimal encodings
					addto.append((char)Integer.parseInt(entity.substring(2, entity.length()-1), 16)); 
				else // number is decimal
					addto.append((char)Integer.parseInt(entity.substring(2, entity.length()-1)));
			} catch (NumberFormatException e) {
				addto.append(entity); // not a valid number, insert original text
			}

		} // number format exception
		else { // entity with a name like "&quot"						
			String isoCharacter = (String) iso2htmlMappings.get(entity);
			if (isoCharacter != null) {
				// insert iso character instead of html entity
				addto.append(isoCharacter);
			} else {
				// illegal entity detected, ignore gracefully
				addto.append(entity);
			}
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

	/**
	 * Converts a data string to something that is safe to use inside
	 * an XML file (like prefs.xml) - entities like &amp; are *NOT*
	 * valid XML unless declared specially, so we must use the numerical
	 * values here.
	 *
	 * @param src (String) raw text to be processed
	 *
	 * @return (String) translated text, or null if input is null
	 */
	public static String strxmlencode(boolean src) {
		/* bools are always safe */
		return (src ? "true" : "false");
	}
	public static String strxmlencode(int src) {
		/* numbers are always safe */
		return (Integer.toString(src));
	}
	public static String strxmlencode(String src) {
		int i, slen;
		char tmp[];
		StringBuffer dst;

		if (src == null)
			return (null);

		slen = src.length();
		dst = new StringBuffer(slen);
		tmp = new char[slen];
		src.getChars(0, slen, tmp, 0);
		for (i = 0; i < slen; ++i)
			if (tmp[i] == '&' || tmp[i] == '<' ||
			    tmp[i] == '>' || tmp[i] > 0x7E) {
				dst.append("&#");
				dst.append((int)tmp[i]);
				dst.append(';');
			} else
				dst.append(tmp[i]);
		return (dst.toString());
	}

	/**
	 * Converts a string that is safe to use inside an XML file (like
	 * prefs.xml) back to a data string - entities like &amp; are *NOT*
	 * valid XML unless declared specially, so we must use the numerical
	 * values here. We also try to decode non-standard entities.
	 *
	 * @param src (String) translated text to be processed
	 *
	 * @return (String) raw text, or null if input is null
	 */
	public static String strxmldecode(String src) {
		int i, j, slen;
		char ch, tmp[];
		StringBuffer dst;
		boolean isinval;

		if (src == null)
			return (null);

		slen = src.length();
		dst = new StringBuffer(slen);
		tmp = new char[slen];
		src.getChars(0, slen, tmp, 0);
		i = 0;
		while (i < slen)
			if (tmp[i] == '&') {
				/* first scan if we have a trailing ; */
				if (src.indexOf(';', i) == -1) {
					/* no - ignore and proceed */
					i++;
					dst.append(0xFFFD);
				} else if (tmp[++i] == '#') {
					/* yes - numerical value? */
					i++;
					ch = 0;
					isinval = false;
					if (tmp[i] == 'x' || tmp[i] == 'X') {
						/* hexadecimal numeric */
						i++;
						while ((j = tmp[i++]) != ';') {
							ch *= 16;
							if (j < 0x30)
								isinval = true;
							else if (j < 0x3A)
								ch += j - 0x30;
							else if (j < 0x41)
								isinval = true;
							else if (j < 0x47)
								ch += j - 0x37;
							else if (j < 0x61)
								isinval = true;
							else if (j < 0x67)
								ch += j - 0x57;
							else
								isinval = true;
						}
					} else
						/* decimal numeric */
						while ((j = tmp[i++]) != ';') {
							ch *= 10;
							if (j < 0x30)
								isinval = true;
							else if (j < 0x3A)
								ch += j - 0x30;
							else
								isinval = true;
						}
					if (isinval)
						ch = 0xFFFD;
					dst.append(ch);
				} else {
					/* yes - string value */
					StringBuffer tconv = new StringBuffer("&");
					String tc;

					do {
						tconv.append(tmp[i]);
					} while (tmp[i++] != ';');

					if ((tc = (String)iso2htmlMappings.get(tconv.toString())) == null)
						/* invalid entity, just retain it */
						dst.append(tconv);
					else
						dst.append(tc);
				}
			} else
				dst.append(tmp[i++]);
		return (dst.toString());
	}
}
