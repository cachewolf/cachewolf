package CacheWolf;

/**
*	A class to replace unsafe XML characters with characters that a user
*	"can read", and vice versa
*/
public class SafeXML{
	
	/**
	*	This method encodes special characters into
	*	HTML coded characters.
	*	see here: http://www.w3.org/MarkUp/html-spec/html-spec_13.html
	*/
	public static String clean(String str){
		String dummy = new String();
		
		dummy = replace(str,  "&", "&#38;");
		dummy = replace(dummy, "<", "&#60;");
		dummy = replace(dummy, ">", "&#62;");
		dummy = replace(dummy,  "\"" ,"&#34;");
		dummy = replace(dummy,  "�","&#176;");
		dummy = replace(dummy, "'","&apos;");
		dummy = replace(dummy, "'", "&#180;");
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
	public static String cleanback(String str){
		String dummy = new String();
		dummy = replace(str,  "&#38;", "&");
		dummy = replace(dummy,  "&#223;", "�");
		dummy = replace(dummy,  "&#60;","<");
		dummy = replace(dummy,  "&#62;",">");
		dummy = replace(dummy,  "&#34;","\"");
		dummy = replace(dummy,  "&#176;","�");
		dummy = replace(dummy, "&apos;","'");
		dummy = replace(dummy, "&#180;", "'");
		dummy = replace(dummy, "&#252;","�");
		dummy = replace(dummy, "&#228;","�");
		dummy = replace(dummy, "&#246;","�");
		dummy = replace(dummy, "&#196;","�");
		dummy = replace(dummy, "&#214;","�");
		dummy = replace(dummy, "&#220;","�");
		dummy = replace(dummy, "&amp;", "&");
		dummy = replace(dummy, "&quot;", "\"");	
		dummy = replace(dummy, "&lt;", "<");
		dummy = replace(dummy, "&gt;", ">");
		dummy = replace(dummy, "&apos;", "'");
	
		return dummy;
	}
	
	/* Replace all instances of a String in a String.
		 *   @param  s  String to alter.
		 *   @param  f  String to look for.
		 *   @param  r  String to replace it with, or null to just remove it.
		 */ 
		private static String replace( String s, String f, String r )
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
