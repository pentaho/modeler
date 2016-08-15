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

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/30/11
 * 
 * @author rfellows
 */
public class CategoryMetaDataCollection extends AbstractMetaDataModelNode<CategoryMetaData> {
  private static final long serialVersionUID = 7083527229930283278L;
  private String name = "Categories";
  private static final String CLASSNAME = "pentaho-smallcategorybutton";

  public CategoryMetaDataCollection() {
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
  public void onAdd( CategoryMetaData child ) {
    child.setParent( this );
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  @Override
  public void onRemove( CategoryMetaData child ) {
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
          "validation.categorycollecion.REQUIRES_AT_LEAST_ONE_CATEGORY" ) );
      valid = false;
    }

    HashMap<String, CategoryMetaData> usedNames = new HashMap<String, CategoryMetaData>();
    for ( CategoryMetaData cat : children ) {
      valid &= cat.isValid();
      validationMessages.addAll( cat.getValidationMessages() );
      if ( usedNames.containsKey( cat.getName() ) ) {
        valid = false;
        String msg =
            ModelerMessagesHolder.getMessages().getString( "validation.categorycollecion.DUPLICATE_CATEGORY_NAMES",
                cat.getName() );
        validationMessages.add( msg );

        cat.invalidate();
        if ( !cat.getValidationMessages().contains( msg ) ) {
          cat.getValidationMessages().add( msg );
        }
        CategoryMetaData c = usedNames.get( cat.getName() );
        if ( c.isValid() ) {
          c.invalidate();
          if ( !c.getValidationMessages().contains( msg ) ) {
            c.getValidationMessages().add( msg );
          }
        }

      } else {
        usedNames.put( cat.getName(), cat );
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

  public boolean equals( CategoryMetaDataCollection obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    CategoryMetaDataCollection other = obj;
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
    return obj instanceof AvailableField || obj instanceof FieldMetaData || obj instanceof AvailableTable
        || obj instanceof CategoryMetaData;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      if ( data instanceof AvailableTable ) {
        AvailableTable table = (AvailableTable) data;
        CategoryMetaData cat = new CategoryMetaData( table.getName() );
        for ( AvailableField field : table.getAvailableFields() ) {
          cat.add( getWorkspace().createFieldForParentWithNode( cat, field ) );
        }
        return cat;
      } else if ( data instanceof AvailableField ) {
        AvailableField field = (AvailableField) data;
        CategoryMetaData cat = new CategoryMetaData( field.getName() );
        cat.add( getWorkspace().createFieldForParentWithNode( cat, field ) );
        return cat;
      } else if ( data instanceof CategoryMetaData ) {
        return data;
      } else {
        throw new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString( "invalid_drop" ) );
      }
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }
}
