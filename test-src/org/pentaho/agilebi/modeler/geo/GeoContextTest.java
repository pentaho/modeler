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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.geo;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.DataRole;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.IAvailableItem;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.annotations.IDataRoleAnnotation;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/16/11 Time: 9:19 AM To change this template use File | Settings |
 * File Templates.
 */
public class GeoContextTest extends AbstractModelerTest {
  private static final String GEO_ROLE_KEY = "geo.roles";
  private static Properties props = null;
  private static final String LOCALE = "en_US";

  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "test-res/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
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
  public void testMatchingAliasesToRole() throws Exception {

    // pre-configure to use the CUSTOMERS table
    generateTestDomain();

    GeoContext geo = GeoContextFactory.create( config );
    List<GeoRole> matched = new ArrayList<GeoRole>();

    List<AvailableTable> tables = workspace.getAvailableTables().getAsAvailableTablesList();

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    for ( AvailableTable t : tables ) {
      for ( AvailableField field : t.getAvailableFields() ) {
        GeoRole geoRole = geo.matchFieldToGeoRole( field );
        if ( geoRole != null ) {
          matched.add( geoRole );
          System.out.println( "Identified geo field " + field.getName() + " => " + geoRole.getName() );
        }
      }
    }
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
  public void testBuildingGeoDimensions() throws Exception {
    // pre-configure to use the CUSTOMERS table
    generateTestDomain();

    GeoContext geo = GeoContextFactory.create( config );
    List<DimensionMetaData> geoDims = geo.buildDimensions( workspace );
    assertNotNull( geoDims );
    assertEquals( 1, geoDims.size() );

    DimensionMetaData dim = geoDims.get( 0 );
    assertNotNull( dim );

    IDataRoleAnnotation dataRole =
        (IDataRoleAnnotation) dim.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
    assertTrue( dataRole instanceof GeoRole );

    dataRole = (IDataRoleAnnotation) dim.get( 0 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
    assertTrue( dataRole instanceof GeoRole );

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    assertEquals( 4, dim.get( 0 ).size() );

    int lastFoundIndex = 0;
    int found = 0;
    // make sure they are in the correct order
    for ( int i = 0; i < dim.get( 0 ).size(); i++ ) {
      LevelMetaData level = dim.get( 0 ).get( i );

      dataRole = (IDataRoleAnnotation) level.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
      assertTrue( dataRole instanceof GeoRole );

      for ( int j = lastFoundIndex; j < geo.size(); j++ ) {
        GeoRole role = geo.getGeoRole( j );
        GeoRole geoRole = (GeoRole) level.getMemberAnnotations().get( GeoContext.ANNOTATION_GEO_ROLE );

        if ( geoRole.equals( role ) ) {
          assertTrue( i <= lastFoundIndex );
          lastFoundIndex = j;
          found++;
          continue;
        }
      }
    }
    assertTrue( found == dim.get( 0 ).size() );
  }

  @Test
  public void testBuildingGeoDimension_MultiTable() throws Exception {
    generateMultiStarTestDomain();

    GeoContext geo = GeoContextFactory.create( config );
    List<DimensionMetaData> geoDims = geo.buildDimensions( workspace );
    assertNotNull( geoDims );
    assertEquals( 1, geoDims.size() );

    DimensionMetaData dim = geoDims.get( 0 );
    assertNotNull( dim );

    IDataRoleAnnotation dataRole =
        (IDataRoleAnnotation) dim.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );

    assertTrue( dataRole instanceof GeoRole );
    dataRole = (IDataRoleAnnotation) dim.get( 0 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
    assertTrue( dataRole instanceof GeoRole );

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    assertEquals( 4, dim.get( 0 ).size() );

    int lastFoundIndex = 0;
    int found = 0;
    // make sure they are in the correct order
    for ( int i = 0; i < dim.get( 0 ).size(); i++ ) {
      LevelMetaData level = dim.get( 0 ).get( i );
      dataRole = (IDataRoleAnnotation) dim.get( 0 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
      assertTrue( dataRole instanceof GeoRole );
      for ( int j = lastFoundIndex; j < geo.size(); j++ ) {
        DataRole role = geo.getGeoRole( j );
        if ( level.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ).equals( role ) ) {
          assertTrue( i <= lastFoundIndex );
          lastFoundIndex = j;
          found++;
          continue;
        }
      }
    }
    assertTrue( found == dim.get( 0 ).size() );
  }

  @Test
  public void testMultipleTablesWithGeoFields() throws Exception {

    // build up 2 mock tables that both have geographic fields, make sure that
    // 2 dimensions are built, one for each table.

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    cols1.add( mockStateCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();

    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    IPhysicalTable mockTable2 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols2 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockCountryCol = createMock( IPhysicalColumn.class );
    cols2.add( mockCountryCol );
    IPhysicalColumn mockStateCol2 = createMock( IPhysicalColumn.class );
    cols2.add( mockStateCol2 );

    expect( mockTable2.getName( LOCALE ) ).andReturn( "ORDERFACT" ).anyTimes();
    expect( mockTable2.getPhysicalColumns() ).andReturn( cols2 ).anyTimes();
    expect( mockTable2.getId() ).andReturn( "PT_ORDERFACT" ).anyTimes();
    expect( mockTable2.getProperty( "target_table" ) ).andReturn( "PT_ORDERFACT" ).anyTimes();

    expect( mockCountryCol.getName( LOCALE ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getName( "en-US" ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getPhysicalTable() ).andReturn( mockTable2 ).anyTimes();
    expect( mockCountryCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockCountryCol.getAggregationList() ).andReturn( null );
    expect( mockCountryCol.getAggregationType() ).andReturn( null );
    expect( mockCountryCol.getId() ).andReturn( "COUNTRY" ).anyTimes();

    expect( mockStateCol2.getName( LOCALE ) ).andReturn( "Province" ).anyTimes();
    expect( mockStateCol2.getName( "en-US" ) ).andReturn( "Province" ).anyTimes();
    expect( mockStateCol2.getPhysicalTable() ).andReturn( mockTable2 ).anyTimes();
    expect( mockStateCol2.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol2.getAggregationList() ).andReturn( null );
    expect( mockStateCol2.getAggregationType() ).andReturn( null );
    expect( mockStateCol2.getId() ).andReturn( "PROVINCE" ).anyTimes();

    replay( mockTable1 );
    replay( mockStateCol );
    replay( mockTable2 );
    replay( mockCountryCol );
    replay( mockStateCol2 );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    AvailableTable table2 = new AvailableTable( mockTable2 );
    items.add( table2 );
    AvailableTable table3 = new AvailableTable( mockTable2 );
    table3.setFactTable( true ); // tables designated as fact tables shouldn't be considered for geo fields
    items.add( table3 );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateMultiStarTestDomain();
    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );
    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 2, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );
    DimensionMetaData orderGeoDim = dims.get( 1 );

    // since there was more than one geo dimension created, dim names should be prefixed with table name (example:
    // table_geography)
    String expectedDimName =
        mockTable1.getName( LOCALE ) + geo.getGeoRole( 0 ).getMatchSeparator() + geo.getDimensionName();
    assertEquals( expectedDimName, custGeoDim.getName() );
    assertEquals( 1, custGeoDim.get( 0 ).size() );

    expectedDimName = mockTable2.getName( LOCALE ) + geo.getGeoRole( 0 ).getMatchSeparator() + geo.getDimensionName();
    assertEquals( expectedDimName, orderGeoDim.getName() );
    assertEquals( 2, orderGeoDim.get( 0 ).size() );

    verify( mockTable1 );
    verify( mockStateCol );
    verify( mockTable2 );
    verify( mockCountryCol );
    verify( mockStateCol2 );
  }

  @Test
  public void testLatLongDetection() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMERNAME" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockStateCol );
    replay( mockCustomerCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat( workspace );

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );

    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 1, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );

    // make sure that the CustomerName dim->hier->level in the original automodeled version got updated with the
    // locationRole
    for ( DimensionMetaData existingDim : workspace.getModel().getDimensions() ) {
      if ( existingDim.getName().equalsIgnoreCase( "customername" ) ) {
        for ( HierarchyMetaData existingHier : existingDim ) {
          if ( existingHier.getName().equalsIgnoreCase( "customername" ) ) {
            for ( LevelMetaData existingLevel : existingHier ) {
              if ( existingLevel.getName().equalsIgnoreCase( "customername" ) ) {
                assertNotNull( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) );

                IDataRoleAnnotation dataRole =
                    (IDataRoleAnnotation) existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
                assertTrue( dataRole instanceof LocationRole );

                assertEquals( 2, existingLevel.size() );

                LocationRole lr =
                    (LocationRole) existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_GEO_ROLE );

                assertNotNull( existingLevel.getLatitudeField() );
                assertNotNull( existingLevel.getLongitudeField() );

                // make sure the lat & long fields are available as logical columns in the model
                LogicalColumn lc =
                    workspace.findLogicalColumn( existingLevel.getLatitudeField().getLogicalColumn()
                        .getPhysicalColumn(), ModelerPerspective.ANALYSIS );
                assertNotNull( lc );
                assertEquals( "latitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

                lc =
                  workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn()
                    .getPhysicalColumn(), ModelerPerspective.ANALYSIS );
                assertNotNull( lc );
                assertEquals( "longitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

              } else {
                // make sure none of the other levels have a data role added on

                assertNull( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) );
              }
            }
          }
        }
      }
    }

