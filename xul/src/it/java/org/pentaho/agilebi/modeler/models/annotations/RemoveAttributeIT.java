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
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.FileInputStream;
import java.util.List;

public class RemoveAttributeIT {
  private IMetaStore metaStore;

  private static String PRODUCT_XMI_FILE = "src/it/resources/products.xmi";

  private static String PRODUCT_LINE_OLAP = "PRODUCTLINE_OLAP";
  private static String PRODUCT_NAME_OLAP = "PRODUCTNAME_OLAP";
  private static String PRODUCT_ORDINAL_OLAP = "PRODUCTSCALE_OLAP";

  private static String AUTOMODEL_PRODUCT_NAME = "PRODUCTNAME";

  private static String DIMENSION_NAME = "Products";
  private static String HIERARCHY_NAME = "Products";
  private static String PRODUCT_LINE = "Remove Product Line";
  private static String PRODUCT_NAME = "Remove Product Name";

  private static String INIT_PRODUCT_LINE_FORMULA = "[" + AUTOMODEL_PRODUCT_NAME + "." + AUTOMODEL_PRODUCT_NAME
    + "].[" + AUTOMODEL_PRODUCT_NAME + "]";
  private static String PRODUCT_LINE_FORMULA = "[" +  DIMENSION_NAME + "." + HIERARCHY_NAME + "].["
    + PRODUCT_LINE + "]";
  private static String PRODUCT_NAME_FORMULA = "[" +  DIMENSION_NAME + "." + HIERARCHY_NAME + "].["
    + PRODUCT_NAME + "]";

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testCanRemoveExistingHierarchy() throws Exception {
    ModelerWorkspace model =
      new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    RemoveAttribute removeProductLine = new RemoveAttribute();
    removeProductLine.setName( AUTOMODEL_PRODUCT_NAME );
    removeProductLine.setLevel( INIT_PRODUCT_LINE_FORMULA );
    removeProductLine.apply( model, metaStore );

    LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertNull( AnnotationUtil.getOlapDimensionUsage( AUTOMODEL_PRODUCT_NAME, dimensionUsages ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testCanRemoveMultipleHierarchies() throws Exception {
    ModelerWorkspace model =
      new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( PRODUCT_LINE );
    productLine.setDimension( DIMENSION_NAME );
    productLine.setHierarchy( HIERARCHY_NAME );
    productLine.setField( PRODUCT_ORDINAL_OLAP );
    productLine.setCube( "products_38GA" );
    productLine.apply( model, metaStore );

    CreateAttribute productName = new CreateAttribute();
    productName.setName( PRODUCT_NAME );
    productName.setParentAttribute( PRODUCT_LINE );
    productName.setDimension( DIMENSION_NAME );
    productName.setHierarchy( HIERARCHY_NAME );
    productName.setOrdinalField( PRODUCT_ORDINAL_OLAP );
    productName.setField( PRODUCT_NAME_OLAP );
    productName.setCube( "products_38GA" );
    productName.apply( model, metaStore );

    LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    int startDimSize = dimensionUsages.size();

    OlapDimensionUsage productsDim = AnnotationUtil.getOlapDimensionUsage( DIMENSION_NAME, dimensionUsages );
    assertNotNull( productsDim );

    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    OlapHierarchyLevel productLineLevel = AnnotationUtil.getOlapHierarchyLevel( PRODUCT_LINE, levels );
    OlapHierarchyLevel productNameLevel = AnnotationUtil.getOlapHierarchyLevel( PRODUCT_NAME, levels );

    assertNotNull( productLineLevel );
    assertNotNull( productNameLevel );

    RemoveAttribute removeProductName = new RemoveAttribute();
    removeProductName.setName( PRODUCT_NAME );
    removeProductName.setLevel( PRODUCT_NAME_FORMULA );
    removeProductName.apply( model, metaStore );

    anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    dimensionUsages = cube.getOlapDimensionUsages();
    productsDim = AnnotationUtil.getOlapDimensionUsage( DIMENSION_NAME, dimensionUsages );
    assertNotNull( productsDim );

    hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    levels = hierarchy.getHierarchyLevels();
    for ( OlapHierarchyLevel level : levels ) {
      assertFalse( PRODUCT_NAME.equals( level.getName() ) );
    }

    RemoveAttribute removeProductLine = new RemoveAttribute();
    removeProductLine.setName( PRODUCT_LINE );
    removeProductLine.setLevel( PRODUCT_LINE_FORMULA );
    removeProductLine.apply( model, metaStore );

    anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    dimensionUsages = cube.getOlapDimensionUsages();

    productsDim = AnnotationUtil.getOlapDimensionUsage( DIMENSION_NAME, dimensionUsages );
    assertNull( productsDim );

    assertEquals( startDimSize - 1, dimensionUsages.size() );
  }

  @Test
  public void testValidate() throws Exception {

    RemoveAttribute removeAttribute = new RemoveAttribute();
    removeAttribute.setName( "A" );
    removeAttribute.setLevel( "[Dimension].[Level]" );

    try {
      removeAttribute.validate(); // no error
    } catch ( ModelerException me ) {
      fail( "Exception" );
    }

    try {
      removeAttribute.setLevel( "" );
      removeAttribute.validate(); // throws an error
      fail( "no exception" );
    } catch ( ModelerException me ) {
    }

    try {
      removeAttribute.setLevel( "[Dimension].[Level]" );
      removeAttribute.setName( "" );
      removeAttribute.validate();
    } catch ( ModelerException me ) {
      // Name is not needed on a remove
      fail( "Exception" );
    }

    try {
      ( new RemoveAttribute() ).validate();
      fail( "no exception" );
    } catch ( ModelerException me ) {
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
}
