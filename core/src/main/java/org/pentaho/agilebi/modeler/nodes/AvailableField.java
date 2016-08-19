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

import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;

public class AvailableField extends AbstractAvailableItem<IAvailableItem> implements IAvailableItem {

  private static final long serialVersionUID = -4430951279551589688L;

  private transient IPhysicalColumn physicalColumn;

  private static final String FIELD_IMAGE = "images/column.png"; //$NON-NLS-1$
  private static final String CLASS_NAME = "pentaho-column"; //$NON-NLS-1$
  public static String MEASURE_PROP = "potential_measure"; //$NON-NLS-1$

  public AvailableField() {
    setImage( FIELD_IMAGE );
    setClassname( CLASS_NAME );
  }

  public AvailableField( IPhysicalColumn physicalColumn ) {
    setPhysicalColumn( physicalColumn );
    setName( physicalColumn.getName( LocalizedString.DEFAULT_LOCALE ) );
    setImage( FIELD_IMAGE );
    setClassname( CLASS_NAME );
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
