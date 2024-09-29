/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSourceIT;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;

/**
 * User: nbaker Date: 4/8/11
 */
@Ignore
public class AbstractModelerTest {

  protected ModelerWorkspace workspace;
  protected DatabaseMeta databaseMeta;

  private static final String LOCALE = "en-US";

  @BeforeClass
  public static void setupBeforeClass() {
    System.setProperty( "org.osjava.sj.root", "src/it/resources/solution1/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      if ( !KettleEnvironment.isInitialized() ) {
        KettleEnvironment.init();
      }
    } catch ( KettleException e ) {
      throw new IllegalStateException( "Unable to initialize Kettle.", e );
    }
    if ( ! Props.isInitialized() ) {
      Props.init( Props.TYPE_PROPERTIES_EMPTY );
    }
  }

  @Before
  public void setUp() throws Exception {

    if ( ModelerMessagesHolder.getMessages() == null ) {
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    }

    Reader propsReader = new FileReader( new File( "src/it/resources/geoRoles.properties" ) );
    Properties props = new Properties();
    props.load( propsReader );
    GeoContextConfigProvider config = new GeoContextPropertiesProvider( props );
    GeoContext geo = GeoContextFactory.create( config );

    workspace = new ModelerWorkspace( new ModelerWorkspaceHelper( LOCALE ), geo );
    databaseMeta = getDatabaseMeta();
  }

  protected void generateTestDomain() throws ModelerException {
    Domain d = ModelerSourceUtil.generateDomain( getDatabaseMeta(), "", "CUSTOMERS" );
    workspace.setDomain( d );
  }

  protected void generateMultiStarTestDomain() throws Exception {

    SchemaModel schemaModel = MultiTableModelerSourceIT.getSchemaModel1(true);

    MultiTableModelerSource modelerSource =
        new MultiTableModelerSource( getDatabaseMeta(), schemaModel, "TEST", Arrays.asList( "CUSTOMERS", "PRODUCTS",
        "CUSTOMERNAME", "PRODUCTCODE" ) );

    Domain d = modelerSource.generateDomain( true );
    workspace.setDomain( d );
  }

  protected void generateMultiTableTestDomain() throws ModelerException {
    SchemaModel schemaModel = MultiTableModelerSourceIT.getSchemaModel1(false);

    MultiTableModelerSource modelerSource =
        new MultiTableModelerSource( getDatabaseMeta(), schemaModel, "TEST", Arrays.asList( "CUSTOMERS", "PRODUCTS",
        "CUSTOMERNAME", "PRODUCTCODE" ) );

    Domain d = modelerSource.generateDomain( false );
    workspace.setDomain( d );
  }

  public static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType( "Hypersonic" ); //$NON-NLS-1$
    database.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    database.setDBName( "SampleData" ); //$NON-NLS-1$
    database.setName( "SampleData" ); //$NON-NLS-1$
    return database;
  }
}
