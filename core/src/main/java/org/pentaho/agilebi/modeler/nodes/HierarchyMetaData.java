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

package org.pentaho.agilebi.modeler.nodes;

import java.util.HashMap;
import java.util.List;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.propforms.HierarchyPropertiesForm;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class HierarchyMetaData extends AbstractMetaDataModelNode<LevelMetaData> {

  private static final long serialVersionUID = 7063031303948537101L;

  String name;
  private static final String CLASSNAME = "pentaho-smallhierarchybutton";

  public HierarchyMetaData() {
    super( CLASSNAME );
  }

  public HierarchyMetaData( String name ) {
    this();
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
    if ( !name.equals( this.name ) ) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange( "name", oldName, name ); //$NON-NLS-1$
      this.firePropertyChange( "displayName", oldName, name ); //$NON-NLS-1$
      validateNode();
    }
  }

  public DimensionMetaData getDimensionMetaData() {
    return (DimensionMetaData) getParent();
  }

  public boolean isTimeHierarchy() {
    DimensionMetaData dimensionMetaData = getDimensionMetaData();
    if ( dimensionMetaData == null ) {
      return false;
    }
    return dimensionMetaData.isTimeDimension();
  }

  public void dimensionTypeChanged() {
    validate();
  }

  public List<LevelMetaData> getLevels() {
    return this.children;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if ( name == null || "".equals( name ) ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString( "validation.hierarchy.MISSING_NAME" ) );
      valid = false;
    }
    if ( size() == 0 ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.hierarchy.REQUIRES_AT_LEAST_ONE_LEVEL" ) );
      valid = false;
    }

    HashMap<String, LevelMetaData> usedNames = new HashMap<String, LevelMetaData>();
    if ( children.size() == 0 ) {
      valid = false;
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.hierarchy.REQUIRES_AT_LEAST_ONE_LEVEL" ) );
    }
    for ( LevelMetaData level : children ) {
      // level's validate doesn't trigger events, only validateNode, so this is ok
      level.validate();
      valid &= level.isValid();
      validationMessages.addAll( level.getValidationMessages() );
      if ( usedNames.containsKey( level.getName() ) ) {
        valid = false;
        String dupeString =
            ModelerMessagesHolder.getMessages().getString( "validation.hierarchy.DUPLICATE_LEVEL_NAMES",
                level.getName() );
        validationMessages.add( dupeString );

        if ( level.isValid() ) {
          invalidateQuietly( level );
        }
        if ( !level.getValidationMessages().contains( dupeString ) ) {
          level.getValidationMessages().add( dupeString );
        }

        LevelMetaData l = usedNames.get( level.getName() );
        if ( l.isValid() ) {
          // avoid infinite loop here
          invalidateQuietly( l );
          if ( !l.getValidationMessages().contains( dupeString ) ) {
            l.getValidationMessages().add( dupeString );
          }
        }
      } else {
        usedNames.put( level.getName(), level );
      }
    }
  }

  /**
   * Hierarchy has a validation listener on its level children and invalidate triggers it;
   * if a level finds itself valid but hierarchy invalidates it we'd have an infinite loop
   * @param level
   */
  private void invalidateQuietly( LevelMetaData level ) {
    boolean prevLevelSuppress = level.suppressEvents;
    level.suppressEvents = true;
    level.invalidate();
    level.suppressEvents = prevLevelSuppress;
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
  public Class<HierarchyPropertiesForm> getPropertiesForm() {
    return HierarchyPropertiesForm.class;
  }

  @Override
  public void onAdd( LevelMetaData child ) {
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  @Override
  public void onRemove( LevelMetaData child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    return result;
  }

  public boolean equals( HierarchyMetaData obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    HierarchyMetaData other = obj;
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    return true;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    boolean isSupportedType = false;
    isSupportedType =
        ( obj instanceof AvailableField || obj instanceof LevelMetaData
            || obj instanceof MeasureMetaData || obj instanceof MemberPropertyMetaData );

    // get the columns of children, make sure the potential drop object is backed by the same table
    if ( isSupportedType ) {
      if ( size() == 0 ) {
        // no children to compare tables with, accept the drop
        return true;
      }
      LevelMetaData level = this.get( 0 );
      if ( level.getLogicalColumn() == null ) {
        return false; // make them fix the broken column first
      }
      String myTableId = level.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId();
      if ( obj instanceof AvailableField ) {
        AvailableField field = (AvailableField) obj;
        return myTableId.equals( field.getPhysicalColumn().getPhysicalTable().getId() );
      } else if ( obj instanceof ColumnBackedNode ) {
        // this will take care of both Levels & Measures
        ColumnBackedNode field = (ColumnBackedNode) obj;
        return myTableId.equals( field.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId() );
      }
    }
    return false;

  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      LevelMetaData level = null;
      if ( data instanceof AvailableField ) {
        ColumnBackedNode node =
            getWorkspace().createColumnBackedNode( (AvailableField) data, ModelerPerspective.ANALYSIS );
        level = getWorkspace().createLevelForParentWithNode( this, node );
      } else if ( data instanceof LevelMetaData ) {
        level = (LevelMetaData) data;
        level.setParent( this );
      } else if ( data instanceof ColumnBackedNode ) {
        ColumnBackedNode node = (ColumnBackedNode) data;
        level = getWorkspace().createLevelForParentWithNode( this, node );
        level.setName( node.getName() );
      } else {
        throw new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString( "invalid_drop" ) );
      }
      if ( size() > 0 ) {
        LogicalTable existingTable = get( 0 ).getLogicalColumn().getLogicalTable();
        if ( level.getLogicalColumn().getLogicalTable().getId() != existingTable.getId() ) {
          throw new IllegalStateException( ModelerMessagesHolder.getMessages().getString(
              "DROP.ERROR.TWO_TABLES_IN_HIERARCHY" ) );
        }
      }
      return level;
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }

}
