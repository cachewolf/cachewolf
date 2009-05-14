//package org.minml;
package ewesoft.xml;

public interface Parser extends ewesoft.xml.sax.Parser {
  void setDocumentHandler(DocumentHandler handler);
}
