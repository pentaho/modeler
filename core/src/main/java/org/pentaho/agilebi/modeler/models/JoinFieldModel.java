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


package org.pentaho.agilebi.modeler.models;

import java.io.Serializable;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class JoinFieldModel extends XulEventSourceAdapter implements Serializable {
  private static final long serialVersionUID = -6133818241404582344L;
  private String name;
  private JoinTableModel parentTable;

  public JoinFieldModel() {

  }

  @Bindable
  public String getName() {
    return this.name;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public JoinTableModel getParentTable() {
    return this.parentTable;
  }

  @Bindable
  public void setParentTable( JoinTableModel parentTable ) {
    this.parentTable = parentTable;
  }
}
