package CacheWolf;

public class STRreplace{
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
		   int index02 = 0;
		   StringBuffer sb = new StringBuffer();
		   while (index01 != -1)
		   {
			   sb.append(s.substring(index02,index01)).append(r);			   
			   index02 = index01 + f.length();
			   index01 = s.indexOf( f, index02 );
			  /* original impl.
			  s = s.substring(0,index01) + r + s.substring(index01+f.length());
			  index01 += r.length();
			  index01 = s.indexOf( f, index01 );
			  */
		   }
		   return sb.append(s.substring(index02)).toString();
		}
}
