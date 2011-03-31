package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.util.Map;

/**
 * Created: 3/22/11
 *
 * @author rfellows
 */
public class DimensionTreeHelper extends ModelerTreeHelper {
  public DimensionTreeHelper() {
  }

  public DimensionTreeHelper(Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms,
      XulDeck propsDeck,
      ModelerWorkspace workspace,
      Document document) {

    super(propertiesForms, propsDeck, workspace, document);
  }

  @Override
  public void removeField() {
    if (getSelectedTreeItem() instanceof DimensionMetaDataCollection
        || getSelectedTreeItem() instanceof MeasuresCollection
        || getSelectedTreeItem() instanceof MainModelNode
        || getSelectedTreeItem() == null) {
      return;
    }
    ((AbstractModelNode) getSelectedTreeItem()).getParent().remove(getSelectedTreeItem());
    setTreeSelectionChanged(null);
  }

  @Override
  public void addField(Object[] selectedFields) {
    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
    AbstractMetaDataModelNode theNode = null;
    Object selectedTreeItem = getSelectedTreeItem();

    for (Object obj : selectedFields) {
      if (obj instanceof AvailableField) {
        // olap fields are stored in a cloned LogicalTable with cloned columns
        // this allows for individual editing of the 2 models (reporting & olap)
        AvailableField availableField = convertToOlapField((AvailableField)obj, workspace.getDomain().getLogicalModels().get(0));

        // depending on the parent
        if (selectedTreeItem == null) {
          // null - cannot add fields at this level
        } else if (selectedTreeItem instanceof MeasuresCollection) {
          // measure collection - add as a measure
          MeasuresCollection theMesaures = (MeasuresCollection) selectedTreeItem;
          theNode = workspace.createMeasureForNode(availableField);
          theMesaures.add((MeasureMetaData) theNode);
        } else if (selectedTreeItem instanceof DimensionMetaDataCollection) {
          // dimension collection - add as a dimension
          theNode = workspace.createDimensionFromNode(availableField);
          DimensionMetaDataCollection theDimensions = (DimensionMetaDataCollection) selectedTreeItem;
          theDimensions.add((DimensionMetaData) theNode);
        } else if (selectedTreeItem instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
          theNode = workspace.createHierarchyForParentWithNode((DimensionMetaData) selectedTreeItem, availableField);
          DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
          theDimension.add((HierarchyMetaData) theNode);
        } else if (selectedTreeItem instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          theNode = workspace.createLevelForParentWithNode((HierarchyMetaData) selectedTreeItem, availableField);
          HierarchyMetaData theHierarchy = (HierarchyMetaData) selectedTreeItem;
          theHierarchy.add((LevelMetaData) theNode);
        }
        if (theNode != null) {
          theNode.setParent((AbstractMetaDataModelNode) selectedTreeItem);
        }
      }
    }
    workspace.setModelIsChanging(prevChangeState);
  }

  public static AvailableField convertToOlapField(AvailableField availableField, LogicalModel model) {
    AvailableField newField = new AvailableField();
    newField.setAggTypeDesc(availableField.getAggTypeDesc());
    newField.setName(availableField.getName());
    newField.setDisplayName(availableField.getDisplayName());
    LogicalColumn lc = model.findLogicalColumn(BaseModelerWorkspaceHelper.getCorrespondingOlapColumnId(availableField.getLogicalColumn()));
    newField.setLogicalColumn(lc);
    return newField;
  }

  @Override
  public void clearTreeModel(){
    workspace.setModelIsChanging(true);
    workspace.getModel().getDimensions().clear();
    workspace.getModel().getMeasures().clear();
    workspace.setModelIsChanging(false, true);
  }
}
