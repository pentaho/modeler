package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.util.ArrayList;
import java.util.List;
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

    Object targetParent = getSelectedTreeItem();
    for (Object obj : selectedFields) {
      if (obj instanceof AvailableTable) {
        AvailableTable table = (AvailableTable)obj;
        if (targetParent instanceof CategoryMetaDataCollection) {
          // create a new category for this table
          CategoryMetaDataCollection catCollection = ((CategoryMetaDataCollection)targetParent);
          CategoryMetaData cat = new CategoryMetaData(table.getName());
          cat.setParent(catCollection);
          catCollection.add(cat);
          
          targetParent = cat;
        }
        for (AvailableField field : table.getAvailableFields()) {
          addAvailableField(field, targetParent);
        }
      } else if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField)obj;
        addAvailableField(availableField, targetParent);
      }
    }
    workspace.setRelationalModelIsChanging(prevChangeState);
  }

  private AbstractMetaDataModelNode addAvailableField(AvailableField availableField, Object targetParent) {
    AbstractMetaDataModelNode theNode = null;
    // depending on the parent
    if (targetParent == null) {
      // null - cannot add fields at this level
    } else if (targetParent instanceof CategoryMetaData) {
      // category - add as a field
      theNode = workspace.createFieldForParentWithNode((CategoryMetaData) targetParent, availableField);
      CategoryMetaData theCategory = (CategoryMetaData) targetParent;
      theCategory.add((FieldMetaData) theNode);
    } else if (targetParent instanceof FieldMetaData) {
      // cant add field to a field
    }
    if (theNode != null) {
      theNode.setParent((AbstractMetaDataModelNode) targetParent);
    }
    return theNode;
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

  @Bindable
  public void onModelDrop(DropEvent event) {
    boolean prevChangeState = workspace.isModelChanging();
    workspace.setRelationalModelIsChanging(true);
    List<Object> data = event.getDataTransfer().getData();
    List<Object> newdata = new ArrayList<Object>();

    for (Object obj : data) {
      if (obj instanceof AvailableTable) {
        AvailableTable table = (AvailableTable)obj;
        if (event.getDropParent() instanceof CategoryMetaData) {
          for (AvailableField field : table.getAvailableFields()) {
            newdata.add(workspace.createFieldForParentWithNode((CategoryMetaData) event.getDropParent(), field));
          }
        } else if (event.getDropParent() instanceof CategoryMetaDataCollection ) {
          // create a new category for this table?
          CategoryMetaData cat = new CategoryMetaData(table.getName());
          for (AvailableField field : table.getAvailableFields()) {
            cat.add(workspace.createFieldForParentWithNode(cat, field));
          }
          newdata.add(cat);
        } else {
          event.setAccepted(false);
          workspace.setModelIsChanging(prevChangeState, false);
          return;
        }
      } else if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField)obj;
        if (event.getDropParent() instanceof CategoryMetaData) {
          newdata.add(workspace.createFieldForParentWithNode((CategoryMetaData) event.getDropParent(), availableField));
        } else {
          event.setAccepted(false);
          workspace.setModelIsChanging(prevChangeState, false);
          return;
        }
      } else if (obj instanceof FieldMetaData) {
        FieldMetaData field = (FieldMetaData)obj;
        if (event.getDropParent() instanceof CategoryMetaData) {
          field.setParent((CategoryMetaData)event.getDropParent());
          newdata.add(field);
        }
      }
    }
    if (newdata.size() == 0) {
      event.setAccepted(false);
    } else {
      event.getDataTransfer().setData(newdata);
    }

    workspace.setRelationalModelIsChanging(prevChangeState);
  }

  @Bindable
  public void checkDropLocation(DropEvent event){
    List<Object> data = event.getDataTransfer().getData();
    Object dropTarget = event.getDropParent();
    if (dropTarget == null) {
      event.setAccepted(false);
      return;
    }

    for (Object obj : data) {

      if(obj instanceof AbstractMetaDataModelNode && event.getDropParent() != null){
        event.setAccepted(((AbstractMetaDataModelNode) event.getDropParent()).acceptsDrop(obj));
      }
      if (obj instanceof AvailableTable) {
        // dropping an AvailableTable, see if the drop location is valid
        event.setAccepted(dropTarget instanceof CategoryMetaData || dropTarget instanceof CategoryMetaDataCollection);
        return;
      } else if (obj instanceof AvailableField) {
        // dropping an AvailableField
        event.setAccepted(dropTarget instanceof CategoryMetaData);
        return;
      } else if (obj instanceof FieldMetaData) {
        // moving a field object around
        event.setAccepted(dropTarget instanceof CategoryMetaData);
        return;
      } else if (obj instanceof CategoryMetaData) {
        event.setAccepted(dropTarget instanceof CategoryMetaDataCollection);
        return;
      }
      event.setAccepted(false);
      return;
    }
  }

}
