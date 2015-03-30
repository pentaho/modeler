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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.pentaho.metadata.model.LogicalModel.PROPERTY_OLAP_CUBES;
import static org.pentaho.metadata.model.SqlPhysicalColumn.TARGET_COLUMN;
import static org.pentaho.metadata.model.concept.types.AggregationType.*;

public class CreateMeasureTest {
  private IMetaStore metaStore;

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

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

    createMeasure.apply( model, "differentName", metaStore );
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
    createMeasure.apply( model, "bc_QUANTITYINSTOCK", metaStore );
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
    annotation.apply( model, metaStore );

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
    annotation.apply( model, metaStore );

    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "PRODUCTNAME", measureMetaData.getColumnName() );
    assertEquals( "Product Count", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AggregationType.COUNT_DISTINCT, measureMetaData.getDefaultAggregation() );
  }

  @Test
  public void testRemovesAnyLevelsWhichUseTheSameColumn() throws Exception {
    CreateMeasure minWeight = new CreateMeasure();
    minWeight.setAggregateType( MINIMUM );
    minWeight.setName( "Min Weight" );
    minWeight.setFormatString( "##.##" );

    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    minWeight.apply( model, "PRODUCTCODE_OLAP", metaStore );
    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    assertEquals( 8, cube.getOlapDimensionUsages().size() );

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
    minWeight.apply( model, "bc_QUANTITYINSTOCK", metaStore );
    maxWeight.apply( model, "bc_QUANTITYINSTOCK", metaStore );
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

  @Test
  public void testSummaryDescribesNameAndAggregator() throws Exception {
    CreateMeasure sumMeasure = new CreateMeasure();
    sumMeasure.setAggregateType( SUM );
    sumMeasure.setName( "Value" );
    assertEquals( "Value, aggregated with SUM", sumMeasure.getSummary() );

    CreateMeasure maxMeasure = new CreateMeasure();
    maxMeasure.setAggregateType( MAXIMUM );
    maxMeasure.setName( "Max Val" );
    assertEquals( "Max Val, aggregated with MAXIMUM", maxMeasure.getSummary() );

    CreateMeasure noAggregate = new CreateMeasure();
    noAggregate.setName( "Test" );
    noAggregate.setAggregateType( null );
    assertEquals( "Test", noAggregate.getSummary() );
  }

  @Test
  public void testValidate() throws Exception {
    try {
      ( new CreateMeasure() ).validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }
  }

  @Test
  public void testCreateMeasureRemovesAutoMeasure() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().setName( new LocalizedString( "en_US", "BUYPRICE" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Sum Buy Price" );
    sumBuyPrice.apply( model, "BUYPRICE", metaStore );

    CreateMeasure avgBuyPrice = new CreateMeasure();
    avgBuyPrice.setAggregateType( AVERAGE );
    avgBuyPrice.setName( "BUYPRICE" );
    avgBuyPrice.apply( model, "BUYPRICE", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertEquals( 4, olapMeasures.size() );
    assertEquals( SUM, olapMeasures.get( 0 ).getLogicalColumn().getAggregationType() );
    assertEquals( "MSRP", olapMeasures.get( 0 ).getName() );
    assertEquals( SUM, olapMeasures.get( 1 ).getLogicalColumn().getAggregationType() );
    assertEquals( "QUANTITYINSTOCK", olapMeasures.get( 1 ).getName() );
    assertEquals( SUM, olapMeasures.get( 2 ).getLogicalColumn().getAggregationType() );
    assertEquals( "Sum Buy Price", olapMeasures.get( 2 ).getName() );
    assertEquals( "LC_INLINE_SQL_1_pc_BUYPRICE_OLAP_2", olapMeasures.get( 2 ).getLogicalColumn().getId() );
    assertEquals( AVERAGE, olapMeasures.get( 3 ).getLogicalColumn().getAggregationType() );
    assertEquals( "BUYPRICE", olapMeasures.get( 3 ).getName() );
    assertEquals( "LC_INLINE_SQL_1_pc_BUYPRICE_OLAP_3", olapMeasures.get( 3 ).getLogicalColumn().getId() );
  }

  @Test
  public void testFindsColumnFromBeautifiedName() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().setName( new LocalizedString( "en_US", "Buy price" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Sum Buy Price" );
    sumBuyPrice.apply( model, "buy_price", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertEquals( SUM, olapMeasures.get( 3 ).getLogicalColumn().getAggregationType() );
    assertEquals( "Sum Buy Price", olapMeasures.get( 3 ).getName() );
  }

  @Test
  public void testRemovesMeasuresOfTheSameName() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().setName( new LocalizedString( "en_US", "BUYPRICE" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Buy Price" );
    sumBuyPrice.apply( model, "BUYPRICE", metaStore );

    CreateMeasure avgBuyPrice = new CreateMeasure();
    avgBuyPrice.setAggregateType( AVERAGE );
    avgBuyPrice.setName( "Buy Price" );
    avgBuyPrice.apply( model, "BUYPRICE", metaStore );

    final LogicalModel anlModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    final OlapCube cube = ( (List<OlapCube>) anlModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertEquals( 3, olapMeasures.size() );
    assertEquals( AVERAGE, olapMeasures.get( 2 ).getLogicalColumn().getAggregationType() );
    assertEquals( "Buy Price", olapMeasures.get( 2 ).getName() );
  }

  @Test
  public void testDefaultAggregationTypIsSum() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    assertEquals( AggregationType.SUM, createMeasure.getAggregateType() );
  }
}
