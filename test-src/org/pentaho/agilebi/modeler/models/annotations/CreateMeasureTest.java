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
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.util.XmiParser;

import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.pentaho.metadata.model.LogicalModel.PROPERTY_OLAP_CUBES;
import static org.pentaho.metadata.model.SqlPhysicalColumn.TARGET_COLUMN;
import static org.pentaho.metadata.model.concept.types.AggregationType.AVERAGE;
import static org.pentaho.metadata.model.concept.types.AggregationType.MAXIMUM;
import static org.pentaho.metadata.model.concept.types.AggregationType.MINIMUM;

public class CreateMeasureTest {
  @Test
  public void testCreatesNewMeasureWithAggregation() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( AVERAGE );
    createMeasure.setName( "Avg Weight" );
    createMeasure.setFormatString( "##.##" );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 ).getLogicalColumns().get( 6 )
        .setName( new LocalizedString( model.getWorkspaceHelper().getLocale(), "differentName" ) );
    model.getWorkspaceHelper().populateDomain( model );

    createMeasure.apply( model, "differentName" );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "QUANTITYINSTOCK", measureMetaData.getColumnName() );
    assertEquals( "Avg Weight", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AVERAGE, measureMetaData.getDefaultAggregation() );

    @SuppressWarnings( "unchecked" )
    OlapCube cube =
        ( (List<OlapCube>) model.getDomain().getLogicalModels().get( 1 ).getProperty( PROPERTY_OLAP_CUBES ) ).get( 0 );

    // only fields in rowMeta should be present
    assertEquals( 4, cube.getOlapMeasures().size() );
    assertEquals( "Avg Weight", cube.getOlapMeasures().get( 3 ).getName() );
    assertEquals( "QUANTITYINSTOCK", cube.getOlapMeasures().get( 3 ).getLogicalColumn().getPhysicalColumn()
        .getProperty( TARGET_COLUMN ) );
  }

  @Test
  public void testMeasureNotDuplicatedWhenMultipleLogicalColumns() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( MINIMUM );
    createMeasure.setName( "Min Weight" );
    createMeasure.setFormatString( "##.##" );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    createMeasure.apply( model, "bc_QUANTITYINSTOCK" );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "QUANTITYINSTOCK", measureMetaData.getColumnName() );
    assertEquals( "Min Weight", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( MINIMUM, measureMetaData.getDefaultAggregation() );
  }

  /**
   * Verifies that we can create a new measure using an existing measure as the source.
   * This basically results in using the existing measure's physical column as the
   * underlying column for the new measure.
   * 
   * @throws Exception
   */
  @Test
  public void testCreateMeasureByMeasure() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( MINIMUM );
    createMeasure.setName( "Min Buy Price" );
    createMeasure.setFormatString( "##.##" );
    ModelAnnotation annotation =
        new ModelAnnotation<CreateMeasure>( ModelAnnotation.SourceType.Measure, "products_38GA",
            "[Measures].[bc_BUYPRICE]", createMeasure );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    annotation.apply( model );

    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "BUYPRICE", measureMetaData.getColumnName() );
    assertEquals( "Min Buy Price", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( MINIMUM, measureMetaData.getDefaultAggregation() );
  }

  /**
   * Verified that we can create a new measure using an existing hierarchy as the source.
   * 
   * @throws Exception
   */
  @Test
  public void testCreateMeasureByLevel() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( AggregationType.COUNT_DISTINCT );
    createMeasure.setName( "Product Count" );
    createMeasure.setFormatString( "##.##" );
    ModelAnnotation annotation =
        new ModelAnnotation<CreateMeasure>( ModelAnnotation.SourceType.HierarchyLevel, "products_38GA",
            "[PRODUCTNAME].[PRODUCTNAME]", createMeasure );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    annotation.apply( model );

    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "PRODUCTNAME", measureMetaData.getColumnName() );
    assertEquals( "Product Count", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AggregationType.COUNT_DISTINCT, measureMetaData.getDefaultAggregation() );
  }

  @Test
  public void testCanCreateMultipleMeasuresOnSameColumn() throws Exception {
    CreateMeasure minWeight = new CreateMeasure();
    minWeight.setAggregateType( MINIMUM );
    minWeight.setName( "Min Weight" );
    minWeight.setFormatString( "##.##" );

    CreateMeasure maxWeight = new CreateMeasure();
    maxWeight.setAggregateType( MAXIMUM );
    maxWeight.setName( "Max Weight" );
    maxWeight.setFormatString( "##.##" );

    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    minWeight.apply( model, "bc_QUANTITYINSTOCK" );
    maxWeight.apply( model, "bc_QUANTITYINSTOCK" );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 5, measures.size() );
    MeasureMetaData minMeta = measures.get( 3 );
    assertEquals( "QUANTITYINSTOCK", minMeta.getColumnName() );
    assertEquals( "Min Weight", minMeta.getName() );
    assertEquals( "##.##", minMeta.getFormat() );
    assertEquals( MINIMUM, minMeta.getDefaultAggregation() );

    MeasureMetaData maxMeta = measures.get( 4 );
    assertEquals( "QUANTITYINSTOCK", maxMeta.getColumnName() );
    assertEquals( "Max Weight", maxMeta.getName() );
    assertEquals( "##.##", maxMeta.getFormat() );
    assertEquals( MAXIMUM, maxMeta.getDefaultAggregation() );
  }
}
