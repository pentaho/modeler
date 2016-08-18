/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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
import static junit.framework.Assert.fail;
import static org.pentaho.agilebi.modeler.models.annotations.CreateAttribute.*;

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
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation.TimeType;
import org.pentaho.agilebi.modeler.models.annotations.data.InlineFormatAnnotation;
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

public class CreateAttributeIT {
  private IMetaStore metaStore;

  private static String PRODUCT_XMI_FILE = "src/it/resources/products.xmi";
  private static String GEO_ROLES_PROPERTIES_FILE = "src/it/resources/geoRoles.properties";

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
    productLine.setField( "PRODUCTLINE_OLAP" );
    productLine.apply( model, metaStore );

    CreateAttribute productName = new CreateAttribute();
    productName.setName( "Product Name" );
    productName.setParentAttribute( "Product Line" );
    productName.setDimension( "Products" );
    productName.setHierarchy( "Products" );
    productName.setField( "PRODUCTNAME_OLAP" );
    productName.setOrdinalField( "PRODUCTSCALE_OLAP" );
    productName.apply( model, metaStore );

    CreateAttribute year = new CreateAttribute();
    year.setName( "Year" );
    year.setDimension( "Date" );
    year.setHierarchy( "DateByMonth" );
    year.setTimeType( ModelAnnotation.TimeType.TimeYears );
    year.setTimeFormat( "yyyy" );
    year.setField( "PRODUCTCODE_OLAP" );
    year.apply( model, metaStore );

    CreateAttribute month = new CreateAttribute();
    month.setName( "Month" );
    month.setParentAttribute( "Year" );
    month.setDimension( "Date" );
    month.setHierarchy( "DateByMonth" );
    month.setOrdinalField( "bc_MSRP" );
    month.setTimeType( ModelAnnotation.TimeType.TimeMonths );
    month.setTimeFormat( "mm" );
    month.setField( "PRODUCTDESCRIPTION_OLAP" );
    month.apply( model, metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 2, cube.getOlapMeasures().size() );

