/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.propforms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

/*
 * Empty main node property form handler. Used as a key for the interface.
 */
public class MainModelerNodePropertiesForm extends AbstractModelerNodeForm<MainModelNode> implements Serializable {
  private static final long serialVersionUID = 2257973330712395088L;
  private XulTextbox name;
  private XulVbox messageBox;
  private XulLabel messageLabel;

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( !evt.getPropertyName().equals( "valid" ) ) {
        return;
      }
      showValidations();
    }
  };
  private XulButton messageBtn;

  public MainModelerNodePropertiesForm() {
    super( "mainprops" );
  }

  public void setObject( MainModelNode dim ) {
    if ( this.getNode() == dim ) {
      return;
    }
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( propListener );
    }
    setNode( dim );
    if ( dim == null ) {
      return;
    }

    name.setValue( dim.getName() );

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( dim, "name", name, "value" );

    showValidations();
    dim.addPropertyChangeListener( propListener );
  }

  private void showValidations() {
    if ( getNode() != null ) {
      messageBox.setVisible( getNode().getValidationMessages().size() > 0 );
      setValidMessages( getNode().getValidationMessagesString() );
    }
  }

  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    name = (XulTextbox) document.getElementById( "main_name" );
    messageBox = (XulVbox) document.getElementById( "main_message" );
    messageLabel = (XulLabel) document.getElementById( "mainnode_message_label" );
    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bf.createBinding( this, "name", name, "value" );
    bf.createBinding( this, "validMessages", messageLabel, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "mainnode_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );
  }

  @Bindable
  public void setName( String name ) {
    if ( getNode() != null ) {
      getNode().setName( name );
    }
    this.name.setValue( name );
  }

  @Bindable
  public String getName() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getName();
  }

  @Override
  public String getValidMessages() {
    if ( getNode() != null ) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }

}
