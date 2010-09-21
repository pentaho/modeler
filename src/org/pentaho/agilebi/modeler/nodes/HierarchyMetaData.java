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

import org.pentaho.agilebi.modeler.propforms.HierarchyPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class HierarchyMetaData extends AbstractMetaDataModelNode<LevelMetaData> implements Serializable {

  private static final long serialVersionUID = 7063031303948537101L;

  String name;

  public HierarchyMetaData(){
    
  }

  public HierarchyMetaData( String name ) {
    this.name = name;
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


  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if (name == null || "".equals(name)) {
      validationMessages.add("Name is empty");
      valid = false;
    }
    if (size() == 0) {
      validationMessages.add("Hierarchy must have at least one level");
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();

    if (children.size() == 0) {
      valid = false;
      //TODO: GWT i18n
      validationMessages.add(
          "Need at least one level");//BaseMessages.getString(ModelerWorkspace.class, "missing_level_from_heirarchy"));
    }
    for (LevelMetaData level : children) {
      valid &= level.isValid();
      validationMessages.addAll(level.getValidationMessages());
      if (usedNames.contains(level.getName())) {
        valid = false;
        //TODO: GWT i18n
        validationMessages.add(
            "Duplicate Level names");//BaseMessages.getString(ModelerWorkspace.class, "duplicate_level_names"));
      }
      usedNames.add(level.getName());
    }
  }

  @Bindable
  public String toString() {
    return "Hierarchy Name: " + name;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_hierarchy_icon.png"; //$NON-NLS-1$
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
    return HierarchyPropertiesForm.class;
  }

  @Override
  public void onAdd( LevelMetaData child ) {
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  @Override
  public void onRemove( LevelMetaData child ) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  public boolean equals( HierarchyMetaData obj ) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HierarchyMetaData other = (HierarchyMetaData) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }


}