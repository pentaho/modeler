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

import org.pentaho.agilebi.modeler.nodes.BaseAggregationMetaDataNode;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class MeasuresPropertiesForm extends AbstractModelerNodeForm<BaseAggregationMetaDataNode> {

  protected BaseAggregationMetaDataNode fieldMeta;
  protected Vector aggTypes;
  private String colName;
  private String locale;
  protected AggregationType defaultAggregation;

  public MeasuresPropertiesForm(String panelId, String locale) {
    super(panelId);
    this.locale = locale;
  }

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (evt.getPropertyName().equals("logicalColumn")) {
        setColumnName(fieldMeta.getLogicalColumn());
      } else if (evt.getPropertyName().equals("possibleAggregations")) {
        setPossibleAggregations(new Vector(fieldMeta.getPossibleAggregations()));
      } else if (evt.getPropertyName().equals("defaultAggregation")) {
        setDefaultAggregation(fieldMeta.getDefaultAggregation());
      }
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

  public MeasuresPropertiesForm(String locale) {
    this("measuresprops", locale);
  }


  @Bindable
  public void init() {
    super.init();

    bf.createBinding(this, "notValid", "messages2", "visible");
    bf.createBinding(this, "validMessages", "messages2label", "value");
    bf.createBinding(this, "displayName", "displayname", "value");
    bf.createBinding(this, "possibleAggregations", "defaultAggregation", "elements");
    bf.createBinding(this, "defaultAggregation", "defaultAggregation", "selectedItem");

    bf.createBinding(this, "format", "formatstring", "selectedItem", new FormatStringConverter());
    bf.createBinding(this, "backingColumnAvailable", "fixMeasuresColumnsBtn", "!visible");
    bf.createBinding(this, "columnName", "measure_column_name", "value");

  }

  private void showValidations() {
    setNotValid(!fieldMeta.isValid());
    setBackingColumnAvailable(fieldMeta.getLogicalColumn()!=null);
    setValidMessages(fieldMeta.getValidationMessagesString());
  }

  public void setObject( BaseAggregationMetaDataNode t ) {
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
    if (t.getPossibleAggregations() != null) {
      setPossibleAggregations(new Vector(t.getPossibleAggregations()));
    }
    setDefaultAggregation(t.getDefaultAggregation());
    setValidMessages(t.getValidationMessagesString());
    setColumnName(t.getLogicalColumn());
    this.setDefaultAggregation(t.getDefaultAggregation());
    this.fieldMeta = t;
    showValidations();
  }

  @Bindable
  public void setColumnName( LogicalColumn col ) {
    String prevName = this.colName;
    //TODO: GWT LanguageChoice.getInstance().getDefaultLocale().toString()
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(
        locale) : ""; //$NON-NLS-1$
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
  public boolean isBackingColumnAvailable() {
    if (fieldMeta != null) {
      return fieldMeta.getLogicalColumn() != null;
    } else {
      return false;
    }
  }

  @Bindable
  public void setBackingColumnAvailable(boolean available) {
    this.firePropertyChange("backingColumnAvailable", null, available);
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
  public Vector getPossibleAggregations() {
    return aggTypes;
  }


  @Bindable
  public void setPossibleAggregations( Vector aggTypes ) {
    this.aggTypes = aggTypes;
    this.firePropertyChange("possibleAggregations", null, aggTypes);
  }

  @Bindable
  public AggregationType getDefaultAggregation() {
    return defaultAggregation;
  }

  @Bindable
  public void setDefaultAggregation(AggregationType defaultAggregation) {
    AggregationType previousAggregation = this.defaultAggregation;
    this.defaultAggregation = defaultAggregation;
    if (fieldMeta != null) {
      fieldMeta.setDefaultAggregation(defaultAggregation);
    }
    this.firePropertyChange("defaultAggregation", previousAggregation, defaultAggregation);
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
