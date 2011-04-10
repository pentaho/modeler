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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;

public class AvailableField implements Serializable {

  private static final long serialVersionUID = -4430951279551589688L;
  
  private transient IPhysicalColumn physicalColumn;
  private String name, displayName;

  public static String MEASURE_PROP = "potential_measure";

  public AvailableField(){
    
  }


  public AvailableField(IPhysicalColumn physicalColumn) {
    setPhysicalColumn(physicalColumn);
    setName(physicalColumn.getName(LocalizedString.DEFAULT_LOCALE));
    setDisplayName(physicalColumn.getName(LocalizedString.DEFAULT_LOCALE));
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName(String name) {
    this.name = name;
  }

  @Bindable
  public String getDisplayName() {
    return displayName;
  }

  @Bindable
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }



  public String toString() {
    return name;
  }

  public IPhysicalColumn getPhysicalColumn() {
    return physicalColumn;
  }

  public void setPhysicalColumn(IPhysicalColumn physicalColumn) {
    this.physicalColumn = physicalColumn;
  }

  public boolean isSameUnderlyingPhysicalColumn(IPhysicalColumn column) {
    IPhysicalTable table = column.getPhysicalTable();

    return getPhysicalColumn().getId().equals(column.getId()) &&
           getPhysicalColumn().getPhysicalTable().getId().equals(table.getId());

  }
    
  public boolean isPossibleMeasure(){
    String measureProp = (String) getLogicalColumn().getPhysicalColumn().getProperty(MEASURE_PROP);
    return measureProp != null && measureProp.equals("true");
  }

  public void setPossibleMeasure(boolean possibleMeasure){
    getLogicalColumn().getPhysicalColumn().setProperty(MEASURE_PROP, ""+possibleMeasure);
  }
  
}
