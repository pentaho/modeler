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

import static junit.framework.Assert.assertNotNull;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.util.XmiParser;

import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
    productName.setOrdinalField( "PRODUCTSCALE_OLAP" );
    productName.apply( model, "PRODUCTNAME_OLAP" );

    CreateAttribute year = new CreateAttribute();
    year.setName( "Year" );
    year.setDimension( "Date" );
    year.setHierarchy( "DateByMonth" );
    year.setTimeType( ModelAnnotation.TimeType.TimeYears );
    year.setTimeFormat( "yyyy" );
    year.apply( model,  "PRODUCTCODE_OLAP" );

    CreateAttribute month = new CreateAttribute();
    month.setName( "Month" );
    month.setParentAttribute( "Year" );
    month.setDimension( "Date" );
    month.setHierarchy( "DateByMonth" );
    month.setOrdinalField( "bc_MSRP" );
    month.setTimeType( ModelAnnotation.TimeType.TimeMonths );
    month.setTimeFormat( "mm" );
    month.apply( model, "PRODUCTDESCRIPTION_OLAP" );

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
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute productCode = new CreateAttribute();
    productCode.setName( "Product Code" );
    productCode.setDimension( "Product" );
    productCode.apply( model, "PRODUCTCODE_OLAP" );

    CreateAttribute productDescription = new CreateAttribute();
    productDescription.setName( "Product Description" );
    productDescription.setParentAttribute( "Product Code" );
    productDescription.setDimension( "Product" );
    productDescription.apply( model, "PRODUCTDESCRIPTION_OLAP" );

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
    assertEquals( "Product Description participates in hierarchy with parent Product Code", productDescription.getSummary() );
  }

  @Test
  public void testValidate() throws Exception {

    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setName( "A" );
    createAttribute.validate(); // no error

    try {
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

  @Test
  public void testCanSetGeoType() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    CreateAttribute country = new CreateAttribute();
    country.setName( "Country" );
    country.setDimension( "Geo" );
    country.setGeoType( ModelAnnotation.GeoType.Country );
    country.apply( model, "PRODUCTLINE_OLAP" );

    CreateAttribute state = new CreateAttribute();
    state.setName( "State" );
    state.setParentAttribute( "Country" );
    state.setDimension( "Geo" );
    state.setGeoType( ModelAnnotation.GeoType.State );
    state.apply( model, "PRODUCTNAME_OLAP" );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapDimensionUsage> dimensionUsages = cube.getOlapDimensionUsages();
    OlapDimensionUsage productsDim = dimensionUsages.get( 7 );
    assertEquals( OlapDimension.TYPE_STANDARD_DIMENSION, productsDim.getOlapDimension().getType() );
    assertFalse( productsDim.getOlapDimension().isTimeDimension() );
    OlapHierarchy hierarchy = productsDim.getOlapDimension().getHierarchies().get( 0 );
    List<OlapHierarchyLevel> levels = hierarchy.getHierarchyLevels();
    OlapHierarchyLevel countryLevel = levels.get( 0 );
    assertEquals( "Country", countryLevel.getName() );
    assertAnnotation( countryLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( countryLevel.getAnnotations().get( 1 ), "Geo.Role", "Country" );
    OlapHierarchyLevel stateLevel = levels.get( 1 );
    assertEquals( "State", stateLevel.getName() );
    assertAnnotation( stateLevel.getAnnotations().get( 0 ), "Data.Role", "Geography" );
    assertAnnotation( stateLevel.getAnnotations().get( 1 ), "Geo.Role", "State" );
    assertAnnotation( stateLevel.getAnnotations().get( 2 ), "Geo.RequiredParents", "Country" );
  }

  private void assertAnnotation( final OlapAnnotation olapAnnotation, final String name, final String value ) {
    assertEquals( name, olapAnnotation.getName() );
    assertEquals( value, olapAnnotation.getValue() );
  }
}
