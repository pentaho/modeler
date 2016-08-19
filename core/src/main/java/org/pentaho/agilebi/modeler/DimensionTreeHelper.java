/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
