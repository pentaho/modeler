package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dnd.DropEvent;
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
    Document document) {

    super(propertiesForms, propsDeck, workspace, document);
  }

  @Override
  public void removeField() {
    if (getSelectedTreeItem() instanceof CategoryMetaDataCollection
        || getSelectedTreeItem() instanceof RelationalModelNode
        || getSelectedTreeItem() == null) {
      return;
    } else if (getSelectedTreeItem() instanceof CategoryMetaData) {
      // remove the logical columns associated with the fields in this category
      CategoryMetaData cat = (CategoryMetaData)getSelectedTreeItem();
      for(FieldMetaData field : cat) {
        removeLogicalColumnFromParentTable(field);
      }
    } else if (getSelectedTreeItem() instanceof FieldMetaData) {
      removeLogicalColumnFromParentTable((FieldMetaData)getSelectedTreeItem());
    }
    ((AbstractModelNode) getSelectedTreeItem()).getParent().remove(getSelectedTreeItem());
    setTreeSelectionChanged(null);
  }

  @Override
  public void addField(Object[] selectedFields) {
    boolean prevChangeState = workspace.isModelChanging();
    workspace.setRelationalModelIsChanging(true);

    for (Object obj : selectedFields) {
      if (obj instanceof AvailableTable) {
        AvailableTable table = (AvailableTable)obj;
        for (AvailableField field : table.getAvailableFields()) {
          addAvailableField(field);
        }
      } else if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField)obj;
        addAvailableField(availableField);
      }
    }
    workspace.setRelationalModelIsChanging(prevChangeState);
  }

  private void addAvailableField(AvailableField availableField) {
    AbstractMetaDataModelNode theNode = null;
    Object selectedTreeItem = getSelectedTreeItem();
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

  @Override
  public void clearTreeModel(){
    workspace.setRelationalModelIsChanging(true);

    // remove all logical columns from existing logical tables
    for (LogicalTable table : workspace.getDomain().getLogicalModels().get(0).getLogicalTables()) {
      if (!table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        table.getLogicalColumns().clear();
      }
    }

    workspace.getRelationalModel().getCategories().clear();
    workspace.setRelationalModelIsChanging(false, true);
  }

  public void onModelDrop(DropEvent event) {
    // TODO: add drop logic here
  }
}
