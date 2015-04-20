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

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.easymock.EasyMock.createMock;

public class CreateCalculatedMemberTest {
  private static final String TEST_CALCULATED_MEMBER_NAME = "TestCalculatedMember";
  private static final String TEST_CALCULCATED_MEMBER_CAPTION = "Test Caption";
  private static final String TEST_CALCULATED_MEMBER_DESC = "Test Description";
  private static final String TEST_CALCULATED_MEMBER_FORMULA = "Test Formula";
  private static final String TEST_CALCULATED_MEMBER_DIMENSION = "Test Dimension";
  private static final String TEST_CALCULATED_MEMBER_FORMAT_STRING = "$#,##0.00";
  private static final String MONDRIAN_TEST_FILE_PATH = "test-res/products.mondrian.xml";
  private static final String TEST_FIELD = "test field";
  private static final String CALCULATED_MEMBER_ELEMENT_NAME = "CalculatedMember";
  private static final String CAPTION_ATTRIB = "caption";
  private static final String DESCRIPTION_ATTRIB = "description";
  private static final String DIMENSION_ATTRIB = "dimension";
  private static final String FORMULA_ATTRIB = "formula";
  private static final String NAME_ATTRIB = "name";
  private static final String VISIBLE_ATTRIB = "visible";
  private static final String FORMAT_STRING_ATTRIB = "formatString";

  CreateCalculatedMember createCalculatedMember = new CreateCalculatedMember();
  Document mockDocument;

  @Before
  public void setUp() throws Exception {
    createCalculatedMember.setName( TEST_CALCULATED_MEMBER_NAME );
    createCalculatedMember.setCaption( TEST_CALCULCATED_MEMBER_CAPTION );
    createCalculatedMember.setDescription( TEST_CALCULATED_MEMBER_DESC );
    createCalculatedMember.setFormula( TEST_CALCULATED_MEMBER_FORMULA );
    createCalculatedMember.setDimension( TEST_CALCULATED_MEMBER_DIMENSION );
    createCalculatedMember.setFormatString( TEST_CALCULATED_MEMBER_FORMAT_STRING );
    createCalculatedMember.setVisible( Boolean.TRUE );
  }

  @Test
  public void testApplyMondrian() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
      DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse( mondrianSchemaXmlFile );

    createCalculatedMember.apply( mondrianSchemaXmlDoc, TEST_FIELD );

    assert ( mondrianSchemaXmlDoc != null );
    assert ( mondrianSchemaXmlDoc.getElementsByTagName( CALCULATED_MEMBER_ELEMENT_NAME ).getLength() > 0 );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, CAPTION_ATTRIB, TEST_CALCULCATED_MEMBER_CAPTION ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, DESCRIPTION_ATTRIB, TEST_CALCULATED_MEMBER_DESC ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, DIMENSION_ATTRIB, TEST_CALCULATED_MEMBER_DIMENSION ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, FORMULA_ATTRIB, TEST_CALCULATED_MEMBER_FORMULA ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, NAME_ATTRIB, TEST_CALCULATED_MEMBER_NAME ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, FORMAT_STRING_ATTRIB, TEST_CALCULATED_MEMBER_FORMAT_STRING ) );
    assert ( validateNodeValue( mondrianSchemaXmlDoc, VISIBLE_ATTRIB, Boolean.TRUE.toString() ) );
  }

  /**
   * Calculated member not yet supported in DSW
   *
   * @throws Exception
   */
  @Test
  public void testApplyDSW() throws Exception {
    ModelerWorkspace modelerWorkspace = createMock( ModelerWorkspace.class );
    try {
      createCalculatedMember.apply( modelerWorkspace, null, null );
    } catch ( Exception e ) {
      assert ( e.getClass().isAssignableFrom( UnsupportedOperationException.class ) );
    }
  }

  @Test
  public void testValidate() throws Exception {
    createCalculatedMember.validate();
  }

  /**
   * @param docToTest
   * @param nodeName
   * @param testValue
   * @return
   */
  private boolean validateNodeValue( Document docToTest, String nodeName, String testValue ) {
    boolean validated = false;
    NodeList calculatedMemberNodeList = docToTest.getElementsByTagName( CALCULATED_MEMBER_ELEMENT_NAME );
    if ( calculatedMemberNodeList.getLength() > 0 ) {
      // just check the first element
      Element calculatedMemberNode = (Element) calculatedMemberNodeList.item( 0 );
      if( calculatedMemberNode != null ) {
        validated = testValue.equals( calculatedMemberNode.getAttribute( nodeName ) );
      }
    }

    return validated;
  }
}