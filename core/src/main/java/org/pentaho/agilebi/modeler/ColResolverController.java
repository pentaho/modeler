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

import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableItemCollection;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.IAvailableItem;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Serves as a Controller and Form Model for the Missing Column Resolver Dialog. Condensed here for succinctness.
 * 
 * @author nbaker
 */
public class ColResolverController extends AbstractXulEventHandler {

  private ModelerWorkspace workspace;
  private XulDialog dialog;
  private ColumnBackedNode node;
  private String columnType;

  private IAvailableItem[] selectedFields = new IAvailableItem[] {};
  private AvailableItemCollection items;
  BindingFactory bf;

  public ColResolverController() {
    items = new AvailableItemCollection();
  }

  public void show( ModelerWorkspace workspace, ColumnBackedNode node, String columnType,
      AvailableTable restrictedToTable ) {
    this.workspace = workspace;
    this.columnType = columnType;
    items.clear();
    if ( restrictedToTable != null ) {
      items.add( restrictedToTable );
    } else {
      items.addAll( workspace.getAvailableTables() );
    }
    this.node = node;
    dialog.setTitle( ModelerMessagesHolder.getMessages().getString(
        "ColResolverController." + columnType + "_column_selection_dialog" ) );
    dialog.show();
  }

  public void show( ModelerWorkspace workspace, ColumnBackedNode node, String columnType ) {
    show( workspace, node, columnType, null );
  }

  public void init() {
    bf.setDocument( document );

    this.dialog = (XulDialog) document.getElementById( "resolveColumnsDialog" );
    bf.setBindingType( Binding.Type.ONE_WAY );

    // fieldListBinding
    bf.createBinding( items, "children", "resolveColumnsTree", "elements" );

    // selectedFieldsBinding
    bf.createBinding( "resolveColumnsTree", "selectedItem", this, "selectedFieldsChanged" );

  }

  @Bindable
  public void done() {
    if ( selectedFields.length != 1 ) {
      return;
    }
    AvailableField field = (AvailableField) selectedFields[0];
    ColumnBackedNode cnode = workspace.createColumnBackedNode( field, workspace.getCurrentModelerPerspective() );
    LogicalColumn lCol = cnode.getLogicalColumn();
    if ( ColumnBackedNode.COLUMN_TYPE_SOURCE.equals( columnType ) ) {
      node.setLogicalColumn( lCol );
    } else if ( ColumnBackedNode.COLUMN_TYPE_ORDINAL.equals( columnType ) ) {
      node.setLogicalOrdinalColumn( lCol );
    } else if ( ColumnBackedNode.COLUMN_TYPE_CAPTION.equals( columnType ) ) {
      node.setLogicalCaptionColumn( lCol );
    }
    workspace.setDirty( true );
    dialog.hide();
  }

  @Bindable
  public void cancel() {
    dialog.hide();
  }

  public String getName() {
    return "colResolver";
  }

  @Bindable
  public void setSelectedFieldsChanged( Object selected ) {
    if ( selected != null && selected instanceof AvailableField ) {
      selectedFields = new IAvailableItem[] { (IAvailableItem) selected };
    }
  }

  public BindingFactory getBindingFactory() {
    return bf;
  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }
}
