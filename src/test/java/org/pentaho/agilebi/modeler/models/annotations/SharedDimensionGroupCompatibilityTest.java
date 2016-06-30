package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.IOException;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SharedDimensionGroupCompatibilityTest {

  private IMetaStore metaStore = null;
  private ModelAnnotationManager sharedDimensionManager = null;

  private static final String XML_METASTORE = "src/test/resources/metastore_test";

  @Before
  public void before() throws IOException, MetaStoreException, KettleException {
    metaStore = new XmlMetaStore( XML_METASTORE );
    sharedDimensionManager = new ModelAnnotationManager( true );
  }

  @Test
  public void testLoadCustomerDimension() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "Customer Dimension", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 7, sdg.getModelAnnotations().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
    assertEquals( 7, sdg.getModelAnnotations().get( 2 ).describeAnnotation().size() );
    assertEquals( 7, sdg.getModelAnnotations().get( 3 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 4 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 5 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 6 ).describeAnnotation().size() );
  }

  @Test
  public void testLoadDateDimension() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "Date Dimension", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 6, sdg.getModelAnnotations().size() );
    assertEquals( 7, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 8, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
    assertEquals( 8, sdg.getModelAnnotations().get( 2 ).describeAnnotation().size() );
    assertEquals( 8, sdg.getModelAnnotations().get( 3 ).describeAnnotation().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 4 ).describeAnnotation().size() );
    assertEquals( 8, sdg.getModelAnnotations().get( 5 ).describeAnnotation().size() );
  }

  @Test
  public void testLoadGeoGroup() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "GeoGroup", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 2, sdg.getModelAnnotations().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
  }

  @Test
  public void testLoadProductDimension() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "Product Dimension", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 7, sdg.getModelAnnotations().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 2 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 3 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 4 ).describeAnnotation().size() );
    assertEquals( 6, sdg.getModelAnnotations().get( 5 ).describeAnnotation().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 6 ).describeAnnotation().size() );
  }

  @Test
  public void testSalesCustomerDimension() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "SalesCustomerDimension", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 7, sdg.getModelAnnotations().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 2 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 3 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 4 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 5 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 6 ).describeAnnotation().size() );
  }

  @Test
  public void testLoadStateGroup() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "StateGroup", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 2, sdg.getModelAnnotations().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
  }

  @Test
  public void testLoadTeams() throws Exception {
    ModelAnnotationGroup group = sharedDimensionManager.readGroup( "Teams", this.metaStore );
    assertNotNull( group );
    assertTrue( group instanceof SharedDimensionGroup );

    SharedDimensionGroup sdg = (SharedDimensionGroup) group;
    assertTrue( sdg.isSharedDimension() );
    assertEquals( 3, sdg.getModelAnnotations().size() );
    assertEquals( 2, sdg.getModelAnnotations().get( 0 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 1 ).describeAnnotation().size() );
    assertEquals( 5, sdg.getModelAnnotations().get( 2 ).describeAnnotation().size() );
  }
}
