/*
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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created: 3/31/11
 *
 * @author rfellows
 */
public class ModelerSourceUtilTest {

  private static DatabaseMeta databaseMeta;
  private static String locale;

  @BeforeClass
  public static void setup() throws KettleException {
    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    KettleEnvironment.init();
    Props.init(Props.TYPE_PROPERTIES_EMPTY);

    databaseMeta = getDatabaseMeta();
    locale = "en-US";
  }

  @Test
  public void testGenerateDomain_SingleModelingMode() throws ModelerException {
    String schemaName = "";
    String tableName = "CUSTOMERS";
    Domain d = ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableName, tableName, false);
    assertNotNull(d);

    int physicalTables = d.getPhysicalModels().get(0).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get(0).getLogicalTables().size();
    assertEquals(physicalTables, logicalTables);

    int physicalColumns = d.getPhysicalModels().get(0).getPhysicalTables().get(0).getPhysicalColumns().size();
    int logicalColumns = d.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns().size();
    assertEquals(physicalColumns, logicalColumns);
  }

  @Test
  public void testGenerateDomain_DualModelingMode() throws ModelerException {
    String schemaName = "";
    String tableName = "CUSTOMERS";
    Domain d = ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableName, tableName, true);
    assertNotNull(d);

    int physicalTables = d.getPhysicalModels().get(0).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get(0).getLogicalTables().size();
    assertEquals(physicalTables * 2, logicalTables);

    int physicalColumns = d.getPhysicalModels().get(0).getPhysicalTables().get(0).getPhysicalColumns().size();
    int logicalColumns = 0;
    for (int i = 0; i < logicalTables; i++) {
      logicalColumns += d.getLogicalModels().get(0).getLogicalTables().get(i).getLogicalColumns().size();
    }
    assertEquals(physicalColumns * 2, logicalColumns);

  }

  @Test
  public void testDuplicateLogicalTablesForDualModelingMode() throws ModelerException {
    String schemaName = "";
    String tableName = "CUSTOMERS";

    Domain d = ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableName, tableName, false);
    LogicalModel logicalModel =  d.getLogicalModels().get(0);

    int physicalTables = d.getPhysicalModels().get(0).getPhysicalTables().size();
    int logicalTables = d.getLogicalModels().get(0).getLogicalTables().size();
    assertEquals(physicalTables, logicalTables);

    int physicalColumns = d.getPhysicalModels().get(0).getPhysicalTables().get(0).getPhysicalColumns().size();
    int logicalColumns = d.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns().size();
    assertEquals(physicalColumns, logicalColumns);

    BaseModelerWorkspaceHelper.duplicateLogicalTablesForDualModelingMode(logicalModel);
    logicalTables = d.getLogicalModels().get(0).getLogicalTables().size();
    assertEquals(physicalTables * 2, logicalTables);

    logicalColumns = 0;
    for (int i = 0; i < logicalTables; i++) {
      logicalColumns += d.getLogicalModels().get(0).getLogicalTables().get(i).getLogicalColumns().size();
    }
    
    assertEquals(physicalColumns * 2, logicalColumns);

    // makes sure table names aren't the same either
    LogicalTable origTable = logicalModel.getLogicalTables().get(0);
    LogicalTable copyTable = logicalModel.getLogicalTables().get(1);
    assertEquals(origTable.getId() + "_OLAP", copyTable.getId());

    // make sure the column id's & names are not equal between the copies
    List<LogicalColumn> orig = logicalModel.getLogicalTables().get(0).getLogicalColumns();
    List<LogicalColumn> copy = logicalModel.getLogicalTables().get(1).getLogicalColumns();
    for(int i = 0; i < physicalColumns; i++) {
      assertCopiesAreKosher(orig.get(i), copy.get(i));
    }
  }

  private void assertCopiesAreKosher(LogicalColumn lc1, LogicalColumn lc2) {
    assertEquals(lc1.getPhysicalColumn(), lc2.getPhysicalColumn());

    String newColName = lc1.getLogicalTable().getId().replaceAll("BT", "LC");
    newColName += "_OLAP_" + lc1.getPhysicalColumn().getId();
    
    assertEquals(newColName, lc2.getId());
    assertEquals(lc1.getAggregationType(), lc2.getAggregationType());
    assertEquals(lc1.getDataType(), lc2.getDataType());
  }

  public static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType("Hypersonic");//$NON-NLS-1$
    database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
    database.setDBName("SampleData");//$NON-NLS-1$
    database.setName("SampleData");//$NON-NLS-1$
    return database;
  }


}
