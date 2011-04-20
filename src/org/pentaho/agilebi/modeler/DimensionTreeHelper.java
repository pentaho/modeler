package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    Object item = getSelectedTreeItem();
    if (item instanceof DimensionMetaDataCollection
        || item instanceof MeasuresCollection
        || item instanceof MainModelNode
        || item == null) {
      return;
    }
    workspace.setModelIsChanging(true);

    ((AbstractModelNode) getSelectedTreeItem()).getParent().remove(getSelectedTreeItem());
    setTreeSelectionChanged(null);
    workspace.setModelIsChanging(false, true);
    
  }

  @Override
  public void addField(Object[] selectedFields) {
    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
    AbstractMetaDataModelNode theNode = null;
    Object selectedTreeItem = getSelectedTreeItem();

    for (Object obj : selectedFields) {
      if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField)obj;

        // create the logicalColumn and new node fronting it
        ColumnBackedNode node = workspace.createColumnBackedNode(availableField, ModelerPerspective.ANALYSIS);

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

          theNode = workspace.createDimensionFromNode(node);
          DimensionMetaDataCollection theDimensions = (DimensionMetaDataCollection) selectedTreeItem;
          theDimensions.add((DimensionMetaData) theNode);
        } else if (selectedTreeItem instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
          theNode = workspace.createHierarchyForParentWithNode((DimensionMetaData) selectedTreeItem, node);
          DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
          theDimension.add((HierarchyMetaData) theNode);
        } else if (selectedTreeItem instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          theNode = workspace.createLevelForParentWithNode((HierarchyMetaData) selectedTreeItem, node);
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

  @Override
  public void clearTreeModel(){
    workspace.setModelIsChanging(true);

    workspace.getModel().getDimensions().clear();
    workspace.getModel().getMeasures().clear();
    workspace.setModelIsChanging(false, true);
  }
}
