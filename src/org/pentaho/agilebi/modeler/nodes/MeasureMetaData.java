/*
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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.propforms.MeasuresPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * @author wseyler
 */
@SuppressWarnings("unchecked")
public class MeasureMetaData extends BaseAggregationMetaDataNode {

  private static final String IMAGE = "images/sm_measure_icon.png";

  public MeasureMetaData(String locale) {
    super(locale);
  }

  public MeasureMetaData( String fieldName, String format, String displayName, String locale ) {
    super(fieldName, format, displayName, locale);
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
  public boolean acceptsDrop(Object obj) {
    return false;
  }
}
