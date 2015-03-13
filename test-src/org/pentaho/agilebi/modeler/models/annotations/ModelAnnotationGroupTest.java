/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.Arrays;

public class ModelAnnotationGroupTest {

  @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
  @Test
  public void testHasAVarArgsConstructor() throws Exception {
    ModelAnnotation<CreateMeasure> measure = new ModelAnnotation<CreateMeasure>();
    ModelAnnotation<CreateAttribute> attribute = new ModelAnnotation<CreateAttribute>();
    ModelAnnotationGroup annotationGroup = new ModelAnnotationGroup( measure, attribute );
    annotationGroup.setId( "cleverIdentifier" );
    assertEquals( 2, annotationGroup.size() );
    assertSame( measure, annotationGroup.get( 0 ) );
    assertSame( attribute, annotationGroup.get( 1 ) );
    assertEquals( "cleverIdentifier", annotationGroup.getId() );
  }

  @Test
  public void testEquals() throws Exception {

    // test list objects
    ModelAnnotationGroup modelAnnotationGroup = getSampleModelAnnotationGroup();
    ModelAnnotationGroup modelAnnotationGroupCopy = getSampleModelAnnotationGroup();

    modelAnnotationGroupCopy.get( 0 ).getAnnotation().setModelPropertyValueById( "name", "modified" );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.get( 0 ).getAnnotation().setModelPropertyValueById( "name", "modified" );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroupCopy.getDataProviders().get( 0 ).setDatabaseMetaNameRef( "newRef" );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.getDataProviders().get( 0 ).setDatabaseMetaNameRef( "newRef" );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroupCopy.getDataProviders().get( 0 ).getColumnMappings().get( 0 )
        .setColumnDataType( DataType.BINARY );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.getDataProviders().get( 0 ).getColumnMappings().get( 0 ).setColumnDataType( DataType.BINARY );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    assertFalse( modelAnnotationGroup.equals( null ) );
    assertFalse( modelAnnotationGroup.equals( new ModelAnnotationGroup() ) );
  }

  private ModelAnnotationGroup getSampleModelAnnotationGroup() {

    ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.setName( "sample" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.SUM );

    CreateAttribute ca = new CreateAttribute();
    ca.setTimeType( ModelAnnotation.TimeType.TimeDays );

    ModelAnnotation ma1 = new ModelAnnotation( "f1", cm );
    ma1.setName( "ma1" );

    ModelAnnotation ma2 = new ModelAnnotation( "f2", ca );
    ma2.setName( "ma2" );

    modelAnnotationGroup.add( ma1 );
    modelAnnotationGroup.add( ma2 );

    DataProvider dataProvider1 = new DataProvider();
    dataProvider1.setName( "dbp1" );
    dataProvider1.setDatabaseMetaNameRef( "ref1" );
    dataProvider1.setColumnMappings( Arrays.asList( new ColumnMapping[] { new ColumnMapping() } ) );

    DataProvider dataProvider2 = new DataProvider();
    dataProvider2.setName( "dbp2" );
    dataProvider2.setDatabaseMetaNameRef( "ref2" );
    dataProvider2.setColumnMappings( Arrays.asList( new ColumnMapping[] { new ColumnMapping() } ) );

    modelAnnotationGroup.setDataProviders( Arrays.asList( new DataProvider[] { dataProvider1, dataProvider2 } ) );

    modelAnnotationGroup.setSharedDimension( true );

    return modelAnnotationGroup;
  }
}
