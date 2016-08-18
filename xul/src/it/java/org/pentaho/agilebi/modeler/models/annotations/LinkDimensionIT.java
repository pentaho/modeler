/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2016 Pentaho Corporation (Pentaho). All rights reserved.
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

import junit.framework.Assert;
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
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
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
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

@SuppressWarnings( "unchecked" )
public class LinkDimensionIT {
  private IMetaStore metaStore;
  private DatabaseMeta dbMeta;

  private static final String GEO_ROLE_PROPERTIES = "src/it/resources/geoRoles.properties";

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
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
  public void testDimensionAndSharedDimensionRequired() throws Exception {
    LinkDimension linkDimension = new LinkDimension();
    try {
      linkDimension.validate();
      fail( "expected Exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Dimension Name is required.", e.getMessage() );
    }
    linkDimension.setName( "anything" );
    try {
      linkDimension.validate();
      fail( "expected Exception" );
    } catch ( ModelerException e ) {
      assertEquals( "Shared Dimension is required.", e.getMessage() );
    }
    linkDimension.setSharedDimension( "aShared Dim" );
    try {
      linkDimension.validate();
    } catch ( ModelerException e ) {
      fail( "should have been valid" );
    }
  }

  @Test
  public void testHasNameAndSharedDimension() throws Exception {
    LinkDimension linkDimension = new LinkDimension();
    assertEquals( ModelAnnotation.Type.LINK_DIMENSION, linkDimension.getType() );
    List<ModelProperty> modelProperties = linkDimension.getModelProperties();
    assertEquals( 3, modelProperties.size() );
    assertEquals( "Dimension Name", modelProperties.get( 0 ).name() );
    assertEquals( "Shared Dimension", modelProperties.get( 1 ).name() );

    linkDimension.setName( "myName" );
    linkDimension.setSharedDimension( "sharedName" );
    assertEquals( "myName", linkDimension.getName() );
    assertEquals( "sharedName", linkDimension.getSharedDimension() );

    assertEquals( "Dimension myName is linked to shared dimension sharedName", linkDimension.getSummary() );
  }

