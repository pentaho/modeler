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
package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

public class MeasuresPropertiesForm extends AbstractModelerNodeForm<MeasureMetaData> {

  private MeasureMetaData fieldMeta;
  private Vector aggTypes;
  private String colName;

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("logicalColumn")) {
        return;
      }
      setColumnName(fieldMeta.getLogicalColumn());
    }
  };

  private PropertyChangeListener validListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("valid")) {
        return;
      }
      showValidations();
    }
  };

  public MeasuresPropertiesForm() {
    super("measuresprops");
  }


  @Bindable
  public void init() {
    super.init();

    bf.createBinding(this, "notValid", "messages2", "visible");
    bf.createBinding(this, "validMessages", "messages2label", "value");
    bf.createBinding(this, "displayName", "displayname", "value");
    bf.createBinding(this, "aggTypes", "aggregationtype", "elements");
    bf.createBinding(this, "aggTypeDesc", "aggregationtype", "selectedItem");
    bf.createBinding(this, "format", "formatstring", "selectedItem", new FormatStringConverter());
    bf.createBinding(this, "notValid", "fixMeasuresColumnsBtn", "visible");
    bf.createBinding(this, "columnName", "measure_column_name", "value");

  }

  private void showValidations() {
    setNotValid(!fieldMeta.isValid());
    setValidMessages(fieldMeta.getValidationMessagesString());
  }

  public void setObject( MeasureMetaData t ) {
    if (fieldMeta != null) {
      fieldMeta.removePropertyChangeListener(validListener);
      fieldMeta.removePropertyChangeListener(propListener);
      fieldMeta = null;
    }
    if (t == null) {
      return;
    }

    t.addPropertyChangeListener(validListener);
    t.addPropertyChangeListener(propListener);

    setDisplayName(t.getName());
    setFormat(t.getFormat());
    setAggTypes(t.getAggTypeDescValues());
    setAggTypeDesc(t.getAggTypeDesc());
    setValidMessages(t.getValidationMessagesString());
    setColumnName(t.getLogicalColumn());
    this.fieldMeta = t;
    showValidations();
  }

  @Bindable
  public void setColumnName( LogicalColumn col ) {
    String prevName = this.colName;
    //TODO: GWT LanguageChoice.getInstance().getDefaultLocale().toString()
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(
        "en_US") : ""; //$NON-NLS-1$
    this.firePropertyChange("columnName", prevName, this.colName); //$NON-NLS-1$
  }

  @Bindable
  public String getColumnName() {
    return colName;
  }

  @Bindable
  public boolean isNotValid() {
    if (fieldMeta != null) {
      return !fieldMeta.isValid();
    } else {
      return false;
    }
  }

  @Bindable
  public void setNotValid( boolean notValid ) {
    this.firePropertyChange("notValid", null, notValid);
  }

  @Bindable
  public void setValidMessages( String validMessages ) {
    this.firePropertyChange("validMessages", null, validMessages);
  }

  @Bindable
  public String getValidMessages() {
    if (fieldMeta != null) {
      return fieldMeta.getValidationMessagesString();
    } else {
      return null;
    }
  }

  @Override
  public String getName() {
    return "propertiesForm";
  }

  @Bindable
  public String getDisplayName() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getName();
  }

  @Bindable
  public void setDisplayName( String displayName ) {
    if (fieldMeta != null) {
      fieldMeta.setName(displayName);
    }
    this.firePropertyChange("displayName", null, displayName);

  }

  @Bindable
  public String getFormat() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getFormat();
  }

  @Bindable
  public void setFormat( String format ) {

    if (fieldMeta != null) {
      fieldMeta.setFormat(format);
    }
    this.firePropertyChange("format", null, format);
  }

  @Bindable
  public String getAggTypeDesc() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getAggTypeDesc();
  }

  @Bindable
  public void setAggTypeDesc( String aggTypeDesc ) {
    String prevVal = null;

    if (fieldMeta != null) {
      fieldMeta.getAggTypeDesc();
      fieldMeta.setAggTypeDesc(aggTypeDesc);
    }
    this.firePropertyChange("aggTypeDesc", prevVal, aggTypeDesc);
  }


  @Bindable
  public Vector getAggTypes() {
    return aggTypes;
  }


  @Bindable
  public void setAggTypes( Vector aggTypes ) {
    this.aggTypes = aggTypes;
    this.firePropertyChange("aggTypes", null, aggTypes);
  }


  /**
   * @author wseyler
   */
  private static class FormatStringConverter extends BindingConvertor<String, String> {

    @Override
    public String sourceToTarget( String value ) {
      if (value == null) {
        return "NONE";
      } else {
        return value;
      }
    }

    @Override
    public String targetToSource( String value ) {
      if (value.equalsIgnoreCase("NONE")) {
        return null;
      } else {
        return value;
      }
    }

  }
}
