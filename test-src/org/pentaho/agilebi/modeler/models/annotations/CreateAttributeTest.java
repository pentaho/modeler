/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package org.pentaho.agilebi.modeler.models.annotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

public class CreateAttributeTest {
  private IMetaStore metaStore;

  private static String PRODUCT_XMI_FILE = "test-res/products.xmi";
  private static String GEO_ROLES_PROPERTIES_FILE = "test-res/geoRoles.properties";

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testCanCreateHierarchyWithMultipleLevels() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setHierarchy( "Products" );
    productLine.apply( model, "PRODUCTLINE_OLAP", metaStore );

    CreateAttribute productName = new CreateAttribute();
    productName.setName( "Product Name" );
    productName.setParentAttribute( "Product Line" );
    productName.setDimension( "Products" );
    productName.setHierarchy( "Products" );
    productName.setOrdinalField( "PRODUCTSCALE_OLAP" );
    productName.apply( model, "PRODUCTNAME_OLAP", metaStore );

    CreateAttribute year = new CreateAttribute();
    year.setName( "Year" );
    year.setDimension( "Date" );
    year.setHierarchy( "DateByMonth" );
    year.setTimeType( ModelAnnotation.TimeType.TimeYears );
    year.setTimeFormat( "yyyy" );
    year.apply( model, "PRODUCTCODE_OLAP", metaStore );

    CreateAttribute month = new CreateAttribute();
    month.setName( "Month" );
    month.setParentAttribute( "Year" );
    month.setDimension( "Date" );
    month.setHierarchy( "DateByMonth" );
    month.setOrdinalField( "bc_MSRP" );
    month.setTimeType( ModelAnnotation.TimeType.TimeMonths );
    month.setTimeFormat( "mm" );
    month.apply( model, "PRODUCTDESCRIPTION_OLAP", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 2, cube.getOlapMeasures().size() );

