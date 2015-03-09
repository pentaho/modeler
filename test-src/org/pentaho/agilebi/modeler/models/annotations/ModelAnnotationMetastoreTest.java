package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.io.FileUtils;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationMetastoreTest {

  private String tempDir = null;
  private IMetaStore metaStore = null;

  @Before
  public void before() throws IOException, MetaStoreException {
    File f = File.createTempFile( "ModelAnnotationManageTest", "before" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
  }

  @After
  public void after() throws IOException {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  @Test
  public void testSaveAndLoadCreateMeasure() throws Exception {
    CreateMeasure createMeasure = new CreateMeasure();
    createMeasure.setName( "myMeasure" );
    createMeasure.setFormatString( "xxx" );
    createMeasure.setDescription( "description" );
    createMeasure.setBusinessGroup( "myBizGrp" );
    createMeasure.setAggregateType( AggregationType.AVERAGE );
    ModelAnnotation modelAnnotation = new ModelAnnotation( "f1", createMeasure );

    // Save in the metastore
    MetaStoreFactory<ModelAnnotation> factory = new MetaStoreFactory( ModelAnnotation.class, metaStore, "pentaho" );
    factory.saveElement( modelAnnotation );

    // Load all elements
    List<ModelAnnotation> list = factory.getElements();
    assertNotNull( list );
    assertEquals( 1, list.size() );

    ModelAnnotation<CreateMeasure> loaded = list.get( 0 );
    assertNotNull( loaded );
    assertEquals( ModelAnnotation.Type.CREATE_MEASURE, loaded.getType() );
    assertEquals( "myMeasure", loaded.getAnnotation().getName() );
    assertEquals( "xxx", loaded.getAnnotation().getFormatString() );
    assertEquals( "description", loaded.getAnnotation().getDescription() );
    assertEquals( "myBizGrp", loaded.getAnnotation().getBusinessGroup() );
    assertEquals( AggregationType.AVERAGE, loaded.getAnnotation().getAggregateType() );
  }

  @Test
  public void testSaveAndLoadCreateAttribute() throws Exception {
    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setName( "myAttribute" );
    createAttribute.setDimension( "myDim" );
    createAttribute.setHierarchy( "myHierarchy" );
    createAttribute.setParentAttribute( "myParent" );
    createAttribute.setDescription( "description" );
    createAttribute.setBusinessGroup( "myBizGrp" );
    createAttribute.setGeoType( ModelAnnotation.GeoType.City );
    createAttribute.setTimeType( ModelAnnotation.TimeType.TimeHalfYears );
    ModelAnnotation modelAnnotation = new ModelAnnotation( "f1", createAttribute );

    // Save in the metastore
    MetaStoreFactory<ModelAnnotation> factory = new MetaStoreFactory( ModelAnnotation.class, metaStore, "pentaho" );
    factory.saveElement( modelAnnotation );

    // Load all elements
    List<ModelAnnotation> list = factory.getElements();
    assertNotNull( list );
    assertEquals( 1, list.size() );

    ModelAnnotation<CreateAttribute> loaded = list.get( 0 );
    assertNotNull( loaded );
    assertEquals( ModelAnnotation.Type.CREATE_ATTRIBUTE, loaded.getType() );
    assertEquals( "myAttribute", loaded.getAnnotation().getName() );
    assertEquals( "description", loaded.getAnnotation().getDescription() );
    assertEquals( "myBizGrp", loaded.getAnnotation().getBusinessGroup() );

    assertEquals( "myDim", loaded.getAnnotation().getDimension() );
    assertEquals( "myHierarchy", loaded.getAnnotation().getHierarchy() );
    assertEquals( "myParent", loaded.getAnnotation().getParentAttribute() );

    assertEquals( ModelAnnotation.GeoType.City, loaded.getAnnotation().getGeoType() );
    assertEquals( ModelAnnotation.TimeType.TimeHalfYears, loaded.getAnnotation().getTimeType() );
  }

  @Test
  public void testSaveAndLoadModelAnnotationGroup() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();
    group.setName( "My Category" );
    group.setDescription( "Test Description" );

    for ( int i = 0; i < 100; i++ ) {
      if ( i % 2 == 0 ) {
        CreateMeasure createMeasure = new CreateMeasure();
        createMeasure.setName( "measure" + 1 );
        group.add( new ModelAnnotation( "f" + i, createMeasure ) );
      } else {
        CreateAttribute createAttribute = new CreateAttribute();
        createAttribute.setName( "attribute" + 1 );
        group.add( new ModelAnnotation( "f", createAttribute ) );
      }
    }

    // create metastore
    MetaStoreFactory<ModelAnnotationGroup>
        factory =
        new MetaStoreFactory( ModelAnnotationGroup.class, metaStore, "pentaho" );
    factory.saveElement( group );

    // load element
    assertEquals( 1, factory.getElements().size() );
    assertEquals( group.size(), factory.loadElement( group.getName() ).size() );

    assertEquals( "Test Description", factory.loadElement( group.getName() ).getDescription() );

        group.setModelAnnotations( null );
    assertEquals( 0, group.size() );

    group.setModelAnnotations( new ModelAnnotationGroup(  ) );
    assertEquals( 0, group.size() );
  }

  @Test
  public void testSaveAndLoadCreateDimensionKey() throws Exception {
    CreateDimensionKey createDimKey = new CreateDimensionKey();
    createDimKey.setName( "f1" );
    createDimKey.setDimension( "1dim" );
    ModelAnnotation<CreateDimensionKey> annotation =
        new ModelAnnotation<CreateDimensionKey>( "f1", createDimKey );

    MetaStoreFactory<ModelAnnotation<?>> factory = getModelAnnotationFactory();
    factory.saveElement( annotation );
    assertNotNull( createDimKey.getName() );
    assertNotNull( createDimKey.getDimension() );

    assertEquals( 1, factory.getElements().size() );
    @SuppressWarnings( "unchecked" )
    ModelAnnotation<CreateDimensionKey> roundtripAnnotation =
        (ModelAnnotation<CreateDimensionKey>) factory.getElements().get( 0 );

    CreateDimensionKey roundtripCreateDimKey = roundtripAnnotation.getAnnotation();
    assertEquals( createDimKey.getName(), roundtripCreateDimKey.getName() );
    assertEquals( createDimKey.getDimension(), roundtripCreateDimKey.getDimension() );
  }

  private MetaStoreFactory<ModelAnnotation<?>> getModelAnnotationFactory() {
    return new MetaStoreFactory( ModelAnnotation.class, metaStore, "pentaho" );
  }
}
