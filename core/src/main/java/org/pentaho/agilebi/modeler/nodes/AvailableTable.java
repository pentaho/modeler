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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 4/11/11
 * 
 * @author rfellows
 */
public class AvailableTable extends AbstractAvailableItem<AvailableField> implements IAvailableItem {

  private static final String FACT_TABLE_IMAGE = "images/table_fact.png";
  private static final String DIM_TABLE_IMAGE = "images/table.png";
  private static final String FACT_TABLE_CLASSNAME = "icon-fact-table";
  private static final String DIM_TABLE_CLASSNAME = "icon-table";

  private static final long serialVersionUID = -6428366981250876565L;

  private List<AvailableField> availableFields;
  protected transient IPhysicalTable physicalTable;
  private boolean factTable = false;

  private Comparator<IAvailableItem> itemComparator = new Comparator<IAvailableItem>() {
    public int compare( IAvailableItem arg0, IAvailableItem arg1 ) {
      return arg0.getName().compareTo( arg1.getName() );
    }
  };

  public AvailableTable() {
    availableFields = new ArrayList<AvailableField>();
    setImage( DIM_TABLE_IMAGE );
    setClassname( DIM_TABLE_CLASSNAME );
  }

  public AvailableTable( IPhysicalTable physicalTable ) {
    this( physicalTable, false );
  }

  public AvailableTable( IPhysicalTable physicalTable, boolean isFactTable ) {
    setPhysicalTable( physicalTable );
    setFactTable( isFactTable );
    setImage( isFactTable ? FACT_TABLE_IMAGE : DIM_TABLE_IMAGE );
    setClassname( isFactTable ? FACT_TABLE_CLASSNAME : DIM_TABLE_CLASSNAME );
  }

  protected void populateAvailableFields() {
    if ( physicalTable != null ) {
      for ( IPhysicalColumn column : physicalTable.getPhysicalColumns() ) {
        AvailableField field = new AvailableField( column );
        availableFields.add( field );
      }
    }
    Collections.sort( this.availableFields, itemComparator );
  }

  public List<AvailableField> getAvailableFields() {
    return availableFields;
  }

  public void setAvailableFields( List<AvailableField> availableFields ) {
    this.availableFields = availableFields;
    Collections.sort( this.availableFields, itemComparator );
  }

  public IPhysicalTable getPhysicalTable() {
    return physicalTable;
  }

  public void setPhysicalTable( IPhysicalTable physicalTable ) {
    this.physicalTable = physicalTable;
    setName( physicalTable.getName( LocalizedString.DEFAULT_LOCALE ) );
    availableFields = new ArrayList<AvailableField>();
    populateAvailableFields();
  }

  public boolean isSameUnderlyingPhysicalTable( IPhysicalTable table ) {
    return getPhysicalTable().getId().equals( table.getId() );
  }

  public boolean containsUnderlyingPhysicalColumn( IPhysicalColumn column ) {
    for ( AvailableField field : availableFields ) {
      if ( field.isSameUnderlyingPhysicalColumn( column ) ) {
        return true;
      }
    }
    return false;
  }

  public AvailableField findFieldByPhysicalColumn( IPhysicalColumn column ) {
    for ( AvailableField field : availableFields ) {
      if ( field.isSameUnderlyingPhysicalColumn( column ) ) {
        return field;
      }
    }
    return null;
  }

  public boolean isFactTable() {
    return factTable;
  }

  public void setFactTable( boolean factTable ) {
    this.factTable = factTable;
  }

  @Bindable
  public List<AvailableField> getChildren() {
    return getAvailableFields();
  }

}
