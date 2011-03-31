package org.pentaho.agilebi.modeler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created: 3/25/11
 *
 * @author rfellows
 */
public class BaseModelerWorkspaceHelperTest {

  private static final String LOCALE = "en-US";
  RelationalModelNode relationalModelNode;
  static ModelerWorkspace workspace;

  @BeforeClass
  public static void init() throws KettleException {
    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    workspace = new ModelerWorkspace(new ModelerWorkspaceHelper(LOCALE));
    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
      Domain d = ModelerSourceUtil.generateDomain(getDatabaseMeta(), "", "CUSTOMERS");
      workspace.setDomain(d);
    } catch (ModelerException e) {
      e.printStackTrace();
    } catch (KettleException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testPopulateCategories() throws ModelerException {
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(LOCALE);
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    int fields = workspace.getAvailableFields().size();
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);
    helper.populateCategories(workspace);

    List<Category> categories = logicalModel.getCategories();
    
    assertEquals(1, categories.size());
    assertEquals(fields, workspace.getAvailableFields().size());
    System.out.println(logicalModel.getLogicalTables().get(0).getLogicalColumns().size());
    assertEquals(workspace.getAvailableFields().size(), categories.get(0).getLogicalColumns().size());

    for (LogicalColumn lCol : categories.get(0).getLogicalColumns()) {
      FieldMetaData orig = null;
      for (FieldMetaData fieldMetaData : workspace.getRelationalModel().getCategories().get(0)) {
        if (lCol.getId().equals(fieldMetaData.getLogicalColumn().getId())) {
          orig = fieldMetaData;
          break;
        }
      }
      assertNotNull(orig);
      assertEquals(orig.getAggTypeDesc(), lCol.getAggregationType().name());
      if (orig.getFormat().equals("NONE")) {
        if (orig.getLogicalColumn().getDataType() == DataType.NUMERIC) {
          assertTrue(lCol.getProperty("mask") == "#");
        } else {
          assertTrue(lCol.getProperty("mask") == null);
        }
      } else {
        assertEquals(orig.getFormat(), lCol.getProperty("mask"));
      }
    }
  }

  @Test
  public void testPopulateCategories_MultipleCategoriesAggTypesAndFormatMasks() throws ModelerException {
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(LOCALE);
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);
    spiceUpRelationalModel(workspace.getRelationalModel());
    helper.populateCategories(workspace);

    List<Category> categories = logicalModel.getCategories();
    assertEquals(2, categories.size());
    assertEquals(workspace.getAvailableFields().size(), categories.get(0).getLogicalColumns().size());
    System.out.println(logicalModel.getLogicalTables().get(0).getLogicalColumns().size());

    assertEquals(1, categories.get(1).getLogicalColumns().size());

    for (int i = 0; i < categories.size(); i++) {

      for (LogicalColumn lCol : categories.get(i).getLogicalColumns()) {
        FieldMetaData orig = null;
        for (FieldMetaData fieldMetaData : workspace.getRelationalModel().getCategories().get(i)) {
          if (lCol.getId().equals(fieldMetaData.getLogicalColumn().getId())) {
            orig = fieldMetaData;
            break;
          }
        }
        assertNotNull(orig);
        assertEquals(orig.getAggTypeDesc(), lCol.getAggregationType().name());
        if (orig.getFormat().equals("NONE")) {
          if (orig.getLogicalColumn().getDataType() == DataType.NUMERIC) {
            assertTrue(lCol.getProperty("mask") == "#");
          } else {
            assertTrue(lCol.getProperty("mask") == null);
          }
        } else {
          assertEquals(orig.getFormat(), lCol.getProperty("mask"));
        }
      }
    }
  }

  private void spiceUpRelationalModel(RelationalModelNode model) {
    model.getCategories().add(createCategoryMetaData("Test Category"));
  }

  private CategoryMetaData createCategoryMetaData(String name) {
    CategoryMetaData cat = new CategoryMetaData(name);

    FieldMetaData field = null;
    AvailableField avaialbleField = null;
    for (AvailableField af : workspace.getAvailableFields()) {
      if (af.getLogicalColumn().getDataType() == DataType.NUMERIC) {
        avaialbleField = af;
        break;
      }
    }
    field = workspace.createFieldForParentWithNode(cat, avaialbleField);
    field.setFormat("$#,###.##");
    field.setAggTypeDesc("SUM");
    cat.add(field);

    return cat;
  }

  public static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType("Hypersonic");//$NON-NLS-1$
    database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
    database.setDBName("SampleData");//$NON-NLS-1$
    database.setName("SampleData");//$NON-NLS-1$
    return database;
  }

  @Test
  public void testGetCorrespondingOlapColumnId() {
    String origTableId = "BT_CUSTOMERS_CUSTOMERS";
    String origColumnId = "LC_CUSTOMERS_CUSTOMERNUMBER";
    String olapTableId = "BT_CUSTOMERS_CUSTOMERS_OLAP";
    String olapColumnId = "LC_CUSTOMERS_CUSTOMERS_OLAP_CUSTOMERNUMBER";

    LogicalTable ltOrig = new LogicalTable();
    ltOrig.setId(origTableId);

    LogicalColumn lcOrig = new LogicalColumn();
    lcOrig.setId(origColumnId);
    lcOrig.setLogicalTable(ltOrig);
    IPhysicalColumn pc = new SqlPhysicalColumn();
    pc.setId("CUSTOMERNUMBER");
    lcOrig.setPhysicalColumn(pc);


    assertEquals(olapColumnId, BaseModelerWorkspaceHelper.getCorrespondingOlapColumnId(lcOrig));

  }



}
