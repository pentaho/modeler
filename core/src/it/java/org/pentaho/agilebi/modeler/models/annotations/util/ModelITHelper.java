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


package org.pentaho.agilebi.modeler.models.annotations.util;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

public class ModelITHelper {
  
  private static final String GEO_ROLE_PROPERTIES = "src/it/resources/geoRoles.properties";

  private static DatabaseMeta newH2Db( String dbName ) {
    // DB Setup
    String dbDir = "target/test-db/" + dbName;
    File file = new File( dbDir + ".h2.db" );
    if ( file.exists() ) {
      file.delete();
    }
    DatabaseMeta dbMeta = new DatabaseMeta( "myh2", "HYPERSONIC", "Native", null, dbDir, null, "sa", null );
    return dbMeta;
  }

  public static DatabaseMeta newH2Db( String dbName, String... statements ) throws Exception {
    DatabaseMeta dbMeta = newH2Db( dbName );
    Database db = new Database( null, dbMeta );
    db.connect();
    for ( String stmt : statements ) {
      db.execStatement( stmt );
    }
    db.disconnect();
    return dbMeta;
  }

  public static ModelerWorkspace modelTable( String dbName, String tableName, String... statements )
    throws Exception {
    DatabaseMeta dbMeta = newH2Db( dbName, statements );
    Reader propsReader = new FileReader( new File( GEO_ROLE_PROPERTIES ) );
    Properties props = new Properties();
    props.load( propsReader );
    GeoContextConfigProvider config = new GeoContextPropertiesProvider( props );
    GeoContext geoContext = GeoContextFactory.create( config );
    TableModelerSource source = new TableModelerSource( dbMeta, tableName, "" );
    Domain domain = source.generateDomain();
    ModelerWorkspace workspace = new ModelerWorkspace( new ModelerWorkspaceHelper( "en_US" ), geoContext );
    workspace.setModelSource( source );
    workspace.setDomain( domain );
    workspace.setModelName( "someModel" );
    workspace.getWorkspaceHelper().autoModelFlat( workspace );
    workspace.getWorkspaceHelper().populateDomain( workspace );
    return workspace;
  }

  public static  ModelerWorkspace prepareOrderModel( String dbName ) throws Exception {
    String dropOrderFact = "DROP TABLE IF EXISTS orderfact;";
    String dropProduct = "DROP TABLE IF EXISTS product;";
    String dropMydate = "DROP TABLE IF EXISTS mydate;";
    String createOrderFact = "CREATE TABLE orderfact\n"
      + "(\n"
      + "   ordernumber int,\n"
      + "   product_id int,\n"
      + "   quantityordered int\n,"
      + "   date Date"
      + ");\n";
    String createProduct = "CREATE TABLE product\n"
      + "(\n"
      + "   product_id int,\n"
      + "   product_name varchar(50),\n"
      + "   product_description varchar(50)\n"
      + ");\n";
    String createMyDate = "CREATE TABLE mydate\n"
      + "(\n"
      + "   date Date,\n"
      + "   year varchar(50),\n"
      + "   month varchar(50)\n"
      + ");\n";
    return modelTable(
      dbName, "orderfact",
      dropOrderFact, dropProduct, dropMydate, createOrderFact, createProduct, createMyDate );
  }

  public static  ModelerWorkspace prepareGeoModel( String dbName ) throws Exception {
    String dropGeo = "DROP TABLE if exists geodata;";
    String createGeo = "CREATE TABLE geodata\n"
      + "(\n"
      + "  id bigint\n"
      + ", state_fips bigint\n"
      + ", state varchar(25)\n"
      + ", state_abbr varchar(4)\n"
      + ", zipcode varchar(10)\n"
      + ", country varchar(45)\n"
      + ", city varchar(45)\n"
      + ", lat numeric(16, 4)\n"
      + ", long numeric(16, 4)\n"
      + ");\n";
    return modelTable(
      dbName, "geodata",
      dropGeo, createGeo );
  }
}
