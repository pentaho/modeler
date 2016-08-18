/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package org.pentaho.agilebi.modeler.models.annotations.util;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
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
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
