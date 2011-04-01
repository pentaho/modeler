package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.util.ArrayList;
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

  public void onDimensionTreeDrop( DropEvent event ) {


    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
    List<Object> data = event.getDataTransfer().getData();
    List<Object> newdata = new ArrayList<Object>();
    for (Object obj : data) {
      if (obj instanceof AvailableField) {
        // olap fields are stored in a cloned LogicalTable with cloned columns
        // this allows for individual editing of the 2 models (reporting & olap)
        AvailableField availableField = convertToOlapField((AvailableField) obj, workspace.getDomain().getLogicalModels().get(0));
        // depending on the parent
        if (event.getDropParent() == null) {
          // null - cannot add fields at this level
        } else if (event.getDropParent() instanceof MeasuresCollection) {
          // measure collection - add as a measure
          newdata.add(workspace.createMeasureForNode(availableField));
        } else if (event.getDropParent() instanceof DimensionMetaDataCollection) {
          // dimension collection - add as a dimension
          newdata.add(workspace.createDimensionFromNode(availableField));
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
          newdata.add(
              workspace.createHierarchyForParentWithNode((DimensionMetaData) event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          newdata.add(
              workspace.createLevelForParentWithNode((HierarchyMetaData) event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof LevelMetaData) {
          // level - cannot drop into a level
          event.setAccepted(false);
          workspace.setModelIsChanging(prevChangeState, false);
          return;
        }
      } else if (obj instanceof LevelMetaData) {
        LevelMetaData level = (LevelMetaData) obj;
        if (event.getDropParent() instanceof HierarchyMetaData) {
          // rebind to workspace, including logical column and actual parent
          level.setParent((HierarchyMetaData) event.getDropParent());
          newdata.add(level);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // add as a new hierarchy
          HierarchyMetaData hier = workspace.createHierarchyForParentWithNode((DimensionMetaData) event.getDropParent(),
              level);
          hier.setName(level.getName());
          hier.get(0).setName(level.getName());
          newdata.add(hier);
        } else if (event.getDropParent() == null) {
          DimensionMetaData dim = workspace.createDimensionWithName(level.getColumnName());
          dim.setName(level.getName());
          dim.get(0).setName(level.getName());
          dim.get(0).get(0).setName(level.getName());
          newdata.add(dim);
        }
      } else if (obj instanceof HierarchyMetaData) {
        HierarchyMetaData hierarchy = (HierarchyMetaData) obj;
        if (event.getDropParent() == null) {
          DimensionMetaData dim = new DimensionMetaData(hierarchy.getName());
          dim.add(hierarchy);
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(dim);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          DimensionMetaData dim = (DimensionMetaData) event.getDropParent();
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(hierarchy);
        }
      } else if (obj instanceof DimensionMetaData) {

        if (event.getDropParent() instanceof DimensionMetaDataCollection) {
          newdata.add((DimensionMetaData) obj);
        } else if (event.getDropParent() == null) {
          newdata.add((DimensionMetaData) obj);
          // TODO: this will also need to resolve level LogicalColumns
        }
      } else if (obj instanceof MeasureMetaData) {
        if (event.getDropParent() instanceof MeasuresCollection) {
          MeasureMetaData measure = (MeasureMetaData) obj;
          LogicalColumn col = workspace.findLogicalColumn(obj.toString());
          measure.setLogicalColumn(col);
          newdata.add(measure);
        }
      }

    }
    if (newdata.size() == 0) {
      event.setAccepted(false);
    } else {
      event.getDataTransfer().setData(newdata);
    }
    workspace.setModelIsChanging(prevChangeState, false);
  }

  public void checkDropLocation(DropEvent event){

    List<Object> data = event.getDataTransfer().getData();
    for (Object obj : data) {
      if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField) obj;
        // depending on the parent
        if (event.getDropParent() == null) {
          event.setAccepted(false);
          return;
        } else if (event.getDropParent() instanceof MeasuresCollection) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof DimensionMetaDataCollection) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof HierarchyMetaData) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof LevelMetaData) {
          event.setAccepted(false);
          return;
        }
      } else if (obj instanceof LevelMetaData) {
        LevelMetaData level = (LevelMetaData) obj;
        if (event.getDropParent() instanceof HierarchyMetaData) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          event.setAccepted(true);
          return;
        }
      } else if (obj instanceof HierarchyMetaData) {
        HierarchyMetaData hierarchy = (HierarchyMetaData) obj;
        if (event.getDropParent() == null) {
          event.setAccepted(true);
          return;
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          event.setAccepted(true);
          return;
        }
      } else if (obj instanceof DimensionMetaData) {
        if (event.getDropParent() instanceof DimensionMetaDataCollection) {
          event.setAccepted(true);
          return;
        }
      } else if (obj instanceof MeasureMetaData) {
        if (event.getDropParent() instanceof MeasuresCollection) {
          event.setAccepted(true);
          return;
        }
      }

    }
    event.setAccepted(false);
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