    assertEquals( 5, dimensionUsages.size() );
    OlapDimensionUsage productsDim = AnnotationUtil.getOlapDimensionUsage( "Products", dimensionUsages );
    assertNotNull( productsDim );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, productsDim.getOlapDimension().getType() );
    assertFalse( productsDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    OlapHierarchyLevel productLineLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Line", levels );
    assertNotNull( productLineLevel );
    assertEquals( "Product Line", productLineLevel.getName() );
    assertFalse( productLineLevel.isHidden() );

    OlapHierarchyLevel productNameLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Name", levels );
    assertNotNull( productNameLevel );
    assertEquals( "Product Name", productNameLevel.getName() );
    assertEquals( "PRODUCTSCALE_OLAP",
        productNameLevel.getReferenceOrdinalColumn().getName( model.getWorkspaceHelper().getLocale() ) );
    assertFalse( productNameLevel.isHidden() );

    OlapDimensionUsage dateDim = AnnotationUtil.getOlapDimensionUsage( "Date", dimensionUsages );
    assertEquals( OlapDimension.TYPE_TIME_DIMENSION, dateDim.getOlapDimension().getType() );
    assertTrue( dateDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy dateHierarchy = dateDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> dateLevels = dateHierarchy.getHierarchyLevels();
    OlapHierarchyLevel yearLevel = AnnotationUtil.getOlapHierarchyLevel( "Year", dateLevels );
    assertNotNull( yearLevel );
    assertEquals( "Year", yearLevel.getName() );
    assertEquals( "TimeYears", yearLevel.getLevelType() );
    assertEquals( "[yyyy]", yearLevel.getAnnotations().get( 0 ).getValue() );
    assertFalse( yearLevel.isHidden() );

    OlapHierarchyLevel monthLevel = AnnotationUtil.getOlapHierarchyLevel( "Month", dateLevels );
    assertNotNull( monthLevel );
    assertEquals( "Month", monthLevel.getName() );
    assertEquals( "TimeMonths", monthLevel.getLevelType() );
    assertEquals( "[yyyy].[mm]", monthLevel.getAnnotations().get( 0 ).getValue() );
    assertFalse( monthLevel.isHidden() );
  }

  @SuppressWarnings( { "unchecked", "ConstantConditions" } )
  @Test
  public void testCanCreateMultipleHierarchiesInDimensionWithDefaultHierarchy() throws Exception {
    ModelerWorkspace model =
      new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setHierarchy( "Products" );
    productLine.setField( "PRODUCTLINE_OLAP" );
    productLine.apply( model, metaStore );

    CreateAttribute productName = new CreateAttribute();
    productName.setName( "Product Name" );
    productName.setDimension( "Products" );
    productName.setHierarchy( "Product Name" );
    productName.setField( "PRODUCTNAME_OLAP" );
    productName.setOrdinalField( "PRODUCTSCALE_OLAP" );
    productName.apply( model, metaStore );


    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );

    OlapDimensionUsage productsDim = AnnotationUtil.getOlapDimensionUsage( "Products", cube.getOlapDimensionUsages() );
    assertEquals( 2, productsDim.getOlapDimension().getHierarchies().size() );

    OlapHierarchy productsHierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel productLineLevel =
      AnnotationUtil.getOlapHierarchyLevel( "Product Line", productsHierarchy.getHierarchyLevels() );
    assertEquals( "Product Line", productLineLevel.getName() );

    OlapHierarchy productNameHierarchy = productsDim.getOlapDimension().getHierarchies().get( 1 );
    OlapHierarchyLevel productNameLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Name",
      productNameHierarchy.getHierarchyLevels() );
    assertEquals( "Product Name", productNameLevel.getName() );
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

    assertEquals( "PRODUCTCODE",
        AnnotationUtil.getOlapDimensionUsage( "PRODUCTCODE", getCubes( model ).get( 0 ).getOlapDimensionUsages() )
            .getOlapDimension().getHierarchies().get( 0 ).getName() );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "Product" );
    productCode.setField( "PRODUCTCODE_OLAP" );
    productCode.apply( model, metaStore );

    assertEquals( "Product",
        AnnotationUtil.getOlapDimensionUsage( "Product", getCubes( model ).get( 0 ).getOlapDimensionUsages() )
            .getOlapDimension().getHierarchies().get( 0 ).getName() ); // should be same as Dimension name

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setName( "Product Description" );
    productDescription.setParentAttribute( "Product Code" );
    productDescription.setDimension( "Product" );
    productDescription.setField( "PRODUCTDESCRIPTION_OLAP" );
    productDescription.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 8, dimensionUsages.size() );
    OlapDimensionUsage dateDim = dimensionUsages.get( 7 );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, dateDim.getOlapDimension().getType() );
    OlapHierarchy dateHierarchy = dateDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> dateLevels = dateHierarchy.getHierarchyLevels();

    OlapHierarchyLevel productCodeLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Code", dateLevels );
    assertNotNull( productCodeLevel );
    assertEquals( "Product Code", dateLevels.get( 0 ).getName() );
    assertEquals( "Product", productCodeLevel.getOlapHierarchy().getName() );

    OlapHierarchyLevel productDescLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Description", dateLevels );
    assertNotNull( productDescLevel );
    assertEquals( "Product Description", productDescLevel.getName() );
    assertEquals( "Product", productDescLevel.getOlapHierarchy().getName() );

    assertEquals( "Product Code is top level in hierarchy", productCode.getSummary() );
    assertEquals( "Product Description participates in hierarchy with parent Product Code",
        productDescription.getSummary() );
  }

  @Test
  public void testInvalidColumn() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "Productz" );
    productCode.setField( "PRODUCTCODE_OLAP-1" );
    boolean applied = productCode.apply( model, metaStore );
    assertFalse( "This should not be applied", applied );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setName( "Product Description" );
    productDescription.setDimension( "Product" );
    productDescription.setField( "PRODUCTCODE_OLAP" );
    applied = productDescription.apply( model, metaStore );
    assertTrue( "This should be applied", applied );

  }

  @Test
  public void testValidate() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setName( "A" );
    createAttribute.setDimension( "ADim" );
    createAttribute.setField( "Field" );
    createAttribute.validate(); // no error


    createAttribute.setField( "" );
    createAttribute.setLevel( "[Dimension].[Level]" );
    createAttribute.setCube( "Cube" );
    createAttribute.validate(); // no error

    try {
      createAttribute.setLevel( "" );
      createAttribute.validate(); // throws an error
      fail( "no exception" );
    } catch ( ModelerException me ) {
      assertEquals( "Field name or level name is required.", me.getMessage() );
    }

    try {
      createAttribute.setLevel( "[Dimension].[Level]" );
      createAttribute.setCube( "" );
      createAttribute.validate(); // throws an error
      fail( "no exception" );
    } catch ( ModelerException me ) {
      assertEquals( "Field name or level name is required.", me.getMessage() );
    }

    try {
      createAttribute.setCube( "Cube" );
      createAttribute.setDimension( "" );
      createAttribute.setParentAttribute( "parent" );
      createAttribute.validate(); // throws an error
      fail( "no exception" );
    } catch ( ModelerException me ) {
      assertEquals( "Dimension name is required.", me.getMessage() );
    }

    try {
      createAttribute.setDimension( "dimension" );
      createAttribute.validate(); // no error
    } catch ( ModelerException me ) {
      fail( "Exception" );
    }

    try {
      ( new CreateAttribute() ).validate();
      fail( "no exception" );
    } catch ( ModelerException me ) {
      assertEquals( "Attribute Name is required.", me.getMessage() );
    }
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void testCreateGeoDimensionAndRemovesAutoGeo() throws Exception {
    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute country = new CreateAttribute();
    country.setName( "Country" );
    country.setDimension( "Geo" );
    country.setGeoType( ModelAnnotation.GeoType.Country );
    country.setField( "Country" );
    country.apply( model, metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setParentAttribute( "Country" );
    state.setDimension( "Geo" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.setField( "STATE" );
    state.apply( model, metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geo" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.setField( "CITY" );
    city.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = dimensionUsages.get( 2 );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = AnnotationUtil.getOlapHierarchyLevel( "Country", levels );
    assertNotNull( countryLevel );
    assertEquals( "Country", countryLevel.getName() );
    assertAnnotation( countryLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( countryLevel.getAnnotations().get( 1 ), "Geo.Role", "country" );

    OlapHierarchyLevel stateLevel = AnnotationUtil.getOlapHierarchyLevel( "State", levels );
    assertNotNull( stateLevel );
    assertEquals( "State", stateLevel.getName() );
    assertAnnotation( stateLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( stateLevel.getAnnotations().get( 1 ), "Geo.Role", "state" );
    assertAnnotation( stateLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country" );

    OlapHierarchyLevel cityLevel = AnnotationUtil.getOlapHierarchyLevel( "City", levels );
    assertNotNull( cityLevel );
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
    country.setField( "Country" );
    country.apply( model, metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setParentAttribute( "Country" );
    state.setDimension( "Geography" );
    state.setHierarchy( "Geo" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.setField( "STATE" );
    state.apply( model, metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geography" );
    city.setHierarchy( "Geo" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.setField( "CITY" );
    city.apply( model, metaStore );

    // Test additional hierarchy
    country.setName( "MyCountry" );
    country.setDimension( "MyGeography" );
    country.setHierarchy( "MyGeo" );
    country.setField( "Country" );
    country.apply( model, metaStore );

    state.setName( "MyState" );
    state.setParentAttribute( "MyCountry" );
    state.setDimension( "MyGeography" );
    state.setHierarchy( "MyGeo" );
    state.setField( "STATE" );
    state.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = AnnotationUtil.getOlapDimensionUsage( "Geography", dimensionUsages );
    assertNotNull( geoDim );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = AnnotationUtil.getOlapHierarchyLevel( "Country", levels );
    assertNotNull( countryLevel );
    assertEquals( "Country", countryLevel.getName() );

    OlapHierarchyLevel stateLevel = AnnotationUtil.getOlapHierarchyLevel( "State", levels );
    assertNotNull( stateLevel );
    assertEquals( "State", stateLevel.getName() );


    OlapHierarchyLevel cityLevel = AnnotationUtil.getOlapHierarchyLevel( "City", levels );
    assertNotNull( cityLevel );
    assertEquals( "City", cityLevel.getName() );

    // Test second hierarchy
    geoDim = AnnotationUtil.getOlapDimensionUsage( "MyGeography", dimensionUsages );
    hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    levels = hierarchy.getHierarchyLevels();

    countryLevel = AnnotationUtil.getOlapHierarchyLevel( "MyCountry", levels );
    assertNotNull( countryLevel );
    assertEquals( "MyCountry", countryLevel.getName() );

    stateLevel = AnnotationUtil.getOlapHierarchyLevel( "MyState", levels );
    assertNotNull( stateLevel );
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
    country.setField( "Country" );
    country.apply( model, metaStore );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setDimension( "Geo2" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.setField( "STATE" );
    state.apply( model, metaStore );

    CreateAttribute city = new CreateAttribute();
    city.setName( "City" );
    city.setParentAttribute( "State" );
    city.setDimension( "Geo2" );
    city.setGeoType( ModelAnnotation.GeoType.City );
    city.setField( "CITY" );
    city.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage geoDim = AnnotationUtil.getOlapDimensionUsage( "Geo", dimensionUsages );
    assertNotNull( geoDim );
    OlapHierarchy hierarchy = geoDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();

    OlapHierarchyLevel countryLevel = AnnotationUtil.getOlapHierarchyLevel( "Country", levels );
    assertNotNull( countryLevel );
    assertEquals( "Country", countryLevel.getName() );
    assertAnnotation( countryLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( countryLevel.getAnnotations().get( 1 ), "Geo.Role", "country" );

    OlapDimensionUsage geoDim2 = AnnotationUtil.getOlapDimensionUsage( "Geo2", dimensionUsages );
    assertNotNull( geoDim2 );
    OlapHierarchy hierarchy2 = geoDim2.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels2 = hierarchy2.getHierarchyLevels();
    OlapHierarchyLevel stateLevel = AnnotationUtil.getOlapHierarchyLevel( "State", levels2 );
    assertNotNull( stateLevel );
    assertEquals( "State", stateLevel.getName() );
    assertAnnotation( stateLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( stateLevel.getAnnotations().get( 1 ), "Geo.Role", "state" );
    assertAnnotation( stateLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "country" );

    OlapHierarchyLevel cityLevel = AnnotationUtil.getOlapHierarchyLevel( "City", levels2 );
    assertNotNull( cityLevel );
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

  private DatabaseMeta newH2Db( String ... statements ) throws Exception {
    DatabaseMeta dbMeta = newH2Db();
    Database db = new Database( null, dbMeta );
    db.connect();
    for ( String stmt : statements ) {
      db.execStatement( stmt );
    }
    db.disconnect();
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
    month.setField( "PRODUCTCODE_OLAP" );
    month.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage timeDim = AnnotationUtil.getOlapDimensionUsage( "DIM TIME", dimensionUsages );
    assertNotNull( timeDim );
    assertEquals( OlapDimension.TYPE_TIME_DIMENSION, timeDim.getOlapDimension().getType() );
    OlapHierarchy timeHierarchy = timeDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel monthLevel = AnnotationUtil.getOlapHierarchyLevel( "MonthDesc",
        timeHierarchy.getHierarchyLevels() );
    assertNotNull( monthLevel );
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
    month.setField( "PRODUCTCODE_OLAP" );
    month.apply( model, metaStore );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "DIM TIME" );
    productCode.setHierarchy( "Time" );
    productCode.setField( "PRODUCTCODE_OLAP" );
    productCode.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage timeDim = AnnotationUtil.getOlapDimensionUsage( "DIM TIME", dimensionUsages );
    assertNotNull( timeDim );
    OlapHierarchy timeHierarchy = timeDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel codeLevel = AnnotationUtil.getOlapHierarchyLevel( "Product Code",
        timeHierarchy.getHierarchyLevels() );
    assertNotNull( codeLevel );
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
    abbr.setField( "STATE_ABBR" );
    abbr.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage stateDim = AnnotationUtil.getOlapDimensionUsage( "dim", dimensionUsages );
    assertNotNull( stateDim );
    OlapHierarchy hierarchy = stateDim.getOlapDimension().getHierarchies().get( 0 );
    OlapHierarchyLevel level = AnnotationUtil.getOlapHierarchyLevel( "State Abbr", hierarchy.getHierarchyLevels() );
    assertNotNull( level );
    assertEquals( "State Abbr", level.getName() );
    assertFalse( level.isHidden() );
  }

  @Test
  public void testResolveFieldFromLevel() throws Exception {
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

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 3, cube.getOlapMeasures().size() );

    assertEquals( 9, dimensionUsages.size() );
    OlapDimensionUsage productsDim = AnnotationUtil.getOlapDimensionUsage( "Products", dimensionUsages );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, productsDim.getOlapDimension().getType() );
    assertFalse( productsDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    OlapHierarchyLevel level = AnnotationUtil.getOlapHierarchyLevel( "Product Line", hierarchy.getHierarchyLevels() );
    assertNotNull( level );
    assertEquals( "Product Line", level.getName() );
    assertFalse( level.isHidden() );
  }

  /**
   * Time dimension with same name as a field not getting marked as time dimension.
   */
  @Test
  public void testTimeDimensionSetAfterAutoModel() throws Exception {
    ModelerWorkspace wspace = new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ) );
    DatabaseMeta dbMeta = newH2Db( "DROP TABLE if exists datetable;",
        "CREATE TABLE datetable\n"
            + "(\n"
            + "\"date\" TIMESTAMP\n"
            + ");\n" );
    TableModelerSource source = new TableModelerSource( dbMeta, "datetable", "" );
    Domain domain = source.generateDomain();
    wspace.setModelSource( source );
    wspace.setDomain( domain );
    wspace.setModelName( "DateModel" );
    wspace.getWorkspaceHelper().autoModelFlat( wspace );
    wspace.getWorkspaceHelper().populateDomain( wspace );

    CreateAttribute createAttr = new CreateAttribute();
    createAttr.setName( "Date" );
    createAttr.setTimeType( TimeType.TimeDays );
    createAttr.setDimension( "Date" );
    createAttr.setHierarchy( "Date" );
    createAttr.setField( "date" );
    createAttr.apply( wspace, new MemoryMetaStore() );

    OlapDimension dateDim =
        getCubes( wspace ).get( 0 ).getOlapDimensionUsages().get( 0 ).getOlapDimension();
    assertEquals( "Date", dateDim.getName() );
    assertTrue( "time dimension not set", dateDim.isTimeDimension() );
  }

  @Test
  public void testAttributeDescription() throws Exception {
    ModelerWorkspace wspace =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ) );
    wspace.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    wspace.getWorkspaceHelper().populateDomain( wspace );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setHierarchy( "Products" );
    productLine.setDescription( "a line of products" );
    productLine.setField( "PRODUCTLINE_OLAP" );
    productLine.setHidden( true );

    productLine.apply( wspace, metaStore );

    boolean foundDim = false;
    for ( OlapDimensionUsage dimUse : getCubes( wspace ).get( 0 ).getOlapDimensionUsages() ) {
      if ( dimUse.getName().equals( productLine.getDimension() ) ) {
        foundDim = true;
        OlapHierarchyLevel prodLineLvl =
            dimUse.getOlapDimension().getHierarchies().get( 0 ).getHierarchyLevels().get( 0 );
        assertEquals( 1, prodLineLvl.getAnnotations().size() );
        OlapAnnotation desc = prodLineLvl.getAnnotations().get( 0 );
        assertEquals( "description.en_US", desc.getName() );
        assertEquals( productLine.getDescription(), desc.getValue() );
        assertTrue( prodLineLvl.isHidden() );
        break;
      }
    }
    assertTrue( foundDim );
  }

  @Test
  public void testAttributeFormatString() throws Exception {
    ModelerWorkspace wspace =
      new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ) );
    wspace.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    wspace.getWorkspaceHelper().populateDomain( wspace );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setField( "PRODUCTLINE_OLAP" );
    productLine.setFormatString( "mm-dd-yyyy" );

    productLine.apply( wspace, metaStore );

    boolean foundDim = false;
    for ( OlapDimensionUsage dimUse : getCubes( wspace ).get( 0 ).getOlapDimensionUsages() ) {
      if ( dimUse.getName().equals( productLine.getDimension() ) ) {
        foundDim = true;
        OlapHierarchyLevel prodLineLvl =
          dimUse.getOlapDimension().getHierarchies().get( 0 ).getHierarchyLevels().get( 0 );
        assertEquals( 1, prodLineLvl.getAnnotations().size() );
        OlapAnnotation formatAnnotation = prodLineLvl.getAnnotations().get( 0 );
        assertEquals( InlineFormatAnnotation.INLINE_MEMBER_FORMAT_STRING, formatAnnotation.getName() );
        break;
      }
    }
    assertTrue( foundDim );
  }

  /**
   * <a href="http://jira.pentaho.com/browse/BACKLOG-3219">BACKLOG-3219</a>
   */
  @Test
  public void testGeoAttributeTriggersStackOverflow() throws Exception {
    ModelerWorkspace model = prepareGeoModel();

    CreateAttribute state = new CreateAttribute();
    state.setName( "STATE" );
    state.setDimension( "Geography" );
    state.setHierarchy( "Geography" );
    state.setParentAttribute( "CITY" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.setField( "STATE" );

    // threw StackOverflowError from validation loop
    state.apply( model, metaStore );
  }

  @SuppressWarnings( "unchecked" )
  private List<OlapCube> getCubes( ModelerWorkspace wspace ) {
    return (List<OlapCube>) wspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty(
        LogicalModel.PROPERTY_OLAP_CUBES );
  }

}

