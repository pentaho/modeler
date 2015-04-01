/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
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
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LinkDimensionTest {
  private IMetaStore metaStore;
  private DatabaseMeta dbMeta;

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    Props.init( 0 );
  }

  @Test
  public void testHasNameAndSharedDimension() throws Exception {
    LinkDimension linkDimension = new LinkDimension();
    assertEquals( ModelAnnotation.Type.LINK_DIMENSION, linkDimension.getType() );
    List<ModelProperty> modelProperties = linkDimension.getModelProperties();
    assertEquals( 2, modelProperties.size() );
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
    saveSharedDimensionToMetastore();
    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "Product Dim" );
    linkDimension.setSharedDimension( "shared product group" );
    assertTrue( linkDimension.apply( model, "PRODUCT_ID", metaStore ) );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 3, dimensionUsages.size() );
    OlapDimensionUsage productDim = dimensionUsages.get( 2 );
    OlapHierarchy productHierarchy = productDim.getOlapDimension().getHierarchies().get( 0 );
    assertEquals( "PRODUCT", productHierarchy.getLogicalTable().getName( "en_us" ) );

    OlapHierarchyLevel nameLevel = productHierarchy.getHierarchyLevels().get( 0 );
    assertEquals( "Product", nameLevel.getName() );
    assertEquals( "PRODUCT NAME",
        nameLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    OlapHierarchyLevel descriptionLevel = productHierarchy.getHierarchyLevels().get( 1 );
    assertEquals( "Description", descriptionLevel.getName() );
    assertEquals( "PRODUCT DESCRIPTION",
        descriptionLevel.getReferenceColumn().getName( model.getWorkspaceHelper().getLocale() ) );

    assertEquals( 2, cube.getOlapMeasures().size() );
  }

  private void saveSharedDimensionToMetastore() throws Exception {
    CreateAttribute productName = new CreateAttribute();
    String sharedDimName = "Shared Product dim";
    productName.setDimension( sharedDimName );
    productName.setName( "Product" );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setDimension( sharedDimName );
    productDescription.setName( "Description" );
    productDescription.setParentAttribute( "Product" );

    CreateDimensionKey productId = new CreateDimensionKey();
    productId.setDimension( sharedDimName );
    productId.setName( "id" );

    final ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( "PRODUCT_DESCRIPTION", productDescription ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateAttribute>( "PRODUCT_NAME", productName ) );
    modelAnnotationGroup.add( new ModelAnnotation<CreateDimensionKey>( "PRODUCT_ID", productId ) );
    modelAnnotationGroup.setSharedDimension( true );
    modelAnnotationGroup.setName( "shared product group" );
    ModelAnnotationManager manager = new ModelAnnotationManager();
    String metaRef = manager.storeDatabaseMeta( dbMeta, metaStore );
    final DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dp" );
    dataProvider.setTableName( "product" );
    dataProvider.setDatabaseMetaNameRef( metaRef );

    modelAnnotationGroup.setDataProviders( Collections.singletonList( dataProvider ) );
    manager.createGroup( modelAnnotationGroup, metaStore );
  }

  private ModelerWorkspace prepareOrderModel() throws Exception {
    createOrderfactDB();
    TableModelerSource source = new TableModelerSource( dbMeta, "orderfact", "" );
    Domain domain = source.generateDomain();

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ) );
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
    db.execStatement( "CREATE TABLE orderfact\n"
        + "(\n"
        + "   ordernumber int,\n"
        + "   product_id int,\n"
        + "   quantityordered int\n"
        + ");\n" );
    db.execStatement( "CREATE TABLE product\n"
        + "(\n"
        + "   product_id int,\n"
        + "   product_name varchar(50),\n"
        + "   product_description varchar(50)\n"
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
