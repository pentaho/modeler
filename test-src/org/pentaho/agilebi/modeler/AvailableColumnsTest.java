package org.pentaho.agilebi.modeler;

import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.metadata.model.LogicalModel;

import java.util.List;

import static org.junit.Assert.*;

/**
 * User: nbaker
 * Date: 4/8/11
 */
public class AvailableColumnsTest extends AbstractModelerTest{

  @Test
  public void testAvailableColumnPossibleMeasure() throws ModelerException {
    super.generateTestDomain();
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();
    int fields = tablesList.get(0).getAvailableFields().size();
    assertTrue(fields > 2);

    // replicating work that would be done by the auto-modeler
    AvailableField field1 = tablesList.get(0).getAvailableFields().get(0);

    workspace.getDomain().getLogicalModels().get(0).setProperty("AGILE_BI_VERSION", "2.0");
    tablesList.get(0).getPhysicalTable().setProperty("FACT_TABLE", true);

    AvailableField field2 = tablesList.get(0).getAvailableFields().get(1);


    // Add the first available field as a measure. This should work
    workspace.getModel().getMeasures().clear();
    ModelerController controller = new ModelerController(workspace);
    controller.setSelectedFields(new Object[]{field1});
    controller.getDimTreeHelper().setSelectedTreeItem(workspace.getModel().getMeasures());
    controller.addField();
    assertEquals(1, workspace.getModel().getMeasures().size());
    assertEquals(field1.getPhysicalColumn(), workspace.getModel().getMeasures().get(0).getLogicalColumn().getPhysicalColumn());

    // Attempt to add the second field as a measure. It should fail and the list remain the same
    controller.setSelectedFields(new Object[]{field2});
    tablesList.get(0).getPhysicalTable().setProperty("FACT_TABLE", false);
    controller.addField();
    assertEquals(1, workspace.getModel().getMeasures().size());
        

  }
}
