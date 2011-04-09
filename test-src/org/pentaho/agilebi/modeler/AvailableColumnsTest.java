package org.pentaho.agilebi.modeler;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.metadata.model.LogicalModel;

/**
 * User: nbaker
 * Date: 4/8/11
 */
public class AvailableColumnsTest extends AbstractModelerTest{

  @Test
  public void testAvailableColumnPossibleMeasure() throws ModelerException {
    super.generateTestDomain();
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    int fields = workspace.getAvailableFields().size();
    assertTrue(fields > 2);

    // replicating work that would be done by the auto-modeler
    AvailableField field1 = workspace.getAvailableFields().get(0);
    field1.setPossibleMeasure(true);

    AvailableField field2 = workspace.getAvailableFields().get(1);
    field2.setPossibleMeasure(false);

    assertTrue(field1.isPossibleMeasure());
    assertFalse(field2.isPossibleMeasure());

    // Add the first available field as a measure. This should work
    workspace.getModel().getMeasures().clear();
    ModelerController controller = new ModelerController(workspace);
    controller.setSelectedFields(new Object[]{field1});
    controller.getDimTreeHelper().setSelectedTreeItem(workspace.getModel().getMeasures());
    controller.addField();
    assertTrue(workspace.getModel().getMeasures().size() == 1);
    assertEquals(field1.getLogicalColumn().getPhysicalColumn(), workspace.getModel().getMeasures().get(0).getLogicalColumn().getPhysicalColumn());

    // Attempt to add the second field as a measure. It should fail and the list remain the same
    controller.setSelectedFields(new Object[]{field2});
    controller.addField();
    assertTrue(workspace.getModel().getMeasures().size() == 1);
        

  }
}
