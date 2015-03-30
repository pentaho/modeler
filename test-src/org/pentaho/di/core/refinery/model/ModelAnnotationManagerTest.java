package org.pentaho.di.core.refinery.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.CreateAttribute;
import org.pentaho.agilebi.modeler.models.annotations.CreateMeasure;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationManager;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationManagerTest {

  private String tempDir = null;
  private IMetaStore metaStore = null;
  private ModelAnnotationManager modelAnnotationManager = null;

  @Before
  public void before() throws IOException, MetaStoreException, KettleException {
    File f = File.createTempFile( "ModelAnnotationManageTest", "before" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
    modelAnnotationManager = new ModelAnnotationManager();
  }

  @After
  public void after() throws IOException {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  @Test
  public void testCreateModelAnnotation() throws Exception {

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    ModelAnnotation<CreateMeasure> m = new ModelAnnotation<CreateMeasure>( "f1", cm );
    modelAnnotationManager.create( m, this.metaStore );

    ModelAnnotation loaded = modelAnnotationManager.read( m.getName(), this.metaStore );

    assertNotNull( loaded );
    assertTrue( loaded.getAnnotation().getType().equals( ModelAnnotation.Type.CREATE_MEASURE ) );
  }

  @Test
  public void testCreateModelAnnotationGroup() throws Exception {

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    ModelAnnotation<CreateAttribute> m = new ModelAnnotation<CreateAttribute>( "f1", ca );
    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );
    group.add( m );

    modelAnnotationManager.createGroup( group, this.metaStore );

    assertNotNull( modelAnnotationManager.readGroup( group.getName(), this.metaStore ) );
    assertNotNull(
        modelAnnotationManager.readGroup( group.getName(), this.metaStore ).get( 0 ).getAnnotation().getType() );
  }

  @Test
  public void testList() throws Exception {

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );

    // add annotations
    modelAnnotationManager.create( mcm, this.metaStore );
    modelAnnotationManager.create( mca, this.metaStore );

    List<String> names = modelAnnotationManager.listNames( this.metaStore );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        if ( name.equals( mcm.getName() ) ) { // check mcm object
          return true;
        }

        return false;
      }
    } ) );

    assertTrue( CollectionUtils.exists( names, new Predicate() {
      @Override public boolean evaluate( Object o ) {

        String name = (String) o;
        if ( name.equals( mca.getName() ) ) { // check mca object
          return true;
        }

        return false;
      }
    } ) );

    ModelAnnotationGroup loadedGroup =
        modelAnnotationManager.list( this.metaStore );
    assertEquals( 2, loadedGroup.size() );
  }

  @Test
  public void testListGroups() throws Exception {

    final ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Sales Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );
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
  public void testContains() throws Exception {

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );

    // add annotations
    modelAnnotationManager.create( mcm, this.metaStore );
    modelAnnotationManager.create( mca, this.metaStore );

    assertTrue( modelAnnotationManager.contains( mcm.getName(), this.metaStore ) );
    assertTrue( modelAnnotationManager.contains( mca.getName(), this.metaStore ) );
    assertFalse( modelAnnotationManager.contains( "null", this.metaStore ) );
  }

  @Test
  public void testContainsGroup() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );
    group.add( mca );

    // add annotations
    modelAnnotationManager.createGroup( group, this.metaStore );

    assertFalse( modelAnnotationManager.contains( mcm.getName(), this.metaStore ) ); // not found, stored in group
    assertFalse( modelAnnotationManager.contains( mca.getName(), this.metaStore ) ); // not found, stored in group
    assertFalse( modelAnnotationManager.contains( "null", this.metaStore ) );
    assertTrue( modelAnnotationManager.containsGroup( group.getName(), this.metaStore ) );
  }

  @Test
  public void testDelete() throws Exception {

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );

    // add annotations
    modelAnnotationManager.create( mcm, this.metaStore );
    modelAnnotationManager.create( mca, this.metaStore );
    assertEquals( 2, modelAnnotationManager.listNames( this.metaStore ).size() );

    modelAnnotationManager.delete( mca.getName(), this.metaStore );
    assertEquals( 1, modelAnnotationManager.listNames( this.metaStore ).size() );

    // add more
    for ( ModelAnnotation modelAnnotation : createTestGroup() ) {
      modelAnnotationManager.create( modelAnnotation, this.metaStore );
    }
    assertEquals( 3, modelAnnotationManager.listNames( this.metaStore ).size() );

    modelAnnotationManager.deleteAll( this.metaStore );
    assertEquals( 0, modelAnnotationManager.listNames( this.metaStore ).size() );
  }

  @Test
  public void testDeleteGroups() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Inventory Category" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );
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

  private ModelAnnotationGroup createTestGroup() {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "Test Group" );

    CreateMeasure cm = new CreateMeasure();
    cm.setAggregateType( AggregationType.COUNT_DISTINCT );

    final ModelAnnotation<?> mcm = new ModelAnnotation<CreateMeasure>( "f1", cm );
    group.add( mcm );

    CreateAttribute ca = new CreateAttribute();
    ca.setGeoType( ModelAnnotation.GeoType.Country );

    final ModelAnnotation<?> mca = new ModelAnnotation<CreateAttribute>( "f2", ca );
    group.add( mca );

    return group;
  }

  @Test
  public void testCreateEmptyAnnotationGroup() throws Exception {
    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );

    modelAnnotationManager.createGroup( group, this.metaStore );

    assertEquals( 0, modelAnnotationManager.readGroup( group.getName(), this.metaStore ).size() );
  }

  @Test
  public void testStoreLoadDbMetaNew() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta dbMeta = new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
    dbMeta.getAttributes().setProperty( "SUPPORTS_BOOLEAN_DATA_TYPE", "N" );
    final String dbRef = modelAnnotationManager.storeDatabaseMeta( dbMeta, this.metaStore );
    assertEquals( dbMeta.getName(), dbRef );
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
    assertEquals( dbMeta.getURL() , dbMetaBack.getURL() );
  }

  @Test
  public void testStoreLoadDbMetaUpdate() throws Exception {
    KettleClientEnvironment.init();
    DatabaseMeta dbMetaPrevious = new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "otherhost", "db", "3002", "user1", "pass1" );
    final String dbRefPrev = modelAnnotationManager.storeDatabaseMeta( dbMetaPrevious, this.metaStore );
    DatabaseMeta dbMeta = new DatabaseMeta( "dbmetaTest", "postgresql", "Native", "somehost", "db", "3001", "user", "pass" );
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
    assertEquals( dbMeta.getURL() , dbMetaBack.getURL() );
  }
}
