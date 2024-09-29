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

public class ShowHideMeasureTest {
  private static final String MONDRIAN_TEST_FILE_PATH = "src/test/resources/hideshow.mondrian.xml";

  @Test
  public void testSummary() throws Exception {
    ShowHideMeasure showHideMeasure = new ShowHideMeasure();
    showHideMeasure.setName( "Price" );
    showHideMeasure.setCube( "Sales" );
    assertEquals( "Hide measure Price in cube Sales", showHideMeasure.getSummary() );
    showHideMeasure.setVisible( true );
    assertEquals( "Show measure Price in cube Sales", showHideMeasure.getSummary() );
  }

  @Test
  public void testHidesMeasureWithNoVisibilitySpecified() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "Price" );
    hideMeasure.setCube( "products" );
    assertTrue( hideMeasure.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, MEASURE_ELEMENT_NAME, "Price", "visible", "false" ) );
  }

  @Test
  public void testHidesMeasureWithExplicitVisible() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "visibleMeasure" );
    hideMeasure.setCube( "products" );
    assertTrue( hideMeasure.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, MEASURE_ELEMENT_NAME, "visibleMeasure", "visible", "false" ) );
  }

  @Test
  public void testHiddenMeasureStaysHidden() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "hiddenMeasure" );
    hideMeasure.setCube( "products" );
    assertTrue( hideMeasure.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, MEASURE_ELEMENT_NAME, "hiddenMeasure", "visible", "false" ) );
  }

  @Test
  public void testHideMeasureNotFoundIsFalse() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "measureNotFound" );
    hideMeasure.setCube( "products" );
    assertFalse( hideMeasure.apply( mondrianDoc ) );
    assertFalse( validateNodeAttribute( mondrianDoc, MEASURE_ELEMENT_NAME, "measureNotFound", "visible", "false" ) );
  }

  @Test
  public void testCubeNameIsRequired() throws Exception {
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "aMeasure" );
    try {
      hideMeasure.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Cube Name is required", e.getMessage() );
    }
  }

  @Test
  public void testMeasureNameIsRequired() throws Exception {
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setCube( "aCube" );
    try {
      hideMeasure.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Measure Name is required", e.getMessage() );
    }
  }

  @Test
  public void testHidesCalculatedMeasures() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure hideMeasure = new ShowHideMeasure();
    hideMeasure.setName( "Profit" );
    hideMeasure.setCube( "products" );
    assertTrue( hideMeasure.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, CALCULATED_MEMBER_ELEMENT_NAME, "Profit", "visible", "false" ) );
  }

  @Test
  public void testShowsMeasure() throws Exception {
    Document mondrianDoc = getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    ShowHideMeasure showMeasure = new ShowHideMeasure();
    showMeasure.setName( "hiddenMeasure" );
    showMeasure.setCube( "products" );
    showMeasure.setVisible( true );
    assertTrue( showMeasure.apply( mondrianDoc ) );
    assertTrue( validateNodeAttribute( mondrianDoc, MEASURE_ELEMENT_NAME, "hiddenMeasure", "visible", "true" ) );
  }
}
