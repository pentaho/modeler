package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.util.Map;

/**
 * Created: 3/22/11
 *
 * @author rfellows
 */
public class CategoryTreeHelper extends ModelerTreeHelper {
  public CategoryTreeHelper() {
  }

  public CategoryTreeHelper(Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms,
    XulDeck propsDeck,
    ModelerWorkspace workspace,
    AvailableField[] availableFields,
    Document document) {

    super(propertiesForms, propsDeck, workspace, availableFields, document);
  }

  @Override
  public void removeField() {
    AbstractModelNode parent = ((AbstractModelNode) getSelectedTreeItem()).getParent();
    if (parent == null) {
      workspace.getRelationalModel().remove(getSelectedTreeItem());
    } else {
      parent.remove(getSelectedTreeItem());
    }
    setTreeSelectionChanged(null);
  }

  @Override
  public void addField() {
    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
    AbstractMetaDataModelNode theNode = null;
    Object selectedTreeItem = getSelectedTreeItem();
    Object[] selectedItems = getSelectedFields();
    for (Object obj : selectedItems) {
      if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField) obj;
        // depending on the parent
        if (selectedTreeItem == null) {
          // null - cannot add fields at this level
        } else if (selectedTreeItem instanceof CategoryMetaData) {
          // category - add as a field
          theNode = workspace.createFieldForParentWithNode((CategoryMetaData) selectedTreeItem, availableField);
          CategoryMetaData theCategory = (CategoryMetaData) selectedTreeItem;
          theCategory.add((FieldMetaData) theNode);
        }
        if (theNode != null) {
          theNode.setParent((AbstractMetaDataModelNode) selectedTreeItem);
        }
      }
    }
    workspace.setModelIsChanging(prevChangeState);
  }
  @Override
  public void clearTreeModel(){
    workspace.setModelIsChanging(true);
    workspace.getRelationalModel().clear();
    workspace.setModelIsChanging(false, true);
  }
}
