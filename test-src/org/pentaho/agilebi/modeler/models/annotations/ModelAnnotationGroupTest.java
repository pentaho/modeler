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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus.FAILED;
import static org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus.NULL_FIELD;
import static org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus.SUCCESS;

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
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metastore.api.IMetaStore;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ModelAnnotationGroupTest {

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

    ModelAnnotation ma1 = new ModelAnnotation( "f1", cm );
    ma1.setName( "ma1" );

    ModelAnnotation ma2 = new ModelAnnotation( "f2", ca );
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
    Props.init( 0 );
  }

  @Test
  public void testNullAnnotationsAreIgnored() throws Exception {
    ModelerWorkspace model = prepareOrderModel();
    ModelAnnotation modelAnnotation = new ModelAnnotation();
    modelAnnotation.setField( "aField" );
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup( modelAnnotation );

    Map<ApplyStatus, List<ModelAnnotation>> statusMap = modelAnnotations.applyAnnotations( model, null );
    assertEquals( 1, statusMap.get( ApplyStatus.NULL_FIELD ).size() );
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
    assertEquals( 0, statusMap.get( ApplyStatus.NULL_FIELD ).size() );
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
    assertEquals( 0, statusMap.get( NULL_FIELD ).size() );
  }

  private ModelAnnotation testingAnnotation( final boolean... statuses ) {
    return new ModelAnnotation() {
      private int i = 0;
      @Override public boolean apply( final ModelerWorkspace modelerWorkspace, final IMetaStore metaStore )
          throws ModelerException {
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
}
