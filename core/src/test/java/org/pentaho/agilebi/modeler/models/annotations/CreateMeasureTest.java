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

import junit.framework.Assert;
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

import static junit.framework.Assert.*;
import static org.pentaho.agilebi.modeler.models.annotations.CreateMeasure.*;
import static org.pentaho.metadata.model.LogicalModel.PROPERTY_OLAP_CUBES;
import static org.pentaho.metadata.model.SqlPhysicalColumn.TARGET_COLUMN;
import static org.pentaho.metadata.model.concept.types.AggregationType.*;

public class CreateMeasureTest {

  private static String PRODUCT_XMI_FILE = "src/test/resources/products.xmi";

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
    createMeasure.setField( "differentName" );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 ).getLogicalColumns().get( 6 )
        .setName( new LocalizedString( model.getWorkspaceHelper().getLocale(), "differentName" ) );
    model.getWorkspaceHelper().populateDomain( model );

    createMeasure.apply( model, metaStore );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = AnnotationUtil.getMeasureMetaData( "Avg Weight", measures );
    assertNotNull( measureMetaData );
    assertEquals( "QUANTITYINSTOCK", measureMetaData.getColumnName() );
    assertEquals( "Avg Weight", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AVERAGE, measureMetaData.getDefaultAggregation() );
    assertFalse( measureMetaData.isHidden() );

    @SuppressWarnings( "unchecked" )
    OlapCube cube =
        ( (List<OlapCube>) model.getDomain().getLogicalModels().get( 1 ).getProperty( PROPERTY_OLAP_CUBES ) ).get( 0 );

    // only fields in rowMeta should be present
    assertEquals( 4, cube.getOlapMeasures().size() );
    OlapMeasure olapMeasure = AnnotationUtil.getOlapMeasure( "Avg Weight", cube.getOlapMeasures() );
    assertNotNull( olapMeasure );
    assertEquals( "Avg Weight", olapMeasure.getName() );
    assertEquals( "QUANTITYINSTOCK", olapMeasure.getLogicalColumn().getPhysicalColumn()
        .getProperty( TARGET_COLUMN ) );
    assertFalse( olapMeasure.isHidden() );
  }

  @Test
  public void testMeasureNotDuplicatedWhenMultipleLogicalColumns() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setAggregateType( MINIMUM );
    createMeasure.setName( "Min Weight" );
    createMeasure.setFormatString( "##.##" );
    createMeasure.setField( "bc_QUANTITYINSTOCK" );
    createMeasure.setHidden( true );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    createMeasure.apply( model, metaStore );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = AnnotationUtil.getMeasureMetaData( "Min Weight", measures );
    assertNotNull( measureMetaData );
    assertEquals( "QUANTITYINSTOCK", measureMetaData.getColumnName() );
    assertEquals( "Min Weight", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( MINIMUM, measureMetaData.getDefaultAggregation() );
    assertTrue( measureMetaData.isHidden() );
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
    createMeasure.setMeasure( "[Measures].[bc_BUYPRICE]" );
    createMeasure.setCube( "products_38GA" );
    createMeasure.setHidden( true );
    ModelAnnotation annotation =
        new ModelAnnotation<CreateMeasure>( createMeasure );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    annotation.apply( model, metaStore );

    Assert.assertEquals( "bc_BUYPRICE", ( (CreateMeasure) annotation.getAnnotation() ).getField() );

    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = AnnotationUtil.getMeasureMetaData( "Min Buy Price", measures );
    assertNotNull( measureMetaData );
    assertEquals( "BUYPRICE", measureMetaData.getColumnName() );
    assertEquals( "Min Buy Price", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( MINIMUM, measureMetaData.getDefaultAggregation() );
    assertTrue( measureMetaData.isHidden() );
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
    createMeasure.setLevel( "[PRODUCTNAME].[PRODUCTNAME]" );
    createMeasure.setCube( "products_38GA" );
    createMeasure.setHidden( true );
    ModelAnnotation annotation =
        new ModelAnnotation<CreateMeasure>( createMeasure );

    ModelerWorkspace model = new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    LogicalTable logicalTable = model.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    logicalTable.addLogicalColumn( (LogicalColumn) logicalTable.getLogicalColumns().get( 6 ).clone() );
    annotation.apply( model, metaStore );

    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = AnnotationUtil.getMeasureMetaData( "Product Count", measures );
    assertNotNull( measureMetaData );
    assertEquals( "PRODUCTNAME", measureMetaData.getColumnName() );
    assertEquals( "Product Count", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AggregationType.COUNT_DISTINCT, measureMetaData.getDefaultAggregation() );
    assertTrue( measureMetaData.isHidden() );
  }

  @Test
  public void testRemovesAnyLevelsWhichUseTheSameColumn() throws Exception {
    CreateMeasure minWeight = new CreateMeasure();
    minWeight.setAggregateType( MINIMUM );
    minWeight.setName( "Min Weight" );
    minWeight.setFormatString( "##.##" );
    minWeight.setField( "PRODUCTCODE_OLAP" );

    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    minWeight.apply( model, metaStore );
    final OlapCube cube = getCubes( model ).get( 0 );
    assertEquals( 8, cube.getOlapDimensionUsages().size() );

  }

  @Test
  public void testCanCreateMultipleMeasuresOnSameColumn() throws Exception {
    CreateMeasure minWeight = new CreateMeasure();
    minWeight.setAggregateType( MINIMUM );
    minWeight.setName( "Min Weight" );
    minWeight.setFormatString( "##.##" );
    minWeight.setField( "bc_QUANTITYINSTOCK" );

    CreateMeasure maxWeight = new CreateMeasure();
    maxWeight.setAggregateType( MAXIMUM );
    maxWeight.setName( "Max Weight" );
    maxWeight.setFormatString( "##.##" );
    maxWeight.setField( "bc_QUANTITYINSTOCK" );
    maxWeight.setHidden( true );

    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    minWeight.apply( model, metaStore );
    maxWeight.apply( model, metaStore );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 5, measures.size() );
    MeasureMetaData minMeta = AnnotationUtil.getMeasureMetaData( "Min Weight", measures );
    assertNotNull( minMeta );
    assertEquals( "QUANTITYINSTOCK", minMeta.getColumnName() );
    assertEquals( "Min Weight", minMeta.getName() );
    assertEquals( "##.##", minMeta.getFormat() );
    assertEquals( MINIMUM, minMeta.getDefaultAggregation() );
    assertFalse( minMeta.isHidden() );

    MeasureMetaData maxMeta = measures.get( 4 );
    assertEquals( "QUANTITYINSTOCK", maxMeta.getColumnName() );
    assertEquals( "Max Weight", maxMeta.getName() );
    assertEquals( "##.##", maxMeta.getFormat() );
    assertEquals( MAXIMUM, maxMeta.getDefaultAggregation() );
    assertTrue( maxMeta.isHidden() );
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

    CreateMeasure createMeasure = new CreateMeasure();

    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }

    createMeasure.setName( "name" );
    createMeasure.setField( "field" );
    createMeasure.validate();

    createMeasure.setField( "" );
    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }

    createMeasure.setName( "name" );
    createMeasure.setLevel( "level" );
    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }


    createMeasure.setCube( "cube" );
    createMeasure.validate();

    createMeasure.setLevel( "" );
    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }

    createMeasure.setMeasure( "measure" );
    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }

    createMeasure.setCube( "" );
    try {
      createMeasure.validate();
    } catch ( ModelerException me ) {
      assertNotNull( me );
    }
  }

  @Test
  public void testCreateMeasureRemovesAutoMeasure() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getModel().getMeasures().get( 0 ).setName( "Buy Price" );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().getPhysicalColumn()
        .setName( new LocalizedString( "en_US", "BUY_PRICE" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Sum Buy Price" );
    sumBuyPrice.setField( "BUY_PRICE" );
    sumBuyPrice.apply( model, metaStore );

    CreateMeasure avgBuyPrice = new CreateMeasure();
    avgBuyPrice.setAggregateType( AVERAGE );
    avgBuyPrice.setName( "buyprice" );
    avgBuyPrice.setField( "BUY_PRICE" );
    avgBuyPrice.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertEquals( 4, olapMeasures.size() );
    OlapMeasure msrp = AnnotationUtil.getOlapMeasure( "MSRP", olapMeasures );
    assertNotNull( msrp );
    assertEquals( SUM, msrp.getLogicalColumn().getAggregationType() );
    assertEquals( "MSRP", msrp.getName() );
    OlapMeasure qtyStock = AnnotationUtil.getOlapMeasure( "QUANTITYINSTOCK", olapMeasures );
    assertNotNull( qtyStock );
    assertEquals( SUM, qtyStock.getLogicalColumn().getAggregationType() );
    assertEquals( "QUANTITYINSTOCK", qtyStock.getName() );
    OlapMeasure sumBuyPriceMeasure = AnnotationUtil.getOlapMeasure( "Sum Buy Price", olapMeasures );
    assertNotNull( sumBuyPriceMeasure );
    assertEquals( SUM, sumBuyPriceMeasure.getLogicalColumn().getAggregationType() );
    assertEquals( "Sum Buy Price", sumBuyPriceMeasure.getName() );
    assertEquals( "LC_INLINE_SQL_1_pc_BUYPRICE_OLAP_2", sumBuyPriceMeasure.getLogicalColumn().getId() );
    OlapMeasure avgPriceMeasure = AnnotationUtil.getOlapMeasure( "buyprice", olapMeasures );
    assertNotNull( avgPriceMeasure );
    assertEquals( AVERAGE, avgPriceMeasure.getLogicalColumn().getAggregationType() );
    assertEquals( "buyprice", avgPriceMeasure.getName() );
    assertEquals( "LC_INLINE_SQL_1_pc_BUYPRICE_OLAP_3", avgPriceMeasure.getLogicalColumn().getId() );
  }

  @Test
  public void testFindsColumnFromBeautifiedName() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().setName( new LocalizedString( "en_US", "Buy price" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Sum Buy Price" );
    sumBuyPrice.setField( "buy_price" );
    sumBuyPrice.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    OlapMeasure sumMeasure = AnnotationUtil.getOlapMeasure( "Sum Buy Price", olapMeasures );
    assertNotNull( sumMeasure );
    assertEquals( SUM, sumMeasure.getLogicalColumn().getAggregationType() );
    assertEquals( "Sum Buy Price", sumMeasure.getName() );
  }

  @Test
  public void testRemovesMeasuresOfTheSameName() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getModel().getMeasures().get( 0 ).getLogicalColumn().setName( new LocalizedString( "en_US", "BUYPRICE" ) );

    CreateMeasure sumBuyPrice = new CreateMeasure();
    sumBuyPrice.setAggregateType( SUM );
    sumBuyPrice.setName( "Buy Price" );
    sumBuyPrice.setField( "BUYPRICE" );
    sumBuyPrice.apply( model, metaStore );

    CreateMeasure avgBuyPrice = new CreateMeasure();
    avgBuyPrice.setAggregateType( AVERAGE );
    avgBuyPrice.setName( "Buy Price" );
    avgBuyPrice.setField( "BUYPRICE" );
    avgBuyPrice.apply( model, metaStore );

    final OlapCube cube = getCubes( model ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertEquals( 3, olapMeasures.size() );
    OlapMeasure priceMeasure = AnnotationUtil.getOlapMeasure( "Buy Price", olapMeasures );
    assertNotNull( priceMeasure );
    assertEquals( AVERAGE, priceMeasure.getLogicalColumn().getAggregationType() );
    assertEquals( "Buy Price", priceMeasure.getName() );
  }

  @Test
  public void testDefaultAggregationTypIsSum() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    assertEquals( AggregationType.SUM, createMeasure.getAggregateType() );
  }


  @Test
  public void testMeasureDescription() throws Exception {
    ModelerWorkspace wspace =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ) );
    wspace.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    wspace.getWorkspaceHelper().populateDomain( wspace );

    CreateMeasure msrp = new CreateMeasure();
    msrp.setName( "MSRP" );
    msrp.setDescription( "Manufacturer's Suggested Retail Price" );
    msrp.setField( "bc_MSRP" );
    msrp.apply( wspace, metaStore );

    boolean found = false;
    for ( OlapMeasure measure : getCubes( wspace ).get( 0 ).getOlapMeasures() ) {
      if ( measure.getName().equals( msrp.getName() ) ) {
        found = true;
        final String description = measure.getLogicalColumn().getDescription().getString( "en_US" );
        assertEquals( msrp.getDescription(), description );
        break;
      }
    }
    assertTrue( found );
  }

  @Test
  public void testFieldLevelCubeMeasureAreHiddenProperties() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    List<ModelProperty> modelProperties = createMeasure.getModelProperties();
    int assertCount = 0;
    for ( ModelProperty modelProperty : modelProperties ) {
      String id = modelProperty.id();
      if ( FIELD_ID.equals( id ) || LEVEL_ID.equals( id ) || CUBE_ID.equals( id ) || MEASURE_ID.equals( id ) ) {
        assertTrue( modelProperty.hideUI() );
        assertCount++;
      }
    }
    assertEquals( 4, assertCount );
  }

  @SuppressWarnings( "unchecked" )
  private List<OlapCube> getCubes( ModelerWorkspace wspace ) {
    return (List<OlapCube>) wspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty(
        LogicalModel.PROPERTY_OLAP_CUBES );
  }
}
