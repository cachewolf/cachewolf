package CacheWolf;

public class Log {
	private static String MAXLOGICON="MAXLOG";
	private static String INVALIDLOGICON=null;
	/** The icon which describes the log e.g. icon_sad */
	private String icon;
	/** The date in format yyyy-mm-dd */
	private String date;
	/** The person who logged the cache */
	private String logger;
	/** The logged message */
	private String message;
	
	/** Create a log from a single line in format<br>
	 * <pre><img src='ICON'>&nbsp;DATE LOGGER<br>MESSAGE
	 * or <img src='ICON'>&nbsp;DATE by LOGGER<br>MESSAGE</pre>
	 * @param logLine
	 */
	Log(String logLine) {
//		<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
		try {
			int ic1=logLine.indexOf("<img src='");
			int ic2=logLine.indexOf("'",ic1+10);
			icon=logLine.substring(ic1+10,ic2);
			int d1=logLine.indexOf(";");
			date=logLine.substring(d1+1,d1+11);
			int l1=d1+12;
			if (logLine.substring(l1,l1+3).equals("by ")) l1+=3;
			int l2=logLine.indexOf("<br>",l1);
			logger=logLine.substring(l1,l2);
			message=logLine.substring(l2+4);
		} catch (Exception ex) {
			Global.getPref().log("Error parsing log: "+logLine);
			icon=INVALIDLOGICON;
			date="1900-00-00";
			logger=message="";
		}
	}
	
	Log(String icon, String date, String logger, String message) {
		this.icon=icon;
		this.date=date;
		this.logger=logger;
		this.message=message.trim();
	}
	
	public static Log maxLog() {
		return new Log(MAXLOGICON,"1900-00-00","","");
	}
	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getLogger() {
		return logger;
	}
	public void setLogger(String logger) {
		this.logger = logger;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message.trim();
	}

	/** Return XML representation of log for storing in cache.xml */
	public String toXML(){
		StringBuffer s=new StringBuffer(400);
		s.append("<LOG><![CDATA[");
		s.append(toHtml());
		s.append("]]></LOG>\r\n");
		return s.toString();
	}
	
	/** Return HTML representation of log for display on screen */
	public String toHtml(){
//		<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
		if (icon.equals(MAXLOGICON)) return "<hr>"+MyLocale.getMsg(736,"Too many logs")+"<hr>";
		StringBuffer s=new StringBuffer(300);
		s.append("<img src='");
		s.append(icon);
		s.append("'>&nbsp;");
		s.append(date);
		s.append(" by ");
		s.append(logger);
		s.append("<br>");
		s.append(message.trim());
		return s.toString();
	}
}
