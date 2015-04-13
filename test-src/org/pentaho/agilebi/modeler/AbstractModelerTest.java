/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSourceTest;
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

  static {

    System.setProperty( "org.osjava.sj.root", "test-res/solution1/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      throw new IllegalStateException( "Unable to initialize Kettle.", e );
    }
    Props.init( Props.TYPE_PROPERTIES_EMPTY );
  }

  @Before
  public void setUp() throws Exception {

    if ( ModelerMessagesHolder.getMessages() == null ) {
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    }

    Reader propsReader = new FileReader( new File( "test-res/geoRoles.properties" ) );
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

    SchemaModel schemaModel = MultiTableModelerSourceTest.getSchemaModel1( true );

    MultiTableModelerSource modelerSource =
        new MultiTableModelerSource( getDatabaseMeta(), schemaModel, "TEST", Arrays.asList( "CUSTOMERS", "PRODUCTS",
        "CUSTOMERNAME", "PRODUCTCODE" ) );

    Domain d = modelerSource.generateDomain( true );
    workspace.setDomain( d );
  }

  protected void generateMultiTableTestDomain() throws ModelerException {
    SchemaModel schemaModel = MultiTableModelerSourceTest.getSchemaModel1( false );

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
