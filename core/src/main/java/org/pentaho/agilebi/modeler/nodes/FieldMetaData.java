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
import org.pentaho.agilebi.modeler.propforms.FieldsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.Arrays;
import java.util.List;

/**
 * Created: 3/18/11
 * 
 * @author rfellows
 */
public class FieldMetaData extends BaseAggregationMetaDataNode {

  private static final String IMAGE = "images/fields.png";
  private static final long serialVersionUID = -7091129923372909756L;

  public FieldMetaData( String locale ) {
    super( locale );
  }

  public FieldMetaData( CategoryMetaData parent, String fieldName, String format, String displayName, String locale ) {
    super( fieldName, format, displayName, locale );
    setParent( parent );
  }

  @Override
  public List<AggregationType> getNumericAggregationTypes() {
    return Arrays.asList( AggregationType.NONE, AggregationType.SUM, AggregationType.AVERAGE, AggregationType.MINIMUM,
        AggregationType.MAXIMUM, AggregationType.COUNT, AggregationType.COUNT_DISTINCT );
  }

  @Override
  public List<AggregationType> getTextAggregationTypes() {
    return Arrays.asList( AggregationType.NONE, AggregationType.COUNT, AggregationType.COUNT_DISTINCT );
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm<BaseAggregationMetaDataNode>> getPropertiesForm() {
    return FieldsPropertiesForm.class;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if ( name == null || "".equals( name ) ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString( "validation.field.MISSING_NAME" ) );
      valid = false;
    }
    if ( logicalColumn == null ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString( "validation.field.MISSING_BACKING_COLUMN",
          getName() ) );
      valid = false;
    }
  }

  @Bindable
  public AggregationType getDefaultAggregation() {
    if ( logicalColumn == null ) {
      return null;
    }
    if ( defaultAggregation == null ) {
      defaultAggregation = AggregationType.NONE;
    }
    return defaultAggregation;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return false;
  }

}
