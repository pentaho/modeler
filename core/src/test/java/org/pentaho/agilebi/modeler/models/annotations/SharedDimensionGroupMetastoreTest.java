/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharedDimensionGroupMetastoreTest {

  private String tempDir = null;
  private IMetaStore metaStore = null;
  private ModelAnnotationManager sharedDimensionManager = null;

  private static final String XML_METASTORE = "src/test/resources/metastore_test";

  @Before
  public void before() throws IOException, MetaStoreException, KettleException {
    File f = File.createTempFile( "ModelAnnotationManageTest", "test" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
    sharedDimensionManager = new ModelAnnotationManager( true );
  }

  @After
  public void after() throws IOException {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  @Test
  public void createAndVerifySharedDimensionGroup() throws Exception {

    SharedDimensionGroup sdg = createSharedDimensionGroup();

    sharedDimensionManager.createGroup( sdg, metaStore );

    ModelAnnotationGroup loadedGroup = sharedDimensionManager.readGroup( "My Group", metaStore );
    assertTrue( loadedGroup instanceof SharedDimensionGroup );

    verifySavedAndLoadedSharedDimensions( sdg, (SharedDimensionGroup) loadedGroup );
  }

  @Test
  public void verifySavedAndLoadedSharedDimensionsFromXML() throws Exception {

    SharedDimensionGroup sdg = createSharedDimensionGroup();

    ModelAnnotationManager modelAnnotationManager = new ModelAnnotationManager( true );
    XmlMetaStore xmlMetaStore = new XmlMetaStore( XML_METASTORE );
    SharedDimensionGroup loaded = (SharedDimensionGroup) modelAnnotationManager.readGroup( "My Group", xmlMetaStore );

    verifySavedAndLoadedSharedDimensions( sdg, loaded );
  }

  private SharedDimensionGroup createSharedDimensionGroup() {
    CreateDimensionKey cdk = new CreateDimensionKey();
    cdk.setDimension( "Some Dimension" );
    cdk.setName( "Some Name" );

    CreateAttribute ca = new CreateAttribute();
    ca.setName( "Attribute Name" );
    ca.setDimension( "Some Dimension" );
    ca.setDescription( "Some Description" );
    ca.setBusinessGroup( "Some Business Group" );
    ca.setGeoType( ModelAnnotation.GeoType.City );
    ca.setHierarchy( "Some hierarchy" );
    ca.setOrdinalField( "Some Ordinal Field" );
    ca.setParentAttribute( "Some Parent Attribute" );
    ca.setTimeFormat( "Some Time Format" );
    ca.setTimeType( ModelAnnotation.TimeType.TimeDays );
    ca.setUnique( true );

    ModelAnnotation<?> m1 = new ModelAnnotation<AnnotationType>( "f1", cdk );
    ModelAnnotation<?> m2 = new ModelAnnotation<AnnotationType>( "f2", ca );

    List<DataProvider> dpList = new ArrayList<DataProvider>();
    DataProvider dp = new DataProvider();
    dp.setName( "Some Data Provider" );
    dp.setDatabaseMetaNameRef( "Some Database Meta Name Ref" );
    dp.setSchemaName( "Some Schema Name" );
    dp.setTableName( "Some Table Name" );
    dp.setColumnMappings( new ArrayList<ColumnMapping>() );
    dpList.add( dp );

    SharedDimensionGroup sdg = new SharedDimensionGroup();
    sdg.setName( "My Group" );
    sdg.setDescription( "My Description" );
    sdg.setId( "My Id" );
    sdg.setSharedDimension( true );
    sdg.add( m1 );
    sdg.add( m2 );
    sdg.setDataProviders( dpList );

    return sdg;
  }

  private void verifySavedAndLoadedSharedDimensions( SharedDimensionGroup saved, SharedDimensionGroup loaded ) {
    assertEquals( saved.getId(), loaded.getId() );
    assertEquals( saved.getName(), loaded.getName() );
    assertEquals( saved.getDescription(), loaded.getDescription() );
    assertEquals( saved.isSharedDimension(), loaded.isSharedDimension() );

    verifySavedAndLoadedAnnotations( saved.getModelAnnotations(), loaded.getModelAnnotations() );
  }


  private void verifySavedAndLoadedAnnotations( List<ModelAnnotation> saved, List<ModelAnnotation> loaded ) {
    assertEquals( saved.size(), loaded.size() );

    for ( int i = 0; i < saved.size(); i++ ) {
      ModelAnnotation s = saved.get( i );
      ModelAnnotation l = saved.get( i );
      assertEquals( s, l );
      assertEquals( s.getAnnotation(), l.getAnnotation() );
    }
  }
}
