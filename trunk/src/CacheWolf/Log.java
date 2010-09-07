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
	/** true, if the logger recommended the cache */
	private boolean recommended = false;
	
	/** Create a log from a single line in format<br>
	 * <pre>RECOMMENDED="1"<img src='ICON'>&nbsp;DATE LOGGER<br>MESSAGE
	 * or <img src='ICON'>&nbsp;DATE by LOGGER<br>MESSAGE</pre>
	 * @param logLine
	 */
	public Log(String logLine) {
//		RECOMMENDED="1"<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
		try {
			int ic1=logLine.indexOf("RECOMMENDED=\"1\"");
			if (ic1 >= 0) 
				recommended = true; else recommended = false;
			ic1=logLine.indexOf("<img src='");
			int ic2=logLine.indexOf("'",ic1+10);
			icon=logLine.substring(ic1+10,ic2);
			int d1=logLine.indexOf(";");
			date=logLine.substring(d1+1,d1+11);
			int l1=d1+12;
			if (logLine.substring(l1,l1+3).equals("by ")) l1+=3;
			int l2=logLine.indexOf("<br>",l1);
			logger=logLine.substring(l1,l2);
			message=logLine.substring(l2+4, logLine.indexOf("]]>", l1));
		} catch (Exception ex) {
			if (logLine.indexOf("<img")<0) { // Have we reached the line that states max logs reached 
				icon=MAXLOGICON; 
			} else {
				Global.getPref().log("Error parsing log: "+logLine);
				icon=INVALIDLOGICON;
			}	
			date="1900-00-00";
			logger=message="";
		}
	}
	
	public Log(String icon, String date, String logger, String message) {
		this(icon, date, logger, message, false);
	}
	
	public Log(String icon, String date, String logger, String message, boolean recommended_) {
		this.icon=icon;
		this.date=date;
		this.logger=logger;
		this.message=message.trim();
		this.recommended = recommended_;
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

	public boolean isRecomended() {
		return recommended;
	}
	
	public boolean isFoundLog() {
		return icon.equals(typeText2Image("Found"));
	}
	
	// if you change any of these make sure to check image2TypeText in the GPX exporters
	public static String typeText2Image(String typeText){
		if (typeText.equals("Found it")||typeText.equals("Found")||typeText.equals("find")) return "icon_smile.gif";
		if (typeText.equals("Didn't find it")||typeText.equals("Not Found")||typeText.equals("no_find")) return "icon_sad.gif";
		if (typeText.equals("Write note")||typeText.equals("Note")||typeText.equals("note")
			||typeText.equals("Not Attempted")||typeText.equals("Other")) return "icon_note.gif";
		if (typeText.equals("Enable Listing")) return "icon_enabled.gif";
		if (typeText.equals("Temporarily Disable Listing")) return "icon_disabled.gif";
		if (typeText.equals("Webcam Photo Taken")) return "icon_camera.gif";
		if (typeText.equals("Attended")) return "icon_attended.gif";
		if (typeText.equals("Publish Listing")) return "icon_greenlight.gif";
		if (typeText.equals("Will Attend")) return "icon_rsvp.gif";
		if (typeText.equals("Post Reviewer Note")) return "big_smile.gif";
		if (typeText.equals("Unarchive")) return "traffic_cone.gif";
		if (typeText.equals("Archive")) return "traffic_cone.gif";
		if (typeText.equals("Owner Maintenance")) return "icon_maint.gif";
		if (typeText.equals("Needs Maintenance")) return "icon_needsmaint.gif";
		if (typeText.equals("Needs Archived")) return "icon_remove.gif";
		if (typeText.equals("Update Coordinates")) return "coord_update.gif";
		if (typeText.equals("Retract Listing")) return "img_redlight.gif";
		Global.getPref().log("GPX Import: warning, unknown logtype "+typeText+" assuming Write note");
		return "icon_note.gif";
	}

	/** log was written by one of the aliases defined in preferences */
	public boolean isOwnLog() {
		return this.logger.equalsIgnoreCase(Global.getPref().myAlias) || this.logger.equalsIgnoreCase(Global.getPref().myAlias2); 
	}
	
	/** Return XML representation of log for storing in cache.xml */
	public String toXML(){
		StringBuffer s=new StringBuffer(400);
		s.append("<LOG>");
		if (recommended)
			s.append("RECOMMENDED=\"1\"");
		s.append("<![CDATA[");
		s.append(toHtml());
		s.append("]]>");
		s.append("</LOG>\r\n");
		return s.toString();
	}
	
	/** Return HTML representation of log for display on screen */
	public String toHtml(){
//		<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a wonderful log
		if (icon.equals(MAXLOGICON)) return "<hr>"+MyLocale.getMsg(736,"Too many logs")+"<hr>";
		StringBuffer s=new StringBuffer(300);
		s.append("<img src='"+icon+"'>");
		if (recommended) s.append("<img src='recommendedlog.gif'>");
		s.append("&nbsp;");
		s.append(date);
		s.append(" by ");
		s.append(logger);
		s.append("<br>");
		s.append(message.trim());
		return s.toString();
	}
}
