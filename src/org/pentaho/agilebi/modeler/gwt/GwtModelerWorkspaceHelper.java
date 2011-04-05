package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class GwtModelerWorkspaceHelper extends BaseModelerWorkspaceHelper {

  public static final String DEFAULT_LOCALE = "en_US";

  public GwtModelerWorkspaceHelper() {
    super(DEFAULT_LOCALE);
    BogoPojo bogo = new BogoPojo();
  }

  @Override
  protected MainModelNode getMainModelNode(ModelerWorkspace workspace) {
    MainModelNode mainModel = null;
    if (workspace.getModel() == null) {
      mainModel = new MainModelNode();
    } else {
      workspace.getModel().getMeasures().clear();
      workspace.getModel().getDimensions().clear();
      mainModel = workspace.getModel();
    }
    return mainModel;
  }

  @Override
  protected  RelationalModelNode getRelationalModelNode(ModelerWorkspace workspace) {
    RelationalModelNode relationalModel = null;
    if (workspace.getRelationalModel() == null) {
      relationalModel = new RelationalModelNode();
    } else {
      workspace.getRelationalModel().getCategories().clear();
      relationalModel = workspace.getRelationalModel();
    }
    return relationalModel;
  }

}
