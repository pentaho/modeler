/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.geo;

import org.junit.BeforeClass;
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
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.nodes.annotations.IDataRoleAnnotation;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.agilebi.modeler.geo.GeoContext.ANNOTATION_DATA_ROLE;
import static org.pentaho.agilebi.modeler.geo.GeoContext.ANNOTATION_GEO_ROLE;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/16/11 Time: 9:19 AM To change this template use File | Settings |
 * File Templates.
 */
public class GeoContextIT extends AbstractModelerTest {
  private static final String GEO_ROLE_KEY = "geo.roles";
  private static final String LOCALE = "en_US";
  private static Properties props = null;
  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "src/it/resources/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
  }

  @Test
  public void testMatchingAliasesToRole() throws Exception {

    // pre-configure to use the CUSTOMERS table
    generateTestDomain();

    GeoContext geo = GeoContextFactory.create( config );
    List<GeoRole> matched = new ArrayList<>();

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
    assertEquals( dim.get( 0 ).size(), found );
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
    assertEquals( dim.get( 0 ).size(), found );
  }

  @Test
  public void testMultipleTablesWithGeoFields() throws Exception {

    // build up 2 mock tables that both have geographic fields, make sure that
    // 2 dimensions are built, one for each table.

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    cols1.add( mockStateCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );

    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    IPhysicalTable mockTable2 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols2 = new ArrayList<>();
    IPhysicalColumn mockCountryCol = mock( IPhysicalColumn.class );
    cols2.add( mockCountryCol );
    IPhysicalColumn mockStateCol2 = mock( IPhysicalColumn.class );
    cols2.add( mockStateCol2 );

    when( mockTable2.getName( LOCALE ) ).thenReturn( "ORDERFACT" );
    when( mockTable2.getPhysicalColumns() ).thenReturn( cols2 );
    when( mockTable2.getId() ).thenReturn( "PT_ORDERFACT" );
    when( mockTable2.getProperty( "target_table" ) ).thenReturn( "PT_ORDERFACT" );

    when( mockCountryCol.getName( LOCALE ) ).thenReturn( "Country" );
    when( mockCountryCol.getName( "en-US" ) ).thenReturn( "Country" );
    when( mockCountryCol.getPhysicalTable() ).thenReturn( mockTable2 );
    when( mockCountryCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockCountryCol.getAggregationList() ).thenReturn( null );
    when( mockCountryCol.getAggregationType() ).thenReturn( null );
    when( mockCountryCol.getId() ).thenReturn( "COUNTRY" );

    when( mockStateCol2.getName( LOCALE ) ).thenReturn( "Province" );
    when( mockStateCol2.getName( "en-US" ) ).thenReturn( "Province" );
    when( mockStateCol2.getPhysicalTable() ).thenReturn( mockTable2 );
    when( mockStateCol2.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol2.getAggregationList() ).thenReturn( null );
    when( mockStateCol2.getAggregationType() ).thenReturn( null );
    when( mockStateCol2.getId() ).thenReturn( "PROVINCE" );

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
  }

  @Test
  public void testLatLongDetection() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get its data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    // customer col
    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

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

                lc = workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn()
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
  }

  @Test
  public void testLatLongDetection_OnGeoField() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get its data role set to LocationRole. In this case the preceding col is also a GeoRole. That should be
    // overridden and set to LocationRole

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

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

        lc = workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn().getPhysicalColumn(),
          ModelerPerspective.ANALYSIS );
        assertNotNull( lc );
        assertEquals( "longitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );
      }
    }
  }

  @Test
  public void testLatLongDetection_LatLongAreFirstInSourceData() throws Exception {
    // LatLong fields should be detected and the column immediately following them in the source table should
    // get its data role set to LocationRole.

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockStateCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

    // customer col
    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

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
                LogicalColumn lc = workspace.findLogicalColumn( existingLevel.getLatitudeField().getLogicalColumn()
                  .getPhysicalColumn(), ModelerPerspective.ANALYSIS );
                assertNotNull( lc );
                assertEquals( "latitude", lc.getName( workspace.getWorkspaceHelper().getLocale() ) );

                lc = workspace.findLogicalColumn( existingLevel.getLongitudeField().getLogicalColumn()
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

    workspace.getWorkspaceHelper().populateDomain( workspace );
    List<OlapDimension> olapDimensions = (List) workspace.getDomain().getLogicalModels().get( 1 ).getProperty( "olap_dimensions" );
    boolean foundLevel = false;
    for ( OlapDimension dim : olapDimensions ) {
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
  public void testInvertedOrderOfSourceGeoFields() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get its data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCountryCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCityCol = mock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockCityCol );
    cols1.add( mockStateCol );
    cols1.add( mockCountryCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // country
    when( mockCountryCol.getName( LOCALE ) ).thenReturn( "Country" );
    when( mockCountryCol.getName( "en-US" ) ).thenReturn( "Country" );
    when( mockCountryCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCountryCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockCountryCol.getAggregationList() ).thenReturn( null );
    when( mockCountryCol.getAggregationType() ).thenReturn( null );
    when( mockCountryCol.getId() ).thenReturn( "COUNTRY" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    // city
    when( mockCityCol.getName( LOCALE ) ).thenReturn( "City" );
    when( mockCityCol.getName( "en-US" ) ).thenReturn( "City" );
    when( mockCityCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCityCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockCityCol.getAggregationList() ).thenReturn( null );
    when( mockCityCol.getAggregationType() ).thenReturn( null );
    when( mockCityCol.getId() ).thenReturn( "CITY" );

    // customer col
    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

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
  }

  @Test
  public void testSourceDataAlreadyHasGeographyField() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get its data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCountryCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCityCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockGeoCol = mock( IPhysicalColumn.class );

    cols1.add( mockCustomerCol );
    cols1.add( mockCityCol );
    cols1.add( mockStateCol );
    cols1.add( mockCountryCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );
    cols1.add( mockGeoCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // country
    when( mockCountryCol.getName( LOCALE ) ).thenReturn( "Country" );
    when( mockCountryCol.getName( "en-US" ) ).thenReturn( "Country" );
    when( mockCountryCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCountryCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockCountryCol.getAggregationList() ).thenReturn( null );
    when( mockCountryCol.getAggregationType() ).thenReturn( null );
    when( mockCountryCol.getId() ).thenReturn( "COUNTRY" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    // city
    when( mockCityCol.getName( LOCALE ) ).thenReturn( "City" );
    when( mockCityCol.getName( "en-US" ) ).thenReturn( "City" );
    when( mockCityCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCityCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockCityCol.getAggregationList() ).thenReturn( null );
    when( mockCityCol.getAggregationType() ).thenReturn( null );
    when( mockCityCol.getId() ).thenReturn( "CITY" );

    // customer col
    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

    // geography col
    when( mockGeoCol.getName( LOCALE ) ).thenReturn( "Geography" );
    when( mockGeoCol.getName( "en-US" ) ).thenReturn( "Geography" );
    when( mockGeoCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockGeoCol.getId() ).thenReturn( "GEOGRAPHY" );

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
  }

  @Test
  public void testSetLocationFields() throws Exception {
    List<IAvailableItem> items = new ArrayList<>();

    // mock object init...
    IPhysicalTable mockTable1 = mock( IPhysicalTable.class );
    List<IPhysicalColumn> cols1 = new ArrayList<>();
    IPhysicalColumn mockStateCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockCustomerCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLatitudeCol = mock( IPhysicalColumn.class );
    IPhysicalColumn mockLongitudeCol = mock( IPhysicalColumn.class );

    cols1.add( mockStateCol );
    cols1.add( mockCustomerCol );
    cols1.add( mockLatitudeCol );
    cols1.add( mockLongitudeCol );

    when( mockTable1.getName( LOCALE ) ).thenReturn( "CUSTOMERS" );
    when( mockTable1.getPhysicalColumns() ).thenReturn( cols1 );
    when( mockTable1.getId() ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "target_table" ) ).thenReturn( "PT_CUSTOMERS" );
    when( mockTable1.getProperty( "name" ) ).thenReturn( "CUSTOMERS" );

    // state col
    when( mockStateCol.getName( LOCALE ) ).thenReturn( "State" );
    when( mockStateCol.getName( "en-US" ) ).thenReturn( "State" );
    when( mockStateCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockStateCol.getDataType() ).thenReturn( DataType.STRING );
    when( mockStateCol.getAggregationList() ).thenReturn( null );
    when( mockStateCol.getAggregationType() ).thenReturn( null );
    when( mockStateCol.getId() ).thenReturn( "STATE" );

    // customer col
    when( mockCustomerCol.getName( LOCALE ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getName( "en-US" ) ).thenReturn( "CustomerName" );
    when( mockCustomerCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockCustomerCol.getId() ).thenReturn( "CUSTOMERNAME" );

    // lat col
    when( mockLatitudeCol.getName( LOCALE ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getName( "en-US" ) ).thenReturn( "Latitude" );
    when( mockLatitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLatitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLatitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLatitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLatitudeCol.getId() ).thenReturn( "LATITUDE" );

    HashMap<String, Object> latProperties = new HashMap<>();
    latProperties.put( "name", "Latitude" );
    when( mockLatitudeCol.getProperties() ).thenReturn( latProperties );

    // lng col
    when( mockLongitudeCol.getName( LOCALE ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getName( "en-US" ) ).thenReturn( "Longitude" );
    when( mockLongitudeCol.getPhysicalTable() ).thenReturn( mockTable1 );
    when( mockLongitudeCol.getDataType() ).thenReturn( DataType.NUMERIC );
    when( mockLongitudeCol.getAggregationList() ).thenReturn( null );
    when( mockLongitudeCol.getAggregationType() ).thenReturn( null );
    when( mockLongitudeCol.getId() ).thenReturn( "LONGITUDE" );

    HashMap<String, Object> lngProperties = new HashMap<>();
    lngProperties.put( "name", "Longitude" );
    when( mockLongitudeCol.getProperties() ).thenReturn( lngProperties );

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
    LocationRole locationRole = geo.getLocationRole();

    LevelMetaData levelMetaData = new LevelMetaData();
    geo.setLocationFields( workspace, levelMetaData );

    assertEquals( locationRole, levelMetaData.getMemberAnnotations().get( ANNOTATION_DATA_ROLE ) );
    assertEquals( locationRole, levelMetaData.getMemberAnnotations().get( ANNOTATION_GEO_ROLE ) );

    MemberPropertyMetaData latMemberPropertyMetaData = levelMetaData.get( 0 );
    MemberPropertyMetaData longMemberPropertyMetaData = levelMetaData.get( 1 );

    assertEquals( GeoContext.LATITUDE, latMemberPropertyMetaData.getName() );
    assertEquals( "LATITUDE", latMemberPropertyMetaData.getLogicalColumn().getPhysicalColumn().getId() );

    assertEquals( GeoContext.LONGITUDE, longMemberPropertyMetaData.getName() );
    assertEquals( "LONGITUDE", longMemberPropertyMetaData.getLogicalColumn().getPhysicalColumn().getId() );
  }
}
