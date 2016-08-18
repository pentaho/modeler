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

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.pentaho.agilebi.modeler.models.annotations.util.AnnotationConstants;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.olap.OlapCalculatedMember;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

public class CreateCalculatedMemberIT {
  private static final String TEST_CALCULATED_MEMBER_NAME = "TestCalculatedMember";
  private static final String TEST_CALCULCATED_MEMBER_CAPTION = "Test Caption";
  private static final String TEST_CALCULATED_MEMBER_DESC = "Test Description";
  private static final String TEST_CALCULATED_MEMBER_FORMULA = "Test Formula";
  private static final String TEST_CALCULATED_MEMBER_DIMENSION = "Test Dimension";
  private static final String TEST_CALCULATED_MEMBER_FORMAT_STRING = "$#,##0.00";
  private static final String TEST_CALCULATED_MEMBER_FORMAT_CATEGORY = "Default";
  private static final String MONDRIAN_TEST_FILE_PATH = "src/it/resources/products.mondrian.xml";
  private static final String CAPTION_ATTRIB = "caption";
  private static final String DESCRIPTION_ATTRIB = "description";
  private static final String DIMENSION_ATTRIB = "dimension";
  private static final String FORMULA_ATTRIB = "formula";
  private static final String FORMAT_STRING_ATTRIB = "formatString";
  private static final String GEO_ROLE_PROPERTIES = "src/it/resources/geoRoles.properties";


  CreateCalculatedMember createCalculatedMember = new CreateCalculatedMember();
  Document mockDocument;

  private DatabaseMeta dbMeta;

  @Before
  public void setUp() throws Exception {
    createCalculatedMember.setName( TEST_CALCULATED_MEMBER_NAME );
    createCalculatedMember.setCaption( TEST_CALCULCATED_MEMBER_CAPTION );
    createCalculatedMember.setDescription( TEST_CALCULATED_MEMBER_DESC );
    createCalculatedMember.setFormula( TEST_CALCULATED_MEMBER_FORMULA );
    createCalculatedMember.setDimension( TEST_CALCULATED_MEMBER_DIMENSION );
    createCalculatedMember.setFormatString( TEST_CALCULATED_MEMBER_FORMAT_STRING );
    createCalculatedMember.setFormatCategory( TEST_CALCULATED_MEMBER_FORMAT_CATEGORY );
    createCalculatedMember.setHidden( Boolean.FALSE );
  }

  @Test
  public void testApplyMondrian() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse( mondrianSchemaXmlFile );

    createCalculatedMember.apply( mondrianSchemaXmlDoc );

