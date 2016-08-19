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

package org.pentaho.agilebi.modeler.models.annotations;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.AnnotationConstants;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Created by pminutillo on 1/22/2016.
 */
public class UpdateCalculatedMemberTest {

  private UpdateCalculatedMember updateCalculatedMemberAnnotation = new UpdateCalculatedMember();
  private IMetaStore metaStore;

  private static String METADATA_TEST_FILE_PATH = "src/test/resources/products.xmi";
  private static final String MONDRIAN_TEST_FILE_PATH = "src/test/resources/products.with.calc.measures.mondrian.xml";

  private static final String TEST_CUBE_NAME = "products_38GA";
  private static final String TEST_CALC_MEMBER_CAPTION = "Updated Test Caption";
  private static final String TEST_CALC_MEMBER_FORMULA = "Updated Test Calc Formula";
  private static final String TEST_CALC_MEMBER_NAME = "Updated Test Calc Name";
  private static final String TEST_CALC_MEMBER_SOURCE_NAME = "Test Calc Name";
  private static final String TEST_CALC_MEMBER_DIMENSION = "Updated Test Calc Dimension";
  private static final String TEST_CALC_MEMBER_DESCRIPTION = "Updated Test Calc Description";
  private static final int TEST_CALC_MEMBER_FORMAT_SCALE = 5;
  private static final String TEST_CALC_MEMBER_FORMAT_CATEGORY = "Currency";
  private static final String TEST_CALC_MEMBER_FORMAT_STRING = "$#.##";

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();

    updateCalculatedMemberAnnotation.setCube( TEST_CUBE_NAME );
    updateCalculatedMemberAnnotation.setCaption( TEST_CALC_MEMBER_CAPTION );
    updateCalculatedMemberAnnotation.setFormula( TEST_CALC_MEMBER_FORMULA );
    updateCalculatedMemberAnnotation.setName( TEST_CALC_MEMBER_NAME );
    updateCalculatedMemberAnnotation.setSourceCalculatedMeasure( TEST_CALC_MEMBER_SOURCE_NAME );
    updateCalculatedMemberAnnotation.setDimension( TEST_CALC_MEMBER_DIMENSION );
    updateCalculatedMemberAnnotation.setDescription( TEST_CALC_MEMBER_DESCRIPTION );
    updateCalculatedMemberAnnotation.setDecimalPlaces( TEST_CALC_MEMBER_FORMAT_SCALE );
    updateCalculatedMemberAnnotation.setFormatCategory( TEST_CALC_MEMBER_FORMAT_CATEGORY );
    updateCalculatedMemberAnnotation.setFormatString( TEST_CALC_MEMBER_FORMAT_STRING );
    updateCalculatedMemberAnnotation.setInline( true );
    updateCalculatedMemberAnnotation.setCalculateSubtotals( true );
  }

  @Test
  public void testApplyMondrian() throws Exception {


    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
      DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse( mondrianSchemaXmlFile );

    boolean result = updateCalculatedMemberAnnotation.apply( mondrianSchemaXmlDoc );

    Element measureNode =
      AnnotationUtil.getCalculatedMemberNode( mondrianSchemaXmlDoc, TEST_CUBE_NAME, TEST_CALC_MEMBER_NAME );

    assertEquals(
      TEST_CALC_MEMBER_NAME,
      measureNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_NAME_ATTRIBUTE ).getNodeValue() );
    assertEquals(
      TEST_CALC_MEMBER_CAPTION,
      measureNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_CAPTION_ATTRIBUTE ).getNodeValue() );
    assertEquals(
      TEST_CALC_MEMBER_FORMULA,
      measureNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_FORMULA_ATTRIBUTE ).getNodeValue() );
    assertEquals(
      TEST_CALC_MEMBER_DIMENSION,
      measureNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_DIMENSION_ATTRIBUTE ).getNodeValue() );
    assertEquals(
      TEST_CALC_MEMBER_DESCRIPTION,
      measureNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_DESCRIPTION_ATTRIBUTE ).getNodeValue() );

    assertTrue( result );

    testAnnotations( measureNode );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testApplyMetadata() throws Exception {
    ModelerWorkspace modelerWorkspace =
      new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    modelerWorkspace.setDomain( new XmiParser().parseXmi( new FileInputStream( METADATA_TEST_FILE_PATH ) ) );
    modelerWorkspace.getWorkspaceHelper().populateDomain( modelerWorkspace );

    updateCalculatedMemberAnnotation.apply( modelerWorkspace, metaStore );
  }

  @Test
  public void testValidate() throws Exception {
    updateCalculatedMemberAnnotation.validate();
  }

  @Test( expected = ModelerException.class )
  public void testValidateWithoutCube() throws Exception {
    updateCalculatedMemberAnnotation.setCube( "" );
    updateCalculatedMemberAnnotation.validate();
  }

  @Test( expected = ModelerException.class )
  public void testValidateWithoutName() throws Exception {
    updateCalculatedMemberAnnotation.setName( "" );
    updateCalculatedMemberAnnotation.validate();
  }

  private void testAnnotations( Element measureNode ) {
    // Test annotations
    NodeList annotationsNodes = measureNode.getElementsByTagName( AnnotationConstants.ANNOTATIONS_NODE_NAME );
    if ( annotationsNodes.getLength() <= 0 ) {
      fail( AnnotationConstants.NO_ANNOTATIONS_FOUND_MESSAGE );
    }

    // assume the first element is the only annotations node, as per the spec
    Element annotationsNode = (Element) annotationsNodes.item( 0 );
    NodeList annotationNodes = annotationsNode.getElementsByTagName( AnnotationConstants.ANNOTATION_NODE_NAME );
    for ( int i = 0; i <= annotationNodes.getLength() - 1; i++ ) {
      Node annotationNode = annotationNodes.item( i );
      switch ( annotationNode.getAttributes().getNamedItem( AnnotationConstants.CALCULATED_MEMBER_NAME_ATTRIBUTE ).getTextContent() ) {
        case AnnotationConstants.INLINE_ANNOTATION_FORMAT_CATEGORY:
          Assert.assertTrue( annotationNode.getTextContent().equals( TEST_CALC_MEMBER_FORMAT_CATEGORY ) );
          break;
        case AnnotationConstants.INLINE_ANNOTATION_FORMAT_SCALE:
          Assert.assertTrue( annotationNode.getTextContent().equals( String.valueOf( TEST_CALC_MEMBER_FORMAT_SCALE ) ) );
          break;
        case AnnotationConstants.INLINE_ANNOTATION_FORMULA_EXPRESSION:
          Assert.assertTrue( annotationNode.getTextContent().equals( TEST_CALC_MEMBER_FORMULA ) );
          break;
        case AnnotationConstants.INLINE_ANNOTATION_CREATED_INLINE:
          annotationNode.setNodeValue( Boolean.FALSE.toString() );
          break;
        case AnnotationConstants.INLINE_ANNOTATION_CALCULATE_SUBTOTALS:
          annotationNode.setNodeValue( Boolean.FALSE.toString() );
          break;
      }
    }
  }
}
