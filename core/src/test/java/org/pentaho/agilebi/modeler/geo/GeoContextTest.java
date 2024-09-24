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

package org.pentaho.agilebi.modeler.geo;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.IAvailableItem;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.DataType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeoContextTest {
  private static Properties props = null;
  private static final String LOCALE = "en_US";
  protected ModelerWorkspace workspace;

  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "src/test/resources/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
  }

  @Before
  public void setUp() throws Exception {

    if ( ModelerMessagesHolder.getMessages() == null ) {
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    }

    Reader propsReader = new FileReader( new File( "src/test/resources/geoRoles.properties" ) );
    Properties props = new Properties();
    props.load( propsReader );
    GeoContextConfigProvider config = new GeoContextPropertiesProvider( props );
    GeoContext geo = GeoContextFactory.create( config );

    workspace = new ModelerWorkspace( new ModelerWorkspaceHelper( LOCALE ), geo );
  }

  @Test
  public void addGeoRole() {
    GeoContext geo = new GeoContext();
    assertEquals( 0, geo.size() );
    geo.addGeoRole( new GeoRole( "country", Arrays.asList( new String[] { "country, ctry" } ) ) );
    geo.addGeoRole( new GeoRole( "state", Arrays.asList( new String[] { "state, st, province" } ) ) );
    geo.addGeoRole( new GeoRole( "city", Arrays.asList( new String[] { "city, town" } ) ) );
    assertEquals( 3, geo.size() );

    // make sure they are in the same order as entered
    assertEquals( "country", geo.getGeoRole( 0 ).getName() );
    assertEquals( "state", geo.getGeoRole( 1 ).getName() );
    assertEquals( "city", geo.getGeoRole( 2 ).getName() );
  }

  @Test
  public void testMatchingAliasToRole_prefixedPhysicalColumn() throws Exception {

    GeoContext geo = GeoContextFactory.create( config );

    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    when( mockStateCol.getId() ).thenReturn( "pc__STATE" );
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );

    IPhysicalColumn mockLatCol = mock( IPhysicalColumn.class );
    when( mockLatCol.getId() ).thenReturn( "pc__CUSTOMER_LATITUDE" );
    when( mockLatCol.getName( LOCALE ) ).thenReturn( "Customer_Latitude" );
    when( mockLatCol.getName( "en-US" ) ).thenReturn( "Customer_Latitude" );

    AvailableField field = new AvailableField( mockStateCol );

    GeoRole geoRole = geo.matchFieldToGeoRole( field );
    assertNotNull( geoRole );
    assertEquals( "state", geoRole.getName() );

    field = new AvailableField( mockLatCol );

    geoRole = geo.matchFieldToGeoRole( field );
    assertNotNull( geoRole );
    assertEquals( "location", geoRole.getName() );

  }

  @Test
  public void testDetermineLocationField() throws Exception {

    List<IAvailableItem> items = new ArrayList<>();

    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "Customer" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "Customer" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMER" );

    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Customer Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Customer Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "CUSTOMER_LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Customer_Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Customer Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Customer Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "CUSTOMER_LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Customer_Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );

    GeoContext geo = GeoContextFactory.create( config );
    LocationRole locationRole = (LocationRole) geo.getLocationRole().clone();
    locationRole.getLatitudeRole().eval( "customer_latitude", "latitude" );
    locationRole.getLongitudeRole().eval( "customer_longitude", "longitude" );

    AvailableField field =
      geo.determineLocationField( table, locationRole, 2, 3, workspace.getWorkspaceHelper().getLocale() );
    assertEquals( "Customer", field.getName() );

  }

  @Test
  public void testDetermineLocationField_NoPrefixMatch() throws Exception {

    List<IAvailableItem> items = new ArrayList<>();

    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Customer_Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Customer_Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "CUSTOMER_LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Customer_Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Customer_Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Customer_Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "CUSTOMER_LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Customer_Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );

    GeoContext geo = GeoContextFactory.create( config );
    AvailableField field =
      geo.determineLocationField( table, geo.getLocationRole(), 2, 3, workspace.getWorkspaceHelper().getLocale() );
    assertEquals( "State", field.getName() );
  }

  @Ignore
  // currently don't support more than one lat/long pair in any one table
  @Test
  public void testDetermineLocationField_MultipleLatLongFields() throws Exception {

    List<IAvailableItem> items = new ArrayList<>();

    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockStreetCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol2 = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol2 = mock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockStreetCol );
    cols1.add( mockLatitudeCol2 );
    cols1.add( mockLongitudeCol2 );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "Customer" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "Customer" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMER" );

    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Customer_Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Customer_Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "CUSTOMER_LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Customer_Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Customer_Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Customer_Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "CUSTOMER_LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Customer_Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

    when( mockStreetCol.getName( LOCALE ) ).thenReturn( "Street" );
    when( mockStreetCol.getName( "en-US" ) ).thenReturn( "Street" );
    when( mockStreetCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStreetCol.getId() ).thenReturn( "STREET" );

    when( mockLatitudeCol2.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol2.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol2.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol2.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol2.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol2.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol2.getId() ).thenReturn( "LATITUDE" );

    latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol2.getProperties() ).thenReturn( latProperties );

    when( mockLongitudeCol2.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol2.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol2.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol2.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol2.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol2.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol2.getId() ).thenReturn( "LONGITUDE" );

    lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol2.getProperties() ).thenReturn( lngProperties );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );

    GeoContext geo = GeoContextFactory.create( config );
    AvailableField field =
      geo.determineLocationField( table, geo.getLocationRole(), 2, 3, workspace.getWorkspaceHelper().getLocale() );
    assertEquals( "Customer", field.getName() );

  }
}
