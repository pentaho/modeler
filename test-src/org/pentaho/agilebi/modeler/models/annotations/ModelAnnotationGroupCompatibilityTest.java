package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupCompatibilityTest {

  private static String XML_METASTORE = "test-res/metastore_test";

  private IMetaStore metaStore = null;
  private ModelAnnotationManager modelAnnotationManager = null;

  @Before
  public void before() throws IOException, MetaStoreException {
    metaStore = new XmlMetaStore( XML_METASTORE );
    modelAnnotationManager = new ModelAnnotationManager();
  }

  @Test
  public void testLoadMyCategory() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "My Category", this.metaStore );
    assertNotNull( group );
    assertEquals( group.size(), 100 );
    for ( ModelAnnotation m : group ) {
      assertNotNull( m );
      assertNotNull( m.getName() );
      assertNotNull( m.getField() );
      assertNotNull( m.getAnnotation() );
      assertNotNull( m.getAnnotation().getName() );
    }
  }

  @Test
  public void testLoadMyGroup() throws Exception {
    ModelAnnotationGroup group = modelAnnotationManager.readGroup( "My Group", this.metaStore );
    assertNotNull( group );
    assertEquals( group.size(), 1 );

    ModelAnnotation m = group.get( 0 );
    assertNotNull( m );
    assertNotNull( m.getName() );
    assertEquals( m.getName(), "4aae4455-5d06-4b13-a14c-f51a5a78a220" );
    assertNotNull( m.getField() );
    assertEquals( m.getField(), "f1" );

    CreateDimensionKey a = (CreateDimensionKey) m.getAnnotation();
    assertNotNull( a );
    assertNotNull( a.getName() );
    assertEquals( a.getName(), "f1" );
    assertNotNull( a.getDimension() );
    assertEquals( a.getDimension(), "1dim" );
  }
}
