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

import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMetaDataModelNode<T extends AbstractMetaDataModelNode> extends AbstractModelNode<T> implements
                                                                                                                  Serializable {

  private static final long serialVersionUID = 1547202580713108254L;

  protected boolean valid = true;
  protected transient List<String> validationMessages = new ArrayList<String>();
  protected String image;
  protected boolean suppressEvents;
  protected boolean expanded;

  protected transient PropertyChangeListener validListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent arg0 ) {
      validateNode();
    }
  };

  protected transient PropertyChangeListener nameListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent arg0 ) {
      validateNode();
    }
  };

  protected transient PropertyChangeListener childrenListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      fireCollectionChanged();
    }
  };

  public AbstractMetaDataModelNode() {
    this.image = getInvalidImage();
  }


  @Override
  public void onAdd( T child ) {
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateTree();
  }

  @Override
  public void onRemove( T child ) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }

  public String getValidationMessagesString() {
    String str = ""; //$NON-NLS-1$
    for (int i = 0; i < validationMessages.size(); i++) {
      if (i > 0) {
        str += ", "; //$NON-NLS-1$
      }
      str += validationMessages.get(i);
    }
    return str;
  }

  public List<String> getValidationMessages() {
    return validationMessages;
  }

  @Override
  protected void fireCollectionChanged() {
    if (this.suppressEvents == false) {
      super.fireCollectionChanged();
    }
  }

  @Bindable
  public void setImage( String image ) {
    if (this.image == null || !this.image.equals(image)) {
      String oldimg = this.image;
      this.image = image;
      if (suppressEvents == false) {
        this.firePropertyChange("image", oldimg, image); //$NON-NLS-1$
      }
    }
  }

  public abstract String getValidImage();

  @Bindable
  public final String getInvalidImage() {
    return "images/warning.png"; //$NON-NLS-1$
  }

  @Bindable
  public String getImage() {
    return (this.valid) ? getValidImage() : getInvalidImage();
  }

  public abstract void validate();

  public void validateNode() {
    boolean prevValid = valid;
    String prevMessages = getValidationMessagesString();
    validate();
    if (suppressEvents == false) {
      this.firePropertyChange("validationMessagesString", prevMessages, getValidationMessagesString());
      this.firePropertyChange("valid", prevValid, valid);
    }
    if (prevValid != valid) {
      if (valid) {
        setImage(getValidImage());
      } else {
        setImage(getInvalidImage());
      }
    }
  }


  @SuppressWarnings("unchecked")
  public void validateTree() {
    for (T t : this) {
      ((AbstractMetaDataModelNode) t).validateTree();
    }
    validateNode();
  }

  ;

  @SuppressWarnings("unchecked")
  public boolean isTreeValid() {
    if (!isValid()) {
      return false;
    }
    for (T t : this) {
      if (!((AbstractMetaDataModelNode) t).isValid()) {
        return false;
      }
    }
    return true;
  }

  public boolean isValid() {
    return valid;
  }


  public abstract Class<? extends ModelerNodePropertiesForm> getPropertiesForm();

  public void setSupressEvents( boolean suppress ) {
    this.suppressEvents = suppress;
    for (T child : this) {
      child.setSupressEvents(suppress);
    }
  }

  @Bindable
  public boolean isExpanded() {
    return expanded;
  }

  @Bindable
  public void setExpanded( boolean expanded ) {
    this.expanded = expanded;
  }

}
