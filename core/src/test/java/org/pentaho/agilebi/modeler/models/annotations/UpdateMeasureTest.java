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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * @author Brandon Groves
 */
public class UpdateMeasureTest {
  private IMetaStore metaStore;

  private static String PRODUCT_XMI_FILE = "src/test/resources/products.xmi";

  private static String INIT_BUYPRICE_COLUMN = "bc_BUYPRICE";
  private static String INIT_BUYPRICE_NAME = "BUYPRICE";
  private static String INIT_MEASURE_FORMULA = "[" + AnnotationType.MEASURES_DIMENSION + "].["
      + INIT_BUYPRICE_NAME + "]";

  private static String NEW_BUYPRICE_NAME = "new_buyprice";

  private static String EXISTING_COLUMN = "bc_QUANTITYINSTOCK";
  private static String EXISTING_NAME = "QUANTITYINSTOCK";

  private static final String MONDRIAN_TEST_FILE_PATH = "src/test/resources/products.mondrian.xml";
  private static final String MONDRIAN_CALC_TEST_FILE_PATH = "src/test/resources/products.with.calc.measures.mondrian.xml";

  private static String INIT_MEASURE_MONDRIAN_FORMULA = "[" + AnnotationType.MEASURES_DIMENSION + "].["
      + INIT_BUYPRICE_COLUMN + "]";

  private static final String INIT_FORMAT = "#";
  private static final String NEW_FORMAT = "##.##";

