package CacheWolf;
/**
 * A list of GC travelbugs
 * @author salzkammergut
 */
import ewe.util.Vector;
import ewe.io.*;
import ewesoft.xml.*;
import ewesoft.xml.sax.*;

public class TravelbugList extends MinML{

	/** The Vector containing the Travelbug objects */
	private Vector tbList=new Vector(10);
	
	/** Get the travelbug at a certain position in the list */
	public Travelbug getTB(int i) {
		return (Travelbug) tbList.elementAt(i);
	}
	
	/** Return the size of the list */
	public int size() {
		return tbList.size();
	}
	
	/** Clear the travelbug list */
	public void clear() {
		tbList.clear();
	}

	/** Add a travelbug to the list */
	public void add(Travelbug tb) {
		tbList.add(tb);
	}
	
	/** Remove a travelbug from the list */
	public void remove(int i) {
		tbList.removeElementAt(i);
	}

	/** Replace a travelbug in the list */
	public void replace(int i, Travelbug tb) {
		tbList.set(i, tb);
	}
	
	/** Construct an empty travelbug list */
	public TravelbugList() {
	}
	
	/** Find a travelbug in the list. 
	 *  Return null if not found */
	public Travelbug find(String name) {
		name=name.trim();
		for (int i=size()-1; i>=0; i--)
			if (name.equals(getTB(i).getName().trim())) return getTB(i);
		return null;
	}
	
	/** Convert the old representation to a new one. In the old representation,
	 * all travelbugs were stored as one HTML string within the cache.xml file.
	 * This representation does not include the id or guid and does not allow for
	 * unique identification of the travelbug (Several travelbugs with identical
	 * names may exist, so the id/guid must be stored to uniquely identify the travelbug. 
	 * All TBs are stored as one HTML string <b>Name:</b>name_of_tb<br>mission<hr>
	 */
	public void addFromHTML(String htmlList) {
		int fnd;
		fnd=htmlList.indexOf("<b>Name:</b>");
		while(fnd>=0) {
			int fnd2=htmlList.indexOf("<br>",fnd+12);
			int fnd3=htmlList.indexOf("<b>Name:</b>",fnd2+4);
			Travelbug tb=new Travelbug(htmlList.substring(fnd+12,fnd2));
			String mission;
			if (fnd3>0) {
				mission=htmlList.substring(fnd2+4,fnd3);
			} else {
				mission=htmlList.substring(fnd2+4);
			}
			if (mission.endsWith("<hr>")) mission=mission.substring(0,mission.length()-4);
			tb.setMission(mission);
			tbList.add(tb);
			fnd=fnd3;
		}
	}
	
	/** Return list of travelbugs in HTML representation */
	public String toHtml() {
		int size=tbList.size();
		StringBuffer s=new StringBuffer(size*300);
		for (int i=0; i<size; i++) {
			s.append(getTB(i).toHtml());
		}
		return s.toString();
	}

	/** Return list of travelbugs in XML representation */
	public String toXML() {
		int size=tbList.size();
		StringBuffer s=new StringBuffer(size*300);
		s.append("<TRAVELBUGS>\n");
		for (int i=0; i<size; i++) {
			s.append(getTB(i).toXML());
		}
		s.append("</TRAVELBUGS>\n");
		return s.toString();
	}
	
	/*=====================================================================
	 * The following section implements the XML parser for a travelbug list
       as contained in the cache.xml file 
      =====================================================================*/ 	    
	private String lastName="";
	private Travelbug tb;
	private StringBuffer xmlElement=new StringBuffer(200);
	
	/** 
	 * Parse the travelbug part of a cache. The XML String passed as an argument
	 * must contain the enclosing <TRAVELBUGS> ... </TRAVELBUGS> XML tags. 
	 */
	public void addFromXML(String XMLString) {
		try {
			parse(new StringReader(XMLString));
		} catch (Exception e) {
			if (e instanceof NullPointerException)
				Global.getPref().log("Error reading cache-travelbug list: NullPointerException in Element "+lastName +". Wrong attribute?",e,true);
			else 
				Global.getPref().log("Error reading cache-travelbug list: ", e);
		};
	}
	
	/**
	 * Method that gets called when a new element has been identified
	 */
	public void startElement(String name, AttributeList atts){
		lastName=name;
		if (name.equals("tb")) {
			tb=new Travelbug(atts.getValue("guid"),"","");
		}
	}

	public void characters( char ch[], int start, int length ) {
			xmlElement.append(ch,start,length); // Collect the mission or the name
	}	

	public void endElement(String tag){
		if (tag.equals("tb")) {
			tb.setMission(xmlElement.toString());
			tbList.add(tb);
			xmlElement.delete(0,xmlElement.length());
		}
		if (tag.equals("name")) {
			tb.setName(xmlElement.toString());
			xmlElement.delete(0,xmlElement.length());
		}
	}

}
