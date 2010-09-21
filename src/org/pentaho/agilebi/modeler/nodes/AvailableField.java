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
package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;

public class AvailableField implements Serializable, ColumnBackedNode {

  private static final long serialVersionUID = -4430951279551589688L;
  
  private transient LogicalColumn logicalColumn;
  private String name, displayName, aggTypeDesc;

  public AvailableField(){
    
  }
  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn(LogicalColumn logicalColumn) {
    this.logicalColumn = logicalColumn;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName(String name) {
    this.name = name;
  }

  @Bindable
  public String getDisplayName() {
    return displayName;
  }

  @Bindable
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getAggTypeDesc() {
    return aggTypeDesc;
  }

  public void setAggTypeDesc(String aggTypeDesc) {
    this.aggTypeDesc = aggTypeDesc;
  }

  public String toString() {
    return name;
  }
  
}
