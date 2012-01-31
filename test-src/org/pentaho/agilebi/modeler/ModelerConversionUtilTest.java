package org.pentaho.agilebi.modeler;

import junit.framework.Assert;
import org.junit.Test;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerConversionUtil;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.util.XmiParser;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

/**
 * User: rfellows
 * Date: 1/26/12
 * Time: 11:20 AM
 */
public class ModelerConversionUtilTest {

  @Test
  public void testUpgradeAndSplitCombinedModel() throws Exception {

    // go get an xmi that needs upgraded
    XmiParser parser = new XmiParser();
    FileInputStream input = new FileInputStream(new File("test-res/multi-table-model-2.0.xmi"));
    Domain domain = parser.parseXmi(input);

    LogicalModel originalModel = domain.getLogicalModels().get(0);
    int originalTableCount = originalModel.getLogicalTables().size();
    int originalJoinCount = originalModel.getLogicalRelationships().size();

    LogicalModel olapModel = ModelerConversionUtil.upgradeAndSplitCombinedModel(originalModel);

    assertNotNull(olapModel);

    // make sure the olap tables are gone
    assertEquals(originalTableCount / 2, originalModel.getLogicalTables().size());
    
    // no _OLAP tables left
    for (LogicalTable table : originalModel.getLogicalTables()) {
      if(table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        fail(table.getId() + " table found in the relational model following the upgrade & split");
      }
    }

    // no non-_OLAP tables in the olap model
    for (LogicalTable table : olapModel.getLogicalTables()) {
      if( !table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX) ) {
        fail(table.getId() + " table found in the olap model following the upgrade & split");
      }
    }

    // make sure they have the same number of tables now
    assertEquals(originalModel.getLogicalTables().size(), olapModel.getLogicalTables().size());

    // check the version numbers
    assertEquals(BaseModelerWorkspaceHelper.AGILE_BI_VERSION, originalModel.getProperty("AGILE_BI_VERSION"));
    assertEquals(BaseModelerWorkspaceHelper.AGILE_BI_VERSION, olapModel.getProperty("AGILE_BI_VERSION"));
    assertEquals("OLAP", olapModel.getProperty("MODELING_SCHEMA"));

    // check the relationships
    assertEquals(originalJoinCount / 2, originalModel.getLogicalRelationships().size());
    assertEquals(originalModel.getLogicalRelationships().size(), olapModel.getLogicalRelationships().size());

