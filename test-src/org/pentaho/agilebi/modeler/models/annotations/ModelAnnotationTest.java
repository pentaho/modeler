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
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationTest {

  @Test
  public void testMeasure() {

    Measure measure = new Measure();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<Measure>( "testColumn", measure ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getColumn(), "testColumn" );
    assertTrue( md.isMeasure() );
    assertFalse( md.asAttribute() );
    assertFalse( md.isDimension() );
    assertFalse( md.isHierarchyLevel() );
  }

  @Test
  public void testAttribute() {

    Attribute attribute = new Attribute();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<Attribute>( "testColumn", attribute ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getColumn(), "testColumn" );
    assertFalse( md.isMeasure() );
    assertTrue( md.asAttribute() );
    assertFalse( md.isDimension() );
    assertFalse( md.isHierarchyLevel() );
  }

  @Test
  public void testDimension() {

    Dimension dimension = new Dimension();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<Dimension>( "testColumn", dimension ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getColumn(), "testColumn" );
    assertFalse( md.isMeasure() );
    assertFalse( md.asAttribute() );
    assertTrue( md.isDimension() );
    assertFalse( md.isHierarchyLevel() );
  }

  @Test
  public void testHierarchyLevel() {

    HierarchyLevel hierarchyLevel = new HierarchyLevel();

    List<ModelAnnotation<?>> list = new ArrayList<ModelAnnotation<?>>();
    list.add( new ModelAnnotation<HierarchyLevel>( "testColumn", hierarchyLevel ) );

    ModelAnnotation<?> md = list.get( 0 ); // don't know the type

    // check
    assertEquals( md.getColumn(), "testColumn" );
    assertFalse( md.isMeasure() );
    assertFalse( md.asAttribute() );
    assertFalse( md.isDimension() );
    assertTrue( md.isHierarchyLevel() );
  }

  @Test
  public void testGetAndFilter() {

    List<ModelAnnotation<?>> annotations = new ArrayList<ModelAnnotation<?>>();
    annotations.add( new ModelAnnotation<Measure>( "A", new Measure() ) );
    annotations.add( new ModelAnnotation<Attribute>( "B", new Attribute() ) );
    annotations.add( new ModelAnnotation<Dimension>( "C", new Dimension() ) );
    annotations.add( new ModelAnnotation<HierarchyLevel>( "D", new HierarchyLevel() ) );
    annotations.add( new ModelAnnotation<Measure>( "E", new Measure() ) );

    assertEquals( ModelAnnotation.getMeasures( annotations ).size(), 2 );
    assertEquals( ModelAnnotation.getAttributes( annotations ).size(), 1 );
    assertEquals( ModelAnnotation.getDimensions( annotations ).size(), 1 );
    assertEquals( ModelAnnotation.getHeirachyLevels( annotations ).size(), 1 );
  }
}
