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
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.propforms.HierarchyPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;
import java.util.HashMap;

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
      validationMessages.add(ModelerMessagesHolder.getMessages().getString("validation.hierarchy.MISSING_NAME"));
      valid = false;
    }
    if (size() == 0) {
      validationMessages.add(ModelerMessagesHolder.getMessages().getString("validation.hierarchy.REQUIRES_AT_LEAST_ONE_LEVEL"));
      valid = false;
    }

    HashMap<String, LevelMetaData> usedNames = new HashMap<String, LevelMetaData>();
    if (children.size() == 0) {
      valid = false;
      validationMessages.add(ModelerMessagesHolder.getMessages().getString("validation.hierarchy.REQUIRES_AT_LEAST_ONE_LEVEL"));
    }
    for (LevelMetaData level : children) {
      valid &= level.isValid();
      validationMessages.addAll(level.getValidationMessages());
      if (usedNames.containsKey(level.getName())) {
        valid = false;
        String dupeString = ModelerMessagesHolder.getMessages().getString("validation.hierarchy.DUPLICATE_LEVEL_NAMES", level.getName());
        validationMessages.add(dupeString);

        level.invalidate();
        if (!level.getValidationMessages().contains(dupeString)) {
          level.getValidationMessages().add(dupeString);
        }

        LevelMetaData l = usedNames.get(level.getName());
        if (l.isValid()) {
          l.invalidate();
          if (!l.getValidationMessages().contains(dupeString)) {
            l.getValidationMessages().add(dupeString);
          }
        }
      } else {
        usedNames.put(level.getName(), level);
      }
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


  @Override
  public boolean acceptsDrop(Object obj) {
    return obj instanceof AvailableField || obj instanceof LevelMetaData || obj instanceof MeasureMetaData;
  }

  @Override
  public Object onDrop(Object data) throws ModelerException {
    try{
      LevelMetaData level = null;
      if(data instanceof AvailableField){
        ColumnBackedNode node = getWorkspace().createColumnBackedNode((AvailableField) data, ModelerPerspective.ANALYSIS);
        level = getWorkspace().createLevelForParentWithNode(this, node);
      } else if(data instanceof MeasureMetaData){
        MeasureMetaData measure = (MeasureMetaData) data;
        level = getWorkspace().createLevelForParentWithNode(this, measure);
        level.setName(measure.getName());
      } else if(data instanceof LevelMetaData){
        level = (LevelMetaData) data;
        level.setParent(this);
      } else {
        throw new IllegalArgumentException(ModelerMessagesHolder.getMessages().getString("invalid_drop"));
      }
      if(size() > 0){
        LogicalTable existingTable = get(0).getLogicalColumn().getLogicalTable();
        if(level.getLogicalColumn().getLogicalTable().getId() != existingTable.getId()){
          throw new IllegalStateException(ModelerMessagesHolder.getMessages().getString("DROP.ERROR.TWO_TABLES_IN_HIERARCHY"));
        }
      }
      return level;
    } catch(Exception e){
      throw new ModelerException(e);
    }
  }

}