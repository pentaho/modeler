package org.pentaho.agilebi.modeler.gwt;

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
public class GwtModelerWorkspaceHelper extends BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {

  public static final String DEFAULT_LOCALE = "en_US";

  public GwtModelerWorkspaceHelper(){
    super(DEFAULT_LOCALE);
    BogoPojo bogo = new BogoPojo();
  }

  public void autoModelFlatInBackground( ModelerWorkspace workspace ) throws ModelerException {
    autoModelFlat(workspace);
  }

  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    MainModelNode mainModel = null;
    if (workspace.getModel() == null) {
      mainModel = new MainModelNode();
    } else {
      workspace.getModel().getMeasures().clear();
      workspace.getModel().getDimensions().clear();
      mainModel = workspace.getModel();
    }

    mainModel.setName(workspace.getModelName());

    workspace.setModel(mainModel);

    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);

    List<AvailableField> fields = workspace.getAvailableOlapFields();
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

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlat(ModelerWorkspace workspace) throws ModelerException {
    RelationalModelNode relationalModel = null;
    if (workspace.getRelationalModel() == null) {
      relationalModel = new RelationalModelNode();
    } else {
      workspace.getRelationalModel().getCategories().clear();
      relationalModel = workspace.getRelationalModel();
    }

    relationalModel.setName(workspace.getRelationalModelName());

    workspace.setRelationalModel(relationalModel);

    CategoryMetaData category = new CategoryMetaData("Category");

    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);

    List<AvailableField> fields = workspace.getAvailableFields();
    for( AvailableField field : fields ) {
      category.add(workspace.createFieldForParentWithNode(category, field));
    }
    relationalModel.getCategories().add(category);

    workspace.setModelIsChanging(prevChangeState);
    workspace.setSelectedNode(workspace.getRelationalModel());
  }
  
  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlatInBackground(ModelerWorkspace workspace) throws ModelerException {
    autoModelRelationalFlat(workspace);
  }
}
