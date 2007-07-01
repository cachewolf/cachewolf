package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.graphics.*;
import ewe.util.*;

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

	public Attributes() { 
//		attribs[0]=new Attribute("camping-no.gif"); attribs[1]=new Attribute("dogs-yes.gif"); attribs[2]=new Attribute("onehour-yes.gif"); count=3; //TODO REMOVE THIS
	}

	public int getCount(){
		return count;
	}
	
	public void clear() {
		count=0;
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
	
	public void add(String attributeName){
		if (count<attribs.length) attribs[count++]=new Attribute(attributeName);
    }

	public String getName(int i) {
		if (i>=0 && i<count) return attribs[i].getImageName();
		return "error.gif";
	}
	
	public mImage getImage(int i) {
		if (i>=0 && i<count) return attribs[i].getImage();
		return new Attribute().getImage();
	}

	public String getInfo(int i) {
		if (i>=0 && i<count) return attribs[i].getInfo();
		return "No info";
	}
}

