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

package org.pentaho.agilebi.modeler.nodes;

import java.util.HashMap;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.CategoryPropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

public class CategoryMetaData extends AbstractMetaDataModelNode<FieldMetaData> {

  private static final String IMAGE = "images/sm_folder_icon.png";
  private static final String CLASSNAME = "pentaho-smallcategorybutton icon-zoomable";
  private static final long serialVersionUID = 7879805619425103630L;
  String name;

  public CategoryMetaData() {
    super( CLASSNAME );
    getMessageStringAndSetAltText( "modeler.alternative_text.category" );
  }

  public CategoryMetaData( String name ) {
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

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public void validate() {
    // make sure there is at least one field
    valid = true;
    this.validationMessages.clear();

    if ( this.children.size() == 0 ) {
      valid = false;
      this.validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.category.REQUIRES_AT_LEAST_ONE_FIELD" ) );
    }

    HashMap<String, FieldMetaData> usedNames = new HashMap<String, FieldMetaData>();
    for ( FieldMetaData child : children ) {
      valid &= child.isValid();
      this.validationMessages.addAll( child.getValidationMessages() );
      if ( usedNames.containsKey( child.getName() ) ) {
        valid = false;
        String dupeString =
            ModelerMessagesHolder.getMessages()
                .getString( "validation.category.DUPLICATE_FIELD_NAMES", child.getName() );
        validationMessages.add( dupeString );

        child.invalidate();
        if ( !child.getValidationMessages().contains( dupeString ) ) {
          child.getValidationMessages().add( dupeString );
        }

        FieldMetaData dupe = usedNames.get( child.getName() );
        if ( dupe.isValid() ) {
          dupe.invalidate();
          if ( !dupe.getValidationMessages().contains( dupeString ) ) {
            dupe.getValidationMessages().add( dupeString );
          }
        }
      } else {
        usedNames.put( child.getName(), child );
      }
    }
    if ( this.suppressEvents == false ) {
      this.firePropertyChange( "valid", null, valid );
    }
  }

  @Override
  public Class<CategoryPropertiesForm> getPropertiesForm() {
    return CategoryPropertiesForm.class;
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
  public void onAdd( FieldMetaData child ) {
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  @Override
  public void onRemove( FieldMetaData child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return obj instanceof AvailableField || obj instanceof FieldMetaData;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      if ( data instanceof AvailableField ) {
        AvailableField field = (AvailableField) data;
        return getWorkspace().createFieldForParentWithNode( this, field );
      } else if ( data instanceof FieldMetaData ) {
        ( (FieldMetaData) data ).setParent( this );
        return data;
      } else {
        return null;
      }
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }
}
