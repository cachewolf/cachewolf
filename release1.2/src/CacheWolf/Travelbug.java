package CacheWolf;
/**
 * This contains the basic information of a GC travelbug.
 * @author salzkammergut
 *
 */
public class Travelbug {
	/** GC unique id or guid (both are used depending on how the TB is picked up).
	 * Travelbugs retrieved from a cache use the guid, travelbugs entered manually
	 * use the id */
	private String guid;       //0
	/** GC Name i.e. "First Roman Geocoin" */
	private String name;       //1
	/** GC tracking no i.e. 652345, needed for logging */
	private String trackingNo; //2 
	/** GC Mission */
	private String mission;    //3

	/** Construct a travelbug with a given name */
	public Travelbug(String name) {
		this("",name,"");
	}

	/** Construct a travelbug with id, name and mission */
	public Travelbug(String guid, String name, String mission) {
		setGuid(guid);
		setName(name);
		setMission(mission);
		setTrackingNo("");
	}
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = SafeXML.cleanback(name);
	}

	public String getTrackingNo() {
		return trackingNo;
	}

	public void setTrackingNo(String trackingNo) {
		this.trackingNo = trackingNo;
	}

	public void setMission(String mission) {
		this.mission = mission;
	}

	public String getMission() {
		return this.mission;
	}
	
	/** Return XML representation of travelbug for storing in cache.xml */
	public String toXML(){
		StringBuffer s=new StringBuffer(300);
		s.append("  <tb guid=\"");
		s.append(guid);
		s.append("\"><name><![CDATA[");
		s.append(name);
		s.append("]]></name><![CDATA[");
		s.append(mission);
		s.append("]]></tb>\n");
		return s.toString();
	}
	
	/** Return HTML representation of travelbug for display on screen */
	public String toHtml(){
		StringBuffer s=new StringBuffer(300);
		s.append("<b>Name:</b> ");
		s.append(name);
		s.append("<br>");
		s.append(mission);
		s.append("<hr>");
		return s.toString();
	}

}
