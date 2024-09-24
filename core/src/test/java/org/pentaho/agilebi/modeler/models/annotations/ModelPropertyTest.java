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

    CreateMeasure createMeasure = new CreateMeasure();

    List<String> propertyNames = createMeasure.getModelPropertyNames();
    assertEquals( propertyNames.size(), 9 );

    createMeasure.setModelPropertyByName( "Measure Name", "A" );
    createMeasure.setModelPropertyByName( "Description", "B" );
    createMeasure.setModelPropertyByName( "Unique Members", "true" ); // auto converted
    createMeasure.setModelPropertyByName( "Format", "D" );
    createMeasure.setModelPropertyByName( "Aggregation Type", AggregationType.COUNT );
    createMeasure.setModelPropertyByName( "Field Name", "F" );
    createMeasure.setModelPropertyByName( "Level Name", "G" );
    createMeasure.setModelPropertyByName( "Measure", "H" );
    createMeasure.setModelPropertyByName( "Cube Name", "I" );
    createMeasure.setModelPropertyByName( "XXX", "Does not exist" ); // should not fail

    assertEquals( createMeasure.getName(), "A" );
    assertEquals( createMeasure.getDescription(), "B" );
    assertEquals( createMeasure.getFormatString(), "D" );
    assertEquals( createMeasure.getAggregateType(), AggregationType.COUNT );

    createMeasure.setModelPropertyByName( "Unique Members", true );

    assertEquals( createMeasure.getField(), "F" );
    assertEquals( createMeasure.getLevel(), "G" );
    assertEquals( createMeasure.getMeasure(), "H" );
    assertEquals( createMeasure.getCube(), "I" );
  }

  @Test
  public void testAttribute() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();
    GeoRole geoRole = new GeoRole();

    createAttribute.setModelPropertyByName( "Time Level Type", ModelAnnotation.TimeType.TimeHours );
    createAttribute.setModelPropertyByName( "Time Source Format", "B" );
    createAttribute.setModelPropertyByName( "Geo Type", ModelAnnotation.GeoType.City );

    assertEquals( createAttribute.getTimeType(), ModelAnnotation.TimeType.TimeHours );
    assertEquals( createAttribute.getTimeFormat(), "B" );
    // assertEquals( createAttribute.getGeoType(), ModelAnnotation.GeoType.City );
  }
}
