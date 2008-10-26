package CacheWolf;

import ewe.fx.*;

public class Attributes {
	public final static int MAXATTRIBS=12;
	/**
	 * The number of attributes for this cache (=number of array elements in use in attribs)
	 */
	private int count=0;
	/**
	 * The attributes are represented as a byte array with each element encoding one attribute
	 */
	private Attribute attribs[]=new Attribute[MAXATTRIBS];

	public long attributesYes;
	public long attributesNo;

	public Attributes() { 
	}

	/**
	 * Get the number of attributes in the list
	 * @return number of attributes
	 */public int getCount(){
		return count;
	}
	
	/**
	 * Clear the attributes list
	 *
	 */
	 public void clear() {
		count=0;
		attributesYes=0;
		attributesNo=0;
	}
	
    ////////////////////////////////////////////////
    // Attribute set functions
    ////////////////////////////////////////////////
	
	public void XmlAttributesStart() {}
	public void XmlAttributesEnd(String elem){
		clear();
		Extractor ex=new Extractor(elem,"<ATT>","</ATT>",0,true);
		String dummy = ex.findNext();
		while(ex.endOfSearch()==false){
			add(dummy);
			dummy = ex.findNext();
		}
	}

	public void XmlAttributeStart(){}
	public void XmlAttributeEnd(String elem){
		add(elem);
	}

	/*
	 * Prepare for attributes to be written to cache.xml file 
	 */
	public String XmlAttributesWrite(){
		StringBuffer sb=new StringBuffer(1000);
		sb.append("<ATTRIBUTES>\n");
		for (int i=0; i<count; i++) {
			sb.append("   <ATT>");
			sb.append(attribs[i].getImageName());
			sb.append("</ATT>\n");
		}
		sb.append("</ATTRIBUTES>\n");
		return sb.toString();
	}
	
	/**
	 * Add a new attributes
	 * @param attributeName
	 */
	public void add(String attributeName){
		if (count<attribs.length) {
			if ( !attributeName.equalsIgnoreCase( "attribute-blank.gif" ) ) {
				Attribute attr = new Attribute(attributeName);
				attribs[count++] = attr;
				if (attributeName.endsWith("-yes.gif"))
					attributesYes |= (1l << ( (long)(Math.ceil(attr.getAttrNr() / 2.0) - 1.0) ) );
				else
					attributesNo |= (1l << ( (long)(Math.ceil(attr.getAttrNr() / 2.0) - 1.0) ) );
			}
		}
	}

	/**
	 * Get the name of the i'th attribute in the list
	 * @param i The number of the attribute to retrieve
	 * @return The name of the attribute (e.g. horses-no.gif)
	 */
	public String getName(int i) {
		if (i>=0 && i<count) return attribs[i].getImageName();
		return "error.gif";
	}
	
	/**
	 * Get the image corresponding to an attribute
	 * @param i The number of the attribute for which the image is to be retrieved
	 * @return The mImage of the attribute
	 */
	public mImage getImage(int i) {
		if (i>=0 && i<count) return attribs[i].getImage();
		return new Attribute().getImage();
	}

	/**
	 * Get the infotext for an attribute
	 * @param i The number of the attribute for which the infotext is to be retrieved
	 * @return The infotext of the attribute
	 */
	public String getInfo(int i) {
		if (i>=0 && i<count) return attribs[i].getInfo();
		return "No info";
	}
}

