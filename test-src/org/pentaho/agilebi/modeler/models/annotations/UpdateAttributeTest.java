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

public class UpdateAttributeTest {
  private static final String MONDRIAN_TEST_FILE_PATH = "test-res/updateattribute.mondrian.xml";

  @Test
  public void testSummaryDescribesExistingAttribute() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setName( "changedName" );
    updateAttribute.setCube( "existingCube" );
    updateAttribute.setDimension( "existingDim" );
    updateAttribute.setHierarchy( "existingHierarchy" );
    updateAttribute.setLevel( "existingLevel" );

    assertEquals(
      "Update properties for attribute existingLevel in hierarchy existingHierarchy, dimension existingDim and cube "
        + "existingCube",
      updateAttribute.getSummary() );
  }

  @Test
  public void testRenameInvalidNodeIsFalse() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setCube( "sales" );
    updateAttribute.setDimension( "Time2" );
    updateAttribute.setHierarchy( "Time" );
    updateAttribute.setLevel( "Month" );
    updateAttribute.setName( "Month by Year" );
    Document mondrianDoc = AnnotationUtil.getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    assertFalse( updateAttribute.apply( mondrianDoc ) );
  }

  @Test
  public void testRenamesLevel() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setCube( "sales" );
    updateAttribute.setDimension( "Time" );
    updateAttribute.setHierarchy( "Time" );
    updateAttribute.setLevel( "Month" );
    updateAttribute.setName( "Month by Year" );
    Document mondrianDoc = AnnotationUtil.getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    assertTrue( updateAttribute.apply( mondrianDoc ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianDoc, "Level", "Month by Year", "", "" ) );
  }

  @Test
  public void testRenamesLevelInSharedDimension() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setCube( "sales" );
    updateAttribute.setDimension( "Product" );
    updateAttribute.setHierarchy( "two" );
    updateAttribute.setLevel( "Product Code" );
    updateAttribute.setName( "Code" );
    Document mondrianDoc = AnnotationUtil.getMondrianDoc( MONDRIAN_TEST_FILE_PATH );
    assertTrue( updateAttribute.apply( mondrianDoc ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianDoc, "Level", "Code", "column", "code" ) );
  }

  @Test
  public void testCubeNameIsRequired() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setName( "aLevel" );
    updateAttribute.setDimension( "aDimension" );
    updateAttribute.setHierarchy( "aHierarchy" );
    try {
      updateAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Cube Name is required", e.getMessage() );
    }
  }

  @Test
  public void testDimensionNameIsRequired() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setName( "aLevel" );
    updateAttribute.setCube( "aCube" );
    updateAttribute.setHierarchy( "aHierarchy" );
    try {
      updateAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Dimension Name is required", e.getMessage() );
    }
  }

  @Test
  public void testHierarchyNameIsRequired() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setName( "aLevel" );
    updateAttribute.setDimension( "aDimension" );
    updateAttribute.setCube( "aCube" );
    try {
      updateAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Hierarchy Name is required", e.getMessage() );
    }
  }

  @Test
  public void testLevelNameIsRequired() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setHierarchy( "aHierarchy" );
    updateAttribute.setDimension( "aDimension" );
    updateAttribute.setCube( "aCube" );
    try {
      updateAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Level Name is required", e.getMessage() );
    }
  }

  @Test
  public void testNameIsRequired() throws Exception {
    UpdateAttribute updateAttribute = new UpdateAttribute();
    updateAttribute.setLevel( "aLevel" );
    updateAttribute.setHierarchy( "aHierarchy" );
    updateAttribute.setDimension( "aDimension" );
    updateAttribute.setCube( "aCube" );
    try {
      updateAttribute.validate();
      fail( "should have got exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Name is required", e.getMessage() );
    }
  }
}
