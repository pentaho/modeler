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

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.propforms.MeasuresPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author wseyler
 */
@SuppressWarnings("unchecked")
public class MeasureMetaData extends AbstractMetaDataModelNode implements Serializable, ColumnBackedNode {

  public static final String FORMAT_NONE = "NONE"; //$NON-NLS-1$

  String name;
  String format = FORMAT_NONE;
  String displayName;
  String fieldTypeDesc = "---";
  String levelTypeDesc = "---";
  String aggTypeDesc = null;
  transient LogicalColumn logicalColumn;

  private List<String> numericAggTypes = new ArrayList<String>();

  {
    numericAggTypes.add("SUM");
    numericAggTypes.add("AVERAGE");
    numericAggTypes.add("MINIMUM");
    numericAggTypes.add("MAXIMUM");
    numericAggTypes.add("COUNT");
  }

  private List<String> textAggTypes = Collections.singletonList("COUNT");

  public MeasureMetaData() {

  }

  public MeasureMetaData( String fieldName, String format, String displayName ) {
    super();
    this.name = fieldName;
    this.format = format;
    this.displayName = displayName;
  }

  public String toString() {
    return name;
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
    if (!(name == null || "".equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange("name", oldName, name); //$NON-NLS-1$
      this.firePropertyChange("displayName", oldName, name); //$NON-NLS-1$
      validateNode();
      if (logicalColumn != null) {
        //TODO: GWT i18n
        logicalColumn.setName(new LocalizedString("en_US", name));//Locale.getDefault().toString(), name));
      }
    }
  }

  @Bindable
  public String getFormat() {
    if (format == null || "".equals(format) || "#".equals(format)) {
      return FORMAT_NONE;
    }
    return format;
  }

  @Bindable
  public void setFormat( String format ) {
    this.format = format;
  }

  @Bindable
  public String getFieldTypeDesc() {
    return fieldTypeDesc;
  }

  @Bindable
  public void setFieldTypeDesc( String fieldTypeDesc ) {
    this.fieldTypeDesc = fieldTypeDesc;
  }

  @Bindable
  public String getLevelTypeDesc() {
    return levelTypeDesc;
  }

  @Bindable
  public void setLevelTypeDesc( String levelTypeDesc ) {
    this.levelTypeDesc = levelTypeDesc;
  }

  @Bindable
  public String getAggTypeDesc() {
    if (logicalColumn == null) {
      return null;
    }
    if (aggTypeDesc == null || "".equals(aggTypeDesc)) {
      switch (logicalColumn.getDataType()) {
        case NUMERIC:
          aggTypeDesc = "SUM";
          break;
        default:
          aggTypeDesc = "COUNT";
      }
    }
    return aggTypeDesc;
  }

  @Bindable
  public void setAggTypeDesc( String aggTypeDesc ) {
    this.aggTypeDesc = aggTypeDesc;
  }

  // TODO: generate this based on field type

  @Bindable
  public Vector getAggTypeDescValues() {
    if (logicalColumn == null) {
      return null;
    }
    if (logicalColumn.getDataType() == DataType.NUMERIC) {
      return new Vector<String>(numericAggTypes);
    } else {
      return new Vector<String>(textAggTypes);
    }
  }

  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn( LogicalColumn col ) {
    LogicalColumn prevVal = this.logicalColumn;
    this.logicalColumn = col;
    validateNode();
    firePropertyChange("logicalColumn", prevVal, col);
  }

  public boolean equals( MeasureMetaData o ) {
    if (o == null || o instanceof MeasureMetaData == false) {
      return false;
    }
    MeasureMetaData f = (MeasureMetaData) o;

    if (o == this) {
      return true;
    }

    if (f.getLogicalColumn() == null || this.getLogicalColumn() == null) {
      return false;
    }

    if (f.getLogicalColumn().getId().equals(this.getLogicalColumn().getId())) {
      return true;
    }
    return false;
  }

  @Bindable
  public void setUiExpanded() {

  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return "images/sm_measure_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if (name == null || "".equals(name)) {
      validationMessages.add(
          "Measure Name Missing");//BaseMessages.getString(ModelerWorkspace.class, "measure_name_missing"));
//      validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "measure_name_missing"));
      valid = false;
    }
    if (logicalColumn == null) {
      validationMessages.add("measure_column_missing");
//      validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "measure_column_missing", getName()));
      valid = false;
    }
  }

  public boolean isEditingDisabled() {
    return false;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm<MeasureMetaData>> getPropertiesForm() {
    return MeasuresPropertiesForm.class;
  }


}