  @Test
  public void testLinksDimensionToModel() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    saveProductToMetastore();
    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "Product Dim" );
    linkDimension.setSharedDimension( "shared product group" );
    linkDimension.setField( "PRODUCT_ID" );
    assertTrue( linkDimension.apply( model, metaStore ) );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 4, dimensionUsages.size() );
    OlapDimensionUsage productDim = dimensionUsages.get( 3 );
    OlapHierarchy productHierarchy = productDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( "PRODUCT", productHierarchy.getLogicalTable().getName( "en_us" ) );

    OlapHierarchyLevel nameLevel = productHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Product", nameLevel.getName() );
    assertEquals( "Name",
        nameLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapHierarchyLevel descriptionLevel = productHierarchy.getHierarchyLevels().get( 1 );
    assertEquals( "Description", descriptionLevel.getName() );
    assertEquals( "Description",
        descriptionLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    assertEquals( 2, cube.getOlapMeasures().size() );
  }

  @Test
  public void testInvalidAnnotationsApplication() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    saveInvalidProductToMetastore();
    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "Product Dim" );
    linkDimension.setSharedDimension( "shared product group" );
    linkDimension.setField( "PRODUCT_ID" );
    assertFalse( "This should fail", linkDimension.apply( model, metaStore ) );
    assertEquals(
      "Dimension Product Dim is linked to shared dimension shared product group\n"
      + "    Unable to apply annotation: Description participates in hierarchy with parent Product\n"
      + "    Unable to apply annotation: Product is top level in hierarchy\n"
      + "    Successfully applied annotation: Id is key for dimension Shared Product dim",
      linkDimension.getSummary() );
  }

  @Test
  public void testSummaryWithNoApply() throws Exception {
    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setField( "keyField" );
    linkDimension.setName( "link dim" );
    linkDimension.setSharedDimension( "shared dim" );
    assertEquals( "Dimension link dim is linked to shared dimension shared dim", linkDimension.getSummary() );
  }

  private void saveInvalidProductToMetastore() throws Exception {
    CreateAttribute productName = new CreateAttribute();
    String sharedDimName = "Shared Product dim";
    productName.setDimension( sharedDimName );
    productName.setName( "Product" );
    productName.setField( "Name-1" );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setDimension( sharedDimName );
    productDescription.setName( "Description" );
    productDescription.setParentAttribute( "Product" );
    productDescription.setField( "Description" );

    CreateDimensionKey productId = new CreateDimensionKey();
    productId.setDimension( sharedDimName );
    productId.setName( "id" );
    productId.setField( "Id" );

    final ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( productDescription ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( productName ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateDimensionKey>( productId ) );
    modelAnnotationGroup.setSharedDimension( true );
    modelAnnotationGroup.setName( "shared product group" );
    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    String metaRef = manager.storeDatabaseMeta( dbMeta, metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "product" );
    dataProvider.setDatabaseMetaNameRef( metaRef );
    ColumnMapping descMapping = new ColumnMapping();
    descMapping.setColumnName( "product_description" );
    descMapping.setName( "Description" );
    ColumnMapping nameMapping = new ColumnMapping();
    nameMapping.setColumnName( "product_name" );
    nameMapping.setName( "Name" );
    ColumnMapping idMapping = new ColumnMapping();
    idMapping.setColumnName( "product_id" );
    idMapping.setName( "Id" );
    dataProvider.setColumnMappings( Arrays.asList( descMapping, nameMapping, idMapping ) );

    modelAnnotationGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( modelAnnotationGroup, metaStore );
  }

  private void saveProductToMetastore() throws Exception {
    CreateAttribute productName = new CreateAttribute();
    String sharedDimName = "Shared Product dim";
    productName.setDimension( sharedDimName );
    productName.setName( "Product" );
    productName.setField( "Name" );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setDimension( sharedDimName );
    productDescription.setName( "Description" );
    productDescription.setParentAttribute( "Product" );
    productDescription.setField( "Description" );

    CreateDimensionKey productId = new CreateDimensionKey();
    productId.setDimension( sharedDimName );
    productId.setName( "id" );
    productId.setField( "Id" );

    final ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( productDescription ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( productName ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateDimensionKey>( productId ) );
    modelAnnotationGroup.setSharedDimension( true );
    modelAnnotationGroup.setName( "shared product group" );
    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    String metaRef = manager.storeDatabaseMeta( dbMeta, metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "product" );
    dataProvider.setDatabaseMetaNameRef( metaRef );
    ColumnMapping descMapping = new ColumnMapping();
    descMapping.setColumnName( "product_description" );
    descMapping.setName( "Description" );
    ColumnMapping nameMapping = new ColumnMapping();
    nameMapping.setColumnName( "product_name" );
    nameMapping.setName( "Name" );
    ColumnMapping idMapping = new ColumnMapping();
    idMapping.setColumnName( "product_id" );
    idMapping.setName( "Id" );
    dataProvider.setColumnMappings( Arrays.asList( descMapping, nameMapping, idMapping ) );

    modelAnnotationGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( modelAnnotationGroup, metaStore );
  }

  @Test
  public void testLinksMultipleDimensionsToModel() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    saveProductAsTwoDimensionsToMetastore();
    LinkDimension linkProduct = new LinkDimension();
    linkProduct.setName( "Product" );
    linkProduct.setSharedDimension( "shared product group" );
    linkProduct.setField( "PRODUCT_ID" );
    assertTrue( linkProduct.apply( model, metaStore ) );

    LinkDimension linkDescription = new LinkDimension();
    linkDescription.setName( "Description" );
    linkDescription.setSharedDimension( "shared description group" );
    linkDescription.setField( "PRODUCT_ID" );
    assertTrue( linkDescription.apply( model, metaStore ) );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 5, dimensionUsages.size() );
    OlapDimensionUsage productDim = dimensionUsages.get( 3 );
    OlapHierarchy productHierarchy = productDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( "PRODUCT", productHierarchy.getLogicalTable().getName( "en_us" ) );

    OlapHierarchyLevel nameLevel = productHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Product", nameLevel.getName() );
    assertEquals( "PRODUCT NAME",
        nameLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapDimensionUsage descriptionDim = dimensionUsages.get( 4 );
    OlapHierarchy descriptionHierarchy = descriptionDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( "PRODUCT", productHierarchy.getLogicalTable().getName( "en_us" ) );
    OlapHierarchyLevel descriptionLevel = descriptionHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Description", descriptionLevel.getName() );
    assertEquals( "PRODUCT DESCRIPTION",
        descriptionLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    assertEquals( 2, cube.getOlapMeasures().size() );
  }

  private void saveProductAsTwoDimensionsToMetastore() throws Exception {
    CreateAttribute productName = new CreateAttribute();
    productName.setDimension( "Product Dim" );
    productName.setName( "Product" );
    productName.setField( "PRODUCT_NAME" );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setDimension( "Description Dim" );
    productDescription.setName( "Description" );
    productDescription.setGeoType( ModelAnnotation.GeoType.State );
    productDescription.setField( "PRODUCT_DESCRIPTION" );

    CreateDimensionKey productId = new CreateDimensionKey();
    productId.setDimension( "Product Dim" );
    productId.setName( "id" );
    productId.setField( "PRODUCT_ID" );

    CreateDimensionKey descriptionId = new CreateDimensionKey();
    descriptionId.setDimension( "Description Dim" );
    descriptionId.setName( "id" );
    descriptionId.setField( "PRODUCT_ID" );

    final ModelAnnotationGroup productGroup = new ModelAnnotationGroup();
    productGroup.add( new ModelAnnotation<CreateAttribute>( productName ) );
    productGroup.add( new ModelAnnotation<CreateDimensionKey>( productId ) );
    productGroup.setSharedDimension( true );
    productGroup.setName( "shared product group" );

    final ModelAnnotationGroup descriptionGroup = new ModelAnnotationGroup();
    descriptionGroup.add( new ModelAnnotation<CreateAttribute>( productDescription ) );
    descriptionGroup.add( new ModelAnnotation<CreateDimensionKey>( descriptionId ) );
    descriptionGroup.setSharedDimension( true );
    descriptionGroup.setName( "shared description group" );
    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    String metaRef = manager.storeDatabaseMeta( dbMeta, metaStore );
    DatabaseMeta decoyMeta = (DatabaseMeta) dbMeta.clone();
    decoyMeta.setName( "other" );
    String decoyRef = manager.storeDatabaseMeta( decoyMeta, metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "product" );
    dataProvider.setDatabaseMetaNameRef( metaRef );

    DataProvider decoyProvider = new DataProvider();
    decoyProvider.setName( "dp2" );
    decoyProvider.setTableName( "faketable" );
    decoyProvider.setDatabaseMetaNameRef( decoyRef );

    productGroup.setDataProviders( Arrays.asList( new DataProvider(), dataProvider, decoyProvider ) );
    descriptionGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( productGroup, metaStore );
    manager.createGroup( descriptionGroup, metaStore );
  }

  @Test
  public void testAutoLevelRemovedWithLinkingFieldToASharedDimension() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    saveDateToMetastore();
    LinkDimension linkDate = new LinkDimension();
    linkDate.setName( "Date" );
    linkDate.setSharedDimension( "shared date group" );
    linkDate.setField( "DATE" );
    assertTrue( linkDate.apply( model, metaStore ) );
    assertTrue( linkDate.apply( model, metaStore ) );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 4, dimensionUsages.size() );
    OlapDimensionUsage dateDim = dimensionUsages.get( 3 );

    assertEquals( 1, dateDim.getOlapDimension().getHierarchies().size() );
    OlapHierarchy dateHierarchy = dateDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( "MYDATE", dateHierarchy.getLogicalTable().getName( "en_us" ) );
    assertEquals( "Date", dateHierarchy.getName() );

    OlapHierarchyLevel yearLevel = dateHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Year", yearLevel.getName() );
    assertEquals( "YEAR",
        yearLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapHierarchyLevel monthLevel = dateHierarchy.getHierarchyLevels().get( 1 );
    assertEquals( "Month", monthLevel.getName() );
    assertEquals( "MONTH",
        monthLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapHierarchyLevel dateLevel = dateHierarchy.getHierarchyLevels().get( 2 );
    assertEquals( "Date", dateLevel.getName() );
    assertEquals( "DATE",
        dateLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );
  }

  private void saveDateToMetastore() throws Exception {
    CreateAttribute year = new CreateAttribute();
    year.setDimension( "DATE" );
    year.setName( "Year" );
    year.setHierarchy( "Date" );
    year.setField( "YEAR" );

    CreateAttribute month = new CreateAttribute();
    month.setDimension( "DATE" );
    month.setName( "Month" );
    month.setParentAttribute( "Year" );
    month.setHierarchy( "Date" );
    month.setField( "MONTH" );

    CreateAttribute day = new CreateAttribute();
    day.setDimension( "DATE" );
    day.setName( "Date" );
    day.setParentAttribute( "Month" );
    day.setHierarchy( "Date" );
    day.setField( "DATE" );

    CreateDimensionKey dateKey = new CreateDimensionKey();
    dateKey.setDimension( "DATE" );
    dateKey.setName( "date" );
    dateKey.setField( "DATE" );

    final ModelAnnotationGroup dateGroup = new ModelAnnotationGroup();
    dateGroup.add( new ModelAnnotation<CreateAttribute>( year ) );
    dateGroup.add( new ModelAnnotation<CreateAttribute>( month ) );
    dateGroup.add( new ModelAnnotation<CreateAttribute>( day ) );
    dateGroup.add( new ModelAnnotation<CreateDimensionKey>( dateKey ) );
    dateGroup.setSharedDimension( true );
    dateGroup.setName( "shared date group" );

    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    String metaRef = manager.storeDatabaseMeta( dbMeta, metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "mydate" );
    dataProvider.setDatabaseMetaNameRef( metaRef );
    dateGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( dateGroup, metaStore );
  }

  @Test
  public void testNoProviderMatchFailsToApply() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    saveBadDbMeta();
    LinkDimension linkDate = new LinkDimension();
    linkDate.setName( "Date" );
    linkDate.setSharedDimension( "shared date group" );
    linkDate.setField( "DATE" );
    assertFalse( linkDate.apply( model, metaStore ) );
  }

  private void saveBadDbMeta() throws Exception {
    CreateAttribute year = new CreateAttribute();
    year.setDimension( "Date" );
    year.setName( "Year" );
    year.setHierarchy( "Date" );
    year.setField( "YEAR" );

    CreateDimensionKey dateKey = new CreateDimensionKey();
    dateKey.setDimension( "Date" );
    dateKey.setName( "date" );
    dateKey.setField( "DATE" );

    final ModelAnnotationGroup dateGroup = new ModelAnnotationGroup();
    dateGroup.add( new ModelAnnotation<CreateAttribute>( year ) );
    dateGroup.add( new ModelAnnotation<CreateDimensionKey>( dateKey ) );
    dateGroup.setSharedDimension( true );
    dateGroup.setName( "shared date group" );

    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    String metaRef = manager.storeDatabaseMeta( new DatabaseMeta(), metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "mydate" );
    dataProvider.setDatabaseMetaNameRef( metaRef );
    dateGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( dateGroup, metaStore );
  }

  @Test
  public void testLinkDimRemovingSelf() throws Exception {

    ModelerWorkspace model = prepareOrderModel();
    CreateDimensionKey key = new CreateDimensionKey();
    key.setDimension( "Shared" );
    key.setField( "PRODUCT_ID" );

    CreateAttribute attr = new CreateAttribute();
    attr.setName( "Product ID" );
    attr.setDimension( "Shared" );
    attr.setHierarchy( "some hierarchy" );
    attr.setField( "PRODUCT_ID" );

    ModelAnnotationGroup sharedDim = new ModelAnnotationGroup(
        new ModelAnnotation<CreateDimensionKey>( key ),
        new ModelAnnotation<CreateAttribute>( attr ) );
    sharedDim.setName( "SharedDim" );
    sharedDim.setSharedDimension( true );
    IMetaStore mstore = new MemoryMetaStore();
    ModelAnnotationManager mgr = new ModelAnnotationManager( true );
    String metaRef = mgr.storeDatabaseMeta( dbMeta, mstore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "orderfact" );
    dataProvider.setDatabaseMetaNameRef( metaRef );
    sharedDim.setDataProviders( Collections.singletonList( dataProvider ) );
    mgr.createGroup( sharedDim, mstore );

    CreateMeasure prodIdMeasure = new CreateMeasure();
    prodIdMeasure.setName( "Product IDs" );
    prodIdMeasure.setAggregateType( AggregationType.COUNT );
    prodIdMeasure.setField( "PRODUCT_ID" );

    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "Product Dim" );
    linkDimension.setSharedDimension( "SharedDim" );
    linkDimension.setField( "PRODUCT_ID" );

    assertTrue( prodIdMeasure.apply( model, mstore ) );
    assertTrue( linkDimension.apply( model, mstore ) );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 4, dimensionUsages.size() );
    OlapDimensionUsage productDim = dimensionUsages.get( 3 );
    OlapHierarchy productHierarchy = productDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( attr.getHierarchy(), productHierarchy.getName() );

    OlapHierarchyLevel idLevel = productHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( attr.getName(), idLevel.getName() );

    assertEquals( 3, cube.getOlapMeasures().size() );
  }

  @Test
  public void testFieldIsHiddenProperty() throws Exception {
    LinkDimension linkDimension = new LinkDimension();
    List<ModelProperty> modelProperties = linkDimension.getModelProperties();
    int assertCount = 0;
    for ( ModelProperty modelProperty : modelProperties ) {
      String id = modelProperty.id();
      if ( LinkDimension.FIELD_ID.equals( id ) ) {
        Assert.assertTrue( modelProperty.hideUI() );
        assertCount++;
      }
    }
    assertEquals( 1, assertCount );
  }

  private ModelerWorkspace prepareOrderModel() throws Exception {
    createOrderfactDB();
    TableModelerSource source = new TableModelerSource( dbMeta, "orderfact", "" );
    Domain domain = source.generateDomain();

    Reader propsReader = new FileReader( new File( GEO_ROLE_PROPERTIES ) );
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

  private void createOrderfactDB() throws Exception {
    dbMeta = newH2Db();
    Database db = new Database( null, dbMeta );
    db.connect();
    db.execStatement( "DROP TABLE IF EXISTS orderfact;" );
    db.execStatement( "DROP TABLE IF EXISTS product;" );
    db.execStatement( "DROP TABLE IF EXISTS mydate;" );
    db.execStatement( "CREATE TABLE orderfact\n"
        + "(\n"
        + "   ordernumber int,\n"
        + "   product_id int,\n"
        + "   quantityordered int\n,"
        + "   date Date"
        + ");\n" );
    db.execStatement( "CREATE TABLE product\n"
        + "(\n"
        + "   product_id int,\n"
        + "   product_name varchar(50),\n"
        + "   product_description varchar(50)\n"
        + ");\n" );
    db.execStatement( "CREATE TABLE mydate\n"
        + "(\n"
        + "   date Date,\n"
        + "   year varchar(50),\n"
        + "   month varchar(50)\n"
        + ");\n" );
    db.disconnect();
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

}
