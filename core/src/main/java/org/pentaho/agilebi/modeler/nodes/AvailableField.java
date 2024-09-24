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

import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;

public class AvailableField extends AbstractAvailableItem<IAvailableItem> implements IAvailableItem {

  private static final long serialVersionUID = -4430951279551589688L;

  private transient IPhysicalColumn physicalColumn;

  private static final String FIELD_IMAGE = "images/column.png";
  private static final String CLASS_NAME = "pentaho-column icon-zoomable";
  public static String MEASURE_PROP = "potential_measure";

  public AvailableField() {
    setImage( FIELD_IMAGE );
    setClassname( CLASS_NAME );
    getMessageStringAndSetAltText( "modeler.alternative_text.availableField" );
  }

  public AvailableField( IPhysicalColumn physicalColumn ) {
    setPhysicalColumn( physicalColumn );
    setName( physicalColumn.getName( LocalizedString.DEFAULT_LOCALE ) );
    setImage( FIELD_IMAGE );
    setClassname( CLASS_NAME );
    getMessageStringAndSetAltText( "modeler.alternative_text.availableField" );
  }

  public IPhysicalColumn getPhysicalColumn() {
    return physicalColumn;
  }

  public void setPhysicalColumn( IPhysicalColumn physicalColumn ) {
    this.physicalColumn = physicalColumn;
  }

  public boolean isSameUnderlyingPhysicalColumn( IPhysicalColumn column ) {
    IPhysicalTable table = column.getPhysicalTable();

    return getPhysicalColumn().getId().equals( column.getId() )
        && getPhysicalColumn().getPhysicalTable().getId().equals( table.getId() );

  }
}
