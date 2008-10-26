/*
 * Created on Jan 20, 2005
 *
 */
package ewesoft.xml;

import ewesoft.xml.sax.AttributeList;
import ewesoft.xml.sax.SAXException;
import java.util.Vector;
import eve.data.PropertyList;

/**
 * This class will fully decode an XML document from a Reader.
 * It parses the XML document and creates a tree of XMLElement
 * objects, with the root of the tree being the <b>document</b>
 * element of the XMLDecoder.
 * <p>
 * To use simply create a Reader to provide the characters of the
 * XML document and then call parse() on that reader. 
 * @author Michael Brereton, Ewesoft.com.
 */
public class XMLDecoder extends MinML {

	/**
	 * This is the XMLElement that represents the entire document.
	 * It will be valid only after the parse() method has completed
	 * successfully.
	 */
	public XMLElement document;
	
	private XMLElement current;

	/* (non-Javadoc)
	 * @see ewesoft.xml.sax.DocumentHandler#startElement(java.lang.String, ewesoft.xml.sax.AttributeList)
	 */
	public void startElement(String name, AttributeList atts)
			throws SAXException {
		if (current == null){
			current = document = new XMLElement();
		}else{
			XMLElement xe = new XMLElement();
			current.subElements.add(xe);
			xe.parent = current;
			current = xe;
		}
		current.tag = name;
		if (atts != null && atts.getLength() == 0)
			atts = null;
		if (atts != null){
			current.attributes = new PropertyList();
			int max = atts.getLength();
			for (int i = 0; i<max; i++)
				current.attributes.add(atts.getName(i),atts.getValue(i));
		}
	}

	/* (non-Javadoc)
	 * @see ewesoft.xml.sax.DocumentHandler#endElement(java.lang.String)
	 */
	public void endElement(String name) throws SAXException {
		current = current.parent;
	}

	/* (non-Javadoc)
	 * @see ewesoft.xml.sax.DocumentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		current.text += new String(ch,start,length);
	}
/**
 * This main() method takes the argument from the command line
 * as the name of an XMLFile. It then attempts to open and parse
 * the file. If successful, it will then call format() on the
 * document XMLElement produced and then display this on the
 * console.
 * @param args
 * @throws ewe.io.IOException
 * @throws SAXException
 */
	public static void main(String[] args) throws java.io.IOException, SAXException
	{
		eve.ui.Application.startApplication(args);
		java.io.Reader r = new java.io.InputStreamReader(new java.io.FileInputStream(args[0]));
		XMLDecoder xd = new XMLDecoder();
		xd.parse(r);
		r.close();
		eve.sys.Vm.debug(xd.document.format());
		eve.ui.Application.exit(0);
	}
}
