package org.pentaho.agilebi.modeler;

import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapMeasure;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Created: 3/25/11
 *
 * @author rfellows
 */
public class BaseModelerWorkspaceHelperTest extends AbstractModelerTest {

  private static final String LOCALE = "en-US";
  RelationalModelNode relationalModelNode;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    super.generateTestDomain();
  }

  @Test
  public void testFieldAggregations() throws ModelerException{

    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(LOCALE);
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);

    FieldMetaData firstField = workspace.getRelationalModel().get(0).get(0).get(0);
    assertEquals( firstField.getTextAggregationTypes(), firstField.getSelectedAggregations());
    assertEquals(AggregationType.NONE, firstField.getDefaultAggregation());
    firstField.setDefaultAggregation(AggregationType.COUNT);
    assertEquals(AggregationType.COUNT, firstField.getDefaultAggregation());
    firstField.setSelectedAggregations(Arrays.asList(AggregationType.COUNT));

    //Test Olap side
    assertEquals(workspace.getModel().getMeasures().get(0).getDefaultAggregation(), AggregationType.SUM);

    helper.populateDomain(workspace);

    // Now verify with the generated models
    LogicalModel logicalModel = workspace.getLogicalModel(ModelerPerspective.REPORTING);
    LogicalColumn lCol = logicalModel.getCategories().get(0).getLogicalColumns().get(0);
    assertEquals(firstField.getDefaultAggregation(), lCol.getAggregationType());
    assertEquals(firstField.getSelectedAggregations(), lCol.getAggregationList());


    List<OlapCube> cubes = (List<OlapCube>) workspace.getLogicalModel(ModelerPerspective.ANALYSIS).getProperty("olap_cubes");
    OlapMeasure measure = cubes.get(0).getOlapMeasures().get(0);
    assertEquals(AggregationType.SUM, measure.getLogicalColumn().getAggregationType());

  }

  @Test
  public void testPopulateCategories() throws ModelerException {
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(LOCALE);
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();

    int fields = tablesList.get(0).getAvailableFields().size();
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);
    helper.populateCategories(workspace);

    List<Category> categories = logicalModel.getCategories();

    assertEquals(1, categories.size());
    assertEquals(fields, tablesList.get(0).getAvailableFields().size());
    System.out.println(logicalModel.getLogicalTables().get(0).getLogicalColumns().size());
    assertEquals(tablesList.get(0).getAvailableFields().size(), categories.get(0).getLogicalColumns().size());

    for (LogicalColumn lCol : categories.get(0).getLogicalColumns()) {
      FieldMetaData orig = null;
      for (FieldMetaData fieldMetaData : workspace.getRelationalModel().getCategories().get(0)) {
        if (lCol.getId().equals(fieldMetaData.getLogicalColumn().getId())) {
          orig = fieldMetaData;
          break;
        }
      }
      assertNotNull(orig);
      assertEquals(orig.getDefaultAggregation(), lCol.getAggregationType());
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
  public void testPopulateCategories_MultipleCategoriesAggregationTypesAndFormatMasks() throws ModelerException {
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(LOCALE);
    LogicalModel logicalModel = workspace.getLogicalModel(ModelerPerspective.REPORTING);
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);
    spiceUpRelationalModel(workspace.getRelationalModel());
    helper.populateCategories(workspace);

    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();

    List<Category> categories = logicalModel.getCategories();
    assertEquals(2, categories.size());
    assertEquals(tablesList.get(0).getAvailableFields().size(), categories.get(0).getLogicalColumns().size());
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
        assertEquals(orig.getDefaultAggregation(), lCol.getAggregationType());
        if (orig.getFormat().equals("NONE")) {
          if (orig.getLogicalColumn().getDataType() == DataType.NUMERIC) {
            assertTrue(((String) lCol.getProperty("mask")).indexOf("#") > -1);
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
    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();

    FieldMetaData field = null;
    AvailableField avaialbleField = null;
    for (AvailableField af : tablesList.get(0).getAvailableFields()) {
      if (af.getPhysicalColumn().getDataType() == DataType.NUMERIC) {
        avaialbleField = af;
        break;
      }
    }
    field = workspace.createFieldForParentWithNode(cat, avaialbleField);
    field.setFormat("$#,###.##");
    field.setDefaultAggregation(AggregationType.SUM);
    cat.add(field);

    return cat;
  }

}
