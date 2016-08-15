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

import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.w3c.dom.Document;

import static org.junit.Assert.*;
import static org.pentaho.agilebi.modeler.models.annotations.AnnotationUtil.*;

public class ShowHideAttributeTest {
  private static final String MONDRIAN_TEST_FILE_PATH = "src/test/resources/hideshow.mondrian.xml";

  @Test
  public void testSummary() throws Exception {
    ShowHideAttribute showHideAttribute = new ShowHideAttribute();
    showHideAttribute.setCube( "Warehouse" );
    showHideAttribute.setDimension( "Location" );
    showHideAttribute.setHierarchy( "Geo" );
    showHideAttribute.setName( "City" );
    assertEquals( "Hide attribute City in hierarchy Geo, dimension Location and cube Warehouse",
      showHideAttribute.getSummary() );
    showHideAttribute.setVisible( true );
    assertEquals( "Show attribute City in hierarchy Geo, dimension Location and cube Warehouse",
      showHideAttribute.getSummary() );
  }

  @Test
  public void testHidesInlineAttribute() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Customer" );
    hideAttribute.setHierarchy( "cust" );
    hideAttribute.setName( "Gender" );
    assertTrue( hideAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Gender", "visible", "false" ) );
  }

  @Test
  public void testHidesInlineAttributeWithDefaultHierarchyName() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Customer" );
    hideAttribute.setHierarchy( "Customer" );
    hideAttribute.setName( "Gender2" );
    assertTrue( hideAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Gender2", "visible", "false" ) );
  }

  @Test
  public void testHidesSharedAttribute() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Product" );
    hideAttribute.setHierarchy( "By Line" );
    hideAttribute.setName( "Product Line" );
    assertTrue( hideAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Product Line", "visible", "false" ) );
  }

  @Test
  public void testHidesSharedAttributeWithDefaultHierarchyName() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Product" );
    hideAttribute.setHierarchy( "Product" );
    hideAttribute.setName( "Product Category" );
    assertTrue( hideAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Product Category", "visible", "false" ) );
  }

  @Test
  public void testAttributeNotFoundIsFalse() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Product" );
    hideAttribute.setHierarchy( "NotFoundHierarchy" );
    hideAttribute.setName( "Product Category" );
    assertFalse( hideAttribute.apply( mondrianDoc ) );
    assertFalse( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Product Category", "visible", "false" ) );
  }

  @Test
  public void testCubeNameIsRequired() throws Exception {
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setName( "aLevel" );
    hideAttribute.setDimension( "aDimension" );
    hideAttribute.setHierarchy( "aHierarchy" );
    try {
      hideAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Cube Name is required", e.getMessage() );
    }
  }

  @Test
  public void testDimensionNameIsRequired() throws Exception {
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setName( "aLevel" );
    hideAttribute.setCube( "aCube" );
    hideAttribute.setHierarchy( "aHierarchy" );
    try {
      hideAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Dimension Name is required", e.getMessage() );
    }
  }

  @Test
  public void testHierarchyNameIsRequired() throws Exception {
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setName( "aLevel" );
    hideAttribute.setDimension( "aDimension" );
    hideAttribute.setCube( "aCube" );
    try {
      hideAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Hierarchy Name is required", e.getMessage() );
    }
  }

  @Test
  public void testLevelNameIsRequired() throws Exception {
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setHierarchy( "aHierarchy" );
    hideAttribute.setDimension( "aDimension" );
    hideAttribute.setCube( "aCube" );
    try {
      hideAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Level Name is required", e.getMessage() );
    }
  }

  @Test
  public void testHidesLevelInHierarchyWithBlankName() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute hideAttribute = new ShowHideAttribute();
    hideAttribute.setCube( "products" );
    hideAttribute.setDimension( "Location" );
    hideAttribute.setHierarchy( "Location" );
    hideAttribute.setName( "State" );
    assertTrue( hideAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "State", "visible", "false" ) );
  }

  @Test
  public void testShowsAttribute() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideAttribute showAttribute = new ShowHideAttribute();
    showAttribute.setCube( "products" );
    showAttribute.setDimension( "Product" );
    showAttribute.setHierarchy( "By Line" );
    showAttribute.setName( "Product Line" );
    showAttribute.setVisible( true );
    assertTrue( showAttribute.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, LEVEL_ELEMENT_NAME, "Product Line", "visible", "true" ) );
  }
}
