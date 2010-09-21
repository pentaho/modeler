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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;

@SuppressWarnings("unchecked")
public class LevelMetaData extends AbstractMetaDataModelNode implements Serializable, ColumnBackedNode {

  private static final long serialVersionUID = -8026104295937064671L;
  String name;
  String columnName;
  HierarchyMetaData parent;
  transient LogicalColumn logicalColumn;
  Boolean uniqueMembers = true;

  public LevelMetaData(){
    
  }

  public LevelMetaData( HierarchyMetaData parent, String name ) {
    this.parent = parent;
    this.name = name;
    this.columnName = name;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public String getDisplayName() {
    return getName();
  }

  @Bindable
  public void setName( String name ) {
    if (!name.equals(this.name)) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange("name", oldName, name); //$NON-NLS-1$
      this.firePropertyChange("displayName", oldName, name); //$NON-NLS-1$
      validateNode();
    }
  }

  @Bindable
  public String getColumnName() {
    return columnName;
  }

  @Bindable
  public void setColumnName( String columnName ) {
    this.columnName = columnName;
  }


  public HierarchyMetaData getParent() {
    return parent;
  }

  public void setParent( HierarchyMetaData md ) {
    this.parent = md;
  }

  @Bindable
  public String toString() {
    return "Level Name: " + name + "\nColumn Name: " + columnName;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_level_icon.png";
  }

  @Bindable
  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn( LogicalColumn col ) {
    LogicalColumn prevVal = this.logicalColumn;
    this.logicalColumn = col;
    validateNode();
    firePropertyChange("logicalColumn", prevVal, col);
  }

  public void setUniqueMembers( Boolean uniqueMembers ) {
    this.uniqueMembers = uniqueMembers;
  }

  public Boolean isUniqueMembers() {
    return uniqueMembers;
  }

  @Override
  public void validate() {
    String prevMessages = getValidationMessagesString();
    valid = true;
    validationMessages.clear();
    // check name
    if (name == null || "".equals(name)) {
      validationMessages.add("Name is empty");
      valid = false;
    }
    if (logicalColumn == null) {
      validationMessages.add("Column is missing");
      valid = false;
    }
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return false;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return LevelsPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((columnName == null) ? 0 : columnName.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result
        + ((uniqueMembers == null) ? 0 : uniqueMembers.hashCode());
    return result;
  }

  public boolean equals( LevelMetaData obj ) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LevelMetaData other = (LevelMetaData) obj;
    if (columnName == null) {
      if (other.columnName != null) {
        return false;
      }
    } else if (!columnName.equals(other.columnName)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (parent == null) {
      if (other.parent != null) {
        return false;
      }
    } else if (!parent.equals(other.parent)) {
      return false;
    }
    return true;
  }


}