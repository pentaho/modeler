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

import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.util.XmiParser;

import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class CreateAttributeTest {
  @SuppressWarnings( "unchecked" )
  @Test
  public void testCanCreateHierarchyWithMultipleLevels() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productLine = new CreateAttribute();
    productLine.setName( "Product Line" );
    productLine.setDimension( "Products" );
    productLine.setHierarchy( "Products" );
    productLine.apply( model,  "PRODUCTLINE_OLAP" );

    CreateAttribute productName = new CreateAttribute();
    productName.setName( "Product Name" );
    productName.setParentAttribute( "Product Line" );
    productName.setDimension( "Products" );
    productName.setHierarchy( "Products" );
    productName.apply( model, "PRODUCTNAME_OLAP" );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    assertEquals( 8, dimensionUsages.size() );
    OlapDimensionUsage productsDim = dimensionUsages.get( 7 );
    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    assertEquals( "Product Line", levels.get( 0 ).getName() );
    assertEquals( "Product Name", levels.get( 1 ).getName() );
  }
}
