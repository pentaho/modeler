package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class ModelerWorkspaceHelper extends BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {


  public ModelerWorkspaceHelper(String locale) {
    super(locale);
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {

    MainModelNode mainModel = new MainModelNode();
    RelationalModelNode relationalModelNode = new RelationalModelNode();

    mainModel.setName(workspace.getModelName());
    relationalModelNode.setName(workspace.getRelationalModelName());

    workspace.setModel(mainModel);
    workspace.setRelationalModel(relationalModelNode);
    workspace.setModelIsChanging(true);

    CategoryMetaData category = new CategoryMetaData("Category");

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

      category.add(workspace.createFieldForParentWithNode(category, field));
    }
    relationalModelNode.getCategories().add(category);
    
    workspace.setModelIsChanging(false);

  }


  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {
//    throw new UnsupportedOperationException("Not available outside of Spoon");
    autoModelFlat(workspace);
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


