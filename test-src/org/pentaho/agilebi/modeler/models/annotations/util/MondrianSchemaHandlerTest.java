/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import mondrian.olap.MondrianDef;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;

import static junit.framework.Assert.assertTrue;

/**
 * Created by pminutillo on 3/5/15.
 */
public class MondrianSchemaHandlerTest {
  private static final String TEST_FILE_PATH = "test-res/products.mondrian.xml";
  private static final String TEST_MEASURE_NAME = "TestMeasure";
  private static final String TEST_AGG_TYPE = "SUM";
  private static final String TEST_COLUMN = "ColumnName";
  private static final String MEASURE_NODE_NAME = "Measure";
  private static final String CALCULATED_MEMBER_NODE_NAME = "CalculatedMember";


  private static final String TEST_CALC_MEMBER_CAPTION = "Test Caption";
  private static final String TEST_CALC_MEMBER_FORMULA = "Test Calc Formula";
  private static final String TEST_CALC_MEMBER_NAME = "Test Calc Name";
  private static final String TEST_CALC_MEMBER_DIMENSION = "Test Calc Dimension";
  private static final String TEST_CALC_MEMBER_DESCRIPTION = "Test Calc Description";



  Document schemaDocument;

  @Before
  public void setUp() throws Exception {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      schemaDocument = documentBuilder.parse( TEST_FILE_PATH );
    } catch ( ParserConfigurationException e ) {
      e.printStackTrace();
    }
   }

  @Test
  public void testAddMeasure(){
    assertTrue( schemaDocument != null );

    MondrianDef.Measure measure = new MondrianDef.Measure();
    measure.name = TEST_MEASURE_NAME;
    measure.aggregator = TEST_AGG_TYPE;
    measure.column = TEST_COLUMN;

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    try {
      mondrianSchemaHandler.addMeasure( null, measure );
    } catch ( ModelerException e ) {
      e.printStackTrace();
    }

    boolean testMeasureFound = false;
    NodeList nodeList = schemaDocument.getElementsByTagName( MEASURE_NODE_NAME );
    for( int x = 0; x <= nodeList.getLength() - 1; x++ ){
      Node measureNode = nodeList.item( x );
      String measureName = measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.MEASURE_NAME_ATTRIBUTE ).getNodeValue();
      if( measureName != null ){
        if( measureName.equals( TEST_MEASURE_NAME )){
          testMeasureFound = true;
        }
      }
    }

    assertTrue( testMeasureFound );
  }

  @Test
  public void testAddCalculatedMember(){
    assertTrue( schemaDocument != null );

    MondrianDef.CalculatedMember calculatedMember = new MondrianDef.CalculatedMember();
    calculatedMember.caption = TEST_CALC_MEMBER_CAPTION;
    calculatedMember.formula = TEST_CALC_MEMBER_FORMULA;
    calculatedMember.name = TEST_CALC_MEMBER_NAME;
    calculatedMember.dimension = TEST_CALC_MEMBER_DIMENSION;
    calculatedMember.description = TEST_CALC_MEMBER_DESCRIPTION;
    calculatedMember.visible = true;

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    try{
      mondrianSchemaHandler.addCalculatedMember( null, calculatedMember );
    } catch ( ModelerException e ) {
      e.printStackTrace();
    }

    Element measureNode = null;

    NodeList nodeList = schemaDocument.getElementsByTagName( CALCULATED_MEMBER_NODE_NAME );
    for( int x = 0; x <= nodeList.getLength() - 1; x++ ){
      measureNode = (Element) nodeList.item( x );
      
      String measureName = measureNode.getAttribute( MondrianSchemaHandler.CALCULATED_MEMBER_NAME_ATTRIBUTE );

      if( measureName != null ){
        if( measureName.equals( TEST_CALC_MEMBER_NAME )) {
          break;
        }
      }
    }

    String schemaXml = xmlDocToString( mondrianSchemaHandler.getSchema() );

    assertTrue( measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.CALCULATED_MEMBER_NAME_ATTRIBUTE ).getNodeValue().equals( TEST_CALC_MEMBER_NAME ));
    assertTrue( measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.CALCULATED_MEMBER_CAPTION_ATTRIBUTE ).getNodeValue().equals( TEST_CALC_MEMBER_CAPTION ));
    assertTrue( measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.CALCULATED_MEMBER_FORMULA_ATTRIBUTE ).getNodeValue().equals( TEST_CALC_MEMBER_FORMULA ));
    assertTrue( measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.CALCULATED_MEMBER_DIMENSION_ATTRIBUTE ).getNodeValue().equals( TEST_CALC_MEMBER_DIMENSION ));
    assertTrue( measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.CALCULATED_MEMBER_DESCRIPTION_ATTRIBUTE ).getNodeValue().equals( TEST_CALC_MEMBER_DESCRIPTION ));
  }

  private static String xmlDocToString( Document doc ){
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );

      StringWriter writer = new StringWriter();
      DOMSource source = new DOMSource( doc );
      StreamResult result = new StreamResult( writer );

      transformer.transform( source, result );
      return writer.toString();
    }
    catch( Exception e ){
      return null;
    }
  }
}
