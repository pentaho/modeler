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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationTest {

  private IMetaStore metaStore;

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @Test
  public void testMeasure() {

    CreateMeasure createMeasure = new CreateMeasure();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<CreateMeasure>( createMeasure ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getType(), ModelAnnotation.Type.CREATE_MEASURE );
    assertEquals( md.getType().description(), "Create Measure" );
  }

  @Test
  public void testAttribute() {

    CreateAttribute createAttribute = new CreateAttribute();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<CreateAttribute>( createAttribute ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getType(), ModelAnnotation.Type.CREATE_ATTRIBUTE );
    assertEquals( md.getType().description(), "Create Attribute" );
  }

  @Test
  public void testGetAndFilter() {

    List<ModelAnnotation<?>> annotations = new ArrayList<ModelAnnotation<?>>();
    annotations.add( new ModelAnnotation<CreateMeasure>( new CreateMeasure() ) );
    annotations.add( new ModelAnnotation<CreateAttribute>( new CreateAttribute() ) );
    annotations.add( new ModelAnnotation<CreateMeasure>( new CreateMeasure() ) );

    assertEquals( ModelAnnotation.getMeasures( annotations ).size(), 2 );
    assertEquals( ModelAnnotation.getAttributes( annotations ).size(), 1 );
  }

  @Test
  public void testAppliesAllAnnotationsToWorkspace() throws Exception {
    final ModelerWorkspace modelerWorkspace = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    AnnotationType annotationType = new AnnotationType() {
      @Override
      public boolean apply( final ModelerWorkspace workspace, final IMetaStore metaStore ) {
        assertSame( workspace, modelerWorkspace );
        return true;
      }

      @Override public ModelAnnotation.Type getType() {
        return null;
      }

      @Override public String getSummary() {
        return "";
      }

      @Override
      public boolean apply( Document schema ) throws ModelerException {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public String getName() {
        return "";
      }

      @Override
      public void validate() throws ModelerException { }

      @Override
      public String getField() {
        return "";
      }
    };
    ModelAnnotation<AnnotationType> modelAnnotation = new ModelAnnotation<AnnotationType>();
    modelAnnotation.setAnnotation( annotationType );
    modelAnnotation.apply( modelerWorkspace, metaStore );
  }

  @Test
  public void testDimensionKeyOnlyAppliesToSharedDimensions() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    modelAnnotations.setSharedDimension( true );
    assertTrue(
        ModelAnnotation.Type.CREATE_DIMENSION_KEY
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    assertFalse(
        ModelAnnotation.Type.CREATE_DIMENSION_KEY
            .isApplicable( new ModelAnnotationGroup(), new ModelAnnotation(), new ValueMetaInteger() ) );
  }

  @Test
  public void testDimensionKeyOnlyAppliesOncePerGroup() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    modelAnnotations.setSharedDimension( true );
    assertTrue(
        ModelAnnotation.Type.CREATE_DIMENSION_KEY
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    modelAnnotations.add( new ModelAnnotation<CreateDimensionKey>( new CreateDimensionKey() ) );
    assertFalse(
        ModelAnnotation.Type.CREATE_DIMENSION_KEY
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
  }

  @Test
  public void testAttributeAlwaysApplicable() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    modelAnnotations.setDataProviders( Arrays.asList( new DataProvider() ) );
    assertTrue(
        ModelAnnotation.Type.CREATE_ATTRIBUTE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    modelAnnotations.add( new ModelAnnotation<CreateDimensionKey>( new CreateDimensionKey() ) );
    assertTrue(
        ModelAnnotation.Type.CREATE_ATTRIBUTE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
  }

  @Test
  public void testMeasureNotApplicableToSharedDimension() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    assertTrue(
        ModelAnnotation.Type.CREATE_MEASURE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    modelAnnotations.setSharedDimension( true );
    assertFalse(
        ModelAnnotation.Type.CREATE_MEASURE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );


    modelAnnotations.setSharedDimension( false );
    assertTrue(
        ModelAnnotation.Type.CREATE_MEASURE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    assertTrue(
        ModelAnnotation.Type.CREATE_MEASURE
            .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
  }

  @Test
  public void testLinkDimensionNotApplicableToSharedDimension() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    assertTrue( ModelAnnotation.Type.LINK_DIMENSION
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
    modelAnnotations.setSharedDimension( true );
    assertFalse( ModelAnnotation.Type.LINK_DIMENSION
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaInteger() ) );
  }

  @Test
  public void testTheInlineAnnotationsAreNeverApplicable() throws Exception {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    modelAnnotations.setDataProviders( Arrays.asList( new DataProvider() ) );
    assertFalse(
        ModelAnnotation.Type.UPDATE_MEASURE
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
        ModelAnnotation.Type.REMOVE_MEASURE
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
        ModelAnnotation.Type.REMOVE_ATTRIBUTE
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
        ModelAnnotation.Type.CREATE_CALCULATED_MEMBER
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
      ModelAnnotation.Type.SHOW_HIDE_ATTRIBUTE
      .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
      ModelAnnotation.Type.SHOW_HIDE_MEASURE
        .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() ) );
    assertFalse(
      ModelAnnotation.Type.UPDATE_ATTRIBUTE
      .isApplicable( modelAnnotations, new ModelAnnotation(), new ValueMetaString() )
    );
  }
}
