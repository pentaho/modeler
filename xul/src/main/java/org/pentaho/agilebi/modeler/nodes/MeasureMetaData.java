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

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.MeasuresPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * @author wseyler
 */
@SuppressWarnings( "unchecked" )
public class MeasureMetaData extends BaseAggregationMetaDataNode {
  private static final long serialVersionUID = -7974277299013394857L;
  private static final String IMAGE = "images/sm_measure_icon.png";

  public MeasureMetaData( String locale ) {
    super( locale );
  }

  public MeasureMetaData( String fieldName, String format, String displayName, String locale ) {
    super( fieldName, format, displayName, locale );
  }

  @Override
  @Bindable
  public String getValidImage() {
    return IMAGE; //$NON-NLS-1$
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm<BaseAggregationMetaDataNode>> getPropertiesForm() {
    return MeasuresPropertiesForm.class;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return false;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if ( name == null || "".equals( name ) ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString( "validation.columnnode.MISSING_NAME" ) );
      valid = false;
    }
    if ( logicalColumn == null ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.columnnode.MISSING_BACKING_COLUMN", getName() ) );
      valid = false;
    }
  }

  @Override
  @Bindable
  public void setDefaultAggregation( AggregationType aggType ) {
    // Agg type of NONE is invalid for olap nodes, if that is the value we get, ignore it
    if ( aggType != AggregationType.NONE ) {
      super.setDefaultAggregation( aggType );
    }
  }

  @Override
  public IPhysicalTable getTableRestriction() {
    // restrict to the fact table if one exists
    AvailableTable factTable = getWorkspace().getAvailableTables().findFactTable();
    if ( factTable != null ) {
      return factTable.getPhysicalTable();
    }
    return null;
  }
}
