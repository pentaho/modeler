package org.pentaho.agilebi.modeler.models.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rowell Belen
 */
public class AnnotationTypeTest {

  @Test
  public void testDescribe() {

    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( AggregationType.AVERAGE );
    createMeasure.setFormatString( "SSSS" );
    createMeasure.setName( "avg" );
    createMeasure.setDescription( null );

    assertTrue( createMeasure.describe().containsKey( "name" ) );
    assertFalse( createMeasure.describe().containsKey( "description" ) ); // null
    assertEquals( createMeasure.describe().get( "aggregateType" ), AggregationType.AVERAGE );
  }

  @Test
  public void testPopulate() {

    Map<String, Serializable> properties = new HashMap<String, Serializable>();
    properties.put( null, null );
    properties.put( "name", "NameTest" );
    properties.put( "localizedName", 12 ); // type doesn't match, not saved
    properties.put( "geoType", ModelAnnotation.GeoType.Continent );
    properties.put( "hidden", "yes" );
    properties.put( "timeType", "TimeDays" ); // auto-convert

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.populate( properties );

    assertEquals( createAttribute.getName(), "NameTest" );
    assertNotNull( createAttribute.getGeoType() );
    assertEquals( createAttribute.getTimeType(), ModelAnnotation.TimeType.TimeDays );
    assertEquals( createAttribute.getGeoType(), ModelAnnotation.GeoType.Continent );

    // Test Measure
    properties = new HashMap<String, Serializable>();
    properties.put( "aggregateType", "MAXIMUM" );
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.populate( properties );
    assertEquals( createMeasure.getAggregateType(), AggregationType.MAXIMUM );
  }

  @Test
  public void testConvertAndAssign() {

    Map<String, Serializable> properties = new HashMap<String, Serializable>();
    properties.put( "i", "11" );
    properties.put( "d", 11.11 );
    properties.put( "f", "22.22" );
    properties.put( "l", Long.MAX_VALUE + "" );
    properties.put( "s", Short.MAX_VALUE );

    MockAnnotationType mockAnnotationType = new MockAnnotationType();
    mockAnnotationType.populate( properties );

    assertEquals( mockAnnotationType.getI(), 11 );
    assertEquals( mockAnnotationType.getD(), 11.11D, 0 );
    assertEquals( mockAnnotationType.getF(), 22.22F, 0 );
    assertEquals( mockAnnotationType.getL(), Long.MAX_VALUE );
    assertEquals( mockAnnotationType.getS(), Short.MAX_VALUE );
  }

  @Test
  public void testIterateProperties() {

    Map<String, Serializable> properties = new HashMap<String, Serializable>();
    properties.put( null, null );
    properties.put( "name", "NameTest" );
    properties.put( "localizedName", 12 ); // type doesn't match, not saved
    properties.put( "geoType", ModelAnnotation.GeoType.Continent );
    properties.put( "hidden", "yes" );
    properties.put( "timeFormat", null );

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.populate( properties );

    final List<String> keyList = new ArrayList<String>();
    final List<Serializable> valueList = new ArrayList<Serializable>();
    createAttribute.iterateProperties( new KeyValueClosure() {
      @Override public void execute( String key, Serializable serializable ) {
        keyList.add( key );
        valueList.add( serializable );
      }
    } );

    // found hidden and name
    assertEquals( 3, keyList.size() );
    assertEquals( 3, valueList.size() );
  }

  @Test
  public void testGetModelPropertyValueByName() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setModelPropertyByName( "Time Forma...", "xxxxx" );
    assertNull( createAttribute.getModelPropertyValueByName( "Time Forma..." ) ); // doesn't exist

    createAttribute.setModelPropertyByName( "Time Source Format", "xxxxx" );
    assertTrue( createAttribute.getTimeFormat().equalsIgnoreCase( "xxxxx" ) );

    createAttribute.setModelPropertyByName( "Time Level Type", ModelAnnotation.TimeType.TimeHalfYears );
    assertTrue( createAttribute.getTimeType().equals( ModelAnnotation.TimeType.TimeHalfYears ) );

    createAttribute.setModelPropertyByName( "Geo Type", ModelAnnotation.GeoType.Continent.toString() );
    // assertTrue( createAttribute.getGeoType().equals( ModelAnnotation.GeoType.Continent ) );
  }

  @Test
  public void testGetModelPropertyNameClassType() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();

    assertEquals( createAttribute.getModelPropertyNameClassType( "Time Source Forma.." ), null );
    assertEquals( createAttribute.getModelPropertyNameClassType( "Time Source Format" ), String.class );
    assertEquals( createAttribute.getModelPropertyNameClassType( "Time Level Type" ), ModelAnnotation.TimeType.class );
    // assertEquals( createAttribute.getModelPropertyNameClassType( "Geo Type" ), ModelAnnotation.GeoType.class );

    CreateMeasure createMeasure = new CreateMeasure();
    assertEquals( createMeasure.getModelPropertyNameClassType( "Aggregation Type" ), AggregationType.class );
  }
}
