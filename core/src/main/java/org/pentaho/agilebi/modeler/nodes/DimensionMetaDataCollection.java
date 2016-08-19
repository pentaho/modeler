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
import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DimensionMetaDataCollection extends AbstractMetaDataModelNode<DimensionMetaData> {

  private static final long serialVersionUID = -6327799582519270107L;

  private String name = "Dimensions";
  private static final String CLASSNAME = "pentaho-smallcategorybutton";

  public DimensionMetaDataCollection() {
    super( CLASSNAME );
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

  // TODO: investigate using "this" form of notification in super-class
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this ); //$NON-NLS-1$
  }

  @Override
  public void onAdd( DimensionMetaData child ) {
    child.setParent( this );
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  @Override
  public void onRemove( DimensionMetaData child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    if ( size() == 0 ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.dimcollecion.REQUIRES_AT_LEAST_ONE_MEASURE" ) );
      valid = false;
    }

    HashMap<String, DimensionMetaData> usedNames = new HashMap<String, DimensionMetaData>();
    for ( DimensionMetaData dim : children ) {
      valid &= dim.isValid();
      validationMessages.addAll( dim.getValidationMessages() );
      if ( usedNames.containsKey( dim.getName() ) ) {
        valid = false;
        String msg =
            ModelerMessagesHolder.getMessages().getString( "validation.dimcollection.DUPLICATE_DIMENSION_NAMES",
                dim.getName() );
        validationMessages.add( msg );

        dim.invalidate();
        if ( !dim.getValidationMessages().contains( msg ) ) {
          dim.getValidationMessages().add( msg );
        }
        DimensionMetaData d = usedNames.get( dim.getName() );
        if ( d.isValid() ) {
          d.invalidate();
          if ( !d.getValidationMessages().contains( msg ) ) {
            d.getValidationMessages().add( msg );
          }
        }
      } else {
        usedNames.put( dim.getName(), dim );
      }
    }
    if ( this.suppressEvents == false ) {
      this.firePropertyChange( "valid", null, valid );
    }
  }

  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  @Override
  public Class<GenericPropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    return result;
  }

  public boolean equals( DimensionMetaDataCollection obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    DimensionMetaDataCollection other = obj;
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
    return obj instanceof AvailableField || obj instanceof AvailableTable || obj instanceof LevelMetaData
        || obj instanceof DimensionMetaData;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      if ( data instanceof AvailableTable ) {
        AvailableTable tbl = (AvailableTable) data;
        DimensionMetaData dim = getWorkspace().createDimensionFromAvailableTable( tbl );
        return dim;
      } else if ( data instanceof AvailableField ) {
        ColumnBackedNode node =
            getWorkspace().createColumnBackedNode( (AvailableField) data, ModelerPerspective.ANALYSIS );
        return getWorkspace().createDimensionFromNode( node );
      } else if ( data instanceof MeasureMetaData ) {
        return getWorkspace().createDimensionFromNode( (MeasureMetaData) data );
      } else if ( data instanceof LevelMetaData ) {
        return getWorkspace().createDimensionFromNode( (LevelMetaData) data );
      } else if ( data instanceof DimensionMetaData ) {
        return data;
      } else {
        throw new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString( "invalid_drop" ) );
      }

    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }
}
