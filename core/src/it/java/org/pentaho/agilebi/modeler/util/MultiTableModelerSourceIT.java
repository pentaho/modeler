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


package org.pentaho.agilebi.modeler.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.olap.OlapCube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MultiTableModelerSourceIT extends AbstractModelerTest {

  private static Logger logger = LoggerFactory.getLogger( MultiTableModelerSourceIT.class );

  @BeforeClass
  public static void callParentSetup() {
    AbstractModelerTest.setupBeforeClass();
  }

  @Test
  public void testStarSchema() throws Exception {

    MultiTableModelerSource multiTable =
        new MultiTableModelerSource( this.getDatabase(), getSchemaModel1( true ), this.getDatabase().getName(),
            Arrays.asList( "CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE" ) );
    Domain domain = multiTable.generateDomain( true );

    assertNotNull( domain );
    LogicalModel olapModel = domain.getLogicalModels().get( 1 );
    List<OlapCube> cubes = (List) olapModel.getProperty( "olap_cubes" );
    OlapCube cube = cubes.get( 0 );

    // Ensure cube has a fact table.
    assertNotNull( cube.getLogicalTable() );
    // Ensure we have logical relationships (joins).
    assertEquals( true, olapModel.getLogicalRelationships().size() > 0 );
    // Ensure all joins use Olap tables.
    LogicalRelationship logicalRelationship = olapModel.getLogicalRelationships().get( 0 );
    assertEquals( true, logicalRelationship.getToTable().getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) );
    assertEquals( true, logicalRelationship.getFromTable().getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) );
  }

  @Test
  public void testReportingSchema() throws Exception {

    MultiTableModelerSource multiTable =
        new MultiTableModelerSource( this.getDatabase(), getSchemaModel1( false ), this.getDatabase().getName(),
            Arrays.asList( "CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE" ) );
    Domain domain = multiTable.generateDomain( false );
    // Ensure domain was created.
    assertNotNull( domain );
    // Ensure we have logical relationships (joins).
    assertEquals( true, domain.getLogicalModels().get( 0 ).getLogicalRelationships().size() > 0 );
    // Ensure all joins DO NOT use Olap tables.
    for ( LogicalRelationship logicalRelationship : domain.getLogicalModels().get( 0 ).getLogicalRelationships() ) {
      assertEquals( true,
          !logicalRelationship.getToTable().getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) );
      assertEquals( true,
          !logicalRelationship.getFromTable().getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) );
    }
  }

  public static SchemaModel getSchemaModel1() {
    return getSchemaModel1( true );
  }

  public static SchemaModel getSchemaModel1( boolean doOlap ) {
    List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

    JoinTableModel joinTable1 = new JoinTableModel();
    joinTable1.setName( "ORDERFACT" );

    JoinTableModel joinTable2 = new JoinTableModel();
    joinTable2.setName( "CUSTOMERS" );

    JoinRelationshipModel join1 = new JoinRelationshipModel();
    JoinFieldModel lField1 = new JoinFieldModel();
    lField1.setName( "CUSTOMERNUMBER" );
    lField1.setParentTable( joinTable1 );
    join1.setLeftKeyFieldModel( lField1 );

    JoinFieldModel rField1 = new JoinFieldModel();
    rField1.setName( "CUSTOMERNUMBER" );
    rField1.setParentTable( joinTable2 );
    join1.setRightKeyFieldModel( rField1 );

    joins.add( join1 );
    SchemaModel model = new SchemaModel();
    model.setJoins( joins );
    if ( doOlap ) {
      model.setFactTable( joinTable1 );
    }
    return model;
  }

  private DatabaseMeta getDatabase() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType( "Hypersonic" );
    database.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    database.setDBName( "SampleData" );
    database.setName( "SampleData" );
    return database;
  }
}