    assertTrue( mondrianSchemaXmlDoc != null );
    assertTrue(
        mondrianSchemaXmlDoc.getElementsByTagName( AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME ).getLength() > 0 );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        CAPTION_ATTRIB,
        TEST_CALCULCATED_MEMBER_CAPTION ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        DESCRIPTION_ATTRIB,
        TEST_CALCULATED_MEMBER_DESC ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        DIMENSION_ATTRIB,
        TEST_CALCULATED_MEMBER_DIMENSION ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        FORMULA_ATTRIB,
        TEST_CALCULATED_MEMBER_FORMULA ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        AnnotationUtil.NAME_ATTRIB,
        TEST_CALCULATED_MEMBER_NAME ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        FORMAT_STRING_ATTRIB,
        TEST_CALCULATED_MEMBER_FORMAT_STRING ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        AnnotationUtil.VISIBLE_ATTRIB,
        Boolean.TRUE.toString() ) );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_PROPERTY_ELEMENT_NAME, "SOLVE_ORDER", "value", "0" ) );

    // validate mondrian annotations
    assertTrue( AnnotationUtil.validateMondrianAnnotationValue( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_ELEMENT_NAME,
        TEST_CALCULATED_MEMBER_NAME,
        AnnotationConstants.INLINE_ANNOTATION_FORMAT_CATEGORY,
        TEST_CALCULATED_MEMBER_FORMAT_CATEGORY ) );
  }

  @Test
  public void testApplyMondrianCalculateSubTotalsHasSolvedOrder200() throws Exception {
    File mondrianSchemaXmlFile = new File( MONDRIAN_TEST_FILE_PATH );

    Document mondrianSchemaXmlDoc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse( mondrianSchemaXmlFile );

    createCalculatedMember.setCalculateSubtotals( true );
    createCalculatedMember.apply( mondrianSchemaXmlDoc );

    assertTrue( mondrianSchemaXmlDoc != null );
    assertTrue( AnnotationUtil.validateNodeAttribute( mondrianSchemaXmlDoc,
        AnnotationUtil.CALCULATED_MEMBER_PROPERTY_ELEMENT_NAME, "SOLVE_ORDER", "value", "200" ) );
  }

  @Test
  public void testGetSummary() throws Exception {
    CreateCalculatedMember cm = new CreateCalculatedMember();
    cm.setName( "Some Name" );
    cm.setFormula( "[A]+[B]" );

    assertEquals( "Some Name, calculated from [A]+[B]", cm.getSummary() );
  }

  @Test
  public void testValidate() throws Exception {
    createCalculatedMember.validate();
  }

  @Test
  public void testValidateException() throws Exception {
    CreateCalculatedMember cm = new CreateCalculatedMember();

    try {
      cm.validate();
    } catch ( ModelerException me ) {
      Assert.assertEquals( me.getMessage(), "Catalog name is required." );
    }

    try {
      cm.setPdiContext( true );
      cm.validate();
    } catch ( ModelerException me ) {
      Assert.assertEquals( me.getMessage(), "Measure name is required." );
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
  public void testCreatesCalculatedMembersInAnalysisModel() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    CreateCalculatedMember doubleQuantity = new CreateCalculatedMember();
    doubleQuantity.setFormatString( "##.##" );
    doubleQuantity.setFormula( "[Measures].[Quantity Ordered] * 2" );
    doubleQuantity.setName( "Double Quantity" );
    doubleQuantity.setDimension( "Measures" );
    doubleQuantity.setCalculateSubtotals( false );
    doubleQuantity.apply( model, new MemoryMetaStore() );

    CreateCalculatedMember productTotal = new CreateCalculatedMember();
    productTotal.setFormatString( "##" );
    productTotal.setFormula( "Aggregate([PRODUCT ID].[PRODUCT ID].[PRODUCT ID].members)" );
    productTotal.setName( "Product Total" );
    productTotal.setDimension( "PRODUCT ID" );
    productTotal.setCalculateSubtotals( true );
    productTotal.setHidden( true );
    productTotal.apply( model, new MemoryMetaStore() );

    assertCalcMembersInModel( model );
    //in practice a seperate annotation might be run after this one which calls populateDomain
    //need to test that the calc members are not lost when this happens
    model.getWorkspaceHelper().populateDomain( model );
    assertCalcMembersInModel( model );
  }

  @SuppressWarnings( "unchecked" )
  private void assertCalcMembersInModel( final ModelerWorkspace model ) {
    List<OlapCube> cubes =
        (List<OlapCube>) model.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty( "olap_cubes" );
    List<OlapCalculatedMember> olapCalcMembers = cubes.get( 0 ).getOlapCalculatedMembers();
    assertEquals( 2, olapCalcMembers.size() );
    assertCalcMember( olapCalcMembers.get( 0 ), "[Measures].[Quantity Ordered] * 2", "Double Quantity", "Measures",
        "##.##", false );
    assertFalse( olapCalcMembers.get( 0 ).isHidden() );
    assertCalcMember(
        olapCalcMembers.get( 1 ),
        "Aggregate([PRODUCT ID].[PRODUCT ID].[PRODUCT ID].members)", "Product Total", "PRODUCT ID", "##", true );
    assertTrue( olapCalcMembers.get( 1 ).isHidden() );
  }

  private void assertCalcMember( final OlapCalculatedMember firstMember, final String formula, final String name,
                                 final String dimension, final String format, final boolean calculateSubtotals ) {
    assertEquals( formula, firstMember.getFormula() );
    assertEquals( name, firstMember.getName() );
    assertEquals( dimension, firstMember.getDimension() );
    assertEquals( format, firstMember.getFormatString() );
    assertEquals( calculateSubtotals, firstMember.isCalculateSubtotals() );
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
