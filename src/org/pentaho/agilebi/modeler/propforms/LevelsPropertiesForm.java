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

import org.pentaho.agilebi.modeler.nodes.BaseColumnBackedMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<BaseColumnBackedMetaData> {

  private XulTextbox name;
  private XulLabel sourceLabel;
  private XulLabel level_message_label;
  private XulVbox messageBox;
  private String colName;
  private String locale;
  private XulButton messageBtn;

  public LevelsPropertiesForm(String panelId, String locale) {
    super(panelId);
    this.locale = locale;
  }

  private PropertyChangeListener validListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("valid") && !evt.getPropertyName().equals("logicalColumn")) {
        return;
      }
      showValidations();
    }
  };

  public LevelsPropertiesForm(String locale) {
    this("levelprops", locale);
  }

  public void setObject( BaseColumnBackedMetaData dim ) {
    if (getNode() != null) {
      getNode().removePropertyChangeListener(validListener);
    }

    setNode(dim);
    if (dim == null) {
      return;
    }
    getNode().addPropertyChangeListener(validListener);

    name.setValue(dim.getName());
    setColumnName(dim.getLogicalColumn());
    showValidations();
  }

  private void showValidations() {
    if (getNode() == null) {
      return;
    }

    setNotValid(!getNode().isValid());
    setBackingColumnAvailable(getNode().getLogicalColumn()!=null);
    setColumnName(getNode().getLogicalColumn());
    messageBox.setVisible(getNode().getValidationMessages().size() > 0);
    setValidMessages(getNode().getValidationMessagesString());
  }

  public void init() {
    super.init();
    bf.createBinding(this, "notValid", "level_message", "visible");
    name = (XulTextbox) document.getElementById("level_name");
    sourceLabel = (XulLabel) document.getElementById("level_source_col");
    level_message_label = (XulLabel) document.getElementById("level_message_label");
    messageBox = (XulVbox) document.getElementById("level_message");
    bf.createBinding(this, "backingColumnAvailable", "fixLevelColumnsBtn", "!visible");

    bf.createBinding(this, "columnName", sourceLabel, "value");
    bf.createBinding(this, "name", name, "value");
    bf.createBinding(this, "validMessages", level_message_label, "value", validMsgTruncatedBinding);
    messageBtn = (XulButton) document.getElementById("level_message_btn");
    bf.createBinding(this, "validMessages", messageBtn, "visible", showMsgBinding);

  }

  @Bindable
  public void setColumnName( LogicalColumn col ) {
    String prevName = this.colName;
    //TODO: GWT locale
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(
        locale) : ""; //$NON-NLS-1$
    this.firePropertyChange("columnName", prevName, this.colName); //$NON-NLS-1$
  }

  @Bindable
  public String getColumnName() {
    return colName;
  }

  @Bindable
  public void setName( String name ) {
    if (getNode() != null) {
      getNode().setName(name);
    }
    this.name.setValue(name);
  }

  @Bindable
  public String getName() {
    if (getNode() == null) {
      return null;
    }
    return getNode().getName();
  }

  @Bindable
  public boolean isNotValid() {
    if (getNode() != null) {
      return !getNode().isValid();
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
    if (getNode() != null) {
      return getNode().getLogicalColumn() != null;
    } else {
      return false;
    }
  }

  @Bindable
  public void setBackingColumnAvailable(boolean available) {
    this.firePropertyChange("backingColumnAvailable", null, available);
  }

  @Override
  public String getValidMessages()  {
    if (getNode() != null) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }
}
