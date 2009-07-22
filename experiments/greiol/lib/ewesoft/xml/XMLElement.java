/*
 * Created on Jan 20, 2005
 */
package ewesoft.xml;
import ewe.util.Vector;
import ewe.data.PropertyList;
/**
 * This class represents the data within the open and close tags
 * of an XML entry. It holds the name of the entry (the <b>tag</b>)
 * and either the text for the entry, or the sub-elements within
 * that entry.
 * @author Michael L Brereton, Ewesoft.com.
 */
public class XMLElement {
	/**
	 * This is the tag for this XMLElement.
	 */
	public String tag;
	/**
	 * If there is any text within the XML tags, then this value
	 * will be set to be that text, otherwise it will be an empty String. 
	 */
	public String text = "";
	/**
	 * If there are any sub-elements within this element, then
	 * they are stored in this Vector. Otherwise this Vector will
	 * be null.
	 */
	public Vector subElements;
	/**
	 * If there are any attributes for the XML element, they
	 * will be placed here.
	 */
	public PropertyList attributes;
	/**
	 * This holds the parent XMLElement as determined during
	 * parsing. The root element will have no parent.
	 */
	public XMLElement parent;
	/**
	 * Format the XMLElement and its children into a String.
	 * @param prefix tabs or spaces to put in front of the format. 
	 * @return the XMLElement and its children into a String.
	 */
	public String format(String prefix)
	{
		String ret = new String();
		ret += prefix+"<"+tag+">";
		if (prefix.length() == 0) prefix += " ";
		else prefix += prefix.charAt(0);
		if (attributes != null) ret += attributes;
		ret += "\n";
		if (text.length() != 0) ret += prefix+"\""+text+"\""+"\n";
		if (subElements != null){
			for (int i = 0; i<subElements.size(); i++)
				ret += ((XMLElement)subElements.get(i)).format(prefix);
		}
		return ret;
	}
	/**
	 * Format the XMLElement and its children into a String, with
	 * each sub-element prefixed by increasing numbers of spaces.
	 * @return the XMLElement and its children into a String.
	 */
	public String format()
	{
		return format("");
	}
}
