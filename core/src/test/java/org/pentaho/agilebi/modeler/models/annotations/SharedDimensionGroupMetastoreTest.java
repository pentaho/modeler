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