    for(int i = 0; i < originalModel.getLogicalRelationships().size(); i++) {
      LogicalRelationship rrel = originalModel.getLogicalRelationships().get(i);
      LogicalRelationship orel = olapModel.getLogicalRelationships().get(i);

      assertEquals(rrel.getFromTable().getPhysicalTable().getId(), orel.getFromTable().getPhysicalTable().getId());
      assertEquals(rrel.getToTable().getPhysicalTable().getId(), orel.getToTable().getPhysicalTable().getId());

      assertEquals(rrel.getFromColumn().getPhysicalColumn().getId(), orel.getFromColumn().getPhysicalColumn().getId());
      assertEquals(orel.getToColumn().getPhysicalColumn().getId(), orel.getToColumn().getPhysicalColumn().getId());
    }
    
  }

  @Test
  public void testUpConvertDomain_v2_multiTable() throws Exception {
    // go get an xmi that needs upgraded
    XmiParser parser = new XmiParser();
    FileInputStream input = new FileInputStream(new File("test-res/multi-table-model-2.0.xmi"));
    Domain domain = parser.parseXmi(input);

    int tableCount = domain.getLogicalModels().get(0).getLogicalTables().size();
    int relationshipCount = domain.getLogicalModels().get(0).getLogicalRelationships().size();

    assertEquals(1, domain.getLogicalModels().size());

    ModelerConversionUtil.upConvertDomain(domain);

    assertEquals(2, domain.getLogicalModels().size());

    assertEquals(tableCount / 2, domain.getLogicalModels().get(0).getLogicalTables().size() );
    assertEquals(tableCount / 2, domain.getLogicalModels().get(1).getLogicalTables().size() );

    assertEquals(relationshipCount / 2, domain.getLogicalModels().get(0).getLogicalRelationships().size() );
    assertEquals(relationshipCount / 2, domain.getLogicalModels().get(1).getLogicalRelationships().size() );

  }

  @Test
  public void testDuplicateModelForOlap() throws Exception {
    // go get an xmi that needs upgraded
    XmiParser parser = new XmiParser();
    FileInputStream input = new FileInputStream(new File("test-res/sql-model-1.0.xmi"));
    Domain d = parser.parseXmi(input);

    LogicalModel logicalModel =  d.getLogicalModels().get(0);

    int physicalTables = d.getPhysicalModels().get(0).getPhysicalTables().size();
    int logicalTables = logicalModel.getLogicalTables().size();
    Assert.assertEquals(physicalTables, logicalTables);

    int physicalColumns = d.getPhysicalModels().get(0).getPhysicalTables().get(0).getPhysicalColumns().size();
    int logicalColumns = logicalModel.getLogicalTables().get(0).getLogicalColumns().size();
    Assert.assertEquals(physicalColumns, logicalColumns);

    LogicalModel newModel = ModelerConversionUtil.duplicateModelForOlap(logicalModel);
    logicalTables = newModel.getLogicalTables().size();
    Assert.assertEquals(physicalTables, logicalTables);

    logicalColumns = 0;
    for (int i = 0; i < logicalTables; i++) {
      logicalColumns += newModel.getLogicalTables().get(i).getLogicalColumns().size();
    }

    Assert.assertEquals(physicalColumns, logicalColumns);

    // makes sure table names aren't the same either
    LogicalTable origTable = logicalModel.getLogicalTables().get(0);
    LogicalTable copyTable = newModel.getLogicalTables().get(0);
    Assert.assertEquals(origTable.getId() + "_OLAP", copyTable.getId());

    // make sure the columns are linked to the *_OLAP table parents and are named *_OLAP as well
    for(LogicalTable table : newModel.getLogicalTables()) {
      assertTrue(table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
      for(LogicalColumn column : table.getLogicalColumns()) {
        assertTrue(column.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
        assertEquals(table.getId(), column.getLogicalTable().getId());
      }
    }

    // verify relationships...
    for(int i = 0; i < logicalModel.getLogicalRelationships().size(); i++) {
      LogicalRelationship rrel = logicalModel.getLogicalRelationships().get(i);
      LogicalRelationship orel = newModel.getLogicalRelationships().get(i);

      assertEquals(rrel.getFromTable().getPhysicalTable().getId(), orel.getFromTable().getPhysicalTable().getId());
      assertEquals(rrel.getToTable().getPhysicalTable().getId(), orel.getToTable().getPhysicalTable().getId());

      assertEquals(rrel.getFromColumn().getPhysicalColumn().getId(), orel.getFromColumn().getPhysicalColumn().getId());
      assertEquals(orel.getToColumn().getPhysicalColumn().getId(), orel.getToColumn().getPhysicalColumn().getId());
    }


  }

  @Test
  public void testUpConvertDomain_v1_sql() throws Exception {
    // go get an xmi that needs upgraded
    XmiParser parser = new XmiParser();
    FileInputStream input = new FileInputStream(new File("test-res/sql-model-1.0.xmi"));
    Domain domain = parser.parseXmi(input);

    int tableCount = domain.getLogicalModels().get(0).getLogicalTables().size();
    assertEquals(1, tableCount);

    assertEquals(1, domain.getLogicalModels().size());

    ModelerConversionUtil.upConvertDomain(domain);

    assertEquals(2, domain.getLogicalModels().size());

    assertEquals(tableCount, domain.getLogicalModels().get(0).getLogicalTables().size() );
    assertEquals(tableCount, domain.getLogicalModels().get(1).getLogicalTables().size() );

  }


}