    verify( mockTable1 );
    verify( mockStateCol );
    verify( mockCustomerCol );
    verify( mockLatitudeCol );
    verify( mockLongitudeCol );
  }

  @Test
  public void testLatLongDetection_OnGeoField() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole. In this case the preceding col is also a GeoRole. That should be
    // overridden and set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "LATITUDE" ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "LONGITUDE" ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockStateCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat( workspace );

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );

    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 1, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );

    for ( LevelMetaData existingLevel : custGeoDim.get( 0 ) ) {
      if ( existingLevel.getName().equalsIgnoreCase( "state" ) ) {
        assertNotNull( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) );
        assertTrue( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) instanceof GeoRole );
        assertNotNull( existingLevel.getLatitudeField() );
        assertNotNull( existingLevel.getLongitudeField() );

        // make sure the lat & long fields are available as logical columns in the model
        LogicalColumn lc =
            workspace.findLogicalColumn( existingLevel.getLatitudeField().getLogicalColumn().getPhysicalColumn(),
                ModelerPerspective.ANALYSIS );
        assertNotNull( lc );
        assertEquals( "latitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

        lc =
          workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn().getPhysicalColumn(),
            ModelerPerspective.ANALYSIS );
        assertNotNull( lc );
        assertEquals( "longitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

      }
    }

    verify( mockTable1 );
    verify( mockStateCol );
    verify( mockLatitudeCol );
    verify( mockLongitudeCol );
  }

  @Test
  public void testLatLongDetection_LatLongAreFirstInSourceData() throws Exception {
    // LatLong fields should be detected and the column immediately following them in the source table should
    // get it's data role set to LocationRole.

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );

    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

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

    replay( mockTable1 );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );
    replay( mockCustomerCol );
    replay( mockStateCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat( workspace );

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );

    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 1, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );

    // make sure that the CustomerName dim->hier->level in the original automodeled version got updated with the
    // locationRole
    for ( DimensionMetaData existingDim : workspace.getModel().getDimensions() ) {
      if ( existingDim.getName().equalsIgnoreCase( "customername" ) ) {
        for ( HierarchyMetaData existingHier : existingDim ) {
          if ( existingHier.getName().equalsIgnoreCase( "customername" ) ) {
            for ( LevelMetaData existingLevel : existingHier ) {
              if ( existingLevel.getName().equalsIgnoreCase( "customername" ) ) {
                assertNotNull( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) );
                assertTrue(
                    existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) instanceof LocationRole );
                LocationRole lr =
                    (LocationRole) existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
                assertNotNull( existingLevel.getLatitudeField() );
                assertNotNull( existingLevel.getLongitudeField() );

                // make sure the lat & long fields are available as logical columns in the model
                LogicalColumn lc =
                    workspace.findLogicalColumn( existingLevel.getLatitudeField().getLogicalColumn()
                        .getPhysicalColumn(), ModelerPerspective.ANALYSIS );
                assertNotNull( lc );
                assertEquals( "latitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

                lc =
                  workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn()
                    .getPhysicalColumn(), ModelerPerspective.ANALYSIS );
                assertNotNull( lc );
                assertEquals( "longitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

              } else {
                // make sure none of the other levels have a data role added on
                assertNull( existingLevel.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ) );
              }
            }
          }
        }
      }
    }

    verify( mockTable1 );
    verify( mockLatitudeCol );
    verify( mockLongitudeCol );
    verify( mockCustomerCol );
    verify( mockStateCol );

    workspace.getWorkspaceHelper().populateDomain( workspace );
    List olapDimensions = (List) workspace.getDomain().getLogicalModels().get( 1 ).getProperty( "olap_dimensions" );
    boolean foundLevel = false;
    for ( Object d : olapDimensions ) {
      OlapDimension dim = (OlapDimension) d;
      if ( dim.getName().equalsIgnoreCase( "customername" ) ) {
        for ( OlapHierarchy hier : dim.getHierarchies() ) {
          if ( hier.getName().equalsIgnoreCase( "customername" ) ) {
            for ( OlapHierarchyLevel lvl : hier.getHierarchyLevels() ) {
              if ( lvl.getName().equalsIgnoreCase( "customername" ) ) {
                foundLevel = true;
                assertEquals( 2, lvl.getAnnotations().size() );
              }
            }
          }
        }
      }
    }
    assertTrue( foundLevel );
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

  @Test
  public void testInvertedOrderOfSourceGeoFields() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCountryCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCityCol = createMock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockCityCol );
    cols1.add( mockStateCol );
    cols1.add( mockCountryCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // country
    expect( mockCountryCol.getName( LOCALE ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getName( "en-US" ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCountryCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockCountryCol.getAggregationList() ).andReturn( null );
    expect( mockCountryCol.getAggregationType() ).andReturn( null );
    expect( mockCountryCol.getId() ).andReturn( "COUNTRY" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // city
    expect( mockCityCol.getName( LOCALE ) ).andReturn( "City" ).anyTimes();
    expect( mockCityCol.getName( "en-US" ) ).andReturn( "City" ).anyTimes();
    expect( mockCityCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCityCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockCityCol.getAggregationList() ).andReturn( null );
    expect( mockCityCol.getAggregationType() ).andReturn( null );
    expect( mockCityCol.getId() ).andReturn( "CITY" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMERNAME" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    replay( mockTable1 );
    replay( mockCustomerCol );
    replay( mockCityCol );
    replay( mockStateCol );
    replay( mockCountryCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat( workspace );

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );

    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 1, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );

    // make sure we only have 3 levels and that they are in the correct order (country, state, city)
    assertEquals( 3, custGeoDim.get( 0 ).size() );

    assertEquals( "location", custGeoDim.get( 0 ).get( 0 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() ); // due to order of fields, this should be the location role
    assertEquals( "state", custGeoDim.get( 0 ).get( 1 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() );
    assertEquals( "city", custGeoDim.get( 0 ).get( 2 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() );

    verify( mockTable1 );
    verify( mockCityCol );
    verify( mockStateCol );
    verify( mockCountryCol );
    verify( mockCustomerCol );
    verify( mockLatitudeCol );
    verify( mockLongitudeCol );
  }

  @Test
  public void testSourceDataAlreadyHasGeographyField() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCountryCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockCityCol = createMock( IPhysicalColumn.class );
    IPhysicalColumn mockGeoCol = createMock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockCityCol );
    cols1.add( mockStateCol );
    cols1.add( mockCountryCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockGeoCol );

    expect( mockTable1.getName( LOCALE ) ).andReturn( "CUSTOMERS" ).anyTimes();
    expect( mockTable1.getPhysicalColumns() ).andReturn( cols1 ).anyTimes();
    expect( mockTable1.getId() ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "target_table" ) ).andReturn( "PT_CUSTOMERS" ).anyTimes();
    expect( mockTable1.getProperty( "name" ) ).andReturn( "CUSTOMERS" ).anyTimes();

    // country
    expect( mockCountryCol.getName( LOCALE ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getName( "en-US" ) ).andReturn( "Country" ).anyTimes();
    expect( mockCountryCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCountryCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockCountryCol.getAggregationList() ).andReturn( null );
    expect( mockCountryCol.getAggregationType() ).andReturn( null );
    expect( mockCountryCol.getId() ).andReturn( "COUNTRY" ).anyTimes();

    // state col
    expect( mockStateCol.getName( LOCALE ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getName( "en-US" ) ).andReturn( "State" ).anyTimes();
    expect( mockStateCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockStateCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockStateCol.getAggregationList() ).andReturn( null );
    expect( mockStateCol.getAggregationType() ).andReturn( null );
    expect( mockStateCol.getId() ).andReturn( "STATE" ).anyTimes();

    // city
    expect( mockCityCol.getName( LOCALE ) ).andReturn( "City" ).anyTimes();
    expect( mockCityCol.getName( "en-US" ) ).andReturn( "City" ).anyTimes();
    expect( mockCityCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCityCol.getDataType() ).andReturn( DataType.STRING );
    expect( mockCityCol.getAggregationList() ).andReturn( null );
    expect( mockCityCol.getAggregationType() ).andReturn( null );
    expect( mockCityCol.getId() ).andReturn( "CITY" ).anyTimes();

    // customer col
    expect( mockCustomerCol.getName( LOCALE ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getName( "en-US" ) ).andReturn( "CustomerName" ).anyTimes();
    expect( mockCustomerCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockCustomerCol.getId() ).andReturn( "CUSTOMERNAME" ).anyTimes();

    // lat col
    expect( mockLatitudeCol.getName( LOCALE ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getName( "en-US" ) ).andReturn( "Latitude" ).anyTimes();
    expect( mockLatitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLatitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLatitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLatitudeCol.getId() ).andReturn( "LATITUDE" ).anyTimes();

    HashMap<String, Object> latProperties = new HashMap<String, Object>();
    latProperties.put( "name", "Latitude" );
    expect( mockLatitudeCol.getProperties() ).andReturn( latProperties ).anyTimes();

    // lng col
    expect( mockLongitudeCol.getName( LOCALE ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getName( "en-US" ) ).andReturn( "Longitude" ).anyTimes();
    expect( mockLongitudeCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockLongitudeCol.getDataType() ).andReturn( DataType.NUMERIC ).anyTimes();
    expect( mockLongitudeCol.getAggregationList() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getAggregationType() ).andReturn( null ).anyTimes();
    expect( mockLongitudeCol.getId() ).andReturn( "LONGITUDE" ).anyTimes();

    HashMap<String, Object> lngProperties = new HashMap<String, Object>();
    lngProperties.put( "name", "Longitude" );
    expect( mockLongitudeCol.getProperties() ).andReturn( lngProperties ).anyTimes();

    // geography col
    expect( mockGeoCol.getName( LOCALE ) ).andReturn( "Geography" ).anyTimes();
    expect( mockGeoCol.getName( "en-US" ) ).andReturn( "Geography" ).anyTimes();
    expect( mockGeoCol.getPhysicalTable() ).andReturn( mockTable1 ).anyTimes();
    expect( mockGeoCol.getId() ).andReturn( "GEOGRAPHY" ).anyTimes();

    replay( mockTable1 );
    replay( mockCustomerCol );
    replay( mockCityCol );
    replay( mockStateCol );
    replay( mockCountryCol );
    replay( mockLatitudeCol );
    replay( mockLongitudeCol );
    replay( mockGeoCol );

    AvailableTable table = new AvailableTable( mockTable1 );
    items.add( table );
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat( workspace );

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren( items );

    GeoContext geo = GeoContextFactory.create( config );

    List<DimensionMetaData> dims = geo.buildDimensions( workspace );
    assertEquals( 1, dims.size() );

    DimensionMetaData custGeoDim = dims.get( 0 );

    assertEquals( "Geography2", custGeoDim.getName() );

    // make sure we only have 3 levels and that they are in the correct order (country, state, city)
    assertEquals( 3, custGeoDim.get( 0 ).size() );

    assertEquals( "location", custGeoDim.get( 0 ).get( 0 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() ); // due to order of fields, this should be the location role
    assertEquals( "state", custGeoDim.get( 0 ).get( 1 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() );
    assertEquals( "city", custGeoDim.get( 0 ).get( 2 ).getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE )
        .getName() );

    verify( mockTable1 );
    verify( mockCityCol );
    verify( mockStateCol );
    verify( mockCountryCol );
    verify( mockCustomerCol );
    verify( mockLatitudeCol );
    verify( mockLongitudeCol );
    verify( mockGeoCol );
  }

}
