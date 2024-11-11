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

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;

public class GenericPropertiesForm extends AbstractModelerNodeForm<AbstractMetaDataModelNode> {

  private XulLabel messageLabel;
  private XulButton messageBtn;

  private PropertyChangeListener validListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( !evt.getPropertyName().equals( "valid" ) ) {
        return;
      }
      showValidations();

    }
  };

  public GenericPropertiesForm() {
    super( "genericProps" );
  }

  public GenericPropertiesForm( String deckName ) {
    super( deckName );
  }

  public void setObject( AbstractMetaDataModelNode node ) {
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( validListener );
    }
    setNode( node );
    if ( node == null ) {
      return;
    }
    getNode().addPropertyChangeListener( validListener );
    showValidations();
  }

  private void showValidations() {
    if ( node == null ) {
      return;
    }
    setValidMessages( getNode().getValidationMessagesString() );
  }

  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    messageLabel = (XulLabel) document.getElementById( "generic_message_label" );
    bf.createBinding( this, "validMessages", messageLabel, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "generic_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );
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
