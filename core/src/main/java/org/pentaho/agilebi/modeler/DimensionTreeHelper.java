/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.agilebi.modeler;

import java.util.Map;

import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Created: 3/22/11
 * 
 * @author rfellows
 */
public class DimensionTreeHelper extends ModelerTreeHelper {
  public DimensionTreeHelper() {
  }

  public DimensionTreeHelper(
      Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms, XulDeck propsDeck,
      ModelerWorkspace workspace, Document document ) {

    super( propertiesForms, propsDeck, workspace, document );
  }

  @Override
  public void removeField() {
    Object item = getSelectedTreeItem();
    if ( item instanceof DimensionMetaDataCollection || item instanceof MeasuresCollection
        || item instanceof MainModelNode || item == null ) {
      return;
    }
    workspace.setModelIsChanging( true );

    ( (AbstractModelNode) getSelectedTreeItem() ).getParent().remove( getSelectedTreeItem() );
    setTreeSelectionChanged( null );
    workspace.setModelIsChanging( false, true );

  }

  @Override
  protected boolean isModelChanging() {
    return workspace.isModelChanging();
  }

  @Override
  protected void setModelIsChanging( boolean changing ) {
    workspace.setModelIsChanging( changing );
  }

  @Override
  public void clearTreeModel() {
    workspace.setModelIsChanging( true );

    // remove all logical columns from existing logical tables
    // for (LogicalTable table : workspace.getLogicalModel(ModelerPerspective.ANALYSIS).getLogicalTables()) {
    // table.getLogicalColumns().clear();
    // }

    workspace.getModel().getDimensions().clear();
    workspace.getModel().getMeasures().clear();
    workspace.setModelIsChanging( false, true );
  }

  @Override
  public void addField( Object[] selectedFields ) throws ModelerException {
    boolean prevChangeState = workspace.isModelChanging();
    try {
      workspace.setModelIsChanging( true );
      super.addField( selectedFields );
    } catch ( ModelerException e ) {
      throw e;
    } finally {
      workspace.setModelIsChanging( prevChangeState );
    }
  }
}
