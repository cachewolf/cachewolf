//package org.minml;
package ewesoft.xml;

import ewe.io.Writer;
import ewesoft.xml.sax.SAXException;
import ewesoft.xml.sax.AttributeList;

public interface DocumentHandler extends ewesoft.xml.sax.DocumentHandler {
  Writer startDocument(final Writer writer) throws SAXException;
  Writer startElement(final String name, final AttributeList attributes, final Writer writer)
        throws SAXException;
}
