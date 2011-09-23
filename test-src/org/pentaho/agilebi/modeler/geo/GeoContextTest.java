package org.pentaho.agilebi.modeler.geo;

import org.apache.tools.ant.Location;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.DataType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/16/11
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoContextTest extends AbstractModelerTest {
  private static final String GEO_ROLE_KEY = "geo.roles";
  private static Properties props = null;
  private static final String LOCALE = "en_US";

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader(new File("test-res/geoRoles.properties"));
    props = new Properties();
    props.load(propsReader);
  }

  @Test
  public void addGeoRole() {
    GeoContext geo = new GeoContext();
    assertEquals(0, geo.size());
    geo.addGeoRole(new GeoRole("country", Arrays.asList(new String[]{"country, ctry"})));
    geo.addGeoRole(new GeoRole("state", Arrays.asList(new String[]{"state, st, province"})));
    geo.addGeoRole(new GeoRole("city", Arrays.asList(new String[]{"city, town"})));
    assertEquals(3, geo.size());

    // make sure they are in the same order as entered
    assertEquals("country", geo.getGeoRole(0).getName());
    assertEquals("state", geo.getGeoRole(1).getName());
    assertEquals("city", geo.getGeoRole(2).getName());
  }

  @Test
  public void testMatchingAliasesToRole() throws Exception {

    // pre-configure to use the CUSTOMERS table
    generateTestDomain();
    
    GeoContext geo = GeoContextFactory.create(props);
    List<GeoRole> matched = new ArrayList<GeoRole>();

    List<AvailableTable> tables = workspace.getAvailableTables().getAsAvailableTablesList();

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    for(AvailableTable t: tables){
      for(AvailableField field: t.getAvailableFields()) {
        GeoRole geoRole = geo.matchFieldToGeoRole(field);
        if (geoRole != null) {
          matched.add(geoRole);
          System.out.println("Identified geo field " + field.getName() + " => " + geoRole.getName());
        }
      }
    }
  }

  @Test
  public void testBuildingGeoDimensions() throws Exception {
    // pre-configure to use the CUSTOMERS table
    generateTestDomain();

    GeoContext geo = GeoContextFactory.create(props);
    List<DimensionMetaData> geoDims = geo.buildDimensions(workspace);
    assertNotNull(geoDims);
    assertEquals(1, geoDims.size());

    DimensionMetaData dim = geoDims.get(0);
    assertNotNull(dim);

    assertTrue(dim.getDataRole() instanceof GeoRole);
    assertTrue(dim.get(0).getDataRole() instanceof GeoRole);

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    assertEquals(4, dim.get(0).size());

    int lastFoundIndex = 0;
    int found = 0;
    // make sure they are in the correct order
    for (int i = 0; i < dim.get(0).size(); i++) {
      LevelMetaData level = dim.get(0).get(i);
      assertTrue(level.getDataRole() instanceof GeoRole);
      for(int j = lastFoundIndex; j < geo.size(); j++) {
        DataRole role = geo.getGeoRole(j);
        if(level.getDataRole().equals(role)) {
          assertTrue(i <= lastFoundIndex);
          lastFoundIndex = j;
          found++;
          continue;
        }
      }
    }
    assertTrue(found == dim.get(0).size());
  }

  @Test
  public void testBuildingGeoDimension_MultiTable() throws Exception {
    generateMultiStarTestDomain();

    GeoContext geo = GeoContextFactory.create(props);
    List<DimensionMetaData> geoDims = geo.buildDimensions(workspace);
    assertNotNull(geoDims);
    assertEquals(1, geoDims.size());

    DimensionMetaData dim = geoDims.get(0);
    assertNotNull(dim);

    assertTrue(dim.getDataRole() instanceof GeoRole);
    assertTrue(dim.get(0).getDataRole() instanceof GeoRole);

    // CUSTOMERS table has CITY, COUNTRY, STATE, and POSTALCODE fields that should be auto-detected
    assertEquals(4, dim.get(0).size());

    int lastFoundIndex = 0;
    int found = 0;
    // make sure they are in the correct order
    for (int i = 0; i < dim.get(0).size(); i++) {
      LevelMetaData level = dim.get(0).get(i);
      assertTrue(level.getDataRole() instanceof GeoRole);
      for(int j = lastFoundIndex; j < geo.size(); j++) {
        DataRole role = geo.getGeoRole(j);
        if(level.getDataRole().equals(role)) {
          assertTrue(i <= lastFoundIndex);
          lastFoundIndex = j;
          found++;
          continue;
        }
      }
    }
    assertTrue(found == dim.get(0).size());
  }

  @Test
  public void testMultipleTablesWithGeoFields() throws Exception {

    // build up 2 mock tables that both have geographic fields, make sure that
    // 2 dimensions are built, one for each table.

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock(IPhysicalTable.class);
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock(IPhysicalColumn.class);
    cols1.add(mockStateCol);

    expect(mockTable1.getName(LOCALE)).andReturn("CUSTOMERS").anyTimes();
    expect(mockTable1.getPhysicalColumns()).andReturn(cols1).anyTimes();
    expect(mockTable1.getId()).andReturn("PT_CUSTOMERS").anyTimes();
    expect(mockTable1.getProperty("target_table")).andReturn("PT_CUSTOMERS").anyTimes();

    expect(mockStateCol.getName(LOCALE)).andReturn("State").anyTimes();
    expect(mockStateCol.getName("en-US")).andReturn("State").anyTimes();
    expect(mockStateCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockStateCol.getDataType()).andReturn(DataType.STRING);
    expect(mockStateCol.getAggregationList()).andReturn(null);
    expect(mockStateCol.getAggregationType()).andReturn(null);
    expect(mockStateCol.getId()).andReturn("PC_STATE").anyTimes();

    IPhysicalTable mockTable2 = createMock(IPhysicalTable.class);
    List<IPhysicalColumn> cols2 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockCountryCol = createMock(IPhysicalColumn.class);
    cols2.add(mockCountryCol);
    IPhysicalColumn mockStateCol2 = createMock(IPhysicalColumn.class);
    cols2.add(mockStateCol2);

    expect(mockTable2.getName(LOCALE)).andReturn("ORDERFACT").anyTimes();
    expect(mockTable2.getPhysicalColumns()).andReturn(cols2).anyTimes();
    expect(mockTable2.getId()).andReturn("PT_ORDERFACT").anyTimes();
    expect(mockTable2.getProperty("target_table")).andReturn("PT_ORDERFACT").anyTimes();

    expect(mockCountryCol.getName(LOCALE)).andReturn("Country").anyTimes();
    expect(mockCountryCol.getName("en-US")).andReturn("Country").anyTimes();
    expect(mockCountryCol.getPhysicalTable()).andReturn(mockTable2).anyTimes();
    expect(mockCountryCol.getDataType()).andReturn(DataType.STRING);
    expect(mockCountryCol.getAggregationList()).andReturn(null);
    expect(mockCountryCol.getAggregationType()).andReturn(null);
    expect(mockCountryCol.getId()).andReturn("PC_COUNTRY").anyTimes();

    expect(mockStateCol2.getName(LOCALE)).andReturn("Province").anyTimes();
    expect(mockStateCol2.getName("en-US")).andReturn("Province").anyTimes();
    expect(mockStateCol2.getPhysicalTable()).andReturn(mockTable2).anyTimes();
    expect(mockStateCol2.getDataType()).andReturn(DataType.STRING);
    expect(mockStateCol2.getAggregationList()).andReturn(null);
    expect(mockStateCol2.getAggregationType()).andReturn(null);
    expect(mockStateCol2.getId()).andReturn("PC_PROVINCE").anyTimes();

    replay(mockTable1);
    replay(mockStateCol);
    replay(mockTable2);
    replay(mockCountryCol);
    replay(mockStateCol2);

    AvailableTable table = new AvailableTable(mockTable1);
    items.add(table);
    AvailableTable table2 = new AvailableTable(mockTable2);
    items.add(table2);
    AvailableTable table3 = new AvailableTable(mockTable2);
    table3.setFactTable(true); // tables designated as fact tables shouldn't be considered for geo fields
    items.add(table3);
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateMultiStarTestDomain();
    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren(items);

    GeoContext geo = GeoContextFactory.create(props);
    List<DimensionMetaData> dims = geo.buildDimensions(workspace);
    assertEquals(2, dims.size());

    DimensionMetaData custGeoDim = dims.get(0);
    DimensionMetaData orderGeoDim = dims.get(1);

    // since there was more than one geo dimension created, dim names should be prefixed with table name (example: table_geography)
    String expectedDimName = mockTable1.getName(LOCALE) + geo.getGeoRole(0).getMatchSeparator() + geo.getDimensionName();
    assertEquals(expectedDimName, custGeoDim.getName());
    assertEquals(1, custGeoDim.get(0).size());

    expectedDimName = mockTable2.getName(LOCALE) + geo.getGeoRole(0).getMatchSeparator() + geo.getDimensionName();
    assertEquals(expectedDimName, orderGeoDim.getName());
    assertEquals(2, orderGeoDim.get(0).size());

    verify(mockTable1);
    verify(mockStateCol);
    verify(mockTable2);
    verify(mockCountryCol);
    verify(mockStateCol2);
  }


  @Test
  public void testLatLongDetection() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock(IPhysicalTable.class);
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockCustomerCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLatitudeCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLongitudeCol = createMock(IPhysicalColumn.class);


    cols1.add(mockStateCol);
    cols1.add(mockCustomerCol);
    cols1.add(mockLatitudeCol);
    cols1.add(mockLongitudeCol);

    expect(mockTable1.getName(LOCALE)).andReturn("CUSTOMERS").anyTimes();
    expect(mockTable1.getPhysicalColumns()).andReturn(cols1).anyTimes();
    expect(mockTable1.getId()).andReturn("PT_CUSTOMERS").anyTimes();
    expect(mockTable1.getProperty("target_table")).andReturn("PT_CUSTOMERS").anyTimes();

    // state col
    expect(mockStateCol.getName(LOCALE)).andReturn("State").anyTimes();
    expect(mockStateCol.getName("en-US")).andReturn("State").anyTimes();
    expect(mockStateCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockStateCol.getDataType()).andReturn(DataType.STRING);
    expect(mockStateCol.getAggregationList()).andReturn(null);
    expect(mockStateCol.getAggregationType()).andReturn(null);
    expect(mockStateCol.getId()).andReturn("PC_STATE").anyTimes();

    // customer col
    expect(mockCustomerCol.getName(LOCALE)).andReturn("CustomerName").anyTimes();
    expect(mockCustomerCol.getName("en-US")).andReturn("CustomerName").anyTimes();
    expect(mockCustomerCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockCustomerCol.getId()).andReturn("CUSTOMERNAME").anyTimes();

    // lat col
    expect(mockLatitudeCol.getName(LOCALE)).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getName("en-US")).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockLatitudeCol.getId()).andReturn("LATITUDE").anyTimes();

    // lng col
    expect(mockLongitudeCol.getName(LOCALE)).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getName("en-US")).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockLongitudeCol.getId()).andReturn("LONGITUDE").anyTimes();

    replay(mockTable1);
    replay(mockStateCol);
    replay(mockCustomerCol);
    replay(mockLatitudeCol);
    replay(mockLongitudeCol);

    AvailableTable table = new AvailableTable(mockTable1);
    items.add(table);
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat(workspace);

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren(items);

    GeoContext geo = GeoContextFactory.create(props);

    List<DimensionMetaData> dims = geo.buildDimensions(workspace);
    assertEquals(1, dims.size());

    DimensionMetaData custGeoDim = dims.get(0);

    // make sure that the CustomerName dim->hier->level in the original automodeled version got updated with the locationRole
    for(DimensionMetaData existingDim : workspace.getModel().getDimensions()) {
      if(existingDim.getName().equalsIgnoreCase("customername")) {
        for(HierarchyMetaData existingHier : existingDim) {
          if(existingHier.getName().equalsIgnoreCase("customername")) {
            for(LevelMetaData existingLevel : existingHier) {
              if(existingLevel.getName().equalsIgnoreCase("customername")) {
                assertNotNull(existingLevel.getDataRole());
                assertTrue(existingLevel.getDataRole() instanceof LocationRole);
                LocationRole lr = (LocationRole)existingLevel.getDataRole();
                assertNotNull(lr.getLatitudeField());
                assertNotNull(lr.getLongitudeField());
              } else {
                // make sure none of the other levels have a data role added on
                assertNull(existingLevel.getDataRole());
              }
            }
          }
        }
      }
    }

    verify(mockTable1);
    verify(mockStateCol);
    verify(mockCustomerCol);
    verify(mockLatitudeCol);
    verify(mockLongitudeCol);
  }

  @Test
  public void testLatLongDetection_OnGeoField() throws Exception {
    // LatLong fields should be detected and the column immediately preceding them in the source table should
    // get it's data role set to LocationRole. In this case the preceding col is also a GeoRole. That should be
    // overridden and set to LocationRole

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock(IPhysicalTable.class);
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLatitudeCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLongitudeCol = createMock(IPhysicalColumn.class);


    cols1.add(mockStateCol);
    cols1.add(mockLatitudeCol);
    cols1.add(mockLongitudeCol);

    expect(mockTable1.getName(LOCALE)).andReturn("CUSTOMERS").anyTimes();
    expect(mockTable1.getPhysicalColumns()).andReturn(cols1).anyTimes();
    expect(mockTable1.getId()).andReturn("PT_CUSTOMERS").anyTimes();
    expect(mockTable1.getProperty("target_table")).andReturn("PT_CUSTOMERS").anyTimes();

    // state col
    expect(mockStateCol.getName(LOCALE)).andReturn("State").anyTimes();
    expect(mockStateCol.getName("en-US")).andReturn("State").anyTimes();
    expect(mockStateCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockStateCol.getDataType()).andReturn(DataType.STRING);
    expect(mockStateCol.getAggregationList()).andReturn(null);
    expect(mockStateCol.getAggregationType()).andReturn(null);
    expect(mockStateCol.getId()).andReturn("PC_STATE").anyTimes();

    // lat col
    expect(mockLatitudeCol.getName(LOCALE)).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getName("en-US")).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getId()).andReturn("LATITUDE").anyTimes();
    expect(mockLatitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();

    // lng col
    expect(mockLongitudeCol.getName(LOCALE)).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getName("en-US")).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getId()).andReturn("LONGITUDE").anyTimes();
    expect(mockLongitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();

    replay(mockTable1);
    replay(mockStateCol);
    replay(mockLatitudeCol);
    replay(mockLongitudeCol);

    AvailableTable table = new AvailableTable(mockTable1);
    items.add(table);
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat(workspace);

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren(items);

    GeoContext geo = GeoContextFactory.create(props);

    List<DimensionMetaData> dims = geo.buildDimensions(workspace);
    assertEquals(1, dims.size());

    DimensionMetaData custGeoDim = dims.get(0);

    for(LevelMetaData existingLevel : custGeoDim.get(0)) {
      if(existingLevel.getName().equalsIgnoreCase("state")) {
        assertNotNull(existingLevel.getDataRole());
        assertTrue(existingLevel.getDataRole() instanceof LocationRole);
        LocationRole lr = (LocationRole)existingLevel.getDataRole();
        assertNotNull(lr.getLatitudeField());
        assertNotNull(lr.getLongitudeField());
      }
    }

    verify(mockTable1);
    verify(mockStateCol);
    verify(mockLatitudeCol);
    verify(mockLongitudeCol);
  }

  @Test
  public void testLatLongDetection_LatLongAreFirstInSourceData() throws Exception {
    // LatLong fields should be detected and the column immediately following them in the source table should
    // get it's data role set to LocationRole.

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();

    // mock object init...
    IPhysicalTable mockTable1 = createMock(IPhysicalTable.class);
    List<IPhysicalColumn> cols1 = new ArrayList<IPhysicalColumn>();
    IPhysicalColumn mockStateCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockCustomerCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLatitudeCol = createMock(IPhysicalColumn.class);
    IPhysicalColumn mockLongitudeCol = createMock(IPhysicalColumn.class);

    cols1.add(mockLatitudeCol);
    cols1.add(mockLongitudeCol);
    cols1.add(mockCustomerCol);
    cols1.add(mockStateCol);

    expect(mockTable1.getName(LOCALE)).andReturn("CUSTOMERS").anyTimes();
    expect(mockTable1.getPhysicalColumns()).andReturn(cols1).anyTimes();
    expect(mockTable1.getId()).andReturn("PT_CUSTOMERS").anyTimes();
    expect(mockTable1.getProperty("target_table")).andReturn("PT_CUSTOMERS").anyTimes();

    // lat col
    expect(mockLatitudeCol.getName(LOCALE)).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getName("en-US")).andReturn("Latitude").anyTimes();
    expect(mockLatitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockLatitudeCol.getId()).andReturn("LATITUDE").anyTimes();

    // lng col
    expect(mockLongitudeCol.getName(LOCALE)).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getName("en-US")).andReturn("Longitude").anyTimes();
    expect(mockLongitudeCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockLongitudeCol.getId()).andReturn("LONGITUDE").anyTimes();

    // customer col
    expect(mockCustomerCol.getName(LOCALE)).andReturn("CustomerName").anyTimes();
    expect(mockCustomerCol.getName("en-US")).andReturn("CustomerName").anyTimes();
    expect(mockCustomerCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockCustomerCol.getId()).andReturn("CUSTOMERNAME").anyTimes();

    // state col
    expect(mockStateCol.getName(LOCALE)).andReturn("State").anyTimes();
    expect(mockStateCol.getName("en-US")).andReturn("State").anyTimes();
    expect(mockStateCol.getPhysicalTable()).andReturn(mockTable1).anyTimes();
    expect(mockStateCol.getDataType()).andReturn(DataType.STRING);
    expect(mockStateCol.getAggregationList()).andReturn(null);
    expect(mockStateCol.getAggregationType()).andReturn(null);
    expect(mockStateCol.getId()).andReturn("PC_STATE").anyTimes();

    replay(mockTable1);
    replay(mockLatitudeCol);
    replay(mockLongitudeCol);
    replay(mockCustomerCol);
    replay(mockStateCol);

    AvailableTable table = new AvailableTable(mockTable1);
    items.add(table);
    // end mock object init...

    // use the existing tool to generate a domain with multiple tables
    generateTestDomain();

    // automodel this first, so we have some dimension to test that our locationRole gets set properly
    workspace.getWorkspaceHelper().autoModelFlat(workspace);

    // overwrite the table definitions with our mocks, so we control the columns
    workspace.getAvailableTables().setChildren(items);

    GeoContext geo = GeoContextFactory.create(props);

    List<DimensionMetaData> dims = geo.buildDimensions(workspace);
    assertEquals(1, dims.size());

    DimensionMetaData custGeoDim = dims.get(0);

    // make sure that the CustomerName dim->hier->level in the original automodeled version got updated with the locationRole
    for(DimensionMetaData existingDim : workspace.getModel().getDimensions()) {
      if(existingDim.getName().equalsIgnoreCase("customername")) {
        for(HierarchyMetaData existingHier : existingDim) {
          if(existingHier.getName().equalsIgnoreCase("customername")) {
            for(LevelMetaData existingLevel : existingHier) {
              if(existingLevel.getName().equalsIgnoreCase("customername")) {
                assertNotNull(existingLevel.getDataRole());
                assertTrue(existingLevel.getDataRole() instanceof LocationRole);
                LocationRole lr = (LocationRole)existingLevel.getDataRole();
                assertNotNull(lr.getLatitudeField());
                assertNotNull(lr.getLongitudeField());
              } else {
                // make sure none of the other levels have a data role added on
                assertNull(existingLevel.getDataRole());
              }
            }
          }
        }
      }
    }

    verify(mockTable1);
    verify(mockLatitudeCol);
    verify(mockLongitudeCol);
    verify(mockCustomerCol);
    verify(mockStateCol);
  }

}
