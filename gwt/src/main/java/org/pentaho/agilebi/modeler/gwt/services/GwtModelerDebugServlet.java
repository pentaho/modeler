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

package org.pentaho.agilebi.modeler.gwt.services;

import java.io.File;
import java.io.FileInputStream;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * User: nbaker Date: Jun 18, 2010
 */
public class GwtModelerDebugServlet extends RemoteServiceServlet implements IGwtModelerService {
  private static final long serialVersionUID = 3877637597240414312L;

  static {
    try {
      KettleEnvironment.init();
      Props.init( Props.TYPE_PROPERTIES_EMPTY );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  public Domain generateDomain( String connectionName, String tableName, String dbType, String query,
      String datasourceName ) throws Exception {

    try {
      DatabaseMeta database = new DatabaseMeta();
      database.setDatabaseInterface( new MySQLDatabaseMeta() );
      database.setDatabaseType( "mysql" );
      database.setUsername( "root" );
      database.setPassword( "" );
      database.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
      database.setHostname( "localhost" );
      database.setDBName( "hibernate" );
      database.setDBPort( "3306" );

      TableModelerSource source = new TableModelerSource( database, tableName, null );
      Domain d = null;
      try {
        d = source.generateDomain();
      } catch ( ModelerException e ) {
        e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
      }

      return d;
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return new BogoPojo();
  }

  public String serializeModels( Domain domain, String name ) throws Exception {
    // Do nothing in debug mode.
    return null;
  }

  public Domain loadDomain( String id ) throws Exception {
    XmiParser parser = new XmiParser();
    try {
      return parser.parseXmi( new FileInputStream( new File( "src/test/resources/example_olap.xmi" ) ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      throw e;
    }
  }

  @Override
  public String serializeModels( Domain domain, String name, boolean doOlap ) throws Exception {
    return null;
  }
}
