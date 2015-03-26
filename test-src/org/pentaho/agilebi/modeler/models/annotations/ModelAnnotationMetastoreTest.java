package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProviderConnection;
import org.pentaho.agilebi.modeler.models.annotations.data.NameValueProperty;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    DataProvider dataProvider = new DataProvider();
    dataProvider.setName( "dataProvider1" );
    dataProvider.setSchemaName( "schemaName" );
    dataProvider.setTableName( "tableName" );
    dataProvider.setDatabaseMetaNameRef( "dbMeta1" );
    dataProvider.setColumnMappings( getTestColumnMappings() );

    List<DataProvider> dataProviders = new ArrayList<DataProvider>();
    dataProviders.add( dataProvider );

    group.setDataProviders( dataProviders );
    group.setSharedDimension( true );

    // create metastore
    MetaStoreFactory<ModelAnnotationGroup> factory =
        new MetaStoreFactory( ModelAnnotationGroup.class, metaStore, "pentaho" );
    factory.saveElement( group );

    // load element
    assertEquals( 1, factory.getElements().size() );

    ModelAnnotationGroup loadedGroup = factory.loadElement( group.getName() );

    assertEquals( group.size(), loadedGroup.size() );

    assertEquals( "Test Description", loadedGroup.getDescription() );

    assertEquals( true, loadedGroup.isSharedDimension() );
    assertEquals( 1, loadedGroup.getDataProviders().size() );
    assertEquals( "dbMeta1", loadedGroup.getDataProviders().get( 0 ).getDatabaseMetaNameRef() );
    assertEquals( 2, loadedGroup.getDataProviders().get( 0 ).getColumnMappings().size() );

    group.setModelAnnotations( null );
    assertEquals( 0, group.size() );

    group.setModelAnnotations( new ModelAnnotationGroup() );
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

  @Test
  public void testSaveAndLoadLinkDimension() throws Exception {

    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "ldName" );
    linkDimension.setSharedDimension( "ldsd" );

    ModelAnnotation<LinkDimension> linkDimensionModelAnnotation =
        new ModelAnnotation<LinkDimension>( "f1", linkDimension );
    MetaStoreFactory<ModelAnnotation<?>> factory = getModelAnnotationFactory();
    factory.saveElement( linkDimensionModelAnnotation );
    assertNotNull( linkDimension.getName() );
    assertNotNull( linkDimension.getSharedDimension() );

    assertEquals( 1, factory.getElements().size() );
    ModelAnnotation<LinkDimension> loadedAnnotation =
        (ModelAnnotation<LinkDimension>) factory.getElements().get( 0 );
    LinkDimension loadedLinkDimension = loadedAnnotation.getAnnotation();
    assertEquals( linkDimension.getName(), loadedLinkDimension.getName() );
    assertEquals( linkDimension.getSharedDimension(), loadedLinkDimension.getSharedDimension() );

    assertEquals( linkDimension, loadedLinkDimension );
    assertFalse( linkDimension.equals( new LinkDimension() ) );
    assertFalse( loadedLinkDimension.equals( new LinkDimension() ) );
  }

  private MetaStoreFactory<ModelAnnotation<?>> getModelAnnotationFactory() {
    return new MetaStoreFactory( ModelAnnotation.class, metaStore, "pentaho" );
  }

  private DataProviderConnection getTestDataProviderConnection() {

    DataProviderConnection dataProviderConnection = new DataProviderConnection();
    dataProviderConnection.setName( "STAGING" );

    List<NameValueProperty> attributeList = new ArrayList<NameValueProperty>();
    attributeList.add( new NameValueProperty( "prop1", "propVal1" ) );
    attributeList.add( new NameValueProperty( "prop2", "propVal2" ) );
    dataProviderConnection.setAttributeList( attributeList );

    return dataProviderConnection;
  }

  private List<ColumnMapping> getTestColumnMappings() {

    List<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();

    ColumnMapping cm = new ColumnMapping();
    cm.setName( "cm-name" );
    cm.setColumnName( "cm-column-name" );
    cm.setColumnDataType( DataType.BINARY );
    columnMappings.add( cm );

    ColumnMapping cm1 = new ColumnMapping();
    cm1.setName( "cm1-name" );
    cm1.setColumnName( "cm1-column-name" );
    cm1.setColumnDataType( DataType.DATE );
    columnMappings.add( cm );

    return columnMappings;
  }

}
