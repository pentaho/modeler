/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

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

    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    expect( mockStateCol.getId() ).andReturn( "pc__STATE" ).anyTimes();
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();

    IPhysicalColumn mockLatCol = createMock( IPhysicalColumn.class );
    expect( mockLatCol.getId() ).andReturn( "pc__CUSTOMER_LATITUDE" ).anyTimes();
    expect( mockLatCol.getName( LOCALE ) ).andReturn( "Customer_Latitude" ).anyTimes();
    expect( mockLatCol.getName( "en-US" ) ).andReturn( "Customer_Latitude" ).anyTimes();

    replay( mockStateCol );
    replay( mockLatCol );

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

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "Customer" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "Customer" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMER" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Customer Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Customer Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "CUSTOMER_LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Customer_Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Customer Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Customer Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "CUSTOMER_LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Customer_Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockCustomerCol );
    replay( mockStateCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

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

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMERNAME" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Customer_Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Customer_Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "CUSTOMER_LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Customer_Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Customer_Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Customer_Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "CUSTOMER_LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Customer_Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockCustomerCol );
    replay( mockStateCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    GeoContext geo = GeoContextFactory.create( config );
    AvailableField field =
        geo.determineLocationField( table, geo.getLocationRole(), 2, 3, workspace.getWorkspaceHelper().getLocale() );
    assertEquals( "State", field.getName() );
  }

  @Ignore
  // currently don't support more than one lat/long pair in any one table
  @Test
  public void testDetermineLocationField_MultipleLatLongFields() throws Exception {

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockStreetCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol2 = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol2 = createMock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockStreetCol );
    cols1.add( mockLatitudeCol2 );
    cols1.add( mockLongitudeCol2 );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "Customer" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "Customer" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMER" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Customer_Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Customer_Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "CUSTOMER_LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Customer_Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Customer_Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Customer_Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "CUSTOMER_LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Customer_Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    // street col
    expect( mockStreetCol.getName( LOCALE ) ).andReturn( "Street" ).anyTimes();
    expect( mockStreetCol.getName( "en-US" ) ).andReturn( "Street" ).anyTimes();
    expect( mockStreetCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStreetCol.getId() ).andReturn( "STREET" ).anyTimes();

    // lat col2
    expect( mockLatitudeCol2.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol2.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol2.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol2.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol2.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol2.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol2.getId() ).andReturn( "LATITUDE" ).anyTimes();

    latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol2.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col2
    expect( mockLongitudeCol2.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol2.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol2.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol2.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol2.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol2.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol2.getId() ).andReturn( "LONGITUDE" ).anyTimes();

    lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol2.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockCustomerCol );
    replay( mockStateCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );
    replay( mockStreetCol );
    replay( mockLatitudeCol2 );
    replay( mockLongitudeCol2 );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    GeoContext geo = GeoContextFactory.create( config );
    AvailableField field =
        geo.determineLocationField( table, geo.getLocationRole(), 2, 3, workspace.getWorkspaceHelper().getLocale() );
    assertEquals( "Customer", field.getName() );

  }
}
