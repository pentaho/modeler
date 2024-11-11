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
