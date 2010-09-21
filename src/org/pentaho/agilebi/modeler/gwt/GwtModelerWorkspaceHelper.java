package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class GwtModelerWorkspaceHelper extends BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {

  public GwtModelerWorkspaceHelper(){
    BogoPojo bogo = new BogoPojo();
  }

  public void autoModelFlatInBackground( ModelerWorkspace workspace ) throws ModelerException {
    autoModelFlat(workspace);
  }

  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    MainModelNode mainModel = new MainModelNode();
    mainModel.setName(workspace.getModelName());
    workspace.setModel(mainModel);
    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);

    List<AvailableField> fields = workspace.getAvailableFields();
    for( AvailableField field : fields ) {
      DataType dataType = field.getLogicalColumn().getDataType();
      if( dataType == DataType.NUMERIC) {
        // create a measure
        MeasureMetaData measure = workspace.createMeasureForNode(field);
        workspace.getModel().getMeasures().add(measure);
      }
      // create a dimension
      workspace.addDimensionFromNode(field);
    }

    workspace.setModelIsChanging(prevChangeState);
    workspace.setSelectedNode(workspace.getModel());

  }

  public void sortFields( List<AvailableField> availableFields) {
    Collections.sort(availableFields, new Comparator<AvailableField>() {
      public int compare( AvailableField o1, AvailableField o2 ) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        }
        String name1 = ((AvailableField) o1).getDisplayName();
        String name2 = ((AvailableField) o2).getDisplayName();
        if (name1 == null && name2 == null) {
          return 0;
        } else if (name1 == null) {
          return -1;
        } else if (name2 == null) {
          return 1;
        }
        return name1.compareToIgnoreCase(name2);
      }
    });
  }
}
