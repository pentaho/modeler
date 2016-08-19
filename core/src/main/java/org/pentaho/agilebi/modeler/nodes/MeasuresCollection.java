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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MeasuresCollection extends AbstractMetaDataModelNode<MeasureMetaData> implements Serializable {
  private static final long serialVersionUID = 5827211352596188503L;

  private String name = "Measures"; // BaseMessages.getString(ModelerWorkspace.class, "measures");

  public static String MEASURE_PROP = "potential_measure";
  private static final String CLASSNAME = "pentaho-smallcategorybutton";

  public MeasuresCollection() {
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
          "validation.measurecollecion.REQUIRES_AT_LEAST_ONE_MEASURE" ) );
      valid = false;
    }

    HashMap<String, MeasureMetaData> usedNames = new HashMap<String, MeasureMetaData>();
    for ( MeasureMetaData measure : children ) {
      valid &= measure.isValid();
      validationMessages.addAll( measure.getValidationMessages() );
      if ( usedNames.containsKey( measure.getName() ) ) {
        valid = false;
        String msg =
            ModelerMessagesHolder.getMessages().getString( "validation.measurecollecion.DUPLICATE_MEASURE_NAMES",
                measure.getName() );
        validationMessages.add( msg );

        measure.invalidate();
        if ( !measure.getValidationMessages().contains( msg ) ) {
          measure.getValidationMessages().add( msg );
        }
        MeasureMetaData m = usedNames.get( measure.getName() );
        if ( m.isValid() ) {
          m.invalidate();
          if ( !m.getValidationMessages().contains( msg ) ) {
            m.getValidationMessages().add( msg );
          }
        }

      } else {
        usedNames.put( measure.getName(), measure );
      }

    }
    if ( this.suppressEvents == false ) {
      this.firePropertyChange( "valid", null, valid );
    }
  }

  @Override
  public void onAdd( MeasureMetaData child ) {
    child.setParent( this );
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateNode();
  }

  public void onRemove( MeasureMetaData child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
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
  public boolean acceptsDrop( Object obj ) {
    boolean isSupportedType = false;
    isSupportedType =
        ( obj instanceof AvailableField || obj instanceof AvailableTable || obj instanceof MeasureMetaData );
    if ( !isSupportedType ) {
      return false;
    }

    if ( obj instanceof MeasureMetaData ) {
      return true;
    } else if ( obj instanceof AvailableField ) {
      AvailableField field = (AvailableField) obj;
      if ( isFactTable( field.getPhysicalColumn().getPhysicalTable() )
          || getWorkspace().getAvailableTables().size() == 1 ) {
        return true;
      }
    } else if ( obj instanceof AvailableTable ) {
      AvailableTable field = (AvailableTable) obj;
      if ( isFactTable( field.getPhysicalTable() ) || getWorkspace().getAvailableTables().size() == 1 ) {
        return true;
      }
    }
    return false;
  }

  private boolean isFactTable( IPhysicalTable table ) {
    String agileBiVersion =
        (String) getWorkspace().getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty( "AGILE_BI_VERSION" );
    if ( agileBiVersion != null && Float.parseFloat( agileBiVersion ) >= 2.0 ) {
      // if we're in a multi-table mode check for a fact table
      if ( getWorkspace().getAvailableTables().size() > 1 ) {
        Object factProp = table.getProperty( "FACT_TABLE" );
        if ( factProp == null || factProp.equals( Boolean.FALSE ) ) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      MeasureMetaData measure = null;
      if ( data instanceof AvailableField ) {
        measure = getWorkspace().createMeasureForNode( (AvailableField) data );
      } else if ( data instanceof AvailableTable ) {
        AvailableTable table = (AvailableTable) data;
        List<MeasureMetaData> measureList = new ArrayList<MeasureMetaData>();

        for ( AvailableField field : table.getChildren() ) {
          measureList.add( getWorkspace().createMeasureForNode( field ) );
        }
        // We need to return something back which will be added to the top of the children list. So we'll return the
        // first
        // element we've added. The mechanics following this return will be invisible to the user
        MeasureMetaData firstField = measureList.size() > 0 ? measureList.get( 0 ) : null;
        if ( firstField != null ) {
          measureList.remove( firstField );
        }
        addAll( measureList );
        return firstField;
      } else if ( data instanceof MeasureMetaData ) {
        measure = (MeasureMetaData) data;
        measure.setParent( this );
      } else {
        throw new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString( "invalid_drop" ) );
      }
      String agileBiVersion =
          (String) getWorkspace().getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty( "AGILE_BI_VERSION" );

      if ( measure != null && agileBiVersion != null && Float.parseFloat( agileBiVersion ) >= 2.0 ) {
        // if we're in a multi-table mode check for a fact table
        if ( getWorkspace().getAvailableTables().size() > 1 ) {
          Object factProp = measure.getLogicalColumn().getLogicalTable().getPhysicalTable().getProperty( "FACT_TABLE" );
          if ( factProp == null || factProp.equals( Boolean.FALSE ) ) {
            throw new IllegalStateException( ModelerMessagesHolder.getMessages()
                .getString( "DROP.ERROR.NON_FACT_TABLE" ) );
          }
        }
      }
      return measure;
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }
}
