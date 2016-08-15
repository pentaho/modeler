/*! ******************************************************************************
 *
 * Pentaho Community Edition Project: pentaho-modeler
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ********************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.pentaho.metadata.model.concept.types.AggregationType.MINIMUM;

import org.junit.Test;
import static org.mockito.Matchers.any;

import org.mockito.Mockito;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rowell Belen
 */
public class AnnotationTypeTest {
  private static String PRODUCT_XMI_FILE = "src/test/resources/products.xmi";

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
  public void testConvertAndAssign() throws Exception {

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

    // Test null/empty value
    properties = new HashMap<String, Serializable>();
    properties.put( "l", "" );
    properties.put( "s", null );
    mockAnnotationType = spy( new MockAnnotationType() );
    mockAnnotationType.populate( properties );
    verify( mockAnnotationType, times( 2 ) ).attemptAutoConvertAndAssign( any( Field.class ), any() );
    verify( mockAnnotationType, times( 0 ) ).getLogger(); // ignored
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
    assertEquals( 4, keyList.size() );
    assertEquals( 4, valueList.size() );
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

  @Test
  public void testResolveFieldFromLevel() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setHierarchy( "Products" );
    productLine.setLevel( "[PRODUCTLINE].[PRODUCTLINE]" );
    productLine.setCube( "products_38GA" );
    productLine.apply( model, metaStore );
    assertEquals( "PRODUCTLINE_OLAP", productLine.getField() );
  }

  @Test
  public void testResolveFieldFromMeasure() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( MINIMUM );
    createMeasure.setName( "Min Buy Price" );
    createMeasure.setFormatString( "##.##" );
    createMeasure.setMeasure( "[Measures].[bc_BUYPRICE]" );
    createMeasure.setCube( "products_38GA" );
    createMeasure.apply( model, metaStore );
    assertEquals( "bc_BUYPRICE", createMeasure.getField() );
  }

  @Test
  public void testAttemptAutoConvertAndAssignTimeType() throws Exception {

    CreateAttribute createAttribute = spy( new CreateAttribute() );

    createAttribute.setModelPropertyValueById( CreateAttribute.TIME_TYPE_ID, null );
    verify( createAttribute, times( 0 ) ).getLogger();

    createAttribute.setModelPropertyValueById( CreateAttribute.TIME_TYPE_ID, "" );
    verify( createAttribute, times( 0 ) ).getLogger(); // still no warning

    createAttribute.setModelPropertyValueById( CreateAttribute.TIME_TYPE_ID, "someInvalidValue" );
    verify( createAttribute, times( 1 ) ).getLogger(); // warning!
  }

  @Test
  public void testAttemptAutoConvertAndAssignGeoType() throws Exception {

    CreateAttribute createAttribute = spy( new CreateAttribute() );

    createAttribute.setModelPropertyValueById( CreateAttribute.GEO_TYPE_ID, null );
    verify( createAttribute, times( 0 ) ).getLogger();

    createAttribute.setModelPropertyValueById( CreateAttribute.GEO_TYPE_ID, "" );
    verify( createAttribute, times( 0 ) ).getLogger(); // still no warning

    createAttribute.setModelPropertyValueById( CreateAttribute.GEO_TYPE_ID, "someInvalidValue" );
    verify( createAttribute, times( 1 ) ).getLogger(); // warning!
  }

  @Test
  public void testEqualsLogically() throws Exception {
    AnnotationType left = mock( AnnotationType.class, Mockito.CALLS_REAL_METHODS );

    doReturn( "Hello, my name is..." ).when( left ).getName();

    AnnotationType right = mock( AnnotationType.class );
    doReturn( "Hello, my name is..." ).when( right ).getName();

    assertTrue( left.equalsLogically( right ) );
  }
  @Test
  public void testEqualsLogically_caseInsensitive() throws Exception {
    AnnotationType left = mock( AnnotationType.class, Mockito.CALLS_REAL_METHODS );

    doReturn( "Hello, my name is..." ).when( left ).getName();

    AnnotationType right = mock( AnnotationType.class );
    doReturn( "hello, my name IS..." ).when( right ).getName();

    assertTrue( left.equalsLogically( right ) );
  }
  @Test
  public void testEqualsLogically_fails() throws Exception {
    AnnotationType left = mock( AnnotationType.class, Mockito.CALLS_REAL_METHODS );

    doReturn( "Hello, my name is..." ).when( left ).getName();

    AnnotationType right = mock( AnnotationType.class );
    doReturn( "Slim Shady" ).when( right ).getName();

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void testEqualsLogically_failsDifferentTypes() throws Exception {
    AnnotationType left = mock( AnnotationType.class, Mockito.CALLS_REAL_METHODS );

    doReturn( "Hello, my name is..." ).when( left ).getName();

    AnnotationType right = new CreateMeasure();

    assertFalse( left.equalsLogically( right ) );
  }
}
