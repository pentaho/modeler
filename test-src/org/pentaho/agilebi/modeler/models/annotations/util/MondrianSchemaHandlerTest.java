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

import static junit.framework.Assert.*;
import static org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler.*;

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

  private static final String TEST_AVERAGE_AGG_TYPE = "avg";
  private static final String TEST_NUM_DECIMAL_FORMAT_STRING = "##.##";
  private static final String TEST_EXISTING_MEASURE_STRING = "bc_BUYPRICE";

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
  public void testAddMeasure() {
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
    for ( int x = 0; x <= nodeList.getLength() - 1; x++ ) {
      Node measureNode = nodeList.item( x );
      String measureName = measureNode.getAttributes().getNamedItem( MondrianSchemaHandler.MEASURE_NAME_ATTRIBUTE ).getNodeValue();
      if ( measureName != null ) {
        if ( measureName.equals( TEST_MEASURE_NAME ) ) {
          testMeasureFound = true;
        }
      }
    }

    assertTrue( testMeasureFound );
  }

  @Test
  public void testAddCalculatedMember() {
    assertTrue( schemaDocument != null );

    MondrianDef.CalculatedMember calculatedMember = new MondrianDef.CalculatedMember();
    calculatedMember.caption = TEST_CALC_MEMBER_CAPTION;
    calculatedMember.formula = TEST_CALC_MEMBER_FORMULA;
    calculatedMember.name = TEST_CALC_MEMBER_NAME;
    calculatedMember.dimension = TEST_CALC_MEMBER_DIMENSION;
    calculatedMember.description = TEST_CALC_MEMBER_DESCRIPTION;
    calculatedMember.visible = true;
    MondrianDef.CalculatedMemberProperty property1 = new MondrianDef.CalculatedMemberProperty();
    property1.name = "name1";
    property1.value = "value1";
    MondrianDef.CalculatedMemberProperty property2 = new MondrianDef.CalculatedMemberProperty();
    property2.name = "name2";
    property2.value = "value2";
    calculatedMember.memberProperties = new MondrianDef.CalculatedMemberProperty[]{property1, property2};

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    try {
      mondrianSchemaHandler.addCalculatedMember( null, calculatedMember );
    } catch ( ModelerException e ) {
      e.printStackTrace();
    }

    Element measureNode = null;

    NodeList nodeList = schemaDocument.getElementsByTagName( CALCULATED_MEMBER_NODE_NAME );
    for ( int x = 0; x <= nodeList.getLength() - 1; x++ ) {
      measureNode = (Element) nodeList.item( x );

      String measureName = measureNode.getAttribute( CALCULATED_MEMBER_NAME_ATTRIBUTE );

      if ( measureName != null ) {
        if ( measureName.equals( TEST_CALC_MEMBER_NAME ) ) {
          break;
        }
      }
    }

    assertNotNull( measureNode );
    assertEquals(
        TEST_CALC_MEMBER_NAME,
        measureNode.getAttributes().getNamedItem( CALCULATED_MEMBER_NAME_ATTRIBUTE ).getNodeValue() );
    assertEquals(
        TEST_CALC_MEMBER_CAPTION,
        measureNode.getAttributes().getNamedItem( CALCULATED_MEMBER_CAPTION_ATTRIBUTE ).getNodeValue() );
    assertEquals(
        TEST_CALC_MEMBER_FORMULA,
        measureNode.getAttributes().getNamedItem( CALCULATED_MEMBER_FORMULA_ATTRIBUTE ) .getNodeValue() );
    assertEquals(
        TEST_CALC_MEMBER_DIMENSION,
        measureNode.getAttributes().getNamedItem( CALCULATED_MEMBER_DIMENSION_ATTRIBUTE ).getNodeValue() );
    assertEquals(
        TEST_CALC_MEMBER_DESCRIPTION,
        measureNode.getAttributes().getNamedItem( CALCULATED_MEMBER_DESCRIPTION_ATTRIBUTE ).getNodeValue() );
    NodeList childNodes = measureNode.getChildNodes();
    assertEquals( 2, childNodes.getLength() );
    assertEquals(
        "name1",
        childNodes.item( 0 ).getAttributes().getNamedItem( CALCULATED_MEMBER_PROPERTY_NAME_ATTRIBUTE ).getNodeValue() );
    assertEquals( "value1", childNodes.item( 0 ).getAttributes().getNamedItem( "value" ).getNodeValue() );
    assertEquals( "name2",
        childNodes.item( 1 ).getAttributes().getNamedItem( CALCULATED_MEMBER_PROPERTY_NAME_ATTRIBUTE ).getNodeValue() );
    assertEquals( "value2", childNodes.item( 1 ).getAttributes().getNamedItem( "value" ).getNodeValue() );
  }

  @Test
  public void testUpdateMeasure() throws ModelerException {
    assertTrue( schemaDocument != null );

    MondrianDef.Measure measure = new MondrianDef.Measure();
    measure.name = TEST_MEASURE_NAME;
    measure.aggregator = TEST_AVERAGE_AGG_TYPE;
    measure.formatString = TEST_NUM_DECIMAL_FORMAT_STRING;

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    assertTrue( mondrianSchemaHandler.updateMeasure( null, TEST_EXISTING_MEASURE_STRING, measure ) );

    boolean testMeasureFound = false;
    NodeList nodeList = schemaDocument.getElementsByTagName( MEASURE_NODE_NAME );
    for ( int x = 0; x <= nodeList.getLength() - 1; x++ ) {
      Node measureNode = nodeList.item( x );
      String measureName = measureNode.getAttributes().getNamedItem(
          MondrianSchemaHandler.MEASURE_NAME_ATTRIBUTE ).getNodeValue();
      if ( measureName != null && measureName.equals( TEST_MEASURE_NAME ) ) {
        testMeasureFound = true;
        assertEquals( TEST_AVERAGE_AGG_TYPE,
            measureNode.getAttributes().getNamedItem(
                MondrianSchemaHandler.MEASURE_AGGREGATOR_ATTRIBUTE ).getNodeValue() );
        assertEquals( TEST_NUM_DECIMAL_FORMAT_STRING,
            measureNode.getAttributes().getNamedItem(
                MondrianSchemaHandler.MEASURE_FORMAT_STRING_ATTRIBUTE ).getNodeValue() );
        break;
      }
    }

    assertTrue( testMeasureFound );
  }

  @Test
  public void testUpdatingMeasureNotFoundReturnsFalse() throws Exception {
    assertTrue( schemaDocument != null );

    MondrianDef.Measure measure = new MondrianDef.Measure();
    measure.name = TEST_MEASURE_NAME;
    measure.aggregator = TEST_AVERAGE_AGG_TYPE;
    measure.formatString = TEST_NUM_DECIMAL_FORMAT_STRING;

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    assertFalse( mondrianSchemaHandler.updateMeasure( null, "MeasureNotFound", measure ) );
  }

  @Test
  public void testUpdatingMeasureToExistingReturnsFalse() throws Exception {
    assertTrue( schemaDocument != null );

    MondrianDef.Measure measure = new MondrianDef.Measure();
    measure.name = TEST_EXISTING_MEASURE_STRING;
    measure.aggregator = TEST_AVERAGE_AGG_TYPE;
    measure.formatString = TEST_NUM_DECIMAL_FORMAT_STRING;

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schemaDocument );

    assertFalse( mondrianSchemaHandler.updateMeasure( null, "bc_QUANTITYINSTOCK", measure ) );
  }
}
