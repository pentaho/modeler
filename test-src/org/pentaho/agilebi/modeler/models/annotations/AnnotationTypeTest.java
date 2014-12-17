package org.pentaho.agilebi.modeler.models.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoRole;
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

    Measure measure = new Measure();
    measure.setAggregateType( AggregationType.AVERAGE );
    measure.setFormatString( "SSSS" );
    measure.setName( "avg" );
    measure.setDescription( "description" );
    measure.setLocalizedName( null );
    measure.setHidden( false );

    assertTrue( measure.describe().containsKey( "name" ) );
    assertFalse( measure.describe().containsKey( "localizedName" ) ); // null
    assertEquals( measure.describe().get( "aggregateType" ), AggregationType.AVERAGE );
  }

  @Test
  public void testPopulate() {

    Map<String, Serializable> properties = new HashMap<String, Serializable>();
    properties.put( null, null );
    properties.put( "name", "NameTest" );
    properties.put( "localizedName", 12 ); // type doesn't match, not saved
    properties.put( "geoType", ModelAnnotation.GeoType.Continent );
    properties.put( "hidden", "yes" );

    Attribute attribute = new Attribute();
    attribute.populate( properties );

    assertEquals( attribute.getName(), "NameTest" );
    assertNull( attribute.getLocalizedName() );
    assertNotNull( attribute.getGeoType() );
    assertTrue( attribute.isHidden() );

    properties.put( "hidden", "false" );
    attribute.populate( properties );
    assertFalse( attribute.isHidden() );
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

    Attribute attribute = new Attribute();
    attribute.populate( properties );

    final List<String> keyList = new ArrayList<String>();
    final List<Serializable> valueList = new ArrayList<Serializable>();
    attribute.iterateProperties( new KeyValueClosure() {
      @Override public void execute( String key, Serializable serializable ) {
        keyList.add( key );
        valueList.add( serializable );
      }
    } );

    assertEquals( 4, keyList.size() );
    assertEquals( 4, valueList.size() );
  }
}