  @Before
  public void setUp() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @Test
  public void testMetaDataUpdateName() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    // Initial measure exists
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertNotNull( AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures ) );

    // Renaming the measure
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_FORMULA );
    updateMeasure.setName( NEW_BUYPRICE_NAME );
    boolean isApplied = updateMeasure.apply( model, metaStore );
    assertTrue( isApplied );

    // Make sure the initial measure name cannot be found and the new measure name exists
    logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    olapMeasures = cube.getOlapMeasures();
    assertNull( AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures ) );
    assertNotNull( AnnotationUtil.getOlapMeasure( NEW_BUYPRICE_NAME, olapMeasures ) );
  }

  @Test
  public void testMetaDataNoDuplicateNames() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    // Initial measure exists
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    assertNotNull( AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures ) );

    // Renaming the measure
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_FORMULA );
    updateMeasure.setName( EXISTING_NAME );
    boolean isApplied = updateMeasure.apply( model, metaStore );
    assertFalse( isApplied );

    // Make sure nothing has changed
    logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    olapMeasures = cube.getOlapMeasures();

    OlapMeasure oldMeasure = AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures );
    assertNotNull( oldMeasure );
    INIT_BUYPRICE_COLUMN.equals( oldMeasure.getLogicalColumn().getName() );

    oldMeasure = AnnotationUtil.getOlapMeasure( EXISTING_NAME, olapMeasures );
    assertNotNull( oldMeasure );
    EXISTING_COLUMN.equals( oldMeasure.getLogicalColumn().getName() );
  }

  @Test
  public void testMetaDataUpdateAggregation() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    // Initial measure exists
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    OlapMeasure measure = AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures );
    assertNotNull( measure );
    assertEquals( AggregationType.SUM, measure.getLogicalColumn().getAggregationType() );

    // Changing the aggregation type
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_FORMULA );
    updateMeasure.setName( INIT_BUYPRICE_NAME );
    updateMeasure.setAggregationType( AggregationType.AVERAGE );
    boolean isApplied = updateMeasure.apply( model, metaStore );
    assertTrue( isApplied );

    // Ensure the aggregation type got set
    logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    olapMeasures = cube.getOlapMeasures();
    measure = AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures );
    assertNotNull( measure );
    assertEquals( AggregationType.AVERAGE, measure.getLogicalColumn().getAggregationType() );
  }

  @Test
  public void testMetaDataUpdateFormat() throws Exception {
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( PRODUCT_XMI_FILE ) ) );
    model.getWorkspaceHelper().populateDomain( model );

    // Initial measure exists
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    OlapCube cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    List<OlapMeasure> olapMeasures = cube.getOlapMeasures();
    OlapMeasure measure = AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures );
    assertNotNull( measure );
    assertEquals( "#", measure.getLogicalColumn().getProperty( "mask" ) );

    // Changing the aggregation type
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_FORMULA );
    updateMeasure.setName( INIT_BUYPRICE_NAME );
    updateMeasure.setFormat( NEW_FORMAT );
    boolean isApplied = updateMeasure.apply( model, metaStore );
    assertTrue( isApplied );

    // Ensure the aggregation type got set
    logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    cube = ( (List<OlapCube>) logicalModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ) ).get( 0 );
    olapMeasures = cube.getOlapMeasures();
    measure = AnnotationUtil.getOlapMeasure( INIT_BUYPRICE_NAME, olapMeasures );
    assertNotNull( measure );
    assertEquals( NEW_FORMAT, measure.getLogicalColumn().getProperty( "mask" ) );
  }

  @Test
  public void testMondrianUpdateName() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse( mondrianSchemaXmlFile );

    // Renaming the measure
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_MONDRIAN_FORMULA );
    updateMeasure.setName( NEW_BUYPRICE_NAME );
    boolean isApplied = updateMeasure.apply( mondrianSchemaXmlDoc );
    assertTrue( isApplied );

    assertTrue( mondrianSchemaXmlDoc != null );
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.MEASURE_ELEMENT_NAME ).getLength()
        > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        NEW_BUYPRICE_NAME,
        AnnotationUtil.NAME_ATTRIB,
        NEW_BUYPRICE_NAME ) );
  }

  @Test
  public void testMondrianNoDuplicateNames() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse( mondrianSchemaXmlFile );

    // Renaming the measure
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_MONDRIAN_FORMULA );
    updateMeasure.setName( EXISTING_COLUMN );
    assertFalse( updateMeasure.apply( mondrianSchemaXmlDoc ) );

    // Ensure nothing has changed
    assertTrue( mondrianSchemaXmlDoc != null );
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.MEASURE_ELEMENT_NAME ).getLength()
        > 0 );

    // Ensure new Element was not created
    assertFalse( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        NEW_BUYPRICE_NAME,
        AnnotationUtil.NAME_ATTRIB,
        "" ) );

    // Ensure original element still exists
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        EXISTING_COLUMN,
        AnnotationUtil.NAME_ATTRIB,
        EXISTING_COLUMN ) );
  }

  @Test
  public void testMondrianUpdateAggregation() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse( mondrianSchemaXmlFile );
    assertTrue( mondrianSchemaXmlDoc != null );
    // Check existing state
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.NAME_ATTRIB,
        INIT_BUYPRICE_COLUMN ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.AGGREGATOR_ATTRIB,
        "sum" ) );

    // Changing the aggregation type
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_MONDRIAN_FORMULA );
    updateMeasure.setName( INIT_BUYPRICE_COLUMN );
    updateMeasure.setAggregationType( AggregationType.AVERAGE );
    boolean isApplied = updateMeasure.apply( mondrianSchemaXmlDoc );
    assertTrue( isApplied );

    // Check change
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.MEASURE_ELEMENT_NAME ).getLength()
        > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.NAME_ATTRIB,
        INIT_BUYPRICE_COLUMN ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.AGGREGATOR_ATTRIB,
        "avg" ) );
  }

  @Test
  public void testMondrianUpdateFormat() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse( mondrianSchemaXmlFile );
    assertTrue( mondrianSchemaXmlDoc != null );
    // Check existing state
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.NAME_ATTRIB,
        INIT_BUYPRICE_COLUMN ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.FORMATSTRING_ATTRIB,
        INIT_FORMAT ) );

    // Changing the aggregation type
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_MONDRIAN_FORMULA );
    updateMeasure.setName( INIT_BUYPRICE_COLUMN );
    updateMeasure.setFormat( NEW_FORMAT );
    boolean isApplied = updateMeasure.apply( mondrianSchemaXmlDoc );
    assertTrue( isApplied );

    // Check change
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.MEASURE_ELEMENT_NAME ).getLength()
        > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.NAME_ATTRIB,
        INIT_BUYPRICE_COLUMN ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.MEASURE_ELEMENT_NAME,
        INIT_BUYPRICE_COLUMN,
        AnnotationUtil.FORMATSTRING_ATTRIB,
        NEW_FORMAT ) );
  }

  @Test
  public void testUpdatesNameAndFormatForCalculatedMeasures() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_CALC_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
      DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse( mondrianSchemaXmlFile );
    // Changing the aggregation type
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( "[Measures].[Test Calc Without Annotations]" );
    updateMeasure.setCaption( "newName" );
    updateMeasure.setFormat( NEW_FORMAT );
    updateMeasure.setCube( "products_38GA" );
    boolean isApplied = updateMeasure.apply( mondrianSchemaXmlDoc );
    assertTrue( isApplied );

    // Check change
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME ).getLength()
      > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
      AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
      "Test Calc Without Annotations", AnnotationUtil.CAPTION_ATTRIB, "newName" ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
      AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
      "Test Calc Without Annotations",
      AnnotationUtil.FORMATSTRING_ATTRIB,
      NEW_FORMAT ) );
  }

  @Test
  public void testValidate() {
    // Valid
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_FORMULA );
    updateMeasure.setName( NEW_BUYPRICE_NAME );
    try {
      updateMeasure.validate();
    } catch ( Exception e ) {
      fail( "Should be valid be was not" );
    }

    updateMeasure.setMeasure( "" );
    try {
      updateMeasure.validate();
      fail( "Exception should of been thrown" );
    } catch ( Exception e ) {
    }
  }

  @Test
  public void testUpdatesCaptionNameUnchanged() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
      DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse( mondrianSchemaXmlFile );

    // Renaming the measure
    UpdateMeasure updateMeasure = new UpdateMeasure();
    updateMeasure.setMeasure( INIT_MEASURE_MONDRIAN_FORMULA );
    updateMeasure.setCaption( NEW_BUYPRICE_NAME );
    boolean isApplied = updateMeasure.apply( mondrianSchemaXmlDoc );
    assertTrue( isApplied );

    assertTrue( mondrianSchemaXmlDoc != null );
    assertTrue( mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.MEASURE_ELEMENT_NAME ).getLength()
      > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
      AnnotationUtil.MEASURE_ELEMENT_NAME,
      INIT_BUYPRICE_COLUMN,
      AnnotationUtil.CAPTION_ATTRIB,
      NEW_BUYPRICE_NAME ) );
  }
}
