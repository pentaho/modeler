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

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Created: 3/22/11
 * 
 * @author rfellows
 */
public class CategoryTreeHelper extends ModelerTreeHelper {
  public CategoryTreeHelper() {
  }

  public CategoryTreeHelper(
      Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms, XulDeck propsDeck,
      ModelerWorkspace workspace, Document document ) {

    super( propertiesForms, propsDeck, workspace, document );
  }

  @Override
  public void removeField() {
    if ( getSelectedTreeItem() instanceof CategoryMetaDataCollection
        || getSelectedTreeItem() instanceof RelationalModelNode || getSelectedTreeItem() == null ) {
      return;
    } else if ( getSelectedTreeItem() instanceof CategoryMetaData ) {
      // remove the logical columns associated with the fields in this category
      CategoryMetaData cat = (CategoryMetaData) getSelectedTreeItem();
      for ( FieldMetaData field : cat ) {
        removeLogicalColumnFromParentTable( field );
      }
    } else if ( getSelectedTreeItem() instanceof FieldMetaData ) {
      removeLogicalColumnFromParentTable( (FieldMetaData) getSelectedTreeItem() );
    }

    workspace.setRelationalModelIsChanging( true );
    ( (AbstractModelNode) getSelectedTreeItem() ).getParent().remove( getSelectedTreeItem() );
    setTreeSelectionChanged( null );
    workspace.setRelationalModelIsChanging( false, true );
  }

  @Override
  public void addField( Object[] selectedFields ) throws ModelerException {
    boolean prevChangeState = workspace.isModelChanging();
    try {
      workspace.setRelationalModelIsChanging( true );
      super.addField( selectedFields );
    } catch ( ModelerException e ) {
      throw e;
    } finally {
      workspace.setRelationalModelIsChanging( prevChangeState );
    }
  }

  private AbstractMetaDataModelNode addAvailableField( AvailableField availableField, Object targetParent ) {
    AbstractMetaDataModelNode theNode = null;
    // depending on the parent
    if ( targetParent != null && targetParent instanceof CategoryMetaData ) {
      // category - add as a field
      theNode = workspace.createFieldForParentWithNode( (CategoryMetaData) targetParent, availableField );
      CategoryMetaData theCategory = (CategoryMetaData) targetParent;
      theCategory.add( (FieldMetaData) theNode );
    }
    if ( theNode != null ) {
      theNode.setParent( (AbstractMetaDataModelNode) targetParent );
    }
    return theNode;
  }

  @Override
  public void clearTreeModel() {
    workspace.setRelationalModelIsChanging( true );

    // remove all logical columns from existing logical tables
    for ( LogicalTable table : workspace.getLogicalModel( ModelerPerspective.REPORTING ).getLogicalTables() ) {
      table.getLogicalColumns().clear();
    }

    workspace.getRelationalModel().getCategories().clear();
    workspace.setRelationalModelIsChanging( false, true );
  }

  @Override
  protected boolean isModelChanging() {
    return workspace.isModelChanging();
  }

  @Override
  protected void setModelIsChanging( boolean changing ) {
    workspace.setRelationalModelIsChanging( changing );
  }
}
