package CacheWolf.utils;

import ewe.util.Hashtable;

public final class XMLParser{

    final static int PARSE_START     = 0;
    final static int PARSE_NAME      = 1;
    final static int PARSE_PRE2      = 2;
    final static int PARSE_2         = 3;
    final static int PARSE_ATTRIBUTE = 4;

    public static Hashtable getAttributes (String xmlText, String elementName){ 
	String prefix = '<' + elementName+' ';
	int index = xmlText.indexOf(prefix);
	Hashtable attributes = new Hashtable();
	int state = PARSE_START;
	StringBuffer attrName = new StringBuffer();
	StringBuffer attrVal = new StringBuffer();
	for (int i=index + prefix.length(); i < xmlText.length();i++){
	    char ch = xmlText.charAt(i);
	    switch (state){
	    case PARSE_START:
		if (Character.isLetter(ch)){
		    state = PARSE_NAME;
		    attrName.append (ch);
		}
		break;
	    case PARSE_NAME:
		if (ch == '='){
		    state = PARSE_2;
		}
		else if (!(Character.isLetterOrDigit(ch))){
		    state = PARSE_PRE2;
		}
		else{
		    attrName.append(ch);
		}
		break;
	    case PARSE_PRE2:
		if (ch == '='){
		    state = PARSE_2;
		}
		break;
	    case PARSE_2:
		if (ch == '"'){
		    state = PARSE_ATTRIBUTE;
		}
		break;
	    case PARSE_ATTRIBUTE:
		if (ch == '"'){
		    attributes.put(attrName.toString(), attrVal.toString());
		    attrName = new StringBuffer();
		    attrVal = new StringBuffer();
		    state = PARSE_START;
		}
		else{
		    attrVal.append(ch);
		}
		break;
	    default:
		throw new IllegalStateException();
	    }
	}
	return attributes;
    }
}