package org.pentaho.agilebi.modeler.models.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.io.Serializable;
import java.util.HashMap;
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
    properties.put( "geoRole", new GeoRole(  ) );

    Attribute attribute = new Attribute();
    attribute.populate( properties );

    assertEquals( attribute.getName(), "NameTest" );
    assertNull( attribute.getLocalizedName() );
    assertNotNull( attribute.getGeoRole() );
  }
}
