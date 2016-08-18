/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2016 Pentaho Corporation (Pentaho). All rights reserved.
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
package org.pentaho.di.core.refinery.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.agilebi.modeler.models.annotations.CreateAttribute;
import org.pentaho.agilebi.modeler.models.annotations.CreateMeasure;
import org.pentaho.agilebi.modeler.models.annotations.LinkDimension;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationManager;
import org.pentaho.agilebi.modeler.models.annotations.SharedDimensionGroup;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.PentahoDefaults;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationManagerIT {

  private String tempDir = null;
  private IMetaStore metaStore = null;
  private ModelAnnotationManager modelAnnotationManager = null;
  private ModelAnnotationManager sharedDimensionManager = null;

  @Before
  public void before() throws IOException, MetaStoreException, KettleException {
    File f = File.createTempFile( "ModelAnnotationManageTest", "before" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
    modelAnnotationManager = new ModelAnnotationManager();
    sharedDimensionManager = new ModelAnnotationManager( true );
  }

  @After
  public void after() throws IOException {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  @Test
  public void testCreateModelAnnotationGroup() throws Exception {

    CreateAttribute ca = new CreateAttribute();
    ca.setField( "country" );
    ca.setGeoType( ModelAnnotation.GeoType.Country );
    ModelAnnotation<CreateAttribute> m1 = new ModelAnnotation<CreateAttribute>( ca );

    LinkDimension ld = new LinkDimension();
    ld.setField( "country" );
    ld.setSharedDimension( "Geo Dimension" );
    ModelAnnotation<LinkDimension> m2 = new ModelAnnotation<LinkDimension>( ld );

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );
    group.add( m1 );
    group.add( m2 );

    modelAnnotationManager.createGroup( group, this.metaStore );
    assertNotNull( modelAnnotationManager.readGroup( group.getName(), this.metaStore ) );
    assertNotNull(
        modelAnnotationManager.readGroup( group.getName(), this.metaStore ).get( 0 ).getAnnotation().getType() );

    SharedDimensionGroup sGroup = new SharedDimensionGroup();
    sGroup.setName( "Shared Dimension Group" );
    sGroup.add( m1 );
    sGroup.add( m2 );

    sharedDimensionManager.createGroup( sGroup, this.metaStore ); // able to save even with the same group name

    ModelAnnotationGroup sharedDimensionGroup = sharedDimensionManager.readGroup( sGroup.getName(), this.metaStore );
    assertEquals( sharedDimensionGroup.getName(), "Shared Dimension Group" );
    assertTrue( sharedDimensionGroup instanceof SharedDimensionGroup );
    assertEquals( sharedDimensionGroup.size(), 2 );
    assertNotNull( sharedDimensionGroup.get( 0 ).getAnnotation().getType() );
    assertNotNull( sharedDimensionGroup.get( 1 ).getAnnotation().getType() );
    assertEquals( sharedDimensionGroup.get( 0 ).getAnnotation().getType(), ModelAnnotation.Type.CREATE_ATTRIBUTE );
    assertEquals( sharedDimensionGroup.get( 1 ).getAnnotation().getType(), ModelAnnotation.Type.LINK_DIMENSION );

    CreateAttribute createAttribute = (CreateAttribute) sharedDimensionGroup.get( 0 ).getAnnotation();
    assertEquals( createAttribute.getField(), "country" );

    LinkDimension linkDimension = (LinkDimension) sharedDimensionGroup.get( 1 ).getAnnotation();
    assertEquals( linkDimension.getField(), "country" );
    assertEquals( linkDimension.getSharedDimension(), "Geo Dimension" );
  }

  @Test
  public void testListGroups() throws Exception {

    final ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Sales Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );

    List<String> names = modelAnnotationManager.listGroupNames( this.metaStore );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        if ( name.equals( group.getName() ) ) { // check mcm object
          return true;
        }

        return false;
      }
    } ) );

    List<ModelAnnotationGroup> loadedGroups =
        modelAnnotationManager.listGroups( this.metaStore );
    assertEquals( 1, loadedGroups.size() );
  }

  @Test
  public void testListSharedDimensionGroups() throws Exception {

    final SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );

    List<String> names = sharedDimensionManager.listGroupNames( this.metaStore );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        if ( name.equals( group.getName() ) ) { // check mcm object
          return true;
        }

        return false;
      }
    } ) );

    List<ModelAnnotationGroup> loadedGroups =
        sharedDimensionManager.listGroups( this.metaStore );
    assertEquals( 1, loadedGroups.size() );
  }

  @Test
  public void testContainsGroup() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );

    assertFalse( modelAnnotationManager.containsGroup( "null", this.metaStore ) );
    assertTrue( modelAnnotationManager.containsGroup( group.getName(), this.metaStore ) );

    assertFalse( sharedDimensionManager.containsGroup( group.getName(), this.metaStore ) );
  }

  @Test
  public void testContainsSharedDimensionGroup() throws Exception {

    SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );

    assertFalse( sharedDimensionManager.containsGroup( "null", this.metaStore ) );
    assertTrue( sharedDimensionManager.containsGroup( group.getName(), this.metaStore ) );
    assertFalse( modelAnnotationManager.containsGroup( group.getName(), this.metaStore ) );
  }

  @Test
  public void testDeleteGroups() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Inventory Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );
    assertEquals( 1, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    modelAnnotationManager.deleteGroup( group.getName(), this.metaStore );
    assertEquals( 0, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    // add more
    group.addAll( createTestGroup() );
    modelAnnotationManager.createGroup( group, this.metaStore );
    assertEquals( 1, modelAnnotationManager.listGroupNames( this.metaStore ).size() );

    modelAnnotationManager.deleteAllGroups( this.metaStore );
    assertEquals( 0, modelAnnotationManager.listGroupNames( this.metaStore ).size() );
  }

  @Test
  public void testDeleteSharedDimensionGroups() throws Exception {

    SharedDimensionGroup group = createSampleSharedDimensionGroup();

    // add annotations
    sharedDimensionManager.createGroup( group, this.metaStore );
    assertEquals( 1, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    sharedDimensionManager.deleteGroup( group.getName(), this.metaStore );
    assertEquals( 0, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    // add more
    group.addAll( createTestGroup() );
    sharedDimensionManager.createGroup( group, this.metaStore );
    assertEquals( 1, sharedDimensionManager.listGroupNames( this.metaStore ).size() );

    sharedDimensionManager.deleteAllGroups( this.metaStore );
    assertEquals( 0, sharedDimensionManager.listGroupNames( this.metaStore ).size() );
  }

  private ModelAnnotationGroup createTestGroup() {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Test Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    return group;
  }

  private SharedDimensionGroup createSampleSharedDimensionGroup() {

    final SharedDimensionGroup group = new SharedDimensionGroup();
    group.setName( "Shared Dimension" );

    LinkDimension ld = new LinkDimension();
    ld.setField( "country" );
    ld.setSharedDimension( "Geo Dimension" );
    ModelAnnotation<LinkDimension> mld = new ModelAnnotation<LinkDimension>( ld );

    group.add( mld );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( ca );
    group.add( mca );

    return group;
  }

  @Test
  public void testCreateEmptyAnnotationGroup() throws Exception {
    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );

    modelAnnotationManager.createGroup( group, this.metaStore );

    assertEquals( 0, modelAnnotationManager.readGroup( group.getName(), this.metaStore ).size() );

    sharedDimensionManager.createGroup( group, this.metaStore );

    assertEquals( 0, sharedDimensionManager.readGroup( group.getName(), this.metaStore ).size() );
  }

  @Test
  public void testStoreLoadDbMetaNew() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta
        dbMeta =
        new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
    dbMeta.getAttributes().setProperty( "SUPPORTS_BOOLEAN_DATA_TYPE", "N" );
    final String dbRef = modelAnnotationManager.storeDatabaseMeta( dbMeta, this.metaStore );
    assertEquals( dbMeta.getName(), dbRef );
    IMetaStore spy = Mockito.spy( this.metaStore );
    DatabaseMeta dbMetaBack = modelAnnotationManager.loadDatabaseMeta( dbRef, spy );
    Mockito.verify( spy ).getElementByName(
        eq( PentahoDefaults.NAMESPACE ) , argThat( idNotNull() ), eq( dbRef ) );
    assertEquals( dbMeta, dbMetaBack );
    dbMetaBack.setChangedDate( dbMeta.getChangedDate() );
    assertEquals( dbMeta.getAccessType(), dbMetaBack.getAccessType() );
    assertEquals( dbMeta.getAttributes(), dbMetaBack.getAttributes() );
    assertEquals( dbMeta.getHostname(), dbMetaBack.getHostname() );
    assertEquals( dbMeta.getPluginId(), dbMetaBack.getPluginId() );
    assertEquals( dbMeta.getUsername(), dbMetaBack.getUsername() );
    assertEquals( dbMeta.getPassword(), dbMetaBack.getPassword() );
    assertEquals( dbMeta.getDatabaseName(), dbMetaBack.getDatabaseName() );
    assertEquals( dbMeta.getURL(), dbMetaBack.getURL() );
  }

  private Matcher<IMetaStoreElementType> idNotNull() {
    return new BaseMatcher<IMetaStoreElementType>() {
      @Override public boolean matches( final Object item ) {
        IMetaStoreElementType elementType = (IMetaStoreElementType) item;
        return elementType.getId() != null;
      }

      @Override public void describeTo( final Description description ) {

      }
    };
  }

  @Test
  public void testStoreLoadDbMetaUpdate() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta
        dbMetaPrevious =
        new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "otherhost", "db", "3002", "user1", "pass1" );
    final String dbRefPrev = modelAnnotationManager.storeDatabaseMeta( dbMetaPrevious, this.metaStore );
    DatabaseMeta
        dbMeta =
        new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
    dbMeta.getAttributes().setProperty( "SUPPORTS_BOOLEAN_DATA_TYPE", "N" );
    final String dbRef = modelAnnotationManager.storeDatabaseMeta( dbMeta, this.metaStore );
    assertEquals( dbMeta.getName(), dbRef );
    assertEquals( dbRef, dbRefPrev );
    DatabaseMeta dbMetaBack = modelAnnotationManager.loadDatabaseMeta( dbRef, this.metaStore );
    assertEquals( dbMeta, dbMetaBack );
    dbMetaBack.setChangedDate( dbMeta.getChangedDate() );
    assertEquals( dbMeta.getAccessType(), dbMetaBack.getAccessType() );
    assertEquals( dbMeta.getAttributes(), dbMetaBack.getAttributes() );
    assertEquals( dbMeta.getHostname(), dbMetaBack.getHostname() );
    assertEquals( dbMeta.getPluginId(), dbMetaBack.getPluginId() );
    assertEquals( dbMeta.getUsername(), dbMetaBack.getUsername() );
    assertEquals( dbMeta.getPassword(), dbMetaBack.getPassword() );
    assertEquals( dbMeta.getDatabaseName(), dbMetaBack.getDatabaseName() );
    assertEquals( dbMeta.getURL(), dbMetaBack.getURL() );
  }
}
