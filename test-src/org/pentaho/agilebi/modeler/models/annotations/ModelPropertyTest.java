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
import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelPropertyTest {

  @Test
  public void testMeasure() throws Exception {

    Measure measure = new Measure();

    List<String> propertyNames = measure.getModelPropertyNames();
    assertEquals( propertyNames.size(), 7 );

    measure.setModelPropertyByName( "Display Name", "A" );
    measure.setModelPropertyByName( "Description", "B" );
    measure.setModelPropertyByName( "Unique Members", "true" ); // auto converted
    measure.setModelPropertyByName( "Localized Name", "C" );
    measure.setModelPropertyByName( "Format String", "D" );
    measure.setModelPropertyByName( "Hidden", true );
    measure.setModelPropertyByName( "Aggregate Type", AggregationType.COUNT );
    measure.setModelPropertyByName( "XXX", "Does not exist" ); // should not fail

    assertEquals( measure.getName(), "A" );
    assertEquals( measure.getDescription(), "B" );
    assertEquals( measure.getLocalizedName(), "C" );
    assertEquals( measure.getFormatString(), "D" );
    assertEquals( measure.isUniqueMembers(), true );
    assertEquals( measure.isHidden(), true );
    assertEquals( measure.getAggregateType(), AggregationType.COUNT );

    measure.setModelPropertyByName( "Unique Members", true );
    assertEquals( measure.isUniqueMembers(), true );
  }

  @Test
  public void testAttribute() throws Exception {

    Attribute attribute = new Attribute();
    GeoRole geoRole = new GeoRole();

    attribute.setModelPropertyByName( "Level Type", ModelAnnotation.LevelType.Regular );
    attribute.setModelPropertyByName( "Attribute Type", ModelAnnotation.AttributeType.PROPERTY );
    attribute.setModelPropertyByName( "Format String", "B" );
    attribute.setModelPropertyByName( "Geo Role", geoRole );

    assertEquals( attribute.getLevelType(), ModelAnnotation.LevelType.Regular );
    assertEquals( attribute.getAttributeType(), ModelAnnotation.AttributeType.PROPERTY );
    assertEquals( attribute.getFormatString(), "B" );
    assertEquals( attribute.getGeoRole(), geoRole );
  }

  @Test
  public void testDimension() throws Exception {
    Dimension dimension = new Dimension();
    dimension.setModelPropertyByName( "Type", ModelAnnotation.LevelType.Regular );
    assertEquals( dimension.getLevelType(), ModelAnnotation.LevelType.Regular );
  }

  @Test
  public void testHeirarchyLevel() throws Exception {
    HierarchyLevel parent = new HierarchyLevel();
    HierarchyLevel hierarchyLevel = new HierarchyLevel();

    hierarchyLevel.setModelPropertyByName( "Level Ordinal", 0 );
    hierarchyLevel.setModelPropertyByName( "Unique Members", true );
    hierarchyLevel.setModelPropertyByName( "Parent", parent );

    assertEquals( hierarchyLevel.getLevelOrdinal(), 0 );
    assertEquals( hierarchyLevel.isUniqueMembers(), true );
    assertEquals( hierarchyLevel.getParent(), parent );
    assertEquals( hierarchyLevel.getModelPropertyNames().size(), 11 );
  }
}
