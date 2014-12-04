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

package org.pentaho.agilebi.modeler.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.model.Domain;

/**
 * Created: 3/31/11
 *
 * @author rfellows
 */
public class ModelerSourceUtilTest extends AbstractModelerTest {

  private static String locale;

  @Test
  public void testGenerateDomain_SingleModelingMode() throws ModelerException {
    String schemaName = "";
    String tableName = "CUSTOMERS";
    Domain d = ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, tableName, false );
    assertNotNull( d );

    int physicalTables = d.getPhysicalModels().get( 0 ).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get( 0 ).getLogicalTables().size();
    assertEquals( physicalTables, logicalTables );

    int physicalColumns = d.getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().size();
    int logicalColumns = d.getLogicalModels().get( 0 ).getLogicalTables().get( 0 ).getLogicalColumns().size();
    assertEquals( physicalColumns, logicalColumns );
  }

  @Test
  public void testGenerateDomain_DualModelingMode() throws ModelerException {
    String schemaName = "";
    String tableName = "CUSTOMERS";
    Domain d = ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, tableName, true );
    assertNotNull( d );

    assertEquals( 2, d.getLogicalModels().size() );

    int physicalTables = d.getPhysicalModels().get( 0 ).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get( 0 ).getLogicalTables().size();
    assertEquals( physicalTables, logicalTables );

    int physicalColumns = d.getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().size();
    int logicalColumns = 0;
    for ( int i = 0; i < logicalTables; i++ ) {
      logicalColumns += d.getLogicalModels().get( 0 ).getLogicalTables().get( i ).getLogicalColumns().size();
    }
    assertEquals( physicalColumns, logicalColumns );

    physicalTables = d.getPhysicalModels().get( 0 ).getPhysicalTables().size();
    logicalTables = d.getLogicalModels().get( 1 ).getLogicalTables().size();
    assertEquals( physicalTables, logicalTables );

    physicalColumns = d.getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().size();
    logicalColumns = 0;
    for ( int i = 0; i < logicalTables; i++ ) {
      logicalColumns += d.getLogicalModels().get( 1 ).getLogicalTables().get( i ).getLogicalColumns().size();
    }
    assertEquals( physicalColumns, logicalColumns );

  }

  @Test( expected = ModelerException.class )
  public void testGenerateDomain_TableNeedsQuoted() throws ModelerException {
    // this test is checking to make sure that a ModelerException is thrown when we generate a
    // domain from a table that, when quoted, can't be found
    String schemaName = "joe";
    String tableName = "customers";
    Domain d = ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, tableName, true );
  }

  @Test
  public void testAcceptsAStragegyForDefiningRowMeta() throws Exception {
    String schemaName = "";
    String tableName = "CUSTOMERS";
    Domain d = ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, tableName, false,
      rowMetaStrategy() );
    assertNotNull( d );

    int physicalTables = d.getPhysicalModels().get( 0 ).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get( 0 ).getLogicalTables().size();
    assertEquals( physicalTables, logicalTables );

    int physicalColumns = d.getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().size();
    int logicalColumns = d.getLogicalModels().get( 0 ).getLogicalTables().get( 0 ).getLogicalColumns().size();
    assertEquals( 2, physicalColumns );
    assertEquals( 2, logicalColumns );

  }

  private PhysicalTableImporter.RowMetaStrategy rowMetaStrategy() {
    return new PhysicalTableImporter.RowMetaStrategy() {
      @Override public RowMetaInterface rowMeta( final Database database, final String schemaName,
                                                 final String tableName )
        throws KettleDatabaseException {
        RowMeta rowMeta = new RowMeta();
        rowMeta.addValueMeta( new ValueMetaString( "CUSTOMERNAME" ) );
        rowMeta.addValueMeta( new ValueMetaString( "CONTACTLASTNAME" ) );
        return rowMeta;
      }
    };
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
