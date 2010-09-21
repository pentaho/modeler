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

import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<LevelMetaData> {

  private XulTextbox name;
  private XulLabel sourceLabel;
  private XulLabel level_message_label;
  private XulVbox messageBox;
  private String colName;


  private PropertyChangeListener validListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("valid") && !evt.getPropertyName().equals("logicalColumn")) {
        return;
      }
      showValidations();
    }
  };

  private LevelMetaData dim;

  public LevelsPropertiesForm() {
    super("levelprops");
  }

  public void setObject( LevelMetaData dim ) {
    if (this.dim != null) {
      this.dim.removePropertyChangeListener(validListener);
    }

    this.dim = dim;
    if (dim == null) {
      return;
    }
    this.dim.addPropertyChangeListener(validListener);

    name.setValue(dim.getName());
    setColumnName(dim.getLogicalColumn());
    showValidations();
  }

  private void showValidations() {
    if (dim == null) {
      return;
    }
    messageBox.setVisible(dim.getValidationMessages().size() > 0);
    level_message_label.setValue(dim.getValidationMessagesString());
    setNotValid(!dim.isValid());
    setColumnName(dim.getLogicalColumn());
  }

  public void init() {
    super.init();
    bf.createBinding(this, "notValid", "level_message", "visible");
    name = (XulTextbox) document.getElementById("level_name");
    sourceLabel = (XulLabel) document.getElementById("level_source_col");
    level_message_label = (XulLabel) document.getElementById("level_message_label");
    messageBox = (XulVbox) document.getElementById("level_message");
    bf.createBinding(this, "notValid", "fixLevelColumnsBtn", "visible");

    bf.createBinding(this, "columnName", sourceLabel, "value");


    bf.createBinding(this, "name", name, "value");

  }

  @Bindable
  public void setColumnName( LogicalColumn col ) {
    String prevName = this.colName;
    //TODO: GWT locale
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(
        "en_US") : ""; //$NON-NLS-1$
    this.firePropertyChange("columnName", prevName, this.colName); //$NON-NLS-1$
  }

  @Bindable
  public String getColumnName() {
    return colName;
  }

  @Bindable
  public void setName( String name ) {
    if (dim != null) {
      dim.setName(name);
    }
    this.name.setValue(name);
  }

  @Bindable
  public String getName() {
    if (dim == null) {
      return null;
    }
    return dim.getName();
  }

  @Bindable
  public boolean isNotValid() {
    if (dim != null) {
      return !dim.isValid();
    } else {
      return false;
    }
  }

  @Bindable
  public void setNotValid( boolean notValid ) {
    this.firePropertyChange("notValid", null, notValid);
  }

}