    assertEquals( 5, dimensionUsages.size() );
    OlapDimensionUsage productsDim = dimensionUsages.get( 3 );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, productsDim.getOlapDimension().getType() );
    assertFalse( productsDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    assertEquals( "Product Line", levels.get( 0 ).getName() );
    assertEquals( "Product Name", levels.get( 1 ).getName() );
    assertEquals( "PRODUCTSCALE_OLAP",
        levels.get( 1 ).getReferenceOrdinalColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapDimensionUsage dateDim = dimensionUsages.get( 4 );
    assertEquals( OlapDimension.TYPE_TIME_DIMENSION, dateDim.getOlapDimension().getType() );
    assertTrue( dateDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy dateHierarchy = dateDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> dateLevels = dateHierarchy.getHierarchyLevels();
    assertEquals( "Year", dateLevels.get( 0 ).getName() );
    assertEquals( "TimeYears", dateLevels.get( 0 ).getLevelType() );
    assertEquals( "[yyyy]", dateLevels.get( 0 ).getAnnotations().get( 0 ).getValue() );
    assertEquals( "Month", dateLevels.get( 1 ).getName() );
    assertEquals( "TimeMonths", dateLevels.get( 1 ).getLevelType() );
    assertEquals( "[yyyy].[mm]", dateLevels.get( 1 ).getAnnotations().get( 0 ).getValue() );

  }

  @Test
  public void testSummaryDescribesLevelInHierarchy() throws Exception {
    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setName( "Product Name" );
    createAttribute.setParentAttribute( "Product Category" );
    createAttribute.setHierarchy( "Product" );
    assertEquals(
        "Product Name participates in hierarchy Product with parent Product Category",
        createAttribute.getSummary() );

    CreateAttribute topAttribute = new CreateAttribute();
    topAttribute.setName( "Product Category" );
    topAttribute.setHierarchy( "Product" );
    assertEquals(
        "Product Category is top level in hierarchy Product",
        topAttribute.getSummary() );
  }

  @Test
  public void testEmptyHierarchyIsValid() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "Product" );
    productCode.apply( model, "PRODUCTCODE_OLAP", metaStore );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setName( "Product Description" );
    productDescription.setParentAttribute( "Product Code" );
    productDescription.setDimension( "Product" );
    productDescription.apply( model, "PRODUCTDESCRIPTION_OLAP", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 8, dimensionUsages.size() );
    OlapDimensionUsage dateDim = dimensionUsages.get( 7 );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, dateDim.getOlapDimension().getType() );
    OlapHierarchy dateHierarchy = dateDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> dateLevels = dateHierarchy.getHierarchyLevels();
    assertEquals( "Product Code", dateLevels.get( 0 ).getName() );
    assertEquals( "Product Description", dateLevels.get( 1 ).getName() );

    assertEquals( "Product Code is top level in hierarchy", productCode.getSummary() );
    assertEquals( "Product Description participates in hierarchy with parent Product Code",
        productDescription.getSummary() );
  }

  @Test
  public void testValidate() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setName( "A" );
    createAttribute.setDimension( "ADim" );
    createAttribute.validate(); // no error

    try {
      createAttribute.setDimension( "" );
      createAttribute.setParentAttribute( "parent" );
      createAttribute.validate(); // throws an error
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }

    createAttribute.setDimension( "dimension" );
    createAttribute.validate(); // no error

    try {
      ( new CreateAttribute() ).validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    Props.init( 0 );
  }

  @Test
  public void testCreateGeoDimensionAndRemovesAutoGeo() throws Exception {
    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute country = new CreateAttribute();
    country.setName( "Country" );
    country.setDimension( "Geo" );
    country.setGeoType( ModelAnnotation.GeoType.Country );
    country.apply( model, "Country", metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setParentAttribute( "Country" );
    state.setDimension( "Geo" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.apply( model, "STATE", metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geo" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.apply( model, "CITY", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = dimensionUsages.get( 2 );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = levels.get( 0 );
    assertEquals( "Country", countryLevel.getName() );
    assertAnnotation( countryLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( countryLevel.getAnnotations().get( 1 ), "Geo.Role", "country" );

    OlapHierarchyLevel stateLevel = levels.get( 1 );
    assertEquals( "State", stateLevel.getName() );
    assertAnnotation( stateLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( stateLevel.getAnnotations().get( 1 ), "Geo.Role", "state" );
    assertAnnotation( stateLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country" );

    OlapHierarchyLevel cityLevel = levels.get( 2 );
    assertEquals( "City", cityLevel.getName() );
    assertAnnotation( cityLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( cityLevel.getAnnotations().get( 1 ), "Geo.Role", "city" );
    assertAnnotation( cityLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country,state" );

  }

  @Test
  public void testCreateMultipleGeoDimensionAndRemovesAutoGeo() throws Exception {

    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute country = new CreateAttribute();
    country.setName( "Country" );
    country.setDimension( "Geography" );
    country.setGeoType( ModelAnnotation.GeoType.Country );
    country.setHierarchy( "Geo" );
    country.apply( model, "Country", metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setParentAttribute( "Country" );
    state.setDimension( "Geography" );
    state.setHierarchy( "Geo" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.apply( model, "STATE", metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geography" );
    city.setHierarchy( "Geo" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.apply( model, "CITY", metaStore );

    // Test additional hierarchy
    country.setName( "MyCountry" );
    country.setDimension( "MyGeography" );
    country.setHierarchy( "MyGeo" );
    country.apply( model, "Country", metaStore );

    state.setName( "MyState" );
    state.setParentAttribute( "MyCountry" );
    state.setDimension( "MyGeography" );
    state.setHierarchy( "MyGeo" );
    state.apply( model, "STATE", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = dimensionUsages.get( 2 );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = levels.get( 0 );
    assertEquals( "Country", countryLevel.getName() );

    OlapHierarchyLevel stateLevel = levels.get( 1 );
    assertEquals( "State", stateLevel.getName() );

    OlapHierarchyLevel cityLevel = levels.get( 2 );
    assertEquals( "City", cityLevel.getName() );

    // Test second hierarchy
    geoDim = dimensionUsages.get( 3 );
    hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    levels = hierarchy.getHierarchyLevels();

    countryLevel = levels.get( 0 );
    assertEquals( "MyCountry", countryLevel.getName() );

    stateLevel = levels.get( 1 );
    assertEquals( "MyState", stateLevel.getName() );
  }

  private ModelerWorkspace prepareGeoModel() throws Exception {
    DatabaseMeta dbMeta = createGeoTable();
    TableModelerSource source = new TableModelerSource( dbMeta, "geodata", "" );
    Domain domain = source.generateDomain();

    Reader propsReader = new FileReader( new File( GEO_ROLES_PROPERTIES_FILE ) );
    Properties props = new Properties();
    props.load( propsReader );
    GeoContextConfigProvider config = new GeoContextPropertiesProvider( props );
    GeoContext geoContext = GeoContextFactory.create( config );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ), geoContext );
    model.setModelSource( source );
    model.setDomain( domain );
    model.setModelName( "someModel" );
    model.getWorkspaceHelper().autoModelFlat( model );
    model.getWorkspaceHelper().populateDomain( model );
    return model;
  }

  @Test
  public void testCreateMultipleGeoDimensions() throws Exception {
    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute country = new CreateAttribute();
    country.setName( "Country" );
    country.setDimension( "Geo" );
    country.setGeoType( ModelAnnotation.GeoType.Country );
    country.apply( model, "Country", metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setDimension( "Geo2" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.apply( model, "STATE", metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geo2" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.apply( model, "CITY", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = dimensionUsages.get( 2 );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = levels.get( 0 );
    assertEquals( "Country", countryLevel.getName() );
    assertAnnotation( countryLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( countryLevel.getAnnotations().get( 1 ), "Geo.Role", "country" );

    OlapDimensionUsage geoDim2 = dimensionUsages.get( 3 );
    OlapHierarchy hierarchy2 = geoDim2.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels2 = hierarchy2.getHierarchyLevels();
    OlapHierarchyLevel stateLevel = levels2.get( 0 );
    assertEquals( "State", stateLevel.getName() );
    assertAnnotation( stateLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( stateLevel.getAnnotations().get( 1 ), "Geo.Role", "state" );
    assertAnnotation( stateLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country" );

    OlapHierarchyLevel cityLevel = levels2.get( 1 );
    assertEquals( "City", cityLevel.getName() );
    assertAnnotation( cityLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( cityLevel.getAnnotations().get( 1 ), "Geo.Role", "city" );
    assertAnnotation( cityLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country,state" );

  }

  private DatabaseMeta createGeoTable() throws Exception {
    DatabaseMeta dbMeta = newH2Db();
    Database db = new Database( null, dbMeta );
    db.connect();
    db.execStatement( "DROP TABLE if exists geodata;" );
    db.execStatement( "CREATE TABLE geodata\n"
        + "(\n"
        + "  state_fips bigint\n"
        + ", state varchar(25)\n"
        + ", state_abbr varchar(4)\n"
        + ", zipcode varchar(10)\n"
        + ", country varchar(45)\n"
        + ", city varchar(45)\n"
        + ");\n" );
    db.disconnect();
    return dbMeta;

  }

  private DatabaseMeta newH2Db() {
    // DB Setup
    String dbDir = "bin/test/DswModelerTest-H2-DB";
    File file = new File( dbDir + ".h2.db" );
    if ( file.exists() ) {
      file.delete();
    }
    DatabaseMeta dbMeta = new DatabaseMeta( "myh2", "HYPERSONIC", "Native", null, dbDir, null, "sa", null );
    return dbMeta;
  }

  private void assertAnnotation( final OlapAnnotation olapAnnotation, final String name, final String value ) {
    assertEquals( name, olapAnnotation.getName() );
    assertEquals( value, olapAnnotation.getValue() );
  }

  @Test
  public void testNoExceptionWithOrdinalSameAsColumn() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute month = new CreateAttribute();
    month.setName( "MonthDesc" );
    month.setDimension( "DIM TIME" );
    month.setHierarchy( "Time" );
    month.setUnique( false );
    month.setOrdinalField( "PRODUCTCODE_OLAP" );
    month.setTimeType( ModelAnnotation.TimeType.TimeMonths );
    month.setTimeFormat( "MMM" );
    month.apply( model, "PRODUCTCODE_OLAP", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage timeDim = dimensionUsages.get( 8 );
    assertEquals( OlapDimension.TYPE_TIME_DIMENSION, timeDim.getOlapDimension().getType() );
    OlapHierarchy timeHierarchy = timeDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel monthLevel = timeHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "PRODUCTCODE_OLAP",
        monthLevel.getReferenceOrdinalColumn().getName( model.getWorkspaceHelper().getLocale() ) );
    assertEquals( "PRODUCTCODE_OLAP",
        monthLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );
  }

  @Test
  public void testUsingHierarchyWithSameNameWillOverwrite() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute month = new CreateAttribute();
    month.setName( "MonthDesc" );
    month.setDimension( "DIM TIME" );
    month.setHierarchy( "Time" );
    month.apply( model, "PRODUCTCODE_OLAP", metaStore );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "DIM TIME" );
    productCode.setHierarchy( "Time" );
    productCode.apply( model, "PRODUCTCODE_OLAP", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage timeDim = dimensionUsages.get( 8 );
    OlapHierarchy timeHierarchy = timeDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel codeLevel = timeHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Product Code", codeLevel.getName() );
    assertEquals( "PRODUCTCODE_OLAP",
        codeLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );
  }

  @Test
  public void testBeautifiedNamesAreHandled() throws Exception {
    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute abbr = new CreateAttribute();
    abbr.setName( "State Abbr" );
    abbr.setDimension( "dim" );
    abbr.apply( model, "STATE_ABBR", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage stateDim = dimensionUsages.get( 2 );
    OlapHierarchy hierarchy = stateDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel level = hierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "State Abbr", level.getName() );
  }
}
