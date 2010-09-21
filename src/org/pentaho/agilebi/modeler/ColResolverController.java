/*
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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;


/**
 * Serves as a Controller and Form Model for the Missing Column Resolver Dialog.
 * Condensed here for succinctness.
 *
 * @author nbaker
 */
public class ColResolverController extends AbstractXulEventHandler {

  private ModelerWorkspace workspace;
  private XulDialog dialog;
  private ColumnBackedNode node;
  private XulListbox availableCols;

  public ColResolverController() {

  }

  public void show( ModelerWorkspace workspace, ColumnBackedNode node ) {
    this.workspace = workspace;
    this.node = node;
    availableCols.setElements(workspace.getAvailableFields());
    dialog.show();
  }

  public void init() {
    this.dialog = (XulDialog) document.getElementById("resolveColumnsDialog");
    availableCols = (XulListbox) document.getElementById("resolveColumnsList");

  }

  @Bindable
  public void done() {
    int idx = this.availableCols.getSelectedIndex();
    if (idx > -1) {
      LogicalColumn lCol = workspace.getAvailableFields().get(idx).getLogicalColumn();
      node.setLogicalColumn(lCol);
      workspace.setDirty(true);
    }
    dialog.hide();
  }

  @Bindable
  public void cancel() {
    dialog.hide();
  }

  public String getName() {
    return "colResolver";
  }
}
