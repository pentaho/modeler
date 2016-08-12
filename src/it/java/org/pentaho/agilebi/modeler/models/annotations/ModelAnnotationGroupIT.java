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

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus;
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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus.*;

public class ModelAnnotationGroupIT {

  @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
  @Test
  public void testHasAVarArgsConstructor() throws Exception {
    ModelAnnotation<CreateMeasure> measure = new ModelAnnotation<CreateMeasure>();
    ModelAnnotation<CreateAttribute> attribute = new ModelAnnotation<CreateAttribute>();
    ModelAnnotationGroup annotationGroup = new ModelAnnotationGroup( measure, attribute );
    annotationGroup.setId( "cleverIdentifier" );
    assertEquals( 2, annotationGroup.size() );
    assertSame( measure, annotationGroup.get( 0 ) );
    assertSame( attribute, annotationGroup.get( 1 ) );
    assertEquals( "cleverIdentifier", annotationGroup.getId() );
  }

  @Test
  public void testEquals() throws Exception {

    // test list objects
    ModelAnnotationGroup modelAnnotationGroup = getSampleModelAnnotationGroup();
    ModelAnnotationGroup modelAnnotationGroupCopy = getSampleModelAnnotationGroup();

    modelAnnotationGroupCopy.get( 0 ).getAnnotation().setModelPropertyValueById( "name", "modified" );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.get( 0 ).getAnnotation().setModelPropertyValueById( "name", "modified" );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroupCopy.getDataProviders().get( 0 ).setDatabaseMetaNameRef( "newRef" );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.getDataProviders().get( 0 ).setDatabaseMetaNameRef( "newRef" );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroupCopy.getDataProviders().get( 0 ).getColumnMappings().get( 0 )
        .setColumnDataType( DataType.BINARY );
    assertFalse( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    modelAnnotationGroup.getDataProviders().get( 0 ).getColumnMappings().get( 0 ).setColumnDataType( DataType.BINARY );
    assertTrue( modelAnnotationGroup.equals( modelAnnotationGroupCopy ) );

    assertFalse( modelAnnotationGroup.equals( null ) );
    assertFalse( modelAnnotationGroup.equals( new ModelAnnotationGroup() ) );
  }

  private ModelAnnotationGroup getSampleModelAnnotationGroup() {

    ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.setName( "sample" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.SUM );

    CreateAttribute ca = new CreateAttribute();
    ca.setTimeType( ModelAnnotation.TimeType.TimeDays );

    ModelAnnotation ma1 = new ModelAnnotation( cm );
    ma1.setName( "ma1" );

    ModelAnnotation ma2 = new ModelAnnotation( ca );
    ma2.setName( "ma2" );

    modelAnnotationGroup.add( ma1 );
    modelAnnotationGroup.add( ma2 );

    DataProvider dataProvider1 = new DataProvider();
    dataProvider1.setName( "dbp1" );
    dataProvider1.setDatabaseMetaNameRef( "ref1" );
    dataProvider1.setColumnMappings( Arrays.asList( new ColumnMapping[] { new ColumnMapping() } ) );

    DataProvider dataProvider2 = new DataProvider();
    dataProvider2.setName( "dbp2" );
    dataProvider2.setDatabaseMetaNameRef( "ref2" );
    dataProvider2.setColumnMappings( Arrays.asList( new ColumnMapping[] { new ColumnMapping() } ) );

    modelAnnotationGroup.setDataProviders( Arrays.asList( new DataProvider[] { dataProvider1, dataProvider2 } ) );

    modelAnnotationGroup.setSharedDimension( true );

    return modelAnnotationGroup;
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
  public void testNullAnnotationsAreIgnored() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    ModelAnnotation modelAnnotation = new ModelAnnotation();
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( modelAnnotation );

    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( model, null );
    assertEquals( 1, statusMap.get( ApplyStatus.NULL_ANNOTATION ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.SUCCESS ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.FAILED ).size() );
  }

  @Test
  public void testAnnotationsAreRetriedUntilDone() throws Exception {
    ModelerWorkspace model = prepareOrderModel();

    ModelAnnotation annotation1 = testingAnnotation( false, true );
    ModelAnnotation annotation2 = testingAnnotation( true );
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( annotation1, annotation2 );

    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( model, null );
    assertEquals( 0, statusMap.get( ApplyStatus.NULL_ANNOTATION ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.FAILED ).size() );
    assertEquals( 2, statusMap.get( ApplyStatus.SUCCESS ).size() );
  }

  @Test
  public void testAnnotationsStopRetryWhenAllFailed() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    ModelAnnotation annotation1 = testingAnnotation( false, false );
    ModelAnnotation annotation2 = testingAnnotation( false, false );
    ModelAnnotation annotation3 = testingAnnotation( true );
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( annotation1, annotation2, annotation3 );

    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( model, null );
    assertEquals( 2, statusMap.get( FAILED ).size() );
    assertEquals( 1, statusMap.get( SUCCESS ).size() );
    assertEquals( 0, statusMap.get( NULL_ANNOTATION ).size() );
  }

