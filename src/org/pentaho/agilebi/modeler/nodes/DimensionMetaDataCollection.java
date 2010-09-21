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

import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DimensionMetaDataCollection extends AbstractMetaDataModelNode<DimensionMetaData> implements Serializable {

  private static final long serialVersionUID = -6327799582519270107L;

  private String name = "Dimensions";

  public DimensionMetaDataCollection() {
    this.valid = false;
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
    this.name = name;
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  private transient PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      fireCollectionChanged();
    }
  };

  //TODO: investigate using "this" form of notification in super-class
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this); //$NON-NLS-1$
  }

  @Override
  public void onAdd( DimensionMetaData child ) {
    child.setParent(this);
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  @Override
  public void onRemove( DimensionMetaData child ) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    boolean prevValid = valid;
    valid = true;
    validationMessages.clear();
    if (size() == 0) {
      validationMessages.add("Model requires at least one Dimension");
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();

    for (DimensionMetaData dim : children) {
      valid &= dim.isValid();
      validationMessages.addAll(dim.getValidationMessages());
      if (usedNames.contains(dim.getName())) {
        valid = false;
        //TODO: GWT i18n
        validationMessages.add(
            "Duplicate Dimension Names");//BaseMessages.getString(ModelerWorkspace.class, "duplicate_dimension_names"));
      }
      usedNames.add(dim.getName());
    }
    if (this.suppressEvents == false) {
      this.firePropertyChange("valid", prevValid, valid);
    }
  }

  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  public boolean equals( DimensionMetaDataCollection obj ) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DimensionMetaDataCollection other = (DimensionMetaDataCollection) obj;
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