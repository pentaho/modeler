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

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.propforms.DimensionPropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class DimensionMetaData extends AbstractMetaDataModelNode<HierarchyMetaData> {

  private static final long serialVersionUID = -891901735974255178L;

  String name;
  String dimensionType = "StandardDimension";

  private static final String CLASSNAME = "pentaho-smalldimensionbutton";

  public DimensionMetaData() {
    super( CLASSNAME );
  }

  public DimensionMetaData( String name ) {
    this( name, "StandardDimension" );
  }

  public DimensionMetaData( String name, String dimensionType ) {
    this();
    this.name = name;
    this.dimensionType = dimensionType;
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

  @Bindable
  public String getDimensionType() {
    return dimensionType;
  }

  @Bindable
  public void setDimensionType( String type ) {
    String oldType = dimensionType;
    if ( oldType.equals( type ) ) {
      return;
    }
    dimensionType = type;
    firePropertyChange( "dimensionType", oldType, type );
  }

  @Bindable
  public boolean isTimeDimension() {
    return isTimeDimension( dimensionType );
  }

  static boolean isTimeDimension( String dimensionType ) {
    return "TimeDimension".equals( dimensionType );
  }

  @Bindable
  public void setTimeDimension( boolean timeDimension ) {
    boolean oldTimeDimension = isTimeDimension();
    if ( timeDimension == oldTimeDimension ) {
      return;
    }
    setDimensionType( timeDimension ? "TimeDimension" : "StandardDimension" );
    firePropertyChange( "timeDimension", oldTimeDimension, timeDimension );
    validateNode();
  }

  @Bindable
  public String toString() {
    return "Dimension Name: " + name;
  }

  @Bindable
  public String getValidImage() {
    return "images/sm_dim_icon.png"; //$NON-NLS-1$
  }

  @Bindable
  public void validate() {
    validationMessages.clear();
    valid = true;
    if ( name == null || "".equals( name ) ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString( "validation.dimension.MISSING_NAME" ) );
      valid = false;
    }
    if ( size() == 0 ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.dimension.REQUIRES_AT_LEAST_ONE_HIERARCHY" ) );
      valid = false;
    }
    HashMap<String, HierarchyMetaData> usedNames = new HashMap<String, HierarchyMetaData>();
    for ( HierarchyMetaData hier : children ) {
      hier.validate();
      valid &= hier.isValid();
      validationMessages.addAll( hier.getValidationMessages() );
      if ( usedNames.containsKey( hier.getName() ) ) {
        valid = false;
        String msg =
            ModelerMessagesHolder.getMessages().getString( "validation.dimension.DUPLICATE_HIERARCHY_NAMES",
                hier.getName() );
        validationMessages.add( msg );
        hier.invalidate();
        if ( !hier.getValidationMessages().contains( msg ) ) {
          hier.getValidationMessages().add( msg );
        }
        HierarchyMetaData h = usedNames.get( hier.getName() );
        if ( h.isValid() ) {
          h.invalidate();
          if ( !h.getValidationMessages().contains( msg ) ) {
            h.getValidationMessages().add( msg );
          }
        }

      } else {
        usedNames.put( hier.getName(), hier );
      }
    }
  }

  @Bindable
  public boolean equals( DimensionMetaData obj ) {
    if ( obj instanceof DimensionMetaData ) {
      DimensionMetaData dim = obj;
      return name != null && name.equals( dim.name );
    } else {
      return false;
    }
  }

  @Bindable
  public boolean isTime() {
    return isTimeDimension();
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return false;
  }

  public Class<DimensionPropertiesForm> getPropertiesForm() {
    return DimensionPropertiesForm.class;
  }

  public void onAdd( HierarchyMetaData child ) {
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  public void onRemove( HierarchyMetaData child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return obj instanceof AvailableField || obj instanceof HierarchyMetaData || obj instanceof LevelMetaData
        || obj instanceof MeasureMetaData;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      if ( data instanceof AvailableField ) {
        ColumnBackedNode node =
            getWorkspace().createColumnBackedNode( (AvailableField) data, ModelerPerspective.ANALYSIS );
        return getWorkspace().createHierarchyForParentWithNode( this, node );
      } else if ( data instanceof HierarchyMetaData ) {
        return data;
      } else if ( data instanceof LevelMetaData ) {
        LevelMetaData level = (LevelMetaData) data;
        HierarchyMetaData hier = getWorkspace().createHierarchyForParentWithNode( this, level );
        hier.setName( level.getName() );
        hier.get( 0 ).setName( level.getName() );
        return hier;
      } else if ( data instanceof MeasureMetaData ) {
        MeasureMetaData measure = (MeasureMetaData) data;
        HierarchyMetaData hier = getWorkspace().createHierarchyForParentWithNode( this, measure );
        hier.setName( measure.getName() );
        hier.get( 0 ).setName( measure.getName() );
        return hier;
      } else {
        throw new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString( "invalid_drop" ) );
      }
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }
}