  private ModelAnnotation testingAnnotation( final boolean... statuses ) {
    return new ModelAnnotation() {
      private int i = 0;
      @SuppressWarnings( "SimplifiableIfStatement" )
      @Override public boolean apply( final ModelerWorkspace modelerWorkspace, final IMetaStore metaStore )
          throws ModelerException {
        if ( statuses.length > i ) {
          return statuses[i++];
        }
        return true;
      }

      @SuppressWarnings( "SimplifiableIfStatement" )
      @Override public boolean apply( final Document schema ) throws ModelerException {
        if ( statuses.length > i ) {
          return statuses[i++];
        }
        return true;
      }

      @Override public AnnotationType getAnnotation() {
        return new CreateAttribute();
      }
    };
  }

  private ModelerWorkspace prepareOrderModel() throws Exception {
    DatabaseMeta dbMeta = createOrderfactDB();
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

  private DatabaseMeta createOrderfactDB() throws Exception {
    DatabaseMeta dbMeta = newH2Db();
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
    return dbMeta;
  }

  private DatabaseMeta newH2Db() {
    // DB Setup
    String dbDir = "bin/test/ModelAnnotationGroupTest-H2-DB";
    File file = new File( dbDir + ".h2.db" );
    if ( file.exists() ) {
      file.delete();
    }
    DatabaseMeta dbMeta = new DatabaseMeta( "myh2", "HYPERSONIC", "Native", null, dbDir, null, "sa", null );
    return dbMeta;
  }

  @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
  @Test
  public void testMondrianAnnotationsRetry() throws Exception {
    ModelAnnotation annotation1 = testingAnnotation( false, false, false );
    ModelAnnotation annotation2 = testingAnnotation( false, true );
    ModelAnnotation annotation3 = testingAnnotation( true );
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( annotation1, annotation2, annotation3 );
    Document document = XMLHandler.loadXMLFile( getClass().getResourceAsStream( "resources/simple.mondrian.xml" ) );
    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( document );
    assertEquals( 2, statusMap.get( ApplyStatus.SUCCESS ).size() );
    assertEquals( 1, statusMap.get( ApplyStatus.FAILED ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.NULL_ANNOTATION ).size() );
  }

  @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
  @Test
  public void testAnnotationsApplyToMondrianSchema() throws Exception {
    UpdateMeasure updatePrice = new UpdateMeasure();
    updatePrice.setMeasure( "[Measures].[PRICEEACH]" );
    updatePrice.setName( "Price Each" );
    updatePrice.setFormat( "##.##" );
    ModelAnnotation annotation1 = new ModelAnnotation<>( updatePrice );
    UpdateMeasure updateQuantity = new UpdateMeasure();
    updateQuantity.setMeasure( "[Measures].[Quantity ordered]" );
    updateQuantity.setName( "Quantity Ordered" );
    updateQuantity.setFormat( "#" );
    updateQuantity.setAggregationType( AggregationType.AVERAGE );
    ModelAnnotation annotation2 = new ModelAnnotation<>( updateQuantity );

    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( annotation1, annotation2 );
    Document document = XMLHandler.loadXMLFile( getClass().getResourceAsStream( "resources/simple.mondrian.xml" ) );
    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( document );
    assertEquals( 2, statusMap.get( ApplyStatus.SUCCESS ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.FAILED ).size() );
    assertEquals( 0, statusMap.get( ApplyStatus.NULL_ANNOTATION ).size() );
    String actual = XMLHandler.formatNode( document );

    assertEquals(
      IOUtils.toString( getClass().getResourceAsStream( "resources/annotated.mondrian.xml" ) ).replace( "\r|\n", "" ),
      actual.replace( "\r|\n", "" )
    );
  }

  @Test
  public void testAddInjectedAnnotations_allNew() throws Exception {

    List<CreateMeasure> injectedMeasures = new ArrayList<>();
    CreateMeasure cm = buildMeasureAnnotation( "sales", "sales for our company", "sales", AggregationType.SUM, "$ #,###.00", false );

    injectedMeasures.add( cm );

    // these are the `templated` annotations. in this case we won't have any
    ModelAnnotationGroup group = new ModelAnnotationGroup();

    group.addInjectedAnnotations( injectedMeasures );

    assertEquals( injectedMeasures.size(), group.size() );
    assertEquals( cm, group.get( 0 ).getAnnotation() );

  }

  @Test
  public void testAddInjectedAnnotations_updatingTemplatedAnnotation() throws Exception {
    List<CreateMeasure> injectedMeasures = new ArrayList<>();
    CreateMeasure cm = buildMeasureAnnotation( "sales", "sales for our company", "sales", null, "$ #,###.00", false );
    CreateMeasure cmOther = buildMeasureAnnotation( "xxx", "xxx", "xxx", AggregationType.AVERAGE, "0", false );

    injectedMeasures.add( cm );
    injectedMeasures.add( cmOther );

    CreateMeasure templatedCm = buildMeasureAnnotation( "sales", null, "FIELD", AggregationType.AVERAGE, null, true );

    // these are the `templated` annotations.
    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.add( new ModelAnnotation( templatedCm ) );

    // measures are unique based on name, so the one we are injecting should match the name of templated one
    group.addInjectedAnnotations( injectedMeasures );

    // should only be 2 items in the group, not 3
    assertEquals( 2, group.size() );

    CreateMeasure cmInjected = (CreateMeasure) ( group.get( 0 ).getAnnotation() );

    // the original item in the group should be logically equal to the first injected measure
    assertTrue( cm.equalsLogically( cmInjected ) );
    // but, they should not be the same object or equal to each other

    assertNotEquals( cm, cmInjected );

    // verify the non-null properties of cm have been set on the original object
    assertEquals( cm.getName(), cmInjected.getName() );
    assertEquals( cm.getDescription(), cmInjected.getDescription() );
    assertEquals( cm.getField(), cmInjected.getField() );
    assertEquals( cm.getFormatString(), cmInjected.getFormatString() );
    assertEquals( cm.isHidden(), cmInjected.isHidden() );

    // verify any props that were null (not set) in the injecting annotation did not override templated values
    assertEquals( AggregationType.AVERAGE, cmInjected.getAggregateType() );
  }

  private CreateMeasure buildMeasureAnnotation(
    String name,
    String description,
    String field,
    AggregationType aggType,
    String formatString,
    boolean isHidden ) {

    CreateMeasure cm = new CreateMeasure();
    cm.setName( name );
    cm.setDescription( description );
    cm.setField( field );
    cm.setAggregateType( aggType );
    cm.setFormatString( formatString );
    cm.setHidden( isHidden );
    return cm;

  }
}
