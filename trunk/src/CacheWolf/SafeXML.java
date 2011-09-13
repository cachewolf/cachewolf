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
package CacheWolf;

/**
 *	A class to replace unsafe XML characters with characters that a user
 *	"can read", and vice versa
 * 20061222: skg Modified cleanback to speed up the new index.xml reader
 */

import ewe.util.Hashtable;

public final class SafeXML {
	private static final char ENTITY_START = '&';
	private static final char ENTITY_END = ';';

	private final static Hashtable iso2htmlMappings = new Hashtable(300);
	static {
		final String[] mappingArray = new String[] {
				"&apos;",
				"'", // Added 20061227 - not a valid HTML entity but used in XML
				"&quot;", "\"", "&amp;", "&", "&lt;", "<", "&gt;", ">", "&nbsp;", " ", "&iexcl;", "¡", "&cent;", "¢", "&pound;", "£", "&curren;", "¤", "&yen;", "¥", "&brvbar;", "¦", "&sect;", "§", "&uml;", "¨", "&copy;", "©", "&ordf;", "ª", "&laquo;",
				"«", "&not;", "¬", "&shy;", "­", "&reg;", "®", "&macr;", "¯", "&deg;", "°", "&plusmn;", "±", "&sup2;", "²", "&sup3;", "³", "&acute;", "´", "&micro;", "µ", "&para;", "¶", "&middot;", "·", "&cedil;", "¸", "&sup1;", "¹", "&ordm;", "º",
				"&raquo;", "»", "&frac14;", "¼", "&frac12;", "½", "&frac34;", "¾", "&iquest;", "¿", "&Agrave;", "À", "&Aacute;", "Á", "&Acirc;", "Â", "&Atilde;", "Ã", "&Auml;", "Ä", "&Aring;", "Å", "&AElig;", "Æ", "&Ccedil;", "Ç", "&Egrave;", "È",
				"&Eacute;", "É", "&Ecirc;", "Ê", "&Euml;", "Ë", "&Igrave;", "Ì", "&Iacute;", "Í", "&Icirc;", "Î", "&Iuml;", "Ï", "&ETH;", "Ð", "&Ntilde;", "Ñ", "&Ograve;", "Ò", "&Oacute;", "Ó", "&Ocirc;", "Ô", "&Otilde;", "Õ", "&Ouml;", "Ö", "&times;",
				"×", "&Oslash;", "Ø", "&Ugrave;", "Ù", "&Uacute;", "Ú", "&Ucirc;", "Û", "&Uuml;", "Ü", "&Yacute;", "Ý", "&THORN;", "Þ", "&szlig;", "ß", "&agrave;", "à", "&aacute;", "á", "&acirc;", "â", "&atilde;", "ã", "&auml;", "ä", "&aring;", "å",
				"&aelig;", "æ", "&ccedil;", "ç", "&egrave;", "è", "&eacute;", "é", "&ecirc;", "ê", "&euml;", "ë", "&igrave;", "ì", "&iacute;", "í", "&icirc;", "î", "&iuml;", "ï", "&eth;", "ð", "&ntilde;", "ñ", "&ograve;", "ò", "&oacute;", "ó",
				"&ocirc;", "ô", "&otilde;", "õ", "&ouml;", "ö", "&divide;", "÷", "&oslash;", "ø", "&ugrave;", "ù", "&uacute;", "ú", "&ucirc;", "û", "&uuml;", "ü", "&yacute;", "ý", "&thorn;", "þ", "&yuml;", "ÿ", "&ndash;", "–" };
		for (int i = 0; i < mappingArray.length; i = i + 2) {
			iso2htmlMappings.put(mappingArray[i], mappingArray[i + 1]);
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
	 * @param htmlString
	 *            The <code>String</code> containing HTML
	 *            entities
	 * @return A <code>String</code> containing only ISO8859-1
	 *         characters
	 */
	public final static String cleanback(String htmlString) {
		int indexStart;
		// return immediately if string is null or does not contain &
		if (htmlString != null && (indexStart = htmlString.indexOf(ENTITY_START)) >= 0) {
			// copy everything from the beginning to entity start into buffer
			StringBuffer isoBuffer = new StringBuffer(htmlString.substring(0, indexStart));
			while (indexStart >= 0) {
				int indexEnd = htmlString.indexOf(ENTITY_END, indexStart + 1);
				if (indexEnd >= 0) {
					int alternativeStart = htmlString.indexOf(ENTITY_START, indexStart + 1);
					if ((alternativeStart > indexStart) && (alternativeStart < indexEnd)) {
						// a second index start is found inbetween current index start
						// and index end

						// flush the html string inbetween
						isoBuffer.append(htmlString.substring(indexStart, alternativeStart));

						// use the second index start and loop again
						indexStart = alternativeStart;
					} else {
						String entity = htmlString.substring(indexStart, indexEnd + 1);
						appendEntityAsIsoChar(entity, isoBuffer);
						indexStart = htmlString.indexOf(ENTITY_START, indexEnd + 1);
						if (indexStart >= 0) {
							// another entity start detected, flush the html string inbetween
							isoBuffer.append(htmlString.substring(indexEnd + 1, indexStart));
						} else {
							// no further entity start detected, flush rest of html string
							isoBuffer.append(htmlString.substring(indexEnd + 1));
						}
					}
				} else {
					// entity start without matching entity end detected, ignore gracefully
					isoBuffer.append(htmlString.substring(indexStart));
					break;
				}
			}
			return isoBuffer.toString();
		} else {
			// nothing to do
			return htmlString;
		}
	}

	private final static void appendEntityAsIsoChar(String entity, StringBuffer addto) {
		if (entity.startsWith("&#")) {
			try {
				if (entity.charAt(2) == 'x' || entity.charAt(2) == 'X') // number in hexadecimal // not tested because I don't have an XML containing hexadecimal encodings
					addto.append((char) Integer.parseInt(entity.substring(3, entity.length() - 1), 16));
				else
					// number is decimal
					addto.append((char) Integer.parseInt(entity.substring(2, entity.length() - 1)));
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
	 * @param c
	 *            Char to convert
	 * 
	 * @return equivalent string eg. &amp;, null means leave char as is.
	 */
	private final static String charToEntity(char c) {
		switch (c) {
		case '"':
			return "&quot;";
		case '&':
			return "&amp;";
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '\'':
			return "&apos;";
		default:
			if (c < 127) {
				// leave alone as equivalent string.
				return null;
				// faster than String.valueOf( c ).intern();
			} else {
				// use the &#nnn; form
				return "&#" + Integer.toString(c) + ";";
			}
		} // end switch
	} // end charToEntity

	/**
	 * Converts text to HTML by quoting dangerous characters. Text must not
	 * already contain entities. e.g. " ==> &quot; < ==> &lt; ordinary text
	 * passes unchanged. Does not convert space to &nbsp;
	 * 
	 * @param text
	 *            raw text to be processed. Must not be null.
	 * 
	 * @return translated text, or null if input is null.
	 */
	public final static String clean(String text) {
		if (text == null)
			return null;
		int originalTextLength = text.length();
		StringBuffer sb = new StringBuffer(originalTextLength * 110 / 100);
		int charsToAppend = 0;
		for (int i = 0; i < originalTextLength; i++) {
			char c = text.charAt(i);
			String entity = charToEntity(c);
			if (entity == null) {
				// we could sb.append( c ), but that would be slower
				// than saving them up for a big append.
				charsToAppend++;
			} else {
				if (charsToAppend != 0) {
					sb.append(text.substring(i - charsToAppend, i));
					charsToAppend = 0;
				}
				sb.append(entity);
			}
		} // end for
			// append chars to the right of the last entity.
		if (charsToAppend != 0) {
			sb.append(text.substring(originalTextLength - charsToAppend, originalTextLength));
		}

		// if result is not longer, we did not do anything. Save RAM.
		return (sb.length() == originalTextLength) ? text : sb.toString();
	} // end insertEntities

	/**
	 * Converts a data string to something that is safe to use inside
	 * an XML file (like prefs.xml) - entities like &amp; are *NOT*
	 * valid XML unless declared specially, so we must use the numerical
	 * values here.
	 * 
	 * @param src
	 *            (String) raw text to be processed
	 * 
	 * @return (String) translated text, or null if input is null
	 */
	public final static String cleanGPX(String str) {
		String dummy = STRreplace.replace(str, "&", "&amp;");
		// "&amp;#" --> "&#"); //Darstellung Umlaute etc : siehe http://www.geoclub.de/viewtopic.php?f=40&t=50635&p=798796#p798796
		// aber so etwas nicht "&amp;#entry15063" --> !!not!! "&#entry15063" (Cache GCPB5P export -> gpx, import -> mapsource)
		int pos = 0;
		while (pos > -1) {
			pos = dummy.indexOf("&amp;#", pos);
			int pos1 = dummy.indexOf(";", pos + 6);
			int k = pos1 - pos; // wann kommt das ; als Ende eines numerischen entities?
			if (pos > -1) {
				if (pos1 > -1) {
					if (k < 12) {
						String s = dummy.substring(pos + 6, pos + 8).toLowerCase();
						char c = s.charAt(0);
						char c1 = s.charAt(1);
						if ((c == 'x' && ((c1 >= '0' && c1 <= '9') || (c1 >= 'a' && c1 <= 'f'))) || (c >= '0' && c <= '9')) {
							dummy = dummy.substring(0, pos + 1) + dummy.substring(pos + 5, dummy.length());
						}
					}
				}
				pos++;
			}
		}
		dummy = STRreplace.replace(dummy, "&amp;amp;", "&amp;"); // falls schon &amp; im str war

		dummy = STRreplace.replace(dummy, "<", "&lt;");
		dummy = STRreplace.replace(dummy, ">", "&gt;");
		dummy = STRreplace.replace(dummy, "\"", "&quot;");
		dummy = STRreplace.replace(dummy, "'", "&apos;");
		// why
		dummy = STRreplace.replace(dummy, "\u0004", "");
		// this means changing content,
		// but it is the easiest way of avoiding ]]> to be interpreted as endmark of CDATA-section
		dummy = STRreplace.replace(dummy, "]]>", "]] >");
		// \ in gpx is not imported by mapsource, basecamp, garmin?...(there is no replacement)
		dummy = STRreplace.replace(dummy, "\\", "BkSlsh;");

		return dummy;
	}

	public final static String strxmlencode(boolean src) {
		/* bools are always safe */
		return (src ? "true" : "false");
	}

	public final static String strxmlencode(int src) {
		/* numbers are always safe */
		return (Integer.toString(src));
	}

}
