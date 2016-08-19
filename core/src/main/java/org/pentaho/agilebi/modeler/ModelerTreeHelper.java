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

import java.util.Collections;
import java.util.Map;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * Created: 3/21/11
 * 
 * @author rfellows
 */
public abstract class ModelerTreeHelper extends XulEventSourceAdapter {

  private transient Object selectedTreeItem;
  private Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms;
  private ModelerNodePropertiesForm selectedForm;
  private XulDeck propsDeck;
  protected ModelerWorkspace workspace;

  private Document document;

  public ModelerTreeHelper() {
  }

  public ModelerTreeHelper( Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms,
      XulDeck propsDeck, ModelerWorkspace workspace, Document document ) {
    this.propertiesForms = propertiesForms;
    this.propsDeck = propsDeck;
    this.workspace = workspace;
    this.document = document;
  }

  @Bindable
  public Object getSelectedTreeItem() {
    return selectedTreeItem;
  }

  @Bindable
  public void setSelectedTreeItem( Object selectedTreeItem ) {
    this.selectedTreeItem = selectedTreeItem;
  }

  @Bindable
  public void setTreeSelectionChanged( Object selection ) {
    setSelectedTreeItem( selection );
    if ( selection != null && selection instanceof AbstractMetaDataModelNode ) {
      AbstractMetaDataModelNode node = (AbstractMetaDataModelNode) selection;
      ModelerNodePropertiesForm form = propertiesForms.get( node.getPropertiesForm() );
      if ( form != null ) {
        if ( selectedForm != null && selectedForm != form ) {
          selectedForm.setObject( null );
        }
        form.activate( (AbstractMetaDataModelNode) selection );
        selectedForm = form;
        return;
      }
    }
    if ( propsDeck != null ) {
      propsDeck.setSelectedIndex( 0 );
    }
  }

  @Bindable
  public void moveFieldUp() {
    if ( selectedTreeItem == null ) {
      return;
    }
    ( (AbstractModelNode) selectedTreeItem ).getParent().moveChildUp( selectedTreeItem );

  }

  @Bindable
  public void moveFieldDown() {
    if ( selectedTreeItem == null ) {
      return;
    }
    ( (AbstractModelNode) selectedTreeItem ).getParent().moveChildDown( selectedTreeItem );

  }

  @Bindable
  public void removeField() {
    ( (AbstractModelNode) selectedTreeItem ).getParent().remove( selectedTreeItem );
    setTreeSelectionChanged( null );
  }

  @Bindable
  public void clearFields() {
    try {

      XulConfirmBox confirm = (XulConfirmBox) document.createElement( "confirmbox" ); //$NON-NLS-1$
      confirm.setTitle( ModelerMessagesHolder.getMessages().getString( "clear_model_title" ) ); //$NON-NLS-1$
      confirm.setMessage( ModelerMessagesHolder.getMessages().getString( "clear_model_msg" ) ); //$NON-NLS-1$
      confirm.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "yes" ) ); //$NON-NLS-1$
      confirm.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "no" ) ); //$NON-NLS-1$

      confirm.addDialogCallback( new XulDialogCallback() {
        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            clearTreeModel();
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
        }
      } );
      confirm.open();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public abstract void clearTreeModel();

  public void addField( Object[] selectedFields ) throws ModelerException {
    try {
      IDropTarget dropNode = (IDropTarget) getSelectedTreeItem();

      for ( Object selectedField : selectedFields ) {
        AbstractModelNode newNode = (AbstractModelNode) dropNode.onDrop( selectedField );
        ( (AbstractModelNode) dropNode ).add( newNode );
      }

    } catch ( IllegalStateException e ) {
      throw new ModelerException( e );
    }
  }

  public Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> getPropertiesForms() {
    return propertiesForms;
  }

  public void setPropertiesForms(
      Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms ) {
    this.propertiesForms = propertiesForms;
  }

  public ModelerNodePropertiesForm getSelectedForm() {
    return selectedForm;
  }

  public void setSelectedForm( ModelerNodePropertiesForm selectedForm ) {
    this.selectedForm = selectedForm;
  }

  public XulDeck getPropsDeck() {
    return propsDeck;
  }

  public void setPropsDeck( XulDeck propsDeck ) {
    this.propsDeck = propsDeck;
  }

  public ModelerWorkspace getWorkspace() {
    return workspace;
  }

  public void setWorkspace( ModelerWorkspace workspace ) {
    this.workspace = workspace;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument( Document document ) {
    this.document = document;
  }

  protected void removeLogicalColumnFromParentTable( ColumnBackedNode node ) {
    LogicalColumn lCol = node.getLogicalColumn();
    if ( lCol != null && lCol.getLogicalTable() != null ) {
      LogicalTable lTab = lCol.getLogicalTable();
      lTab.getLogicalColumns().remove( lCol );
    }
  }

  public void onModelDrop( DropEvent event ) throws ModelerException {
    try {
      IDropTarget dropNode = (IDropTarget) event.getDropParent();
      Object newData = null;
      for ( Object data : event.getDataTransfer().getData() ) {
        newData = dropNode.onDrop( data );
      }
      if ( newData == null ) {
        event.setAccepted( false );
      } else {
        event.getDataTransfer().setData( Collections.singletonList( newData ) );
      }
    } catch ( ModelerException e ) {
      throw e;
    }
  }

  protected abstract boolean isModelChanging();

  protected abstract void setModelIsChanging( boolean changing );

}
