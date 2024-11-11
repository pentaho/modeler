/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations.util;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class XMLUtil {

  /**
   *
   * @param xml
   * @return
   * @throws Exception
   */
  public static String compactPrint( String xml ) throws Exception {
    final OutputFormat format = OutputFormat.createCompactFormat();
    return print( xml, format );
  }

  /**
   *
   * @param xml
   * @return
   * @throws Exception
   */
  public static String prettyPrint( String xml ) throws Exception {
    final OutputFormat format = OutputFormat.createPrettyPrint();
    return print( xml, format );
  }

  /**
   *
   * @param xml
   * @param format
   * @return
   * @throws Exception
   */
  private static String print( final String xml, final OutputFormat format ) throws Exception {
    format.setSuppressDeclaration( true );
    final org.dom4j.Document document = DocumentHelper.parseText( xml );
    StringWriter sw = new StringWriter();
    final XMLWriter writer = new XMLWriter( sw, format );
    writer.write( document );
    return sw.toString();
  }

  /**
   *
   * @param xml
   * @return
   * @throws Exception
   */
  public static Node asDOMNode( String xml ) throws Exception {
    DocumentBuilderFactory dbFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse( new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) ) );
    return doc.getFirstChild();
  }

  /**
   * Update an attribute for an Element with the provided value. If
   * it does not exist, add it.
   *
   * @param element
   * @param name
   * @param value
   */
  public static void addOrUpdateAttribute( Element element, String name, String value ) {
    NamedNodeMap measureAttrs = element.getAttributes();
    Node attrNode = measureAttrs.getNamedItem( name );
    if ( attrNode != null ) {
      attrNode.setNodeValue( value );
    } else {
      element.setAttribute(
        name,
        value
      );
    }
  }
}
